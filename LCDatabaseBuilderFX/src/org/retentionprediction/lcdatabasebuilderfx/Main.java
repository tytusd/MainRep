package org.retentionprediction.lcdatabasebuilderfx;

import java.io.File;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application implements ParentPaneControllerListener {

	private Stage primaryStage;
	private Preferences prefs;
	private ParentPaneController parentPaneController;
	private File currentFile;
	
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
			VBox root = fxmlLoader.load(getClass().getResource("ParentPane.fxml").openStream());
			parentPaneController = fxmlLoader.getController();
			parentPaneController.setParentPaneControllerListener(this);
			
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			Scene scene = new Scene(root, primaryScreenBounds.getWidth()*0.8, primaryScreenBounds.getHeight()*0.9);
			String css = this.getClass().getResource("application.css").toExternalForm();
			scene.getStylesheets().add(css);
			
			//TODO: update the window title here
			
			primaryStage.setScene(scene);
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

	@Override
	public void onNew() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSave() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClose() {
		primaryStage.close();
	}

	@Override
	public void onAbout() {
		// TODO Auto-generated method stub
		
	}
	
	private void updateWindowTitle()
	{
		String fileName;
		if (currentFile == null)
			fileName = "untitled";
		else
			fileName = currentFile.getName();
		primaryStage.setTitle("LC Retention Database Builder - " + fileName);
	}

}
