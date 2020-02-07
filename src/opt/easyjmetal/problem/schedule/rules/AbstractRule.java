package opt.easyjmetal.problem.schedule.rules;


import opt.easyjmetal.problem.schedule.models.FactObject;
import opt.easyjmetal.problem.schedule.models.Fragment;
import opt.easyjmetal.problem.schedule.util.ISimulationScheduler;

public abstract class AbstractRule {
    protected static ISimulationScheduler _scheduler;

    public abstract Fragment decode(FactObject factObject);

    public AbstractRule(ISimulationScheduler scheduler) {
        _scheduler = scheduler;
    }

    /**
     * 触发规则，按照顺序确定各个决策变量值
     *
     * @param factObject
     * @return
     */
    public Fragment fireAllRule(FactObject factObject) {
        return decode(factObject);
    }
}
