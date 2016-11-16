package edu.illinois.cs.cogcomp.wikiparse.agiga;

import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.jhu.agiga.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitishgupta on 11/3/16.
 */
public class AgigaMentions {


	static class WriteMentions implements Runnable {
		AgigaDocument doc;
		String docid;
		public static final String unkmid = "<unk_mid>";
		public static final String unkwid = "<unk_wid>";

		public WriteMentions(AgigaDocument doc) {
			this.doc = doc;
			this.docid = doc.getDocId();
		}

		public String getMentions(){
			StringBuffer all_mentions = new StringBuffer();
			for (AgigaSentence sent : doc.getSents()) {
				List<String> ner = new ArrayList<>();
				StringBuffer sentence_txt = new StringBuffer();
				String currner = "";
				String currnertype = "";

				for (AgigaToken token : sent.getTokens()) {
					String word = token.getWord().trim();
					sentence_txt.append(word + " ");
					String nertag = token.getNerTag();
					if (AGIGA.nertypes.contains(nertag)) {
						if (nertag.equals(currnertype)) {
							currner += word + " ";
						} else {
							if (!currner.equals(""))
								ner.add(currner.replaceAll("\\s", " ").trim());
							currner = word + " ";
							currnertype = nertag;
						}
					} else {
						if (!currner.equals("")) {
							ner.add(currner.replaceAll("\\s", " ").trim());
							currner = "";
							currnertype = "";
						}
					}
				}
				if (ner.size() > 0) {
					String sent_text = sentence_txt.toString().replaceAll("\\s", " ").trim();
					for (String surface : ner) {
						StringBuffer men = new StringBuffer();
						men.append(unkmid + "\t");
						men.append(unkwid + "\t");
						men.append(surface + "\t");
						men.append(sent_text + "\t");
						men.append(docid);
						all_mentions.append(men.toString().trim());
						all_mentions.append("\n");
					}
				}
			}
			String allmentions = all_mentions.toString().trim();
			if (allmentions != null)
				return allmentions;
			else
				return null;
		}

		public void run() {
			String allmentions = getMentions();
			String docid = this.doc.getDocId();

			if (allmentions != null)
				FileUtils.writeStringToFile(Constants.agiga_mentionsDir + docid, allmentions);
		}
	}





	public static void writeAgigaMentions() {
		System.out.println("[#] Writing AGIGA mentions .. ");
		ExecutorService executor = Executors.newFixedThreadPool(45);
		int total_docs = 0;
		for (int i = 0; i < AGIGA.xml_files.size(); i++) {
			String filepath = Constants.agiga_xml_dir + AGIGA.xml_files.get(i);
			StreamingDocumentReader reader = new StreamingDocumentReader(filepath, new AgigaPrefs());
			for (AgigaDocument doc : reader) {
				WriteMentions writementions = new WriteMentions(doc);
				executor.submit(writementions);
				total_docs++;
				if (total_docs % 10000 == 0)
					System.out.print(total_docs + " (" + i + ") .. ");
			}
		}

		System.out.println("\n\nAwating Termination");
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdownNow();
		System.out.println("DONE");
		System.out.println("Total Docs Written : " + total_docs);
	}

	public static void main(String [] args) {
		writeAgigaMentions();
	}


}
