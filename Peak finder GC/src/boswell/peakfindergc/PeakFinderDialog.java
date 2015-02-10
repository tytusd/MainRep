package boswell.peakfindergc;

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
	public double[] m_dRetentionTimes = null;
	int m_iSpectraCount = 0;
	
	private double[] m_sgcoefficients = null;
	private SGFilter m_sgfilter = null;  //  @jve:decl-index=0:

    public double[][] m_dIdealTemperatureProfileArray;
    public LinearInterpolationFunction m_InterpolatedTemperatureProfile; //Linear
    public double[][] m_dHoldUpArray;
    public InterpolationFunction m_InterpolatedHoldUpProfile;  //  @jve:decl-index=0:
    public double m_V0 = 1; // in mL

	public double m_dInitialTemperature = 60;
	public double m_dInitialTime = 1;
	public double m_dColumnLength = 30;
	public double m_dInnerDiameter = 0.25;
	public double m_dFilmThickness = 0.25;
	public double m_dFlowRate = 1; // in mL/min
	public double m_dInletPressure = 45; // in kPa
	public double m_dOutletPressure = 0.000001; // in kPa
    public double m_dColumnInnerDiameter = 2.1;
    public double m_dTheoreticalPlatesPerMeter = 4433;
    public double m_dTheoreticalPlates = m_dTheoreticalPlatesPerMeter * m_dColumnLength;
    public boolean m_bConstantFlowMode = true;
    
    public double[] m_dPredictedRetentionTimes;
    public double[] m_dPredictedPeakWidths;
    Vector<Vector<double[]>> m_peaks = new Vector<Vector<double[]>>();  //  @jve:decl-index=0:
    public double[] m_dMaxIntensities;
    
    String[] m_strStationaryPhaseArray = null;
    String[] m_strStandardCompoundsNameArray = null;
    double[][] m_dStandardCompoundsMZArray = null;
    double[][] m_dIsothermalParamArray = null;

    // Final output data
    public double[] m_dSelectedRetentionTimes = null;
    public boolean[] m_bSkippedStandards = null;
    public int[] m_iSelectedPeakRank= null;
    public boolean m_bOkPressed = false;
    
    boolean m_bDoNotUpdateTable = false;
    
    double m_dtstep = 0;
	boolean m_bEditable = true;

	/**
	 * This is the xxx default constructor
	 */
	public PeakFinderDialog(String[] strCompoundsNameArray, double[][] dCompoundsMZArray, double[][] dCompoundsIsothermalParamArray) 
	{
	    super();
	    this.m_strStandardCompoundsNameArray = strCompoundsNameArray;
	    this.m_dStandardCompoundsMZArray = dCompoundsMZArray;
	    this.m_dIsothermalParamArray = dCompoundsIsothermalParamArray;
		initialize();
	}

	public PeakFinderDialog(Frame owner, boolean modal, String[] strCompoundsNameArray, double[][] dCompoundsMZArray, double[][] dCompoundsIsothermalParamArray) 
	{
		super(owner, modal);
	    this.m_strStandardCompoundsNameArray = strCompoundsNameArray;
	    this.m_dStandardCompoundsMZArray = dCompoundsMZArray;
	    this.m_dIsothermalParamArray = dCompoundsIsothermalParamArray;
		initialize();
	    
	}

	public PeakFinderDialog(JDialog owner, boolean modal, String[] strCompoundsNameArray, double[][] dCompoundsMZArray, double[][] dCompoundsIsothermalParamArray) 
	{
		super(owner, modal);
	    this.m_strStandardCompoundsNameArray = strCompoundsNameArray;
	    this.m_dStandardCompoundsMZArray = dCompoundsMZArray;
	    this.m_dIsothermalParamArray = dCompoundsIsothermalParamArray;
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
		// If m_bEditable is true, then it needs to find a good hold-up time to fit the data
		// If m_bEditable is false, then we are providing the back-calculated hold-up time profile and temperature program
		if (m_bEditable)
		{
			// Make the interpolated ideal gradient profile
	    	this.m_InterpolatedTemperatureProfile = new LinearInterpolationFunction(this.m_dIdealTemperatureProfileArray);
			
	    	// Calculate hold-up time profile
	    	calculateHoldUpTimeProfile();
	    	
	    	// Calculate approximate retention times of the standards
	    	predictRetentionTimes(0);
	    	
	    	// Now find all peaks
	    	findPeaks();
	    	
	    	// Now calculate peak ranks
	    	//calculatePeakRanks();
	    	
	    	for (int i = 0; i < m_peaks.size(); i++)
			{
				this.m_iSelectedPeakRank[i] = 0;
			}
	    	
	    	// Try 100 different hold-up times offsets to find the one that gives the best score
	    	double[][] dStoreHoldUpArray = m_dHoldUpArray;
	    	int iCount = 100;
	    	double[] dSumScore = new double[iCount];
	    	
	    	for (int iOffsetNum = 0; iOffsetNum < iCount; iOffsetNum++)
	    	{
	    		double dOffset = 0;
	  
	    		if (iOffsetNum < iCount / 2)
	    			dOffset = 0.5 + ((double)iOffsetNum / (double)iCount);
	    		else
	    			dOffset = ((double)iOffsetNum / (double)iCount) * 2.0;
	    			
	    		m_dHoldUpArray = new double[dStoreHoldUpArray.length][2];
	    		for (int i = 0; i < dStoreHoldUpArray.length; i++)
	    		{
	    			m_dHoldUpArray[i][0] = dStoreHoldUpArray[i][0];
	    			m_dHoldUpArray[i][1] = dStoreHoldUpArray[i][1] * dOffset;
	    		}
	        	this.m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
	        	
	        	predictRetentionTimes(0);
	
	        	calculatePeakRanks();
	        	
	        	// Calculate and store this hold-up time offset's score
	        	dSumScore[iOffsetNum] = scorePermutation(m_iSelectedPeakRank);
	    	}
	    	
	    	// Find the maximum sum score
	    	int iMax = 0;
	    	for (int i = 0; i < dSumScore.length; i++)
	    	{
	    		if (dSumScore[i] > dSumScore[iMax])
	    			iMax = i;
	    	}
	    	
	    	double dOffset = 0;
	    	  
			if (iMax < iCount / 2)
				dOffset = 0.5 + ((double)iMax / (double)iCount);
			else
				dOffset = ((double)iMax / (double)iCount) * 2.0;
				
			m_dHoldUpArray = new double[dStoreHoldUpArray.length][2];
			for (int i = 0; i < dStoreHoldUpArray.length; i++)
			{
				m_dHoldUpArray[i][0] = dStoreHoldUpArray[i][0];
				m_dHoldUpArray[i][1] = dStoreHoldUpArray[i][1] * dOffset;
			}
	    	this.m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
	    	
	    	predictRetentionTimes(0);
	
	    	calculatePeakRanks();
	
	    	for (int i = 0; i < m_peaks.size(); i++)
			{
	    		if (m_peaks.get(i).size() == 0)
	    		{
	    			this.m_bSkippedStandards[i] = true;
	    			this.m_iSelectedPeakRank[i] = -1;
	    			continue;
	    		}

				this.m_dSelectedRetentionTimes[i] = m_peaks.get(i).get(0)[0] / 60;
				
				// Check if this peak is predicted to be past the end of the run
				if (this.m_dPredictedRetentionTimes[i] > this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1] / 60)
				{
					this.m_bSkippedStandards[i] = true;
				}
				
				// Check if this peak is predicted to be before the beginning of the run
				if (this.m_dPredictedRetentionTimes[i] < this.m_dRetentionTimes[0] / 60)
				{
					this.m_bSkippedStandards[i] = true;
				}
			}
		}
		else
		{
	    	// Calculate approximate retention times of the standards
	    	predictRetentionTimes(m_dtstep);
	    	
	    	// Now find all peaks
	    	findPeaks();
	    	
	    	// Now calculate peak ranks
	    	calculatePeakRanks();
	    	
	    	for (int i = 0; i < m_peaks.size(); i++)
			{
	    		if (m_peaks.get(i).size() == 0)
	    		{
	    			this.m_bSkippedStandards[i] = true;
	    			this.m_iSelectedPeakRank[i] = -1;
	    			continue;
	    		}

				this.m_iSelectedPeakRank[i] = 0;

				this.m_dSelectedRetentionTimes[i] = m_peaks.get(i).get(0)[0] / 60;
				
				// Check if this peak is predicted to be past the end of the run
				if (this.m_dPredictedRetentionTimes[i] > this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1] / 60)
				{
					this.m_bSkippedStandards[i] = true;
				}

				// Check if this peak is predicted to be before the beginning of the run
				if (this.m_dPredictedRetentionTimes[i] < this.m_dRetentionTimes[0] / 60)
				{
					this.m_bSkippedStandards[i] = true;
				}
			}
		}

    	
    	// Return things the way they were
    	//m_dHoldUpArray = dStoreHoldUpArray;
    	//this.m_InterpolatedHoldUp = new InterpolationFunction(m_dHoldUpArray);
    	//predictRetentionTimes();

    	
    	
    	
		// Now we have a list of all peaks for each standard, sorted by their individual scores
	
		/*
		// Try all possible permutations of peaks to find the most favorable set of 25
		// First, make a list of all permutations
		// Only those where retention time is increasing
		Vector<int[]> peakPermutations = new Vector<int[]>();
		
		
		// TODO: Take care of the situation that some peaks are missing. 
		// Right now that causes it to fail to find any solutions.
		permutePeaks(null, m_peaks.size(), peakPermutations);

		// Score each permutation
		double dScores[] = new double[peakPermutations.size()];
		for (int i = 0; i < peakPermutations.size(); i++)
		{
			dScores[i] = scorePermutation(peakPermutations.get(i));
		}
		
		// Find highest score
		int iMax = 0;
		for (int i = 0; i < dScores.length; i++)
		{
			if (dScores[i] > dScores[iMax])
				iMax = i;
		}
		
		for (int i = 0; i < m_peaks.size(); i++)
		{
			if (peakPermutations.size() == 0)
				this.m_iSelectedPeakRank[i] = 0;
			else
				this.m_iSelectedPeakRank[i] = peakPermutations.get(iMax)[i];
			this.m_dSelectedRetentionTimes[i] = m_peaks.get(i).get(m_iSelectedPeakRank[i])[0] / 60;
		}
		*/    	
    	// Now 
		updateStandardsTable();

		// Select the first standard
		contentPane.jTableStandards.getSelectionModel().setSelectionInterval(0, 0);
	}
	
	void calculateHoldUpTimeProfile()
	{
		// Calculate hold-up time profile
		double dInnerDiameter = 0.025; // in cm
		m_V0 = (Math.PI * Math.pow(dInnerDiameter / 2, 2) * (this.m_dColumnLength * 100)) / 1000; // gives the volume in the column (in L)

		int iNumPoints = 10;
		double dLowTemp = 0;
		double dHighTemp = 500;
		
		m_dHoldUpArray = new double[iNumPoints][2];
		
		for (int i = 0; i < iNumPoints; i++)
		{
			m_dHoldUpArray[i][0] = ((dHighTemp - dLowTemp) * ((double)i / (double)(iNumPoints - 1))) + dLowTemp;
			double dGasViscosity = 18.69 * Math.pow(10, -6) * Math.pow((m_dHoldUpArray[i][0] + 273.15) / 273.15, 0.6958 + -0.0071 * (((m_dHoldUpArray[i][0] + 273.15) - 273.15) / 273.15));
			double dOmega = this.m_dColumnLength * dGasViscosity * (32.0 / Math.pow(dInnerDiameter / 100, 2));
			double dDeadTime = 0;
			if (m_bConstantFlowMode)
			{	// Constant flow rate mode
				double dFirstTerm = (Math.pow(Math.PI, 2) * Math.pow(dInnerDiameter / 10, 4) * Math.pow(this.m_dOutletPressure * 1000.0, 3)) / (48 * 32 * dGasViscosity * Math.pow(101325, 2) * Math.pow(((m_dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 10.0)), 2)); 
				double dSecondTerm = Math.pow(1.0 + ((8.0 * 32.0 * this.m_dColumnLength * dGasViscosity * 101325.0 * (((m_dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 100.0))))/(Math.PI * Math.pow(dInnerDiameter / 10, 3) * Math.pow(this.m_dOutletPressure * 1000.0, 2))), 3.0/2.0) - 1.0;
				dDeadTime = dFirstTerm * dSecondTerm;
			}
			else
			{	// Constant pressure mode
				dDeadTime = (4 * dOmega * this.m_dColumnLength * (Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 3) - Math.pow(this.m_dOutletPressure * 1000.0, 3))) / (3 * Math.pow(Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 2) - Math.pow(this.m_dOutletPressure * 1000.0, 2), 2));				
			}

			m_dHoldUpArray[i][1] = dDeadTime; // in seconds
		}
    	
    	this.m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
	}
	
	void findPeaks()
	{
		this.m_dMaxIntensities = new double[m_dStandardCompoundsMZArray.length];
		
		for (int iStandard = 0; iStandard < m_dStandardCompoundsMZArray.length; iStandard++)
		{
			double dSelectedMasses[] = m_dStandardCompoundsMZArray[iStandard];
    	
			// Create new data array for the EIC
			m_rawEIC = new double[m_iSpectraCount];
			double m_EICderivative[] = new double[m_iSpectraCount];
			double m_EICSecondDerivative[] = new double[m_iSpectraCount];
			double intensity;
        
			// This for loop is the part that is slow when you click on a standard.
			// This is where we create the raw EIC.
			// If the standard has more than one mass listed, the EIC is the sum of both masses
			for (int iSpectrum = 0; iSpectrum < this.m_iSpectraCount; iSpectrum++)
			{
				// process all peaks by iterating over the m/z values
				intensity = 0;
          
				for (int i = 0; i < m_MzData[iSpectrum].length; i++) 
				{
					for (int j = 0; j < dSelectedMasses.length; j++)
					{
						if (m_MzData[iSpectrum][i][0] >= dSelectedMasses[j] - 0.5 && m_MzData[iSpectrum][i][0] <= dSelectedMasses[j] + 0.5)
						{
							intensity += m_MzData[iSpectrum][i][1];
						}
					}
				}

				m_rawEIC[iSpectrum] = intensity;
			}
        
			// Determine number of points to filter
			// # of points per second
			double dTotalTime = this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1] - this.m_dRetentionTimes[0];
			double dPointsPerSecond = this.m_iSpectraCount / dTotalTime;
			double dPeakWidthInSeconds = this.m_dPredictedPeakWidths[iStandard] * 60;
			double dK = 0.5;
			int dNumPointsInFilter = (int)(dK * dPeakWidthInSeconds * dPointsPerSecond);

			if (dNumPointsInFilter < 2)
				dNumPointsInFilter = 2;
        
			int iOrder = 3;
			if (dNumPointsInFilter == 2)
				iOrder = 2;
			
			// Run Savitzky-Golay filter
			m_sgfilter = new SGFilter(dNumPointsInFilter, dNumPointsInFilter);
			m_sgcoefficients = SGFilter.computeSGCoefficients(dNumPointsInFilter, dNumPointsInFilter, iOrder);
			m_smoothEIC = m_sgfilter.smooth(m_rawEIC, m_sgcoefficients);
			for (int i = 0; i < 10; i++)
			{
				m_smoothEIC = m_sgfilter.smooth(m_smoothEIC, m_sgcoefficients);
			}
		
			// Calculate the derivative of the EIC
			m_EICderivative[0] = 0;
			m_EICderivative[m_rawEIC.length - 1] = 0;
        
	        for (int i = 1; i < m_rawEIC.length - 1; i++)
	        {
	        	double dYBefore = (m_smoothEIC[i - 1] + m_smoothEIC[i]) / 2;
	        	double dYAfter = (m_smoothEIC[i + 1] + m_smoothEIC[i]) / 2;
	        	double dXBefore = (m_dRetentionTimes[i - 1] + m_dRetentionTimes[i]) / 2;
	        	double dXAfter = (m_dRetentionTimes[i + 1] + m_dRetentionTimes[i]) / 2;
	        	
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
	        	double dXBefore = (m_dRetentionTimes[i - 1] + m_dRetentionTimes[i]) / 2;
	        	double dXAfter = (m_dRetentionTimes[i + 1] + m_dRetentionTimes[i]) / 2;
	        	
	        	double dSlope = (dYAfter - dYBefore) / (dXAfter - dXBefore);
	        	m_EICSecondDerivative[i] = dSlope;
	        }
        
	        // Smooth the second derivative
	        m_EICSecondDerivative = m_sgfilter.smooth(m_EICSecondDerivative, m_sgcoefficients);

	        // Find maximum intensity
	        m_dMaxIntensities[iStandard] = 0;
	        for (int i = 0; i < m_rawEIC.length; i++)
	        {
	        	if (m_smoothEIC[i] > m_dMaxIntensities[iStandard])
	        		m_dMaxIntensities[iStandard] = m_smoothEIC[i];
	        }
        
	        // Find the peaks (any place where the derivative crosses zero with a negative slope)        
	        Vector<double[]> thisCompoundPeaks = new Vector<double[]>();
	        
	        for (int i = 0; i < m_rawEIC.length - 1; i++)
	        {
	        	double dYBefore = m_EICderivative[i];
	        	double dYAfter = m_EICderivative[i + 1];
	        	if (dYBefore >= 0 && dYAfter < 0)
	        	{
	        		double dXBefore = m_dRetentionTimes[i];
	            	double dXAfter = m_dRetentionTimes[i + 1];
	            	
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
	        				
	        				double dYBeforeDD = m_EICSecondDerivative[j - 1]; // Problem is here when i = 0
	        	        	double dYAfterDD = m_EICSecondDerivative[j];
	        	        	double dFractionDD = (dYBeforeDD - 0) / (dYBeforeDD - dYAfterDD);
	        	        	double dXBeforeDD = m_dRetentionTimes[j - 1];
	                    	double dXAfterDD = m_dRetentionTimes[j];
	        	        	dRightSideTime = (dFractionDD * (dXAfterDD - dXBeforeDD)) + dXBeforeDD;
	        	        	break;
	        			}
	        		}
	        		
	        		// Measure time to left side of peak
	        		double dLeftSideTime = m_dRetentionTimes[0];
	        		for (int j = i; j > 0; j--)
	        		{
	        			if (m_EICSecondDerivative[j] > 0)
	        			{
	        				double dYBeforeDD = m_EICSecondDerivative[j];
	        	        	double dYAfterDD = m_EICSecondDerivative[j + 1];
	        	        	double dFractionDD = (dYBeforeDD - 0) / (dYBeforeDD - dYAfterDD);
	        	        	double dXBeforeDD = m_dRetentionTimes[j];
	                    	double dXAfterDD = m_dRetentionTimes[j + 1];
	        	        	dLeftSideTime = (dFractionDD * (dXAfterDD - dXBeforeDD)) + dXBeforeDD;
	        	        	break;
	        			}
	        		}
	        		
	        		double dPeakWidthHalfHeight = dRightSideTime - dLeftSideTime;
	        		
	        		// {peak time, peak intensity, peak width at half height}
	        		thisCompoundPeaks.add(new double[]{dPeakTime, dPeakIntensity, dPeakWidthHalfHeight, 0, 0});
	           	}
	        }

	        m_peaks.add(thisCompoundPeaks);
		}
        
	}
	
	void calculatePeakRanks()
	{
		for (int iStandard = 0; iStandard < m_dStandardCompoundsMZArray.length; iStandard++)
		{
			for (int iPeak = 0; iPeak < m_peaks.get(iStandard).size(); iPeak++)
			{
				double dPeakTime = m_peaks.get(iStandard).get(iPeak)[0];
				double dPeakIntensity = m_peaks.get(iStandard).get(iPeak)[1];
				double dPeakWidthHalfHeight = m_peaks.get(iStandard).get(iPeak)[2];
				
				// Calculate peak score.
	    		double TimeAt36Percent = m_dPredictedPeakWidths[iStandard] * (1 / 0.03); // in min
	    		// Gives a value from 1 to 0 for closeness to predicted retention time
	    		double dPeakScoreRetentionTime = Math.exp(-Math.abs((dPeakTime / 60) - this.m_dPredictedRetentionTimes[iStandard]) / TimeAt36Percent);
	    		double dPeakScoreHeight = dPeakIntensity / m_dMaxIntensities[iStandard];
	    		double ErrorAt36Percent = this.m_dPredictedPeakWidths[iStandard];
	    		double dPeakScoreWidth = Math.exp(-Math.abs((dPeakWidthHalfHeight / 60) - this.m_dPredictedPeakWidths[iStandard]) / ErrorAt36Percent);
	    		
	    		// Calculate the combined peak score
	    		double dPeakScore = Math.pow(dPeakScoreRetentionTime, 2) * Math.pow(dPeakScoreHeight, 1) * Math.pow(dPeakScoreWidth, 1);
	    		m_peaks.get(iStandard).get(iPeak)[3] = dPeakScore;
			}

	        // Sort the peaks in descending order based on their score
	        Collections.sort(m_peaks.get(iStandard), new CompareRank());
	
	        int iNumPeaksToShow = 20;
	        if (iNumPeaksToShow > m_peaks.get(iStandard).size())
	        {
	        	iNumPeaksToShow = m_peaks.get(iStandard).size();
	        }
        
	        double dSumOfScores = 0;
	        for (int i = 0; i < iNumPeaksToShow; i++)
	        {
	        	dSumOfScores += m_peaks.get(iStandard).get(i)[3];
	        }
	        
	        // Calculate percentage probability for each peak
	        for (int i = 0; i < m_peaks.get(iStandard).size(); i++)
	        {
	        	m_peaks.get(iStandard).get(i)[4] = (m_peaks.get(iStandard).get(i)[3] / dSumOfScores) * 100;
	        }
		}
	}
	
	public double scorePermutation(int[] permutation)
	{
		double dTotalDistanceError = 0;
		double dTotalIntensityDifference = 0;
		double dTotalPeakScores = 0;
		int iPeakCount = 0;

		for (int i = 1; i < permutation.length; i++)
		{
			if (this.m_bSkippedStandards[i])
				continue;
			
			// Check if this peak is predicted to be past the end of the run
			if (this.m_dPredictedRetentionTimes[i] > this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1] / 60)
				continue;
			
			// Check if this peak is predicted to be earlier than the beginning of the run
			if (this.m_dPredictedRetentionTimes[i] < this.m_dRetentionTimes[0] / 60)
				continue;
			
			iPeakCount++;
			
			// Score by scores of individual peaks selected
			if (permutation[i] < 0)
				continue;

			double dPeakScore = m_peaks.get(i).get(permutation[i])[3];
			dTotalPeakScores += dPeakScore;
			
			if (permutation[i] < 0 || permutation[i - 1] < 0)
				continue;
			
			// Score by distance between peaks
			// Should be very similar to the spacing predicted

			double dActualDistance = (m_peaks.get(i).get(permutation[i])[0] - m_peaks.get(i - 1).get(permutation[i - 1])[0]) / 60;
			double dPredictedDistance = this.m_dPredictedRetentionTimes[i] - this.m_dPredictedRetentionTimes[i - 1];
			double dDistanceErrorPercent = Math.abs(dActualDistance - dPredictedDistance) / dPredictedDistance;
			dTotalDistanceError += dDistanceErrorPercent;

			// Score by intensity
			// Should be close to the nearby intensities

			double dIntensityDiff = Math.abs(m_peaks.get(i).get(permutation[i])[1] - m_peaks.get(i - 1).get(permutation[i - 1])[1]);
			double dIntensityPercentDiff = dIntensityDiff / Math.max(m_peaks.get(i).get(permutation[i])[1], m_peaks.get(i - 1).get(permutation[i - 1])[1]);
			dTotalIntensityDifference += dIntensityPercentDiff;
		}
		
		dTotalPeakScores /= iPeakCount;
		dTotalDistanceError /= iPeakCount;
		dTotalIntensityDifference /= iPeakCount;
		
		double dDenominator = (dTotalDistanceError * dTotalIntensityDifference);
		if (dDenominator == 0)
			return dTotalPeakScores;
		else
			return Math.pow(dTotalPeakScores, 1) / (dTotalDistanceError * dTotalIntensityDifference);
	}
	
	public void permutePeaks(int[] beginningPeaks, int iDepth, Vector<int[]> peakPermutations) 
	{
		if (beginningPeaks == null)
		{
			beginningPeaks = new int[0];
		}
		
		int iCurrentDepth = beginningPeaks.length;
		
	    if (iCurrentDepth >= iDepth)
	    {
	    	peakPermutations.add(beginningPeaks);
	    }
	    else
	    {
	    	// Run through first 3 peaks for this standard
	    	for (int i = 0; i < 3; i++)
	    	{
    			// Must have a retention time greater than the last peak
	    		if (iCurrentDepth < 1 || m_peaks.get(iCurrentDepth).get(i)[0] > m_peaks.get(iCurrentDepth - 1).get(beginningPeaks[iCurrentDepth - 1])[0])
	    		{
	    			double dPercentRetentionDifference = 100;
	    			if (iCurrentDepth > 0)
	    			{
	    				double dPredictedRetentionDifference = this.m_dPredictedRetentionTimes[iCurrentDepth] - this.m_dPredictedRetentionTimes[iCurrentDepth - 1];
	    				double dActualRetentionDifference = (m_peaks.get(iCurrentDepth).get(i)[0] - m_peaks.get(iCurrentDepth - 1).get(beginningPeaks[iCurrentDepth - 1])[0]) / 60;
	    				dPercentRetentionDifference = Math.abs(dActualRetentionDifference - dPredictedRetentionDifference) / dPredictedRetentionDifference;
	    			}
	    			
	    			// Must have a retention time difference within 20% of the predicted
		    		if (iCurrentDepth < 1 || dPercentRetentionDifference < 0.2)
		    		{
			    		int[] newPeaks = new int[beginningPeaks.length + 1];
			    		for (int j = 0; j < beginningPeaks.length; j++)
			    		{
			    			newPeaks[j] = beginningPeaks[j];
			    		}
			    		newPeaks[newPeaks.length - 1] = i;
	
			    		permutePeaks(newPeaks, iDepth, peakPermutations);
		    		}
	    		}
	    	}
	    }
	    
	    return;// peakPermutations;
	}
	
	public void setTemperatureProgram(double[][] dTemperatureProgram)
	{
		this.m_dIdealTemperatureProfileArray = dTemperatureProgram;
	}
	
	public void setFlowRate(double dFlowRate)
	{
		this.m_dFlowRate = dFlowRate;
	}
	
	public void setInletPressure(double dInletPressure)
	{
		this.m_dInletPressure = dInletPressure;
	}

	public void setColumnLength(double dColumnLength)
	{
		this.m_dColumnLength = dColumnLength;
	}
	
	public void setInnerDiameter(double dInnerDiameter)
	{
		this.m_dInnerDiameter = dInnerDiameter;
	}

	public void setFilmThickness(double dFilmThickness)
	{
		this.m_dFilmThickness = dFilmThickness;
	}

	public void setInitialTime(double dInitialTime)
	{
		this.m_dInitialTime = dInitialTime;
	}

	public void setInitialTemperature(double dInitialTemperature)
	{
		this.m_dInitialTemperature = dInitialTemperature;
	}
	
	public void setOutletPressure(double dOutletPressure)
	{
		this.m_dOutletPressure = dOutletPressure;
	}
	
	public void setConstantFlowMode(boolean bConstantFlowMode)
	{
		this.m_bConstantFlowMode = bConstantFlowMode;
	}
    
	public void setMzData(double[][][] mzData)
	{
		this.m_MzData = mzData;
		this.m_iSpectraCount = m_MzData.length;
	}
	
	public void setRetentionTimes(double[] dRetentionTimes)
	{
		this.m_dRetentionTimes = dRetentionTimes;
	}
	
	public void setTemperatureProfile(LinearInterpolationFunction TemperatureProfile)
	{
		this.m_InterpolatedTemperatureProfile = TemperatureProfile;
	}

	public void setHoldUpTimeProfile(InterpolationFunction HoldUpTimeProfile)
	{
		this.m_InterpolatedHoldUpProfile = HoldUpTimeProfile;
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

		double dSelectedMasses[] = m_dStandardCompoundsMZArray[iIndexOfStandard];

    	if (iIndexOfStandard == contentPane.m_tabModelStandards.getRowCount() - 1)
    	{
    		contentPane.jbtnNext.setText("Finished");
    		contentPane.jbtnNext.setIcon(null);
    	}
    	else
    	{
	    	contentPane.jbtnNext.setText("Next Standard  ");
			contentPane.jbtnNext.setIcon(new ImageIcon(getClass().getResource("/boswell/peakfindergc/images/forward.png")));
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
    	contentPane.jPanelPeakPick.setBorder(BorderFactory.createTitledBorder(null, "Choose the correct peak for " + this.m_strStandardCompoundsNameArray[iIndexOfStandard], TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
        
    	// Create new data array for the EIC
        m_rawEIC = new double[m_iSpectraCount];
        double m_EICderivative[] = new double[m_iSpectraCount];
        double m_EICSecondDerivative[] = new double[m_iSpectraCount];
        double intensity;
        
        // This for loop is the part that is slow when you click on a standard.
        for (int iSpectrum = 0; iSpectrum < this.m_iSpectraCount; iSpectrum++)
		{
			// process all peaks by iterating over the m/z values
			intensity = 0;
      
			for (int i = 0; i < m_MzData[iSpectrum].length; i++) 
			{
				for (int j = 0; j < dSelectedMasses.length; j++)
				{
					if (m_MzData[iSpectrum][i][0] >= dSelectedMasses[j] - 0.5 && m_MzData[iSpectrum][i][0] <= dSelectedMasses[j] + 0.5)
					{
						intensity += m_MzData[iSpectrum][i][1];
					}
				}
			}

			m_rawEIC[iSpectrum] = intensity;
		}
        
        // Determine number of points to filter
        // # of points per second
        double dTotalTime = this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1] - this.m_dRetentionTimes[0];
        double dPointsPerSecond = this.m_iSpectraCount / dTotalTime;
        double dPeakWidthInSeconds = this.m_dPredictedPeakWidths[iIndexOfStandard] * 60;
        double dK = 0.5;
        int dNumPointsInFilter = (int)(dK * dPeakWidthInSeconds * dPointsPerSecond);
        if (dNumPointsInFilter < 2)
        	dNumPointsInFilter = 2;
        
		int iOrder = 3;
		if (dNumPointsInFilter == 2)
			iOrder = 2;

		// Run Savitzky-Golay filter
		m_sgfilter = new SGFilter(dNumPointsInFilter, dNumPointsInFilter);
		m_sgcoefficients = SGFilter.computeSGCoefficients(dNumPointsInFilter, dNumPointsInFilter, iOrder);
		m_smoothEIC = m_sgfilter.smooth(m_rawEIC, m_sgcoefficients);
		for (int i = 0; i < 10; i++)
		{
			m_smoothEIC = m_sgfilter.smooth(m_smoothEIC, m_sgcoefficients);
		}
		
		contentPane.m_FullEICGraphControl.RemoveAllSeries();
        int iExtractedIonRawPlotIndex = contentPane.m_FullEICGraphControl.AddSeries("Extracted ion chromatogram", new Color(0, 0, 0), 1, false, false);
        
        // Set the expected retention time and peak width labels
        contentPane.jlblExpectedRetentionTime.setText(formatter2.format(this.m_dPredictedRetentionTimes[iIndexOfStandard]) + " min");
        contentPane.jlblExpectedPeakWidth.setText(formatter2.format(this.m_dPredictedPeakWidths[iIndexOfStandard]) + " min");
        
        // Clear the peak rank table
        contentPane.m_tabModelPeakRank.setRowCount(0);
        
        int iNumPeaksToShow = m_peaks.get(iIndexOfStandard).size();
        
        // Fill the peak rank table
        for (int i = 0; i < iNumPeaksToShow; i++)
        {
        	if (m_peaks.size() - 1 < i)
        		break;
        	
        	double dProbability = m_peaks.get(iIndexOfStandard).get(i)[4];
        	String strRank = ((Integer)(i + 1)).toString() + " (" + formatter.format(dProbability) + "%)";
        	Object[] newRow = new Object[]{
        			strRank, 
        			formatter2.format(m_peaks.get(iIndexOfStandard).get(i)[0] / 60), 
        			formatter2.format(m_peaks.get(iIndexOfStandard).get(i)[2] / 60), 
        			formatter3.format(m_peaks.get(iIndexOfStandard).get(i)[1])};
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
        	contentPane.m_FullEICGraphControl.AddDataPoint(iExtractedIonRawPlotIndex, m_dRetentionTimes[i], m_rawEIC[i]);
        }

		contentPane.m_FullEICGraphControl.AutoScaleX();
		contentPane.m_FullEICGraphControl.AutoScaleY();
		
		contentPane.m_FullEICGraphControl.repaint();
    }
	
    public void predictRetentionTimes(double dtstepToUse)
    {
    	double dTimeLimit = Math.max(this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1] / 60, this.m_dIdealTemperatureProfileArray[this.m_dIdealTemperatureProfileArray.length - 1][0]) * 3.0;
		double dtstep;
		if (dtstepToUse == 0)
			dtstep = dTimeLimit * 0.001;
		else
			dtstep = dtstepToUse;

		m_dPredictedRetentionTimes = new double[this.m_dIsothermalParamArray.length];
		m_dPredictedPeakWidths = new double[this.m_dIsothermalParamArray.length];

    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
    	double dBeta2 = Math.pow((this.m_dInnerDiameter / 2) - (this.m_dFilmThickness / 1000), 2) / (Math.pow(this.m_dInnerDiameter / 2, 2) - Math.pow((this.m_dInnerDiameter / 2) - (this.m_dFilmThickness / 1000), 2));
    	double dBeta1Beta2 = dBeta1 / dBeta2;

		int iNumCompounds = this.m_dIsothermalParamArray.length;
		for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
		{
			//InterpolationFunction IsothermalData = new InterpolationFunction(this.m_dIsothermalDataArray[iCompound]);

			double dtRFinal = 0;
			double dXPosition = 0;
			double dLastXPosition = 0;
			double dXMovement = 0;
			boolean bIsEluted = false;
			double dTcA = 0;
			double dTcB = 0;
			double dCurVal = 0;
			double kprime = 1;
			double dt0 = 1;
			
			dTcA = this.m_InterpolatedTemperatureProfile.getAt(0);
			
			for (double t = 0; t <= dTimeLimit; t += dtstep)
			{
				dTcB = m_InterpolatedTemperatureProfile.getAt(t + dtstep);

				double dTc = (dTcA + dTcB) / 2;
				dt0 = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
				
				kprime = calckfromT(this.m_dIsothermalParamArray[iCompound], dTc) * dBeta1Beta2;

				//kprime = Math.pow(10, IsothermalData.getAt(dTc)) * dBeta1Beta2;
				dCurVal = dtstep / (1 + kprime);

				dXMovement = dCurVal / dt0;
				
				dLastXPosition = dXPosition;
				dXPosition += dXMovement;
				
				if (dXPosition >= 1)
				{
					dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * dtstep) + t;
					bIsEluted = true;
					break;
				}
				
				dTcA = dTcB;
			}
			
			if (bIsEluted)
			{
		    	double dSigma = Math.sqrt(Math.pow((dt0 * (1 + kprime)) / Math.sqrt(m_dTheoreticalPlates), 2));
		    	m_dPredictedPeakWidths[iCompound] = dSigma * 2.355; // Peak width at half height
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
    
	/**
	 * Returns the value of k for a given set of compound parameters and temperature
	 *
	 * @param  compoundParams dH(T0), dS(T0), and dC, where T0 is 273.15 K
	 * @param  T the temperature in deg C
	 * @return      the retention factor (k) at temperature T
	 */
    public double calckfromT(double[] compoundParams, double T)
    {
    	double dH = compoundParams[0];
    	double dS = compoundParams[1];
    	double dC = compoundParams[2];
    	
    	double A = (dS - dC * Math.log(273.15) - dC) / 8.3145;
    	double B = -(dH - dC * 273.15) / 8.3145;
    	double C = dC / 8.3145;
    	
    	double k = Math.pow(Math.E, A + B * (1 / (T + 273.15)) + C * Math.log(T + 273.15));
    	return k;
    }
    
    public void selectNewPeak(int iSelectedPeak)
    {
		NumberFormat formatter = new DecimalFormat("#0.000");
    	int iSelectedStandard = contentPane.jTableStandards.getSelectedRow();
    	
    	// Determine range of data to plot in smaller graph
    	double dRetentionTime = m_peaks.get(iSelectedStandard).get(iSelectedPeak)[0];
    	double dExpectedPeakWidth = this.m_dPredictedPeakWidths[iSelectedStandard] * 60;
    	double dLeftWindow = dRetentionTime - (dExpectedPeakWidth * 6);
    	double dRightWindow = dRetentionTime + (dExpectedPeakWidth * 6);
    	if (dLeftWindow < 0)
    		dLeftWindow = 0;
    	if (dRightWindow > this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1])
    	{
    		dRightWindow = this.m_dRetentionTimes[this.m_dRetentionTimes.length - 1];
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
        	if (this.m_dRetentionTimes[i] >= dLeftWindow && this.m_dRetentionTimes[i] <= dRightWindow)
        	{
        		contentPane.m_PeakEICGraphControl.AddDataPoint(iExtractedIonRawPlotIndex, m_dRetentionTimes[i], m_rawEIC[i]);
        		contentPane.m_PeakEICGraphControl.AddDataPoint(iExtractedIonSmoothPlotIndex, m_dRetentionTimes[i], m_smoothEIC[i]);
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
    			contentPane.m_tabModelStandards.setValueAt(((Integer)(m_iSelectedPeakRank[i] + 1)).toString(), i, 2);
    			contentPane.m_tabModelStandards.setValueAt(formatter.format(m_dSelectedRetentionTimes[i]), i, 3);
    		}
    	}
		
		// TODO: Scoring should account for missing standards (-1 rank)
		double dScore = scorePermutation(this.m_iSelectedPeakRank);
		contentPane.jlblOverallFitScore.setText(formatter.format(dScore));
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
