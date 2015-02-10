package boswell.peakfinderlc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import boswell.graphcontrol.GraphControl;

import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.GridLayout;

public class TopPanel extends JPanel implements Scrollable, ComponentListener
{
	private static final long serialVersionUID = 1L;
	public GraphControl m_FullEICGraphControl = null;
	public GraphControl m_PeakEICGraphControl = null;
	private JScrollPane jScrollPaneStandards = null;
	public JXTable jTableStandards = null;
	public StandardCompoundsTableModel m_tabModelStandards;
	public PeakRankTableModel m_tabModelPeakRank;
	public Vector<String> vectColumnNames = new Vector<String>();
	public Vector<Vector<String>> vectChemicalRows = new Vector<Vector<String>>();

	public JPanel jPanelEICTotal = null;
	public JPanel jPanelStandards = null;
	public JScrollPane jScrollPane1 = null;
	public JXTable jTablePeaks = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	public JLabel jlblExpectedRetentionTime = null;
	public JLabel jlblExpectedPeakWidth = null;
	public JPanel jPanelPeakPick = null;
	public JButton jbtnNext = null;
	public JButton jbtnPrevious = null;
	public JButton jbtnCancel = null;
	public JCheckBox jchkNotHere = null;
	private JPanel jpanelExtractedIonChromatogram = null;
	private JPanel jpanelPeakWindow = null;
	/**
	 * This is the default constructor
	 */
	public TopPanel() {
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
		this.setPreferredSize(new Dimension(1056, 500));
        this.setMinimumSize(new Dimension(1056, 500));
		this.setSize(new Dimension(1056, 650));

		m_FullEICGraphControl = new GraphControl();
		m_FullEICGraphControl.setControlsEnabled(true);
		
		m_PeakEICGraphControl = new GraphControl();
		m_PeakEICGraphControl.setControlsEnabled(true);
		
		this.add(getJPanelEICTotal(), null);
		this.add(getJPanelStandards(), null);
		this.add(getJPanelPeakPick(), null);
		
	    this.addComponentListener(this);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
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
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
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
	public void componentResized(ComponentEvent arg0) {
		// Respond to window resize
		if (arg0.getComponent() == this)
		{
			Dimension size = this.getSize();
			int dividex = (int)(0.3 * (float)size.getWidth());
			this.jPanelStandards.setSize(dividex - 2, size.height - 4 - 4);
			this.jScrollPaneStandards.setSize(jPanelStandards.getWidth() - 24, jPanelStandards.getHeight()- jScrollPaneStandards.getY() - 12);
			// Scale standards table
			int xpos = 0;
			int diff;
			diff = ((jScrollPaneStandards.getViewport().getWidth() - 10) * 160) / 300;
			xpos += diff;
			jTableStandards.getColumnModel().getColumn(0).setPreferredWidth(diff);
			
			diff = ((jScrollPaneStandards.getViewport().getWidth() - 10) * 40) / 300;
			xpos += diff;
			jTableStandards.getColumnModel().getColumn(1).setPreferredWidth(diff);
			
			diff = ((jScrollPaneStandards.getViewport().getWidth() - 10) * 50) / 300;
			xpos += diff;
			jTableStandards.getColumnModel().getColumn(2).setPreferredWidth(diff);
			
			jTableStandards.getColumnModel().getColumn(3).setPreferredWidth(jScrollPaneStandards.getViewport().getWidth() - xpos);
			jTableStandards.revalidate();
			
			// Scale EIC panel
			int dividey = (size.height * 1) / 3;
			this.jPanelEICTotal.setLocation(dividex + 2, 4);
			this.jPanelEICTotal.setSize(size.width - (dividex + 2) - 4, dividey - 2 - 4);
			
			// Scale EIC graph control
			this.jpanelExtractedIonChromatogram.setSize(jPanelEICTotal.getWidth() - 24, jPanelEICTotal.getHeight() - jpanelExtractedIonChromatogram.getY() - 14);
			//this.m_FullEICGraphControl.setSize(jPanelEICTotal.getWidth() - 8, jPanelEICTotal.getHeight() - 20);
			
			// Scale peak pick panel
			this.jPanelPeakPick.setLocation(dividex + 2, dividey + 2);
			this.jPanelPeakPick.setSize(size.width - (dividex + 2) - 4, size.height - dividey - 2 - 4);
			
			// Scale peak pick graph control
			this.jpanelPeakWindow.setSize((jPanelPeakPick.getWidth() * 325) / 733, jPanelPeakPick.getHeight() - 86);
			//this.m_PeakEICGraphControl.setSize((jPanelPeakPick.getWidth() * 321) / 733, jPanelPeakPick.getHeight() - 76);
			
			// Scale table
			this.jScrollPane1.setLocation(jpanelPeakWindow.getX() + jpanelPeakWindow.getWidth() + 4, jpanelPeakWindow.getY());
			this.jScrollPane1.setSize(jPanelPeakPick.getWidth() - jScrollPane1.getX() - 12, jpanelPeakWindow.getHeight() - 84);
			
			xpos = 0;
			diff = ((jScrollPane1.getViewport().getWidth() - 10) * 90) / 380;
			xpos += diff;
			jTablePeaks.getColumnModel().getColumn(0).setPreferredWidth(diff);
			
			diff = ((jScrollPane1.getViewport().getWidth() - 10) * 90) / 380;
			xpos += diff;
			jTablePeaks.getColumnModel().getColumn(1).setPreferredWidth(diff);
			
			diff = ((jScrollPane1.getViewport().getWidth() - 10) * 90) / 380;
			xpos += diff;
			jTablePeaks.getColumnModel().getColumn(2).setPreferredWidth(diff);
			
			jTablePeaks.getColumnModel().getColumn(3).setPreferredWidth(jScrollPane1.getViewport().getWidth() - xpos);
			jTablePeaks.revalidate();
			
			this.jbtnCancel.setLocation(jbtnCancel.getX(), jPanelPeakPick.getHeight() - 54);
			this.jbtnNext.setLocation(jPanelPeakPick.getWidth() - 196, jbtnCancel.getY());
			this.jbtnPrevious.setLocation(jScrollPane1.getX(), jbtnCancel.getY());
			
			this.jLabel.setLocation(jScrollPane1.getX() + 4, jScrollPane1.getY() + jScrollPane1.getHeight() + 4);
			this.jLabel.setSize((jScrollPane1.getWidth() / 2) - 2, jLabel.getHeight());
			this.jLabel1.setLocation(jScrollPane1.getX() + 4, jLabel.getY() + jLabel.getHeight() + 4);
			this.jLabel1.setSize(jLabel.getWidth(), jLabel.getHeight());
			this.jlblExpectedRetentionTime.setLocation(jLabel.getX() + jLabel.getWidth() + 4, jLabel.getY());
			this.jlblExpectedRetentionTime.setSize((jScrollPane1.getWidth() / 2) - 2, jlblExpectedRetentionTime.getHeight());
			this.jlblExpectedPeakWidth.setLocation(jLabel1.getX() + jLabel1.getWidth() + 4, jLabel1.getY());
			this.jlblExpectedPeakWidth.setSize(jlblExpectedRetentionTime.getWidth(), jLabel1.getHeight());
			this.jchkNotHere.setLocation(jLabel1.getX(), jLabel1.getY() + jLabel1.getHeight() + 4);
			this.jchkNotHere.setSize(jScrollPane1.getWidth(), jchkNotHere.getHeight());
			
			this.m_FullEICGraphControl.repaint();
			this.m_PeakEICGraphControl.repaint();

		}
	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method initializes jScrollPaneStandards	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPaneStandards() {
		if (jScrollPaneStandards == null) {
			jScrollPaneStandards = new JScrollPane();
			jScrollPaneStandards.setBounds(new Rectangle(12, 28, 285, 601));
			jScrollPaneStandards.setViewportView(getJTableStandards());
		}
		return jScrollPaneStandards;
	}

	class StandardCompoundsTableModel extends DefaultTableModel 
	{
	    public StandardCompoundsTableModel(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public StandardCompoundsTableModel(final Object[][] data, final Object[] columnNames) 
	    {
	        setDataVector(data, columnNames);
	    }

	    public boolean isCellEditable(int row, int column) 
	    {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        //if (column == 3 || column == 0)
	        //    return true;
	        //else
	            return false;
	        
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
	
	class PeakRankTableModel extends DefaultTableModel 
	{
	    public PeakRankTableModel(final Object[] columnNames, final int rowCount) 
	    {
	        super(convertToVector(columnNames), rowCount);
	    }
	    
	    public PeakRankTableModel(final Object[][] data, final Object[] columnNames) 
	    {
	        setDataVector(data, columnNames);
	    }

	    public boolean isCellEditable(int row, int column) 
	    {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        //if (column == 3 || column == 0)
	        //    return true;
	        //else
	            return false;
	        
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
	
	/**
	 * This method initializes jTableStandards	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JXTable getJTableStandards() {
		if (jTableStandards == null) 
		{
			Object[] columnNames = {
					"Standard",
					"m/z",
					"Peak",
					"tR (min)"
					};
			
			/*Object[][] data = new Object[Globals.StandardCompoundsNameArray.length][4];
			for (int i = 0; i < Globals.StandardCompoundsNameArray.length; i++)
			{
				data[i][0] = Globals.StandardCompoundsNameArray[i];
				String str = "";
				for (int j = 0; j < Globals.StandardCompoundsMZArray[i].length; j++)
				{
					str += Globals.StandardCompoundsMZArray[i][j];
					if (j < Globals.StandardCompoundsMZArray[i].length - 1)
						str += ", ";
				}
				data[i][1] = str;
				data[i][2] = (Object)0;
				data[i][3] = (Object)0.0;
			}*/
			Object[][] data = {{"Test", "0", (Object)0, (Object)0.0}};

			m_tabModelStandards = new StandardCompoundsTableModel(data, columnNames);

			jTableStandards = new JXTable(m_tabModelStandards);

			jTableStandards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTableStandards.setSortable(false);
			jTableStandards.setFocusable(false);
			jTableStandards.setColumnSelectionAllowed(false);
			jTableStandards.setShowGrid(true);
			jTableStandards.setFillsViewportHeight(false);
			jTableStandards.getTableHeader().setPreferredSize(new Dimension(jTableStandards.getColumnModel().getTotalColumnWidth(), 22));
			
			jTableStandards.getColumnModel().getColumn(0).setPreferredWidth(160);
			jTableStandards.getColumnModel().getColumn(1).setPreferredWidth(40);
			jTableStandards.getColumnModel().getColumn(2).setPreferredWidth(50);
		}
		
		return jTableStandards;
	}


	/**
	 * This method initializes jPanelEICTotal	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelEICTotal() {
		if (jPanelEICTotal == null) {
			jlblExpectedPeakWidth = new JLabel();
			jlblExpectedPeakWidth.setText("0 min");
			jlblExpectedPeakWidth.setBounds(new Rectangle(540, 312, 181, 16));
			jlblExpectedPeakWidth.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblExpectedRetentionTime = new JLabel();
			jlblExpectedRetentionTime.setFont(new Font("Dialog", Font.PLAIN, 12));
			jlblExpectedRetentionTime.setBounds(new Rectangle(540, 288, 181, 16));
			jlblExpectedRetentionTime.setText("0 min");
			jLabel1 = new JLabel();
			jLabel1.setText("Expected peak width (FWHM):");
			jLabel1.setBounds(new Rectangle(344, 312, 193, 16));
			jLabel1.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel = new JLabel();
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel.setBounds(new Rectangle(344, 288, 193, 16));
			jLabel.setText("Expected retention time (tR):");
			jPanelEICTotal = new JPanel();
			jPanelEICTotal.setLayout(null);
			jPanelEICTotal.setBounds(new Rectangle(320, 4, 733, 225));
			jPanelEICTotal.setBorder(BorderFactory.createTitledBorder(null, "Extracted ion chromatogram of 113 m/z", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelEICTotal.add(getJpanelExtractedIonChromatogram(), null);
		}
		return jPanelEICTotal;
	}


	/**
	 * This method initializes jPanelStandards	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelStandards() {
		if (jPanelStandards == null) {
			jPanelStandards = new JPanel();
			jPanelStandards.setLayout(null);
			jPanelStandards.setBounds(new Rectangle(4, 4, 309, 641));
			jPanelStandards.setBorder(BorderFactory.createTitledBorder(null, "Standard Compounds", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelStandards.add(getJScrollPaneStandards(), null);
		}
		return jPanelStandards;
	}


	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setBounds(new Rectangle(344, 28, 377, 253));
			jScrollPane1.setViewportView(getJTablePeaks());
		}
		return jScrollPane1;
	}


	/**
	 * This method initializes jTablePeaks	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JXTable getJTablePeaks() {
		if (jTablePeaks == null) {
			Object[] columnNames = {
					"Rank (P)",
					"tR (min)",
					"FWHM (min)",
					"Intensity (counts)"
					};
			
			Object[][] data = new Object[1][4];
				data[0][0] = (Object)" ";
				data[0][1] = (Object)" ";
				data[0][2] = (Object)" ";
				data[0][3] = (Object)" ";

			m_tabModelPeakRank = new PeakRankTableModel(data, columnNames);

			jTablePeaks = new JXTable(m_tabModelPeakRank);

			jTablePeaks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			jTablePeaks.setSortable(false);
			jTablePeaks.setFocusable(false);
			jTablePeaks.setColumnSelectionAllowed(false);
			jTablePeaks.setShowGrid(true);
			jTablePeaks.setFillsViewportHeight(false);
			jTablePeaks.getTableHeader().setPreferredSize(new Dimension(jTablePeaks.getColumnModel().getTotalColumnWidth(), 22));
			
			jTablePeaks.getColumnModel().getColumn(0).setPreferredWidth(70);
			jTablePeaks.getColumnModel().getColumn(1).setPreferredWidth(70);
			jTablePeaks.getColumnModel().getColumn(2).setPreferredWidth(70);
		}
		return jTablePeaks;
	}


	/**
	 * This method initializes jPanelPeakPick	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelPeakPick() {
		if (jPanelPeakPick == null) {
			jPanelPeakPick = new JPanel();
			jPanelPeakPick.setLayout(null);
			jPanelPeakPick.setBounds(new Rectangle(320, 232, 733, 413));
			jPanelPeakPick.setBorder(BorderFactory.createTitledBorder(null, "Choose the correct peak for uracil", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelPeakPick.add(jLabel1, null);
			jPanelPeakPick.add(jLabel, null);
			jPanelPeakPick.add(jlblExpectedRetentionTime, null);
			jPanelPeakPick.add(jlblExpectedPeakWidth, null);
			jPanelPeakPick.add(getJScrollPane1(), null);
			jPanelPeakPick.add(getJbtnNext(), null);
			jPanelPeakPick.add(getJbtnPrevious(), null);
			jPanelPeakPick.add(getJbtnCancel(), null);
			jPanelPeakPick.add(getJchkNotHere(), null);
			jPanelPeakPick.add(getJpanelPeakWindow(), null);
		}
		return jPanelPeakPick;
	}


	/**
	 * This method initializes jbtnNext	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnNext() {
		if (jbtnNext == null) {
			jbtnNext = new JButton();
			jbtnNext.setBounds(new Rectangle(540, 360, 185, 41));
			jbtnNext.setIcon(new ImageIcon(getClass().getResource("/boswell/peakfinderlc/images/forward.png")));
			jbtnNext.setHorizontalTextPosition(SwingConstants.LEADING);
			jbtnNext.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnNext.setActionCommand("Next Standard");
			jbtnNext.setText("Next Standard  ");
		}
		return jbtnNext;
	}


	/**
	 * This method initializes jbtnPrevious	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnPrevious() {
		if (jbtnPrevious == null) {
			jbtnPrevious = new JButton();
			jbtnPrevious.setBounds(new Rectangle(344, 360, 185, 41));
			jbtnPrevious.setIcon(new ImageIcon(getClass().getResource("/boswell/peakfinderlc/images/back.png")));
			jbtnPrevious.setHorizontalTextPosition(SwingConstants.TRAILING);
			jbtnPrevious.setHorizontalAlignment(SwingConstants.CENTER);
			jbtnPrevious.setActionCommand("Previous Standard");
			jbtnPrevious.setText("  Previous Standard");
		}
		return jbtnPrevious;
	}


	/**
	 * This method initializes jbtnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJbtnCancel() {
		if (jbtnCancel == null) {
			jbtnCancel = new JButton();
			jbtnCancel.setBounds(new Rectangle(12, 360, 185, 41));
			jbtnCancel.setText("Cancel");
		}
		return jbtnCancel;
	}


	/**
	 * This method initializes jchkNotHere	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJchkNotHere() {
		if (jchkNotHere == null) {
			jchkNotHere = new JCheckBox();
			jchkNotHere.setBounds(new Rectangle(344, 336, 377, 21));
			jchkNotHere.setText("Skip this one. I'll enter the retention time myself later.");
		}
		return jchkNotHere;
	}


	/**
	 * This method initializes jpanelExtractedIonChromatogram	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelExtractedIonChromatogram() {
		if (jpanelExtractedIonChromatogram == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			jpanelExtractedIonChromatogram = new JPanel();
			jpanelExtractedIonChromatogram.setLayout(gridLayout);
			jpanelExtractedIonChromatogram.setBounds(new Rectangle(12, 28, 709, 185));
			jpanelExtractedIonChromatogram.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jpanelExtractedIonChromatogram.add(m_FullEICGraphControl, null);
		}
		return jpanelExtractedIonChromatogram;
	}


	/**
	 * This method initializes jpanelPeakWindow	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJpanelPeakWindow() {
		if (jpanelPeakWindow == null) {
			GridLayout gridLayout1 = new GridLayout();
			gridLayout1.setRows(1);
			jpanelPeakWindow = new JPanel();
			jpanelPeakWindow.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jpanelPeakWindow.setLayout(gridLayout1);
			jpanelPeakWindow.setBounds(new Rectangle(12, 28, 325, 325));
			jpanelPeakWindow.add(m_PeakEICGraphControl, null);
		}
		return jpanelPeakWindow;
	}
}  //  @jve:decl-index=0:visual-constraint="-259,126"
