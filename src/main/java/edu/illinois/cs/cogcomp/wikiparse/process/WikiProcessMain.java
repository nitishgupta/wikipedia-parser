package edu.illinois.cs.cogcomp.wikiparse.process;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.wikiparse.parser.WikiDoc;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class WikiProcessMain {
    /*
     * Reads WikiDoc objects (contains one wiki article per object), process them and output in the required format.
     */

    public static String serialized_wikiDocsDir = "/save/ngupta19/wikipedia/serialized/";
    public static String output_plaintext_wikiDocsDir = "/save/ngupta19/wikipedia/mid.wiki_id.title.sentences.links/";
    public static String mid_names_wikiId_file = "/save/ngupta19/freebase/mid.names.wiki_en_id";
    public static String mid_alias_names_file = "/save/ngupta19/freebase/entity.alias.names";
    public static Map<String, List<String>> mid_aliasNames;
    public static Map<String, String> wikiId_mid;

    public static String[] wikiDoc_filenames;

    public WikiProcessMain() throws Exception {
        // Input: Folder containing serialized wikiDocs
        // Output: Filenames of all serialized docs in the folder
        System.out.println("[#] Initializing Wikipedia Processor ...");
        File wikiDocsDir_file = new File(serialized_wikiDocsDir);
        File[] files = wikiDocsDir_file.listFiles();
        wikiDoc_filenames = new String[files.length];
        System.out.println("[#] Number of files : " + files.length);
        for (int i = 0; i < files.length; i++) {
            wikiDoc_filenames[i] = files[i].getName();
            if (i % 10000 == 0)
                System.out.print(i + ", .., ");
        }
        System.out.println("\n[#] Num of Wiki Articles : " + wikiDoc_filenames.length);

        mid_aliasNames = make_MIDNamesMap(mid_alias_names_file);
        wikiId_mid = make_WikiIDMIDMap(mid_names_wikiId_file);
    }

    public Map<String, List<String>> make_MIDNamesMap(String mid_alias_names_file) throws Exception {
        /*
         * mid_alias_names_file : contains mid \t name . All aliases and names for mid.
         */
        System.out.println("[#] Generating mid, list of names/alias map ... ");
        Map<String, List<String>> mid_aliasNames = new HashMap<String, List<String>>();
        BufferedReader bwr = new BufferedReader(new FileReader(mid_alias_names_file));
        String line = bwr.readLine();
        while (line != null) {
            String[] mid_name = line.split("\t");
            String mid = mid_name[0].trim();
            String name = mid_name[1].trim();
            if (!mid_aliasNames.containsKey(mid))
                mid_aliasNames.put(mid, new ArrayList<String>());
            mid_aliasNames.get(mid).add(name);
            line = bwr.readLine();
        }
        System.out.println("Total mids read : " + mid_aliasNames.keySet().size());
        return mid_aliasNames;
    }

    public Map<String, String> make_WikiIDMIDMap(String mid_names_wikiId_file) throws Exception {
        /*
         * mid_names_wikiId_file: contains mid \t name \t wikiId . Name is the freebase type.object.name
         */
        System.out.println("[#] Generating wiki id -> mid map ... ");
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
        System.out.println("Total WikiIds/MIDs read : " + wikiId_MID.keySet().size());
        return wikiId_MID;
    }

    public void processAllDocs(String outputDir) throws Exception {
        System.out.println("[#] Processing serialized docs from : " + serialized_wikiDocsDir);
        System.out.println("[#] Writing processed docs in directory : " + output_plaintext_wikiDocsDir);
        int docs_done = 0;
        while (docs_done < wikiDoc_filenames.length) {
            String filename = wikiDoc_filenames[docs_done];
            WikiDoc doc = Utilities.deserializeObject(serialized_wikiDocsDir + filename);
            docs_done++;
            /*
             * Get list of \t delimited wiki_id, name/alias, sentences, links for single article.
             * If freebase name/alias does not contain wikipedia title, it has been added to the list received
             */
            List<String> toWrite_stringList = processFile(doc);
            if (toWrite_stringList == null) {
                continue;
            }
            for (int i=0; i< toWrite_stringList.size(); i++) {
                String toWrite = toWrite_stringList.get(i);
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir + filename + "_" + i));
                writer.write(toWrite);
                writer.flush();
                writer.close();
            }

            if (docs_done % 5000 == 0)
                System.out.print(docs_done + ", ..., ");
        }
        System.out.println();
    }

    /*
     * Take a wikipedia doc. Create file with each line for one document.
     * Line : wiki_id \t wiki_Title \t tokens_in_first_few_sentences_sapce_separated \t words_in_wiki_titles_of_outgoing_links_space_separated
     */
    public List<String> processFile(WikiDoc doc) {
        int sentences_to_store = 5;

        TextAnnotation ta = doc.getTextAnnotation();
        if (ta == null) {
            return null;
        }

        // Getting Wiki ID
        String wiki_id = doc.getID().trim();
        if (wiki_id.isEmpty()) {
            return null;
        }

        String mid;
        if (wikiId_mid.containsKey(wiki_id))
            mid = wikiId_mid.get(wiki_id);
        else
            mid = "<unk_mid>";

        // Getting Wiki Title - delimited by space instead of _
        String title = doc.getTitle().trim();
        if (title.isEmpty()) {
            return null;
        }

        // Contains multiple tokenized sentences delimited by space.
        StringBuffer sentences = new StringBuffer();
        View sentence = ta.getView(ViewNames.SENTENCE);
        int sen_written = 0;
        for (Constituent c : sentence) {
            if (sen_written == sentences_to_store)
                break;
            sentences.append(c.getSurfaceForm().trim() + " ");
            sen_written++;
        }
        String sentences_string = sentences.toString().replaceAll("\\s+", " ").trim();

        if (sentences_string.isEmpty()) {
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


        // toWrite_stringList contains multiple copy of the article with different aliases //
        List<String> toWrite_stringList = new ArrayList<String>();

        List<String> names_aliases = null;
        if (mid_aliasNames.containsKey(mid))
            names_aliases  = mid_aliasNames.get(mid);

        // If aliases found, make multiple copies, else make one with Wikipedia article title
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
                toWrite.append(sentences_string);
                toWrite.append("\t");
                toWrite.append(outLinks_string);

                toWrite_stringList.add(toWrite.toString());
            }
        } else {
            StringBuffer toWrite = new StringBuffer();
            toWrite.append(mid);
            toWrite.append("\t");
            toWrite.append(wiki_id);
            toWrite.append("\t");
            toWrite.append(title);
            toWrite.append("\t");
            toWrite.append(sentences_string);
            toWrite.append("\t");
            toWrite.append(outLinks_string);

            toWrite_stringList.add(toWrite.toString());
        }

        return toWrite_stringList;
    }

    public static void main(String [] args) throws Exception {
        WikiProcessMain wiki_processor = new WikiProcessMain();
        wiki_processor.processAllDocs(wiki_processor.output_plaintext_wikiDocsDir);
    }






}
