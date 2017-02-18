package edu.illinois.cs.cogcomp.wikiparse.jwpl;

import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitishgupta on 2/6/17.
 */
public class PageParser {
	public static DatabaseConfiguration dbConfig = null;
	public static Wikipedia wiki = null;
	private ThreadPoolExecutor parsing=null;
	/**
	 * Bounds the number concurrent executing thread to 1/2 of the cores
	 * available to the JVM. If more jobs are submitted than the allowed
	 * upperbound, the caller thread will be executing the job.
	 * @return a fixed thread pool with bounded job numbers
	 */
	public static ThreadPoolExecutor getBoundedThreadPool() {
		int coreCount = Runtime.getRuntime().availableProcessors();
		coreCount = Math.max(coreCount, 10);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
						coreCount, // Core count
						//coreCount / 2 + 1, // Core count
						coreCount, // Pool Max
						60, TimeUnit.SECONDS, // Thread keep alive time
						new ArrayBlockingQueue<Runnable>(coreCount),// Queue
						new ThreadPoolExecutor.CallerRunsPolicy()// Blocking mechanism
		);
		executor.allowCoreThreadTimeOut(true);
		return executor;
	}
	public PageParser(){
		parsing = getBoundedThreadPool();
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
	public void parse() {
		long startTime = System.currentTimeMillis();
		long rep_startTime = System.currentTimeMillis();
		PageIterator pages = new PageIterator(wiki, true, 50000); // 2 M pages in dewiki
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

					parsing.execute(new PageParseWorker(page) {
						@Override
						public void processAnnotation(String text) {
							return;
//							if (text.length()< 100)
//							{
//								System.err.println("too short");
//								return;
//							}
//							System.out.println(text+"\n"+"#-EOD-#");
						}
					});
					num++;
				}
			} catch (WikiApiException e) {
				// e.printStackTrace();
				System.err.println("some error");
			}
			if (num % 5000 == 0) {
				long estimatedTime = System.currentTimeMillis() - rep_startTime;
				rep_startTime = System.currentTimeMillis();
				double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
				System.out.print(num + " (" + tt + "  mins).. ");
			}
		}
		System.out.println();
		long estimatedTime = System.currentTimeMillis() - startTime;
		double tt = ((double) (estimatedTime)) / (1000.0 * 60.0);
		System.out.println("Total Time : " + tt + "  minutes");
		System.out.println("Total Number of WikiPages = " + num);
	}
	/**
	 * Waits for all pasring jobs to finish If not called, there might be pages
	 * still being parsed
	 */
	public void finishUp() {
		parsing.shutdown();
		try {
			parsing.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		PageParser parser = new PageParser();
		parser.parse();
		parser.finishUp();
	}
}