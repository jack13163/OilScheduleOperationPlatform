package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.plot.PlotObjectives;
import opt.easyjmetal.util.comparators.one.DominanceComparator;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.ArrayList;
import java.util.List;

public class IBEA extends Algorithm {
    public static final int TOURNAMENTS_ROUNDS = 1;
    private List<List<Double>> indicatorValues_;
    private double maxIndicatorValue_;
    private int populationSize_;
    private int archiveSize_;
    private SolutionSet population_;
    private String dataDirectory_;
    // 当前迭代次数
    private int evaluations_;
    // 最大迭代次数
    private int maxEvaluations_;

    protected SolutionSet archive_;

    public IBEA(Problem problem) {
        super(problem);
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        // 参数初始化
        evaluations_ = 0;
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        populationSize_ = (Integer) getInputParameter("populationSize");
        archiveSize_ = (Integer) getInputParameter("externalArchiveSize");
        int runningTime = (Integer) getInputParameter("runningTime");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        population_ = new SolutionSet(populationSize_);
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");

        // 交叉选择算子
        Operator mutationOperator_ = (Operator) getInputParameter("mutation");
        Operator crossoverOperator_ = (Operator) getInputParameter("crossover");
        Operator selectionOperator_ = (Operator) getInputParameter("selection");

        // 创建数据表，方便后面保存结果
        String dbName = dataDirectory_ + problem_.getName();
        String tableName = "IBEA_" + runningTime;
        SqlUtils.createTable(tableName, dbName);

        // 初始化种群和储备集
        population_ = new SolutionSet(populationSize_);
        archive_ = new SolutionSet(archiveSize_);
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }

        // 迭代
        while (evaluations_ < maxEvaluations_) {
            // 合并当前种群和储备集
            SolutionSet union = new SolutionSet(population_.size() + archive_.size());
            union = union.union(population_);
            union = union.union(archive_);

            // 计算适应值
            calculateFitness(union);

            // 计算新的储备集
            archive_ = union;
            while (archive_.size() > populationSize_) {
                removeWorst(archive_);
            }

            // 创建子代种群
            SolutionSet offSpringSolutionSet = new SolutionSet(populationSize_);
            while (offSpringSolutionSet.size() < populationSize_) {
                // 通过轮盘赌算法从父代种群中选择两个个体
                Solution[] parents = new Solution[2];
                int j = 0;
                do {
                    j++;
                    parents[0] = (Solution) selectionOperator_.execute(archive_);
                } while (j < IBEA.TOURNAMENTS_ROUNDS);
                int k = 0;
                do {
                    k++;
                    parents[1] = (Solution) selectionOperator_.execute(archive_);
                } while (k < IBEA.TOURNAMENTS_ROUNDS);

                // 执行交叉变异
                Solution[] offspring = (Solution[]) crossoverOperator_.execute(parents);
                mutationOperator_.execute(offspring[0]);

                // 评估适应度值
                problem_.evaluate(offspring[0]);
                offSpringSolutionSet.add(offspring[0]);
                evaluations_++;
            }
            population_ = offSpringSolutionSet;

            // 显示当前储备集中的解
            if (isDisplay_) {
                PlotObjectives.plotSolutions("IBEA", archive_);
            }
        }

        SqlUtils.insertSolutionSet(dbName, tableName, archive_);

