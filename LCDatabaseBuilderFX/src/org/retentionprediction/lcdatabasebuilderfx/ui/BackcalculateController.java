package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JOptionPane;

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
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;
import org.retentionprediction.lcdatabasebuilderfx.ui.StepFourPaneController.StepFourPaneControllerListener;
import org.retentionprediction.lcdatabasebuilderfx.business.*;

import boswell.fxoptionpane.FXOptionPane;
import boswell.graphcontrolfx.GraphControlFX;
import boswell.peakfinderlcfx.PeakFinderLCFX;

public class BackcalculateController implements Initializable, StepFourPaneControllerListener{
	
	class PredictedRetentionObject
	{
		double dPredictedRetentionTime = 0;
		double dPredictedErrorSigma = 0;
	}

	class Compound
	{
		String strCompoundName;
		int iCompoundIndex;

		public int getCompoundNum()
		{
			return Globals.StandardCompoundsNameArray.length;
		}
		
		public boolean loadCompoundInfo(int iIndex)
		{
			iCompoundIndex = iIndex;
			
			strCompoundName = Globals.StandardCompoundsNameArray[iIndex];
			
			return true;
		}
	}

	public BackCalculateControllerListener backcalculateControllerListener;
	private TitledPane step3Pane;
	private StepThreePaneController stepThreePaneController;
	private TitledPane step4Pane;
	private StepFourPaneController stepFourPaneController;
	private GraphControlFX eluentCompositionTimeGraph;
	private GraphControlFX deadTimeEluentCompositionGraph;
	private final double rem = javafx.scene.text.Font.getDefault().getSize();
	
	@FXML AnchorPane anchorPaneEluentCompositionTime;
	@FXML AnchorPane anchorPaneDeadTimeEluentComposition;
	
	public double[][] gradientProgram;
    public double[][] idealGradientProfileArray;
    public LinearInterpolationFunction interpolatedIdealGradientProfile; //Linear
    public double[][] gradientProfileDifferenceArray;
    public LinearInterpolationFunction interpolatedGradientDifferenceProfile;

    public double[][] initialDeadTimeArray;
    public InterpolationFunction initialInterpolatedDeadTimeProfile;
    public double[][] deadTimeDifferenceArray;
    public InterpolationFunction interpolatedDeadTimeDifferenceProfile;
    
    public InterpolationFunction[] standardIsocraticDataInterpolated;

    // For the mixing/non-mixing volume
	public double[][] simpleGradientArray;
	public LinearInterpolationFunction interpolatedSimpleGradient = null;
	public LinearInterpolationFunction interpolatedSimpleDeadTime = null;
	
    public int interpolatedGradientProgramSeries = 0;
    public int gradientProgramMarkerSeries = 0;
    public int interpolatedFlowRateSeries = 0;
    public int flowRateMarkerSeries = 0;
    public int simpleGradientSeries = 0;
    public int simpleDeadTimeSeries = 0;
    
    // Mixing volume in mL
    //public double m_dMixingVolume = 0.001;
    //public double m_dNonMixingVolume = 0.001;
    
    // Mixing volume as a function of solvent composition
    public double[][] mixingVolumeArray = {{0.0, 0.001}, {100.0, 0.001}};
    // Non-mixing volume as a function of solvent composition
    public double[][] nonMixingVolumeArray = {{0.0, 0.001}, {100.0, 0.001}};
    public LinearInterpolationFunction interpolatedMixingVolume = null;
    public LinearInterpolationFunction interpolatedNonMixingVolume = null;
    
    public double plotXMax = 0;
    public double plotXMax2 = 0;
    
    public boolean doNotChangeTable = false;
    public final double goldenRatio = (1 + Math.sqrt(5)) / 2;
    public boolean showSimpleGradient = false;
    public boolean showSimpleDeadTime = false;

    public double instrumentDeadTime = 0.125; // in min

    public double[] expectedErrorArray;
    public PredictedRetentionObject[] predictedRetentionTimes;
    public double confidenceInterval = 0.99;
	
    public int stage = 1;
    public double dtstep = 0.01;
    public double variance = 0;
    public boolean noFullBackcalculation = false;
    

	public int stationaryPhase = 0;
	public double columnLength = 100; // in mm
	public double innerDiameter = 2.1; // in mm
	public double programTime = 20;
	public double flowRate = 0.4; // in mL/min
	
	private String fileName = "";
	
	// To be acquired from the Step3Pane.
	private ObservableList<StandardCompound> standardsList = FXCollections.observableArrayList();
	@FXML private Node buttonNextStep;
	@FXML private GridPane mainGrid;
	private BackcalculateTask task = null;
		
    public final int INCOMPLETE = 0;
    public final int PASSED = 1;
    public final int PASSEDBUTQUESTIONABLE = 2;
    public final int FAILED = 3;
    
    private int status = 0;
    private double score = 0;
	private Window parentWindow;
	private int m_iIdealPlotIndexGrad = -1;
	private int m_iIdealPlotIndexDeadTime = -1;
    
	private boolean isInjectionMode = false;
	
	public BackCalculateControllerListener getBackcalculateListener() {
		return backcalculateControllerListener;
	}

	public void setBackCalculateControllerListener(BackCalculateControllerListener thisListener)
	{
		backcalculateControllerListener = thisListener;
	}

	public double getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(double columnLength) {
		this.columnLength = columnLength;
	}

	public double getInnerDiameter() {
		return innerDiameter;
	}

	public void setInnerDiameter(double innerDiameter) {
		this.innerDiameter = innerDiameter;
	}

	public double getFlowRate() {
		return flowRate;
	}

