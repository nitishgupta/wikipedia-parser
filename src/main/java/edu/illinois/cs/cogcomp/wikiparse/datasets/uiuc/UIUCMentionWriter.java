package edu.illinois.cs.cogcomp.wikiparse.datasets.uiuc;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wikiparse.PredMentions.PredMentions;
import edu.illinois.cs.cogcomp.wikiparse.datasets.eval.UIUCEvaluator;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.illinois.cs.cogcomp.wikiparse.wikiextractparser.Mention;

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

	public static PredMentions predmenAnnotator = new PredMentions();

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
		//StringBuffer mentions;
		public List<Mention> mentions;
		StringBuffer mentionsstring;
		Set<String> notFoundInKB;
		Set<String> notFoundInRedirect;
		Set<String> notFoundInWiki;
		Set<String> foundInWiki_notInKBRed;
		int total_mentions = 0;
		int non_unk_mentions = 0;
		List<Boolean> predMensBool;

		public MentionsInDoc(String text, Map<Pair<Integer, Integer>, String> goldSet, String doc_id) {
			predMensBool = new ArrayList<>();
			notFoundInKB = new HashSet<>();
			notFoundInRedirect  = new HashSet<>();
			notFoundInWiki = new HashSet<>();
			foundInWiki_notInKBRed  = new HashSet<>();
			this.doc_id = doc_id;

			TextAnnotation ta = tab.createTextAnnotation("", "", text);

			mentions = new ArrayList<>();
			Set<String> mentionSurfaces = new HashSet<>(); // This is the list for coherence. Surfaces are joined by _
			for (Pair<Integer, Integer> key : goldSet.keySet()) {
				try {
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
					mentionSurfaces.add(surface.replaceAll(" ", "_"));    // Adding mention surface to set of surfaces in doc for coherence

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

					Mention m = new Mention(mid, wid, wt_KB, surface, sentenceSurface, typestxt.toString().trim(),
									"", surfaceSentStart, surfaceSentEnd); // Mention wihout coherence
					m.addDocId(doc_id);
					mentions.add(m);
				} catch (Exception e) {

				}
			}
			StringBuilder coherence_mentions = new StringBuilder();		// Make coherence stringbuilder
			for (String mentionsurface : mentionSurfaces) {
				coherence_mentions.append(mentionsurface.trim()).append(" ");
			}
			String coherence_string = coherence_mentions.toString().trim();		// Make coherence mentions string
			for (Mention m : mentions) {		// Add coherence mentions to all mentions of this doc
				m.updateCoherence(coherence_string);
			}

			mentionsstring = new StringBuffer();
			for (Mention m : mentions) {
				mentionsstring.append(m.toString());
			}

			for (Mention m : mentions) {
				Boolean r = predmenAnnotator.annotateNER(m.sentence, m.startTokenidx, m.endTokenidx);
				predMensBool.add(r);
			}
		}
	}


	public static void MentionsWriter(String outfile) throws Exception {
		System.out.println("Writing corpus mentions : " + outfile);
		StringBuffer corpus_mentions = new StringBuffer();
		int num_nonnull_mentions = 0;
		int num_mentions = 0;
		int num_nonunk_mentions = 0;
		String outf = "/save/ngupta19/datasets/ACE/BOT.gold.all";
		StringBuffer botgold = new StringBuffer();
		for (String file : new File(labelDir).list()) {
			String doc = file;
			System.out.println(doc);
			// Getting offsets for gold mentions
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);


			String text = FileUtils.getTextFromFile(textDir + doc, "Windows-1252");
			//StringBuffer mentions = getMentions(text, goldSet, doc);
			MentionsInDoc mentionsindoc = new MentionsInDoc(text, goldSet, doc);
			corpus_mentions.append(mentionsindoc.mentionsstring);
			num_nonnull_mentions += goldSet.size();
			num_mentions += mentionsindoc.total_mentions;
			num_nonunk_mentions += mentionsindoc.non_unk_mentions;

			StringBuffer docbot = new StringBuffer();
			docbot.append(doc).append("\t");
			Set<String> bots = new HashSet<>();
			for (int i = 0; i < mentionsindoc.mentions.size(); i++) {
					String wid = mentionsindoc.mentions.get(i).wid;
					if (!wid.equals("<unk_wid>"))
						bots.add(wid);
			}
			if (bots.size() > 0) {
				for (String wid : bots) {
					docbot.append(wid).append(" ");
				}
				botgold.append(docbot.toString().trim()).append("\n");
			}
		}

		FileUtils.writeStringToFile(outf, botgold.toString());
		FileUtils.writeStringToFile(outfile, corpus_mentions.toString().trim());
		System.out.println("Num of non-null mentions : " + num_nonnull_mentions);
		System.out.println("Total Mentions according to function : " + num_mentions);
		System.out.println("Total Non Unk Mentions : " + num_nonunk_mentions);
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
				processDatasetPath = Constants.processDatasetRootPath + "ACE/wcoh/";
				MentionsWriter(processDatasetPath + "mentions_xxx.txt");
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
