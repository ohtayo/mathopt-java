package jp.ohtayo.mathopt.function;

import java.lang.reflect.Method;

import jp.ohtayo.commons.log.Logging;

/**
 * 最適化アルゴリズムの目的関数を計算するクラスです。<br>
 * クラス名を指定することで別途用意した目的関数計算クラスを呼び出して計算を実行します。<br>
 *
 * @author ohtayo (ohta.yoshihiro@outlook.jp)
 */
public class ObjectiveFunction {
	
	/**
	 * 目的関数を計算して適応度を返します。<br>
	 * @param variables 変数
	 * @param nameOfObjectiveFunction 目的関数クラスの名前
	 * @return 適応度
	 */
	public static Object execute(double[] variables,String nameOfObjectiveFunction)
	{
		Class<?> cClass;
		try{
			//Staticでクラス定義と関数実行する
			cClass = Class.forName(nameOfObjectiveFunction);				//クラスを定義
			Method method = cClass.getMethod("execute", double[].class);	//関数を取得
			Object ret = method.invoke(cClass, variables);					//関数の実行
			
			return ret;	//値を型変換して返す
		}
		catch(Exception e)
		{
			Logging.logger.severe(e.toString());
			return null;
		}
	}

	/**
	 * 目的関数の最大値を返します。<br>
	 * @param nameOfObjectiveFunction 目的関数のクラス名
	 * @return 最大値
	 */
	public static Object getMaxValue(String nameOfObjectiveFunction)
	{
		Class<?> cClass;
		try{
			//Staticでクラス定義と関数実行する
			cClass = Class.forName(nameOfObjectiveFunction);	//クラスを定義
			Method method = cClass.getMethod("getMaxValue");	//関数を取得
			Object ret = method.invoke(cClass);					//関数の実行
			return ret;	//値を型変換して返す
		}
		catch(Exception e)
		{
			Logging.logger.severe(e.toString());
			return null;
		}
	}

	/**
	 * 目的関数の最小値を返します。<br>
	 * @param nameOfObjectiveFunction 目的関数のクラス名
	 * @return 最小値
	 */
	public static Object getMinValue(String nameOfObjectiveFunction)
	{
		Class<?> cClass;
		try{
			//Staticでクラス定義と関数実行する
			cClass = Class.forName(nameOfObjectiveFunction);	//クラスを定義
			Method method = cClass.getMethod("getMinValue");	//関数を取得
			Object ret = method.invoke(cClass);					//関数の実行
			return ret;	//値を型変換して返す
		}
		catch(Exception e)
		{
			Logging.logger.severe(e.toString());
			return null;
		}
	}
	
}
