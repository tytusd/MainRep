package dialogs;

import javax.swing.JPanel;
import java.awt.Frame;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import java.awt.Rectangle;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;
import java.awt.Dimension;
import java.lang.String;
import javax.swing.JCheckBox;
import java.awt.Point;
import java.awt.GridBagLayout;

public class DetectorPropertiesDialog extends JDialog implements ActionListener, KeyListener, FocusListener
{
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel = null;
	private JTextField jtxtName = null;
	public boolean m_bOk = false;
	
	public String m_strName = "Default detector";
	
	private JTextField m_ControlChanged = null;
	public void setName(String strNewName)
	{
		this.jtxtName.setText(strNewName);
        performCalculations();
	}

	public String getName()
	{
		return this.m_strName;
	}
	
    private void validateName()
    {
    	if (this.jtxtName.getText() == null)
    		this.jtxtName.setText("Default detector");

		this.m_strName = this.jtxtName.getText();
		this.jtxtName.setText(m_strName);    	
    }   

	/**
	 * @param owner
	 */
	public DetectorPropertiesDialog(Frame owner) 
	{
		super(owner);
		initialize();
		
		jbtnOk.addActionListener(this);
		jbtnCancel.addActionListener(this);
        jtxtName.addKeyListener(this);
        jtxtName.addFocusListener(this);
        
        performCalculations();

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(284, 110);
		this.setContentPane(getJContentPane());
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("Detector Properties");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jLabel.setText("Name:");
			jLabel.setLocation(new Point(12, 10));
			jLabel.setSize(new Dimension(48, 16));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBackground(new Color(238, 238, 238));
			jContentPane.add(getJbtnOk(), null);
			jContentPane.add(getJbtnCancel(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getJtxtName(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jbtnOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnOk() {
		if (jbtnOk == null) {
			jbtnOk = new JButton();
			jbtnOk.setBounds(new Rectangle(8, 40, 125, 31));
			jbtnOk.setActionCommand("OK");
			jbtnOk.setText("OK");
		}
		return jbtnOk;
	}

	/**
	 * This method initializes jbtnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnCancel() {
		if (jbtnCancel == null) {
			jbtnCancel = new JButton();
			jbtnCancel.setBounds(new Rectangle(144, 40, 125, 31));
			jbtnCancel.setText("Cancel");
		}
		return jbtnCancel;
	}

	//@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getActionCommand() == "OK")
		{
			m_bOk = true;
			//this.m_iCondition = this.jcboTemperatureProgram.getSelectedIndex();
			this.setVisible(false);
			this.dispose(); 
		}
		else if (arg0.getActionCommand() == "Cancel")
		{
			this.setVisible(false);
			this.dispose(); 			
		}
	}

	/**
	 * This method initializes jtxtInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtName() {
		if (jtxtName == null) {
			jtxtName = new JTextField();
			jtxtName.setBounds(new Rectangle(92, 9, 177, 20));
			jtxtName.setHorizontalAlignment(JTextField.LEADING);
			jtxtName.setText("Default autosampler");
		}
		return jtxtName;
	}

	public void performCalculations()
	{
		validateName();

		this.m_ControlChanged = null;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) 
	{
		/*if (!((Character.isDigit(e.getKeyChar()) ||
				(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
				(e.getKeyChar() == KeyEvent.VK_DELETE) ||
				(e.getKeyChar() == KeyEvent.VK_PERIOD))))
		{
	        e.consume();
		}*/
		
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
		{
			this.m_ControlChanged = (JTextField)e.getComponent();
			performCalculations();
		}
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent e) 
	{
		this.m_ControlChanged = (JTextField)e.getComponent();
		performCalculations();	
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
