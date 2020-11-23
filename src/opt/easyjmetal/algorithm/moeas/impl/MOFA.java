package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.algorithm.util.Utils;
import opt.easyjmetal.algorithm.util.PlotObjectives;
import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.problem.sj.CloneUtil;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 多策略协同多目标萤火虫算法
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

    // 外部储备集
    private int externalArchiveSize;
    private SolutionSet external_archive_;

    // 光吸收系数
    private double gamma;
    // 最大吸引力
    private double beta0;

    /**
     * 初始化
     *
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
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        populationSize_ = (Integer) getInputParameter("populationSize");
        externalArchiveSize = (Integer) getInputParameter("externalArchiveSize");
        String dbName = getInputParameter("DBName").toString();
        int runningTime = (Integer) getInputParameter("runningTime");
        population_ = new SolutionSet(populationSize_);
        gamma = Double.parseDouble(getInputParameter("gamma").toString());
        beta0 = Double.parseDouble(getInputParameter("beta0").toString());
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");

        // 创建数据表，方便后面保存结果
        String tableName = "MOFA_" + runningTime;
        SqlUtils.CreateTable(tableName, dbName);

        // 初始化种群
        initPopulation();

        // 初始化外部储备集
        external_archive_ = Utils.initializeExternalArchive(population_, populationSize_, new SolutionSet(externalArchiveSize));

        // 迭代更新
        do {
            int[] permutation = new int[populationSize_];
            Utils.randomPermutation(permutation, populationSize_);

            for (int i = 0; i < populationSize_; i++) {
                for (int j = 0; j < populationSize_; j++) {
                    int domination = get_domination(population_.get(i), population_.get(i));
                    if (domination != -1) {
                        // i和j之间存在支配关系，从储备集中随机选取一个个体作为g
                        int eSize = external_archive_.size();
                        int ind = new Random().nextInt(eSize);
                        Solution g = external_archive_.get(ind);
                        if (domination == 0) {
                            // i支配j
                            population_.replace(j, firefly_move(population_.get(i), population_.get(j),beta0, gamma, true, g).get(0));
                        } else {
                            // j支配i
                            population_.replace(i, firefly_move(population_.get(j), population_.get(i), beta0, gamma, true, g).get(0));
                        }
                    } else {
                        // i和j之间存在支配关系，从储备集中随机选取一个个体作为g
                        int eSize = external_archive_.size();
                        int ind = new Random().nextInt(eSize);
                        Solution g = external_archive_.get(ind);
                        List<Solution> res = firefly_move(population_.get(i), population_.get(j), beta0, gamma, false, g);
                        population_.replace(i, res.get(0));
                        population_.replace(j, res.get(1));
                    }
                }
            }

            // 评估适应值
            for (int i = 0; i < this.populationSize_; i++) {
                problem_.evaluate(this.population_.get(i));
                evaluations_++;
            }

            // 更新储备集
            Utils.updateExternalArchive(population_, populationSize_, external_archive_);

            // 显示当前储备集中的解
            if (isDisplay_) {
                PlotObjectives.plotSolutions("MOFA", external_archive_);
            }
        } while (evaluations_ < maxEvaluations_);

        SqlUtils.InsertSolutionSet(dbName, tableName, external_archive_);

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

    /**
     * 获得两个个体的支配关系，x1支配x2返回0，x2支配x1返回1，否则返回-1
     *
     * @param s1
     * @param s2
     * @return
     */
    public int get_domination(Solution s1, Solution s2) {
        int less = 0;
        int equal = 0;
        int more = 0;
        int res = 0;

        int M = s1.getNumberOfObjectives();
        for (int i = 0; i < M; i++) {
            if (s1.getObjective(i) < s2.getObjective(i)) {
                less = less + 1;
            } else if (s1.getObjective(i) == s2.getObjective(i)) {
                equal = equal + 1;
            } else {
                more = more + 1;
            }
        }

        if (more == 0 && equal != M) {
            // i支配j
            res = 0;
        } else if (less == 0 && equal != M) {
            // i被j支配
            res = 1;
        } else {
            res = -1;
        }
        return res;
    }

    /**
     * 萤火虫x1向x2移动
     * 当x1、x2之间存在支配关系时，omega = omega0，omega0为[0,1]之间的随机数
     * 当x1、x2之间不存在支配关系，omega = 1 - omega0
     *
     * @param s1
     * @param s2
     * @param beta0      最大吸引度
     * @param gamma      光吸收系数
     * @param domination 是否存在支配关系
     * @param g          精英个体
     * @return
     */
    public List<Solution> firefly_move(Solution s1, Solution s2, double beta0, double gamma, boolean domination, Solution g) throws JMException {
        int V = s1.getDecisionVariables().length;
        // 获得x1和x2之间的距离
        double r = get_distance(s1, s2);
        // 获得x1和x2之间的吸引力
        double beta = get_attraction(r, beta0, gamma);
        // 莱维飞行获得随机扰动
        double s = levy_flights();
        double omega0 = Math.random();

        List<Solution> res = new ArrayList<>();

        // 存在支配关系
        if (domination) {
            // 获得x2与精英个体g之间的距离
            double r_g = get_distance(s2, g);
            // 获得x2和g之间的吸引力
            double beta_g = get_attraction(r_g, beta0, gamma);

            Solution new_x = CloneUtil.clone(s1);
            for (int i = 0; i < V; i++) {
                new_x.getDecisionVariables()[i].setValue(s1.getDecisionVariables()[i].getValue()
                        + omega0 * beta * (s1.getDecisionVariables()[i].getValue() - s2.getDecisionVariables()[i].getValue())
                        + (1 - omega0) * beta_g * (g.getDecisionVariables()[i].getValue() - s2.getDecisionVariables()[i].getValue()));
            }
            res.add(new_x);
        } else {
            // 获得x1与精英个体g之间的距离
            double r_g = get_distance(s1, g);
            // 获得x1和g之间的吸引力
            double beta_g = get_attraction(r_g, beta0, gamma);
            Solution new_x = new Solution(s1);
            for (int i = 0; i < V; i++) {
                new_x.getDecisionVariables()[i].setValue(omega0 * s1.getDecisionVariables()[i].getValue()
                        + (1 - omega0) * beta_g * (g.getDecisionVariables()[i].getValue() - s1.getDecisionVariables()[i].getValue()));
            }
            res.add(new_x);

            // 获得x2与精英个体g之间的距离
            r_g = get_distance(s2, g);
            // 获得x2和g之间的吸引力
            beta_g = get_attraction(r_g, beta0, gamma);
            Solution new_y = new Solution(s1);
            for (int i = 0; i < V; i++) {
                new_y.getDecisionVariables()[i].setValue(omega0 * s2.getDecisionVariables()[i].getValue()
                        + (1 - omega0) * beta_g * (g.getDecisionVariables()[i].getValue() - s2.getDecisionVariables()[i].getValue()));
            }
            res.add(new_y);
        }

        // 越界处理
        for (int i = 0; i < res.size(); i++) {
            outbound(res.get(i));
        }

        return res;
    }

    /**
     * 越界处理
     * @param s
     */
    public void outbound(Solution s) throws JMException {
        int V = s.getDecisionVariables().length;
        for (int i = 0; i < V; i++) {
            if(s.getDecisionVariables()[i].getValue() < problem_.getLowerLimit(i)){
                s.getDecisionVariables()[i].setValue(problem_.getLowerLimit(i));
            }else if(s.getDecisionVariables()[i].getValue() > problem_.getUpperLimit(i)){
                s.getDecisionVariables()[i].setValue(problem_.getUpperLimit(i));
            }
        }
    }

    /**
     * 获得萤火虫x1和x2之间的吸引力
     *
     * @param r     两萤火虫之间的距离
     * @param beta0 最大吸引力
     * @param gamma 光吸收系数
     * @return
     */
    public double get_attraction(double r, double beta0, double gamma) {
        double beta = beta0 * Math.exp(-1 * gamma * Math.pow(r, 2));
        return beta;
    }

    /**
     * 获得萤火虫x1和x2之间的距离[二范数]
     *
     * @param s1
     * @param s2
     * @return
     */
    public double get_distance(Solution s1, Solution s2) throws JMException {
        int V = s1.getDecisionVariables().length;
        double distance = 0.0;
        for (int i = 0; i < V; i++) {
            distance += Math.pow(s1.getDecisionVariables()[i].getValue() - s1.getDecisionVariables()[i].getValue(), 2);
        }
        return distance;
    }

    /**
     * 莱维飞行产生随机扰动
     *
     * @return
     */
    public double levy_flights() {
        // beta为(0,2]之间的常数，一般取值为1.5
        double beta = 1.5;
        double sigma_u = Math.pow((gamma(1 + beta) * Math.sin(Math.PI * beta / 2)) / (gamma((1 + beta) / 2) * beta * Math.pow(2, (beta - 1) / 2)), 1 / beta);// 0.6966
        double sigma_v = 1;
        double u = normrnd(0, sigma_u);// 产生均值为0，标准差为sigma_u的正态分布随机数 0 < u≤0.5232
        double v = normrnd(0, sigma_v);// 产生均值为0，标准差为sigma_v的正态分布随机数 0 < v≤0.3989
        double s = u / Math.pow(Math.abs(v), 1 / beta);
        return s;
    }

    /**
     * 产生正态随机分布
     *
     * @param mean
     * @param std
     * @return
     */
    public double normrnd(double mean, double std) {
        java.util.Random random = new java.util.Random();
        return Math.sqrt(std) * random.nextGaussian() + mean;
    }

    /**
     * gamma函数
     *
     * @param x
     * @param setAbsRelaErr
     * @return
     */
    public double gamma(double x, double setAbsRelaErr) {
        //setAbsRelaErr 相对误差绝对值
        //递归结束条件
        if (x < 0) {
            return gamma(x + 1, setAbsRelaErr) / x;
        }
        if (Math.abs(1.0 - x) < 0.00001) {
            return 1;
        }
        if (Math.abs(0.5 - x) < 0.00001) {
            return Math.sqrt(3.1415926);
        }

        if (x > 1.0) {
            return (x - 1) * gamma(x - 1, setAbsRelaErr);
        }

        double res = 0.0;
        double temp = 1.0;
        double check = 0.0;
        int i = 1;
        while (Math.abs((check - temp) / temp) > setAbsRelaErr) {
            check = temp;
            temp *= i / (x - 1 + i);
            i++;
        }
        res = temp * Math.pow(i, x - 1);
        return res;
    }

    public double gamma(double num) {
        return gamma(num, 0.00001);
    }
}
