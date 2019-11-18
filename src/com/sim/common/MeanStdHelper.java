package com.sim.common;

public class MeanStdHelper {

    private int[] array = new int[10];
    private int num = 10;

    public int getRandomDigit() {
        return (int) (Math.random() * 1000);
    }

    public void getTargetDigit() {

        for (int i = 0; i < num; i++) {
            array[i] = getRandomDigit();
            System.out.println(array[i]);
        }
    }

    //均值
    public double getAverage() {
        int sum = 0;
        for (int i = 0; i < num; i++) {
            sum += array[i];
        }
        return (double) (sum / num);
    }

    //标准差
    public double getStandardDevition() {
        double sum = 0;
        for (int i = 0; i < num; i++) {
            sum += Math.sqrt(((double) array[i] - getAverage()) * (array[i] - getAverage()));
        }
        return (sum / (num - 1));
    }

    public static void main(String[] args) {

        MeanStdHelper gcs = new MeanStdHelper();

        gcs.getTargetDigit();
        System.out.println(gcs.getAverage() + "  " + gcs.getStandardDevition());
    }
}
