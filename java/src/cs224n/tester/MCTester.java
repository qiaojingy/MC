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

public class MCTester<SYS extends MCSystem> {
	private static String dataPath = "/Users/yixinwang/Study/2015Autumn/CS224N/project/Data/MCTest/";

	public static enum DataType {
		TRAIN, DEV, TEST
	}

	private static List<Task> getData(String dataPath) {
		String fileName = dataPath.concat(new String("mc500.test.tsv"));
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
		System.out.println("Creating MC System...");
		//(classname)
		String systemClass = props.getProperty("model", "BaselineOne");
		//if (systemClass.equalsIgnoreCase("baselineone")) {
		//	systemClass = BaselineOne.class.getName();
		//}
		MCSystem system;
		try {
			//((try loading the class))
			system = MetaClass.create(systemClass).createInstance();
		}
		catch (MetaClass.ClassCreationException e) {
			//((maybe you forget to include the package))
			try {
				system = MetaClass.create("cs224n.MCsystems."+systemClass).createInstance();
			}
			catch (MetaClass.ClassCreationException e2) {
				throw e;
			}
		}
		System.out.println("done");

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
		List<Task> trainingTasks = TaskReader.read(fileName);

		fileName = dataPath.concat(new String("mc500.train.tsv"));
		trainingTasks.addAll(TaskReader.read(fileName));
		
		// Read training answers
		System.out.println("Reading gold answers ...");
		fileName = dataPath.concat(new String("mc160.train.ans"));
		List<List<String>> trainingGoldAnswerLists = AnswerReader.read(fileName);
		
		fileName = dataPath.concat(new String("mc500.train.ans"));
		trainingGoldAnswerLists.addAll(AnswerReader.read(fileName));
		
		// Train the MC system
		system.train(trainingTasks,trainingGoldAnswerLists);

		// Read test tasks
		String dev_or_test = "test";
		fileName = dataPath.concat(new String("mc160."+dev_or_test+".tsv"));
		List<Task> testTasks = TaskReader.read(fileName);

		fileName = dataPath.concat(new String("mc500."+dev_or_test+".tsv"));
		testTasks.addAll(TaskReader.read(fileName));

		// Read answers
		System.out.println("Reading gold answers ...");
		fileName = dataPath.concat(new String("mc160."+dev_or_test+".ans"));
		List<List<String>> testGoldAnswerLists = AnswerReader.read(fileName);

		fileName = dataPath.concat(new String("mc500."+dev_or_test+".ans"));
		testGoldAnswerLists.addAll(AnswerReader.read(fileName));

		// Do machine comprehension using selected MC system and compare with answer
		Integer correct = 0;
		Integer all = 0;
		for (int i=0; i<testTasks.size(); i++) {
			Task task= testTasks.get(i);
			List<String> answers = system.runMC(task);
			List<String> testGoldAnswerList = testGoldAnswerLists.get(i);
			for (int j=0; j<answers.size(); j++){
				//System.out.format("%d th question: ", j+1);
				if (answers.get(j).equalsIgnoreCase(testGoldAnswerList.get(j))) {
					correct += 1;
					//System.out.print("Correct!");
				}
				//System.out.format("Gold: %s,  Answer: %s %n", goldAnswerList.get(j), answers.get(j));
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
