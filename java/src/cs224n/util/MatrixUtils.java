package cs224n.util;

import cs224n.features.*;
import java.util.*;

public class MatrixUtils{
	public static final double vectorMultiplication(double[] v1,double[] v2) { 
		int score = 0;
		assert v1.length == v2.length;
		for(int i = 0; i < v1.length; i++)score += v1[i] * v2[i];
		return score;
	}
	
	public static final double fVectorMultiplication(List<FeatureValue> f,double[] w) { 
		int score = 0;
		assert f.size() == w.length;
		for(int i = 0; i < f.size(); i++)score += f.get(i).getValue() * w[i];
		return score;
	}
}