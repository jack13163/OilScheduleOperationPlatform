package com.sim.ui.multicombobox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;

public class MultiComboBox extends JComponent {
	private static final long serialVersionUID = 1L;

	private List<KeyValuePair> values;
	public List<KeyValuePair> defaultValues;

	private List<ActionListener> listeners = new ArrayList<ActionListener>();

	private MultiPopup popup;
	private JTextField editor;

	private String valueSperator;
	private static final String DEFAULT_VALUE_SPERATOR = ",";

	public MultiComboBox(List<KeyValuePair> value, List<KeyValuePair> defaultValue) {
		values = value;
		defaultValues = defaultValue;
		this.valueSperator = DEFAULT_VALUE_SPERATOR;
		initComponent();
	}

	@Override
	public void setEnabled(boolean enabled) {
		editor.setEnabled(enabled);
	}

	/**
	 * 初始化组件
	 */
	private void initComponent() {
		// 设置边界布局，即东西南北中布局
		BorderLayout borderayout = new BorderLayout();
		this.setLayout(borderayout);

		// 创建下拉列表
		popup = new MultiPopup(values, defaultValues);
		popup.addActionListener(new PopupAction());

		// 创建JTextField，并绑定鼠标点击事件
		editor = new JTextField();
		editor.setEditable(false);
		editor.setColumns(10);
		editor.setPreferredSize(new Dimension(140, 20));// 默认长度，不存在布局的情况下
		editor.addMouseListener(new EditorHandler());// 设置JTextField的鼠标事件
		add(editor);

		// 设置默认值
		setText();
	}

	public List<KeyValuePair> getSelectedValues() {
		return popup.getSelectedValues();
	}

	/**
	 * 添加事件监听器
	 * 
	 * @param listener
	 */
	public void addActionListener(ActionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * 移除事件监听器
	 * 
	 * @param listener
	 */
	public void removeActionListener(ActionListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	/**
	 * 事件触发，通知事件监听器
	 * 
	 * @param e
	 */
	protected void fireActionPerformed(ActionEvent e) {
		for (ActionListener l : listeners) {
			l.actionPerformed(e);
		}
	}

	/**
	 * 展开下拉列表事件
	 * 
	 * @author Administrator
	 *
	 */
	private class PopupAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals(MultiPopup.CANCEL_EVENT)) {

			} else if (e.getActionCommand().equals(MultiPopup.COMMIT_EVENT)) {
				defaultValues = popup.getSelectedValues();
				setText();
				// 把事件继续传递出去
				fireActionPerformed(e);
			}

			// 因为当前下拉列表处于开启状态，因此，此时调用该方法的目的是使下拉列表关闭
			togglePopup();
		}

	}

	/**
	 * 返回下拉文本框中的值
	 * 
	 * @return
	 */
	public String getText() {
		return editor.getText();
	}

	/**
	 * 展开下拉列表
	 */
	private void togglePopup() {
		if (popup.isVisible()) {
			popup.setVisible(false);
		} else {
			popup.setDefaultValue(defaultValues);
			popup.show(this, 0, getHeight());
		}
	}

	/**
	 * 设置文本框中的内容
	 */
	private void setText() {
		StringBuilder builder = new StringBuilder();
		for (KeyValuePair dv : defaultValues) {
			builder.append(dv);
			builder.append(valueSperator);
		}

		editor.setText(builder.substring(0, builder.length() > 0 ? builder.length() - 1 : 0).toString());
	}

	/**
	 * 设置鼠标监听器
	 * 
	 * @author Administrator
	 */
	private class EditorHandler implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			togglePopup();
		}

		public void mousePressed(MouseEvent e) {

		}

		public void mouseReleased(MouseEvent e) {

		}

		public void mouseEntered(MouseEvent e) {

		}

		public void mouseExited(MouseEvent e) {

		}
	}
}
