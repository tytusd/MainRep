package org.retentionprediction.gcdatabasebuilderfx;
	
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.retentionprediction.gcdatabasebuilderfx.ParentPaneController.ParentPaneControllerListener;

import boswell.fxoptionpane.FXOptionPane;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;


public class Main extends Application implements ParentPaneControllerListener
{
	private boolean documentChangedFlag = true;
	private ParentPaneController parentPaneController;
	private Stage primaryStage;
	private File currentFile = null;
	private Preferences prefs;
	private SaveData saveData = new SaveData();
	private int majorVersion = 1;
	private int minorVersion = 0;
	
	@Override
	public void start(Stage primaryStage) 
	{
		this.primaryStage = primaryStage;
		
		// Create the Preferences class
		prefs = Preferences.userNodeForPackage(this.getClass());

		try
		{
			// Load the main pane and its controller
			FXMLLoader fxmlLoader = new FXMLLoader();
			VBox root = (VBox)fxmlLoader.load(getClass().getResource("ParentPane.fxml").openStream());
			parentPaneController = fxmlLoader.getController();
			
			parentPaneController.setParentPaneControllerListener(this);

			// Create the scene
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

			Scene scene = new Scene(root,primaryScreenBounds.getWidth() * .8, primaryScreenBounds.getHeight() * 0.9);
			String cssURL = this.getClass().getResource("application.css")
					 .toExternalForm();
			scene.getStylesheets().add(cssURL);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Load the primary stage and load the main pane into it
			updateWindowTitle();
			primaryStage.setScene(scene);
			primaryStage.setOnHiding(new EventHandler<WindowEvent>() 
					{
			      		public void handle(WindowEvent event) 
			      		{
			      			parentPaneController.cancelAllTasks();
			      		}
					});
			// Load the required values into each program tab
			for (int i = 0; i < 6; i++)
			{
				parentPaneController.setRequiredInitialTemperature(i, Globals.requiredTemperaturePrograms[i][0][0]);
				parentPaneController.setRequiredInitialHoldTime(i, Globals.requiredTemperaturePrograms[i][0][1]);
				parentPaneController.setRequiredRampRate(i, Globals.requiredTemperaturePrograms[i][1][0]);
				parentPaneController.setRequiredFinalTemperature(i, Globals.requiredTemperaturePrograms[i][1][1]);
				parentPaneController.setRequiredFinalHoldTime(i, Globals.requiredTemperaturePrograms[i][1][2]);
				parentPaneController.setProgramName(i, Globals.requiredTemperatureProgramNames[i]);
			}
			
			parentPaneController.setStage(primaryStage);
			
			// Show the primary stage
			primaryStage.show();
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}

	@Override
	public void onNew() {
		SaveData saveData = new SaveData();
       	parentPaneController.loadSaveData(saveData);
		currentFile = null;
		this.updateWindowTitle();
	}

	@Override
	public void onOpen() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open GC Database Builder File");
		
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("GC Database Builder Files (*.gcdata)", "*.gcdata"),
				new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
            );
		
		// Set default directory
		String lastOutputDir = prefs.get("LAST_OUTPUT_DIR", "");
		if (lastOutputDir != "")
		{
			File lastDir = new File(lastOutputDir);
			if (lastDir.exists())
				fileChooser.setInitialDirectory(lastDir.getParentFile());
		}
		
		File returnedFile = fileChooser.showOpenDialog(primaryStage);

