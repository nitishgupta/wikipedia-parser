package edu.illinois.cs.cogcomp.wikiparse.kb;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.WikiDB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nitishgupta on 10/22/16.
 */
public class KBProcessing {
    public static void writeWikipediaDocuments() {
        long startTime = System.currentTimeMillis();
        System.out.println("[#] Reading Wiki Pages text and out links ...");
        int num = 0;
        for (String wid : KB.widMidMap_FoundInWiki.keySet()) {
            Integer wiki_id = Integer.parseInt(wid);
            Page page = null;
            try {
                page = WikiDB.getPage(wiki_id);
            } catch (WikiTitleParsingException e ) {
                System.out.println("Cannot read Wiki page : " + wiki_id);
            }
            if (page == null) {
                System.out.println("Cannot read Wiki page : " + wiki_id);
                continue;
            }
            String text = page.getText();
            Set<Page> outLinks = page.getOutlinks();
            Set<String> outTitles = new HashSet<>();
            num++;
            for (Page p : outLinks) {
                try {
                    outTitles.add(page.getTitle().toString());
                } catch (WikiTitleParsingException e) {

                }
            }
            if (num % 50000 == 0)
                System.out.println(num + " ... ");
        }
        System.out.println();
        long estimatedTime = System.currentTimeMillis() - startTime;
        double tt = ((double)(estimatedTime)) / (1000.0 * 60.0);

        System.out.println("Total Time in reading text : " + tt + "  minutes");
        System.out.println("[#] Number of pages text read : " + num);



    }

    public static void main(String [] args) throws Exception {
        long startTime = System.currentTimeMillis();
        KB.widMidMap_FoundInWiki.keySet().size();
        long estimatedTime = System.currentTimeMillis() - startTime;
        double tt = ((double)(estimatedTime)) / (1000.0 * 60.0);

        System.out.println("Total Time : " + tt + "  minutes");
        writeWikipediaDocuments();
    }
}
