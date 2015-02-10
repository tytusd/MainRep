package org.hplcretentionpredictor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.Rectangle;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;

import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import boswell.graphcontrol.GraphControl;

import javax.swing.JProgressBar;
import javax.swing.JLabel;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.awt.GridLayout;

public class TopPanel2 extends JPanel implements TableModelListener, Scrollable, ComponentListener
{

	private static final long serialVersionUID = 1L;
	public JButton jbtnPreviousStep = null;
	public JPanel jpanelGradientProfile = null;
	public GraphControl m_GraphControlGradient = null;
	public JPanel jpanelFlowProfile = null;
	public GraphControl m_GraphControlFlowRate = null;
	public JPanel jpanelStep4 = null;
	public JScrollPane jScrollPane = null;
	public JTable jtableOutput = null;
	public JButton jbtnCalculate = null;
	public NoEditTableModel tmOutputModel = null;
	public TestCompoundsTableModel m_tmTestCompoundsModel = null;
	public JLabel jlblIterationNumber = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel11 = null;
	private JLabel jLabel111 = null;
	public JLabel jlblVariance = null;
	public JLabel jlblPhase = null;
	public JLabel jlblTimeElapsed = null;
	private JLabel jLabel12 = null;
	public JLabel jlblLastVariance = null;
	private JLabel jLabel121 = null;
	public JLabel jlblPercentImprovement = null;
	public JButton jbtnHelp = null;
	public JProgressBar jProgressBar = null;
	private JLabel jlblStatus1 = null;
	public NoEditTableModel tmPredictionModel = null;
	private JLabel jlblPreColumnVolume = null;
	private JLabel jlblMixingVolumeLabel = null;
	private JLabel jlblNonMixingVolumeLabel = null;
	private JLabel jlblTotalGradientDelayVolumeLabel = null;
	public JLabel jlblMixingVolume = null;
	public JLabel jlblNonMixingVolume = null;
	public JLabel jlblTotalGradientDelayVolume = null;
	public JButton jbtnNextStep = null;
	public JPanel jpanelStep6 = null;
	public JProgressBar jProgressBar2 = null;
	private JLabel jlblStatus2 = null;
	public JButton jbtnPredict = null;
	public JScrollPane jScrollPane2 = null;
	public JTable jtablePredictions = null;
	private JLabel jLabel2 = null;
	private JLabel jlblBackCalculatedDeadTimeProfile = null;
	public JPanel jpanelStep5 = null;
	private JLabel jlblInstructions = null;
	public JButton jbtnAutomaticDetermineTestCompounds = null;
	private JLabel jlblGood = null;
	private JLabel jlblBad = null;
	private JLabel jlblStandardDeviationName = null;
	public JLabel jlblStandardDeviation = null;
	private JLabel jlblExpectedStandardDeviationName = null;
	public JLabel jlblExpectedStandardDeviation = null;
	private JLabel jlblColumnRatingName = null;
	public JLabel jlblColumnRating = null;
	public SliderIndicator jSliderIndicator = null;
	private JScrollPane jScrollPaneTestCompounds = null;
	public JTable jtableTestCompoundPredictions = null;
	private JLabel jlblToleranceWindow = null;
	public JTextField jtxtWindowConfidence = null;
	private JLabel jlblPercent = null;
	/**
	 * This is the default constructor
	 */
	public TopPanel2() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jlblBackCalculatedDeadTimeProfile = new JLabel();
		jlblBackCalculatedDeadTimeProfile.setBounds(new Rectangle(12, 304, 377, 16));
		jlblBackCalculatedDeadTimeProfile.setForeground(new Color(51, 51, 51));
		jlblBackCalculatedDeadTimeProfile.setText("Back-Calculated Thiourea Dead Time vs. Eluent Composition");
		jlblBackCalculatedDeadTimeProfile.setFont(new Font("Dialog", Font.BOLD, 12));
		jLabel2 = new JLabel();
		jLabel2.setBounds(new Rectangle(12, 4, 233, 16));
		jLabel2.setForeground(new Color(51, 51, 51));
		jLabel2.setText("Back-Calculated Gradient Program");
		jLabel2.setFont(new Font("Dialog", Font.BOLD, 12));
		this.setLayout(null);
		this.setBounds(new Rectangle(0, 0, 1912, 615));
        this.setPreferredSize(new Dimension(890,570));
        this.setMinimumSize(new Dimension(890,570));

		m_GraphControlGradient = new GraphControl();
		m_GraphControlGradient.setControlsEnabled(false);

		m_GraphControlFlowRate = new GraphControl();
		m_GraphControlFlowRate.setControlsEnabled(false);

