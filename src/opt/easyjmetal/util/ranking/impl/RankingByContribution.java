package opt.easyjmetal.util.ranking.impl;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.line.ContributionComparator;
import opt.easyjmetal.util.contribution.Contribution;
import opt.easyjmetal.util.ranking.AbstractRanking;

/**
 * 按照个体的贡献值进行分层
 * 注意：自动进行贡献值计算
 */
public class RankingByContribution extends AbstractRanking {
    public RankingByContribution(SolutionSet solutionSet) {
        super(solutionSet);
        dominance_ = new ContributionComparator();
        // 计算贡献值并分层排序
        Contribution.calculateHVContribution(solutionSet);
        ranking();
    }
}
