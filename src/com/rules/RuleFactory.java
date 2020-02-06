package com.rules;

import com.rules.impl.EarliestDealineFirstAndPreemptiveScheduling;
import com.rules.impl.EarliestDealineFirstAndTwoStepsScheduling;
import com.rules.impl.Backtracking;
import com.sim.common.ISimulationScheduler;

public class RuleFactory {

    /**
     * 使用 getRule 方法获取规则对象
     *
     * @param ruleName
     * @param scheduler
     * @return
     */
    public AbstractRule getRule(String ruleName, ISimulationScheduler scheduler) {
        if (ruleName == null) {
            return null;
        }

        if (ruleName.equalsIgnoreCase("EDF_PS")) {
            // Earliest Dealine First, Preemptive Scheduling, EDF
            return new EarliestDealineFirstAndPreemptiveScheduling(scheduler);
        } else if (ruleName.equalsIgnoreCase("EDF_TSS")) {
            // Earliest Dealine First, Two Steps Scheduling, EDF_TSS
            return new EarliestDealineFirstAndTwoStepsScheduling(scheduler);
        } else if (ruleName.equalsIgnoreCase("BT")) {
            // Backtracking, BT
            return new Backtracking(scheduler);
        }
        return null;
    }
}
