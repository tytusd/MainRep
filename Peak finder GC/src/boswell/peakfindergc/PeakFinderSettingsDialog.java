package boswell.peakfindergc;

import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Frame;
import javax.swing.JDialog;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Point;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import javax.swing.border.EtchedBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

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
import uk.ac.ebi.jmzml.model.mzml.params.ScanListCVParam;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import javax.swing.JRadioButton;

public class PeakFinderSettingsDialog extends JDialog implements FocusListener, KeyListener, ActionListener, ListSelectionListener, TableModelListener, DocumentListener
{
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel = null;
	private JTextField jtxtMZFile = null;
	private JButton jbtnLoadFile = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JLabel jlblStationaryPhase = null;
	private JComboBox jcboStationaryPhase = null;
	private JLabel jlblColumnLength = null;
	private JTextField jtxtColumnLength = null;
	private JLabel jlblColumnLengthUnit = null;
	private JScrollPane jScrollPane = null;
	private JTable jtableTemperatureProgram = null;
	private JButton jbtnInsertRow = null;
	private JButton jbtnRemoveRow = null;
	private JLabel jLabel2 = null;
	private JLabel jlblGradientProgram = null;
	private JPanel jPanel2 = null;
	
	private Preferences prefs;

	public SpecialTableModel2 tmTemperatureProgram;

	public LinearInterpolationFunction m_InterpolatedTemperatureProfile;  //  @jve:decl-index=0:
	public InterpolationFunction m_InterpolatedHoldUpProfile;
	
	public int m_iStationaryPhase = 0;
	public double m_dInitialTemperature = 60;
	public double m_dInitialTime = 1;
	public double m_dColumnLength = 30; // in m
	public double m_dInnerDiameter = 0.25; // in mm
	public double m_dFilmThickness = 0.25; // in um
	public double m_dProgramTime = 20;
	public double m_dFlowRate = 1; // in mL/min
	public double m_dInletPressure = 45; // in kPa
	public double m_dOutletPressure = 0.000001; // in kPa
    public boolean m_bDoNotChangeTable = false;
    
    public double[] m_dSelectedRetentionTimes = null;
    public boolean[] m_bSkippedStandards = null;
    public int[] m_iSelectedPeakRank = null;
    public boolean m_bOkPressed = false;
    
    String[] m_strStationaryPhaseArray = null;
    String[] m_strStandardCompoundsNameArray = null;
    double[][] m_dStandardCompoundsMZArray = null;
    //double[][][] m_dIsothermalDataArray = null;
    double[][] m_dIsothermalParamArray = null;
    
    boolean m_bEditable = true;
    String m_strFileName = null;
    
    double m_dtstep = 0;
    
    public double[][] m_dTemperatureProgram = null;
	private JRadioButton jrdoConstantFlowRate = null;
	private JLabel jlblFlowRate = null;
	private JTextField jtxtFlowRate = null;
	private JLabel jlblFlowRateUnit = null;
	private JRadioButton jrdoConstantPressure = null;
	private JLabel jlblPressure = null;
	private JTextField jtxtPressure = null;
	private JLabel jlblPressureUnit = null;
	private JLabel jlblOutletPressure = null;
	private JRadioButton jrdoVacuum = null;
	private JRadioButton jrdoOtherPressure = null;
	private JTextField jtxtOtherPressure = null;
	private JLabel jlblOtherPressureUnit = null;
	private JLabel jlblInitialTemperature = null;
	private JTextField jtxtInitialTemperature = null;
	private JLabel jlblInitialTemperatureUnit = null;
	private JLabel jlblInitialTime = null;
	private JTextField jtxtInitialTime = null;
	private JLabel jlblInitialTimeUnit = null;
	public ProgressDialog progressDialog = null;
	private JLabel jlblInnerDiameter = null;
	private JTextField jtxtInnerDiameter = null;
	private JLabel jlblInnerDiameterUnit = null;
	private JLabel jlblFilmThickness = null;
	private JTextField jtxtFilmThickness = null;
	private JLabel jlblFilmThicknessUnit = null;

    class OpenMzXMLFileTask extends SwingWorker<Void, Void> 
    {
        JDialog owner = null;
        boolean bOpenedSuccessfully = false;
        
        OpenMzXMLFileTask(JDialog owner)
    	{
    		super();
    		this.owner = owner;
    	}
        
