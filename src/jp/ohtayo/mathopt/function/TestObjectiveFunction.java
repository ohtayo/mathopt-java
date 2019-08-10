package jp.ohtayo.mathopt.function;

/**
 * 最適化アルゴリズムの目的関数のサンプルクラスです。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class TestObjectiveFunction {
	
	/** 最大値の定義	*/	private static double[] maxValue = {10.0};
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
	 * @return 目的関数の評価値
	 */
	public static double[] execute(double[] variable){

		double[] fitness = new double[1];
		fitness[0] = 3.0;
		
		for(int i=0; i<variable.length; i++)
			fitness[0] += variable[i] * variable[i];
		
		return fitness;
	}
}
