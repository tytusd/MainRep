package org.retentionprediction;

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
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.SwingWorker;

import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import boswell.peakfindergc.PeakFinderSettingsDialog;

import boswell.graphcontrol.AutoScaleEvent;
import boswell.graphcontrol.AutoScaleListener;

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
		return Globals.CompoundNameArray.length;
	}
	
	public boolean loadCompoundInfo(int iIndex)
	{
		iCompoundIndex = iIndex;
		
		strCompoundName = Globals.CompoundNameArray[iIndex];
		
		return true;
	}
}

public class GCRetentionPredictorApp extends JFrame implements ActionListener, KeyListener, FocusListener, ListSelectionListener, AutoScaleListener, TableModelListener, ClipboardOwner
{
	private static final long serialVersionUID = 1L;

	TopPanel contentPane = null;
	TopPanel2 contentPane2 = null;
	public JScrollPane jMainScrollPane = null;
	public JPanel jBackPanel = null;
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
	//public double m_dTransferLineTemperature = 320; // in C
	//public double m_dTransferLineLength = .34; // in m
	public int m_iNumPoints = 3000;
	public Vector<Object[]> m_vectCalCompounds = new Vector<Object[]>(); //{compound #, measured retention time, projected retention time};
    private Task task = null;
    private TaskPredict taskPredict = null;
    public int m_iStage = 1;
    public double m_dtstep = 0.01;
    public double m_V0 = 1; // in mL
    
    public double m_dTmax = 1000;
    public double m_dk = 100;
    
    // Calibration error equation is Tactual-Tnominal = mx + b
    public double m_dCalibrationErrorSlope = 0;
    public double m_dCalibrationErrorIntercept = 0;
    
    public double[][] m_dIdealTemperatureProfileArray;
    public LinearInterpolationFunction m_InterpolatedIdealTempProfile; //Linear
    
    public double[][] m_dTemperatureProfileDifferenceArray;
    public LinearInterpolationFunction m_InterpolatedTemperatureDifferenceProfile; // Linear
    
    public double[][] m_dSimpleTemperatureProfileArray;
    public LinearInterpolationFunction m_InterpolatedSimpleTemperatureProfile; // Linear
    
    public double[][] m_dHoldUpArray;
    public InterpolationFunction m_InitialInterpolatedHoldUpVsTempProfile;

    //public InterpolationFunction m_InterpolatedTm2ToTmTotVsTemperature; // Gives the fraction of total hold-up time that is from the transfer line as a function of T.
    public InterpolationFunction m_InterpolatedHoldUpProfile;
    //public InterpolationFunction[] m_AlkaneIsothermalDataInterpolated;
    //public InterpolationFunction[] m_CompoundIsothermalDataInterpolated;
    public InterpolationFunction m_Vm;
	
    //public double[][][] m_dHoldUpArrayStore;
    //public double[][][] m_dTemperatureArrayStore;
    //public double[] m_dRetentionErrorStore;

    public int m_iInterpolatedTempProgramSeries = 0;
    public int m_iTempProgramMarkerSeries = 0;
    public int m_iInterpolatedHoldUpSeries = 0;
    public int m_iHoldUpMarkerSeries = 0;
    
    public double m_dPlotXMax = 0;
    public double m_dPlotXMax2 = 0;
    
    public boolean m_bDoNotChangeTable = false;
    public final double m_dGoldenRatio = (1 + Math.sqrt(5)) / 2;

    public double m_dVariance = 0;
    
    public String m_strFileName = "";
    
    public double[] m_dExpectedErrorArray;
    public PredictedRetentionObject[] m_PredictedRetentionTimes;
    public double m_dConfidenceInterval = 0.99;
    
	public Vector<Compound> m_vectCompound = new Vector<Compound>();

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
        GCRetentionPredictorApp frame;
		if (!Globals.CHECKYOURGC)
			frame = new GCRetentionPredictorApp("GC Retention Predictor"); // create frame with title
		else
			frame = new GCRetentionPredictorApp("Check Your GC"); // create frame with title
        
		//java.net.URL url1 = ClassLoader.getSystemResource("org/hplcsimulator/images/icon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
	    Image img;
		if (!Globals.CHECKYOURGC)
			img = kit.createImage(frame.getClass().getResource("/org/retentionprediction/images/gc_retention_predictor_icon.png"));
		else
			img = kit.createImage(frame.getClass().getResource("/org/retentionprediction/images/check_your_gc_icon.png"));			
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
	public GCRetentionPredictorApp(String str) 
	{
	    super(str);
	    
		this.setPreferredSize(new Dimension(943, 730));
	}
	
	/**
	 * This is the xxx default constructor
	 */
	public GCRetentionPredictorApp() 
	{
	    super();
	    
		/*try {
	        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }*/

		this.setPreferredSize(new Dimension(1000, 700));
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() 
	{
        // Load the JavaHelp
		String helpHS = "org/retentionprediction/help/RetentionPredictorHelp.hs";

		//Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
        //    SwingUtilities.invokeAndWait(new Runnable() 
        //    {
        //        public void run() {
                	createGUI();
        //        }
        //    });
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

        contentPane.jtxtInitialTemperature.addFocusListener(this);
        contentPane.jtxtInitialTemperature.addKeyListener(this);
        contentPane.jtxtInitialTime.addFocusListener(this);
        contentPane.jtxtInitialTime.addKeyListener(this);
        contentPane.jtxtColumnLength.addFocusListener(this);
        contentPane.jtxtColumnLength.addKeyListener(this);
        contentPane.jtxtInnerDiameter.addFocusListener(this);
        contentPane.jtxtInnerDiameter.addKeyListener(this);
        contentPane.jtxtFilmThickness.addFocusListener(this);
        contentPane.jtxtFilmThickness.addKeyListener(this);
        contentPane.jtxtFlowRate.addFocusListener(this);
        contentPane.jtxtFlowRate.addKeyListener(this);
        contentPane.jtxtPressure.addFocusListener(this);
        contentPane.jtxtPressure.addKeyListener(this);
        contentPane.jtxtOtherPressure.addFocusListener(this);
        contentPane.jtxtOtherPressure.addKeyListener(this);
        contentPane.jrdoConstantFlowRate.addActionListener(this);
        contentPane.jrdoConstantPressure.addActionListener(this);
        contentPane.jrdoOtherPressure.addActionListener(this);
        contentPane.jrdoVacuum.addActionListener(this);
        contentPane.jbtnHelp.addActionListener(this);
        contentPane.jbtnInsertRow.addActionListener(this);
        contentPane.jbtnRemoveRow.addActionListener(this);
        //contentPane.jtxtTransferLineTemperature.addFocusListener(this);
        //contentPane.jtxtTransferLineTemperature.addKeyListener(this);
        //contentPane.jtxtTransferLineLength.addFocusListener(this);
        //contentPane.jtxtTransferLineLength.addKeyListener(this);
       
        contentPane.m_GraphControlTemp.addAutoScaleListener(this);
        contentPane.m_GraphControlHoldUp.addAutoScaleListener(this);
        contentPane.tmTemperatureProgram.addTableModelListener(this);
        contentPane.tmMeasuredRetentionTimes.addTableModelListener(this);
        contentPane.jbtnNextStep.addActionListener(this);
        contentPane.jbtnPreloadedValues.addActionListener(this);
        contentPane.jbtnPeakFinder.addActionListener(this);

        contentPane.m_GraphControlTemp.setYAxisTitle("Column Temperature");
        contentPane.m_GraphControlTemp.setYAxisBaseUnit("\u00B0C", "\u00B0C");
        contentPane.m_GraphControlTemp.setYAxisRangeLimits(0, 105);
        contentPane.m_GraphControlTemp.setYAxisRangeIndicatorsVisible(true);
        contentPane.m_GraphControlTemp.setXAxisRangeIndicatorsVisible(false);
        contentPane.m_GraphControlTemp.setAutoScaleY(true);
        //contentPane.m_GraphControl.setVisibleWindow(0, 300, 0, 110);
        contentPane.m_GraphControlTemp.repaint();

        contentPane.m_GraphControlHoldUp.setYAxisTitle("Hold-up Time");
        contentPane.m_GraphControlHoldUp.setYAxisBaseUnit("seconds", "s");
        contentPane.m_GraphControlHoldUp.setYAxisRangeLimits(0, 10000);
        contentPane.m_GraphControlHoldUp.setYAxisRangeIndicatorsVisible(true);
        contentPane.m_GraphControlHoldUp.setXAxisRangeIndicatorsVisible(false);
        contentPane.m_GraphControlHoldUp.setAutoScaleY(true);
        //contentPane.m_GraphControlFlow.setVisibleWindow(0, 300, 0.195 / 1000, 0.205 / 1000);
        contentPane.m_GraphControlHoldUp.repaint();
        
        //Create and set up the second content pane
        contentPane2 = new TopPanel2();
        contentPane2.setOpaque(true); 
        contentPane2.jpanelStep6.setVisible(false);
        
        contentPane2.jbtnCalculate.addActionListener(this);
        contentPane2.jbtnNextStep.addActionListener(this);
        contentPane2.jbtnPreviousStep.addActionListener(this);
        contentPane2.jbtnPredict.addActionListener(this);
        contentPane2.jbtnHelp.addActionListener(this);
        
        contentPane2.jtxtWindowConfidence.addFocusListener(this);
        contentPane2.jtxtWindowConfidence.addKeyListener(this);

        contentPane2.m_GraphControlTemp.setYAxisTitle("Column Temperature");
        contentPane2.m_GraphControlTemp.setYAxisBaseUnit("\u00b0C", "\u00b0C");
        contentPane2.m_GraphControlTemp.setYAxisRangeLimits(0, 105);
        contentPane2.m_GraphControlTemp.setYAxisRangeIndicatorsVisible(true);
        contentPane2.m_GraphControlTemp.setXAxisRangeIndicatorsVisible(false);
        contentPane2.m_GraphControlTemp.setAutoScaleY(true);
        //contentPane.m_GraphControl.setVisibleWindow(0, 300, 0, 110);
        contentPane2.m_GraphControlTemp.repaint();

        contentPane2.m_GraphControlHoldUp.setYAxisTitle("Hold-up Time");
        contentPane2.m_GraphControlHoldUp.setYAxisBaseUnit("seconds", "s");
        contentPane2.m_GraphControlHoldUp.setYAxisRangeLimits(0, 100);
        contentPane2.m_GraphControlHoldUp.setYAxisRangeIndicatorsVisible(true);
        contentPane2.m_GraphControlHoldUp.setAutoScaleY(true);
        contentPane2.m_GraphControlHoldUp.setXAxisType(false);
        contentPane2.m_GraphControlHoldUp.setXAxisBaseUnit("\u00b0C", "\u00b0C");
        contentPane2.m_GraphControlHoldUp.setXAxisRangeLimits(0, 1000);
        contentPane2.m_GraphControlHoldUp.setXAxisTitle("Temperature");
        contentPane2.m_GraphControlHoldUp.setXAxisRangeIndicatorsVisible(false);

        contentPane2.m_tmTestCompoundsModel.addTableModelListener(this);
        contentPane2.jbtnAutomaticDetermineTestCompounds.addActionListener(this);
        
        //contentPane.m_GraphControlFlow.setVisibleWindow(0, 300, 0.195 / 1000, 0.205 / 1000);
        contentPane2.m_GraphControlHoldUp.repaint();       
    }

    private void validateInitialTemperature()
    {
    	if (contentPane.jtxtInitialTemperature.getText().length() == 0)
    		contentPane.jtxtInitialTemperature.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jtxtInitialTemperature.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 500)
			dTemp = 500;
		
		if (contentPane.tmTemperatureProgram.getRowCount() > 0)
		{
			double dFirstTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(0, 1);
			if (dTemp > dFirstTemp)
				dTemp = dFirstTemp;
		}
		
		this.m_dInitialTemperature = dTemp;
		contentPane.jtxtInitialTemperature.setText(Float.toString((float)dTemp));    	
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
    
/*    private void validateTransferLineTemperature()
    {
		double dTemp = (double)Float.valueOf(contentPane.jtxtTransferLineTemperature.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 500)
			dTemp = 500;
		
		this.m_dTransferLineTemperature = dTemp;
		contentPane.jtxtTransferLineTemperature.setText(Float.toString((float)dTemp));    	
    }*/
    
/*    private void validateTransferLineLength()
    {
		double dTemp = (double)Float.valueOf(contentPane.jtxtTransferLineLength.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 99999)
			dTemp = 99999;
		
		this.m_dTransferLineLength = dTemp / 100; // put it in m
		contentPane.jtxtTransferLineLength.setText(Float.toString((float)dTemp));    	
    }*/
    
    private void validateInitialTime()
    {
    	if (contentPane.jtxtInitialTime.getText().length() == 0)
    		contentPane.jtxtInitialTime.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jtxtInitialTime.getText());
		
		if (dTemp < 0)
			dTemp = 0;
		if (dTemp > 1000)
			dTemp = 1000;
		
		this.m_dInitialTime = dTemp;
		contentPane.jtxtInitialTime.setText(Float.toString((float)dTemp));    	
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
    
    private void validateInnerDiameter()
    {
    	if (contentPane.jtxtInnerDiameter.getText().length() == 0)
    		contentPane.jtxtInnerDiameter.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jtxtInnerDiameter.getText());
		
		if (dTemp < 0.00001)
			dTemp = 0.00001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		if (dTemp - (2 * this.m_dFilmThickness / 1000) > 0)
			this.m_dInnerDiameter = dTemp;
		else
			this.m_dInnerDiameter = ((this.m_dFilmThickness * 2) / 1000) - 0.0001;

		contentPane.jtxtInnerDiameter.setText(Float.toString((float)m_dInnerDiameter));    	
    }
    
    private void validateFilmThickness()
    {
    	if (contentPane.jtxtFilmThickness.getText().length() == 0)
    		contentPane.jtxtFilmThickness.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jtxtFilmThickness.getText());
		
		if (dTemp < 0.00001)
			dTemp = 0.00001;
		if (dTemp > 10000)
			dTemp = 10000;
		
		if (this.m_dInnerDiameter - (2 * dTemp / 1000) > 0)
			this.m_dFilmThickness = dTemp;
		else
			this.m_dFilmThickness = ((this.m_dInnerDiameter / 2) * 1000) - 0.0001;
		
