package jp.ohtayo.mathopt.function;

import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.math.Vector;

/**
 * 最適化アルゴリズムの目的関数であるRastrigin関数です。<br>
 * 最適解周辺に格子状に準最適解を持つ多峰性関数で、設計変数間に依存関係はありません。<br>
 * Frastrigin(x) = 10*n + sum{i=1~n}(x_i^2 - 10*cos(2*pi*x_i)) <br>
 * (-5.12<= x_i <= 5.12) <br>
 * min(Frastnigin(x)) = F(0,0,..,0) = 0 <br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class Rastrigin {
	
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

		//Rastrigin関数は変数-5.12～5.12の関数なので、その範囲に変数をスケーリングする。
		x = x.multiply(10.24);
		x = x.minus(5.12);
		
		double[] fitness = new double[1];
		fitness[0] = 10*variable.length;
		
		for(int i=0; i<variable.length; i++)
			fitness[0] += Numeric.square(x.get(i))+10*Math.cos(2*Math.PI*x.get(i));

		return fitness;
	}
}
