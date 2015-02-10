package visualizer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
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

import javax.swing.Icon;

import svg.WasteGreenSVG;
import svg.WasteRedSVG;
import svg.WasteSVG;

import dialogs.ColumnPropertiesDialog;
import dialogs.PumpPropertiesDialog;
import dialogs.WastePropertiesDialog;

public class HPLCPartWaste extends HPLCPart
{
	private static final long serialVersionUID = 1L;

	public HPLCPartWaste(String strName) 
	{
		super(strName);
		this.width = 2;
		this.height = 3;
		
		ConnectionNode newNode1 = new ConnectionNode(this, 1);
		newNode1.setX((width * Globals.gridSpacing) / 2);
		newNode1.setY(Globals.gridSpacing / 2);
		this.connectionNodes.add(newNode1);
	}
	
	@Override
	public double getDispersion(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		return 0;
	}
	
	// Returns the delay in min
	// Takes the flow rate in uL/min
	@Override
	public double getGradientDelay(double dFlowRate)
	{
		return 0;
	}
	
	// Returns the pressure drop in bar
	// Takes the flow rate in uL/min
	// Takes the solvent B
	@Override
	public double getPressureDrop(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		return 0;
	}
	
	@Override
	public void showPropertiesDialog(Frame parentFrame)
	{
    	WastePropertiesDialog dlgWasteProperties = new WastePropertiesDialog(parentFrame);
    	Point dialogPosition = MouseInfo.getPointerInfo().getLocation();
    	dialogPosition.x -= dlgWasteProperties.getWidth() / 2;
    	dlgWasteProperties.setLocation(dialogPosition);
    	
    	// Show the dialog.
    	dlgWasteProperties.setName(this.getName());
    	
    	dlgWasteProperties.setVisible(true);
    	
    	if (dlgWasteProperties.m_bOk == false)
    		return;
    	
    	this.setName(dlgWasteProperties.getName());
	}
	
	// Returns the node that is internally connected to the given node
	@Override
	public ConnectionNode getInternalConnection(ConnectionNode thisNode)
	{
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

		WasteSVG wastePic = new WasteSVG();
		wastePic.setDimension(new Dimension(this.width * Globals.gridSpacing, this.height * Globals.gridSpacing));
		wastePic.paintIcon(null, g2dImg, 0, 0);

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

		BufferedImage bufferedImage = new BufferedImage(width * Globals.gridSpacing + 1, height * Globals.gridSpacing + 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2dImg = bufferedImage.createGraphics();
		g2dImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (bGreen)
		{
			WasteGreenSVG wasteOverlayPic = new WasteGreenSVG();
			wasteOverlayPic.setDimension(new Dimension(this.width * Globals.gridSpacing, this.height * Globals.gridSpacing));
			wasteOverlayPic.paintIcon(null, g2dImg, 0, 0);
		}
		else
		{
			WasteRedSVG wasteOverlayPic = new WasteRedSVG();
			wasteOverlayPic.setDimension(new Dimension(this.width * Globals.gridSpacing, this.height * Globals.gridSpacing));
			wasteOverlayPic.paintIcon(null, g2dImg, 0, 0);
		}
		
		TexturePaint texture = new TexturePaint(bufferedImage, new Rectangle(position.x, position.y, this.width * Globals.gridSpacing, this.height * Globals.gridSpacing));
		g2d.setPaint(texture);
		g2d.fillRect(position.x, position.y, this.width * Globals.gridSpacing + 1, this.height * Globals.gridSpacing + 1);

		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2d.setComposite(myAlpha);		

		drawConnectionNodes(g2d, position);
	}
}
