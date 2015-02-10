package org.hplcretentionpredictor;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import java.awt.Point;
import javax.swing.JButton;
import java.awt.Dimension;
import java.util.EventObject;
import java.util.Vector;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;

import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
import org.hplcretentionpredictor.TopPanel.JChemicalTable;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;

import boswell.graphcontrol.GraphControl;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import java.awt.Insets;
import java.awt.GridLayout;

public class TopPanel extends JPanel implements Scrollable, ComponentListener
{
	private static final long serialVersionUID = 1L;
	
	public GraphControl m_GraphControlGradient = null;
	//public DefaultTableColumnModelExt tabModel;
	
	public Vector<String> vectColumnNames = new Vector<String>();  //  @jve:decl-index=0:
	public Vector<Vector<String>> vectChemicalRows = new Vector<Vector<String>>();  //  @jve:decl-index=0:
	
	private JPanel jpanelGradientProfile = null;
	public JPanel jpanelStep1 = null;
	private JLabel jlblStationaryPhase = null;
	public JComboBox jcboStationaryPhase = null;
	public JLabel jlblFlowRate = null;
	public JTextField jtxtFlowRate = null;
	public JLabel jlblFlowRateUnit = null;
	private JPanel jpanelStep2 = null;
	public JPanel jpanelStep3 = null;
	private JScrollPane jScrollPane1 = null;
	public JTable jtableMeasuredRetentionTimes = null;
	public SpecialTableModel2 tmGradientProgram;
	public SpecialTableModel tmMeasuredRetentionTimes;
	public JPanel jpanelFlowProfile = null;
	public GraphControl m_GraphControlFlowRate = null;
	public JButton jbtnNextStep = null;
	public JButton jbtnHelp = null;
	public JButton jbtnPreloadedValues = null;
	private JLabel jlblColumnLength = null;
	public JTextField jtxtColumnLength = null;
	private JLabel jlblColumnLengthUnit = null;
	private JScrollPane jScrollPane = null;
	public JTable jtableGradientProgram = null;
	public JButton jbtnInsertRow = null;
	public JButton jbtnRemoveRow = null;
	private JLabel jlblColumnInnerDiameter = null;
	public JTextField jtxtInnerDiameter = null;
	private JLabel jlblInnerDiameterUnit = null;
	public JButton jbtnPeakFinder = null;

	public JLabel jlblInstrumentDeadTime = null;
	public JTextField jtxtIDT = null;
	public JComboBox jcboIDTUnits = null;

	private JLabel jLabel = null;

	private JLabel jlblApproxDeadTime = null;

	/**
	 * This is the default constructor
	 */
	public TopPanel() 
	{
		super();
		initialize();
	}

	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		jlblApproxDeadTime = new JLabel();
		jlblApproxDeadTime.setBounds(new Rectangle(336, 316, 298, 16));
		jlblApproxDeadTime.setForeground(new Color(51, 51, 51));
		jlblApproxDeadTime.setText("Approximate Dead Time vs. Eluent Composition");
		jlblApproxDeadTime.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
		jlblApproxDeadTime.setFont(new Font("Dialog", Font.BOLD, 12));
		jLabel = new JLabel();
		jLabel.setBounds(new Rectangle(336, 4, 193, 16));
		jLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		jLabel.setForeground(new Color(51, 51, 51));
		jLabel.setText("Approximate Gradient Program");
		this.setLayout(null);
		
		this.setVisible(true);
		//this.setBounds(new Rectangle(0, 0, 943, 615));
		this.setSize(new Dimension(943, 649));
        this.setPreferredSize(new Dimension(890,570));
        this.setMinimumSize(new Dimension(890,570));

		m_GraphControlGradient = new GraphControl();
		m_GraphControlGradient.setControlsEnabled(false);
		
		m_GraphControlFlowRate = new GraphControl();
		m_GraphControlFlowRate.setControlsEnabled(false);
		
