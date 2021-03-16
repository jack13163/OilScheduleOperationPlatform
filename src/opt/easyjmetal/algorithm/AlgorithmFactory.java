package opt.easyjmetal.algorithm;

import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.util.JMException;

import java.lang.reflect.Constructor;

public class AlgorithmFactory {

    /**
     * 利用反射创建算法实例
     *
     * @param name
     * @param params
     * @return
     * @throws JMException
     */
    public static Algorithm getAlgorithm(String name, Object[] params) throws JMException {
        String base = "opt.easyjmetal.algorithm.";

        if (name.equalsIgnoreCase("NSGAIII_CDP")) {
            base += "cmoeas.impl.nsgaiii_cdp.";
        } else if (name.equalsIgnoreCase("SPEA2_CDP")) {
            base += "cmoeas.impl.spea2_cdp.";
        } else if (name.equalsIgnoreCase("ISDEPLUS_CDP")) {
            base += "cmoeas.impl.isdeplus_cdp.";
        } else if (name.equalsIgnoreCase("C_TAEA")) {
            base += "cmoeas.impl.c_taea.";
        } else if (name.equalsIgnoreCase("CMMO")) {
            base += "cmoeas.impl.cmmo.";
        } else if (name.equalsIgnoreCase("C_MOEAD")
                || name.equalsIgnoreCase("MOEAD_CDP")
                || name.equalsIgnoreCase("MOEAD_Epsilon")
                || name.equalsIgnoreCase("MOEAD_IEpsilon")
                || name.equalsIgnoreCase("MOEAD_SR")
                || name.equalsIgnoreCase("NSGAII_CDP")
                || name.equalsIgnoreCase("PPS_MOEAD")) {
            base += "cmoeas.impl.";
        } else if (name.equalsIgnoreCase("NSGAII")
                || name.equalsIgnoreCase("MOFA")
                || name.equalsIgnoreCase("MOPSO")
                || name.equalsIgnoreCase("MOEAD")
                || name.equalsIgnoreCase("IBEA")
                || name.equalsIgnoreCase("ISDEPlus")
                || name.equalsIgnoreCase("SPEA2")
                || name.equalsIgnoreCase("NSGAIII")) {
            base += "moeas.impl.";
        }

        try {
            Class AlgorithmClass = Class.forName(base + name);
            Constructor[] constructors = AlgorithmClass.getConstructors();
            int i = 0;
            // 根据参数个数查找构造函数
            while ((i < constructors.length) && (constructors[i].getParameterTypes().length != params.length)) {
                i++;
            }
            Algorithm algorithm = (Algorithm) constructors[i].newInstance(params);
            return algorithm;
        } catch (Exception e) {
            e.printStackTrace();
            throw new JMException("Exception in " + name + ".getAlgorithm()");
        }
    }
}
