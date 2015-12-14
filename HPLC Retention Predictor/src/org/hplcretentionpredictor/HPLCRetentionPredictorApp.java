package org.hplcretentionpredictor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.special.Erf;

import boswell.graphcontrol.AutoScaleEvent;
import boswell.graphcontrol.AutoScaleListener;
import boswell.peakfinderlc.PeakFinderSettingsDialog;

class PredictedRetentionObject
{
	double dPredictedRetentionTime = 0;
	double dPredictedErrorSigma = 0;
}

class Compound
{
	String strCompoundName;
	int iCompoundIndex;

	static public int getCompoundNum()
	{
		return GlobalsDan.StandardCompoundsNameArray.length;
	}
	
	public boolean loadCompoundInfo(int iIndex)
	{
		iCompoundIndex = iIndex;
		
		strCompoundName = GlobalsDan.StandardCompoundsNameArray[iIndex];
		
		return true;
	}
}

public class HPLCRetentionPredictorApp extends JFrame implements ActionListener, KeyListener, FocusListener, ListSelectionListener, AutoScaleListener, TableModelListener, ClipboardOwner
{
	private static final long serialVersionUID = 1L;

	TopPanel contentPane = null;
	TopPanel2 contentPane2 = null;
	public JScrollPane jMainScrollPane = null;
	public JPanel jBackPanel = null;
	public int m_iStationaryPhase = 0;
	public double m_dColumnLength = 100; // in mm
	public double m_dColumnInnerDiameter = 2.1; // in mm
	public double m_dProgramTime = 20;
	public double m_dFlowRate = 1; // in mL/min
	public int m_iNumPoints = 3000;
	public Vector<Object[]> m_vectCalCompounds = new Vector<Object[]>(); //{compound #, measured retention time, projected retention time};
    private Task task = null;
    private TaskPredict taskPredict;
    public int m_iStage = 1;
    public double m_dtstep = 0.01;
    public double m_dVariance = 0;
    public boolean m_bNoFullBackcalculation = false;
    
    public String m_strFileName = "";
    
    public double[][] m_dIdealGradientProfileArray;
    public LinearInterpolationFunction m_InterpolatedIdealGradientProfile; //Linear
    public double[][] m_dGradientProfileDifferenceArray;
    public LinearInterpolationFunction m_InterpolatedGradientDifferenceProfile;

    public double[][] m_dInitialDeadTimeArray;
    public InterpolationFunction m_InitialInterpolatedDeadTimeProfile;
    public double[][] m_dDeadTimeDifferenceArray;
    public InterpolationFunction m_InterpolatedDeadTimeDifferenceProfile;
    
    public InterpolationFunction[] m_StandardIsocraticDataInterpolated;

    // For the mixing/non-mixing volume
	public double[][] m_dSimpleGradientArray;
	public LinearInterpolationFunction m_InterpolatedSimpleGradient = null;
	public LinearInterpolationFunction m_InterpolatedSimpleDeadTime = null;

    public int m_iInterpolatedGradientProgramSeries = 0;
    public int m_iGradientProgramMarkerSeries = 0;
    public int m_iInterpolatedFlowRateSeries = 0;
    public int m_iFlowRateMarkerSeries = 0;
    public int m_iSimpleGradientSeries = 0;
    public int m_iSimpleDeadTimeSeries = 0;
    
    // Mixing volume in mL
    //public double m_dMixingVolume = 0.001;
    //public double m_dNonMixingVolume = 0.001;
    
    // Mixing volume as a function of solvent composition
    public double[][] m_dMixingVolumeArray = {{0.0, 0.001}, {100.0, 0.001}};
    // Non-mixing volume as a function of solvent composition
    public double[][] m_dNonMixingVolumeArray = {{0.0, 0.001}, {100.0, 0.001}};
    public LinearInterpolationFunction m_InterpolatedMixingVolume = null;
    public LinearInterpolationFunction m_InterpolatedNonMixingVolume = null;
    
    public double m_dPlotXMax = 0;
    public double m_dPlotXMax2 = 0;
    
    public boolean m_bDoNotChangeTable = false;
    public final double m_dGoldenRatio = (1 + Math.sqrt(5)) / 2;
    public boolean m_bShowSimpleGradient = false;
    public boolean m_bShowSimpleDeadTime = false;

    public double m_dInstrumentDeadTime = 0; // in min

    public double[] m_dExpectedErrorArray;
    public PredictedRetentionObject[] m_PredictedRetentionTimes;
    public double m_dConfidenceInterval = 0.99;

	public Vector<Compound> m_vectCompound = new Vector<Compound>();
	private ArrayList<IsocraticCompound> otherCompounds = new ArrayList<IsocraticCompound>(1000);
	public UpdateProgressDialog updateDialog = null;
	public TaskUpdateDatabase taskUpdateDb = null;

	private long totalTime;
	private String path = "";
	private String database_dir = "isocratic_database_files";
	private String local_db_summary = "";
	private String online_db_summary = "http://retentionprediction.org/hplc/database/crc_database.txt";
	private String online_db_dir = "http://retentionprediction.org/hplc/database/isocraticdatabase/";
	
