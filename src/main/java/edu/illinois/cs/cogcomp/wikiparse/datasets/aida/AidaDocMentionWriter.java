package edu.illinois.cs.cogcomp.wikiparse.datasets.aida;

import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by nitishgupta on 11/30/16.
 */
public class AidaDocMentionWriter {

	public static final String aida_tsv = Constants.aida_yago_tsv;
	public static final String datasetMentionsFileDir = "/save/ngupta19/datasets/AIDA/wcoh/";
	public static final String datasetDocsDir = "/save/ngupta19/datasets/AIDA/docs/";
	public static final String datasetLinksDir = "/save/ngupta19/datasets/AIDA/links/";

	public static final String train_MentionsFile = "mentions_train.txt";
	public static final String dev_MentionsFile = "mentions_dev.txt";
	public static final String test_MentionsFile = "mentions_test.txt";

	public static final String train_MentionsNNFile = "mentions_train_nonnme.txt";
	public static final String dev_MentionsNNFile = "mentions_dev_nonnme.txt";
	public static final String test_MentionsNNFile = "mentions_test_nonnme.txt";

	public static List<Document.Doc> documents;

	static {
		AidaReader();
		//WriteDocs();
		//WriteLinks();
		WriteAllMentions();
		WriteNonNullMentions();
		System.out.println("Number of docs : " + documents.size());
	}

