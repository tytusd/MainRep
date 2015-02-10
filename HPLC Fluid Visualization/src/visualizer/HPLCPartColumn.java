package visualizer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import dialogs.ColumnPropertiesDialog;
import dialogs.PumpPropertiesDialog;

public class HPLCPartColumn extends HPLCPart
{
	private static final long serialVersionUID = 1L;

	protected double dInnerDiameter = 4.6; // in mm
	protected double dLength = 100; // in mm
	protected double dParticleDiameter = 4.6; // in um
	protected double dTemperature = 25;
	protected boolean bPreheater = false;
	protected double dPreheaterInnerDiameter = 5;
	protected double dPreheaterLength = 10;
	protected double dInterparticlePorosity = 0.4;
	protected double dIntraparticlePorosity = 0.4;
	protected double dATerm = 1;
	protected double dBTerm = 5;
	protected double dCTerm = 0.05;
	
	ArrayList<ConnectionNode[]> internalConnections = new ArrayList<ConnectionNode[]>();
	
	public HPLCPartColumn(double dInnerDiameter, double dLength, double dParticleDiameter, String strName) 
	{
		super(strName);
		this.dInnerDiameter = dInnerDiameter;
		this.dLength = dLength;
		this.dParticleDiameter = dParticleDiameter;
		this.width = 10;
		this.height = 2;
		
		ConnectionNode newNode1 = new ConnectionNode(this, 1);
		newNode1.setX(Globals.gridSpacing / 2);
		newNode1.setY((height * Globals.gridSpacing) / 2);
		this.connectionNodes.add(newNode1);

		ConnectionNode newNode2 = new ConnectionNode(this, 1);
		newNode2.setX((Globals.gridSpacing * width) - (Globals.gridSpacing / 2));
		newNode2.setY((height * Globals.gridSpacing) / 2);
		this.connectionNodes.add(newNode2);
		
		ConnectionNode[] newConnection1 = {newNode1, newNode2};
		internalConnections.add(newConnection1);
		ConnectionNode[] newConnection2 = {newNode2, newNode1};
		internalConnections.add(newConnection2);
	}
	
