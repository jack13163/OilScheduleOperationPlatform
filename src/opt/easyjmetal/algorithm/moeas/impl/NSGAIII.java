// An Evolutionary Many-Objective Optimization Algorithm Using Reference-point
// Based Non-dominated Sorting Approach,
// Part I: Solving Problems with Box Constraints.
package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.algorithm.cmoeas.impl.nsgaiii_cdp.EnvironmentalSelection;
import opt.easyjmetal.algorithm.common.ReferencePoint;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.*;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.plot.PlotObjectives;
import opt.easyjmetal.util.ranking.impl.RankingByCDP;
import opt.easyjmetal.util.solution.MoeadUtils;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class NSGAIII extends Algorithm {

    public NSGAIII(Problem problem) {
        super(problem);
    }

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;
    private String weightDirectory_;

    Distance distance;

    /**
     * 参考点
     */
    //Vector<Vector<Double>> lambda_ ;
    private double[][] lambda_;
    protected List<ReferencePoint> referencePoints = new Vector<>();

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        distance = new Distance();
        int runningTime = (Integer) getInputParameter("runningTime");

        //Read the parameters
        populationSize_ = (Integer) getInputParameter("populationSize");
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        weightDirectory_ = getInputParameter("weightDirectory").toString();
        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");

        // 交叉选择算子
        Operator mutationOperator_ = (Operator) getInputParameter("mutation");
        Operator crossoverOperator_ = (Operator) getInputParameter("crossover");
        Operator selectionOperator_ = (Operator) getInputParameter("selection");

        // STEP 1. Initialization
        // STEP 1.1. Compute euclidean distances between weight vectors and find T
        initUniformWeight();

        // Create the initial solutionSet
        population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }

        // Initialize the external archive
        external_archive_ = new SolutionSet(populationSize_);
        MoeadUtils.initializeExternalArchive(population_, populationSize_, external_archive_);

        // 创建数据库记录数据
        String dbName = dataDirectory_ + problem_.getName();
        String tableName = "NSGAIII_" + runningTime;
        SqlUtils.createTable(tableName, dbName);

        while (evaluations_ < maxEvaluations_) {
            // Create the offSpring solutionSet
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                Solution[] offSpring = new Solution[2];
                // Apply Crossover for Real codification
                if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    Solution[] parents = new Solution[2];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    offSpring = ((Solution[]) crossoverOperator_.execute(parents));
                }
                // Apply DE crossover
                else if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
                    Solution[] parents = new Solution[3];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    parents[2] = parents[0];
                    offSpring[0] = (Solution) crossoverOperator_.execute(new Object[]{parents[0], parents});
                    offSpring[1] = (Solution) crossoverOperator_.execute(new Object[]{parents[1], parents});
                } else {
                    System.out.println("unknown crossover");

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
            population_ = replacement(population_, offspringPopulation_);

            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            // 显示当前储备集中的解
            if (isDisplay_) {
                PlotObjectives.plotSolutions("NSGAIII", external_archive_);
            }
        }

        SqlUtils.insertSolutionSet(dbName, tableName, external_archive_);

        return external_archive_;
    } // execute


    // Generate the reference points and random population
    private void initUniformWeight() {
        if ((problem_.getNumberOfObjectives() == 2) && (populationSize_ <= 300)) {
            for (int n = 0; n < populationSize_; n++) {
                double a = 1.0 * n / (populationSize_ - 1);
                lambda_[n][0] = a;
                lambda_[n][1] = 1 - a;
            } // for
        } // if
        else {
            String dataFileName;
            dataFileName = "W" + problem_.getNumberOfObjectives() + "D_" + populationSize_ + ".dat";

            try {
                // Open the file
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
                        //System.out.println("lambda["+i+","+j+"] = " + value) ;
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
    } // initUniformWeight

    // 环境选择
    protected SolutionSet replacement(SolutionSet population, SolutionSet offspringPopulation) throws JMException {

        // Create the solutionSet union of solutionSet and offSpring
        SolutionSet jointPopulation = population_.union(offspringPopulation);

        // RankingByCDP the union
        RankingByCDP ranking = new RankingByCDP(jointPopulation);

        // List<Solution> pop = crowdingDistanceSelection(ranking);
        SolutionSet pop = new SolutionSet(populationSize_);
        List<SolutionSet> fronts = new ArrayList<>();
        int rankingIndex = 0;
        int candidateSolutions = 0;
        while (candidateSolutions < populationSize_) {

            SolutionSet solutions = ranking.getSubfront(rankingIndex);
            fronts.add(solutions);

            candidateSolutions += ranking.getSubfront(rankingIndex).size();
            if ((pop.size() + ranking.getSubfront(rankingIndex).size()) <= populationSize_) {

                for (int i = 0; i < solutions.size(); i++) {
                    pop.add(solutions.get(i));
                }
            }
            rankingIndex++;
        }

        // 执行选择操作
        EnvironmentalSelection selection =
                new EnvironmentalSelection(fronts, populationSize_, getReferencePointsCopy(), problem_.getNumberOfObjectives());
        pop = selection.execute(pop);

        return pop;
    }

    private List<ReferencePoint> getReferencePointsCopy() {
        List<ReferencePoint> copy = new ArrayList<>();
        for (int i = 0; i < lambda_.length; i++) {
            copy.add(new ReferencePoint(lambda_[i]));
        }
        return copy;
    }
}
