package jp.ohtayo.mathopt.core;

import jp.ohtayo.mathopt.function.ObjectiveFunction;

/**
 * 粒子群最適化(PSO)アルゴリズムの粒子クラスです。<br>
 * 位置・速度とその際の適応度を持つ粒子を提供します。<br>
 * また、粒子がこれまでに最も良かった粒子の位置と適応度を保存しておきます。<br>
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class Particle{
	/** 位置(変数)	*/	public double[] position;
	/** 速度		*/	public double[] velocity;
	/** 適応度		*/	public double[] fitness;
	/** 制約違反量	*/	public double[] constraintViolation;

	/** 最良位置	*/	public double[] bestPosition;
	/** 最良適応度	*/	public double[] bestFitness;
	/** 最良制約違反量*/public double[] bestConstraintViolation;

	/**
	 * デフォルトコンストラクタ<br>
	 * 変数の数と目的関数の数を指定して粒子のメモリを確保します。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfObjectives 目的関数の数
	 */
	public Particle(int numberOfVariables, int numberOfObjectives)
	{
		position = new double[numberOfVariables];
		fitness  = new double[numberOfObjectives];
		velocity = new double[numberOfVariables];
		constraintViolation = new double[0];
		bestPosition = new double[numberOfVariables];
		bestFitness  = new double[numberOfObjectives];
		bestConstraintViolation = new double[0];
	}

	/**
	 * デフォルトコンストラクタ<br>
	 * 変数の数と目的関数の数を指定して粒子のメモリを確保します。<br>
	 * 制約違反量も算出します。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfObjectives 目的関数の数
	 * @param numberOfConstraints 制約の数
	 */
	public Particle(int numberOfVariables, int numberOfObjectives, int numberOfConstraints)
	{
		position = new double[numberOfVariables];
		fitness  = new double[numberOfObjectives];
		velocity = new double[numberOfVariables];
		constraintViolation = new double[numberOfConstraints];
		for(int i=0; i<constraintViolation.length; i++)	constraintViolation[i] = Double.MAX_VALUE;
		bestPosition = new double[numberOfVariables];
		bestFitness  = new double[numberOfObjectives];
		bestConstraintViolation = new double[numberOfConstraints];
		for(int i=0; i<bestConstraintViolation.length; i++)	bestConstraintViolation[i] = Double.MAX_VALUE;
	}

	/**
	 * 目的関数計算と適応度の更新をします。<br>
	 * @param nameOfObjectiveFunction 目的関数の名前<br>
	 */
	public void evaluate(String nameOfObjectiveFunction)
	{
		if( constraintViolation.length==0 ){
			//制約違反量を使用しない場合そのまま評価値を算出
			fitness = (double[]) ObjectiveFunction.execute(position, nameOfObjectiveFunction);
		}else{
			//制約違反量と評価値を算出
			EvaluatedValues ev = (EvaluatedValues) ObjectiveFunction.execute(position, nameOfObjectiveFunction);
			this.fitness = ev.fitness;
			this.constraintViolation = ev.constraintViolation;
		}
	}

	/**
	 * 今の粒子位置と、優良位置の適応度を比較して、<br>
	 * 今の粒子位置のほうが良ければ、優良位置と適応度を更新します。<br>
	 */
	public void updateBest(int numberOfObjectives)
	{
		if(numberOfObjectives>1)	//多目的の場合
		{
			if( constraintViolation.length==0 ){
				//制約違反を使用しない場合
				if(Rank.dominated(bestFitness, fitness, 0, 0)==false)	//fitnessが優越していたら
				{
					System.arraycopy(position, 0, bestPosition, 0, position.length);
					System.arraycopy(fitness, 0, bestFitness, 0, fitness.length);
				}
			}else{
				//制約違反を使用する場合
				if(Rank.dominated(bestFitness, fitness, bestConstraintViolation, constraintViolation, 0, 0)==false)	//fitnessが優越していたら
				{
					System.arraycopy(position, 0, bestPosition, 0, position.length);
					System.arraycopy(fitness, 0, bestFitness, 0, fitness.length);
					bestConstraintViolation = constraintViolation;
				}
			}
		}
		else	//単目的の場合
		{
			if( constraintViolation.length==0 ){
				if(this.fitness[0] < this.bestFitness[0])
				{
					System.arraycopy(this.position, 0, this.bestPosition, 0, this.position.length);
					System.arraycopy(this.fitness, 0, this.bestFitness, 0, this.fitness.length);
				}
			}else{
				
			}
		}
	}

	/**
	 * 別の粒子の値を自身にコピーする(別メモリを確保)<br>
	 * @param p コピー元の粒子
	 */
	public void copy(Particle p)
	{
		System.arraycopy(p.position, 0, position, 0, p.position.length);
		System.arraycopy(p.velocity, 0, velocity, 0, p.velocity.length);
		System.arraycopy(p.fitness, 0, fitness, 0, p.fitness.length);
		this.constraintViolation = p.constraintViolation;
		System.arraycopy(p.bestPosition, 0, bestPosition, 0, p.bestPosition.length);
		System.arraycopy(p.bestFitness, 0, bestFitness, 0, p.bestFitness.length);
		this.bestConstraintViolation = p.bestConstraintViolation;
	}

	/**
	 * 自身を別の粒子にコピーする(別メモリを確保)<br>
	 * @return コピーしたデータ
	 */
	public Particle copy()
	{
		Particle result = new Particle(position.length, fitness.length);

		System.arraycopy(position, 0, result.position, 0, position.length);
		System.arraycopy(velocity, 0, result.velocity, 0, velocity.length);
		System.arraycopy(fitness, 0, result.fitness, 0, fitness.length);
		result.constraintViolation = this.constraintViolation;
		System.arraycopy(bestPosition, 0, result.bestPosition, 0, bestPosition.length);
		System.arraycopy(bestFitness, 0, result.bestFitness, 0, bestFitness.length);
		result.bestConstraintViolation = this.bestConstraintViolation;

		return result;
	}

	/**
	 * 粒子の内容を文字列として返す。<br>
	 * @return 粒子の文字列
	 */
	public String toString()
	{
		String s = "";
		s += "\r\nFitness: \r\n";
		for (int i=0; i<fitness.length; i++)
			s += "  " + fitness[i];
		s += "\r\nValue of constraint violation: \r\n";
		for (int i=0; i<constraintViolation.length; i++)
			s += "  " + constraintViolation[i];
		s += "\r\nPosition: \r\n";
		for (int i = 0; i < position.length; ++i)
			s += "  " + position[i];
		s += "\r\nVelocity: \r\n";
		for (int i = 0; i < velocity.length; ++i)
		    s += "  " + velocity[i];
		s += "\r\nParticle best position: \r\n";
		for (int i = 0; i < bestPosition.length; ++i)
		    s += "  " + bestPosition[i];
		s += "\r\nParticle best fitness: \r\n";
		for (int i=0; i<bestFitness.length; i++)
			s += "  " + bestFitness[i];
		s += "\r\nParticle best constraint violation: \r\n";
		for (int i=0; i<bestConstraintViolation.length; i++)
			s += "  " + bestConstraintViolation[i];
		s += "\r\n";
		return s;
    }
}
