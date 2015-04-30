package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;

import org.retentionprediction.lcdatabasebuilderfx.business.GradientProgramStep;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;

import boswell.peakfinderlcfx.Globals;
import boswell.peakfinderlcfx.GlobalsDan;
import boswell.peakfinderlcfx.PeakFinderLCFX;

public class MeasuredRetentionTimesController implements Initializable, ChangeListener<Boolean> {

	@FXML private Label finalSolventComposition;
    @FXML private Label holdTime;	//Useless
    @FXML private Label gradientTime;
    @FXML private Label labelColumnLength;
    @FXML private Label initialSolventComposition;
    @FXML private Label labelInstrumentDeadTime;
    @FXML private Label labelFlowRateUnits;
    @FXML private Label labelInnerDiameter;
	
	@FXML private ComboBox<String> comboStationaryPhase;
	
    @FXML private TableColumn<StandardCompound, Boolean> columnUse;
    @FXML private TableColumn<StandardCompound, String> columnRetentionTime;
    @FXML private TableColumn<StandardCompound, String> columnMZ;
    @FXML private TableColumn<StandardCompound, String> columnCompound;
    @FXML private TableView<GradientProgramStep> tableViewGradientProgram;
	@FXML private TableColumn<GradientProgramStep, Double> columnTime;
	@FXML private TableColumn<GradientProgramStep, Double> columnSolventComposition;
    
    @FXML private TextField textFieldColumnLength;
    @FXML private TextField textFieldFlowRate;
    @FXML private TextField textFieldInnerDiameter;
    @FXML private TextField textFieldInstrumentDeadTime;
    
    @FXML private TableView<StandardCompound> tableMeasuredRetentionTimes;

    @FXML private TitledPane requirementsPane;

    @FXML private ScrollPane measuredretentiontimespage;

    @FXML private Button buttonNextStep;

    private MeasuredRetentionTimesControllerListener measuredRetentionTimesControllerListener;
    private ObservableList<StandardCompound> standardsList;

	private double[][] gradientProgram;
    private double[][] gradientProgramInConventionalForm = {{0.0,5.0} , {5.0,95.0}};
    
	private double columnLength = 100; // in mm
	private double innerDiameter = 2.1; // in mm
	private double instrumentDeadTime = 0.125; // in  min
	private double flowRate = 0.4; // in mL/min
	private double initialTime = 0.0;
	private double initialSolventComp = 5.0;
	
	private Window parentWindow;
	private String fileName;
	private String strProgramName = "";
	private ObservableList<GradientProgramStep> gradientProgramList;
	
	
   	public interface MeasuredRetentionTimesControllerListener{
   		public void onNextStepPressed(MeasuredRetentionTimesController thisController);
    }
   	
	@Override
	public void changed(ObservableValue<? extends Boolean> observable,
			Boolean oldValue, Boolean newValue) {
		if (newValue == false)
		{
			// Lost focus, so commit the text
			performValidations();
		}	
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		comboStationaryPhase.getItems().clear();
		comboStationaryPhase.getItems().addAll("Agilent HP-5MS UI");
		
		textFieldColumnLength.focusedProperty().addListener(this);
		textFieldInnerDiameter.focusedProperty().addListener(this);
		textFieldFlowRate.focusedProperty().addListener(this);
		textFieldInstrumentDeadTime.focusedProperty().addListener(this);
		
		List<StandardCompound> data = new ArrayList<StandardCompound>();
		for (int i = 0; i < GlobalsDan.StandardCompoundsNameArray.length; i++){
			StandardCompound newItem = new StandardCompound();
			newItem.setUse(false);
			newItem.setName(GlobalsDan.StandardCompoundsNameArray[i]);
			newItem.setMz(GlobalsDan.convertMZToString(GlobalsDan.StandardCompoundsMZArray[i]));
			newItem.setMeasuredRetentionTime(0.0);
			newItem.setIndex(i);
			data.add(newItem);
		}
		standardsList = FXCollections.observableArrayList(data);
		columnUse.setCellValueFactory(new PropertyValueFactory<StandardCompound,Boolean>("use"));
		columnUse.setCellFactory(new Callback<TableColumn<StandardCompound, Boolean>, TableCell<StandardCompound, Boolean>>() {
            public TableCell<StandardCompound, Boolean> call(TableColumn<StandardCompound, Boolean> p) 
            {
            	CheckBoxTableCell<StandardCompound, Boolean> cb = new CheckBoxTableCell<StandardCompound, Boolean>();
            	cb.setAlignment(Pos.CENTER);
                return cb;
            }
        });
		columnCompound.setCellValueFactory(new PropertyValueFactory<StandardCompound,String>("name"));
		columnMZ.setCellValueFactory(new PropertyValueFactory<StandardCompound,String>("mz"));
		
		columnRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound,String>("measuredRetentionTimeString"));
		columnRetentionTime.setCellFactory(TextFieldTableCell.<StandardCompound>forTableColumn());
		setTableMeasuredRetentionTimesItems(standardsList);
	
