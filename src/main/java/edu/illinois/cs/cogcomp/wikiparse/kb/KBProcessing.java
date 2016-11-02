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
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.lazy.parser.LazyRatsParser;
import org.sweble.wikitext.lazy.utils.ParserShouldNotBeHereException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

	public static String getLinks(Page page) {
		StringBuffer sbuf = new StringBuffer();
		Set<Page> outlinks = page.getOutlinks();
		for (Page p : outlinks) {
			try {
				String title = p.getTitle().toString();
				title = title.replaceAll("\\s", " ").trim();
				sbuf.append(title + " ");
			} catch (WikiTitleParsingException e) {
			}
		}
		return sbuf.toString().trim();
	}

	public static String getText(Page page) {
		StringBuffer sbuf = new StringBuffer();
		int sentences_written = 0;

		try {
			String[] paras = page.getPlainText().split("\n");
			for (String para : paras) {
				TextAnnotation ta = tab.createTextAnnotation("", "", para);
				if (ta.sentences().size() <= 1)
					continue; // Most probably Section Headings.
				for (Sentence sent : ta.sentences()) {
					String text = sent.getTokenizedText().replaceAll("\\s", " ").trim();
					if (!(text.split(" ").length < 4)) {
						sbuf.append(text);
						sbuf.append(" <eos_word> ");
						sentences_written++;
						if (sentences_written >= max_sentences)
							break;
					}
				}
				if (sentences_written >= max_sentences)
					break;
			}
			if (sentences_written < min_sentences)
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\n\nDoc Failed : " + page.getPageId());
			return null;
		}

		tab.createTextAnnotation("", "", "asd");
		return sbuf.toString().trim();


	}

	public static String getTextAndLinks(Page page) {
		String text = getText(page);
		if (text == null)
			return null;
		String links = getLinks(page);
		StringBuffer sbf = new StringBuffer();
		sbf.append(text);
		sbf.append("\t");
		sbf.append(links);
		return sbf.toString();
	}


	static class ProcessPage implements Runnable {
		String textandlinks;
		String outputFilePath;

		public ProcessPage(String textandlinks, String outputpath) {
			this.textandlinks= textandlinks;
			this.outputFilePath = outputpath;
		}

		public void run() {
			FileUtils.writeStringToFile(outputFilePath, textandlinks);
		}
	}

	public static void writeWikiDocs(String outputDir) throws WikiApiException {
		ExecutorService executor = Executors.newFixedThreadPool(45);
		PageIterator iterator = new PageIterator(WikiDB.wiki, true, 100000);

		int i = 0, totaldocs = 0;
		while (iterator.hasNext()) {
			Page page = iterator.next();
			totaldocs++;
			if (KB.wids_FoundInWiki.contains(Integer.toString(page.getPageId()))) {
				String outpath = outputDir + page.getPageId();
				String textandlinks = getTextAndLinks(page);
				if (textandlinks == null)
					continue;
				ProcessPage pp = new ProcessPage(textandlinks, outpath);
				executor.submit(pp);
				if (i % 10000 == 0)
					System.out.print(i + " .. ");
				i++;
			}
			if (i % 100000 == 0)
				System.out.print("(" + i + ")" + " .. ");
		}
		System.out.println("\nDONEEE");
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdownNow();
		System.out.println("DONEEEEEEEE");
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
		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);


		System.out.println("Total Time : " + tt + "  minutes");
		//writeWikipediaDocuments();
		//writeWikiDocs("/save/ngupta19/wikipedia/wiki_kb_10_15/docs_links/");
		writeMentions("/save/ngupta19/wikipedia/wiki_kb_10_15/docs_links/",
									"/save/ngupta19/wikipedia/wiki_kb_10_15/mentions_new/");
		System.exit(1);
	}
}
