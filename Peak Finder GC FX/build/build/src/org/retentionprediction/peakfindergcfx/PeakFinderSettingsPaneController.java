package org.retentionprediction.peakfindergcfx;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;

public class PeakFinderSettingsPaneController implements Initializable, ChangeListener<Boolean>
{
	@FXML private ComboBox<String> comboStationaryPhase;
	@FXML private ComboBox<String> comboInletPressureUnits;
	@FXML private ComboBox<String> comboOutletPressureUnits;
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
	@FXML private TitledPane titledPaneEnterGCConditions;
	@FXML private TextField textFieldDataFile;
	@FXML private TextField textFieldInitialTemperature;
	@FXML private TextField textFieldInitialHoldTime;
	@FXML private TableView<TemperatureProgramStep> tableViewTemperatureProgram;
	@FXML private TableColumn<TemperatureProgramStep, Double> columnRampRate;
	@FXML private TableColumn<TemperatureProgramStep, Double> columnFinalTemperature;
	@FXML private TableColumn<TemperatureProgramStep, Double> columnHoldTime;
	@FXML private Button buttonAdd;
	@FXML private Button buttonInsert;
	@FXML private Button buttonRemove;
		
	private double columnLength = 30.0; // in m
	private double innerDiameter = 0.25; // in mm
	private double filmThickness = 0.25; // in um
	private double flowRate = 1; // in mL/min
	private double inletPressure = 151325; // in Pa;
	private double outletPressure = 0.000001; // in Pa;
	private double initialTime = 5.0; // in min
	private double initialTemperature = 60.0; // in C
	private boolean isConstantFlowRateMode = false;
	private boolean isVacuumOutletPressure = true;
	private boolean okPressed = false;
	private String fileName;
	private double[][] temperatureProgram = {{0.0, 5.0}, {5.0, 320.0}};
	private ObservableList<TemperatureProgramStep> temperatureProgramList;
	private Stage thisStage;

	private Preferences prefs;
	
	public class TemperatureProgramStep
	{
		private DoubleProperty rampRate;
		private DoubleProperty finalTemperature;
		private DoubleProperty finalHoldTime;
		
		TemperatureProgramStep(double rampRate, double finalTemperature, double finalHoldTime)
		{
			this.rampRate = new SimpleDoubleProperty(rampRate);
			this.finalTemperature = new SimpleDoubleProperty(finalTemperature);
			this.finalHoldTime = new SimpleDoubleProperty(finalHoldTime);
		}
				
		public double getRampRate()
		{
			return rampRate.get();
		}
		
		public void setUse(double rampRate)
		{
			this.rampRate.set(rampRate);
		}
		
		public DoubleProperty rampRateProperty() 
		{
		    return rampRate;
		}
		
		public double getFinalTemperature()
		{
			return finalTemperature.get();
		}
		
		public void setFinalTemperature(double finalTemperature)
		{
			this.finalTemperature.set(finalTemperature);
		}
		
		public DoubleProperty finalTemperatureProperty() 
		{
		    return finalTemperature;
		}
		
		public double getFinalHoldTime()
		{
			return finalHoldTime.get();
		}
		
		public void setFinalHoldTime(double finalHoldTime)
		{
			this.finalHoldTime.set(finalHoldTime);
		}
		
		public DoubleProperty finalHoldTimeProperty() 
		{
		    return finalHoldTime;
		}
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		comboStationaryPhase.getItems().clear();
		comboStationaryPhase.getItems().addAll("Agilent DB-5MS UI");
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
		textFieldInitialHoldTime.focusedProperty().addListener(this);
		textFieldInitialTemperature.focusedProperty().addListener(this);
		
		// Create the Preferences class
		prefs = Preferences.userNodeForPackage(this.getClass());
		
		// Add a listener to the table so that we can detect when the selection has changed
		tableViewTemperatureProgram.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
			    {
			        @Override
			        public void onChanged(Change<? extends Integer> change)
			        {
			            if (change.getList().size() >= 1)
			            {
			            	buttonInsert.setDisable(false);
			                buttonRemove.setDisable(false);
			            }
			            else
			            {
			            	buttonInsert.setDisable(true);
			                buttonRemove.setDisable(true);
			            }
			        }

			    });
		
		// Load the table with the correct values
		List<TemperatureProgramStep> data = new ArrayList<TemperatureProgramStep>();

		/*for (int i = 0; i < this.temperatureProgram.length; i++)
		{
			TemperatureProgramStep newItem = new TemperatureProgramStep(temperatureProgram[i][0], temperatureProgram[i][1], temperatureProgram[i][2]);
			data.add(newItem);
		}*/
		
		temperatureProgramList = FXCollections.observableArrayList(data);

