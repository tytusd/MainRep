package org.retentionprediction.gcdatabasebuilderfx;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.retentionprediction.gcdatabasebuilderfx.StepFourPaneController.StepFourPaneControllerListener;
import org.retentionprediction.peakfindergcfx.PeakFinderGCFX;

import boswell.fxoptionpane.FXOptionPane;
import boswell.graphcontrolfx.GraphControlFX;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

class PredictedRetentionObject
{
	double dPredictedRetentionTime = 0;
	double dPredictedErrorSigma = 0;
}

public class BackcalculateController implements Initializable, StepFourPaneControllerListener
{
	private Window parentWindow;
	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();

	private TitledPane step3Pane;
	private StepThreePaneController stepThreePaneController;
	private StepFourPaneController stepFourPaneController;
	private TitledPane step4Pane;
	@FXML AnchorPane anchorPaneTemperatureProfile;
	@FXML AnchorPane anchorPaneHoldUpProfile;
	
	private GraphControlFX temperatureProfileGraph;
	private GraphControlFX holdUpProfileGraph;
	
	@FXML private GridPane mainGrid;
	@FXML private Button buttonNextStep;
	@FXML private Button buttonPreviousStep;
	
	private double filmThickness = 0.25;
	private double innerDiameter = 0.25;
	private double flowRate = 1;
	private double columnLength = 30;
	private double inletPressure = 151325;
	private double outletPressure = .000001;
	private boolean constantFlowRateMode = false;
	private boolean isUnderVacuum = true;
	private String fileName = "";
	private double initialTemperature = 60;
	private double initialHoldTime = 5;
	private double[][] temperatureProgramInConventionalForm = {{26, 320, 15}};
	
    private BackCalculateTask task = null;
    public double tStep = 0.01;
    
    public double m_dTmax = 1000;
    public double m_dk = 100;
    
    public double[][] m_dIdealTemperatureProfileArray;
    public LinearInterpolationFunction m_InterpolatedIdealTempProfile; //Linear
    
    public double[][] m_dTemperatureProfileDifferenceArray;
    public LinearInterpolationFunction m_InterpolatedTemperatureDifferenceProfile; // Linear
    
    public double[][] m_dSimpleTemperatureProfileArray;
    public LinearInterpolationFunction m_InterpolatedSimpleTemperatureProfile; // Linear
    
    public double[][] m_dHoldUpArray;
    public InterpolationFunction m_InitialInterpolatedHoldUpVsTempProfile;

    public InterpolationFunction m_InterpolatedHoldUpProfile;
    public InterpolationFunction m_Vm;
	
    public int m_iInterpolatedTempProgramSeries = -1;
    public int m_iTempProgramMarkerSeries = -1;
    public int m_iInterpolatedHoldUpSeries = -1;
    public int m_iHoldUpMarkerSeries = -1;
    public int m_iIdealPlotIndexTemp = -1;
    public int m_iIdealPlotIndexHoldUp = -1;
    
    public double plotXMax = 0;
    
    public final double m_dGoldenRatio = (1 + Math.sqrt(5)) / 2;

    public double m_dVariance = 0;
    
    public double[] m_dExpectedErrorArray;
    
    public final int INCOMPLETE = 0;
    public final int PASSED = 1;
    public final int PASSEDBUTQUESTIONABLE = 2;
    public final int FAILED = 3;
    
    private int status = 0;
    private double score = 0;
	
	// To be acquired from the Step3Pane.
	private ObservableList<StandardCompound> standardsList = FXCollections.observableArrayList();
	
    private BackCalculateControllerListener backCalculateControllerListener;

