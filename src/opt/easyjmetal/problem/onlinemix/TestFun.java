package opt.easyjmetal.problem.onlinemix;

import java.util.*;

public class TestFun {
    public static void sort(double[][] ob, int[] order) {
        Arrays.sort(ob, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {    //降序排列
                double[] one = (double[]) o1;
                double[] two = (double[]) o2;
                for (int i = 0; i < order.length; i++) {
                    int k = order[i];
                    if (one[k] > two[k]) return 1; //返回值大于0，将前一个目标值和后一个目标值交换
                    else if (one[k] < two[k]) return -1;
                    else continue;
                }
                return 0;
            }

        });
    }

    public static int getInt(double a, int len) {  //返回一个0-length之间的整数
        double[] sequence = getHDsequence(0.29583, 1000);
        int index = (int) (Math.ceil(a * 1000));
        if (index == 1000) {
            index = 0;
        }
        if (a != 0) a = sequence[index];  //ceil向无穷大方向取整,将随机产生的数转换为混沌序列
        if (a == 1) {
            return len;
        } else {
            return (int) (Math.floor((len + 1) * a)); //取整（舍掉小数）
        }
    }

    public static double[] getHDsequence(double x0, int num) {  //获取混沌序列
        double[] r = new double[num];
        double xn = x0;
        for (int i = 0; i < num; i++) {
            xn = 4 * xn * (1 - xn);
            r[i] = xn;
        }
        return r;
    }

    public static double getMax(double[] array) { //取得一维数组中的最大值
        double max = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static double gNum(List<List<Double>> a) { //计算供油罐个数
        List<Double> TKset = new ArrayList<Double>();
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).get(1) != 0) {
                TKset.add(a.get(i).get(1));
            }
        }
        HashSet h = new HashSet(TKset);//清除重复数据
        TKset.clear();
        TKset.addAll(h);
        return TKset.size();
    }

    public static double gChange(List<List<Double>> a) { //油罐切换次数
        double K = 0;
        double[] array = new double[a.size()];
        for (int i = 0; i < a.size(); i++) {
            array[i] = a.get(i).get(0);
        }
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).get(0) >= TestFun.getMax(array)) {
                K = i;
                break;
            }
        }
        return K;
    }

    public static double gDmix(List<List<Double>> a, int[][] c1) {//计算管道混合成本
        int K = 0;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).get(0) == 4d) {
                K = i;
                break;
            }
        }
        double[][] m1 = new double[6][6];//用于存放混合次数
        List<List<Double>> PIPE = new ArrayList<List<Double>>();
        double sum = 0;
        for (int i = K; i < a.size(); i++) {
            if (a.get(i).get(4) != 0d) {
                PIPE.add(a.get(i));
            }
        }
        for (int i = 0; i < PIPE.size() - 1; i++) {
            if (!(Double.toString(PIPE.get(i).get(4)).equals(Double.toString(PIPE.get(i + 1).get(4))))) {
                double m, n;
                m = PIPE.get(i).get(4);
                n = PIPE.get(i + 1).get(4);
                m1[(int) m - 1][(int) n - 1]++;
            }
        }
        //计算混合成本
        for (int i = 0; i < c1.length; i++) {
            for (int j = 0; j < c1[0].length; j++) {
                sum = sum + c1[i][j] * m1[i][j];
            }
        }
        return sum;
    }

    public static double gDimix(List<List<Double>> a, int[][] c2) { //计算罐底混合成本
        double[][] m2 = new double[6][6]; //存放各个类型油的混合次数
        int K = 0;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).get(0) >= 4d) {
                K = i;
                break;
            }
        }
        double[][] record = new double[K][5];
        for (int i = 0; i < record.length; i++) {
            for (int j = 0; j < 5; j++) {
                record[i][j] = a.get(i).get(j);
            }
        }
        sort(record, new int[]{1}); //按照油罐序号升序排列
        //统计各个油罐的使用次数
        List<Double> list = new ArrayList<Double>();
        List<Double> list1 = new ArrayList<Double>();
        for (int i = 0; i < record.length; i++) { //存放所有的油罐号
            list.add(record[i][1]);
            list1.add(record[i][1]);
        }
        HashSet h = new HashSet(list1);
        list1.clear();
        list1.addAll(h);
        Collections.sort(list1);
        int[] tks = new int[list1.size()]; //记录各个油罐罐使用次数
        for (int i = 0; i < tks.length; i++) {
            for (int j = 0; j < list.size(); j++) {
                if (Double.toString(list1.get(i)).equals(Double.toString(list.get(j)))) {
                    tks[i]++;
                }
            }
        }
        //根据炼油记录表分析罐底混合次数
        for (int i = 0; i < tks.length; i++) {
            int tmp = 0;
            for (int j = 0; j < record.length - 1; j++) {
                if ((int) record[j][1] == i + 1) {
                    tmp++;
                    if (tmp >= tks[i]) break;
                    if (!(Double.toString(record[j][4]).equals(record[j + 1][4]))) {
                        m2[(int) record[j][4] - 1][(int) record[j + 1][4] - 1]++;
                    }
                }
            }
        }
        double sum = 0;
        for (int i = 0; i < m2.length; i++) {
            for (int j = 0; j < m2[0].length; j++) {
                sum = sum + m2[i][j] * c2[i][j];
            }
        }
        return sum;
    }

    public static double sum(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum = sum + array[i];
        }
        return sum;
    }

    public static int all(int[] array) {
        int a = 1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) {
                a = 0;
            }
        }
        return a;
    }
}
