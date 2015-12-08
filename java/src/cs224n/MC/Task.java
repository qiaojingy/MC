package cs224n.MC;

import cs224n.util.Decodable;

import java.io.Serializable;
import java.util.*;

/**
 * A task includes the passage itself, the questions with choices and answers
 *
 * @author Qiaojing Yan (qiaojing at stanford.edu), Yixin Wang (wyixin at stanford.edu)
 */
public class Task implements Serializable {
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
   * Print the task 
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

	public String toString() {
		return this.normalPrint();
	}

}
