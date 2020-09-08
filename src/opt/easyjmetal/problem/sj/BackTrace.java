package opt.easyjmetal.problem.sj;

import java.io.Serializable;
import java.util.List;

public class BackTrace implements Serializable {
    public boolean Flag;
    public int step;
    public double[] x;
    public List<Integer> ET;
    public List<Integer> UD;
    public double PET;
    public double[] DSFET;
    public double[] FP;
    public double[][] TKS;
    public List<List<Double>> schedulePlan;
    public double[] DSFR;
    public int PIPEFR;
    public int[][] FPORDER;
    public int RT;

    public BackTrace(double[] x, int step, List<Integer> ET, List<Integer> UD, double PET, double[] DSFET, double[] FP, double[][] TKS, List<List<Double>> schedulePlan, double[] DSFR, int PIPEFR, int[][] FPORDER, int RT) {
        this.step = step;
        this.x = x;
        this.ET = ET;
        this.UD = UD;
        this.PET = PET;
        this.DSFET = DSFET;
        this.FP = FP;
        this.TKS = TKS;
        this.schedulePlan = schedulePlan;
        this.DSFR = DSFR;
        this.PIPEFR = PIPEFR;
        this.FPORDER = FPORDER;
        this.RT = RT;
    }

    public double[] getDSFR() {
        return DSFR;
    }

    public void setDSFR(double[] DSFR) {
        this.DSFR = DSFR;
    }

    public boolean getFlag() {
        return Flag;
    }

    public void setFlag(boolean flag) {
        Flag = flag;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public void setET(List<Integer> ET) {
        this.ET = ET;
    }

    public void setUD(List<Integer> UD) {
        this.UD = UD;
    }

    public void setPET(double PET) {
        this.PET = PET;
    }

    public void setDSFET(double[] DSFET) {
        this.DSFET = DSFET;
    }

    public void setFP(double[] FP) {
        this.FP = FP;
    }

    public void setTKS(double[][] TKS) {
        this.TKS = TKS;
    }

    public void setSchedulePlan(List<List<Double>> schedulePlan) {
        this.schedulePlan = schedulePlan;
    }

    public void setPIPEFR(int PIPEFR) {
        this.PIPEFR = PIPEFR;
    }

    public void setFPORDER(int[][] FPORDER) {
        this.FPORDER = FPORDER;
    }

    public void setRT(int RT) {
        this.RT = RT;
    }

    public int getStep() {
        return step;
    }

    public double[] getX() {
        return x;
    }

    public List<Integer> getET() {
        return ET;
    }

    public List<Integer> getUD() {
        return UD;
    }

    public double getPET() {
        return PET;
    }

    public double[] getDSFET() {
        return DSFET;
    }

    public double[] getFP() {
        return FP;
    }

    public double[][] getTKS() {
        return TKS;
    }

    public List<List<Double>> getSchedulePlan() {
        return schedulePlan;
    }

    public int getPIPEFR() {
        return PIPEFR;
    }

    public int[][] getFPORDER() {
        return FPORDER;
    }

    public int getRT() {
        return RT;
    }
}
