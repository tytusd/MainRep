package org.retentionprediction.gcdatabasebuilderfx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SaveData implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final long classVersion = 1L;

	MeasuredRetentionTimeSaveData measuredRetentionTimeSaveData[];
	BackCalculateSaveData backCalculateSaveData[];

	// For ParentPaneController
	int[] iCurrentStep;
	boolean finalFitComplete;
	ObservableList<StandardCompound> programList;
	String labelHText;
	String labelSText;
	String labelCpText;
	String labelIterationText;
	String labelVarianceText;
	String labelTimeElapsedText;
	String statusText;
	String compoundName;
	String formula;
	String pubChemID;
	String cAS;
	String nISTID;
	String hMDB;
	String retentionTimeA;
	String retentionTimeB;
	String retentionTimeC;
	String retentionTimeD;
	String retentionTimeE;
	String retentionTimeF;
	
	SaveData()
	{
		// Load with default values
		
		iCurrentStep = new int[]{0, 0, 0, 0, 0, 0};
		finalFitComplete = false;
		programList = FXCollections.observableArrayList();
	    for (int i = 0; i < 6; i++)
	    {
	    	StandardCompound newTestCompound = new StandardCompound();
	    	newTestCompound.setIndex(i);
	    	newTestCompound.setName(Globals.requiredTemperatureProgramNames[i]);
	    	programList.add(newTestCompound);
	    }
		labelHText = "";
		labelSText = "";
		labelCpText = "";
		labelIterationText = "";
		labelVarianceText = "";
		labelTimeElapsedText = "";
		statusText = "Click the button below to solve for \u0394H, \u0394S, and \u0394Cp";
		compoundName = "";
		formula = "";
		pubChemID = "";
		cAS = "";
		nISTID = "";
		hMDB = "";
		retentionTimeA = "";
		retentionTimeB = "";
		retentionTimeC = "";
		retentionTimeD = "";
		retentionTimeE = "";
		retentionTimeF = "";		

		measuredRetentionTimeSaveData = new MeasuredRetentionTimeSaveData[6];
		backCalculateSaveData = new BackCalculateSaveData[6];
		
		for (int i = 0; i < 6; i++)
		{
			measuredRetentionTimeSaveData[i] = new MeasuredRetentionTimeSaveData();
			measuredRetentionTimeSaveData[i].initialTemperature = Globals.requiredTemperaturePrograms[i][0][0];
			measuredRetentionTimeSaveData[i].initialHoldTime = Globals.requiredTemperaturePrograms[i][0][1];
			measuredRetentionTimeSaveData[i].rampRate = Globals.requiredTemperaturePrograms[i][1][0];
			measuredRetentionTimeSaveData[i].finalTemperature = Globals.requiredTemperaturePrograms[i][1][1];
			measuredRetentionTimeSaveData[i].finalHoldTime = Globals.requiredTemperaturePrograms[i][1][2];
			measuredRetentionTimeSaveData[i].programName = Globals.requiredTemperatureProgramNames[i];

			backCalculateSaveData[i] = new BackCalculateSaveData();
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeLong(this.classVersion);
		
		out.writeObject(measuredRetentionTimeSaveData);
		out.writeObject(backCalculateSaveData);

		out.writeObject(iCurrentStep);
		out.writeBoolean(finalFitComplete);
		StandardCompound[] x = programList.toArray(new StandardCompound[0]);
		out.writeObject(x);
		out.writeObject(labelHText);
		out.writeObject(labelSText);
		out.writeObject(labelCpText);
		out.writeObject(labelIterationText);
		out.writeObject(labelVarianceText);
		out.writeObject(labelTimeElapsedText);
		out.writeObject(statusText);
		out.writeObject(compoundName);
		out.writeObject(formula);
		out.writeObject(pubChemID);
		out.writeObject(cAS);
		out.writeObject(nISTID);
		out.writeObject(hMDB);
		out.writeObject(retentionTimeA);
		out.writeObject(retentionTimeB);
		out.writeObject(retentionTimeC);
		out.writeObject(retentionTimeD);
		out.writeObject(retentionTimeE);
		out.writeObject(retentionTimeF);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		long version = in.readLong();
		
		if (version >= 1)
		{
			measuredRetentionTimeSaveData = (MeasuredRetentionTimeSaveData[])in.readObject();
			backCalculateSaveData = (BackCalculateSaveData[])in.readObject();
			
			iCurrentStep = (int[])in.readObject();
			finalFitComplete = in.readBoolean();
			programList = FXCollections.observableArrayList((StandardCompound[])in.readObject());
			labelHText = (String)in.readObject();
			labelSText = (String)in.readObject();
			labelCpText = (String)in.readObject();
			labelIterationText = (String)in.readObject();
			labelVarianceText = (String)in.readObject();
			labelTimeElapsedText = (String)in.readObject();
			statusText = (String)in.readObject();
			compoundName = (String)in.readObject();
			formula = (String)in.readObject();
			pubChemID = (String)in.readObject();
			cAS = (String)in.readObject();
			nISTID = (String)in.readObject();
			hMDB = (String)in.readObject();
			retentionTimeA = (String)in.readObject();
			retentionTimeB = (String)in.readObject();
			retentionTimeC = (String)in.readObject();
			retentionTimeD = (String)in.readObject();
			retentionTimeE = (String)in.readObject();
			retentionTimeF = (String)in.readObject();
		}
	}
	
	class BackCalculateSaveData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private static final long classVersion = 1;
		
		double initialTemperature;
		double initialHoldTime;
		double columnLength;
		double innerDiameter;
		double filmThickness;
		double flowRate;
		double inletPressure;
		double outletPressure;
		boolean constantFlowRateMode;
		boolean vacuumOutletPressure;
		String fileName;
		ObservableList<StandardCompound> standardsList;
		double[][] temperatureProgramInConventionalForm;
		double tStep;
		double[][] m_dIdealTemperatureProfileArray;
		double[][] m_dTemperatureProfileDifferenceArray;
		double[][] m_dSimpleTemperatureProfileArray;
		double[][] m_dHoldUpArray;
		double[] m_dExpectedErrorArray;
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
			initialTemperature = 60;
			initialHoldTime = 5;
			columnLength = 30;
			innerDiameter = 0.25;
			filmThickness = 0.25;
			flowRate = 1.0;
			inletPressure = 151325;
			outletPressure = 1E-6;
			constantFlowRateMode = false;
			vacuumOutletPressure = true;
			fileName = "";
			standardsList = FXCollections.observableArrayList();
			temperatureProgramInConventionalForm = new double[][]{{26, 320, 15}};
			m_dIdealTemperatureProfileArray = null;
			m_dTemperatureProfileDifferenceArray = null;
			m_dSimpleTemperatureProfileArray = null;
			m_dHoldUpArray = null;
			m_dExpectedErrorArray = null;
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
			out.writeLong(this.classVersion);
			
			out.writeDouble(initialTemperature);
			out.writeDouble(initialHoldTime);
			out.writeDouble(columnLength);
			out.writeDouble(innerDiameter);
			out.writeDouble(filmThickness);
			out.writeDouble(flowRate);
			out.writeDouble(inletPressure);
			out.writeDouble(outletPressure);
			out.writeBoolean(constantFlowRateMode);
			out.writeBoolean(vacuumOutletPressure);
			out.writeObject(fileName);
			StandardCompound[] x = standardsList.toArray(new StandardCompound[0]);
			out.writeObject(x);
			out.writeObject(temperatureProgramInConventionalForm);
			out.writeDouble(tStep);
		    out.writeObject(m_dIdealTemperatureProfileArray);
		    out.writeObject(m_dTemperatureProfileDifferenceArray);
		    out.writeObject(m_dSimpleTemperatureProfileArray);
		    out.writeObject(m_dHoldUpArray);
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
				initialTemperature = in.readDouble();
				initialHoldTime = in.readDouble();
				columnLength = in.readDouble();
				innerDiameter = in.readDouble();
				filmThickness = in.readDouble();
				flowRate = in.readDouble();
				inletPressure = in.readDouble();
				outletPressure = in.readDouble();
				constantFlowRateMode = in.readBoolean();
				vacuumOutletPressure = in.readBoolean();
				fileName = (String)in.readObject();
				standardsList = FXCollections.observableArrayList((StandardCompound[])in.readObject());
				temperatureProgramInConventionalForm = (double[][])in.readObject();
				tStep = in.readDouble();
				m_dIdealTemperatureProfileArray = (double[][])in.readObject();
		    	m_dTemperatureProfileDifferenceArray = (double[][])in.readObject();
				m_dSimpleTemperatureProfileArray = (double[][])in.readObject();
				m_dHoldUpArray = (double[][])in.readObject();
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
		double initialTemperature;
		double initialHoldTime;
		double rampRate;
		double finalTemperature;
		double finalHoldTime;
		double columnLength;
		double innerDiameter;
		double filmThickness;
		double flowRate;
		double inletPressure;
		double outletPressure;
		boolean constantFlowRateMode;
		boolean vacuumOutletPressure;
		
		MeasuredRetentionTimeSaveData()
		{
			programName = "Program A";
			fileName = "";
			// Load the measured retention times table with the correct values
			List<StandardCompound> data = new ArrayList<StandardCompound>();
			for (int i = 0; i < Globals.AlkaneNameArray.length; i++)
			{
				StandardCompound newItem = new StandardCompound();
				newItem.setUse(false);
				newItem.setName(Globals.AlkaneNameArray[i]);
				newItem.setMz(Globals.convertMZToString(Globals.AlkaneMZArray[i]));
				newItem.setMeasuredRetentionTime(0.0);
				newItem.setIndex(i);
				
				data.add(newItem);
			}
			standardsList = FXCollections.observableArrayList(data);
			initialTemperature = 60;
			initialHoldTime = 5;
			rampRate = 26;
			finalTemperature = 320;
			finalHoldTime = 15;
			columnLength = 30;
			innerDiameter = 0.25;
			filmThickness = 0.25;
			flowRate = 1;
			inletPressure = 151325;
			outletPressure = 1E-6;
			constantFlowRateMode = false;
			vacuumOutletPressure = true;
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException
		{
			out.writeLong(this.classVersion);
			
			out.writeObject(programName);
			out.writeObject(fileName);
			StandardCompound[] x = standardsList.toArray(new StandardCompound[0]);
			out.writeObject(x);
			out.writeDouble(initialTemperature);
			out.writeDouble(initialHoldTime);
			out.writeDouble(rampRate);
			out.writeDouble(finalTemperature);
			out.writeDouble(finalHoldTime);
			out.writeDouble(columnLength);
			out.writeDouble(innerDiameter);
			out.writeDouble(filmThickness);
			out.writeDouble(flowRate);
			out.writeDouble(inletPressure);
			out.writeDouble(outletPressure);
			out.writeBoolean(constantFlowRateMode);
			out.writeBoolean(vacuumOutletPressure);
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			long version = in.readLong();
			
			if (version >= 1)
			{
				programName = (String)in.readObject();
				fileName = (String)in.readObject();
				StandardCompound[] x = (StandardCompound[])in.readObject();
				standardsList = FXCollections.observableArrayList(x);
				initialTemperature = in.readDouble();
				initialHoldTime = in.readDouble();
				rampRate = in.readDouble();
				finalTemperature = in.readDouble();
				finalHoldTime = in.readDouble();
				columnLength = in.readDouble();
				innerDiameter = in.readDouble();
				filmThickness = in.readDouble();
				flowRate = in.readDouble();
				inletPressure = in.readDouble();
				outletPressure = in.readDouble();
				constantFlowRateMode = in.readBoolean();
				vacuumOutletPressure = in.readBoolean();
			}
		}
	}
}
