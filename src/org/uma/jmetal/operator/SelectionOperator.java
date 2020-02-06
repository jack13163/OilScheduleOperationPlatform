package org.uma.jmetal.operator;

/**
 * Interface representing selection operators
 *
 * @param <Source> Class of the source object (typically, a list of solutions)
 * @param <Result> Class of the result of applying the operator
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface SelectionOperator<Source, Result> extends Operator<Source, Result> {
}
