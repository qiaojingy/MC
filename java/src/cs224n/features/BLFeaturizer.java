package cs224n.features;

import cs224n.MC.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.*;

import java.util.*;

public class BLFeaturizer implements Featurizer {

	private static final String FEATURE_NAME = "BLFeature";
	private static final int FEATURE_DIM = 1;

	public int getDim() {
		return FEATURE_DIM;
	}

	public String getName() {
		return FEATURE_NAME;
	}

	@Override
	public void initialize() {}

	@Override
	public List<FeatureValue> featurize(Passage p, int w, Question q, int a) {
		List<String> answer = new ArrayList<String>();
		for (CoreLabel token : q.getOptionTokens(a)) {
			answer.add(token.get(CoreAnnotations.LemmaAnnotation.class));
		}

		CoreMap sentence = p.getSentence(w);
		List<String> sentenceLemmaStrings = new ArrayList<String>();
		for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
			sentenceLemmaStrings.add(token.get(CoreAnnotations.LemmaAnnotation.class));
		}

		List<String> Q = new ArrayList<String>();
		for (CoreLabel token : q.getStemTokens()) {
			Q.add(token.get(CoreAnnotations.LemmaAnnotation.class));
		}
		// Calculate S = A_i U Q
		List<String> S = new ArrayList<String>();
		S.addAll(Q);
		S.addAll(answer);
		// Calculate sw_i
		double score = 0;
		Counter<String> LemmaIC = p.getLemmaIC();
		for (String Lemma : sentenceLemmaStrings) {
			if (S.contains(Lemma)) {
				score += LemmaIC.getCount(Lemma);
			}
		}

		List<FeatureValue> features = new ArrayList<FeatureValue>();
		if (q.isNegation()) score = -score;
		features.add(new FeatureValue(FEATURE_NAME, score));
		return features;
	}

}
