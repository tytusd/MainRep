package boswell.peakfinderlcfx;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Rectangle;

public class PeakFinderPaneController implements Initializable{

    @FXML private TableColumn<StandardCompound, String> columnRetentionTime;
    @FXML private TableColumn<StandardCompound, String> columnStandard;
    @FXML private TableColumn<StandardCompound, String> columnMZ;
    @FXML private TableColumn<Peak, String> columnPeakRank;
    @FXML private TableColumn<Peak, String> columnPeakFWHM;
    @FXML private TableColumn<Peak, String> columnPeakIntensity;
    @FXML private TableColumn<StandardCompound, String> columnPeak;
    @FXML private TableColumn<Peak, String> columnPeakRetentionTime;
    @FXML private LineChart<?, ?> chartZoomPlot;
    @FXML private LineChart<?, ?> chartExtractedIonChromatogram;

    @FXML private Button buttonPreviousStandard;
    @FXML private Button buttonNextStandard;
    @FXML private Button buttonCancel;

    @FXML private TableView<StandardCompound> tableStandards;
    @FXML private TableView<Peak> tablePeaks;
    @FXML private TitledPane titledPaneExtractedIonChromatogram;
    @FXML private TitledPane titledPaneChoosePeak;
    @FXML private CheckBox checkSkipThisOne;

    @FXML private Label labelExpectedPeakWidth;
    @FXML private Label labelFitScore;
    @FXML private Label labelExpectedRetentionTime;
    @FXML private NumberAxis axisZoomPlotTime;
    @FXML private NumberAxis axisExtractedIonChromatogramTime;
    @FXML private NumberAxis axisExtractedIonChromatogramIntensity;
    @FXML private NumberAxis axisZoomPlotIntensity;

    @FXML private Rectangle s;
    private final double rem = javafx.scene.text.Font.getDefault().getSize();
    private boolean okPressed = false;
    private ObservableList<StandardCompound> standardCompoundList;
    private ObservableList<Peak> peakList;
    private int iSelectedStandardIndex = 0;
    
    public class Peak
	{
		private StringProperty rank;
		private StringProperty retentionTime;
		private StringProperty fwhm;
		private StringProperty intensity;
				
		Peak(String rank, String retentionTime, String fwhm, String intensity)
		{
			this.rank = new SimpleStringProperty(rank);
			this.retentionTime = new SimpleStringProperty(retentionTime);
			this.fwhm = new SimpleStringProperty(fwhm);
			this.intensity = new SimpleStringProperty(intensity);
		}
				
		public String getRank()
		{
			return rank.get();
		}
		
		public void setRank(String rank)
		{
			this.rank.set(rank);
		}
		
		public StringProperty rankProperty() 
		{
		    return rank;
		}
		
		public String getRetentionTime()
		{
			return retentionTime.get();
		}
		
		public void setRetentionTime(String retentionTime)
		{
			this.retentionTime.set(retentionTime);
		}
		
		public StringProperty retentionTimeProperty() 
		{
		    return retentionTime;
		}

		public String getFwhm()
		{
			return fwhm.get();
		}
		
		public void setFwhm(String fwhm)
		{
			this.fwhm.set(fwhm);
		}
		
		public StringProperty fwhmProperty() 
		{
		    return fwhm;
		}

		public String getIntensity()
		{
			return intensity.get();
		}
		
		public void setIntensity(String intensity)
		{
			this.intensity.set(intensity);
		}
		
		public StringProperty intensityProperty() 
		{
		    return intensity;
		}
	}
    
    public class StandardCompound{
		private SimpleStringProperty name = new SimpleStringProperty();
		private SimpleStringProperty mz = new SimpleStringProperty();
		private SimpleStringProperty peak = new SimpleStringProperty();
		private SimpleStringProperty retentionTime = new SimpleStringProperty();
		
		StandardCompound(int index, String name, String mz, String peak, String retentionTime)
		{
			this.name.set(name);
			this.mz.set(mz);
			this.peak.set(peak);
			this.retentionTime.set(retentionTime);
		}
		
		public String getName()
		{
			return name.get();
		}
		
		public void setName(String name)
		{
			this.name.set(name);
		}
		
		public SimpleStringProperty nameProperty() 
		{
		    return name;
		}
		
		public String getMz()
		{
			return mz.get();
		}
		
		public void setMz(String mz)
		{
			this.mz.set(mz);
		}
		
		public SimpleStringProperty mzProperty() 
		{
		    return mz;
		}
		
		public String getPeak()
		{
			return peak.get();
		}
		
		public void setPeak(String peak)
		{
			this.peak.set(peak);
		}
		
		public SimpleStringProperty peakProperty() 
		{
		    return peak;
		}
		
		public String getRetentionTime()
		{
			return retentionTime.get();
		}
		
		public void setRetentionTime(String retentionTime)
		{
			this.retentionTime.set(retentionTime);
		}
		
		public SimpleStringProperty retentionTimeProperty() 
		{
		    return retentionTime;
		}
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		s.setWidth(rem);
		s.setHeight(rem);
		// Initialize the standards table
		List<StandardCompound> standardsData = new ArrayList<StandardCompound>();
		standardCompoundList = FXCollections.observableArrayList(standardsData);
		
	    columnStandard.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("name"));
	    columnMZ.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("mz"));
	    columnPeak.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("peak"));
	    columnRetentionTime.setCellValueFactory(new PropertyValueFactory<StandardCompound, String>("retentionTime"));
		
		tableStandards.setItems(standardCompoundList);
		
		tableStandards.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
			    {
			        @Override
			        public void onChanged(Change<? extends Integer> change)
			        {
			        	if (change.getList().size() > 0)
			        	{
			        		iSelectedStandardIndex = change.getList().get(0);
			        		selectNewStandard(iSelectedStandardIndex);
			        	}
			        }

			    });

		// Initialize the peaks table
		List<Peak> peakData = new ArrayList<Peak>();
		peakList = FXCollections.observableArrayList(peakData);
		
	    columnPeakRank.setCellValueFactory(new PropertyValueFactory<Peak, String>("rank"));
	    columnPeakRetentionTime.setCellValueFactory(new PropertyValueFactory<Peak, String>("retentionTime"));
	    columnPeakFWHM.setCellValueFactory(new PropertyValueFactory<Peak, String>("fwhm"));
	    columnPeakIntensity.setCellValueFactory(new PropertyValueFactory<Peak, String>("intensity"));

	    tablePeaks.setItems(peakList);
	    tablePeaks.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
			    {
			        @Override
			        public void onChanged(Change<? extends Integer> change)
			        {
			        	if (change.getList().size() > 0)
			        	{
			        		//selectNewPeak(change.getList().get(0));
			        	}
			        }

			    });
	    
	  
		
	}

	protected void selectNewStandard(int iSelectedStandardIndex) {
		// TODO Auto-generated method stub
		
	}

}
