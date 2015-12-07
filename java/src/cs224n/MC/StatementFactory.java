package cs224n.MC;

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
import java.util.StringJoiner;



/**
 * The factory combines question stem and options to produce a list of statements. 
 * * @author Qiaojing Yan (qiaojing at stanford.edu), Yixin Wang (wyixin at st * anford.edu)
 */

public class StatementFactory {

	private static StanfordCoreNLP pipeline;
	private static int count = 0;
	public static List<CoreMap> makeStatements(Question question) {
		// We will identify these wh words
		String elements[] = { "what", "who", "why", "when", "how", "where", "which"};
		Set whWords = new HashSet(Arrays.asList(elements));

		CoreMap sentence = question.getStem().get(0);
		List<CoreMap> options = question.getOptions();

		// Find the wh- word
		// whToken is the token of the wh word
		List<CoreLabel> tokens = question.getStemTokens();
		CoreLabel whToken = null;
		for (CoreLabel token : tokens) {
			if (whWords.contains(token.value().toLowerCase())) {
				whToken = token;
				break;
			}
		}

		List<String> statementStrings = new ArrayList<String>();
		if (whToken == null) {
		}

		else {

			// whIWord is the IndexedWord of the wh word
			IndexedWord whIWord = new IndexedWord(whToken);

			// graph is the dependcy graph of the stem
			SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			// rootIWord is the IndexedWord of the root of the semantic graph
			IndexedWord rootIWord = graph.getFirstRoot();
			String rootPOS = rootIWord.get(CoreAnnotations.PartOfSpeechAnnotation.class);

			// These will be used in the conversion
			GrammaticalRelation relation;
			SemanticGraphEdge edge;
			String answer;

			boolean parsable = true;

			switch (whIWord.value().toLowerCase()) {
				// The wh word is "what"
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
								if (subIWord == null) break; 
								for (CoreMap option : options) {
									answer = option.get(CoreAnnotations.TextAnnotation.class);
									StringJoiner joiner = new StringJoiner(" ", "", ".");
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
									statementStrings.add(joiner.toString());
								}
							}
							else if (relation.toString() == "nsubj") {
								for (CoreMap option : options) {
									answer = option.get(CoreAnnotations.TextAnnotation.class);
									StringJoiner joiner = new StringJoiner(" ");
									for (int i=0; i<tokens.size(); i++) {
										if (tokens.get(i) == whToken){
											joiner.add(answer);
											i++;
										}
										if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
									}
									statementStrings.add(joiner.toString());
								}
							}
							break;

						case "WP":	
							for (CoreMap option : options) {
								answer = option.get(CoreAnnotations.TextAnnotation.class);
								StringJoiner joiner = new StringJoiner(" ");
								for (int i=0; i<tokens.size(); i++) {
									if (tokens.get(i) == whToken){
										joiner.add(answer);
										i++;
									}
									if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
								}
								statementStrings.add(joiner.toString());
							}
							break;

						case "NN":
							edge = graph.getEdge(rootIWord, whIWord);
							if (edge == null) break;
							relation =edge.getRelation();
							if (!(relation == null)&(relation.toString() == "nusbj")) {
								for (CoreMap option : options) {
									answer = option.get(CoreAnnotations.TextAnnotation.class);
									StringJoiner joiner = new StringJoiner(" ");
									for (int i=0; i<tokens.size(); i++) {
										if (tokens.get(i) == whToken){
											joiner.add(answer);
											i++;
										}
										if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
									}
									statementStrings.add(joiner.toString());
								}
							}
							break;
						default:
							parsable = false;
							break;
					}
					break;


					// The wh word is "which"
				case "which":
					for (CoreMap option : options) {
						answer = option.get(CoreAnnotations.TextAnnotation.class);
						StringJoiner joiner = new StringJoiner(" ");
						for (int i=0; i<tokens.size(); i++) {
							if (tokens.get(i) == whToken){
								joiner.add(answer);
								i = i + 2;
							}
							if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
						}
						statementStrings.add(joiner.toString());
					}
					break;

				case "where":
					switch (rootPOS) {
						case "VBP":
						case "VB":
							edge = graph.getEdge(rootIWord, whIWord);
							if (edge == null) break;
							relation = edge.getRelation();
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
								for (CoreMap option : options) {
									answer = option.get(CoreAnnotations.TextAnnotation.class);
									StringJoiner joiner = new StringJoiner(" ");
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
									statementStrings.add(joiner.toString());
								}
							}
							break;

						case "NNP":
							edge = graph.getEdge(rootIWord, whIWord);
							if (edge == null) break;
							relation = edge.getRelation();
							if (relation.toString() == "advmod") {
								String a = null;
								for (CoreMap option : options) {
									answer = option.get(CoreAnnotations.TextAnnotation.class);
									StringJoiner joiner = new StringJoiner(" ");
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
									statementStrings.add(joiner.toString());
								}
							}
							break;

						default:
							parsable = false;
							break;
					}
					break;


