package cs224n.MCsystems;

import cs224n.MC.*;
import cs224n.util.*;
import cs224n.features.*;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;

import java.util.*;
import java.lang.Math;

public class ClassifierBased implements MCSystem {
	
	public int[] weights;
	public List<List<List<FeatureValue>>> features = new List<List<List<FeatureValue>>>();
	//features: dim1: i^th training example (p,q,a), dim2: wrt j^th sentence, dim3: features
	public List<Integer> goldAnswers = new List<Integer>();
	//correct answers, 0 based, in the same order of (p,q) as specified in variable features
	public static int numPassage = 0;
	public static List<Integer> numQuestion = new List<Integer>();
	
	// function for prediction: returns the index of the instance (0 based) that maximizes the score
	// features: row: instances, column: feature values
	public int predict(int[][] featureMatrix){
		int maxScore = Integer.MIN_VALUE;
		int maxIdx = -1;
		for(int i = 0; i < featureMatrix.length(); i++){
			int score = 0;
			for(int j = 0; j < featureMatrix[0].length(); j++)score += featureMatrix[i][j] * weights[j];
			if(score > maxScore){
				maxScore = score;
				maxIdx = i;
			}
		}
		return maxIdx;
	}
	
	public int[] train(){
		//theta[][], 
		random(theta);
		double delta = 0.01;
		double alpha = 0.1;
		for(int dim = 0; dim < theta.length(); i++){
			new_theta1 = theta;
			new_theta1[dim] += delta;
			new_theta2 = theta;
			new_theta2[dim] -= delta;
			double gradient = (func(new_theta1) - func(new_theta2))/(2*delta);
			theta[dim] = theta[dim] - alpha * gradient;  // bug HERE
		}
		//lambda: 0.1 -- 1 -- 10 -- 100
	}
	
	public double func(double lambda, double[] theta){
		double regularization = lambda * MatrixUtils.vectorMultiplication(theta,theta);
		int n = goldAnswers.size();
		double sum = 0.0;
		int pq_counter = 0;
		int ans_counter = 0;
		for(int p = 0; p < numPassage; p++){
			for(int q = 0; q < numQuestion.get(p); q++){
				int maxValue = Integer.MIN_VALUE;
				List<List<FeatureValue>> feature_pqa = features.get(pq_counter*4 + goldAnswers.get(ans_counter));
				for(int w = 0; w < features_pqa.size(); w++){
					double score = MatrixUtils.fVectorMultiplication(feature_pqa.get(w),theta);
					if(score > maxValue)maxValue = score;
				}
				sum -= maxValue;
				int maxValue_outer = Integer.MIN_VALUE;
				for(int a = 0; a < 4; a++){
					feature_pqa = features.get(pq_counter*4 + a);
					int maxValue_inner = Integer.MIN_VALUE;
					for(int w = 0; w < features_pqa.size(); w++){
						double score = MatrixUtils.fVectorMultiplication(feature_pqa.get(w),theta);
						if(score > maxValue_inner)maxValue_inner = score;
					}
					double score = maxValue_inner;
					if(a != goldAnswers.get(ans_counter))score += 1;
					if(score > maxValue_outer)maxValue_outer = score;
				}
				sum += maxValue_outer;
				ans_counter++;
				pq_counter++;
			}
		}
		return regularization + sum;
	}
	
	@Override
	public List<String> runMC(Task task) {
		// Read the passage
		Passage passage = task.getPassage();
		numPassage++;

		// Read the questions
		List<Question> questions = task.getQuestions();
		numQuestion.add(questions.size());

		// Answers stores the answer for each question
		List<String> answers = new ArrayList<String>();
		
		// Iterate through each question and find features
		for (Question question : questions) {
			// Get token lists
			List<List<String>> A = question.getOptionsTokenStrings();

			// Iterate throught options and calculate sw_i
			for (List<String> a : A) {
				List<List<FeatureValue>> feature_pqa = new List<List<FeatureValue>>();
				FeaturizerOne slidingWindowFeaturizer = new SlidingWindowFeaturizer();
				//slidingWindowFeaturizer.featurize(passage, question, a);
				FeatureValue scoreBaselineOne = slidingWindowFeaturizer.featurize(passage, question, a).get(0); 
				FeaturizerOne distanceBasedFeaturizer = new DistanceBasedFeaturizer();
				FeatureValue distancePunish = distanceBasedFeaturizer.featurize(passage, question, a).get(0);
				
				for(int w = 0; w < passage.totalSentenceNum(); w++){
					List<FeatureValue> feature_pqaw = new List<FeatureValue>();
					feature_pqaw.add(scoreBaselineOne);
					feature_pqaw.add(distancePunish);
					Featurizer syntacticFeaturizer = new SyntacticFeaturizer();
					FeatureValue scoreSyntactic = syntacticFeaturizer.featurize(passage,w,question,a).get(0);
					feature_pqaw.add(scoreSyntactic);
					feature_pqa.add(feature_pqaw);
				}
				this.features.add(feature_pqa);
			}
		}
		//System.out.println(answers);
		return answers;
	}
	
	public setGoldAnswers(List<Integer> goldAnswers){
		this.goldAnswers = goldAnswers;
	}
	
}
