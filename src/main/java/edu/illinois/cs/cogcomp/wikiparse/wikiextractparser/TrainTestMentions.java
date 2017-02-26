package edu.illinois.cs.cogcomp.wikiparse.wikiextractparser;


import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.io.*;
import java.util.*;

/**
 * Created by nitishgupta on 2/13/17.
 */
public class TrainTestMentions {
	public static Random rand = new Random();
	public static String allMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/merged_mentions/";
	public static String trainMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/train_val/train/";
	public static String valMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/train_val/val/";
	public MentionStats menstats;
	public List<String> mentionsFilenames;
	public List<BufferedReader> readers;
	public Map<String, Pair<Integer, Integer>> en2traintestmentioncount;
	public Set<String> singletonentitiesForValidation;
	public static final int trainMensPerEntity = 10;
	public static final int valMensPerEntity = 2;

	public int numMensReaders;
	public int numMentionsPerOutputFile = 1000000;


	public TrainTestMentions() {
		System.out.println("Initializing Train Test Mentions writing module");
		// Count of mentions per entities
		menstats = new MentionStats();


		// Making list of mention filenames
		File f = new File(allMentionsDir);
		File[] files = f.listFiles();
		mentionsFilenames = new ArrayList<>();
		for (File ff : files)
			mentionsFilenames.add(ff.toString());
		Collections.shuffle(mentionsFilenames);

		// Opening readers for all mention files to access randomly
		readers = new ArrayList<>();
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

		// en2traintestmentioncount : Tracks train / test mentions written per entity
		// First fill train mentions till mensPerEntityThreshold, then fill test mentions
		en2traintestmentioncount = new HashMap<>();
		List<String> entitiesWithOneMention = new ArrayList<>();
		for (String en : menstats.entityMentionCount.keySet()) {
			en2traintestmentioncount.put(en, new Pair<Integer, Integer>(0,0));
			if (menstats.entityMentionCount.get(en) == 1)
				entitiesWithOneMention.add(en);
		}
		Collections.shuffle(entitiesWithOneMention);
		int numentitiesForValidation = (int)(entitiesWithOneMention.size()*0.4);
		System.out.println(" Entities with one mention : " + entitiesWithOneMention.size());
		System.out.println(" Entities for validation : " + numentitiesForValidation);
		singletonentitiesForValidation = new HashSet<String>();
		for (int i=0; i<numentitiesForValidation; i++)
			singletonentitiesForValidation.add(entitiesWithOneMention.get(i));
	}

	public void writeTrainTestMentions() throws IOException {
		System.out.println("Writing Train / Validation Mentions ... ");
		String trainfname_root = "train.mens.";
		String valfname = "val.mens";
		String singletonvalmentions = "val.single.mens";

		int trainfnum = 0, valfnum = 0;
		int trainmensinfile = 0, valmensinfile = 0;

		int num_of_readers_open = numMensReaders;
		System.out.println("Number of Mention Readers open : " + num_of_readers_open);

		BufferedWriter trwriter = new BufferedWriter(new FileWriter(trainMentionsDir + trainfname_root + trainfnum));
		BufferedWriter valwriter = new BufferedWriter(new FileWriter(valMentionsDir + valfname));
		BufferedWriter singlevalwriter = new BufferedWriter(new FileWriter(valMentionsDir + singletonvalmentions));

		while (num_of_readers_open > 0) {
			int reader_num = rand.nextInt(num_of_readers_open);
			String line = readers.get(reader_num).readLine();

			if (line == null) {
				readers.get(reader_num).close();
				readers.remove(reader_num);
				num_of_readers_open--;
				System.out.println("Number of Mention Readers open : " + num_of_readers_open);
			} else {
				String en = line.trim().split("\t")[2];
				Pair<Integer, Integer> trainvalmencounts = en2traintestmentioncount.get(en);
				if (trainvalmencounts.getFirst() < trainMensPerEntity && !singletonentitiesForValidation.contains(en)) {
					trwriter.write(line);
					trwriter.write("\n");
					trainmensinfile ++;
					en2traintestmentioncount.put(en,
									new Pair<Integer, Integer>(trainvalmencounts.getFirst()+1, trainvalmencounts.getSecond()));
				} else if (trainvalmencounts.getSecond() < valMensPerEntity && !singletonentitiesForValidation.contains(en)) {
					valwriter.write(line);
					valwriter.write("\n");
					valmensinfile ++;
					en2traintestmentioncount.put(en,
									new Pair<Integer, Integer>(trainvalmencounts.getFirst(), trainvalmencounts.getSecond()+1));
				} else if (singletonentitiesForValidation.contains(en)) {
					singlevalwriter.write(line);
					singlevalwriter.write("\n");
				}

				if (trainmensinfile  == numMentionsPerOutputFile) {
					trwriter.close();
					trainmensinfile = 0;
					trainfnum++;
					trwriter = new BufferedWriter(new FileWriter(trainMentionsDir + trainfname_root + trainfnum));
				}
			}
		}
		trwriter.close();
		valwriter.close();
		singlevalwriter.close();
	}


	public static void main (String [] args) throws IOException {
		TrainTestMentions trmentions = new TrainTestMentions();
		trmentions.writeTrainTestMentions();
	}





}
