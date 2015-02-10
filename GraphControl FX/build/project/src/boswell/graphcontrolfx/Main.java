package boswell.graphcontrolfx;
	
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Main extends Application implements Initializable{
	
	@FXML private AnchorPane mainPane;
	
	@Override
	public void start(Stage primaryStage) 
	{
		Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);

		try {
			// Load the PeakFinderSettingsPane and its controller
			FXMLLoader fxmlLoader = new FXMLLoader();
			AnchorPane root = (AnchorPane)fxmlLoader.load(getClass().getResource("MainWindow.fxml").openStream());
			
			// Create the scene
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			dialog.setTitle("Canvas test");
			dialog.setScene(scene);
			
			// Show the dialog and wait for it to return
			dialog.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		GraphControlFX graphControlFX = new GraphControlFX();
		//graphControlFX.setHeight(500);
		//graphControlFX.setWidth(500);
		mainPane.getChildren().add(graphControlFX);
		AnchorPane.setTopAnchor(graphControlFX, 0.0);
		AnchorPane.setBottomAnchor(graphControlFX, 0.0);
		AnchorPane.setLeftAnchor(graphControlFX, 0.0);
		AnchorPane.setRightAnchor(graphControlFX, 0.0);
		
        graphControlFX.widthProperty().bind(mainPane.widthProperty());
        graphControlFX.heightProperty().bind(mainPane.heightProperty());
        
        Color clrBlack = Color.BLACK;
        
		int chromatogramPlotIndex = graphControlFX.AddSeries("Chromatogram", clrBlack, 1, false, false);
		graphControlFX.AddDataPoint(chromatogramPlotIndex, 0, 0);
		graphControlFX.AddDataPoint(chromatogramPlotIndex, 1, 1);
		graphControlFX.AddDataPoint(chromatogramPlotIndex, 2, -1);


		graphControlFX.repaint();
	}
}
