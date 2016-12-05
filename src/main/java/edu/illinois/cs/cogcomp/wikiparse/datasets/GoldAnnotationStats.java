package edu.illinois.cs.cogcomp.wikiparse.datasets;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikiparse.datasets.aida.AidaGoldAnnotationStats;
import edu.illinois.cs.cogcomp.wikiparse.datasets.eval.UIUCEvaluator;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Created by nitishgupta on 11/8/16.
 */
public class GoldAnnotationStats {


	public static Map<String, List<Boolean>> goldSet_Appearance = new HashMap<>();
	public static int notInWiki = 0;
	public static int notInKB = 0;

	public static Set<String> goldNotInKB_butDisam = new HashSet<>();
	public static Set<String> goldNotInKB_notDisam = new HashSet<>();

	public static Set<String> goldEntitiesInKB = new HashSet<>();
	public static Set<String> goldNotInWiki = new HashSet<>();
	public static Set<String> goldEntities = new HashSet<>();


	public static void fillGoldSetAppearanceMap(Map<Pair<Integer, Integer>, String> goldSet, String doc_id) {
		for (Pair<Integer, Integer> key : goldSet.keySet()) {
			String true_wikit = goldSet.get(key);
			true_wikit = true_wikit.replaceAll(" ", "_");
			goldEntities.add(true_wikit);
			if (!goldSet_Appearance.containsKey(true_wikit)) {
				boolean foundinKB = KB.wikiTitle2Wid.containsKey(true_wikit);
				if (foundinKB) {
					goldSet_Appearance.put(true_wikit, Arrays.asList(true, false, false, false));
					goldEntitiesInKB.add(true_wikit);
				} else if (KB.redirect2WikiTitle.containsKey(true_wikit)) {
					String wT = KB.redirect2WikiTitle.get(true_wikit);
					goldSet_Appearance.put(wT, Arrays.asList(false, true, false, false));
					goldEntitiesInKB.add(wT);
				} else {
					try {
						Page page = WikiDB.wiki.getPage(true_wikit);
						String nT = page.getTitle().getWikiStyleTitle();
						if (KB.wikiTitle2Wid.containsKey(nT)) {
							goldSet_Appearance.put(true_wikit, Arrays.asList(false, false, true, false));
							goldEntitiesInKB.add(true_wikit);
						}
						else if (KB.redirect2WikiTitle.containsKey(nT)) {
							String wT = KB.redirect2WikiTitle.get(true_wikit);
							goldSet_Appearance.put(wT, Arrays.asList(false, false, true, false));
							goldEntitiesInKB.add(wT);
						} else {
							goldSet_Appearance.put(true_wikit, Arrays.asList(false, false, false, true));
							notInKB += 1;
							if (page.isDisambiguation()) {
								goldNotInKB_butDisam.add(true_wikit);
								System.out.println("Not In KB But Disam: ");
								System.out.println("Doc id : " + doc_id + "\tWikiTitle : " + true_wikit);
							}
							else
								goldNotInKB_notDisam.add(true_wikit);
						}
					} catch (Exception e) {
						System.out.println("Not In Wiki : ");
						System.out.println("Doc id : " + doc_id + "\tWikiTitle : " + true_wikit);
						notInWiki += 1;
						goldNotInWiki.add(true_wikit);
						goldSet_Appearance.put(true_wikit, Arrays.asList(false, false, false, true));
					}
				}
			}
		}
	}

	public static void processingGoldEntities() {
		for (String file : new File(labelDir).list()) {
			String doc = file;
			System.out.println(doc);
			// Getting offsets for gold mentions
			Map<Pair<Integer, Integer>, String> goldSet =
							UIUCEvaluator.readGoldFromWikifier(labelDir + doc, true);

			fillGoldSetAppearanceMap(goldSet, doc);
		}

//		for (String key : goldSet_Appearance.keySet()) {
//			System.out.println(key + " : " + goldSet_Appearance.get(key));
//		}

		System.out.println("Not in KB But Disambiguation : " + goldNotInKB_butDisam.size());
		System.out.println("Not in KB NOT Disambiguation : " + goldNotInKB_notDisam.size());
		System.out.println("Not IN Wiki : " + goldNotInWiki.size());
		System.out.println("In KB : " + goldEntitiesInKB.size());
		System.out.println("Total Number of Gold Entities : " + goldEntities.size());
		FileUtils.writeSetToFile(goldEntities, processDatasetPath + "GoldStats/" +"GoldEntities_All.txt");
		FileUtils.writeSetToFile(goldEntitiesInKB, processDatasetPath + "GoldStats/" + "GoldEntities_InKB.txt");
		FileUtils.writeSetToFile(goldNotInWiki, processDatasetPath + "GoldStats/" + "GoldEntities_NotInWiki.txt");
		FileUtils.writeSetToFile(goldNotInKB_butDisam, processDatasetPath + "GoldStats/" + "GoldEntities_NotInKB_ButDisamb.txt");
		FileUtils.writeSetToFile(goldNotInKB_notDisam, processDatasetPath + "GoldStats/" + "GoldEntities_NotInKB_NotDisamb.txt");
	}




	//******************						MAIN 						*******************************//
	public static final String dataset = "aida";
	public static String labelDir;
	public static String textDir;

	public static String processDatasetPath;

	public static void main(String[] args) throws Exception {

		switch (dataset) {
			case "msnbc":
				labelDir = Constants.uiucDataSetRootPath + "MSNBC/Problems/";
				textDir = Constants.uiucDataSetRootPath  + "MSNBC/RawTextsSimpleChars/";
				processDatasetPath = Constants.processDatasetRootPath + "MSNBC/";
				processingGoldEntities();
				break;
			case "ace":
				labelDir = Constants.uiucDataSetRootPath + "ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/";
				textDir = Constants.uiucDataSetRootPath + "ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/";
				processDatasetPath = Constants.processDatasetRootPath + "ACE/";
				processingGoldEntities();
				break;

			case "aida":
				AidaGoldAnnotationStats.readAIDAGoldAnnotations(AidaGoldAnnotationStats.AidaGoldAnnotationsTSV);
				break;

			default:
				System.out.println("No Such dataset : " + dataset);

		}



		System.exit(1);
	}

}
