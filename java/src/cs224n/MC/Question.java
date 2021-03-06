package cs224n.MC;

import cs224n.util.Decodable;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;

import java.io.Serializable;
import java.util.*;

/**
 * A question is a multiple choice question for the passage, 
 * A question consists of the stem, the option, and the answer. 
 * @author Qiaojing Yan (qiaojing at cs.stanford), Yixin Wang (wyixin at stanford.edu)
 */
public class Question implements Serializable {
	private static final long serialVersionUID = 1L;


	public enum QuestionType {
		ONE, MULTIPLE
	}

	/**
	 * The sentences in this document
	 */
	public final List<CoreMap> stem;
	public final QuestionType questionType;
	public final List<CoreMap> options; 
	public final Annotation annotation_stem;
	public final List<Annotation> annotation_options;
	public boolean negation;
	public List<CoreMap> statements;
	public List<float[]> fap;
	public List<float[]> fam;

	/**
	 * Create a document from an id and a list of sentences.
	 * You're not likely to have to use this method.
	 *
	 * @param id The unique id of the document
	 * @param sentences The sentences in the document
	 */
	public Question(QuestionType questionType, List<CoreMap> stem, List<CoreMap> options, 
			Annotation annotation_stem, List<Annotation> annotation_options){
		this.questionType = questionType;
		this.stem = stem;
		this.options = options;
		this.annotation_stem = annotation_stem;
		this.annotation_options = annotation_options;
		this.isNegation();

		boolean neg = false; 
		boolean wh = false; 
		for (String s : this.getStemTokenStrings()) {
			if (s.equals("not") || s.equals("n't")) neg = true;
			if (s.equals("how") || s.equals("why")) wh = true;
		}
		if (neg && !wh) this.negation = true;
		else this.negation = false;
	}

	public List<CoreMap> getStem() {
		return this.stem;
	}

	public List<CoreMap> getOptions() {
		return this.options;
	}

	public boolean isNegation() {
		return this.negation;
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

	public List<String> getStemLemmaStrings() {
		List<String> stemLemmaStrings = new ArrayList<String>();
		for (CoreMap s : stem) {
			for (CoreLabel token : s.get(CoreAnnotations.TokensAnnotation.class)) {
				stemLemmaStrings.add(token.get(CoreAnnotations.LemmaAnnotation.class));
			}
		}
		return stemLemmaStrings;
	}

	// Return a list of tokens in the stem
	public List<CoreLabel> getStemTokens() {
		List<CoreLabel> stemTokens = new ArrayList<CoreLabel>();
		for (CoreMap s : stem) {
			for (CoreLabel token : s.get(CoreAnnotations.TokensAnnotation.class)) {
				stemTokens.add(token);
			}
		}
		return stemTokens;
	}

	public List<String> getOptionTokenStrings(int a) {
		CoreMap o = options.get(a);
		List<String> optionTokenStrings = new ArrayList<String>();
		for (CoreLabel token : o.get(CoreAnnotations.TokensAnnotation.class)) {
			optionTokenStrings.add(token.value());
		}
		return optionTokenStrings;
	}

	public List<CoreLabel> getOptionTokens(int a) {
		CoreMap o = options.get(a);
		List<CoreLabel> optionTokens = new ArrayList<CoreLabel>();
		for (CoreLabel token : o.get(CoreAnnotations.TokensAnnotation.class)) {
			optionTokens.add(token);
		}
		return optionTokens;
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

	public void calculateFa(WordEmbeddingsDict dict) {
		int size = dict.getSize();
		float[] fap_q = new float[size]; 
		float[] fam_q = new float[size]; 

		for (int i=0; i<size; i++) {
			fap_q[i] = 0;
			fam_q[i] = (float) 1e30;
		}

		for (String stemTokenString : this.getStemTokenStrings()) {
			float[] v = dict.getWordVector(stemTokenString);
			if (v == null) continue;
			for (int i=0; i<size; i++) {
				fap_q[i] = fap_q[i] + v[i];
				fam_q[i] = fam_q[i] * v[i];
			}
		}
		this.fap = new ArrayList<float[]>();
		this.fam = new ArrayList<float[]>();

		for (List<String> optionTokenStrings : this.getOptionsTokenStrings()) {
			float[] fap_all = new float[size];
			float[] fam_all = new float[size];
			for (int i=0; i<size; i++) {
				fap_all[i] = fap_q[i];
				fam_all[i] = fam_q[i];
			}
			for (String optionTokenString : optionTokenStrings) {
				float[] v = dict.getWordVector(optionTokenString);
				if (v == null) continue;
				for (int i=0; i<size; i++) {
					fap_all[i] = fap_all[i] + v[i];
					fam_all[i] = fam_all[i] * v[i];
				}
			}
			float len_p = 0;
			float len_m = 0;
			for (int i=0; i<size; i++) {
				len_p += fap_all[i] * fap_all[i];
				len_m += fam_all[i] * fam_all[i];
			}
			len_p = (float) Math.sqrt(len_p);
			len_m = (float) Math.sqrt(len_m);

			if (len_m == (double) 0) {
				System.out.println("isNaN");
				System.out.println(len_m);
				for (int m=0; m<fam_all.length; m++) {
					System.out.print(fam_all[m]);
					System.out.print("*");
				}
				System.exit(1);
			}
			for (int i=0; i<size; i++) {
				fap_all[i] = (float) (fap_all[i]/len_p);
				fam_all[i] = (float) (fam_all[i]/len_m);
			}

			this.fap.add(fap_all);
			this.fam.add(fam_all);
		}

	}


	public float[] getFap(WordEmbeddingsDict dict, int a) {
		if (this.fap == null) {
			this.calculateFa(dict);
		}
		return this.fap.get(a);
	}

	public float[] getFam(WordEmbeddingsDict dict, int a) {
		if (this.fam == null) {
			this.calculateFa(dict);
		}
		return this.fam.get(a);
	}

	public void makeStatements() {
		this.statements = StatementFactory.makeStatements(this);
	}

	public CoreMap getStatement(int i) {
		if (this.statements == null) {
			System.out.println("Making statements");
			this.makeStatements();
		}
		if (this.statements == null) {
			System.out.println("null indeed");
		}
		return this.statements.get(i);
	}

	public void printStatements() {
		List<CoreMap> statements = this.getStatements();
		System.out.println("Statements: ");
		for (CoreMap statement : statements) {
			System.out.println(statement.get(CoreAnnotations.TextAnnotation.class));
		}
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

}
