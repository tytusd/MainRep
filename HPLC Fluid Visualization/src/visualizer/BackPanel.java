package visualizer;
import javax.swing.JPanel;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
//import javax.media.opengl.*;
import java.awt.Point;
import javax.swing.JButton;
import java.awt.Dimension;
import java.util.EventObject;
import java.util.Vector;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;

import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
//import javax.help.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;

import panels.AutosamplersPanel;
import panels.ColumnsPanel;
import panels.DetectorsPanel;
import panels.PumpsPanel;
import panels.ValvesPanel;
import panels.WastePanel;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
//import javax.media.opengl.GLCapabilities;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

public class BackPanel extends JPanel implements Scrollable, ComponentListener
{
	private static final long serialVersionUID = 1L;
	
	public JXTaskPaneContainer taskpanecontainer = null;
	public ValvesPanel jxpanelValvesPanel = null;
	public JXTaskPane jxtaskValvesPanel = null;
	public ColumnsPanel jxpanelColumns46Panel = null;
	public JXTaskPane jxtaskColumns46Panel = null;
	public ColumnsPanel jxpanelColumns35Panel = null;
	public JXTaskPane jxtaskColumns35Panel = null;
	public ColumnsPanel jxpanelColumns18Panel = null;
	public JXTaskPane jxtaskColumns18Panel = null;
	public PumpsPanel jxpanelPumpsPanel = null;
	public JXTaskPane jxtaskPumpsPanel = null;
	public AutosamplersPanel jxpanelAutosamplersPanel = null;
	public JXTaskPane jxtaskAutosamplersPanel = null;
	public DetectorsPanel jxpanelDetectorsPanel = null;
	public JXTaskPane jxtaskDetectorsPanel = null;
	public WastePanel jxpanelWastePanel = null;
	public JXTaskPane jxtaskWastePanel = null;
	public JScrollPane jScrollPaneMain = null;
	public PartPlacementPanel jPartPlacementPanel = null;
	private JScrollPane jScrollPaneControls = null;
	private JScrollPane jScrollPaneTable = null;
	private JXTable jxTable = null;
	public NoEditTableModel tmCalculations = null;
//	private JToolBar jMainToolBar = null;
	public JToolBar jMainToolBar = null;
	public JToggleButton jbtnRemoveTool = null;
	public JToggleButton jbtnEditTool = null;
	public JToggleButton jbtnSelectTool = null;
	public JToggleButton jbtn5mil = null;
	public JToggleButton jbtn4mil = null;
	public JToggleButton jbtn7mil = null;
	public JToggleButton jbtn10mil = null;
	public JToggleButton jbtn20mil = null;
	public JToggleButton jbtn30mil = null;
	public JToggleButton jbtnOtherTube = null;
	private JPanel jpanelStatus = null;
	public JLabel jlblStatus = null;
	/**
	 * This is the default constructor
	 */
	public BackPanel() 
	{
		super();
		initialize();
	}

	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setLayout(null);
		
		this.setVisible(true);
		this.setSize(new Dimension(943, 615));
		this.setBackground(new Color(238, 238, 238));
        this.setPreferredSize(new Dimension(500,300));
        this.setMinimumSize(new Dimension(500,300));

		this.addComponentListener(this);
		
		//this.add(getJMainToolBar(), BorderLayout.PAGE_START);
	    this.add(getJScrollPaneControls(), null);
	    this.add(getJScrollPaneMain(), null);
	    this.add(getJScrollPaneTable(), null);
	    this.add(getJMainToolBar(), null);
	    this.add(getJpanelStatus(), null);
	    
	    //this.jScrollPaneMain.setSize(this.jScrollPaneMain.getSize());
	    //this.jScrollPaneMain.revalidate();
	    // Move to the middle of the workspace
	    //ComponentEvent fakeEvent = new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED);
	    //componentResized(fakeEvent);
	    //this.revalidate();
	    
