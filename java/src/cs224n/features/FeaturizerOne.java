package cs224n.features;

import cs224n.MC.*;

import java.util.*;

public interface FeaturizerOne {
	public void initialize(); 
	public List<FeatureValue> featurize(Passage p, Question q, List<String> a); 
}
