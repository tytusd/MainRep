package boswell.peakfinderlcfx;

import java.awt.Color;
import java.io.IOException;
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

public class PeakFinderLCFX
{
	private PeakFinderSettingsPaneController peakFinderSettingsPaneController;
	private PeakFinderPaneController peakFinderPaneController;
	private ProgressDialogController progressDialogController;
	private Stage progressDialogStage;
	private Stage peakFinderDialogStage;
	
	private double[] sgcoefficients = null;
	private SGFilter sgfilter = null;
	private double[] rawEIC = null;
	private double[] smoothEIC = null;
	private double[] maxIntensities;
	
	private double columnLength = 100; // in m
	private double innerDiameter = 2.1; // in mm
	private double flowRate = 1; // in mL/min=
	private double mixingVolume = 0.1;
	private double nonMixingVolume = 0.2;
    private double theoreticalPlates = 10000;
    private double tStep = 0;

	private double[] retentionTimes = null;
	private double[] selectedRetentionTimes = null;
    private double[] predictedRetentionTimes;
    private double[] predictedPeakWidths;
	public  double[][] simpleGradientArray;	
	private double[][] standardCompoundsMZArray = null;
	private double[][] gradientProgram;
	private double[][] gradientProgramInConventionalForm = {{0,5},{5, 95}};
	private double[][][] mzData = null;
	private double[][][] isocraticDataArray = null;

	private boolean[] skippedStandards = null;
	private int[] selectedPeakRank = null;
	
	private String[] stationaryPhaseArray = null;
	private String[] standardCompoundsNameArray = null;

	private LinearInterpolationFunction interpolatedGradientProfile;  //  @jve:decl-index=0:
	private LinearInterpolationFunction interpolatedDeadTimeProfile;

    
    private Vector<Vector<double[]>> peaks = new Vector<Vector<double[]>>();

 	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();

	OpenMzXMLFileTask openMZFileTask = null;
	FindPeaksTask findPeaksTask = null;

	private String fileName = "";
	private boolean editable = true;
	private boolean okPressed = false;
	
	private Window parentWindow;
	private double instrumentDeadTime;
	
	public PeakFinderLCFX(Window parentWindow, String[] stationaryPhaseNames, boolean editable)
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
			peakFinderSettingsPaneController.setFlowRate(flowRate);
			peakFinderSettingsPaneController.setInnerDiameter(innerDiameter);
			peakFinderSettingsPaneController.setFileName(fileName);
			peakFinderSettingsPaneController.setMixingVolume(mixingVolume);
			peakFinderSettingsPaneController.setNonMixingVolume(nonMixingVolume);
			peakFinderSettingsPaneController.setGradientProgramInConventionalForm(gradientProgramInConventionalForm);
			//TODO: Fix this line above..the method its calling is not working properly
			
			// Set the dialog to the be editable or not
			peakFinderSettingsPaneController.setEditable(editable);

			
			
			// Create the scene
			Scene scene = new Scene(root, 60*rem, 43*rem);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			dialog.setTitle("Load a LC-MS Data File");
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
			peakFinderSettingsPaneController.performValidations();
			fileName = peakFinderSettingsPaneController.getFileName();
			flowRate = peakFinderSettingsPaneController.getFlowRate();
			columnLength = peakFinderSettingsPaneController.getColumnLength();
			innerDiameter = peakFinderSettingsPaneController.getInnerDiameter();
			mixingVolume = peakFinderSettingsPaneController.getMixingVolume();
			nonMixingVolume = peakFinderSettingsPaneController.getNonMixingVolume();
			
			//TODO: add gradientProfile
			
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
			progressDialogController.getCancelProperty().addListener(new CancelListener());
			
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
				        	gradientProgram = peakFinderSettingsPaneController.getGradientProgram(retentionTimes);
				        	
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

		Scene peakFinderDialogScene = new Scene(peakFinderPane, 100 * rem, 60 * rem);
		peakFinderDialogScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		//Initialize the peaks vector
		for(int z = 0; z < standardCompoundsNameArray.length; z++){
			peaks.add(new Vector<double[]>());
		}
		
