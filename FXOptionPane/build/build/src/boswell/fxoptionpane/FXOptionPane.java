package boswell.fxoptionpane;

import javafx.stage.Window;

public class FXOptionPane {
	public static final int WARNING_MESSAGE = 0;
	public static final int INFORMATION_MESSAGE = 1;
	public static final int YES_NO_OPTION = 2;
	
	public static final int YES_OPTION = 0;
	public static final int NO_OPTION = 1;
	
 /*   public static void showConfirmDialog(String message, OptionType optionType, Callback<Option, ?> callback) {
        showConfirmDialog(null, message, optionType, callback);
    }
    */
    public static int showConfirmDialog(Window owner, String message, String windowTitle, int optionType) {
    	return new MessageDialog(owner, message, windowTitle, optionType).showConfirmDialog();
    }
    /*
    public static void showMessageDialog(String message, MessageType messageType) {
        showMessageDialog(null, message, messageType);
    }
    */
    public static void showMessageDialog(Window owner, String message, String windowTitle, int optionType) {
        new MessageDialog(owner, message, windowTitle, optionType).showDialog();
    }

}