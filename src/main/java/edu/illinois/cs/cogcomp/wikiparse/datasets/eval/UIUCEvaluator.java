package edu.illinois.cs.cogcomp.wikiparse.datasets.eval;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nitishgupta on 10/20/16.
 */
public class UIUCEvaluator {

	/*
	 * Map: Mention char offsets and True Wiki Title (with spaces)
	 *  key: Pair<Int, Int> : Pair<StartCharOffset, EndCharOffset>
	 *  value: String : True Wiki Title (with spaces)
	 */
	public static Map<Pair<Integer, Integer>, String> readGoldFromWikifier(String filename,
																																				 boolean useNonNamedEntities) {
		List<String> lines = null;
		lines = FileUtils.readLines(filename,
						(filename.contains("Wiki") || filename.contains("AQUAINT")) ? "utf-8" : "Windows-1252");
		int offset = -1, length = -1;
		String label = null;
		String surfaceForm = null;
		int state = 0;
		Map<Pair<Integer, Integer>, String> gold = new HashMap<Pair<Integer, Integer>, String>();
		for (String line : lines) {
			if (state > 0) {
				switch (state) {
					case 1:
						offset = Integer.parseInt(line.trim());
						break;
					case 2:
						length = Integer.parseInt(line.trim());
						break;
					case 3:
						label = line.trim();
						// if (label.startsWith("http")) {
						// label = label.replace("http://en.wikipedia.org/wiki/",
						// "");
						// }
						// if (label.endsWith("\"") &&
						// StringUtils.countMatches(label, "\"")==1)
						// label = label.substring(0, label.length()-1);
						// if (redirects.containsKey(label)) {
						// label = redirects.get(label);
						// }
						break;
					case 4:
						surfaceForm = line.trim();
						break;
					default:
						break;
				}
				state = 0;
			}
			if (line.trim().equals("<Offset>")) {
				state = 1;
			} else if (line.trim().equals("<Length>")) {
				state = 2;
			} else if (line.trim().equals("<ChosenAnnotation>")) {
				state = 3;
			} else if (line.trim().equals("<SurfaceForm>")) {
				state = 4;
			} else if (line.trim().equals("</ReferenceInstance>")) {
				state = 0;
				if (!label.equals("none") && !label.equals("---") && !label.equals("*null*")
								&& (useNonNamedEntities || !containsNoUpperCase(surfaceForm))) {
					String goldTitle =
									label.replace("http://en.wikipedia.org/wiki/", "").replace("_", " ");
					if (goldTitle.endsWith("\"")) {
						goldTitle = goldTitle.substring(0, goldTitle.length() - 1).trim();
					}
					gold.put(new Pair<Integer, Integer>(offset, offset + length), goldTitle);
				}
			}
		}
		return gold;
	}

	public static boolean containsNoUpperCase(String str) {
		for (char c : str.toCharArray()) {
			if (Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}
}
