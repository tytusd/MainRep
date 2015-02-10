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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Vector;

import dialogs.ColumnPropertiesDialog;
import dialogs.ValvePropertiesDialog;

public class HPLCPartValve extends HPLCPart
{
	private static final long serialVersionUID = 1L;

	protected int iNumPorts;
	protected int iNumPositions;
	protected int iDiameter;
	protected int iSwitchState; // 0 for position 1, 1 for position 2
	protected double dStatorHoleLength = 0.1; // in cm
	protected double dStatorHoleDiameter = 24; // in mil
	protected double dGrooveLength = 0.5; // in cm
	protected double dGrooveDiameter = 18; // in mil
	
	ArrayList<ConnectionNode[]> internalConnections = new ArrayList<ConnectionNode[]>();

	private void init(int iNumPorts, int iNumPositions, String strName) 
	{
		this.iNumPorts = iNumPorts;
		this.iNumPositions = iNumPositions;
		
		if (iNumPositions == 2)
		{
			// Circumference with 6 ports should be
			double sixPortCircumference = Math.PI * 4;
			double newCircumference = sixPortCircumference * ((double)iNumPorts / 6);
			this.iDiameter = (int)Math.round(newCircumference / Math.PI);
			this.width = iDiameter;
			this.height = iDiameter;
			this.iSwitchState = 0;
			
			for (int i = 1; i <= iNumPorts; i++)
			{
				double dRadiusToPort = (((double)iDiameter * (double)Globals.gridSpacing) / 2) - ((double)Globals.gridSpacing / 2);
				double dAngleRadians = -((double)i * ((2.0 * Math.PI) / (double)iNumPorts));
				double adj = Math.cos(dAngleRadians) * dRadiusToPort;
				double opp = Math.sin(dAngleRadians) * dRadiusToPort;
				Point portCenter = new Point((width * Globals.gridSpacing / 2) + (int)Math.round(opp), (height * Globals.gridSpacing / 2) - (int)Math.round(adj));
	
				ConnectionNode newNode = new ConnectionNode(this, 1);
				newNode.setX(portCenter.x);
				newNode.setY(portCenter.y);
				this.connectionNodes.add(newNode);
			}
		}
		else
		{
			// Circumference with 7 ports should be
			double sixPortCircumference = Math.PI * 4;
			double newCircumference = sixPortCircumference * (((double)iNumPorts - 1.0) / 6.0);
			this.iDiameter = (int)Math.round(newCircumference / Math.PI);
			this.width = iDiameter;
			this.height = iDiameter;
			this.iSwitchState = 0;
			
			for (int i = 1; i <= iNumPorts - 1; i++)
			{
				double dRadiusToPort = (((double)iDiameter * (double)Globals.gridSpacing) / 2) - ((double)Globals.gridSpacing / 2);
				double dAngleRadians = -((double)i * ((2.0 * Math.PI) / ((double)iNumPorts - 1)));
				double adj = Math.cos(dAngleRadians) * dRadiusToPort;
				double opp = Math.sin(dAngleRadians) * dRadiusToPort;
				Point portCenter = new Point((width * Globals.gridSpacing / 2) + (int)Math.round(opp), (height * Globals.gridSpacing / 2) - (int)Math.round(adj));
	
				ConnectionNode newNode = new ConnectionNode(this, 1);
				newNode.setX(portCenter.x);
				newNode.setY(portCenter.y);
				this.connectionNodes.add(newNode);
			}
			
			Point portCenter = new Point(width * Globals.gridSpacing / 2, height * Globals.gridSpacing / 2);
			
			ConnectionNode newNode = new ConnectionNode(this, 1);
			newNode.setX(portCenter.x);
			newNode.setY(portCenter.y);
			this.connectionNodes.add(newNode);
		}
	}
	
	public HPLCPartValve(int iNumPorts, String strName) 
	{
		super(strName);
		init(iNumPorts, 2, strName);
	}

	public HPLCPartValve(int iNumPorts, int iNumPositions, String strName)
	{
		super(strName);
		init(iNumPorts, iNumPositions, strName);
	}
	
