package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.commons.util.Cast;
import jp.ohtayo.mathopt.core.Population;
import jp.ohtayo.mathopt.core.BinaryIndividual;

/**
 * 遺伝的アルゴリズム(GA)の計算を行うクラスです。<br>
 * 計算には他にnameOfObjectiveFunctionで指定した目的関数クラスが必要です。<br>
 * 制約条件には対応していません。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class SimpleGeneticAlgorithm {

	/**
	 * GAのメイン関数です。<br>
	 * GAの計算は本関数を呼び出して行います。<br>
	 * @param numberOfBits ビット数
	 * @param numberOfPopulations 個体数
	 * @param numberOfIterations 世代数
	 * @param nameOfObjectiveFunction 目的関数名
	 * @param crossoverRate 交叉率
	 * @param mutationRate 突然変異確率
	 * @param eliteNumber エリート数
	 * @return 最終世代の個体集団のうち最優秀の適応度
	 */
	public static double main(
			int numberOfBits,
			int numberOfVariables, 
			int numberOfPopulations, 
			int numberOfIterations,
			String nameOfObjectiveFunction, 
			double crossoverRate, 
			double mutationRate,
			int eliteNumber)
	{
		//初期化
		Population parents = new Population(numberOfPopulations);
		int numberOfObjectives = 1;
		parents.initialize(numberOfVariables, numberOfBits, numberOfPopulations, numberOfObjectives, nameOfObjectiveFunction);
		Population children = null;
		Population parentsElite = null;
		Population parentsRoulette = null;
		
		Vector bestFitness = new Vector(numberOfIterations);
		Matrix bestVariables = new Matrix(numberOfIterations, numberOfVariables);
		int[] min = {0};
		
		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
		
			children = crossover(parents, crossoverRate);
			
			children = mutate(children, mutationRate);
			
			parents.evaluate(nameOfObjectiveFunction);
			children.evaluate(nameOfObjectiveFunction);
			
			//親個体と子個体を合わせた個体群を生成
			Population population = parents.add(children);
			parentsElite = selectElite(population, eliteNumber);
			double rate = (double)(numberOfPopulations-eliteNumber)/numberOfPopulations;
			parentsRoulette = selectRoulette(parents, children, rate);
			parents = parentsElite.add(parentsRoulette);
			
			//最優秀適応度の表示
			bestFitness.set(iterate, new Matrix(parents.getFitness()).getColumn(0).min(min) );
			bestVariables.setRow(iterate, new Vector(parents.individual[min[0]].getVariable()));
			Logging.logger.info(parents.individual[min[0]].toString() );
		}
		
		//適応度変化の描画(将来対応)
		/*
		Vector horizon = new Vector(1,1,numberOfIterations);
		Matrix fitness = new Matrix(horizon.length(),1+1);
		fitness.setColumn(0, horizon); 	//横軸を設定
		fitness.setColumn(1, bestFitness);	//縦軸は適応度
		Figure fig = new Figure("fitness","iterations","fitness");
		fig.plot(fitness);
		*/
		/*//変数変化の描画
		Matrix variable = new Matrix(horizon.length(), numberOfVariables+1);
		variable.setColumn(0, horizon);
		variable.setSubMatrix(0, variable.length(), 1, bestVariables.columnLength(), bestVariables ); 
		Figure fig2 = new Figure("variables","iterations","variable value");
		fig2.plot(variable);
		*/
		
		return bestFitness.get(numberOfIterations-1);
	}
		
	/**
	 * 親を交叉して子を生成します。<br>
	 * @param parent 親個体群
	 * @param rate 交叉確率
	 * @return 子個体群
	 */
	public static Population crossover(Population parent, double rate)
	{
		Random random = new Random();
		int numberOfBits = ((BinaryIndividual) parent.individual[0]).getNumberOfBits();
		
		int[] chromosome0 = new int[parent.individual[0].getVariable().length];
		int[] chromosome1 = new int[parent.individual[0].getVariable().length];
		int mask		;
		int maskInverse	;
		
		//親の数と交叉確率から子個体配列を生成。子個体配列の初期値は全て0
		Population children = new Population( (int)Math.round(parent.individual.length * rate) );	//子供個体群のサイズ決定
		children.initialize(parent.getNumberOfVariables(), numberOfBits, parent.getNumberOfObjectives());
		children.setVariables( new Matrix(parent.individual.length, parent.individual[0].getVariable().length, 0.0).get() );	//全部0の行列を作って子供個体群の初期値に設定する。
		
		//親を2個体を取り出す順番を決める
		int[] parentNumber = new int[parent.individual.length];
		parentNumber = Cast.doubleToInt( random.shuffle(new Vector(0,1,parent.individual.length-1).get()) );
		
		//2個体ずつ親を選び，子を生成
		for (int i = 0; i < parent.individual.length; i += 2)
		{
			//各個体の染色体数分交叉を繰り返し
			for (int j = 0; j < chromosome0.length; j++)
			{
				mask = random.nextInt( ((BinaryIndividual)parent.individual[0]).bitmask );
				maskInverse = mask ^ (-1);

				chromosome0[j] = (mask & ((BinaryIndividual)parent.individual[parentNumber[i]]).getChromosome()[j]);	//マスクのかかったところは親1のデータを引っ張ってくる
				chromosome0[j]+= (maskInverse & ((BinaryIndividual)parent.individual[parentNumber[i+1]]).getChromosome()[j]);	//マスクのかかってないところは親2のデータを引っ張ってくる
				chromosome0[j]&= ((BinaryIndividual)parent.individual[parentNumber[i]]).bitmask;						//指定ビット数分だけ保持して残りは0にする
				chromosome1[j] = (maskInverse & ((BinaryIndividual)parent.individual[parentNumber[i]]).getChromosome()[j]);
				chromosome1[j]+= (mask & ((BinaryIndividual)parent.individual[parentNumber[i+1]]).getChromosome()[j]);
				chromosome1[j]&= ((BinaryIndividual)parent.individual[parentNumber[i]]).bitmask;
			}
			((BinaryIndividual)children.individual[i]).setChromosome(chromosome0);
			((BinaryIndividual)children.individual[i + 1]).setChromosome(chromosome1);
		}
		
		return children;
		/*デバッグ用
		System.out.println("mask = "+Integer.toBinaryString( mask ));
		System.out.println("nomask = "+Integer.toBinaryString( maskInverse ));
		System.out.println("parent1 = "+Integer.toBinaryString( parent.individual[parentNumber[i]].getChromosome()[j] ));
		System.out.println("parent2 = "+Integer.toBinaryString( parent.individual[parentNumber[i+1]].getChromosome()[j] ));
		System.out.println("mask&parent1 = "+Integer.toBinaryString( chromosome0[j] ));
		System.out.println("mask&parent1+nomask&parent2 = "+Integer.toBinaryString( chromosome0[j] ));
		System.out.println("child = "+Integer.toBinaryString( chromosome0[j] ));
		*/
	}

	/**
	 * 個体群に突然変異を起こします。<br>
	 * @param population 個体群
	 * @param rate 突然変異確率
	 * @return 突然変異した個体群
	 */
	public static Population mutate(Population population, double rate)
	{
		Random random = new Random();
		int numberOfBits = ((BinaryIndividual)population.individual[0]).getNumberOfBits();
		
		for(int i=0; i<population.individual.length; i++)
		{
			if(random.nextDouble() < rate)
			{
				int[] chromosome = ((BinaryIndividual)population.individual[i]).getChromosome();
				int geneNum = (int)(chromosome.length*random.nextDouble());
				for(int j=0; j<chromosome.length; j++)
				{
					if(random.nextDouble() * chromosome.length < geneNum)
					{
						chromosome[j] = (int)((Math.pow(2, numberOfBits)-1) * random.nextDouble());
					}
				}
				((BinaryIndividual)population.individual[i]).setChromosome(chromosome);
			}
		}
		return population;
	}

	
	/**
	 * 親の個体群から次世代の親個体群を選択します。<br>
	 * 選択方法にはエリート選択とルーレット選択を用います。<br>
	 * @param population 個体群
	 * @param eliteNumber エリート数
	 * @return 次世代の親個体群
	 */
	public static  Population selectElite(Population population, int eliteNumber)
	{
		
		//それぞれの適応度を取得する。
		Matrix tempfitness = new Matrix(population.getFitness());
		Vector fitness = tempfitness.getColumn(0);
		
		//適応度順にソートしてインデックスを得る
		int[] index = fitness.sort();
		
		//次の親個体の生成
		Population nextGeneration = new Population(eliteNumber);
		boolean binary = (population.individual[0].getClass().getName() == "BinaryIndividual");	//Individualのコーディングを確認
		int numberOfBits=0;
		if(binary)	numberOfBits = ((BinaryIndividual)population.individual[0]).getNumberOfBits();
		if(binary)	nextGeneration.initialize(population.individual[0].getVariable().length, numberOfBits, 1);
		else		nextGeneration.initialize(population.individual[0].getVariable().length, 1);

		//エリート選択
		for (int i = 0; i < eliteNumber; i++)
		{
			nextGeneration.individual[i] = population.individual[index[i]].copy();
		}

		return nextGeneration;
	}
	
	/**
	 * 親と子の個体群から次世代の親個体群を選択します。<br>
	 * 選択方法にはエリート選択とルーレット選択を用います。<br>
	 * @param parent 親個体群
	 * @param children 子個体群
	 * @param rate ルーレットで選択する割合
	 * @return 次世代の親個体群
	 */
	public static  Population selectRoulette(Population parent, Population children, double rate)
	{
		int nextSize = (int)(parent.individual.length*rate);	//次世代個体数
		Random random = new Random();
		boolean binary = (parent.individual[0].getClass().getName() == "BinaryIndividual");
		int numberOfBits=0;
		if(binary)	numberOfBits = ((BinaryIndividual)parent.individual[0]).getNumberOfBits();
		
		//親個体と子個体を合わせた個体群を生成
		Population population = parent.add(children);
		
		//それぞれの適応度を取得する。
		Matrix tempfitness = new Matrix(population.getFitness());
		Vector fitness = tempfitness.getColumn(0);
				
		//適応度順にソートしてインデックスを得る
		int[] index = fitness.sort();
		double sum = fitness.sum();
		
		//次の親個体の生成
		Population nextGeneration = new Population(nextSize);
		if(binary)	nextGeneration.initialize(parent.individual[0].getVariable().length, numberOfBits, 1);
		else		nextGeneration.initialize(parent.individual[0].getVariable().length, 1);
		boolean[] through = new boolean[fitness.length()];	//選択済みか否か．初期値はfalse

		//ルーレット選択
		for (int i = 0; i < nextSize; i++)
		{
			double roulette = random.nextDouble() * sum;   //ルーレット値を生成
			for (int j = 0; j < fitness.length(); j++)  //ルーレットの中であたりを探す
			{
				if (through[j] == false)    //選択されてないもののみチェック
				{
					if (roulette < fitness.get(j))  //当たっていたら
					{
						nextGeneration.individual[i] = population.individual[index[j]].copy();
						sum -= fitness.get(j);                      //ルーレットからその個体を除く
						through[j] = true;                          //選択フラグを立てる
						break;
					}
					else
					{
						roulette -= fitness.get(j); //当たっていなければ次をチェック
					}
				}
			}
		}
		return nextGeneration;
	}
	
}
