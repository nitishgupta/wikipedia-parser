package edu.illinois.cs.cogcomp.wikiparse.wikiextractparser;

/**
 * Created by nitishgupta on 3/19/17.
 */
public class Mention {
	String mid;
	String wid;
	String wikiT;
	String surface;
	String sentence;
	String types;
	String coherence_mentions;
	int startTokenidx;
	int endTokenidx;

	public Mention(String mid, String wid, String wikiT, String surface, String sentence, String types,
								 String coherence_mentions, int start, int end) {
		this.mid = mid;
		this.wid = wid;
		this.wikiT = wikiT;
		this.surface = surface;
		this.sentence = sentence;
		this.types = types;
		this.coherence_mentions = coherence_mentions;
		this.startTokenidx = start;
		this.endTokenidx = end;
	}

	public void updateCoherence(String coherence_mentions) {
		this.coherence_mentions = coherence_mentions;
	}

	public String toString() {
		StringBuilder MentionText = new StringBuilder();
		MentionText.append(mid).append("\t");
		MentionText.append(wid).append("\t");
		MentionText.append(wikiT).append("\t");
		MentionText.append(Integer.toString(startTokenidx)).append("\t");
		MentionText.append(Integer.toString(endTokenidx)).append("\t");
		MentionText.append(surface).append("\t");
		MentionText.append(sentence).append("\t");
		MentionText.append(types).append("\t");
		MentionText.append(coherence_mentions).append("\n");
		return MentionText.toString();
	}
}