	@Override
	public double getDispersion(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		double dTempKelvin = 25 + 273.15;
		
		double dEluentViscosity = 0;
		
		// Get tube radius in units of cm
		double dGrooveRadius = (this.getGrooveDiameter() * 0.00254) / 2;
		double dStatorHoleRadius = (this.getStatorHoleDiameter() * 0.00254) / 2;
		
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
		
		// Now determine the average diffusion coefficient (cm^2/s)
		double dDiffusionCoefficient = 0.000000074 * (Math.pow(dAssociationParameter * dSolventMW, 0.5) * dTempKelvin) / (dEluentViscosity * Math.pow(dAverageMolarVolume, 0.6));
		
		// Open tube velocity in cm/s
		double dOpenTubeVelocityStator = (dFlowRate / (60 * 1000)) / (Math.PI * Math.pow(dStatorHoleRadius, 2));
		double dOpenTubeVelocityGroove = (dFlowRate / (60 * 1000)) / (Math.PI * Math.pow(dGrooveRadius, 2));

		// in cm^2
		double dZBroadeningStator = (2 * dDiffusionCoefficient * this.getStatorHoleLength() / dOpenTubeVelocityStator) + ((Math.pow(dStatorHoleRadius, 2) * this.getStatorHoleLength() * dOpenTubeVelocityStator) / (24 * dDiffusionCoefficient));
		double dZBroadeningGroove = (2 * dDiffusionCoefficient * this.getGrooveLength() / dOpenTubeVelocityGroove) + ((Math.pow(dGrooveRadius, 2) * this.getGrooveLength() * dOpenTubeVelocityGroove) / (24 * dDiffusionCoefficient));
		
		// convert to mL^2
		double dVolumeBroadening = (Math.pow(Math.sqrt(dZBroadeningStator) * Math.PI * Math.pow(dStatorHoleRadius, 2), 2) * 2) + Math.pow(Math.sqrt(dZBroadeningGroove) * Math.PI * Math.pow(dGrooveRadius, 2), 2);
		
		return dVolumeBroadening;
	}

	// Returns the delay in min
	// Takes the flow rate in uL/min
	@Override
	public double getGradientDelay(double dFlowRate)
	{
		// dVolume in uL
		double dStatorHoleVolume = Math.PI * Math.pow((this.getStatorHoleDiameter() * 0.00254) / 2, 2) * this.getStatorHoleLength();
		double dGrooveVolume = Math.PI * Math.pow((this.getGrooveDiameter() * 0.00254) / 2, 2) * this.getGrooveLength();
		return (((2 * dStatorHoleVolume) + dGrooveVolume) * 1000) / dFlowRate;
	}
	
	// Returns the pressure drop in bar
	// Takes the flow rate in uL/min
	// Takes the solvent B
	@Override
	public double getPressureDrop(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		double dTempKelvin = 25 + 273.15;
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
		
		// Convert to Pa*s
		dEluentViscosity /= 1000;
		
		// Get tube length in units of m
		double dGrooveLength = this.getGrooveLength() / 100;
		
		// Get tube radius in units of m
		double dGrooveRadius = (this.getGrooveDiameter() * 0.0000254) / 2;

		// Get tube length in units of m
		double dStatorHoleLength = this.getStatorHoleLength() / 100;
		
		// Get tube radius in units of m
		double dStatorHoleRadius = (this.getStatorHoleDiameter() * 0.0000254) / 2;

		// Get flow rate in m^3/s
		// 1 m^3 = 1000000 cm^3
		double dFlowRateInUnits = ((dFlowRate / 1000) / 1000000) / 60;
		
		// Gives pressure drop in Pa
		double dGroovePressureDrop = (8 * dEluentViscosity * dGrooveLength * dFlowRateInUnits) / (Math.PI * Math.pow(dGrooveRadius, 4));

		// Gives pressure drop in Pa
		double dStatorHolePressureDrop = (8 * dEluentViscosity * dStatorHoleLength * dFlowRateInUnits) / (Math.PI * Math.pow(dStatorHoleRadius, 4));

		// Return the pressure drop in bar
		return ((dStatorHolePressureDrop * 2) + dGroovePressureDrop) / 100000;
	}

