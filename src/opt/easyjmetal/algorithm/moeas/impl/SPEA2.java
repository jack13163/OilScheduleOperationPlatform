// SPEA2: Improving the Strength Pareto Evolutionary Algorithm For Multiobjective Optimization.
package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.algorithm.common.StrengthRawFitness;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.PlotObjectives;
import opt.easyjmetal.util.comparators.FitnessComparator;
import opt.easyjmetal.util.sqlite.SqlUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SPEA2 extends Algorithm {

    public SPEA2(Problem problem) {
        super(problem);
    }

    private SolutionSet external_archive_;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;

    Distance distance;

    private SolutionSet archive;// 档案集
    private StrengthRawFitness strenghtRawFitness = new StrengthRawFitness();
    private int k_;

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        distance = new Distance();// 计算距离

        // 读取参数
        int runningTime = (Integer) getInputParameter("runningTime");
        populationSize_ = (Integer) getInputParameter("populationSize");
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        String dbName = getInputParameter("DBName").toString();
        dataDirectory_ = getInputParameter("weightsDirectory").toString();
        k_ = (Integer) getInputParameter("k");
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");

        // 交叉选择算子
        Operator mutationOperator_ = (Operator) getInputParameter("mutation");
        Operator crossoverOperator_ = (Operator) getInputParameter("crossover");
        Operator selectionOperator_ = (Operator) getInputParameter("selection");

        // 创建初始种群
        SolutionSet population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }

        // 初始化外部储备集
        external_archive_ = new SolutionSet(populationSize_);
        MoeadUtils.initializeExternalArchive(population_, populationSize_, external_archive_);

        // 创建数据表记录数据
        String tableName = "SPEA2_" + runningTime;
        SqlUtils.CreateTable(tableName, dbName);

        while (evaluations_ < maxEvaluations_) {

            // 创建子代种群
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                Solution[] offSpring = new Solution[2];
                if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    // 模拟二进制交叉算子
                    Solution[] parents = new Solution[2];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    offSpring = ((Solution[]) crossoverOperator_.execute(parents));
                } else {
                    // 差分交叉算子
                    Solution[] parents = new Solution[3];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    parents[2] = parents[0];
                    offSpring[0] = (Solution) crossoverOperator_.execute(new Object[]{parents[0], parents});
                    offSpring[1] = (Solution) crossoverOperator_.execute(new Object[]{parents[1], parents});
                }
                mutationOperator_.execute(offSpring[0]);
                mutationOperator_.execute(offSpring[1]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[1]);
                offspringPopulation_.add(offSpring[0]);
                offspringPopulation_.add(offSpring[1]);
                evaluations_ += 2;
            }

            // 环境选择
            SolutionSet jointPopulation = population_.union(offspringPopulation_);
            population_ = select(jointPopulation, populationSize_, 1);

            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            // 显示当前储备集中的解
            if (isDisplay_) {
                PlotObjectives.plotSolutions("SPEA2", external_archive_);
            }
        }

        SqlUtils.InsertSolutionSet(dbName, tableName, external_archive_);

        return external_archive_;
    }

    /**
     * 环境选择
     * @param source2
     * @param solutionsToSelect
     * @param kValue      推荐1
     * @return
     */
    public SolutionSet select(SolutionSet source2, int solutionsToSelect, int kValue) {
        // 计算适应度值
        StrengthRawFitness strengthRawFitness = new StrengthRawFitness(kValue);
        strengthRawFitness.computeDensityEstimator(source2);

        int size;
        List<Solution> source = new ArrayList<>(source2.size());
        for (int i = 0; i < source2.size(); i++) {
            source.add(source2.get(i));
        }
        if (source2.size() < solutionsToSelect) {
            size = source.size();
        } else {
            size = solutionsToSelect;
        }

        // 先选出非支配解，即fitness小于1的解
        SolutionSet aux = new SolutionSet(source.size());
        int i = 0;
        while (i < source.size()) {
            double fitness = source.get(i).getFitness();
            if (fitness < 1.0) {
                aux.add(source.get(i));
                source.remove(i);
            } else {
                i++;
            }
        }

        // 再按照适应度值排序选择剩余个体
        if (aux.size() < size) {
            FitnessComparator comparator = new FitnessComparator();
            Collections.sort(source, comparator);
            int remain = size - aux.size();
            for (i = 0; i < remain; i++) {
                aux.add(source.get(i));
            }
            return aux;
        } else if (aux.size() == size) {
            return aux;
        }

        // 若非支配解个数很多，则进行规模为2的锦标赛算法
        Distance _distance = new Distance();
        List<List<Pair<Integer, Double>>> distanceList = new ArrayList<>();
        for (int pos = 0; pos < aux.size(); pos++) {
            List<Pair<Integer, Double>> distanceNodeList = new ArrayList<>();
            for (int ref = 0; ref < aux.size(); ref++) {
                if (pos != ref) {
                    distanceNodeList.add(Pair.of(ref,
                            _distance.distanceBetweenObjectives(aux.get(pos), aux.get(ref))));
                }
            }
            distanceList.add(distanceNodeList);
        }

        for (int q = 0; q < distanceList.size(); q++) {
            Collections.sort(distanceList.get(q), (pair1, pair2) -> {
                if (pair1.getRight() < pair2.getRight()) {
                    return -1;
                } else if (pair1.getRight() > pair2.getRight()) {
                    return 1;
                } else {
                    return 0;
                }
            });
        }

        while (aux.size() > size) {
            double minDistance = Double.MAX_VALUE;
            int toRemove = 0;
            i = 0;
            Iterator<List<Pair<Integer, Double>>> iterator = distanceList.iterator();
            while (iterator.hasNext()) {
                List<Pair<Integer, Double>> dn = iterator.next();
                if (dn.get(0).getRight() < minDistance) {
                    toRemove = i;
                    minDistance = dn.get(0).getRight();
                    //i y toRemove have the same distance to the first solution
                } else if (dn.get(0).getRight().equals(minDistance)) {
                    int k = 0;
                    while ((dn.get(k).getRight().equals(
                            distanceList.get(toRemove).get(k).getRight())) &&
                            k < (distanceList.get(i).size() - 1)) {
                        k++;
                    }

                    if (dn.get(k).getRight() <
                            distanceList.get(toRemove).get(k).getRight()) {
                        toRemove = i;
                    }
                }
                i++;
            }

            int tmp = aux.get(toRemove).getLocation();
            aux.remove(toRemove);
            distanceList.remove(toRemove);

            Iterator<List<Pair<Integer, Double>>> externIterator = distanceList.iterator();
            while (externIterator.hasNext()) {
                Iterator<Pair<Integer, Double>> interIterator = externIterator.next().iterator();
                while (interIterator.hasNext()) {
                    if (interIterator.next().getLeft() == tmp) {
                        interIterator.remove();
                        continue;
                    }
                }
            }
        }
        return aux;
    }
}
