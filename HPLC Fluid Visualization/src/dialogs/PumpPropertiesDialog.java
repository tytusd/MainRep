package dialogs;

import javax.swing.JPanel;
import java.awt.Frame;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import java.awt.Component;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;
import java.awt.Dimension;
import java.lang.String;
import java.awt.GridBagLayout;
import java.awt.Point;

public class PumpPropertiesDialog extends JDialog implements ActionListener, KeyListener, FocusListener, TableModelListener
{
	private static final long serialVersionUID = 1L;
	
	public static final String[] SolventASolvents = {"Water"};
	public static final String[] SolventBSolvents = {"Methanol", "Acetonitrile"};
	public static final String[] PressureUnits = {"bar", "psi"};
	private JPanel jContentPane = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel = null;
	private JTextField jtxtName = null;
	public boolean m_bOk = false;
	public Color m_fluidColor = Color.red;  //  @jve:decl-index=0:
	private int m_iSolventA = 0;
	private int m_iSolventB = 0;
	private double m_dPressureLimit = 1000;
	private double m_dPumpVolume = 50;
	private double m_dFlowRate = 400;
	private ArrayList<double[]> m_gradientArray= new ArrayList<double[]>();  //  @jve:decl-index=0:
	
	boolean m_bIsBar = true;
	boolean m_bDoNotChangeTable = false;
	
	public String m_strName = "Default pump";
	
	private JTextField m_ControlChanged = null;
	private JLabel jLabel1 = null;
	private JButton jbtnChooseColor = null;
	private JPanel jColorPanel = null;
	
	public SpecialTableModel tmGradientProgram;
	private JScrollPane jScrollPane = null;
	public JTable jtableGradientProgram = null;
	public JButton jbtnInsertRow = null;
	public JButton jbtnRemoveRow = null;
	public JTextField jtxtPumpVolume = null;
	public JLabel jlblMixingVolumeUnit = null;
	public JLabel jlblPreColumnVolume = null;
	private JLabel jlblProgram = null;
	private JLabel jlblSolventA = null;
	private JLabel jlblSolventB = null;
	private JComboBox jcboSolventA = null;
	private JComboBox jcboSolventB = null;
	private JLabel jlblMaxPressure = null;
	private JTextField jtxtPressureLimit = null;
	private JComboBox jcboPressureUnits = null;
	private JPanel jPanel = null;
	private JPanel jGeneralPanel = null;
	private JPanel jPumpSettingsPanel = null;

	private JPanel jPanel1 = null;

	private JLabel jlblFlowRate = null;

	private JTextField jtxtFlowRate = null;

	private JLabel jlblFlowRateUnits = null;
	
	public void setFlowRate(double dFlowRate)
	{
		this.jtxtFlowRate.setText(Float.toString((float)dFlowRate));
		performCalculations();
	}
	
	public double getFlowRate()
	{
		return this.m_dFlowRate;
	}

	public void setSolventA(int iSolventA)
	{
		this.jcboSolventA.setSelectedIndex(iSolventA);
		this.m_iSolventA = iSolventA;
	}
	
	public int getSolventA()
	{
		return this.m_iSolventA;
	}
	
	public void setSolventB(int iSolventB)
	{
		this.jcboSolventB.setSelectedIndex(iSolventB);
		this.m_iSolventB = iSolventB;
	}
	
	public int getSolventB()
	{
		return this.m_iSolventB;
	}
	
	public void setFluidColor(Color newColor)
	{
		this.m_fluidColor = newColor;
		jColorPanel.setBackground(this.m_fluidColor);
	}
	
	public Color getFluidColor()
	{
		return this.m_fluidColor;
	}
	
	public void setPumpVolume(double dPumpVolume)
	{
		this.jtxtPumpVolume.setText(Float.toString((float)dPumpVolume));
		performCalculations();
	}
	
	public double getPumpVolume()
	{
		return this.m_dPumpVolume;
	}
	
	public void setPressureLimit(double dPressureLimit)
	{
		this.jtxtPressureLimit.setText(Float.toString((float)dPressureLimit));
		performCalculations();
	}
	
