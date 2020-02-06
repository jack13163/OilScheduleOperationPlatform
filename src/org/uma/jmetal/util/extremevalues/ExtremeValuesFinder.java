package org.uma.jmetal.util.extremevalues;

/**
 * Interface representing classes aimed at finding the extreme values of Source objects (e.g., lists)
 *
 * @param <Source>
 * @param <Result>
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface ExtremeValuesFinder<Source, Result> {
    Result findLowestValues(Source source);

    Result findHighestValues(Source source);
}