		contentPane.jtxtFilmThickness.setText(Float.toString((float)m_dFilmThickness));    	
    }
    
    private void validateOtherOutletPressure()
    {
    	if (contentPane.jtxtOtherPressure.getText().length() == 0)
    		contentPane.jtxtOtherPressure.setText("0");

    	if (contentPane.jrdoOtherPressure.isSelected() == false)
    		return;

		double dTemp = (double)Float.valueOf(contentPane.jtxtOtherPressure.getText());
		
		if (dTemp < 0.000001)
			dTemp = 0.000001;
		if (dTemp > 101.325)
			dTemp = 101.325;
		
		this.m_dOutletPressure = dTemp;
		contentPane.jtxtOtherPressure.setText(Float.toString((float)m_dOutletPressure));    	
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
    }

    private void validateInletPressure()
    {
    	if (contentPane.jtxtPressure.getText().length() == 0)
    		contentPane.jtxtPressure.setText("0");

    	double dTemp = (double)Float.valueOf(contentPane.jtxtPressure.getText());
		
		if (dTemp < 0.000000001)
			dTemp = 0.000000001;
		if (dTemp > 100000)
			dTemp = 100000;
	
		this.m_dInletPressure = dTemp;			
		contentPane.jtxtPressure.setText(Float.toString((float)m_dInletPressure));    	
    }
    
    public void nextStepButtonPressed()
    {
    	// Check if the cell editor is currently open in the table
    	if (this.contentPane.jtableMeasuredRetentionTimes.isEditing())
    	{
    		this.contentPane.jtableMeasuredRetentionTimes.getCellEditor().stopCellEditing();
    	}

    	if (this.contentPane2.jtableTestCompoundPredictions.isEditing())
    	{
    		this.contentPane2.jtableTestCompoundPredictions.getCellEditor().stopCellEditing();
    	}

    	// Check for moderate gas decompression in constant flow rate mode
    	
    	if (this.contentPane.jrdoConstantFlowRate.isSelected())
    	{
    		// TODO: warn of moderate gas decompression
    		//for (int i = 0; i < this.m_if (this.m_dOutletPressure)
    	}
    	
    		
    	// Set the graph and flow profiles to contain the correct data
    	m_iStage = 2;

		NumberFormat formatter = new DecimalFormat("#0.0000");

    	// Set initial control values
    	contentPane2.jbtnCalculate.setText("Back-Calculate Profiles");
    	contentPane2.jbtnCalculate.setEnabled(true);
    	contentPane2.jlblIterationNumber.setText("1");
    	contentPane2.jlblLastVariance.setText("");
    	contentPane2.jlblPercentImprovement.setText("");
    	contentPane2.jlblPhase.setText("I");
    	contentPane2.jlblTimeElapsed.setText("");
    	contentPane2.jlblVariance.setText("");
    	contentPane2.jProgressBar.setString("");
    	contentPane2.jProgressBar.setIndeterminate(false);
    	contentPane2.jbtnNextStep.setEnabled(false);
    	contentPane2.m_GraphControlTemp.RemoveAllSeries();
    	contentPane2.m_GraphControlHoldUp.RemoveAllSeries();
        contentPane2.jpanelStep4.setVisible(true);
        contentPane2.jpanelStep5.setVisible(false);
        contentPane2.jpanelStep6.setVisible(false);
		
    	// Set the table to contain the correct data
    	contentPane2.tmOutputModel.getDataVector().clear();
    	m_vectCalCompounds.clear();
    	
    	for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
    	{
    		if ((Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3) <= (Double)0.0
    				|| (Boolean)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 0) == false)
    			continue;
    		
    		Object[] newRow = {contentPane.tmMeasuredRetentionTimes.getValueAt(i, 1), formatter.format(contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3)), null, null};

    		contentPane2.tmOutputModel.addRow(newRow);
    		
			Object[] newSolute = {i, contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3), (Double)0.0};
			m_vectCalCompounds.add(newSolute);
    	}
    	
		m_dPlotXMax2 = (((Double)m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1]));
    	
    	// Here is where we set the value of m_dtstep
    	m_dtstep = m_dPlotXMax2 * 0.001;

    	int iIdealPlotIndexTemp = contentPane2.m_GraphControlTemp.AddSeries("Ideal Temperature Program", new Color(0, 0, 0), 1, false, false);
    	int iIdealPlotIndexHoldUp = contentPane2.m_GraphControlHoldUp.AddSeries("Ideal Hold-up Time", new Color(0, 0, 0), 1, false, false);
    	
    	m_iInterpolatedTempProgramSeries = contentPane2.m_GraphControlTemp.AddSeries("Interpolated Temperature Program", new Color(255,0,0), 1, false, false);
	    m_iTempProgramMarkerSeries = contentPane2.m_GraphControlTemp.AddSeries("Temp Program Markers", new Color(255,0,0), 1, true, false);
	    
    	m_iInterpolatedHoldUpSeries = contentPane2.m_GraphControlHoldUp.AddSeries("Interpolated Hold-up", new Color(255,0,0), 1, false, false);
	    m_iHoldUpMarkerSeries = contentPane2.m_GraphControlHoldUp.AddSeries("Hold-up Markers", new Color(255,0,0), 1, true, false);

	    // Add in data points for the ideal temperature program series
    	this.m_dIdealTemperatureProfileArray = new double[(contentPane.tmTemperatureProgram.getRowCount() * 2) + 2][2];
    	int iPointCount = 0;

    	contentPane2.m_GraphControlTemp.AddDataPoint(iIdealPlotIndexTemp, 0, m_dInitialTemperature);
    	this.m_dIdealTemperatureProfileArray[iPointCount][0] = 0.0;
    	this.m_dIdealTemperatureProfileArray[iPointCount][1] = m_dInitialTemperature;
		iPointCount++;
		
		if (m_dInitialTime > 0)
		{
	    	contentPane2.m_GraphControlTemp.AddDataPoint(iIdealPlotIndexTemp, m_dInitialTime * 60, m_dInitialTemperature);
	    	this.m_dIdealTemperatureProfileArray[iPointCount][0] = m_dInitialTime;
	    	this.m_dIdealTemperatureProfileArray[iPointCount][1] = m_dInitialTemperature;
			iPointCount++;
		}

    	double dTotalTime = m_dInitialTime;
    	double dLastTemp = m_dInitialTemperature;
    	double dFinalTemp = m_dInitialTemperature;
    	
    	// Go through the temperature program table and create an array that contains temp vs. time
		for (int i = 0; i < contentPane.tmTemperatureProgram.getRowCount(); i++)
		{
			double dRamp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 0);
			dFinalTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 1);
			double dFinalTime = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 2);

			if (dRamp != 0)
			{
				dTotalTime += (dFinalTemp - dLastTemp) / dRamp;
				contentPane2.m_GraphControlTemp.AddDataPoint(iIdealPlotIndexTemp, dTotalTime * 60, dFinalTemp);
				this.m_dIdealTemperatureProfileArray[iPointCount][0] = dTotalTime;
				this.m_dIdealTemperatureProfileArray[iPointCount][1] = dFinalTemp;
				iPointCount++;
			}
			
			if (dFinalTime != 0)
			{
				if (i < contentPane.tmTemperatureProgram.getRowCount() - 1)
				{
					dTotalTime += dFinalTime;
					contentPane2.m_GraphControlTemp.AddDataPoint(iIdealPlotIndexTemp, dTotalTime * 60, dFinalTemp);				
					this.m_dIdealTemperatureProfileArray[iPointCount][0] = dTotalTime;
					this.m_dIdealTemperatureProfileArray[iPointCount][1] = dFinalTemp;
					iPointCount++;						
				}
			}
			
			dLastTemp = dFinalTemp;
		}
		
    	if (dTotalTime < m_dPlotXMax2)
    		this.m_dIdealTemperatureProfileArray[iPointCount][0] = m_dPlotXMax2;
    	else
    	{
    		this.m_dIdealTemperatureProfileArray[iPointCount][0] = dTotalTime + 1;
    		m_dPlotXMax2 = dTotalTime + 1;
    	}
    	this.m_dIdealTemperatureProfileArray[iPointCount][1] = dFinalTemp;
       	contentPane2.m_GraphControlTemp.AddDataPoint(iIdealPlotIndexTemp, m_dPlotXMax2 * 60, dFinalTemp);
        iPointCount++;

		// Ideal series finished
		// Now cut it down to the correct size
		double tempArray[][] = new double[iPointCount][2];
		for (int i = 0; i < iPointCount; i++)
		{
			tempArray[i][0] = this.m_dIdealTemperatureProfileArray[i][0];
			tempArray[i][1] = this.m_dIdealTemperatureProfileArray[i][1];
		}
		this.m_dIdealTemperatureProfileArray = tempArray;
		
		// Make the interpolated temperature profile
    	this.m_InterpolatedIdealTempProfile = new LinearInterpolationFunction(this.m_dIdealTemperatureProfileArray);

    	// Select number of data points for the gradient and flow profiles
		int iTotalDataPoints = m_vectCalCompounds.size();
		
		// 11/15ths of the data points should be on the gradient profile
		int iNumTempProgramDataPoints = (int)(((double)11/(double)15)*(double)iTotalDataPoints);
		int iNumFlowDataPoints = iTotalDataPoints - iNumTempProgramDataPoints;
		
		if (iNumFlowDataPoints < 3)
		{
			iNumFlowDataPoints = 3;
			iNumTempProgramDataPoints = iTotalDataPoints - iNumFlowDataPoints;
		}
		
		// Create initial gradient and flow rate arrays
		
		// First make an array with the correct number of data points.
		m_dTemperatureProfileDifferenceArray = new double [iNumTempProgramDataPoints][2];
		m_dHoldUpArray = new double [iNumFlowDataPoints][2];
		
		// Set the value of the first data point
		m_dTemperatureProfileDifferenceArray[0][0] = 0;
		m_dTemperatureProfileDifferenceArray[0][1] = 0;
		
		for (int i = 1; i < iNumTempProgramDataPoints - 1; i++)
		{
			// Find the two nearest alkanes
			double dAlkaneNum = ((double)i / ((double)iNumTempProgramDataPoints - 1)) * (double)iTotalDataPoints;
			double dOneGreater = (int)Math.ceil(dAlkaneNum);
			double dOneLesser = (int)Math.floor(dAlkaneNum);
			double dRtOneLesser = (Double)m_vectCalCompounds.get((int)dOneLesser)[1];
			double dRtOneGreater = (Double)m_vectCalCompounds.get((int)dOneGreater)[1];
		
			// Check to see if we landed exactly on an alkane
			if (dOneGreater == dOneLesser)
			{
				// If so, set the time of this point to the alkane we landed on
				m_dTemperatureProfileDifferenceArray[i][0] = dRtOneLesser;
			}
			else
			{
				// Otherwise, find the time of this point in between the surrounding alkanes
				double dPosition = ((dAlkaneNum - dOneLesser) / (dOneGreater - dOneLesser));
				m_dTemperatureProfileDifferenceArray[i][0] = (dPosition * dRtOneGreater) + ((1 - dPosition) * dRtOneLesser);
			}
			
			// Subtract half a hold-up time
			m_dTemperatureProfileDifferenceArray[i][0] = m_dTemperatureProfileDifferenceArray[i][0] - (0.5 * (this.m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_InterpolatedIdealTempProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0])) / 60));
			m_dTemperatureProfileDifferenceArray[i][1] = 0;
		}
		
		// Add the last data point at the position of the last-eluting compound
		m_dTemperatureProfileDifferenceArray[iNumTempProgramDataPoints - 1][0] = (Double)m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1];
		m_dTemperatureProfileDifferenceArray[iNumTempProgramDataPoints - 1][1] = 0;
		
		// Now adjust to get points at the corners
		int iPointIndex = 1;
		// Run through each corner in the ideal temperature profile array
		for (int i = 1; i < this.m_dIdealTemperatureProfileArray.length - 1; i++)
		{
			double dFirst = 0;
			double dNext = 0;
			
			// Find the first point after the corner (dNext) and the first point before the corner (dFirst)
			while (dNext < this.m_dIdealTemperatureProfileArray[i][0] && iPointIndex < m_dTemperatureProfileDifferenceArray.length - 1)
			{
				dFirst = m_dTemperatureProfileDifferenceArray[iPointIndex][0];
				dNext = m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0];
				
				iPointIndex++;
			}
			
			// Remove the last increment - now iPointIndex is the index of the point before this corner
			iPointIndex--;
			
			// Find the distances between the corner and the two points
			double dDistFirst = this.m_dIdealTemperatureProfileArray[i][0] - dFirst;
			double dDistNext = dNext - this.m_dIdealTemperatureProfileArray[i][0];
			
			// Find the distances between the two points and their next further point
			double dDistFirstBefore = m_dTemperatureProfileDifferenceArray[iPointIndex][0] - m_dTemperatureProfileDifferenceArray[iPointIndex - 1][0];
			double dDistNextAfter;
			if (iPointIndex + 2 < m_dTemperatureProfileDifferenceArray.length)
				dDistNextAfter = m_dTemperatureProfileDifferenceArray[iPointIndex + 2][0] - m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0];
			else
				dDistNextAfter = 0;
			
			// Score is based on how close the point is to the corner
			double dScoreFirst = dDistFirst + dDistFirstBefore;
			double dScoreNext = dDistNext + dDistNextAfter;
			
			// If the earlier point is sitting on a corner, then it should not be moved.
			if (isTemperatureProfilePointAtCorner(iPointIndex))
			{
				dScoreFirst = 1;
				dScoreNext = 0;
			}
			
			// Point with lower score moves to corner
			if (dScoreFirst < dScoreNext)
			{
				// Move the first point
				m_dTemperatureProfileDifferenceArray[iPointIndex][0] = this.m_dIdealTemperatureProfileArray[i][0];
				m_dTemperatureProfileDifferenceArray[iPointIndex][1] = 0;

				// Move the one before it right in between it and the last point
				if (iPointIndex >= 2 && !isTemperatureProfilePointAtCorner(iPointIndex - 1))
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex - 1][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex - 2][0] + m_dTemperatureProfileDifferenceArray[iPointIndex][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex - 1][1] = 0;
				}
				
				// Move the one after it in between it and the next point
				if (iPointIndex <= m_dTemperatureProfileDifferenceArray.length - 3)
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex][0] + m_dTemperatureProfileDifferenceArray[iPointIndex + 2][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex + 1][1] = 0;
				}
			}
			else
			{
				// Move the next point
				m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0] = this.m_dIdealTemperatureProfileArray[i][0];
				m_dTemperatureProfileDifferenceArray[iPointIndex + 1][1] = 0;

				// Move the one before it right in between it and the last point
				if (iPointIndex >= 1 && !isTemperatureProfilePointAtCorner(iPointIndex))
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex - 1][0] + m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex][1] = 0;
				}
				
				// Move the one after it in between it and the next point
				if (iPointIndex <= m_dTemperatureProfileDifferenceArray.length - 4)
				{
					m_dTemperatureProfileDifferenceArray[iPointIndex + 2][0] = (m_dTemperatureProfileDifferenceArray[iPointIndex + 1][0] + m_dTemperatureProfileDifferenceArray[iPointIndex + 3][0]) / 2;
					m_dTemperatureProfileDifferenceArray[iPointIndex + 2][1] = 0;
				}
			}
		}

    	// Now for the flow rate vs. temp profile:
    	// First use m_InitialInterpolatedFlowRateVsTempProfile to figure out where to put the points
		double dMinTemp = this.m_dIdealTemperatureProfileArray[0][1];
		double dMaxTemp = this.m_dIdealTemperatureProfileArray[this.m_dIdealTemperatureProfileArray.length - 1][1];
		
    	// Add the initial interpolated hold-up time profile to the graph control
	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = (((double)i / (double)(iNumPoints - 1)) * (dMaxTemp - dMinTemp)) + dMinTemp;
	    	contentPane2.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, dXPos, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dXPos));
	    }

	    for (int i = 0; i < iNumFlowDataPoints; i++)
		{
			m_dHoldUpArray[i][0] = ((dMaxTemp - dMinTemp) * ((double)i/((double)iNumFlowDataPoints - 1))) + dMinTemp;
			m_dHoldUpArray[i][1] = this.m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dHoldUpArray[i][0]);
		}
		
		m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
		m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);

		//setContentPane(contentPane2);
		this.jMainScrollPane.setViewportView(contentPane2);
		
		// Will come out to the same value as the ideal temperature program
		calcSimpleTemperatureProgram(100, 1000);

		this.updateGraphs(false);
    }
    
    public boolean isTemperatureProfilePointAtCorner(int iPointIndex)
    {
    	double dPointTime = m_dTemperatureProfileDifferenceArray[iPointIndex][0];
    	
		for (int i = 0; i < this.m_dIdealTemperatureProfileArray.length - 1; i++)
    	{
    		if (dPointTime == this.m_dIdealTemperatureProfileArray[i][0])
    			return true;
    	}
		
		return false;
    }
    
    public void actionPerformed(ActionEvent evt) 
	{
	    String strActionCommand = evt.getActionCommand();

	    if (strActionCommand == "Help")
	    {
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
	    	Frame[] frames = Frame.getFrames();
	    	//BackCalculationOptionsDialog dlgBackCalculationOptions = new BackCalculationOptionsDialog(frames[0]);
	    	
	    	// Show the dialog.
	    	//dlgBackCalculationOptions.setVisible(true);
	    	
	    	//if (dlgBackCalculationOptions.m_bOk == false)
	    	//	return;

	    	beginBackCalculation(true);//dlgBackCalculationOptions.m_bFlowRateProfileFirst);
	    }
	    else if (strActionCommand == "Stop Calculations")
	    {
	    	task.cancel(true);
	    }
	    else if (strActionCommand == "Next Step2")
	    {
	    	if (m_iStage == 2)
	    	{
	    		m_dExpectedErrorArray = new double[Globals.TestCompoundNameArray.length];
	    		
	    		// Fill in the table with predicted retention times
		    	for (int i = 0; i < Globals.TestCompoundNameArray.length; i++)
		    	{
		    		PredictedRetentionObject predictedRetention;
		    		//if (!Globals.USEPARAMDATA)
		    		//	predictedRetention = predictRetention(Globals.TestCompoundIsothermalDataArray[i]);
		    		//else
		    			predictedRetention = predictRetention2(Globals.TestCompoundParamArray[i]);

		    		m_dExpectedErrorArray[i] = predictedRetention.dPredictedErrorSigma;
		    		contentPane2.m_tmTestCompoundsModel.setValueAt(Globals.roundToSignificantFigures(predictedRetention.dPredictedRetentionTime, 5), i, 2);
		    	}
		    	
		    	this.updateTestCompoundTable();
	    		
		    	contentPane2.jpanelStep4.setVisible(false);
		    	contentPane2.jpanelStep5.setVisible(true);
		    	contentPane2.jpanelStep6.setVisible(false);
		    	if (!Globals.CHECKYOURGC)
		    		contentPane2.jbtnNextStep.setVisible(true);
		    	else
		    		contentPane2.jbtnNextStep.setVisible(false);
	    	}
	    	else if (m_iStage == 3)
	    	{
		    	// Fill in the table with the solutes that weren't selected
		    	contentPane2.tmPredictionModel.getDataVector().clear();
		    	
		    	//Add compounds to the prediction table
		    	for (int i = 0; i < Globals.CompoundParamArray.length; i++)
		    	{
		    		Object[] newRow = {Globals.CompoundNameArray[i], null};
	    			contentPane2.tmPredictionModel.addRow(newRow);
		    	}
		    	
		    	contentPane2.jpanelStep4.setVisible(false);
		    	contentPane2.jpanelStep5.setVisible(false);
		    	contentPane2.jpanelStep6.setVisible(true);
		    	contentPane2.jbtnNextStep.setVisible(false);
		    	contentPane2.jbtnPredict.setEnabled(true);
		    	contentPane2.jProgressBar2.setString("");
		    	contentPane2.jProgressBar2.setIndeterminate(false);
	    	}
		    	
	    	m_iStage++;
	    }
	    else if (strActionCommand == "Predict")
	    {
	    	contentPane2.jbtnPredict.setEnabled(false);
			//contentPane2.jProgressBar2.setIndeterminate(true);
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
	    	
	    	// Change settings
    		if (Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][0] == 0.0)
    		{
    			switchToConstantFlowRateMode();
    	    	contentPane.jtxtFlowRate.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][1]).toString());
    		}
    		else
    		{
    			switchToConstantPressureMode();    			
    	    	contentPane.jtxtPressure.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][1]).toString());
    		}
    		
    		if (Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][2] == 0.0)
    		{
    			vacuumOutletPressure();
    		}
    		else
    		{
    			otherOutletPressure();
    			contentPane.jtxtOtherPressure.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][3]).toString());
    		}

    		contentPane.jtxtColumnLength.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][4]).toString());
    		contentPane.jtxtInnerDiameter.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][5]).toString());
    		contentPane.jtxtFilmThickness.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][0][6]).toString());
    		contentPane.jtxtInitialTemperature.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][1][0]).toString());
    		contentPane.jtxtInitialTime.setText(((Double)Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][1][1]).toString());

    		this.m_bDoNotChangeTable = true;
    		contentPane.tmTemperatureProgram.setRowCount(0);
    		
    		for (int i = 2; i < Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition].length; i++)
    		{
        		Object[] rowData = {
        				Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][i][0],
        				Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][i][1],
        				Globals.dTemperaturePrograms[dlgPreloadedValues.m_iCondition][i][2]};
        		
    			contentPane.tmTemperatureProgram.addRow(rowData);
    		}
    		
	    	// Put in default values
	    	for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
	    	{
	    		contentPane.tmMeasuredRetentionTimes.setValueAt(0.0, i, 3);
	    	}
	    	
			for (int i = 0; i < Globals.dPredefinedValues[dlgPreloadedValues.m_iCondition][0].length; i++)
			{
	            contentPane.tmMeasuredRetentionTimes.setValueAt(Globals.dPredefinedValues[dlgPreloadedValues.m_iCondition][0][i], i, 3);
	            if (Globals.dPredefinedValues[dlgPreloadedValues.m_iCondition][0][i] > 0)
		            contentPane.tmMeasuredRetentionTimes.setValueAt(true, i, 0);	            	
	            else
		            contentPane.tmMeasuredRetentionTimes.setValueAt(false, i, 0);	
			}
			
			for (int i = 0; i < Globals.dPredefinedValues[dlgPreloadedValues.m_iCondition][1].length; i++)
			{
	            double dRetentionTime = Globals.roundToSignificantFigures(Globals.dPredefinedValues[dlgPreloadedValues.m_iCondition][1][i], 5);
	            
	            if (dRetentionTime == 0)
	            	contentPane2.m_tmTestCompoundsModel.setValueAt("", i, 3);
	            else
	            	contentPane2.m_tmTestCompoundsModel.setValueAt(dRetentionTime, i, 3);
			}
			
	    	performValidations();
	    	
	    }
	    else if (strActionCommand == "Insert Row")
	    {
	    	int iSelectedRow = contentPane.jtblTemperatureProgram.getSelectedRow();
	    	
	    	if (contentPane.jtblTemperatureProgram.getRowCount() == 0)
	    	{
		    	Double dRowValue1 = 20.0;
		    	Double dRowValue2 = m_dInitialTemperature;
		    	Double dRowValue3 = 5.0;
		    	Double dRowData[] = {dRowValue1, dRowValue2, dRowValue3};
		    	contentPane.tmTemperatureProgram.addRow(dRowData);	    		
	    	}
	    	else if (iSelectedRow == -1)
	    	{
		    	Double dRowValue1 = 0.0;
		    	Double dRowValue2 = (Double) contentPane.jtblTemperatureProgram.getValueAt(contentPane.jtblTemperatureProgram.getRowCount() - 1, 1);
		    	Double dRowValue3 = 5.0;
		    	Double dRowData[] = {dRowValue1, dRowValue2, dRowValue3};
		    	contentPane.tmTemperatureProgram.addRow(dRowData);	    		
	    	}
	    	else
	    	{
		    	Double dRowValue1 = 0.0;
		    	Double dRowValue2 = (Double) contentPane.jtblTemperatureProgram.getValueAt(iSelectedRow, 1);
		    	Double dRowValue3 = 5.0;
		    	Double dRowData[] = {dRowValue1, dRowValue2, dRowValue3};
		    	contentPane.tmTemperatureProgram.insertRow(iSelectedRow, dRowData);	    		
	    	}	
	    }
	    else if (strActionCommand == "Remove Row")
	    {
	    	int iSelectedRow = contentPane.jtblTemperatureProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = contentPane.jtblTemperatureProgram.getRowCount() - 1;
	    	
	    	if (iSelectedRow >= 0)
	    	{
	    		contentPane.tmTemperatureProgram.removeRow(iSelectedRow);
	    	}
	    }
	    else if (strActionCommand == "Constant flow rate mode")
	    {
	    	switchToConstantFlowRateMode();
	    }
	    else if (strActionCommand == "Constant pressure mode")
	    {
	    	switchToConstantPressureMode();
	    }
	    else if (strActionCommand == "Vacuum")
	    {
	    	vacuumOutletPressure();
	    }
	    else if (strActionCommand == "OtherPressure")
	    {
	    	otherOutletPressure();
	    }
	    else if (strActionCommand == "Find retention times automatically...")
	    {
	    	Component p = this;
	    	while ((p = p.getParent()) != null && !(p instanceof Frame));
	  
	    	PeakFinderSettingsDialog peakFinderSettingsDialog = new PeakFinderSettingsDialog((Frame)p, true, Globals.StationaryPhaseArray, true);
	    	peakFinderSettingsDialog.setLocationByPlatform(true);
	    	peakFinderSettingsDialog.setIsothermalData(Globals.AlkaneParamArray);
	    	peakFinderSettingsDialog.setStandardCompoundNames(Globals.AlkaneNameArray);
	    	peakFinderSettingsDialog.setStandardCompoundMZData(Globals.AlkaneMZArray);
	    	peakFinderSettingsDialog.setFlowRate(m_dFlowRate);
	    	peakFinderSettingsDialog.setInletPressure(m_dInletPressure);
	    	peakFinderSettingsDialog.setConstantFlowMode(contentPane.jrdoConstantFlowRate.isSelected());
	    	peakFinderSettingsDialog.setInitialTime(m_dInitialTime);
	    	peakFinderSettingsDialog.setInitialTemperature(m_dInitialTemperature);
	    	peakFinderSettingsDialog.setOutletPressure(m_dOutletPressure, contentPane.jrdoVacuum.isSelected());
	    	peakFinderSettingsDialog.setColumnLength(m_dColumnLength);
	    	peakFinderSettingsDialog.setInnerDiameter(m_dInnerDiameter);
	    	peakFinderSettingsDialog.setFilmThickness(m_dFilmThickness);
	    	
	    	double[][] dTemperatureProgram = new double[contentPane.tmTemperatureProgram.getRowCount()][3];
	    	for (int i = 0; i < contentPane.tmTemperatureProgram.getRowCount(); i++)
	    	{
	    		dTemperatureProgram[i][0] = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 0);
	    		dTemperatureProgram[i][1] = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 1);
	    		dTemperatureProgram[i][2] = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 2);
	    	}
	    	peakFinderSettingsDialog.setTemperatureProgram(dTemperatureProgram);
	    	
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
	    				this.contentPane.tmMeasuredRetentionTimes.setValueAt(false, i, 0);
	    			}
	    			else if (iPeakRank[i] != -1)
	    			{
	    				this.contentPane.tmMeasuredRetentionTimes.setValueAt(true, i, 0);
	    				this.contentPane.tmMeasuredRetentionTimes.setValueAt(dRetentionTimes[i], i, 3);
	    			}
	    		}
	    		
	    		performValidations();
	    		
	    		m_strFileName = peakFinderSettingsDialog.getFileName();
	    	}
	    }
	    else if (strActionCommand == "Copy to clipboard")
	    {
	    	copyProfilesToClipboard();
	    }
	    else if (strActionCommand == "Automatically Determine Test Compound Retention Times")
	    {
	    	Component p = this;
	    	while ((p = p.getParent()) != null && !(p instanceof Frame));
	  
	    	PeakFinderSettingsDialog peakFinderSettingsDialog = new PeakFinderSettingsDialog((Frame)p, true, Globals.StationaryPhaseArray, false);
	    	peakFinderSettingsDialog.setLocationByPlatform(true);
	    	peakFinderSettingsDialog.setIsothermalData(Globals.TestCompoundParamArray);
	    	peakFinderSettingsDialog.setStandardCompoundNames(Globals.TestCompoundNameArray);
	    	peakFinderSettingsDialog.setStandardCompoundMZData(Globals.TestCompoundMZArray);
	    	peakFinderSettingsDialog.setFlowRate(m_dFlowRate);
	    	peakFinderSettingsDialog.setInletPressure(m_dInletPressure);
	    	peakFinderSettingsDialog.setConstantFlowMode(contentPane.jrdoConstantFlowRate.isSelected());
	    	peakFinderSettingsDialog.setInitialTime(m_dInitialTime);
	    	peakFinderSettingsDialog.setInitialTemperature(m_dInitialTemperature);
	    	peakFinderSettingsDialog.setOutletPressure(m_dOutletPressure, contentPane.jrdoVacuum.isSelected());
	    	peakFinderSettingsDialog.setColumnLength(m_dColumnLength);
	    	peakFinderSettingsDialog.setInnerDiameter(m_dInnerDiameter);
	    	peakFinderSettingsDialog.setFilmThickness(m_dFilmThickness);
	    	
	    	double[][] dCombinedTempProfileArray = new double[m_dSimpleTemperatureProfileArray.length][2];
	    	for (int i = 0; i < m_dSimpleTemperatureProfileArray.length; i++)
	    	{
	    		dCombinedTempProfileArray[i][0] = m_dSimpleTemperatureProfileArray[i][0];
	    		dCombinedTempProfileArray[i][1] = m_dSimpleTemperatureProfileArray[i][1] + this.m_InterpolatedTemperatureDifferenceProfile.getAt(m_dSimpleTemperatureProfileArray[i][0]);
	    	}
	    	peakFinderSettingsDialog.setTemperatureProfile(dCombinedTempProfileArray);
	    	
	    	peakFinderSettingsDialog.setHoldUpTimeProfile(this.m_dHoldUpArray);
	    	peakFinderSettingsDialog.setFileName(this.m_strFileName);
	    	
	    	// Set the tstep to the last one used in back-calculation
	    	peakFinderSettingsDialog.setTStep(this.m_dtstep);
	    		    	
	    	double[][] dTemperatureProgram = new double[contentPane.tmTemperatureProgram.getRowCount()][3];
	    	for (int i = 0; i < contentPane.tmTemperatureProgram.getRowCount(); i++)
	    	{
	    		dTemperatureProgram[i][0] = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 0);
	    		dTemperatureProgram[i][1] = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 1);
	    		dTemperatureProgram[i][2] = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 2);
	    	}
	    	peakFinderSettingsDialog.setTemperatureProgram(dTemperatureProgram);
	    	
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
	    				this.contentPane2.m_tmTestCompoundsModel.setValueAt(Globals.roundToSignificantFigures(dRetentionTimes[i], 5), i, 3);
	    			}
	    		}
	    	}
	    }
	}

    public void vacuumOutletPressure()
    {
    	contentPane.jrdoVacuum.setSelected(true);
    	contentPane.jrdoOtherPressure.setSelected(false);
    	
    	contentPane.jtxtOtherPressure.setEnabled(false);
    	contentPane.jlblOtherPressureUnit.setEnabled(false);
    	
    	this.m_dOutletPressure = .000001;
    	performValidations();
    }
    
    public void otherOutletPressure()
    {
    	contentPane.jrdoVacuum.setSelected(false);
    	contentPane.jrdoOtherPressure.setSelected(true);
    	
    	contentPane.jtxtOtherPressure.setEnabled(true);
    	contentPane.jlblOtherPressureUnit.setEnabled(true);
    	
    	this.m_dOutletPressure = 100; // 100 kPa
    	performValidations();
    }
    
    public void switchToConstantPressureMode()
    {
    	contentPane.jrdoConstantPressure.setSelected(true);
    	contentPane.jrdoConstantFlowRate.setSelected(false);
    	contentPane.jlblFlowRate.setEnabled(false);
    	contentPane.jlblFlowRateUnit.setEnabled(false);
    	contentPane.jtxtFlowRate.setEnabled(false);
    	contentPane.jlblPressure.setEnabled(true);
    	contentPane.jtxtPressure.setEnabled(true);
    	contentPane.jlblPressureUnit.setEnabled(true);
    	performValidations();
    }
    
    public void switchToConstantFlowRateMode()
    {
    	contentPane.jrdoConstantFlowRate.setSelected(true);
    	contentPane.jrdoConstantPressure.setSelected(false);
    	contentPane.jlblFlowRate.setEnabled(true);
    	contentPane.jlblFlowRateUnit.setEnabled(true);
    	contentPane.jtxtFlowRate.setEnabled(true);
    	contentPane.jlblPressure.setEnabled(false);
    	contentPane.jtxtPressure.setEnabled(false);
    	contentPane.jlblPressureUnit.setEnabled(false);
    	performValidations();
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
		validateInitialTemperature();
		validateInitialTime();
		validateColumnLength();
		validateInnerDiameter();
		validateFilmThickness();
		validateFlowRate();
		validateInletPressure();
		validateOtherOutletPressure();
		validateConfidenceWindow();
		/*validateTransferLineLength();
		validateTransferLineTemperature();
		
		double dInnerDiameter = 0.00025; // in m
		m_V0 = (Math.PI * Math.pow((dInnerDiameter * 100) / 2, 2) * (this.m_dColumnLength * 100)) / 1000; // gives the volume in the column (in L)

		int iNumPoints = 10;
		double dLowTemp = 0;
		double dHighTemp = 500;
		
		double[][] dHoldUpArray = new double[iNumPoints][2];
		//double[][] dTm2ToTmTotVsTemperatureArray = new double[iNumPoints][2];
		
		for (int i = 0; i < iNumPoints; i++)
		{
			dHoldUpArray[i][0] = ((dHighTemp - dLowTemp) * ((double)i / (double)(iNumPoints - 1))) + dLowTemp;
			dTm2ToTmTotVsTemperatureArray[i][0] = dHoldUpArray[i][0];

			double dInletPressure = this.m_dInletPressure * 1000.0 + 101325;
			double dOutletPressure = this.m_dOutletPressure * 1000.0;
			double dT1 = dHoldUpArray[i][0] + 273.15;
			double dT2 = m_dTransferLineTemperature + 273.15;
			double dL1 = this.m_dColumnLength - this.m_dTransferLineLength;
			double dL2 = this.m_dTransferLineLength;
			double dFlowRate = m_dFlowRate / (1000000 * 60);

			// tM1 is the hold up time in the column, tM2 is the hold up time in the transfer line
			double dGasViscosity1 = 18.69 * Math.pow(10, -6) * Math.pow((dT1) / 273.15, 0.6958 + -0.0071 * (((dT1) - 273.15) / 273.15));
			double dOmega1 = dL1 * dGasViscosity1 * (32.0 / Math.pow(dInnerDiameter, 2));
			double dDeadTime1 = 0;
			
			double dGasViscosity2 = 18.69 * Math.pow(10, -6) * Math.pow((dT2) / 273.15, 0.6958 + -0.0071 * (((dT2) - 273.15) / 273.15));
			double dOmega2 = dL2 * dGasViscosity2 * (32.0 / Math.pow(dInnerDiameter, 2));
			double dDeadTime2 = 0;
			
			double dMidPressure = 0;
			
			if (contentPane.jrdoConstantFlowRate.isSelected())
			{	// Constant flow rate mode
				//double dFirstTerm = (Math.pow(Math.PI, 2) * Math.pow(dInnerDiameter / 10, 4) * Math.pow(this.m_dOutletPressure * 1000.0, 3)) / (48 * 32 * dGasViscosity * Math.pow(101325, 2) * Math.pow(((dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 10.0)), 2)); 
				//double dSecondTerm = Math.pow(1.0 + ((8.0 * 32.0 * this.m_dColumnLength * dGasViscosity * 101325.0 * (((dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 100.0))))/(Math.PI * Math.pow(dInnerDiameter / 10, 3) * Math.pow(this.m_dOutletPressure * 1000.0, 2))), 3.0/2.0) - 1.0;
				//dDeadTime = dFirstTerm * dSecondTerm;
				dMidPressure = Math.sqrt(Math.PI * Math.pow(dOutletPressure, 2) + (8 * dFlowRate * 101325 * dT2 * dOmega2)
						/ (Math.pow(dInnerDiameter, 2) * (25 + 273.15))) / Math.sqrt(Math.PI);
				double dTopPart = -8 * dFlowRate * dT1 - (Math.pow(dInnerDiameter, 2) * Math.PI * Math.pow(dMidPressure, 2) * (25 + 273.15)) 
						/ (101325 * dOmega1);
				Complex cTopPart = new Complex(dTopPart);
				Complex cNumerator = Complex.I.multiply(Math.sqrt(101325)).multiply(cTopPart.sqrt()).multiply(new Complex(dOmega1).sqrt());
				double dDenominator = dInnerDiameter * Math.sqrt(Math.PI) * Math.sqrt(25 + 273.15);
				Complex cInletPressure = cNumerator.divide(dDenominator);
				dInletPressure = -cInletPressure.getReal();
			}
			else
			{	// Constant pressure mode
				// The equation immediately below does not account for transfer line effects
				// dDeadTime = (4 * dOmega * this.m_dColumnLength * (Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 3) - Math.pow(this.m_dOutletPressure * 1000.0, 3))) / (3 * Math.pow(Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 2) - Math.pow(this.m_dOutletPressure * 1000.0, 2), 2));				
			
				// This equation does account for transfer line effects on hold-up time
				
				double dTopPart = -(Math.pow(dInletPressure, 2) / (2 * dT1 * dOmega1)) - (Math.pow(dOutletPressure, 2) / (2 * dT2 * dOmega2));
				double dBottomPart = -(1 / (2 * dT1 * dOmega1)) - (1 / (2 * dT2 * dOmega2));
				Complex cTopPart = new Complex(dTopPart);
				Complex cBottomPart = new Complex(dBottomPart);
				Complex cMidPressure = (cTopPart.sqrt()).divide(cBottomPart.sqrt());
				dMidPressure = cMidPressure.getReal();
			}
			
			dDeadTime1 = (4 * dOmega1 * dL1 *(Math.pow(dInletPressure, 3) - Math.pow(dMidPressure, 3))) / (3 * Math.pow(Math.pow(dInletPressure, 2) - Math.pow(dMidPressure, 2), 2));
			dDeadTime2 = (4 * dOmega2 * dL2 *(Math.pow(dMidPressure, 3) - Math.pow(dOutletPressure, 3))) / (3 * Math.pow(Math.pow(dMidPressure, 2) - Math.pow(dOutletPressure, 2), 2));

			dHoldUpArray[i][1] = dDeadTime1 + dDeadTime2; // in seconds
			dTm2ToTmTotVsTemperatureArray[i][1] = dDeadTime2 / dHoldUpArray[i][1];
		}
		
		this.m_InitialInterpolatedHoldUpVsTempProfile = new InterpolationFunction(dHoldUpArray);
		this.m_InterpolatedTm2ToTmTotVsTemperature = new InterpolationFunction(dTm2ToTmTotVsTemperatureArray);
*/

		// Calculate hold-up time vs. temperature profile
		
		// Subtract film thickness out of the inner diameter
		double dInnerDiameter = (this.m_dInnerDiameter / 10) - (2 * this.m_dFilmThickness / 10000); // in cm
		
		m_V0 = (Math.PI * Math.pow(dInnerDiameter / 2, 2) * (this.m_dColumnLength * 100)) / 1000; // gives the volume in the column (in L)

		int iNumPoints = 10;
		double dLowTemp = 0;
		double dHighTemp = 500;
		
		double[][] dHoldUpArray = new double[iNumPoints][2];
		
		for (int i = 0; i < iNumPoints; i++)
		{
			dHoldUpArray[i][0] = ((dHighTemp - dLowTemp) * ((double)i / (double)(iNumPoints - 1))) + dLowTemp;
			double dGasViscosity = 18.69 * Math.pow(10, -6) * Math.pow((dHoldUpArray[i][0] + 273.15) / 273.15, 0.6958 + -0.0071 * (((dHoldUpArray[i][0] + 273.15) - 273.15) / 273.15));
			double dOmega = this.m_dColumnLength * dGasViscosity * (32.0 / Math.pow(dInnerDiameter / 100, 2));
			double dDeadTime = 0;
			if (contentPane.jrdoConstantFlowRate.isSelected())
			{	// Constant flow rate mode
				double dFirstTerm = (Math.pow(Math.PI, 2) * Math.pow(dInnerDiameter / 10, 4) * Math.pow(this.m_dOutletPressure * 1000.0, 3)) / (48 * 32 * dGasViscosity * Math.pow(101325, 2) * Math.pow(((dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 10.0)), 2)); 
				double dSecondTerm = Math.pow(1.0 + ((8.0 * 32.0 * this.m_dColumnLength * dGasViscosity * 101325.0 * (((dHoldUpArray[i][0] + 273.15) / (25.0 + 273.15)) * ((this.m_dFlowRate / (60.0 * 1000.0)) / (dInnerDiameter / 100.0))))/(Math.PI * Math.pow(dInnerDiameter / 10, 3) * Math.pow(this.m_dOutletPressure * 1000.0, 2))), 3.0/2.0) - 1.0;
				dDeadTime = dFirstTerm * dSecondTerm;
			}
			else
			{	// Constant pressure mode
				dDeadTime = (4 * dOmega * this.m_dColumnLength * (Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 3) - Math.pow(this.m_dOutletPressure * 1000.0, 3))) / (3 * Math.pow(Math.pow(this.m_dInletPressure * 1000.0 + 101325.0, 2) - Math.pow(this.m_dOutletPressure * 1000.0, 2), 2));				
			}

			dHoldUpArray[i][1] = dDeadTime; // in seconds
		}
		
		this.m_InitialInterpolatedHoldUpVsTempProfile = new InterpolationFunction(dHoldUpArray);

		// Find maximum retention time
		double dMax = 0;
		for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
		{
			double dValue = (Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i, 3);
			if (dValue > dMax)
				dMax = dValue;
		}

		// Calculate total time of temperature program
		double dTotalTime = this.m_dInitialTime;
		double dLastTemp = this.m_dInitialTemperature;

		for (int i = 0; i < contentPane.tmTemperatureProgram.getRowCount(); i++)
		{
			double dRamp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 0);
			double dFinalTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 1);
			double dFinalTime = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 2);
			
			if (dRamp != 0)
				dTotalTime += (dFinalTemp - dLastTemp) / dRamp;
			
			dTotalTime += dFinalTime;
			
			dLastTemp = dFinalTemp;
		}
		
		m_dPlotXMax = Math.max(dTotalTime * 1.2, dMax * 1.02);
		
		// Begin drawing temperature program and hold-up time vs. time
    	contentPane.m_GraphControlTemp.RemoveAllSeries();
    	contentPane.m_GraphControlHoldUp.RemoveAllSeries();

    	int iIdealPlotIndex = contentPane.m_GraphControlTemp.AddSeries("Ideal Gradient", new Color(0, 0, 0), 1, false, false);
    	int iIdealPlotIndexHoldUp = contentPane.m_GraphControlHoldUp.AddSeries("Ideal Hold Up", new Color(0, 0, 0), 1, false, false);

    	// Draw first data points
    	contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, 0, m_dInitialTemperature);
    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, 0, m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dInitialTemperature));
    	
    	contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, m_dInitialTime * 60, m_dInitialTemperature);
    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, m_dInitialTime * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dInitialTemperature));

    	// Draw the rest of the profiles
    	dTotalTime = m_dInitialTime;
    	dLastTemp = m_dInitialTemperature;
    	double dFinalTemp = m_dInitialTemperature;

    	for (int i = 0; i < contentPane.tmTemperatureProgram.getRowCount(); i++)
		{
			double dRamp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 0);
			dFinalTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 1);
			double dFinalTime = (Double)contentPane.tmTemperatureProgram.getValueAt(i, 2);

			if (dRamp != 0)
			{
				double dLastTime = dTotalTime;
				dTotalTime += (dFinalTemp - dLastTemp) / dRamp;
				contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, dTotalTime * 60, dFinalTemp);
		    	
				// Add many data points to show flow rate.
				for (int j = 1; j < 500; j++)
				{
					double dTime = ((dTotalTime - dLastTime) * ((double)j / 500.0)) + dLastTime;
					double dTemperature = ((dFinalTemp - dLastTemp) * ((double)j / 500.0)) + dLastTemp;
					contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, dTime * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dTemperature));
				}
			}
			
			if (dFinalTime != 0)
			{
				dTotalTime += dFinalTime;
				contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, dTotalTime * 60, dFinalTemp);				
		    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, dTotalTime * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dFinalTemp));
			}
			
			dLastTemp = dFinalTemp;
		}

		if (contentPane.tmTemperatureProgram.getRowCount() > 0)
		{
			contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, m_dPlotXMax * 60, dFinalTemp);
    		contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, m_dPlotXMax * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(dFinalTemp));
		}
    	else
    	{
			contentPane.m_GraphControlTemp.AddDataPoint(iIdealPlotIndex, m_dPlotXMax * 60, m_dInitialTemperature);
	    	contentPane.m_GraphControlHoldUp.AddDataPoint(iIdealPlotIndexHoldUp, m_dPlotXMax * 60, m_InitialInterpolatedHoldUpVsTempProfile.getAt(m_dInitialTemperature));
    	}

   		contentPane.m_GraphControlTemp.AutoScaleX();
   		contentPane.m_GraphControlTemp.AutoScaleY();
    	
    	contentPane.m_GraphControlTemp.repaint();   
    	
   		contentPane.m_GraphControlHoldUp.AutoScaleX();
   		contentPane.m_GraphControlHoldUp.AutoScaleY();
    	
    	contentPane.m_GraphControlHoldUp.repaint();   	
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
		
		if(e.getSource() == contentPane.tmTemperatureProgram || e.getSource() == contentPane.tmMeasuredRetentionTimes)
		{
			if(e.getSource() == contentPane.tmTemperatureProgram)
			{
				int row = e.getFirstRow();
				int column = e.getColumn();
				
				if (column == 0)
				{
					Double dNewValue = (Double) contentPane.tmTemperatureProgram.getValueAt(row, 0);
					
					double dTemp = dNewValue;
					if (dTemp < 0)
						dTemp = 0;
					if (dTemp > 1000)
						dTemp = 1000;
					
			    	m_bDoNotChangeTable = true;
					contentPane.tmTemperatureProgram.setValueAt(dTemp, row, column);
				}
				else if (column == 1)
				{
					Double dNewValue = (Double) contentPane.tmTemperatureProgram.getValueAt(row, 1);
					
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
					if (row < contentPane.tmTemperatureProgram.getRowCount() - 1)
					{
						if (dTemp > (Double)contentPane.tmTemperatureProgram.getValueAt(row + 1, column))
						{
							dTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(row + 1, column);
						}
					}
					if (row > 0)
					{
						if (dTemp < (Double)contentPane.tmTemperatureProgram.getValueAt(row - 1, column))
						{
							dTemp = (Double)contentPane.tmTemperatureProgram.getValueAt(row - 1, column);
						}
					}
			    	m_bDoNotChangeTable = true;
					contentPane.tmTemperatureProgram.setValueAt(dTemp, row, column);
				}
				else if (column == 2)
				{
					Double dNewValue = (Double) contentPane.tmTemperatureProgram.getValueAt(row, 2);
					
					double dTemp = dNewValue;
					if (dTemp < 0)
						dTemp = 0;
					if (dTemp > 1000)
						dTemp = 1000;
					
			    	m_bDoNotChangeTable = true;
					contentPane.tmTemperatureProgram.setValueAt(dTemp, row, column);
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
			
			contentPane.m_GraphControlTemp.removeAllLineMarkers();
			contentPane.m_GraphControlHoldUp.removeAllLineMarkers();
			
			for (int i = 0; i < contentPane.tmMeasuredRetentionTimes.getRowCount(); i++)
			{
				if ((Boolean)contentPane.tmMeasuredRetentionTimes.getValueAt(i,0) == true)
				{
					contentPane.m_GraphControlTemp.addLineMarker((Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i,3), (String)contentPane.tmMeasuredRetentionTimes.getValueAt(i,1));
					contentPane.m_GraphControlHoldUp.addLineMarker((Double)contentPane.tmMeasuredRetentionTimes.getValueAt(i,3), (String)contentPane.tmMeasuredRetentionTimes.getValueAt(i,1));
				}
			}

			contentPane.m_GraphControlTemp.repaint();
			contentPane.m_GraphControlHoldUp.repaint();
			
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

			contentPane2.tmPredictionModel.setValueAt(Float.toString((float)Globals.roundToSignificantFigures(m_PredictedRetentionTimes[i].dPredictedErrorSigma * dNumSigmas, 2)), i, 2);
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
				contentPane2.m_tmTestCompoundsModel.setValueAt(Globals.roundToSignificantFigures((Double)MeasuredRetentionTimeValue, 5), i, 3);
				contentPane2.m_tmTestCompoundsModel.setValueAt(Globals.roundToSignificantFigures((Double)dError, 5), i, 4);
			}
			
			if (dError != null && m_dExpectedErrorArray != null)
			{
				dSumofSquares += Math.pow(dError, 2);
				dExpectedSumofSquares += Math.pow(this.m_dExpectedErrorArray[i], 2);
				dSumAbsolute += Math.abs(dError);
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
			
			contentPane2.jlblStandardDeviation.setText("± " + Float.toString((float)Globals.roundToSignificantFigures(dStandardDeviation, 2)) + " min (" + Float.toString((float)Globals.roundToSignificantFigures(dStandardDeviation * 60, 2)) + " sec)");
			contentPane2.jlblExpectedStandardDeviation.setText("± " + Float.toString((float)Globals.roundToSignificantFigures(dExpectedStandardDeviation, 2)) + " min (" + Float.toString((float)Globals.roundToSignificantFigures(dExpectedStandardDeviation * 60, 2)) + " sec)");
			contentPane2.jlblColumnRating.setText(Float.toString((float)Globals.roundToSignificantFigures(dColumnRating, 2)));
			
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
	
	public void updateGraphs(boolean bAlsoUpdateTable)
	{
		synchronized(contentPane2.m_GraphControlTemp.lockObject)
		{
		synchronized(contentPane2.m_GraphControlHoldUp.lockObject)
		{
		// Update the graphs with the new m_dGradientArray markers and the m_InterpolatedGradient (and the same with the flow graph)
		contentPane2.m_GraphControlTemp.RemoveSeries(m_iInterpolatedTempProgramSeries);
		contentPane2.m_GraphControlTemp.RemoveSeries(m_iTempProgramMarkerSeries);
		
		contentPane2.m_GraphControlHoldUp.RemoveSeries(m_iInterpolatedHoldUpSeries);
		contentPane2.m_GraphControlHoldUp.RemoveSeries(m_iHoldUpMarkerSeries);
		
		Color clrOrange = new Color(240,90,40);
	    m_iInterpolatedTempProgramSeries = contentPane2.m_GraphControlTemp.AddSeries("Interpolated Gradient", clrOrange, 1, false, false);
	    m_iTempProgramMarkerSeries = contentPane2.m_GraphControlTemp.AddSeries("Gradient Markers", clrOrange, 1, true, false);

	    m_iInterpolatedHoldUpSeries = contentPane2.m_GraphControlHoldUp.AddSeries("Interpolated Flow", clrOrange, 1, false, false);
	    m_iHoldUpMarkerSeries = contentPane2.m_GraphControlHoldUp.AddSeries("Flow Rate Markers", clrOrange, 1, true, false);

	    double dMinTemp = this.m_dHoldUpArray[0][0];
	    double dMaxTemp = this.m_dHoldUpArray[m_dHoldUpArray.length - 1][0];
	    
	    int iNumPoints = 1000;
	    for (int i = 0; i < iNumPoints; i++)
	    {
	    	double dXPos = ((double)i / (double)(iNumPoints - 1)) * (m_dPlotXMax2 * 60);
	    	contentPane2.m_GraphControlTemp.AddDataPoint(m_iInterpolatedTempProgramSeries, dXPos, m_InterpolatedSimpleTemperatureProfile.getAt(dXPos / 60) + m_InterpolatedTemperatureDifferenceProfile.getAt(dXPos / 60));
	    	dXPos = (((double)i / (double)(iNumPoints - 1)) * (dMaxTemp - dMinTemp)) + dMinTemp;
	    	contentPane2.m_GraphControlHoldUp.AddDataPoint(m_iInterpolatedHoldUpSeries, dXPos, m_InterpolatedHoldUpProfile.getAt(dXPos));
	    }
	    
	    for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
	    {
	    	contentPane2.m_GraphControlTemp.AddDataPoint(m_iTempProgramMarkerSeries, m_dTemperatureProfileDifferenceArray[i][0] * 60, m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1]);
	    }
		
	    for (int i = 0; i < m_dHoldUpArray.length; i++)
	    {
	    	contentPane2.m_GraphControlHoldUp.AddDataPoint(m_iHoldUpMarkerSeries, m_dHoldUpArray[i][0], m_dHoldUpArray[i][1]);
	    }
	    
	    contentPane2.m_GraphControlTemp.AutoScaleX();
	    contentPane2.m_GraphControlTemp.AutoScaleY();
	    contentPane2.m_GraphControlHoldUp.AutoScaleX();
	    contentPane2.m_GraphControlHoldUp.AutoScaleY();
     
	    contentPane2.m_GraphControlTemp.repaint();
	    contentPane2.m_GraphControlHoldUp.repaint();

	    if (bAlsoUpdateTable)
	    {
			NumberFormat formatter = new DecimalFormat("#0.0000");
	
		    for (int i = 0; i < m_vectCalCompounds.size(); i++)
		    {
		    	double dMeasuredTime = (Double)m_vectCalCompounds.get(i)[1];
		    	double dPredictedTime = (Double)m_vectCalCompounds.get(i)[2];
		    	if (dPredictedTime >= 0)
		    	{
		    		contentPane2.tmOutputModel.setValueAt(formatter.format(dPredictedTime), i, 2);
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
            backCalculate(this, bFlowRateProfileBackCalculationFirst);

            return null;
        }
        
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() 
        {
        	// Calculate median of errors
        	// m_vectCalCompounds = compound #, measured retention time, projected retention time
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
	        	
	        	if (dRatio > 8)
	        	{
	        		// Report a problem with the retention time for the compound
	            	String strMessage = "Please double-check the retention time you reported for alkane " + Globals.AlkaneNameArray[(Integer)m_vectCalCompounds.get(iCompoundWithMaxError)[0]] + ". It does not seem to be correct.\n\nTo correct the retention time you entered, click \"Previous Step\", change the retention time, and re-run the back-calculation.\nOtherwise, if you are sure that the retention time is correct, continue to the next step.";
	        		JOptionPane.showMessageDialog(null, strMessage, "GC Retention Predictor", JOptionPane.WARNING_MESSAGE);
	        		
	        		contentPane2.jbtnNextStep.setEnabled(true);
	        		contentPane2.jProgressBar.setIndeterminate(false);
	        		contentPane2.jProgressBar.setStringPainted(true);
	        		contentPane2.jProgressBar.setString("Optimization complete, but errors may exist.");        		
	        	}
	        	else if (m_dVariance > 0.0001)
	        	{
	        		// Report a problem with the retention time for the compound
	            	String strMessage = "Your variance is high. This usually indicates that more than one of the alkane retention times you entered are incorrect.\nPlease double-check all of the alkane retention times you entered.\n\nTo correct the retention times, click \"Previous Step\", change the retention times, and re-run the back-calculation.\nOtherwise, if you are sure that the retention times are all correct, continue to the next step.";
	        		JOptionPane.showMessageDialog(null, strMessage, "GC Retention Predictor", JOptionPane.WARNING_MESSAGE);
	        		
	        		contentPane2.jbtnNextStep.setEnabled(true);
	        		contentPane2.jProgressBar.setIndeterminate(false);
	        		contentPane2.jProgressBar.setStringPainted(true);
	        		contentPane2.jProgressBar.setString("Optimization complete, but errors may exist.");        		
	        	}
	        	else
	        	{
	        		contentPane2.jbtnNextStep.setEnabled(true);
	        		contentPane2.jProgressBar.setIndeterminate(false);
	        		contentPane2.jProgressBar.setStringPainted(true);
	        		contentPane2.jProgressBar.setString("Optimization complete! Continue to next step.");        		
	        	}
        	}
        }
    }

    // Projects retention for one compound
    // Input: dIsothermalDataArray (for one compound)
    public PredictedRetentionObject predictRetention(double[][] dIsothermalDataArray)
    {
    	PredictedRetentionObject pro = new PredictedRetentionObject();
    	
    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
    	double dBeta2 = Math.pow((m_dInnerDiameter / 2) - (m_dFilmThickness / 1000), 2) / (Math.pow(m_dInnerDiameter / 2, 2) - Math.pow((m_dInnerDiameter / 2) - (m_dFilmThickness / 1000), 2));
    	double dBeta1Beta2 = dBeta1 / dBeta2;

		InterpolationFunction IsocraticData = new InterpolationFunction(dIsothermalDataArray);
		
		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dXPosition = 0;
		double dXPositionError = 0;
		double dLastXPosition = 0;
		double dXMovement = 0;
		Boolean bIsEluted = false;
		double dTcA = 0;
		double dTcB = 0;
		double dCurVal = 0;
		
		// Grab the first temp
		dTcA = m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_InterpolatedTemperatureDifferenceProfile.getAt(0);

		for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += m_dtstep)
		{
			// Grab the second temp
			dTcB = m_InterpolatedSimpleTemperatureProfile.getAt(t + m_dtstep) + m_InterpolatedTemperatureDifferenceProfile.getAt(t + m_dtstep);
			
			// Find the average of the two temps
			double dTc = (dTcA + dTcB) / 2;
			
			// Get the hold-up time at this temp
			double dHc = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
			// Get the amount of dead time traveled in dtstep
			double dk = Math.pow(10, IsocraticData.getAt(dTc)) * dBeta1Beta2;
			dCurVal = m_dtstep / (1 + dk);
			// Determine what fraction of the column it moved
			dXMovement = dCurVal / dHc;
			
			// Add that to the running total
			dLastXPosition = dXPosition;
			dXPosition += dXMovement;
			
			// Now calculate error
			// First, determine error in k from error in T
			//double dTError = 0.13; // sigma in deg C
			//double dkerror = Math.abs((Math.pow(10, IsocraticData.getAt(dTc + dTError)) * dBeta1Beta2) - dk);
			
			// Assume error in k from measurement itself is small.
			
			double dXMovementErrorFraction = (dk * 0.00565) / (1 + dk);
			//double dXMovementErrorFraction = (dkerror) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;
			
			/*if (dXPosition + dXPositionError >= 1 && !bSigmaEluted)
			{
				dtRErrorFinal = (((1 - dLastXPosition)/((dXPosition + dXPositionError) - dLastXPosition)) * m_dtstep) + t;
				bSigmaEluted = true;
			}*/			
			
			if (dXPosition >= 1)
			{
				dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * m_dtstep) + t;
				//dtRErrorFinal = ((1 - dLastXPosition)/(dXPosition - dLastXPosition) * m_dtstep) + t;
				double dxdt = (dXPosition - dLastXPosition) / m_dtstep;
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);		
				bIsEluted = true;
				break;
			}
			
			dTcA = dTcB;
		}
		
		if (bIsEluted)
		{
			pro.dPredictedRetentionTime = dtRFinal;
		}
		else
		{
			pro.dPredictedRetentionTime = -1.0;
		}

		// Now calculate final error in the projection
		pro.dPredictedErrorSigma = dtRErrorFinal - dtRFinal;

		return pro;
    }
    
	/**
	 * Returns the value of k for a given set of compound parameters and temperature
	 *
	 * @param  compoundParams dH(T0), dS(T0), and dC, where T0 is 273.15 K
	 * @param  T the temperature in deg C
	 * @return      the retention factor (k) at temperature T
	 */
    public double calckfromT(double[] compoundParams, double T)
    {
    	double dH = compoundParams[0];
    	double dS = compoundParams[1];
    	double dC = compoundParams[2];
    	
    	double A = (dS - dC * Math.log(273.15) - dC) / 8.3145;
    	double B = -(dH - dC * 273.15) / 8.3145;
    	double C = dC / 8.3145;
    	
    	double k = Math.pow(Math.E, A + B * (1 / (T + 273.15)) + C * Math.log(T + 273.15));
    	return k;
    }
    
    // Projects retention for one compound
    // Input: dCompoundParams (for one compound)
    public PredictedRetentionObject predictRetention2(double[] dCompoundParams)
    {
    	PredictedRetentionObject pro = new PredictedRetentionObject();
    	
    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
    	double dBeta2 = Math.pow((m_dInnerDiameter / 2) - (m_dFilmThickness / 1000), 2) / (Math.pow(m_dInnerDiameter / 2, 2) - Math.pow((m_dInnerDiameter / 2) - (m_dFilmThickness / 1000), 2));
    	double dBeta1Beta2 = dBeta1 / dBeta2;

		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dXPosition = 0;
		double dXPositionError = 0;
		double dLastXPosition = 0;
		double dXMovement = 0;
		Boolean bIsEluted = false;
		double dTcA = 0;
		double dTcB = 0;
		double dCurVal = 0;
		
		// Grab the first temp
		dTcA = m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_InterpolatedTemperatureDifferenceProfile.getAt(0);

		for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += m_dtstep)
		{
			// Grab the second temp
			dTcB = m_InterpolatedSimpleTemperatureProfile.getAt(t + m_dtstep) + m_InterpolatedTemperatureDifferenceProfile.getAt(t + m_dtstep);
			
			// Find the average of the two temps
			double dTc = (dTcA + dTcB) / 2;
			
			// Get the hold-up time at this temp
			double dHc = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
			// Get the amount of dead time traveled in dtstep
			
			double dk = calckfromT(dCompoundParams, dTc) * dBeta1Beta2;
			dCurVal = m_dtstep / (1 + dk);
			// Determine what fraction of the column it moved
			dXMovement = dCurVal / dHc;
			
			// Add that to the running total
			dLastXPosition = dXPosition;
			dXPosition += dXMovement;
			
			// Now calculate error
			// First, determine error in k from error in T
			//double dTError = 0.13; // sigma in deg C
			//double dkerror = Math.abs((Math.pow(10, IsocraticData.getAt(dTc + dTError)) * dBeta1Beta2) - dk);
			
			// Assume error in k from measurement itself is small.
			
			double dXMovementErrorFraction = (dk * 0.00565) / (1 + dk);
			//double dXMovementErrorFraction = (dkerror) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;
			
			/*if (dXPosition + dXPositionError >= 1 && !bSigmaEluted)
			{
				dtRErrorFinal = (((1 - dLastXPosition)/((dXPosition + dXPositionError) - dLastXPosition)) * m_dtstep) + t;
				bSigmaEluted = true;
			}*/			
			
			if (dXPosition >= 1)
			{
				dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * m_dtstep) + t;
				//dtRErrorFinal = ((1 - dLastXPosition)/(dXPosition - dLastXPosition) * m_dtstep) + t;
				double dxdt = (dXPosition - dLastXPosition) / m_dtstep;
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);		
				bIsEluted = true;
				break;
			}
			
			dTcA = dTcB;
		}
		
		if (bIsEluted)
		{
			pro.dPredictedRetentionTime = dtRFinal;
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
        // Main task. Executed in background thread.
        
    	@Override
    	public Void doInBackground()
    	{
    		int iNumCompounds = Globals.CompoundParamArray.length;
    		
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

				m_PredictedRetentionTimes[iCompound] = predictRetention2(Globals.CompoundParamArray[iCompound]);
    		}
    		
    		updatePredictionsTable();
    		
    		return null;
    	}
    	
        /*public Void doInBackground() 
        {
    		NumberFormat formatter1 = new DecimalFormat("#0.000");
    		
        	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
        	double dBeta2 = Math.pow((m_dInnerDiameter / 2) - (m_dFilmThickness / 1000), 2) / (Math.pow(m_dInnerDiameter / 2, 2) - Math.pow((m_dInnerDiameter / 2) - (m_dFilmThickness / 1000), 2));
        	double dBeta1Beta2 = dBeta1 / dBeta2;

        	m_dtstep = m_dPlotXMax2 * 0.00001;//0.01;

    		int iNumCompounds = Globals.CompoundIsothermalDataArray.length;
    		for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
    		{
				if (taskPredict.isCancelled())
				{
					return null;
				}

				InterpolationFunction IsocraticData = new InterpolationFunction(Globals.CompoundIsothermalDataArray[iCompound]);
				
				double dtRFinal = 0;
				double dtRErrorFinal = 0;
				double dXPosition = 0;
				double dLastXPosition = 0;
				double dXMovement = 0;
				Boolean bIsEluted = false;
				Boolean bSigmaEluted = false;
				double dTcA = 0;
				double dTcB = 0;
				double dCurVal = 0;
				
				// Grab the first temp
				dTcA = m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_InterpolatedTemperatureDifferenceProfile.getAt(0);

				double dXPositionError = 0;
				
				for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += m_dtstep)
				{
					// Grab the second temp
					dTcB = m_InterpolatedSimpleTemperatureProfile.getAt(t + m_dtstep) + m_InterpolatedTemperatureDifferenceProfile.getAt(t + m_dtstep);
					
					// Find the average of the two temps
					double dTc = (dTcA + dTcB) / 2;
					
					// Get the hold-up time at this temp
					double dHc = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
					// Get the amount of dead time traveled in dtstep
					double dk = Math.pow(10, IsocraticData.getAt(dTc)) * dBeta1Beta2;
					
					dCurVal = m_dtstep / (1 + dk);
					// Determine what fraction of the column it moved
					dXMovement = dCurVal / dHc;
					
					// Add that to the running total
					dLastXPosition = dXPosition;
					dXPosition += dXMovement;
					
					double dXMovementErrorFraction = (dk * 0.00565) / (1 + dk);
					dXPositionError += dXMovement * dXMovementErrorFraction;
					
					if (dXPosition + dXPositionError >= 1 && !bSigmaEluted)
					{
						dtRErrorFinal = (((1 - dLastXPosition)/((dXPosition + dXPositionError) - dLastXPosition)) * m_dtstep) + t;
						bSigmaEluted = true;
					}
					
					if (dXPosition >= 1)
					{
						dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * m_dtstep) + t;
						bIsEluted = true;
						break;
					}
					
					dTcA = dTcB;
					
				}
				
    			if (bIsEluted)
    			{
    				contentPane2.tmPredictionModel.setValueAt(formatter1.format(dtRFinal), iCompound, 1);
    			}
    			else
    			{
    				contentPane2.tmPredictionModel.setValueAt("Did not elute", iCompound, 1);
    			}

    			contentPane2.tmPredictionModel.setValueAt(Float.toString((float)Globals.roundToSignificantFigures(dtRFinal - dtRErrorFinal, 2)), iCompound, 2);
    		}
    				
            return null;
        }*/
        
    
        @Override
        public void done() 
        {
    		//contentPane2.jProgressBar2.setIndeterminate(false);
    		contentPane2.jProgressBar2.setStringPainted(true);
    		contentPane2.jProgressBar2.setString("Calculations complete. Now copy report to clipboard.");
    		contentPane2.jbtnPredict.setActionCommand("Copy to clipboard");
    		contentPane2.jbtnPredict.setText("Copy report to clipboard");
    		contentPane2.jbtnPredict.setEnabled(true);
        }
    }
    
 /*   class TaskPredict extends SwingWorker<Void, Void> 
    {
        // Main task. Executed in background thread.
        
    	@Override
        public Void doInBackground() 
        {
    		NumberFormat formatter1 = new DecimalFormat("#0.000");
    		NumberFormat formatter2 = new DecimalFormat("#0.0000");
    		
    		double dPercentErrorInk = 0.042;
    		
    		int iNumCompounds = Globals.CompoundIsothermalDataArray.length;
    		for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
    		{
				if (taskPredict.isCancelled())
				{
					return null;
				}

				InterpolationFunction IsocraticData = new InterpolationFunction(Globals.CompoundIsothermalDataArray[iCompound]);
				
				double dtRFinal1 = 0;
				double dtRFinal2 = 0;
				double dXPosition = 0;
				double dLastXPosition = 0;
				double dXMovement = 0;
				Boolean bIsEluted = false;
				double dTcA = 0;
				double dTcB = 0;
				double dCurVal = 0;
				
				// {low temp, high temp, fraction, abs error in tr contributed by this segment}
				double[][] dFractionkMovedCompound = {
						{0,70,0,0,0},
						{70,90,0,0,0},
						{90,110,0,0,0},
						{110,130,0,0,0},
						{130,150,0,0,0},
						{150,170,0,0,0},
						{170,190,0,0,0},
						{190,210,0,0,0},
						{210,230,0,0,0},
						{230,250,0,0,0},
						{250,270,0,0,0},
						{270,290,0,0,0},
						{290,310,0,0,0},
						{310,800,0,0,0}};
				
				// Grab the first temp
				dTcA = m_InterpolatedTemperatureProfile.getAt(0);
				boolean bInTransferLine = false;

				for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += m_dtstep)
				{
					// Grab the second temp
					dTcB = m_InterpolatedTemperatureProfile.getAt(t + m_dtstep);
					
					// Find the average of the two temps
					double dTc = (dTcA + dTcB) / 2;
					
					// Figure out which k value we're using
					int thisIndex = 0;
					for (int i = 0; i < dFractionkMovedCompound.length; i++)
					{
						if (dFractionkMovedCompound[i][0] <= dTc && dFractionkMovedCompound[i][1] > dTc)
						{
							thisIndex = i;
							break;
						}
					}

					// Get the hold-up time at this temp
					double dHc = m_InterpolatedHoldUp.getAt(dTc);
					double dH2c = m_InterpolatedTm2ToTmTotVsTemperature.getAt(dTc) * dHc;
					double dH1c = dHc - dH2c;
					dH1c /= 60;
					dH2c /= 60;

					if (!bInTransferLine)
					{
						// Get the amount of dead time traveled in dtstep
						dCurVal = m_dtstep / (1 + Math.pow(10, IsocraticData.getAt(dTc)));
						// Determine what fraction of the column it moved
						dXMovement = dCurVal / dH1c;
					}
					else
					{
						// Get the amount of dead time traveled in dtstep
						dCurVal = m_dtstep / (1 + Math.pow(10, IsocraticData.getAt(m_dTransferLineTemperature)));
						// Determine what fraction of the column it moved
						dXMovement = dCurVal / dH2c;
					}

					dFractionkMovedCompound[thisIndex][2] += dXMovement;
					double dBigFraction = (dFractionkMovedCompound[thisIndex][2] - dXMovement) / dFractionkMovedCompound[thisIndex][2];
					double dSmallFraction = 1 - dBigFraction;
					// TODO: fix the following two lines to account for transfer line
					dFractionkMovedCompound[thisIndex][3] = (dFractionkMovedCompound[thisIndex][3] * dBigFraction) + ((Math.pow(10, IsocraticData.getAt(dTc)) * dHc * (dPercentErrorInk / 100)) * dSmallFraction);
					dFractionkMovedCompound[thisIndex][4] = (dFractionkMovedCompound[thisIndex][4] * dBigFraction) + (Math.pow(10, IsocraticData.getAt(dTc)) * dSmallFraction);
					                                   
					// Add that to the running total
					dLastXPosition = dXPosition;
					dXPosition += dXMovement;
					
					if (dXPosition >= 1)
					{
						if (!bInTransferLine)
						{
							dtRFinal1 = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * m_dtstep) + t;
							bInTransferLine = true;
							dLastXPosition = 0;
							dXPosition = 0;
							t = -m_dtstep; // On the next loop, t will start at 0 again.
						}
						else
						{
							dtRFinal2 = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * m_dtstep) + t;
							bIsEluted = true;
							break;
						}
					}
					
					dTcA = dTcB;
				}
				
				double dtRFinal = dtRFinal1 + dtRFinal2;
				
    			if (bIsEluted)
    			{
    				contentPane2.tmPredictionModel.setValueAt(formatter1.format(dtRFinal), iCompound, 1);
    			}
    			else
    			{
    				contentPane2.tmPredictionModel.setValueAt("Did not elute", iCompound, 1);
    			}

    			// Now calculate final error in the projection
    			double dFinalError = 0;
    			double dkavg = 0;
				for (int i = 0; i < dFractionkMovedCompound.length; i++)
				{
					dFinalError += Math.pow(dFractionkMovedCompound[i][3] * dFractionkMovedCompound[i][2], 2);
					dkavg += dFractionkMovedCompound[i][4] * dFractionkMovedCompound[i][2];
				}
				dFinalError = Math.sqrt(dFinalError);
				contentPane2.tmPredictionModel.setValueAt(formatter2.format(dFinalError), iCompound, 2);
    		}
    				
            return null;
        }
        
        @Override
        public void done() 
        {
    		contentPane2.jProgressBar2.setIndeterminate(false);
    		contentPane2.jProgressBar2.setStringPainted(true);
    		contentPane2.jProgressBar2.setString("Retention predictions complete.");
    		
    		contentPane2.jbtnPredict.setText("Copy Report to Clipboard");
    		contentPane2.jbtnPredict.setActionCommand("Copy Report to Clipboard");
    		contentPane2.jbtnPredict.setEnabled(true);
        }
    }*/
    
    /*public double getFlowRate(double dTime)
    {
    	double dFlowRate = (this.m_InterpolatedFlowRate.getAt(dTime) * this.m_InterpolatedFlowRateVsTempProfile.getAt(this.m_InterpolatedTemperatureProfile.getAt(dTime))) + this.m_InterpolatedFlowRateVsTempProfile.getAt(this.m_InterpolatedTemperatureProfile.getAt(dTime));
    	return dFlowRate;
    }*/
    
    /*public double calcRetentionError(double dtstep, int iNumCompoundsToInclude)
    {
		//NumberFormat formatter = new DecimalFormat("#0.0000");
    	double dRetentionError = 0;
		
		for (int iCompound = 0; iCompound < iNumCompoundsToInclude; iCompound++)
		{
			double dtRFinal = 0;
			double dXPosition = 0;
			double dLastXPosition = 0;
			double dXMovement = 0;
			boolean bIsEluted = false;
			double dTcA = 0;
			double dTcB = 0;
			double dCurVal = 0;
			
			dTcA = m_InterpolatedTemperatureProfile.getAt(0);
			
			for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += dtstep)
			{
				dTcB = m_InterpolatedTemperatureProfile.getAt(t + dtstep);

				double dTc = (dTcA + dTcB) / 2;
				double dHc = m_InterpolatedHoldUp.getAt(dTc); // Get current total hold-up time at this temperature
				dHc /= 60;
				
				dCurVal = dtstep / (1 + Math.pow(10, m_AlkaneIsothermalDataInterpolated[iCompound].getAt(dTc)));
				dXMovement = dCurVal / dHc;
				
				dLastXPosition = dXPosition;
				dXPosition += dXMovement;
				
				if (dXPosition >= 1)
				{
					dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * dtstep) + t;
					bIsEluted = true;
					break;
				}
				
				dTcA = dTcB;
			}
			
			if (bIsEluted)
			{
				dRetentionError += Math.pow(dtRFinal - (Double)m_vectCalCompounds.get(iCompound)[1], 2);
				m_vectCalCompounds.get(iCompound)[2] = dtRFinal;
				//contentPane2.tmOutputModel.setValueAt(formatter.format(dtRFinal), iCompound, 2);
				//contentPane2.tmOutputModel.setValueAt(formatter.format(dtRFinal - (Double)m_vectCalCompounds.get(iCompound)[1]), iCompound, 3);
			}
			else
			{
				dRetentionError += Math.pow((Double)m_vectCalCompounds.get(iCompound)[1], 2);
				m_vectCalCompounds.get(iCompound)[2] = -1;
				//contentPane2.tmOutputModel.setValueAt("Did not elute", iCompound, 2);
				//contentPane2.tmOutputModel.setValueAt("-", iCompound, 3);
			}
		}
				
    	return dRetentionError;
    }*/
    