        @Override
        public Void doInBackground() 
        {
        	// The progress dialog must be created before the thread can run.
        	if (progressDialog == null)
        		return null;
        	
        	int iSpectraCount;
        	double[][][] mzData = null;
        	double[] dRetentionTimes = null;
        	
        	Vector<Double> mzArray = new Vector<Double>();
        	
        	// Create an m/z array that contains all the values we need EICs of
        	for (int i = 0; i < m_dStandardCompoundsMZArray.length; i++)
        	{
        		for (int j = 0; j < m_dStandardCompoundsMZArray[i].length; j++)
        		{
        			double thisMZ = m_dStandardCompoundsMZArray[i][j];
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
        	
    		try 
    		{
                String fileExtension = "";

    			int index = jtxtMZFile.getText().lastIndexOf('.');
    			if (index > 0) 
    			{
    				fileExtension = jtxtMZFile.getText().substring(index + 1);
    			}
    			
    			if (fileExtension.toUpperCase().equals("MZXML"))
    			{
    				File mzFile = new File(jtxtMZFile.getText());
                    if (!mzFile.exists())
                    {
            			JOptionPane.showMessageDialog(null, "The specified file does not exist.", "File does not exist", JOptionPane.ERROR_MESSAGE); 
              	  		return null;
                    }

                	// Launch the progress dialog in a separate thread
                	//runProgressDialogTask = new RunProgressDialogTask(owner);
                    progressDialog.jProgressBar.setIndeterminate(true);
            		String fileName = new File(jtxtMZFile.getText()).getName();
            		progressDialog.jlblProgressCaption.setText("Parsing " + fileName);
                	//runProgressDialogTask.execute();
                	
                    DatatypeFactory dfactory = null;
            		try {
            			dfactory = DatatypeFactory.newInstance();
            		} catch (DatatypeConfigurationException e) {
            			e.printStackTrace();
            		}
            		
        			JMzReader inputParser;
    				inputParser = new MzXMLFile(mzFile);
    				
    				//JMzReader inputParser = new MzMlWrapper(mzxmlFile);
        	        // get the number of spectra in the file
        	        //iSpectraCount = inputParser.getSpectraCount();
        	        
    				// Find how many spectra have MS level 1
    				iSpectraCount = 0;
        	        Iterator<Spectrum> iterator = inputParser.getSpectrumIterator();
        	        while(iterator.hasNext()) 
        	        {
        	        	Spectrum spectrum = iterator.next();	        
        	        	
        	        	if (spectrum.getMsLevel() == 1)
        	        		iSpectraCount++;
        	        }
        	        
        	        mzData = new double[iSpectraCount][][];
        	        dRetentionTimes = new double[iSpectraCount];
        	        
        	        // use the iterator to access all spectra in the file
        	        iterator = inputParser.getSpectrumIterator();
        	        
                	progressDialog.jProgressBar.setIndeterminate(false);
                	progressDialog.jProgressBar.setStringPainted(true);
                	progressDialog.jProgressBar.setValue(0);
        	        
        	        int iSpectrum = 0;
        	        while(iterator.hasNext() && iSpectrum < iSpectraCount) 
        	        {
        	    		progressDialog.jlblProgressCaption.setText("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
                    	progressDialog.jProgressBar.setValue((int)(((double)iSpectrum / (double)iSpectraCount) * 100));

                    	if (progressDialog.m_bCancel)
                    	{
                    		return null;
                    	}
                    	
        	        	Spectrum spectrum = iterator.next();	        
        	        	
        	        	if (spectrum.getMsLevel() == 1)
        	        	{
	        	        	// retrieve the spectrum's peak list
	        	        	Map<Double, Double> peakList = spectrum.getPeakList();
	        	        	
	        	        	int iPeakCount = peakList.size();
	        	        	mzData[iSpectrum] = new double[mzArray.size()][2];
	        	        	
	        	        	// initialize all values to 0
	        	        	for (int i = 0; i < mzData[iSpectrum].length; i++)
	        	        	{
	        	        		mzData[iSpectrum][i][0] = mzArray.get(i);
	        	        		mzData[iSpectrum][i][1] = 0;
	        	        	}
	        	        	
	        	        	// process all peaks by iterating over the m/z values
	        	        	for (Double mz : peakList.keySet()) 
	        	        	{
	        	        		for (int i = 0; i < mzArray.size(); i++)
	        	        		{
	        	        			if (mz <= mzArray.get(i) + 0.5 && mz >= mzArray.get(i) - 0.5)
	        	        				mzData[iSpectrum][i][1] += peakList.get(mz);
	        	        		}
	        	        	}
	
	        	        	ParamGroup x = spectrum.getAdditional();
	        	        	List<CvParam> paramList = x.getCvParams();
	
	        	        	Duration dur = null;
	        	        	for (int i = 0; i < paramList.size(); i++)
	        	        	{
	        	        		CvParam cvParam = paramList.get(i);
	        	        		// process the additional information
	        	        		if (cvParam.getName() == "retention time" && dfactory != null)
	        	        			dur = dfactory.newDuration(cvParam.getValue());
	        	        	}
	
	        	    		BigDecimal dSeconds = (BigDecimal)dur.getField(DatatypeConstants.SECONDS);
	        	        	// process the peak
	        	        	if (dur != null)
	        	        	{
	        	        		dRetentionTimes[iSpectrum] = dSeconds.doubleValue();
	        	        	}
	        	        	
	        	        	iSpectrum++;
        	        	}
        	        }
    			}
    			// TODO: deal with MS level not equal to for mzML files
    			else if (fileExtension.toUpperCase().equals("MZML"))
    			{
    	            File mzFile = new File(jtxtMZFile.getText());
    	            if (!mzFile.exists())
    	            {
    	    			JOptionPane.showMessageDialog(null, "The specified file does not exist.", "File does not exist", JOptionPane.ERROR_MESSAGE); 
    	      	  		return null;
    	            }

    	        	// Launch the progress dialog in a separate thread
    	        	//runProgressDialogTask = new RunProgressDialogTask(owner);
    	        	progressDialog.jProgressBar.setIndeterminate(true);
    	    		String fileName = new File(jtxtMZFile.getText()).getName();
    	    		progressDialog.jlblProgressCaption.setText("Parsing " + fileName);
    	        	//runProgressDialogTask.execute();
    	        	
    	            // Create the unmarshaller
    				MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(mzFile);

    				progressDialog.jProgressBar.setIndeterminate(false);
                	progressDialog.jProgressBar.setStringPainted(true);
                	progressDialog.jProgressBar.setValue(0);
    				
    				iSpectraCount = unmarshaller.getObjectCountForXpath("/run/spectrumList/spectrum");

        	        mzData = new double[iSpectraCount][][];
        	        dRetentionTimes = new double[iSpectraCount];
        	        
    				//dealing with element collections
    				MzMLObjectIterator<uk.ac.ebi.jmzml.model.mzml.Spectrum> spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", uk.ac.ebi.jmzml.model.mzml.Spectrum.class);
    				int iSpectrum = 0;
    				while (spectrumIterator.hasNext())
    				{
        	    		progressDialog.jlblProgressCaption.setText("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
                    	progressDialog.jProgressBar.setValue((int)(((double)iSpectrum / (double)iSpectraCount) * 100));

                    	if (progressDialog.m_bCancel)
                    	{
                    		return null;
                    	}

    					//read next spectrum from XML file
    					uk.ac.ebi.jmzml.model.mzml.Spectrum spectrum = spectrumIterator.next();
    					
    					// get the retention time out of it
    					List<Scan> scanList = spectrum.getScanList().getScan();
    					Scan thisScan = scanList.get(0);
    					List<CVParam> cvParamList = thisScan.getCvParam();
    					for (int j = 0; j < cvParamList.size(); j++)
    					{
    						CVParam thisParam = cvParamList.get(j);
    						if (thisParam.getName().equals("scan start time"))
    						{
    							dRetentionTimes[iSpectrum] = Double.valueOf(thisParam.getValue());
    							if (thisParam.getUnitName().equals("minute"))
    								dRetentionTimes[iSpectrum] *= 60;
    							break;
    						}
    					}
    					
    					// now get the spectral data out of it
    					BinaryDataArrayList binaryDataArrayList = spectrum.getBinaryDataArrayList();
    					List<BinaryDataArray> binaryDataArray = binaryDataArrayList.getBinaryDataArray();

          	        	mzData[iSpectrum] = new double[mzArray.size()][2];
            	        double thisSpectrum[][] = null;

    					for (int j = 0; j < binaryDataArray.size(); j++)
    					{
    						Number[] binaryData = (Number[])binaryDataArray.get(j).getBinaryDataAsNumberArray();
            	        	//if (mzData[iSpectrum] == null)
            	        	//	mzData[iSpectrum] = new double[binaryData.length][2];
            	        	
            	        	// initialize all values to 0
            	        	for (int i = 0; i < mzData[iSpectrum].length; i++)
            	        	{
            	        		mzData[iSpectrum][i][0] = mzArray.get(i);
            	        		mzData[iSpectrum][i][1] = 0;
            	        	}
            	        	
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
    					
        	        	for (int j = 0; j < thisSpectrum.length; j++) 
        	        	{
        	        		for (int i = 0; i < mzArray.size(); i++)
        	        		{
        	        			if (thisSpectrum[j][0] <= mzArray.get(i) + 0.5 && thisSpectrum[j][0] >= mzArray.get(i) - 0.5)
        	        				mzData[iSpectrum][i][1] += thisSpectrum[j][1];
        	        		}
        	        	}

    					iSpectrum++;
    				}
    				
    			}
    			else if (fileExtension.toUpperCase().equals("CDF"))
    			{
    				// Launch the progress dialog in a separate thread
    	        	//runProgressDialogTask = new RunProgressDialogTask(owner);
    	        	progressDialog.jProgressBar.setIndeterminate(true);
    	    		String fileName = new File(jtxtMZFile.getText()).getName();
    	    		progressDialog.jlblProgressCaption.setText("Parsing " + fileName);
    	        	//runProgressDialogTask.execute();
    				
    				progressDialog.jProgressBar.setIndeterminate(false);
                	progressDialog.jProgressBar.setStringPainted(true);
                	progressDialog.jProgressBar.setValue(0);
    				
    				NetcdfFile dataFile = null;
    			    
    			    // Open the file.
    			    try 
    			    {
    			    	String file = jtxtMZFile.getText();
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
     		           
           	        	mzData = new double[iSpectraCount][][];
           	        	dRetentionTimes = new double[iSpectraCount];
           	        	
           	        	for (int iSpectrum = 0; iSpectrum < iSpectraCount; iSpectrum++)
           	        	{
            	    		progressDialog.jlblProgressCaption.setText("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
                        	progressDialog.jProgressBar.setValue((int)(((double)iSpectrum / (double)iSpectraCount) * 100));

                        	if (progressDialog.m_bCancel)
                        	{
                        		return null;
                        	}

           	        		// Create the new array for the spectrum
              	        	mzData[iSpectrum] = new double[mzArray.size()][2];
              	        	
              	        	for (int i = 0; i < mzData[iSpectrum].length; i++)
            	        	{
            	        		mzData[iSpectrum][i][0] = mzArray.get(i);
            	        		mzData[iSpectrum][i][1] = 0;
            	        	}
              	        	
              	        	// Now pull the m/z values and intensities for this spectrum
              	        	int[] shape = {pointCountArray.get(iSpectrum)};
        		           	int[] origin = {scanIndexArray.get(iSpectrum)};
        		           	
              	        	ArrayFloat.D1 massDataArray = (ArrayFloat.D1)massValuesDataVar.read(origin, shape);
              	        	ArrayFloat.D1 intensityDataArray = (ArrayFloat.D1)intensityValuesDataVar.read(origin, shape);
        			    	
              	        	for (int j = 0; j < massDataArray.getSize(); j++) 
            	        	{
            	        		for (int i = 0; i < mzArray.size(); i++)
            	        		{
            	        			if (massDataArray.get(j) <= mzArray.get(i) + 0.5 && massDataArray.get(j) >= mzArray.get(i) - 0.5)
            	        				mzData[iSpectrum][i][1] += intensityDataArray.get(j);
            	        		}
            	        	}

              	        	// Now pull the retention time for this spectrum
              	        	shape[0] = 1;
              	        	origin[0] = iSpectrum;
              	        	ArrayDouble.D1 timeDataArray = (ArrayDouble.D1)timeValuesDataVar.read(origin, shape);
              	        	
              	        	dRetentionTimes[iSpectrum] = timeDataArray.get(0);
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
    			}
    			else
    			{
        			JOptionPane.showMessageDialog(null, "The specified file type is not supported.", "File type not supported", JOptionPane.ERROR_MESSAGE); 
      	  			return null;
    			}
    			
	    		progressDialog.jlblProgressCaption.setText("Loading peak finder...");

    	        // Launch the PeakFinderDialog
    	        PeakFinderDialog peakFinderDialog = new PeakFinderDialog(owner, true, m_strStandardCompoundsNameArray, m_dStandardCompoundsMZArray, m_dIsothermalParamArray);
    	        peakFinderDialog.setLocationRelativeTo(null);

    	        peakFinderDialog.setMzData(mzData);
    	        mzData = null;
    	        peakFinderDialog.setFlowRate(m_dFlowRate);
    	        peakFinderDialog.setInletPressure(m_dInletPressure);
    	        peakFinderDialog.setConstantFlowMode(jrdoConstantFlowRate.isSelected());
    	        peakFinderDialog.setInitialTime(m_dInitialTime);
    	        peakFinderDialog.setInitialTemperature(m_dInitialTemperature);
    	        peakFinderDialog.setOutletPressure(m_dOutletPressure);
    	        peakFinderDialog.setColumnLength(m_dColumnLength);
    	        peakFinderDialog.setInnerDiameter(m_dInnerDiameter);
    	        peakFinderDialog.setFilmThickness(m_dFilmThickness);
    	        peakFinderDialog.setTemperatureProgram(m_dTemperatureProgram);
    	        peakFinderDialog.setRetentionTimes(dRetentionTimes);
    	        dRetentionTimes = null;
    	        peakFinderDialog.setTemperatureProfile(m_InterpolatedTemperatureProfile);
    	        peakFinderDialog.setHoldUpTimeProfile(m_InterpolatedHoldUpProfile);
    	        peakFinderDialog.setEditable(m_bEditable);
    	        peakFinderDialog.setTStep(m_dtstep);
    	        peakFinderDialog.finalInit();
    	        
    	        progressDialog.setVisible(false);
    	        //runProgressDialogTask.cancel(false);
    	        peakFinderDialog.setVisible(true);
    	        
    	        if (peakFinderDialog.m_bOkPressed)
    	        {
    	        	m_dSelectedRetentionTimes = peakFinderDialog.m_dSelectedRetentionTimes;
    	        	m_bSkippedStandards = peakFinderDialog.m_bSkippedStandards;
    	        	m_iSelectedPeakRank = peakFinderDialog.m_iSelectedPeakRank;
    	        	m_bOkPressed = true;
    	        	
    	        	setVisible(false);
    	        }
    	        
	        	// The following is necessary to stop a memory leak. The garbage collector isn't deleting these arrays.
	        	peakFinderDialog.m_MzData = null;
	        	peakFinderDialog.m_dRetentionTimes = null;
	        	mzData = null;
	        	dRetentionTimes = null;
    		}
    		catch (MzXMLParsingException e) 
    		{
    			// Parsing of the file failed
    			e.printStackTrace();
    			
    			JOptionPane.showMessageDialog(null, "The specified file could not be loaded.", "Error loading file", JOptionPane.ERROR_MESSAGE); 
			}
            
            return null;
        }
        
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() 
        {
        	if (progressDialog != null)
        	{
        		progressDialog.setVisible(false);
        	}
        	this.owner = null;
        }
    }
    
	// This TableCellEditor, if a JTextField, automatically selects all the text when it is created.
	// This makes it so that when you type something into the cell, it removes whatever was there
	// It does not make it select all on double-click
	class TableCellEditorCustom extends DefaultCellEditor
	{
		public TableCellEditorCustom(JTextField textField) {
			super(textField);
		}
		
		@Override
	    public Component getTableCellEditorComponent(JTable table, Object value,
				 boolean isSelected,
				 int row, int column) 
	    {
    		java.awt.Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    		if (c instanceof javax.swing.JTextField)
    		{
    			JTextField jtf = ((javax.swing.JTextField)c);
    			jtf.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK, 1));
    			//Rectangle rect = table.getCellRect(row, column, false);
    			//jtf.setSize(rect.width, rect.height);
    			jtf.setHorizontalAlignment(JTextField.RIGHT);
    			jtf.selectAll();
    			//jtf.setText("");
    			//jtf.setCaretPosition(0);
    		}
    		return c;
	    }
	    
		@Override
	    public Object getCellEditorValue() 
		{
			Object obj = delegate.getCellEditorValue();
			if (obj.equals(""))
				return 0.0;
			else
				return (Double)(double)Float.valueOf((String)delegate.getCellEditorValue());
	    }
	}
	
	public class SpecialTableModel2 extends DefaultTableModel 
	{
		public SpecialTableModel2(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public SpecialTableModel2(final Object[][] data, final Object[] columnNames) 
	    {
	        setDataVector(data, columnNames);
	    }

	    public boolean isCellEditable(int row, int column) 
	    {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        /*if (column < 1) {
	            return false;
	        } else {
	            return true;
	        }*/
	    	return true;
	    }
	    
	    /*
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	}
	
	public double[] getSelectedRetentionTimes()
    {
    	return m_dSelectedRetentionTimes;
    }
    	
    public boolean[] getSkippedStandards()
    {
    	return m_bSkippedStandards;
    }

    public int[] getSelectedPeakRank()
    {
    	return m_iSelectedPeakRank;
    }
    
    public boolean getOkPressed()
    {
    	return m_bOkPressed;
    }

    public void setIsothermalData (double[][] dIsothermalParamArray)
    {
    	this.m_dIsothermalParamArray = dIsothermalParamArray;
    }
    
    public void setStandardCompoundNames (String[] strStandardCompoundNames)
    {
    	this.m_strStandardCompoundsNameArray = strStandardCompoundNames;
    }

    public void setStandardCompoundMZData (double[][] dStandardCompoundMZValues)
    {
    	this.m_dStandardCompoundsMZArray = dStandardCompoundMZValues;
    }
    
    public String getFileName()
    {
    	return this.m_strFileName;
    }
    
    public void setFileName(String strFileName)
    {
    	this.jtxtMZFile.setText(strFileName);
    }
    
	public void setTemperatureProgram(double[][] dTemperatureProgram)
	{
		tmTemperatureProgram.setRowCount(0);
		for (int i = 0; i < dTemperatureProgram.length; i++)
		{
			Object[] newRow = new Object[]{dTemperatureProgram[i][0], dTemperatureProgram[i][1], dTemperatureProgram[i][2]};
			tmTemperatureProgram.addRow(newRow);
		}
		performValidations();
	}

	public void setTemperatureProfile(double[][] TemperatureArray)
	{
		this.m_InterpolatedTemperatureProfile = new LinearInterpolationFunction(TemperatureArray);
	}
	
	public void setHoldUpTimeProfile(double[][] HoldUpArray)
	{
		this.m_InterpolatedHoldUpProfile = new InterpolationFunction(HoldUpArray);
	}
	
	public void setFlowRate(double dFlowRate)
	{
		jtxtFlowRate.setText(Float.toString((float)dFlowRate));
		performValidations();
	}
	
	public void setColumnLength(double dColumnLength)
	{
		jtxtColumnLength.setText(Float.toString((float)dColumnLength));
		performValidations();
	}
	
	public void setInnerDiameter(double dInnerDiameter)
	{
		jtxtInnerDiameter.setText(Float.toString((float)dInnerDiameter));
		performValidations();
	}
	
	public void setFilmThickness(double dFilmThickness)
	{
		jtxtFilmThickness.setText(Float.toString((float)dFilmThickness));
		performValidations();
	}

	public void setInletPressure(double dInletPressure)
	{
		jtxtPressure.setText(Float.toString((float)dInletPressure));
		performValidations();
	}

	public void setConstantFlowMode(boolean bConstantFlowMode)
	{
		if (bConstantFlowMode)
			switchToConstantFlowRateMode();
		else
			switchToConstantPressureMode();
	}
	
	public void setOutletPressure(double dOutletPressure, boolean bUnderVacuum)
	{
		if (bUnderVacuum)
			vacuumOutletPressure();
		else
		{
			jtxtOtherPressure.setText(Float.toString((float)dOutletPressure));
			otherOutletPressure();
		}
	}

	public void setInitialTime(double dInitialTime)
	{
		this.jtxtInitialTime.setText(Float.toString((float)dInitialTime));
		performValidations();
	}

	public void setInitialTemperature(double dInitialTemperature)
	{
		this.jtxtInitialTemperature.setText(Float.toString((float)dInitialTemperature));
		performValidations();
	}

	public void setTStep(double dtstep)
	{
		this.m_dtstep = dtstep;
	}

	/**
	 * @param owner
	 */
	public PeakFinderSettingsDialog(Frame owner, String[] strStationaryPhaseNames, boolean bEditable) 
	{
		super(owner);
		this.m_strStationaryPhaseArray = strStationaryPhaseNames;
		this.m_bEditable = bEditable;
		initialize();
	}
	
	public PeakFinderSettingsDialog(Frame owner, boolean modal, String[] strStationaryPhaseNames, boolean bEditable)
	{
		super(owner, modal);
		this.m_strStationaryPhaseArray = strStationaryPhaseNames;
		this.m_bEditable = bEditable;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(660, 502);
		this.setResizable(false);
		this.setTitle("Load a GC-MS data file");
		this.setContentPane(getJContentPane());
		
		// Create the Preferences class
		prefs = Preferences.userNodeForPackage(this.getClass());

        jtxtMZFile.addFocusListener(this);
        jtxtMZFile.addKeyListener(this);
        jtxtMZFile.getDocument().addDocumentListener(this);
        tmTemperatureProgram.addTableModelListener(this);
        jbtnLoadFile.addActionListener(this);
        jbtnCancel.addActionListener(this);
        jbtnOk.addActionListener(this);
        jtxtInitialTemperature.addFocusListener(this);
        jtxtInitialTemperature.addKeyListener(this);
        jtxtInitialTime.addFocusListener(this);
        jtxtInitialTime.addKeyListener(this);
        jtxtColumnLength.addFocusListener(this);
        jtxtColumnLength.addKeyListener(this);
        jtxtFlowRate.addFocusListener(this);
        jtxtFlowRate.addKeyListener(this);
        jtxtPressure.addFocusListener(this);
        jtxtPressure.addKeyListener(this);
        jtxtOtherPressure.addFocusListener(this);
        jtxtOtherPressure.addKeyListener(this);
        jtxtFilmThickness.addFocusListener(this);
        jtxtFilmThickness.addKeyListener(this);
        jtxtInnerDiameter.addFocusListener(this);
        jtxtInnerDiameter.addKeyListener(this);
        jrdoConstantFlowRate.addActionListener(this);
        jrdoConstantPressure.addActionListener(this);
        jrdoOtherPressure.addActionListener(this);
        jrdoVacuum.addActionListener(this);
        jbtnInsertRow.addActionListener(this);
        jbtnRemoveRow.addActionListener(this);
        
       	jbtnOk.setEnabled(false);
       	
       	if (!m_bEditable)
       	{
       		jcboStationaryPhase.setEnabled(false);
            jtxtInitialTemperature.setEnabled(false);
            jtxtInitialTime.setEnabled(false);
            jtxtColumnLength.setEnabled(false);
            jtxtFlowRate.setEnabled(false);
            jtxtPressure.setEnabled(false);
            jtxtOtherPressure.setEnabled(false);
            jtxtFilmThickness.setEnabled(false);
            jtxtInnerDiameter.setEnabled(false);
            jrdoConstantFlowRate.setEnabled(false);
            jrdoConstantPressure.setEnabled(false);
            jrdoOtherPressure.setEnabled(false);
            jrdoVacuum.setEnabled(false);
            jbtnInsertRow.setEnabled(false);
            jbtnRemoveRow.setEnabled(false);
            jlblStationaryPhase.setEnabled(false);
            jlblColumnLength.setEnabled(false);
            jlblColumnLengthUnit.setEnabled(false);
            jlblInnerDiameter.setEnabled(false);
            jlblInnerDiameterUnit.setEnabled(false);
            jlblFilmThickness.setEnabled(false);
            jlblFilmThicknessUnit.setEnabled(false);
            jlblFlowRate.setEnabled(false);
            jlblFlowRateUnit.setEnabled(false);
            jlblOutletPressure.setEnabled(false);
            jlblOtherPressureUnit.setEnabled(false);
            jlblGradientProgram.setEnabled(false);
            jlblInitialTemperature.setEnabled(false);
            jlblInitialTemperatureUnit.setEnabled(false);
            jlblInitialTime.setEnabled(false);
            jlblInitialTimeUnit.setEnabled(false);
            jtableTemperatureProgram.setEnabled(false);
       	}
        	
        performValidations();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jLabel.setText("GC-MS data file:");
			jLabel.setLocation(new Point(12, 32));
			jLabel.setSize(new Dimension(109, 16));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJbtnOk(), null);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getJPanel1(), null);
			jContentPane.add(getJbtnCancel(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jbtnOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnOk() {
		if (jbtnOk == null) {
			jbtnOk = new JButton();
			jbtnOk.setBounds(new Rectangle(364, 432, 137, 34));
			jbtnOk.setText("OK");
		}
		return jbtnOk;
	}

	/**
	 * This method initializes jbtnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnCancel() {
		if (jbtnCancel == null) {
			jbtnCancel = new JButton();
			jbtnCancel.setText("Cancel");
			jbtnCancel.setBounds(new Rectangle(512, 432, 137, 34));
		}
		return jbtnCancel;
	}

	/**
	 * This method initializes jtxtMZFile	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtMZFile() {
		if (jtxtMZFile == null) {
			jtxtMZFile = new JTextField();
			jtxtMZFile.setBounds(new Rectangle(128, 28, 469, 26));
			jtxtMZFile.setText("");
		}
		return jtxtMZFile;
	}

	/**
	 * This method initializes jbtnLoadFile	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnLoadFile() {
		if (jbtnLoadFile == null) {
			jbtnLoadFile = new JButton();
			jbtnLoadFile.setHorizontalTextPosition(SwingConstants.CENTER);
			jbtnLoadFile.setBounds(new Rectangle(604, 28, 29, 26));
			jbtnLoadFile.setToolTipText("");
			jbtnLoadFile.setText("...");
		}
		return jbtnLoadFile;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jlblFilmThicknessUnit = new JLabel();
			jlblFilmThicknessUnit.setBounds(new Rectangle(260, 172, 49, 16));
			jlblFilmThicknessUnit.setText("\u00b5m");
			jlblFilmThickness = new JLabel();
			jlblFilmThickness.setBounds(new Rectangle(12, 172, 169, 16));
			jlblFilmThickness.setText("Film thickness:");
			jlblInnerDiameterUnit = new JLabel();
			jlblInnerDiameterUnit.setBounds(new Rectangle(260, 148, 49, 16));
			jlblInnerDiameterUnit.setText("mm");
			jlblInnerDiameter = new JLabel();
			jlblInnerDiameter.setBounds(new Rectangle(12, 148, 168, 16));
			jlblInnerDiameter.setText("Inner diameter:");
			jlblInitialTimeUnit = new JLabel();
			jlblInitialTimeUnit.setBounds(new Rectangle(576, 120, 49, 16));
			jlblInitialTimeUnit.setText("min");
			jlblInitialTime = new JLabel();
			jlblInitialTime.setBounds(new Rectangle(328, 120, 169, 16));
			jlblInitialTime.setText("Initial time:");
			jlblInitialTime.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblInitialTemperatureUnit = new JLabel();
			jlblInitialTemperatureUnit.setBounds(new Rectangle(576, 96, 49, 16));
			jlblInitialTemperatureUnit.setText("°C");
			jlblInitialTemperature = new JLabel();
			jlblInitialTemperature.setBounds(new Rectangle(328, 96, 169, 16));
			jlblInitialTemperature.setText("Initial oven temperature:");
			jlblOtherPressureUnit = new JLabel();
			jlblOtherPressureUnit.setBounds(new Rectangle(260, 308, 49, 16));
			jlblOtherPressureUnit.setEnabled(false);
			jlblOtherPressureUnit.setText("kPa");
			jlblOtherPressureUnit.setVisible(true);
			jlblOutletPressure = new JLabel();
			jlblOutletPressure.setBounds(new Rectangle(16, 288, 136, 16));
			jlblOutletPressure.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblOutletPressure.setText("Column outlet pressure:");
			jlblOutletPressure.setVisible(true);
			jlblPressureUnit = new JLabel();
			jlblPressureUnit.setBounds(new Rectangle(260, 264, 49, 16));
			jlblPressureUnit.setEnabled(false);
			jlblPressureUnit.setText("kPa");
			jlblPressureUnit.setVisible(true);
			jlblPressure = new JLabel();
			jlblPressure.setBounds(new Rectangle(32, 264, 149, 16));
			jlblPressure.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblPressure.setText("Column inlet pressure:");
			jlblPressure.setEnabled(false);
			jlblFlowRateUnit = new JLabel();
			jlblFlowRateUnit.setBounds(new Rectangle(260, 220, 49, 16));
			jlblFlowRateUnit.setText("mL/min");
			jlblFlowRate = new JLabel();
			jlblFlowRate.setBounds(new Rectangle(32, 220, 149, 16));
			jlblFlowRate.setText("Flow rate:");
			jlblFlowRate.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jlblGradientProgram = new JLabel();
			jlblGradientProgram.setBounds(new Rectangle(328, 72, 154, 16));
			jlblGradientProgram.setText("Temperature program:");
			jLabel2 = new JLabel();
			jLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel2.setBounds(new Rectangle(8, 0, 605, 37));
			jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel2.setText("<html><center>Enter your approximate GC conditions to help identify the correct peaks and smooth your data properly. </center></html>");
			jlblColumnLengthUnit = new JLabel();
			jlblColumnLengthUnit.setBounds(new Rectangle(260, 124, 49, 16));
			jlblColumnLengthUnit.setText("m");
			jlblColumnLength = new JLabel();
			jlblColumnLength.setBounds(new Rectangle(12, 124, 169, 16));
			jlblColumnLength.setText("Column length:");
			jlblStationaryPhase = new JLabel();
			jlblStationaryPhase.setBounds(new Rectangle(12, 72, 118, 16));
			jlblStationaryPhase.setText("Stationary phase:");
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(4, 80, 645, 345));
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "Enter approximate GC conditions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.add(jlblStationaryPhase, null);
			jPanel.add(getJcboStationaryPhase(), null);
			jPanel.add(jlblColumnLength, null);
			jPanel.add(getJtxtColumnLength(), null);
			jPanel.add(jlblColumnLengthUnit, null);
			jPanel.add(getJScrollPane(), null);
			jPanel.add(getJbtnInsertRow(), null);
			jPanel.add(getJbtnRemoveRow(), null);
			jPanel.add(jlblGradientProgram, null);
			jPanel.add(getJPanel2(), null);
			jPanel.add(getJrdoConstantFlowRate(), null);
			jPanel.add(jlblFlowRate, null);
			jPanel.add(getJtxtFlowRate(), null);
			jPanel.add(jlblFlowRateUnit, null);
			jPanel.add(getJrdoConstantPressure(), null);
			jPanel.add(jlblPressure, null);
			jPanel.add(getJtxtPressure(), null);
			jPanel.add(jlblPressureUnit, null);
			jPanel.add(jlblOutletPressure, null);
			jPanel.add(getJrdoVacuum(), null);
			jPanel.add(getJrdoOtherPressure(), null);
			jPanel.add(getJtxtOtherPressure(), null);
			jPanel.add(jlblOtherPressureUnit, null);
			jPanel.add(jlblInitialTemperature, null);
			jPanel.add(getJtxtInitialTemperature(), null);
			jPanel.add(jlblInitialTemperatureUnit, null);
			jPanel.add(jlblInitialTime, null);
			jPanel.add(getJtxtInitialTime(), null);
			jPanel.add(jlblInitialTimeUnit, null);
			jPanel.add(jlblInnerDiameter, null);
			jPanel.add(getJtxtInnerDiameter(), null);
			jPanel.add(jlblInnerDiameterUnit, null);
			jPanel.add(jlblFilmThickness, null);
			jPanel.add(getJtxtFilmThickness(), null);
			jPanel.add(jlblFilmThicknessUnit, null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(null);
			jPanel1.setBounds(new Rectangle(4, 4, 645, 65));
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Choose a GC-MS data file", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel1.add(jLabel, null);
			jPanel1.add(getJtxtMZFile(), null);
			jPanel1.add(getJbtnLoadFile(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jcboStationaryPhase	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboStationaryPhase() {
		if (jcboStationaryPhase == null) {
			jcboStationaryPhase = new JComboBox(Globals.StationaryPhaseArray);
			jcboStationaryPhase.setBounds(new Rectangle(12, 92, 297, 26));
		}
		return jcboStationaryPhase;
	}

	/**
	 * This method initializes jtxtColumnLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtColumnLength() {
		if (jtxtColumnLength == null) {
			jtxtColumnLength = new JTextField();
			jtxtColumnLength.setHorizontalAlignment(JTextField.TRAILING);
			jtxtColumnLength.setBounds(new Rectangle(184, 120, 73, 26));
			jtxtColumnLength.setText("30.0");
		}
		return jtxtColumnLength;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(328, 152, 301, 133));
			jScrollPane.setViewportView(getJtableTemperatureProgram());
		}
		return jScrollPane;
	}

	private JTable getJtableTemperatureProgram() {
		if (jtableTemperatureProgram == null) 
		{
			Object[] columnNames = {"Ramp (\u00b0C/min)", "Final temp (\u00b0C)", "Hold time (min)"};
			Double[][] data = {{20.0, 260.0, 5.0}};
	        
			tmTemperatureProgram = new SpecialTableModel2(data, columnNames);

			jtableTemperatureProgram = new JTable(tmTemperatureProgram);
			
			jtableTemperatureProgram.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			jtableTemperatureProgram.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtableTemperatureProgram.getTableHeader().setPreferredSize(new Dimension(22, 22));
			jtableTemperatureProgram.getColumnModel().getColumn(0).setPreferredWidth(85);
			
			JTextField jtf = new JTextField();
			TableCellEditorCustom cellEditor = new TableCellEditorCustom(jtf);
			jtableTemperatureProgram.getColumnModel().getColumn(0).setCellEditor(cellEditor);
			jtableTemperatureProgram.getColumnModel().getColumn(1).setCellEditor(cellEditor);
			jtableTemperatureProgram.getColumnModel().getColumn(2).setCellEditor(cellEditor);
		}
		return jtableTemperatureProgram;
	}

	/**
	 * This method initializes jbtnInsertRow	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnInsertRow() {
		if (jbtnInsertRow == null) {
			jbtnInsertRow = new JButton();
			jbtnInsertRow.setBounds(new Rectangle(328, 292, 137, 34));
			jbtnInsertRow.setText("Insert Row");
		}
		return jbtnInsertRow;
	}

	/**
	 * This method initializes jbtnRemoveRow	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnRemoveRow() {
		if (jbtnRemoveRow == null) {
			jbtnRemoveRow = new JButton();
			jbtnRemoveRow.setBounds(new Rectangle(492, 292, 137, 34));
			jbtnRemoveRow.setText("Remove Row");
			jbtnRemoveRow.setActionCommand("Remove Row");
		}
		return jbtnRemoveRow;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(null);
			jPanel2.setBounds(new Rectangle(12, 28, 621, 37));
			jPanel2.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jPanel2.add(jLabel2, null);
		}
		return jPanel2;
	}

    private void validateColumnLength()
    {
    	if (jtxtColumnLength.getText().length() == 0)
    		jtxtColumnLength.setText("0");
    	
		double dTemp = (double)Float.valueOf(jtxtColumnLength.getText());
		
		if (dTemp < 0.1)
			dTemp = 0.1;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.m_dColumnLength = dTemp;
		jtxtColumnLength.setText(Float.toString((float)m_dColumnLength));    	
    }

    private void validateFlowRate()
    {
    	if (jtxtFlowRate.getText().length() == 0)
    		jtxtFlowRate.setText("0");

    	double dTemp = (double)Float.valueOf(jtxtFlowRate.getText());
		
		if (dTemp < 0.000000001)
			dTemp = 0.000000001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.m_dFlowRate = dTemp;
		jtxtFlowRate.setText(Float.toString((float)m_dFlowRate));    	
    }
    
    private void validateOtherOutletPressure()
    {
    	if (jrdoOtherPressure.isSelected() == false)
    		return;

    	if (jtxtOtherPressure.getText().length() == 0)
    		jtxtOtherPressure.setText("0");

		double dTemp = (double)Float.valueOf(jtxtOtherPressure.getText());
		
		if (dTemp < 0.000001)
			dTemp = 0.000001;
		if (dTemp > 101.325)
			dTemp = 101.325;
		
		this.m_dOutletPressure = dTemp;
		jtxtOtherPressure.setText(Float.toString((float)m_dOutletPressure));    	
    }

    private void validateInletPressure()
    {
    	if (jtxtPressure.getText().length() == 0)
    		jtxtPressure.setText("0");

		double dTemp = (double)Float.valueOf(jtxtPressure.getText());
		
		if (dTemp < 0.000000001)
			dTemp = 0.000000001;
		if (dTemp > 100000)
			dTemp = 100000;
	
		this.m_dInletPressure = dTemp;			
		jtxtPressure.setText(Float.toString((float)m_dInletPressure));    	
    }
    
    private void validateInitialTime()
    {
    	if (jtxtInitialTime.getText().length() == 0)
    		jtxtInitialTime.setText("0");

		double dTemp = (double)Float.valueOf(jtxtInitialTime.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 1000)
			dTemp = 1000;
		
		this.m_dInitialTime = dTemp;
		jtxtInitialTime.setText(Float.toString((float)dTemp));    	
    }
    
    private void validateInitialTemperature()
    {
    	if (jtxtInitialTemperature.getText().length() == 0)
    		jtxtInitialTemperature.setText("0");

		double dTemp = (double)Float.valueOf(jtxtInitialTemperature.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 500)
			dTemp = 500;
		
		if (tmTemperatureProgram.getRowCount() > 0)
		{
			double dFirstTemp = (Double)tmTemperatureProgram.getValueAt(0, 1);
			if (dTemp > dFirstTemp)
				dTemp = dFirstTemp;
		}
		
		this.m_dInitialTemperature = dTemp;
		jtxtInitialTemperature.setText(Float.toString((float)dTemp));    	
    }
    
    private void validateInnerDiameter()
    {
    	if (jtxtInnerDiameter.getText().length() == 0)
    		jtxtInnerDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(jtxtInnerDiameter.getText());
		
		if (dTemp < 0.00001)
			dTemp = 0.00001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		if (dTemp - (2 * this.m_dFilmThickness / 1000) > 0)
			this.m_dInnerDiameter = dTemp;
		else
			this.m_dInnerDiameter = ((this.m_dFilmThickness * 2) / 1000) - 0.0001;

		jtxtInnerDiameter.setText(Float.toString((float)m_dInnerDiameter));    	
    }
    
    private void validateFilmThickness()
    {
    	if (jtxtFilmThickness.getText().length() == 0)
    		jtxtFilmThickness.setText("0");

    	double dTemp = (double)Float.valueOf(jtxtFilmThickness.getText());
		
		if (dTemp < 0.00001)
			dTemp = 0.00001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		if (this.m_dInnerDiameter - (2 * dTemp / 1000) > 0)
			this.m_dFilmThickness = dTemp;
		else
			this.m_dFilmThickness = ((this.m_dInnerDiameter / 2) * 1000) - 0.0001;
		
		jtxtFilmThickness.setText(Float.toString((float)m_dFilmThickness));    	
    }
    
	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		performValidations();		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) 
	{
		
		if (e.getSource() == this.jtxtMZFile)
		{
			
		}
		else
		{
			if (!((Character.isDigit(e.getKeyChar()) ||
					(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
					(e.getKeyChar() == KeyEvent.VK_DELETE) ||
					(e.getKeyChar() == KeyEvent.VK_PERIOD))))
			{
		        e.consume();
			}
		}
		
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
		{
			performValidations();
		}
	}

	public void performValidations()
	{
		validateInitialTemperature();
		validateInitialTime();
		validateColumnLength();
		validateInnerDiameter();
		validateFilmThickness();
		validateFlowRate();
		validateInletPressure();
		validateOtherOutletPressure();
		
	    // Add in data points for the ideal temperature program series
    	this.m_dTemperatureProgram = new double[(tmTemperatureProgram.getRowCount() * 2) + 2][2];
    	int iPointCount = 0;

    	this.m_dTemperatureProgram[iPointCount][0] = 0.0;
    	this.m_dTemperatureProgram[iPointCount][1] = m_dInitialTemperature;
		iPointCount++;
		
    	this.m_dTemperatureProgram[iPointCount][0] = m_dInitialTime;
    	this.m_dTemperatureProgram[iPointCount][1] = m_dInitialTemperature;
		iPointCount++;

    	double dTotalTime = m_dInitialTime;
    	double dLastTemp = m_dInitialTemperature;
    	double dFinalTemp = m_dInitialTemperature;
    	
    	// Go through the temperature program table and create an array that contains temp vs. time
		for (int i = 0; i < tmTemperatureProgram.getRowCount(); i++)
		{
			double dRamp = (Double)tmTemperatureProgram.getValueAt(i, 0);
			dFinalTemp = (Double)tmTemperatureProgram.getValueAt(i, 1);
			double dFinalTime = (Double)tmTemperatureProgram.getValueAt(i, 2);

			if (dRamp != 0)
			{
				dTotalTime += (dFinalTemp - dLastTemp) / dRamp;
				this.m_dTemperatureProgram[iPointCount][0] = dTotalTime;
				this.m_dTemperatureProgram[iPointCount][1] = dFinalTemp;
				iPointCount++;
			}
			
			if (dFinalTime != 0)
			{
				if (i < tmTemperatureProgram.getRowCount() - 1)
				{
					dTotalTime += dFinalTime;
					this.m_dTemperatureProgram[iPointCount][0] = dTotalTime;
					this.m_dTemperatureProgram[iPointCount][1] = dFinalTemp;
					iPointCount++;						
				}
			}
			
			dLastTemp = dFinalTemp;
		}
		
    	this.m_dTemperatureProgram[iPointCount][0] = this.m_dTemperatureProgram[iPointCount - 1][0] * 2;
    	this.m_dTemperatureProgram[iPointCount][1] = dFinalTemp;
		iPointCount++;

		// Ideal series finished
		// Now cut it down to the correct size
		double tempArray[][] = new double[iPointCount][2];
		for (int i = 0; i < iPointCount; i++)
		{
			tempArray[i][0] = this.m_dTemperatureProgram[i][0];
			tempArray[i][1] = this.m_dTemperatureProgram[i][1];
		}
		this.m_dTemperatureProgram = tempArray;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == this.jbtnLoadFile)
		{
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter1 = new FileNameExtensionFilter("mzXML Files (*.mzXML)", "mzXML");
			FileNameExtensionFilter filter2 = new FileNameExtensionFilter("mzML Files (*.mzML)", "mzML");
			FileNameExtensionFilter filter3 = new FileNameExtensionFilter("netCDF Files (*.CDF)", "CDF");
			chooser.addChoosableFileFilter(filter1);
			chooser.addChoosableFileFilter(filter2);
			chooser.addChoosableFileFilter(filter3);
			
			// Set default directory
			String lastOutputDir = prefs.get("LAST_OUTPUT_DIR", "");
			if (lastOutputDir != "")
			{
				File lastDir = new File(lastOutputDir);
				if (lastDir.exists())
					chooser.setCurrentDirectory(lastDir);
			}
			
			int returnVal = chooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
				this.jtxtMZFile.setText(chooser.getSelectedFile().getPath());
				prefs.put("LAST_OUTPUT_DIR", chooser.getSelectedFile().getAbsolutePath());
			}
		}
		else if (e.getSource() == jbtnInsertRow)
	    {
			int iSelectedRow = jtableTemperatureProgram.getSelectedRow();
	    	
	    	if (jtableTemperatureProgram.getRowCount() == 0)
	    	{
		    	Double dRowValue1 = 20.0;
		    	Double dRowValue2 = m_dInitialTemperature;
		    	Double dRowValue3 = 5.0;
		    	Double dRowData[] = {dRowValue1, dRowValue2, dRowValue3};
		    	tmTemperatureProgram.addRow(dRowData);	    		
	    	}
	    	else if (iSelectedRow == -1)
	    	{
		    	Double dRowValue1 = 0.0;
		    	Double dRowValue2 = (Double) jtableTemperatureProgram.getValueAt(jtableTemperatureProgram.getRowCount() - 1, 1);
		    	Double dRowValue3 = 5.0;
		    	Double dRowData[] = {dRowValue1, dRowValue2, dRowValue3};
		    	tmTemperatureProgram.addRow(dRowData);	    		
	    	}
	    	else
	    	{
		    	Double dRowValue1 = 0.0;
		    	Double dRowValue2 = (Double)jtableTemperatureProgram.getValueAt(iSelectedRow, 1);
		    	Double dRowValue3 = 5.0;
		    	Double dRowData[] = {dRowValue1, dRowValue2, dRowValue3};
		    	tmTemperatureProgram.insertRow(iSelectedRow, dRowData);	    		
	    	}
	    }
	    else if (e.getSource() == jbtnRemoveRow)
	    {
	    	int iSelectedRow = jtableTemperatureProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = jtableTemperatureProgram.getRowCount() - 1;
	    	
	    	if (iSelectedRow >= 0)
	    	{
	    		tmTemperatureProgram.removeRow(iSelectedRow);
	    	}
	    }
	    else if (e.getSource() == jbtnOk)
	    {
	    	// Check if the cell editor is currently open in the table
	    	if (jtableTemperatureProgram.isEditing())
	    	{
	    		jtableTemperatureProgram.getCellEditor().stopCellEditing();
	    	}

	    	performValidations();
	    	
        	progressDialog = new ProgressDialog(this);
        	progressDialog.setLocationByPlatform(true);

        	OpenMzXMLFileTask openMzXMLFileTask = new OpenMzXMLFileTask(this);
	    	openMzXMLFileTask.execute();
	    	
	    	progressDialog.setVisible(true);
	    	
	    	// Set the file name variable
	    	m_strFileName = this.jtxtMZFile.getText();
	    	
    		//progressDialog = new ProgressDialog(this);
    		//progressDialog.setLocationByPlatform(true);
	    }
	    else if (e.getSource() == jbtnCancel)
	    {
	    	this.setVisible(false);
	    }
	    else if (e.getActionCommand() == "Constant flow rate mode")
	    {
    		switchToConstantFlowRateMode();
	    }
	    else if (e.getActionCommand() == "Constant pressure mode")
	    {
    		switchToConstantPressureMode();
	    }
	    else if (e.getActionCommand() == "Vacuum")
	    {
    		vacuumOutletPressure();
	    }
	    else if (e.getActionCommand() == "OtherPressure")
	    {
	    	otherOutletPressure();
	    }	
	}

    public void vacuumOutletPressure()
    {
    	jrdoVacuum.setSelected(true);
    	jrdoOtherPressure.setSelected(false);
    	
    	if (this.m_bEditable)
    	{
	    	jtxtOtherPressure.setEnabled(false);
	    	jlblOtherPressureUnit.setEnabled(false);
    	}
    	
    	this.m_dOutletPressure = .000001;
    	performValidations();
    }
    
    public void otherOutletPressure()
    {
    	jrdoVacuum.setSelected(false);
    	jrdoOtherPressure.setSelected(true);
    	
    	if (this.m_bEditable)
    	{
	    	jtxtOtherPressure.setEnabled(true);
	    	jlblOtherPressureUnit.setEnabled(true);
    	}
    	
    	this.m_dOutletPressure = 100; // 100 kPa
    	performValidations();
    }
    
    public void switchToConstantPressureMode()
    {
    	jrdoConstantPressure.setSelected(true);
    	jrdoConstantFlowRate.setSelected(false);

    	if (this.m_bEditable)
    	{
	    	jlblFlowRate.setEnabled(false);
	    	jlblFlowRateUnit.setEnabled(false);
	    	jtxtFlowRate.setEnabled(false);
	    	jlblPressure.setEnabled(true);
	    	jtxtPressure.setEnabled(true);
	    	jlblPressureUnit.setEnabled(true);
    	}
    	performValidations();
    }
    
    public void switchToConstantFlowRateMode()
    {
    	jrdoConstantFlowRate.setSelected(true);
    	jrdoConstantPressure.setSelected(false);
	    	
    	if (this.m_bEditable)
    	{
	    	jlblFlowRate.setEnabled(true);
	    	jlblFlowRateUnit.setEnabled(true);
	    	jtxtFlowRate.setEnabled(true);
	    	jlblPressure.setEnabled(false);
	    	jtxtPressure.setEnabled(false);
	    	jlblPressureUnit.setEnabled(false);
    	}
    	performValidations();
    }
    
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		performValidations();
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (m_bDoNotChangeTable)
		{
			m_bDoNotChangeTable = false;
			return;
		}
		
		if (e.getSource() == tmTemperatureProgram)
		{
			int row = e.getFirstRow();
			int column = e.getColumn();
			
			if (column == 0)
			{
				Double dNewValue = (Double) tmTemperatureProgram.getValueAt(row, 0);
				
				double dTemp = dNewValue;
				if (dTemp < 0)
					dTemp = 0;
				if (dTemp > 1000)
					dTemp = 1000;
				
		    	m_bDoNotChangeTable = true;
				tmTemperatureProgram.setValueAt(dTemp, row, column);
			}
			else if (column == 1)
			{
				Double dNewValue = (Double) tmTemperatureProgram.getValueAt(row, 1);
				
				double dTemp = dNewValue;
				if (dTemp < 0)
					dTemp = 0;
				if (dTemp > 500)
					dTemp = 500;
				if (row == 0)
				{
					if (dTemp < (Double)this.m_dInitialTemperature)
					{
						dTemp = this.m_dInitialTemperature;
					}
				}
				if (row < tmTemperatureProgram.getRowCount() - 1)
				{
					if (dTemp > (Double)tmTemperatureProgram.getValueAt(row + 1, column))
					{
						dTemp = (Double)tmTemperatureProgram.getValueAt(row + 1, column);
					}
				}
				if (row > 0)
				{
					if (dTemp < (Double)tmTemperatureProgram.getValueAt(row - 1, column))
					{
						dTemp = (Double)tmTemperatureProgram.getValueAt(row - 1, column);
					}
				}
		    	m_bDoNotChangeTable = true;
				tmTemperatureProgram.setValueAt(dTemp, row, column);
			}
			else if (column == 2)
			{
				Double dNewValue = (Double)tmTemperatureProgram.getValueAt(row, 2);
				
				double dTemp = dNewValue;
				if (dTemp < 0)
					dTemp = 0;
				if (dTemp > 1000)
					dTemp = 1000;
				
		    	m_bDoNotChangeTable = true;
				tmTemperatureProgram.setValueAt(dTemp, row, column);
			}
		}
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) 
	{
		if (this.jtxtMZFile.getText().length() == 0)
			this.jbtnOk.setEnabled(false);
		else
			this.jbtnOk.setEnabled(true);
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		if (this.jtxtMZFile.getText().length() == 0)
			this.jbtnOk.setEnabled(false);
		else
			this.jbtnOk.setEnabled(true);
		
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		if (this.jtxtMZFile.getText().length() == 0)
			this.jbtnOk.setEnabled(false);
		else
			this.jbtnOk.setEnabled(true);
	}

	/**
	 * This method initializes jrdoConstantFlowRate	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoConstantFlowRate() {
		if (jrdoConstantFlowRate == null) {
			jrdoConstantFlowRate = new JRadioButton();
			jrdoConstantFlowRate.setBounds(new Rectangle(12, 196, 297, 20));
			jrdoConstantFlowRate.setSelected(true);
			jrdoConstantFlowRate.setText("Constant flow rate mode");
		}
		return jrdoConstantFlowRate;
	}

	/**
	 * This method initializes jtxtFlowRate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtFlowRate() {
		if (jtxtFlowRate == null) {
			jtxtFlowRate = new JTextField();
			jtxtFlowRate.setBounds(new Rectangle(184, 216, 73, 26));
			jtxtFlowRate.setText("1.0");
		}
		return jtxtFlowRate;
	}

	/**
	 * This method initializes jrdoConstantPressure	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoConstantPressure() {
		if (jrdoConstantPressure == null) {
			jrdoConstantPressure = new JRadioButton();
			jrdoConstantPressure.setBounds(new Rectangle(12, 240, 166, 20));
			jrdoConstantPressure.setText("Constant pressure mode");
		}
		return jrdoConstantPressure;
	}

	/**
	 * This method initializes jtxtPressure	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtPressure() {
		if (jtxtPressure == null) {
			jtxtPressure = new JTextField();
			jtxtPressure.setBounds(new Rectangle(184, 260, 73, 26));
			jtxtPressure.setText("100");
			jtxtPressure.setEditable(true);
			jtxtPressure.setActionCommand("Inlet Pressure");
			jtxtPressure.setEnabled(false);
		}
		return jtxtPressure;
	}

	/**
	 * This method initializes jrdoVacuum	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoVacuum() {
		if (jrdoVacuum == null) {
			jrdoVacuum = new JRadioButton();
			jrdoVacuum.setBounds(new Rectangle(12, 308, 77, 20));
			jrdoVacuum.setSelected(true);
			jrdoVacuum.setText("Vacuum");
		}
		return jrdoVacuum;
	}

	/**
	 * This method initializes jrdoOtherPressure	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoOtherPressure() {
		if (jrdoOtherPressure == null) {
			jrdoOtherPressure = new JRadioButton();
			jrdoOtherPressure.setBounds(new Rectangle(104, 308, 77, 20));
			jrdoOtherPressure.setActionCommand("OtherPressure");
			jrdoOtherPressure.setText("Other:");
		}
		return jrdoOtherPressure;
	}

	/**
	 * This method initializes jtxtOtherPressure	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtOtherPressure() {
		if (jtxtOtherPressure == null) {
			jtxtOtherPressure = new JTextField();
			jtxtOtherPressure.setBounds(new Rectangle(184, 304, 73, 26));
			jtxtOtherPressure.setText("101.325");
			jtxtOtherPressure.setEnabled(false);
		}
		return jtxtOtherPressure;
	}

	/**
	 * This method initializes jtxtInitialTemperature	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInitialTemperature() {
		if (jtxtInitialTemperature == null) {
			jtxtInitialTemperature = new JTextField();
			jtxtInitialTemperature.setBounds(new Rectangle(500, 92, 73, 26));
			jtxtInitialTemperature.setText("60.0");
		}
		return jtxtInitialTemperature;
	}

	/**
	 * This method initializes jtxtInitialTime	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInitialTime() {
		if (jtxtInitialTime == null) {
			jtxtInitialTime = new JTextField();
			jtxtInitialTime.setBounds(new Rectangle(500, 116, 73, 26));
			jtxtInitialTime.setText("5");
		}
		return jtxtInitialTime;
	}

	/**
	 * This method initializes jtxtInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInnerDiameter() {
		if (jtxtInnerDiameter == null) {
			jtxtInnerDiameter = new JTextField();
			jtxtInnerDiameter.setHorizontalAlignment(JTextField.TRAILING);
			jtxtInnerDiameter.setBounds(new Rectangle(184, 144, 73, 26));
			jtxtInnerDiameter.setText("0.25");
		}
		return jtxtInnerDiameter;
	}

	/**
	 * This method initializes jtxtFilmThickness	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtFilmThickness() {
		if (jtxtFilmThickness == null) {
			jtxtFilmThickness = new JTextField();
			jtxtFilmThickness.setHorizontalAlignment(JTextField.TRAILING);
			jtxtFilmThickness.setBounds(new Rectangle(184, 168, 73, 26));
			jtxtFilmThickness.setText("0.25");
		}
		return jtxtFilmThickness;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
