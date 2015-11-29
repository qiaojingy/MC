package cs224n.features;

import cs224n.MC.*;

import edu.stanford.nlp.stats.*;

import java.util.*;

public class SlidingWindowFeaturizer implements FeaturizerOne {

	private static final String FEATURE_NAME = "SlidingWindow";

	@Override
	public void initialize() {}

	@Override
	public List<FeatureValue> featurize(Passage p, Question q, List<String> a) {
		
		List<String> passageTokenStrings = p.getTokenStrings();
		List<String> Q = q.getStemTokenStrings();
		// Calculate S = A_i U Q
		List<String> S = new ArrayList<String>();
		S.addAll(Q);
		S.addAll(a);
		// Calculate sw_i
		int S_size = S.size();
		double score = 0;
		double maxScore = 0;
		Counter<String> IC = p.getIC();
		for (int j=0; j<passageTokenStrings.size()-S.size()+1; j++) {
			if (j == 0) {
				for (int w=0; w<S_size; w++) {
					String token = passageTokenStrings.get(j+w);
					if (S.contains(token)) {
						score += IC.getCount(token);
					}
				}
				maxScore = score;
			}
			else {
				String token = passageTokenStrings.get(j-1);
				if (S.contains(token)){
					score -= IC.getCount(passageTokenStrings.get(j-1));
				}
				token = passageTokenStrings.get(j+S_size-1);
				if (S.contains(token)){
					score += IC.getCount(passageTokenStrings.get(j+S_size-1));
				}
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}

		List<FeatureValue> features = new ArrayList<FeatureValue>();
		features.add(new FeatureValue(FEATURE_NAME, maxScore));
		return features;
	}

}
