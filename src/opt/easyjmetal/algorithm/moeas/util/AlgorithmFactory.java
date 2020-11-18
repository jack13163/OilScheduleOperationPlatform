package opt.easyjmetal.algorithm.moeas.util;

import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.util.JMException;

import java.lang.reflect.Constructor;

public class AlgorithmFactory {

    /**
     * 利用反射创建算法实例
     * @param name
     * @param params
     * @return
     * @throws JMException
     */
    public static Algorithm getAlgorithm(String name, Object[] params) throws JMException {
        String base = "opt.easyjmetal.algorithm.moeas.";

        if (name.equalsIgnoreCase("NSGAII")) {
            base += "impl.";
        } else if (name.equalsIgnoreCase("MOFA")) {
            base += "impl.";
        } else if (name.equalsIgnoreCase("MOPSO")) {
            base += "impl.";
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
