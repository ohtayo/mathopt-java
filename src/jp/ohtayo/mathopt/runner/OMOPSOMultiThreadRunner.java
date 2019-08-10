package jp.ohtayo.mathopt.runner;

import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.mathopt.algorithm.OMOPSOMultiThreadConstraint;
import jp.ohtayo.mathopt.config.ConfigMOPSO;

import java.io.File;

/**
 * MOPSOクラスのサンプルプログラム<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class OMOPSOMultiThreadRunner {

	public static void main(String[] args) {

		ConfigMOPSO config = new ConfigMOPSO();
		try{
			//設定ファイル読み込み
			config.read(".\\xml\\mopso.config.xml");
		}catch(Exception e){
			Logging.logger.severe(e.toString() + "\nプログラムを終了します。");
			return;
		}
		Logging.logger.info("以下のパラメータを読み込みました。\n"
				+ "\n  numberOfParticles  = " + config.numberOfParticles
				+ "\n  numberOfVariables  = " + config.numberOfVariables
				+ "\n  numberOfIterations = " + config.numberOfIterations
				+ "\n  numberOfObjectives = " + config.numberOfObjectives
				+ "\n  numberOfConstraints = " + config.numberOfConstraints
				+ "\n  nameOfObjectiveFunction = " + config.nameOfObjectiveFunction
				+ "\n  filenameOfInitialSolutions = " + config.filenameOfInitialSolutions
				+ "\n  epsilon = " + String.valueOf(config.epsilon)
				+ "\n  alpha = " + String.valueOf(config.alpha)
				+ "\n");

		//入力エラーチェック
		try{
			config.inputErrorCheck(
					Integer.valueOf(config.numberOfVariables),
					Integer.valueOf(config.numberOfParticles),
					Integer.valueOf(config.numberOfIterations),
					Integer.valueOf(config.numberOfObjectives),
					Integer.valueOf(config.numberOfConstraints),
					String.valueOf(config.nameOfObjectiveFunction),
					String.valueOf(config.filenameOfInitialSolutions),
					Double.valueOf(config.epsilon),
					Double.valueOf(config.alpha)
					);
		}catch(Exception e){
			Logging.logger.severe("コンフィグファイル(mopso.config.xml)の内容を見なおしてください。");
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

		int numberOfThreads = 8;

		OMOPSOMultiThreadConstraint mopso = new OMOPSOMultiThreadConstraint();

		mopso.main(
				Integer.valueOf(config.numberOfVariables),
				Integer.valueOf(config.numberOfParticles),
				Integer.valueOf(config.numberOfIterations),
				Integer.valueOf(config.numberOfObjectives),
				String.valueOf(config.nameOfObjectiveFunction),
				Integer.valueOf(config.numberOfConstraints),
				Double.valueOf(config.epsilon),
				Double.valueOf(config.alpha),
				numberOfThreads,
				config.filenameOfInitialSolutions
				);

		Logging.logger.info("計算を終了します。");
	}
}
