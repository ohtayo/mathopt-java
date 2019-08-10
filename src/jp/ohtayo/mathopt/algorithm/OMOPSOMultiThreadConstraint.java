package jp.ohtayo.mathopt.algorithm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.io.Csv;
import jp.ohtayo.mathopt.core.Swarm;
import jp.ohtayo.mathopt.core.Rank;
import jp.ohtayo.mathopt.function.ObjectiveFunction;

/**
 * 多目的粒子群最適化(MOPSO)アルゴリズムの計算を行うクラスです。<br>
 * 計算には他にnameOfObjectiveFunctionで指定した目的関数クラスが必要です。<br>
 * 制約条件によるペナルティを付けます。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class OMOPSOMultiThreadConstraint extends OMOPSO{

	/**
	/**
	 * MOPSOのメイン関数です。<br>
	 * MOPSOの計算は本関数を呼び出して行います。<br>
	 * 優劣計算にε-dominationかα-dominationを使用する事ができます。<br>
	 *
	 * @param numberOfVariables 変数の数
	 * @param numberOfParticles 粒子の数
	 * @param numberOfIterations 評価回数
	 * @param numberOfObjectives 目的関数の数
	 * @param nameOfObjectiveFunction 目的関数クラスの名前(文字列)
	 * @param numberOfConstraints 制約数
	 * @param epsilon ε値
	 * @param alpha α値
	 * @param numberOfThreads スレッド数
	 * @param fileOfInitialSolutions 初期解を指定する場合そのファイル名。nullか空文字列ならランダムで初期化
	 */
	public void main(int numberOfVariables, int numberOfParticles, int numberOfIterations,
					 int numberOfObjectives, String nameOfObjectiveFunction, int numberOfConstraints,
					 double epsilon, double alpha, int numberOfThreads, String fileOfInitialSolutions)
	{
		Swarm swarm = new Swarm( numberOfParticles );
		swarm.initialize( numberOfVariables, numberOfObjectives, numberOfConstraints,
						nameOfObjectiveFunction, fileOfInitialSolutions );
		swarm = evaluate(swarm, nameOfObjectiveFunction, numberOfThreads);

		//グローバルベストにswarmをコピー
		Swarm globalBest = swarm.copy();

		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
			save(globalBest, iterate);

			swarm = update(swarm, globalBest);

			swarm = mutate(swarm,iterate);

			swarm = evaluate(swarm, nameOfObjectiveFunction, numberOfThreads);

			globalBest = select(swarm, globalBest, nameOfObjectiveFunction, epsilon, alpha);
		}

		save(globalBest, numberOfIterations);
	}

	/**
	 * グローバルベスト粒子群をCSVファイルとして保存します。<br>
	 * @param globalBest グローバルベスト粒子群
	 * @param iterate これまでの評価回数
	 */
	public void save(Swarm globalBest, int iterate)
	{
		//ファイル名の生成
		String fileNameFitness  = "./result/fitness" + iterate +".csv";
		String fileNamePosition = "./result/position"+ iterate +".csv";
		String fileNameConstraint = "./result/constraint"+ iterate +".csv";
		//ヘッダーの生成
		String fitnessHeader = "";
		String positionHeader = "";
		String constraintHeader = "";
		for(int i=0; i<globalBest.getFitness()[0].length; i++)	fitnessHeader += "objective " + String.valueOf(i) + ",";
		for(int i=0; i<globalBest.getPosition()[0].length; i++)	positionHeader += "variable " + String.valueOf(i) + ",";
		for(int i=0; i<globalBest.getConstraint().length; i++)	positionHeader += "constraint " + String.valueOf(i) + ",";
		//各データの保存
		Csv.write(fileNameFitness ,globalBest.getFitness(), fitnessHeader);		//適応度の保存
		Csv.write(fileNamePosition,globalBest.getPosition(), positionHeader);	//グローバルベストの保存
		Csv.write(fileNameConstraint,globalBest.getConstraint(), constraintHeader);	//グローバルベストの制約違反量
	}

	/**
	 * 粒子群の評価を行い適応度を更新します。<br>
	 * マルチスレッドで処理するためExecutor Framework(jdk1.5以上)を用います<br>
	 * @param swarm 粒子群
	 * @param nameOfObjectiveFunction 目的関数の名前
	 * @param numberOfThreads スレッド数
	 * @return 適応度を更新した粒子群
	 */
	private Swarm evaluate(Swarm swarm, String nameOfObjectiveFunction, int numberOfThreads)
	{
		//8スレッドの枠を用意
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		try{
			//別スレッドで順次解の評価タスクを渡す
			for(int i=0; i<swarm.particle.length; i++)
			{
				executor.execute(new EvaluateOne(swarm, i, nameOfObjectiveFunction));
			}
		}finally{
			//新規タスクの受付を終了して残ったタスクを継続する．
			executor.shutdown();
			try {
				//指定時間(個体数×1分)が経過するか，全タスクが終了するまで処理を停止する．
				executor.awaitTermination(swarm.particle.length, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Logging.logger.severe(e.getMessage());
			}
		}

		return swarm;
	}

	/**
	 * 粒子群評価をマルチスレッドで処理するためのクラス<br>
	 * implements Runnable<br>
	 */
	private class EvaluateOne implements Runnable{

		private Swarm swarm;					//粒子群
		private int particleNumber;				//粒子番号
		private String nameOfObjectiveFunction;	//目的関数名
		int numberOfObjectives;					//目的数
		private int threadNumber;				//スレッドID
		private String[] threadName;
		//コンストラクタ
		public EvaluateOne(Swarm swarm, int particleNumber, String nameOfObjectiveFunction){
			this.swarm = swarm;
			this.particleNumber = particleNumber;
			this.nameOfObjectiveFunction = nameOfObjectiveFunction;
			this.numberOfObjectives = this.swarm.particle[0].fitness.length;
		}

		//実行
		public void run(){
			//プール番号を除くスレッドIDを取得
			threadName = Thread.currentThread().getName().split("-");
			threadNumber = Integer.valueOf(threadName[threadName.length-1]);
			//スレッドIDを指定して評価を実行
			System.out.println("スレッドID="+String.valueOf(threadNumber));
			Logging.logger.info(Thread.currentThread().getName()+"開始");
			swarm.particle[this.particleNumber].evaluate(nameOfObjectiveFunction);
			swarm.particle[this.particleNumber].updateBest(numberOfObjectives);
			Logging.logger.info(Thread.currentThread().getName()+"終了");
		}
	}

	/**
	 * グローバルベスト粒子群と更新した粒子群から、次世代のグローバルベスト粒子群を選択します。<br>
	 * @param swarm 更新した粒子群
	 * @param globalBest グローバルベスト
	 * @param nameOfObjectiveFunction 目的関数の名前
	 * @param epsilon ε値
	 * @param alpha α値
	 * @return 次世代のグローバルベスト粒子群
	 */
	public Swarm select(Swarm swarm, Swarm globalBest, String nameOfObjectiveFunction, double epsilon, double alpha)
	{
		int globalSize = swarm.particle.length;	//swarmと同じ数がグローバルベストのサイズ
		Swarm combined = Swarm.add(swarm, globalBest);	//swarmとglobalBestを一つにする

		//ランク付けする
		double[][] fitness = combined.getFitness();	//fitnessを取得
		double[] maxValue = (double[])ObjectiveFunction.getMaxValue(nameOfObjectiveFunction);	//最大値を取得
		double[] minValue = (double[])ObjectiveFunction.getMinValue(nameOfObjectiveFunction);	//最小値を取得
		fitness = new Matrix(fitness).normalize(maxValue, minValue).get();
		int[] rank = Rank.ranking(combined.getFitness(), combined.getConstraint(), epsilon, alpha);

		//境界ランク番号計算
		int numOfBorderRank = Rank.calculateBorderRank(rank, globalSize);

		//境界ランクと上位ランクの適応度を取り出し
		//境界ランクと上位ランクの数を数える
		int borderRankSize=0;
		int upperRankSize = 0;
		for (int i=0; i<rank.length; i++)
		{
			if (rank[i] < numOfBorderRank)
			{
				upperRankSize++;
			}
			else if (rank[i] == numOfBorderRank)
			{
				borderRankSize++;
			}
		}

		//上位ランクの数がglobalSizeを上回っていたらエラー
		if (upperRankSize > globalSize )	Logging.logger.severe("上位ランク数が異常です。");

		//境界ランクと上位ランクのSwarmを生成して、格納
		Swarm border = new Swarm(borderRankSize);
		border.initialize(swarm.particle[0].position.length, swarm.particle[0].fitness.length);
		Swarm upper = new Swarm(upperRankSize);
		upper.initialize(swarm.particle[0].position.length, swarm.particle[0].fitness.length);
		int countBorder = 0, countUpper = 0;
		for (int i=0; i<rank.length; i++)
		{
			if (rank[i] < numOfBorderRank)
			{
				upper.particle[countUpper] = combined.particle[i].copy();
				countUpper++;
			}
			else if (rank[i] == numOfBorderRank)
			{
				border.particle[countBorder] = combined.particle[i].copy();
				countBorder++;
			}
		}

		//境界ランク+上位ランクの近傍距離を計算し境界ランクのみ残す
		Swarm last = Swarm.add(upper, border);
		double[][] lastFitness = last.getFitness();
		lastFitness = new Matrix(lastFitness).normalize(maxValue, minValue).get();
		double[] lastDistance =  Rank.calculateDistance(lastFitness);
		double[] borderDistance = new Vector(lastDistance).get(upperRankSize, borderRankSize).get();

		//近傍距離でソートする
		int[] index = new Vector(borderDistance).sort("descend");

		//上位ランク粒子をglobalBestに保存
		for (int i=0; i<upperRankSize; i++)
		{
			globalBest.particle[i] = upper.particle[i].copy();
		}
		//残数分境界ランクから順に抽出して保存
		for (int i=0; i<(globalSize-upperRankSize) ; i++)
		{
			globalBest.particle[upperRankSize+i] = border.particle[index[i]].copy();
		}

		//グローバルベストとして返す。
		return globalBest;
	}
}
