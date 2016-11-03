package edu.illinois.cs.cogcomp.wikiparse.kb;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class KB {
	public static Wikipedia wiki = WikiDB.wiki;
	public static final String mid_alias_names_file = Constants.mid_alias_names_file;
	public static final String mid_names_wid_filepath = Constants.mid_names_wid_filepath;
	public static final String wid_foundInWiki_filepath = Constants.wid_foundInWiki_filepath;
	public static final String wiki_kb_docsDir = Constants.wiki_kb_docsDir;
	// Complete maps for entities as found in Freebase
	public static Map<String, String> mid2wid = new HashMap<>();
	public static Map<String, String> wid2mid = new HashMap<>();
	public static Map<String, List<String>> mid2aliases = new HashMap<>();
	// wikiTitle - With underscores
	public static Map<String, String> wid2WikiTitle = new HashMap<>();
	public static Map<String, String> wikiTitle2Wid = new HashMap<>();

	// Wiki Ids from Freebase whose pages are legitimate in Wiki.
	public static Set<String> wids_FoundInWiki = new HashSet<>();
	// Wiki Ids for legitimate Wiki Pages who's text parse meets conditions. Filter on wikiMidMap_FoundInWiki
	public static Set<String> wids_ParsedInWiki = new HashSet<>();

	public static final int name_length_threshold = 75;

	static {
		System.out.println("[#] Making mid->wid and wid->mid maps ... ");
		makeMIDWIDMaps();
		System.out.println("[#] Generating mid, list of names/alias map ... ");
		makeNameAliasMap();
		System.out.println("[#] Making wid set for wids in mid.names.wiki_id that are legitimate in Wikipedia");
		foundInWiki();
		System.out.println("[#] Making wid set for pages with text in docs_links of : " + wiki_kb_docsDir);
		parsedInWikiPages();
		System.out.println("[#] Making wid->WikiTitle Map ... " );
		make_wid2WikiTitle();
	}

	/*
	 * Make mid -> List<Names/Alias> Map only for mids found in mid.names.wiki_id
	 */
	private static void makeMIDWIDMaps() {
		int duplicateMIDs = 0, duplicateWIDs = 0;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(mid_names_wid_filepath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (line != null) {
			String[] midnamewid = line.trim().split("\t");
			assert midnamewid.length == 3;
			String mid = midnamewid[0].trim();
			String name = midnamewid[1].trim();
			String wid = midnamewid[2].trim();
			if (mid2wid.containsKey(mid)) {
				duplicateMIDs++;
			} else {
				mid2wid.put(mid, wid);
			}

			if (wid2mid.containsKey(wid)) {
				duplicateWIDs++;
			} else {
				wid2mid.put(wid, mid);
			}
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(" [#] Number of mid duplicates : " + duplicateMIDs);
		//System.out.println(" [#] Number of wid duplicates : " + duplicateWIDs);
		System.out.println(" [#] Number of mids : " + mid2wid.size());
		System.out.println(" [#] Number of wids : " + wid2mid.size());
	}

	private static void makeNameAliasMap() {
		BufferedReader bwr = null;
		try {
			bwr = new BufferedReader(new FileReader(mid_alias_names_file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;
		try {
			line = bwr.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (line != null) {
			String[] mid_name = line.split("\t");
			String mid = mid_name[0].trim();
			String name = mid_name[1].trim();
			if (name.length() <= name_length_threshold && mid2wid.containsKey(mid)) {
				if (!mid2aliases.containsKey(mid))
					mid2aliases.put(mid, new ArrayList<String>());
				if (!mid2aliases.get(mid).contains(name)) {
					mid2aliases.get(mid).add(name);
				}
			}
			try {
				line = bwr.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(" [#] Total mids read : " + mid2aliases.size());
	}

	private static void load_widFoundInWiki() {
		System.out.println("Loading the wid->mid map for entities found in Wikipedia ... ");
		BufferedReader bwr = null;
		try {
			bwr = new BufferedReader(new FileReader(wid_foundInWiki_filepath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;
		try {
			line = bwr.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (line != null) {
			String wid = line.trim();
			assert wid2mid.containsKey(wid);
			wids_FoundInWiki.add(wid);
			try {
				line = bwr.readLine();
			} catch (IOException e) {
				System.out.println("Error in reading line in wid.FoundInWiki");
			}
		}
		System.out.println(" [#] Number of pages found in wiki : " + wids_FoundInWiki.size());
	}

	private static void foundInWiki() {
		File f = new File(wid_foundInWiki_filepath);
		if (f.exists()) {
			load_widFoundInWiki();
		} else {
			BufferedWriter bwr = null;
			try {
				bwr = new BufferedWriter(new FileWriter(wid_foundInWiki_filepath));
			} catch (IOException e) {
				e.printStackTrace();
			}
			int widsprocessed = 0;
			PageIterator pages = new PageIterator(WikiDB.wiki, true, 100000);
			while (pages.hasNext()) {
				widsprocessed++;
				Page page = null;
				String wid = null, mid = null;
				if ((page = pages.next()) != null) {
					try {
						if (!page.isRedirect() && !page.isDisambiguation() && !page.isDiscussion() &&
								!page.getTitle().getPlainTitle().startsWith("List of") &&
								!page.getTitle().getPlainTitle().startsWith("Lists of")) {

							if (wid2mid.containsKey(Integer.toString(page.getPageId()))) {
								wid = Integer.toString(page.getPageId());
								wids_FoundInWiki.add(wid);
								try {
									bwr.write(wid);
									bwr.write("\n");
								} catch (IOException e) {
									System.out.println("Error in writing line in wid.FoundInWiki");
								}
							}
						}
					} catch (WikiTitleParsingException e) {

					}
				}
				if (widsprocessed % 50000 == 0)
					System.out.print(widsprocessed + " ... ");
			}
			try {
				bwr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println();
			System.out.println(" [#] Number of pages from Wikipedia processed : " + widsprocessed);
			System.out.println(" [#] Number of wiki ids in mid.names.wid : " + KB.wid2mid.size());
			System.out.println(" [#] Number of pages found in wiki : " + wids_FoundInWiki.size());
		}
	}

	private static void parsedInWikiPages() {
		File f = new File(wiki_kb_docsDir);
		if (!f.exists() == true || !f.isDirectory() == true) {
			System.out.println("Docs Directory does not exist : " + wiki_kb_docsDir);
			return;
		}

		for (String wid : new File(wiki_kb_docsDir).list()) {
			wids_ParsedInWiki.add(wid);
		}

		System.out.println(" [#] Number of Wiki Pages parsed and saved in docs links dir : " + wids_ParsedInWiki.size());

		Set<String> notParsed = new HashSet<String>(wids_FoundInWiki);
		notParsed.removeAll(wids_ParsedInWiki);

		StringBuffer notparsed = new StringBuffer();
		for (String wid : notParsed){
			notparsed.append(wid + "\n");
		}
		FileUtils.writeStringToFile("/save/ngupta19/wikipedia/wiki_kb_10_15/notparsed", notparsed.toString());
	}

	private static void load_wid2WikiTitle() {
		try {
			BufferedReader bwr = new BufferedReader(new FileReader(Constants.wid_WikiTitle_filepath));
			String line = bwr.readLine();
			while (line != null) {
				String[] ssplit = line.split("\t");
				String wid = ssplit[0].trim();
				String wikititle = ssplit[1].trim();
				wid2WikiTitle.put(wid, wikititle);
				wikiTitle2Wid.put(wikititle, wid);
				line = bwr.readLine();
			}
			bwr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void make_wid2WikiTitle() {
		File f = new File(Constants.wid_WikiTitle_filepath);
		if (f.exists() && f.isFile()) {
			System.out.println(" [#] wid.WikiTitle file found. Loading ... ");
			load_wid2WikiTitle();
		}

		else {
			int pagesread = 0, titles_written = 0;
			System.out.println(" [#] wid.WikiTitle file NOT found. Making ... ");
			try {
				PageIterator pages = new PageIterator(WikiDB.wiki, true, 100000);
				BufferedWriter bwr = new BufferedWriter(new FileWriter(Constants.wid_WikiTitle_filepath));
				while (pages.hasNext()) {
					pagesread++;
					Page page = pages.next();
					String wid = Integer.toString(page.getPageId());
					if (wids_ParsedInWiki.contains(wid)) {
						titles_written++;
						String title = page.getTitle().getWikiStyleTitle();
						wid2WikiTitle.put(wid, title);
						wikiTitle2Wid.put(title, wid);
						bwr.write(wid + "\t" + title + "\n");
					}
					if (pagesread % 10000 == 0) {
						System.out.print(pagesread + "(" + titles_written + ")" + " ... ");
					}
				}
				bwr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(" [#] wid.WikiTitle size : " + wid2WikiTitle.size());
	}

	public static void main(String [] args) {
		System.out.println(KB.wids_ParsedInWiki.size());
	}


}
