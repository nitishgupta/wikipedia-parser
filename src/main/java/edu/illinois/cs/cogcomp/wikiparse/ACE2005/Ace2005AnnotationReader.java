package edu.illinois.cs.cogcomp.wikiparse.ACE2005;

import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nitishgupta on 4/8/17.
 */
public class Ace2005AnnotationReader {
	public static String annotationFile = "/save/ngupta19/datasets/ACE-2005/ACEtoWIKI_resource.txt";

	public static final String wikiHyper = "http://en.wikipedia.org/wiki/";

	Map<String, String> mention2Wid;

	public Ace2005AnnotationReader() {
		System.out.println("Reading ACE 2005 Annotation.");

		mention2Wid = new HashMap<>();
		String[] lines = FileUtils.readFileToString(annotationFile).trim().split("\n");

		for (String line : lines) {
			String [] ssplit = line.trim().split("\t");
			assert(ssplit.length == 4);
			String [] wids = ssplit[0].split(" ");
			String WT = wids[0].replace(wikiHyper, "");

			String KbWT = KB.KBWikiTitle(WT);

			if (KbWT.equals(KB.NullWikiTitle)) {
				continue;
			}

			String docId = ssplit[1].trim();
			String enId = ssplit[2].trim();
			String charOff = ssplit[3].trim();
			String key = docId+"-"+enId+"-"+charOff;

			mention2Wid.put(key, KbWT);

		}
		System.out.println("Annotation Read. Key : " + mention2Wid.keySet().size());

	}

}
