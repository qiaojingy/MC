package cs224n.MCsystems;

import java.util.*;

public class Util{
	public static final String[] STOPWORD_LIST = new String[] { 
		"a", "an", "and", "are", "as", "at", "be", "but", "by",
		"for", "if", "in", "into", "is", "it",
		"no", "not", "of", "on", "or", "such",
		"that", "the", "their", "then", "there", "these",
		"they", "this", "to", "was", "will", "with" };
	public static final Set<String> STOPWORD_SET = new HashSet<String>(Arrays.asList(STOPWORD_LIST));
	
	public static final void removeStopWords(Set<String> set){
		//using stopword list from lucene's stop filter
		set.removeAll(STOPWORD_SET);
	}
}


