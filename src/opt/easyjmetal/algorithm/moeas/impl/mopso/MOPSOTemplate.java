package opt.easyjmetal.algorithm.moeas.impl.mopso;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.operator.mutation.NonUniformMutation;
import opt.easyjmetal.operator.mutation.UniformMutation;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.archive.CrowdingArchive;
import opt.easyjmetal.util.archive.NonDominatedArchive;
import opt.easyjmetal.util.comparators.CrowdingDistanceComparator;
import opt.easyjmetal.util.comparators.DominanceComparator;
import opt.easyjmetal.util.distance.impl.CrowdingDistance;
import opt.easyjmetal.util.evaluator.SolutionListEvaluator;
import opt.easyjmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import opt.easyjmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MOPSOTemplate extends AbstractParticleSwarmOptimization {

    private Problem problem;
    SolutionListEvaluator evaluator;
    private int swarmSize;
    private int archiveSize;
    private int maxIterations;
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

    public MOPSOTemplate(Problem problem, int swarmSize,
                         int maxIterations, int archiveSize, UniformMutation uniformMutation,
                         NonUniformMutation nonUniformMutation) {
        this.problem = problem;
        this.evaluator = new SequentialSolutionListEvaluator();

        this.swarmSize = swarmSize;
        this.maxIterations = maxIterations;
        this.archiveSize = archiveSize;

        this.uniformMutation = uniformMutation;
        this.nonUniformMutation = nonUniformMutation;

        localBest = new Solution[swarmSize];
        leaderArchive = new CrowdingArchive(this.archiveSize, problem.getNumberOfObjectives());
        epsilonArchive = new NonDominatedArchive();

        dominanceComparator = new DominanceComparator();
        crowdingDistanceComparator = new CrowdingDistanceComparator();

        speed = new double[swarmSize][problem.getNumberOfVariables()];

        randomGenerator = JMetalRandom.getInstance();
        crowdingDistance = new CrowdingDistance();
    }

    @Override
    protected void initProgress() {
        currentIteration = 1;
        crowdingDistance.computeDensityEstimator(leaderArchive.getSolutionList());
    }

    @Override
    protected void updateProgress() {
        currentIteration += 1;
        crowdingDistance.computeDensityEstimator(leaderArchive.getSolutionList());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return currentIteration >= maxIterations;
    }

    @Override
    protected List<Solution> createInitialSwarm() throws ClassNotFoundException {
        List<Solution> swarm = new ArrayList<>(swarmSize);

        Solution newSolution;
        for (int i = 0; i < swarmSize; i++) {
            newSolution = new Solution(problem);
            swarm.add(newSolution);
        }

        return swarm;
    }

    @Override
    protected List<Solution> evaluateSwarm(List<Solution> swarm) throws JMException {
        for (int i = 0; i < swarmSize; i++) {
            problem.evaluate(swarm.get(i));
        }
        return swarm;
    }

    @Override
    public SolutionSet getResult() {
        List<Solution> solutionList = this.epsilonArchive.getSolutionList();
        SolutionSet solutionSet = new SolutionSet();
        for (int i = 0; i < solutionList.size(); i++) {
            solutionSet.add(solutionList.get(i));
        }
        return solutionSet;
    }

    @Override
    protected void initializeLeader(List<Solution> swarm) {
        for (Solution solution : swarm) {
            Solution particle = new Solution(solution);
            if (leaderArchive.add(particle)) {
                epsilonArchive.add(new Solution(particle));
            }
        }
    }

    @Override
    protected void initializeParticlesMemory(List<Solution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            Solution particle = new Solution(swarm.get(i));
            localBest[i] = particle;
        }
    }

    @Override
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
    @Override
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

    @Override
    protected void updateParticlesMemory(List<Solution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            int flag = dominanceComparator.compare(swarm.get(i), localBest[i]);
            if (flag != 1) {
                Solution particle = new Solution(swarm.get(i));
                localBest[i] = particle;
            }
        }
    }

    @Override
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
    @Override
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
    @Override
    protected void updateLeaders(List<Solution> swarm) {
        for (Solution solution : swarm) {
            Solution particle = new Solution(solution);
            if (leaderArchive.add(particle)) {
                epsilonArchive.add(new Solution(particle));
            }
        }
    }
}
