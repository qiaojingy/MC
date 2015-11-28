package cs224n.MCsystems;

import cs224n.MC.Passage;
import cs224n.MC.Question;
import cs224n.MC.Document;
import cs224n.MC.Task;

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

public class TaskReader {
	public static List<Task> read(String fileName) {
		List<Task> tasks = new ArrayList<Task>();
		try {
			// FileReader reads text files in the default encoding
			FileReader fileReader = new FileReader(fileName);

			// Wrap FileReader in BufferedReader
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;
			while ((line = bufferedReader.readLine())!= null) {
				// Read a line. In MC datasets, one line correspondes to a task
				String[] terms = line.split("\t");

				// Id of the task
				String id = terms[0];
				// Author of the task
				String author = terms[1].split(";")[0].split(": ")[1];
				// Time spent
				Integer time = Integer.valueOf(terms[1].split(";")[1].split(": ")[1]);
				// Read passage
				Passage passage;
				// The string corresponds to the main passage
				String passageString = terms[2].replaceAll("\\\\newline", " ");

				// Use Stanford CoreNLP to process the string
				Properties props = new Properties();
				props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
				StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

				Annotation annotation_p = new Annotation(passageString);

				// run all the selected Annotators on this text
				pipeline.annotate(annotation_p);

				List<CoreMap> sentences = annotation_p.get(CoreAnnotations.SentencesAnnotation.class);
				if (sentences != null && ! sentences.isEmpty()) {
					passage = new Passage(sentences,annotation_p);
				}
				else {
					passage = null;
				}

				// Read questions
				List<Question> questions = new ArrayList<Question>();
				int lineIndex = 3;
				while (lineIndex < terms.length) {
					// Read question type
					String qtype = terms[lineIndex].split(": ")[0];
					Question.QuestionType questionType = Question.QuestionType.ONE;
					switch (qtype) {
						case "one":
							questionType = Question.QuestionType.ONE;
							break;
						case "multiple":
							questionType = Question.QuestionType.MULTIPLE;
							break;
						default:
							System.out.println("Question Type Error");
							break;
					}
					// Read question stem
					String questionString = terms[lineIndex].split(": ")[1];
					Annotation annotation_stem = new Annotation(questionString);
					pipeline.annotate(annotation_stem);
					List<CoreMap> stem = annotation_stem.get(CoreAnnotations.SentencesAnnotation.class);
					// Read options
					List<CoreMap> options = new ArrayList<CoreMap>();
					List<Annotation> annotation_options = new ArrayList<Annotation>();
					for (int i=1; i<=4; i++){
						Annotation annotation_oneOption = new Annotation(terms[lineIndex+i]);
						pipeline.annotate(annotation_oneOption);
						annotation_options.add(annotation_oneOption);
						options.add(annotation_oneOption.get(CoreAnnotations.SentencesAnnotation.class).get(0));
					}
					Question question = new Question(questionType, stem, options, annotation_stem, annotation_options);
					questions.add(question);
					lineIndex += 5;
				}
				Task task = new Task(id, author, time, passage, questions);
				tasks.add(task);
				System.out.println("Task Reading is complete! ");
				System.out.println();
				System.out.println(task.normalPrint());
			}

			// Close files
			bufferedReader.close();
		}
		catch (IOException ex) {
			System.out.println("Error reading file \"" + fileName + "\"");
		} 
		return tasks;
	}
}
