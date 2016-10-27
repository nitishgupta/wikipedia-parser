package edu.illinois.cs.cogcomp.wikiparse.datasets;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wikiparse.datasets.eval.UIUCEvaluator;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class WriteDocs {
	private static String labelDir;
	private static String textDir;
	private static String outputDir;
	private static String dataset = "ace";

	public static final String uiucPath = "/save/ngupta19/WikificationACL2011Data/";
	public static final String datasetPath = "/save/ngupta19/datasets/";
	public static TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());

	public static void main(String[] args) throws Exception {

		switch (dataset) {
			case "msnbc":
				labelDir = uiucPath + "MSNBC/Problems/";
				textDir = uiucPath + "MSNBC/RawTextsSimpleChars/";
				outputDir = datasetPath + "MSNBC/docs_links_gold/";
				DocsWriter();
				break;
			case "ace":
				labelDir = uiucPath + "ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/";
				textDir = uiucPath + "ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/";
				outputDir = datasetPath + "ACE/docs_links_gold/";
				DocsWriter();
				break;
			default:
				System.out.println("No Such dataset : " + dataset);
		}
	}

	public static void DocsWriter() throws Exception {
		for (String file : new File(labelDir).list()) {
			String doc = file;
			System.out.println(doc);
			// Getting offsets for gold mentions
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);
			String text = FileUtils.getTextFromFile(textDir + doc, "Windows-1252");
			StringBuffer links = new StringBuffer();
			for (Pair<Integer, Integer> key : goldSet.keySet()) {
				String mention = text.substring(key.getFirst(), key.getSecond());
				mention = mention.replaceAll("\\s", " ");
				links.append(mention.trim() + " ");
				System.out.println(mention);
			}
			String links_string = links.toString().trim();
			String text_string = getTokenizedText(text);

			BufferedWriter bw = new BufferedWriter(new FileWriter(outputDir + doc));
			bw.write(text_string);
			bw.write("\t");
			bw.write(links_string);
			bw.close();
		}
	}

	public static String getTokenizedText(String text) {
		TextAnnotation ta = tab.createTextAnnotation("", "", text);
		StringBuffer sbuf = new StringBuffer();
		List<Sentence> sentences = ta.sentences();
		for (Sentence sent : sentences) {
			sbuf.append(sent.getTokenizedText().replaceAll("\\s", " ").trim());
			sbuf.append(" <eos_word> ");
		}
		return sbuf.toString().trim();
	}


	private static void runUiuc() {
		int numNotFound = 0, numNilGoldWpid = 0;
		int totalNonNullMentions = 0;
		for (String file : new File(labelDir).list()) {
			System.out.println(file);
			String doc = file;
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);
			String text = FileUtils.getTextFromFile(textDir + doc, "Windows-1252");
			for (Pair<Integer, Integer> key : goldSet.keySet()) {
								/*
                 * After setting up JWPL find what pages are missing from our dataset.
                 */
				totalNonNullMentions++;
				String mention = text.substring(key.getFirst(), key.getSecond());
				System.out.println(mention + "\t" + goldSet.get(key));

			}
		}
		System.out.println("Non Null Mentions : " + totalNonNullMentions);
	}
}
