package edu.illinois.cs.cogcomp.wikiparse.wikiextractparser;

import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Created by nitishgupta on 2/11/17.
 */
public class MentionStats {
	//public static final String wikiMentionsDir = WikiExtractParser.outputWikiMentionsDir;
	public static final String wikiMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/complete_mentions";
	//public static final String wikiMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/val_mentions";
	public Map<String, Integer> entityCount;

	public MentionStats () {
		entityCount = new HashMap<String, Integer>();
		for (String en : KB.wikiTitle2Wid.keySet()) {
			entityCount.put(en, 0);
		}
		entityPresenceCountMap();
	}

	public static Iterator<File> getFileIterator() {
		Iterator<File> i = org.apache.commons.io.FileUtils.iterateFiles(new File(wikiMentionsDir), null, true);
		return i;
	}

	public void entityPresenceCountMap() {
		System.out.println("[#] Counting number of mentions per entity");
		Set<String> types = new HashSet<String>();
		Iterator<File> i = getFileIterator();
		int filesread = 0, mensread = 0;
		while (i.hasNext()) {
			File file = i.next();
			List<String> mentions = FileUtils.readLines(file.toString());
			// Mention : mid \t wid \t wikititle \t start_token \t end_token \t surface \t tokenized_sentence \t all_types
			for (String men : mentions) {
				String[] split = men.trim().split("\t");
				assert (split.length == 8): "Mention split does not have 8 items";
				String wikititle = split[2];
				entityCount.put(wikititle, entityCount.get(wikititle)+1);
				String[] typs = split[7].split(" ");
				types.addAll(Arrays.asList(typs));
				mensread++;
			}
			filesread++;
			if (filesread % 100 == 0) {
				System.out.print(filesread + " (" + mensread + "), ");
			}
		}
		System.out.println("\n [#] Done counting entity presence.");
		System.out.println(" [#] Total Type Labels : " + types.size());
	}

	public void entityStats() {
		int numOfZeros = 0, numOfOnes = 0, numOfTwos = 0, numOfThrees = 0, numOfFours = 0, numOfFives = 0;
		int threshold100mentions = 0; // If each entity gets max of 100 mentions, then count of mentions
		Map<String, Integer> sortedEntityCounts = Utilities.sortByDecreasingValue(entityCount);
		Iterator<String> it = sortedEntityCounts.keySet().iterator();

		for (int i=0; i<50; i++) {
			String en = it.next();
			int count = sortedEntityCounts.get(en);
			System.out.println(en + " : " + count);
		}

		// Key : Entity, Value : Number of mentions
		for (Map.Entry<String, Integer> entry : sortedEntityCounts.entrySet()) {
			if (entry.getValue() == 0) {
				numOfZeros++;
			}
			if (entry.getValue() == 1) {
				numOfOnes++;
			}
			if (entry.getValue() == 2) {
				numOfTwos++;
			}
			if (entry.getValue() == 3) {
				numOfThrees++;
			}
			if (entry.getValue() == 4) {
				numOfFours++;
			}
			if (entry.getValue() == 5) {
				numOfFives++;
			}
			threshold100mentions += Math.min(10, entry.getValue());
		}
		System.out.println("Entities with Zero mentions: " + numOfZeros);
		System.out.println("Entities with One mention: " + numOfOnes);
		System.out.println("Entities with Two mentions: " + numOfTwos);
		System.out.println("Entities with Three mentions: " + numOfThrees);
		System.out.println("Entities with Four mentions: " + numOfFours);
		System.out.println("Entities with Five mentions: " + numOfFives);
		System.out.println("Number of Mentions if thresholded at 100 mentions: " + threshold100mentions);
	}

	public static void main (String [] args) {
		MentionStats menstats = new MentionStats();
		menstats.entityStats();
	}

}