		tableViewGradientProgram.setEditable(false);
		
		//Load the table with correct values
		List<GradientProgramStep> data1 = new ArrayList<GradientProgramStep>();
		gradientProgramList = FXCollections.observableArrayList(data1);
		setGradientProgramInConventionalForm();
		columnTime.setCellValueFactory(new PropertyValueFactory<GradientProgramStep, Double>("time"));
		columnTime.setCellFactory(TextFieldTableCell.<GradientProgramStep, Double>forTableColumn(new DoubleStringConverter()));
		columnSolventComposition.setCellValueFactory(new PropertyValueFactory<GradientProgramStep, Double>("solventComposition"));
		columnSolventComposition.setCellFactory(TextFieldTableCell.<GradientProgramStep, Double>forTableColumn(new DoubleStringConverter()));
		this.tableViewGradientProgram.setItems(gradientProgramList);
	}
	
	private void setTableMeasuredRetentionTimesItems(ObservableList<StandardCompound> standardCompoundList){
		standardsList = standardCompoundList;
		
		tableMeasuredRetentionTimes.setItems(standardsList);
		
		// Add a listener for the "Use" property
		for (int i = 0; i < standardsList.size(); i++){
			StandardCompound thisStandard = standardsList.get(i);
			
			thisStandard.useProperty().addListener(new ChangeListener<Boolean>(){
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
							performValidations();
						}
					});
		}
		performValidations();
	}
	
	private void performValidations()
	{
		validatePhysicalVariables(textFieldColumnLength, columnLength, 0.1, 10000);
		validatePhysicalVariables(textFieldInnerDiameter, innerDiameter, 0.00001, 10000);
		validatePhysicalVariables(textFieldFlowRate, flowRate, 0.000000001, 10000);
		//validatePhysicalVariables(textFieldInstrumentDeadTime, instrumentDeadTime, dTempLow, dTempHigh);
		
		// Now check to see if we have enough retention times to make the Next Step button enabled
		int iTotalUsed = 0;
		for (int i = 0; i < this.standardsList.size(); i++)
		{
			if (standardsList.get(i).getUse())
				iTotalUsed++;
		}
		
		if (iTotalUsed >= 8)
			buttonNextStep.setDisable(false);
		else
			buttonNextStep.setDisable(true);			
	}
	
	private void validatePhysicalVariables(TextField textField, double variable, double dTempLow, double dTempHigh){
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textField.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
		if (dTemp < dTempLow)
			dTemp = dTempLow;
		if (dTemp > dTempHigh)
			dTemp = dTempHigh;
		
		variable = dTemp;
		textField.setText(Float.toString((float)variable));   
	}
	
	
	@FXML private void commitRetentionTime(TableColumn.CellEditEvent<StandardCompound,String> t)
	{
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

		// Check to see if the new retention time is <> 0. If so, check the corresponding box.
		if (dNewRetentionTime > 0)
			currentItem.setUse(true);
		else
			currentItem.setUse(false);
			
		performValidations();
	}
	
	@FXML private void actionPerformValidation(ActionEvent e){
		performValidations();
	}
	
	@FXML private void onNextStepAction(ActionEvent e){
		// Send notification back to the ParentPaneController to change the visible pane
		if (measuredRetentionTimesControllerListener != null)
			this.measuredRetentionTimesControllerListener.onNextStepPressed(this);
	}
	
	@FXML private void onFindRetentionTimesAutomatically(ActionEvent e){
//		PeakFinderLCFX peakfinder = new PeakFinderLCFX(parentWindow, GlobalsDan.StationaryPhaseArray, true);
//		peakfinder.setStandardCompoundMZData(GlobalsDan.StandardCompoundsMZArray);
//		peakfinder.setStandardCompoundNames(GlobalsDan.StandardCompoundsNameArray);
//		peakfinder.setIsocraticDataArray(GlobalsDan.StandardIsocraticDataArray);
//		peakfinder.setInterpolatedDeadTime();
//		//TODO: might need to add more parameters here
//		peakfinder.run();
//		
//		if (peakfinder.getOkPressed())
//    	{
    		//double[] dRetentionTimes = peakfinder.getSelectedRetentionTimes();
		double[] dRetentionTimes = GlobalsDan.dPredefinedValues[0][0];
    		//boolean[] bSkippedStandards = peakfinder.getSkippedStandards();
    		//int[] iPeakRank = peakfinder.getSelectedPeakRank();
    	boolean[] bSkippedStandards = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};	
		
    		for (int i = 0; i < dRetentionTimes.length; i++)
    		{
    			// Mark whether the standard is skipped.
    			standardsList.get(i).setUse(!bSkippedStandards[i]);
    			
//    			// Put in the correct retention time
//    			if (iPeakRank[i] >= 0)
//    			{
    				if (!bSkippedStandards[i])
    					standardsList.get(i).setMeasuredRetentionTime(dRetentionTimes[i]);
    				else
    					standardsList.get(i).setMeasuredRetentionTime(0.0);
    			
//    			else
//    			{
    				// If the peak wasn't picked, then skip this one.
					//standardsList.get(i).setMeasuredRetentionTime(0.0);    				
	    			//standardsList.get(i).setUse(false);
    			//}
    		}
    		
    		performValidations();
    		
    //		fileName = peakfinder.getFileName();
    	}
