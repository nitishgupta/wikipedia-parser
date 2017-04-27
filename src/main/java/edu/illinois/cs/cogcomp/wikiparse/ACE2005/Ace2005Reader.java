package edu.illinois.cs.cogcomp.wikiparse.ACE2005;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACEEntityMention;
import edu.illinois.cs.cogcomp.wikiparse.kb.KB;
import edu.illinois.cs.cogcomp.wikiparse.util.io.FileUtils;
import edu.illinois.cs.cogcomp.wikiparse.wikiextractparser.Mention;

import java.util.*;

/**
 * Created by nitishgupta on 4/7/17.
 */
public class Ace2005Reader {
	public static String datasetPath = "/save/ngupta19/datasets/ACE-2005/data/English";
	public ACEReader acereader;
	public Ace2005AnnotationReader ace2005AnnotationReader;

	public static String outDir = "/save/ngupta19/datasets/ACE-2005/mentions/perDoc/";
	public static String testMentionsFile = "/save/ngupta19/datasets/ACE-2005/mentions/ace2005.test.mens";

	public Ace2005Reader() throws Exception {
		ace2005AnnotationReader = new Ace2005AnnotationReader();

		acereader =  new ACEReader(datasetPath, new String[] { "nw"}, false);
	}

	public StringBuffer getMentionsForADoc(TextAnnotation doc) {
		StringBuffer mentionsstring = new StringBuffer();
		// docID below matches with annotation
		String docId = doc.getId().split("/")[1].replace(".apf.xml", ".sgm");
		SpanLabelView entityView = (SpanLabelView) doc.getView(ViewNames.MENTION_ACE);
		Set<String> mentionSurfaces = new HashSet<>(); // This is the list for coherence. Surfaces are joined by _
		int relMensInDoc = 0;
		List<Mention> mentions = new ArrayList<>();
		for (Constituent mention : entityView.getConstituents()) {
			String docIDWOSGM = docId.replace(".sgm", "");
			String entityId = mention.getAttribute(ACEReader.EntityIDAttribute).replace(docIDWOSGM + "-", "");
			String entityType = mention.getAttribute(ACEReader.EntityMentionTypeAttribute);

			// Dont process non-NAM mentions
			if (!entityType.equals("NAM"))
				continue;

			int startChar = Integer.parseInt(mention.getAttribute(ACEReader.EntityHeadStartCharOffset));
			int endChar = Integer.parseInt(mention.getAttribute(ACEReader.EntityHeadEndCharOffset)) -  1;

			int startTokenInDoc = doc.getTokenIdFromCharacterOffset(startChar);
			int endTokenInDoc = doc.getTokenIdFromCharacterOffset(endChar);

			String keyForAnnotation = docId+"-"+entityId+"-"+ Integer.toString(startChar);
			if (ace2005AnnotationReader.mention2Wid.containsKey(keyForAnnotation)) {
				relMensInDoc += 1;

				Sentence sent = doc.getSentenceFromToken(startTokenInDoc);
				int sentStartToken = sent.getStartSpan();                        // Token location for sent start
				int surfaceSentStart = startTokenInDoc - sentStartToken;        // Token loc. in sent
				int surfaceSentEnd = endTokenInDoc - sentStartToken;            // Token loc. in sent

				int start_token_id = doc.getTokenIdFromCharacterOffset(startChar);
				Sentence s1 = doc.getSentenceFromToken(start_token_id);
				String[] tokens = sent.getTokens();
				String sentenceSurface = sent.getTokenizedText();

				// Surface
				StringBuilder surfaceBuider = new StringBuilder();
				for (int i = surfaceSentStart; i <= surfaceSentEnd; i++)
					surfaceBuider.append(tokens[i]).append(" ");
				String surface = surfaceBuider.toString().trim();
				mentionSurfaces.add(surface.replaceAll(" ", "_"));    // Adding mention surface to set of surfaces in doc for coherence

				// WID and WikiTitle
				String true_wikit = ace2005AnnotationReader.mention2Wid.get(keyForAnnotation);
				String wt_KB = KB.KBWikiTitle(true_wikit);
				assert (!wt_KB.equals(KB.NullWikiTitle));
				String wid = KB.wikiTitle2WID(wt_KB);
				String mid;
				if (KB.wid2mid.containsKey(wid)) {
					mid = KB.wid2mid.get(wid);
				} else {
					mid = "<unk_mid>";
				}
				StringBuilder typestxt = new StringBuilder();
				if (KB.mid2typelabels.containsKey(mid)) {
					Set<String> types = KB.mid2typelabels.get(mid);
					for (String t : types) {
						typestxt.append(t).append(" ");
					}
				} else {
					typestxt.append("<NULL_TYPES>");
				}
				Mention m = new Mention(mid, wid, wt_KB, surface, sentenceSurface, typestxt.toString().trim(),
								"", surfaceSentStart, surfaceSentEnd); // Mention wihout coherence

				mentions.add(m);
			}
		}
		StringBuilder coherence_mentions = new StringBuilder();		// Make coherence stringbuilder
		for (String mentionsurface : mentionSurfaces) {
			coherence_mentions.append(mentionsurface.trim()).append(" ");
		}
		String coherence_string = coherence_mentions.toString().trim();		// Make coherence mentions string
		for (Mention m : mentions) {		// Add coherence mentions to all mentions of this doc
			m.updateCoherence(coherence_string);
		}

		mentionsstring = new StringBuffer();
		for (Mention m : mentions) {
			mentionsstring.append(m.toString());
		}

		FileUtils.writeStringToFile(outDir + docId, mentionsstring.toString());

		System.out.println(docId + " : " + relMensInDoc);

		return mentionsstring;
	}

	public void readDocs() {
		int numDocs = 0;
		List<StringBuffer> mentionsForDocs = new ArrayList<>();
		while (acereader.hasNext()) {
			TextAnnotation doc = acereader.next();
			numDocs += 1;
			StringBuffer mentions = getMentionsForADoc(doc);
			mentionsForDocs.add(mentions);
		}

		Collections.shuffle(mentionsForDocs);

		int numTestDocs = 24;
		StringBuffer testmens = new StringBuffer();
		for (int i=0; i<numTestDocs; i++){
			testmens.append(mentionsForDocs.get(i));
		}

		FileUtils.writeStringToFile(testMentionsFile, testmens.toString());
		System.out.println(numDocs);
	}

	public static void main(String [] args) throws Exception {
		Ace2005Reader ace2005Reader = new Ace2005Reader();
		ace2005Reader.readDocs();


	}




}
