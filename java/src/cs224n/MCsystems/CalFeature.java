package cs224n.MCsystems;
import cs224n.MC.*;

import java.util.*;
import java.lang.Math;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.dcoref.*;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.*;
import edu.stanford.nlp.dcoref.CorefChain.*;
import edu.stanford.nlp.ling.CoreAnnotations;

public class CalFeature{
	public static final double calBaselineOneScore(List<String> passageTokenStrings,Counter<String> IC,List<String> Q,List<String> a){
		// Calculate S = A_i U Q
		List<String> S = new ArrayList<String>();
		S.addAll(Q);
		S.addAll(a);

		// Calculate sw_i
		int S_size = S.size();
		double score = 0;
		double maxScore = 0;
		for (int j=0; j<passageTokenStrings.size()-S.size()+1; j++){
			if (j == 0) {
				for (int w=0; w<S_size; w++){
					String token = passageTokenStrings.get(j+w);
					if (S.contains(token)) {
						score += IC.getCount(token);
					}
				}
				maxScore = score;
			}
			else {
				String token = passageTokenStrings.get(j-1);
				if (S.contains(token)){
					score -= IC.getCount(passageTokenStrings.get(j-1));
				}
				token = passageTokenStrings.get(j+S_size-1);
				if (S.contains(token)){
					score += IC.getCount(passageTokenStrings.get(j+S_size-1));
				}
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		System.out.print("BaselineOne return: ");
		System.out.println(maxScore);
		return maxScore;
	}
	
	public static final double calDistancePunish(List<String> passageTokenStrings,HashMap<String,List<Integer>> tokenPosi,List<String> Q,List<String> a){
		//S_Q = (Q intersect PW) minus U
		Set<String> set_Q = new HashSet<String>();
		set_Q.addAll(Q);
		Set<String> set_PW = new HashSet<String>();
		set_PW.addAll(passageTokenStrings);
		Set<String> set_SQ = new HashSet<String>(set_Q);
		set_SQ.retainAll(set_PW);
		Util.removeStopWords(set_SQ);
		//S_Ai = ((Ai intersect PW) minus Q) minus U
		Set<String> set_Ai = new HashSet<String>();
		set_Ai.addAll(a);
		Set<String> set_SAi = new HashSet<String>(set_Ai);
		set_SAi.retainAll(set_PW);
		set_SAi.removeAll(set_Q);
		Util.removeStopWords(set_SAi);
		if(set_SQ.size() == 0 || set_SAi.size() == 0){
			System.out.print("Return distance punishment: ");
			System.out.println(1);
			return 1;
		}
		else{
			double ret = (double)Util.getSmallestDistance(tokenPosi,set_SQ,set_SAi) / (passageTokenStrings.size()-1);
			System.out.print("Return distance punishment: ");
			System.out.println(ret);
			return ret;
		}
	}
	
	public static final List<String> doCorefResolution(Annotation annotation){
		
		Map<Integer,CorefChain> corefs = annotation.get(CorefChainAnnotation.class);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		List<String> resolved = new ArrayList<String>();
		for(CoreMap sentence:sentences){
			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
			for(CoreLabel token:tokens){
				Integer corefClustId = token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
				CorefChain chain = corefs.get(corefClustId);
				if(chain == null)resolved.add(token.word());
				else{
					int sentINdx = chain.getRepresentativeMention().sentNum - 1;
					CoreMap corefSentence = sentences.get(sentINdx);
					List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);
					CorefMention reprMent = chain.getRepresentativeMention();
					if(token.index() < reprMent.startIndex || token.index() > reprMent.endIndex){
						for(int i = reprMent.startIndex; i < reprMent.endIndex; i++){
							CoreLabel matchedLabel = corefSentenceTokens.get(i-1);
							resolved.add(matchedLabel.word());
						}
					}
					else resolved.add(token.word());
				}
			}
		}
	    String resolvedStr ="";
	    System.out.println();
	    for (String str : resolved) {
	        resolvedStr+=str+" ";
	    }
	    System.out.println(resolvedStr);
		
		return resolved;
	}
}