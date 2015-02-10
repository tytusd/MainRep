package boswell.peakfinderlc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mr.go.sgfilter.SGFilter;

import boswell.graphcontrol.AutoScaleEvent;
import boswell.graphcontrol.AutoScaleListener;

public class PeakFinderDialog extends JDialog implements ActionListener, KeyListener, FocusListener, AutoScaleListener, ListSelectionListener
{
	private static final long serialVersionUID = 1L;

	TopPanel contentPane = null;
	public JScrollPane jMainScrollPane = null;
	
	public double[] m_rawEIC = null;
	public double[] m_smoothEIC = null;
	
	public double[][][] m_MzData = null;
	//public Double[] m_MzMasses = null;
	//public double[] m_dRetentionTimes = null;
	//int m_iSpectraCount = 0;
	
	private double[] m_sgcoefficients = null;
	private SGFilter m_sgfilter = null;

    public double[][] m_dDeadTimeArray;
    public double[][] m_dIdealGradientProfileArray;
    public LinearInterpolationFunction m_InterpolatedIdealGradientProfile;
	public double[][] m_dSimpleGradientArray;
    public LinearInterpolationFunction m_InterpolatedGradientProfile = null;  //  @jve:decl-index=0:
    public double m_dMixingVolume = 0.001;
    public double m_dNonMixingVolume = 0.001;
    public double m_dInstrumentDeadTime = 0;
    public double m_dFlowRate = 0.4;
    public LinearInterpolationFunction m_InterpolatedDeadTimeProfile;  //  @jve:decl-index=0:
    public double m_dColumnInnerDiameter = 2.1;
    public double m_dColumnLength = 100;
    public double m_dTheoreticalPlates = 10000;
    public double[] m_dPredictedRetentionTimes;
    public double[] m_dPredictedPeakWidths;
    Vector<double[]> m_peaks = new Vector<double[]>();
    
    // Final output data
    public double[] m_dSelectedRetentionTimes = null;
    public boolean[] m_bSkippedStandards = null;
    public int[] m_iSelectedPeakRank = null;
    public boolean m_bOkPressed = false;
    
    String[] m_strStationaryPhaseArray = null;
    String[] m_strStandardCompoundsNameArray = null;
    double[][] m_dStandardCompoundsMZArray = null;
    double[][][] m_dIsocraticDataArray = null;
    
    boolean m_bDoNotUpdateTable = false;
    
    double m_dtstep = 0;
	boolean m_bEditable = true;

	/**
	 * This is the xxx default constructor
	 */
	public PeakFinderDialog(String[] strCompoundsNameArray, double[][] dCompoundsMZArray, double[][][] dCompoundsIsocraticDataArray) 
	{
	    super();
	    this.m_strStandardCompoundsNameArray = strCompoundsNameArray;
	    this.m_dStandardCompoundsMZArray = dCompoundsMZArray;
	    this.m_dIsocraticDataArray = dCompoundsIsocraticDataArray;
		initialize();
	}

	public PeakFinderDialog(Frame owner, boolean modal, String[] strCompoundsNameArray, double[][] dCompoundsMZArray, double[][][] dCompoundsIsocraticDataArray) 
	{
		super(owner, modal);
	    this.m_strStandardCompoundsNameArray = strCompoundsNameArray;
	    this.m_dStandardCompoundsMZArray = dCompoundsMZArray;
	    this.m_dIsocraticDataArray = dCompoundsIsocraticDataArray;
		initialize();
	    
	}

	public PeakFinderDialog(JDialog owner, boolean modal, String[] strCompoundsNameArray, double[][] dCompoundsMZArray, double[][][] dCompoundsIsocraticDataArray) 
	{
		super(owner, modal);
	    this.m_strStandardCompoundsNameArray = strCompoundsNameArray;
	    this.m_dStandardCompoundsMZArray = dCompoundsMZArray;
	    this.m_dIsocraticDataArray = dCompoundsIsocraticDataArray;
		initialize();
	    
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() 
	{
        this.setTitle("Peak Finder");
        this.setSize(new Dimension(1100, 700));
        this.setModal(true);
		
		//	    this.setPreferredSize(new Dimension(1056, 650));

		//Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
        //    SwingUtilities.invokeAndWait(new Runnable() 
        //    {
        //        public void run() {
                	createGUI();
        //        }
        //    });
        } catch (Exception e) { 
            System.err.println("createGUI didn't complete successfully");
            System.err.println(e.getMessage());
            System.err.println(e.getLocalizedMessage());
            System.err.println(e.toString());
            System.err.println(e.getStackTrace());
            System.err.println(e.getCause());
        }
        
        // Initialize output arrays
        m_bSkippedStandards = new boolean[this.m_strStandardCompoundsNameArray.length];
        for (int i = 0; i < m_bSkippedStandards.length; i++)
        	m_bSkippedStandards[i] = false;

        m_dSelectedRetentionTimes = new double[this.m_strStandardCompoundsNameArray.length];
        for (int i = 0; i < m_dSelectedRetentionTimes.length; i++)
        	m_dSelectedRetentionTimes[i] = 0d;
        
        m_iSelectedPeakRank = new int[this.m_strStandardCompoundsNameArray.length];
        for (int i = 0; i < m_iSelectedPeakRank.length; i++)
        	m_iSelectedPeakRank[i] = -1;
	}

