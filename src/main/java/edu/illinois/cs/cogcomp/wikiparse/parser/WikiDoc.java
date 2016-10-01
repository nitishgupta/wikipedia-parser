package edu.illinois.cs.cogcomp.wikiparse.parser;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.InfoBox;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import info.bliki.wiki.dump.WikiArticle;

import javax.xml.soap.Text;
import java.io.Serializable;
import java.util.List;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class WikiDoc implements Serializable{
    static final long serialVersionUID = 1L;

    TextAnnotation ta;
    // From WikiArticle
    private String id = null;
    private String title = null;
    private String text;
    private String timeStamp;

    // From PageMeta
    private String wikiText = null;
    private List<String> pageCats = null;
    private List<String> pageLinks = null;
    private List<String> pageHeadings = null;
    private List<String> citations = null;
    private String redirectString = null;
    private String redirectedTitle = null;
    private boolean redirect = false;
    private boolean stub = false;
    private boolean disambiguation = false;
    private String plainText = null;

    public WikiDoc(WikiArticle page, PageMeta meta, TextAnnotation ta) {
        this.ta = ta;
        // From WikiArticle
        this.id = page.getId();
        this.title = page.getTitle();
        this.text = page.getText();
        this.timeStamp = page.getTimeStamp();

        //From PageMeta
        try {
            this.wikiText = page.getText();
        } catch (NullPointerException e) {

        }
        try {
            this.pageCats = meta.getCategories();
        } catch (NullPointerException e) {

        }
        try {
            this.pageLinks = meta.getLinks();
        } catch (NullPointerException e) {

        }
        try {
            this.pageHeadings = meta.getHeadings();
        } catch (NullPointerException e) {

        }
        try {
            this.citations = meta.getCitations();
        } catch (NullPointerException e) {

        }
        try {
            this.redirectString = meta.getRedirectText();
        } catch (NullPointerException e) {

        }
        try {
            this.plainText = meta.getPlainText();
        } catch (NullPointerException e) {

        }
        try {
            this.redirectedTitle = meta.getRedirectedTitle();
        } catch (NullPointerException e) {

        }
        try {
            this.redirect = meta.isRedirect();
        } catch (NullPointerException e) {

        }
        try {
            this.stub = meta.isStub();
        } catch (NullPointerException e) {

        }
        try {
            this.disambiguation = meta.isDisambiguationPage();
        } catch (NullPointerException e) {

        }


    }

    public TextAnnotation getTextAnnotation() { return ta; }

    public String getID(){ return this.id; }

    public String getTitle(){ return this.title; }

    public String getText(){ return this.text; }

    public String getTimeStamp(){ return this.timeStamp; }

    public String getWikiText(){ return this.wikiText; }

    public List<String> getCategories(){ return this.pageCats; }

    public List<String> getLinks(){ return this.pageLinks; }

    public List<String> getPageHeadings(){ return this.pageHeadings; }

    public List<String> getCitations(){ return this.citations; }

    public String getRedirectString(){ return this.redirectString; }

    public String getRedirectedTitle(){ return this.redirectedTitle; }

    public boolean isRedirect(){ return this.redirect; }

    public boolean isDisambiguation(){ return this.disambiguation; }

    public boolean isStub(){ return this.stub; }

    public String getPlainText(){ return this.plainText; }

}
