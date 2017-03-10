package edu.illinois.cs.cogcomp.wikiparse.wikiextractparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by nitishgupta on 3/9/17.
 */
public class TestDataKnownEntitiesOverlap {
	public Set<String> entitiesGE2;
	public Set<String> entitiesGE5;
	public Set<String> entitiesGE7;
	public Set<String> entitiesGE9;
	public Set<String> entitiesGE14;

	public MentionStats menstats;

	public String acetest, aidatrain, aidadev, aidatest;
	public List<String> testfiles;

	public TestDataKnownEntitiesOverlap (String wikiMentionsDir) {
		aidatrain = "/save/ngupta19/datasets/AIDA/inkb_mentions/mentions_train_inkb.txt";
		aidadev= "/save/ngupta19/datasets/AIDA/inkb_mentions/mentions_dev_inkb.txt";
		aidatest = "/save/ngupta19/datasets/AIDA/inkb_mentions/mentions_test_inkb.txt";
		acetest = "/save/ngupta19/datasets/ACE/mentions_inkb.txt";
		String[] testies = new String[] {acetest, aidatrain, aidadev, aidatest };
		testfiles = Arrays.asList(testies);

		menstats = new MentionStats(wikiMentionsDir);
		entitiesGE2  = new HashSet<>();
		entitiesGE5  = new HashSet<>();
		entitiesGE7  = new HashSet<>();
		entitiesGE9  = new HashSet<>();
		entitiesGE14  = new HashSet<>();

		for (Map.Entry<String, Integer> entry : menstats.entityMentionCount.entrySet()) {
			String en = entry.getKey();
			int count = entry.getValue();

			if (count >= 2) {
				entitiesGE2.add(en);
			}
			if (count >= 5) {
				entitiesGE5.add(en);
			}
			if (count >= 7) {
				entitiesGE7.add(en);
			}
			if (count >= 9) {
				entitiesGE9.add(en);
			}
			if (count >= 14) {
				entitiesGE14.add(en);
			}
		}

		System.out.println("Entities with >= 2 mentions: " + entitiesGE2.size());
		System.out.println("Entities with >= 5 mention: " + entitiesGE5.size());
		System.out.println("Entities with >= 7 mentions: " + entitiesGE7.size());
		System.out.println("Entities with >= 9 mentions: " + entitiesGE9.size());
		System.out.println("Entities with >= 14 mentions: " + entitiesGE14.size());
	}

	private void testKwnEnOverlap(String testfile, Set<String> knownEnKB) throws Exception {
		int totalMentions = 0, knownMentions = 0;
		Set<String> knownEntities = new HashSet<>();
		Set<String> allEntities = new HashSet<>();

		BufferedReader bwr = new BufferedReader(new FileReader(new File(testfile)));
		String line = bwr.readLine();
		while (line != null) {
			totalMentions += 1;

			String en = line.trim().split("\t")[2];
			allEntities.add(en);
			if (knownEnKB.contains(en)) {
				knownEntities.add(en);
				knownMentions += 1;
			}

			line = bwr.readLine();
		}
		bwr.close();

		System.out.println("Total Mentions : " + totalMentions + " KownMentions : " + knownMentions);
		System.out.println("Total Test Entities : " + allEntities.size() + " Entities in KnownKB : " + knownEntities.size());
	}

	public void computeTestDataKnownEnOverlap () throws Exception{
		for (String testfile : this.testfiles) {
			System.out.println(testfile);

			System.out.println("Known Set Mention >= 2");
			testKwnEnOverlap(testfile, entitiesGE2);
			System.out.println("Known Set Mention >= 5");
			testKwnEnOverlap(testfile, entitiesGE5);
			System.out.println("Known Set Mention >= 7");
			testKwnEnOverlap(testfile, entitiesGE7);
			System.out.println("Known Set Mention >= 9");
			testKwnEnOverlap(testfile, entitiesGE9);
			System.out.println("Known Set Mention >= 14");
			testKwnEnOverlap(testfile, entitiesGE14);
		}
	}

	public static void main (String [] args) throws Exception {
		String wikiMentionsDir = "/save/ngupta19/wikipedia/wiki_mentions/mentions_wcoh_merged/";
		TestDataKnownEntitiesOverlap tt = new TestDataKnownEntitiesOverlap(wikiMentionsDir);
		tt.computeTestDataKnownEnOverlap();
	}



}
