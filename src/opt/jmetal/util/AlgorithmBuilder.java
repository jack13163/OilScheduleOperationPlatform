package opt.jmetal.util;


import opt.jmetal.algorithm.Algorithm;

/**
 * Interface representing algorithm builders
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface AlgorithmBuilder<A extends Algorithm<?>> {
  public A build() ;
}
