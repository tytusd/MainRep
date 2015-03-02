package boswell.peakfinderlc;

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
import java.util.prefs.Preferences;

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

public class PeakFinderSettingsDialog extends JDialog implements FocusListener, KeyListener, ActionListener, ListSelectionListener, TableModelListener, DocumentListener
{
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jbtnOk = null;
	private JButton jbtnCancel = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JTextField jtxtMZFile = null;
	private JButton jbtnLoadFile = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JLabel jlblStationaryPhase = null;
	private JComboBox jcboStationaryPhase = null;
	private JLabel jlblColumnInnerDiameter = null;
	private JTextField jtxtInnerDiameter = null;
	private JLabel jlblInnerDiameterUnit = null;
	private JLabel jlblColumnLength = null;
	private JTextField jtxtColumnLength = null;
	private JLabel jlblColumnLengthUnit = null;
	private JTextField jtxtFlowRate = null;
	private JLabel jlblFlowRateUnit = null;
	private JScrollPane jScrollPane = null;
	private JTable jtableGradientProgram = null;
	private JButton jbtnInsertRow = null;
	private JButton jbtnRemoveRow = null;
	private JLabel jLabel2 = null;
	private JLabel jlblGradientDelay = null;
	private JLabel jlblMixingVolume = null;
	private JLabel jlblNonMixingVolume = null;
	private JTextField jtxtMixingVolume = null;
	private JLabel jlblMixingVolumeUnits = null;
	private JTextField jtxtNonMixingVolume = null;
	private JLabel jlblNonMixingVolumeUnits = null;
	private JLabel jlblGradientProgram = null;
	private JPanel jPanel2 = null;
	
	private Preferences prefs;

	public SpecialTableModel2 tmGradientProgram;

	public LinearInterpolationFunction m_InterpolatedGradientProfile;  //  @jve:decl-index=0:
	public LinearInterpolationFunction m_InterpolatedDeadTimeProfile;  //  @jve:decl-index=0:

	public int m_iStationaryPhase = 0;
	public double m_dColumnLength = 100; // in mm
	public double m_dColumnInnerDiameter = 2.1; // in mm
	public double m_dProgramTime = 20;
	public double m_dFlowRate = 1; // in mL/min
    public double m_dMixingVolume = 0.001;
    public double m_dNonMixingVolume = 0.001;
    public boolean m_bDoNotChangeTable = false;
    public double m_dInstrumentDeadTime = 0;
    
    public double[] m_dSelectedRetentionTimes = null;
    public boolean[] m_bSkippedStandards = null;
    public int[] m_iSelectedPeakRank = null;
    public boolean m_bOkPressed = false;
    
    String[] m_strStationaryPhaseArray = null;
    String[] m_strStandardCompoundsNameArray = null;
    double[][] m_dStandardCompoundsMZArray = null;
    double[][][] m_dIsocraticDataArray = null;
    
    boolean m_bEditable = true;

    String m_strFileName = null;
    
    double m_dtstep = 0;

    public double[][] m_dGradientProgram = null;

	public ProgressDialog progressDialog = null;

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
        	int iPositiveSpectraCount;
        	int iNegativeSpectraCount;
        	double[][][] mzData = null;
        	double[] dRetentionTimes = null;
        	
        	Vector<Double> mzDataMassList = new Vector<Double>();
        	
