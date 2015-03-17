package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.util.Callback;

import org.retentionprediction.lcdatabasebuilderfx.business.Globals;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;

public class MeasuredRetentionTimesController implements Initializable, ChangeListener<Boolean> {

	@FXML private Label finalSolventComposition;
    @FXML private Label holdTime;
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
	private double columnLength = 30.0; // in m
	private double innerDiameter = 0.25; // in mm
	private double instrumentDeadTime = 0.25; // in um
	private double flowRate = 1; // in mL/min
	
   	public interface MeasuredRetentionTimesControllerListener{
   		public void onNextStepPressed(MeasuredRetentionTimesController thisController);
    }
   	
	@Override
	public void changed(ObservableValue<? extends Boolean> observable,
			Boolean oldValue, Boolean newValue) {
		// TODO Auto-generated method stub
		
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
		for (int i = 0; i < Globals.AlkaneNameArray.length; i++){
			StandardCompound newItem = new StandardCompound();
			newItem.setUse(false);
			newItem.setName(Globals.AlkaneNameArray[i]);
			newItem.setMz(Globals.convertMZToString(Globals.AlkaneMZArray[i]));
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
		validateColumnLength();
		validateColumnInnerDiameter();
		validateFlowRate();
		validateInstrumentDeadTime();
		
		// Now check to see if we have enough retention times to make the Next Step button enabled
		int iTotalUsed = 0;
		for (int i = 0; i < this.standardsList.size(); i++)
		{
			if (standardsList.get(i).getUse())
				iTotalUsed++;
		}
		
		if (iTotalUsed >= 6)
			buttonNextStep.setDisable(false);
		else
			buttonNextStep.setDisable(true);			
	}
	
	private void validateInstrumentDeadTime() {
		// TODO Auto-generated method stub
		
	}

	private void validateColumnLength()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldColumnLength.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
		if (dTemp < 0.1)
			dTemp = 0.1;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.columnLength = dTemp;
		textFieldColumnLength.setText(Float.toString((float)this.columnLength));    
	}
	
	private void validateColumnInnerDiameter()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldInnerDiameter.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
		if (dTemp < 0.00001)
			dTemp = 0.00001;
		if (dTemp > 10000)
			dTemp = 10000;
		
//		if (dTemp - (2 * this.filmThickness / 1000) > 0)
			this.innerDiameter = dTemp;
//		else
//			this.innerDiameter = ((this.filmThickness * 2) / 1000) - 0.0001;
		
		textFieldInnerDiameter.setText(Float.toString((float)this.innerDiameter));    
	}
	
	private void validateFlowRate()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldFlowRate.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
    	if (dTemp < 0.000000001)
			dTemp = 0.000000001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.flowRate = dTemp;
		textFieldFlowRate.setText(Float.toString((float)flowRate));  
	}
	
	@FXML private void commitRetentionTime(TableColumn.CellEditEvent<StandardCompound,String> t)
	{
		Double dNewRetentionTime = 0.0;
		try
		{
			// TODO: More sophisticated?
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
		
	}
	
	public MeasuredRetentionTimesControllerListener getMeasuredRetentionTimesControllerListener() {
		return measuredRetentionTimesControllerListener;
	}

	public void setMeasuredRetentionTimesControllerListener(
			MeasuredRetentionTimesControllerListener measuredRetentionTimesControllerListener) {
		this.measuredRetentionTimesControllerListener = measuredRetentionTimesControllerListener;
	}

}