//	}

	public void writeSaveData(SaveData.MeasuredRetentionTimeSaveData saveData)
	{
		saveData.programName = strProgramName;
		saveData.fileName = fileName;
		saveData.standardsList = standardsList;
		saveData.columnLength = columnLength;
		saveData.innerDiameter = innerDiameter;
		saveData.flowRate = flowRate;
	}
	
	public void loadSaveData(SaveData.MeasuredRetentionTimeSaveData saveData)
	{
		strProgramName = saveData.programName;
		fileName = saveData.fileName;
		standardsList = saveData.standardsList;
		columnLength = saveData.columnLength;
		innerDiameter = saveData.innerDiameter;
		flowRate = saveData.flowRate;

		setTableMeasuredRetentionTimesItems(standardsList);

		this.textFieldColumnLength.setText(Float.toString((float)columnLength));
		this.textFieldInnerDiameter.setText(Float.toString((float)innerDiameter));
		this.textFieldFlowRate.setText(Float.toString((float)flowRate));
		this.setProgramName(strProgramName);
	}
	
	public void setParentWindow(Window parentWindow)
	{
		this.parentWindow = parentWindow;
	}
	
	public void setGradientProgramInConventionalForm() {
		
		gradientProgramList.clear();
		for(int i = 0; i < gradientProgramInConventionalForm.length; i++){
			GradientProgramStep step = new GradientProgramStep(gradientProgramInConventionalForm[i][0], gradientProgramInConventionalForm[i][1]);
			gradientProgramList.add(step);
		}
	}
	
	public double[][] getGradientProgram() {
		double[][] gradientProgramInConventionalForm = new double[gradientProgramList.size()][2];
		for (int i = 0; i < gradientProgramList.size(); i++)
		{
			gradientProgramInConventionalForm[i][0] = gradientProgramList.get(i).getTime();
			gradientProgramInConventionalForm[i][1] = gradientProgramList.get(i).getSolventComposition();
		}
		
		return Globals.convertGradientProgramInConventionalFormToRegularForm(gradientProgramInConventionalForm, initialTime, initialSolventComp);
	}
	
	public void setGradientProgram(double[][] gradientProgram) {
		this.gradientProgram = gradientProgram;
	}
	
	public MeasuredRetentionTimesControllerListener getMeasuredRetentionTimesControllerListener() {
		return measuredRetentionTimesControllerListener;
	}

	public void setMeasuredRetentionTimesControllerListener(
			MeasuredRetentionTimesControllerListener measuredRetentionTimesControllerListener) {
		this.measuredRetentionTimesControllerListener = measuredRetentionTimesControllerListener;
	}
	
	public ObservableList<StandardCompound> getStandardsList() {
		return standardsList;
	}

	public void setStandardsList(ObservableList<StandardCompound> standardsList) {
		this.standardsList = standardsList;
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
	
	public void setProgramName(String strName)
	{
		strProgramName = strName;
		requirementsPane.setText("Requirements for " + strProgramName);
	}

}
