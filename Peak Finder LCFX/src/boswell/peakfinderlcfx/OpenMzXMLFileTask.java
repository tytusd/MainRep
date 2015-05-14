package boswell.peakfinderlcfx;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import boswell.fxoptionpane.FXOptionPane;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Scan;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

class OpenMzXMLFileTask extends Task 
{
	private String fileName;
	private File file;
	private double[][] standardCompoundsMZArray = null;
	private int iSpectraCount;
	private double[][][] mzData = null;
	private double[] dRetentionTimes = null;
	private Vector<Double> mzArray = new Vector<Double>();
	
	private Stage primaryStage;
	private int iPositiveSpectraCount;
	private int iNegativeSpectraCount;


	OpenMzXMLFileTask(Stage parentWindow)
	{
		primaryStage = parentWindow;
	}
	
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
		file = new File(fileName);
		//TODO: this is error prone. can throw null pointer exception when fileName is ""
	}
	
	public void setStandardCompoundsMZArray (double[][] standardCompoundsMZArray)
	{
		this.standardCompoundsMZArray = standardCompoundsMZArray;
	}
	
	public double[][][] getMzData()
	{
		return mzData;
	}
	
	public double[] getRetentionTimes()
	{
		return dRetentionTimes;
	}
	
	@Override
	protected Object call() throws Exception 
	{
    	// Create an m/z array that contains all the values we need EICs of
    	for (int i = 0; i < standardCompoundsMZArray.length; i++)
    	{
    		for (int j = 0; j < standardCompoundsMZArray[i].length; j++)
    		{
    			double thisMZ = standardCompoundsMZArray[i][j];
    			boolean bFound = false;
    			
    			for (int k = 0; k < mzArray.size(); k++)
    			{
    				if (mzArray.get(k) == thisMZ)
    				{
    					bFound = true;
    					break;
    				}
    			}
    			
    			if (!bFound)
    			{
    				mzArray.add(thisMZ);
    			}
    		}
    	}
    	
        String fileExtension = "";

		int index = fileName.lastIndexOf('.');
		if (index > 0) 
		{
			fileExtension = fileName.substring(index + 1);
		}
		
        if (!file.exists())
        {
			Platform.runLater(new Runnable(){
	            @Override
	            public void run() {
	        		FXOptionPane.showMessageDialog(primaryStage, "The specified file does not exist.", "File does not exist", FXOptionPane.WARNING_MESSAGE);
	        		primaryStage.close();
	            }
	        });	
  	  		return null;
        }
        
		if (fileExtension.toUpperCase().equals("MZXML"))
		{
			boolean result = loadMzXMLFile();
			if (!result)
				return null;
		}
		else if (fileExtension.toUpperCase().equals("MZML"))
		{
			boolean result = loadMzMLFile();
			if (!result)
				return null;
		}
		else if (fileExtension.toUpperCase().equals("CDF"))
		{
			boolean result = loadCDFFile();
			if (!result)
				return null;
		}
		else
		{
			Platform.runLater(new Runnable(){
	            @Override
	            public void run() {
	        		FXOptionPane.showMessageDialog(primaryStage, "The specified file type is not supported.", "File type not supported", FXOptionPane.WARNING_MESSAGE);
	        		primaryStage.close();
	            }
	        });	
  			return null;
		}
		
		//updateMessage("Loading peak finder...");

		return null;		
	}
	
	private boolean loadMzXMLFile()
	{
        // Set progress to indeterminate
        this.updateProgress(-1, -1);
        this.updateMessage("Parsing " + file.getName());
    	
        DatatypeFactory dfactory = null;
		try {
			dfactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			return false;
		}
		
		JMzReader inputParser = null;
		try {
			inputParser = new MzXMLFile(file);
		} catch (MzXMLParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		//JMzReader inputParser = new MzMlWrapper(mzxmlFile);
        // get the number of spectra in the file
        //iSpectraCount = inputParser.getSpectraCount();
        
		// Find how many spectra have MS level 1
		iSpectraCount = 0;
		iPositiveSpectraCount = 0;
		iNegativeSpectraCount = 0;
		
        Iterator<Spectrum> iterator = inputParser.getSpectrumIterator();
        while(iterator.hasNext()) 
        {
        	Spectrum spectrum = iterator.next();
        	
        	if (spectrum.getMsLevel() == 1)//2)
        	{
        		ParamGroup paramGroup = spectrum.getAdditional();
        		List<CvParam> cvParamList = paramGroup.getCvParams();
        		boolean bPosPolarity = true;
        		for (int j = 0; j < cvParamList.size(); j++)
				{
					CvParam thisParam = cvParamList.get(j);
					if (thisParam.getName().equals("scan polarity"))
					{
						if (thisParam.getValue().equals("+") || thisParam.getValue().equals("any"))
						{
							bPosPolarity = true;
							break;
						}
						else
						{
							bPosPolarity = false;
							break;
						}
					}
				}
        		if (bPosPolarity)
        			iPositiveSpectraCount++;
        		else
        			iNegativeSpectraCount++;
        	}      	        	
        }
        iSpectraCount = iPositiveSpectraCount + iNegativeSpectraCount;
        
        if (iSpectraCount == 0)
        {
			FXOptionPane.showMessageDialog(null, "There is no MS level 1 data in the file.", "No MS Spectra in File", JOptionPane.ERROR_MESSAGE); 
  	  		return false;
        }

		// Create the mzData array to be the right size for each ion
		mzData = new double[standardCompoundsMZArray.length][][];
        for (int j = 0; j < mzData.length; j++)
        {
        	if (standardCompoundsMZArray[j][0] > 0)
        		mzData[j] = new double[iPositiveSpectraCount][2];
        	else
        		mzData[j] = new double[iNegativeSpectraCount][2];
        }
   
        // use the iterator to access all spectra in the file
        iterator = inputParser.getSpectrumIterator();
        
    	updateProgress(0, 1);
        
    	int iSpectrum = 0;
		int iPositiveSpectrum = 0;
		int iNegativeSpectrum = 0;
		dRetentionTimes = new double[iSpectraCount];
		
		// Walk through each of the spectra in the file
		while(iterator.hasNext() && iSpectrum < iSpectraCount) 
        {
        	updateMessage("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
        	updateProgress((double)iSpectrum, (double)iSpectraCount);
        	
        	if (this.isCancelled())
        	{
        		return false;
        	}
        	
        	// Get the next spectrum
        	Spectrum spectrum = iterator.next();	        
			
        	// Only continue if MS level is 1
        	if (spectrum.getMsLevel() != 1)//2)
        		continue;
        	
        	// Get the retention time and scan polarity
        	List<CvParam> cvParamList = spectrum.getAdditional().getCvParams();
			double dRetentionTime = 0;
			boolean bPositivePolarity = true;
			for (int j = 0; j < cvParamList.size(); j++)
			{
				CvParam thisParam = cvParamList.get(j);
				if (thisParam.getName().equals("retention time") && dfactory != null)
				{
        			Duration dur = dfactory.newDuration(thisParam.getValue());
    	        	if (dur != null)
    	        	{
        	    		BigDecimal dSeconds = (BigDecimal)dur.getField(DatatypeConstants.SECONDS);
    	        		dRetentionTime = dSeconds.doubleValue();
    	        	}
					continue;
				}
				else if (thisParam.getName().equals("scan polarity") && dfactory != null)
				{
					if (thisParam.getValue().equals("+") || thisParam.getValue().equals("any"))
					{
						bPositivePolarity = true;
						continue;
					}
					else
					{
						bPositivePolarity = false;
						continue;
					}
				}
			}

			// Now get the mass spectra out of the file
	        
	        // Initialize the intensities to 0 and set the retention time
			for (int j = 0; j < mzData.length; j++)
			{
				if (bPositivePolarity && standardCompoundsMZArray[j][0] > 0)
				{
					mzData[j][iPositiveSpectrum][0] = dRetentionTime;
					mzData[j][iPositiveSpectrum][1] = 0;
				}
				else if (!bPositivePolarity && standardCompoundsMZArray[j][0] < 0)
				{
					mzData[j][iNegativeSpectrum][0] = dRetentionTime;
					mzData[j][iNegativeSpectrum][1] = 0;
				}
			}
	        dRetentionTimes[iSpectrum] = dRetentionTime;
			
			// Retrieve the spectrum's peak list
	        Map<Double, Double> peakList = spectrum.getPeakList();

	        // Fill in mzData with the intensities at each mass
        	for (Double mz : peakList.keySet()) 
        	{
        		for (int i = 0; i < standardCompoundsMZArray.length; i++)
        		{
        			for (int massnum = 0; massnum < standardCompoundsMZArray[i].length; massnum++)
        			{
	        			if (bPositivePolarity)
	        			{
	        				if (mz <= standardCompoundsMZArray[i][massnum] + 0.5 && mz >= standardCompoundsMZArray[i][massnum] - 0.5)
	        					mzData[i][iPositiveSpectrum][1] += peakList.get(mz);
	        			}
	        			else
	        			{
	         				if (mz <= (-standardCompoundsMZArray[i][massnum]) + 0.5 && mz >= (-standardCompoundsMZArray[i][massnum]) - 0.5)
	        					mzData[i][iNegativeSpectrum][1] += peakList.get(mz);
	        			}
        			}
        		}
        	}


        	if (bPositivePolarity)
        		iPositiveSpectrum++;
        	else
        		iNegativeSpectrum++;
        	
			iSpectrum++;
        }        
        return true;
	}
	
	private boolean loadMzMLFile()
	{
    	updateProgress(-1, -1);
		updateMessage("Parsing " + file.getName());
    	
        // Create the unmarshaller
		MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(file);

		updateProgress(0, 1);
		
		iSpectraCount = unmarshaller.getObjectCountForXpath("/run/spectrumList/spectrum");

        mzData = new double[iSpectraCount][][];
        dRetentionTimes = new double[iSpectraCount];
        
     // Count the number of positive and negative ion spectra that are MS level 1
		//iSpectraCount = unmarshaller.getObjectCountForXpath("/run/spectrumList/spectrum");

        iPositiveSpectraCount = 0;
		iNegativeSpectraCount = 0;
    	MzMLObjectIterator<uk.ac.ebi.jmzml.model.mzml.Spectrum> spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", uk.ac.ebi.jmzml.model.mzml.Spectrum.class);
		while (spectrumIterator.hasNext())
		{	
			uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum = spectrumIterator.next();	        
			List<CVParam> cvParamList = spectrum.getCvParam();
			int iMSLevel = 1;
			boolean bPositivePolarity = true;
			for (int j = 0; j < cvParamList.size(); j++)
			{
				CVParam thisParam = cvParamList.get(j);
				if (thisParam.getName().equals("ms level"))
				{
					iMSLevel = Integer.valueOf(thisParam.getValue());
					continue;
				}
				if (thisParam.getName().equals("negative scan"))
				{
					bPositivePolarity = false;
					continue;
				}
			}
			    					
			// Only count it if it's MS level 1
        	
			if (iMSLevel == 1)
			{
				if (bPositivePolarity)
	        		iPositiveSpectraCount++;
				else
					iNegativeSpectraCount++;
			}
		}

		// iSpectraCount is the total number of spectra
		iSpectraCount = iPositiveSpectraCount + iNegativeSpectraCount;
		
        if (iSpectraCount == 0)
        {
			JOptionPane.showMessageDialog(null, "There is no MS level 1 data in the file.", "No MS Spectra in File", JOptionPane.ERROR_MESSAGE); 
  	  		return false;
        }

		//mzData[m/z][
		
		// Create the mzData array to be the right size for each ion
		mzData = new double[standardCompoundsMZArray.length][][];
        for (int j = 0; j < mzData.length; j++)
        {
        	if (standardCompoundsMZArray[j][0] > 0)
        		mzData[j] = new double[iPositiveSpectraCount][2];
        	else
        		mzData[j] = new double[iNegativeSpectraCount][2];
        }

        updateProgress(0, 1);
        
      //dealing with element collections
		spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", uk.ac.ebi.jmzml.model.mzml.Spectrum.class);
		int iSpectrum = 0;
		int iPositiveSpectrum = 0;
		int iNegativeSpectrum = 0;
		// Walk through each of the spectra in the file
		while (spectrumIterator.hasNext())
		{
    		updateMessage("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
    		updateProgress((double)iSpectrum, (double)iSpectraCount);
    		
        	if (this.isCancelled())
        	{
        		return false;
        	}

			//read next spectrum from XML file
			uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum = spectrumIterator.next();
			
			// Get the retention time
			List<Scan> scanList = spectrum.getScanList().getScan();
			Scan thisScan = scanList.get(0);
			List<CVParam> cvParamList = thisScan.getCvParam();
			double dRetentionTime = 0;
			for (int j = 0; j < cvParamList.size(); j++)
			{
				CVParam thisParam = cvParamList.get(j);
				if (thisParam.getName().equals("scan start time"))
				{
					dRetentionTime = Double.valueOf(thisParam.getValue());
					if (thisParam.getUnitName().equals("minute"))
						dRetentionTime *= 60;
					break;
				}
			}
			
			// Get the polarity and MS level
			boolean bPositivePolarity = true;
			int iMSLevel = 1;
			cvParamList = spectrum.getCvParam();
			for (int j = 0; j < cvParamList.size(); j++)
			{
				CVParam thisParam = cvParamList.get(j);
				if (thisParam.getName().equals("ms level"))
				{
					iMSLevel = Integer.valueOf(thisParam.getValue());
					continue;
				}
				if (thisParam.getName().equals("negative scan"))
				{
					bPositivePolarity = false;
					continue;
				}
			}
			
			if (iMSLevel == 1)
			{
				// Now get the mass spectra out of the file
				BinaryDataArrayList binaryDataArrayList = spectrum.getBinaryDataArrayList();
				List<BinaryDataArray> binaryDataArray = binaryDataArrayList.getBinaryDataArray();

				// Put the spectrum into thisSpectrum[][]
  	        	double thisSpectrum[][] = null;
    	        
    	        // thisSpectrum[Spectrum#][0] = m/z
    	        // thisSpectrum[Spectrum#][1] = intensity
    	        
				for (int j = 0; j < binaryDataArray.size(); j++)
				{
					Number[] binaryData = (Number[])binaryDataArray.get(j).getBinaryDataAsNumberArray();
    	        	//if (mzData[iSpectrum] == null)
    	        	//	mzData[iSpectrum] = new double[binaryData.length][2];
    	        	
    	        	// initialize intensity to 0 - we just add to it each time we find an intensity at the mass
    	        	//for (int i = 0; i < mzData[iSpectrum].length; i++)
    	        	//{
    	        	//	mzData[iSpectrum][i][0] = mzDataMassList.get(i);
    	        	//	mzData[iSpectrum][i][1] = 0;
    	        	//}
    	        	
    	        	if (thisSpectrum == null)
    	        		thisSpectrum = new double[binaryData.length][2];
    	        	
					List<CVParam> cvParamListDataArray = binaryDataArray.get(j).getCvParam();
					
					for (int k = 0; k < cvParamListDataArray.size(); k++)
					{
						CVParam cvParam = cvParamListDataArray.get(k);
						if (cvParam.getName().equals("m/z array"))
						{
							for (int peak = 0; peak < binaryData.length; peak++)
							{
								thisSpectrum[peak][0] = binaryData[peak].doubleValue();
							}
							break;
						}
						else if (cvParam.getName().equals("intensity array"))
						{
							for (int peak = 0; peak < binaryData.length; peak++)
							{
								thisSpectrum[peak][1] = binaryData[peak].doubleValue();
							}
							break;
						}
					}
				}
				
				// Initialize the intensities to 0 and set the retention time
				for (int j = 0; j < mzData.length; j++)
				{
					if (bPositivePolarity && standardCompoundsMZArray[j][0] > 0)
					{
						mzData[j][iPositiveSpectrum][0] = dRetentionTime;
						mzData[j][iPositiveSpectrum][1] = 0;
					}
					else if (!bPositivePolarity && standardCompoundsMZArray[j][0] < 0)
					{
						mzData[j][iNegativeSpectrum][0] = dRetentionTime;
						mzData[j][iNegativeSpectrum][1] = 0;
					}
				}
				
				// Fill in mzData with the intensities at each mass
	        	for (int j = 0; j < thisSpectrum.length; j++) 
	        	{
	        		for (int i = 0; i < standardCompoundsMZArray.length; i++)
	        		{
	        			for (int massnum = 0; massnum < standardCompoundsMZArray[i].length; massnum++)
	        			{
    	        			if (bPositivePolarity)
    	        			{
    	        				if (thisSpectrum[j][0] <= standardCompoundsMZArray[i][massnum] + 0.5 && thisSpectrum[j][0] >= standardCompoundsMZArray[i][massnum] - 0.5)
    	        					mzData[i][iPositiveSpectrum][1] += thisSpectrum[j][1];
    	        			}
    	        			else
    	        			{
    	         				if (thisSpectrum[j][0] <= (-standardCompoundsMZArray[i][massnum]) + 0.5 && thisSpectrum[j][0] >= (-standardCompoundsMZArray[i][massnum]) - 0.5)
    	        					mzData[i][iNegativeSpectrum][1] += thisSpectrum[j][1];
    	        			}
	        			}
	        		}
	        	}
	        	
	        	if (bPositivePolarity)
	        		iPositiveSpectrum++;
	        	else
	        		iNegativeSpectrum++;
	        	
				iSpectrum++;
			}
		}
		return true;
	}
	
	private boolean loadCDFFile()
	{
		updateProgress(-1, -1);
		updateMessage("Parsing " + file.getName());
		
		updateProgress(0, 1);
		
		NetcdfFile dataFile = null;
	    
		try 
	    {
	    	String file = fileName;
	    	dataFile = NetcdfFile.open(file);
	    	
	    	// Retrieve the variable named "scan_index"
	    	// Contains the index of the start of each scan
	    	Variable scanIndexDataVar = dataFile.findVariable("scan_index");
	    	Variable pointCountDataVar = dataFile.findVariable("point_count");
	    	Variable massValuesDataVar = dataFile.findVariable("mass_values");
	    	Variable intensityValuesDataVar = dataFile.findVariable("intensity_values");
	    	Variable timeValuesDataVar = dataFile.findVariable("scan_acquisition_time");
	    	
	    	if (scanIndexDataVar == null) 
	    	{
                System.out.println("Can't find scan_index data");
                throw new IOException();
	    	}
	    	
	    	if (pointCountDataVar == null) 
	    	{
                System.out.println("Can't find point_count data");
                throw new IOException();
	    	}
	    	
	    	if (massValuesDataVar == null) 
	    	{
                System.out.println("Can't find mass_values data");
                throw new IOException();
	    	}

	    	if (intensityValuesDataVar == null) 
	    	{
                System.out.println("Can't find intensity_values data");
                throw new IOException();
	    	}

	    	if (timeValuesDataVar == null) 
	    	{
                System.out.println("Can't find scan_acquisition_time data");
                throw new IOException();
	    	}

	    	// Grab all of the scan_index data, point_count data, and 
	        	ArrayInt.D1 scanIndexArray = (ArrayInt.D1)scanIndexDataVar.read();
	        	ArrayInt.D1 pointCountArray = (ArrayInt.D1)pointCountDataVar.read();
	        	
	        	iSpectraCount = (int)scanIndexArray.getSize();
	           
	        	mzData = new double[standardCompoundsMZArray.length][][];
	        	dRetentionTimes = new double[iSpectraCount];
	        	
	        	for(int i = 0; i < mzData.length; i++)
	        	{
	        		mzData[i] = new double[iSpectraCount][2];
	        	}
	        	
	        	for (int iSpectrum = 0; iSpectrum < iSpectraCount; iSpectrum++)
	        	{
	    		updateMessage("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
	    		updateProgress((double)iSpectrum, (double)iSpectraCount);
	    		
            	if (this.isCancelled())
            	{
            		return false;
            	}

  	        	
  	        	
  	        	// Now pull the m/z values and intensities for this spectrum
  	        	int[] shape = {pointCountArray.get(iSpectrum)};
	           	int[] origin = {scanIndexArray.get(iSpectrum)};
	           	ArrayFloat.D1 massDataArray = (ArrayFloat.D1)massValuesDataVar.read(origin, shape);
  	        	ArrayFloat.D1 intensityDataArray = (ArrayFloat.D1)intensityValuesDataVar.read(origin, shape);
		    	
	           	// Now pull the retention time for this spectrum
  	        	shape[0] = 1;
  	        	origin[0] = iSpectrum;
  	        	ArrayDouble.D1 timeDataArray = (ArrayDouble.D1)timeValuesDataVar.read(origin, shape);
  	        
  	        	
  	        	for (int i = 0; i < mzData.length; i++)
	        	{
	        		mzData[i][iSpectrum][0] = timeDataArray.get(0);
	        		mzData[i][iSpectrum][1] = 0;
	        	}
	           	
  	        	
  	        	for (int j = 0; j < massDataArray.getSize(); j++) 
	        	{
	        		for (int i = 0; i < mzArray.size(); i++)
	        		{
	        			if (massDataArray.get(j) <= mzArray.get(i) + 0.5 && massDataArray.get(j) >= mzArray.get(i) - 0.5)
	        				mzData[i][iSpectrum][1] += intensityDataArray.get(j);
	        		}
	        	}

  	        	
	        	}

	       // The file is closed no matter what by putting inside a try/catch block.
	    } 
	    catch (java.io.IOException e) 
	    {
            e.printStackTrace();
            //return;
	    }  
	    catch (InvalidRangeException e) 
	    {
	        e.printStackTrace();
	    } 
	    finally 
	    {
	        if (dataFile != null)
	        try 
	        {
	        	dataFile.close();
	        } 
	        catch (IOException ioe) 
	        {
	        	ioe.printStackTrace();
	        }
	    }
		
		return true;
	}

}