		this.add(getJbtnPreviousStep(), null);
		this.add(getJpanelGradientProfile(), null);
		this.add(getJpanelFlowProfile(), null);
		this.setVisible(true);
		this.add(getJbtnHelp(), null);
		this.add(getJpanelStep5(), null);
		this.add(getJbtnNextStep(), null);
		this.add(jLabel2, null);
		this.add(jlblBackCalculatedDeadTimeProfile, null);
		this.add(getJpanelStep6(), null);
		this.add(getJpanelStep52(), null);
		
		this.tmOutputModel.addTableModelListener(this);
		this.addComponentListener(this);
	}

	/**
	 * This method initializes jbtnPreviousStep	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnPreviousStep() {
		if (jbtnPreviousStep == null) {
			jbtnPreviousStep = new JButton();
			jbtnPreviousStep.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnPreviousStep.setHorizontalTextPosition(SwingConstants.TRAILING);
			jbtnPreviousStep.setIcon(new ImageIcon(getClass().getResource("/org/hplcretentionpredictor/images/back.png")));
			jbtnPreviousStep.setText("  Previous Step");
			jbtnPreviousStep.setBounds(new Rectangle(4, 572, 178, 36));
			jbtnPreviousStep.setActionCommand("Previous Step");
		}
		return jbtnPreviousStep;
	}

	/**
	 * This method initializes jpanelGradientProfile	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelGradientProfile() {
		if (jpanelGradientProfile == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			jpanelGradientProfile = new JPanel();
			jpanelGradientProfile.setLayout(gridLayout);
			jpanelGradientProfile.setPreferredSize(new Dimension(615, 477));
			jpanelGradientProfile.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jpanelGradientProfile.setSize(new Dimension(465, 277));
			jpanelGradientProfile.setLocation(new Point(8, 24));
			jpanelGradientProfile.add(m_GraphControlGradient, null);
		}
		return jpanelGradientProfile;
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
			jpanelFlowProfile.setSize(new Dimension(465, 245));
			jpanelFlowProfile.setLocation(new Point(8, 324));
			jpanelFlowProfile.add(m_GraphControlFlowRate, null);
		}
		return jpanelFlowProfile;
	}

	/**
	 * This method initializes jpanelStep5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStep5() {
		if (jpanelStep4 == null) {
			jlblTotalGradientDelayVolume = new JLabel();
			jlblTotalGradientDelayVolume.setText("");
			jlblTotalGradientDelayVolume.setSize(new Dimension(145, 16));
			jlblTotalGradientDelayVolume.setFont(new Font("Dialog", Font.BOLD, 12));
			jlblTotalGradientDelayVolume.setLocation(new Point(260, 444));
			jlblNonMixingVolume = new JLabel();
			jlblNonMixingVolume.setText("");
			jlblNonMixingVolume.setSize(new Dimension(145, 16));
			jlblNonMixingVolume.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblNonMixingVolume.setLocation(new Point(260, 424));
			jlblMixingVolume = new JLabel();
			jlblMixingVolume.setText("");
			jlblMixingVolume.setSize(new Dimension(145, 16));
			jlblMixingVolume.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblMixingVolume.setLocation(new Point(260, 404));
			jlblTotalGradientDelayVolumeLabel = new JLabel();
			jlblTotalGradientDelayVolumeLabel.setBounds(new Rectangle(56, 444, 197, 16));
			jlblTotalGradientDelayVolumeLabel.setFont(new Font("Dialog", Font.BOLD, 12));
			jlblTotalGradientDelayVolumeLabel.setText("Total gradient delay volume:");
			jlblNonMixingVolumeLabel = new JLabel();
			jlblNonMixingVolumeLabel.setBounds(new Rectangle(56, 424, 197, 16));
			jlblNonMixingVolumeLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblNonMixingVolumeLabel.setText("Non-mixing volume:");
			jlblMixingVolumeLabel = new JLabel();
			jlblMixingVolumeLabel.setBounds(new Rectangle(56, 404, 197, 16));
			jlblMixingVolumeLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblMixingVolumeLabel.setText("Mixing volume:");
			jlblPreColumnVolume = new JLabel();
			jlblPreColumnVolume.setBounds(new Rectangle(40, 384, 213, 16));
			jlblPreColumnVolume.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblPreColumnVolume.setText("Pre-column volume:");
			jlblStatus1 = new JLabel();
			jlblStatus1.setBounds(new Rectangle(40, 472, 173, 16));
			jlblStatus1.setText("Status:");
			jlblPercentImprovement = new JLabel();
			jlblPercentImprovement.setBounds(new Rectangle(260, 316, 145, 17));
			jlblPercentImprovement.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblPercentImprovement.setText("");
			jLabel121 = new JLabel();
			jLabel121.setBounds(new Rectangle(40, 316, 213, 16));
			jLabel121.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel121.setText("% improvement:");
			jlblLastVariance = new JLabel();
			jlblLastVariance.setBounds(new Rectangle(260, 296, 145, 17));
			jlblLastVariance.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblLastVariance.setText("");
			jLabel12 = new JLabel();
			jLabel12.setBounds(new Rectangle(40, 296, 213, 16));
			jLabel12.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel12.setText("Last iteration variance:");
			jlblTimeElapsed = new JLabel();
			jlblTimeElapsed.setBounds(new Rectangle(260, 356, 145, 17));
			jlblTimeElapsed.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblTimeElapsed.setText("");
			jlblPhase = new JLabel();
			jlblPhase.setBounds(new Rectangle(260, 336, 145, 17));
			jlblPhase.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblPhase.setText("I");
			jlblVariance = new JLabel();
			jlblVariance.setBounds(new Rectangle(260, 276, 145, 16));
			jlblVariance.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblVariance.setText("");
			jLabel111 = new JLabel();
			jLabel111.setBounds(new Rectangle(40, 356, 213, 16));
			jLabel111.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel111.setText("Time Elapsed:");
			jLabel11 = new JLabel();
			jLabel11.setBounds(new Rectangle(40, 336, 213, 16));
			jLabel11.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel11.setText("Phase:");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(40, 276, 213, 16));
			jLabel1.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel1.setText("Variance (\u03C3\u00B2):");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(40, 256, 213, 16));
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel.setText("Iteration #:");
			jlblIterationNumber = new JLabel();
			jlblIterationNumber.setBounds(new Rectangle(260, 256, 145, 16));
			jlblIterationNumber.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblIterationNumber.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblIterationNumber.setText("");
			jpanelStep4 = new JPanel();
			jpanelStep4.setLayout(null);
			jpanelStep4.setBorder(BorderFactory.createTitledBorder(null, "Step #4: Back-Calculate Gradient and Dead Time Profiles", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep4.setBounds(new Rectangle(476, 0, 461, 573));
			jpanelStep4.add(getJScrollPane(), null);
			jpanelStep4.add(getJbtnCalculate(), null);
			jpanelStep4.add(jlblIterationNumber, null);
			jpanelStep4.add(jLabel, null);
			jpanelStep4.add(jLabel1, null);
			jpanelStep4.add(jLabel11, null);
			jpanelStep4.add(jLabel111, null);
			jpanelStep4.add(jlblVariance, null);
			jpanelStep4.add(jlblPhase, null);
			jpanelStep4.add(jlblTimeElapsed, null);
			jpanelStep4.add(jLabel12, null);
			jpanelStep4.add(jlblLastVariance, null);
			jpanelStep4.add(jLabel121, null);
			jpanelStep4.add(jlblPercentImprovement, null);
			jpanelStep4.add(getJProgressBar(), null);
			jpanelStep4.add(jlblStatus1, null);
			jpanelStep4.add(jlblPreColumnVolume, null);
			jpanelStep4.add(jlblMixingVolumeLabel, null);
			jpanelStep4.add(jlblNonMixingVolumeLabel, null);
			jpanelStep4.add(jlblTotalGradientDelayVolumeLabel, null);
			jpanelStep4.add(jlblMixingVolume, null);
			jpanelStep4.add(jlblNonMixingVolume, null);
			jpanelStep4.add(jlblTotalGradientDelayVolume, null);
		}
		return jpanelStep4;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(8, 24, 445, 225));
			jScrollPane.setViewportView(getJtableOutput());
		}
		return jScrollPane;
	}

	class NoEditTableModel extends DefaultTableModel 
	{
	    public NoEditTableModel(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public NoEditTableModel(final Object[][] data, final Object[] columnNames) 
	    {
	        setDataVector(data, columnNames);
	    }

	    public boolean isCellEditable(int row, int column) 
	    {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        return false;
	    }
	    
	    /*
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    //public Class getColumnClass(int c) {
	    //    return getValueAt(0, c).getClass();
	    //}

	}
	
	class TestCompoundsTableModel extends DefaultTableModel 
	{
	    public TestCompoundsTableModel(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public TestCompoundsTableModel(final Object[][] data, final Object[] columnNames) 
	    {
	        setDataVector(data, columnNames);
	    }

	    public boolean isCellEditable(int row, int column) 
	    {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	    	if (column == 3)
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
	    //public Class getColumnClass(int c) {
	        //return getValueAt(0, c).getClass();
	    //}

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
				return "";
			else
			{
				boolean bIsAcceptableNumber = true;
				try {
			        Float test = Float.parseFloat((String)obj);
			    } catch (NumberFormatException e) {
			        bIsAcceptableNumber = false;
			    }
			    
			    if (bIsAcceptableNumber)
			    	return (Double)(double)Float.valueOf((String)obj);
			    else
			    	return "";
			}
	    }
	}
	
	/**
	 * This method initializes jtableOutput	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJtableOutput() {
		if (jtableOutput == null) 
		{
			Object[] columnNames = {"Compound", "Expt tR (min)", "Calc tR (min)", "Diff (min)"};
			tmOutputModel = new NoEditTableModel(columnNames, 0);
			jtableOutput = new JTable(tmOutputModel);
			
			jtableOutput.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			jtableOutput.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			jtableOutput.setBounds(new Rectangle(0, 0, 20, 20));
			
			jtableOutput.getColumnModel().getColumn(0).setPreferredWidth(200);
			
			jtableOutput.setAutoCreateColumnsFromModel(false);
		}
		return jtableOutput;
	}

	/**
	 * This method initializes jbtnCalculate	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnCalculate() {
		if (jbtnCalculate == null) {
			jbtnCalculate = new JButton();
			jbtnCalculate.setText("Calculate");
			jbtnCalculate.setBounds(new Rectangle(128, 520, 213, 36));
			jbtnCalculate.setActionCommand("Calculate");
		}
		return jbtnCalculate;
	}

	@Override
	public void tableChanged(TableModelEvent arg0) 
	{
	    Vector data = tmOutputModel.getDataVector();
	    Collections.sort(data, new ColumnSorter(1));
	}

	class ColumnSorter implements Comparator
	{
		int colIndex;

		ColumnSorter(int colIndex) 
		{
			this.colIndex = colIndex;
		}

		public int compare(Object a, Object b) 
		{
		    Vector v1 = (Vector) a;
		    Vector v2 = (Vector) b;
		    Object o1 = Double.valueOf((String)v1.get(colIndex));
		    Object o2 = Double.valueOf((String)v2.get(colIndex));
	
		    if (o1 instanceof String && ((String) o1).length() == 0) {
		      o1 = null;
		    }
		    if (o2 instanceof String && ((String) o2).length() == 0) {
		      o2 = null;
		    }
	
		    if (o1 == null && o2 == null) {
		    	return 0;
		    } else if (o1 == null) {
		    	return 1;
		    } else if (o2 == null) {
		    	return -1;
		    } else if (o1 instanceof Comparable) {
	
		    	return ((Comparable) o1).compareTo(o2);
		    } else {
	
		    	return o1.toString().compareTo(o2.toString());
		    }
		}
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
			jbtnHelp.setBounds(new Rectangle(572, 572, 178, 36));
			jbtnHelp.setForeground(Color.blue);
			jbtnHelp.setVisible(false);
		}
		return jbtnHelp;
	}

	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setBounds(new Rectangle(40, 492, 381, 25));
			jProgressBar.setStringPainted(false);
		}
		return jProgressBar;
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
			this.jbtnPreviousStep.setLocation(jbtnPreviousStep.getX(), size.height - jbtnPreviousStep.getHeight() - 6);
			this.jbtnNextStep.setLocation((int)size.getWidth() - this.jbtnNextStep.getWidth() - 6, (int)size.getHeight() - jbtnNextStep.getHeight() - 6);
			this.jbtnHelp.setLocation(size.width - 6 - jbtnHelp.getWidth() - 10 - jbtnHelp.getWidth(), size.height - jbtnHelp.getHeight() - 6);
			
			this.jpanelStep4.setLocation(((size.width) / 2) + 3, jpanelStep4.getY());
			this.jpanelStep4.setSize(size.width - jpanelStep4.getX() - 6, size.height - 6 - jbtnHelp.getHeight() - 6);
			this.jbtnCalculate.setLocation((jpanelStep4.getWidth() / 2) - (jbtnCalculate.getWidth() / 2), jpanelStep4.getHeight() - 15 - jbtnCalculate.getHeight());
			this.jProgressBar.setLocation((jpanelStep4.getWidth() / 2) - (jProgressBar.getWidth() / 2), jbtnCalculate.getY() - jProgressBar.getHeight() - 12);
			this.jlblStatus1.setLocation(jProgressBar.getX(), jProgressBar.getY() - 4 - jlblStatus1.getHeight());
			this.jlblTotalGradientDelayVolumeLabel.setLocation(jlblStatus1.getX() + 16, jlblStatus1.getY() - 12 - jlblTotalGradientDelayVolumeLabel.getHeight());
			this.jlblNonMixingVolumeLabel.setLocation(jlblStatus1.getX() + 16, jlblTotalGradientDelayVolumeLabel.getY() - 4 - jlblNonMixingVolumeLabel.getHeight());
			this.jlblMixingVolumeLabel.setLocation(jlblStatus1.getX() + 16, jlblNonMixingVolumeLabel.getY() - 4 - jlblMixingVolumeLabel.getHeight());
			this.jlblPreColumnVolume.setLocation(jlblStatus1.getX(), jlblMixingVolumeLabel.getY() - 4 - jlblPreColumnVolume.getHeight());
			this.jLabel111.setLocation(jlblStatus1.getX(), jlblPreColumnVolume.getY() - 12 - jLabel111.getHeight());
			this.jLabel11.setLocation(jLabel111.getX(), jLabel111.getY() - 4 - jLabel11.getHeight());
			this.jLabel121.setLocation(jLabel11.getX(), jLabel11.getY() - 4 - jLabel121.getHeight());
			this.jLabel12.setLocation(jLabel121.getX(), jLabel121.getY() - 4 - jLabel12.getHeight());
			this.jLabel1.setLocation(jLabel12.getX(), jLabel12.getY() - 4 - jLabel1.getHeight());
			this.jLabel.setLocation(jLabel1.getX(), jLabel1.getY() - 4 - jLabel.getHeight());
			this.jScrollPane.setSize(jpanelStep4.getWidth() - 8 - 8, jLabel.getY() - 6 - jScrollPane.getY());
			this.jlblTotalGradientDelayVolume.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblTotalGradientDelayVolume.getWidth(), jlblTotalGradientDelayVolumeLabel.getY());
			this.jlblNonMixingVolume.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblNonMixingVolume.getWidth(), jlblNonMixingVolumeLabel.getY());
			this.jlblMixingVolume.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblMixingVolume.getWidth(), jlblMixingVolumeLabel.getY());
			this.jlblTimeElapsed.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblTimeElapsed.getWidth(), jLabel111.getY());
			this.jlblPhase.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblPhase.getWidth(), jLabel11.getY());
			this.jlblPercentImprovement.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblPercentImprovement.getWidth(), jLabel121.getY());
			this.jlblLastVariance.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblLastVariance.getWidth(), jLabel12.getY());
			this.jlblVariance.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblVariance.getWidth(), jLabel1.getY());
			this.jlblIterationNumber.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblIterationNumber.getWidth(), jLabel.getY());

			this.jpanelStep5.setLocation(((size.width) / 2) + 3, jpanelStep4.getY());
			this.jpanelStep5.setSize(size.width - jpanelStep4.getX() - 6, size.height - 6 - jbtnNextStep.getHeight() - 6);
			this.jSliderIndicator.setLocation(72, jpanelStep5.getHeight() - jSliderIndicator.getHeight() - 8);
			this.jSliderIndicator.setSize(jpanelStep5.getWidth() - (72 * 2), 69);
			this.jlblGood.setLocation(16, jSliderIndicator.getY() + 16);
			this.jlblBad.setLocation(jSliderIndicator.getX() + jSliderIndicator.getWidth() + 8, jSliderIndicator.getY() + 16);
			this.jlblColumnRatingName.setLocation((jpanelStep5.getWidth() / 2) - 4 - jlblColumnRatingName.getWidth(), jSliderIndicator.getY() - jlblColumnRatingName.getHeight() - 16);
			this.jlblColumnRating.setLocation((jpanelStep5.getWidth() / 2) + 4, jSliderIndicator.getY() - jlblColumnRating.getHeight() - 16);
			this.jlblExpectedStandardDeviationName.setLocation((jpanelStep5.getWidth() / 2) - 4 - jlblExpectedStandardDeviationName.getWidth(), jlblColumnRatingName.getY() - jlblExpectedStandardDeviationName.getHeight() - 4);
			this.jlblExpectedStandardDeviation.setLocation((jpanelStep5.getWidth() / 2) + 4, jlblColumnRating.getY() - jlblExpectedStandardDeviation.getHeight() - 4);
			this.jlblStandardDeviationName.setLocation((jpanelStep5.getWidth() / 2) - 4 - jlblStandardDeviationName.getWidth(), jlblExpectedStandardDeviationName.getY() - jlblStandardDeviationName.getHeight() - 4);
			this.jlblStandardDeviation.setLocation((jpanelStep5.getWidth() / 2) + 4, jlblExpectedStandardDeviation.getY() - jlblStandardDeviation.getHeight() - 4);
			this.jbtnAutomaticDetermineTestCompounds.setLocation((jpanelStep5.getWidth() / 2) - (jbtnAutomaticDetermineTestCompounds.getWidth() / 2), jlblStandardDeviation.getY() - 16 - jbtnAutomaticDetermineTestCompounds.getHeight());
			this.jScrollPaneTestCompounds.setSize(jpanelStep4.getWidth() - 8 - 8, jbtnAutomaticDetermineTestCompounds.getY() - 6 - jScrollPaneTestCompounds.getY());
			this.jlblInstructions.setSize(jpanelStep4.getWidth() - 8 - 8, jlblInstructions.getHeight());
			
			this.jpanelStep6.setLocation(((size.width) / 2) + 3, jpanelStep6.getY());
			this.jpanelStep6.setSize(size.width - jpanelStep6.getX() - 6, size.height - 6 - jbtnHelp.getHeight() - 6);
			this.jbtnPredict.setLocation((jpanelStep6.getWidth() / 2) - (jbtnPredict.getWidth() / 2), jpanelStep6.getHeight() - 15 - jbtnPredict.getHeight());
			this.jProgressBar2.setLocation((jpanelStep6.getWidth() / 2) - (jProgressBar2.getWidth() / 2), jbtnPredict.getY() - jProgressBar2.getHeight() - 12);
			this.jlblStatus2.setLocation(jProgressBar2.getX(), jProgressBar2.getY() - 4 - jlblStatus2.getHeight());
			this.jScrollPane2.setSize(jpanelStep6.getWidth() - 8 - 8, jlblStatus2.getY() - 6 - jScrollPane2.getY());
			this.jlblPercent.setLocation(jScrollPane2.getX() + jScrollPane2.getWidth() - jlblPercent.getWidth(), jlblPercent.getY());
			this.jtxtWindowConfidence.setLocation(jlblPercent.getX() - 4 - jtxtWindowConfidence.getWidth(), jtxtWindowConfidence.getY());

			this.jpanelGradientProfile.setSize(size.width - jpanelStep4.getX() - 6, (size.height - 6 - 24 - 24 - jbtnHelp.getHeight() - 6 - 6) / 2);
			//this.m_GraphControlGradient.setSize(jpanelGradientProfile.getWidth() - 3 - 5, jpanelGradientProfile.getHeight() - 16 - 3);
			this.m_GraphControlGradient.repaint();
			
			this.jlblBackCalculatedDeadTimeProfile.setLocation(jlblBackCalculatedDeadTimeProfile.getX(), jpanelGradientProfile.getY() + jpanelGradientProfile.getHeight() + 6);

			this.jpanelFlowProfile.setLocation(jpanelFlowProfile.getX(), jpanelGradientProfile.getY() + jpanelGradientProfile.getHeight() + 3 + 24);
			this.jpanelFlowProfile.setSize(jpanelGradientProfile.getWidth(), size.height - jpanelFlowProfile.getY() - 6 - jbtnHelp.getHeight() - 6);
			//this.m_GraphControlFlowRate.setSize(jpanelFlowProfile.getWidth() - 3 - 5, jpanelFlowProfile.getHeight() - 16 - 3);
			this.m_GraphControlFlowRate.repaint();
		}
		// TODO Auto-generated method stub
		
	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method initializes jbtnNextStep	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnNextStep() {
		if (jbtnNextStep == null) {
			jbtnNextStep = new JButton();
			jbtnNextStep.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnNextStep.setHorizontalTextPosition(SwingConstants.LEADING);
			jbtnNextStep.setIcon(new ImageIcon(getClass().getResource("/org/hplcretentionpredictor/images/forward.png")));
			jbtnNextStep.setText("Next Step  ");
			jbtnNextStep.setEnabled(false);
			jbtnNextStep.setBounds(new Rectangle(760, 572, 178, 36));
			jbtnNextStep.setActionCommand("Next Step2");
		}
		return jbtnNextStep;
	}

	/**
	 * This method initializes jpanelStep6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStep6() {
		if (jpanelStep6 == null) {
			jlblPercent = new JLabel();
			jlblPercent.setBounds(new Rectangle(428, 32, 19, 16));
			jlblPercent.setText("%");
			jlblToleranceWindow = new JLabel();
			jlblToleranceWindow.setBounds(new Rectangle(12, 32, 289, 16));
			jlblToleranceWindow.setText("Select retention time tolerance window confidence:");
			jlblStatus2 = new JLabel();
			jlblStatus2.setBounds(new Rectangle(40, 472, 173, 16));
			jlblStatus2.setText("Status:");
			jpanelStep6 = new JPanel();
			jpanelStep6.setLayout(null);
			jpanelStep6.setBorder(BorderFactory.createTitledBorder(null, "Step #6: Predict Retention of Compounds", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep6.setVisible(true);
			jpanelStep6.setBounds(new Rectangle(1424, 0, 461, 573));
			jpanelStep6.add(getJProgressBar1(), null);
			jpanelStep6.add(jlblStatus2, null);
			jpanelStep6.add(getJbtnPredict(), null);
			jpanelStep6.add(getJScrollPane1(), null);
			jpanelStep6.add(jlblToleranceWindow, null);
			jpanelStep6.add(getJtxtWindowConfidence(), null);
			jpanelStep6.add(jlblPercent, null);
		}
		return jpanelStep6;
	}

	/**
	 * This method initializes jProgressBar1	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar1() {
		if (jProgressBar2 == null) {
			jProgressBar2 = new JProgressBar();
			jProgressBar2.setBounds(new Rectangle(40, 492, 381, 25));
			jProgressBar2.setStringPainted(false);
		}
		return jProgressBar2;
	}

	/**
	 * This method initializes jbtnPredict	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnPredict() {
		if (jbtnPredict == null) {
			jbtnPredict = new JButton();
			jbtnPredict.setBounds(new Rectangle(128, 520, 213, 36));
			jbtnPredict.setText("Predict Retention Times");
			jbtnPredict.setActionCommand("Predict");
		}
		return jbtnPredict;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setBounds(new Rectangle(8, 60, 445, 413));
			jScrollPane2.setViewportView(getJtablePredictions());
		}
		return jScrollPane2;
	}
	
	/**
	 * This method initializes jtablePredictions	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJtablePredictions() {
		if (jtablePredictions == null) 
		{
			Object[] columnNames = {"Compound", "Predicted tR (min)", "Tolerance window (+/- min)"};
			tmPredictionModel = new NoEditTableModel(columnNames, 0);
			jtablePredictions = new JTable(tmPredictionModel);
			
			jtablePredictions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			jtablePredictions.setCellSelectionEnabled(true);

			jtablePredictions.setBounds(new Rectangle(0, 0, 20, 20));
			
			jtablePredictions.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			jtablePredictions.getColumnModel().getColumn(0).setPreferredWidth(110);
			jtablePredictions.getColumnModel().getColumn(1).setPreferredWidth(100);
			
			jtablePredictions.setAutoCreateColumnsFromModel(false);
		}
		return jtablePredictions;
	}

	/**
	 * This method initializes jpanelStep5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStep52() {
		if (jpanelStep5 == null) {
			jlblColumnRating = new JLabel();
			jlblColumnRating.setBounds(new Rectangle(284, 452, 141, 17));
			jlblColumnRating.setText("");
			jlblColumnRatingName = new JLabel();
			jlblColumnRatingName.setBounds(new Rectangle(56, 452, 225, 17));
			jlblColumnRatingName.setText("Your column\'s rating:");
			jlblColumnRatingName.setFont(new Font("Dialog", Font.BOLD, 12));
			jlblExpectedStandardDeviation = new JLabel();
			jlblExpectedStandardDeviation.setBounds(new Rectangle(284, 424, 141, 17));
			jlblExpectedStandardDeviation.setText("");
			jlblExpectedStandardDeviationName = new JLabel();
			jlblExpectedStandardDeviationName.setBounds(new Rectangle(56, 424, 225, 17));
			jlblExpectedStandardDeviationName.setText("Most probable error for a new column:");
			jlblExpectedStandardDeviationName.setFont(new Font("Dialog", Font.BOLD, 12));
			jlblStandardDeviation = new JLabel();
			jlblStandardDeviation.setBounds(new Rectangle(284, 396, 141, 17));
			jlblStandardDeviation.setText("");
			jlblStandardDeviationName = new JLabel();
			jlblStandardDeviationName.setBounds(new Rectangle(56, 396, 225, 17));
			jlblStandardDeviationName.setText("Your overall prediction error:");
			jlblStandardDeviationName.setFont(new Font("Dialog", Font.BOLD, 12));
			jlblBad = new JLabel();
			jlblBad.setBounds(new Rectangle(416, 504, 41, 16));
			jlblBad.setText("Poor");
			jlblGood = new JLabel();
			jlblGood.setBounds(new Rectangle(12, 504, 61, 16));
			jlblGood.setText("Like new");
			jlblInstructions = new JLabel();
			jlblInstructions.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblInstructions.setHorizontalTextPosition(SwingConstants.CENTER);
			jlblInstructions.setText("<html>Enter the retention times of each of the test compounds in the \"Measured tR\" column. Either enter them manually or click the \"Find retention times automatically...\" button to automatically extract them from your LC-MS data file.  </html>");
			jlblInstructions.setVerticalAlignment(SwingConstants.CENTER);
			jlblInstructions.setBounds(new Rectangle(12, 28, 457, 73));
			jlblInstructions.setHorizontalAlignment(SwingConstants.CENTER);
			jpanelStep5 = new JPanel();
			jpanelStep5.setLayout(null);
			jpanelStep5.setBounds(new Rectangle(940, 0, 481, 573));
			jpanelStep5.setBorder(BorderFactory.createTitledBorder(null, "Step #5: Check System Suitability", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, new Font("sansserif", Font.BOLD, 12), new Color(59, 59, 59)));
			jpanelStep5.add(jlblInstructions, null);
			jpanelStep5.add(getJbtnAutomaticDetermineTestCompounds(), null);
			jpanelStep5.add(jlblGood, null);
			jpanelStep5.add(jlblBad, null);
			jpanelStep5.add(jlblStandardDeviationName, null);
			jpanelStep5.add(jlblStandardDeviation, null);
			jpanelStep5.add(jlblExpectedStandardDeviationName, null);
			jpanelStep5.add(jlblExpectedStandardDeviation, null);
			jpanelStep5.add(jlblColumnRatingName, null);
			jpanelStep5.add(jlblColumnRating, null);
			jpanelStep5.add(getJSliderIndicator(), null);
			jpanelStep5.add(getJScrollPaneTestCompounds(), null);
		}
		return jpanelStep5;
	}

	/**
	 * This method initializes jbtnAutomaticDetermineTestCompounds	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnAutomaticDetermineTestCompounds() {
		if (jbtnAutomaticDetermineTestCompounds == null) {
			jbtnAutomaticDetermineTestCompounds = new JButton();
			jbtnAutomaticDetermineTestCompounds.setBounds(new Rectangle(132, 348, 221, 36));
			jbtnAutomaticDetermineTestCompounds.setText("Find retention times automatically...");
			jbtnAutomaticDetermineTestCompounds.setActionCommand("Automatically Determine Test Compound Retention Times");
		}
		return jbtnAutomaticDetermineTestCompounds;
	}

	/**
	 * This method initializes jSliderIndicator	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private SliderIndicator getJSliderIndicator() {
		if (jSliderIndicator == null) {
			jSliderIndicator = new SliderIndicator();
			jSliderIndicator.setBounds(new Rectangle(76, 488, 329, 69));
		}
		return jSliderIndicator;
	}

	/**
	 * This method initializes jScrollPaneTestCompounds	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPaneTestCompounds() {
		if (jScrollPaneTestCompounds == null) {
			jScrollPaneTestCompounds = new JScrollPane();
			jScrollPaneTestCompounds.setBounds(new Rectangle(12, 104, 457, 237));
			jScrollPaneTestCompounds.setViewportView(getJtableTestCompoundPredictions());
		}
		return jScrollPaneTestCompounds;
	}

	/**
	 * This method initializes jtableTestCompoundPredictions	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJtableTestCompoundPredictions() {
		if (jtableTestCompoundPredictions == null) {

			Object[] columnNames = {"Compound", "m/z", "Predicted tR (min)", "Measured tR (min)", "Error (min)"};
			Object[][] data = new Object[GlobalsDan.TestCompoundNameArray.length][5];
			for (int i = 0; i < GlobalsDan.TestCompoundNameArray.length; i++)
			{
				data[i][0] = GlobalsDan.TestCompoundNameArray[i];
				String str = "";
				for (int j = 0; j < GlobalsDan.TestCompoundMZArray[i].length; j++)
				{
					str += GlobalsDan.TestCompoundMZArray[i][j];
					if (j < GlobalsDan.TestCompoundMZArray[i].length - 1)
						str += ", ";
				}
				data[i][1] = str;
				data[i][2] = (Object)0.0;
				data[i][3] = (String)"";
				data[i][4] = (Object)0.0;
			}
			
			m_tmTestCompoundsModel = new TestCompoundsTableModel(data, columnNames);
			jtableTestCompoundPredictions = new JTable(m_tmTestCompoundsModel);
			
			jtableTestCompoundPredictions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			jtableTestCompoundPredictions.setCellSelectionEnabled(true);

			jtableTestCompoundPredictions.getColumnModel().getColumn(0).setPreferredWidth(120);

			jtableTestCompoundPredictions.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			JTextField jtf = new JTextField();
			TableCellEditorCustom cellEditor = new TableCellEditorCustom(jtf);
			jtableTestCompoundPredictions.getColumnModel().getColumn(3).setCellEditor(cellEditor);
		}
		return jtableTestCompoundPredictions;
	}

	/**
	 * This method initializes jtxtWindowConfidence	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtWindowConfidence() {
		if (jtxtWindowConfidence == null) {
			jtxtWindowConfidence = new JTextField();
			jtxtWindowConfidence.setBounds(new Rectangle(360, 28, 64, 26));
			jtxtWindowConfidence.setHorizontalAlignment(JTextField.TRAILING);
			jtxtWindowConfidence.setText("95.0");
		}
		return jtxtWindowConfidence;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
