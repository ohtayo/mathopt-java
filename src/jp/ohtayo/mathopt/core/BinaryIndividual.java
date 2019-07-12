package jp.ohtayo.mathopt.core;

import jp.ohtayo.commons.util.Cast;
import jp.ohtayo.commons.math.Vector;

/**
 * 遺伝的アルゴリズム(GA)の個体を表すクラスです。<br>
 * 個体の変数とコード化された染色体、適応度のフィールドを持ちます。<br>
 * 変数variableは0～1のdouble型配列で、染色体はvariableを2進数化したものです。<br>
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class BinaryIndividual extends Individual {
	
	/** 個体の染色体	*/	private int[] chromosome;
	/** 2進数用マスク	*/	public int bitmask;
	/** ビット数		*/	private int numberOfBits;

	/**
	 * デフォルトコンストラクタ<br>
	 * 変数を指定して初期化し適応度を計算します。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfBits 染色体のビット数
	 * @param numberOfObjectives 目的数
	 */
	public BinaryIndividual(int numberOfVariables, int numberOfBits, int numberOfObjectives)
	{
		//変数の初期化
		variable = new double[numberOfVariables];
		chromosome = new int[variable.length];
		fitness = new double[numberOfObjectives];
		this.numberOfBits = numberOfBits;
		
		//ビットマスク
		bitmask = (int)(Math.pow(2, this.numberOfBits)-1);
	}
	
	/**
	 * ビット数を返します。
	 * @return ビット数
	 */
	public int getNumberOfBits()
	{
		return this.numberOfBits;
	}
	
	/**
	 * コード化します。<br>
	 */
	private void coding()
	{
		double[] temp = new Vector(variable).multiply(Math.pow(2,this.numberOfBits)-1).get();	//0～1を0～2^bitsの配列に変換
		chromosome = Cast.doubleToInt(temp);
	}
	
	/**
	 * デコード化します。<br>
	 */
	private void decoding()
	{
		Vector temp = new Vector(Cast.intToDouble(chromosome));	//doubleに変換
	    variable = temp.division(Math.pow(2,this.numberOfBits)-1).get();	//0～2^numberofbitsの配列を0～1の配列に変換
	}
	
	/**
	 * 変数の値を更新します。<br>
	 * @param value 変数の値
	 */
	@Override
	public void setVariable(double[] value)
	{
		System.arraycopy(value, 0, variable, 0, value.length);
	    coding();
	}
	
	/**
	 * 染色体配列の値を設定します。<br>
	 * @param value 染色体の値の配列
	 */
	public void setChromosome(int[] value)
	{
		System.arraycopy(value, 0, chromosome, 0, value.length);
	    decoding();
	}
	/**
	 * 染色体の配列を返します。<br>
	 * @return 染色体配列
	 */
	public int[] getChromosome()
	{
	    return chromosome;
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
		Individual result = new BinaryIndividual(variable.length, numberOfBits, fitness.length);
		
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
	    s += "chromosome: ";
	    for (int i = 0; i < chromosome.length; ++i)
	        s += Integer.toString(chromosome[i]) + " ";
	    s += "\r\n";
	    s += "Fitness = " + new Vector(fitness).toString() + "\r\n";
	    s += "==========================\r\n";
	    return s;
	}
}
