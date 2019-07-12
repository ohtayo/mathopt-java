package jp.ohtayo.mathopt.function;

import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.math.Vector;

/**
 * 最適化アルゴリズムの目的関数であるRosenbrock関数です。<br>
 * 設計変数間に依存関係を持つ単峰性関数です。<br>
 * Frosenbrock(x) = sum{i=1~n-1}( 100*(x_i+1-x_i^2)^2 + (1-x_i)^2 )<br>
 * (-2.048<= x_i <= 2.048) <br>
 * min(Frastnigin(x)) = F(1,1,1,..,1) = 0 <br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class Rosenbrock {
	
	/** 最大値の定義	*/	private static double[] maxValue = {100.0};
	/** 最小値の定義	*/	private static double[] minValue = {0.0
		};

	/**
	 * 最大値を返します。<br>
	 * @return 最大値
	 */
	public static double[] getMaxValue()
	{
		return maxValue;
	}
	/**
	 * 最小値を返します。<br>
	 * @return 最小値
	 */
	public static double[] getMinValue()
	{
		return minValue;
	}
	
	/**
	 * 目的関数を計算します。<br>
	 * @param variable 変数(0～1)
	 * @return 目的関数の値
	 */
	public static double[] execute(double[] variable){

		Vector x = new Vector(variable);

		//変数をスケーリングする。
		x = x.multiply(4.096);
		x = x.minus(2.048);
		
		double[] fitness = new double[1];
		fitness[0] = 0;
		
		//文献によってRosenbrock関数の書き方が以下の２パターンに別れる。とりあえず下を採用。
		//for(int i=0; i<x.length()-1; i++)
		//	result[0] += (100 * Numeric.square( x.get(i+1)-Numeric.square(x.get(i)) ) + Numeric.square( 1-x.get(i) ) );
		
		for(int i=1; i<x.length(); i++)
			fitness[0] += (100 * Numeric.square( x.get(0)-Numeric.square(x.get(i)) ) + Numeric.square( 1-x.get(i) ) );

		return fitness;
	}
}
