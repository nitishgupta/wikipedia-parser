package edu.illinois.cs.cogcomp.wikiparse.figertest;

import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.illinois.cs.cogcomp.wikiparse.wikiextractparser.Mention;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nitishgupta on 4/9/17.
 */
public class FigerTestData {
	public static final String figerTestFile = "/save/ngupta19/datasets/FIGER/Wiki/test.txt";
	public static final String ontonotesTestFile = "/save/ngupta19/datasets/FIGER/OntoNotes/test.txt";

	public static final String figerTestOutputFile = "/save/ngupta19/datasets/FIGER/figer.txt";
	public static final String ontonotesTestOutputFile = "/save/ngupta19/datasets/FIGER/ontonotes.txt";

	public static void writeTestData(String inputFile, String outputFile) {

		System.out.println("Reading : " + inputFile);
		System.out.println("Writing : " + outputFile);

		List<String> inputLines = FileUtils.readLines(inputFile);
		List<String> inputMentions = new ArrayList<>();

		StringBuffer outputMentions = new StringBuffer();

		for (String mention : inputLines) {
			String trimmed = mention.trim();
			if (trimmed.equals(""))
				continue;

			StringBuffer mentionString = new StringBuffer();

			String trimmedMention = trimmed.replaceAll("-LRB-", "(").replaceAll("-RRB-", ")");


			String[] split = trimmedMention.split("\t");
			int start = Integer.parseInt(split[0]);
			int end = Integer.parseInt(split[1]) - 1;
			String sentence = split[2].trim();
			String[] sentence_tokens = sentence.split(" ");
			String surface = "";
			for (int i = start; i < end + 1; i++) {
				surface += sentence_tokens[i];
				surface += " ";
			}
			surface = surface.trim();

			String [] types = split[3].split(" ");
			StringBuffer outTypes = new StringBuffer();
			for (String t : types) {
				t = t.replaceAll("/", ".");
				t = t.substring(1);
				outTypes.append(t).append(" ");
			}
			String outTypesString = outTypes.toString().trim();

			Mention m = new Mention("<unk_mid>", "<unk_wid>", "NULLWIKITITLE", surface, sentence, outTypesString,
							            		surface.replaceAll(" ", "_"), start, end);

			outputMentions.append(m.toString());

		}

		FileUtils.writeStringToFile(outputFile, outputMentions.toString());

		System.out.println("Done");
	}

	public static void main (String [] args) {
		FigerTestData.writeTestData(figerTestFile, figerTestOutputFile);
		FigerTestData.writeTestData(ontonotesTestFile, ontonotesTestOutputFile);
	}






}