    // Start the app
    public static void main(String[] args) 
    {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() 
    {
        //Use the Java look and feel.
    	try {
    	    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
    	        if ("Nimbus".equals(info.getName())) {
    	            UIManager.setLookAndFeel(info.getClassName());
    	            break;
    	        }
    	    }
    	} catch (Exception e) {
    	    // If Nimbus is not available, you can set the GUI to another look and feel.
    	}
    	/*try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { }*/

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        //Instantiate the controlling class.
        HPLCRetentionPredictorApp frame = new HPLCRetentionPredictorApp("HPLC Retention Predictor");
        
		//java.net.URL url1 = ClassLoader.getSystemResource("/images/icon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
	    Image img = kit.createImage(frame.getClass().getResource("/org/hplcretentionpredictor/images/icon.png"));
	    frame.setIconImage(img);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the HPLC Simulator window
        frame.init();

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null); //center it
        frame.setVisible(true);
    }
    
	/**
	 * This is the xxx default constructor
	 */
	public HPLCRetentionPredictorApp(String str) 
	{
	    super(str);
	    
		this.setPreferredSize(new Dimension(943, 615));
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() 
	{
        // Load the JavaHelp
		String helpHS = "org/hplcretentionpredictor/help/RetentionPredictorHelp.hs";
		ClassLoader cl = TopPanel.class.getClassLoader();
		
		String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		if(path.contains("/target/classes")){
			path = Pattern.compile("/target/classes", Pattern.LITERAL).matcher(path).replaceAll("");
		}
		
		if(path.charAt(path.length()-1) != '/' && path.charAt(path.length()-1) != '\\'){
			path += "/";
		}
		if(path.contains(".jar")){
			path = path + "../";
		}
		this.path = path;
		local_db_summary = path + "database.lcdsv";
		
		//Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
        	createGUI();
        } catch (Exception e) { 
            System.err.println("createGUI didn't complete successfully");
            System.err.println(e.getMessage());
            System.err.println(e.getLocalizedMessage());
            System.err.println(e.toString());
            System.err.println(e.getStackTrace());
            System.err.println(e.getCause());
        }
        
        performValidations();
        
    }
    
    private void createGUI()
    {
        //Create and set up the first content pane (steps 1-4).
    	jMainScrollPane = new JScrollPane();
    	jMainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	jMainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    	contentPane = new TopPanel();
        contentPane.setOpaque(true);
        jMainScrollPane.setViewportView(contentPane);
    	setContentPane(jMainScrollPane);
    	jMainScrollPane.revalidate();

        contentPane.jtxtColumnLength.addFocusListener(this);
        contentPane.jtxtColumnLength.addKeyListener(this);
        contentPane.jtxtInnerDiameter.addFocusListener(this);
        contentPane.jtxtInnerDiameter.addKeyListener(this);
        contentPane.jtxtFlowRate.addFocusListener(this);
        contentPane.jtxtFlowRate.addKeyListener(this);
        contentPane.jbtnHelp.addActionListener(this);
        contentPane.jbtnInsertRow.addActionListener(this);
        contentPane.jbtnRemoveRow.addActionListener(this);
        contentPane.jtxtIDT.addFocusListener(this);
        contentPane.jtxtIDT.addKeyListener(this);
        contentPane.jcboIDTUnits.addActionListener(this);
       
        contentPane.m_GraphControlGradient.addAutoScaleListener(this);
        contentPane.m_GraphControlFlowRate.addAutoScaleListener(this);
        contentPane.tmGradientProgram.addTableModelListener(this);
        contentPane.tmMeasuredRetentionTimes.addTableModelListener(this);
        contentPane.jbtnNextStep.addActionListener(this);
        contentPane.jbtnPreloadedValues.addActionListener(this);
        contentPane.jbtnPeakFinder.addActionListener(this);
        
        contentPane.m_GraphControlGradient.setYAxisTitle("Eluent Composition");
        contentPane.m_GraphControlGradient.setYAxisBaseUnit("%B", "");
        contentPane.m_GraphControlGradient.setYAxisRangeLimits(0, 105);
        contentPane.m_GraphControlGradient.setYAxisRangeIndicatorsVisible(false);
        contentPane.m_GraphControlGradient.setXAxisRangeIndicatorsVisible(false);
        contentPane.m_GraphControlGradient.setAutoScaleY(true);
        contentPane.m_GraphControlGradient.repaint();

        contentPane.m_GraphControlFlowRate.setYAxisTitle("Uracil Dead Time");
        contentPane.m_GraphControlFlowRate.setYAxisBaseUnit("seconds", "s");
        contentPane.m_GraphControlFlowRate.setYAxisRangeLimits(0, 10000);
        contentPane.m_GraphControlFlowRate.setYAxisRangeIndicatorsVisible(false);
        contentPane.m_GraphControlFlowRate.setXAxisRangeIndicatorsVisible(false);
        contentPane.m_GraphControlFlowRate.setAutoScaleY(true);
        contentPane.m_GraphControlFlowRate.setXAxisType(false);
        contentPane.m_GraphControlFlowRate.setXAxisTitle("Eluent Composition");
        contentPane.m_GraphControlFlowRate.setXAxisBaseUnit("%B", "");
        contentPane.m_GraphControlFlowRate.setXAxisRangeLimits(0, 105);
        contentPane.m_GraphControlFlowRate.repaint();
        
        //Create and set up the second content pane
        contentPane2 = new TopPanel2();
        contentPane2.setOpaque(true); 
        contentPane2.jpanelStep6.setVisible(false);
        contentPane2.jpanelStep5.setVisible(false);
        
        contentPane2.jbtnAutomaticDetermineTestCompounds.addActionListener(this);
        contentPane2.m_tmTestCompoundsModel.addTableModelListener(this);

        contentPane2.jbtnNextStep.addActionListener(this);
        contentPane2.jbtnCalculate.addActionListener(this);
        contentPane2.jbtnPreviousStep.addActionListener(this);
        contentPane2.jbtnPredict.addActionListener(this);
        contentPane2.jbtnUpdateIsocraticDatabase.addActionListener(this);
        contentPane2.jbtnImportData.addActionListener(this);
        contentPane2.jbtnHelp.addActionListener(this);
        contentPane2.jtxtWindowConfidence.addFocusListener(this);
        contentPane2.jtxtWindowConfidence.addKeyListener(this);

        contentPane2.m_GraphControlGradient.setYAxisTitle("Eluent Composition");
        contentPane2.m_GraphControlGradient.setYAxisBaseUnit("%B", "");
        contentPane2.m_GraphControlGradient.setYAxisRangeLimits(0, 105);
        contentPane2.m_GraphControlGradient.setYAxisRangeIndicatorsVisible(false);
        contentPane2.m_GraphControlGradient.setXAxisRangeIndicatorsVisible(false);
        contentPane2.m_GraphControlGradient.setAutoScaleY(true);
        contentPane2.m_GraphControlGradient.repaint();

        contentPane2.m_GraphControlFlowRate.setYAxisTitle("Uracil Dead Time");
        contentPane2.m_GraphControlFlowRate.setYAxisBaseUnit("seconds", "s");
        contentPane2.m_GraphControlFlowRate.setYAxisRangeLimits(0, 10000);
        contentPane2.m_GraphControlFlowRate.setYAxisRangeIndicatorsVisible(true);
        contentPane2.m_GraphControlFlowRate.setAutoScaleY(true);
        contentPane2.m_GraphControlFlowRate.setXAxisType(false);
        contentPane2.m_GraphControlFlowRate.setXAxisTitle("Eluent Composition");
        contentPane2.m_GraphControlFlowRate.setXAxisBaseUnit("%B", "");
        contentPane2.m_GraphControlFlowRate.setXAxisRangeLimits(0, 105);
        contentPane2.m_GraphControlFlowRate.setXAxisRangeIndicatorsVisible(false);
        contentPane2.m_GraphControlFlowRate.repaint();       
    }

    private void validateColumnLength()
    {
    	if (contentPane.jtxtColumnLength.getText().length() == 0)
    		contentPane.jtxtColumnLength.setText("0");
    	
		double dTemp = (double)Float.valueOf(contentPane.jtxtColumnLength.getText());
		
		if (dTemp < 0.1)
			dTemp = 0.1;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.m_dColumnLength = dTemp;
		contentPane.jtxtColumnLength.setText(Float.toString((float)m_dColumnLength));    	
    }

    private void validateColumnInnerDiameter()
    {
    	if (contentPane.jtxtInnerDiameter.getText().length() == 0)
    		contentPane.jtxtInnerDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jtxtInnerDiameter.getText());
		
		if (dTemp < 0.01)
			dTemp = 0.01;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.m_dColumnInnerDiameter = dTemp;
		contentPane.jtxtInnerDiameter.setText(Float.toString((float)m_dColumnInnerDiameter));    	
    }
    
    private void validateFlowRate()
    {
    	if (contentPane.jtxtFlowRate.getText().length() == 0)
    		contentPane.jtxtFlowRate.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jtxtFlowRate.getText());
		
		if (dTemp < 0.000000001)
			dTemp = 0.000000001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		this.m_dFlowRate = dTemp;
		contentPane.jtxtFlowRate.setText(Float.toString((float)m_dFlowRate));
		
		// If the flow rate changes and we're selecting instrument dead time by volume, we need to re-set it here
		validateInstrumentDeadTime();
    }
    
    private void validateInstrumentDeadTime()
    {
    	if (contentPane.jtxtIDT.getText().length() == 0)
    		contentPane.jtxtIDT.setText(Float.toString(0));

    	double dTemp = (double)Float.valueOf(contentPane.jtxtIDT.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 10000)
			dTemp = 10000;
		
		if (contentPane.jcboIDTUnits.getSelectedIndex() == 0) // min selected
		{
			this.m_dInstrumentDeadTime = dTemp;
		}
		else // mL selected
		{
			this.m_dInstrumentDeadTime = dTemp / this.m_dFlowRate;
		}
		contentPane.jtxtIDT.setText(Float.toString((float)dTemp));
    }
    
    private void validateConfidenceWindow()
    {
		double dTemp = (double)Float.valueOf(contentPane2.jtxtWindowConfidence.getText());
		
		if (dTemp < 0.01)
			dTemp = 0.01;
		if (dTemp > 99.999)
			dTemp = 99.999;
		
		if (this.m_dConfidenceInterval != dTemp / 100)
		{
			this.m_dConfidenceInterval = dTemp / 100;
			contentPane2.jtxtWindowConfidence.setText(Float.toString((float)dTemp));
			this.updatePredictionsTable();
		}
    }
    
	class CompoundSorter implements Comparator 
	{
		int colIndex;
	
		CompoundSorter(int colIndex) 
		{
			this.colIndex = colIndex;
		}
	
		public int compare(Object a, Object b) 
		{
		    Object o1 = ((Object[])a)[colIndex];
		    Object o2 = ((Object[])b)[colIndex];
	
		    if (o1 instanceof String && ((String) o1).length() == 0) {
		      o1 = null;
		    }
		    if (o2 instanceof String && ((String) o2).length() == 0) {
		      o2 = null;
		    }
	
		    if (o1 == null && o2 == null) {
		    	return 0;
		    } else if (o1 == null) {
		    	return 1;
		    } else if (o2 == null) {
		    	return -1;
		    } else if (o1 instanceof Comparable) {
	
		    	return ((Comparable) o1).compareTo(o2);
		    } else {
	
		    	return o1.toString().compareTo(o2.toString());
		    }
		}
	}

    public void nextStepButtonPressed()
    {
    	performValidations();
    	
    	if (this.m_dInstrumentDeadTime == 0)
    	{
    		String message = "<html><body><p style='width: 300px;'>Please enter your measured instrument dead time in " +
    						"the &quot;Instrument dead time&quot; box. Not entering " +
    						"an instrument dead time can cause significant error in your " +
    						"retention predictions unless your instrument dead time is " +
    						"exceedingly small.<br/>" +
    						"<br/>" +
    						"Do you wish to continue anyway?</p></body></html>";
    		
    		int retVal = JOptionPane.showConfirmDialog(null, message, "Enter Instrument Dead Time", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			
			if (retVal == JOptionPane.NO_OPTION)
				return;
    	}
    	
    	// Set the graph and flow profiles to contain the correct data
    	m_iStage = 2;
    	
		NumberFormat formatter = new DecimalFormat("#0.0000");

    	// Set initial control values
    	contentPane2.jbtnCalculate.setText("Back-Calculate Profiles");
    	contentPane2.jbtnCalculate.setEnabled(true);
		contentPane2.jbtnCalculate.setActionCommand("Calculate");
    	contentPane2.jlblIterationNumber.setText("1");
    	contentPane2.jlblLastVariance.setText("");
    	contentPane2.jlblPercentImprovement.setText("");
    	contentPane2.jlblPhase.setText("I");
    	contentPane2.jlblTimeElapsed.setText("");
    	contentPane2.jlblVariance.setText("");
    	contentPane2.jProgressBar.setString("");
    	contentPane2.jProgressBar.setIndeterminate(false);
    	contentPane2.m_GraphControlGradient.RemoveAllSeries();
    	contentPane2.m_GraphControlFlowRate.RemoveAllSeries();
        contentPane2.jpanelStep6.setVisible(false);
        contentPane2.jpanelStep5.setVisible(false);
        contentPane2.jpanelStep4.setVisible(true);
        contentPane2.jlblTotalGradientDelayVolume.setText("");
        contentPane2.jlblMixingVolume.setText("");
        contentPane2.jlblNonMixingVolume.setText("");
    	contentPane2.jbtnNextStep.setEnabled(false);
		
        m_bShowSimpleGradient = false;
        m_bShowSimpleDeadTime = false;

        // Set the initial values of the mixing and nonmixing volumes
        this.m_dMixingVolumeArray[0][1] = 0.000001;
        this.m_dMixingVolumeArray[1][1] = 0.000001;
        this.m_dNonMixingVolumeArray[0][1] = 0.000001;
        this.m_dNonMixingVolumeArray[1][1] = 0.000001;
        this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
        this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);

        //this.m_dMixingVolume = 0.000001;
        //this.m_dNonMixingVolume = 0.000001;
        
    	// Set the table to contain the correct data
    	contentPane2.tmOutputModel.getDataVector().clear();
    	m_vectCalCompounds.clear();
    	
    	for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
    	{
    		if ((Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3) <= (Double)0.0
    				|| (Boolean)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 0) == false)
    			continue;
    		
    		Object strCompoundName = contentPane.tmMeasuredRetentionTimes.getValueAt(i, 1);
    		Double dMeasuredRetentionTime = (Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3);
    		
    		// Subtract the instrument dead time
    		dMeasuredRetentionTime -= this.m_dInstrumentDeadTime;
    		
    		if (dMeasuredRetentionTime < 0)
    			dMeasuredRetentionTime = (Double)0.0;
    		
    		Object[] newRow = {strCompoundName, formatter.format(dMeasuredRetentionTime + this.m_dInstrumentDeadTime), null, null};

    		contentPane2.tmOutputModel.addRow(newRow);
    		
			Object[] newSolute = {i, dMeasuredRetentionTime, (Double)0.0};
			m_vectCalCompounds.add(newSolute);
    	}
    	
    	// Sort the calibration compounds by their retention times
        Collections.sort(m_vectCalCompounds, new CompoundSorter(1));
    	
        // Find greatest dead volume
		double dVmMax = 0;
		
		for (int i = 0; i < this.m_dInitialDeadTimeArray.length; i++)
		{
			if (this.m_dInitialDeadTimeArray[i][1] > dVmMax)
			{
				dVmMax = this.m_dInitialDeadTimeArray[i][1];
			}
		}	  
		
		dVmMax *= this.m_dFlowRate / 60;
		
		m_dPlotXMax2 = (((Double)m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1]));
    	
    	// Here is where we set the value of m_dtstep
    	m_dtstep = m_dPlotXMax2 * 0.001;

    	int iIdealPlotIndexGradient = contentPane2.m_GraphControlGradient.AddSeries("Ideal Gradient Program", new Color(0, 0, 0), 1, false, false);
    	int iIdealPlotIndexDeadTime = contentPane2.m_GraphControlFlowRate.AddSeries("Ideal Dead Time", new Color(0, 0, 0), 1, false, false);
    	
    	m_iInterpolatedGradientProgramSeries = contentPane2.m_GraphControlGradient.AddSeries("Interpolated Gradient Program", new Color(255,0,0), 1, false, false);
	    m_iGradientProgramMarkerSeries = contentPane2.m_GraphControlGradient.AddSeries("Gradient Program Markers", new Color(255,0,0), 1, true, false);

	    m_iSimpleGradientSeries = contentPane2.m_GraphControlGradient.AddSeries("Simple Gradient", new Color(0,0,255), 1, false, false);
    	
    	m_iInterpolatedFlowRateSeries = contentPane2.m_GraphControlFlowRate.AddSeries("Interpolated Flow Rate", new Color(255,0,0), 1, false, false);
	    m_iFlowRateMarkerSeries = contentPane2.m_GraphControlFlowRate.AddSeries("Flow Rate Markers", new Color(255,0,0), 1, true, false);

	    m_iSimpleDeadTimeSeries = contentPane2.m_GraphControlFlowRate.AddSeries("Simple Dead Time", new Color(0,0,255), 1, false, false);

	    // Add in data points for the ideal gradient program series
	    
    	this.m_dIdealGradientProfileArray = new double[contentPane.tmGradientProgram.getRowCount() + 2][2];
    	int iPointCount = 0;

    	contentPane2.m_GraphControlGradient.AddDataPoint(iIdealPlotIndexGradient, 0, (Double)contentPane.tmGradientProgram.getValueAt(0, 1));	
    	this.m_dIdealGradientProfileArray[iPointCount][0] = 0.0;
    	this.m_dIdealGradientProfileArray[iPointCount][1] = (Double)contentPane.tmGradientProgram.getValueAt(0, 1);
    	double dLastTime = 0;
		iPointCount++;
		
    	// Go through the gradient program table and create an array that contains solvent composition vs. time
		for (int i = 0; i < contentPane.tmGradientProgram.getRowCount(); i++)
		{
    		if ((Double)contentPane.tmGradientProgram.getValueAt(i, 0) > dLastTime)
    		{
    			double dTime = (Double)contentPane.tmGradientProgram.getValueAt(i, 0);
    			double dFractionB = (Double)contentPane.tmGradientProgram.getValueAt(i, 1);
    			
    	    	contentPane2.m_GraphControlGradient.AddDataPoint(iIdealPlotIndexGradient, dTime * 60, dFractionB);	
				this.m_dIdealGradientProfileArray[iPointCount][0] = dTime;
				this.m_dIdealGradientProfileArray[iPointCount][1] = dFractionB;
    	    	iPointCount++;
    		
    	    	dLastTime = dTime;
    		}
		}
		
		contentPane2.m_GraphControlGradient.AddDataPoint(iIdealPlotIndexGradient, m_dPlotXMax2 * 60, (Double)contentPane.tmGradientProgram.getValueAt(contentPane.tmGradientProgram.getRowCount() - 1, 1));
		this.m_dIdealGradientProfileArray[iPointCount][0] = m_dPlotXMax2 * 60;
		this.m_dIdealGradientProfileArray[iPointCount][1] = (Double)contentPane.tmGradientProgram.getValueAt(contentPane.tmGradientProgram.getRowCount() - 1, 1);
    	iPointCount++;

		// Ideal series finished
		// Now cut it down to the correct size
		double tempArray[][] = new double[iPointCount][2];
		for (int i = 0; i < iPointCount; i++)
		{
			tempArray[i][0] = this.m_dIdealGradientProfileArray[i][0];
			tempArray[i][1] = this.m_dIdealGradientProfileArray[i][1];
		}
		this.m_dIdealGradientProfileArray = tempArray;
		
		// Make the interpolated gradient profile
    	this.m_InterpolatedIdealGradientProfile = new LinearInterpolationFunction(this.m_dIdealGradientProfileArray);

    	// Add the initial interpolated hold-up time profile to the graph control
	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	contentPane2.m_GraphControlFlowRate.AddDataPoint(iIdealPlotIndexDeadTime, dXPos * 100, this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos));
	    }
	    
		// Create initial gradient and flow rate difference arrays
		m_dGradientProfileDifferenceArray = new double[2][2];
		m_dGradientProfileDifferenceArray[0][0] = 0;
		m_dGradientProfileDifferenceArray[0][1] = 0;
		m_dGradientProfileDifferenceArray[1][0] = m_dPlotXMax2;
		m_dGradientProfileDifferenceArray[1][1] = 0;
		
		m_dDeadTimeDifferenceArray = new double [2][2];
		m_dDeadTimeDifferenceArray[0][0] = .05;
		m_dDeadTimeDifferenceArray[0][1] = 0;
		m_dDeadTimeDifferenceArray[1][0] = .95;
		m_dDeadTimeDifferenceArray[1][1] = 0;

		m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
		m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);

		//setContentPane(contentPane2);
		this.jMainScrollPane.setViewportView(contentPane2);
		jMainScrollPane.setSize(jMainScrollPane.getSize());

		// Calculate the gradients (even though we don't show them)
		calculateSimpleDeadTime();
		calculateSimpleGradient();
		
		this.updateGraphs(false);
    }
    
    // Select where to place the handles in the m_dGradientProfileDifferenceArray and the m_dDeadTimeDifferenceArray
    public void placeHandles()
    {
        // Find greatest dead volume
		double dVmMax = 0;
		
		for (int i = 0; i < this.m_dInitialDeadTimeArray.length; i++)
		{
			if (this.m_dInitialDeadTimeArray[i][1] + m_InterpolatedDeadTimeDifferenceProfile.getAt(this.m_dInitialDeadTimeArray[i][0])> dVmMax)
			{
				dVmMax = this.m_dInitialDeadTimeArray[i][1] + m_InterpolatedDeadTimeDifferenceProfile.getAt(this.m_dInitialDeadTimeArray[i][0]);
			}
		}	  
		
		dVmMax *= this.m_dFlowRate / 60;
		
    	// Select number of data points for the gradient and flow profiles
		int iTotalDataPoints = m_vectCalCompounds.size();
		
		// 11/15ths of the data points should be on the gradient profile
		int iNumGradientProgramDataPoints = (int)(((double)11 / (double)15)*(double)iTotalDataPoints);
		int iNumFlowDataPoints = iTotalDataPoints - iNumGradientProgramDataPoints;
		
		if (iNumFlowDataPoints < 3)
		{
			iNumFlowDataPoints = 3;
			iNumGradientProgramDataPoints = iTotalDataPoints - iNumFlowDataPoints;
		}
		
		// Create initial gradient and flow rate arrays
		
		// First make an array with the correct number of data points.
		m_dGradientProfileDifferenceArray = new double [iNumGradientProgramDataPoints][2];
		
		m_dDeadTimeDifferenceArray = new double [iNumFlowDataPoints][2];
		
		// Set the value of the first data point
		m_dGradientProfileDifferenceArray[0][0] = 0;
		m_dGradientProfileDifferenceArray[0][1] = 0;
		
		for (int i = 1; i < iNumGradientProgramDataPoints - 1; i++)
		{
			// Find the two nearest standards
			double dStandardNum = ((double)i / ((double)iNumGradientProgramDataPoints - 1)) * (double)iTotalDataPoints;
			double dOneGreater = (int)Math.ceil(dStandardNum);
			double dOneLesser = (int)Math.floor(dStandardNum);
			double dRtOneLesser = (Double)m_vectCalCompounds.get((int)dOneLesser)[1];
			double dRtOneGreater = (Double)m_vectCalCompounds.get((int)dOneGreater)[1];
		
			// Check to see if we landed exactly on an alkane
			if (dOneGreater == dOneLesser)
			{
				// If so, set the time of this point to the alkane we landed on
				m_dGradientProfileDifferenceArray[i][0] = dRtOneLesser;
			}
			else
			{
				// Otherwise, find the time of this point in between the surrounding alkanes
				double dPosition = ((dStandardNum - dOneLesser) / (dOneGreater - dOneLesser));
				m_dGradientProfileDifferenceArray[i][0] = (dPosition * dRtOneGreater) + ((1 - dPosition) * dRtOneLesser);
			}
			
			// Subtract half a hold-up time
			m_dGradientProfileDifferenceArray[i][0] = m_dGradientProfileDifferenceArray[i][0] - (0.5 * (dVmMax / this.m_dFlowRate));
			m_dGradientProfileDifferenceArray[i][1] = 0;
		}
		
		// Add the last data point at the position of the last-eluting compound - half dead time
		m_dGradientProfileDifferenceArray[iNumGradientProgramDataPoints - 1][0] = (Double)m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] - (0.5 * (dVmMax / this.m_dFlowRate));
		m_dGradientProfileDifferenceArray[iNumGradientProgramDataPoints - 1][1] = 0;
		
		// Create another copy of the ideal gradient profile array, delayed by the gradient delay
		double[][] dDelayedIdealGradientProfileArray = new double[m_dIdealGradientProfileArray.length + 1][2];
		dDelayedIdealGradientProfileArray[0][0] = 0;
		dDelayedIdealGradientProfileArray[0][1] = m_dIdealGradientProfileArray[0][1];
		for (int i = 1; i < dDelayedIdealGradientProfileArray.length; i++)
		{
			//dDelayedIdealGradientProfileArray[i][0] = m_dIdealGradientProfileArray[i - 1][0] + ((this.m_dMixingVolume + this.m_dNonMixingVolume) / this.m_dFlowRate);
			dDelayedIdealGradientProfileArray[i][0] = m_dIdealGradientProfileArray[i - 1][0] + ((this.m_InterpolatedMixingVolume.getAt(m_dIdealGradientProfileArray[i - 1][1]) + this.m_InterpolatedNonMixingVolume.getAt(m_dIdealGradientProfileArray[i - 1][1])) / this.m_dFlowRate);
			dDelayedIdealGradientProfileArray[i][1] = m_dIdealGradientProfileArray[i - 1][1];
		}
		
		// Now adjust to get points at the corners
		int iPointIndex = 1;
		// Run through each corner in the ideal gradient profile array
		for (int i = 1; i < dDelayedIdealGradientProfileArray.length - 1; i++)
		{
			double dFirst = 0;
			double dNext = 0;
			
			// Find the first point after the corner (dNext) and the first point before the corner (dFirst)
			while (dNext < dDelayedIdealGradientProfileArray[i][0] && iPointIndex < m_dGradientProfileDifferenceArray.length - 1)
			{
				dFirst = m_dGradientProfileDifferenceArray[iPointIndex][0];
				dNext = m_dGradientProfileDifferenceArray[iPointIndex + 1][0];
				
				iPointIndex++;
			}
			
			// Remove the last increment
			iPointIndex--;
			
			// Find the distances between the corner and the two points
			double dDistFirst = dDelayedIdealGradientProfileArray[i][0] - dFirst;
			double dDistNext = dNext - dDelayedIdealGradientProfileArray[i][0];
			
			// Find the distances between the two points and their next further point
			double dDistFirstBefore = m_dGradientProfileDifferenceArray[iPointIndex][0] - m_dGradientProfileDifferenceArray[iPointIndex - 1][0];
			double dDistNextAfter;
			if (iPointIndex + 2 < m_dGradientProfileDifferenceArray.length)
				dDistNextAfter = m_dGradientProfileDifferenceArray[iPointIndex + 2][0] - m_dGradientProfileDifferenceArray[iPointIndex + 1][0];
			else
				dDistNextAfter = 0;
			
			double dScoreFirst = dDistFirst + dDistFirstBefore;
			double dScoreNext = dDistNext + dDistNextAfter;
			
			// Point with lower score moves
			if (dScoreFirst < dScoreNext)
			{
				// Move the first point
				m_dGradientProfileDifferenceArray[iPointIndex][0] = dDelayedIdealGradientProfileArray[i][0];
				m_dGradientProfileDifferenceArray[iPointIndex][1] = 0;

				// Move the one before it right in between it and the last point
				if (iPointIndex >= 2)
				{
					m_dGradientProfileDifferenceArray[iPointIndex - 1][0] = (m_dGradientProfileDifferenceArray[iPointIndex - 2][0] + m_dGradientProfileDifferenceArray[iPointIndex][0]) / 2;
					m_dGradientProfileDifferenceArray[iPointIndex - 1][1] = 0;
				}
				
				// Move the one after it in between it and the next point
				if (iPointIndex <= m_dGradientProfileDifferenceArray.length - 3)
				{
					m_dGradientProfileDifferenceArray[iPointIndex + 1][0] = (m_dGradientProfileDifferenceArray[iPointIndex][0] + m_dGradientProfileDifferenceArray[iPointIndex + 2][0]) / 2;
					m_dGradientProfileDifferenceArray[iPointIndex + 1][1] = 0;
				}
				
			}
			else
			{
				// Move the next point
				m_dGradientProfileDifferenceArray[iPointIndex + 1][0] = dDelayedIdealGradientProfileArray[i][0];
				m_dGradientProfileDifferenceArray[iPointIndex + 1][1] = 0;

				// Move the one before it right in between it and the last point
				if (iPointIndex >= 1)
				{
					m_dGradientProfileDifferenceArray[iPointIndex][0] = (m_dGradientProfileDifferenceArray[iPointIndex - 1][0] + m_dGradientProfileDifferenceArray[iPointIndex + 1][0]) / 2;
					m_dGradientProfileDifferenceArray[iPointIndex][1] = 0;
				}
				
				// Move the one after it in between it and the next point
				if (iPointIndex <= m_dGradientProfileDifferenceArray.length - 4)
				{
					m_dGradientProfileDifferenceArray[iPointIndex + 2][0] = (m_dGradientProfileDifferenceArray[iPointIndex + 1][0] + m_dGradientProfileDifferenceArray[iPointIndex + 3][0]) / 2;
					m_dGradientProfileDifferenceArray[iPointIndex + 2][1] = 0;
				}
			}
		}

    	// Now for the flow rate vs. time profile:
		
