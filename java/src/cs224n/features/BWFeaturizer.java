package cs224n.features;

import cs224n.MC.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.*;

import java.util.*;

public class BWFeaturizer implements Featurizer {

	private static final String FEATURE_NAME = "BW";
	private static final int FEATURE_DIM = 2;
	private WordEmbeddingsDict dict; 

	public int getDim() {
		return this.FEATURE_DIM;
	}

	public BWFeaturizer(WordEmbeddingsDict dict) {
		this.dict = dict;
	}

	@Override
	public void initialize() {}

	@Override
	public List<FeatureValue> featurize(Passage p, int w, Question q, int a) {
		float[] passageFwp = p.getFwp(this.dict, w);
		float[] questionFap = q.getFap(this.dict, a);
		float[] passageFwm = p.getFwm(this.dict, w);
		float[] questionFam = q.getFam(this.dict, a);
		/*
		for (int i=0; i<30; i++) {
			System.out.println(passageFwm[i]);
		}
		System.out.println("***********");
		for (int i=0; i<30; i++) {
			System.out.println(questionFam[i]);
		}
		*/
		float fp = 0;
		float fm = 0;
		for (int i=0; i<passageFwp.length; i++) {
			fp = fp + passageFwp[i] * questionFap[i];
			fm = fm + passageFwm[i] * questionFam[i];
		}

		List<FeatureValue> features = new ArrayList<FeatureValue>();
		features.add(new FeatureValue(FEATURE_NAME+" ", fp));
		features.add(new FeatureValue(FEATURE_NAME+" ", fm));
		return features;
	}

}
