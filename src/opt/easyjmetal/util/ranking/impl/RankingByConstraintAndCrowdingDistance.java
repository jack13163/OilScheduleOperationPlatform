package opt.easyjmetal.util.ranking.impl;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.two.ConstraintAndCrowdingDistance;
import opt.easyjmetal.util.ranking.AbstractRanking;

/**
 * ����CVֵ�ͻ��ڲο����ӵ���ȶ��������з�֧������
 */
public class RankingByConstraintAndCrowdingDistance extends AbstractRanking {

	/**
	 * ����ӵ�����룬����������Լ��Υ��ֵ���з�֧������
	 * @param solutionSet
	 * @param w
	 */
	public RankingByConstraintAndCrowdingDistance(SolutionSet solutionSet, double[][] w) {
		super(solutionSet);
		dominance_ = new ConstraintAndCrowdingDistance();
		ranking();
	}
}
