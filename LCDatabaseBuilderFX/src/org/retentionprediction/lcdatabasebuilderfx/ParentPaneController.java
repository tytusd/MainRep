package org.retentionprediction.lcdatabasebuilderfx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

public class ParentPaneController implements Initializable {

	private ParentPaneControllerListener parentPaneControllerListener;
	
    @FXML
    private TextField textRetentionTimeD;

    @FXML
    private TextField textRetentionTimeE;

    @FXML
    private AnchorPane gradientAanchor;

    @FXML
    private AnchorPane gradientHanchor;

    @FXML
    private TextField textRetentionTimeB;

    @FXML
    private TextField textRetentionTimeC;

    @FXML
    private Label labelH;

    @FXML
    private TextField textRetentionTimeA;

    @FXML
    private Label backCalculateGLabel;

    @FXML
    private TextField textNISTID;

    @FXML
    private Tab gradientBtab;

    @FXML
    private TableColumn<?, ?> columnMeasuredRetentionTime;

    @FXML
    private Label labelGradientAStatus;

    @FXML
    private TextField textRetentionTimeH;

    @FXML
    private Label enterTimesBLabel;

    @FXML
    private Label enterTimesGLabel;

    @FXML
    private ProgressBar progressOverall;

    @FXML
    private Label checkSystemSuitabilityGLabel;

    @FXML
    private TextField textRetentionTimeF;

    @FXML
    private Label labelGradientEStatus;

    @FXML
    private TextField textRetentionTimeG;

    @FXML
    private Label labelS;

    @FXML
    private Label gradientDLabel;

    @FXML
    private Label backCalculateHLabel;

    @FXML
    private TextField textCAS;

    @FXML
    private AnchorPane gradientEanchor;

    @FXML
    private Tab gradientHTab;

    @FXML
    private Label labelGradientHStatus;

    @FXML
    private Label gradientHLabel;

    @FXML
    private Label labelTimeElapsed;

    @FXML
    private Label checkSystemSuitabilityFLabel;

    @FXML
    private Label enterTimesFLabel;

    @FXML
    private TableColumn<?, ?> columnError;

    @FXML
    private Label backCalculateALabel;

    @FXML
    private Label finalFitLabel;

    @FXML
    private TableColumn<?, ?> columnProgram;

    @FXML
    private GridPane roadMapGrid;

    @FXML
    private TableColumn<?, ?> columnPredictedRetentionTime;

    @FXML
    private Tab gradientEtab;

    @FXML
    private TitledPane paneSolveForParameters;

    @FXML
    private Label labelStatus;

    @FXML
    private Tab gradientAtab;

    @FXML
    private Label labelIteration;

    @FXML
    private AnchorPane gradientBanchor;

    @FXML
    private Label backCalculateELabel;

    @FXML
    private Label enterTimesHLabel;

    @FXML
    private TextField textCompoundName;

    @FXML
    private Label enterTimesELabel;

    @FXML
    private Label backCalculateBLabel;

    @FXML
    private Label checkSystemSuitabilityALabel;

    @FXML
    private Label labelGradientBStatus;

    @FXML
    private Label labelCp;

    @FXML
    private Label gradientCLabel;

    @FXML
    private Label gradientFLabel;

    @FXML
    private Label backCalculateDLabel;

    @FXML
    private TextField textPubChemID;

    @FXML
    private Tab gradientDtab;

    @FXML
    private AnchorPane gradientFanchor;

    @FXML
    private Label enterTimesDLabel;

    @FXML
    private Label checkSystemSuitabilityDLabel;

    @FXML
    private Tab gradientGtab;

    @FXML
    private Label gradientALabel;

    @FXML
    private Label labelGradientFStatus;

    @FXML
    private Label gradientELabel;

    @FXML
    private TextField textHMDB;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private AnchorPane gradientCanchor;

    @FXML
    private Button buttonSolve;

    @FXML
    private Label enterTimesCLabel;

    @FXML
    private Label checkSystemSuitabilityCLabel;

    @FXML
    private Label labelGradientCStatus;

    @FXML
    private Label labelVariance;

    @FXML
    private Label checkSystemSuitabilityHLabel;

    @FXML
    private Label checkSystemSuitabilityELabel;

    @FXML
    private AnchorPane gradientGanchor;

    @FXML
    private Label backCalculateCLabel;

    @FXML
    private Label labelGradientDStatus;

    @FXML
    private Label gradientBLabel;

    @FXML
    private Tab gradientCtab;

    @FXML
    private Label checkSystemSuitabilityBLabel;

    @FXML
    private TableView<?> tableRetentionTimes;

    @FXML
    private Label gradientGLabel;

    @FXML
    private Tab gradientFtab;

    @FXML
    private TextField textFormula;

    @FXML
    private Label backCalculateFLabel;

    @FXML
    private Label enterTimesALabel;

    @FXML
    private Label labelGradientGStatus;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane gradientDanchor;

    @FXML
    private Tab finalfittab;

    @FXML
    void onNewAction(ActionEvent event) {

    }

    @FXML
    void onOpenAction(ActionEvent event) {

    }

    @FXML
    void onSaveAction(ActionEvent event) {

    }

    @FXML
    void onSaveAsAction(ActionEvent event) {

    }

    @FXML
    void onCloseAction(ActionEvent event) {

    }

    @FXML
    void onAboutAction(ActionEvent event) {

    }

    @FXML
    void tabSelectionChanged(ActionEvent event) {

    }

    @FXML
    void onCommitRetentionTime(ActionEvent event) {

    }

    @FXML
    void onSolveForRetentionParameters(ActionEvent event) {

    }
    
    @FXML
    public void actionPerformValidation(ActionEvent event){
    	
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		
	}

	public ParentPaneControllerListener getParentPaneControllerListener() {
		return parentPaneControllerListener;
	}

	public void setParentPaneControllerListener(
			ParentPaneControllerListener parentPaneControllerListener) {
		this.parentPaneControllerListener = parentPaneControllerListener;
	}

}
