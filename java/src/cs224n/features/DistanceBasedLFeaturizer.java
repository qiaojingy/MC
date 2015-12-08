package cs224n.features;

import cs224n.MC.*;
import cs224n.util.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.ling.*;

import java.util.*;
import java.lang.Math;

public class DistanceBasedLFeaturizer implements FeaturizerOne {

	private static final String FEATURE_NAME = "DistanceBased";
	private static final int FEATURE_DIM = 1;

	public int getDim() {
		return this.FEATURE_DIM;
	}

	public String getName() {
		return FEATURE_NAME;
	}

	@Override
	public void initialize() {}

	@Override
	public List<FeatureValue> featurize(Passage p, Question q, List<String> a) {
		
		List<String> passageLemmaStrings = p.getLemmaStrings();
		List<String> Q = q.getStemLemmaStrings();
		//S_Q = (Q intersects PW) minus U
		Set<String> Q_set = new HashSet<String>(Q);
		Set<String> PW_set = new HashSet<String>(passageLemmaStrings);
		Set<String> SQ_set = new HashSet<String>(Q_set);
		SQ_set.retainAll(PW_set);
		StopWordsRemover.removeStopWords(SQ_set);

		//S_Ai = ((Ai intersects PW) minus Q) minus U
		List<String> S = new ArrayList<String>();
		for (int i=0; i<4; i++) {
			if (q.getOptionTokenStrings(i).equals(a)) {
				for (CoreLabel token : q.getOptionTokens(i)) S.add(token.get(CoreAnnotations.LemmaAnnotation.class));
			}
		}
		Set<String> Ai_set = new HashSet<String>(S);
		Set<String> SAi_set = new HashSet<String>(Ai_set);
		SAi_set.retainAll(PW_set);
		SAi_set.removeAll(Q_set);
		StopWordsRemover.removeStopWords(SAi_set);

		List<FeatureValue> features = new ArrayList<FeatureValue>();
		if (SQ_set.size() == 0 || SAi_set.size() == 0) {
			if (q.isNegation()) features.add(new FeatureValue(FEATURE_NAME, -1));
			else features.add(new FeatureValue(FEATURE_NAME, 1));
			return features;
		}
		else {
			Map<String, List<Integer>> lemmaPositions = p.getLemmaPositions();
			int min = Integer.MAX_VALUE;
			for (String qWord : SQ_set) {
				for (String aWord : SAi_set) {
					for (int i : lemmaPositions.get(qWord)) {
						for (int j : lemmaPositions.get(aWord)) {
							if (Math.abs(i-j) < min) min = Math.abs(i-j);
						}
					}
				}
			}
			if (q.isNegation()) features.add(new FeatureValue(FEATURE_NAME, -((double)(min+1))/(passageLemmaStrings.size()-1)));
			else features.add(new FeatureValue(FEATURE_NAME, ((double)(min+1))/(passageLemmaStrings.size()-1)));
			
			return features;
		}
	}

}


