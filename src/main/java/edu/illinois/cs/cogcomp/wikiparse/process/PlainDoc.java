package edu.illinois.cs.cogcomp.wikiparse.process;

import java.util.List;

/**
 * Created by nitishgupta on 10/10/16.
 */
public class PlainDoc {
    public String wikiId;
    public String mid;
    public String doc_links;
    public List<String> mentions;

    public PlainDoc(String mid, String wikiId, String doc_links, List<String> mentions) {
        /*
         * mid, wikiID
         * doc_links : tokenized doc \t tokenized links
         * mentions : List of string, Each string mid \t wikiid \t mention_surface \t doc_wiki_id
         */
        this.mid = mid;
        this.wikiId = wikiId;
        this.doc_links = doc_links;
        this.mentions = mentions;
    }
}
