package opt.jmetal.problem;

import opt.jmetal.solution.BinarySolution;

/**
 * Interface representing binary problems
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface BinaryProblem extends Problem<BinarySolution> {
    public int getNumberOfBits(int index);

    public int getTotalNumberOfBits();
}
