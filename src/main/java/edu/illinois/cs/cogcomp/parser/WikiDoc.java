package edu.illinois.cs.cogcomp.parser;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.WikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import info.bliki.wiki.dump.WikiArticle;

import javax.xml.soap.Text;
import java.io.Serializable;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class WikiDoc implements Serializable{
    static final long serialVersionUID = 1L;
    
    WikiArticle page;
    PageMeta meta;
    TextAnnotation ta;

    public WikiDoc(WikiArticle page, PageMeta meta, TextAnnotation ta) {
        this.page = page;
        this.meta = meta;
        this.ta = ta;
    }
}
