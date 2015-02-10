package panels;

import org.jdesktop.swingx.JXPanel;

import visualizer.HPLCPartColumn;
import visualizer.HPLCPartValve;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

public class ColumnsPanel extends JXPanel implements ComponentListener
{
	protected double dParticleSize = 0;
	
	public double[][] columnTypes = {
			{4.6, 150},
			{4.6, 100},
			{4.6, 50},
			{3.0, 150},
			{3.0, 100},
			{3.0, 50},
			{2.1, 150},
			{2.1, 100},
			{2.1, 50}
	};
	
    public int iButtonHeight = 30;

    public JToggleButton[] JToggleButtons = new JToggleButton[columnTypes.length];
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This method initializes 
	 * 
	 */
	public ColumnsPanel(double dParticleSize) 
	{
		super();
		initialize();
		this.addComponentListener(this);
		this.dParticleSize = dParticleSize;
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(null);
        this.setPreferredSize(new Dimension(254, iButtonHeight * columnTypes.length));
        this.setBounds(new Rectangle(0, 0, 254, iButtonHeight * columnTypes.length));
        this.setBackground(Color.white);
        
        for (int i = 0; i < columnTypes.length; i++)
        {
        	JToggleButtons[i] = new JToggleButton();
        	JToggleButtons[i].setBounds(new Rectangle(0, i * iButtonHeight, 253, iButtonHeight));
        	JToggleButtons[i].setIcon(new ImageIcon(getClass().getResource("/images/column.png")));
        	JToggleButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
        	JToggleButtons[i].setActionCommand("Button" + Integer.toString(i));
        	JToggleButtons[i].setText(Float.toString((float)columnTypes[i][0]) + " x " + Float.toString((float)columnTypes[i][1]) + " mm");
        	JToggleButtons[i].setBackground(new Color(255,255,255));
        	this.add(JToggleButtons[i], null);
        }

	}

	public HPLCPartColumn getHPLCPartFromButton(int i)
	{
		HPLCPartColumn newPart = new HPLCPartColumn((Double)this.columnTypes[i][0], (Double)this.columnTypes[i][1], dParticleSize, "");
		return newPart;
	}
	
	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) 
	{
		// Respond to window resize
		if (arg0.getComponent() == this)
		{
			Dimension size = this.getSize();
			for (int i = 0; i < this.columnTypes.length; i++)
			{
				this.JToggleButtons[i].setSize(size.width, JToggleButtons[i].getHeight());
			}
		}
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}



}  //  @jve:decl-index=0:visual-constraint="10,10"
