package opt.jmetal.problem.oil.chart.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JfreeGante {
    private static String current = "2019-11-18";

    public static void main(String[] args) {
        // 创建数据集
        IntervalCategoryDataset dataset = createSampleDataset();

        JFreeChart chart = ChartFactory.createGanttChart("任务管理系统",
                "任务各阶段详细实施计划",
                "",
                dataset,
                false,
                false,
                false);
        chart.getTitle().setFont(new Font("宋体", Font.BOLD, 20));

        CategoryPlot plot = chart.getCategoryPlot();
        //图片背景色
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        // 水平轴
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("新宋体", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("新宋体", Font.BOLD, 12));

        // 垂直轴
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("新宋体", Font.BOLD, 16));

        // 控制时间轴标签的显示
        DateAxis da = (DateAxis) plot.getRangeAxis(0);
        da.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        // 窗口
        JFrame frame = new JFrame();
        ChartPanel chartpanel = new ChartPanel(chart);
        frame.add(chartpanel);

        // 设置窗口大小为最佳大小
        frame.pack();
        frame.setVisible(true);

        // 关闭窗口的行为设置为退出应用程序
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * 时间相加
     *
     * @param h
     * @return
     */
    private static Date date(double h) {
        Calendar calendar = Calendar.getInstance();
        Date result = null;

        try {
            // 解析当前时间
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(current);
            calendar.setTime(date);

            // 计算时间
            int hour = (int) (h / 24.0);
            int minute = (int) ((h / 24.0 - hour) * 60);//精确到分钟

            // 日期加法
            calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + hour);
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + minute);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar.getTime();
    }

    /**
     * 创建数据集
     *
     * @return The dataset.
     */
    private static IntervalCategoryDataset createSampleDataset() {

        TaskSeries s1 = new TaskSeries("SCHEDULE");

        Task t1 = new Task("任务1", date(0), date(5));
        // t1.setPercentComplete(0.8);// 完成率
        s1.add(t1);

        // 创建一个任务并插入两个子任务
        Task t3 = new Task("任务2", date(10), date(51));
        Task st31 = new Task("需求1", date(10), date(25));
        Task st32 = new Task("需求2", date(22), date(55));
        t3.addSubtask(st31);
        t3.addSubtask(st32);
        s1.add(t3);

        Task t5 = new Task("任务3", date(2), date(3));
        s1.add(t5);

        Task t6 = new Task("任务4", date(3), date(31));
        s1.add(t6);

        TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s1);

        return collection;
    }
}
