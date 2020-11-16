package opt.easyjmetal.problem.onlinemix.gante;

import java.util.List;

/**
 * 绘制甘特图的工具类
 */
public class PlotUtils {
    private static ChartFrame chartFrame = null;

    public static boolean showDetail = false;

    /**
     * 绘制调度的甘特图
     * <p>
     * 格式：蒸馏塔/管道 油罐 开始时间 结束时间 原油类型
     *
     * @param operations
     */
    public static void plotSchedule2(List<List<Double>> operations) {

        try {
            int size = operations.size();
            double[][] data = new double[size][6];

            if(showDetail) {
                System.out.println("-------------------------------------详细调度-------------------------------------------");
            }
            for (int i = 0; i < operations.size(); i++) {

                // 区分转运和炼油
                if (operations.get(i).get(0) == 4.0) {
                    data[i][0] = 4.0;
                } else {
                    data[i][0] = operations.get(i).get(0);
                }

                data[i][1] = operations.get(i).get(1);
                data[i][2] = operations.get(i).get(2);
                data[i][3] = operations.get(i).get(3);
                data[i][4] = operations.get(i).get(4);

                // 第二个供油罐
                if (operations.get(i).size() > 5) {
                    data[i][5] = operations.get(i).get(5);
                } else {
                    data[i][5] = -1;
                }

                if(showDetail) {
                    System.out.println(data[i][0] + "," + data[i][1] + "," + data[i][2] + "," + data[i][3] + "," + data[i][4] + (data[i][5] < 0 ? "" : "," + data[i][5]));
                }
            }

            if (chartFrame == null) {
                chartFrame = new ChartFrame();
            } else if (!chartFrame.frame.isVisible()) {
                chartFrame.frame.setVisible(true);
            }
            chartFrame.updateCanvas(data);
        } catch (Exception e) {
            System.out.println("Java Canvas plot module error.");
            e.printStackTrace();
            System.exit(1);
        }

        // 为了防止刷新过快，让线程休眠一段时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}