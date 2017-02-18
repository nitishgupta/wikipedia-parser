package edu.illinois.cs.cogcomp.wikiparse.jwpl;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.api.sweble.PlainTextConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.sweble.wikitext.engine.*;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.utils.HtmlPrinter;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class WikiDB {
	public static DatabaseConfiguration dbConfig = null;
	public static Wikipedia wiki = null;
	final static Pattern LINKS_PATTERN = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.MULTILINE);


	static {
		dbConfig = new DatabaseConfiguration();
		dbConfig.setHost("localhost");
		// Can also use "wiki" database
		dbConfig.setDatabase("newiki");
		dbConfig.setUser("root");
		dbConfig.setPassword("dickens");
		dbConfig.setLanguage(WikiConstants.Language.english);
		try {
			wiki = new Wikipedia(dbConfig);
		} catch (WikiInitializationException e) {
			e.printStackTrace();
		}
	}

	public static void countPages() {
		long startTime = System.currentTimeMillis();
		long rep_startTime = System.currentTimeMillis();
		PageIterator pages = new PageIterator(wiki, true, 50000); // XX pages in enwiki
		int num = 0;
		while (pages.hasNext()) {
			Page page = null;
			try {
				if ((page = pages.next()) != null) {
					if (page.isRedirect() || page.isDisambiguation() || page.isDiscussion() ||
							page.getTitle().getPlainTitle().startsWith("List of") ||
							page.getTitle().getPlainTitle().startsWith("Lists of")) {
						continue;
					}
					num++;
					String text = page.getPlainText();
					if (num % 50000 == 0) {
						long estimatedTime = System.currentTimeMillis() - rep_startTime;
						rep_startTime = System.currentTimeMillis();
						double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
						System.out.print(num + " (" + tt + "  mins).. ");
					}

				}
			} catch (WikiApiException e) {
				// e.printStackTrace();
				//System.err.println("some error");
			}
		}
		System.out.println();
		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
		System.out.println("Total Time : " + tt + "  minutes");
		System.out.println("Total Number of WikiPages = " + num);
	}


	public static String getPlainText(Page p) throws WikiApiException {
		AstVisitor v = new MyPlainTextConverter();
		return (String)v.go(p.getCompiledPage().getPage());
	}

	public static Page getPage(Integer wid) throws WikiTitleParsingException {
		if (!wiki.existsPage(wid))
			return null;
		Page page = null;
		try {
			page = wiki.getPage(wid);
		} catch (WikiApiException e) {
			return null;
		}
		if (page.isRedirect() || page.isDisambiguation() || page.isDiscussion() ||
						page.getTitle().getPlainTitle().startsWith("List of") ||
						page.getTitle().getPlainTitle().startsWith("Lists of")) {
			return null;
		} else {
			return page;
		}
	}

	public static Page getPage(String title) {
		Page page = null;
		try {
			//page = wiki.getPageByExactTitle(title);
			page = wiki.getPage(title);
			if (!page.isRedirect() && !page.isDisambiguation() && !page.isDiscussion() &&
							!page.getTitle().getPlainTitle().startsWith("List of") &&
							!page.getTitle().getPlainTitle().startsWith("Lists of")) {
				return page;
			}
		} catch (WikiApiException e) {
			return null;
		}

		return null;
	}

	public static Pair<StringBuilder,Map<Pair<Integer, Integer>, String>> getCleanText(String text) {
		char [] textarray = text.toCharArray();
		StringBuilder cleanText = new StringBuilder();
		boolean writing = true;
		int lenOrigText = text.length();
		int i = 0;
		Map<Pair<Integer, Integer>, String> offsets2Title = new HashMap<>();
		while (i < lenOrigText-1) {															// Going till last but one char to avoid segmentation fault.
			if (writing) {
				if ( !(textarray[i] == '[' && textarray[i+1] == '[') ) {					// Plain text, add char to builder
					cleanText.append(textarray[i]);
					i++;
				} else {														// Encountered first '['
					i = i+2;
					StringBuilder surfaceBarTitle = new StringBuilder();
					int locationOfBar = i;
					while ( !(textarray[i] == ']' && textarray[i+1] == ']') ) {
						if (textarray[i] == '|')
							locationOfBar = i - locationOfBar;
						surfaceBarTitle.append(textarray[i]);
						i++;
					}
					i = i+2;
					String surface = surfaceBarTitle.substring(0, locationOfBar);
					String Title = surfaceBarTitle.substring(locationOfBar+1, surfaceBarTitle.length());
					Pair<Integer, Integer> offsets = new Pair<>(cleanText.length(), cleanText.length()+surface.length());
					offsets2Title.put(offsets, Title);
					cleanText.append(surface);
				}
			}
		}
		cleanText.append(textarray[lenOrigText-1]);				// This was not processed in the while loop above.
		for (Map.Entry<Pair<Integer, Integer>, String> entry : offsets2Title.entrySet()) {
			int start = entry.getKey().getFirst();
			int end = entry.getKey().getSecond();
			String title = entry.getValue();
			System.out.println(cleanText.substring(start, end) + " : " + title);
		}
		return new Pair<StringBuilder,Map<Pair<Integer, Integer>, String>>(cleanText, offsets2Title);
	}

	public static void main(String[] args) throws Exception {
		//Page page = wiki.getPage("Federal government of the United States");
		//Iterator<Integer> pages = wiki.getPageIds().iterator();
		long startTime = System.currentTimeMillis();

		String wikiTitle = "yoshua bengio";

		System.out.println("Yes!");
		Page page = WikiDB.wiki.getPage(wikiTitle);
		System.out.println("Given Title : " + wikiTitle + " WikiStyle Title : " + page.getTitle().getWikiStyleTitle() +
											 " Title : " + page.getTitle().getPlainTitle());




		//String testText = "Google Brain is a [[deep learning|deep_learning]] research project at [[Google|Goooogle]].";
		//WikiDB.getCleanText(plainTextModified);

		//WikiDB.countPages();

		System.exit(1);
	}
}
