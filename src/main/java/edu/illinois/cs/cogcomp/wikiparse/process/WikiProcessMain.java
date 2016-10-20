package edu.illinois.cs.cogcomp.wikiparse.process;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.wikiparse.parser.WikiDoc;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;

import java.io.*;
import java.util.*;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class WikiProcessMain {
    /*
     * Reads WikiDoc objects (contains one wiki article per object), process them and output in the required format.
     */

    public static String serialized_wikiDocsDir = "/save/ngupta19/wikipedia/serialized_full/";
    public static String output_plaintext_wikiMentionsDir = "/save/ngupta19/wikipedia/wiki_pruned_types/mentions/";
    public static String output_plaintext_wikiDocsDir = "/save/ngupta19/wikipedia/wiki_pruned_types/docs_links/";
    //public static String output_plaintext_wikiDocsDir = "/save/ngupta19/wikipedia/mid.wiki_id.title.sentences.links/";
    public static String mid_names_wikiId_file = "/save/ngupta19/freebase/types_pruned/mid.names.wiki_id_en";
    public static String mid_alias_names_file = "/save/ngupta19/freebase/types_pruned/mid.alias.names";
    public static Map<String, List<String>> mid_aliasNames;
    public static Map<String, String> wikiId_mid;
    public static int name_length_threshold = 100;

    public static String[] wikiDoc_filenames;

    public WikiProcessMain() throws Exception {
        // Input: Folder containing serialized wikiDocs
        // Output: Filenames of all serialized docs in the folder
        System.out.println("[#] Initializing Wikipedia Processor ...");
        System.out.println("[#] Reading serialized docs from " + serialized_wikiDocsDir);
//        File wikiDocsDir_file = new File(serialized_wikiDocsDir);
//        File[] files = wikiDocsDir_file.listFiles();
//        wikiDoc_filenames = new String[files.length];
//        System.out.println("[#] Number of files : " + files.length);
//        for (int i = 0; i < files.length; i++) {
//            wikiDoc_filenames[i] = files[i].getName();
//            if (i % 10000 == 0)
//                System.out.print(i + ", .., ");
//        }
//        System.out.println("\n[#] Num of Wiki Articles in Wikipedia : " + wikiDoc_filenames.length);

        mid_aliasNames = make_MIDNamesMap(mid_alias_names_file);
        wikiId_mid = make_WikiIDMIDMap(mid_names_wikiId_file);
    }

    public Map<String, List<String>> make_MIDNamesMap(String mid_alias_names_file) throws Exception {
        /*
         * mid_alias_names_file : contains mid \t name . All aliases and names for mid.
         */
        System.out.println(" [#] Generating mid, list of names/alias map ... ");
        Map<String, List<String>> mid_aliasNames = new HashMap<String, List<String>>();
        BufferedReader bwr = new BufferedReader(new FileReader(mid_alias_names_file));
        String line = bwr.readLine();
        while (line != null) {
            String[] mid_name = line.split("\t");
            String mid = mid_name[0].trim();
            String name = mid_name[1].trim();
            if (name.length() <= name_length_threshold) {
                if (!mid_aliasNames.containsKey(mid))
                    mid_aliasNames.put(mid, new ArrayList<String>());
                mid_aliasNames.get(mid).add(name);
            }
            line = bwr.readLine();
        }
        System.out.println(" [#] Total mids read : " + mid_aliasNames.keySet().size());
        return mid_aliasNames;
    }

    public Map<String, String> make_WikiIDMIDMap(String mid_names_wikiId_file) throws Exception {
        /*
         * mid_names_wikiId_file: contains mid \t name \t wikiId . Name is the freebase type.object.name
         */
        System.out.println(" [#] Generating wiki id -> mid map ... ");
        Map<String, String> wikiId_MID = new HashMap<String, String>();
        BufferedReader bwr = new BufferedReader(new FileReader(mid_names_wikiId_file));
        String line = bwr.readLine();
        while (line != null) {
            String[] mid_name = line.split("\t");
            String mid = mid_name[0].trim();
            String wikiId = mid_name[2].trim();
            if (!wikiId_MID.containsKey(wikiId))
                wikiId_MID.put(wikiId, mid);
            line = bwr.readLine();
        }
        System.out.println(" [#] Total WikiIds/ MIDs read : " + wikiId_MID.keySet().size());
        System.out.println(" [#] Num of pages written should be less than this.");
        return wikiId_MID;
    }

    public void processAllDocs() throws Exception {
        System.out.println(" [#] Processing serialized docs from : " + serialized_wikiDocsDir);
        System.out.println(" [#] Writing processed mentions in directory : " + output_plaintext_wikiMentionsDir);
        System.out.println(" [#] Writing processed docs (and links) in directory : " + output_plaintext_wikiDocsDir);
        int docs_done = 0;
        System.out.println(" [#] WikiIds to process : " + wikiId_mid.keySet().size());
        for (String wikiId : wikiId_mid.keySet()) {
            if(!new File(serialized_wikiDocsDir + wikiId).exists())
                continue;
            WikiDoc doc = Utilities.deserializeObject(serialized_wikiDocsDir + wikiId);
            docs_done++;
            /*
             * Get list of \t delimited wiki_id, name/alias, sentences, links for single article.
             * If freebase name/alias does not contain wikipedia title, it has been added to the list received
             */

            PlainDoc plaindoc = processFile(doc);
            if (plaindoc == null) {
                continue;
            }
            String doc_links = plaindoc.doc_links;
            String doc_filename = plaindoc.wikiId;
            BufferedWriter doc_writer = new BufferedWriter(new FileWriter(output_plaintext_wikiDocsDir + doc_filename));
            doc_writer.write(doc_links);
            doc_writer.flush();
            doc_writer.close();
            for (int i=0; i< plaindoc.mentions.size(); i++) {
                String mention = plaindoc.mentions.get(i);
                String mention_filename = plaindoc.wikiId + "_" + i;
                BufferedWriter mention_writer = new BufferedWriter(new FileWriter(output_plaintext_wikiMentionsDir + mention_filename));
                mention_writer.write(mention);
                mention_writer.flush();
                mention_writer.close();
            }

            if (docs_done % 5000 == 0)
                System.out.print(docs_done + ", ..., ");
        }
        System.out.println();
    }

    /*
     * Take a wikipedia doc. Create file with each line for one document.
     * Line : mid \t wiki_id \t wiki_Title \t tokens_in_a_sentence \t tokens_in_doc \t words_in_wiki_titles_of_outgoing_links
     */
    public PlainDoc processFile(WikiDoc doc) {
        int sentence_threshold = 20;
        int sentences_to_store = 5;

        if(doc.isRedirect()) {
            return null;
        }

        TextAnnotation ta = doc.getTextAnnotation();
        if (ta == null) {
            return null;
        }

        // Getting Wiki ID
        String wiki_id = doc.getID().trim();
        if (wiki_id.isEmpty() || !wikiId_mid.containsKey(wiki_id)) {
            return null;
        }

        // Getting mid
        String mid;
        if (wikiId_mid.containsKey(wiki_id))
            mid = wikiId_mid.get(wiki_id);
        else
            return null;

        // Getting Wiki Title - delimited by space instead of _
        String title = doc.getTitle().trim();
        if (title.isEmpty() || title.length() > name_length_threshold) {
            return null;
        }

        // Making separate local sentences for entity and complete doc
        // List of sentences contain the first 'sentences_to_store'
        StringBuffer doc_complete = new StringBuffer();
        List<String> local_sentences = new ArrayList<String>();
        View sentences_view = ta.getView(ViewNames.SENTENCE);
        if (sentences_view.getConstituents().size() < sentence_threshold) {
            return null;
        }

        int sen_written = 0;
        for (Constituent c : sentences_view) {
            if (sen_written < sentences_to_store) {
                local_sentences.add(c.getSurfaceForm().replaceAll("\\s+", " ").trim() + " " + "<eos_word>");
                sen_written++;
            }
            doc_complete.append(c.getSurfaceForm().replaceAll("\\s+", " ").trim() + " " + "<eos_word>" + " ");
        }
        String doc_string = doc_complete.toString().trim();

        if (doc_string.isEmpty()) {
            return null;
        }

        // Out Link Wiki Title Delimited by Space
        StringBuffer outLinks = new StringBuffer();
        if (doc.getLinks() != null) {
            for (String link : doc.getLinks()) {
                outLinks.append(link + " ");
            }
        } else {
            outLinks.append("<UNK>");
        }
        String outLinks_string = outLinks.toString().replaceAll("\\s+", " ").trim();
        if (outLinks_string.isEmpty()) {
            return null;
        }


        // Making single string for doc and links
        StringBuffer doc_links = new StringBuffer();
        doc_links.append(doc_string);
        doc_links.append("\t");
        doc_links.append(outLinks_string);
        String doc_links_string = doc_links.toString();


        // toWrite_stringList contains multiple copy of the article with different aliases //
        List<String> mentions = new ArrayList<String>();

        List<String> names_aliases = null;
        if (mid_aliasNames.containsKey(mid))
            names_aliases  = mid_aliasNames.get(mid);

        // If aliases found, make multiple copies, else make one with Wikipedia article title
        for (String sentence : local_sentences) {
            if (names_aliases != null) {
                // If freebase name/aliases do not contain Wikipedia title, add it.
                if (!names_aliases.contains(title))
                    names_aliases.add(title);
                for (String name_alias : names_aliases) {
                    StringBuffer toWrite = new StringBuffer();
                    toWrite.append(mid);
                    toWrite.append("\t");
                    toWrite.append(wiki_id);
                    toWrite.append("\t");
                    toWrite.append(name_alias);
                    toWrite.append("\t");
                    toWrite.append(sentence);
                    toWrite.append("\t");
                    toWrite.append(wiki_id);

                    mentions.add(toWrite.toString());
                }
            } else {
                StringBuffer toWrite = new StringBuffer();
                toWrite.append(mid);
                toWrite.append("\t");
                toWrite.append(wiki_id);
                toWrite.append("\t");
                toWrite.append(title);
                toWrite.append("\t");
                toWrite.append(sentence);
                toWrite.append("\t");
                toWrite.append(wiki_id);

                mentions.add(toWrite.toString());
            }
        }


        PlainDoc plaindoc = new PlainDoc(mid, wiki_id, doc_links_string, mentions);
        return plaindoc;
    }

    public static void main(String [] args) throws Exception {
        WikiProcessMain wiki_processor = new WikiProcessMain();
        wiki_processor.processAllDocs();
    }






}
