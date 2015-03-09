package boswell.fxoptionpane;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class MessageDialog 
{
	private MessageDialogController messageDialogController;
	private Stage dialog;
	private String message;
	private String windowTitle;
	private int optionType;
	private int retVal;
	
	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();

	MessageDialog(Window owner, String message, String windowTitle, int optionType)
	{
		this.message = message;
		this.windowTitle = windowTitle;
		this.optionType = optionType;
		
		dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		dialog.setResizable(false);
		FXMLLoader fxmlLoader = new FXMLLoader();
		try {
			GridPane root = (GridPane)fxmlLoader.load(getClass().getResource("MessageDialog.fxml").openStream());
			messageDialogController = fxmlLoader.getController();
			messageDialogController.setMessage(message);
			
			// Create the scene
			Scene scene = new Scene(root);//, 25 * rem, 10 * rem);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			dialog.setTitle(windowTitle);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(owner);
			dialog.setScene(scene);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void showDialog()
	{
		messageDialogController.setOptionType(optionType);
		messageDialogController.button1ActionProperty().set(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent arg0) {
				dialog.close();
			}
		});
		dialog.sizeToScene();
		
		// Show the dialog and wait for it to return
		dialog.showAndWait();
	}
	
	public int showConfirmDialog()
	{
		messageDialogController.setOptionType(optionType);
		messageDialogController.button1ActionProperty().set(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent arg0) {
				dialog.close();
				retVal = FXOptionPane.YES_OPTION;
			}
		});
		messageDialogController.button2ActionProperty().set(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent arg0) {
				dialog.close();
				retVal = FXOptionPane.NO_OPTION;
			}
		});
		dialog.sizeToScene();
		
		// Show the dialog and wait for it to return
		dialog.showAndWait();
		
		return retVal;
	}
}
