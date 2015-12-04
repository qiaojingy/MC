package cs224n.MCsystems;

import cs224n.MC.*;
import cs224n.util.*;
import cs224n.features.*;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;

import java.util.*;
import java.lang.Math;

public class FeatureBaseline implements MCSystem {
	@Override
	public void train(List<Task> tasks) {
	}

	public List<String> runMC(Task task) {
		// Read the passage
		Passage passage = task.getPassage();

		// Read the questions
		List<Question> questions = task.getQuestions();

		// Answers stores the answer for each question
		List<String> answers = new ArrayList<String>();
		
		// Iterate through each question and find answer
		for (Question question : questions) {
			// Get token lists
			List<List<String>> A = question.getOptionsTokenStrings();

			// Iterate throught options and calculate sw_i
			List<Double> sw = new ArrayList<Double>();
			for (List<String> a : A) {
				FeaturizerOne slidingWindowFeaturizer = new SlidingWindowFeaturizer();
				slidingWindowFeaturizer.featurize(passage, question, a);
				double scoreBaselineOne = slidingWindowFeaturizer.featurize(passage, question, a).get(0).getValue(); 
				FeaturizerOne distanceBasedFeaturizer = new DistanceBasedFeaturizer();
				double distancePunish = distanceBasedFeaturizer.featurize(passage, question, a).get(0).getValue();
				sw.add(scoreBaselineOne - distancePunish);
				//sw.add(scoreBaselineOne);
			}
			// Find largest sw and add answer
			double max = Integer.MIN_VALUE;
			int index = -1;
			for (int i=0; i<sw.size(); i++) {
				if (sw.get(i) > max) {
					max = sw.get(i);
					index = i;
				}
			}
			switch (index) {
				case 0:
					answers.add("A");
					break;
				case 1:
					answers.add("B");
					break;
				case 2:
					answers.add("C");
					break;
				case 3:
					answers.add("D");
					break;
				default:
					System.out.print("sw vector: ");
					System.out.println(sw);
					System.out.println("Answer Number Error"); 
					break;
			}
		}
		//System.out.println(answers);
		return answers;
	}
	
}
