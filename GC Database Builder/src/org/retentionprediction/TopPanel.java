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
	
	public Vector<String> vectColumnNames = new Vector<String>();  //  @jve:decl-index=0:
	public Vector<Vector<String>> vectChemicalRows = new Vector<Vector<String>>();  //  @jve:decl-index=0:
	
	public JPanel jpanelStep1 = null;
	private JLabel jlblStationaryPhase = null;
	public JComboBox jcboStationaryPhase = null;
	public JLabel jlblFlowRate = null;
	public JTextField jtxtFlowRate = null;
	public JLabel jlblFlowRateUnit = null;
	public JPanel jpanelStep2 = null;
	private JScrollPane jScrollPane1 = null;
	public JTable jtableMeasuredRetentionTimes = null;
	public SpecialTableModel2 tmTemperatureProgram;
	public SpecialTableModel tmMeasuredRetentionTimes;
	public GLCapabilities capsFlow = null;
	public JButton jbtnNextStep = null;
	public JButton jbtnPreloadedValues = null;
	private JLabel jlblColumnLength = null;
	public JTextField jtxtColumnLength = null;
	private JLabel jlblColumnLengthUnit = null;
	public JRadioButton jrdoConstantPressure = null;
	public JRadioButton jrdoConstantFlowRate = null;
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

	private JPanel jPanelRequirements = null;

	private JLabel jlblInitialTemperature = null;

	private JLabel jlblInitialTemperatureIndicator = null;

	private JLabel jlblInitialTemperatureUnit = null;

	private JLabel jlblInitialHoldTime = null;

	private JLabel jlblInitialHoldTimeIndicator = null;

	private JLabel jlblInitialTemperatureUnit1 = null;

	private JLabel jlblRampRate = null;

	private JLabel jlblRampRateIndicator = null;

	private JLabel jlblRampRateUnits = null;

	private JLabel jlblFinalTemperature = null;

	private JLabel jlblFinalTemperatureIndicator = null;

	private JLabel jlblFinalTemperatureUnit = null;

	private JLabel jlblFinalHoldTime = null;

	private JLabel jlblFinalHoldTimeIndicator = null;

	private JLabel jlblFinalTemperatureUnits = null;

	private JLabel jLabelInstructions = null;

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
		jLabelInstructions = new JLabel();
		jLabelInstructions.setBounds(new Rectangle(4, 4, 905, 65));
		jLabelInstructions.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelInstructions.setText("<html>The retention times of the <i>n</i>-alkanes are needed to back-calculate how your GC behaved during this temperature program. Make sure your run conforms to the required experimental conditions listed below. Other experimental conditions are optional. First, enter the optional experimental conditions you used during this run in step #1. Then, enter the retention times of each of the <i>n</i>-alkanes in step #2.</html>");
		this.setLayout(null);
		
		this.setVisible(true);
		//this.setBounds(new Rectangle(0, 0, 943, 615));
		this.setSize(new Dimension(916, 434));
        this.setPreferredSize(new Dimension(890,685));
        this.setMinimumSize(new Dimension(890,685));

		
		
	    this.add(getJpanelStep1(), null);
	    this.add(getJpanelStep2(), null);
	    this.add(getJbtnNextStep(), null);
	    this.add(getJbtnPreloadedValues(), null);
	    this.add(getJPanelRequirements(), null);
	    this.add(jLabelInstructions, null);
   
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
			jlblFlowRateUnit.setBounds(new Rectangle(256, 176, 53, 16));
			jlblFlowRate = new JLabel();
			jlblFlowRate.setText("Flow rate:");
			jlblFlowRate.setBounds(new Rectangle(24, 176, 169, 16));
			jlblFlowRate.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblStationaryPhase = new JLabel();
			jlblStationaryPhase.setText("Standard column:");
			jlblStationaryPhase.setBounds(new Rectangle(12, 28, 105, 16));
			jlblOtherPressureUnit = new JLabel();
			jlblOtherPressureUnit.setEnabled(false);
			jlblOtherPressureUnit.setText("kPa");
			jlblOtherPressureUnit.setBounds(new Rectangle(256, 264, 50, 16));
			jlblOtherPressureUnit.setVisible(true);
			jlblPressure = new JLabel();
			jlblPressure.setText("Column inlet pressure:");
			jlblPressure.setEnabled(false);
			jlblPressure.setBounds(new Rectangle(24, 220, 165, 16));
			jlblPressure.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblPressureUnit = new JLabel();
			jlblPressureUnit.setText("kPa");
			jlblPressureUnit.setEnabled(false);
			jlblPressureUnit.setBounds(new Rectangle(256, 220, 53, 16));
			jlblPressureUnit.setVisible(true);
			jlblOutletPressure = new JLabel();
			jlblOutletPressure.setText("Column outlet pressure:");
			jlblOutletPressure.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblOutletPressure.setBounds(new Rectangle(12, 244, 165, 16));
			jlblOutletPressure.setVisible(true);
			jpanelStep1 = new JPanel();
			jpanelStep1.setLayout(null);
			jpanelStep1.setBorder(BorderFactory.createTitledBorder(null, "Step #1: Enter Optional Experimental Conditions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep1.setSize(new Dimension(317, 313));
			jpanelStep1.setLocation(new Point(272, 72));
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
			jpanelStep1.add(getJrdoConstantFlowRate(), null);
			jpanelStep1.add(jlblFlowRate, null);
			jpanelStep1.add(getJtxtFlowRate(), null);
			jpanelStep1.add(jlblFlowRateUnit, null);
			jpanelStep1.add(getJrdoConstantPressure(), null);
			jpanelStep1.add(jlblPressure, null);
			jpanelStep1.add(getJtxtPressure(), null);
			jpanelStep1.add(jlblPressureUnit, null);
			jpanelStep1.add(jlblOutletPressure, null);
			jpanelStep1.add(getJrdoVacuum(), null);
			jpanelStep1.add(getJrdoOtherPressure(), null);
			jpanelStep1.add(getJtxtOtherPressure(), null);
			jpanelStep1.add(jlblOtherPressureUnit, null);
		}
		return jpanelStep1;
	}

	public void setProgramNumber(int progNum)
	{
		String strTitle = "Requirements for Program ";
		if (progNum == 0)
			strTitle += "A";
		else if (progNum == 1)
			strTitle += "B";
		else if (progNum == 2)
			strTitle += "C";
		else if (progNum == 3)
			strTitle += "D";
		else if (progNum == 4)
			strTitle += "E";
		else if (progNum == 5)
			strTitle += "F";
		
		jPanelRequirements.setBorder(BorderFactory.createTitledBorder(null, strTitle, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));

		this.jlblInitialTemperatureIndicator.setText(Float.toString((float)Globals.dRequiredTemperaturePrograms[progNum][0][0]));
		this.jlblInitialHoldTimeIndicator.setText(Float.toString((float)Globals.dRequiredTemperaturePrograms[progNum][0][1]));
		this.jlblRampRateIndicator.setText(Float.toString((float)Globals.dRequiredTemperaturePrograms[progNum][1][0]));
		this.jlblFinalTemperatureIndicator.setText(Float.toString((float)Globals.dRequiredTemperaturePrograms[progNum][1][1]));
		this.jlblFinalHoldTimeIndicator.setText(Float.toString((float)Globals.dRequiredTemperaturePrograms[progNum][1][2]));
			
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
			jtxtFlowRate.setText("1.0");
			jtxtFlowRate.setBounds(new Rectangle(192, 172, 61, 26));
		}
		return jtxtFlowRate;
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
	private JPanel getJpanelStep2() {
		if (jpanelStep2 == null) {
			jpanelStep2 = new JPanel();
			jpanelStep2.setLayout(null);
			jpanelStep2.setBorder(BorderFactory.createTitledBorder(null, "Step #2: Enter Measured n-Alkane Retention Times", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep2.setSize(new Dimension(317, 313));
			jpanelStep2.setLocation(new Point(592, 72));
			jpanelStep2.add(getJScrollPane1(), null);
			jpanelStep2.add(getJbtnPeakFinder(), null);
		}
		return jpanelStep2;
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
			jbtnNextStep.setEnabled(true);
			jbtnNextStep.setBounds(new Rectangle(732, 392, 178, 36));
			jbtnNextStep.setActionCommand("Next Step");
		}
		return jbtnNextStep;
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
			jbtnPreloadedValues.setBounds(new Rectangle(324, 344, 178, 36));
			jbtnPreloadedValues.setActionCommand("Preloaded Values");
			jbtnPreloadedValues.setVisible(false);
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
			jrdoConstantPressure.setText("Constant pressure mode");
			jrdoConstantPressure.setBounds(new Rectangle(12, 196, 257, 20));
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
			jrdoConstantFlowRate.setText("Constant flow rate mode");
			jrdoConstantFlowRate.setBounds(new Rectangle(12, 152, 257, 20));
			jrdoConstantFlowRate.setSelected(true);
		}
		return jrdoConstantFlowRate;
	}



	/**
	 * This method initializes jtxtPressure	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPressure() {
		if (jtxtPressure == null) {
			jtxtPressure = new JTextField();
			jtxtPressure.setEditable(true);
			jtxtPressure.setEnabled(false);
			jtxtPressure.setActionCommand("Inlet Pressure");
			jtxtPressure.setBounds(new Rectangle(192, 216, 61, 26));
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
			jrdoVacuum.setSelected(true);
			jrdoVacuum.setBounds(new Rectangle(12, 264, 77, 20));
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
			jrdoOtherPressure.setActionCommand("OtherPressure");
			jrdoOtherPressure.setBounds(new Rectangle(108, 264, 77, 20));
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
			jLabelInstructions.setSize((int)size.getWidth(), jLabelInstructions.getHeight());
			jPanelRequirements.setLocation(4, jLabelInstructions.getY() + jLabelInstructions.getHeight());
			jPanelRequirements.setSize((int)(size.getWidth() - 8) / 3, (int)size.getHeight() - jPanelRequirements.getY() - 41);
			jpanelStep1.setLocation(((int)(size.getWidth() - 8) / 3) + 4, jLabelInstructions.getY() + jLabelInstructions.getHeight());
			jpanelStep1.setSize((int)(size.getWidth() - 8) / 3, (int)size.getHeight() - jpanelStep1.getY() - 41);
			jpanelStep2.setLocation(((int)((size.getWidth() - 8) * 2) / 3) + 4, jLabelInstructions.getY() + jLabelInstructions.getHeight());
			jpanelStep2.setSize((int)(size.getWidth() - 8) / 3, (int)size.getHeight() - jpanelStep2.getY() - 41);
			jScrollPane1.setSize(this.jScrollPane1.getWidth(), jpanelStep2.getHeight() - 68);
			jbtnPeakFinder.setLocation(jScrollPane1.getX(), jpanelStep2.getHeight() - 41);
			jbtnPeakFinder.setSize(jScrollPane1.getWidth(), jbtnPeakFinder.getHeight());
			jtableMeasuredRetentionTimes.revalidate();
			jbtnNextStep.setLocation((int)size.getWidth() - this.jbtnNextStep.getWidth() - 6, (int)size.getHeight() - jbtnNextStep.getHeight() - 6);
			jbtnPreloadedValues.setLocation(this.jbtnPreloadedValues.getLocation().x, (int)size.getHeight() - jbtnPreloadedValues.getHeight() - 6);
			
			
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
			jtxtOtherPressure.setBounds(new Rectangle(192, 260, 61, 26));
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


	/**
	 * This method initializes jPanelRequirements	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelRequirements() {
		if (jPanelRequirements == null) {
			jlblFinalTemperatureUnits = new JLabel();
			jlblFinalTemperatureUnits.setBounds(new Rectangle(208, 124, 49, 16));
			jlblFinalTemperatureUnits.setForeground(Color.red);
			jlblFinalTemperatureUnits.setText("min");
			jlblFinalHoldTimeIndicator = new JLabel();
			jlblFinalHoldTimeIndicator.setBounds(new Rectangle(172, 124, 33, 16));
			jlblFinalHoldTimeIndicator.setForeground(Color.red);
			jlblFinalHoldTimeIndicator.setText("40");
			jlblFinalHoldTime = new JLabel();
			jlblFinalHoldTime.setBounds(new Rectangle(12, 124, 157, 16));
			jlblFinalHoldTime.setText("Final hold time:");
			jlblFinalTemperatureUnit = new JLabel();
			jlblFinalTemperatureUnit.setBounds(new Rectangle(208, 100, 49, 16));
			jlblFinalTemperatureUnit.setForeground(Color.red);
			jlblFinalTemperatureUnit.setText("°C");
			jlblFinalTemperatureIndicator = new JLabel();
			jlblFinalTemperatureIndicator.setBounds(new Rectangle(172, 100, 33, 16));
			jlblFinalTemperatureIndicator.setForeground(Color.red);
			jlblFinalTemperatureIndicator.setText("270");
			jlblFinalTemperature = new JLabel();
			jlblFinalTemperature.setBounds(new Rectangle(12, 100, 157, 16));
			jlblFinalTemperature.setText("Final temperature:");
			jlblRampRateUnits = new JLabel();
			jlblRampRateUnits.setBounds(new Rectangle(208, 76, 49, 16));
			jlblRampRateUnits.setForeground(Color.red);
			jlblRampRateUnits.setText("°C/min");
			jlblRampRateIndicator = new JLabel();
			jlblRampRateIndicator.setBounds(new Rectangle(172, 76, 33, 16));
			jlblRampRateIndicator.setForeground(Color.red);
			jlblRampRateIndicator.setText("4.7");
			jlblRampRate = new JLabel();
			jlblRampRate.setBounds(new Rectangle(12, 76, 157, 16));
			jlblRampRate.setText("Ramp rate:");
			jlblInitialTemperatureUnit1 = new JLabel();
			jlblInitialTemperatureUnit1.setBounds(new Rectangle(208, 52, 49, 16));
			jlblInitialTemperatureUnit1.setForeground(Color.red);
			jlblInitialTemperatureUnit1.setText("min");
			jlblInitialHoldTimeIndicator = new JLabel();
			jlblInitialHoldTimeIndicator.setBounds(new Rectangle(172, 52, 33, 16));
			jlblInitialHoldTimeIndicator.setForeground(Color.red);
			jlblInitialHoldTimeIndicator.setText("5");
			jlblInitialHoldTime = new JLabel();
			jlblInitialHoldTime.setBounds(new Rectangle(12, 52, 157, 16));
			jlblInitialHoldTime.setText("Initial hold time:");
			jlblInitialTemperatureUnit = new JLabel();
			jlblInitialTemperatureUnit.setBounds(new Rectangle(208, 28, 49, 16));
			jlblInitialTemperatureUnit.setForeground(Color.red);
			jlblInitialTemperatureUnit.setText("°C");
			jlblInitialTemperatureIndicator = new JLabel();
			jlblInitialTemperatureIndicator.setBounds(new Rectangle(172, 28, 33, 16));
			jlblInitialTemperatureIndicator.setForeground(Color.red);
			jlblInitialTemperatureIndicator.setText("60");
			jlblInitialTemperature = new JLabel();
			jlblInitialTemperature.setBounds(new Rectangle(12, 28, 157, 16));
			jlblInitialTemperature.setText("Initial oven temperature:");
			jPanelRequirements = new JPanel();
			jPanelRequirements.setLayout(null);
			jPanelRequirements.setBounds(new Rectangle(4, 72, 265, 313));
			jPanelRequirements.setBorder(BorderFactory.createTitledBorder(null, "Requirements for Program A", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelRequirements.setForeground(Color.red);
			jPanelRequirements.add(jlblInitialTemperature, null);
			jPanelRequirements.add(jlblInitialTemperatureIndicator, null);
			jPanelRequirements.add(jlblInitialTemperatureUnit, null);
			jPanelRequirements.add(jlblInitialHoldTime, null);
			jPanelRequirements.add(jlblInitialHoldTimeIndicator, null);
			jPanelRequirements.add(jlblInitialTemperatureUnit1, null);
			jPanelRequirements.add(jlblRampRate, null);
			jPanelRequirements.add(jlblRampRateIndicator, null);
			jPanelRequirements.add(jlblRampRateUnits, null);
			jPanelRequirements.add(jlblFinalTemperature, null);
			jPanelRequirements.add(jlblFinalTemperatureIndicator, null);
			jPanelRequirements.add(jlblFinalTemperatureUnit, null);
			jPanelRequirements.add(jlblFinalHoldTime, null);
			jPanelRequirements.add(jlblFinalHoldTimeIndicator, null);
			jPanelRequirements.add(jlblFinalTemperatureUnits, null);
		}
		return jPanelRequirements;
	}
}  //  @jve:decl-index=0:visual-constraint="-264,131"
