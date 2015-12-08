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
import cs224n.util.CorefResolWorker;

import java.io.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class TaskReader {
	public static List<Task> readFromText(String fileName,boolean coref) {
		List<Task> tasks = new ArrayList<Task>();
		try {
			// FileReader reads text files in the default encoding
			FileReader fileReader = new FileReader(fileName);

			// Wrap FileReader in BufferedReader
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			// Prepare to use Stanford CoreNLP to process the string
			// properties for coref resolution
			Properties props1 = new Properties();
			props1.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner, dcoref");
			props1.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
			props1.put("ner.applyNumericClassifiers", "false");
			StanfordCoreNLP pipeline1 = new StanfordCoreNLP(props1);
			
			// Properties for annotating the passage after coref resolution
			Properties props2 = new Properties();
			props2.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner, dcoref, depparse");
			props2.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
			props2.put("ner.applyNumericClassifiers", "false");
			StanfordCoreNLP pipeline2 = new StanfordCoreNLP(props2);

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

				Annotation annotation_passage = new Annotation(passageString);
				// run all the selected Annotators on this text
				if(coref == true){
					System.out.println(passageString);
					pipeline1.annotate(annotation_passage);
					//passageString = CorefResolWorker.doCorefResolution(annotation_passage);
					annotation_passage = new Annotation(passageString);
				}
				
				//pipeline2.annotate(annotation_passage);
				pipeline1.annotate(annotation_passage);
				
				System.out.println(passageString);

				List<CoreMap> sentences = annotation_passage.get(CoreAnnotations.SentencesAnnotation.class);
				if (sentences != null && ! sentences.isEmpty()) {
					passage = new Passage(sentences,annotation_passage);
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
					pipeline2.annotate(annotation_stem);
					List<CoreMap> stem = annotation_stem.get(CoreAnnotations.SentencesAnnotation.class);
					// Read options
					List<CoreMap> options = new ArrayList<CoreMap>();
					List<Annotation> annotation_options = new ArrayList<Annotation>();
					for (int i=1; i<=4; i++){
						Annotation annotation_oneOption = new Annotation(terms[lineIndex+i]);
						pipeline2.annotate(annotation_oneOption);
						annotation_options.add(annotation_oneOption);
						options.add(annotation_oneOption.get(CoreAnnotations.SentencesAnnotation.class).get(0));
					}
					Question question = new Question(questionType, stem, options,annotation_stem,annotation_options);
					//make statements
					question.makeStatements();
					questions.add(question);
					lineIndex += 5;
				}
				Task task = new Task(id, author, time, passage, questions);
				tasks.add(task);
			}

			// Close files
			bufferedReader.close();
		}
		catch (IOException ex) {
			System.out.println("Error reading file \"" + fileName + "\"");
		} 

		// Save the tasks by serialization
		try {
			String s_coref = "";
			if(coref == true)s_coref = ".coref";
			FileOutputStream fileOut = new FileOutputStream(fileName + s_coref + ".ser");
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(tasks);
			objOut.close();
			fileOut.close();
			System.out.println("Serialized data is saved in " + fileName + s_coref + ".ser");
		}
		catch (IOException e) {
			System.out.println("Exception during serialization: " + e);
		}

		return tasks;
	}

	public static List<Task> readFromSerializedData(String serializedDataName) {
		List<Task> tasks = new ArrayList<Task>();
		try {
			FileInputStream fileIn = new FileInputStream(serializedDataName);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			tasks = (ArrayList<Task>) objIn.readObject();
			objIn.close();
			fileIn.close();
		}
		catch (Exception e) {
			System.out.println("Exception during deserialization: " + e);
		}
		return tasks;
	}



	public static List<Task> read(String fileName,boolean coref) {
		String s_coref = "";
		if(coref == true)s_coref = ".coref";
		String serializedDataName = fileName + s_coref + ".ser";
		File f = new File(serializedDataName);
		if (f.exists() && !f.isDirectory()) {
			System.out.println("Reading from serialized data ......");
			return readFromSerializedData(serializedDataName);
		}
		else {
			return readFromText(fileName,coref);
		}
	}

}
