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

public class ValvePropertiesDialog extends JDialog implements ActionListener, KeyListener, FocusListener
{
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel = null;
	private JTextField jtxtName = null;
	public boolean m_bOk = false;
	public String m_strName = "Default valve";
	private JTextField m_ControlChanged = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JTextField jtxtStatorHoleDiameter = null;
	private JTextField jtxtStatorHoleLength = null;
	private JLabel jLabel7 = null;
	private JTextField jtxtPorts = null;

	private double m_dStatorHoleDiameter = 2;
	private double m_dStatorHoleLength = 5;
	private double m_dGrooveDiameter = 2;
	private double m_dGrooveLength = 1;
	
	private int m_iPorts = 6;
	private JLabel jLabel5 = null;
	private JLabel jLabel51 = null;
	private JLabel jLabel52 = null;
	private JTextField jtxtGrooveDiameter = null;
	private JLabel jLabel31 = null;
	private JTextField jtxtGrooveLength = null;
	private JLabel jLabel41 = null;

	public void setStatorHoleDiameter(double dStatorHoleDiameter)
	{
		this.jtxtStatorHoleDiameter.setText(Float.toString((float)dStatorHoleDiameter));
		performCalculations();
	}
	
	public double getStatorHoleDiameter()
	{
		return m_dStatorHoleDiameter;
	}
	
	public void setStatorHoleLength(double dStatorHoleLength)
	{
		this.jtxtStatorHoleLength.setText(Float.toString((float)dStatorHoleLength));
		performCalculations();
	}
	
	public double getStatorHoleLength()
	{
		return m_dStatorHoleLength;
	}

	public void setGrooveDiameter(double dGrooveDiameter)
	{
		this.jtxtGrooveDiameter.setText(Float.toString((float)dGrooveDiameter));
		performCalculations();
	}
	
	public double getGrooveDiameter()
	{
		return m_dGrooveDiameter;
	}
	
	public void setGrooveLength(double dGrooveLength)
	{
		this.jtxtGrooveLength.setText(Float.toString((float)dGrooveLength));
		performCalculations();
	}
	
	public double getGrooveLength()
	{
		return m_dGrooveLength;
	}
	
	public void setNumPorts(int iNumPorts)
	{
		this.jtxtPorts.setText(Integer.toString(iNumPorts));
		this.m_iPorts = iNumPorts;
	}
	
	public int getNumPorts()
	{
		return m_iPorts;
	}
	
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
    		this.jtxtName.setText("Default autosampler");

