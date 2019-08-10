package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.commons.util.Cast;
import jp.ohtayo.mathopt.core.Population;

/**
 * 実数値遺伝的アルゴリズム(RCGA)の計算を行うクラスです。<br>
 * 交叉にはUNDX、世代交代モデルはSimpleGAのモデルを用いています。<br>
 * 計算には他にnameOfObjectiveFunctionで指定した目的関数クラスが必要です。<br>
 * 制約条件には対応していません。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class RCGA {

	/**
	 * RCGAのメイン関数です。<br>
	 * RCGAの計算は本関数を呼び出して行います。<br>
	 * @param numberOfPopulations 個体数
	 * @param numberOfIterations 世代数
	 * @param nameOfObjectiveFunction 目的関数名
	 * @param crossoverRate 交叉率
	 * @param mutationRate 突然変異確率
	 * @param eliteNumber エリート数
	 * @return 最終世代の解集合のもっともよい適応度
	 */
	public static double main(
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
		parents.initialize(numberOfVariables, numberOfPopulations, numberOfObjectives, nameOfObjectiveFunction);
		Population children = null;
		Population parentsElite = null;
		Population parentsRoulette = null;
		
		Vector bestFitness = new Vector(numberOfIterations);
		Matrix bestVariables = new Matrix(numberOfIterations, numberOfVariables);
		int[] min = {0};
		
		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
		
			children = undx(parents, crossoverRate);
			
			children = mutate(children, mutationRate);
			
			parents.evaluate(nameOfObjectiveFunction);
			children.evaluate(nameOfObjectiveFunction);
			
			Population population = parents.add(children);
			parentsElite = SimpleGeneticAlgorithm.selectElite(population, eliteNumber);
			double rate = (double)(numberOfPopulations-eliteNumber)/numberOfPopulations;
			parentsRoulette = SimpleGeneticAlgorithm.selectRoulette(parents, children, rate);
			parents = parentsElite.add(parentsRoulette);
			
			//最優秀適応度の表示
			bestFitness.set(iterate, new Matrix(parents.getFitness()).getColumn(0).min(min) );
			bestVariables.setRow(iterate, new Vector(parents.individual[min[0]].getVariable()));
			Logging.logger.info(parents.individual[min[0]].toString() );
		}
		
		/*//適応度変化の描画
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
	 * UNDXを使って親を交叉して子を生成します。<br>
	 * 参照：http://www.sice.jp/e-trans/papers/E2-21.pdf
	 * @param population 親個体群
	 * @param rate 交叉確率
	 * @return 子個体群
	 */
	public static Population undx(Population population, double rate)
	{
		double eps = 1.0/(double)Integer.MAX_VALUE;
		Vector order = new Vector((int)(population.individual.length*rate), "random");
		int[] temp = Cast.doubleToInt( order.multiply(population.individual.length).get() );
		order = new Vector( Cast.intToDouble(temp) );
		//Vector order = new Vector(0,1,population.individual.length-1);	//0～numberOfPopulation-1までの数列を生成
		//order = order.shuffle();	//数列をシャッフル
		int[] index = {0,1,2,3};	//先頭4つを数列に継ぎ足し。
		order = order.add(order.get(index));
		
		Population children = new Population( (int)Math.round(population.individual.length * rate) );	//子供個体群のサイズ決定
		children.initialize(population.getNumberOfVariables(), population.getNumberOfObjectives());

		for(int i=0; i<population.individual.length*rate; i++)
		{
			//親を3人決める
			double[] idx = {order.get(i), order.get(i+2), order.get(i+4)};
			Population parent = population.picup(Cast.doubleToInt(idx));
			
			//親が同一でないかを確認して、同一であった場合親1を変更
			int count = i;
			while( parent.individual[0].equals(parent.individual[1]) || parent.individual[1].equals(parent.individual[2]) || parent.individual[2].equals(parent.individual[0])	)
			{
				count+=1;
				if( count > (int)(population.individual.length*rate) )	count = 0;
				if( count == i )	break;	//一周回って、すべての親を入れ替えても無理だった場合break
				if( parent.individual[1].equals(parent.individual[2]) ){
					parent.individual[1] = population.individual[(int)order.get(count+2)].copy();
				}
				if( parent.individual[0].equals(parent.individual[1]) ){
					parent.individual[0] = population.individual[(int)order.get(count)].copy();
				}
				//Logging.logger.info("Change parents 1");
			}
			//違う親を選択することができなかったため、子を親と同一にしてこの回は終了
			if( parent.individual[0].equals(parent.individual[1]) || parent.individual[1].equals(parent.individual[2]) || parent.individual[2].equals(parent.individual[0]) ){
				children.individual[i].setVariable( parent.individual[0].getVariable() );
				continue;
			}
			
			//中点を決める
			Matrix variables = new Matrix(parent.getVariables());
			Vector center = variables.getRow(0).plus(variables.getRow(1)).division(2.0);
			
			//差を求める
			Vector difference01 = variables.getRow(1).minus(variables.getRow(0));
			Vector difference02 = variables.getRow(2).minus(variables.getRow(0));
			
			//親0と親1の直線から親2への単位垂直ベクトルを求める
			//公式：aとbの正射影ベクトル=a(a dot b) / norm(a)^2
			//から正射影ベクトルvを求める。ベクトルbからvを引くと、垂直ベクトルが求まる。
			//垂直ベクトルを大きさで割れば単位垂直ベクトルが求まる。
			if(difference01.norm() == 0)	difference01 = new Vector(difference01.length(),eps);	//ノルムが0の場合、ゼロ割を防ぐために値を入れる。
			Vector difference03 = difference01.multiply( difference01.innerProduct(difference02) / Numeric.square(difference01.norm()) );	//dif01とdif02の正射影ベクトル
			Vector vertical = difference02.minus(difference03);	//親0ー親1直線に垂直方向のベクトル
			double distance = vertical.norm();	//垂線方向の距離
			if( distance == 0 )	distance = eps;	//距離が0の場合、ゼロ割を防ぐために値を入れる。
			Vector basis = vertical.division(distance);	//垂線方向の基底ベクトル
			
			//子を求める
			//子=中点c+ξd+D sigma(i=1～n-1){η_i e_i}
			//ただしσξ=1/2、ση=0.35/sqrt(n)の正規分布(平均0)
			//dは親0と親1の差、eiはdに直交な正規直交基底=垂線方向の基底ベクトルbasis、Dはdと直交な親2までのベクトルの大きさ
			Random random = new Random();
			Vector child = center.plus( difference01.multiply(random.randn()*0.5) );
			child = child.plus( new Vector(random.randn(center.length())).multiply(0.35/Math.sqrt(center.length())).multiply(basis).multiply(distance) );
			children.individual[i].setVariable(child.get());
			
			//位置がはみ出てたら補正
			children.individual[i].setVariable( Numeric.limit(children.individual[i].getVariable(), 1.0, 0.0) );
		}
		
		return children;
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
		
		for(int i=0; i<population.individual.length; i++)
		{
			if(random.nextDouble() < rate)
			{
				double[] variable = population.individual[i].getVariable();
				double geneNum = variable.length*random.nextDouble();
				for(int j=0; j<variable.length; j++)
				{
					if(random.nextDouble() * variable.length < geneNum)
					{
						variable[j] = random.nextDouble();
					}
				}
				population.individual[i].setVariable(variable);
			}
		}
		return population;
	}
}
