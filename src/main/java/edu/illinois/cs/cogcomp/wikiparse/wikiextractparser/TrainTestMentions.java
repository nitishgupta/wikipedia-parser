package edu.illinois.cs.cogcomp.wikiparse.wikiextractparser;


import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.io.*;
import java.util.*;

/**
 * Created by nitishgupta on 2/13/17.
 */
public class TrainTestMentions {
	public static Random rand = new Random();
	public static String allMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/mentions_wcoh_merged/";
	public static String trainMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/wcoh/train/";
	public static String valMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/wcoh/val/";
	public static String testMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/wcoh/test/";
	public static String coldMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/wcoh/cold/";
	public static String leftoverMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/wcoh/leftovers/";
	public MentionStats menstats;
	public List<String> mentionsFilenames;
	public List<BufferedReader> readers;

	// min train mens : KnEnMentionThreshold  - testMensPerEntity - valMensPerEntity
	public Set<String> knownEntities;
	public static final int KnEnMentionThreshold = 9; // val=2, test=1, 6<=train<=8
	public static final int maxTrMensPerEntity = 8;
	public static final int testMensPerEntity = 1;
	public static final int valMensPerEntity = 2;
	public Map<String, List<Integer>> en2trValTestMencount; // KnownEn -> <trmens, valmens, testmens>

	// Max mentions per cold start entities
	public Set<String> coldStartEntities;
	public static final int coldstartMensThresh = 2;
	public Map<String, Integer> colden2valmensCount;

	public int numMensReaders;
	public int numMentionsPerOutputFile = 1000000;


	/**
	 * Writes train, val and cold_val mentions by pruning all mentions parsed from wikipedia.
	 *
	 * Input :
	 *  Directory of files containing mentions (after merging from nested dirs)
	 *
	 * Output :
	 *  In train dir : Files contain mentions for training
	 *  In Val dir : 2 mentions file, one validation containing entities from training. One cold val containing unseen en.
	 *
 	 * Process :
	 *
	 *
	 */

	public TrainTestMentions() {
		System.out.println("Initializing Train Test Mentions writing module");
		menstats = new MentionStats(allMentionsDir);    // Count of mentions per entities

		File f = new File(allMentionsDir);    // Making list of mention filenames
		File[] files = f.listFiles();
		mentionsFilenames = new ArrayList<>();
		for (File ff : files)
			mentionsFilenames.add(ff.toString());
		Collections.shuffle(mentionsFilenames);

		readers = new ArrayList<>();    // Opening readers for all mention files to access randomly
		for (String filename : mentionsFilenames) {
			System.out.println(filename);
			try {
				BufferedReader bwr = new BufferedReader(new FileReader(new File(filename)));
				readers.add(bwr);
			} catch (IOException e) {
				System.out.println("Cannot open reader for : " + filename);
				System.exit(0);
			}
		}
		numMensReaders = readers.size();

		// Finding known / cold start entities
		knownEntities = new HashSet<>();
		en2trValTestMencount = new HashMap<>();
		coldStartEntities = new HashSet<>();
		colden2valmensCount = new HashMap<>();
		for (Map.Entry<String, Integer> entry : menstats.entityMentionCount.entrySet()) {
			String en = entry.getKey();
			int count = entry.getValue();
			if (count < KnEnMentionThreshold) {
				coldStartEntities.add(en);
				colden2valmensCount.put(en, 0);
			}
			else {
				knownEntities.add(en);
				List<Integer> zer = Arrays.asList(new Integer[]{0,0,0});
				en2trValTestMencount.put(en, zer);
			}
		}

		System.out.println(" Entities in Known set : " + knownEntities.size());
		System.out.println(" Entities in Cold Start set : " + coldStartEntities.size());
	}

