package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.retentionprediction.lcdatabasebuilderfx.business.Globals;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;
import org.retentionprediction.lcdatabasebuilderfx.ui.InjectionSaveData.BackCalculateSaveData;
import org.retentionprediction.lcdatabasebuilderfx.ui.InjectionSaveData.MeasuredRetentionTimeSaveData;
import org.retentionprediction.lcdatabasebuilderfx.ui.SolveParametersTask.SolveParametersListener;

import boswell.graphcontrolfx.GraphControlFX;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;

public class FinalFitController implements Initializable, SolveParametersListener{

		public interface FinalFitControllerListener{
			public void onPreviousStepPressed(BackcalculateController thisController);

			public void onPreviousStepPressed(
					FinalFitController finalFitController);
		}
	
	 	@FXML private AnchorPane anchorPaneRetentionSolver;
	    
	    @FXML private Label labelATwo;
	    @FXML private Label labelAOne;
	    @FXML private Label labelBOne;
	    @FXML private Label labelBTwo;
	    @FXML private Label labelAZero;
	    @FXML private Label labelStatus;
	    @FXML private Label labelTimeElapsed;
	    @FXML private Label labelVariance;
	    @FXML private Label labelIteration;

	    @FXML private TableView<StandardCompound> tableRetentionTimes;

	    @FXML private TableView<StandardCompound> tableCompoundList;
	    
	    @FXML private ProgressBar progressBar;

	    @FXML private Button buttonSolve;
	    @FXML private Button buttonAdd;
	    @FXML private Button buttonDelete;
	    @FXML private Button buttonInsert;
	    @FXML private Button buttonPrevious;
	    
	    @FXML private TableColumn<StandardCompound, String> columnError;
	    @FXML private TableColumn<StandardCompound, String> columnProgram;
	    @FXML private TableColumn<StandardCompound, String> columnPredictedRetentionTime;
	    @FXML private TableColumn<StandardCompound, String> columnMeasuredRetentionTime;
	    
	    @FXML private TableColumn<StandardCompound, String> columnNames;
	    @FXML private TableColumn<StandardCompound, Boolean> columnUse;
	    @FXML private TableColumn<StandardCompound, Double> columnRetentionTime;
	    @FXML private TableColumn<StandardCompound, Double> columnInjectionTime;
	    
	    @FXML private TitledPane paneSolveForParameters;

	    private GraphControlFX retentionSolverTimeGraph;
	    private final double rem = javafx.scene.text.Font.getDefault().getSize();
	    
	    private SolveParametersTask solveParametersTask;
	    private ObservableList<StandardCompound> compoundsList;
	    private ObservableList<StandardCompound> compoundsRetentionList;
	    
	    private FinalFitControllerListener finalFitControllerListener;
	