/*    public double calcRetentionError(double dtstep, int iNumCompoundsToInclude)
    {
		//NumberFormat formatter = new DecimalFormat("#0.0000");
    	double dRetentionError = 0;
		
		for (int iCompound = 0; iCompound < iNumCompoundsToInclude; iCompound++)
		{
			double dtRFinal1 = 0;
			double dtRFinal2 = 0;
			double dXPosition = 0;
			double dLastXPosition = 0;
			double dXMovement = 0;
			boolean bIsEluted = false;
			double dTcA = 0;
			double dTcB = 0;
			double dCurVal = 0;
			
			dTcA = m_InterpolatedTemperatureProfile.getAt(0);
			boolean bInTransferLine = false;
			
			for (double t = 0; t <= (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5; t += dtstep)
			{
				dTcB = m_InterpolatedTemperatureProfile.getAt(t + dtstep);

				double dTc = (dTcA + dTcB) / 2;
				double dHc = m_InterpolatedHoldUp.getAt(dTc); // Get current total hold-up time at this temperature
				double dH2c = this.m_InterpolatedTm2ToTmTotVsTemperature.getAt(dTc) * dHc;
				double dH1c = dHc - dH2c;
				dH1c /= 60;
				dH2c /= 60;
				
				if (!bInTransferLine)
				{
					dCurVal = dtstep / (1 + Math.pow(10, m_AlkaneIsothermalDataInterpolated[iCompound].getAt(dTc)));
					dXMovement = dCurVal / dH1c;
				}
				else
				{
					dCurVal = dtstep / (1 + Math.pow(10, m_AlkaneIsothermalDataInterpolated[iCompound].getAt(this.m_dTransferLineTemperature)));
					dXMovement = dCurVal / dH2c;
				}
				
				dLastXPosition = dXPosition;
				dXPosition += dXMovement;
				
				if (dXPosition >= 1)
				{
					if (!bInTransferLine)
					{
						dtRFinal1 = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * dtstep) + t;
						bInTransferLine = true;
						dLastXPosition = 0;
						dXPosition = 0;
						t = -dtstep; // On the next loop, t will start at 0 again.
					}
					else
					{
						dtRFinal2 = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * dtstep) + t;
						bIsEluted = true;
						break;
					}
				}
				
				dTcA = dTcB;
			}
			
			double dtRFinal = dtRFinal1 + dtRFinal2;
			
			if (bIsEluted)
			{
				dRetentionError += Math.pow(dtRFinal - (Double)m_vectCalCompounds.get(iCompound)[1], 2);
				m_vectCalCompounds.get(iCompound)[2] = dtRFinal;
				//contentPane2.tmOutputModel.setValueAt(formatter.format(dtRFinal), iCompound, 2);
				//contentPane2.tmOutputModel.setValueAt(formatter.format(dtRFinal - (Double)m_vectCalCompounds.get(iCompound)[1]), iCompound, 3);
			}
			else
			{
				dRetentionError += Math.pow((Double)m_vectCalCompounds.get(iCompound)[1], 2);
				m_vectCalCompounds.get(iCompound)[2] = -1;
				//contentPane2.tmOutputModel.setValueAt("Did not elute", iCompound, 2);
				//contentPane2.tmOutputModel.setValueAt("-", iCompound, 3);
			}
		}
				
    	return dRetentionError;
    }*/
    
    public double calcRetentionError(double dtstep, int iNumCompoundsToInclude)
    {
    	double dRetentionError = 0;
		
    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
    	double dBeta2 = Math.pow((this.m_dInnerDiameter / 2) - (this.m_dFilmThickness / 1000), 2) / (Math.pow(this.m_dInnerDiameter / 2, 2) - Math.pow((this.m_dInnerDiameter / 2) - (this.m_dFilmThickness / 1000), 2));
    	double dBeta1Beta2 = dBeta1 / dBeta2;
    	
		double dtRFinal = 0;
		double dXPosition = 0;
		double dLastXPosition = 0;
		double dXMovement = 0;
		boolean bIsEluted = false;
		double dTcA = 0;
		double dTcB = 0;
		double dTc = 0;
		double dHc = 0;
		double dCurVal = 0;
		double dk = 0;
		
		// Create an array of temperatures - this will speed things up
		double dMaxTime = (Double) m_vectCalCompounds.get(m_vectCalCompounds.size() - 1)[1] * 1.5;
		int iNumSteps = (int)(dMaxTime / dtstep);
		double[] dTemps = new double[iNumSteps];
		for (int i = 0; i < iNumSteps; i++)
		{
			double dTime = (double)i * dtstep;
			dTemps[i] = m_InterpolatedSimpleTemperatureProfile.getAt(dTime) + m_InterpolatedTemperatureDifferenceProfile.getAt(dTime);
		}
		                  
		for (int iCompound = 0; iCompound < iNumCompoundsToInclude; iCompound++)
		{
			int iCompoundIndex = (Integer)m_vectCalCompounds.get(iCompound)[0];
			
			dtRFinal = 0;
			dXPosition = 0;
			dLastXPosition = 0;
			bIsEluted = false;

			dTcA = dTemps[0];
			
			for (int i = 0; i < iNumSteps - 1; i++)
			{
				dTcB = dTemps[i + 1];
				//dTcA = dTcB;

				dTc = (dTcA + dTcB) / 2;
				dHc = m_InterpolatedHoldUpProfile.getAt(dTc) / 60;
				
				dk = calckfromT(Globals.AlkaneParamArray[iCompoundIndex], dTc) * dBeta1Beta2;
				//dk = Math.pow(10, m_AlkaneIsothermalDataInterpolated[iCompound].getAt(dTc)) * dBeta1Beta2;
				dCurVal = dtstep / (1 + dk);
				
				dXMovement = dCurVal / dHc;
				
				dLastXPosition = dXPosition;
				dXPosition += dXMovement;
				
				if (dXPosition >= 1)
				{
					dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * dtstep) + ((double)i * dtstep);
					bIsEluted = true;
					break;
				}
				
				dTcA = dTcB;
			}
			
			if (bIsEluted)
			{
				dRetentionError += Math.pow(dtRFinal - (Double)m_vectCalCompounds.get(iCompound)[1], 2);
				m_vectCalCompounds.get(iCompound)[2] = dtRFinal;
				//contentPane2.tmOutputModel.setValueAt(formatter.format(dtRFinal), iCompound, 2);
				//contentPane2.tmOutputModel.setValueAt(formatter.format(dtRFinal - (Double)m_vectCalCompounds.get(iCompound)[1]), iCompound, 3);
			}
			else
			{
				dRetentionError += Math.pow((Double)m_vectCalCompounds.get(iCompound)[1], 2);
				m_vectCalCompounds.get(iCompound)[2] = -1.0;
				//contentPane2.tmOutputModel.setValueAt("Did not elute", iCompound, 2);
				//contentPane2.tmOutputModel.setValueAt("-", iCompound, 3);
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
    
    public double calcAngleDifferenceHoldUp(int iIndex)
    {
    	double dTotalAngleError = 0;
    	double dHoldUpRange = 20;
    	
    	for (int i = 0; i < this.m_dHoldUpArray.length; i++)
    	{
        	if (i < 2)
        		continue;
        	
        	double dTime2 = this.m_dHoldUpArray[i][0];
        	double dHoldUp2 = this.m_dHoldUpArray[i][1];
        	double dTime1 = this.m_dHoldUpArray[i - 1][0];
        	double dHoldUp1 = this.m_dHoldUpArray[i - 1][1];
        	double dTime0 = this.m_dHoldUpArray[i - 2][0];
        	double dHoldUp0 = this.m_dHoldUpArray[i - 2][1];
        	
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
    		
    		double dAngleError = Math.pow((Math.abs(dNewAngle - dPreviousAngle) / (Math.PI)), 2);
    		dTotalAngleError += dAngleError;
    	}
    	
     	return (dTotalAngleError * 10000) + 1;
    }
    
    public double calcAngleDifferenceTemp()
    {
    	double dTotalAngleError = 0;
    	double dMaxRampRate = 10;
    	
    	for (int i = 0; i < this.m_dTemperatureProfileDifferenceArray.length; i++)
    	{
        	if (i < 2)
        		continue;
        	
        	double dTime2 = this.m_dTemperatureProfileDifferenceArray[i][0];
        	double dTemp2 = this.m_dTemperatureProfileDifferenceArray[i][1];
        	double dTime1 = this.m_dTemperatureProfileDifferenceArray[i - 1][0];
        	double dTemp1 = this.m_dTemperatureProfileDifferenceArray[i - 1][1];
        	double dTime0 = this.m_dTemperatureProfileDifferenceArray[i - 2][0];
        	double dTemp0 = this.m_dTemperatureProfileDifferenceArray[i - 2][1];
        	
        	// Check if the previous point is a corner
        	// If it is, then don't worry about the angle - return 0
    		/*boolean bIsCorner = false;
    		
        	for (int j = 0; j < this.m_dIdealTemperatureProfileArray.length - 1; j++)
    		{
    			if (this.m_dIdealTemperatureProfileArray[j][0] == dTime1)
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
    		
    		double dAngleError = Math.pow((Math.abs(dNewAngle - dPreviousAngle) / (Math.PI)), 2);
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
    
 	public double goldenSectioningSearchSimpleTemperatureProfile(boolean bTmax, double dStep, double dPrecision, double dMaxChangeAtOnce)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		
		double dLastGuess;
		
		if (bTmax)
			dLastGuess = this.m_dTmax;
		else
			dLastGuess = this.m_dk;
		
		// Find bounds
		if (bTmax)
			x1 = m_dTmax;
		else
			x1 = m_dk;
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
		
		x2 = x1 + dStep;
		if (bTmax)
			m_dTmax = x2;
		else
			m_dk = x2;
		calcSimpleTemperatureProgram(m_dk, m_dTmax);
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

		if (dRetentionErrorX2 < dRetentionErrorX1)
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			if (bTmax)
				m_dTmax = x2;
			else
				m_dk = x2;
			calcSimpleTemperatureProgram(m_dk, m_dTmax);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

			while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;

				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				if (bTmax)
					m_dTmax = x2;
				else
					m_dk = x2;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			if (bTmax)
				m_dTmax = x1;
			else
				m_dk = x1;
			calcSimpleTemperatureProgram(m_dk, m_dTmax);
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

			while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;

				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				if (bTmax)
					m_dTmax = x1;
				else
					m_dk = x1;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastGuess + dMaxChangeAtOnce)
		{
			if (bTmax)
				m_dTmax = dLastGuess + dMaxChangeAtOnce;
			else
				m_dk = dLastGuess + dMaxChangeAtOnce;
			calcSimpleTemperatureProgram(m_dk, m_dTmax);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

			return dRetentionError;
		}
		
		if (x1 < dLastGuess - dMaxChangeAtOnce)
		{
			/*if (dLastGuess - dMaxChangeAtOnce < 0.00001)
			{
				if (bNonMixingVolume)
					m_dNonMixingVolume = 0.00001;
				else
					m_dMixingVolume = 0.00001;
			}
			else*/
			if (bTmax)
				m_dTmax = dLastGuess - dMaxChangeAtOnce;
			else
				m_dk = dLastGuess - dMaxChangeAtOnce;
			calcSimpleTemperatureProgram(m_dk, m_dTmax);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			
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

			if (bTmax)
				m_dTmax = x4;
			else
				m_dk = x4;
			calcSimpleTemperatureProgram(m_dk, m_dTmax);			
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			
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
		if (bTmax)
			m_dTmax = x3;
		else
			m_dk = x3;
		calcSimpleTemperatureProgram(m_dk, m_dTmax);			
		dRetentionError = dRetentionErrorX3;
 		
 		return dRetentionError;
 	}
 	
 	public double goldenSectioningSearchTemperatureProfile(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, /*boolean bMinimizeAngles,*/ double dAngleErrorMultiplier)
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
		
		double dLastTempGuess = m_dTemperatureProfileDifferenceArray[iIndex][1];
		
		// Find bounds
		x1 = m_dTemperatureProfileDifferenceArray[iIndex][1];
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
		dAngleErrorX1 = calcAngleDifferenceTemp();
		
		x2 = x1 + dStep;
		m_dTemperatureProfileDifferenceArray[iIndex][1] = x2;
		m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
		dAngleErrorX2 = calcAngleDifferenceTemp();
		
		//double dDiffPercent = (dRetentionErrorX2 - dRetentionErrorX1) / ((dRetentionErrorX2 + dRetentionErrorX1) / 2);
		//double dDiffPercent2 = (dAngleErrorX2 - dAngleErrorX1) / ((dAngleErrorX2 + dAngleErrorX1) / 2);
		//dDiffPercent2 *= dAngleErrorMultiplier;
		
		if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			dAngleErrorX3 = dAngleErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			m_dTemperatureProfileDifferenceArray[iIndex][1] = x2;
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			dAngleErrorX2 = calcAngleDifferenceTemp();

			//dDiffPercent = (dRetentionErrorX2 - dRetentionErrorX3) / ((dRetentionErrorX2 + dRetentionErrorX3) / 2);
			//dDiffPercent2 = (dAngleErrorX2 - dAngleErrorX3) / ((dAngleErrorX2 + dAngleErrorX3) / 2);
			//dDiffPercent2 *= dAngleErrorMultiplier;

			while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastTempGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;
				dAngleErrorX1 = dAngleErrorX3;
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				m_dTemperatureProfileDifferenceArray[iIndex][1] = x2;
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
				dAngleErrorX2 = calcAngleDifferenceTemp();
				
				//dDiffPercent = (dRetentionErrorX2 - dRetentionErrorX3) / ((dRetentionErrorX2 + dRetentionErrorX3) / 2);
				//dDiffPercent2 = (dAngleErrorX2 - dAngleErrorX3) / ((dAngleErrorX2 + dAngleErrorX3) / 2);
				//dDiffPercent2 *= dAngleErrorMultiplier;
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			dAngleErrorX3 = dAngleErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			m_dTemperatureProfileDifferenceArray[iIndex][1] = x1;
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			dAngleErrorX1 = calcAngleDifferenceTemp();

			//dDiffPercent = (dRetentionErrorX1 - dRetentionErrorX3) / ((dRetentionErrorX1 + dRetentionErrorX3) / 2);
			//dDiffPercent2 = (dAngleErrorX1 - dAngleErrorX3) / ((dAngleErrorX1 + dAngleErrorX3) / 2);
			//dDiffPercent2 *= dAngleErrorMultiplier;

			while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastTempGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;
				dAngleErrorX2 = dAngleErrorX3;
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				m_dTemperatureProfileDifferenceArray[iIndex][1] = x1;
				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
				dAngleErrorX1 = calcAngleDifferenceTemp();
				
				//dDiffPercent = (dRetentionErrorX1 - dRetentionErrorX3) / ((dRetentionErrorX1 + dRetentionErrorX3) / 2);
				//dDiffPercent2 = (dAngleErrorX1 - dAngleErrorX3) / ((dAngleErrorX1 + dAngleErrorX3) / 2);
				//dDiffPercent2 *= dAngleErrorMultiplier;
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastTempGuess + dMaxChangeAtOnce)
		{
			m_dTemperatureProfileDifferenceArray[iIndex][1] = dLastTempGuess + dMaxChangeAtOnce;
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

			return dRetentionError;
		}
		
		if (x1 < dLastTempGuess - dMaxChangeAtOnce)
		{
			m_dTemperatureProfileDifferenceArray[iIndex][1] = dLastTempGuess - dMaxChangeAtOnce;
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			
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

			m_dTemperatureProfileDifferenceArray[iIndex][1] = x4;
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			dAngleErrorX4 = calcAngleDifferenceTemp();
			
			//dDiffPercent = (dRetentionErrorX4 - dRetentionErrorX3) / ((dRetentionErrorX4 + dRetentionErrorX3) / 2);
			//dDiffPercent2 = (dAngleErrorX4 - dAngleErrorX3) / ((dAngleErrorX4 + dAngleErrorX3) / 2);
			//dDiffPercent2 *= dAngleErrorMultiplier;

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
		m_dTemperatureProfileDifferenceArray[iIndex][1] = x3;
		m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
		dRetentionError = dRetentionErrorX3;
 		
 		return dRetentionError;
 	}
 	
 	// m_dTemperatureProfileDifferenceArray is calculated from slope/intercept of calibration error
    // Calibration error equation is Tactual-Tnominal = mx + b
    //m_dCalibrationErrorSlope
    //m_dCalibrationErrorIntercept
 	public double goldenSectioningSearchTemperatureCalibrationError(boolean bSlope, double dStep, double dPrecision, double dMaxChangeAtOnce)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		
		double dLastGuess;
		
		if (bSlope)
			dLastGuess = this.m_dCalibrationErrorSlope;
		else
			dLastGuess = this.m_dCalibrationErrorIntercept;
		
		// Find bounds
		if (bSlope)
			x1 = m_dCalibrationErrorSlope;
		else
			x1 = m_dCalibrationErrorIntercept;
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
		
		x2 = x1 + dStep;
		if (bSlope)
			m_dCalibrationErrorSlope = x2;
		else
			m_dCalibrationErrorIntercept = x2;
		calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

		if (dRetentionErrorX2 < dRetentionErrorX1)
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			if (bSlope)
				m_dCalibrationErrorSlope = x2;
			else
				m_dCalibrationErrorIntercept = x2;
			calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

			while (dRetentionErrorX2 < dRetentionErrorX3 && x2 < dLastGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;

				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				if (bSlope)
					m_dCalibrationErrorSlope = x2;
				else
					m_dCalibrationErrorIntercept = x2;
				calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			if (bSlope)
				m_dCalibrationErrorSlope = x1;
			else
				m_dCalibrationErrorIntercept = x1;
			calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

			while (dRetentionErrorX1 < dRetentionErrorX3 && x1 > dLastGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;

				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				if (bSlope)
					m_dCalibrationErrorSlope = x1;
				else
					m_dCalibrationErrorIntercept = x1;
				calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastGuess + dMaxChangeAtOnce)
		{
			if (bSlope)
				m_dCalibrationErrorSlope = dLastGuess + dMaxChangeAtOnce;
			else
				m_dCalibrationErrorIntercept = dLastGuess + dMaxChangeAtOnce;
			calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

			return dRetentionError;
		}
		
		if (x1 < dLastGuess - dMaxChangeAtOnce)
		{
			/*if (dLastGuess - dMaxChangeAtOnce < 0.00001)
			{
				if (bNonMixingVolume)
					m_dNonMixingVolume = 0.00001;
				else
					m_dMixingVolume = 0.00001;
			}
			else*/
			if (bSlope)
				m_dCalibrationErrorSlope = dLastGuess - dMaxChangeAtOnce;
			else
				m_dCalibrationErrorIntercept = dLastGuess - dMaxChangeAtOnce;
			calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			
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

			if (bSlope)
				m_dCalibrationErrorSlope = x4;
			else
				m_dCalibrationErrorIntercept = x4;
			calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			
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
		if (bSlope)
			m_dCalibrationErrorSlope = x3;
		else
			m_dCalibrationErrorIntercept = x3;
		calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
		dRetentionError = dRetentionErrorX3;
 		
 		return dRetentionError;
 	}
 	
 	// offset moves the m_dTemperatureProfileDifferenceArray array up/down 
 	public double goldenSectioningSearchTemperatureProfileOffset(double dStep, double dPrecision, int iNumCompoundsToUse)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		
		// Find bounds
		x1 = m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1];
		dRetentionErrorX1 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
		
		x2 = x1 + dStep;
		
		double dPercentDiff = (x2 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
		for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
		{
			m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
		}
		
		m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
		dRetentionErrorX2 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
				
		if (dRetentionErrorX2 < dRetentionErrorX1)
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			dPercentDiff = (x2 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
			for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
			{
				m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
			}

			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
			
			while (dRetentionErrorX2 < dRetentionErrorX3)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;

				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				dPercentDiff = (x2 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
				for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
				{
					m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
				}

				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			dPercentDiff = (x1 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
			for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
			{
				m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
			}
			
			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX1 = calcRetentionError(m_dtstep, iNumCompoundsToUse);

			while (dRetentionErrorX1 < dRetentionErrorX3)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;

				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				dPercentDiff = (x1 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
				for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
				{
					m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
				}

				m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
			}
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

			dPercentDiff = (x4 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
			for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
			{
				m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
			}

			m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
			dRetentionErrorX4 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
			
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
		dPercentDiff = (x3 - (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1])) / (m_InterpolatedSimpleTemperatureProfile.getAt(0) + m_dTemperatureProfileDifferenceArray[0][1] + 273.15);
		for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
		{
			m_dTemperatureProfileDifferenceArray[i][1] += ((m_InterpolatedSimpleTemperatureProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0]) + m_dTemperatureProfileDifferenceArray[i][1] + 273.15) * dPercentDiff);
		}
		
		m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
		dRetentionError = dRetentionErrorX3;
 		
 		return dRetentionError;
 	}
 	
 	public double goldenSectioningSearchHoldUp(int iIndex, double dStep, double dPrecision, double dMaxChangeAtOnce, double dAngleErrorMultiplier)
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
		
		double dLastFGuess = m_dHoldUpArray[iIndex][1];

		// Find bounds
		x1 = this.m_dHoldUpArray[iIndex][1];
		dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
		dAngleErrorX1 = calcAngleDifferenceHoldUp(iIndex);
		
		x2 = x1 + dStep;
		m_dHoldUpArray[iIndex][1] = x2;
		this.m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
		dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
		dAngleErrorX2 = calcAngleDifferenceHoldUp(iIndex);
		
		if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			dAngleErrorX3 = dAngleErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			m_dHoldUpArray[iIndex][1] = x2;
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			dAngleErrorX2 = calcAngleDifferenceHoldUp(iIndex);

			while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastFGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;
				dAngleErrorX1 = dAngleErrorX3;
				
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				m_dHoldUpArray[iIndex][1] = x2;
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorX2 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
				dAngleErrorX2 = calcAngleDifferenceHoldUp(iIndex);
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			dAngleErrorX3 = dAngleErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			m_dHoldUpArray[iIndex][1] = x1;
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			dAngleErrorX1 = calcAngleDifferenceHoldUp(iIndex);

			while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastFGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;
				dAngleErrorX2 = dAngleErrorX3;
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;
				
				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				m_dHoldUpArray[iIndex][1] = x1;
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorX1 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
				dAngleErrorX1 = calcAngleDifferenceHoldUp(iIndex);
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastFGuess + dMaxChangeAtOnce)
		{
			m_dHoldUpArray[iIndex][1] = dLastFGuess + dMaxChangeAtOnce;
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			
			return dRetentionError;
		}
		
		if (x1 < dLastFGuess - dMaxChangeAtOnce)
		{
			m_dHoldUpArray[iIndex][1] = dLastFGuess - dMaxChangeAtOnce;
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());

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
			
			m_dHoldUpArray[iIndex][1] = x4;
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorX4 = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
			dAngleErrorX4 = calcAngleDifferenceHoldUp(iIndex);

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
		m_dHoldUpArray[iIndex][1] = x3;
		m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
		dRetentionError = dRetentionErrorX3;
		
		return dRetentionError;
 	}
    
 	public double goldenSectioningSearchHoldUpOffset(double dStep, double dPrecision, int iNumCompoundsToUse)
 	{
		double dRetentionError = 1;
 		double F1;
		double F2;
		double F3;
		double dRetentionErrorF1;
		double dRetentionErrorF2;
		double dRetentionErrorF3;
		
		// Find bounds
		F1 = m_dHoldUpArray[0][1];
		dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse);

		F2 = F1 + dStep;
		
		double dDiff = F2 - m_dHoldUpArray[0][1];
		for (int i = 0; i < m_dHoldUpArray.length; i++)
		{
			m_dHoldUpArray[i][1] += dDiff;
		}
		m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
		dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
		
		if (dRetentionErrorF2 < dRetentionErrorF1)
		{
			// We're going in the right direction
			F3 = F2;
			dRetentionErrorF3 = dRetentionErrorF2;
			
			F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
			
			dDiff = F2 - m_dHoldUpArray[0][1];
			for (int i = 0; i < m_dHoldUpArray.length; i++)
			{
				m_dHoldUpArray[i][1] += dDiff;
			}
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse);

			while (dRetentionErrorF2 < dRetentionErrorF3)
			{
				F1 = F3;
				dRetentionErrorF1 = dRetentionErrorF3;
				F3 = F2;
				dRetentionErrorF3 = dRetentionErrorF2;
				
				F2 = (F3 - F1) * this.m_dGoldenRatio + F3;
				
				dDiff = F2 - m_dHoldUpArray[0][1];
				for (int i = 0; i < m_dHoldUpArray.length; i++)
				{
					m_dHoldUpArray[i][1] += dDiff;
				}
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorF2 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
			}
		}
		else
		{
			// We need to go in the opposite direction
			F3 = F1;
			dRetentionErrorF3 = dRetentionErrorF1;
			
			F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
			
			dDiff = F1 - m_dHoldUpArray[0][1];
			for (int i = 0; i < m_dHoldUpArray.length; i++)
			{
				m_dHoldUpArray[i][1] += dDiff;
			}
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse);

			while (dRetentionErrorF1 < dRetentionErrorF3)
			{
				F2 = F3;
				dRetentionErrorF2 = dRetentionErrorF3;
				F3 = F1;
				dRetentionErrorF3 = dRetentionErrorF1;
				
				F1 = F3 - (F2 - F3) * this.m_dGoldenRatio;
				
				dDiff = F1 - m_dHoldUpArray[0][1];
				for (int i = 0; i < m_dHoldUpArray.length; i++)
				{
					m_dHoldUpArray[i][1] += dDiff;
				}
				m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
				dRetentionErrorF1 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
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

			
			dDiff = F4 - m_dHoldUpArray[0][1];
			for (int i = 0; i < m_dHoldUpArray.length; i++)
			{
				m_dHoldUpArray[i][1] += dDiff;
			}
			m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
			dRetentionErrorF4 = calcRetentionError(m_dtstep, iNumCompoundsToUse);
			
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
		
		dDiff = F3 - m_dHoldUpArray[0][1];
		for (int i = 0; i < m_dHoldUpArray.length; i++)
		{
			m_dHoldUpArray[i][1] += dDiff;
		}
		m_InterpolatedHoldUpProfile = new InterpolationFunction(m_dHoldUpArray);
		dRetentionError = dRetentionErrorF3;
		
		return dRetentionError;
 	}
 	
 	// Calculates m_dTemperatureProfileDifferenceArray from dTemperatureCalibrationSlope and dTemperatureCalibrationIntercept
 	// Tactual - Tnominal = m*Tnominal + b
 	public void calcTemperatureDifferenceProfile(double dTemperatureCalibrationSlope, double dTemperatureCalibrationIntercept)
 	{
		for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
		{
			m_dTemperatureProfileDifferenceArray[i][1] = (dTemperatureCalibrationSlope * this.m_InterpolatedIdealTempProfile.getAt(m_dTemperatureProfileDifferenceArray[i][0])) + dTemperatureCalibrationIntercept;
		}
		
		this.m_InterpolatedTemperatureDifferenceProfile = new LinearInterpolationFunction(m_dTemperatureProfileDifferenceArray);
 	}
 	
 	// Calculates m_InterpolatedSimpleTemperatureProfile from dk and dTmax
 	public void calcSimpleTemperatureProgram(double dk, double dTmax)
 	{
		int iNumPoints = 1000;
		double dC = 0;
		
		double dFinalTime = this.m_dPlotXMax2; // in min
		double dTimeStep = dFinalTime / (iNumPoints - 1);
		
		// Create an array for the simple temperature program
		m_dSimpleTemperatureProfileArray = new double[iNumPoints][2];
		
		// Set the first point.
		m_dSimpleTemperatureProfileArray[0][0] = 0;
		m_dSimpleTemperatureProfileArray[0][1] = this.m_dIdealTemperatureProfileArray[0][1];
				
		for (int i = 0; i < iNumPoints - 1; i++)
		{
			double dt1 = (double)i * dTimeStep;
			double dt2 = (double)(i + 1) * dTimeStep;
			
			double dT1 = m_dSimpleTemperatureProfileArray[i][1];
			double dT2 = this.m_InterpolatedIdealTempProfile.getAt(dt2);
			
			double dIdealSlope = (dT2 - dT1) / dTimeStep;
			
			double dt1b = -Math.log(1 - ((dT1 - dC) / dTmax)) / dk;
			double dt2b = dt1b + dTimeStep;
			
			double dT2b = dTmax * (1 - Math.exp(-dk * dt2b)) + dC;
			
			double dLimitingSlope = (dT2b - dT1) / dTimeStep;
			
			m_dSimpleTemperatureProfileArray[i + 1][0] = dt2;
			if (dLimitingSlope < dIdealSlope)
			{
				m_dSimpleTemperatureProfileArray[i + 1][1] = dT2b;
			}
			else
			{
				m_dSimpleTemperatureProfileArray[i + 1][1] = dT2;
			}
		}
		
		this.m_InterpolatedSimpleTemperatureProfile = new LinearInterpolationFunction(m_dSimpleTemperatureProfileArray);
 	}
 	
    // Iterative - in sequence, *Golden* Sectioning Search algorithm
    // Start by optimizing the entire flow rate error profile.
	public void backCalculate(Task task, boolean bHoldUpProfileFirst)
	{
		//if (true)
		//	return;
		long starttime = System.currentTimeMillis();

		boolean bForwards = true;
		
		//m_dTemperatureArrayStore = new double[300][m_dTemperatureProfileDifferenceArray.length][2];
		//m_dHoldUpArrayStore = new double[300][m_dHoldUpArray.length][2];
		//m_dRetentionErrorStore = new double[300];
		
		// Phase I conditions:
		boolean bBackCalculateTempProfile = false;
		boolean bBackCalculateTempProfileOffset = true;
		boolean bBackCalculateTempCalibration = false;
		boolean bBackCalculateTempSimpleProfile = true;
		boolean bBackCalculateHoldUpProfile = false;
		boolean bBackCalculateHoldUpProfileOffset = true;
		double dTempProfileAngleWeight = 0;
		double dHoldUpProfileAngleWeight = 0;
		
		boolean bSkipTerminationTest = true;
		
		// TODO: change back to 0.01
    	m_dtstep = m_dPlotXMax2 * 0.01;

		NumberFormat formatter1 = new DecimalFormat("#0.000000");
		NumberFormat formatter2 = new DecimalFormat("0.0000E0");
		NumberFormat percentFormatter = new DecimalFormat("0.00");
		
		// Step #1: Create interpolating functions for the isocratic data of each gradient calibration solute
		//m_AlkaneIsothermalDataInterpolated = new InterpolationFunction[contentPane2.tmOutputModel.getRowCount()];
		
		//for (int i = 0; i < m_AlkaneIsothermalDataInterpolated.length; i++)
		//{
		//	Integer iIndex = (Integer) m_vectCalCompounds.get(i)[0];
		//	m_AlkaneIsothermalDataInterpolated[i] = new InterpolationFunction(Globals.AlkaneIsothermalDataArray[iIndex]);
		//}
		
				
		// Step #2: Begin back-calculation
		// If bHoldUpProfileFirst then back-calculate the hold up profile first, otherwise do the temperature profile first
		
		// TODO: put back to phase 1
		int iPhase = 1;
		int iIteration = 0;
		double dLastFullIterationError = 0;
		double dRetentionError = 0;
		
		while (true)
		{
			iIteration++;
			contentPane2.jlblIterationNumber.setText(((Integer)iIteration).toString());
			dLastFullIterationError = dRetentionError;
			
			//if (true)
			//	break;
			
			// Now we optimize the hold-up time vs. temp profile
			if (bBackCalculateHoldUpProfileOffset)
			{
				double dHoldUpStep = .1;
				double dHoldUpPrecision = .0001;
				int iNumCompoundsToUse = m_vectCalCompounds.size();

				dRetentionError = goldenSectioningSearchHoldUpOffset(dHoldUpStep, dHoldUpPrecision, iNumCompoundsToUse);
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
			
			if (bBackCalculateHoldUpProfile)
			{
				for (int iTimePoint = 0; iTimePoint < m_dHoldUpArray.length; iTimePoint++)
				{
					double dHoldUpStep = .1;
					double dHoldUpPrecision = .0001;
					double dMaxChangeAtOnce = 2;
					
					int iPoint;
					if (bForwards)
						iPoint = iTimePoint;
					else
						iPoint = m_dHoldUpArray.length - 1 - iTimePoint;

					dRetentionError = goldenSectioningSearchHoldUp(iPoint, dHoldUpStep, dHoldUpPrecision, dMaxChangeAtOnce, dHoldUpProfileAngleWeight);
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
			
			if (bBackCalculateTempProfileOffset)
			{
				double dTempStep = .5;
				double dTempPrecision = 0.001;
				int iNumCompoundsToUse = m_vectCalCompounds.size();

				dRetentionError = goldenSectioningSearchTemperatureProfileOffset(dTempStep, dTempPrecision, iNumCompoundsToUse);
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
			
			if (bBackCalculateTempProfile)
			{
				for (int iTimePoint = 0; iTimePoint < m_dTemperatureProfileDifferenceArray.length; iTimePoint++)
				{
					double dTempStep = .5;
					double dMaxChangeAtOnce = 5;
					double dTempPrecision = 0.001;
					
					int iPoint;
					if (bForwards)
						iPoint = iTimePoint;
					else
						iPoint = m_dTemperatureProfileDifferenceArray.length - 1 - iTimePoint;
						
					dRetentionError = goldenSectioningSearchTemperatureProfile(iPoint, dTempStep, dTempPrecision, dMaxChangeAtOnce, dTempProfileAngleWeight);

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
			
			// Try several values of m_dCalibrationErrorSlope and m_dCalibrationErrorIntercept to find the combination that gives the best accuracy
			if (bBackCalculateTempCalibration)
			{
				// Find the rough dk and dTmax that fit the best
				double dBestSlope = this.m_dCalibrationErrorSlope;
				double dBestIntercept = this.m_dCalibrationErrorIntercept;
				double dBestError = dRetentionError;

				for (double dSlope = -0.03; dSlope <= 0.03; dSlope += 0.002)
				{
					for (double dIntercept = -10; dIntercept <= 10; dIntercept += 0.5)
					{
						calcTemperatureDifferenceProfile(dSlope, dIntercept);
						dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
						
						if (dRetentionError < dBestError)
						{
							dBestSlope = dSlope;
							dBestIntercept = dIntercept;
							dBestError = dRetentionError;
						}
					}
				}
				
				dRetentionError = dBestError;
				this.m_dCalibrationErrorSlope = dBestSlope;
				this.m_dCalibrationErrorIntercept = dBestIntercept;
				calcTemperatureDifferenceProfile(m_dCalibrationErrorSlope, m_dCalibrationErrorIntercept);
				
				//double dStep = .1;
				//double dPrecision = 0.001;
				//double dMaxChangeAtOnce = 1;

				//dRetentionError = goldenSectioningSearchSimpleTemperatureProfile(false, dStep, dPrecision, dMaxChangeAtOnce);

				//dStep = 10;
				//dPrecision = 0.001;
				//dMaxChangeAtOnce = 100;
				
				//dRetentionError = goldenSectioningSearchSimpleTemperatureProfile(true, dStep, dPrecision, dMaxChangeAtOnce);

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

			// Try several values of k and Tmax to find the combination that gives the best accuracy
			if (bBackCalculateTempSimpleProfile)
			{
				// Find the rough dk and dTmax that fit the best
				double dBestTmax = this.m_dTmax;
				double dBestk = this.m_dk;
				double dBestError = dRetentionError;

				for (double dTmax = 400; dTmax <= 1000; dTmax = dTmax + 20)
				{
					for (double dk = 0.01; dk <= 1; dk = dk * 1.2)
					{
						calcSimpleTemperatureProgram(dk, dTmax);
						dRetentionError = calcRetentionError(m_dtstep, m_vectCalCompounds.size());
						
						if (dRetentionError < dBestError)
						{
							dBestk = dk;
							dBestTmax = dTmax;
							dBestError = dRetentionError;
						}
					}
				}
				
				dRetentionError = dBestError;
				this.m_dk = dBestk;
				this.m_dTmax = dBestTmax;
				calcSimpleTemperatureProgram(m_dk, m_dTmax);
				
				//double dStep = .1;
				//double dPrecision = 0.001;
				//double dMaxChangeAtOnce = 1;

				//dRetentionError = goldenSectioningSearchSimpleTemperatureProfile(false, dStep, dPrecision, dMaxChangeAtOnce);

				//dStep = 10;
				//dPrecision = 0.001;
				//dMaxChangeAtOnce = 100;
				
				//dRetentionError = goldenSectioningSearchSimpleTemperatureProfile(true, dStep, dPrecision, dMaxChangeAtOnce);

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
			
			//bForwards = !bForwards;
				
			String str;
			double dNum = dRetentionError / m_vectCalCompounds.size();
			
			if (dNum == 0)
				str = "";
			else if (dNum < 0.0001)
				str = formatter2.format(dNum);
			else
				str = formatter1.format(dNum);
			
			contentPane2.jlblLastVariance.setText(str);
			
			{
				// Save the new temperature profile
				/*for (int i = 0; i < m_dTemperatureProfileDifferenceArray.length; i++)
				{
					m_dTemperatureArrayStore[iIteration - 1][i][0] = m_dTemperatureProfileDifferenceArray[i][0];
					m_dTemperatureArrayStore[iIteration - 1][i][1] = m_dTemperatureProfileDifferenceArray[i][1];
				}
	
				// Save the new hold-up time profile
				for (int i = 0; i < m_dHoldUpArray.length; i++)
				{
					m_dHoldUpArrayStore[iIteration - 1][i][0] = m_dHoldUpArray[i][0];
					m_dHoldUpArrayStore[iIteration - 1][i][1] = m_dHoldUpArray[i][1];
				}
				
				// Save the retention error for this iteration
				m_dRetentionErrorStore[iIteration - 1] = dRetentionError;
				*/
			}
			
			// Calculate the percent improvement
			if (!bSkipTerminationTest)//dLastFullIterationError != 0)
			{
				double dPercentImprovement = (1 - (dRetentionError / dLastFullIterationError)) * 100;
				contentPane2.jlblPercentImprovement.setText(percentFormatter.format(dPercentImprovement) + "%");
				
				if (iPhase == 1)
				{
					if (dPercentImprovement < 2 && dPercentImprovement >= 0)
					{
						iPhase = 2;
						contentPane2.jlblPhase.setText("II");
						
						bBackCalculateTempProfileOffset = false;
						bBackCalculateTempCalibration = false;
						bBackCalculateTempSimpleProfile = false;
						bBackCalculateTempProfile = true;
						bBackCalculateHoldUpProfileOffset = false;
						bBackCalculateHoldUpProfile = true;
						dTempProfileAngleWeight = 20;
						dHoldUpProfileAngleWeight = 2;
						bSkipTerminationTest = true;
				    	//m_dtstep = m_dPlotXMax2 * 0.001;
					}
				}
				else if (iPhase == 2)
				{
					if (dPercentImprovement < 1 && dPercentImprovement >= 0)
					{
						if (Globals.MEASUREKDATA)
							iPhase = 3;
						else
							iPhase = 7;
						
						contentPane2.jlblPhase.setText("III");
						bBackCalculateTempProfileOffset = false;
						bBackCalculateTempCalibration = false;
						bBackCalculateTempSimpleProfile = false;
						bBackCalculateTempProfile = true;
						bBackCalculateHoldUpProfileOffset = false;
						bBackCalculateHoldUpProfile = true;
						dTempProfileAngleWeight = 0.25;
						dHoldUpProfileAngleWeight = 0.25;
						bSkipTerminationTest = true;
				    	m_dtstep = m_dPlotXMax2 * 0.001;
					}
				}
				else if (iPhase == 3)
				{
					if (dPercentImprovement < 1 && dPercentImprovement >= 0)
					{
						iPhase = 7;
						contentPane2.jlblPhase.setText("IV");
						bBackCalculateTempProfileOffset = false;
						bBackCalculateTempCalibration = false;
						bBackCalculateTempSimpleProfile = false;
						bBackCalculateTempProfile = true;
						bBackCalculateHoldUpProfileOffset = false;
						bBackCalculateHoldUpProfile = true;
						dTempProfileAngleWeight = 0.25;
						dHoldUpProfileAngleWeight = 0.25;
						bSkipTerminationTest = true;
				    	//m_dtstep = m_dPlotXMax2 * 0.0001;
				    	m_dtstep = 0.005;
					}
				}
				else if (iPhase == 4)
				{
					if (dPercentImprovement < 2 && dPercentImprovement >= 0)
					{
						iPhase = 5;
						contentPane2.jlblPhase.setText("V");
						bBackCalculateTempProfileOffset = false;
						bBackCalculateTempCalibration = false;
						bBackCalculateTempSimpleProfile = false;
						bBackCalculateTempProfile = true;
						bBackCalculateHoldUpProfileOffset = false;
						bBackCalculateHoldUpProfile = true;
						dTempProfileAngleWeight = .05;
						dHoldUpProfileAngleWeight = 0.25;
						bSkipTerminationTest = true;
					}
				}
				else if (iPhase == 5)
				{
					if (dPercentImprovement < 1 && dPercentImprovement >= 0)
					{
						iPhase = 6;
						contentPane2.jlblPhase.setText("VI");
						bBackCalculateTempProfile = true;
						bBackCalculateHoldUpProfile = true;
						dTempProfileAngleWeight = 0.25;
						dHoldUpProfileAngleWeight = 0.25;
						bSkipTerminationTest = true;
					}
				}
				else if (iPhase == 6)
				{
					if (dPercentImprovement < 1 && dPercentImprovement >= 0)
					{
						bSkipTerminationTest = true;
						
						iPhase = 7;
						contentPane2.jlblPhase.setText("VII");
						bBackCalculateTempProfile = true;
						bBackCalculateHoldUpProfile = true;
						dTempProfileAngleWeight = 0;
						dHoldUpProfileAngleWeight = 0.25;
				    	m_dtstep = m_dPlotXMax2 * 0.001;//0.001;
					}					
				}
				else
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
	}
	
    public void copyProfilesToClipboard()
    {
    	// Make a string that can be pasted into Excel
    	String outString = "";
    	String eol = System.getProperty("line.separator");
    	// First create the heading
    	// Programmed conditions
    	outString += "Programmed (initial) experimental conditions" + eol;
    	outString += "Stationary phase:\t" + Globals.StationaryPhaseArray[this.contentPane.jcboStationaryPhase.getSelectedIndex()] + eol;
    	outString += "Column inner diameter:\t" + this.contentPane.jtxtInnerDiameter.getText() + " mm" + eol;
    	outString += "Column length:\t" + this.contentPane.jtxtColumnLength.getText() + " m" + eol;
    	
    	if (contentPane.jrdoConstantFlowRate.isSelected())
    		outString += "Flow rate:\t" + this.contentPane.jtxtFlowRate.getText() + " mL/min" + eol;
    	else
    		outString += "Inlet pressure:\t" + this.contentPane.jtxtPressure.getText() + " kPa" + eol;
    	
    	if (contentPane.jrdoVacuum.isSelected())
    		outString += "Outlet pressure:\t" + " vacuum" + eol;
    	else
    		outString += "Outlet pressure:\t" + this.contentPane.jtxtOtherPressure.getText() + " kPa" + eol;

    	outString += "Temperature program:" + eol;
    	outString += "Initial temperature (°C):\t" + this.contentPane.jtxtInitialTemperature.getText() + eol;
    	outString += "Initial time (min):\t" + this.contentPane.jtxtInitialTime.getText() + eol;
    	outString += "Ramp rate (°C/min)\tFinal temperature (°C)\tHold time (min)" + eol;
    	for (int i = 0; i < this.contentPane.tmTemperatureProgram.getRowCount(); i++)
    	{
    		outString += contentPane.tmTemperatureProgram.getValueAt(i, 0) + "\t" + contentPane.tmTemperatureProgram.getValueAt(i, 1) + "\t" + contentPane.tmTemperatureProgram.getValueAt(i, 2) + eol;
    	}
    	outString += eol;
    	
    	// Standards, experimental, calculated, and error
    	outString += "Retention times of standards" + eol;
    	outString += "Standard\tExperimental retention time (min)\tCalculated retention time (min)\tDifference (min)" + eol;
    	for (int i = 0; i < this.contentPane2.tmOutputModel.getRowCount(); i++)
    	{
    		outString += this.contentPane2.tmOutputModel.getValueAt(i, 0) + "\t" + this.contentPane2.tmOutputModel.getValueAt(i, 1) + "\t" + this.contentPane2.tmOutputModel.getValueAt(i, 2) + "\t" + this.contentPane2.tmOutputModel.getValueAt(i, 3) + eol;
    	}
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

    	outString += "Back-calculated temperature profile\t\t\tBack-calculated hold-up time profile" + eol;
    	outString += "Time (min)\tTemperature (°C)\t\tTemperature (°C)\tHold-up time (min)" + eol;
    	
	    double dMinTemp = this.m_dHoldUpArray[0][0];
	    double dMaxTemp = this.m_dHoldUpArray[m_dHoldUpArray.length - 1][0];
	    
    	int iNumPoints = 1000;
    	for (int i = 0; i < iNumPoints; i++)
    	{
    		double dCurrentTime = this.m_dPlotXMax2 * ((double)i / ((double)iNumPoints - 1));
    		double dCurrentTemperature = this.m_InterpolatedSimpleTemperatureProfile.getAt(dCurrentTime) + this.m_InterpolatedTemperatureDifferenceProfile.getAt(dCurrentTime);
    		
    		double dHoldUpTemp = (((double)i / (double)(iNumPoints - 1)) * (dMaxTemp - dMinTemp)) + dMinTemp;
    		double dCurrentHoldUpTime = m_InterpolatedHoldUpProfile.getAt(dHoldUpTemp);
    		
    		outString += Float.toString((float)dCurrentTime) + "\t" + Float.toString((float)dCurrentTemperature) + "\t\t" + Float.toString((float)dHoldUpTemp) + "\t" + Float.toString((float)dCurrentHoldUpTime) + eol;
    	}
    	
    	StringSelection stringSelection = new StringSelection(outString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
        
        JOptionPane.showMessageDialog(null, "A report has been copied to the clipboard. You may now paste it into a spreadsheet.", "GC Retention Predictor", JOptionPane.INFORMATION_MESSAGE);
    }

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
		
	}
}