/*    	// Add the initial interpolated hold-up time profile to the graph control
	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	contentPane2.m_GraphControlFlowRate.AddDataPoint(iIdealPlotIndexDeadTime, dXPos * 100, this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos));
	    }
*/
	    for (int i = 0; i < iNumFlowDataPoints; i++)
		{
	    	m_dDeadTimeDifferenceArray[i][0] = (0.90 * ((double)i/((double)iNumFlowDataPoints - 1)) + 0.05);
	    	m_dDeadTimeDifferenceArray[i][1] = m_InterpolatedDeadTimeDifferenceProfile.getAt(m_dDeadTimeDifferenceArray[i][0]);
		}
		
		m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
		this.m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
		
		/*//setContentPane(contentPane2);
		this.jMainScrollPane.setViewportView(contentPane2);
		
		// Calculate the gradients (even though we don't show them)
		calculateSimpleDeadTime();
		calculateSimpleGradient();
		
		this.updateGraphs(false);*/
    }
    
    public void actionPerformed(ActionEvent evt) 
	{
	    String strActionCommand = evt.getActionCommand();

	    if (strActionCommand == "Help")
	    {
			//GlobalsDan.hbMainHelpBroker.setCurrentID("step-by-step");
			//GlobalsDan.hbMainHelpBroker.setDisplayed(true);
	    }
	    else if (strActionCommand == "Next Step")
	    {
	    	nextStepButtonPressed();
	    }
	    else if (strActionCommand == "Previous Step")
	    {
			if (m_iStage == 2)
			{
				this.jMainScrollPane.setViewportView(contentPane);

				if (task != null)
				{
					task.cancel(true);	
				}
				m_iStage--;
				
		    	contentPane2.jbtnNextStep.setVisible(true);
			}
			else if (m_iStage == 3)
			{
				contentPane2.jpanelStep6.setVisible(false);
				contentPane2.jpanelStep5.setVisible(false);
				contentPane2.jpanelStep4.setVisible(true);
		    	contentPane2.jbtnNextStep.setVisible(true);
				m_iStage--;
			}
			else if (m_iStage == 4)
			{
				if (taskPredict != null)
				{
					taskPredict.cancel(true);
				}
				
				contentPane2.jbtnPredict.setText("Predict Retention Times");
	    		contentPane2.jbtnPredict.setActionCommand("Predict");
	    		contentPane2.jbtnPredict.setEnabled(true);
	    		contentPane2.jProgressBar2.setValue(0);
	    		
				contentPane2.jpanelStep6.setVisible(false);
				contentPane2.jpanelStep5.setVisible(true);
				contentPane2.jpanelStep4.setVisible(false);
		    	contentPane2.jbtnNextStep.setVisible(true);
				m_iStage--;
			}
	    }
	    else if (strActionCommand == "Calculate")
	    {
	    	beginBackCalculation(false);
	    }
	    else if (strActionCommand == "Copy to clipboard")
	    {
	    	copyProfilesToClipboard();
	    }
	    else if (strActionCommand == "Stop Calculations")
	    {
	    	task.cancel(true);
	    }
	    else if (strActionCommand == "Next Step2")
	    {
	    	if (m_iStage == 2)
	    	{
	    		m_dExpectedErrorArray = new double[GlobalsDan.TestCompoundNameArray.length];
	    		
	    		// Fill in the table with predicted retention times
		    	for (int i = 0; i < GlobalsDan.TestCompoundNameArray.length; i++)
		    	{
		    		PredictedRetentionObject predictedRetention = predictRetention(GlobalsDan.TestCompoundsIsocraticDataArray[i]);
		    		m_dExpectedErrorArray[i] = predictedRetention.dPredictedErrorSigma;
		    		contentPane2.m_tmTestCompoundsModel.setValueAt(GlobalsDan.roundToSignificantFigures(predictedRetention.dPredictedRetentionTime, 5), i, 2);
		    	}
		    	
		    	this.updateTestCompoundTable();
	    		
		    	contentPane2.jpanelStep4.setVisible(false);
		    	contentPane2.jpanelStep5.setVisible(true);
		    	contentPane2.jpanelStep6.setVisible(false);
	    		contentPane2.jbtnNextStep.setVisible(true);
	    	}
	    	else if (m_iStage == 3)
	    	{
	    		checkForLocalUpdatesAndStartUpdate();
	    	}

	    	m_iStage++;
	    }
	    else if (strActionCommand == "Predict")
	    {
	    	contentPane2.jbtnPredict.setEnabled(false);
			contentPane2.jProgressBar2.setIndeterminate(true);
			contentPane2.jProgressBar2.setStringPainted(true);
			contentPane2.jProgressBar2.setString("Please wait, calculating retention...");
			taskPredict = new TaskPredict();
			taskPredict.execute();
	    }
	    else if (strActionCommand == "Preloaded Values")
	    {
	    	Frame[] frames = Frame.getFrames();
	    	PreloadedValuesDialog dlgPreloadedValues = new PreloadedValuesDialog(frames[0]);
	    	
	    	// Show the dialog.
	    	dlgPreloadedValues.setVisible(true);
	    	
	    	if (dlgPreloadedValues.m_bOk == false)
	    		return;
    		
    		contentPane.jtxtInnerDiameter.setText(((Double)GlobalsDan.dGradientPrograms[dlgPreloadedValues.m_iCondition][0][0]).toString());
    		contentPane.jtxtColumnLength.setText(((Double)GlobalsDan.dGradientPrograms[dlgPreloadedValues.m_iCondition][0][1]).toString());
    		contentPane.jtxtFlowRate.setText(((Double)GlobalsDan.dGradientPrograms[dlgPreloadedValues.m_iCondition][0][2]).toString());
    		contentPane.jtxtIDT.setText(((Double)GlobalsDan.dGradientPrograms[dlgPreloadedValues.m_iCondition][0][3]).toString());
    		
    		/*int rowcount = contentPane.tmGradientProgram.getRowCount();
    		for (int i = 0; i < rowcount; i++)
    		{
        		contentPane.tmGradientProgram.removeRow(0);
    		}*/
    		
    		this.m_bDoNotChangeTable = true;
    		contentPane.tmGradientProgram.setRowCount(0);

    		for (int i = 1; i < GlobalsDan.dGradientPrograms[dlgPreloadedValues.m_iCondition].length; i++)
    		{
        		Object[] rowData = {
        				GlobalsDan.dGradientPrograms[dlgPreloadedValues.m_iCondition][i][0],
        				GlobalsDan.dGradientPrograms[dlgPreloadedValues.m_iCondition][i][1]};

    			contentPane.tmGradientProgram.addRow(rowData);
    		}
    		
    		// Populate the table with the retention times of the standards
			for (int i = 0; i < GlobalsDan.dPredefinedValues[dlgPreloadedValues.m_iCondition][0].length; i++)
			{
	            contentPane.tmMeasuredRetentionTimes.setValueAt(GlobalsDan.dPredefinedValues[dlgPreloadedValues.m_iCondition][0][i], i, 3);
	            if (GlobalsDan.dPredefinedValues[dlgPreloadedValues.m_iCondition][0][i] > 0)
		            contentPane.tmMeasuredRetentionTimes.setValueAt(true, i, 0);	            	
	            else
		            contentPane.tmMeasuredRetentionTimes.setValueAt(false, i, 0);	            	
			}
			
			// Populate the table with the retention times of the test compounds
			for (int i = 0; i < GlobalsDan.dPredefinedValues[dlgPreloadedValues.m_iCondition][1].length; i++)
			{
	            double dRetentionTime = GlobalsDan.roundToSignificantFigures(GlobalsDan.dPredefinedValues[dlgPreloadedValues.m_iCondition][1][i], 5);
	            
	            if (dRetentionTime == 0)
	            	contentPane2.m_tmTestCompoundsModel.setValueAt("", i, 3);
	            else
	            	contentPane2.m_tmTestCompoundsModel.setValueAt(dRetentionTime, i, 3);
			}
			
	    	performValidations();
	    	
	    }
	    else if (strActionCommand == "Insert Row")
	    {
	    	int iSelectedRow = contentPane.jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = contentPane.tmGradientProgram.getRowCount()-1;
	    	
	    	Double dRowValue1 = (Double) contentPane.tmGradientProgram.getValueAt(iSelectedRow, 0);
	    	Double dRowValue2 = (Double) contentPane.tmGradientProgram.getValueAt(iSelectedRow, 1);
	    	Double dRowData[] = {dRowValue1, dRowValue2};
	    	contentPane.tmGradientProgram.insertRow(iSelectedRow + 1, dRowData);
	    }
	    else if (strActionCommand == "Remove Row")
	    {
	    	int iSelectedRow = contentPane.jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = contentPane.tmGradientProgram.getRowCount()-1;
	    	
	    	if (contentPane.tmGradientProgram.getRowCount() >= 3)
	    	{
	    		contentPane.tmGradientProgram.removeRow(iSelectedRow);
	    	}
	    }
	    else if (strActionCommand == "Find retention times automatically...")
	    {
	    	Component p = this;
	    	while ((p = p.getParent()) != null && !(p instanceof Frame));
	  
	    	PeakFinderSettingsDialog peakFinderSettingsDialog = new PeakFinderSettingsDialog((Frame)p, true, GlobalsDan.StationaryPhaseArray, true);
	    	peakFinderSettingsDialog.setLocationByPlatform(true);
	    	peakFinderSettingsDialog.setIsocraticData(GlobalsDan.StandardIsocraticDataArray);
	    	peakFinderSettingsDialog.setStandardCompoundNames(GlobalsDan.StandardCompoundsNameArray);
	    	peakFinderSettingsDialog.setStandardCompoundMZData(GlobalsDan.StandardCompoundsMZArray);
	    	peakFinderSettingsDialog.setFlowRate(m_dFlowRate);
	    	peakFinderSettingsDialog.setColumnInnerDiameter(m_dColumnInnerDiameter);
	    	peakFinderSettingsDialog.setColumnLength(m_dColumnLength);
	    	double[][] dGradientProgram = new double[this.contentPane.tmGradientProgram.getRowCount()][2];
	    	for (int i = 0; i < dGradientProgram.length; i++)
	    	{
	    		dGradientProgram[i][0] = (Double)contentPane.tmGradientProgram.getValueAt(i, 0);
	    		dGradientProgram[i][1] = (Double)contentPane.tmGradientProgram.getValueAt(i, 1);
	    	}
	    	peakFinderSettingsDialog.setGradientProgram(dGradientProgram);
	    	peakFinderSettingsDialog.setDeadTimeProfile(m_dInitialDeadTimeArray);

	    	// Make the dialog modal
	    	peakFinderSettingsDialog.setVisible(true);
	    	
	    	if (peakFinderSettingsDialog.getOkPressed())
	    	{
	    		double[] dRetentionTimes = peakFinderSettingsDialog.getSelectedRetentionTimes();
	    		boolean[] bSkippedStandards = peakFinderSettingsDialog.getSkippedStandards();
	    		int[] iPeakRank = peakFinderSettingsDialog.getSelectedPeakRank();
	    		
	    		for (int i = 0; i < dRetentionTimes.length; i++)
	    		{
	    			if (bSkippedStandards[i] == false && iPeakRank[i] != -1)
	    			{
	    				this.contentPane.tmMeasuredRetentionTimes.setValueAt(true, i, 0);
	    				this.contentPane.tmMeasuredRetentionTimes.setValueAt(dRetentionTimes[i], i, 3);
	    			}
	    		}
	    		
	    		performValidations();
	    		
	    		m_strFileName = peakFinderSettingsDialog.getFileName();
	    	}
	    }
	    else if (strActionCommand == "IDT Units Changed")
	    {
			if (contentPane.jcboIDTUnits.getSelectedIndex() == 0) // min selected
			{
				contentPane.jtxtIDT.setText(Float.toString((float)this.m_dInstrumentDeadTime));
				contentPane.jlblInstrumentDeadTime.setText("Instrument dead time:");
			}
			else // mL selected
			{
				contentPane.jtxtIDT.setText(Float.toString((float)(this.m_dInstrumentDeadTime * this.m_dFlowRate)));
				contentPane.jlblInstrumentDeadTime.setText("Instrument dead volume:");
			}
			
	    	performValidations();
	    }
	    else if (strActionCommand == "Automatically Determine Test Compound Retention Times")
	    {
	    	Component p = this;
	    	while ((p = p.getParent()) != null && !(p instanceof Frame));
	  
	    	PeakFinderSettingsDialog peakFinderSettingsDialog = new PeakFinderSettingsDialog((Frame)p, true, GlobalsDan.StationaryPhaseArray, false);
	    	peakFinderSettingsDialog.setLocationByPlatform(true);
	    	peakFinderSettingsDialog.setIsocraticData(GlobalsDan.TestCompoundsIsocraticDataArray);
	    	peakFinderSettingsDialog.setStandardCompoundNames(GlobalsDan.TestCompoundNameArray);
	    	peakFinderSettingsDialog.setStandardCompoundMZData(GlobalsDan.TestCompoundMZArray);
	    	peakFinderSettingsDialog.setFlowRate(m_dFlowRate);
	    	peakFinderSettingsDialog.setColumnInnerDiameter(m_dColumnInnerDiameter);
	    	peakFinderSettingsDialog.setColumnLength(m_dColumnLength);

	    	peakFinderSettingsDialog.setInstrumentDeadTime(m_dInstrumentDeadTime);
	    	double[][] dGradientProgram = new double[this.contentPane.tmGradientProgram.getRowCount()][2];
	    	for (int i = 0; i < dGradientProgram.length; i++)
	    	{
	    		dGradientProgram[i][0] = (Double)contentPane.tmGradientProgram.getValueAt(i, 0);
	    		dGradientProgram[i][1] = (Double)contentPane.tmGradientProgram.getValueAt(i, 1);
	    	}
	    	peakFinderSettingsDialog.setGradientProgram(dGradientProgram);
	    	
	    	// Now set the back-calculated gradient profile
	    	double[][] dCombinedGradientProfileArray = new double[m_dSimpleGradientArray.length][2];
	    	for (int i = 0; i < m_dSimpleGradientArray.length; i++)
	    	{
	    		dCombinedGradientProfileArray[i][0] = m_dSimpleGradientArray[i][0];
	    		dCombinedGradientProfileArray[i][1] = m_dSimpleGradientArray[i][1] + this.m_InterpolatedGradientDifferenceProfile.getAt(m_dSimpleGradientArray[i][0]);
	    	}
	    	peakFinderSettingsDialog.setGradientProfile(dCombinedGradientProfileArray);

	    	// Now set the back-calculated dead time profile
	    	int iNumPoints = 1000;
	    	double[][] dCombinedDeadTimeProfileArray = new double[iNumPoints][2];
	    	double dStartPhi = m_dInitialDeadTimeArray[0][0];
	    	double dEndPhi = m_dInitialDeadTimeArray[m_dInitialDeadTimeArray.length - 1][0];
	    	for (int i = 0; i < iNumPoints; i++)
	    	{
	    		double dCurrentPhi = (((dEndPhi - dStartPhi) / (double)(iNumPoints - 1)) * (double)i) + dStartPhi;
	    		dCombinedDeadTimeProfileArray[i][0] = dCurrentPhi;
	    		dCombinedDeadTimeProfileArray[i][1] = m_InitialInterpolatedDeadTimeProfile.getAt(dCurrentPhi) + m_InterpolatedDeadTimeDifferenceProfile.getAt(dCurrentPhi);
	    	}
	    	peakFinderSettingsDialog.setDeadTimeProfile(dCombinedDeadTimeProfileArray);
	    	
	    	peakFinderSettingsDialog.setFileName(this.m_strFileName);
	    	
	    	// Set the tstep to the last one used in back-calculation
	    	peakFinderSettingsDialog.setTStep(this.m_dtstep);
	    		    	
	    	peakFinderSettingsDialog.setVisible(true);
	    	
	    	if (peakFinderSettingsDialog.getOkPressed())
	    	{
	    		double[] dRetentionTimes = peakFinderSettingsDialog.getSelectedRetentionTimes();
	    		boolean[] bSkippedStandards = peakFinderSettingsDialog.getSkippedStandards();
	    		int[] iPeakRank = peakFinderSettingsDialog.getSelectedPeakRank();
	    		
	    		for (int i = 0; i < dRetentionTimes.length; i++)
	    		{
	    			if (bSkippedStandards[i] == true)
	    			{
	    				this.contentPane2.m_tmTestCompoundsModel.setValueAt("", i, 3);
	    			}
	    			else if (iPeakRank[i] != -1)
	    			{
	    				this.contentPane2.m_tmTestCompoundsModel.setValueAt(GlobalsDan.roundToSignificantFigures(dRetentionTimes[i], 5), i, 3);
	    			}
	    		}
	    	}
	    }
	    else if(strActionCommand == "Update Database"){
	    	//ToDo
	    	Frame[] frame = Frame.getFrames();
	    	taskUpdateDb = new TaskUpdateDatabase();
	    	updateDialog = new UpdateProgressDialog(frame[0], taskUpdateDb);
	    	updateDialog.setVisible(true);
	    	
	    }
	    else if(strActionCommand == "Import Data"){
	    	importXmlData();
	    }
	}
    
    public void checkForLocalUpdatesAndStartUpdate(){
    	final File database_dir = new File(path+this.database_dir+"/");
		final File compressedDb = new File(local_db_summary);
		final boolean[] foundUpdates = {false};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable task = new Runnable(){

			@Override
			public void run() {
				if(database_dir.exists() && compressedDb.exists()){
					foundUpdates[0] = Utilities.checkForLocalUpdates(database_dir, compressedDb);
				}
			}
			
		};
		executor.schedule(task, 10, TimeUnit.MILLISECONDS);
		executor.shutdown();
		
		ArrayList<IsocraticCompound> list = new ArrayList<IsocraticCompound>(16);
		if(compressedDb.exists() && (foundUpdates[0] || otherCompounds.size() == 0)){
			list = Utilities.readDatabase(compressedDb);
		}
		updateOtherCompoundsList(list);
    }
    
    public void updateOtherCompoundsList(List<IsocraticCompound> list){
    	// Fill in the table with the solutes that weren't selected
    	if(list.size() > 0){
        	contentPane2.tmPredictionModel.getDataVector().clear();
        	otherCompounds.clear();
    		otherCompounds.addAll(list);

    		//Add compounds to the prediction table
        	for (int i = 0; i < otherCompounds.size(); i++)
        	{
        		Object[] newRow = {otherCompounds.get(i).getId(), null};
    			contentPane2.tmPredictionModel.addRow(newRow);
        	}
    	}
    	
    	
    	contentPane2.jpanelStep4.setVisible(false);
    	contentPane2.jpanelStep5.setVisible(false);
    	contentPane2.jpanelStep6.setVisible(true);
    	contentPane2.jbtnNextStep.setVisible(false);
    	contentPane2.jbtnPredict.setEnabled(true);
    	contentPane2.jbtnUpdateIsocraticDatabase.setEnabled(true);
    	contentPane2.jProgressBar2.setString("");
    	contentPane2.jProgressBar2.setIndeterminate(false);
    }
    
    public void importXmlData(){
    	long n1 = 0;
    	ArrayList<IsocraticCompound> list = new ArrayList<IsocraticCompound>(16);
    	Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    	JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify local database directory/file");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		// Set default directory
		String lastOutputDir = prefs.get("LAST_OUTPUT_DIR", "");
		if (lastOutputDir != "")
		{
			File lastDir = new File(lastOutputDir);
			if (lastDir.exists())
				fileChooser.setCurrentDirectory(lastDir.getParentFile());
		}
		
		int result = fileChooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION){
			File selectedFile = fileChooser.getSelectedFile();
			if(selectedFile != null){
				n1 = System.currentTimeMillis();
				String path = selectedFile.getPath();
    			String extension = FilenameUtils.getExtension(path.toLowerCase()); 
				if(selectedFile.isFile()){
					String[] xmlValues = Utilities.xmlFileParser(selectedFile);
					IsocraticCompound comp = Utilities.valuesToIsocraticCompoundObj(xmlValues);
					list.add(comp);
				}
				else {
					list = Utilities.parseDirectory(selectedFile, list);
				}
			}
		}
		updateOtherCompoundsList(list);
		contentPane2.jProgressBar2.setStringPainted(true);
		contentPane2.jbtnPredict.setActionCommand("Predict");
		contentPane2.jbtnPredict.setText("Predict dB");
		
		long n2 = System.currentTimeMillis();
		System.out.println((n2-n1) + "ms");
    }
    
    public double[] convertToDoubles(String[] values){
    	double[] result = new double[values.length];
    	for(int i = 0; i < values.length; i++){
    		result[i] = Double.parseDouble(values[i]);
    	}
    	return result;
    }
    
    public void copyProfilesToClipboard()
    {
    	// Make a string that can be pasted into Excel
    	String outString = "";
    	String eol = System.getProperty("line.separator");
    	// First create the heading
    	// Programmed conditions
    	outString += "Programmed (initial) experimental conditions" + eol;
    	outString += "Stationary phase:\t" + GlobalsDan.StationaryPhaseArray[this.contentPane.jcboStationaryPhase.getSelectedIndex()] + eol;
    	outString += "Column inner diameter:\t" + this.contentPane.jtxtInnerDiameter.getText() + "\tmm" + eol;
    	outString += "Column length:\t" + this.contentPane.jtxtColumnLength.getText() + "\tmm" + eol;
    	outString += "Flow rate:\t" + this.contentPane.jtxtFlowRate.getText() + "\tmL/min" + eol;
    	outString += "Instrument dead time:\t" + Float.toString((float)this.m_dInstrumentDeadTime) + "\tmin" + eol;
    	outString += "Gradient program:" + eol;
    	outString += "Time (min)\tEluent composition (%B)" + eol;
    	for (int i = 0; i < this.contentPane.tmGradientProgram.getRowCount(); i++)
    	{
    		outString += contentPane.tmGradientProgram.getValueAt(i, 0) + "\t" + contentPane.tmGradientProgram.getValueAt(i, 1) + eol;
    	}
    	outString += eol;
    	
		NumberFormat formatter3 = new DecimalFormat("0.000");

    	// Calculated gradient delay
    	outString += "Gradient delay volume" + eol;
    	
    	outString += eol;
    	
    	// Standards, experimental, calculated, and error
    	outString += "Retention times of standards" + eol;
    	outString += "Standard\tExperimental retention time (min)\tProjected retention time (min)\tDifference (min)" + eol;
    	for (int i = 0; i < this.contentPane2.tmOutputModel.getRowCount(); i++)
    	{
    		outString += this.contentPane2.tmOutputModel.getValueAt(i, 0) + "\t" + this.contentPane2.tmOutputModel.getValueAt(i, 1) + "\t" + this.contentPane2.tmOutputModel.getValueAt(i, 2) + "\t" + this.contentPane2.tmOutputModel.getValueAt(i, 3) + eol;
    	}
    	outString += eol;
    	
    	// System suitability check results
    	outString += "System suitability check" + eol;
    	outString += "Standard\tm/z\tExperimental retention time (min)\tProjected retention time (min)\tError (min)" + eol;
    	for (int i = 0; i < this.contentPane2.m_tmTestCompoundsModel.getRowCount(); i++)
    	{
    		outString += this.contentPane2.m_tmTestCompoundsModel.getValueAt(i, 0) + "\t" + this.contentPane2.m_tmTestCompoundsModel.getValueAt(i, 1) + "\t" + this.contentPane2.m_tmTestCompoundsModel.getValueAt(i, 3) + "\t" + this.contentPane2.m_tmTestCompoundsModel.getValueAt(i, 2) + "\t" + this.contentPane2.m_tmTestCompoundsModel.getValueAt(i, 4) + eol;
    	}
    	outString += eol;
    	outString += "Your overall prediction error:" + "\t" + contentPane2.jlblStandardDeviation.getText() + eol;
    	outString += "Expected error for a new column:" + "\t" + contentPane2.jlblExpectedStandardDeviation.getText() + eol;    	
    	outString += "Your column's rating:" + "\t" + contentPane2.jlblColumnRating.getText() + eol;
    	outString += eol;
    	
    	// Predicted retention times
    	outString += "Predicted retention times" + eol;
    	outString += "Retention time tolerance window confidence:\t" + Float.toString((float)this.m_dConfidenceInterval * 100) + eol;
    	outString += "Compound\tPredicted retention time (min)\tRetention time tolerance window (+/- min)" + eol;
    	for (int i = 0; i < this.contentPane2.tmPredictionModel.getRowCount(); i++)
    	{
    		outString += this.contentPane2.tmPredictionModel.getValueAt(i, 0) + "\t" + this.contentPane2.tmPredictionModel.getValueAt(i, 1) + "\t" + this.contentPane2.tmPredictionModel.getValueAt(i, 2) + eol;
    	}
    	outString += eol;

    	outString += "Number of iterations:\t" + contentPane2.jlblIterationNumber.getText() + eol;
    	outString += "Variance (\u03a3\u00b2):\t" + contentPane2.jlblVariance.getText() + "\tmin\u00b2" + eol;
    	outString += "Time elapsed (min):\t" + contentPane2.jlblTimeElapsed.getText() + eol;
    	outString += eol;
    	
    	outString += "Back-calculated gradient profile\t\t\tBack-calculated dead time profile" + eol;
    	outString += "Time (min)\tEluent composition (%B)\t\tEluent Composition (%B)\tDead time (s)" + eol;
    	
    	int iNumPoints = 1000;
    	for (int i = 0; i < iNumPoints; i++)
    	{
    		double dCurrentTime = this.m_dPlotXMax2 * ((double)i / ((double)iNumPoints - 1));
    		double dSolventComposition = this.m_InterpolatedSimpleGradient.getAt(dCurrentTime) + this.m_InterpolatedGradientDifferenceProfile.getAt(dCurrentTime);

    		double dCurrentSolventComposition = 0.9 * ((double)i / ((double)iNumPoints - 1)) + 0.05;
    		double dDeadTime = (this.m_InitialInterpolatedDeadTimeProfile.getAt(dCurrentSolventComposition) + this.m_InterpolatedDeadTimeDifferenceProfile.getAt(dCurrentSolventComposition));
    		
    		outString += Float.toString((float)dCurrentTime) + "\t" + Float.toString((float)dSolventComposition) + "\t\t" + Float.toString((float)dCurrentSolventComposition) + "\t" + Float.toString((float)dDeadTime) + eol;
    	}
    	
    	StringSelection stringSelection = new StringSelection(outString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
        
        JOptionPane.showMessageDialog(null, "A report has been copied to the clipboard. You may now paste it into a spreadsheet.", "Measure Your Gradient", JOptionPane.INFORMATION_MESSAGE);
    }
    
	//@Override
	public void keyPressed(KeyEvent arg0) 
	{
		
	}

	//@Override
	public void keyReleased(KeyEvent e) 
	{

	}

	//@Override
	public void keyTyped(KeyEvent e) 
	{
		//JTextField source = (JTextField)e.getSource();
		
		if (!((Character.isDigit(e.getKeyChar()) ||
				(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
				(e.getKeyChar() == KeyEvent.VK_DELETE) ||
				(e.getKeyChar() == KeyEvent.VK_PERIOD))))
		{
	        e.consume();
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
		validateConfidenceWindow();

		// Create dead time array
        this.m_dInitialDeadTimeArray = new double[GlobalsDan.dDeadTimeArray.length][2];
        
        for (int i = 0; i < GlobalsDan.dDeadTimeArray.length; i++)
        {
        	double dVolumeInRefColumn = Math.PI * Math.pow(GlobalsDan.dRefColumnID / 2, 2) * GlobalsDan.dRefColumnLength;
        	double dDeadVolPerVol = (GlobalsDan.dDeadTimeArray[i][1] * GlobalsDan.dRefFlowRate) / dVolumeInRefColumn;
        	double dNewDeadVol = dDeadVolPerVol * Math.PI * Math.pow((this.m_dColumnInnerDiameter / 2) / 10, 2) * this.m_dColumnLength / 10;
        	this.m_dInitialDeadTimeArray[i][0] = GlobalsDan.dDeadTimeArray[i][0];
        	this.m_dInitialDeadTimeArray[i][1] = (dNewDeadVol / this.m_dFlowRate) * 60;
        }
        
		this.m_InitialInterpolatedDeadTimeProfile = new InterpolationFunction(this.m_dInitialDeadTimeArray);

		validateInstrumentDeadTime();
		
		// Find longest retention time
		double dLongestRetentionTime = 0;
		for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
		{
			double dValue = (Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3);
			if (dValue > dLongestRetentionTime)
				dLongestRetentionTime = dValue;
		}

		double dLastProgramTime = (Double)contentPane.tmGradientProgram.getValueAt(contentPane.tmGradientProgram.getRowCount() - 1, 0);
		m_dPlotXMax = Math.max(dLongestRetentionTime * 1.1, dLastProgramTime * 1.1);
		
    	contentPane.m_GraphControlGradient.RemoveAllSeries();
    	contentPane.m_GraphControlFlowRate.RemoveAllSeries();

    	int iIdealPlotIndex = contentPane.m_GraphControlGradient.AddSeries("Ideal Gradient", new Color(0, 0, 0), 1, false, false);
    	int iIdealPlotIndexDeadTime = contentPane.m_GraphControlFlowRate.AddSeries("Ideal Dead Time", new Color(0, 0, 0), 1, false, false);

    	//contentPane.m_GraphControlFlowRate.AddDataPoint(iIdealPlotIndexHoldUp, 0, this.m_dFlowRate / 1000);
    	//contentPane.m_GraphControlFlowRate.AddDataPoint(iIdealPlotIndexHoldUp, m_dPlotXMax * 60, this.m_dFlowRate / 1000);

	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	double dDeadTime = this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos);
	    	contentPane.m_GraphControlFlowRate.AddDataPoint(iIdealPlotIndexDeadTime, dXPos * 100, dDeadTime);
	    }

    	contentPane.m_GraphControlGradient.AddDataPoint(iIdealPlotIndex, 0, (Double)contentPane.tmGradientProgram.getValueAt(0, 1));	
    	double dLastTime = 0;
    	
    	for (int i = 0; i < contentPane.tmGradientProgram.getRowCount(); i++)
		{
    		if ((Double)contentPane.tmGradientProgram.getValueAt(i, 0) > dLastTime)
    		{
    			double dTime = (Double)contentPane.tmGradientProgram.getValueAt(i, 0);
    			double dFractionB = (Double)contentPane.tmGradientProgram.getValueAt(i, 1);
    			
    	    	contentPane.m_GraphControlGradient.AddDataPoint(iIdealPlotIndex, dTime * 60, dFractionB);	
    		
    	    	dLastTime = dTime;
    		}
		}
    	
		contentPane.m_GraphControlGradient.AddDataPoint(iIdealPlotIndex, m_dPlotXMax * 60, (Double)contentPane.tmGradientProgram.getValueAt(contentPane.tmGradientProgram.getRowCount() - 1, 1));

   		contentPane.m_GraphControlGradient.AutoScaleX();
   		contentPane.m_GraphControlGradient.AutoScaleY();
    	
    	contentPane.m_GraphControlGradient.repaint();   
    	
   		contentPane.m_GraphControlFlowRate.AutoScaleX();
   		contentPane.m_GraphControlFlowRate.AutoScaleY();
    	
    	contentPane.m_GraphControlFlowRate.repaint();   	
	}

	//@Override
	public void focusGained(FocusEvent e) 
	{
		
	}

	//@Override
	public void focusLost(FocusEvent e) 
	{
		performValidations();
	}

	//@Override
	public void valueChanged(ListSelectionEvent arg0) 
	{
		performValidations();
		
	}
	
	//@Override
	public void autoScaleChanged(AutoScaleEvent event) 
	{
		//if(event.getSource()==contentPane.m_GraphControl)
		//{
/*			if (event.getAutoScaleXState() == true)
				contentPane.jbtnAutoscaleX.setSelected(true);
			else
				contentPane.jbtnAutoscaleX.setSelected(false);
			
			if (event.getAutoScaleYState() == true)
				contentPane.jbtnAutoscaleY.setSelected(true);
			else
				contentPane.jbtnAutoscaleY.setSelected(false);
			
			if (event.getAutoScaleXState() == true && event.getAutoScaleYState() == true)
				contentPane.jbtnAutoscale.setSelected(true);			
			else
				contentPane.jbtnAutoscale.setSelected(false);	
		}
		else if (event.getSource()==contentPane.m_GraphControlFlow)
		{
			if (event.getAutoScaleXState() == true)
				contentPane.jbtnAutoscaleXFlow.setSelected(true);
			else
				contentPane.jbtnAutoscaleXFlow.setSelected(false);
			
			if (event.getAutoScaleYState() == true)
				contentPane.jbtnAutoscaleYFlow.setSelected(true);
			else
				contentPane.jbtnAutoscaleYFlow.setSelected(false);
			
			if (event.getAutoScaleXState() == true && event.getAutoScaleYState() == true)
				contentPane.jbtnAutoscaleFlow.setSelected(true);			
			else
				contentPane.jbtnAutoscaleFlow.setSelected(false);				
		}*/
	}

	@Override
	public void tableChanged(TableModelEvent e) 
	{
		if (m_bDoNotChangeTable)
		{
			m_bDoNotChangeTable = false;
			return;
		}
		
		if(e.getSource() == contentPane.tmGradientProgram || e.getSource() == contentPane.tmMeasuredRetentionTimes)
		{
			if(e.getSource() == contentPane.tmGradientProgram)
			{
				int iChangedRow = e.getFirstRow();
				int iChangedColumn = e.getColumn();
	
				Double dRowValue1 = 0.0;
				Double dRowValue2 = 0.0;
				
				if (iChangedRow < contentPane.tmGradientProgram.getRowCount())
				{
					dRowValue1 = (Double) contentPane.tmGradientProgram.getValueAt(iChangedRow, 0);
					dRowValue2 = (Double) contentPane.tmGradientProgram.getValueAt(iChangedRow, 1);
				}
				
		    	if (iChangedColumn == 0)
				{
					// If the column changed was the first, then make sure the time falls in the right range
					if (iChangedRow == 0)
					{
						// No changes allowed in first row - must be zero min
						dRowValue1 = 0.0;
					}
					else if (iChangedRow == contentPane.tmGradientProgram.getRowCount() - 1)
					{
						Double dPreviousTime = (Double) contentPane.tmGradientProgram.getValueAt(contentPane.tmGradientProgram.getRowCount() - 2, 0);
						// If it's the last row, just make sure the time is greater than or equal to the time before it.
						if (dRowValue1 < dPreviousTime)
							dRowValue1 = dPreviousTime;
					}
					else
					{
						Double dPreviousTime = (Double) contentPane.tmGradientProgram.getValueAt(iChangedRow - 1, 0);
						Double dNextTime = (Double) contentPane.tmGradientProgram.getValueAt(iChangedRow + 1, 0);
						
						if (dRowValue1 < dPreviousTime)
							dRowValue1 = dPreviousTime;
						
						if (dRowValue1 > dNextTime)
							dRowValue1 = dNextTime;
					}
					
			    	m_bDoNotChangeTable = true;
			    	contentPane.tmGradientProgram.setValueAt(dRowValue1, iChangedRow, iChangedColumn);
				}
				else if (iChangedColumn == 1)
				{
					// If the column changed was the second, then make sure the solvent composition falls between 0 and 100
					if (dRowValue2 > 100)
						dRowValue2 = 100.0;
					
					if (dRowValue2 < 0)
						dRowValue2 = 0.0;
					
			    	m_bDoNotChangeTable = true;
			    	contentPane.tmGradientProgram.setValueAt(dRowValue2, iChangedRow, iChangedColumn);
				}
			}
			else if (e.getSource() == contentPane.tmMeasuredRetentionTimes)
			{
				int iChangedRow = e.getFirstRow();
				int iChangedColumn = e.getColumn();
	
				if (iChangedColumn == 3)
				{
			    	if (contentPane.tmMeasuredRetentionTimes.getValueAt(iChangedRow, 3) == null)
			    	{
				    	m_bDoNotChangeTable = true;
			    		contentPane.tmMeasuredRetentionTimes.setValueAt((Double)0.0, iChangedRow, 3);
			    	}
			    	
			    	double dNewValue = (Double)contentPane.tmMeasuredRetentionTimes.getValueAt(iChangedRow, 3);
					
					if (dNewValue < 0)
						dNewValue = 0;
					if (dNewValue > 9999999)
						dNewValue = 9999999;
					
			    	m_bDoNotChangeTable = true;
		    		contentPane.tmMeasuredRetentionTimes.setValueAt((Double)dNewValue, iChangedRow, 3);
					
			    	m_bDoNotChangeTable = true;
		    		if (dNewValue <= 0)
		    			contentPane.tmMeasuredRetentionTimes.setValueAt((Boolean)false, iChangedRow, 0);
		    		else
		    			contentPane.tmMeasuredRetentionTimes.setValueAt((Boolean)true, iChangedRow, 0);
				}
			}
			
			performValidations();
			
			contentPane.m_GraphControlGradient.removeAllLineMarkers();
			contentPane.m_GraphControlFlowRate.removeAllLineMarkers();
			
			for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
			{
				if ((Boolean)contentPane.tmMeasuredRetentionTimes.getValueAt(i,0) == true)
				{
					contentPane.m_GraphControlGradient.addLineMarker((Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i,3), (String)contentPane.tmMeasuredRetentionTimes.getValueAt(i,1));
					//contentPane.m_GraphControlFlowRate.addLineMarker((Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i,3), (String)contentPane.tmMeasuredRetentionTimes.getValueAt(i,1));
				}
			}
	
			contentPane.m_GraphControlGradient.repaint();
			contentPane.m_GraphControlFlowRate.repaint();
			
			// Make sure there are at least 6 used retention times
			int iNumUsed = 0;
			
			for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
			{
				if ((Boolean)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 0) == true)
					iNumUsed++;
			}
			
			if (iNumUsed >= 6)
				contentPane.jbtnNextStep.setEnabled(true);
			else
				contentPane.jbtnNextStep.setEnabled(false);
		}
		else if (e.getSource() == contentPane2.m_tmTestCompoundsModel)
		{
			int iChangedRow = e.getFirstRow();
			int iChangedColumn = e.getColumn();

			if (iChangedColumn == 3)
			{
		    	if (contentPane2.m_tmTestCompoundsModel.getValueAt(iChangedRow, 3) == "")
		    	{
			    	m_bDoNotChangeTable = true;
		    		contentPane2.m_tmTestCompoundsModel.setValueAt("", iChangedRow, 3);
		    		
		    		updateTestCompoundTable();
		    	}
		    	else
		    	{
			    	double dNewValue = (Double)contentPane2.m_tmTestCompoundsModel.getValueAt(iChangedRow, 3);
					
					if (dNewValue < 0)
						dNewValue = 0;
					if (dNewValue > 9999999)
						dNewValue = 9999999;
					
			    	m_bDoNotChangeTable = true;
		    		contentPane2.m_tmTestCompoundsModel.setValueAt((Double)dNewValue, iChangedRow, 3);
		    		
		    		updateTestCompoundTable();
		    	}
			}
		}
	}
	
	public void updateGraphs(boolean bAlsoUpdateTable)
	{
		synchronized(contentPane2.m_GraphControlGradient.lockObject)
		{
		synchronized(contentPane2.m_GraphControlFlowRate.lockObject)
		{
		
		// Update the graphs with the new m_dGradientArray markers and the m_InterpolatedGradient (and the same with the flow graph)
		contentPane2.m_GraphControlGradient.RemoveSeries(m_iInterpolatedGradientProgramSeries);
		contentPane2.m_GraphControlGradient.RemoveSeries(m_iGradientProgramMarkerSeries);
	
		contentPane2.m_GraphControlGradient.RemoveSeries(m_iSimpleGradientSeries);		
		
		contentPane2.m_GraphControlFlowRate.RemoveSeries(m_iInterpolatedFlowRateSeries);
		contentPane2.m_GraphControlFlowRate.RemoveSeries(m_iFlowRateMarkerSeries);
		
		contentPane2.m_GraphControlFlowRate.RemoveSeries(m_iSimpleDeadTimeSeries);
		
	    if (m_bShowSimpleGradient)
	    	m_iSimpleGradientSeries = contentPane2.m_GraphControlGradient.AddSeries("Simple Gradient", new Color(170,170,170), 1, false, false);
	    m_iInterpolatedGradientProgramSeries = contentPane2.m_GraphControlGradient.AddSeries("Interpolated Gradient", new Color(225,0,0), 1, false, false);
	    m_iGradientProgramMarkerSeries = contentPane2.m_GraphControlGradient.AddSeries("Gradient Markers", new Color(225,0,0), 1, true, false);

	    if (m_bShowSimpleDeadTime)
	    	m_iSimpleDeadTimeSeries = contentPane2.m_GraphControlFlowRate.AddSeries("Simple Dead Time", new Color(170,170,170), 1, false, false);
	    m_iInterpolatedFlowRateSeries = contentPane2.m_GraphControlFlowRate.AddSeries("Interpolated Flow", new Color(225,0,0), 1, false, false);
	    m_iFlowRateMarkerSeries = contentPane2.m_GraphControlFlowRate.AddSeries("Flow Rate Markers", new Color(225,0,0), 1, true, false);

	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = ((double)i / (double)(iNumPoints - 1)) * (m_dPlotXMax2 * 60);
	    	contentPane2.m_GraphControlGradient.AddDataPoint(m_iInterpolatedGradientProgramSeries, dXPos, this.m_InterpolatedSimpleGradient.getAt(dXPos / 60) + m_InterpolatedGradientDifferenceProfile.getAt(dXPos / 60));
	    	if (m_bShowSimpleGradient)
	    		contentPane2.m_GraphControlGradient.AddDataPoint(m_iSimpleGradientSeries, dXPos, m_InterpolatedSimpleGradient.getAt(dXPos / 60));

	    	dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	contentPane2.m_GraphControlFlowRate.AddDataPoint(m_iInterpolatedFlowRateSeries, dXPos * 100, this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos) + this.m_InterpolatedDeadTimeDifferenceProfile.getAt(dXPos));
	    	if (m_bShowSimpleDeadTime)
	    		contentPane2.m_GraphControlFlowRate.AddDataPoint(m_iSimpleDeadTimeSeries, dXPos * 100, m_InterpolatedSimpleDeadTime.getAt(dXPos));
	    }
	    
	    for (int i = 0; i < m_dGradientProfileDifferenceArray.length; i++)
	    {
	    	contentPane2.m_GraphControlGradient.AddDataPoint(m_iGradientProgramMarkerSeries, m_dGradientProfileDifferenceArray[i][0] * 60, this.m_InterpolatedSimpleGradient.getAt(m_dGradientProfileDifferenceArray[i][0]) + m_dGradientProfileDifferenceArray[i][1]);
	    }
		
	    for (int i = 0; i < this.m_dDeadTimeDifferenceArray.length; i++)
	    {
	    	double dXPos = m_dDeadTimeDifferenceArray[i][0];
	    	contentPane2.m_GraphControlFlowRate.AddDataPoint(m_iFlowRateMarkerSeries, m_dDeadTimeDifferenceArray[i][0] * 100, this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos) + this.m_InterpolatedDeadTimeDifferenceProfile.getAt(dXPos));
	    }
	    
	    contentPane2.m_GraphControlGradient.AutoScaleToSeries(m_iInterpolatedGradientProgramSeries);
	    contentPane2.m_GraphControlGradient.AutoScaleY();
	    contentPane2.m_GraphControlFlowRate.AutoScaleX();
	    contentPane2.m_GraphControlFlowRate.AutoScaleY();
     
	    contentPane2.m_GraphControlGradient.repaint();
	    contentPane2.m_GraphControlFlowRate.repaint();

	    if (bAlsoUpdateTable)
	    {
			NumberFormat formatter = new DecimalFormat("#0.0000");
	
		    for (int i = 0; i < m_vectCalCompounds.size(); i++)
		    {
		    	double dMeasuredTime = (Double)m_vectCalCompounds.get(i)[1];
		    	double dPredictedTime = (Double)m_vectCalCompounds.get(i)[2];
		    	if (dPredictedTime >= 0)
		    	{
		    		contentPane2.tmOutputModel.setValueAt(formatter.format(dPredictedTime + this.m_dInstrumentDeadTime), i, 2);
		    		contentPane2.tmOutputModel.setValueAt(formatter.format(dPredictedTime - dMeasuredTime), i, 3);
		    	}
		    	else
		    	{
		    		contentPane2.tmOutputModel.setValueAt("Did not elute", i, 2);
		    		contentPane2.tmOutputModel.setValueAt("-", i, 3);
		    	}
		    }
	    }
	    
		}
		}
	}
	
	public void beginBackCalculation(boolean bFlowRateProfileBackCalculationFirst)
	{
		contentPane2.jbtnCalculate.setEnabled(false);
		//contentPane2.jbtnCalculate.setText("Please wait...");
		contentPane2.jProgressBar.setIndeterminate(true);
		contentPane2.jProgressBar.setStringPainted(true);
		contentPane2.jProgressBar.setString("Please wait, optimization in progress...");
        
       	task = new Task();
		task.setOptimizationOrder(bFlowRateProfileBackCalculationFirst);
        task.execute();
	}
	
    class Task extends SwingWorker<Void, Void> 
    {
        /*
         * Main task. Executed in background thread.
         */
    	private boolean bFlowRateProfileBackCalculationFirst = true;
    	
    	public void setOptimizationOrder(boolean bFlowRateProfileBackCalculationFirst)
    	{
    		this.bFlowRateProfileBackCalculationFirst = bFlowRateProfileBackCalculationFirst;
    	}
    	
        @Override
        public Void doInBackground() 
        {
    		contentPane2.jProgressBar.setString("Determining gradient delay volume...");
            
            backCalculate(this, bFlowRateProfileBackCalculationFirst);
            return null;
        }
        
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() 
        {
        	if (!this.isCancelled())
        	{
	        	double[] dErrors = new double[m_vectCalCompounds.size()];
	        	for (int i = 0; i < m_vectCalCompounds.size(); i++)
	        	{
	        		dErrors[i] = Math.abs((Double)m_vectCalCompounds.get(i)[1] - (Double)m_vectCalCompounds.get(i)[2]);
	        	}
	        	Arrays.sort(dErrors);
	        	double dMedianError = dErrors[(int)(m_vectCalCompounds.size() / 2)];
	        	
	        	// Now find the compound with the maximum error
	        	int iCompoundWithMaxError = 0;
	        	for (int i = 1; i < m_vectCalCompounds.size(); i++)
	        	{
	        		double dMaxError = Math.abs((Double)m_vectCalCompounds.get(iCompoundWithMaxError)[1] - (Double)m_vectCalCompounds.get(iCompoundWithMaxError)[2]);
	        		double dThisError = Math.abs((Double)m_vectCalCompounds.get(i)[1] - (Double)m_vectCalCompounds.get(i)[2]);
	        		
	        		if (dThisError > dMaxError)
	        			iCompoundWithMaxError = i;
	        	}
	        	
	        	// Determine the ratio of the max error to the median
	    		double dMaxError = Math.abs((Double)m_vectCalCompounds.get(iCompoundWithMaxError)[1] - (Double)m_vectCalCompounds.get(iCompoundWithMaxError)[2]);
	        	double dRatio = dMaxError / dMedianError;
	        	
	        	if (dRatio > 8 && !m_bNoFullBackcalculation)
	        	{
	        		// Report a problem with the retention time for the compound
	            	String strMessage = "<html><body><p style='width: 300px;'>Please double-check the retention time you reported for " + GlobalsDan.StandardCompoundsNameArray[(Integer)m_vectCalCompounds.get(iCompoundWithMaxError)[0]] + ". It does not seem to be correct."
	            		+ "<br><br>"
	            		+ "To correct the retention time you entered, click &quot;Previous Step&quot;, change the retention time, and re-run the back-calculation.</p></body></html>";
	        		
	        		JOptionPane.showMessageDialog(null, strMessage, "Recheck Retention Time", JOptionPane.WARNING_MESSAGE);
	
	        		contentPane2.jProgressBar.setString("Optimization complete, but errors may exist.");        		
	        	}
	        	else if (m_dVariance > 0.0001 && !m_bNoFullBackcalculation)
	        	{
	        		// Report a problem with the retention time for the compound
	            	String strMessage = "<html><body><p style='width: 300px;'>Your variance is high. This usually indicates that more than one of the retention times you entered are incorrect. Please double-check all of the retention times you entered."
            		+ "<br><br>"
            		+ "To correct a retention time you entered, click &quot;Previous Step&quot;, change the retention time, and re-run the back-calculation.</p></body></html>";

	            	JOptionPane.showMessageDialog(null, strMessage, "High Variance", JOptionPane.WARNING_MESSAGE);
	        		
	        		contentPane2.jProgressBar.setString("Optimization complete, but errors may exist.");        		
	        	}
	        	else
	        	{
	        		contentPane2.jProgressBar.setString("Optimization complete! Continue to next step.");        		
	        	}
	        	
	    		contentPane2.jProgressBar.setIndeterminate(false);
	    		contentPane2.jProgressBar.setStringPainted(true);
	    		//contentPane2.jbtnCalculate.setText("Copy profiles to clipboard");
	    		//contentPane2.jbtnCalculate.setActionCommand("Copy to clipboard");
	    		//contentPane2.jbtnCalculate.setEnabled(true);
	    		contentPane2.jbtnNextStep.setEnabled(true);
	    		
        	}
        }
    }
    
    // Projects retention for one compound
    // Input: dIsocraticDataArray (for one compound)
    public PredictedRetentionObject predictRetention(double[][] dIsocraticDataArray)
    {
    	PredictedRetentionObject pro = new PredictedRetentionObject();
    	
		InterpolationFunction IsocraticData = new InterpolationFunction(dIsocraticDataArray);

		double dIntegral = 0;
		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dD = 0;
		double dTotalTime = 0;
		double dTotalDeadTime = 0;
		double dXPosition = 0;
		double dXPositionError = 0;
		double[] dLastXPosition = {0,0};
		double[] dLastko = {0,0};
		double dXMovement = 0;
		Boolean bIsEluted = false;
		double dPhiC = 0;
		double dCurVal = 0;
		double dk = 0;
		double dt0 = 0;
		
		for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += m_dtstep)
		{
			dPhiC = (m_InterpolatedSimpleGradient.getAt(dTotalTime - dIntegral) + m_InterpolatedGradientDifferenceProfile.getAt(dTotalTime - dIntegral)) / 100;

			dk = Math.pow(10, IsocraticData.getAt(dPhiC));
			dCurVal = m_dtstep / dk;
			dt0 = (m_InitialInterpolatedDeadTimeProfile.getAt(dPhiC) + m_InterpolatedDeadTimeDifferenceProfile.getAt(dPhiC)) / 60;
			dXMovement = dCurVal / dt0;
			
			if (dXPosition >= 1)
			{
				dD = ((1 - dLastXPosition[0])/(dXPosition - dLastXPosition[0])) * (dTotalDeadTime - dLastXPosition[1]) + dLastXPosition[1]; 
			}
			else
			{
				dLastXPosition[0] = dXPosition;
				dLastXPosition[1] = dTotalDeadTime;
			}
			
			dTotalDeadTime += dXMovement * dt0;
			
			if (dXPosition >= 1)
			{
				dtRFinal = ((dD - dLastko[0])/(dIntegral - dLastko[0]))*(dTotalTime - dLastko[1]) + dLastko[1];
				double dxdt = (dXPosition - dLastXPosition[0]) / m_dtstep;
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);	
			}
			else
			{
				dLastko[0] = dIntegral;
				dLastko[1] = dTotalTime;
			}
			
			dTotalTime += m_dtstep + dCurVal;
			dIntegral += dCurVal;
			
			if (dXPosition > 1 && bIsEluted == false)
			{
				bIsEluted = true;
				break;
			}
			
			dXPosition += dXMovement;
			
			// Add error to position
			double dXMovementErrorFraction = (dk * 0.03) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;
		}
		
		if (bIsEluted)
		{
			pro.dPredictedRetentionTime = dtRFinal + m_dInstrumentDeadTime;
		}
		else
		{
			pro.dPredictedRetentionTime = -1.0;
		}
		
		// Now calculate final error in the projection
		pro.dPredictedErrorSigma = dtRErrorFinal - dtRFinal;

		return pro;
    }
    
    class TaskPredict extends SwingWorker<Void, Void> 
    {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() 
        {
    		int iNumCompounds = otherCompounds.size();
        	
        	m_PredictedRetentionTimes = new PredictedRetentionObject[iNumCompounds];
    		contentPane2.jProgressBar2.setMinimum(0);
    		contentPane2.jProgressBar2.setMaximum(iNumCompounds - 1);
    		
    		for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
    		{
    			contentPane2.jProgressBar2.setString("Projecting compound " + Integer.toString(iCompound + 1) + " of " + Integer.toString(iNumCompounds));
        		contentPane2.jProgressBar2.setValue(iCompound);
    			
				if (taskPredict.isCancelled())
				{
					return null;
				}
				IsocraticCompound iCompoundObject = otherCompounds.get(iCompound);
//				
//				//For testing where the program breaks. This checks if there is only one data point, then exclude this compound.
//				if(iCompoundObject.getConcentrationList().size() <= 1){
//					System.out.println(iCompound+","+iCompoundObject.getId());
//					m_PredictedRetentionTimes[iCompound] = null;
//					continue;
//				}
				
				m_PredictedRetentionTimes[iCompound] = predictRetention(iCompoundObject.convertListsTo2DArray());
    		}
    		
    		updatePredictionsTable();
    		
    		return null;
        }
        
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() 
        {
//    		contentPane2.jProgressBar2.setIndeterminate(false);
//    		contentPane2.jProgressBar2.setStringPainted(true);
//    		contentPane2.jProgressBar2.setString("Retention predictions complete.");
        	
        	contentPane2.jProgressBar2.setIndeterminate(false);
    		contentPane2.jProgressBar2.setStringPainted(true);
    		contentPane2.jProgressBar2.setString("Calculations complete. Now copy report to clipboard.");
    		contentPane2.jbtnPredict.setActionCommand("Copy to clipboard");
    		contentPane2.jbtnPredict.setText("Copy report to clipboard");
    		contentPane2.jbtnPredict.setEnabled(true);
        }
    }
    
    class TaskUpdateDatabase extends SwingWorker<Void,Void>
    {
    	boolean didAnyUpdateOccur = false;

		@Override
		protected Void doInBackground() throws Exception {

			updateDialog.jProgressBar.setMinimum(0);
			updateDialog.jLblStatus.setText("Checking for updates on retentionprediction.org ...");
			
			File localDbSummaryFile = new File(local_db_summary);
			File localDir = new File(path+database_dir+"/");
			List<String> filesInUpdate = Utilities.findDifferenceInLocalAndWebDb(online_db_summary, localDbSummaryFile);
			
			if(!filesInUpdate.isEmpty()){
				updateDialog.jLblStatus.setText("Updates found. Now initiating the updates ...");
			}
			else{
				updateDialog.jLblStatus.setText("No updates found. Your database is up-to-date");
			}
			updateDialog.jProgressBar.setMaximum(filesInUpdate.size());
	
			if(localDir.exists() && !filesInUpdate.isEmpty()){
				for(int i = 0; i < filesInUpdate.size() ;i++){
					String fileName = filesInUpdate.get(i);
					String urlStr = online_db_dir+fileName;
					String pathToLocalFile = localDir.getPath();
					
					updateDialog.jLblStatus.setText("Downloading file "+ (i+1) + " out of "+ filesInUpdate.size() + " : " + fileName );
					
					
					if(pathToLocalFile.charAt(pathToLocalFile.length()-1) != '/' && pathToLocalFile.charAt(pathToLocalFile.length()-1) != '\\'){
						pathToLocalFile += "\\";
					}
					
					pathToLocalFile += fileName;
					boolean result = Utilities.fileDownloader(urlStr, pathToLocalFile);
					
					if(result)
						didAnyUpdateOccur = true;
					updateDialog.jProgressBar.setValue((i+1));
				}
				if(didAnyUpdateOccur){
					checkForLocalUpdatesAndStartUpdate();
				}
				
			}
			updateDialog.jLblStatus.setText("Database update successful.");
			updateDialog.jBtnCancel.setText("Close");
			updateDialog.jBtnCancel.setActionCommand("Close");
			
			return null;
		}
    	
    }
    
	public void updatePredictionsTable()
	{
		if (m_PredictedRetentionTimes == null)
			return;

		NumberFormat formatter1 = new DecimalFormat("#0.000");

		double dNumSigmas = Erf.erfInv(m_dConfidenceInterval) * Math.sqrt(2);
		
		for (int i = 0; i < m_PredictedRetentionTimes.length; i++)
		{
			if (m_PredictedRetentionTimes[i].dPredictedRetentionTime != -1)
				contentPane2.tmPredictionModel.setValueAt(formatter1.format(m_PredictedRetentionTimes[i].dPredictedRetentionTime), i, 1);
			else
				contentPane2.tmPredictionModel.setValueAt("Did not elute", i, 1);

			contentPane2.tmPredictionModel.setValueAt(Float.toString((float)GlobalsDan.roundToSignificantFigures(m_PredictedRetentionTimes[i].dPredictedErrorSigma * dNumSigmas, 2)), i, 2);
		}
	}
	
	public void updateTestCompoundTable()
	{
		double dSumofSquares = 0;
		double dExpectedSumofSquares = 0;
		double dSumAbsolute = 0;
		double[] dErrorList = new double[contentPane2.m_tmTestCompoundsModel.getRowCount()];
		
		NumberFormat formatter1 = new DecimalFormat("#0.000");
		NumberFormat formatter2 = new DecimalFormat("#0.0");

		int iTotalCompounds = 0;
		
		for (int i = 0; i < contentPane2.m_tmTestCompoundsModel.getRowCount(); i++)
		{
			Double dPredictedRetentionTime = (Double)contentPane2.m_tmTestCompoundsModel.getValueAt(i, 2);
			Object MeasuredRetentionTimeValue = contentPane2.m_tmTestCompoundsModel.getValueAt(i, 3);

	    	m_bDoNotChangeTable = true;
			Double dError;
			if (MeasuredRetentionTimeValue == "")
			{
				dError = null;
				contentPane2.m_tmTestCompoundsModel.setValueAt("", i, 4);
			}
			else
			{
				dError = (Double)MeasuredRetentionTimeValue - dPredictedRetentionTime;
				contentPane2.m_tmTestCompoundsModel.setValueAt(GlobalsDan.roundToSignificantFigures((Double)MeasuredRetentionTimeValue, 5), i, 3);
				contentPane2.m_tmTestCompoundsModel.setValueAt(GlobalsDan.roundToSignificantFigures((Double)dError, 5), i, 4);
			}
			
			if (dError != null && m_dExpectedErrorArray != null)
			{
				dSumofSquares += Math.pow(dError, 2);
				dExpectedSumofSquares += Math.pow(this.m_dExpectedErrorArray[i], 2);
				dSumAbsolute += Math.abs(dError);
				//System.out.println(i+","+dError+","+dSumofSquares+","+dExpectedSumofSquares+","+dSumAbsolute);
				dErrorList[i] = Math.abs(dError);
				iTotalCompounds++;
			}
			else
				dErrorList[i] = -1.0;
		}
		
		if (iTotalCompounds <= 1)
		{
			contentPane2.jlblStandardDeviation.setText("-");
			contentPane2.jlblExpectedStandardDeviation.setText("-");
			contentPane2.jlblColumnRating.setText("-");
			contentPane2.jSliderIndicator.setEnabled(false);
		}
		else
		{
			
			double dStandardDeviation = Math.sqrt(dSumofSquares / (double)iTotalCompounds);
			double dExpectedStandardDeviation = Math.sqrt(dExpectedSumofSquares / (double)iTotalCompounds);
			ChiSquaredDistribution chidist = new ChiSquaredDistribution(iTotalCompounds);
			double d95Limit = Math.sqrt((chidist.inverseCumulativeProbability(.95) * Math.pow(dExpectedStandardDeviation, 2)) / iTotalCompounds);
			double d75Limit = Math.sqrt((chidist.inverseCumulativeProbability(.75) * Math.pow(dExpectedStandardDeviation, 2)) / iTotalCompounds);
			double dColumnRating = dStandardDeviation / d75Limit;
			double dYellowRating = d95Limit / d75Limit;
			
			contentPane2.jlblStandardDeviation.setText("± " + Float.toString((float)GlobalsDan.roundToSignificantFigures(dStandardDeviation, 2)) + " min (" + Float.toString((float)GlobalsDan.roundToSignificantFigures(dStandardDeviation * 60, 2)) + " sec)");
			contentPane2.jlblExpectedStandardDeviation.setText("± " + Float.toString((float)GlobalsDan.roundToSignificantFigures(dExpectedStandardDeviation, 2)) + " min (" + Float.toString((float)GlobalsDan.roundToSignificantFigures(dExpectedStandardDeviation * 60, 2)) + " sec)");
			contentPane2.jlblColumnRating.setText(Float.toString((float)GlobalsDan.roundToSignificantFigures(dColumnRating, 2)));
			
			Color clrGreen = new Color(0, 161, 75);
			Color clrYellow = new Color(188, 174, 0);
			Color clrRed = new Color(236, 28, 36);
			Color clrBlack = new Color(51, 51, 51);
			
			if (dColumnRating <= 1.0)
				contentPane2.jlblColumnRating.setForeground(clrGreen);
			else if (dColumnRating > 1.0 && dColumnRating <= dYellowRating)
				contentPane2.jlblColumnRating.setForeground(clrYellow);
			else if (dColumnRating > dYellowRating)
				contentPane2.jlblColumnRating.setForeground(clrRed);
				
			
			contentPane2.jSliderIndicator.setEnabled(true);
			contentPane2.jSliderIndicator.setYellowLimit((float)dYellowRating);
			contentPane2.jSliderIndicator.setPosition((float)(dColumnRating / 3.0) * 100);
		}
		
		/*if (iTotalCompounds == 0)
			contentPane2.jlblAverageError.setText("-");
		else
		{
			double dAbsoluteAverage = dSumAbsolute / (double)iTotalCompounds;
			contentPane2.jlblAverageError.setText(formatter1.format(dAbsoluteAverage * 60));
		}

		if (iTotalCompounds == 0)
			contentPane2.jlblMedianError.setText("-");
		else
		{
			Arrays.sort(dErrorList);
			double dMedianError = dErrorList[dErrorList.length - (int)Math.ceil((double)iTotalCompounds / 2.0)];
			contentPane2.jlblMedianError.setText(formatter1.format(dMedianError * 60));
		}*/	
	}


    
	   public double calcRetentionError(double dtstep, int iNumCompoundsToInclude, boolean bUseSimpleGradient)
	    {
		  double dRetentionError = 0;
			double dt0;
			double dIntegral;
			double dtRFinal;
			double dD;
			double dTotalTime;
			double dTotalDeadTime;
			double dXPosition;
			double[] dLastXPosition = {0,0};
			double[] dLastko = {0,0};
			double dXMovement = 0;
			double dPhiC = 0;
			double dCurVal = 0;
			boolean bIsEluted;

			for (int iCompound = 0; iCompound < iNumCompoundsToInclude; iCompound++)
			{
				dIntegral = 0;
				dtRFinal = 0;
				dD = 0;
				dTotalTime = 0;
				dTotalDeadTime = 0;
				dXPosition = 0;
				dLastXPosition[0] = 0;
				dLastXPosition[1] = 0;
				dLastko[0] = 0;
				dLastko[1] = 0;
				bIsEluted = false;
				
				for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += dtstep)
				{
					if (bUseSimpleGradient)
						dPhiC = m_InterpolatedSimpleGradient.getAt(dTotalTime - dIntegral) / 100;
					else
						dPhiC = (m_InterpolatedSimpleGradient.getAt(dTotalTime - dIntegral) + m_InterpolatedGradientDifferenceProfile.getAt(dTotalTime - dIntegral)) / 100;

					dCurVal = dtstep / (Math.pow(10, m_StandardIsocraticDataInterpolated[iCompound].getAt(dPhiC)));
					dt0 = (m_InitialInterpolatedDeadTimeProfile.getAt(dPhiC) + m_InterpolatedDeadTimeDifferenceProfile.getAt(dPhiC)) / 60;
					dXMovement = dCurVal / dt0;
					
					if (dXPosition >= 1)
					{
						// ((1 - lastx)/(x - lastx)) gives fraction of the last step that should be considered
						// multiply that by (dTotalDeadTime - dLastXPosition[1]) to get the fraction of time in the last step that should be considered.
						// add that to dLastXPosition[1] to get the total dead time
						// dD is the total dead time at time of elution
						dD = ((1 - dLastXPosition[0])/(dXPosition - dLastXPosition[0])) * (dTotalDeadTime - dLastXPosition[1]) + dLastXPosition[1]; 
					}
					else
					{
						dLastXPosition[0] = dXPosition;
						dLastXPosition[1] = dTotalDeadTime;
					}
					
					dTotalDeadTime += dXMovement * dt0;
					
					if (dXPosition >= 1)
					{
						dtRFinal = ((dD - dLastko[0])/(dIntegral - dLastko[0]))*(dTotalTime - dLastko[1]) + dLastko[1];
					}
					else
					{
						dLastko[0] = dIntegral;
						dLastko[1] = dTotalTime;
					}
					
					dTotalTime += dtstep + dCurVal;
					dIntegral += dCurVal;
					
					if (dXPosition > 1 && bIsEluted == false)
					{
						bIsEluted = true;
						break;
					}
					
					dXPosition += dXMovement;
				}
				
				if (bIsEluted)
				{
					double errorRoot = dtRFinal - (Double)m_vectCalCompounds.get(iCompound)[1];
					dRetentionError += errorRoot*errorRoot;
					m_vectCalCompounds.get(iCompound)[2] = dtRFinal;
				}
				else
				{
					double errorRoot = (Double)m_vectCalCompounds.get(iCompound)[1];
					dRetentionError += errorRoot;
					m_vectCalCompounds.get(iCompound)[2] = (double)-1.0;
				}
			}
			
	    	return dRetentionError;
	    }

    public void updateTime(long starttime)
    {
		NumberFormat timeformatter = new DecimalFormat("00");
		
		long currentTime = System.currentTimeMillis();
		long lNumSecondsPassed = (currentTime - starttime) / 1000;
		long lNumDaysPassed = lNumSecondsPassed / (24 * 60 * 60);
		lNumSecondsPassed -= lNumDaysPassed * (24 * 60 * 60);
		long lNumHoursPassed = lNumSecondsPassed / (60 * 60);
		lNumSecondsPassed -= lNumHoursPassed * (60 * 60);
		long lNumMinutesPassed = lNumSecondsPassed / (60);
		lNumSecondsPassed -= lNumMinutesPassed * (60);
		
		String strProgress2 = "";
		strProgress2 += timeformatter.format(lNumHoursPassed) + ":" + timeformatter.format(lNumMinutesPassed) + ":" + timeformatter.format(lNumSecondsPassed);
		contentPane2.jlblTimeElapsed.setText(strProgress2);
    }
    
    public double calcAngleDifferenceDeadTime(int iIndex)
    {
    	double dTotalAngleError = 0;
    	double dHoldUpRange = this.m_dInitialDeadTimeArray[0][1];
    	
    	for (int i = 0; i < this.m_dDeadTimeDifferenceArray.length; i++)
    	{
        	if (i < 2)
        		continue;
        	
        	double dTime2 = this.m_dDeadTimeDifferenceArray[i][0];
        	double dHoldUp2 = this.m_dDeadTimeDifferenceArray[i][1];
        	double dTime1 = this.m_dDeadTimeDifferenceArray[i - 1][0];
        	double dHoldUp1 = this.m_dDeadTimeDifferenceArray[i - 1][1];
        	double dTime0 = this.m_dDeadTimeDifferenceArray[i - 2][0];
        	double dHoldUp0 = this.m_dDeadTimeDifferenceArray[i - 2][1];
        	
        	// First determine angle of previous segment
    		double dPreviousAdjacent = (dHoldUp1 - dHoldUp0) / dHoldUpRange;
    		double dPreviousOpposite = dTime1 - dTime0;
    		double dPreviousAngle;
    		if (dPreviousAdjacent == 0)
    			dPreviousAngle = Math.PI / 2; // 90 degrees
    		else
    			dPreviousAngle = Math.atan(dPreviousOpposite / dPreviousAdjacent);
    		
    		if (dPreviousAngle < 0)
    			dPreviousAngle = Math.PI + dPreviousAngle;
    		
    		double dAdjacent = (dHoldUp2 - dHoldUp1) / dHoldUpRange;
    		double dOpposite = dTime2 - dTime1;
    		double dNewAngle;
    		if (dAdjacent == 0)
    			dNewAngle = Math.PI / 2; // 90 degrees
    		else
    			dNewAngle = Math.atan(dOpposite / dAdjacent);
    		
    		if (dNewAngle < 0)
    			dNewAngle = Math.PI + dNewAngle;
    		
    		double angleErrorRoot = (Math.abs(dNewAngle - dPreviousAngle) / (Math.PI));
    		double dAngleError = angleErrorRoot*angleErrorRoot;
    		dTotalAngleError += dAngleError;
    	}
    	
     	return dTotalAngleError + 1;
    }
    
    public double calcAngleDifferenceGradient(int iIndex)
    {
    	//if (true)
    	//return 0;
    	double dTotalAngleError = 0;
    	double dMaxRampRate = 50;
    	
    	for (int i = 0; i < this.m_dGradientProfileDifferenceArray.length; i++)
    	{
        	if (i < 2)
        		continue;
        	
        	double dTime2 = this.m_dGradientProfileDifferenceArray[i][0];
        	double dTemp2 = this.m_dGradientProfileDifferenceArray[i][1];
        	double dTime1 = this.m_dGradientProfileDifferenceArray[i - 1][0];
        	double dTemp1 = this.m_dGradientProfileDifferenceArray[i - 1][1];
        	double dTime0 = this.m_dGradientProfileDifferenceArray[i - 2][0];
        	double dTemp0 = this.m_dGradientProfileDifferenceArray[i - 2][1];
        	
        	// Check if the previous point is a corner
        	// If it is, then don't worry about the angle - return 0
    		/*boolean bIsCorner = false;
    		
        	for (int j = 0; j < this.m_dIdealGradientProfileArray.length - 1; j++)
    		{
    			if (this.m_dIdealGradientProfileArray[j][0] == dTime1)
    			{
    				bIsCorner = true;
    				break;
    			}
    		}
        	
        	if (bIsCorner)
        		continue;*/

    	   	// First determine angle of previous segment
    		double dPreviousAdjacent = (dTemp1 - dTemp0) / dMaxRampRate;
    		double dPreviousOpposite = dTime1 - dTime0;
    		double dPreviousAngle;
    		if (dPreviousAdjacent == 0)
    			dPreviousAngle = Math.PI / 2; // 90 degrees
    		else
    			dPreviousAngle = Math.atan(dPreviousOpposite / dPreviousAdjacent);
    		
    		if (dPreviousAngle < 0)
    			dPreviousAngle = Math.PI + dPreviousAngle;
    		
    		double dAdjacent = (dTemp2 - dTemp1) / dMaxRampRate;
    		double dOpposite = dTime2 - dTime1;
    		double dNewAngle;
    		if (dAdjacent == 0)
    			dNewAngle = Math.PI / 2; // 90 degrees
    		else
    			dNewAngle = Math.atan(dOpposite / dAdjacent);
    		
    		if (dNewAngle < 0)
    			dNewAngle = Math.PI + dNewAngle;
    		
    		double angleErrorRoot = (Math.abs(dNewAngle - dPreviousAngle) / (Math.PI));
    		double dAngleError = angleErrorRoot*angleErrorRoot;
    		dTotalAngleError += dAngleError;
    	}
    	
     	return dTotalAngleError + 1;
    }
    
