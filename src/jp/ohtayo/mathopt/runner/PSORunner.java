package jp.ohtayo.mathopt.runner;

import java.io.File;

import jp.ohtayo.mathopt.config.ConfigPSO;
import jp.ohtayo.mathopt.algorithm.PSO;
import jp.ohtayo.commons.log.Logging;

/**
 * PSOクラスのサンプルプログラム<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class PSORunner {

	public static void main(String[] args) {

		ConfigPSO config = new ConfigPSO();
		try{
			//設定ファイル読み込み
			config.read(".\\xml\\pso.config.xml");
		}catch(Exception e){
			Logging.logger.severe(e.toString() + "\nプログラムを終了します。");
			return;
		}
		Logging.logger.info("以下のパラメータを読み込みました。\n"
				+ "\n  numberOfParticles  = " + config.numberOfParticles
				+ "\n  numberOfVariables  = " + config.numberOfVariables
				+ "\n  numberOfIterations = " + config.numberOfIterations
				+ "\n  nameOfObjectiveFunction = " + config.nameOfObjectiveFunction
				+ "\n  w = " + config.weight
				+ "\n  c1 = " + config.constant1
				+ "\n  c2 = " + config.constant2
				+ "\n");
		
		//入力エラーチェック
		try{
			config.inputErrorCheck(
					Integer.valueOf(config.numberOfVariables),
					Integer.valueOf(config.numberOfParticles),
					Integer.valueOf(config.numberOfIterations),
					config.nameOfObjectiveFunction,
					Double.valueOf(config.weight),
					Double.valueOf(config.constant1),
					Double.valueOf(config.constant2)
					);
		}catch(Exception e){
			Logging.logger.severe("コンフィグファイル(pso.config.xml)の内容を見なおしてください。");
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

		PSO pso = new PSO();
		pso.main(
				Integer.valueOf(config.numberOfVariables),
				Integer.valueOf(config.numberOfParticles),
				Integer.valueOf(config.numberOfIterations),
				config.nameOfObjectiveFunction,
				Double.valueOf(config.weight),
				Double.valueOf(config.constant1),
				Double.valueOf(config.constant2)
				);
		
		Logging.logger.info("計算を終了します。");
	}
}
