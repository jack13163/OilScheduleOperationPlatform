package opt.easyjmetal.algorithm.moeas.impl.mopso;

import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.operator.mutation.NonUniformMutation;
import opt.easyjmetal.operator.mutation.UniformMutation;
import opt.easyjmetal.util.JMException;

import java.util.HashMap;

/**
 * 多目标粒子群算法实现
 */
public class MOPSO extends Algorithm {

    private int swarmSize = 100;
    private int archiveSize = 100;
    private int maxIterations = 100;

    public MOPSO(Problem problem) {
        super(problem);
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

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        UniformMutation uniformMutation = new UniformMutation(new HashMap<String, Object>(){{
            put("probability", 0.8);
            put("perturbation", 20);
        }});
        NonUniformMutation nonUniformMutation = new NonUniformMutation(new HashMap<String, Object>(){{
            put("probability", 0.8);
            put("perturbation", 20);
            put("maxIterations", maxIterations);
        }});
        MOPSOTemplate algorithm = new MOPSOTemplate(getProblem(), swarmSize, maxIterations, archiveSize, uniformMutation,
                nonUniformMutation);
        return algorithm.execute();
    }
}
