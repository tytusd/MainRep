package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.retentionprediction.lcdatabasebuilderfx.ui.InjectionParentPaneController.InjectionParentPaneControllerListener;
import org.retentionprediction.lcdatabasebuilderfx.ui.ParentPaneController.ParentPaneControllerListener;

import boswell.fxoptionpane.FXOptionPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainController implements Initializable, ParentPaneControllerListener, InjectionParentPaneControllerListener{

    //Fields for this class
	@FXML private RadioButton eightGradientsRadio;
    @FXML private RadioButton singleGradientRadio;
    @FXML private Button exitBtn;
    @FXML private Button okBtn;
    private final ToggleGroup toggleGroup = new ToggleGroup();
    
    //Fields for ParentPaneController
    private ParentPaneController parentPaneController;
    private InjectionParentPaneController injectionParentPaneController;
    
    private Stage primaryStage;
	private File currentFile = null;
	private Preferences prefs;
	private SaveData saveData = new SaveData();
	private InjectionSaveData injectionSaveData = new InjectionSaveData();
	//private SaveData saveData = null;
	private int majorVersion = 1;
	private int minorVersion = 0;
	private boolean documentChangedFlag;
    private boolean isEightGradientsRadio = true;
	
    public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	@FXML
    void onOkBtnClick(ActionEvent event) {
    	FXMLLoader fxmlLoader = new FXMLLoader();
    	try {
    		VBox root = null;
    		isEightGradientsRadio = toggleGroup.getSelectedToggle().equals(eightGradientsRadio);
    		if(isEightGradientsRadio){
    			InputStream stream = getClass().getResource("ParentPane.fxml").openStream();
    			root = fxmlLoader.load(stream);
    			parentPaneController = fxmlLoader.getController();
    			parentPaneController.setParentPaneControllerListener(this);
    		}
    		else{
    			//Singlegradientpane
    			InputStream stream = getClass().getResource("InjectionParentPane.fxml").openStream();
    			root = fxmlLoader.load(stream);
    			injectionParentPaneController = fxmlLoader.getController();
    			injectionParentPaneController.setInjectionParentPaneControllerListener(this);
    		}
			primaryStage.hide();
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
			Scene scene = new Scene(root, primaryScreenBounds.getWidth()*0.8, primaryScreenBounds.getHeight()*0.9);
			String css = this.getClass().getResource("application.css").toExternalForm();
			scene.getStylesheets().add(css);
			
			primaryStage.setScene(scene);
			primaryStage.setMaximized(true);
			primaryStage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

    @FXML
    void onExitBtnClick(ActionEvent event) {
    	System.exit(0);
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Create the Preferences class
		prefs = Preferences.userNodeForPackage(this.getClass());
		eightGradientsRadio.setToggleGroup(toggleGroup);
		singleGradientRadio.setToggleGroup(toggleGroup);
		toggleGroup.selectToggle(eightGradientsRadio);
		
	}

	@Override
	public void onNew() {
		if(isEightGradientsRadio){
			SaveData saveData = new SaveData();
	       	parentPaneController.loadSaveData(saveData);
		}
		else{
			InjectionSaveData saveData = new InjectionSaveData();
			injectionParentPaneController.loadSaveData(saveData);
		}
		currentFile = null;
		this.updateWindowTitle();
	}

	@Override
	public void onOpen() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open LC Database Builder File");
		
		fileChooser.getExtensionFilters().addAll(
	            new FileChooser.ExtensionFilter("LC Database Builder Files (*.lcdata)", "*.lcdata"),
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
				if(isEightGradientsRadio){
					parentPaneController.cancelAllTasks();
				}
				else injectionParentPaneController.cancelAllTasks();
				
				updateWindowTitle();
				prefs.put("LAST_OUTPUT_DIR", returnedFile.getAbsolutePath());
			}
		}	
		
	}

	@Override
	public void onSave() {
		if(isEightGradientsRadio){
			if (parentPaneController.areThreadsRunning())
			{
				FXOptionPane.showMessageDialog(primaryStage, "You cannot save while background calculations are running. Wait until the calculations are complete and then try again.", "Cannot Save Now", FXOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		else{
			if (injectionParentPaneController.areThreadsRunning())
			{
				FXOptionPane.showMessageDialog(primaryStage, "You cannot save while background calculations are running. Wait until the calculations are complete and then try again.", "Cannot Save Now", FXOptionPane.WARNING_MESSAGE);
				return;
			}
		}	
		if (currentFile == null)
		{
			onSaveAs();
		}
		writeToOutputStream();
				
		
	}

	@Override
	public void onSaveAs() {
		if(isEightGradientsRadio){
			if (parentPaneController.areThreadsRunning())
			{
				FXOptionPane.showMessageDialog(primaryStage, "You cannot save while background calculations are running. Wait until the calculations are complete and then try again.", "Cannot Save Now", FXOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		else{
			if (injectionParentPaneController.areThreadsRunning())
			{
				FXOptionPane.showMessageDialog(primaryStage, "You cannot save while background calculations are running. Wait until the calculations are complete and then try again.", "Cannot Save Now", FXOptionPane.WARNING_MESSAGE);
				return;
			}
		}

			
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save LC Database Builder File");
			
			fileChooser.getExtensionFilters().addAll(
	                new FileChooser.ExtensionFilter("LC Database Builder Files (*.lcdata)", "*.lcdata"),
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
				if (!returnedFile.getName().endsWith(".lcdata"))
					returnedFile = new File(returnedFile.getAbsolutePath() + ".lcdata");
				
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
		FXOptionPane.showMessageDialog(primaryStage, "LC Retention Database Builder version " + Integer.toString(majorVersion) + "." + Integer.toString(minorVersion) + "\n\nLearn more about LC Retention Database Builder at www.retentionprediction.org", "About LC Retention Database Builder", FXOptionPane.INFORMATION_MESSAGE);
	}
	
    public boolean readFromInputStream(File fileToRead)
    {
    	if (fileToRead != null)
    	{
    		try 
            {
                FileInputStream fis = new FileInputStream(fileToRead);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                if(isEightGradientsRadio){
                	saveData = (SaveData)ois.readObject();
                   	parentPaneController.loadSaveData(saveData);
                }
                else{
                	injectionSaveData = (InjectionSaveData)ois.readObject();
                	injectionParentPaneController.loadSaveData(injectionSaveData);
                }
                
    	        
                ois.close();
			} 
            catch (IOException | ClassNotFoundException e) 
            {
				e.printStackTrace();
	    		FXOptionPane.showMessageDialog(primaryStage, "The file is not a valid LC Retention Database Builder file.", "Not a Valid File", FXOptionPane.WARNING_MESSAGE);
		        return false;
			} 
    	}

    	currentFile = fileToRead;
    	this.updateWindowTitle();
    	
    	return true;
    }
    
	private boolean writeToOutputStream()
	{
    	try 
		{
            FileOutputStream fos = new FileOutputStream(currentFile, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            if(isEightGradientsRadio){
            	parentPaneController.writeSaveData(saveData);
            	oos.writeObject(saveData);
            }
            else{
            	injectionParentPaneController.writeSaveData(injectionSaveData);
            	oos.writeObject(injectionSaveData);
            }
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
