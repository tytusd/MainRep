package panels;

import org.jdesktop.swingx.JXPanel;

import visualizer.HPLCPartAutosampler;
import visualizer.HPLCPartColumn;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

public class AutosamplersPanel extends JXPanel implements ComponentListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Object[][] autosamplerTypes = {
			{"Autosampler"}
	};
	
    public int iButtonHeight = 45;

    public JToggleButton[] JToggleButtons = new JToggleButton[autosamplerTypes.length];
    
    /**
	 * This method initializes 
	 * 
	 */
	public AutosamplersPanel() {
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
        this.setPreferredSize(new Dimension(254, iButtonHeight * autosamplerTypes.length));
        this.setBounds(new Rectangle(0, 0, 254, iButtonHeight * autosamplerTypes.length));
        this.setBackground(Color.white);
        
        for (int i = 0; i < autosamplerTypes.length; i++)
        {
        	JToggleButtons[i] = new JToggleButton();
        	JToggleButtons[i].setBounds(new Rectangle(0, i * iButtonHeight, 253, iButtonHeight));
        	JToggleButtons[i].setIcon(new ImageIcon(getClass().getResource("/images/pump.png")));
        	JToggleButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
        	JToggleButtons[i].setActionCommand("Button" + Integer.toString(i));
        	JToggleButtons[i].setText((String)autosamplerTypes[i][0]);
        	JToggleButtons[i].setBackground(new Color(255,255,255));
        	this.add(JToggleButtons[i], null);
        }
	}

	public HPLCPartAutosampler getHPLCPartFromButton(int i)
	{
		HPLCPartAutosampler newPart = new HPLCPartAutosampler((String)this.autosamplerTypes[i][0]);
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
			for (int i = 0; i < this.autosamplerTypes.length; i++)
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