        return population_;
    }

    /**
     * Calculates the hypervolume of that portion of the objective space that is
     * dominated by individual a but not by individual b
     */
    double calculateHypervolumeIndicator(Solution solutionA, Solution solutionB, int d, double maximumValues[],
                                         double minimumValues[]) {
        double a, b, r, max;
        double volume;
        double rho = 2.0;

        r = rho * (maximumValues[d - 1] - minimumValues[d - 1]);
        max = minimumValues[d - 1] + r;

        a = solutionA.getObjective(d - 1);
        if (solutionB == null) {
            b = max;
        } else {
            b = solutionB.getObjective(d - 1);
        }

        if (d == 1) {
            if (a < b) {
                volume = (b - a) / r;
            } else {
                volume = 0;
            }
        } else {
            if (a < b) {
                volume = calculateHypervolumeIndicator(solutionA, null, d - 1, maximumValues, minimumValues) * (b - a)
                        / r;
                volume += calculateHypervolumeIndicator(solutionA, solutionB, d - 1, maximumValues, minimumValues)
                        * (max - b) / r;
            } else {
                volume = calculateHypervolumeIndicator(solutionA, solutionB, d - 1, maximumValues, minimumValues)
                        * (max - a) / r;
            }
        }

        return (volume);
    }

    /**
     * This structure stores the indicator values of each pair of elements
     */
    public void computeIndicatorValuesHD(SolutionSet solutionSet, double[] maximumValues, double[] minimumValues) {
        List<Solution> A, B;
        // Initialize the structures
        indicatorValues_ = new ArrayList<List<Double>>();
        maxIndicatorValue_ = -Double.MAX_VALUE;

        for (int j = 0; j < solutionSet.size(); j++) {
            A = new ArrayList<>(1);
            A.add(solutionSet.get(j));

            List<Double> aux = new ArrayList<Double>();
            for (int i = 0; i < solutionSet.size(); i++) {
                Solution solution = solutionSet.get(i);
                B = new ArrayList<>(1);
                B.add(solution);

                int flag = (new DominanceComparator()).compare(A.get(0), B.get(0));

                double value;
                if (flag == -1) {
                    value = -calculateHypervolumeIndicator(A.get(0), B.get(0), problem_.getNumberOfObjectives(),
                            maximumValues, minimumValues);
                } else {
                    value = calculateHypervolumeIndicator(B.get(0), A.get(0), problem_.getNumberOfObjectives(),
                            maximumValues, minimumValues);
                }

                // Update the max value of the indicator
                if (Math.abs(value) > maxIndicatorValue_) {
                    maxIndicatorValue_ = Math.abs(value);
                }
                aux.add(value);
            }
            indicatorValues_.add(aux);
        }
    }

    /**
     * Calculate the fitness for the individual at position pos
     */
    public void fitness(SolutionSet solutionSet, int pos) {
        double fitness = 0.0;
        double kappa = 0.05;

        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != pos) {
                fitness += Math.exp((-1 * indicatorValues_.get(i).get(pos) / maxIndicatorValue_) / kappa);
            }
        }
        solutionSet.get(pos).setFitness(fitness);
    }

    /**
     * Calculate the fitness for the entire population.
     */
    public void calculateFitness(SolutionSet solutionSet) {
        // Obtains the lower and upper bounds of the population
        double[] maximumValues = new double[problem_.getNumberOfObjectives()];
        double[] minimumValues = new double[problem_.getNumberOfObjectives()];

        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            maximumValues[i] = -Double.MAX_VALUE;
            minimumValues[i] = Double.MAX_VALUE;
        }

        for (int i = 0; i < solutionSet.size(); i++) {
            opt.easyjmetal.core.Solution solution = solutionSet.get(i);
            for (int obj = 0; obj < problem_.getNumberOfObjectives(); obj++) {
                double value = solution.getObjective(obj);
                if (value > maximumValues[obj]) {
                    maximumValues[obj] = value;
                }
                if (value < minimumValues[obj]) {
                    minimumValues[obj] = value;
                }
            }
        }

        computeIndicatorValuesHD(solutionSet, maximumValues, minimumValues);
        for (int pos = 0; pos < solutionSet.size(); pos++) {
            fitness(solutionSet, pos);
        }
    }

    /**
     * Update the fitness before removing an individual
     */
    public void removeWorst(SolutionSet solutionSet) {

        // Find the worst;
        double worst = solutionSet.get(0).getFitness();
        int worstIndex = 0;
        double kappa = 0.05;

        for (int i = 1; i < solutionSet.size(); i++) {
            if (solutionSet.get(i).getFitness() > worst) {
                worst = solutionSet.get(i).getFitness();
                worstIndex = i;
            }
        }

        // Update the population
        for (int i = 0; i < solutionSet.size(); i++) {
            if (i != worstIndex) {
                double fitness = solutionSet.get(i).getFitness();
                fitness -= Math.exp((-indicatorValues_.get(worstIndex).get(i) / maxIndicatorValue_) / kappa);
                solutionSet.get(i).setFitness(fitness);
            }
        }

        // remove worst from the indicatorValues list
        indicatorValues_.remove(worstIndex);
        for (List<Double> anIndicatorValues_ : indicatorValues_) {
            anIndicatorValues_.remove(worstIndex);
        }

        solutionSet.remove(worstIndex);
    }
}
