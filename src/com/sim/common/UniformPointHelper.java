package com.sim.common;

import java.util.Arrays;
import java.util.Random;

public class UniformPointHelper {

    public static void main(String[] args) {
        UniformPointHelper.GenerateUniformPoint(100, 3);
    }

    /**
     * 返回最大的划分个数
     *
     * @param popSize
     * @param M
     * @return
     */
    public static int getNumOfDivide(int popSize, int M) {
        int H1 = 1;
        while (nchoosek(H1 + M, M - 1) <= popSize) {
            H1++;
        }
        return H1;
    }

    /**
     * 生成均匀参考点【组合数】
     *
     * @param popSize
     * @param numOfObjs
     * @return
     */
    public static double[][] GenerateUniformPoint(int popSize, int numOfObjs) {
        int N = popSize;
        int M = numOfObjs;

        int H1 = 1;
        while (nchoosek(H1 + M, M - 1) <= N) {
            H1++;
        }

        double[][] arr1 = generateNchooseK(H1, M - 1);
        double[][] arr2 = norm(arr1, H1);

        if (H1 < M) {
            int H2 = 0;
            while (nchoosek(H1 + M - 1, M - 1) + nchoosek(H2 + M, M - 1) <= N) {
                H2 = H2 + 1;
            }
            if (H2 > 0) {
                double[][] arr3 = generateNchooseK(H2, M - 1);
                double[][] arr4 = norm(arr3, H2);
                for (int i = 0; i < arr4.length; i++) {
                    for (int j = 0; j < arr4[0].length; j++) {
                        arr4[i][j] = arr4[i][j] / 2.0 + 1.0 / (2 * M);
                    }
                }
                double[][] arr5 = contat(arr2, arr4);
                print(arr5);
                return arr5;
            }
        }

        print(arr2);
        return arr2;
    }

    /**
     * 打印结果
     *
     * @param data
     */
    public static void print(double[][] data) {
        StringBuilder sBuilder = new StringBuilder();
        int numOfObjs = data[0].length;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < numOfObjs; j++) {
                if (j == numOfObjs - 1) {
                    sBuilder.append(data[i][j] + "\n");
                } else {
                    sBuilder.append(data[i][j] + ",");
                }
            }
        }
        System.out.println(sBuilder.toString());
    }

    /**
     * 从n个数中选择k个，返回是否选中标记
     *
     * @param n
     * @param k
     * @return
     */
    public static boolean[] select(int n, int k) {
        boolean[] flags = new boolean[n];
        int count = 0;
        Random random = new Random();
        while (count < k) {
            int row = -1;// 不包括numOfReferencePoints
            do {
                row = random.nextInt(k);// 不包括numOfReferencePoints
            } while (flags[row]);

            flags[row] = true;
            count++;
        }
        return flags;
    }

    /**
     * 数组拼接
     *
     * @param array1
     * @param array2
     * @return
     */
    public static double[][] contat(double[][] array1, double[][] array2) {
        int total = array1.length + array2.length;
        int d = array1[0].length;
        double[][] array = new double[total][d];
        for (int i = 0; i < total; i++) {
            if (i < array1.length) {
                for (int j = 0; j < d; j++) {
                    array[i][j] = array1[i][j];
                }
            } else {
                for (int j = 0; j < d; j++) {
                    array[i][j] = array2[i - array1.length][j];
                }
            }
        }

        return array;
    }

    /**
     * 输出从0~n中选择d个数的排列组合
     *
     * @param n 数量
     * @param d 维度
     * @return
     */
    public static double[][] generateNchooseK(int n, int d) {

        int numOfReferencePoints = nchoosek(n + d, d);
        double[][] result = new double[numOfReferencePoints][d];

        int loc = 0;
        if (d == 1) {
            for (int i = 0; i <= n; i++) {
                result[i][0] = i;
            }
        } else if (d == 2) {
            for (int i = 0; i <= n; i++) {
                for (int j = i; j <= n; j++) {
                    result[loc][0] = i;
                    result[loc][1] = j;
                    loc++;
                }
            }
        } else if (d == 3) {
            for (int i = 0; i <= n; i++) {
                for (int j = i; j <= n; j++) {
                    for (int k = j; k <= n; k++) {
                        result[loc][0] = i;
                        result[loc][1] = j;
                        result[loc][2] = k;
                        loc++;
                    }
                }
            }
        } else if (d == 4) {
            for (int i = 0; i <= n; i++) {
                for (int j = i; j <= n; j++) {
                    for (int k = j; k <= n; k++) {
                        for (int l = k; l <= n; l++) {
                            result[loc][0] = i;
                            result[loc][1] = j;
                            result[loc][2] = k;
                            result[loc][3] = l;
                            loc++;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 标准化到0~1，每一个数据的和为1
     *
     * @param data
     * @param n
     * @return
     */
    public static double[][] norm(double[][] data, int n) {
        int numOfReferencePoints = data.length;
        int d = data[0].length + 1;
        double[][] result = new double[numOfReferencePoints][d];
        // 生成所有组合
        for (int i = 0; i < numOfReferencePoints; i++) {
            for (int j = 0; j < d; j++) {
                if (j == 0) {
                    result[i][j] = data[i][j];
                } else if (j < d - 1) {
                    result[i][j] = data[i][j] - data[i][j - 1];
                } else if (j == d - 1) {
                    result[i][j] = n - data[i][j - 1];
                }
            }
        }

        // 标准化为0~1之间
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = MathUtil.round(MathUtil.divide(result[i][j], n), 5);
            }
        }

        return result;
    }

    public static void checknk(int n, int k) {
        if (k < 0 || k > n) { // N must be a positive integer.
            throw new IllegalArgumentException("K must be an integer between 0 and N.");
        }
    }

    public static int nchoosek(int n, int k) {
        checknk(n, k);
        k = k > (n - k) ? n - k : k;
        if (k <= 1) { // C(n, 0) = 1, C(n, 1) = n
            return k == 0 ? 1 : n;
        }
        int[] divisors = new int[k]; // n - k + 1 : n
        int firstDivisor = n - k + 1;
        for (int i = 0; i < k; i++) {
            divisors[i] = firstDivisor + i;
        }
        outer:
        for (int dividend = 2; dividend <= k; dividend++) {
            for (int i = k - 1; i >= 0; i--) {
                int divisor = divisors[i];
                if (divisor % dividend == 0) {
                    divisors[i] = divisor / dividend;
                    continue outer;
                }
            }
            int[] perms = factor(dividend);
            for (int perm : perms) {
                for (int j = 0; j < k; j++) {
                    int divisor = divisors[j];
                    if (divisor % perm == 0) {
                        divisors[j] = divisor / perm;
                        break;
                    }
                }
            }
        }
        int cnk = 1;
        for (int i = 0; i < k; i++) {
            cnk *= divisors[i];
        }
        return cnk;
    }

    /**
     * 素数分解
     *
     * @param n
     * @return
     */
    public static int[] factor(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("N must be a non negative integer.");
        }
        if (n < 4) {
            return new int[]{n};
        }
        int factorNums = (int) (Math.log(Integer.highestOneBit(n)) / Math.log(2));
        int[] factors = new int[factorNums];
        int factorCount = 0;
        for (int i = 2; i <= (int) Math.sqrt(n); i++) {
            if (n % i == 0) {
                factors[factorCount++] = i;
                n /= i;
                i = 1;
            }
        }
        factors[factorCount++] = n;
        return Arrays.copyOf(factors, factorCount);
    }
}
