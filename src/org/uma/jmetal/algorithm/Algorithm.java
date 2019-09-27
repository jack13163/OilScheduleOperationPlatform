package org.uma.jmetal.algorithm;

import java.io.Serializable;
import java.util.List;

import org.uma.jmetal.util.naming.DescribedEntity;

/**
 * Interface representing an algorithm
 * 
 * @author Antonio J. Nebro
 * @version 0.1
 * @param <Result> Result
 */
public interface Algorithm<Result> extends Runnable, Serializable, DescribedEntity {
	void run();

	Result getResult();

	List<Double[]> getSolutions();

	void clearSolutions();
}
