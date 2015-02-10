package org.retentionprediction.gcdatabasebuilderfx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

// This controller doesn't do anything but provide inputs/outputs

public class StepFourPaneController implements Initializable 
{
	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();

    @FXML private TableView<StandardCompound> tableTestCompounds;
    @FXML private TableColumn<StandardCompound, String> columnName;
    @FXML private TableColumn<StandardCompound, String> columnMZ;
    @FXML private TableColumn<StandardCompound, String> columnExperimentalRetentionTime;
    @FXML private TableColumn<StandardCompound, String> columnCalculatedRetentionTime;
    @FXML private TableColumn<StandardCompound, String> columnDifference;
    
    @FXML private Label labelOverallPredictionError;
    @FXML private Label labelMostLikelyError;
    @FXML private Label labelColumnRating;
    
    @FXML private Button buttonFindRetentionTimesAutomatically;
    
    @FXML private AnchorPane anchorPaneSuitabilityControl;
    
    private SliderIndicator sliderIndicator = new SliderIndicator();
    
    private ObservableList<StandardCompound> testCompoundList = FXCollections.observableArrayList();

    private StepFourPaneControllerListener stepFourPaneControllerListener;

	public interface StepFourPaneControllerListener 
	{
		public void onTableUpdate();
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		anchorPaneSuitabilityControl.getChildren().add(sliderIndicator);
		AnchorPane.setTopAnchor(sliderIndicator, 0.0);
		AnchorPane.setBottomAnchor(sliderIndicator, 0.0);
		AnchorPane.setLeftAnchor(sliderIndicator, 0.0);
		AnchorPane.setRightAnchor(sliderIndicator, 0.0);
		
		sliderIndicator.widthProperty().bind(anchorPaneSuitabilityControl.widthProperty().subtract(rem));
		sliderIndicator.heightProperty().bind(anchorPaneSuitabilityControl.heightProperty().subtract(rem));

		sliderIndicator.setShowIndicator(false);
		
		float fScaleFactor = (float)javafx.scene.text.Font.getDefault().getSize() / 15;
		anchorPaneSuitabilityControl.setMinHeight(fScaleFactor * 60);

	    columnName.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("name"));
	    columnMZ.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("mz"));
	    columnExperimentalRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("measuredRetentionTimeString"));
	    columnExperimentalRetentionTime.setCellFactory(TextFieldTableCell.<StandardCompound>forTableColumn());
	    /*columnExperimentalRetentionTime.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>()
	    {
	    	@Override
	        public ObservableValue<String> call(TableColumn.CellDataFeatures<StandardCompound, String> p) 
	        {
	            if (p.getValue() != null) 
	            {
	            	return p.getValue().measuredRetentionTimeStringBindingObservableValue();
	            }
	            else 
	            {
	            	return null;
	            }
	        }
	    });*/
	    
	    /*columnExperimentalRetentionTime.setCellFactory(TextFieldTableCell.<StandardCompound,String>forTableColumn(new StringConverter<String>(){
			@Override
			public String fromString(String arg0) {
				double dTemp = 0;
		    	try
		    	{
		    		dTemp = (double)Float.valueOf(arg0);
		    	}
		    	catch (NumberFormatException e)
		    	{
		    		dTemp = 0.0;
		    	}
				return dTemp;
			}

			@Override
			public String toString(String arg0) {
            	float value = arg0.floatValue();
				NumberFormat formatter = new DecimalFormat("#0.0000");
				String retVal = formatter.format(value);
				return retVal;
			}
		
		}));*/
	    
	    columnCalculatedRetentionTime.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>()
	    {
	    	@Override
	        public ObservableValue<String> call(TableColumn.CellDataFeatures<StandardCompound, String> p) 
	        {
	            if (p.getValue() != null) 
	            {
	            	return p.getValue().predictedRetentionTimeStringBindingObservableValue();
	            } 
	            else 
	            {
	            	return null;
	            }
	        }
	    });
	    columnDifference.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>()
	    {
	    	@Override
	        public ObservableValue<String> call(TableColumn.CellDataFeatures<StandardCompound, String> p) 
	        {
	            if (p.getValue() != null) 
	            {
	            	return p.getValue().errorStringBindingObservableValue();
	            } 
	            else 
	            {
	            	return null;
	            }
	        }
	    });
	    
	    // Populate the table with the test compounds
	    for (int i = 0; i < Globals.TestCompoundNameArray.length; i++)
	    {
	    	StandardCompound newTestCompound = new StandardCompound();
	    	newTestCompound.setIndex(i);
	    	newTestCompound.setName(Globals.TestCompoundNameArray[i]);
	    	newTestCompound.setMz(Globals.convertMZToString(Globals.TestCompoundMZArray[i]));
	    	testCompoundList.add(newTestCompound);
	    }
	    
	    tableTestCompounds.setItems(testCompoundList);
	}

	public void setStep4PaneControllerListener(StepFourPaneControllerListener thisListener)
	{
		stepFourPaneControllerListener = thisListener;
	}

	@FXML private void onCommitRetentionTime(TableColumn.CellEditEvent<StandardCompound,String> t)
	{
		Double dNewRetentionTime = 0.0;
		try
		{
			// TODO: More sophisticated?
			dNewRetentionTime = Double.valueOf(t.getNewValue());
		}
		catch (NumberFormatException e)
		{
			dNewRetentionTime = 0.0;
		}
		
		// Get the current item that was changed
		StandardCompound currentItem = (StandardCompound)t.getTableView().getItems().get(t.getTablePosition().getRow());

		// Commit the new retention time
		currentItem.setMeasuredRetentionTime(dNewRetentionTime);
		
		// Send notification back to the ParentPaneController to change the visible pane
		if (stepFourPaneControllerListener != null)
			this.stepFourPaneControllerListener.onTableUpdate();

	}
	
	public ObservableList<StandardCompound> getTestCompoundList()
	{
		return this.testCompoundList;
	}
	
	public StringProperty overallErrorLabelProperty()
	{
		return labelOverallPredictionError.textProperty();
	}
	
	public StringProperty mostLikelyErrorLabelProperty()
	{
		return labelMostLikelyError.textProperty();
	}
	
	public StringProperty columnRatingLabelProperty()
	{
		return labelColumnRating.textProperty();
	}
		
	public void setTestCompoundList(ObservableList<StandardCompound> testCompoundList)
	{
		this.testCompoundList = testCompoundList;
		tableTestCompounds.setItems(this.testCompoundList);
	}
	
	public ObjectProperty<EventHandler<? super MouseEvent>> buttonFindRetentionTimesOnMouseClickedProperty()
	{
		return buttonFindRetentionTimesAutomatically.onMouseClickedProperty();
	}
	
	public void setSliderIndicatorVisible(boolean visible)
	{
		sliderIndicator.setShowIndicator(visible);
	}
	
	public void setRatingColor(Color color)
	{
		this.labelColumnRating.setTextFill(color);
	}
	
	public void setSliderYellowLimit(float yellowLimit)
	{
		sliderIndicator.setYellowLimit(yellowLimit);
	}
	
	public void setSliderPosition(float position)
	{
		sliderIndicator.setPosition(position);
	}
}
