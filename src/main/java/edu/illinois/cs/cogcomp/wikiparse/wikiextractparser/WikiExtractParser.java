package edu.illinois.cs.cogcomp.wikiparse.wikiextractparser;

/**
 * Created by nitishgupta on 2/6/17.
 */

import de.tudarmstadt.ukp.wikipedia.api.Page;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.PageParseWorker;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Currently in Testing Mode.
 *
 * Functions to write :
 * 		Takes one file and breaks the docs in it.
 * 		Takes text of doc and gives clean text with offsets for surface with linked WikiTitle
 * 		Makes TextAnnotation on clean text and matches surface tokens to WikiTitles
 * 	 	Writes the data for sentences with relevant entities
 */

/**
 * Output : Single or Split files,
 * 	One line per training sample
 * 	mid wid wikititle start_token end_token surface tokenized_sentence all_types
 */

public class WikiExtractParser {
	private Logger logger = Logger.getLogger("WikiExtractParser");
	public String logfile = System.getProperty("user.dir") + "/Mentions.log";
	private FileHandler fh;
	private ThreadPoolExecutor parser = null;

	public static final int sentenceLengthThreshold = 31;
	public static final String parsedWikiDirectory = "/save/ngupta19/enwiki/20160920/en/EN_WIKI_PARSED/";
	public static final String outputWikiMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/mentions/";

	public WikiExtractParser() {
		parser = getBoundedThreadPool();
		System.out.println(logfile);
		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler(logfile);
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			logger.setUseParentHandlers(false);
			// the following statement is used to log any messages
			logger.info("Static Function");

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ThreadPoolExecutor getBoundedThreadPool() {
		int coreCount = Runtime.getRuntime().availableProcessors();
		coreCount = Math.max(coreCount, 10);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
						coreCount, // Core count
						//coreCount / 2 + 1, // Core count
						coreCount, // Pool Max
						60, TimeUnit.SECONDS, // Thread keep alive time
						new ArrayBlockingQueue<Runnable>(coreCount),// Queue
						new ThreadPoolExecutor.CallerRunsPolicy()// Blocking mechanism
		);
		executor.allowCoreThreadTimeOut(true);
		return executor;
	}

	private String _getCorrespondingOutFilepath(String infilepath, String outDir, int dirpathlength) {
		String infilepathminusroot = infilepath.substring(dirpathlength);
		String outfilepath = new File(outDir, infilepathminusroot).toString();
		return outfilepath;
	}

	public void writeWikiMentions() {
		long startTime = System.currentTimeMillis();
		long rep_startTime = System.currentTimeMillis();
		// Dir path to parsed Wikipedia. This dir contains multiple nested dirs with multiple files.
		File inDir = new File(parsedWikiDirectory);
		// This dir will replicate the dir/file structure in 'inDir'. Files will contain mentions corresponsing to docs in
		// original file
		String outDir = outputWikiMentionsDir;
		Iterator<File> i = org.apache.commons.io.FileUtils.iterateFiles(inDir, null, true);
		int cf = 0;
		int mentions = 0;
		int dirpathlength = inDir.toString().length();
		while (i.hasNext()) {
			File file = i.next();
			String infilepath = file.toString();
			String outfilepath = _getCorrespondingOutFilepath(infilepath, outDir, dirpathlength);

			// Give this to thread runner
			parser.execute(new FileParseWorker(infilepath, outfilepath, logger));
			//int m = writeMentionsForFile(infilepath, outfilepath);
			//mentions += m;
			cf++;

			//if (cf % 100 == 0) {
				long estimatedTime = System.currentTimeMillis() - rep_startTime;
				double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
				rep_startTime = System.currentTimeMillis();
				System.out.print(cf + " (" + mentions + ", " + tt + " mins) .. ");
			//}
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
		System.out.println("[#] Total Files: " + cf);
		System.out.println("[#] Total Mentions : " + mentions);
		System.out.print("[#] Total Processing Time : " + tt + " mins");
	}

	public static void main (String [] args ) {
		System.out.println("/* Wikipedia Extractor Parser */");
		//List<String> docTexts = breakDocs("/save/ngupta19/enwiki/20160920/en/EN_WIKI_PARSED/AA/wiki_00");

		//TextAnnotation ta = tab.createTextAnnotation("", "", "\n");

		WikiExtractParser wikiparser = new WikiExtractParser();
		wikiparser.logger.info("Starting to Write Wiki Mentions");
		wikiparser.writeWikiMentions();
	}
}
