package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.mathopt.core.Vertex;
import jp.ohtayo.mathopt.core.Simplex;

/**
 * 滑降シンプレックス法を計算するクラスです。
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class DownhillSimplex {

	/**
	 * メイン関数です。<br>
	 * @param numberOfPoints 探索点の数
	 * @param numberOfVariables 変数の数
	 * @param numberOfIterations 世代数
	 * @param nameOfObjectiveFunction 目的関数名
	 * @param alpha 反射率α
	 * @param beta 縮小率β
	 * @param ganma 拡大率γ
	 * @return 最終世代の最優秀適応度
	 */
	public static double main(
			int numberOfPoints,
			int numberOfVariables,
			int numberOfIterations,
			String nameOfObjectiveFunction, 
			double alpha, 
			double beta,
			double ganma)
	{
		int numberOfObjectives = 1;
		Vertex xh;	// 最悪点
		Vertex xs;	// 2番めに悪い点
		Vertex xl;	// 最良点
		Vertex xr;	// 重心を中心とした対称位置
		Vertex xe;	// 重心からxr方向にγ倍延長した位置
		Vertex xc;	// 重心からxh方向にβ倍縮小した位置
		int indexh;	// 最悪値のインデックス

		//1. 初期化
		Simplex simplex = new Simplex(numberOfPoints);
		simplex.initialize(numberOfVariables, numberOfPoints, numberOfObjectives, nameOfObjectiveFunction);
		simplex.evaluate(nameOfObjectiveFunction);

		Vector fitness;
		Vector bestFitness = new Vector(numberOfIterations);
		Matrix bestVariables = new Matrix(numberOfIterations, numberOfVariables);
		int[] index = {0};
		
		for (int iterate = 0; iterate<numberOfIterations; iterate++){
			Logging.logger.info(iterate+1 + "世代目の計算を始めます。");
			//最優秀適応度の表示
			bestFitness.set(iterate, new Matrix(simplex.getFitness()).getColumn(0).min(index) );
			
			double[] test = simplex.individual[index[0]].getVariable();
			Vector debug = new Vector(test);
			bestVariables.setRow(iterate, debug);
			//bestVariables.setRow(iterate, new Vector(simplex.individual[index[0]].getVariable()));
			Logging.logger.info(simplex.individual[index[0]].toString() );

			//適応度配列の取得
			fitness = new Matrix( simplex.getFitness() ).getColumn(0);
			
			//2. xh, xs, xlと、xhを除いた重心xgを求める
			fitness.max(index);	//最大値のindexを求める
			indexh = index[0];
			xh = (Vertex) simplex.individual[indexh].copy();
			
			fitness.set(index[0], fitness.mean());	//最大値を最大ではなくする。
			fitness.max(index);	//2番目の最大値をもとめる
			xs = (Vertex) simplex.individual[index[0]].copy();
			
			fitness.min(index);	//最小値を求める
			xl = (Vertex) simplex.individual[index[0]].copy();
			
			simplex.calculateCentroid();	//重心を求める。
			
			//3. 反射操作によりxrを求め、評価する
			xr = xh.reflection(simplex.xg, alpha, nameOfObjectiveFunction);
			
			//4. f(xr)< f(xl)なら拡大操作でxeを求め評価する
			if( xr.getFitness()[0] < xl.getFitness()[0] ){
				xe = xr.expansion(simplex.xg, ganma, nameOfObjectiveFunction);

				//f(xe)<f(xr)ならxh=xeでもどる
				if( xe.getFitness()[0] < xr.getFitness()[0] ){
					simplex.individual[indexh]=xe.copy();
				//f(xe)>=f(xr)ならxh=xrでもどる
				}else{
					simplex.individual[indexh]=xr.copy();
				}
				continue;
			}
			
			//5. f(xs)<f(xr)<f(xh)ならxhをxrに置き換えて縮小操作でxcを求め評価する
			if( (xs.getFitness()[0]<xr.getFitness()[0]) || (xr.getFitness()[0]<xh.getFitness()[0]) ){
				xh = xr.copy();
				simplex.individual[indexh]=xr.copy();
				xc = xh.contraction(simplex.xg, beta, nameOfObjectiveFunction);

				//f(xc)<f(xh)ならxh=xcでもどる
				if(xc.getFitness()[0]<xh.getFitness()[0]){
					simplex.individual[indexh]=xc.copy();
				//さもなければ収縮操作で位置を更新し評価して戻る
				}else{
					simplex.reduction(nameOfObjectiveFunction);
				}
				continue;
			}
			
			//6. f(xh)<=f(xr)なら縮小操作でxcを求め評価する
			if( xh.getFitness()[0]<=xr.getFitness()[0] ){
				xc = xh.contraction(simplex.xg, beta, nameOfObjectiveFunction);
				
				//f(xc)<f(xh)ならxh=xcでもどる
				if(xc.getFitness()[0]<xh.getFitness()[0]){
					simplex.individual[indexh]=xc.copy();
				//さもなければ収縮操作で位置を更新し評価して戻る
				}else{
					simplex.reduction(nameOfObjectiveFunction);
				}
				continue;
			}
			
			//上記条件以外の時xhをxrで置き換えて手順2へ戻る
			simplex.individual[indexh]=xr.copy();
		}
		
		//適応度変化の描画
		/*Vector horizon = new Vector(1,1,numberOfIterations);
		Matrix fitness2 = new Matrix(horizon.length(),1+1);
		fitness2.setColumn(0, horizon); 	//横軸を設定
		fitness2.setColumn(1, bestFitness);	//縦軸は適応度
		Figure fig = new Figure("fitness","iterations","fitness");
		fig.plot(fitness2);
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
}
