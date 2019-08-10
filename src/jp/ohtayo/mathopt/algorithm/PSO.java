package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.commons.io.Csv;
import jp.ohtayo.mathopt.core.Particle;
import jp.ohtayo.mathopt.core.Swarm;

/**
 * 粒子群最適化(PSO)アルゴリズムの計算を行うクラスです。<br>
 * 計算には他にnameOfObjectiveFunctionで指定した目的関数クラスが必要です。<br>
 * 制約条件には対応していません。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class PSO {

	/**
	 * PSOのメイン関数です。<br>
	 * PSOの計算は本関数を呼び出して行います。<br>

	 * @param numberOfVariables 変数の数
	 * @param numberOfParticles 粒子の数
	 * @param numberOfIterations 評価回数
	 * @param nameOfObjectiveFunction 目的関数の名前
	 * @param weight 重み
	 * @param constant1 定数1
	 * @param constant2 定数2
	 * @return 最終世代のglobalbestの適応度
	 */
	public double main(int numberOfVariables, int numberOfParticles, int numberOfIterations,
			 String nameOfObjectiveFunction, double weight, double constant1, double constant2)
	{
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
		
			swarm = update(swarm, globalBest, weight, constant1, constant2);
			
			swarm = mutate(swarm,iterate);
			
			swarm = evaluate(swarm, nameOfObjectiveFunction);
			
			globalBest = updateGlobalBest(swarm, globalBest);
			Logging.logger.info(globalBest.toString());
			bestFitness.set(iterate, globalBest.fitness[0] );
		}

		save(globalBest, numberOfIterations);
		return bestFitness.get(numberOfIterations-1);

	}
	
	/**
	 * グローバルベスト粒子を保存します。<br>
	 * @param globalBest グローバルベスト粒子
	 * @param iterate 今までの評価回数
	 */
	public void save(Particle globalBest, int iterate)
	{
		//ファイル名の生成
		String fileName  = "./result/pso" + iterate +".csv";
		
		double[][] data = new double[globalBest.position.length+1][1];
		data[0][0] = globalBest.fitness[0];
		for(int i=1; i<globalBest.position.length+1; i++)
			data[i][0] = globalBest.position[i-1];
		//各データの保存
		Csv.write(fileName ,data);			
	}
	
	/**
	 * 粒子群に突然変異を起こします。<br>
	 * 突然変異方法は非一様突然変異です。<br>
	 * @param swarm 粒子群
	 * @param iterate 今までの評価回数
	 * @return 突然変異を起こした粒子群
	 */
	public Swarm mutate(Swarm swarm, int iterate)
	{
		Random random = new Random();
		double mutationRate = 1.0/swarm.particle[0].position.length;
		double variation;
		for(int i=0; i<swarm.particle.length; i++)
		{
			for(int j=0; j<swarm.particle[0].position.length; j++)
			{
				if(random.nextDouble() < mutationRate)
				{
					variation = (random.nextDouble()-0.5)/Math.sqrt(iterate+1);
					swarm.particle[i].position[j] += variation;
					swarm.particle[i].position[j] = Numeric.limit(swarm.particle[i].position[j], 1, 0);
				}
				if(random.nextDouble() < mutationRate)
				{
					variation = (random.nextDouble()-0.5)/Math.sqrt(iterate+1);
					swarm.particle[i].velocity[j] += variation;
					swarm.particle[i].velocity[j] = Numeric.limit(swarm.particle[i].velocity[j], 0.5, -0.5);
				}
			}
		}
		return swarm;
	}
	
	/**
	 * 粒子群の位置の更新をします。<br>
	 * @param swarm 粒子群
	 * @param globalBest グローバルベスト粒子
	 * @param w 重み
	 * @param c1 定数1
	 * @param c2 定数2
	 * @return 更新した粒子群
	 */
	
	public Swarm update(Swarm swarm, Particle globalBest, double w, double c1, double c2)
	{
		Random random = new Random();
		
		//ランダム数
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
	
	/**
	 * グローバルベスト粒子を更新します。<br>
	 * @param swarm 粒子群
	 * @param globalBest グローバルベスト粒子
	 * @return 更新したグローバルベスト粒子
	 */
	public Particle updateGlobalBest(Swarm swarm, Particle globalBest)
	{
		Particle ret = globalBest.copy();
		
		for(int i=0; i<swarm.particle.length; i++){
			if( swarm.particle[i].fitness[0] < ret.fitness[0] )
			{
				ret = swarm.particle[i].copy();
			}
		}
		return ret;
	}
	
	/**
	 * 粒子群の各粒子の評価を行い適応度を更新します。<br>
	 * また、各粒子の最良適応度・位置も更新します。<br>
	 * @param swarm 粒子群
	 * @param nameOfObjectiveFunction 目的関数名
	 * @return 更新した粒子群
	 */
	public Swarm evaluate(Swarm swarm, String nameOfObjectiveFunction)
	{
		for(int i=0; i<swarm.particle.length; i++)
		{
			swarm.particle[i].evaluate(nameOfObjectiveFunction);
			swarm.particle[i].updateBest(1);
		}
		return swarm;
	}
}