	    this.add(getJpanelSimulatedChromatogram(), null);
	    this.add(getJpanelStep1(), null);
	    this.add(getJpanelStep2(), null);
	    this.add(getJpanelStep4(), null);
	    this.add(getJpanelFlowProfile(), null);
	    this.add(getJbtnNextStep(), null);
	    this.add(getJbtnHelp(), null);
	    this.add(getJbtnPreloadedValues(), null);
	    this.add(jLabel, null);
	    this.add(jlblApproxDeadTime, null);
   
		this.addComponentListener(this);

	}

	public class JChemicalTable extends JTable
	{
		private static final long serialVersionUID = 1L;
		
		public JChemicalTable(DefaultTableModel tabModel) {
			super(tabModel);
		}

		public boolean isCellEditable(int row, int column)
		{
			return false;
		}
	}

	/**
	 * This method initializes jSimulatedChromatogram	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelSimulatedChromatogram() 
	{
		if (jpanelGradientProfile == null) 
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			jpanelGradientProfile = new JPanel();
			jpanelGradientProfile.setLayout(gridLayout);
			jpanelGradientProfile.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jpanelGradientProfile.setPreferredSize(new Dimension(615, 477));
			jpanelGradientProfile.setSize(new Dimension(605, 289));
			jpanelGradientProfile.setLocation(new Point(332, 24));
			jpanelGradientProfile.add(m_GraphControlGradient, null);
			
		}
		return jpanelGradientProfile;
	}

	/**
	 * This method initializes jpanelStep1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStep1() {
		if (jpanelStep1 == null) {
			jlblInnerDiameterUnit = new JLabel();
			jlblInnerDiameterUnit.setText("mm");
			jlblInnerDiameterUnit.setLocation(new Point(256, 84));
			jlblInnerDiameterUnit.setSize(new Dimension(36, 16));
			jlblColumnInnerDiameter = new JLabel();
			jlblColumnInnerDiameter.setText("Inner diameter:");
			jlblColumnInnerDiameter.setLocation(new Point(12, 84));
			jlblColumnInnerDiameter.setSize(new Dimension(165, 16));
			jlblColumnLengthUnit = new JLabel();
			jlblColumnLengthUnit.setText("mm");
			jlblColumnLengthUnit.setLocation(new Point(256, 108));
			jlblColumnLengthUnit.setSize(new Dimension(38, 16));
			jlblColumnLength = new JLabel();
			jlblColumnLength.setText("Column length:");
			jlblColumnLength.setLocation(new Point(12, 108));
			jlblColumnLength.setSize(new Dimension(165, 16));
			jlblFlowRateUnit = new JLabel();
			jlblFlowRateUnit.setText("mL/min");
			jlblFlowRateUnit.setBounds(new Rectangle(256, 28, 53, 16));
			jlblFlowRate = new JLabel();
			jlblFlowRate.setText("Flow rate:");
			jlblFlowRate.setSize(new Dimension(165, 16));
			jlblFlowRate.setLocation(new Point(12, 28));
			jlblFlowRate.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblStationaryPhase = new JLabel();
			jlblStationaryPhase.setText("Stationary phase:");
			jlblStationaryPhase.setSize(new Dimension(293, 16));
			jlblStationaryPhase.setLocation(new Point(12, 28));
			jpanelStep1 = new JPanel();
			jpanelStep1.setLayout(null);
			jpanelStep1.setBorder(BorderFactory.createTitledBorder(null, "Step #1: Enter Column Properties", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep1.setSize(new Dimension(317, 145));
			jpanelStep1.setLocation(new Point(8, 0));
			jpanelStep1.add(jlblStationaryPhase, null);
			jpanelStep1.add(getJcboStationaryPhase(), null);
			jpanelStep1.add(jlblColumnLength, null);
			jpanelStep1.add(getJtxtColumnLength(), null);
			jpanelStep1.add(jlblColumnLengthUnit, null);
			jpanelStep1.add(jlblColumnInnerDiameter, null);
			jpanelStep1.add(getJtxtInnerDiameter(), null);
			jpanelStep1.add(jlblInnerDiameterUnit, null);
		}
		return jpanelStep1;
	}


	/**
	 * This method initializes jcboStationaryPhase	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboStationaryPhase() {
		if (jcboStationaryPhase == null) {
			jcboStationaryPhase = new JComboBox(Globals.StationaryPhaseArray);
			jcboStationaryPhase.setBounds(new Rectangle(12, 48, 293, 26));
		}
		return jcboStationaryPhase;
	}


	/**
	 * This method initializes jtxtFlowRate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtFlowRate() {
		if (jtxtFlowRate == null) {
			jtxtFlowRate = new JTextField();
			jtxtFlowRate.setText("0.4");
			jtxtFlowRate.setBounds(new Rectangle(180, 24, 73, 26));
			jtxtFlowRate.setHorizontalAlignment(JTextField.TRAILING);
		}
		return jtxtFlowRate;
	}


	/**
	 * This method initializes jpanelStep2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStep2() {
		if (jpanelStep2 == null) {
			jpanelStep2 = new JPanel();
			jpanelStep2.setLayout(null);
			jpanelStep2.setBorder(BorderFactory.createTitledBorder(null, "Step #2: Enter Approximate Gradient/Flow Rate", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep2.setSize(new Dimension(317, 189));
			jpanelStep2.setLocation(new Point(8, 144));
			jpanelStep2.add(jlblFlowRate, null);
			jpanelStep2.add(getJtxtFlowRate(), null);
			jpanelStep2.add(jlblFlowRateUnit, null);
			jpanelStep2.add(getJScrollPane(), null);
			jpanelStep2.add(getJbtnInsertRow(), null);
			jpanelStep2.add(getJbtnRemoveRow(), null);
		}
		return jpanelStep2;
	}


	class SelectCompoundsTableModel extends AbstractTableModel {
		private String[] columnNames = {"Compound", "Select"};
        private Object[][] data = new Object[Globals.StandardCompoundsNameArray.length][2];
        
        public void loadData()
        {
        	for(int i=0; i < Globals.StandardCompoundsNameArray.length; i++)
            {
            	data[i][0] = Globals.StandardCompoundsNameArray[i];
            	data[i][1] = new Boolean(false);
            }
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }
        
	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */

	/**
	 * This method initializes jpanelStep4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStep4() {
		if (jpanelStep3 == null) {
			jlblInstrumentDeadTime = new JLabel();
			jlblInstrumentDeadTime.setBounds(new Rectangle(12, 28, 165, 16));
			jlblInstrumentDeadTime.setFont(new Font("Dialog", Font.BOLD, 12));
			jlblInstrumentDeadTime.setText("Instrument dead time:");
			jpanelStep3 = new JPanel();
			jpanelStep3.setLayout(null);
			jpanelStep3.setBorder(BorderFactory.createTitledBorder(null, "Step #3: Enter Retention Times of Standards", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep3.setBounds(new Rectangle(8, 332, 317, 301));
			jpanelStep3.add(getJScrollPane1(), null);
			jpanelStep3.add(getJbtnPeakFinder(), null);
			jpanelStep3.add(jlblInstrumentDeadTime, null);
			jpanelStep3.add(getJtxtIDT(), null);
			jpanelStep3.add(getJcboIDTUnits(), null);
		}
		return jpanelStep3;
	}


	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setLocation(new Point(8, 56));
			jScrollPane1.setSize(new Dimension(301, 201));
			jScrollPane1.setViewportView(getJtableMeasuredRetentionTimes());
		}
		return jScrollPane1;
	}

	public class SpecialTableModel2 extends DefaultTableModel 
	{
		public SpecialTableModel2(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public SpecialTableModel2(final Object[][] data, final Object[] columnNames) 
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
	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	}
	
	class SpecialTableModel extends DefaultTableModel 
	{
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
	        if (column == 3 || column == 0)
	            return true;
	        else
	            return false;
	        
	    }
	    
	    /*
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	}
	
	// This TableCellEditor, if a JTextField, automatically selects all the text when it is created.
	// This makes it so that when you type something into the cell, it removes whatever was there
	// It does not make it select all on double-click
	class TableCellEditorCustom extends DefaultCellEditor
	{
		public TableCellEditorCustom(JTextField textField) {
			super(textField);

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
			Object obj = delegate.getCellEditorValue();
			if (obj.equals(""))
				return 0.0;
			else
				return (Double)(double)Float.valueOf((String)delegate.getCellEditorValue());
	    }
	}
	
	/**
	 * This method initializes jtableMeasuredRetentionTimes	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJtableMeasuredRetentionTimes() {
		if (jtableMeasuredRetentionTimes == null) 
		{
			Object[] columnNames = {"Use", "Compound", "m/z", "tR (min)"};
			Object[][] data = new Object[Globals.StandardCompoundsNameArray.length][4];
			for (int i = 0; i < Globals.StandardCompoundsNameArray.length; i++)
			{
				data[i][0] = false;
				data[i][1] = Globals.StandardCompoundsNameArray[i];
				String str = "";
				for (int j = 0; j < Globals.StandardCompoundsMZArray[i].length; j++)
				{
					str += Globals.StandardCompoundsMZArray[i][j];
					if (j < Globals.StandardCompoundsMZArray[i].length - 1)
						str += ", ";
				}
				data[i][2] = str;
				data[i][3] = (Object)0.0;
			}

			tmMeasuredRetentionTimes = new SpecialTableModel(data, columnNames);

			jtableMeasuredRetentionTimes = new JTable(tmMeasuredRetentionTimes);

			jtableMeasuredRetentionTimes.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			jtableMeasuredRetentionTimes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtableMeasuredRetentionTimes.getTableHeader().setPreferredSize(new Dimension(jtableMeasuredRetentionTimes.getColumnModel().getTotalColumnWidth(), 22));
			
			jtableMeasuredRetentionTimes.getColumnModel().getColumn(0).setPreferredWidth(30);
			jtableMeasuredRetentionTimes.getColumnModel().getColumn(1).setPreferredWidth(140);
			jtableMeasuredRetentionTimes.getColumnModel().getColumn(2).setPreferredWidth(40);
			
			JTextField jtf = new JTextField();
			TableCellEditorCustom cellEditor = new TableCellEditorCustom(jtf);
			jtableMeasuredRetentionTimes.getColumnModel().getColumn(3).setCellEditor(cellEditor);
		}
		return jtableMeasuredRetentionTimes;
	}


	/**
	 * This method initializes jpanelFlowProfile	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelFlowProfile() {
		if (jpanelFlowProfile == null) {
			GridLayout gridLayout1 = new GridLayout();
			gridLayout1.setRows(1);
			jpanelFlowProfile = new JPanel();
			jpanelFlowProfile.setLayout(gridLayout1);
			jpanelFlowProfile.setPreferredSize(new Dimension(615, 477));
			jpanelFlowProfile.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jpanelFlowProfile.setSize(new Dimension(605, 265));
			jpanelFlowProfile.setLocation(new Point(332, 336));
			jpanelFlowProfile.add(m_GraphControlFlowRate, null);
		}
		return jpanelFlowProfile;
	}


	/**
	 * This method initializes jbtnNextStep	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnNextStep() {
		if (jbtnNextStep == null) {
			jbtnNextStep = new JButton();
			jbtnNextStep.setText("Next Step  ");
			jbtnNextStep.setIcon(new ImageIcon(getClass().getResource("/org/hplcretentionpredictor/images/forward.png")));
			jbtnNextStep.setHorizontalTextPosition(SwingConstants.LEADING);
			jbtnNextStep.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnNextStep.setEnabled(false);
			jbtnNextStep.setBounds(new Rectangle(760, 608, 178, 36));
			jbtnNextStep.setActionCommand("Next Step");
		}
		return jbtnNextStep;
	}


	/**
	 * This method initializes jbtnHelp	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnHelp() {
		if (jbtnHelp == null) {
			jbtnHelp = new JButton();
			jbtnHelp.setText("Help");
			jbtnHelp.setEnabled(false);
			jbtnHelp.setBounds(new Rectangle(572, 608, 178, 36));
			jbtnHelp.setForeground(Color.blue);
			jbtnHelp.setVisible(false);
		}
		return jbtnHelp;
	}


	/**
	 * This method initializes jbtnPreloadedValues	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnPreloadedValues() {
		if (jbtnPreloadedValues == null) {
			jbtnPreloadedValues = new JButton();
			jbtnPreloadedValues.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnPreloadedValues.setHorizontalTextPosition(SwingConstants.LEADING);
			jbtnPreloadedValues.setText("Use Preloaded Values...");
			jbtnPreloadedValues.setBounds(new Rectangle(332, 608, 178, 36));
			jbtnPreloadedValues.setActionCommand("Preloaded Values");
		}
		return jbtnPreloadedValues;
	}


	/**
	 * This method initializes jtxtColumnLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtColumnLength() {
		if (jtxtColumnLength == null) {
			jtxtColumnLength = new JTextField();
			jtxtColumnLength.setBounds(new Rectangle(180, 104, 73, 26));
			jtxtColumnLength.setHorizontalAlignment(JTextField.TRAILING);
			jtxtColumnLength.setText("100");
		}
		return jtxtColumnLength;
	}


	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(8, 52, 301, 89));
			jScrollPane.setViewportView(getJtblGradientProgram());
		}
		return jScrollPane;
	}


	/**
	 * This method initializes jtblTemperatureProgram	
	 * 	
	 * @return javax.swing.JTable	
	 */

	private JTable getJtblGradientProgram() 
	{
		if (jtableGradientProgram == null) 
		{
			Object[] columnNames = {"Time (min)", "% B"};
			Double[][] data = {{0.0, 5.0},{5.0, 95.0}};
	        
			tmGradientProgram = new SpecialTableModel2(data, columnNames);

			jtableGradientProgram = new JTable(tmGradientProgram);
			
			jtableGradientProgram.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			jtableGradientProgram.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtableGradientProgram.getTableHeader().setPreferredSize(new Dimension(22, 22));
			jtableGradientProgram.getColumnModel().getColumn(0).setPreferredWidth(88);
			
			JTextField jtf = new JTextField();
			TableCellEditorCustom cellEditor = new TableCellEditorCustom(jtf);
			jtableGradientProgram.getColumnModel().getColumn(0).setCellEditor(cellEditor);
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
			jbtnInsertRow.setBounds(new Rectangle(8, 148, 138, 30));
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
			jbtnRemoveRow.setBounds(new Rectangle(172, 148, 138, 30));
			jbtnRemoveRow.setActionCommand("Remove Row");
			jbtnRemoveRow.setText("Remove Row");
		}
		return jbtnRemoveRow;
	}


	@Override
	public Dimension getPreferredScrollableViewportSize() {

		return null;
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {

		return 0;
	}


	@Override
	public boolean getScrollableTracksViewportHeight() {
		Dimension minSize = this.getMinimumSize();
		Dimension portSize = null;
		if (getParent() instanceof JViewport) 
		{
			JViewport port = (JViewport)getParent();
			portSize = port.getSize();
		}
		else
			return false;
		
		if (portSize.height < minSize.height)
			return false;
		else
			return true;
	}


	@Override
	public boolean getScrollableTracksViewportWidth() {
		Dimension minSize = this.getMinimumSize();
		Dimension portSize = null;
		if (getParent() instanceof JViewport) 
		{
			JViewport port = (JViewport)getParent();
			portSize = port.getSize();
		}
		else
			return false;
		
		if (portSize.width < minSize.width)
			return false;
		else
			return true;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {

		return 0;
	}


	@Override
	public void componentHidden(ComponentEvent arg0) {

		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		this.revalidate();
	}

	@Override
	public void componentResized(ComponentEvent arg0) 
	{
		// Respond to window resize
		if (arg0.getComponent() == this)
		{
			Dimension size = this.getSize();
			this.jpanelStep3.setSize(this.jpanelStep3.getWidth(), size.height - 332);
			this.jbtnPeakFinder.setLocation(jScrollPane1.getX(), jpanelStep3.getHeight() - 41);
			this.jbtnPeakFinder.setSize(jScrollPane1.getWidth(), jbtnPeakFinder.getHeight());
			this.jScrollPane1.setSize(this.jScrollPane1.getWidth(), jbtnPeakFinder.getY() - this.jScrollPane1.getY() - 8);
			this.jtableMeasuredRetentionTimes.revalidate();
			this.jbtnNextStep.setLocation((int)size.getWidth() - this.jbtnNextStep.getWidth() - 6, (int)size.getHeight() - jbtnNextStep.getHeight() - 6);
			this.jbtnHelp.setLocation(this.jbtnNextStep.getLocation().x - this.jbtnHelp.getWidth() - 10, (int)size.getHeight() - jbtnHelp.getHeight() - 6);
			this.jbtnPreloadedValues.setLocation(this.jbtnPreloadedValues.getLocation().x, (int)size.getHeight() - jbtnPreloadedValues.getHeight() - 6);
			
			this.jpanelGradientProfile.setSize(size.width - jpanelGradientProfile.getLocation().x - 6, ((size.height - 6 - 6 - 24 - 24 - this.jbtnNextStep.getHeight()) * 5) / 10);
			//this.m_GraphControlGradient.setSize(jpanelGradientProfile.getWidth() - 3 - 5, jpanelGradientProfile.getHeight() - 16 - 3);
			this.m_GraphControlGradient.repaint();
			
			this.jlblApproxDeadTime.setLocation(jlblApproxDeadTime.getX(), jpanelGradientProfile.getY() + jpanelGradientProfile.getHeight() + 6);
			
			this.jpanelFlowProfile.setLocation(jpanelFlowProfile.getX(), jpanelGradientProfile.getY() + jpanelGradientProfile.getHeight() + 3 + 24);
			this.jpanelFlowProfile.setSize(jpanelGradientProfile.getWidth(), size.height - jpanelFlowProfile.getY() - 6 - 6 - this.jbtnNextStep.getHeight());
			//this.m_GraphControlFlowRate.setSize(jpanelFlowProfile.getWidth() - 3 - 5, jpanelFlowProfile.getHeight() - 16 - 3);
			this.m_GraphControlFlowRate.repaint();
		}
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		
	}

	/**
	 * This method initializes jtxtInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInnerDiameter() {
		if (jtxtInnerDiameter == null) {
			jtxtInnerDiameter = new JTextField();
			jtxtInnerDiameter.setBounds(new Rectangle(180, 80, 73, 26));
			jtxtInnerDiameter.setHorizontalAlignment(JTextField.TRAILING);
			jtxtInnerDiameter.setText("2.1");
		}
		return jtxtInnerDiameter;
	}


	/**
	 * This method initializes jbtnPeakFinder	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnPeakFinder() {
		if (jbtnPeakFinder == null) {
			jbtnPeakFinder = new JButton();
			jbtnPeakFinder.setBounds(new Rectangle(8, 260, 301, 30));
			jbtnPeakFinder.setText("Find retention times automatically...");
		}
		return jbtnPeakFinder;
	}


	/**
	 * This method initializes jtxtIDT	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtIDT() {
		if (jtxtIDT == null) {
			jtxtIDT = new JTextField();
			jtxtIDT.setBounds(new Rectangle(180, 24, 65, 26));
			jtxtIDT.setHorizontalAlignment(JTextField.TRAILING);
			jtxtIDT.setText("0.0");
		}
		return jtxtIDT;
	}


	/**
	 * This method initializes jcboIDTUnits	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboIDTUnits() {
		if (jcboIDTUnits == null) {
			jcboIDTUnits = new JComboBox(new String[] {"min", "mL"});
			jcboIDTUnits.setBounds(new Rectangle(248, 24, 59, 26));
			jcboIDTUnits.setActionCommand("IDT Units Changed");
		}
		return jcboIDTUnits;
	}
	
}  //  @jve:decl-index=0:visual-constraint="-259,126"
