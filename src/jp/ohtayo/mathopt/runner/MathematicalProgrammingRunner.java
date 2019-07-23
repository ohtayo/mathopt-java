package jp.ohtayo.mathopt.runner;

import jp.ohtayo.commons.io.Csv;
import jp.ohtayo.mathopt.algorithm.DownhillSimplex;
import jp.ohtayo.mathopt.algorithm.JGGUNDX;
import jp.ohtayo.mathopt.algorithm.OPSO;
import jp.ohtayo.mathopt.algorithm.PSO;
import jp.ohtayo.mathopt.algorithm.QuasiNewton;
import jp.ohtayo.mathopt.algorithm.RCGA;
import jp.ohtayo.mathopt.algorithm.SimpleGeneticAlgorithm;
import jp.ohtayo.mathopt.algorithm.SimulatedAnnealing;
import jp.ohtayo.commons.math.Matrix;

/**
 * 多数の数理最適化手法，メタヒューリスティクスの比較するプログラム例です．
 *
 * @author ohtayo<ohta.yoshihiro@outlook.jp>
 */
public class MathematicalProgrammingRunner {
	public static void main(String[] args){
		String[] objective = new String[4];
		objective[0] = "jp.ohtayo.mathopt.function.Schwefel";
		objective[1] = "jp.ohtayo.mathopt.function.Griewank";
		objective[2] = "jp.ohtayo.mathopt.function.Rosenbrock";
		objective[3] = "jp.ohtayo.mathopt.function.Rastrigin";
		int length = 4;
		int repeat = 1000;
		int population = 400;
		QuasiNewton newton;
		PSO pso;
		OPSO opso;
		double fitness;
	
		double alpha = 1, beta=0.5, ganma=2;
		long start, end;
		Matrix vector = new Matrix(11,9);
		
		for(int o=0; o<objective.length; o++){
			
			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				fitness = DownhillSimplex.main(population, length, repeat, objective[o], alpha, beta, ganma);
				vector.set(i,0, fitness);
			}
			end = System.currentTimeMillis();
			vector.set(10, 0, end-start);
			
			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				newton = new QuasiNewton(length, 10000, 1e-9, 1.0, objective[o] );
				fitness = newton.solve();
				vector.set(i,1, fitness);
			}
			end = System.currentTimeMillis();
			vector.set(10, 2, end-start);
			
			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				fitness = SimulatedAnnealing.main(length, 1000, objective[o], 1000, 100, 0.001, 0.98);
				vector.set(i,3, fitness);
			}
			end = System.currentTimeMillis();
			vector.set(10, 3, end-start);
			
			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				vector.set(i,4, SimpleGeneticAlgorithm.main(10, length, population , repeat, objective[o] ,1, 0.01, 1 ));
			}
			end = System.currentTimeMillis();
			vector.set(10, 4, end-start);
			
			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				vector.set(i,5, RCGA.main(length, population, repeat, objective[o],1, 0.01, 1 ));
			}
			end = System.currentTimeMillis();
			vector.set(10, 5, end-start);
			
			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				vector.set(i,6, JGGUNDX.main(length, repeat, objective[o], 0.01));
			}
			end = System.currentTimeMillis();
			vector.set(10, 6, end-start);

			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				pso = new PSO();
				vector.set(i,7, pso.main(length, population, repeat, objective[o], 0.3, 1.75, 1.75));
			}
			end = System.currentTimeMillis();
			vector.set(10, 7, end-start);
	
			start = System.currentTimeMillis();
			for(int i=0; i<10; i++){
				opso = new OPSO();
				vector.set(i,8, opso.main(length, population, repeat, objective[o]));
			}
			end = System.currentTimeMillis();
			vector.set(10, 8, end-start);
	
			Csv.write("test"+objective[o]+".csv", vector.get(),"");
			System.out.println(vector.toString());
		}
	}
}
