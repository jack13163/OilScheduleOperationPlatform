package com.sim.ui.multicombobox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

public class MultiPopup extends JPopupMenu {
    private static final long serialVersionUID = 1L;

    private List<ActionListener> listeners = new ArrayList<ActionListener>();

    private List<KeyValuePair> values;
    private List<KeyValuePair> defaultValues;

    private List<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
    private JButton commitButton;
    private JButton cancelButton;

    public static final String COMMIT_EVENT = "commit";
    public static final String CANCEL_EVENT = "cancel";

    public MultiPopup(List<KeyValuePair> value, List<KeyValuePair> defaultValue) {
        super();
        values = value;
        defaultValues = defaultValue;
        initComponent();
    }

    public void addActionListener(ActionListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        if (listeners.contains(listener))
            listeners.remove(listener);
    }

    /**
     * 创建下拉复选框列表
     */
    private void initComponent() {
        // 设置当前布局为边界布局，即东西南北中
        this.setLayout(new BorderLayout());

        // 创建CheckBox列表
        for (KeyValuePair v : values) {
            JCheckBox temp = new JCheckBox(v.toString(), selected(v));
            checkBoxList.add(temp);
        }

        // 创建下拉复选框列表面板
        int height = 200;
        int width = 180;
        if (checkBoxList.size() > 6) {
            height = 200;
        } else {
            height = checkBoxList.size() * 31;
        }
        JPanel checkboxOutPane = new JPanel();
        checkboxOutPane.setPreferredSize(new Dimension(width, height));
        checkboxOutPane.setLayout(null);
        JPanel checkboxPane = new JPanel();// 下拉复选框列表面板
        for (JCheckBox box : checkBoxList) {
            checkboxPane.add(box);
        }
        JScrollPane scrollPane = new JScrollPane(checkboxPane);// 为列表添加滚动条
        checkboxPane.setPreferredSize(new Dimension(width, checkBoxList.size() * 25));
        checkboxPane.setLayout(new GridLayout(checkBoxList.size(), 1, 0, 3));
        scrollPane.setBounds(0, 0, width + 5, height);
        checkboxOutPane.add(scrollPane);
        this.add(checkboxOutPane, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPane = new JPanel();
        // 添加确定按钮事件
        commitButton = new JButton("ok");
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                commit();
            }
        });
        // 添加取消按钮事件
        cancelButton = new JButton("cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        buttonPane.setSize(new Dimension(width, 10));
        buttonPane.add(commitButton);
        buttonPane.add(cancelButton);
        this.add(buttonPane, BorderLayout.SOUTH);
    }

    /**
     * 根据默认值判断是否选中
     *
     * @param v
     * @return
     */
    private boolean selected(KeyValuePair v) {
        for (KeyValuePair dv : defaultValues) {
            if (dv.getName().equals(v.getName())) {
                return true;
            }
        }
        return false;
    }

    protected void fireActionPerformed(ActionEvent e) {
        for (ActionListener l : listeners) {
            l.actionPerformed(e);
        }
    }

    public List<KeyValuePair> getSelectedValues() {
        List<KeyValuePair> selectedValues = new ArrayList<KeyValuePair>();

        for (int i = 0; i < checkBoxList.size(); i++) {

            if (checkBoxList.get(i).isSelected())
                selectedValues.add(values.get(i));
        }

        return selectedValues;
    }

    public void setDefaultValue(List<KeyValuePair> defaultValue) {
        defaultValues = defaultValue;
    }

    public void commit() {
        fireActionPerformed(new ActionEvent(this, 0, COMMIT_EVENT));
    }

    public void cancel() {
        fireActionPerformed(new ActionEvent(this, 0, CANCEL_EVENT));
    }
}
