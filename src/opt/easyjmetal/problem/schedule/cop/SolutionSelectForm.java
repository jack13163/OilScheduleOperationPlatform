package opt.easyjmetal.problem.schedule.cop;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.fileinput.ParetoFrontUtil;
import opt.javasim.SimulationProcess;
import opt.jmetal.problem.oil.sim.common.JTableHelper;
import opt.jmetal.problem.oil.sim.common.ParetoHelper;
import opt.jmetal.problem.oil.sim.oil.Config;
import opt.jmetal.problem.oil.sim.ui.multicombobox.KeyValuePair;
import opt.jmetal.problem.oil.sim.ui.multicombobox.MultiComboBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SolutionSelectForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(SolutionSelectForm.class.getName());

    private static final String basePath_ = "result/easyjmetal/twopipeline";

    // UIƤ��
    private static final String[] themes = {"com.jtattoo.plaf.smart.SmartLookAndFeel",
            "com.jtattoo.plaf.mcwin.McWinLookAndFeel", "com.jtattoo.plaf.luna.LunaLookAndFeel",
            "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel",
            "com.jtattoo.plaf.hifi.HiFiLookAndFeel", "com.jtattoo.plaf.mint.MintLookAndFeel",
            "com.jtattoo.plaf.aero.AeroLookAndFeel", "com.jtattoo.plaf.fast.FastLookAndFeel",
            "com.jtattoo.plaf.acryl.AcrylLookAndFeel"};

    // ����UI
    private JMenuBar menubar; // �˵���
    private JMenu menuSystem; // �˵�
    private JMenuItem itemTop, itemExit; // �˵���
    private Box VMainPanel;// ������
    private JTabbedPane tabbedPane;// ѡ�
    private boolean onTop = false;// �Ƿ�����ǰ

    // ���в���UI
    private MultiComboBox cbAlgorithmsForExperiment;
    private MultiComboBox cbProblemsForExperiment;
    private JTextField txtRuns;
    public JButton btnStartForExperiment;

    public static void main(String[] args) {

        // ָ��log4j2.xml�ļ���λ��
        ConfigurationSource source;
        String relativePath = "log4j2.xml";
        File log4jFile = new File(relativePath);
        try {
            if (log4jFile.exists()) {
                source = new ConfigurationSource(new FileInputStream(log4jFile), log4jFile);
                Configurator.initialize(null, source);
            } else {
                System.out.println("loginit failed");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        // ��ʼ��GUI���棬����Ȩ�����û�
        new SolutionSelectForm("���ӻ���֧��⼯");
    }

    /**
     * ����UI���
     *
     * @param style
     */
    private void changeUIStyle(String style) {

        try {
            UIManager.setLookAndFeel(style);// ����������
            SwingUtilities.updateComponentTreeUI(this);// ����UI����
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SolutionSelectForm(String s) {
        // ����Ĭ��UI���
        changeUIStyle(themes[4]);
        setIconImage(new ImageIcon("img/plan.png").getImage());

        // �رմ����¼�
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(null, "ȷ���˳�?", "ȷ��", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    // �ر��̳߳�
                    SimulationProcess.cachedThreadPool.shutdown();
                    System.exit(0);
                }
            }
        });
        // ���ô����С������
        setWindowSizeAndCenter(this, 1200, 800);
        setTitle(s);
        menubar = new JMenuBar();
        menuSystem = new JMenu("ϵͳ(S)");
        menuSystem.setMnemonic('S'); // ���ò˵��ļ��̲�����ʽ��Alt + S��
        itemTop = new JMenuItem("��ǰ(T)");

        itemTop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlwaysOnTop(!onTop);
                onTop = !onTop;
                if (onTop) {
                    itemTop.setText("ȡ����ǰ(T)");
                } else {
                    itemTop.setText("��ǰ(T)");
                }
            }
        });
        itemExit = new JMenuItem("�˳�(E)");
        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // ���ò˵���ļ��̲�����ʽ��Ctrl+T��Ctrl+E��
        KeyStroke Ctrl_cutKey = KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK);
        itemTop.setAccelerator(Ctrl_cutKey);
        Ctrl_cutKey = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK);
        itemExit.setAccelerator(Ctrl_cutKey);

        menuSystem.add(itemTop);
        menuSystem.add(itemExit);
        menubar.add(menuSystem); // ���˵���ӵ��˵�����
        setJMenuBar(menubar);

        JMenu menuUIStyle = new JMenu("����");
        menuUIStyle.setMnemonic('S');
        menubar.add(menuUIStyle);

        JMenuItem menuItemStyle1 = new JMenuItem("���1");
        menuItemStyle1.addActionListener((e) -> {
            changeUIStyle(themes[0]);
        });
        menuUIStyle.add(menuItemStyle1);

        JMenuItem menuItemStyle2 = new JMenuItem("���2");
        menuItemStyle2.addActionListener((e) -> {
            changeUIStyle(themes[1]);
        });
        menuUIStyle.add(menuItemStyle2);

        JMenuItem menuItemStyle3 = new JMenuItem("���3");
        menuItemStyle3.addActionListener((e) -> {
            changeUIStyle(themes[2]);
        });
        menuUIStyle.add(menuItemStyle3);

        JMenuItem menuItemStyle4 = new JMenuItem("���4");
        menuItemStyle4.addActionListener((e) -> {
            changeUIStyle(themes[3]);
        });
        menuUIStyle.add(menuItemStyle4);

        JMenuItem menuItemStyle5 = new JMenuItem("���5");
        menuItemStyle5.addActionListener((e) -> {
            changeUIStyle(themes[4]);
        });
        menuUIStyle.add(menuItemStyle5);

        // tab��ǩ��
        VMainPanel = Box.createVerticalBox();
        getContentPane().add(VMainPanel, BorderLayout.CENTER);
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Color.WHITE);
        VMainPanel.add(tabbedPane);
        tabbedPane.addTab("����ʵ����", createExperimentUI());
        tabbedPane.setSelectedIndex(0);// ����Ĭ��ѡ�е�ѡ�

        Box HBox12 = Box.createHorizontalBox();
        VMainPanel.add(HBox12);

        setVisible(true);
    }

    /**
     * ����ʵ��ģ�����
     */
    private JComponent createExperimentUI() {
        // ����һ����ֱ�ĺ���
        Box box = Box.createVerticalBox();

        Box HBox5 = Box.createHorizontalBox();
        JLabel lblIndex = new JLabel("�о����⣺");
        HBox5.add(lblIndex);
        List<KeyValuePair> problemList = new LinkedList<>();
        problemList.add(new KeyValuePair("EDF_PS", "EDF_PS"));
        problemList.add(new KeyValuePair("EDF_TSS", "EDF_TSS"));
        List<KeyValuePair> defaultValue1 = new LinkedList<>();
        defaultValue1.add(new KeyValuePair("EDF_PS", "EDF_PS"));
        defaultValue1.add(new KeyValuePair("EDF_TSS", "EDF_TSS"));
        cbProblemsForExperiment = new MultiComboBox(problemList, defaultValue1);
        HBox5.add(cbProblemsForExperiment);
        box.add(HBox5);

        // �����㷨��ѡ��
        Box HBox4 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("�㷨��        ");
        HBox4.add(lblSu2);
        List<KeyValuePair> algorithmList = new LinkedList<>();
        algorithmList.add(new KeyValuePair("NSGAII_CDP", "NSGAII_CDP"));
        algorithmList.add(new KeyValuePair("ISDEPLUS_CDP", "ISDEPLUS_CDP"));
        algorithmList.add(new KeyValuePair("NSGAIII_CDP", "NSGAIII_CDP"));
        algorithmList.add(new KeyValuePair("MOEAD_CDP", "MOEAD_CDP"));
        algorithmList.add(new KeyValuePair("MOEAD_IEpsilon", "MOEAD_IEpsilon"));
        algorithmList.add(new KeyValuePair("MOEAD_Epsilon", "MOEAD_Epsilon"));
        algorithmList.add(new KeyValuePair("MOEAD_SR", "MOEAD_SR"));
        algorithmList.add(new KeyValuePair("C_MOEAD", "C_MOEAD"));
        algorithmList.add(new KeyValuePair("PPS_MOEAD", "PPS_MOEAD"));
        List<KeyValuePair> defaultValue2 = new LinkedList<>();
        defaultValue2.add(new KeyValuePair("NSGAII_CDP", "NSGAII_CDP"));
        defaultValue2.add(new KeyValuePair("ISDEPLUS_CDP", "ISDEPLUS_CDP"));
        defaultValue2.add(new KeyValuePair("NSGAIII_CDP", "NSGAIII_CDP"));
        defaultValue2.add(new KeyValuePair("MOEAD_CDP", "MOEAD_CDP"));
        defaultValue2.add(new KeyValuePair("MOEAD_IEpsilon", "MOEAD_IEpsilon"));
        defaultValue2.add(new KeyValuePair("MOEAD_Epsilon", "MOEAD_Epsilon"));
        defaultValue2.add(new KeyValuePair("MOEAD_SR", "MOEAD_SR"));
        defaultValue2.add(new KeyValuePair("C_MOEAD", "C_MOEAD"));
        defaultValue2.add(new KeyValuePair("PPS_MOEAD", "PPS_MOEAD"));
        cbAlgorithmsForExperiment = new MultiComboBox(algorithmList, defaultValue2);
        HBox4.add(cbAlgorithmsForExperiment);
        // ��ӱ�ǩ�����
        box.add(HBox4);

        // ���д���
        Box HBox3 = Box.createHorizontalBox();
        JLabel label = new JLabel("���д�����");
        HBox3.add(label);
        txtRuns = new JTextField();
        txtRuns.setText("10");
        txtRuns.setColumns(10);
        HBox3.add(txtRuns);
        box.add(HBox3);

        // ����ָ����
        Box HBox8 = Box.createHorizontalBox();
        final JTable resultTable = new JTable();
        JTableHelper.setTableColumnCenter(resultTable);
        resultTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // ������Σ�������˫���¼�
                if (e.getClickCount() == 2) {
                    // ���ҳ�ָ���Ľ�
                    double[][] tofind = new double[1][5];
                    int row = resultTable.getSelectedRow();
                    // ��ȡ��Ӧ�ļ�¼
                    for (int i = 0; i < 5; i++) {
                        tofind[0][i] = Double.parseDouble(resultTable.getValueAt(row, i + 1).toString());
                    }

                    try {
                        ParetoFrontUtil.getSolutionFromDB(cbAlgorithmsForExperiment.getText().split(","),
                                cbProblemsForExperiment.getText().split(","),
                                Integer.parseInt(txtRuns.getText()),
                                tofind,
                                new ParetoFrontUtil.ToDo() {
                                    @Override
                                    public void dosomething(Solution solution, String rule) {
                                        // ����λ��
                                        COPDecoder.decode(solution, rule, true);
                                    }
                                }, basePath_);
                    } catch (JMException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        JTableHelper.setTableColumnCenter(resultTable);
        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setPreferredSize(new Dimension(100, 600));
        HBox8.add(scroll);
        box.add(HBox8);

        Box HBox10 = Box.createHorizontalBox();
        btnStartForExperiment = new JButton("��ʼʵ��");
        btnStartForExperiment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ��ȡ�û�����
                final int runtimes = Integer.parseInt(txtRuns.getText());
                final String[] algorithmNames = cbAlgorithmsForExperiment.getText().split(",");
                final String[] problemNames = cbProblemsForExperiment.getText().split(",");

                // �����µ��߳�����ʵ�顾��ֹUI�߳�������
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String basePath = "result/easyjmetal/twopipeline/";
                            // ����paretoǰ����
                            String path = ParetoFrontUtil.generateOilScheduleParetoFront(algorithmNames, problemNames, runtimes, basePath);
                            // �������Ľ�
                            paintNodominanceSolution(resultTable, path);
                            // �Ƚ������ܵ����ܺġ��������ݡ�
                            // compareEnergyConsumptionTwoPipeline(path, algorithmNames, problemNames, runtimes);
                        } catch (Exception e) {
                            logger.fatal(e.getMessage());
                        }
                    }
                });

                mainThread.setName("Experiment");
                mainThread.setDaemon(true);// ��Ϊ��̨�ػ���������
                mainThread.start();
            }
        });
        HBox10.add(btnStartForExperiment);
        box.add(HBox10);

        return box;
    }

    /**
     * ��ȡ���н������������ʾ��֧��⡾Ŀ�꺯��ֵ��
     *
     * @param resultTable
     * @param filePath
     * @throws IOException
     */
    protected void paintNodominanceSolution(JTable resultTable, String filePath) throws IOException {
        // ׼������
        final DefaultTableModel mm = JTableHelper.showTableWithNo(filePath, false);

        // ����ָ������
        String[] columnNames = {"���", "�ܺĳɱ�", "�ܵ���ϳɱ�", "�޵׻�ϳɱ�", "�л�����", "�ù޸���"};
        mm.setColumnIdentifiers(columnNames);

        // ��Ҫ��UI�߳�����²���UI������SwingUtilities���ҵ�UI�̲߳�ִ�и���UI����
        SwingUtilities.invokeLater(() -> {
            resultTable.setModel(mm);
            boolean[] flags = new boolean[mm.getRowCount()];
            for (int i = 0; i < mm.getRowCount(); i++) {
                Map<String, Double> referenceCost = Config.getInstance().loadConfig().referenceCost;
                Map<String, Double> resultCost = new HashMap<String, Double>();
                resultCost.put("energyCost", Double.parseDouble(mm.getValueAt(i, 1).toString()));
                resultCost.put("pipeMix", Double.parseDouble(mm.getValueAt(i, 2).toString()));
                resultCost.put("tankMix", Double.parseDouble(mm.getValueAt(i, 3).toString()));
                resultCost.put("chargeTime", Double.parseDouble(mm.getValueAt(i, 4).toString()));
                resultCost.put("tankUsed", Double.parseDouble(mm.getValueAt(i, 5).toString()));

                // ���֧���
                flags[i] = ParetoHelper.dominanceComparison(referenceCost, resultCost);
            }

            // ͻ����ʾ��֧���
            JTableHelper.setRowsColor(resultTable, flags);
        });
    }

    /**
     * �Ƚ������ܵ����ܺ�
     *
     * @param paretoFilePath
     * @param algorithmNames
     * @param problemNames
     * @param runtimes
     */
    protected void compareEnergyConsumptionTwoPipeline(String paretoFilePath,
                                                       String[] algorithmNames,
                                                       String[] problemNames,
                                                       int runtimes) throws IOException {
        // ׼������
        final DefaultTableModel mm = JTableHelper.showTableWithNo(paretoFilePath, false);

        // ��Ҫ��UI�߳�����²���UI������SwingUtilities���ҵ�UI�̲߳�ִ�и���UI����
        SwingUtilities.invokeLater(() -> {
            // ����ܵ�ת���ܺĶԱ�
            String basePath = "result/easyjmetal/";
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //true = append file
            FileWriter fileWritter = null;
            try {
                fileWritter = new FileWriter(basePath + "transportationEnergyConsumption.csv", false);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("hardCost, pipeOneEC, pipeTwoEC, totalEC\n");
                for (int i = 0; i < mm.getRowCount(); i++) {
                    double energyCost = Double.parseDouble(mm.getValueAt(i, 1).toString());
                    double pipeMix = Double.parseDouble(mm.getValueAt(i, 2).toString());
                    double tankMix = Double.parseDouble(mm.getValueAt(i, 3).toString());
                    double chargeTime = Double.parseDouble(mm.getValueAt(i, 4).toString());
                    double tankUsed = Double.parseDouble(mm.getValueAt(i, 5).toString());

                    // ���ҳ�ָ���Ľ�
                    double[][] tofind = new double[][]{{energyCost, pipeMix, tankMix, chargeTime, tankUsed}};
                    try {
                        ParetoFrontUtil.getSolutionFromDB(algorithmNames, problemNames, runtimes, tofind,
                                new ParetoFrontUtil.ToDo() {
                                    @Override
                                    public void dosomething(Solution solution, String rule) {
                                        // {ӲԼ��Υ�����ܵ�1ת���ܺģ��ܵ�2ת���ܺģ���ת���ܺ�}
                                        double[] costs = COPDecoder.decodePipelineEnergyConsumption(solution, rule);
                                        stringBuilder.append(costs[0] + "," + costs[1] + "," + costs[2] + "," + costs[3] + "\n");
                                    }
                                }, basePath_);
                    } catch (JMException e) {
                        e.printStackTrace();
                    }
                }

                fileWritter.write(stringBuilder.toString());
                fileWritter.flush();
                fileWritter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * �Զ������ڷŵ���Ļ���м�
     *
     * @param frame
     * @param width
     * @param height
     */
    public void setWindowSizeAndCenter(JFrame frame, int width, int height) {
        int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
    }
}
