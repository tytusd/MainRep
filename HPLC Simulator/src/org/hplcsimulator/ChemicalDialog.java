package org.hplcsimulator;

import javax.swing.JPanel;

import java.awt.Frame;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

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
import java.text.ParseException;
import java.util.Vector;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import java.awt.GridBagConstraints;

public class ChemicalDialog extends JDialog implements ActionListener, KeyListener, FocusListener, ChangeListener{

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JComboBox jcboCompound = null;
	private JLabel jLabel1 = null;
	private JTextField jtxtConcentration1 = null;
	private JLabel jLabel2 = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel41 = null;
	private JLabel jLabel51 = null;
	private JLabel jlblSlope1 = null;
	private JLabel jlblSlope2 = null;
	private JLabel jlblIntercept1 = null;
	private JLabel jlblIntercept2 = null;
	private JPanel jPanel = null;
	
	public Vector<Integer> m_vectCompoundsUsed = new Vector<Integer>();  //  @jve:decl-index=0:
	public boolean m_bOk = false;
	public int m_iCompound = 0;
	public String m_strCompoundName = "";  //  @jve:decl-index=0:
	public double m_dConcentration1 = 50;
	public double m_dConcentration2 = 50;
	public double m_dMinimumConcentration = 0.01;
	public double m_dMaximumConcentration = 1000;
	public int m_iNumMatrixCompounds = 100;
	public double[] m_dLogkwvsTSlope = new double[Globals.iNumSolvents];
	public double[] m_dLogkwvsTIntercept = new double[Globals.iNumSolvents];
	public double[] m_dSvsTSlope = new double[Globals.iNumSolvents];
	public double[] m_dSvsTIntercept = new double[Globals.iNumSolvents];
	public double m_dMolarVolume = 0;
	public int m_iCompoundType = 0;
	public int m_iStationaryPhase = 0;
	private JTabbedPane jTabbedPane = null;
	private JPanel jPanel2 = null;
	private JPanel jPanel3 = null;
	private JPanel jPanel4 = null;
	private JLabel jLabel31 = null;
	private JLabel jLabel311 = null;
	private JTextField jtxtCompoundName = null;
	private JLabel jLabel32 = null;
	private JLabel jLabel61 = null;
	private JLabel jLabel42 = null;
	private JLabel jLabel421 = null;
	private JTextField jtxtSlope1 = null;
	private JTextField jtxtIntercept1 = null;
	private JLabel jLabel422 = null;
	private JLabel jLabel4211 = null;
	private JTextField jtxtSlope2 = null;
	private JTextField jtxtIntercept2 = null;
	private JLabel jlblConcentration2 = null;
	private JTextField jtxtConcentration2 = null;
	private JLabel jLabel21 = null;
	private JLabel jlblNumCompounds = null;
	private JTextField jtxtNumMatrixCompounds = null;
	private JLabel jlblMaxConcentration = null;
	private JTextField jtxtMaximumConcentration = null;
	private JTextField jtxtMinimumConcentration = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel11 = null;
	private JLabel jLabel611 = null;
	private JTextField jtxtSlope21 = null;
	private JLabel jLabel424 = null;
	private JLabel jLabel4221 = null;
	private JLabel jLabel42111 = null;
	private JTextField jtxtSlope11 = null;
	private JTextField jtxtIntercept21 = null;
	private JTextField jtxtIntercept11 = null;
	private JLabel jLabel322 = null;
	private JLabel jLabel4213 = null;
	private JPanel jPanel5 = null;
	private JPanel jPanel51 = null;
	private JLabel jLabel33 = null;
	private JLabel jLabel43 = null;
	private JLabel jlblSlope11 = null;
	private JLabel jLabel52 = null;
	private JLabel jlblIntercept11 = null;
	private JLabel jLabel63 = null;
	private JLabel jLabel411 = null;
	private JLabel jlblSlope21 = null;
	private JLabel jLabel511 = null;
	private JLabel jlblIntercept21 = null;
	private JLabel jlblMaxConcentration1 = null;
	private JLabel jLabel211 = null;
	private JLabel jLabel2111 = null;
	private JLabel jLabel = null;
	private JPanel jPanel6 = null;
	
	private NumberFormat floatFormatter = new DecimalFormat("#0.0#########");

