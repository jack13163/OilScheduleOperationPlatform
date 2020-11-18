package opt.easyjmetal.operator.mutation;

import opt.easyjmetal.util.Configuration;
import opt.easyjmetal.util.JMException;

import java.util.HashMap;

public class MutationFactory {

    /**
     * 根据名字获取变异算子
     *
     * @param name of the operator
     * @return the operator
     * @throws JMException
     */
    public static Mutation getMutationOperator(String name, HashMap parameters) throws JMException {

        if (name.equalsIgnoreCase("PolynomialMutation"))
            return new PolynomialMutation(parameters);
        else if (name.equalsIgnoreCase("BitFlipMutation"))
            return new BitFlipMutation(parameters);
        else if (name.equalsIgnoreCase("NonUniformMutation"))
            return new NonUniformMutation(parameters);
        else if (name.equalsIgnoreCase("SwapMutation"))
            return new SwapMutation(parameters);
        else {
            Configuration.logger_.severe("Operator '" + name + "' not found ");
            Class cls = String.class;
            String name2 = cls.getName();
            throw new JMException("Exception in " + name2 + ".getMutationOperator()");
        }
    }
}
