package cs224n.features;

import cs224n.MC.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.*;

import java.util.*;

public class BFeaturizer implements Featurizer {

	private static final String FEATURE_NAME = "B";

	@Override
	public void initialize() {}

	@Override
	public List<FeatureValue> featurize(Passage p, int w, Question q, List<String> a) {
		CoreMap sentence = p.getSentence(w);
		List<String> sentenceTokenStrings = new ArrayList<String>();
		for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
			sentenceTokenStrings.add(token.value());
		}

		List<String> Q = q.getStemTokenStrings();
		// Calculate S = A_i U Q
		List<String> S = new ArrayList<String>();
		S.addAll(Q);
		S.addAll(a);
		// Calculate sw_i
		double score = 0;
		Counter<String> IC = p.getIC();
		for (String token : sentenceTokenStrings) {
			if (S.contains(token)) {
				score += IC.getCount(token);
			}
		}

		List<FeatureValue> features = new ArrayList<FeatureValue>();
		features.add(new FeatureValue(FEATURE_NAME, score));
		return features;
	}

}
