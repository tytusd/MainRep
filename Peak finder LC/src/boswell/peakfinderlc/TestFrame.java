package boswell.peakfinderlc;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class TestFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	protected PeakFinderDialog myDialog;
	protected PeakFinderSettingsDialog myInitDialog;
	
	public TestFrame(String str)
	{
		super(str);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    addWindowListener(new WindowAdapter()
	    {
	       public void windowClosing(WindowEvent e)
	       {
	         System.exit(0);
	       }
	    });
	}

	public static void main(String[] args) 
	{
		TestFrame myFrame = new TestFrame("Back Frame"); // create frame with title
	    
		/*myFrame.myDialog = new PeakFinderDialog(myFrame, "Peak Finder", true); // define JDialog of interest
		
	    myFrame.setVisible(true);
	    myFrame.myDialog.pack();
	    myFrame.myDialog.setVisible(true);
	    */
		
		myFrame.myInitDialog = new PeakFinderSettingsDialog(myFrame, true, Globals.StationaryPhaseArray, true);

		myFrame.setVisible(true);
	    myFrame.myInitDialog.setVisible(true);
		
	    System.exit(0);
	}
}