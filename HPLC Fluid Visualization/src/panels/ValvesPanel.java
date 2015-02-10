package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXPanel;

import visualizer.HPLCPart;
import visualizer.HPLCPartValve;

public class ValvesPanel extends JXPanel implements ComponentListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Object[][] valveTypes = {
			{4, 2, "4 port, 2 pos valve"},
			{6, 2, "6 port, 2 pos valve"},
			{8, 2, "8 port, 2 pos valve"},
			{10, 2, "10 port, 2 pos valve"},
			{12, 2, "12 port, 2 pos valve"},
			{5, 4, "5 port, 4 pos valve"},
			{7, 6, "7 port, 6 pos valve"},
			{9, 8, "9 port, 8 pos valve"},
			{11, 10, "11 port, 10 pos valve"},
			{13, 12, "13 port, 12 pos valve"},
	};
	
	public JToggleButton[] JToggleButtons = new JToggleButton[valveTypes.length];
	
    public int iButtonHeight = 45;

	/**
	 * This method initializes 
	 * 
	 */
	public ValvesPanel() {
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
        this.setPreferredSize(new Dimension(254, iButtonHeight * valveTypes.length));
        this.setBounds(new Rectangle(0, 0, 254, iButtonHeight * valveTypes.length));
        this.setBackground(Color.white);

        for (int i = 0; i < valveTypes.length; i++)
        {
        	JToggleButtons[i] = new JToggleButton();
        	JToggleButtons[i].setBounds(new Rectangle(0, i * iButtonHeight, 253, iButtonHeight));
        	JToggleButtons[i].setIcon(new ImageIcon(getClass().getResource("/images/" + valveTypes[i][0].toString() + "port " + valveTypes[i][1].toString() + "pos valve.png")));
        	JToggleButtons[i].setHorizontalAlignment(SwingConstants.LEFT);
        	JToggleButtons[i].setActionCommand("Button" + Integer.toString(i));
        	JToggleButtons[i].setText(valveTypes[i][0].toString() + "-port, " + valveTypes[i][1].toString() + "-pos valve");
        	JToggleButtons[i].setBackground(new Color(255,255,255));
        	this.add(JToggleButtons[i], null);
        }
			
	}

	public HPLCPartValve getHPLCPartFromButton(int i)
	{
		HPLCPartValve newValve = new HPLCPartValve((Integer)this.valveTypes[i][0], (Integer)this.valveTypes[i][1], (String)this.valveTypes[i][2]);
		return newValve;	}
	
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
			for (int i = 0; i < this.valveTypes.length; i++)
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
