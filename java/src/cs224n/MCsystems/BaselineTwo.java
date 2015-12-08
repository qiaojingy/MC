package cs224n.MCsystems;

import cs224n.MC.*;
import cs224n.util.*;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;

import java.util.*;
import java.lang.Math;

public class BaselineTwo implements MCSystem {
	public List<Integer> getWResult() {
		return null;
	}


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
		
		// preprocessing, get token positions in passage
		HashMap<String,List<Integer>> tokenPosi = tokenPosiInPassage(passageTokenStrings);
		
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
				
				double scoreBaselineOne = calBaselineOneScore(passageTokenStrings,IC,Q,a);
				double distancePunish = calDistancePunish(passageTokenStrings,tokenPosi,Q,a);

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
		//System.out.println(answers);
		return answers;
	}
	
	public double calBaselineOneScore(List<String> passageTokenStrings,Counter<String> IC,List<String> Q,List<String> a){
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
		//System.out.print("BaselineOne return: ");
		//System.out.println(maxScore);
		return maxScore;
	}
	
	public double calDistancePunish(List<String> passageTokenStrings,HashMap<String,List<Integer>> tokenPosi,List<String> Q,List<String> a){
		//S_Q = (Q intersect PW) minus U
		Set<String> set_Q = new HashSet<String>();
		set_Q.addAll(Q);
		Set<String> set_PW = new HashSet<String>();
		set_PW.addAll(passageTokenStrings);
		Set<String> set_SQ = new HashSet<String>(set_Q);
		set_SQ.retainAll(set_PW);
		StopWordsRemover.removeStopWords(set_SQ);
		//S_Ai = ((Ai intersect PW) minus Q) minus U
		Set<String> set_Ai = new HashSet<String>();
		set_Ai.addAll(a);
		Set<String> set_SAi = new HashSet<String>(set_Ai);
		set_SAi.retainAll(set_PW);
		set_SAi.removeAll(set_Q);
		StopWordsRemover.removeStopWords(set_SAi);
		if(set_SQ.size() == 0 || set_SAi.size() == 0){
			//System.out.print("Return distance punishment: ");
			//System.out.println(1);
			return 1;
		}
		else{
			double ret = (double)getSmallestDistance(tokenPosi,set_SQ,set_SAi) / (passageTokenStrings.size()-1);
			//System.out.print("Return distance punishment: ");
			//System.out.println(ret);
			return ret;
		}
	}
	
	//given a passage, parse the passage and return a HashMap with tokens as keys, 
	//and token positions as a list of integers
	public HashMap<String,List<Integer>> tokenPosiInPassage(List<String> tokens){
		HashMap<String,List<Integer>> ret = new HashMap<String,List<Integer>>();
		for(int i = 0; i < tokens.size(); i++){
			if(ret.containsKey(tokens.get(i))){
				//this token has already emerged before
				ret.get(tokens.get(i)).add(i);
			}
			else{
				//this is the first time this token has emerged
				List<Integer> posi = new ArrayList<Integer>();
				posi.add(i);
				ret.put(tokens.get(i),posi);
			}
		}
		return ret;
	}
	
	public int getSmallestDistance(HashMap<String,List<Integer>> tokenPosi,Set<String> set_SQ,Set<String> set_SAi){
		int min = Integer.MAX_VALUE;
		for(String q:set_SQ){
			for(String a:set_SAi){
				for(int i:tokenPosi.get(q)){
					for(int j:tokenPosi.get(a)){
						if(Math.abs(i-j) < min)min = Math.abs(i-j);
					}
				}
			}
		}
		return min;
	}
}
