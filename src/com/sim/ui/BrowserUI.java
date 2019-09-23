package com.sim.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * @author lijunming
 * @date 2018/7/22 下午6:00
 */
public class BrowserUI extends JPanel {

	private JPanel webBrowserPanel;

	private JWebBrowser webBrowser;

	public BrowserUI(String url) {
		super(new BorderLayout());
		webBrowserPanel = new JPanel(new BorderLayout());
		webBrowser = new JWebBrowser();
		webBrowser.navigate(url);
		webBrowser.setButtonBarVisible(false);
		webBrowser.setMenuBarVisible(false);
		webBrowser.setBarsVisible(false);
		webBrowser.setStatusBarVisible(false);
		webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
		add(webBrowserPanel, BorderLayout.CENTER);
		// 执行Js代码
		webBrowser.executeJavascript("alert('hello swing')");
	}

	/**
	 * 在swing里内嵌浏览器
	 * 
	 * @param url   要访问的url
	 * @param title 窗体的标题
	 */
	public static void openForm(String url, String title) {
		UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(title);
				// 设置窗体关闭的时候不关闭应用程序
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.getContentPane().add(new BrowserUI(url), BorderLayout.CENTER);
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				frame.setLocationByPlatform(true);
				// 让窗体可见
				frame.setVisible(true);
				// 重置窗体大小
				frame.setResizable(true);
				// 设置窗体的宽度、高度
				frame.setSize(1400, 700);
				// 设置窗体居中显示
				frame.setLocationRelativeTo(frame.getOwner());
			}
		});
		NativeInterface.runEventPump();
	}

	public static void main(String[] args) {
		openForm("C:\\Users\\Administrator\\Desktop\\html\\tmp.html", "hello swing");
	}
}