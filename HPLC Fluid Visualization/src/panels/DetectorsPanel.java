package panels;

import org.jdesktop.swingx.JXPanel;

import visualizer.HPLCPartAutosampler;
import visualizer.HPLCPartDetector;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class DetectorsPanel extends JXPanel implements ComponentListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Object[][] detectorTypes = {
			{"Detector"}
	};
	
    public int iButtonHeight = 45;

    public JToggleButton[] JToggleButtons = new JToggleButton[detectorTypes.length];
    
	/**
	 * This method initializes 
	 * 
	 */
	public DetectorsPanel() {
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
        this.setPreferredSize(new Dimension(254, iButtonHeight * detectorTypes.length));
        this.setBounds(new Rectangle(0, 0, 254, iButtonHeight * detectorTypes.length));
        this.setBackground(Color.white);
        
        for (int i = 0; i < detectorTypes.length; i++)
        {
        	JToggleButtons[i] = new JToggleButton();
        	JToggleButtons[i].setBounds(new Rectangle(0, i * iButtonHeight, 253, iButtonHeight));
        	JToggleButtons[i].setIcon(new ImageIcon(getClass().getResource("/images/detector.png")));
        	JToggleButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
        	JToggleButtons[i].setActionCommand("Button" + Integer.toString(i));
        	JToggleButtons[i].setText((String)detectorTypes[i][0]);
        	JToggleButtons[i].setBackground(new Color(255,255,255));
        	this.add(JToggleButtons[i], null);
        }
	}

	public HPLCPartDetector getHPLCPartFromButton(int i)
	{
		HPLCPartDetector newPart = new HPLCPartDetector((String)this.detectorTypes[i][0]);
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
			for (int i = 0; i < this.detectorTypes.length; i++)
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
