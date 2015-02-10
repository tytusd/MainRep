package org.hplcretentionpredictor;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

public class SliderIndicator extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private float fPosition = 0;
	private boolean bEnabled = true;
	private float fYellowLimit = (float)2.0;
	
	/**
	 * This is the default constructor
	 */
	public SliderIndicator() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(409, 85);
		this.setLayout(null);
	}
	
	public void setPosition(float fPos)
	{
		if (fPos > 100)
			fPos = 100;
		if (fPos < 0)
			fPos = 0;
		
		this.fPosition = fPos;
		
		this.repaint();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		super.setEnabled(bEnabled);
		this.bEnabled = bEnabled;
		
		this.repaint();
	}
	
	public void setYellowLimit(float fYellowLimit)
	{
		this.fYellowLimit = fYellowLimit;
	}
	
	@Override
	public void paint(Graphics g) 
	{
		// TODO Auto-generated method stub
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(this.getBackground());
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		// Determine width of gradient area
		int iWidth = this.getWidth() - 20;
		int iX = 10;
		int iEndOfGreen = iX + (iWidth / 3) - (int)((float)iWidth * 0.025);
		int iEndOfGreenYellow = iEndOfGreen + (int)((float)iWidth * .05);
		int iEndOfYellow = iEndOfGreen  + (int)((float)iWidth * 0.025) + (int)(((float)iWidth / 3) * (fYellowLimit - 1));
		int iEndOfYellowRed = (int)((float)iEndOfYellow + + (int)((float)iWidth * .05));
		int iEndOfRed = iX + iWidth;
		
		// Determine height of gradient area
		int iY = 10;
		int iHeight = this.getHeight() - 15 - 25;
		
		Color clrGreen = new Color(0, 161, 75);
		Color clrYellow = new Color(255, 221, 21);
		Color clrRed = new Color(236, 28, 36);
		Color clrBlack = new Color(51, 51, 51);
		
		g2d.setColor(clrGreen);
		g2d.fillRect(iX, iY, iEndOfGreen - iX, iHeight);
		
		GradientPaint gradGreenYellow = new GradientPaint(iEndOfGreen, 0, clrGreen, iEndOfGreenYellow, 0, clrYellow, true);
		g2d.setPaint(gradGreenYellow);
		g2d.fillRect(iEndOfGreen, iY, iEndOfGreenYellow - iEndOfGreen, iHeight);

		g2d.setColor(clrYellow);
		g2d.fillRect(iEndOfGreenYellow, iY, iEndOfYellow - iEndOfGreenYellow, iHeight);

		GradientPaint gradYellowRed = new GradientPaint(iEndOfYellow, 0, clrYellow, iEndOfYellowRed, 0, clrRed, true);
		g2d.setPaint(gradYellowRed);
		g2d.fillRect(iEndOfYellow, iY, iEndOfYellowRed - iEndOfYellow, iHeight);

		g2d.setColor(clrRed);
		g2d.fillRect(iEndOfYellowRed, iY, iEndOfRed - iEndOfYellowRed, iHeight);

		// Draw border
		g2d.setColor(clrBlack);
		g2d.drawRect(iX, iY, iWidth, iHeight);
		
		// Draw hash marks
		for (int i = 0; i < 4; i++)
		{
			int xpos = (int)(iX + ((float)i * ((float)iWidth / (float)3)));
			g2d.drawLine(xpos, iY + iHeight, xpos, iY + iHeight + 10);
			
			// Draw numbers
			Point center = new Point(xpos, iY + iHeight + 10);
			g2d.setColor(clrBlack);
			g2d.setFont(getFont().deriveFont(Font.BOLD));
			
			String str = Integer.toString(i);
			double strWidth = g2d.getFontMetrics().getStringBounds(str, g2d).getWidth();
			double strHeight = g2d.getFontMetrics().getStringBounds(str, g2d).getHeight();
			g2d.drawString(str, center.x - (int)(strWidth / 2), (int)(center.y + strHeight));
		}
		
		// Draw sliders
		if (bEnabled)
		{
			int iSliderXPos = (int)((this.fPosition / 100) * (float)iWidth) + iX;
			IndicatorSVG indicatorPic = new IndicatorSVG();
			//indicatorPic.setDimension(new Dimension(indicatorPic.getOrigWidth(),indicatorPic.getOrigHeight()));
			indicatorPic.paintIcon(null, g2d, iSliderXPos - (indicatorPic.getIconWidth() / 2), 0);
			g2d.translate(indicatorPic.getIconWidth() - 1 + (iSliderXPos - (indicatorPic.getIconWidth() / 2)), indicatorPic.getIconHeight() - 1 + iY + iHeight - 10);
			g2d.rotate(Math.PI);
			indicatorPic.paintIcon(null, g2d, 0, 0);
		}
		
		g2d.setColor(Color.BLACK);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
