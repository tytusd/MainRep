package boswell.peakfinderlcfx;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

public class PeakFinderSettingsPaneController implements Initializable, ChangeListener<Boolean>{

	@FXML private Rectangle s;
    @FXML private ComboBox<String> comboStationaryPhase;
    @FXML private Label labelGradientDelayVolume;
    @FXML private Label labelFlowRate;
    @FXML private Label labelFlowRateUnits;
    @FXML private Label labelMixingVolume;
    @FXML private Label labelNonMixingVolume;
    
    @FXML private TextField textFieldDataFile;
    @FXML private TextField textFieldMixingVolume;
    @FXML private TextField textFieldInnerDiameter;
    @FXML private TextField textFieldColumnLength;
    @FXML private TextField textFieldFlowRate;
    @FXML private TextField textFieldNonMixingVolume;
    @FXML private TableView<GradientProgramStep> tableViewGradientProgram;
    @FXML private TableColumn<GradientProgramStep, Double> columnTime; 
    @FXML private TableColumn<GradientProgramStep, Double> columnSolventComposition;
    @FXML private TitledPane titledPaneEnterLCConditions;
    
    @FXML private Button buttonBrowse;
	@FXML private Button buttonAdd;
	@FXML private Button buttonInsert;
	@FXML private Button buttonRemove;
	@FXML private Button buttonOK;
	@FXML private Button buttonCancel;
	
	private Stage thisStage;
	private ObservableList<GradientProgramStep> gradientProgramList;
	private Preferences prefs;
	
	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();
	private double initialSolventComposition = 5.0;
	private double columnLength = 100.0; // in m
	private double innerDiameter = 2.1; // in mm
	private double flowRate = 1.0; // in mL/min
	private double initialTime = 0.0; // in min
	private double mixingVolume = 0.1;
	private double nonMixingVolume = 0.2;
	private boolean okPressed = false;
	private String fileName;
	private double[][] gradientProgram;
    


    public class GradientProgramStep{
    	
		private DoubleProperty time;
    	private DoubleProperty solventComposition;
    	public GradientProgramStep(double time, double solventComposition) {
			this.time = new SimpleDoubleProperty(time);
			this.solventComposition = new SimpleDoubleProperty(solventComposition);
		}
    	
    	public double getTime(){
    		return time.get();
    	}
    	
    	public void setTime(double time){
    		this.time.set(time);
    	}
    	
    	public DoubleProperty getTimeProperty(){
    		return time;
    	}
    	
    	public double getSolventComposition(){
    		return solventComposition.get();
    	}
    	
    	public void setSolventComposition(double solventComposition){
    		this.solventComposition.set(solventComposition);
    	}
    	
    	public DoubleProperty getSolventCompositionProperty(){
    		return solventComposition;
    	}
    }
    
	public Stage getStage() {
		return thisStage;
	}

	public void setStage(Stage thisStage) {
		this.thisStage = thisStage;
	}

    public void setEditable(boolean editable){
		titledPaneEnterLCConditions.setDisable(!editable);
	}
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		s.setWidth(rem);
		s.setHeight(rem);
		
		comboStationaryPhase.getItems().clear();
		comboStationaryPhase.getItems().addAll("Agilent HP-5MS UI");
		textFieldColumnLength.focusedProperty().addListener(this);
		textFieldDataFile.focusedProperty().addListener(this);
		textFieldFlowRate.focusedProperty().addListener(this);
		textFieldInnerDiameter.focusedProperty().addListener(this);
		textFieldMixingVolume.focusedProperty().addListener(this);
		textFieldNonMixingVolume.focusedProperty().addListener(this);
		
		// Create the Preferences class
		prefs = Preferences.userNodeForPackage(this.getClass());
		
