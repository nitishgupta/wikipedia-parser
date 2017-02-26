package edu.illinois.cs.cogcomp.wikiparse.kb;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class KB {
	public static final String NullWikiTitle = "NULLWIKITITLE";
	public static final String mid_alias_names_file = Constants.mid_alias_names_file;
	public static final String mid_names_wid_filepath = Constants.mid_names_wid_filepath;
	public static final String wiki_kb_docsDir = Constants.wiki_kb_docsDir;
	// Complete maps for entities as found in Freebase
	public static Map<String, String> mid2wid = new HashMap<>();
	public static Map<String, String> wid2mid = new HashMap<>();
	public static Map<String, String> wid2FBName = new HashMap<>();
	public static Map<String, List<String>> mid2aliases = new HashMap<>();
	// wikiTitle - With underscores
	public static Map<String, String> wid2WikiTitle = new HashMap<>();
	public static Map<String, String> wikiTitle2Wid = new HashMap<>();
	public static Map<String, String> redirect2WikiTitle = new HashMap<>();

	// MID-> FBTypes (565 types)
	public static Map<String, Set<String>> mid2types  = new HashMap<>();
	// MID -> FBTypeLebls that XIAO made (113 labels)
	public static Map<String, Set<String>> mid2typelabels  = new HashMap<>();


	// Wiki Ids from Freebase whose pages are legitimate in Wiki.
	public static Set<String> wids_FoundInWiki = new HashSet<>();
	// Wiki Ids for legitimate Wiki Pages who's text parse meets conditions. Filter on wikiMidMap_FoundInWiki
	public static Set<String> wids_ParsedInWiki = new HashSet<>();

	public static final int name_length_threshold = 75;

	static {
		System.out.println("[#] Making mid->wid, wid->mid and wid->FreebaseName maps ... ");
		makeMIDWIDMaps();
		//System.out.println("[#] Generating mid, list of names/alias map ... ");
		//makeNameAliasMap();
		//System.out.println("[#] Making wid set for wids in mid.names.wiki_id that are legitimate in Wikipedia");
		//foundInWiki();
		System.out.println("[#] Making WID set for pages in our KB (i.e. parsed in wiki and in freebase) : " + wiki_kb_docsDir);
		parsedInWikiPages();
		System.out.println("[#] Making wid->WikiTitle Map ... " );
		make_wid2WikiTitle();
		System.out.println("[#] Writing wid2FreebaseName file ... ");
		make_wid2FBNameFile();
		System.out.println("[#] Making Redirect Title -> Title Map");
		makeRedirect_WikiTitleMap();
		System.out.println("[#] Making MID -> Types Map.");
		System.out.println("[#] This is all MIDS that are in Wikipedia, not just the ones that were parsed.");
		make_MID2FBTypesMap();
	}

	/**
	 * mid->wid & wid->mid map
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
			wid2FBName.put(wid, name);

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
		System.out.println(" [#] Number of mid duplicates : " + duplicateMIDs);
		System.out.println(" [#] Number of wid duplicates : " + duplicateWIDs);
		System.out.println(" [#] Number of mids : " + mid2wid.size());
		System.out.println(" [#] Number of wids : " + wid2mid.size());
	}

	/*
	 * Make mid -> List<Names/Alias> Map only for mids found in mid.names.wiki_id
	 */
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


	/**
	 * File loader for parsedInWiki
	 * Reads file wid.parsedInWiki if exists
	 */
	private static void load_widParsedInWiki() {
		System.out.println(" [#] Loading the wids in our KB ... ");
		BufferedReader bwr = null;
		try {
			bwr = new BufferedReader(new FileReader(Constants.wid_parsedInWiki_filepath));
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
			wids_ParsedInWiki.add(wid);
			try {
				line = bwr.readLine();
			} catch (IOException e) {
				System.out.println("Error in reading line in wid.FoundInWiki");
			}
		}
		System.out.println(" [#] Number of wids in our KB : " + wids_ParsedInWiki.size());
	}

	/**
	 *
	 */
	private static void parsedInWikiPages() {
		File f = new File(Constants.wid_parsedInWiki_filepath);
		if (f.exists()) {
			load_widParsedInWiki();
		} else {
			System.out.println(" [#] wid.parsedInWiki : File NOT found. Making ... ");
			File dir = new File(wiki_kb_docsDir);
			if (!dir.exists() == true || !dir.isDirectory() == true) {
				System.out.println("Docs Directory does not exist : " + wiki_kb_docsDir);
				return;
			}

			for (String wid : dir.list()) {
				wids_ParsedInWiki.add(wid);
			}

			System.out.println(" [#] Number of Wiki Pages parsed and saved in docs links dir : " + wids_ParsedInWiki.size());

			Set<String> notParsed = new HashSet<String>(wid2mid.keySet());
			notParsed.removeAll(wids_ParsedInWiki);

			StringBuffer notparsed = new StringBuffer();
			for (String wid : notParsed) {
				notparsed.append(wid + "\n");
			}

			wids_ParsedInWiki.retainAll(wid2mid.keySet());
			System.out.println(" [#] Number of Wiki Pages parsed also in freebase(mid.names.wid): " + wids_ParsedInWiki.size());
			FileUtils.writeSetToFile(wids_ParsedInWiki, Constants.wid_parsedInWiki_filepath);
			FileUtils.writeStringToFile("/save/ngupta19/wikipedia/wiki_kb/widsNotParsed", notparsed.toString());
		}
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

	private static void make_wid2FBNameFile() {
		File f = new File(Constants.wid_FBName_filepath);
		if (f.exists() && f.isFile()) {
			System.out.println(" [#] Done.");
			return;
		}

		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(Constants.wid_FBName_filepath));
			for (String wid : wid2WikiTitle.keySet()) {
				bwr.write(wid);
				bwr.write("\t");
				bwr.write(wid2FBName.get(wid));
				bwr.write("\n");
			}
			bwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(" [#] Done.");
	}

	private static void load_redirect2WikiTitle() {
		int num_redirectTitles = 0;
		try {
			BufferedReader bwr = new BufferedReader(new FileReader(Constants.redirectTitle_WikiTitle_filepath));
			String line = bwr.readLine();
			while (line != null) {
				String[] ssplit = line.split("\t");
				String redirectTitle = ssplit[0].trim();
				String wikititle = ssplit[1].trim();
				redirect2WikiTitle.put(redirectTitle, wikititle);
				num_redirectTitles++;
				line = bwr.readLine();
			}
			bwr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" [#] redirect2WikiTitle Map Loaded! Total Redirect Titles : " + num_redirectTitles);
	}


	/**
	 * Makes the set of redirectWikiTitle -> Actual Wiki Title
	 * Also writes a file for the same.
	 */
	private static void makeRedirect_WikiTitleMap() {
		File f = new File(Constants.redirectTitle_WikiTitle_filepath);
		if (f.exists() && f.isFile()) {
			System.out.println(" [#] redirect.wikiTitle file found. Loading ... ");
			load_redirect2WikiTitle();
		} else {
			int num_wikipages_w_redirect = 0;
			int num_pages_processed = 0;
			int num_redirects_written = 0;
			Set<String> redirectTitlesWritten = new HashSet<>();
			Set<String> wikiTitles = wikiTitle2Wid.keySet();
			Set<String> redirectPageNotFound = new HashSet<>();
			System.out.println(" [#] Number of wikiTitles to process : " + wikiTitles.size());

			long startTime = System.currentTimeMillis();
			long rep_startTime = System.currentTimeMillis();

			try {
				BufferedWriter bwr = new BufferedWriter(new FileWriter(Constants.redirectTitle_WikiTitle_filepath));
				BufferedWriter bw = new BufferedWriter(new FileWriter("/save/ngupta19/freebase/types_xiao/redirectPageNotInKB.txt"));
				for (String wikiTitle : wikiTitles) {
					Page page = WikiDB.wiki.getPage(wikiTitle);
					Set<String> redirects = page.getRedirects();
					if (!redirects.isEmpty()) {
						for (String redirectTitle : redirects) {
							if (!redirectTitle.trim().equals("")) {
								if (!redirectTitlesWritten.contains(redirectTitle)) {
									// This is the title, internet redirects to
									try {
										String wikiT = WikiDB.wiki.getPage(redirectTitle).getTitle().getWikiStyleTitle();
										if (!wikiTitle2Wid.containsKey(wikiT)) {
											bw.write("Redirect: " + redirectTitle + "\tW: " + wikiT + "\tWT: " + wikiTitle + "\n");
										} else {
											bwr.write(redirectTitle);
											bwr.write("\t");
											bwr.write(wikiT);
											bwr.write("\n");
											num_redirects_written++;
										}
									} catch (WikiPageNotFoundException e) {
										System.out.println("Redirect not found : " + redirectTitle);
										redirectPageNotFound.add(redirectTitle);
									}
								}
							}
						}
						num_wikipages_w_redirect++;
					}
					num_pages_processed++;
					if (num_pages_processed % 10000 == 0) {
						long estimatedTime = System.currentTimeMillis() - rep_startTime;
						rep_startTime = System.currentTimeMillis();
						double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
						System.out.print(num_pages_processed + " (" +
										num_redirects_written + ")" + " (" +
										new DecimalFormat("#######.####").format(tt) + " mins) .. ");
					}
				}
				bwr.close();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			long estimatedTime = System.currentTimeMillis() - startTime;
			double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
			System.out.println("Total Time : " + tt + "  minutes");

			System.out.println("\n [#] Number of wikiTitles with redirects : " + num_wikipages_w_redirect);
			System.out.println("\n [#] Number of redirect titles written : " + num_redirects_written);
		}
	}

	private static void load_mid2FBTypes() {
		Set<String> fbtypes = new HashSet<>();
		Set<String> fbtypelabels = new HashSet<>();
		try {
			BufferedReader bwr = new BufferedReader(new FileReader(Constants.mid_AllFBTypes_filename));
			String line = bwr.readLine();
			while (line != null) {
				String[] ssplit = line.split("\t");
				String mid = ssplit[0].trim();
				String[] types = ssplit[1].trim().split(" ");
				Set<String> tts = new HashSet<String>(Arrays.asList(types));
				fbtypes.addAll(tts);
				mid2types.put(mid, tts);
				line = bwr.readLine();
			}
			bwr.close();

			bwr = new BufferedReader(new FileReader(Constants.mid_AllFBTypeLabels_filename));
			line = bwr.readLine();
			while (line != null) {
				String[] ssplit = line.split("\t");
				String mid = ssplit[0].trim();
				String[] types = ssplit[1].trim().split(" ");
				Set<String> tts = new HashSet<String>(Arrays.asList(types));
				fbtypelabels.addAll(tts);
				mid2typelabels.put(mid, tts);
				line = bwr.readLine();
			}
			bwr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("  [#] FB Types : " + fbtypes.size() + "  FBTypeLabels : " + fbtypelabels.size());
	}

	private static Map<String, String> _FB2Label() {
		System.out.println(" [#] Making FbType2Label map ... ");
		Map<String, String> fbtype2label = new HashMap<>();
		String[] lines = FileUtils.getTextFromFile(Constants.figerTypesMap).trim().split("\n");
		for (String line : lines) {
			String [] fbtype_label = line.split("\t");
			assert (fbtype_label.length == 2);

			String fbtype = fbtype_label[0].replaceAll("/", ".").substring(1);
			String label = fbtype_label[1].replaceAll("/", ".").substring(1);
			//System.out.println(fbtype + "\t" + label);
			fbtype2label.put(fbtype, label);
		}
		System.out.println(" [#] Number of Fbtypes in Map : " + fbtype2label.keySet().size());
		return fbtype2label;
	}

	private static void make_MID2FBTypesMap() {
		File f = new File(Constants.mid_AllFBTypes_filename);
		File f2 = new File(Constants.mid_AllFBTypeLabels_filename);
		if (f.exists() && f2.exists()) {
			System.out.println(" [#] mid.alltypes and mid.fbtypelabels file found. Loading ... ");
			load_mid2FBTypes();
		}

		else {
			System.out.println(" [#] mid.alltypes file NOT found. Making ... ");
			try {
				BufferedReader br = new BufferedReader(new FileReader(Constants.allMIDs_FBType_filename));
				Map<String, String> fbtype2label = _FB2Label();

				String line = br.readLine();
				while (line != null) {
					String[] midfbtype = line.trim().split("\t");
					if (mid2wid.containsKey(midfbtype[0])) {
						if (!mid2types.containsKey(midfbtype[0])) {
							mid2types.put(midfbtype[0], new HashSet<String>());
						}
						mid2types.get(midfbtype[0]).add(midfbtype[1]);
						if (!mid2typelabels.containsKey(midfbtype[0])) {
							mid2typelabels.put(midfbtype[0], new HashSet<String>());
						}
						mid2typelabels.get(midfbtype[0]).add(fbtype2label.get(midfbtype[1]));
					}
					line = br.readLine();
				}
				br.close();

				// Write mid2fbtype to file
				BufferedWriter bwr = new BufferedWriter(new FileWriter(Constants.mid_AllFBTypes_filename));
				for (Map.Entry<String, Set<String>> entry : mid2types.entrySet()) {
					StringBuilder lin2wr = new StringBuilder(entry.getKey());
					lin2wr.append("\t");
					for (String type : entry.getValue()) {
						lin2wr.append(type);
						lin2wr.append(" ");
					}
					line = lin2wr.toString().trim();
					bwr.write(line);
					bwr.write("\n");
				}
				bwr.close();

				// Write mid2fbtypelabels to file
				bwr = new BufferedWriter(new FileWriter(Constants.mid_AllFBTypeLabels_filename));
				for (Map.Entry<String, Set<String>> entry : mid2typelabels.entrySet()) {
					StringBuilder lin2wr = new StringBuilder(entry.getKey());
					lin2wr.append("\t");
					for (String type : entry.getValue()) {
						lin2wr.append(type);
						lin2wr.append(" ");
					}
					line = lin2wr.toString().trim();
					bwr.write(line);
					bwr.write("\n");
				}
				bwr.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(" [#] mid.FBTypes size : " + mid2types.size());
	}


//	/**

	public static String KBWikiTitle(String wikititle) {
		if (KB.wikiTitle2Wid.containsKey(wikititle)) {
			return wikititle;
	  } else if (KB.redirect2WikiTitle.containsKey(wikititle)) {
			return KB.redirect2WikiTitle.get(wikititle);
		} else {
			Page page = null;
			try {
				page = WikiDB.getPage(wikititle);
				if (page==null)
					return NullWikiTitle;
				String pagetitle = page.getTitle().getWikiStyleTitle();
				if (KB.wikiTitle2Wid.containsKey(pagetitle)) {
					return wikititle;
				} else if (KB.redirect2WikiTitle.containsKey(pagetitle)) {
					return KB.redirect2WikiTitle.get(pagetitle);
				} else {
					return NullWikiTitle;
				}
			} catch (WikiApiException e) {
				return NullWikiTitle;
			}
		}
	}

	public static String wikiTitle2WID(String wikititle) {
		String wikiTitle = KB.KBWikiTitle(wikititle);
		if (wikiTitle2Wid.containsKey(wikiTitle))
			return wikiTitle2Wid.get(wikiTitle);
		else
			return "<unk_wid>";
	}

	public static String wikiTitle2Mid(String wikititle) {
		String wid = KB.wikiTitle2WID(wikititle);
		if (wid2mid.containsKey(wid))
			return wid2mid.get(wid);
		else
			return "<unk_mid>";
	}


//	 * After making MID->WID from Freebase, Find the WIDs in Wikipedia
//	 */
//	private static void foundInWiki() {
//		File f = new File(wid_foundInWiki_filepath);
//		if (f.exists()) {
//			load_widFoundInWiki();
//		} else {
//			BufferedWriter bwr = null;
//			try {
//				bwr = new BufferedWriter(new FileWriter(wid_foundInWiki_filepath));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			int widsprocessed = 0;
//			PageIterator pages = new PageIterator(WikiDB.wiki, true, 100000);
//			while (pages.hasNext()) {
//				widsprocessed++;
//				Page page = null;
//				String wid = null, mid = null;
//				if ((page = pages.next()) != null) {
//					try {
//						if (!page.isRedirect() && !page.isDisambiguation() && !page.isDiscussion() &&
//										!page.getTitle().getPlainTitle().startsWith("List of") &&
//										!page.getTitle().getPlainTitle().startsWith("Lists of")) {
//
//							if (wid2mid.containsKey(Integer.toString(page.getPageId()))) {
//								wid = Integer.toString(page.getPageId());
//								wids_FoundInWiki.add(wid);
//								try {
//									bwr.write(wid);
//									bwr.write("\n");
//								} catch (IOException e) {
//									System.out.println("Error in writing line in wid.FoundInWiki");
//								}
//							}
//						}
//					} catch (WikiTitleParsingException e) {
//
//					}
//				}
//				if (widsprocessed % 50000 == 0)
//					System.out.print(widsprocessed + " ... ");
//			}
//			try {
//				bwr.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			System.out.println();
//			System.out.println(" [#] Number of pages from Wikipedia processed : " + widsprocessed);
//			System.out.println(" [#] Number of wiki ids in mid.names.wid : " + KB.wid2mid.size());
//			System.out.println(" [#] Number of pages found in wiki : " + wids_FoundInWiki.size());
//		}
//	}

//	private static void makeRedirect_WikiTitleMap() {
//		File f = new File(Constants.redirectTitle_WikiTitle_filepath);
//		if (f.exists() && f.isFile()) {
//			System.out.println(" [#] redirect.wikiTitle file found. Loading ... ");
//			load_redirect2WikiTitle();
//		} else {
//			int num_wikipages_w_redirect = 0;
//			int num_pages_processed = 0;
//			int num_redirects_written = 0;
//			Set<String> redirectTitles = new HashSet<>();
//			Set<String> wikiTitles = wikiTitle2Wid.keySet();
//			System.out.println("Number of wikiTitles to process : " + wikiTitles.size());
//
//			long startTime = System.currentTimeMillis();
//			long rep_startTime = System.currentTimeMillis();
//
//
//			try {
//				BufferedWriter bwr = new BufferedWriter(new FileWriter(Constants.redirectTitle_WikiTitle_filepath));
//				for (String wikiTitle : wikiTitles) {
//					Page page = wiki.getPage(wikiTitle);
//					Set<String> redirects = page.getRedirects();
//					if (!redirects.isEmpty()) {
//						for (String redirectTitle : redirects) {
//							if (!redirectTitle2WikiTitles.containsKey(redirectTitle)) {
//								//redirectTitle2WikiTitle.put(redirectTitle, wikiTitle);
//								redirectTitle2WikiTitles.put(redirectTitle, new ArrayList<String>());
//								redirectTitle2WikiTitles.get(redirectTitle).add(wikiTitle);
//								StringBuilder s = new StringBuilder();
//								s.append(redirectTitle);
//								s.append("\t");
//								s.append(wikiTitle);
//								s.append("\n");
//								bwr.write(s.toString());
//								num_redirects_written++;
//								if (redirectTitle.equals(wikiTitle))
//									System.out.println("R = W : " + wikiTitle);
//							} else {
//								redirectTitle2WikiTitles.get(redirectTitle).add(wikiTitle);
//								System.out.println("Redirect Title Found Again : " + redirectTitle);
//							}
//						}
//						num_wikipages_w_redirect++;
//					}
//					num_pages_processed++;
//					if (num_pages_processed % 5000 == 0) {
//						long estimatedTime = System.currentTimeMillis() - rep_startTime;
//						rep_startTime = System.currentTimeMillis();
//						double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
//						System.out.print(num_pages_processed + " (" +
//										num_redirects_written + ")" + " (" +
//										new DecimalFormat("#######.####").format(tt) + " mins) .. ");
//					}
//				}
//				bwr.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			long estimatedTime = System.currentTimeMillis() - startTime;
//			double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
//			System.out.println("Total Time : " + tt + "  minutes");
//
//			System.out.println("\n [#] Number of wikiTitles with redirects : " + num_wikipages_w_redirect);
//			System.out.println("\n [#] Number of redirect titles written : " + num_redirects_written);
//		}
//	}

//			Page page = wiki.getPage("John_McLaughlin_(musician)");
//			System.out.println("Yes : " + page.isRedirect());
//			System.out.println("title : " + page.getTitle().getWikiStyleTitle());
//			String wikiTitle = page.getRedirects().iterator().next();
//			System.out.println(page.getRedirects());


		public static void main(String [] args) throws Exception {
			System.out.println("Hello World!");

//		text = "";
//		System.out.println("Text : " + text);
//		TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
//		TextAnnotation ta = tab.createTextAnnotation("", "", text);
//		String[] tokens = ta.getTokens();
//		System.out.println(tokens);
	}


}