	@Override
	public void showPropertiesDialog(Frame parentFrame)
	{
    	ValvePropertiesDialog dlgValveProperties = new ValvePropertiesDialog(parentFrame);
    	Point dialogPosition = MouseInfo.getPointerInfo().getLocation();
    	dialogPosition.x -= dlgValveProperties.getWidth() / 2;
    	dlgValveProperties.setLocation(dialogPosition);
    	
    	// Show the dialog.
    	dlgValveProperties.setName(this.getName());
    	dlgValveProperties.setStatorHoleLength(this.getStatorHoleLength());
    	dlgValveProperties.setStatorHoleDiameter(this.getStatorHoleDiameter());
    	dlgValveProperties.setGrooveLength(this.getGrooveLength());
    	dlgValveProperties.setGrooveDiameter(this.getGrooveDiameter());
    	dlgValveProperties.setNumPorts(this.iNumPorts);
    	
    	dlgValveProperties.setVisible(true);
    	
    	if (dlgValveProperties.m_bOk == false)
    		return;
    	
    	this.setName(dlgValveProperties.getName());
    	this.setStatorHoleDiameter(dlgValveProperties.getStatorHoleDiameter());
    	this.setStatorHoleLength(dlgValveProperties.getStatorHoleLength());
    	this.setGrooveDiameter(dlgValveProperties.getGrooveDiameter());
    	this.setGrooveLength(dlgValveProperties.getGrooveLength());
	}
	
	public void setStatorHoleLength(double dStatorHoleLength)
	{
		this.dStatorHoleLength = dStatorHoleLength;
	}
	
	public double getStatorHoleLength()
	{
		return this.dStatorHoleLength;
	}
	
	public void setStatorHoleDiameter(double dStatorHoleDiameter)
	{
		this.dStatorHoleDiameter = dStatorHoleDiameter;
	}
	
	public double getStatorHoleDiameter()
	{
		return dStatorHoleDiameter;
	}
	
	public void setGrooveLength(double dGrooveLength)
	{
		this.dGrooveLength = dGrooveLength;
	}
	
	public double getGrooveLength()
	{
		return this.dGrooveLength;
	}
	
	public void setGrooveDiameter(double dGrooveDiameter)
	{
		this.dGrooveDiameter = dGrooveDiameter;
	}
	
	public double getGrooveDiameter()
	{
		return dGrooveDiameter;
	}
	
