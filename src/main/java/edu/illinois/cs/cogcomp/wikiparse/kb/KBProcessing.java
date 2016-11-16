package edu.illinois.cs.cogcomp.wikiparse.kb;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.TypesafeMap;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.lazy.parser.LazyRatsParser;
import org.sweble.wikitext.lazy.utils.ParserShouldNotBeHereException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitishgupta on 10/22/16.
 */
public class KBProcessing {
	public static TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
	public static int max_sentences = 15;
	public static int min_sentences = 10;
	public static int num_sent_per_mention = 4;

	public static class TextLinks {
		String pageId;
		String text;
		String links;
		int num_sentences;
		boolean wikifail;
		boolean tafail;

		public TextLinks (Page page) {
			this.wikifail = false;
			this.tafail = false;
			this.text = "";
			this.links = "";
			this.num_sentences = 0;
			this.pageId = Integer.toString(page.getPageId());
			getText(page);
			if (this.text != null && this.links != null && this.num_sentences != 0)
				getLinks(page);
		}

		public void getText(Page page) {
			StringBuffer sbuf = new StringBuffer();
			int sentences_written = 0;
			String pagePlainText = null;
			try {
				pagePlainText = page.getPlainText();
			} catch (WikiApiException e) {
				this.wikifail = true;
				this.text = null;
				this.num_sentences = 0;
				this.links = null;
				System.out.println("Wiki Parsing Doc Failed : " + page.getPageId());
				return;
			}
			try {
				// Different Paragraphs in Wiki Page
				// Splitting to remove Section Headings
				String[] paras = pagePlainText.split("\n");
				for (String para : paras) {
					TextAnnotation ta = tab.createTextAnnotation("", "", para);
					// Most probably Section Headings.
					if (ta.sentences().size() <= 1)
						continue;
					// If legitimate section para
					for (Sentence sent : ta.sentences()) {
						int num_tokens_in_sent = sent.getTokens().length;
						if (!(num_tokens_in_sent < 4)) {
							String text = sent.getTokenizedText().replaceAll("\\s", " ").trim();
							sbuf.append(text);
							sbuf.append(" "); sbuf.append("<eos_word>"); sbuf.append(" ");
							sentences_written++;
							if (sentences_written >= max_sentences)
								break;
						}
					}
					if (sentences_written >= max_sentences)
						break;
				}
			} catch (Exception e) {
				this.tafail = true;
				this.text = null;
				this.num_sentences = 0;
				this.links = null;
				e.printStackTrace();
				System.out.println("TA Doc Failed : " + page.getPageId());
				return;
			}

			String text = sbuf.toString().trim();
			if (text.equals("")) {
				this.text = null;
				this.num_sentences = 0;
				this.links = null;
			} else {
				this.text = text;
				this.num_sentences = sentences_written;
			}
	}

		public void getLinks(Page page) {
			StringBuffer sbuf = new StringBuffer();
			Set<Page> outlinks = page.getOutlinks();
			for (Page p : outlinks) {
				try {
					String title = p.getTitle().toString();
					title = title.replaceAll("\\s", " ").trim();
					sbuf.append(title);
					sbuf.append("\t");
				} catch (WikiTitleParsingException e) {
				}
			}
			String links = sbuf.toString().trim();
			if (links.equals(""))
				links = "<unk_word>";

			this.links = links;
		}

	}

	static class ProcessPage implements Runnable {
		String text;
		String links;
		String outputDocsPath;
		String outputLinksPath;

		public ProcessPage(String text, String links, String outputDocsPath, String outputLinksPath) {
			this.text = text;
			this.links = links;
			this.outputDocsPath = outputDocsPath;
			this.outputLinksPath = outputLinksPath;
		}

		public void run() {
			FileUtils.writeStringToFile(outputDocsPath, this.text);
			FileUtils.writeStringToFile(outputLinksPath, this.links);
		}
	}

