package visualizer;

import java.awt.Color;


//import javax.help.HelpBroker;
//import javax.help.HelpSet;

public class Globals
{
	//public static HelpSet hsMainHelpSet = null;
	//public static HelpBroker hbMainHelpBroker = null;
	
	public static final int gridSpacing = 20;
	public static final int tubeDiameter = 12;
	public static final int tubeEndRadius = (int)Math.round((Math.sqrt(2) * (double)gridSpacing) / 2) - (tubeDiameter / 2);

	public static final Color standardFluidColors[] = {
		new Color(150, 217, 237),
		new Color(255, 146, 154),
		new Color(128, 221, 168),
		new Color(216, 211, 152),
		new Color(234, 162, 208),
		new Color(178, 180, 211),
		new Color(216, 216, 216),
		new Color(168, 168, 168),
		new Color(239, 198, 187),
		new Color(215, 232, 184)
		};
	
	public static double roundToSignificantFigures(double num, int n) {
	    if(num == 0) {
	        return 0;
	    }

	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;

	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num*magnitude);
	    return shifted/magnitude;
	}
}
