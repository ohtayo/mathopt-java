package jp.ohtayo.mathopt.config;

import jp.ohtayo.commons.io.ConfigBase;
import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Vector;
import jp.ohtayo.mathopt.function.ObjectiveFunction;
import jp.ohtayo.commons.util.StringUtility;

/**
 * 遺伝的アルゴリズム(GA)のコンフィグ設定をXMLで外部に保存・読込するクラスです。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class ConfigGA extends ConfigBase {
	
	/** 染色体のビット数*/	public String numberOfBits;
	/** 変数の数		*/	public String numberOfVariables;
	/** 個体の数		*/	public String numberOfPopulations;
	/** 評価回数		*/	public String numberOfIterations;
	/** 目的関数名		*/	public String nameOfObjectiveFunction;
	/** 交叉確率		*/	public String rateOfCrossOver;
	/** 突然変異数		*/	public String rateOfMutation;
	/** エリート数		*/	public String numberOfElite;

	/**
	 * 入力エラーチェックをします。
	 * @param numberOfBits
	 * @param numberOfVariables
	 * @param numberOfPopulations
	 * @param numberOfIterations
	 * @param nameOfObjectiveFunction
	 * @param rateOfCrossOver
	 * @param rateOfMutation
	 * @param numberOfElite
	 * @throws Exception
	 */
	public void inputErrorCheck(int numberOfBits, int numberOfVariables, int numberOfPopulations, int numberOfIterations,
			String nameOfObjectiveFunction, double rateOfCrossOver, double rateOfMutation, double numberOfElite) throws Exception
	{
		//変数は1つ以上必要
		if (numberOfVariables < 1){
			Logging.logger.severe("変数長(numberOfVariables)が短すぎます。");
			throw new IllegalArgumentException();
		}
		if (numberOfPopulations < 1){
			Logging.logger.severe("個体数(numberOfParticles)が少なすぎます。");
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
		
		if (rateOfCrossOver < 0 || rateOfCrossOver > 1){
			Logging.logger.severe("rateOfCrossOverが異常値です。");
			throw new IllegalArgumentException();
		}
		if (rateOfMutation < 0 || rateOfMutation > 1){
			Logging.logger.severe("rateOfMutationが異常値です。");
			throw new IllegalArgumentException();
		}
		if (numberOfElite < 0 || numberOfElite > numberOfPopulations){
			Logging.logger.severe("numberOfEliteが異常値です。");
			throw new IllegalArgumentException();
		}
	}
}
