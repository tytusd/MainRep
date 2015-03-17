package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.net.URL;
import java.util.ResourceBundle;

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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;
import org.retentionprediction.lcdatabasebuilderfx.ui.MeasuredRetentionTimesController.MeasuredRetentionTimesControllerListener;

public class ParentPaneController implements Initializable, MeasuredRetentionTimesControllerListener {

	private ParentPaneControllerListener parentPaneControllerListener;
	
	@FXML private AnchorPane gradientAanchor;
	@FXML private AnchorPane gradientBanchor;
	@FXML private AnchorPane gradientCanchor;
	@FXML private AnchorPane gradientDanchor;
	@FXML private AnchorPane gradientEanchor;
	@FXML private AnchorPane gradientFanchor;
	@FXML private AnchorPane gradientGanchor;
	@FXML private AnchorPane gradientHanchor;

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
    @FXML private Label labelH;

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
    
    @FXML private TextField textNISTID;
    @FXML private TableColumn<StandardCompound, String> columnMeasuredRetentionTime;
    @FXML private ProgressBar progressOverall;

    @FXML private Label labelS;
    @FXML private TextField textCAS;
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
    @FXML private Label labelCp;
    @FXML private TextField textPubChemID;

    @FXML private TextField textHMDB;
    @FXML private ProgressBar progressBar;
    @FXML private Button buttonSolve;
    @FXML private Label labelVariance;
    @FXML private TableView<StandardCompound> tableRetentionTimes;
    @FXML private TextField textFormula;
    @FXML private TabPane tabPane;
    @FXML private Tab finalfittab;
    @FXML private Pane drawPane;
    
    private ScrollPane[] measuredRetentionTimes = new ScrollPane[8];
    private ScrollPane[] backCalculatePane = new ScrollPane[8];
    private BackcalculateController[] backcalculateController = new BackcalculateController[8];
    private MeasuredRetentionTimesController[] measuredRetentionTimesController = new MeasuredRetentionTimesController[8];
    private int[] iCurrentStep = new int[8];
    private ObservableList<StandardCompound> programList = FXCollections.observableArrayList();
    private boolean finalFitComplete = false;
    
