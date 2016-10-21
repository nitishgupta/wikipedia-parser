package edu.illinois.cs.cogcomp.wikiparse.kb;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class KB {
    public static String mid_names_wid_filepath = "/save/ngupta19/freebase/types_pruned/mid.names.wiki_id_en";
    public static Map<String, Pair<String, String>> midNameWIDMap = null;
    public static Map<String, Pair<String, String>> widNameMIDMap = null;

    public static void makeMaps() throws Exception {
        int duplicateMIDs = 0, duplicateWIDs = 0;
        midNameWIDMap = new HashMap<>();
        widNameMIDMap = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(mid_names_wid_filepath));
        String line = br.readLine();
        while (line != null) {
            String[] midnamewid = line.trim().split("\t");
            assert midnamewid.length == 3;
            String mid = midnamewid[0].trim();
            String name = midnamewid[1].trim();
            String wid = midnamewid[2].trim();
            if (midNameWIDMap.containsKey(mid)) {
                duplicateMIDs++;
            } else {
                midNameWIDMap.put(mid, new Pair(name, wid));
            }

            if (widNameMIDMap.containsKey(wid)) {
                duplicateWIDs++;
            } else {
                widNameMIDMap.put(wid, new Pair(name, mid));
            }
            line = br.readLine();
        }
        br.close();
        System.out.println("[#] Number of mid duplicates : " + duplicateMIDs);
        System.out.println("[#] Number of wid duplicates : " + duplicateWIDs);
        System.out.println("[#] Number of mids : " + midNameWIDMap.keySet().size());
        System.out.println("[#] Number of wids : " + widNameMIDMap.keySet().size());
    }
}
