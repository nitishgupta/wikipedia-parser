package edu.illinois.cs.cogcomp.wikiparse.crosswikis;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitishgupta on 11/5/16.
 */
public class CrossWikis {

	// Map from AnchorText --> Map<WID -> cprob>
	// In crosswikis if Page was redirect, we add prob. to the actualy page cprob.
	// Eg. 'la' in cross wikis was (0.3, LA), (0.4, LA,_C) and true page is LA then
	// we store 'la' = (0.7, LA)
	public static Map<String, Map<String, Double>> crosswikis_dict = new HashMap<>();

	public static Map<String, Map<String, Double>> crosswikis_normalized_dict = new HashMap<>();

	public static final double cprob_threshold = 0.001;

	static {
		normalizedProcessedCrosswikis();
	}

	public static void normalizedProcessedCrosswikis() {
		if (new File(Constants.crosswikis_normalized_map_ser).exists()){
			System.out.println("[#] Loading Normalized Crosswikis Map ... ");
			long startTime = System.currentTimeMillis();
			crosswikis_normalized_dict = Utilities.deserializeObject(Constants.crosswikis_normalized_map_ser);
			long estimatedTime = System.currentTimeMillis() - startTime;
			double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
			System.out.println("[#] Load complete. " + "Total Time : " + tt + "  minutes");
		} else {
			loadCrossWikisDict();
			System.out.println(" [#] Normalized CrossWikis map not found. Normalizing and Serializing ... ");
			for (Map.Entry<String, Map<String, Double>> cwiki_entry : crosswikis_dict.entrySet()) {
				String mention = cwiki_entry.getKey();
				Map<String, Double> wiki_cprob = cwiki_entry.getValue();
				Double total_prob = 0.0;
				for (Map.Entry<String, Double> w_prob_entry : wiki_cprob.entrySet()) {
					total_prob += w_prob_entry.getValue();
				}
				Map<String, Double> normalizedDist = new HashMap<String, Double>();
				for (Map.Entry<String, Double> w_prob_entry : wiki_cprob.entrySet()) {
					String wikititle = w_prob_entry.getKey();
					Double newprob = w_prob_entry.getValue() / total_prob;
					normalizedDist.put(wikititle, newprob);
				}
				crosswikis_normalized_dict.put(mention, normalizedDist);
			}

			System.out.println("Serializing ... ");
			Utilities.serializeObject(crosswikis_normalized_dict, Constants.crosswikis_normalized_map_ser);
			System.out.println("Serialization Done ... ");
		}
	}

