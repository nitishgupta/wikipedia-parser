package edu.illinois.cs.cogcomp.wikiparse.datasets.uiuc;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wikiparse.datasets.eval.UIUCEvaluator;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class UIUCMentionWriter {
	private static String labelDir;
	private static String textDir;
	private static String outputDocsDir;
	private static String dataset = "wiki_train";
	private static String processDatasetPath;

	public static TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
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

	public static class MentionsInDoc {
		String doc_id;
		StringBuffer mentions;
		Set<String> notFoundInKB;
		Set<String> notFoundInRedirect;
		Set<String> notFoundInWiki;
		Set<String> foundInWiki_notInKBRed;
		int total_mentions = 0;
		int non_unk_mentions = 0;

		public MentionsInDoc(String text, Map<Pair<Integer, Integer>, String> goldSet, String doc_id) {
			notFoundInKB = new HashSet<>();
			notFoundInRedirect  = new HashSet<>();
			notFoundInWiki = new HashSet<>();
			foundInWiki_notInKBRed  = new HashSet<>();
			this.doc_id = doc_id;

			TextAnnotation ta = tab.createTextAnnotation("", "", text);
			mentions = new StringBuffer();
			for (Pair<Integer, Integer> key : goldSet.keySet()) {
				total_mentions++;
				int men_start = key.getFirst();
				int men_end = key.getSecond() - 1;

				int startTokenInDoc = ta.getTokenIdFromCharacterOffset(men_start);  // Token Location in Doc
				int endTokenInDoc = ta.getTokenIdFromCharacterOffset(men_end);      // Token Location in Doc

				Sentence sent = ta.getSentenceFromToken(startTokenInDoc);
				int sentStartToken = sent.getStartSpan();                        // Token location for sent start
				int surfaceSentStart = startTokenInDoc - sentStartToken;        // Token loc. in sent
				int surfaceSentEnd = endTokenInDoc - sentStartToken;            // Token loc. in sent

				int start_token_id = ta.getTokenIdFromCharacterOffset(men_start);
				Sentence s1 = ta.getSentenceFromToken(start_token_id);
				String[] tokens = sent.getTokens();
				String sentenceSurface = sent.getTokenizedText();

				// Surface
				StringBuilder surfaceBuider = new StringBuilder();
				for (int i = surfaceSentStart; i <= surfaceSentEnd; i++)
					surfaceBuider.append(tokens[i]).append(" ");
				String surface = surfaceBuider.toString().trim();

				// WID and WikiTitle
				String true_wikit = goldSet.get(key).replace(" ", "_");
				String wt_KB = KB.KBWikiTitle(true_wikit);
				String wid = KB.wikiTitle2WID(wt_KB);

				// MID
				String mid;
				if (KB.wid2mid.containsKey(wid)) {
					mid = KB.wid2mid.get(wid);
				} else {
					mid = "<unk_mid>";
				}

				StringBuilder typestxt = new StringBuilder();
				if (KB.mid2typelabels.containsKey(mid)) {
					Set<String> types = KB.mid2typelabels.get(mid);
					for (String t : types) {
						typestxt.append(t).append(" ");
					}
				} else {
					typestxt.append("<NULL_TYPES>");
				}

				StringBuffer mention = new StringBuffer();
				mention.append(mid).append("\t");
				mention.append(wid).append("\t");
				mention.append(wt_KB).append("\t");
				mention.append(Integer.toString(surfaceSentStart)).append("\t");
				mention.append(Integer.toString(surfaceSentEnd)).append("\t");
				mention.append(surface).append("\t");
				mention.append(sentenceSurface).append("\t");
				mention.append(typestxt.toString().trim());
				
				mentions.append(mention).append("\n");
			}

//			System.out.println(this.doc_id);
//			System.out.println("NOT IN KB : " + notFoundInKB.toString());
//			System.out.println("NOT IN REDIRECT : " + notFoundInRedirect.toString());
//			System.out.println("FOUND IN WIKI but NOT IN KB : " + foundInWiki_notInKBRed.toString());
//			System.out.println("NOT FOUND IN WIKI : " + notFoundInWiki.toString());
		}
	}

	public static StringBuffer getMentions(String text, Map<Pair<Integer, Integer>, String> goldSet, String doc_id) {
		TextAnnotation ta = tab.createTextAnnotation("", "", text);
		StringBuffer mentions = new StringBuffer();
		for (Pair<Integer, Integer> key : goldSet.keySet()) {
			int men_start = key.getFirst();
			int men_end = key.getSecond();
			int start_token_id = ta.getTokenIdFromCharacterOffset(men_start);
			Sentence s1 = ta.getSentenceFromToken(start_token_id);

			String true_wikit = goldSet.get(key);
			boolean foundinKB = KB.wikiTitle2Wid.containsKey(true_wikit);
			boolean foundinredirect = false;
			if (!foundinKB) {
				foundinredirect = KB.redirect2WikiTitle.containsKey(true_wikit);
			}
			String wid = "";
			if (foundinKB)
				wid = KB.wikiTitle2Wid.get(true_wikit);
			else if (foundinredirect)
				wid = KB.wikiTitle2Wid.get(KB.redirect2WikiTitle.get(true_wikit));
//			String wid = getWikiId(goldSet.get(key));
//			if (wid == null) {
//				System.out.println(goldSet.get(key));
//				continue;
//			}

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
		int num_nonnull_mentions = 0;
		int num_mentions = 0;
		int num_nonunk_mentions = 0;
		for (String file : new File(labelDir).list()) {
			String doc = file;
			System.out.println(doc);
			// Getting offsets for gold mentions
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);


			String text = FileUtils.getTextFromFile(textDir + doc, "Windows-1252");
			//StringBuffer mentions = getMentions(text, goldSet, doc);
			MentionsInDoc mentionsindoc = new MentionsInDoc(text, goldSet, doc);
			corpus_mentions.append(mentionsindoc.mentions);
			num_nonnull_mentions += goldSet.size();
			num_mentions += mentionsindoc.total_mentions;
			num_nonunk_mentions += mentionsindoc.non_unk_mentions;
		}
		FileUtils.writeStringToFile(outfile, corpus_mentions.toString().trim());
		System.out.println("Num of non-null mentions : " + num_nonnull_mentions);
		System.out.println("Total Mentions according to function : " + num_mentions);
		System.out.println("Total Non Unk Mentions : " + num_nonunk_mentions);
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

		dataset = "ace";

		switch (dataset) {
			case "msnbc":
				labelDir = Constants.uiucDataSetRootPath + "MSNBC/Problems/";
				textDir = Constants.uiucDataSetRootPath  + "MSNBC/RawTextsSimpleChars/";
				processDatasetPath = Constants.processDatasetRootPath + "MSNBC/";
				MentionsWriter(processDatasetPath + "mentions.txt");
				break;
			case "ace":
				labelDir = Constants.uiucDataSetRootPath + "ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/";
				textDir = Constants.uiucDataSetRootPath + "ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/";
				processDatasetPath = Constants.processDatasetRootPath + "ACE/";
				MentionsWriter(processDatasetPath + "mentions.txt");
				break;
			case "wiki_train":
				labelDir = Constants.uiucDataSetRootPath + "WikipediaSample/ProblemsTrain/";;
				textDir = Constants.uiucDataSetRootPath + "WikipediaSample/RawTextsTrain/";
				processDatasetPath = Constants.processDatasetRootPath + "WIKI/";
				MentionsWriter(processDatasetPath + "mentions_train.txt");
				break;
			case "wiki_test":
				labelDir = Constants.uiucDataSetRootPath + "WikipediaSample/ProblemsTest/";;
				textDir = Constants.uiucDataSetRootPath + "WikipediaSample/RawTextsTest/";
				processDatasetPath = Constants.processDatasetRootPath + "WIKI/";
				MentionsWriter(processDatasetPath + "mentions_test.txt");
				break;
			default:
				System.out.println("No Such dataset : " + dataset);
		}
		System.exit(1);
	}
}
