# mathopt-java
The mathopt-java provides classes for developing mathematical programming and metaheuristics projects in java languages.

## How to use

### Getting Started (run PSO(Particle Swarm Optimization) algorithm)
1. Build project.
2. Create .jar file (e.g. "mathopt-java.jar").
3. Modify config file("pso.config.xml").
4. Execute command "java -jar mathopt-java.jar".
5. Get output files from the "result" directory.

### Objective functions
This project includes following objective functions in jp.ohtayo.mathopt.function.
* Griewank function
* Rastrigin function
* Rosenbrock function
* Schwefel function
* ZDT functions(2, 3, 4)
* DTLZ functions(2, 3)

To add new objective function, create class and functions as follows:

    public class NameOfObjectiveFunction {
        private static double[] maxValue = {1.0};
        private static double[] minValue = {0.0};
        public static double[] getMaxValue() { return maxValue; }
        public static double[] getMinValue() { return minValue; }
        public static double[] execute(double[] variable){
            double[] fitness;
            /* Calculate fitness */
            return fitness;
        }
    }

### Algorithm implementation
For evaluation of objective function, use ObjectiveFunction class and the name of objective function as String.

    String nameOfObjectiveFunction = "jp.ohtayo.mathopt.function.Rastrigin";
    (double[])ObjectiveFunction.execute(variable, nameOfObjectiveFunction);

## Requirement
This project needs following library.
* [ohtayo-commons-java](https://github.com/ohtayo/commons-java)

## Licence
The mathopt-java is open-sourced software licensed under the [MIT license](https://github.com/ohtayo/mathopt-java/blob/master/LICENSE).