	    Dimension workspaceSize = jScrollPaneMain.getViewport().getViewSize();
	    Rectangle viewSize = jScrollPaneMain.getViewportBorderBounds();
	    Point newPosition = new Point((workspaceSize.width / 2) - (viewSize.width / 2), (workspaceSize.height / 2) - (viewSize.height / 2));
	    jScrollPaneMain.getViewport().setViewPosition(newPosition);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public boolean getScrollableTracksViewportHeight() {
		Dimension minSize = this.getMinimumSize();
		Dimension portSize = null;
		if (getParent() instanceof JViewport) 
		{
			JViewport port = (JViewport)getParent();
			portSize = port.getSize();
		}
		else
			return false;
		
		if (portSize.height < minSize.height)
			return false;
		else
			return true;
	}


	@Override
	public boolean getScrollableTracksViewportWidth() {
		Dimension minSize = this.getMinimumSize();
		Dimension portSize = null;
		if (getParent() instanceof JViewport) 
		{
			JViewport port = (JViewport)getParent();
			portSize = port.getSize();
		}
		else
			return false;
		
		if (portSize.width < minSize.width)
			return false;
		else
			return true;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void componentMoved(ComponentEvent arg0) {
		this.revalidate();
	}


	@Override
	public void componentResized(ComponentEvent arg0) 
	{
		// Respond to window resize
		if (arg0.getComponent() == this)
		{
			Dimension size = this.getSize();
			int iTableHeight = 100;
			int iToolBarHeight = 40;
			int iStatusHeight = 20;
			this.jMainToolBar.setBounds(new Rectangle(this.jScrollPaneControls.getWidth(), 0, size.width - this.jScrollPaneControls.getWidth(), iToolBarHeight));
			this.jScrollPaneMain.setBounds(new Rectangle(this.jScrollPaneControls.getWidth(), iToolBarHeight, size.width - this.jScrollPaneControls.getWidth(), size.height - iTableHeight - iToolBarHeight - iStatusHeight));
			this.jScrollPaneTable.setBounds(new Rectangle(this.jScrollPaneControls.getWidth(), size.height - iTableHeight - iStatusHeight, size.width - this.jScrollPaneControls.getWidth(), iTableHeight));
			this.jpanelStatus.setBounds(new Rectangle(this.jScrollPaneControls.getWidth(), size.height - iStatusHeight, size.width - this.jScrollPaneControls.getWidth(), iStatusHeight));
			this.jScrollPaneControls.setBounds(new Rectangle(0, 0, this.jScrollPaneControls.getWidth(), size.height));
		}
		// TODO Auto-generated method stub
		
	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
/*	private JToolBar getJMainToolBar() 
	{
		if (jMainToolBar == null) {
			jMainToolBar = new JToolBar();
			
			JButton button = null;
	        //first button
	        button = makeNavigationButton("Back24", "ActionCommand", "Back to previous something-or-other", "Previous");
	        jMainToolBar.add(button);
		}
		return this.jMainToolBar;
	}*/

	protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText) 
	{
		//Look for the image.
		String imgLocation = "images/" + imageName + ".png";
		
		//Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		//button.addActionListener(this);
		
		button.setText(altText);
	
		return button;
	}
	/**
	 * This method initializes jScrollPaneMain	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPaneMain() {
		if (jScrollPaneMain == null) {
			jScrollPaneMain = new JScrollPane();
			jScrollPaneMain.setSize(new Dimension(685, 457));
			jScrollPaneMain.setLocation(new Point(256, 40));
			jScrollPaneMain.setViewportView(getJPartPlacementPanel());
		}
		return jScrollPaneMain;
	}


	/**
	 * This method initializes jPartPlacementPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PartPlacementPanel getJPartPlacementPanel() {
		if (jPartPlacementPanel == null) {
			jPartPlacementPanel = new PartPlacementPanel();
			jPartPlacementPanel.setLayout(new GridBagLayout());
		}
		return jPartPlacementPanel;
	}


	/**
	 * This method initializes jScrollPaneControls	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPaneControls() {
		if (jScrollPaneControls == null) 
		{
			jScrollPaneControls = new JScrollPane();
			jScrollPaneControls.setLocation(new Point(0, 0));
			jScrollPaneControls.setPreferredSize(new Dimension(256, 224));
			jScrollPaneControls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPaneControls.setSize(new Dimension(256, 613));
		
		    VerticalLayout verticalLayout = new VerticalLayout();
		    verticalLayout.setGap(0);
		    		    
		    // create a taskpanecontainer
		    taskpanecontainer = new JXTaskPaneContainer();
		    // create a taskpane, and set it's title and icon
		    taskpanecontainer.setBorder(null);
		    //taskpanecontainer.setBackground(new Color(238,238,238));
		    taskpanecontainer.setPaintBorderInsets(true);
		    //taskpanecontainer.setPreferredSize(new Dimension(253, 800));
		    taskpanecontainer.setLocation(new Point(0, 0));
		    taskpanecontainer.setSize(new Dimension(253, 800));
		    taskpanecontainer.setLayout(verticalLayout);

		    jxtaskValvesPanel = new JXTaskPane();
		    jxtaskValvesPanel.setAnimated(true);
		    jxtaskValvesPanel.setTitle("Valves");
		    jxpanelValvesPanel = new ValvesPanel();
		    jxtaskValvesPanel.add(jxpanelValvesPanel);
		    
		    jxtaskColumns46Panel = new JXTaskPane();
		    jxtaskColumns46Panel.setAnimated(true);
		    jxtaskColumns46Panel.setTitle("Columns (4.6 \u00b5m particles)");
		    jxtaskColumns46Panel.setCollapsed(true);
		    jxpanelColumns46Panel = new ColumnsPanel(4.6);
		    jxtaskColumns46Panel.add(jxpanelColumns46Panel);

		    jxtaskColumns35Panel = new JXTaskPane();
		    jxtaskColumns35Panel.setAnimated(true);
		    jxtaskColumns35Panel.setTitle("Columns (3.5 \u00b5m particles)");
		    jxtaskColumns35Panel.setCollapsed(true);
		    jxpanelColumns35Panel = new ColumnsPanel(3.5);
		    jxtaskColumns35Panel.add(jxpanelColumns35Panel);
		    
		    jxtaskColumns18Panel = new JXTaskPane();
		    jxtaskColumns18Panel.setAnimated(true);
		    jxtaskColumns18Panel.setTitle("Columns (1.8 \u00b5m particles)");
		    jxtaskColumns18Panel.setCollapsed(true);
		    jxpanelColumns18Panel = new ColumnsPanel(1.8);
		    jxtaskColumns18Panel.add(jxpanelColumns18Panel);
		    
		    jxtaskPumpsPanel = new JXTaskPane();
		    jxtaskPumpsPanel.setAnimated(true);
		    jxtaskPumpsPanel.setTitle("Pumps");
		    jxtaskPumpsPanel.setCollapsed(false);
		    jxpanelPumpsPanel = new PumpsPanel();
		    jxtaskPumpsPanel.add(jxpanelPumpsPanel);
		    
		    jxtaskAutosamplersPanel = new JXTaskPane();
		    jxtaskAutosamplersPanel.setAnimated(true);
		    jxtaskAutosamplersPanel.setTitle("Autosamplers");
		    jxtaskAutosamplersPanel.setCollapsed(false);
		    jxpanelAutosamplersPanel = new AutosamplersPanel();
		    jxtaskAutosamplersPanel.add(jxpanelAutosamplersPanel);
		    
		    jxtaskDetectorsPanel = new JXTaskPane();
		    jxtaskDetectorsPanel.setAnimated(true);
		    jxtaskDetectorsPanel.setTitle("Detectors");
		    jxtaskDetectorsPanel.setCollapsed(false);
		    jxpanelDetectorsPanel = new DetectorsPanel();
		    jxtaskDetectorsPanel.add(jxpanelDetectorsPanel);

		    jxtaskWastePanel = new JXTaskPane();
		    jxtaskWastePanel.setAnimated(true);
		    jxtaskWastePanel.setTitle("Waste");
		    jxtaskWastePanel.setCollapsed(false);
		    jxpanelWastePanel = new WastePanel();
		    jxtaskWastePanel.add(jxpanelWastePanel);

		    // add the task pane to the taskpanecontainer
		    taskpanecontainer.add(jxtaskColumns46Panel, null);
		    taskpanecontainer.add(jxtaskColumns35Panel, null);
		    taskpanecontainer.add(jxtaskColumns18Panel, null);
		    taskpanecontainer.add(jxtaskValvesPanel, null);
		    taskpanecontainer.add(jxtaskPumpsPanel, null);
		    taskpanecontainer.add(jxtaskAutosamplersPanel, null);
		    taskpanecontainer.add(jxtaskDetectorsPanel, null);
		    taskpanecontainer.add(jxtaskWastePanel, null);
		    	
		    //JScrollPane jsclControlPanel = new JScrollPane(taskpanecontainer);
		    jScrollPaneControls.setViewportView(taskpanecontainer);
		}
		return jScrollPaneControls;
	}


	/**
	 * This method initializes jScrollPaneTable	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPaneTable() {
		if (jScrollPaneTable == null) {
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setBounds(new Rectangle(256, 456, 685, 137));
			jScrollPaneTable.setViewportView(getJXTable());
		}
		return jScrollPaneTable;
	}

	class NoEditTableModel extends DefaultTableModel 
	{
	    public NoEditTableModel(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public NoEditTableModel(final Object[][] data, final Object[] columnNames) 
	    {
	        setDataVector(data, columnNames);
	    }

	    public boolean isCellEditable(int row, int column) 
	    {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        return false;
	    }
	    
	    /*
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    //public Class getColumnClass(int c) {
	    //    return getValueAt(0, c).getClass();
	    //}

	}
	
	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JXTable getJXTable() {
		if (jxTable == null) {
			Object[] columnNames = {"Source", "Max backpressure", "Dispersion (\u03c3)", "Gradient delay"};
			tmCalculations = new NoEditTableModel(columnNames, 0);
			jxTable = new JXTable(tmCalculations);
			
			jxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jxTable.setBounds(new Rectangle(0, 0, 20, 20));
			
			jxTable.getColumnModel().getColumn(0).setPreferredWidth(100);
			
			jxTable.setAutoCreateColumnsFromModel(false);
		}
		return jxTable;
	}


	/**
	 * This method initializes jMainToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getJMainToolBar() {
		if (jMainToolBar == null) {
			jMainToolBar = new JToolBar();
			jMainToolBar.setFloatable(false);
			jMainToolBar.setLocation(new Point(256, 0));
			jMainToolBar.setSize(new Dimension(685, 38));
			jMainToolBar.add(getJbtnSelectTool());
			jMainToolBar.add(getJbtnEditTool());
			jMainToolBar.add(getJbtnRemove());
			jMainToolBar.add(new JToolBar.Separator());
			jMainToolBar.add(getJbtn4mil());
			jMainToolBar.add(getJbtn5mil());
			jMainToolBar.add(getJbtn7mil());
			jMainToolBar.add(getJbtn10mil());
			jMainToolBar.add(getJbtn20mil());
			jMainToolBar.add(getJbtn30mil());
			jMainToolBar.add(getJbtnOtherTube());
		}
		return jMainToolBar;
	}


	/**
	 * This method initializes jbtnRemove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getJbtnRemove() {
		if (jbtnRemoveTool == null) {
			jbtnRemoveTool = new JToggleButton();
			jbtnRemoveTool.setPreferredSize(new Dimension(24, 24));
			jbtnRemoveTool.setText("");
			jbtnRemoveTool.setIcon(new ImageIcon(getClass().getResource("/images/x.png")));
			jbtnRemoveTool.setHorizontalTextPosition(SwingConstants.TRAILING);
			jbtnRemoveTool.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnRemoveTool.setVerticalTextPosition(SwingConstants.CENTER);
			jbtnRemoveTool.setVerticalAlignment(SwingConstants.CENTER);
			jbtnRemoveTool.setActionCommand("Remove part");
			jbtnRemoveTool.setToolTipText("Remove a part");
			jbtnRemoveTool.setBounds(new Rectangle(2, 13, 35, 35));
		}
		return jbtnRemoveTool;
	}


	/**
	 * This method initializes jbtnEditTool	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getJbtnEditTool() {
		if (jbtnEditTool == null) {
			jbtnEditTool = new JToggleButton();
			jbtnEditTool.setIcon(new ImageIcon(getClass().getResource("/images/edit.png")));
			jbtnEditTool.setActionCommand("Edit part");
			jbtnEditTool.setToolTipText("Edit a part's properties");
			jbtnEditTool.setText("");
		}
		return jbtnEditTool;
	}


	/**
	 * This method initializes jbtnSelectTool	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getJbtnSelectTool() {
		if (jbtnSelectTool == null) {
			jbtnSelectTool = new JToggleButton();
			jbtnSelectTool.setIcon(new ImageIcon(getClass().getResource("/images/arrow.png")));
			jbtnSelectTool.setSelected(true);
			jbtnSelectTool.setToolTipText("Select");
		}
		return jbtnSelectTool;
	}


	/**
	 * This method initializes jbtn5mil	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJbtn5mil() {
		if (jbtn5mil == null) {
			jbtn5mil = new JToggleButton();
			jbtn5mil.setIcon(new ImageIcon(getClass().getResource("/images/redtube.png")));
			jbtn5mil.setToolTipText("Add 5 mil tubing");
		}
		return jbtn5mil;
	}


	/**
	 * This method initializes jbtn4mil	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJbtn4mil() {
		if (jbtn4mil == null) {
			jbtn4mil = new JToggleButton();
			jbtn4mil.setIcon(new ImageIcon(getClass().getResource("/images/blacktube.png")));
			jbtn4mil.setToolTipText("Add 4 mil tubing");
		}
		return jbtn4mil;
	}


	/**
	 * This method initializes jbtn7mil	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJbtn7mil() {
		if (jbtn7mil == null) {
			jbtn7mil = new JToggleButton();
			jbtn7mil.setIcon(new ImageIcon(getClass().getResource("/images/yellowtube.png")));
			jbtn7mil.setToolTipText("Add 7 mil tubing");
		}
		return jbtn7mil;
	}


	/**
	 * This method initializes jbtn10mil	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJbtn10mil() {
		if (jbtn10mil == null) {
			jbtn10mil = new JToggleButton();
			jbtn10mil.setIcon(new ImageIcon(getClass().getResource("/images/bluetube.png")));
			jbtn10mil.setToolTipText("Add 10 mil tubing");
		}
		return jbtn10mil;
	}


	/**
	 * This method initializes jbtn20mil	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJbtn20mil() {
		if (jbtn20mil == null) {
			jbtn20mil = new JToggleButton();
			jbtn20mil.setIcon(new ImageIcon(getClass().getResource("/images/orangetube.png")));
			jbtn20mil.setToolTipText("Add 20 mil tubing");
		}
		return jbtn20mil;
	}


	/**
	 * This method initializes jbtn30mil	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJbtn30mil() {
		if (jbtn30mil == null) {
			jbtn30mil = new JToggleButton();
			jbtn30mil.setIcon(new ImageIcon(getClass().getResource("/images/greentube.png")));
			jbtn30mil.setToolTipText("Add 30 mil tubing");
		}
		return jbtn30mil;
	}


	/**
	 * This method initializes jbtnOtherTube	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJbtnOtherTube() {
		if (jbtnOtherTube == null) {
			jbtnOtherTube = new JToggleButton();
			jbtnOtherTube.setIcon(new ImageIcon(getClass().getResource("/images/other.png")));
			jbtnOtherTube.setToolTipText("Add custom inner diameter tubing");
		}
		return jbtnOtherTube;
	}


	/**
	 * This method initializes jpanelStatus	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelStatus() {
		if (jpanelStatus == null) {
			jlblStatus = new JLabel();
			jlblStatus.setText("No problems detected");
			jlblStatus.setSize(new Dimension(679, 19));
			jlblStatus.setLocation(new Point(4, 0));
			jlblStatus.setName("jlblStatus");
			jpanelStatus = new JPanel();
			jpanelStatus.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jpanelStatus.setBackground(Color.white);
			jpanelStatus.setLayout(null);
			jpanelStatus.setBounds(new Rectangle(256, 592, 685, 23));
			jpanelStatus.add(jlblStatus, null);
		}
		return jpanelStatus;
	}
	
}  //  @jve:decl-index=0:visual-constraint="-259,126"
