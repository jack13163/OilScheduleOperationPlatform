package opt.jmetal.operator;

/**
 * Interface representing a local search operator
 * <p>
 * Created by cbarba on 5/3/15.
 */
public interface LocalSearchOperator<Source> extends Operator<Source, Source> {
    int getEvaluations();

    int getNumberOfImprovements();

    int getNumberOfNonComparableSolutions();
}
