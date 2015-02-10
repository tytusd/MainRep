package boswell.peakfinderlc;

import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.WindowConstants;
import java.awt.Dimension;

public class ProgressDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	public JProgressBar jProgressBar = null;
	public JLabel jlblProgressCaption = null;
	public JPanel jContentPane = null;
	private JButton jbtnCancel = null;
	
	public boolean m_bCancel = false;
	
	/**
	 * @param owner
	 */
	public ProgressDialog(JDialog owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(351, 131);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.setModal(true);
		this.setTitle("Please wait...");
		this.setContentPane(getJContentPane());
		jbtnCancel.addActionListener(this);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jlblProgressCaption = new JLabel();
			jlblProgressCaption.setBounds(new Rectangle(16, 12, 313, 16));
			jlblProgressCaption.setText("Loading sdfs.mzXML...");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJProgressBar(), null);
			jContentPane.add(jlblProgressCaption, null);
			jContentPane.add(getJbtnCancel(), null);
		}
		return jContentPane;
	}	

	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setBounds(new Rectangle(16, 32, 313, 25));
			jProgressBar.setStringPainted(false);
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jbtnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnCancel() {
		if (jbtnCancel == null) {
			jbtnCancel = new JButton();
			jbtnCancel.setBounds(new Rectangle(100, 60, 145, 36));
			jbtnCancel.setText("Cancel");
		}
		return jbtnCancel;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getSource() == jbtnCancel)
		{
			this.m_bCancel = true;
			this.jbtnCancel.setEnabled(false);
		}
	}

}  //  @jve:decl-index=0:visual-constraint="12,28"
