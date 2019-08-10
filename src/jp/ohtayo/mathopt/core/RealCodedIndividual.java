package jp.ohtayo.mathopt.core;

import jp.ohtayo.commons.math.Vector;

/**
 * 遺伝的アルゴリズム(GA)の個体を表すクラスです。<br>
 * 個体の変数とコード化された染色体、適応度のフィールドを持ちます。<br>
 * 変数variableは0～1のdouble型配列で、染色体はvariableを2進数化したものです。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class RealCodedIndividual extends Individual{
	
	/**
	 * デフォルトコンストラクタ<br>
	 * 変数を指定して初期化し適応度を計算します。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfObjectives 目的数
	 */
	public RealCodedIndividual(int numberOfVariables, int numberOfObjectives)
	{
		//変数の初期化
		variable = new double[numberOfVariables];
		fitness = new double[numberOfObjectives];
	}

	/**
	 * 他の個体の値を自身にコピーします
	 * @param i コピー先個体
	 */
	public void copy(Individual i)
	{
		this.setVariable(i.getVariable());	//変数のコピー
		System.arraycopy(i.fitness, 0, this.fitness, 0, i.fitness.length);	//適応度のコピー
	}
	
	/**
	 * 自身を別の個体iにコピーします
	 * @return コピー先個体
	 */
	public Individual copy()
	{
		//コピー先を用意
		Individual result = new RealCodedIndividual(variable.length, fitness.length);
		
		result.setVariable(this.variable);	//変数のコピー
		System.arraycopy(this.fitness, 0, result.fitness, 0, this.fitness.length);	//適応度のコピー

		return result;
	}
	
	/**
	 * 個体の内容を文字列として返します。<br>
	 */
	public String toString()
	{
	    String s = "";
	    s += "==========================\r\n";
	    s += "variable: ";
	    for (int i = 0; i < variable.length; ++i)
	        s += Double.toString(variable[i]) + " ";
	    s += "\r\n";
	    s += "Fitness = " + new Vector(fitness).toString() + "\r\n";
	    s += "==========================\r\n";
	    return s;
	}
}
