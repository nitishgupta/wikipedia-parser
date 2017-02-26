package edu.washington.cs.figer.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.washington.cs.figer.data.EntityProtos.Mention;

public class MentionReader {

	public String inputFile = null;
	public InputStream inputStream = null;
	public Mention current = null;

	private MentionReader() {
	}

	public static MentionReader getMentionReader(String file) {
		if (file == null) {
			return null;
		}
		MentionReader reader = null;

		try {
			reader = new MentionReader();
			reader.inputFile = file;
			reader.inputStream = new FileInputStream(file);
			if (file.endsWith(".gz")) {
				reader.inputStream = new GZIPInputStream(reader.inputStream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}

	public Mention readMention() {
		try {
			current = Mention.parseDelimitedFrom(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return current;
	}

	public void close() {
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printMention(Mention m) {
		Set<String> labels  = new HashSet<>();
		for (String fbtype : m.getLabelsList()) {
			if (FigerData.fbtype2label.containsKey(fbtype)) {
				labels.add(FigerData.fbtype2label.get(fbtype));
			}
		}
		System.out.println("start : " + m.getStart());
		System.out.println("end : " + m.getEnd());
		System.out.println("tokens : " + m.getTokensList());
		System.out.println("entity name : " + m.getEntityName());
		System.out.println("FbTypes : " + labels + "\n");
	}

	public static Set<String> getLabelSet(Mention m) {
		Set<String> labels = new HashSet<>();
		for (String fbtype : m.getLabelsList()) {
			if (FigerData.fbtype2label.containsKey(fbtype)) {
				labels.add(FigerData.fbtype2label.get(fbtype));
			}
		}
		return labels;
	}

	public static void main(String[] args) {
		MentionReader reader = MentionReader.getMentionReader(Constants.figerTrainingDataGz);
		Mention m = null;
		int totalmentions = 0, mentionstoconsider=0;
		Set<String> entities = new HashSet<String>();
		Set<String> entitiesInKb = new HashSet<String>();

		while ((m = reader.readMention()) != null) {
			totalmentions++;
			Set<String> labels = getLabelSet(m);
			if (labels.size() > 0) {
				mentionstoconsider++;
				String wikititle = m.getEntityName().replaceAll(" ", "_");
				String KBwikititle = KB.KBWikiTitle(wikititle);
				entities.add(wikititle);
				entitiesInKb.add(KBwikititle);
			}
		}


		System.out.println("Total Mentions : " + totalmentions);
		System.out.println("Consider Mentions (label>0) : " + mentionstoconsider);
		System.out.println("Total Entities : " + entities.size());
		System.out.println("Entities in KB : " + (entitiesInKb.size()-1));

	}
}