package visualizer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileNameExtensionFilter;

import dialogs.AboutDialog;

public class FluidVisualizationApp extends JFrame implements ActionListener, KeyListener, FocusListener, PartPlacementPanelListener
{
	private static final long serialVersionUID = 1L;

	private static final int PATH_BLOCKED = 0;
	private static final int OVER_PRESSURE = 1;
	
	public class SystemProblem
	{
		int ErrorID = 0;
		HPLCPart ProblemPart;
	}
	
	public JScrollPane jMainScrollPane = null;
	BackPanel contentPane = null;
    JMenuItem menuNewAction = new JMenuItem("New");
    JMenuItem menuOpenAction = new JMenuItem("Open...");
    JMenuItem menuSaveAction = new JMenuItem("Save");
    JMenuItem menuSaveAsAction = new JMenuItem("Save As...");
    JMenuItem menuExitAction = new JMenuItem("Exit");
    
    JCheckBoxMenuItem menuShowGrid = new JCheckBoxMenuItem("Show grid", true);
    JRadioButtonMenuItem menuUseMinutes = new JRadioButtonMenuItem("Use minutes", true);
    JRadioButtonMenuItem menuUseSeconds = new JRadioButtonMenuItem("Use seconds", false);
    
    JMenuItem menuAboutAction = new JMenuItem("About HPLC Fluid Visualizer...");

    File m_currentFile = null;
    boolean m_bDocumentChangedFlag = false;
    
    boolean m_bUseMinutes = true;
    
    public Vector<SystemProblem> m_vectSystemProblems = new Vector<SystemProblem>();
    