/*    public double calcAngleDifference3(int iIndex)
    {
    	if (iIndex <= 1)
    		return 1;
    	
    	double dMaxRampRate = 70;
    	
    	double dTime2 = this.m_dTemperatureProfileArray[iIndex][0];
    	double dTemp2 = this.m_dTemperatureProfileArray[iIndex][1];
    	double dTime1 = this.m_dTemperatureProfileArray[iIndex - 1][0];
    	double dTemp1 = this.m_dTemperatureProfileArray[iIndex - 1][1];
    	double dTime0 = this.m_dTemperatureProfileArray[iIndex - 2][0];
    	double dTemp0 = this.m_dTemperatureProfileArray[iIndex - 2][1];
    	
    	// Check if the previous point is a corner
    	// If it is, then don't worry about the angle - return 0
		for (int i = 0; i < this.m_dIdealTemperatureProfileArray.length - 1; i++)
		{
			if (this.m_dIdealTemperatureProfileArray[i][0] == dTime1)
				return 1;
		}
    	
    	// First determine angle of previous segment
		double dPreviousAdjacent = (dTemp1 - dTemp0) / dMaxRampRate;
		double dPreviousOpposite = dTime1 - dTime0;
		double dPreviousAngle;
		if (dPreviousAdjacent == 0)
			dPreviousAngle = Math.PI / 2; // 90 degrees
		else
			dPreviousAngle = Math.atan(dPreviousOpposite / dPreviousAdjacent);
		
		if (dPreviousAngle < 0)
			dPreviousAngle = Math.PI + dPreviousAngle;
		
		double dAdjacent = (dTemp2 - dTemp1) / dMaxRampRate;
		double dOpposite = dTime2 - dTime1;
		double dNewAngle;
		if (dAdjacent == 0)
			dNewAngle = Math.PI / 2; // 90 degrees
		else
			dNewAngle = Math.atan(dOpposite / dAdjacent);
		
		if (dNewAngle < 0)
			dNewAngle = Math.PI + dNewAngle;
		
		double dFactor1 = 1;
		double dAngleError = Math.pow((Math.abs(dNewAngle - dPreviousAngle) / (Math.PI)) * dFactor1, 2) + 1;
    	return dAngleError;
    }*/
    
 	/*public double goldenSectioningSearchSimpleGradientProfile(boolean bNonMixingVolume, double dStep, double dPrecision, double dMaxChangeAtOnce)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		
		double dLastVolumeGuess;
		
		if (bNonMixingVolume)
			dLastVolumeGuess = this.m_dNonMixingVolume;
		else
			dLastVolumeGuess = this.m_dMixingVolume;
		
		// Find bounds
		if (bNonMixingVolume)
			x1 = m_dNonMixingVolume;
		else
			x1 = m_dMixingVolume;
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
		
		x2 = x1 + dStep;
		if (bNonMixingVolume)
			m_dNonMixingVolume = x2;
		else
			m_dMixingVolume = x2;
		calculateSimpleGradient();
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
		
		if (dRetentionErrorX2 < dRetentionErrorX1)
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			
			x2 = ((x3 - x1) * this.m_dGoldenRatio) + x3;
			
			if (bNonMixingVolume)
				m_dNonMixingVolume = x2;
			else
				m_dMixingVolume = x2;
			calculateSimpleGradient();
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);

			while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastVolumeGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = ((x3 - x1) * this.m_dGoldenRatio) + x3;
				
				if (bNonMixingVolume)
					m_dNonMixingVolume = x2;
				else
					m_dMixingVolume = x2;
				calculateSimpleGradient();
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			if (bNonMixingVolume)
				m_dNonMixingVolume = x1;
			else
				m_dMixingVolume = x1;
			calculateSimpleGradient();
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);

			while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastVolumeGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				if (bNonMixingVolume)
					m_dNonMixingVolume = x1;
				else
					m_dMixingVolume = x1;
				calculateSimpleGradient();
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastVolumeGuess + dMaxChangeAtOnce)
		{
			if (bNonMixingVolume)
				m_dNonMixingVolume = dLastVolumeGuess + dMaxChangeAtOnce;
			else
				m_dMixingVolume = dLastVolumeGuess + dMaxChangeAtOnce;

			calculateSimpleGradient();
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);

			return dRetentionError;
		}
		
		if (x1 < dLastVolumeGuess - dMaxChangeAtOnce)
		{
			if (dLastVolumeGuess - dMaxChangeAtOnce < 0.00001)
			{
				if (bNonMixingVolume)
					m_dNonMixingVolume = 0.00001;
				else
					m_dMixingVolume = 0.00001;
			}
			else
			{
				if (bNonMixingVolume)
					m_dNonMixingVolume = dLastVolumeGuess - dMaxChangeAtOnce;
				else
					m_dMixingVolume = dLastVolumeGuess - dMaxChangeAtOnce;
			}
			
			calculateSimpleGradient();
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			
			return dRetentionError;
		}
		
		// Loop of optimization
		while ((x2 - x1) > dPrecision)
		{
			double x4;
			double dRetentionErrorX4;
			
			// Is the bigger gap between x3 and x2 or x3 and x1?
			if (x2 - x3 > x3 - x1) 
			{
				// x3 and x2, so x4 must be placed between them
				x4 = x3 + (2 - this.m_dGoldenRatio) * (x2 - x3);
			}
			else 
			{
				// x1 and x3, so x4 must be placed between them
				x4 = x3 - (2 - this.m_dGoldenRatio) * (x3 - x1);
			}

			if (bNonMixingVolume)
				m_dNonMixingVolume = x4;
			else
				m_dMixingVolume = x4;
			calculateSimpleGradient();
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			
			// Decide what to do next
			if (dRetentionErrorX4 < dRetentionErrorX3)
			{
				// Our new guess was better
				// Where did we put our last guess again?
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;							
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
				}
			}
			else
			{
				// Our new guess was worse
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x2 = x4;
					dRetentionErrorX2 = dRetentionErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x1 = x4;
					dRetentionErrorX1 = dRetentionErrorX4;							
				}
			}
		}
		
		// Restore profile to best value
		if (x3 < 0.00001)
		{
			// We can't have mixing and nonmixing volumes that are negative and they also can't be zero
			if (bNonMixingVolume)
				m_dNonMixingVolume = 0.00001;
			else
				m_dMixingVolume = 0.00001;
			calculateSimpleGradient();
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);			
		}
		else
		{
			if (bNonMixingVolume)
				m_dNonMixingVolume = x3;
			else
				m_dMixingVolume = x3;
			calculateSimpleGradient();
			dRetentionError = dRetentionErrorX3;			
		}
 		
 		return dRetentionError;
 	}*/
 	
 	public double goldenSectioningSearchSimpleGradientProfile(int iIndex, boolean bNonMixingVolume, double dStep, double dPrecision, double dMaxChangeAtOnce)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		
		double dLastVolumeGuess;
		
		if (bNonMixingVolume)
			dLastVolumeGuess = this.m_dNonMixingVolumeArray[iIndex][1];
		else
			dLastVolumeGuess = this.m_dMixingVolumeArray[iIndex][1];
		
		// Find bounds
		if (bNonMixingVolume)
			x1 = this.m_dNonMixingVolumeArray[iIndex][1];
		else
			x1 = this.m_dMixingVolumeArray[iIndex][1];
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
		
		x2 = x1 + dStep;
		if (bNonMixingVolume)
			this.m_dNonMixingVolumeArray[iIndex][1] = x2;
		else
			this.m_dMixingVolumeArray[iIndex][1] = x2;
		this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
		this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
		calculateSimpleGradient();
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
		
		if (dRetentionErrorX2 < dRetentionErrorX1)
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			
			x2 = ((x3 - x1) * this.m_dGoldenRatio) + x3;
			
			if (bNonMixingVolume)
				this.m_dNonMixingVolumeArray[iIndex][1] = x2;
			else
				this.m_dMixingVolumeArray[iIndex][1] = x2;
			this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);

			while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastVolumeGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = ((x3 - x1) * this.m_dGoldenRatio) + x3;
				
				if (bNonMixingVolume)
					this.m_dNonMixingVolumeArray[iIndex][1] = x2;
				else
					this.m_dMixingVolumeArray[iIndex][1] = x2;
				this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			if (bNonMixingVolume)
				this.m_dNonMixingVolumeArray[iIndex][1] = x1;
			else
				this.m_dMixingVolumeArray[iIndex][1] = x1;
			this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);

			while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastVolumeGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				if (bNonMixingVolume)
					this.m_dNonMixingVolumeArray[iIndex][1] = x1;
				else
					this.m_dMixingVolumeArray[iIndex][1] = x1;
				this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
				this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
				calculateSimpleGradient();
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastVolumeGuess + dMaxChangeAtOnce)
		{
			if (bNonMixingVolume)
				this.m_dNonMixingVolumeArray[iIndex][1] = dLastVolumeGuess + dMaxChangeAtOnce;
			else
				this.m_dMixingVolumeArray[iIndex][1] = dLastVolumeGuess + dMaxChangeAtOnce;
			this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);

			return dRetentionError;
		}
		
		if (x1 < dLastVolumeGuess - dMaxChangeAtOnce)
		{
			if (dLastVolumeGuess - dMaxChangeAtOnce < 0.00001)
			{
				if (bNonMixingVolume)
					this.m_dNonMixingVolumeArray[iIndex][1] = 0.00001;
				else
					this.m_dMixingVolumeArray[iIndex][1] = 0.00001;
			}
			else
			{
				if (bNonMixingVolume)
					this.m_dNonMixingVolumeArray[iIndex][1] = dLastVolumeGuess - dMaxChangeAtOnce;
				else
					this.m_dMixingVolumeArray[iIndex][1] = dLastVolumeGuess - dMaxChangeAtOnce;
			}
			this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			
			calculateSimpleGradient();
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			
			return dRetentionError;
		}
		
		// Loop of optimization
		while ((x2 - x1) > dPrecision)
		{
			double x4;
			double dRetentionErrorX4;
			
			// Is the bigger gap between x3 and x2 or x3 and x1?
			if (x2 - x3 > x3 - x1) 
			{
				// x3 and x2, so x4 must be placed between them
				x4 = x3 + (2 - this.m_dGoldenRatio) * (x2 - x3);
			}
			else 
			{
				// x1 and x3, so x4 must be placed between them
				x4 = x3 - (2 - this.m_dGoldenRatio) * (x3 - x1);
			}

			if (bNonMixingVolume)
				this.m_dNonMixingVolumeArray[iIndex][1] = x4;
			else
				this.m_dMixingVolumeArray[iIndex][1] = x4;
			this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);
			
			// Decide what to do next
			if (dRetentionErrorX4 < dRetentionErrorX3)
			{
				// Our new guess was better
				// Where did we put our last guess again?
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;							
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
				}
			}
			else
			{
				// Our new guess was worse
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x2 = x4;
					dRetentionErrorX2 = dRetentionErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x1 = x4;
					dRetentionErrorX1 = dRetentionErrorX4;							
				}
			}
		}
		
		// Restore profile to best value
		if (x3 < 0.00001)
		{
			// We can't have mixing and nonmixing volumes that are negative and they also can't be zero
			if (bNonMixingVolume)
				this.m_dNonMixingVolumeArray[iIndex][1] = 0.00001;
			else
				this.m_dMixingVolumeArray[iIndex][1] = 0.00001;
			this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), true);			
		}
		else
		{
			if (bNonMixingVolume)
				this.m_dNonMixingVolumeArray[iIndex][1] = x3;
			else
				this.m_dMixingVolumeArray[iIndex][1] = x3;
			this.m_InterpolatedMixingVolume = new LinearInterpolationFunction(m_dMixingVolumeArray);
			this.m_InterpolatedNonMixingVolume = new LinearInterpolationFunction(m_dNonMixingVolumeArray);
			calculateSimpleGradient();
			dRetentionError = dRetentionErrorX3;			
		}
 		
 		return dRetentionError;
 	}
 	
 	public double goldenSectioningSearchGradientProfile(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, double dAngleErrorMultiplier)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		double dAngleErrorX1;
		double dAngleErrorX2;
		double dAngleErrorX3;
		
		double dLastTempGuess = m_dGradientProfileDifferenceArray[iIndex][1];
		
		// Find bounds
		x1 = m_dGradientProfileDifferenceArray[iIndex][1];
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
		dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
		
		x2 = x1 + dStep;
		m_dGradientProfileDifferenceArray[iIndex][1] = x2;
		m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
		dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
		
		if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			dAngleErrorX3 = dAngleErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			m_dGradientProfileDifferenceArray[iIndex][1] = x2;
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
			

			while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastTempGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;
				dAngleErrorX1 = dAngleErrorX3;
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				m_dGradientProfileDifferenceArray[iIndex][1] = x2;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
				dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			dAngleErrorX3 = dAngleErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			m_dGradientProfileDifferenceArray[iIndex][1] = x1;
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);

			while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastTempGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;
				dAngleErrorX2 = dAngleErrorX3;
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				m_dGradientProfileDifferenceArray[iIndex][1] = x1;
				m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
				dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastTempGuess + dMaxChangeAtOnce)
		{
			if (dLastTempGuess + dMaxChangeAtOnce > 100)
				m_dGradientProfileDifferenceArray[iIndex][1] = 100;
			else
				m_dGradientProfileDifferenceArray[iIndex][1] = dLastTempGuess + dMaxChangeAtOnce;
			
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);

			return dRetentionError;
		}
		
		if (x1 < dLastTempGuess - dMaxChangeAtOnce)
		{
			if (dLastTempGuess - dMaxChangeAtOnce < -100)
				m_dGradientProfileDifferenceArray[iIndex][1] = -100;
			else
				m_dGradientProfileDifferenceArray[iIndex][1] = dLastTempGuess - dMaxChangeAtOnce;
			
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			
			return dRetentionError;
		}
		
		// Loop of optimization
		while ((x2 - x1) > dPrecision)
		{
			double x4;
			double dRetentionErrorX4;
			double dAngleErrorX4;
			
			// Is the bigger gap between x3 and x2 or x3 and x1?
			if (x2 - x3 > x3 - x1) 
			{
				// x3 and x2, so x4 must be placed between them
				x4 = x3 + (2 - this.m_dGoldenRatio) * (x2 - x3);
			}
			else 
			{
				// x1 and x3, so x4 must be placed between them
				x4 = x3 - (2 - this.m_dGoldenRatio) * (x3 - x1);
			}

			
			m_dGradientProfileDifferenceArray[iIndex][1] = x4;
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			dAngleErrorX4 = calcAngleDifferenceGradient(iIndex);
			
			// Decide what to do next
			if (dRetentionErrorX4 * Math.pow(dAngleErrorX4, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier))
			{
				// Our new guess was better
				// Where did we put our last guess again?
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
					dAngleErrorX3 = dAngleErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;							
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
					dAngleErrorX3 = dAngleErrorX4;
				}
			}
			else
			{
				// Our new guess was worse
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x2 = x4;
					dRetentionErrorX2 = dRetentionErrorX4;
					dAngleErrorX2 = dAngleErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x1 = x4;
					dRetentionErrorX1 = dRetentionErrorX4;							
					dAngleErrorX1 = dAngleErrorX4;
				}
			}
		}
		
		// Restore profile to best value
		if (x3 > 100)
		{
			m_dGradientProfileDifferenceArray[iIndex][1] = 100;
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);			
		}
		else if (x3 < -100)
		{
			m_dGradientProfileDifferenceArray[iIndex][1] = -100;
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);			
		}
		else
		{
			m_dGradientProfileDifferenceArray[iIndex][1] = x3;
			m_InterpolatedGradientDifferenceProfile = new LinearInterpolationFunction(m_dGradientProfileDifferenceArray);
			dRetentionError = dRetentionErrorX3;			
		}
 		
 		return dRetentionError;
 	}

 	public double goldenSectioningSearchDeadTime(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, double dAngleErrorMultiplier)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		double dAngleErrorX1;
		double dAngleErrorX2;
		double dAngleErrorX3;
		
		double dLastFGuess = this.m_dDeadTimeDifferenceArray[iIndex][1];

		// Find bounds
		x1 = this.m_dDeadTimeDifferenceArray[iIndex][1];
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
		dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);
		
		x2 = x1 + dStep;
		m_dDeadTimeDifferenceArray[iIndex][1] = x2;
		m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
		dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);
		
		if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			dAngleErrorX3 = dAngleErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			m_dDeadTimeDifferenceArray[iIndex][1] = x2;
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);

			while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastFGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;
				dAngleErrorX1 = dAngleErrorX3;
				
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				m_dDeadTimeDifferenceArray[iIndex][1] = x2;
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
				dAngleErrorX2 = calcAngleDifferenceDeadTime(iIndex);
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			dAngleErrorX3 = dAngleErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			m_dDeadTimeDifferenceArray[iIndex][1] = x1;
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);

			while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastFGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;
				dAngleErrorX2 = dAngleErrorX3;
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				m_dDeadTimeDifferenceArray[iIndex][1] = x1;
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
				dAngleErrorX1 = calcAngleDifferenceDeadTime(iIndex);
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastFGuess + dMaxChangeAtOnce)
		{
			m_dDeadTimeDifferenceArray[iIndex][1] = dLastFGuess + dMaxChangeAtOnce;
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			
			return dRetentionError;
		}
		
		if (x1 < dLastFGuess - dMaxChangeAtOnce)
		{
			m_dDeadTimeDifferenceArray[iIndex][1] = dLastFGuess - dMaxChangeAtOnce;
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);

			return dRetentionError;
		}
		
		// Loop of optimization
		while ((x2 - x1) > dPrecision)
		{
			double x4;
			double dRetentionErrorX4;
			double dAngleErrorX4;
			
			// Is the bigger gap between x3 and x2 or x3 and x1?
			if (x2 - x3 > x3 - x1) 
			{
				// x3 and x2, so x4 must be placed between them
				x4 = x3 + (2 - this.m_dGoldenRatio) * (x2 - x3);
			}
			else 
			{
				// x1 and x3, so x4 must be placed between them
				x4 = x3 - (2 - this.m_dGoldenRatio) * (x3 - x1);
			}
			
			m_dDeadTimeDifferenceArray[iIndex][1] = x4;
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			dAngleErrorX4 = calcAngleDifferenceDeadTime(iIndex);

			// Decide what to do next
			if (dRetentionErrorX4 * Math.pow(dAngleErrorX4, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier))
			{
				// Our new guess was better
				// Where did we put our last guess again?
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x1 = x3;
					dRetentionErrorX1 = dRetentionErrorX3;
					dAngleErrorX1 = dAngleErrorX3;
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
					dAngleErrorX3 = dAngleErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x2 = x3;
					dRetentionErrorX2 = dRetentionErrorX3;
					dAngleErrorX2 = dAngleErrorX3;
					x3 = x4;
					dRetentionErrorX3 = dRetentionErrorX4;
					dAngleErrorX3 = dAngleErrorX4;
				}
			}
			else
			{
				// Our new guess was worse
				if (x2 - x3 > x3 - x1) 
				{
					// x4 was in between x3 and x2
					x2 = x4;
					dRetentionErrorX2 = dRetentionErrorX4;
					dAngleErrorX2 = dAngleErrorX4;
				}
				else
				{
					// x4 was in between x1 and x3
					x1 = x4;
					dRetentionErrorX1 = dRetentionErrorX4;
					dAngleErrorX1 = dAngleErrorX4;
				}
			}
		}
		
		// Restore profile to best value
		m_dDeadTimeDifferenceArray[iIndex][1] = x3;
		m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
		dRetentionError = dRetentionErrorX3;
		
		return dRetentionError;
 	}
    
