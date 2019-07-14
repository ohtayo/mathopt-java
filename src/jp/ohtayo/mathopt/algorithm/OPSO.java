package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.mathopt.core.Particle;
import jp.ohtayo.mathopt.core.Swarm;
import jp.ohtayo.mathopt.algorithm.PSO;
import jp.ohtayo.mathopt.algorithm.OMOPSO;

/**
 * 粒子群最適化(PSO)アルゴリズムの計算を行うクラスです。<br>
 * 探索の定数および突然変異方法にOMOPSOの手法を取り込んでいます。<br>
 * 計算には他にnameOfObjectiveFunctionで指定した目的関数クラスが必要です。<br>
 * 制約条件には対応していません。<br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class OPSO extends PSO{

	/**
	 * OptimalPSOのメイン関数です。<br>
	 * OptimalPSOの計算は本関数を呼び出して行います。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfParticles 粒子の数
	 * @param numberOfIterations 評価回数
	 * @param nameOfObjectiveFunction 目的関数の名前
	 * @return 最終世代のglobalbestの適応度
	 */
	public double main(int numberOfVariables, int numberOfParticles, int numberOfIterations, String nameOfObjectiveFunction)
	{
		// mutate()のためにOMOPSOをインスタンス化
		OMOPSO omopso = new OMOPSO();

		//初期化
		Swarm swarm = new Swarm( numberOfParticles );
		swarm.initialize( numberOfVariables, 1, nameOfObjectiveFunction );
		swarm = evaluate(swarm, nameOfObjectiveFunction);
		
		//グローバルベストにswarmをコピー
		Particle globalBest = swarm.particle[0].copy();
		globalBest = updateGlobalBest(swarm, globalBest);

		Vector bestFitness = new Vector(numberOfIterations);

		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
			save(globalBest, iterate);
		
			swarm = update(swarm, globalBest);
			
			swarm = omopso.mutate(swarm,iterate);

			swarm = evaluate(swarm, nameOfObjectiveFunction);
			
			globalBest = updateGlobalBest(swarm, globalBest);

			Logging.logger.info(globalBest.toString());
			bestFitness.set(iterate, globalBest.fitness[0] );
		}

		save(globalBest, numberOfIterations);
		return bestFitness.get(numberOfIterations-1);
	}

	/**
	 * 粒子群の位置の更新をします。<br>
	 * @param swarm 粒子群
	 * @param globalBest グローバルベスト粒子
	 * @return 更新した粒子群
	 */
	
	private Swarm update(Swarm swarm, Particle globalBest)
	{
		Random random = new Random();
		
		//ランダム数
		double w = 0.1+0.4*random.nextDouble();
		double c1 = 1.5+0.5*random.nextDouble();
		double c2 = 1.5+0.5*random.nextDouble();
		double r1, r2;

		//全ての粒子に対して
		for (int i=0; i<swarm.particle.length; i++){
			//ランダム数
			r1 = random.nextDouble();
			r2 = random.nextDouble();
			//位置・速度の更新
			for (int v=0; v<swarm.particle[i].velocity.length; v++){
				swarm.particle[i].velocity[v] = w * swarm.particle[i].velocity[v]
						+ c1 * r1 * (swarm.particle[i].bestPosition[v]- swarm.particle[i].position[v])
						+ c2 * r2 * (globalBest.position[v] - swarm.particle[i].position[v]);

				//速度がはみ出てたら補正
				swarm.particle[i].velocity[v] = Numeric.limit(swarm.particle[i].velocity[v], 0.5, -0.5);
				
				//位置の更新
				swarm.particle[i].position[v] += swarm.particle[i].velocity[v];
				
				//位置がはみ出てたら補正
				swarm.particle[i].position[v] = Numeric.limit(swarm.particle[i].position[v], 1.0, 0.0);
			}
		}
		
		return swarm;
	}
}
