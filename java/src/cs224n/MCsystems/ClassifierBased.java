package cs224n.MCsystems;

import cs224n.MC.*;
import cs224n.util.*;
import cs224n.features.*;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.ling.*;

import java.util.*;
import java.lang.Math;

public class ClassifierBased implements MCSystem{
	
	public double[] weights;
	public int featureDim = 3;
	public ArrayList<ArrayList<ArrayList<FeatureValue>>> features = new ArrayList<ArrayList<ArrayList<FeatureValue>>>();
	//features: dim1: i^th training example (p,q,a), dim2: wrt j^th sentence, dim3: features
	public ArrayList<Integer> goldAnswers = new ArrayList<Integer>();
	//correct answers, 0 based, in the same order of (p,q) as specified in variable features
	public static int numPassage = 0;
	public static ArrayList<Integer> numQuestion = new ArrayList<Integer>();
	private List<Featurizer> featurizers;
	private List<FeaturizerOne> featurizerOnes;
	private List<Integer> wResult;
	
	public ClassifierBased() {
		this.featurizers = new ArrayList<Featurizer>();
		this.featurizerOnes = new ArrayList<FeaturizerOne>();

		//Choose different feature combinitions
		//FeaturizerOne bFeaturizer = new SlidingWindowFeaturizer();
		//this.featurizerOnes.add(bFeaturizer);
		FeaturizerOne bLFeaturizer = new SlidingWindowLFeaturizer();
		this.featurizerOnes.add(bLFeaturizer);
		//FeaturizerOne dFeaturizer = new DistanceBasedFeaturizer();
		//this.featurizerOnes.add(dFeaturizer);
		FeaturizerOne dLFeaturizer = new DistanceBasedLFeaturizer();
		this.featurizerOnes.add(dLFeaturizer);
		Featurizer bFeaturizer_w = new BFeaturizer();
		this.featurizers.add(bFeaturizer_w);
		Featurizer dFeaturizer_w = new DFeaturizer();
		this.featurizers.add(dFeaturizer_w);
		Featurizer syntacticFeaturizer = new SyntacticFeaturizer();
		this.featurizers.add(syntacticFeaturizer);
		Featurizer bWFeaturizer = new BWFeaturizer();
		this.featurizers.add(bWFeaturizer);

		this.featureDim = 0;
		System.out.println("Using features: ");
		for (Featurizer featurizer : this.featurizers) {
			this.featureDim += featurizer.getDim();
			System.out.println(featurizer.getName());
		}
		for (FeaturizerOne featurizerOne : this.featurizerOnes) {
			this.featureDim += featurizerOne.getDim();
			System.out.println(featurizerOne.getName());
		}
		System.out.print("Total feature dimension:  ");
		System.out.println(this.featureDim);

	}
	
	public List<Integer> getWResult() {
		return this.wResult;
	}

	// function for prediction: returns the index of the instance (0 based) that maximizes the score
	// features: row: instances (options for a question), column: feature values
	public int predictOne(ArrayList<ArrayList<ArrayList<FeatureValue>>> feature_pq, boolean saveW){
		double maxValue_outer = 0 - Double.MAX_VALUE;
		int maxIdx = -1;
		int wTemp = -1;
		for(int a = 0; a < feature_pq.size(); a++){
			ArrayList<ArrayList<FeatureValue>> feature_pqa = feature_pq.get(a);
			double maxValue_inner = 0 - Double.MAX_VALUE;
			int maxIdx_inner = -1;
			for(int w = 0; w < feature_pqa.size(); w++){
				double score = MatrixUtils.fVectorMultiplication(feature_pqa.get(w),this.weights);
				if(score > maxValue_inner){
					maxValue_inner = score;
					maxIdx_inner = w;
				}
			}
			//System.out.println(maxIdx_inner);
			double score = maxValue_inner;
			if(score > maxValue_outer){
				maxValue_outer = score;
				maxIdx = a;
				wTemp = maxIdx_inner; 
			}
		}
		if (saveW) this.wResult.add(wTemp);
		return maxIdx;
	}

	public int predictOne(ArrayList<ArrayList<ArrayList<FeatureValue>>> feature_pq) {
		return this.predictOne(feature_pq, false);
	}
	
	public void initialize(){
		this.weights = new double[featureDim];
		for(int dim = 0; dim < featureDim; dim++)this.weights[dim] = Math.random() * 1;  //initialize weights to random value
		System.out.println("w initialized to");
		System.out.println(Arrays.toString(this.weights));
	}
	
