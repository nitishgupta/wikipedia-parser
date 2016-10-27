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

import java.io.IOException;
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

	public static void textParsing(String outputDir) throws WikiApiException {
		ExecutorService executor = Executors.newFixedThreadPool(45);
		PageIterator iterator = new PageIterator(WikiDB.wiki, true, 100000);

		int i = 0, totaldocs = 0;
		while (iterator.hasNext()) {
			Page page = iterator.next();
			totaldocs++;
			if (KB.widMidMap_FoundInWiki.containsKey(Integer.toString(page.getPageId()))) {
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


	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		//KB.widMidMap_FoundInWiki.keySet().size();
		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);


		System.out.println("Total Time : " + tt + "  minutes");
		//writeWikipediaDocuments();
		textParsing("/save/ngupta19/wikipedia/wiki_kb_10_15/docs_links/");
		System.exit(1);
	}
}
