package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.retentionprediction.lcdatabasebuilderfx.ui.BackcalculateController;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;

public class BackcalculateController implements Initializable{

	public BackCalculateControllerListener backcalculateListener;
	private TitledPane step3Pane;
	private StepThreePaneController stepThreePaneController;
	
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

}
