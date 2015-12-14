package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.File;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.retentionprediction.lcdatabasebuilderfx.business.Globals;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;
import org.retentionprediction.lcdatabasebuilderfx.business.UIComponents;
import org.retentionprediction.lcdatabasebuilderfx.business.Utilities;
import org.retentionprediction.lcdatabasebuilderfx.ui.BackcalculateController.BackCalculateControllerListener;
import org.retentionprediction.lcdatabasebuilderfx.ui.MeasuredRetentionTimesController.MeasuredRetentionTimesControllerListener;
import org.retentionprediction.lcdatabasebuilderfx.ui.SolveParametersTask.SolveParametersListener;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import boswell.graphcontrolfx.GraphControlFX;

public class ParentPaneController implements Initializable, MeasuredRetentionTimesControllerListener, BackCalculateControllerListener, SolveParametersListener {

	private ParentPaneControllerListener parentPaneControllerListener;
	
	@FXML private VBox parentRoot;
	@FXML private AnchorPane gradientAanchor;
	@FXML private AnchorPane gradientBanchor;
	@FXML private AnchorPane gradientCanchor;
	@FXML private AnchorPane gradientDanchor;
	@FXML private AnchorPane gradientEanchor;
	@FXML private AnchorPane gradientFanchor;
	@FXML private AnchorPane gradientGanchor;
	@FXML private AnchorPane gradientHanchor;
	@FXML private AnchorPane anchorPaneRetentionSolver;

    @FXML private TextField textRetentionTimeA;
    @FXML private TextField textRetentionTimeB;
    @FXML private TextField textRetentionTimeC;
	@FXML private TextField textRetentionTimeD;
    @FXML private TextField textRetentionTimeE;
    @FXML private TextField textRetentionTimeF;
    @FXML private TextField textRetentionTimeG;
    @FXML private TextField textRetentionTimeH;
    
    @FXML private Label labelA;
    @FXML private Label labelB;
    @FXML private Label labelC;
    @FXML private Label labelD;
    @FXML private Label labelE;
    @FXML private Label labelF;
    @FXML private Label labelG;

    @FXML private Tab gradientAtab;
    @FXML private Tab gradientBtab;
    @FXML private Tab gradientCtab;
    @FXML private Tab gradientDtab;
    @FXML private Tab gradientEtab;
    @FXML private Tab gradientFtab;
    @FXML private Tab gradientGtab;
    @FXML private Tab gradientHtab;
    
    @FXML private Label labelGradientAStatus;
    @FXML private Label labelGradientBStatus;
    @FXML private Label labelGradientCStatus;
    
    @FXML private Label labelGradientDStatus;
    @FXML private Label labelGradientEStatus;
    @FXML private Label labelGradientFStatus;
    @FXML private Label labelGradientGStatus;
    @FXML private Label labelGradientHStatus;

    @FXML private Label labelOverallStatus;
    
    @FXML private Label enterTimesALabel;
    @FXML private Label enterTimesBLabel;
    @FXML private Label enterTimesCLabel;
    @FXML private Label enterTimesDLabel;
    @FXML private Label enterTimesELabel;
    @FXML private Label enterTimesFLabel;
    @FXML private Label enterTimesGLabel;
    @FXML private Label enterTimesHLabel;
    
    @FXML private Label gradientALabel;
    @FXML private Label gradientBLabel;
    @FXML private Label gradientCLabel;
    @FXML private Label gradientDLabel;
    @FXML private Label gradientELabel;
    @FXML private Label gradientFLabel;
    @FXML private Label gradientGLabel;
    @FXML private Label gradientHLabel;

    @FXML private Label backCalculateALabel;
    @FXML private Label backCalculateBLabel;
    @FXML private Label backCalculateCLabel;
    @FXML private Label backCalculateDLabel;
    @FXML private Label backCalculateELabel;
    @FXML private Label backCalculateFLabel;
    @FXML private Label backCalculateGLabel;
    @FXML private Label backCalculateHLabel;
    

    @FXML private Label checkSystemSuitabilityALabel;
    @FXML private Label checkSystemSuitabilityBLabel;
    @FXML private Label checkSystemSuitabilityCLabel;
    @FXML private Label checkSystemSuitabilityDLabel;
    @FXML private Label checkSystemSuitabilityELabel;
    @FXML private Label checkSystemSuitabilityFLabel;
    @FXML private Label checkSystemSuitabilityGLabel;
    @FXML private Label checkSystemSuitabilityHLabel;
    
    @FXML private StackPane progressGraphPane;
    @FXML private StackPane progressPane;
    
    @FXML private TextField textInchiKey;
    @FXML private TableColumn<StandardCompound, String> columnMeasuredRetentionTime;
    @FXML private ProgressBar progressOverall;

	@FXML private Label labelAZero;
	@FXML private Label labelAOne;
	@FXML private Label labelATwo;
	@FXML private Label labelBOne;
	@FXML private Label labelBTwo;
    
    @FXML private TextField textInchi;
    @FXML private Label labelTimeElapsed;
    @FXML private TableColumn<StandardCompound, String> columnError;
    @FXML private Label finalFitLabel;
    @FXML private TableColumn<StandardCompound, String> columnProgram;
    @FXML private GridPane roadMapGrid;
    @FXML private TableColumn<StandardCompound, String> columnPredictedRetentionTime;
    @FXML private TitledPane paneSolveForParameters;
    @FXML private Label labelStatus;
    @FXML private Label labelIteration;
    @FXML private TextField textCompoundName;
    @FXML private TextField textPubChemID;

    @FXML private TextField textSmiles;
    @FXML private ProgressBar progressBar;
    @FXML private Button buttonSolve;
    @FXML private Button exportToXml;
    @FXML private Label labelVariance;
    @FXML private TableView<StandardCompound> tableRetentionTimes;
    @FXML private TextField textFormula;
    @FXML private TextField textIupacName;
    @FXML private TabPane tabPane;
    @FXML private Tab finalfittab;
    @FXML private Pane drawPane;
    
    private ScrollPane[] measuredRetentionTimes = new ScrollPane[8];
    private ScrollPane[] backCalculatePane = new ScrollPane[8];
    private BackcalculateController[] backcalculateController = new BackcalculateController[8];
    private MeasuredRetentionTimesController[] measuredRetentionTimesController = new MeasuredRetentionTimesController[8];
    private int[] iCurrentStep = new int[8];
    private ObservableList<StandardCompound> programList = FXCollections.observableArrayList();
    private SolveParametersTask solveParametersTask;

    private final double rem = javafx.scene.text.Font.getDefault().getSize();
    private Preferences  prefs = Preferences.userNodeForPackage(this.getClass());
	private GraphControlFX retentionSolverTimeGraph;
    
	private boolean[] isDeadTimeRemoved = new boolean[8];
	private String[]  textRetentionTimes = {"0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0"};
	private DecimalFormat df = new DecimalFormat("#.####");
	
    public interface ParentPaneControllerListener {