		tableViewGradientProgram.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>(){

			@Override
			public void onChanged(Change<? extends Integer> change) {
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
		
		//Load the table with correct values
		List<GradientProgramStep> data = new ArrayList<GradientProgramStep>();
		gradientProgramList = FXCollections.observableArrayList(data);
		
		columnTime.setCellValueFactory(new PropertyValueFactory<GradientProgramStep, Double>("time"));
		columnTime.setCellFactory(TextFieldTableCell.<GradientProgramStep, Double>forTableColumn(new DoubleStringConverter()));
		columnSolventComposition.setCellValueFactory(new PropertyValueFactory<GradientProgramStep, Double>("solventComposition"));
		columnSolventComposition.setCellFactory(TextFieldTableCell.<GradientProgramStep, Double>forTableColumn(new DoubleStringConverter()));
		this.tableViewGradientProgram.setItems(gradientProgramList);
	}
	
	@FXML private void onOKAction(ActionEvent e){
		this.okPressed = true;
		setFileName(this.textFieldDataFile.getText());
		thisStage.close();
	}
	
	@FXML private void onCancelAction(ActionEvent e){
		this.okPressed = false;
		thisStage.close();
	}

	@FXML private void onInsertAction(ActionEvent e){
		int activatedRowIndex = this.tableViewGradientProgram.getSelectionModel().getSelectedIndex();
		if (activatedRowIndex < 0)
			return;

    	double rowValue1, rowValue2;
    	
		rowValue1 = (Double) this.gradientProgramList.get(activatedRowIndex).getTime();
		rowValue2 = (Double) this.gradientProgramList.get(activatedRowIndex).getSolventComposition();

		GradientProgramStep newStep = new GradientProgramStep(rowValue1, rowValue2);
    	
		gradientProgramList.add(activatedRowIndex, newStep);
	}
	
	public boolean getOKButtonPressed(){
		return this.okPressed;
	}	
	
	@FXML private void onAddAction(ActionEvent e){
		int lastRowIndex = gradientProgramList.size() - 1;

    	double rowValue1, rowValue2;
    	//TODO: Set default values here
    	if (lastRowIndex == -1)    	{
    		rowValue1 = 10.0; //Default time
    		rowValue2 = initialSolventComposition ;
    	}
    	else    	{
    		rowValue1 = gradientProgramList.get(lastRowIndex).getTime(); // default time
    		rowValue2 = gradientProgramList.get(lastRowIndex).getSolventComposition();
    	}
    	
		GradientProgramStep newStep = new GradientProgramStep(rowValue1, rowValue2);

		gradientProgramList.add(newStep);
	}
	
	@FXML private void onRemoveAction(ActionEvent e){
		int activatedRowIndex = this.tableViewGradientProgram.getSelectionModel().getSelectedIndex();
		if (activatedRowIndex < 0)
			return;

		gradientProgramList.remove(activatedRowIndex);
    	
	    this.buttonRemove.setDisable(true);
	    this.buttonInsert.setDisable(true);
	}
	

	@Override
	public void changed(ObservableValue<? extends Boolean> observable,
			Boolean oldValue, Boolean newValue) {
		if(newValue == false){
			performValidations();
		}
			
	}
	
	@FXML
	void onBrowseAction(ActionEvent event) {
		if (thisStage == null)
			return;
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open LC-MS Data File");
		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Supported File Types (*.mzXML, *.mzML, *.CDF)", "*.mzXML", "*.mzML", "*.CDF"),
                new FileChooser.ExtensionFilter("mzXML Files (*.mzXML)", "*.mzXML"),
                new FileChooser.ExtensionFilter("mzML Files (*.mzML)", "*.mzML"),
                new FileChooser.ExtensionFilter("netCDF Files (*.CDF)", "*.CDF")
            );

		// Set default directory
		String lastOutputDir = prefs.get("LAST_OUTPUT_DIR", "");
		if (lastOutputDir != ""){
			File lastDir = new File(lastOutputDir);
			if (lastDir.exists())
				fileChooser.setInitialDirectory(lastDir.getParentFile());
		}
		
		File returnedFile = fileChooser.showOpenDialog(thisStage);

		if (returnedFile != null){
			this.textFieldDataFile.setText(returnedFile.getPath());
			prefs.put("LAST_OUTPUT_DIR", returnedFile.getAbsolutePath());
		}
	}

	@FXML
	void actionPerformValidation(ActionEvent event) {
		performValidations();
	}
	
	
	public void performValidations()	{
		this.columnLength = validateParameters(textFieldColumnLength, 0.1, 10000, this.columnLength);
		this.innerDiameter = validateParameters(textFieldInnerDiameter, 0.01, 10000, this.innerDiameter);
		this.flowRate = validateParameters(textFieldFlowRate, 0.000000001, 10000, this.flowRate);
		this.mixingVolume = validateParameters(textFieldMixingVolume, 0.001, 100000, this.mixingVolume);
		this.nonMixingVolume = validateParameters(textFieldNonMixingVolume, 0.001, 100000, this.nonMixingVolume);
		setFileName(this.textFieldDataFile.getText());
		//TODO: Check if we need any more validations. Compare with PeakFinderLC
	}
	
	public double validateParameters(TextField textField, double dTemp1, double dTemp2, double valueToSet){
    	double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textField.getText());
    	}
    	catch (NumberFormatException e)
    	{
    		dTemp = 0.0;
    	}
		
		if (dTemp < dTemp1)
			dTemp = dTemp1;
		if (dTemp > dTemp2)
			dTemp = dTemp2;
		valueToSet = dTemp;
		textField.setText(Float.toString((float)valueToSet));    
		return valueToSet;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.textFieldDataFile.setText(fileName);
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

	public double getMixingVolume() {
		return mixingVolume;
	}

	public void setMixingVolume(double mixingVolume) {
		this.mixingVolume = mixingVolume;
		this.textFieldMixingVolume.setText(Float.toString((float)mixingVolume));
	}

	public double getNonMixingVolume() {
		return nonMixingVolume;
	}

	public void setNonMixingVolume(double nonMixingVolume) {
		this.nonMixingVolume = nonMixingVolume;
		this.textFieldNonMixingVolume.setText(Float.toString((float)nonMixingVolume));
	}

	public double[][] getGradientProgram(double[] retentionTimes) {
		double[][] gradientProgramInConventionalForm = new double[gradientProgramList.size()][3];
		for (int i = 0; i < gradientProgramList.size(); i++)
		{
			gradientProgramInConventionalForm[i][0] = gradientProgramList.get(i).getTime();
			gradientProgramInConventionalForm[i][1] = gradientProgramList.get(i).getSolventComposition();
		}
		
		return Globals.convertGradientProgramInConventionalFormToRegularForm(gradientProgramInConventionalForm, initialTime, initialSolventComposition, retentionTimes);
	}

	public void setGradientProgram(double[][] gradientProgram) {
		this.gradientProgram = gradientProgram;
	}

	public void setGradientProgramInConventionalForm(
			double[][] gradientProgramInConventionalForm, double initialTime,
			double initialSolventComposition) {
		
		gradientProgramList.clear();
		for(int i = 0; i < gradientProgramInConventionalForm.length; i++){
			GradientProgramStep step = new GradientProgramStep(gradientProgramInConventionalForm[i][0], gradientProgramInConventionalForm[i][1]);
			gradientProgramList.add(step);
		}
//		this.initialTime = initialTime;
//		this.initialSolventComposition = initialSolventComposition;
//		gradientProgram = Globals.convertGradientProgramInConventionalFormToRegularForm(gradientProgramInConventionalForm, initialTime, initialSolventComposition);
	}

}