	public void finalInit()
	{
		// If m_bEditable is true, then use default gradient and dead time profiles
		// If m_bEditable is false, then we are providing the back-calculated gradient and dead time profiles
		if (m_bEditable)
		{

	    	// Create dead time array
	        /*this.m_dDeadTimeArray = new double[Globals.dDeadTimeArray.length][2];
	        
	        for (int i = 0; i < Globals.dDeadTimeArray.length; i++)
	        {
	        	double dVolumeInRefColumn = Math.PI * Math.pow(0.21 / 2, 2) * 10.0;
	        	double dDeadVolPerVol = (Globals.dDeadTimeArray[i][1] * 0.4) / dVolumeInRefColumn;
	        	double dNewDeadVol = dDeadVolPerVol * Math.PI * Math.pow((this.m_dColumnInnerDiameter / 2) / 10, 2) * this.m_dColumnLength / 10;
	        	this.m_dDeadTimeArray[i][0] = Globals.dDeadTimeArray[i][0];
	        	this.m_dDeadTimeArray[i][1] = (dNewDeadVol / this.m_dFlowRate) * 60;
	        }*/
	        
			// Make the interpolated dead time profile
	        //this.m_InterpolatedDeadTimeProfile = new LinearInterpolationFunction(this.m_dDeadTimeArray);
	
			// Make the interpolated ideal gradient profile
	    	this.m_InterpolatedIdealGradientProfile = new LinearInterpolationFunction(this.m_dIdealGradientProfileArray);
			
	    	// Calculate the gradient with gradient delay
	    	calculateSimpleGradient();
		}
		
    	// Calculate approximate retention times of the standards
    	predictRetentionTimes();
		
		// Select the first standard
		contentPane.jTableStandards.getSelectionModel().setSelectionInterval(0, 0);
	}
	
	public void setGradientProgram(double[][] dGradientProgram)
	{
		this.m_dIdealGradientProfileArray = dGradientProgram;
	}
	
	public void setFlowRate(double dFlowRate)
	{
		this.m_dFlowRate = dFlowRate;
	}
	
	public void setMixingVolume(double dMixingVolume)
	{
		this.m_dMixingVolume = dMixingVolume;
	}
	
	public void setNonMixingVolume(double dNonMixingVolume)
	{
		this.m_dNonMixingVolume = dNonMixingVolume;
	}
	
	public void setInstrumentDeadTime (double dInstrumentDeadTime)
	{
		this.m_dInstrumentDeadTime = dInstrumentDeadTime;
	}
	
	public void setColumnLength(double dColumnLength)
	{
		this.m_dColumnLength = dColumnLength;
	}

	public void setColumnInnerDiameter(double dColumnInnerDiameter)
	{
		this.m_dColumnInnerDiameter = dColumnInnerDiameter;
	}
	
	public void setMzData(double[][][] mzData)
	{
		this.m_MzData = mzData;
		//this.m_iSpectraCount = m_MzData.length;
	}
	
/*	public void setRetentionTimes(double[] dRetentionTimes)
	{
		this.m_dRetentionTimes = dRetentionTimes;
	}*/
	
	public void setGradientProfile(LinearInterpolationFunction GradientProfile)
	{
		this.m_InterpolatedGradientProfile = GradientProfile;
	}

	public void setDeadTimeProfile(LinearInterpolationFunction DeadTimeProfile)
	{
		this.m_InterpolatedDeadTimeProfile = DeadTimeProfile;
	}
	
	public void setEditable(boolean bEditable)
	{
		this.m_bEditable = bEditable;
	}

	public void setTStep(double dtstep)
	{
		this.m_dtstep = dtstep;
	}

