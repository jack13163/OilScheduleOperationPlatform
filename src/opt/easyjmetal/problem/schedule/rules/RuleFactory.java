package opt.easyjmetal.problem.schedule.rules;

import opt.easyjmetal.problem.schedule.rules.impl.Backtracking;
import opt.easyjmetal.problem.schedule.rules.impl.EarliestDealineFirstAndPreemptiveScheduling;
import opt.easyjmetal.problem.schedule.rules.impl.EarliestDealineFirstAndTwoStepsScheduling;
import opt.easyjmetal.problem.schedule.util.ISimulationScheduler;

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
