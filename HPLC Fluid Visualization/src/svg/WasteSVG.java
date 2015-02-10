package svg;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.Icon; 

/**
 * This class has been automatically generated using svg2java
 * 
 */
public class WasteSVG implements Icon {
	
	private float origAlpha = 1.0f;

	/**
	 * Paints the transcoded SVG image on the specified graphics context. You
	 * can install a custom transformation on the graphics context to scale the
	 * image.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	public void paint(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = 
                (AlphaComposite)origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }
        
		// _0
		AffineTransform trans_0 = g.getTransform();
		paintRootGraphicsNode_0(g);
		g.setTransform(trans_0);

	}

	private void paintShapeNode_0_0_0(Graphics2D g) {
		GeneralPath shape0 = new GeneralPath();
		shape0.moveTo(6.309, 36.04);
		shape0.curveTo(6.309, 39.731003, 12.683001, 42.724, 20.547, 42.724);
		shape0.curveTo(28.411001, 42.724, 34.785, 39.732, 34.785, 36.04);
		shape0.lineTo(34.785, 51.149002);
		shape0.curveTo(34.785, 54.840004, 28.411, 57.833, 20.547, 57.833);
		shape0.curveTo(12.683002, 57.833, 6.309, 54.841, 6.309, 51.149);
		shape0.lineTo(6.309, 36.04);
		shape0.closePath();
		g.setPaint(new LinearGradientPaint(new Point2D.Double(6.308599948883057, 46.936500549316406), new Point2D.Double(34.78419876098633, 46.936500549316406), new float[] {0.0f,1.0f}, new Color[] {new Color(184, 207, 229, 255),new Color(255, 255, 255, 255)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f)));
		g.fill(shape0);
	}

	private void paintShapeNode_0_0_1(Graphics2D g) {
		Ellipse2D.Double shape1 = new Ellipse2D.Double(6.307999610900879, 29.356000900268555, 28.47599983215332, 13.368000030517578);
		g.setPaint(new LinearGradientPaint(new Point2D.Double(6.308599948883057, 36.04100036621094), new Point2D.Double(34.78419876098633, 36.04100036621094), new float[] {0.0f,1.0f}, new Color[] {new Color(255, 255, 255, 255),new Color(184, 207, 229, 255)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f)));
		g.fill(shape1);
	}

	private void paintShapeNode_0_0_2(Graphics2D g) {
		GeneralPath shape2 = new GeneralPath();
		shape2.moveTo(34.784, 17.832);
		shape2.lineTo(34.784, 51.15);
		shape2.curveTo(34.784, 54.841003, 28.41, 57.834, 20.546001, 57.834);
		shape2.curveTo(12.682003, 57.834, 6.309, 54.841, 6.309, 51.149);
		shape2.lineTo(6.309, 20.145);
		g.setPaint(new Color(64, 64, 64, 255));
		g.setStroke(new BasicStroke(1.0f,0,0,4.0f,null,0.0f));
		g.draw(shape2);
	}

	private void paintShapeNode_0_0_3(Graphics2D g) {
		Ellipse2D.Double shape3 = new Ellipse2D.Double(6.307999610900879, 29.356000900268555, 28.47599983215332, 13.368000030517578);
		g.draw(shape3);
	}

	private void paintShapeNode_0_0_4(Graphics2D g) {
		GeneralPath shape4 = new GeneralPath();
		shape4.moveTo(8.292, 21.221);
		shape4.lineTo(2.047, 17.832);
		shape4.lineTo(8.292, 14.442);
		shape4.lineTo(8.287, 14.432);
		shape4.curveTo(10.764999, 12.467, 15.327, 11.148001, 20.547, 11.148001);
		shape4.curveTo(28.411001, 11.148001, 34.785, 14.140001, 34.785, 17.832);
		shape4.curveTo(34.785, 21.523, 28.411, 24.515001, 20.547, 24.515001);
		shape4.curveTo(15.329, 24.515001, 10.766001, 23.198002, 8.287001, 21.232002);
		shape4.lineTo(8.292, 21.221);
		shape4.closePath();
		g.draw(shape4);
	}

	private void paintCanvasGraphicsNode_0_0(Graphics2D g) {
		// _0_0_0
		AffineTransform trans_0_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_0(g);
		g.setTransform(trans_0_0_0);
		// _0_0_1
		AffineTransform trans_0_0_1 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_1(g);
		g.setTransform(trans_0_0_1);
		// _0_0_2
		AffineTransform trans_0_0_2 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_2(g);
		g.setTransform(trans_0_0_2);
		// _0_0_3
		AffineTransform trans_0_0_3 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_3(g);
		g.setTransform(trans_0_0_3);
		// _0_0_4
		AffineTransform trans_0_0_4 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_4(g);
		g.setTransform(trans_0_0_4);
	}

	private void paintRootGraphicsNode_0(Graphics2D g) {
		// _0_0
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
		AffineTransform trans_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -0.0f, -0.0f));
		paintCanvasGraphicsNode_0_0(g);
		g.setTransform(trans_0_0);
	}



    /**
     * Returns the X of the bounding box of the original SVG image.
     * @return The X of the bounding box of the original SVG image.
     */
    public int getOrigX() {
        return 1;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     * @return The Y of the bounding box of the original SVG image.
     */
    public int getOrigY() {
        return 11;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     * @return The width of the bounding box of the original SVG image.
     */
    public int getOrigWidth() {
        return 40;//35;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     * @return The height of the bounding box of the original SVG image.
     */
    public int getOrigHeight() {
        return 60;//48;
    }
    
    
	/**
	 * The current width of this resizable icon.
	 */
	int width;

	/**
	 * The current height of this resizable icon.
	 */
	int height;

	/**
	 * Creates a new transcoded SVG image.
	 */
	public WasteSVG() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#getIconHeight()
	 */
    @Override
	public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#getIconWidth()
	 */
    @Override
	public int getIconWidth() {
		return width;
	}

	/*
	 * Set the dimension of the icon.
	 */

	public void setDimension(Dimension newDimension) {
		this.width = newDimension.width;
		this.height = newDimension.height;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
    @Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.translate(x, y);

		double coef1 = (double) this.width / (double) getOrigWidth();
		double coef2 = (double) this.height / (double) getOrigHeight();
		double coef = Math.min(coef1, coef2);
		g2d.scale(coef, coef);
		paint(g2d);
		g2d.dispose();
	}
}

