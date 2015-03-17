package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

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
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainController implements Initializable, ParentPaneControllerListener{

    //Fields for this class
	@FXML private RadioButton eightGradientsRadio;
    @FXML private RadioButton singleGradientRadio;
    @FXML private Button exitBtn;
    @FXML private Button okBtn;
    private final ToggleGroup toggleGroup = new ToggleGroup();
    
    //Fields for ParentPaneController
    private ParentPaneControllerListener parentPaneControllerListener;
    private ParentPaneController parentPaneController;
    private Stage primaryStage;
    
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
    		if(toggleGroup.getSelectedToggle().equals(eightGradientsRadio)){
    			InputStream stream = getClass().getResource("ParentPane.fxml").openStream();
    			root = fxmlLoader.load(stream);
    			parentPaneController = fxmlLoader.getController();
    			parentPaneController.setParentPaneControllerListener(parentPaneControllerListener);
    		}
    		else{
    			//Singlegradientpane
    		}
			
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
			Scene scene = new Scene(root, primaryScreenBounds.getWidth()*0.8, primaryScreenBounds.getHeight()*0.9);
			String css = this.getClass().getResource("application.css").toExternalForm();
			scene.getStylesheets().add(css);
			
			primaryStage.setScene(scene);
			//primaryStage.setMaximized(true);
			primaryStage.show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    @FXML
    void onExitBtnClick(ActionEvent event) {
    	System.exit(0);
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		eightGradientsRadio.setToggleGroup(toggleGroup);
		singleGradientRadio.setToggleGroup(toggleGroup);
		toggleGroup.selectToggle(eightGradientsRadio);
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAbout() {
		// TODO Auto-generated method stub
		
	}

	public ParentPaneControllerListener getParentPaneControllerListener() {
		return parentPaneControllerListener;
	}

	public void setParentPaneControllerListener(
			ParentPaneControllerListener parentPaneControllerListener) {
		this.parentPaneControllerListener = parentPaneControllerListener;
	}

}