	// Returns the node that is internally connected to the given node
	@Override
	public ConnectionNode getInternalConnection(ConnectionNode thisNode)
	{
		int thisIndex = connectionNodes.indexOf(thisNode);
		
		if (thisIndex < 0)
			return null;
		
		if (iNumPositions == 2)
		{
			if (iSwitchState == 0)
			{
				if (thisIndex % 2 == 0)
				{
					thisIndex++;
					if (thisIndex >= iNumPorts)
						thisIndex = 0;
				}
				else
				{
					thisIndex--;
					if (thisIndex < 0)
						thisIndex = iNumPorts - 1;
				}
				
				return connectionNodes.get(thisIndex);
			}
			else
			{
				if (thisIndex % 2 == 1)
				{
					thisIndex++;
					if (thisIndex >= iNumPorts)
						thisIndex = 0;
				}
				else
				{
					thisIndex--;
					if (thisIndex < 0)
						thisIndex = iNumPorts - 1;
				}
				
				return connectionNodes.get(thisIndex);
			}
		}
		else
		{
			if (thisIndex == iNumPorts - 1)
			{
				// Center port is always connected to another port
				return connectionNodes.get(iSwitchState);
			}
			else
			{
				if (iSwitchState == thisIndex)
					return connectionNodes.get(iNumPorts - 1);
				else
					return null;
			}
		}
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

		GradientPaint gradient1 = new GradientPaint(this.width * Globals.gridSpacing, 0, new Color(230,230,230), 0, this.height * Globals.gridSpacing, new Color(170,170,170), true);
		GradientPaint gradient2 = new GradientPaint(this.width * Globals.gridSpacing, 0, new Color(170,170,170), 0, this.height * Globals.gridSpacing, new Color(230,230,230), true);

		// Outside circle
		g2dImg.setPaint(gradient2);
		g2dImg.fillOval(0, 0, this.width * Globals.gridSpacing, this.height * Globals.gridSpacing);
		
		// Draw connections between ports
		double dRadiusToPort = (((double)iDiameter * (double)Globals.gridSpacing) / 2) - ((double)Globals.gridSpacing / 2);

		if (iNumPositions == 2)
		{
			for (int i = 0; i < iNumPorts / 2; i++)
			{
				int iPort = i * 2 + this.iSwitchState;
				Color thisColor = this.getConnectionNodes().get(iPort).getFluidColor();
				g2dImg.setColor(thisColor);
				g2dImg.fillArc((Globals.gridSpacing / 2) - (Globals.tubeDiameter / 2), (Globals.gridSpacing / 2) - (Globals.tubeDiameter / 2), (this.width - 1) * Globals.gridSpacing + Globals.tubeDiameter, (this.height - 1) * Globals.gridSpacing + Globals.tubeDiameter, 90 + (360 / iNumPorts) + ((360 / iNumPorts) * iPort), 360 / iNumPorts);
			}
	
			// Cut out part of arc
			g2dImg.setPaint(gradient2);
			g2dImg.fillOval((Globals.gridSpacing / 2) + (Globals.tubeDiameter / 2), (Globals.gridSpacing / 2) + (Globals.tubeDiameter / 2), (this.width - 1) * Globals.gridSpacing - Globals.tubeDiameter, (this.height - 1) * Globals.gridSpacing - Globals.tubeDiameter);
		
			// Draw outline around valve
			g2dImg.setColor(Color.DARK_GRAY);
			g2dImg.drawOval(0, 0, this.width * Globals.gridSpacing, this.height * Globals.gridSpacing);
			
			// Inside circle
			g2dImg.setPaint(gradient1);
			g2dImg.fillOval(Globals.gridSpacing, Globals.gridSpacing, (this.width - 2) * Globals.gridSpacing, (this.height - 2) * Globals.gridSpacing);
			g2dImg.setColor(Color.DARK_GRAY);
			g2dImg.drawOval(Globals.gridSpacing, Globals.gridSpacing, (this.width - 2) * Globals.gridSpacing, (this.height - 2) * Globals.gridSpacing);
		}
		else
		{
			// Draw outline around valve
			g2dImg.setColor(Color.DARK_GRAY);
			g2dImg.drawOval(0, 0, this.width * Globals.gridSpacing, this.height * Globals.gridSpacing);
			
			// Inside circle
			g2dImg.setPaint(gradient1);
			g2dImg.fillOval(Globals.gridSpacing, Globals.gridSpacing, (this.width - 2) * Globals.gridSpacing, (this.height - 2) * Globals.gridSpacing);
			g2dImg.setColor(Color.DARK_GRAY);
			g2dImg.drawOval(Globals.gridSpacing, Globals.gridSpacing, (this.width - 2) * Globals.gridSpacing, (this.height - 2) * Globals.gridSpacing);
		
			// Draw connection to center port
			Color thisColor = this.getConnectionNodes().get(iNumPorts - 1).getFluidColor();
			g2dImg.setColor(thisColor);
			Point centerPoint = new Point(this.width * Globals.gridSpacing / 2, this.height * Globals.gridSpacing / 2);
			double dAngleRadians = -((double)(iSwitchState + 1) * ((2.0 * Math.PI) / ((double)iNumPorts - 1)));
			g2dImg.rotate(dAngleRadians, centerPoint.x, centerPoint.y);
			g2dImg.fillRect((int)(centerPoint.x - (Globals.tubeDiameter / 2)), (int)(centerPoint.y - dRadiusToPort), (int)Globals.tubeDiameter, (int)dRadiusToPort);
			
			double adj = Math.cos(dAngleRadians) * dRadiusToPort;
			double opp = Math.sin(dAngleRadians) * dRadiusToPort;

		}

		TexturePaint texture = new TexturePaint(bufferedImage, new Rectangle(position.x, position.y, this.width * Globals.gridSpacing, this.height * Globals.gridSpacing));
		g2d.setPaint(texture);
		g2d.fillRect(position.x, position.y, this.width * Globals.gridSpacing + 1, this.height * Globals.gridSpacing + 1);
	
		// Draw ports
		if (iNumPositions == 2)
		{
			for (int i = 1; i <= iNumPorts; i++)
			{
				double dAngleRadians = -((double)i * ((2.0 * Math.PI) / (double)iNumPorts));
				double adj = Math.cos(dAngleRadians) * dRadiusToPort;
				double opp = Math.sin(dAngleRadians) * dRadiusToPort;
				
				Point portCenter = new Point((width * Globals.gridSpacing / 2) + (int)Math.round(opp), (height * Globals.gridSpacing / 2) - (int)Math.round(adj));
				
				adj = Math.cos(dAngleRadians) * (dRadiusToPort + 17);
				opp = Math.sin(dAngleRadians) * (dRadiusToPort + 17);
				Point textPoint = new Point(position.x + (width * Globals.gridSpacing / 2) + (int)Math.round(opp), position.y + (height * Globals.gridSpacing / 2) - (int)Math.round(adj));
	
				g2d.setFont(arial);
				g2d.setColor(Color.DARK_GRAY);
				String str = Integer.toString(i);
				double strWidth = g2d.getFontMetrics().getStringBounds(str, g2d).getWidth();
				double strHeight = g2d.getFontMetrics().getStringBounds(str, g2d).getHeight();
				g2d.drawString(str, textPoint.x - (int)(strWidth / 2), textPoint.y + (int)(strHeight / 2));
			}
		}
		else
		{
			for (int i = 1; i <= iNumPorts - 1; i++)
			{
				double dAngleRadians = -((double)i * ((2.0 * Math.PI) / ((double)iNumPorts - 1)));
				double adj = Math.cos(dAngleRadians) * dRadiusToPort;
				double opp = Math.sin(dAngleRadians) * dRadiusToPort;
				
				Point portCenter = new Point((width * Globals.gridSpacing / 2) + (int)Math.round(opp), (height * Globals.gridSpacing / 2) - (int)Math.round(adj));

				adj = Math.cos(dAngleRadians) * (dRadiusToPort + 17);
				opp = Math.sin(dAngleRadians) * (dRadiusToPort + 17);
				Point textPoint = new Point(position.x + (width * Globals.gridSpacing / 2) + (int)Math.round(opp), position.y + (height * Globals.gridSpacing / 2) - (int)Math.round(adj));
	
				g2d.setFont(arial);
				g2d.setColor(Color.DARK_GRAY);
				String str = Integer.toString(i);
				double strWidth = g2d.getFontMetrics().getStringBounds(str, g2d).getWidth();
				double strHeight = g2d.getFontMetrics().getStringBounds(str, g2d).getHeight();
				g2d.drawString(str, textPoint.x - (int)(strWidth / 2), textPoint.y + (int)(strHeight / 2));
			}
			
			// Center port
			Point portCenter = new Point((width * Globals.gridSpacing / 2), (height * Globals.gridSpacing / 2));
			//Point textPoint = new Point(position.x + 20, position.y + 20);
			Point textPoint = new Point(position.x + (width * Globals.gridSpacing / 2) + 9, position.y + (height * Globals.gridSpacing / 2) + 9);
			String str = Integer.toString(iNumPorts);
			double strWidth = g2d.getFontMetrics().getStringBounds(str, g2d).getWidth();
			double strHeight = g2d.getFontMetrics().getStringBounds(str, g2d).getHeight();
			g2d.drawString(str, textPoint.x - (int)(strWidth / 2), textPoint.y + (int)(strHeight / 2));
		}
	
		// Draw name at bottom
		g2d.setColor(Color.BLACK);
		g2d.setFont(arial);
		double strWidth = g2d.getFontMetrics().getStringBounds(name, g2d).getWidth();
		double strHeight = g2d.getFontMetrics().getStringBounds(name, g2d).getHeight();
		Point center = new Point(position.x + (width * Globals.gridSpacing) / 2, position.y + (height * Globals.gridSpacing) / 2);
		g2d.drawString(name, center.x - (int)(strWidth / 2), position.y + (height * Globals.gridSpacing) + (int)(1.5 * strHeight) + (int)(strHeight / 2));
		
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
		
		g2d.fillOval(position.x, position.y, this.width * Globals.gridSpacing + 1, this.height * Globals.gridSpacing + 1);

		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2d.setComposite(myAlpha);		

		//drawConnectionNodes(g2d, position);
	}

	public boolean isPointInValveSwitchArea(Point pointInPart)
	{
		Point center = new Point((width * Globals.gridSpacing) / 2, (height * Globals.gridSpacing) / 2);
		double dDistance = center.distance(new Point2D.Double(pointInPart.x, pointInPart.y));
		
		if (dDistance > (double)((width - 2) * Globals.gridSpacing) / 2)
			return false;
		
		return true;
	}
	
	public int getSwitchState()
	{
		return this.iSwitchState;
	}
	
	public void setSwitchState(int iState)
	{
		this.iSwitchState = iState;
	}
	
	public void toggleSwitchState()
	{
		if (this.iNumPositions == 2)
		{
			if (this.iSwitchState == 1)
				this.iSwitchState = 0;
			else
				this.iSwitchState = 1;
		}
		else
		{
			this.iSwitchState++;
			if (iSwitchState >= this.iNumPositions)
			{
				iSwitchState = 0;
			}
		}
	}
	
	// Returns the direction the cursor should go (1 = clockwise, 0 = counter-clockwise)
	public int getCursor()
	{
		if (this.iNumPositions == 2)
		{
			if (this.iSwitchState == 1)
				return 1;
			else
				return 0;
		}
		else
		{
			return 0;
		}
	}
}
