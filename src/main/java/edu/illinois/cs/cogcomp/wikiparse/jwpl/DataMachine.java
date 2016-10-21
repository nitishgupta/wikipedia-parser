package edu.illinois.cs.cogcomp.wikiparse.jwpl;

import de.tudarmstadt.ukp.wikipedia.datamachine.domain.JWPLDataMachine;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by nitishgupta on 10/20/16.
 */
public class DataMachine {
    public static void main(String [] args) throws Exception {
        System.setProperty("jdk.xml.totalEntitySizeLimit", "500000000");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        String[] arg = {"english", "Contents", "Disambiguation_pages", "/save/ngupta19/enwiki/20160501/"};
        JWPLDataMachine.main(arg);
    }
}
