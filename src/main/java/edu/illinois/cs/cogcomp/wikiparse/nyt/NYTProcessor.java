package edu.illinois.cs.cogcomp.wikiparse.nyt;

import edu.illinois.cs.cogcomp.wikiparse.nyt.nytlabs.corpus.core.NYTCorpusDocument;
import edu.illinois.cs.cogcomp.wikiparse.nyt.nytlabs.corpus.core.NYTCorpusDocumentParser;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nitishgupta on 11/4/16.
 */
public class NYTProcessor {
	public static final String nyt03dir = "/save/ngupta19/annotated_nyt/accum2003-07/";
	public static final String nyt87dir = "/save/ngupta19/annotated_nyt/accum1987-02/";

	public static List<String> nyt87filelist = new ArrayList<>();
	public static List<String> nyt03filelist = new ArrayList<>();

	static {
		nyt87filelist = makeFileList(nyt87dir);
		nyt03filelist = makeFileList(nyt03dir);

		System.out.println("NYT 87 List : " + nyt87filelist.size());
		System.out.println("NYT 03 List : " + nyt03filelist.size());
	}

	public static List<String> makeFileList(String dirname) {
		List<String> filelist = new ArrayList<String>();
		if (new File(dirname).isDirectory()) {
			for (String wid : new File(dirname).list()) {
				filelist.add(wid);
			}
			return filelist;
		} else {
			return filelist;
		}
	}

	public static boolean filterDoc(NYTCorpusDocument d) {
		boolean leg = true;
		for (String classifier : d.getTaxonomicClassifiers()) {
			if (classifier.contains("Top/Classifieds"))
				return false;
		}

		for (String mat : d.getTypesOfMaterial()) {
			if (mat.toLowerCase().contains("list") || mat.toLowerCase().contains("statistic"))
				return false;
		}

		return true;
	}

	public static NYTCorpusDocument read_NYTdocument(String filepath){
		NYTCorpusDocumentParser parser = new NYTCorpusDocumentParser();
		File file = new File(filepath);
		NYTCorpusDocument timesLDCDocument = parser.parseNYTCorpusDocumentFromFile(file, false);
		return timesLDCDocument;
	}


	public static void filterdocs(String nytdir, List<String> nytfilelist) {
		int count = 0;
		int num_to_process = nytfilelist.size();
		StringBuilder filter_docs = new StringBuilder();
		System.out.println("Num of docs left to be processed : " + num_to_process);
		for (String filename : nytfilelist) {
			String path = nytdir + filename;
			NYTCorpusDocument d = read_NYTdocument(path);
			if (filterDoc(d)) {
				count ++;
				filter_docs.append(filename);
				filter_docs.append("\t");
				filter_docs.append(d.getPublicationDate().toString());
				filter_docs.append("\n");
			}
			num_to_process--;
			if (num_to_process % 10000 == 0) {
				System.out.print(num_to_process + " ... ");
				break;
			}
		}
		FileUtils.writeStringToFile("/save/ngupta19/annotated_nyt/legitimate_03.txt", filter_docs.toString());
		System.out.println("Num of illeg docs : " + count);
	}

	public static void main(String [] args) {
		System.out.println(NYTProcessor.nyt03filelist.size());

		filterdocs(nyt03dir, nyt03filelist);
	}


}