	/**
	 * @param owner
	 */
	public ChemicalDialog(Frame owner, boolean bEditDialog) 
	{
		super(owner);
		initialize();
		
		if (bEditDialog)
		{
			this.setTitle("Edit Compound");
			this.jTabbedPane.removeTabAt(2);
		}
		
		jTabbedPane.addChangeListener(this);
		jcboCompound.addActionListener(this);
		jbtnOk.addActionListener(this);
		jbtnCancel.addActionListener(this);
        jtxtConcentration1.addKeyListener(this);
        jtxtConcentration1.addFocusListener(this);
        jtxtConcentration2.addKeyListener(this);
        jtxtConcentration2.addFocusListener(this);
        jtxtSlope1.addKeyListener(this);
        jtxtSlope1.addFocusListener(this);
        jtxtSlope2.addKeyListener(this);
        jtxtSlope2.addFocusListener(this);
        jtxtSlope11.addKeyListener(this);
        jtxtSlope11.addFocusListener(this);
        jtxtSlope21.addKeyListener(this);
        jtxtSlope21.addFocusListener(this);
        jtxtIntercept1.addKeyListener(this);
        jtxtIntercept1.addFocusListener(this);
        jtxtIntercept2.addKeyListener(this);
        jtxtIntercept2.addFocusListener(this);
        jtxtIntercept11.addKeyListener(this);
        jtxtIntercept11.addFocusListener(this);
        jtxtIntercept21.addKeyListener(this);
        jtxtIntercept21.addFocusListener(this);
        jtxtNumMatrixCompounds.addKeyListener(this);
        jtxtNumMatrixCompounds.addFocusListener(this);
        jtxtMinimumConcentration.addKeyListener(this);
        jtxtMinimumConcentration.addFocusListener(this);
        jtxtMaximumConcentration.addKeyListener(this);
        jtxtMaximumConcentration.addFocusListener(this);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(445, 433);
		this.setContentPane(getJContentPane());
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("Add New Compound");
		this.setCompoundVariables();
		this.performValidations();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jlblIntercept2 = new JLabel();
			jlblIntercept2.setText(floatFormatter.format(-4.492));
			jlblIntercept2.setBounds(new Rectangle(296, 76, 81, 16));
			jlblIntercept2.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblIntercept1 = new JLabel();
			jlblIntercept1.setText(floatFormatter.format(1.775));
			jlblIntercept1.setBounds(new Rectangle(100, 76, 81, 16));
			jlblIntercept1.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblSlope2 = new JLabel();
			jlblSlope2.setText(floatFormatter.format(0.0049));
			jlblSlope2.setBounds(new Rectangle(296, 52, 81, 16));
			jlblSlope2.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblSlope1 = new JLabel();
			jlblSlope1.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblSlope1.setBounds(new Rectangle(100, 52, 81, 16));
			jlblSlope1.setText(floatFormatter.format(-0.0092));
			jLabel51 = new JLabel();
			jLabel51.setText("Intercept:");
			jLabel51.setBounds(new Rectangle(208, 76, 81, 17));
			jLabel41 = new JLabel();
			jLabel41.setText("Slope:");
			jLabel41.setBounds(new Rectangle(208, 52, 81, 17));
			jLabel6 = new JLabel();
			jLabel6.setText("S vs T");
			jLabel6.setBounds(new Rectangle(208, 24, 105, 25));
			jLabel5 = new JLabel();
			jLabel5.setText("Intercept:");
			jLabel5.setBounds(new Rectangle(12, 76, 81, 17));
			jLabel4 = new JLabel();
			jLabel4.setText("Slope:");
			jLabel4.setBounds(new Rectangle(12, 52, 81, 17));
			jLabel3 = new JLabel();
			jLabel3.setText("<html>log(<i>k<sub>w</sub></i>) vs T</html>");
			jLabel3.setBounds(new Rectangle(12, 24, 105, 24));
			jLabel2 = new JLabel();
			jLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel2.setBounds(new Rectangle(212, 296, 57, 17));
			jLabel2.setText("\u00b5M");
			jLabel1 = new JLabel();
			jLabel1.setText("Concentration:");
			jLabel1.setBounds(new Rectangle(12, 296, 105, 17));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJbtnOk(), null);
			jContentPane.add(getJbtnCancel(), null);
			jContentPane.add(getJTabbedPane(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jcboCompound	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboCompound() 
	{
		if (jcboCompound == null) 
		{
			jcboCompound = new JComboBox(Globals.CompoundNameArray[m_iStationaryPhase]);
			jcboCompound.setFont(new Font("Dialog", Font.PLAIN, 12));
			jcboCompound.setBounds(new Rectangle(12, 32, 417, 26));
			jcboCompound.setBackground(Color.white);
		}
		
		return jcboCompound;
	}

	/**
	 * This method initializes jtxtConcentration1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtConcentration1() {
		if (jtxtConcentration1 == null) {
			jtxtConcentration1 = new JTextField();
			jtxtConcentration1.setText(floatFormatter.format(50.00));
			jtxtConcentration1.setBounds(new Rectangle(120, 292, 85, 26));
		}
		return jtxtConcentration1;
	}

	/**
	 * This method initializes jbtnOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnOk() {
		if (jbtnOk == null) {
			jbtnOk = new JButton();
			jbtnOk.setBounds(new Rectangle(176, 368, 125, 31));
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
			jbtnCancel.setBounds(new Rectangle(308, 368, 124, 31));
			jbtnCancel.setText("Cancel");
		}
		return jbtnCancel;
	}

	public void setSelectedCompound(int iCompoundIndex)
	{
		this.jTabbedPane.setSelectedIndex(0);
		jcboCompound.setSelectedIndex(iCompoundIndex);
	}

	public void setSelectedStationaryPhase(int iStationaryPhase)
	{
		this.m_iStationaryPhase = iStationaryPhase;
		DefaultComboBoxModel newModel = new DefaultComboBoxModel(Globals.CompoundNameArray[m_iStationaryPhase]); 
		this.jcboCompound.setModel(newModel);
	}
	
	public void setCustomCompoundProperties(Compound compound)
	{
		this.jTabbedPane.setSelectedIndex(1);
		this.jtxtCompoundName.setText(compound.strCompoundName);
		this.jtxtSlope1.setText(floatFormatter.format(compound.dLogkwvsTSlope[0]));    	
		this.jtxtSlope2.setText(floatFormatter.format(compound.dSvsTSlope[0]));    	
		this.jtxtSlope11.setText(floatFormatter.format(compound.dLogkwvsTSlope[1]));    	
		this.jtxtSlope21.setText(floatFormatter.format(compound.dSvsTSlope[1]));    	
		this.jtxtIntercept1.setText(floatFormatter.format(compound.dLogkwvsTIntercept[0]));    	
		this.jtxtIntercept2.setText(floatFormatter.format(compound.dSvsTIntercept[0]));    	
		this.jtxtIntercept11.setText(floatFormatter.format(compound.dLogkwvsTIntercept[1]));    	
		this.jtxtIntercept21.setText(floatFormatter.format(compound.dSvsTIntercept[1]));
		performValidations();
	}
	
	public void setCompoundConcentration(double dConcentration)
	{
		this.m_dConcentration1 = dConcentration;
		this.m_dConcentration2 = dConcentration;
		this.jtxtConcentration1.setText(floatFormatter.format(dConcentration));
		this.jtxtConcentration2.setText(floatFormatter.format(dConcentration));
	}
	
	private void setCompoundVariables()
	{
		this.m_iCompound = this.jcboCompound.getSelectedIndex();
		
		Compound thisCompound = new Compound();
		thisCompound.loadCompoundInfo(this.m_iStationaryPhase, m_iCompound);
		
		m_strCompoundName = thisCompound.strCompoundName;
		for (int i = 0; i < Globals.iNumSolvents; i++)
		{
			m_dLogkwvsTSlope[i] = thisCompound.dLogkwvsTSlope[i];
			m_dLogkwvsTIntercept[i] = thisCompound.dLogkwvsTIntercept[i];
			m_dSvsTSlope[i] = thisCompound.dSvsTSlope[i];
			m_dSvsTIntercept[i] = thisCompound.dSvsTIntercept[i];
		}
		
		m_dMolarVolume = thisCompound.dMolarVolume;

		NumberFormat formatter = new DecimalFormat("#0.0000");

		jlblSlope1.setText(formatter.format(m_dLogkwvsTSlope[0]));
		jlblIntercept1.setText(formatter.format(m_dLogkwvsTIntercept[0]));
		jlblSlope2.setText(formatter.format(m_dSvsTSlope[0]));
		jlblIntercept2.setText(formatter.format(m_dSvsTIntercept[0]));
		
		jlblSlope11.setText(formatter.format(m_dLogkwvsTSlope[1]));
		jlblIntercept11.setText(formatter.format(m_dLogkwvsTIntercept[1]));
		jlblSlope21.setText(formatter.format(m_dSvsTSlope[1]));
		jlblIntercept21.setText(formatter.format(m_dSvsTIntercept[1]));
		String test = formatter.format(m_dConcentration1);
		jtxtConcentration1.setText(formatter.format(m_dConcentration1));
	}
	
	//@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getActionCommand() == "comboBoxChanged")
		{
			setCompoundVariables();
		}
		else if (arg0.getActionCommand() == "OK")
		{
			m_iCompoundType = this.jTabbedPane.getSelectedIndex();
			
			if (m_iCompoundType == 0)
			{
				for (int i = 0; i < this.m_vectCompoundsUsed.size(); i++)
				{
					if (m_iCompound == m_vectCompoundsUsed.get(i).intValue())
					{
						
						JOptionPane.showMessageDialog(this,
								"That compound was already added. Please select a different compound.",
								"Compound already added",
								JOptionPane.INFORMATION_MESSAGE,
								null);
						return;
					}
				}
			}
			else if (m_iCompoundType > 0)
			{
				this.m_iCompound = -1;
				this.m_strCompoundName = this.jtxtCompoundName.getText();
			}
			
			m_bOk = true;
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
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.setBounds(new Rectangle(0, 8, 441, 353));
			jTabbedPane.setTabPlacement(JTabbedPane.TOP);
			jTabbedPane.addTab("Preloaded Compound", null, getJPanel2(), null);
			jTabbedPane.addTab("Custom Compound", null, getJPanel3(), null);
			jTabbedPane.addTab("Generate Matrix", null, getJPanel4(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jLabel31 = new JLabel();
			jLabel31.setBounds(new Rectangle(12, 12, 120, 17));
			jLabel31.setText("Compound:");
			jPanel2 = new JPanel();
			jPanel2.setLayout(null);
			jPanel2.add(getJcboCompound(), null);
			jPanel2.add(jLabel1, null);
			jPanel2.add(getJtxtConcentration1(), null);
			jPanel2.add(jLabel2, null);
			jPanel2.add(jLabel31, null);
			jPanel2.add(getJPanel5(), null);
			jPanel2.add(getJPanel51(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jLabel21 = new JLabel();
			jLabel21.setBounds(new Rectangle(212, 296, 54, 16));
			jLabel21.setText("\u00b5M");
			jLabel21.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblConcentration2 = new JLabel();
			jlblConcentration2.setBounds(new Rectangle(12, 296, 105, 16));
			jlblConcentration2.setText("Concentration:");
			jLabel4211 = new JLabel();
			jLabel4211.setText("Intercept:");
			jLabel4211.setBounds(new Rectangle(208, 76, 81, 17));
			jLabel422 = new JLabel();
			jLabel422.setText("Slope:");
			jLabel422.setBounds(new Rectangle(208, 52, 81, 17));
			jLabel421 = new JLabel();
			jLabel421.setText("Intercept:");
			jLabel421.setBounds(new Rectangle(12, 76, 81, 17));
			jLabel42 = new JLabel();
			jLabel42.setText("Slope:");
			jLabel42.setBounds(new Rectangle(12, 52, 81, 17));
			jLabel61 = new JLabel();
			jLabel61.setText("S vs T");
			jLabel61.setBounds(new Rectangle(208, 24, 105, 24));
			jLabel32 = new JLabel();
			jLabel32.setText("<html>log(<i>k<sub>w</sub></i>) vs T</html>");
			jLabel32.setBounds(new Rectangle(12, 24, 105, 24));
			jLabel311 = new JLabel();
			jLabel311.setBounds(new Rectangle(12, 12, 149, 16));
			jLabel311.setText("Compound name:");
			jPanel3 = new JPanel();
			jPanel3.setLayout(null);
			jPanel3.add(jLabel311, null);
			jPanel3.add(getJtxtCompoundName(), null);
			jPanel3.add(jlblConcentration2, null);
			jPanel3.add(getJtxtConcentration2(), null);
			jPanel3.add(jLabel21, null);
			jPanel3.add(getJPanel1(), null);
			jPanel3.add(getJPanel11(), null);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jLabel = new JLabel();
			jLabel.setText("<html><center>This option generates a set of custom compounds with random retention properties and random concentrations. Use it to simulate a random background matrix.</center></html>");
			jLabel.setBounds(new Rectangle(4, 4, 405, 57));
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel2111 = new JLabel();
			jLabel2111.setBounds(new Rectangle(392, 156, 37, 16));
			jLabel2111.setText("\u00b5M");
			jLabel2111.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel211 = new JLabel();
			jLabel211.setBounds(new Rectangle(392, 132, 38, 16));
			jLabel211.setText("\u00b5M");
			jLabel211.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblMaxConcentration1 = new JLabel();
			jlblMaxConcentration1.setBounds(new Rectangle(12, 156, 289, 16));
			jlblMaxConcentration1.setText("Maximum concentration:");
			jlblMaxConcentration = new JLabel();
			jlblMaxConcentration.setBounds(new Rectangle(12, 132, 289, 16));
			jlblMaxConcentration.setText("Minimum concentration:");
			jlblNumCompounds = new JLabel();
			jlblNumCompounds.setBounds(new Rectangle(12, 100, 289, 16));
			jlblNumCompounds.setText("Number of random matrix compounds to add:");
			jPanel4 = new JPanel();
			jPanel4.setLayout(null);
			jPanel4.add(jlblNumCompounds, null);
			jPanel4.add(getJtxtNumMatrixCompounds(), null);
			jPanel4.add(jlblMaxConcentration, null);
			jPanel4.add(getJtxtMaximumConcentration(), null);
			jPanel4.add(getJtxtMinimumConcentration(), null);
			jPanel4.add(jlblMaxConcentration1, null);
			jPanel4.add(jLabel211, null);
			jPanel4.add(jLabel2111, null);
			jPanel4.add(getJPanel6(), null);
		}
		return jPanel4;
	}

	/**
	 * This method initializes jtxtCompoundName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtCompoundName() {
		if (jtxtCompoundName == null) {
			jtxtCompoundName = new JTextField();
			jtxtCompoundName.setBounds(new Rectangle(12, 32, 413, 26));
			jtxtCompoundName.setText("Custom compound");
		}
		return jtxtCompoundName;
	}

	/**
	 * This method initializes jtxtSlope1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtSlope1() {
		if (jtxtSlope1 == null) {
			jtxtSlope1 = new JTextField();
			jtxtSlope1.setText(floatFormatter.format(-0.010042));
			jtxtSlope1.setBounds(new Rectangle(96, 48, 85, 26));
		}
		return jtxtSlope1;
	}

	/**
	 * This method initializes jtxtIntercept1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtIntercept1() {
		if (jtxtIntercept1 == null) {
			jtxtIntercept1 = new JTextField();
			jtxtIntercept1.setText(floatFormatter.format(2.738254));
			jtxtIntercept1.setBounds(new Rectangle(96, 72, 85, 26));
		}
		return jtxtIntercept1;
	}

	/**
	 * This method initializes jtxtSlope2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtSlope2() {
		if (jtxtSlope2 == null) {
			jtxtSlope2 = new JTextField();
			jtxtSlope2.setText(floatFormatter.format(0.009006));
			jtxtSlope2.setBounds(new Rectangle(292, 48, 85, 26));
		}
		return jtxtSlope2;
	}

	/**
	 * This method initializes jtxtIntercept2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtIntercept2() {
		if (jtxtIntercept2 == null) {
			jtxtIntercept2 = new JTextField();
			jtxtIntercept2.setText(floatFormatter.format(-3.39349));
			jtxtIntercept2.setBounds(new Rectangle(292, 72, 85, 26));
		}
		return jtxtIntercept2;
	}

	/**
	 * This method initializes jtxtConcentration2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtConcentration2() {
		if (jtxtConcentration2 == null) {
			jtxtConcentration2 = new JTextField();
			jtxtConcentration2.setBounds(new Rectangle(120, 292, 85, 26));
			jtxtConcentration2.setText(floatFormatter.format(50));
		}
		return jtxtConcentration2;
	}

	/**
	 * This method initializes jtxtNumMatrixCompounds	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtNumMatrixCompounds() {
		if (jtxtNumMatrixCompounds == null) {
			jtxtNumMatrixCompounds = new JTextField();
			jtxtNumMatrixCompounds.setBounds(new Rectangle(308, 96, 81, 26));
			jtxtNumMatrixCompounds.setText(floatFormatter.format(50));
		}
		return jtxtNumMatrixCompounds;
	}

	/**
	 * This method initializes jtxtMaximumConcentration	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtMaximumConcentration() {
		if (jtxtMaximumConcentration == null) {
			jtxtMaximumConcentration = new JTextField();
			jtxtMaximumConcentration.setBounds(new Rectangle(308, 152, 81, 26));
			jtxtMaximumConcentration.setText(floatFormatter.format(1000));
		}
		return jtxtMaximumConcentration;
	}

	/**
	 * This method initializes jtxtMinimumConcentration	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtMinimumConcentration() {
		if (jtxtMinimumConcentration == null) {
			jtxtMinimumConcentration = new JTextField();
			jtxtMinimumConcentration.setBounds(new Rectangle(308, 128, 81, 26));
			jtxtMinimumConcentration.setText(floatFormatter.format(.01));
		}
		return jtxtMinimumConcentration;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(null);
			jPanel1.setBounds(new Rectangle(12, 60, 413, 109));
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Acetonitrile/water", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel1.add(jLabel61, null);
			jPanel1.add(getJtxtSlope2(), null);
			jPanel1.add(jLabel42, null);
			jPanel1.add(jLabel422, null);
			jPanel1.add(jLabel4211, null);
			jPanel1.add(getJtxtSlope1(), null);
			jPanel1.add(getJtxtIntercept2(), null);
			jPanel1.add(getJtxtIntercept1(), null);
			jPanel1.add(jLabel32, null);
			jPanel1.add(jLabel421, null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel11	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel11() {
		if (jPanel11 == null) {
			jLabel4213 = new JLabel();
			jLabel4213.setBounds(new Rectangle(12, 76, 81, 17));
			jLabel4213.setText("Intercept:");
			jLabel322 = new JLabel();
			jLabel322.setBounds(new Rectangle(12, 24, 105, 24));
			jLabel322.setText("<html>log(<i>k<sub>w</sub></i>) vs T</html>");
			jLabel42111 = new JLabel();
			jLabel42111.setBounds(new Rectangle(208, 76, 81, 17));
			jLabel42111.setText("Intercept:");
			jLabel4221 = new JLabel();
			jLabel4221.setBounds(new Rectangle(208, 52, 81, 17));
			jLabel4221.setText("Slope:");
			jLabel424 = new JLabel();
			jLabel424.setBounds(new Rectangle(12, 52, 81, 17));
			jLabel424.setText("Slope:");
			jLabel611 = new JLabel();
			jLabel611.setBounds(new Rectangle(208, 24, 105, 24));
			jLabel611.setText("S vs T");
			jPanel11 = new JPanel();
			jPanel11.setLayout(null);
			jPanel11.setBounds(new Rectangle(12, 176, 413, 109));
			jPanel11.setBorder(BorderFactory.createTitledBorder(null, "Methanol/water", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel11.add(jLabel611, null);
			jPanel11.add(getJtxtSlope21(), null);
			jPanel11.add(jLabel424, null);
			jPanel11.add(jLabel4221, null);
			jPanel11.add(jLabel42111, null);
			jPanel11.add(getJtxtSlope11(), null);
			jPanel11.add(getJtxtIntercept21(), null);
			jPanel11.add(getJtxtIntercept11(), null);
			jPanel11.add(jLabel322, null);
			jPanel11.add(jLabel4213, null);
		}
		return jPanel11;
	}

	/**
	 * This method initializes jtxtSlope21	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtSlope21() {
		if (jtxtSlope21 == null) {
			jtxtSlope21 = new JTextField();
			jtxtSlope21.setBounds(new Rectangle(292, 48, 85, 26));
			jtxtSlope21.setText(floatFormatter.format(0.010959));
		}
		return jtxtSlope21;
	}

	/**
	 * This method initializes jtxtSlope11	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtSlope11() {
		if (jtxtSlope11 == null) {
			jtxtSlope11 = new JTextField();
			jtxtSlope11.setBounds(new Rectangle(96, 48, 85, 26));
			jtxtSlope11.setText(floatFormatter.format(-0.013100));
		}
		return jtxtSlope11;
	}

	/**
	 * This method initializes jtxtIntercept21	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtIntercept21() {
		if (jtxtIntercept21 == null) {
			jtxtIntercept21 = new JTextField();
			jtxtIntercept21.setBounds(new Rectangle(292, 72, 85, 26));
			jtxtIntercept21.setText(floatFormatter.format(-3.547073));
		}
		return jtxtIntercept21;
	}

	/**
	 * This method initializes jtxtIntercept11	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtIntercept11() {
		if (jtxtIntercept11 == null) {
			jtxtIntercept11 = new JTextField();
			jtxtIntercept11.setBounds(new Rectangle(96, 72, 85, 26));
			jtxtIntercept11.setText(floatFormatter.format(3.230992));
		}
		return jtxtIntercept11;
	}

	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			jPanel5 = new JPanel();
			jPanel5.setLayout(null);
			jPanel5.setBounds(new Rectangle(12, 60, 413, 109));
			jPanel5.setBorder(BorderFactory.createTitledBorder(null, "Acetonitrile/water", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel5.add(jLabel3, null);
			jPanel5.add(jLabel4, null);
			jPanel5.add(jlblSlope1, null);
			jPanel5.add(jLabel5, null);
			jPanel5.add(jlblIntercept1, null);
			jPanel5.add(jLabel6, null);
			jPanel5.add(jLabel41, null);
			jPanel5.add(jlblSlope2, null);
			jPanel5.add(jLabel51, null);
			jPanel5.add(jlblIntercept2, null);
		}
		return jPanel5;
	}

	/**
	 * This method initializes jPanel51	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel51() {
		if (jPanel51 == null) {
			jlblIntercept21 = new JLabel();
			jlblIntercept21.setBounds(new Rectangle(296, 76, 81, 16));
			jlblIntercept21.setText(floatFormatter.format(-4.492));
			jlblIntercept21.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel511 = new JLabel();
			jLabel511.setBounds(new Rectangle(208, 76, 81, 17));
			jLabel511.setText("Intercept:");
			jlblSlope21 = new JLabel();
			jlblSlope21.setBounds(new Rectangle(296, 52, 81, 16));
			jlblSlope21.setText(floatFormatter.format(0.0049));
			jlblSlope21.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel411 = new JLabel();
			jLabel411.setBounds(new Rectangle(208, 52, 81, 17));
			jLabel411.setText("Slope:");
			jLabel63 = new JLabel();
			jLabel63.setBounds(new Rectangle(208, 24, 105, 25));
			jLabel63.setText("S vs T");
			jlblIntercept11 = new JLabel();
			jlblIntercept11.setBounds(new Rectangle(100, 76, 81, 16));
			jlblIntercept11.setText(floatFormatter.format(1.775));
			jlblIntercept11.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel52 = new JLabel();
			jLabel52.setBounds(new Rectangle(12, 76, 81, 17));
			jLabel52.setText("Intercept:");
			jlblSlope11 = new JLabel();
			jlblSlope11.setBounds(new Rectangle(100, 52, 81, 16));
			jlblSlope11.setText(floatFormatter.format(-0.0092));
			jlblSlope11.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel43 = new JLabel();
			jLabel43.setBounds(new Rectangle(12, 52, 81, 17));
			jLabel43.setText("Slope:");
			jLabel33 = new JLabel();
			jLabel33.setBounds(new Rectangle(12, 24, 105, 24));
			jLabel33.setText("<html>log(<i>k<sub>w</sub></i>) vs T</html>");
			jPanel51 = new JPanel();
			jPanel51.setLayout(null);
			jPanel51.setBounds(new Rectangle(12, 176, 413, 109));
			jPanel51.setBorder(BorderFactory.createTitledBorder(null, "Methanol/water", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel51.add(jLabel33, null);
			jPanel51.add(jLabel43, null);
			jPanel51.add(jlblSlope11, null);
			jPanel51.add(jLabel52, null);
			jPanel51.add(jlblIntercept11, null);
			jPanel51.add(jLabel63, null);
			jPanel51.add(jLabel411, null);
			jPanel51.add(jlblSlope21, null);
			jPanel51.add(jLabel511, null);
			jPanel51.add(jlblIntercept21, null);
		}
		return jPanel51;
	}

	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			jPanel6 = new JPanel();
			jPanel6.setLayout(null);
			jPanel6.setBounds(new Rectangle(12, 16, 413, 65));
			jPanel6.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jPanel6.add(jLabel, null);
		}
		return jPanel6;
	}

	@Override
	public void keyPressed(KeyEvent arg0) 
	{

	}

	@Override
	public void keyReleased(KeyEvent arg0) 
	{

	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (!((Character.isDigit(e.getKeyChar()) ||
				(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
				(e.getKeyChar() == KeyEvent.VK_DELETE) ||
				(e.getKeyChar() == KeyEvent.VK_PERIOD) ||
				(e.getKeyChar() == KeyEvent.VK_COMMA))))
		{
	        e.consume();
		}
		
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
		{
			performValidations();
		}
		
	}

	@Override
	public void focusGained(FocusEvent arg0) 
	{

	}

	@Override
	public void focusLost(FocusEvent arg0) 
	{
		performValidations();		
	}

	public void performValidations()
	{
		try {
			validateConcentration1();
			validateConcentration2();
			validateSlope1();
			validateSlope2();
			validateSlope11();
			validateSlope21();
			validateIntercept1();
			validateIntercept2();
			validateIntercept11();
			validateIntercept21();
			validateNumMatrixCompounds();
			validateMinConcentration();
			validateMaxConcentration();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void validateConcentration1() throws ParseException
	{
		if (jtxtConcentration1.getText().length() == 0)
			jtxtConcentration1.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtConcentration1.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dConcentration1 = dTemp.doubleValue();
		jtxtConcentration1.setText(floatFormatter.format(m_dConcentration1));    	
	}
	
	public void validateConcentration2() throws ParseException
	{
		if (jtxtConcentration2.getText().length() == 0)
			jtxtConcentration2.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtConcentration2.getText());
		
    	if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dConcentration2 = dTemp.doubleValue();
		jtxtConcentration2.setText(floatFormatter.format(m_dConcentration2));    	
	}
	
	public void validateSlope1() throws ParseException
	{
		if (jtxtSlope1.getText().length() == 0)
			jtxtSlope1.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtSlope1.getText());
		
		if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dLogkwvsTSlope[0] = dTemp.doubleValue();
		jtxtSlope1.setText(floatFormatter.format(m_dLogkwvsTSlope[0]));    	
	}
	
	public void validateSlope2() throws ParseException
	{
		if (jtxtSlope2.getText().length() == 0)
			jtxtSlope2.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtSlope2.getText());
		
    	if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dSvsTSlope[0] = dTemp.doubleValue();
		jtxtSlope2.setText(floatFormatter.format(m_dSvsTSlope[0]));    	
	}
	
	public void validateSlope11() throws ParseException
	{
		if (jtxtSlope11.getText().length() == 0)
			jtxtSlope11.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtSlope11.getText());
		
		if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dLogkwvsTSlope[1] = dTemp.doubleValue();
		jtxtSlope11.setText(floatFormatter.format(m_dLogkwvsTSlope[1]));    	
	}
	
	public void validateSlope21() throws ParseException
	{
		if (jtxtSlope21.getText().length() == 0)
			jtxtSlope21.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtSlope21.getText());
		
    	if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dSvsTSlope[1] = dTemp.doubleValue();
		jtxtSlope21.setText(floatFormatter.format(m_dSvsTSlope[1]));    	
	}
	
	public void validateIntercept1() throws ParseException
	{
		if (jtxtIntercept1.getText().length() == 0)
			jtxtIntercept1.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtIntercept1.getText());
		
		if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dLogkwvsTIntercept[0] = dTemp.doubleValue();
		jtxtIntercept1.setText(floatFormatter.format(m_dLogkwvsTIntercept[0]));    	
	}
	
	public void validateIntercept2() throws ParseException
	{
		if (jtxtIntercept2.getText().length() == 0)
			jtxtIntercept2.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtIntercept2.getText());
		
		if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dSvsTIntercept[0] = dTemp.doubleValue();
		jtxtIntercept2.setText(floatFormatter.format(m_dSvsTIntercept[0]));    	
	}
	
	public void validateIntercept11() throws ParseException
	{
		if (jtxtIntercept11.getText().length() == 0)
			jtxtIntercept11.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtIntercept11.getText());
		
		if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dLogkwvsTIntercept[1] = dTemp.doubleValue();
		jtxtIntercept11.setText(floatFormatter.format(m_dLogkwvsTIntercept[1]));    	
	}
	
	public void validateIntercept21() throws ParseException
	{
		if (jtxtIntercept21.getText().length() == 0)
			jtxtIntercept21.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtIntercept21.getText());
		
		if (dTemp.doubleValue() < -100000)
			dTemp = -100000;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		
		this.m_dSvsTIntercept[1] = dTemp.doubleValue();
		jtxtIntercept21.setText(floatFormatter.format(m_dSvsTIntercept[1]));    	
	}
	
	public void validateNumMatrixCompounds() throws ParseException
	{
		if (jtxtNumMatrixCompounds.getText().length() == 0)
			jtxtNumMatrixCompounds.setText("0");

		NumberFormat intFormatter = new DecimalFormat("#0");
    	Number iTemp = intFormatter.parse(jtxtNumMatrixCompounds.getText());
		
		if (iTemp.intValue() < 1)
			iTemp = 1;
		if (iTemp.intValue() > 1000)
			iTemp = 1000;
		
		this.m_iNumMatrixCompounds = iTemp.intValue();
		jtxtNumMatrixCompounds.setText(intFormatter.format(m_iNumMatrixCompounds));    	
	}
	
	public void validateMinConcentration() throws ParseException
	{
		if (jtxtMinimumConcentration.getText().length() == 0)
			jtxtMinimumConcentration.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtMinimumConcentration.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		if (dTemp.doubleValue() > this.m_dMaximumConcentration)
			dTemp = this.m_dMaximumConcentration;
		
		this.m_dMinimumConcentration = dTemp.doubleValue();
		jtxtMinimumConcentration.setText(floatFormatter.format(m_dMinimumConcentration));    	
	}

	public void validateMaxConcentration() throws ParseException
	{
		if (jtxtMaximumConcentration.getText().length() == 0)
			jtxtMaximumConcentration.setText("0");

    	Number dTemp = floatFormatter.parse(jtxtMaximumConcentration.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 100000)
			dTemp = 100000;
		if (dTemp.doubleValue() < this.m_dMinimumConcentration)
			dTemp = this.m_dMinimumConcentration;
		
		this.m_dMaximumConcentration = dTemp.doubleValue();
		jtxtMaximumConcentration.setText(floatFormatter.format(m_dMaximumConcentration));    	
	}

	@Override
	public void stateChanged(ChangeEvent arg0) 
	{
		performValidations();
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
