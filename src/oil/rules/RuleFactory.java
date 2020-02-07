package oil.rules;

import oil.rules.impl.EarliestDealineFirstAndPreemptiveScheduling;
import oil.rules.impl.EarliestDealineFirstAndTwoStepsScheduling;
import oil.rules.impl.Backtracking;
import oil.sim.common.ISimulationScheduler;

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