    	public void onNew();
    	public void onOpen();
    	public void onSave();
    	public void onSaveAs();
    	public void onClose();
    	public void onAbout();
    }

    
    @Override
	public void initialize(URL location, ResourceBundle resources) {
		try{
			
			for (int i = 0; i < iCurrentStep.length; i++)
			{
				FXMLLoader fxmlLoader = new FXMLLoader();
				backCalculatePane[i] = (ScrollPane)fxmlLoader.load(getClass().getResource("Backcalculate.fxml").openStream());
				backcalculateController[i] = fxmlLoader.getController();
				backcalculateController[i].setBackCalculateControllerListener(this);
			}
			
			for(int i = 0; i < iCurrentStep.length; i++){
				FXMLLoader fxmlLoader = new FXMLLoader();
				measuredRetentionTimes[i] = fxmlLoader.load(getClass().getResource("MeasuredRetentionTimes.fxml").openStream());
				measuredRetentionTimesController[i] = fxmlLoader.getController();
				measuredRetentionTimesController[i].setMeasuredRetentionTimesControllerListener(this);
				measuredRetentionTimesController[i].setIndex(i);
				measuredRetentionTimesController[i].setGradientProgramInConventionalForm(Globals.dGradientPrograms[i]);
			}
			gradientAtab.setContent(measuredRetentionTimes[0]);
			gradientBtab.setContent(measuredRetentionTimes[1]);
			gradientCtab.setContent(measuredRetentionTimes[2]);
			gradientDtab.setContent(measuredRetentionTimes[3]);
			gradientEtab.setContent(measuredRetentionTimes[4]);
			gradientFtab.setContent(measuredRetentionTimes[5]);
			gradientGtab.setContent(measuredRetentionTimes[6]);
			gradientHtab.setContent(measuredRetentionTimes[7]);

			setupAllLinesInDrawPane();
			
			ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) 
				{
					if (newPropertyValue == false)
					{
						// Lost focus, so commit the text
						performValidations();
					}	
				}
			};
			
			textRetentionTimeA.focusedProperty().addListener(changeListener);
			textRetentionTimeB.focusedProperty().addListener(changeListener);
			textRetentionTimeC.focusedProperty().addListener(changeListener);
			textRetentionTimeD.focusedProperty().addListener(changeListener);
			textRetentionTimeE.focusedProperty().addListener(changeListener);
			textRetentionTimeF.focusedProperty().addListener(changeListener);
			textRetentionTimeG.focusedProperty().addListener(changeListener);
			textRetentionTimeH.focusedProperty().addListener(changeListener);
			
			retentionSolverTimeGraph = new GraphControlFX();
			retentionSolverTimeGraph.setControlsEnabled(false);
			retentionSolverTimeGraph.setYAxisTitle("log k");
			retentionSolverTimeGraph.setYAxisBaseUnit("units", "units");
			retentionSolverTimeGraph.setYAxisScientificNotation(true);
			retentionSolverTimeGraph.setYAxisRangeIndicatorsVisible(true);
			retentionSolverTimeGraph.setAutoScaleY(true);
			retentionSolverTimeGraph.setYAxisRangeIndicatorsVisible(false);
			
			retentionSolverTimeGraph.setXAxisType(false);
			retentionSolverTimeGraph.setXAxisRangeIndicatorsVisible(false);
			retentionSolverTimeGraph.setXAxisTitle("Eluent Composition");
			retentionSolverTimeGraph.setXAxisBaseUnit("%B", "%B");
			retentionSolverTimeGraph.setAutoScaleX(true);
			retentionSolverTimeGraph.setSelectionCursorVisible(false);
			
			anchorPaneRetentionSolver.getChildren().add(retentionSolverTimeGraph);
			
			AnchorPane.setTopAnchor(retentionSolverTimeGraph, 0.0);
			AnchorPane.setBottomAnchor(retentionSolverTimeGraph, 0.0);
			AnchorPane.setLeftAnchor(retentionSolverTimeGraph, 0.0);
			AnchorPane.setRightAnchor(retentionSolverTimeGraph, 0.0);
			retentionSolverTimeGraph.widthProperty().bind(anchorPaneRetentionSolver.widthProperty().subtract(rem));
			retentionSolverTimeGraph.heightProperty().bind(anchorPaneRetentionSolver.heightProperty().subtract(rem));

			df.setRoundingMode(RoundingMode.CEILING);
			
			setupTextRetentionTimesListener(textRetentionTimeA, 0);
			setupTextRetentionTimesListener(textRetentionTimeB, 1);
			setupTextRetentionTimesListener(textRetentionTimeC, 2);
			setupTextRetentionTimesListener(textRetentionTimeD, 3);
			setupTextRetentionTimesListener(textRetentionTimeE, 4);
			setupTextRetentionTimesListener(textRetentionTimeF, 5);
			setupTextRetentionTimesListener(textRetentionTimeG, 6);
			setupTextRetentionTimesListener(textRetentionTimeH, 7);
			
			for(int i = 0; i < 8; i++){
				setupStationaryPhaseVariableListener(measuredRetentionTimesController[i].getTextFieldColumnLength(), i, "columnLength");
				setupStationaryPhaseVariableListener(measuredRetentionTimesController[i].getTextFieldFlowRate(), i, "flowRate");
				setupStationaryPhaseVariableListener(measuredRetentionTimesController[i].getTextFieldInnerDiameter(), i, "innerDiameter");
				setupStationaryPhaseVariableListener(measuredRetentionTimesController[i].getTextFieldInstrumentDeadTime(), i, "instrumentDeadTime");
			}

