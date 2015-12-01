package cs224n.MC;

import cs224n.util.Decodable;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.*;

import java.io.Serializable;
import java.util.*;

/**
 * A question is a multiple choice question for the passage, 
 * A question consists of the stem, the option, and the answer. 
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public class Question implements Serializable, Decodable {
	private static final long serialVersionUID = 1L;


	public static enum QuestionType {
		ONE, MULTIPLE
	}

	/**
	 * The sentences in this document
	 */
	public final List<CoreMap> stem;
	public final QuestionType questionType;
	public final List<CoreMap> options;
	public List<CoreMap> statements;

	/**
	 * Create a document from an id and a list of sentences.
	 * You're not likely to have to use this method.
	 *
	 * @param id The unique id of the document
	 * @param sentences The sentences in the document
	 */
	public Question(QuestionType questionType, List<CoreMap> stem, List<CoreMap> options){
		this.questionType = questionType;
		this.stem = stem;
		this.options = options;
	}

	// Return a list of strings representing tokens in the stem
	public List<String> getStemTokenStrings() {
		List<String> stemTokenStrings = new ArrayList<String>();
		for (CoreMap s : stem) {
			for (CoreLabel token : s.get(CoreAnnotations.TokensAnnotation.class)) {
				stemTokenStrings.add(token.value());
			}
		}
		return stemTokenStrings;
	}

	// Return a list of tokens in the stem
	public List<CoreLabel> getStemTokens() {
		List<CoreLabel> stemTokens = new ArrayList<String>();
		for (CoreMap s : stem) {
			for (CoreLabel token : s.get(CoreAnnotations.TokensAnnotation.class)) {
				stemTokens.add(token);
			}
		}
		return stemTokens;
	}

	public List<List<String>> getOptionsTokenStrings() {
		List<List<String>> optionsTokenStrings = new ArrayList<List<String>>();
		for (CoreMap o : options) {
			List<String> optionTokenStrings = new ArrayList<String>();
			for (CoreLabel token : o.get(CoreAnnotations.TokensAnnotation.class)) {
				optionTokenStrings.add(token.value());
			}
			optionsTokenStrings.add(optionTokenStrings);
		}
		return optionsTokenStrings;
	}

	public void makeStatements() {
		this.statements = StatementFactory.makeStatements(this);
	}

	public CoreMap getStatement(int i) {
		if (statements == null) {
			this.makeStatements();
		}
		return this.statements.get(i);
	}

	public List<CoreMap> getStatements() {
		if (statements == null) {
			this.makeStatements();
		}
		return this.statements;
	}




	public String normalPrint(){
		//--Variables
		int sentenceIndex = 0;
		StringBuilder b = new StringBuilder();
		//--Print Question Type
		b.append(questionType.name()).append(": ");
		//--Print CoreMaps
		for(CoreMap s : stem){
			b.append(s.toString());
		}
		b.append("\n");
		//--Print Options
		for(CoreMap s : options){
			b.append(s.toString()).append("\n");
		}
		return b.toString();
	}

	@Override
	public String toString(){ return this.normalPrint(); }

	//--------------
	// SERIALIZATION
	//--------------
	public String encode() {
		/*
			 StringBuilder b = new StringBuilder();
		//(save question type)
		b.append(this.questionType.name()).append("\n");
		//(save stem)
		b.append("stem:\n");
		for(CoreMap s : stem){
		b.append(s.encode()).append("\n");
		}
		//(end)
		b.append("<end>");
		//(error check)
		if(!decode(b.toString()).equals(this)){
		throw new IllegalStateException("Did not encode Document properly");
		}
		return b.toString();
		*/
		return null;
	}

	public static Question decode(String encoded){
		/*
			 String[] lines = encoded.split("\n");
		//(get question type)
		QuestionType questionType = QuestionType.valueOf(lines[0]);
		//(get stem)
		if(!lines[1].equals("stem:")){ throw new IllegalStateException("Could not decode Question: " + " offending line: " + lines[1]); }
		int index = 2;
		List<CoreMap> stem = new ArrayList<Sentence>();
		while(!lines[index].equals("<end>")){
		stem.add(CoreMap.decode(lines[index]));
		index += 1;
		}
		List<String> options = null;
		return new Question(questionType, stem, options);
		*/
		return null;
	}
}