    @Override
	public void initialize(URL location, ResourceBundle resources) {
		try{
			for(int i = 0; i < iCurrentStep.length; i++){
				FXMLLoader fxmlLoader = new FXMLLoader();
				measuredRetentionTimes[i] = fxmlLoader.load(getClass().getResource("MeasuredRetentionTimes.fxml").openStream());
				measuredRetentionTimesController[i] = fxmlLoader.getController();
				measuredRetentionTimesController[i].setMeasuredRetentionTimesControllerListener(this);
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
	}
    
    /**
     * Draws line from one label to another (This is a part of progress feature available on top of the main screen).
     * @param startBinderLabel
     * @param endBinderLabel
     * @return
     */
    public Line drawLine(Label startBinderLabel, Label endBinderLabel){
    	Line line = new Line();
    	line.startXProperty().bind(startBinderLabel.layoutXProperty().add(startBinderLabel.widthProperty().add(12.0)));
		line.startYProperty().bind(startBinderLabel.layoutYProperty().add(startBinderLabel.heightProperty().divide(2.0)));
		line.endXProperty().bind(endBinderLabel.layoutXProperty().subtract(12.0));
		line.endYProperty().bind(endBinderLabel.layoutYProperty().add(endBinderLabel.heightProperty().divide(2.0)));
		return line;
    }
    
    /**
     * Draws an arrow at the end of a line.
     * @param line
     * @return
     */
    public Polygon drawPolygon(Line line){
    	Polygon polygon = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
		polygon.layoutXProperty().bind(line.endXProperty());
		polygon.layoutYProperty().bind(line.endYProperty());
		return polygon;
    }
    
    public Line drawLineForFinalFit(Label startLabel){
    	Line line = new Line();
		line.startXProperty().bind(startLabel.layoutXProperty().add(startLabel.widthProperty().add(12.0)));
		line.startYProperty().bind(startLabel.layoutYProperty().add(startLabel.heightProperty().divide(2.0)));
		line.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line.startXProperty()).multiply(2.0/3.0).add(line.startXProperty()));
		line.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));
		return line;
    }
    

    
    
    @FXML void onNewAction(ActionEvent event) {

    }

    @FXML void onOpenAction(ActionEvent event) {

    }

    @FXML void onSaveAction(ActionEvent event) {

    }

    @FXML void onSaveAsAction(ActionEvent event) {

    }

    @FXML void onCloseAction(ActionEvent event) {

    }

    @FXML void onAboutAction(ActionEvent event) {

    }

    @FXML void tabSelectionChanged(Event event) {
    	updateRoadMap();
		performValidations();
		updateFinalFitProgress();
    }

    @FXML void onCommitRetentionTime(ActionEvent event) {
    	
    }

    @FXML void onSolveForRetentionParameters(ActionEvent event) {

    }
    
    @FXML void actionPerformValidation(ActionEvent event){
    	performValidations();
    }

	

	public ParentPaneControllerListener getParentPaneControllerListener() {
		return parentPaneControllerListener;
	}

	public void setParentPaneControllerListener(
			ParentPaneControllerListener parentPaneControllerListener) {
		this.parentPaneControllerListener = parentPaneControllerListener;
	}

	@Override
	public void onNextStepPressed(
			MeasuredRetentionTimesController thisController) {
		// TODO Auto-generated method stub
		
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
		
		if (finalFitComplete)
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
	
	public void updateFinalFitProgress(){
		//TODO: IMPLEMENT THIS
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
	
	/**
	 * This method sets up the progress feature of the program where all the required lines are drawn to show users
	 * which steps they need to finish.
	 */
    public void setupAllLinesInDrawPane(){
    	Line line1A = drawLine(gradientALabel, enterTimesALabel);
		Line line1B = drawLine(gradientBLabel, enterTimesBLabel);
		Line line1C = drawLine(gradientCLabel, enterTimesCLabel);
		Line line1D = drawLine(gradientDLabel, enterTimesDLabel);
		Line line1E = drawLine(gradientELabel, enterTimesELabel);
		Line line1F = drawLine(gradientFLabel, enterTimesFLabel);
		Line line1G = drawLine(gradientGLabel, enterTimesGLabel);
		Line line1H = drawLine(gradientHLabel, enterTimesHLabel);
		Line line2A = drawLine(enterTimesALabel, backCalculateALabel);
		Line line2B = drawLine(enterTimesBLabel, backCalculateBLabel);
		Line line2C = drawLine(enterTimesCLabel, backCalculateCLabel);
		Line line2D = drawLine(enterTimesDLabel, backCalculateDLabel);
		Line line2E = drawLine(enterTimesELabel, backCalculateELabel);
		Line line2F = drawLine(enterTimesFLabel, backCalculateFLabel);
		Line line2G = drawLine(enterTimesGLabel, backCalculateGLabel);
		Line line2H = drawLine(enterTimesHLabel, backCalculateHLabel);
		Line line3A = drawLine(backCalculateALabel, checkSystemSuitabilityALabel);
		Line line3B = drawLine(backCalculateBLabel, checkSystemSuitabilityBLabel);
		Line line3C = drawLine(backCalculateCLabel, checkSystemSuitabilityCLabel);
		Line line3D = drawLine(backCalculateDLabel, checkSystemSuitabilityDLabel);
		Line line3E = drawLine(backCalculateELabel, checkSystemSuitabilityELabel);
		Line line3F = drawLine(backCalculateFLabel, checkSystemSuitabilityFLabel);
		Line line3G = drawLine(backCalculateGLabel, checkSystemSuitabilityGLabel);
		Line line3H = drawLine(backCalculateHLabel, checkSystemSuitabilityHLabel);
		Line line4A = drawLineForFinalFit(checkSystemSuitabilityALabel);
		Line line4B = drawLineForFinalFit(checkSystemSuitabilityBLabel);
		Line line4C = drawLineForFinalFit(checkSystemSuitabilityCLabel);
		Line line4D = drawLineForFinalFit(checkSystemSuitabilityDLabel);
		Line line4E = drawLineForFinalFit(checkSystemSuitabilityELabel);
		Line line4F = drawLineForFinalFit(checkSystemSuitabilityFLabel);
		Line line4G = drawLineForFinalFit(checkSystemSuitabilityGLabel);
		Line line4H = drawLineForFinalFit(checkSystemSuitabilityHLabel);
		
		Polygon polygon1A = drawPolygon(line1A);
		Polygon polygon1B = drawPolygon(line1B);
		Polygon polygon1C = drawPolygon(line1C);
		Polygon polygon1D = drawPolygon(line1D);
		Polygon polygon1E = drawPolygon(line1E);
		Polygon polygon1F = drawPolygon(line1F);
		Polygon polygon1G = drawPolygon(line1G);
		Polygon polygon1H = drawPolygon(line1H);
		Polygon polygon2A = drawPolygon(line2A);
		Polygon polygon2B = drawPolygon(line2B);
		Polygon polygon2C = drawPolygon(line2C);
		Polygon polygon2D = drawPolygon(line2D);
		Polygon polygon2E = drawPolygon(line2E);
		Polygon polygon2F = drawPolygon(line2F);
		Polygon polygon2G = drawPolygon(line2G);
		Polygon polygon2H = drawPolygon(line2H);
		Polygon polygon3A = drawPolygon(line3A);
		Polygon polygon3B = drawPolygon(line3B);
		Polygon polygon3C = drawPolygon(line3C);
		Polygon polygon3D = drawPolygon(line3D);
		Polygon polygon3E = drawPolygon(line3E);
		Polygon polygon3F = drawPolygon(line3F);
		Polygon polygon3G = drawPolygon(line3G);
		Polygon polygon3H = drawPolygon(line3H);
		
		drawPane.getChildren().add(line1A);
		drawPane.getChildren().add(polygon1A);
		drawPane.getChildren().add(line1B);
		drawPane.getChildren().add(polygon1B);
		drawPane.getChildren().add(line1C);
		drawPane.getChildren().add(polygon1C);
		drawPane.getChildren().add(line1D);
		drawPane.getChildren().add(polygon1D);
		drawPane.getChildren().add(line1E);
		drawPane.getChildren().add(polygon1E);
		drawPane.getChildren().add(line1F);
		drawPane.getChildren().add(polygon1F);
		drawPane.getChildren().add(line1G);
		drawPane.getChildren().add(polygon1G);
		drawPane.getChildren().add(line1H);
		drawPane.getChildren().add(polygon1H);
		
		drawPane.getChildren().add(line2A);
		drawPane.getChildren().add(polygon2A);
		drawPane.getChildren().add(line2B);
		drawPane.getChildren().add(polygon2B);
		drawPane.getChildren().add(line2C);
		drawPane.getChildren().add(polygon2C);
		drawPane.getChildren().add(line2D);
		drawPane.getChildren().add(polygon2D);
		drawPane.getChildren().add(line2E);
		drawPane.getChildren().add(polygon2E);
		drawPane.getChildren().add(line2F);
		drawPane.getChildren().add(polygon2F);
		drawPane.getChildren().add(line2G);
		drawPane.getChildren().add(polygon2G);
		drawPane.getChildren().add(line2H);
		drawPane.getChildren().add(polygon2H);
		
		drawPane.getChildren().add(line3A);
		drawPane.getChildren().add(polygon3A);
		drawPane.getChildren().add(line3B);
		drawPane.getChildren().add(polygon3B);
		drawPane.getChildren().add(line3C);
		drawPane.getChildren().add(polygon3C);
		drawPane.getChildren().add(line3D);
		drawPane.getChildren().add(polygon3D);
		drawPane.getChildren().add(line3E);
		drawPane.getChildren().add(polygon3E);
		drawPane.getChildren().add(line3F);
		drawPane.getChildren().add(polygon3F);
		drawPane.getChildren().add(line3G);
		drawPane.getChildren().add(polygon3G);
		drawPane.getChildren().add(line3H);
		drawPane.getChildren().add(polygon3H);
		
		drawPane.getChildren().add(line4A);
		drawPane.getChildren().add(line4B);
		drawPane.getChildren().add(line4C);
		drawPane.getChildren().add(line4D);
		drawPane.getChildren().add(line4E);
		drawPane.getChildren().add(line4F);
		drawPane.getChildren().add(line4G);
		drawPane.getChildren().add(line4H);

		Line finalLine = new Line();
		finalLine.startXProperty().bind(line4A.endXProperty());
		finalLine.startYProperty().bind(line4A.endYProperty());
		finalLine.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(12.0));
		finalLine.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));
		
		Polygon finalPolygon = drawPolygon(finalLine);

		drawPane.getChildren().add(finalLine);
		drawPane.getChildren().add(finalPolygon);
    }
}
