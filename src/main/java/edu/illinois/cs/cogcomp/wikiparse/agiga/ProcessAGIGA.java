package edu.illinois.cs.cogcomp.wikiparse.agiga;

import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.jhu.agiga.*;

import java.io.File;
import java.util.*;

/**
 * Created by nitishgupta on 11/2/16.
 */
public class ProcessAGIGA {
	public static final String agiga_dir = Constants.agiga_xml_dir;
	public static List<String> xml_files = new ArrayList<>();
	public static final Set<String> nertypes = new HashSet<>(
					Arrays.asList("ORGANIZATION", "PERSON", "LOCATION"));
	public static final String eosword = "<eos_word>";
	static {
		load_xml_filenames();
		readDocuments();
	}

	public static void load_xml_filenames() {
		File f = new File(agiga_dir);
		if (!f.exists() == true || !f.isDirectory() == true) {
			System.out.println("AGIGA Directory does not exist : " + agiga_dir);
			return;
		}

		for (String filename : new File(agiga_dir).list()) {
			if (new File(agiga_dir + filename).isFile())
				xml_files.add(filename);
		}
		System.out.println(" [#] Number of xml files in AGIGA : " + xml_files.size());
	}

	public static String getDocText_NER(AgigaDocument doc) {
		StringBuffer doctext = new StringBuffer();
		List<String> ner = new ArrayList<>();

		for (AgigaSentence sent : doc.getSents()) {
			String currner = "";
			String currnertype = "";

			for (AgigaToken token : sent.getTokens()) {
				String word = token.getWord();
				String nertag = token.getNerTag();
				if (nertypes.contains(nertag)) {
					if (nertag.equals(currnertype)) {
						currner += word + " ";
					} else {
						if (!currner.equals(""))
							ner.add(currner.trim());
						currner = word + " ";
						currnertype = nertag;
					}
				} else {
					if (!currner.equals("")) {
						ner.add(currner.trim());
						currner = "";
						currnertype="";
					}
				}
				doctext.append(word + " ");
			}
			doctext.append(eosword + " ");
		}
		StringBuilder all_ner = new StringBuilder();
		for (String n : ner) {
			all_ner.append(n);
			all_ner.append(" ");
		}
		StringBuffer doc_ner = new StringBuffer();
		doc_ner.append(doctext.toString().trim());
		doc_ner.append("\t");
		doc_ner.append(all_ner);
		return doc_ner.toString().trim();
	}

	public static void readDocuments() {
		int total_docs = 0;
		for (int i = 0; i < xml_files.size(); i++) {
			String filepath = agiga_dir + xml_files.get(i);
			StreamingDocumentReader reader = new StreamingDocumentReader(filepath, new AgigaPrefs());
			for (AgigaDocument doc : reader) {
				System.out.println("DOC NER : " + doc.getDocId());
				String doc_ner = getDocText_NER(doc);
				System.out.println(doc_ner);
			}
			break;
		}
		System.out.println("Total Number of Docs : " + total_docs);
	}

	public static void main(String [] args) {
		List<String> x = xml_files;
	}
}
