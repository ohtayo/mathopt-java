package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.commons.util.Cast;
import jp.ohtayo.commons.io.Csv;
import jp.ohtayo.mathopt.core.Swarm;
import jp.ohtayo.mathopt.core.Rank;
import jp.ohtayo.mathopt.function.ObjectiveFunction;

/**
 * 多目的粒子群最適化(MOPSO)アルゴリズムの計算を行うクラスです。<br>
 * アルゴリズムにOMOPSOを採用しています．<br>
 * 計算には他にnameOfObjectiveFunctionで指定した目的関数クラスが必要です。<br>
 * 制約条件には対応していません。<br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class MOPSO {

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
	 * @param epsilon ε値
	 * @param alpha α値
	 */
	public static void main(int numberOfVariables, int numberOfParticles, int numberOfIterations,
					 int numberOfObjectives, String nameOfObjectiveFunction, double epsilon, double alpha)
	{
		//初期化
		Swarm swarm = new Swarm( numberOfParticles );
		swarm.initialize( numberOfVariables, numberOfObjectives, nameOfObjectiveFunction );
		swarm = evaluate(swarm, nameOfObjectiveFunction);

		//グローバルベストにswarmをコピー
		Swarm globalBest = new Swarm( numberOfParticles );
		globalBest = swarm.copy();

		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
			save(globalBest, iterate);

			swarm = update(swarm, globalBest);

			swarm = mutate(swarm,iterate);

			swarm = evaluate(swarm, nameOfObjectiveFunction);

			globalBest = select(swarm, globalBest, nameOfObjectiveFunction, epsilon, alpha);
		}

		save(globalBest, numberOfIterations);
	}

	/**
	 * グローバルベスト粒子群をCSVファイルとして保存します。<br>
	 * @param globalBest グローバルベスト粒子群
	 * @param iterate これまでの評価回数
	 */
	private static void save(Swarm globalBest, int iterate)
	{
		//ファイル名の生成
		String fileNameFitness  = "./result/fitness" + iterate +".csv";
		String fileNamePosition = "./result/position"+ iterate +".csv";
		//ヘッダーの生成
		String fitnessHeader = "";
		String positionHeader = "";
		for(int i=0; i<globalBest.getFitness()[0].length; i++)	fitnessHeader += "objective " + String.valueOf(i) + ",";
		for(int i=0; i<globalBest.getPosition()[0].length; i++)	positionHeader += "variable " + String.valueOf(i) + ",";
		//各データの保存
		Csv.write(fileNameFitness ,globalBest.getFitness(), fitnessHeader);		//適応度の保存
		Csv.write(fileNamePosition,globalBest.getPosition(), positionHeader);	//グローバルベストの保存
	}

	//位置の更新
	/**
	 * グローバルベスト粒子群を用いて、粒子群の位置と速度を更新します。<br>
	 * @param swarm 粒子群
	 * @param globalBest グローバルベスト粒子群
	 * @return 更新した粒子群
	 */
	private static Swarm update(Swarm swarm, Swarm globalBest)
	{
		Random random = new Random(32648L);

		//ランダム数
		double w = 0.1+0.4*random.nextDouble();
		double c1 = 1.5+0.5*random.nextDouble();
		double c2 = 1.5+0.5*random.nextDouble();
		double r1, r2;
		int rg;

		//グローバルベストからランク1配列を抽出
		int[] rank = Rank.ranking(globalBest.getFitness(),0.0,0.0);	//ランク付け。ランク1だけでいいので正規化もalphaも不要
		//int[] rank1Index = Rank.rankIndex(rank, (int)Numeric.min(Cast.intToDouble(rank)));//ランク1個体を抽出する。
		int[] rank1Index = Rank.rankIndex(rank, (int)(new Vector(Cast.intToDouble(rank)).min()) );

		//全ての粒子に対して
		for (int i=0; i<swarm.particle.length; i++){
			//ランダム数
			r1 = random.nextDouble();
			r2 = random.nextDouble();
			rg = rank1Index[ (int)Math.floor( rank1Index.length * random.nextDouble() )];
			//位置・速度の更新
			for (int v=0; v<swarm.particle[i].velocity.length; v++){
				swarm.particle[i].velocity[v] = w * swarm.particle[i].velocity[v]
						+ c1 * r1 * (swarm.particle[i].bestPosition[v]- swarm.particle[i].position[v])
						+ c2 * r2 * (globalBest.particle[rg].position[v] - swarm.particle[i].position[v]);

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
	 * 粒子群に突然変異を発生します。<br>
	 * 粒子群は、一様突然変異を起こす粒子群、非一様突然変異を起こす粒子群、突然変異しない粒子群の３つに分割して、<br>
	 * それぞれの粒子群に対し突然変異計算を行います。<br>
	 * @param swarm 粒子群
	 * @param iterate これまでの評価回数
	 * @return 突然変異を起こした粒子群
	 */
	public static Swarm mutate(Swarm swarm, int iterate)
	{
		Random random = new Random(2149857L);

		//突然変異確率
		double mutationRate = 1.0/swarm.particle[0].position.length;

		//Swarmを３つに分ける
		//まず3等分の個数を決める
		int size = (int)( Math.floor(swarm.particle.length/3)); //3等分した大きさ
		int remainder = swarm.particle.length%3;				//余り
		int num1, num2, num3;
		if(remainder == 0){
			num1 = size; num2 = size; num3 = size;
		}else if(remainder == 1){
			num1 = size+1; num2 = size; num3 = size;
		}else{
			num1 = size+1; num2 = size+1; num3 = size;
		}
		//次にランダム数を生成
		//int[] index = Numeric.sequence(0, 1, swarm.particle.length-1);
		//index = Cast.doubleToInt( random.shuffle(Cast.intToDouble(index)) );
		Vector index = new Vector(0,1,swarm.particle.length-1);
		index = index.shuffle();
		//int[] uniformIndex = Cast.doubleToInt(Vector.slice(Cast.intToDouble(index), 0, num1));
		//int[] nonUniformIndex = Cast.doubleToInt(Vector.slice(Cast.intToDouble(index), num1, num2));
		//int[] nonIndex = Cast.doubleToInt(Vector.slice(Cast.intToDouble(index), num1+num2, num3));
		int[] uniformIndex = Cast.doubleToInt( index.get(0,num1).get() );
		int[] nonUniformIndex = Cast.doubleToInt( index.get(num1,num2).get() );
		int[] nonIndex = Cast.doubleToInt( index.get(num1+num2,num3).get() );


		//ランダム数で選んだものを抽出
		Swarm uniformSwarm = new Swarm(num1);
		Swarm nonUniformSwarm = new Swarm(num2);
		Swarm nonSwarm = new Swarm(num3);
		uniformSwarm = Swarm.picup(swarm, uniformIndex);
		nonUniformSwarm = Swarm.picup(swarm,nonUniformIndex);
		nonSwarm = Swarm.picup(swarm,nonIndex);

		//1つめのSwarmは一様突然変異
		for(int i=0; i<uniformSwarm.particle.length; i++){
			for(int j=0; j<uniformSwarm.particle[i].position.length; j++){
				if(random.nextDouble() < mutationRate)
				{
					uniformSwarm.particle[i].position[j] = random.nextDouble();
				}
				if(random.nextDouble() < mutationRate)
				{
					uniformSwarm.particle[i].velocity[j] = random.nextDouble()-0.5;
				}
			}
		}

		//2つめのSwarmは非一様突然変異
		for(int i=0; i<nonUniformSwarm.particle.length; i++){
			for(int j=0; j<nonUniformSwarm.particle[i].position.length; j++){
				if(random.nextDouble() < mutationRate){
					double variation = (random.nextDouble()-0.5)/Math.sqrt(iterate+1);
					nonUniformSwarm.particle[i].position[j] += variation;
					nonUniformSwarm.particle[i].position[j] = Numeric.limit(nonUniformSwarm.particle[i].position[j], 1, 0);
				}
				if(random.nextDouble() < mutationRate){
					double variation = (random.nextDouble()-0.5)/Math.sqrt(iterate+1);
					nonUniformSwarm.particle[i].velocity[j] += variation;
					nonUniformSwarm.particle[i].velocity[j] = Numeric.limit(nonUniformSwarm.particle[i].velocity[j], 0.5, -0.5);
				}
			}
		}

		//3つめは何もせず、3つをくっつけて返す
		swarm = Swarm.add(uniformSwarm, Swarm.add(nonUniformSwarm, nonSwarm));

		return swarm;
	}

	/**
	 * 粒子群の評価を行い適応度を更新します。<br>
	 * @param swarm 粒子群
	 * @param nameOfObjectiveFunction 目的関数の名前
	 * @return 適応度を更新した粒子群
	 */
	private static Swarm evaluate(Swarm swarm, String nameOfObjectiveFunction)
	{
		int numberOfObjectives = swarm.particle[0].fitness.length;
		for(int i=0; i<swarm.particle.length; i++)
		{
			swarm.particle[i].evaluate(nameOfObjectiveFunction);
			swarm.particle[i].updateBest(numberOfObjectives);
		}
		return swarm;
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
	private static Swarm select(Swarm swarm, Swarm globalBest, String nameOfObjectiveFunction, double epsilon, double alpha)
	{
		int globalSize = swarm.particle.length;	//swarmと同じ数がグローバルベストのサイズ
		Swarm combined = Swarm.add(swarm, globalBest);	//swarmとglobalBestを一つにする

		//ランク付けする
		double[][] fitness = combined.getFitness();	//fitnessを取得
		double[] maxValue = (double[])ObjectiveFunction.getMaxValue(nameOfObjectiveFunction);	//最大値を取得
		double[] minValue = (double[])ObjectiveFunction.getMinValue(nameOfObjectiveFunction);	//最小値を取得
		//fitness = Numeric.normalize(fitness, maxValue, minValue);	//正規化
		fitness = new Matrix(fitness).normalize(maxValue, minValue).get();
		//int[] rank = Rank.ranking(fitness, epsilon, alpha);
		int[] rank = Rank.ranking(combined.getFitness(), epsilon, alpha);

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
				//combined.particle[i].copy(upper.particle[countUpper]);
				upper.particle[countUpper] = combined.particle[i].copy();
				countUpper++;
			}
			else if (rank[i] == numOfBorderRank)
			{
				//combined.particle[i].copy(border.particle[countBorder]);
				border.particle[countBorder] = combined.particle[i].copy();
				countBorder++;
			}
		}

		//境界ランク+上位ランクの近傍距離を計算し境界ランクのみ残す
		Swarm last = Swarm.add(upper, border);
		double[][] lastFitness = last.getFitness();
		//lastFitness = Numeric.normalize(lastFitness, maxValue, minValue);	//正規化
		lastFitness = new Matrix(lastFitness).normalize(maxValue, minValue).get();
		double[] lastDistance =  Rank.calculateDistance(lastFitness);
		//double[] borderDistance = Vector.slice(lastDistance, upperRankSize, borderRankSize);
		double[] borderDistance = new Vector(lastDistance).get(upperRankSize, borderRankSize).get();

		//近傍距離でソートする
		//int[] index = Numeric.sort(borderDistance,"descend");
		int[] index = new Vector(borderDistance).sort("descend");

		//上位ランク粒子をglobalBestに保存
		for (int i=0; i<upperRankSize; i++)
		{
			//upper.particle[i].copy(globalBest.particle[i]);
			globalBest.particle[i] = upper.particle[i].copy();
		}
		//残数分境界ランクから順に抽出して保存
		for (int i=0; i<(globalSize-upperRankSize) ; i++)
		{
			//border.particle[index[i]].copy(globalBest.particle[upperRankSize+i]);
			globalBest.particle[upperRankSize+i] = border.particle[index[i]].copy();
		}

		//グローバルベストとして返す。
		return globalBest;
	}
}