/* 	public double goldenSectioningSearchDeadTimeOffset(double dStep, double dPrecision, int iNumCompoundsToUse)
 	{
		double dRetentionError = 1;
 		double F1;
		double F2;
		double F3;
		double dRetentionErrorF1;
		double dRetentionErrorF2;
		double dRetentionErrorF3;
		
		// Find bounds
		// Get the first value
		F1 = m_dDeadTimeDifferenceArray[0][1];
		dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

		// Try a direction
		F2 = F1 + dStep;
		
		// Add that to the difference array
		double dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
		for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
		{
			m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
		}

		m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
		dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
		
		if (dRetentionErrorF2 < dRetentionErrorF1)
		{
			// We're going in the right direction
			F3 = F2;
			dRetentionErrorF3 = dRetentionErrorF2;
			
			F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
			
			dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

			while (dRetentionErrorF2 < dRetentionErrorF3)
			{
				F1 = F3;
				dRetentionErrorF1 = dRetentionErrorF3;
				F3 = F2;
				dRetentionErrorF3 = dRetentionErrorF2;
				
				F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
				
				dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			}
		}
		else
		{
			// We need to go in the opposite direction
			F3 = F1;
			dRetentionErrorF3 = dRetentionErrorF1;
			
			F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
			
			dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

			while (dRetentionErrorF1 < dRetentionErrorF3)
			{
				F2 = F3;
				dRetentionErrorF2 = dRetentionErrorF3;
				F3 = F1;
				dRetentionErrorF3 = dRetentionErrorF1;
				
				F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
				
				dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			}
		}
		
		// Loop of optimization
		while ((F2 - F1) > dPrecision)
		{
			double F4;
			double dRetentionErrorF4;
			// Is the bigger gap between F3 and F2 or F3 and F1?
			if (F2 - F3 > F3 - F1) 
			{
				// F3 and F2, so F4 must be placed between them
				F4 = F3 + (2 - this.m_dGoldenRatio) * (F2 - F3);
			}
			else 
			{
				// F1 and F3, so F4 must be placed between them
				F4 = F3 - (2 - this.m_dGoldenRatio) * (F3 - F1);
			}

			
			dPercentDiff = (F4 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF4 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			
			// Decide what to do next
			if (dRetentionErrorF4 < dRetentionErrorF3)
			{
				// Our new guess was better
				// Where did we put our last guess again?
				if (F2 - F3 > F3 - F1) 
				{
					// F4 was in between F3 and F2
					F1 = F3;
					dRetentionErrorF1 = dRetentionErrorF3;
					F3 = F4;
					dRetentionErrorF3 = dRetentionErrorF4;
				}
				else
				{
					// F4 was in between F1 and F3
					F2 = F3;
					dRetentionErrorF2 = dRetentionErrorF3;							
					F3 = F4;
					dRetentionErrorF3 = dRetentionErrorF4;
				}
			}
			else
			{
				// Our new guess was worse
				if (F2 - F3 > F3 - F1) 
				{
					// F4 was in between F3 and F2
					F2 = F4;
					dRetentionErrorF2 = dRetentionErrorF4;
				}
				else
				{
					// F4 was in between F1 and F3
					F1 = F4;
					dRetentionErrorF1 = dRetentionErrorF4;							
				}
			}
		}
		
		// Restore profile to best value
		dPercentDiff = (F3 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
		for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
		{
			m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
		}
		m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
		
		//dRetentionError = dRetentionErrorF3;
		
		// Calculate retention error with all of the standards
		dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);

		return dRetentionError;
 	}*/
 	
 	public double goldenSectioningSearchDeadTimeOffset(double dStep, double dPrecision, int iNumCompoundsToUse, double dMaxPercentChangeAtOnce)
 	{
		double dRetentionError = 1;
 		double F1;
		double F2;
		double F3;
		double dRetentionErrorF1;
		double dRetentionErrorF2;
		double dRetentionErrorF3;
		double dUpperBound = (m_dDeadTimeDifferenceArray[0][1] + m_dInitialDeadTimeArray[0][1]) * (1.0 + (dMaxPercentChangeAtOnce / 100.0));
		double dLowerBound = (m_dDeadTimeDifferenceArray[0][1] + m_dInitialDeadTimeArray[0][1]) * (1.0 - (dMaxPercentChangeAtOnce / 100.0));
		
		// Find bounds
		F1 = m_dDeadTimeDifferenceArray[0][1];
		dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

		F2 = F1 + dStep;
		
		double dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
		for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
		{
			m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
		}

		m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
		dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
		
		if (dRetentionErrorF2 < dRetentionErrorF1)
		{
			// We're going in the right direction
			F3 = F2;
			dRetentionErrorF3 = dRetentionErrorF2;
			
			F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
			
			dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

			while (dRetentionErrorF2 < dRetentionErrorF3 && F2 + this.m_dInitialDeadTimeArray[0][1] < dUpperBound)
			{
				F1 = F3;
				dRetentionErrorF1 = dRetentionErrorF3;
				F3 = F2;
				dRetentionErrorF3 = dRetentionErrorF2;
				
				F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
				
				dPercentDiff = (F2 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			}
		}
		else
		{
			// We need to go in the opposite direction
			F3 = F1;
			dRetentionErrorF3 = dRetentionErrorF1;
			
			F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
			
			dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);

			while (dRetentionErrorF1 < dRetentionErrorF3 && F1 + this.m_dInitialDeadTimeArray[0][1] > dLowerBound)
			{
				F2 = F3;
				dRetentionErrorF2 = dRetentionErrorF3;
				F3 = F1;
				dRetentionErrorF3 = dRetentionErrorF1;
				
				F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
				
				dPercentDiff = (F1 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
				for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
				{
					m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
				}
				m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
				dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (F2 + this.m_dInitialDeadTimeArray[0][1] > dUpperBound)
		{
			dPercentDiff = (dUpperBound - m_dInitialDeadTimeArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] = this.m_dInitialDeadTimeArray[i][1] * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);
			
			return dRetentionError;
		}
		
		if (F1 + this.m_dInitialDeadTimeArray[0][1] < dLowerBound)
		{
			dPercentDiff = (dLowerBound - m_dInitialDeadTimeArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] = this.m_dInitialDeadTimeArray[i][1] * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);

			return dRetentionError;
		}
		
		// Loop of optimization
		while ((F2 - F1) > dPrecision)
		{
			double F4;
			double dRetentionErrorF4;
			// Is the bigger gap between F3 and F2 or F3 and F1?
			if (F2 - F3 > F3 - F1) 
			{
				// F3 and F2, so F4 must be placed between them
				F4 = F3 + (2 - this.m_dGoldenRatio) * (F2 - F3);
			}
			else 
			{
				// F1 and F3, so F4 must be placed between them
				F4 = F3 - (2 - this.m_dGoldenRatio) * (F3 - F1);
			}

			
			dPercentDiff = (F4 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
			for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
			{
				m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
			}
			m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
			dRetentionErrorF4 = calcRetentionError(m_dtstep, iNumCompoundsToUse, false);
			
			// Decide what to do next
			if (dRetentionErrorF4 < dRetentionErrorF3)
			{
				// Our new guess was better
				// Where did we put our last guess again?
				if (F2 - F3 > F3 - F1) 
				{
					// F4 was in between F3 and F2
					F1 = F3;
					dRetentionErrorF1 = dRetentionErrorF3;
					F3 = F4;
					dRetentionErrorF3 = dRetentionErrorF4;
				}
				else
				{
					// F4 was in between F1 and F3
					F2 = F3;
					dRetentionErrorF2 = dRetentionErrorF3;							
					F3 = F4;
					dRetentionErrorF3 = dRetentionErrorF4;
				}
			}
			else
			{
				// Our new guess was worse
				if (F2 - F3 > F3 - F1) 
				{
					// F4 was in between F3 and F2
					F2 = F4;
					dRetentionErrorF2 = dRetentionErrorF4;
				}
				else
				{
					// F4 was in between F1 and F3
					F1 = F4;
					dRetentionErrorF1 = dRetentionErrorF4;							
				}
			}
		}
		
		// Restore profile to best value
		dPercentDiff = (F3 - m_dDeadTimeDifferenceArray[0][1]) / (this.m_dInitialDeadTimeArray[0][1] + m_dDeadTimeDifferenceArray[0][1]);
		for (int i = 0; i < m_dDeadTimeDifferenceArray.length; i++)
		{
			m_dDeadTimeDifferenceArray[i][1] += (this.m_dInitialDeadTimeArray[i][1] + m_dDeadTimeDifferenceArray[i][1]) * dPercentDiff;
		}
		m_InterpolatedDeadTimeDifferenceProfile = new InterpolationFunction(m_dDeadTimeDifferenceArray);
		
		//dRetentionError = dRetentionErrorF3;
		
		// Calculate retention error with all of the standards
		dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size(), false);

		return dRetentionError;
 	}
 	
    // Iterative - in sequence, *Golden* Sectioning Search algorithm
    // Start by optimizing the entire dead time error profile.
	public void backCalculate(Task task, boolean bDeadTimeProfileFirst)
	{
//		if (true)
//			return;
		long starttime = System.currentTimeMillis();
		m_bNoFullBackcalculation = false;
		
		// Phase I conditions:
		contentPane2.jProgressBar.setString("Determining gradient delay volume...");
		boolean bBackCalculateSimpleGradientProfile = true;
		boolean bBackCalculateDeadTimeProfileOffset = false;
		boolean bBackCalculateGradientProfile = false;
		boolean bBackCalculateDeadTimeProfile = false;
		double dGradientProfileAngleWeight = 0;
		double dDeadTimeProfileAngleWeight = 0;
		int iNumCompoundsToUseForDeadTimeOffset = this.m_vectCalCompounds.size(); // 2;
    	m_dtstep = m_dPlotXMax2 * 0.01;
		boolean bSkipTerminationTest = true;

		NumberFormat formatter1 = new DecimalFormat("#0.000000");
		NumberFormat formatter2 = new DecimalFormat("0.0000E0");
		NumberFormat formatter3 = new DecimalFormat("0.000");
		NumberFormat percentFormatter = new DecimalFormat("0.00");
		
		// Step #1: Create interpolating functions for the isocratic data of each gradient calibration solute
		m_StandardIsocraticDataInterpolated = new InterpolationFunction[contentPane2.tmOutputModel.getRowCount()];
		
		for (int i = 0; i < m_StandardIsocraticDataInterpolated.length; i++)
		{
			Integer iIndex = (Integer) m_vectCalCompounds.get(i)[0];
			m_StandardIsocraticDataInterpolated[i] = new InterpolationFunction(GlobalsDan.StandardIsocraticDataArray[iIndex]);
		}

		int iPhase = 1;
		int iIteration = 0;
		double dLastFullIterationError = 0;
		double dRetentionError = 0;
		
		while (true)
		{
			iIteration++;
			contentPane2.jlblIterationNumber.setText(((Integer)iIteration).toString());
			dLastFullIterationError = dRetentionError;
			
			if (bBackCalculateSimpleGradientProfile)
			{
				double dVolumeStep = .01; //in mL
				double dMaxChangeAtOnce = .05;
				double dPercentBPrecision = 0.0001;
				
				for (int i = 0; i < this.m_dNonMixingVolumeArray.length; i++)
				{
					dRetentionError = goldenSectioningSearchSimpleGradientProfile(i, true, dVolumeStep, dPercentBPrecision, dMaxChangeAtOnce);
				}
				
				for (int i = 0; i < this.m_dMixingVolumeArray.length; i++)
				{
					dRetentionError = goldenSectioningSearchSimpleGradientProfile(i, false, dVolumeStep, dPercentBPrecision, dMaxChangeAtOnce);
				}
				
				m_dVariance = dRetentionError / m_vectCalCompounds.size();

				updateTime(starttime);
				
				double dNum = dRetentionError / m_vectCalCompounds.size();
				String str;
				if (dNum < 0.0001)
					str = formatter2.format(dNum);
				else
					str = formatter1.format(dNum);
				
				contentPane2.jlblMixingVolume.setText(formatter3.format(this.m_dMixingVolumeArray[0][1]) + " mL");
				contentPane2.jlblNonMixingVolume.setText(formatter3.format(this.m_dNonMixingVolumeArray[0][1]) + " mL");
				
				contentPane2.jlblVariance.setText(str);
				
				calculateSimpleGradient();
				this.updateGraphs(true);

				if (task.isCancelled())
				{
					return;
				}
			}

			if (bBackCalculateDeadTimeProfileOffset)
			{
				double dDeadTimeStep = this.m_dInitialDeadTimeArray[0][1] / 1000;
				double dDeadTimePrecision = this.m_dInitialDeadTimeArray[0][1] / 100000;
				double dMaxPercentChangeAtOnce = 1;
				
				dRetentionError = goldenSectioningSearchDeadTimeOffset(dDeadTimeStep, dDeadTimePrecision, iNumCompoundsToUseForDeadTimeOffset, dMaxPercentChangeAtOnce);
				m_dVariance = dRetentionError / m_vectCalCompounds.size();
				
				updateTime(starttime);
				
				String str;
				double dNum = dRetentionError / m_vectCalCompounds.size();
				if (dNum < 0.0001)
					str = formatter2.format(dNum);
				else
					str = formatter1.format(dNum);
				
				contentPane2.jlblVariance.setText(str);
				
				calculateSimpleDeadTime();
				this.updateGraphs(true);
				
				if (task.isCancelled())
				{
					return;
				}
			}
			
			if (bBackCalculateDeadTimeProfile)
			{
				for (int iTimePoint = 0; iTimePoint < this.m_dDeadTimeDifferenceArray.length; iTimePoint++)
				{
					double dDeadTimeStep = this.m_dInitialDeadTimeArray[0][1] / 1000;
					double dDeadTimePrecision = this.m_dInitialDeadTimeArray[0][1] / 100000;
					double dMaxChangeAtOnce = this.m_dInitialDeadTimeArray[0][1] / 100;
					
					dRetentionError = goldenSectioningSearchDeadTime(iTimePoint, dDeadTimeStep, dDeadTimePrecision, dMaxChangeAtOnce, dDeadTimeProfileAngleWeight);
					m_dVariance = dRetentionError / m_vectCalCompounds.size();
					
					updateTime(starttime);
					
					String str;
					double dNum = dRetentionError / m_vectCalCompounds.size();
					if (dNum < 0.0001)
						str = formatter2.format(dNum);
					else
						str = formatter1.format(dNum);
					
					contentPane2.jlblVariance.setText(str);
					this.updateGraphs(true);

					if (task.isCancelled())
					{
						return;
					}
				}
			}
			
			if (bBackCalculateGradientProfile)
			{
				for (int iTimePoint = 0; iTimePoint < this.m_dGradientProfileDifferenceArray.length; iTimePoint++)
				{
					double dPercentBStep = .1;
					double dMaxChangeAtOnce = 2;
					double dPercentBPrecision = 0.001;
					
					dRetentionError = goldenSectioningSearchGradientProfile(iTimePoint, dPercentBStep, dPercentBPrecision, dMaxChangeAtOnce, dGradientProfileAngleWeight);
					m_dVariance = dRetentionError / m_vectCalCompounds.size();
					
					updateTime(starttime);
					
					String str;
					double dNum = dRetentionError / m_vectCalCompounds.size();
					if (dNum < 0.0001)
						str = formatter2.format(dNum);
					else
						str = formatter1.format(dNum);
					
					contentPane2.jlblVariance.setText(str);
					this.updateGraphs(true);

					if (task.isCancelled())
					{
						return;
					}
				}
			}
			
			String str;
			//double dNum = dRetentionError / m_vectCalCompounds.size();
			
			if (m_dVariance == 0)
				str = "";
			else if (m_dVariance < 0.0001)
				str = formatter2.format(m_dVariance);
			else
				str = formatter1.format(m_dVariance);
			
			contentPane2.jlblLastVariance.setText(str);
			
			// Calculate the percent improvement
			if (!bSkipTerminationTest)
			{
				double dPercentImprovement = (1 - (dRetentionError / dLastFullIterationError)) * 100;
				contentPane2.jlblPercentImprovement.setText(percentFormatter.format(dPercentImprovement) + "%");
				
				if (iPhase == 1)
				{
					if (dPercentImprovement < .1 && dPercentImprovement >= 0)
					{
						iPhase++;
						contentPane2.jlblPhase.setText("II");
						
						bBackCalculateSimpleGradientProfile = true;
						bBackCalculateDeadTimeProfileOffset = true;
						iNumCompoundsToUseForDeadTimeOffset = m_vectCalCompounds.size();
						bBackCalculateGradientProfile = false;
						bBackCalculateDeadTimeProfile = false;
						dGradientProfileAngleWeight = 1000d;
						dDeadTimeProfileAngleWeight = 100;
				    	m_dtstep = m_dPlotXMax2 * 0.01;
						bSkipTerminationTest = true;
						
						contentPane2.jProgressBar.setString("Fitting dead time...");
					}
				}
				else if (iPhase == 2)
				{
					if (dPercentImprovement < .1 && dPercentImprovement >= 0)
					{
						iPhase++;
						contentPane2.jlblPhase.setText("III");
						
						bBackCalculateSimpleGradientProfile = false;
						bBackCalculateDeadTimeProfileOffset = false;
						iNumCompoundsToUseForDeadTimeOffset = m_vectCalCompounds.size();
						bBackCalculateGradientProfile = true;
						bBackCalculateDeadTimeProfile = true;
						dGradientProfileAngleWeight = 1000d;
						dDeadTimeProfileAngleWeight = 100;
				    	m_dtstep = m_dPlotXMax2 * 0.01;
						bSkipTerminationTest = true;
						
						contentPane2.jProgressBar.setString("Fitting with angle constraints...");
						
						// Now place handles on the gradient - need to do this now instead of earlier because we need gradient delay to find true corners
						placeHandles();
						
						this.m_bShowSimpleDeadTime = true;
						this.m_bShowSimpleGradient = true;
					}
				}
				else if (iPhase == 3)
				{
					if (dPercentImprovement < 2 && dPercentImprovement >= 0)
					{
						iPhase++;
						contentPane2.jlblPhase.setText("IV");
						
						contentPane2.jProgressBar.setString("Fitting without angle constraints...");

						bBackCalculateSimpleGradientProfile = false;
						bBackCalculateDeadTimeProfileOffset = false;
						iNumCompoundsToUseForDeadTimeOffset = m_vectCalCompounds.size();
						bBackCalculateGradientProfile = true;
						bBackCalculateDeadTimeProfile = true;
						dGradientProfileAngleWeight = 0;
						dDeadTimeProfileAngleWeight = 0;	
						bSkipTerminationTest = true;
					}					
				}
				else if (iPhase == 4)
				{
					if (dPercentImprovement < 2 && dPercentImprovement >= 0)
					{
						iPhase++;
						contentPane2.jlblPhase.setText("V");
						
						contentPane2.jProgressBar.setString("Final optimization...");

						bBackCalculateSimpleGradientProfile = false;
						bBackCalculateDeadTimeProfileOffset = false;
						iNumCompoundsToUseForDeadTimeOffset = m_vectCalCompounds.size();
						bBackCalculateGradientProfile = true;
						bBackCalculateDeadTimeProfile = true;
						dGradientProfileAngleWeight = 0;
						dDeadTimeProfileAngleWeight = 0;	
						m_dtstep = m_dPlotXMax2 * 0.001;
						bSkipTerminationTest = true;
					}					
				}
				else if (iPhase >= 5)
				{
					if (dPercentImprovement < 1 && dPercentImprovement >= 0)
					{
						// Optimization is complete.
						break;
					}
				}
			}
			else
			{
				bSkipTerminationTest = false;
				contentPane2.jlblPercentImprovement.setText("");
			}
			
		}
		System.out.println(totalTime);
	}

	public void calculateSimpleDeadTime()
	{
		// Create new profile for the dead time offset
		int iNumPoints = 1000;
		double[][] dSimpleDeadTimeArray = new double[iNumPoints][2];
		for (int i = 0; i < iNumPoints; i++)
		{
	    	double dXPos = 0.9 * ((double)i / (double)(iNumPoints - 1)) + 0.05;
	    	dSimpleDeadTimeArray[i][0] = dXPos;
			dSimpleDeadTimeArray[i][1] = this.m_InitialInterpolatedDeadTimeProfile.getAt(dXPos) + this.m_InterpolatedDeadTimeDifferenceProfile.getAt(dXPos);
		}
		this.m_InterpolatedSimpleDeadTime = new LinearInterpolationFunction(dSimpleDeadTimeArray);
	}
	
	// New version handles change in mixing/nonmixing volume as a function of solvent composition
			public void calculateSimpleGradient()
			{
				//long n1 = System.currentTimeMillis();
				int iNumPoints = 1000;
				// Create an array for the simple gradient
				m_dSimpleGradientArray = new double[iNumPoints][2];
						
				// Initialize the solvent mixer composition to that of the initial solvent composition
				double dMixerComposition = ((Double) contentPane.tmGradientProgram.getValueAt(0, 1)).doubleValue();
				//double dFinalTime = (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) + ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iGradientTableLength - 1, 0)).doubleValue();
				double dFinalTime = this.m_dPlotXMax2; // in min
				double dTimeStep = dFinalTime / (iNumPoints - 1);
				
				// Start at time 0
				double dTime = 0;
				
				// Enter new values into the output array
				m_dSimpleGradientArray[0][0] = dTime;
				m_dSimpleGradientArray[0][1] = dMixerComposition;
				
				for (int i = 0; i < iNumPoints; i++)
				{
					// Find the current time
					dTime = i * dTimeStep;
					
					// Find the solvent composition coming into the nonmixing volume
					double dIncomingSolventCompositionToNonMixingVolume = this.m_InterpolatedIdealGradientProfile.getAt(dTime);

					// Now find the solvent composition coming into the mixing volume
					double dIncomingSolventCompositionToMixingVolume = 0;
					if (dTime < this.m_InterpolatedNonMixingVolume.getAt(dIncomingSolventCompositionToNonMixingVolume) / this.m_dFlowRate)
					{
						dIncomingSolventCompositionToMixingVolume = this.m_InterpolatedIdealGradientProfile.getAt(0);
					}
					else
					{
						double dTimeOfSolventComposition = dTime - (this.m_InterpolatedNonMixingVolume.getAt(dIncomingSolventCompositionToNonMixingVolume) / this.m_dFlowRate);
						dIncomingSolventCompositionToMixingVolume = this.m_InterpolatedIdealGradientProfile.getAt(dTimeOfSolventComposition);
					}
					
					// Figure out the volume of solvent B in the mixer
					double dSolventBInMixer = dMixerComposition * this.m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume);//m_dMixingVolume;
									
					// Now push out a step's worth of volume from the mixer
					dSolventBInMixer -= (m_dFlowRate * dTimeStep) * dMixerComposition;
					
					// dSolventBInMixer could be negative if the volume pushed out of the mixer is greater than the total volume of the mixer
					if (dSolventBInMixer < 0)
						dSolventBInMixer = 0;
					
					// Now add a step's worth of new volume from the pump
					// First, find which two data points we are between
					// Find the last data point that isn't greater than our current time
					
					if ((m_dFlowRate * dTimeStep) < this.m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume))
						dSolventBInMixer += (m_dFlowRate * dTimeStep) * dIncomingSolventCompositionToMixingVolume;
					else
					{
						// The amount of solvent entering the mixing chamber is larger than the mixing chamber. Just set the solvent composition in the mixer to that of the mobile phase.
						dSolventBInMixer = this.m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume) * dIncomingSolventCompositionToMixingVolume;
					}
					
					// Calculate the new solvent composition in the mixing volume
					if ((m_dFlowRate * dTimeStep) < this.m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume))
						dMixerComposition = dSolventBInMixer / this.m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume);
					else
						dMixerComposition = dIncomingSolventCompositionToMixingVolume;
					
					// Enter new values into the output array
					m_dSimpleGradientArray[i][0] = dTime - ((m_InterpolatedMixingVolume.getAt(dIncomingSolventCompositionToMixingVolume) - m_InterpolatedMixingVolume.getAt(0)) / this.m_dFlowRate);
					m_dSimpleGradientArray[i][1] = dMixerComposition;
				}
				m_InterpolatedSimpleGradient = new LinearInterpolationFunction(m_dSimpleGradientArray);
				
			}

 	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) 
 	{
		
	}

}