		if (returnedFile != null) 
		{
			if (readFromInputStream(returnedFile))
			{
				parentPaneController.cancelAllTasks();
				updateWindowTitle();
				prefs.put("LAST_OUTPUT_DIR", returnedFile.getAbsolutePath());
			}
		}	
	}

	@Override
	public void onSave() {
		if (parentPaneController.areThreadsRunning())
		{
			FXOptionPane.showMessageDialog(primaryStage, "You cannot save while background calculations are running. Wait until the calculations are complete and then try again.", "Cannot Save Now", FXOptionPane.WARNING_MESSAGE);
			return;
		}
		
		if (currentFile == null)
		{
			onSaveAs();
		}
	
		writeToOutputStream();
	}

	@Override
	public void onSaveAs() {
		if (parentPaneController.areThreadsRunning())
		{
			FXOptionPane.showMessageDialog(primaryStage, "You cannot save while background calculations are running. Wait until the calculations are complete and then try again.", "Cannot Save Now", FXOptionPane.WARNING_MESSAGE);
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save GC Database Builder File");
		
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("GC Database Builder Files (*.gcdata)", "*.gcdata"),
				new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
            );

		// Set default directory
		String lastOutputDir = prefs.get("LAST_OUTPUT_DIR", "");
		if (lastOutputDir != "")
		{
			File lastDir = new File(lastOutputDir);
			if (lastDir.exists())
				fileChooser.setInitialDirectory(lastDir.getParentFile());
		}
		
		File returnedFile = fileChooser.showSaveDialog(primaryStage);

		if (returnedFile != null) 
		{
			if (!returnedFile.getName().endsWith(".gcdata"))
				returnedFile = new File(returnedFile.getAbsolutePath() + ".gcdata");
			
			this.currentFile = returnedFile;
			updateWindowTitle();
			writeToOutputStream();
			prefs.put("LAST_OUTPUT_DIR", returnedFile.getAbsolutePath());
		}
	}

	@Override
	public void onClose() {
		primaryStage.close();
	}

	@Override
	public void onAbout() {
		FXOptionPane.showMessageDialog(primaryStage, "GC Retention Database Builder version " + Integer.toString(majorVersion) + "." + Integer.toString(minorVersion) + "\n\nLearn more about GC Retention Database Builder at www.retentionprediction.org", "About GC Retention Database Builder", FXOptionPane.INFORMATION_MESSAGE);
		//int x = FXOptionPane.showConfirmDialog(primaryStage, "Message goes here. This is what needs to be said to the user when this message dialog pops up. It could be a single line or it could be multiple lines. Don't know.", "Hello again", FXOptionPane.YES_NO_OPTION);
		//FXOptionPane.showMessageDialog(primaryStage, Integer.toString(x), "Hello again", FXOptionPane.WARNING_MESSAGE);
		
	}
	
	private boolean writeToOutputStream()
	{
    	try 
		{
            FileOutputStream fos = new FileOutputStream(currentFile, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

	    	parentPaneController.writeSaveData(saveData);
	    	oos.writeObject(saveData);
	    	
            oos.flush();
			oos.close();
			this.documentChangedFlag = false;
    	}
    	catch (IOException e) 
		{
			e.printStackTrace();
    		FXOptionPane.showMessageDialog(primaryStage, "The file could not be saved.", "Error saving file", FXOptionPane.WARNING_MESSAGE);
	        return false;
		}
    	
    	return true;
	}
	
    public boolean readFromInputStream(File fileToRead)
    {
    	if (fileToRead != null)
    	{
    		try 
            {
                FileInputStream fis = new FileInputStream(fileToRead);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                saveData = (SaveData)ois.readObject();
               	parentPaneController.loadSaveData(saveData);
    	        
                ois.close();
			} 
            catch (IOException | ClassNotFoundException e) 
            {
				e.printStackTrace();
	    		FXOptionPane.showMessageDialog(primaryStage, "The file is not a valid GC Retention Database Builder file.", "Not a Valid File", FXOptionPane.WARNING_MESSAGE);
		        return false;
			} 
    	}

    	currentFile = fileToRead;
    	this.updateWindowTitle();
    	
    	return true;
    }
    
	private void updateWindowTitle()
	{
		String fileName;
		if (currentFile == null)
			fileName = "untitled";
		else
			fileName = currentFile.getName();
		primaryStage.setTitle("GC Retention Database Builder - " + fileName);
	}
	
	private void loadDefaults()
	{
		
	}
	
	private void loadControlsWithStoredValues()
	{
		
	}
}