	public void writeTrainTestMentions() throws IOException {
		System.out.println("Writing Mentions ... ");
		System.out.println(" [#] Known Train / Validation / Test Mentions ... ");
		System.out.println(" [#] Known Leftover Mentions ... ");
		System.out.println(" [#] ColdStart Mentions ... ");
		System.out.println(" [#] ColdStart Leftover Mentions ... ");
		String trainfname_root = "train.mens.";
		String valfname = "val.mens";
		String testfname = "test.mens";
		String knownleftovermensfname = "known.leftover.mens";

		String coldstartValMens = "val.coldstart.mens";
		String coldleftovermensfname = "coldstart.leftover.mens";



		int trainfnum = 0;
		int trainmensinfile = 0;

		int num_of_readers_open = numMensReaders;
		System.out.println("Number of Mention Readers open : " + num_of_readers_open);

		BufferedWriter trwriter = new BufferedWriter(new FileWriter(trainMentionsDir + trainfname_root + trainfnum));
		BufferedWriter valwriter = new BufferedWriter(new FileWriter(valMentionsDir + valfname));
		BufferedWriter testwriter = new BufferedWriter(new FileWriter(testMentionsDir + testfname));
		BufferedWriter knleftoverwriter = new BufferedWriter(new FileWriter(leftoverMentionsDir + knownleftovermensfname));

		BufferedWriter coldwriter = new BufferedWriter(new FileWriter(coldMentionsDir + coldstartValMens));
		BufferedWriter coldleftoverwriter = new BufferedWriter(new FileWriter(leftoverMentionsDir + coldleftovermensfname));

		int linesread = 0, totKnMens=0, totColdMens=0;
		while (num_of_readers_open > 0) {
			int reader_num = rand.nextInt(num_of_readers_open);
			String line = readers.get(reader_num).readLine();

			if (line == null) {
				readers.get(reader_num).close();
				readers.remove(reader_num);
				num_of_readers_open--;
				System.out.println("Number of Mention Readers open : " + num_of_readers_open);
			} else {
				linesread++;
				String en = line.trim().split("\t")[2];
				/**
				 * Known Entity : Check test mentions, val mentions then train in order
				 */
				if (knownEntities.contains(en)) {
					totKnMens++;
					List<Integer> counts = en2trValTestMencount.get(en);
					// Test Mention Check
					if (counts.get(2) < testMensPerEntity) {
						testwriter.write(line);
						testwriter.write("\n");
						counts.set(2, counts.get(2) + 1);
						en2trValTestMencount.put(en, counts);
						// Val Mention Check
					} else if (counts.get(1) < valMensPerEntity) {
						valwriter.write(line);
						valwriter.write("\n");
						counts.set(1, counts.get(1) + 1);
						en2trValTestMencount.put(en, counts);
						// Train Mention Check
					} else if (counts.get(0) < maxTrMensPerEntity) {
						trwriter.write(line);
						trwriter.write("\n");
						counts.set(0, counts.get(0) + 1);
						en2trValTestMencount.put(en, counts);
						trainmensinfile ++;
						if (trainmensinfile  == numMentionsPerOutputFile) {
							trwriter.close();
							trainmensinfile = 0;
							trainfnum++;
							trwriter = new BufferedWriter(new FileWriter(trainMentionsDir + trainfname_root + trainfnum));
						}
						// Test, Val and Train mentions written. Write to overflow
					} else {
						knleftoverwriter.write(line);
						knleftoverwriter.write("\n");
					}
				}

				else if (coldStartEntities.contains(en)) {
					totColdMens++;
					int count = colden2valmensCount.get(en);
					if (count < coldstartMensThresh) {
						coldwriter.write(line);
						coldwriter.write("\n");
						count += 1;
						colden2valmensCount.put(en, count);
					} else {
						coldleftoverwriter.write(line);
						coldleftoverwriter.write("\n");
					}
				}

				else {
					System.out.println("WHY THE FUCK AM I HERE??   " + en);
				}
			}
		}
		trwriter.close();
		valwriter.close();
		testwriter.close();
		knleftoverwriter.close();
		coldwriter.close();
		coldleftoverwriter.close();

		System.out.println("Total Mentions : " + linesread + " Total Known Mentions : " + totKnMens +
											 "Total Cold Mentions : " + totColdMens);
	}


	public static void main (String [] args) throws IOException {
		TrainTestMentions trmentions = new TrainTestMentions();
		trmentions.writeTrainTestMentions();
	}





}
