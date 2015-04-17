package boswell.peakfinderlcfx;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class Main extends Application 
{
	public static String[] StationaryPhaseArray = {
		"Agilent HP-5MS UI"
	};
	
	public static String[] AlkaneNameArray = {
		"7 - n-heptane",
		"8 - n-octane",
		"9 - n-nonane",
		"10 - n-decane",
		"11 - n-undecane",
		"12 - n-dodecane",
		"13 - n-tridecane",
		"14 - n-tetradecane",
		"15 - n-pentadecane",
		"16 - n-hexadecane",
		"17 - n-heptadecane",
		"18 - n-octadecane",
		"19 - n-nonadecane",
		"20 - n-eicosane",
		"21 - n-heneicosane",
		"22 - n-docosane",
		"23 - n-tricosane",
		"24 - n-tetracosane",
		"25 - n-pentacosane",
		"26 - n-hexacosane",
		"28 - n-octacosane",
		"30 - n-triacontane",
		"32 - n-dotriacontane",
		"34 - n-tetratriacontane",
		"36 - n-hexatriacontane"
	};

	public static double[][] AlkaneMZArray = {
		{57, 100},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85},
		{57, 85}
	};
	
	// For the HP-5MS UI column
	public static double[][] AlkaneParamArray = {
		{-35272.82881, -111.5189454, 55.66653177},
		{-39692.06134, -117.8735776, 59.44298395},
		{-42832.99151, -120.2459619, 47.57662236},
		{-48188.29819, -129.8428984, 63.77203649},
		{-53129.24938, -138.115973, 73.0161765},
		{-57837.17313, -145.6402398, 79.22710271},
		{-61742.52683, -150.8140365, 80.62658526},
		{-66822.6602, -159.4917, 89.26156672},
		{-71613.39723, -167.3788168, 96.47383895},
		{-76251.34757, -174.8341622, 103.0541569},
		{-80387.98823, -180.8635413, 106.0897423},
		{-84098.98136, -185.8452722, 107.9595635},
		{-90346.8712, -197.7989808, 122.2308595},
		{-94368.27173, -203.5826331, 124.8160028},
		{-97822.80921, -207.9546372, 125.8386271},
		{-99424.0131, -207.4288682, 118.9603377},
		{-107299.4675, -223.6336173, 138.9402945},
		{-112367.114, -232.2664394, 146.4065421},
		{-112468.1548, -228.0633895, 134.5996668},
		{-122446.0776, -249.6544641, 162.2609452},
		{-128363.578, -256.3028235, 161.9851264},
		{-132477.6217, -258.5097491, 155.5442406},
		{-144919.3652, -282.0241099, 179.9820312},
		{-154754.2116, -298.8054462, 194.350128},
		{-156368.8287, -295.3109635, 181.3871097}
	};
		
	public static String[] TestCompoundNameArray = {
		"ethylbenzene",
		"naphthalene",
		"anthracene",
		"N,N-diethylacetamide",
		"4-nitroaniline",
		"caffeine",
		"phenol",
		"resorcinol",
		"1-naphthol",
		"N,N-dimethylisobutyramide",
		"benzamide",
		"dextromethorphan"
	};
	
	public static double[][] TestCompoundMZArray = {
		{91},
		{128},
		{178},
		{115},
		{65},
		{194},
		{94},
		{110},
		{144},
		{72},
		{77},
		{270, 271}
	};
		
	// For the HP-5MS UI column
	public static double[][] TestCompoundParamArray = {
		{-39593.41823, -113.0246722, 49.35561509},
		{-50537.72611, -125.7870921, 61.50714271},
		{-67831.35373, -144.9435441, 67.87132339},
		{-48984.05073, -132.4205585, 79.44205647},
		{-70003.23272, -158.8742398, 93.52750704},
		{-79557.51079, -173.4741429, 104.4783682},
		{-51384.0244, -141.2477773, 97.57274314},
		{-69042.64151, -173.7961978, 134.1195296},
		{-68766.72882, -160.2840267, 101.6177606},
		{-48432.21981, -132.372841, 79.26371294},
		{-63648.10929, -155.6210846, 105.1415104},
		{-88040.53234, -182.519477, 106.2419028}
	};
	
	// For the HP-5MS UI column
	public static String[] CompoundNameArray = {
		"ethylbenzene",
		"naphthalene",
		"anthracene",
		"N,N-diethylacetamide",
		"4-nitroaniline",
		"caffeine",
		"phenol",
		"resorcinol",
		"1-naphthol",
		"N,N-dimethylisobutyramide",
		"benzamide",
		"dextromethorphan"
	};
		
	// For the HP-5MS UI column
	public static double[][] CompoundParamArray = {
		{-39593.41823, -113.0246722, 49.35561509},
		{-50537.72611, -125.7870921, 61.50714271},
		{-67831.35373, -144.9435441, 67.87132339},
		{-48984.05073, -132.4205585, 79.44205647},
		{-70003.23272, -158.8742398, 93.52750704},
		{-79557.51079, -173.4741429, 104.4783682},
		{-51384.0244, -141.2477773, 97.57274314},
		{-69042.64151, -173.7961978, 134.1195296},
		{-68766.72882, -160.2840267, 101.6177606},
		{-48432.21981, -132.372841, 79.26371294},
		{-63648.10929, -155.6210846, 105.1415104},
		{-88040.53234, -182.519477, 106.2419028}
	};
	
	
	@Override
	public void start(Stage primaryStage) {
		
		PeakFinderLCFX peakFinderGC = new PeakFinderLCFX(primaryStage, StationaryPhaseArray, true);
		peakFinderGC.setStandardCompoundMZData(GlobalsDan.StandardCompoundsMZArray);
		peakFinderGC.setStandardCompoundNames(GlobalsDan.StandardCompoundsNameArray);
		peakFinderGC.setIsocraticDataArray(GlobalsDan.StandardIsocraticDataArray);
		peakFinderGC.setInterpolatedDeadTime();
		peakFinderGC.run();		

	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
}
