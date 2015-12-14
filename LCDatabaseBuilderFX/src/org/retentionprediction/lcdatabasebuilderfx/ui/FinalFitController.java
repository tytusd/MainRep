package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;

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
import org.retentionprediction.lcdatabasebuilderfx.business.Utilities;
import org.retentionprediction.lcdatabasebuilderfx.ui.SolveParametersTask.SolveParametersListener;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import boswell.graphcontrolfx.GraphControlFX;

public class FinalFitController implements Initializable, SolveParametersListener, ClipboardOwner{

		public interface FinalFitControllerListener{
			public void onPreviousStepPressed(BackcalculateController thisController);

			public void onPreviousStepPressed(
					FinalFitController finalFitController);
		}
	
	 	@FXML private AnchorPane anchorPaneRetentionSolver;
	    
	 	@FXML VBox finalFitRoot;
	 	
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
	    @FXML private Button exportToXml;
	    @FXML private Button importCompoundsButton;
	    @FXML private Button exportDataButton;
	    @FXML private Button runBatchSolverButton;

	    @FXML private TextField textInchi;
	    @FXML private TextField textChemicalFormula; 
	    @FXML private TextField textIupacName;
	    @FXML private TextField textPubChemId;
	    @FXML private TextField textCompoundName;
	    @FXML private TextField textInchiKey;
	    @FXML private TextField textSmiles;
	    
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
	    private Preferences  prefs = Preferences.userNodeForPackage(this.getClass());
	    private SolveParametersTask solveParametersTask;
	    private ObservableList<StandardCompound> compoundsList;
	    private ObservableList<StandardCompound> compoundsRetentionList;
	    private ObservableList<StandardCompound> importedCompoundsList;
	    private FinalFitControllerListener finalFitControllerListener;
	
	    private BackcalculateController backcalculateController;
	
	    private double instrumentDeadTime = 0;
	    
	    

		private MeasuredRetentionTimesController measuredRetentionTimeController;

		private String[] padeCoefficients;
	    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try{
			retentionSolverTimeGraph = new GraphControlFX();
			retentionSolverTimeGraph.setControlsEnabled(false);
			retentionSolverTimeGraph.setYAxisTitle("log K");
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
		exportToXml.setDisable(true);
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
		exportToXml.setDisable(true);
		solveParametersTask = new SolveParametersTask();
		BackcalculateController[] controller = new BackcalculateController[1];
		controller[0] = this.backcalculateController;
    	solveParametersTask.setBackCalculateController(controller);
    	solveParametersTask.setProgramList(compoundsRetentionList);
    	solveParametersTask.setGraphControl(retentionSolverTimeGraph);
    	solveParametersTask.setSolveParametersListener(this);
    	solveParametersTask.setInjectionMode(true);
    	solveParametersTask.setInstrumentDeadTime(instrumentDeadTime);
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
		
	    anchorPaneRetentionSolver.getChildren().clear();
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

	public double getInstrumentDeadTime() {
		return instrumentDeadTime;
	}

	public void setInstrumentDeadTime(double instrumentDeadTime) {
		this.instrumentDeadTime = instrumentDeadTime;
	}
	
	@FXML public void exportToXml(){
		Map<String,String> solvermap = null;
		if(solveParametersTask != null){
			solvermap = solveParametersTask.exportToXml();
		}

		Map<String,String> measuredControllerMap = measuredRetentionTimeController.exportToXml();
		Map<String,String> backcalculateControllerMap = backcalculateController.exportStandardsToXml();
		Map<String,String> systemSuitabilityMap = backcalculateController.exportSystemSuitabilityInfoToXml();
		
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
			
			createXml(measuredControllerMap,backcalculateControllerMap,systemSuitabilityMap,solvermap,outputFile);
			prefs.put("LAST_OUTPUT_DIR", outputFile.getAbsolutePath());
		}
	}

	public void setMeasuredRetentionTimesController(
			MeasuredRetentionTimesController measuredRetentionTimesController) {
		this.measuredRetentionTimeController = measuredRetentionTimesController;
		
	}

