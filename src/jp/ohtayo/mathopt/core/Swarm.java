package jp.ohtayo.mathopt.core;

import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.io.Csv;
import jp.ohtayo.commons.util.StringUtility;
import jp.ohtayo.mathopt.function.ObjectiveFunction;

/**
 * 粒子群最適化(PSO)アルゴリズムの粒子群クラスです。<br>
 * 粒子クラス(Particle)を多数インスタンス化して,群として取り扱うメソッドを提供します。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class Swarm{

	/** 粒子クラス配列 */	public Particle[] particle;

	/**
	 * デフォルトコンストラクタ<br>
	 * 粒子数を指定して粒子インスタンスを生成します。<br>
	 * @param numberOfParticle 粒子数
	 */
	public Swarm(int numberOfParticle)
	{
		particle = new Particle[numberOfParticle];
	}

	/**
	 * 粒子を初期化します。
	 * @param numberOfVariables 変数の数
	 * @param numberOfObjectives 目的関数の数
	 * @param nameOfObjectiveFunction 目的関数の名前(文字列)
	 */
	public void initialize(int numberOfVariables, int numberOfObjectives, String nameOfObjectiveFunction)
	{
		for (int i=0; i<particle.length; i++)
		{
			particle[i] = new Particle(numberOfVariables, numberOfObjectives);
		}
		//位置は0~1で初期化
		double[][] initPosition = new Matrix(particle.length, numberOfVariables, "random").get();
		//速度は-0.5~0.5で初期化
		double[][] initVelocity = new Matrix(particle.length, numberOfVariables, "random").minus(0.5).get();

		for (int i=0; i<particle.length; i++)
		{
			particle[i].position = initPosition[i];
			particle[i].velocity = initVelocity[i];
			System.arraycopy(particle[i].position, 0, particle[i].bestPosition, 0, particle[i].position.length);
			double[] maximum = (double[]) ObjectiveFunction.getMaxValue(nameOfObjectiveFunction);
			System.arraycopy(maximum, 0, particle[i].fitness, 0, maximum.length);	//目的関数の最大値をfitnessに格納
			System.arraycopy(particle[i].fitness, 0, particle[i].bestFitness, 0, particle[i].fitness.length);	//fitnessの値をbestfitnessに格納
		}
	}

	/**
	 * 粒子を初期化します。
	 * @param numberOfVariables 変数の数
	 * @param numberOfObjectives 目的関数の数
	 * @param nameOfObjectiveFunction 目的関数の名前(文字列)
	 * @param fileOfInitialSolutions 初期解を指定する場合そのファイル名。nullか空文字列ならランダムで初期化
	 */
	public void initialize(int numberOfVariables, int numberOfObjectives, int numberOfConstraints,
			String nameOfObjectiveFunction, String fileOfInitialSolutions)
	{
		for (int i=0; i<particle.length; i++)
		{
			if(numberOfConstraints == 0) {	//制約なしの場合制約違反量は算出しない
				particle[i] = new Particle(numberOfVariables, numberOfObjectives);
			}else {
				particle[i] = new Particle(numberOfVariables, numberOfObjectives, numberOfConstraints);
			}
		}

		//位置の初期値
		double[][] initPosition;
		if( StringUtility.isNullOrEmpty(fileOfInitialSolutions) ) {
			//位置は0~1で初期化
			initPosition = new Matrix(particle.length, numberOfVariables, "random").get();
		} else {
			//ファイル名があれば初期化時に解を取り込む
			initPosition = Csv.read(fileOfInitialSolutions, 1, 0);
		}
		//速度は-0.5~0.5で初期化
		double[][] initVelocity = new Matrix(particle.length, numberOfVariables, "random").minus(0.5).get();

		for (int i=0; i<particle.length; i++)
		{
			particle[i].position = initPosition[i];
			particle[i].velocity = initVelocity[i];
			System.arraycopy(particle[i].position, 0, particle[i].bestPosition, 0, particle[i].position.length);
			double[] maximum = (double[]) ObjectiveFunction.getMaxValue(nameOfObjectiveFunction);
			System.arraycopy(maximum, 0, particle[i].fitness, 0, maximum.length);	//目的関数の最大値をfitnessに格納
			System.arraycopy(particle[i].fitness, 0, particle[i].bestFitness, 0, particle[i].fitness.length);	//fitnessの値をbestfitnessに格納
		}
	}
	/**
	 * 粒子を初期化します。<br>
	 * (配列のメモリ確保のみ。通常は使用しないでください。)<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfObjectives 目的関数の数
	 */
	public void initialize(int numberOfVariables, int numberOfObjectives)
	{
		for (int i=0; i<particle.length; i++)
		{
			particle[i] = new Particle(numberOfVariables, numberOfObjectives);
		}
	}

	/**
	 * 粒子群の位置を行列で返します。
	 * @return 粒子群の位置行列
	 */
	public double[][] getPosition()
	{
		double[][] position = new double[particle.length][particle[0].position.length];
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(particle[i].position, 0, position[i], 0, particle[i].position.length);
		}
		return position;
	}
	/**
	 * 粒子群の位置を行列で設定します。
	 * @param position 粒子群の位置行列
	 */
	public void setPosition(double[][] position)
	{
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(position[i], 0, particle[i].position, 0, particle[i].position.length);
		}
	}

	/**
	 * 粒子群の速度を行列で返します。
	 * @return 粒子群の速度行列
	 */
	public double[][] getVelocity()
	{
		double[][] velocity = new double[particle.length][particle[0].position.length];
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(particle[i].velocity, 0, velocity[i], 0, particle[i].velocity.length);
		}
		return velocity;
	}
	/**
	 * 粒子群の速度を行列で設定します。
	 * @param velocity 粒子群の速度行列
	 */
	public void setVelocity(double[][] velocity)
	{
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(velocity[i], 0, particle[i].velocity, 0, particle[i].velocity.length);
		}
	}

	/**
	 * 粒子群の適応度を行列で返します。
	 * @return 粒子群の適応度行列
	 */
	public double[][] getFitness()
	{
		double[][] fitness = new double[particle.length][particle[0].fitness.length];
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(particle[i].fitness, 0, fitness[i], 0, particle[i].fitness.length);
		}
		return fitness;
	}
	/**
	 * 粒子群の適応度を行列で設定します。
	 * @param fitness 粒子群の適応度行列
	 */
	public void setFitness(double[][] fitness)
	{
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(fitness[i], 0, particle[i].fitness, 0, particle[i].fitness.length);
		}
	}

	/**
	 * 粒子群の制約違反量を行列で返します。
	 * @return 粒子群の適応度行列
	 */
	public double[][] getConstraint()
	{
		double[][] constraint = new double[particle.length][particle[0].constraintViolation.length];

		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(particle[i].constraintViolation, 0, constraint[i], 0, particle[i].constraintViolation.length);
		}
		return constraint;
	}

	/**
	 * 粒子群の最良位置を行列で返します。
	 * @return 粒子群の最良位置行列
	 */
	public double[][] getBestPosition()
	{
		double[][] bestPosition = new double[particle.length][particle[0].bestPosition.length];
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(particle[i].bestPosition, 0, bestPosition[i], 0, particle[i].bestPosition.length);
		}
		return bestPosition;
	}
	/**
	 * 粒子群の最良位置の適応度を行列で返します。
	 * @return 粒子群の最良位置の適応度行列
	 */
	public double[][] getBestFitness()
	{
		double[][] bestFitness = new double[particle.length][particle[0].bestFitness.length];
		for (int i=0; i<particle.length; i++)
		{
			System.arraycopy(particle[i].bestFitness, 0, bestFitness[i], 0, particle[i].bestFitness.length);
		}
		return bestFitness;
	}

	/**
	 * ２つの粒子群を合成して1つの粒子群にします。
	 * @param basis 先頭に置く粒子群
	 * @param target 後ろに接続する粒子群
	 * @return 合成した粒子群
	 */
	public static Swarm add(Swarm basis, Swarm target)
	{
		Swarm result = new Swarm(basis.particle.length+target.particle.length);
		result.initialize(target.particle[0].position.length, target.particle[0].fitness.length);

		for (int i=0; i<basis.particle.length; i++)
		{
			result.particle[i] = basis.particle[i].copy();
		}
		for (int i=basis.particle.length; i<result.particle.length; i++)
		{
			result.particle[i] = target.particle[i-basis.particle.length].copy();
		}
		return result;
	}

	/**
	 * 粒子群のうち一部を取り出して返します。
	 * @param swarm 抽出したい粒子を含む粒子群
	 * @param index 抽出する粒子のインデックス
	 * @return 抽出した粒子群
	 */
	public static Swarm picup(Swarm swarm, int[] index)
	{
		Swarm result = new Swarm(index.length);
		result.initialize(swarm.particle[0].position.length, swarm.particle[0].fitness.length);

		for (int i=0; i<index.length; i++)
		{
			for (int j=0; j<swarm.particle.length; j++)
			{
				if(j == index[i])
				{
					result.particle[i] = swarm.particle[j].copy();
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 自身のパラメータを別の粒子群にコピーします。(別メモリ確保)
	 * @return コピーした粒子群
	 */
	public Swarm copy()
	{
		Swarm result = new Swarm(particle.length);
		result.initialize(particle[0].position.length, particle[0].fitness.length);
		for (int i=0; i<particle.length; i++)
		{
			result.particle[i] = particle[i].copy();
		}
		return result;
	}

	/**
	 * 粒子群の内容を文字列として返します。
	 * @return 粒子群の内容の文字列
	 */
	public String toString()
	{
		String str = "";
		for(int i=0; i<particle.length; i++)
		{
			str += "particle " + String.valueOf(i) + ":\r\n" + particle[i].toString();
		}
		return str;
	}
}
