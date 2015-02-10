package org.retentionprediction.gcdatabasebuilderfx;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.retentionprediction.peakfindergcfx.PeakFinderGCFX;

import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.RadioButton;
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
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

public class MeasuredRetentionTimesController implements Initializable, ChangeListener<Boolean>
{
	@FXML private Label requiredInitialTemperature;
	@FXML private Label requiredInitialHoldTime;
	@FXML private Label requiredRampRate;
	@FXML private Label requiredFinalTemperature;
	@FXML private Label requiredFinalHoldTime;
	@FXML private TitledPane requirementsPane;
	@FXML private ComboBox<String> comboStationaryPhase;
	@FXML private ComboBox<String> comboInletPressureUnits;
	@FXML private ComboBox<String> comboOutletPressureUnits;
	@FXML private TableView<StandardCompound> tableMeasuredRetentionTimes;
	@FXML private TableColumn<StandardCompound, Boolean> columnUse;
	@FXML private TableColumn<StandardCompound, String> columnCompound;
	@FXML private TableColumn<StandardCompound, String> columnMZ;
	@FXML private TableColumn<StandardCompound, String> columnRetentionTime;
	@FXML private TextField textFieldColumnLength;
	@FXML private TextField textFieldInnerDiameter;
	@FXML private TextField textFieldFilmThickness;
	@FXML private TextField textFieldFlowRate;
	@FXML private TextField textFieldInletPressure;
	@FXML private TextField textFieldOutletPressure;
	@FXML private RadioButton radioConstantFlowRate;
	@FXML private RadioButton radioConstantInletPressure;
	@FXML private Label labelFlowRate;
	@FXML private Label labelFlowRateUnits;
	@FXML private Label labelInletPressure;
	@FXML private RadioButton radioVacuum;
	@FXML private RadioButton radioOther;
	@FXML private Button buttonNextStep;
	
	private Window parentWindow;
	
	private ObservableList<StandardCompound> standardsList;
	private String strProgramName = "";
	private double columnLength = 30.0; // in m
	private double innerDiameter = 0.25; // in mm
	private double filmThickness = 0.25; // in um
	private double flowRate = 1; // in mL/min
	private double inletPressure = 151325; // in Pa;
	private double outletPressure = 0.000001; // in Pa;
	private double initialTemperature = 60;
	private double initialHoldTime = 5;
	private double rampRate = 26;
	private double finalTemperature = 320;
	private double finalHoldTime = 15;
	private String fileName = "";
	
    private MeasuredRetentionTimesControllerListener measuredRetentionTimesControllerListener;

	public interface MeasuredRetentionTimesControllerListener 
	{
		public void onNextStepPressed(MeasuredRetentionTimesController thisController);
    }
	
	public double[][] getTemperatureProgramInConventionalForm()
	{
		return new double[][] {{rampRate, finalTemperature, finalHoldTime}};
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		comboStationaryPhase.getItems().clear();
		comboStationaryPhase.getItems().addAll("Agilent HP-5MS UI");
		comboInletPressureUnits.getItems().clear();
		comboInletPressureUnits.getItems().addAll("kPag", "kPa", "psig", "psi");
		comboInletPressureUnits.setValue("kPag");
		comboOutletPressureUnits.getItems().clear();
		comboOutletPressureUnits.getItems().addAll("kPag", "kPa", "psig", "psi");
		comboOutletPressureUnits.setValue("kPa");
		
		// Switch to constant inlet pressure mode to start
		switchToConstantInletPressureMode();
		
		// Switch to vacuum outlet pressure to start
		switchToVacuumOutletPressure();
		
		// Create listeners for the text boxes
		textFieldColumnLength.focusedProperty().addListener(this);
		textFieldFilmThickness.focusedProperty().addListener(this);
		textFieldInnerDiameter.focusedProperty().addListener(this);
		textFieldFlowRate.focusedProperty().addListener(this);
		textFieldInletPressure.focusedProperty().addListener(this);
		textFieldOutletPressure.focusedProperty().addListener(this);
		
		// Load the measured retention times table with the correct values
		List<StandardCompound> data = new ArrayList<StandardCompound>();

		for (int i = 0; i < Globals.AlkaneNameArray.length; i++)
		{
			StandardCompound newItem = new StandardCompound();
			newItem.setUse(false);
			newItem.setName(Globals.AlkaneNameArray[i]);
			newItem.setMz(Globals.convertMZToString(Globals.AlkaneMZArray[i]));
			newItem.setMeasuredRetentionTime(0.0);
			newItem.setIndex(i);
			
			data.add(newItem);
		}
		
		standardsList = FXCollections.observableArrayList(data);

		// Set up the table view
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
		//columnRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound,Double>("measuredRetentionTime"));
		columnRetentionTime.setCellFactory(TextFieldTableCell.<StandardCompound>forTableColumn());/*{
			@Override
			public Double fromString(String arg0) {
				double dTemp = 0;
		    	try
		    	{
		    		dTemp = (double)Float.valueOf(arg0);
		    	}
		    	catch (NumberFormatException e)
		    	{
		    		dTemp = 0.0;
		    	}
				return dTemp;
			}

			@Override
			public String toString(Double arg0) {
            	float value = arg0.floatValue();
				NumberFormat formatter = new DecimalFormat("#0.0000");
				String retVal = formatter.format(value);
				return retVal;
			}
		
		}));*/
		
		setTableMeasuredRetentionTimesItems(standardsList);
		
		performValidations();
	}
	
