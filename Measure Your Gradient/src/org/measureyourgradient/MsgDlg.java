/*
 * Created on 21 Dec 2006 by Josef Garvi (www.edenfoundation.org)
 * Updated on 8 Dec 2009.
 */
package org.measureyourgradient;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class MsgDlg {

	private static JComponent wrap(String s) {
		return wrap(s, null);
	}

	private static int getContentHeight(String content, int width) {
		JEditorPane dummyEditorPane = new JEditorPane();
		dummyEditorPane.setSize(width, Short.MAX_VALUE);
		dummyEditorPane.setContentType("text/html");
		dummyEditorPane.setText(content);

		return dummyEditorPane.getPreferredSize().height;
	}

	private static JComponent wrap(String message, String shortTitle) {

		int maxWidth = 400;
		int maxHeight = Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3;

		String htmlizedMessage = null;
		if (message.contains("<p>") || message.contains("<P>")
				|| message.contains("<br>") || message.contains("<BR>")
				|| message.contains("<Br>"))
			htmlizedMessage = message;
		else
			htmlizedMessage = message.replace("\n", "<BR>");

		int h = getContentHeight(htmlizedMessage, maxWidth);

		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		JEditorPane text = new JEditorPane("text/html", htmlizedMessage);
		text.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		text.setBorder(null);
		text.setBackground(null);
		text.setEditable(false);

		int titleHeight = 0;
		if (shortTitle != null) {
			JLabel lblTitle = new JLabel(shortTitle);
			//lblTitle.setFont(lblTitle.getFont().deriveFont(lblTitle.getFont().getSize() * 1.5f));
			lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
			lblTitle.setMaximumSize(lblTitle.getPreferredSize());
			titleHeight = lblTitle.getPreferredSize().height;
			pnl.add(lblTitle, BorderLayout.PAGE_START);
		}
		JScrollPane scroll = new JScrollPane(text);
		scroll.setBorder(null);
		scroll.setMaximumSize(new Dimension(maxWidth, maxHeight));
		scroll.setPreferredSize(new Dimension(Math.min(maxWidth, text
				.getPreferredSize().width), Math.min(maxHeight, getContentHeight(
				htmlizedMessage, maxWidth))));

		pnl.add(scroll, BorderLayout.CENTER);
		return pnl;
	}

	public static void error(Component owner, Throwable ex) {
		error(owner, ex, null);
	}

	public static void error(Component owner, Throwable ex, String title) {
		System.out.println("Error: " + ex.getMessage());
		ex.printStackTrace();
		if (ex.getMessage() == null)
			error(owner, ex.getClass().getName().substring(
					ex.getClass().getName().lastIndexOf('.') + 1), title);
		else
			error(owner, ex.getMessage(), title);
	}

	public static void error(Component owner, String message) {
		error(owner, message, null);
	}

	public static void error(Component owner, String message, String title) {
		JOptionPane.showMessageDialog(owner, wrap(message, "Error Encountered:"),
				title == null ? "Error" : title, JOptionPane.ERROR_MESSAGE);
	}

	public static void alert(Component owner, String message) {
		alert(owner, message, null);
	}

	public static void alert(Component owner, String message, String title) {
		JOptionPane.showMessageDialog(owner, wrap(message),
				title == null ? "Warning" : title, JOptionPane.WARNING_MESSAGE);
	}

	public static void information(Component owner, String message) {
		information(owner, message, null);
	}

	public static void information(Component owner, String message, String title) {
		JOptionPane.showMessageDialog(owner, wrap(message),
				title == null ? "Information" : title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void information(Component owner, Object[] messages) {
		generic(owner, JOptionPane.INFORMATION_MESSAGE, "Information", true,
				messages);
	}

	public static int question(Component owner, String message, int answerOptions) {
		return question(owner, message, null, answerOptions);
	}

	public static int question(Component owner, String message, String title, int answerOptions) {
		return JOptionPane.showConfirmDialog(owner, wrap(message),
				title == null ? "Question" : title, answerOptions, JOptionPane.WARNING_MESSAGE/*JOptionPane.QUESTION_MESSAGE*/);
	}

	public static void generic(Component owner, int messageType, String title,
			boolean modal, Object... messages) {
		JOptionPane optionPane = getNarrowOptionPane(50);
		Object[] msgParam = new Object[messages.length];
		for (int i = 0; i < messages.length; i++) {
			if (messages[i] instanceof String) {
				msgParam[i] = wrap((String) messages[i]);
			} else
				msgParam[i] = messages[i];
		}
		optionPane.setMessage(msgParam);
		optionPane.setMessageType(messageType);
		JDialog dialog = optionPane.createDialog(owner, title);
		dialog.setResizable(true);
		dialog.setTitle(title);
		if (!modal) {
			dialog.setModalityType(ModalityType.MODELESS);
			dialog.setAlwaysOnTop(true);
		}
		//FrameExtra.centerOnWindow(FrameExtra.ancestorOfComponent(owner), dialog);
		dialog.setVisible(true);
	}

	private static JOptionPane getNarrowOptionPane(int maxCharactersPerLineCount) {
		// Our inner class definition
		class NarrowOptionPane extends JOptionPane {
			int maxCharactersPerLineCount;

			NarrowOptionPane(int maxCharactersPerLineCount) {
				this.maxCharactersPerLineCount = maxCharactersPerLineCount;
			}

			@Override
			public int getMaxCharactersPerLineCount() {
				return maxCharactersPerLineCount;
			}
		}

		return new NarrowOptionPane(maxCharactersPerLineCount);
	}

	public static void registerDefaultExceptionHandler() {
	   Thread.setDefaultUncaughtExceptionHandler(
	         new Thread.UncaughtExceptionHandler() {
	            public void uncaughtException(Thread t, Throwable e) {
	               error(null, e);
	            }
	         });
	}

}
