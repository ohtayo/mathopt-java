package jp.ohtayo.mathopt.core;

import jp.ohtayo.mathopt.function.ObjectiveFunction;
import jp.ohtayo.commons.math.Vector;

/**
 * 遺伝的アルゴリズムに利用する個体の抽象クラスです。
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public abstract class Individual {
	
	/** 個体の変数		*/	protected double[] variable;
	/** 個体の適応度	*/	protected double[] fitness;

	/**
	 * 目的関数の計算と適応度の更新を行います<br>
	 * @param nameOfObjectiveFunction 目的関数の名前
	 */
	public void evaluate(String nameOfObjectiveFunction)
	{
		fitness = (double[])ObjectiveFunction.execute(variable, nameOfObjectiveFunction);
	}
	
	/**
	 * 変数の値を更新します。<br>
	 * @param value 変数の値
	 */
	public void setVariable(double[] value)
	{
		System.arraycopy(value, 0, variable, 0, value.length);
	}
	
	/**
	 * 変数の値を返します。<br>
	 * @return 変数の値
	 */
	public double[] getVariable()
	{
	    return variable;
	}

	/**
	 * 変数の数を返します．<br>
	 * @return 変数の数
	 */
	public int getNumberOfVariables() { return variable.length; }
	
	/**
	 * 適応度の値を更新します。<br>
	 * @param value 適応度の値
	 */
	public void setFitness(double[] value)
	{
		System.arraycopy(value, 0, fitness, 0, value.length);
	}
	
	/**
	 * 適応度の値を返します。<br>
	 * @return 適応度の値
	 */
	public double[] getFitness()
	{
	    return fitness;
	}

	/**
	 * 適応度の数を返します．<br>
	 * @return
	 */
	public int getNumberOfObjectives() { return fitness.length; }
		
	/**
	 * 他の個体の値を自身にコピーします
	 * @param i コピー先個体
	 */
	abstract public void copy(Individual i);
	
	/**
	 * 自身を別の個体iにコピーします
	 * @return コピー先個体
	 */
	abstract public Individual copy();
	
	/**
	 * 個体の中身を文字列で表示する。<br>
	 */
	abstract public String toString();
	
	/**
	 * 変数が同一であるかを確認する。<br>
	 * @param individual 確認したい個体
	 * @return 同一であればtrue
	 */
	public boolean equals(Individual individual)
	{
		//変数の差の配列の和が0なら同一
		return new Vector(variable).minus(new Vector(individual.variable)).sum() == 0;
	}

}
