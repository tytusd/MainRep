package org.retentionprediction.peakfindergcfx;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import boswell.fxoptionpane.FXOptionPane;
import boswell.graphcontrolfx.GraphControlFX;
import mr.go.sgfilter.SGFilter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PeakFinderPaneController implements Initializable {

	private Stage thisStage;

	private double[] rawEIC = null;
	private double[] smoothEIC = null;

	private double[][][] mzData = null;
	private double[] retentionTimes = null;
	
	private double[] sgcoefficients = null;
	private SGFilter sgfilter = null;

	private double[] selectedRetentionTimes = null;
	private boolean[] skippedStandards = null;
	private int[] selectedPeakRank = null;
	private double[] predictedRetentionTimes;
    private double[] predictedPeakWidths;
    private Vector<Vector<double[]>> peaks = new Vector<Vector<double[]>>();
    private int spectraCount;
    private double[] maxIntensities;
    
    private String[] stationaryPhaseArray = null;
    private String[] standardCompoundsNameArray = null;
    private double[][] standardCompoundsMZArray = null;
    
    private boolean okPressed = false;
    private ObservableList<StandardCompound> standardCompoundList;
    private ObservableList<Peak> peakList;

    @FXML private TableView<StandardCompound> tableStandards;
    @FXML private TableColumn<StandardCompound, String> columnStandard;
    @FXML private TableColumn<StandardCompound, String> columnMZ;
    @FXML private TableColumn<StandardCompound, String> columnPeak;
    @FXML private TableColumn<StandardCompound, String> columnRetentionTime;
    private int iSelectedStandardIndex = 0;
    
    @FXML private TableView<Peak> tablePeaks;
    @FXML private TableColumn<Peak, String> columnPeakRank;
    @FXML private TableColumn<Peak, String> columnPeakRetentionTime;
    @FXML private TableColumn<Peak, String> columnPeakFWHM;
    @FXML private TableColumn<Peak, String> columnPeakIntensity;
    
    @FXML private Button buttonCancel;
    @FXML private Button buttonNextStandard;
    @FXML private Button buttonPreviousStandard;
    @FXML private CheckBox checkSkipThisOne;
    @FXML private TitledPane titledPaneExtractedIonChromatogram;
    @FXML private TitledPane titledPaneChoosePeak;
    //@FXML private LineChart chartExtractedIonChromatogram;
    //@FXML private LineChart chartZoomPlot;
    //@FXML private NumberAxis axisExtractedIonChromatogramIntensity;
    //@FXML private NumberAxis axisExtractedIonChromatogramTime;
    //@FXML private NumberAxis axisZoomPlotIntensity;
    //@FXML private NumberAxis axisZoomPlotTime;
    @FXML private AnchorPane anchorPaneExtractedIonChromatogram;
    @FXML private AnchorPane anchorPanePeak;
    private GraphControlFX extractedIonChromatogramGraph;
    private GraphControlFX peakGraph;
    @FXML private Label labelExpectedRetentionTime;
    @FXML private Label labelExpectedPeakWidth;
    
    private boolean finalInitComplete = false;
    
	// For sizing
    private final double rem = javafx.scene.text.Font.getDefault().getSize();

    
	public class StandardCompound
	{
		private SimpleStringProperty name = new SimpleStringProperty();
		private SimpleStringProperty mz = new SimpleStringProperty();
		private SimpleStringProperty peak = new SimpleStringProperty();
		private SimpleStringProperty retentionTime = new SimpleStringProperty();
		//private int index;
		
		StandardCompound(int index, String name, String mz, String peak, String retentionTime)
		{
			//this.index = index;
			this.name.set(name);
			this.mz.set(mz);
			this.peak.set(peak);
			this.retentionTime.set(retentionTime);
		}
		
		//public int getIndex()
		//{
		//	return index;
		//}
		
		//public void setIndex(int index)
		//{
		//	this.index = index;
		//}
				
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
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
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
			        		selectNewPeak(change.getList().get(0));
			        	}
			        }

			    });
	    
	    extractedIonChromatogramGraph = new GraphControlFX();
	    extractedIonChromatogramGraph.setControlsEnabled(true);
	    extractedIonChromatogramGraph.setYAxisTitle("Intensity");
	    extractedIonChromatogramGraph.setYAxisBaseUnit("counts", "cnts");
	    extractedIonChromatogramGraph.setYAxisRangeLimits(-1E15d, 1E15d);
	    extractedIonChromatogramGraph.setYAxisScientificNotation(true);
	    extractedIonChromatogramGraph.setYAxisRangeIndicatorsVisible(false);
	    extractedIonChromatogramGraph.setAutoScaleY(true);
        
	    extractedIonChromatogramGraph.setXAxisType(true);
	    extractedIonChromatogramGraph.setXAxisRangeIndicatorsVisible(false);
	    extractedIonChromatogramGraph.setAutoScaleX(true);
	    extractedIonChromatogramGraph.setSelectionCursorVisible(true);

	    anchorPaneExtractedIonChromatogram.getChildren().add(extractedIonChromatogramGraph);
		AnchorPane.setTopAnchor(extractedIonChromatogramGraph, 0.0);
		AnchorPane.setBottomAnchor(extractedIonChromatogramGraph, 0.0);
		AnchorPane.setLeftAnchor(extractedIonChromatogramGraph, 0.0);
		AnchorPane.setRightAnchor(extractedIonChromatogramGraph, 0.0);
		
		extractedIonChromatogramGraph.widthProperty().bind(anchorPaneExtractedIonChromatogram.widthProperty().subtract(anchorPaneExtractedIonChromatogram.getPadding().getLeft() + anchorPaneExtractedIonChromatogram.getPadding().getRight()));
        extractedIonChromatogramGraph.heightProperty().bind(anchorPaneExtractedIonChromatogram.heightProperty().subtract(anchorPaneExtractedIonChromatogram.getPadding().getTop() + anchorPaneExtractedIonChromatogram.getPadding().getBottom()));

	    peakGraph = new GraphControlFX();
	    peakGraph.setControlsEnabled(true);
    	peakGraph.setYAxisTitle("Intensity");
	    peakGraph.setYAxisBaseUnit("counts", "cnts");
	    peakGraph.setYAxisScientificNotation(true);
	    peakGraph.setYAxisRangeLimits(-1E15d, 1E15d);
	    peakGraph.setYAxisRangeIndicatorsVisible(false);
	    peakGraph.setAutoScaleY(true);
        
	    peakGraph.setXAxisType(true);
	    peakGraph.setXAxisRangeIndicatorsVisible(false);
	    peakGraph.setAutoScaleX(true);

	    anchorPanePeak.getChildren().add(peakGraph);
		AnchorPane.setTopAnchor(peakGraph, 0.0);
		AnchorPane.setBottomAnchor(peakGraph, 0.0);
		AnchorPane.setLeftAnchor(peakGraph, 0.0);
		AnchorPane.setRightAnchor(peakGraph, 0.0);
		
		peakGraph.widthProperty().bind(anchorPanePeak.widthProperty());
		peakGraph.heightProperty().bind(anchorPanePeak.heightProperty());
	   
	}
	
	public void finalInit()
	{
		// Populate table with initial values
		for (int i = 0; i < standardCompoundsNameArray.length; i++)
		{
			String name = standardCompoundsNameArray[i];
			String mz = "";
			for (int j = 0; j < standardCompoundsMZArray[i].length; j++)
			{
				mz += standardCompoundsMZArray[i][j];
				if (j < standardCompoundsMZArray[i].length - 1)
					mz += ", ";
			}

			StandardCompound data = new StandardCompound(i, name, mz, "0", "0.0");
			standardCompoundList.add(data);
		}
		
		//this.updateStandardsTable();
		finalInitComplete = true;
		this.tableStandards.getSelectionModel().selectFirst();
		this.tablePeaks.getSelectionModel().selectFirst();
	}
	
    private void updateStandardsTable()
    {
		NumberFormat formatter = new DecimalFormat("#0.000");

		for (int i = 0; i < standardCompoundList.size(); i++)
    	{
    		// Check if this standard is skipped
    		if (this.skippedStandards[i])
    		{
    			standardCompoundList.get(i).setPeak("skip");
    			standardCompoundList.get(i).setRetentionTime("skip");
    		}
    		else if (this.selectedPeakRank[i] == -1)
    		{
    			standardCompoundList.get(i).setPeak("");
    			standardCompoundList.get(i).setRetentionTime("");
    		}
    		else
    		{
    			standardCompoundList.get(i).setPeak(((Integer)(selectedPeakRank[i] + 1)).toString());
    			standardCompoundList.get(i).setRetentionTime(formatter.format(selectedRetentionTimes[i]));
    		}
    	}
		
		//this.tableStandards.setVisible(false);
		//this.tableStandards.setVisible(true);
		//standardCompoundList.
		// TODO: calculate score?
		//double dScore = scorePermutation(this.m_iSelectedPeakRank);
		//contentPane.jlblOverallFitScore.setText(formatter.format(dScore));
    }
    
	public void setPeaksArray(Vector<Vector<double[]>> peaks)
	{
		this.peaks = peaks;
	}
	
	public void setSelectedRetentionTimes(double[] selectedRetentionTimes)
	{
		this.selectedRetentionTimes = selectedRetentionTimes;
	}
	
	public double[] getSelectedRetentionTimes()
	{
		return selectedRetentionTimes;
	}
	
	public void setSkippedStandards(boolean[] skippedStandards)
	{
		this.skippedStandards = skippedStandards;
	}
	
	public boolean[] getSkippedStandards()
	{
		return skippedStandards;
	}

	public void setSelectedPeakRank(int[] selectedPeakRank)
	{
		this.selectedPeakRank = selectedPeakRank;
	}
	
	public int[] getSelectedPeakRank()
	{
		return selectedPeakRank;
	}

	public void setMZArray(double[][] mzArray)
	{
		standardCompoundsMZArray = mzArray;
	}
	
	public void setCompoundNameArray(String[] nameArray)
	{
		standardCompoundsNameArray = nameArray;
	}
	
	public void setMzData(double[][][] mzData)
	{
		this.mzData = mzData;
	}
	
	public void setRetentionTimes(double[] retentionTimes)
	{
		this.retentionTimes = retentionTimes;
		spectraCount = retentionTimes.length;
	}
	
	public void setPredictedPeakWidths(double[] predictedPeakWidths)
	{
		this.predictedPeakWidths = predictedPeakWidths;
	}
	
	public void setPredictedRetentionTimes(double[] predictedRetentionTimes)
	{
		this.predictedRetentionTimes = predictedRetentionTimes;
	}
	
    public void selectNewStandard(int iIndexOfStandard)
    {
    	if (iIndexOfStandard < 0)
    		return;

    	if (!finalInitComplete)
    		return;
    	
		NumberFormat formatter = new DecimalFormat("#0.0");
		NumberFormat formatter2 = new DecimalFormat("#0.000");
		NumberFormat formatter3 = new DecimalFormat("#0.###E0");

		double dSelectedMasses[] = standardCompoundsMZArray[iIndexOfStandard];

		// Set the text of the "Next" button
    	if (iIndexOfStandard == this.standardCompoundList.size() - 1)
    	{
    		buttonNextStandard.setText("Finished");
    		//buttonNextStandard.setIcon(null);
    	}
    	else
    	{
    		buttonNextStandard.setText("Next Standard  ");
			//contentPane.jbtnNext.setIcon(new ImageIcon(getClass().getResource("/boswell/peakfindergc/images/forward.png")));
    	}
    	
    	if (iIndexOfStandard == 0)
    		buttonPreviousStandard.setDisable(true);
    	else
    		buttonPreviousStandard.setDisable(false);
    	    		
    	checkSkipThisOne.setSelected(this.skippedStandards[iIndexOfStandard]);
    	
   		String str = "";
   		for (int i = 0; i < dSelectedMasses.length; i++)
   		{
   			str += dSelectedMasses[i];
   			if (i < dSelectedMasses.length - 2)
   			{
   				str += ", ";
   			}
   			else if (i < dSelectedMasses.length - 1)
   			{
   				str += " and ";
   			}
   		}
   		titledPaneExtractedIonChromatogram.setText("Extracted ion chromatogram of " + str + " m/z");
   		titledPaneChoosePeak.setText("Choose the correct peak for " + this.standardCompoundsNameArray[iIndexOfStandard]);
        
    	// Create new data array for the EIC
        rawEIC = new double[spectraCount];
        double m_EICderivative[] = new double[spectraCount];
        double m_EICSecondDerivative[] = new double[spectraCount];
        double intensity;
        
        // This for loop is the part that is slow when you click on a standard.
        for (int iSpectrum = 0; iSpectrum < spectraCount; iSpectrum++)
		{
			// process all peaks by iterating over the m/z values
			intensity = 0;
      
			for (int i = 0; i < mzData[iSpectrum].length; i++) 
			{
				for (int j = 0; j < dSelectedMasses.length; j++)
				{
					if (mzData[iSpectrum][i][0] >= dSelectedMasses[j] - 0.5 && mzData[iSpectrum][i][0] <= dSelectedMasses[j] + 0.5)
					{
						intensity += mzData[iSpectrum][i][1];
					}
				}
			}

			rawEIC[iSpectrum] = intensity;
		}
        
        // Determine number of points to filter
        // # of points per second
        double dTotalTime = this.retentionTimes[this.retentionTimes.length - 1] - this.retentionTimes[0];
        double dPointsPerSecond = this.spectraCount / dTotalTime;
        double dPeakWidthInSeconds = this.predictedPeakWidths[iIndexOfStandard] * 60;
        double dK = 0.5;
        int dNumPointsInFilter = (int)(dK * dPeakWidthInSeconds * dPointsPerSecond);
        if (dNumPointsInFilter < 2)
        	dNumPointsInFilter = 2;
        
		int iOrder = 3;
		if (dNumPointsInFilter == 2)
			iOrder = 2;

		// Run Savitzky-Golay filter
		sgfilter = new SGFilter(dNumPointsInFilter, dNumPointsInFilter);
		sgcoefficients = SGFilter.computeSGCoefficients(dNumPointsInFilter, dNumPointsInFilter, iOrder);
		smoothEIC = sgfilter.smooth(rawEIC, sgcoefficients);
		for (int i = 0; i < 10; i++)
		{
			smoothEIC = sgfilter.smooth(smoothEIC, sgcoefficients);
		}
		
        // Set the expected retention time and peak width labels
        this.labelExpectedRetentionTime.setText(formatter2.format(predictedRetentionTimes[iIndexOfStandard]) + " min");
        this.labelExpectedPeakWidth.setText(formatter2.format(predictedPeakWidths[iIndexOfStandard]) + " min");
        
        // Clear the peak rank table
        this.peakList.clear();
        
        int iNumPeaksToShow = peaks.get(iIndexOfStandard).size();
        
        // Fill the peak rank table
        for (int i = 0; i < iNumPeaksToShow; i++)
        {
        	if (peaks.size() - 1 < i)
        		break;
        	
        	double dProbability = peaks.get(iIndexOfStandard).get(i)[4];
        	String strRank = ((Integer)(i + 1)).toString() + " (" + formatter.format(dProbability) + "%)";
        	
        	Peak newPeak = new Peak(strRank, 
        			formatter2.format(peaks.get(iIndexOfStandard).get(i)[0] / 60), 
        			formatter2.format(peaks.get(iIndexOfStandard).get(i)[2] / 60), 
        			formatter3.format(peaks.get(iIndexOfStandard).get(i)[1]));
        	
        	peakList.add(newPeak);
        }
        
        // Select the chosen peak
        if (peakList.size() > 0)
        {
        	if (selectedPeakRank[iIndexOfStandard] == -1)
        		this.tablePeaks.getSelectionModel().selectFirst();
        	else
        		this.tablePeaks.getSelectionModel().select(selectedPeakRank[iIndexOfStandard]);
        }
        	
        // Plot the data
		extractedIonChromatogramGraph.RemoveAllSeries();
        int iExtractedIonRawPlotIndex = extractedIonChromatogramGraph.AddSeries("Extracted ion chromatogram", Color.BLACK, 1, false, false);

        // Plot the data
        for (int i = 0; i < rawEIC.length; i++)
        {
        	extractedIonChromatogramGraph.AddDataPoint(iExtractedIonRawPlotIndex, retentionTimes[i], rawEIC[i]);
        }

        extractedIonChromatogramGraph.autoScaleX();
        extractedIonChromatogramGraph.autoScaleY();
		
        extractedIonChromatogramGraph.repaint();
    }
    
    public void selectNewPeak(int iSelectedPeak)
    {
    	if (iSelectedPeak < 0)
    		return;
    	
    	if (!finalInitComplete)
    		return;
    	
		NumberFormat formatter = new DecimalFormat("#0.000");
    	
    	if (iSelectedStandardIndex < 0)
    		return;
    	
    	// Determine range of data to plot in smaller graph
    	double dRetentionTime = peaks.get(iSelectedStandardIndex).get(iSelectedPeak)[0];
    	double dExpectedPeakWidth = predictedPeakWidths[iSelectedStandardIndex] * 60;
    	double dLeftWindow = dRetentionTime - (dExpectedPeakWidth * 6);
    	double dRightWindow = dRetentionTime + (dExpectedPeakWidth * 6);
    	if (dLeftWindow < 0)
    		dLeftWindow = 0;
    	if (dRightWindow > retentionTimes[retentionTimes.length - 1])
    	{
    		dRightWindow = retentionTimes[retentionTimes.length - 1];
    	}
    	
    	extractedIonChromatogramGraph.setLeftCursorPosition(dLeftWindow);
    	extractedIonChromatogramGraph.setRightCursorPosition(dRightWindow);
    	extractedIonChromatogramGraph.repaint();
    	
    	// Create new series for the plots
    	peakGraph.RemoveAllSeries();
    	int iExtractedIonRawPlotIndex = peakGraph.AddSeries("EIC", Color.BLACK, 1, false, false);
    	int iExtractedIonSmoothPlotIndex = peakGraph.AddSeries("EIC smoothed", Color.RED, 1, false, false);

    	// Plot the data
        for (int i = 0; i < rawEIC.length; i++)
        {
        	if (this.retentionTimes[i] >= dLeftWindow && retentionTimes[i] <= dRightWindow)
        	{
        		peakGraph.AddDataPoint(iExtractedIonRawPlotIndex, retentionTimes[i], rawEIC[i]);
        		peakGraph.AddDataPoint(iExtractedIonSmoothPlotIndex, retentionTimes[i], smoothEIC[i]);
        	}
        }

        peakGraph.removeAllLineMarkers();
        peakGraph.addLineMarker(dRetentionTime / 60, formatter.format(dRetentionTime / 60) + " min");
        peakGraph.autoScaleX();
        peakGraph.autoScaleY();
		
        peakGraph.repaint();
		
		selectedRetentionTimes[iSelectedStandardIndex] = dRetentionTime / 60;
		selectedPeakRank[iSelectedStandardIndex] = iSelectedPeak;
		
		updateStandardsTable();
    }
    
    @FXML private void onCheckSkipThisOne(ActionEvent event)
    {
    	int iSelectedRow = this.tableStandards.getSelectionModel().getSelectedIndex();
    	skippedStandards[iSelectedRow] = this.checkSkipThisOne.isSelected();
    	updateStandardsTable();
    }
    
    @FXML private void onCancelAction(ActionEvent event)
    {
    	okPressed = false;
    	thisStage.close();
    }
    
    @FXML private void onNextStandardAction(ActionEvent event)
    {
    	//this.buttonPreviousStandard.setDisable(false);
    	
    	int iSelectedRow = this.tableStandards.getSelectionModel().getSelectedIndex();
    	if (iSelectedRow < standardCompoundList.size() - 1)
    	{
    		this.tableStandards.getSelectionModel().select(iSelectedRow + 1);
    	}
    	if (iSelectedRow == standardCompoundList.size() - 1)
    	{
    		// Finished - copy values to software
    		
    		// First, check to see if all peaks were looked at
    		boolean bNotPickedYet = false;
    		for (int i = 0; i < selectedPeakRank.length; i++)
    		{
    			if (selectedPeakRank[i] == -1)
    				bNotPickedYet = true;
    		}
    		
    		if (bNotPickedYet)
    		{
	    		int ret = FXOptionPane.showConfirmDialog(thisStage, "You did not select a peak for at least one of the standards. Are you sure you want to continue?", "Are you sure you're done?", FXOptionPane.YES_NO_OPTION);
	    	
	    		if (ret == FXOptionPane.YES_OPTION)
	    		{
	    			okPressed = true;
	    			thisStage.close();
	    		}
    		}
    		else
    		{
    			okPressed = true;
    			thisStage.close();
    		}
    	}
    }
    
    @FXML private void onPreviousStandardAction(ActionEvent event)
    {
    	int iSelectedRow = this.tableStandards.getSelectionModel().getSelectedIndex();
    	
    	if (iSelectedRow > 0)
    		this.tableStandards.getSelectionModel().select(iSelectedRow - 1);
    }
    
	public void setStage(Stage stage)
	{
		thisStage = stage;
	}

	public boolean wasOkPressed()
	{
		return okPressed;
	}
}
