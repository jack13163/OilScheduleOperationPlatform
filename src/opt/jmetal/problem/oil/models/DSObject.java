package opt.jmetal.problem.oil.models;

import java.io.Serializable;
import java.util.List;

import opt.jmetal.problem.oil.sim.common.MathUtil;
import opt.jmetal.problem.oil.sim.oil.Config;

public class DSObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FPObject> fps;

    private double speed;

    private double finishTime;

    public List<FPObject> getFps() {
        return fps;
    }

    public void setFps(List<FPObject> fps) {
        this.fps = fps;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        if (this.getNextOilVolume() <= 0) {
            return true;
        } else {
            return false;
        }
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public DSObject() {
    }

    /**
     * 获取下一种原油
     *
     * @return
     */
    public int getNextOilType() {
        int result = -1;

        for (int i = 0; i < fps.size(); i++) {
            if (MathUtil.round(fps.get(i).getVolume(), Config.getInstance().Precision) > 0) {
                result = fps.get(i).getOiltype();
                break;
            }
        }
        return result;
    }

    /**
     * 获取下一种原油来自于哪一个港口
     *
     * @return
     */
    public int getWhereNextOilFrom() {
        int result = -1;

        for (int i = 0; i < fps.size(); i++) {
            if (MathUtil.round(fps.get(i).getVolume(), Config.getInstance().Precision) > 0) {
                result = fps.get(i).getSite();
                break;
            }
        }
        return result;
    }

    /**
     * 获取下一种原油的体积
     *
     * @return
     */
    public double getNextOilVolume() {
        double result = -1;

        for (int i = 0; i < fps.size(); i++) {
            if (MathUtil.round(fps.get(i).getVolume(), Config.getInstance().Precision) > 0) {
                result = MathUtil.round(fps.get(i).getVolume(), Config.getInstance().Precision);
                break;
            }
        }
        return result;
    }

    /**
     * 更新当前调度原油的体积
     *
     * @param vol
     */
    public void updateOilVolume(double vol) {

        for (int i = 0; i < fps.size(); i++) {
            if (MathUtil.round(fps.get(i).getVolume(), Config.getInstance().Precision) > 0) {
                double result = MathUtil.round(
                        MathUtil.subtract(String.valueOf(fps.get(i).getVolume()), String.valueOf(vol)),
                        Config.getInstance().Precision);
                fps.get(i).setVolume(result);
                break;
            }
        }
    }
}
