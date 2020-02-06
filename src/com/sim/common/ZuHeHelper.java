package com.sim.common;

import java.util.Stack;

public class ZuHeHelper {
    public static Stack<Integer> stack = new Stack<Integer>();

    public static void main(String[] args) {
        int shu[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        f(shu, 3, 0, 0); // 从数组中选择三个
    }

    /**
     * @param shu  可选集合
     * @param targ 要选多少个元素
     * @param has  当前有多少个元素
     * @param cur  当前选到的下标
     */
    private static void f(int[] shu, int targ, int has, int cur) {
        if (has == targ) {
            System.out.println(stack);
            return;
        }

        for (int i = cur; i < shu.length; i++) {
            if (!stack.contains(shu[i])) {
                stack.add(shu[i]);
                f(shu, targ, has + 1, i);
                stack.pop();
            }
        }
    }
}
