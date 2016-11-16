package edu.illinois.cs.cogcomp.wikiparse.agiga;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikiparse.crosswikis.CrossWikis;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.jhu.agiga.*;

import javax.print.Doc;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitishgupta on 11/3/16.
 */
public class AgigaDocuments {

	public static List<String> xml_files = new ArrayList<>();
	public static final Set<String> nertypes = new HashSet<>(
					Arrays.asList("ORGANIZATION", "PERSON", "LOCATION"));
	public static final String eosword = "<eos_word>";
	public static final String unkmid = "<unk_mid>";
	public static final String unkwid = "<unk_wid>";

	static {
		load_xml_filenames();
	}

	public static void load_xml_filenames() {
		File f = new File(Constants.agiga_xml_dir);
		if (!f.exists() == true || !f.isDirectory() == true) {
			System.out.println("AGIGA Directory does not exist : " + Constants.agiga_xml_dir);
			return;
		}

		for (String filename : new File(Constants.agiga_xml_dir).list()) {
			if (new File(Constants.agiga_xml_dir + filename).isFile())
				xml_files.add(filename);
		}
		System.out.println("[#] Number of xml files in AGIGA : " + xml_files.size());
	}




	static class WriteDoc implements Runnable {
		AgigaDocument doc;

		public WriteDoc(AgigaDocument doc) {
			this.doc = doc;
		}

		public class SentenceStat {
			public String sent_text;
			public List<String> surfaces;
			public String mentions_tf;
			public String mentions_sp;
			public int num_sentence_tokens;
			public int num_mention_tokens;
			public int num_mentions;
			public int foundInCrosswikis;

			public SentenceStat(String s, List<String> m, String mentions_tf_style, String mentions_space,
													int num_t, int num_t_m, int num_m, int fICw) {
				sent_text = s;
				surfaces = m;
				// mid wid mention sentence doc_id
				mentions_tf = mentions_tf_style;
				// Mentions separated by space
				// TODO : Change space to tab and make set
				mentions_sp = mentions_space;
				num_sentence_tokens = num_t;
				num_mention_tokens = num_t_m;
				num_mentions = num_m;
				foundInCrosswikis = fICw;
			}
		}

		public SentenceStat getSentTokensANDNer(AgigaSentence sent, String docid) {
			StringBuffer mentions_tf_style = new StringBuffer();
			StringBuffer sent_text = new StringBuffer();
			List<String> mentions = new ArrayList<>(); // Each mention can be multiwords. Cleaning done
			Set<String> uniq_mentions = new HashSet<String>();
			StringBuffer mentions_space = new StringBuffer();
			int num_mention_tokens = 0;
			int num_sent_tokens = 0;
			int num_m = 0;

			String currner = "";
			String currnertype = "";

			for (AgigaToken token : sent.getTokens()) {
				String word = token.getWord().replaceAll("\\s", "").trim();
				if (!word.equals("")) {
					sent_text.append(word);
					sent_text.append(" ");
					num_sent_tokens++;
					String nertag = token.getNerTag().trim();
					if (nertypes.contains(nertag)) {
						num_mention_tokens++;
						if (nertag.equals(currnertype)) {
							currner += word + " ";
						} else {
							if (!currner.equals("")) {
								mentions.add(currner.trim());
								uniq_mentions.add(currner.trim());
								num_m++;
							}
							currner = word + " ";
							currnertype = nertag;
						}
					} else {
						if (!currner.equals("")) {
							mentions.add(currner.trim());
							uniq_mentions.add(currner.trim());
							num_m++;
							currner = "";
							currnertype = "";
						}
					}
				}
			}
			sent_text.append(eosword);
			String sentence_t = sent_text.toString().replaceAll("\\s", " ").trim();

			int foundInCrosswikis = 0;

			// Only writing mentions that have CrossWikis candidates
			for (String surface : mentions) {
				if (CrossWikis.getNormalizedCandidates(surface) != null) {
					foundInCrosswikis++;
					mentions_tf_style.append(unkmid);
					mentions_tf_style.append("\t");
					mentions_tf_style.append(unkwid);
					mentions_tf_style.append("\t");
					mentions_tf_style.append(surface);
					mentions_tf_style.append("\t");
					mentions_tf_style.append(sentence_t);
					mentions_tf_style.append("\t");
					mentions_tf_style.append(docid);
					mentions_tf_style.append("\n");
				}

				mentions_space.append(surface);
				mentions_space.append(" ");
			}
			String mentions_tf = mentions_tf_style.toString().trim();
			String mentions_sp = mentions_space.toString().trim();

			SentenceStat s = new SentenceStat(sentence_t, mentions, mentions_tf, mentions_sp,
																				num_sent_tokens, num_mention_tokens, num_m,
																				foundInCrosswikis);

			return s;
		}

		public class DocumentStats {
			public String doctext;
			public String linkstext;
			public String mentions_tf;
			public int num_mentions;
			public Set<String> uniq_surfaces;

			public int num_tokens;
			public int num_mention_tokens;
			public int foundInCrossWikis;
			public double frac_ner_tagged_tokens;
			public double frac_uniq_in_crosswikis;
			public double num_uniq;

