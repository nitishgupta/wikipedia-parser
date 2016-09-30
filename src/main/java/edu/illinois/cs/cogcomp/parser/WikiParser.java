package edu.illinois.cs.cogcomp.parser;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.DumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.WikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.dump.WikiXMLParser;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by nitishgupta on 9/29/16.
 */
public class WikiParser {
    public static String basepath = "/save/ngupta19/wikipedia_dump/";
    public static String basename = "enwiki-20150805-pages-articles";
    public static String dumpExtension = ".xml.bz2";
    public static String bz2DumpLocation = basepath + basename + dumpExtension;


    public WikiParser() {
        super();
    }

    /**
     * Parses the given Wikipedia XML dump file. User needs to instantiate the
     * parser for call backs
     *
     * @param file
     * @param parser
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     */
    public static void parseDump(String file, DumpFilter parser)
            throws UnsupportedEncodingException, FileNotFoundException,
            IOException, SAXException {
        new WikiXMLParser(file, parser).parse();
        parser.finishUp();
    }

    /**
     * Parses the given Wikipedia XML dump stream. User needs to instantiate the
     * parser for call backs
     */
    public static void parseDump(InputStream is, WikiDumpFilter parser)
            throws UnsupportedEncodingException, FileNotFoundException,
            IOException, SAXException {
        new WikiXMLParser(is, parser).parse();
        parser.finishUp();
    }

    public static void parseWiki() {
        try {
            System.out.println("Started dump parsing");
            WikiDumpFilter filter = new WikiDumpFilter() {
                @Override
                public void processAnnotation(WikiArticle page, PageMeta meta,
                                              TextAnnotation ta) {
                    // Do anything you want to both annotations
                    if (meta.isDisambiguationPage() || meta.isRedirect() || meta.isStub())
                        return;
                    if (page.getTitle().isEmpty() || page.getText().isEmpty())
                        return;
                    if (page.getTitle().startsWith("List of") ||
                        page.getTitle().startsWith("Lists of"))
                        return;



                    boolean stop = WikiParserMain.filter(page, meta, ta);
                    if (stop) {
                        System.out.println("Finishing ....");
                        System.exit(1);
                    }

                    // get pageInfoBox
//					InfoBox infoBox = meta.getInfoBox();
//					if (!meta.noInfoBox()) {
//						System.out.println("PAGEINFOBOX:");
//						LinkedHashMap<String, String> fields = infoBox
//								.getInfoBoxFields();
//						for (String k : fields.keySet()) {
//							System.out.println(k + " " + fields.get(k));
//						}
//					}
//					System.out.println();

                    // Example for getting categories/disambiguation/redirects
                    // Note: the disambiguation classifier is broken for the
                    // current version
                    // User is encouraged to write their own extractor for these
                    // classifications
//					String name = (meta.isStub() ? "<stub>" : "")
//							+ page.getTitle();
//					if (meta.isRedirect()) {
//						System.out
//								.println(name + "=>" + meta.getRedirectText());
//					}

//					System.out.println("CATEGORIES:");
//					List<String> categories = meta.getCategories();
//					if (!categories.isEmpty())
//						System.out.println(name + "=>" + categories);
//					System.out.println();

                    // this is NOT wikified output. This is using wikipedia's
                    // annotation to create a TA
//					if (!meta.isRedirect()) {
//						System.out.println("WIKIPEDIA_TA VIEW:");
//						SpanLabelView wikipedia_view = (SpanLabelView) ta.getView(ViewNames.WIKIFIER);
//						for (Constituent cons : wikipedia_view) {
//							System.out.println("surface:" + cons + " "
//									+ "links to:" + cons.getLabel());
//						}
//						System.out.println();
//					}
                }

            };
            parseDump(bz2DumpLocation, filter);
            System.out.println(' ');
            System.out.println("Parsing done! Totalling "
                    + filter.getParsedPageCount() + " articles.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
