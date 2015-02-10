package org.measureyourgradient;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.Rectangle;

import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.measureyourgradient.TopPanel.SpecialTableModel;

import boswell.graphcontrol.GraphControl;

import java.awt.GridBagConstraints;
import javax.swing.JProgressBar;
import javax.swing.JLabel;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.JEditorPane;

public class TopPanel2 extends JPanel implements TableModelListener, Scrollable, ComponentListener
{
	private static final long serialVersionUID = 1L;
	public JButton jbtnPreviousStep = null;
	public JPanel jpanelGradientProfile = null;
	public GraphControl m_GraphControlGradient = null;
	public JPanel jpanelFlowProfile = null;
	public GraphControl m_GraphControlFlowRate = null;
	public JPanel jpanelStep5 = null;
	public JScrollPane jScrollPane = null;
	public JTable jtableOutput = null;
	public JButton jbtnCalculate = null;
	public NoEditTableModel tmOutputModel = null;
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
	private JLabel jLabel1111 = null;
	public NoEditTableModel tmPredictionModel = null;
	private JLabel jlblPreColumnVolume = null;
	private JLabel jlblMixingVolumeLabel = null;
	private JLabel jlblNonMixingVolumeLabel = null;
	private JLabel jlblTotalGradientDelayVolumeLabel = null;
	public JLabel jlblMixingVolume = null;
	public JLabel jlblNonMixingVolume = null;
	public JLabel jlblTotalGradientDelayVolume = null;
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
		this.setLayout(null);
		this.setBackground(Color.white);
		this.setBounds(new Rectangle(0, 0, 943, 615));
        this.setPreferredSize(new Dimension(890,570));
        this.setMinimumSize(new Dimension(890,570));

		m_GraphControlGradient = new GraphControl();
		m_GraphControlGradient.setBounds(new Rectangle(4, 16, 461, 285));
		m_GraphControlGradient.setControlsEnabled(false);

		m_GraphControlFlowRate = new GraphControl();
		m_GraphControlFlowRate.setBounds(new Rectangle(3, 16, 462, 241));
		m_GraphControlFlowRate.setControlsEnabled(false);