	public static void loadCrossWikisDict() {
		//System.out.println("Number of pages in Wiki : " + KB.wikiTitle2Wid.size());
		if (new File(Constants.crosswikis_map_ser).exists()){
			System.out.println("[#] Loading Crosswikis Map ... ");
			long startTime = System.currentTimeMillis();
			crosswikis_dict = Utilities.deserializeObject(Constants.crosswikis_map_ser);
			long estimatedTime = System.currentTimeMillis() - startTime;
			double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
			System.out.println("[#] Load complete. " + "Total Time : " + tt + "  minutes");
		}
		else {
			System.out.println(" [#] CrossWikis map not found. Making and Serializing ... ");
			try {
				FileInputStream fin = new FileInputStream(Constants.crosswikis_dict_bz2);
				BufferedInputStream bis = new BufferedInputStream(fin);
				CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
				BufferedReader ir = new BufferedReader(new InputStreamReader(input));

				//			InputStream input = new BufferedInputStream(new FileInputStream(Constants.crosswikis_dict_bz2));
				//			input = new BZip2CompressorInputStream(input, true);
				//			InputStreamReader inputreade = new InputStreamReader(input, "UTF-16");
				//			BufferedReader ir = new BufferedReader(inputreade);

				long startTime = System.currentTimeMillis();
				long rep_startTime = System.currentTimeMillis();
				int lines_read = 0;
				int unique_mentions_written = 0;
				int tuples_written = 0;
				String line = ir.readLine();
				while (line != null) {
					String[] line_split = line.split("\t");
					String mention = line_split[0].trim();
					if (line_split.length >= 2 && !mention.equals("")) {
						String[] prop_split = line_split[1].split(" ");
						Double cprob = Double.parseDouble(prop_split[0]);
						String wikiTitle = prop_split[1].trim();
						// If wikiTitle not in KB but redirect->WikiTitle contains title, change title to new title
						if (!KB.wikiTitle2Wid.containsKey(wikiTitle) && KB.redirect2WikiTitle.containsKey(wikiTitle)) {
							wikiTitle = KB.redirect2WikiTitle.get(wikiTitle);
						}
						// Need to check again if title in wikiTitle->Wid
						// If not in KB and no redirect then condition fails
						// If redirect was found, checks again for safety.
						if (cprob > cprob_threshold && KB.wikiTitle2Wid.containsKey(wikiTitle)) {
							if (!crosswikis_dict.containsKey(mention)) {
								crosswikis_dict.put(mention, new HashMap<String, Double>());
								unique_mentions_written++;
							}
							String wid = KB.wikiTitle2Wid.get(wikiTitle);
							if (crosswikis_dict.get(mention).containsKey(wid)){
								double current_prob = crosswikis_dict.get(mention).get(wid);
								double new_cprob = current_prob + cprob;
								crosswikis_dict.get(mention).put(wid, new_cprob);
							} else {
								crosswikis_dict.get(mention).put(wid, cprob);
							}
							tuples_written++;
						}
					}
					line = ir.readLine();
					lines_read++;
					if (lines_read % 1000000 == 0) {
						long estimatedTime = System.currentTimeMillis() - rep_startTime;
						rep_startTime = System.currentTimeMillis();
						double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
						System.out.print(lines_read / 1000000 + "Mil., " + tuples_written + " (" + tt + " mins) .. ");
					}
				}
				System.out.println("\n\n");
				long estimatedTime = System.currentTimeMillis() - startTime;
				double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
				System.out.println("Total Time : " + tt + "  minutes");
				System.out.println("Unique Mentions Written : " + unique_mentions_written);
				System.out.println("Tuples Written : " + tuples_written);
				System.out.println("Total Lines Read : " + lines_read);

				ir.close();

				System.out.println("Serializing ... ");
				Utilities.serializeObject(crosswikis_dict, Constants.crosswikis_map_ser);
				System.out.println("Serialization Done ... ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get the lower-cased normalized version of the given string. Canonicalizes
	 * UTF-8 characters, removes diacritics, lower-cases the UTF-8 and throws
	 * out all ASCII-range characters that are not alphanumeric.
	 *
	 * @param string
	 * @return lnrm converted string
	 */
	public static String getLnrm(String string) {
		String lnrm = Normalizer.normalize(string, Normalizer.Form.NFD);
		lnrm = lnrm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		lnrm = lnrm.toLowerCase();
		lnrm = lnrm.replaceAll("[^\\p{Alnum}]+", "");
		return lnrm;
	}

	/**
	 * Returns null if mention not found.
	 * @param mention
	 * @return
	 */

	public static Map<String, Double> getCandidates(String mention) {
		mention = getLnrm(mention);
		if (crosswikis_dict.containsKey(mention))
			return crosswikis_dict.get(mention);
		else
			return null;
	}


	public static Map<String, Double> getNormalizedCandidates(String mention) {
		mention = getLnrm(mention);
		if (crosswikis_normalized_dict.containsKey(mention))
			return crosswikis_normalized_dict.get(mention);
		else
			return null;
	}

	public static void main(String [] args) {
		Map<String, Double> candidates = getCandidates("Los Angeles");
		if (candidates != null) {
			for (String wid : candidates.keySet()) {
				System.out.print("(" + wid + ", " + candidates.get(wid) + "),  ");
			}
		} else{
			System.out.println("No Candidates found");
		}
		System.out.println();

		candidates = getNormalizedCandidates("Los Angeles");
		if (candidates != null) {
			for (String wid : candidates.keySet()) {
				System.out.print("(" + wid + ", " + candidates.get(wid) + "),  ");
			}
		} else{
			System.out.println("No Candidates found");
		}
		System.out.println();


		candidates = getCandidates("aklsdjfajkl;dhsfio;asdhf;oa");
		if (candidates == null) {
			System.out.println("YES!!");
		}

	}
}
