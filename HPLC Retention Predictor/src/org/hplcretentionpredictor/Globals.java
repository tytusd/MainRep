package org.hplcretentionpredictor;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Globals
{
	public static double dRefColumnID = .21;
	public static double dRefColumnLength = 5;
	public static double dRefFlowRate = 0.8;

	// For a 2.1 x 50 mm column at 800 uL/min
	public static double[][] dDeadTimeArray = {
		 { 0.05, 0.146},
		 { 0.1, 0.146},
		 { 0.2, 0.146},
		 { 0.3, 0.146},
		 { 0.4, 0.146},
		 { 0.5, 0.146},
		 { 0.6, 0.146},
		 { 0.7, 0.146},
		 { 0.8, 0.146},
		 { 0.90, 0.146},
		 { 0.95, 0.146}
	};
	
	// Add a string here to describe the method in words
	public static String[] strPredefinedValues = {
		"Yamil Method A - 5 min gradient from 5% to 95% B, 400 uL/min flow rate, 2.1 x 100 mm",
		"Emma Method A - 5 min gradient from 5% to 95% B, 400 uL/min flow rate, 2.1 x 100 mm",
		"Dionex pump, 5 min gradient from 5% to 95% B, 400 uL/min flow rate, 2.1 x 100 mm, lot B12157",
		"Yan Method A - 5 min gradient from 5% to 95% B, 400 uL/min flow rate, 2.1 x 100 mm",
		"Yan Method B - 30 min gradient from 5% to 95% B, 400 uL/min flow rate, 2.1 x 100 mm",
		"Yan Method C - 10 min gradient from 5% to 95% B, 200 uL/min flow rate, 2.1 x 100 mm",
		"Yan Method D - 10 min gradient from 5% to 95% B, 800 uL/min flow rate, 2.1 x 100 mm",
		"Method B - 30 min gradient from 5% to 95% B, 400 uL/min flow rate, 2.1 x 50 mm",		
	};
	
	// Enter the gradient program parameters here
	//
	// Preloaded gradient programs are:
	// {column ID (in mm), column length (in mm), flow rate (in mL/min), instrument dead time (in min)} 
	// {time (in min), solvent composition (as %B)}
	// {time (in min), solvent composition (as %B)}
	// ...
	public static double[][][] dGradientPrograms = 
	{
		{ // Yamil method A
			{2.1, 100, 0.4, 0.036},
			{0.0, 5.0},
			{5.0, 95.0},
			{20, 95.0}
		},
		{ // Emma method A
			{2.1, 100, 0.4, 0.0796},
			{0.0, 5.0},
			{5.0, 95.0},
			{20, 95.0}
		},
		{ // Dionex 5 min gradient, 0.4 mL/min
			{2.1, 100, 0.4, 0.151},
			{0.0, 5.0},
			{5.0, 95.0},
			{20, 95.0}
		},
		{ // Yan method A
			{2.1, 100, 0.4, 0.125},
			{0.0, 5.0},
			{5.0, 95.0},
			{20, 95.0}
		},
		{ // Yan method B
			{2.1, 100, 0.4, 0.125},
			{0.0, 5.0},
			{30.0, 95.0},
			{40.0, 95.0}
		},
		{ // Yan method C
			{2.1, 100, 0.2, 0.25},
			{0.0, 5.0},
			{10.0, 95.0},
			{20, 95.0}
		},
		{ // Yan method D
			{2.1, 100, 0.8, 0.0625},
			{0.0, 5.0},
			{10.0, 95.0},
			{20, 95.0}
		},
		{ // Method B on 50 mm column
			{2.1, 100, 0.4, 0.082},
			{0.0, 5.0},
			{30.0, 95.0},
			{40.0, 95.0}
		},
	};
	
	// Enter the retention times of the standards here
	// 
	// The first part of the array lists the retention times (in min) of the back-calculation standards
	// The second part of the array lists the retention times (in min) of the test compounds, in the same order they are listed below
	// Important: if a retention time isn't available, just enter a zero ('0') in its place. 
	public static double[][][] dPredefinedValues = {
		{ // Yamil method A
			{
				0.725,
				1.03,
				1.557,
				1.923,
				2.101,
				2.472,
				2.916,
				3.387,
				3.803,
				4.18,
				4.541,
				4.89,
				5.226,
				5.545,
				5.844,
				6.171,
				6.582,
				7.109,
				7.791,
				8.634
			},
			{
				1.9859,
				2.4583,
				2.9585,
				4.2911,
				4.6925,
				2.6592,
				2.9217,
				4.7917,
				4.2904,
				3.21,
				2.8965,
				3.9428,
				5.9851,
				1.7759,
				2.8882,
				2.9411,
				2.8745,
			}
		},
		{ // Emma method A
			{
				0.865,
				1.236,
				2.175,
				2.537,
				2.787,
				3.183,
				3.622,
				4.113,
				4.553,
				4.941,
				5.311,
				5.673,
				6.009,
				6.354,
				6.707,
				7.095,
				7.543,
				8.068,
				8.714,
				9.507
			},
			{
				2.658,
				3.192,
				3.683,
				5.018,
				5.406,
				3.295,
				3.571,
				5.492,
				5.018,
				3.924,
				3.554,
				4.708,
				6.879,
				2.399,
				3.562,
				3.614,
				3.571,
			}
		},
		{ // Dionex 5 min gradient, 400 uL/min, 20 solutes
			{
				0.879,
				1.176,
				2.222,
				2.579,
				2.801,
				3.206,
				3.65,
				4.108,
				4.509,
				4.871,
				5.213,
				5.535,
				5.848,
				6.157,
				6.452,
				6.763,
				7.124,
				7.547,
				8.059,
				8.67,
			},
			{
				1.9859,
				2.4583,
				2.9585,
				4.2911,
				4.6925,
				2.6592,
				2.9217,
				4.7917,
				4.2904,
				3.21,
				2.8965,
				3.9428,
				5.9851,
				1.7759,
				2.8882,
				2.9411,
				2.8745,
			}
		},
		{ // Yan method A
			{
				0.8065,
				1.1407,
				1.6849,
				1.9833,
				2.1865,
				2.5721,
				3.0193,
				3.4803,
				3.888,
				4.2589,
				4.6069,
				4.9359,
				5.2467,
				5.5408,
				5.8142,
				6.0845,
				6.3991,
				6.7881,
				7.2657,
				7.8725
			},
			{
				2.0782,
				2.7121,
				0,
				4.3445,
				4.6737,
				2.669,
				2.9429,
				4.7596,
				4.3445,
				3.3199,
				0,
				2.9406,
				4.0651,
				5.9175,
				1.7959,
				2.9533,
				3.0082,
				2.9733,
				4.1542
			}
		},
		{ // Yan method B
			{
				0.79,
				1.0729,
				2.211,
				3.1346,
				3.8632,
				5.3763,
				7.4567,
				9.9958,
				12.3789,
				14.5064,
				16.4304,
				18.1952,
				19.8889,
				21.5272,
				23.1332,
				24.6863,
				26.1634,
				27.556,
				28.8348,
				29.9916
			},
			{
				3.6197,
				4.3193,
				0,
				13.529,
				16.614,
				6.229,
				8.114,
				17.364,
				13.53,
				10.976,
				28.512,
				8.3726,
				16.05,
				24.081,
				3.2615,
				8.893,
				9.1302,
				8.9855,
				15.185
			}
		},
		{ // Yan method C
			{
				1.585,
				2.2392,
				3.3409,
				3.9474,
				4.3535,
				5.1285,
				6.026,
				6.9489,
				7.7649,
				8.5057,
				9.2011,
				9.8605,
				10.4872,
				11.0764,
				11.6214,
				12.1556,
				12.7868,
				13.5547,
				14.5115
			},
			{
				4.1359,
				5.4231,
				0,
				8.6911,
				9.3435,
				5.3268,
				5.8759,
				9.513,
				8.6916,
				6.6718,
				0,
				5.867,
				8.1622,
				11.829,
				3.5657,
				5.8813,
				5.9929,
				5.917,
				8.2828
			}
		},
		{ // Yan method D
			{
				0.4068,
				0.5784,
				1.0928,
				1.4596,
				1.7385,
				2.2947,
				3.0383,
				3.9114,
				4.7177,
				5.4365,
				6.0862,
				6.6929,
				7.2754,
				7.8371,
				8.3792,
				8.8968,
				9.3723,
				9.8275,
				10.2314,
				10.6024
			},
			{
				1.6346,
				2.0011,
				0,
				5.2104,
				6.1647,
				2.5677,
				3.1984,
				6.4026,
				5.2112,
				4.1598,
				0,
				3.2625,
				5.8134,
				8.6848,
				1.4395,
				3.4186,
				3.5049,
				3.4537,
				5.5942
			}
		},
		{ // 50 mm, 2.1 column, 30 min gradient
			{
				0.388,
				0.5955,
				1.4513,
				2.2062,
				2.8926,
				4.2499,
				6.1645,
				8.5738,
				10.9245,
				13.0222,
				14.8872,
				16.5865,
				18.1848,
				19.7277,
				21.2368,
				22.7119,
				24.1476,
				25.5343,
				0,
				0
			},
			{
				1.9859,
				2.4583,
				2.9585,
				4.2911,
				4.6925,
				2.6592,
				2.9217,
				4.7917,
				4.2904,
				3.21,
				2.8965,
				3.9428,
				5.9851,
				1.7759,
				2.8882,
				2.9411,
				2.8745,
			}
		},
	};
	
	public static String[] StationaryPhaseArray = {
	"Agilent Eclipse Plus C18 (3.5 \u00b5m particle size)"
	};
	
	public static String[] StandardCompoundsNameArray = {
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
		{74.06059},
		{88.07624},
		{102.09189},
		{122.06059},
		{136.07624},
		{150.09189},
		{164.10754},
		{178.12319},
		{192.13884},
		{206.15449},
		{220.17014},
		{234.18579},
		{248.20144},
		{262.21709},
		{276.23274},
		{290.24839},
		{304.26404},
		{318.27969},
		{332.29534},
		{346.31099}
	};
	
	public static double[][][] StandardIsocraticDataArray = {
		{{ .05, -0.2049808243}, { .1, -0.388020943}, { .2, -0.668529272}, { .3, -0.782472624}, { .4, -0.937374584}, { .5, -1.036700000}, { .6, -1.131100000}, { .7, -1.217200000}, { .8, -1.296400000}, { .9, -1.369300000}, { .95, -1.403600000}},
		{{ .05, 0.1580438608}, { .1, -0.094052802}, { .2, -0.413256767}, { .3, -0.532052622}, { .4, -0.682102079}, { .5, -0.765439285}, { .6, -0.879382637}, { .7, -0.913240904}, { .8, -0.925140128}, { .9, -0.978540000}, { .95, -0.991800000}},
		{{ .05, 0.7436080676}, { .1, 0.393039189}, { .2, -0.017549640}, { .3, -0.233469362}, { .4, -0.416984639}, { .5, -0.536959956}, { .6, -0.583815537}, { .7, -0.636344588}, { .8, -0.668529272}, { .9, -0.690300000}, { .95, -0.697870000}},
		{{ .05, 1.0056957470}, { .1, 0.678725664}, { .2, 0.231207073}, { .3, -0.081768907}, { .4, -0.335314593}, { .5, -0.512959680}, { .6, -0.665410000}, { .7, -0.784600000}, { .8, -0.884570000}, { .9, -0.961600000}, { .95, -0.994930000}},
		{{ .05, 1.2130748253}, { .1, 0.855017105}, { .2, 0.371037365}, { .3, 0.080850236}, { .4, -0.169688767}, { .5, -0.347903720}, { .6, -0.477121255}, { .7, -0.589350000}, { .8, -0.672400000}, { .9, -0.737200000}, { .95, -0.764040000}},
		{{ .05, 1.5656764102}, { .1, 1.179138284}, { .2, 0.652096280}, { .3, 0.334135120}, { .4, 0.052583478}, { .5, -0.153063025}, { .6, -0.320074626}, { .7, -0.432224606}, { .8, -0.522401236}, { .9, -0.601690000}, { .95, -0.631990000}},
		{{ .05, 2.0099330746}, { .1, 1.584547661}, { .2, 0.993065011}, { .3, 0.620991077}, { .4, 0.287934698}, { .5, 0.038385365}, { .6, -0.153063025}, { .7, -0.320074626}, { .8, -0.444016131}, { .9, -0.527200119}, { .95, -0.547180000}},
		{{ .05, 2.4502000000}, { .1, 2.082180825}, { .2, 1.404370746}, { .3, 0.957257904}, { .4, 0.545906979}, { .5, 0.256544698}, { .6, 0.023707350}, { .7, -0.178246571}, { .8, -0.326106591}, { .9, -0.380730000}, { .95, -0.415100000}},
		{{ .05, 3.0344000000}, { .1, 2.596600000}, { .2, 1.838890634}, { .3, 1.306159518}, { .4, 0.815003166}, { .5, 0.473282162}, { .6, 0.201604410}, { .7, -0.023560732}, { .8, -0.209601022}, { .9, -0.293921908}, { .95, -0.367499276}},
		{{ .05, 3.6413000000}, { .1, 3.138500000}, { .2, 2.279281344}, { .3, 1.662695509}, { .4, 1.088165339}, { .5, 0.692617179}, { .6, 0.384253431}, { .7, 0.133454588}, { .8, -0.074902448}, { .9, -0.240893380}, { .95, -0.274616752}},
		{{ .05, 4.4448000000}, { .1, 3.831000000}, { .2, 2.813800000}, { .3, 2.034854711}, { .4, 1.368099624}, { .5, 0.917018221}, { .6, 0.572787281}, { .7, 0.296708622}, { .8, 0.060136615}, { .9, -0.139019948}, { .95, -0.195885319}},
		{{ .05, 5.853998524}, { .1, 4.44102847}, { .2, 3.028058416}, { .3, 2.20152392}, { .4, 1.640281317}, { .5, 1.144692350}, { .6, 0.764809684}, { .7, 0.462051887}, { .8, 0.214039048}, { .9, -0.023560732}, { .95, -0.101231387}},
		{{ .05, 5.0003000000}, { .1, 4.414200000}, { .2, 3.412000000}, { .3, 2.600100000}, { .4, 1.946837986}, { .5, 1.370426972}, { .6, 0.961350597}, { .7, 0.632166523}, { .8, 0.357406462}, { .9, 0.099482347}, { .95, -0.002876133}},
		{{ .05, 5.5300000000}, { .1, 4.897400000}, { .2, 3.814300000}, { .3, 2.935600000}, { .4, 2.227878223}, { .5, 1.601809089}, { .6, 1.154643887}, { .7, 0.802762439}, { .8, 0.496737888}, { .9, 0.216657917}, { .95, 0.106268337}},
		{{ .05, 5.0780000000}, { .1, 4.596300000}, { .2, 3.738500000}, { .3, 3.004560000}, { .4, 2.376000000}, { .5, 1.833351222}, { .6, 1.352978075}, { .7, 0.976136518}, { .8, 0.647602431}, { .9, 0.343983489}, { .95, 0.218395097}},
		{{ .05, 5.3104000000}, { .1, 4.843000000}, { .2, 4.000000000}, { .3, 3.265900000}, { .4, 2.626800000}, { .5, 2.067069628}, { .6, 1.554467170}, { .7, 1.153437512}, { .8, 0.798224316}, { .9, 0.464518075}, { .95, 0.313045418}},
		{{ .05, 4.9406000000}, { .1, 4.626900000}, { .2, 4.027300000}, { .3, 3.464000000}, { .4, 2.934700000}, { .5, 2.437300000}, { .6, 1.755598163}, { .7, 1.329797144}, { .8, 0.954560907}, { .9, 0.590071177}, { .95, 0.421647358}},
		{{ .05, 4.9409000000}, { .1, 4.626900000}, { .2, 4.027300000}, { .3, 3.439200000}, { .4, 2.911400000}, { .5, 2.415400000}, { .6, 1.959356292}, { .7, 1.511066707}, { .8, 1.109454910}, { .9, 0.712516350}, { .95, 0.529281237}},
		{{ .05, 5.0885000000}, { .1, 4.792700000}, { .2, 4.220600000}, { .3, 3.676700000}, { .4, 3.127400000}, { .5, 2.628400000}, { .6, 2.162513143}, { .7, 1.691160303}, { .8, 1.271527237}, { .9, 0.839534049}, { .95, 0.639788827}},
		{{ .05, 4.9114000000}, { .1, 4.671500000}, { .2, 4.195300000}, { .3, 3.702400000}, { .4, 3.235800000}, { .5, 2.773800000}, { .6, 2.316500000}, { .7, 1.868301005}, { .8, 1.422027200}, { .9, 0.967418217}, { .95, 0.753833248}}
	};
	
	public static String[] TestCompoundNameArray = {
		"N,N-diethylacetamide",
		"N-allyl aniline",
		"1,3-naphthalenediol",
		"p-coumaric acid",
		"diphenylamine",
		"7-amino-4-methylcoumarin",
		"naphthalene acetamide",
		"2-phenylindole",
		"anilinoacetaldehyde diethyl acetal",
		"tetrabutylammonium",
		"dodecanophenone",
		"abscisic acid",
		"tetrapentylammonium",
		"di-n-pentyl phthalate",
		"chlorogenic acid",
		"prednisone",
		"cortisone",
		"hydrocortisone",
		"curcumin"
	};
	
	public static double[][] TestCompoundMZArray = {
		{116.10754},
		{134.09697},
		{161.06026},
		{165.05517},
		{170.09697},
		{176.04460}, // methyl amino coumarin
		{186.09189},
		{194.09697},
		{210.14940},
		{242.28423}, // tetrabutylammonium
		{261.22184}, // dodecanophenone
		{265.14398}, // abscisic acid
		{298.34683}, // tetrapentylammonium
		{307.19093}, // di-n-pentyl phthalate
		{355.10291}, // chlorogenic acid
		{359.18585}, // prednisone
		{361.20150}, // cortisone
		{363.21715}, // hydrocortisone
		{369.13381}, // curcumin
	};
	
	public static double[][][] TestCompoundsIsocraticDataArray = {
		{{ .05, 1.1630803075}, { .1, 0.758122311}, { .2, 0.229949497}, { .3, -0.056781446}, { .4, -0.239628410}, { .5, -0.417080000}, { .6, -0.586240000}, { .7, -0.734520289}, { .8, -0.898260000}, { .9, -1.043000000}, { .95, -1.121270000}},
		{{ .05, 1.1992554012}, { .1, 0.937355662}, { .2, 0.660633833}, { .3, 0.528433005}, { .4, 0.377695669}, { .5, 0.295984194}, { .6, 0.144897739}, { .7, 0.011317761}, { .8, -0.139019948}, { .9, -0.277322646}, { .95, -0.374232659}},
		{{ .05, 2.2132415259}, { .1, 1.839803584}, { .2, 1.156347201}, { .3, 0.669620625}, { .4, 0.237059060}, { .5, -0.058196755}, { .6, -0.285542976}, { .7, -0.452058851}, { .8, -0.696112793}, { .9, -0.749048869}, { .95, -0.696112793}},
		{{ .05, 2.5672000000}, { .1, 2.300500000}, { .2, 1.803889599}, { .3, 1.429821542}, { .4, 1.043083308}, { .5, 0.745414942}, { .6, 0.475685569}, { .7, 0.246098629}, { .8, 0.025062404}, { .9, -0.144982895}, { .95, -0.197890000}},
		{{ .05, 6.3131000000}, { .1, 4.742900000}, { .2, 2.931200000}, { .3, 1.971900178}, { .4, 1.387025471}, { .5, 0.959938256}, { .6, 0.615467385}, { .7, 0.331470728}, { .8, 0.091428974}, { .9, -0.157160173}, { .95, -0.258726157}},
		{{ .05, 2.0053419429}, { .1, 1.553506518}, { .2, 0.910726521}, { .3, 0.517252530}, { .4, 0.176568768}, { .5, -0.066469281}, { .6, -0.248446518}, { .7, -0.404400000}, { .8, -0.522030000}, { .9, -0.614850000}, { .95, -0.653650000}},
		{{ .05, 2.3596000000}, { .1, 1.872396695}, { .2, 1.107165176}, { .3, 0.593739226}, { .4, 0.163979641}, { .5, -0.115954644}, { .6, -0.305351369}, { .7, -0.440049943}, { .8, -0.583815537}, { .9, -0.612210909}, { .95, -0.660280000}},
		{{ .05, 7.9519000000}, { .1, 5.755280000}, { .2, 3.359200000}, { .3, 2.185140866}, { .4, 1.506312988}, { .5, 1.036149102}, { .6, 0.663753778}, { .7, 0.359290606}, { .8, 0.092588639}, { .9, -0.137050355}, { .95, -0.241150000}},
		{{ .05, 2.5325000000}, { .1, 2.277700000}, { .2, 1.805642148}, { .3, 1.430619927}, { .4, 1.042823640}, { .5, 0.744641487}, { .6, 0.477121255}, { .7, 0.250951131}, { .8, 0.034431215}, { .9, -0.140998514}, { .95, -0.222540000}},
		{{ .05, 2.7041000000}, { .1, 2.355200000}, { .2, 1.667961251}, { .3, 1.063493137}, { .4, 0.426505893}, { .5, -0.165472283}, { .6, -0.682102079}, { .7, -1.219400000}, { .8, -1.710000000}, { .9, -2.175900000}, { .95, -2.400000000}},
		// The following is incorrect for dodecanophenone
		{{ .965, .45}, { .9069281682, .7}, { .8024476487, 1.14}, { .7026600699, 1.58}},
		{{ .05, 2.2709000000}, { .1, 1.860802350}, { .2, 0.993792594}, { .3, 0.598822999}, { .4, 0.130280680}, { .5, -0.171812461}, { .6, -0.468605404}, { .7, -0.589348026}, { .8, -0.731920000}, { .9, -0.833570000}, { .95, -0.874710000}},
		{{ .05, 4.8266000000}, { .1, 4.227800000}, { .2, 3.144400000}, { .3, 2.197300000}, { .4, 1.363282999}, { .5, 0.604917202}, { .6, 0.023707350}, { .7, -0.567628776}, { .8, -1.044800000}, { .9, -1.465100000}, { .95, -1.638200000}},
		{{ .05, 4.6085000000}, { .1, 4.258100000}, { .2, 3.603500000}, { .3, 2.980400000}, { .4, 2.461300000}, { .5, 1.963974741}, { .6, 1.477216799}, { .7, 1.081801073}, { .8, 0.723761735}, { .9, 0.366746488}, { .95, 0.186010324}},
		{{ .05, 1.2564652949}, { .1, 0.618583102}, { .2, -0.657300000}, { .3, -1.933100000}, { .4, -3.208900000}, { .5, -4.484700000}, { .6, -5.760500000}, { .7, -7.036300000}, { .8, -8.312100000}, { .9, -9.587900000}, { .95, -10.225000000}},
		{{ .05, 2.9206000000}, { .1, 2.278549872}, { .2, 1.322833139}, { .3, 0.621333986}, { .4, 0.115154467}, { .5, -0.184777438}, { .6, -0.436119650}, { .7, -0.578352642}, { .8, -0.725567773}, { .9, -0.795180000}, { .95, -0.826020000}},
		{{ .05, 3.617967859}, { .1, 2.484493616}, { .2, 1.382058672}, { .3, 0.678425219}, { .4, 0.156047101}, { .5, -0.161296342}, { .6, -0.391537517}, { .7, -0.532052622}, { .8, -0.682102079}, { .9, -0.753290000}, { .95, -0.786220000}},
		{{ .05, 2.4919000000}, { .1, 1.920440194}, { .2, 1.354754852}, { .3, 0.644038637}, { .4, 0.119530267}, { .5, -0.184777438}, { .6, -0.391537517}, { .7, -0.490216553}, { .8, -0.612210909}, { .9, -0.747300000}, { .95, -0.784060000}},
		// The following is incorrect for curcumin
		{{ .9069281682, -.7165521618}, { .8024476487, -.4346436523}, { .7026600699, -.1458461689}, { .603097122, .1849818309}, { .5027039615, .59}, { .4028808372, 1.1163692501}}
};
	
//	public static String[] OtherCompoundsNameArray = {
//		"N,N-diethylacetamide",
//		"N-allyl aniline",
//		"1,3-naphthalenediol",
//		"p-coumaric acid",
//		"diphenylamine",
//		"7-amino-4-methylcoumarin",
//		"naphthalene acetamide",
//		"2-phenylindole",
//		"anilinoacetaldehyde diethyl acetal",
//		"tetrabutylammonium",
//		"dodecanophenone",
//		"abscisic acid",
//		"tetrapentylammonium",
//		"di-n-pentyl phthalate",
//		"chlorogenic acid",
//		"prednisone",
//		"cortisone",
//		"hydrocortisone",
//		"curcumin"
//	};

//	public static double[][][] OtherCompoundsIsocraticDataArray = {
//		//{{ .05, 1.1630803075}, { .1, 0.758122311}, { .2, 0.229949497}, { .3, -0.056781446}, { .4, -0.239628410}, { .5, -0.417080000}, { .6, -0.586240000}, { .7, -0.734520289}, { .8, -0.898260000}, { .9, -1.043000000}, { .95, -1.121270000}},
//		{{ 0.1,	2},{0.2, 0},{0.3, -2},{0.4,-4},{0.5,-6},{0.6,-8}},
//
//		{{ .05, 1.1992554012}, { .1, 0.937355662}, { .2, 0.660633833}, { .3, 0.528433005}, { .4, 0.377695669}, { .5, 0.295984194}, { .6, 0.144897739}, { .7, 0.011317761}, { .8, -0.139019948}, { .9, -0.277322646}, { .95, -0.374232659}},
//		{{ .05, 2.2132415259}, { .1, 1.839803584}, { .2, 1.156347201}, { .3, 0.669620625}, { .4, 0.237059060}, { .5, -0.058196755}, { .6, -0.285542976}, { .7, -0.452058851}, { .8, -0.696112793}, { .9, -0.749048869}, { .95, -0.696112793}},
//		{{ .05, 2.5672000000}, { .1, 2.300500000}, { .2, 1.803889599}, { .3, 1.429821542}, { .4, 1.043083308}, { .5, 0.745414942}, { .6, 0.475685569}, { .7, 0.246098629}, { .8, 0.025062404}, { .9, -0.144982895}, { .95, -0.197890000}},
//		{{ .05, 6.3131000000}, { .1, 4.742900000}, { .2, 2.931200000}, { .3, 1.971900178}, { .4, 1.387025471}, { .5, 0.959938256}, { .6, 0.615467385}, { .7, 0.331470728}, { .8, 0.091428974}, { .9, -0.157160173}, { .95, -0.258726157}},
//		{{ .05, 2.0053419429}, { .1, 1.553506518}, { .2, 0.910726521}, { .3, 0.517252530}, { .4, 0.176568768}, { .5, -0.066469281}, { .6, -0.248446518}, { .7, -0.404400000}, { .8, -0.522030000}, { .9, -0.614850000}, { .95, -0.653650000}},
//		{{ .05, 2.3596000000}, { .1, 1.872396695}, { .2, 1.107165176}, { .3, 0.593739226}, { .4, 0.163979641}, { .5, -0.115954644}, { .6, -0.305351369}, { .7, -0.440049943}, { .8, -0.583815537}, { .9, -0.612210909}, { .95, -0.660280000}},
//		{{ .05, 7.9519000000}, { .1, 5.755280000}, { .2, 3.359200000}, { .3, 2.185140866}, { .4, 1.506312988}, { .5, 1.036149102}, { .6, 0.663753778}, { .7, 0.359290606}, { .8, 0.092588639}, { .9, -0.137050355}, { .95, -0.241150000}},
//		{{ .05, 2.5325000000}, { .1, 2.277700000}, { .2, 1.805642148}, { .3, 1.430619927}, { .4, 1.042823640}, { .5, 0.744641487}, { .6, 0.477121255}, { .7, 0.250951131}, { .8, 0.034431215}, { .9, -0.140998514}, { .95, -0.222540000}},
//		{{ .05, 2.7041000000}, { .1, 2.355200000}, { .2, 1.667961251}, { .3, 1.063493137}, { .4, 0.426505893}, { .5, -0.165472283}, { .6, -0.682102079}, { .7, -1.219400000}, { .8, -1.710000000}, { .9, -2.175900000}, { .95, -2.400000000}},
//		// The following is incorrect for dodecanophenone
//		{{ .965, .45}, { .9069281682, .7}, { .8024476487, 1.14}, { .7026600699, 1.58}},
//		{{ .05, 2.2709000000}, { .1, 1.860802350}, { .2, 0.993792594}, { .3, 0.598822999}, { .4, 0.130280680}, { .5, -0.171812461}, { .6, -0.468605404}, { .7, -0.589348026}, { .8, -0.731920000}, { .9, -0.833570000}, { .95, -0.874710000}},
//		{{ .05, 4.8266000000}, { .1, 4.227800000}, { .2, 3.144400000}, { .3, 2.197300000}, { .4, 1.363282999}, { .5, 0.604917202}, { .6, 0.023707350}, { .7, -0.567628776}, { .8, -1.044800000}, { .9, -1.465100000}, { .95, -1.638200000}},
//		{{ .05, 4.6085000000}, { .1, 4.258100000}, { .2, 3.603500000}, { .3, 2.980400000}, { .4, 2.461300000}, { .5, 1.963974741}, { .6, 1.477216799}, { .7, 1.081801073}, { .8, 0.723761735}, { .9, 0.366746488}, { .95, 0.186010324}},
//		{{ .05, 1.2564652949}, { .1, 0.618583102}, { .2, -0.657300000}, { .3, -1.933100000}, { .4, -3.208900000}, { .5, -4.484700000}, { .6, -5.760500000}, { .7, -7.036300000}, { .8, -8.312100000}, { .9, -9.587900000}, { .95, -10.225000000}},
//		{{ .05, 2.9206000000}, { .1, 2.278549872}, { .2, 1.322833139}, { .3, 0.621333986}, { .4, 0.115154467}, { .5, -0.184777438}, { .6, -0.436119650}, { .7, -0.578352642}, { .8, -0.725567773}, { .9, -0.795180000}, { .95, -0.826020000}},
//		{{ .05, 3.617967859}, { .1, 2.484493616}, { .2, 1.382058672}, { .3, 0.678425219}, { .4, 0.156047101}, { .5, -0.161296342}, { .6, -0.391537517}, { .7, -0.532052622}, { .8, -0.682102079}, { .9, -0.753290000}, { .95, -0.786220000}},
//		{{ .05, 2.4919000000}, { .1, 1.920440194}, { .2, 1.354754852}, { .3, 0.644038637}, { .4, 0.119530267}, { .5, -0.184777438}, { .6, -0.391537517}, { .7, -0.490216553}, { .8, -0.612210909}, { .9, -0.747300000}, { .95, -0.784060000}},
//		// The following is incorrect for curcumin
//		{{ .9069281682, -.7165521618}, { .8024476487, -.4346436523}, { .7026600699, -.1458461689}, { .603097122, .1849818309}, { .5027039615, .59}, { .4028808372, 1.1163692501}}
//	};
	
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
