package jp.ohtayo.mathopt.core;

import jp.ohtayo.commons.random.Random;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;

/**
 * 滑降シンプレックス法の探索点集合(シンプレックス)を表すクラスです。
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class Simplex extends Population {

	/** 探索点集合の重心 */	public Vertex xg;	// 探索点集合の重心
	
	/**
	 * コンストラクタ<br>
	 * 
	 * @param numberOfPopulations 探索点数
	 */
	public Simplex(int numberOfPopulations) {
		super(numberOfPopulations);
		individual = new Vertex[numberOfPopulations];	//PopulationのindividualをVertexで定義し直す。
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
		individual = new Vertex[numberOfPopulations];
		double[] value = new double[numberOfVariables];
		
		//変数初期化
		for (int i = 0; i < individual.length; i++)
		{
			value = random.rand(value.length);	//0～1の乱数配列を生成
			individual[i] = new Vertex(numberOfVariables, numberOfObjectives);
			individual[i].setVariable(value);
			individual[i].evaluate(nameOfObjectiveFunction);
		}
		
		//重心の初期化
		value = random.rand(value.length);	//0～1の乱数配列を生成
		xg = new Vertex(numberOfVariables, numberOfObjectives);
		xg.setVariable(value);
		xg.evaluate(nameOfObjectiveFunction);

	}
	
	/**
	 * 収縮操作をします。
	 * @param nameOfObjectiveFunction 目的関数名
	 */
	public void reduction(String nameOfObjectiveFunction){
		
		int[] index={0};
		new Matrix(this.getFitness()).getColumn(0).min(index);
		
		for(int i=0; i<individual.length; i++){
			//(xl+xi)/2
			individual[i].setVariable( new Vector(individual[index[0]].getVariable()).plus(new Vector(individual[i].getVariable())).division(2).get() );
		}
		this.limit();
		this.evaluate(nameOfObjectiveFunction);
	}
	
	/**
	 * 変数にリミッターをかける<br>
	 * 最大1最小0<br>
	 */
	public void limit()
	{
		for(int i=0; i<individual.length; i++){
			individual[i].setVariable( Numeric.limit(individual[i].getVariable(), 1.0, 0.0) );
		}
	}
	
	/**
	 * 重心を求め、xgに格納します。
	 */
	public void calculateCentroid()
	{
		//最悪点のインデックスを取得
		int[] index = {0};
		Vector temp = new Matrix(this.getFitness()).getColumn(0);
		temp.max(index);
		
		//変数の和から最悪点を引く
		Matrix variables = new Matrix( this.getVariables() );
		Vector sum = variables.sum(Matrix.DIRECTION_COLUMN);
		temp = variables.getRow(index[0]);
		sum = sum.minus(temp);
		
		//個体数-1で割って平均を算出し、xgにセット
		xg.setVariable( sum.division(temp.length()-1).get() );
	}
}