	private void setTableMeasuredRetentionTimesItems(ObservableList<StandardCompound> standardCompoundList)
	{
		standardsList = standardCompoundList;
		
		tableMeasuredRetentionTimes.setItems(standardsList);
		
		// Add a listener for the "Use" property
		for (int i = 0; i < standardsList.size(); i++)
		{
			StandardCompound thisStandard = standardsList.get(i);
			
			thisStandard.useProperty().addListener(new ChangeListener<Boolean>()
					{
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
						{
							performValidations();
						}
					});
		}
		
		performValidations();
	}
	
	public void setMeasuredRetentionTimesControllerListener(MeasuredRetentionTimesControllerListener thisListener)
	{
		measuredRetentionTimesControllerListener = thisListener;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public double getInitialTemperature()
	{
		return this.initialTemperature;
	}
	
	public double getInitialHoldTime()
	{
		return this.initialHoldTime;
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
	
	@FXML private void actionPerformValidation(ActionEvent e)
	{
		performValidations();
	}
	
	@FXML private void onInletPressureUnitsAction(ActionEvent e)
	{
		updateInletPressure();
	}
	
	private void updateInletPressure()
	{
		if (comboInletPressureUnits.getValue() == "kPag")
    		textFieldInletPressure.setText(Float.toString((float)((this.inletPressure - 101325) / 1000.0)));
    	else if (comboInletPressureUnits.getValue() == "kPa")
    		textFieldInletPressure.setText(Float.toString((float)((this.inletPressure) / 1000.0)));
    	else if (comboInletPressureUnits.getValue() == "psig")
    		textFieldInletPressure.setText(Float.toString((float)((this.inletPressure - 101325) / 6894.75729)));
    	else if (comboInletPressureUnits.getValue() == "psi")
    		textFieldInletPressure.setText(Float.toString((float)((this.inletPressure) / 6894.75729)));	
	}
	
	@FXML private void onOutletPressureUnitsAction(ActionEvent e)
	{
		updateOutletPressure();
	}
	
	private void updateOutletPressure()
	{
		if (comboOutletPressureUnits.getValue() == "kPag")
    		textFieldOutletPressure.setText(Float.toString((float)((this.outletPressure - 101325) / 1000.0)));
    	else if (comboOutletPressureUnits.getValue() == "kPa")
    		textFieldOutletPressure.setText(Float.toString((float)((this.outletPressure) / 1000.0)));
    	else if (comboOutletPressureUnits.getValue() == "psig")
    		textFieldOutletPressure.setText(Float.toString((float)((this.outletPressure - 101325) / 6894.75729)));
    	else if (comboOutletPressureUnits.getValue() == "psi")
    		textFieldOutletPressure.setText(Float.toString((float)((this.outletPressure) / 6894.75729)));	
	}
	
	@FXML private void onConstantFlowRateAction(ActionEvent e)
	{
		switchToConstantFlowRateMode();
	}
	
	@FXML private void onConstantInletPressureAction(ActionEvent e)
	{
		switchToConstantInletPressureMode();
	}
	
	@FXML private void onVacuumAction(ActionEvent e)
	{
		switchToVacuumOutletPressure();
	}
	
	@FXML private void onOtherAction(ActionEvent e)
	{
		switchToOtherOutletPressure();
	}
	
	private void switchToConstantFlowRateMode()
	{
		this.radioConstantFlowRate.setSelected(true);
		this.labelFlowRate.setDisable(false);
		this.textFieldFlowRate.setDisable(false);
		this.labelFlowRateUnits.setDisable(false);

		this.radioConstantInletPressure.setSelected(false);
		this.labelInletPressure.setDisable(true);
		this.textFieldInletPressure.setDisable(true);
		this.comboInletPressureUnits.setDisable(true);
	}

	private void switchToConstantInletPressureMode()
	{
		this.radioConstantFlowRate.setSelected(false);
		this.labelFlowRate.setDisable(true);
		this.textFieldFlowRate.setDisable(true);
		this.labelFlowRateUnits.setDisable(true);

		this.radioConstantInletPressure.setSelected(true);
		this.labelInletPressure.setDisable(false);
		this.textFieldInletPressure.setDisable(false);
		this.comboInletPressureUnits.setDisable(false);
	}

	private void switchToVacuumOutletPressure()
	{
		this.radioOther.setSelected(false);
		this.textFieldOutletPressure.setDisable(true);
		this.comboOutletPressureUnits.setDisable(true);

		this.radioVacuum.setSelected(true);
		
    	this.outletPressure = .000001;
    	updateOutletPressure();
	}
	
	private void switchToOtherOutletPressure()
	{
		this.radioOther.setSelected(true);
		this.textFieldOutletPressure.setDisable(false);
		this.comboOutletPressureUnits.setDisable(false);

		this.radioVacuum.setSelected(false);
		
    	this.outletPressure = 101325;
    	updateOutletPressure();
	}
	
	@FXML private void onNextStepAction(ActionEvent e)
	{
		// Send notification back to the ParentPaneController to change the visible pane
		if (measuredRetentionTimesControllerListener != null)
			this.measuredRetentionTimesControllerListener.onNextStepPressed(this);
	}
	
	public ObservableList<StandardCompound> getStandardsList()
	{
		return this.standardsList;
	}
	
	public double[][] getTemperatureProgram()
	{
		double[][] temperatureProgramInConventionalForm = new double[1][3];
		temperatureProgramInConventionalForm[0][0] = this.rampRate;
		temperatureProgramInConventionalForm[0][1] = this.finalTemperature;
		temperatureProgramInConventionalForm[0][2] = this.finalHoldTime;
		
		return Globals.convertTemperatureProgramInConventionalFormToRegularForm(temperatureProgramInConventionalForm, initialTemperature, initialHoldTime);
	}

	public void setRequiredInitialTemperature(double dInitialTemperature)
	{
		initialTemperature = dInitialTemperature;
		requiredInitialTemperature.setText(((Float)(float)dInitialTemperature).toString() + " °C");
	}
	
	public void setRequiredInitialHoldTime(double dInitialHoldTime)
	{
		initialHoldTime = dInitialHoldTime;
		requiredInitialHoldTime.setText(((Float)(float)dInitialHoldTime).toString() + " min");
	}
	
	public void setRequiredRampRate(double dRampRate)
	{
		rampRate = dRampRate;
		requiredRampRate.setText(((Float)(float)dRampRate).toString() + " °C/min");
	}
	
	public void setRequiredFinalTemperature(double dFinalTemperature)
	{
		finalTemperature = dFinalTemperature;
		requiredFinalTemperature.setText(((Float)(float)dFinalTemperature).toString() + " °C");
	}
	
	public void setRequiredFinalHoldTime(double dFinalHoldTime)
	{
		finalHoldTime = dFinalHoldTime;
		requiredFinalHoldTime.setText(((Float)(float)dFinalHoldTime).toString() + " min");
	}
	
	public void setProgramName(String strName)
	{
		strProgramName = strName;
		requirementsPane.setText("Requirements for " + strProgramName);
	}
	
	public double getFilmThickness()
	{
		return filmThickness;
	}
	
	public double getInnerDiameter()
	{
		return innerDiameter;
	}

	public double getColumnLength()
	{
		return columnLength;
	}
	
	public double getInletPressure()
	{
		return inletPressure;
	}
	
	public double getOutletPressure()
	{
		return outletPressure;
	}
	
	public boolean isUnderVacuum()
	{
		return this.radioVacuum.isSelected();
	}
	
	public boolean getConstantFlowRateMode()
	{
		return this.radioConstantFlowRate.isSelected();
	}
	
	public double getFlowRate()
	{
		return flowRate;
	}

	// Called whenever a text field loses focus
	@Override
	public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) 
	{
		if (newPropertyValue == false)
		{
			// Lost focus, so commit the text
			performValidations();
		}	
	}
	
	private void performValidations()
	{
		validateColumnLength();
		validateColumnInnerDiameter();
		validateColumnFilmThickness();
		validateFlowRate();
		validateInletPressure();
		validateOutletPressure();
		
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
		
		if (dTemp - (2 * this.filmThickness / 1000) > 0)
			this.innerDiameter = dTemp;
		else
			this.innerDiameter = ((this.filmThickness * 2) / 1000) - 0.0001;
		
		textFieldInnerDiameter.setText(Float.toString((float)this.innerDiameter));    
	}
	
	private void validateColumnFilmThickness()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldFilmThickness.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
    	if (dTemp < 0.00001)
			dTemp = 0.00001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		if (this.innerDiameter - (2 * dTemp / 1000) > 0)
			this.filmThickness = dTemp;
		else
			this.filmThickness = ((this.innerDiameter / 2) * 1000) - 0.0001;
		
		textFieldFilmThickness.setText(Float.toString((float)this.filmThickness));    
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
	
	private void validateInletPressure()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldInletPressure.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
		if (dTemp < 0.000000001)
			dTemp = 0.000000001;
		if (dTemp > 100000000)
			dTemp = 100000000;
	
		if (comboInletPressureUnits.getValue() == "kPag")
			this.inletPressure = (dTemp * 1000) + 101325;
		else if (comboInletPressureUnits.getValue() == "kPa")
			this.inletPressure = dTemp * 1000;
		else if (comboInletPressureUnits.getValue() == "psig")
			this.inletPressure = dTemp * 6894.75729 + 101325;
		else if (comboInletPressureUnits.getValue() == "psi")
			this.inletPressure = dTemp * 6894.75729;
		
		textFieldInletPressure.setText(Float.toString((float)Globals.roundToSignificantFigures(dTemp, 6)));  
	}
	
