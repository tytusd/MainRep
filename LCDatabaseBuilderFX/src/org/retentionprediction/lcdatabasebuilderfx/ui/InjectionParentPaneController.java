package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import org.retentionprediction.lcdatabasebuilderfx.ui.BackcalculateController.BackCalculateControllerListener;
import org.retentionprediction.lcdatabasebuilderfx.ui.FinalFitController.FinalFitControllerListener;
import org.retentionprediction.lcdatabasebuilderfx.ui.MeasuredRetentionTimesController.MeasuredRetentionTimesControllerListener;

public class InjectionParentPaneController implements Initializable, MeasuredRetentionTimesControllerListener, BackCalculateControllerListener, FinalFitControllerListener{

	    @FXML private ScrollPane gradientAanchor;
	   	@FXML private VBox injectionVBox;
	    
	    

	    public InjectionParentPaneControllerListener injectionParentPaneControllerListener;
	    
		private ScrollPane measuredRetentionTimes;
		private ScrollPane backcalculatePane;
		private VBox finalFitPane;
		
		private MeasuredRetentionTimesController measuredRetentionTimesController;
		private BackcalculateController backcalculateController;
		private FinalFitController finalFitController;
		
		private int iCurrentStep;
		private SolveParametersTask solveParametersTask;
	    
