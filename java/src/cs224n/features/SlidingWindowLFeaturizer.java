package cs224n.features;

import cs224n.MC.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.ling.*;

import java.util.*;

public class SlidingWindowLFeaturizer implements FeaturizerOne {

	private static final String FEATURE_NAME = "SlidingWindowL";
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
		// Calculate S = A_i U Q
		List<String> S = new ArrayList<String>();
		S.addAll(Q);
		for (int i=0; i<4; i++) {
			if (q.getOptionTokenStrings(i).equals(a)) {
				for (CoreLabel token : q.getOptionTokens(i)) S.add(token.get(CoreAnnotations.LemmaAnnotation.class));
			}
		}
		// Calculate sw_i
		int S_size = S.size();
		double score = 0;
		double maxScore = 0;
		Counter<String> lemmaIC = p.getLemmaIC();
		for (int j=0; j<passageLemmaStrings.size()-S.size()+1; j++) {
			if (j == 0) {
				for (int w=0; w<S_size; w++) {
					String token = passageLemmaStrings.get(j+w);
					if (S.contains(token)) {
						score += lemmaIC.getCount(token);
					}
				}
				maxScore = score;
			}
			else {
				String token = passageLemmaStrings.get(j-1);
				if (S.contains(token)){
					score -= lemmaIC.getCount(passageLemmaStrings.get(j-1));
				}
				token = passageLemmaStrings.get(j+S_size-1);
				if (S.contains(token)){
					score += lemmaIC.getCount(passageLemmaStrings.get(j+S_size-1));
				}
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}

		if (q.isNegation()) maxScore = -maxScore;
		List<FeatureValue> features = new ArrayList<FeatureValue>();
		features.add(new FeatureValue(FEATURE_NAME, maxScore));
		return features;
	}

}
