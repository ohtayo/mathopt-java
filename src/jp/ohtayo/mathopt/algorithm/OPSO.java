package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.commons.io.Csv;
import jp.ohtayo.mathopt.core.Particle;
import jp.ohtayo.mathopt.core.Swarm;

/**
 * 粒子群最適化(PSO)アルゴリズムの計算を行うクラスです。<br>
 * 探索の定数および突然変異方法にOMOPSOの手法を取り込んでいます。<br>
 * 計算には他にnameOfObjectiveFunctionで指定した目的関数クラスが必要です。<br>
 * 制約条件には対応していません。<br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class OPSO {

	/**
	 * OptimalPSOのメイン関数です。<br>
	 * OptimalPSOの計算は本関数を呼び出して行います。<br>

	 * @param numberOfVariables 変数の数
	 * @param numberOfParticles 粒子の数
	 * @param numberOfIterations 評価回数
	 * @param nameOfObjectiveFunction 目的関数の名前
	 * @return 最終世代のglobalbestの適応度
	 */
	public static double main(int numberOfVariables, int numberOfParticles, int numberOfIterations, String nameOfObjectiveFunction, boolean visualization)
	{
		//初期化
		Swarm swarm = new Swarm( numberOfParticles );
		swarm.initialize( numberOfVariables, 1, nameOfObjectiveFunction );
		swarm = evaluate(swarm, nameOfObjectiveFunction);
		
		//グローバルベストにswarmをコピー
		Particle globalBest = new Particle( numberOfVariables, 1 );
		globalBest = swarm.particle[0].copy();
		globalBest = updateGlobalBest(swarm, globalBest);

		Vector bestFitness = new Vector(numberOfIterations);

		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
			save(globalBest, iterate);
		
			swarm = update(swarm, globalBest);
			
			//swarm = mutate(swarm,iterate);
			swarm = MOPSO.mutate(swarm, iterate);
			
			swarm = evaluate(swarm, nameOfObjectiveFunction);
			
			globalBest = updateGlobalBest(swarm, globalBest);

			//Logging.logger.info("globalBestの適応度は" + globalBest.fitness[0] + "でした。");
			Logging.logger.info(globalBest.toString());
			bestFitness.set(iterate, globalBest.fitness[0] );
		}
		
		//適応度変化の描画
		if(visualization){
			Vector horizon = new Vector(1,1,numberOfIterations);
			Matrix fitness = new Matrix(horizon.length(),1+1);
			fitness.setColumn(0, horizon); 	//横軸を設定
			fitness.setColumn(1, bestFitness);	//縦軸は適応度
			// Figure fig = new Figure("fitness","iterations","fitness");	// 描画は将来対応
			// fig.plot(fitness);
		}
		
		save(globalBest, numberOfIterations);
		return bestFitness.get(numberOfIterations-1);
	}
	
	/**
	 * グローバルベスト粒子を保存します。<br>
	 * @param globalBest グローバルベスト粒子
	 * @param iterate 今までの評価回数
	 */
	private static void save(Particle globalBest, int iterate)
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
	 * 粒子群の位置の更新をします。<br>
	 * @param swarm 粒子群
	 * @param globalBest グローバルベスト粒子
	 * @return 更新した粒子群
	 */
	
	private static Swarm update(Swarm swarm, Particle globalBest)
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
	
	/**
	 * グローバルベスト粒子を更新します。<br>
	 * @param swarm 粒子群
	 * @param globalBest グローバルベスト粒子
	 * @return 更新したグローバルベスト粒子
	 */
	private static Particle updateGlobalBest(Swarm swarm, Particle globalBest)
	{
		Particle ret = new Particle(globalBest.position.length, 1);
		ret = globalBest.copy();
		
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
	private static Swarm evaluate(Swarm swarm, String nameOfObjectiveFunction)
	{
		for(int i=0; i<swarm.particle.length; i++)
		{
			swarm.particle[i].evaluate(nameOfObjectiveFunction);
			swarm.particle[i].updateBest(1);
		}
		return swarm;
	}
}
