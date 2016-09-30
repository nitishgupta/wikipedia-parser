package edu.illinois.cs.cogcomp.parser;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import info.bliki.wiki.dump.WikiArticle;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class WikiParserMain implements Serializable {
    static Set<String> pageIds;
    String mid_name_pageID_filepath;
    static String serializationDirectory;
    static Integer numberDocs = 0;



    public WikiParserMain(String mid_name_pageID_filepath) throws Exception {
        this.mid_name_pageID_filepath = mid_name_pageID_filepath;
        serializationDirectory = "/save/ngupta19/wikipedia/serialization/";
        this.pageIds = new HashSet<String>();
        this.populatePageIds();

    }

    public void populatePageIds() throws Exception {
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

        if (pageIds.contains(page.getId())) {
            System.out.println(page.getTitle());
            String pageId = page.getId();
            String serializationPath = serializationDirectory + pageId;
            WikiDoc doc = new WikiDoc(page, meta, ta);
            Utilities.serializeObject(doc, serializationPath);
            numberDocs++;
            System.out.println("\nNumber of docs stored = " + numberDocs);
            return false;

        }
        if (numberDocs == pageIds.size())
            return true;

        return false;
    }




    public static void main(String [] args) throws Exception {
        System.setProperty("jdk.xml.totalEntitySizeLimit", "500000000");
        String mid_name_pageID_filepath = "/save/ngupta19/freebase/mid.names.wiki_en_id";
        WikiParserMain wikiparser = new WikiParserMain(mid_name_pageID_filepath);
        WikiParserMain.serializationDirectory = "/save/ngupta19/wikipedia/serialization/";

        WikiParser.parseWiki();
    }
}
