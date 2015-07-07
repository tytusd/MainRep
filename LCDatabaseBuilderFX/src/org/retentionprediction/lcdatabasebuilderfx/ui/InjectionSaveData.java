package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.retentionprediction.lcdatabasebuilderfx.business.Globals;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InjectionSaveData implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final long classVersion = 1L;

	MeasuredRetentionTimeSaveData measuredRetentionTimeSaveData;
	BackCalculateSaveData backCalculateSaveData;

	// For ParentPaneController
	int iCurrentStep;
	boolean finalFitComplete;
	ObservableList<StandardCompound> programList;
	String labelAZeroText;
	String labelAOneText;
	String labelATwoText;
	String labelBOneText;
	String labelBTwoText;
	String labelIterationText;
	String labelVarianceText;
	String labelTimeElapsedText;
	String statusText;
	
	InjectionSaveData()
	{
		// Load with default values
		programList = FXCollections.observableArrayList();
		iCurrentStep = 0;
		finalFitComplete = false;
		labelAOneText = "";
		labelATwoText = "";
		labelAZeroText = "";
		labelBOneText = "";
		labelBTwoText = "";
		labelIterationText = "";
		labelVarianceText = "";
		labelTimeElapsedText = "";
		statusText = "Click the button below to solve for \u0394H, \u0394S, and \u0394Cp";
		
		measuredRetentionTimeSaveData = new MeasuredRetentionTimeSaveData();
		backCalculateSaveData = new BackCalculateSaveData();

		measuredRetentionTimeSaveData = new MeasuredRetentionTimeSaveData();
		measuredRetentionTimeSaveData.programName = "Gradient A";
		backCalculateSaveData = new BackCalculateSaveData();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeLong(SaveData.classVersion);
		
		out.writeObject(measuredRetentionTimeSaveData);
		out.writeObject(backCalculateSaveData);

		out.writeObject(iCurrentStep);
		out.writeBoolean(finalFitComplete);
		StandardCompound[] x = programList.toArray(new StandardCompound[programList.size()]);
		out.writeObject(x);
		out.writeObject(labelAZeroText);
		out.writeObject(labelAOneText);
		out.writeObject(labelATwoText);
		out.writeObject(labelBOneText);
		out.writeObject(labelBTwoText);
		out.writeObject(labelIterationText);
		out.writeObject(labelVarianceText);
		out.writeObject(labelTimeElapsedText);
		out.writeObject(statusText);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		long version = in.readLong();
		
		if (version >= 1)
		{
			measuredRetentionTimeSaveData = (MeasuredRetentionTimeSaveData)in.readObject();
			backCalculateSaveData = (BackCalculateSaveData)in.readObject();
			
			iCurrentStep = (int)in.readObject();
			finalFitComplete = in.readBoolean();
			programList = FXCollections.observableArrayList((StandardCompound[])in.readObject());
			labelAZeroText = (String)in.readObject();
			labelAOneText = (String)in.readObject();
			labelATwoText = (String)in.readObject();
			labelBOneText = (String)in.readObject();
			labelBTwoText = (String)in.readObject();
			labelIterationText = (String)in.readObject();
			labelVarianceText = (String)in.readObject();
			labelTimeElapsedText = (String)in.readObject();
			statusText = (String)in.readObject();
		}
	}
	
	class BackCalculateSaveData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private static final long classVersion = 1;
		
		double columnLength;
		double innerDiameter;
		double flowRate;
		double instrumentDeadTime;
		String fileName;
		ObservableList<StandardCompound> standardsList;
		double[][] gradientProgramInConventionalForm;
		double tStep;
		double[][] m_dIdealGradientProfileArray;
		double[][] m_dGradientProfileDifferenceArray;
		double[][] m_dSimpleGradientProfileArray;
		double[][] m_dDeadTimeArray;
		double[] m_dExpectedErrorArray;
		double[][] m_dDeadTimeDifferenceArray;
		int status;
		double score;
	    
	    // Step3Pane stuff
		String labelIterationText;
		String labelVarianceText;
		String labelLastIterationVarianceText;
		String labelPercentImprovementText;
		String labelTimeElapsedText;
		String labelStatusText;
		Number progressBarValue;
		boolean backCalculationButtonDisabled;
	    
	    // Step4Pane stuff - most is updated with updateTestCompoundTable()
		ObservableList<StandardCompound> testCompoundList;
		
		BackCalculateSaveData()
		{
			columnLength = 30;
			innerDiameter = 0.25;
			flowRate = 1.0;
			fileName = "";
			standardsList = FXCollections.observableArrayList();
			gradientProgramInConventionalForm = new double[][]{{0,5},{60,95}};
			m_dIdealGradientProfileArray = null;
			m_dGradientProfileDifferenceArray = null;
			m_dSimpleGradientProfileArray = null;
			m_dDeadTimeDifferenceArray = null;
			m_dDeadTimeArray = null;
			m_dExpectedErrorArray = null;
			instrumentDeadTime = 0;
			status = 0;
			score = 0;
			
			labelIterationText = "";
			labelVarianceText = "";
			labelLastIterationVarianceText = "";
			labelPercentImprovementText = "";
			labelTimeElapsedText = "";
			labelStatusText = "Click \"Back-calculate profiles\" to begin the optimization";
			progressBarValue = 0;
			backCalculationButtonDisabled = false;
			
			// Populate the table with the test compounds
		    testCompoundList = FXCollections.observableArrayList();
			for (int i = 0; i < Globals.TestCompoundNameArray.length; i++)
		    {
		    	StandardCompound newTestCompound = new StandardCompound();
		    	newTestCompound.setIndex(i);
		    	newTestCompound.setName(Globals.TestCompoundNameArray[i]);
		    	newTestCompound.setMz(Globals.convertMZToString(Globals.TestCompoundMZArray[i]));
		    	testCompoundList.add(newTestCompound);
		    }
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException
		{
			out.writeLong(BackCalculateSaveData.classVersion);
			
			out.writeDouble(columnLength);
			out.writeDouble(innerDiameter);
			out.writeDouble(flowRate);
			out.writeDouble(instrumentDeadTime);
			out.writeObject(fileName);
			StandardCompound[] x = standardsList.toArray(new StandardCompound[0]);
			out.writeObject(x);
			out.writeObject(gradientProgramInConventionalForm);
			out.writeDouble(tStep);
		    out.writeObject(m_dIdealGradientProfileArray);
		    out.writeObject(m_dGradientProfileDifferenceArray);
		    out.writeObject(m_dSimpleGradientProfileArray);
		    out.writeObject(m_dDeadTimeDifferenceArray);
		    out.writeObject(m_dDeadTimeArray);
		    out.writeObject(m_dExpectedErrorArray);
		    out.writeInt(status);
		    out.writeDouble(score);
		    
		    // Step3Pane stuff
		    out.writeObject(labelIterationText);
		    out.writeObject(labelVarianceText);
		    out.writeObject(labelLastIterationVarianceText);
		    out.writeObject(labelPercentImprovementText);
		    out.writeObject(labelTimeElapsedText);
		    out.writeObject(labelStatusText);
		    out.writeObject(progressBarValue);
		    out.writeBoolean(backCalculationButtonDisabled);
		    
		    // Step4Pane stuff - most is updated with updateTestCompoundTable()
			StandardCompound[] y = testCompoundList.toArray(new StandardCompound[0]);
		    out.writeObject(y);
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			long version = in.readLong();
			
			if (version >= 1)
			{
				columnLength = in.readDouble();
				innerDiameter = in.readDouble();
				flowRate = in.readDouble();
				instrumentDeadTime = in.readDouble();
				fileName = (String)in.readObject();
				standardsList = FXCollections.observableArrayList((StandardCompound[])in.readObject());
				gradientProgramInConventionalForm = (double[][])in.readObject();
				tStep = in.readDouble();
				m_dIdealGradientProfileArray = (double[][])in.readObject();
		    	m_dGradientProfileDifferenceArray = (double[][])in.readObject();
				m_dSimpleGradientProfileArray = (double[][])in.readObject();
				m_dDeadTimeDifferenceArray = (double[][]) in.readObject();
				m_dDeadTimeArray = (double[][])in.readObject();
				m_dExpectedErrorArray = (double[])in.readObject();
				status = in.readInt();
				score = in.readDouble();
				
			    // Step3Pane stuff
				labelIterationText = (String)in.readObject();
				labelVarianceText = (String)in.readObject();
				labelLastIterationVarianceText = (String)in.readObject();
				labelPercentImprovementText = (String)in.readObject();
				labelTimeElapsedText = (String)in.readObject();
				labelStatusText = (String)in.readObject();
				progressBarValue = (Number)in.readObject();
				backCalculationButtonDisabled = in.readBoolean();

				// Step4Pane stuff
				testCompoundList = FXCollections.observableArrayList((StandardCompound[])in.readObject());
			}
		}
	}
	
	class MeasuredRetentionTimeSaveData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private static final long classVersion = 1;
		
		String programName;
		String fileName;
		ObservableList<StandardCompound> standardsList;
		double columnLength;
		double innerDiameter;
		double flowRate;
		double instrumentDeadTime;
		double[][] gradientProgramInConventionalForm;
		MeasuredRetentionTimeSaveData()
		{
			gradientProgramInConventionalForm = new double[][]{{0,5},{60,95}};
			programName = "Gradient A";
			fileName = "";
			// Load the measured retention times table with the correct values
			List<StandardCompound> data = new ArrayList<StandardCompound>();
			for (int i = 0; i < Globals.StandardCompoundsNameArray.length; i++)
			{
				StandardCompound newItem = new StandardCompound();
				newItem.setUse(false);
				newItem.setName(Globals.StandardCompoundsNameArray[i]);
				newItem.setMz(Globals.convertMZToString(Globals.StandardCompoundsMZArray[i]));
				newItem.setMeasuredRetentionTime(0.0);
				newItem.setIndex(i);
				
				data.add(newItem);
			}
			standardsList = FXCollections.observableArrayList(data);
			columnLength = 30;
			innerDiameter = 0.25;
			flowRate = 1;
			instrumentDeadTime = 0.125;
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException
		{
			out.writeLong(MeasuredRetentionTimeSaveData.classVersion);
			out.writeObject(gradientProgramInConventionalForm);
			out.writeObject(programName);
			out.writeObject(fileName);
			StandardCompound[] x = standardsList.toArray(new StandardCompound[0]);
			out.writeObject(x);
			out.writeDouble(columnLength);
			out.writeDouble(innerDiameter);
			out.writeDouble(flowRate);
			out.writeDouble(instrumentDeadTime);
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			long version = in.readLong();
			
			if (version >= 1)
			{
				gradientProgramInConventionalForm = (double[][])in.readObject();
				programName = (String)in.readObject();
				fileName = (String)in.readObject();
				StandardCompound[] x = (StandardCompound[])in.readObject();
				standardsList = FXCollections.observableArrayList(x);
				columnLength = in.readDouble();
				innerDiameter = in.readDouble();
				flowRate = in.readDouble();
				instrumentDeadTime = in.readDouble();
			}
		}
	}
}
