package org.retentionprediction.lcdatabasebuilderfx.ui;

import java.awt.Point;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;


public class SliderIndicator extends Canvas 
{
	private float fPosition = 0;
	private boolean bEnabled = true;
	private float fYellowLimit = (float)2.0;
	private float fScaleFactor = (float)javafx.scene.text.Font.getDefault().getSize() / 15;
    private float fontSize = (int)(fScaleFactor * (float)14);
    private Font font = Font.font("System", FontWeight.BOLD, fontSize);

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
	private void initialize() 
	{
        InvalidationListener listener = new InvalidationListener(){
            @Override
            public void invalidated(Observable arg0) {
                redraw();       
            }
        };
        this.widthProperty().addListener(listener);
        this.heightProperty().addListener(listener);

		this.redraw();
	}
	
    @Override
	public boolean isResizable()
    {
    	return true;
    }
    
    @Override
    public double minHeight(double width)
    {
		return fScaleFactor * 100;
    }
    
    @Override
    public double prefWidth(double height) 
    {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) 
    {
        return getHeight();
    }
    
	public void setPosition(float fPos)
	{
		if (fPos > 100)
			fPos = 100;
		if (fPos < 0)
			fPos = 0;
		
		this.fPosition = fPos;
		this.redraw();
	}
	
	public void setShowIndicator(boolean showIndicator)
	{
		this.bEnabled = showIndicator;
		this.redraw();
	}
	
	public void setYellowLimit(float fYellowLimit)
	{
		this.fYellowLimit = fYellowLimit;
		this.redraw();
	}
	
	public void redraw() 
	{
		GraphicsContext gc = this.getGraphicsContext2D();
    	gc.clearRect(0, 0, this.getWidth(), this.getHeight());

		FontLoader fl = Toolkit.getToolkit().getFontLoader();
    	FontMetrics fontMetrics = fl.getFontMetrics(font);

		// Determine width of gradient area
    	int margin = (int) fontMetrics.computeStringWidth("000");
		int iWidth = (int) (this.getWidth() - (margin * 2));
		int iX = margin;
		int iEndOfGreen = iX + (iWidth / 3) - (int)((float)iWidth * 0.025);
		int iEndOfGreenYellow = iEndOfGreen + (int)((float)iWidth * .05);
		int iEndOfYellow = iEndOfGreen  + (int)((float)iWidth * 0.025) + (int)(((float)iWidth / 3) * (fYellowLimit - 1));
		int iEndOfYellowRed = (int)((float)iEndOfYellow + + (int)((float)iWidth * .05));
		int iEndOfRed = iX + iWidth;
		
		// Determine height of gradient area
		int iY = (int) (fScaleFactor * 5);
		
		int lineLength = (int)(8.0 * fScaleFactor);
		// iY is space above the gradient, then save space for the font and the lines
		int iHeight = (int) (this.getHeight() - iY - lineLength - fontMetrics.getMaxAscent());
		
		Color clrGreen = Color.rgb(0, 161, 75);
		Color clrYellow = Color.rgb(255, 221, 21);
		Color clrRed = Color.rgb(236, 28, 36);
		Color clrBlack = Color.rgb(51, 51, 51);
		Color clrGray = Color.web("#D0D2D3");
		
		gc.setFill(clrGreen);
		gc.fillRect(iX, iY, iEndOfGreen - iX, iHeight);
		LinearGradient gradGreenYellow = new LinearGradient(iEndOfGreen, 0, iEndOfGreenYellow, 0, false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, clrGreen),
                new Stop(1.0, clrYellow));
		gc.setFill(gradGreenYellow);
		gc.fillRect(iEndOfGreen, iY, iEndOfGreenYellow - iEndOfGreen, iHeight);

		gc.setFill(clrYellow);
		gc.fillRect(iEndOfGreenYellow, iY, iEndOfYellow - iEndOfGreenYellow, iHeight);

		LinearGradient gradYellowRed = new LinearGradient(iEndOfYellow, 0, iEndOfYellowRed, 0, false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, clrYellow),
                new Stop(1.0, clrRed));
		gc.setFill(gradYellowRed);
		gc.fillRect(iEndOfYellow, iY, iEndOfYellowRed - iEndOfYellow, iHeight);

		gc.setFill(clrRed);
		gc.fillRect(iEndOfYellowRed, iY, iEndOfRed - iEndOfYellowRed, iHeight);

		// Draw border
		gc.setStroke(clrBlack);
		gc.strokeRect(iX, iY, iWidth, iHeight);
		
		// Draw hash marks
		for (int i = 0; i < 4; i++)
		{
			int xpos = (int)(iX + ((float)i * ((float)iWidth / (float)3)));
			gc.strokeLine(xpos, iY + iHeight, xpos, iY + iHeight + lineLength);
			
			// Draw numbers
			Point center = new Point(xpos, iY + iHeight + lineLength);
			gc.setFill(clrBlack);
			gc.setFont(font);
			
			String str = Integer.toString(i);
			gc.setTextAlign(TextAlignment.CENTER);
			double strHeight = fontMetrics.getMaxAscent();
			gc.fillText(str, center.x, center.y + strHeight);
		}
		
		// Draw sliders
		if (bEnabled)
		{
			int iSliderXPos = (int)((this.fPosition / 100) * (float)iWidth) + iX;
			double SVGWidth = 18;
			double SVGHeight = 19.681;
			
			double scaledWidth = ((iHeight + iY * 2) / SVGHeight) * SVGWidth;
			double scaledHeight = ((iHeight + iY * 2) / SVGHeight) * SVGHeight;
			
			gc.setFill(clrGray);
			gc.save();
			gc.translate(iSliderXPos - (scaledWidth / 2), 0);
			gc.scale((iHeight + iY * 2) / SVGHeight, (iHeight + iY * 2) / SVGHeight);
			gc.fillPolygon(new double[]{9, 0, 0, 18, 18}, new double[]{19.681, 10.371, 0, 0, 10.371}, 5);
			gc.setStroke(clrBlack);
			gc.setLineWidth(1);
			gc.strokePolygon(new double[]{9, 0, 0, 18, 18}, new double[]{19.681, 10.371, 0, 0, 10.371}, 5);
			gc.strokeLine(15, 8.616, 15, 2.366);
			gc.strokeLine(12, 8.616, 12, 2.366);
			gc.strokeLine(9, 8.616, 9, 2.366);
			gc.strokeLine(6, 8.616, 6, 2.366);
			gc.strokeLine(3, 8.616, 3, 2.366);
			gc.restore();
		}
	}

}
