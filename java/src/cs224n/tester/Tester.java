package cs224n.tester;

import cs224n.MCsystems.MCSystem;
import cs224n.MC.*;
import cs224n.util.*;

import java.util.*;

/**
 * Some simple tests for debugging
 *
 *
 */

public class Tester {
  private static String sentenceExample = "Today, we reclaim our home world!";

  public static void main(String[] args) {
	//(print header)
    System.out.println("-------------------");
    System.out.println("Tester");
    System.out.println("-------------------");
	String[] arr = sentenceExample.split("\\s+");
	List<String> words = new ArrayList<String>();
	for (String s : arr) {
      words.add(s);
	}
    Sentence s = new Sentence(words);
	System.out.println(s.gloss());
	Sentence q = new Sentence("What are we bound by?");
	List<Sentence> stem = new ArrayList<Sentence>();
	stem.add(q);
	List<String> options = new ArrayList<String>();
	options.add("Communism");
	options.add("Christian");
	options.add("Confucius");
	options.add("The glory of Khali");

	Question question = new Question(Question.QuestionType.ONE, stem, options);
	System.out.println(question.normalPrint());
	
	Sentence ps1 = new Sentence("The swarm brought ruin to our world. ");
	Sentence ps2 = new Sentence("Our pround people became refugees. ");
	Sentence ps3 = new Sentence("And yet they could not shatter our unity. ");
	Sentence ps4 = new Sentence("For we are bound by the Khali: ");
	Sentence ps5 = new Sentence("The sacred union of our every thought and emotion. ");
	List<Sentence> sentences = new ArrayList<Sentence>();
	sentences.add(ps1);
	sentences.add(ps2);
	sentences.add(ps3);
	sentences.add(ps4);
	sentences.add(ps5);
	Passage passage = new Passage(sentences);
	System.out.println(passage.normalPrint());
	List<Question> questions = new ArrayList<Question>();
	questions.add(question);

	Document document = new Document("Trailer", "Blizzard Entertainment", 999, 
		passage, questions);
	System.out.println(document.normalPrint());
  } 
}

