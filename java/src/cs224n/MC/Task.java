package cs224n.MC;

import cs224n.util.Decodable;

import edu.stanford.nlp.pipeline.*;

import java.io.Serializable;
import java.util.*;

/**
 * A task includes the passage itself, the questions with choices and answers
 *
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public class Task implements Serializable, Decodable {
  private static final long serialVersionUID = 1L;
  /**
   * A unique identifier for this document
   */
  public final String id;
  /**
   * Author of the document
   */
  public final String author;
  /**
   * Word time(s) of the document
   */
  public final Integer time;
  /**
   * The passage of this document
   */
  public final Passage passage;
  /**
   * The questions of the document, A question usually consists of a stem
   * and four options
   */
  private List<Question> questions = null;

  /**
   * Create a document from an id and a string of text.
   * You're not likely to have to use this method.
   *
   * @param id The unique id of the document
   * @param sentences The sentences in the document
   */
  public Task(String id, String author, Integer time, Passage passage, List<Question> questions){
	this.id = id;
    this.passage = passage;
    this.questions = questions;
	this.author = author;
	this.time = time;
  }

  public List<Question> getQuestions() {
	return questions;
  }

  public Passage getPassage() {
	return passage;
  }

  /**
   * Print the document
   */
  public String normalPrint(){
    //--Variables
    StringBuilder b = new StringBuilder();
	//--Print id
	b.append("id:  ").append(id).append("\n");
    //--Print passage
    b.append("Passage:  ").append(passage.normalPrint());
	//--Print questions
    b.append("Questions:  \n");
	for (Question q : questions){
	  b.append(q.normalPrint());
    }
    return b.toString();
  }


  //--------------
  // SERIALIZATION
  //--------------
  public String encode() {
	/*
    StringBuilder b = new StringBuilder();
    //(save id)
    b.append(this.id).append("\n");
    //(save author)
    b.append(this.author).append("\n");
    //(save time)
    b.append(Integer.toString(this.time)).append("\n");
    //(save passage)
    b.append("passage:\n");
	b.append(passage.encode()).append("\n");
	//(save questions)
    for(Question q : questions){
      b.append(q.encode()).append("\n");
    }
    //(end)
    b.append("<end>");
    //(error check)
    if(!decode(b.toString()).equals(this)){
      throw new IllegalStateException("Did not encode Task properly");
    }
    return b.toString();
	*/
	return null;
  }

  public static Task decode(String encoded){
	/*
    String[] lines = encoded.split("\n");
    //(get id)
    String id = lines[0];
    //(get id)
    String author = lines[1];
    //(get id)
    Integer time = Integer.valueOf(lines[2]);
	//(get passage)
	Passage passage = Passage.decode(lines[3]);
    //(get sentences)
	List<Question> questions = new ArrayList<Question>();
    if(!lines[3].equals("sentences:")){ throw new IllegalStateException("Could not decode document: " + id + " offending line: " + lines[3]); }
    int index = 4;
    List<Sentence> sentences = new ArrayList<Sentence>();
    while(!lines[index].equals("<end>")){
      sentences.add(Sentence.decode(lines[index]));
      index += 1;
    }
    return new Task(id, author, time, passage, questions);
	*/
	return null;
  }
}
