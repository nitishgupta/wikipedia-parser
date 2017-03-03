package edu.illinois.cs.cogcomp.wikiparse.jwpl;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitishgupta on 2/6/17.
 */
public abstract class PageParseWorker implements Runnable{
		private final Page page;
		public static final String SWEBLE_CONFIG = "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml";
		public PageParseWorker(Page page)
		{
			this.page=page;
		}
		public CompiledPage getCompiledPage() throws WikiApiException
		{
			CompiledPage cp;
			try{
				SimpleWikiConfiguration config = new SimpleWikiConfiguration(SWEBLE_CONFIG);
				PageTitle pageTitle = PageTitle.make(config, page.getTitle().toString());
				PageId pageId = new PageId(pageTitle, -1);
				// Compile the retrieved page
				org.sweble.wikitext.engine.Compiler compiler = new Compiler(config);
				cp = compiler.postprocess(pageId, page.getText(), null);
			}catch(Exception e){
				throw new WikiApiException(e);
			}
			return cp;
		}
		public Object parsePage(AstVisitor v) throws WikiApiException
		{
			// Use the provided visitor to parse the page
			return v.go(getCompiledPage().getPage());
		}
		public void run() {
			try {
				String text = (String) parsePage(new MyPlainTextConverter());
				processAnnotation(text);
			} catch (WikiApiException e) {
				System.err.println("something bad happened in thread");
//            e.printStackTrace();
			}
		}
		/**
		 * Call back function for parsed annotation
		 * @param plaintext
		 */
		public abstract void processAnnotation(String plaintext);
}

