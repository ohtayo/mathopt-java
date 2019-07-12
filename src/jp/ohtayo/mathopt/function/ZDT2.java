package jp.ohtayo.mathopt.function;

/**
 * 多目的最適化のベンチマーク関数のクラスです。<br>
 * ZDT2ベンチマークを計算します。<br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class ZDT2 {
	
	/** 最大値の定義	*/	private final static double[] maxValue = {1.0, 10.0};
	/** 最小値の定義	*/	private final static double[] minValue = {0.0, 0.0};

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
	 * 目的関数を計算します<br>
	 * @param variable 変数
	 * @return 目的関数の値
	 */
	public static double[] execute(double[] variable)
	{
		double[] fitness = new double[2];
		fitness[0] = variable[0];
		
		double sum = 0.0;
		for(int i=1; i<variable.length; i++){
			sum += variable[i];
		}
		double g = 1+ 9/(variable.length-1) * sum;
		fitness[1] = g * ( 1 - (variable[0]/g) * (variable[0]/g) );
		
		return fitness;
	}
	
}
