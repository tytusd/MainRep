package org.retentionprediction.gcdatabasebuilderfx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.retentionprediction.gcdatabasebuilderfx.BackcalculateController.BackCalculateControllerListener;
import org.retentionprediction.gcdatabasebuilderfx.MeasuredRetentionTimesController.MeasuredRetentionTimesControllerListener;
import org.retentionprediction.gcdatabasebuilderfx.SolveParametersTask.SolveParametersListener;
import boswell.fxoptionpane.FXOptionPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ParentPaneController implements Initializable, MeasuredRetentionTimesControllerListener, BackCalculateControllerListener, SolveParametersListener
{
	@FXML private Tab programAtab;
	@FXML private Tab programBtab;
	@FXML private Tab programCtab;
	@FXML private Tab programDtab;
	@FXML private Tab programEtab;
	@FXML private Tab programFtab;
	@FXML private Tab finalfittab;

	@FXML private AnchorPane programAanchor;
	@FXML private AnchorPane programBanchor;
	@FXML private AnchorPane programCanchor;
	@FXML private AnchorPane programDanchor;
	@FXML private AnchorPane programEanchor;
	@FXML private AnchorPane programFanchor;
	
	@FXML private Label programALabel;
	@FXML private Label programBLabel;
	@FXML private Label programCLabel;
	@FXML private Label programDLabel;
	@FXML private Label programELabel;
	@FXML private Label programFLabel;

	@FXML private Label enterTimesALabel;
	@FXML private Label enterTimesBLabel;
	@FXML private Label enterTimesCLabel;
	@FXML private Label enterTimesDLabel;
	@FXML private Label enterTimesELabel;
	@FXML private Label enterTimesFLabel;
	
	@FXML private Label backCalculateALabel;
	@FXML private Label backCalculateBLabel;
	@FXML private Label backCalculateCLabel;
	@FXML private Label backCalculateDLabel;
	@FXML private Label backCalculateELabel;
	@FXML private Label backCalculateFLabel;

	@FXML private Label checkSystemSuitabilityALabel;
	@FXML private Label checkSystemSuitabilityBLabel;
	@FXML private Label checkSystemSuitabilityCLabel;
	@FXML private Label checkSystemSuitabilityDLabel;
	@FXML private Label checkSystemSuitabilityELabel;
	@FXML private Label checkSystemSuitabilityFLabel;
	
	@FXML private Label labelProgramAStatus;
	@FXML private Label labelProgramBStatus;
	@FXML private Label labelProgramCStatus;
	@FXML private Label labelProgramDStatus;
	@FXML private Label labelProgramEStatus;
	@FXML private Label labelProgramFStatus;
	
	@FXML private TextField textCompoundName;
	@FXML private TextField textFormula;
	@FXML private TextField textPubChemID;
	@FXML private TextField textCAS;
	@FXML private TextField textNISTID;
	@FXML private TextField textHMDB;
	
	@FXML private ProgressBar progressOverall;

	@FXML private Label finalFitLabel;

	@FXML private GridPane roadMapGrid;
	@FXML private Pane drawPane;
	
	@FXML private TabPane tabPane;
	
	@FXML private TableView<StandardCompound> tableRetentionTimes;
	@FXML private TableColumn<StandardCompound, String> columnProgram;
	@FXML private TableColumn<StandardCompound, String> columnMeasuredRetentionTime;
	@FXML private TableColumn<StandardCompound, String> columnPredictedRetentionTime;
	@FXML private TableColumn<StandardCompound, String> columnError;
	@FXML private Label labelH;
	@FXML private Label labelS;
	@FXML private Label labelCp;
	@FXML private Label labelIteration;
	@FXML private Label labelVariance;
	@FXML private Label labelTimeElapsed;
	@FXML private Label labelStatus;
	@FXML private ProgressBar progressBar;
	@FXML private TitledPane paneSolveForParameters;
	@FXML private TextField textRetentionTimeA;
	@FXML private TextField textRetentionTimeB;
	@FXML private TextField textRetentionTimeC;
	@FXML private TextField textRetentionTimeD;
	@FXML private TextField textRetentionTimeE;
	@FXML private TextField textRetentionTimeF;
	@FXML private Button buttonSolve;
	
	Stage primaryStage = null;
	
	private boolean finalFitComplete = false;
	private int[] iCurrentStep = new int[6];
	
	private ScrollPane[] measuredRetentionTimes = new ScrollPane[6];;
	private ScrollPane[] backCalculatePane = new ScrollPane[6];
	
	private MeasuredRetentionTimesController[] measuredRetentionTimesController = new MeasuredRetentionTimesController[6];
	private BackcalculateController[] backCalculateController = new BackcalculateController[6];

    private ObservableList<StandardCompound> programList = FXCollections.observableArrayList();
    
    private SolveParametersTask solveParametersTask;

    private ParentPaneControllerListener parentPaneControllerListener;

	public interface ParentPaneControllerListener 
	{
		public void onNew();
		public void onOpen();
		public void onSave();
		public void onSaveAs();
		public void onClose();
		public void onAbout();
    }

	public void setParentPaneControllerListener(ParentPaneControllerListener thisListener)
	{
		parentPaneControllerListener = thisListener;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		// Set the current step to the first one in all temp programs
		for (int i = 0; i < iCurrentStep.length; i++)
		{
			iCurrentStep[i] = 0;
		}
		
		// Load the MeasuredRetentionTimes.fxml layout into each one
		try {
			
			// Load 6 copies of the back-calculation pane and also get the controlling class
			for (int i = 0; i < 6; i++)
			{
				FXMLLoader fxmlLoader = new FXMLLoader();
				backCalculatePane[i] = (ScrollPane)fxmlLoader.load(getClass().getResource("Backcalculate.fxml").openStream());
				backCalculateController[i] = fxmlLoader.getController();
				backCalculateController[i].setBackCalculateControllerListener(this);
			}
			
			// Load 6 copies of the measure retention times pane and also get the controlling class
			for (int i = 0; i < 6; i++)
			{
				FXMLLoader fxmlLoader = new FXMLLoader();
				measuredRetentionTimes[i] = (ScrollPane)fxmlLoader.load(getClass().getResource("MeasuredRetentionTimes.fxml").openStream());
				measuredRetentionTimesController[i] = fxmlLoader.getController();
				measuredRetentionTimesController[i].setMeasuredRetentionTimesControllerListener(this);
			}
			
			// Show the measure retention times pane
			programAtab.setContent(measuredRetentionTimes[0]);
			programBtab.setContent(measuredRetentionTimes[1]);
			programCtab.setContent(measuredRetentionTimes[2]);
			programDtab.setContent(measuredRetentionTimes[3]);
			programEtab.setContent(measuredRetentionTimes[4]);
			programFtab.setContent(measuredRetentionTimes[5]);
			
			// Draw the arrows between the labels. Bind them so that they move with the labels upon resizing the window.
			Line line1A = new Line();
			line1A.startXProperty().bind(programALabel.layoutXProperty().add(programALabel.widthProperty().add(12.0)));
			line1A.startYProperty().bind(programALabel.layoutYProperty().add(programALabel.heightProperty().divide(2.0)));
			line1A.endXProperty().bind(enterTimesALabel.layoutXProperty().subtract(12.0));
			line1A.endYProperty().bind(enterTimesALabel.layoutYProperty().add(enterTimesALabel.heightProperty().divide(2.0)));
			Polygon polygon1A = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon1A.layoutXProperty().bind(line1A.endXProperty());
			polygon1A.layoutYProperty().bind(line1A.endYProperty());
			
			Line line1B = new Line();
			line1B.startXProperty().bind(programBLabel.layoutXProperty().add(programBLabel.widthProperty().add(12.0)));
			line1B.startYProperty().bind(programBLabel.layoutYProperty().add(programBLabel.heightProperty().divide(2.0)));
			line1B.endXProperty().bind(enterTimesBLabel.layoutXProperty().subtract(12.0));
			line1B.endYProperty().bind(enterTimesBLabel.layoutYProperty().add(enterTimesBLabel.heightProperty().divide(2.0)));
			Polygon polygon1B = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon1B.layoutXProperty().bind(line1B.endXProperty());
			polygon1B.layoutYProperty().bind(line1B.endYProperty());

			Line line1C = new Line();
			line1C.startXProperty().bind(programCLabel.layoutXProperty().add(programCLabel.widthProperty().add(12.0)));
			line1C.startYProperty().bind(programCLabel.layoutYProperty().add(programCLabel.heightProperty().divide(2.0)));
			line1C.endXProperty().bind(enterTimesCLabel.layoutXProperty().subtract(12.0));
			line1C.endYProperty().bind(enterTimesCLabel.layoutYProperty().add(enterTimesCLabel.heightProperty().divide(2.0)));
			Polygon polygon1C = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon1C.layoutXProperty().bind(line1C.endXProperty());
			polygon1C.layoutYProperty().bind(line1C.endYProperty());

			Line line1D = new Line();
			line1D.startXProperty().bind(programDLabel.layoutXProperty().add(programDLabel.widthProperty().add(12.0)));
			line1D.startYProperty().bind(programDLabel.layoutYProperty().add(programDLabel.heightProperty().divide(2.0)));
			line1D.endXProperty().bind(enterTimesDLabel.layoutXProperty().subtract(12.0));
			line1D.endYProperty().bind(enterTimesDLabel.layoutYProperty().add(enterTimesDLabel.heightProperty().divide(2.0)));
			Polygon polygon1D = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon1D.layoutXProperty().bind(line1D.endXProperty());
			polygon1D.layoutYProperty().bind(line1D.endYProperty());

			Line line1E = new Line();
			line1E.startXProperty().bind(programELabel.layoutXProperty().add(programELabel.widthProperty().add(12.0)));
			line1E.startYProperty().bind(programELabel.layoutYProperty().add(programELabel.heightProperty().divide(2.0)));
			line1E.endXProperty().bind(enterTimesELabel.layoutXProperty().subtract(12.0));
			line1E.endYProperty().bind(enterTimesELabel.layoutYProperty().add(enterTimesELabel.heightProperty().divide(2.0)));
			Polygon polygon1E = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon1E.layoutXProperty().bind(line1E.endXProperty());
			polygon1E.layoutYProperty().bind(line1E.endYProperty());

			Line line1F = new Line();
			line1F.startXProperty().bind(programFLabel.layoutXProperty().add(programFLabel.widthProperty().add(12.0)));
			line1F.startYProperty().bind(programFLabel.layoutYProperty().add(programFLabel.heightProperty().divide(2.0)));
			line1F.endXProperty().bind(enterTimesFLabel.layoutXProperty().subtract(12.0));
			line1F.endYProperty().bind(enterTimesFLabel.layoutYProperty().add(enterTimesFLabel.heightProperty().divide(2.0)));
			Polygon polygon1F = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon1F.layoutXProperty().bind(line1F.endXProperty());
			polygon1F.layoutYProperty().bind(line1F.endYProperty());

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
			
			Line line2A = new Line();
			line2A.startXProperty().bind(enterTimesALabel.layoutXProperty().add(enterTimesALabel.widthProperty().add(12.0)));
			line2A.startYProperty().bind(enterTimesALabel.layoutYProperty().add(enterTimesALabel.heightProperty().divide(2.0)));
			line2A.endXProperty().bind(backCalculateALabel.layoutXProperty().subtract(12.0));
			line2A.endYProperty().bind(backCalculateALabel.layoutYProperty().add(backCalculateALabel.heightProperty().divide(2.0)));
			Polygon polygon2A = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon2A.layoutXProperty().bind(line2A.endXProperty());
			polygon2A.layoutYProperty().bind(line2A.endYProperty());
			
			Line line2B = new Line();
			line2B.startXProperty().bind(enterTimesBLabel.layoutXProperty().add(enterTimesBLabel.widthProperty().add(12.0)));
			line2B.startYProperty().bind(enterTimesBLabel.layoutYProperty().add(enterTimesBLabel.heightProperty().divide(2.0)));
			line2B.endXProperty().bind(backCalculateBLabel.layoutXProperty().subtract(12.0));
			line2B.endYProperty().bind(backCalculateBLabel.layoutYProperty().add(backCalculateBLabel.heightProperty().divide(2.0)));
			Polygon polygon2B = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon2B.layoutXProperty().bind(line2B.endXProperty());
			polygon2B.layoutYProperty().bind(line2B.endYProperty());

			Line line2C = new Line();
			line2C.startXProperty().bind(enterTimesCLabel.layoutXProperty().add(enterTimesCLabel.widthProperty().add(12.0)));
			line2C.startYProperty().bind(enterTimesCLabel.layoutYProperty().add(enterTimesCLabel.heightProperty().divide(2.0)));
			line2C.endXProperty().bind(backCalculateCLabel.layoutXProperty().subtract(12.0));
			line2C.endYProperty().bind(backCalculateCLabel.layoutYProperty().add(backCalculateCLabel.heightProperty().divide(2.0)));
			Polygon polygon2C = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon2C.layoutXProperty().bind(line2C.endXProperty());
			polygon2C.layoutYProperty().bind(line2C.endYProperty());

			Line line2D = new Line();
			line2D.startXProperty().bind(enterTimesDLabel.layoutXProperty().add(enterTimesDLabel.widthProperty().add(12.0)));
			line2D.startYProperty().bind(enterTimesDLabel.layoutYProperty().add(enterTimesDLabel.heightProperty().divide(2.0)));
			line2D.endXProperty().bind(backCalculateDLabel.layoutXProperty().subtract(12.0));
			line2D.endYProperty().bind(backCalculateDLabel.layoutYProperty().add(backCalculateDLabel.heightProperty().divide(2.0)));
			Polygon polygon2D = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon2D.layoutXProperty().bind(line2D.endXProperty());
			polygon2D.layoutYProperty().bind(line2D.endYProperty());

			Line line2E = new Line();
			line2E.startXProperty().bind(enterTimesELabel.layoutXProperty().add(enterTimesELabel.widthProperty().add(12.0)));
			line2E.startYProperty().bind(enterTimesELabel.layoutYProperty().add(enterTimesELabel.heightProperty().divide(2.0)));
			line2E.endXProperty().bind(backCalculateELabel.layoutXProperty().subtract(12.0));
			line2E.endYProperty().bind(backCalculateELabel.layoutYProperty().add(backCalculateELabel.heightProperty().divide(2.0)));
			Polygon polygon2E = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon2E.layoutXProperty().bind(line2E.endXProperty());
			polygon2E.layoutYProperty().bind(line2E.endYProperty());

			Line line2F = new Line();
			line2F.startXProperty().bind(enterTimesFLabel.layoutXProperty().add(enterTimesFLabel.widthProperty().add(12.0)));
			line2F.startYProperty().bind(enterTimesFLabel.layoutYProperty().add(enterTimesFLabel.heightProperty().divide(2.0)));
			line2F.endXProperty().bind(backCalculateFLabel.layoutXProperty().subtract(12.0));
			line2F.endYProperty().bind(backCalculateFLabel.layoutYProperty().add(backCalculateFLabel.heightProperty().divide(2.0)));
			Polygon polygon2F = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon2F.layoutXProperty().bind(line2F.endXProperty());
			polygon2F.layoutYProperty().bind(line2F.endYProperty());

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
			
			Line line3A = new Line();
			line3A.startXProperty().bind(backCalculateALabel.layoutXProperty().add(backCalculateALabel.widthProperty().add(12.0)));
			line3A.startYProperty().bind(backCalculateALabel.layoutYProperty().add(backCalculateALabel.heightProperty().divide(2.0)));
			line3A.endXProperty().bind(checkSystemSuitabilityALabel.layoutXProperty().subtract(12.0));
			line3A.endYProperty().bind(checkSystemSuitabilityALabel.layoutYProperty().add(checkSystemSuitabilityALabel.heightProperty().divide(2.0)));
			Polygon polygon3A = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon3A.layoutXProperty().bind(line3A.endXProperty());
			polygon3A.layoutYProperty().bind(line3A.endYProperty());
			
			Line line3B = new Line();
			line3B.startXProperty().bind(backCalculateBLabel.layoutXProperty().add(backCalculateBLabel.widthProperty().add(12.0)));
			line3B.startYProperty().bind(backCalculateBLabel.layoutYProperty().add(backCalculateBLabel.heightProperty().divide(2.0)));
			line3B.endXProperty().bind(checkSystemSuitabilityBLabel.layoutXProperty().subtract(12.0));
			line3B.endYProperty().bind(checkSystemSuitabilityBLabel.layoutYProperty().add(checkSystemSuitabilityBLabel.heightProperty().divide(2.0)));
			Polygon polygon3B = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon3B.layoutXProperty().bind(line3B.endXProperty());
			polygon3B.layoutYProperty().bind(line3B.endYProperty());

			Line line3C = new Line();
			line3C.startXProperty().bind(backCalculateCLabel.layoutXProperty().add(backCalculateCLabel.widthProperty().add(12.0)));
			line3C.startYProperty().bind(backCalculateCLabel.layoutYProperty().add(backCalculateCLabel.heightProperty().divide(2.0)));
			line3C.endXProperty().bind(checkSystemSuitabilityCLabel.layoutXProperty().subtract(12.0));
			line3C.endYProperty().bind(checkSystemSuitabilityCLabel.layoutYProperty().add(checkSystemSuitabilityCLabel.heightProperty().divide(2.0)));
			Polygon polygon3C = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon3C.layoutXProperty().bind(line3C.endXProperty());
			polygon3C.layoutYProperty().bind(line3C.endYProperty());

			Line line3D = new Line();
			line3D.startXProperty().bind(backCalculateDLabel.layoutXProperty().add(backCalculateDLabel.widthProperty().add(12.0)));
			line3D.startYProperty().bind(backCalculateDLabel.layoutYProperty().add(backCalculateDLabel.heightProperty().divide(2.0)));
			line3D.endXProperty().bind(checkSystemSuitabilityDLabel.layoutXProperty().subtract(12.0));
			line3D.endYProperty().bind(checkSystemSuitabilityDLabel.layoutYProperty().add(checkSystemSuitabilityDLabel.heightProperty().divide(2.0)));
			Polygon polygon3D = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon3D.layoutXProperty().bind(line3D.endXProperty());
			polygon3D.layoutYProperty().bind(line3D.endYProperty());

			Line line3E = new Line();
			line3E.startXProperty().bind(backCalculateELabel.layoutXProperty().add(backCalculateELabel.widthProperty().add(12.0)));
			line3E.startYProperty().bind(backCalculateELabel.layoutYProperty().add(backCalculateELabel.heightProperty().divide(2.0)));
			line3E.endXProperty().bind(checkSystemSuitabilityELabel.layoutXProperty().subtract(12.0));
			line3E.endYProperty().bind(checkSystemSuitabilityELabel.layoutYProperty().add(checkSystemSuitabilityELabel.heightProperty().divide(2.0)));
			Polygon polygon3E = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon3E.layoutXProperty().bind(line3E.endXProperty());
			polygon3E.layoutYProperty().bind(line3E.endYProperty());

			Line line3F = new Line();
			line3F.startXProperty().bind(backCalculateFLabel.layoutXProperty().add(backCalculateFLabel.widthProperty().add(12.0)));
			line3F.startYProperty().bind(backCalculateFLabel.layoutYProperty().add(backCalculateFLabel.heightProperty().divide(2.0)));
			line3F.endXProperty().bind(checkSystemSuitabilityFLabel.layoutXProperty().subtract(12.0));
			line3F.endYProperty().bind(checkSystemSuitabilityFLabel.layoutYProperty().add(checkSystemSuitabilityFLabel.heightProperty().divide(2.0)));
			Polygon polygon3F = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			polygon3F.layoutXProperty().bind(line3F.endXProperty());
			polygon3F.layoutYProperty().bind(line3F.endYProperty());

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

			Line line4A = new Line();
			line4A.startXProperty().bind(checkSystemSuitabilityALabel.layoutXProperty().add(checkSystemSuitabilityALabel.widthProperty().add(12.0)));
			line4A.startYProperty().bind(checkSystemSuitabilityALabel.layoutYProperty().add(checkSystemSuitabilityALabel.heightProperty().divide(2.0)));
			line4A.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line4A.startXProperty()).multiply(2.0/3.0).add(line4A.startXProperty()));
			line4A.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));

			Line line4B = new Line();
			line4B.startXProperty().bind(checkSystemSuitabilityBLabel.layoutXProperty().add(checkSystemSuitabilityBLabel.widthProperty().add(12.0)));
			line4B.startYProperty().bind(checkSystemSuitabilityBLabel.layoutYProperty().add(checkSystemSuitabilityBLabel.heightProperty().divide(2.0)));
			line4B.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line4A.startXProperty()).multiply(2.0/3.0).add(line4A.startXProperty()));
			line4B.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));

			Line line4C = new Line();
			line4C.startXProperty().bind(checkSystemSuitabilityCLabel.layoutXProperty().add(checkSystemSuitabilityCLabel.widthProperty().add(12.0)));
			line4C.startYProperty().bind(checkSystemSuitabilityCLabel.layoutYProperty().add(checkSystemSuitabilityCLabel.heightProperty().divide(2.0)));
			line4C.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line4A.startXProperty()).multiply(2.0/3.0).add(line4A.startXProperty()));
			line4C.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));

			Line line4D = new Line();
			line4D.startXProperty().bind(checkSystemSuitabilityDLabel.layoutXProperty().add(checkSystemSuitabilityDLabel.widthProperty().add(12.0)));
			line4D.startYProperty().bind(checkSystemSuitabilityDLabel.layoutYProperty().add(checkSystemSuitabilityDLabel.heightProperty().divide(2.0)));
			line4D.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line4A.startXProperty()).multiply(2.0/3.0).add(line4A.startXProperty()));
			line4D.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));

			Line line4E = new Line();
			line4E.startXProperty().bind(checkSystemSuitabilityELabel.layoutXProperty().add(checkSystemSuitabilityELabel.widthProperty().add(12.0)));
			line4E.startYProperty().bind(checkSystemSuitabilityELabel.layoutYProperty().add(checkSystemSuitabilityELabel.heightProperty().divide(2.0)));
			line4E.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line4A.startXProperty()).multiply(2.0/3.0).add(line4A.startXProperty()));
			line4E.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));

			Line line4F = new Line();
			line4F.startXProperty().bind(checkSystemSuitabilityFLabel.layoutXProperty().add(checkSystemSuitabilityFLabel.widthProperty().add(12.0)));
			line4F.startYProperty().bind(checkSystemSuitabilityFLabel.layoutYProperty().add(checkSystemSuitabilityFLabel.heightProperty().divide(2.0)));
			line4F.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(line4A.startXProperty()).multiply(2.0/3.0).add(line4A.startXProperty()));
			line4F.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));

			drawPane.getChildren().add(line4A);
			drawPane.getChildren().add(line4B);
			drawPane.getChildren().add(line4C);
			drawPane.getChildren().add(line4D);
			drawPane.getChildren().add(line4E);
			drawPane.getChildren().add(line4F);
			
			Line finalLine = new Line();
			finalLine.startXProperty().bind(line4A.endXProperty());
			finalLine.startYProperty().bind(line4A.endYProperty());
			finalLine.endXProperty().bind(finalFitLabel.layoutXProperty().subtract(12.0));
			finalLine.endYProperty().bind(finalFitLabel.layoutYProperty().add(finalFitLabel.heightProperty().divide(2.0)));
			
			Polygon finalPolygon = new Polygon(new double[]{0, -6.0, 0, 6.0, 8, 0});
			finalPolygon.layoutXProperty().bind(finalLine.endXProperty());
			finalPolygon.layoutYProperty().bind(finalLine.endYProperty());

			drawPane.getChildren().add(finalLine);
			drawPane.getChildren().add(finalPolygon);
			
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
			// Create listeners for the text boxes
			textRetentionTimeA.focusedProperty().addListener(changeListener);
			textRetentionTimeB.focusedProperty().addListener(changeListener);
			textRetentionTimeC.focusedProperty().addListener(changeListener);
			textRetentionTimeD.focusedProperty().addListener(changeListener);
			textRetentionTimeE.focusedProperty().addListener(changeListener);
			textRetentionTimeF.focusedProperty().addListener(changeListener);

			performValidations();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		columnProgram.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("name"));
	    columnMeasuredRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("measuredRetentionTimeString"));
	    //columnMeasuredRetentionTime.setCellFactory(TextFieldTableCell.<StandardCompound>forTableColumn());
	    
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
	    
	    
	    for (int i = 0; i < 6; i++)
	    {
	    	StandardCompound newTestCompound = new StandardCompound();
	    	newTestCompound.setIndex(i);
	    	if (i == 0)
	    		newTestCompound.setName("Program A");
	    	else if (i == 1)
	    		newTestCompound.setName("Program B");
	    	else if (i == 2)
	    		newTestCompound.setName("Program C");
	    	else if (i == 3)
	    		newTestCompound.setName("Program D");
	    	else if (i == 4)
	    		newTestCompound.setName("Program E");
	    	else if (i == 5)
	    		newTestCompound.setName("Program F");
	    	programList.add(newTestCompound);
	    }
	    
	    tableRetentionTimes.setItems(programList);
	}
	
	@FXML private void actionPerformValidation(ActionEvent e)
	{
		performValidations();
	}

	private void performValidations()
	{
		validateMeasuredRetentionTimeA();
		validateMeasuredRetentionTimeB();
		validateMeasuredRetentionTimeC();
		validateMeasuredRetentionTimeD();
		validateMeasuredRetentionTimeE();
		validateMeasuredRetentionTimeF();
		
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
	
	private void validateMeasuredRetentionTimeA()
	{
		if (textRetentionTimeA == null)
			return;
		
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textRetentionTimeA.getText());
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
			textRetentionTimeA.setText("");
		else
			textRetentionTimeA.setText(Float.toString((float)dTemp));
		
		if (programList.size() > 0)
			programList.get(0).setMeasuredRetentionTime(dTemp);
	}
	
	private void validateMeasuredRetentionTimeB()
	{
		if (textRetentionTimeB == null)
			return;
		
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textRetentionTimeB.getText());
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
			textRetentionTimeB.setText("");
		else
			textRetentionTimeB.setText(Float.toString((float)dTemp));

		if (programList.size() > 0)
			this.programList.get(1).setMeasuredRetentionTime(dTemp);
	}
	
	private void validateMeasuredRetentionTimeC()
	{
		if (textRetentionTimeC == null)
			return;
		
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textRetentionTimeC.getText());
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
			textRetentionTimeC.setText("");
		else
			textRetentionTimeC.setText(Float.toString((float)dTemp));

		if (programList.size() > 0)
			this.programList.get(2).setMeasuredRetentionTime(dTemp);
	}
	
	private void validateMeasuredRetentionTimeD()
	{
		if (textRetentionTimeD == null)
			return;
		
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textRetentionTimeD.getText());
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
			textRetentionTimeD.setText("");
		else
			textRetentionTimeD.setText(Float.toString((float)dTemp));
		
		if (programList.size() > 0)
			this.programList.get(3).setMeasuredRetentionTime(dTemp);
	}

	private void validateMeasuredRetentionTimeE()
	{
		if (textRetentionTimeE == null)
			return;
		
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textRetentionTimeE.getText());
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
			textRetentionTimeE.setText("");
		else
			textRetentionTimeE.setText(Float.toString((float)dTemp));
		
		if (programList.size() > 0)
			this.programList.get(4).setMeasuredRetentionTime(dTemp);
	}
	
	private void validateMeasuredRetentionTimeF()
	{
		if (textRetentionTimeF == null)
			return;
		
		double dTemp = 0;
    	try
    	{
    		dTemp = (double)Float.valueOf(textRetentionTimeF.getText());
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
			textRetentionTimeF.setText("");
		else
			textRetentionTimeF.setText(Float.toString((float)dTemp));
		
		if (programList.size() > 0)
			this.programList.get(5).setMeasuredRetentionTime(dTemp);
	}

	public void setRequiredInitialTemperature(int iTempProgram, double dInitialTemperature)
	{
		measuredRetentionTimesController[iTempProgram].setRequiredInitialTemperature(dInitialTemperature);
	}

	public void setRequiredInitialHoldTime(int iTempProgram, double dInitialHoldTime)
	{
		measuredRetentionTimesController[iTempProgram].setRequiredInitialHoldTime(dInitialHoldTime);
	}

	public void setRequiredRampRate(int iTempProgram, double dRampRate)
	{
		measuredRetentionTimesController[iTempProgram].setRequiredRampRate(dRampRate);
	}

	public void setRequiredFinalTemperature(int iTempProgram, double dFinalTemperature)
	{
		measuredRetentionTimesController[iTempProgram].setRequiredFinalTemperature(dFinalTemperature);
	}

	public void setRequiredFinalHoldTime(int iTempProgram, double dFinalHoldTime)
	{
		measuredRetentionTimesController[iTempProgram].setRequiredFinalHoldTime(dFinalHoldTime);
	}
	
	public void setProgramName(int iTempProgram, String strName)
	{
		measuredRetentionTimesController[iTempProgram].setProgramName(strName);
	}

	@FXML private void tabSelectionChanged() 
	{
		updateRoadMap();
		performValidations();
		updateFinalFitProgress();
    }
	
	private void updateRoadMap()
	{
		if (programAtab.isSelected())
		{
			selectSelectedRoadMapItem(0, iCurrentStep[0], false);
		}
		else if (programBtab.isSelected())
		{
			selectSelectedRoadMapItem(1, iCurrentStep[1], false);
		}	
		else if (programCtab.isSelected())
		{
			selectSelectedRoadMapItem(2, iCurrentStep[2], false);
		}	
		else if (programDtab.isSelected())
		{
			selectSelectedRoadMapItem(3, iCurrentStep[3], false);
		}	
		else if (programEtab.isSelected())
		{
			selectSelectedRoadMapItem(4, iCurrentStep[4], false);
		}	
		else if (programFtab.isSelected())
		{
			selectSelectedRoadMapItem(5, iCurrentStep[5], false);
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
		backCalculateALabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateALabel.getFont().getSize()));
		backCalculateBLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateBLabel.getFont().getSize()));
		backCalculateCLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateCLabel.getFont().getSize()));
		backCalculateDLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateDLabel.getFont().getSize()));
		backCalculateELabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateELabel.getFont().getSize()));
		backCalculateFLabel.setFont(Font.font(null, FontWeight.NORMAL, backCalculateFLabel.getFont().getSize()));
		checkSystemSuitabilityALabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityALabel.getFont().getSize()));
		checkSystemSuitabilityBLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityBLabel.getFont().getSize()));
		checkSystemSuitabilityCLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityCLabel.getFont().getSize()));
		checkSystemSuitabilityDLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityDLabel.getFont().getSize()));
		checkSystemSuitabilityELabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityELabel.getFont().getSize()));
		checkSystemSuitabilityFLabel.setFont(Font.font(null, FontWeight.NORMAL, checkSystemSuitabilityFLabel.getFont().getSize()));

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
		}
	}

	private void switchToBackCalculatePane(MeasuredRetentionTimesController measuredRetentionTimesController, BackcalculateController backCalculateController)
	{
		backCalculateController.resetValues();
		backCalculateController.setFilmThickness(measuredRetentionTimesController.getFilmThickness());
		backCalculateController.setInnerDiameter(measuredRetentionTimesController.getInnerDiameter());
		backCalculateController.setFlowRate(measuredRetentionTimesController.getFlowRate());
		backCalculateController.setInletPressure(measuredRetentionTimesController.getInletPressure());
		backCalculateController.setOutletPressure(measuredRetentionTimesController.getOutletPressure());
		backCalculateController.setColumnLength(measuredRetentionTimesController.getColumnLength());
		backCalculateController.setConstantFlowRateMode(measuredRetentionTimesController.getConstantFlowRateMode());
		backCalculateController.setTemperatureProgram(measuredRetentionTimesController.getTemperatureProgram());
		backCalculateController.setTemperatureProgramInConventionalForm(measuredRetentionTimesController.getInitialTemperature(), measuredRetentionTimesController.getInitialHoldTime(), measuredRetentionTimesController.getTemperatureProgramInConventionalForm());
		backCalculateController.setStandardsList(measuredRetentionTimesController.getStandardsList());
		backCalculateController.setUnderVacuum(measuredRetentionTimesController.isUnderVacuum());
		backCalculateController.setFileName(measuredRetentionTimesController.getFileName());
	}
	
	@Override
	public void onNextStepPressed(MeasuredRetentionTimesController thisController) 
	{
		if (thisController == this.measuredRetentionTimesController[0])
		{
			switchToBackCalculatePane(measuredRetentionTimesController[0], backCalculateController[0]);
			programAtab.setContent(backCalculatePane[0]);
			this.iCurrentStep[0]++;
			updateRoadMap();
		}
		else if (thisController == this.measuredRetentionTimesController[1])
		{
			switchToBackCalculatePane(measuredRetentionTimesController[1], backCalculateController[1]);
			programBtab.setContent(backCalculatePane[1]);
			this.iCurrentStep[1]++;
			updateRoadMap();
		}
		else if (thisController == this.measuredRetentionTimesController[2])
		{
			switchToBackCalculatePane(measuredRetentionTimesController[2], backCalculateController[2]);
			programCtab.setContent(backCalculatePane[2]);
			this.iCurrentStep[2]++;
			updateRoadMap();
		}
		else if (thisController == this.measuredRetentionTimesController[3])
		{
			switchToBackCalculatePane(measuredRetentionTimesController[3], backCalculateController[3]);
			programDtab.setContent(backCalculatePane[3]);
			this.iCurrentStep[3]++;
			updateRoadMap();
		}
		else if (thisController == this.measuredRetentionTimesController[4])
		{
			switchToBackCalculatePane(measuredRetentionTimesController[4], backCalculateController[4]);
			programEtab.setContent(backCalculatePane[4]);
			this.iCurrentStep[4]++;
			updateRoadMap();
		}
		else if (thisController == this.measuredRetentionTimesController[5])
		{
			switchToBackCalculatePane(measuredRetentionTimesController[5], backCalculateController[5]);
			programFtab.setContent(backCalculatePane[5]);
			this.iCurrentStep[5]++;
			updateRoadMap();
		}
	}

