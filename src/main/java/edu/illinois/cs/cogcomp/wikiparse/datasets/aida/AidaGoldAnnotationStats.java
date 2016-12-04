package edu.illinois.cs.cogcomp.wikiparse.datasets;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by nitishgupta on 11/8/16.
 */
public class AidaGoldAnnotationStats {

	public static Map<String, List<Boolean>> goldSet_Appearance = new HashMap<>();
	public static int notInWiki = 0;
	public static int notInKB = 0;

	public static Set<String> goldNotInKB_butDisam = new HashSet<>();
	public static Set<String> goldNotInKB_notDisam = new HashSet<>();

	public static Set<String> goldEntitiesInKB = new HashSet<>();
	public static Set<String> goldNotInWiki = new HashSet<>();
	public static Set<String> goldEntities = new HashSet<>();


	public static void addGoldEntityToSets(String true_wikit, String doc_id) {
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


	public static void readAIDAGoldAnnotations(String filepath) throws Exception {
		BufferedReader bwr = new BufferedReader(new FileReader(filepath));
		int num_docs_read = 0;
		int num_testa_docs = 0, num_textb_docs = 0;
		String l = bwr.readLine();
		String doc_id = "";
		while (l != null) {
			String line = l.trim();
			l = bwr.readLine();
			if (line.equals("")) {
				continue;
			}

			if (line.startsWith("-DOCSTART-")) {
				num_docs_read++;
				if (line.contains("testa"))
					num_testa_docs++;
				else if (line.contains("testb"))
					num_textb_docs++;

				doc_id = line.split("\\(")[1].split("\\)")[0].trim();
				System.out.println(doc_id);
			}


			if (doc_id.contains("testa") || doc_id.contains("testb")) {
				String[] splits = line.split("\t");
				if (splits.length == 5) {
					String wikiURL = splits[2];
					String goldTitle =
									wikiURL.replace("http://en.wikipedia.org/wiki/", "");
					if (goldTitle.endsWith("\"")) {
						goldTitle = goldTitle.substring(0, goldTitle.length() - 1).trim();
					}
					//System.out.println(goldTitle);
					addGoldEntityToSets(goldTitle, doc_id);
				}
			}
		}
		bwr.close();

		System.out.println("Num of Docs : " + num_docs_read);
		System.out.println("Num of TEST A Docs : " + num_testa_docs);
		System.out.println("Num of TEST B Docs : " + num_textb_docs);
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

	public static final String processDatasetPath = "/save/ngupta19/datasets/AIDA/";
	public static final String AidaGoldAnnotationsTSV = "/save/ngupta19/AIDA/aida-yago2-dataset/AIDA-YAGO2-annotations.tsv";

	public static void main (String [] args) throws Exception {
		readAIDAGoldAnnotations(AidaGoldAnnotationsTSV);
	}


}
