package org.retentionprediction.gcdatabasebuilderfx;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.retentionprediction.peakfindergcfx.PeakFinderSettingsPaneController.TemperatureProgramStep;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

// This controller doesn't do anything but provide inputs/outputs

public class StepThreePaneController implements Initializable 
{
    @FXML private TableView<StandardCompound> tableStandards;
    @FXML private TableColumn<StandardCompound, String> columnName;
    @FXML private TableColumn<StandardCompound, String> columnExperimentalRetentionTime;
    @FXML private TableColumn<StandardCompound, String> columnCalculatedRetentionTime;
    @FXML private TableColumn<StandardCompound, String> columnDifference;
    
    @FXML private Label labelIteration;
    @FXML private Label labelVariance;
    @FXML private Label labelLastIterationVariance;
    @FXML private Label labelPercentImprovement;
    @FXML private Label labelTimeElapsed;
    @FXML private Label labelStatus;
    
    @FXML private ProgressBar progressBar;
    @FXML private Button buttonBackCalculate;
    
    private ObservableList<StandardCompound> standardCompoundList = FXCollections.observableArrayList();;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
	    columnName.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("name"));
	    //columnExperimentalRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound, Double>("measuredRetentionTime"));
	    columnExperimentalRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("measuredRetentionTimeString"));
/*	    columnExperimentalRetentionTime.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>()
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

	    /*

	    columnCalculatedRetentionTime.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>()
	    {
	    	SimpleStringProperty stringProperty = new SimpleStringProperty("");

	    	@Override
	        public ObservableValue<String> call(TableColumn.CellDataFeatures<StandardCompound, String> p) 
	        {
	            if (p.getValue() != null) 
	            {
	            	float value = (float)p.getValue().getPredictedRetentionTime();
					if (value < 0)
					{
						stringProperty.set("Did not elute");
						return stringProperty;
					}
					else if (value == 0)
					{
						stringProperty.set("");
						return stringProperty;
					}
					
					NumberFormat formatter = new DecimalFormat("#0.0000");
					String retVal = formatter.format(value);
					stringProperty.set(retVal);
					return stringProperty;
	            } 
	            else 
	            {
	            	stringProperty.set("");
	                return stringProperty;
	            }
	    		//if (p.getValue() != null)
	    		{
					NumberFormat formatter = new DecimalFormat("#0.0000");
					String retVal = formatter.format(p.getValue().getPredictedRetentionTime());
					stringProperty.set(retVal);

	    			return stringProperty;
	    		}
	        }
	    });
	    
	    /*columnDifference.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<StandardCompound, String>, ObservableValue<String>>() 
	    {
	    	SimpleStringProperty stringProperty = new SimpleStringProperty("");

	    	@Override
	        public ObservableValue<String> call(TableColumn.CellDataFeatures<StandardCompound, String> p) 
	        {
	            if (p.getValue() != null) 
	            {
	            	float value = (float)p.getValue().getPredictedRetentionTime();
					if (value < 0)
					{
						stringProperty.set("-");
						return stringProperty;
					}
					else if (value == 0)
					{
						stringProperty.set("");
						return stringProperty;
					}
					
					value = (float)p.getValue().getError();
					NumberFormat formatter = new DecimalFormat("#0.0000");
					String retVal = formatter.format(value);
					stringProperty.set(retVal);
					return stringProperty;
	            } 
	            else 
	            {
	            	stringProperty.set("");
	                return stringProperty;
	            }
	        }
	    });*/
		
		tableStandards.setItems(standardCompoundList);
		
		labelStatus.setText("Click \"Back-calculate profiles\" to begin the optimization.");
	}

	public ObservableList<StandardCompound> getStandardCompoundList()
	{
		return this.standardCompoundList;
	}
	
	public void setStandardCompoundList(ObservableList<StandardCompound> standardsList)
	{
		this.standardCompoundList = standardsList;
		tableStandards.setItems(this.standardCompoundList);
	}
	
	public StringProperty iterationLabelProperty()
	{
		return labelIteration.textProperty();
	}
	
	public StringProperty lastIterationVarianceLabelProperty()
	{
		return labelLastIterationVariance.textProperty();
	}
	
	public StringProperty percentImprovementLabelProperty()
	{
		return labelPercentImprovement.textProperty();
	}
	
	public StringProperty varianceLabelProperty()
	{
		return labelVariance.textProperty();
	}
	
	public StringProperty timeElapsedLabelProperty()
	{
		return labelTimeElapsed.textProperty();
	}
	
	public DoubleProperty progressBarProperty()
	{
		return progressBar.progressProperty();
	}
	
	public StringProperty statusLabelProperty()
	{
		return labelStatus.textProperty();
	}
	
	public ObjectProperty<EventHandler<? super MouseEvent>> backCalculateOnMouseClickedProperty()
	{
		return buttonBackCalculate.onMouseClickedProperty();
	}
	
	@FXML private void onBackCalculateAction(ActionEvent event)
	{
		
	}
	
	public void setBackCalculationButtonDisable(boolean value)
	{
		this.buttonBackCalculate.setDisable(value);
	}

	public boolean isBackCalculationButtonDisabled()
	{
		return this.buttonBackCalculate.isDisabled();
	}
}
