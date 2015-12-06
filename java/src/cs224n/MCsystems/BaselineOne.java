package cs224n.MCsystems;

import cs224n.MC.*;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;

import java.util.*;
import java.lang.Math;

public class BaselineOne implements MCSystem {
	@Override
	public void train(List<Task> tasks, List<List<String>> answers) {
	}
	
	@Override
	public List<String> runMC(Task task) {
		// Read the questions
		List<Question> questions = task.getQuestions();
		List<String> passageTokenStrings = task.getPassage().getTokenStrings();

		// Answers stores the answer for each question
		List<String> answers = new ArrayList<String>();

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
				// Calculate S = A_i U Q
				List<String> S = new ArrayList<String>();
				S.addAll(Q);
				S.addAll(a);

				// Calculate sw_i
				int S_size = S.size();
				double score = 0;
				double maxScore = 0;
				for (int j=0; j<passageTokenStrings.size()-S.size()+1; j++){
					if (j == 0) {
						for (int w=0; w<S_size; w++){
							String token = passageTokenStrings.get(j+w);
							if (S.contains(token)) {
								score += IC.getCount(token);
							}
						}
						maxScore = score;
					}
					else {
						String token = passageTokenStrings.get(j-1);
						if (S.contains(token)){
							score -= IC.getCount(passageTokenStrings.get(j-1));
						}
						token = passageTokenStrings.get(j+S_size-1);
						if (S.contains(token)){
							score += IC.getCount(passageTokenStrings.get(j+S_size-1));
						}
						if (score > maxScore) {
							maxScore = score;
						}
					}
				}
				sw.add(maxScore);
			}
			// Find largest sw and add answer
			double max = 0;
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
					System.out.println("Answer Number Error"); 
					break;
			}
		}
		return answers;
	}
}
