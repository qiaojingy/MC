package cs224n.MC;

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

public class AnswerReader {
	public static List<List<String>> read(String fileName) {
		List<List<String>> answers = new ArrayList<List<String>>();
		try {
			// FileReader reads text files in the default encoding
			FileReader fileReader = new FileReader(fileName);

			// Wrap FileReader in BufferedReader
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;
			while ((line = bufferedReader.readLine())!= null){
				// Read a line. In MC datasets, one line correspondes to the answers of a task
				List<String> answer = new ArrayList<String>();
				for (String s : line.split("\t")) {
					answer.add(s);
				}
				answers.add(answer);
			}

			// Close files
			bufferedReader.close();
		}
		catch (IOException ex) {
			System.out.println("Error reading file \"" + fileName + "\"");
		} 
		return answers;
	}
}
