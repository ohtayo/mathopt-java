package jp.ohtayo.mathopt.core;

import jp.ohtayo.commons.random.Random;

/**
 * 個体群を取り扱うクラスです．
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class Population {
	
	/** 個体群=個体配列 */	public Individual[] individual;
	
	/**
	 * デフォルトコンストラクタ<br>
	 * 個体数を指定してインスタンスを生成します。<br>
	 * @param numberOfPopulations 粒子数
	 */
	public Population(int numberOfPopulations)
	{
		individual = new Individual[numberOfPopulations];
	}

	/**
	 * 目的数を返します．<br>
	 * @return 目的数
	 */
	public int getNumberOfObjectives()
	{
		if (individual.length != 0)
			return individual[0].getNumberOfObjectives();
		else
			return 0;
	}

	/**
	 * 変数の数を返します．<br>
	 * @return 変数の数
	 */
	public int getNumberOfVariables()
	{
		if (individual.length != 0)
			return individual[0].getNumberOfVariables();
		else
			return 0;
	}

	/**
	 * 個体数を返します．
	 * @return 個体数
	 */
	public int getPopulationSize()	{ return individual.length; }

	/**
	 * 個体群の変数を初期化します。ビット数を指定してビットコーディングします。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfBits ビット数
	 * @param numberOfPopulations 個体数
	 * @param numberOfObjectives 目的数
	 * @param nameOfObjectiveFunction 目的関数名
	 */
	public void initialize(int numberOfVariables, int numberOfBits, int numberOfPopulations, int numberOfObjectives, String nameOfObjectiveFunction)
	{
		Random random = new Random();
		individual = new BinaryIndividual[numberOfPopulations];
		double[] value = new double[numberOfVariables];
		
		for (int i = 0; i < individual.length; i++)
		{
			value = random.rand(value.length);	//0～1の乱数配列を生成
			individual[i] = new BinaryIndividual(numberOfVariables, numberOfBits, numberOfObjectives);
			individual[i].setVariable(value);
			individual[i].evaluate(nameOfObjectiveFunction);
		 }
	}
	
	/**
	 * 個体群の変数を初期化します。実数値で個体群を作ります。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfPopulations 個体数
	 * @param numberOfObjectives 目的数
	 * @param nameOfObjectiveFunction 目的関数名
	 */
	public void initialize(int numberOfVariables, int numberOfPopulations, int numberOfObjectives, String nameOfObjectiveFunction)
	{
		Random random = new Random();
		individual = new RealCodedIndividual[numberOfPopulations];
		double[] value = new double[numberOfVariables];
		
		for (int i = 0; i < individual.length; i++)
		{
			value = random.rand(value.length);	//0～1の乱数配列を生成
			individual[i] = new RealCodedIndividual(numberOfVariables, numberOfObjectives);
			individual[i].setVariable(value);
			individual[i].evaluate(nameOfObjectiveFunction);
		 }
	}
	
	/**
	 * 粒子を初期化します。ビット数を指定してビットコーディングします。<br>
	 * (配列のメモリ確保のみ。通常は使用しないでください。)<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfBits ビット数
	 * @param numberOfObjectives 目的関数の数
	 */
	public void initialize(int numberOfVariables, int numberOfBits, int numberOfObjectives)
	{
		for (int i=0; i<individual.length; i++)
		{
			individual[i] = new BinaryIndividual(numberOfVariables, numberOfBits, numberOfObjectives);
		}
	}
	/**
	 * 粒子を初期化します。実数値で個体群を作ります。<br>
	 * (配列のメモリ確保のみ。通常は使用しないでください。)<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfObjectives 目的関数の数
	 */
	public void initialize(int numberOfVariables, int numberOfObjectives)
	{
		for (int i=0; i<individual.length; i++)
		{
			individual[i] = new RealCodedIndividual(numberOfVariables, numberOfObjectives);
		}
	}
	
	/**
	 * 個体群の変数値を行列で返します。
	 * @return 変数行列
	 */
	public double[][] getVariables()
	{	
		double[][] variables = new double[individual.length][individual[0].getVariable().length];
		for (int i=0; i<individual.length; i++)
		{
			System.arraycopy(individual[i].getVariable(), 0, variables[i], 0, individual[i].getVariable().length);
		}
		return variables;
	}
	/**
	 * 個体群の位置を行列で設定します。
	 * @param variables 個体群の位置行列
	 */
	public void setVariables(double[][] variables)
	{	
		for (int i=0; i<individual.length; i++)
		{
			individual[i].setVariable(variables[i]);
		}
	}
		
	/**
	 * 個体群をまとめて評価します。
	 * @param nameOfObjectiveFunction 評価したい目的関数名
	 */
	public void evaluate(String nameOfObjectiveFunction)
	{
		for (int i=0; i<individual.length; i++)
		{
			individual[i].evaluate(nameOfObjectiveFunction);
		}
	}
	
	/**
	 * 個体群の適応度を行列で返します。
	 * @return 個体群の適応度行列
	 */
	public double[][] getFitness()
	{	
		double[][] fitness = new double[individual.length][individual[0].fitness.length];
		for (int i=0; i<individual.length; i++)
		{
			System.arraycopy(individual[i].getFitness(), 0, fitness[i], 0, individual[i].getFitness().length);
		}
		return fitness;
	}	
	/**
	 * 個体の適応度を行列で設定します。
	 * @param fitness 個体の適応度行列
	 */
	public void setFitness(double[][] fitness)
	{	
		for (int i=0; i<individual.length; i++)
		{
			individual[i].setFitness(fitness[i]);
		}
	}
	
	/**
	 * ２つの個体群を合成して1つの個体群にします。
	 * @param target 後ろに接続する個体群
	 * @return 合成した個体群
	 */
	public Population add(Population target)
	{
		Population result = new Population(individual.length+target.individual.length);

		for (int i=0; i<individual.length; i++)
		{
			result.individual[i]=individual[i].copy();
		}
		for (int i=individual.length; i<result.individual.length; i++)
		{
			result.individual[i]=target.individual[i-individual.length].copy();
		}		
		return result;
	}
	
	/**
	 * 個体群のうち一部を取り出して返します。
	 * @param index 抽出する個体のインデックス
	 * @return 抽出した個体群
	 */
	public Population picup(int[] index)
	{
		Population result = new Population(index.length);

		for (int i=0; i<index.length; i++)
		{
			for (int j=0; j<individual.length; j++)
			{
				if(j == index[i])
				{
					result.individual[i]=individual[j].copy();
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 自身に別の個体群のパラメータをコピーします。(別メモリ確保)
	 * @param basis コピー元の個体群
	 */
	public void copy(Population basis)
	{	
		for (int i=0; i<basis.individual.length; i++)
		{
			this.individual[i] = basis.individual[i].copy();
		}
	}
	
	/**
	 * 自身のパラメータを別の個体群にコピーします。(別メモリ確保)
	 * @return コピーした個体群
	 */
	public Population copy()
	{	
		Population result = this;
		for (int i=0; i<this.individual.length; i++)
		{
			result.individual[i] = this.individual[i].copy();
		}
		return result;
	}
	
	/**
	 * 個体群の内容を文字列として返します。
	 * @return 個体群の内容の文字列
	 */
	public String toString()
	{
		String str = "";
		for(int i=0; i<individual.length; i++)
		{
			str += String.valueOf(i) + "\r\n" + individual[i].toString();
		}
		return str;
	}
}
