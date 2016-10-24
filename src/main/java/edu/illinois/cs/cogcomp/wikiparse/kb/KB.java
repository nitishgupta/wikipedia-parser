package edu.illinois.cs.cogcomp.wikiparse.kb;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;
import edu.illinois.cs.cogcomp.wikiparse.util.Constants;

import java.io.*;
import java.util.*;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class KB {
    public static Wikipedia wiki = WikiDB.wiki;
    public static final String mid_alias_names_file = Constants.mid_alias_names_file;
    public static final String mid_names_wid_filepath = Constants.mid_names_wid_filepath;
    public static final String wid_foundInWiki_filepath = Constants.wid_foundInWiki_filepath;
    // Complete maps for entities as found in Freebase
    public static Map<String, String> midWidMap = new HashMap<>();
    public static Map<String, String> widMidMap = new HashMap<>();
    public static Map<String, List<String>> midAliasNamesMap= new HashMap<>();
    // Wiki Ids from Freebase whose pages are legitimate in Wiki.
    public static Map<String, String> widMidMap_FoundInWiki = new HashMap<>();


    public static final int name_length_threshold= 75;

    static  {
        System.out.println("[#] Making mid->wid and wid->mid maps ... ");
        makeMIDWIDMaps();
        System.out.println("[#] Generating mid, list of names/alias map ... ");
        makeNameAliasMap();
        System.out.println("[#] Finding pages in mid.names.wiki_id that are legitimate in Wikipedia");
        foundInWiki();
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
            if (midWidMap.containsKey(mid)) {
                duplicateMIDs++;
            } else {
                midWidMap.put(mid, wid);
            }

            if (widMidMap.containsKey(wid)) {
                duplicateWIDs++;
            } else {
                widMidMap.put(wid, mid);
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
        System.out.println(" [#] Number of mids : " + midWidMap.keySet().size());
        System.out.println(" [#] Number of wids : " + widMidMap.keySet().size());
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
            if (name.length() <= name_length_threshold && midWidMap.containsKey(mid)) {
                if (!midAliasNamesMap.containsKey(mid))
                    midAliasNamesMap.put(mid, new ArrayList<String>());
                if (!midAliasNamesMap.get(mid).contains(name)) {
                    midAliasNamesMap.get(mid).add(name);
                }
            }
            try {
                line = bwr.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(" [#] Total mids read : " + midAliasNamesMap.keySet().size());
    }

    private static void load_widFoundInWiki() {
        System.out.println("Loading the wid->mid map for entities found in Wikipedia ... ");
        BufferedReader bwr = null;
        try {
            bwr = new BufferedReader(new FileReader(wid_foundInWiki_filepath));
        }  catch(FileNotFoundException e) {
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
            assert widMidMap.containsKey(wid);
            widMidMap_FoundInWiki.put(wid, widMidMap.get(wid));
            try {
                line = bwr.readLine();
            } catch (IOException e) {
                System.out.println("Error in reading line in wid.FoundInWiki");
            }
        }
        System.out.println(" [#] Number of pages found in wiki : " + widMidMap_FoundInWiki.keySet().size());
    }

    private static void foundInWiki() {
        File f = new File(wid_foundInWiki_filepath);
        if (f.exists()) {
            load_widFoundInWiki();
        } else {
            BufferedWriter bwr = null;
            try {
                bwr = new BufferedWriter(new FileWriter(wid_foundInWiki_filepath));
            }  catch(IOException e) {
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

                            if (widMidMap.containsKey(Integer.toString(page.getPageId()))) {
                                wid = Integer.toString(page.getPageId());
                                mid = widMidMap.get(wid);
                                widMidMap_FoundInWiki.put(wid, mid);
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
            System.out.println(" [#] Number of wiki ids in mid.names.wid : " + KB.widMidMap.keySet().size());
            System.out.println(" [#] Number of pages found in wiki : " + widMidMap_FoundInWiki.keySet().size());
        }
    }


}
