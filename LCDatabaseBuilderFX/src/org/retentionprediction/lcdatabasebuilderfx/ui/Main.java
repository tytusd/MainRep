package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.File;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	private Stage primaryStage;
	private Preferences prefs;
	private MainController mainController;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		this.primaryStage = primaryStage;
		this.prefs = Preferences.userNodeForPackage(this.getClass());
		
		try{
			FXMLLoader fxmlLoader = new FXMLLoader();
			BorderPane root = fxmlLoader.load(getClass().getResource("main.fxml").openStream());
			mainController = fxmlLoader.getController();
			
			Scene scene = new Scene(root, 600, 400);
			String css = this.getClass().getResource("application.css").toExternalForm();
			scene.getStylesheets().add(css);
			
			primaryStage.setTitle("HPLC Database Builder - Mode Selection Wizard");
			
			primaryStage.setScene(scene);
			mainController.setPrimaryStage(primaryStage);
			primaryStage.setOnHiding(new EventHandler<WindowEvent>(){

				@Override
				public void handle(WindowEvent event) {
					//TODO: Cancel all tasks.
				}
				
			});
			
			primaryStage.show();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
