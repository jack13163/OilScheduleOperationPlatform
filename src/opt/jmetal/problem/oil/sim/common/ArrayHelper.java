package opt.jmetal.problem.oil.sim.common;

public class ArrayHelper {
    /**
     * 排序并返回对应原始数组的下标
     *
     * @param arr
     * @param desc
     * @return
     */
    public static int[] Arraysort(double[] arr, boolean desc) {
        double temp;
        int index;
        int k = arr.length;
        int[] Index = new int[k];
        for (int i = 0; i < k; i++) {
            Index[i] = i;
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (desc) {
                    if (arr[j] < arr[j + 1]) {
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                } else {
                    if (arr[j] > arr[j + 1]) {
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                }
            }
        }
        return Index;
    }

    /**
     * 排序并返回对应原始数组的下标【默认升序】
     *
     * @param arr
     * @return
     */
    public static int[] Arraysort(double[] arr) {
        return Arraysort(arr, false);
    }

    /**
     * 返回指定元素所在的索引位置
     *
     * @param arr
     * @param tofind
     * @return
     */
    public static int indexOf(int[] arr, int tofind) {
        int result = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == tofind) {
                result = i;
                break;
            }
        }
        return result;
    }
}
