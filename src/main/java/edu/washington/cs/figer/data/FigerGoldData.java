package edu.washington.cs.figer.data;

import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by nitishgupta on 12/23/16.
 */
public class FigerGoldData {
	public static final String figerGoldRawData = Constants.figerGoldRawData;
	public static final String figerGoldProcessedData = Constants.figerGoldProcessedData;

	public class Mention {
		String surface;
		int start;
		int end;
		List<String> labels;

		public Mention() {
			this.labels = new ArrayList<String>();
		}

//		public Mention(String surface, int start, int end, List<String> rawLabels) {
//			// rawLabels are /labelp1/labelp2/labelp3 -- labelpx here is converted type not FBase type
//			// to be converted to labelp1.labelp2.labelp3
//
//			this.surface = surface;
//			this.start = start;
//			this.end = end;
//			this.labels = new ArrayList<String>();
//			for (String l : rawLabels)
//				this.labels.append(l.replaceAll("/", ".").substring(1));
//		}
//	}
//
//	public static void writeProcessedGoldTestMentions() {
//		try {
//			System.out.println("[#] Writing FIGER GOlD Test data ... ");
//			BufferedWriter bwr = new BufferedWriter(new FileWriter(figerGoldProcessedData));
//			String text = FileUtils.getTextFromFile(figerGoldRawData);
//			String[] sentences = text.split("\n");
//
//			for (String sent : sentences) {
//				String prevTag = null, curTag = null;
//				int sentId = 0, tokenId = 0, startToken = -1, endToken = -1;
//
//				ArrayList<String> sent = new ArrayList<String>();
//				List<Mention> mentions = new ArrayList<>();
//
//				StringBuilder sentence_txt = new StringBuilder();
//
//
//
//
//			}
//
//
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	}







}
