package cs224n.MC;

import cs224n.util.Decodable;


import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.stats.*;

import java.io.Serializable;
import java.util.*;

/**
 * A passage is the story part of the document
 *
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public class Passage implements Serializable, Decodable {
	private static final long serialVersionUID = 1L;
	/**
	 * The sentences in this passage
	 */
	public final List<CoreMap> sentences;
	public List<CoreLabel> tokens; 
	public Counter<String> IC;
	public Map<String, List<Integer>> tokenPositions;
	private final HashMap<CoreMap,Integer> sentenceToIndex = new HashMap<CoreMap,Integer>();
	public final Annotation annotation;
	public List<float[]> fwp;
	public List<float[]> fwm;

	/**
	 * Create a passage from a list of sentences(of type CoreMap).
	 *
	 * @param sentences The sentences in the passage
	 */
	public Passage(List<CoreMap> sentences, Annotation annotation){
		this.sentences = sentences;
		this.annotation = annotation;
		int index = 0;

		// initialize sentenceToIndex
		for (CoreMap s : sentences) {
			sentenceToIndex.put(s, index);
			index += 1;
		}
	}

	public int totalSentenceNum() {
		return sentences.size();
	}

	public CoreMap getSentence(int i) {
		return sentences.get(i);
	}

	public List<String> getTokenStrings() {
		this.getTokens();
		List<String> passageTokenStrings = new ArrayList<String>();
		for (CoreLabel token : tokens) {
			passageTokenStrings.add(token.value());
		}
		return passageTokenStrings;
	}


	public List<CoreLabel> getTokens() {
		if (tokens == null) {
			tokens = new ArrayList<CoreLabel>();
			for (CoreMap sentence : sentences) {
				tokens.addAll(sentence.get(CoreAnnotations.TokensAnnotation.class)); 
			}
		}
		return tokens;
	}

	public Counter<String> getIC() {
		if (IC == null) {
			List<String> passageTokenStrings = this.getTokenStrings();
			this.IC = new ClassicCounter<String>(passageTokenStrings);
			for (String key : IC.keySet()) {
				IC.setCount(key, Math.log(1 + 1.0/IC.getCount(key)));
			}
		}
		return IC;
	}

	public Map<String, List<Integer>> getTokenPositions() {
		if (tokenPositions == null) {
			List<String> passageTokenStrings = this.getTokenStrings();
			this.tokenPositions = new HashMap<String, List<Integer>>();
			for (int i=0; i<passageTokenStrings.size(); i++) {
				String tokenString = passageTokenStrings.get(i);
				if (tokenPositions.containsKey(tokenString)) {
					//this token has already emerged before
					tokenPositions.get(tokenString).add(i);
				}
				else {
					//this is the first time this token has emerged
					List<Integer> posi = new ArrayList<Integer>();
					posi.add(i);
					tokenPositions.put(tokenString, posi);
				}
			}
		}
		return tokenPositions;
	}

	public void calculateFw(WordEmbeddingsDict dict) {
		int size = dict.getSize();
		this.fwp = new ArrayList<float[]>(); 
		this.fwm = new ArrayList<float[]>(); 
		
		for (CoreMap sentence : this.sentences) {
			float[] fwp_w = new float[size];
			float[] fwm_w = new float[size];

			for (int i=0; i<size; i++) {
				fwp_w[i] = 0;
				fwm_w[i] = 1;
			}

			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				float[] v = dict.getWordVector(token.value());
				if (v == null) continue;
				for (int i=0; i<size; i++) {
					fwp_w[i] = fwp_w[i] + v[i];
					fwm_w[i] = fwm_w[i] * v[i];
				}
			}
			float len_p = 0;
			float len_m = 0;
			for (int i=0; i<size; i++) {
				len_p += fwp_w[i] * fwp_w[i];
				len_m += fwm_w[i] * fwm_w[i];
			}
			len_p = (float) Math.sqrt(len_p);
			len_m = (float) Math.sqrt(len_m);

			for (int i=0; i<size; i++) {
				fwp_w[i] = (float) (fwp_w[i]/len_p);
				fwm_w[i] = (float) (fwm_w[i]/len_m);
			}

			this.fwp.add(fwp_w);
			this.fwm.add(fwm_w);

		}

	}


	public float[] getFwp(WordEmbeddingsDict dict, int w) {
		if (this.fwp == null) {
			this.calculateFw(dict);
		}
		return this.fwp.get(w);
	}

	public float[] getFwm(WordEmbeddingsDict dict, int w) {
		if (this.fwm == null) {
			this.calculateFw(dict);
		}
		return this.fwm.get(w);
	}
			
			
			

	public String normalPrint(){
		//--Variables
		StringBuilder b = new StringBuilder();
		//--Print CoreMaps
		for(CoreMap s : sentences){
			b.append(s.toString()).append(" ");
		}
		b.append("\n");
		return b.toString();
	}

	public int indexOfSentence(CoreMap s){
		//(try simple get)
		Integer cand = sentenceToIndex.get(s);
		if(cand == null){
			//(populate map)
			for(int i=0; i<sentences.size(); i++){
				if(sentenceToIndex.containsKey(sentences.get(i))){ throw new IllegalStateException("CoreMap equals() collision (not your fault!): " + sentences.get(i)); }
				sentenceToIndex.put(sentences.get(i), i);
			}
			cand = sentenceToIndex.get(s);
		}
		//(error check)
		if(cand == null){ throw new IllegalArgumentException("CoreMap is not in document: " + s); }
		//(return)
		return cand.intValue();
	}

	//--------------
	// SERIALIZATION
	//--------------
	public String encode() {
		/*
			 StringBuilder b = new StringBuilder();
		//(save sentences)
		b.append("sentences:\n");
		for(CoreMap s : sentences){
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

	public static Passage decode(String encoded){
		/*
			 String[] lines = encoded.split("\n");
		//(get sentences)
		if(!lines[0].equals("sentences:")){ throw new IllegalStateException("Could not decode document: " + " offending line: " + lines[0]); }
		int index = 1;
		List<CoreMap> sentences = new ArrayList<CoreMap>();
		while(!lines[index].equals("<end>")){
		sentences.add(CoreMap.decode(lines[index]));
		index += 1;
		}
		return new Passage(sentences);
		*/
		return null;
	}
}
