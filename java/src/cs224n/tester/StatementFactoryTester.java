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
 * 
 * @author Qiaojing Yan (qiaojing at stanford.edu), Yixin Wang (wyixin at st * anford.edu)
 */

public class StatementFactoryTester<SYS extends MCSystem> {
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
		//--Read Data
		System.out.println("Reading documents ... ");
		//(get path)
		dataPath = "/Users/yixinwang/Study/2015Autumn/CS224N/project/Data/MCTest/";
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

		// Read tasks
		String fileName = dataPath.concat(new String("mc160.train.tsv"));
		List<Task> tasks = TaskReader.read(fileName);
		Task task = tasks.get(0);
		List<Question> questions = task.getQuestions();
		Question question = questions.get(2);
		question.printStatements();
	}


}