	public interface BackCalculateControllerListener 
	{
		public void onNextStepPressed(BackcalculateController thisController);
		public void onPreviousStepPressed(BackcalculateController thisController);
    }

	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		// Load the Step3Pane.fxml layout
		try
		{
			// Load the Step3Pane
			FXMLLoader fxmlLoaderStep3Pane = new FXMLLoader();
			step3Pane = (TitledPane)fxmlLoaderStep3Pane.load(getClass().getResource("Step3Pane.fxml").openStream());
			stepThreePaneController = fxmlLoaderStep3Pane.getController();
			
			stepThreePaneController.setStandardCompoundList(standardsList);
			
			// Add an event handler for when the back calculate button is pressed.
			stepThreePaneController.backCalculateOnMouseClickedProperty().set(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event) {
							onBackCalculateButtonMouseClicked(event);
						}
					});
			
			FXMLLoader fxmlLoaderStep4Pane = new FXMLLoader();
			step4Pane = (TitledPane)fxmlLoaderStep4Pane.load(getClass().getResource("Step4Pane.fxml").openStream());
			stepFourPaneController = fxmlLoaderStep4Pane.getController();

			// Add an event handler for when the "Find Retention Times Automatically..." button is pressed.
			stepFourPaneController.buttonFindRetentionTimesOnMouseClickedProperty().set(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event) {
							onFindRetentionTimesAutomaticallyButtonMouseClicked(event);
						}
					});

			stepFourPaneController.setStep4PaneControllerListener((StepFourPaneControllerListener)this);
			
			// Add the GraphControlFX's
			temperatureProfileGraph = new GraphControlFX();
			temperatureProfileGraph.setControlsEnabled(false);
			temperatureProfileGraph.setYAxisTitle("Column Temperature");
			temperatureProfileGraph.setYAxisBaseUnit("\u00B0C", "\u00B0C");
			//temperatureProfileGraph.setYAxisRangeLimits(0, 600);
			temperatureProfileGraph.setYAxisScientificNotation(true);
			temperatureProfileGraph.setYAxisRangeIndicatorsVisible(true);
			temperatureProfileGraph.setAutoScaleY(true);
	        
			temperatureProfileGraph.setXAxisType(true);
			temperatureProfileGraph.setXAxisRangeIndicatorsVisible(false);
			temperatureProfileGraph.setAutoScaleX(true);
			temperatureProfileGraph.setSelectionCursorVisible(false);

			anchorPaneTemperatureProfile.getChildren().add(temperatureProfileGraph);
			AnchorPane.setTopAnchor(temperatureProfileGraph, 0.0);
			AnchorPane.setBottomAnchor(temperatureProfileGraph, 0.0);
			AnchorPane.setLeftAnchor(temperatureProfileGraph, 0.0);
			AnchorPane.setRightAnchor(temperatureProfileGraph, 0.0);
			
			temperatureProfileGraph.widthProperty().bind(anchorPaneTemperatureProfile.widthProperty().subtract(rem));
			temperatureProfileGraph.heightProperty().bind(anchorPaneTemperatureProfile.heightProperty().subtract(rem));

			holdUpProfileGraph = new GraphControlFX();
			holdUpProfileGraph.setControlsEnabled(false);
			holdUpProfileGraph.setYAxisTitle("Hold-Up Time");
			holdUpProfileGraph.setYAxisBaseUnit("seconds", "s");
			//holdUpProfileGraph.setYAxisRangeLimits(-1E15d, 1E15d);
			holdUpProfileGraph.setYAxisScientificNotation(true);
			holdUpProfileGraph.setYAxisRangeIndicatorsVisible(false);
			holdUpProfileGraph.setAutoScaleY(true);
	        
			holdUpProfileGraph.setXAxisType(false);
			holdUpProfileGraph.setXAxisRangeIndicatorsVisible(false);
			holdUpProfileGraph.setXAxisTitle("Temperature");
			holdUpProfileGraph.setXAxisBaseUnit("\u00b0C", "\u00b0C");
			holdUpProfileGraph.setAutoScaleX(true);
			holdUpProfileGraph.setSelectionCursorVisible(false);
			
			anchorPaneHoldUpProfile.getChildren().add(holdUpProfileGraph);
			AnchorPane.setTopAnchor(holdUpProfileGraph, 0.0);
			AnchorPane.setBottomAnchor(holdUpProfileGraph, 0.0);
			AnchorPane.setLeftAnchor(holdUpProfileGraph, 0.0);
			AnchorPane.setRightAnchor(holdUpProfileGraph, 0.0);
			
			holdUpProfileGraph.widthProperty().bind(anchorPaneHoldUpProfile.widthProperty().subtract(rem));
			holdUpProfileGraph.heightProperty().bind(anchorPaneHoldUpProfile.heightProperty().subtract(rem));
			
			resetValues();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void resetValues()
	{
		stepThreePaneController.lastIterationVarianceLabelProperty().unbind();
		stepThreePaneController.lastIterationVarianceLabelProperty().set("");
		stepThreePaneController.setBackCalculationButtonDisable(false);
		stepThreePaneController.iterationLabelProperty().unbind();
		stepThreePaneController.iterationLabelProperty().set("");
		stepThreePaneController.percentImprovementLabelProperty().unbind();
		stepThreePaneController.percentImprovementLabelProperty().set("");
		stepThreePaneController.progressBarProperty().unbind();
		stepThreePaneController.progressBarProperty().set(0);
		stepThreePaneController.timeElapsedLabelProperty().unbind();
		stepThreePaneController.timeElapsedLabelProperty().set("");
		stepThreePaneController.varianceLabelProperty().unbind();
		stepThreePaneController.varianceLabelProperty().set("");
		stepThreePaneController.statusLabelProperty().unbind();
		stepThreePaneController.statusLabelProperty().set("Click \"Back-calculate profiles\" to begin the optimization");
		
		/*if (standardsList != null)
		{
			for (int i = 0; i < standardsList.size(); i++)
			{
				standardsList.get(i).setPredictedRetentionTime(0);
			}
		}*/
		
		stepThreePaneController.setBackCalculationButtonDisable(false);
		buttonNextStep.setDisable(true);
		
		temperatureProfileGraph.RemoveAllSeries();
		holdUpProfileGraph.RemoveAllSeries();

		// Add the step3Pane to start
		mainGrid.getChildren().removeAll(step3Pane, step4Pane);
		mainGrid.add(step3Pane, 1, 0);
	}
	
	public void onBackCalculateButtonMouseClicked(MouseEvent event)
	{
		beginBackCalculation(true);
	}

	public double getTStep()
	{
		return this.tStep;
	}
	
	public InterpolationFunction getHoldUpTimeProfile()
	{
		return this.m_InterpolatedHoldUpProfile;
	}
	
	public LinearInterpolationFunction getTemperatureProfile()
	{
		double dMaxTime = this.m_dIdealTemperatureProfileArray[m_dIdealTemperatureProfileArray.length - 1][0] * 1.5;
		int iNumSteps = (int)(dMaxTime / this.tStep);
		double[][] dTempProfile = new double[iNumSteps][2];
		for (int i = 0; i < iNumSteps; i++)
		{
			double dTime = (double)i * tStep;
			dTempProfile[i][0] = dTime;
			dTempProfile[i][1] = m_InterpolatedSimpleTemperatureProfile.getAt(dTime) + m_InterpolatedTemperatureDifferenceProfile.getAt(dTime);
		}
		LinearInterpolationFunction tempProfile = new LinearInterpolationFunction(dTempProfile);
		return tempProfile;
	}
	
	public void onFindRetentionTimesAutomaticallyButtonMouseClicked(MouseEvent event)
	{
		PeakFinderGCFX peakFinderGC = new PeakFinderGCFX(parentWindow, Globals.StationaryPhaseArray, false);
		peakFinderGC.setFileName(fileName);
		peakFinderGC.setStandardCompoundMZData(Globals.TestCompoundMZArray);
		peakFinderGC.setStandardCompoundNames(Globals.TestCompoundNameArray);
		peakFinderGC.setIsothermalData(Globals.TestCompoundParamArray);
		peakFinderGC.setColumnLength(columnLength);
		peakFinderGC.setConstantFlowMode(constantFlowRateMode);
		peakFinderGC.setFilmThickness(filmThickness);
		peakFinderGC.setFlowRate(flowRate);
		peakFinderGC.setTemperatureProgramInConventionalForm(temperatureProgramInConventionalForm, initialTemperature, initialHoldTime);
		peakFinderGC.setInnerDiameter(innerDiameter);
		peakFinderGC.setInletPressure(inletPressure);
		peakFinderGC.setOutletPressure(outletPressure, isUnderVacuum);
    	double[][] dCombinedTempProfileArray = new double[m_dSimpleTemperatureProfileArray.length][2];
    	for (int i = 0; i < m_dSimpleTemperatureProfileArray.length; i++)
    	{
    		dCombinedTempProfileArray[i][0] = m_dSimpleTemperatureProfileArray[i][0];
    		dCombinedTempProfileArray[i][1] = m_dSimpleTemperatureProfileArray[i][1] + this.m_InterpolatedTemperatureDifferenceProfile.getAt(m_dSimpleTemperatureProfileArray[i][0]);
    	}
    	peakFinderGC.setTemperatureProfile(dCombinedTempProfileArray);
    	peakFinderGC.setHoldUpTimeProfile(m_dHoldUpArray);
    	// Set the tstep to the last one used in back-calculation
    	peakFinderGC.setTStep(tStep);
    	
		peakFinderGC.run();
		
		if (peakFinderGC.getOkPressed())
    	{
    		double[] dRetentionTimes = peakFinderGC.getSelectedRetentionTimes();
    		boolean[] bSkippedStandards = peakFinderGC.getSkippedStandards();
    		int[] iPeakRank = peakFinderGC.getSelectedPeakRank();

			ObservableList<StandardCompound> testCompoundList = stepFourPaneController.getTestCompoundList();

    		for (int i = 0; i < dRetentionTimes.length; i++)
    		{
    			// Mark whether the standard is skipped.
    			testCompoundList.get(i).setUse(!bSkippedStandards[i]);
    			
    			// Put in the correct retention time
    			if (iPeakRank[i] >= 0)
    			{
    				if (!bSkippedStandards[i])
    					testCompoundList.get(i).setMeasuredRetentionTime(dRetentionTimes[i]);
    				else
    					testCompoundList.get(i).setMeasuredRetentionTime(0);
    			}
    			else
    			{
    				// If the peak wasn't picked, then skip this one.
    				testCompoundList.get(i).setMeasuredRetentionTime(0.0);    				
    				testCompoundList.get(i).setUse(false);
    			}
    		}
    	}
		
		updateTestCompoundTable();
	}

	// Calculates the hold-up time profile given the column dimensions, film thickness, inlet pressure/flow rate, and outlet pressure
	
	private double[][] calculateInitialHoldUpTimeProfile()
	{
		// Calculate hold-up time profile
		double dInnerDiameter = (innerDiameter / 10) - (2 * filmThickness / 10000); // in cm

		int iNumPoints = 10;
		double dLowTemp = 0;
		double dHighTemp = 500;
		
		double[][] holdUpArray = new double[iNumPoints][2];
		
		for (int i = 0; i < iNumPoints; i++)
		{
			holdUpArray[i][0] = ((dHighTemp - dLowTemp) * ((double)i / (double)(iNumPoints - 1))) + dLowTemp;
			double dGasViscosity = 18.69 * Math.pow(10, -6) * Math.pow((holdUpArray[i][0] + 273.15) / 273.15, 0.6958 + -0.0071 * (((holdUpArray[i][0] + 273.15) - 273.15) / 273.15));
			double dOmega = columnLength * dGasViscosity * (32.0 / Math.pow(dInnerDiameter / 100, 2));
			double dDeadTime = 0;
			if (constantFlowRateMode)
			{	// Constant flow rate mode
				double dFirstTerm = (Math.pow(Math.PI, 2) * Math.pow(dInnerDiameter / 10, 4) * Math.pow(outletPressure, 3)) / (48 * 32 * dGasViscosity * Math.pow(101325, 2) * Math.pow(((holdUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((flowRate / (60.0 * 1000.0)) / (dInnerDiameter / 10.0)), 2)); 
				double dSecondTerm = Math.pow(1.0 + ((8.0 * 32.0 * columnLength * dGasViscosity * 101325.0 * (((holdUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((flowRate / (60.0 * 1000.0)) / (dInnerDiameter / 100.0))))/(Math.PI * Math.pow(dInnerDiameter / 10, 3) * Math.pow(outletPressure, 2))), 3.0/2.0) - 1.0;
				dDeadTime = dFirstTerm * dSecondTerm;
			}
			else
			{	// Constant pressure mode
				dDeadTime = (4 * dOmega * columnLength * (Math.pow(inletPressure, 3) - Math.pow(outletPressure, 3))) / (3 * Math.pow(Math.pow(inletPressure, 2) - Math.pow(outletPressure, 2), 2));				
			}

			holdUpArray[i][1] = dDeadTime; // in seconds
		}
    	return holdUpArray;
	}
	
	// in m
	public void setColumnLength(double columnLength)
	{
		this.columnLength = columnLength;
	}
	
	// in Pa
	public void setInletPressure(double inletPressure)
	{
		this.inletPressure = inletPressure;
	}
	
	public void setOutletPressure(double outletPressure)
	{
		this.outletPressure = outletPressure;
	}
	
	public void setConstantFlowRateMode(boolean constantFlowRateMode)
	{
		this.constantFlowRateMode = constantFlowRateMode;
	}
	
	public void setFlowRate(double flowRate)
	{
		this.flowRate = flowRate;
	}
	
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	
	public double calcPlotXMaximum()
	{
		double plotXMax = (((Double)this.standardsList.get(this.standardsList.size() - 1).getMeasuredRetentionTime()));

	    // Extend the end of the temperature program if it is too short.
	    double finalTime = m_dIdealTemperatureProfileArray[m_dIdealTemperatureProfileArray.length - 1][0];
    	if (finalTime > plotXMax)
    		plotXMax = finalTime + 1;
    	
    	return plotXMax;
	}

	// The standardsList coming in will have all compounds, including those that are skipped
	// Here we need to eliminate those standards that we don't want in the table - that aren't used.
	public void setStandardsList(ObservableList<StandardCompound> standardsList)
	{
		this.standardsList.clear();
		for (int i = 0; i < standardsList.size(); i++)
		{
			if (standardsList.get(i).getUse())
				this.standardsList.add(standardsList.get(i));
		}
		
    	m_iInterpolatedTempProgramSeries = temperatureProfileGraph.AddSeries("Interpolated Temperature Program", Color.rgb(255,0,0), 1, false, false);
	    m_iTempProgramMarkerSeries = temperatureProfileGraph.AddSeries("Temp Program Markers", Color.rgb(255,0,0), 1, true, false);
	    
    	m_iInterpolatedHoldUpSeries = holdUpProfileGraph.AddSeries("Interpolated Hold-up", Color.rgb(255,0,0), 1, false, false);
	    m_iHoldUpMarkerSeries = holdUpProfileGraph.AddSeries("Hold-up Markers", Color.rgb(255,0,0), 1, true, false);
	    
    	this.m_InitialInterpolatedHoldUpVsTempProfile = new InterpolationFunction(this.calculateInitialHoldUpTimeProfile());
	    
	    plotXMax = calcPlotXMaximum();

		// Here is where we set the value of m_dtstep
    	tStep = plotXMax * 0.001;
    	
	    // Extend the end of the temperature program if it is too short.
   		this.m_dIdealTemperatureProfileArray[m_dIdealTemperatureProfileArray.length - 1][0] = plotXMax;
    	
		// Make the interpolated temperature profile
    	this.m_InterpolatedIdealTempProfile = new LinearInterpolationFunction(this.m_dIdealTemperatureProfileArray);

    	// Select number of data points for the gradient and flow profiles
		int iTotalDataPoints = standardsList.size();
		
		// 11/15ths of the data points should be on the gradient profile
		int iNumTempProgramDataPoints = (int)(((double)11/(double)15)*(double)iTotalDataPoints);
		int iNumFlowDataPoints = iTotalDataPoints - iNumTempProgramDataPoints;
		
		if (iNumFlowDataPoints < 3)
		{
			iNumFlowDataPoints = 3;
			iNumTempProgramDataPoints = iTotalDataPoints - iNumFlowDataPoints;
		}
		
		// Create initial gradient and flow rate arrays
		
		// First make an array with the correct number of data points.
		m_dTemperatureProfileDifferenceArray = new double [iNumTempProgramDataPoints][2];
		m_dHoldUpArray = new double [iNumFlowDataPoints][2];
		
		// Set the value of the first data point
		m_dTemperatureProfileDifferenceArray[0][0] = 0;
		m_dTemperatureProfileDifferenceArray[0][1] = 0;
		
		for (int i = 1; i < iNumTempProgramDataPoints - 1; i++)
		{
			// Find the two nearest alkanes
			double dAlkaneNum = ((double)i / ((double)iNumTempProgramDataPoints - 1)) * (double)iTotalDataPoints;
			double dOneGreater = (int)Math.ceil(dAlkaneNum);
			double dOneLesser = (int)Math.floor(dAlkaneNum);
			double dRtOneLesser = (Double)standardsList.get((int)dOneLesser).getMeasuredRetentionTime();
			double dRtOneGreater = (Double)standardsList.get((int)dOneGreater).getMeasuredRetentionTime();
		
			// Check to see if we landed exactly on an alkane
			if (dOneGreater == dOneLesser)
			{
				// If so, set the time of this point to the alkane we landed on
				m_dTemperatureProfileDifferenceArray[i][0] = dRtOneLesser;
			}
			else
			{
				// Otherwise, find the time of this point in between the surrounding alkanes
				double dPosition = ((dAlkaneNum - dOneLesser) / (dOneGreater - dOneLesser));
				m_dTemperatureProfileDifferenceArray[i][0] = (dPosition * dRtOneGreater) + ((1 - dPosition) * dRtOneLesser);
			}
			
			// Subtract half a hold-up time
			m_dTemperatureProfileDifferenceArray[i][0] = m_dTemperatureProfileDifferenceArray[i][0] - (0.5 * (this.m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_InterpolatedIdealTempProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0])) / 60));
			m_dTemperatureProfileDifferenceArray[i][1] = 0;
		}
		
		// Add the last data point at the position of the last-eluting compound
		m_dTemperatureProfileDifferenceArray[iNumTempProgramDataPoints - 1][0] = (Double)standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime();
		m_dTemperatureProfileDifferenceArray[iNumTempProgramDataPoints - 1][1] = 0;
		
		// Now adjust to get points at the corners
		int iPointIndex = 1;
		// Run through each corner in the ideal temperature profile array
		for (int i = 1; i < this.m_dIdealTemperatureProfileArray.length - 1; i++)
		{
			double dFirst = 0;
			double dNext = 0;
			
			// Find the first point after the corner (dNext) and the first point before the corner (dFirst)
			while (dNext < this.m_dIdealTemperatureProfileArray[i][0] && iPointIndex < m_dTemperatureProfileDifferenceArray.length - 1)
			{
				dFirst = m_dTemperatureProfileDifferenceArray[iPointIndex][0];
				dNext = m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0];
				
				iPointIndex++;
			}
			
			// Remove the last increment - now iPointIndex is the index of the point before this corner
			iPointIndex--;
			
			// Find the distances between the corner and the two points
			double dDistFirst = this.m_dIdealTemperatureProfileArray[i][0] - dFirst;
			double dDistNext = dNext - this.m_dIdealTemperatureProfileArray[i][0];
			
			// Find the distances between the two points and their next further point
			double dDistFirstBefore = m_dTemperatureProfileDifferenceArray[iPointIndex][0] - m_dTemperatureProfileDifferenceArray[iPointIndex - 1][0];
			double dDistNextAfter;
			if (iPointIndex + 2 < m_dTemperatureProfileDifferenceArray.length)
				dDistNextAfter = m_dTemperatureProfileDifferenceArray[iPointIndex + 2][0] - m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0];
			else
				dDistNextAfter = 0;
			
			// Score is based on how close the point is to the corner
			double dScoreFirst = dDistFirst + dDistFirstBefore;
			double dScoreNext = dDistNext + dDistNextAfter;
			
			// If the earlier point is sitting on a corner, then it should not be moved.
			if (isTemperatureProfilePointAtCorner(iPointIndex))
			{
				dScoreFirst = 1;
				dScoreNext = 0;
			}
			
			// Point with lower score moves to corner
			if (dScoreFirst < dScoreNext)
			{
				// Move the first point
				m_dTemperatureProfileDifferenceArray[iPointIndex][0] = this.m_dIdealTemperatureProfileArray[i][0];
				m_dTemperatureProfileDifferenceArray[iPointIndex][1] = 0;

				// Move the one before it right in between it and the last point
				if (iPointIndex >= 2 && !isTemperatureProfilePointAtCorner(iPointIndex - 1))
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex - 1][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex - 2][0] + m_dTemperatureProfileDifferenceArray[iPointIndex][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex - 1][1] = 0;
				}
				
				// Move the one after it in between it and the next point
				if (iPointIndex <= m_dTemperatureProfileDifferenceArray.length - 3)
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex][0] + m_dTemperatureProfileDifferenceArray[iPointIndex + 2][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex + 1][1] = 0;
				}
			}
			else
			{
				// Move the next point
				m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0] = this.m_dIdealTemperatureProfileArray[i][0];
				m_dTemperatureProfileDifferenceArray[iPointIndex + 1][1] = 0;

				// Move the one before it right in between it and the last point
				if (iPointIndex >= 1 && !isTemperatureProfilePointAtCorner(iPointIndex))
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex - 1][0] + m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex][1] = 0;
				}
				
				// Move the one after it in between it and the next point
				if (iPointIndex <= m_dTemperatureProfileDifferenceArray.length - 4)
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex + 2][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0] + m_dTemperatureProfileDifferenceArray[iPointIndex + 3][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex + 2][1] = 0;
				}
			}
		}

    	// Now for the flow rate vs. temp profile:
    	// First use m_InitialInterpolatedFlowRateVsTempProfile to figure out where to put the points
		double dMinTemp = this.m_dIdealTemperatureProfileArray[0][1];
		double dMaxTemp = this.m_dIdealTemperatureProfileArray[this.m_dIdealTemperatureProfileArray.length - 1][1];
		
	    for (int i = 0; i < iNumFlowDataPoints; i++)
		{
			m_dHoldUpArray[i][0] = ((dMaxTemp - dMinTemp) * ((double)i/((double)iNumFlowDataPoints - 1))) + dMinTemp;
			m_dHoldUpArray[i][1] = this.m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dHoldUpArray[i][0]);
		}
		
		m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
		m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);

		// Will come out to the same value as the ideal temperature program
		calcSimpleTemperatureProgram(100, 1000);

		updateGraphsWithIdealProfiles();
    	updateGraphs(this.m_InterpolatedSimpleTemperatureProfile, this.m_InterpolatedTemperatureDifferenceProfile, this.m_dTemperatureProfileDifferenceArray, this.m_InterpolatedHoldUpProfile, this.m_dHoldUpArray);
	}
	
	private void updateGraphsWithIdealProfiles()
	{
		temperatureProfileGraph.RemoveSeries(m_iIdealPlotIndexTemp);
		holdUpProfileGraph.RemoveSeries(m_iIdealPlotIndexHoldUp);

    	m_iIdealPlotIndexTemp = temperatureProfileGraph.AddSeries("Ideal Temperature Program", Color.rgb(0, 0, 0), 1, false, false);
    	m_iIdealPlotIndexHoldUp = holdUpProfileGraph.AddSeries("Ideal Hold-up Time", Color.rgb(0, 0, 0), 1, false, false);
    	
    	this.m_InitialInterpolatedHoldUpVsTempProfile = new InterpolationFunction(this.calculateInitialHoldUpTimeProfile());

		double dMinTemp = this.m_dIdealTemperatureProfileArray[0][1];
		double dMaxTemp = this.m_dIdealTemperatureProfileArray[this.m_dIdealTemperatureProfileArray.length - 1][1];
		
		// Plot the ideal temperature profile
    	for (int i = 0; i < m_dIdealTemperatureProfileArray.length; i++)
    	{
    		this.temperatureProfileGraph.AddDataPoint(m_iIdealPlotIndexTemp, m_dIdealTemperatureProfileArray[i][0] * 60, m_dIdealTemperatureProfileArray[i][1]);
    	}
    	
    	// Add the initial interpolated hold-up time profile to the graph control
	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = (((double)i / (double)(iNumPoints - 1)) * (dMaxTemp - dMinTemp)) + dMinTemp;
	    	holdUpProfileGraph.AddDataPoint(m_iIdealPlotIndexHoldUp, dXPos, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dXPos));
	    }
	}
	
	public void setFilmThickness(double filmThickness)
	{
		this.filmThickness = filmThickness;
	}
	
	public void setInnerDiameter(double innerDiameter)
	{
		this.innerDiameter = innerDiameter;
	}

	// Sets the temperature program as time, temperature pairs
	public void setTemperatureProgram(double[][] temperatureProgram)
	{
		this.m_dIdealTemperatureProfileArray = temperatureProgram;
	}
	
	public void setTemperatureProgramInConventionalForm(double initialTemperature, double initialHold, double[][] program)
	{
		this.initialHoldTime = initialHold;
		this.initialTemperature = initialTemperature;
		this.temperatureProgramInConventionalForm = program;
	}
	
	public void setUnderVacuum(boolean isUnderVacuum)
	{
		this.isUnderVacuum = isUnderVacuum;
	}

	public void setParentWindow(Window parentWindow)
	{
		this.parentWindow = parentWindow;
	}
	
    public boolean isTemperatureProfilePointAtCorner(int iPointIndex)
    {
    	double dPointTime = m_dTemperatureProfileDifferenceArray[iPointIndex][0];
    	
		for (int i = 0; i < this.m_dIdealTemperatureProfileArray.length - 1; i++)
    	{
    		if (dPointTime == this.m_dIdealTemperatureProfileArray[i][0])
    			return true;
    	}
		
		return false;
    }
