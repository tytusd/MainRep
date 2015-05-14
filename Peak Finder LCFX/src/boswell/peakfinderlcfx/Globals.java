package boswell.peakfinderlcfx;


public class Globals
{
	// For a 2.1 x 100 mm column
	/*public static double[][] dDeadVolumeArray = {
		{0.95, 0.205733},
		{0.90, 0.184933},
		{0.80, 0.1848},
		{0.70, 0.1832},
		{0.60, 0.1833333},
		{0.50, 0.1838667},
		{0.40, 0.1882667},
		{0.30, 0.1932},
		{0.20, 0.2037333},
		{0.10, 0.2249333},
		{0.05, 0.2598667}
	};*/
	public static double[][] dDeadTimeArray = {
	{0.95, 0.514333333},
	{0.90, 0.462333333},
	{0.80, 0.462},
	{0.70, 0.458},
	{0.60, 0.458333333},
	{0.50, 0.459666667},
	{0.40, 0.470666667},
	{0.30, 0.483},
	{0.20, 0.509333333},
	{0.10, 0.562333333},
	{0.05, 0.649666667}
	};
	
	public static String[] StationaryPhaseArray = {
		"Agilent Eclipse Plus C18 (3.5 \u00b5m particle size)"
	};
	
	public static String[] StandardCompoundsNameArray = {
		//"Uracil",
		"N-methylacetamide",
		"N-ethylacetamide",
		"N,N-dimethylpropionamide",
		"benzamide",
		"N-methylbenzamide",
		"N-ethylbenzamide",
		"N-propylbenzamide",
		"N-butylbenzamide",
		"N-pentylbenzamide",
		"N-hexylbenzamide",
		"N-heptylbenzamide",
		"N-octylbenzamide",
		"N-nonylbenzamide",
		"N-decylbenzamide",
		"N-undecylbenzamide",
		"N-dodecylbenzamide",
		"N-tridecylbenzamide",
		"N-tetradecylbenzamide",
		"N-pentadecylbenzamide",
		"N-hexadecylbenzamide"
	};
	
	public static double[][] StandardCompoundsMZArray = {
		//113,
		{74},
		{88},
		{102},
		{122},
		{136},
		{150},
		{164},
		{178},
		{192},
		{206},
		{220},
		{234},
		{248},
		{262},
		{276},
		{290},
		{304},
		{318},
		{332},
		{346}
	};
	
