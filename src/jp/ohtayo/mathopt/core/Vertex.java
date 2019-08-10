package jp.ohtayo.mathopt.core;

import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.math.Vector;

/**
 * 滑降シンプレックス法のシンプレックスの頂点を表すクラスです。
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class Vertex extends RealCodedIndividual {

	/**
	 * コンストラクタ
	 * @param numberOfVariables	変数の数
	 * @param numberOfObjectives	目的の数
	 */
	public Vertex(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives);
	}
	
	
	/**
	 * 反射操作をします。
	 * @param xg 重心にある頂点
	 * @param alpha 反射率
	 * @param nameOfObjectiveFunction 目的関数名
	 * @return 自分から重心に対して反射された頂点
	 */
	public Vertex reflection(Vertex xg, double alpha, String nameOfObjectiveFunction)
	{
		Vector vxg = new Vector(xg.getVariable());
		Vector vx = new Vector(this.getVariable());
		Vertex vertex = this.copy();
		
		//xr = xo + α(xo-xh) (xhが自分(最悪値))
		vertex.setVariable( vxg.minus(vx).multiply(alpha).plus(vxg).get() );
		vertex.setVariable(Numeric.limit(vertex.getVariable(), 1, 0));
		vertex.evaluate(nameOfObjectiveFunction);
		return vertex;
	}
	
	/**
	 * 拡大操作をします。
	 * @param xg 重心にある頂点
	 * @param ganma 拡大率
	 * @param nameOfObjectiveFunction 目的関数名
	 * @return 拡大操作された頂点
	 */
	public Vertex expansion(Vertex xg, double ganma, String nameOfObjectiveFunction)
	{
		Vector vxg = new Vector(xg.getVariable());
		Vector vx = new Vector(this.getVariable());
		Vertex vertex = this.copy();
		
		//xe = xo + γ(xr-xo) (xrが自分)
		vertex.setVariable( vx.minus(vxg).multiply(ganma).plus(vxg).get() );
		vertex.setVariable(Numeric.limit(vertex.getVariable(), 1, 0));
		vertex.evaluate(nameOfObjectiveFunction);
		return vertex;
	}
	
	/**
	 * 縮小操作をします。
	 * @param xg 重心にある頂点
	 * @param beta 縮小率
	 * @param nameOfObjectiveFunction 目的関数名
	 * @return 縮小操作された頂点
	 */
	public Vertex contraction(Vertex xg, double beta, String nameOfObjectiveFunction)
	{
		//xc = xo + γ(xh-xo) (xhが自分)
		return expansion(xg, beta, nameOfObjectiveFunction);
	}
	
	/**
	 * 自身を別の個体iにコピーします
	 * @return コピー先個体
	 */
	public Vertex copy()
	{
		//コピー先を用意
		Vertex result = new Vertex(variable.length, fitness.length);
		
		result.setVariable(this.variable);	//変数のコピー
		System.arraycopy(this.fitness, 0, result.fitness, 0, this.fitness.length);	//適応度のコピー

		return result;
	}
}
