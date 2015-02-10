package org.retentionprediction.peakfindergcfx;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import mr.go.sgfilter.SGFilter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class PeakFinderGCFX
{
	private PeakFinderSettingsPaneController peakFinderSettingsPaneController;
	private PeakFinderPaneController peakFinderPaneController;
	private ProgressDialogController progressDialogController;
	private Stage progressDialogStage;
	private Stage peakFinderDialogStage;
	
	private int stationaryPhase = 0;
	private double initialTemperature = 60;
	private double initialTime = 1;
	private double columnLength = 30; // in m
	private double innerDiameter = 0.25; // in mm
	private double filmThickness = 0.25; // in um
	private double flowRate = 1; // in mL/min
	private double inletPressure = 45; // in kPa
	private double outletPressure = 0.000001; // in kPa
	private boolean isConstantFlowRateMode = false;
	private boolean isVacuumOutletPressure = true;
	
	private double[] sgcoefficients = null;
	private SGFilter sgfilter = null;
	private double[] rawEIC = null;
	private double[] smoothEIC = null;

	private double[][][] mzData = null;
	private double[] retentionTimes = null;

	private double[] selectedRetentionTimes = null;
	private boolean[] skippedStandards = null;
	private int[] selectedPeakRank = null;
	private boolean okPressed = false;
    
	private String[] stationaryPhaseArray = null;
	private String[] standardCompoundsNameArray = null;
	private double[][] standardCompoundsMZArray = null;
	private double[][] isothermalParamArray = null;
	private double[][] temperatureProgram;
	private double[][] temperatureProgramInConventionalForm = {{26.0, 260.0, 5.0}};
	private LinearInterpolationFunction interpolatedTemperatureProfile;  //  @jve:decl-index=0:
	private double[][] holdUpArray;
	private InterpolationFunction interpolatedHoldUpProfile;
	private double v0 = 1; // in mL
    private double theoreticalPlatesPerMeter = 4433;
    private double theoreticalPlates = theoreticalPlatesPerMeter * columnLength;
	
    private double[] predictedRetentionTimes;
    private double[] predictedPeakWidths;
    private Vector<Vector<double[]>> peaks = new Vector<Vector<double[]>>();
    private double[] maxIntensities;

	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();

	OpenMzXMLFileTask openMZFileTask = null;
	FindPeaksTask findPeaksTask = null;
    
	private boolean editable = true;
	private String fileName = "";
    
	private double tStep = 0;
	private Window parentWindow;
	
	public PeakFinderGCFX(Window parentWindow, String[] stationaryPhaseNames, boolean editable)
	{
		this.parentWindow = parentWindow;
		this.stationaryPhaseArray = stationaryPhaseNames;
		this.editable = editable;
	}
	
	@SuppressWarnings("unchecked")
	public void run()
	{
		Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		
		try {
			// Load the PeakFinderSettingsPane and its controller
			FXMLLoader fxmlLoader = new FXMLLoader();
			ScrollPane root = (ScrollPane)fxmlLoader.load(getClass().getResource("PeakFinderSettingsPane.fxml").openStream());
			peakFinderSettingsPaneController = fxmlLoader.getController();
			peakFinderSettingsPaneController.setStage(dialog);

			// Load the dialog with all the values it needs
			peakFinderSettingsPaneController.setColumnLength(columnLength);
			peakFinderSettingsPaneController.setConstantFlowMode(isConstantFlowRateMode);
			peakFinderSettingsPaneController.setFilmThickness(filmThickness);
			peakFinderSettingsPaneController.setFlowRate(flowRate);
			peakFinderSettingsPaneController.setInletPressure(inletPressure);
			peakFinderSettingsPaneController.setInnerDiameter(innerDiameter);
			peakFinderSettingsPaneController.setOutletPressure(outletPressure, this.isVacuumOutletPressure);
			peakFinderSettingsPaneController.setFileName(fileName);
			peakFinderSettingsPaneController.setTemperatureProgramInConventionalForm(temperatureProgramInConventionalForm, initialTemperature, initialTime);
			
			// Set the dialog to the be editable or not
			peakFinderSettingsPaneController.setEditable(editable);

			// Create the scene
			Scene scene = new Scene(root, 60 * rem, 40 * rem);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			dialog.setTitle("Load a GC-MS Data File");
			dialog.setScene(scene);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(parentWindow);
			dialog.sizeToScene();
			
			// Show the dialog and wait for it to return
			dialog.showAndWait();
			
			// Check if the OK button was pressed. If not, quit now.
			if (!peakFinderSettingsPaneController.getOKButtonPressed())
			{
				this.okPressed = false;
				return;
			}
			
			// OK button was pressed, so first grab all the data out of that dialog.
			fileName = peakFinderSettingsPaneController.getFileName();
			flowRate = peakFinderSettingsPaneController.getFlowRate();
			inletPressure = peakFinderSettingsPaneController.getInletPressure();
			isConstantFlowRateMode = peakFinderSettingsPaneController.getConstantFlowMode();
			initialTime = peakFinderSettingsPaneController.getInitialTime();
			initialTemperature = peakFinderSettingsPaneController.getInitialTemperature();
			outletPressure = peakFinderSettingsPaneController.getOutletPressure();
			columnLength = peakFinderSettingsPaneController.getColumnLength();
			innerDiameter = peakFinderSettingsPaneController.getInnerDiameter();
			filmThickness = peakFinderSettingsPaneController.getFilmThickness();
			temperatureProgram = peakFinderSettingsPaneController.getTemperatureProgram();
			
			// Now create the progress dialog
			fxmlLoader = new FXMLLoader();
			GridPane progressDialog = (GridPane)fxmlLoader.load(getClass().getResource("ProgressDialog.fxml").openStream());
			progressDialogController = fxmlLoader.getController();
			progressDialogStage = new Stage();
			progressDialogStage.setResizable(false);
			progressDialogStage.initStyle(StageStyle.UTILITY);
			progressDialogStage.initModality(Modality.WINDOW_MODAL);
			progressDialogStage.initOwner(parentWindow);


			Scene progressDialogScene = new Scene(progressDialog, 30 * rem, 10 * rem);
			progressDialogScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			progressDialogStage.setTitle("Loading...");
			progressDialogStage.setScene(progressDialogScene);
			
			// Add a listener for the Cancel button
			progressDialogController.getCancelProperty().addListener(new CancelListener<Boolean>());
			
			// Now load the file
			openMZFileTask = new OpenMzXMLFileTask(progressDialogStage);
			progressDialogController.progressProperty().bind(openMZFileTask.progressProperty());
			progressDialogController.messageProperty().bind(openMZFileTask.messageProperty());
			openMZFileTask.setFileName(fileName);
			openMZFileTask.setStandardCompoundsMZArray(standardCompoundsMZArray);
			openMZFileTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() 
					{
				        @Override
				        public void handle(WorkerStateEvent t)
				        {
				        	// The file was successfully loaded. Grab the data out of it.
				        	mzData = openMZFileTask.getMzData();
				        	retentionTimes = openMZFileTask.getRetentionTimes();

				        	// Run the FindPeaksTask
				        	findPeaksTask = new FindPeaksTask();
				        	progressDialogController.progressProperty().unbind();
				        	progressDialogController.messageProperty().unbind();
				        	progressDialogController.progressProperty().bind(findPeaksTask.progressProperty());
				        	progressDialogController.messageProperty().bind(findPeaksTask.messageProperty());
				        	
				        	findPeaksTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() 
									{
								        @Override
								        public void handle(WorkerStateEvent t)
								        {
								        	launchPeakFinderDialog();
								        	progressDialogStage.close();
								        }
								    });
				        	findPeaksTask.setOnCancelled(new EventHandler<WorkerStateEvent>() 
									{
								        @Override
								        public void handle(WorkerStateEvent t)
								        {
								        	progressDialogStage.close();
								        }
								    });

				        	Thread findPeaksThread = new Thread(findPeaksTask);
				        	findPeaksThread.start();
				        }
				    });
			
			openMZFileTask.setOnCancelled(new EventHandler<WorkerStateEvent>() 
					{
				        @Override
				        public void handle(WorkerStateEvent t)
				        {
				        	progressDialogStage.close();
				        }
				    });

			Thread openThread = new Thread(openMZFileTask);
			openThread.start();
			
			progressDialogStage.showAndWait();


		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void launchPeakFinderDialog()
	{
    	FXMLLoader fxmlLoaderPeakFinder = new FXMLLoader();
		ScrollPane peakFinderPane = null;
		try {
			peakFinderPane = (ScrollPane)fxmlLoaderPeakFinder.load(getClass().getResource("PeakFinderPane.fxml").openStream());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		peakFinderPaneController = fxmlLoaderPeakFinder.getController();
		peakFinderDialogStage = new Stage();

		Scene peakFinderDialogScene = new Scene(peakFinderPane, 75 * rem, 50 * rem);
		peakFinderDialogScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		peakFinderDialogStage.setTitle("Peak Finder");
		peakFinderDialogStage.setScene(peakFinderDialogScene);
		peakFinderDialogStage.initModality(Modality.WINDOW_MODAL);
		peakFinderDialogStage.initOwner(parentWindow);

		peakFinderPaneController.setStage(peakFinderDialogStage);
		peakFinderPaneController.setMzData(mzData);
		peakFinderPaneController.setMZArray(standardCompoundsMZArray);
		peakFinderPaneController.setPeaksArray(peaks);
		peakFinderPaneController.setCompoundNameArray(standardCompoundsNameArray);
		peakFinderPaneController.setRetentionTimes(retentionTimes);
		peakFinderPaneController.setSelectedPeakRank(selectedPeakRank);
		peakFinderPaneController.setSelectedRetentionTimes(selectedRetentionTimes);
		peakFinderPaneController.setSkippedStandards(skippedStandards);
		peakFinderPaneController.setPredictedRetentionTimes(predictedRetentionTimes);
		peakFinderPaneController.setPredictedPeakWidths(predictedPeakWidths);
		peakFinderPaneController.finalInit();
		
		peakFinderDialogStage.showAndWait();

        if (peakFinderPaneController.wasOkPressed())
        {
        	selectedRetentionTimes = peakFinderPaneController.getSelectedRetentionTimes();
        	skippedStandards = peakFinderPaneController.getSkippedStandards();
        	selectedPeakRank = peakFinderPaneController.getSelectedPeakRank();
        	okPressed = true;
        }
        
        // Peak Finder is now finished!
	}
	
	private class CancelListener<Boolean> implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) 
		{
			// Cancel button was pressed on the progress dialog
			if (openMZFileTask != null)
				openMZFileTask.cancel();
		}
	}
	
	public double[] getSelectedRetentionTimes()
    {
    	return selectedRetentionTimes;
    }
    	
    public boolean[] getSkippedStandards()
    {
    	return skippedStandards;
    }

    public int[] getSelectedPeakRank()
    {
    	return selectedPeakRank;
    }
    
    public boolean getOkPressed()
    {
    	return okPressed;
    }

    public void setIsothermalData (double[][] isothermalParamArray)
    {
    	this.isothermalParamArray = isothermalParamArray;
    }
    
    public void setStandardCompoundNames (String[] standardCompoundNames)
    {
    	this.standardCompoundsNameArray = standardCompoundNames;
    }

    public void setStandardCompoundMZData (double[][] standardCompoundMZValues)
    {
    	this.standardCompoundsMZArray = standardCompoundMZValues;
    }
    
    public String getFileName()
    {
    	return this.fileName;
    }
    
    public void setFileName(String fileName)
    {
    	this.fileName = fileName;
    }
    
    // This temperature program is input as {{ramp rate, final temp, hold time}}
	public void setTemperatureProgramInConventionalForm(double[][] temperatureProgramInConventionalForm, double initialTemperature, double initialTime)
	{
		this.temperatureProgramInConventionalForm = temperatureProgramInConventionalForm;
		this.initialTemperature = initialTemperature;
		this.initialTime = initialTime;
		this.temperatureProgram = Globals.convertTemperatureProgramInConventionalFormToRegularForm(temperatureProgramInConventionalForm, initialTemperature, initialTime);
	}

	public void setTemperatureProfile(double[][] temperatureArray)
	{
		this.interpolatedTemperatureProfile = new LinearInterpolationFunction(temperatureArray);
	}
	
	public void setHoldUpTimeProfile(double[][] holdUpArray)
	{
		this.interpolatedHoldUpProfile = new InterpolationFunction(holdUpArray);
	}
	
	public void setFlowRate(double flowRate)
	{
		this.flowRate = flowRate;
	}
	
	public void setColumnLength(double columnLength)
	{
		this.columnLength = columnLength;
	}
	
	public void setInnerDiameter(double innerDiameter)
	{
		this.innerDiameter = innerDiameter;
	}
	
	public void setFilmThickness(double filmThickness)
	{
		this.filmThickness = filmThickness;
	}

	public void setInletPressure(double inletPressure)
	{
		this.inletPressure = inletPressure;
	}

	public void setConstantFlowMode(boolean constantFlowMode)
	{
		this.isConstantFlowRateMode = constantFlowMode;
	}
	
	public void setOutletPressure(double outletPressure, boolean isUnderVacuum)
	{
		this.isVacuumOutletPressure = isUnderVacuum;
		this.outletPressure = outletPressure;
	}

	public void setInitialTime(double initialTime)
	{
		this.initialTime = initialTime;
	}

	public void setInitialTemperature(double initialTemperature)
	{
		this.initialTemperature = initialTemperature;
	}

	public void setTStep(double dtstep)
	{
		this.tStep = dtstep;
	}
	
	class FindPeaksTask extends Task 
	{
		@Override
		protected Object call() throws Exception 
		{
			// Initialize output arrays
	        skippedStandards = new boolean[standardCompoundsNameArray.length];
	        for (int i = 0; i < skippedStandards.length; i++)
	        	skippedStandards[i] = false;

	        selectedRetentionTimes = new double[standardCompoundsNameArray.length];
	        for (int i = 0; i < selectedRetentionTimes.length; i++)
	        	selectedRetentionTimes[i] = 0d;
	        
	        selectedPeakRank = new int[standardCompoundsNameArray.length];
	        for (int i = 0; i < selectedPeakRank.length; i++)
	        	selectedPeakRank[i] = -1;
	        
			// If editable is true, then it needs to find a good hold-up time to fit the data
			// If editable is false, then we are providing the back-calculated hold-up time profile and temperature program
			if (editable)
			{
				this.updateProgress(0.0, 1.0);
				this.updateMessage("Finding peaks...");

				// Make the interpolated ideal gradient profile
		    	interpolatedTemperatureProfile = new LinearInterpolationFunction(temperatureProgram);
				
		    	// Calculate hold-up time profile
		    	calculateHoldUpTimeProfile();
		    	
		    	// Calculate approximate retention times of the standards
		    	predictRetentionTimes(0);
		    	
		    	// Now find all peaks
		    	findPeaks();
		    	
		    	for (int i = 0; i < peaks.size(); i++)
				{
					selectedPeakRank[i] = 0;
				}
		    	
		    	// Try 100 different hold-up times offsets to find the one that gives the best score
		    	double[][] dStoreHoldUpArray = holdUpArray;
		    	int iCount = 100;
		    	double[] dSumScore = new double[iCount];
		    	
		    	for (int iOffsetNum = 0; iOffsetNum < iCount; iOffsetNum++)
		    	{
					updateProgress(iOffsetNum, iCount);
					
					if (this.isCancelled())
					{
						return null;
					}

		    		double dOffset = 0;
		  
		    		if (iOffsetNum < iCount / 2)
		    			dOffset = 0.5 + ((double)iOffsetNum / (double)iCount);
		    		else
		    			dOffset = ((double)iOffsetNum / (double)iCount) * 2.0;
		    			
		    		holdUpArray = new double[dStoreHoldUpArray.length][2];
		    		for (int i = 0; i < dStoreHoldUpArray.length; i++)
		    		{
		    			holdUpArray[i][0] = dStoreHoldUpArray[i][0];
		    			holdUpArray[i][1] = dStoreHoldUpArray[i][1] * dOffset;
		    		}
		        	interpolatedHoldUpProfile = new InterpolationFunction(holdUpArray);
		        	
		        	predictRetentionTimes(0);
		
		        	calculatePeakRanks();
		        	
		        	// Calculate and store this hold-up time offset's score
		        	// selectedPeakRank is now filled with 0's, so it is calculating the score with all the highest ranked peaks for each standard
		        	dSumScore[iOffsetNum] = scorePermutation(selectedPeakRank);
		    	}
		    	
		    	// Find the maximum sum score
		    	int iMax = 0;
		    	for (int i = 0; i < dSumScore.length; i++)
		    	{
		    		if (dSumScore[i] > dSumScore[iMax])
		    			iMax = i;
		    	}
		    	
		    	// Return the hold up time profile to the one that had the max score
		    	double dOffset = 0;
		    	  
				if (iMax < iCount / 2)
					dOffset = 0.5 + ((double)iMax / (double)iCount);
				else
					dOffset = ((double)iMax / (double)iCount) * 2.0;
					
				holdUpArray = new double[dStoreHoldUpArray.length][2];
				for (int i = 0; i < dStoreHoldUpArray.length; i++)
				{
					holdUpArray[i][0] = dStoreHoldUpArray[i][0];
					holdUpArray[i][1] = dStoreHoldUpArray[i][1] * dOffset;
				}
		    	interpolatedHoldUpProfile = new InterpolationFunction(holdUpArray);
		    	
		    	// Predict retention times again with this best fit hold-up time profile
		    	predictRetentionTimes(0);
		
		    	calculatePeakRanks();
		
		    	// Now skip standards that aren't right for one reason or another.
		    	// Also populate the "selectedRetentionTimes" array
		    	for (int i = 0; i < peaks.size(); i++)
				{
		    		// Skip standards where there were no peaks identified.
		    		if (peaks.get(i).size() == 0)
		    		{
		    			skippedStandards[i] = true;
		    			selectedPeakRank[i] = -1;
		    			continue;
		    		}

		    		// Populate the selected retention times array
					selectedRetentionTimes[i] = peaks.get(i).get(0)[0] / 60;
					
					// Check if this peak is predicted to be past the end of the run
					if (predictedRetentionTimes[i] > retentionTimes[retentionTimes.length - 1] / 60)
					{
						skippedStandards[i] = true;
					}
					
					// Check if this peak is predicted to be before the beginning of the run
					if (predictedRetentionTimes[i] < retentionTimes[0] / 60)
					{
						skippedStandards[i] = true;
					}
				}
			}
			else
			{
		    	// Calculate approximate retention times of the standards
		    	predictRetentionTimes(tStep);
		    	
		    	// Now find all peaks
		    	findPeaks();
		    	
		    	// Now calculate peak ranks
		    	calculatePeakRanks();
		    	
		    	for (int i = 0; i < peaks.size(); i++)
				{
					this.updateProgress(i, peaks.size());

					if (this.isCancelled())
					{
						return null;
					}
					
					if (peaks.get(i).size() == 0)
		    		{
		    			skippedStandards[i] = true;
		    			selectedPeakRank[i] = -1;
		    			continue;
		    		}

					selectedPeakRank[i] = 0;

					selectedRetentionTimes[i] = peaks.get(i).get(0)[0] / 60;
					
					// Check if this peak is predicted to be past the end of the run
					if (predictedRetentionTimes[i] > retentionTimes[retentionTimes.length - 1] / 60)
					{
						skippedStandards[i] = true;
					}

					// Check if this peak is predicted to be before the beginning of the run
					if (predictedRetentionTimes[i] < retentionTimes[0] / 60)
					{
						skippedStandards[i] = true;
					}
				}
			}
			
			return null;		
		}
		
		// Calculates the hold-up time profile given the column dimensions, film thickness, inlet pressure/flow rate, and outlet pressure
		// Populates "holdUpArray"
		// Populates "interpolatedHoldUpProfile"
		
		private void calculateHoldUpTimeProfile()
		{
			// Calculate hold-up time profile
			double dInnerDiameter = (innerDiameter / 10) - (2 * filmThickness / 10000); // in cm
			v0 = (Math.PI * Math.pow(dInnerDiameter / 2, 2) * (columnLength * 100)) / 1000; // gives the volume in the column (in L)

			int iNumPoints = 10;
			double dLowTemp = 0;
			double dHighTemp = 500;
			
			holdUpArray = new double[iNumPoints][2];
			
			for (int i = 0; i < iNumPoints; i++)
			{
				holdUpArray[i][0] = ((dHighTemp - dLowTemp) * ((double)i / (double)(iNumPoints - 1))) + dLowTemp;
				double dGasViscosity = 18.69 * Math.pow(10, -6) * Math.pow((holdUpArray[i][0] + 273.15) / 273.15, 0.6958 + -0.0071 * (((holdUpArray[i][0] + 273.15) - 273.15) / 273.15));
				double dOmega = columnLength * dGasViscosity * (32.0 / Math.pow(dInnerDiameter / 100, 2));
				double dDeadTime = 0;
				if (isConstantFlowRateMode)
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
	    	
	    	interpolatedHoldUpProfile = new InterpolationFunction(holdUpArray);
		}
		
		// Predicts retention times for each of the compounds for which there is isothermal data.
		// Populates the "predictedRetentionTimes" variable
		// Populates the "predictedPeakWidths" variable
		// When dtstepToUse is 0, the function calculates its own value.
		
		private void predictRetentionTimes(double dtstepToUse)
	    {
	    	double dTimeLimit = Math.max(retentionTimes[retentionTimes.length - 1] / 60, temperatureProgram[temperatureProgram.length - 1][0]) * 3.0;
			double dtstep;
			if (dtstepToUse == 0)
				dtstep = dTimeLimit * 0.001;
			else
				dtstep = dtstepToUse;

			predictedRetentionTimes = new double[isothermalParamArray.length];
			predictedPeakWidths = new double[isothermalParamArray.length];

	    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
	    	double dBeta2 = Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2) / (Math.pow(innerDiameter / 2, 2) - Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2));
	    	double dBeta1Beta2 = dBeta1 / dBeta2;

			int iNumCompounds = isothermalParamArray.length;
			for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
			{
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
				
				dTcA = interpolatedTemperatureProfile.getAt(0);
				
				for (double t = 0; t <= dTimeLimit; t += dtstep)
				{
					dTcB = interpolatedTemperatureProfile.getAt(t + dtstep);

					double dTc = (dTcA + dTcB) / 2;
					dt0 = interpolatedHoldUpProfile.getAt(dTc) / 60;
					
					kprime = calckfromT(isothermalParamArray[iCompound], dTc) * dBeta1Beta2;

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
			    	double dSigma = Math.sqrt(Math.pow((dt0 * (1 + kprime)) / Math.sqrt(theoreticalPlates), 2));
			    	predictedPeakWidths[iCompound] = dSigma * 2.355; // Peak width at half height
					predictedRetentionTimes[iCompound] = dtRFinal;
				}
				else
				{
					predictedPeakWidths[iCompound] = -1;
					// Did not elute
					predictedRetentionTimes[iCompound] = -1;
				}
				
			}
	    }
		
	    /*private void predictRetentionTimes(double dtstepToUse)
	    {
	    	double dTimeLimit = Math.max(retentionTimes[retentionTimes.length - 1] / 60, temperatureProgram[temperatureProgram.length - 1][0]) * 3.0;
			double dtstep;
			if (dtstepToUse == 0)
				dtstep = dTimeLimit * 0.001;
			else
				dtstep = dtstepToUse;

			predictedRetentionTimes = new double[isothermalDataArray.length];
			predictedPeakWidths = new double[isothermalDataArray.length];

	    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
	    	double dBeta2 = Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2) / (Math.pow(innerDiameter / 2, 2) - Math.pow((innerDiameter / 2) - (filmThickness / 1000), 2));
	    	double dBeta1Beta2 = dBeta1 / dBeta2;

			int iNumCompounds = isothermalDataArray.length;
			for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
			{
				InterpolationFunction IsothermalData = new InterpolationFunction(isothermalDataArray[iCompound]);

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
				
				dTcA = interpolatedTemperatureProfile.getAt(0);
				
				for (double t = 0; t <= dTimeLimit; t += dtstep)
				{
					dTcB = interpolatedTemperatureProfile.getAt(t + dtstep);

					double dTc = (dTcA + dTcB) / 2;
					dt0 = interpolatedHoldUpProfile.getAt(dTc) / 60;
					
					kprime = Math.pow(10, IsothermalData.getAt(dTc)) * dBeta1Beta2;
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
			    	double dSigma = Math.sqrt(Math.pow((dt0 * (1 + kprime)) / Math.sqrt(theoreticalPlates), 2));
			    	predictedPeakWidths[iCompound] = dSigma * 2.355; // Peak width at half height
					predictedRetentionTimes[iCompound] = dtRFinal;
				}
				else
				{
					predictedPeakWidths[iCompound] = -1;
					// Did not elute
					predictedRetentionTimes[iCompound] = -1;
				}
				
			}
	    }*/
	    
	    // Finds a list of peaks in the EIC for each standard. 
	    // Populates the "peaks" variable.
	    // Populates the "maxIntensities" variable.
	    
		private void findPeaks()
		{
			maxIntensities = new double[standardCompoundsMZArray.length];
			int spectraCount = mzData.length;
			
			for (int iStandard = 0; iStandard < standardCompoundsMZArray.length; iStandard++)
			{
				double dSelectedMasses[] = standardCompoundsMZArray[iStandard];
	    	
				// Create new data array for the EIC
				rawEIC = new double[spectraCount];
				double m_EICderivative[] = new double[spectraCount];
				double m_EICSecondDerivative[] = new double[spectraCount];
				double intensity;
	        
				// This for loop is the part that is slow when you click on a standard.
				// This is where we create the raw EIC.
				// If the standard has more than one mass listed, the EIC is the sum of both masses
				for (int iSpectrum = 0; iSpectrum < spectraCount; iSpectrum++)
				{
					// process all peaks by iterating over the m/z values
					intensity = 0;
	          
					for (int i = 0; i < mzData[iSpectrum].length; i++) 
					{
						for (int j = 0; j < dSelectedMasses.length; j++)
						{
							if (mzData[iSpectrum][i][0] >= dSelectedMasses[j] - 0.5 && mzData[iSpectrum][i][0] <= dSelectedMasses[j] + 0.5)
							{
								intensity += mzData[iSpectrum][i][1];
							}
						}
					}

					rawEIC[iSpectrum] = intensity;
				}
	        
				// Determine number of points to filter
				// # of points per second
				double dTotalTime = retentionTimes[retentionTimes.length - 1] - retentionTimes[0];
				double dPointsPerSecond = spectraCount / dTotalTime;
				double dPeakWidthInSeconds = predictedPeakWidths[iStandard] * 60;
				double dK = 0.5;
				int dNumPointsInFilter = (int)(dK * dPeakWidthInSeconds * dPointsPerSecond);

				if (dNumPointsInFilter < 2)
					dNumPointsInFilter = 2;
	        
				int iOrder = 3;
				if (dNumPointsInFilter == 2)
					iOrder = 2;
				
				// Run Savitzky-Golay filter
				sgfilter = new SGFilter(dNumPointsInFilter, dNumPointsInFilter);
				sgcoefficients = SGFilter.computeSGCoefficients(dNumPointsInFilter, dNumPointsInFilter, iOrder);
				smoothEIC = sgfilter.smooth(rawEIC, sgcoefficients);
				for (int i = 0; i < 10; i++)
				{
					smoothEIC = sgfilter.smooth(smoothEIC, sgcoefficients);
				}
			
				// Calculate the derivative of the EIC
				m_EICderivative[0] = 0;
				m_EICderivative[rawEIC.length - 1] = 0;
	        
		        for (int i = 1; i < rawEIC.length - 1; i++)
		        {
		        	double dYBefore = (smoothEIC[i - 1] + smoothEIC[i]) / 2;
		        	double dYAfter = (smoothEIC[i + 1] + smoothEIC[i]) / 2;
		        	double dXBefore = (retentionTimes[i - 1] + retentionTimes[i]) / 2;
		        	double dXAfter = (retentionTimes[i + 1] + retentionTimes[i]) / 2;
		        	
		        	double dSlope = (dYAfter - dYBefore) / (dXAfter - dXBefore);
		        	m_EICderivative[i] = dSlope;
		        }
	        
		        // Calculate the second derivative of the EIC
		        m_EICSecondDerivative[0] = 0;
		        m_EICSecondDerivative[rawEIC.length - 1] = 0;
		        
		        for (int i = 1; i < rawEIC.length - 1; i++)
		        {
		        	double dYBefore = (m_EICderivative[i - 1] + m_EICderivative[i]) / 2;
		        	double dYAfter = (m_EICderivative[i + 1] + m_EICderivative[i]) / 2;
		        	double dXBefore = (retentionTimes[i - 1] + retentionTimes[i]) / 2;
		        	double dXAfter = (retentionTimes[i + 1] + retentionTimes[i]) / 2;
		        	
		        	double dSlope = (dYAfter - dYBefore) / (dXAfter - dXBefore);
		        	m_EICSecondDerivative[i] = dSlope;
		        }
	        
		        // Smooth the second derivative
		        m_EICSecondDerivative = sgfilter.smooth(m_EICSecondDerivative, sgcoefficients);

		        // Find maximum intensity
		        maxIntensities[iStandard] = 0;
		        for (int i = 0; i < rawEIC.length; i++)
		        {
		        	if (smoothEIC[i] > maxIntensities[iStandard])
		        		maxIntensities[iStandard] = smoothEIC[i];
		        }
	        
		        // Find the peaks (any place where the derivative crosses zero with a negative slope)        
		        Vector<double[]> thisCompoundPeaks = new Vector<double[]>();
		        
		        for (int i = 0; i < rawEIC.length - 1; i++)
		        {
		        	double dYBefore = m_EICderivative[i];
		        	double dYAfter = m_EICderivative[i + 1];
		        	if (dYBefore >= 0 && dYAfter < 0)
		        	{
		        		double dXBefore = retentionTimes[i];
		            	double dXAfter = retentionTimes[i + 1];
		            	
		        		double dFraction = (dYBefore - 0) / (dYBefore - dYAfter);
		        		double dPeakTime = (dFraction * (dXAfter - dXBefore)) + dXBefore;
		        		
		        		// Find the intensity of the peak (signal at the peak maximum)
		        		double dPeakIntensity = (dFraction * (smoothEIC[i + 1] - smoothEIC[i])) + smoothEIC[i];
		        		
		        		// Calculate peak width. How wide is the second derivative at zero?
		        		// Measure time to right side of peak
		        		double dRightSideTime = 0;
		        		for (int j = i; j < rawEIC.length; j++)
		        		{
		        			if (m_EICSecondDerivative[j] > 0)
		        			{
		        				if (j == 0)
		        					break;
		        				
		        				double dYBeforeDD = m_EICSecondDerivative[j - 1]; // Problem is here when i = 0
		        	        	double dYAfterDD = m_EICSecondDerivative[j];
		        	        	double dFractionDD = (dYBeforeDD - 0) / (dYBeforeDD - dYAfterDD);
		        	        	double dXBeforeDD = retentionTimes[j - 1];
		                    	double dXAfterDD = retentionTimes[j];
		        	        	dRightSideTime = (dFractionDD * (dXAfterDD - dXBeforeDD)) + dXBeforeDD;
		        	        	break;
		        			}
		        		}
		        		
		        		// Measure time to left side of peak
		        		double dLeftSideTime = retentionTimes[0];
		        		for (int j = i; j > 0; j--)
		        		{
		        			if (m_EICSecondDerivative[j] > 0)
		        			{
		        				double dYBeforeDD = m_EICSecondDerivative[j];
		        	        	double dYAfterDD = m_EICSecondDerivative[j + 1];
		        	        	double dFractionDD = (dYBeforeDD - 0) / (dYBeforeDD - dYAfterDD);
		        	        	double dXBeforeDD = retentionTimes[j];
		                    	double dXAfterDD = retentionTimes[j + 1];
		        	        	dLeftSideTime = (dFractionDD * (dXAfterDD - dXBeforeDD)) + dXBeforeDD;
		        	        	break;
		        			}
		        		}
		        		
		        		double dPeakWidthHalfHeight = dRightSideTime - dLeftSideTime;
		        		
		        		// {peak time, peak intensity, peak width at half height}
		        		thisCompoundPeaks.add(new double[]{dPeakTime, dPeakIntensity, dPeakWidthHalfHeight, 0, 0});
		           	}
		        }

		        peaks.add(thisCompoundPeaks);
			}
		}
		
	    private class CompareRank implements Comparator<double[]>
	    {
	        @Override
	        public int compare(double[] o1, double[] o2) 
	        {
	            return (o1[3] > o2[3] ? -1 : (o1[3] == o2[3] ? 0 : 1));
	        }
	    }
		
	    // Calculates a score for each peak and then calculates a percentage probability that the peak is the correct one.
	    // Modifies the "peaks" variable - peaks[i][3] = peak score and peaks[i][4] = peak rank.
	    
		private void calculatePeakRanks()
		{
			for (int iStandard = 0; iStandard < standardCompoundsMZArray.length; iStandard++)
			{
				for (int iPeak = 0; iPeak < peaks.get(iStandard).size(); iPeak++)
				{
					double dPeakTime = peaks.get(iStandard).get(iPeak)[0];
					double dPeakIntensity = peaks.get(iStandard).get(iPeak)[1];
					double dPeakWidthHalfHeight = peaks.get(iStandard).get(iPeak)[2];
					
					// Calculate peak score.
		    		double TimeAt36Percent = predictedPeakWidths[iStandard] * (1 / 0.03); // in min
		    		// Gives a value from 1 to 0 for closeness to predicted retention time
		    		double dPeakScoreRetentionTime = Math.exp(-Math.abs((dPeakTime / 60) - predictedRetentionTimes[iStandard]) / TimeAt36Percent);
		    		double dPeakScoreHeight = dPeakIntensity / maxIntensities[iStandard];
		    		double ErrorAt36Percent = predictedPeakWidths[iStandard];
		    		double dPeakScoreWidth = Math.exp(-Math.abs((dPeakWidthHalfHeight / 60) - predictedPeakWidths[iStandard]) / ErrorAt36Percent);
		    		
		    		// Calculate the combined peak score
		    		double dPeakScore = Math.pow(dPeakScoreRetentionTime, 2) * Math.pow(dPeakScoreHeight, 1) * Math.pow(dPeakScoreWidth, 1);
		    		peaks.get(iStandard).get(iPeak)[3] = dPeakScore;
				}

		        // Sort the peaks in descending order based on their score
		        Collections.sort(peaks.get(iStandard), new CompareRank());
		
		        int iNumPeaksToShow = 20;
		        if (iNumPeaksToShow > peaks.get(iStandard).size())
		        {
		        	iNumPeaksToShow = peaks.get(iStandard).size();
		        }
	        
		        double dSumOfScores = 0;
		        for (int i = 0; i < iNumPeaksToShow; i++)
		        {
		        	dSumOfScores += peaks.get(iStandard).get(i)[3];
		        }
		        
		        // Calculate percentage probability for each peak
		        for (int i = 0; i < peaks.get(iStandard).size(); i++)
		        {
		        	peaks.get(iStandard).get(i)[4] = (peaks.get(iStandard).get(i)[3] / dSumOfScores) * 100;
		        }
			}
		}
		
		// Assign an overall score to the set of selected peaks based on a number of factors.
		// returns a score
		
		private double scorePermutation(int[] permutation)
		{
			double dTotalDistanceError = 0;
			double dTotalIntensityDifference = 0;
			double dTotalPeakScores = 0;
			int iPeakCount = 0;

			for (int i = 1; i < permutation.length; i++)
			{
				if (skippedStandards[i])
					continue;
				
				// Check if this peak is predicted to be past the end of the run
				if (predictedRetentionTimes[i] > retentionTimes[retentionTimes.length - 1] / 60)
					continue;
				
				// Check if this peak is predicted to be earlier than the beginning of the run
				if (predictedRetentionTimes[i] < retentionTimes[0] / 60)
					continue;
				
				iPeakCount++;
				
				// Score by scores of individual peaks selected
				if (permutation[i] < 0)
					continue;

				double dPeakScore = peaks.get(i).get(permutation[i])[3];
				dTotalPeakScores += dPeakScore;
				
				if (permutation[i] < 0 || permutation[i - 1] < 0)
					continue;
				
				// Score by distance between peaks
				// Should be very similar to the spacing predicted

				double dActualDistance = (peaks.get(i).get(permutation[i])[0] - peaks.get(i - 1).get(permutation[i - 1])[0]) / 60;
				double dPredictedDistance = predictedRetentionTimes[i] - predictedRetentionTimes[i - 1];
				double dDistanceErrorPercent = Math.abs(dActualDistance - dPredictedDistance) / dPredictedDistance;
				dTotalDistanceError += dDistanceErrorPercent;

				// Score by intensity
				// Should be close to the nearby intensities

				double dIntensityDiff = Math.abs(peaks.get(i).get(permutation[i])[1] - peaks.get(i - 1).get(permutation[i - 1])[1]);
				double dIntensityPercentDiff = dIntensityDiff / Math.max(peaks.get(i).get(permutation[i])[1], peaks.get(i - 1).get(permutation[i - 1])[1]);
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
    
}
