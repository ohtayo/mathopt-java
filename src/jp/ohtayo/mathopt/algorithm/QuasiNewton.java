package jp.ohtayo.mathopt.algorithm;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.math.Numeric;

import jp.ohtayo.mathopt.function.ObjectiveFunction;
/**
 * 準ニュートン法の探索を行うプログラムです．
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class QuasiNewton {
	
	private String nameOfObjectiveFunction;
	private double step;
	private double maxOfIterations;
	private double epsilon;
	private int numberOfVariables;
	
	/**
	 * コンストラクタ
	 * @param numberOfVariables
	 * @param maxOfIterations
	 * @param epsilon
	 * @param step
	 * @param nameOfObjectiveFunction
	 */
	public QuasiNewton(int numberOfVariables, int maxOfIterations, double epsilon, double step, String nameOfObjectiveFunction)
	{
		this.numberOfVariables = numberOfVariables;
		this.nameOfObjectiveFunction = nameOfObjectiveFunction;
		this.maxOfIterations = maxOfIterations;
		this.epsilon = epsilon;
		this.step = step;
	}
	
	/**
	 * 準ニュートン法のメイン関数です。<br>
	 * 準ニュートン法の計算は本関数を呼び出して行います。<br>
	 * @return 最終世代の適応度
	 */
	public double solve( double[] x0 )
	{
		//初期化
		Vector x = new Vector(x0);
		Matrix identity = new Matrix(numberOfVariables, numberOfVariables, Matrix.CONSTRUCT_IDENTITY);
		Matrix hesse = new Matrix(numberOfVariables, numberOfVariables, Matrix.CONSTRUCT_IDENTITY);
		Matrix g2 = gradient(x);
		
		for (int iterate = 0; iterate<maxOfIterations; iterate++)
		{
			Logging.logger.info(iterate+1 + "回目の計算を始めます。");
			
			Matrix g1 = new Matrix(g2);
			if( g1.getColumn(0).norm() < epsilon ) break;	//収束判定
			Matrix p = hesse.multiply(-1).multiply(g1);
			double k = goldenSection(x, p);
			Matrix s = p.multiply(k);
			x = x.plus(s.getColumn(0));
			
			//Hessian行列を計算する
			g2 = gradient(x);
			Matrix y = g2.minus(g1);
			Matrix yt = y.transpose();
			Matrix st = y.transpose();
			double z = st.multiply(y).get()[0][0];
			if( z==0.0 ) break;	//収束判定
			
			//BFGS式
			Matrix temp1 = identity.minus( s.multiply(yt).multiply(1/z) );
			Matrix temp2 = identity.minus( y.multiply(st).multiply(1/z) );
			Matrix temp3 = s.multiply(st).multiply(1/z);
			hesse = temp1.multiply(hesse).multiply(temp2).plus( temp3 );
			
			limit(x);
			
			Logging.logger.info(x.toString());
			Logging.logger.info("fitness = "+String.valueOf(evaluate(x.get())[0]));
		}
		
		/*//適応度変化の描画
		Vector horizon = new Vector(1,1,numberOfIterations);
		Matrix fitness = new Matrix(horizon.length(),1+1);
		fitness.setColumn(0, horizon); 	//横軸を設定
		fitness.setColumn(1, bestFitness);	//縦軸は適応度
		Figure fig = new Figure("fitness","iterations","fitness");
		fig.plot(fitness);
		*/
		return evaluate(x.get())[0];
	}
	
	public double solve()
	{
		Vector x0 = new Vector(numberOfVariables, "random");
		return solve(x0.get());
	}
	
	/**
	 * 勾配を算出<br>
	 * @param x 変数
	 * @return 勾配ベクトル
	 */
	private Matrix gradient(Vector x)
	{
		double H = (epsilon*2);
		Matrix g = new Matrix(x.length(), 1);
		
		for(int i=0; i<x.length(); i++ )
		{
			Vector x1, x2;
			x1 = new Vector(x);
			x2 = new Vector(x);
			x1.set(i, x1.get(i)-H );
			x2.set(i, x2.get(i)+H );
			double y1 = evaluate(x1.get())[0];
			double y2 = evaluate(x2.get())[0];
			g.set(i, 0, (y2-y1)/(H*2) );
		}
		return g;
	}
	
	/**
	 * 黄金探索法<br>
	 * @param x 変数
	 * @param pmatrix 行列P
	 * @return 探索した係数k
	 */
	private double goldenSection(Vector x, Matrix pmatrix)
	{
		Vector p = pmatrix.getColumn(0);
		double TAU = 0.61803398874989484820458683436564;
		double a = -step;
		double b = step;
		double x1 = b - TAU * (b-a);
		double x2 = a + TAU * (b-a);
		double f1 = evaluate( x.plus(p.multiply(x1)).get() )[0];
		double f2 = evaluate( x.plus(p.multiply(x2)).get() )[0];
		
		for(int i=0; (i<maxOfIterations) && (Math.abs(b-a)>epsilon); i++ )
		{
			if( f2 > f1 ){
				b = x2;
				x2 = x1;
				x1 = a + (1-TAU)*(b-a);
				f2 = f1;
				f1 = evaluate( x.plus(p.multiply(x1)).get() )[0];
			}else{
				a = x1;
				x1 = x2;
				x2 = b - (1-TAU)*(b-a);
				f1 = f2;
				f2 = evaluate( x.plus(p.multiply(x2)).get() )[0];
			}
		}
		return (x1+x2)/2;
	}
	
	/**
	 * 目的関数の計算と適応度の更新を行います<br>
	 * @param variable 変数値
	 * @return 適応度
	 */
	private double[] evaluate(double[] variable)
	{
		return (double[])ObjectiveFunction.execute(variable, nameOfObjectiveFunction);
	}
	
	/**
	 * 変数にリミッターをかける
	 * @param x リミッターをかけるベクトル
	 */
	public void limit(Vector x)
	{
		for(int i=0; i<x.length(); i++){
			x.set(i, Numeric.limit(x.get(i), 1.0, 0.0) );
		}
	}
}
