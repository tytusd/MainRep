package org.hplcsimulator.methodcreator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;






//import javax.help.CSH;
//import javax.help.HelpSet;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A Transferable able to transfer an AWT Image.
 * Similar to the JDK StringSelection class.
 */
class ImageSelection implements Transferable {
    private Image image;
   
    public static void copyImageToClipboard(Image image) {
        ImageSelection imageSelection = new ImageSelection(image);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.getSystemClipboard().setContents(imageSelection, null);
    }
   
    public ImageSelection(Image image) {
        this.image = image;
    }
   
	@Override
    public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.imageFlavor) == false) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
   
	@Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.imageFlavor);
    }
   
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {
            DataFlavor.imageFlavor
        };
    }
}

class Compound implements Serializable
{
	private static final long serialVersionUID = 2L;
	
	String strCompoundName;
	double dConcentration;
	//double[] dLogkwvsTSlope;
	//double[] dLogkwvsTIntercept;
	//double[] dSvsTSlope;
	//double[] dSvsTIntercept;
	double[][] dLogkvsPhi;
	InterpolationFunction interpolatedLogkvsPhi;
	double dMolarVolume;
	int iStationaryPhase;
	
	double dRetentionTime;
	double dSigma;
	double dW;
	int iCompoundIndex;
	
	public Compound()
	{

		
	}
	
	public void createRandomCompound(String strName, double dMinConcentration, double dMaxConcentration)
	{
		Random rand = new Random();

		iCompoundIndex = -1;
		strCompoundName = strName;
		dMolarVolume = (rand.nextDouble() * 100) + 90;
		
		
		double dLogConcentration = rand.nextDouble() * (Math.log10(dMaxConcentration / dMinConcentration)) + Math.log10(dMinConcentration);
		dConcentration = Math.pow(10, dLogConcentration);
	}
	
	public boolean loadCompoundInfo(int iStationaryPhase, int iIndex)
	{
		this.iCompoundIndex = iIndex;
		this.iStationaryPhase = iStationaryPhase;
		
		strCompoundName = Globals.CompoundNameArray[iIndex];
		
		dLogkvsPhi = Globals.LogKDataArray[iIndex];
		interpolatedLogkvsPhi = new InterpolationFunction(dLogkvsPhi);

		dMolarVolume = 200;//Globals.MolarVolumeArray[iStationaryPhase][iIndex];
		
		return true;
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeLong(this.serialVersionUID);
		
		out.writeObject(strCompoundName);
		out.writeDouble(dConcentration);
		
		out.writeObject(dLogkvsPhi);
		
		out.writeDouble(dMolarVolume);
		
		out.writeInt(iStationaryPhase);
		out.writeInt(iCompoundIndex);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		long lVersion = in.readLong();
		
		strCompoundName = (String)in.readObject();
		dConcentration = in.readDouble();
		
		dLogkvsPhi = (double[][])in.readObject();
		interpolatedLogkvsPhi = new InterpolationFunction(dLogkvsPhi);
		
		dMolarVolume = in.readDouble();
		
		iStationaryPhase = in.readInt();
		iCompoundIndex = in.readInt();
	}
}

public class HPLCMethodCreator extends JFrame implements ActionListener, ChangeListener, KeyListener, FocusListener, ListSelectionListener, AutoScaleListener, TableModelListener
{
	private static final long serialVersionUID = 1L;
	double dFileVersion = 1.15;

	private boolean m_bSliderUpdate = true;
    public final double m_dGoldenRatio = (1 + Math.sqrt(5)) / 2;

	TopPanel contentPane = null;
	public JScrollPane jMainScrollPane = null;
	public int m_iSecondPlotType = 0;
	public double m_dTemperature = 25;
	public boolean m_bGradientMode = false;
	public double m_dSolventBFraction = 0.5;
	public double m_dMixingVolume = 200; /* in uL */
	public double m_dNonMixingVolume = 200; /* in uL */
	public double m_dColumnLength = 100;
	public double m_dColumnDiameter = 4.6;
	public double m_dInterparticlePorosity = 0.4;
	public double m_dIntraparticlePorosity = 0.4;
	public double m_dTotalPorosity = 0.64;
	public double m_dFlowRate = 2; /* in mL/min */
	public double m_dVoidVolume;
	public double m_dVoidTime;
	public double m_dOpenTubeVelocity;
	public double m_dInterstitialVelocity;
	public double m_dChromatographicVelocity;
	public double m_dParticleSize = 5;
	public double m_dDiffusionCoefficient = 0.00001;
	public double m_dATerm = 1;
	public double m_dBTerm = 5;
	public double m_dCTerm = 0.05;
	public double m_dReducedFlowVelocity;
	public double m_dReducedPlateHeight;
	public double m_dTheoreticalPlates;
	public double m_dHETP;
	public double m_dInjectionVolume = 5; //(in uL)
	public double m_dTimeConstant = 0.5;
	public double m_dStartTime = 0;
	public double m_dEndTime = 0;
	public double m_dNoise = 3;
	public double m_dSignalOffset = 30;
	public int m_iNumPoints = 3000;
	public double m_dEluentViscosity = 1;
	public double m_dBackpressure = 400;
	public double m_dTubingPressureDrop = 100;
	public int m_iSolventB = 0; // 0 = Acetonitrile, 1 = Methanol
	public boolean m_bDoNotChangeTable = false;
	public Vector<Compound> m_vectCompound = new Vector<Compound>();
	public double[][] m_dGradientArray;
	public LinearInterpolationFunction m_lifGradient = null;
	public int m_iChromatogramPlotIndex = -1;
	public int[] m_iSinglePlotIndex = null;
	public int m_iSecondPlotIndex = -1;
	public Vector<double[]> m_vectRetentionFactorArray;
	public Vector<double[]> m_vectPositionArray;
	public double m_dSelectedIsocraticRetentionFactor = 0;
	public double m_dTubingLength = 0; /* in cm */
	public double m_dTubingDiameter = 5; /* in mil */
	//public double m_dTubingDelay = 0; /* in seconds */
	public int m_iStationaryPhase = 0; // the selected stationary phase
	public boolean m_bAutomaticTimeRange = true;
	public double m_dOptimizationMinTimeForLinearGradient = 0;
	public double m_dOptimizationMinTimeForOptimizedGradient = 0;
    public InterpolationFunction m_InterpolatedDeadTimeProfile;
    public double[][] m_DeadTimeArray;
    
	private boolean m_bDoNotMessage = false;
	
	// For gradient optimization (hidden function)
	public double[][] m_optGradient = null;
	
	// Menu items
    JMenuItem menuLoadSettingsAction = new JMenuItem("Load Settings");
    JMenuItem menuSaveSettingsAction = new JMenuItem("Save Settings");
    JMenuItem menuSaveSettingsAsAction = new JMenuItem("Save Settings As...");
    JMenuItem menuResetToDefaultValuesAction = new JMenuItem("Reset To Default Settings");
    JMenuItem menuExitAction = new JMenuItem("Exit");
    
    //JMenuItem menuHelpTopicsAction = new JMenuItem("Help Topics");
    JMenuItem menuAboutAction = new JMenuItem("About HPLC Method Creator");
    
    File m_currentFile = null;
    boolean m_bDocumentChangedFlag = false;

    public class JFileChooser2 extends JFileChooser
    {
		@Override
		public void approveSelection()
		{
		    File f = getSelectedFile();
		    if(f.exists() && getDialogType() == SAVE_DIALOG)
		    {
		        int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
		        switch(result){
		            case JOptionPane.YES_OPTION:
		                super.approveSelection();
		                return;
		            case JOptionPane.NO_OPTION:
		                return;
		            case JOptionPane.CANCEL_OPTION:
		                cancelSelection();
		                return;
		        }
		    }
		    super.approveSelection();
		}
	}
    
    
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
        HPLCMethodCreator frame = new HPLCMethodCreator("HPLC Method Creator");
        
		//java.net.URL url1 = ClassLoader.getSystemResource("org/hplcsimulator/images/icon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
	    Image img = kit.createImage(frame.getClass().getResource("/org/hplcsimulator/methodcreator/images/icon.png"));
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
	public HPLCMethodCreator(String str) 
	{
	    super(str);
	    
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
		/*String helpHS = "org/hplcsimulator/help/HPLCSimHelp.hs";
		ClassLoader cl = TopPanel.class.getClassLoader();
		try {
			URL hsURL = HelpSet.findHelpSet(cl, helpHS);
			Globals.hsMainHelpSet = new HelpSet(null, hsURL);
		} catch (Exception ee) {
			System.out.println( "HelpSet " + ee.getMessage());
			System.out.println("HelpSet "+ helpHS +" not found");
			return;
		}
		Globals.hbMainHelpBroker = Globals.hsMainHelpSet.createHelpBroker();
*/
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
        
        /*
        double[][] compoundsToAdd = {
        		{2, 50},
        		{3, 50},
        		{4, 50},
        		{5, 50},
        		{12, 50},
        		{14, 50},
        		{17, 50},
        		{18, 50},
        		{19, 50}
        };
        
        for (int i = 0; i < compoundsToAdd.length; i++)
        {
        	Compound compound = new Compound();
        	compound.loadCompoundInfo(m_iStationaryPhase, (int)compoundsToAdd[i][0]);
        	compound.dConcentration = compoundsToAdd[i][1];
        	this.m_vectCompound.add(compound);
        }
    	Compound compound1 = new Compound();
    	compound1.loadCompoundInfo(m_iStationaryPhase, 1);
    	compound1.dConcentration = 5;
    	this.m_vectCompound.add(compound1);
    	
    	Compound compound2 = new Compound();
    	compound2.loadCompoundInfo(m_iStationaryPhase, 19);
    	compound2.dConcentration = 25;
    	this.m_vectCompound.add(compound2);
    	
    	Compound compound3 = new Compound();
    	compound3.loadCompoundInfo(m_iStationaryPhase, 4);
    	compound3.dConcentration = 40;
    	this.m_vectCompound.add(compound3);

    	Compound compound4 = new Compound();
    	compound4.loadCompoundInfo(m_iStationaryPhase, 6);
    	compound4.dConcentration = 15;
    	this.m_vectCompound.add(compound4);

    	Compound compound5 = new Compound();
    	compound5.loadCompoundInfo(m_iStationaryPhase, 11);
    	compound5.dConcentration = 10;
    	this.m_vectCompound.add(compound5);

    	updateCompoundComboBoxes();

    	for (int i = 0; i < m_vectCompound.size(); i++)
    	{
        	// Add the table space for the compound. Fill it in later with performCalculations().
        	Vector<String> vectNewRow = new Vector<String>();
        	vectNewRow.add(m_vectCompound.get(i).strCompoundName);
        	vectNewRow.add(Float.toString((float)m_vectCompound.get(i).dConcentration));
        	vectNewRow.add("");
        	vectNewRow.add("");
        	vectNewRow.add("");
        	vectNewRow.add("");
        	vectNewRow.add("");
        	
        	contentPane.vectChemicalRows.add(vectNewRow);    		
    	}
    	*/
        
        loadDefaultsForStationaryPhase(0);
    	calculateGradient();
        performCalculations();
    }
    
    private void createGUI()
    {
    	// Creates a menubar for a JFrame
        JMenuBar menuBar = new JMenuBar();
        
        // Add the menubar to the frame
        setJMenuBar(menuBar);
        
        // Define and add two drop down menu to the menubar
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        // Create and add simple menu item to one of the drop down menu
        menuLoadSettingsAction.addActionListener(this);
        menuSaveSettingsAction.addActionListener(this);
        menuSaveSettingsAsAction.addActionListener(this);
        menuResetToDefaultValuesAction.addActionListener(this);
        menuExitAction.addActionListener(this);

        //menuHelpTopicsAction.addActionListener(this);
        menuAboutAction.addActionListener(this);
        
        fileMenu.add(menuLoadSettingsAction);
        fileMenu.add(menuSaveSettingsAction);
        fileMenu.add(menuSaveSettingsAsAction);
        fileMenu.addSeparator();
        fileMenu.add(menuResetToDefaultValuesAction);
        fileMenu.addSeparator();
        fileMenu.add(menuExitAction);
        
        //helpMenu.add(menuHelpTopicsAction);
        helpMenu.addSeparator();
        helpMenu.add(menuAboutAction);

        //Create and set up the content pane
    	jMainScrollPane = new JScrollPane();
    	jMainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	jMainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    	contentPane = new TopPanel();
        contentPane.setOpaque(true);
        jMainScrollPane.setViewportView(contentPane);
    	setContentPane(jMainScrollPane);
    	jMainScrollPane.revalidate();
        
        contentPane.jbtnAddChemical.addActionListener(this);
        contentPane.jbtnEditChemical.addActionListener(this);
        contentPane.jbtnRemoveChemical.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoNoPlot.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoRetentionFactor.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoSolventBFraction.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoPosition.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoBackpressure.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.addActionListener(this);
        contentPane.jxpanelPlotOptions.jcboPositionCompounds.addActionListener(this);
        contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.addActionListener(this);
        contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.addActionListener(this);
        contentPane.jxpanelMobilePhaseComposition.jcboSolventB.addActionListener(this);
        contentPane.jxpanelChromatographyProperties.jsliderTemp.addChangeListener(this);
        contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.addChangeListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtTemp.addKeyListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtTemp.addFocusListener(this);
        contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.addKeyListener(this);
        contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.addFocusListener(this);
        contentPane.jxpanelGradientOptions.jtxtMixingVolume.addKeyListener(this);
        contentPane.jxpanelGradientOptions.jtxtMixingVolume.addFocusListener(this);
        contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.addKeyListener(this);
        contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jcboStationaryPhase.addActionListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnLength.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnLength.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnDiameter.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtColumnDiameter.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.addFocusListener(this);        
        contentPane.jxpanelChromatographyProperties.jtxtFlowRate.addKeyListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtFlowRate.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtParticleSize.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtParticleSize.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtATerm.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtATerm.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtBTerm.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtBTerm.addFocusListener(this);        
        contentPane.jxpanelColumnProperties.jtxtCTerm.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtCTerm.addFocusListener(this);
        contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.addKeyListener(this);
        contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.addFocusListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.addKeyListener(this);
        contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.addFocusListener(this);        
        contentPane.jxpanelGeneralProperties.jtxtTimeConstant.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtTimeConstant.addFocusListener(this);        
        contentPane.jxpanelGeneralProperties.jtxtNoise.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtNoise.addFocusListener(this);        
        contentPane.jxpanelGeneralProperties.jtxtSignalOffset.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtSignalOffset.addFocusListener(this);   
        contentPane.jxpanelGeneralProperties.jtxtInitialTime.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtInitialTime.addFocusListener(this);   
        contentPane.jxpanelGeneralProperties.jtxtFinalTime.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtFinalTime.addFocusListener(this);   
        contentPane.jxpanelGeneralProperties.jtxtNumPoints.addKeyListener(this);
        contentPane.jxpanelGeneralProperties.jtxtNumPoints.addFocusListener(this);   
        contentPane.jtableChemicals.getSelectionModel().addListSelectionListener(this);
        contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.addActionListener(this);
        contentPane.jbtnPan.addActionListener(this);
        contentPane.jbtnZoomIn.addActionListener(this);
        contentPane.jbtnZoomOut.addActionListener(this);
        contentPane.jbtnAutoscale.addActionListener(this);
        contentPane.jbtnAutoscaleX.addActionListener(this);
        contentPane.jbtnAutoscaleY.addActionListener(this);
        //contentPane.jbtnHelp.addActionListener(this);
        contentPane.jbtnOptimize.addActionListener(this);
        contentPane.jbtnCopyImage.addActionListener(this);
        contentPane.m_GraphControl.addAutoScaleListener(this);
        contentPane.m_GraphControl.setSecondYAxisVisible(false);
        contentPane.m_GraphControl.setSecondYAxisRangeLimits(0, 100);
        contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.addActionListener(this);
        contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.addActionListener(this);
        contentPane.jxpanelGradientOptions.jbtnInsertRow.addActionListener(this);
        contentPane.jxpanelGradientOptions.jbtnRemoveRow.addActionListener(this);
        //contentPane.jbtnContextHelp.addActionListener(new CSH.DisplayHelpAfterTracking(Globals.hbMainHelpBroker));
        contentPane.jxpanelGradientOptions.tmGradientProgram.addTableModelListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.addKeyListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.addFocusListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.addKeyListener(this);
        contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.addFocusListener(this);        
    }