	@Override
	public void train(List<Task> tasks,List<List<String>> goldAnswerLists){
		
		//collecting features of training examples
		System.out.println("Calculating features. ");
		for(Task t:tasks)calTrainingFeatures(t);
		
		//collecting label of training examples
		System.out.println("Setting labels. ");
		setGoldAnswers(goldAnswerLists);
		
		//initializing weights to random values
		System.out.println("Initializing classifier. ");
		initialize();
		
		//training
		System.out.println("Training. ");
		// learning rate
		double alpha = 0.0001;  //0.0001
		// weight of l2 term
		double lambda = 10;
		// EM, loop until convergence
		double[] old_weights = new double[featureDim];
		int count = 0;
		while(count++ < 1000){
			System.out.println("######Iteration " + count + "#######");
			System.arraycopy(this.weights,0,old_weights,0,featureDim);
			System.out.println("Finding wList according to weights: ");
			System.out.println(Arrays.toString(this.weights));
			
			List<Integer> wList = findWList();
			System.out.println("wList is ");
			System.out.println(Arrays.toString(wList.toArray()));
			gradientDescent(lambda,alpha,wList);
			double diffNorm = MatrixUtils.diffNorm(old_weights,this.weights);
			if(diffNorm < 0.001)break;
		}
		//lambda: 0.1 -- 1 -- 10 -- 100
	}
	
	public List<Integer> findWList(){
		List<Integer> wList = new ArrayList<Integer>();
		int counter = 0;
		for(int p = 0; p < numPassage; p++){
			for(int q = 0; q < numQuestion.get(p); q++){
				double maxValue = 0 - Double.MAX_VALUE;
				int maxIdx = -1;
				ArrayList<ArrayList<FeatureValue>> feature_pqa = features.get(counter*4 + goldAnswers.get(counter));
				for(int w = 0; w < feature_pqa.size(); w++){
					double score = MatrixUtils.fVectorMultiplication(feature_pqa.get(w),this.weights);
					if(score > maxValue){
						maxValue = score;
						maxIdx = w;
					}
				}
				wList.add(maxIdx);
				counter++;
			}
		}
		return wList;
	}
	
	public void gradientDescent(double lambda, double alpha,List<Integer> wList){
		double[] gradient = new double[featureDim];
		//repeat until convergence
		int count = 0;
		double delta = 0.001;
		while(/*count < 1000*/true){
			count++;
			//double step = (double)100*alpha /Math.sqrt(count);
			//System.out.println("adaptive step size is "+step);
			if (count%20 == 0) {
				System.out.format("%dth iteration", count);
				System.out.print("weights is ");
				System.out.println(Arrays.toString(this.weights));
				System.out.println("Loss is " + func(lambda,this.weights,wList));
			}
			for(int dim = 0; dim < featureDim; dim++){
				
				
				double[] new_theta1 = new double[featureDim];
				double[] new_theta2 = new double[featureDim];
				System.arraycopy(this.weights, 0, new_theta1, 0, featureDim);
				System.arraycopy(this.weights, 0, new_theta2, 0, featureDim);
				new_theta1[dim] += delta;
				new_theta2[dim] -= delta;
				
				//System.out.print("modified weights is ");
				//System.out.println(Arrays.toString(new_theta1));
				//System.out.println(Arrays.toString(new_theta2));
				
				gradient[dim] = (func(lambda,new_theta1,wList) - func(lambda,new_theta2,wList))/(2*delta);
				//System.out.print("Func change: ");
				//System.out.println(func(lambda,new_theta1));
				//System.out.println(func(lambda,new_theta2));
				//System.out.println(gradient[dim]);
			}
			double gradient_norm = 0;
			for(int dim = 0; dim < featureDim; dim++){  //update theta
				this.weights[dim] = this.weights[dim] - alpha * gradient[dim];
				//this.weights[dim] = this.weights[dim] - step * gradient[dim];
				//gradient_norm += gradient[dim] * gradient[dim] * step;
				gradient_norm += gradient[dim] * gradient[dim];
			}
			if (count%20 == 0) {
				System.out.println("gradient norm is " + gradient_norm);
			}
			if(gradient_norm < 0.001)break;
		}
	}
	