    public interface InjectionParentPaneControllerListener {
	   	public void onNew();
	   	public void onOpen();
	   	public void onSave();
	   	public void onSaveAs();
	  	public void onClose();
	   	public void onAbout();
	}
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FXMLLoader loader = new FXMLLoader();
		try {
			measuredRetentionTimes = loader.load(getClass().getResource("MeasuredRetentionTimes.fxml").openStream());
		
			measuredRetentionTimesController = loader.getController();
			measuredRetentionTimesController.setMeasuredRetentionTimesControllerListener(this);
			
			loader = new FXMLLoader();
			backcalculatePane = loader.load(getClass().getResource("Backcalculate.fxml").openStream());
			backcalculateController = loader.getController();
			backcalculateController.setBackCalculateControllerListener(this);
			backcalculateController.setInjectionMode(true);
			
			gradientAanchor.setFitToHeight(true);
			gradientAanchor.setFitToWidth(true);
			gradientAanchor.setContent(measuredRetentionTimes);
			
			measuredRetentionTimesController.setInjectionGradientMode(true);
			
			loader = new FXMLLoader();
			finalFitPane = loader.load(getClass().getResource("FinalFit.fxml").openStream());
			finalFitController = loader.getController();
			finalFitController.setFinalFitControllerListener(this);
			finalFitController.setBackcalculateController(backcalculateController);
			finalFitController.setMeasuredRetentionTimesController(measuredRetentionTimesController);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    @FXML void onNewAction(ActionEvent event) {
    	this.injectionParentPaneControllerListener.onNew();
    }

    @FXML void onOpenAction(ActionEvent event) {
    	this.injectionParentPaneControllerListener.onOpen();
    }

    @FXML void onSaveAction(ActionEvent event) {
    	this.injectionParentPaneControllerListener.onSave();
    }

    @FXML void onSaveAsAction(ActionEvent event) {
    	this.injectionParentPaneControllerListener.onSaveAs();
    }

    @FXML void onCloseAction(ActionEvent event) {
    	this.injectionParentPaneControllerListener.onClose();
    }

    @FXML void onAboutAction(ActionEvent event) {
    	this.injectionParentPaneControllerListener.onAbout();
    }

	public InjectionParentPaneControllerListener getInjectionParentPaneControllerListener() {
		return injectionParentPaneControllerListener;
	}

	public void setInjectionParentPaneControllerListener(
			InjectionParentPaneControllerListener injectionParentPaneControllerListener) {
		this.injectionParentPaneControllerListener = injectionParentPaneControllerListener;
	}
	
	@Override
	public void onNextStepPressed(
			MeasuredRetentionTimesController thisController) {
		gradientAanchor.setContent(backcalculatePane);
		if(thisController == this.measuredRetentionTimesController){
			backcalculateController.resetValues();
			backcalculateController.setInnerDiameter(measuredRetentionTimesController.getInnerDiameter());
			backcalculateController.setFlowRate(measuredRetentionTimesController.getFlowRate());
			backcalculateController.setColumnLength(measuredRetentionTimesController.getColumnLength());
			backcalculateController.setGradientProgram(measuredRetentionTimesController.getGradientProgram());
			backcalculateController.setFileName(measuredRetentionTimesController.getFileName());
			backcalculateController.setInstrumentDeadTime(measuredRetentionTimesController.getInstrumentDeadTime());
			backcalculateController.setStandardsList(measuredRetentionTimesController.getStandardsList());
			this.iCurrentStep++;
			
		}
		
	}

	@Override
	public void onNextStepPressed(BackcalculateController thisController) {
		if (thisController == this.backcalculateController)
		{
			if(this.iCurrentStep == 1){
				backcalculateController.switchToStep4();
			}
			else{
				//TODO: switch to final fit
				gradientAanchor.setContent(finalFitPane);
			}
			this.iCurrentStep++;
			finalFitController.setInstrumentDeadTime(backcalculateController.getInstrumentDeadTime());
		}
		
	}

	@Override
	public void onPreviousStepPressed(BackcalculateController thisController) {
		if (this.iCurrentStep == 2)
		{
			backcalculateController.switchToStep3();
		}
		else if (this.iCurrentStep == 1)
		{
			gradientAanchor.setContent(measuredRetentionTimes);
			measuredRetentionTimes.setVisible(true);
		}
		this.iCurrentStep--;
		
	}
	
	@Override
	public void onPreviousStepPressed(FinalFitController finalFitController) {
		gradientAanchor.setContent(backcalculatePane);
		backcalculateController.switchToStep4();
		this.iCurrentStep--;
	}

	public boolean areThreadsRunning() {
		boolean threadRunning = false;
		
		if (backcalculateController.isThreadRunning())
			threadRunning = true;
		
		
		if (this.solveParametersTask != null && this.solveParametersTask.isRunning())
			threadRunning = true;
		
		return threadRunning;
	}

	public void cancelAllTasks() {
		if(backcalculateController.isThreadRunning()){
			backcalculateController.cancelTasks();
		}
	}


	public void writeSaveData(InjectionSaveData saveData) {
		
		saveData.measuredRetentionTimeSaveData = saveData.new MeasuredRetentionTimeSaveData();
		measuredRetentionTimesController.writeSaveData(saveData.measuredRetentionTimeSaveData);
		saveData.measuredRetentionTimeSaveData.gradientProgramInConventionalForm = measuredRetentionTimesController.getGradientProgram();
		
		saveData.backCalculateSaveData = saveData.new BackCalculateSaveData();
		backcalculateController.writeSaveData(saveData.backCalculateSaveData);
		
		
		saveData.iCurrentStep = iCurrentStep;
		finalFitController.writeSaveData(saveData);
	}

	public void loadSaveData(InjectionSaveData saveData) {
		
		measuredRetentionTimesController.loadSaveData(saveData.measuredRetentionTimeSaveData);
		backcalculateController.loadSaveData(saveData.backCalculateSaveData);
		
		iCurrentStep = saveData.iCurrentStep;
		
		// Set the visible pane in the tab
		if (iCurrentStep <= 1)
			backcalculateController.switchToStep3();
		else if (iCurrentStep >= 2)
			backcalculateController.switchToStep4();
		
		if(iCurrentStep == 0){
			gradientAanchor.setContent(measuredRetentionTimes);
		}
		
		if(iCurrentStep >= 1){
			gradientAanchor.setContent(backcalculatePane);
		}
		
		finalFitController.loadSaveData(saveData);
	}



}