        	// Create an m/z array that contains all the values we need EICs of
        	for (int i = 0; i < m_dStandardCompoundsMZArray.length; i++)
        	{
        		for (int j = 0; j < m_dStandardCompoundsMZArray[i].length; j++)
        		{
        			double thisMZ = m_dStandardCompoundsMZArray[i][j];
        			boolean bFound = false;
        			
        			for (int k = 0; k < mzDataMassList.size(); k++)
        			{
        				if (mzDataMassList.get(k) == thisMZ)
        				{
        					bFound = true;
        					break;
        				}
        			}
        			
        			if (!bFound)
        			{
        				mzDataMassList.add(thisMZ);
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
    			
    			// Parse mzXML file
    			if (fileExtension.toUpperCase().equals("MZXML"))
    			{
    				File mzFile = new File(jtxtMZFile.getText());
                    if (!mzFile.exists())
                    {
            			JOptionPane.showMessageDialog(null, "The specified file does not exist.", "File does not exist", JOptionPane.ERROR_MESSAGE); 
              	  		return null;
                    }

                	// Launch the progress dialog in a separate thread
                    progressDialog.jProgressBar.setIndeterminate(true);
            		String fileName = new File(jtxtMZFile.getText()).getName();
            		progressDialog.jlblProgressCaption.setText("Parsing " + fileName);
                	
                    DatatypeFactory dfactory = null;
            		try {
            			dfactory = DatatypeFactory.newInstance();
            		} catch (DatatypeConfigurationException e) {
            			e.printStackTrace();
            		}
            		
        			JMzReader inputParser;
    				inputParser = new MzXMLFile(mzFile);
    				
    	    		progressDialog.jlblProgressCaption.setText("Analyzing spectra...");

                	// Count the number of positive and negative ion spectra that are MS level 1
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
            			JOptionPane.showMessageDialog(null, "There is no MS level 1 data in the file.", "No MS Spectra in File", JOptionPane.ERROR_MESSAGE); 
              	  		return null;
                    }

    				// Create the mzData array to be the right size for each ion
    				mzData = new double[m_dStandardCompoundsMZArray.length][][];
        	        for (int j = 0; j < mzData.length; j++)
        	        {
        	        	if (m_dStandardCompoundsMZArray[j][0] > 0)
        	        		mzData[j] = new double[iPositiveSpectraCount][2];
        	        	else
        	        		mzData[j] = new double[iNegativeSpectraCount][2];
        	        }
        	        
    				progressDialog.jProgressBar.setIndeterminate(false);
                	progressDialog.jProgressBar.setStringPainted(true);
                	progressDialog.jProgressBar.setValue(0);

        	        // use the iterator to access all spectra in the file
        	        iterator = inputParser.getSpectrumIterator();
    				int iSpectrum = 0;
    				int iPositiveSpectrum = 0;
    				int iNegativeSpectrum = 0;

    				// Walk through each of the spectra in the file
    				while(iterator.hasNext() && iSpectrum < iSpectraCount) 
        	        {
        	    		progressDialog.jlblProgressCaption.setText("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
                    	progressDialog.jProgressBar.setValue((int)(((double)iSpectrum / (double)iSpectraCount) * 100));

                    	if (progressDialog.m_bCancel)
                    	{
                    		return null;
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
    						if (bPositivePolarity && m_dStandardCompoundsMZArray[j][0] > 0)
    						{
    							mzData[j][iPositiveSpectrum][0] = dRetentionTime;
    							mzData[j][iPositiveSpectrum][1] = 0;
    						}
    						else if (!bPositivePolarity && m_dStandardCompoundsMZArray[j][0] < 0)
    						{
    							mzData[j][iNegativeSpectrum][0] = dRetentionTime;
    							mzData[j][iNegativeSpectrum][1] = 0;
    						}
    					}
	        	        
    					// Retrieve the spectrum's peak list
	        	        Map<Double, Double> peakList = spectrum.getPeakList();

	        	        // Fill in mzData with the intensities at each mass
        	        	for (Double mz : peakList.keySet()) 
        	        	{
        	        		for (int i = 0; i < m_dStandardCompoundsMZArray.length; i++)
        	        		{
        	        			for (int massnum = 0; massnum < m_dStandardCompoundsMZArray[i].length; massnum++)
        	        			{
	        	        			if (bPositivePolarity)
	        	        			{
	        	        				if (mz <= m_dStandardCompoundsMZArray[i][massnum] + 0.5 && mz >= m_dStandardCompoundsMZArray[i][massnum] - 0.5)
	        	        					mzData[i][iPositiveSpectrum][1] += peakList.get(mz);
	        	        			}
	        	        			else
	        	        			{
	        	         				if (mz <= (-m_dStandardCompoundsMZArray[i][massnum]) + 0.5 && mz >= (-m_dStandardCompoundsMZArray[i][massnum]) - 0.5)
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
    			}
    			// Parse mzML file
    			else if (fileExtension.toUpperCase().equals("MZML"))
    			{
    	            File mzFile = new File(jtxtMZFile.getText());
    	            if (!mzFile.exists())
    	            {
    	    			JOptionPane.showMessageDialog(null, "The specified file does not exist.", "File does not exist", JOptionPane.ERROR_MESSAGE); 
    	      	  		return null;
    	            }

    	        	// Launch the progress dialog in a separate thread
    	        	progressDialog.jProgressBar.setIndeterminate(true);
    	    		String fileName = new File(jtxtMZFile.getText()).getName();
    	    		progressDialog.jlblProgressCaption.setText("Parsing " + fileName);
    	        	
    	            // Create the unmarshaller
    				MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(mzFile);
    				
    	    		progressDialog.jlblProgressCaption.setText("Analyzing spectra...");

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
              	  		return null;
                    }

    				//mzData[m/z][
    				
    				// Create the mzData array to be the right size for each ion
    				mzData = new double[m_dStandardCompoundsMZArray.length][][];
        	        for (int j = 0; j < mzData.length; j++)
        	        {
        	        	if (m_dStandardCompoundsMZArray[j][0] > 0)
        	        		mzData[j] = new double[iPositiveSpectraCount][2];
        	        	else
        	        		mzData[j] = new double[iNegativeSpectraCount][2];
        	        }
        	        
    				progressDialog.jProgressBar.setIndeterminate(false);
                	progressDialog.jProgressBar.setStringPainted(true);
                	progressDialog.jProgressBar.setValue(0);

    				//dealing with element collections
    				spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", uk.ac.ebi.jmzml.model.mzml.Spectrum.class);
    				int iSpectrum = 0;
    				int iPositiveSpectrum = 0;
    				int iNegativeSpectrum = 0;
    				// Walk through each of the spectra in the file
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
	    						if (bPositivePolarity && m_dStandardCompoundsMZArray[j][0] > 0)
	    						{
	    							mzData[j][iPositiveSpectrum][0] = dRetentionTime;
	    							mzData[j][iPositiveSpectrum][1] = 0;
	    						}
	    						else if (!bPositivePolarity && m_dStandardCompoundsMZArray[j][0] < 0)
	    						{
	    							mzData[j][iNegativeSpectrum][0] = dRetentionTime;
	    							mzData[j][iNegativeSpectrum][1] = 0;
	    						}
	    					}
	    					
	    					// Fill in mzData with the intensities at each mass
	        	        	for (int j = 0; j < thisSpectrum.length; j++) 
	        	        	{
	        	        		for (int i = 0; i < m_dStandardCompoundsMZArray.length; i++)
	        	        		{
	        	        			for (int massnum = 0; massnum < m_dStandardCompoundsMZArray[i].length; massnum++)
	        	        			{
		        	        			if (bPositivePolarity)
		        	        			{
		        	        				if (thisSpectrum[j][0] <= m_dStandardCompoundsMZArray[i][massnum] + 0.5 && thisSpectrum[j][0] >= m_dStandardCompoundsMZArray[i][massnum] - 0.5)
		        	        					mzData[i][iPositiveSpectrum][1] += thisSpectrum[j][1];
		        	        			}
		        	        			else
		        	        			{
		        	         				if (thisSpectrum[j][0] <= (-m_dStandardCompoundsMZArray[i][massnum]) + 0.5 && thisSpectrum[j][0] >= (-m_dStandardCompoundsMZArray[i][massnum]) - 0.5)
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
    				
    			}
    			// Parse CDF file
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
     		           
           	        	mzData = new double[m_dStandardCompoundsMZArray.length][][];
           	        	dRetentionTimes = new double[iSpectraCount];
           	        	
           	        	for(int i = 0; i < mzData.length; i++)
           	        	{
           	        		mzData[i] = new double[iSpectraCount][2];
           	        	}
           	        	
           	        	for (int iSpectrum = 0; iSpectrum < iSpectraCount; iSpectrum++)
           	        	{
            	    		progressDialog.jlblProgressCaption.setText("Loading spectrum " + ((Integer)iSpectrum).toString() + " of " + ((Integer)iSpectraCount).toString() + "...");
                        	progressDialog.jProgressBar.setValue((int)(((double)iSpectrum / (double)iSpectraCount) * 100));

                        	if (progressDialog.m_bCancel)
                        	{
                        		return null;
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
            	        		for (int i = 0; i < mzDataMassList.size(); i++)
            	        		{
            	        			if (massDataArray.get(j) <= mzDataMassList.get(i) + 0.5 && massDataArray.get(j) >= mzDataMassList.get(i) - 0.5)
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
    			}
    			else
    			{
        			JOptionPane.showMessageDialog(null, "The specified file type is not supported.", "File type not supported", JOptionPane.ERROR_MESSAGE); 
      	  			return null;
    			}
    			
	    		progressDialog.jlblProgressCaption.setText("Loading peak finder...");

    	        // Launch the PeakFinderDialog
    	        PeakFinderDialog peakFinderDialog = new PeakFinderDialog(owner, true, m_strStandardCompoundsNameArray, m_dStandardCompoundsMZArray, m_dIsocraticDataArray);
    	        peakFinderDialog.setLocationRelativeTo(null);
    	        peakFinderDialog.setMzData(mzData);
    	        mzData = null;
    	        peakFinderDialog.setFlowRate(m_dFlowRate);
    	        peakFinderDialog.setMixingVolume(m_dMixingVolume);
    	        peakFinderDialog.setNonMixingVolume(m_dNonMixingVolume);
    	        peakFinderDialog.setInstrumentDeadTime(m_dInstrumentDeadTime);
    	        peakFinderDialog.setColumnInnerDiameter(m_dColumnInnerDiameter);
    	        peakFinderDialog.setColumnLength(m_dColumnLength);
//    	        peakFinderDialog.setRetentionTimes(dRetentionTimes);
//    	        dRetentionTimes = null;
    	        peakFinderDialog.setGradientProgram(m_dGradientProgram);
    	        peakFinderDialog.setGradientProfile(m_InterpolatedGradientProfile);
    	        peakFinderDialog.setDeadTimeProfile(m_InterpolatedDeadTimeProfile);
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
	        	//peakFinderDialog.m_dRetentionTimes = null;
	        	//dRetentionTimes = null;
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
	
	public void setGradientProgram(double[][] dGradientProgram)
	{
		tmGradientProgram.setRowCount(0);
		for (int i = 0; i < dGradientProgram.length; i++)
		{
			Object[] newRow = new Object[]{dGradientProgram[i][0], dGradientProgram[i][1]};
			tmGradientProgram.addRow(newRow);
		}
		this.m_dGradientProgram = dGradientProgram;
		performValidations();
	}
	
	public void setGradientProfile(double[][] GradientArray)
	{
		this.m_InterpolatedGradientProfile = new LinearInterpolationFunction(GradientArray);
	}
	
	public void setDeadTimeProfile(double[][] DeadTimeArray)
	{
		this.m_InterpolatedDeadTimeProfile = new LinearInterpolationFunction(DeadTimeArray);
	}
	
    public String getFileName()
    {
    	return this.m_strFileName;
    }
    
    public void setFileName(String strFileName)
    {
    	this.jtxtMZFile.setText(strFileName);
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

    public void setIsocraticData (double[][][] dIsocraticDataArray)
    {
    	this.m_dIsocraticDataArray = dIsocraticDataArray;
    }
    
    public void setStandardCompoundNames (String[] strStandardCompoundNames)
    {
    	this.m_strStandardCompoundsNameArray = strStandardCompoundNames;
    }

    public void setStandardCompoundMZData (double[][] dStandardCompoundMZValues)
    {
    	this.m_dStandardCompoundsMZArray = dStandardCompoundMZValues;
    }
    
	public void setFlowRate(double dFlowRate)
	{
		jtxtFlowRate.setText(Float.toString((float)dFlowRate));
		performValidations();
	}
	
	public void setMixingVolume(double dMixingVolume)
	{
		jtxtMixingVolume.setText(Float.toString((float)dMixingVolume * 1000));
		performValidations();
	}
	
	public void setInstrumentDeadTime(double dInstrumentDeadTime)
	{
		m_dInstrumentDeadTime = dInstrumentDeadTime;
	}
	
	public void setNonMixingVolume(double dNonMixingVolume)
	{
		jtxtNonMixingVolume.setText(Float.toString((float)dNonMixingVolume * 1000));
		performValidations();
	}
	
	public void setColumnLength(double dColumnLength)
	{
		jtxtColumnLength.setText(Float.toString((float)dColumnLength));
		performValidations();
	}

	public void setColumnInnerDiameter(double dColumnInnerDiameter)
	{
		jtxtInnerDiameter.setText(Float.toString((float)dColumnInnerDiameter));
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
		this.setSize(660, 437);
		this.setResizable(false);
		this.setTitle("Load an LC-MS data file");
		this.setContentPane(getJContentPane());
		
		// Create the Preferences class
		prefs = Preferences.userNodeForPackage(this.getClass());
		
        jtxtColumnLength.addFocusListener(this);
        jtxtColumnLength.addKeyListener(this);
        jtxtInnerDiameter.addFocusListener(this);
        jtxtInnerDiameter.addKeyListener(this);
        jtxtFlowRate.addFocusListener(this);
        jtxtFlowRate.addKeyListener(this);
        jtxtMixingVolume.addFocusListener(this);
        jtxtMixingVolume.addKeyListener(this);
        jtxtNonMixingVolume.addFocusListener(this);
        jtxtNonMixingVolume.addKeyListener(this);
        jtxtMZFile.addFocusListener(this);
        jtxtMZFile.addKeyListener(this);
        jtxtMZFile.getDocument().addDocumentListener(this);
        jbtnInsertRow.addActionListener(this);
        jbtnRemoveRow.addActionListener(this);
        tmGradientProgram.addTableModelListener(this);
        jbtnLoadFile.addActionListener(this);
        jbtnCancel.addActionListener(this);
        jbtnOk.addActionListener(this);
        
        jbtnOk.setEnabled(false);
        
        if (!m_bEditable)
       	{
            jlblStationaryPhase.setEnabled(false);
       		jcboStationaryPhase.setEnabled(false);
            jlblColumnInnerDiameter.setEnabled(false);
            jlblInnerDiameterUnit.setEnabled(false);
            jtxtInnerDiameter.setEnabled(false);
            jlblColumnLength.setEnabled(false);
            jlblColumnLengthUnit.setEnabled(false);
            jtxtColumnLength.setEnabled(false);
            jLabel1.setEnabled(false);
            jlblFlowRateUnit.setEnabled(false);
            jtxtFlowRate.setEnabled(false);
            jlblGradientDelay.setEnabled(false);
            jlblMixingVolume.setEnabled(false);
            jlblMixingVolumeUnits.setEnabled(false);
            jtxtMixingVolume.setEnabled(false);
            jlblNonMixingVolume.setEnabled(false);
            jlblNonMixingVolumeUnits.setEnabled(false);
            jtxtNonMixingVolume.setEnabled(false);
            jlblGradientProgram.setEnabled(false);
            jtableGradientProgram.setEnabled(false);
            jbtnInsertRow.setEnabled(false);
            jbtnRemoveRow.setEnabled(false);
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
			jLabel1 = new JLabel();
			jLabel1.setText("Flow rate:");
			jLabel1.setBounds(new Rectangle(12, 172, 169, 16));
			jLabel = new JLabel();
			jLabel.setText("LC-MS data file:");
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
			jbtnOk.setBounds(new Rectangle(360, 368, 137, 34));
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
			jbtnCancel.setBounds(new Rectangle(508, 368, 137, 34));
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
			jlblGradientProgram = new JLabel();
			jlblGradientProgram.setBounds(new Rectangle(332, 72, 154, 16));
			jlblGradientProgram.setText("Gradient program:");
			jlblNonMixingVolumeUnits = new JLabel();
			jlblNonMixingVolumeUnits.setBounds(new Rectangle(260, 244, 49, 16));
			jlblNonMixingVolumeUnits.setText("µL");
			jlblMixingVolumeUnits = new JLabel();
			jlblMixingVolumeUnits.setBounds(new Rectangle(260, 220, 49, 16));
			jlblMixingVolumeUnits.setText("µL");
			jlblNonMixingVolume = new JLabel();
			jlblNonMixingVolume.setBounds(new Rectangle(32, 244, 149, 16));
			jlblNonMixingVolume.setText("Non-mixing volume:");
			jlblMixingVolume = new JLabel();
			jlblMixingVolume.setBounds(new Rectangle(32, 220, 149, 16));
			jlblMixingVolume.setText("Mixing volume:");
			jlblGradientDelay = new JLabel();
			jlblGradientDelay.setBounds(new Rectangle(12, 196, 169, 16));
			jlblGradientDelay.setText("*Gradient delay volume:");
			jLabel2 = new JLabel();
			jLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel2.setBounds(new Rectangle(8, 0, 605, 37));
			jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabel2.setText("<html><center>Enter your approximate HPLC conditions to help identify the correct peaks and smooth data properly. *Gradient delay parameters may be left blank (or with default values) if you do not know what they are.</center></html>");
			jlblFlowRateUnit = new JLabel();
			jlblFlowRateUnit.setBounds(new Rectangle(260, 172, 49, 16));
			jlblFlowRateUnit.setText("mL/min");
			jlblColumnLengthUnit = new JLabel();
			jlblColumnLengthUnit.setBounds(new Rectangle(260, 148, 49, 16));
			jlblColumnLengthUnit.setText("mm");
			jlblColumnLength = new JLabel();
			jlblColumnLength.setBounds(new Rectangle(12, 148, 169, 16));
			jlblColumnLength.setText("Column length:");
			jlblInnerDiameterUnit = new JLabel();
			jlblInnerDiameterUnit.setBounds(new Rectangle(260, 124, 49, 16));
			jlblInnerDiameterUnit.setText("mm");
			jlblColumnInnerDiameter = new JLabel();
			jlblColumnInnerDiameter.setBounds(new Rectangle(12, 124, 169, 16));
			jlblColumnInnerDiameter.setText("Inner diameter:");
			jlblStationaryPhase = new JLabel();
			jlblStationaryPhase.setBounds(new Rectangle(12, 72, 118, 16));
			jlblStationaryPhase.setText("Stationary phase:");
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(4, 80, 645, 281));
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "Enter approximate HPLC conditions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.add(jLabel1, null);
			jPanel.add(jlblStationaryPhase, null);
			jPanel.add(getJcboStationaryPhase(), null);
			jPanel.add(jlblColumnInnerDiameter, null);
			jPanel.add(getJtxtInnerDiameter(), null);
			jPanel.add(jlblInnerDiameterUnit, null);
			jPanel.add(jlblColumnLength, null);
			jPanel.add(getJtxtColumnLength(), null);
			jPanel.add(jlblColumnLengthUnit, null);
			jPanel.add(getJtxtFlowRate(), null);
			jPanel.add(jlblFlowRateUnit, null);
			jPanel.add(getJScrollPane(), null);
			jPanel.add(getJbtnInsertRow(), null);
			jPanel.add(getJbtnRemoveRow(), null);
			jPanel.add(jlblGradientDelay, null);
			jPanel.add(jlblMixingVolume, null);
			jPanel.add(jlblNonMixingVolume, null);
			jPanel.add(getJtxtMixingVolume(), null);
			jPanel.add(jlblMixingVolumeUnits, null);
			jPanel.add(getJtxtNonMixingVolume(), null);
			jPanel.add(jlblNonMixingVolumeUnits, null);
			jPanel.add(jlblGradientProgram, null);
			jPanel.add(getJPanel2(), null);
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
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Choose an LC-MS data file", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
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
	 * This method initializes jtxtInnerDiameter	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtInnerDiameter() {
		if (jtxtInnerDiameter == null) {
			jtxtInnerDiameter = new JTextField();
			jtxtInnerDiameter.setBounds(new Rectangle(184, 120, 73, 26));
			jtxtInnerDiameter.setHorizontalAlignment(JTextField.TRAILING);
			jtxtInnerDiameter.setText("2.1");
		}
		return jtxtInnerDiameter;
	}

	/**
	 * This method initializes jtxtColumnLength	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtColumnLength() {
		if (jtxtColumnLength == null) {
			jtxtColumnLength = new JTextField();
			jtxtColumnLength.setBounds(new Rectangle(184, 144, 73, 26));
			jtxtColumnLength.setHorizontalAlignment(JTextField.TRAILING);
			jtxtColumnLength.setText("100");
		}
		return jtxtColumnLength;
	}

	/**
	 * This method initializes jtxtFlowRate	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtFlowRate() {
		if (jtxtFlowRate == null) {
			jtxtFlowRate = new JTextField();
			jtxtFlowRate.setBounds(new Rectangle(184, 168, 73, 26));
			jtxtFlowRate.setHorizontalAlignment(JTextField.TRAILING);
			jtxtFlowRate.setText("0.4");
		}
		return jtxtFlowRate;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(332, 92, 301, 109));
			jScrollPane.setViewportView(getJtableGradientProgram());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jtableGradientProgram	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJtableGradientProgram() {
		if (jtableGradientProgram == null) 
		{
			Object[] columnNames = {"Time (min)", "% B"};
			Double[][] data = {{0.0, 5.0},{5.0, 95.0}};
	        
			tmGradientProgram = new SpecialTableModel2(data, columnNames);

			jtableGradientProgram = new JTable(tmGradientProgram);
			
			jtableGradientProgram.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			jtableGradientProgram.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jtableGradientProgram.getTableHeader().setPreferredSize(new Dimension(22, 22));
			jtableGradientProgram.getColumnModel().getColumn(0).setPreferredWidth(88);
			
			JTextField jtf = new JTextField();
			TableCellEditorCustom cellEditor = new TableCellEditorCustom(jtf);
			jtableGradientProgram.getColumnModel().getColumn(0).setCellEditor(cellEditor);
			jtableGradientProgram.getColumnModel().getColumn(1).setCellEditor(cellEditor);
		}
		return jtableGradientProgram;
	}

	/**
	 * This method initializes jbtnInsertRow	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnInsertRow() {
		if (jbtnInsertRow == null) {
			jbtnInsertRow = new JButton();
			jbtnInsertRow.setBounds(new Rectangle(332, 204, 137, 34));
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
			jbtnRemoveRow.setBounds(new Rectangle(496, 204, 137, 34));
			jbtnRemoveRow.setText("Remove Row");
			jbtnRemoveRow.setActionCommand("Remove Row");
		}
		return jbtnRemoveRow;
	}

	/**
	 * This method initializes jtxtMixingVolume	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtMixingVolume() {
		if (jtxtMixingVolume == null) {
			jtxtMixingVolume = new JTextField();
			jtxtMixingVolume.setBounds(new Rectangle(184, 216, 73, 26));
			jtxtMixingVolume.setHorizontalAlignment(JTextField.TRAILING);
			jtxtMixingVolume.setText("100");
		}
		return jtxtMixingVolume;
	}

	/**
	 * This method initializes jtxtNonMixingVolume	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtNonMixingVolume() {
		if (jtxtNonMixingVolume == null) {
			jtxtNonMixingVolume = new JTextField();
			jtxtNonMixingVolume.setBounds(new Rectangle(184, 240, 73, 26));
			jtxtNonMixingVolume.setHorizontalAlignment(JTextField.TRAILING);
			jtxtNonMixingVolume.setText("200");
		}
		return jtxtNonMixingVolume;
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

    private void validateColumnInnerDiameter()
    {
    	if (jtxtInnerDiameter.getText().length() == 0)
    		jtxtInnerDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(jtxtInnerDiameter.getText());
		
		if (dTemp < 0.01)
			dTemp = 0.01;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.m_dColumnInnerDiameter = dTemp;
		jtxtInnerDiameter.setText(Float.toString((float)m_dColumnInnerDiameter));    	
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
    
    private void validateMixingVolume()
    {
    	if (jtxtMixingVolume.getText().length() == 0)
    		jtxtMixingVolume.setText("0");

    	double dTemp = (double)Float.valueOf(jtxtMixingVolume.getText());
		
		if (dTemp < 0.001)
			dTemp = 0.001;
		if (dTemp > 100000)
			dTemp = 100000;
		
		this.m_dMixingVolume = dTemp / 1000;
		jtxtMixingVolume.setText(Float.toString((float)m_dMixingVolume * 1000));    	
    }
    
    private void validateNonMixingVolume()
    {
    	if (jtxtNonMixingVolume.getText().length() == 0)
    		jtxtNonMixingVolume.setText("0");

    	double dTemp = (double)Float.valueOf(jtxtNonMixingVolume.getText());
		
		if (dTemp < 0.001)
			dTemp = 0.001;
		if (dTemp > 100000)
			dTemp = 100000;
		
		this.m_dNonMixingVolume = dTemp / 1000;
		jtxtNonMixingVolume.setText(Float.toString((float)m_dNonMixingVolume * 1000));    	
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
		validateColumnLength();
		validateColumnInnerDiameter();
		validateFlowRate();	
		validateMixingVolume();
		validateNonMixingVolume();
		
    	this.m_dGradientProgram = new double[tmGradientProgram.getRowCount() + 2][2];
    	int iPointCount = 0;

    	this.m_dGradientProgram[iPointCount][0] = 0.0;
    	this.m_dGradientProgram[iPointCount][1] = (Double)tmGradientProgram.getValueAt(0, 1);
    	double dLastTime = 0;
		iPointCount++;
		
    	// Go through the gradient program table and create an array that contains solvent composition vs. time
		for (int i = 0; i < tmGradientProgram.getRowCount(); i++)
		{
    		if ((Double)tmGradientProgram.getValueAt(i, 0) > dLastTime)
    		{
    			double dTime = (Double)tmGradientProgram.getValueAt(i, 0);
    			double dFractionB = (Double)tmGradientProgram.getValueAt(i, 1);
    			
				this.m_dGradientProgram[iPointCount][0] = dTime;
				this.m_dGradientProgram[iPointCount][1] = dFractionB;
    	    	iPointCount++;
    		
    	    	dLastTime = dTime;
    		}
		}
		
		// Add another point past the end of the gradient to make it flatten out and go forever.
		this.m_dGradientProgram[iPointCount][0] = this.m_dGradientProgram[iPointCount - 1][0] * 2;
		this.m_dGradientProgram[iPointCount][1] = (Double)tmGradientProgram.getValueAt(tmGradientProgram.getRowCount() - 1, 1);
    	iPointCount++;

		// Ideal series finished
		// Now cut it down to the correct size
		double tempArray[][] = new double[iPointCount][2];
		for (int i = 0; i < iPointCount; i++)
		{
			tempArray[i][0] = this.m_dGradientProgram[i][0];
			tempArray[i][1] = this.m_dGradientProgram[i][1];
		}
		this.m_dGradientProgram = tempArray;
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
			// Set the default file filter to mzXML
			chooser.setFileFilter(filter1);
			
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
	    	int iSelectedRow = jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = tmGradientProgram.getRowCount()-1;
	    	
	    	Double dRowValue1 = (Double) tmGradientProgram.getValueAt(iSelectedRow, 0);
	    	Double dRowValue2 = (Double) tmGradientProgram.getValueAt(iSelectedRow, 1);
	    	Double dRowData[] = {dRowValue1, dRowValue2};
	    	tmGradientProgram.insertRow(iSelectedRow + 1, dRowData);
	    }
	    else if (e.getSource() == jbtnRemoveRow)
	    {
	    	int iSelectedRow = jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = tmGradientProgram.getRowCount()-1;
	    	
	    	if (tmGradientProgram.getRowCount() >= 3)
	    	{
	    		tmGradientProgram.removeRow(iSelectedRow);
	    	}
	    }
	    else if (e.getSource() == jbtnOk)
	    {
	    	// Check if the cell editor is currently open in the table
	    	if (jtableGradientProgram.isEditing())
	    	{
	    		jtableGradientProgram.getCellEditor().stopCellEditing();
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
		
		if(e.getSource() == tmGradientProgram)
		{
			int iChangedRow = e.getFirstRow();
			int iChangedColumn = e.getColumn();

			Double dRowValue1 = 0.0;
			Double dRowValue2 = 0.0;
			
			if (iChangedRow < tmGradientProgram.getRowCount())
			{
				dRowValue1 = (Double) tmGradientProgram.getValueAt(iChangedRow, 0);
				dRowValue2 = (Double) tmGradientProgram.getValueAt(iChangedRow, 1);
			}
			
	    	if (iChangedColumn == 0)
			{
				// If the column changed was the first, then make sure the time falls in the right range
				if (iChangedRow == 0)
				{
					// No changes allowed in first row - must be zero min
					dRowValue1 = 0.0;
				}
				else if (iChangedRow == tmGradientProgram.getRowCount() - 1)
				{
					Double dPreviousTime = (Double) tmGradientProgram.getValueAt(tmGradientProgram.getRowCount() - 2, 0);
					// If it's the last row, just make sure the time is greater than or equal to the time before it.
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
				}
				else
				{
					Double dPreviousTime = (Double) tmGradientProgram.getValueAt(iChangedRow - 1, 0);
					Double dNextTime = (Double) tmGradientProgram.getValueAt(iChangedRow + 1, 0);
					
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
					
					if (dRowValue1 > dNextTime)
						dRowValue1 = dNextTime;
				}
				
		    	m_bDoNotChangeTable = true;
		    	tmGradientProgram.setValueAt(dRowValue1, iChangedRow, iChangedColumn);
			}
			else if (iChangedColumn == 1)
			{
				// If the column changed was the second, then make sure the solvent composition falls between 0 and 100
				if (dRowValue2 > 100)
					dRowValue2 = 100.0;
				
				if (dRowValue2 < 0)
					dRowValue2 = 0.0;
				
		    	m_bDoNotChangeTable = true;
		    	tmGradientProgram.setValueAt(dRowValue2, iChangedRow, iChangedColumn);
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

}  //  @jve:decl-index=0:visual-constraint="10,10"
