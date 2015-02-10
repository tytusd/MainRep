package panels;

import org.jdesktop.swingx.JXPanel;

import visualizer.HPLCPartAutosampler;
import visualizer.HPLCPartColumn;
import visualizer.HPLCPartWaste;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

public class WastePanel extends JXPanel implements ComponentListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Object[][] wasteTypes = {
			{"Waste"}
	};
	
    public int iButtonHeight = 45;

    public JToggleButton[] JToggleButtons = new JToggleButton[wasteTypes.length];
    
    /**
	 * This method initializes 
	 * 
	 */
	public WastePanel() {
		super();
		initialize();
		this.addComponentListener(this);

	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setLayout(null);
        this.setPreferredSize(new Dimension(254, iButtonHeight * wasteTypes.length));
        this.setBounds(new Rectangle(0, 0, 254, iButtonHeight * wasteTypes.length));
        this.setBackground(Color.white);
        
        for (int i = 0; i < wasteTypes.length; i++)
        {
        	JToggleButtons[i] = new JToggleButton();
        	JToggleButtons[i].setBounds(new Rectangle(0, i * iButtonHeight, 253, iButtonHeight));
        	JToggleButtons[i].setIcon(new ImageIcon(getClass().getResource("/images/waste.png")));
        	JToggleButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
        	JToggleButtons[i].setActionCommand("Button" + Integer.toString(i));
        	JToggleButtons[i].setText((String)wasteTypes[i][0]);
        	JToggleButtons[i].setBackground(new Color(255,255,255));
        	this.add(JToggleButtons[i], null);
        }
	}

	public HPLCPartWaste getHPLCPartFromButton(int i)
	{
		HPLCPartWaste newPart = new HPLCPartWaste((String)this.wasteTypes[i][0]);
		return newPart;
	}
	
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) 
	{
		// Respond to window resize
		if (e.getComponent() == this)
		{
			Dimension size = this.getSize();
			for (int i = 0; i < this.wasteTypes.length; i++)
			{
				this.JToggleButtons[i].setSize(size.width, JToggleButtons[i].getHeight());
			}
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
