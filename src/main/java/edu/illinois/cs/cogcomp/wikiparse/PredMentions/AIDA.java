package edu.illinois.cs.cogcomp.wikiparse.PredMentions;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by nitishgupta on 4/10/17.
 */
public class AIDA {

	public static final String aidaDevFile = "/save/ngupta19/datasets/AIDA/wcoh/known_mentions/mentions_dev_known.txt";
	public static final String aidaDevOutFile = "/save/ngupta19/datasets/AIDA/wcoh/known_mentions/mentions_dev_ner.txt";

	public static final String aidaTestFile = "/save/ngupta19/datasets/AIDA/wcoh/known_mentions/mentions_test_known.txt";
	public static final String aidaTestOutFile = "/save/ngupta19/datasets/AIDA/wcoh/known_mentions/mentions_test_ner.txt";

	public StanfordCoreNLP pipeline;

	public AIDA () {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.setProperty("tokenize.options", "ptb3Escaping=false");
		props.setProperty("ssplit.isOneSentence", "true");
		props.setProperty("tokenize.whitespace", "true");
		pipeline = new StanfordCoreNLP(props);
	}

	public Boolean annotateNER(String sent, int start, int end) {
		Annotation document = new Annotation(sent);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		assert sentences.size() == 1;

		CoreMap sentence = sentences.get(0);
//		for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//			// this is the text of the token
//			String word = token.get(CoreAnnotations.TextAnnotation.class);
//			String nertag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//			System.out.print(word + "_" + nertag + " ");
//			//System.out.print(word + " ");
//		}
		List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
		String nertag_start = tokens.get(start).get(CoreAnnotations.NamedEntityTagAnnotation.class);
		String nertag_end = tokens.get(end).get(CoreAnnotations.NamedEntityTagAnnotation.class);
		//System.out.println(nertag_start + " " + nertag_end);
		//System.out.println();
		Boolean r = true;
		if (nertag_start.equals("O") && nertag_end.equals("O"))
			r = false;

		return r;
	}


	public void readMentions(String inputFile, String outFile) throws Exception {
		List<String> lines = Arrays.asList(FileUtils.getTextFromFile(inputFile).trim().split("\n"));
		System.out.println("Total Mentions : " + lines.size());

		int correct = 0;

		Map<String, Set<Pair<Integer, Integer>>> sent2SetMentions = new HashMap<>();

		StringBuffer nerLines = new StringBuffer();
		for (String line : lines) {
			String[] split = line.split("\t");
			String mid = split[0];
			String wid = split[1];
			String wT = split[2];
			Integer start = Integer.parseInt(split[3]);
			Integer end = Integer.parseInt(split[4]);
			String surface = split[5];
			String sentence = split[6];
			String types = split[7];
			String coherence = split[8];

			if (!sent2SetMentions.containsKey(sentence))
				sent2SetMentions.put(sentence, new HashSet<Pair<Integer, Integer>>());

			Pair<Integer, Integer> pair = new Pair<>(start, end);
			Set<Pair<Integer, Integer>> existingMens = sent2SetMentions.get(sentence);
			if (existingMens.contains(pair)) {
				System.out.println(sentence);
				System.out.println(pair.getFirst() + " " + pair.getSecond());
			}
			sent2SetMentions.get(sentence).add(new Pair<Integer, Integer>(start, end));

		}

		int countTotal = 0;
		for (String sent : sent2SetMentions.keySet())
			countTotal += sent2SetMentions.get(sent).size();


//		Boolean r = annotateNER(sentence, start, end);
//		if (r == true) {
//			correct += 1;
//			nerLines.append(line).append("\n");
//		}
		System.out.println("Total mens : " + countTotal);
		//FileUtils.writeStringToFile(outFile, nerLines.toString());
	}

	public static void main (String [] args) throws Exception {
		AIDA aida = new AIDA();
		aida.readMentions(aidaDevFile, aidaDevOutFile);
		//aida.readMentions(aidaTestFile, aidaTestOutFile);
	}
}
