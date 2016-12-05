package edu.illinois.cs.cogcomp.wikiparse.util;

import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.TypesafeMap;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

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


		String doc_text = FileUtils.readFileToString(Constants.wiki_kb_docsDir + "4860");
		System.out.println(doc_text);
		System.out.println("****");

		String[] lines = doc_text.split("<eos_word>");


		for (String sentence : lines) {
			System.out.println(sentence.trim() + " <eos_word>");
		}

		int f = (5 > 3) ? 5 : 3;
		System.out.println(f);





	}
}
