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
	
	//given a passage, parse the passage and return a HashMap with tokens as keys, 
	//and token positions as a list of integers
	public static final HashMap<String,List<Integer>> tokenPosiInPassage(List<String> tokens){
		HashMap<String,List<Integer>> ret = new HashMap<String,List<Integer>>();
		for(int i = 0; i < tokens.size(); i++){
			if(ret.containsKey(tokens.get(i))){
				//this token has already emerged before
				ret.get(tokens.get(i)).add(i);
			}
			else{
				//this is the first time this token has emerged
				List<Integer> posi = new ArrayList<Integer>();
				posi.add(i);
				ret.put(tokens.get(i),posi);
			}
		}
		return ret;
	}
	
	//for baseline two, get the smallest distance of any two occurances of a certain word in the passage
	public static final int getSmallestDistance(HashMap<String,List<Integer>> tokenPosi,Set<String> set_SQ,Set<String> set_SAi){
		int min = Integer.MAX_VALUE;
		for(String q:set_SQ){
			for(String a:set_SAi){
				for(int i:tokenPosi.get(q)){
					for(int j:tokenPosi.get(a)){
						if(Math.abs(i-j) < min)min = Math.abs(i-j);
					}
				}
			}
		}
		return min+1;
	}
}


