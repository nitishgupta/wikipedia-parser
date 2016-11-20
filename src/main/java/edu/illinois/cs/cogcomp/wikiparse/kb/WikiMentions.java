package edu.illinois.cs.cogcomp.wikiparse.kb;

import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import org.openrdf.query.algebra.In;

import java.io.*;

/**
 * Created by nitishgupta on 11/13/16.
 */
public class WikiMentions {

	public static final int max_mentions_perfile = 1000000;
	public static final int max_mentions_per_entity = 3;

	public static BufferedReader doc_vectors_reader = null;
	public static BufferedReader links_vectors_reader = null;

	static {
		initializeDocLinksVectorsReaders();
	}

	public static void initializeDocLinksVectorsReaders() {
		try {
			doc_vectors_reader = new BufferedReader(new FileReader(Constants.wiki_doc_vectors_file));
			links_vectors_reader = new BufferedReader(new FileReader(Constants.wiki_links_vectors_file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class WIDSentences {
		String[] sentences;
		String wid;
		String mid;
		String wikiTitle;
		String fb_name;
		int num_sentences;

		public WIDSentences(String wid) {
			this.wid = wid;
			this.wikiTitle = KB.wid2WikiTitle.get(wid);
			this.fb_name = KB.wid2FBName.get(wid);
			String doc_text = FileUtils.readFileToString(Constants.wiki_kb_docsDir + wid);
			String[] sentences = doc_text.split("<eos_word>");
			this.num_sentences = sentences.length;
			this.sentences = new String[num_sentences];
			for (int i=0; i<num_sentences; i++) {
				String sent = sentences[i].trim() + " <eos_word>";
				this.sentences[i] = sent;
			}
			this.mid = KB.wid2mid.get(wid);
		}
	}

	static class DocLinksVectors {
		String wid;
		String[] doc_vectors_split;
		String[] link_vectors_split;

		// One line each from doc_vec and links_vec file.
		// Should be running in sync and hence have vectors for the same entity. Still ASSERT
		public DocLinksVectors(String doc_vecs_line, String links_vecs_line) {
			String[] doc_vecs_split = doc_vecs_line.trim().split("\t");
			String[] links_vecs_split = links_vecs_line.trim().split("\t");
			assert(doc_vecs_split[0].equals(links_vecs_split[0]));
			this.wid = doc_vecs_split[0];
			this.doc_vectors_split = doc_vecs_split;
			this.link_vectors_split = links_vecs_split;
		}
	}

	public static BufferedWriter getNewMentionFileWriter (String outDir, String mention_file_root, int file_num) {
		BufferedWriter bwr = null;
		try {
			String filepath = outDir + mention_file_root + Integer.toString(file_num);
			bwr = new BufferedWriter(new FileWriter(filepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bwr;
	}

	public static void writeMentions(String mentionsOutputDir) {
		try {
			long startTime = System.currentTimeMillis();
			long rep_startTime = System.currentTimeMillis();

			String outDir = mentionsOutputDir;
			String mentions_file_rootname = "mentions.";
			int file_num = 0;
			int total_mentions_written = 0;
			int mentions_written_infile = 0;
			int docs_read = 0;
			String doc_vecs_line = doc_vectors_reader.readLine();
			String links_vecs_line = links_vectors_reader.readLine();
			BufferedWriter bwr = getNewMentionFileWriter(outDir, mentions_file_rootname, file_num);
			while (doc_vecs_line != null && links_vecs_line != null) {
				DocLinksVectors doclinksvectors = new DocLinksVectors(doc_vecs_line, links_vecs_line);
				String wid = doclinksvectors.wid;
				assert (KB.wid2WikiTitle.containsKey(wid));
				WIDSentences widsentences = new WIDSentences(wid);
				int mentions_to_write = (widsentences.num_sentences > max_mentions_per_entity)
								? max_mentions_per_entity : widsentences.num_sentences;

				StringBuilder mentions_for_wid = new StringBuilder();
				for (int sent_num = 0; sent_num < mentions_to_write; sent_num++) {
					for (int vec_num = 1; vec_num <= 5; vec_num++) {
						mentions_for_wid.append(widsentences.mid);
						mentions_for_wid.append("\t");
						mentions_for_wid.append(widsentences.wid);
						mentions_for_wid.append("\t");
						mentions_for_wid.append(widsentences.fb_name);
						mentions_for_wid.append("\t");
						mentions_for_wid.append(widsentences.sentences[sent_num]);
						mentions_for_wid.append("\t");
						mentions_for_wid.append(widsentences.wid);
						mentions_for_wid.append("\t");
						mentions_for_wid.append(doclinksvectors.doc_vectors_split[vec_num]);
						mentions_for_wid.append("\t");
						mentions_for_wid.append(doclinksvectors.link_vectors_split[vec_num]);
						mentions_for_wid.append("\n");
						mentions_written_infile++;
						total_mentions_written++;
					}
				}

				bwr.write(mentions_for_wid.toString());
				if (mentions_written_infile >= max_mentions_perfile) {
					bwr.close();
					mentions_written_infile = 0;
					file_num++;
					bwr = getNewMentionFileWriter(outDir, mentions_file_rootname, file_num);
				}

				doc_vecs_line = doc_vectors_reader.readLine();
				links_vecs_line = links_vectors_reader.readLine();
				docs_read++;

				if (docs_read % 10000 == 0) {
					long estimatedTime = System.currentTimeMillis() - rep_startTime;
					rep_startTime = System.currentTimeMillis();
					double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
					System.out.print(docs_read + " (" + total_mentions_written + ", " + tt + " mins) .. ");
				}
			}

			bwr.close();
			doc_vectors_reader.close();
			links_vectors_reader.close();

			long estimatedTime = System.currentTimeMillis() - startTime;
			double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
			System.out.println("\n" + docs_read + " (" + total_mentions_written + ", " + tt + " mins) .. ");

		} catch (IOException e ) {
			e.printStackTrace();
		}



	}

	public static void main(String [] args) {
		WikiMentions.writeMentions(Constants.wiki_kb_mentionsdir);

	}
}
