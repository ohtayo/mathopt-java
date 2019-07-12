package jp.ohtayo.mathopt.core;

import jp.ohtayo.commons.util.Cast;
import jp.ohtayo.commons.log.Logging;
import jp.ohtayo.commons.math.Numeric;
import jp.ohtayo.commons.math.Matrix;
import jp.ohtayo.commons.math.Vector;

/**
 * 多目的最適化アルゴリズムのうち、個体の優劣やランクを取り扱うメソッドを提供するクラスです。<br>
 * 優劣判定、ランク付け、境界ランク番号計算、混雑距離計算等のメソッドを含みます。<br>
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class Rank {

	/**
	 * ２つの粒子の適応度の優劣を判定します。<br>
	 * ε、もしくはαどちらかの値を入れると、ε-domination,またα-dominationによる優劣判定を行います。<br>
	 * どちらにも値が入っている場合、α-dominationを優先します。<br>
	 *
	 * @param basis 比較対象の適応度
	 * @param target 比較される適応度
	 * @param epsilon ε値
	 * @param alpha α値
	 * @return basisに対しtargetが優越していればtrue / 優越していなければfalse
	 */
	public static boolean dominated(double[] basis, double[] target, double epsilon, double alpha)
	{
		if(alpha > 0.0)
		{
			return dominatedAlpha(basis, target, alpha);
		}
		else if(epsilon > 0.0)
		{
			return dominatedEpsilon(basis, target, epsilon);
		}
		else
		{
			return dominated(basis, target);
		}
	}
	/**
	 * ２つの解の優劣判定をします。<br>
	 * どちらかの解が制約を満たしていなければ自動的に制約を満たした解を優越とします。<br>
	 * 両方の解が制約を満たしていない場合、制約違反量で優越判定します。<br>
	 * どちらの解も制約を満たしていれば、評価値で優越判定します。<br>
	 * ε、もしくはαどちらかの値を入れると、評価値をε-domination,またα-dominationで優劣判定します。<br>
	 * εおよびαどちらにも値が入っている場合、α-dominationを優先します。<br>
	 * @param basis 比較対象の適応度
	 * @param target 比較される適応度
	 * @param constraintBasis 比較対象の制約違反量
	 * @param constraintTarget 比較される側の制約違反量
	 * @param epsilon ε値
	 * @param alpha α値
	 * @return basisがtargetより優越していればtrue / 優越していなければfalse
	 */
	public static boolean dominated(double[] basis, double[] target, double constraintBasis, double constraintTarget, double epsilon, double alpha)
	{
		//制約がある場合
		if( !Double.isNaN(constraintBasis) && !Double.isNaN(constraintTarget) ){
		//どちらも制約を満たさなければ制約違反量で比較する
			if (constraintBasis > 0 && constraintTarget > 0){
				return (constraintTarget >= constraintBasis);
			}else if(constraintBasis > 0){
				//targetのみ制約を満たしていればbasisは優越していない
				return false;
			}else if(constraintTarget > 0){
				//basisのみが制約を満たしていればbasisはtargetよりも優越している
				return true;
			}
		}
		//制約が無いか，どちらも制約を満たしていれば目的関数で比較する
		return dominated(basis, target, epsilon, alpha);
	}
	
	/**
	 * ２つの解の優劣判定をします。<br>
	 * 制約数が1つなら，どちらかの解が制約を満たしていなければ自動的に制約を満たした解を優越とします。<br>
	 * 
	 * 両方の解が制約を満たしていない場合、制約違反量で優越判定します。<br>
	 * どちらの解も制約を満たしていれば、評価値で優越判定します。<br>
	 * ε、もしくはαどちらかの値を入れると、評価値をε-domination,またα-dominationで優劣判定します。<br>
	 * εおよびαどちらにも値が入っている場合、α-dominationを優先します。<br>
	 * @param basis 比較対象の適応度
	 * @param target 比較される適応度
	 * @param constraintBasis 比較対象の制約違反量
	 * @param constraintTarget 比較される側の制約違反量
	 * @param epsilon ε値
	 * @param alpha α値
	 * @return basisがtargetより優越していればtrue / 優越していなければfalse
	 */
	public static boolean dominated(double[] basis, double[] target, double[] constraintBasis, double[] constraintTarget, double epsilon, double alpha)
	{
		//制約数を数える
		int numberOfConstraints = constraintBasis.length;
		
		//制約数が1
		if(numberOfConstraints==1){
			//どちらも制約を満たさなければ制約違反量で比較する
			if (constraintBasis[0] > 0 && constraintTarget[0] > 0){
				return (constraintTarget[0] >= constraintBasis[0]);
			}else if(constraintBasis[0] > 0){
				//targetのみ制約を満たしていればbasisは優越していない
				return false;
			}else if(constraintTarget[0] > 0){
				//basisのみが制約を満たしていればbasisはtargetよりも優越している
				return true;
			}
		}
		
		//制約数が2以上
		else if(numberOfConstraints>=2){
			//制約を満たしている数を数える
			int countBasis=0;
			int countTarget=0;
			for(int i=0; i<numberOfConstraints; i++){
				if(constraintBasis[i]!=0)	countBasis++;
				if(constraintTarget[i]!=0)	countTarget++;
			}
			
			//targetのほうが満たしていたらbasisは優越していない
			if(countBasis < countTarget ){
				return false;
			//basisのほうが満たしていたらbasisは優越している
			}else if(countBasis > countTarget ){
				return true;
			}
		}

		//制約数0か，制約数1でどちらも満たしているか，制約数2で同数制約を満たしていたら目的関数比較
		return dominated(basis, target, epsilon, alpha);
		
	}
	
	/**
	 * ２つの粒子の適応度の優劣を判定します。<br>
	 *
	 * @param basis 比較したい適応度
	 * @param target 比較される適応度
	 * @return basisがtargetより優越していればtrue / 優越していなければfalse
	 */
	public static boolean dominated(double[] basis, double[] target)
	{
		//1つでもbasisよりtargetの方が小さければ優越していない
		for(int i=0; i<target.length; i++)
		{
			if( target[i]<basis[i] ){
				return false;
			}
		}
		//basisのすべての値がtargetより小さければ優越している
		return true;
	}

	/**
	 * ２つの粒子の適応度の優劣を判定します。<br>
	 * ε-dominationによる優劣判定を行います。<br>
	 *
	 * @param basis 比較対象の適応度
	 * @param target 比較される適応度
	 * @param epsilon ε値
	 * @return basisがtargetより優越していればtrue / 優越していなければfalse
	 */
	private static boolean dominatedEpsilon(double[] basis, double[] target, double epsilon)
	{
		//1つでもtargetをε広げた範囲よりbasisの方が小さければ優越していない
		for(int i=0; i<target.length; i++)
		{
			if( target[i]<(basis[i]*(1-epsilon)) )
			{
				return false;
			}
		}
		//targetをεだけ広げた範囲にbasisが入っていれば優越している
		return true;
	}

	/**
	 * ２つの粒子の適応度の優劣を判定します。<br>
	 * α-dominationによる優劣判定を行います。適応度は正規化してください。<br>
	 *
	 * @param basis 比較対象の適応度
	 * @param target 比較される適応度
	 * @param epsilon α値
	 * @return basisがtargetより優越していればtrue / 優越していなければfalse
	 */
	private static boolean dominatedAlpha(double[] basis, double[] target, double alpha)
	{
		//double[] difference = Vector.minus(basis, target);	//basis-target
		double[] difference = new Vector(basis).minus(new Vector(target)).get();

		//targetが勝ってる目的数を抽出
		int count=0;
		for (int i=0; i<basis.length; i++)
		{
			if(difference[i]>0)	count++;	//差が0より大きい→targetの値が小さいのでtargetが優越
		}

		//全ての目的でtargetが勝っていたらbasisは非優越
		if (count == basis.length)
		{
			return false;
		}
		//全ての目的でtargetが負けていたらbasisが優越
		else if (count == 0)
		{
			return true;
		}
		//その他の場合、targetに対し、勝ってない目的の距離×α < 勝ってる目的の距離 かどうか判定
		//→本来非優越だが、全ての勝ってる目的でこれなら優越。
		else
		{
			//targetが勝ってる目的のインデックスを抽出
			int[] winIndex = new int[count];
			count = 0;
			for (int i=0; i<basis.length; i++)
			{
				if (difference[i]>0)
				{
					winIndex[count] = i;
					count++;
				}
			}

			//勝ってる目的1つずつに対し、他の目的×αで勝てるか確認
			for (int w=0; w<winIndex.length; w++ )
			{
				//負けてる目的のインデックスを抽出
				int[] loseIndex = new int[basis.length - winIndex.length];
				count = 0;
				for (int i=0; i<basis.length; i++)
				{
					if (i!=winIndex[w])
					{
						loseIndex[count] = i;
						count++;
					}
				}

				//basisに対し、勝ってない目的の距離×α < 勝ってる目的の距離 かどうか判定
				double temp = 0;
				for (int n=0; n<loseIndex.length; n++)
				{
					temp += Numeric.square(difference[loseIndex[n]]);
				}
				if ( Math.abs( Math.sqrt(temp)*alpha ) < Math.abs( difference[winIndex[w]]) )
				{
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * 解集合のランク付けを行います。ランク付けにはFlemingの方法を用います。<br>
	 * 優劣判定にε-dominationもしくはα-dominationを使う場合、ε・α値に0以上の値を入力してください。<br>
	 * @param fitness 適応度行列
	 * @param epsilon ε値
	 * @param alpha α値
	 * @return 解のランク配列
	 */
	public static int[] ranking(double[][] fitness, double epsilon, double alpha)
	{
		int[] number = new int[fitness.length];

		//優越されている個数を数える
		for(int i=0; i<fitness.length; i++){
			for(int j=0; j<fitness.length; j++){
				if(i!=j){
					if( dominated(fitness[j], fitness[i], epsilon, alpha) )
					{
						number[i] +=1;
					}
				}
			}
		}

		//個数+1がランク
		for(int i=0; i<number.length; i++)
		{
			number[i] += 1;
		}

		return number;
	}
	/**
	 * 解集合のランク付けを行います。ランク付けにはFlemingの方法を用います。<br>
	 * 優越判定に制約違反量も考慮します。<br>
	 * 優劣判定にε-dominationもしくはα-dominationを使う場合、ε・α値に0以上の値を入力してください。<br>
	 * @param fitness 適応度行列
	 * @param epsilon ε値
	 * @param alpha α値
	 * @return 解のランク配列
	 */
	public static int[] ranking(double[][] fitness, double[][] constraint, double epsilon, double alpha)
	{
		int[] number = new int[fitness.length];

		//優越されている個数を数える
		for(int i=0; i<fitness.length; i++){
			for(int j=0; j<fitness.length; j++){
				if(i!=j){
					if( dominated(fitness[j], fitness[i], constraint[j], constraint[i], epsilon, alpha) )
					{
						number[i] +=1;
					}
				}
			}
		}

		//個数+1がランク
		for(int i=0; i<number.length; i++)
		{
			number[i] += 1;
		}

		return number;
	}

	/**
	 * あるランク値の解のインデックスを返します。<br>
	 * @param rank 解のランク配列
	 * @param number インデックスを探すランク番号
	 * @return ランク値がnumberのランクのインデックス
	 */
	public static int[] rankIndex(int[] rank, int number)
	{
		int count = 0;
		//ランク=numberの数を数える
		for (int i=0; i<rank.length; i++){
			if(rank[i] == number)
			{
				count += 1;
			}
		}

		//ランク=numberのindexを抽出
		int[] index = new int[count];
		count = 0;
		for (int i=0; i<rank.length; i++){
			if(rank[i] == number)
			{
				index[count] = i;
				count += 1;
			}
		}
		return index;
	}

	/**
	 * 境界ランク番号を計算して返します。<br>
	 * @param rank 解のランク配列
	 * @param globalSize グローバルベストの粒子数
	 * @return 境界ランク番号
	 */
	public static int calculateBorderRank(int[] rank, int globalSize)
	{
		//もしランク数よりglobalSizeのほうが大きかったらエラー
		if(rank.length < globalSize )
			Logging.logger.severe("illegal globalSize");

		//int[] count= new int[(int)(Numeric.max(Cast.intToDouble(rank))) +1];
		int[] count = new int[(int)(new Vector(Cast.intToDouble(rank)).max()+1)];

		//各ランクの個数を数える
		for (int i=0; i<rank.length; i++)
		{
			count[rank[i]] += 1;
		}
		//globalSizeをはみ出るラング番号をメモ
		int numOfBorderRank = 0;
		int buffer = 0;
		for (int i=0; i<count.length; i++){
			buffer += count[i];
			if(buffer > globalSize)
			{
				numOfBorderRank = i;
				break;
			}
		}
		return numOfBorderRank;
	}

	/**
	 * 混雑距離を計算します。<br>
	 * 距離計算はマンハッタン距離を使用します。<br>
	 * 混雑距離は、距離の１番目・２番目に近い解の距離の和とします。<br>
	 * @param fitness 適応度行列
	 * @return 各解の混雑距離
	 */
	public static double[] calculateDistance(double[][] fitness){
		double[] distance = new double[fitness.length];
		double[] temp = new double[fitness.length];
		int index=0;

		Matrix fitnessMatrix = new Matrix(fitness);
		double[] maxFitness = fitnessMatrix.max("column").get();
		double[] minFitness = fitnessMatrix.min("column").get();
		//double[] maxFitness = Numeric.max(fitness, "column");
		//double[] minFitness = Numeric.min(fitness, "column");
		int objectiveNumber = fitness[0].length;
		boolean[] finMaxEdge = new boolean[objectiveNumber];
		boolean[] finMinEdge = new boolean[objectiveNumber];
		boolean isEdge;

		//マンハッタン距離で最も近い解までの距離を返す。
		for(int i=0; i<fitness.length; i++){
			isEdge = false;
			//端の解かどうか判定
			for( int o=0; o<objectiveNumber; o++)
			{
				if( fitness[i][o] == maxFitness[o] && finMaxEdge[o] == false )
				{
					isEdge = true;
					finMaxEdge[o] = true;
				}
				else if( fitness[i][o] == minFitness[o] && finMinEdge[o] == false )
				{
					isEdge = true;
					finMinEdge[o] = true;
				}
			}

			//端の解なら距離は最大
			if(isEdge){
				distance[i] = Double.MAX_VALUE;
			}else{
				for(int j=0; j<fitness.length; j++)
				{
					if(i!=j)
					{
						//temp[j] = Numeric.sum( Numeric.abs( Vector.minus(fitness[i], fitness[j]) ) );
						temp[j] = new Vector(fitness[i]).minus(new Vector(fitness[j])).abs().sum();

					}
					else
					{
						temp[j] = Double.MAX_VALUE;
					}
				}
				index = minIndex(temp);		//最小値のインデックスを格納
				distance[i] = temp[index];	//最小値を格納
				temp[index] = Double.MAX_VALUE;		//最小値を無限にする。
				//distance[i] += Numeric.min(temp);	//2番目の最小値を加算
				distance[i] += new Vector(temp).min();
			}
		}
		return distance;
	}

	/**
	 * 最小値のインデックスを計算します。<br>
	 * @param value 最小値を計算したい配列
	 * @return 最小値のインデックス
	 */
	private static int minIndex(double[] value)
	{
		double min = value[0];
		int index=0;
		for( int i=0; i<value.length; i++ )
		{
			if( min > value[i] )
			{
				min = value[i];
				index = i;
			}
		}
		return index;
	}
}