/*
	public void performValidations()
	{
		// Calculate hold-up time vs. temperature profile
		
		// Subtract film thickness out of the inner diameter
		double dInnerDiameter = (this.m_dInnerDiameter / 10) - (2 * this.m_dFilmThickness / 10000); // in cm
		
		m_V0 = (Math.PI * Math.pow(dInnerDiameter / 2, 2) * (this.m_dColumnLength * 100)) / 1000; // gives the volume in the column (in L)

		int iNumPoints = 10;
		double dLowTemp = 0;
		double dHighTemp = 500;
		
		double[][] dHoldUpArray = new double[iNumPoints][2];
		
		for (int i = 0; i < iNumPoints; i++)
		{
			dHoldUpArray[i][0] = ((dHighTemp - dLowTemp) * ((double)i / (double)(iNumPoints - 1))) + dLowTemp;
			double dGasViscosity = 18.69 * Math.pow(10, -6) * Math.pow((dHoldUpArray[i][0] + 273.15) / 273.15, 0.6958 + -0.0071 * (((dHoldUpArray[i][0] + 273.15) - 273.15) / 273.15));
			double dOmega = this.m_dColumnLength * dGasViscosity * (32.0 / Math.pow(dInnerDiameter / 100, 2));
			double dDeadTime = 0;
			if (contentPane.jrdoConstantFlowRate.isSelected())
			{	// Constant flow rate mode
				double dFirstTerm = (Math.pow(Math.PI, 2) * Math.pow(dInnerDiameter / 10, 4) * Math.pow(this.m_dOutletPressure * 1000.0, 3)) / (48 * 32 * dGasViscosity * Math.pow(101325, 2) * Math.pow(((dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 10.0)), 2)); 
				double dSecondTerm = Math.pow(1.0 + ((8.0 * 32.0 * this.m_dColumnLength * dGasViscosity * 101325.0 * (((dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 100.0))))/(Math.PI * Math.pow(dInnerDiameter / 10, 3) * Math.pow(this.m_dOutletPressure * 1000.0, 2))), 3.0/2.0) - 1.0;
				dDeadTime = dFirstTerm * dSecondTerm;
			}
			else
			{	// Constant pressure mode
				dDeadTime = (4 * dOmega * this.m_dColumnLength * (Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 3) - Math.pow(this.m_dOutletPressure * 1000.0, 3))) / (3 * Math.pow(Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 2) - Math.pow(this.m_dOutletPressure * 1000.0, 2), 2));				
			}

			dHoldUpArray[i][1] = dDeadTime; // in seconds
		}
		
		this.m_InitialInterpolatedHoldUpVsTempProfile = new InterpolationFunction(dHoldUpArray);

		// Find maximum retention time
		double dMax = 0;
		for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
		{
			double dValue = (Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3);
			if (dValue > dMax)
				dMax = dValue;
		}

		// Calculate total time of temperature program
		double dTotalTime = this.m_dInitialTime;
		double dLastTemp = this.m_dInitialTemperature;

		for (int i = 0; i < contentPane.tmTemperatureProgram.getRowCount(); i++)
		{
			double dRamp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 0);
			double dFinalTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 1);
			double dFinalTime = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 2);
			
			if (dRamp != 0)
				dTotalTime += (dFinalTemp - dLastTemp) / dRamp;
			
			dTotalTime += dFinalTime;
			
			dLastTemp = dFinalTemp;
		}
		
		m_dPlotXMax = Math.max(dTotalTime * 1.2, dMax * 1.02);
		
		// Begin drawing temperature program and hold-up time vs. time
    	contentPane.m_GraphControlTemp.RemoveAllSeries();
    	contentPane.m_GraphControlHoldUp.RemoveAllSeries();

    	int iIdealPlotIndex = contentPane.m_GraphControlTemp.AddSeries("Ideal Gradient", new Color(0, 0, 0), 1, false, false);
    	int iIdealPlotIndexHoldUp = contentPane.m_GraphControlHoldUp.AddSeries("Ideal Hold Up", new Color(0, 0, 0), 1, false, false);

    	// Draw first data points
    	contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, 0, m_dInitialTemperature);
    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, 0, m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dInitialTemperature));
    	
    	contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, m_dInitialTime * 60, m_dInitialTemperature);
    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, m_dInitialTime * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dInitialTemperature));

    	// Draw the rest of the profiles
    	dTotalTime = m_dInitialTime;
    	dLastTemp = m_dInitialTemperature;
    	double dFinalTemp = m_dInitialTemperature;

    	for (int i = 0; i < contentPane.tmTemperatureProgram.getRowCount(); i++)
		{
			double dRamp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 0);
			dFinalTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 1);
			double dFinalTime = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 2);

			if (dRamp != 0)
			{
				double dLastTime = dTotalTime;
				dTotalTime += (dFinalTemp - dLastTemp) / dRamp;
				contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, dTotalTime * 60, dFinalTemp);
		    	
				// Add many data points to show flow rate.
				for (int j = 1; j < 500; j++)
				{
					double dTime = ((dTotalTime - dLastTime) * ((double)j / 500.0)) + dLastTime;
					double dTemperature = ((dFinalTemp - dLastTemp) * ((double)j / 500.0)) + dLastTemp;
					contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, dTime * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dTemperature));
				}
			}
			
			if (dFinalTime != 0)
			{
				dTotalTime += dFinalTime;
				contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, dTotalTime * 60, dFinalTemp);				
		    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, dTotalTime * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dFinalTemp));
			}
			
			dLastTemp = dFinalTemp;
		}

		if (contentPane.tmTemperatureProgram.getRowCount() > 0)
		{
			contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, m_dPlotXMax * 60, dFinalTemp);
    		contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, m_dPlotXMax * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dFinalTemp));
		}
    	else
    	{
			contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, m_dPlotXMax * 60, m_dInitialTemperature);
	    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, m_dPlotXMax * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dInitialTemperature));
    	}

   		contentPane.m_GraphControlTemp.AutoScaleX();
   		contentPane.m_GraphControlTemp.AutoScaleY();
    	
    	contentPane.m_GraphControlTemp.repaint();   
    	
   		contentPane.m_GraphControlHoldUp.AutoScaleX();
   		contentPane.m_GraphControlHoldUp.AutoScaleY();
    	
    	contentPane.m_GraphControlHoldUp.repaint();   	
	}
*/
	// Plots:
    // m_InterpolatedSimpleTemperatureProfile + m_InterpolatedTemperatureDifferenceProfile
    // m_InterpolatedSimpleTemperatureProfile + m_dTemperatureProfileDifferenceArray
    // m_InterpolatedHoldUpProfile
    // m_dHoldUpArray
	public void updateGraphs(LinearInterpolationFunction interpolatedSimpleTemperatureProfile, LinearInterpolationFunction interpolatedTemperatureDifferenceProfile, double[][] temperatureProfileDifferenceArray, InterpolationFunction interpolatedHoldUpProfile, double[][] holdUpArray)
	{
		synchronized(temperatureProfileGraph.lockObject)
		{
		synchronized(holdUpProfileGraph.lockObject)
		{
		plotXMax = this.calcPlotXMaximum();

		// Update the graphs with the new m_dGradientArray markers and the m_InterpolatedGradient (and the same with the flow graph)
		temperatureProfileGraph.RemoveSeries(m_iInterpolatedTempProgramSeries);
		temperatureProfileGraph.RemoveSeries(m_iTempProgramMarkerSeries);
		
		holdUpProfileGraph.RemoveSeries(m_iInterpolatedHoldUpSeries);
		holdUpProfileGraph.RemoveSeries(m_iHoldUpMarkerSeries);
		
		Color clrOrange = Color.rgb(240,90,40);
	    m_iInterpolatedTempProgramSeries = temperatureProfileGraph.AddSeries("Interpolated Gradient", clrOrange, 1, false, false);
	    m_iTempProgramMarkerSeries = temperatureProfileGraph.AddSeries("Gradient Markers", clrOrange, 1, true, false);

	    m_iInterpolatedHoldUpSeries = holdUpProfileGraph.AddSeries("Interpolated Flow", clrOrange, 1, false, false);
	    m_iHoldUpMarkerSeries = holdUpProfileGraph.AddSeries("Flow Rate Markers", clrOrange, 1, true, false);

	    double dMinTemp = this.m_dHoldUpArray[0][0];
	    double dMaxTemp = this.m_dHoldUpArray[m_dHoldUpArray.length - 1][0];
	    
	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = ((double)i / (double)(iNumPoints - 1)) * (plotXMax * 60);
	    	temperatureProfileGraph.AddDataPoint(m_iInterpolatedTempProgramSeries, dXPos, interpolatedSimpleTemperatureProfile.getAt(dXPos / 60) + interpolatedTemperatureDifferenceProfile.getAt(dXPos / 60));
	    	dXPos = (((double)i / (double)(iNumPoints - 1)) * (dMaxTemp - dMinTemp)) + dMinTemp;
	    	holdUpProfileGraph.AddDataPoint(m_iInterpolatedHoldUpSeries, dXPos, interpolatedHoldUpProfile.getAt(dXPos));
	    }
	    
	    for (int i = 0; i < temperatureProfileDifferenceArray.length; i++)
	    {
	    	temperatureProfileGraph.AddDataPoint(m_iTempProgramMarkerSeries, temperatureProfileDifferenceArray[i][0] * 60, interpolatedSimpleTemperatureProfile.getAt(temperatureProfileDifferenceArray[i][0]) + temperatureProfileDifferenceArray[i][1]);
	    }
		
	    for (int i = 0; i < m_dHoldUpArray.length; i++)
	    {
	    	holdUpProfileGraph.AddDataPoint(m_iHoldUpMarkerSeries, holdUpArray[i][0], holdUpArray[i][1]);
	    }
	    
	    temperatureProfileGraph.autoScaleX();
	    temperatureProfileGraph.autoScaleY();
	    holdUpProfileGraph.autoScaleX();
	    holdUpProfileGraph.autoScaleY();
     
	    temperatureProfileGraph.repaint();
	    holdUpProfileGraph.repaint();
		}
		}
	}
	
	private void updateTable(ObservableList<StandardCompound> newStandardsList)
	{
		if (newStandardsList.size() != standardsList.size())
			return;
		
		for (int i = 0; i < newStandardsList.size(); i++)
		{
			standardsList.get(i).makeEqualTo(newStandardsList.get(i));
		}
	}
	
	public void beginBackCalculation(boolean bFlowRateProfileBackCalculationFirst)
	{
		this.stepThreePaneController.setBackCalculationButtonDisable(true);
		
       	task = new BackCalculateTask();
		task.setOptimizationOrder(bFlowRateProfileBackCalculationFirst);
		task.setStandardsList(standardsList);
		this.stepThreePaneController.progressBarProperty().bind(task.progressProperty());
		this.stepThreePaneController.statusLabelProperty().bind(task.messageProperty());
		this.stepThreePaneController.varianceLabelProperty().bind(task.varianceProperty());
		this.stepThreePaneController.lastIterationVarianceLabelProperty().bind(task.lastIterationVarianceProperty());
		this.stepThreePaneController.iterationLabelProperty().bind(task.iterationProperty());
		this.stepThreePaneController.percentImprovementLabelProperty().bind(task.percentImprovementProperty());
		this.stepThreePaneController.timeElapsedLabelProperty().bind(task.timeElapsedProperty());
		
        Thread backCalculateThread = new Thread(task);
        backCalculateThread.start();
	}
	
    class BackCalculateTask extends Task
    {
    	private boolean bFlowRateProfileBackCalculationFirst = true;
    	private SimpleStringProperty varianceProperty = new SimpleStringProperty("");
    	private SimpleStringProperty lastIterationVarianceProperty = new SimpleStringProperty("");
    	private SimpleStringProperty iterationProperty = new SimpleStringProperty("");
    	private SimpleStringProperty percentImprovementProperty = new SimpleStringProperty("");
    	private SimpleStringProperty timeElapsedProperty = new SimpleStringProperty("");
    	private ObservableList<StandardCompound> standardsList;

    	private boolean updateVarianceReady = true;
    	private boolean updateLastIterationVarianceReady = true;
    	private boolean updateIterationReady = true;
    	private boolean updatePercentImprovementReady = true;
    	private boolean updateTimeElapsedReady = true;
    	private boolean updateGraphsReady = true;
    	private boolean updateTableReady = true;

    	public ObservableList<StandardCompound> getStandardsList()
    	{
    		return standardsList;
    	}
    	
    	public void setStandardsList(ObservableList<StandardCompound> standardsList)
    	{
    		// Make a deep copy of the standardsList
    		this.standardsList = FXCollections.observableArrayList();
    		for (int i = 0; i < standardsList.size(); i++)
    		{
    			StandardCompound newStandardCompound = new StandardCompound(standardsList.get(i).getUse(), standardsList.get(i).getName(), standardsList.get(i).getMz(), standardsList.get(i).getMeasuredRetentionTime(), standardsList.get(i).getPredictedRetentionTime(), standardsList.get(i).getIndex());
    			this.standardsList.add(newStandardCompound);
    		}
    	}
    	
    	public SimpleStringProperty varianceProperty()
    	{
    		return varianceProperty;
    	}
    	
    	public SimpleStringProperty lastIterationVarianceProperty()
    	{
    		return lastIterationVarianceProperty;
    	}

    	public SimpleStringProperty percentImprovementProperty()
    	{
    		return percentImprovementProperty;
    	}

    	public SimpleStringProperty timeElapsedProperty()
    	{
    		return timeElapsedProperty;
    	}

    	public SimpleStringProperty iterationProperty()
    	{
    		return iterationProperty;
    	}
    	
    	private void updateVariance(double variance)
    	{
    		if (!updateVarianceReady)
    			return;
    		
			NumberFormat formatter1 = new DecimalFormat("#0.000000");
			NumberFormat formatter2 = new DecimalFormat("0.0000E0");

    		final String str;
    		if (variance < 0.0001)
				str = formatter2.format(variance);
			else
				str = formatter1.format(variance);
    		
    		updateVarianceReady = false;
    		
    		Platform.runLater(new Runnable(){
                @Override
                public void run() {
                	varianceProperty.set(str);
            		updateVarianceReady = true;
                }
            });	
    	}
    	
    	private void updateLastIterationVariance(final double lastIterationVariance)
    	{
    		if (!updateLastIterationVarianceReady)
    			return;
    		
			NumberFormat formatter1 = new DecimalFormat("#0.000000");
			NumberFormat formatter2 = new DecimalFormat("0.0000E0");

    		final String str;
    		if (lastIterationVariance < 0.0001)
				str = formatter2.format(lastIterationVariance);
			else
				str = formatter1.format(lastIterationVariance);
    		
    		updateLastIterationVarianceReady = false;
    		
    		Platform.runLater(new Runnable(){
                @Override
                public void run() {
                	lastIterationVarianceProperty.set(str);
            		updateLastIterationVarianceReady = true;
                }
            });	
    	}

    	private void updatePercentImprovement(double percentImprovement, boolean show)
    	{
    		if (!updatePercentImprovementReady)
    			return;
    		
			NumberFormat percentFormatter = new DecimalFormat("0.00");
			final String str;
			if (show)
				str = percentFormatter.format(percentImprovement) + "%";
			else
				str = "";
			
			updatePercentImprovementReady = false;

    		Platform.runLater(new Runnable(){
                @Override
                public void run() {
                	percentImprovementProperty.set(str);
                	updatePercentImprovementReady = true;
                }
            });
    	}

    	private void updateTimeElapsed(long starttime)
    	{
    		if (!updateTimeElapsedReady)
    			return;
    		
    		NumberFormat timeformatter = new DecimalFormat("00");
    		
    		long currentTime = System.currentTimeMillis();
    		long lNumSecondsPassed = (currentTime - starttime) / 1000;
    		long lNumDaysPassed = lNumSecondsPassed / (24 * 60 * 60);
    		lNumSecondsPassed -= lNumDaysPassed * (24 * 60 * 60);
    		long lNumHoursPassed = lNumSecondsPassed / (60 * 60);
    		lNumSecondsPassed -= lNumHoursPassed * (60 * 60);
    		long lNumMinutesPassed = lNumSecondsPassed / (60);
    		lNumSecondsPassed -= lNumMinutesPassed * (60);
    		
    		final String str = timeformatter.format(lNumHoursPassed) + ":" + timeformatter.format(lNumMinutesPassed) + ":" + timeformatter.format(lNumSecondsPassed);

    		updateTimeElapsedReady = false;

    		Platform.runLater(new Runnable(){
                @Override
                public void run() {
                	timeElapsedProperty.set(str);
            		updateTimeElapsedReady = true;
                }
            });
    	}

    	private void updateIteration(final int iteration)
    	{
    		if (!updateIterationReady)
    			return;
    		
        	updateIterationReady = false;

        	Platform.runLater(new Runnable(){
                @Override
                public void run() {
                	iterationProperty.set(Integer.toString(iteration));
                	updateIterationReady = true;
               	}
            });
    	}

    	private void updateTable()
    	{
    		if (!updateTableReady)
    			return;

    		// Make a deep copy of the standardsList
    		final ObservableList<StandardCompound> currentStandardsList = FXCollections.observableArrayList();
    		for (int i = 0; i < standardsList.size(); i++)
    		{
    			StandardCompound newStandardCompound = new StandardCompound(standardsList.get(i).getUse(), standardsList.get(i).getName(), standardsList.get(i).getMz(), standardsList.get(i).getMeasuredRetentionTime(), standardsList.get(i).getPredictedRetentionTime(), standardsList.get(i).getIndex());
    			currentStandardsList.add(newStandardCompound);
    		}

    		updateTableReady = false;
    		
    		Platform.runLater(new Runnable(){
                @Override
                public void run() {
                	BackcalculateController.this.updateTable(currentStandardsList);
                	updateTableReady = true;
                }
            });
    	}

    	// m_InterpolatedSimpleTemperatureProfile + m_InterpolatedTemperatureDifferenceProfile
        // m_InterpolatedSimpleTemperatureProfile + m_dTemperatureProfileDifferenceArray
        // m_InterpolatedHoldUpProfile
        // m_dHoldUpArray

    	private void updateGraphs()
    	{
    		if (!updateGraphsReady)
    			return;

        	// Make a copy of the hold up array
    		final double[][] holdUpArray = new double[m_dHoldUpArray.length][2];
        	for (int i = 0; i < BackcalculateController.this.m_dHoldUpArray.length; i++)
        	{
        		holdUpArray[i][0] = BackcalculateController.this.m_dHoldUpArray[i][0];
        		holdUpArray[i][1] = BackcalculateController.this.m_dHoldUpArray[i][1];
        	}
        	
        	// Make a copy of the interpolated hold up array
        	final InterpolationFunction interpolatedHoldUpProfile = new InterpolationFunction(holdUpArray);
        	
        	// Make a copy of the interpolated simple temperature profile
        	final LinearInterpolationFunction interpolatedSimpleTemperatureProfile = new LinearInterpolationFunction(BackcalculateController.this.m_dSimpleTemperatureProfileArray);
        	
        	// Make a copy of the temperature profile difference array
        	final double[][] temperatureProfileDifferenceArray = new double[m_dTemperatureProfileDifferenceArray.length][2];
        	for (int i = 0; i < BackcalculateController.this.m_dTemperatureProfileDifferenceArray.length; i++)
        	{
        		temperatureProfileDifferenceArray[i][0] = BackcalculateController.this.m_dTemperatureProfileDifferenceArray[i][0];
        		temperatureProfileDifferenceArray[i][1] = BackcalculateController.this.m_dTemperatureProfileDifferenceArray[i][1];
        	}
        	
        	// Make a copy of the interpolated temperature profile difference array.
        	final LinearInterpolationFunction interpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(temperatureProfileDifferenceArray);
        	
        	updateGraphsReady = false;
        	
    		Platform.runLater(new Runnable(){
                @Override
                public void run() {
                	BackcalculateController.this.updateGraphs(interpolatedSimpleTemperatureProfile, interpolatedTemperatureDifferenceProfile, temperatureProfileDifferenceArray, interpolatedHoldUpProfile, holdUpArray);
                	updateGraphsReady = true;
                }
            });
    		updateTable();
    	}
    	
    	public void setOptimizationOrder(boolean bFlowRateProfileBackCalculationFirst)
    	{
    		this.bFlowRateProfileBackCalculationFirst = bFlowRateProfileBackCalculationFirst;
    	}
    	
        @Override
        public void done() 
        {
        	// Calculate median of errors
        	if (!this.isCancelled())
        	{
	        	double[] dErrors = new double[standardsList.size()];
	        	for (int i = 0; i < standardsList.size(); i++)
	        	{
	        		dErrors[i] = Math.abs(standardsList.get(i).getError());
	        	}
	        	Arrays.sort(dErrors);
	        	double dMedianError = dErrors[(int)(standardsList.size() / 2)];
	        	
	        	// Now find the compound with the maximum error
	        	int iCompoundWithMaxError = 0;
	        	for (int i = 1; i < standardsList.size(); i++)
	        	{
	        		double dMaxError = Math.abs((Double)standardsList.get(iCompoundWithMaxError).getError());
	        		double dThisError = Math.abs((Double)standardsList.get(i).getError());
	        		
	        		if (dThisError > dMaxError)
	        			iCompoundWithMaxError = i;
	        	}
	        	
	        	// Determine the ratio of the max error to the median
	    		double dMaxError = Math.abs((Double)standardsList.get(iCompoundWithMaxError).getError());
	        	double dRatio = dMaxError / dMedianError;
	        	
	        	final int compoundWithMaxError = iCompoundWithMaxError;
	        	if (dRatio > 8)
	        	{
	        		// Report a problem with the retention time for the compound
	    			Platform.runLater(new Runnable(){
	    	            @Override
	    	            public void run() {
	    	            	String strMessage = "Please double-check the retention time you reported for alkane " + standardsList.get(compoundWithMaxError).getName() + ". It does not seem to be correct.\n\nTo correct the retention time you entered, click \"Previous Step\", change the retention time, and re-run the back-calculation.\nOtherwise, if you are sure that the retention time is correct, continue to the next step.";
	    	    			FXOptionPane.showMessageDialog(mainGrid.getScene().getWindow() , strMessage, "Check Retention Times", FXOptionPane.WARNING_MESSAGE);
	    	            }
	    	        });	
	        		
	        		buttonNextStep.setDisable(false);
	        		updateProgress(100, 100);
	        		updateMessage("Optimization complete, but errors may exist.");
	        	}
	        	else if (m_dVariance > 0.0001)
	        	{
	        		// Report a problem with the retention time for the compound
	    			Platform.runLater(new Runnable(){
	    	            @Override
	    	            public void run() {
	    	            	String strMessage = "Your variance is high. This usually indicates that more than one of the alkane retention times you entered are incorrect.\nPlease double-check all of the alkane retention times you entered.\n\nTo correct the retention times, click \"Previous Step\", change the retention times, and re-run the back-calculation.\nOtherwise, if you are sure that the retention times are all correct, continue to the next step.";
	    	        		FXOptionPane.showMessageDialog(mainGrid.getScene().getWindow(), strMessage, "High Variance", FXOptionPane.WARNING_MESSAGE);
	    	            }
	    	        });	
	        		
	        		buttonNextStep.setDisable(false);
	        		updateProgress(100, 100);
	        		updateMessage("Optimization complete, but errors may exist.");
	        	}
	        	else
	        	{
	        		buttonNextStep.setDisable(false);
	        		updateProgress(100, 100);
	        		updateMessage("Optimization complete! Continue to next step.");
	        	}
        	}
        }

		@Override
		protected Object call() throws Exception {
			
            backCalculate(bFlowRateProfileBackCalculationFirst);

            return null;

		}
		
	    public double calcAngleDifferenceHoldUp(int iIndex)
	    {
	    	double dTotalAngleError = 0;
	    	double dHoldUpRange = 20;
	    	
	    	for (int i = 0; i < m_dHoldUpArray.length; i++)
	    	{
	        	if (i < 2)
	        		continue;
	        	
	        	double dTime2 = m_dHoldUpArray[i][0];
	        	double dHoldUp2 = m_dHoldUpArray[i][1];
	        	double dTime1 = m_dHoldUpArray[i - 1][0];
	        	double dHoldUp1 = m_dHoldUpArray[i - 1][1];
	        	double dTime0 = m_dHoldUpArray[i - 2][0];
	        	double dHoldUp0 = m_dHoldUpArray[i - 2][1];
	        	
	        	// First determine angle of previous segment
	    		double dPreviousAdjacent = (dHoldUp1 - dHoldUp0) / dHoldUpRange;
	    		double dPreviousOpposite = dTime1 - dTime0;
	    		double dPreviousAngle;
	    		if (dPreviousAdjacent == 0)
	    			dPreviousAngle = Math.PI / 2; // 90 degrees
	    		else
	    			dPreviousAngle = Math.atan(dPreviousOpposite / dPreviousAdjacent);
	    		
	    		if (dPreviousAngle < 0)
	    			dPreviousAngle = Math.PI + dPreviousAngle;
	    		
	    		double dAdjacent = (dHoldUp2 - dHoldUp1) / dHoldUpRange;
	    		double dOpposite = dTime2 - dTime1;
	    		double dNewAngle;
	    		if (dAdjacent == 0)
	    			dNewAngle = Math.PI / 2; // 90 degrees
	    		else
	    			dNewAngle = Math.atan(dOpposite / dAdjacent);
	    		
	    		if (dNewAngle < 0)
	    			dNewAngle = Math.PI + dNewAngle;
	    		
	    		double dAngleError = Math.pow((Math.abs(dNewAngle - dPreviousAngle) / (Math.PI)), 2);
	    		dTotalAngleError += dAngleError;
	    	}
	    	
	     	return (dTotalAngleError * 10000) + 1;
	    }
	    
	    public double calcAngleDifferenceTemp()
	    {
	    	double dTotalAngleError = 0;
	    	double dMaxRampRate = 10;
	    	
	    	for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
	    	{
	        	if (i < 2)
	        		continue;
	        	
	        	double dTime2 = m_dTemperatureProfileDifferenceArray[i][0];
	        	double dTemp2 = m_dTemperatureProfileDifferenceArray[i][1];
	        	double dTime1 = m_dTemperatureProfileDifferenceArray[i - 1][0];
	        	double dTemp1 = m_dTemperatureProfileDifferenceArray[i - 1][1];
	        	double dTime0 = m_dTemperatureProfileDifferenceArray[i - 2][0];
	        	double dTemp0 = m_dTemperatureProfileDifferenceArray[i - 2][1];
	        	
	        	// Check if the previous point is a corner
	        	// If it is, then don't worry about the angle - return 0
	    		/*boolean bIsCorner = false;
	    		
	        	for (int j = 0; j < this.m_dIdealTemperatureProfileArray.length - 1; j++)
	    		{
	    			if (this.m_dIdealTemperatureProfileArray[j][0] == dTime1)
	    			{
	    				bIsCorner = true;
	    				break;
	    			}
	    		}
	        	
	        	if (bIsCorner)
	        		continue;*/

	    	   	// First determine angle of previous segment
	    		double dPreviousAdjacent = (dTemp1 - dTemp0) / dMaxRampRate;
	    		double dPreviousOpposite = dTime1 - dTime0;
	    		double dPreviousAngle;
	    		if (dPreviousAdjacent == 0)
	    			dPreviousAngle = Math.PI / 2; // 90 degrees
	    		else
	    			dPreviousAngle = Math.atan(dPreviousOpposite / dPreviousAdjacent);
	    		
	    		if (dPreviousAngle < 0)
	    			dPreviousAngle = Math.PI + dPreviousAngle;
	    		
	    		double dAdjacent = (dTemp2 - dTemp1) / dMaxRampRate;
	    		double dOpposite = dTime2 - dTime1;
	    		double dNewAngle;
	    		if (dAdjacent == 0)
	    			dNewAngle = Math.PI / 2; // 90 degrees
	    		else
	    			dNewAngle = Math.atan(dOpposite / dAdjacent);
	    		
	    		if (dNewAngle < 0)
	    			dNewAngle = Math.PI + dNewAngle;
	    		
	    		double dAngleError = Math.pow((Math.abs(dNewAngle - dPreviousAngle) / (Math.PI)), 2);
	    		dTotalAngleError += dAngleError;
	    	}
	    	
	     	return dTotalAngleError + 1;
	    }
		
	 	public double goldenSectioningSearchSimpleTemperatureProfile(boolean bTmax, double dStep, double dPrecision, double dMaxChangeAtOnce)
	 	{
			double dRetentionError = 1;
			double x1;
			double x2;
			double x3;
			double dRetentionErrorX1;
			double dRetentionErrorX2;
			double dRetentionErrorX3;
			
			double dLastGuess;
			
			if (bTmax)
				dLastGuess = m_dTmax;
			else
				dLastGuess = m_dk;
			
			// Find bounds
			if (bTmax)
				x1 = m_dTmax;
			else
				x1 = m_dk;
			dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
			
			x2 = x1 + dStep;
			if (bTmax)
				m_dTmax = x2;
			else
				m_dk = x2;
			calcSimpleTemperatureProgram(m_dk, m_dTmax);
			dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());

			if (dRetentionErrorX2 < dRetentionErrorX1)
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = (x3 - x1) * m_dGoldenRatio + x3;
				
				if (bTmax)
					m_dTmax = x2;
				else
					m_dk = x2;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);
				dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());

				while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;

					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					
					x2 = (x3 - x1) * m_dGoldenRatio + x3;
					
					if (bTmax)
						m_dTmax = x2;
					else
						m_dk = x2;
					calcSimpleTemperatureProgram(m_dk, m_dTmax);
					dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				
				x1 = x3 - (x2 - x3) * m_dGoldenRatio;
				
				if (bTmax)
					m_dTmax = x1;
				else
					m_dk = x1;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);
				dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());

				while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;

					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;

					x1 = x3 - (x2 - x3) * m_dGoldenRatio;
					
					if (bTmax)
						m_dTmax = x1;
					else
						m_dk = x1;
					calcSimpleTemperatureProgram(m_dk, m_dTmax);
					dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastGuess + dMaxChangeAtOnce)
			{
				if (bTmax)
					m_dTmax = dLastGuess + dMaxChangeAtOnce;
				else
					m_dk = dLastGuess + dMaxChangeAtOnce;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);
				dRetentionError = calcRetentionError(tStep, standardsList.size());

				return dRetentionError;
			}
			
			if (x1 < dLastGuess - dMaxChangeAtOnce)
			{
				/*if (dLastGuess - dMaxChangeAtOnce < 0.00001)
				{
					if (bNonMixingVolume)
						m_dNonMixingVolume = 0.00001;
					else
						m_dMixingVolume = 0.00001;
				}
				else*/
				if (bTmax)
					m_dTmax = dLastGuess - dMaxChangeAtOnce;
				else
					m_dk = dLastGuess - dMaxChangeAtOnce;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);
				dRetentionError = calcRetentionError(tStep, standardsList.size());
				
				return dRetentionError;
			}
			
			// Loop of optimization
			while ((x2 - x1) > dPrecision)
			{
				double x4;
				double dRetentionErrorX4;
				double dAngleErrorX4;
				
				// Is the bigger gap between x3 and x2 or x3 and x1?
				if (x2 - x3 > x3 - x1) 
				{
					// x3 and x2, so x4 must be placed between them
					x4 = x3 + (2 - m_dGoldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - m_dGoldenRatio) * (x3 - x1);
				}

				if (bTmax)
					m_dTmax = x4;
				else
					m_dk = x4;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);			
				dRetentionErrorX4 = calcRetentionError(tStep, standardsList.size());
				
				// Decide what to do next
				if (dRetentionErrorX4 < dRetentionErrorX3)
				{
					// Our new guess was better
					// Where did we put our last guess again?
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x1 = x3;
						dRetentionErrorX1 = dRetentionErrorX3;

						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x2 = x3;
						dRetentionErrorX2 = dRetentionErrorX3;							

						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
					}
				}
				else
				{
					// Our new guess was worse
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x2 = x4;
						dRetentionErrorX2 = dRetentionErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x1 = x4;
						dRetentionErrorX1 = dRetentionErrorX4;							
					}
				}
			}
			
			// Restore profile to best value
			if (bTmax)
				m_dTmax = x3;
			else
				m_dk = x3;
			calcSimpleTemperatureProgram(m_dk, m_dTmax);			
			dRetentionError = dRetentionErrorX3;
	 		
	 		return dRetentionError;
	 	}
	 	
	 	public double goldenSectioningSearchTemperatureProfile(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, /*boolean bMinimizeAngles,*/ double dAngleErrorMultiplier)
	 	{
			double dRetentionError = 1;
			double x1;
			double x2;
			double x3;
			double dRetentionErrorX1;
			double dRetentionErrorX2;
			double dRetentionErrorX3;
			double dAngleErrorX1;
			double dAngleErrorX2;
			double dAngleErrorX3;
			
			double dLastTempGuess = m_dTemperatureProfileDifferenceArray[iIndex][1];
			
			// Find bounds
			x1 = m_dTemperatureProfileDifferenceArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
			dAngleErrorX1 = calcAngleDifferenceTemp();
			
			x2 = x1 + dStep;
			m_dTemperatureProfileDifferenceArray[iIndex][1] = x2;
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());
			dAngleErrorX2 = calcAngleDifferenceTemp();
			
			//double dDiffPercent = (dRetentionErrorX2 - dRetentionErrorX1) / ((dRetentionErrorX2 + dRetentionErrorX1) / 2);
			//double dDiffPercent2 = (dAngleErrorX2 - dAngleErrorX1) / ((dAngleErrorX2 + dAngleErrorX1) / 2);
			//dDiffPercent2 *= dAngleErrorMultiplier;
			
			if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * m_dGoldenRatio + x3;
				
				m_dTemperatureProfileDifferenceArray[iIndex][1] = x2;
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());
				dAngleErrorX2 = calcAngleDifferenceTemp();

				//dDiffPercent = (dRetentionErrorX2 - dRetentionErrorX3) / ((dRetentionErrorX2 + dRetentionErrorX3) / 2);
				//dDiffPercent2 = (dAngleErrorX2 - dAngleErrorX3) / ((dAngleErrorX2 + dAngleErrorX3) / 2);
				//dDiffPercent2 *= dAngleErrorMultiplier;

				while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastTempGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					dAngleErrorX3 = dAngleErrorX2;
					
					x2 = (x3 - x1) * m_dGoldenRatio + x3;
					
					m_dTemperatureProfileDifferenceArray[iIndex][1] = x2;
					m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
					dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());
					dAngleErrorX2 = calcAngleDifferenceTemp();
					
					//dDiffPercent = (dRetentionErrorX2 - dRetentionErrorX3) / ((dRetentionErrorX2 + dRetentionErrorX3) / 2);
					//dDiffPercent2 = (dAngleErrorX2 - dAngleErrorX3) / ((dAngleErrorX2 + dAngleErrorX3) / 2);
					//dDiffPercent2 *= dAngleErrorMultiplier;
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * m_dGoldenRatio;
				
				m_dTemperatureProfileDifferenceArray[iIndex][1] = x1;
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
				dAngleErrorX1 = calcAngleDifferenceTemp();

				//dDiffPercent = (dRetentionErrorX1 - dRetentionErrorX3) / ((dRetentionErrorX1 + dRetentionErrorX3) / 2);
				//dDiffPercent2 = (dAngleErrorX1 - dAngleErrorX3) / ((dAngleErrorX1 + dAngleErrorX3) / 2);
				//dDiffPercent2 *= dAngleErrorMultiplier;

				while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastTempGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;
					dAngleErrorX3 = dAngleErrorX1;

					x1 = x3 - (x2 - x3) * m_dGoldenRatio;
					
					m_dTemperatureProfileDifferenceArray[iIndex][1] = x1;
					m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
					dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
					dAngleErrorX1 = calcAngleDifferenceTemp();
					
					//dDiffPercent = (dRetentionErrorX1 - dRetentionErrorX3) / ((dRetentionErrorX1 + dRetentionErrorX3) / 2);
					//dDiffPercent2 = (dAngleErrorX1 - dAngleErrorX3) / ((dAngleErrorX1 + dAngleErrorX3) / 2);
					//dDiffPercent2 *= dAngleErrorMultiplier;
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastTempGuess + dMaxChangeAtOnce)
			{
				m_dTemperatureProfileDifferenceArray[iIndex][1] = dLastTempGuess + dMaxChangeAtOnce;
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionError = calcRetentionError(tStep, standardsList.size());

				return dRetentionError;
			}
			
			if (x1 < dLastTempGuess - dMaxChangeAtOnce)
			{
				m_dTemperatureProfileDifferenceArray[iIndex][1] = dLastTempGuess - dMaxChangeAtOnce;
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionError = calcRetentionError(tStep, standardsList.size());
				
				return dRetentionError;
			}
			
			// Loop of optimization
			while ((x2 - x1) > dPrecision)
			{
				double x4;
				double dRetentionErrorX4;
				double dAngleErrorX4;
				
				// Is the bigger gap between x3 and x2 or x3 and x1?
				if (x2 - x3 > x3 - x1) 
				{
					// x3 and x2, so x4 must be placed between them
					x4 = x3 + (2 - m_dGoldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - m_dGoldenRatio) * (x3 - x1);
				}

				m_dTemperatureProfileDifferenceArray[iIndex][1] = x4;
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX4 = calcRetentionError(tStep, standardsList.size());
				dAngleErrorX4 = calcAngleDifferenceTemp();
				
				//dDiffPercent = (dRetentionErrorX4 - dRetentionErrorX3) / ((dRetentionErrorX4 + dRetentionErrorX3) / 2);
				//dDiffPercent2 = (dAngleErrorX4 - dAngleErrorX3) / ((dAngleErrorX4 + dAngleErrorX3) / 2);
				//dDiffPercent2 *= dAngleErrorMultiplier;

				// Decide what to do next
				if (dRetentionErrorX4 * Math.pow(dAngleErrorX4, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier))
				{
					// Our new guess was better
					// Where did we put our last guess again?
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x1 = x3;
						dRetentionErrorX1 = dRetentionErrorX3;
						dAngleErrorX1 = dAngleErrorX3;
						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
						dAngleErrorX3 = dAngleErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x2 = x3;
						dRetentionErrorX2 = dRetentionErrorX3;							
						dAngleErrorX2 = dAngleErrorX3;
						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
						dAngleErrorX3 = dAngleErrorX4;
					}
				}
				else
				{
					// Our new guess was worse
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x2 = x4;
						dRetentionErrorX2 = dRetentionErrorX4;
						dAngleErrorX2 = dAngleErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x1 = x4;
						dRetentionErrorX1 = dRetentionErrorX4;							
						dAngleErrorX1 = dAngleErrorX4;
					}
				}
			}
			
			// Restore profile to best value
			m_dTemperatureProfileDifferenceArray[iIndex][1] = x3;
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionError = dRetentionErrorX3;
	 		
	 		return dRetentionError;
	 	}
	 	
	 	// offset moves the difference array up/down 
	 	public double goldenSectioningSearchTemperatureProfileOffset(double dStep, double dPrecision, int iNumCompoundsToUse)
	 	{
			double dRetentionError = 1;
			double x1;
			double x2;
			double x3;
			double dRetentionErrorX1;
			double dRetentionErrorX2;
			double dRetentionErrorX3;
			
			// Find bounds
			x1 = m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1];
			dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
			
			x2 = x1 + dStep;
			
			double dPercentDiff = (x2 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
			for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
			{
				m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
			}
			
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(tStep, iNumCompoundsToUse);
					
			if (dRetentionErrorX2 < dRetentionErrorX1)
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = (x3 - x1) * m_dGoldenRatio + x3;
				
				dPercentDiff = (x2 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
				for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
				{
					m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
				}

				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(tStep, iNumCompoundsToUse);
				
				while (dRetentionErrorX2 < dRetentionErrorX3)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;

					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					
					x2 = (x3 - x1) * m_dGoldenRatio + x3;
					
					dPercentDiff = (x2 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
					for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
					{
						m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
					}

					m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
					dRetentionErrorX2 = calcRetentionError(tStep, iNumCompoundsToUse);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				
				x1 = x3 - (x2 - x3) * m_dGoldenRatio;
				
				dPercentDiff = (x1 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
				for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
				{
					m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
				}
				
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(tStep, iNumCompoundsToUse);

				while (dRetentionErrorX1 < dRetentionErrorX3)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;

					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;

					x1 = x3 - (x2 - x3) * m_dGoldenRatio;
					
					dPercentDiff = (x1 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
					for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
					{
						m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
					}

					m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
					dRetentionErrorX1 = calcRetentionError(tStep, iNumCompoundsToUse);
				}
			}
			
			// Loop of optimization
			while ((x2 - x1) > dPrecision)
			{
				double x4;
				double dRetentionErrorX4;
				
				// Is the bigger gap between x3 and x2 or x3 and x1?
				if (x2 - x3 > x3 - x1) 
				{
					// x3 and x2, so x4 must be placed between them
					x4 = x3 + (2 - m_dGoldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - m_dGoldenRatio) * (x3 - x1);
				}

				dPercentDiff = (x4 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
				for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
				{
					m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
				}

				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX4 = calcRetentionError(tStep, iNumCompoundsToUse);
				
				// Decide what to do next
				if (dRetentionErrorX4 < dRetentionErrorX3)
				{
					// Our new guess was better
					// Where did we put our last guess again?
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x1 = x3;
						dRetentionErrorX1 = dRetentionErrorX3;

						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x2 = x3;
						dRetentionErrorX2 = dRetentionErrorX3;							

						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
					}
				}
				else
				{
					// Our new guess was worse
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x2 = x4;
						dRetentionErrorX2 = dRetentionErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x1 = x4;
						dRetentionErrorX1 = dRetentionErrorX4;							
					}
				}
			}
			
			// Restore profile to best value
			dPercentDiff = (x3 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
			for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
			{
				m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
			}
			
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionError = dRetentionErrorX3;
	 		
	 		return dRetentionError;
	 	}
	 	
	 	public double goldenSectioningSearchHoldUp(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, double dAngleErrorMultiplier)
	 	{
			double dRetentionError = 1;
			double x1;
			double x2;
			double x3;
			double dRetentionErrorX1;
			double dRetentionErrorX2;
			double dRetentionErrorX3;
			double dAngleErrorX1;
			double dAngleErrorX2;
			double dAngleErrorX3;
			
			double dLastFGuess = m_dHoldUpArray[iIndex][1];

			// Find bounds
			x1 = m_dHoldUpArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
			dAngleErrorX1 = calcAngleDifferenceHoldUp(iIndex);
			
			x2 = x1 + dStep;
			m_dHoldUpArray[iIndex][1] = x2;
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());
			dAngleErrorX2 = calcAngleDifferenceHoldUp(iIndex);
			
			if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * m_dGoldenRatio + x3;
				
				m_dHoldUpArray[iIndex][1] = x2;
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());
				dAngleErrorX2 = calcAngleDifferenceHoldUp(iIndex);

				while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastFGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					dAngleErrorX3 = dAngleErrorX2;
					
					x2 = (x3 - x1) * m_dGoldenRatio + x3;
					
					m_dHoldUpArray[iIndex][1] = x2;
					m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
					dRetentionErrorX2 = calcRetentionError(tStep, standardsList.size());
					dAngleErrorX2 = calcAngleDifferenceHoldUp(iIndex);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * m_dGoldenRatio;
				
				m_dHoldUpArray[iIndex][1] = x1;
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
				dAngleErrorX1 = calcAngleDifferenceHoldUp(iIndex);

				while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastFGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;
					dAngleErrorX3 = dAngleErrorX1;
					
					x1 = x3 - (x2 - x3) * m_dGoldenRatio;
					
					m_dHoldUpArray[iIndex][1] = x1;
					m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
					dRetentionErrorX1 = calcRetentionError(tStep, standardsList.size());
					dAngleErrorX1 = calcAngleDifferenceHoldUp(iIndex);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastFGuess + dMaxChangeAtOnce)
			{
				m_dHoldUpArray[iIndex][1] = dLastFGuess + dMaxChangeAtOnce;
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionError = calcRetentionError(tStep, standardsList.size());
				
				return dRetentionError;
			}
			
			if (x1 < dLastFGuess - dMaxChangeAtOnce)
			{
				m_dHoldUpArray[iIndex][1] = dLastFGuess - dMaxChangeAtOnce;
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionError = calcRetentionError(tStep, standardsList.size());

				return dRetentionError;
			}
			
			// Loop of optimization
			while ((x2 - x1) > dPrecision)
			{
				double x4;
				double dRetentionErrorX4;
				double dAngleErrorX4;
				
				// Is the bigger gap between x3 and x2 or x3 and x1?
				if (x2 - x3 > x3 - x1) 
				{
					// x3 and x2, so x4 must be placed between them
					x4 = x3 + (2 - m_dGoldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - m_dGoldenRatio) * (x3 - x1);
				}
				
				m_dHoldUpArray[iIndex][1] = x4;
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorX4 = calcRetentionError(tStep, standardsList.size());
				dAngleErrorX4 = calcAngleDifferenceHoldUp(iIndex);

				// Decide what to do next
				if (dRetentionErrorX4 * Math.pow(dAngleErrorX4, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier))
				{
					// Our new guess was better
					// Where did we put our last guess again?
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x1 = x3;
						dRetentionErrorX1 = dRetentionErrorX3;
						dAngleErrorX1 = dAngleErrorX3;
						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
						dAngleErrorX3 = dAngleErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x2 = x3;
						dRetentionErrorX2 = dRetentionErrorX3;
						dAngleErrorX2 = dAngleErrorX3;
						x3 = x4;
						dRetentionErrorX3 = dRetentionErrorX4;
						dAngleErrorX3 = dAngleErrorX4;
					}
				}
				else
				{
					// Our new guess was worse
					if (x2 - x3 > x3 - x1) 
					{
						// x4 was in between x3 and x2
						x2 = x4;
						dRetentionErrorX2 = dRetentionErrorX4;
						dAngleErrorX2 = dAngleErrorX4;
					}
					else
					{
						// x4 was in between x1 and x3
						x1 = x4;
						dRetentionErrorX1 = dRetentionErrorX4;
						dAngleErrorX1 = dAngleErrorX4;
					}
				}
			}
			
			// Restore profile to best value
			m_dHoldUpArray[iIndex][1] = x3;
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionError = dRetentionErrorX3;
			
			return dRetentionError;
	 	}
	    
	 	public double goldenSectioningSearchHoldUpOffset(double dStep, double dPrecision, int iNumCompoundsToUse)
	 	{
			double dRetentionError = 1;
	 		double F1;
			double F2;
			double F3;
			double dRetentionErrorF1;
			double dRetentionErrorF2;
			double dRetentionErrorF3;
			
			// Find bounds
			F1 = m_dHoldUpArray[0][1];
			dRetentionErrorF1 = calcRetentionError(tStep, iNumCompoundsToUse);

			F2 = F1 + dStep;
			
			double dDiff = F2 - m_dHoldUpArray[0][1];
			for (int i = 0; i < m_dHoldUpArray.length; i++)
			{
				m_dHoldUpArray[i][1] += dDiff;
			}
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorF2 = calcRetentionError(tStep, iNumCompoundsToUse);
			
			if (dRetentionErrorF2 < dRetentionErrorF1)
			{
				// We're going in the right direction
				F3 = F2;
				dRetentionErrorF3 = dRetentionErrorF2;
				
				F2 = (F3 - F1) * m_dGoldenRatio + F3;
				
				dDiff = F2 - m_dHoldUpArray[0][1];
				for (int i = 0; i < m_dHoldUpArray.length; i++)
				{
					m_dHoldUpArray[i][1] += dDiff;
				}
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorF2 = calcRetentionError(tStep, iNumCompoundsToUse);

				while (dRetentionErrorF2 < dRetentionErrorF3)
				{
					F1 = F3;
					dRetentionErrorF1 = dRetentionErrorF3;
					F3 = F2;
					dRetentionErrorF3 = dRetentionErrorF2;
					
					F2 = (F3 - F1) * m_dGoldenRatio + F3;
					
					dDiff = F2 - m_dHoldUpArray[0][1];
					for (int i = 0; i < m_dHoldUpArray.length; i++)
					{
						m_dHoldUpArray[i][1] += dDiff;
					}
					m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
					dRetentionErrorF2 = calcRetentionError(tStep, iNumCompoundsToUse);
				}
			}
			else
			{
				// We need to go in the opposite direction
				F3 = F1;
				dRetentionErrorF3 = dRetentionErrorF1;
				
				F1 = F3 - (F2 - F3) * m_dGoldenRatio;
				
				dDiff = F1 - m_dHoldUpArray[0][1];
				for (int i = 0; i < m_dHoldUpArray.length; i++)
				{
					m_dHoldUpArray[i][1] += dDiff;
				}
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorF1 = calcRetentionError(tStep, iNumCompoundsToUse);

				while (dRetentionErrorF1 < dRetentionErrorF3)
				{
					F2 = F3;
					dRetentionErrorF2 = dRetentionErrorF3;
					F3 = F1;
					dRetentionErrorF3 = dRetentionErrorF1;
					
					F1 = F3 - (F2 - F3) * m_dGoldenRatio;
					
					dDiff = F1 - m_dHoldUpArray[0][1];
					for (int i = 0; i < m_dHoldUpArray.length; i++)
					{
						m_dHoldUpArray[i][1] += dDiff;
					}
					m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
					dRetentionErrorF1 = calcRetentionError(tStep, iNumCompoundsToUse);
				}
			}
			
			// Loop of optimization
			while ((F2 - F1) > dPrecision)
			{
				double F4;
				double dRetentionErrorF4;
				// Is the bigger gap between F3 and F2 or F3 and F1?
				if (F2 - F3 > F3 - F1) 
				{
					// F3 and F2, so F4 must be placed between them
					F4 = F3 + (2 - m_dGoldenRatio) * (F2 - F3);
				}
				else 
				{
					// F1 and F3, so F4 must be placed between them
					F4 = F3 - (2 - m_dGoldenRatio) * (F3 - F1);
				}

				
				dDiff = F4 - m_dHoldUpArray[0][1];
				for (int i = 0; i < m_dHoldUpArray.length; i++)
				{
					m_dHoldUpArray[i][1] += dDiff;
				}
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorF4 = calcRetentionError(tStep, iNumCompoundsToUse);
				
				// Decide what to do next
				if (dRetentionErrorF4 < dRetentionErrorF3)
				{
					// Our new guess was better
					// Where did we put our last guess again?
					if (F2 - F3 > F3 - F1) 
					{
						// F4 was in between F3 and F2
						F1 = F3;
						dRetentionErrorF1 = dRetentionErrorF3;
						F3 = F4;
						dRetentionErrorF3 = dRetentionErrorF4;
					}
					else
					{
						// F4 was in between F1 and F3
						F2 = F3;
						dRetentionErrorF2 = dRetentionErrorF3;							
						F3 = F4;
						dRetentionErrorF3 = dRetentionErrorF4;
					}
				}
				else
				{
					// Our new guess was worse
					if (F2 - F3 > F3 - F1) 
					{
						// F4 was in between F3 and F2
						F2 = F4;
						dRetentionErrorF2 = dRetentionErrorF4;
					}
					else
					{
						// F4 was in between F1 and F3
						F1 = F4;
						dRetentionErrorF1 = dRetentionErrorF4;							
					}
				}
			}
			
			dDiff = F3 - m_dHoldUpArray[0][1];
			for (int i = 0; i < m_dHoldUpArray.length; i++)
			{
				m_dHoldUpArray[i][1] += dDiff;
			}
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionError = dRetentionErrorF3;
			
			return dRetentionError;
	 	}
	 	
	    public double calcRetentionError(double dtstep, int iNumCompoundsToInclude)
	    {
	    	double dRetentionError = 0;
			
	    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
	    	double dBeta2 = Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2) / (Math.pow(innerDiameter / 2, 2) - Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2));
	    	double dBeta1Beta2 = dBeta1 / dBeta2;
	    	
			double dtRFinal = 0;
			double dXPosition = 0;
			double dLastXPosition = 0;
			double dXMovement = 0;
			boolean bIsEluted = false;
			double dTcA = 0;
			double dTcB = 0;
			double dTc = 0;
			double dHc = 0;
			double dCurVal = 0;
			double dk = 0;
			
			// Create an array of temperatures - this will speed things up
			double dMaxTime = (Double) standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() * 1.5;
			int iNumSteps = (int)(dMaxTime / dtstep);
			double[] dTemps = new double[iNumSteps];
			for (int i = 0; i < iNumSteps; i++)
			{
				double dTime = (double)i * dtstep;
				dTemps[i] = m_InterpolatedSimpleTemperatureProfile.getAt(dTime) + m_InterpolatedTemperatureDifferenceProfile.getAt(dTime);
			}
			                             
			for (int iCompound = 0; iCompound < iNumCompoundsToInclude; iCompound++)
			{
				int iCompoundIndex = standardsList.get(iCompound).getIndex();

				dtRFinal = 0;
				dXPosition = 0;
				dLastXPosition = 0;
				bIsEluted = false;

				dTcA = dTemps[0];
				
				for (int i = 0; i < iNumSteps - 1; i++)
				{
					dTcB = dTemps[i + 1];
					//dTcA = dTcB;

					dTc = (dTcA + dTcB) / 2;
					dHc = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
					
					dk = calckfromT(Globals.AlkaneParamArray[iCompoundIndex], dTc) * dBeta1Beta2;
					
					dCurVal = dtstep / (1 + dk);
					
					dXMovement = dCurVal / dHc;
					
					dLastXPosition = dXPosition;
					dXPosition += dXMovement;
					
					if (dXPosition >= 1)
					{
						dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * dtstep) + ((double)i * dtstep);
						bIsEluted = true;
						break;
					}
					
					dTcA = dTcB;
				}
				
				if (bIsEluted)
				{
					dRetentionError += Math.pow(dtRFinal - (Double)this.standardsList.get(iCompound).getMeasuredRetentionTime(), 2);
					this.standardsList.get(iCompound).setPredictedRetentionTime(dtRFinal);
				}
				else
				{
					dRetentionError += Math.pow((Double)this.standardsList.get(iCompound).getMeasuredRetentionTime(), 2);
					this.standardsList.get(iCompound).setPredictedRetentionTime(-1.0);
					//contentPane2.tmOutputModel.setValueAt("Did not elute", iCompound, 2);
					//contentPane2.tmOutputModel.setValueAt("-", iCompound, 3);
				}
			}
					
	    	return dRetentionError;
	    }
	    
	    // Iterative - in sequence, *Golden* Sectioning Search algorithm
	    // Start by optimizing the entire flow rate error profile.
		public void backCalculate(boolean bHoldUpProfileFirst)
		{
			//if (true)
			//	return;
			long starttime = System.currentTimeMillis();

			updateProgress(-1, 0);
			updateMessage("Please wait, optimization in progress...");

			boolean bForwards = true;
			
			//m_dTemperatureArrayStore = new double[300][m_dTemperatureProfileDifferenceArray.length][2];
			//m_dHoldUpArrayStore = new double[300][m_dHoldUpArray.length][2];
			//m_dRetentionErrorStore = new double[300];
			
			// Phase I conditions:
			boolean bBackCalculateTempProfile = false;
			boolean bBackCalculateTempProfileOffset = true;
			boolean bBackCalculateTempSimpleProfile = true;
			boolean bBackCalculateHoldUpProfile = false;
			boolean bBackCalculateHoldUpProfileOffset = false;//true;
			double dTempProfileAngleWeight = 0;
			double dHoldUpProfileAngleWeight = 0;
			
			boolean bSkipTerminationTest = true;
			
	    	tStep = plotXMax * 0.01;

			// Step #2: Begin back-calculation
			// If bHoldUpProfileFirst then back-calculate the hold up profile first, otherwise do the temperature profile first
			
			int iPhase = 1;
			int iIteration = 0;
			double dLastFullIterationError = 0;
			double dRetentionError = 0;
			
			while (true)
			{
				iIteration++;
				updateIteration(iIteration);
				dLastFullIterationError = dRetentionError;
				
				//if (true)
				//	break;
				
				// Try several values of k and Tmax to find the combination that gives the best accuracy
				if (bBackCalculateTempSimpleProfile)
				{
					// Find the rough dk and dTmax that fit the best
					double dBestTmax = m_dTmax;
					double dBestk = m_dk;
					double dBestError = dRetentionError;

					for (double dTmax = 400; dTmax <= 1000; dTmax = dTmax + 20)
					{
						for (double dk = 0.01; dk <= 1; dk = dk * 1.2)
						{
							calcSimpleTemperatureProgram(dk, dTmax);
							dRetentionError = calcRetentionError(tStep, standardsList.size());
							
							if (dRetentionError < dBestError)
							{
								dBestk = dk;
								dBestTmax = dTmax;
								dBestError = dRetentionError;
							}
						}
					}
					
					dRetentionError = dBestError;
					m_dk = dBestk;
					m_dTmax = dBestTmax;
					calcSimpleTemperatureProgram(m_dk, m_dTmax);
					
					//double dStep = .1;
					//double dPrecision = 0.001;
					//double dMaxChangeAtOnce = 1;

					//dRetentionError = goldenSectioningSearchSimpleTemperatureProfile(false, dStep, dPrecision, dMaxChangeAtOnce);

					//dStep = 10;
					//dPrecision = 0.001;
					//dMaxChangeAtOnce = 100;
					
					//dRetentionError = goldenSectioningSearchSimpleTemperatureProfile(true, dStep, dPrecision, dMaxChangeAtOnce);

					m_dVariance = dRetentionError / standardsList.size();

					updateTimeElapsed(starttime);
					
					updateVariance(dRetentionError / standardsList.size());
					
					//updateGraphs(true);
					
					if (task.isCancelled())
					{
						return;
					}
				}
				
				if (bBackCalculateTempProfileOffset)
				{
					double dTempStep = .5;
					double dTempPrecision = 0.001;
					int iNumCompoundsToUse = standardsList.size();

					dRetentionError = goldenSectioningSearchTemperatureProfileOffset(dTempStep, dTempPrecision, iNumCompoundsToUse);
					m_dVariance = dRetentionError / standardsList.size();

					updateTimeElapsed(starttime);
					
					updateVariance(dRetentionError / standardsList.size());

					updateGraphs();
					
					if (task.isCancelled())
					{
						return;
					}
				}
				
				if (bBackCalculateTempProfile)
				{
					for (int iTimePoint = 0; iTimePoint < m_dTemperatureProfileDifferenceArray.length; iTimePoint++)
					{
						double dTempStep = .5;
						double dMaxChangeAtOnce = 5;
						double dTempPrecision = 0.001;
						
						int iPoint;
						if (bForwards)
							iPoint = iTimePoint;
						else
							iPoint = m_dTemperatureProfileDifferenceArray.length - 1 - iTimePoint;
							
						dRetentionError = goldenSectioningSearchTemperatureProfile(iPoint, dTempStep, dTempPrecision, dMaxChangeAtOnce, dTempProfileAngleWeight);

						m_dVariance = dRetentionError / standardsList.size();
						
						updateTimeElapsed(starttime);
						
						updateVariance(dRetentionError / standardsList.size());

						updateGraphs();

						if (task.isCancelled())
						{
							return;
						}
					}
				}
				
				
				
				// Now we optimize the hold-up time vs. temp profile
				if (bBackCalculateHoldUpProfileOffset)
				{
					double dHoldUpStep = .1;
					double dHoldUpPrecision = .0001;
					int iNumCompoundsToUse = standardsList.size();

					dRetentionError = goldenSectioningSearchHoldUpOffset(dHoldUpStep, dHoldUpPrecision, iNumCompoundsToUse);
					m_dVariance = dRetentionError / standardsList.size();

					updateTimeElapsed(starttime);
					
					updateVariance(dRetentionError / standardsList.size());
					
					updateGraphs();
					
					if (task.isCancelled())
					{
						return;
					}
				}
				
				if (bBackCalculateHoldUpProfile)
				{
					for (int iTimePoint = 0; iTimePoint < m_dHoldUpArray.length; iTimePoint++)
					{
						double dHoldUpStep = .1;
						double dHoldUpPrecision = .0001;
						double dMaxChangeAtOnce = 2;
						
						int iPoint;
						if (bForwards)
							iPoint = iTimePoint;
						else
							iPoint = m_dHoldUpArray.length - 1 - iTimePoint;

						dRetentionError = goldenSectioningSearchHoldUp(iPoint, dHoldUpStep, dHoldUpPrecision, dMaxChangeAtOnce, dHoldUpProfileAngleWeight);
						m_dVariance = dRetentionError / standardsList.size();
						
						updateTimeElapsed(starttime);
						
						updateVariance(dRetentionError / standardsList.size());

						updateGraphs();

						if (task.isCancelled())
						{
							return;
						}
					}
				}
				
				//bForwards = !bForwards;
					
				String str;
				double dNum = dRetentionError / standardsList.size();
				
				updateLastIterationVariance(dRetentionError / standardsList.size());
			
				{
					// Save the new temperature profile
					/*for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
					{
						m_dTemperatureArrayStore[iIteration - 1][i][0] = m_dTemperatureProfileDifferenceArray[i][0];
						m_dTemperatureArrayStore[iIteration - 1][i][1] = m_dTemperatureProfileDifferenceArray[i][1];
					}
		
					// Save the new hold-up time profile
					for (int i = 0; i < m_dHoldUpArray.length; i++)
					{
						m_dHoldUpArrayStore[iIteration - 1][i][0] = m_dHoldUpArray[i][0];
						m_dHoldUpArrayStore[iIteration - 1][i][1] = m_dHoldUpArray[i][1];
					}
					
					// Save the retention error for this iteration
					m_dRetentionErrorStore[iIteration - 1] = dRetentionError;
					*/
				}
				
				// Calculate the percent improvement
				if (!bSkipTerminationTest)//dLastFullIterationError != 0)
				{
					double dPercentImprovement = (1 - (dRetentionError / dLastFullIterationError)) * 100;
					updatePercentImprovement(dPercentImprovement, true);
					
					if (iPhase == 1)
					{
						if (dPercentImprovement < 2 && dPercentImprovement >= 0)
						{
							iPhase = 2;
							//contentPane2.jlblPhase.setText("II");
							
							bBackCalculateTempProfileOffset = false;
							bBackCalculateTempSimpleProfile = false;
							bBackCalculateTempProfile = true;
							bBackCalculateHoldUpProfileOffset = false;
							bBackCalculateHoldUpProfile = true;
							dTempProfileAngleWeight = 20;
							dHoldUpProfileAngleWeight = 2;
							bSkipTerminationTest = true;
					    	//m_dtstep = m_dPlotXMax2 * 0.001;
						}
					}
					else if (iPhase == 2)
					{
						if (dPercentImprovement < 1 && dPercentImprovement >= 0)
						{
							if (Globals.MEASUREKDATA)
								iPhase = 3;
							else
								iPhase = 7;
							
							//contentPane2.jlblPhase.setText("III");
							bBackCalculateTempProfileOffset = false;
							bBackCalculateTempSimpleProfile = false;
							bBackCalculateTempProfile = true;
							bBackCalculateHoldUpProfileOffset = false;
							bBackCalculateHoldUpProfile = true;
							dTempProfileAngleWeight = 0.25;
							dHoldUpProfileAngleWeight = 0.25;
							bSkipTerminationTest = true;
					    	tStep = plotXMax * 0.001;
						}
					}
					else if (iPhase == 3)
					{
						if (dPercentImprovement < 1 && dPercentImprovement >= 0)
						{
							iPhase = 7;
							//contentPane2.jlblPhase.setText("IV");
							bBackCalculateTempProfileOffset = false;
							bBackCalculateTempSimpleProfile = false;
							bBackCalculateTempProfile = true;
							bBackCalculateHoldUpProfileOffset = false;
							bBackCalculateHoldUpProfile = true;
							dTempProfileAngleWeight = 0.25;
							dHoldUpProfileAngleWeight = 0.25;
							bSkipTerminationTest = true;
					    	//m_dtstep = m_dPlotXMax2 * 0.0001;
					    	tStep = 0.005;
						}
					}
					else if (iPhase == 4)
					{
						if (dPercentImprovement < 2 && dPercentImprovement >= 0)
						{
							iPhase = 5;
							//contentPane2.jlblPhase.setText("V");
							bBackCalculateTempProfileOffset = false;
							bBackCalculateTempSimpleProfile = false;
							bBackCalculateTempProfile = true;
							bBackCalculateHoldUpProfileOffset = false;
							bBackCalculateHoldUpProfile = true;
							dTempProfileAngleWeight = .05;
							dHoldUpProfileAngleWeight = 0.25;
							bSkipTerminationTest = true;
						}
					}
					else if (iPhase == 5)
					{
						if (dPercentImprovement < 1 && dPercentImprovement >= 0)
						{
							iPhase = 6;
							//contentPane2.jlblPhase.setText("VI");
							bBackCalculateTempProfile = true;
							bBackCalculateHoldUpProfile = true;
							dTempProfileAngleWeight = 0.25;
							dHoldUpProfileAngleWeight = 0.25;
							bSkipTerminationTest = true;
						}
					}
					else if (iPhase == 6)
					{
						if (dPercentImprovement < 1 && dPercentImprovement >= 0)
						{
							bSkipTerminationTest = true;
							
							iPhase = 7;
							//contentPane2.jlblPhase.setText("VII");
							bBackCalculateTempProfile = true;
							bBackCalculateHoldUpProfile = true;
							dTempProfileAngleWeight = 0;
							dHoldUpProfileAngleWeight = 0.25;
					    	tStep = plotXMax * 0.001;//0.001;
						}					
					}
					else
					{
						if (dPercentImprovement < 1 && dPercentImprovement >= 0)
						{
							// Optimization is complete.
							break;
						}
					}
				}
				else
				{
					bSkipTerminationTest = false;
					updatePercentImprovement(0, false);
				}
				
			}
		}
    }

    // Projects retention for one compound
    // Input: dIsothermalDataArray (for one compound)
    public PredictedRetentionObject predictRetention(double[][] dIsothermalDataArray)
    {
    	PredictedRetentionObject pro = new PredictedRetentionObject();
    	
    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
    	double dBeta2 = Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2) / (Math.pow(innerDiameter / 2, 2) - Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2));
    	double dBeta1Beta2 = dBeta1 / dBeta2;

		InterpolationFunction IsocraticData = new InterpolationFunction(dIsothermalDataArray);
		
		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dXPosition = 0;
		double dXPositionError = 0;
		double dLastXPosition = 0;
		double dXMovement = 0;
		Boolean bIsEluted = false;
		double dTcA = 0;
		double dTcB = 0;
		double dCurVal = 0;
		
		// Grab the first temp
		dTcA = m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_InterpolatedTemperatureDifferenceProfile.getAt(0);

		for (double t = 0; t <= (Double) standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() * 1.5; t += tStep)
		{
			// Grab the second temp
			dTcB = m_InterpolatedSimpleTemperatureProfile.getAt(t + tStep) + m_InterpolatedTemperatureDifferenceProfile.getAt(t + tStep);
			
			// Find the average of the two temps
			double dTc = (dTcA + dTcB) / 2;
			
			// Get the hold-up time at this temp
			double dHc = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
			// Get the amount of dead time traveled in dtstep
			double dk = Math.pow(10, IsocraticData.getAt(dTc)) * dBeta1Beta2;
			dCurVal = tStep / (1 + dk);
			// Determine what fraction of the column it moved
			dXMovement = dCurVal / dHc;
			
			// Add that to the running total
			dLastXPosition = dXPosition;
			dXPosition += dXMovement;
			
			// Now calculate error
			// First, determine error in k from error in T
			//double dTError = 0.13; // sigma in deg C
			//double dkerror = Math.abs((Math.pow(10, IsocraticData.getAt(dTc + dTError)) * dBeta1Beta2) - dk);
			
			// Assume error in k from measurement itself is small.
			
			double dXMovementErrorFraction = (dk * 0.00565) / (1 + dk);
			//double dXMovementErrorFraction = (dkerror) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;
			
			/*if (dXPosition + dXPositionError >= 1 && !bSigmaEluted)
			{
				dtRErrorFinal = (((1 - dLastXPosition)/((dXPosition + dXPositionError) - dLastXPosition)) * m_dtstep) + t;
				bSigmaEluted = true;
			}*/			
			
			if (dXPosition >= 1)
			{
				dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * tStep) + t;
				//dtRErrorFinal = ((1 - dLastXPosition)/(dXPosition - dLastXPosition) * m_dtstep) + t;
				double dxdt = (dXPosition - dLastXPosition) / tStep;
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);		
				bIsEluted = true;
				break;
			}
			
			dTcA = dTcB;
		}
		
		if (bIsEluted)
		{
			pro.dPredictedRetentionTime = dtRFinal;
		}
		else
		{
			pro.dPredictedRetentionTime = -1.0;
		}

		// Now calculate final error in the projection
		pro.dPredictedErrorSigma = dtRErrorFinal - dtRFinal;

		return pro;
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
    
    // Projects retention for one compound
    // Input: dCompoundParams (for one compound)
    public PredictedRetentionObject predictRetention2(double[] dCompoundParams)
    {
    	PredictedRetentionObject pro = new PredictedRetentionObject();
    	
    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
    	double dBeta2 = Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2) / (Math.pow(innerDiameter / 2, 2) - Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2));
    	double dBeta1Beta2 = dBeta1 / dBeta2;

		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dXPosition = 0;
		double dXPositionError = 0;
		double dLastXPosition = 0;
		double dXMovement = 0;
		Boolean bIsEluted = false;
		double dTcA = 0;
		double dTcB = 0;
		double dCurVal = 0;
		
		// Grab the first temp
		dTcA = m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_InterpolatedTemperatureDifferenceProfile.getAt(0);

		for (double t = 0; t <= (Double) standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() * 1.5; t += tStep)
		{
			// Grab the second temp
			dTcB = m_InterpolatedSimpleTemperatureProfile.getAt(t + tStep) + m_InterpolatedTemperatureDifferenceProfile.getAt(t + tStep);
			
			// Find the average of the two temps
			double dTc = (dTcA + dTcB) / 2;
			
			// Get the hold-up time at this temp
			double dHc = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
			// Get the amount of dead time traveled in dtstep
			
			double dk = calckfromT(dCompoundParams, dTc) * dBeta1Beta2;
			dCurVal = tStep / (1 + dk);
			// Determine what fraction of the column it moved
			dXMovement = dCurVal / dHc;
			
			// Add that to the running total
			dLastXPosition = dXPosition;
			dXPosition += dXMovement;
			
			// Now calculate error
			// First, determine error in k from error in T
			//double dTError = 0.13; // sigma in deg C
			//double dkerror = Math.abs((Math.pow(10, IsocraticData.getAt(dTc + dTError)) * dBeta1Beta2) - dk);
			
			// Assume error in k from measurement itself is small.
			
			double dXMovementErrorFraction = (dk * 0.00565) / (1 + dk);
			//double dXMovementErrorFraction = (dkerror) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;
			
			/*if (dXPosition + dXPositionError >= 1 && !bSigmaEluted)
			{
				dtRErrorFinal = (((1 - dLastXPosition)/((dXPosition + dXPositionError) - dLastXPosition)) * m_dtstep) + t;
				bSigmaEluted = true;
			}*/			
			
			if (dXPosition >= 1)
			{
				dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * tStep) + t;
				//dtRErrorFinal = ((1 - dLastXPosition)/(dXPosition - dLastXPosition) * m_dtstep) + t;
				double dxdt = (dXPosition - dLastXPosition) / tStep;
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);		
				bIsEluted = true;
				break;
			}
			
			dTcA = dTcB;
		}
		
		if (bIsEluted)
		{
			pro.dPredictedRetentionTime = dtRFinal;
		}
		else
		{
			pro.dPredictedRetentionTime = -1.0;
		}

		// Now calculate final error in the projection
		pro.dPredictedErrorSigma = dtRErrorFinal - dtRFinal;

		return pro;
    }
    
