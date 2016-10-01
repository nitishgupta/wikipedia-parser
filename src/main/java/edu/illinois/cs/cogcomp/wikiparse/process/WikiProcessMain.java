package edu.illinois.cs.cogcomp.wikiparse.process;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.wikiparse.parser.WikiDoc;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class WikiProcessMain {
    /*
     * Reads WikiDoc objects (contains one wiki article per object), process them and output in the required format.
     */

    public static String serialized_wikiDocsDir = "/save/ngupta19/wikipedia/serialized/";
    public static String[] wikiDoc_filenames;

    public WikiProcessMain() {
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
    }

    public void processAllDocs(String outputDir) throws Exception {
        int docs_done = 0;
        while (docs_done < wikiDoc_filenames.length) {
            String filename = wikiDoc_filenames[docs_done];
            WikiDoc doc = Utilities.deserializeObject(serialized_wikiDocsDir + filename);
            docs_done++;
            // Get \t delimited wiki_id, wiki_title, sentences, links
            String write_string = processFile(doc);
            if (write_string.equals("")) {
                continue;
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir + filename));
            writer.write(write_string);
            writer.flush();
            writer.close();

            if (docs_done % 5000 == 0)
                System.out.print(docs_done + ", ..., ");
        }
        System.out.println();
    }

    /*
     * Take a wikipedia doc. Create file with each line for one document.
     * Line : wiki_id \t wiki_Title \t tokens_in_first_few_sentences_sapce_separated \t words_in_wiki_titles_of_outgoing_links_space_separated
     */
    public String processFile(WikiDoc doc) {
        int sentences_to_store = 5;

        TextAnnotation ta = doc.getTextAnnotation();
        if (ta == null) {
            return "";
        }

        // Getting Wiki ID
        String id = doc.getID().trim();
        if (id.isEmpty()) {
            return "";
        }

        // Getting Wiki Title - delimited by space instead of _
        String title = doc.getTitle().trim();
        if (title.isEmpty()) {
            return "";
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
            return "";
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
            return "";
        }

		StringBuffer toWrite = new StringBuffer();
        toWrite.append(id);
        toWrite.append("\t");
        toWrite.append(title);
        toWrite.append("\t");
        toWrite.append(sentences_string);
        toWrite.append("\t");
        toWrite.append(outLinks_string);

        return toWrite.toString();
    }

    public static void main(String [] args) throws Exception {
        WikiProcessMain wiki_processor = new WikiProcessMain();
        wiki_processor.processAllDocs("/save/ngupta19/wikipedia/wiki_id.title.sentences.links/");
    }






}