	/**
	 * This is the xxx default constructor
	 */
	public FluidVisualizationApp(String str) 
	{
	    super(str);
	    
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

		this.setPreferredSize(new Dimension(943, 615));
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
        FluidVisualizationApp frame = new FluidVisualizationApp("HPLC Fluid Visualizer");
        
		//java.net.URL url1 = ClassLoader.getSystemResource("org/hplcsimulator/images/icon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
	    Image img = kit.createImage(frame.getClass().getResource("/images/icon.png"));
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
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() 
	{
        // Load the JavaHelp
		/*String helpHS = "org/retentionprediction/help/RetentionPredictorHelp.hs";
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
        
    }
    
    private void createGUI()
    {
    	// Creates a menubar for a JFrame
        JMenuBar menuBar = new JMenuBar();
        
        // Add the menubar to the frame
        setJMenuBar(menuBar);
        
        // Define and add two drop down menu to the menubar
        JMenu fileMenu = new JMenu("File");
        JMenu viewMenu = new JMenu("View");
        JMenu aboutMenu = new JMenu("About");
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(aboutMenu);
        
        // Create and add simple menu item to one of the drop down menu
        menuNewAction.addActionListener(this);
        menuOpenAction.addActionListener(this);
        menuSaveAction.addActionListener(this);
        menuSaveAsAction.addActionListener(this);
        menuExitAction.addActionListener(this);

        menuShowGrid.addActionListener(this);
        menuUseSeconds.addActionListener(this);
        menuUseMinutes.addActionListener(this);
        
        menuAboutAction.addActionListener(this);
        
        fileMenu.add(menuNewAction);
        fileMenu.add(menuOpenAction);
        fileMenu.add(menuSaveAction);
        fileMenu.add(menuSaveAsAction);
        fileMenu.addSeparator();
        fileMenu.add(menuExitAction);
        
        viewMenu.add(menuShowGrid);
        viewMenu.addSeparator();
        viewMenu.add(menuUseMinutes);
        viewMenu.add(menuUseSeconds);
        
        aboutMenu.add(menuAboutAction);

        //Create and set up the first content pane (steps 1-4).
    	jMainScrollPane = new JScrollPane();
    	jMainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	jMainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    	contentPane = new BackPanel();
        contentPane.setOpaque(true);
        jMainScrollPane.setViewportView(contentPane);
    	setContentPane(jMainScrollPane);
    	
    	contentPane.jPartPlacementPanel.addAddPartListener(this);
    	contentPane.jbtnSelectTool.addActionListener(this);
    	contentPane.jbtnRemoveTool.addActionListener(this);
    	contentPane.jbtnEditTool.addActionListener(this);
    	contentPane.jbtn4mil.addActionListener(this);
    	contentPane.jbtn5mil.addActionListener(this);
    	contentPane.jbtn7mil.addActionListener(this);
    	contentPane.jbtn10mil.addActionListener(this);
    	contentPane.jbtn20mil.addActionListener(this);
    	contentPane.jbtn30mil.addActionListener(this);
    	contentPane.jbtnOtherTube.addActionListener(this);
    	
    	for (int i = 0; i < this.contentPane.jxpanelValvesPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelValvesPanel.JToggleButtons[i].addActionListener(this);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns18Panel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelColumns18Panel.JToggleButtons[i].addActionListener(this);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns35Panel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelColumns35Panel.JToggleButtons[i].addActionListener(this);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns46Panel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelColumns46Panel.JToggleButtons[i].addActionListener(this);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelAutosamplersPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelAutosamplersPanel.JToggleButtons[i].addActionListener(this);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelDetectorsPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelDetectorsPanel.JToggleButtons[i].addActionListener(this);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelPumpsPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelPumpsPanel.JToggleButtons[i].addActionListener(this);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelWastePanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelWastePanel.JToggleButtons[i].addActionListener(this);
    	}
    	
    	contentPane.jPartPlacementPanel.addDocumentChangedListener(this);
    }

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
	
    public boolean saveFile(boolean bSaveAs)
    {
		if (bSaveAs == false && m_currentFile != null)
		{
			try 
			{
                FileOutputStream fos = new FileOutputStream(m_currentFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(this.contentPane.jPartPlacementPanel.getHPLCParts());
				oos.flush();
				oos.close();
				this.m_bDocumentChangedFlag = false;
				return true;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
		        JOptionPane.showMessageDialog(this, "The file could not be saved.", "Error saving file", JOptionPane.ERROR_MESSAGE);
			}
		}
		else
		{	
			JFileChooser2 fc = new JFileChooser2();
			
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Fluid Visualizer Files (*.lc)", "lc");
			fc.setFileFilter(filter);
			fc.setDialogTitle("Save As...");
			int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                m_currentFile = fc.getSelectedFile();
                String path = m_currentFile.getAbsolutePath();
                if (path.lastIndexOf(".") >= 0)
                	path = path.substring(0, path.lastIndexOf("."));
                	
                m_currentFile = new File(path + ".lc");

                try 
                {
                    FileOutputStream fos = new FileOutputStream(m_currentFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(this.contentPane.jPartPlacementPanel.getHPLCParts());
					oos.flush();
					oos.close();
					this.m_bDocumentChangedFlag = false;
					return true;
				} 
                catch (IOException e) 
				{
					e.printStackTrace();
			        JOptionPane.showMessageDialog(this, "The file could not be saved.", "Error saving file", JOptionPane.ERROR_MESSAGE);
				}
            }
		}
		return false;
    }
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getSource() == this.menuNewAction)
		{
			if (this.m_bDocumentChangedFlag)
			{
				String fileName;
				if (this.m_currentFile == null)
					fileName = "Untitled";
				else
					fileName = m_currentFile.getName();
				
		        int result = JOptionPane.showConfirmDialog(this,"Do you want to save changes to " + fileName + "?", "HPLC Fluid Visualizer", JOptionPane.YES_NO_CANCEL_OPTION);
		        
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
			this.contentPane.jPartPlacementPanel.setHPLCParts(null);
			this.recalculateSystemParams();
		}
		else if (arg0.getSource() == this.menuOpenAction)
		{
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Fluid Visualizer Files (*.lc)", "lc");
			fc.setFileFilter(filter);
			fc.setDialogTitle("Open");
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                m_currentFile = fc.getSelectedFile();

                try 
                {
                    FileInputStream fis = new FileInputStream(m_currentFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    this.contentPane.jPartPlacementPanel.setHPLCParts((Vector<HPLCPart>)ois.readObject());
					ois.close();
				} 
                catch (IOException e) 
                {
					e.printStackTrace();
			        JOptionPane.showMessageDialog(this, "The file is not a valid HPLC Fluid Visualizer file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
			        m_currentFile = null;
				} 
                catch (ClassNotFoundException e) 
                {
					e.printStackTrace();
			        JOptionPane.showMessageDialog(this, "The file is not a valid HPLC Fluid Visualizer file.", "Error opening file", JOptionPane.ERROR_MESSAGE);
			        m_currentFile = null;
                }
            }
			this.recalculateSystemParams();
		}
		else if (arg0.getSource() == this.menuSaveAction)
		{
			saveFile(false);
		}
		else if (arg0.getSource() == this.menuSaveAsAction)
		{
			saveFile(true);
		}
		else if (arg0.getSource() == this.menuExitAction)
		{
			if (this.m_bDocumentChangedFlag)
			{
				String fileName;
				if (this.m_currentFile == null)
					fileName = "Untitled";
				else
					fileName = m_currentFile.getName();
				
		        int result = JOptionPane.showConfirmDialog(this,"Do you want to save changes to " + fileName + "?", "HPLC Fluid Visualizer", JOptionPane.YES_NO_CANCEL_OPTION);
		        
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
		else if (arg0.getSource() == this.menuAboutAction)
		{
			Frame[] frames = Frame.getFrames();
			AboutDialog aboutDialog = new AboutDialog(frames[0]);
	    	Point dialogPosition = new Point(this.getSize().width / 2, this.getSize().height / 2);
	    	dialogPosition.x -= aboutDialog.getWidth() / 2;
	    	dialogPosition.y -= aboutDialog.getHeight() / 2;
	    	aboutDialog.setLocation(dialogPosition);
	    	
	    	// Show the dialog.
	    	aboutDialog.setVisible(true);

		}
		else if (arg0.getSource() == this.menuShowGrid)
		{
			this.contentPane.jPartPlacementPanel.m_bShowGrid = this.menuShowGrid.isSelected();
			this.contentPane.jPartPlacementPanel.repaint();
		}
		else if (arg0.getSource() == this.menuUseMinutes)
		{
			this.menuUseMinutes.setSelected(true);
			this.menuUseSeconds.setSelected(false);
			
			this.m_bUseMinutes = true;
			
			recalculateSystemParams();
		}
		else if (arg0.getSource() == this.menuUseSeconds)
		{
			this.menuUseMinutes.setSelected(false);
			this.menuUseSeconds.setSelected(true);
			
			this.m_bUseMinutes = false;
			
			recalculateSystemParams();
		}
		
    	for (int i = 0; i < this.contentPane.jxpanelValvesPanel.JToggleButtons.length; i++)
    	{
    		if (arg0.getSource() != this.contentPane.jxpanelValvesPanel.JToggleButtons[i])
    			this.contentPane.jxpanelValvesPanel.JToggleButtons[i].setSelected(false);
    		else
    		{
    			if (this.contentPane.jxpanelValvesPanel.JToggleButtons[i].isSelected())
    			{
    				HPLCPartValve newPart = this.contentPane.jxpanelValvesPanel.getHPLCPartFromButton(i);
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
    			}
    			else
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
    			
    			resetToolbarButtons();
    			contentPane.jbtnSelectTool.setSelected(true);
    			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
    		}

    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns18Panel.JToggleButtons.length; i++)
    	{
    		if (arg0.getSource() != this.contentPane.jxpanelColumns18Panel.JToggleButtons[i])
    			this.contentPane.jxpanelColumns18Panel.JToggleButtons[i].setSelected(false);
    		else
    		{
    			if (this.contentPane.jxpanelColumns18Panel.JToggleButtons[i].isSelected())
    			{
    				HPLCPartColumn newPart = this.contentPane.jxpanelColumns18Panel.getHPLCPartFromButton(i);
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
    			}
    			else
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
    			
    			resetToolbarButtons();
    			contentPane.jbtnSelectTool.setSelected(true);
    			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
    		}
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns35Panel.JToggleButtons.length; i++)
    	{
    		if (arg0.getSource() != this.contentPane.jxpanelColumns35Panel.JToggleButtons[i])
    			this.contentPane.jxpanelColumns35Panel.JToggleButtons[i].setSelected(false);
    		else
    		{
    			if (this.contentPane.jxpanelColumns35Panel.JToggleButtons[i].isSelected())
    			{
    				HPLCPartColumn newPart = this.contentPane.jxpanelColumns35Panel.getHPLCPartFromButton(i);
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
    			}
    			else
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
    			
    			resetToolbarButtons();
    			contentPane.jbtnSelectTool.setSelected(true);
    			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
    		}
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns46Panel.JToggleButtons.length; i++)
    	{
    		if (arg0.getSource() != this.contentPane.jxpanelColumns46Panel.JToggleButtons[i])
    			this.contentPane.jxpanelColumns46Panel.JToggleButtons[i].setSelected(false);
    		else
    		{
    			if (this.contentPane.jxpanelColumns46Panel.JToggleButtons[i].isSelected())
    			{
    				HPLCPartColumn newPart = this.contentPane.jxpanelColumns46Panel.getHPLCPartFromButton(i);
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
    			}
    			else
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
    			
    			resetToolbarButtons();
    			contentPane.jbtnSelectTool.setSelected(true);
    			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
    		}
    	}
    	for (int i = 0; i < this.contentPane.jxpanelAutosamplersPanel.JToggleButtons.length; i++)
    	{
    		if (arg0.getSource() != this.contentPane.jxpanelAutosamplersPanel.JToggleButtons[i])
    			this.contentPane.jxpanelAutosamplersPanel.JToggleButtons[i].setSelected(false);
    		else
    		{
    			if (this.contentPane.jxpanelAutosamplersPanel.JToggleButtons[i].isSelected())
    			{
    				HPLCPartAutosampler newPart = this.contentPane.jxpanelAutosamplersPanel.getHPLCPartFromButton(i);
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
    			}
    			else
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
    			
    			resetToolbarButtons();
    			contentPane.jbtnSelectTool.setSelected(true);
    			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
    		}
    	}
		for (int i = 0; i < this.contentPane.jxpanelPumpsPanel.JToggleButtons.length; i++)
		{
			if (arg0.getSource() != this.contentPane.jxpanelPumpsPanel.JToggleButtons[i])
				this.contentPane.jxpanelPumpsPanel.JToggleButtons[i].setSelected(false);
			else
			{
				if (this.contentPane.jxpanelPumpsPanel.JToggleButtons[i].isSelected())
				{
					HPLCPartPump newPart = this.contentPane.jxpanelPumpsPanel.getHPLCPartFromButton(i);
					this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
				}
				else
					this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
				
				resetToolbarButtons();
				contentPane.jbtnSelectTool.setSelected(true);
				contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
			}
		}
		for (int i = 0; i < this.contentPane.jxpanelDetectorsPanel.JToggleButtons.length; i++)
		{
			if (arg0.getSource() != this.contentPane.jxpanelDetectorsPanel.JToggleButtons[i])
				this.contentPane.jxpanelDetectorsPanel.JToggleButtons[i].setSelected(false);
			else
			{
				if (this.contentPane.jxpanelDetectorsPanel.JToggleButtons[i].isSelected())
				{
					HPLCPartDetector newPart = this.contentPane.jxpanelDetectorsPanel.getHPLCPartFromButton(i);
					this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
				}
				else
					this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
				
				resetToolbarButtons();
				contentPane.jbtnSelectTool.setSelected(true);
				contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
			}
		}
    	for (int i = 0; i < this.contentPane.jxpanelWastePanel.JToggleButtons.length; i++)
    	{
    		if (arg0.getSource() != this.contentPane.jxpanelWastePanel.JToggleButtons[i])
    			this.contentPane.jxpanelWastePanel.JToggleButtons[i].setSelected(false);
    		else
    		{
    			if (this.contentPane.jxpanelWastePanel.JToggleButtons[i].isSelected())
    			{
    				HPLCPartWaste newPart = this.contentPane.jxpanelWastePanel.getHPLCPartFromButton(i);
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(newPart);
    			}
    			else
    				this.contentPane.jPartPlacementPanel.setSelectedHPLCPart(null);
    			
    			resetToolbarButtons();
    			contentPane.jbtnSelectTool.setSelected(true);
    			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
    		}
    	}
		
		if (arg0.getSource() == this.contentPane.jbtnSelectTool)
		{
			resetToolbarButtons();
			contentPane.jbtnSelectTool.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.SELECT_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtnRemoveTool)
		{
			resetToolbarButtons();
			contentPane.jbtnRemoveTool.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.REMOVE_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtnEditTool)
		{
			resetToolbarButtons();
			contentPane.jbtnEditTool.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.EDIT_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtn4mil)
		{
			resetToolbarButtons();
			contentPane.jbtn4mil.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.TUBE_4_MIL_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtn5mil)
		{
			resetToolbarButtons();
			contentPane.jbtn5mil.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.TUBE_5_MIL_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtn7mil)
		{
			resetToolbarButtons();
			contentPane.jbtn7mil.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.TUBE_7_MIL_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtn10mil)
		{
			resetToolbarButtons();
			contentPane.jbtn10mil.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.TUBE_10_MIL_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtn20mil)
		{
			resetToolbarButtons();
			contentPane.jbtn20mil.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.TUBE_20_MIL_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtn30mil)
		{
			resetToolbarButtons();
			contentPane.jbtn30mil.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.TUBE_30_MIL_TOOL);
		}
		else if (arg0.getSource() == this.contentPane.jbtnOtherTube)
		{
			resetToolbarButtons();
			contentPane.jbtnOtherTube.setSelected(true);
			contentPane.jPartPlacementPanel.setSelectedTool(PartPlacementPanel.TUBE_OTHER_TOOL);
		}
	}

	public void resetToolbarButtons()
	{
		contentPane.jbtnSelectTool.setSelected(false);
		contentPane.jbtnRemoveTool.setSelected(false);
		contentPane.jbtnEditTool.setSelected(false);
		contentPane.jbtn4mil.setSelected(false);
		contentPane.jbtn5mil.setSelected(false);
		contentPane.jbtn7mil.setSelected(false);
		contentPane.jbtn10mil.setSelected(false);
		contentPane.jbtn20mil.setSelected(false);
		contentPane.jbtn30mil.setSelected(false);
		contentPane.jbtnOtherTube.setSelected(false);
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
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partAdded(AddPartEvent event) 
	{
    	for (int i = 0; i < this.contentPane.jxpanelValvesPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelValvesPanel.JToggleButtons[i].setSelected(false);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns18Panel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelColumns18Panel.JToggleButtons[i].setSelected(false);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns35Panel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelColumns35Panel.JToggleButtons[i].setSelected(false);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelColumns46Panel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelColumns46Panel.JToggleButtons[i].setSelected(false);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelAutosamplersPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelAutosamplersPanel.JToggleButtons[i].setSelected(false);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelDetectorsPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelDetectorsPanel.JToggleButtons[i].setSelected(false);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelPumpsPanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelPumpsPanel.JToggleButtons[i].setSelected(false);
    	}
    	for (int i = 0; i < this.contentPane.jxpanelWastePanel.JToggleButtons.length; i++)
    	{
    		this.contentPane.jxpanelWastePanel.JToggleButtons[i].setSelected(false);
    	}
	}

	@Override
	public void documentChanged(DocumentChangedEvent event) 
	{
		m_bDocumentChangedFlag = true;
		// Recalculate table parameters
		recalculateSystemParams();
	}
	
	public void recalculateSystemParams()
	{
		this.m_vectSystemProblems.clear();
		
		this.contentPane.tmCalculations.setRowCount(0);
		
		// Run through all the parts looking for pumps.
		// Then follow the path of the pump through to the end and sum up all the backpressures, etc.
		for (int i = 0; i < this.contentPane.jPartPlacementPanel.getHPLCParts().size(); i++)
		{
			HPLCPart thisPart = contentPane.jPartPlacementPanel.getHPLCParts().get(i);
			
			if (thisPart instanceof HPLCPartPump)
			{
				HPLCPartPump thisPump = (HPLCPartPump)thisPart;
				// Trace the path of the fluid and add up each part's contribution to backpressure, band broadening, and gradient delay
				double dBackpressure = 0;
				double dBandBroadening = 0;
				double dGradientDelay = 0;
				boolean bPathBlocked = false;
				
				boolean bBeforeColumn = true;
				
				// Add contribution to gradient delay from the pump
				dGradientDelay += thisPump.getPumpVolume() / thisPump.getFlowRate();
				
				ConnectionNode thisNode = thisPump.getConnectionNodes().get(0);

				boolean bPartConnects = true;
				
				while (bPartConnects)
				{
					bPartConnects = false;
					
					// Check if it is connected. If not, skip it.
					if (thisNode.getConnectedNodes()[0] == null && thisNode.getConnectedNodes()[1] == null)
						break;
					
					// Get the next node
					ConnectionNode nextNode;
					if (thisNode.getConnectedNodes()[0] == null)
						nextNode = thisNode.getConnectedNodes()[1];
					else
						nextNode = thisNode.getConnectedNodes()[0];
					
					// Move down the tube until nextNode is either the end of a tube or a part
					while (nextNode.getNumConnections() > 1)
					{
						if (nextNode.connectedNode1 == thisNode)
						{
							thisNode = nextNode;
							nextNode = nextNode.connectedNode2;
						}
						else
						{
							thisNode = nextNode;
							nextNode = nextNode.connectedNode1;
						}
					}
					
					// Add contribution of tube to gradient delay, backpressure, and peak width

					// Gradient delay:
					if (bBeforeColumn)
						dGradientDelay += nextNode.getGradientDelay(thisPump.getFlowRate());
					
					// Backpressure:
					dBackpressure += nextNode.getPressureDrop(thisPump.getFlowRate(), thisPump.getSolventB(), thisPump.getSolventBFraction());
					
					// Dispersion:
					dBandBroadening += nextNode.getDispersion(thisPump.getFlowRate(), thisPump.getSolventB(), thisPump.getSolventBFraction()) * 1000000;
					
					thisPart = nextNode.getParentHPLCPart();
					
					if (thisPart == null)
						break;
					
					// Add contribution of the part to gradient delay, backpressure, and peak width
					if (bBeforeColumn)
						dGradientDelay += thisPart.getGradientDelay(thisPump.getFlowRate());

					dBackpressure += thisPart.getPressureDrop(thisPump.getFlowRate(), thisPump.getSolventB(), thisPump.getSolventBFraction());
				
					dBandBroadening += thisPart.getDispersion(thisPump.getFlowRate(), thisPump.getSolventB(), thisPump.getSolventBFraction()) * 1000000;
					
					// No more parts can contribute to gradient delay if this part is a column
					if (thisPart instanceof HPLCPartColumn)
						bBeforeColumn = false;
					
					// If this part is a pump or autosampler, then backpressure is infinite
					if (thisPart instanceof HPLCPartPump ||
						thisPart instanceof HPLCPartAutosampler)
					{
						bPathBlocked = true;
						SystemProblem problem = new SystemProblem();
						problem.ErrorID = PATH_BLOCKED;
						problem.ProblemPart = thisPump;
						this.m_vectSystemProblems.add(problem);
					}
					
					thisNode = thisPart.getInternalConnection(nextNode);
					
					if (thisPart instanceof HPLCPartValve &&
							thisNode == null)
					{
						bPathBlocked = true;
						SystemProblem problem = new SystemProblem();
						problem.ErrorID = PATH_BLOCKED;
						problem.ProblemPart = thisPump;
						this.m_vectSystemProblems.add(problem);
					}
						
					if (thisNode != null)
						bPartConnects = true;
				}
				
				Vector<Object> newRow = new Vector<Object>();
				newRow.add(thisPump.getName());
				
				NumberFormat formatter = new DecimalFormat("#0.#############");

				// Check if pump is overpressure and create an error for it
				if (dBackpressure > thisPump.getPressureLimit())
				{
					SystemProblem problem = new SystemProblem();
					problem.ErrorID = OVER_PRESSURE;
					problem.ProblemPart = thisPump;
					this.m_vectSystemProblems.add(problem);
				}

				if (bPathBlocked == false)
					//newRow.add(formatter.format(Globals.roundToSignificantFigures(dBackpressure, 3)) + " bar");
					newRow.add(formatter.format(Globals.roundToSignificantFigures(dBackpressure, 3)) + " bar");
				else
					newRow.add("infinite");
				newRow.add(formatter.format(Globals.roundToSignificantFigures(Math.sqrt(dBandBroadening), 3)) + " \u00b5L");//\u00b2");
				
				if (this.m_bUseMinutes)
					newRow.add(formatter.format(Globals.roundToSignificantFigures(dGradientDelay, 3)) + " min");
				else
					newRow.add(formatter.format(Globals.roundToSignificantFigures(dGradientDelay * 60, 3)) + " sec");
				
				this.contentPane.tmCalculations.addRow(newRow);
			}
		}
		
		displayErrors();
	}
	
	public void displayErrors()
	{
		if (this.m_vectSystemProblems.size() > 0)
		{
			this.contentPane.jlblStatus.setForeground(new Color(255,0,0));

			SystemProblem firstProblem = this.m_vectSystemProblems.get(0);
			if (firstProblem.ErrorID == PATH_BLOCKED)
				this.contentPane.jlblStatus.setText("\"" + firstProblem.ProblemPart.getName() + "\" is blocked! (e.g. it is connected to another pump or autosampler)");
			if (firstProblem.ErrorID == OVER_PRESSURE)
				this.contentPane.jlblStatus.setText("Backpressure for \"" + firstProblem.ProblemPart.getName() + "\" is over the limit!");
		}
		else
		{
			this.contentPane.jlblStatus.setForeground(new Color(0,0,0));
			this.contentPane.jlblStatus.setText("No problems detected.");
		}
	}
}
