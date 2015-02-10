package visualizer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import dialogs.AutosamplerPropertiesDialog;
import dialogs.PumpPropertiesDialog;

public class HPLCPartPump extends HPLCPart
{
	private static final long serialVersionUID = 1L;

	private int m_iSolventA = 0;
	private int m_iSolventB = 0;
	private double m_dPumpVolume = 50; //  in uL
	private double m_dPressureLimit = 1000; // in bar
	private double m_dFlowRate = 400; // in uL/min
	private ArrayList<double[]> m_gradientProgram;
	private double m_dSolventBFraction = 0;
	
	public void updateSolventBFraction()
	{
		// First, determine the range of solvent compositions in the gradient program
		double dMinB = 100;
		double dMaxB = 0;
		
		for (int i = 0; i < this.getGradientProgram().size(); i++)
		{
			double dBFraction = this.getGradientProgram().get(i)[1];
			
			if (dBFraction < dMinB)
				dMinB = dBFraction;
			if (dBFraction > dMaxB)
				dMaxB = dBFraction;
		}
		
		double dMaxViscosity = 0;
		double dSolventBFractionWithHighestViscosity = 0;
		
		dMinB /= 100;
		dMaxB /= 100;
		
		// Calculate the eluent viscosity at 100 solvent compositions
		for (int i = 0; i <= 100; i++)
		{
			double dSolventBFraction = (((dMaxB - dMinB) / (double)100) * i) + dMinB;
			double dTempKelvin = 25 + 273.15;
			
			double dEluentViscosity = 0;
			
			// Calculate eluent viscosity
			if (this.getSolventB() == 1)
			{
				// This formula is for acetonitrile/water mixtures:
				// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
				dEluentViscosity = Math.exp((dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
			}
			else if (this.getSolventB() == 0)
			{
				// This formula is for methanol/water mixtures:
				// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30–44.
				dEluentViscosity = Math.exp((dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
			}
			
			if (dEluentViscosity > dMaxViscosity)
			{
				dSolventBFractionWithHighestViscosity = dSolventBFraction;
				dMaxViscosity = dEluentViscosity;
			}
		}
		
		this.m_dSolventBFraction = dSolventBFractionWithHighestViscosity;
		
	}
	
	public double getSolventBFraction()
	{
		return this.m_dSolventBFraction;
	}
	
	public double getFlowRate()
	{
		return m_dFlowRate;
	}
	
	public void setFlowRate(double dFlowRate)
	{
		this.m_dFlowRate = dFlowRate;
	}
	
	public int getSolventA()
	{
		return m_iSolventA;
	}
	
	public void setSolventA(int iSolventA)
	{
		this.m_iSolventA = iSolventA;
		updateSolventBFraction();
	}
	
	public int getSolventB()
	{
		return m_iSolventB;
	}

	public void setSolventB(int iSolventB)
	{
		this.m_iSolventB = iSolventB;
		updateSolventBFraction();
	}

	public double getPumpVolume()
	{
		return this.m_dPumpVolume;
	}

	public void setPumpVolume(double dPumpVolume)
	{
		this.m_dPumpVolume = dPumpVolume;
	}
	
	public double getPressureLimit()
	{
		return this.m_dPressureLimit;
	}

	public void setPressureLimit(double dPressureLimit)
	{
		this.m_dPressureLimit = dPressureLimit;
	}
	
	public ArrayList<double[]> getGradientProgram()
	{
		return this.m_gradientProgram;
	}
	
	public void setGradientProgam(ArrayList<double[]> gradientProgram)
	{
		this.m_gradientProgram = gradientProgram;
		updateSolventBFraction();
	}
	
	public HPLCPartPump(double dPressureLimit, String strName) 
	{
		super(strName);
		
		this.m_dPressureLimit = dPressureLimit;
		this.width = 3;
		this.height = 2;
		
		ConnectionNode newNode = new ConnectionNode(this, 1);
		newNode.setX((width * Globals.gridSpacing) / 2);
		newNode.setY((height * Globals.gridSpacing) - (Globals.gridSpacing / 2));
		this.connectionNodes.add(newNode);
		
		// Initialize the gradient program to default values
		ArrayList<double[]> defaultProgram = new ArrayList<double[]>();
		double[] row1 = {0, 5};
		double[] row2 = {10, 95};
		double[] row3 = {15, 95};
		defaultProgram.add(row1);
		defaultProgram.add(row2);
		defaultProgram.add(row3);
		this.setGradientProgam(defaultProgram);
		
		updateSolventBFraction();
		
		// Select a fluid color for this pump
		fluidColor = Globals.standardFluidColors[0];
	}
	
	@Override
	public void showPropertiesDialog(Frame parentFrame)
	{
    	PumpPropertiesDialog dlgPumpProperties = new PumpPropertiesDialog(parentFrame);
    	Point dialogPosition = MouseInfo.getPointerInfo().getLocation();
    	dialogPosition.x -= dlgPumpProperties.getWidth() / 2;
    	dlgPumpProperties.setLocation(dialogPosition);
    	
    	// Show the dialog.
    	dlgPumpProperties.setName(this.getName());
    	dlgPumpProperties.setFluidColor(this.getFluidColor());
    	dlgPumpProperties.setSolventA(this.getSolventA());
    	dlgPumpProperties.setSolventB(this.getSolventB());
    	dlgPumpProperties.setPumpVolume(this.getPumpVolume());
    	dlgPumpProperties.setPressureLimit(this.getPressureLimit());
    	dlgPumpProperties.setGradientProgram(this.getGradientProgram());
    	dlgPumpProperties.setFlowRate(this.getFlowRate());
    	
    	dlgPumpProperties.setVisible(true);
    	
    	if (dlgPumpProperties.m_bOk == false)
    		return;
    	
    	this.setName(dlgPumpProperties.getName());
    	this.setFluidColor(dlgPumpProperties.getFluidColor());
    	this.setSolventA(dlgPumpProperties.getSolventA());
    	this.setSolventB(dlgPumpProperties.getSolventB());
    	this.setPumpVolume(dlgPumpProperties.getPumpVolume());
    	this.setPressureLimit(dlgPumpProperties.getPressureLimit());
    	this.setGradientProgam(dlgPumpProperties.getGradientProgram());
    	this.setFlowRate(dlgPumpProperties.getFlowRate());
	}
	
	@Override
	public void drawPart(Graphics2D g2d, Point position)
	{
		super.drawPart(g2d, position);
		
		BufferedImage bufferedImage = new BufferedImage(width * Globals.gridSpacing + 1, height * Globals.gridSpacing + 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2dImg = bufferedImage.createGraphics();
		g2dImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		Font arial = new Font("Arial", Font.BOLD, 12);

		// Draw box first
		g2dImg.setColor(Color.LIGHT_GRAY);
		g2dImg.fillRoundRect(0, 0, width * Globals.gridSpacing, height * Globals.gridSpacing, 5, 5);
		g2dImg.setColor(Color.DARK_GRAY);
		g2dImg.drawRoundRect(0, 0, width * Globals.gridSpacing, height * Globals.gridSpacing, 5, 5);
		
		// Draw name at bottom
		Point center = new Point(position.x + (width * Globals.gridSpacing) / 2, position.y + (height * Globals.gridSpacing) / 2);
		g2d.setColor(Color.BLACK);
		g2d.setFont(arial);
		String str = name;
		double strWidth = g2d.getFontMetrics().getStringBounds(str, g2d).getWidth();
		double strHeight = g2d.getFontMetrics().getStringBounds(str, g2d).getHeight();
		g2d.drawString(str, center.x - (int)(strWidth / 2), position.y + (height * Globals.gridSpacing) + (int)(0.5 * strHeight) + (int)(strHeight / 2));

		TexturePaint texture = new TexturePaint(bufferedImage, new Rectangle(position.x, position.y, this.width * Globals.gridSpacing, this.height * Globals.gridSpacing));
		g2d.setPaint(texture);
		g2d.fillRect(position.x, position.y, this.width * Globals.gridSpacing + 1, this.height * Globals.gridSpacing + 1);

		//drawConnectionNodes(g2d, position);
	}
	
	@Override
	public void drawOverlay(Graphics2D g2d, Point position, boolean bGreen)
	{
		AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);
		g2d.setComposite(myAlpha);

		if (bGreen)
			g2d.setColor(Color.GREEN);
		else
			g2d.setColor(Color.RED);
		
		g2d.fillRoundRect(position.x, position.y, width * Globals.gridSpacing + 1, height * Globals.gridSpacing + 1, 5, 5);

		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2d.setComposite(myAlpha);		

		//drawConnectionNodes(g2d, position);
	}
}
