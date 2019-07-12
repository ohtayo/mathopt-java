package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.random.Random;
import jp.ohtayo.mathopt.core.Individual;
import jp.ohtayo.mathopt.core.RealCodedIndividual;

/**
 * 焼きなまし法(シミュレーテッドアニーリング)を計算するプログラムです．
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class SimulatedAnnealing {

	/**
	 * SAのメイン関数です。<br>
	 * SAの計算は本関数を呼び出して行います。<br>
	 * @param numberOfVariables 変数の数
	 * @param numberOfIterations 世代数
	 * @param nameOfObjectiveFunction 目的関数名
	 * @param temperature 初期温度
	 * @param step 変数変更幅
	 * @param ganma 温度変更比ガンマ値
	 * @return 最終世代の適応度
	 */
	public static double main(
			int numberOfVariables, 
			int numberOfIterations,
			String nameOfObjectiveFunction, 
			double temperature,
			double numberOfAnnealing, 
			double step,
			double ganma)
	{
		Random random = new Random();
		//初期化
		Individual state = new RealCodedIndividual(numberOfVariables, 1);	//変数＝現在の状態
		state.setVariable( random.array(state.getNumberOfVariables()) );	//状態の初期化
		state.evaluate(nameOfObjectiveFunction);		//状態の評価
		Individual nextState = state.copy();			//次の状態
		nextState.evaluate(nameOfObjectiveFunction);	//次状態の評価
		
		Vector bestFitness = new Vector(numberOfIterations);
		Matrix bestVariables = new Matrix(numberOfIterations, numberOfVariables);
		
		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
		
			//アニーリング
			//現在の温度Tkで一定期間以下の処理を繰り返す
			for(int a = 0; a < numberOfAnnealing; a++ )
			{
				//次状態生成して評価
				nextState = generate(state, step);	
				nextState.evaluate(nameOfObjectiveFunction);
				//受理する場合次状態に遷移
				if(accept(state.getFitness()[0], nextState.getFitness()[0], temperature))
					state = nextState.copy();
			}
			
			//クーリング
			//次の温度Tk+1を決める
			temperature = reduce(temperature, ganma);
			
			//最優秀適応度の表示
			bestFitness.set(iterate, state.getFitness()[0] );
			bestVariables.setRow(iterate, new Vector(state.getVariable()));
			Logging.logger.info(state.toString() );
		}
		
		//適応度変化の描画
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
	 * 生成関数 現在の状態から次に遷移すべき状態を返す
	 * @param state 現在の状態
	 * @param step 次状態までの距離の基準ステップ
	 * @return 次状態
	 */
	private static Individual generate(Individual state, double step){
		//1～-1の配列を作る
		Vector vector = new Vector(state.getVariable().length, "random").multiply(2).minus(1).round();
		//±stepの配列にする
		vector = vector.multiply(step);
		//Individualに加減算して次のindividualを生成
		vector = vector.plus(new Vector(state.getVariable()));
		Individual result = new RealCodedIndividual(state.getVariable().length, 1);
		result.setVariable(vector.get());
		
		//limitをかける
		result.setVariable( Numeric.limit(result.getVariable(), 1, 0) );
		
		return result;
	}
	
	/**
	 * 受理関数 次の状態と現在の状態との差分および温度パラメータを与え、
	 * 次の状態への繊維を受理するか否かを返す。
	 * @param e 現在状態のエネルギー
	 * @param edash 次状態のエネルギー
	 * @param temperature 現在の温度
	 * @return 受理するかどうか
	 */
	private static boolean accept(double e, double edash, double temperature)
	{
		double delta = edash - e;
		double probability;
		if(delta < 0)	probability = 1.0;
		else	probability = (Math.exp(-delta/temperature));
		
		if( new Random().nextDouble() <= probability )	return true;
		else return false;
	}
	
	/**
	 * 徐冷関数 次のステップの温度Tk+1を返す
	 * @param temperature 現在の温度
	 * @param ganma アニーリング係数
	 * @return 次ステップの温度
	 */
	private static double reduce(double temperature, double ganma)
	{
		return ganma * temperature;
		//		return Math.pow(ganma, temperature);
	}
		
}