	public static double[][][] StandardCompoundsIsocraticDataArray = {
		//{{ .95, -10}, {.05, -10}},
		{{ .5, -1.496111590}, { .4, -1.357443007}, { .3, -1.257978398}, { .2, -1.154739577}, { .1, -1.06262035}, { .05, -1.091154752}},
		{{ .95, -1.9330934210}, { .9, -1.664955206}, { .8, -1.417487361}, { .7, -1.198467480}, { .6, -1.112996833}, { .5, -0.911677562}, { .4, -0.720082417}, { .3, -0.590525446}, { .2, -0.582063363}, { .1, -0.444642458}, { .05, -0.260022368}},
		{{ .95, -1.3688219905}, { .9, -0.963099514}, { .8, -0.858462002}, { .7, -0.772498748}, { .6, -0.711791437}, { .5, -0.645409672}, { .4, -0.552139511}, { .3, -0.425469486}, { .2, -0.21145776}, { .1, 0.1803328}, { .05, 0.481481604}},
		{{ .9, -1.5188271707}, { .8, -1.088684787}, { .7, -0.822016387}, { .6, -0.691144667}, { .5, -0.574898202}, { .4, -0.426379025}, { .3, -0.235756294}, { .2, 0.058666455}, { .1, 0.506483378}, { .05, 0.767930317}},
		{{ .95, -1.9330934210}, { .9, -1.085171610}, { .8, -0.733523265}, { .7, -0.642442395}, { .6, -0.546125941}, { .5, -0.424396908}, { .4, -0.261655203}, { .3, -0.062424660}, { .2, 0.233846288}, { .1, 0.690757873}, { .05, 0.981913630}},
		{{ .95, -1.2015941918}, { .9, -0.889223430}, { .8, -0.601433755}, { .7, -0.511646365}, { .6, -0.384719639}, { .5, -0.219440940}, { .4, -0.025004547}, { .3, 0.205541325}, { .2, 0.535373662}, { .1, 1.023743872}, { .05, 1.341763822}},
		{{ .95, -0.7335210661}, { .9, -0.571533521}, { .8, -0.494380260}, { .7, -0.371573886}, { .6, -0.209906846}, { .5, -0.018661449}, { .4, 0.217893849}, { .3, 0.499037836}, { .2, 0.885359740}, { .1, 1.434934082}, { .05, 1.789622672}},
		{{ .95, -0.6272645424}, { .9, -0.481210983}, { .8, -0.375350383}, { .7, -0.225233429}, { .6, -0.031771844}, { .5, 0.199688368}, { .4, 0.489053728}, { .3, 0.833512696}, { .2, 1.301768350}, { .1, 1.937621330}},
		{{ .95, -0.5351534123}, { .9, -0.386201605}, { .8, -0.258101795}, { .7, -0.072033752}, { .6, 0.145676586}, { .5, 0.417461456}, { .4, 0.761908681}, { .3, 1.177905765}, { .2, 1.738134869}},
		{{ .95, -0.4249379325}, { .9, -0.288378249}, { .8, -0.125984474}, { .7, 0.084469604}, { .6, 0.330635107}, { .5, 0.638007539}, { .4, 1.038756340}, { .3, 1.527013125}},
		{{ .95, -0.3262345468}, { .9, -0.183512578}, { .8, 0.009300023}, { .7, 0.244750533}, { .6, 0.518466081}, { .5, 0.863594568}, { .4, 1.318911293}, { .3, 1.880458515}},
		{{ .95, -0.2241062959}, { .9, -0.070931171}, { .8, 0.150492841}, { .7, 0.410525524}, { .6, 0.710809468}, { .5, 1.090833559}, { .4, 1.599205572}},
		{{ .95, -0.1135494854}, { .9, 0.045162159}, { .8, 0.295352863}, { .7, 0.579434104}, { .6, 0.905648720}, { .5, 1.320732061}, { .4, 1.880206114}},
		{{ .95, -0.0114069455}, { .9, 0.164134047}, { .8, 0.441322136}, { .7, 0.750978612}, { .6, 1.103019605}, { .5, 1.551508381}},
		{{ .95, 0.1030029244}, { .9, 0.288482308}, { .8, 0.591032468}, { .7, 0.924933531}, { .6, 1.302795167}, { .5, 1.783923262}},
		{{ .95, 0.2135513245}, { .9, 0.410834989}, { .8, 0.742352132}, { .7, 1.100836890}, { .6, 1.503330030}, { .5, 2.017069676}},
		{{ .95, 0.3256502543}, { .9, 0.537169684}, { .8, 0.896340080}, { .7, 1.277887676}, { .6, 1.704780512}},
		{{ .95, 0.4379744413}, { .9, 0.664307041}, { .8, 1.050887840}, { .7, 1.456118403}, { .6, 1.907192399}},
		{{ .95, 0.5537304762}, { .9, 0.792825797}, { .8, 1.207397692}, { .7, 1.635741217}, { .6, 2.110442778}},
		{{ .95, 0.6667290251}, { .9, 0.923652598}, { .8, 1.364877075}, { .7, 1.815512014}}
	};

	public static double[][] convertGradientProgramInConventionalFormToRegularForm(
			double[][] gradientProgramInConventionalProgram,
			double initialTime, double initialSolventComposition) {
		double[][] newGradientProgram = new double[gradientProgramInConventionalProgram.length + 2][2];
    	int iPointCount = 0;
    	

    	newGradientProgram[iPointCount][0] = 0.0;
    	newGradientProgram[iPointCount][1] = (Double)gradientProgramInConventionalProgram[0][1];
    	double dLastTime = 0;
		iPointCount++;
		
    	// Go through the gradient program table and create an array that contains solvent composition vs. time
		for (int i = 0; i < gradientProgramInConventionalProgram.length; i++)
		{
    		if ((Double)gradientProgramInConventionalProgram[i][0] > dLastTime)
    		{
    			double dTime = (Double)gradientProgramInConventionalProgram[i][0];
    			double dFractionB = (Double)gradientProgramInConventionalProgram[i][1];
    			
				newGradientProgram[iPointCount][0] = dTime;
				newGradientProgram[iPointCount][1] = dFractionB;
    	    	iPointCount++;
    		
    	    	dLastTime = dTime;
    		}
		}
		
		// Add another point past the end of the gradient to make it flatten out and go forever.
		newGradientProgram[iPointCount][0] = newGradientProgram[iPointCount - 1][0] * 2;
		newGradientProgram[iPointCount][1] = (Double)gradientProgramInConventionalProgram[gradientProgramInConventionalProgram.length-1][1];
    	iPointCount++;

		// Ideal series finished
		// Now cut it down to the correct size
		double tempArray[][] = new double[iPointCount][2];
		for (int i = 0; i < iPointCount; i++)
		{
			tempArray[i][0] = newGradientProgram[i][0];
			tempArray[i][1] = newGradientProgram[i][1];
		}

		return tempArray;
		
	}

	
	public static double roundToSignificantFigures(double num, int n) 
	{
	    if (num == 0) 
	    {
	        return 0;
	    }

	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;

	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num * magnitude);
	    
	    return shifted / magnitude;
	}
}