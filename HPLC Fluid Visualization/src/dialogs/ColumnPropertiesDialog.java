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
import javax.swing.SwingConstants;

public class ColumnPropertiesDialog extends JDialog implements ActionListener, KeyListener, FocusListener
{
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel = null;
	private JTextField jtxtName = null;
	public boolean m_bOk = false;
	public String m_strName = "Default column";
	private JTextField m_ControlChanged = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JTextField jtxtLength = null;
	private JTextField jtxtInnerDiameter = null;
	private JTextField jtxtParticleDiameter = null;
	private JLabel jLabel7 = null;
	private JTextField jtxtTemperature = null;
	private JLabel jLabel8 = null;
	private JCheckBox jchkPreheater = null;
	private JLabel jlblPreheaterInnerDiameter = null;
	private JTextField jtxtPreheaterInnerDiameter = null;
	private JLabel jlblPreheaterInnerDiameterUnits = null;
	private JLabel jlblPreheaterLength = null;
	private JTextField jtxtPreheaterLength = null;
	private JLabel jlblPreheaterLengthUnits = null;
	private JLabel jLabel51 = null;
	private JTextField jtxtInterparticlePorosity = null;
	private JLabel jLabel511 = null;
	private JTextField jtxtIntraparticlePorosity = null;
	private JLabel jLabel5111 = null;
	private JLabel jlblTotalPorosity = null;
	private JLabel jLabel51111 = null;
	private JLabel jLabel51112 = null;
	private JLabel jLabel511121 = null;
	private JLabel jLabel5111211 = null;
	private JTextField jtxtATerm = null;
	private JTextField jtxtBTerm = null;
	private JTextField jtxtCTerm = null;
	
	private double m_dColumnLength = 100;
	private double m_dColumnInnerDiameter = 4.6;
	private double m_dParticleDiameter = 5;
	private double m_dTemperature = 25;
	private boolean m_bPreheater = false;
	private double m_dPreheaterLength = 10;
	private double m_dPreheaterInnerDiameter = 5;
	private double m_dInterparticlePorosity = 0.4;
	private double m_dIntraparticlePorosity = 0.4;
	private double m_dATerm = 1;
	private double m_dBTerm = 5;
	private double m_dCTerm = 0.05;
	

	public void setIntraparticlePorosity(double dIntraparticlePorosity)
	{
		this.jtxtIntraparticlePorosity.setText(Float.toString((float)dIntraparticlePorosity));
		performCalculations();
	}
	
	public double getIntraparticlePorosity()
	{
		return m_dIntraparticlePorosity;
	}
	
	public void setATerm(double dATerm)
	{
		this.jtxtATerm.setText(Float.toString((float)dATerm));
		performCalculations();
	}
	
	public double getATerm()
	{
		return m_dATerm;
	}
	
	public void setBTerm(double dBTerm)
	{
		this.jtxtBTerm.setText(Float.toString((float)dBTerm));
		performCalculations();
	}
	
	public double getBTerm()
	{
		return m_dBTerm;
	}
	
	public void setCTerm(double dCTerm)
	{
		this.jtxtCTerm.setText(Float.toString((float)dCTerm));
		performCalculations();
	}
	
	public double getCTerm()
	{
		return m_dCTerm;
	}
	
	public void setInterparticlePorosity(double dInterparticlePorosity)
	{
		this.jtxtInterparticlePorosity.setText(Float.toString((float)dInterparticlePorosity));
		performCalculations();
	}
	
	public double getInterparticlePorosity()
	{
		return m_dInterparticlePorosity;
	}
	
	public void setColumnLength(double dColumnLength)
	{
		this.jtxtLength.setText(Float.toString((float)dColumnLength));
		performCalculations();
	}
	
	public double getColumnLength()
	{
		return m_dColumnLength;
	}
	
	public void setColumnInnerDiameter(double dColumnInnerDiameter)
	{
		this.jtxtInnerDiameter.setText(Float.toString((float)dColumnInnerDiameter));
		performCalculations();
	}
	
