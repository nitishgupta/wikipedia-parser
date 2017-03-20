package edu.illinois.cs.cogcomp.wikiparse.datasets.aida;

import edu.illinois.cs.cogcomp.wikiparse.kb.KB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nitishgupta on 12/3/16.
 */
public class Document {

	public static class Doc {
		String docid;
		List<Sentence> sentences;
		String coherenceString;

		public Doc(String docid) {
			this.docid = docid;
			this.sentences = new ArrayList<>();
		}

		public void setCoherenceString(String coherencestring) {
			this.coherenceString = coherencestring;
		}
	}

	public static class Sentence {
		public List<String> tokens;
		public List<Mention> mentions;
		public String text;

		public Sentence () {
			this.tokens = new ArrayList<>();
			this.mentions = new ArrayList<>();
		}

		public void tokensToString() {
			StringBuilder t = new StringBuilder();
			for (String token : tokens) {
				t.append(token).append(" ");
			}
			this.text = t.toString().trim();
		}
	}

	public static class Mention {
		public String surface;
		public String widTitle;
		public String mid;
		public String wid;
		public int startToken;
		public int endToken;

		public Mention(String surface, String wT, String wid, String mid, int startToken) {
			this.surface = surface;
			this.widTitle = wT;
			this.mid = mid;
			this.wid = wid;
			this.startToken = startToken;
			int numTokensInSurface = this.surface.split(" ").length;
			this.endToken = startToken + numTokensInSurface - 1;
		}
	}


}
