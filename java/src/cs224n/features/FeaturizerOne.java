package cs224n.features;

import cs224n.MC.*;

import java.util.*;

public interface FeaturizerOne {
	public void initialize(); 
	public int getDim();
	public String getName();
	public List<FeatureValue> featurize(Passage p, Question q, List<String> a); 
}