					// The wh word is "who"
				case "who":
					switch (rootPOS) {
						case "NN":
							break;
						case "VB":
						case "VBG":
						case "VBD":
							edge = graph.getEdge(rootIWord, whIWord);
							if (edge == null) break;
							relation = edge.getRelation();
							if (relation.toString() == "nsubj") {
								for (CoreMap option : options) {
									answer = option.get(CoreAnnotations.TextAnnotation.class);
									StringJoiner joiner = new StringJoiner(" ");
									for (CoreLabel token : tokens) {
										if (token == whToken) joiner.add(answer);
										else if (!(token.value().equals("?"))) {
											joiner.add(token.value());
										}
									}
									statementStrings.add(joiner.toString());
								}
							}
							break;
						case "WP":
							for (CoreMap option : options) {
								answer = option.get(CoreAnnotations.TextAnnotation.class);
								StringJoiner joiner = new StringJoiner(" ");
								for (CoreLabel token : tokens) {
									if (token == whToken) joiner.add(answer);
									else if (!(token.value().equals("?"))) {
										joiner.add(token.value());
									}
								}
								statementStrings.add(joiner.toString());
							}
							break;


						default:
							parsable = false;
							break;
					}
					break;



					// The wh word is "how"
				case "how":
					int whIndex = whIWord.index();
					// (if how is followed by many)
					if (tokens.get(whIWord.index()).value().toLowerCase().equals("many")) {
						for (CoreMap option : options) {
							answer = option.get(CoreAnnotations.TextAnnotation.class);
							StringJoiner joiner = new StringJoiner(" ");
							int i = 0;
							for (; i < whIndex - 1; i++) {
								joiner.add(tokens.get(i).value());
							}
							joiner.add(answer);
							for (i = whIndex + 1; i < tokens.size(); i++) {
								if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
							}
							statementStrings.add(joiner.toString());
						}
						break;
					}
					// (other cases)
					switch (rootPOS) {
						case "VB":
							List<IndexedWord> children =graph.getChildList(rootIWord);
							IndexedWord subIWord = null;
							for (IndexedWord child : children) {
								edge = graph.getEdge(rootIWord, child);
								if (edge == null) break;
								relation = edge.getRelation();
								if (relation.toString() == "nsubj") {
									subIWord = child;
									break;
								}
							}
							if (subIWord == null) break;
							for (CoreMap option : options) {
								answer = option.get(CoreAnnotations.TextAnnotation.class);
								StringJoiner joiner = new StringJoiner(" ");
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
								statementStrings.add(joiner.toString());
							}
							break;
						default:
							parsable = false;
							break;
					}
					break;



					// The wh word is "why" 
				case "why":
					for (CoreMap option : options) {
						answer = option.get(CoreAnnotations.TextAnnotation.class);
						StringJoiner joiner = new StringJoiner(" ");
						int i = 0;
						for (;i < tokens.size(); i++) {
							if (new IndexedWord(tokens.get(i)).equals(whIWord)) break;
						}
						for (i=i+2; i < tokens.size(); i++) {
							if (!(tokens.get(i).value().equals("?"))) joiner.add(tokens.get(i).value());
						}
						joiner.add("because");
						joiner.add(answer);
						statementStrings.add(joiner.toString());
					}
					break;

				default:
					parsable = false;
					break;
			}

		}

		if (statementStrings.size() == 0) {
			for(CoreMap option:options){
				String answer = option.get(CoreAnnotations.TextAnnotation.class);
				StringJoiner joiner = new StringJoiner(" ", "", ".");
				joiner.add(answer);
				for(int i = 0; i < tokens.size(); i++){
					if(!(tokens.get(i).value().equals("?")))joiner.add(tokens.get(i).value());
				}
				statementStrings.add(joiner.toString());
			}
		}
		if (statementStrings.size() == 0) {
			System.out.println("statement string size = 0");
			System.exit(1);
		}

		// Create a CoreNLP pipeline. This line just builds the default pipeline.
		// In comments we show how you can build a particular pipeline
		if (StatementFactory.pipeline == null) {
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner, depparse");
			props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
			props.put("ner.applyNumericClassifiers", "false");
			pipeline = new StanfordCoreNLP(props);
		}
		List<CoreMap> statements = new ArrayList<CoreMap>();
		for (String statementString : statementStrings) {
			Annotation annotation = new Annotation(statementString);

			// run all the selected Annotators on this text
			StatementFactory.pipeline.annotate(annotation);
			statements.add(annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0));
		}
		if (statements == null) {
			System.out.println("null in factory");
		}
		if(statements.size() != 4){
			System.out.println("In statementFactory: Size of statement is "+statements.size());
			System.exit(1);
		}
		System.out.println(count);
		count++;
		return statements;
	}

}

