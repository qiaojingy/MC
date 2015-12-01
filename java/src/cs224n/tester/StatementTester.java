package cs224n.tester;

import cs224n.MCsystems.*;
import cs224n.MC.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

import java.io.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.PriorityQueue;
import java.util.StringJoiner;



/**
 * The framework for running machine comprehension system
 * @author Qiaojing Yan (qiaojing at stanford.edu), Yixin Wang (wyixin at st * anford.edu)
 */

public class StatementTester<SYS extends MCSystem> {

	public static void main(String[] args) {
  	// Create a CoreNLP pipeline. This line just builds the default pipeline.
		// In comments we show how you can build a particular pipeline
		Annotation annotation = null;
		
		String serializedDataName = "/Users/david/Documents/CS224N/final_project/codes/MC/java/src/cs224n/tester/temp.ser";
		File f = new File(serializedDataName);
		if (f.exists() && !f.isDirectory()) {
			System.out.println("Reading from serialized data ......");
			try {
				FileInputStream fileIn = new FileInputStream(serializedDataName);
				ObjectInputStream objIn = new ObjectInputStream(fileIn);
				annotation = (Annotation) objIn.readObject();
				objIn.close();
				fileIn.close();
			}
			catch (Exception e) {
				System.out.println("Exception during deserialization: " + e);
			}
		}
		else {

			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner, depparse");
			props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
			props.put("ner.applyNumericClassifiers", "false");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			// Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
			// The stem goes in here
			//
			//
			//
			annotation = new Annotation("What did James pull off?");

			// run all the selected Annotators on this text
			pipeline.annotate(annotation);

			try {
				FileOutputStream fileOut = new FileOutputStream(serializedDataName);
				ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
				objOut.writeObject(annotation);
				objOut.close();
				fileOut.close();
				System.out.println("Serialized data is saved in " + serializedDataName);
			}
			catch (IOException e) {
				System.out.println("Exception during serialization: " + e);
			}


		}

		PrintWriter out = new PrintWriter(System.out, true);

		// Access the Annotation in code
		// The toString() method on an Annotation just prints the text of the Annotation
		// But you can see what is in it with other methods like toShorterString()
		out.println();
		out.println("The top level annotation");
		if (annotation == null) return;
		out.println(annotation.toShorterString());

		// An Annotation is a Map and you can get and use the various analyses individually.
		// For instance, this gets the parse tree of the first sentence in the text.
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && ! sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			out.println();
			out.println("The first sentence is:");
			out.println(sentence.toShorterString());
			out.println();
			out.println("The first sentence tokens are:");
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				out.println(token.toShorterString());
			}

