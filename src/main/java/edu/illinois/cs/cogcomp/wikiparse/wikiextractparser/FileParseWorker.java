package edu.illinois.cs.cogcomp.wikiparse.wikiextractparser;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.wikiparse.jwpl.MyPlainTextConverter;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.Utilities;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nitishgupta on 2/10/17.
 */
public class FileParseWorker implements Runnable {
	public static TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(false));
	public String infile;
	public String outfile;
	public Logger logger;

	public FileParseWorker(String infile, String outfile, Logger logger) {
		this.logger = logger;
		this.infile = infile;
		this.outfile = outfile;
	}

	public Pair<StringBuilder, Map<Pair<Integer, Integer>, String>> _cleanDocText(String markedupText) {
		/**
		 * Takes text marked with <a href="url">surface</a> and returns
		 * Returns:
		 * 	StringBuilder : cleanText with no markups.
		 * 	Map<<start, end>, Title> : Start (inc) end(exc) char offsets for surface and their resp. marked titles
		 */
		StringBuilder cleanText = new StringBuilder();
		Map<Pair<Integer, Integer>, String> offsets2Title = new HashMap<>();
		Pattern linkPattern = Pattern.compile("(<a[^>]+>.+?</a>)",  Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
		Matcher matcher = linkPattern.matcher(markedupText);
		int len = markedupText.length();
		int oldStart = 0;
		int start = 0;
		int end = 0;

		List<Integer[]> startEnds = new ArrayList<>();
		while (matcher.find()) {
			start = matcher.start();
			cleanText.append(markedupText.substring(oldStart, start));
			String[] urlSurface = this._cleanHyperLink(matcher.group());
			if (urlSurface[0] != null) {
				offsets2Title.put(new Pair<Integer, Integer>(cleanText.length(), cleanText.length()+urlSurface[1].length()),
								urlSurface[0]);
			}
			cleanText.append(urlSurface[1]);
			oldStart = matcher.end();
		}
		if (oldStart < len)
			cleanText.append(markedupText.substring(oldStart, len));

		return new Pair<StringBuilder,Map<Pair<Integer, Integer>, String>>(cleanText, offsets2Title);
	}

	private String[] _cleanHyperLink(String string) {
		/**
		 * Input : <a href="Grand%20Slam%20%28tennis%29">Grand Slam</a>
		 * Output : String[] = [decode("Grand%20Slam%20%28tennis%29"), "Grand Slam"]
		 *
		 * If Url cannot be parsed, then output String[0] = null
		 */

		int len = string.length();
		String urlCharCharSurface = string.substring(9, len - 4);			// Returns : url">surface
		String[] urlSurface = urlCharCharSurface.split("\">");
		assert (urlSurface.length == 2);
		String decodedUrl = new String();
		try {
			decodedUrl = Utilities.decodeURL(urlSurface[0]);
		} catch (Exception e) {
			logger.severe("URL Parsing failed : " + urlSurface[0]);
		}
		urlSurface[0] = decodedUrl;
		return urlSurface;
	}

	private String getMentionsInDoc(String markedupDocText, String pageTitle) {
		Pair<StringBuilder, Map<Pair<Integer, Integer>, String>> cleanText2Offset = _cleanDocText(markedupDocText);
		String cleanText = cleanText2Offset.getFirst().toString();
		Map<Pair<Integer, Integer>, String> offsets2Title = cleanText2Offset.getSecond();

		int start = 0, end = 0;
		TextAnnotation ta = null;
		try {
			ta = tab.createTextAnnotation("", "", cleanText);
		} catch (Exception e) {
			logger.severe("TA Failed : " + pageTitle);
			return "";
		}

		StringBuilder MentionSamples = new StringBuilder();
		for (Map.Entry<Pair<Integer, Integer>, String> entry : offsets2Title.entrySet()) {
			try {
				start = entry.getKey().getFirst();	// Inclusive
				end = entry.getKey().getSecond() - 1 ;		// Inclusive after -1
				String title = entry.getValue().replace(" ", "_");
				String wikiTitle = KB.KBWikiTitle(title);
				if (KB.wikiTitle2Wid.containsKey(wikiTitle)) {
					String wid = KB.wikiTitle2Wid.get(wikiTitle);
					String mid = KB.wid2mid.get(wid);
					Set<String> types = KB.mid2typelabels.get(mid);
					int startTokenInDoc = ta.getTokenIdFromCharacterOffset(start);  // Token Location in Doc
					int endTokenInDoc = ta.getTokenIdFromCharacterOffset(end);      // Token Location in Doc

					Sentence sent = ta.getSentenceFromToken(startTokenInDoc);
					int sentStartToken = sent.getStartSpan();                        // Token location for sent start

					int surfaceSentStart = startTokenInDoc - sentStartToken;        // Token loc. in sent
					int surfaceSentEnd = endTokenInDoc - sentStartToken;            // Token loc. in sent

					if (surfaceSentEnd >= WikiExtractParser.sentenceLengthThreshold)
						continue;

					// Making surface
					String[] tokens = sent.getTokens();
					String sentenceSurface = "";
					if (tokens.length > WikiExtractParser.sentenceLengthThreshold) {
						StringBuilder strbldr = new StringBuilder();
						for (int i = 0; i < WikiExtractParser.sentenceLengthThreshold; i++) {
							strbldr.append(tokens[i]).append(" ");
						}
						sentenceSurface = strbldr.toString().trim();
					} else {
						sentenceSurface = sent.getTokenizedText();
					}
					StringBuilder surfaceBuider = new StringBuilder();
					for (int i = surfaceSentStart; i <= surfaceSentEnd; i++)
						surfaceBuider.append(tokens[i]).append(" ");
					String surface = surfaceBuider.toString().trim();

					// Making types string
					StringBuilder typestxt = new StringBuilder();
					for (String t : types) {
						typestxt.append(t).append(" ");
					}

					MentionSamples.append(mid).append("\t");
					MentionSamples.append(wid).append("\t");
					MentionSamples.append(wikiTitle).append("\t");
					MentionSamples.append(Integer.toString(surfaceSentStart)).append("\t");
					MentionSamples.append(Integer.toString(surfaceSentEnd)).append("\t");
					MentionSamples.append(surface).append("\t");
					MentionSamples.append(sentenceSurface).append("\t");
					MentionSamples.append(typestxt.toString().trim()).append("\n");
				}
			} catch (Exception e) {
				logger.warning("Mention Writing Failed : " + pageTitle);
			}
		}
		return MentionSamples.toString();
	}

	public Pair<List<String>,List<String>> breakDocs (String filename) {
		String text = FileUtils.readFileToString(filename);
		String [] docs = text.split("</doc>");
		List<String> docTitles = new ArrayList<>();
		List<String> docTexts = new ArrayList<>();
		int count = 0;
		for (String doc : docs) {
			if (!doc.trim().isEmpty()) {
				String [] lines = doc.trim().split("\n");
				assert (lines[0].startsWith("<doc id=")); // Line 1 should be <doc ... >
				// Extract Page Title from lines[0]
				String pageTitle = lines[0].split("title=\"")[1];
				pageTitle = pageTitle .substring(0,pageTitle.length()-2);
				if (pageTitle.startsWith("List of") || pageTitle.startsWith("Lists of"))
					continue;

				// Skip lines[1] as it is the title
				StringBuilder docText = new StringBuilder();
				for (int i=2; i<lines.length; i++) {
					if (!lines[i].trim().isEmpty()) {
						docText.append(lines[i]);
						docText.append("\n");
					}
				}
				String doctext = docText.toString().trim();
				if (!doctext.isEmpty()) {
					docTitles.add(pageTitle);
					docTexts.add(doctext);
					count++;
				}
			}
		}
		//System.out.println("Number of doccs : " + count);
		//System.out.println(docTitles);

		return new Pair<List<String>,List<String>>(docTitles, docTexts);
	}

	public int writeMentionsForFile(String inFilePath, String outFilepath) {
		/**
		 * Takes a file with multiple <doc ... >rawText</doc> and writes file with mentions in the documents.
		 * Independent function. Can be run in parallel. DO RUN IN PARALLEL
		 *
		 * inFilePath : File generated by python parser that contains multiple documents with text and link markups.
		 * outFilePath : File to be written with all mentions in all documents in the input file
		 *
		 * Returns: Number of mentions written in file
		 */

		File f = new File(inFilePath);
		assert(f.isFile() && f.exists());
		Pair<List<String>,List<String>> titlesAnddoctexts = breakDocs(inFilePath);
		List<String> pageTitles = titlesAnddoctexts.getFirst();
		List<String> rawDocTexts = titlesAnddoctexts.getSecond();
		StringBuilder Mentions = new StringBuilder();
		for (int i=0; i<rawDocTexts.size(); i++) {
			String title = pageTitles.get(i);
			String rawdoctext = rawDocTexts.get(i);
			//System.out.println(title);
			String mentions = getMentionsInDoc(rawdoctext, title);
			Mentions.append(mentions);
		}
		String MentionsWritten = Mentions.toString();
		FileUtils.writeStringToFile(outFilepath, Mentions.toString());
		logger.info("Mentions File Written : " + outFilepath);
		return MentionsWritten.split("\n").length;
	}


	public void run() {
		try {
			writeMentionsForFile(this.infile, this.outfile);
		} catch (Exception e) {
			logger.severe("File2Mentions failed : \nInFile : " + infile + "\nOutfile : " + outfile);
		}
	}

}
