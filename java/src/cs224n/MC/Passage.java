package cs224n.MC;

import cs224n.util.Decodable;


import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.*;


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
  public List<CoreMap> tokens; 
  private final HashMap<CoreMap,Integer> sentenceToIndex = new HashMap<CoreMap,Integer>();

  /**
   * Create a passage from a list of sentences(of type CoreMap).
   *
   * @param sentences The sentences in the passage
   */
  public Passage(List<CoreMap> sentences){
    this.sentences = sentences;
	int index = 0;

	// initialize sentenceToIndex
	for (CoreMap s : sentences) {
	  sentenceToIndex.put(s, index);
	  index += 1;
	}
  }

  public List<CoreMap> getTokens() {
	if (tokens == null) {
	  for (CoreMap sentence : sentences) {
		tokens.addAll(sentence.get(CoreAnnotations.TokensAnnotation.class)); 
	  }
	}
	return tokens;
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
