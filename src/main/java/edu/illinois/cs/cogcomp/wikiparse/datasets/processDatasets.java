package edu.illinois.cs.cogcomp.wikiparse.datasets;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikiparse.datasets.eval.UIUCEvaluator;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Created by nitishgupta on 10/20/16.
 */
public class processDatasets {
	private static String labelDir;
	private static String textDir;
	private static String dataset = "ACE";

	public static final String uiucPath = "/save/ngupta19/WikificationACL2011Data/";
	public static final String general_datasetPath = "/save/ngupta19/datasets/";


	private static void setDirectories(String dataset) {
		switch (dataset) {
			case "MSNBC":
				labelDir = uiucPath + "MSNBC/Problems/";
				textDir = uiucPath + "MSNBC/RawTextsSimpleChars/";
				break;
			case "ACE":
				labelDir =
								uiucPath + "ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/";
				textDir =
								uiucPath + "ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/";
				break;
			default:
				System.out.println("No Such dataset : " + dataset);
		}
	}


	private static void runUiuc(String dataset) {
		setDirectories(dataset);
		int numNotFound = 0, numNilGoldWpid = 0;
		int totalNonNullMentions = 0;
		Set<String> goldEntities = new HashSet<>();
		for (String file : new File(labelDir).list()) {
			//System.out.println(file);
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
				//System.out.println(mention + "\t" + goldSet.get(key));
				goldEntities.add(goldSet.get(key));
			}
		}
		System.out.println("Non Null Mentions : " + totalNonNullMentions);
	}


	private static void writeGoldEntities(String dataset) {
		setDirectories(dataset);
		String outputPath1 = general_datasetPath + dataset + "goldEntities_completeDataset.txt";
		String outputPath2 = general_datasetPath + dataset + "goldEntities_perDocument.txt";
		StringBuffer goldEntites_string = new StringBuffer();
		StringBuffer goldEntitesperDoc_string = new StringBuffer();
		Set<String> goldEntities = new HashSet<>();
		for (String file : new File(labelDir).list()) {
			String doc = file;
			Set<String> gentities = new HashSet<>();
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);
			for (Pair<Integer, Integer> key : goldSet.keySet()) {
				String entity = goldSet.get(key);
				if (!goldEntities.contains(entity)) {
					goldEntites_string.append(entity + "\n");
				}
				if (!gentities.contains(entity)) {
					goldEntitesperDoc_string.append(entity + "\n");
				}
				goldEntities.add(entity);
				gentities.add(entity);
			}
		}
		FileUtils.writeStringToFile(outputPath1, goldEntites_string.toString().trim());
		FileUtils.writeStringToFile(outputPath2, goldEntitesperDoc_string.toString().trim());
	}

	public static void overalapWithKB(String dataset) {
		System.out.println("[#] Finding Overlap of Gold Entities with KB Entities in " + dataset);
		setDirectories(dataset);
		List<String> titlesNotFound = new ArrayList<>();
		List<String> notFoundInKB = new ArrayList<>();
		Map<String, String> foundInKB = new HashMap<>();
		int totalEntities = 0, titleNotFound = 0, notinKB = 0;
		for (String file : new File(labelDir).list()) {
			String doc = file;
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);

			for (Pair<Integer, Integer> key : goldSet.keySet()) {
				String wiki_Title = goldSet.get(key).replaceAll(" ", "_");
				Page page;
				try {
					page = WikiDB.wiki.getPage(wiki_Title);
					if (page.isRedirect()) {
						String new_page = page.getRedirects().iterator().next();
						page = WikiDB.wiki.getPage(new_page);
					}
				} catch (Exception e) {
					page = null;
				}
				if (page == null) {
					titlesNotFound.add(wiki_Title);
					titleNotFound++;
				} else {
					String wid = Integer.toString(page.getPageId());
					if (!KB.wids_ParsedInWiki.contains(wid)) {
						notFoundInKB.add(wiki_Title);
						notinKB++;
					} else {
						foundInKB.put(wid, wiki_Title);
					}
				}
				totalEntities++;
			}
		}
		System.out.println("[#] Total Gold Entities (with repetition) found in Dataset : " + totalEntities);
		System.out.println("[#] Title not found : " + titleNotFound);
		System.out.println("[#] Not found in KB : " + notinKB);
		StringBuffer notfoundinwiki = new StringBuffer();
		StringBuffer notfoundinkb = new StringBuffer();
		StringBuffer foundinkb = new StringBuffer();
		for (String title : titlesNotFound) {
			notfoundinwiki.append(title + "\n");
		}
		for (String title : notFoundInKB) {
			notfoundinkb.append(title + "\n");
		}
		for (String wid : foundInKB.keySet()) {
			foundinkb.append(wid + "\t" + foundInKB.get(wid) + "\n");
		}
		FileUtils.writeStringToFile(general_datasetPath + dataset + "/notFoundInWiki.txt", notfoundinwiki.toString());
		FileUtils.writeStringToFile(general_datasetPath + dataset + "/notFoundInKB.txt", notfoundinkb.toString());
		FileUtils.writeStringToFile(general_datasetPath + dataset + "/foundInKB.txt", foundinkb.toString());

	}

	public static void main(String[] args) throws Exception {
		overalapWithKB("ACE");
		overalapWithKB("MSNBC");
		System.exit(1);
	}
}
