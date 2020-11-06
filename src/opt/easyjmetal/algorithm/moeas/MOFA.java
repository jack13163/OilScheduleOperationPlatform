package opt.easyjmetal.algorithm.moeas;

import opt.easyjmetal.algorithm.cmoeas.util.Utils;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.comparators.IConstraintViolationComparator;
import opt.easyjmetal.util.comparators.ViolationThresholdComparator;
import opt.easyjmetal.util.jmathplot.ScatterPlot;
import opt.easyjmetal.util.sqlite.SqlUtils;

/**
 * 多策略协同多目标萤火虫算法
 * Programmed by Kevin Kong
 */
public class MOFA extends Algorithm {

    // 种群规模
    private int populationSize_;

    // 种群
    private SolutionSet population_;

    // 当前迭代次数
    private int evaluations_;

    // 最大迭代次数
    private int maxEvaluations_;

    // 保存路径
    private String dataDirectory_;

    private ScatterPlot plot_;

    // 外部储备集
    private int externalArchiveSize;
    private SolutionSet external_archive_;

    // 光吸收系数
    private double gamma;
    // 最大吸引力
    private double beta0;

    private IConstraintViolationComparator comparator = new ViolationThresholdComparator();

    /**
     * 初始化
     * @param problem 问题
     */
    public MOFA(Problem problem) {
        super(problem);
        this.populationSize_ = 100;         // 种群大小
        this.maxEvaluations_ = 500;         // 最大迭代次数
        this.externalArchiveSize = 100;     // 外部档案规模
        this.gamma = 1;                     // 光吸收系数
        this.beta0 = 1;                     // 最大吸引力
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        // 参数初始化
        evaluations_ = 0;
        int maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        populationSize_ = (Integer) getInputParameter("populationSize");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        String dbName = getInputParameter("DBName").toString();
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");
        int plotFlag_ = (Integer) getInputParameter("plotFlag");

        int runningTime = (Integer) getInputParameter("runningTime") + 1; // start from 1
        population_ = new SolutionSet(populationSize_);
        gamma = (Integer) getInputParameter("gamma");
        beta0 = (Integer) getInputParameter("beta0");

        String paratoFilePath_ = getInputParameter("paretoPath").toString();
        Operator crossover_ = operators_.get("crossover"); // default: DE crossover
        Operator mutation_ = operators_.get("mutation");  // default: polynomial mutation

        // 创建数据表，方便后面保存结果
        String problemName = problem_.getName() + "_" + runningTime;
        SqlUtils.CreateTable(problemName, dbName);


        // STEP 1.2. Initialize population
        initPopulation();

        // Initialize the external archive
        external_archive_ = new SolutionSet(populationSize_);
        Utils.initializeExternalArchive(population_,populationSize_,external_archive_);

        //display constraint info
        if (isDisplay_ && paratoFilePath_ != null) {
            if (plotFlag_ == 0) {
                plot_ = new ScatterPlot(this.getClass().getName(), problem_.getName(), population_);
            }
            if (plotFlag_ == 1) {
                plot_ = new ScatterPlot(this.getClass().getName(), problem_.getName(), external_archive_);
            }
            plot_.displayPf(paratoFilePath_);
        }

        // STEP 2. Update
        int gen = 0;
//        do {
//            int[] permutation = new int[populationSize_];
//            Utils.randomPermutation(permutation, populationSize_);
//
//            for (int i = 0; i < populationSize_; i++) {
//                int n = permutation[i]; // or int n = i;
//                //int n = i ; // or int n = i;
//                int type;
//                double rnd = PseudoRandom.randDouble();
//
//                // STEP 2.1. Mating selection based on probability
//                if (rnd < delta_){ // if (rnd < realb)
//                    type = 1;   // neighborhood
//                } else {
//                    type = 2;   // whole population
//                }
//                Vector<Integer> p = new Vector<Integer>();
//                matingSelection(p, n,2, type);
//
//                // STEP 2.2. Reproduction
//                Solution child = null;
//                // Apply Crossover for Real codification
//                if (crossover_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
//                    Solution[] parents = new Solution[2];
//                    parents[0] = population_.get(p.get(0));
//                    parents[1] = population_.get(n);
//                    child = ((Solution[]) crossover_.execute(parents))[0];
//                }
//                // Apply DE crossover
//                else if (crossover_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
//                    Solution[] parents = new Solution[3];
//                    parents[0] = population_.get(p.get(0));
//                    parents[1] = population_.get(p.get(1));
//                    parents[2] = population_.get(n);
//                    child = (Solution) crossover_.execute(new Object[]{population_.get(n), parents});
//                } else {
//                    System.out.println("unknown crossover");
//                }
//
//                // Apply mutation
//                mutation_.execute(child);
//
//                // Evaluation
//                problem_.evaluate(child);
//                problem_.evaluateConstraints(child);
//
//                evaluations_++;
//
//                // STEP 2.3. Repair. Not necessary
//
//                // STEP 2.4. Update z_
//                updateReference(child);
//
//                // STEP 2.5. Update of solutions
//                updateProblem(child, n, type);
//            } // for
//
//            ((ViolationThresholdComparator) this.comparator).updateThreshold(this.population_);
//
//            gen += 1;
//
//            //Update the external archive
//            Utils.updateExternalArchive(population_,populationSize_,external_archive_);
//
//            if (isDisplay_) {
//                plotPopulation(plotFlag_);
//            }
//
//            allPop = allPop.union(population_);
//
//        } while (evaluations_ < maxEvaluations_);

        SqlUtils.InsertSolutionSet(dbName, problemName, external_archive_);
        return external_archive_;
    }

    private void initPopulation() throws JMException, ClassNotFoundException {
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);

            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }
    }
}