	public static void writeWikiDocs(String outputKBDir) throws Exception {
		System.out.println("Writing WIkipedia Docs .. ");
		String outputDocsDir = outputKBDir + "docs/";
		String outputCoherenceDir = outputKBDir + "links/";
		BufferedWriter bw_wikifail = new BufferedWriter(new FileWriter(outputKBDir + "wiki_failed"));
		BufferedWriter bw_tafail = new BufferedWriter(new FileWriter(outputKBDir + "ta_failed"));

		System.out.println("Writing docs to : " + outputDocsDir);
		System.out.println("Writing links to : " + outputCoherenceDir);
		System.out.println("Failed Wiki Docs to : " + outputKBDir + "wiki_failed");
		System.out.println("Failed Annotation Docs to : " + outputKBDir + "ta_failed");

		ExecutorService executor = Executors.newFixedThreadPool(45);
		PageIterator iterator = new PageIterator(WikiDB.wiki, true, 100000);

		int docs_processed = 0, docs_written = 0;
		while (iterator.hasNext()) {
			Page page = iterator.next();
			try {
				if (!page.isRedirect() && !page.isDisambiguation() && !page.isDiscussion() &&
						!page.getTitle().getPlainTitle().startsWith("List of") &&
						!page.getTitle().getPlainTitle().startsWith("Lists of")) {

					docs_processed++;
					String outputDocPath = outputDocsDir + Integer.toString(page.getPageId());
					String outputLinksPath = outputCoherenceDir + Integer.toString(page.getPageId());
					TextLinks textlinks = new TextLinks(page);
					// Page Failed OR Empty
					if (textlinks.text == null && textlinks.num_sentences == 0 && textlinks.links == null) {
						if (textlinks.wikifail) {
							bw_wikifail.write(page.getPageId());
							bw_wikifail.write("\n");
						} else if (textlinks.tafail) {
							bw_tafail.write(page.getPageId());
							bw_tafail.write("\n");
						}
					} else {
						ProcessPage pp = new ProcessPage(textlinks.text, textlinks.links, outputDocPath, outputLinksPath);
						executor.submit(pp);
						docs_written++;
					}
					if (docs_processed % 100 == 0) {
						System.out.print(docs_processed + " (" +
										docs_written + ") ... ");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nDONEEE");
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdownNow();
		System.out.println("DONEEEEEEEE");
		bw_tafail.close();
		bw_wikifail.close();
		return;
	}

	static class WriteDocMentions implements Runnable {
		String infolder;
		String outfolder;
		String infile;

		public WriteDocMentions(String infolder, String outfolder, String infile) {
			this.infolder = infolder;
			this.outfolder = outfolder;
			this.infile = infile;
		}
		public void run() {
			String wid = infile;
			String doc_links = FileUtils.getTextFromFile(infolder + infile, "UTF-8");
			String [] split = doc_links.split("\t");
			String[] doc_tokens = split[0].trim().split(" ");
			int sentences_written = 0;

			String mid = KB.wid2mid.get(wid);
			List<String> aliases = KB.mid2aliases.get(mid);
			String wikiTitle = KB.wid2WikiTitle.get(wid).replaceAll("_", " ");
			if (!aliases.contains(wikiTitle))
				aliases.add(wikiTitle);

			List<String> sentencesToWrite = new ArrayList<>();
			String sent = "";
			for (String token : doc_tokens) {
				if (!(sentences_written < num_sent_per_mention))
					break;
				if (token.equals("<eos_word>")) {
					sent += "<eos_word>";
					sentencesToWrite.add(sent);
					sent = "";
					sentences_written++;
				} else {
					sent = sent + token + " ";
				}
			}
			int mention_written = 0;
			for (String sentence : sentencesToWrite) {
				for (String name_alias : aliases) {
					StringBuffer toWrite = new StringBuffer();
					toWrite.append(mid);
					toWrite.append("\t");
					toWrite.append(wid);
					toWrite.append("\t");
					toWrite.append(name_alias);
					toWrite.append("\t");
					toWrite.append(sentence);
					toWrite.append("\t");
					toWrite.append(wid);

					FileUtils.writeStringToFile(outfolder + wid + "_"+ Integer.toString(mention_written),
						toWrite.toString().trim());
					mention_written++;
				}
			}
		}

	}

	public static void writeMentions(String docs_folder, String out_folder) {
		ExecutorService executor = Executors.newFixedThreadPool(45);
		int docs_written = 0;
		for (String wid : new File(docs_folder).list()) {
			WriteDocMentions wr = new WriteDocMentions(docs_folder, out_folder, wid);
			executor.submit(wr);
			docs_written++;
			if (docs_written % 10000 == 0)
				System.out.print(docs_written + " .. ");
		}
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdownNow();
		System.out.println("DONEEEEEEEE");

	}


	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		//KB.widMidMap_FoundInWiki.keySet().size();

		System.out.println("\n\n " + WikiDB.getPage("Eddie Vedder").getTitle().getWikiStyleTitle() + "\n\n");



		//writeWikipediaDocuments();
		writeWikiDocs("/save/ngupta19/wikipedia/wiki_kb/");
//		writeMentions("/save/ngupta19/wikipedia/wiki_kb_10_15/docs_links/",
//									"/save/ngupta19/wikipedia/wiki_kb_10_15/mentions_new/");




		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
		System.out.println("Total Time : " + tt + "  minutes");

		System.exit(1);
	}
}