			public DocumentStats(String doct, String linkt, String mentions, Set<String> uniq_s,
													 int n_m, int n_t, int n_m_t, int fICw) {
				// Document text
				doctext = doct;
				// Coherence text. Unique mentions separated by \t
				linkstext = linkt;
				// mid wid mention sentence doc_id -- for all mentions in document
				mentions_tf = mentions;
				// Set of unique surfaces
				uniq_surfaces = uniq_s;
				num_mentions = n_m;
				num_tokens = n_t;
				num_mention_tokens = n_m_t;
				foundInCrossWikis = fICw;
				frac_ner_tagged_tokens = ((double)(num_mention_tokens))/((double)(num_tokens));
				num_uniq = uniq_s.size();
				frac_uniq_in_crosswikis = ((double)foundInCrossWikis)/((double)num_mentions);
//				double foundInCrosswikis = 0.0;
//				for (String surface : uniq_s) {
//					if (CrossWikis.getCandidates(surface) != null)
//						foundInCrosswikis += 1.0;
//				}
//				frac_uniq_in_crosswikis = foundInCrosswikis/num_uniq;
			}
		}


		public DocumentStats getDocText_NER(AgigaDocument doc) {
			StringBuffer doc_text = new StringBuffer();
			StringBuilder links_text = new StringBuilder();
			StringBuffer mentions_space = new StringBuffer();
			StringBuffer mentions_all_tf = new StringBuffer();
			Set<String> uniq_surfaces = new HashSet<>();
			int num_tokens = 0;
			int num_mentions = 0;
			int num_mention_tokens = 0;
			int foundInCW = 0;

			for (AgigaSentence sent : doc.getSents()) {
				SentenceStat s = getSentTokensANDNer(sent, doc.getDocId());
				uniq_surfaces.addAll(s.surfaces);
				num_tokens += s.num_sentence_tokens;
				num_mention_tokens += s.num_mention_tokens;
				num_mentions += s.num_mentions;
				foundInCW += s.foundInCrosswikis;
				if (!s.sent_text.equals("")) {
					doc_text.append(s.sent_text);
					doc_text.append(" ");
				}

				if (!s.mentions_sp.equals("")) {
					mentions_space.append(s.mentions_sp);
					mentions_space.append(" ");
				}

				if (!s.mentions_tf.equals("")) {
					mentions_all_tf.append(s.mentions_tf);
					mentions_all_tf.append("\n");
				}
			}

			for (String uniq_mention : uniq_surfaces) {
				links_text.append(uniq_mention);
				links_text.append("\t");
			}

			String doctext = doc_text.toString().trim();
			String linkstext = links_text.toString().trim();
			String mentions_tf = mentions_all_tf.toString().trim();

			DocumentStats d = new DocumentStats(doctext, linkstext, mentions_tf, uniq_surfaces,
																			    num_mentions, num_tokens, num_mention_tokens,
																					foundInCW);
			return d;
		}

		public void run() {
			DocumentStats d = getDocText_NER(this.doc);
			String docid = this.doc.getDocId();
			boolean remove = false;
			if (d.frac_ner_tagged_tokens >= 0.1 || d.num_mentions < 10 || d.frac_uniq_in_crosswikis < 0.8)
				remove = true;
			//System.out.print(docid + "\t" + d.num_mentions + "\t" + d.num_uniq + "\t");
			//System.out.print(new DecimalFormat("#.##").format(d.frac_ner_tagged_tokens) + "\t");
			//System.out.print(new DecimalFormat("#.##").format(d.frac_uniq_in_crosswikis) + "\t");
			//if (remove)
			//	System.out.println("------------------- ");
			if (!remove) {
				FileUtils.writeStringToFile(Constants.agiga_docsDir + docid, d.doctext);
				FileUtils.writeStringToFile(Constants.agiga_linksDir + docid, d.linkstext);
				FileUtils.writeStringToFile(Constants.agiga_mentionsDir + docid, d.mentions_tf);
			}
		}
	}

	public static void writeAgigaDocs() {
		long startTime = System.currentTimeMillis();
		long rep_startTime = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(40);
		int total_docs = 0;
		for (int i = 0; i < xml_files.size(); i++) {
			String filepath = Constants.agiga_xml_dir + xml_files.get(i);
			StreamingDocumentReader reader = new StreamingDocumentReader(filepath, new AgigaPrefs());
			for (AgigaDocument doc : reader) {
				if (doc.getType().equals("story")) {
					WriteDoc writedoc = new WriteDoc(doc);
					executor.submit(writedoc);
				}
				total_docs++;

				if (total_docs % 5000 == 0) {
					System.out.print(total_docs + " (" + i + ") .. ");
					long estimatedTime = System.currentTimeMillis() - rep_startTime;
					rep_startTime = System.currentTimeMillis();
					double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
					System.out.print(total_docs + " (" + i + ") .. " + " (" + tt + " mins) .. ");
				}
			}
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
		System.out.println("Total Time : " + tt + "  minutes");

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

//	public static void writeAgigaDocs() {
//		int total_docs = 0;
//		int story_docs = 0;
//		for (int i = 0; i < xml_files.size(); i++) {
//			String filepath = Constants.agiga_xml_dir + xml_files.get(i);
//			StreamingDocumentReader reader = new StreamingDocumentReader(filepath, new AgigaPrefs());
//			AgigaDocument doc = null;
//			while (reader.hasNext()) {
//				doc = reader.next();
//				if (doc.getType().equals("story"))
//					story_docs++;
//				total_docs++;
//				if (total_docs % 100 == 0) {
//					System.out.print(total_docs + ", " + story_docs + " (" + i + ") .. ");
//					System.out.println(doc.getType());
//				}
//			}
//		}
//		System.out.println("DONE");
//		System.out.println("Total Docs Written : " + total_docs);
//	}

	public static void main(String [] args) {
		System.out.println(CrossWikis.getNormalizedCandidates("barack"));
		writeAgigaDocs();
	}

}
