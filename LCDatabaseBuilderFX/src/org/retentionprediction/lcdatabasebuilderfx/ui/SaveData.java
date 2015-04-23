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
	String retentionTimeG;
	String retentionTimeH;
	
	SaveData()
	{
		// Load with default values
		
		iCurrentStep = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		finalFitComplete = false;
		programList = FXCollections.observableArrayList();
	    for (int i = 0; i < 8; i++)
	    {
	    	StandardCompound newTestCompound = new StandardCompound();
	    	newTestCompound.setIndex(i);
	    	newTestCompound.setName(Globals.requiredGradientProgramNames[i]);
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
		retentionTimeG = "";
		retentionTimeH = "";

		measuredRetentionTimeSaveData = new MeasuredRetentionTimeSaveData[8];
		backCalculateSaveData = new BackCalculateSaveData[8];
		
		for (int i = 0; i < 8; i++)
		{
			measuredRetentionTimeSaveData[i] = new MeasuredRetentionTimeSaveData();
			measuredRetentionTimeSaveData[i].programName = Globals.requiredGradientProgramNames[i];

			backCalculateSaveData[i] = new BackCalculateSaveData();
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeLong(SaveData.classVersion);
		
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
		out.writeObject(retentionTimeG);
		out.writeObject(retentionTimeH);
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
			retentionTimeG = (String)in.readObject();
			retentionTimeH = (String)in.readObject();
		}
	}
	
	class BackCalculateSaveData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private static final long classVersion = 1;
		
		double columnLength;
		double innerDiameter;
		double flowRate;
		String fileName;
		ObservableList<StandardCompound> standardsList;
		double[][] gradientProgramInConventionalForm;
		double tStep;
		double[][] m_dIdealGradientProfileArray;
		double[][] m_dGradientProfileDifferenceArray;
		double[][] m_dSimpleGradientProfileArray;
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
			columnLength = 30;
			innerDiameter = 0.25;
			flowRate = 1.0;
			fileName = "";
			standardsList = FXCollections.observableArrayList();
			gradientProgramInConventionalForm = new double[][]{{5,95}};
			m_dIdealGradientProfileArray = null;
			m_dGradientProfileDifferenceArray = null;
			m_dSimpleGradientProfileArray = null;
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
			out.writeLong(BackCalculateSaveData.classVersion);
			
			out.writeDouble(columnLength);
			out.writeDouble(innerDiameter);
			out.writeDouble(flowRate);
			out.writeObject(fileName);
			StandardCompound[] x = standardsList.toArray(new StandardCompound[0]);
			out.writeObject(x);
			out.writeObject(gradientProgramInConventionalForm);
			out.writeDouble(tStep);
		    out.writeObject(m_dIdealGradientProfileArray);
		    out.writeObject(m_dGradientProfileDifferenceArray);
		    out.writeObject(m_dSimpleGradientProfileArray);
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
				columnLength = in.readDouble();
				innerDiameter = in.readDouble();
				flowRate = in.readDouble();
				fileName = (String)in.readObject();
				standardsList = FXCollections.observableArrayList((StandardCompound[])in.readObject());
				gradientProgramInConventionalForm = (double[][])in.readObject();
				tStep = in.readDouble();
				m_dIdealGradientProfileArray = (double[][])in.readObject();
		    	m_dGradientProfileDifferenceArray = (double[][])in.readObject();
				m_dSimpleGradientProfileArray = (double[][])in.readObject();
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
		double columnLength;
		double innerDiameter;
		double flowRate;
		
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
			columnLength = 30;
			innerDiameter = 0.25;
			flowRate = 1;
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException
		{
			out.writeLong(MeasuredRetentionTimeSaveData.classVersion);
			
			out.writeObject(programName);
			out.writeObject(fileName);
			StandardCompound[] x = standardsList.toArray(new StandardCompound[0]);
			out.writeObject(x);
			out.writeDouble(columnLength);
			out.writeDouble(innerDiameter);
			out.writeDouble(flowRate);
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
				columnLength = in.readDouble();
				innerDiameter = in.readDouble();
				flowRate = in.readDouble();
			}
		}
	}
}
