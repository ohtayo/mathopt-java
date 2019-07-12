package jp.ohtayo.mathopt.function;

import jp.ohtayo.commons.math.Vector;

/**
 * 多目的最適化のベンチマーク関数のクラスです。<br>
 * DTRZ3ベンチマークを計算します。<br>
 * @author ohtayo <ohta.yoshihiro@outlook.jp></>
 */
public class DTLZ3 {
	
	/** 最大値の定義	*/	private final static double[] maxValue = {10.0, 10.0};
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
	 * 制約条件を満たしているか判定し、<br>
	 * double(0以下なら制約が満たされている。0超過で大きくなるほど制約より遠い)を返します。<br>
	 * @return 
	 */
	public static double constraint(double[] variable, double[] score)
	{
		return 0.0;	//制約なし
	}
	
	/**
	 * 目的関数を計算します<br>
	 * @param variable 変数
	 * @return 目的関数の値
	 */
	public static double[] execute(double[] variable)
	{
		int objectiveNumber = 2;
		int difficulty = variable.length - objectiveNumber +1;	//難易度係数
		
		double[] fitness = new double[objectiveNumber];
		fitness[0] = 0;

		Vector tmp = new Vector(variable);
		Vector x = tmp.get( objectiveNumber-1, variable.length-objectiveNumber+1).minus(0.5);
		//double[] x = Vector.minus( Vector.slice(variable,objectiveNumber-1,variable.length-objectiveNumber+1), 0.5 );
		double[] temp = new double[x.length()];
		for(int i=0; i<x.length(); i++)
			temp[i] = Math.cos(20*Math.PI*x.get(i));
		
		double g = 100 * ( difficulty + x.square().minus(new Vector(temp)).sum());
		//double g = 100 * ( difficulty + Numeric.sum( Vector.minus(Numeric.square(x), temp) ) );
		double a = 1+g;
		
		for (int i=0; i<objectiveNumber; i++)
		{
			if(i>0)
			{
				a = a* Math.cos(variable[i-1]* Math.PI/2);
			}
			double value = a;
			if(i<objectiveNumber-1)
			{
				value = value * Math.sin(variable[i] * Math.PI/2);
			}
			fitness[i] = value;
		}
		
		return fitness;
	}
	
}
