package cs224n.features;

import cs224n.MC.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.ling.*;

import java.util.*;

public class SlidingWindowVFeaturizer implements FeaturizerOne {

	private static final String FEATURE_NAME = "SlidingWindowV";
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
		
		List<String> passageTokenStrings = p.getTokenStrings();
		List<CoreLabel> Q_ = q.getStemTokens();
		List<String> Q = new ArrayList<String>();
		//String elements[] = {"JJ", "NN", "NNP", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
		String elements[] = {"JJ", "NN", "NNP", "NNPS", "NNS", "VB", "VBG", "VBD"};
		Set vWords = new HashSet(Arrays.asList(elements));

		for (int i=0;; i++) {
			CoreLabel token = Q_.get(i);
			if (i == Q_.size()-1) {
				Q.add(token.value());
				break;
			}
			if (vWords.contains(token.get(CoreAnnotations.PartOfSpeechAnnotation.class))) Q.add(token.value());
			//Q.add(token.value());
		}
		// Calculate S = A_i U Q
		List<String> S = new ArrayList<String>();
		S.addAll(Q);
		S.addAll(a);
		// Calculate sw_i
		//int S_size = S.size();
		int S_size = Q_.size() + a.size();
		double score = 0;
		double maxScore = 0;
		Counter<String> IC = p.getIC();
		for (int j=0; j<passageTokenStrings.size()-S_size+1; j++) {
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
