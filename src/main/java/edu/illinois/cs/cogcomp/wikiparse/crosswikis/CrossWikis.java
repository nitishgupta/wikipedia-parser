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

	// Map from AnchorText --> List<(WID, cprob)>
	public static Map<String, List<Pair<String, Double>>> crosswikis_dict = new HashMap<>();

	static {
		loadCrossWikisDict();
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
						if (cprob > 0.0 && KB.wikiTitle2Wid.containsKey(wikiTitle)) {
							if (!crosswikis_dict.containsKey(mention)) {
								crosswikis_dict.put(mention, new ArrayList<Pair<String, Double>>());
								unique_mentions_written++;
							}
							crosswikis_dict.get(mention).add(new Pair<String, Double>(KB.wikiTitle2Wid.get(wikiTitle), cprob));
							tuples_written++;
						}
					}
					line = ir.readLine();
					lines_read++;
					if (lines_read % 1000000 == 0) {
						long estimatedTime = System.currentTimeMillis() - rep_startTime;
						rep_startTime = System.currentTimeMillis();
						double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
						System.out.print(lines_read / 1000000 + ", " + tuples_written + " (" + tt + " mins) .. ");
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
				Utilities.serializeObject(crosswikis_dict, "/save/ngupta19/crosswikis/crosswikis.ser");
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
	 * @return
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

	public static List<Pair<String, Double>> getCandidates(String mention) {
		mention = getLnrm(mention);
		if (crosswikis_dict.containsKey(mention))
			return crosswikis_dict.get(mention);
		else
			return null;
	}

	public static void main(String [] args) {
		List<Pair<String, Double>> candidates = getCandidates("Barack Obama");
		if (candidates != null) {
			for (Pair<String, Double> c : candidates) {
				System.out.print("(" + c.getFirst() + ", " + c.getSecond() + "),  ");
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