    private void createGUI()
    {
        //Create and set up the content pane
    	jMainScrollPane = new JScrollPane();
    	jMainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	jMainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	setContentPane(jMainScrollPane);

    	contentPane = new TopPanel();
        contentPane.setOpaque(true);
    	jMainScrollPane.setViewportView(contentPane);
    	jMainScrollPane.revalidate();
    	
    	// Populate table with initial values
    	contentPane.m_tabModelStandards.setRowCount(0);
		for (int i = 0; i < this.m_strStandardCompoundsNameArray.length; i++)
		{
	    	Object[] data = new Object[4];
			data[0] = this.m_strStandardCompoundsNameArray[i];
			String str = "";
			for (int j = 0; j < this.m_dStandardCompoundsMZArray[i].length; j++)
			{
				str += this.m_dStandardCompoundsMZArray[i][j];
				if (j < this.m_dStandardCompoundsMZArray[i].length - 1)
					str += ", ";
			}
			data[1] = str;
			data[2] = (Object)0;
			data[3] = (Object)0.0;

			contentPane.m_tabModelStandards.addRow(data);
		}
		
    	contentPane.m_FullEICGraphControl.addAutoScaleListener(this);
    	contentPane.m_FullEICGraphControl.setYAxisTitle("Intensity");
        contentPane.m_FullEICGraphControl.setYAxisBaseUnit("counts", "cnts");
        contentPane.m_FullEICGraphControl.setYAxisRangeLimits(-1E15d, 1E15d);
        contentPane.m_FullEICGraphControl.setYAxisScientificNotation(true);
        contentPane.m_FullEICGraphControl.setYAxisRangeIndicatorsVisible(false);
        contentPane.m_FullEICGraphControl.setAutoScaleY(true);
        
        contentPane.m_FullEICGraphControl.setXAxisType(true);
        contentPane.m_FullEICGraphControl.setXAxisRangeIndicatorsVisible(false);
        contentPane.m_FullEICGraphControl.setAutoScaleX(true);
        contentPane.m_FullEICGraphControl.setSelectionCursorVisible(true);

    	contentPane.m_PeakEICGraphControl.addAutoScaleListener(this);
    	
    	contentPane.m_PeakEICGraphControl.setYAxisTitle("Intensity");
        contentPane.m_PeakEICGraphControl.setYAxisBaseUnit("counts", "cnts");
        contentPane.m_PeakEICGraphControl.setYAxisScientificNotation(true);
        contentPane.m_PeakEICGraphControl.setYAxisRangeLimits(-1E15d, 1E15d);
        contentPane.m_PeakEICGraphControl.setYAxisRangeIndicatorsVisible(false);
        contentPane.m_PeakEICGraphControl.setAutoScaleY(true);
        
        contentPane.m_PeakEICGraphControl.setXAxisType(true);
        contentPane.m_PeakEICGraphControl.setXAxisRangeIndicatorsVisible(false);
        contentPane.m_PeakEICGraphControl.setAutoScaleX(true);

		contentPane.jTableStandards.getSelectionModel().addListSelectionListener(this);
		contentPane.jTablePeaks.getSelectionModel().addListSelectionListener(this);
		contentPane.jbtnNext.addActionListener(this);
		contentPane.jbtnPrevious.addActionListener(this);
		contentPane.jbtnCancel.addActionListener(this);
		contentPane.jchkNotHere.addActionListener(this);

    	contentPane.jbtnPrevious.setEnabled(false);

        contentPane.m_FullEICGraphControl.repaint();
        contentPane.m_PeakEICGraphControl.repaint();
    }

    public void actionPerformed(ActionEvent evt) 
	{
	    String strActionCommand = evt.getActionCommand();
	    if (strActionCommand == "Next Standard")
	    {
	    	contentPane.jbtnPrevious.setEnabled(true);
	    	
	    	int iSelectedRow = this.contentPane.jTableStandards.getSelectedRow();
	    	if (iSelectedRow < contentPane.m_tabModelStandards.getRowCount() - 1)
	    	{
	    		contentPane.jTableStandards.setRowSelectionInterval(iSelectedRow + 1, iSelectedRow + 1);
	    	}
	    	if (iSelectedRow == contentPane.m_tabModelStandards.getRowCount() - 1)
	    	{
	    		// Finished - copy values to software
	    		
	    		// First, check to see if all peaks were looked at
	    		boolean bNotPickedYet = false;
	    		for (int i = 0; i < this.m_iSelectedPeakRank.length; i++)
	    		{
	    			if (m_iSelectedPeakRank[i] == -1)
	    				bNotPickedYet = true;
	    		}
	    		
	    		if (bNotPickedYet)
	    		{
		    		int ret = JOptionPane.showConfirmDialog(null, "You did not select a peak for at least one of the standards. Are you sure you want to continue?", "Are you sure you're done?", JOptionPane.YES_NO_OPTION);
		    	
		    		if (ret == JOptionPane.YES_OPTION)
		    		{
		    			m_bOkPressed = true;
		    			this.setVisible(false);
		    		}
	    		}
	    		else
	    		{
	    			m_bOkPressed = true;
	    			this.setVisible(false);
	    		}
	    	}
	    }
	    else if (strActionCommand == "Previous Standard")
	    {
	    	int iSelectedRow = this.contentPane.jTableStandards.getSelectedRow();
	    	
	    	if (iSelectedRow > 0)
	    		contentPane.jTableStandards.setRowSelectionInterval(iSelectedRow - 1, iSelectedRow - 1);
	    }
	    else if (strActionCommand == "Cancel")
	    {
	    	m_bOkPressed = false;
			this.setVisible(false);
	    }
	    else if (strActionCommand == "Skip this one. I'll enter the retention time myself later.")
	    {
	    	int iSelectedRow = this.contentPane.jTableStandards.getSelectedRow();
	    	m_bSkippedStandards[iSelectedRow] = !m_bSkippedStandards[iSelectedRow];
	    	updateStandardsTable();
	    }
	}
    
    public class CompareRank implements Comparator<double[]>
    {
        @Override
        public int compare(double[] o1, double[] o2) 
        {
            return (o1[3] > o2[3] ? -1 : (o1[3] == o2[3] ? 0 : 1));
        }
    }
    