	public static void WriteAllMentions() {
		try {
			BufferedWriter train_wr = new BufferedWriter(new FileWriter(datasetMentionsFileDir + train_MentionsFile));
			BufferedWriter dev_wr = new BufferedWriter(new FileWriter(datasetMentionsFileDir + dev_MentionsFile));
			BufferedWriter test_wr = new BufferedWriter(new FileWriter(datasetMentionsFileDir + test_MentionsFile));

			int tr = 0, dev = 0, test = 0;
			int trm = 0, dem = 0, tem = 0;

			for (Document.Doc doc : documents) {
				int docm = 0;
				StringBuilder mentext = new StringBuilder();
				for (Document.Sentence sent : doc.sentences) {
					for (Document.Mention mention : sent.mentions) {

						StringBuilder typestxt = new StringBuilder();
						if (KB.mid2typelabels.containsKey(mention.mid)) {
							Set<String> types = KB.mid2typelabels.get(mention.mid);
							for (String t : types) {
								typestxt.append(t).append(" ");
							}
						} else {
							typestxt.append("<NULL_TYPES>");
						}

						mentext.append(mention.mid).append("\t");
						mentext.append(mention.wid).append("\t");
						mentext.append(mention.widTitle).append("\t");
						mentext.append(Integer.toString(mention.startToken)).append("\t");
						mentext.append(Integer.toString(mention.endToken)).append("\t");
						mentext.append(mention.surface).append("\t");
						mentext.append(sent.text).append("\t");
						mentext.append(typestxt.toString().trim()).append("\t");
						mentext.append(doc.coherenceString);
						//mentext.append(doc.docid.replaceAll(" ", "_"));
						mentext.append("\n");
						docm++;
					}
				}

				if (doc.docid.contains("testa")) {
					dev_wr.write(mentext.toString());
					dev++;
					dem+=docm;
				}
				else if (doc.docid.contains("testb")) {
					test_wr.write(mentext.toString());
					test++;
					tem+=docm;
				}
				else {
					train_wr.write(mentext.toString());
					tr++;
					trm+=docm;
				}
			}
			train_wr.close();
			dev_wr.close();
			test_wr.close();

			System.out.println("[#] Number of Train Docs : " + tr);
			System.out.println("[#] Number of Dev Docs : " + dev);
			System.out.println("[#] Number of Test Docs : " + test);

			System.out.println("[#] Number of Train Mentions : " + trm);
			System.out.println("[#] Number of Dev Mentions : " + dem);
			System.out.println("[#] Number of Test Mentions : " + tem);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void WriteNonNullMentions() {
		try {
			BufferedWriter train_wr = new BufferedWriter(new FileWriter(datasetMentionsFileDir + train_MentionsNNFile));
			BufferedWriter dev_wr = new BufferedWriter(new FileWriter(datasetMentionsFileDir + dev_MentionsNNFile));
			BufferedWriter test_wr = new BufferedWriter(new FileWriter(datasetMentionsFileDir + test_MentionsNNFile));

			int tr = 0, dev = 0, test = 0;
			int trm = 0, dem = 0, tem = 0;

			for (Document.Doc doc : documents) {
				int docm = 0;
				StringBuilder mentext = new StringBuilder();
				for (Document.Sentence sent : doc.sentences) {
					for (Document.Mention mention : sent.mentions) {
						if (mention.mid.equals("--NME--"))
							continue;

						StringBuilder typestxt = new StringBuilder();
						if (KB.mid2typelabels.containsKey(mention.mid)) {
							Set<String> types = KB.mid2typelabels.get(mention.mid);
							for (String t : types) {
								typestxt.append(t).append(" ");
							}
						} else {
							typestxt.append("<NULL_TYPES>");
						}

						mentext.append(mention.mid).append("\t");
						mentext.append(mention.wid).append("\t");
						mentext.append(mention.widTitle).append("\t");
						mentext.append(Integer.toString(mention.startToken)).append("\t");
						mentext.append(Integer.toString(mention.endToken)).append("\t");
						mentext.append(mention.surface).append("\t");
						mentext.append(sent.text).append("\t");
						mentext.append(typestxt.toString().trim()).append("\t");
						mentext.append(doc.coherenceString);
						//mentext.append(doc.docid.replaceAll(" ", "_"));
						mentext.append("\n");
						docm++;
					}
				}

				if (doc.docid.contains("testa")) {
					dev_wr.write(mentext.toString());
					dev++;
					dem+=docm;
				}
				else if (doc.docid.contains("testb")) {
					test_wr.write(mentext.toString());
					test++;
					tem+=docm;
				}
				else {
					train_wr.write(mentext.toString());
					tr++;
					trm+=docm;
				}
			}
			train_wr.close();
			dev_wr.close();
			test_wr.close();

			System.out.println("[#] Number of Train Docs : " + tr);
			System.out.println("[#] Number of Dev Docs : " + dev);
			System.out.println("[#] Number of Test Docs : " + test);

			System.out.println("[#] Number of Train Mentions : " + trm);
			System.out.println("[#] Number of Dev Mentions : " + dem);
			System.out.println("[#] Number of Test Mentions : " + tem);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void WriteLinks() {
		for (Document.Doc doc : documents) {
			StringBuilder mentionsText = new StringBuilder();
			for (Document.Sentence sent : doc.sentences) {
				List<Document.Mention> mentions = sent.mentions;
				for (Document.Mention mention : mentions) {
					mentionsText.append(mention.surface);
					mentionsText.append("\t");
				}
			}
			String text = mentionsText.toString().trim();
			String filename = doc.docid.replaceAll(" ", "_");
			FileUtils.writeStringToFile(datasetLinksDir+filename, text);
		}
	}

	public static void WriteDocs() {
		for (Document.Doc doc : documents) {
			StringBuilder docText = new StringBuilder();
			for (Document.Sentence sent : doc.sentences) {
				List<String> tokens = sent.tokens;
				for (String token : tokens) {
					docText.append(token);
					docText.append(" ");
				}
			}
			String text = docText.toString().trim();
			String filename = doc.docid.replaceAll(" ", "_");
			FileUtils.writeStringToFile(datasetDocsDir+filename, text);
		}
	}

	public static void AidaReader() {
		/* Input : AIDA-yago2-dataset.tsv
		 * DOCSTART starts a new doc, each line as a token and annotation for NER, MID WID etc.
		 * Empty lines break sentences.
		 */
		documents = new ArrayList<>();
		Document.Sentence s = new Document.Sentence();
		Document.Doc doc = null;
		Set<String> docMentionsForCoherence = null;
		List<String> lines = FileUtils.getLines(aida_tsv);
		for (String line : lines) {
			// Start of a new document
			if (line.startsWith("-DOCSTART-")) {
				if (doc != null) {
					// Adding coherence mentions to doc
					StringBuilder coherencestring = new StringBuilder();
					for (String str : docMentionsForCoherence)
						coherencestring.append(str).append(" ");
					doc.setCoherenceString(coherencestring.toString().trim());

					documents.add(doc);    // Adding doc to documents list
					System.out.println(doc.sentences.size());
				}
				int start = line.indexOf("(");
				String docId = line.substring(start + 1, line.length() - 1); // ignore last ")"
				docMentionsForCoherence = new HashSet<>();
				doc = new Document.Doc(docId);
				s = new Document.Sentence();
				System.out.println("Doc : " + doc.docid);
			}
			// Regular line
			else {
				// End of a sentence with empty line
				if (line.trim().length() == 0) {
					s.tokensToString();
					doc.sentences.add(s);
					s = new Document.Sentence();
					continue;
				}
				String[] data = line.split("\t");
				if (data.length == 0) {
					System.out.println("Line length 0 for doc id " + doc.docid);
				} else if (data.length == 1) {
					String token = data[0].trim();
					s.tokens.add(token);
				} else if (data.length == 4) {
					// --NME--
					String token = data[0].trim();
					Boolean mentionStart = "B".equals(data[1]);
					String mentionsurface = data[2];
					String wT = data[3];
					String mid = data[3];
					String wid = data[3];
					// As the currect token hasn't been added, with indexing 0 the length of the sentence is the mention start
					int startToken = s.tokens.size();
					if (mentionStart == true) {
						s.mentions.add(new Document.Mention(mentionsurface, wT, wid, mid, startToken));
						docMentionsForCoherence.add(mentionsurface.replaceAll(" ", "_"));
					}
					s.tokens.add(token);
				} else if (data.length == 6 || data.length == 7) {
					// The public AIDA dataset release has 6 or 7 columns,
					// depending on the presence of the Freebase mid.
					// However the additional IDs are not necessary for internal
					// use.
					String token = data[0].trim();
					Boolean mentionStart = "B".equals(data[1]);
					String mentionsurface = data[2];
					int startToken = s.tokens.size();

					// AIDA WikiTitle
					String wTitle = urlToTitle(data[4]);
					// Our KB Title
					String wT = KB.KBWikiTitle(wTitle);
					String wid = KB.wikiTitle2WID(wT);
					String mid = KB.wikiTitle2Mid(wT);
//					if (wT.equals("NULL_WIKITITLE")) {
//						String aidawid = data[5];
//						if (KB.wid2WikiTitle.containsKey(aidawid)) {
//							wT = KB.wid2WikiTitle.get(aidawid);
//							wid = aidawid;
//						} else
//							wid = "<unk_wid>";
//					} else {
//						wid = KB.wikiTitle2Wid.get(wT);
//					}
//					String mid = "";
//					if (data.length == 6) {
//						mid = "NULL";
//					} else {
//						mid = data[6].replaceAll("/", ".").substring(1);
//					}
					if (mentionStart == true) {
						s.mentions.add(new Document.Mention(mentionsurface, wT, wid, mid, startToken));
						docMentionsForCoherence.add(mentionsurface.replaceAll(" ", "_"));
					}
					s.tokens.add(token);
				} else {
					System.out.println("line has wrong format " + line + " for docId " + doc.docid);
				}
			}
		}
	}

	public static String urlToTitle(String wikiURL) {
		String goldTitle =
						wikiURL.replace("http://en.wikipedia.org/wiki/", "");
		if (goldTitle.endsWith("\"")) {
			goldTitle = goldTitle.substring(0, goldTitle.length() - 1).trim();
		}
		return goldTitle;
	}

	public static void main(String [] args) {

	}

}