		peakFinderDialogStage.setTitle("Peak Finder");
		peakFinderDialogStage.setScene(peakFinderDialogScene);
		peakFinderDialogStage.initModality(Modality.WINDOW_MODAL);
		peakFinderDialogStage.initOwner(parentWindow);
		peakFinderDialogStage.setMaximized(true);
		//TODO: set interpolated gradient and dead time functions here
		peakFinderPaneController.setStage(peakFinderDialogStage);
		peakFinderPaneController.setInstrumentDeadTime(instrumentDeadTime);
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
		    	interpolatedGradientProfile = new LinearInterpolationFunction(gradientProgram);
		    	//TODO: anything missing here?
		    	
		    	calculateSimpleGradient();
			}
			// Calculate approximate retention times of the standards
	    	predictRetentionTimes();
	    	return null;		
		}
		

		public void calculateSimpleGradient()
		{
			int iNumPoints = 10000;
			// Create an array for the simple gradient
			simpleGradientArray = new double[iNumPoints][2];
					
			// Initialize the solvent mixer composition to that of the initial solvent composition
			double dMixerComposition = gradientProgram[0][1];
			//double dFinalTime = (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) + ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iGradientTableLength - 1, 0)).doubleValue();
			double dFinalTime = gradientProgram[gradientProgram.length - 1][0]; // in min
			double dTimeStep = dFinalTime / (iNumPoints - 1);
			
			// Start at time 0
			double dTime = 0;
			double dTotalSolventVolumeMoved = 0;
			double dNonMixingDelayTime = 0;
			
			for (int i = 0; i < iNumPoints; i++)
			{
				dTime = i * dTimeStep;
				
				simpleGradientArray[i][0] = dTime;
				simpleGradientArray[i][1] = dMixerComposition;
				
				double dSolventBInMixer = dMixerComposition * mixingVolume;
								
				// Now push out a step's worth of volume from the mixer
				//dSolventBInMixer -= ((m_dFlowRate * 1000) * dTimeStep) * dMixerComposition;
				dSolventBInMixer -= (flowRate * dTimeStep) * dMixerComposition;
				
				// dSolventBInMixer could be negative if the volume pushed out of the mixer is greater than the total volume of the mixer
				if (dSolventBInMixer < 0)
					dSolventBInMixer = 0;
				
				// Now add a step's worth of new volume from the pump
				// First, find which two data points we are between
				// Find the last data point that isn't greater than our current time
				double dIncomingSolventComposition = 0;
				
				dIncomingSolventComposition = interpolatedGradientProfile.getAt(dTime - dNonMixingDelayTime);
				
				// Add to the total amount of solvent moved.
				dTotalSolventVolumeMoved += dTimeStep * flowRate;
				
				if (dTotalSolventVolumeMoved <= nonMixingVolume)
					dNonMixingDelayTime += dTimeStep;
				
				if ((flowRate * dTimeStep) < mixingVolume)
					dSolventBInMixer += (flowRate * dTimeStep) * dIncomingSolventComposition;
				else
				{
					// The amount of solvent entering the mixing chamber is larger than the mixing chamber. Just set the solvent composition in the mixer to that of the mobile phase.
					dSolventBInMixer = mixingVolume * dIncomingSolventComposition;
				}
				
				// Calculate the new solvent composition in the mixing volume
				if ((flowRate * dTimeStep) < mixingVolume)
					dMixerComposition = dSolventBInMixer / mixingVolume;
				else
					dMixerComposition = dIncomingSolventComposition;
			}
			
			interpolatedGradientProfile = new LinearInterpolationFunction(simpleGradientArray);
		}
		
	    public void predictRetentionTimes()
	    {
	    	double dTimeLimit = gradientProgram[gradientProgram.length - 1][0] * 1.5;
			double dtstep = dTimeLimit * 0.001;

			predictedRetentionTimes = new double[isocraticDataArray.length];
			predictedPeakWidths = new double[isocraticDataArray.length];
			
			int iNumCompounds = isocraticDataArray.length;
			for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
			{
				InterpolationFunction IsocraticData = new InterpolationFunction(isocraticDataArray[iCompound]);

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
					dPhiC = interpolatedGradientProfile.getAt(dTotalTime - dIntegral) / 100;
					// Calculate k'
			    	kprime = Math.pow(10, IsocraticData.getAt(dPhiC));
					dCurVal = dtstep / kprime;
					dt0 = interpolatedDeadTimeProfile.getAt(dPhiC) / 60;
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
					//pw.write(("Compound No: "+iCompound+", t="+Math.round(t*1000)/1000.0d+", dtstep="+Math.round(dtstep*1000)/1000.0d+", dPhiC="+Math.round(dPhiC*1000)/1000.0d+", dCurVal="+Math.round(dCurVal*1000)/1000.0d+", dD="+Math.round(dD*1000)/1000.d+", dIntegral="+Math.round(dIntegral*1000)/1000.d+", kprime="+Math.round(kprime*1000)/1000.0d+", dt0="+Math.round(dt0*1000)/1000.0d+", totaldeadtime="+Math.round(dTotalDeadTime*1000)/1000.0d+", dtRFinal="+Math.round(dtRFinal*1000)/1000.d+", dTotalTime="+Math.round(dTotalTime*1000)/1000.d+", dXMovement="+Math.round(dXMovement*1000)/1000.0d+", dXPosition="+Math.round(dXPosition*1000)/1000.0d+", dLastXPosition="+Arrays.toString(dLastXPosition)+", dLastKo="+Arrays.toString(dLastko)+"\n"));
					
				}
				
				if (bIsEluted)
				{
			    	double dSigma = Math.sqrt(Math.pow((dt0 * (1 + kprime)) / Math.sqrt(theoreticalPlates), 2));
			    	double dMinSigma = Math.sqrt(Math.pow(dt0 / Math.sqrt(theoreticalPlates), 2)) * 6;
			    	predictedPeakWidths[iCompound] = Math.max(dSigma * 2.355, dMinSigma); // Peak width at half height
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
	}
	
	private class CancelListener implements ChangeListener<Boolean>
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) 
		{
			// Cancel button was pressed on the progress dialog
			if (openMZFileTask != null)
				openMZFileTask.cancel();
		}
	}
	
	public void setIsocraticDataArray(double[][][] isocraticDataArray) {
		this.isocraticDataArray = isocraticDataArray;
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
	
	public void setTStep(double dtstep)
	{
		this.tStep = dtstep;
	}
	
	public double getInstrumentDeadTime() {
		return instrumentDeadTime;
	}

	public void setInstrumentDeadTime(double instrumentDeadTime) {
		this.instrumentDeadTime = instrumentDeadTime;
	}

	public double[][] getGradientProgramInConventionalForm() {
		return gradientProgramInConventionalForm;
	}

	public void setGradientProgramInConventionalForm(
			double[][] gradientProgramInConventionalForm) {
		this.gradientProgramInConventionalForm = gradientProgramInConventionalForm;
	}

	public void setInterpolatedGradientProfile(
			double[][] interpolatedGradientProfileArray) {
		this.interpolatedGradientProfile = new LinearInterpolationFunction(interpolatedGradientProfileArray);
	}

	public void setInterpolatedDeadTime(double[][] deadTimeArray) {
		double[][] initialDeadTimeArray = deadTimeArray;
		
		if(deadTimeArray == null){
			// Create dead time array
	        initialDeadTimeArray = new double[GlobalsDan.dDeadTimeArray.length][2];
	        
	        for (int i = 0; i < GlobalsDan.dDeadTimeArray.length; i++)
	        {
	        	double dVolumeInRefColumn = Math.PI * Math.pow(GlobalsDan.dRefColumnID / 2, 2) * GlobalsDan.dRefColumnLength;
	        	double dDeadVolPerVol = (GlobalsDan.dDeadTimeArray[i][1] * GlobalsDan.dRefFlowRate) / dVolumeInRefColumn;
	        	double dNewDeadVol = dDeadVolPerVol * Math.PI * Math.pow((this.innerDiameter / 2) / 10, 2) * this.columnLength / 10;
	        	initialDeadTimeArray[i][0] = GlobalsDan.dDeadTimeArray[i][0];
	        	initialDeadTimeArray[i][1] = (dNewDeadVol / this.flowRate) * 60;
	        }
		}
        this.interpolatedDeadTimeProfile = new LinearInterpolationFunction(initialDeadTimeArray);
	}

}
