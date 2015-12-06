package cs224n.tester;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

public class NNDPAPITester {

	public static void main(String[] args) throws IOException {
		// Create a CoreNLP pipeline. This line just builds the default pipeline.
		// In comments we show how you can build a particular pipeline
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner, depparse");
		props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
		props.put("ner.applyNumericClassifiers", "false");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
		Annotation annotation;
		annotation = new Annotation("How did Mark feel.");

		// run all the selected Annotators on this text
		pipeline.annotate(annotation);

		PrintWriter out = new PrintWriter(System.out, true);
		// print the results to file(s)
		pipeline.prettyPrint(annotation, out);

		// Access the Annotation in code
		// The toString() method on an Annotation just prints the text of the Annotation
		// But you can see what is in it with other methods like toShorterString()
		out.println();
		out.println("The top level annotation");
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
			for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				out.println(token.toShorterString());
			}
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

			// Access coreference. In the coreference link graph,
			// each chain stores a set of mentions that co-refer with each other,
			// along with a method for getting the most representative mention.
			// Both sentence and token offsets start at 1!
			out.println("Coreference information");
			Map<Integer, CorefChain> corefChains =
				annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class);
			if (corefChains == null) { return; }
			for (Map.Entry<Integer,CorefChain> entry: corefChains.entrySet()) {
				out.println("Chain " + entry.getKey() + " ");
				for (CorefChain.CorefMention m : entry.getValue().getMentionsInTextualOrder()) {
					// We need to subtract one since the indices count from 1 but the Lists start from 0
					List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(CoreAnnotations.TokensAnnotation.class);
					// We subtract two for end: one for 0-based indexing, and one because we want last token of mention not one following.
					out.println("  " + m + ", i.e., 0-based character offsets [" + tokens.get(m.startIndex - 1).beginPosition() +
							", " + tokens.get(m.endIndex - 2).endPosition() + ")");
				}
			}
		}
		IOUtils.closeIgnoringExceptions(out);
	}

}