	@Override
	public double getDispersion(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		double dTempKelvin = this.getTemperature() + 273.15;
		
		double dEluentViscosity = 0;
		
		// Calculate eluent viscosity
		if (iSolventB == 1)
		{
			// This formula is for acetonitrile/water mixtures:
			// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
			dEluentViscosity = Math.exp((dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
		}
		else if (iSolventB == 0)
		{
			// This formula is for methanol/water mixtures:
			// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30–44.
			dEluentViscosity = Math.exp((dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
		}
		
		// Calculate the average diffusion coefficient using Wilke-Chang empirical determination
		// See Wilke, C. R.; Chang, P. AICHE J. 1955, 1, 264-270.
		
		// First, determine association parameter
		double dAssociationParameter = ((1 - dSolventBFraction) * (2.6 - 1.9)) + 1.9;
		
		// Determine weighted average molecular weight of solvent
		double dSolventBMW;
		if (iSolventB == 1)
			dSolventBMW = 41;
		else
			dSolventBMW = 32;
		
		double dSolventMW = (dSolventBFraction * (dSolventBMW - 18)) + 18;
		
		// Use an average molar volume of 120
		double dAverageMolarVolume = 120;
		
		// Now determine the average diffusion coefficient
		double dDiffusionCoefficient = 0.000000074 * (Math.pow(dAssociationParameter * dSolventMW, 0.5) * dTempKelvin) / (dEluentViscosity * Math.pow(dAverageMolarVolume, 0.6));
		
		// Open tube velocity in cm/s
		double dOpenTubeVelocity = (dFlowRate / (60 * 1000)) / (Math.PI * Math.pow(((this.getColumnInnerDiameter() / 10) / 2), 2));

		double dInterstitialVelocity = dOpenTubeVelocity / this.getInterparticlePorosity();

		// Determine the reduced flow velocity
		double dReducedFlowVelocity = ((this.getParticleDiameter() / 10000) * dInterstitialVelocity) / dDiffusionCoefficient;
		
		// Calculate reduced plate height
		double dReducedPlateHeight = this.getATerm() + (this.getBTerm() / dReducedFlowVelocity) + (this.getCTerm() * dReducedFlowVelocity);
    	
		// Calculate HETP
		double dHETP = (this.getParticleDiameter() / 10000) * dReducedPlateHeight;
		
		// Calculate number of theoretical plates
		double dTheoreticalPlates = (this.getColumnLength() / 10) / dHETP;

		double dTotalPorosity = this.getInterparticlePorosity() + this.getIntraparticlePorosity() * (1 - this.getInterparticlePorosity());

		double dVoidVolume = Math.PI * Math.pow(((this.getColumnInnerDiameter() / 10) / 2), 2) * (this.getColumnLength() / 10) * dTotalPorosity;
		
		//double dVoidTime = (dVoidVolume / (dFlowRate / (60 * 1000)));

		double dBroadening = Math.pow(dVoidVolume / Math.sqrt(dTheoreticalPlates), 2);
		
		if (this.getPreheater())
		{
			// Get tube radius in units of cm
			double dPreheaterRadius = (this.getPreheaterInnerDiameter() * 0.00254) / 2;
			
			// Open tube velocity in cm/s
			double dOpenTubeVelocityPreheater = (dFlowRate / (60 * 1000)) / (Math.PI * Math.pow(dPreheaterRadius, 2));

			// in cm^2
			double dZBroadeningPreheater = (2 * dDiffusionCoefficient * this.getPreheaterLength() / dOpenTubeVelocityPreheater) + ((Math.pow(dPreheaterRadius, 2) * this.getPreheaterLength() * dOpenTubeVelocityPreheater) / (24 * dDiffusionCoefficient));
			
			// convert to mL^2
			double dPreheaterBroadening = Math.pow(Math.sqrt(dZBroadeningPreheater) * Math.PI * Math.pow(dPreheaterRadius, 2), 2);

			dBroadening += dPreheaterBroadening;
		}
		
		return dBroadening;
	}
	
	// Returns the delay in min
	// Takes the flow rate in uL/min
	@Override
	public double getGradientDelay(double dFlowRate)
	{
		// dVolume in uL
		double dPreheaterVolume = 0;
		
		if (this.getPreheater())
		{
			dPreheaterVolume = Math.PI * Math.pow((this.getPreheaterInnerDiameter() * 0.00254) / 2, 2) * this.getPreheaterLength();
		}
		
		return (dPreheaterVolume * 1000) / dFlowRate;
	}
	
	// Returns the pressure drop in bar
	// Takes the flow rate in uL/min
	// Takes the solvent B
	@Override
	public double getPressureDrop(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		double dTempKelvin = this.getTemperature() + 273.15;
		double dEluentViscosity = 0;
		
		// Calculate eluent viscosity
		if (iSolventB == 1)
		{
			// This formula is for acetonitrile/water mixtures:
			// See Chen, H.; Horvath, C. Anal. Methods Instrum. 1993, 1, 213-222.
			dEluentViscosity = Math.exp((dSolventBFraction * (-3.476 + (726 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.414 + (1566 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-1.762 + (929 / dTempKelvin))));
		}
		else if (iSolventB == 0)
		{
			// This formula is for methanol/water mixtures:
			// Based on fit of data (at 1 bar) in Journal of Chromatography A, 1210 (2008) 30–44.
			dEluentViscosity = Math.exp((dSolventBFraction * (-4.597 + (1211 / dTempKelvin))) + ((1 - dSolventBFraction) * (-5.961 + (1736 / dTempKelvin))) + (dSolventBFraction * (1 - dSolventBFraction) * (-6.215 + (2809 / dTempKelvin))));
		}

		// dEluentViscosity is in units of cP
		
		// Open tube velocity in cm/s
		double dOpenTubeVelocity = (dFlowRate / (60 * 1000)) / (Math.PI * Math.pow(((this.getColumnInnerDiameter() / 10) / 2), 2));

		// Calculate backpressure in Pa
		double dBackpressure = ((dOpenTubeVelocity / 100.0) * (this.getColumnLength() / 1000.0) * (dEluentViscosity / 1000.0) * 180.0 * Math.pow(1 - this.getInterparticlePorosity(), 2)) / (Math.pow(this.getInterparticlePorosity(), 3) * Math.pow(this.getParticleDiameter() / 1000000, 2));

		if (this.getPreheater())
		{
			// Get tube length in units of m
			double dPreheaterLength = this.getPreheaterLength() / 100;
			
			// Get tube radius in units of m
			double dPreheaterRadius = (this.getPreheaterInnerDiameter() * 0.0000254) / 2;
			
			// Get flow rate in m^3/s
			// 1 m^3 = 1000000 cm^3
			double dFlowRateInUnits = ((dFlowRate / 1000) / 1000000) / 60;
			
			// Gives pressure drop in Pa
			double dPreheaterPressureDrop = (8 * (dEluentViscosity / 1000) * dPreheaterLength * dFlowRateInUnits) / (Math.PI * Math.pow(dPreheaterRadius, 4));

			dBackpressure += dPreheaterPressureDrop;
		}
		
		// Return the pressure drop in bar
		return dBackpressure / 100000;
	}
	
	public void setIntraparticlePorosity(double dIntraparticlePorosity)
	{
		this.dIntraparticlePorosity = dIntraparticlePorosity;
	}
	
	public double getIntraparticlePorosity()
	{
		return this.dIntraparticlePorosity;
	}
	
	public void setATerm(double dATerm)
	{
		this.dATerm = dATerm;
	}
	
	public double getATerm()
	{
		return this.dATerm;
	}
	
	public void setBTerm(double dBTerm)
	{
		this.dBTerm = dBTerm;
	}
	
	public double getBTerm()
	{
		return this.dBTerm;
	}
	
	public void setCTerm(double dCTerm)
	{
		this.dCTerm = dCTerm;
	}
	
	public double getCTerm()
	{
		return this.dCTerm;
	}
	
	public void setInterparticlePorosity(double dInterparticlePorosity)
	{
		this.dInterparticlePorosity = dInterparticlePorosity;
	}
	
	public double getInterparticlePorosity()
	{
		return this.dInterparticlePorosity;
	}
	
	public void setColumnLength(double dColumnLength)
	{
		this.dLength = dColumnLength;
	}
	
	public double getColumnLength()
	{
		return this.dLength;
	}
	
	public void setColumnInnerDiameter(double dColumnInnerDiameter)
	{
		this.dInnerDiameter = dColumnInnerDiameter;
	}
	
	public double getColumnInnerDiameter()
	{
		return dInnerDiameter;
	}

	public void setParticleDiameter(double dParticleDiameter)
	{
		this.dParticleDiameter = dParticleDiameter;
	}
	
	public double getParticleDiameter()
	{
		return this.dParticleDiameter;
	}

	public void setTemperature(double dTemperature)
	{
		this.dTemperature = dTemperature;
	}
	
	public double getTemperature()
	{
		return this.dTemperature;
	}

	public void setPreheater(boolean bPreheaterOn)
	{
		this.bPreheater = bPreheaterOn;
	}
	
	public boolean getPreheater()
	{
		return this.bPreheater;
	}

	public void setPreheaterLength(double dPreheaterLength)
	{
		this.dPreheaterLength = dPreheaterLength;
	}
	
	public double getPreheaterLength()
	{
		return dPreheaterLength;
	}
	
	public void setPreheaterInnerDiameter(double dPreheaterInnerDiameter)
	{
		this.dPreheaterInnerDiameter = dPreheaterInnerDiameter;
	}
	
	public double getPreheaterInnerDiameter()
	{
		return dPreheaterInnerDiameter;
	}
	
	@Override
	public void showPropertiesDialog(Frame parentFrame)
	{
    	ColumnPropertiesDialog dlgColumnProperties = new ColumnPropertiesDialog(parentFrame);
    	Point dialogPosition = MouseInfo.getPointerInfo().getLocation();
    	dialogPosition.x -= dlgColumnProperties.getWidth() / 2;
    	dlgColumnProperties.setLocation(dialogPosition);
    	
    	// Show the dialog.
    	dlgColumnProperties.setName(this.getName());
    	dlgColumnProperties.setColumnInnerDiameter(this.getColumnInnerDiameter());
    	dlgColumnProperties.setColumnLength(this.getColumnLength());
    	dlgColumnProperties.setParticleDiameter(this.getParticleDiameter());
    	dlgColumnProperties.setTemperature(this.getTemperature());
    	dlgColumnProperties.setPreheater(this.getPreheater());
    	dlgColumnProperties.setPreheaterInnerDiameter(this.getPreheaterInnerDiameter());
    	dlgColumnProperties.setPreheaterLength(this.getPreheaterLength());
    	dlgColumnProperties.setInterparticlePorosity(this.getInterparticlePorosity());
    	dlgColumnProperties.setIntraparticlePorosity(this.getIntraparticlePorosity());
    	dlgColumnProperties.setATerm(this.getATerm());
    	dlgColumnProperties.setBTerm(this.getBTerm());
    	dlgColumnProperties.setCTerm(this.getCTerm());
    	
    	dlgColumnProperties.setVisible(true);
    	
    	if (dlgColumnProperties.m_bOk == false)
    		return;
    	
    	this.setName(dlgColumnProperties.getName());
    	this.setColumnInnerDiameter(dlgColumnProperties.getColumnInnerDiameter());
    	this.setColumnLength(dlgColumnProperties.getColumnLength());
    	this.setParticleDiameter(dlgColumnProperties.getParticleDiameter());
    	this.setTemperature(dlgColumnProperties.getTemperature());
    	this.setPreheater(dlgColumnProperties.getPreheater());
    	this.setPreheaterInnerDiameter(dlgColumnProperties.getPreheaterInnerDiameter());
    	this.setPreheaterLength(dlgColumnProperties.getPreheaterLength());
    	this.setInterparticlePorosity(dlgColumnProperties.getInterparticlePorosity());
    	this.setIntraparticlePorosity(dlgColumnProperties.getIntraparticlePorosity());
    	this.setATerm(dlgColumnProperties.getATerm());
    	this.setBTerm(dlgColumnProperties.getBTerm());
    	this.setCTerm(dlgColumnProperties.getCTerm());
	}
	
	// Returns the node that is internally connected to the given node
	@Override
	public ConnectionNode getInternalConnection(ConnectionNode thisNode)
	{
		for (int i = 0; i < internalConnections.size(); i++)
		{
			if (internalConnections.get(i)[0] == thisNode)
				return internalConnections.get(i)[1];
		}
		
		return null;
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

		int iColumnDiameter = 20;

		// Draw tube first
		GradientPaint tubeGradient = new GradientPaint(0, Globals.gridSpacing - (Globals.tubeDiameter / 2), new Color(170,170,170), 0, Globals.gridSpacing, Color.WHITE, true);
		
		g2dImg.setPaint(tubeGradient);
		g2dImg.fillRect((Globals.gridSpacing / 2), Globals.gridSpacing - (Globals.tubeDiameter / 2), width * Globals.gridSpacing - Globals.gridSpacing, Globals.tubeDiameter);

		// Draw column
		GradientPaint columnGradient = new GradientPaint(0, Globals.gridSpacing - (iColumnDiameter / 2), new Color(170,170,170), 0, Globals.gridSpacing, Color.WHITE, true);
		
		g2dImg.setPaint(columnGradient);
		g2dImg.fillRect((3 * Globals.gridSpacing), Globals.gridSpacing - (iColumnDiameter / 2), (width - 6) * Globals.gridSpacing, iColumnDiameter);
		g2dImg.setColor(Color.DARK_GRAY);
		g2dImg.drawRect((3 * Globals.gridSpacing), Globals.gridSpacing - (iColumnDiameter / 2), (width - 6) * Globals.gridSpacing, iColumnDiameter - 1);
		
		// Draw nuts on either end
		GradientPaint nutGradient = new GradientPaint(0, 0, new Color(170,170,170), 0, Globals.gridSpacing, Color.WHITE, true);
		Point nut1Point = new Point((int)(1.5 * (double)Globals.gridSpacing), 0);
		Point nut2Point = new Point((width * Globals.gridSpacing) - (int)(3 * (double)Globals.gridSpacing), 0);
		
		g2dImg.setPaint(nutGradient);
		g2dImg.fillRoundRect(nut1Point.x, nut1Point.y, (int)(1.5 * (double)Globals.gridSpacing), height * Globals.gridSpacing, 5, 5);
		g2dImg.setColor(Color.DARK_GRAY);
		g2dImg.drawRoundRect(nut1Point.x, nut1Point.y, (int)(1.5 * (double)Globals.gridSpacing), height * Globals.gridSpacing, 5, 5);
		g2dImg.drawLine(nut1Point.x, nut1Point.y + 8, nut1Point.x + (int)(1.5 * (double)Globals.gridSpacing), nut1Point.y + 8);
		g2dImg.drawLine(nut1Point.x, nut1Point.y + (height * Globals.gridSpacing) - 8, nut1Point.x + (int)(1.5 * (double)Globals.gridSpacing), nut1Point.y + (height * Globals.gridSpacing) - 8);

		g2dImg.setPaint(nutGradient);
		g2dImg.fillRoundRect(nut2Point.x, nut2Point.y, (int)(1.5 * (double)Globals.gridSpacing), height * Globals.gridSpacing, 5, 5);
		g2dImg.setColor(Color.DARK_GRAY);
		g2dImg.drawRoundRect(nut2Point.x, nut2Point.y, (int)(1.5 * (double)Globals.gridSpacing), height * Globals.gridSpacing, 5, 5);
		g2dImg.drawLine(nut2Point.x, nut2Point.y + 8, nut2Point.x + (int)(1.5 * (double)Globals.gridSpacing), nut2Point.y + 8);
		g2dImg.drawLine(nut2Point.x, nut2Point.y + (height * Globals.gridSpacing) - 8, nut2Point.x + (int)(1.5 * (double)Globals.gridSpacing), nut2Point.y + (height * Globals.gridSpacing) - 8);

		// Draw name at bottom
		Point center = new Point(position.x + (width * Globals.gridSpacing) / 2, position.y + (height * Globals.gridSpacing) / 2);
		g2d.setColor(Color.BLACK);
		g2d.setFont(arial);
		String str = name + " (" + Float.toString((float)this.dInnerDiameter) + " x " + Float.toString((float)this.dLength) + " mm, " + Float.toString((float)this.dParticleDiameter) + " \u00b5m particle size)";
		double strWidth = g2d.getFontMetrics().getStringBounds(str, g2d).getWidth();
		double strHeight = g2d.getFontMetrics().getStringBounds(str, g2d).getHeight();
		g2d.drawString(str, center.x - (int)(strWidth / 2), position.y + (height * Globals.gridSpacing) + (int)(1.5 * strHeight) + (int)(strHeight / 2));
		
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

		BufferedImage bufferedImage = new BufferedImage(width * Globals.gridSpacing + 1, height * Globals.gridSpacing + 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2dImg = bufferedImage.createGraphics();
		g2dImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int iColumnDiameter = 20;

		// Draw tube first
		if (bGreen)
			g2dImg.setColor(Color.GREEN);
		else
			g2dImg.setColor(Color.RED);

		g2dImg.fillRect((Globals.gridSpacing / 2), Globals.gridSpacing - (Globals.tubeDiameter / 2), width * Globals.gridSpacing - Globals.gridSpacing, Globals.tubeDiameter);

		// Draw column
		g2dImg.fillRect((3 * Globals.gridSpacing), Globals.gridSpacing - (iColumnDiameter / 2), (width - 6) * Globals.gridSpacing, iColumnDiameter);
		
		// Draw nuts on either end
		Point nut1Point = new Point((int)(1.5 * (double)Globals.gridSpacing), 0);
		Point nut2Point = new Point((width * Globals.gridSpacing) - (int)(3 * (double)Globals.gridSpacing), 0);
		
		g2dImg.fillRoundRect(nut1Point.x, nut1Point.y, (int)(1.5 * (double)Globals.gridSpacing) + 1, (height * Globals.gridSpacing) + 1, 5, 5);

		g2dImg.fillRoundRect(nut2Point.x, nut2Point.y, (int)(1.5 * (double)Globals.gridSpacing) + 1, (height * Globals.gridSpacing) + 1, 5, 5);

		TexturePaint texture = new TexturePaint(bufferedImage, new Rectangle(position.x, position.y, this.width * Globals.gridSpacing, this.height * Globals.gridSpacing));
		g2d.setPaint(texture);
		g2d.fillRect(position.x, position.y, this.width * Globals.gridSpacing + 1, this.height * Globals.gridSpacing + 1);

		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2d.setComposite(myAlpha);		

		drawConnectionNodes(g2d, position);
	}
}