	public double getPressureLimit()
	{
		return this.m_dPressureLimit;
	}
	
	public void setGradientProgram(ArrayList<double[]> gradientProgram)
	{
		this.m_gradientArray = gradientProgram;
		this.tmGradientProgram.setRowCount(0);
		for (int i = 0; i < gradientProgram.size(); i++)
		{
			Vector<Double> newRow = new Vector<Double>();
			newRow.add(gradientProgram.get(i)[0]);			
			newRow.add(gradientProgram.get(i)[1]);
			tmGradientProgram.addRow(newRow);
		}
		performCalculations();
	}
	
	public ArrayList<double[]> getGradientProgram()
	{
		m_gradientArray = new ArrayList<double[]>();
		for (int i = 0; i < tmGradientProgram.getRowCount(); i++)
		{
			double[] newRow = new double[2];
			newRow[0] = (Double)tmGradientProgram.getValueAt(i, 0);
			newRow[1] = (Double)tmGradientProgram.getValueAt(i, 1);
			m_gradientArray.add(newRow);
		}
		return this.m_gradientArray;
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
    
    private void validateFlowRate()
    {
    	if (this.jtxtFlowRate.getText() == null)
    		this.jtxtFlowRate.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtFlowRate.getText());
		
		if (dTemp < .000001)
			dTemp = .000001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dFlowRate = dTemp;
		this.jtxtFlowRate.setText(Float.toString((float)dTemp));    	
    }  
    
    private void validatePressureLimit()
    {
    	if (this.jtxtPressureLimit.getText() == null)
    		this.jtxtPressureLimit.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtPressureLimit.getText());
		
		if (dTemp < 1)
			dTemp = 1;
		if (dTemp > 999999)
			dTemp = 999999;
		
		if (this.jcboPressureUnits.getSelectedIndex() == 0)
		{
			this.m_dPressureLimit = dTemp;
		}
		else
		{
			this.m_dPressureLimit = dTemp / 14.5037738;
		}
		