			out.println(sentence.get(CoreAnnotations.LemmaAnnotation.class));
			Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
			out.println();
			out.println("The first sentence parse tree is:");
			if (tree == null) System.out.println("The tree is null");
			tree.pennPrint(out);
			out.println();
			out.println("The first sentence basic dependencies are:");
			out.println(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));
			out.println("The first sentence collapsed, CC-processed dependencies are:");
			SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			out.println(graph.toString(SemanticGraph.OutputFormat.LIST));
			IndexedWord rootIWord = graph.getFirstRoot();
			out.println(rootIWord.value());
			String rootPOS = rootIWord.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			
			// The answer goes in here
			//
			//
			//
			//
			String answer = "pudding";

			// Find the wh- word
			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
			String elements[] = { "what", "who", "why", "when", "how", "where", "which" };
			Set whWords = new HashSet(Arrays.asList(elements));
			CoreLabel whToken = null;
			for (CoreLabel token : tokens) {
				if (whWords.contains(token.value().toLowerCase())) {
					out.println(token.value());
					whToken = token;
					break;
				}
			}
			IndexedWord whIWord = new IndexedWord(whToken);
			StringJoiner joiner = new StringJoiner(" ");
			GrammaticalRelation relation;
			SemanticGraphEdge edge;
			switch (whIWord.value().toLowerCase()) {
				case "what":
					switch (rootPOS) {
						case "VBD":
						case "VBP":
						case "VB":
							edge = graph.getEdge(rootIWord, whIWord);
							if (edge == null) break;
							relation = edge.getRelation();
							if (relation.toString() == "dobj") {
								List<IndexedWord> children =graph.getChildList(rootIWord);
								IndexedWord subIWord = null;
								for (IndexedWord child : children) {
									relation = graph.getEdge(rootIWord, child).getRelation();
									if (relation.toString() == "nsubj") {
										subIWord = child;
										break;
									}
								}
								for (int i=0; i<tokens.size(); i++) {
									if (tokens.get(i) == whToken){
										i++;
										continue;
									}
									if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
									if (new IndexedWord(tokens.get(i)).equals(rootIWord)) {
										joiner.add(answer);
									}
								}
							}
							else if (relation.toString() == "nsubj") {
								for (int i=0; i<tokens.size(); i++) {
									if (tokens.get(i) == whToken){
										joiner.add(answer);
										i++;
									}
									if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
								}
							}
							break;
						case "WP":	
							for (int i=0; i<tokens.size(); i++) {
								if (tokens.get(i) == whToken){
									joiner.add(answer);
									i++;
								}
								if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
							}
							break;

						case "NN":
							edge = graph.getEdge(rootIWord, whIWord);
							if (edge == null) break;
							relation =edge.getRelation();
							if (!(relation == null)&(relation.toString() == "nusbj")) {
								for (int i=0; i<tokens.size(); i++) {
									if (tokens.get(i) == whToken){
										joiner.add(answer);
										i++;
									}
									if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
								}
							}
							break;
						default:
							break;
					}
					break;
				case "which":
					for (int i=0; i<tokens.size(); i++) {
						if (tokens.get(i) == whToken){
							joiner.add(answer);
							i++;
						}
						if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
					}
					break;
				case "where":
					switch (rootPOS) {
						case "VBP":
						case "VB":
							relation = graph.getEdge(rootIWord, whIWord).getRelation();
							if (relation.toString() == "advmod") {
								List<IndexedWord> children =graph.getChildList(rootIWord);
								IndexedWord objIWord = null;
								for (IndexedWord child : children) {
									relation = graph.getEdge(rootIWord, child).getRelation();
									if (relation.toString() == "dobj") {
										objIWord = child;
										break;
									}
								}
								int i = 0;
								for (; i<tokens.size(); i++) {
									if (tokens.get(i) == whToken){
										i = i+2;
										break;
									}
								}
								if (objIWord == null) {
									for (; i<tokens.size(); i++) {
										if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
										if (new IndexedWord(tokens.get(i)).equals(rootIWord)) joiner.add(answer);
									}
								}
								else {
									for (; i<tokens.size(); i++) {
										if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
										if (new IndexedWord(tokens.get(i)).equals(objIWord)) joiner.add(answer);
									}
								}
							}

						case "NNP":
							relation = graph.getEdge(rootIWord, whIWord).getRelation();
							if (relation.toString() == "advmod") {
								String a = null;
								for (int i=0; i<tokens.size(); i++) {
									if (tokens.get(i) == whToken) {
										i++;
										a = tokens.get(i).value();
									}
									else {
										if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
										if (new IndexedWord(tokens.get(i)).equals(rootIWord)) {
											joiner.add(a);
											joiner.add(answer);
										}
									}
								}
								joiner.add(".");
							}
							break;

						default:
							break;
					}
					break;
				case "who":
					switch (rootPOS) {
						case "NN":
							break;
						case "VB":
						case "VBG":
						case "VBD":
							relation = graph.getEdge(rootIWord, whIWord).getRelation();
							if (relation.toString() == "nsubj") {
								for (CoreLabel token : tokens) {
									if (token == whToken) joiner.add(answer);
									else if (!(token.value().equals("?"))) {
										joiner.add(token.value());
									}
								}
								joiner.add(".");
							}
							break;
						default:
							break;
					}
					break;
				case "how":
					int whIndex = whIWord.index();
					if (tokens.get(whIWord.index()).value().toLowerCase().equals("many")) {
						int i = 0;
						for (; i < whIndex - 1; i++) {
							joiner.add(tokens.get(i).value());
						}
						joiner.add(answer);
						for (i = whIndex + 1; i < tokens.size(); i++) {
							if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
						}
						joiner.add(".");
						break;
					}
					switch (rootPOS) {
						case "VB":
							List<IndexedWord> children =graph.getChildList(rootIWord);
							IndexedWord subIWord = null;
							for (IndexedWord child : children) {
								relation = graph.getEdge(rootIWord, child).getRelation();
								if (relation.toString() == "nsubj") {
									subIWord = child;
									break;
								}
							}
							joiner.add(subIWord.value());
							int i = 0;
							for (;i < tokens.size(); i++) {
								if (new IndexedWord(tokens.get(i)).equals(subIWord)) break;
							}
							joiner.add(answer);
							joiner.add("to");
							for (i++; i < tokens.size(); i++) {
								if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
							}
							joiner.add(".");
							break;
						default:
							break;
					}
				case "why":
					int i = 0;
					for (;i < tokens.size(); i++) {
						if (new IndexedWord(tokens.get(i)).equals(whIWord)) break;
					}
					for (i=i+2; i < tokens.size(); i++) {
						if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
					}
					joiner.add("because");
					joiner.add(answer);
					joiner.add(".");
					break;

				default:
					break;
			}
			if (!(joiner == null)) out.println(joiner.toString());
		}
		IOUtils.closeIgnoringExceptions(out);
	}

}
 