	    private BackcalculateController backcalculateController;
	    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try{
			retentionSolverTimeGraph = new GraphControlFX();
			retentionSolverTimeGraph.setControlsEnabled(false);
			retentionSolverTimeGraph.setYAxisTitle("log K");
			retentionSolverTimeGraph.setYAxisBaseUnit("", "");
			retentionSolverTimeGraph.setYAxisScientificNotation(true);
			retentionSolverTimeGraph.setYAxisRangeIndicatorsVisible(true);
			retentionSolverTimeGraph.setAutoScaleY(true);
			
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
			
			
			columnUse.setCellValueFactory(new PropertyValueFactory<StandardCompound,Boolean>("use"));
			columnUse.setCellFactory(new Callback<TableColumn<StandardCompound, Boolean>, TableCell<StandardCompound, Boolean>>() {
	            public TableCell<StandardCompound, Boolean> call(TableColumn<StandardCompound, Boolean> p) 
	            {
	            	CheckBoxTableCell<StandardCompound, Boolean> cb = new CheckBoxTableCell<StandardCompound, Boolean>();
	            	cb.setAlignment(Pos.CENTER);
	                return cb;
	            }
	        });
			
			columnInjectionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound,Double>("injectionTime"));
			columnInjectionTime.setCellFactory(TextFieldTableCell.<StandardCompound, Double>forTableColumn(new DoubleStringConverter()));
			columnRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound,Double>("measuredRetentionTime"));
			columnRetentionTime.setCellFactory(TextFieldTableCell.<StandardCompound, Double>forTableColumn(new DoubleStringConverter()));
			
			List<StandardCompound> data = new ArrayList<StandardCompound>();
			compoundsList = FXCollections.observableArrayList(data);
			
			//Add injection data and retention data here.
			for(int i = 0; i < Globals.injectionModeData.length; i++){
				StandardCompound compound = new StandardCompound();
				compound.setIndex(i+1);
				compound.setName("Injection "+compound.getIndex());
				compound.setUse(true);
				double injectiondata = Globals.injectionModeData[i][0];
				compound.setInjectionTime(injectiondata);
				compound.setMeasuredRetentionTime(Globals.injectionModeData[i][1]);
				compoundsList.add(compound);
			}
			
			this.tableCompoundList.setItems(compoundsList);
			columnNames.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("name"));
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
		    
		    this.compoundsRetentionList = FXCollections.observableArrayList(this.compoundsList);
			
			this.tableRetentionTimes.setItems(compoundsRetentionList);
		    
		    this.tableCompoundList.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>(){
	
				@Override
				public void onChanged(
						javafx.collections.ListChangeListener.Change<? extends Integer> c) {
					if (c.getList().size() >= 1)
		            {
		            	buttonInsert.setDisable(false);
		                buttonDelete.setDisable(false);
		            }
		            else
		            {
		            	buttonInsert.setDisable(true);
		                buttonDelete.setDisable(true);
		            }
					
					copyUsedStandardCompounds(compoundsList, compoundsRetentionList);
				}
		    	
		    });
		
		}
		catch(Exception e){
			
		}
		
	}
	
	public void setFinalFitControllerListener(FinalFitControllerListener finalFitControllerListener){
		this.finalFitControllerListener = finalFitControllerListener;
	}

	@FXML public void onCommitRetentionTime(TableColumn.CellEditEvent<StandardCompound,String> t) {
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
	
	@FXML public void onPreviousStepPressed(ActionEvent event){
		if(finalFitControllerListener != null){
			this.finalFitControllerListener.onPreviousStepPressed(this);
		}
	}
	
	@FXML public void onSolveForRetentionParameters(ActionEvent event) {
		solveParametersTask = new SolveParametersTask();
		BackcalculateController[] controller = new BackcalculateController[1];
		controller[0] = this.backcalculateController;
    	solveParametersTask.setBackCalculateController(controller);
    	solveParametersTask.setProgramList(compoundsRetentionList);
    	solveParametersTask.setGraphControl(retentionSolverTimeGraph);
    	solveParametersTask.setSolveParametersListener(this);
    	solveParametersTask.setInjectionMode(true);
    	
    	labelIteration.textProperty().bind(solveParametersTask.getIterationProperty());
		labelTimeElapsed.textProperty().bind(solveParametersTask.getTimeElapsedProperty());
		labelVariance.textProperty().bind(solveParametersTask.getVarianceProperty());
		labelAZero.textProperty().bind(solveParametersTask.getAZeroProperty());
		labelAOne.textProperty().bind(solveParametersTask.getAOneProperty());
		labelATwo.textProperty().bind(solveParametersTask.getATwoProperty());
		labelBOne.textProperty().bind(solveParametersTask.getBOneProperty());
		labelBTwo.textProperty().bind(solveParametersTask.getBTwoProperty());
		labelStatus.textProperty().bind(solveParametersTask.messageProperty());
    	
    	Thread thread = new Thread(solveParametersTask);
    	thread.start();
    	buttonSolve.setDisable(true);
	
	}
	
	@Override
	public void onUpdateTable(ObservableList<StandardCompound> list) {
		for (int i = 0; i < list.size(); i++)
		{
			this.compoundsRetentionList.get(i).makeEqualTo(list.get(i));
		}
	}

	@FXML public void onAddAction(ActionEvent event) {
		int lastRowIndex = compoundsList.size() - 1;
    	double rowValue1, rowValue2;
    	String name = "",mz = "";
    	if (lastRowIndex == -1)    	{
    		lastRowIndex = 0;
    		rowValue1 = 0.0; 
    		rowValue2 = 0.0;
    	}
    	else{
    		rowValue1 = compoundsList.get(lastRowIndex).getInjectionTime();
    		rowValue2 = compoundsList.get(lastRowIndex).getMeasuredRetentionTime();
    		name = compoundsList.get(lastRowIndex).getName();
    		mz = compoundsList.get(lastRowIndex).getMz();
    	}
    	
		StandardCompound newStep = new StandardCompound(true,name,mz, rowValue2,0.0,lastRowIndex+2);
		newStep.setInjectionTime(rowValue1);
		compoundsList.add(newStep);
		compoundsRetentionList.add(newStep);
	}
	
	@FXML public void onDeleteAction(ActionEvent event) {
		int activatedRowIndex = this.tableCompoundList.getSelectionModel().getSelectedIndex();
		if (activatedRowIndex < 0)
			return;

		compoundsList.remove(activatedRowIndex);
    	
		for(int i = activatedRowIndex; i < this.compoundsList.size(); i++){
    		StandardCompound c = compoundsList.get(i);
    		c.setIndex(c.getIndex()-1);
    		c.setName("Injection "+c.getIndex());
    	}
		copyUsedStandardCompounds(compoundsList, compoundsRetentionList);
	    this.buttonDelete.setDisable(true);
	    this.buttonInsert.setDisable(true);
		
	}
	
	@FXML public void onInsertAction(ActionEvent event) {
		int activatedRowIndex = this.tableCompoundList.getSelectionModel().getSelectedIndex();
		if (activatedRowIndex < 0)
			return;

    	StandardCompound compound = this.compoundsList.get(activatedRowIndex);
    	StandardCompound newStep = new StandardCompound(compound.getUse(),compound.getName(),compound.getMz(),compound.getMeasuredRetentionTime(),compound.getPredictedRetentionTime(),compound.getIndex());
    	newStep.setInjectionTime(compound.getInjectionTime());
    	compoundsList.add(activatedRowIndex, newStep);
    	
    	for(int i = activatedRowIndex+1; i < this.compoundsList.size(); i++){
    		StandardCompound c = compoundsList.get(i);
    		c.setIndex(c.getIndex()+1);
    		c.setName("Injection "+c.getIndex());
    	}
    	
    	copyUsedStandardCompounds(compoundsList, compoundsRetentionList);
	}
	
	private void copyUsedStandardCompounds(List<StandardCompound> src, List<StandardCompound> dest){
		dest.clear();
		for(StandardCompound c: src){
			if(c.getUse()){
				dest.add(c);
			}
		}
	}
	
	public BackcalculateController getBackcalculateController() {
		return backcalculateController;
	}

	public void setBackcalculateController(
			BackcalculateController backcalculateController) {
		this.backcalculateController = backcalculateController;
	}

	public void loadSaveData(InjectionSaveData saveData) {
		

		compoundsList = saveData.programList;
		copyUsedStandardCompounds(compoundsList, compoundsRetentionList);
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

		
	    tableRetentionTimes.setItems(compoundsRetentionList);
	    tableCompoundList.setItems(compoundsList);
		
	    
		retentionSolverTimeGraph = new GraphControlFX();
		retentionSolverTimeGraph.setControlsEnabled(false);
		retentionSolverTimeGraph.setYAxisTitle("log K");
		retentionSolverTimeGraph.setYAxisBaseUnit("", "");
		retentionSolverTimeGraph.setYAxisScientificNotation(true);
		retentionSolverTimeGraph.setYAxisRangeIndicatorsVisible(true);
		retentionSolverTimeGraph.setAutoScaleY(true);
		
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
	}

	public void writeSaveData(InjectionSaveData saveData) {
		saveData.programList = compoundsRetentionList;
		saveData.labelAZeroText = labelAZero.getText();
		saveData.labelAOneText = labelAOne.getText();
		saveData.labelATwoText = labelATwo.getText();
		saveData.labelBOneText = labelBOne.getText();
		saveData.labelBTwoText = labelBTwo.getText();
		saveData.labelIterationText = labelIteration.getText();
		saveData.labelVarianceText = labelVariance.getText();
		saveData.labelTimeElapsedText = labelTimeElapsed.getText();
		saveData.statusText = labelStatus.getText();
		
	}
	
}
