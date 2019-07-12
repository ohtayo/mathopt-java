package jp.ohtayo.mathopt.core;

/**
 * 評価値クラスです．<br>
 * 制約条件がある場合に多目的最適化の目的関数評価値を格納します．<br>
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class EvaluatedValues {
	public double[] fitness;
	public double[] constraintViolation;

	/**
	 * コンストラクタ
	 * @param numberOfObjectives
	 */
	public EvaluatedValues(int numberOfObjectives){
		fitness = new double[numberOfObjectives];
		constraintViolation = new double[0];	//制約を長さ0の配列で初期化
	}
	/**
	 * コンストラクタ
	 * @param numberOfObjectives
	 * @param numberOfConstraints
	 */
	public EvaluatedValues(int numberOfObjectives, int numberOfConstraints){
		fitness = new double[numberOfObjectives];
		constraintViolation = new double[numberOfConstraints];
	}

	public String toString() {
		String s = "";
		s += "fitness: \r\n";
		for (int i = 0; i < this.fitness.length; i++)
			s += "  " + this.fitness[i];
		s += "\r\nValue of constraint violation: \r\n";
		for (int i = 0; i < this.constraintViolation.length; i++)
			s += "  " + this.constraintViolation[i];
		s += "\r\n";
		return s;
	}
}
