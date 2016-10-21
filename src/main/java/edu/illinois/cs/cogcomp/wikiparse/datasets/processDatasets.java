package edu.illinois.cs.cogcomp.wikiparse.datasets;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikiparse.datasets.eval.MsnbcEvaluator;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by nitishgupta on 10/20/16.
 */
public class processDatasets {
    private static String labelDir;
    private static String textDir;
    private static String dataset = "ace";

    public static final String uiucPath = "/save/ngupta19/WikificationACL2011Data/";

    public static void main(String[] args) {

        switch (dataset) {
            case "msnbc":
                labelDir = uiucPath + "MSNBC/Problems/";
                textDir = uiucPath + "MSNBC/RawTextsSimpleChars/";
                runUiuc();
                break;
            case "ace":
                labelDir =
                        uiucPath + "ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/";
                textDir =
                        uiucPath + "ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/";
                runUiuc();
                break;
            default:
                System.out.println("No Such dataset : " + dataset);
        }
    }


    private static void runUiuc() {
        int numNotFound = 0, numNilGoldWpid = 0;
        int totalNonNullMentions = 0;
        for (String file : new File(labelDir).list()) {
            System.out.println(file);
            String doc = file;
            Map<Pair<Integer, Integer>, String> goldSet =
                    MsnbcEvaluator.readGoldFromWikifier(labelDir + doc, true);
            String text = FileUtils.getTextFromFile(textDir + doc, "Windows-1252");
            for (Pair<Integer, Integer> key : goldSet.keySet()) {
                /*
                 * After setting up JWPL find what pages are missing from our dataset.
                 */
                totalNonNullMentions++;
                String mention = text.substring(key.getFirst(), key.getSecond());
                System.out.println(mention + "\t" + goldSet.get(key));

            }
        }
        System.out.println("Non Null Mentions : " + totalNonNullMentions);
    }
}
