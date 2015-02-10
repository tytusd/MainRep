package dialogs;

import javax.swing.JPanel;
import java.awt.Frame;
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

public class TubePropertiesDialog extends JDialog implements ActionListener, KeyListener, FocusListener
{
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel = null;
	private JRadioButton jrdoLength = null;
	private JRadioButton jrdoVolume = null;
	private JTextField jtxtInnerDiameter = null;
	private JTextField jtxtLength = null;
	private JTextField jtxtVolume = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel11 = null;
	private JLabel jLabel111 = null;
	
	public boolean m_bOk = false;
	private double m_dInnerDiameter;
	private double m_dLength;
	private double m_dVolume;
	private boolean m_bVolumeSelected;
	
	private JTextField m_ControlChanged = null;
	
	public void setInnerDiameter(double dInnerDiameter)
	{
		this.jtxtInnerDiameter.setText(Float.toString((float)dInnerDiameter));
        performCalculations();
	}

	public double getInnerDiameter()
	{
		return this.m_dInnerDiameter;
	}
	
	public void setLength(double dLength)
	{
		this.jtxtLength.setText(Float.toString((float)dLength));
		performCalculations();
	}

	public double getLength()
	{
		return this.m_dLength;
	}
	
	public void setVolume(double dVolume)
	{
		this.jtxtVolume.setText(Float.toString((float)dVolume));
		performCalculations();
	}

	public double getVolume()
	{
		return this.m_dVolume;
	}
	
	public void setVolumeVisible(boolean bVolumeVisible)
	{
		this.m_bVolumeSelected = bVolumeVisible;
		
		if (bVolumeVisible)
		{
			this.jrdoLength.setSelected(false);
			this.jrdoVolume.setSelected(true);
			this.jtxtLength.setEnabled(false);
			this.jtxtVolume.setEnabled(true);
		}
		else
		{
			this.jrdoLength.setSelected(true);
			this.jrdoVolume.setSelected(false);
			this.jtxtLength.setEnabled(true);
			this.jtxtVolume.setEnabled(false);
		}
	}

	public boolean getVolumeVisible()
	{
		return this.m_bVolumeSelected;
	}
	
    private void validateInnerDiameter()
    {
    	if (this.jtxtInnerDiameter.getText() == null)
    		this.jtxtInnerDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtInnerDiameter.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dInnerDiameter = dTemp;
		this.jtxtInnerDiameter.setText(Float.toString((float)m_dInnerDiameter));    	
    }   
    
