package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.Vector;

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

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;
import org.retentionprediction.lcdatabasebuilderfx.ui.StepFourPaneController.StepFourPaneControllerListener;
import org.retentionprediction.lcdatabasebuilderfx.business.*;

import boswell.graphcontrolfx.GraphControlFX;
import boswell.peakfinderlcfx.GlobalsDan;

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
			return GlobalsDan.StandardCompoundsNameArray.length;
		}
		
		public boolean loadCompoundInfo(int iIndex)
		{
			iCompoundIndex = iIndex;
			
			strCompoundName = GlobalsDan.StandardCompoundsNameArray[iIndex];
			
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
	
	public double[][] gradientProgramArray;
    public double[][] m_dIdealGradientProfileArray;
    public LinearInterpolationFunction m_InterpolatedIdealGradientProfile; //Linear
    public double[][] m_dGradientProfileDifferenceArray;
    public LinearInterpolationFunction m_InterpolatedGradientDifferenceProfile;

    public double[][] m_dInitialDeadTimeArray;
    public InterpolationFunction m_InitialInterpolatedDeadTimeProfile;
    public double[][] m_dDeadTimeDifferenceArray;
    public InterpolationFunction m_InterpolatedDeadTimeDifferenceProfile;
    
    public InterpolationFunction[] m_StandardIsocraticDataInterpolated;

    // For the mixing/non-mixing volume
	public double[][] m_dSimpleGradientArray;
	public LinearInterpolationFunction m_InterpolatedSimpleGradient = null;
	public LinearInterpolationFunction m_InterpolatedSimpleDeadTime = null;
	
    public int m_iInterpolatedGradientProgramSeries = 0;
    public int m_iGradientProgramMarkerSeries = 0;
    public int m_iInterpolatedFlowRateSeries = 0;
    public int m_iFlowRateMarkerSeries = 0;
    public int m_iSimpleGradientSeries = 0;
    public int m_iSimpleDeadTimeSeries = 0;
    
    // Mixing volume in mL
    //public double m_dMixingVolume = 0.001;
    //public double m_dNonMixingVolume = 0.001;
    
    // Mixing volume as a function of solvent composition
    public double[][] m_dMixingVolumeArray = {{0.0, 0.001}, {100.0, 0.001}};
    // Non-mixing volume as a function of solvent composition
    public double[][] m_dNonMixingVolumeArray = {{0.0, 0.001}, {100.0, 0.001}};
    public LinearInterpolationFunction m_InterpolatedMixingVolume = null;
    public LinearInterpolationFunction m_InterpolatedNonMixingVolume = null;
    
    public double m_dPlotXMax = 0;
    public double m_dPlotXMax2 = 0;
    
    public boolean m_bDoNotChangeTable = false;
    public final double m_dGoldenRatio = (1 + Math.sqrt(5)) / 2;
    public boolean m_bShowSimpleGradient = false;
    public boolean m_bShowSimpleDeadTime = false;

    public double m_dInstrumentDeadTime = 0; // in min

    public double[] m_dExpectedErrorArray;
    public PredictedRetentionObject[] m_PredictedRetentionTimes;
    public double m_dConfidenceInterval = 0.99;

	public Vector<Compound> m_vectCompound = new Vector<Compound>();
	
    public int m_iStage = 1;
    public double m_dtstep = 0.01;
    public double m_dVariance = 0;
    public boolean m_bNoFullBackcalculation = false;
    

	public int m_iStationaryPhase = 0;
	public double m_dColumnLength = 0.1; // in mm
	public double m_dColumnInnerDiameter = 2.1; // in mm
	public double m_dProgramTime = 20;
	public double m_dFlowRate = 1; // in mL/min
	
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
    
	public BackCalculateControllerListener getBackcalculateListener() {
		return backcalculateControllerListener;
	}

	public void setBackCalculateControllerListener(BackCalculateControllerListener thisListener)
	{
		backcalculateControllerListener = thisListener;
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
							//onFindRetentionTimesAutomaticallyButtonMouseClicked(event);
						}
					});

			stepFourPaneController.setStep4PaneControllerListener((StepFourPaneControllerListener)this);

			eluentCompositionTimeGraph = new GraphControlFX();
			eluentCompositionTimeGraph.setControlsEnabled(false);
			eluentCompositionTimeGraph.setYAxisTitle("Eluent Composition (%B)");
			eluentCompositionTimeGraph.setYAxisBaseUnit("\u00B0C", "\u00B0C");
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
			deadTimeEluentCompositionGraph = new GraphControlFX();
			deadTimeEluentCompositionGraph.setControlsEnabled(false);
			deadTimeEluentCompositionGraph.setYAxisTitle("Uracil Dead Time (s)");
			deadTimeEluentCompositionGraph.setYAxisBaseUnit("seconds", "s");
			//holdUpProfileGraph.setYAxisRangeLimits(-1E15d, 1E15d);
			deadTimeEluentCompositionGraph.setYAxisScientificNotation(true);
			deadTimeEluentCompositionGraph.setYAxisRangeIndicatorsVisible(false);
			deadTimeEluentCompositionGraph.setAutoScaleY(true);
	        
			deadTimeEluentCompositionGraph.setXAxisType(false);
			deadTimeEluentCompositionGraph.setXAxisRangeIndicatorsVisible(false);
			deadTimeEluentCompositionGraph.setXAxisTitle("Eluent Composition (%B)");
			deadTimeEluentCompositionGraph.setXAxisBaseUnit("\u00B0C", "\u00B0C");
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
		buttonNextStep.setDisable(true);
		
		eluentCompositionTimeGraph.RemoveAllSeries();
		deadTimeEluentCompositionGraph.RemoveAllSeries();

		// Add the step3Pane to start
		mainGrid.getChildren().removeAll(step3Pane, step4Pane);
		mainGrid.add(step3Pane, 1, 0);
	}

	public void switchToStep4() {
		// TODO Auto-generated method stub
		
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
    			StandardCompound newStandardCompound = new StandardCompound(standardsList.get(i).getUse(), standardsList.get(i).getName(), standardsList.get(i).getMz(), standardsList.get(i).getMeasuredRetentionTime(), standardsList.get(i).getPredictedRetentionTime(), standardsList.get(i).getIndex());
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
    	
    	//TODO: IImplement a updateGraph method
    	
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
							dPhiC = m_InterpolatedSimpleGradient.getAt(dTotalTime - dIntegral) / 100;
						else
							dPhiC = (m_InterpolatedSimpleGradient.getAt(dTotalTime - dIntegral) + m_InterpolatedGradientDifferenceProfile.getAt(dTotalTime - dIntegral)) / 100;

						dCurVal = dtstep / (Math.pow(10, m_StandardIsocraticDataInterpolated[iCompound].getAt(dPhiC)));
						dt0 = (m_InitialInterpolatedDeadTimeProfile.getAt(dPhiC) + m_InterpolatedDeadTimeDifferenceProfile.getAt(dPhiC)) / 60;
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
	    	double dHoldUpRange = m_dInitialDeadTimeArray[0][1];
	    	
	    	for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
	    	{
	        	if (i < 2)
	        		continue;
	        	
	        	double dTime2 = m_dDeadTimeDifferenceArray[i][0];
	        	double dHoldUp2 = m_dDeadTimeDifferenceArray[i][1];
	        	double dTime1 = m_dDeadTimeDifferenceArray[i - 1][0];
	        	double dHoldUp1 = m_dDeadTimeDifferenceArray[i - 1][1];
	        	double dTime0 = m_dDeadTimeDifferenceArray[i - 2][0];
	        	double dHoldUp0 = m_dDeadTimeDifferenceArray[i - 2][1];
	        	
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
	    	
	    	for (int i = 0; i < m_dGradientProfileDifferenceArray.length; i++)
	    	{
	        	if (i < 2)
	        		continue;
	        	
	        	double dTime2 = m_dGradientProfileDifferenceArray[i][0];
	        	double dTemp2 = m_dGradientProfileDifferenceArray[i][1];
	        	double dTime1 = m_dGradientProfileDifferenceArray[i - 1][0];
	        	double dTemp1 = m_dGradientProfileDifferenceArray[i - 1][1];
	        	double dTime0 = m_dGradientProfileDifferenceArray[i - 2][0];
	        	double dTemp0 = m_dGradientProfileDifferenceArray[i - 2][1];
	        	
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
				dLastVolumeGuess = m_dNonMixingVolumeArray[iIndex][1];
			else
				dLastVolumeGuess = m_dMixingVolumeArray[iIndex][1];
			
			// Find bounds
			if (bNonMixingVolume)
				x1 = m_dNonMixingVolumeArray[iIndex][1];
			else
				x1 = m_dMixingVolumeArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), true);
			
			x2 = x1 + dStep;
			if (bNonMixingVolume)
				m_dNonMixingVolumeArray[iIndex][1] = x2;
			else
				m_dMixingVolumeArray[iIndex][1] = x2;
			m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), true);
			
			if (dRetentionErrorX2 < dRetentionErrorX1)
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = ((x3 - x1) * m_dGoldenRatio) + x3;
				
				if (bNonMixingVolume)
					m_dNonMixingVolumeArray[iIndex][1] = x2;
				else
					m_dMixingVolumeArray[iIndex][1] = x2;
				m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), true);

				while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastVolumeGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					
					x2 = ((x3 - x1) * m_dGoldenRatio) + x3;
					
					if (bNonMixingVolume)
						m_dNonMixingVolumeArray[iIndex][1] = x2;
					else
						m_dMixingVolumeArray[iIndex][1] = x2;
					m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
					m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
					calculateSimpleGradient();
					dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), true);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				
				x1 = x3 - (x2 - x3) * m_dGoldenRatio;
				
				if (bNonMixingVolume)
					m_dNonMixingVolumeArray[iIndex][1] = x1;
				else
					m_dMixingVolumeArray[iIndex][1] = x1;
				m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), true);

				while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastVolumeGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;

					x1 = x3 - (x2 - x3) * m_dGoldenRatio;
					
					if (bNonMixingVolume)
						m_dNonMixingVolumeArray[iIndex][1] = x1;
					else
						m_dMixingVolumeArray[iIndex][1] = x1;
					m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
					m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
					calculateSimpleGradient();
					dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), true);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastVolumeGuess + dMaxChangeAtOnce)
			{
				if (bNonMixingVolume)
					m_dNonMixingVolumeArray[iIndex][1] = dLastVolumeGuess + dMaxChangeAtOnce;
				else
					m_dMixingVolumeArray[iIndex][1] = dLastVolumeGuess + dMaxChangeAtOnce;
				m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), true);

				return dRetentionError;
			}
			
			if (x1 < dLastVolumeGuess - dMaxChangeAtOnce)
			{
				if (dLastVolumeGuess - dMaxChangeAtOnce < 0.00001)
				{
					if (bNonMixingVolume)
						m_dNonMixingVolumeArray[iIndex][1] = 0.00001;
					else
						m_dMixingVolumeArray[iIndex][1] = 0.00001;
				}
				else
				{
					if (bNonMixingVolume)
						m_dNonMixingVolumeArray[iIndex][1] = dLastVolumeGuess - dMaxChangeAtOnce;
					else
						m_dMixingVolumeArray[iIndex][1] = dLastVolumeGuess - dMaxChangeAtOnce;
				}
				m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
				
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
					x4 = x3 + (2 - m_dGoldenRatio) * (x2 - x3);
				}
				else 
				{
					// x1 and x3, so x4 must be placed between them
					x4 = x3 - (2 - m_dGoldenRatio) * (x3 - x1);
				}

				if (bNonMixingVolume)
					m_dNonMixingVolumeArray[iIndex][1] = x4;
				else
					m_dMixingVolumeArray[iIndex][1] = x4;
				m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
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
					m_dNonMixingVolumeArray[iIndex][1] = 0.00001;
				else
					m_dMixingVolumeArray[iIndex][1] = 0.00001;
				m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), true);			
			}
			else
			{
				if (bNonMixingVolume)
					m_dNonMixingVolumeArray[iIndex][1] = x3;
				else
					m_dMixingVolumeArray[iIndex][1] = x3;
				m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
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
			
			double dLastTempGuess = m_dGradientProfileDifferenceArray[iIndex][1];
			
			// Find bounds
			x1 = m_dGradientProfileDifferenceArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), false);
			dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
			
			x2 = x1 + dStep;
			m_dGradientProfileDifferenceArray[iIndex][1] = x2;
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), false);
			dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
			
			if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * m_dGoldenRatio + x3;
				
				m_dGradientProfileDifferenceArray[iIndex][1] = x2;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), false);
				dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
				

				while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastTempGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					dAngleErrorX3 = dAngleErrorX2;
					
					x2 = (x3 - x1) * m_dGoldenRatio + x3;
					
					m_dGradientProfileDifferenceArray[iIndex][1] = x2;
					m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
					dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), false);
					dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * m_dGoldenRatio;
				
				m_dGradientProfileDifferenceArray[iIndex][1] = x1;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), false);
				dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);

				while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastTempGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;
					dAngleErrorX3 = dAngleErrorX1;

					x1 = x3 - (x2 - x3) * m_dGoldenRatio;
					
					m_dGradientProfileDifferenceArray[iIndex][1] = x1;
					m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
					dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), false);
					dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastTempGuess + dMaxChangeAtOnce)
			{
				if (dLastTempGuess + dMaxChangeAtOnce > 100)
					m_dGradientProfileDifferenceArray[iIndex][1] = 100;
				else
					m_dGradientProfileDifferenceArray[iIndex][1] = dLastTempGuess + dMaxChangeAtOnce;
				
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);

				return dRetentionError;
			}
			
			if (x1 < dLastTempGuess - dMaxChangeAtOnce)
			{
				if (dLastTempGuess - dMaxChangeAtOnce < -100)
					m_dGradientProfileDifferenceArray[iIndex][1] = -100;
				else
					m_dGradientProfileDifferenceArray[iIndex][1] = dLastTempGuess - dMaxChangeAtOnce;
				
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);
				
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

				
				m_dGradientProfileDifferenceArray[iIndex][1] = x4;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionErrorX4 = calcRetentionError(m_dtstep, standardsList.size(), false);
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
				m_dGradientProfileDifferenceArray[iIndex][1] = 100;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);			
			}
			else if (x3 < -100)
			{
				m_dGradientProfileDifferenceArray[iIndex][1] = -100;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);			
			}
			else
			{
				m_dGradientProfileDifferenceArray[iIndex][1] = x3;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
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
			
			double dLastFGuess = m_dDeadTimeDifferenceArray[iIndex][1];

			// Find bounds
			x1 = m_dDeadTimeDifferenceArray[iIndex][1];
			dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), false);
			dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);
			
			x2 = x1 + dStep;
			m_dDeadTimeDifferenceArray[iIndex][1] = x2;
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), false);
			dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);
			
			if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
			{
				// We're going in the right direction
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * m_dGoldenRatio + x3;
				
				m_dDeadTimeDifferenceArray[iIndex][1] = x2;
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), false);
				dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);

				while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastFGuess + dMaxChangeAtOnce)
				{
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					
					x3 = x2;
					dRetentionErrorX3 = dRetentionErrorX2;
					dAngleErrorX3 = dAngleErrorX2;
					
					x2 = (x3 - x1) * m_dGoldenRatio + x3;
					
					m_dDeadTimeDifferenceArray[iIndex][1] = x2;
					m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
					dRetentionErrorX2 = calcRetentionError(m_dtstep, standardsList.size(), false);
					dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);
				}
			}
			else
			{
				// We need to go in the opposite direction
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * m_dGoldenRatio;
				
				m_dDeadTimeDifferenceArray[iIndex][1] = x1;
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), false);
				dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);

				while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastFGuess - dMaxChangeAtOnce)
				{
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x1;
					dRetentionErrorX3 = dRetentionErrorX1;
					dAngleErrorX3 = dAngleErrorX1;
					
					x1 = x3 - (x2 - x3) * m_dGoldenRatio;
					
					m_dDeadTimeDifferenceArray[iIndex][1] = x1;
					m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
					dRetentionErrorX1 = calcRetentionError(m_dtstep, standardsList.size(), false);
					dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (x2 > dLastFGuess + dMaxChangeAtOnce)
			{
				m_dDeadTimeDifferenceArray[iIndex][1] = dLastFGuess + dMaxChangeAtOnce;
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);
				
				return dRetentionError;
			}
			
			if (x1 < dLastFGuess - dMaxChangeAtOnce)
			{
				m_dDeadTimeDifferenceArray[iIndex][1] = dLastFGuess - dMaxChangeAtOnce;
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);

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
				
				m_dDeadTimeDifferenceArray[iIndex][1] = x4;
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorX4 = calcRetentionError(m_dtstep, standardsList.size(), false);
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
			m_dDeadTimeDifferenceArray[iIndex][1] = x3;
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
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
			double dUpperBound = (m_dDeadTimeDifferenceArray[0][1] + m_dInitialDeadTimeArray[0][1]) * (1.0 + (dMaxPercentChangeAtOnce / 100.0));
			double dLowerBound = (m_dDeadTimeDifferenceArray[0][1] + m_dInitialDeadTimeArray[0][1]) * (1.0 - (dMaxPercentChangeAtOnce / 100.0));
			
			// Find bounds
			F1 = m_dDeadTimeDifferenceArray[0][1];
			dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

			F2 = F1 + dStep;
			
			double dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}

			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			
			if (dRetentionErrorF2 < dRetentionErrorF1)
			{
				// We're going in the right direction
				F3 = F2;
				dRetentionErrorF3 = dRetentionErrorF2;
				
				F2 = (F3 - F1) * m_dGoldenRatio + F3;
				
				dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

				while (dRetentionErrorF2 < dRetentionErrorF3 && F2 + m_dInitialDeadTimeArray[0][1] < dUpperBound)
				{
					F1 = F3;
					dRetentionErrorF1 = dRetentionErrorF3;
					F3 = F2;
					dRetentionErrorF3 = dRetentionErrorF2;
					
					F2 = (F3 - F1) * m_dGoldenRatio + F3;
					
					dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
					for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
					{
						m_dDeadTimeDifferenceArray[i][1] += (m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
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
				
				F1 = F3 - (F2 - F3) * m_dGoldenRatio;
				
				dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

				while (dRetentionErrorF1 < dRetentionErrorF3 && F1 + m_dInitialDeadTimeArray[0][1] > dLowerBound)
				{
					F2 = F3;
					dRetentionErrorF2 = dRetentionErrorF3;
					F3 = F1;
					dRetentionErrorF3 = dRetentionErrorF1;
					
					F1 = F3 - (F2 - F3) * m_dGoldenRatio;
					
					dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
					for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
					{
						m_dDeadTimeDifferenceArray[i][1] += (m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
					}
					m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
					dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
				}
			}
			
			// Now we have our bounds (x1 to x2) and the results of one guess (x3)
			if (F2 + m_dInitialDeadTimeArray[0][1] > dUpperBound)
			{
				dPercentDiff = (dUpperBound - m_dInitialDeadTimeArray[0][1]) / (m_dInitialDeadTimeArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] = m_dInitialDeadTimeArray[i][1] * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);
				
				return dRetentionError;
			}
			
			if (F1 + m_dInitialDeadTimeArray[0][1] < dLowerBound)
			{
				dPercentDiff = (dLowerBound - m_dInitialDeadTimeArray[0][1]) / (m_dInitialDeadTimeArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] = m_dInitialDeadTimeArray[i][1] * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);

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
					F4 = F3 + (2 - m_dGoldenRatio) * (F2 - F3);
				}
				else 
				{
					// F1 and F3, so F4 must be placed between them
					F4 = F3 - (2 - m_dGoldenRatio) * (F3 - F1);
				}

				
				dPercentDiff = (F4 - m_dDeadTimeDifferenceArray[0][1]) / (m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
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
			dPercentDiff = (F3 - m_dDeadTimeDifferenceArray[0][1]) / (m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			
			//dRetentionError = dRetentionErrorF3;
			
			// Calculate retention error with all of the standards
			dRetentionError = calcRetentionError(m_dtstep, standardsList.size(), false);

			return dRetentionError;
	 	}

		
		
		
		// Iterative - in sequence, *Golden* Sectioning Search algorithm
	    // Start by optimizing the entire dead time error profile.
		public void backCalculate(boolean bDeadTimeProfileFirst)
		{
			if (true)
				return;
			long starttime = System.currentTimeMillis();
			m_bNoFullBackcalculation = false;
			
			// Phase I conditions:
			updateProgress(-1, 0);
			updateMessage("Determining gradient delay volume...");
			
			boolean bBackCalculateSimpleGradientProfile = true;
			boolean bBackCalculateDeadTimeProfileOffset = false;
			boolean bBackCalculateGradientProfile = false;
			boolean bBackCalculateDeadTimeProfile = false;
			double dGradientProfileAngleWeight = 0;
			double dDeadTimeProfileAngleWeight = 0;
			int iNumCompoundsToUseForDeadTimeOffset = standardsList.size(); // 2;
	    	m_dtstep = m_dPlotXMax2 * 0.01;
			boolean bSkipTerminationTest = true;

			NumberFormat formatter1 = new DecimalFormat("#0.000000");
			NumberFormat formatter2 = new DecimalFormat("0.0000E0");
			NumberFormat formatter3 = new DecimalFormat("0.000");
			NumberFormat percentFormatter = new DecimalFormat("0.00");
			
			// Step #1: Create interpolating functions for the isocratic data of each gradient calibration solute
			m_StandardIsocraticDataInterpolated = new InterpolationFunction[GlobalsDan.StandardIsocraticDataArray.length];
			
			for (int i = 0; i < m_StandardIsocraticDataInterpolated.length; i++)
			{
				Integer iIndex = (Integer) standardsList.get(i).getIndex();
				m_StandardIsocraticDataInterpolated[i] = new InterpolationFunction(GlobalsDan.StandardIsocraticDataArray[iIndex]);
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
					
					for (int i = 0; i < m_dNonMixingVolumeArray.length; i++)
					{
						dRetentionError = goldenSectioningSearchSimpleGradientProfile(i, true, dVolumeStep, dPercentBPrecision, dMaxChangeAtOnce);
					}
					
					for (int i = 0; i < m_dMixingVolumeArray.length; i++)
					{
						dRetentionError = goldenSectioningSearchSimpleGradientProfile(i, false, dVolumeStep, dPercentBPrecision, dMaxChangeAtOnce);
					}
					
					m_dVariance = dRetentionError / standardsList.size();

					updateTimeElapsed(starttime);
					
					double dNum = dRetentionError / standardsList.size();
					String str;
					if (dNum < 0.0001)
						str = formatter2.format(dNum);
					else
						str = formatter1.format(dNum);
					
					// TODO: Fix this
					//contentPane2.jlblMixingVolume.setText(formatter3.format(m_dMixingVolumeArray[0][1]) + " mL");
					// TODO: Fix this
					//contentPane2.jlblNonMixingVolume.setText(formatter3.format(m_dNonMixingVolumeArray[0][1]) + " mL");
					updateVariance(dRetentionError/standardsList.size());
					
					calculateSimpleGradient();
					//this.updateGraphs(true);

					if (this.isCancelled())
					{
						return;
					}
				}

				if (bBackCalculateDeadTimeProfileOffset)
				{
					double dDeadTimeStep = m_dInitialDeadTimeArray[0][1] / 1000;
					double dDeadTimePrecision = m_dInitialDeadTimeArray[0][1] / 100000;
					double dMaxPercentChangeAtOnce = 1;
					
					dRetentionError = goldenSectioningSearchDeadTimeOffset(dDeadTimeStep, dDeadTimePrecision, iNumCompoundsToUseForDeadTimeOffset, dMaxPercentChangeAtOnce);
					m_dVariance = dRetentionError / standardsList.size();
					
					updateTimeElapsed(starttime);
					updateVariance(dRetentionError/standardsList.size());
					
					calculateSimpleDeadTime();
					//this.updateGraphs(true);
					
					if (this.isCancelled())
					{
						return;
					}
				}
				
				if (bBackCalculateDeadTimeProfile)
				{
					for (int iTimePoint = 0; iTimePoint < m_dDeadTimeDifferenceArray.length; iTimePoint++)
					{
						double dDeadTimeStep = m_dInitialDeadTimeArray[0][1] / 1000;
						double dDeadTimePrecision = m_dInitialDeadTimeArray[0][1] / 100000;
						double dMaxChangeAtOnce = m_dInitialDeadTimeArray[0][1] / 100;
						
						dRetentionError = goldenSectioningSearchDeadTime(iTimePoint, dDeadTimeStep, dDeadTimePrecision, dMaxChangeAtOnce, dDeadTimeProfileAngleWeight);
						m_dVariance = dRetentionError / standardsList.size();
						
						updateTimeElapsed(starttime);
						updateVariance(dRetentionError/standardsList.size());
						
						
					//	this.updateGraphs(true);

						if (this.isCancelled())
						{
							return;
						}
					}
				}
				
				if (bBackCalculateGradientProfile)
				{
					for (int iTimePoint = 0; iTimePoint < m_dGradientProfileDifferenceArray.length; iTimePoint++)
					{
						double dPercentBStep = .1;
						double dMaxChangeAtOnce = 2;
						double dPercentBPrecision = 0.001;
						
						dRetentionError = goldenSectioningSearchGradientProfile(iTimePoint, dPercentBStep, dPercentBPrecision, dMaxChangeAtOnce, dGradientProfileAngleWeight);
						m_dVariance = dRetentionError / standardsList.size();
						
						updateTimeElapsed(starttime);
						updateVariance(dRetentionError/standardsList.size());
						//TODO: update table
						
						if (this.isCancelled())
						{
							return;
						}
					}
				}
				
//				String str;
//				//double dNum = dRetentionError / standardsList.size();
//				
//				if (m_dVariance == 0)
//					str = "";
//				else if (m_dVariance < 0.0001)
//					str = formatter2.format(m_dVariance);
//				else
//					str = formatter1.format(m_dVariance);
				
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
					    	m_dtstep = m_dPlotXMax2 * 0.01;
							bSkipTerminationTest = true;
							
					//		contentPane2.jProgressBar.setString("Fitting dead time...");
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
					    	m_dtstep = m_dPlotXMax2 * 0.01;
							bSkipTerminationTest = true;
							
						//	contentPane2.jProgressBar.setString("Fitting with angle constraints...");
							
							// Now place handles on the gradient - need to do this now instead of earlier because we need gradient delay to find true corners
							placeHandles();
							
							m_bShowSimpleDeadTime = true;
							m_bShowSimpleGradient = true;
						}
					}
					else if (iPhase == 3)
					{
						if (dPercentImprovement < 2 && dPercentImprovement >= 0)
						{
							iPhase++;
//							contentPane2.jlblPhase.setText("IV");
//							
//							contentPane2.jProgressBar.setString("Fitting without angle constraints...");

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
							
							//contentPane2.jProgressBar.setString("Final optimization...");

							bBackCalculateSimpleGradientProfile = false;
							bBackCalculateDeadTimeProfileOffset = false;
							iNumCompoundsToUseForDeadTimeOffset = standardsList.size();
							bBackCalculateGradientProfile = true;
							bBackCalculateDeadTimeProfile = true;
							dGradientProfileAngleWeight = 0;
							dDeadTimeProfileAngleWeight = 0;	
							m_dtstep = m_dPlotXMax2 * 0.001;
							bSkipTerminationTest = true;
						}					
					}
					else if (iPhase >= 5)
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


		public void calculateSimpleDeadTime()
		{
			// Create new profile for the dead time offset
			int iNumPoints = 1000;
			double[][] dSimpleDeadTimeArray = new double[iNumPoints][2];
			for (int i = 0; i < iNumPoints; i++)
			{
		    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
		    	dSimpleDeadTimeArray[i][0] = dXPos;
				dSimpleDeadTimeArray[i][1] = m_InitialInterpolatedDeadTimeProfile.getAt(dXPos) + m_InterpolatedDeadTimeDifferenceProfile.getAt(dXPos);
			}
			m_InterpolatedSimpleDeadTime = new LinearInterpolationFunction(dSimpleDeadTimeArray);
		}
		
		// New version handles change in mixing/nonmixing volume as a function of solvent composition
		public void calculateSimpleGradient()
		{
			int iNumPoints = 10000;
			// Create an array for the simple gradient
			m_dSimpleGradientArray = new double[iNumPoints][2];
					
			// Initialize the solvent mixer composition to that of the initial solvent composition
		//	double dMixerComposition = ((Double) contentPane.tmGradientProgram.getValueAt(0, 1)).doubleValue(); //tmgradientprogram is an array with {{0.0, 5.0},{5.0, 95.0}}
			double dMixerComposition = 2.0;
			//double dFinalTime = (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) + ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iGradientTableLength - 1, 0)).doubleValue();
			double dFinalTime = m_dPlotXMax2; // in min
			double dTimeStep = dFinalTime / (iNumPoints - 1);
			
			// Start at time 0
			double dTime = 0;
			
			// Enter new values into the output array
			m_dSimpleGradientArray[0][0] = dTime;
			m_dSimpleGradientArray[0][1] = dMixerComposition;
			
			for (int i = 0; i < iNumPoints; i++)
			{
				// Find the current time
				dTime = i * dTimeStep;
				
				// Find the solvent composition coming into the nonmixing volume
				double dIncomingSolventCompositionToNonMixingVolume = m_InterpolatedIdealGradientProfile.getAt(dTime);

				// Now find the solvent composition coming into the mixing volume
				double dIncomingSolventCompositionToMixingVolume = 0;
				if (dTime < m_InterpolatedNonMixingVolume.getAt(dIncomingSolventCompositionToNonMixingVolume) / m_dFlowRate)
				{
					dIncomingSolventCompositionToMixingVolume = m_InterpolatedIdealGradientProfile.getAt(0);
				}
				else
				{
					double dTimeOfSolventComposition = dTime - (m_InterpolatedNonMixingVolume.getAt(dIncomingSolventCompositionToNonMixingVolume) / m_dFlowRate);
					dIncomingSolventCompositionToMixingVolume = m_InterpolatedIdealGradientProfile.getAt(dTimeOfSolventComposition);
				}
				
				// Figure out the volume of solvent B in the mixer
				double dSolventBInMixer = dMixerComposition * m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume);//m_dMixingVolume;
								
				// Now push out a step's worth of volume from the mixer
				dSolventBInMixer -= (m_dFlowRate * dTimeStep) * dMixerComposition;
				
				// dSolventBInMixer could be negative if the volume pushed out of the mixer is greater than the total volume of the mixer
				if (dSolventBInMixer < 0)
					dSolventBInMixer = 0;
				
				// Now add a step's worth of new volume from the pump
				// First, find which two data points we are between
				// Find the last data point that isn't greater than our current time
				
				if ((m_dFlowRate * dTimeStep) < m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume))
					dSolventBInMixer += (m_dFlowRate * dTimeStep) * dIncomingSolventCompositionToMixingVolume;
				else
				{
					// The amount of solvent entering the mixing chamber is larger than the mixing chamber. Just set the solvent composition in the mixer to that of the mobile phase.
					dSolventBInMixer = m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume) * dIncomingSolventCompositionToMixingVolume;
				}
				
				// Calculate the new solvent composition in the mixing volume
				if ((m_dFlowRate * dTimeStep) < m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume))
					dMixerComposition = dSolventBInMixer / m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume);
				else
					dMixerComposition = dIncomingSolventCompositionToMixingVolume;
				
				// Enter new values into the output array
				m_dSimpleGradientArray[i][0] = dTime - ((m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume) - m_InterpolatedMixingVolume.getAt(0)) / m_dFlowRate);
				m_dSimpleGradientArray[i][1] = dMixerComposition;
			}
			
			m_InterpolatedSimpleGradient = new LinearInterpolationFunction(m_dSimpleGradientArray);
		}
		
	    // Select where to place the handles in the m_dGradientProfileDifferenceArray and the m_dDeadTimeDifferenceArray
	    public void placeHandles()
	    {
	        // Find greatest dead volume
			double dVmMax = 0;
			
			for (int i = 0; i < m_dInitialDeadTimeArray.length; i++)
			{
				if (m_dInitialDeadTimeArray[i][1] + m_InterpolatedDeadTimeDifferenceProfile.getAt(m_dInitialDeadTimeArray[i][0])> dVmMax)
				{
					dVmMax = m_dInitialDeadTimeArray[i][1] + m_InterpolatedDeadTimeDifferenceProfile.getAt(m_dInitialDeadTimeArray[i][0]);
				}
			}	  
			
			dVmMax *= m_dFlowRate / 60;
			
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
			m_dGradientProfileDifferenceArray = new double [iNumGradientProgramDataPoints][2];
			
			m_dDeadTimeDifferenceArray = new double [iNumFlowDataPoints][2];
			
			// Set the value of the first data point
			m_dGradientProfileDifferenceArray[0][0] = 0;
			m_dGradientProfileDifferenceArray[0][1] = 0;
			
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
					m_dGradientProfileDifferenceArray[i][0] = dRtOneLesser;
				}
				else
				{
					// Otherwise, find the time of this point in between the surrounding alkanes
					double dPosition = ((dStandardNum - dOneLesser) / (dOneGreater - dOneLesser));
					m_dGradientProfileDifferenceArray[i][0] = (dPosition * dRtOneGreater) + ((1 - dPosition) * dRtOneLesser);
				}
				
				// Subtract half a hold-up time
				m_dGradientProfileDifferenceArray[i][0] = m_dGradientProfileDifferenceArray[i][0] - (0.5 * (dVmMax / m_dFlowRate));
				m_dGradientProfileDifferenceArray[i][1] = 0;
			}
			
			// Add the last data point at the position of the last-eluting compound - half dead time
			m_dGradientProfileDifferenceArray[iNumGradientProgramDataPoints - 1][0] = (Double)standardsList.get(standardsList.size() - 1).getMeasuredRetentionTime() - (0.5 * (dVmMax / m_dFlowRate));
			m_dGradientProfileDifferenceArray[iNumGradientProgramDataPoints - 1][1] = 0;
			
			// Create another copy of the ideal gradient profile array, delayed by the  delay
			double[][] dDelayedIdealGradientProfileArray = new double[m_dIdealGradientProfileArray.length + 1][2];
			dDelayedIdealGradientProfileArray[0][0] = 0;
			dDelayedIdealGradientProfileArray[0][1] = m_dIdealGradientProfileArray[0][1];
			for (int i = 1; i < dDelayedIdealGradientProfileArray.length; i++)
			{
				//dDelayedIdealGradientProfileArray[i][0] = m_dIdealGradientProfileArray[i - 1][0] + ((this.m_dMixingVolume + this.m_dNonMixingVolume) / this.m_dFlowRate);
				dDelayedIdealGradientProfileArray[i][0] = m_dIdealGradientProfileArray[i - 1][0] + ((m_InterpolatedMixingVolume.getAt(m_dIdealGradientProfileArray[i - 1][1]) + m_InterpolatedNonMixingVolume.getAt(m_dIdealGradientProfileArray[i - 1][1])) / m_dFlowRate);
				dDelayedIdealGradientProfileArray[i][1] = m_dIdealGradientProfileArray[i - 1][1];
			}
			
			// Now adjust to get points at the corners
			int iPointIndex = 1;
			// Run through each corner in the ideal gradient profile array
			for (int i = 1; i < dDelayedIdealGradientProfileArray.length - 1; i++)
			{
				double dFirst = 0;
				double dNext = 0;
				
				// Find the first point after the corner (dNext) and the first point before the corner (dFirst)
				while (dNext < dDelayedIdealGradientProfileArray[i][0] && iPointIndex < m_dGradientProfileDifferenceArray.length - 1)
				{
					dFirst = m_dGradientProfileDifferenceArray[iPointIndex][0];
					dNext = m_dGradientProfileDifferenceArray[iPointIndex + 1][0];
					
					iPointIndex++;
				}
				
				// Remove the last increment
				iPointIndex--;
				
				// Find the distances between the corner and the two points
				double dDistFirst = dDelayedIdealGradientProfileArray[i][0] - dFirst;
				double dDistNext = dNext - dDelayedIdealGradientProfileArray[i][0];
				
				// Find the distances between the two points and their next further point
				double dDistFirstBefore = m_dGradientProfileDifferenceArray[iPointIndex][0] - m_dGradientProfileDifferenceArray[iPointIndex - 1][0];
				double dDistNextAfter;
				if (iPointIndex + 2 < m_dGradientProfileDifferenceArray.length)
					dDistNextAfter = m_dGradientProfileDifferenceArray[iPointIndex + 2][0] - m_dGradientProfileDifferenceArray[iPointIndex + 1][0];
				else
					dDistNextAfter = 0;
				
				double dScoreFirst = dDistFirst + dDistFirstBefore;
				double dScoreNext = dDistNext + dDistNextAfter;
				
				// Point with lower score moves
				if (dScoreFirst < dScoreNext)
				{
					// Move the first point
					m_dGradientProfileDifferenceArray[iPointIndex][0] = dDelayedIdealGradientProfileArray[i][0];
					m_dGradientProfileDifferenceArray[iPointIndex][1] = 0;

					// Move the one before it right in between it and the last point
					if (iPointIndex >= 2)
					{
						m_dGradientProfileDifferenceArray[iPointIndex - 1][0] = (m_dGradientProfileDifferenceArray[iPointIndex - 2][0] + m_dGradientProfileDifferenceArray[iPointIndex][0]) / 2;
						m_dGradientProfileDifferenceArray[iPointIndex - 1][1] = 0;
					}
					
					// Move the one after it in between it and the next point
					if (iPointIndex <= m_dGradientProfileDifferenceArray.length - 3)
					{
						m_dGradientProfileDifferenceArray[iPointIndex + 1][0] = (m_dGradientProfileDifferenceArray[iPointIndex][0] + m_dGradientProfileDifferenceArray[iPointIndex + 2][0]) / 2;
						m_dGradientProfileDifferenceArray[iPointIndex + 1][1] = 0;
					}
					
				}
				else
				{
					// Move the next point
					m_dGradientProfileDifferenceArray[iPointIndex + 1][0] = dDelayedIdealGradientProfileArray[i][0];
					m_dGradientProfileDifferenceArray[iPointIndex + 1][1] = 0;

					// Move the one before it right in between it and the last point
					if (iPointIndex >= 1)
					{
						m_dGradientProfileDifferenceArray[iPointIndex][0] = (m_dGradientProfileDifferenceArray[iPointIndex - 1][0] + m_dGradientProfileDifferenceArray[iPointIndex + 1][0]) / 2;
						m_dGradientProfileDifferenceArray[iPointIndex][1] = 0;
					}
					
					// Move the one after it in between it and the next point
					if (iPointIndex <= m_dGradientProfileDifferenceArray.length - 4)
					{
						m_dGradientProfileDifferenceArray[iPointIndex + 2][0] = (m_dGradientProfileDifferenceArray[iPointIndex + 1][0] + m_dGradientProfileDifferenceArray[iPointIndex + 3][0]) / 2;
						m_dGradientProfileDifferenceArray[iPointIndex + 2][1] = 0;
					}
				}
			}

	    	// Now for the flow rate vs. time profile:
			
	/*    	// Add the initial interpolated hold-up time profile to the graph control
		    int iNumPoints = 1000;
		    for (int i = 0; i < iNumPoints; i++)
		    {
		    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
		    	contentPane2.m_GraphControlFlowRate.AddDataPoint(iIdealPlotIndexDeadTime, dXPos * 100, this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos));
		    }
	*/
		    for (int i = 0; i < iNumFlowDataPoints; i++)
			{
		    	m_dDeadTimeDifferenceArray[i][0] = (0.90 * ((double)i/((double)iNumFlowDataPoints - 1)) + 0.05);
		    	m_dDeadTimeDifferenceArray[i][1] = m_InterpolatedDeadTimeDifferenceProfile.getAt(m_dDeadTimeDifferenceArray[i][0]);
			}
			
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			
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

}
