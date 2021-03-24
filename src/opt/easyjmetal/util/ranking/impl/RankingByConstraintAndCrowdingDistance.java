package opt.easyjmetal.util.ranking.impl;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.two.ConstraintAndCrowdingDistance;
import opt.easyjmetal.util.ranking.AbstractRanking;

/**
 * 按照CV值和基于参考点的拥挤度度量来进行非支配排序
 */
public class RankingByConstraintAndCrowdingDistance extends AbstractRanking {

	/**
	 * 计算拥挤距离，并根据它和约束违背值进行非支配排序
	 * @param solutionSet
	 * @param w
	 */
	public RankingByConstraintAndCrowdingDistance(SolutionSet solutionSet, double[][] w) {
		super(solutionSet);
		dominance_ = new ConstraintAndCrowdingDistance();
		ranking();
	}
}
