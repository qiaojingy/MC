package cs224n.MCsystems;

import cs224n.MC.*;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;

import java.util.*;
import java.lang.Math;

public class BaselineTwo implements MCSystem {
	@Override
	public List<String> runMC(Task task) {
		// Read the questions
		List<Question> questions = task.getQuestions();
		List<String> passageTokenStrings = task.getPassage().getTokenStrings();

		// Answers stores the answer for each question
		List<String> answers = new ArrayList<String>();
		
		// preprocessing, get token positions in passage
		HashMap<String,List<Integer>> tokenPosi = Util.tokenPosiInPassage(passageTokenStrings);
		
		// Iterate through each question and find answer
		for (Question question : questions) {
			// Get token lists
			List<String> Q = question.getStemTokenStrings();
			List<List<String>> A = question.getOptionsTokenStrings();

			// Calculate IC(w) for w in passage
			// Use edu.stanford.nlp.stats.ClassicCounter<E>
			Counter<String> IC = new ClassicCounter<String>(passageTokenStrings);
			for (String key : IC.keySet()) {
				IC.setCount(key, Math.log(1 + 1.0 / IC.getCount(key)));
			}

			// Iterate throught options and calculate sw_i
			List<Double> sw = new ArrayList<Double>();
			for (List<String> a : A) {
				
				double scoreBaselineOne = CalFeature.calBaselineOneScore(passageTokenStrings,IC,Q,a);
				double distancePunish = CalFeature.calDistancePunish(passageTokenStrings,tokenPosi,Q,a);

				sw.add(scoreBaselineOne - distancePunish);
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