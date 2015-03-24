package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;
import org.retentionprediction.lcdatabasebuilderfx.ui.StepFourPaneController.StepFourPaneControllerListener;

import boswell.graphcontrolfx.GraphControlFX;

public class BackcalculateController implements Initializable, StepFourPaneControllerListener{

	public BackCalculateControllerListener backcalculateListener;
	private TitledPane step3Pane;
	private StepThreePaneController stepThreePaneController;
	private TitledPane step4Pane;
	private StepFourPaneController stepFourPaneController;
	private GraphControlFX eluentCompositionTimeGraph;
	private GraphControlFX deadTimeEluentCompositionGraph;
	private final double rem = javafx.scene.text.Font.getDefault().getSize();
	
	@FXML AnchorPane anchorPaneEluentCompositionTime;
	@FXML AnchorPane anchorPaneDeadTimeEluentComposition;
	
	// To be acquired from the Step3Pane.
	private ObservableList<StandardCompound> standardsList = FXCollections.observableArrayList();
		
	public BackCalculateControllerListener getBackcalculateListener() {
		return backcalculateListener;
	}

	public void setBackcalculateListener(
			BackCalculateControllerListener backcalculateListener) {
		this.backcalculateListener = backcalculateListener;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		FXMLLoader fxmlLoader = new FXMLLoader();
		try {
			step3Pane = (TitledPane)fxmlLoader.load(getClass().getResource("Step3Pane.fxml").openStream());
			stepThreePaneController = fxmlLoader.getController();
			stepThreePaneController.setStandardCompoundList(standardsList);
			
			// Add an event handler for when the back calculate button is pressed.
			stepThreePaneController.backCalculateOnMouseClickedProperty().set(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event) {
							//onBackCalculateButtonMouseClicked(event);
						}
					});
			FXMLLoader fxmlLoaderStep4Pane = new FXMLLoader();
			step4Pane = (TitledPane)fxmlLoaderStep4Pane.load(getClass().getResource("Step4Pane.fxml").openStream());
			stepFourPaneController = fxmlLoaderStep4Pane.getController();

			// Add an event handler for when the "Find Retention Times Automatically..." button is pressed.
			stepFourPaneController.buttonFindRetentionTimesOnMouseClickedProperty().set(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event) {
							//onFindRetentionTimesAutomaticallyButtonMouseClicked(event);
						}
					});

			stepFourPaneController.setStep4PaneControllerListener((StepFourPaneControllerListener)this);

			//TODO: GraphControlFX
			eluentCompositionTimeGraph = new GraphControlFX();
			eluentCompositionTimeGraph.setControlsEnabled(false);
			eluentCompositionTimeGraph.setYAxisTitle("Eluent Composition (%B)");
			eluentCompositionTimeGraph.setYAxisBaseUnit("\u00B0C", "\u00B0C");
			eluentCompositionTimeGraph.setYAxisScientificNotation(true);
			eluentCompositionTimeGraph.setYAxisRangeIndicatorsVisible(true);
			eluentCompositionTimeGraph.setAutoScaleY(true);
			
			eluentCompositionTimeGraph.setXAxisType(true);
			eluentCompositionTimeGraph.setXAxisRangeIndicatorsVisible(true);
			eluentCompositionTimeGraph.setAutoScaleX(true);
			eluentCompositionTimeGraph.setSelectionCursorVisible(false);
			anchorPaneEluentCompositionTime.getChildren().add(eluentCompositionTimeGraph);
			AnchorPane.setTopAnchor(eluentCompositionTimeGraph, 0.0);
			AnchorPane.setBottomAnchor(eluentCompositionTimeGraph, 0.0);
			AnchorPane.setLeftAnchor(eluentCompositionTimeGraph, 0.0);
			AnchorPane.setRightAnchor(eluentCompositionTimeGraph, 0.0);
			
			eluentCompositionTimeGraph.widthProperty().bind(anchorPaneEluentCompositionTime.widthProperty().subtract(rem));
			eluentCompositionTimeGraph.heightProperty().bind(anchorPaneEluentCompositionTime.heightProperty().subtract(rem));

			deadTimeEluentCompositionGraph = new GraphControlFX();
			deadTimeEluentCompositionGraph = new GraphControlFX();
			deadTimeEluentCompositionGraph.setControlsEnabled(false);
			deadTimeEluentCompositionGraph.setYAxisTitle("Uracil Dead Time (s)");
			deadTimeEluentCompositionGraph.setYAxisBaseUnit("seconds", "s");
			//holdUpProfileGraph.setYAxisRangeLimits(-1E15d, 1E15d);
			deadTimeEluentCompositionGraph.setYAxisScientificNotation(true);
			deadTimeEluentCompositionGraph.setYAxisRangeIndicatorsVisible(false);
			deadTimeEluentCompositionGraph.setAutoScaleY(true);
	        
			deadTimeEluentCompositionGraph.setXAxisType(false);
			deadTimeEluentCompositionGraph.setXAxisRangeIndicatorsVisible(false);
			deadTimeEluentCompositionGraph.setXAxisTitle("Eluent Composition (%B)");
			deadTimeEluentCompositionGraph.setXAxisBaseUnit("\u00B0C", "\u00B0C");
			deadTimeEluentCompositionGraph.setAutoScaleX(true);
			deadTimeEluentCompositionGraph.setSelectionCursorVisible(false);
			
			anchorPaneDeadTimeEluentComposition.getChildren().add(deadTimeEluentCompositionGraph);
			AnchorPane.setTopAnchor(deadTimeEluentCompositionGraph, 0.0);
			AnchorPane.setBottomAnchor(deadTimeEluentCompositionGraph, 0.0);
			AnchorPane.setLeftAnchor(deadTimeEluentCompositionGraph, 0.0);
			AnchorPane.setRightAnchor(deadTimeEluentCompositionGraph, 0.0);
			
			deadTimeEluentCompositionGraph.widthProperty().bind(anchorPaneDeadTimeEluentComposition.widthProperty().subtract(rem));
			deadTimeEluentCompositionGraph.heightProperty().bind(anchorPaneDeadTimeEluentComposition.heightProperty().subtract(rem));
			
			//resetValues();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public interface BackCalculateControllerListener 
	{
		public void onNextStepPressed(BackcalculateController thisController);
		public void onPreviousStepPressed(BackcalculateController thisController);
    }

	@Override
	public void onTableUpdate() {
		// TODO Auto-generated method stub
		
	}

}
