package cs224n.util;
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

public class CorefResolWorker{
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
