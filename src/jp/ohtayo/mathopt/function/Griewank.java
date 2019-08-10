package jp.ohtayo.mathopt.function;

import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.math.Vector;

/**
 * 最適化アルゴリズムの目的関数であるGriewank関数です。<br>
 * 設計変数間に依存関係を持つ多峰性関数です。<br>
 * 準最適解を求めるのは容易ですが、局所最適解が多数存在するため、大域的最適解を求めるのが困難です。<br>
 * Fgriewank(x) = 1+sum{i=1~n}( x_i^2/4000 )- multiply{i=1~n}( cos(x_i/sqrt(i)) )<br>
 * (-512&lt;= x_i &lt;= 512) <br>
 * min(Frastnigin(x)) = F(0,0,0,..,0) = 0 <br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class Griewank {
	
	/** 最大値の定義	*/	private static double[] maxValue = {200.0};
	/** 最小値の定義	*/	private static double[] minValue = {0.0};

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
	 * @param variable 変数
	 * @return 目的関数の値
	 */
	public static double[] execute(double[] variable){

		Vector x = new Vector(variable);
		
		//Griewank関数は変数-512～512の関数なので、その範囲に変数をスケーリングする。
		x = x.multiply(1024);
		x = x.minus(512);
		
		double[] fitness = new double[1];
		fitness[0] = 1.0;
		double temp = 1.0;
		
		for(int i=0; i<variable.length; i++){
			fitness[0] += Numeric.square(x.get(i))/4000;
			temp *= Math.cos(x.get(i)/Math.sqrt(i+1));
		}
		fitness[0] -= temp;
		
		return fitness;
	}
}