		this.add(getJbtnPreviousStep(), null);
		this.add(getJpanelGradientProfile(), null);
		this.add(getJpanelFlowProfile(), null);
		this.setVisible(true);
		this.add(getJbtnHelp(), null);
		this.add(getJpanelStep5(), null);
		
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
			jbtnPreviousStep.setIcon(new ImageIcon(getClass().getResource("/org/measureyourgradient/images/back.png")));
			jbtnPreviousStep.setText("  Previous Step");
			jbtnPreviousStep.setSize(new Dimension(178, 34));
			jbtnPreviousStep.setLocation(new Point(6, 576));
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
			jpanelGradientProfile = new JPanel();
			jpanelGradientProfile.setLayout(null);
			jpanelGradientProfile.setPreferredSize(new Dimension(615, 477));
			jpanelGradientProfile.setBorder(BorderFactory.createTitledBorder(null, "Back-Calculated Gradient Program", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelGradientProfile.setBackground(Color.white);
			jpanelGradientProfile.setSize(new Dimension(469, 305));
			jpanelGradientProfile.setLocation(new Point(6, 0));
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
			jpanelFlowProfile = new JPanel();
			jpanelFlowProfile.setLayout(null);
			jpanelFlowProfile.setPreferredSize(new Dimension(615, 477));
			jpanelFlowProfile.setBorder(BorderFactory.createTitledBorder(null, "Back-Calculated Uracil Dead Time vs. Eluent Composition", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelFlowProfile.setBackground(Color.white);
			jpanelFlowProfile.setSize(new Dimension(469, 261));
			jpanelFlowProfile.setLocation(new Point(6, 308));
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
		if (jpanelStep5 == null) {
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
			jLabel1111 = new JLabel();
			jLabel1111.setBounds(new Rectangle(40, 472, 173, 16));
			jLabel1111.setText("Status:");
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
			jpanelStep5 = new JPanel();
			jpanelStep5.setLayout(null);
			jpanelStep5.setBorder(BorderFactory.createTitledBorder(null, "Step #4: Back-Calculate Gradient and Dead Time Profiles", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStep5.setBackground(Color.white);
			jpanelStep5.setBounds(new Rectangle(476, 0, 461, 569));
			jpanelStep5.add(getJScrollPane(), null);
			jpanelStep5.add(getJbtnCalculate(), null);
			jpanelStep5.add(jlblIterationNumber, null);
			jpanelStep5.add(jLabel, null);
			jpanelStep5.add(jLabel1, null);
			jpanelStep5.add(jLabel11, null);
			jpanelStep5.add(jLabel111, null);
			jpanelStep5.add(jlblVariance, null);
			jpanelStep5.add(jlblPhase, null);
			jpanelStep5.add(jlblTimeElapsed, null);
			jpanelStep5.add(jLabel12, null);
			jpanelStep5.add(jlblLastVariance, null);
			jpanelStep5.add(jLabel121, null);
			jpanelStep5.add(jlblPercentImprovement, null);
			jpanelStep5.add(getJProgressBar(), null);
			jpanelStep5.add(jLabel1111, null);
			jpanelStep5.add(jlblPreColumnVolume, null);
			jpanelStep5.add(jlblMixingVolumeLabel, null);
			jpanelStep5.add(jlblNonMixingVolumeLabel, null);
			jpanelStep5.add(jlblTotalGradientDelayVolumeLabel, null);
			jpanelStep5.add(jlblMixingVolume, null);
			jpanelStep5.add(jlblNonMixingVolume, null);
			jpanelStep5.add(jlblTotalGradientDelayVolume, null);
		}
		return jpanelStep5;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(8, 20, 445, 225));
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
			
			jtableOutput.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtableOutput.setBounds(new Rectangle(0, 0, 20, 20));
			
			jtableOutput.getColumnModel().getColumn(0).setPreferredWidth(200);
			
			jtableOutput.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

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
			jbtnCalculate.setBounds(new Rectangle(128, 520, 213, 34));
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
			jbtnHelp.setLocation(new Point(572, 576));
			jbtnHelp.setSize(new Dimension(178, 34));
			jbtnHelp.setEnabled(false);
			jbtnHelp.setForeground(Color.blue);
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
			jProgressBar.setBounds(new Rectangle(40, 492, 381, 19));
			jProgressBar.setStringPainted(false);
			jProgressBar.setBackground(Color.white);
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
			this.jbtnHelp.setLocation(size.width - 6 - jbtnHelp.getWidth() - 10 - jbtnHelp.getWidth(), size.height - jbtnHelp.getHeight() - 6);
			this.jpanelStep5.setLocation(((size.width) / 2) + 3, jpanelStep5.getY());
			this.jpanelStep5.setSize(size.width - jpanelStep5.getX() - 6, size.height - 6 - jbtnHelp.getHeight() - 6);
			this.jbtnCalculate.setLocation((jpanelStep5.getWidth() / 2) - (jbtnCalculate.getWidth() / 2), jpanelStep5.getHeight() - 15 - jbtnCalculate.getHeight());
			this.jProgressBar.setLocation((jpanelStep5.getWidth() / 2) - (jProgressBar.getWidth() / 2), jbtnCalculate.getY() - jProgressBar.getHeight() - 12);
			this.jLabel1111.setLocation(jProgressBar.getX(), jProgressBar.getY() - 4 - jLabel1111.getHeight());

			this.jlblTotalGradientDelayVolumeLabel.setLocation(jLabel1111.getX() + 16, jLabel1111.getY() - 12 - jlblTotalGradientDelayVolumeLabel.getHeight());
			this.jlblNonMixingVolumeLabel.setLocation(jLabel1111.getX() + 16, jlblTotalGradientDelayVolumeLabel.getY() - 4 - jlblNonMixingVolumeLabel.getHeight());
			this.jlblMixingVolumeLabel.setLocation(jLabel1111.getX() + 16, jlblNonMixingVolumeLabel.getY() - 4 - jlblMixingVolumeLabel.getHeight());
			this.jlblPreColumnVolume.setLocation(jLabel1111.getX(), jlblMixingVolumeLabel.getY() - 4 - jlblPreColumnVolume.getHeight());
			
			this.jLabel111.setLocation(jLabel1111.getX(), jlblPreColumnVolume.getY() - 12 - jLabel111.getHeight());
			this.jLabel11.setLocation(jLabel111.getX(), jLabel111.getY() - 4 - jLabel11.getHeight());
			this.jLabel121.setLocation(jLabel11.getX(), jLabel11.getY() - 4 - jLabel121.getHeight());
			this.jLabel12.setLocation(jLabel121.getX(), jLabel121.getY() - 4 - jLabel12.getHeight());
			this.jLabel1.setLocation(jLabel12.getX(), jLabel12.getY() - 4 - jLabel1.getHeight());
			this.jLabel.setLocation(jLabel1.getX(), jLabel1.getY() - 4 - jLabel.getHeight());
			this.jScrollPane.setSize(jpanelStep5.getWidth() - 8 - 8, jLabel.getY() - 6 - jScrollPane.getY());
			
			this.jlblTotalGradientDelayVolume.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblTotalGradientDelayVolume.getWidth(), jlblTotalGradientDelayVolumeLabel.getY());
			this.jlblNonMixingVolume.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblNonMixingVolume.getWidth(), jlblNonMixingVolumeLabel.getY());
			this.jlblMixingVolume.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblMixingVolume.getWidth(), jlblMixingVolumeLabel.getY());

			this.jlblTimeElapsed.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblTimeElapsed.getWidth(), jLabel111.getY());
			this.jlblPhase.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblPhase.getWidth(), jLabel11.getY());
			this.jlblPercentImprovement.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblPercentImprovement.getWidth(), jLabel121.getY());
			this.jlblLastVariance.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblLastVariance.getWidth(), jLabel12.getY());
			this.jlblVariance.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblVariance.getWidth(), jLabel1.getY());
			this.jlblIterationNumber.setLocation((jProgressBar.getX() + jProgressBar.getWidth()) - this.jlblIterationNumber.getWidth(), jLabel.getY());
			this.jpanelGradientProfile.setSize(size.width - jpanelStep5.getX() - 6, (size.height - 6 - jbtnHelp.getHeight() - 6 - 6) / 2);
			this.m_GraphControlGradient.setSize(jpanelGradientProfile.getWidth() - 3 - 5, jpanelGradientProfile.getHeight() - 16 - 3);
			this.m_GraphControlGradient.repaint();
			this.jpanelFlowProfile.setLocation(jpanelFlowProfile.getX(), jpanelGradientProfile.getY() + jpanelGradientProfile.getHeight() + 6);
			this.jpanelFlowProfile.setSize(jpanelGradientProfile.getWidth(), size.height - jpanelFlowProfile.getY() - 6 - jbtnHelp.getHeight() - 6);
			this.m_GraphControlFlowRate.setSize(jpanelFlowProfile.getWidth() - 3 - 5, jpanelFlowProfile.getHeight() - 16 - 3);
			this.m_GraphControlFlowRate.repaint();
			/*this.jpanelStep4.setSize(this.jpanelStep4.getWidth(), size.height - 438);
			this.jScrollPane1.setSize(this.jScrollPane1.getWidth(), size.height - 438 - 28);
			this.jtableMeasuredRetentionTimes.revalidate();
			this.jbtnNextStep.setLocation((int)size.getWidth() - this.jbtnNextStep.getWidth() - 6, (int)size.getHeight() - jbtnNextStep.getHeight() - 6);
			this.jbtnHelp.setLocation(this.jbtnNextStep.getLocation().x - this.jbtnHelp.getWidth() - 10, (int)size.getHeight() - jbtnHelp.getHeight() - 6);
			this.jbtnPreloadedValues.setLocation(this.jbtnPreloadedValues.getLocation().x, (int)size.getHeight() - jbtnPreloadedValues.getHeight() - 6);
			this.jpanelSimulatedChromatogram.setSize(size.width - jpanelSimulatedChromatogram.getLocation().x - 6, ((size.height - 6 - 6 - this.jbtnNextStep.getHeight()) * 5) / 10);
			this.m_GraphControlTemp.setSize(jpanelSimulatedChromatogram.getWidth() - 3 - 5, jpanelSimulatedChromatogram.getHeight() - 16 - 3);
			this.m_GraphControlTemp.repaint();
			this.jpanelFlowProfile.setLocation(jpanelFlowProfile.getX(), jpanelSimulatedChromatogram.getY() + jpanelSimulatedChromatogram.getHeight() + 6);
			this.jpanelFlowProfile.setSize(jpanelSimulatedChromatogram.getWidth(), size.height - jpanelFlowProfile.getY() - 6 - 6 - this.jbtnNextStep.getHeight());
			this.m_GraphControlHoldUp.setSize(jpanelFlowProfile.getWidth() - 3 - 5, jpanelFlowProfile.getHeight() - 16 - 3);
			this.m_GraphControlHoldUp.repaint();*/
		}
		// TODO Auto-generated method stub
		
	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
