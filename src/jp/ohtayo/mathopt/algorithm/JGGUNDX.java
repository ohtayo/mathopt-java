package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.commons.util.Cast;
import jp.ohtayo.mathopt.core.Population;
import jp.ohtayo.mathopt.core.BinaryIndividual;

/**
 * 世代交代モデルにJGG，交叉にUNDXを用いた実数地遺伝的アルゴリズムの計算クラスです．
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class JGGUNDX {
	/**
	 * RCGAのメイン関数です。<br>
	 * 交叉にはUNDX、世代交代モデルにJGGを用いています。<br>
	 * RCGAの計算は本関数を呼び出して行います。<br>
	 * @param numberOfVariables 変数数
	 * @param numberOfIterations 世代数
	 * @param nameOfObjectiveFunction 目的関数名
	 * @param mutationRate 突然変異確率
	 * @return 最終世代の解集団の一番よい適応度
	 */
	public static double main(
			int numberOfVariables, 
			int numberOfIterations,
			String nameOfObjectiveFunction, 
			double mutationRate)
	{
		int numberOfPopulations = numberOfVariables * 50;	//個体数は15n～50nが推奨
		int numberOfReplace = numberOfVariables;			//入れ替え個体数は変数と同値
		int numberOfChildren = numberOfVariables * 10;		//子個体生成数は10nが推奨
		double crossoverRate = numberOfChildren / numberOfReplace;	//交叉確率は、取り出した個体から生成する親の割合。
		
		//初期化
		Population population = new Population(numberOfPopulations);
		int numberOfObjectives = 1;
		population.initialize(numberOfVariables, numberOfPopulations, numberOfObjectives, nameOfObjectiveFunction);
		Population parents = null;
		Population children = null;
		
		Vector bestFitness = new Vector(numberOfIterations);
		Matrix bestVariables = new Matrix(numberOfIterations, numberOfVariables);
		int[] min = {0};
		
		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
			//populationからparentsを抽出
			Vector vector = new Vector(numberOfReplace, "random");	//ランダム配列
			int[] index = Cast.doubleToInt( vector.multiply(numberOfPopulations).get() );
			parents = population.picup(index);
			
			//抽出したparentsからchildrenを生成
			children = RCGA.undx(parents, crossoverRate);
			//生成したchildrenに突然変異発生
			children = RCGA.mutate(children, mutationRate);
			//childrenの評価
			children.evaluate(nameOfObjectiveFunction);
			//childrenからエリート保存をして、入れ替え個体を作る
			parents = SimpleGeneticAlgorithm.selectElite(children, parents.individual.length);
			
			//入れ替え個体をpopulationに戻す
			for(int i=0; i<index.length; i++)
			{
				population.individual[index[i]] = parents.individual[i].copy();
			}
			
			//最優秀適応度の表示
			bestFitness.set(iterate, new Matrix(population.getFitness()).getColumn(0).min(min) );
			bestVariables.setRow(iterate, new Vector(population.individual[min[0]].getVariable()));
			Logging.logger.info(population.individual[min[0]].toString() );
		}
		
		//適応度変化の描画
		/*Vector horizon = new Vector(1,1,numberOfIterations);
		Matrix fitness = new Matrix(horizon.length(),1+1);
		fitness.setColumn(0, horizon); 	//横軸を設定
		fitness.setColumn(1, bestFitness);	//縦軸は適応度
		Figure fig = new Figure("fitness","iterations","fitness");
		fig.plot(fitness);
		*/
		/*
		//変数変化の描画
		Matrix variable = new Matrix(horizon.length(), numberOfVariables+1);
		variable.setColumn(0, horizon);
		variable.setSubMatrix(0, variable.length(), 1, bestVariables.columnLength(), bestVariables ); 
		Figure fig2 = new Figure("variables","iterations","variable value");
		fig2.plot(variable);
		*/
		return bestFitness.get(numberOfIterations-1);

	}
	
	/**
	 * 親と子の個体群から次世代の親個体群を選択します。<br>
	 * 選択方法にはエリート選択とルーレット選択を用います。<br>
	 * @param parent 親個体群
	 * @param children 子個体群
	 * @param eliteNumber エリート数
	 * @return 次世代の親個体群
	 */
	public static  Population select(Population parent, Population children, int eliteNumber)
	{
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
		Population nextGeneration = new Population(parent.individual.length);
		if(binary)	nextGeneration.initialize(parent.individual[0].getVariable().length, numberOfBits, 1);
		else		nextGeneration.initialize(parent.individual[0].getVariable().length, 1);
		boolean[] through = new boolean[fitness.length()];	//選択済みか否か．初期値はfalse

		//エリート選択
		for (int i = 0; i < eliteNumber; i++)
		{
			nextGeneration.individual[i] = population.individual[index[i]].copy();
			sum -= fitness.get(i);
		}
		
		//ルーレット選択
		for (int i = eliteNumber; i < nextGeneration.individual.length; i++)
		{
			double roulette = random.nextDouble() * sum;   //ルーレット値を生成
			for (int j = eliteNumber; j < fitness.length(); j++)  //ルーレットの中であたりを探す
			{
				if (through[j] == false)    //選択されてないもののみチェック
				{
					if (roulette < fitness.get(j))  //当たっていたら
					{
						nextGeneration.individual[i] = population.individual[index[j]].copy();
						sum -= fitness.get(j);                          //ルーレットからその個体を除く
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
