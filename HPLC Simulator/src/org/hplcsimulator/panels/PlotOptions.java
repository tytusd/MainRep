package org.hplcsimulator.panels;

import org.jdesktop.swingx.JXPanel;

import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

public class PlotOptions extends JXPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel jlblSecondPlot = null;
	public JRadioButton jrdoNoPlot = null;
	public JRadioButton jrdoSolventBFraction = null;
	public JRadioButton jrdoBackpressure = null;
	public JRadioButton jrdoMobilePhaseViscosity = null;
	public JRadioButton jrdoRetentionFactor = null;
	public JRadioButton jrdoPosition = null;
	public JComboBox jcboRetentionFactorCompounds = null;
	public JComboBox jcboPositionCompounds = null;
	/**
	 * This method initializes 
	 * 
	 */
	public PlotOptions() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        jlblSecondPlot = new JLabel();
        jlblSecondPlot.setBounds(new Rectangle(8, 8, 129, 16));
        jlblSecondPlot.setText("Second Plot:");
        this.setLayout(null);
        this.setSize(new Dimension(254, 223));
        this.setPreferredSize(this.getSize());
        this.setBackground(new Color(214, 217, 223));
        this.add(jlblSecondPlot, null);
        this.add(getJrdoNoPlot(), null);
        this.add(getJrdoSolventBFraction(), null);
        this.add(getJrdoBackpressure(), null);
        this.add(getJrdoMobilePhaseViscosity(), null);
        this.add(getJrdoRetentionFactor(), null);
        this.add(getJrdoPosition(), null);
        this.add(getJcboRetentionFactorCompounds(), null);
        this.add(getJcboPositionCompounds(), null);
			
	}

	/**
	 * This method initializes jrdoNoPlot	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoNoPlot() {
		if (jrdoNoPlot == null) {
			jrdoNoPlot = new JRadioButton();
			jrdoNoPlot.setBounds(new Rectangle(8, 28, 201, 17));
			jrdoNoPlot.setText("No plot");
			jrdoNoPlot.setSelected(true);
		}
		return jrdoNoPlot;
	}

	/**
	 * This method initializes jrdoSolventBFraction	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoSolventBFraction() {
		if (jrdoSolventBFraction == null) {
			jrdoSolventBFraction = new JRadioButton();
			jrdoSolventBFraction.setBounds(new Rectangle(8, 52, 201, 17));
			jrdoSolventBFraction.setText("Solvent B fraction");
			jrdoSolventBFraction.setActionCommand("Plot solvent B fraction");
		}
		return jrdoSolventBFraction;
	}

	/**
	 * This method initializes jrdoBackpressure	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoBackpressure() {
		if (jrdoBackpressure == null) {
			jrdoBackpressure = new JRadioButton();
			jrdoBackpressure.setBounds(new Rectangle(8, 76, 201, 17));
			jrdoBackpressure.setText("Backpressure");
			jrdoBackpressure.setActionCommand("Plot backpressure");
		}
		return jrdoBackpressure;
	}

	/**
	 * This method initializes jrdoMobilePhaseViscosity	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoMobilePhaseViscosity() {
		if (jrdoMobilePhaseViscosity == null) {
			jrdoMobilePhaseViscosity = new JRadioButton();
			jrdoMobilePhaseViscosity.setBounds(new Rectangle(8, 100, 201, 17));
			jrdoMobilePhaseViscosity.setText("Mobile phase viscosity");
			jrdoMobilePhaseViscosity.setActionCommand("Plot mobile phase viscosity");
		}
		return jrdoMobilePhaseViscosity;
	}

	/**
	 * This method initializes jrdoRetentionFactor	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoRetentionFactor() {
		if (jrdoRetentionFactor == null) {
			jrdoRetentionFactor = new JRadioButton();
			jrdoRetentionFactor.setBounds(new Rectangle(8, 124, 201, 17));
			jrdoRetentionFactor.setText("Retention factor of:");
			jrdoRetentionFactor.setActionCommand("Plot retention factor");
		}
		return jrdoRetentionFactor;
	}

	/**
	 * This method initializes jrdoPosition	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJrdoPosition() {
		if (jrdoPosition == null) {
			jrdoPosition = new JRadioButton();
			jrdoPosition.setBounds(new Rectangle(8, 172, 233, 17));
			jrdoPosition.setActionCommand("Plot position");
			jrdoPosition.setText("Position along column of:");
		}
		return jrdoPosition;
	}

	/**
	 * This method initializes jcboRetentionFactorCompounds	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboRetentionFactorCompounds() {
		if (jcboRetentionFactorCompounds == null) {
			jcboRetentionFactorCompounds = new JComboBox();
			jcboRetentionFactorCompounds.setBounds(new Rectangle(32, 144, 209, 25));
			jcboRetentionFactorCompounds.setActionCommand("RetentionFactorCompoundChanged");
		}
		return jcboRetentionFactorCompounds;
	}

	/**
	 * This method initializes jcboPositionCompounds	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJcboPositionCompounds() {
		if (jcboPositionCompounds == null) {
			jcboPositionCompounds = new JComboBox();
			jcboPositionCompounds.setBounds(new Rectangle(32, 192, 208, 25));
			jcboPositionCompounds.setActionCommand("PositionCompoundChanged");
		}
		return jcboPositionCompounds;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
