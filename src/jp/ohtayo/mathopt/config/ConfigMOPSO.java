package jp.ohtayo.mathopt.config;

import jp.ohtayo.commons.io.ConfigBase;
import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.commons.util.StringUtility;
import jp.ohtayo.mathopt.function.ObjectiveFunction;

/**
 * 多目的粒子群最適化(MOPSO)のコンフィグ設定をXMLで外部に保存・読込するクラスです。<br>
 *
 * @author ohtayo <ohta.yoshihiro@outlook.jp>
 */
public class ConfigMOPSO extends ConfigBase {

	/** 変数の数	*/	public String numberOfVariables;
	/** 粒子の数	*/	public String numberOfParticles;
	/** 評価回数	*/	public String numberOfIterations;
	/** 目的関数の数*/	public String numberOfObjectives;
	/** 制約条件の数*/	public String numberOfConstraints;
	/** 目的関数名	*/	public String nameOfObjectiveFunction;
	/** ε値		*/	public String epsilon;
	/** α値		*/	public String alpha;
	/** 初期解	*/	public String filenameOfInitialSolutions;

	/**
	 * 入力エラーチェックをします。
	 * @param numberOfVariables
	 * @param numberOfParticles
	 * @param numberOfIterations
	 * @param numberOfObjectives
	 * @param numberOfConstraints
	 * @param nameOfObjectiveFunction
	 * @param fileNameOfInitialSolutions
	 * @param epsilon
	 * @param alpha
	 * @throws Exception
	 */
	public void inputErrorCheck(int numberOfVariables, int numberOfParticles, int numberOfIterations,
			 int numberOfObjectives, int numberOfConstraints, String nameOfObjectiveFunction,
			 String fileNameOfInitialSolutions,double epsilon, double alpha) throws Exception
	{
		//変数は2つ以上必要
		if (numberOfVariables < 2){
			Logging.logger.severe("変数長(numberOfVariables)が短すぎます。");
			throw new IllegalArgumentException();
		}
		//粒子数は3つ以上必要
		if (numberOfParticles < 3){
			Logging.logger.severe("粒子数(numberOfParticles)が少なすぎます。");
			throw new IllegalArgumentException();
		}
		if (numberOfIterations < 0){
			Logging.logger.severe("繰返し回数(numberOfIterations)が少なすぎます。");
			throw new IllegalArgumentException();
		}
		//目的数は1以上
		if (numberOfObjectives < 1){
			Logging.logger.severe("目的数(numberOfObjectives)が少なすぎます。");
			throw new IllegalArgumentException();
		}
		//制約数数は0以上
		if (numberOfConstraints < 0){
			Logging.logger.severe("制約数(numberOfConstraints)が少なすぎます。");
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

		//目的数と目的関数の返り値を確認
		if (result.length != numberOfObjectives)
		{
			Logging.logger.severe("目的数と目的関数の返り値が合致しません。");
			throw new IllegalArgumentException();
		}
		//epsilonとalphaは0以上1未満
		if (epsilon < 0){
			Logging.logger.severe("epsilonが小さすぎます。");
			throw new IllegalArgumentException();
		}
		if (epsilon >= 1){
			Logging.logger.severe("epsilonが大きすぎます。");
			throw new IllegalArgumentException();
		}
		if (alpha < 0){
			Logging.logger.severe("alphaが小さすぎます。");
			throw new IllegalArgumentException();
		}
		if (alpha >= 1){
			Logging.logger.severe("alphaが大きすぎます。");
			throw new IllegalArgumentException();
		}
	}
}