    private void validateTemp() throws ParseException
    {
    	if (contentPane.jxpanelChromatographyProperties.jtxtTemp.getText().length() == 0)
    		contentPane.jxpanelChromatographyProperties.jtxtTemp.setText("0");

		NumberFormat formatter = new DecimalFormat("#0");
		Number dTemp = formatter.parse(contentPane.jxpanelChromatographyProperties.jtxtTemp.getText());
		dTemp = Math.floor(dTemp.doubleValue());
		
		if (dTemp.doubleValue() < 10)
			dTemp = 10;
		if (dTemp.doubleValue() > 150)
			dTemp = 150;
		
		m_dTemperature = dTemp.doubleValue();
		m_bSliderUpdate = false;
		contentPane.jxpanelChromatographyProperties.jsliderTemp.setValue((int)m_dTemperature);
		m_bSliderUpdate = true;
		contentPane.jxpanelChromatographyProperties.jtxtTemp.setText(formatter.format((int)m_dTemperature));    	
    }
    
    private void validateSolventBFraction() throws ParseException
    {
    	if (contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.getText().length() == 0)
    		contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText("0");
    	
		NumberFormat formatter = new DecimalFormat("#0");
		Number dTemp = formatter.parse(contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.getText());
		dTemp = Math.floor(dTemp.doubleValue());
		
		if (dTemp.doubleValue() < 0)
			dTemp = 0;
		if (dTemp.doubleValue() > 100)
			dTemp = 100;
		
		this.m_dSolventBFraction = dTemp.doubleValue() / 100;
		m_bSliderUpdate = false;
		contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.setValue((int)(m_dSolventBFraction * 100));
		m_bSliderUpdate = true;
		contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText(formatter.format((int)(m_dSolventBFraction * 100)));    	
    }    
 
    private void validateMixingVolume() throws ParseException
    {
    	if (contentPane.jxpanelGradientOptions.jtxtMixingVolume.getText().length() == 0)
    		contentPane.jxpanelGradientOptions.jtxtMixingVolume.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelGradientOptions.jtxtMixingVolume.getText());
		
