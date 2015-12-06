package cs224n.features;

import cs224n.MC.*;
import cs224n.util.*;

import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;

import java.util.*;
import java.lang.Math;

public class SyntacticFeaturizer implements Featurizer {

	private static final String FEATURE_NAME = "Syntactic";
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
	public List<FeatureValue> featurize(Passage p, int w, Question q, int a) {
		CoreMap sentence = p.getSentence(w);
		CoreMap statement = q.getStatement(a);
		List<String> sentenceDependencyList = Arrays.asList(sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).toList().split("\n"));
		for (int i=0; i<sentenceDependencyList.size(); i++) {
			String s = sentenceDependencyList.get(i);
			s = s.replaceAll("-[0-9]+\\)", ")");
			s = s.replaceAll("-[0-9]+, ", ", ");
			sentenceDependencyList.set(i, s);
		}
		//System.out.println(sentenceDependencyList);
		List<String> statementDependencyList = Arrays.asList(statement.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).toList().split("\n"));
		for (int i=0; i<statementDependencyList.size(); i++) {
			String s = statementDependencyList.get(i);
			s = s.replaceAll("-[0-9]+\\)", ")");
			s = s.replaceAll("-[0-9]+, ", ", ");
			statementDependencyList.set(i, s);
		}
		//System.out.println(statementDependencyList);


		List<FeatureValue> features = new ArrayList<FeatureValue>();

		// Feature One
		List<String> commonDependencyList = new ArrayList<String>(statementDependencyList);
		commonDependencyList.retainAll(sentenceDependencyList);
		//System.out.println(commonDependencyList);
		features.add(new FeatureValue(FEATURE_NAME+"_One", commonDependencyList.size()));


		return features;
	}

	


}
