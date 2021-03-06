package cs224n.MCsystems;

import cs224n.MC.*;

import edu.stanford.nlp.util.*;

import java.util.List;


/**
 * The framework class for building a machine comprehension system.
 *
 *
 */

public interface MCSystem {
	public List<Integer> getWResult();
	public void train(List<Task> tasks,List<List<String>> goldAnswerLists);
	public List<String> runMC(Task task);
}
