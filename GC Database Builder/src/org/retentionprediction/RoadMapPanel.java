package org.retentionprediction;

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

import svg.IndicatorSVG;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JFrame;
import javax.swing.border.Border;

import java.awt.BorderLayout;

public class RoadMapPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JLabel jLabelStart = null;

	private JLabel jLabelRun1 = null;

	private JLabel jLabelRun2 = null;

	private JLabel jLabelRun3 = null;

	private JLabel jLabelRun4 = null;

	private JLabel jLabelRun5 = null;

	private JLabel jLabelRun6 = null;

	private JLabel jLabelBackCalculate1 = null;

	private JLabel jLabelBackCalculate2 = null;

	private JLabel jLabelBackCalculate3 = null;

	private JLabel jLabelBackCalculate4 = null;

	private JLabel jLabelBackCalculate5 = null;

	private JLabel jLabelBackCalculate6 = null;

	private JLabel jLabelSuitability1 = null;

	private JLabel jLabelSuitability2 = null;

	private JLabel jLabelSuitability3 = null;

	private JLabel jLabelSuitability4 = null;

	private JLabel jLabelSuitability5 = null;

	private JLabel jLabelSuitability6 = null;

	private JLabel jLabelFinal = null;

	private JLabel jLabelMeasuredTimes1 = null;

	private JLabel jLabelMeasuredTimes2 = null;

	private JLabel jLabelMeasuredTimes3 = null;

	private JLabel jLabelMeasuredTimes4 = null;

	private JLabel jLabelMeasuredTimes5 = null;

	private JLabel jLabelMeasuredTimes6 = null;

	/**
	 * This is the default constructor
	 */
	public RoadMapPanel() 
	{
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jLabelMeasuredTimes6 = new JLabel();
		jLabelMeasuredTimes6.setBounds(new Rectangle(164, 108, 121, 16));
		jLabelMeasuredTimes6.setText("Enter retention times");
		jLabelMeasuredTimes6.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelMeasuredTimes6.setSize(jLabelMeasuredTimes6.getPreferredSize());
		jLabelMeasuredTimes5 = new JLabel();
		jLabelMeasuredTimes5.setBounds(new Rectangle(164, 88, 121, 16));
		jLabelMeasuredTimes5.setText("Enter retention times");
		jLabelMeasuredTimes5.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelMeasuredTimes5.setSize(jLabelMeasuredTimes5.getPreferredSize());
		jLabelMeasuredTimes4 = new JLabel();
		jLabelMeasuredTimes4.setBounds(new Rectangle(164, 68, 121, 16));
		jLabelMeasuredTimes4.setText("Enter retention times");
		jLabelMeasuredTimes4.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelMeasuredTimes4.setSize(jLabelMeasuredTimes4.getPreferredSize());
		jLabelMeasuredTimes3 = new JLabel();
		jLabelMeasuredTimes3.setBounds(new Rectangle(164, 48, 121, 16));
		jLabelMeasuredTimes3.setText("Enter retention times");
		jLabelMeasuredTimes3.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelMeasuredTimes3.setSize(jLabelMeasuredTimes3.getPreferredSize());
		jLabelMeasuredTimes2 = new JLabel();
		jLabelMeasuredTimes2.setBounds(new Rectangle(164, 28, 121, 16));
		jLabelMeasuredTimes2.setText("Enter retention times");
		jLabelMeasuredTimes2.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelMeasuredTimes2.setSize(jLabelMeasuredTimes2.getPreferredSize());
		jLabelMeasuredTimes1 = new JLabel();
		jLabelMeasuredTimes1.setBounds(new Rectangle(164, 8, 121, 16));
		jLabelMeasuredTimes1.setText("Enter retention times");
		jLabelMeasuredTimes1.setHorizontalAlignment(SwingConstants.LEFT);
		jLabelMeasuredTimes1.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelMeasuredTimes1.setSize(jLabelMeasuredTimes1.getPreferredSize());
		jLabelStart = new JLabel();
		jLabelStart.setBounds(new Rectangle(4, 52, 37, 16));
		jLabelStart.setName("");
		jLabelStart.setText("Start");
		jLabelStart.setHorizontalTextPosition(SwingConstants.LEADING);
		jLabelStart.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelStart.setFont(new Font("Dialog", Font.BOLD, 12));
		this.setSize(842, 133);
		this.setLayout(null);
		
		jLabelFinal = new JLabel();
		jLabelFinal.setText("Final fit");
		jLabelFinal.setBounds(new Rectangle(728, 56, 45, 16));
		jLabelFinal.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelSuitability6 = new JLabel();
		jLabelSuitability6.setText("Check system suitability");
		jLabelSuitability6.setBounds(new Rectangle(528, 108, 137, 16));
		jLabelSuitability6.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelSuitability6.setSize(jLabelSuitability6.getPreferredSize());
		jLabelSuitability5 = new JLabel();
		jLabelSuitability5.setText("Check system suitability");
		jLabelSuitability5.setBounds(new Rectangle(528, 88, 137, 16));
		jLabelSuitability5.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelSuitability5.setSize(jLabelSuitability5.getPreferredSize());
		jLabelSuitability4 = new JLabel();
		jLabelSuitability4.setText("Check system suitability");
		jLabelSuitability4.setBounds(new Rectangle(528, 68, 137, 16));
		jLabelSuitability4.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelSuitability4.setSize(jLabelSuitability4.getPreferredSize());
		jLabelSuitability3 = new JLabel();
		jLabelSuitability3.setText("Check system suitability");
		jLabelSuitability3.setBounds(new Rectangle(528, 48, 137, 16));
		jLabelSuitability3.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelSuitability3.setSize(jLabelSuitability3.getPreferredSize());
		jLabelSuitability2 = new JLabel();
		jLabelSuitability2.setText("Check system suitability");
		jLabelSuitability2.setBounds(new Rectangle(528, 28, 137, 16));
		jLabelSuitability2.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelSuitability2.setSize(jLabelSuitability2.getPreferredSize());
		jLabelSuitability1 = new JLabel();
		jLabelSuitability1.setText("Check system suitability");
		jLabelSuitability1.setBounds(new Rectangle(528, 8, 137, 16));
		jLabelSuitability1.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelSuitability1.setSize(jLabelSuitability1.getPreferredSize());
		jLabelBackCalculate6 = new JLabel();
		jLabelBackCalculate6.setText("Back-calculate profiles");
		jLabelBackCalculate6.setBounds(new Rectangle(340, 108, 129, 16));
		jLabelBackCalculate6.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelBackCalculate6.setSize(jLabelBackCalculate6.getPreferredSize());
		jLabelBackCalculate5 = new JLabel();
		jLabelBackCalculate5.setText("Back-calculate profiles");
		jLabelBackCalculate5.setBounds(new Rectangle(340, 88, 129, 16));
		jLabelBackCalculate5.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelBackCalculate5.setSize(jLabelBackCalculate5.getPreferredSize());
		jLabelBackCalculate4 = new JLabel();
		jLabelBackCalculate4.setText("Back-calculate profiles");
		jLabelBackCalculate4.setBounds(new Rectangle(340, 68, 129, 16));
		jLabelBackCalculate4.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelBackCalculate4.setSize(jLabelBackCalculate4.getPreferredSize());
		jLabelBackCalculate3 = new JLabel();
		jLabelBackCalculate3.setText("Back-calculate profiles");
		jLabelBackCalculate3.setBounds(new Rectangle(340, 48, 129, 16));
		jLabelBackCalculate3.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelBackCalculate3.setSize(jLabelBackCalculate3.getPreferredSize());
		jLabelBackCalculate2 = new JLabel();
		jLabelBackCalculate2.setText("Back-calculate profiles");
		jLabelBackCalculate2.setBounds(new Rectangle(340, 28, 129, 16));
		jLabelBackCalculate2.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelBackCalculate2.setSize(jLabelBackCalculate2.getPreferredSize());
		jLabelBackCalculate1 = new JLabel();
		jLabelBackCalculate1.setText("Back-calculate profiles");
		jLabelBackCalculate1.setBounds(new Rectangle(340, 8, 129, 16));
		jLabelBackCalculate1.setFont(new Font("Dialog", Font.PLAIN, 12));
		jLabelBackCalculate1.setSize(jLabelBackCalculate1.getPreferredSize());
		jLabelRun6 = new JLabel();
		jLabelRun6.setText("Program F");
		jLabelRun6.setBounds(new Rectangle(80, 100, 69, 14));
		jLabelRun6.setFont(new Font("Dialog", Font.BOLD, 10));
		jLabelRun5 = new JLabel();
		jLabelRun5.setText("Program E");
		jLabelRun5.setBounds(new Rectangle(80, 80, 69, 14));
		jLabelRun5.setFont(new Font("Dialog", Font.BOLD, 10));
		jLabelRun4 = new JLabel();
		jLabelRun4.setText("Program D");
		jLabelRun4.setBounds(new Rectangle(80, 60, 69, 16));
		jLabelRun4.setFont(new Font("Dialog", Font.BOLD, 10));
		jLabelRun3 = new JLabel();
		jLabelRun3.setText("Program C");
		jLabelRun3.setBounds(new Rectangle(80, 40, 69, 16));
		jLabelRun3.setFont(new Font("Dialog", Font.BOLD, 10));
		jLabelRun2 = new JLabel();
		jLabelRun2.setText("Program B");
		jLabelRun2.setBounds(new Rectangle(80, 20, 69, 16));
		jLabelRun2.setFont(new Font("Dialog", Font.BOLD, 10));
		jLabelRun1 = new JLabel();
		jLabelRun1.setText("Program A");
		jLabelRun1.setBounds(new Rectangle(80, 0, 69, 16));
		jLabelRun1.setFont(new Font("Dialog", Font.BOLD, 10));
		
		this.add(jLabelStart, null);
		this.add(jLabelRun1, null);
		this.add(jLabelRun2, null);
		this.add(jLabelRun3, null);
		this.add(jLabelRun4, null);
		this.add(jLabelRun5, null);
		this.add(jLabelRun6, null);
		this.add(jLabelBackCalculate1, null);
		this.add(jLabelBackCalculate2, null);
		this.add(jLabelBackCalculate3, null);
		this.add(jLabelBackCalculate4, null);
		this.add(jLabelBackCalculate5, null);
		this.add(jLabelBackCalculate6, null);
		this.add(jLabelSuitability1, null);
		this.add(jLabelSuitability2, null);
		this.add(jLabelSuitability3, null);
		this.add(jLabelSuitability4, null);
		this.add(jLabelSuitability5, null);
		this.add(jLabelSuitability6, null);
		this.add(jLabelFinal, null);
		this.add(jLabelMeasuredTimes1, null);
		this.add(jLabelMeasuredTimes2, null);
		this.add(jLabelMeasuredTimes3, null);
		this.add(jLabelMeasuredTimes4, null);
		this.add(jLabelMeasuredTimes5, null);
		this.add(jLabelMeasuredTimes6, null);
		
	}
	
	
	@Override
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		//g2d.setColor(this.getBackground());
		//g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Color clrBlack = new Color(51, 51, 51);
		
		// Draw first arrows
		g2d.setColor(clrBlack);
		int x1 = this.jLabelStart.getX() + this.jLabelStart.getWidth() + 4;
		int y1 = this.jLabelStart.getY() + (this.jLabelStart.getHeight() / 2);
		int x2 = (((this.jLabelRun1.getX() - x1) * 2) / 3) + x1;
		int y2 = this.jLabelBackCalculate1.getY() + (this.jLabelMeasuredTimes1.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x2, y2, jLabelMeasuredTimes1.getX() - 4, y2);
		g2d.drawPolygon(new int[]{jLabelMeasuredTimes1.getX() - 4, jLabelMeasuredTimes1.getX() - 8, jLabelMeasuredTimes1.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{jLabelMeasuredTimes1.getX() - 4, jLabelMeasuredTimes1.getX() - 8, jLabelMeasuredTimes1.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelStart.getX() + this.jLabelStart.getWidth() + 4;
		y1 = this.jLabelStart.getY() + (this.jLabelStart.getHeight() / 2);
		x2 = (((this.jLabelRun2.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelMeasuredTimes2.getY() + (this.jLabelMeasuredTimes2.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x2, y2, jLabelMeasuredTimes2.getX() - 4, y2);
		g2d.drawPolygon(new int[]{jLabelMeasuredTimes2.getX() - 4, jLabelMeasuredTimes2.getX() - 8, jLabelMeasuredTimes2.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{jLabelMeasuredTimes2.getX() - 4, jLabelMeasuredTimes2.getX() - 8, jLabelMeasuredTimes2.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelStart.getX() + this.jLabelStart.getWidth() + 4;
		y1 = this.jLabelStart.getY() + (this.jLabelStart.getHeight() / 2);
		x2 = (((this.jLabelRun3.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelMeasuredTimes3.getY() + (this.jLabelMeasuredTimes3.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x2, y2, jLabelMeasuredTimes3.getX() - 4, y2);
		g2d.drawPolygon(new int[]{jLabelMeasuredTimes3.getX() - 4, jLabelMeasuredTimes3.getX() - 8, jLabelMeasuredTimes3.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{jLabelMeasuredTimes3.getX() - 4, jLabelMeasuredTimes3.getX() - 8, jLabelMeasuredTimes3.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelStart.getX() + this.jLabelStart.getWidth() + 4;
		y1 = this.jLabelStart.getY() + (this.jLabelStart.getHeight() / 2);
		x2 = (((this.jLabelRun4.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelMeasuredTimes4.getY() + (this.jLabelMeasuredTimes4.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x2, y2, jLabelMeasuredTimes4.getX() - 4, y2);
		g2d.drawPolygon(new int[]{jLabelMeasuredTimes4.getX() - 4, jLabelMeasuredTimes4.getX() - 8, jLabelMeasuredTimes4.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{jLabelMeasuredTimes4.getX() - 4, jLabelMeasuredTimes4.getX() - 8, jLabelMeasuredTimes4.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelStart.getX() + this.jLabelStart.getWidth() + 4;
		y1 = this.jLabelStart.getY() + (this.jLabelStart.getHeight() / 2);
		x2 = (((this.jLabelRun5.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelMeasuredTimes5.getY() + (this.jLabelMeasuredTimes5.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x2, y2, jLabelMeasuredTimes5.getX() - 4, y2);
		g2d.drawPolygon(new int[]{jLabelMeasuredTimes5.getX() - 4, jLabelMeasuredTimes5.getX() - 8, jLabelMeasuredTimes5.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{jLabelMeasuredTimes5.getX() - 4, jLabelMeasuredTimes5.getX() - 8, jLabelMeasuredTimes5.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelStart.getX() + this.jLabelStart.getWidth() + 4;
		y1 = this.jLabelStart.getY() + (this.jLabelStart.getHeight() / 2);
		x2 = (((this.jLabelRun6.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelMeasuredTimes6.getY() + (this.jLabelMeasuredTimes6.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawLine(x2, y2, jLabelMeasuredTimes6.getX() - 4, y2);
		g2d.drawPolygon(new int[]{jLabelMeasuredTimes6.getX() - 4, jLabelMeasuredTimes6.getX() - 8, jLabelMeasuredTimes6.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{jLabelMeasuredTimes6.getX() - 4, jLabelMeasuredTimes6.getX() - 8, jLabelMeasuredTimes6.getX() - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		// Draw second arrows
		x1 = this.jLabelMeasuredTimes1.getX() + this.jLabelMeasuredTimes1.getWidth() + 4;
		y1 = this.jLabelMeasuredTimes1.getY() + (this.jLabelMeasuredTimes1.getHeight() / 2);
		x2 = this.jLabelBackCalculate1.getX() - 4;
		y2 = this.jLabelBackCalculate1.getY() + (this.jLabelBackCalculate1.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelMeasuredTimes2.getX() + this.jLabelMeasuredTimes2.getWidth() + 4;
		y1 = this.jLabelMeasuredTimes2.getY() + (this.jLabelMeasuredTimes2.getHeight() / 2);
		x2 = this.jLabelBackCalculate2.getX() - 4;
		y2 = this.jLabelBackCalculate2.getY() + (this.jLabelBackCalculate2.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelMeasuredTimes3.getX() + this.jLabelMeasuredTimes3.getWidth() + 4;
		y1 = this.jLabelMeasuredTimes3.getY() + (this.jLabelMeasuredTimes3.getHeight() / 2);
		x2 = this.jLabelBackCalculate3.getX() - 4;
		y2 = this.jLabelBackCalculate3.getY() + (this.jLabelBackCalculate3.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelMeasuredTimes4.getX() + this.jLabelMeasuredTimes4.getWidth() + 4;
		y1 = this.jLabelMeasuredTimes4.getY() + (this.jLabelMeasuredTimes4.getHeight() / 2);
		x2 = this.jLabelBackCalculate4.getX() - 4;
		y2 = this.jLabelBackCalculate4.getY() + (this.jLabelBackCalculate4.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelMeasuredTimes5.getX() + this.jLabelMeasuredTimes5.getWidth() + 4;
		y1 = this.jLabelMeasuredTimes5.getY() + (this.jLabelMeasuredTimes5.getHeight() / 2);
		x2 = this.jLabelBackCalculate5.getX() - 4;
		y2 = this.jLabelBackCalculate5.getY() + (this.jLabelBackCalculate5.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelMeasuredTimes6.getX() + this.jLabelMeasuredTimes6.getWidth() + 4;
		y1 = this.jLabelMeasuredTimes6.getY() + (this.jLabelMeasuredTimes6.getHeight() / 2);
		x2 = this.jLabelBackCalculate6.getX() - 4;
		y2 = this.jLabelBackCalculate6.getY() + (this.jLabelBackCalculate6.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		// Draw third arrows
		x1 = this.jLabelBackCalculate1.getX() + this.jLabelBackCalculate1.getWidth() + 4;
		y1 = this.jLabelBackCalculate1.getY() + (this.jLabelBackCalculate1.getHeight() / 2);
		x2 = this.jLabelSuitability1.getX() - 4;
		y2 = this.jLabelSuitability1.getY() + (this.jLabelSuitability1.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		
		x1 = this.jLabelBackCalculate2.getX() + this.jLabelBackCalculate2.getWidth() + 4;
		y1 = this.jLabelBackCalculate2.getY() + (this.jLabelBackCalculate2.getHeight() / 2);
		x2 = this.jLabelSuitability2.getX() - 4;
		y2 = this.jLabelSuitability2.getY() + (this.jLabelSuitability2.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelBackCalculate3.getX() + this.jLabelBackCalculate3.getWidth() + 4;
		y1 = this.jLabelBackCalculate3.getY() + (this.jLabelBackCalculate3.getHeight() / 2);
		x2 = this.jLabelSuitability3.getX() - 4;
		y2 = this.jLabelSuitability3.getY() + (this.jLabelSuitability3.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelBackCalculate4.getX() + this.jLabelBackCalculate4.getWidth() + 4;
		y1 = this.jLabelBackCalculate4.getY() + (this.jLabelBackCalculate4.getHeight() / 2);
		x2 = this.jLabelSuitability4.getX() - 4;
		y2 = this.jLabelSuitability4.getY() + (this.jLabelSuitability4.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelBackCalculate5.getX() + this.jLabelBackCalculate5.getWidth() + 4;
		y1 = this.jLabelBackCalculate5.getY() + (this.jLabelBackCalculate5.getHeight() / 2);
		x2 = this.jLabelSuitability5.getX() - 4;
		y2 = this.jLabelSuitability5.getY() + (this.jLabelSuitability5.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		x1 = this.jLabelBackCalculate6.getX() + this.jLabelBackCalculate6.getWidth() + 4;
		y1 = this.jLabelBackCalculate6.getY() + (this.jLabelBackCalculate6.getHeight() / 2);
		x2 = this.jLabelSuitability6.getX() - 4;
		y2 = this.jLabelSuitability6.getY() + (this.jLabelSuitability6.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);

		// Draw fourth lines
		x1 = this.jLabelSuitability1.getX() + this.jLabelSuitability1.getWidth() + 4;
		y1 = this.jLabelSuitability1.getY() + (this.jLabelSuitability1.getHeight() / 2);
		x2 = (((this.jLabelFinal.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelFinal.getY() + (this.jLabelFinal.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);

		x1 = this.jLabelSuitability2.getX() + this.jLabelSuitability2.getWidth() + 4;
		y1 = this.jLabelSuitability2.getY() + (this.jLabelSuitability2.getHeight() / 2);
		x2 = (((this.jLabelFinal.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelFinal.getY() + (this.jLabelFinal.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);

		x1 = this.jLabelSuitability3.getX() + this.jLabelSuitability3.getWidth() + 4;
		y1 = this.jLabelSuitability3.getY() + (this.jLabelSuitability3.getHeight() / 2);
		x2 = (((this.jLabelFinal.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelFinal.getY() + (this.jLabelFinal.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);

		x1 = this.jLabelSuitability4.getX() + this.jLabelSuitability4.getWidth() + 4;
		y1 = this.jLabelSuitability4.getY() + (this.jLabelSuitability4.getHeight() / 2);
		x2 = (((this.jLabelFinal.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelFinal.getY() + (this.jLabelFinal.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);

		x1 = this.jLabelSuitability5.getX() + this.jLabelSuitability5.getWidth() + 4;
		y1 = this.jLabelSuitability5.getY() + (this.jLabelSuitability5.getHeight() / 2);
		x2 = (((this.jLabelFinal.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelFinal.getY() + (this.jLabelFinal.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);

		x1 = this.jLabelSuitability6.getX() + this.jLabelSuitability6.getWidth() + 4;
		y1 = this.jLabelSuitability6.getY() + (this.jLabelSuitability6.getHeight() / 2);
		x2 = (((this.jLabelFinal.getX() - x1) * 2) / 3) + x1;
		y2 = this.jLabelFinal.getY() + (this.jLabelFinal.getHeight() / 2);
		g2d.drawLine(x1, y1, x2, y2);

		x1 = x2;
		y1 = y2;
		x2 = jLabelFinal.getX() - 4;
		y2 = y1;
		g2d.drawLine(x1, y1, x2, y2);
		g2d.drawPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		g2d.fillPolygon(new int[]{x2, x2 - 4, x2 - 4}, new int[]{y2, y2 - 4, y2 + 4}, 3);
		
		g2d.setColor(Color.BLACK);
	}



}  //  @jve:decl-index=0:visual-constraint="10,10"
