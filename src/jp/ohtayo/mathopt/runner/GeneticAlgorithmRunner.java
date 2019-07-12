package jp.ohtayo.mathopt.runner;

import java.io.File;

import jp.ohtayo.mathopt.config.ConfigGA;
import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.mathopt.algorithm.SimpleGeneticAlgorithm;

/**
 * GAクラスのサンプルプログラム<br>
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class GeneticAlgorithmRunner {

	public static void main(String[] args) {

		ConfigGA config = new ConfigGA();
		if(	!config.read(".\\xml\\ga.config.xml") )	{
			Logging.logger.severe("\nプログラムを終了します。");
			return;
		}

		Logging.logger.info("以下のパラメータを読み込みました。\n"
				+ "\n  numberOfBits = " 		+ config.numberOfBits
				+ "\n  numberOfVariables  = " 	+ config.numberOfVariables
				+ "\n  numberOfPopulations  = " + config.numberOfPopulations
				+ "\n  numberOfIterations = " 	+ config.numberOfIterations
				+ "\n  nameOfObjectiveFunction = " + config.nameOfObjectiveFunction
				+ "\n  rateOfCrossOver = " 		+ config.rateOfCrossOver
				+ "\n  rateOfMutation = " 		+ config.rateOfMutation
				+ "\n  numberOfElite = " 		+ config.numberOfElite
				+ "\n");
		
		//入力エラーチェック
		try{
			config.inputErrorCheck( 
					Integer.valueOf(config.numberOfBits),
					Integer.valueOf(config.numberOfVariables),
					Integer.valueOf(config.numberOfPopulations),
					Integer.valueOf(config.numberOfIterations),
					config.nameOfObjectiveFunction,
					Double.valueOf(config.rateOfCrossOver),
					Double.valueOf(config.rateOfMutation),
					Double.valueOf(config.numberOfElite)
					);
		}catch(Exception e){
			Logging.logger.severe("コンフィグファイル(ga.config.xml)の内容を見なおしてください。");
			Logging.logger.info("プログラムを終了します。");
			return;
		}

		
		//resultフォルダがなければ作成
		File dir = new File("./result");
		if(dir.exists() == false){
			dir.mkdir();
			Logging.logger.info("resultフォルダを作成しました。");
		}
		
		//mopsoの計算実行
		Logging.logger.info("計算を開始します。");

		SimpleGeneticAlgorithm.main(
				Integer.valueOf(config.numberOfBits),
				Integer.valueOf(config.numberOfVariables),
				Integer.valueOf(config.numberOfPopulations),
				Integer.valueOf(config.numberOfIterations),
				config.nameOfObjectiveFunction,
				Double.valueOf(config.rateOfCrossOver),
				Double.valueOf(config.rateOfMutation),
				Integer.valueOf(config.numberOfElite)
				);
		
		Logging.logger.info("計算を終了します。");
	}
}
