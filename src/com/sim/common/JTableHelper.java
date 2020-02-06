package com.sim.common;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class JTableHelper {

    public static DefaultTableModel showTableWithNo(String filePath, boolean containColumeNames) throws IOException {
        return showTable(filePath, containColumeNames, true);
    }

    public static DefaultTableModel showTableWithnotNo(String filePath, boolean containColumeNames) throws IOException {
        return showTable(filePath, containColumeNames, false);
    }

    public static DefaultTableModel showTable(String filePath, boolean containColumeNames, boolean containNo)
            throws IOException {
        DefaultTableModel mm = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            // 设置单元格不可编辑
            public boolean isCellEditable(int rowIndex, int ColIndex) {
                return false;
            }
        };

        FileReader reader = new FileReader(filePath);
        BufferedReader br = new BufferedReader(reader);
        String line;

        if ((line = br.readLine()) != null) {
            // 列数
            int numOfColumes = 0;
            if (containNo) {
                numOfColumes = line.split(",").length + 1;
            } else {
                numOfColumes = line.split(",").length;
            }

            if (containColumeNames) {
                // 若列名存在，则读取列名
                String[] columnNames = line.split(",");
                if (containNo) {
                    String[] newColumnNames = new String[columnNames.length + 1];
                    newColumnNames[0] = "No";
                    for (int i = 0; i < columnNames.length; i++) {
                        newColumnNames[i + 1] = columnNames[i];
                    }
                    mm.setColumnIdentifiers(newColumnNames);
                } else {
                    mm.setColumnIdentifiers(columnNames);
                }
                // 读取下一行
                line = br.readLine();
            } else {
                // 若列名不存在，则设置临时列名，否则会出错
                if (containNo) {
                    String[] tmpColumnNames = new String[numOfColumes + 1];
                    tmpColumnNames[0] = "No";
                    for (int i = 0; i < numOfColumes; i++) {
                        tmpColumnNames[i + 1] = (i + 1) + "";
                    }
                    mm.setColumnIdentifiers(tmpColumnNames);
                } else {
                    String[] tmpColumnNames = new String[numOfColumes];
                    for (int i = 0; i < numOfColumes; i++) {
                        tmpColumnNames[i] = (i + 1) + "";
                    }
                    mm.setColumnIdentifiers(tmpColumnNames);
                }
            }

            do {
                String data[] = line.split(",");
                Vector<String> v = null;
                if (containNo) {
                    v = new Vector<>(numOfColumes);
                    for (int i = 0; i < numOfColumes; i++) {
                        if (i == 0) {
                            v.add(i, (mm.getDataVector().size() + 1) + "");
                        } else {
                            v.add(i, data[i - 1]);
                        }
                    }
                } else {
                    v = new Vector<>(data.length);
                    for (int i = 0; i < data.length; i++) {
                        v.add(i, data[i]);
                    }
                }
                mm.addRow(v);
            } while ((line = br.readLine()) != null);
        }
        // 关闭读写器
        br.close();
        reader.close();

        return mm;
    }

    /**
     * 设置某些行的颜色
     *
     * @param table 表格
     * @param flags 是否特殊显示
     */
    public static void setRowsColor(JTable table, boolean[] flags) {
        try {
            DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                // 重写getTableCellRendererComponent 方法
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column) {

                    if (flags[row]) {
                        setBackground(Color.RED);
                        setForeground(Color.WHITE);
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                    }

                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            };
            // 对每行的每一个单元格
            int columnCount = table.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                table.getColumn(table.getColumnName(i)).setCellRenderer(dtcr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 表格数据居中
     *
     * @param table
     */
    public static void setTableColumnCenter(JTable table) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, r);
    }

    /**
     * 设置表格的某一行的背景色
     *
     * @param table
     */
    public static void setOneRowBackgroundColor(JTable table, int rowIndex, Color color) {
        try {
            // 获取初始时刻供油罐的状态
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DefaultTableCellRenderer tcr;

                    if (rowIndex != 0) {
                        tcr = new DefaultTableCellRenderer() {
                            private static final long serialVersionUID = 1L;

                            public Component getTableCellRendererComponent(JTable table, Object value,
                                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                                if (row == rowIndex - 1) {
                                    setBackground(color);
                                    setForeground(Color.WHITE);
                                } else if (row > rowIndex - 1) {
                                    setBackground(null);
                                    setForeground(null);
                                } else {
                                    setBackground(null);
                                    setForeground(null);
                                }

                                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                        column);
                            }
                        };
                    } else {
                        tcr = new DefaultTableCellRenderer() {
                            private static final long serialVersionUID = 1L;

                            public Component getTableCellRendererComponent(JTable table, Object value,
                                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                                setBackground(Color.WHITE);
                                setForeground(Color.BLACK);

                                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                                        column);
                            }
                        };
                    }

                    int columnCount = table.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
                    }

                    table.repaint();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 更新swing中的jtable
     *
     * @param table
     * @param mm
     */
    public static void showTableInSwing(JTable table, TableModel mm) {
        // 不要在UI线程外更新操作UI，这里SwingUtilities会找到UI线程并执行更新UI操作
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                table.setModel(mm);
            }
        });
    }
}