	public double func(double lambda, double[] theta,List<Integer> wList){
		double regularization = lambda * MatrixUtils.vectorMultiplication(theta,theta);
		int n = goldAnswers.size();
		double sum = 0.0;
		int pq_counter = 0;
		int ans_counter = 0;
		for(int p = 0; p < numPassage; p++){
			for(int q = 0; q < numQuestion.get(p); q++){
				ArrayList<ArrayList<FeatureValue>> feature_pqa = features.get(pq_counter*4 + goldAnswers.get(ans_counter));
				sum -= MatrixUtils.fVectorMultiplication(feature_pqa.get(wList.get(pq_counter)),theta);
				double maxValue_outer = 0 - Double.MAX_VALUE;
				for(int a = 0; a < 4; a++){
					feature_pqa = features.get(pq_counter*4 + a);
					double maxValue_inner = 0 - Double.MAX_VALUE;
					for(int w = 0; w < feature_pqa.size(); w++){
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
	
	public void calTrainingFeatures(Task task) {
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
			for (int a_counter = 0; a_counter < A.size(); a_counter++) {
				List<String> a = A.get(a_counter);
				ArrayList<ArrayList<FeatureValue>> feature_pqa = new ArrayList<ArrayList<FeatureValue>>();
				
				for(int w = 0; w < passage.totalSentenceNum(); w++){
					ArrayList<FeatureValue> feature_pqaw = new ArrayList<FeatureValue>();
					for(FeaturizerOne featurizerOne:this.featurizerOnes){
						List<FeatureValue> featureValueList = featurizerOne.featurize(passage,question,a);
						feature_pqaw.addAll(featureValueList);
					}
					
					for(Featurizer featurizer:this.featurizers){
						List<FeatureValue> featureValueList = featurizer.featurize(passage,w,question,a_counter);
						feature_pqaw.addAll(featureValueList);
					}
					feature_pqa.add(feature_pqaw);
				}
				this.features.add(feature_pqa);
			}
		}
	}
	
	@Override
	public List<String> runMC(Task task){
		// Read the passage
		Passage passage = task.getPassage();
		// Read the questions
		List<Question> questions = task.getQuestions();
		// Answers stores the answer for each question
		List<String> answers = new ArrayList<String>();

		this.wResult = new ArrayList<Integer>();
		
		// Iterate through each question and predict answers
		for (Question question : questions) {
			// stores features of each of these four options
			// dim1: i^th option, dim2: wrt j^th sentence, dim3: features
			ArrayList<ArrayList<ArrayList<FeatureValue>>> test_features = new ArrayList<ArrayList<ArrayList<FeatureValue>>>();
			
			// Get token lists
			List<List<String>> A = question.getOptionsTokenStrings();

			for (int a_counter = 0; a_counter < A.size(); a_counter++) {
				List<String> a = A.get(a_counter);
				ArrayList<ArrayList<FeatureValue>> feature_pqa = new ArrayList<ArrayList<FeatureValue>>();
				
				for(int w = 0; w < passage.totalSentenceNum(); w++){
					ArrayList<FeatureValue> feature_pqaw = new ArrayList<FeatureValue>();
					for (FeaturizerOne featurizerOne : this.featurizerOnes) {
						List<FeatureValue> featureValueList = featurizerOne.featurize(passage, question, a);
						feature_pqaw.addAll(featureValueList);
					}
					
					for (Featurizer featurizer : this.featurizers) {
						List<FeatureValue> featureValueList = featurizer.featurize(passage, w, question, a_counter);
						feature_pqaw.addAll(featureValueList);
					}
					
					feature_pqa.add(feature_pqaw);
				}
				test_features.add(feature_pqa);
			}
			int i_ans = predictOne(test_features, true);
			String ans = "";
			switch(i_ans){
				case 0:
				ans = "A";
				break;
				case 1:
				ans = "B";
				break;
				case 2:
				ans = "C";
				break;
				case 3:
				ans = "D";
				break;
				default:
				System.out.println("Error when predicting answer. ");
			}
			answers.add(ans);
		}
		return answers;
	}
	
	public void setGoldAnswers(List<List<String>> goldAnswerLists){
		//loop through gold answer lists and append them linearly
		for(int p = 0; p < goldAnswerLists.size(); p++){
			for(int q = 0; q < goldAnswerLists.get(p).size(); q++){
				String ans = goldAnswerLists.get(p).get(q);
				switch(ans){
					case "A":
					this.goldAnswers.add(0);
					break;
					case "B":
					this.goldAnswers.add(1);
					break;
					case "C":
					this.goldAnswers.add(2);
					break;
					case "D":
					this.goldAnswers.add(3);
					break;
					default:
					System.out.println("Missing gold answer. ");
				}
			}
		}
	}
}
		//Featurizer bFeaturizer_w = new BFeaturizer();