		if (dTemp.doubleValue() < 0.01)
			dTemp = 0.01;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dMixingVolume = dTemp.doubleValue();
		contentPane.jxpanelGradientOptions.jtxtMixingVolume.setText(formatter.format(m_dMixingVolume));    	
    }    

    private void validateNonMixingVolume() throws ParseException
    {
    	if (contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.getText().length() == 0)
    		contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.getText());
		
		if (dTemp.doubleValue() < 0.01)
			dTemp = 0.01;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dNonMixingVolume = dTemp.doubleValue();
		contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.setText(formatter.format(m_dNonMixingVolume));    	
    }
    
    private void validateColumnLength() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtColumnLength.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtColumnLength.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtColumnLength.getText());
		
		if (dTemp.doubleValue() < .01)
			dTemp = .01;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dColumnLength = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtColumnLength.setText(formatter.format(m_dColumnLength));    	
    }    

    private void validateColumnDiameter() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtColumnDiameter.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtColumnDiameter.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtColumnDiameter.getText());
		
		if (dTemp.doubleValue()< .001)
			dTemp = .001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dColumnDiameter = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtColumnDiameter.setText(formatter.format(m_dColumnDiameter));    	
    }    

    private void validateInterparticlePorosity() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.getText());
		
		if (dTemp.doubleValue() < .001)
			dTemp = .001;
		if (dTemp.doubleValue() > .999)
			dTemp = .999;
		
		this.m_dInterparticlePorosity = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.setText(formatter.format(m_dInterparticlePorosity));    	
    }    

    private void validateIntraparticlePorosity() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.getText());
		
		if (dTemp.doubleValue() < .001)
			dTemp = .001;
		if (dTemp.doubleValue() > .999)
			dTemp = .999;
		
		this.m_dIntraparticlePorosity = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.setText(formatter.format(m_dIntraparticlePorosity));    	
    }    

    private void validateFlowRate() throws ParseException
    {
    	if (contentPane.jxpanelChromatographyProperties.jtxtFlowRate.getText().length() == 0)
    		contentPane.jxpanelChromatographyProperties.jtxtFlowRate.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelChromatographyProperties.jtxtFlowRate.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dFlowRate = dTemp.doubleValue();
		contentPane.jxpanelChromatographyProperties.jtxtFlowRate.setText(formatter.format(m_dFlowRate));    	
    }    

    private void validateParticleSize() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtParticleSize.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtParticleSize.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtParticleSize.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dParticleSize = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtParticleSize.setText(formatter.format(m_dParticleSize));    	
    }    

    private void validateATerm() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtATerm.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtATerm.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtATerm.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dATerm = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtATerm.setText(formatter.format(m_dATerm));    	
    }    

    private void validateBTerm() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtBTerm.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtBTerm.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtBTerm.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dBTerm = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtBTerm.setText(formatter.format(m_dBTerm));    	
    } 
    
    private void validateCTerm() throws ParseException
    {
    	if (contentPane.jxpanelColumnProperties.jtxtCTerm.getText().length() == 0)
    		contentPane.jxpanelColumnProperties.jtxtCTerm.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelColumnProperties.jtxtCTerm.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dCTerm = dTemp.doubleValue();
		contentPane.jxpanelColumnProperties.jtxtCTerm.setText(formatter.format(m_dCTerm));    	
    } 

    private void validateInjectionVolume() throws ParseException
    {
    	if (contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.getText().length() == 0)
    		contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dInjectionVolume = dTemp.doubleValue();
		contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.setText(formatter.format(m_dInjectionVolume));    	
    } 

    private void validateTimeConstant() throws ParseException
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtTimeConstant.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtTimeConstant.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelGeneralProperties.jtxtTimeConstant.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dTimeConstant = dTemp.doubleValue();
		contentPane.jxpanelGeneralProperties.jtxtTimeConstant.setText(formatter.format(m_dTimeConstant));    	
    } 

    private void validateNoise() throws ParseException
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtNoise.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtNoise.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelGeneralProperties.jtxtNoise.getText());
		
		if (dTemp.doubleValue() < .000001)
			dTemp = .000001;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dNoise = dTemp.doubleValue();
		contentPane.jxpanelGeneralProperties.jtxtNoise.setText(formatter.format(m_dNoise));    	
    } 

    private void validateSignalOffset() throws ParseException
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtSignalOffset.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtSignalOffset.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelGeneralProperties.jtxtSignalOffset.getText());
		
		if (dTemp.doubleValue() < 0)
			dTemp = 0;
		if (dTemp.doubleValue() > 999999)
			dTemp = 999999;
		
		this.m_dSignalOffset = dTemp.doubleValue();
		contentPane.jxpanelGeneralProperties.jtxtSignalOffset.setText(formatter.format(m_dSignalOffset));    	
    } 

    private void validateStartTime() throws ParseException
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtInitialTime.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelGeneralProperties.jtxtInitialTime.getText());
		
		if (dTemp.doubleValue() < 0)
			dTemp = 0;
		if (dTemp.doubleValue() > m_dEndTime)
			dTemp = m_dEndTime - .000001;
		
		this.m_dStartTime = dTemp.doubleValue();
		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText(formatter.format(m_dStartTime));    	
    } 

    private void validateEndTime() throws ParseException
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtFinalTime.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelGeneralProperties.jtxtFinalTime.getText());
		
		if (dTemp.doubleValue() < m_dStartTime)
			dTemp = m_dStartTime + .000001;
		if (dTemp.doubleValue() > 99999999)
			dTemp = 99999999;
		
		this.m_dEndTime = dTemp.doubleValue();
		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(formatter.format(m_dEndTime));    	
    } 

    private void validateNumPoints() throws ParseException
    {
    	if (contentPane.jxpanelGeneralProperties.jtxtNumPoints.getText().length() == 0)
    		contentPane.jxpanelGeneralProperties.jtxtNumPoints.setText("0");

		NumberFormat formatter = new DecimalFormat("#0");
    	Number iTemp = formatter.parse(contentPane.jxpanelGeneralProperties.jtxtNumPoints.getText());
		
		if (iTemp.intValue() < 2)
			iTemp = 2;
		if (iTemp.intValue() > 100000)
			iTemp = 100000;
		
		this.m_iNumPoints = iTemp.intValue();
		contentPane.jxpanelGeneralProperties.jtxtNumPoints.setText(formatter.format(m_iNumPoints));    	
    } 

    private void validateTubingLength() throws ParseException
    {
    	if (contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.getText().length() == 0)
    		contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.getText());
		
		if (dTemp.doubleValue() < 0)
			dTemp = 0;
		if (dTemp.doubleValue() > 99999999)
			dTemp = 99999999;
		
		this.m_dTubingLength = dTemp.doubleValue();
		contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.setText(formatter.format(m_dTubingLength));    	
    } 
    
    private void validateTubingDiameter() throws ParseException
    {
    	if (contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.getText().length() == 0)
    		contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.setText("0");

		NumberFormat formatter = new DecimalFormat("#0.0#########");
    	Number dTemp = formatter.parse(contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.getText());

    	if (dTemp.doubleValue() < 0.001)
			dTemp = 0.001;
		if (dTemp.doubleValue() > 99999999)
			dTemp = 99999999;
		
		this.m_dTubingDiameter = dTemp.doubleValue();
		contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.setText(formatter.format(m_dTubingDiameter));    	
    } 
    
    public boolean writeToOutputStream()
    {
    	try 
		{
            FileOutputStream fos = new FileOutputStream(m_currentFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

	    	oos.writeDouble(dFileVersion);
	    	oos.writeObject(contentPane.jxpanelGradientOptions.tmGradientProgram.getDataVector());
            oos.writeBoolean(m_bGradientMode);
	    	oos.writeBoolean(m_bAutomaticTimeRange);
	        oos.writeDouble(m_dTemperature);
	        oos.writeDouble(m_dSolventBFraction);
	        oos.writeDouble(m_dMixingVolume);
	        oos.writeDouble(m_dNonMixingVolume);
	        oos.writeDouble(m_dColumnLength);
	        oos.writeDouble(m_dColumnDiameter);
	        oos.writeDouble(m_dInterparticlePorosity);
	        oos.writeDouble(m_dIntraparticlePorosity);
	        oos.writeDouble(m_dFlowRate);
	        oos.writeDouble(m_dParticleSize);
	        oos.writeDouble(m_dATerm);
	        oos.writeDouble(m_dBTerm);
	        oos.writeDouble(m_dCTerm);
	        oos.writeDouble(m_dInjectionVolume);
	        oos.writeDouble(m_dTimeConstant);
	        oos.writeDouble(m_dStartTime);
	        oos.writeDouble(m_dEndTime);
	        oos.writeDouble(m_dNoise);
	        oos.writeDouble(m_dSignalOffset);
	        oos.writeInt(m_iNumPoints);
	        oos.writeInt(m_iSolventB);
	        oos.writeObject(m_vectCompound);
	        oos.writeDouble(m_dTubingLength);
	        oos.writeDouble(m_dTubingDiameter);
	        oos.writeInt(m_iStationaryPhase);
	        
            oos.flush();
			oos.close();
			this.m_bDocumentChangedFlag = false;
    	}
    	catch (IOException e) 
		{
			e.printStackTrace();
	        JOptionPane.showMessageDialog(this, "The file could not be saved.", "Error saving file", JOptionPane.ERROR_MESSAGE);
	        return false;
		}
    	
    	return true;
    }
    
    public boolean saveFile(boolean bSaveAs)
    {
		if (bSaveAs == false && m_currentFile != null)
		{
			if (writeToOutputStream())
				return true;
		}
		else
		{	
			JFileChooser2 fc = new JFileChooser2();
			
			FileNameExtensionFilter filter = new FileNameExtensionFilter("HPLC Method Creator Files (*.hplccreate)", "hplccreate");
			fc.setFileFilter(filter);
			fc.setDialogTitle("Save As...");
			int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                m_currentFile = fc.getSelectedFile();
                String path = m_currentFile.getAbsolutePath();
                if (path.lastIndexOf(".") >= 0)
                	path = path.substring(0, path.lastIndexOf("."));
                	
                m_currentFile = new File(path + ".hplccreate");

               	if (writeToOutputStream())
               		return true;
				
            }
		}
		return false;
    }
    
    public void loadDefaultsForStationaryPhase(int iStationaryPhase)
    {
    	if (iStationaryPhase == 0)
    	{
        	int iNumRows = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
        	for (int i = 0; i < iNumRows; i++)
        	{
        		m_bDoNotChangeTable = true;
        		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(0);
        	}

    		m_bDoNotChangeTable = true;
        	contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(new Double[] {0.0, 5.0});
    		m_bDoNotChangeTable = true;
        	contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(new Double[] {5.0, 95.0});
        	
        	m_bGradientMode = false;
        	m_bAutomaticTimeRange = true;
        	m_dTemperature = 35;
        	m_dSolventBFraction = 0.4;
        	m_dMixingVolume = 50; /* in uL */
        	m_dNonMixingVolume = 50; /* in uL */
        	m_dColumnLength = 100;
        	m_dColumnDiameter = 2.1;
        	m_dInterparticlePorosity = 0.4;
        	m_dIntraparticlePorosity = 0.4;
        	m_dFlowRate = 0.8; /* in mL/min */
        	m_dParticleSize = 3.5;
        	m_dATerm = 1;
        	m_dBTerm = 5;
        	m_dCTerm = 0.05;
        	m_dInjectionVolume = 5; //(in uL)
        	m_dTimeConstant = 0.1;
        	m_dStartTime = 0;
        	m_dEndTime = 277;
        	m_dNoise = 2.0;
        	m_dSignalOffset = 0;
        	m_iNumPoints = 3000;
        	m_iSolventB = 0; // 0 = Acetonitrile, 1 = Methanol
        	m_iStationaryPhase = 0;
        	m_vectCompound.clear();
        	
        	double[][] compoundsToAdd = {
        			{0, 76.92816658},
        			{1, 88.964585},
        			{2, 126.2170083},
        			{3, 222.9921914},
        			{4, 70.93217779},
        			{5, 84.28308006},
        			{6, 281.3182302},
        			{7, 59.32673774},
        			{8, 89.63616166},
        			{9, 106.6039579},
        			{10, 236.9223435},
        			{11, 94.13858571},
        			{12, 269.7997095},
        			{13, 166.4315284},
        			{14, 136.5448243},
        			{15, 184.2624363},
        			{16, 148.9066732},
        			{17, 134.4274553},
        			{18, 111.8530041},
        			{19, 66.03055297},
        			{20, 241.4095766},
        			{21, 184.0092423},
        			{22, 80.81135495},
        			{23, 67.22420851},
        			{24, 86.91404864},
        			{25, 97.65897462},
        			{26, 70.12273735},
        			{27, 178.8724291},
        			{28, 67.7112736},
        			{29, 107.064891},
        			{30, 255.363031},
        			{31, 64.60215056},
        			{32, 32.15423675},
        			{33, 63.95351867},
        			{34, 114.0131087},
        			{35, 76.62158344},
        			{36, 100.1672398},
        			{37, 173.0927246},
        			{38, 61.61440585},
        			{39, 45.68993011},
        			{40, 78.0102625},
        			{41, 95.78447141},
        			{42, 185.3996838},
        			{43, 166.5450612},
        			{44, 135.9012758},
        			{45, 39.50202854},
        			{46, 114.3455364},
        			{47, 149.8785435},
        			{48, 77.56859427},
        			{49, 67.47023284},
        			{50, 233.311855},
        			{51, 113.5765066},
        			{52, 88.10997065},
        			{53, 128.5797809},
        			{54, 59.76174185},
        			{55, 90.93490636},
        			{56, 56.8312583},
        			{57, 156.5079783},
        			{58, 45.69259886},
        			{59, 101.5120931},
        			{60, 166.161738},
        			{61, 107.9221852},
        			{62, 38.28267294},
        			{63, 70.63218902},
        			{64, 111.3041577},
        			{65, 84.59926327},
        			{66, 81.99048645},
        			{67, 112.1854261},
        			{68, 101.5273052},
        			{69, 69.5917077},
        			{70, 36.4458315},
        			{71, 61.01919798},
        			{72, 75.91187313},
        			{73, 71.89222292},
        			{74, 77.05473588},
        			{75, 161.6632094},
        			{76, 87.6610963},
        			{77, 127.8210016},
        			{78, 162.9693462},
        			{79, 183.1656229},
        			{80, 32.89446217},
        			{81, 85.7594782},
        			{82, 44.3024017},
        			{83, 181.5972329},
        			{84, 22.52491525},
        			{85, 149.7924326},
        			{86, 230.1122321},
        			{87, 88.75984898},
        			{88, 42.76685633},
        			{89, 106.6759479},
        			{90, 42.44839521},
        			{91, 106.5964276},
        			{92, 63.52075134},
        			{93, 67.51645924},
        			{94, 56.27261509},
        			{95, 245.1925449},
        			{96, 44.22140282},
        			{97, 69.07769504},
        			{98, 111.3290613},
        			{99, 43.26538899},
        			{100, 67.16675363},
        			{101, 37.79319138},
        			{102, 82.87370136},
        			{103, 156.900584},
        			{104, 61.785167},
        			{105, 118.5942103},
        			{106, 50.62441681},
        			{107, 83.19476882},
        			{108, 207.194531},
        			{109, 112.1257602},
        			{110, 227.5733073},
        			{111, 101.8636758},
        			{112, 221.7952898},
        			{113, 181.6403654},
        			{114, 97.69323317},
        			{115, 131.4466392},
        			{116, 159.749383},
        			{117, 101.1693141},
        			{118, 38.77610304},
        			{119, 45.55992914},
        			{120, 169.999547},
        			{121, 84.82859104},
        			{122, 67.84593049},
        			{123, 96.39275622},
        			{124, 83.02366309},
        			{125, 82.02177655},
        			{126, 137.4797029},
        			{127, 147.2525463},
        			{128, 76.43990868},
        			{129, 76.04900033},
        			{130, 65.37965929},
        			{131, 90.69811661},
        			{132, 289.8419105},
        			{133, 231.3041124},
        			{134, 63.93685422},
        			{135, 87.87704439},
        			{136, 136.4010107},
        			{137, 80.42249757},
        			{138, 79.28736491},
        			{139, 137.9140018},
        			{140, 116.7544327},
        			{141, 190.5476599},
        			{142, 124.87802},
        			{143, 42.41956746},
        			{144, 123.0549049},
        			{145, 108.4262986},
        			{146, 42.75569709},
        			{147, 67.77419047},
        			{148, 217.9306306},
        			{149, 75.36836586},
        			{150, 187.2347009},
        			{151, 113.6020657},
        			{152, 70.63071109},
        			{153, 74.30230189},
        			{154, 159.5451636},
        			{155, 44.82962192},
        			{156, 46.27274981},
        			{157, 80.10148101},
        			{158, 374.3670904},
        			{159, 110.9937636},
        			{160, 66.54608343},
        			{161, 56.1808296},
        			{162, 69.61661575},
        			{163, 184.3853618},
        			{164, 126.9045725},
        			{165, 39.56320653},
        			{166, 162.9114228},
        			//{167, 85.13355142},
        			{168, 178.0413635},
        			{169, 138.2668541},
        			{170, 65.50074152},
        			{171, 58.47071903},
        			{172, 57.12539389},
        			{173, 121.9626435},
        			{174, 24.4615827},
        			{175, 192.0184979},
        			{176, 65.27125839},
        			{177, 34.90111104},
        			{178, 110.7077603},
        			{179, 59.61782},
        			{180, 363.0969936},
        			{181, 91.108274},
        			{182, 45.36428048},
        			{183, 84.27509053},
        			{184, 109.922194},
        			{185, 108.5798059},
        			{186, 107.6245907},
        			{187, 250.8414926},
        			{188, 104.8128659},
        			{189, 131.7434998},
        			{190, 150.2426573},
        			{191, 94.27492041},
        			{192, 102.6867024},
        			{193, 176.166897},
        			{194, 63.28464712},
        			{195, 179.1252573},
        			{196, 51.78518271},
        			{197, 99.5166755},
        			{198, 58.22890593},
        			{199, 63.46855138},
        			{200, 366.1930041},
        			{201, 171.6109147},
        			{202, 97.8512078},
        			{203, 115.0715158},
        			{204, 172.9206417},
        			{205, 165.9192391},
        			{206, 37.85023827},
        			{207, 37.31300008},
        			{208, 146.4763312},
        			{209, 74.85769598},
        			{210, 80.86289211},
        			{211, 79.21869453},
        			{212, 101.6147791},
        			{213, 126.6243812},
        			{214, 127.7258769},
        			{215, 76.94025396},
        			{216, 227.2242458},
        			{217, 161.9103306},
        			{218, 147.7144045},
        			{219, 73.58601788},
        			{220, 78.40897777},
        			{221, 204.2753655},
        			{222, 124.011416}
            };
            
            for (int i = 0; i < compoundsToAdd.length; i++)
            {
            	Compound compound = new Compound();
            	compound.loadCompoundInfo(m_iStationaryPhase, (int)compoundsToAdd[i][0]);
            	compound.dConcentration = compoundsToAdd[i][1];
            	this.m_vectCompound.add(compound);
            }
        	
        	m_dTubingLength = 0; /* in cm */
        	m_dTubingDiameter = 5; /* in mil */
    	}
    	else if (iStationaryPhase == 1)
    	{
        	int iNumRows = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
        	for (int i = 0; i < iNumRows; i++)
        	{
        		m_bDoNotChangeTable = true;
        		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(0);
        	}

    		m_bDoNotChangeTable = true;
        	contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(new Double[] {0.0, 5.0});
    		m_bDoNotChangeTable = true;
        	contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(new Double[] {5.0, 95.0});
        	
        	m_bGradientMode = false;
        	m_bAutomaticTimeRange = true;
        	m_dTemperature = 25;
        	m_dSolventBFraction = 0.5;
        	m_dMixingVolume = 200; /* in uL */
        	m_dNonMixingVolume = 200; /* in uL */
        	m_dColumnLength = 100;
        	m_dColumnDiameter = 4.6;
        	m_dInterparticlePorosity = 0.4;
        	m_dIntraparticlePorosity = 0.4;
        	m_dFlowRate = 2; /* in mL/min */
        	m_dParticleSize = 3.0;
        	m_dATerm = 1;
        	m_dBTerm = 5;
        	m_dCTerm = 0.05;
        	m_dInjectionVolume = 5; //(in uL)
        	m_dTimeConstant = 0.1;
        	m_dStartTime = 0;
        	m_dEndTime = 277;
        	m_dNoise = 2.0;
        	m_dSignalOffset = 0;
        	m_iNumPoints = 3000;
        	m_iSolventB = 0; // 0 = Acetonitrile, 1 = Methanol
        	m_iStationaryPhase = 1;
        	m_vectCompound.clear();
        	Compound compound1 = new Compound();
        	compound1.loadCompoundInfo(m_iStationaryPhase, 2);
        	compound1.dConcentration = 5;
        	this.m_vectCompound.add(compound1);
        	
        	Compound compound2 = new Compound();
        	compound2.loadCompoundInfo(m_iStationaryPhase, 3);
        	compound2.dConcentration = 25;
        	this.m_vectCompound.add(compound2);
        	
        	Compound compound3 = new Compound();
        	compound3.loadCompoundInfo(m_iStationaryPhase, 4);
        	compound3.dConcentration = 40;
        	this.m_vectCompound.add(compound3);

        	Compound compound4 = new Compound();
        	compound4.loadCompoundInfo(m_iStationaryPhase, 6);
        	compound4.dConcentration = 15;
        	this.m_vectCompound.add(compound4);

        	Compound compound5 = new Compound();
        	compound5.loadCompoundInfo(m_iStationaryPhase, 11);
        	compound5.dConcentration = 10;
        	this.m_vectCompound.add(compound5);
        	
        	m_dTubingLength = 0; /* in cm */
        	m_dTubingDiameter = 5; /* in mil */
    	}
    	
    	loadControlsWithStoredValues();
    	updateCompoundComboBoxes();
    }
    
    public boolean loadFile(File fileToLoad)
    {
    	// Set all variables to default values here
    	loadDefaultsForStationaryPhase(0);
    	
    	if (fileToLoad != null)
    	{
    		try 
            {
                FileInputStream fis = new FileInputStream(m_currentFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                double dFileVersion = ois.readDouble();
                /*if (dFileVersion == 1.13)
                {
                	iNumRows = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
                	for (int i = 0; i < iNumRows; i++)
                	{
                		m_bDoNotChangeTable = true;
                		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(0);
                	}
              
                	Vector<Vector<Double>> rowVector = (Vector<Vector<Double>>)ois.readObject();

                	for (int i = 0; i < rowVector.size(); i++)
                	{
                		if (rowVector.elementAt(i) != null)
                		{
                    		m_bDoNotChangeTable = true;
                			contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(rowVector.elementAt(i));
                		}
                	}
                	m_bGradientMode = ois.readBoolean();
                	bAutomaticTimeRange = ois.readBoolean();
                	m_dTemperature = ois.readDouble();
                	m_dSolventBFraction = ois.readDouble();
                	m_dMixingVolume = ois.readDouble();
                	m_dNonMixingVolume = ois.readDouble();
                	m_dColumnLength = ois.readDouble();
                	m_dColumnDiameter = ois.readDouble();
                	m_dInterparticlePorosity = ois.readDouble();
                	m_dIntraparticlePorosity = ois.readDouble();
                	m_dFlowRate = ois.readDouble();
                	m_dParticleSize = ois.readDouble();
                	m_dATerm = ois.readDouble();
                	m_dBTerm = ois.readDouble();
                	m_dCTerm = ois.readDouble();
                	m_dInjectionVolume = ois.readDouble();
                	m_dTimeConstant = ois.readDouble();
                	m_dStartTime = ois.readDouble();
                	m_dEndTime = ois.readDouble();
                	m_dNoise = ois.readDouble();
                	m_dSignalOffset = ois.readDouble();
                	m_iNumPoints = ois.readInt();
                	m_iSolventB = ois.readInt();
                	m_vectCompound = (Vector<Compound>) ois.readObject();
                	m_dTubingLength = ois.readDouble();
                	m_dTubingDiameter = ois.readDouble();
                }*/
                if (dFileVersion == 1.14)
                {
                	int iNumRows = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
                	for (int i = 0; i < iNumRows; i++)
                	{
                		m_bDoNotChangeTable = true;
                		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(0);
                	}
              
                	Vector<Vector<Double>> rowVector = (Vector<Vector<Double>>)ois.readObject();

                	for (int i = 0; i < rowVector.size(); i++)
                	{
                		if (rowVector.elementAt(i) != null)
                		{
                    		m_bDoNotChangeTable = true;
                			contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(rowVector.elementAt(i));
                		}
                	}
                	m_bGradientMode = ois.readBoolean();
                	m_bAutomaticTimeRange = ois.readBoolean();
                	m_dTemperature = ois.readDouble();
                	m_dSolventBFraction = ois.readDouble();
                	m_dMixingVolume = ois.readDouble();
                	m_dNonMixingVolume = ois.readDouble();
                	m_dColumnLength = ois.readDouble();
                	m_dColumnDiameter = ois.readDouble();
                	m_dInterparticlePorosity = ois.readDouble();
                	m_dIntraparticlePorosity = ois.readDouble();
                	m_dFlowRate = ois.readDouble();
                	m_dParticleSize = ois.readDouble();
                	m_dATerm = ois.readDouble();
                	m_dBTerm = ois.readDouble();
                	m_dCTerm = ois.readDouble();
                	m_dInjectionVolume = ois.readDouble();
                	m_dTimeConstant = ois.readDouble();
                	m_dStartTime = ois.readDouble();
                	m_dEndTime = ois.readDouble();
                	m_dNoise = ois.readDouble();
                	m_dSignalOffset = ois.readDouble();
                	m_iNumPoints = ois.readInt();
                	m_iSolventB = ois.readInt();
                	m_vectCompound = (Vector<Compound>) ois.readObject();
                	m_dTubingLength = ois.readDouble();
                	m_dTubingDiameter = ois.readDouble();
                	m_iStationaryPhase = 1; // Select the Agilent column
                }
                else if (dFileVersion >= 1.15)
                {
                	int iNumRows = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
                	for (int i = 0; i < iNumRows; i++)
                	{
                		m_bDoNotChangeTable = true;
                		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(0);
                	}
              
                	Vector<Vector<Double>> rowVector = (Vector<Vector<Double>>)ois.readObject();

                	for (int i = 0; i < rowVector.size(); i++)
                	{
                		if (rowVector.elementAt(i) != null)
                		{
                    		m_bDoNotChangeTable = true;
                			contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(rowVector.elementAt(i));
                		}
                	}
                	m_bGradientMode = ois.readBoolean();
                	m_bAutomaticTimeRange = ois.readBoolean();
                	m_dTemperature = ois.readDouble();
                	m_dSolventBFraction = ois.readDouble();
                	m_dMixingVolume = ois.readDouble();
                	m_dNonMixingVolume = ois.readDouble();
                	m_dColumnLength = ois.readDouble();
                	m_dColumnDiameter = ois.readDouble();
                	m_dInterparticlePorosity = ois.readDouble();
                	m_dIntraparticlePorosity = ois.readDouble();
                	m_dFlowRate = ois.readDouble();
                	m_dParticleSize = ois.readDouble();
                	m_dATerm = ois.readDouble();
                	m_dBTerm = ois.readDouble();
                	m_dCTerm = ois.readDouble();
                	m_dInjectionVolume = ois.readDouble();
                	m_dTimeConstant = ois.readDouble();
                	m_dStartTime = ois.readDouble();
                	m_dEndTime = ois.readDouble();
                	m_dNoise = ois.readDouble();
                	m_dSignalOffset = ois.readDouble();
                	m_iNumPoints = ois.readInt();
                	m_iSolventB = ois.readInt();
                	m_vectCompound = (Vector<Compound>) ois.readObject();
                	m_dTubingLength = ois.readDouble();
                	m_dTubingDiameter = ois.readDouble();
                	m_iStationaryPhase = ois.readInt();
                }
                else
                {
    		        JOptionPane.showMessageDialog(this, "Sorry! This file is no longer compatible with HPLC Method Creator after the latest update. However, new files you save in this version WILL be compatible with future versions.", "Error opening file", JOptionPane.ERROR_MESSAGE);
                    ois.close();
    		        m_currentFile = null;
    		        return false;
                }
    	        
                ois.close();
			} 
            catch (IOException e) 
            {
				e.printStackTrace();
		        JOptionPane.showMessageDialog(this, "The file is not a valid HPLC Method Creator file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
		        m_currentFile = null;
		        return false;
			} 
            catch (ClassNotFoundException e) 
            {
				e.printStackTrace();
		        JOptionPane.showMessageDialog(this, "The file is not a valid HPLC Method Creator file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
		        m_currentFile = null;
		        return false;
            }
    	}
    	
    	loadControlsWithStoredValues();
		/*
    	m_iSecondPlotType = 0;
    	m_dTemperature = 25;
    	m_bGradientMode = false;
    	m_dSolventBFraction = 0.5;
    	m_dMixingVolume = 200;
    	m_dNonMixingVolume = 200;
    	m_dColumnLength = 100;
    	m_dColumnDiameter = 4.6;
    	m_dInterparticlePorosity = 0.4;
    	m_dIntraparticlePorosity = 0.4;
    	m_dTotalPorosity = 0.64;
    	m_dFlowRate = 2;
    	m_dParticleSize = 5;
    	m_dDiffusionCoefficient = 0.00001;
    	m_dATerm = 1;
    	m_dBTerm = 5;
    	m_dCTerm = 0.05;
    	m_dInjectionVolume = 5; //(in uL)
    	m_dTimeConstant = 0.5;
    	m_dStartTime = 0;
    	m_dEndTime = 0;
    	m_dNoise = 3;
    	m_dSignalOffset = 30;
    	m_iNumPoints = 3000;
    	m_dEluentViscosity = 1;
    	m_dBackpressure = 400;
    	m_iSolventB = 0; // 0 = Acetonitrile, 1 = Methanol
    	m_vectCompound.clear();
    	Compound compound1 = new Compound();
    	compound1.loadCompoundInfo(2, m_iSolventB);
    	compound1.dConcentration = 5;
    	this.m_vectCompound.add(compound1);
    	
    	Compound compound2 = new Compound();
    	compound2.loadCompoundInfo(3, m_iSolventB);
    	compound2.dConcentration = 25;
    	this.m_vectCompound.add(compound2);
    	
    	Compound compound3 = new Compound();
    	compound3.loadCompoundInfo(4, m_iSolventB);
    	compound3.dConcentration = 40;
    	this.m_vectCompound.add(compound3);

    	Compound compound4 = new Compound();
    	compound4.loadCompoundInfo(6, m_iSolventB);
    	compound4.dConcentration = 15;
    	this.m_vectCompound.add(compound4);

    	Compound compound5 = new Compound();
    	compound5.loadCompoundInfo(11, m_iSolventB);
    	compound5.dConcentration = 10;
    	this.m_vectCompound.add(compound5);
    	
    	m_iChromatogramPlotIndex = -1;
    	m_iSinglePlotIndex = -1;
    	m_iSecondPlotIndex = -1;
    	m_dTubingLength = 0;
    	m_dTubingDiameter = 5;*/
    	
    	return true;
    }
    
    public void loadControlsWithStoredValues()
    {
    	// Now set each parameter in the controls
    	if (m_bGradientMode)
	    {
    		contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(true);

	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelIsocraticOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelGradientOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jsclControlPanel.validate();
	    }
	    else
	    {
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(true);
	    	
	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelGradientOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelIsocraticOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jsclControlPanel.validate();
	    }
    	
    	m_bDoNotMessage = true;
    	contentPane.jxpanelColumnProperties.jcboStationaryPhase.setSelectedIndex(m_iStationaryPhase);
    	m_bDoNotMessage = false;
    	
    	m_bSliderUpdate = false;
		NumberFormat intFormatter = new DecimalFormat("#0");
		NumberFormat floatFormatter = new DecimalFormat("#0.0#########");
    	contentPane.jxpanelChromatographyProperties.jtxtTemp.setText(intFormatter.format((int)m_dTemperature));
    	contentPane.jxpanelChromatographyProperties.jsliderTemp.setValue((int)m_dTemperature);
		contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.setValue((int)(m_dSolventBFraction * 100));
		contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText(intFormatter.format((int)(m_dSolventBFraction * 100)));    	
		m_bSliderUpdate = true;
		
		contentPane.jxpanelChromatographyProperties.jtxtFlowRate.setText(floatFormatter.format(m_dFlowRate));    	
		contentPane.jxpanelChromatographyProperties.jtxtInjectionVolume.setText(floatFormatter.format(m_dInjectionVolume));    	
		contentPane.jxpanelColumnProperties.jtxtATerm.setText(floatFormatter.format(m_dATerm));    	
		contentPane.jxpanelColumnProperties.jtxtBTerm.setText(floatFormatter.format(m_dBTerm));    	
		contentPane.jxpanelColumnProperties.jtxtCTerm.setText(floatFormatter.format(m_dCTerm));    	
		contentPane.jxpanelColumnProperties.jtxtColumnDiameter.setText(floatFormatter.format(m_dColumnDiameter));    	
		contentPane.jxpanelColumnProperties.jtxtColumnLength.setText(floatFormatter.format(m_dColumnLength));    	
		contentPane.jxpanelColumnProperties.jtxtInterparticlePorosity.setText(floatFormatter.format(m_dInterparticlePorosity));    	
		contentPane.jxpanelColumnProperties.jtxtIntraparticlePorosity.setText(floatFormatter.format(m_dIntraparticlePorosity));    	
		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText(floatFormatter.format(m_dStartTime));    	
		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(floatFormatter.format(m_dEndTime));    	
		contentPane.jxpanelGradientOptions.jtxtMixingVolume.setText(floatFormatter.format(m_dMixingVolume));    	
		contentPane.jxpanelGradientOptions.jtxtNonMixingVolume.setText(floatFormatter.format(m_dNonMixingVolume));    	
		contentPane.jxpanelGeneralProperties.jtxtNoise.setText(floatFormatter.format(m_dNoise));    	
		contentPane.jxpanelGeneralProperties.jtxtNumPoints.setText(intFormatter.format(m_iNumPoints));    	
		contentPane.jxpanelColumnProperties.jtxtParticleSize.setText(floatFormatter.format(m_dParticleSize));    	
		contentPane.jxpanelGeneralProperties.jtxtSignalOffset.setText(floatFormatter.format(m_dSignalOffset));    	
		contentPane.jxpanelGeneralProperties.jtxtTimeConstant.setText(floatFormatter.format(m_dTimeConstant));    	
		contentPane.jxpanelExtraColumnTubing.jtxtTubingDiameter.setText(floatFormatter.format(m_dTubingDiameter));    	
		contentPane.jxpanelExtraColumnTubing.jtxtTubingLength.setText(floatFormatter.format(m_dTubingLength));    	
		
    	// Add the table space for the compound. Fill it in later with performCalculations().
		contentPane.vectChemicalRows.clear();
		for (int i = 0; i < m_vectCompound.size(); i++)
		{
			Vector<String> vectNewRow = new Vector<String>();
	    	vectNewRow.add(m_vectCompound.get(i).strCompoundName);
	    	vectNewRow.add(floatFormatter.format(m_vectCompound.get(i).dConcentration));
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	contentPane.vectChemicalRows.add(vectNewRow);
		}

		contentPane.jxpanelMobilePhaseComposition.jcboSolventA.setSelectedIndex(0);
		contentPane.jxpanelMobilePhaseComposition.jcboSolventB.setSelectedIndex(m_iSolventB);

		updateCompoundComboBoxes();
    	
		contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.setSelected(m_bAutomaticTimeRange);
    	if (m_bAutomaticTimeRange)
    	{
    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(false);
    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(false);
    	}
    	else
    	{
    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(true);
    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(true);	    		
    	}
    	
		performCalculations();
    }
    
    public void actionPerformed(ActionEvent evt) 
	{
	    String strActionCommand = evt.getActionCommand();
	    
	    if (strActionCommand == "Add Chemical")
	    {
	    	//Frame[] frames = Frame.getFrames();
	    	ChemicalDialog dlgChemical = new ChemicalDialog(this, false);
	    	
	    	dlgChemical.setSelectedStationaryPhase(this.m_iStationaryPhase);
	    	
	    	// Make a list of the chemical indices already used
	    	for (int i = 0; i < m_vectCompound.size(); i++)
	    	{
	    		Integer k = new Integer(m_vectCompound.get(i).iCompoundIndex);
	    		dlgChemical.m_vectCompoundsUsed.add(k);
	    	}
	    	
	    	// Show the dialog.
	    	dlgChemical.setVisible(true);
	    	
	    	if (dlgChemical.m_bOk == false)
	    		return;
	    	
	    	if (dlgChemical.m_iCompoundType == 0 || dlgChemical.m_iCompoundType == 1)
	    	{
		    	// Add the compound properties to the m_vectCompound array
		    	Compound newCompound = new Compound();
		    	
		    	if (dlgChemical.m_iCompoundType == 0)
		    	{
		    		newCompound.loadCompoundInfo(m_iStationaryPhase, dlgChemical.m_iCompound);
		    		newCompound.dConcentration = dlgChemical.m_dConcentration1;
		    	}
		    	else if (dlgChemical.m_iCompoundType == 1)
		    	{
		    		for (int i = 0; i < Globals.iNumSolvents; i++)
		    		{
		    			//TODO: Make the add chemical dialog handle log k vs. phi relationships
		    			/*
		    			newCompound.dLogkwvsTSlope[i] = dlgChemical.m_dLogkwvsTSlope[i];
		    			newCompound.dLogkwvsTIntercept[i] = dlgChemical.m_dLogkwvsTIntercept[i];
		    			newCompound.dSvsTSlope[i] = dlgChemical.m_dSvsTSlope[i];
		    			newCompound.dSvsTIntercept[i] = dlgChemical.m_dSvsTIntercept[i];*/
		    		}
		    		//TODO: Make the molar volume user selectable?
		    		newCompound.dMolarVolume = 200;
		    		
		    		newCompound.iCompoundIndex = dlgChemical.m_iCompound;
		    		newCompound.strCompoundName = dlgChemical.m_strCompoundName;
		    		newCompound.dConcentration = dlgChemical.m_dConcentration2;
		    	}
		    	
		    	this.m_vectCompound.add(newCompound);
		    	
		    	// Add the table space for the compound. Fill it in later with performCalculations().
		    	Vector<String> vectNewRow = new Vector<String>();
		    	vectNewRow.add(newCompound.strCompoundName);
				NumberFormat floatFormatter = new DecimalFormat("#0.0#########");
		    	vectNewRow.add(floatFormatter.format(newCompound.dConcentration));
		    	vectNewRow.add("");
		    	vectNewRow.add("");
		    	vectNewRow.add("");
		    	vectNewRow.add("");
		    	vectNewRow.add("");
		    	
		    	contentPane.vectChemicalRows.add(vectNewRow);
	    	}
	    	else // Random matrix generation
	    	{
				NumberFormat floatFormatter = new DecimalFormat("#0.0#########");

	    		for (int i = 0; i < dlgChemical.m_iNumMatrixCompounds; i++)
	    		{
			    	Compound newCompound = new Compound();
			    	newCompound.createRandomCompound("unknown compound", dlgChemical.m_dMinimumConcentration, dlgChemical.m_dMaximumConcentration);
			    	
			    	this.m_vectCompound.add(newCompound);
			    	
			    	// Add the table space for the compound. Fill it in later with performCalculations().
			    	Vector<String> vectNewRow = new Vector<String>();
			    	vectNewRow.add(newCompound.strCompoundName);
			    	vectNewRow.add(floatFormatter.format(newCompound.dConcentration));
			    	vectNewRow.add("");
			    	vectNewRow.add("");
			    	vectNewRow.add("");
			    	vectNewRow.add("");
			    	vectNewRow.add("");
			    	
			    	contentPane.vectChemicalRows.add(vectNewRow);
	    		}
	    	}
	    	
	    	updateCompoundComboBoxes();
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Edit Chemical")
	    {
	    	int iRowSel = contentPane.jtableChemicals.getSelectedRow();
	    	if (iRowSel < 0 || iRowSel >= contentPane.vectChemicalRows.size())
	    		return;

	    	iRowSel = contentPane.jtableChemicals.convertRowIndexToModel(iRowSel);
	    	
	    	//Frame[] frames = Frame.getFrames();
	    	ChemicalDialog dlgChemical = new ChemicalDialog(this, true);
	    	
	    	dlgChemical.setSelectedStationaryPhase(this.m_iStationaryPhase);
	    	
	    	if (this.m_vectCompound.get(iRowSel).iCompoundIndex > -1)
	    	{
		    	dlgChemical.setSelectedCompound(this.m_vectCompound.get(iRowSel).iCompoundIndex);
		    	dlgChemical.setCompoundConcentration(this.m_vectCompound.get(iRowSel).dConcentration);
	    	}
	    	else
	    	{
		    	dlgChemical.setCustomCompoundProperties(this.m_vectCompound.get(iRowSel));
		    	dlgChemical.setCompoundConcentration(this.m_vectCompound.get(iRowSel).dConcentration);	    		
	    	}
	    	
	    	// Make a list of the chemical indices already used
	    	for (int i = 0; i < m_vectCompound.size(); i++)
	    	{
	    		// Don't add the currently selected row to the list
	    		if (i == iRowSel)
	    			continue;
	    		
	    		Integer k = new Integer(m_vectCompound.get(i).iCompoundIndex);
	    		dlgChemical.m_vectCompoundsUsed.add(k);
	    	}

	    	// Show the dialog.
	    	dlgChemical.setVisible(true);
	    	
	    	if (dlgChemical.m_bOk == false)
	    		return;
	    	
	    	// Add the compound properties to the m_vectCompound array
	    	Compound newCompound = new Compound();
	    	
	    	if (dlgChemical.m_iCompoundType == 0)
	    	{
	    		newCompound.loadCompoundInfo(m_iStationaryPhase, dlgChemical.m_iCompound);
	    		newCompound.dConcentration = dlgChemical.m_dConcentration1;
	    	}
	    	else if (dlgChemical.m_iCompoundType == 1)
	    	{
	    		for (int i = 0; i < Globals.iNumSolvents; i++)
	    		{
	    			//TODO: Make the add chemical dialog handle log k vs. phi relationships

	    			//newCompound.dLogkwvsTSlope[i] = dlgChemical.m_dLogkwvsTSlope[i];
	    			//newCompound.dLogkwvsTIntercept[i] = dlgChemical.m_dLogkwvsTIntercept[i];
	    			//newCompound.dSvsTSlope[i] = dlgChemical.m_dSvsTSlope[i];
	    			//newCompound.dSvsTIntercept[i] = dlgChemical.m_dSvsTIntercept[i];
	    		}
	    		//TODO: Make the molar volume user selectable?
	    		newCompound.dMolarVolume = 120;
	    		
	    		newCompound.iCompoundIndex = dlgChemical.m_iCompound;
	    		newCompound.strCompoundName = dlgChemical.m_strCompoundName;
	    		newCompound.dConcentration = dlgChemical.m_dConcentration2;
	    	}
	    	
	    	this.m_vectCompound.set(iRowSel, newCompound);
	    	
	    	// Add the table space for the compound. Fill it in later with performCalculations().
	    	Vector<String> vectNewRow = new Vector<String>();
	    	vectNewRow.add(newCompound.strCompoundName);
			NumberFormat floatFormatter = new DecimalFormat("#0.0#########");
	    	vectNewRow.add(floatFormatter.format(newCompound.dConcentration));
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	vectNewRow.add("");
	    	
	    	contentPane.vectChemicalRows.set(iRowSel, vectNewRow);

	    	updateCompoundComboBoxes();

	    	performCalculations();
	    }
	    else if (strActionCommand == "Remove Chemical")
	    {
	    	int iRowSel = contentPane.jtableChemicals.getSelectedRow();
	    	if (iRowSel < 0 || iRowSel >= contentPane.vectChemicalRows.size())
	    		return;
	    	
	    	iRowSel = contentPane.jtableChemicals.convertRowIndexToModel(iRowSel);

	    	contentPane.vectChemicalRows.remove(iRowSel);
	    	contentPane.jtableChemicals.addNotify();
	    	
	    	if (iRowSel >= m_vectCompound.size())
	    		return;
	    	
	    	m_vectCompound.remove(iRowSel);	    		
	    	updateCompoundComboBoxes();

	    	performCalculations();
	    }
	    else if (strActionCommand == "Automatically determine time span")
	    {
	    	if (contentPane.jxpanelGeneralProperties.jchkAutoTimeRange.isSelected() == true)
	    	{
	    		m_bAutomaticTimeRange = true;
	    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(false);
	    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(false);
	    	}
	    	else
	    	{
	    		m_bAutomaticTimeRange = false;
	    		contentPane.jxpanelGeneralProperties.jtxtInitialTime.setEnabled(true);
	    		contentPane.jxpanelGeneralProperties.jtxtFinalTime.setEnabled(true);	    		
	    	}
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Autoscale")
	    {
	    	if (contentPane.jbtnAutoscale.isSelected() == true)
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(true);
	    		contentPane.m_GraphControl.setAutoScaleY(true);
	    		contentPane.m_GraphControl.repaint();
	    	}
	    	else
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(false);
    			contentPane.m_GraphControl.setAutoScaleY(false);
	    	}
	    }
	    else if (strActionCommand == "Autoscale X")
	    {
	    	if (contentPane.jbtnAutoscaleX.isSelected() == true)
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(true);
	    		contentPane.m_GraphControl.repaint();
	    	}
	    	else
	    	{
	    		contentPane.m_GraphControl.setAutoScaleX(false);
	    	}
	    }
	    else if (strActionCommand == "Autoscale Y")
	    {
	    	if (contentPane.jbtnAutoscaleY.isSelected() == true)
	    	{
	    		contentPane.m_GraphControl.setAutoScaleY(true);
	    		contentPane.m_GraphControl.repaint();
	    	}
	    	else
	    	{
	    		contentPane.m_GraphControl.setAutoScaleY(false);
	    	}
	    }
	    else if (strActionCommand == "Pan")
	    {
	    	contentPane.jbtnPan.setSelected(true);
	    	contentPane.jbtnZoomIn.setSelected(false);
	    	contentPane.jbtnZoomOut.setSelected(false);
	    	contentPane.m_GraphControl.selectPanMode();
	    }
	    else if (strActionCommand == "Zoom in")
	    {
	    	contentPane.jbtnPan.setSelected(false);
	    	contentPane.jbtnZoomIn.setSelected(true);
	    	contentPane.jbtnZoomOut.setSelected(false);	    	
	    	contentPane.m_GraphControl.selectZoomInMode();
	    }
	    else if (strActionCommand == "Zoom out")
	    {
	    	contentPane.jbtnPan.setSelected(false);
	    	contentPane.jbtnZoomIn.setSelected(false);
	    	contentPane.jbtnZoomOut.setSelected(true);	    		    	
	    	contentPane.m_GraphControl.selectZoomOutMode();
	    }
	    else if (strActionCommand == "Help")
	    {
			//Globals.hbMainHelpBroker.setCurrentID("getting_started");
			//Globals.hbMainHelpBroker.setDisplayed(true);
	    }
	    else if (strActionCommand == "Tutorials")
	    {
			//Globals.hbMainHelpBroker.setCurrentID("tutorials");
			//Globals.hbMainHelpBroker.setDisplayed(true);
	    }
	    else if (strActionCommand == "SolventBComboBoxChanged")
	    {
	    	m_iSolventB = contentPane.jxpanelMobilePhaseComposition.jcboSolventB.getSelectedIndex();
	    	// Change the organic modifier fraction label 
	    	String strLabel = "Solvent B fraction (% v/v):";
	    	contentPane.jxpanelIsocraticOptions.jlblSolventBFraction.setText(strLabel);
	    	
	    	// Update all the indicators
	    	performCalculations();
	    }
	    else if (strActionCommand == "StationaryPhaseComboBoxChanged")
	    {
	    	if (!m_bDoNotMessage)
	    	{	
	    		if (m_iStationaryPhase == contentPane.jxpanelColumnProperties.jcboStationaryPhase.getSelectedIndex())
	    			return;

	    		int retval = JOptionPane.showConfirmDialog(null, "If you select a new stationary phase, the selected compounds will reset to the default ones.\nAre you sure you want to choose a new stationary phase?", "HPLC Method Creator", JOptionPane.YES_NO_OPTION);
		    	if (retval == JOptionPane.YES_OPTION)
		    	{
		    		m_iStationaryPhase = contentPane.jxpanelColumnProperties.jcboStationaryPhase.getSelectedIndex();
		    		loadDefaultsForStationaryPhase(m_iStationaryPhase);
		    	}
		    	else
		    	{
		    		contentPane.jxpanelColumnProperties.jcboStationaryPhase.setSelectedIndex(m_iStationaryPhase);
		    	}
	    	}
	    }
	    else if (strActionCommand == "Isocratic elution mode")
	    {
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(true);
	    	
	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelGradientOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelIsocraticOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jsclControlPanel.validate();
	    	
	    	this.m_bGradientMode = false;
	    	performCalculations();
	    }
	    else if (strActionCommand == "Gradient elution mode")
	    {
	    	contentPane.jxpanelMobilePhaseComposition.jrdoIsocraticElution.setSelected(false);
	    	contentPane.jxpanelMobilePhaseComposition.jrdoGradientElution.setSelected(true);

	    	contentPane.jxtaskMobilePhaseComposition.remove(contentPane.jxpanelIsocraticOptions);
	    	contentPane.jxtaskMobilePhaseComposition.add(contentPane.jxpanelGradientOptions);

	    	contentPane.jxpanelMobilePhaseComposition.validate();
	    	contentPane.jsclControlPanel.validate();

	    	this.m_bGradientMode = true;
	    	performCalculations();
	    }
	    else if (strActionCommand == "Insert Row")
	    {
	    	int iSelectedRow = contentPane.jxpanelGradientOptions.jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount()-1;
	    	
	    	Double dRowValue1 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iSelectedRow, 0);
	    	Double dRowValue2 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iSelectedRow, 1);
	    	Double dRowData[] = {dRowValue1, dRowValue2};
	    	contentPane.jxpanelGradientOptions.tmGradientProgram.insertRow(iSelectedRow+1, dRowData);
	    }
	    else if (strActionCommand == "Remove Row")
	    {
	    	int iSelectedRow = contentPane.jxpanelGradientOptions.jtableGradientProgram.getSelectedRow();
	    	
	    	if (iSelectedRow == -1)
	    		iSelectedRow = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount()-1;
	    	
	    	if (contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() >= 3)
	    	{
	    		contentPane.jxpanelGradientOptions.tmGradientProgram.removeRow(iSelectedRow);
	    	}
	    }
	    else if (strActionCommand == "No plot")
	    {
	    	m_iSecondPlotType = 0;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(false);
	    	contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
	    	m_iSecondPlotIndex = -1;
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot solvent B fraction")
	    {
	    	m_iSecondPlotType = 1;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Solvent B Fraction");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("% v/v", "%");
	    	contentPane.m_GraphControl.setSecondYAxisRangeLimits(0.0, 100.0);
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot backpressure")
	    {
	    	m_iSecondPlotType = 2;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Backpressure");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("bar", "bar");
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot mobile phase viscosity")
	    {
	    	m_iSecondPlotType = 3;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Mobile Phase Viscosity");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("Poise", "P");
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot retention factor")
	    {
	    	m_iSecondPlotType = 4;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("k", "");
	    	int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex();
	    	String strCompoundName = "";
	    	
	    	if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
	    		strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    	
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Retention factor of " + strCompoundName);

	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot position")
	    {
	    	m_iSecondPlotType = 5;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(true);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.setSelected(false);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex();
	    	String strCompoundName = "";
	    	
	    	if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
	    		strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    	
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Position of " + strCompoundName + " along column");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("millimeters", "mm");
	    	contentPane.m_GraphControl.setSecondYAxisRangeLimits(0.0, m_dColumnLength);
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "Plot number of compounds eluting")
	    {
	    	m_iSecondPlotType = 6;
	    	contentPane.jxpanelPlotOptions.jrdoBackpressure.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoMobilePhaseViscosity.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoPosition.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNoPlot.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoRetentionFactor.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoSolventBFraction.setSelected(false);
	    	contentPane.jxpanelPlotOptions.jrdoNumberOfCompounds.setSelected(true);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisVisible(true);
	    	
	    	contentPane.m_GraphControl.setSecondYAxisTitle("Number of compounds eluting at once (+/-2 sigma)");
	    	contentPane.m_GraphControl.setSecondYAxisBaseUnit("", "");
	    	
	    	performCalculations();
	    }
	    else if (strActionCommand == "RetentionFactorCompoundChanged")
	    {
	    	if (this.m_iSecondPlotType == 4)
	    	{
	    		int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex();
	    		String strCompoundName = "";
	    		
	    		if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
		    		strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    		
		    	contentPane.m_GraphControl.setSecondYAxisTitle("Retention factor of " + strCompoundName);
	    	}

	    	performCalculations();  	
	    }
	    else if (strActionCommand == "PositionCompoundChanged")
	    {
	    	if (this.m_iSecondPlotType == 5)
	    	{
	    		int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex();
	    		String strCompoundName = "";
	    		
	    		if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
	    			strCompoundName = this.m_vectCompound.get(iSelectedCompound).strCompoundName;
	    		
		    	contentPane.m_GraphControl.setSecondYAxisTitle("Position of " + strCompoundName + " along column");
	    	}
	    	
	    	performCalculations();  	
	    }
	    else if (strActionCommand == "Copy Image")
	    {
	    	ByteBuffer bytePixels = contentPane.m_GraphControl.getPixels();
	    	Image image;
	    	int h = contentPane.m_GraphControl.getHeight();
	    	int w = contentPane.m_GraphControl.getWidth();
	    	if (w % 4 > 0)
        		w += 4 - (w % 4);
	    	
	    	byte[] flippedPixels = new byte[bytePixels.array().length];

	    	for (int y = 0; y < h; y++)
	    	{
	    		for (int x = 0; x < w * 4; x++)
	    		{
	    			flippedPixels[(y * w * 4) + x] = bytePixels.array()[((h - y - 1) * w * 4) + x];
	    		}
	    	}

	    	DataBuffer dbuf = new DataBufferByte(flippedPixels, flippedPixels.length, 0);

	    	int[] bandOffsets = {0,1,2,3};
	    	SampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, contentPane.m_GraphControl.getWidth(), contentPane.m_GraphControl.getHeight(), 4, w * 4, bandOffsets);
	    	WritableRaster raster = Raster.createWritableRaster(sampleModel, dbuf, null);
	    	ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
	    					 new int[] {8,8,8,8},
	    					 true,
	    					 false,
	    					 ComponentColorModel.OPAQUE,
	    					 DataBuffer.TYPE_BYTE);
	    	image = new BufferedImage(colorModel, raster, false, null);
	        
	        new javax.swing.ImageIcon(image); // Force load.
	        BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	        newImage.createGraphics().drawImage(image, 0, 0, null);
	        image = newImage;
	        
	        ImageSelection imageSelection = new ImageSelection(image);
	        Toolkit toolkit = Toolkit.getDefaultToolkit();
	        toolkit.getSystemClipboard().setContents(imageSelection, null);
	    }
	    else if (evt.getSource() == this.menuResetToDefaultValuesAction)
		{
			if (this.m_bDocumentChangedFlag)
			{
				String fileName;
				if (this.m_currentFile == null)
					fileName = "Untitled";
				else
					fileName = m_currentFile.getName();
				
		        int result = JOptionPane.showConfirmDialog(this,"Do you want to save changes to " + fileName + "?", "HPLC Method Creator", JOptionPane.YES_NO_CANCEL_OPTION);
		        
		        if (result == JOptionPane.YES_OPTION)
		        {
		        	if (!saveFile(false))
		        		return;
		        }
		        else if (result == JOptionPane.CANCEL_OPTION)
		        {
		        	return;
		        }
			}
			m_currentFile = null;
			
			loadFile(null);
		}
		else if (evt.getSource() == this.menuLoadSettingsAction)
		{
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("HPLC Method Creator Files (*.hplccreate)", "hplccreate");
			fc.setFileFilter(filter);
			fc.setDialogTitle("Open");
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                m_currentFile = fc.getSelectedFile();

                loadFile(m_currentFile);
            }
		}
		else if (evt.getSource() == this.menuSaveSettingsAction)
		{
			saveFile(false);
		}
		else if (evt.getSource() == this.menuSaveSettingsAsAction)
		{
			saveFile(true);
		}
		else if (evt.getSource() == this.menuExitAction)
		{
			if (this.m_bDocumentChangedFlag)
			{
				String fileName;
				if (this.m_currentFile == null)
					fileName = "Untitled";
				else
					fileName = m_currentFile.getName();
				
		        int result = JOptionPane.showConfirmDialog(this,"Do you want to save changes to " + fileName + "?", "HPLC Method Creator", JOptionPane.YES_NO_CANCEL_OPTION);
		        
		        if (result == JOptionPane.YES_OPTION)
		        {
		        	if (!saveFile(false))
		        		return;
		        }
		        else if (result == JOptionPane.CANCEL_OPTION)
		        {
		        	return;
		        }
			}
			
			this.setVisible(false);
			System.exit(0); 
		}
		else if (evt.getSource() == this.menuAboutAction)
		{
			//Frame[] frames = Frame.getFrames();
			AboutDialog aboutDialog = new AboutDialog(this);
	    	Point dialogPosition = new Point(this.getSize().width / 2, this.getSize().height / 2);
	    	dialogPosition.x -= aboutDialog.getWidth() / 2;
	    	dialogPosition.y -= aboutDialog.getHeight() / 2;
	    	aboutDialog.setLocation(dialogPosition);
	    	
	    	// Show the dialog.
	    	aboutDialog.setVisible(true);

		}
		//else if (evt.getSource() == this.menuHelpTopicsAction)
		//{
		//	Globals.hbMainHelpBroker.setCurrentID("getting_started");
		//	Globals.hbMainHelpBroker.setDisplayed(true);
		//}
		else if (evt.getActionCommand() == "Optimize")
	    {
			contentPane.jbtnOptimize.setEnabled(false);
			// Optimize the gradient
			OptimizeGradientTask task = new OptimizeGradientTask();
	        task.execute();
	    }
	}

	//@Override
	public void stateChanged(ChangeEvent e) 
	{
		JSlider source = (JSlider)e.getSource();
		NumberFormat intFormatter = new DecimalFormat("#0");

		if (source.getName() == "Temperature Slider")
		{
			if (m_bSliderUpdate == false)
				return;
			
			m_dTemperature = contentPane.jxpanelChromatographyProperties.jsliderTemp.getValue();
			contentPane.jxpanelChromatographyProperties.jtxtTemp.setText(intFormatter.format(m_dTemperature));
			performCalculations();
		}
		else if (source.getName() == "Solvent B Slider")
		{
			if (m_bSliderUpdate == false)
				return;
			
			m_dSolventBFraction = ((double)contentPane.jxpanelIsocraticOptions.jsliderSolventBFraction.getValue() / (double)100);
			contentPane.jxpanelIsocraticOptions.jtxtSolventBFraction.setText(intFormatter.format(m_dSolventBFraction * 100));		
			performCalculations();
		}

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
				(e.getKeyChar() == KeyEvent.VK_PERIOD) ||
				(e.getKeyChar() == KeyEvent.VK_COMMA))))
		{
	        e.consume();
		}
		
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
		{
			performCalculations();
		}
		
	}

	public void performCalculations()
	{
		NumberFormat formatter = new DecimalFormat("#0.0000");
		NumberFormat floatFormatter = new DecimalFormat("#0.0#########");

		try {
			validateTemp();
			validateSolventBFraction();
			validateMixingVolume();
			validateNonMixingVolume();
			validateColumnLength();
			validateColumnDiameter();
			validateInterparticlePorosity();
			validateIntraparticlePorosity();
			validateFlowRate();
			validateParticleSize();
			validateATerm();
			validateBTerm();
			validateCTerm();
			validateInjectionVolume();
			validateTimeConstant();
			validateNoise();
			validateSignalOffset();
			validateStartTime();
			validateEndTime();
			validateNumPoints();
			validateTubingDiameter();
			validateTubingLength();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Calculate the dead time profile
		// Create dead time array
        this.m_DeadTimeArray = new double[Globals.dDeadTimeArray.length][2];
        
        for (int i = 0; i < Globals.dDeadTimeArray.length; i++)
        {
        	double dVolumeInRefColumn = Math.PI * Math.pow(Globals.dRefColumnID / 2, 2) * Globals.dRefColumnLength;
        	double dDeadVolPerVol = (Globals.dDeadTimeArray[i][1] * Globals.dRefFlowRate) / dVolumeInRefColumn;
        	double dNewDeadVol = dDeadVolPerVol * Math.PI * Math.pow((this.m_dColumnDiameter / 2) / 10, 2) * this.m_dColumnLength / 10;
        	this.m_DeadTimeArray[i][0] = Globals.dDeadTimeArray[i][0];
        	this.m_DeadTimeArray[i][1] = (dNewDeadVol / this.m_dFlowRate) * 60;
        }
        
		this.m_InterpolatedDeadTimeProfile = new InterpolationFunction(this.m_DeadTimeArray);

		m_dTotalPorosity = this.m_dInterparticlePorosity + this.m_dIntraparticlePorosity * (1 - this.m_dInterparticlePorosity);
		contentPane.jxpanelColumnProperties.jlblTotalPorosityOut.setText(formatter.format(m_dTotalPorosity));
		
		m_dVoidVolume = Math.PI * Math.pow(((m_dColumnDiameter / 10) / 2), 2) * (m_dColumnLength / 10) * m_dTotalPorosity;
		contentPane.jxpanelColumnProperties.jlblVoidVolume.setText(formatter.format(m_dVoidVolume));
		
		m_dVoidTime = (m_dVoidVolume / m_dFlowRate) * 60;
		contentPane.jxpanelColumnProperties.jlblVoidTime.setText(formatter.format(m_dVoidTime));
		
		m_dOpenTubeVelocity = (m_dFlowRate / 60) / (Math.PI * Math.pow(((m_dColumnDiameter / 10) / 2), 2));
		contentPane.jxpanelChromatographyProperties.jlblOpenTubeVelocity.setText(formatter.format(m_dOpenTubeVelocity));

		m_dInterstitialVelocity = m_dOpenTubeVelocity / this.m_dInterparticlePorosity;
		contentPane.jxpanelChromatographyProperties.jlblInterstitialVelocity.setText(formatter.format(m_dInterstitialVelocity));
		
		m_dChromatographicVelocity = m_dOpenTubeVelocity / this.m_dTotalPorosity;
		contentPane.jxpanelChromatographyProperties.jlblChromatographicVelocity.setText(formatter.format(m_dChromatographicVelocity));
			
		NumberFormat bpFormatter = new DecimalFormat("#0.00");
		contentPane.jxpanelGradientOptions.jlblDwellVolumeIndicator.setText(bpFormatter.format(m_dMixingVolume + m_dNonMixingVolume));
		contentPane.jxpanelGradientOptions.jlblDwellTimeIndicator.setText(bpFormatter.format(((m_dMixingVolume + m_dNonMixingVolume) / 1000) / m_dFlowRate));

		double dTempKelvin = m_dTemperature + 273.15;

		// Calculate eluent viscosity
		if (m_iSolventB == 0)
		{
			// This formula is for acetonitrile/water mixtures:
			// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
			m_dEluentViscosity = Math.exp((m_dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - m_dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (m_dSolventBFraction * (1 - m_dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
		}
		else if (m_iSolventB == 1)
		{
			// This formula is for methanol/water mixtures:
			// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30–44.
			m_dEluentViscosity = Math.exp((m_dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - m_dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (m_dSolventBFraction * (1 - m_dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
		}
		if (!m_bGradientMode)
			contentPane.jxpanelGeneralProperties.jlblEluentViscosity.setText(formatter.format(m_dEluentViscosity));
		else
			contentPane.jxpanelGeneralProperties.jlblEluentViscosity.setText("--");
			
		/*m_dBackpressure = 500 * (m_dEluentViscosity / 1000) * (((m_dOpenTubeVelocity / 100) * (m_dColumnLength / 1000)) / Math.pow(m_dParticleSize / 1000000, 2));
		if (!m_bGradientMode)
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText(bpFormatter.format(m_dBackpressure / 100000));
		else
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText("--");
			*/
		
		// Calculate backpressure (in pascals) (Darcy equation)
		// See Thompson, J. D.; Carr, P. W. Anal. Chem. 2002, 74, 4150-4159.
		// Backpressure in units of Pa
		m_dBackpressure = ((this.m_dOpenTubeVelocity / 100.0) * (this.m_dColumnLength / 1000.0) * (m_dEluentViscosity / 1000.0) * 180.0 * Math.pow(1 - this.m_dInterparticlePorosity, 2)) / (Math.pow(this.m_dInterparticlePorosity, 3) * Math.pow(m_dParticleSize / 1000000, 2));
		
		// Calculate the average diffusion coefficient using Wilke-Chang empirical determination
		// See Wilke, C. R.; Chang, P. AICHE J. 1955, 1, 264-270.
		
		// First, determine association parameter
		double dAssociationParameter = ((1 - m_dSolventBFraction) * (2.6 - 1.9)) + 1.9;
		
		// Determine weighted average molecular weight of solvent
		double dSolventBMW;
		if (this.m_iSolventB == 0)
			dSolventBMW = 41;
		else
			dSolventBMW = 32;
		
		double dSolventMW = (m_dSolventBFraction * (dSolventBMW - 18)) + 18;
		
		// Determine the average molar volume
		double dAverageMolarVolume = 0;
		for (int i = 0; i < m_vectCompound.size(); i++)
		{
			dAverageMolarVolume += m_vectCompound.get(i).dMolarVolume;
		}
		dAverageMolarVolume = dAverageMolarVolume / m_vectCompound.size();
		
		// Now determine the average diffusion coefficient
		m_dDiffusionCoefficient = 0.000000074 * (Math.pow(dAssociationParameter * dSolventMW, 0.5) * dTempKelvin) / (m_dEluentViscosity * Math.pow(dAverageMolarVolume, 0.6));
		DecimalFormat df = new DecimalFormat("0.000E0");
		contentPane.jxpanelGeneralProperties.jlblDiffusionCoefficient.setText(df.format(m_dDiffusionCoefficient));
		
		// Determine the reduced flow velocity
		m_dReducedFlowVelocity = ((m_dParticleSize / 10000) * m_dInterstitialVelocity) / m_dDiffusionCoefficient;
		contentPane.jxpanelChromatographyProperties.jlblReducedVelocity.setText(formatter.format(m_dReducedFlowVelocity));
		
		// Calculate reduced plate height
		m_dReducedPlateHeight = m_dATerm + (m_dBTerm / m_dReducedFlowVelocity) + (m_dCTerm * m_dReducedFlowVelocity);
		contentPane.jxpanelColumnProperties.jlblReducedPlateHeight.setText(formatter.format(m_dReducedPlateHeight));
    	
		// Calculate HETP
		m_dHETP = (m_dParticleSize / 10000) * m_dReducedPlateHeight;
		contentPane.jxpanelChromatographyProperties.jlblHETP.setText(df.format(m_dHETP));
		
		// Calculate number of theoretical plates
		NumberFormat NFormatter = new DecimalFormat("#0");
		m_dTheoreticalPlates = (m_dColumnLength / 10) / m_dHETP;
		contentPane.jxpanelChromatographyProperties.jlblTheoreticalPlates.setText(NFormatter.format(m_dTheoreticalPlates));

		// TODO: Account for changes in eluent viscosity during a gradient
		// Calculate post-column tubing volume (in uL)
		double dTubingVolume = (this.m_dTubingLength / 100) * (Math.PI * Math.pow((this.m_dTubingDiameter * 0.0000254) / 2, 2) * 1000000000);
		this.contentPane.jxpanelExtraColumnTubing.jlblTubingVolume.setText(formatter.format(dTubingVolume));

		// Calculate retention time delay from post-column tubing in seconds
		double dTubingDelay = ((dTubingVolume / 1000) / m_dFlowRate) * 60;
		
		// Get extra-column tubing radius in units of cm
		double dTubingRadius = (this.m_dTubingDiameter * 0.00254) / 2;

		// Open tube velocity in cm/s
		double dTubingOpenTubeVelocity = (m_dFlowRate / 60) / (Math.PI * Math.pow(dTubingRadius, 2));

		// Calculate dispersion that will result from extra-column tubing
		// in cm^2
		double dTubingZBroadening = (2 * m_dDiffusionCoefficient * this.m_dTubingLength / dTubingOpenTubeVelocity) + ((Math.pow(dTubingRadius, 2) * m_dTubingLength * dTubingOpenTubeVelocity) / (24 * m_dDiffusionCoefficient));
		
		// convert to mL^2
		double dTubingVolumeBroadening = Math.pow(Math.sqrt(dTubingZBroadening) * Math.PI * Math.pow(dTubingRadius, 2), 2);
		
		// convert to s^2
		double dTubingTimeBroadening = Math.pow((Math.sqrt(dTubingVolumeBroadening) / m_dFlowRate) * 60, 2);
		
		// Get flow rate in m^3/s
		// 1 m^3 = 1000000 cm^3
		double dFlowRateInUnits = (m_dFlowRate / 1000000) / 60;

		// Calculate backpressure from tubing
		// Gives pressure drop in Pa
		m_dTubingPressureDrop = (8 * (m_dEluentViscosity / 1000) * (m_dTubingLength / 100) * dFlowRateInUnits) / (Math.PI * Math.pow(dTubingRadius / 100, 4));
		
		if (!m_bGradientMode)
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText(bpFormatter.format((m_dBackpressure + m_dTubingPressureDrop) / 100000));
		else
			contentPane.jxpanelChromatographyProperties.jlblBackpressure.setText("--");

		// Calculate retention factors
		int iNumCompounds = m_vectCompound.size();
		
		if (this.m_bGradientMode)
		{
	    	// Calculate the time period we're going to be looking at:
	    	if (m_bAutomaticTimeRange == true)
	    	{
				m_dStartTime = 0;
				int iLastRow = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() - 1;
				m_dEndTime = ((Double)contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iLastRow, 0)) * 60;
				m_dEndTime += (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) * 60;
		    	contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(floatFormatter.format(m_dEndTime));
		    	contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText("0");
			}
	    	
			calculateGradient();
			
	    	// Scale dtstep correctly for long and short runs - use the total gradient time as a reference
			double dtstep = (m_dEndTime - m_dStartTime) / 1000;
			this.m_vectRetentionFactorArray = new Vector<double[]>();
			this.m_vectPositionArray = new Vector<double[]>();
			
			for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
			{
				double dIntegral = 0;
				double dtRFinal = 0;
				double dD = 0;
				double dTotalTime = 0;
				double dTotalDeadTime = 0;
				double dXPosition = 0;
				double[] dLastXPosition = {0,0};
				double[] dLastko = {0,0};
				double dXMovement = 0;
				Boolean bIsEluted = false;
				double dPhiC = 0;
				double dCurVal = 0;
				boolean bRecordRetentionFactor = false;
				boolean bRecordPosition = false;
				
				if (contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex() == iCompound)
					bRecordRetentionFactor = true;
				
				if (contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex() == iCompound)
					bRecordPosition = true;
				
		    	// Calculate logk'w1
		    	//double logkprimew1 = (m_vectCompound.get(iCompound).dLogkwvsTSlope[this.m_iSolventB] * this.m_dTemperature) + m_vectCompound.get(iCompound).dLogkwvsTIntercept[this.m_iSolventB];
		    	// Calculate S1
		    	//double S1 = -1 * ((m_vectCompound.get(iCompound).dSvsTSlope[this.m_iSolventB] * this.m_dTemperature) + m_vectCompound.get(iCompound).dSvsTIntercept[this.m_iSolventB]);
				
				double t = 0;
		    	double kprime = 1;
		    	
				while (bIsEluted == false)// (double t = 0; t <= (Double) m_vectCompound.get(m_vectCompound.size() - 1)[1] * 1.5; t += dtstep)
				{
					t += dtstep;
					dPhiC = this.m_lifGradient.getAt((dTotalTime - dIntegral) / 60) / 100;
					// Calculate k'
			    	kprime = Math.pow(10, m_vectCompound.get(iCompound).interpolatedLogkvsPhi.getAt(dPhiC));
					dCurVal = dtstep / kprime;
					// The following line is when we aren't using a measure dead time profile
					//double dt0 = m_dVoidTime;
					double dt0 = m_InterpolatedDeadTimeProfile.getAt(dPhiC);// / 60;
					dXMovement = dCurVal / dt0;

					if (bRecordRetentionFactor)
					{
						double[] temp = {dTotalTime,kprime};
						m_vectRetentionFactorArray.add(temp);
					}

					if (bRecordPosition)
					{
						double[] temp = {dTotalTime,dXPosition * m_dColumnLength};
						m_vectPositionArray.add(temp);
					}

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

				contentPane.vectChemicalRows.get(iCompound).set(2, "--");
				
		    	double dRetentionTime = dtRFinal + dTubingDelay;
		    	m_vectCompound.get(iCompound).dRetentionTime = dRetentionTime;
		    	contentPane.vectChemicalRows.get(iCompound).set(3, formatter.format(dRetentionTime / 60));
		    	
		    	// TODO: The following equation does not account for peak broadening due to injection volume.
		    	// Use the final value of k to determine the peak width.
		    	double dSigma = Math.sqrt(Math.pow((m_dVoidTime * (1 + kprime)) / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2)/* + Math.pow(0.017 * m_dInjectionVolume / m_dFlowRate, 2)*/ + dTubingTimeBroadening);
		    	m_vectCompound.get(iCompound).dSigma = dSigma;	    	
		    	contentPane.vectChemicalRows.get(iCompound).set(4, formatter.format(dSigma));
		    	
		    	double dW = (m_dInjectionVolume / 1000000) * m_vectCompound.get(iCompound).dConcentration;
		    	m_vectCompound.get(iCompound).dW = dW;
		    	contentPane.vectChemicalRows.get(iCompound).set(5, formatter.format(dW * 1000000));		    	
			}
		}
		else
		{
			// Isocratic mode
			
			// Make sure the table is initialized
			if (contentPane.vectChemicalRows.size() == iNumCompounds)
			{
				for (int i = 0; i < iNumCompounds; i++)
				{
			    	// Calculate logk'w1
			    	//double logkprimew1 = (m_vectCompound.get(i).dLogkwvsTSlope[this.m_iSolventB] * this.m_dTemperature) + m_vectCompound.get(i).dLogkwvsTIntercept[this.m_iSolventB];
			    	// Calculate S1
			    	//double S1 = -1 * ((m_vectCompound.get(i).dSvsTSlope[this.m_iSolventB] * this.m_dTemperature) + m_vectCompound.get(i).dSvsTIntercept[this.m_iSolventB]);
					// Calculate k'
					
			    	double kprime = Math.pow(10, m_vectCompound.get(i).interpolatedLogkvsPhi.getAt(this.m_dSolventBFraction));
			    	
			    	contentPane.vectChemicalRows.get(i).set(2, formatter.format(kprime));
			    	
			    	if (contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.getSelectedIndex() == i)
			    	{
			    		m_dSelectedIsocraticRetentionFactor = kprime;	
			    	}
			    	
			    	// In seconds
			    	double dRetentionTime = (m_dVoidTime * (1 + kprime)) + dTubingDelay;
			    	m_vectCompound.get(i).dRetentionTime = dRetentionTime;
			    	contentPane.vectChemicalRows.get(i).set(3, formatter.format(dRetentionTime / 60));
			    	
			    	// 9/22/11 - Peak broadening due to sample injection volume is underestimated.
			    	//double dSigma = Math.sqrt(Math.pow(dRetentionTime / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2) + Math.pow(0.017 * m_dInjectionVolume / m_dFlowRate, 2));
			    	double dSigma = Math.sqrt(Math.pow(dRetentionTime / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2) + (1.0/12.0) * Math.pow((m_dInjectionVolume / 1000.0) / (m_dFlowRate / 60.0), 2) + dTubingTimeBroadening);
			    	m_vectCompound.get(i).dSigma = dSigma;	    	
			    	contentPane.vectChemicalRows.get(i).set(4, formatter.format(dSigma));
			    	
			    	double dW = (m_dInjectionVolume / 1000000) * m_vectCompound.get(i).dConcentration;
			    	m_vectCompound.get(i).dW = dW;
			    	contentPane.vectChemicalRows.get(i).set(5, formatter.format(dW * 1000000));
				}
			}
		}

    	// Now calculate the time period we're going to be looking at:
    	if (m_bAutomaticTimeRange == true)
    	{
	    	// Find the compound with the longest tR
	    	double dLongestRetentionTime = 0;
	    	
	    	for (int i = 0; i < m_vectCompound.size(); i++)
	    	{
	    		if (m_vectCompound.get(i).dRetentionTime > dLongestRetentionTime)
	    		{
	    			dLongestRetentionTime = m_vectCompound.get(i).dRetentionTime;
	    		}
	    	}
	    	
	    	m_dEndTime = dLongestRetentionTime * 1.1;
	    	
	    	contentPane.jxpanelGeneralProperties.jtxtFinalTime.setText(floatFormatter.format(m_dEndTime));
	    	
	    	contentPane.jxpanelGeneralProperties.jtxtInitialTime.setText("0");
	    	
	    	m_dStartTime = 0;
    	}

    	// Clear the old chromatogram
    	contentPane.m_GraphControl.RemoveAllSeries();

		if (m_iSecondPlotType == 1)
			plotGradient();
		if (m_iSecondPlotType == 2 || m_iSecondPlotType == 3)
			plotViscosityOrBackpressure();
		if (m_iSecondPlotType == 4)
			plotRetentionFactor();
		if (m_iSecondPlotType == 5)
			plotPosition();
		if (m_iSecondPlotType == 6)
			plotNumberOfCompoundsElutingAtOnce();

    	// Calculate each data point
    	Random random = new Random();
    	
    	//contentPane.m_GraphControl.RemoveSeries(m_iChromatogramPlotIndex);
    	m_iChromatogramPlotIndex = -1;
    	
    	// Clear the single plot if it exists (the red plot that shows up if you click on a compound)
    	//contentPane.m_GraphControl.RemoveSeries(m_iSinglePlotIndex);
    	m_iSinglePlotIndex = null;
    	
    	if (this.m_vectCompound.size() > 0)
    	{
    		m_iChromatogramPlotIndex = contentPane.m_GraphControl.AddSeries("Chromatogram", new Color(98, 101, 214), 1, false, false);
	    	// Find if a chemical is selected
	    	int[] iRowSel = contentPane.jtableChemicals.getSelectedRows();
	    	m_iSinglePlotIndex = new int[iRowSel.length];

	    	for (int i = 0; i < iRowSel.length; i++)
	    	{
	    		int thisRowSel = iRowSel[i];
	    		
	    		if (thisRowSel >= 0 && thisRowSel < contentPane.vectChemicalRows.size())
		    	{
		    		m_iSinglePlotIndex[i] = contentPane.m_GraphControl.AddSeries("Single " + Integer.toString(i), new Color(206, 70, 70), 1, false, false);
		    		iRowSel[i] = contentPane.jtableChemicals.convertRowIndexToModel(thisRowSel);
		    	}
	    	}
	    	
	    	for (int i = 0; i < this.m_iNumPoints; i++)
	    	{
	    		double dTime = m_dStartTime + (double)i * ((m_dEndTime - m_dStartTime) / (double)this.m_iNumPoints);
	    		double dNoise = random.nextGaussian() * (m_dNoise / 1000000000);
	    		double dCTotal = (dNoise / Math.sqrt(m_dTimeConstant)) + m_dSignalOffset;
	    		
	    		// Add the contribution from each compound to the peak
	    		for (int j = 0; j < m_vectCompound.size(); j++)
	    		{
	    			Compound curCompound = m_vectCompound.get(j);
	    			//double dCthis = ((curCompound.dW / 1000000) / (curCompound.dSigma * (m_dFlowRate / (60 * 1000)))) * Math.exp(-0.5*Math.pow((dTime - curCompound.dRetentionTime) / (curCompound.dSigma), 2));
	    			double dCthis = ((curCompound.dW / 1000000) / (Math.sqrt(2 * Math.PI) * curCompound.dSigma * (m_dFlowRate / (60 * 1000)))) * Math.exp(-Math.pow(dTime - curCompound.dRetentionTime, 2) / (2 * Math.pow(curCompound.dSigma, 2)));
	    			dCTotal += dCthis;
	    			
	    			// If a compound is selected, then show it in a different color and without noise.
	    			for (int x = 0; x < iRowSel.length; x++)
	    			{
	    				if (m_iSinglePlotIndex[x] >= 0 && j == iRowSel[x])
	    					contentPane.m_GraphControl.AddDataPoint(m_iSinglePlotIndex[x], dTime, (dCthis + m_dSignalOffset));
	    			}
	    		}
	    		
		    	contentPane.m_GraphControl.AddDataPoint(m_iChromatogramPlotIndex, dTime, dCTotal);
	    	}
	    	
	    	if (contentPane.jbtnAutoscaleX.isSelected() == true)
	    		contentPane.m_GraphControl.AutoScaleX();	//AutoScaleToSeries(iTotalPlotIndex);
	    	if (contentPane.jbtnAutoscaleY.isSelected() == true)
	    		contentPane.m_GraphControl.AutoScaleY();
    	}
    	
    	contentPane.m_GraphControl.repaint();   	
    	contentPane.jtableChemicals.addNotify();
	}

	public void plotViscosityOrBackpressure()
	{
		// TODO: Considers only viscosity of solvent entering column, not the average viscosity of all solvent in the column
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		
		
		double dViscosityMin = 999999999;
		double dViscosityMax = 0;
		double dBackpressureMin = 999999999;
		double dBackpressureMax = 0;
		
		int iNumPoints = m_dGradientArray.length;
		
		double dTempKelvin = m_dTemperature + 273.15;
		if (this.m_bGradientMode)
		{
			double dFinalValue = 0;
			
			//double[][] dPoints = new double[iNumPoints][2];
			
			for (int i = 0; i < iNumPoints; i++)
			{
				double dViscosity = 0;
				double dSolventBFraction = m_dGradientArray[i][1] / 100;
				// Calculate eluent viscosity
				if (m_iSolventB == 0)
				{
					// This formula is for acetonitrile/water mixtures:
					// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
					dViscosity = Math.exp((dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
				}
				else if (m_iSolventB == 1)
				{
					// This formula is for methanol/water mixtures:
					// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30–44.
					dViscosity = Math.exp((dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
				}
				
				if (dViscosity < dViscosityMin)
					dViscosityMin = dViscosity;
				if (dViscosity > dViscosityMax)
					dViscosityMax = dViscosity;
				
				if (this.m_iSecondPlotType == 2)
				{
					// Calculate backpressure (in pascals) (Darcy equation)
					// See Thompson, J. D.; Carr, P. W. Anal. Chem. 2002, 74, 4150-4159.
					// Backpressure in units of Pa
					double dBackpressure = ((this.m_dOpenTubeVelocity / 100.0) * (this.m_dColumnLength / 1000.0) * (dViscosity / 1000.0) * 180.0 * Math.pow(1 - this.m_dInterparticlePorosity, 2)) / (Math.pow(this.m_dInterparticlePorosity, 3) * Math.pow(m_dParticleSize / 1000000, 2));
					
					double dTubingRadius = (this.m_dTubingDiameter * 0.00254) / 2;

					// Get flow rate in m^3/s
					// 1 m^3 = 1000000 cm^3
					double dFlowRateInUnits = (m_dFlowRate / 1000000) / 60;

					// Calculate backpressure from tubing
					// Gives pressure drop in Pa
					double dTubingPressureDrop = (8 * (dViscosity / 1000) * (m_dTubingLength / 100) * dFlowRateInUnits) / (Math.PI * Math.pow(dTubingRadius / 100, 4));
					
					contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, (dBackpressure + dTubingPressureDrop) / 100000);
				    //dPoints[i][0] = m_dGradientArray[i][0] * 60;
				    //dPoints[i][1] = dBackpressure / 100000;
				    dFinalValue = (dBackpressure + dTubingPressureDrop) / 100000;
				    
				    if (dFinalValue < dBackpressureMin)
				    	dBackpressureMin = dFinalValue;
					if (dFinalValue > dBackpressureMax)
						dBackpressureMax = dFinalValue;
				}
				else if (this.m_iSecondPlotType == 3)
				{
				    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, dViscosity / 100);
				    dFinalValue = dViscosity / 100;
				}
			}
			
		    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, 9999999999d, dFinalValue);

			if (this.m_iSecondPlotType == 2)
			{
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dBackpressureMin, dBackpressureMax);
			}
			else if (this.m_iSecondPlotType == 3)
			{
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dViscosityMin / 100, dViscosityMax / 100);
			}	
		}
		else
		{
			// Isocratic Mode
			if (this.m_iSecondPlotType == 2)
			{
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, (m_dBackpressure + m_dTubingPressureDrop) / 100000);
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, (m_dBackpressure + m_dTubingPressureDrop) / 100000);
				double dBackPressureMin = (m_dBackpressure + m_dTubingPressureDrop) - ((m_dBackpressure + m_dTubingPressureDrop) * 0.2);
				double dBackPressureMax = (m_dBackpressure + m_dTubingPressureDrop) + ((m_dBackpressure + m_dTubingPressureDrop) * 0.2);
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dBackPressureMin / 100000, dBackPressureMax / 100000);
			}
			else if (this.m_iSecondPlotType == 3)
			{
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dEluentViscosity / 100);
			    contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dEluentViscosity / 100);
		    	dViscosityMin = m_dEluentViscosity - (m_dEluentViscosity * 0.2);
		    	dViscosityMax = m_dEluentViscosity + (m_dEluentViscosity * 0.2);
				contentPane.m_GraphControl.setSecondYAxisRangeLimits(dViscosityMin / 100, dViscosityMax / 100);
			}

		}
	}
	
	//@Override
	public void focusGained(FocusEvent e) 
	{
		
	}

	//@Override
	public void focusLost(FocusEvent e) 
	{
		performCalculations();
	}

	//@Override
	public void valueChanged(ListSelectionEvent arg0) 
	{
		performCalculations();
		
	}

	//@Override
	public void autoScaleChanged(AutoScaleEvent event) 
	{
		if (event.getAutoScaleXState() == true)
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

	@Override
	public void tableChanged(TableModelEvent e) 
	{
		if (e.getSource() == contentPane.jxpanelGradientOptions.tmGradientProgram)
		{
			if (m_bDoNotChangeTable)
			{
				m_bDoNotChangeTable = false;
				return;
			}
			
			int iChangedRow = e.getFirstRow();
			int iChangedColumn = e.getColumn();

			Double dRowValue1 = 0.0;
			Double dRowValue2 = 0.0;
			
			if (iChangedRow < contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount())
			{
				dRowValue1 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow, 0);
				dRowValue2 = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow, 1);
			}
			
	    	if (iChangedColumn == 0)
			{
				// If the column changed was the first, then make sure the time falls in the right range
				if (iChangedRow == 0)
				{
					// No changes allowed in first row - must be zero min
					dRowValue1 = 0.0;
				}
				else if (iChangedRow == contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() - 1)
				{
					Double dPreviousTime = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount() - 2, 0);
					// If it's the last row, just make sure the time is greater than or equal to the time before it.
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
				}
				else
				{
					Double dPreviousTime = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow - 1, 0);
					Double dNextTime = (Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iChangedRow + 1, 0);
					
					if (dRowValue1 < dPreviousTime)
						dRowValue1 = dPreviousTime;
					
					if (dRowValue1 > dNextTime)
						dRowValue1 = dNextTime;
				}
				
		    	m_bDoNotChangeTable = true;
		    	contentPane.jxpanelGradientOptions.tmGradientProgram.setValueAt(dRowValue1, iChangedRow, iChangedColumn);
			}
			else if (iChangedColumn == 1)
			{
				// If the column changed was the second, then make sure the solvent composition falls between 0 and 100
				if (dRowValue2 > 100)
					dRowValue2 = 100.0;
				
				if (dRowValue2 < 0)
					dRowValue2 = 0.0;
				
		    	m_bDoNotChangeTable = true;
		    	contentPane.jxpanelGradientOptions.tmGradientProgram.setValueAt(dRowValue2, iChangedRow, iChangedColumn);
			}
	    	
	    	performCalculations();
		}		
	}
	
	public void calculateGradient()
	{
		int iNumPoints = 1000;
		m_dGradientArray = new double[iNumPoints][2];
		int iGradientTableLength = contentPane.jxpanelGradientOptions.tmGradientProgram.getRowCount();
		
		// Initialize the solvent mixer composition to that of the initial solvent composition
		double dMixerComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(0, 1)).doubleValue();
		//double dFinalTime = (((m_dMixingVolume * 3 + m_dNonMixingVolume) / 1000) / m_dFlowRate) + ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iGradientTableLength - 1, 0)).doubleValue();
		double dFinalTime = this.m_dEndTime / 60;
		double dTimeStep = dFinalTime / (iNumPoints - 1);
		
		// Start at time 0
		double dTime = 0;
		
		for (int i = 0; i < iNumPoints; i++)
		{
			dTime = i * dTimeStep;
			
			m_dGradientArray[i][0] = dTime;
			m_dGradientArray[i][1] = dMixerComposition;
			
			//if (((m_dFlowRate * 1000) * dTimeStep) < m_dMixingVolume)
			//{
				double dSolventBInMixer = dMixerComposition * m_dMixingVolume;
							
				// Now push out a step's worth of volume from the mixer
				dSolventBInMixer -= ((m_dFlowRate * 1000) * dTimeStep) * dMixerComposition;
				
				// Now add a step's worth of new volume from the pump
				// First, find which two data points we are between
				// Find the last data point that isn't greater than our current time
				double dIncomingSolventComposition = 0;
				if (dTime < (m_dNonMixingVolume / 1000) / m_dFlowRate)
				{
					dIncomingSolventComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(0, 1)).doubleValue();
				}
				else
				{
					int iRowBefore = 0;
					for (int j = 0; j < iGradientTableLength; j++)
					{
						double dRowTime = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(j, 0)).doubleValue();
						if (dRowTime <= (dTime - ((m_dNonMixingVolume / 1000) / m_dFlowRate)))
							iRowBefore = j;
						else
							break;
					}
					
					// Now interpolate between the solvent composition at iRowBefore and the next row (if it exists)
					double dRowBeforeTime = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore, 0)).doubleValue();
					
					if (iRowBefore <= iGradientTableLength - 2)
					{
						double dRowAfterTime = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore + 1, 0)).doubleValue();
						double dPositionBetween = ((dTime - ((m_dNonMixingVolume / 1000) / m_dFlowRate)) - dRowBeforeTime) / (dRowAfterTime - dRowBeforeTime);
						double dRowBeforeComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore, 1)).doubleValue();
						double dRowAfterComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore + 1, 1)).doubleValue();
						dIncomingSolventComposition = (dPositionBetween * (dRowAfterComposition - dRowBeforeComposition)) + dRowBeforeComposition;
					}
					else
						dIncomingSolventComposition = ((Double) contentPane.jxpanelGradientOptions.tmGradientProgram.getValueAt(iRowBefore, 1)).doubleValue();
				}
				
				dSolventBInMixer += ((m_dFlowRate * 1000) * dTimeStep) * dIncomingSolventComposition;
				
				// Calculate the new solvent composition in the mixing volume
				if (((m_dFlowRate * 1000) * dTimeStep) < m_dMixingVolume)
					dMixerComposition = dSolventBInMixer / m_dMixingVolume;
				else
					dMixerComposition = dIncomingSolventComposition;
		}
		
		m_lifGradient = new LinearInterpolationFunction(m_dGradientArray);
	}
	
	public void plotGradient()
	{
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		
    	
		if (this.m_bGradientMode)
		{
	    	for (int i = 0; i < m_dGradientArray.length; i++)
	    	{
		    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dGradientArray[i][0] * 60, m_dGradientArray[i][1]);
	    	}
	    	
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, 9999999999d, m_dGradientArray[m_dGradientArray.length - 1][1]);
		}
		else
		{
			// Isocratic mode
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, this.m_dStartTime, this.m_dSolventBFraction * 100);			
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, this.m_dEndTime, this.m_dSolventBFraction * 100);			
		}
	}
	
	class DataPointComparator implements Comparator<double[]>
	{
		@Override
		public int compare(double[] arg0, double[] arg1) 
		{
			if (arg0[0] > arg1[0])
			{
				return 1;
			}
			else if (arg0[0] < arg1[0])
			{
				return -1;
			}
			else
				return 0;
		}
	}
	
	public void plotNumberOfCompoundsElutingAtOnce()
	{
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		
    	
		// Make a list of beginnings and endings of each compound
		double[][] addRemoveList = new double[this.m_vectCompound.size() * 2][2];
		
		for (int i = 0; i < m_vectCompound.size(); i++)
		{
			addRemoveList[i * 2][0] = m_vectCompound.get(i).dRetentionTime - 2 * m_vectCompound.get(i).dSigma;
			addRemoveList[i * 2][1] = 1;
			addRemoveList[(i * 2) + 1][0] = m_vectCompound.get(i).dRetentionTime + 2 * m_vectCompound.get(i).dSigma;
			addRemoveList[(i * 2) + 1][1] = -1;
		}
		
		// Now sort the list
		Comparator<double[]> byXVal = new DataPointComparator();
		Arrays.sort(addRemoveList, byXVal);

		int iCounter = 0;
		int iMaxCompounds = 0;
		for (int i = 0; i < addRemoveList.length; i++)
		{
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, addRemoveList[i][0], (double)iCounter);
			iCounter += addRemoveList[i][1];

	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, addRemoveList[i][0], (double)iCounter);

			if (iCounter > iMaxCompounds)
				iMaxCompounds = iCounter;
		}
		
    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, 9999999999d, 0);
	
		contentPane.m_GraphControl.setSecondYAxisRangeLimits(0, (double)iMaxCompounds);
	}
	
	public void plotRetentionFactor()
	{
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		
		
		double dRetentionFactorMin = 999999999;
		double dRetentionFactorMax = 0;
   	
		if (this.m_bGradientMode)
		{
	    	for (int i = 0; i < this.m_vectRetentionFactorArray.size(); i++)
	    	{
	    		double dRetentionFactor = m_vectRetentionFactorArray.get(i)[1];
	    		
				if (dRetentionFactor < dRetentionFactorMin)
					dRetentionFactorMin = dRetentionFactor;
				if (dRetentionFactor > dRetentionFactorMax)
					dRetentionFactorMax = dRetentionFactor;
	
		    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_vectRetentionFactorArray.get(i)[0], m_vectRetentionFactorArray.get(i)[1]);
	    	}
	    	
		}
		else
		{
			// Isocratic Mode
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dSelectedIsocraticRetentionFactor);
	    	contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dSelectedIsocraticRetentionFactor);
	    	dRetentionFactorMin = m_dSelectedIsocraticRetentionFactor - (m_dSelectedIsocraticRetentionFactor * 0.5);
	    	dRetentionFactorMax = m_dSelectedIsocraticRetentionFactor + (m_dSelectedIsocraticRetentionFactor * 0.5);
		}
    	
		contentPane.m_GraphControl.setSecondYAxisRangeLimits(dRetentionFactorMin, dRetentionFactorMax);
	}
	
	public void plotPosition()
	{
		contentPane.m_GraphControl.RemoveSeries(m_iSecondPlotIndex);
		m_iSecondPlotIndex = -1;
		
		m_iSecondPlotIndex = contentPane.m_GraphControl.AddSeries("SecondPlot", new Color(130, 130, 130), 1, false, true);	    		

		if (this.m_bGradientMode)
		{
	    	for (int i = 0; i < m_vectPositionArray.size(); i++)
	    	{
	    		contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_vectPositionArray.get(i)[0], m_vectPositionArray.get(i)[1]);
	    	}
		}
		else
		{
			// Isocratic Mode
			int iSelectedCompound = contentPane.jxpanelPlotOptions.jcboPositionCompounds.getSelectedIndex();
			if (iSelectedCompound < m_vectCompound.size() && iSelectedCompound >= 0)
			{
				double dRetentionTime = m_vectCompound.get(iSelectedCompound).dRetentionTime;
			
				contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dStartTime, m_dStartTime * (m_dColumnLength / dRetentionTime));
				contentPane.m_GraphControl.AddDataPoint(m_iSecondPlotIndex, m_dEndTime, m_dEndTime * (m_dColumnLength / dRetentionTime));
			}
		}
    	
		contentPane.m_GraphControl.setSecondYAxisRangeLimits(0, m_dColumnLength);
	}
	
	public void updateCompoundComboBoxes()
	{
		int iNumCompounds = m_vectCompound.size();
		
		contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.removeAllItems();
		contentPane.jxpanelPlotOptions.jcboPositionCompounds.removeAllItems();
		
		for (int i = 0; i < iNumCompounds; i++)
		{
			contentPane.jxpanelPlotOptions.jcboRetentionFactorCompounds.addItem(m_vectCompound.get(i).strCompoundName);
			contentPane.jxpanelPlotOptions.jcboPositionCompounds.addItem(m_vectCompound.get(i).strCompoundName);
		}
	}
	
	
    class OptimizeGradientTask extends SwingWorker<Void, Void> 
    {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() 
        {
    		//contentPane2.jProgressBar.setString("Determining gradient delay volume...");
            
            optimizeGradient(this);
            return null;
        }
        
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() 
        {
			contentPane.jbtnOptimize.setEnabled(true);

        	if (!this.isCancelled())
        	{
	    		//contentPane2.jProgressBar.setIndeterminate(false);
	    		//contentPane2.jProgressBar.setStringPainted(true);
	    		//contentPane2.jbtnNextStep.setEnabled(true);
        	}
        }
    }
    
	public void optimizeGradient(OptimizeGradientTask task)
	{
		long starttime = System.currentTimeMillis();
		NumberFormat floatFormatter = new DecimalFormat("#0.0#########");

		// Step #1: Find gradient that gives us shortest separation with resolution no worse than 1.5
		// Try 100 different gradient times between 0.5 min and 50 min
		int iNumTries = 100;
		double[] dScore = new double[iNumTries];
		
		for (int i = 1; i <= iNumTries; i++)
		{
			// Create gradient to test
			double tG = (double)i * 0.5;
			
			m_optGradient = new double[3][2];
			m_optGradient[0][0] = 0;
			m_optGradient[0][1] = 5;
			m_optGradient[1][0] = tG;
			m_optGradient[1][1] = 95;
			m_optGradient[2][0] = tG + 1;
			m_optGradient[2][1] = 95;
			
			LinearInterpolationFunction lifGradient = new LinearInterpolationFunction(m_optGradient);
			
			// Now calculate retention times and peak widths in the gradient
			// Calculate the time period we're going to be looking at:
			m_dStartTime = 0;
			m_dEndTime = tG * 60;
			double dtstep = (m_dEndTime - m_dStartTime) / 1000;
			
			dScore[i - 1] = calcScore(lifGradient);
		}
		
		double dMinScore = dScore[0];
		int iIndexOfMinScore = 0;
		
		for (int i = 0; i < dScore.length; i++)
		{
			if (dScore[i] < dMinScore)
			{
				dMinScore = dScore[i];
				iIndexOfMinScore = i;
			}
		}

		double dInittG = (double)(iIndexOfMinScore + 1) * 0.5;
		int iNumDataPoints = 10;

		m_dStartTime = 0;
		m_dEndTime = dInittG * 60;
		double dtstep = (m_dEndTime - m_dStartTime) / 1000;

		// Create initial gradient array
		m_optGradient = new double[iNumDataPoints][2];
		for (int i = 0; i < iNumDataPoints; i++)
		{
			m_optGradient[i][0] = dInittG * ((double)i / (double)(iNumDataPoints - 1));
			m_optGradient[i][1] = 90.0 * ((double)i / (double)(iNumDataPoints - 1)) + 5.0;
		}

		// What was the final retention time in that run?
		double[][] dRetentionAndSigma = calcRetentionTimes(new LinearInterpolationFunction(m_optGradient));
		
		// Calc run time
		double dLatestTimeInLinearRun = 0;
		for (int j = 0; j < dRetentionAndSigma.length; j++)
		{
			dLatestTimeInLinearRun = Math.max(dLatestTimeInLinearRun, dRetentionAndSigma[j][0] + (dRetentionAndSigma[j][1] * 3));
		}
		
		// Now optimize the gradient
		double dPercentBStep = .1;
		double dMaxChangeAtOnce = 2;
		double dPercentBPrecision = 0.001;

		// Loop through each point in the gradient and optimize the score
		double dLastFullIterationTime = 9999;
		double dIterationTime = 0;
		double dOptScore = 0;
		
		while (Math.abs(dLastFullIterationTime - dIterationTime) / dLastFullIterationTime > 0.0001)
		{
			dLastFullIterationTime = dIterationTime;
			
			for (int i = 0; i < iNumDataPoints; i++)
			{
				dOptScore = goldenSectioningSearchGradientProfile(i, dtstep, dPercentBStep, dPercentBPrecision, dMaxChangeAtOnce, 0);
			}
			dIterationTime = dOptScore;
			
			// Put optimized gradient in table
	    	m_bDoNotChangeTable = true;
			contentPane.jxpanelGradientOptions.tmGradientProgram.setRowCount(0);
			
			for (int i = 0; i < iNumDataPoints; i++)
			{
				if (i < iNumDataPoints - 1)
					m_bDoNotChangeTable = true;
				
				contentPane.jxpanelGradientOptions.tmGradientProgram.addRow(new Double[] {m_optGradient[i][0], m_optGradient[i][1]});
			}
		}
		
		// What was the final retention time in that run?
		dRetentionAndSigma = calcRetentionTimes(new LinearInterpolationFunction(m_optGradient));
		
		// Calc run time
		double dLatestTimeInOptimizedRun = 0;
		for (int j = 0; j < dRetentionAndSigma.length; j++)
		{
			dLatestTimeInOptimizedRun = Math.max(dLatestTimeInOptimizedRun, dRetentionAndSigma[j][0] + (dRetentionAndSigma[j][1] * 3));
		}

		
		// Now create message box
		JOptionPane.showMessageDialog(null, "Linear gradient run length: " + Float.toString((float)dLatestTimeInLinearRun / 60) + " min\n" + "Fully optimized gradient length: " + Float.toString((float)dLatestTimeInOptimizedRun / 60) + " min\n\n" + Float.toString((float)(dLatestTimeInOptimizedRun/dLatestTimeInLinearRun)) + "%", "Optimization Results", JOptionPane.INFORMATION_MESSAGE);
		
		updateTime(starttime);
	}
	
	// Need to negatively score a separation in which two compounds of same mass elute together
	// Need to keep the run as short as possible
	// Need to negatively score a separation in which 5 compounds elute at once
	public double calcScore(LinearInterpolationFunction lifGradient)
	{
		double[][] dRetentionAndSigma = calcRetentionTimes(lifGradient);
		
		// Calc minimum resolution
		/*double dMinResolution = 9999;
		for (int j = 0; j < dRetentionAndSigma.length; j++)
		{
			for (int k = j + 1; k < dRetentionAndSigma.length; k++)
			{
				double dResolution = (Math.abs(dRetentionAndSigma[j][0] - dRetentionAndSigma[k][0]) * 2) / (dRetentionAndSigma[j][1] * 4 + dRetentionAndSigma[k][1] * 4);
				if (dResolution < dMinResolution)
				{
					dMinResolution = dResolution;
				}
			}
		}*/
		
		// Calc run time
		double dLatestTime = 0;
		for (int j = 0; j < dRetentionAndSigma.length; j++)
		{
			dLatestTime = Math.max(dLatestTime, dRetentionAndSigma[j][0] + (dRetentionAndSigma[j][1] * 3));
		}
		
		// Calc negative score for separation in which 5 compounds elute at once
		// Calc amount of time that is spent with too many compounds eluting at once.
		
		// First make a list of beginnings and endings of each compound
		double[][] addRemoveList = new double[dRetentionAndSigma.length * 2][2];
		
		for (int i = 0; i < dRetentionAndSigma.length; i++)
		{
			addRemoveList[i * 2][0] = dRetentionAndSigma[i][0] - (2 * dRetentionAndSigma[i][1]);
			addRemoveList[i * 2][1] = 1;
			addRemoveList[(i * 2) + 1][0] = dRetentionAndSigma[i][0] + (2 * dRetentionAndSigma[i][1]);
			addRemoveList[(i * 2) + 1][1] = -1;
		}
		
		// Now sort the list
		Comparator<double[]> byXVal = new DataPointComparator();
		Arrays.sort(addRemoveList, byXVal);

		int iCounter = 0;
		int iMaxCompounds = 0;
		double dTooManyScore = 0;
		double dTooFewScore = 0;
		
		// Add to the score from the first part where no peaks are coming out.
    	dTooFewScore += Math.pow(Math.max(5 - iCounter, 0), 2) * addRemoveList[0][0];

		for (int i = 0; i < addRemoveList.length - 1; i++)
		{
	    	iCounter += addRemoveList[i][1];

	    	// If counter is above 5 then multiply times the amount of time that it is.
	    	dTooManyScore += Math.pow(Math.max(iCounter - 5, 0), 2) * (addRemoveList[i + 1][0] - addRemoveList[i][0]);

	    	// If counter is below 5 then multiply times the amount of time that it is.
	    	dTooFewScore += Math.pow(Math.max(5 - iCounter, 0), 2) * (addRemoveList[i + 1][0] - addRemoveList[i][0]);

			if (iCounter > iMaxCompounds)
				iMaxCompounds = iCounter;
		}
		
		dTooManyScore = Math.pow(dTooManyScore, 2) + 1;
		dTooFewScore = Math.pow(dTooFewScore, 1) + 1;
		return dLatestTime * dTooManyScore * dTooFewScore;
	}
	
 	public double goldenSectioningSearchGradientProfile(int iIndex, double dtstep, double dStep, double dPrecision, double dMaxChangeAtOnce, double dAngleErrorMultiplier)
 	{
		double dRetentionError = 1;
		double x1;
		double x2;
		double x3;
		double dRetentionErrorX1;
		double dRetentionErrorX2;
		double dRetentionErrorX3;
		double dAngleErrorX1 = 0;
		double dAngleErrorX2 = 0;
		double dAngleErrorX3 = 0;
		
		double dLastTempGuess = m_optGradient[iIndex][1];
		LinearInterpolationFunction lifGradient = new LinearInterpolationFunction(m_optGradient);
		
		// Find bounds
		x1 = m_optGradient[iIndex][1];
		dRetentionErrorX1 = calcScore(lifGradient);
		//dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
		
		x2 = x1 + dStep;
		m_optGradient[iIndex][1] = x2;
		lifGradient = new LinearInterpolationFunction(m_optGradient);
		dRetentionErrorX2 = calcScore(lifGradient);
		//dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
		
		if (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier))
		{
			// We're going in the right direction
			x3 = x2;
			dRetentionErrorX3 = dRetentionErrorX2;
			dAngleErrorX3 = dAngleErrorX2;
			
			x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
			
			m_optGradient[iIndex][1] = x2;
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionErrorX2 = calcScore(lifGradient);
			//dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
			

			while (dRetentionErrorX2 * Math.pow(dAngleErrorX2, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x2 < dLastTempGuess + dMaxChangeAtOnce)
			{
				x1 = x3;
				dRetentionErrorX1 = dRetentionErrorX3;
				dAngleErrorX1 = dAngleErrorX3;
				x3 = x2;
				dRetentionErrorX3 = dRetentionErrorX2;
				dAngleErrorX3 = dAngleErrorX2;
				
				x2 = (x3 - x1) * this.m_dGoldenRatio + x3;
				
				m_optGradient[iIndex][1] = x2;
				lifGradient = new LinearInterpolationFunction(m_optGradient);
				dRetentionErrorX2 = calcScore(lifGradient);
				//dAngleErrorX2 = calcAngleDifferenceGradient(iIndex);
			}
		}
		else
		{
			// We need to go in the opposite direction
			x3 = x1;
			dRetentionErrorX3 = dRetentionErrorX1;
			dAngleErrorX3 = dAngleErrorX1;
			
			x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
			
			m_optGradient[iIndex][1] = x1;
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionErrorX1 = calcScore(lifGradient);
			//dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);

			while (dRetentionErrorX1 * Math.pow(dAngleErrorX1, dAngleErrorMultiplier) < dRetentionErrorX3 * Math.pow(dAngleErrorX3, dAngleErrorMultiplier) && x1 > dLastTempGuess - dMaxChangeAtOnce)
			{
				x2 = x3;
				dRetentionErrorX2 = dRetentionErrorX3;
				dAngleErrorX2 = dAngleErrorX3;
				x3 = x1;
				dRetentionErrorX3 = dRetentionErrorX1;
				dAngleErrorX3 = dAngleErrorX1;

				x1 = x3 - (x2 - x3) * this.m_dGoldenRatio;
				
				m_optGradient[iIndex][1] = x1;
				lifGradient = new LinearInterpolationFunction(m_optGradient);
				dRetentionErrorX1 = calcScore(lifGradient);
				//dAngleErrorX1 = calcAngleDifferenceGradient(iIndex);
			}
		}
		
		// Now we have our bounds (x1 to x2) and the results of one guess (x3)
		if (x2 > dLastTempGuess + dMaxChangeAtOnce)
		{
			if (dLastTempGuess + dMaxChangeAtOnce > 100)
				m_optGradient[iIndex][1] = 100;
			else
				m_optGradient[iIndex][1] = dLastTempGuess + dMaxChangeAtOnce;
			
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionError = calcScore(lifGradient);

			return dRetentionError;
		}
		
		if (x1 < dLastTempGuess - dMaxChangeAtOnce)
		{
			if (dLastTempGuess - dMaxChangeAtOnce < -100)
				m_optGradient[iIndex][1] = -100;
			else
				m_optGradient[iIndex][1] = dLastTempGuess - dMaxChangeAtOnce;
			
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionError = calcScore(lifGradient);
			
			return dRetentionError;
		}
		
		// Loop of optimization
		while ((x2 - x1) > dPrecision)
		{
			double x4;
			double dRetentionErrorX4;
			double dAngleErrorX4 = 0;
			
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

			
			m_optGradient[iIndex][1] = x4;
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionErrorX4 = calcScore(lifGradient);
			//dAngleErrorX4 = calcAngleDifferenceGradient(iIndex);
			
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
			m_optGradient[iIndex][1] = 100;
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionError = calcScore(lifGradient);		
		}
		else if (x3 < -100)
		{
			m_optGradient[iIndex][1] = -100;
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionError = calcScore(lifGradient);	
		}
		else
		{
			m_optGradient[iIndex][1] = x3;
			lifGradient = new LinearInterpolationFunction(m_optGradient);
			dRetentionError = dRetentionErrorX3;			
		}
 		
 		return dRetentionError;
 	}
 	
	public double[][] calcRetentionTimes(LinearInterpolationFunction lifGradient)
	{
		double dtstep = (m_dEndTime - m_dStartTime) / 1000;

		int iNumCompounds = m_vectCompound.size();

		double[][] dRetentionAndSigma = new double[iNumCompounds][2];
		
		for (int iCompound = 0; iCompound < iNumCompounds; iCompound++)
		{
			double dIntegral = 0;
			double dtRFinal = 0;
			double dD = 0;
			double dTotalTime = 0;
			double dTotalDeadTime = 0;
			double dXPosition = 0;
			double[] dLastXPosition = {0,0};
			double[] dLastko = {0,0};
			double dXMovement = 0;
			Boolean bIsEluted = false;
			double dPhiC = 0;
			double dCurVal = 0;
			
	    	// Calculate logk'w1
	    	//double logkprimew1 = (m_vectCompound.get(iCompound).dLogkwvsTSlope[this.m_iSolventB] * this.m_dTemperature) + m_vectCompound.get(iCompound).dLogkwvsTIntercept[this.m_iSolventB];
	    	// Calculate S1
	    	//double S1 = -1 * ((m_vectCompound.get(iCompound).dSvsTSlope[this.m_iSolventB] * this.m_dTemperature) + m_vectCompound.get(iCompound).dSvsTIntercept[this.m_iSolventB]);
			
			double t = 0;
	    	double kprime = 1;
	    	
			while (bIsEluted == false)// (double t = 0; t <= (Double) m_vectCompound.get(m_vectCompound.size() - 1)[1] * 1.5; t += dtstep)
			{
				t += dtstep;
				dPhiC = lifGradient.getAt((dTotalTime - dIntegral) / 60) / 100;
				// Calculate k'
				
		    	kprime = Math.pow(10, m_vectCompound.get(iCompound).interpolatedLogkvsPhi.getAt(dPhiC));
				dCurVal = dtstep / kprime;
				// The following line is when we aren't using a measure dead time profile
				//double dt0 = m_dVoidTime;
				double dt0 = m_InterpolatedDeadTimeProfile.getAt(dPhiC) / 60;

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

			contentPane.vectChemicalRows.get(iCompound).set(2, "--");
			
			dRetentionAndSigma[iCompound][0] = dtRFinal;
			dRetentionAndSigma[iCompound][1] = Math.sqrt(Math.pow((m_dVoidTime * (1 + kprime)) / Math.sqrt(m_dTheoreticalPlates), 2) + Math.pow(m_dTimeConstant, 2));
		}
		
		return dRetentionAndSigma;
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
		//contentPane2.jlblTimeElapsed.setText(strProgress2);
    }
}
