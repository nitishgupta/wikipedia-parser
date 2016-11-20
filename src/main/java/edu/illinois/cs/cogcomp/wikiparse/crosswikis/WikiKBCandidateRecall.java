package edu.illinois.cs.cogcomp.wikiparse.crosswikis;

import edu.illinois.cs.cogcomp.wikiparse.kb.KB;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by nitishgupta on 11/18/16.
 */
public class WikiKBCandidateRecall {

	public static void KBFreebaseNameCandidateRecall() {
		Set<String> widsInKB = KB.wid2WikiTitle.keySet();
		int recallAt1 = 0;
		int recallAt30 = 0;
		int nameNotFound = 0;
		int nameFoundWIDNotFound = 0;

		System.out.println("Calculating KB Candidate Gen. Recall using Crosswikis ... ");

		for (String wid : widsInKB) {
			String mid = KB.wid2mid.get(wid);
			String name = KB.wid2FBName.get(mid);
			Map<String, Double> candidates = CrossWikis.getNormalizedCandidates(name);
			if (candidates == null) {
				nameNotFound ++;
			} else {
				Iterator<String> cand_wids = candidates.keySet().iterator();
				int candidateschecked = 0;
				while (cand_wids.hasNext()) {
					if (candidateschecked == 30) {
						nameFoundWIDNotFound++;
						break;
					}
					String can_wid = cand_wids.next();
					if (can_wid.equals(wid) && candidateschecked == 0) {
						recallAt1++;
					}
					if (can_wid.equals(wid) && candidateschecked != 0) {
						recallAt30++;
					}
					candidateschecked++;
				}
			}
		}

		System.out.println("Number of names not in crosswikis : " + nameNotFound);
		System.out.println("Number of names found but WID not found in 30 : " + nameFoundWIDNotFound);
		System.out.println("Recal @ 1 :" + recallAt1);
		System.out.println("Recal @ 30 :" + recallAt30);
	}

	public static void main(String [] args) {
		KBFreebaseNameCandidateRecall();
	}




}