		// Set up the table view
		columnRampRate.setCellValueFactory(new PropertyValueFactory<TemperatureProgramStep, Double>("rampRate"));
		columnRampRate.setCellFactory(TextFieldTableCell.<TemperatureProgramStep, Double>forTableColumn(new DoubleStringConverter()));
		columnFinalTemperature.setCellValueFactory(new PropertyValueFactory<TemperatureProgramStep, Double>("finalTemperature"));
		columnFinalTemperature.setCellFactory(TextFieldTableCell.<TemperatureProgramStep, Double>forTableColumn(new DoubleStringConverter()));
		columnHoldTime.setCellValueFactory(new PropertyValueFactory<TemperatureProgramStep, Double>("finalHoldTime"));
		columnHoldTime.setCellFactory(TextFieldTableCell.<TemperatureProgramStep, Double>forTableColumn(new DoubleStringConverter()));
		
		this.tableViewTemperatureProgram.setItems(temperatureProgramList);
	}

	public String getFileName()
	{
		return fileName;
	}
	
	public void setStage(Stage stage)
	{
		thisStage = stage;
	}
	
	public void setEditable(boolean editable)
	{
		titledPaneEnterGCConditions.setDisable(!editable);
	}
	
    public void setFileName(String fileName)
    {
    	this.fileName = fileName;
    	this.textFieldDataFile.setText(fileName);
    }
    
	public void setTemperatureProgramInConventionalForm(double[][] temperatureProgramInConventionalForm, double initialTemperature, double initialTime)
	{
		temperatureProgramList.clear();
		
		for (int i = 0; i < temperatureProgramInConventionalForm.length; i++)
		{
			TemperatureProgramStep newItem = new TemperatureProgramStep(temperatureProgramInConventionalForm[i][0], temperatureProgramInConventionalForm[i][1], temperatureProgramInConventionalForm[i][2]);
			temperatureProgramList.add(newItem);
		}
		
		this.initialTime = initialTime;
		this.textFieldInitialHoldTime.setText(Float.toString((float)initialTime));

		this.initialTemperature = initialTemperature;
		this.textFieldInitialTemperature.setText(Float.toString((float)initialTemperature));

		temperatureProgram = Globals.convertTemperatureProgramInConventionalFormToRegularForm(temperatureProgramInConventionalForm, initialTemperature, initialTime);
	}

	public double[][] getTemperatureProgram()
	{
		double[][] temperatureProgramInConventionalForm = new double[temperatureProgramList.size()][3];
		for (int i = 0; i < temperatureProgramList.size(); i++)
		{
			temperatureProgramInConventionalForm[i][0] = temperatureProgramList.get(i).getRampRate();
			temperatureProgramInConventionalForm[i][1] = temperatureProgramList.get(i).getFinalTemperature();
			temperatureProgramInConventionalForm[i][2] = temperatureProgramList.get(i).getFinalHoldTime();
		}
		
		return Globals.convertTemperatureProgramInConventionalFormToRegularForm(temperatureProgramInConventionalForm, initialTemperature, initialTime);
	}
	
	public void setFlowRate(double flowRate)
	{
		this.flowRate = flowRate;
		this.textFieldFlowRate.setText(Float.toString((float)flowRate));
	}
	
	public double getFlowRate()
	{
		return flowRate;
	}
	
	public void setColumnLength(double columnLength)
	{
		this.columnLength = columnLength;
		this.textFieldColumnLength.setText(Float.toString((float)columnLength));
	}
	
	public double getColumnLength()
	{
		return this.columnLength;
	}
	
	public void setInnerDiameter(double innerDiameter)
	{
		this.innerDiameter = innerDiameter;
		this.textFieldInnerDiameter.setText(Float.toString((float)innerDiameter));
	}
	
	public double getInnerDiameter()
	{
		return this.innerDiameter;
	}
	
	public void setFilmThickness(double filmThickness)
	{
		this.filmThickness = filmThickness;
		this.textFieldFilmThickness.setText(Float.toString((float)filmThickness));
	}

	public double getFilmThickness()
	{
		return this.filmThickness;
	}
	
	public void setInletPressure(double inletPressure)
	{
		this.inletPressure = inletPressure;
		updateInletPressure();
	}

	public double getInletPressure()
	{
		return this.inletPressure;
	}
	
	public void setConstantFlowMode(boolean constantFlowMode)
	{
		this.isConstantFlowRateMode = constantFlowMode;
		if (constantFlowMode)
			switchToConstantFlowRateMode();
		else
			switchToConstantInletPressureMode();
	}
	
	public boolean getConstantFlowMode()
	{
		return this.isConstantFlowRateMode;
	}
	
	public void setOutletPressure(double outletPressure, boolean isUnderVacuum)
	{
		this.isVacuumOutletPressure = isUnderVacuum;
		this.outletPressure = outletPressure;
		updateOutletPressure();
		
		if (isUnderVacuum)
			switchToVacuumOutletPressure();
		else
			switchToOtherOutletPressure();
	}
	
	public double getOutletPressure()
	{
		return outletPressure;
	}

	/*public void setInitialTime(double initialTime)
	{
		this.initialTime = initialTime;
		this.textFieldInitialHoldTime.setText(Float.toString((float)initialTime));
	}*/

	public double getInitialTime()
	{
		return initialTime;
	}
	
