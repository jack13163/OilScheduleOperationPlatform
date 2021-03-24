package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.operator.mutation.NonUniformMutation;
import opt.easyjmetal.operator.mutation.UniformMutation;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.plot.PlotObjectives;
import opt.easyjmetal.util.archive.CrowdingArchive;
import opt.easyjmetal.util.archive.NonDominatedArchive;
import opt.easyjmetal.util.comparators.one.CrowdingDistanceComparator;
import opt.easyjmetal.util.comparators.one.DominanceComparator;
import opt.easyjmetal.util.evaluator.SolutionListEvaluator;
import opt.easyjmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import opt.easyjmetal.util.pseudorandom.JMetalRandom;
import opt.easyjmetal.util.sqlite.SqlUtils;
import opt.jmetal.util.solutionattribute.impl.CrowdingDistance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MOPSO extends Algorithm {

    private String dataDirectory_;

    public MOPSO(Problem problem) {
        super(problem);

        this.problem = problem;
        this.evaluator = new SequentialSolutionListEvaluator();
        this.uniformMutation = new UniformMutation(new HashMap<String, Object>(){{
            put("probability", 0.8);
            put("perturbation", 20);
        }});
        this.nonUniformMutation = new NonUniformMutation(new HashMap<String, Object>(){{
            put("probability", 0.8);
            put("perturbation", 20);
            put("maxIterations", maxIterations);
        }});
        localBest = new Solution[swarmSize];
        leaderArchive = new CrowdingArchive(this.archiveSize, problem.getNumberOfObjectives());
        epsilonArchive = new NonDominatedArchive();
        dominanceComparator = new DominanceComparator();
        crowdingDistanceComparator = new CrowdingDistanceComparator();
        speed = new double[swarmSize][problem.getNumberOfVariables()];
        randomGenerator = JMetalRandom.getInstance();
        crowdingDistance = new CrowdingDistance();
    }

    public MOPSO setSwarmSize(int swarmSize) {
        this.swarmSize = swarmSize;
        return this;
    }

    public MOPSO setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;
        return this;
    }

    public MOPSO setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    public int getArchiveSize() {
        return archiveSize;
    }

    public int getSwarmSize() {
        return swarmSize;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public List<Solution> getSwarm() {
        return swarm;
    }

    public void setSwarm(List<Solution> swarm) {
        this.swarm = swarm;
    }

    private List<Solution> swarm;
    private Problem problem;
    SolutionListEvaluator evaluator;
    private int swarmSize = 100;
    private int archiveSize = 100;
    private int maxIterations = 100;
    private int currentIteration;
    private Solution[] localBest;
    private CrowdingArchive leaderArchive;
    private NonDominatedArchive epsilonArchive;
    private double[][] speed;
    private Comparator<Solution> dominanceComparator;
    private Comparator<Solution> crowdingDistanceComparator;
    private UniformMutation uniformMutation;
    private NonUniformMutation nonUniformMutation;
    private JMetalRandom randomGenerator;
    private CrowdingDistance crowdingDistance;

    protected void initProgress() {
        currentIteration = 1;
        crowdingDistance.computeDensityEstimator(leaderArchive.getSolutionList());
    }

    protected void updateProgress() {
        currentIteration += 1;
        crowdingDistance.computeDensityEstimator(leaderArchive.getSolutionList());
    }

    protected boolean isStoppingConditionReached() {
        return currentIteration >= maxIterations;
    }

    protected List<Solution> createInitialSwarm() throws ClassNotFoundException {
        List<Solution> swarm = new ArrayList<>(swarmSize);

        Solution newSolution;
        for (int i = 0; i < swarmSize; i++) {
            newSolution = new Solution(problem);
            swarm.add(newSolution);
        }

        return swarm;
    }

    protected List<Solution> evaluateSwarm(List<Solution> swarm) throws JMException {
        for (int i = 0; i < swarmSize; i++) {
            problem.evaluate(swarm.get(i));
        }
        return swarm;
    }

    public SolutionSet getResult() {
        List<Solution> solutionList = this.epsilonArchive.getSolutionList();
        SolutionSet solutionSet = new SolutionSet();
        for (int i = 0; i < solutionList.size(); i++) {
            solutionSet.add(solutionList.get(i));
        }
        return solutionSet;
    }

    protected void initializeLeader(List<Solution> swarm) {
        for (Solution solution : swarm) {
            Solution particle = new Solution(solution);
            if (leaderArchive.add(particle)) {
                epsilonArchive.add(new Solution(particle));
            }
        }
    }

    protected void initializeParticlesMemory(List<Solution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            Solution particle = new Solution(swarm.get(i));
            localBest[i] = particle;
        }
    }

    protected void updateVelocity(List<Solution> swarm) throws JMException {
        double r1, r2, W, C1, C2;
        Solution bestGlobal;

        for (int i = 0; i < swarmSize; i++) {
            Solution particle = swarm.get(i);
            Solution bestParticle = (Solution) localBest[i];

            // Select a global localBest for calculate the speed of particle i, bestGlobal
            Solution one;
            Solution two;
            int pos1 = randomGenerator.nextInt(0, leaderArchive.getSolutionList().size() - 1);
            int pos2 = randomGenerator.nextInt(0, leaderArchive.getSolutionList().size() - 1);
            one = leaderArchive.getSolutionList().get(pos1);
            two = leaderArchive.getSolutionList().get(pos2);

            if (crowdingDistanceComparator.compare(one, two) < 1) {
                bestGlobal = one;
            } else {
                bestGlobal = two;
            }

            // Parameters for velocity equation
            r1 = randomGenerator.nextDouble();
            r2 = randomGenerator.nextDouble();
            C1 = randomGenerator.nextDouble(1.5, 2.0);
            C2 = randomGenerator.nextDouble(1.5, 2.0);
            W = randomGenerator.nextDouble(0.1, 0.5);

            for (int var = 0; var < problem.getNumberOfVariables(); var++) {
                // Computing the velocity of this particle
                speed[i][var] = W * speed[i][var]
                        + C1 * r1 * (bestParticle.getDecisionVariables()[var].getValue() - particle.getDecisionVariables()[var].getValue())
                        + C2 * r2 * (bestGlobal.getDecisionVariables()[var].getValue() - particle.getDecisionVariables()[var].getValue());
            }
        }
    }

    /**
     * Update the position of each particle
     */
    protected void updatePosition(List<Solution> swarm) throws JMException {
        for (int i = 0; i < swarmSize; i++) {
            Solution particle = swarm.get(i);
            for (int var = 0; var < problem.getNumberOfVariables(); var++) {
                particle.getDecisionVariables()[var].setValue(particle.getDecisionVariables()[var].getValue() + speed[i][var]);
                if (particle.getDecisionVariables()[var].getValue() < problem.getLowerLimit(var)) {
                    particle.getDecisionVariables()[var].setValue(problem.getLowerLimit(var));
                    speed[i][var] = speed[i][var] * -1.0;
                }
                if (particle.getDecisionVariables()[var].getValue() > problem.getUpperLimit(var)) {
                    particle.getDecisionVariables()[var].setValue(problem.getUpperLimit(var));
                    speed[i][var] = speed[i][var] * -1.0;
                }
            }
        }
    }

    protected void updateParticlesMemory(List<Solution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            int flag = dominanceComparator.compare(swarm.get(i), localBest[i]);
            if (flag != 1) {
                Solution particle = new Solution(swarm.get(i));
                localBest[i] = particle;
            }
        }
    }

    protected void initializeVelocity(List<Solution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            for (int j = 0; j < problem.getNumberOfVariables(); j++) {
                speed[i][j] = 0.0;
            }
        }
    }

    /**
     * Apply a mutation operator to all particles in the swarm (perturbation)
     */
    protected void perturbation(List<Solution> swarm) throws JMException {
        nonUniformMutation.setParameter("currentIteration", currentIteration);

        for (int i = 0; i < swarm.size(); i++) {
            if (i % 3 == 0) {
                nonUniformMutation.execute(swarm.get(i));
            } else if (i % 3 == 1) {
                uniformMutation.execute(swarm.get(i));
            }
        }
    }

    /**
     * Update leaders method
     *
     * @param swarm List of solutions (swarm)
     */
    protected void updateLeaders(List<Solution> swarm) {
        for (Solution solution : swarm) {
            Solution particle = new Solution(solution);
            if (leaderArchive.add(particle)) {
                epsilonArchive.add(new Solution(particle));
            }
        }
    }

    /**
     * 算法主流程
     * @return
     * @throws JMException
     * @throws ClassNotFoundException
     */
    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int runningTime = (Integer) getInputParameter("runningTime");
        dataDirectory_ = getInputParameter("dataDirectory").toString();

        // 创建数据表，方便后面保存结果
        String dbName = dataDirectory_ + problem_.getName();
        String tableName = "MOPSO_" + runningTime;
        SqlUtils.createTable(tableName, dbName);
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");

        swarm = createInitialSwarm();
        swarm = evaluateSwarm(swarm);
        initializeVelocity(swarm);
        initializeParticlesMemory(swarm);
        initializeLeader(swarm);
        initProgress();

        while (!isStoppingConditionReached()) {
            updateVelocity(swarm);
            updatePosition(swarm);
            perturbation(swarm);
            swarm = evaluateSwarm(swarm);
            updateLeaders(swarm);
            updateParticlesMemory(swarm);
            updateProgress();

            // 显示当前储备集中的解
            if (isDisplay_) {
                PlotObjectives.plotSolutions("MOPSO", epsilonArchive);
            }
        }
        SolutionSet solutionSet = new SolutionSet(swarmSize);
        for (int i = 0; i < swarm.size(); i++) {
            solutionSet.add(swarm.get(i));
        }
        // 插入到数据库中
        SqlUtils.insertSolutionSet(dbName, tableName, epsilonArchive);

        return solutionSet;
    }
}
