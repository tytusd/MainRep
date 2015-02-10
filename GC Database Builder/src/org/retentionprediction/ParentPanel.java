package org.retentionprediction;

import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Dimension;

import java.awt.Font;

import javax.swing.JViewport;
import javax.swing.Scrollable;

import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.GridBagLayout;
import javax.swing.JTabbedPane;
import javax.swing.JProgressBar;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JTextField;

public class ParentPanel extends JPanel implements Scrollable, ComponentListener, ActionListener
{
	private static final long serialVersionUID = 1L;
	private JTabbedPane jTabbedPane = null;
	private JPanel jPanelInstructions = null;
	private JPanel jPanelRun1 = null;
	private JPanel jPanelRun2 = null;
	private JPanel jPanelRun3 = null;
	private JPanel jPanelRun4 = null;
	private JPanel jPanelRun5 = null;
	private JPanel jPanelRun6 = null;
	private JPanel jPanelFinalFit = null;
	private RoadMapPanel jPanelRoadMap = null;
	private JLabel jLabelYourProgress = null;
	private JProgressBar jProgressBarOverallProgress = null;
	private JPanel jPanel = null;
	
	public TopPanel measuredRetentionTimesPanel[];
	public TopPanel2 backCalculationPanel[];
	private JButton jButtonSolve = null;
	private JLabel jLabelRetentionTime1 = null;
	private JLabel jLabelRetentionTime2 = null;
	private JLabel jLabelRetentionTime3 = null;
	private JLabel jLabelRetentionTime4 = null;
	private JLabel jLabelRetentionTime5 = null;
	private JLabel jLabelRetentionTime6 = null;
	private JTextField jtxtRetentionTime1 = null;
	private JLabel jLabelRetentionTime1Unit = null;
	private JTextField jtxtRetentionTime2 = null;
	private JLabel jLabelRetentionTime2Unit = null;
	private JTextField jtxtRetentionTime3 = null;
	private JLabel jLabelRetentionTime3Unit = null;
	private JTextField jtxtRetentionTime4 = null;
	private JLabel jLabelRetentionTime4Unit = null;
	private JTextField jtxtRetentionTime5 = null;
	private JLabel jLabelRetentionTime5Unit = null;
	private JTextField jtxtRetentionTime6 = null;
	private JLabel jLabelRetentionTime6Unit = null;
	private JLabel jLabelH = null;
	private JLabel jLabelS = null;
	private JLabel jLabelCp = null;
	private JLabel jLabelFitError = null;
	private JLabel jLabelIteration = null;
	private JButton jButtonGenerateReport = null;
	private JLabel jLabel = null;
	/**
	 * This is the default constructor
	 */
	public ParentPanel() 
	{
		super();
		initialize();
	}

	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{

		jLabelYourProgress = new JLabel();
		jLabelYourProgress.setBounds(new Rectangle(140, 8, 93, 16));
		jLabelYourProgress.setFont(new Font("Dialog", Font.BOLD, 12));
		jLabelYourProgress.setText("Your progress:");
		this.setLayout(null);
		
		this.setVisible(true);
		//this.setBounds(new Rectangle(0, 0, 943, 615));
		this.setSize(new Dimension(943, 731));
        this.setPreferredSize(new Dimension(890,685));
        this.setMinimumSize(new Dimension(890,685));


        // Must create the panels before the jTappedPane is created
        // Create the back-calculation panel
        backCalculationPanel = new TopPanel2[6];
        for (int i = 0; i < backCalculationPanel.length; i++)
        {
        	backCalculationPanel[i] = new TopPanel2();
        	backCalculationPanel[i].jbtnNextStep.addActionListener(this);
        }
        
        measuredRetentionTimesPanel = new TopPanel[6];
        for (int i = 0; i < backCalculationPanel.length; i++)
        {
        	measuredRetentionTimesPanel[i] = new TopPanel();
            measuredRetentionTimesPanel[i].setProgramNumber(i);
            measuredRetentionTimesPanel[i].jbtnNextStep.addActionListener(this);
        }
        
        // Now create the jTabbedPane
        this.add(getJTabbedPane(), null);
        
        // Now add the first pane into each panel
        this.jPanelRun1.add(measuredRetentionTimesPanel[0]);
        this.jPanelRun2.add(measuredRetentionTimesPanel[1]);
        this.jPanelRun3.add(measuredRetentionTimesPanel[2]);
        this.jPanelRun4.add(measuredRetentionTimesPanel[3]);
        this.jPanelRun5.add(measuredRetentionTimesPanel[4]);
        this.jPanelRun6.add(measuredRetentionTimesPanel[5]);
        
        this.add(getJPanelRoadMap(), null);
        this.add(jLabelYourProgress, null);
        this.add(getJProgressBarOverallProgress(), null);
   
		this.addComponentListener(this);

	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public boolean getScrollableTracksViewportHeight() {
		Dimension minSize = this.getMinimumSize();
		Dimension portSize = null;
		if (getParent() instanceof JViewport) 
		{
			JViewport port = (JViewport)getParent();
			portSize = port.getSize();
		}
		else
			return false;
		
		if (portSize.height < minSize.height)
			return false;
		else
			return true;
	}


	@Override
	public boolean getScrollableTracksViewportWidth() {
		Dimension minSize = this.getMinimumSize();
		Dimension portSize = null;
		if (getParent() instanceof JViewport) 
		{
			JViewport port = (JViewport)getParent();
			portSize = port.getSize();
		}
		else
			return false;
		
		if (portSize.width < minSize.width)
			return false;
		else
			return true;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}


	@Override
	public void componentMoved(ComponentEvent arg0) {
		this.revalidate();
	}


	@Override
	public void componentResized(ComponentEvent arg0) 
	{
		// Respond to window resize
		if (arg0.getComponent() == this)
		{
			Dimension size = this.getSize();
			jProgressBarOverallProgress.setSize((int)size.getWidth() / 2, jProgressBarOverallProgress.getHeight());
			jProgressBarOverallProgress.setLocation((int)size.getWidth() / 4, jProgressBarOverallProgress.getY());
			jLabelYourProgress.setLocation(jProgressBarOverallProgress.getX() - jLabelYourProgress.getWidth() - 8, jLabelYourProgress.getY());
			jPanelRoadMap.setLocation((int)(size.getWidth() / 2) - (jPanelRoadMap.getWidth() / 2), jPanelRoadMap.getY());
			jTabbedPane.setSize((int)size.getWidth(), (int)size.getHeight() - jTabbedPane.getY());
			jTabbedPane.setLocation(0, jTabbedPane.getY());
			
		}
	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.setBounds(new Rectangle(0, 180, 937, 551));
			jTabbedPane.setTabPlacement(JTabbedPane.TOP);
			jTabbedPane.addTab("Instructions", null, getJPanelInstructions(), null);
			jTabbedPane.addTab("Program A", null, getJPanelRun1(), null);
			jTabbedPane.addTab("Program B", null, getJPanelRun2(), null);
			jTabbedPane.addTab("Program C", null, getJPanelRun3(), null);
			jTabbedPane.addTab("Program D", null, getJPanelRun4(), null);
			jTabbedPane.addTab("Program E", null, getJPanelRun5(), null);
			jTabbedPane.addTab("Program F", null, getJPanelRun6(), null);
			jTabbedPane.addTab("Final Fit", null, getJPanelFinalFit(), null);
			
		}
		return jTabbedPane;
	}


