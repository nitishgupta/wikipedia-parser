package edu.washington.cs.figer.data;

import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by nitishgupta on 12/10/16.
 */
public class FigerData {
	public static final String figerTypesMap = Constants.figerTypesMap;
	public static final String figerTrainingData = Constants.figerTrainingDataGz;
	public static final String figerGoldRawData = Constants.figerGoldRawData;
	public static final String figerGoldProcessedData = Constants.figerGoldProcessedData;


	public static Map<String, String> fbtype2label = new HashMap<String, String>();
	public static MentionReader mentionreader = MentionReader.getMentionReader(figerTrainingData);

	static {
		System.out.println("[#] Making fbtype2label map ... ");
		makeFBType2LabelMap();
		int numlabels = new HashSet(fbtype2label.values()).size();
		System.out.println(" [#] fbtype2label map made!");
		System.out.println(" [#] Number of fbtypes : " + fbtype2label.size());
		System.out.println(" [#] Number of labels : " + numlabels);
	}

	public static void makeFBType2LabelMap() {
		String[] lines = FileUtils.getTextFromFile(figerTypesMap).trim().split("\n");
		for (String line : lines) {
			String [] fbtype_label = line.split("\t");
			assert (fbtype_label.length == 2);

			String fbtype = fbtype_label[0];
			String label = fbtype_label[1].replaceAll("/", ".").substring(1);
			System.out.println(fbtype + "\t" + label);

			fbtype2label.put(fbtype, label);
		}
	}

	public static void writeProcessedTrainingMentions() {
		try {
			System.out.println("[#] Writing FIGER mentions data ... ");
			BufferedWriter bwr = new BufferedWriter(new FileWriter(Constants.figerCompleteProcessedData));
			EntityProtos.Mention m = null;
			int numMentionsWritten = 0;
			int longestSentence = 0;
			while ((m = mentionreader.readMention()) != null) {
				Set<String> labels = new HashSet<>();
				for (String fbtype : m.getLabelsList()) {
					if (FigerData.fbtype2label.containsKey(fbtype)) {
						labels.add(FigerData.fbtype2label.get(fbtype));
					}
				}

				if (labels.size() > 0) {
					StringBuilder mention_towrstring = new StringBuilder();

					String wikititle = m.getEntityName().replaceAll(" ", "_");
					StringBuilder label_strb = new StringBuilder();
					for (String l : labels) {
						label_strb.append(l);
						label_strb.append(" ");
					}
					String label_str = label_strb.toString().trim();

					String start = Integer.toString(m.getStart());
					String end = Integer.toString(m.getEnd() - 1);

					StringBuilder token_strb = new StringBuilder();
					List<String> tokens = m.getTokensList();
					if (tokens.size() > longestSentence)
						longestSentence = tokens.size();

					for (String token : tokens){
						token_strb.append(token);
						token_strb.append(" ");
					}
					String token_str = token_strb.toString().trim();

					// WikiTitle
					mention_towrstring.append(KB.KBWikiTitle(wikititle));
					mention_towrstring.append("\t");
					// MID
					mention_towrstring.append(KB.wikiTitle2Mid(wikititle));
					mention_towrstring.append("\t");
					// WID
					mention_towrstring.append(KB.wikiTitle2WID(wikititle));
					mention_towrstring.append("\t");
					// mention start and end
					mention_towrstring.append(start);
					mention_towrstring.append("\t");
					mention_towrstring.append(end);
					mention_towrstring.append("\t");
					// Sentence
					mention_towrstring.append(token_str);
					mention_towrstring.append("\t");
					// Multiple Labels string
					mention_towrstring.append(label_str);
					mention_towrstring.append("\n");

					bwr.write(mention_towrstring.toString());

					numMentionsWritten++;
				}
			}

			bwr.close();
			System.out.println(" [#] Number of mentions written : " + numMentionsWritten);
			System.out.println(" [#] Longest Sentence : " + longestSentence);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public static void main (String[] args) {
		FigerData f = new FigerData();
		//FigerData.writeProcessedTrainingMentions();
	}



}
