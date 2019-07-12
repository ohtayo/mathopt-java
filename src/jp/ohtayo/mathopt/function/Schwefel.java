package jp.ohtayo.mathopt.function;

import jp.ohtayo.commons.math.Vector;

/**
 * 最適化アルゴリズムの目的関数であるSchwefel関数です。<br>
 * 最適解が探索領域の境界付近にある多峰性関数で、設計変数間に依存関係はありません。<br>
 * 速いうちに大域的最適解を求めないと局所最適解に収束します。
 * Fschwefel(x) = sum{i=1~n}( -x_i * sin(sqrt(abs(x_i))) ) <br>
 * (-512<= x_i <= 512) <br>
 * min(Fschwefel(x)) = F(421,421,..,421) = -418.982887 <br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class Schwefel {
	
	/** 最大値の定義	*/	private static double[] maxValue = {1500.0};
	/** 最小値の定義	*/	private static double[] minValue = {-419.0};

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

		//Schwefel関数は変数-512～512の関数なので、その範囲に変数をスケーリングする。
		x = x.multiply(1024);
		x = x.minus(512);
		
		double[] fitness = new double[1];
		fitness[0] = 418.98288727*x.length();
		double temp = 1;
		
		for(int i=0; i<variable.length; i++){
			fitness[0] -= x.get(i)*Math.sin(Math.sqrt(Math.abs(x.get(i))));
		}
		fitness[0] -= temp;
		
		return fitness;
	}
}
