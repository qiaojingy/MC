package cs224n.tester;

import cs224n.MCsystems.*;
import cs224n.MC.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

import java.io.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.PriorityQueue;



/**
 * The framework for running machine comprehension system
 * @author Qiaojing Yan (qiaojing at stanford.edu), Yixin Wang (wyixin at st * anford.edu)
 */

public class ClassifierTester<SYS extends MCSystem> {
	private static String dataPath = "/Users/yixinwang/Study/2015Autumn/CS224N/project/Data/MCTest/";

	public static enum DataType {
		TRAIN, DEV, TEST
	}

	private static List<Task> getData(String dataPath) {
		String fileName = dataPath.concat(new String("mc160.train.tsv"));
		System.out.print("Reading:  ");
		System.out.println(fileName);
		String line = null;

		List<Task> tasks = TaskReader.read(fileName);

		return tasks;
	}


	public static void main(String[] args) {
		//--Get Properties
		Properties props = StringUtils.argsToProperties(args);
		// (order keys)
		PriorityQueue<String> keys = new PriorityQueue<String>();
		int maxLength = 0;
		for (Object key : props.keySet()) {
			keys.add(key.toString());
			maxLength = Math.max(maxLength, key.toString().length());
		}

		//(print header)
		System.out.println("-------------------");
		System.out.println(" MC Tester");
		System.out.println("-------------------");
		System.out.println("Options:");
		//(print keys)
		for (String key : keys){
			System.out.print("  -" + key);
			for (int i=0; i<maxLength-key.length(); i++){
				System.out.print(" ");
			}
			System.out.println("    " + props.getProperty(key));
		}
		System.out.println();

		//--Create MC Class
		ClassifierBased classifierBased = new ClassifierBased();

		//--Read Data
		System.out.println("Reading documents ... ");
		//(get path)
		dataPath = props.getProperty("path", "/Users/yixinwang/Study/2015Autumn/CS224N/project/Data/MCTest/");
		if (!new File(dataPath).exists()) {
			dataPath = "/Users/david/Documents/CS224N/final_project/Dataset/MCTest/";
			if (!new File(dataPath).exists()) {
				System.out.println("ERROR: no such path");
				System.exit(1);
			}
		}
		if (!new File(dataPath).isDirectory()){
			System.out.println("ERROR: not a directory");
			System.exit(1);
		}

		// Read training tasks
		System.out.println("Reading training tasks ...");
		String fileName = dataPath.concat(new String("mc160.train.tsv"));
		List<Task> tasks = TaskReader.read(fileName);
		//fileName = dataPath.concat(new String("mc500.train.tsv"));
		//tasks.addAll(TaskReader.read(fileName));
		for(Task t:tasks)classifierBased.calTrainingFeatures(t);  // calculating training example features

		// Read training answers
		System.out.println("Reading gold answers ...");
		fileName = dataPath.concat(new String("mc160.train.ans"));
		List<List<String>> goldAnswerLists = AnswerReader.read(fileName);
		//fileName = dataPath.concat(new String("mc500.train.ans"));
		//goldAnswerLists.addAll(AnswerReader.read(fileName));
		classifierBased.setGoldAnswers(goldAnswerLists);
		//System.out.println(goldAnswerLists);
		
		//initializing
		System.out.println("Initializing classifier ...");
		classifierBased.initialize();
		
		//training
		System.out.println("Training.");
		classifierBased.train(tasks, goldAnswerLists);
		
		//Read test/dev set
		String test_or_dev = "dev";
		System.out.println("Reading " + test_or_dev + " tasks ...");
		fileName = dataPath.concat(new String("mc160."+test_or_dev+".tsv"));
		List<Task> test_tasks = TaskReader.read(fileName);
		//fileName = dataPath.concat(new String("mc500."+test_or_dev+".tsv"));
		//test_tasks.addAll(TaskReader.read(fileName));
		
		//Read test/dev answers
		System.out.println("Reading " + test_or_dev + " answers ...");
		fileName = dataPath.concat(new String("mc160."+test_or_dev+".ans"));
		List<List<String>> test_goldAnswerLists = AnswerReader.read(fileName);
		//fileName = dataPath.concat(new String("mc500."+test_or_dev+".ans"));
		//test_goldAnswerLists.addAll(AnswerReader.read(fileName));

		// Do machine comprehension using selected MC system and compare with answer
		Integer correct = 0;
		Integer all = 0;
		for(int i = 0; i < test_tasks.size(); i++){
			Task task = test_tasks.get(i);
			List<String> answers = classifierBased.runMC(task);
			List<String> test_goldAnswerList = test_goldAnswerLists.get(i);
			for(int j = 0; j < answers.size(); j++){
				if(answers.get(j).equalsIgnoreCase(test_goldAnswerList.get(j)))correct += 1;
				all += 1;
			}
		}
		
		System.out.print("Correctly answered ");
		System.out.print(correct.toString());
		System.out.print(" out of ");
		System.out.print(all.toString());
		System.out.println(" Questions");

		Double accuracy = correct * 100.0/all; 
		System.out.print("Accuracy:  ");
		System.out.print(accuracy.toString());
		System.out.println("%");
	}

}
