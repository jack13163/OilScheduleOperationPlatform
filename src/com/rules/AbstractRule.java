package com.rules;

import com.models.FactObject;
import com.models.Fragment;
import com.sim.common.ISimulationScheduler;

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
