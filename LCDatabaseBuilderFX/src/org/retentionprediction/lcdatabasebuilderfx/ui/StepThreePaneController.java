package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class StepThreePaneController implements Initializable{
	

    @FXML private TableColumn<StandardCompound, String> columnExperimentalRetentionTime;
    @FXML private TableColumn<StandardCompound, String> columnDifference;
    @FXML private TableView<StandardCompound> tableStandards;
    @FXML private TableColumn<StandardCompound, String> columnName;
    @FXML private TableColumn<StandardCompound, String> columnCalculatedRetentionTime;
    
    
    @FXML private Label labelStatus;
    @FXML private Label labelIteration;
    @FXML private Label labelPercentImprovement;
    @FXML private Label labelLastIterationVariance;
    @FXML private Label labelTimeElapsed;
    @FXML private Label labelVariance;
    
    @FXML private TitledPane step3pane;

    @FXML private ProgressBar progressBar;
    
    @FXML private Button buttonBackCalculate;

    private ObservableList<StandardCompound> standardCompoundList = FXCollections.observableArrayList();
    
    @FXML
    void onBackCalculateAction(ActionEvent event) {

    }

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

	public ObservableList<StandardCompound> getStandardCompoundList() {
		return standardCompoundList;
	}

	public void setStandardCompoundList(ObservableList<StandardCompound> standardCompoundList) {
		this.standardCompoundList = standardCompoundList;
	}

}