	public void setFlowRate(double flowRate) {
		this.flowRate = flowRate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
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

			eluentCompositionTimeGraph = new GraphControlFX();
			eluentCompositionTimeGraph.setControlsEnabled(false);
			eluentCompositionTimeGraph.setYAxisTitle("Eluent Composition");
			eluentCompositionTimeGraph.setYAxisBaseUnit("%B", "%B");
			eluentCompositionTimeGraph.setYAxisScientificNotation(true);
			eluentCompositionTimeGraph.setYAxisRangeIndicatorsVisible(true);
			eluentCompositionTimeGraph.setAutoScaleY(true);
			
			eluentCompositionTimeGraph.setXAxisType(true);
			eluentCompositionTimeGraph.setXAxisRangeIndicatorsVisible(true);
			eluentCompositionTimeGraph.setAutoScaleX(true);
			eluentCompositionTimeGraph.setSelectionCursorVisible(false);
			anchorPaneEluentCompositionTime.getChildren().add(eluentCompositionTimeGraph);
			AnchorPane.setTopAnchor(eluentCompositionTimeGraph, 0.0);
			AnchorPane.setBottomAnchor(eluentCompositionTimeGraph, 0.0);
			AnchorPane.setLeftAnchor(eluentCompositionTimeGraph, 0.0);
			AnchorPane.setRightAnchor(eluentCompositionTimeGraph, 0.0);
			
			eluentCompositionTimeGraph.widthProperty().bind(anchorPaneEluentCompositionTime.widthProperty().subtract(rem));
			eluentCompositionTimeGraph.heightProperty().bind(anchorPaneEluentCompositionTime.heightProperty().subtract(rem));

			deadTimeEluentCompositionGraph = new GraphControlFX();
			deadTimeEluentCompositionGraph.setControlsEnabled(false);
			deadTimeEluentCompositionGraph.setYAxisTitle("Uracil Dead Time");
			deadTimeEluentCompositionGraph.setYAxisBaseUnit("seconds", "s");
			//holdUpProfileGraph.setYAxisRangeLimits(-1E15d, 1E15d);
			deadTimeEluentCompositionGraph.setYAxisScientificNotation(true);
			deadTimeEluentCompositionGraph.setYAxisRangeIndicatorsVisible(false);
			deadTimeEluentCompositionGraph.setAutoScaleY(true);
	        
			deadTimeEluentCompositionGraph.setXAxisType(false);
			deadTimeEluentCompositionGraph.setXAxisRangeIndicatorsVisible(false);
			deadTimeEluentCompositionGraph.setXAxisTitle("Eluent Composition");
			deadTimeEluentCompositionGraph.setXAxisBaseUnit("%B", "%B");
			deadTimeEluentCompositionGraph.setAutoScaleX(true);
			deadTimeEluentCompositionGraph.setSelectionCursorVisible(false);
			
			anchorPaneDeadTimeEluentComposition.getChildren().add(deadTimeEluentCompositionGraph);
			AnchorPane.setTopAnchor(deadTimeEluentCompositionGraph, 0.0);
			AnchorPane.setBottomAnchor(deadTimeEluentCompositionGraph, 0.0);
			AnchorPane.setLeftAnchor(deadTimeEluentCompositionGraph, 0.0);
			AnchorPane.setRightAnchor(deadTimeEluentCompositionGraph, 0.0);
			
			deadTimeEluentCompositionGraph.widthProperty().bind(anchorPaneDeadTimeEluentComposition.widthProperty().subtract(rem));
			deadTimeEluentCompositionGraph.heightProperty().bind(anchorPaneDeadTimeEluentComposition.heightProperty().subtract(rem));
			
			resetValues();
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void onFindRetentionTimesAutomaticallyButtonMouseClicked(
			MouseEvent event) {
		PeakFinderLCFX peakfinder = new PeakFinderLCFX(parentWindow, Globals.StationaryPhaseArray, false);
		peakfinder.setColumnLength(columnLength);
		peakfinder.setFileName(fileName);
		peakfinder.setFlowRate(flowRate);
		peakfinder.setInstrumentDeadTime(instrumentDeadTime);
		peakfinder.setInnerDiameter(innerDiameter);
		peakfinder.setStandardCompoundMZData(Globals.TestCompoundMZArray);
		peakfinder.setStandardCompoundNames(Globals.TestCompoundNameArray);
		peakfinder.setIsocraticDataArray(Globals.TestCompoundsIsocraticDataArray);
		//TODO: might need to add more parameters here
		double[][] combinedGradientProfileArray = new double[simpleGradientArray.length][2];
		for(int i = 0; i < simpleGradientArray.length; i++){
			combinedGradientProfileArray[i][0] = simpleGradientArray[i][0];
			combinedGradientProfileArray[i][1] = simpleGradientArray[i][1] + this.interpolatedGradientDifferenceProfile.getAt(simpleGradientArray[i][0]);
		}
		peakfinder.setInterpolatedGradientProfile(combinedGradientProfileArray);
		peakfinder.setGradientProgramInConventionalForm(gradientProgram);
		// Now set the back-calculated dead time profile
    	int iNumPoints = 1000;
    	double[][] dCombinedDeadTimeProfileArray = new double[iNumPoints][2];
    	double dStartPhi = initialDeadTimeArray[0][0];
    	double dEndPhi = initialDeadTimeArray[initialDeadTimeArray.length - 1][0];
    	for (int i = 0; i < iNumPoints; i++)
    	{
    		double dCurrentPhi = (((dEndPhi - dStartPhi) / (double)(iNumPoints - 1)) * (double)i) + dStartPhi;
    		dCombinedDeadTimeProfileArray[i][0] = dCurrentPhi;
    		dCombinedDeadTimeProfileArray[i][1] = initialInterpolatedDeadTimeProfile.getAt(dCurrentPhi) + interpolatedDeadTimeDifferenceProfile.getAt(dCurrentPhi);
    	}
    	peakfinder.setInterpolatedDeadTime(dCombinedDeadTimeProfileArray);
    	
		
		peakfinder.setTStep(dtstep);
		peakfinder.run();
		
		if (peakfinder.getOkPressed())
    	{
    		double[] dRetentionTimes = peakfinder.getSelectedRetentionTimes();
    		boolean[] bSkippedStandards = peakfinder.getSkippedStandards();
    		int[] iPeakRank = peakfinder.getSelectedPeakRank();
    		
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
    					testCompoundList.get(i).setMeasuredRetentionTime(0.0);
    			}
    			else
    			{
    				// If the peak wasn't picked, then skip this one.
    				testCompoundList.get(i).setMeasuredRetentionTime(0.0);    				
	    			testCompoundList.get(i).setUse(false);
    			}
    		}	
    	}
		fileName = peakfinder.getFileName();
		updateTestCompoundTable();
	}

	public interface BackCalculateControllerListener 
	{
		public void onNextStepPressed(BackcalculateController thisController);
		public void onPreviousStepPressed(BackcalculateController thisController);
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
//TODO: Check the problem with gradientDifferenceProfileArray
		int iTotalCompounds = 0;
		
		for (int i = 0; i < testCompoundsList.size(); i++)
		{
			double predictedRetentionTime = testCompoundsList.get(i).getPredictedRetentionTime();
			double measuredRetentionTimeValue = testCompoundsList.get(i).getMeasuredRetentionTime();

			double error = measuredRetentionTimeValue - predictedRetentionTime;
			
			if (measuredRetentionTimeValue > 0 && expectedErrorArray != null)
			{
				dSumofSquares += Math.pow(error, 2);
				dExpectedSumofSquares += Math.pow(this.expectedErrorArray[i], 2);
				dSumAbsolute += Math.abs(error);
				//System.out.println(i+","+error+","+dSumofSquares+","+dExpectedSumofSquares+","+dSumAbsolute);
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
			this.setStatus(INCOMPLETE);
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
				this.setStatus(PASSED);
			}
			else if (dColumnRating > 1.0 && dColumnRating <= dYellowRating)
			{
				stepFourPaneController.setRatingColor(clrYellow);
				this.setStatus(PASSEDBUTQUESTIONABLE);
			}
			else if (dColumnRating > dYellowRating)
			{
				stepFourPaneController.setRatingColor(clrRed);
				this.setStatus(FAILED);
			}
				
			
			stepFourPaneController.setSliderIndicatorVisible(true);
			stepFourPaneController.setSliderYellowLimit((float)dYellowRating);
			stepFourPaneController.setSliderPosition((float)(dColumnRating / 3.0) * 100);
			
			this.setScore(dColumnRating);
		}
	}
	
	@Override
	public void onTableUpdate() {
		this.updateTestCompoundTable();
		
	}

	public void onBackCalculateButtonMouseClicked(MouseEvent event)
	{
		beginBackCalculation(true);
	}
	
	public void resetValues() {
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
		boolean isNextDisabled = false;
		if(standardsList.isEmpty()){
			isNextDisabled = true;
		}
		else{
			for(int i = 0; i < standardsList.size(); i++){
				if(standardsList.get(i).getPredictedRetentionTime() == 0.0){
					isNextDisabled = true;
					break;
				}
			}
		}
		buttonNextStep.setDisable(isNextDisabled);
		eluentCompositionTimeGraph.RemoveAllSeries();
		deadTimeEluentCompositionGraph.RemoveAllSeries();

		// Add the step3Pane to start
		mainGrid.getChildren().removeAll(step3Pane, step4Pane);
		mainGrid.add(step3Pane, 1, 0);
	}

    // Projects retention for one compound
    // Input: dIsocraticDataArray (for one compound)
    public PredictedRetentionObject predictRetention(double[][] dIsocraticDataArray)
    {
    	PredictedRetentionObject pro = new PredictedRetentionObject();
    	
		InterpolationFunction IsocraticData = new InterpolationFunction(dIsocraticDataArray);

		double dIntegral = 0;
		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dD = 0;
		double dTotalTime = 0;
		double dTotalDeadTime = 0;
		double dXPosition = 0;
		double dXPositionError = 0;
		double[] dLastXPosition = {0,0};
		double[] dLastko = {0,0};
		double dXMovement = 0;
		Boolean bIsEluted = false;
		double dPhiC = 0;
		double dCurVal = 0;
		double dk = 0;
		double dt0 = 0;
		
		for (double t = 0; t <= (Double) standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() * 1.5; t += dtstep)
		{
			dPhiC = (interpolatedSimpleGradient.getAt(dTotalTime - dIntegral) + interpolatedGradientDifferenceProfile.getAt(dTotalTime - dIntegral)) / 100;

			dk = Math.pow(10, IsocraticData.getAt(dPhiC));
			dCurVal = dtstep / dk;
			dt0 = (initialInterpolatedDeadTimeProfile.getAt(dPhiC) + interpolatedDeadTimeDifferenceProfile.getAt(dPhiC)) / 60;
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
				double dxdt = (dXPosition - dLastXPosition[0]) / dtstep;
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);	
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
			
			// Add error to position
			double dXMovementErrorFraction = (dk * 0.03) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;
		}
		
		if (bIsEluted)
		{
			pro.dPredictedRetentionTime = dtRFinal + instrumentDeadTime;
		}
		else
		{
			pro.dPredictedRetentionTime = -1.0;
		}
		
		// Now calculate final error in the projection
		pro.dPredictedErrorSigma = dtRErrorFinal - dtRFinal;

		return pro;
    }

    public PredictedRetentionObject predictRetentionFromLogKPhiRelationship(double a0, double a1, double a2, double b1, double b2, double injectionTime)
    {
    	PredictedRetentionObject pro = new PredictedRetentionObject();

		double dIntegral = 0;
		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dD = 0;
		double dTotalTime = injectionTime;
		double dTotalDeadTime = 0;
		double dXPosition = 0;
		double dXPositionError = 0;
		double[] dLastXPosition = {0,0};
		double[] dLastko = {0,0};
		double dXMovement = 0;
		Boolean bIsEluted = false;
		double dPhiC = 0;
		double dCurVal = 0;
		double dk = 0;
		double dt0 = 0;
		for (double t = injectionTime; t <= (Double) standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() * 1.5; t += dtstep)
		{
//			if(dIntegral < 0.001 && dIntegral != 0.0){
//				break;
//			}
 			dPhiC = (interpolatedSimpleGradient.getAt(dTotalTime - dIntegral) + interpolatedGradientDifferenceProfile.getAt(dTotalTime - dIntegral)) / 100;
 			double padeNumerator = (a0 + a1*dPhiC + a2*dPhiC*dPhiC);
 			double padeDenominator = (1 + b1*dPhiC + b2*dPhiC*dPhiC);
 			
 			if(padeDenominator == 0){
 				break;
 			}
 			
 			
// 			double slope = (padeDenominator*(a1 + 2*a2*dPhiC) - padeNumerator*(b1 + 2*b2*dPhiC))/(padeDenominator*padeDenominator);
// 			if(slope > 0){
// 				break;
// 			}
// 			
			double logk = padeNumerator/padeDenominator; //Pade's approximate
			if(logk < -3){
				logk = -3;
			}
			dk = Math.pow(10, logk);
			dCurVal = dtstep / dk;
			dt0 = (initialInterpolatedDeadTimeProfile.getAt(dPhiC) + interpolatedDeadTimeDifferenceProfile.getAt(dPhiC)) / 60;
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
				if(Double.isNaN(dtRFinal)){
					System.out.println("NaN");
				}
				double dxdt = (dXPosition - dLastXPosition[0]) / dtstep;
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);	
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
			
			// Add error to position
			double dXMovementErrorFraction = (dk * 0.03) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;
		}
		
		if (bIsEluted)
		{
			pro.dPredictedRetentionTime = dtRFinal;// + instrumentDeadTime;
		}
		else
		{
			pro.dPredictedRetentionTime = -1.0;
		}
		// Now calculate final error in the projection
		pro.dPredictedErrorSigma = dtRErrorFinal - dtRFinal;

		return pro;
    }
	
	public void switchToStep4() {
		// Can't update the table until it's added somewhere.
				mainGrid.getChildren().remove(step3Pane);
				mainGrid.getChildren().remove(step4Pane);
				mainGrid.add(step4Pane, 1, 0);

				ObservableList<StandardCompound> testCompoundsList = stepFourPaneController.getTestCompoundList();
				
				// Predict retention times of test compounds here.
				expectedErrorArray = new double[Globals.TestCompoundNameArray.length];
				
				// Fill in the table with predicted retention times
		    	for (int i = 0; i < Globals.TestCompoundNameArray.length; i++)
		    	{
		    		PredictedRetentionObject predictedRetention = predictRetention(Globals.TestCompoundsIsocraticDataArray[i]);
		    		//PredictedRetentionObject predictedRetention = predictRetention2(Globals.TestCompoundParamArray[i]);
		    		expectedErrorArray[i] = predictedRetention.dPredictedErrorSigma;
		    		
		    		testCompoundsList.get(i).setPredictedRetentionTime(Globals.roundToSignificantFigures(predictedRetention.dPredictedRetentionTime, 5));
		    	}
		    	
		    	this.updateTestCompoundTable();
				
				this.buttonNextStep.setVisible(false);
		
	}

	public void switchToStep3() {
		mainGrid.getChildren().remove(step4Pane);
		mainGrid.getChildren().remove(step3Pane);
		mainGrid.add(step3Pane, 1, 0);		
		this.buttonNextStep.setVisible(true);
	}
	
	public void beginBackCalculation(boolean bFlowRateProfileBackCalculationFirst)
	{
		this.stepThreePaneController.setBackCalculationButtonDisable(true);
		
       	task = new BackcalculateTask();
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
	
	@FXML private void onNextStepAction(ActionEvent e)
	{
		// Send notification back to the ParentPaneController to change the visible pane
		if (backcalculateControllerListener != null)
			this.backcalculateControllerListener.onNextStepPressed(this);
	}
	
	@FXML private void onPreviousStepAction(ActionEvent e)
	{
		if (task  != null && task.isRunning())
			task.cancel();
		
		// Send notification back to the ParentPaneController to change the visible pane
		if (backcalculateControllerListener != null)
			this.backcalculateControllerListener.onPreviousStepPressed(this);
	}
	
	class BackcalculateTask extends Task{

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
    			StandardCompound newStandardCompound = new StandardCompound(standardsList.get(i).getUse(), standardsList.get(i).getName(), standardsList.get(i).getMz(), standardsList.get(i).getMeasuredRetentionTime()-instrumentDeadTime, standardsList.get(i).getPredictedRetentionTime(), standardsList.get(i).getIndex());
    			this.standardsList.add(newStandardCompound);
    		}
    	}
    	
    	public void setOptimizationOrder(boolean bFlowRateProfileBackCalculationFirst)
    	{
    		this.bFlowRateProfileBackCalculationFirst = bFlowRateProfileBackCalculationFirst;
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
    			StandardCompound newStandardCompound = new StandardCompound(standardsList.get(i).getUse(), standardsList.get(i).getName(), standardsList.get(i).getMz(), standardsList.get(i).getMeasuredRetentionTime()+instrumentDeadTime, standardsList.get(i).getPredictedRetentionTime()+instrumentDeadTime, standardsList.get(i).getIndex());
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
    	
        @Override
        public void done() 
        {
        	if (!this.isCancelled())
        	{
	        	double[] dErrors = new double[standardsList.size()];
	        	for (int i = 0; i < standardsList.size(); i++)
	        	{
	        		dErrors[i] = Math.abs((Double)standardsList.get(i).getMeasuredRetentionTime() - (Double)standardsList.get(i).getPredictedRetentionTime());
	        	}
	        	Arrays.sort(dErrors);
	        	double dMedianError = dErrors[(int)(standardsList.size() / 2)];
	        	
	        	// Now find the compound with the maximum error
	        	int iCompoundWithMaxError = 0;
	        	for (int i = 1; i < standardsList.size(); i++)
	        	{
	        		double dMaxError = Math.abs((Double)standardsList.get(iCompoundWithMaxError).getMeasuredRetentionTime() - (Double)standardsList.get(iCompoundWithMaxError).getPredictedRetentionTime());
	        		double dThisError = Math.abs((Double)standardsList.get(i).getMeasuredRetentionTime() - (Double)standardsList.get(i).getPredictedRetentionTime());
	        		
	        		if (dThisError > dMaxError)
	        			iCompoundWithMaxError = i;
	        	}
	        	
	        	// Determine the ratio of the max error to the median
	    		double dMaxError = Math.abs((Double)standardsList.get(iCompoundWithMaxError).getMeasuredRetentionTime() - (Double)standardsList.get(iCompoundWithMaxError).getPredictedRetentionTime());
	        	double dRatio = dMaxError / dMedianError;
	        	final int compoundWithMaxError = iCompoundWithMaxError;
	        	if (dRatio > 8 && !noFullBackcalculation)
	        	{
	        		// Report a problem with the retention time for the compound
	    			Platform.runLater(new Runnable(){
	    	            @Override
	    	            public void run() {
	    	            	String strMessage = "Please double-check the retention time you reported for alkane " + standardsList.get(compoundWithMaxError).getName() + ". It does not seem to be correct.\n\nTo correct the retention time you entered, click \"Previous Step\", change the retention time, and re-run the back-calculation.\nOtherwise, if you are sure that the retention time is correct, continue to the next step.";
	    	    			FXOptionPane.showMessageDialog(mainGrid.getScene().getWindow() , strMessage, "Check Retention Times", FXOptionPane.WARNING_MESSAGE);
	    	            }
	    	        });	
	        		updateMessage("Optimization complete, but errors may exist.");        		
	        	}
	        	else if (variance > 0.0001 && !noFullBackcalculation)
	        	{
	        		// Report a problem with the retention time for the compound
	    			Platform.runLater(new Runnable(){
	    	            @Override
	    	            public void run() {
	    	            	String strMessage = "Your variance is high. This usually indicates that more than one of the alkane retention times you entered are incorrect.\nPlease double-check all of the alkane retention times you entered.\n\nTo correct the retention times, click \"Previous Step\", change the retention times, and re-run the back-calculation.\nOtherwise, if you are sure that the retention times are all correct, continue to the next step.";
	    	        		FXOptionPane.showMessageDialog(mainGrid.getScene().getWindow(), strMessage, "High Variance", FXOptionPane.WARNING_MESSAGE);
	    	            }
	    	        });	
	        		updateMessage("Optimization complete, but errors may exist.");        		
	        	}
	        	else
	        	{
	        		updateMessage("Optimization complete! Continue to next step.");        		
	        	}
	        	updateProgress(100, 100);
	    		//contentPane2.jbtnCalculate.setText("Copy profiles to clipboard");
	    		//contentPane2.jbtnCalculate.setActionCommand("Copy to clipboard");
	    		//contentPane2.jbtnCalculate.setEnabled(true);
	    		buttonNextStep.setDisable(false);	    		
        	}
        }
    	
		@Override
		protected Object call() throws Exception {
			backCalculate(bFlowRateProfileBackCalculationFirst);
			return null;
		}
		
		   public double calcRetentionError(double dtstep, int iNumCompoundsToInclude, boolean bUseSimpleGradient)
		    {
		    	double dRetentionError = 0;
				double dt0;
				double dIntegral;
				double dtRFinal;
				double dD;
				double dTotalTime;
				double dTotalDeadTime;
				double dXPosition;
				double[] dLastXPosition = {0,0};
				double[] dLastko = {0,0};
				double dXMovement = 0;
				double dPhiC = 0;
				double dCurVal = 0;
				boolean bIsEluted;

				for (int iCompound = 0; iCompound < iNumCompoundsToInclude; iCompound++)
				{
					dIntegral = 0;
					dtRFinal = 0;
					dD = 0;
					dTotalTime = 0;
					dTotalDeadTime = 0;
					dXPosition = 0;
					dLastXPosition[0] = 0;
					dLastXPosition[1] = 0;
					dLastko[0] = 0;
					dLastko[1] = 0;
					bIsEluted = false;
					
					for (double t = 0; t <= (Double) standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() * 1.5; t += dtstep)
					{
						if (bUseSimpleGradient)
							dPhiC = interpolatedSimpleGradient.getAt(dTotalTime - dIntegral) / 100;
						else
							dPhiC = (interpolatedSimpleGradient.getAt(dTotalTime - dIntegral) + interpolatedGradientDifferenceProfile.getAt(dTotalTime - dIntegral)) / 100;

						dCurVal = dtstep / (Math.pow(10, standardIsocraticDataInterpolated[iCompound].getAt(dPhiC)));
						dt0 = (initialInterpolatedDeadTimeProfile.getAt(dPhiC) + interpolatedDeadTimeDifferenceProfile.getAt(dPhiC)) / 60;
						dXMovement = dCurVal / dt0;
						
						if (dXPosition >= 1)
						{
							// ((1 - lastx)/(x - lastx)) gives fraction of the last step that should be considered
							// multiply that by (dTotalDeadTime - dLastXPosition[1]) to get the fraction of time in the last step that should be considered.
							// add that to dLastXPosition[1] to get the total dead time
							// dD is the total dead time at time of elution
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
						dRetentionError += Math.pow(dtRFinal - (Double)standardsList.get(iCompound).getMeasuredRetentionTime(), 2);
						standardsList.get(iCompound).setPredictedRetentionTime(dtRFinal);
					}
					else
					{
						dRetentionError += Math.pow((Double)standardsList.get(iCompound).getMeasuredRetentionTime(), 2);
						standardsList.get(iCompound).setPredictedRetentionTime((double)-1.0);
					}
				}
				
		    	return dRetentionError;
		    }

		
	    public double calcAngleDifferenceDeadTime(int iIndex)
	    {
	    	double dTotalAngleError = 0;
	    	double dHoldUpRange = initialDeadTimeArray[0][1];
	    	
	    	for (int i = 0; i < deadTimeDifferenceArray.length; i++)
	    	{
	        	if (i < 2)
	        		continue;
	        	
	        	double dTime2 = deadTimeDifferenceArray[i][0];
	        	double dHoldUp2 = deadTimeDifferenceArray[i][1];
	        	double dTime1 = deadTimeDifferenceArray[i - 1][0];
	        	double dHoldUp1 = deadTimeDifferenceArray[i - 1][1];
	        	double dTime0 = deadTimeDifferenceArray[i - 2][0];
	        	double dHoldUp0 = deadTimeDifferenceArray[i - 2][1];
	        	
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
	    	
	     	return dTotalAngleError + 1;
	    }
	    
	    public double calcAngleDifferenceGradient(int iIndex)
	    {
	    	//if (true)
	    	//return 0;
	    	double dTotalAngleError = 0;
	    	double dMaxRampRate = 50;
	    	
	    	for (int i = 0; i < gradientProfileDifferenceArray.length; i++)
	    	{
	        	if (i < 2)
	        		continue;
	        	
	        	double dTime2 = gradientProfileDifferenceArray[i][0];
	        	double dTemp2 = gradientProfileDifferenceArray[i][1];
	        	double dTime1 = gradientProfileDifferenceArray[i - 1][0];
	        	double dTemp1 = gradientProfileDifferenceArray[i - 1][1];
	        	double dTime0 = gradientProfileDifferenceArray[i - 2][0];
	        	double dTemp0 = gradientProfileDifferenceArray[i - 2][1];
	        	
	        	// Check if the previous point is a corner
	        	// If it is, then don't worry about the angle - return 0
	    		/*boolean bIsCorner = false;
	    		
	        	for (int j = 0; j < m_dIdealGradientProfileArray.length - 1; j++)
	    		{
	    			if (m_dIdealGradientProfileArray[j][0] == dTime1)
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
	    
	/*    public double calcAngleDifference3(int iIndex)
	    {
	    	if (iIndex <= 1)
	    		return 1;
	    	
	    	double dMaxRampRate = 70;
	    	
	    	double dTime2 = m_dTemperatureProfileArray[iIndex][0];
	    	double dTemp2 = m_dTemperatureProfileArray[iIndex][1];
	    	double dTime1 = this.m_dTemperatureProfileArray[iIndex - 1][0];
	    	double dTemp1 = this.m_dTemperatureProfileArray[iIndex - 1][1];
	    	double dTime0 = this.m_dTemperatureProfileArray[iIndex - 2][0];
	    	double dTemp0 = this.m_dTemperatureProfileArray[iIndex - 2][1];
	    	
	    	// Check if the previous point is a corner
	    	// If it is, then don't worry about the angle - return 0
			for (int i = 0; i < this.m_dIdealTemperatureProfileArray.length - 1; i++)
			{
				if (this.m_dIdealTemperatureProfileArray[i][0] == dTime1)
					return 1;
			}
	    	
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
			
			double dFactor1 = 1;
			double dAngleError = Math.pow((Math.abs(dNewAngle - dPreviousAngle) / (Math.PI)) * dFactor1, 2) + 1;
	    	return dAngleError;
	    }*/
	    
	 	/*public double goldenSectioningSearchSimpleGradientProfile(boolean bNonMixingVolume, double dStep, double dPrecision, double dMaxChangeAtOnce)
	 	{
			double dRetentionError = 1;
			double x1;
			double x2;
			double x3;
			double dRetentionErrorX1;
			double dRetentionErrorX2;
			double dRetentionErrorX3;
			
			double dLastVolumeGuess;
			
			if (bNonMixingVolume)
				dLastVolumeGuess = this.m_dNonMixingVolume;
			else
				dLastVolumeGuess = this.m_dMixingVolume;
			
			// Find bounds
			if (bNonMixingVolume)
				x1 = m_dNonMixingVolume;
			else
				x1 = m_dMixingVolume;
			dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), true);
			
			x2 = x1 + dStep;
			if (bNonMixingVolume)
				m_dNonMixingVolume = x2;
			else
				m_dMixingVolume = x2;
			calculateSimpleGradient();
			dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), true);
			
			if (dRetentionErrorX2 < dRetentionErrorX1)
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = ((x3 - x1) * this.m_dGoldenRatio) + x3;
				
				if (bNonMixingVolume)
					m_dNonMixingVolume = x2;
				else
					m_dMixingVolume = x2;
				calculateSimpleGradient();
				dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), true);

				while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastVolumeGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					
					x2 = ((x3 - x1) * this.m_dGoldenRatio) + x3;
					
					if (bNonMixingVolume)
						m_dNonMixingVolume = x2;
					else
						m_dMixingVolume = x2;
					calculateSimpleGradient();
					dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), true);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				
				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				if (bNonMixingVolume)
					m_dNonMixingVolume = x1;
				else
					m_dMixingVolume = x1;
				calculateSimpleGradient();
				dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), true);

				while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastVolumeGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;

					x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
					
					if (bNonMixingVolume)
						m_dNonMixingVolume = x1;
					else
						m_dMixingVolume = x1;
					calculateSimpleGradient();
					dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), true);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastVolumeGuess + dMaxChangeAtOnce)
			{
				if (bNonMixingVolume)
					m_dNonMixingVolume = dLastVolumeGuess + dMaxChangeAtOnce;
				else
					m_dMixingVolume = dLastVolumeGuess + dMaxChangeAtOnce;

				calculateSimpleGradient();
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), true);

				return dRetentionError;
			}
			
			if (x1 < dLastVolumeGuess - dMaxChangeAtOnce)
			{
				if (dLastVolumeGuess - dMaxChangeAtOnce < 0.00001)
				{
					if (bNonMixingVolume)
						m_dNonMixingVolume = 0.00001;
					else
						m_dMixingVolume = 0.00001;
				}
				else
				{
					if (bNonMixingVolume)
						m_dNonMixingVolume = dLastVolumeGuess - dMaxChangeAtOnce;
					else
						m_dMixingVolume = dLastVolumeGuess - dMaxChangeAtOnce;
				}
				
				calculateSimpleGradient();
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), true);
				
				return dRetentionError;
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
					x4 = x3 + (2 - this.m_dGoldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - this.m_dGoldenRatio) * (x3 - x1);
				}

				if (bNonMixingVolume)
					m_dNonMixingVolume = x4;
				else
					m_dMixingVolume = x4;
				calculateSimpleGradient();
				dRetentionErrorX4 = calcRetentionError(m_dtstep, standardsList.size(), true);
				
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
			if (x3 < 0.00001)
			{
				// We can't have mixing and nonmixing volumes that are negative and they also can't be zero
				if (bNonMixingVolume)
					m_dNonMixingVolume = 0.00001;
				else
					m_dMixingVolume = 0.00001;
				calculateSimpleGradient();
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), true);			
			}
			else
			{
				if (bNonMixingVolume)
					m_dNonMixingVolume = x3;
				else
					m_dMixingVolume = x3;
				calculateSimpleGradient();
				dRetentionError = dRetentionErrorX3;			
			}
	 		
	 		return dRetentionError;
	 	}*/
	 	
	 	public double goldenSectioningSearchSimpleGradientProfile(int iIndex, boolean bNonMixingVolume, double dStep, double dPrecision, double dMaxChangeAtOnce)
	 	{
			double dRetentionError = 1;
			double x1;
			double x2;
			double x3;
			double dRetentionErrorX1;
			double dRetentionErrorX2;
			double dRetentionErrorX3;
			
			double dLastVolumeGuess;
			
			if (bNonMixingVolume)
				dLastVolumeGuess = nonMixingVolumeArray[iIndex][1];
			else
				dLastVolumeGuess = mixingVolumeArray[iIndex][1];
			
			// Find bounds
			if (bNonMixingVolume)
				x1 = nonMixingVolumeArray[iIndex][1];
			else
				x1 = mixingVolumeArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), true);
			
			x2 = x1 + dStep;
			if (bNonMixingVolume)
				nonMixingVolumeArray[iIndex][1] = x2;
			else
				mixingVolumeArray[iIndex][1] = x2;
			interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
			interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), true);
			
			if (dRetentionErrorX2 < dRetentionErrorX1)
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = ((x3 - x1) * goldenRatio) + x3;
				
				if (bNonMixingVolume)
					nonMixingVolumeArray[iIndex][1] = x2;
				else
					mixingVolumeArray[iIndex][1] = x2;
				interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
				interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), true);

				while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastVolumeGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					
					x2 = ((x3 - x1) * goldenRatio) + x3;
					
					if (bNonMixingVolume)
						nonMixingVolumeArray[iIndex][1] = x2;
					else
						mixingVolumeArray[iIndex][1] = x2;
					interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
					interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
					calculateSimpleGradient();
					dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), true);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				
				x1 = x3 - (x2 - x3) * goldenRatio;
				
				if (bNonMixingVolume)
					nonMixingVolumeArray[iIndex][1] = x1;
				else
					mixingVolumeArray[iIndex][1] = x1;
				interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
				interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), true);

				while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastVolumeGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;

					x1 = x3 - (x2 - x3) * goldenRatio;
					
					if (bNonMixingVolume)
						nonMixingVolumeArray[iIndex][1] = x1;
					else
						mixingVolumeArray[iIndex][1] = x1;
					interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
					interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
					calculateSimpleGradient();
					dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), true);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastVolumeGuess + dMaxChangeAtOnce)
			{
				if (bNonMixingVolume)
					nonMixingVolumeArray[iIndex][1] = dLastVolumeGuess + dMaxChangeAtOnce;
				else
					mixingVolumeArray[iIndex][1] = dLastVolumeGuess + dMaxChangeAtOnce;
				interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
				interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), true);

				return dRetentionError;
			}
			
			if (x1 < dLastVolumeGuess - dMaxChangeAtOnce)
			{
				if (dLastVolumeGuess - dMaxChangeAtOnce < 0.00001)
				{
					if (bNonMixingVolume)
						nonMixingVolumeArray[iIndex][1] = 0.00001;
					else
						mixingVolumeArray[iIndex][1] = 0.00001;
				}
				else
				{
					if (bNonMixingVolume)
						nonMixingVolumeArray[iIndex][1] = dLastVolumeGuess - dMaxChangeAtOnce;
					else
						mixingVolumeArray[iIndex][1] = dLastVolumeGuess - dMaxChangeAtOnce;
				}
				interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
				interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
				
				calculateSimpleGradient();
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), true);
				
				return dRetentionError;
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
					x4 = x3 + (2 - goldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - goldenRatio) * (x3 - x1);
				}

				if (bNonMixingVolume)
					nonMixingVolumeArray[iIndex][1] = x4;
				else
					mixingVolumeArray[iIndex][1] = x4;
				interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
				interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionErrorX4 = calcRetentionError(dtstep, standardsList.size(), true);
				
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
			if (x3 < 0.00001)
			{
				// We can't have mixing and nonmixing volumes that are negative and they also can't be zero
				if (bNonMixingVolume)
					nonMixingVolumeArray[iIndex][1] = 0.00001;
				else
					mixingVolumeArray[iIndex][1] = 0.00001;
				interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
				interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), true);			
			}
			else
			{
				if (bNonMixingVolume)
					nonMixingVolumeArray[iIndex][1] = x3;
				else
					mixingVolumeArray[iIndex][1] = x3;
				interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
				interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionError = dRetentionErrorX3;			
			}
	 		
	 		return dRetentionError;
	 	}
	 	
	 	public double goldenSectioningSearchGradientProfile(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, double dAngleErrorMultiplier)
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
			
			double dLastTempGuess = gradientProfileDifferenceArray[iIndex][1];
			
			// Find bounds
			x1 = gradientProfileDifferenceArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), false);
			dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
			
			x2 = x1 + dStep;
			gradientProfileDifferenceArray[iIndex][1] = x2;
			interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), false);
			dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
			
			if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * goldenRatio + x3;
				
				gradientProfileDifferenceArray[iIndex][1] = x2;
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), false);
				dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
				

				while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastTempGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					dAngleErrorX3 = dAngleErrorX2;
					
					x2 = (x3 - x1) * goldenRatio + x3;
					
					gradientProfileDifferenceArray[iIndex][1] = x2;
					interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
					dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), false);
					dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * goldenRatio;
				
				gradientProfileDifferenceArray[iIndex][1] = x1;
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), false);
				dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);

				while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastTempGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;
					dAngleErrorX3 = dAngleErrorX1;

					x1 = x3 - (x2 - x3) * goldenRatio;
					
					gradientProfileDifferenceArray[iIndex][1] = x1;
					interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
					dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), false);
					dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastTempGuess + dMaxChangeAtOnce)
			{
				if (dLastTempGuess + dMaxChangeAtOnce > 100)
					gradientProfileDifferenceArray[iIndex][1] = 100;
				else
					gradientProfileDifferenceArray[iIndex][1] = dLastTempGuess + dMaxChangeAtOnce;
				
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);

				return dRetentionError;
			}
			
			if (x1 < dLastTempGuess - dMaxChangeAtOnce)
			{
				if (dLastTempGuess - dMaxChangeAtOnce < -100)
					gradientProfileDifferenceArray[iIndex][1] = -100;
				else
					gradientProfileDifferenceArray[iIndex][1] = dLastTempGuess - dMaxChangeAtOnce;
				
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);
				
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
					x4 = x3 + (2 - goldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - goldenRatio) * (x3 - x1);
				}

				
				gradientProfileDifferenceArray[iIndex][1] = x4;
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionErrorX4 = calcRetentionError(dtstep, standardsList.size(), false);
				dAngleErrorX4 = calcAngleDifferenceGradient(iIndex);
				
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
			if (x3 > 100)
			{
				gradientProfileDifferenceArray[iIndex][1] = 100;
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);			
			}
			else if (x3 < -100)
			{
				gradientProfileDifferenceArray[iIndex][1] = -100;
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);			
			}
			else
			{
				gradientProfileDifferenceArray[iIndex][1] = x3;
				interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
				dRetentionError = dRetentionErrorX3;			
			}
	 		
	 		return dRetentionError;
	 	}

	 	public double goldenSectioningSearchDeadTime(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, double dAngleErrorMultiplier)
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
			
			double dLastFGuess = deadTimeDifferenceArray[iIndex][1];

			// Find bounds
			x1 = deadTimeDifferenceArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), false);
			dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);
			
			x2 = x1 + dStep;
			deadTimeDifferenceArray[iIndex][1] = x2;
			interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), false);
			dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);
			
			if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * goldenRatio + x3;
				
				deadTimeDifferenceArray[iIndex][1] = x2;
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), false);
				dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);

				while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastFGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					dAngleErrorX3 = dAngleErrorX2;
					
					x2 = (x3 - x1) * goldenRatio + x3;
					
					deadTimeDifferenceArray[iIndex][1] = x2;
					interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
					dRetentionErrorX2 = calcRetentionError(dtstep, standardsList.size(), false);
					dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * goldenRatio;
				
				deadTimeDifferenceArray[iIndex][1] = x1;
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), false);
				dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);

				while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastFGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;
					dAngleErrorX3 = dAngleErrorX1;
					
					x1 = x3 - (x2 - x3) * goldenRatio;
					
					deadTimeDifferenceArray[iIndex][1] = x1;
					interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
					dRetentionErrorX1 = calcRetentionError(dtstep, standardsList.size(), false);
					dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastFGuess + dMaxChangeAtOnce)
			{
				deadTimeDifferenceArray[iIndex][1] = dLastFGuess + dMaxChangeAtOnce;
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);
				
				return dRetentionError;
			}
			
			if (x1 < dLastFGuess - dMaxChangeAtOnce)
			{
				deadTimeDifferenceArray[iIndex][1] = dLastFGuess - dMaxChangeAtOnce;
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);

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
					x4 = x3 + (2 - goldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - goldenRatio) * (x3 - x1);
				}
				
				deadTimeDifferenceArray[iIndex][1] = x4;
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionErrorX4 = calcRetentionError(dtstep, standardsList.size(), false);
				dAngleErrorX4 = calcAngleDifferenceDeadTime(iIndex);

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
			deadTimeDifferenceArray[iIndex][1] = x3;
			interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
			dRetentionError = dRetentionErrorX3;
			
			return dRetentionError;
	 	}
	    
	/* 	public double goldenSectioningSearchDeadTimeOffset(double dStep, double dPrecision, int iNumCompoundsToUse)
	 	{
			double dRetentionError = 1;
	 		double F1;
			double F2;
			double F3;
			double dRetentionErrorF1;
			double dRetentionErrorF2;
			double dRetentionErrorF3;
			
			// Find bounds
			// Get the first value
			F1 = m_dDeadTimeDifferenceArray[0][1];
			dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

			// Try a direction
			F2 = F1 + dStep;
			
			// Add that to the difference array
			double dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}

			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			
			if (dRetentionErrorF2 < dRetentionErrorF1)
			{
				// We're going in the right direction
				F3 = F2;
				dRetentionErrorF3 = dRetentionErrorF2;
				
				F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
				
				dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

				while (dRetentionErrorF2 < dRetentionErrorF3)
				{
					F1 = F3;
					dRetentionErrorF1 = dRetentionErrorF3;
					F3 = F2;
					dRetentionErrorF3 = dRetentionErrorF2;
					
					F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
					
					dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
					for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
					{
						m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
					}
					m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
					dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
				}
			}
			else
			{
				// We need to go in the opposite direction
				F3 = F1;
				dRetentionErrorF3 = dRetentionErrorF1;
				
				F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
				
				dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

				while (dRetentionErrorF1 < dRetentionErrorF3)
				{
					F2 = F3;
					dRetentionErrorF2 = dRetentionErrorF3;
					F3 = F1;
					dRetentionErrorF3 = dRetentionErrorF1;
					
					F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
					
					dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
					for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
					{
						m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
					}
					m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
					dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
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
					F4 = F3 + (2 - this.m_dGoldenRatio) * (F2 - F3);
				}
				else 
				{
					// F1 and F3, so F4 must be placed between them
					F4 = F3 - (2 - this.m_dGoldenRatio) * (F3 - F1);
				}

				
				dPercentDiff = (F4 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF4 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
				
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
			
			// Restore profile to best value
			dPercentDiff = (F3 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			
			//dRetentionError = dRetentionErrorF3;
			
			// Calculate retention error with all of the standards
			dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);

			return dRetentionError;
	 	}*/
	 	
	 	public double goldenSectioningSearchDeadTimeOffset(double dStep, double dPrecision, int iNumCompoundsToUse, double dMaxPercentChangeAtOnce)
	 	{
			double dRetentionError = 1;
	 		double F1;
			double F2;
			double F3;
			double dRetentionErrorF1;
			double dRetentionErrorF2;
			double dRetentionErrorF3;
			double dUpperBound = (deadTimeDifferenceArray[0][1] + initialDeadTimeArray[0][1]) * (1.0 + (dMaxPercentChangeAtOnce / 100.0));
			double dLowerBound = (deadTimeDifferenceArray[0][1] + initialDeadTimeArray[0][1]) * (1.0 - (dMaxPercentChangeAtOnce / 100.0));
			
			// Find bounds
			F1 = deadTimeDifferenceArray[0][1];
			dRetentionErrorF1 = calcRetentionError(dtstep, iNumCompoundsToUse, false);

			F2 = F1 + dStep;
			
			double dPercentDiff = (F2 - deadTimeDifferenceArray[0][1]) / (initialDeadTimeArray[0][1] + deadTimeDifferenceArray[0][1]);
			for (int i = 0; i < deadTimeDifferenceArray.length; i++)
			{
				deadTimeDifferenceArray[i][1] += (initialDeadTimeArray[i][1] + deadTimeDifferenceArray[i][1]) * dPercentDiff;
			}

			interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
			dRetentionErrorF2 = calcRetentionError(dtstep, iNumCompoundsToUse, false);
			
			if (dRetentionErrorF2 < dRetentionErrorF1)
			{
				// We're going in the right direction
				F3 = F2;
				dRetentionErrorF3 = dRetentionErrorF2;
				
				F2 = (F3 - F1) * goldenRatio + F3;
				
				dPercentDiff = (F2 - deadTimeDifferenceArray[0][1]) / (initialDeadTimeArray[0][1] + deadTimeDifferenceArray[0][1]);
				for (int i = 0; i < deadTimeDifferenceArray.length; i++)
				{
					deadTimeDifferenceArray[i][1] += (initialDeadTimeArray[i][1] + deadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionErrorF2 = calcRetentionError(dtstep, iNumCompoundsToUse, false);

				while (dRetentionErrorF2 < dRetentionErrorF3 && F2 + initialDeadTimeArray[0][1] < dUpperBound)
				{
					F1 = F3;
					dRetentionErrorF1 = dRetentionErrorF3;
					F3 = F2;
					dRetentionErrorF3 = dRetentionErrorF2;
					
					F2 = (F3 - F1) * goldenRatio + F3;
					
					dPercentDiff = (F2 - deadTimeDifferenceArray[0][1]) / (initialDeadTimeArray[0][1] + deadTimeDifferenceArray[0][1]);
					for (int i = 0; i < deadTimeDifferenceArray.length; i++)
					{
						deadTimeDifferenceArray[i][1] += (initialDeadTimeArray[i][1] + deadTimeDifferenceArray[i][1]) * dPercentDiff;
					}
					interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
					dRetentionErrorF2 = calcRetentionError(dtstep, iNumCompoundsToUse, false);
				}
			}
			else
			{
				// We need to go in the opposite direction
				F3 = F1;
				dRetentionErrorF3 = dRetentionErrorF1;
				
				F1 = F3 - (F2 - F3) * goldenRatio;
				
				dPercentDiff = (F1 - deadTimeDifferenceArray[0][1]) / (initialDeadTimeArray[0][1] + deadTimeDifferenceArray[0][1]);
				for (int i = 0; i < deadTimeDifferenceArray.length; i++)
				{
					deadTimeDifferenceArray[i][1] += (initialDeadTimeArray[i][1] + deadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionErrorF1 = calcRetentionError(dtstep, iNumCompoundsToUse, false);

				while (dRetentionErrorF1 < dRetentionErrorF3 && F1 + initialDeadTimeArray[0][1] > dLowerBound)
				{
					F2 = F3;
					dRetentionErrorF2 = dRetentionErrorF3;
					F3 = F1;
					dRetentionErrorF3 = dRetentionErrorF1;
					
					F1 = F3 - (F2 - F3) * goldenRatio;
					
					dPercentDiff = (F1 - deadTimeDifferenceArray[0][1]) / (initialDeadTimeArray[0][1] + deadTimeDifferenceArray[0][1]);
					for (int i = 0; i < deadTimeDifferenceArray.length; i++)
					{
						deadTimeDifferenceArray[i][1] += (initialDeadTimeArray[i][1] + deadTimeDifferenceArray[i][1]) * dPercentDiff;
					}
					interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
					dRetentionErrorF1 = calcRetentionError(dtstep, iNumCompoundsToUse, false);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (F2 + initialDeadTimeArray[0][1] > dUpperBound)
			{
				dPercentDiff = (dUpperBound - initialDeadTimeArray[0][1]) / (initialDeadTimeArray[0][1]);
				for (int i = 0; i < deadTimeDifferenceArray.length; i++)
				{
					deadTimeDifferenceArray[i][1] = initialDeadTimeArray[i][1] * dPercentDiff;
				}
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);
				
				return dRetentionError;
			}
			
			if (F1 + initialDeadTimeArray[0][1] < dLowerBound)
			{
				dPercentDiff = (dLowerBound - initialDeadTimeArray[0][1]) / (initialDeadTimeArray[0][1]);
				for (int i = 0; i < deadTimeDifferenceArray.length; i++)
				{
					deadTimeDifferenceArray[i][1] = initialDeadTimeArray[i][1] * dPercentDiff;
				}
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);

				return dRetentionError;
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
					F4 = F3 + (2 - goldenRatio) * (F2 - F3);
				}
				else 
				{
					// F1 and F3, so F4 must be placed between them
					F4 = F3 - (2 - goldenRatio) * (F3 - F1);
				}

				
				dPercentDiff = (F4 - deadTimeDifferenceArray[0][1]) / (initialDeadTimeArray[0][1] + deadTimeDifferenceArray[0][1]);
				for (int i = 0; i < deadTimeDifferenceArray.length; i++)
				{
					deadTimeDifferenceArray[i][1] += (initialDeadTimeArray[i][1] + deadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
				dRetentionErrorF4 = calcRetentionError(dtstep, iNumCompoundsToUse, false);
				
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
			
			// Restore profile to best value
			dPercentDiff = (F3 - deadTimeDifferenceArray[0][1]) / (initialDeadTimeArray[0][1] + deadTimeDifferenceArray[0][1]);
			for (int i = 0; i < deadTimeDifferenceArray.length; i++)
			{
				deadTimeDifferenceArray[i][1] += (initialDeadTimeArray[i][1] + deadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
			
			//dRetentionError = dRetentionErrorF3;
			
			// Calculate retention error with all of the standards
			dRetentionError = calcRetentionError(dtstep, standardsList.size(), false);

			return dRetentionError;
	 	}

		
		
		
		// Iterative - in sequence, *Golden* Sectioning Search algorithm
	    // Start by optimizing the entire dead time error profile.
		public void backCalculate(boolean bDeadTimeProfileFirst)
		{
//			if (true)
//				return;
			long starttime = System.currentTimeMillis();
			noFullBackcalculation = false;
			
			// Phase I conditions:
			updateProgress(-1, 0);
			updateMessage("Phase I: Determining gradient delay volume");
			
			boolean bBackCalculateSimpleGradientProfile = true;
			boolean bBackCalculateDeadTimeProfileOffset = false;
			boolean bBackCalculateGradientProfile = false;
			boolean bBackCalculateDeadTimeProfile = false;
			double dGradientProfileAngleWeight = 0;
			double dDeadTimeProfileAngleWeight = 0;
			int iNumCompoundsToUseForDeadTimeOffset = standardsList.size(); // 2;
	    	dtstep = plotXMax2 * 0.01;
			boolean bSkipTerminationTest = true;

			NumberFormat formatter1 = new DecimalFormat("#0.000000");
			NumberFormat formatter2 = new DecimalFormat("0.0000E0");
			NumberFormat formatter3 = new DecimalFormat("0.000");
			NumberFormat percentFormatter = new DecimalFormat("0.00");
			
			// Step #1: Create interpolating functions for the isocratic data of each gradient calibration solute
			standardIsocraticDataInterpolated = new InterpolationFunction[standardsList.size()];
			
			for (int i = 0; i < standardsList.size(); i++)
			{
				Integer iIndex = (Integer) standardsList.get(i).getIndex();
				standardIsocraticDataInterpolated[i] = new InterpolationFunction(Globals.StandardIsocraticDataArray[iIndex]);
			}

			int iPhase = 1;
			int iIteration = 0;
			double dLastFullIterationError = 0;
			double dRetentionError = 0;
			
			while (true)
			{
				iIteration++;
				updateIteration(iIteration);
				dLastFullIterationError = dRetentionError;
				
				if (bBackCalculateSimpleGradientProfile)
				{
					double dVolumeStep = .01; //in mL
					double dMaxChangeAtOnce = .05;
					double dPercentBPrecision = 0.0001;
					
					for (int i = 0; i < nonMixingVolumeArray.length; i++)
					{
						dRetentionError = goldenSectioningSearchSimpleGradientProfile(i, true, dVolumeStep, dPercentBPrecision, dMaxChangeAtOnce);
					}
					
					for (int i = 0; i < mixingVolumeArray.length; i++)
					{
						dRetentionError = goldenSectioningSearchSimpleGradientProfile(i, false, dVolumeStep, dPercentBPrecision, dMaxChangeAtOnce);
					}
					
					variance = dRetentionError / standardsList.size();

					updateTimeElapsed(starttime);
					
					double dNum = dRetentionError / standardsList.size();
					String str;
					if (dNum < 0.0001)
						str = formatter2.format(dNum);
					else
						str = formatter1.format(dNum);
					updateVariance(dRetentionError/standardsList.size());
					
					calculateSimpleGradient();
					//updateGraphs(true);

					if (this.isCancelled())
					{
						return;
					}
				}

				if (bBackCalculateDeadTimeProfileOffset)
				{
					double dDeadTimeStep = initialDeadTimeArray[0][1] / 1000;
					double dDeadTimePrecision = initialDeadTimeArray[0][1] / 100000;
					double dMaxPercentChangeAtOnce = 1;
					
					dRetentionError = goldenSectioningSearchDeadTimeOffset(dDeadTimeStep, dDeadTimePrecision, iNumCompoundsToUseForDeadTimeOffset, dMaxPercentChangeAtOnce);
					variance = dRetentionError / standardsList.size();
					
					updateTimeElapsed(starttime);
					updateVariance(dRetentionError/standardsList.size());
					
					calculateSimpleDeadTime();
					//updateGraphs(true);
					
					if (this.isCancelled())
					{
						return;
					}
				}
				
				if (bBackCalculateDeadTimeProfile)
				{
					for (int iTimePoint = 0; iTimePoint < deadTimeDifferenceArray.length; iTimePoint++)
					{
						double dDeadTimeStep = initialDeadTimeArray[0][1] / 1000;
						double dDeadTimePrecision = initialDeadTimeArray[0][1] / 100000;
						double dMaxChangeAtOnce = initialDeadTimeArray[0][1] / 100;
						
						dRetentionError = goldenSectioningSearchDeadTime(iTimePoint, dDeadTimeStep, dDeadTimePrecision, dMaxChangeAtOnce, dDeadTimeProfileAngleWeight);
						variance = dRetentionError / standardsList.size();
						
						updateTimeElapsed(starttime);
						updateVariance(dRetentionError/standardsList.size());
						//updateGraphs(true);
						
					
						if (this.isCancelled())
						{
							return;
						}
					}
				}
				
				if (bBackCalculateGradientProfile)
				{
					for (int iTimePoint = 0; iTimePoint < gradientProfileDifferenceArray.length; iTimePoint++)
					{
						double dPercentBStep = .1;
						double dMaxChangeAtOnce = 2;
						double dPercentBPrecision = 0.001;
						
						dRetentionError = goldenSectioningSearchGradientProfile(iTimePoint, dPercentBStep, dPercentBPrecision, dMaxChangeAtOnce, dGradientProfileAngleWeight);
						variance = dRetentionError / standardsList.size();
						
						updateTimeElapsed(starttime);
						updateVariance(dRetentionError/standardsList.size());
						//updateGraphs(true);
						
						if (this.isCancelled())
						{
							return;
						}
					}
				}
				
				updateLastIterationVariance(dRetentionError/standardsList.size());
				
				// Calculate the percent improvement
				if (!bSkipTerminationTest)
				{
					double dPercentImprovement = (1 - (dRetentionError / dLastFullIterationError)) * 100;
					updatePercentImprovement(dPercentImprovement, true);
					
					if (iPhase == 1)
					{
						if (dPercentImprovement < .1 && dPercentImprovement >= 0)
						{
							iPhase++;
				//			contentPane2.jlblPhase.setText("II");
							
							bBackCalculateSimpleGradientProfile = true;
							bBackCalculateDeadTimeProfileOffset = true;
							iNumCompoundsToUseForDeadTimeOffset = standardsList.size();
							bBackCalculateGradientProfile = false;
							bBackCalculateDeadTimeProfile = false;
							dGradientProfileAngleWeight = 1000d;
							dDeadTimeProfileAngleWeight = 100;
					    	dtstep = plotXMax2 * 0.01;
							bSkipTerminationTest = true;
							
							updateMessage("Phase II: Fitting dead time");
						}
					}
					else if (iPhase == 2)
					{
						if (dPercentImprovement < .1 && dPercentImprovement >= 0)
						{
							iPhase++;
						//	contentPane2.jlblPhase.setText("III");
							
							bBackCalculateSimpleGradientProfile = false;
							bBackCalculateDeadTimeProfileOffset = false;
							iNumCompoundsToUseForDeadTimeOffset = standardsList.size();
							bBackCalculateGradientProfile = true;
							bBackCalculateDeadTimeProfile = true;
							dGradientProfileAngleWeight = 1000d;
							dDeadTimeProfileAngleWeight = 100;
					    	dtstep = plotXMax2 * 0.01;
							bSkipTerminationTest = true;
							
							updateMessage("Phase III: Fitting with angle constraints");
							
							// Now place handles on the gradient - need to do this now instead of earlier because we need gradient delay to find true corners
							placeHandles();
							
							showSimpleDeadTime = true;
							showSimpleGradient = true;
						}
					}
					else if (iPhase == 3)
					{
						if (dPercentImprovement < 2 && dPercentImprovement >= 0)
						{
							iPhase++;
//							contentPane2.jlblPhase.setText("IV");						
							updateMessage("Phase IV: Fitting without angle constraints");

							bBackCalculateSimpleGradientProfile = false;
							bBackCalculateDeadTimeProfileOffset = false;
							iNumCompoundsToUseForDeadTimeOffset = standardsList.size();
							bBackCalculateGradientProfile = true;
							bBackCalculateDeadTimeProfile = true;
							dGradientProfileAngleWeight = 0;
							dDeadTimeProfileAngleWeight = 0;	
							bSkipTerminationTest = true;
						}					
					}
					else if (iPhase == 4)
					{
						if (dPercentImprovement < 2 && dPercentImprovement >= 0)
						{
							iPhase++;
							//contentPane2.jlblPhase.setText("V");
							
							updateMessage("Phase V: optimization");

							bBackCalculateSimpleGradientProfile = false;
							bBackCalculateDeadTimeProfileOffset = false;
							iNumCompoundsToUseForDeadTimeOffset = standardsList.size();
							bBackCalculateGradientProfile = true;
							bBackCalculateDeadTimeProfile = true;
							dGradientProfileAngleWeight = 0;
							dDeadTimeProfileAngleWeight = 0;	
							dtstep = plotXMax2 * 0.001;
							bSkipTerminationTest = true;
						}					
					}
					else if (iPhase >= 5)
					{
						if (dPercentImprovement < 1 && dPercentImprovement >= 0)
						{
							updateMessage("Backcalculation complete!");
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
				updateGraphs(true);
				updateTable();
			}
			
		}


		
	    // Select where to place the handles in the m_dGradientProfileDifferenceArray and the m_dDeadTimeDifferenceArray
	    public void placeHandles()
	    {
	        // Find greatest dead volume
			double dVmMax = 0;
			
			for (int i = 0; i < initialDeadTimeArray.length; i++)
			{
				if (initialDeadTimeArray[i][1] + interpolatedDeadTimeDifferenceProfile.getAt(initialDeadTimeArray[i][0])> dVmMax)
				{
					dVmMax = initialDeadTimeArray[i][1] + interpolatedDeadTimeDifferenceProfile.getAt(initialDeadTimeArray[i][0]);
				}
			}	  
			
			dVmMax *= flowRate / 60;
			
	    	// Select number of data points for the gradient and flow profiles
			int iTotalDataPoints = standardsList.size();
			
			// 11/15ths of the data points should be on the gradient profile
			int iNumGradientProgramDataPoints = (int)(((double)11 / (double)15)*(double)iTotalDataPoints);
			int iNumFlowDataPoints = iTotalDataPoints - iNumGradientProgramDataPoints;
			
			if (iNumFlowDataPoints < 3)
			{
				iNumFlowDataPoints = 3;
				iNumGradientProgramDataPoints = iTotalDataPoints - iNumFlowDataPoints;
			}
			
			// Create initial gradient and flow rate arrays
			
			// First make an array with the correct number of data points.
			gradientProfileDifferenceArray = new double [iNumGradientProgramDataPoints][2];
			
			deadTimeDifferenceArray = new double [iNumFlowDataPoints][2];
			
			// Set the value of the first data point
			gradientProfileDifferenceArray[0][0] = 0;
			gradientProfileDifferenceArray[0][1] = 0;
			
			for (int i = 1; i < iNumGradientProgramDataPoints - 1; i++)
			{
				// Find the two nearest standards
				double dStandardNum = ((double)i / ((double)iNumGradientProgramDataPoints - 1)) * (double)iTotalDataPoints;
				double dOneGreater = (int)Math.ceil(dStandardNum);
				double dOneLesser = (int)Math.floor(dStandardNum);
				double dRtOneLesser = (Double)standardsList.get((int)dOneLesser).getMeasuredRetentionTime();
				double dRtOneGreater = (Double)standardsList.get((int)dOneGreater).getMeasuredRetentionTime();
			
				// Check to see if we landed exactly on an alkane
				if (dOneGreater == dOneLesser)
				{
					// If so, set the time of this point to the alkane we landed on
					gradientProfileDifferenceArray[i][0] = dRtOneLesser;
				}
				else
				{
					// Otherwise, find the time of this point in between the surrounding alkanes
					double dPosition = ((dStandardNum - dOneLesser) / (dOneGreater - dOneLesser));
					gradientProfileDifferenceArray[i][0] = (dPosition * dRtOneGreater) + ((1 - dPosition) * dRtOneLesser);
				}
				
				// Subtract half a hold-up time
				gradientProfileDifferenceArray[i][0] = gradientProfileDifferenceArray[i][0] - (0.5 * (dVmMax / flowRate));
				gradientProfileDifferenceArray[i][1] = 0;
			}
			
			// Add the last data point at the position of the last-eluting compound - half dead time
			gradientProfileDifferenceArray[iNumGradientProgramDataPoints - 1][0] = (Double)standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() - (0.5 * (dVmMax / flowRate));
			gradientProfileDifferenceArray[iNumGradientProgramDataPoints - 1][1] = 0;
			
			// Create another copy of the ideal gradient profile array, delayed by the  delay
			double[][] dDelayedIdealGradientProfileArray = new double[idealGradientProfileArray.length + 1][2];
			dDelayedIdealGradientProfileArray[0][0] = 0;
			dDelayedIdealGradientProfileArray[0][1] = idealGradientProfileArray[0][1];
			for (int i = 1; i < dDelayedIdealGradientProfileArray.length; i++)
			{
				//dDelayedIdealGradientProfileArray[i][0] = m_dIdealGradientProfileArray[i - 1][0] + ((this.m_dMixingVolume + this.m_dNonMixingVolume) / this.m_dFlowRate);
				dDelayedIdealGradientProfileArray[i][0] = idealGradientProfileArray[i - 1][0] + ((interpolatedMixingVolume.getAt(idealGradientProfileArray[i - 1][1]) + interpolatedNonMixingVolume.getAt(idealGradientProfileArray[i - 1][1])) / flowRate);
				dDelayedIdealGradientProfileArray[i][1] = idealGradientProfileArray[i - 1][1];
			}
			
			// Now adjust to get points at the corners
			int iPointIndex = 1;
			// Run through each corner in the ideal gradient profile array
			for (int i = 1; i < dDelayedIdealGradientProfileArray.length - 1; i++)
			{
				double dFirst = 0;
				double dNext = 0;
				
				// Find the first point after the corner (dNext) and the first point before the corner (dFirst)
				while (dNext < dDelayedIdealGradientProfileArray[i][0] && iPointIndex < gradientProfileDifferenceArray.length - 1)
				{
					dFirst = gradientProfileDifferenceArray[iPointIndex][0];
					dNext = gradientProfileDifferenceArray[iPointIndex + 1][0];
					
					iPointIndex++;
				}
				
				// Remove the last increment
				iPointIndex--;
				
				// Find the distances between the corner and the two points
				double dDistFirst = dDelayedIdealGradientProfileArray[i][0] - dFirst;
				double dDistNext = dNext - dDelayedIdealGradientProfileArray[i][0];
				
				// Find the distances between the two points and their next further point
				double dDistFirstBefore = gradientProfileDifferenceArray[iPointIndex][0] - gradientProfileDifferenceArray[iPointIndex - 1][0];
				double dDistNextAfter;
				if (iPointIndex + 2 < gradientProfileDifferenceArray.length)
					dDistNextAfter = gradientProfileDifferenceArray[iPointIndex + 2][0] - gradientProfileDifferenceArray[iPointIndex + 1][0];
				else
					dDistNextAfter = 0;
				
				double dScoreFirst = dDistFirst + dDistFirstBefore;
				double dScoreNext = dDistNext + dDistNextAfter;
				
				// Point with lower score moves
				if (dScoreFirst < dScoreNext)
				{
					// Move the first point
					gradientProfileDifferenceArray[iPointIndex][0] = dDelayedIdealGradientProfileArray[i][0];
					gradientProfileDifferenceArray[iPointIndex][1] = 0;

					// Move the one before it right in between it and the last point
					if (iPointIndex >= 2)
					{
						gradientProfileDifferenceArray[iPointIndex - 1][0] = (gradientProfileDifferenceArray[iPointIndex - 2][0] + gradientProfileDifferenceArray[iPointIndex][0]) / 2;
						gradientProfileDifferenceArray[iPointIndex - 1][1] = 0;
					}
					
					// Move the one after it in between it and the next point
					if (iPointIndex <= gradientProfileDifferenceArray.length - 3)
					{
						gradientProfileDifferenceArray[iPointIndex + 1][0] = (gradientProfileDifferenceArray[iPointIndex][0] + gradientProfileDifferenceArray[iPointIndex + 2][0]) / 2;
						gradientProfileDifferenceArray[iPointIndex + 1][1] = 0;
					}
					
				}
				else
				{
					// Move the next point
					gradientProfileDifferenceArray[iPointIndex + 1][0] = dDelayedIdealGradientProfileArray[i][0];
					gradientProfileDifferenceArray[iPointIndex + 1][1] = 0;

					// Move the one before it right in between it and the last point
					if (iPointIndex >= 1)
					{
						gradientProfileDifferenceArray[iPointIndex][0] = (gradientProfileDifferenceArray[iPointIndex - 1][0] + gradientProfileDifferenceArray[iPointIndex + 1][0]) / 2;
						gradientProfileDifferenceArray[iPointIndex][1] = 0;
					}
					
					// Move the one after it in between it and the next point
					if (iPointIndex <= gradientProfileDifferenceArray.length - 4)
					{
						gradientProfileDifferenceArray[iPointIndex + 2][0] = (gradientProfileDifferenceArray[iPointIndex + 1][0] + gradientProfileDifferenceArray[iPointIndex + 3][0]) / 2;
						gradientProfileDifferenceArray[iPointIndex + 2][1] = 0;
					}
				}
			}

	    	// Now for the flow rate vs. time profile:
			
	/*    	// Add the initial interpolated hold-up time profile to the graph control
		    int iNumPoints = 1000;
		    for (int i = 0; i < iNumPoints; i++)
		    {
		    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
		    	deadTimeEluentCompositionGraph.AddDataPoint(iIdealPlotIndexDeadTime, dXPos * 100, this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos));
		    }
	*/
		    for (int i = 0; i < iNumFlowDataPoints; i++)
			{
		    	deadTimeDifferenceArray[i][0] = (0.90 * ((double)i/((double)iNumFlowDataPoints - 1)) + 0.05);
		    	deadTimeDifferenceArray[i][1] = interpolatedDeadTimeDifferenceProfile.getAt(deadTimeDifferenceArray[i][0]);
			}
			
			interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
			interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
			
			/*//setContentPane(contentPane2);
			this.jMainScrollPane.setViewportView(contentPane2);
			
			// Calculate the gradients (even though we don't show them)
			calculateSimpleDeadTime();
			calculateSimpleGradient();
			
			this.updateGraphs(false);*/
	    }
	}

	protected void updateTable(
			ObservableList<StandardCompound> newStandardsList) {
		if (newStandardsList.size() != standardsList.size())
			return;
		
		for (int i = 0; i < newStandardsList.size(); i++)
		{
			standardsList.get(i).makeEqualTo(newStandardsList.get(i));
		}
	}

	public void setGradientProgram(double[][] gradientProgram) {
		this.gradientProgram = gradientProgram;
		
	}
	
	public double calcPlotXMaximum()
	{
		double plotXMax = (((Double)this.standardsList.get(this.standardsList.size() - 1).getMeasuredRetentionTime()));

	    // Extend the end of the temperature program if it is too short.
	    double finalTime = idealGradientProfileArray[idealGradientProfileArray.length - 1][0];
    	if (finalTime > plotXMax)
    		plotXMax = finalTime + 1;
    	
    	return plotXMax;
	}

	public void setStandardsList(ObservableList<StandardCompound> standardsList) {
		
		// Create dead time array
        this.initialDeadTimeArray = new double[Globals.dDeadTimeArray.length][2];
        
        for (int i = 0; i < Globals.dDeadTimeArray.length; i++)
        {
        	double dVolumeInRefColumn = Math.PI * Math.pow(Globals.dRefColumnID / 2, 2) * Globals.dRefColumnLength;
        	double dDeadVolPerVol = (Globals.dDeadTimeArray[i][1] * Globals.dRefFlowRate) / dVolumeInRefColumn;
        	double dNewDeadVol = dDeadVolPerVol * Math.PI * Math.pow((this.innerDiameter / 2) / 10, 2) * this.columnLength / 10;
        	this.initialDeadTimeArray[i][0] = Globals.dDeadTimeArray[i][0];
        	this.initialDeadTimeArray[i][1] = (dNewDeadVol / this.flowRate) * 60;
        }
        
		this.initialInterpolatedDeadTimeProfile = new InterpolationFunction(this.initialDeadTimeArray);
		
		// Find longest retention time
		double dLongestRetentionTime = 0;
		for (int i = 0; i < standardsList.size(); i++)
		{
			double dValue = standardsList.get(i).getPredictedRetentionTime();
			if (dValue > dLongestRetentionTime)
				dLongestRetentionTime = dValue;
		}

		double dLastProgramTime = gradientProgram[gradientProgram.length - 1][0];
		plotXMax = Math.max(dLongestRetentionTime * 1.1, dLastProgramTime * 1.1);
		
    	eluentCompositionTimeGraph.RemoveAllSeries();
    	deadTimeEluentCompositionGraph.RemoveAllSeries();

    	int iIdealPlotIndex = eluentCompositionTimeGraph.AddSeries("Ideal Gradient", Color.BLACK, 1, false, false);
    	int iIdealPlotIndexDeadTime = deadTimeEluentCompositionGraph.AddSeries("Ideal Dead Time", Color.BLACK, 1, false, false);

    	//deadTimeEluentCompositionGraph.AddDataPoint(iIdealPlotIndexHoldUp, 0, this.m_dFlowRate / 1000);
    	//deadTimeEluentCompositionGraph.AddDataPoint(iIdealPlotIndexHoldUp, m_dPlotXMax * 60, this.m_dFlowRate / 1000);

	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	double dDeadTime = this.initialInterpolatedDeadTimeProfile.getAt(dXPos);
	    	deadTimeEluentCompositionGraph.AddDataPoint(iIdealPlotIndexDeadTime, dXPos * 100, dDeadTime);
	    }

    	eluentCompositionTimeGraph.AddDataPoint(iIdealPlotIndex, 0, gradientProgram[0][1]);	
    	double dLastTime = 0;
    	
    	for (int i = 0; i < gradientProgram.length; i++)
		{
    		if (gradientProgram[i][0] > dLastTime)
    		{
    			double dTime = gradientProgram[i][0];
    			double dFractionB = gradientProgram[i][1];
    			
    	    	eluentCompositionTimeGraph.AddDataPoint(iIdealPlotIndex, dTime * 60, dFractionB);	
    		
    	    	dLastTime = dTime;
    		}
		}
    	
		eluentCompositionTimeGraph.AddDataPoint(iIdealPlotIndex, plotXMax * 60, gradientProgram[gradientProgram.length-1][1]);

   		eluentCompositionTimeGraph.autoScaleX();
   		eluentCompositionTimeGraph.autoScaleY();
    	
    	eluentCompositionTimeGraph.repaint();   
    	
   		deadTimeEluentCompositionGraph.autoScaleX();
   		deadTimeEluentCompositionGraph.autoScaleY();
    	
    	deadTimeEluentCompositionGraph.repaint();  
		
		this.standardsList.clear();
		for (int i = 0; i < standardsList.size(); i++)
		{
			if (standardsList.get(i).getUse())
				this.standardsList.add(standardsList.get(i));
		}
		
		
    	
    	if (this.instrumentDeadTime == 0)
    	{
    		String message = "<html><body><p style='width: 300px;'>Please enter your measured instrument dead time in " +
    						"the &quot;Instrument dead time&quot; box. Not entering " +
    						"an instrument dead time can cause significant error in your " +
    						"retention predictions unless your instrument dead time is " +
    						"exceedingly small.<br/>" +
    						"<br/>" +
    						"Do you wish to continue anyway?</p></body></html>";
    		Platform.runLater(new Runnable(){
	            @Override
	            public void run() {
	            	FXOptionPane.showMessageDialog(mainGrid.getScene().getWindow(), message, "High Variance", FXOptionPane.WARNING_MESSAGE);
	            }
	        });
    	}
    	
    	// Set the graph and flow profiles to contain the correct data
    	stage = 2;
    	
		NumberFormat formatter = new DecimalFormat("#0.0000");

    	eluentCompositionTimeGraph.RemoveAllSeries();
    	deadTimeEluentCompositionGraph.RemoveAllSeries();
		
        showSimpleGradient = false;
        showSimpleDeadTime = false;

        // Set the initial values of the mixing and nonmixing volumes
        this.mixingVolumeArray[0][1] = 0.000001;
        this.mixingVolumeArray[1][1] = 0.000001;
        this.nonMixingVolumeArray[0][1] = 0.000001;
        this.nonMixingVolumeArray[1][1] = 0.000001;
        this.interpolatedMixingVolume = new LinearInterpolationFunction(mixingVolumeArray);
        this.interpolatedNonMixingVolume = new LinearInterpolationFunction(nonMixingVolumeArray);
//	
//     // Set the table to contain the correct data
//    	
//    	for (int i = 0; i < standardsList.size(); i++)
//    	{
//    		if ((standardsList.get(i).getMeasuredRetentionTime() <= (Double)0.0
//    				|| standardsList.get(i).getUse() == false))
//    			continue;
//    		
//    		Object strCompoundName = standardsList.get(i).getName();
//    		Double dMeasuredRetentionTime = standardsList.get(i).getMeasuredRetentionTime();
//    		
//    		// Subtract the instrument dead time
//    		dMeasuredRetentionTime -= this.instrumentDeadTime;
//    		
//    		if (dMeasuredRetentionTime < 0)
//    			dMeasuredRetentionTime = (Double)0.0;
//    		
//    		Object[] newRow = {strCompoundName, formatter.format(dMeasuredRetentionTime + this.instrumentDeadTime), null, null};
//
//    		standardsList.get(i).setMeasuredRetentionTime(dMeasuredRetentionTime);
//    		standardsList.get(i).setPredictedRetentionTime(0.0);
//    	}

        // Find greatest dead volume
		double dVmMax = 0;
		
		for (int i = 0; i < this.initialDeadTimeArray.length; i++)
		{
			if (this.initialDeadTimeArray[i][1] > dVmMax)
			{
				dVmMax = this.initialDeadTimeArray[i][1];
			}
		}	  
		
		dVmMax *= this.flowRate / 60;
		
		plotXMax2 = this.standardsList.get(this.standardsList.size() - 1).getMeasuredRetentionTime()-instrumentDeadTime;
    	
    	// Here is where we set the value of m_dtstep
    	dtstep = plotXMax2 * 0.001;

    	int iIdealPlotIndexGradient = eluentCompositionTimeGraph.AddSeries("Ideal Gradient Program", Color.BLACK, 1, false, false);
    	iIdealPlotIndexDeadTime = deadTimeEluentCompositionGraph.AddSeries("Ideal Dead Time", Color.BLACK, 1, false, false);
    	
    	interpolatedGradientProgramSeries = eluentCompositionTimeGraph.AddSeries("Interpolated Gradient Program", Color.RED, 1, false, false);
	    gradientProgramMarkerSeries = eluentCompositionTimeGraph.AddSeries("Gradient Program Markers", Color.RED, 1, true, false);

	    simpleGradientSeries = eluentCompositionTimeGraph.AddSeries("Simple Gradient", Color.BLUE, 1, false, false);
    	
    	interpolatedFlowRateSeries = deadTimeEluentCompositionGraph.AddSeries("Interpolated Flow Rate", Color.RED, 1, false, false);
	    flowRateMarkerSeries = deadTimeEluentCompositionGraph.AddSeries("Flow Rate Markers", Color.RED, 1, true, false);

	    simpleDeadTimeSeries = deadTimeEluentCompositionGraph.AddSeries("Simple Dead Time", Color.BLUE, 1, false, false);

	    // Add in data points for the ideal gradient program series
	    
    	this.idealGradientProfileArray = new double[gradientProgram.length + 2][2];
    	int iPointCount = 0;

    	eluentCompositionTimeGraph.AddDataPoint(iIdealPlotIndexGradient, 0, (Double)gradientProgram[0][1]);	
    	this.idealGradientProfileArray[iPointCount][0] = 0.0;
    	this.idealGradientProfileArray[iPointCount][1] = (Double)gradientProgram[0][1];
    	dLastTime = 0;
		iPointCount++;
		
    	// Go through the gradient program table and create an array that contains solvent composition vs. time
		for (int i = 0; i < gradientProgram.length; i++)
		{
    		if ((Double)gradientProgram[i][0] > dLastTime)
    		{
    			double dTime = (Double)gradientProgram[i][0];
    			double dFractionB = (Double)gradientProgram[i][1];
    			
    	    	eluentCompositionTimeGraph.AddDataPoint(iIdealPlotIndexGradient, dTime * 60, dFractionB);	
				this.idealGradientProfileArray[iPointCount][0] = dTime;
				this.idealGradientProfileArray[iPointCount][1] = dFractionB;
    	    	iPointCount++;
    		
    	    	dLastTime = dTime;
    		}
		}
		
		eluentCompositionTimeGraph.AddDataPoint(iIdealPlotIndexGradient, plotXMax2 * 60, (Double)gradientProgram[gradientProgram.length - 1][1]);
		this.idealGradientProfileArray[iPointCount][0] = plotXMax2 * 60;
		this.idealGradientProfileArray[iPointCount][1] = (Double)gradientProgram[gradientProgram.length - 1][1];
    	iPointCount++;

		// Ideal series finished
		// Now cut it down to the correct size
		double tempArray[][] = new double[iPointCount][2];
		for (int i = 0; i < iPointCount; i++)
		{
			tempArray[i][0] = this.idealGradientProfileArray[i][0];
			tempArray[i][1] = this.idealGradientProfileArray[i][1];
		}
		this.idealGradientProfileArray = tempArray;
		
		// Make the interpolated gradient profile
    	this.interpolatedIdealGradientProfile = new LinearInterpolationFunction(this.idealGradientProfileArray);

    	// Add the initial interpolated hold-up time profile to the graph control
	    iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	deadTimeEluentCompositionGraph.AddDataPoint(iIdealPlotIndexDeadTime, dXPos * 100, this.initialInterpolatedDeadTimeProfile.getAt(dXPos));
	    }
	    
		// Create initial gradient and flow rate difference arrays
		gradientProfileDifferenceArray = new double[2][2];
		gradientProfileDifferenceArray[0][0] = 0;
		gradientProfileDifferenceArray[0][1] = 0;
		gradientProfileDifferenceArray[1][0] = plotXMax2;
		gradientProfileDifferenceArray[1][1] = 0;
		
		deadTimeDifferenceArray = new double [2][2];
		deadTimeDifferenceArray[0][0] = .05;
		deadTimeDifferenceArray[0][1] = 0;
		deadTimeDifferenceArray[1][0] = .95;
		deadTimeDifferenceArray[1][1] = 0;

		interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
		interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);


		// Calculate the gradients (even though we don't show them)
		calculateSimpleDeadTime();
		calculateSimpleGradient();
		
		updateGraphs(false);
    	
	}
	

	public double getInstrumentDeadTime() {
		return instrumentDeadTime;
	}

	public void setInstrumentDeadTime(double instrumentDeadTime) {
		this.instrumentDeadTime = instrumentDeadTime;
	}

	public void calculateSimpleDeadTime()
	{
		// Create new profile for the dead time offset
		int iNumPoints = 1000;
		double[][] dSimpleDeadTimeArray = new double[iNumPoints][2];
		for (int i = 0; i < iNumPoints; i++)
		{
	    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	dSimpleDeadTimeArray[i][0] = dXPos;
			dSimpleDeadTimeArray[i][1] = initialInterpolatedDeadTimeProfile.getAt(dXPos) + interpolatedDeadTimeDifferenceProfile.getAt(dXPos);
		}
		interpolatedSimpleDeadTime = new LinearInterpolationFunction(dSimpleDeadTimeArray);
	}
	
	// New version handles change in mixing/nonmixing volume as a function of solvent composition
	public void calculateSimpleGradient()
	{
		int iNumPoints = 10000;
		// Create an array for the simple gradient
		simpleGradientArray = new double[iNumPoints][2];
				
		// Initialize the solvent mixer composition to that of the initial solvent composition
	//	double dMixerComposition = ((Double) gradientProgramgetValueAt(0, 1)).doubleValue(); //tmgradientprogram is an array with {{0.0, 5.0},{5.0, 95.0}}
		double dMixerComposition = gradientProgram[0][1];
		//double dFinalTime = (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) + ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iGradientTableLength - 1, 0)).doubleValue();
		double dFinalTime = plotXMax2; // in min
		double dTimeStep = dFinalTime / (iNumPoints - 1);
		
		// Start at time 0
		double dTime = 0;
		
		// Enter new values into the output array
		simpleGradientArray[0][0] = dTime;
		simpleGradientArray[0][1] = dMixerComposition;
		
		for (int i = 0; i < iNumPoints; i++)
		{
			// Find the current time
			dTime = i * dTimeStep;
			
			// Find the solvent composition coming into the nonmixing volume
			double dIncomingSolventCompositionToNonMixingVolume = interpolatedIdealGradientProfile.getAt(dTime);

			// Now find the solvent composition coming into the mixing volume
			double dIncomingSolventCompositionToMixingVolume = 0;
			if (dTime < interpolatedNonMixingVolume.getAt(dIncomingSolventCompositionToNonMixingVolume) / flowRate)
			{
				dIncomingSolventCompositionToMixingVolume = interpolatedIdealGradientProfile.getAt(0);
			}
			else
			{
				double dTimeOfSolventComposition = dTime - (interpolatedNonMixingVolume.getAt(dIncomingSolventCompositionToNonMixingVolume) / flowRate);
				dIncomingSolventCompositionToMixingVolume = interpolatedIdealGradientProfile.getAt(dTimeOfSolventComposition);
			}
			
			// Figure out the volume of solvent B in the mixer
			double dSolventBInMixer = dMixerComposition * interpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume);//m_dMixingVolume;
							
			// Now push out a step's worth of volume from the mixer
			dSolventBInMixer -= (flowRate * dTimeStep) * dMixerComposition;
			
			// dSolventBInMixer could be negative if the volume pushed out of the mixer is greater than the total volume of the mixer
			if (dSolventBInMixer < 0)
				dSolventBInMixer = 0;
			
			// Now add a step's worth of new volume from the pump
			// First, find which two data points we are between
			// Find the last data point that isn't greater than our current time
			
			if ((flowRate * dTimeStep) < interpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume))
				dSolventBInMixer += (flowRate * dTimeStep) * dIncomingSolventCompositionToMixingVolume;
			else
			{
				// The amount of solvent entering the mixing chamber is larger than the mixing chamber. Just set the solvent composition in the mixer to that of the mobile phase.
				dSolventBInMixer = interpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume) * dIncomingSolventCompositionToMixingVolume;
			}
			
			// Calculate the new solvent composition in the mixing volume
			if ((flowRate * dTimeStep) < interpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume))
				dMixerComposition = dSolventBInMixer / interpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume);
			else
				dMixerComposition = dIncomingSolventCompositionToMixingVolume;
			
			// Enter new values into the output array
			simpleGradientArray[i][0] = dTime - ((interpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume) - interpolatedMixingVolume.getAt(0)) / flowRate);
			simpleGradientArray[i][1] = dMixerComposition;
		}
		
		interpolatedSimpleGradient = new LinearInterpolationFunction(simpleGradientArray);
	}
	
	private void updateGraphsWithIdealProfiles()
	{
		eluentCompositionTimeGraph.RemoveSeries(m_iIdealPlotIndexGrad);
		deadTimeEluentCompositionGraph.RemoveSeries(m_iIdealPlotIndexDeadTime);

    	m_iIdealPlotIndexGrad = eluentCompositionTimeGraph.AddSeries("Ideal Gradient Program", Color.rgb(0, 0, 0), 1, false, false);
    	m_iIdealPlotIndexDeadTime = deadTimeEluentCompositionGraph.AddSeries("Ideal Dead Time", Color.rgb(0, 0, 0), 1, false, false);
    	
		// Plot the ideal temperature profile
    	for (int i = 0; i < idealGradientProfileArray.length; i++)
    	{
    		this.eluentCompositionTimeGraph.AddDataPoint(m_iIdealPlotIndexGrad, idealGradientProfileArray[i][0] * 60, idealGradientProfileArray[i][1]);
    	}
    	
    	// Add the initial interpolated hold-up time profile to the graph control
	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = ((double)i / (double)(iNumPoints - 1)) * (plotXMax2 * 60);
	    	deadTimeEluentCompositionGraph.AddDataPoint(m_iIdealPlotIndexDeadTime, dXPos, initialInterpolatedDeadTimeProfile.getAt(dXPos));
	    }
	}
	
	public void updateGraphs(boolean bAlsoUpdateTable)
	{
		synchronized(eluentCompositionTimeGraph.lockObject)
		{
		synchronized(deadTimeEluentCompositionGraph.lockObject)
		{
		
		// Update the graphs with the new m_dGradientArray markers and the m_InterpolatedGradient (and the same with the flow graph)
		eluentCompositionTimeGraph.RemoveSeries(interpolatedGradientProgramSeries);
		eluentCompositionTimeGraph.RemoveSeries(gradientProgramMarkerSeries);
	
		eluentCompositionTimeGraph.RemoveSeries(simpleGradientSeries);		
		
		deadTimeEluentCompositionGraph.RemoveSeries(interpolatedFlowRateSeries);
		deadTimeEluentCompositionGraph.RemoveSeries(flowRateMarkerSeries);
		
		deadTimeEluentCompositionGraph.RemoveSeries(simpleDeadTimeSeries);
		
	    if (showSimpleGradient)
	    	simpleGradientSeries = eluentCompositionTimeGraph.AddSeries("Simple Gradient", Color.rgb(170,170,170), 1, false, false);
	    interpolatedGradientProgramSeries = eluentCompositionTimeGraph.AddSeries("Interpolated Gradient", Color.rgb(225,0,0), 1, false, false);
	    gradientProgramMarkerSeries = eluentCompositionTimeGraph.AddSeries("Gradient Markers", Color.rgb(225,0,0), 1, true, false);

	    if (showSimpleDeadTime)
	    	simpleDeadTimeSeries = deadTimeEluentCompositionGraph.AddSeries("Simple Dead Time", Color.rgb(170,170,170), 1, false, false);
	    interpolatedFlowRateSeries = deadTimeEluentCompositionGraph.AddSeries("Interpolated Flow", Color.rgb(225,0,0), 1, false, false);
	    flowRateMarkerSeries = deadTimeEluentCompositionGraph.AddSeries("Flow Rate Markers", Color.rgb(225,0,0), 1, true, false);

	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = ((double)i / (double)(iNumPoints - 1)) * (plotXMax2 * 60);
	    	eluentCompositionTimeGraph.AddDataPoint(interpolatedGradientProgramSeries, dXPos, this.interpolatedSimpleGradient.getAt(dXPos / 60) + interpolatedGradientDifferenceProfile.getAt(dXPos / 60));
	    	if (showSimpleGradient)
	    		eluentCompositionTimeGraph.AddDataPoint(simpleGradientSeries, dXPos, interpolatedSimpleGradient.getAt(dXPos / 60));

	    	dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	deadTimeEluentCompositionGraph.AddDataPoint(interpolatedFlowRateSeries, dXPos * 100, this.initialInterpolatedDeadTimeProfile.getAt(dXPos) + this.interpolatedDeadTimeDifferenceProfile.getAt(dXPos));
	    	if (showSimpleDeadTime)
	    		deadTimeEluentCompositionGraph.AddDataPoint(simpleDeadTimeSeries, dXPos * 100, interpolatedSimpleDeadTime.getAt(dXPos));
	    }
	    
	    for (int i = 0; i < gradientProfileDifferenceArray.length; i++)
	    {
	    	eluentCompositionTimeGraph.AddDataPoint(gradientProgramMarkerSeries, gradientProfileDifferenceArray[i][0] * 60, this.interpolatedSimpleGradient.getAt(gradientProfileDifferenceArray[i][0]) + gradientProfileDifferenceArray[i][1]);
	    }
		
	    for (int i = 0; i < this.deadTimeDifferenceArray.length; i++)
	    {
	    	double dXPos = deadTimeDifferenceArray[i][0];
	    	deadTimeEluentCompositionGraph.AddDataPoint(flowRateMarkerSeries, deadTimeDifferenceArray[i][0] * 100, this.initialInterpolatedDeadTimeProfile.getAt(dXPos) + this.interpolatedDeadTimeDifferenceProfile.getAt(dXPos));
	    }
	    
	    eluentCompositionTimeGraph.AutoScaleToSeries(interpolatedGradientProgramSeries);
	    eluentCompositionTimeGraph.autoScaleY();
	    deadTimeEluentCompositionGraph.autoScaleX();
	    deadTimeEluentCompositionGraph.autoScaleY();
     
	    eluentCompositionTimeGraph.repaint();
	    deadTimeEluentCompositionGraph.repaint();	    
		}
		}
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public boolean isInjectionMode() {
		return isInjectionMode;
	}

	public void setInjectionMode(boolean isInjectionMode) {
		this.isInjectionMode = isInjectionMode;
	}

	public void writeSaveData(SaveData.BackCalculateSaveData saveData)
	{
		saveData.columnLength = columnLength;
		saveData.innerDiameter = innerDiameter;
		saveData.flowRate = flowRate;
		saveData.fileName = fileName;
		saveData.standardsList = standardsList;
		saveData.instrumentDeadTime = instrumentDeadTime;
		saveData.gradientProgramInConventionalForm = gradientProgram;
		saveData.tStep = dtstep;
		saveData.m_dIdealGradientProfileArray = idealGradientProfileArray;
		saveData.m_dGradientProfileDifferenceArray = gradientProfileDifferenceArray;
		saveData.m_dSimpleGradientProfileArray = simpleGradientArray;
		saveData.m_dDeadTimeArray = initialDeadTimeArray;
		saveData.m_dDeadTimeDifferenceArray = deadTimeDifferenceArray;
		saveData.m_dExpectedErrorArray = expectedErrorArray;
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
	
	public void writeSaveData(InjectionSaveData.BackCalculateSaveData saveData)
	{
		saveData.columnLength = columnLength;
		saveData.innerDiameter = innerDiameter;
		saveData.flowRate = flowRate;
		saveData.fileName = fileName;
		saveData.standardsList = standardsList;
		saveData.instrumentDeadTime = instrumentDeadTime;
		saveData.gradientProgramInConventionalForm = gradientProgram;
		saveData.tStep = dtstep;
		saveData.m_dIdealGradientProfileArray = idealGradientProfileArray;
		saveData.m_dGradientProfileDifferenceArray = gradientProfileDifferenceArray;
		saveData.m_dSimpleGradientProfileArray = simpleGradientArray;
		saveData.m_dDeadTimeArray = initialDeadTimeArray;
		saveData.m_dDeadTimeDifferenceArray = deadTimeDifferenceArray;
		saveData.m_dExpectedErrorArray = expectedErrorArray;
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
		columnLength = saveData.columnLength;
		innerDiameter = saveData.innerDiameter;
		flowRate = saveData.flowRate;
		fileName = saveData.fileName;
		standardsList = saveData.standardsList;
		gradientProgram = saveData.gradientProgramInConventionalForm;
		dtstep = saveData.tStep;
		instrumentDeadTime = saveData.instrumentDeadTime;
		
		idealGradientProfileArray = saveData.m_dIdealGradientProfileArray;
		if (idealGradientProfileArray != null)
			this.interpolatedIdealGradientProfile = new LinearInterpolationFunction(this.idealGradientProfileArray);
		
    	gradientProfileDifferenceArray = saveData.m_dGradientProfileDifferenceArray;
		if (gradientProfileDifferenceArray != null)
			this.interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
		
		simpleGradientArray = saveData.m_dSimpleGradientProfileArray;
		if (simpleGradientArray != null)
			this.interpolatedSimpleGradient = new LinearInterpolationFunction(simpleGradientArray);

		initialDeadTimeArray = saveData.m_dDeadTimeArray;
		if (initialDeadTimeArray != null)
			this.initialInterpolatedDeadTimeProfile = new InterpolationFunction(initialDeadTimeArray);
		
		deadTimeDifferenceArray = saveData.m_dDeadTimeDifferenceArray;
		if(deadTimeDifferenceArray != null){
			this.interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
		}
		//setStandardsList(saveData.standardsList);
		expectedErrorArray = saveData.m_dExpectedErrorArray;
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

		if (interpolatedSimpleGradient != null && idealGradientProfileArray != null)
		{
			//updateGraphsWithIdealProfiles();
			updateGraphs(false);
			updateTestCompoundTable();
		}
	}
	
	public void loadSaveData(InjectionSaveData.BackCalculateSaveData saveData)
	{
		columnLength = saveData.columnLength;
		innerDiameter = saveData.innerDiameter;
		flowRate = saveData.flowRate;
		fileName = saveData.fileName;
		standardsList = saveData.standardsList;
		gradientProgram = saveData.gradientProgramInConventionalForm;
		dtstep = saveData.tStep;
		instrumentDeadTime = saveData.instrumentDeadTime;
		plotXMax2 = standardsList.get(standardsList.size()-1).getMeasuredRetentionTime()-instrumentDeadTime;
		
		idealGradientProfileArray = saveData.m_dIdealGradientProfileArray;
		if (idealGradientProfileArray != null)
			this.interpolatedIdealGradientProfile = new LinearInterpolationFunction(this.idealGradientProfileArray);
		
    	gradientProfileDifferenceArray = saveData.m_dGradientProfileDifferenceArray;
		if (gradientProfileDifferenceArray != null)
			this.interpolatedGradientDifferenceProfile = new LinearInterpolationFunction(gradientProfileDifferenceArray);
		
		simpleGradientArray = saveData.m_dSimpleGradientProfileArray;
		if (simpleGradientArray != null)
			this.interpolatedSimpleGradient = new LinearInterpolationFunction(simpleGradientArray);

		initialDeadTimeArray = saveData.m_dDeadTimeArray;
		if (initialDeadTimeArray != null)
			this.initialInterpolatedDeadTimeProfile = new InterpolationFunction(initialDeadTimeArray);
		
		deadTimeDifferenceArray = saveData.m_dDeadTimeDifferenceArray;
		if(deadTimeDifferenceArray != null){
			this.interpolatedDeadTimeDifferenceProfile = new InterpolationFunction(deadTimeDifferenceArray);
		}
		//setStandardsList(saveData.standardsList);
		expectedErrorArray = saveData.m_dExpectedErrorArray;
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

		if (interpolatedSimpleGradient != null && idealGradientProfileArray != null)
		{
			//updateGraphsWithIdealProfiles();
			showSimpleDeadTime = true;
			showSimpleGradient = true;
			calculateSimpleDeadTime();
			updateGraphs(false);
			updateGraphsWithIdealProfiles();
			updateTestCompoundTable();
		}
	}

	

}
