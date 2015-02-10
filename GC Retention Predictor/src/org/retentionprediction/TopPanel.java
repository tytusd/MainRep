package org.retentionprediction;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import javax.media.opengl.*;

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

import org.retentionprediction.TopPanel.JChemicalTable;
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
import javax.media.opengl.GLCapabilities;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import java.awt.GridLayout;

public class TopPanel extends JPanel implements Scrollable, ComponentListener
{
	private static final long serialVersionUID = 1L;
	
	public GraphControl m_GraphControlTemp = null;
	//public DefaultTableColumnModelExt tabModel;
	
	public Vector<String> vectColumnNames = new Vector<String>();  //  @jve:decl-index=0:
	public Vector<Vector<String>> vectChemicalRows = new Vector<Vector<String>>();  //  @jve:decl-index=0:
	
	private JPanel jpanelTemperatureProgram = null;
	public JPanel jpanelStep1 = null;
	private JLabel jlblStationaryPhase = null;
	public JComboBox jcboStationaryPhase = null;
	private JLabel jlblInitialTemperature = null;
	public JTextField jtxtInitialTemperature = null;
	private JLabel jlblInitialTime = null;
	public JTextField jtxtInitialTime = null;
	private JLabel jlblInitialTemperatureUnit = null;
	private JLabel jlblInitialTimeUnit = null;
	public JLabel jlblFlowRate = null;
	public JTextField jtxtFlowRate = null;
	public JLabel jlblFlowRateUnit = null;
	private JPanel jpanelStep2 = null;
	public JPanel jpanelStep4 = null;
	private JScrollPane jScrollPane1 = null;
	public JTable jtableMeasuredRetentionTimes = null;
	public SpecialTableModel2 tmTemperatureProgram;
	public SpecialTableModel tmMeasuredRetentionTimes;
	public JPanel jpanelFlowProfile = null;
	public GraphControl m_GraphControlHoldUp = null;
	public GLCapabilities capsFlow = null;
	public JButton jbtnNextStep = null;
	public JButton jbtnHelp = null;
	public JButton jbtnPreloadedValues = null;
	private JLabel jlblColumnLength = null;
	public JTextField jtxtColumnLength = null;
	private JLabel jlblColumnLengthUnit = null;
	public JRadioButton jrdoConstantPressure = null;
	public JRadioButton jrdoConstantFlowRate = null;
	private JScrollPane jScrollPane = null;
	public JTable jtblTemperatureProgram = null;
	public JButton jbtnInsertRow = null;
	public JButton jbtnRemoveRow = null;
	public JLabel jlblOutletPressure = null;
	public JLabel jlblPressureUnit = null;
	public JLabel jlblPressure = null;
	public JTextField jtxtPressure = null;
	public JRadioButton jrdoVacuum = null;
	public JRadioButton jrdoOtherPressure = null;
	public JTextField jtxtOtherPressure = null;
	public JLabel jlblOtherPressureUnit = null;
	public JButton jbtnPeakFinder = null;
	private JLabel jlblInnerDiameter = null;
	public JTextField jtxtInnerDiameter = null;
	private JLabel jlblInnerDiameterUnit = null;
	private JLabel jlblFilmThickness = null;
	public JTextField jtxtFilmThickness = null;
	private JLabel jlblFilmThicknessUnits = null;

	private JLabel jLabel = null;

	private JLabel jlblHoldUpTime = null;

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
		jlblHoldUpTime = new JLabel();
		jlblHoldUpTime.setBounds(new Rectangle(328, 280, 316, 16));
		jlblHoldUpTime.setForeground(new Color(51, 51, 51));
		jlblHoldUpTime.setText("Approximate Hold-Up Time vs. Temperature");
		jlblHoldUpTime.setFont(new Font("Dialog", Font.BOLD, 12));
		jLabel = new JLabel();
		jLabel.setBounds(new Rectangle(328, 4, 256, 16));
		jLabel.setForeground(new Color(51, 51, 51));
		jLabel.setText("Approximate Temperature Program");
		jLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		this.setLayout(null);
		
