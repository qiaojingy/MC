package cs224n.features;

import cs224n.MC.*;

import java.util.*;

public interface Featurizer {
	public void initialize(); 
	public List<FeatureValue> featurize(Passage p, int w, Question q, List<String> a); 
}