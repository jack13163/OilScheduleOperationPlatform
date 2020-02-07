package opt.jmetal.solution;

/**
 * Interface representing a double solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface DoubleSolution extends Solution<Double> {
    public Double getLowerBound(int index);

    public Double getUpperBound(int index);
}