	public double getColumnInnerDiameter()
	{
		return m_dColumnInnerDiameter;
	}

	public void setParticleDiameter(double dParticleDiameter)
	{
		this.jtxtParticleDiameter.setText(Float.toString((float)dParticleDiameter));
		performCalculations();
	}
	
	public double getParticleDiameter()
	{
		return this.m_dParticleDiameter;
	}

	public void setTemperature(double dTemperature)
	{
		this.jtxtTemperature.setText(Float.toString((float)dTemperature));
		performCalculations();
	}
	
	public double getTemperature()
	{
		return this.m_dTemperature;
	}

	public void setPreheater(boolean bPreheaterOn)
	{
		m_bPreheater = bPreheaterOn;
		
		if (!bPreheaterOn)
		{
			this.jchkPreheater.setSelected(false);
			this.jtxtPreheaterInnerDiameter.setEnabled(false);
			this.jtxtPreheaterLength.setEnabled(false);
			this.jlblPreheaterInnerDiameter.setEnabled(false);
			this.jlblPreheaterInnerDiameterUnits.setEnabled(false);
			this.jlblPreheaterLength.setEnabled(false);
			this.jlblPreheaterLengthUnits.setEnabled(false);
		}
		else
		{
			this.jchkPreheater.setSelected(true);
			this.jtxtPreheaterInnerDiameter.setEnabled(true);
			this.jtxtPreheaterLength.setEnabled(true);
			this.jlblPreheaterInnerDiameter.setEnabled(true);
			this.jlblPreheaterInnerDiameterUnits.setEnabled(true);
			this.jlblPreheaterLength.setEnabled(true);
			this.jlblPreheaterLengthUnits.setEnabled(true);
		}
		
		performCalculations();
	}
	
	public boolean getPreheater()
	{
		return this.m_bPreheater;
	}

	public void setPreheaterLength(double dPreheaterLength)
	{
		this.jtxtPreheaterLength.setText(Float.toString((float)dPreheaterLength));
		performCalculations();
	}
	
	public double getPreheaterLength()
	{
		return m_dPreheaterLength;
	}
	
	public void setPreheaterInnerDiameter(double dPreheaterInnerDiameter)
	{
		this.jtxtPreheaterInnerDiameter.setText(Float.toString((float)dPreheaterInnerDiameter));
		performCalculations();
	}
	
