package org.uma.jmetal.qualityindicator;

import org.uma.jmetal.util.naming.DescribedEntity;

import java.io.Serializable;

/**
 * @param <Evaluate> Entity to runAlgorithm
 * @param <Result>   Result of the evaluation
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface QualityIndicator<Evaluate, Result> extends DescribedEntity, Serializable {
    public Result evaluate(Evaluate evaluate);
}