		this.m_strName = this.jtxtName.getText();
		this.jtxtName.setText(m_strName);    	
    }
    
    private void validateStatorHoleLength()
    {
    	if (this.jtxtStatorHoleLength.getText() == null)
    		this.jtxtStatorHoleLength.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtStatorHoleLength.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dStatorHoleLength = dTemp;
		this.jtxtStatorHoleLength.setText(Float.toString((float)m_dStatorHoleLength));    	
    }   

    private void validateStatorHoleDiameter()
    {
    	if (this.jtxtStatorHoleDiameter.getText() == null)
    		this.jtxtStatorHoleDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtStatorHoleDiameter.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dStatorHoleDiameter = dTemp;
		this.jtxtStatorHoleDiameter.setText(Float.toString((float)m_dStatorHoleDiameter));    	
    }   

    private void validateGrooveLength()
    {
    	if (this.jtxtGrooveLength.getText() == null)
    		this.jtxtGrooveLength.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtGrooveLength.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dGrooveLength = dTemp;
		this.jtxtGrooveLength.setText(Float.toString((float)m_dGrooveLength));    	
    }   

    private void validateGrooveDiameter()
    {
    	if (this.jtxtGrooveDiameter.getText() == null)
    		this.jtxtGrooveDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtGrooveDiameter.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dGrooveDiameter = dTemp;
		this.jtxtGrooveDiameter.setText(Float.toString((float)m_dGrooveDiameter));    	
    }   
    
    /**
	 * @param owner
	 */
	public ValvePropertiesDialog(Frame owner) 
	{
		super(owner);
		initialize();
		
		jbtnOk.addActionListener(this);
		jbtnCancel.addActionListener(this);
        jtxtName.addKeyListener(this);
        jtxtName.addFocusListener(this);
        jtxtStatorHoleLength.addKeyListener(this);
        jtxtStatorHoleLength.addFocusListener(this);
        jtxtStatorHoleDiameter.addKeyListener(this);
        jtxtStatorHoleDiameter.addFocusListener(this);
        jtxtGrooveLength.addKeyListener(this);
        jtxtGrooveLength.addFocusListener(this);
        jtxtGrooveDiameter.addKeyListener(this);
        jtxtGrooveDiameter.addFocusListener(this);
        
        performCalculations();

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(321, 231);
		this.setContentPane(getJContentPane());
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("Valve Properties");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel41 = new JLabel();
			jLabel41.setBounds(new Rectangle(264, 132, 37, 16));
			jLabel41.setText("cm");
			jLabel31 = new JLabel();
			jLabel31.setBounds(new Rectangle(264, 108, 37, 16));
			jLabel31.setText("mil");
			jLabel52 = new JLabel();
			jLabel52.setBounds(new Rectangle(12, 132, 153, 16));
			jLabel52.setText("Groove length:");
			jLabel51 = new JLabel();
			jLabel51.setBounds(new Rectangle(12, 108, 153, 16));
			jLabel51.setText("Groove diameter:");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(12, 60, 153, 16));
			jLabel5.setText("Stator hole diameter:");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(12, 36, 153, 16));
			jLabel7.setText("Ports:");
			jLabel = new JLabel();
			jLabel.setText("Name:");
			jLabel.setBounds(new Rectangle(12, 12, 153, 16));
			jLabel4 = new JLabel();
			jLabel4.setText("cm");
			jLabel4.setLocation(new Point(264, 86));
			jLabel4.setSize(new Dimension(38, 16));
			jLabel3 = new JLabel();
			jLabel3.setText("mil");
			jLabel3.setLocation(new Point(264, 62));
			jLabel3.setSize(new Dimension(38, 16));
			jLabel2 = new JLabel();
			jLabel2.setText("Stator hole length:");
			jLabel2.setBounds(new Rectangle(12, 84, 153, 16));
			jLabel1 = new JLabel();
			jLabel1.setText("Connection length:");
			jLabel1.setBounds(new Rectangle(12, 60, 121, 16));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBackground(new Color(238, 238, 238));
			jContentPane.add(getJbtnOk(), null);
			jContentPane.add(getJbtnCancel(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getJtxtName(), null);
			jContentPane.add(getJtxtStatorHoleDiameter(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(getJtxtStatorHoleLength(), null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(getJtxtPorts(), null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(jLabel51, null);
			jContentPane.add(jLabel52, null);
			jContentPane.add(getJtxtGrooveDiameter(), null);
			jContentPane.add(jLabel31, null);
			jContentPane.add(getJtxtGrooveLength(), null);
			jContentPane.add(jLabel41, null);
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
			jbtnOk.setBounds(new Rectangle(40, 164, 125, 31));
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
			jbtnCancel.setBounds(new Rectangle(176, 164, 125, 31));
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
			jtxtName.setHorizontalAlignment(JTextField.LEADING);
			jtxtName.setBounds(new Rectangle(172, 12, 133, 20));
			jtxtName.setText("Default valve");
		}
		return jtxtName;
	}

	public void performCalculations()
	{
		validateName();
		validateStatorHoleLength();
		validateStatorHoleDiameter();
		validateGrooveLength();
		validateGrooveDiameter();

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
		if (e.getComponent() != this.jtxtName)
		{
			if (!((Character.isDigit(e.getKeyChar()) ||
					(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
					(e.getKeyChar() == KeyEvent.VK_DELETE) ||
					(e.getKeyChar() == KeyEvent.VK_PERIOD))))
			{
		        e.consume();
			}
		}
		
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

	/**
	 * This method initializes jtxtStatorHoleDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtStatorHoleDiameter() {
		if (jtxtStatorHoleDiameter == null) {
			jtxtStatorHoleDiameter = new JTextField();
			jtxtStatorHoleDiameter.setHorizontalAlignment(JTextField.TRAILING);
			jtxtStatorHoleDiameter.setBounds(new Rectangle(172, 60, 85, 20));
			jtxtStatorHoleDiameter.setText("2");
		}
		return jtxtStatorHoleDiameter;
	}

	/**
	 * This method initializes jtxtStatorHoleLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtStatorHoleLength() {
		if (jtxtStatorHoleLength == null) {
			jtxtStatorHoleLength = new JTextField();
			jtxtStatorHoleLength.setText("5");
			jtxtStatorHoleLength.setBounds(new Rectangle(172, 84, 85, 20));
			jtxtStatorHoleLength.setHorizontalAlignment(JTextField.TRAILING);
		}
		return jtxtStatorHoleLength;
	}

	/**
	 * This method initializes jtxtPorts	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPorts() {
		if (jtxtPorts == null) {
			jtxtPorts = new JTextField();
			jtxtPorts.setBounds(new Rectangle(172, 36, 85, 20));
			jtxtPorts.setHorizontalAlignment(JTextField.TRAILING);
			jtxtPorts.setEnabled(false);
			jtxtPorts.setText("6");
		}
		return jtxtPorts;
	}

	/**
	 * This method initializes jtxtGrooveDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtGrooveDiameter() {
		if (jtxtGrooveDiameter == null) {
			jtxtGrooveDiameter = new JTextField();
			jtxtGrooveDiameter.setBounds(new Rectangle(172, 108, 85, 20));
			jtxtGrooveDiameter.setHorizontalAlignment(JTextField.TRAILING);
			jtxtGrooveDiameter.setText("2");
		}
		return jtxtGrooveDiameter;
	}

	/**
	 * This method initializes jtxtGrooveLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtGrooveLength() {
		if (jtxtGrooveLength == null) {
			jtxtGrooveLength = new JTextField();
			jtxtGrooveLength.setBounds(new Rectangle(172, 132, 84, 20));
			jtxtGrooveLength.setHorizontalAlignment(JTextField.TRAILING);
			jtxtGrooveLength.setText("2");
		}
		return jtxtGrooveLength;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
