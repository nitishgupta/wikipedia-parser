package edu.illinois.cs.cogcomp.wikiparse.util;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * Created by nitishgupta on 11/7/16.
 */
public class Test {

	public static void main(String [] args) {
//		String s = "Los Angeles ( \"The Angels\"),[14] officially the City of" +
//		"Los Angeles and often known by its initials.\ndfa asdf asdfa sdfas well.";
//
//		Properties props = new Properties();
//		props.setProperty("annotators", "tokenize, ssplit");
//		props.setProperty("tokenize.options", "ptb3Escaping=false");
//		props.setProperty("ssplit.isOneSentence", "false");
//
//
//		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//		Annotation document = new Annotation(s);
//		System.out.println(pipeline.getProperties());
//		pipeline.annotate(document);
//
//
//		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//
//		for(CoreMap sentence: sentences) {
//			// traversing the words in the current sentence
//			// a CoreLabel is a CoreMap with additional token-specific methods
//			for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//				// this is the text of the token
//				String word = token.get(CoreAnnotations.TextAnnotation.class);
//				System.out.print(word + " ");
//
//			}
//			System.out.println();
//
//		}

		Tokenizer tkr = new IllinoisTokenizer();
		String text = " "; // or "\n" or ""
		Tokenizer.Tokenization tknzn = tkr.tokenizeTextSpan(text);
		int[] sentEndOffsets = tknzn.getSentenceEndTokenIndexes();

		TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
		TextAnnotation ta = tab.createTextAnnotation(text);






	}
}