/*    class TaskPredict extends Task
    {
            
        @Override
        public void done() 
        {
    		//contentPane2.jProgressBar2.setIndeterminate(false);
    		contentPane2.jProgressBar2.setStringPainted(true);
    		contentPane2.jProgressBar2.setString("Calculations complete. Now copy report to clipboard.");
    		contentPane2.jbtnPredict.setActionCommand("Copy to clipboard");
    		contentPane2.jbtnPredict.setText("Copy report to clipboard");
    		contentPane2.jbtnPredict.setEnabled(true);
        }

		@Override
		protected Object call() throws Exception 
		{
			int iNumCompounds = Globals.CompoundIsothermalDataArray.length;
    		
    		m_PredictedRetentionTimes = new PredictedRetentionObject[iNumCompounds];
    		contentPane2.jProgressBar2.setMinimum(0);
    		contentPane2.jProgressBar2.setMaximum(iNumCompounds - 1);
    		
    		for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
    		{
    			contentPane2.jProgressBar2.setString("Projecting compound " + Integer.toString(iCompound + 1) + " of " + Integer.toString(iNumCompounds));
        		contentPane2.jProgressBar2.setValue(iCompound);
    			
				if (taskPredict.isCancelled())
				{
					return null;
				}

				m_PredictedRetentionTimes[iCompound] = predictRetention(Globals.CompoundIsothermalDataArray[iCompound]);
    		}
    		
    		updatePredictionsTable();
    		
    		return null;
		}
    }*/
    

    public void calcSimpleTemperatureProgram(double dk, double dTmax)
 	{
		int iNumPoints = 1000;
		double dC = 0;
		
		double dFinalTime = this.plotXMax; // in min
		double dTimeStep = dFinalTime / (iNumPoints - 1);
		
		// Create an array for the simple temperature program
		m_dSimpleTemperatureProfileArray = new double[iNumPoints][2];
		
		// Set the first point.
		m_dSimpleTemperatureProfileArray[0][0] = 0;
		m_dSimpleTemperatureProfileArray[0][1] = this.m_dIdealTemperatureProfileArray[0][1];
				
		for (int i = 0; i < iNumPoints - 1; i++)
		{
			double dt1 = (double)i * dTimeStep;
			double dt2 = (double)(i + 1) * dTimeStep;
			
			double dT1 = m_dSimpleTemperatureProfileArray[i][1];
			double dT2 = this.m_InterpolatedIdealTempProfile.getAt(dt2);
			
			double dIdealSlope = (dT2 - dT1) / dTimeStep;
			
			double dt1b = -Math.log(1 - ((dT1 - dC) / dTmax)) / dk;
			double dt2b = dt1b + dTimeStep;
			
			double dT2b = dTmax * (1 - Math.exp(-dk * dt2b)) + dC;
			
			double dLimitingSlope = (dT2b - dT1) / dTimeStep;
			
			m_dSimpleTemperatureProfileArray[i + 1][0] = dt2;
			if (dLimitingSlope < dIdealSlope)
			{
				m_dSimpleTemperatureProfileArray[i + 1][1] = dT2b;
			}
			else
			{
				m_dSimpleTemperatureProfileArray[i + 1][1] = dT2;
			}
		}
		
		this.m_InterpolatedSimpleTemperatureProfile = new LinearInterpolationFunction(m_dSimpleTemperatureProfileArray);
 	}

	public void setBackCalculateControllerListener(BackCalculateControllerListener thisListener)
	{
		backCalculateControllerListener = thisListener;
	}

	@FXML private void onNextStepAction(ActionEvent e)
	{
		// Send notification back to the ParentPaneController to change the visible pane
		if (backCalculateControllerListener != null)
			this.backCalculateControllerListener.onNextStepPressed(this);
	}
	
	@FXML private void onPreviousStepAction(ActionEvent e)
	{
		if (task != null && task.isRunning())
			task.cancel();
		
		// Send notification back to the ParentPaneController to change the visible pane
		if (backCalculateControllerListener != null)
			this.backCalculateControllerListener.onPreviousStepPressed(this);
	}
	
	public void switchToStep4()
	{
		// Can't update the table until it's added somewhere.
		mainGrid.getChildren().remove(step3Pane);
		mainGrid.getChildren().remove(step4Pane);
		mainGrid.add(step4Pane, 1, 0);

		ObservableList<StandardCompound> testCompoundsList = stepFourPaneController.getTestCompoundList();
		
		// Predict retention times of test compounds here.
		m_dExpectedErrorArray = new double[Globals.TestCompoundNameArray.length];
		
		// Fill in the table with predicted retention times
    	for (int i = 0; i < Globals.TestCompoundNameArray.length; i++)
    	{
    		PredictedRetentionObject predictedRetention = predictRetention2(Globals.TestCompoundParamArray[i]);
    		//PredictedRetentionObject predictedRetention = predictRetention2(Globals.TestCompoundParamArray[i]);
    		m_dExpectedErrorArray[i] = predictedRetention.dPredictedErrorSigma;
    		
    		testCompoundsList.get(i).setPredictedRetentionTime(Globals.roundToSignificantFigures(predictedRetention.dPredictedRetentionTime, 5));
    	}
    	
    	this.updateTestCompoundTable();
		
		this.buttonNextStep.setVisible(false);
	}
	
	public void updateTestCompoundTable()
	{
		double dSumofSquares = 0;
		double dExpectedSumofSquares = 0;
		double dSumAbsolute = 0;

		ObservableList<StandardCompound> testCompoundsList = stepFourPaneController.getTestCompoundList();

		double[] dErrorList = new double[testCompoundsList.size()];
		
		NumberFormat formatter1 = new DecimalFormat("#0.000");
		NumberFormat formatter2 = new DecimalFormat("#0.0");

		int iTotalCompounds = 0;
		
		for (int i = 0; i < testCompoundsList.size(); i++)
		{
			double predictedRetentionTime = testCompoundsList.get(i).getPredictedRetentionTime();
			double measuredRetentionTimeValue = testCompoundsList.get(i).getMeasuredRetentionTime();

			double error = measuredRetentionTimeValue - predictedRetentionTime;
			
			if (measuredRetentionTimeValue > 0 && m_dExpectedErrorArray != null)
			{
				dSumofSquares += Math.pow(error, 2);
				dExpectedSumofSquares += Math.pow(this.m_dExpectedErrorArray[i], 2);
				dSumAbsolute += Math.abs(error);
				dErrorList[i] = Math.abs(error);
				iTotalCompounds++;
			}
			else
				dErrorList[i] = -1.0;
		}
		
		if (iTotalCompounds <= 1)
		{
			stepFourPaneController.overallErrorLabelProperty().set("-");
			stepFourPaneController.mostLikelyErrorLabelProperty().set("-");
			stepFourPaneController.columnRatingLabelProperty().set("-");
			stepFourPaneController.setSliderIndicatorVisible(false);
			this.status = INCOMPLETE;
		}
		else
		{
			double dStandardDeviation = Math.sqrt(dSumofSquares / (double)iTotalCompounds);
			double dExpectedStandardDeviation = Math.sqrt(dExpectedSumofSquares / (double)iTotalCompounds);
			ChiSquaredDistribution chidist = new ChiSquaredDistribution(iTotalCompounds);
			double d95Limit = Math.sqrt((chidist.inverseCumulativeProbability(.95) * Math.pow(dExpectedStandardDeviation, 2)) / iTotalCompounds);
			double d75Limit = Math.sqrt((chidist.inverseCumulativeProbability(.75) * Math.pow(dExpectedStandardDeviation, 2)) / iTotalCompounds);
			double dColumnRating = dStandardDeviation / d75Limit;
			double dYellowRating = d95Limit / d75Limit;
			
			stepFourPaneController.overallErrorLabelProperty().set("± " + Float.toString((float)Globals.roundToSignificantFigures(dStandardDeviation, 2)) + " min (" + Float.toString((float)Globals.roundToSignificantFigures(dStandardDeviation * 60, 2)) + " sec)");
			stepFourPaneController.mostLikelyErrorLabelProperty().set("± " + Float.toString((float)Globals.roundToSignificantFigures(dExpectedStandardDeviation, 2)) + " min (" + Float.toString((float)Globals.roundToSignificantFigures(dExpectedStandardDeviation * 60, 2)) + " sec)");
			stepFourPaneController.columnRatingLabelProperty().set(Float.toString((float)Globals.roundToSignificantFigures(dColumnRating, 2)));
			
			Color clrGreen = Color.rgb(0, 161, 75);
			Color clrYellow = Color.rgb(188, 174, 0);
			Color clrRed = Color.rgb(236, 28, 36);
			Color clrBlack = Color.rgb(51, 51, 51);
			
			if (dColumnRating <= 1.0)
			{
				stepFourPaneController.setRatingColor(clrGreen);
				this.status = PASSED;
			}
			else if (dColumnRating > 1.0 && dColumnRating <= dYellowRating)
			{
				stepFourPaneController.setRatingColor(clrYellow);
				this.status = PASSEDBUTQUESTIONABLE;
			}
			else if (dColumnRating > dYellowRating)
			{
				stepFourPaneController.setRatingColor(clrRed);
				this.status = FAILED;
			}
				
			
			stepFourPaneController.setSliderIndicatorVisible(true);
			stepFourPaneController.setSliderYellowLimit((float)dYellowRating);
			stepFourPaneController.setSliderPosition((float)(dColumnRating / 3.0) * 100);
			
			this.score = dColumnRating;
		}
	}
	
	public void switchToStep3()
	{
		mainGrid.getChildren().remove(step4Pane);
		mainGrid.getChildren().remove(step3Pane);
		mainGrid.add(step3Pane, 1, 0);		
		this.buttonNextStep.setVisible(true);
	}
	
	// Cancels the task and waits for it to finish
	public void cancelTasks()
	{
		if (this.task != null && task.isRunning())
			task.cancel();
		
		while(this.task != null && !this.task.isDone())
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean isThreadRunning()
	{
		if (this.task == null)
			return false;
		else
			return this.task.isRunning();
	}
	
	public void onTableUpdate() 
	{
		this.updateTestCompoundTable();
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public double getScore()
	{
		return score;
	}
	
	
	public void writeSaveData(SaveData.BackCalculateSaveData saveData)
	{
		saveData.initialTemperature = initialTemperature;
		saveData.initialHoldTime = initialHoldTime;
		saveData.columnLength = columnLength;
		saveData.innerDiameter = innerDiameter;
		saveData.filmThickness = filmThickness;
		saveData.flowRate = flowRate;
		saveData.inletPressure = inletPressure;
		saveData.outletPressure = outletPressure;
		saveData.constantFlowRateMode = constantFlowRateMode;
		saveData.vacuumOutletPressure = isUnderVacuum;
		saveData.fileName = fileName;
		saveData.standardsList = standardsList;
		saveData.temperatureProgramInConventionalForm = temperatureProgramInConventionalForm;
		saveData.tStep = tStep;
		saveData.m_dIdealTemperatureProfileArray = m_dIdealTemperatureProfileArray;
		saveData.m_dTemperatureProfileDifferenceArray = m_dTemperatureProfileDifferenceArray;
		saveData.m_dSimpleTemperatureProfileArray = m_dSimpleTemperatureProfileArray;
		saveData.m_dHoldUpArray = m_dHoldUpArray;
		saveData.m_dExpectedErrorArray = m_dExpectedErrorArray;
		saveData.status = status;
		saveData.score = score;
	    
	    // Step3Pane stuff
	    saveData.labelIterationText = stepThreePaneController.iterationLabelProperty().getValue();
	    saveData.labelVarianceText = stepThreePaneController.varianceLabelProperty().getValue();
	    saveData.labelLastIterationVarianceText = stepThreePaneController.lastIterationVarianceLabelProperty().getValue();
	    saveData.labelPercentImprovementText = stepThreePaneController.percentImprovementLabelProperty().getValue();
	    saveData.labelTimeElapsedText = stepThreePaneController.timeElapsedLabelProperty().getValue();
	    saveData.labelStatusText = stepThreePaneController.statusLabelProperty().getValue();
	    saveData.progressBarValue = stepThreePaneController.progressBarProperty().getValue();
	    saveData.backCalculationButtonDisabled = stepThreePaneController.isBackCalculationButtonDisabled();
	    
	    // Step4Pane stuff - most is updated with updateTestCompoundTable()
	    saveData.testCompoundList = stepFourPaneController.getTestCompoundList();
	}
	
	public void loadSaveData(SaveData.BackCalculateSaveData saveData)
	{
		initialTemperature = saveData.initialTemperature;
		initialHoldTime = saveData.initialHoldTime;
		columnLength = saveData.columnLength;
		innerDiameter = saveData.innerDiameter;
		filmThickness = saveData.filmThickness;
		flowRate = saveData.flowRate;
		inletPressure = saveData.inletPressure;
		outletPressure = saveData.outletPressure;
		constantFlowRateMode = saveData.constantFlowRateMode;
		isUnderVacuum = saveData.vacuumOutletPressure;
		fileName = saveData.fileName;
		standardsList = saveData.standardsList;
		temperatureProgramInConventionalForm = saveData.temperatureProgramInConventionalForm;
		tStep = saveData.tStep;
		
		m_dIdealTemperatureProfileArray = saveData.m_dIdealTemperatureProfileArray;
		if (m_dIdealTemperatureProfileArray != null)
			this.m_InterpolatedIdealTempProfile = new LinearInterpolationFunction(this.m_dIdealTemperatureProfileArray);
		
    	m_dTemperatureProfileDifferenceArray = saveData.m_dTemperatureProfileDifferenceArray;
		if (m_dTemperatureProfileDifferenceArray != null)
			this.m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
		
		m_dSimpleTemperatureProfileArray = saveData.m_dSimpleTemperatureProfileArray;
		if (m_dSimpleTemperatureProfileArray != null)
			this.m_InterpolatedSimpleTemperatureProfile = new LinearInterpolationFunction(m_dSimpleTemperatureProfileArray);

		m_dHoldUpArray = saveData.m_dHoldUpArray;
		if (m_dHoldUpArray != null)
			this.m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);

		m_dExpectedErrorArray = saveData.m_dExpectedErrorArray;
		status = saveData.status;
		score = saveData.score;
		
	    // Step3Pane stuff
		stepThreePaneController.iterationLabelProperty().unbind();
		stepThreePaneController.iterationLabelProperty().setValue(saveData.labelIterationText);
		stepThreePaneController.varianceLabelProperty().unbind();
		stepThreePaneController.varianceLabelProperty().setValue(saveData.labelVarianceText);
		stepThreePaneController.lastIterationVarianceLabelProperty().unbind();
		stepThreePaneController.lastIterationVarianceLabelProperty().setValue(saveData.labelLastIterationVarianceText);
		stepThreePaneController.percentImprovementLabelProperty().unbind();
		stepThreePaneController.percentImprovementLabelProperty().setValue(saveData.labelPercentImprovementText);
		stepThreePaneController.timeElapsedLabelProperty().unbind();
		stepThreePaneController.timeElapsedLabelProperty().setValue(saveData.labelTimeElapsedText);
		stepThreePaneController.statusLabelProperty().unbind();
		stepThreePaneController.statusLabelProperty().setValue(saveData.labelStatusText);
		stepThreePaneController.progressBarProperty().unbind();
		stepThreePaneController.progressBarProperty().setValue(saveData.progressBarValue);
		stepThreePaneController.setBackCalculationButtonDisable(saveData.backCalculationButtonDisabled);

		// Step4Pane stuff
		stepFourPaneController.setTestCompoundList(saveData.testCompoundList);

		this.buttonNextStep.setDisable(!saveData.backCalculationButtonDisabled);
		stepThreePaneController.setStandardCompoundList(standardsList);

		if (m_InterpolatedSimpleTemperatureProfile != null && m_dIdealTemperatureProfileArray != null)
		{
			updateGraphsWithIdealProfiles();
			updateGraphs(this.m_InterpolatedSimpleTemperatureProfile, this.m_InterpolatedTemperatureDifferenceProfile, this.m_dTemperatureProfileDifferenceArray, this.m_InterpolatedHoldUpProfile, this.m_dHoldUpArray);
	    	updateTestCompoundTable();
		}
	}
}