		this.setVisible(true);
		//this.setBounds(new Rectangle(0, 0, 943, 615));
		this.setSize(new Dimension(943, 912));
        this.setPreferredSize(new Dimension(890,685));
        this.setMinimumSize(new Dimension(890,685));

		m_GraphControlTemp = new GraphControl();
		m_GraphControlTemp.setControlsEnabled(false);
		
		m_GraphControlHoldUp = new GraphControl();
		m_GraphControlHoldUp.setControlsEnabled(false);
		
	    this.add(getJpanelSimulatedChromatogram(), null);
	    this.add(getJpanelStep1(), null);
	    this.add(getJpanelStep2(), null);
	    this.add(getJpanelStep4(), null);
	    this.add(getJpanelFlowProfile(), null);
	    this.add(getJbtnNextStep(), null);
	    this.add(getJbtnHelp(), null);
	    this.add(getJbtnPreloadedValues(), null);
	    this.add(jLabel, null);
	    this.add(jlblHoldUpTime, null);
   
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
		if (jpanelTemperatureProgram == null) 
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			jpanelTemperatureProgram = new JPanel();
			jpanelTemperatureProgram.setLayout(gridLayout);
			jpanelTemperatureProgram.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jpanelTemperatureProgram.setPreferredSize(new Dimension(615, 477));
			jpanelTemperatureProgram.setSize(new Dimension(616, 249));
			jpanelTemperatureProgram.setLocation(new Point(328, 24));
			jpanelTemperatureProgram.add(m_GraphControlTemp, null);
			
		}
		return jpanelTemperatureProgram;
	}

	/**
	 * This method initializes jpanelStep1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStep1() {
		if (jpanelStep1 == null) {
			jlblFilmThicknessUnits = new JLabel();
			jlblFilmThicknessUnits.setText("\u00b5m");
			jlblFilmThicknessUnits.setBounds(new Rectangle(256, 128, 49, 16));
			jlblFilmThickness = new JLabel();
			jlblFilmThickness.setText("Film thickness:");
			jlblFilmThickness.setBounds(new Rectangle(12, 128, 181, 16));
			jlblInnerDiameterUnit = new JLabel();
			jlblInnerDiameterUnit.setText("mm");
			jlblInnerDiameterUnit.setBounds(new Rectangle(256, 104, 49, 16));
			jlblInnerDiameter = new JLabel();
			jlblInnerDiameter.setText("Inner diameter:");
			jlblInnerDiameter.setBounds(new Rectangle(12, 104, 181, 16));
			jlblColumnLengthUnit = new JLabel();
			jlblColumnLengthUnit.setText("m");
			jlblColumnLengthUnit.setBounds(new Rectangle(256, 80, 49, 16));
			jlblColumnLength = new JLabel();
			jlblColumnLength.setText("Column length:");
			jlblColumnLength.setBounds(new Rectangle(12, 80, 181, 16));
			jlblFlowRateUnit = new JLabel();
			jlblFlowRateUnit.setText("mL/min");
			jlblFlowRateUnit.setBounds(new Rectangle(256, 52, 53, 16));
			jlblFlowRate = new JLabel();
			jlblFlowRate.setText("Flow rate:");
			jlblFlowRate.setBounds(new Rectangle(24, 52, 169, 16));
			jlblFlowRate.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblInitialTimeUnit = new JLabel();
			jlblInitialTimeUnit.setText("min");
			jlblInitialTimeUnit.setBounds(new Rectangle(256, 192, 49, 16));
			jlblInitialTemperatureUnit = new JLabel();
			jlblInitialTemperatureUnit.setText("\u00b0C");
			jlblInitialTemperatureUnit.setBounds(new Rectangle(256, 168, 49, 16));
			jlblInitialTime = new JLabel();
			jlblInitialTime.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblInitialTime.setBounds(new Rectangle(12, 192, 181, 16));
			jlblInitialTime.setText("Initial time:");
			jlblInitialTemperature = new JLabel();
			jlblInitialTemperature.setText("Initial oven temperature:");
			jlblInitialTemperature.setBounds(new Rectangle(12, 168, 181, 16));
			jlblStationaryPhase = new JLabel();
			jlblStationaryPhase.setText("Standard column:");
			jlblStationaryPhase.setBounds(new Rectangle(12, 28, 105, 16));
			jpanelStep1 = new JPanel();
			jpanelStep1.setLayout(null);
			jpanelStep1.setBorder(BorderFactory.createTitledBorder(null, "Step #1: Select Standard Column", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep1.setSize(new Dimension(317, 161));
			jpanelStep1.setLocation(new Point(4, 0));
			jpanelStep1.add(jlblStationaryPhase, null);
			jpanelStep1.add(getJcboStationaryPhase(), null);
			jpanelStep1.add(jlblColumnLength, null);
			jpanelStep1.add(getJtxtColumnLength(), null);
			jpanelStep1.add(jlblColumnLengthUnit, null);
			jpanelStep1.add(jlblInnerDiameter, null);
			jpanelStep1.add(getJtxtInnerDiameter(), null);
			jpanelStep1.add(jlblInnerDiameterUnit, null);
			jpanelStep1.add(jlblFilmThickness, null);
			jpanelStep1.add(getJtxtFilmThickness(), null);
			jpanelStep1.add(jlblFilmThicknessUnits, null);
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
	 * This method initializes jtxtInitialSolventComposition	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInitialSolventComposition() {
		if (jtxtInitialTemperature == null) {
			jtxtInitialTemperature = new JTextField();
			jtxtInitialTemperature.setText("60.0");
			jtxtInitialTemperature.setBounds(new Rectangle(192, 164, 61, 26));
		}
		return jtxtInitialTemperature;
	}


	/**
	 * This method initializes jtxtInitialTime	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInitialTime() {
		if (jtxtInitialTime == null) {
			jtxtInitialTime = new JTextField();
			jtxtInitialTime.setText("5");
			jtxtInitialTime.setBounds(new Rectangle(192, 188, 61, 26));
		}
		return jtxtInitialTime;
	}


	/**
	 * This method initializes jtxtFlowRate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtFlowRate() {
		if (jtxtFlowRate == null) {
			jtxtFlowRate = new JTextField();
			jtxtFlowRate.setText("1.0");
			jtxtFlowRate.setBounds(new Rectangle(192, 48, 61, 26));
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
			jlblOtherPressureUnit = new JLabel();
			jlblOtherPressureUnit.setBounds(new Rectangle(256, 140, 50, 16));
			jlblOtherPressureUnit.setEnabled(false);
			jlblOtherPressureUnit.setText("kPa");
			jlblOtherPressureUnit.setVisible(true);
			jlblPressure = new JLabel();
			jlblPressure.setBounds(new Rectangle(24, 96, 165, 16));
			jlblPressure.setText("Column inlet pressure:");
			jlblPressure.setEnabled(false);
			jlblPressure.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblPressureUnit = new JLabel();
			jlblPressureUnit.setBounds(new Rectangle(256, 96, 53, 16));
			jlblPressureUnit.setText("kPa");
			jlblPressureUnit.setEnabled(false);
			jlblPressureUnit.setVisible(true);
			jlblOutletPressure = new JLabel();
			jlblOutletPressure.setBounds(new Rectangle(12, 120, 165, 16));
			jlblOutletPressure.setText("Column outlet pressure:");
			jlblOutletPressure.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblOutletPressure.setVisible(true);
			jpanelStep2 = new JPanel();
			jpanelStep2.setLayout(null);
			jpanelStep2.setBorder(BorderFactory.createTitledBorder(null, "Step #2: Enter Experiment Conditions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep2.setSize(new Dimension(317, 349));
			jpanelStep2.setLocation(new Point(4, 160));
			jpanelStep2.add(jlblInitialTemperature, null);
			jpanelStep2.add(getJtxtInitialSolventComposition(), null);
			jpanelStep2.add(jlblInitialTemperatureUnit, null);
			jpanelStep2.add(jlblInitialTime, null);
			jpanelStep2.add(getJtxtInitialTime(), null);
			jpanelStep2.add(jlblInitialTimeUnit, null);
			jpanelStep2.add(jlblFlowRate, null);
			jpanelStep2.add(getJtxtFlowRate(), null);
			jpanelStep2.add(jlblFlowRateUnit, null);
			jpanelStep2.add(getJrdoConstantPressure(), null);
			jpanelStep2.add(getJrdoConstantFlowRate(), null);
			jpanelStep2.add(getJScrollPane(), null);
			jpanelStep2.add(getJbtnInsertRow(), null);
			jpanelStep2.add(getJbtnRemoveRow(), null);
			jpanelStep2.add(jlblOutletPressure, null);
			jpanelStep2.add(jlblPressureUnit, null);
			jpanelStep2.add(jlblPressure, null);
			jpanelStep2.add(getJtxtPressure(), null);
			jpanelStep2.add(getJrdoVacuum(), null);
			jpanelStep2.add(getJrdoOtherPressure(), null);
			jpanelStep2.add(getJtxtOtherPressure(), null);
			jpanelStep2.add(jlblOtherPressureUnit, null);
		}
		return jpanelStep2;
	}


	class SelectCompoundsTableModel extends AbstractTableModel {
		private String[] columnNames = {"Compound", "Select"};
        private Object[][] data = new Object[Globals.CompoundNameArray.length][2];
        
        public void loadData()
        {
        	for(int i=0; i < Globals.CompoundNameArray.length; i++)
            {
            	data[i][0] = Globals.CompoundNameArray[i];
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
		if (jpanelStep4 == null) {
			jpanelStep4 = new JPanel();
			jpanelStep4.setLayout(null);
			jpanelStep4.setBorder(BorderFactory.createTitledBorder(null, "Step #3: Enter Measured n-Alkane Retention Times", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep4.setSize(new Dimension(317, 261));
			jpanelStep4.setLocation(new Point(4, 508));
			jpanelStep4.add(getJScrollPane1(), null);
			jpanelStep4.add(getJbtnPeakFinder(), null);
		}
		return jpanelStep4;
	}


	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setLocation(new Point(8, 28));
			jScrollPane1.setSize(new Dimension(301, 181));
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
			Object[][] data = new Object[Globals.AlkaneNameArray.length][4];
			for (int i = 0; i < Globals.AlkaneNameArray.length; i++)
			{
				data[i][0] = false;
				data[i][1] = Globals.AlkaneNameArray[i];
				String str = "";
				for (int j = 0; j < Globals.AlkaneMZArray[i].length; j++)
				{
					str += Globals.AlkaneMZArray[i][j];
					if (j < Globals.AlkaneMZArray[i].length - 1)
						str += ", ";
				}
				data[i][2] = str;
				data[i][3] = (Object)0.0;
			}
			
			tmMeasuredRetentionTimes = new SpecialTableModel(data, columnNames);

			jtableMeasuredRetentionTimes = new JTable(tmMeasuredRetentionTimes);

			jtableMeasuredRetentionTimes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtableMeasuredRetentionTimes.getTableHeader().setPreferredSize(new Dimension(jtableMeasuredRetentionTimes.getColumnModel().getTotalColumnWidth(), 22));
			
			jtableMeasuredRetentionTimes.getColumnModel().getColumn(0).setPreferredWidth(30);
			jtableMeasuredRetentionTimes.getColumnModel().getColumn(1).setPreferredWidth(140);
			jtableMeasuredRetentionTimes.getColumnModel().getColumn(2).setPreferredWidth(40);
			
			jtableMeasuredRetentionTimes.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

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
			jpanelFlowProfile.setSize(new Dimension(615, 233));
			jpanelFlowProfile.setLocation(new Point(328, 300));
			jpanelFlowProfile.add(m_GraphControlHoldUp, null);
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
			jbtnNextStep.setIcon(new ImageIcon(getClass().getResource("/org/retentionprediction/images/forward.png")));
			jbtnNextStep.setHorizontalTextPosition(SwingConstants.LEADING);
			jbtnNextStep.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnNextStep.setEnabled(false);
			jbtnNextStep.setBounds(new Rectangle(760, 732, 178, 36));
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
			jbtnHelp.setBounds(new Rectangle(572, 732, 178, 36));
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
			jbtnPreloadedValues.setBounds(new Rectangle(328, 732, 178, 36));
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
			jtxtColumnLength.setText("30.0");
			jtxtColumnLength.setBounds(new Rectangle(192, 76, 61, 26));
		}
		return jtxtColumnLength;
	}


	/**
	 * This method initializes jrdoConstantPressure	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoConstantPressure() {
		if (jrdoConstantPressure == null) {
			jrdoConstantPressure = new JRadioButton();
			jrdoConstantPressure.setBounds(new Rectangle(12, 72, 257, 20));
			jrdoConstantPressure.setText("Constant pressure mode");
		}
		return jrdoConstantPressure;
	}


	/**
	 * This method initializes jrdoConstantFlowRate	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoConstantFlowRate() {
		if (jrdoConstantFlowRate == null) {
			jrdoConstantFlowRate = new JRadioButton();
			jrdoConstantFlowRate.setBounds(new Rectangle(12, 28, 257, 20));
			jrdoConstantFlowRate.setText("Constant flow rate mode");
			jrdoConstantFlowRate.setSelected(true);
		}
		return jrdoConstantFlowRate;
	}


	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(8, 216, 301, 81));
			jScrollPane.setViewportView(getJtblTemperatureProgram());
		}
		return jScrollPane;
	}


	/**
	 * This method initializes jtblTemperatureProgram	
	 * 	
	 * @return javax.swing.JTable	
	 */

	private JTable getJtblTemperatureProgram() {
		if (jtblTemperatureProgram == null) 
		{
			Object[] columnNames = {"Ramp (\u00b0C/min)", "Final temp (\u00b0C)", "Hold time (min)"};
			Double[][] data = {{20.0, 260.0, 5.0}};
	        
			tmTemperatureProgram = new SpecialTableModel2(data, columnNames);

			jtblTemperatureProgram = new JTable(tmTemperatureProgram);

			jtblTemperatureProgram.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtblTemperatureProgram.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			jtblTemperatureProgram.getTableHeader().setPreferredSize(new Dimension(22, 22));
			jtblTemperatureProgram.getColumnModel().getColumn(0).setPreferredWidth(85);
		}
		return jtblTemperatureProgram;
	}

	/**
	 * This method initializes jbtnInsertRow	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnInsertRow() {
		if (jbtnInsertRow == null) {
			jbtnInsertRow = new JButton();
			jbtnInsertRow.setBounds(new Rectangle(12, 304, 138, 30));
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
			jbtnRemoveRow.setBounds(new Rectangle(168, 304, 138, 30));
			jbtnRemoveRow.setActionCommand("Remove Row");
			jbtnRemoveRow.setText("Remove Row");
		}
		return jbtnRemoveRow;
	}


	/**
	 * This method initializes jtxtPressure	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPressure() {
		if (jtxtPressure == null) {
			jtxtPressure = new JTextField();
			jtxtPressure.setBounds(new Rectangle(192, 92, 61, 26));
			jtxtPressure.setEditable(true);
			jtxtPressure.setEnabled(false);
			jtxtPressure.setActionCommand("Inlet Pressure");
			jtxtPressure.setText("100");
		}
		return jtxtPressure;
	}


	/**
	 * This method initializes jrdoVacuum	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoVacuum() {
		if (jrdoVacuum == null) {
			jrdoVacuum = new JRadioButton();
			jrdoVacuum.setBounds(new Rectangle(12, 140, 77, 20));
			jrdoVacuum.setSelected(true);
			jrdoVacuum.setText("Vacuum");
		}
		return jrdoVacuum;
	}


	/**
	 * This method initializes jrdoOtherPressure	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoOtherPressure() {
		if (jrdoOtherPressure == null) {
			jrdoOtherPressure = new JRadioButton();
			jrdoOtherPressure.setBounds(new Rectangle(108, 140, 77, 20));
			jrdoOtherPressure.setActionCommand("OtherPressure");
			jrdoOtherPressure.setText("Other:");
		}
		return jrdoOtherPressure;
	}


	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
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
			this.jpanelStep4.setSize(this.jpanelStep4.getWidth(), size.height - this.jpanelStep4.getY() - 4);
			this.jScrollPane1.setSize(this.jScrollPane1.getWidth(), jpanelStep4.getHeight() - 68);
			this.jbtnPeakFinder.setLocation(jScrollPane1.getX(), jpanelStep4.getHeight() - 41);
			this.jbtnPeakFinder.setSize(jScrollPane1.getWidth(), jbtnPeakFinder.getHeight());
			this.jtableMeasuredRetentionTimes.revalidate();
			this.jbtnNextStep.setLocation((int)size.getWidth() - this.jbtnNextStep.getWidth() - 6, (int)size.getHeight() - jbtnNextStep.getHeight() - 6);
			this.jbtnHelp.setLocation(this.jbtnNextStep.getLocation().x - this.jbtnHelp.getWidth() - 10, (int)size.getHeight() - jbtnHelp.getHeight() - 6);
			this.jbtnPreloadedValues.setLocation(this.jbtnPreloadedValues.getLocation().x, (int)size.getHeight() - jbtnPreloadedValues.getHeight() - 6);
			
			this.jpanelTemperatureProgram.setSize(size.width - jpanelTemperatureProgram.getLocation().x - 6, ((size.height - 6 - 6 - 24 - 24 - this.jbtnNextStep.getHeight()) * 5) / 10);
			this.m_GraphControlTemp.repaint();

			this.jlblHoldUpTime.setLocation(jlblHoldUpTime.getX(), jpanelTemperatureProgram.getY() + jpanelTemperatureProgram.getHeight() + 6);

			this.jpanelFlowProfile.setLocation(jpanelFlowProfile.getX(), jpanelTemperatureProgram.getY() + jpanelTemperatureProgram.getHeight() + 3 + 24);
			this.jpanelFlowProfile.setSize(jpanelTemperatureProgram.getWidth(), size.height - jpanelFlowProfile.getY() - 6 - 6 - this.jbtnNextStep.getHeight());
			this.m_GraphControlHoldUp.repaint();
			
			
		}
		// TODO Auto-generated method stub
		
	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * This method initializes jtxtOtherPressure	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtOtherPressure() {
		if (jtxtOtherPressure == null) {
			jtxtOtherPressure = new JTextField();
			jtxtOtherPressure.setText("101.325");
			jtxtOtherPressure.setBounds(new Rectangle(192, 136, 61, 26));
			jtxtOtherPressure.setEnabled(false);
		}
		return jtxtOtherPressure;
	}


	/**
	 * This method initializes jbtnPeakFinder	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnPeakFinder() {
		if (jbtnPeakFinder == null) {
			jbtnPeakFinder = new JButton();
			jbtnPeakFinder.setBounds(new Rectangle(8, 216, 301, 30));
			jbtnPeakFinder.setText("Find retention times automatically...");
		}
		return jbtnPeakFinder;
	}


	/**
	 * This method initializes jtxtInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInnerDiameter() {
		if (jtxtInnerDiameter == null) {
			jtxtInnerDiameter = new JTextField();
			jtxtInnerDiameter.setText("0.25");
			jtxtInnerDiameter.setBounds(new Rectangle(192, 100, 61, 26));
		}
		return jtxtInnerDiameter;
	}


	/**
	 * This method initializes jtxtFilmThickness	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtFilmThickness() {
		if (jtxtFilmThickness == null) {
			jtxtFilmThickness = new JTextField();
			jtxtFilmThickness.setText("0.25");
			jtxtFilmThickness.setBounds(new Rectangle(192, 124, 61, 26));
		}
		return jtxtFilmThickness;
	}
}  //  @jve:decl-index=0:visual-constraint="-259,126"