    private void validateLength()
    {
    	if (this.jtxtLength.getText() == null)
    		this.jtxtLength.setText("0");
    	
		double dTemp = (double)Float.valueOf(this.jtxtLength.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dLength = dTemp;
		this.jtxtLength.setText(Float.toString((float)m_dLength));
    }   
    
    private void validateVolume()
    {
    	if (this.jtxtVolume.getText() == null)
    		this.jtxtVolume.setText("0");
    	
		double dTemp = (double)Float.valueOf(this.jtxtVolume.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dVolume = dTemp;
		this.jtxtVolume.setText(Float.toString((float)m_dVolume));  
    }   
	/**
	 * @param owner
	 */
	public TubePropertiesDialog(Frame owner) 
	{
		super(owner);
		initialize();
		
		jbtnOk.addActionListener(this);
		jbtnCancel.addActionListener(this);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(284, 173);
		this.setContentPane(getJContentPane());
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("Tube Properties");
		
        this.jtxtInnerDiameter.addKeyListener(this);
        this.jtxtInnerDiameter.addFocusListener(this);
        this.jtxtLength.addKeyListener(this);
        this.jtxtLength.addFocusListener(this);
        this.jtxtVolume.addKeyListener(this);
        this.jtxtVolume.addFocusListener(this);
        this.jrdoLength.addActionListener(this);
        this.jrdoVolume.addActionListener(this);
        
        performCalculations();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel111 = new JLabel();
			jLabel111.setText("\u00b5L");
			jLabel111.setLocation(new Point(216, 72));
			jLabel111.setSize(new Dimension(29, 16));
			jLabel11 = new JLabel();
			jLabel11.setText("cm");
			jLabel11.setLocation(new Point(216, 49));
			jLabel11.setSize(new Dimension(28, 16));
			jLabel1 = new JLabel();
			jLabel1.setText("mil");
			jLabel1.setLocation(new Point(216, 12));
			jLabel1.setSize(new Dimension(29, 16));
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(12, 12, 116, 16));
			jLabel.setText("Inner diameter:");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBackground(new Color(238, 238, 238));
			jContentPane.add(getJbtnOk(), null);
			jContentPane.add(getJbtnCancel(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getJchkLength(), null);
			jContentPane.add(getJchkVolume(), null);
			jContentPane.add(getJtxtInnerDiameter(), null);
			jContentPane.add(getJtxtLength(), null);
			jContentPane.add(getJtxtVolume(), null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(jLabel11, null);
			jContentPane.add(jLabel111, null);
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
			jbtnOk.setBounds(new Rectangle(8, 104, 125, 31));
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
			jbtnCancel.setBounds(new Rectangle(144, 104, 124, 31));
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
		else if (arg0.getActionCommand() == "rdoLength")
		{
			this.m_bVolumeSelected = false;
			this.jrdoVolume.setSelected(false);
			this.jrdoLength.setSelected(true);
			this.jtxtLength.setEnabled(true);
			this.jtxtVolume.setEnabled(false);
		}
		else if (arg0.getActionCommand() == "rdoVolume")
		{
			this.m_bVolumeSelected = true;
			this.jrdoLength.setSelected(false);
			this.jrdoVolume.setSelected(true);
			this.jtxtVolume.setEnabled(true);
			this.jtxtLength.setEnabled(false);
		}
	}

	/**
	 * This method initializes jchkLength	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JRadioButton getJchkLength() {
		if (jrdoLength == null) {
			jrdoLength = new JRadioButton();
			jrdoLength.setBounds(new Rectangle(11, 45, 113, 21));
			jrdoLength.setSelected(true);
			jrdoLength.setActionCommand("rdoLength");
			jrdoLength.setText("Length:");
		}
		return jrdoLength;
	}

	/**
	 * This method initializes jchkVolume	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JRadioButton getJchkVolume() {
		if (jrdoVolume == null) {
			jrdoVolume = new JRadioButton();
			jrdoVolume.setBounds(new Rectangle(11, 65, 113, 24));
			jrdoVolume.setActionCommand("rdoVolume");
			jrdoVolume.setText("Volume:");
		}
		return jrdoVolume;
	}

	/**
	 * This method initializes jtxtInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInnerDiameter() {
		if (jtxtInnerDiameter == null) {
			jtxtInnerDiameter = new JTextField();
			jtxtInnerDiameter.setBounds(new Rectangle(131, 9, 78, 20));
			jtxtInnerDiameter.setHorizontalAlignment(JTextField.TRAILING);
			jtxtInnerDiameter.setText("5");
		}
		return jtxtInnerDiameter;
	}

	/**
	 * This method initializes jtxtLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtLength() {
		if (jtxtLength == null) {
			jtxtLength = new JTextField();
			jtxtLength.setBounds(new Rectangle(131, 45, 78, 20));
			jtxtLength.setText("10");
			jtxtLength.setHorizontalAlignment(JTextField.TRAILING);
		}
		return jtxtLength;
	}

	/**
	 * This method initializes jtxtVolume	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtVolume() {
		if (jtxtVolume == null) {
			jtxtVolume = new JTextField();
			jtxtVolume.setBounds(new Rectangle(131, 69, 78, 20));
			jtxtVolume.setText("10");
			jtxtVolume.setHorizontalAlignment(JTextField.TRAILING);
			jtxtVolume.setEnabled(false);
		}
		return jtxtVolume;
	}

	public void performCalculations()
	{
		validateInnerDiameter();
		validateLength();
		validateVolume();
		
		if (this.m_ControlChanged == this.jtxtInnerDiameter)
		{
			if (this.m_bVolumeSelected)
			{
				// Keep volume constant, change length
				this.m_dLength = ((this.m_dVolume / 1000000000) / (Math.PI * Math.pow((this.m_dInnerDiameter * 0.0000254) / 2, 2)) * 100);
				this.jtxtLength.setText(Float.toString((float)this.m_dLength));
			}
			else
			{
				// Keep length constant, change volume
				this.m_dVolume = (this.m_dLength / 100) * (Math.PI * Math.pow((this.m_dInnerDiameter * 0.0000254) / 2, 2) * 1000000000);
				this.jtxtVolume.setText(Float.toString((float)this.m_dVolume));
			}
		}
		else if (this.m_ControlChanged == this.jtxtLength)
		{
			// Keep length constant, change volume
			this.m_dVolume = (this.m_dLength / 100) * (Math.PI * Math.pow((this.m_dInnerDiameter * 0.0000254) / 2, 2) * 1000000000);
			this.jtxtVolume.setText(Float.toString((float)this.m_dVolume));
			
		}
		else if (this.m_ControlChanged == this.jtxtVolume)
		{
			// Keep volume constant, change length
			this.m_dLength = ((this.m_dVolume / 1000000000) / (Math.PI * Math.pow((this.m_dInnerDiameter * 0.0000254) / 2, 2)) * 100);
			this.jtxtLength.setText(Float.toString((float)this.m_dLength));
		}
		
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
		if (!((Character.isDigit(e.getKeyChar()) ||
				(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
				(e.getKeyChar() == KeyEvent.VK_DELETE) ||
				(e.getKeyChar() == KeyEvent.VK_PERIOD))))
		{
	        e.consume();
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

}  //  @jve:decl-index=0:visual-constraint="10,10"