	/**
	 * This method initializes jPanelInstructions	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelInstructions() {
		if (jPanelInstructions == null) {
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(440, 212, 309, 16));
			jLabel.setText("TODO: Add instructions here");
			jPanelInstructions = new JPanel();
			jPanelInstructions.setLayout(null);
			jPanelInstructions.add(jLabel, null);
		}
		return jPanelInstructions;
	}


	/**
	 * This method initializes jPanelRun1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelRun1() {
		if (jPanelRun1 == null) {
			jPanelRun1 = new JPanel();
			jPanelRun1.setLayout(new GridLayout());
		}
		return jPanelRun1;
	}


	/**
	 * This method initializes jPanelRun2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelRun2() {
		if (jPanelRun2 == null) {
			jPanelRun2 = new JPanel();
			jPanelRun2.setLayout(new GridLayout());
		}
		return jPanelRun2;
	}


	/**
	 * This method initializes jPanelRun3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelRun3() {
		if (jPanelRun3 == null) {
			jPanelRun3 = new JPanel();
			jPanelRun3.setLayout(new GridLayout());
		}
		return jPanelRun3;
	}


	/**
	 * This method initializes jPanelRun4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelRun4() {
		if (jPanelRun4 == null) {
			jPanelRun4 = new JPanel();
			jPanelRun4.setLayout(new GridLayout());
		}
		return jPanelRun4;
	}


	/**
	 * This method initializes jPanelRun5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelRun5() {
		if (jPanelRun5 == null) {
			jPanelRun5 = new JPanel();
			jPanelRun5.setLayout(new GridLayout());
		}
		return jPanelRun5;
	}


	/**
	 * This method initializes jPanelRun6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelRun6() {
		if (jPanelRun6 == null) {
			jPanelRun6 = new JPanel();
			jPanelRun6.setLayout(new GridLayout());
		}
		return jPanelRun6;
	}


	/**
	 * This method initializes jPanelFinalFit	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelFinalFit() {
		if (jPanelFinalFit == null) {
			jLabelIteration = new JLabel();
			jLabelIteration.setBounds(new Rectangle(316, 272, 67, 16));
			jLabelIteration.setText("Iteration:");
			jLabelFitError = new JLabel();
			jLabelFitError.setBounds(new Rectangle(316, 376, 67, 16));
			jLabelFitError.setText("Fit error:");
			jLabelCp = new JLabel();
			jLabelCp.setBounds(new Rectangle(316, 347, 37, 22));
			jLabelCp.setText("<html>C<sub>p</sub>:</html>");
			jLabelS = new JLabel();
			jLabelS.setBounds(new Rectangle(316, 320, 36, 16));
			jLabelS.setText("<html>&Delta;S:</html>");
			jLabelH = new JLabel();
			jLabelH.setBounds(new Rectangle(316, 296, 35, 16));
			jLabelH.setText("<html>&Delta;H:</html>");
			jLabelRetentionTime6Unit = new JLabel();
			jLabelRetentionTime6Unit.setBounds(new Rectangle(616, 180, 53, 16));
			jLabelRetentionTime6Unit.setText("min");
			jLabelRetentionTime5Unit = new JLabel();
			jLabelRetentionTime5Unit.setBounds(new Rectangle(616, 156, 53, 16));
			jLabelRetentionTime5Unit.setText("min");
			jLabelRetentionTime4Unit = new JLabel();
			jLabelRetentionTime4Unit.setBounds(new Rectangle(616, 132, 53, 16));
			jLabelRetentionTime4Unit.setText("min");
			jLabelRetentionTime3Unit = new JLabel();
			jLabelRetentionTime3Unit.setBounds(new Rectangle(616, 108, 53, 16));
			jLabelRetentionTime3Unit.setText("min");
			jLabelRetentionTime2Unit = new JLabel();
			jLabelRetentionTime2Unit.setBounds(new Rectangle(616, 84, 53, 16));
			jLabelRetentionTime2Unit.setText("min");
			jLabelRetentionTime1Unit = new JLabel();
			jLabelRetentionTime1Unit.setBounds(new Rectangle(616, 60, 53, 16));
			jLabelRetentionTime1Unit.setText("min");
			jLabelRetentionTime6 = new JLabel();
			jLabelRetentionTime6.setBounds(new Rectangle(316, 180, 237, 16));
			jLabelRetentionTime6.setText("Measured retention time in program F:");
			jLabelRetentionTime5 = new JLabel();
			jLabelRetentionTime5.setBounds(new Rectangle(316, 156, 237, 16));
			jLabelRetentionTime5.setText("Measured retention time in program E:");
			jLabelRetentionTime4 = new JLabel();
			jLabelRetentionTime4.setBounds(new Rectangle(316, 132, 237, 16));
			jLabelRetentionTime4.setText("Measured retention time in program D:");
			jLabelRetentionTime3 = new JLabel();
			jLabelRetentionTime3.setBounds(new Rectangle(316, 108, 237, 16));
			jLabelRetentionTime3.setText("Measured retention time in program C:");
			jLabelRetentionTime2 = new JLabel();
			jLabelRetentionTime2.setBounds(new Rectangle(316, 84, 237, 16));
			jLabelRetentionTime2.setText("Measured retention time in program B:");
			jLabelRetentionTime1 = new JLabel();
			jLabelRetentionTime1.setBounds(new Rectangle(316, 60, 236, 16));
			jLabelRetentionTime1.setText("Measured retention time in program A:");
			jPanelFinalFit = new JPanel();
			jPanelFinalFit.setLayout(null);
			jPanelFinalFit.add(getJButtonSolve(), null);
			jPanelFinalFit.add(jLabelRetentionTime1, null);
			jPanelFinalFit.add(jLabelRetentionTime2, null);
			jPanelFinalFit.add(jLabelRetentionTime3, null);
			jPanelFinalFit.add(jLabelRetentionTime4, null);
			jPanelFinalFit.add(jLabelRetentionTime5, null);
			jPanelFinalFit.add(jLabelRetentionTime6, null);
			jPanelFinalFit.add(getJtxtRetentionTime1(), null);
			jPanelFinalFit.add(jLabelRetentionTime1Unit, null);
			jPanelFinalFit.add(getJtxtRetentionTime2(), null);
			jPanelFinalFit.add(jLabelRetentionTime2Unit, null);
			jPanelFinalFit.add(getJtxtRetentionTime3(), null);
			jPanelFinalFit.add(jLabelRetentionTime3Unit, null);
			jPanelFinalFit.add(getJtxtRetentionTime4(), null);
			jPanelFinalFit.add(jLabelRetentionTime4Unit, null);
			jPanelFinalFit.add(getJtxtRetentionTime5(), null);
			jPanelFinalFit.add(jLabelRetentionTime5Unit, null);
			jPanelFinalFit.add(getJtxtRetentionTime6(), null);
			jPanelFinalFit.add(jLabelRetentionTime6Unit, null);
			jPanelFinalFit.add(jLabelH, null);
			jPanelFinalFit.add(jLabelS, null);
			jPanelFinalFit.add(jLabelCp, null);
			jPanelFinalFit.add(jLabelFitError, null);
			jPanelFinalFit.add(jLabelIteration, null);
			jPanelFinalFit.add(getJButtonGenerateReport(), null);
		}
		return jPanelFinalFit;
	}


	/**
	 * This method initializes jPanelRoadMap	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private RoadMapPanel getJPanelRoadMap() {
		if (jPanelRoadMap == null) {
			jPanelRoadMap = new RoadMapPanel();
			jPanelRoadMap.setBounds(new Rectangle(8, 36, 785, 129));
		}
		return jPanelRoadMap;
	}


	/**
	 * This method initializes jProgressBarOverallProgress	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBarOverallProgress() {
		if (jProgressBarOverallProgress == null) {
			jProgressBarOverallProgress = new JProgressBar();
			jProgressBarOverallProgress.setBounds(new Rectangle(248, 4, 365, 25));
			jProgressBarOverallProgress.setString("0% complete");
			jProgressBarOverallProgress.setValue(0);
			jProgressBarOverallProgress.setStringPainted(true);
		}
		return jProgressBarOverallProgress;
	}


	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
		}
		return jPanel;
	}


	@Override
	public void actionPerformed(ActionEvent e) 
	{
		// Listen for the Next Step button press
		if (e.getActionCommand() == "Next Step")
		{
			// Figure out which button it was
			for (int i = 0; i < this.measuredRetentionTimesPanel.length; i++)
			{
				if (e.getSource() == measuredRetentionTimesPanel[i].jbtnNextStep)
				{
					// We found the right one, now make it switch to the next panel
					if (i == 0)
					{
						this.jPanelRun1.remove(measuredRetentionTimesPanel[i]);
						this.jPanelRun1.add(backCalculationPanel[i]);
					}
					else if (i == 1)
					{
						this.jPanelRun2.remove(measuredRetentionTimesPanel[i]);
						this.jPanelRun2.add(backCalculationPanel[i]);
					}
					else if (i == 2)
					{
						this.jPanelRun3.remove(measuredRetentionTimesPanel[i]);
						this.jPanelRun3.add(backCalculationPanel[i]);
					}
					else if (i == 3)
					{
						this.jPanelRun4.remove(measuredRetentionTimesPanel[i]);
						this.jPanelRun4.add(backCalculationPanel[i]);
					}
					else if (i == 4)
					{
						this.jPanelRun5.remove(measuredRetentionTimesPanel[i]);
						this.jPanelRun5.add(backCalculationPanel[i]);
					}
					else if (i == 5)
					{
						this.jPanelRun6.remove(measuredRetentionTimesPanel[i]);
						this.jPanelRun6.add(backCalculationPanel[i]);
					}
			        
					backCalculationPanel[i].jpanelStep4.setVisible(true);
					backCalculationPanel[i].jpanelStep5.setVisible(false);
					backCalculationPanel[i].jpanelStep6.setVisible(false);
				}
			}
		}
		else if (e.getActionCommand() == "Next Step2")
		{
			// Figure out which button it was
			for (int i = 0; i < this.backCalculationPanel.length; i++)
			{
				if (e.getSource() == backCalculationPanel[i].jbtnNextStep)
				{
					// We found the right one, now make it switch to the next panel
					if (backCalculationPanel[i].jpanelStep4.isVisible())
					{
						backCalculationPanel[i].jpanelStep4.setVisible(false);
						backCalculationPanel[i].jpanelStep5.setVisible(true);
						backCalculationPanel[i].jpanelStep6.setVisible(false);
						backCalculationPanel[i].jbtnNextStep.setVisible(false);
					}
					

				}

			}

		}

	}


	/**
	 * This method initializes jButtonSolve	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonSolve() {
		if (jButtonSolve == null) {
			jButtonSolve = new JButton();
			jButtonSolve.setBounds(new Rectangle(316, 216, 361, 37));
			jButtonSolve.setText("Solve for this compound's retention parameters");
		}
		return jButtonSolve;
	}


	/**
	 * This method initializes jtxtRetentionTime1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtRetentionTime1() {
		if (jtxtRetentionTime1 == null) {
			jtxtRetentionTime1 = new JTextField();
			jtxtRetentionTime1.setBounds(new Rectangle(556, 56, 57, 26));
			jtxtRetentionTime1.setText("");
		}
		return jtxtRetentionTime1;
	}


	/**
	 * This method initializes jtxtRetentionTime2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtRetentionTime2() {
		if (jtxtRetentionTime2 == null) {
			jtxtRetentionTime2 = new JTextField();
			jtxtRetentionTime2.setBounds(new Rectangle(556, 80, 57, 26));
			jtxtRetentionTime2.setText("");
		}
		return jtxtRetentionTime2;
	}


	/**
	 * This method initializes jtxtRetentionTime3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtRetentionTime3() {
		if (jtxtRetentionTime3 == null) {
			jtxtRetentionTime3 = new JTextField();
			jtxtRetentionTime3.setBounds(new Rectangle(556, 104, 57, 26));
			jtxtRetentionTime3.setText("");
		}
		return jtxtRetentionTime3;
	}


	/**
	 * This method initializes jtxtRetentionTime4	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtRetentionTime4() {
		if (jtxtRetentionTime4 == null) {
			jtxtRetentionTime4 = new JTextField();
			jtxtRetentionTime4.setBounds(new Rectangle(556, 128, 57, 26));
			jtxtRetentionTime4.setText("");
		}
		return jtxtRetentionTime4;
	}


	/**
	 * This method initializes jtxtRetentionTime5	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtRetentionTime5() {
		if (jtxtRetentionTime5 == null) {
			jtxtRetentionTime5 = new JTextField();
			jtxtRetentionTime5.setBounds(new Rectangle(556, 152, 57, 26));
			jtxtRetentionTime5.setText("");
		}
		return jtxtRetentionTime5;
	}


	/**
	 * This method initializes jtxtRetentionTime6	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJtxtRetentionTime6() {
		if (jtxtRetentionTime6 == null) {
			jtxtRetentionTime6 = new JTextField();
			jtxtRetentionTime6.setBounds(new Rectangle(556, 176, 57, 26));
			jtxtRetentionTime6.setText("");
		}
		return jtxtRetentionTime6;
	}


	/**
	 * This method initializes jButtonGenerateReport	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonGenerateReport() {
		if (jButtonGenerateReport == null) {
			jButtonGenerateReport = new JButton();
			jButtonGenerateReport.setBounds(new Rectangle(316, 408, 361, 37));
			jButtonGenerateReport.setEnabled(false);
			jButtonGenerateReport.setText("Copy a final report to the clipboard");
		}
		return jButtonGenerateReport;
	}



}  //  @jve:decl-index=0:visual-constraint="-259,126"