		this.jtxtPressureLimit.setText(Float.toString((float)dTemp));    	
    }   

    private void validatePumpVolume()
    {
    	if (this.jtxtPumpVolume.getText() == null)
    		this.jtxtPumpVolume.setText("0");

    	double dTemp = (double)Float.valueOf(this.jtxtPumpVolume.getText());
		
		if (dTemp < .00001)
			dTemp = .00001;
		if (dTemp > 999999)
			dTemp = 999999;
		
		this.m_dPumpVolume = dTemp;
		this.jtxtPumpVolume.setText(Float.toString((float)m_dPumpVolume));    	
    }   
    
	/**
	 * @param owner
	 */
	public PumpPropertiesDialog(Frame owner) 
	{
		super(owner);
		initialize();
		
		jbtnOk.addActionListener(this);
		jbtnCancel.addActionListener(this);
		jbtnChooseColor.addActionListener(this);
        jtxtName.addKeyListener(this);
        jtxtName.addFocusListener(this);
        jtxtPumpVolume.addKeyListener(this);
        jtxtPumpVolume.addFocusListener(this);
        jtxtFlowRate.addKeyListener(this);
        jtxtFlowRate.addFocusListener(this);
        jtxtPressureLimit.addKeyListener(this);
        jtxtPressureLimit.addFocusListener(this);
        jbtnInsertRow.addActionListener(this);
        jbtnRemoveRow.addActionListener(this);
        tmGradientProgram.addTableModelListener(this);
        jcboPressureUnits.addActionListener(this);
        jcboSolventA.addActionListener(this);
        jcboSolventB.addActionListener(this);
        
        jcboSolventA.setModel(new DefaultComboBoxModel(SolventASolvents));
        jcboSolventB.setModel(new DefaultComboBoxModel(SolventBSolvents));
        jcboPressureUnits.setModel(new DefaultComboBoxModel(PressureUnits));

        performCalculations();

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(593, 324);
		this.setContentPane(getJContentPane());
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("Pump Properties");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jlblMaxPressure = new JLabel();
			jlblMaxPressure.setText("Pressure limit:");
			jlblMaxPressure.setBounds(new Rectangle(8, 52, 97, 16));
			jlblSolventB = new JLabel();
			jlblSolventB.setText("Solvent B:");
			jlblSolventB.setBounds(new Rectangle(12, 52, 85, 16));
			jlblSolventA = new JLabel();
			jlblSolventA.setText("Solvent A:");
			jlblSolventA.setBounds(new Rectangle(12, 24, 85, 16));
			jlblProgram = new JLabel();
			jlblProgram.setText("Program:");
			jlblProgram.setBounds(new Rectangle(12, 82, 137, 16));
			jLabel1 = new JLabel();
			jLabel1.setText("Fluid color:");
			jLabel1.setBounds(new Rectangle(8, 52, 75, 16));
			jLabel = new JLabel();
			jLabel.setText("Name:");
			jLabel.setBounds(new Rectangle(8, 24, 48, 16));
	        jlblPreColumnVolume = new JLabel();
	        jlblPreColumnVolume.setText("Pump volume:");
	        jlblPreColumnVolume.setBounds(new Rectangle(8, 24, 97, 16));
	        jlblMixingVolumeUnit = new JLabel();
	        jlblMixingVolumeUnit.setPreferredSize(new Dimension(50, 16));
	        jlblMixingVolumeUnit.setText("\u00b5L");
	        jlblMixingVolumeUnit.setBounds(new Rectangle(216, 24, 29, 16));
	        jlblMixingVolumeUnit.setFont(new Font("Dialog", Font.PLAIN, 12));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBackground(new Color(238, 238, 238));
			jContentPane.add(getJbtnOk(), null);
			jContentPane.add(getJbtnCancel(), null);
	        jContentPane.add(getJPanel(), null);
	        jContentPane.add(getJGeneralPanel(), null);
	        jContentPane.add(getJPumpSettingsPanel(), null);
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
			jbtnOk.setBounds(new Rectangle(320, 256, 125, 31));
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
			jbtnCancel.setBounds(new Rectangle(456, 256, 125, 31));
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
		else if (arg0.getActionCommand() == "pickNew")
		{
			 Color newColor = JColorChooser.showDialog(this, "Choose Fluid Color", this.m_fluidColor);
			 if (newColor != null)
			 {
				 this.setFluidColor(newColor);
			 }
		}
		else if (arg0.getActionCommand() == "SolventA")
		{
	    	m_iSolventA = jcboSolventA.getSelectedIndex();
	    	// Update all the indicators
	    	performCalculations();
		}
		else if (arg0.getActionCommand() == "SolventB")
		{
	    	m_iSolventB = jcboSolventB.getSelectedIndex();
	    	// Update all the indicators
	    	performCalculations();
		}
	    else if (arg0.getActionCommand() == "Insert Row")
	    {
	    	int iSelectedRow = jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = tmGradientProgram.getRowCount()-1;
	    	
	    	Double dRowValue1 = (Double) tmGradientProgram.getValueAt(iSelectedRow, 0);
	    	Double dRowValue2 = (Double) tmGradientProgram.getValueAt(iSelectedRow, 1);
	    	Double dRowData[] = {dRowValue1, dRowValue2};
	    	tmGradientProgram.insertRow(iSelectedRow+1, dRowData);
	    }
	    else if (arg0.getActionCommand() == "Remove Row")
	    {
	    	int iSelectedRow = jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = tmGradientProgram.getRowCount()-1;
	    	
	    	if (tmGradientProgram.getRowCount() >= 3)
	    	{
	    		tmGradientProgram.removeRow(iSelectedRow);
	    	}
	    }
	    else if (arg0.getActionCommand() == "PressureUnits")
	    {
	    	if (this.jcboPressureUnits.getSelectedIndex() == 0 && this.m_bIsBar == false)
	    	{
	    		// Convert from psi to bar
	    		this.jtxtPressureLimit.setText(Float.toString((float)(this.m_dPressureLimit)));
	    		this.m_bIsBar = true;
	    		performCalculations();
	    	}
	    	else if (this.jcboPressureUnits.getSelectedIndex() == 1 && this.m_bIsBar == true)
	    	{
	    		// Convert from bar to psi
	    		this.jtxtPressureLimit.setText(Float.toString((float)(this.m_dPressureLimit * 14.5037738)));
	    		this.m_bIsBar = false;
	    		performCalculations();
	    	}
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
			jtxtName.setBounds(new Rectangle(116, 24, 153, 20));
			jtxtName.setText("Default pump");
		}
		return jtxtName;
	}

	public void performCalculations()
	{
		validateName();
		validatePressureLimit();
		validatePumpVolume();
		validateFlowRate();

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
	 * This method initializes jbtnChooseColor	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnChooseColor() {
		if (jbtnChooseColor == null) {
			jbtnChooseColor = new JButton();
			jbtnChooseColor.setActionCommand("pickNew");
			jbtnChooseColor.setBounds(new Rectangle(164, 48, 105, 25));
			jbtnChooseColor.setText("Pick new...");
		}
		return jbtnChooseColor;
	}

	/**
	 * This method initializes jColorPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJColorPanel() {
		if (jColorPanel == null) {
			jColorPanel = new JPanel();
			jColorPanel.setLayout(new GridBagLayout());
			jColorPanel.setBackground(Color.red);
			jColorPanel.setBounds(new Rectangle(116, 52, 41, 17));
		}
		return jColorPanel;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(12, 102, 261, 93));
			jScrollPane.setViewportView(getJtableGradientProgram());
		}
		return jScrollPane;
	}

	public class SpecialTableModel extends DefaultTableModel 
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 9144486981092084762L;

		public SpecialTableModel(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public SpecialTableModel(final Object[][] data, final Object[] columnNames) 
	    {
	        setDataVector(data, columnNames);
	    }

	    public boolean isCellEditable(int row, int column) 
	    {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        /*if (column < 1) {
	            return false;
	        } else {
	            return true;
	        }*/
	    	return true;
	    }
	    
	    /*
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    @SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	}
	
	/**
	 * This method initializes jtableGradientProgram	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJtableGradientProgram() {
		if (jtableGradientProgram == null) 
		{
			Object[] columnNames = {"Time (min)", "% B"};
			Double[][] data = {{0.0, 5.0},{5.0, 95.0}};
	        
			tmGradientProgram = new SpecialTableModel(data, columnNames);

			jtableGradientProgram = new JTable(tmGradientProgram);

			jtableGradientProgram.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtableGradientProgram.getTableHeader().setPreferredSize(new Dimension(22, 22));
			jtableGradientProgram.getColumnModel().getColumn(0).setPreferredWidth(150);
		
			JTextField jtf = new JTextField();
			TableCellEditorCustom cellEditor = new TableCellEditorCustom(jtf);
			jtableGradientProgram.getColumnModel().getColumn(1).setCellEditor(cellEditor);

		}
		return jtableGradientProgram;
	}

	/**
	 * This method initializes jbtnInsertRow	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnInsertRow() {
		if (jbtnInsertRow == null) {
			jbtnInsertRow = new JButton();
			jbtnInsertRow.setActionCommand("Insert Row");
			jbtnInsertRow.setBounds(new Rectangle(12, 204, 125, 25));
			jbtnInsertRow.setText("Insert Row");
		}
		return jbtnInsertRow;
	}

	/**
	 * This method initializes jbtnRemoveRow	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnRemoveRow() {
		if (jbtnRemoveRow == null) {
			jbtnRemoveRow = new JButton();
			jbtnRemoveRow.setActionCommand("Remove Row");
			jbtnRemoveRow.setBounds(new Rectangle(148, 204, 124, 25));
			jbtnRemoveRow.setText("Remove Row");
		}
		return jbtnRemoveRow;
	}

	/**
	 * This method initializes jtxtPumpVolume	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPumpVolume() {
		if (jtxtPumpVolume == null) {
			jtxtPumpVolume = new JTextField();
			jtxtPumpVolume.setHorizontalAlignment(JTextField.TRAILING);
			jtxtPumpVolume.setBounds(new Rectangle(116, 22, 93, 20));
			jtxtPumpVolume.setText("50");
		}
		return jtxtPumpVolume;
	}

	/**
	 * This method initializes jcboSolventA	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboSolventA() {
		if (jcboSolventA == null) {
			jcboSolventA = new JComboBox();
			jcboSolventA.setActionCommand("SolventA");
			jcboSolventA.setBounds(new Rectangle(120, 20, 153, 23));
		}
		return jcboSolventA;
	}

	/**
	 * This method initializes jcboSolventB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboSolventB() {
		if (jcboSolventB == null) {
			jcboSolventB = new JComboBox();
			jcboSolventB.setActionCommand("SolventB");
			jcboSolventB.setBounds(new Rectangle(120, 48, 153, 23));
		}
		return jcboSolventB;
	}

	/**
	 * This method initializes jtxtPressureLimit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPressureLimit() {
		if (jtxtPressureLimit == null) {
			jtxtPressureLimit = new JTextField();
			jtxtPressureLimit.setHorizontalAlignment(JTextField.TRAILING);
			jtxtPressureLimit.setBounds(new Rectangle(116, 52, 93, 20));
			jtxtPressureLimit.setText("1000");
		}
		return jtxtPressureLimit;
	}

	/**
	 * This method initializes jcboPressureUnits	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboPressureUnits() {
		if (jcboPressureUnits == null) {
			jcboPressureUnits = new JComboBox();
			jcboPressureUnits.setActionCommand("PressureUnits");
			jcboPressureUnits.setBounds(new Rectangle(216, 50, 53, 23));
		}
		return jcboPressureUnits;
	}

	@Override
	public void tableChanged(TableModelEvent e) 
	{
		if (e.getSource() == tmGradientProgram)
		{
			if (m_bDoNotChangeTable)
			{
				m_bDoNotChangeTable = false;
				return;
			}
			
			int iChangedRow = e.getFirstRow();
			int iChangedColumn = e.getColumn();

			Double dRowValue1 = 0.0;
			Double dRowValue2 = 0.0;
			
			if (iChangedRow < tmGradientProgram.getRowCount())
			{
				dRowValue1 = (Double) tmGradientProgram.getValueAt(iChangedRow, 0);
				dRowValue2 = (Double) tmGradientProgram.getValueAt(iChangedRow, 1);
			}
			
	    	if (iChangedColumn == 0)
			{
				// If the column changed was the first, then make sure the time falls in the right range
				if (iChangedRow == 0)
				{
					// No changes allowed in first row - must be zero min
					dRowValue1 = 0.0;
				}
				else if (iChangedRow == tmGradientProgram.getRowCount() - 1)
				{
					Double dPreviousTime = (Double) tmGradientProgram.getValueAt(tmGradientProgram.getRowCount() - 2, 0);
					// If it's the last row, just make sure the time is greater than or equal to the time before it.
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
				}
				else
				{
					Double dPreviousTime = (Double) tmGradientProgram.getValueAt(iChangedRow - 1, 0);
					Double dNextTime = (Double) tmGradientProgram.getValueAt(iChangedRow + 1, 0);
					
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
					
					if (dRowValue1 > dNextTime)
						dRowValue1 = dNextTime;
				}
				
		    	m_bDoNotChangeTable = true;
		    	tmGradientProgram.setValueAt(dRowValue1, iChangedRow, iChangedColumn);
			}
			else if (iChangedColumn == 1)
			{
				// If the column changed was the second, then make sure the solvent composition falls between 0 and 100
				if (dRowValue2 > 100)
					dRowValue2 = 100.0;
				
				if (dRowValue2 < 0)
					dRowValue2 = 0.0;
				
		    	m_bDoNotChangeTable = true;
		    	tmGradientProgram.setValueAt(dRowValue2, iChangedRow, iChangedColumn);
			}
	    	
	    	performCalculations();
		}				
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(296, 8, 285, 241));
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "Gradient program", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.add(jlblProgram, null);
			jPanel.add(jlblSolventB, null);
			jPanel.add(getJScrollPane(), null);
			jPanel.add(jlblSolventA, null);
			jPanel.add(getJbtnRemoveRow(), null);
			jPanel.add(getJcboSolventB(), null);
			jPanel.add(getJbtnInsertRow(), null);
			jPanel.add(getJcboSolventA(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jGeneralPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJGeneralPanel() {
		if (jGeneralPanel == null) {
			jGeneralPanel = new JPanel();
			jGeneralPanel.setLayout(null);
			jGeneralPanel.setBounds(new Rectangle(8, 8, 281, 85));
			jGeneralPanel.setBorder(BorderFactory.createTitledBorder(null, "General options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jGeneralPanel.add(jLabel, null);
			jGeneralPanel.add(getJtxtName(), null);
			jGeneralPanel.add(jLabel1, null);
			jGeneralPanel.add(getJColorPanel(), null);
			jGeneralPanel.add(getJbtnChooseColor(), null);
		}
		return jGeneralPanel;
	}

	/**
	 * This method initializes jPumpSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPumpSettingsPanel() {
		if (jPumpSettingsPanel == null) {
			jPumpSettingsPanel = new JPanel();
			jPumpSettingsPanel.setLayout(null);
			jPumpSettingsPanel.setBounds(new Rectangle(8, 164, 281, 85));
			jPumpSettingsPanel.setBorder(BorderFactory.createTitledBorder(null, "Pump properties", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPumpSettingsPanel.add(jlblPreColumnVolume, null);
			jPumpSettingsPanel.add(getJtxtPumpVolume(), null);
			jPumpSettingsPanel.add(jlblMixingVolumeUnit, null);
			jPumpSettingsPanel.add(jlblMaxPressure, null);
			jPumpSettingsPanel.add(getJtxtPressureLimit(), null);
			jPumpSettingsPanel.add(getJcboPressureUnits(), null);
		}
		return jPumpSettingsPanel;
	}
	
	// This TableCellEditor, if a JTextField, automatically selects all the text when it is created.
	// This makes it so that when you type something into the cell, it removes whatever was there
	// It does not make it select all on double-click
	class TableCellEditorCustom extends DefaultCellEditor
	{
		public TableCellEditorCustom(JTextField textField) {
			super(textField);
			// TODO Auto-generated constructor stub
		}
		
		@Override
	    public Component getTableCellEditorComponent(JTable table, Object value,
				 boolean isSelected,
				 int row, int column) 
	    {
    		java.awt.Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    		if (c instanceof javax.swing.JTextField)
    		{
    			JTextField jtf = ((javax.swing.JTextField)c);
    			jtf.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));
    			//Rectangle rect = table.getCellRect(row, column, false);
    			//jtf.setSize(rect.width, rect.height);
    			jtf.setHorizontalAlignment(JTextField.RIGHT);
    			jtf.selectAll();
    			//jtf.setText("");
    			//jtf.setCaretPosition(0);
    		}
    		return c;
	    }
	    
		@Override
	    public Object getCellEditorValue() 
		{
	        return (Double)(double)Float.valueOf((String)delegate.getCellEditorValue());
	    }
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jlblFlowRateUnits = new JLabel();
			jlblFlowRateUnits.setPreferredSize(new Dimension(50, 16));
			jlblFlowRateUnits.setText("µL/min");
			jlblFlowRateUnits.setSize(new Dimension(50, 16));
			jlblFlowRateUnits.setLocation(new Point(216, 26));
			jlblFlowRateUnits.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblFlowRate = new JLabel();
			jlblFlowRate.setText("Flow rate:");
			jlblFlowRate.setLocation(new Point(8, 26));
			jlblFlowRate.setSize(new Dimension(80, 16));
			jPanel1 = new JPanel();
			jPanel1.setLayout(null);
			jPanel1.setBounds(new Rectangle(8, 100, 281, 57));
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Flow rate", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel1.add(jlblFlowRate, null);
			jPanel1.add(getJtxtFlowRate(), null);
			jPanel1.add(jlblFlowRateUnits, null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jtxtFlowRate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtFlowRate() {
		if (jtxtFlowRate == null) {
			jtxtFlowRate = new JTextField();
			jtxtFlowRate.setHorizontalAlignment(JTextField.TRAILING);
			jtxtFlowRate.setLocation(new Point(116, 24));
			jtxtFlowRate.setSize(new Dimension(93, 20));
			jtxtFlowRate.setText("400");
		}
		return jtxtFlowRate;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
