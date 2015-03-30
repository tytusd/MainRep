package boswell.peakfinderlcfx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ProgressDialogController implements Initializable
{
	@FXML Label labelProgress;
	@FXML ProgressBar progressBar;
	@FXML Button buttonCancel;
	
	BooleanProperty cancelledProperty = new SimpleBooleanProperty(false);
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		progressBar.setProgress(0);
	}
	
	@FXML private void onCancelAction(ActionEvent e)
	{
		cancelledProperty.set(true);
	}
	
	public DoubleProperty progressProperty()
	{
		return progressBar.progressProperty();
	}
	
	public StringProperty messageProperty()
	{
		return labelProgress.textProperty();
	}
	
	public BooleanProperty getCancelProperty()
	{
		return cancelledProperty;
	}
}