/*	private void switchToSystemBackCalculatePane(MeasuredRetentionTimesController measuredRetentionTimesController, BackcalculateController backCalculateController)
	{
		backCalculateController.resetValues();
		backCalculateController.setFilmThickness(measuredRetentionTimesController.getFilmThickness());
		backCalculateController.setInnerDiameter(measuredRetentionTimesController.getInnerDiameter());
		backCalculateController.setFlowRate(measuredRetentionTimesController.getFlowRate());
		backCalculateController.setInletPressure(measuredRetentionTimesController.getInletPressure());
		backCalculateController.setOutletPressure(measuredRetentionTimesController.getOutletPressure());
		backCalculateController.setColumnLength(measuredRetentionTimesController.getColumnLength());
		backCalculateController.setConstantFlowRateMode(measuredRetentionTimesController.getConstantFlowRateMode());
		backCalculateController.setTemperatureProgram(measuredRetentionTimesController.getTemperatureProgram());
		backCalculateController.setStandardsList(measuredRetentionTimesController.getStandardsList());
	}*/
	
	@Override
	public void onNextStepPressed(BackcalculateController thisController) {
		if (thisController == this.backCalculateController[0])
		{
			backCalculateController[0].switchToStep4();
			this.iCurrentStep[0]++;
			updateRoadMap();
		}
		else if (thisController == this.backCalculateController[1])
		{
			backCalculateController[1].switchToStep4();
			this.iCurrentStep[1]++;
			updateRoadMap();
		}
		else if (thisController == this.backCalculateController[2])
		{
			backCalculateController[2].switchToStep4();
			this.iCurrentStep[2]++;
			updateRoadMap();
		}
		else if (thisController == this.backCalculateController[3])
		{
			backCalculateController[3].switchToStep4();
			this.iCurrentStep[3]++;
			updateRoadMap();
		}
		else if (thisController == this.backCalculateController[4])
		{
			backCalculateController[4].switchToStep4();
			this.iCurrentStep[4]++;
			updateRoadMap();
		}
		else if (thisController == this.backCalculateController[5])
		{
			backCalculateController[5].switchToStep4();
			this.iCurrentStep[5]++;
			updateRoadMap();
		}
	}

	@Override
	public void onPreviousStepPressed(BackcalculateController thisController) {
		if (thisController == this.backCalculateController[0])
		{
			int i = 0;
			if (this.iCurrentStep[i] == 2)
			{
				backCalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				programAtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backCalculateController[1])
		{
			int i = 1;
			if (this.iCurrentStep[i] == 2)
			{
				backCalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				programBtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backCalculateController[2])
		{
			int i = 2;
			if (this.iCurrentStep[i] == 2)
			{
				backCalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				programCtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backCalculateController[3])
		{
			int i = 3;
			if (this.iCurrentStep[i] == 2)
			{
				backCalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				programDtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backCalculateController[4])
		{
			int i = 4;
			if (this.iCurrentStep[i] == 2)
			{
				backCalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				programEtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}
		else if (thisController == this.backCalculateController[5])
		{
			int i = 5;
			if (this.iCurrentStep[i] == 2)
			{
				backCalculateController[i].switchToStep3();
			}
			else if (this.iCurrentStep[i] == 1)
			{
				programFtab.setContent(measuredRetentionTimes[i]);
				measuredRetentionTimes[i].setVisible(true);
			}
			this.iCurrentStep[i]--;
			updateRoadMap();
			updateFinalFitProgress();
		}		
	}

	public void cancelAllTasks()
	{
		for (int i = 0; i < backCalculateController.length; i++)
		{
			if (backCalculateController[i] != null)
				backCalculateController[i].cancelTasks();
		}
	}
	
	public boolean areThreadsRunning()
	{
		boolean threadRunning = false;
		for (int i = 0; i < backCalculateController.length; i++)
		{
			if (backCalculateController[i].isThreadRunning())
				threadRunning = true;
		}
		
		if (this.solveParametersTask != null && this.solveParametersTask.isRunning())
			threadRunning = true;
		
		return threadRunning;
	}
	
	public void updateFinalFitProgress()
	{
		for (int i = 0; i < backCalculateController.length; i++)
		{
			if (backCalculateController[i] == null)
				return;
		}
		
		boolean bIsAcceptableToSolveForParameters = true;
		for (int i = 0; i < backCalculateController.length; i++)
		{
			if (backCalculateController[i].getStatus() == backCalculateController[i].INCOMPLETE)
				bIsAcceptableToSolveForParameters = false;
		}
		
		// Make the Solve pane disabled if one of the programs is incomplete.
		this.paneSolveForParameters.setDisable(!bIsAcceptableToSolveForParameters);
		
		if (backCalculateController[0].getStatus() == backCalculateController[0].INCOMPLETE)
		{
			this.labelProgramAStatus.setText("Incomplete");
			this.labelProgramAStatus.setTextFill(Color.BLACK);
		}
		else if (backCalculateController[0].getStatus() == backCalculateController[0].PASSED)
		{
			this.labelProgramAStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[0].getScore(), 2)) + ")");
			this.labelProgramAStatus.setTextFill(Color.GREEN);
		}
		else if (backCalculateController[0].getStatus() == backCalculateController[0].PASSEDBUTQUESTIONABLE)
		{
			this.labelProgramAStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[0].getScore(), 2)) + ")");
			this.labelProgramAStatus.setTextFill(Color.YELLOW);
		}
		else if (backCalculateController[0].getStatus() == backCalculateController[0].FAILED)
		{
			this.labelProgramAStatus.setText("Failed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[0].getScore(), 2)) + ")");
			this.labelProgramAStatus.setTextFill(Color.RED);
		}

		if (backCalculateController[1].getStatus() == backCalculateController[1].INCOMPLETE)
		{
			this.labelProgramBStatus.setText("Incomplete");
			this.labelProgramBStatus.setTextFill(Color.BLACK);
		}
		else if (backCalculateController[1].getStatus() == backCalculateController[1].PASSED)
		{
			this.labelProgramBStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[1].getScore(), 2)) + ")");
			this.labelProgramBStatus.setTextFill(Color.GREEN);
		}
		else if (backCalculateController[1].getStatus() == backCalculateController[1].PASSEDBUTQUESTIONABLE)
		{
			this.labelProgramBStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[1].getScore(), 2)) + ")");
			this.labelProgramBStatus.setTextFill(Color.YELLOW);
		}
		else if (backCalculateController[1].getStatus() == backCalculateController[1].FAILED)
		{
			this.labelProgramBStatus.setText("Failed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[1].getScore(), 2)) + ")");
			this.labelProgramBStatus.setTextFill(Color.RED);
		}

		if (backCalculateController[2].getStatus() == backCalculateController[2].INCOMPLETE)
		{
			this.labelProgramCStatus.setText("Incomplete");
			this.labelProgramCStatus.setTextFill(Color.BLACK);
		}
		else if (backCalculateController[2].getStatus() == backCalculateController[2].PASSED)
		{
			this.labelProgramCStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[2].getScore(), 2)) + ")");
			this.labelProgramCStatus.setTextFill(Color.GREEN);
		}
		else if (backCalculateController[2].getStatus() == backCalculateController[2].PASSEDBUTQUESTIONABLE)
		{
			this.labelProgramCStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[2].getScore(), 2)) + ")");
			this.labelProgramCStatus.setTextFill(Color.YELLOW);
		}
		else if (backCalculateController[2].getStatus() == backCalculateController[2].FAILED)
		{
			this.labelProgramCStatus.setText("Failed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[2].getScore(), 2)) + ")");
			this.labelProgramCStatus.setTextFill(Color.RED);
		}

		if (backCalculateController[3].getStatus() == backCalculateController[3].INCOMPLETE)
		{
			this.labelProgramDStatus.setText("Incomplete");
			this.labelProgramDStatus.setTextFill(Color.BLACK);
		}
		else if (backCalculateController[3].getStatus() == backCalculateController[3].PASSED)
		{
			this.labelProgramDStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[3].getScore(), 2)) + ")");
			this.labelProgramDStatus.setTextFill(Color.GREEN);
		}
		else if (backCalculateController[3].getStatus() == backCalculateController[3].PASSEDBUTQUESTIONABLE)
		{
			this.labelProgramDStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[3].getScore(), 2)) + ")");
			this.labelProgramDStatus.setTextFill(Color.YELLOW);
		}
		else if (backCalculateController[3].getStatus() == backCalculateController[3].FAILED)
		{
			this.labelProgramDStatus.setText("Failed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[3].getScore(), 2)) + ")");
			this.labelProgramDStatus.setTextFill(Color.RED);
		}

		if (backCalculateController[4].getStatus() == backCalculateController[4].INCOMPLETE)
		{
			this.labelProgramEStatus.setText("Incomplete");
			this.labelProgramEStatus.setTextFill(Color.BLACK);
		}
		else if (backCalculateController[4].getStatus() == backCalculateController[4].PASSED)
		{
			this.labelProgramEStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[4].getScore(), 2)) + ")");
			this.labelProgramEStatus.setTextFill(Color.GREEN);
		}
		else if (backCalculateController[4].getStatus() == backCalculateController[4].PASSEDBUTQUESTIONABLE)
		{
			this.labelProgramEStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[4].getScore(), 2)) + ")");
			this.labelProgramEStatus.setTextFill(Color.YELLOW);
		}
		else if (backCalculateController[4].getStatus() == backCalculateController[4].FAILED)
		{
			this.labelProgramEStatus.setText("Failed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[4].getScore(), 2)) + ")");
			this.labelProgramEStatus.setTextFill(Color.RED);
		}

		if (backCalculateController[5].getStatus() == backCalculateController[5].INCOMPLETE)
		{
			this.labelProgramFStatus.setText("Incomplete");
			this.labelProgramFStatus.setTextFill(Color.BLACK);
		}
		else if (backCalculateController[5].getStatus() == backCalculateController[5].PASSED)
		{
			this.labelProgramFStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[5].getScore(), 2)) + ")");
			this.labelProgramFStatus.setTextFill(Color.GREEN);
		}
		else if (backCalculateController[5].getStatus() == backCalculateController[5].PASSEDBUTQUESTIONABLE)
		{
			this.labelProgramFStatus.setText("Passed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[5].getScore(), 2)) + ")");
			this.labelProgramFStatus.setTextFill(Color.YELLOW);
		}
		else if (backCalculateController[5].getStatus() == backCalculateController[5].FAILED)
		{
			this.labelProgramFStatus.setText("Failed (score = " + Float.toString((float)Globals.roundToSignificantFigures(backCalculateController[5].getScore(), 2)) + ")");
			this.labelProgramFStatus.setTextFill(Color.RED);
		}
	}
	
	@FXML private void onCommitRetentionTime(TableColumn.CellEditEvent<StandardCompound,String> t)
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
	}
	
	@FXML private void onSolveForRetentionParameters(ActionEvent event)
	{
		solveParametersTask = new SolveParametersTask();
		
		double[] filmThickness = new double[measuredRetentionTimesController.length];
		double[] innerDiameter = new double[measuredRetentionTimesController.length];
		double[] tStep = new double[measuredRetentionTimesController.length];
		double[] measuredRetentionTimes = new double[measuredRetentionTimesController.length];
		InterpolationFunction[] holdUpTimeProfiles = new InterpolationFunction[6];
		LinearInterpolationFunction[] temperaturePrograms = new LinearInterpolationFunction[6];
		
		for (int i = 0; i < measuredRetentionTimesController.length; i++)
		{
			filmThickness[i] = measuredRetentionTimesController[i].getFilmThickness();
			innerDiameter[i] = measuredRetentionTimesController[i].getInnerDiameter();
			tStep[i] = backCalculateController[i].getTStep();
			measuredRetentionTimes[i] = programList.get(i).getMeasuredRetentionTime();
			holdUpTimeProfiles[i] = backCalculateController[i].getHoldUpTimeProfile();
			temperaturePrograms[i] = backCalculateController[i].getTemperatureProfile();
		}
		
		solveParametersTask.setFilmThickness(filmThickness);
		solveParametersTask.setInnerDiameter(innerDiameter);
		solveParametersTask.setTStep(tStep);
		solveParametersTask.setRetentionTimes(measuredRetentionTimes);
		solveParametersTask.setHoldUpTimeProfiles(holdUpTimeProfiles);
		solveParametersTask.setTemperaturePrograms(temperaturePrograms);
		solveParametersTask.setProgramsList(programList);
		solveParametersTask.setSolveParametersListener(this);
		
		solveParametersTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
			@Override
			public void handle(WorkerStateEvent arg0) {
				finalFitComplete = true;
				buttonSolve.setDisable(false);
				updateRoadMap();
				
				labelH.textProperty().unbind();
				labelH.setText(solveParametersTask.dHProperty().get());
				labelS.textProperty().unbind();
				labelS.setText(solveParametersTask.dSProperty().get());
				labelCp.textProperty().unbind();
				labelCp.setText(solveParametersTask.dCpProperty().get());
				labelIteration.textProperty().unbind();
				labelIteration.setText(solveParametersTask.iterationProperty().get());
				labelVariance.textProperty().unbind();
				labelVariance.setText(solveParametersTask.varianceProperty().get());
				
				if (solveParametersTask.getBestVariance() < 1E-3)
				{
					String strMessage = "You have successfully determined \u0394H, \u0394S, and \u0394Cp for this compound.\n\nSave this information by choosing \"Save as...\" from the File menu. Then submit the *.gcdata file to boswell@umn.edu to have it incorporated into the shared online database at retentionprediction.org";
	        		FXOptionPane.showMessageDialog(primaryStage, strMessage, "Success", FXOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					String strMessage = "You have determined \u0394H, \u0394S, and \u0394Cp values for this compound, but the variance is unusually high. This could mean that one of the retention times is incorrect. Please double-check to make sure all of the 6 retention times are correct.\n\nThen save this information by choosing \"Save as...\" from the File menu. Then submit the *.gcdata file to boswell@umn.edu to have it incorporated into the shared online database at retentionprediction.org";
	        		FXOptionPane.showMessageDialog(primaryStage, strMessage, "High Variance", FXOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		solveParametersTask.setOnCancelled(new EventHandler<WorkerStateEvent>(){
			@Override
			public void handle(WorkerStateEvent event) {
				finalFitComplete = false;
				buttonSolve.setDisable(false);
				updateRoadMap();
			}
		});
		
		solveParametersTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
			@Override
			public void handle(WorkerStateEvent event) {
				finalFitComplete = false;
				buttonSolve.setDisable(false);
				updateRoadMap();
			}
		});
		
		labelIteration.textProperty().bind(solveParametersTask.iterationProperty());
		labelTimeElapsed.textProperty().bind(solveParametersTask.timeElapsedProperty());
		labelVariance.textProperty().bind(solveParametersTask.varianceProperty());
		labelStatus.textProperty().bind(solveParametersTask.messageProperty());
		labelH.textProperty().bind(solveParametersTask.dHProperty());
		labelS.textProperty().bind(solveParametersTask.dSProperty());
		labelCp.textProperty().bind(solveParametersTask.dCpProperty());
		progressBar.progressProperty().bind(solveParametersTask.progressProperty());
		
		Thread newThread = new Thread(solveParametersTask);
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
		    public void uncaughtException(Thread th, Throwable ex) {
		        System.out.println("Uncaught exception: " + ex);
		    }
		};
		newThread.setUncaughtExceptionHandler(h);
		buttonSolve.setDisable(true);
		newThread.start();
	}

	@Override
	public void onUpdateTable(ObservableList<StandardCompound> list) 
	{
		for (int i = 0; i < list.size(); i++)
		{
			this.programList.get(i).makeEqualTo(list.get(i));
		}
	}
	
	@FXML void onNewAction(ActionEvent event)
	{
		this.parentPaneControllerListener.onNew();
	}
	
	@FXML void onOpenAction(ActionEvent event)
	{
		this.parentPaneControllerListener.onOpen();
	}
	
	@FXML void onSaveAction(ActionEvent event)
	{
		this.parentPaneControllerListener.onSave();
	}
	
	@FXML void onSaveAsAction(ActionEvent event)
	{
		this.parentPaneControllerListener.onSaveAs();
	}
	
	@FXML void onCloseAction(ActionEvent event)
	{
		this.parentPaneControllerListener.onClose();
	}
	
	@FXML void onAboutAction(ActionEvent event)
	{
		this.parentPaneControllerListener.onAbout();
	}

	public void setStage(Stage primaryStage) 
	{
		this.primaryStage = primaryStage;
		for (int i = 0; i < backCalculateController.length; i++)
		{
			backCalculateController[i].setParentWindow(primaryStage);
		}
		
		// Load 6 copies of the measure retention times pane and also get the controlling class
		for (int i = 0; i < measuredRetentionTimesController.length; i++)
		{
			measuredRetentionTimesController[i].setParentWindow(primaryStage);
		}
	}
	
	public void writeSaveData(SaveData saveData)
	{
		saveData.measuredRetentionTimeSaveData = new SaveData.MeasuredRetentionTimeSaveData[this.measuredRetentionTimesController.length];
		saveData.backCalculateSaveData = new SaveData.BackCalculateSaveData[backCalculateController.length];
		
		for (int i = 0; i < measuredRetentionTimesController.length; i++)
		{
			saveData.measuredRetentionTimeSaveData[i] = saveData.new MeasuredRetentionTimeSaveData();
			measuredRetentionTimesController[i].writeSaveData(saveData.measuredRetentionTimeSaveData[i]);
		}
		
		for (int i = 0; i < backCalculateController.length; i++)
		{
			saveData.backCalculateSaveData[i] = saveData.new BackCalculateSaveData();
			backCalculateController[i].writeSaveData(saveData.backCalculateSaveData[i]);
		}
		
		saveData.iCurrentStep = iCurrentStep;
		saveData.finalFitComplete = finalFitComplete;
		saveData.programList = programList;
		saveData.labelHText = labelH.getText();
		saveData.labelSText = labelS.getText();
		saveData.labelCpText = labelCp.getText();
		saveData.labelIterationText = labelIteration.getText();
		saveData.labelVarianceText = labelVariance.getText();
		saveData.labelTimeElapsedText = labelTimeElapsed.getText();
		saveData.statusText = labelStatus.getText();
		saveData.compoundName = textCompoundName.getText();
		saveData.formula = textFormula.getText();
		saveData.pubChemID = textPubChemID.getText();
		saveData.cAS = textCAS.getText();
		saveData.nISTID = textNISTID.getText();
		saveData.hMDB = textHMDB.getText();
		saveData.retentionTimeA = textRetentionTimeA.getText();
		saveData.retentionTimeB = textRetentionTimeB.getText();
		saveData.retentionTimeC = textRetentionTimeC.getText();
		saveData.retentionTimeD = textRetentionTimeD.getText();
		saveData.retentionTimeE = textRetentionTimeE.getText();
		saveData.retentionTimeF = textRetentionTimeF.getText();
	}
	
	public void loadSaveData(SaveData saveData)
	{
		//saveData.measuredRetentionTimeSaveData = new SaveData.MeasuredRetentionTimeSaveData[this.measuredRetentionTimesController.length];
		//saveData.backCalculateSaveData = new SaveData.BackCalculateSaveData[backCalculateController.length];

		for (int i = 0; i < saveData.measuredRetentionTimeSaveData.length; i++)
		{
			//saveData.measuredRetentionTimeSaveData[i] = saveData.new MeasuredRetentionTimeSaveData();
			measuredRetentionTimesController[i].loadSaveData(saveData.measuredRetentionTimeSaveData[i]);
		}
			
		for (int i = 0; i < saveData.backCalculateSaveData.length; i++)
		{
			//saveData.backCalculateSaveData[i] = saveData.new BackCalculateSaveData();
			backCalculateController[i].loadSaveData(saveData.backCalculateSaveData[i]);
		}
		
		iCurrentStep = saveData.iCurrentStep;
		finalFitComplete = saveData.finalFitComplete;
		programList = saveData.programList;
		labelH.textProperty().unbind();
		labelH.setText(saveData.labelHText);
		labelS.textProperty().unbind();
		labelS.setText(saveData.labelSText);
		labelCp.textProperty().unbind();
		labelCp.setText(saveData.labelCpText);
		labelIteration.textProperty().unbind();
		labelIteration.setText(saveData.labelIterationText);
		labelVariance.textProperty().unbind();
		labelVariance.setText(saveData.labelVarianceText);
		labelTimeElapsed.textProperty().unbind();
		labelTimeElapsed.setText(saveData.labelTimeElapsedText);
		labelStatus.textProperty().unbind();
		labelStatus.setText(saveData.statusText);
		textCompoundName.setText(saveData.compoundName);
		textFormula.setText(saveData.formula);
		textPubChemID.setText(saveData.pubChemID);
		textCAS.setText(saveData.cAS);
		textNISTID.setText(saveData.nISTID);
		textHMDB.setText(saveData.hMDB);
		textRetentionTimeA.setText(saveData.retentionTimeA);
		textRetentionTimeB.setText(saveData.retentionTimeB);
		textRetentionTimeC.setText(saveData.retentionTimeC);
		textRetentionTimeD.setText(saveData.retentionTimeD);
		textRetentionTimeE.setText(saveData.retentionTimeE);
		textRetentionTimeF.setText(saveData.retentionTimeF);
		
		// Set the visible pane in the tab
		for (int i = 0; i < this.backCalculateController.length; i++)
		{
			if (iCurrentStep[i] <= 1)
				backCalculateController[i].switchToStep3();
			else if (iCurrentStep[i] >= 2)
				backCalculateController[i].switchToStep4();
		}
		
		if (iCurrentStep[0] == 0)
			programAtab.setContent(measuredRetentionTimes[0]);
		if (iCurrentStep[1] == 0)
			programBtab.setContent(measuredRetentionTimes[1]);
		if (iCurrentStep[2] == 0)
			programCtab.setContent(measuredRetentionTimes[2]);
		if (iCurrentStep[3] == 0)
			programDtab.setContent(measuredRetentionTimes[3]);
		if (iCurrentStep[4] == 0)
			programEtab.setContent(measuredRetentionTimes[4]);
		if (iCurrentStep[5] == 0)
			programFtab.setContent(measuredRetentionTimes[5]);
		
		if (iCurrentStep[0] >= 1)
			programAtab.setContent(backCalculatePane[0]);
		if (iCurrentStep[1] >= 1)
			programBtab.setContent(backCalculatePane[1]);
		if (iCurrentStep[2] >= 1)
			programCtab.setContent(backCalculatePane[2]);
		if (iCurrentStep[3] >= 1)
			programDtab.setContent(backCalculatePane[3]);
		if (iCurrentStep[4] >= 1)
			programEtab.setContent(backCalculatePane[4]);
		if (iCurrentStep[5] >= 1)
			programFtab.setContent(backCalculatePane[5]);
		
	    tableRetentionTimes.setItems(programList);

		this.updateRoadMap();
		this.updateFinalFitProgress();
	}
}
