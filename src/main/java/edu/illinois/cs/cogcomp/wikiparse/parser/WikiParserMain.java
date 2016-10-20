package edu.illinois.cs.cogcomp.wikiparse.parser;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import info.bliki.wiki.dump.WikiArticle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class WikiParserMain implements Serializable {
    static Set<String> pageIds;
    static String mid_name_pageID_filepath;
    static String serializationDirectory;
    static Integer numberDocs = 0;



    public WikiParserMain() throws Exception {
        this.mid_name_pageID_filepath = "/save/ngupta19/freebase/mid.names.wiki_id_en";
        serializationDirectory = "/save/ngupta19/wikipedia/serialized_full/";
        this.pageIds = new HashSet<String>();
        this.populatePageIds();

    }

    public void populatePageIds() throws Exception {
        System.out.println("Reading mid.names.wiki_en_id to create set of wiki ids to be parsed ...");
        BufferedReader br = new BufferedReader(new FileReader(this.mid_name_pageID_filepath));
        String line = br.readLine();
        while (line != null) {
            String[] l_split = line.trim().split("\t");
            String pageID = l_split[2];
            this.pageIds.add(pageID);
            line = br.readLine();
        }
        br.close();
        System.out.println("Number of page Ids read : " + this.pageIds.size());
    }

    public static boolean filter (WikiArticle page, PageMeta meta,
                               TextAnnotation ta) {
        // Other filters of disambiguation, redirect, stub, lists are placed in WikiParser
        //if (pageIds.contains(page.getId())) {
        String pageId = page.getId();
        String serializationPath = serializationDirectory + pageId;
        WikiDoc doc = new WikiDoc(page, meta, ta);
        Utilities.serializeObject(doc, serializationPath);
        numberDocs++;
        return false;

        //}
        //if (numberDocs == pageIds.size())
            //return true;

        //return false;
    }




    public static void main(String [] args) throws Exception {
        System.setProperty("jdk.xml.totalEntitySizeLimit", "500000000");
        WikiParserMain wikiparser = new WikiParserMain();
        WikiParser.parseWiki();
    }
}
