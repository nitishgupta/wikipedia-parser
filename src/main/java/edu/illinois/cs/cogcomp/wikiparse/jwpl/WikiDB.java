package edu.illinois.cs.cogcomp.wikiparse.jwpl;

import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by nitishgupta on 10/21/16.
 */
public class WikiDB {
	public static DatabaseConfiguration dbConfig = null;
	public static Wikipedia wiki = null;

	static {
		dbConfig = new DatabaseConfiguration();
		dbConfig.setHost("localhost");
		dbConfig.setDatabase("wiki");
		dbConfig.setUser("root");
		dbConfig.setPassword("dickens");
		dbConfig.setLanguage(WikiConstants.Language.english);
		try {
			wiki = new Wikipedia(dbConfig);
		} catch (WikiInitializationException e) {
			e.printStackTrace();
		}
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

	public static void main(String[] args) throws Exception {
		//Page page = wiki.getPage("Federal government of the United States");
		//Iterator<Integer> pages = wiki.getPageIds().iterator();
		long startTime = System.currentTimeMillis();

		PageIterator pages = new PageIterator(wiki, true, 50000);

		int num = 0;
		while (pages.hasNext()) {
			Page page = null;
			if ((page = pages.next()) != null) {
				if (page.isRedirect() && page.isDisambiguation() && page.isDiscussion() &&
								page.getTitle().getPlainTitle().startsWith("List of") &&
								page.getTitle().getPlainTitle().startsWith("Lists of")) {
					int a = 5;
				}
				num++;
			}
			if (num % 50000 == 0)
				System.out.print(num + " ... ");
		}
		System.out.println("\nNum of pages : " + num);

		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);

		System.out.println("Total Time : " + tt + "  minutes");


		System.exit(1);
	}
}
