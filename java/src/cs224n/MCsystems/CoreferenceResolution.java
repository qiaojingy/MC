package cs224n.MCsystems;

import cs224n.MC.*;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;

import java.util.*;
import java.lang.Math;

public class CoreferenceResolution implements MCSystem {
	@Override
	public List<String> runMC(Task task) {
		// Read the questions
		List<Question> questions = task.getQuestions();
		List<String> passageTokenStrings = task.getPassage().getTokenStrings();
		List<String> resolvedPassageTokenStrings = CalFeature.doCorefResolution(task.passage.annotation);

		// Answers stores the answer for each question
		List<String> answers = new ArrayList<String>();
		
		// preprocessing, get token positions in passage
		HashMap<String,List<Integer>> tokenPosi = Util.tokenPosiInPassage(passageTokenStrings);
		HashMap<String,List<Integer>> resolvedTokenPosi = Util.tokenPosiInPassage(resolvedPassageTokenStrings);
		
		// Iterate through each question and find answer
		for (Question question : questions) {
			// Get token lists
			List<String> Q = question.getStemTokenStrings();
			List<String> resolved_Q = CalFeature.doCorefResolution(question.annotation_stem);
			List<List<String>> A = question.getOptionsTokenStrings();

			// Calculate IC(w) for w in passage
			// Use edu.stanford.nlp.stats.ClassicCounter<E>
			Counter<String> IC = new ClassicCounter<String>(passageTokenStrings);
			for (String key : IC.keySet()) {
				IC.setCount(key, Math.log(1 + 1.0 / IC.getCount(key)));
			}

			Counter<String> resolved_IC = new ClassicCounter<String>(resolvedPassageTokenStrings);
			for (String key : resolved_IC.keySet()) {
				resolved_IC.setCount(key, Math.log(1 + 1.0 / resolved_IC.getCount(key)));
			}
			
			// Iterate throught options and calculate sw_i
			List<Double> sw = new ArrayList<Double>();
			for (int i = 0; i < A.size(); i++) {
				
				double scoreBaselineOne = CalFeature.calBaselineOneScore(passageTokenStrings,IC,Q,A.get(i));
				double distancePunish = CalFeature.calDistancePunish(passageTokenStrings,tokenPosi,Q,A.get(i));
				List<String> resolved_a = CalFeature.doCorefResolution(question.annotation_options.get(i));
				double resolved_scoreBaselineOne = CalFeature.calBaselineOneScore(resolvedPassageTokenStrings,resolved_IC,
					resolved_Q,resolved_a);
				double resolved_distancePunish = CalFeature.calDistancePunish(resolvedPassageTokenStrings,resolvedTokenPosi, 
					resolved_Q,resolved_a);

				sw.add(scoreBaselineOne - distancePunish + resolved_scoreBaselineOne - resolved_distancePunish);
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
		System.out.println(answers);
		return answers;
	}
	
}
