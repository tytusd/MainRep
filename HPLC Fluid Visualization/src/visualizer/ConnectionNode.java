package visualizer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.io.Serializable;

import dialogs.TubePropertiesDialog;

public class ConnectionNode implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	static final double dNodeSize = 12;
	protected int x; // Actual position within HPLCPart or on workspace, not the cell it's in
	protected int y; // Gives the center of the node

	HPLCPart parentHPLCPart;
	
	protected ConnectionNode connectedNode1 = null;
	protected ConnectionNode connectedNode2 = null;

	protected int iNumAllowedConnections;
	
	protected double dInnerDiameter = 5; 	// (in mil)
	protected double dLength = 10;			// (in cm)
	protected double dVolume = Math.PI * Math.pow((dInnerDiameter * .0000254) / 2, 2) * (10.0 / 100.0) * 1000000000.0;	// (in uL)
	protected boolean bShowVolume = false;
	
	protected Color colorFluid = new Color(255, 255, 255);
	
	public double getDispersion(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		double dTempKelvin = 25 + 273.15;
		
		double dEluentViscosity = 0;
		
		// Get tube radius in units of cm
		double dRadius = (this.getInnerDiameter() * 0.00254) / 2;

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
		double dOpenTubeVelocity = (dFlowRate / (60 * 1000)) / (Math.PI * Math.pow(dRadius, 2));

		// in cm^2
		double dZBroadening = (2 * dDiffusionCoefficient * this.getLength() / dOpenTubeVelocity) + ((Math.pow(dRadius, 2) * this.getLength() * dOpenTubeVelocity) / (24 * dDiffusionCoefficient));
		
		// convert to mL^2
		double dVolumeBroadening = Math.pow(Math.sqrt(dZBroadening) * Math.PI * Math.pow(dRadius, 2), 2);
		
		return dVolumeBroadening;
	}
	
	// Returns the delay in min
	// Takes the flow rate in uL/min
	public double getGradientDelay(double dFlowRate)
	{
		return dVolume / dFlowRate;
	}
	
	// Returns the pressure drop in bar
	// Takes the flow rate in uL/min
	// Takes the solvent B
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
		double dLength = this.getLength() / 100;
		
		// Get tube radius in units of m
		double dRadius = (this.getInnerDiameter() * 0.0000254) / 2;
		
		// Get flow rate in m^3/s
		// 1 m^3 = 1000000 cm^3
		double dFlowRateInUnits = ((dFlowRate / 1000) / 1000000) / 60;
		
		// Gives pressure drop in Pa
		double dPressureDrop = (8 * dEluentViscosity * dLength * dFlowRateInUnits) / (Math.PI * Math.pow(dRadius, 4));
		
		// Return the pressure drop in bar
		return dPressureDrop / 100000;
	}
	
	public void showPropertiesDialog(Frame parentFrame)
	{
    	TubePropertiesDialog dlgTubeProperties = new TubePropertiesDialog(parentFrame);
    	Point dialogPosition = MouseInfo.getPointerInfo().getLocation();
    	dialogPosition.x -= dlgTubeProperties.getWidth() / 2;
    	dlgTubeProperties.setLocation(dialogPosition);
    	
    	// Show the dialog.
    	dlgTubeProperties.setLength(this.getLength());
    	dlgTubeProperties.setInnerDiameter(this.getInnerDiameter());
    	dlgTubeProperties.setVolume(this.getVolume());
    	dlgTubeProperties.setVolumeVisible(this.getVolumeVisible());
    	
    	dlgTubeProperties.setVisible(true);
    	
    	if (dlgTubeProperties.m_bOk == false)
    		return;
    	
    	this.setInnerDiameter(dlgTubeProperties.getInnerDiameter());
    	this.setLength(dlgTubeProperties.getLength());
    	this.setVolume(dlgTubeProperties.getVolume());
    	this.setVolumeVisible(dlgTubeProperties.getVolumeVisible());
    	
    	this.propagateValues();
	}

	public void setFluidColor(Color newColor)
	{
		this.colorFluid = newColor;
	}
	
	public Color getFluidColor()
	{
		return this.colorFluid;
	}
	
	public void setInnerDiameter(double dInnerDiameter)
	{
		this.dInnerDiameter = dInnerDiameter;
	}
	
	public double getInnerDiameter()
	{
		return this.dInnerDiameter;
	}
	
	public void setLength(double dLength)
	{
		this.dLength = dLength;
	}
	
	public double getLength()
	{
		return this.dLength;
	}
	
	public void setVolume(double dVolume)
	{
		this.dVolume = dVolume;
	}
	
	public double getVolume()
	{
		return this.dVolume;
	}
	
	public void setVolumeVisible(boolean bVolumeVisible)
	{
		this.bShowVolume = bVolumeVisible;
	}
	
	public boolean getVolumeVisible()
	{
		return this.bShowVolume;
	}
	
	public int getNumAllowedConnections()
	{
		return iNumAllowedConnections;
	}
	
	public int getNumConnections()
	{
		int iNumConnections = 0;
		if (connectedNode1 != null)
			iNumConnections++;
		if (connectedNode2 != null)
			iNumConnections++;
		
		return iNumConnections;
	}
	
	public boolean makeConnection(ConnectionNode node)
	{
		if (this.getNumConnections() >= this.getNumAllowedConnections())
			return false;
		if (node.getNumConnections() >= node.getNumAllowedConnections())
			return false;
		
		if (this.connectedNode1 == null)
			this.connectedNode1 = node;
		else
			this.connectedNode2 = node;
		
		// Make the connection back to this node
		if (node.connectedNode1 == null)
			node.connectedNode1 = this;
		else
			node.connectedNode2 = this;
		
/*		// Propagate the dLength and dInnerDiameter parameters down the line
		ConnectionNode thisNode = this;
		ConnectionNode nextNode = node;
		
		while (nextNode.getNumConnections() > 1)
		{
			nextNode.dInnerDiameter = this.dInnerDiameter;
			nextNode.dLength = this.dLength;

			if (nextNode.connectedNode1 == thisNode)
			{
				thisNode = nextNode;
				nextNode = nextNode.connectedNode2;
			}
			else
			{
				thisNode = nextNode;
				nextNode = nextNode.connectedNode1;
			}
		}
		
		nextNode.dInnerDiameter = this.dInnerDiameter;
		nextNode.dLength = this.dLength;
*/				
		return true;
	}
	
	public void propagateValues()
	{
		ConnectionNode thisNode = this;
		ConnectionNode nextNode;
		
		if (this.connectedNode1 != null)
		{
			nextNode = this.connectedNode1;
		
			// Propagate the dLength and dInnerDiameter parameters down the line
			while (nextNode.getNumConnections() > 1)
			{
				nextNode.dInnerDiameter = this.dInnerDiameter;
				nextNode.dLength = this.dLength;
				nextNode.dVolume = this.dVolume;
				nextNode.bShowVolume = this.bShowVolume;
	
				if (nextNode.connectedNode1 == thisNode)
				{
					thisNode = nextNode;
					nextNode = nextNode.connectedNode2;
				}
				else
				{
					thisNode = nextNode;
					nextNode = nextNode.connectedNode1;
				}
			}
			
			nextNode.dInnerDiameter = this.dInnerDiameter;
			nextNode.dLength = this.dLength;
			nextNode.dVolume = this.dVolume;
			nextNode.bShowVolume = this.bShowVolume;
		}
		
		thisNode = this;
		
		if (this.connectedNode2 != null)
		{
			nextNode = this.connectedNode2;
		
			// Propagate the dLength and dInnerDiameter parameters down the line
			while (nextNode.getNumConnections() > 1)
			{
				nextNode.dInnerDiameter = this.dInnerDiameter;
				nextNode.dLength = this.dLength;
				nextNode.dVolume = this.dVolume;
				nextNode.bShowVolume = this.bShowVolume;
	
				if (nextNode.connectedNode1 == thisNode)
				{
					thisNode = nextNode;
					nextNode = nextNode.connectedNode2;
				}
				else
				{
					thisNode = nextNode;
					nextNode = nextNode.connectedNode1;
				}
			}
			
			nextNode.dInnerDiameter = this.dInnerDiameter;
			nextNode.dLength = this.dLength;
			nextNode.dVolume = this.dVolume;
			nextNode.bShowVolume = this.bShowVolume;
		}
	}
	
	public void removeConnection(ConnectionNode node)
	{
		if (this.connectedNode1 == node)
			this.connectedNode1 = null;
		else if (this.connectedNode2 == node)
			this.connectedNode2 = null;
	}
	
	public ConnectionNode[] getConnectedNodes()
	{
		ConnectionNode[] returnArray = {this.connectedNode1, this.connectedNode2};
		return returnArray;
	}
	
	@Override
	public Object clone()
	{
          try
	      {
        	  return super.clone();
	      }
	      catch( CloneNotSupportedException e )
	      {
	              return null;
	      }
	}
	
	public ConnectionNode(HPLCPart parentHPLCPart, int iNumAllowedConnections)
	{
		this.parentHPLCPart = parentHPLCPart;
		this.iNumAllowedConnections = iNumAllowedConnections;
	}
	
	public HPLCPart getParentHPLCPart()
	{
		return this.parentHPLCPart;
	}
	
	public void drawNode(Graphics2D g2d, Point position)
	{
		AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2d.setComposite(myAlpha);
		
		
		if (this.getNumConnections() < this.getNumAllowedConnections())
		{
			g2d.setColor(Color.WHITE);
			g2d.fillOval(position.x + this.x - (int)(dNodeSize / 2), position.y + this.y - (int)(dNodeSize / 2), (int)dNodeSize, (int)dNodeSize);
			
			g2d.setColor(Color.RED);
			myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
			
			//g2d.setColor(Color.DARK_GRAY);
			//myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			
			g2d.setComposite(myAlpha);
			
			g2d.drawOval(position.x + this.x - (int)(dNodeSize / 2), position.y + this.y - (int)(dNodeSize / 2), (int)dNodeSize, (int)dNodeSize);
			
			myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
			//myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			g2d.setComposite(myAlpha);
			
			g2d.fillOval(position.x + this.x - (int)(dNodeSize / 2), position.y + this.y - (int)(dNodeSize / 2), (int)dNodeSize, (int)dNodeSize);
			
			myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			g2d.setComposite(myAlpha);
		}
		
		return;
	}
	
	/*public void drawHighlight(Graphics2D g2d, Point position, boolean bGreen)
	{
		AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.2f);
		g2d.setComposite(myAlpha);
		
		if (bGreen)
			g2d.setColor(Color.GREEN);
		else
			g2d.setColor(Color.RED);
		
		g2d.fillRect(position.x, position.y, width * Globals.gridSpacing, height * Globals.gridSpacing);
		
		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2d.setComposite(myAlpha);
	}*/
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	public int getX()
	{
		return x;
	}

	public void setY(int y)
	{
		this.y = y;
	}
	
	public int getY()
	{
		return y;
	}
	
	public Point getLocation()
	{
		return new Point(x, y);
	}
	
	public int getWidth()
	{
		return (int)ConnectionNode.dNodeSize;
	}
	
	public int getHeight()
	{
		return (int)ConnectionNode.dNodeSize;
	}
}