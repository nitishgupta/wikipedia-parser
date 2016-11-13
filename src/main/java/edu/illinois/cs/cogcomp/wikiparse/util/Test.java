package edu.illinois.cs.cogcomp.wikiparse.util;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by nitishgupta on 11/7/16.
 */
public class Test {

	public static void main(String [] args) {
		String s = "Los Angeles ( \"The Angels\"),[14] officially the City of" +
		"Los Angeles and often known by its initials.\ndfa asdf asdfa sdfas well.";

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit");
		props.setProperty("tokenize.options", "ptb3Escaping=false");
		props.setProperty("ssplit.isOneSentence", "false");


		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(s);
		System.out.println(pipeline.getProperties());
		pipeline.annotate(document);


		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

		for(CoreMap sentence: sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				System.out.print(word + " ");

			}
			System.out.println();

		}

//
//
		PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(
						new StringReader(s),
						new CoreLabelTokenFactory(),
						"normalizeParentheses=false,normalizeOtherBrackets=false,invertible=true");
//
//		List<CoreLabel> tokens = new ArrayList<CoreLabel>();
//
//		while (tokenizer.hasNext()) {
//			tokens.add(tokenizer.next());
//		}
//
//		for(CoreMap sentence: sentences) {
//			String sentenceText = sentence.get(TextAnnotation.class)
//		}
////// Join back together
//		int end;
//		int start = 0;
//		sentenceList = new ArrayList<String>();
//		for (List<CoreLabel> sentence: sentences) {
//			end = sentence.get(sentence.size()-1).endPosition();
//			sentenceList.add(paragraph.substring(start, end).trim());
//			start = end;
//		}
//		System.out.println(StringUtils.join(sentenceList, " _ "));




	}
}
