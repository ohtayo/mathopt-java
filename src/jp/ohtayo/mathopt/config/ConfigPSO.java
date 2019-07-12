package jp.ohtayo.mathopt.config;

import jp.ohtayo.commons.io.ConfigBase;
import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.util.StringUtility;
import jp.ohtayo.mathopt.function.ObjectiveFunction;
/**
 * 粒子群最適化(PSO)のコンフィグ設定をXMLで外部に保存・読込するクラスです。<br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class ConfigPSO extends ConfigBase {
	
	/** 変数の数	*/	public String numberOfVariables;
	/** 粒子の数	*/	public String numberOfParticles;
	/** 評価回数	*/	public String numberOfIterations;
	/** 目的関数名	*/	public String nameOfObjectiveFunction;
	/** 重みw		*/	public String weight;
	/** 定数c1		*/	public String constant1;
	/** 定数c2		*/	public String constant2;
		
	/**
	 * 入力エラーチェックをします。
	 * @param numberOfVariables
	 * @param numberOfParticles
	 * @param numberOfIterations
	 * @param nameOfObjectiveFunction
	 * @param weight
	 * @param constant1
	 * @param constant2
	 * @throws Exception
	 */
	public void inputErrorCheck(int numberOfVariables, int numberOfParticles, int numberOfIterations,
			String nameOfObjectiveFunction, double weight, double constant1, double constant2) throws Exception
	{
		//変数は1つ以上必要
		if (numberOfVariables < 1){
			Logging.logger.severe("変数長(numberOfVariables)が短すぎます。");
			throw new IllegalArgumentException();
		}
		//粒子数は1つ以上必要
		if (numberOfParticles < 1){
			Logging.logger.severe("粒子数(numberOfParticles)が少なすぎます。");
			throw new IllegalArgumentException();
		}
		if (numberOfIterations < 0){
			Logging.logger.severe("繰返し回数(numberOfIterations)が少なすぎます。");
			throw new IllegalArgumentException();
		}
		//目的関数名に何もなければエラー
		if (StringUtility.isNullOrEmpty(nameOfObjectiveFunction)){
			Logging.logger.severe("目的関数名がありません。");
			throw new IllegalArgumentException();
		}
		
		double[] result;
		//ObjectiveFunctionを試しに実行。動かなければエラー
		result = (double[])ObjectiveFunction.execute(new Vector(numberOfVariables, 1).get(), nameOfObjectiveFunction);
		if(result == null){
			Logging.logger.severe("目的関数名もしくは変数長にミスがあります。");
			throw new IllegalArgumentException();
		}
		result = (double[])ObjectiveFunction.getMaxValue(nameOfObjectiveFunction);
		if(result == null)
		{
			Logging.logger.severe("目的関数の最大値が取得できません。");
			throw new IllegalArgumentException();
		}
		result = (double[])ObjectiveFunction.getMinValue(nameOfObjectiveFunction);
		if(result == null)
		{
			Logging.logger.severe("目的関数の最小値が取得できません。");
			throw new IllegalArgumentException();
		}
		
		//epsilonとalphaは0以上1未満
		if (weight < 0 || weight > 1){
			Logging.logger.severe("weightが異常値です。");
			throw new IllegalArgumentException();
		}
		if (constant1 < 0 || constant1 > 2){
			Logging.logger.severe("c1が異常値です。");
			throw new IllegalArgumentException();
		}
		if (constant2 < 0 || constant2 > 2){
			Logging.logger.severe("c2が異常値です。");
			throw new IllegalArgumentException();
		}
	}
}
