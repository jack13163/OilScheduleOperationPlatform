package opt.easyjmetal.problem.schedule.rules;


import opt.easyjmetal.problem.schedule.models.FactObject;
import opt.easyjmetal.problem.schedule.models.Fragment;
import opt.easyjmetal.util.JMException;

public abstract class AbstractRule {
    protected static ISimulationScheduler _scheduler;

    public abstract Fragment decode(FactObject factObject) throws JMException;

    public AbstractRule(ISimulationScheduler scheduler) {
        _scheduler = scheduler;
    }

    /**
     * 触发规则，按照顺序确定各个决策变量值
     *
     * @param factObject
     * @return
     */
    public Fragment fireAllRule(FactObject factObject) throws JMException {
        return decode(factObject);
    }
}
