package opt.easyjmetal.algorithm.moeas.impl.mopso;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.JMException;

import java.util.List;

/**
 * 多目标粒子群算法
 */
public abstract class AbstractParticleSwarmOptimization {
    private List<Solution> swarm;

    public List<Solution> getSwarm() {
        return swarm;
    }

    public void setSwarm(List<Solution> swarm) {
        this.swarm = swarm;
    }

    protected abstract void initProgress();

    protected abstract void updateProgress();

    protected abstract boolean isStoppingConditionReached();

    protected abstract List<Solution> createInitialSwarm() throws ClassNotFoundException;

    protected abstract List<Solution> evaluateSwarm(List<Solution> swarm) throws JMException;

    protected abstract void initializeLeader(List<Solution> swarm);

    protected abstract void initializeParticlesMemory(List<Solution> swarm);

    protected abstract void initializeVelocity(List<Solution> swarm);

    protected abstract void updateVelocity(List<Solution> swarm) throws JMException;

    protected abstract void updatePosition(List<Solution> swarm) throws JMException;

    protected abstract void perturbation(List<Solution> swarm) throws JMException;

    protected abstract void updateLeaders(List<Solution> swarm);

    protected abstract void updateParticlesMemory(List<Solution> swarm);

    public abstract SolutionSet getResult();

    /**
     * 算法主流程
     * @return
     * @throws JMException
     * @throws ClassNotFoundException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
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
        }
        SolutionSet solutionSet = new SolutionSet();
        for (int i = 0; i < swarm.size(); i++) {
            solutionSet.add(swarm.get(i));
        }
        return solutionSet;
    }
}
