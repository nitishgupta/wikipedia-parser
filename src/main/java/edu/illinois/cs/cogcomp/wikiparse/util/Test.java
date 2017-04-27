package edu.illinois.cs.cogcomp.wikiparse.util;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * Created by nitishgupta on 11/7/16.
 */
public class Test {

	public static void main(String [] args) {
		String s = "Bandar Seri Begawan 11-15 ( AFP ). A high - level American official announced today Wednesday in the wake of the meeting between American President Bill Clinton and his Russian counterpart Vladimir Putin that Putin 's proposals on reducing the Russian and American nuclear capability to under 1500 nuclear heads does not include \" many new elements . \"";

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.setProperty("tokenize.options", "ptb3Escaping=false");
		props.setProperty("tokenize.whitespace", "true");
		props.setProperty("ssplit.isOneSentence", "true");


		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(s);
		System.out.println(pipeline.getProperties());
		pipeline.annotate(document);


		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			System.out.println("NEW SENTECNE");
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods

			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				String nertag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				System.out.print(word + "_" + nertag + " ");
				//System.out.print(word + " ");

			}
			System.out.println();

		}
	}
}
