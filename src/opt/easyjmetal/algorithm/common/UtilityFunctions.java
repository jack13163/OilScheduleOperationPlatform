package opt.easyjmetal.algorithm.common;

import opt.easyjmetal.core.Operator;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.JMException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class UtilityFunctions {


    /**
     * 生成下一代种群
     * @param solutionSet1       种群1
     * @param solutionSet2       种群2
     * @param mutationOperator_  变异算子
     * @param crossoverOperator_ 交叉算子
     * @param selectionOperator_ 选择算子
     * @return
     * @throws JMException
     */
    public static Solution[] generateOffsprings(SolutionSet solutionSet1,
                                                SolutionSet solutionSet2,
                                                Operator mutationOperator_,
                                                Operator crossoverOperator_,
                                                Operator selectionOperator_) throws JMException {
        Solution[] offSpring = new Solution[2];
        // Apply Crossover for Real codification
        if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
            Solution[] parents = new Solution[2];
            parents[0] = (Solution) selectionOperator_.execute(solutionSet1);
            parents[1] = (Solution) selectionOperator_.execute(solutionSet2);
            offSpring = ((Solution[]) crossoverOperator_.execute(parents));
        }
        // Apply DE crossover
        else if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
            Solution[] parents = new Solution[3];
            parents[0] = (Solution) selectionOperator_.execute(solutionSet1);
            parents[1] = (Solution) selectionOperator_.execute(solutionSet2);
            parents[2] = parents[0];
            offSpring[0] = (Solution) crossoverOperator_.execute(new Object[]{parents[0], parents});
            offSpring[1] = (Solution) crossoverOperator_.execute(new Object[]{parents[1], parents});
        } else {
            System.out.println("unknown crossover");
        }
        mutationOperator_.execute(offSpring[0]);
        mutationOperator_.execute(offSpring[1]);
        return offSpring;
    }

    /**
     * 读取指定路径下的权重文件，生成参考点
     * @param weightDirectory_  权重文件的路径
     * @param lambda_
     */
    public static void initUniformWeight(String weightDirectory_, double[][] lambda_) {
        int populationSize_ = lambda_.length;
        int numberOfObjectives = lambda_[0].length;
        if ((numberOfObjectives == 2) && (populationSize_ <= 300)) {
            for (int n = 0; n < populationSize_; n++) {
                double a = 1.0 * n / (populationSize_ - 1);
                lambda_[n][0] = a;
                lambda_[n][1] = 1 - a;
            }
        } else {
            String dataFileName;
            dataFileName = "W" + numberOfObjectives + "D_" + populationSize_ + ".dat";

            try {
                // 读取权重文件
                String filepath = weightDirectory_ + dataFileName;
                FileInputStream fis = new FileInputStream(filepath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                int i = 0;
                int j;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    while (st.hasMoreTokens()) {
                        double value = new Double(st.nextToken());
                        lambda_[i][j] = value;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
                br.close();
            } catch (Exception e) {
                System.out.println("initUniformWeight: failed when reading for file: " + weightDirectory_ + dataFileName);
                e.printStackTrace();
            }
        }
    }
}