	public void createXml(Map<String, String> measuredControllerMap, Map<String, String> backcalculateControllerMap, Map<String, String> systemSuitabilityMap, Map<String, String> solvermap, File outputFile){
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
		mode.setTextContent("Injection");
		root.appendChild(mode);
		
		//Add compound
		Element compoundName = doc.createElement("compound");
		compoundName.setTextContent(textCompoundName.getText());
		root.appendChild(compoundName);
		
		Element iupac = doc.createElement("IUPAC-name");
		iupac.setTextContent(textIupacName.getText());
		root.appendChild(iupac);
		
		Element formula = doc.createElement("molecular-formula");
		formula.setTextContent(textChemicalFormula.getText());
		root.appendChild(formula);
		
		Element smiles = doc.createElement("smiles");
		smiles.setTextContent(textSmiles.getText());
		root.appendChild(smiles);
		
		Element pubchem = doc.createElement("PubChem-Id");
		pubchem.setTextContent(textPubChemId.getText());
		root.appendChild(pubchem);
		
		Element inchi = doc.createElement("inchi");
		inchi.setTextContent(textInchi.getText());
		root.appendChild(inchi);
		
		Element inchiKey = doc.createElement("inchi-key");
		inchiKey.setTextContent(textInchiKey.getText());
		root.appendChild(inchiKey);
		
		//Add metadata - Here you add all the information from measured retention times controller - first step of the program
		Element metadata = doc.createElement("metadata");
		root.appendChild(metadata);
		
		Element stationaryPhase = doc.createElement("stationary-phase");
		stationaryPhase.setTextContent(measuredControllerMap.get("StationaryPhase"));
		metadata.appendChild(stationaryPhase);
		
		Element innerDiameter = doc.createElement("column-inner-diameter");
		innerDiameter.setTextContent(measuredControllerMap.get("ColumnInnerDiameter"));
		metadata.appendChild(innerDiameter);
		
		Element columnLength = doc.createElement("column-length");
		columnLength.setTextContent(measuredControllerMap.get("ColumnLength"));
		metadata.appendChild(columnLength);
		
		Element instrumentDeadTime = doc.createElement("instrument-dead-time");
		instrumentDeadTime.setTextContent(measuredControllerMap.get("InstrumentDeadTime"));
		metadata.appendChild(instrumentDeadTime);
		
		Element flowRate = doc.createElement("flow-rate");
		flowRate.setTextContent(measuredControllerMap.get("FlowRate"));
		metadata.appendChild(flowRate);
		
		Element gradients = doc.createElement("gradients");
		metadata.appendChild(gradients);
		
		Element gradient = doc.createElement("gradient");
		gradients.appendChild(gradient);
		
		String[] times = measuredControllerMap.get("GradientTimes").split("\\$");
		String[] solventCompositions = measuredControllerMap.get("GradientSolventCompositions").split("\\$");
		
		Element initialTime = doc.createElement("initial-time");
		initialTime.setTextContent(times[0]);
		gradient.appendChild(initialTime);
		
		Element initialComposition = doc.createElement("initial-solvent-composition");
		initialComposition.setTextContent(solventCompositions[0]);
		gradient.appendChild(initialComposition);
		
		Element finalTime = doc.createElement("final-time");
		finalTime.setTextContent(times[1]);
		gradient.appendChild(finalTime);
		
		Element finalComposition = doc.createElement("final-solvent-composition");
		finalComposition.setTextContent(solventCompositions[1]);
		gradient.appendChild(finalComposition);
		
		
		//Add Standards information
		Element backcalculateInfo = doc.createElement("backcalculate-information");
		root.appendChild(backcalculateInfo);
		
		Element variance = doc.createElement("variance");
		variance.setTextContent(backcalculateControllerMap.get("Variance"));
		backcalculateInfo.appendChild(variance);
		
		Element iterations = doc.createElement("iterations");
		iterations.setTextContent(backcalculateControllerMap.get("Iterations"));
		backcalculateInfo.appendChild(iterations);
		
		Element timeElapsed = doc.createElement("time-elapsed");
		timeElapsed.setTextContent(backcalculateControllerMap.get("TimeElapsed"));
		backcalculateInfo.appendChild(timeElapsed);
		
		Element standards = doc.createElement("standard-compounds");
		backcalculateInfo.appendChild(standards);
		
		String[] compoundNames = backcalculateControllerMap.get("CompoundNames").split("\\$");
		String[] predictedRetentionTimes = backcalculateControllerMap.get("PredictedRetentionTimes").split("\\$");
		String[] measuredRetentionTimes = backcalculateControllerMap.get("MeasuredRetentionTimes").split("\\$");
		String[] retentionErrors = backcalculateControllerMap.get("Errors").split("\\$");
		String[] mzs = backcalculateControllerMap.get("MzRatios").split("\\$");
		
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
		
		//Add system suitability check information
		Element systemSuitabilityCheck = doc.createElement("system-suitability-check");
		root.appendChild(systemSuitabilityCheck);
		
		Element predictedError = doc.createElement("predicted-error");
		predictedError.setTextContent(systemSuitabilityMap.get("PredictedError"));
		systemSuitabilityCheck.appendChild(predictedError);
		
		Element expectedError = doc.createElement("expected-error");
		expectedError.setTextContent(systemSuitabilityMap.get("ExpectedError"));
		systemSuitabilityCheck.appendChild(expectedError);
		
		Element columnRating = doc.createElement("column-rating");
		columnRating.setTextContent(systemSuitabilityMap.get("ColumnRating"));
		systemSuitabilityCheck.appendChild(columnRating);
		
		standards = doc.createElement("standard-compounds");
		systemSuitabilityCheck.appendChild(standards);
		
		compoundNames = systemSuitabilityMap.get("CompoundNames").split("\\$");
		mzs = systemSuitabilityMap.get("MzRatios").split("\\$");
		predictedRetentionTimes = systemSuitabilityMap.get("PredictedRetentionTimes").split("\\$");
		measuredRetentionTimes = systemSuitabilityMap.get("MeasuredRetentionTimes").split("\\$");
		retentionErrors = systemSuitabilityMap.get("Errors").split("\\$");
		
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
			
			variance = doc.createElement("variance");
			variance.setTextContent(solvermap.get("Variance"));
			solvedRelationship.appendChild(variance);
			
			timeElapsed = doc.createElement("time-elapsed");
			timeElapsed.setTextContent(solvermap.get("TimeElapsed"));
			solvedRelationship.appendChild(timeElapsed);
			
			standards = doc.createElement("standard-compounds");
			solvedRelationship.appendChild(standards);
			
			Element standard = doc.createElement("standard-compound");
			standards.appendChild(standard);
			
			String[] injectionTimes = solvermap.get("InjectionTimes").split("\\$");
			measuredRetentionTimes = solvermap.get("ExperimentalRetentionTimes").split("\\$");
			predictedRetentionTimes = solvermap.get("CalculatedRetentionTimes").split("\\$");
			retentionErrors = solvermap.get("Errors").split("\\$");
			
			for(int i = 0; i < injectionTimes.length; i++){
				Element injection = doc.createElement("injection");
				injection.setAttribute("number", String.valueOf(i+1));
				standard.appendChild(injection);
				
				Element injectionTime = doc.createElement("injection-time");
				injectionTime.setTextContent(injectionTimes[i]);
				injection.appendChild(injectionTime);
				
				Element measuredTime = doc.createElement("measured-retention-time");
				measuredTime.setTextContent(measuredRetentionTimes[i]);
				injection.appendChild(measuredTime);
				
				Element predictedTime = doc.createElement("predicted-retention-time");
				predictedTime.setTextContent(predictedRetentionTimes[i]);
				injection.appendChild(predictedTime);
				
				Element error = doc.createElement("retention-error");
				error.setTextContent(retentionErrors[i]);
				injection.appendChild(error);
			}
		
		}
		
		//Add profiles tag
		Element profiles = doc.createElement("profiles");
		root.appendChild(profiles);
		
		//Create first profile - Simple Gradient
		String nodeValue = Utilities.getInterpolatedSimpleGradient(backcalculateController);
		Element profile1 = doc.createElement("profile");
		profile1.setAttribute("type", "Back-calculated Gradient");
		profile1.appendChild(doc.createTextNode(nodeValue));
		profiles.appendChild(profile1);
		
		//Create first profile - Simple Gradient
		nodeValue = Utilities.getInterpolatedDeadTime(backcalculateController);
		Element profile2 = doc.createElement("profile");
		profile2.setAttribute("type", "Dead Time");
		profile2.appendChild(doc.createTextNode(nodeValue));
		profiles.appendChild(profile2);
		
		
		
		//Write the contents into a XML file
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outputFile);
		try {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
		
	}
}
