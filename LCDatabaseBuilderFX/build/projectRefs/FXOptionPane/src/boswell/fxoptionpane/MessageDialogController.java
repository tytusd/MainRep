package boswell.fxoptionpane;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MessageDialogController implements Initializable 
{
	@FXML ImageView imagePic;
	@FXML Label labelMessage;
	@FXML Button button1;
	@FXML Button button2;
	
	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imagePic.setFitHeight(rem * 2.5);
		imagePic.setFitWidth(rem * 2.5);
		labelMessage.setPrefWidth(rem * 30);
	}
	
	public void setMessage(String message)
	{
		labelMessage.setText(message);
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> button1ActionProperty()
	{
		return button1.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> button2ActionProperty()
	{
		return button2.onActionProperty();
	}

	public void setOptionType(int optionType)
	{
		if (optionType == FXOptionPane.INFORMATION_MESSAGE)
		{
			Image image = new Image("boswell/fxoptionpane/information-icon.png");
			imagePic.setImage(image);
			button1.setText("Ok");
			button2.setVisible(false);
		}
		else if (optionType == FXOptionPane.WARNING_MESSAGE)
		{
			Image image = new Image("boswell/fxoptionpane/warning-icon.png");
			imagePic.setImage(image);
			button1.setText("Ok");
			button2.setVisible(false);
		}
		else if (optionType == FXOptionPane.YES_NO_OPTION)
		{
			Image image = new Image("boswell/fxoptionpane/help-icon.png");
			imagePic.setImage(image);
			button1.setText("Yes");
			button2.setText("No");
		}
			
	}

}