    public void selectNewStandard(int iIndexOfStandard)
    {
		NumberFormat formatter = new DecimalFormat("#0.0");
		NumberFormat formatter2 = new DecimalFormat("#0.000");
		NumberFormat formatter3 = new DecimalFormat("#0.###E0");

		// Our mzData holds combined masses in EICs
		double dSelectedMasses[] = m_dStandardCompoundsMZArray[iIndexOfStandard];
    	
    	if (iIndexOfStandard == contentPane.m_tabModelStandards.getRowCount() - 1)
    	{
    		contentPane.jbtnNext.setText("Finished");
    		contentPane.jbtnNext.setIcon(null);
    	}
    	else
    	{
	    	contentPane.jbtnNext.setText("Next Standard  ");
			contentPane.jbtnNext.setIcon(new ImageIcon(getClass().getResource("/boswell/peakfinderlc/images/forward.png")));
    	}
    	
    	if (iIndexOfStandard == 0)
    		contentPane.jbtnPrevious.setEnabled(false);
    	else
    		contentPane.jbtnPrevious.setEnabled(true);
    	    		
   		contentPane.jchkNotHere.setSelected(this.m_bSkippedStandards[iIndexOfStandard]);
    	
   		String str = "";
   		for (int i = 0; i < dSelectedMasses.length; i++)
   		{
   			str += dSelectedMasses[i];
   			if (i < dSelectedMasses.length - 2)
   			{
   				str += ", ";
   			}
   			else if (i < dSelectedMasses.length - 1)
   			{
   				str += " and ";
   			}
   		}
    	contentPane.jPanelEICTotal.setBorder(BorderFactory.createTitledBorder(null, "Extracted ion chromatogram of " + str + " m/z", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
    	contentPane.jPanelPeakPick.setBorder(BorderFactory.createTitledBorder(null, "Choose the correct peak for " + m_strStandardCompoundsNameArray[iIndexOfStandard], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
        
    	// Create new data array for the EIC
    	int iSpectraCount = m_MzData[iIndexOfStandard].length;
        m_rawEIC = new double[iSpectraCount];
        double m_EICderivative[] = new double[iSpectraCount];
        double m_EICSecondDerivative[] = new double[iSpectraCount];
        double intensity;
        
        // Create the m_rawEIC array
        for (int iSpectrum = 0; iSpectrum < iSpectraCount; iSpectrum++)
        {
        	m_rawEIC[iSpectrum] = m_MzData[iIndexOfStandard][iSpectrum][1];
        }
        
        // Determine number of points to filter
        // # of points per second
        double dTotalTime = m_MzData[iIndexOfStandard][m_MzData[iIndexOfStandard].length - 1][0] - m_MzData[iIndexOfStandard][0][0];
        double dPointsPerSecond = iSpectraCount / dTotalTime;
        double dPeakWidthInSeconds = this.m_dPredictedPeakWidths[iIndexOfStandard] * 60;
        double dK = 0.5;
        int iNumPointsInFilter = (int)(dK * dPeakWidthInSeconds * dPointsPerSecond);
        
        int iOrder = 3;
		if (iNumPointsInFilter <= 2)
			iOrder = iNumPointsInFilter;
		
		if (iNumPointsInFilter >= 2)
		{
			// Run Savitzky-Golay filter
			m_sgfilter = new SGFilter(iNumPointsInFilter, iNumPointsInFilter);
			m_sgcoefficients = SGFilter.computeSGCoefficients(iNumPointsInFilter, iNumPointsInFilter, iOrder);
			m_smoothEIC = m_sgfilter.smooth(m_rawEIC, m_sgcoefficients);
			for (int i = 0; i < 10; i++)
			{
				m_smoothEIC = m_sgfilter.smooth(m_smoothEIC, m_sgcoefficients);
			}
		}
		else
		{
			m_smoothEIC = m_rawEIC;
		}
		
		contentPane.m_FullEICGraphControl.RemoveAllSeries();
        int iExtractedIonRawPlotIndex = contentPane.m_FullEICGraphControl.AddSeries("Extracted ion chromatogram", new Color(0, 0, 0), 1, false, false);

        // Calculate the derivative of the EIC
        m_EICderivative[0] = 0;
        m_EICderivative[m_rawEIC.length - 1] = 0;
        
        for (int i = 1; i < m_rawEIC.length - 1; i++)
        {
        	double dYBefore = (m_smoothEIC[i - 1] + m_smoothEIC[i]) / 2;
        	double dYAfter = (m_smoothEIC[i + 1] + m_smoothEIC[i]) / 2;
        	double dXBefore = (m_MzData[iIndexOfStandard][i - 1][0] + m_MzData[iIndexOfStandard][i][0]) / 2;
        	double dXAfter = (m_MzData[iIndexOfStandard][i + 1][0] + m_MzData[iIndexOfStandard][i][0]) / 2;
        	
        	double dSlope = (dYAfter - dYBefore) / (dXAfter - dXBefore);
        	m_EICderivative[i] = dSlope;
        }
        
     // Calculate the second derivative of the EIC
        m_EICSecondDerivative[0] = 0;
        m_EICSecondDerivative[m_rawEIC.length - 1] = 0;
        
        for (int i = 1; i < m_rawEIC.length - 1; i++)
        {
        	double dYBefore = (m_EICderivative[i - 1] + m_EICderivative[i]) / 2;
        	double dYAfter = (m_EICderivative[i + 1] + m_EICderivative[i]) / 2;
        	double dXBefore = (m_MzData[iIndexOfStandard][i - 1][0] + m_MzData[iIndexOfStandard][i][0]) / 2;
        	double dXAfter = (m_MzData[iIndexOfStandard][i + 1][0] + m_MzData[iIndexOfStandard][i][0]) / 2;
        	
        	double dSlope = (dYAfter - dYBefore) / (dXAfter - dXBefore);
        	m_EICSecondDerivative[i] = dSlope;
        }
        
        // Smooth the second derivative
		if (iNumPointsInFilter >= 2)
			m_EICSecondDerivative = m_sgfilter.smooth(m_EICSecondDerivative, m_sgcoefficients);

        // Find maximum intensity
        double dMaxIntensity = 0;
        for (int i = 0; i < m_rawEIC.length; i++)
        {
        	if (m_smoothEIC[i] > dMaxIntensity)
        		dMaxIntensity = m_smoothEIC[i];
        }
        
        // Find the peaks (any place where the derivative crosses zero with a negative slope)        
        m_peaks.clear();
        for (int i = 0; i < m_rawEIC.length - 1; i++)
        {
        	double dYBefore = m_EICderivative[i];
        	double dYAfter = m_EICderivative[i + 1];
        	if (dYBefore >= 0 && dYAfter < 0)
        	{
        		double dXBefore = m_MzData[iIndexOfStandard][i][0];
            	double dXAfter = m_MzData[iIndexOfStandard][i + 1][0];
            	
        		double dFraction = (dYBefore - 0) / (dYBefore - dYAfter);
        		double dPeakTime = (dFraction * (dXAfter - dXBefore)) + dXBefore;
        		
        		// Find the intensity of the peak (signal at the peak maximum)
        		double dPeakIntensity = (dFraction * (m_smoothEIC[i + 1] - m_smoothEIC[i])) + m_smoothEIC[i];
        		
        		// Calculate peak width. How wide is the second derivative at zero?
        		// Measure time to right side of peak
        		double dRightSideTime = 0;
        		for (int j = i; j < m_rawEIC.length; j++)
        		{
        			if (m_EICSecondDerivative[j] > 0)
        			{
        				if (j == 0)
        					break;

        				double dYBeforeDD = m_EICSecondDerivative[j - 1];
        	        	double dYAfterDD = m_EICSecondDerivative[j];
        	        	double dFractionDD = (dYBeforeDD - 0) / (dYBeforeDD - dYAfterDD);
        	        	double dXBeforeDD = m_MzData[iIndexOfStandard][j - 1][0];
                    	double dXAfterDD = m_MzData[iIndexOfStandard][j][0];
        	        	dRightSideTime = (dFractionDD * (dXAfterDD - dXBeforeDD)) + dXBeforeDD;
        	        	break;
        			}
        		}
        		
        		// Measure time to left side of peak
        		double dLeftSideTime = m_MzData[iIndexOfStandard][0][0];
        		for (int j = i; j > 0; j--)
        		{
        			if (m_EICSecondDerivative[j] > 0)
        			{
        				double dYBeforeDD = m_EICSecondDerivative[j];
        	        	double dYAfterDD = m_EICSecondDerivative[j + 1];
        	        	double dFractionDD = (dYBeforeDD - 0) / (dYBeforeDD - dYAfterDD);
        	        	double dXBeforeDD = m_MzData[iIndexOfStandard][j][0];
                    	double dXAfterDD = m_MzData[iIndexOfStandard][j + 1][0];
        	        	dLeftSideTime = (dFractionDD * (dXAfterDD - dXBeforeDD)) + dXBeforeDD;
        	        	break;
        			}
        		}
        		
        		double dPeakWidthHalfHeight = dRightSideTime - dLeftSideTime;
        		
        		// Calculate peak score.
        		double TimeAt36Percent = 3; // in min
        		// Gives a value from 1 to 0 for closeness to predicted retention time
        		double dPeakScoreRetentionTime = Math.exp(-Math.abs((dPeakTime / 60) - (this.m_dPredictedRetentionTimes[iIndexOfStandard] + this.m_dInstrumentDeadTime)) / TimeAt36Percent);
        		double dPeakScoreHeight = dPeakIntensity / dMaxIntensity;
        		double ErrorAt36Percent = this.m_dPredictedPeakWidths[iIndexOfStandard];
        		double dPeakScoreWidth = Math.exp(-Math.abs((dPeakWidthHalfHeight / 60) - this.m_dPredictedPeakWidths[iIndexOfStandard]) / ErrorAt36Percent);
        		
        		double dPeakScore = Math.pow(dPeakScoreRetentionTime, 10) * Math.pow(dPeakScoreHeight, 1) * Math.pow(dPeakScoreWidth, .1);
        		// {peak time, peak intensity, peak width at half height}
        		m_peaks.add(new double[]{dPeakTime, dPeakIntensity, dPeakWidthHalfHeight, dPeakScore});
           	}
        }
        
        // Sort the peaks in descending order based on their score
        Collections.sort(m_peaks, new CompareRank());

        int iNumPeaksToShow = 20;
        if (iNumPeaksToShow > m_peaks.size())
        {
        	iNumPeaksToShow = m_peaks.size();
        }
        
        double dSumOfScores = 0;
        for (int i = 0; i < iNumPeaksToShow; i++)
        {
        	dSumOfScores += m_peaks.get(i)[3];
        }
        
        // Set the expected retention time and peak width labels
        contentPane.jlblExpectedRetentionTime.setText(formatter2.format(this.m_dPredictedRetentionTimes[iIndexOfStandard] + this.m_dInstrumentDeadTime) + " min");
        contentPane.jlblExpectedPeakWidth.setText(formatter2.format(this.m_dPredictedPeakWidths[iIndexOfStandard]) + " min");
        
        // Clear the peak rank table
        contentPane.m_tabModelPeakRank.setRowCount(0);
        
        // Fill the peak rank table
        for (int i = 0; i < iNumPeaksToShow; i++)
        {
        	double dProbability = (m_peaks.get(i)[3] / dSumOfScores) * 100;
        	String strRank = ((Integer)(i + 1)).toString() + " (" + formatter.format(dProbability) + "%)";
        	Object[] newRow = new Object[]{
        			strRank, 
        			formatter2.format(m_peaks.get(i)[0] / 60), 
        			formatter2.format(m_peaks.get(i)[2] / 60), 
        			formatter3.format(m_peaks.get(i)[1])};
        	contentPane.m_tabModelPeakRank.addRow(newRow);
        }
        
        // Select the chosen peak
        if (contentPane.m_tabModelPeakRank.getRowCount() > 0)
        {
        	if (m_iSelectedPeakRank[iIndexOfStandard] == -1)
        		contentPane.jTablePeaks.getSelectionModel().setSelectionInterval(0, 0);
        	else
        		contentPane.jTablePeaks.getSelectionModel().setSelectionInterval(m_iSelectedPeakRank[iIndexOfStandard], m_iSelectedPeakRank[iIndexOfStandard]);
        	
        }
        	
        // Plot the data
        for (int i = 0; i < m_rawEIC.length; i++)
        {
        	contentPane.m_FullEICGraphControl.AddDataPoint(iExtractedIonRawPlotIndex, m_MzData[iIndexOfStandard][i][0], m_rawEIC[i]);
        	//contentPane.m_FullEICGraphControl.AddDataPoint(iExtractedIonSmoothPlotIndex, m_dRetentionTimes[i], m_smoothEIC[i]);
        	//contentPane.m_FullEICGraphControl.AddDataPoint(iExtractedIonSmoothPlotIndex2, m_dRetentionTimes[i], m_EICderivative[i]);
        	//contentPane.m_FullEICGraphControl.AddDataPoint(iExtractedIonSmoothPlotIndex3, m_dRetentionTimes[i], m_EICSecondDerivative[i]);
        }

		contentPane.m_FullEICGraphControl.AutoScaleX();
		contentPane.m_FullEICGraphControl.AutoScaleY();
		
		contentPane.m_FullEICGraphControl.repaint();
    }
    
	public void calculateSimpleGradient()
	{
		int iNumPoints = 10000;
		// Create an array for the simple gradient
		m_dSimpleGradientArray = new double[iNumPoints][2];
				
		// Initialize the solvent mixer composition to that of the initial solvent composition
		double dMixerComposition = this.m_dIdealGradientProfileArray[0][1];
		//double dFinalTime = (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) + ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iGradientTableLength - 1, 0)).doubleValue();
		double dFinalTime = this.m_dIdealGradientProfileArray[this.m_dIdealGradientProfileArray.length - 1][0]; // in min
		double dTimeStep = dFinalTime / (iNumPoints - 1);
		
		// Start at time 0
		double dTime = 0;
		double dTotalSolventVolumeMoved = 0;
		double dNonMixingDelayTime = 0;
		
		for (int i = 0; i < iNumPoints; i++)
		{
			dTime = i * dTimeStep;
			
			m_dSimpleGradientArray[i][0] = dTime;
			m_dSimpleGradientArray[i][1] = dMixerComposition;
			
			double dSolventBInMixer = dMixerComposition * m_dMixingVolume;
							
			// Now push out a step's worth of volume from the mixer
			//dSolventBInMixer -= ((m_dFlowRate * 1000) * dTimeStep) * dMixerComposition;
			dSolventBInMixer -= (m_dFlowRate * dTimeStep) * dMixerComposition;
			
			// dSolventBInMixer could be negative if the volume pushed out of the mixer is greater than the total volume of the mixer
			if (dSolventBInMixer < 0)
				dSolventBInMixer = 0;
			
			// Now add a step's worth of new volume from the pump
			// First, find which two data points we are between
			// Find the last data point that isn't greater than our current time
			double dIncomingSolventComposition = 0;
			
			dIncomingSolventComposition = this.m_InterpolatedIdealGradientProfile.getAt(dTime - dNonMixingDelayTime);
			
			// Add to the total amount of solvent moved.
			dTotalSolventVolumeMoved += dTimeStep * m_dFlowRate;
			
			if (dTotalSolventVolumeMoved <= m_dNonMixingVolume)
				dNonMixingDelayTime += dTimeStep;
			
			if ((m_dFlowRate * dTimeStep) < m_dMixingVolume)
				dSolventBInMixer += (m_dFlowRate * dTimeStep) * dIncomingSolventComposition;
			else
			{
				// The amount of solvent entering the mixing chamber is larger than the mixing chamber. Just set the solvent composition in the mixer to that of the mobile phase.
				dSolventBInMixer = m_dMixingVolume * dIncomingSolventComposition;
			}
			
			// Calculate the new solvent composition in the mixing volume
			if ((m_dFlowRate * dTimeStep) < m_dMixingVolume)
				dMixerComposition = dSolventBInMixer / m_dMixingVolume;
			else
				dMixerComposition = dIncomingSolventComposition;
		}
		
		m_InterpolatedGradientProfile = new LinearInterpolationFunction(m_dSimpleGradientArray);
	}
	
    public void predictRetentionTimes()
    {
    	double dTimeLimit = this.m_dIdealGradientProfileArray[this.m_dIdealGradientProfileArray.length - 1][0] * 1.5;
		double dtstep = dTimeLimit * 0.001;

		m_dPredictedRetentionTimes = new double[m_dIsocraticDataArray.length];
		m_dPredictedPeakWidths = new double[m_dIsocraticDataArray.length];
		
		int iNumCompounds = m_dIsocraticDataArray.length;
		for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
		{
			InterpolationFunction IsocraticData = new InterpolationFunction(m_dIsocraticDataArray[iCompound]);

			double dIntegral = 0;
			double dtRFinal = 0;
			double dD = 0;
			double dTotalTime = 0;
			double dTotalDeadTime = 0;
			double dXPosition = 0;
			double[] dLastXPosition = {0,0};
			double[] dLastko = {0,0};
			double dXMovement = 0;
			Boolean bIsEluted = false;
			double dPhiC = 0;
			double dCurVal = 0;
			double kprime = 1;
			double dt0 = 1;
			
			for (double t = 0; t <= dTimeLimit; t += dtstep)
			{
				dPhiC = m_InterpolatedGradientProfile.getAt(dTotalTime - dIntegral) / 100;
				// Calculate k'
		    	kprime = Math.pow(10, IsocraticData.getAt(dPhiC));
				dCurVal = dtstep / kprime;
				dt0 = m_InterpolatedDeadTimeProfile.getAt(dPhiC) / 60;
				dXMovement = dCurVal / dt0;
				
				if (dXPosition >= 1)
				{
					dD = ((1 - dLastXPosition[0])/(dXPosition - dLastXPosition[0])) * (dTotalDeadTime - dLastXPosition[1]) + dLastXPosition[1]; 
				}
				else
				{
					dLastXPosition[0] = dXPosition;
					dLastXPosition[1] = dTotalDeadTime;
				}
				
				dTotalDeadTime += dXMovement * dt0;
				
				if (dXPosition >= 1)
				{
					dtRFinal = ((dD - dLastko[0])/(dIntegral - dLastko[0]))*(dTotalTime - dLastko[1]) + dLastko[1];
				}
				else
				{
					dLastko[0] = dIntegral;
					dLastko[1] = dTotalTime;
				}
				
				dTotalTime += dtstep + dCurVal;
				dIntegral += dCurVal;
				
				if (dXPosition > 1 && bIsEluted == false)
				{
					bIsEluted = true;
					break;
				}
				
				dXPosition += dXMovement;
			}
			
			if (bIsEluted)
			{
		    	double dSigma = Math.sqrt(Math.pow((dt0 * (1 + kprime)) / Math.sqrt(m_dTheoreticalPlates), 2));
		    	double dMinSigma = Math.sqrt(Math.pow(dt0 / Math.sqrt(m_dTheoreticalPlates), 2)) * 6;
		    	m_dPredictedPeakWidths[iCompound] = Math.max(dSigma * 2.355, dMinSigma); // Peak width at half height
				m_dPredictedRetentionTimes[iCompound] = dtRFinal;
			}
			else
			{
				m_dPredictedPeakWidths[iCompound] = -1;
				// Did not elute
				m_dPredictedRetentionTimes[iCompound] = -1;
			}
		}
    }
    
    public void selectNewPeak(int iSelectedPeak)
    {
		NumberFormat formatter = new DecimalFormat("#0.000");
    	int iSelectedStandard = contentPane.jTableStandards.getSelectedRow();
    	
    	// Determine range of data to plot in smaller graph
    	double dRetentionTime = m_peaks.get(iSelectedPeak)[0];
    	double dExpectedPeakWidth = this.m_dPredictedPeakWidths[iSelectedStandard] * 60;
    	double dLeftWindow = dRetentionTime - (dExpectedPeakWidth * 6);
    	double dRightWindow = dRetentionTime + (dExpectedPeakWidth * 6);
    	if (dLeftWindow < 0)
    		dLeftWindow = 0;
    	if (dRightWindow > m_MzData[iSelectedStandard][m_MzData[iSelectedStandard].length - 1][0])
    	{
    		dRightWindow = m_MzData[iSelectedStandard][m_MzData[iSelectedStandard].length - 1][0];
    	}
    	
    	contentPane.m_FullEICGraphControl.setLeftCursorPosition(dLeftWindow);
    	contentPane.m_FullEICGraphControl.setRightCursorPosition(dRightWindow);
    	contentPane.m_FullEICGraphControl.repaint();
    	
    	// Create new series for the plots
    	contentPane.m_PeakEICGraphControl.RemoveAllSeries();
    	int iExtractedIonRawPlotIndex = contentPane.m_PeakEICGraphControl.AddSeries("EIC", new Color(0,0,0), 1, false, false);
    	int iExtractedIonSmoothPlotIndex = contentPane.m_PeakEICGraphControl.AddSeries("EIC smoothed", new Color(255,0,0), 1, false, false);

    	// Plot the data
        for (int i = 0; i < m_rawEIC.length; i++)
        {
        	if (m_MzData[iSelectedStandard][i][0] >= dLeftWindow && m_MzData[iSelectedStandard][i][0] <= dRightWindow)
        	{
        		contentPane.m_PeakEICGraphControl.AddDataPoint(iExtractedIonRawPlotIndex, m_MzData[iSelectedStandard][i][0], m_rawEIC[i]);
        		contentPane.m_PeakEICGraphControl.AddDataPoint(iExtractedIonSmoothPlotIndex, m_MzData[iSelectedStandard][i][0], m_smoothEIC[i]);
        	}
        }

        contentPane.m_PeakEICGraphControl.removeAllLineMarkers();
        contentPane.m_PeakEICGraphControl.addLineMarker(dRetentionTime / 60, formatter.format(dRetentionTime / 60) + " min");
		contentPane.m_PeakEICGraphControl.AutoScaleX();
		contentPane.m_PeakEICGraphControl.AutoScaleY();
		
		contentPane.m_PeakEICGraphControl.repaint();
		
		this.m_dSelectedRetentionTimes[iSelectedStandard] = dRetentionTime / 60;
		this.m_iSelectedPeakRank[iSelectedStandard] = iSelectedPeak;
		
		m_bDoNotUpdateTable = true;
		updateStandardsTable();
    }
    
    private void updateStandardsTable()
    {
		NumberFormat formatter = new DecimalFormat("#0.000");

		for (int i = 0; i < contentPane.m_tabModelStandards.getRowCount(); i++)
    	{
    		// Check if this standard is skipped
    		if (this.m_bSkippedStandards[i])
    		{
    			contentPane.m_tabModelStandards.setValueAt("skip", i, 2);
    			contentPane.m_tabModelStandards.setValueAt("skip", i, 3);
    		}
    		else if (this.m_iSelectedPeakRank[i] == -1)
    		{
    			contentPane.m_tabModelStandards.setValueAt("", i, 2);
    			contentPane.m_tabModelStandards.setValueAt("", i, 3);
    		}
    		else
    		{
    			contentPane.m_tabModelStandards.setValueAt(m_iSelectedPeakRank[i] + 1, i, 2);
    			contentPane.m_tabModelStandards.setValueAt(formatter.format(m_dSelectedRetentionTimes[i]), i, 3);
    		}
    	}
    }
    
	//@Override
	public void keyPressed(KeyEvent arg0) 
	{
		
	}

	//@Override
	public void keyReleased(KeyEvent e) 
	{

	}

	//@Override
	public void keyTyped(KeyEvent e) 
	{
		//JTextField source = (JTextField)e.getSource();
		
		if (!((Character.isDigit(e.getKeyChar()) ||
				(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
				(e.getKeyChar() == KeyEvent.VK_DELETE) ||
				(e.getKeyChar() == KeyEvent.VK_PERIOD))))
		{
	        e.consume();
		}
		
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
		{
			//performCalculations();
		}
		
	}

	//@Override
	public void focusGained(FocusEvent e) 
	{
		
	}

	//@Override
	public void focusLost(FocusEvent e) 
	{
		//performCalculations();
	}

	//@Override
	public void valueChanged(ListSelectionEvent arg0) 
	{
		if (arg0.getSource() == contentPane.jTableStandards.getSelectionModel())
		{
			// Do not allow the selection to be removed
			if (contentPane.jTableStandards.getSelectedRow() == -1)
				contentPane.jTableStandards.setRowSelectionInterval(arg0.getFirstIndex(), arg0.getFirstIndex());
			
			//if (!m_bDoNotUpdateTable)
				selectNewStandard(contentPane.jTableStandards.getSelectedRow());
			//else
			//	m_bDoNotUpdateTable = false;
				
		}
		else if (arg0.getSource() == contentPane.jTablePeaks.getSelectionModel())
		{
			if (contentPane.m_tabModelPeakRank.getRowCount() > 0)
			{
				// Do not allow the selection to be removed
				if (contentPane.jTablePeaks.getSelectedRow() == -1)
					contentPane.jTablePeaks.setRowSelectionInterval(arg0.getFirstIndex(), arg0.getFirstIndex());
				
				selectNewPeak(contentPane.jTablePeaks.getSelectedRow());
			}
		}
	}

	@Override
	public void autoScaleChanged(AutoScaleEvent event) {
		// TODO Auto-generated method stub
		
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
