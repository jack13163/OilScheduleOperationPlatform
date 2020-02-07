package opt.jmetal.problem.impl;

import opt.jmetal.solution.BinarySolution;
import opt.jmetal.solution.impl.DefaultBinarySolution;
import opt.jmetal.problem.BinaryProblem;

@SuppressWarnings("serial")
public abstract class AbstractBinaryProblem extends AbstractGenericProblem<BinarySolution>
        implements BinaryProblem {

    protected abstract int getBitsPerVariable(int index);

    @Override
    public int getNumberOfBits(int index) {
        return getBitsPerVariable(index);
    }

    @Override
    public int getTotalNumberOfBits() {
        int count = 0;
        for (int i = 0; i < this.getNumberOfVariables(); i++) {
            count += this.getBitsPerVariable(i);
        }

        return count;
    }

    @Override
    public BinarySolution createSolution() {
        return new DefaultBinarySolution(this);
    }
}
