package opt.easyjmetal.algorithm.util;


import opt.easyjmetal.core.Solution;

import java.io.Serializable;
import java.util.Comparator;

@SuppressWarnings("serial")
public class FitnessComparator implements Comparator<Solution>, Serializable{

  @Override
  public int compare(Solution solution1, Solution solution2) {
    int result ;
    if (solution1 == null) {
      if (solution2 == null) {
        result = 0;
      } else {
        result = 1 ;
      }
    } else if (solution2 == null) {
      result = -1;
    } else {
      if (solution1.getFitness() < solution2.getFitness()) {
        result = -1;
      } else  if (solution1.getFitness() > solution2.getFitness()) {
        result = 1;
      } else {
        result = 0;
      }
    }
    return result;
  }
}