/*	public void setInitialTemperature(double initialTemperature)
	{
		this.initialTemperature = initialTemperature;
		this.textFieldInitialTemperature.setText(Float.toString((float)initialTemperature));
	}*/
	
	public double getInitialTemperature()
	{
		return initialTemperature;
	}

	@FXML private void onOKAction(ActionEvent e)
	{
		this.okPressed = true;
		fileName = this.textFieldDataFile.getText();
		thisStage.close();
	}

	@FXML private void onCancelAction(ActionEvent e)
	{
		this.okPressed = false;
		thisStage.close();
	}
	
	public boolean getOKButtonPressed()
	{
		return this.okPressed;
	}
	
	@FXML private void onInsertAction(ActionEvent e)
	{
		int activatedRowIndex = this.tableViewTemperatureProgram.getSelectionModel().getSelectedIndex();
		if (activatedRowIndex < 0)
			return;

    	double rowValue1, rowValue2, rowValue3;
    	
		rowValue1 = (Double) this.temperatureProgramList.get(activatedRowIndex).getRampRate();
		rowValue2 = (Double) this.temperatureProgramList.get(activatedRowIndex).getFinalTemperature();
		rowValue3 = (Double) this.temperatureProgramList.get(activatedRowIndex).getFinalHoldTime();

		TemperatureProgramStep newStep = new TemperatureProgramStep(rowValue1, rowValue2, rowValue3);
    	
		temperatureProgramList.add(activatedRowIndex, newStep);
	}
	
	@FXML private void onAddAction(ActionEvent e)
	{
		int lastRowIndex = temperatureProgramList.size() - 1;

    	double rowValue1, rowValue2, rowValue3;
    	
    	if (lastRowIndex == -1)
    	{
    		rowValue1 = 10.0; // default ramp rate
    		rowValue2 = initialTemperature;
    		rowValue3 = 5.0; // default hold time
    	}
    	else
    	{
    		rowValue1 = 10.0; // default ramp rate
    		rowValue2 = temperatureProgramList.get(lastRowIndex).getFinalTemperature();
    		rowValue3 = 5.0; // default hold time
    	}
    	
		TemperatureProgramStep newStep = new TemperatureProgramStep(rowValue1, rowValue2, rowValue3);

		temperatureProgramList.add(newStep);
	}
	
	@FXML private void onRemoveAction(ActionEvent e)
	{
		int activatedRowIndex = this.tableViewTemperatureProgram.getSelectionModel().getSelectedIndex();
		if (activatedRowIndex < 0)
			return;

		temperatureProgramList.remove(activatedRowIndex);
    	
	    this.buttonRemove.setDisable(true);
	    this.buttonInsert.setDisable(true);
	}
	
	@FXML private void onBrowseAction(ActionEvent e)
	{
		if (thisStage == null)
			return;
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open GC-MS Data File");
		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Supported File Types (*.mzXML, *.mzML, *.CDF)", "*.mzXML", "*.mzML", "*.CDF"),
                new FileChooser.ExtensionFilter("mzXML Files (*.mzXML)", "*.mzXML"),
                new FileChooser.ExtensionFilter("mzML Files (*.mzML)", "*.mzML"),
                new FileChooser.ExtensionFilter("netCDF Files (*.CDF)", "*.CDF")
            );

		// Set default directory
		String lastOutputDir = prefs.get("LAST_OUTPUT_DIR", "");
		if (lastOutputDir != "")
		{
			File lastDir = new File(lastOutputDir);
			if (lastDir.exists())
				fileChooser.setInitialDirectory(lastDir.getParentFile());
		}
		
		File returnedFile = fileChooser.showOpenDialog(thisStage);

		if (returnedFile != null) 
		{
			this.textFieldDataFile.setText(returnedFile.getPath());
			prefs.put("LAST_OUTPUT_DIR", returnedFile.getAbsolutePath());
		}
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
		validateInitialTime();
		validateInitialTemperature();
		
		fileName = this.textFieldDataFile.getText();
		
		
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
	
	private void validateInitialTime()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldInitialHoldTime.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
    	if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 1000)
			dTemp = 1000;
		
		this.initialTime = dTemp;
		textFieldInitialHoldTime.setText(Float.toString((float)this.initialTime));    
	}

	private void validateInitialTemperature()
	{
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textFieldInitialTemperature.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
    	if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 500)
			dTemp = 500;
		
		if (this.temperatureProgramList.size() > 0)
		{
			double dFirstTemp = temperatureProgramList.get(0).getFinalTemperature();
			if (dTemp > dFirstTemp)
				dTemp = dFirstTemp;
		}
		
		this.initialTemperature = dTemp;
		textFieldInitialTemperature.setText(Float.toString((float)this.initialTemperature));    
	}
}