	public double getPreheaterInnerDiameter()
	{
		return m_dPreheaterInnerDiameter;
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
    
    private void validateATerm()
    {
    	if (this.jtxtATerm.getText() == null)
    		this.jtxtATerm.setText("0.0");

    	double dTemp = (double)Float.valueOf(this.jtxtATerm.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dATerm = dTemp;
		this.jtxtATerm.setText(Float.toString((float)m_dATerm));    	
    } 
    
    private void validateBTerm()
    {
    	if (this.jtxtBTerm.getText() == null)
    		this.jtxtBTerm.setText("0.0");

    	double dTemp = (double)Float.valueOf(this.jtxtBTerm.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dBTerm = dTemp;
		this.jtxtBTerm.setText(Float.toString((float)m_dBTerm));    	
    } 
    
    private void validateCTerm()
    {
    	if (this.jtxtCTerm.getText() == null)
    		this.jtxtCTerm.setText("0.0");

    	double dTemp = (double)Float.valueOf(this.jtxtCTerm.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dCTerm = dTemp;
		this.jtxtCTerm.setText(Float.toString((float)m_dCTerm));    	
    } 
    
    private void validateInterparticlePorosity()
    {
    	if (this.jtxtInterparticlePorosity.getText() == null)
    		this.jtxtInterparticlePorosity.setText("0.0");

    	double dTemp = (double)Float.valueOf(this.jtxtInterparticlePorosity.getText());
		
    	if (dTemp < .001)
			dTemp = .001;
		if (dTemp > .999)
			dTemp = .999;
		
		this.m_dInterparticlePorosity = dTemp;
		this.jtxtInterparticlePorosity.setText(Float.toString((float)m_dInterparticlePorosity));    	
    }   
    
    private void validateIntraparticlePorosity()
    {
    	if (this.jtxtIntraparticlePorosity.getText() == null)
    		this.jtxtIntraparticlePorosity.setText("0.0");

    	double dTemp = (double)Float.valueOf(this.jtxtIntraparticlePorosity.getText());
		
    	if (dTemp < .001)
			dTemp = .001;
		if (dTemp > .999)
			dTemp = .999;
		
		this.m_dIntraparticlePorosity = dTemp;
		this.jtxtIntraparticlePorosity.setText(Float.toString((float)m_dIntraparticlePorosity));    	
    }  
    
    private void validateColumnInnerDiameter()
    {
    	if (this.jtxtInnerDiameter.getText() == null)
    		this.jtxtInnerDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtInnerDiameter.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dColumnInnerDiameter = dTemp;
		this.jtxtInnerDiameter.setText(Float.toString((float)m_dColumnInnerDiameter));    	
    }   

    private void validateColumnLength()
    {
    	if (this.jtxtLength.getText() == null)
    		this.jtxtLength.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtLength.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dColumnLength = dTemp;
		this.jtxtLength.setText(Float.toString((float)m_dColumnLength));    	
    }   

    private void validateParticleDiameter()
    {
    	if (this.jtxtParticleDiameter.getText() == null)
    		this.jtxtParticleDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtParticleDiameter.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dParticleDiameter = dTemp;
		this.jtxtParticleDiameter.setText(Float.toString((float)m_dParticleDiameter));    	
    }

    private void validateTemperature()
    {
    	if (this.jtxtTemperature.getText() == null)
    		this.jtxtTemperature.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtTemperature.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dTemperature = dTemp;
		this.jtxtTemperature.setText(Float.toString((float)m_dTemperature));    	
    }   

    private void validatePreheaterInnerDiameter()
    {
    	if (this.jtxtPreheaterInnerDiameter.getText() == null)
    		this.jtxtPreheaterInnerDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtPreheaterInnerDiameter.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dPreheaterInnerDiameter = dTemp;
		this.jtxtPreheaterInnerDiameter.setText(Float.toString((float)m_dPreheaterInnerDiameter));    	
    }   

    private void validatePreheaterLength()
    {
    	if (this.jtxtPreheaterLength.getText() == null)
    		this.jtxtPreheaterLength.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtPreheaterLength.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dPreheaterLength = dTemp;
		this.jtxtPreheaterLength.setText(Float.toString((float)m_dPreheaterLength));    	
    }   

    /**
	 * @param owner
	 */
	public ColumnPropertiesDialog(Frame owner) 
	{
		super(owner);
		initialize();
		
		jbtnOk.addActionListener(this);
		jbtnCancel.addActionListener(this);
        jtxtName.addKeyListener(this);
        jtxtName.addFocusListener(this);
        jtxtInnerDiameter.addKeyListener(this);
        jtxtInnerDiameter.addFocusListener(this);
        jtxtLength.addKeyListener(this);
        jtxtLength.addFocusListener(this);
        jtxtParticleDiameter.addKeyListener(this);
        jtxtParticleDiameter.addFocusListener(this);
        jtxtPreheaterInnerDiameter.addKeyListener(this);
        jtxtPreheaterInnerDiameter.addFocusListener(this);
        jtxtPreheaterLength.addKeyListener(this);
        jtxtPreheaterLength.addFocusListener(this);
        jtxtTemperature.addKeyListener(this);
        jtxtTemperature.addFocusListener(this);
        jtxtInterparticlePorosity.addKeyListener(this);
        jtxtInterparticlePorosity.addFocusListener(this);
        jtxtIntraparticlePorosity.addKeyListener(this);
        jtxtIntraparticlePorosity.addFocusListener(this);
        jtxtATerm.addKeyListener(this);
        jtxtATerm.addFocusListener(this);
        jtxtBTerm.addKeyListener(this);
        jtxtBTerm.addFocusListener(this);
        jtxtCTerm.addKeyListener(this);
        jtxtCTerm.addFocusListener(this);
        jchkPreheater.addActionListener(this);
        
        performCalculations();

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(559, 383);
		this.setContentPane(getJContentPane());
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("Column Properties");
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
			jLabel.setBounds(new Rectangle(12, 24, 125, 16));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBackground(new Color(238, 238, 238));
			jContentPane.add(getJbtnOk(), null);
			jContentPane.add(getJbtnCancel(), null);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getJPanel1(), null);
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
			jbtnOk.setBounds(new Rectangle(284, 316, 125, 31));
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
			jbtnCancel.setBounds(new Rectangle(420, 316, 125, 31));
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
		else if (arg0.getSource() == this.jchkPreheater)
		{
			this.setPreheater(jchkPreheater.isSelected());
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
			jtxtName.setBounds(new Rectangle(140, 24, 121, 20));
			jtxtName.setText("Default column");
		}
		return jtxtName;
	}

	public void performCalculations()
	{
		validateName();
		validateColumnInnerDiameter();
		validateColumnLength();
		validateParticleDiameter();
		validateTemperature();
		validatePreheaterInnerDiameter();
		validatePreheaterLength();
		validateInterparticlePorosity();
		validateIntraparticlePorosity();
		validateATerm();
		validateBTerm();
		validateCTerm();
		
		NumberFormat formatter = new DecimalFormat("#0.0000");

		double dTotalPorosity = this.getInterparticlePorosity() + this.getIntraparticlePorosity() * (1 - this.getInterparticlePorosity());
		this.jlblTotalPorosity.setText(formatter.format(dTotalPorosity));

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
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel5111211 = new JLabel();
			jLabel5111211.setBounds(new Rectangle(32, 264, 34, 16));
			jLabel5111211.setText("C:");
			jLabel511121 = new JLabel();
			jLabel511121.setBounds(new Rectangle(32, 240, 30, 16));
			jLabel511121.setText("B:");
			jLabel51112 = new JLabel();
			jLabel51112.setBounds(new Rectangle(32, 216, 30, 16));
			jLabel51112.setText("A:");
			jLabel51111 = new JLabel();
			jLabel51111.setBounds(new Rectangle(12, 192, 203, 16));
			jLabel51111.setText("Reduced Van Deemter terms:");
			jlblTotalPorosity = new JLabel();
			jlblTotalPorosity.setBounds(new Rectangle(140, 168, 73, 16));
			jlblTotalPorosity.setHorizontalAlignment(SwingConstants.TRAILING);
			jlblTotalPorosity.setText("0.64");
			jLabel5111 = new JLabel();
			jLabel5111.setBounds(new Rectangle(12, 168, 121, 16));
			jLabel5111.setText("Total porosity:");
			jLabel511 = new JLabel();
			jLabel511.setBounds(new Rectangle(12, 144, 125, 16));
			jLabel511.setText("Intraparticle porosity:");
			jLabel51 = new JLabel();
			jLabel51.setBounds(new Rectangle(12, 120, 125, 16));
			jLabel51.setText("Interparticle porosity:");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(220, 96, 38, 16));
			jLabel6.setText("\u00b5m");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(12, 96, 125, 16));
			jLabel5.setText("Particle diameter:");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(220, 72, 38, 16));
			jLabel4.setText("mm");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(220, 48, 38, 16));
			jLabel3.setText("mm");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(12, 72, 125, 16));
			jLabel2.setText("Inner diameter:");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(12, 48, 125, 16));
			jLabel1.setText("Length:");
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(8, 8, 269, 293));
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "Column options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.add(jLabel, null);
			jPanel.add(getJtxtName(), null);
			jPanel.add(jLabel1, null);
			jPanel.add(jLabel2, null);
			jPanel.add(jLabel3, null);
			jPanel.add(jLabel4, null);
			jPanel.add(jLabel5, null);
			jPanel.add(jLabel6, null);
			jPanel.add(getJtxtLength(), null);
			jPanel.add(getJtxtInnerDiameter(), null);
			jPanel.add(getJtxtParticleDiameter(), null);
			jPanel.add(jLabel51, null);
			jPanel.add(getJtxtInterparticlePorosity(), null);
			jPanel.add(jLabel511, null);
			jPanel.add(getJtxtIntraparticlePorosity(), null);
			jPanel.add(jLabel5111, null);
			jPanel.add(jlblTotalPorosity, null);
			jPanel.add(jLabel51111, null);
			jPanel.add(jLabel51112, null);
			jPanel.add(jLabel511121, null);
			jPanel.add(jLabel5111211, null);
			jPanel.add(getJtxtATerm(), null);
			jPanel.add(getJtxtBTerm(), null);
			jPanel.add(getJtxtCTerm(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jlblPreheaterLengthUnits = new JLabel();
			jlblPreheaterLengthUnits.setBounds(new Rectangle(212, 96, 38, 16));
			jlblPreheaterLengthUnits.setText("cm");
			jlblPreheaterLength = new JLabel();
			jlblPreheaterLength.setBounds(new Rectangle(28, 96, 97, 16));
			jlblPreheaterLength.setText("Length:");
			jlblPreheaterInnerDiameterUnits = new JLabel();
			jlblPreheaterInnerDiameterUnits.setBounds(new Rectangle(212, 72, 38, 16));
			jlblPreheaterInnerDiameterUnits.setText("mil");
			jlblPreheaterInnerDiameter = new JLabel();
			jlblPreheaterInnerDiameter.setBounds(new Rectangle(28, 72, 101, 16));
			jlblPreheaterInnerDiameter.setText("Inner diameter:");
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(212, 24, 38, 16));
			jLabel8.setText("\u00b0C");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(12, 24, 101, 16));
			jLabel7.setText("Temperature:");
			jPanel1 = new JPanel();
			jPanel1.setLayout(null);
			jPanel1.setBounds(new Rectangle(284, 8, 261, 293));
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Temperature control", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel1.add(jLabel7, null);
			jPanel1.add(getJtxtTemperature(), null);
			jPanel1.add(jLabel8, null);
			jPanel1.add(getJchkPreheater(), null);
			jPanel1.add(jlblPreheaterInnerDiameter, null);
			jPanel1.add(getJtxtPreheaterInnerDiameter(), null);
			jPanel1.add(jlblPreheaterInnerDiameterUnits, null);
			jPanel1.add(jlblPreheaterLength, null);
			jPanel1.add(getJtxtPreheaterLength(), null);
			jPanel1.add(jlblPreheaterLengthUnits, null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jtxtLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtLength() {
		if (jtxtLength == null) {
			jtxtLength = new JTextField();
			jtxtLength.setBounds(new Rectangle(140, 48, 73, 20));
			jtxtLength.setHorizontalAlignment(JTextField.TRAILING);
			jtxtLength.setText("100");
		}
		return jtxtLength;
	}

	/**
	 * This method initializes jtxtInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInnerDiameter() {
		if (jtxtInnerDiameter == null) {
			jtxtInnerDiameter = new JTextField();
			jtxtInnerDiameter.setBounds(new Rectangle(140, 72, 73, 20));
			jtxtInnerDiameter.setText("4.6");
			jtxtInnerDiameter.setHorizontalAlignment(JTextField.TRAILING);
		}
		return jtxtInnerDiameter;
	}

	/**
	 * This method initializes jtxtParticleDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtParticleDiameter() {
		if (jtxtParticleDiameter == null) {
			jtxtParticleDiameter = new JTextField();
			jtxtParticleDiameter.setBounds(new Rectangle(140, 96, 73, 20));
			jtxtParticleDiameter.setHorizontalAlignment(JTextField.TRAILING);
			jtxtParticleDiameter.setText("5");
		}
		return jtxtParticleDiameter;
	}

	/**
	 * This method initializes jtxtTemperature	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtTemperature() {
		if (jtxtTemperature == null) {
			jtxtTemperature = new JTextField();
			jtxtTemperature.setBounds(new Rectangle(136, 24, 69, 20));
			jtxtTemperature.setHorizontalAlignment(JTextField.TRAILING);
			jtxtTemperature.setText("25");
		}
		return jtxtTemperature;
	}

	/**
	 * This method initializes jchkPreheater	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJchkPreheater() {
		if (jchkPreheater == null) {
			jchkPreheater = new JCheckBox();
			jchkPreheater.setBounds(new Rectangle(8, 48, 229, 17));
			jchkPreheater.setText("Preheater/cooler");
		}
		return jchkPreheater;
	}

	/**
	 * This method initializes jtxtPreheaterInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPreheaterInnerDiameter() {
		if (jtxtPreheaterInnerDiameter == null) {
			jtxtPreheaterInnerDiameter = new JTextField();
			jtxtPreheaterInnerDiameter.setBounds(new Rectangle(136, 72, 69, 20));
			jtxtPreheaterInnerDiameter.setText("5");
			jtxtPreheaterInnerDiameter.setHorizontalAlignment(JTextField.TRAILING);
		}
		return jtxtPreheaterInnerDiameter;
	}

	/**
	 * This method initializes jtxtPreheaterLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPreheaterLength() {
		if (jtxtPreheaterLength == null) {
			jtxtPreheaterLength = new JTextField();
			jtxtPreheaterLength.setBounds(new Rectangle(136, 96, 69, 20));
			jtxtPreheaterLength.setHorizontalAlignment(JTextField.TRAILING);
			jtxtPreheaterLength.setText("10");
		}
		return jtxtPreheaterLength;
	}

	/**
	 * This method initializes jtxtInterparticlePorosity	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInterparticlePorosity() {
		if (jtxtInterparticlePorosity == null) {
			jtxtInterparticlePorosity = new JTextField();
			jtxtInterparticlePorosity.setBounds(new Rectangle(140, 120, 73, 20));
			jtxtInterparticlePorosity.setHorizontalAlignment(JTextField.TRAILING);
			jtxtInterparticlePorosity.setText("0.4");
		}
		return jtxtInterparticlePorosity;
	}

	/**
	 * This method initializes jtxtIntraparticlePorosity	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtIntraparticlePorosity() {
		if (jtxtIntraparticlePorosity == null) {
			jtxtIntraparticlePorosity = new JTextField();
			jtxtIntraparticlePorosity.setBounds(new Rectangle(140, 144, 73, 20));
			jtxtIntraparticlePorosity.setHorizontalAlignment(JTextField.TRAILING);
			jtxtIntraparticlePorosity.setText("0.4");
		}
		return jtxtIntraparticlePorosity;
	}

	/**
	 * This method initializes jtxtATerm	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtATerm() {
		if (jtxtATerm == null) {
			jtxtATerm = new JTextField();
			jtxtATerm.setBounds(new Rectangle(140, 216, 73, 20));
			jtxtATerm.setHorizontalAlignment(JTextField.TRAILING);
			jtxtATerm.setText("1");
		}
		return jtxtATerm;
	}

	/**
	 * This method initializes jtxtBTerm	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtBTerm() {
		if (jtxtBTerm == null) {
			jtxtBTerm = new JTextField();
			jtxtBTerm.setBounds(new Rectangle(140, 240, 73, 20));
			jtxtBTerm.setHorizontalAlignment(JTextField.TRAILING);
			jtxtBTerm.setText("5");
		}
		return jtxtBTerm;
	}

	/**
	 * This method initializes jtxtCTerm	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtCTerm() {
		if (jtxtCTerm == null) {
			jtxtCTerm = new JTextField();
			jtxtCTerm.setBounds(new Rectangle(140, 264, 73, 20));
			jtxtCTerm.setHorizontalAlignment(JTextField.TRAILING);
			jtxtCTerm.setText("0.05");
		}
		return jtxtCTerm;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