	private void validateOutletPressure()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldOutletPressure.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
		if (dTemp < 0.000000001)
			dTemp = 0.000000001;
		if (dTemp > 100000000)
			dTemp = 100000000;
	
		if (comboOutletPressureUnits.getValue() == "kPag")
			this.outletPressure = (dTemp * 1000) + 101325;
		else if (comboOutletPressureUnits.getValue() == "kPa")
			this.outletPressure = dTemp * 1000;
		else if (comboOutletPressureUnits.getValue() == "psig")
			this.outletPressure = dTemp * 6894.75729 + 101325;
		else if (comboOutletPressureUnits.getValue() == "psi")
			this.outletPressure = dTemp * 6894.75729;
		
		textFieldOutletPressure.setText(Float.toString((float)Globals.roundToSignificantFigures(dTemp, 6)));  
	}
	
	@FXML private void onFindRetentionTimesAutomatically(ActionEvent event)
	{
		PeakFinderGCFX peakFinderGC = new PeakFinderGCFX(parentWindow, Globals.StationaryPhaseArray, true);
		peakFinderGC.setStandardCompoundMZData(Globals.AlkaneMZArray);
		peakFinderGC.setStandardCompoundNames(Globals.AlkaneNameArray);
		peakFinderGC.setIsothermalData(Globals.AlkaneParamArray);
		peakFinderGC.setColumnLength(columnLength);
		peakFinderGC.setConstantFlowMode(this.radioConstantFlowRate.isSelected());
		peakFinderGC.setFilmThickness(filmThickness);
		peakFinderGC.setFlowRate(flowRate);
		peakFinderGC.setTemperatureProgramInConventionalForm(new double[][]{{rampRate, finalTemperature, finalHoldTime}}, initialTemperature, initialHoldTime);
		peakFinderGC.setInnerDiameter(innerDiameter);
		peakFinderGC.setInletPressure(inletPressure);
		peakFinderGC.setOutletPressure(outletPressure, radioVacuum.isSelected());
		peakFinderGC.run();
		
		if (peakFinderGC.getOkPressed())
    	{
    		double[] dRetentionTimes = peakFinderGC.getSelectedRetentionTimes();
    		boolean[] bSkippedStandards = peakFinderGC.getSkippedStandards();
    		int[] iPeakRank = peakFinderGC.getSelectedPeakRank();
    		
    		for (int i = 0; i < dRetentionTimes.length; i++)
    		{
    			// Mark whether the standard is skipped.
    			standardsList.get(i).setUse(!bSkippedStandards[i]);
    			
    			// Put in the correct retention time
    			if (iPeakRank[i] >= 0)
    			{
    				if (!bSkippedStandards[i])
    					standardsList.get(i).setMeasuredRetentionTime(dRetentionTimes[i]);
    				else
    					standardsList.get(i).setMeasuredRetentionTime(0.0);
    			}
    			else
    			{
    				// If the peak wasn't picked, then skip this one.
					standardsList.get(i).setMeasuredRetentionTime(0.0);    				
	    			standardsList.get(i).setUse(false);
    			}
    		}
    		
    		performValidations();
    		
    		fileName = peakFinderGC.getFileName();
    	}
	}
	
	public void writeSaveData(SaveData.MeasuredRetentionTimeSaveData saveData)
	{
		saveData.programName = strProgramName;
		saveData.fileName = fileName;
		saveData.standardsList = standardsList;
		saveData.initialTemperature = initialTemperature;
		saveData.initialHoldTime = initialHoldTime;
		saveData.rampRate = rampRate;
		saveData.finalTemperature = finalTemperature;
		saveData.finalHoldTime = finalHoldTime;
		saveData.columnLength = columnLength;
		saveData.innerDiameter = innerDiameter;
		saveData.filmThickness = filmThickness;
		saveData.flowRate = flowRate;
		saveData.inletPressure = inletPressure;
		saveData.outletPressure = outletPressure;
		saveData.constantFlowRateMode = this.radioConstantFlowRate.isSelected();
		saveData.vacuumOutletPressure = this.radioVacuum.isSelected();
	}
	
	public void loadSaveData(SaveData.MeasuredRetentionTimeSaveData saveData)
	{
		strProgramName = saveData.programName;
		fileName = saveData.fileName;
		standardsList = saveData.standardsList;
		initialTemperature = saveData.initialTemperature;
		initialHoldTime = saveData.initialHoldTime;
		rampRate = saveData.rampRate;
		finalTemperature = saveData.finalTemperature;
		finalHoldTime = saveData.finalHoldTime;
		columnLength = saveData.columnLength;
		innerDiameter = saveData.innerDiameter;
		filmThickness = saveData.filmThickness;
		flowRate = saveData.flowRate;
		inletPressure = saveData.inletPressure;
		outletPressure = saveData.outletPressure;
		boolean isConstantFlowRateMode = saveData.constantFlowRateMode;
		boolean isVacuumOutletPressure = saveData.vacuumOutletPressure;

		setTableMeasuredRetentionTimesItems(standardsList);

		if (isConstantFlowRateMode)
			this.switchToConstantFlowRateMode();
		else
			this.switchToConstantInletPressureMode();

		if (isVacuumOutletPressure)
			this.switchToVacuumOutletPressure();
		else
			this.switchToOtherOutletPressure();

		this.setRequiredInitialTemperature(initialTemperature);
		this.setRequiredInitialHoldTime(initialHoldTime);
		this.setRequiredRampRate(rampRate);
		this.setRequiredFinalTemperature(finalTemperature);
		this.setRequiredFinalHoldTime(finalHoldTime);
		this.textFieldColumnLength.setText(Float.toString((float)columnLength));
		this.textFieldInnerDiameter.setText(Float.toString((float)innerDiameter));
		this.textFieldFilmThickness.setText(Float.toString((float)filmThickness));
		this.textFieldFlowRate.setText(Float.toString((float)flowRate));
		this.updateInletPressure();
		this.updateOutletPressure();
		this.setProgramName(strProgramName);
	}
	
	public void setParentWindow(Window parentWindow)
	{
		this.parentWindow = parentWindow;
	}
}
