package cs224n.features;

import cs224n.MC.*;
import cs224n.util.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;

import java.util.*;
import java.lang.Math;

public class DFeaturizer implements Featurizer {

	private static final String FEATURE_NAME = "DFeature";
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
		List<String> answer = q.getOptionTokenStrings(a); 
		CoreMap sentence = p.getSentence(w);
		List<String> sentenceTokenStrings = new ArrayList<String>();
		for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
			sentenceTokenStrings.add(token.value());
		}

		List<String> Q = q.getStemTokenStrings();
		//S_Q = (Q intersects PW) minus U
		Set<String> Q_set = new HashSet<String>(Q);
		Set<String> PW_set = new HashSet<String>(sentenceTokenStrings);
		Set<String> SQ_set = new HashSet<String>(Q_set);
		SQ_set.retainAll(PW_set);
		StopWordsRemover.removeStopWords(SQ_set);

		//S_Ai = ((Ai intersects PW) minus Q) minus U
		Set<String> Ai_set = new HashSet<String>(answer);
		Set<String> SAi_set = new HashSet<String>(Ai_set);
		SAi_set.retainAll(PW_set);
		SAi_set.removeAll(Q_set);
		StopWordsRemover.removeStopWords(SAi_set);

		List<FeatureValue> features = new ArrayList<FeatureValue>();
		if (SQ_set.size() == 0 || SAi_set.size() == 0) {
			if (q.isNegation()) features.add(new FeatureValue(FEATURE_NAME, 1));
			else features.add(new FeatureValue(FEATURE_NAME, 1));
			return features;
		}
		else {
			Map<String, List<Integer>> tokenPositions = p.getTokenPositions();
			int min = Integer.MAX_VALUE;
			for (String qWord : SQ_set) {
				for (String aWord : SAi_set) {
					for (int i : tokenPositions.get(qWord)) {
						for (int j : tokenPositions.get(aWord)) {
							if (Math.abs(i-j) < min) min = Math.abs(i-j);
						}
					}
				}
			}
			if (q.isNegation()) features.add(new FeatureValue(FEATURE_NAME, -min/(sentenceTokenStrings.size()-1)));
			else features.add(new FeatureValue(FEATURE_NAME, min/(sentenceTokenStrings.size()-1)));
			return features;
		}
	}


}