	    	solveParametersTask = new SolveParametersTask();
			performValidations();
			
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		columnProgram.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("name"));
	    columnMeasuredRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("measuredRetentionTimeString"));
	    
	    columnPredictedRetentionTime.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>()
	    {
	    	@Override
	        public ObservableValue<String> call(TableColumn.CellDataFeatures<StandardCompound, String> p) 
	        {
	            if (p.getValue() != null) 
	            {
	            	return p.getValue().predictedRetentionTimeStringBindingObservableValue();
	            } 
	            else 
	            {
	            	return null;
	            }
	        }
	    });
	    columnError.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>()
	    {
	    	@Override
	        public ObservableValue<String> call(TableColumn.CellDataFeatures<StandardCompound, String> p) 
	        {
	            if (p.getValue() != null) 
	            {
	            	return p.getValue().errorStringBindingObservableValue();
	            } 
	            else 
	            {
	            	return null;
	            }
	        }
	    });
	    
	    for (int i = 0; i < iCurrentStep.length; i++)
	    {
	    	StandardCompound newTestCompound = new StandardCompound();
	    	newTestCompound.setIndex(i);
	    	if (i == 0)
	    		newTestCompound.setName("Gradient A");
	    	else if (i == 1)
	    		newTestCompound.setName("Gradient B");
	    	else if (i == 2)
	    		newTestCompound.setName("Gradient C");
	    	else if (i == 3)
	    		newTestCompound.setName("Gradient D");
	    	else if (i == 4)
	    		newTestCompound.setName("Gradient E");
	    	else if (i == 5)
	    		newTestCompound.setName("Gradient F");
	    	else if(i == 6)
	    		newTestCompound.setName("Gradient G");
	    	else if(i == 7)
	    		newTestCompound.setName("Gradient H");
	    	programList.add(newTestCompound);
	    }
	    
	    tableRetentionTimes.setItems(programList);
	    exportToXml.setDisable(true);
	}
    
    /**
     * This function takes in a TextField type object and its index as a gradient out of 8 gradients.
     * It automatically removes instrument dead time from the retention time. 
     * Once the field loses focus, that is done. If the field was clicked on without any changes, compare 
     * with the last stored values for retention times and see if nothing has changed, then don't remove dead time again. 
     * @param field
     * @param index
     */
    public void setupTextRetentionTimesListener(TextField field, int index){
    	field.focusedProperty().addListener(new ChangeListener<Boolean>()
    			{
    			    @Override
    			    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
    			    {
    			    	if(!field.getText().trim().equals("")){
	    			    	double value = Double.parseDouble(field.getText());
	    			    	isDeadTimeRemoved[index] = value == Double.parseDouble(textRetentionTimes[index]);
	    			        if (oldPropertyValue && !isDeadTimeRemoved[index] )
	    			        {
	    			        	double newValue = value - measuredRetentionTimesController[index].getInstrumentDeadTime();
	    			        	String newValueStr = df.format(newValue); 
	    			        	field.setText(newValueStr);
	    			        	textRetentionTimes[index] = newValueStr;
	    			        	isDeadTimeRemoved[index] = true;
	    			        	value = Double.parseDouble(newValueStr);
	    			        }
	    			        programList.get(index).setMeasuredRetentionTime(value);
	    			        
    			    	}
    			    }
    			});
    }
    
    public void setupStationaryPhaseVariableListener(TextField field, int index, String variableName){
    	field.focusedProperty().addListener(new ChangeListener<Boolean>(){

			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				double value = Double.parseDouble(field.getText());
				for(int i = 0; i < 8; i++){
					if(variableName.equals("columnLength")){
						measuredRetentionTimesController[i].setColumnLength(value);
					}
					else if(variableName.equals("instrumentDeadTime")){
						measuredRetentionTimesController[i].setInstrumentDeadTime(value);
					}
					else if(variableName.equals("innerDiameter")){
						measuredRetentionTimesController[i].setInnerDiameter(value);
					}
					else if(variableName.equals("flowRate")){
						measuredRetentionTimesController[i].setFlowRate(value);
					}
					measuredRetentionTimesController[i].setTextFieldsByStationaryPhaseVars();
				}
			}
    		
    	});
    }
    
    @FXML void onNewAction(ActionEvent event) {
    	this.parentPaneControllerListener.onNew();
    }

    @FXML void onOpenAction(ActionEvent event) {
    	this.parentPaneControllerListener.onOpen();
    }

    @FXML void onSaveAction(ActionEvent event) {
    	this.parentPaneControllerListener.onSave();
    }

    @FXML void onSaveAsAction(ActionEvent event) {
    	this.parentPaneControllerListener.onSaveAs();
    }

    @FXML void onCloseAction(ActionEvent event) {
    	this.parentPaneControllerListener.onClose();
    }

    @FXML void onAboutAction(ActionEvent event) {
    	this.parentPaneControllerListener.onAbout();
    }

    @FXML void tabSelectionChanged(Event event) {
    	updateRoadMap();
		performValidations();
		updateFinalFitProgress();
    }

    @FXML void onCommitRetentionTime(TableColumn.CellEditEvent<StandardCompound,String> t) {
    	Double dNewRetentionTime = 0.0;
		try
		{
			dNewRetentionTime = Double.valueOf(t.getNewValue());
		}
		catch (NumberFormatException e)
		{
			dNewRetentionTime = 0.0;
		}
		
		// Get the current item that was changed
		StandardCompound currentItem = (StandardCompound)t.getTableView().getItems().get(t.getTablePosition().getRow());

		// Commit the new retention time
		currentItem.setMeasuredRetentionTime(dNewRetentionTime);
    }

    @FXML void onSolveForRetentionParameters(ActionEvent event) {
    	exportToXml.setDisable(true);
    	solveParametersTask.setBackCalculateController(this.backcalculateController);
    	solveParametersTask.setProgramList(programList);
    	solveParametersTask.setGraphControl(retentionSolverTimeGraph);
    	solveParametersTask.setSolveParametersListener(this);
    	solveParametersTask.setCopyToClipboardButton(exportToXml);
    	labelIteration.textProperty().bind(solveParametersTask.getIterationProperty());
		labelTimeElapsed.textProperty().bind(solveParametersTask.getTimeElapsedProperty());
		labelVariance.textProperty().bind(solveParametersTask.getVarianceProperty());
		labelAZero.textProperty().bind(solveParametersTask.getAZeroProperty());
		labelAOne.textProperty().bind(solveParametersTask.getAOneProperty());
		labelATwo.textProperty().bind(solveParametersTask.getATwoProperty());
		labelBOne.textProperty().bind(solveParametersTask.getBOneProperty());
		labelBTwo.textProperty().bind(solveParametersTask.getBTwoProperty());
		labelStatus.textProperty().bind(solveParametersTask.messageProperty());
    	progressBar.progressProperty().bind(solveParametersTask.getProgressBarProperty());
    	Thread thread = new Thread(solveParametersTask);
    	thread.start();
    	
    }
    
    @FXML void toggleProgressGraph(ActionEvent event) {
    	
    }
    
    @FXML void actionPerformValidation(ActionEvent event){
    	performValidations();
    }

	@Override
	public void onNextStepPressed(
			MeasuredRetentionTimesController thisController) {
		int i = 0;
		if (thisController == this.measuredRetentionTimesController[0])
		{
			i = 0;
			gradientAtab.setContent(backCalculatePane[0]);
		}
		else if (thisController == this.measuredRetentionTimesController[1])
		{
			i = 1;
			gradientBtab.setContent(backCalculatePane[1]);
		}
		else if (thisController == this.measuredRetentionTimesController[2])
		{
			i = 2;
			gradientCtab.setContent(backCalculatePane[2]);
		}
		else if (thisController == this.measuredRetentionTimesController[3])
		{
			i = 3;
			gradientDtab.setContent(backCalculatePane[3]);
		}
		else if (thisController == this.measuredRetentionTimesController[4])
		{
			i = 4;
			gradientEtab.setContent(backCalculatePane[4]);
		}
		else if (thisController == this.measuredRetentionTimesController[5])
		{
			i = 5;
			gradientFtab.setContent(backCalculatePane[5]);
		}
		else if (thisController == this.measuredRetentionTimesController[6])
		{
			i = 6;
			gradientGtab.setContent(backCalculatePane[6]);
		}
		else if (thisController == this.measuredRetentionTimesController[7])
		{
			i = 7;
			gradientHtab.setContent(backCalculatePane[7]);
		}
		
		if(thisController == this.measuredRetentionTimesController[i]){
			switchToBackCalculatePane(measuredRetentionTimesController[i], backcalculateController[i]);
			this.iCurrentStep[i]++;
			updateRoadMap();
		}
	}
	
	public void onNextStepPressed(BackcalculateController thisController) {
		for(int i = 0; i < 8; i++){
			if (thisController == this.backcalculateController[i])
			{
				backcalculateController[i].switchToStep4();
				backcalculateController[i].autoFillMeasuredValuesInSystemSuitability(Globals.systemSuitabilityMeasuredValues[i]);
				this.iCurrentStep[i]++;
				updateRoadMap();
				break;
			}
		}
	}
	
	public void onPreviousStepPressed(BackcalculateController thisController) {
		if (thisController == this.backcalculateController[0])
		{
			int i = 0;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientAtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backcalculateController[1])
		{
			int i = 1;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientBtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backcalculateController[2])
		{
			int i = 2;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientCtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backcalculateController[3])
		{
			int i = 3;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientDtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backcalculateController[4])
		{
			int i = 4;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientEtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backcalculateController[5])
		{
			int i = 5;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientFtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}		
		else if (thisController == this.backcalculateController[6])
		{
			int i = 6;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientGtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backcalculateController[7])
		{
			int i = 7;
			if (this.iCurrentStep[i] == 2)
			{
				backcalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				gradientHtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
	}
	
	private void switchToBackCalculatePane(MeasuredRetentionTimesController measuredRetentionTimesController, BackcalculateController backCalculateController)
	{
		backCalculateController.resetValues();
		backCalculateController.setInnerDiameter(measuredRetentionTimesController.getInnerDiameter());
		backCalculateController.setFlowRate(measuredRetentionTimesController.getFlowRate());
		backCalculateController.setColumnLength(measuredRetentionTimesController.getColumnLength());
		backCalculateController.setGradientProgram(measuredRetentionTimesController.getGradientProgram());
		backCalculateController.setFileName(measuredRetentionTimesController.getFileName());
		backCalculateController.setInstrumentDeadTime(measuredRetentionTimesController.getInstrumentDeadTime());
		backCalculateController.setStandardsList(measuredRetentionTimesController.getStandardsList());
	}
	
	public void updateFinalFitProgress(){
		for (int i = 0; i < backcalculateController.length; i++)
		{
			if (backcalculateController[i] == null)
				return;
		}
		
		boolean bIsAcceptableToSolveForParameters = true;
		for (int i = 0; i < backcalculateController.length; i++)
		{
			if (backcalculateController[i].isBackcalculateDone() == false)
				bIsAcceptableToSolveForParameters = false;
		}
		
		// Make the Solve pane disabled if one of the programs is incomplete.
		this.paneSolveForParameters.setDisable(!bIsAcceptableToSolveForParameters);
		
		statusForFinalFitByLabel(labelGradientAStatus, 0);
		statusForFinalFitByLabel(labelGradientBStatus, 1);
		statusForFinalFitByLabel(labelGradientCStatus, 2);
		statusForFinalFitByLabel(labelGradientDStatus, 3);
		statusForFinalFitByLabel(labelGradientEStatus, 4);
		statusForFinalFitByLabel(labelGradientFStatus, 5);
		statusForFinalFitByLabel(labelGradientGStatus, 6);
		statusForFinalFitByLabel(labelGradientHStatus, 7);
		
		int backCalculationDoneCount = 0;
		int systemSuitabilityCheckDoneCount = 0;
		
		for(int i = 0; i < 8; i++){
			if(backcalculateController[i].isBackcalculateDone()){
				backCalculationDoneCount++;
			}
			if(backcalculateController[i].getStatus() != backcalculateController[i].INCOMPLETE){
				systemSuitabilityCheckDoneCount++;
			}
		}
		
		labelOverallStatus.setText(backCalculationDoneCount + " Gradients Backcalculated\n" + systemSuitabilityCheckDoneCount + " System Suitability Check completed\n");
		if(backCalculationDoneCount == 8){
			labelOverallStatus.setTextFill(Color.GREEN);
		}
		else{
			labelOverallStatus.setTextFill(Color.RED);
		}
	}
	
	public void statusForFinalFitByLabel(Label label, int index){
		if (backcalculateController[index].getStatus() == backcalculateController[index].INCOMPLETE)
		{
			if(backcalculateController[index].isBackcalculateDone()){
				label.setText("Backcalculation done.");	
			}
			else{
				label.setText("Backcalculation not done.");
			}
			label.setTextFill(Color.BLACK);
		}
		else if (backcalculateController[index].getStatus() == backcalculateController[index].PASSED)
		{
			label.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backcalculateController[index].getScore(), 2)) + ")");
			label.setTextFill(Color.GREEN);
		}
		else if (backcalculateController[index].getStatus() == backcalculateController[index].PASSEDBUTQUESTIONABLE)
		{
			label.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backcalculateController[index].getScore(), 2)) + ")");
			label.setTextFill(Color.YELLOW);
		}
		else if (backcalculateController[index].getStatus() == backcalculateController[index].FAILED)
		{
			label.setText("Failed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backcalculateController[index].getScore(), 2)) + ")");
			label.setTextFill(Color.RED);
		}
	}
	
	private void performValidations()
	{
		validateMeasuredRetentionTime(textRetentionTimeA, 0);
		validateMeasuredRetentionTime(textRetentionTimeB, 1);
		validateMeasuredRetentionTime(textRetentionTimeC, 2);
		validateMeasuredRetentionTime(textRetentionTimeD, 3);
		validateMeasuredRetentionTime(textRetentionTimeE, 4);
		validateMeasuredRetentionTime(textRetentionTimeF, 5);
		validateMeasuredRetentionTime(textRetentionTimeG, 6);
		validateMeasuredRetentionTime(textRetentionTimeH, 7);
		
		if (this.buttonSolve != null)
		{
			int answerCount = 0;
			for (int i = 0; i < programList.size(); i++)
			{
				if (programList.get(i).getMeasuredRetentionTime() > 0)
					answerCount++;
			}
			
			if (answerCount >= 3)
				this.buttonSolve.setDisable(false);
			else
				this.buttonSolve.setDisable(true);
		}
	}
	
	/**
	 * This method validates the retention times.
	 * @param textRetentionTime This parameter is the text field for variables such as textRetentionTimeA
	 * @param programListIndex
	 */
	private void validateMeasuredRetentionTime(TextField textRetentionTime, int programListIndex){
		if (textRetentionTime == null)
			return;
		
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textRetentionTime.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
    	if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 1000000)
			dTemp = 1000000;
		
		if (dTemp == 0)
			textRetentionTime.setText("");
		else
			textRetentionTime.setText(Float.toString((float)dTemp));
		
		if (programList.size() > 0)
			programList.get(programListIndex).setMeasuredRetentionTime(dTemp);
	}
	
	public void cancelAllTasks()
	{
		for (int i = 0; i < backcalculateController.length; i++)
		{
			if (backcalculateController[i] != null)
				backcalculateController[i].cancelTasks();
		}
	}
	
	public boolean areThreadsRunning()
	{
		boolean threadRunning = false;
		for (int i = 0; i < backcalculateController.length; i++)
		{
			if (backcalculateController[i].isThreadRunning())
				threadRunning = true;
		}
		
		if (this.solveParametersTask != null && this.solveParametersTask.isRunning())
			threadRunning = true;
		
		return threadRunning;
	}
    
	public void loadSaveData(SaveData saveData) {
		for (int i = 0; i < saveData.measuredRetentionTimeSaveData.length; i++)
		{
			measuredRetentionTimesController[i].loadSaveData(saveData.measuredRetentionTimeSaveData[i]);
		}
			
		for (int i = 0; i < saveData.backCalculateSaveData.length; i++)
		{
			backcalculateController[i].loadSaveData(saveData.backCalculateSaveData[i]);
		}
		
		iCurrentStep = saveData.iCurrentStep;
		solveParametersTask.setSolverDone(saveData.finalFitComplete);
		programList = saveData.programList;
		labelAZero.textProperty().unbind();
		labelAZero.setText(saveData.labelAZeroText);
		labelAOne.textProperty().unbind();
		labelAOne.setText(saveData.labelAOneText);
		labelATwo.textProperty().unbind();
		labelATwo.setText(saveData.labelATwoText);
		labelBOne.textProperty().unbind();
		labelBOne.setText(saveData.labelBOneText);
		labelBTwo.textProperty().unbind();
		labelBTwo.setText(saveData.labelBTwoText);
		labelIteration.textProperty().unbind();
		labelIteration.setText(saveData.labelIterationText);
		labelVariance.textProperty().unbind();
		labelVariance.setText(saveData.labelVarianceText);
		labelTimeElapsed.textProperty().unbind();
		labelTimeElapsed.setText(saveData.labelTimeElapsedText);
		labelStatus.textProperty().unbind();
		labelStatus.setText(saveData.statusText);
		textCompoundName.setText(saveData.compoundName);
		textFormula.setText(saveData.formula);
		textPubChemID.setText(saveData.pubChemID);
		textInchi.setText(saveData.inchi);
		textInchiKey.setText(saveData.inchiKey);
		textSmiles.setText(saveData.smiles);
		textIupacName.setText(saveData.iupacName);
		textRetentionTimeA.setText(saveData.retentionTimeA);
		textRetentionTimeB.setText(saveData.retentionTimeB);
		textRetentionTimeC.setText(saveData.retentionTimeC);
		textRetentionTimeD.setText(saveData.retentionTimeD);
		textRetentionTimeE.setText(saveData.retentionTimeE);
		textRetentionTimeF.setText(saveData.retentionTimeF);
		textRetentionTimeG.setText(saveData.retentionTimeG);
		textRetentionTimeH.setText(saveData.retentionTimeH);
		textRetentionTimes[0] = saveData.retentionTimeA;
		textRetentionTimes[1] = saveData.retentionTimeB;
		textRetentionTimes[2] = saveData.retentionTimeC;
		textRetentionTimes[3] = saveData.retentionTimeD;
		textRetentionTimes[4] = saveData.retentionTimeE;
		textRetentionTimes[5] = saveData.retentionTimeF;
		textRetentionTimes[6] = saveData.retentionTimeG;
		textRetentionTimes[7] = saveData.retentionTimeH;

		solveParametersTask.setBackCalculateController(this.backcalculateController);
    	solveParametersTask.setProgramList(programList);
    	solveParametersTask.setGraphControl(retentionSolverTimeGraph);
    	solveParametersTask.setSolveParametersListener(this);
    	solveParametersTask.setCopyToClipboardButton(exportToXml);
		
		if(solveParametersTask.isSolverDone()){
			exportToXml.setDisable(false);
			double a0 = Double.parseDouble(labelAZero.getText());
			double a1 = Double.parseDouble(labelAOne.getText());
			double a2 = Double.parseDouble(labelATwo.getText());
			double b1 = Double.parseDouble(labelBOne.getText());
			double b2 = Double.parseDouble(labelBTwo.getText());
			solveParametersTask.updateGraphs(a0, a1, a2, b1, b2);
		}
		
		// Set the visible pane in the tab
		for (int i = 0; i < this.backcalculateController.length; i++)
		{
			if (iCurrentStep[i] <= 1)
				backcalculateController[i].switchToStep3();
			else if (iCurrentStep[i] >= 2)
				backcalculateController[i].switchToStep4();
		}
		
		if (iCurrentStep[0] == 0)
			gradientAtab.setContent(measuredRetentionTimes[0]);
		if (iCurrentStep[1] == 0)
			gradientBtab.setContent(measuredRetentionTimes[1]);
		if (iCurrentStep[2] == 0)
			gradientCtab.setContent(measuredRetentionTimes[2]);
		if (iCurrentStep[3] == 0)
			gradientDtab.setContent(measuredRetentionTimes[3]);
		if (iCurrentStep[4] == 0)
			gradientEtab.setContent(measuredRetentionTimes[4]);
		if (iCurrentStep[5] == 0)
			gradientFtab.setContent(measuredRetentionTimes[5]);
		if (iCurrentStep[6] == 0)
			gradientGtab.setContent(measuredRetentionTimes[6]);
		if (iCurrentStep[7] == 0)
			gradientHtab.setContent(measuredRetentionTimes[7]);
		
		
		if (iCurrentStep[0] >= 1)
			gradientAtab.setContent(backCalculatePane[0]);
		if (iCurrentStep[1] >= 1)
			gradientBtab.setContent(backCalculatePane[1]);
		if (iCurrentStep[2] >= 1)
			gradientCtab.setContent(backCalculatePane[2]);
		if (iCurrentStep[3] >= 1)
			gradientDtab.setContent(backCalculatePane[3]);
		if (iCurrentStep[4] >= 1)
			gradientEtab.setContent(backCalculatePane[4]);
		if (iCurrentStep[5] >= 1)
			gradientFtab.setContent(backCalculatePane[5]);
		if (iCurrentStep[6] >= 1)
			gradientGtab.setContent(backCalculatePane[6]);
		if (iCurrentStep[7] >= 1)
			gradientHtab.setContent(backCalculatePane[7]);
		
		
	    tableRetentionTimes.setItems(programList);

		this.updateRoadMap();
		this.updateFinalFitProgress();
		
		//TODO: Add performvalidationscheck here
		
	}
	
	public void writeSaveData(SaveData saveData) {
		saveData.measuredRetentionTimeSaveData = new SaveData.MeasuredRetentionTimeSaveData[this.measuredRetentionTimesController.length];
		saveData.backCalculateSaveData = new SaveData.BackCalculateSaveData[backcalculateController.length];
		
		for (int i = 0; i < measuredRetentionTimesController.length; i++)
		{
			saveData.measuredRetentionTimeSaveData[i] = saveData.new MeasuredRetentionTimeSaveData();
			measuredRetentionTimesController[i].writeSaveData(saveData.measuredRetentionTimeSaveData[i]);
			saveData.measuredRetentionTimeSaveData[i].gradientProgramInConventionalForm = measuredRetentionTimesController[i].getGradientProgram();
		}
		
		for (int i = 0; i < backcalculateController.length; i++)
		{
			saveData.backCalculateSaveData[i] = saveData.new BackCalculateSaveData();
			backcalculateController[i].writeSaveData(saveData.backCalculateSaveData[i]);
		}
		
		saveData.iCurrentStep = iCurrentStep;
		saveData.finalFitComplete = solveParametersTask.isSolverDone();
		saveData.programList = programList;
		saveData.labelAZeroText = labelAZero.getText();
		saveData.labelAOneText = labelAOne.getText();
		saveData.labelATwoText = labelATwo.getText();
		saveData.labelBOneText = labelBOne.getText();
		saveData.labelBTwoText = labelBTwo.getText();
		saveData.labelIterationText = labelIteration.getText();
		saveData.labelVarianceText = labelVariance.getText();
		saveData.labelTimeElapsedText = labelTimeElapsed.getText();
		saveData.statusText = labelStatus.getText();
		saveData.compoundName = textCompoundName.getText();
		saveData.formula = textFormula.getText();
		saveData.pubChemID = textPubChemID.getText();
		saveData.iupacName = textIupacName.getText();
		saveData.inchi = textInchi.getText();
		saveData.inchiKey = textInchiKey.getText();
		saveData.smiles = textSmiles.getText();
		saveData.retentionTimeA = textRetentionTimeA.getText();
		saveData.retentionTimeB = textRetentionTimeB.getText();
		saveData.retentionTimeC = textRetentionTimeC.getText();
		saveData.retentionTimeD = textRetentionTimeD.getText();
		saveData.retentionTimeE = textRetentionTimeE.getText();
		saveData.retentionTimeF = textRetentionTimeF.getText();
		saveData.retentionTimeG = textRetentionTimeG.getText();
		saveData.retentionTimeH = textRetentionTimeH.getText();
	}
	
	/*
	 * Getters and setters
	 */
	
	public ParentPaneControllerListener getParentPaneControllerListener() {
		return parentPaneControllerListener;
	}

	public void setParentPaneControllerListener(
			ParentPaneControllerListener parentPaneControllerListener) {
		this.parentPaneControllerListener = parentPaneControllerListener;
	}
	
	
	
	/*
	 * Everything below this is UI stuff
	 */
	
	private void updateRoadMap()
	{
		
		if (gradientAtab.isSelected())
		{
			selectSelectedRoadMapItem(0, iCurrentStep[0], false);
		}
		else if (gradientBtab.isSelected())
		{
			selectSelectedRoadMapItem(1, iCurrentStep[1], false);
		}	
		else if (gradientCtab.isSelected())
		{
			selectSelectedRoadMapItem(2, iCurrentStep[2], false);
		}	
		else if (gradientDtab.isSelected())
		{
			selectSelectedRoadMapItem(3, iCurrentStep[3], false);
		}	
		else if (gradientEtab.isSelected())
		{
			selectSelectedRoadMapItem(4, iCurrentStep[4], false);
		}	
		else if (gradientFtab.isSelected())
		{
			selectSelectedRoadMapItem(5, iCurrentStep[5], false);
		}
		else if(gradientGtab.isSelected())
		{
			selectSelectedRoadMapItem(6, iCurrentStep[6], false);
		}
		else if(gradientHtab.isSelected())
		{
			selectSelectedRoadMapItem(7, iCurrentStep[7], false);
		}
		else if (finalfittab.isSelected())
		{
			selectSelectedRoadMapItem(0, 0, true);
		}
		
		
		int sum = 0;
		for (int i = 0; i < iCurrentStep.length; i++)
		{
			sum += iCurrentStep[i];
		}
		
		double progress = (double)sum / (2 * 6 + 1);
		
		if (solveParametersTask != null && solveParametersTask.isSolverDone())
			progress = 1;
		
		progressOverall.setProgress(progress);
	}
	
	private void selectSelectedRoadMapItem(int iRow, int iColumn, boolean bFinalFit)
	{
		finalFitLabel.setFont(Font.font(null, FontWeight.NORMAL, finalFitLabel.getFont().getSize()));
		enterTimesALabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesALabel.getFont().getSize()));
		enterTimesBLabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesBLabel.getFont().getSize()));
		enterTimesCLabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesCLabel.getFont().getSize()));
		enterTimesDLabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesDLabel.getFont().getSize()));
		enterTimesELabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesELabel.getFont().getSize()));
		enterTimesFLabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesFLabel.getFont().getSize()));
		enterTimesGLabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesGLabel.getFont().getSize()));
		enterTimesHLabel.setFont(Font.font(null, FontWeight.NORMAL, enterTimesHLabel.getFont().getSize()));
		backCalculateALabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateALabel.getFont().getSize()));
		backCalculateBLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateBLabel.getFont().getSize()));
		backCalculateCLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateCLabel.getFont().getSize()));
		backCalculateDLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateDLabel.getFont().getSize()));
		backCalculateELabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateELabel.getFont().getSize()));
		backCalculateFLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateFLabel.getFont().getSize()));
		backCalculateGLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateGLabel.getFont().getSize()));
		backCalculateHLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateHLabel.getFont().getSize()));
		checkSystemSuitabilityALabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityALabel.getFont().getSize()));
		checkSystemSuitabilityBLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityBLabel.getFont().getSize()));
		checkSystemSuitabilityCLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityCLabel.getFont().getSize()));
		checkSystemSuitabilityDLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityDLabel.getFont().getSize()));
		checkSystemSuitabilityELabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityELabel.getFont().getSize()));
		checkSystemSuitabilityFLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityFLabel.getFont().getSize()));
		checkSystemSuitabilityGLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityGLabel.getFont().getSize()));
		checkSystemSuitabilityHLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityHLabel.getFont().getSize()));

		if (bFinalFit == true)
		{
			finalFitLabel.setFont(Font.font(null, FontWeight.BOLD, finalFitLabel.getFont().getSize()));
		}
		else if (iColumn == 0)
		{
			if (iRow == 0)
			{
				enterTimesALabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesALabel.getFont().getSize()));
			}
			else if (iRow == 1)
			{
				enterTimesBLabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesBLabel.getFont().getSize()));
			}
			else if (iRow == 2)
			{
				enterTimesCLabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesCLabel.getFont().getSize()));
			}
			else if (iRow == 3)
			{
				enterTimesDLabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesDLabel.getFont().getSize()));
			}
			else if (iRow == 4)
			{
				enterTimesELabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesELabel.getFont().getSize()));
			}
			else if (iRow == 5)
			{
				enterTimesFLabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesFLabel.getFont().getSize()));
			}
			else if (iRow == 6)
			{
				enterTimesGLabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesGLabel.getFont().getSize()));
			}
			else if (iRow == 7)
			{
				enterTimesHLabel.setFont(Font.font(null, FontWeight.BOLD, enterTimesHLabel.getFont().getSize()));
			}
		}
		else if (iColumn == 1)
		{
			if (iRow == 0)
			{
				backCalculateALabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateALabel.getFont().getSize()));
			}
			else if (iRow == 1)
			{
				backCalculateBLabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateBLabel.getFont().getSize()));
			}
			else if (iRow == 2)
			{
				backCalculateCLabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateCLabel.getFont().getSize()));
			}
			else if (iRow == 3)
			{
				backCalculateDLabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateDLabel.getFont().getSize()));
			}
			else if (iRow == 4)
			{
				backCalculateELabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateELabel.getFont().getSize()));
			}
			else if (iRow == 5)
			{
				backCalculateFLabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateFLabel.getFont().getSize()));
			}
			else if (iRow == 6)
			{
				backCalculateGLabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateGLabel.getFont().getSize()));
			}
			else if (iRow == 7)
			{
				backCalculateHLabel.setFont(Font.font(null, FontWeight.BOLD, backCalculateHLabel.getFont().getSize()));
			}
		}
		else if (iColumn == 2)
		{
			if (iRow == 0)
			{
				checkSystemSuitabilityALabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityALabel.getFont().getSize()));
			}
			else if (iRow == 1)
			{
				checkSystemSuitabilityBLabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityBLabel.getFont().getSize()));
			}
			else if (iRow == 2)
			{
				checkSystemSuitabilityCLabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityCLabel.getFont().getSize()));
			}
			else if (iRow == 3)
			{
				checkSystemSuitabilityDLabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityDLabel.getFont().getSize()));
			}
			else if (iRow == 4)
			{
				checkSystemSuitabilityELabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityELabel.getFont().getSize()));
			}
			else if (iRow == 5)
			{
				checkSystemSuitabilityFLabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityFLabel.getFont().getSize()));
			}
			else if (iRow == 6)
			{
				checkSystemSuitabilityGLabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityGLabel.getFont().getSize()));
			}
			else if (iRow == 7)
			{
				checkSystemSuitabilityHLabel.setFont(Font.font(null, FontWeight.BOLD, checkSystemSuitabilityHLabel.getFont().getSize()));
			}
		}
	}
	
	/**
	 * This method sets up the progress feature of the program where all the required lines are drawn to show users
	 * which steps they need to finish.
	 */
    public void setupAllLinesInDrawPane(){
    	ArrayList<Line> lines = new ArrayList<Line>(32);
    	lines.add(UIComponents.drawLine(gradientALabel, enterTimesALabel));
    	lines.add(UIComponents.drawLine(gradientBLabel, enterTimesBLabel));
    	lines.add(UIComponents.drawLine(gradientCLabel, enterTimesCLabel));
    	lines.add(UIComponents.drawLine(gradientDLabel, enterTimesDLabel));
    	lines.add(UIComponents.drawLine(gradientELabel, enterTimesELabel));
    	lines.add(UIComponents.drawLine(gradientFLabel, enterTimesFLabel));
    	lines.add(UIComponents.drawLine(gradientGLabel, enterTimesGLabel));
    	lines.add(UIComponents.drawLine(gradientHLabel, enterTimesHLabel));
    	lines.add(UIComponents.drawLine(enterTimesALabel, backCalculateALabel));
    	lines.add(UIComponents.drawLine(enterTimesBLabel, backCalculateBLabel));
    	lines.add(UIComponents.drawLine(enterTimesCLabel, backCalculateCLabel));
    	lines.add(UIComponents.drawLine(enterTimesDLabel, backCalculateDLabel));
    	lines.add(UIComponents.drawLine(enterTimesELabel, backCalculateELabel));
    	lines.add(UIComponents.drawLine(enterTimesFLabel, backCalculateFLabel));
    	lines.add(UIComponents.drawLine(enterTimesGLabel, backCalculateGLabel));
    	lines.add(UIComponents.drawLine(enterTimesHLabel, backCalculateHLabel));
    	lines.add(UIComponents.drawLine(backCalculateALabel, checkSystemSuitabilityALabel));
    	lines.add(UIComponents.drawLine(backCalculateBLabel, checkSystemSuitabilityBLabel));
    	lines.add(UIComponents.drawLine(backCalculateCLabel, checkSystemSuitabilityCLabel));
		lines.add(UIComponents.drawLine(backCalculateDLabel, checkSystemSuitabilityDLabel));
		lines.add(UIComponents.drawLine(backCalculateELabel, checkSystemSuitabilityELabel));
		lines.add(UIComponents.drawLine(backCalculateFLabel, checkSystemSuitabilityFLabel));
		lines.add(UIComponents.drawLine(backCalculateGLabel, checkSystemSuitabilityGLabel));
		lines.add(UIComponents.drawLine(backCalculateHLabel, checkSystemSuitabilityHLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityALabel, finalFitLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityBLabel, finalFitLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityCLabel, finalFitLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityDLabel, finalFitLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityELabel, finalFitLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityFLabel, finalFitLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityGLabel, finalFitLabel));
		lines.add(UIComponents.drawLineForFinalFit(checkSystemSuitabilityHLabel, finalFitLabel));
		
		ArrayList<Polygon> polygons = new ArrayList<Polygon>(24);
		for(int i = 0; i < 24; i++){
			polygons.add(UIComponents.drawPolygon(lines.get(i)));
		}
		UIComponents.setupAllShapes(drawPane, lines, polygons, finalFitLabel);

    }

	@Override
	public void onUpdateTable(ObservableList<StandardCompound> list) {
		for (int i = 0; i < list.size(); i++)
		{
			this.programList.get(i).makeEqualTo(list.get(i));
		}
		
	}

	
	@FXML public void exportToXml(){
		Map<String,String> solvermap = null;
		if(solveParametersTask != null){
			solvermap = solveParametersTask.exportToXml();
		}
		
		List<HashMap<String,String>> measuredControllerMaps = new ArrayList<HashMap<String,String>>(8);
		List<HashMap<String,String>> backcalculateControllerMaps = new ArrayList<HashMap<String,String>>(8);
		List<HashMap<String,String>> systemSuitabilityMaps = new ArrayList<HashMap<String,String>>(8);
		
		for(int i = 0; i < 8; i++){
			measuredControllerMaps.add(i, (HashMap<String, String>) measuredRetentionTimesController[i].exportToXml());
			backcalculateControllerMaps.add(i, (HashMap<String, String>) backcalculateController[i].exportStandardsToXml());
			systemSuitabilityMaps.add(i, (HashMap<String, String>) backcalculateController[i].exportSystemSuitabilityInfoToXml());
		}
		
		
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Specify your local database folder");
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("LCXML file (*.lcxml)", "*.lcxml"));
		// Set default directory
		String lastOutputDir = prefs.get("LAST_OUTPUT_DIR", "");
		if (lastOutputDir != "")
		{
			File lastDir = new File(lastOutputDir);
			if (lastDir.exists())
				chooser.setInitialDirectory(lastDir.getParentFile());
		}
		Stage stage = new Stage();
		File outputFile = chooser.showSaveDialog(stage);
		if(outputFile != null){
			
			if (!outputFile.getName().endsWith(".lcxml"))
				outputFile = new File(outputFile.getAbsolutePath() + ".lcxml");
			
			createXml(measuredControllerMaps,backcalculateControllerMaps,systemSuitabilityMaps,solvermap,outputFile);
			prefs.put("LAST_OUTPUT_DIR", outputFile.getAbsolutePath());
		}
	}
	
	public void createXml(List<HashMap<String, String>> measuredControllerMaps, List<HashMap<String, String>> backcalculateControllerMaps, List<HashMap<String, String>> systemSuitabilityMaps, Map<String, String> solvermap, File outputFile){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		Document doc = builder.newDocument();
		
		//Add root tag - xml
		Element root = doc.createElement("xml");
		doc.appendChild(root);
		
		//Add units comment
		String comment = "\n\tFollowing are the units for variables used throughout the XML: \n\t Time: minute\n\t Solvent Composition: percentage\n\t Column Inner Diameter: mm\n\t Flow Rate: mL\\minute\n\t Column Length: mm\n\t";
		Comment commentNode = doc.createComment(comment);
		root.appendChild(commentNode);
		
		//Add version
		Element version = doc.createElement("version");
		version.setTextContent("1.0");
		root.appendChild(version);
		
		//Add mode tag
		Element mode = doc.createElement("mode");
		mode.setTextContent("Eight Gradients");
		root.appendChild(mode);
		
		//Add compound
		Element compoundName = doc.createElement("compound");
		compoundName.setTextContent(textCompoundName.getText());
		root.appendChild(compoundName);
		
		Element iupac = doc.createElement("IUPAC-name");
		iupac.setTextContent(textIupacName.getText());
		root.appendChild(iupac);
		
		Element formula = doc.createElement("molecular-formula");
		formula.setTextContent(textFormula.getText());
		root.appendChild(formula);
		
		Element smiles = doc.createElement("smiles");
		smiles.setTextContent(textSmiles.getText());
		root.appendChild(smiles);
		
		Element pubchem = doc.createElement("PubChem-Id");
		pubchem.setTextContent(textPubChemID.getText());
		root.appendChild(pubchem);
		
		Element inchi = doc.createElement("inchi");
		inchi.setTextContent(textInchi.getText());
		root.appendChild(inchi);
		
		Element inchiKey = doc.createElement("inchi-key");
		inchiKey.setTextContent(textInchiKey.getText());
		root.appendChild(inchiKey);
	
		Element gradients = doc.createElement("gradients");
		root.appendChild(gradients);
		
		for(int k = 0; k < 8; k++){
			char gradientChar = (char)(k+1+64);
			Element gradient = doc.createElement("gradient");
			gradient.setAttribute("name", String.valueOf(gradientChar));
			gradients.appendChild(gradient);
			
			Map<String,String> currentMeasuredControllerMap = measuredControllerMaps.get(k);
			Map<String,String> currentBackcalculateControllerMap = backcalculateControllerMaps.get(k);
			
			String[] times = currentMeasuredControllerMap.get("GradientTimes").split("\\$");
			String[] solventCompositions = currentMeasuredControllerMap.get("GradientSolventCompositions").split("\\$");
			
			Element time = doc.createElement("times");
			Element solventComp = doc.createElement("solvent-compositions");
			String timeStr = "";
			String solventCompStr = "";
			for(int i = 0; i < times.length; i++){
				timeStr += times[i] + ",";
				solventCompStr += solventCompositions[i] +",";
			}
			timeStr = timeStr.substring(0, timeStr.length()-1);
			solventCompStr = solventCompStr.substring(0, solventCompStr.length()-1);
			time.setTextContent(timeStr);
			solventComp.setTextContent(solventCompStr);
			gradient.appendChild(time);
			gradient.appendChild(solventComp);
			
			Element stationaryPhase = doc.createElement("stationary-phase");
			stationaryPhase.setTextContent(currentMeasuredControllerMap.get("StationaryPhase"));
			gradient.appendChild(stationaryPhase);
			
			Element innerDiameter = doc.createElement("column-inner-diameter");
			innerDiameter.setTextContent(currentMeasuredControllerMap.get("ColumnInnerDiameter"));
			gradient.appendChild(innerDiameter);
			
			Element columnLength = doc.createElement("column-length");
			columnLength.setTextContent(currentMeasuredControllerMap.get("ColumnLength"));
			gradient.appendChild(columnLength);
			
			Element instrumentDeadTime = doc.createElement("instrument-dead-time");
			instrumentDeadTime.setTextContent(currentMeasuredControllerMap.get("InstrumentDeadTime"));
			gradient.appendChild(instrumentDeadTime);
			
			Element flowRate = doc.createElement("flow-rate");
			flowRate.setTextContent(currentMeasuredControllerMap.get("FlowRate"));
			gradient.appendChild(flowRate);
			
			//Add Standards information
			Element backcalculateInfo = doc.createElement("backcalculate-information");
			gradient.appendChild(backcalculateInfo);
			
			Element variance = doc.createElement("variance");
			variance.setTextContent(currentBackcalculateControllerMap.get("Variance"));
			backcalculateInfo.appendChild(variance);
			
			Element iterations = doc.createElement("iterations");
			iterations.setTextContent(currentBackcalculateControllerMap.get("Iterations"));
			backcalculateInfo.appendChild(iterations);
			
			Element timeElapsed = doc.createElement("time-elapsed");
			timeElapsed.setTextContent(currentBackcalculateControllerMap.get("TimeElapsed"));
			backcalculateInfo.appendChild(timeElapsed);
			
			Element standards = doc.createElement("standard-compounds");
			backcalculateInfo.appendChild(standards);
			
			String[] compoundNames = currentBackcalculateControllerMap.get("CompoundNames").split("\\$");
			String[] predictedRetentionTimes = currentBackcalculateControllerMap.get("PredictedRetentionTimes").split("\\$");
			String[] measuredRetentionTimes = currentBackcalculateControllerMap.get("MeasuredRetentionTimes").split("\\$");
			String[] retentionErrors = currentBackcalculateControllerMap.get("Errors").split("\\$");
			String[] mzs = currentBackcalculateControllerMap.get("MzRatios").split("\\$");
			
			for(int i = 0; i < compoundNames.length; i++){
				Element standard = doc.createElement("standard-compound");
				standard.setAttribute("name", compoundNames[i]);
				standards.appendChild(standard);
			
				Element mz = doc.createElement("mz-ratio");
				mz.setTextContent(mzs[i]);
				standard.appendChild(mz);
				
				Element measuredRetTime = doc.createElement("measured-retention-time");
				measuredRetTime.setTextContent(measuredRetentionTimes[i]);
				standard.appendChild(measuredRetTime);
				
				Element predictedRetTime = doc.createElement("predicted-retention-time");
				predictedRetTime.setTextContent(predictedRetentionTimes[i]);
				standard.appendChild(predictedRetTime);
				
				Element error = doc.createElement("retention-error");
				error.setTextContent(retentionErrors[i]);
				standard.appendChild(error);
			}
			
			//Add profiles tag
			Element profiles = doc.createElement("profiles");
			backcalculateInfo.appendChild(profiles);
			
			//Create first profile - Simple Gradient
			String nodeValue = Utilities.getInterpolatedSimpleGradient(backcalculateController[k]);
			Element profile1 = doc.createElement("profile");
			profile1.setAttribute("type", "Back-calculated Gradient");
			profile1.appendChild(doc.createTextNode(nodeValue));
			profiles.appendChild(profile1);
			
			//Create first profile - Simple Gradient
			nodeValue = Utilities.getInterpolatedDeadTime(backcalculateController[k]);
			Element profile2 = doc.createElement("profile");
			profile2.setAttribute("type", "Dead Time");
			profile2.appendChild(doc.createTextNode(nodeValue));
			profiles.appendChild(profile2);
			
			//Add system suitability check information
			Element systemSuitabilityCheck = doc.createElement("system-suitability-check");
			gradient.appendChild(systemSuitabilityCheck);
			
			Map<String,String> currentSystemSuitabilityMap = systemSuitabilityMaps.get(k);
			
			Element predictedError = doc.createElement("predicted-error");
			predictedError.setTextContent(currentSystemSuitabilityMap.get("PredictedError"));
			systemSuitabilityCheck.appendChild(predictedError);
			
			Element expectedError = doc.createElement("expected-error");
			expectedError.setTextContent(currentSystemSuitabilityMap.get("ExpectedError"));
			systemSuitabilityCheck.appendChild(expectedError);
			
			Element columnRating = doc.createElement("column-rating");
			columnRating.setTextContent(currentSystemSuitabilityMap.get("ColumnRating"));
			systemSuitabilityCheck.appendChild(columnRating);
			
			standards = doc.createElement("standard-compounds");
			systemSuitabilityCheck.appendChild(standards);
			
			compoundNames = currentSystemSuitabilityMap.get("CompoundNames").split("\\$");
			mzs = currentSystemSuitabilityMap.get("MzRatios").split("\\$");
			predictedRetentionTimes = currentSystemSuitabilityMap.get("PredictedRetentionTimes").split("\\$");
			measuredRetentionTimes = currentSystemSuitabilityMap.get("MeasuredRetentionTimes").split("\\$");
			retentionErrors = currentSystemSuitabilityMap.get("Errors").split("\\$");
			
			for(int i = 0; i < compoundNames.length; i++){
				Element standard = doc.createElement("standard-compound");
				standard.setAttribute("name", compoundNames[i]);
				standards.appendChild(standard);
			
				Element mz = doc.createElement("mz-ratio");
				mz.setTextContent(mzs[i]);
				standard.appendChild(mz);
				
				Element measuredRetTime = doc.createElement("measured-retention-time");
				measuredRetTime.setTextContent(measuredRetentionTimes[i]);
				standard.appendChild(measuredRetTime);
				
				Element predictedRetTime = doc.createElement("predicted-retention-time");
				predictedRetTime.setTextContent(predictedRetentionTimes[i]);
				standard.appendChild(predictedRetTime);
				
				Element error = doc.createElement("retention-error");
				error.setTextContent(retentionErrors[i]);
				standard.appendChild(error);
			}
			
		}
		
		//Add solver information
		if(solveParametersTask != null){
			Element solvedRelationship = doc.createElement("logk-information");
			root.appendChild(solvedRelationship);
			
			Element padeCoefficients = doc.createElement("pade-coefficients");
			solvedRelationship.appendChild(padeCoefficients);
			
			Element a0 = doc.createElement("a0");
			a0.setTextContent(solvermap.get("a0"));
			padeCoefficients.appendChild(a0);
			
			Element a1 = doc.createElement("a1");
			a1.setTextContent(solvermap.get("a1"));
			padeCoefficients.appendChild(a1);
			
			Element a2 = doc.createElement("a2");
			a2.setTextContent(solvermap.get("a2"));
			padeCoefficients.appendChild(a2);
			
			Element b1 = doc.createElement("b1");
			b1.setTextContent(solvermap.get("b1"));
			padeCoefficients.appendChild(b1);
			
			Element b2 = doc.createElement("b2");
			b2.setTextContent(solvermap.get("b2"));
			padeCoefficients.appendChild(b2);
			
			Element variance = doc.createElement("variance");
			variance.setTextContent(solvermap.get("Variance"));
			solvedRelationship.appendChild(variance);
			
			Element timeElapsed = doc.createElement("time-elapsed");
			timeElapsed.setTextContent(solvermap.get("TimeElapsed"));
			solvedRelationship.appendChild(timeElapsed);
			
			Element standards = doc.createElement("standard-compounds");
			solvedRelationship.appendChild(standards);
			
			Element standard = doc.createElement("standard-compound");
			standards.appendChild(standard);
			
			String[] measuredRetTimes = solvermap.get("ExperimentalRetentionTimes").split("\\$");
			String[] predictedRetentionTimes = solvermap.get("CalculatedRetentionTimes").split("\\$");
			String[] retentionErrors = solvermap.get("Errors").split("\\$");
			
			for(int i = 0; i < measuredRetTimes.length; i++){
				Element gradient = doc.createElement("gradient");
				char gradientChar = (char)(i+1+64);
				gradient.setAttribute("number", String.valueOf(gradientChar));
				standard.appendChild(gradient);
				
				Element measuredTime = doc.createElement("measured-retention-time");
				measuredTime.setTextContent(measuredRetTimes[i]);
				gradient.appendChild(measuredTime);
				
				Element predictedTime = doc.createElement("predicted-retention-time");
				predictedTime.setTextContent(predictedRetentionTimes[i]);
				gradient.appendChild(predictedTime);
				
				Element error = doc.createElement("retention-error");
				error.setTextContent(retentionErrors[i]);
				gradient.appendChild(error);
			}
		
		}
		
		//Write the contents into a XML file
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outputFile);
		try {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
