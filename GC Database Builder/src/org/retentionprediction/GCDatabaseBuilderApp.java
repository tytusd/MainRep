package org.retentionprediction;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

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

public class GCDatabaseBuilderApp extends JFrame implements ActionListener, KeyListener, FocusListener, ListSelectionListener, AutoScaleListener, TableModelListener, ClipboardOwner
{
	private static final long serialVersionUID = 1L;

	ParentPanel contentPane = null;
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
    public int m_iStage = 1;
    public double m_dtstep = 0.01;
    public double m_V0 = 1; // in mL
    
    public double m_dTmax = 1000;
    public double m_dk = 100;
    
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
    public InterpolationFunction[] m_AlkaneIsothermalDataInterpolated;
    public InterpolationFunction[] m_CompoundIsothermalDataInterpolated;
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

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        //Instantiate the controlling class.
        GCDatabaseBuilderApp frame;
		frame = new GCDatabaseBuilderApp("GC Retention Database Builder"); // create frame with title
        
		//java.net.URL url1 = ClassLoader.getSystemResource("org/hplcsimulator/images/icon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
	    Image img;
		img = kit.createImage(frame.getClass().getResource("/org/retentionprediction/images/gc_retention_predictor_icon.png"));
		frame.setIconImage(img);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the window
        frame.init();

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null); //center it
        frame.setVisible(true);
    }
    
	/**
	 * This is the xxx default constructor
	 */
	public GCDatabaseBuilderApp(String str) 
	{
	    super(str);
	    
		this.setPreferredSize(new Dimension(1000, 730));
	}
	
	/**
	 * This is the xxx default constructor
	 */
	public GCDatabaseBuilderApp() 
	{
	    super();
	    
		this.setPreferredSize(new Dimension(1000, 730));
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() 
	{
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

    	contentPane = new ParentPanel();
        contentPane.setOpaque(true);
        jMainScrollPane.setViewportView(contentPane);
    	setContentPane(jMainScrollPane);
    	jMainScrollPane.revalidate();

        
             
    }

    
    public void actionPerformed(ActionEvent evt) 
	{
	    String strActionCommand = evt.getActionCommand();

	    
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

	}

	@Override
	public void tableChanged(TableModelEvent e) 
	{

	}
	

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
		
	}
}
