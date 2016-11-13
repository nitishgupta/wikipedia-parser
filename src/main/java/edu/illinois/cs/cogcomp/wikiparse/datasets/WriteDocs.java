package edu.illinois.cs.cogcomp.wikiparse.datasets;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wikiparse.datasets.eval.UIUCEvaluator;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
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
	private static String dataset = "msnbc";

	public static final String uiucPath = "/save/ngupta19/WikificationACL2011Data/";
	public static final String datasetPath = "/save/ngupta19/datasets/";
	public static TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());

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
	// Input wikiTitle is space delimited and coming from a database.
	// Converts it to the the WikiId if found in our KB.
	public static String getWikiId(String wikiTitle) {
		String wiki_Title = wikiTitle.replaceAll(" ", "_");
		Page page = null;
		try {
			page = WikiDB.wiki.getPage(wiki_Title);
			if (page.isRedirect()) {
				String new_page = page.getRedirects().iterator().next();
				page = WikiDB.wiki.getPage(new_page);
			}
		} catch (Exception e) {
			return null;
		}
		if (page != null) {
			String wid = Integer.toString(page.getPageId());
			if (KB.wids_ParsedInWiki.contains(wid)) {
				return wid;
			}
		}
		return null;
	}

	public static StringBuffer getMentions(String text, Map<Pair<Integer, Integer>, String> goldSet, String doc_id) {
		TextAnnotation ta = tab.createTextAnnotation("", "", text);
		StringBuffer mentions = new StringBuffer();
		for (Pair<Integer, Integer> key : goldSet.keySet()) {
			int men_start = key.getFirst();
			int men_end = key.getSecond();
			int start_token_id = ta.getTokenIdFromCharacterOffset(men_start);
			Sentence s1 = ta.getSentenceFromToken(start_token_id);
			String wid = getWikiId(goldSet.get(key));
			if (wid == null)
				continue;

			String mid = KB.wid2mid.get(wid);
			String mention_surface = text.substring(men_start, men_end).replaceAll("\\s", " ").trim();

			StringBuffer mention = new StringBuffer();
			mention.append(mid + "\t");
			mention.append(wid + "\t");
			mention.append(mention_surface + "\t");
			mention.append(s1.getTokenizedText().replaceAll("\\s", " ").trim() + "\t");
			mention.append(doc_id);

			mentions.append(mention);
			mentions.append("\n");
		}
		return mentions;
	}

	public static void MentionsWriter(String outfile) throws Exception {
		System.out.println("Writing corpus mentions : " + outfile);
		StringBuffer corpus_mentions = new StringBuffer();
		for (String file : new File(labelDir).list()) {
			String doc = file;
			System.out.println(doc);
			// Getting offsets for gold mentions
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);
			String text = FileUtils.getTextFromFile(textDir + doc, "Windows-1252");

			StringBuffer mentions = getMentions(text, goldSet, doc);
			corpus_mentions.append(mentions);
		}
		FileUtils.writeStringToFile(outfile, corpus_mentions.toString().trim());
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

	public static void main(String[] args) throws Exception {

		switch (dataset) {
			case "msnbc":
				labelDir = uiucPath + "MSNBC/Problems/";
				textDir = uiucPath + "MSNBC/RawTextsSimpleChars/";
				outputDir = datasetPath + "MSNBC/docs_links_gold/";
				//DocsWriter();
				MentionsWriter(datasetPath + "MSNBC/mentions.txt");
				break;
			case "ace":
				labelDir = uiucPath + "ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/";
				textDir = uiucPath + "ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/";
				outputDir = datasetPath + "ACE/docs_links_gold/";
				MentionsWriter(datasetPath + "ACE/mentions.txt");
				break;
			default:
				System.out.println("No Such dataset : " + dataset);
		}
		System.exit(1);
	}
}
