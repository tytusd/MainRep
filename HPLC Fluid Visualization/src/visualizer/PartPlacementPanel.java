package visualizer;
import javax.swing.JPanel;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;

import dialogs.TubePropertiesDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class PartPlacementPanel extends JPanel implements Scrollable, ComponentListener, MouseMotionListener, MouseListener
{
	private static final long serialVersionUID = 1L;
	
	static final public int SELECT_TOOL = 0;
	static final public int EDIT_TOOL = 1;
	static final public int REMOVE_TOOL = 2;
	static final public int TUBE_4_MIL_TOOL = 3;
	static final public int TUBE_5_MIL_TOOL = 4;
	static final public int TUBE_7_MIL_TOOL = 5;
	static final public int TUBE_10_MIL_TOOL = 6;
	static final public int TUBE_20_MIL_TOOL = 7;
	static final public int TUBE_30_MIL_TOOL = 8;
	static final public int TUBE_OTHER_TOOL = 9;
	
	private int iSelectedTool = SELECT_TOOL;
	
	public double m_dZoomFactor = 1;
	public boolean m_bShowGrid = true;
	
	public Point m_originPoint = new Point(0, 0);  //  @jve:decl-index=0:
	public Vector<HPLCPart> m_vectHPLCParts = new Vector<HPLCPart>();  //  @jve:decl-index=0:
	public HPLCPart m_selectedPart = null;
    private ArrayList<PartPlacementPanelListener> m_addPartListeners = new ArrayList<PartPlacementPanelListener>();
    private ArrayList<PartPlacementPanelListener> m_documentChangedListeners = new ArrayList<PartPlacementPanelListener>();
    
    Cursor m_curClockwise;
    Cursor m_curCounterClockwise;
    Cursor m_curOpenHand;
    Cursor m_curClosedHand;
    Cursor m_curPointHand;
    
    // For dragging parts
    private HPLCPart m_dragPart;
    private Point m_dragStartMouseDragPoint;
    private Point m_dragStartPartCell;
    
    // For adding tubing
    private ConnectionNode m_addTubeStartNode = null;
    private HPLCPartTubeJoint m_cursorTubeJoint = null;
    private ConnectionNode m_addTubeEndNode = null;
    
    private ConnectionNode m_editNode = null;
	/**
	 * This is the default constructor
	 */
	public PartPlacementPanel() 
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
		this.setLayout(null);
		
		this.setVisible(true);
		this.setSize(new Dimension(5000,5000));
		this.setBackground(Color.white);
        this.setPreferredSize(new Dimension(5000,5000));
        this.setMinimumSize(new Dimension(5000,5000));

		this.addComponentListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		
		Action actionEscape = new AbstractAction() 
		{
		    public void actionPerformed(ActionEvent e) 
		    {
		    	if (iSelectedTool >= TUBE_4_MIL_TOOL && iSelectedTool <= TUBE_OTHER_TOOL)
				{
					if (m_addTubeStartNode != null)
					{
						if (m_addTubeEndNode != null)
							m_addTubeEndNode.removeConnection(m_addTubeStartNode);
						
						m_addTubeStartNode.removeConnection(m_addTubeEndNode);
						m_addTubeEndNode = null;
						m_cursorTubeJoint = null;
						m_addTubeStartNode = null;
						repaint();
					}
				}
		    }
		};
		
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "EscPressed");
		this.getActionMap().put("EscPressed", actionEscape);

        // Center the origin 
        m_originPoint.x = this.getWidth() / 2;
        m_originPoint.y = this.getHeight() / 2;
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image imgClockwise = toolkit.getImage(getClass().getResource("/images/clockwise.gif"));
        m_curClockwise = toolkit.createCustomCursor(imgClockwise, new Point(7,7), "clockwise");
        Image imgCounterClockwise = toolkit.getImage(getClass().getResource("/images/counterclockwise.gif"));
        m_curCounterClockwise = toolkit.createCustomCursor(imgCounterClockwise, new Point(7,7), "counterclockwise");
        Image imgOpenHand = toolkit.getImage(getClass().getResource("/images/openhand.gif"));
        m_curOpenHand = toolkit.createCustomCursor(imgOpenHand, new Point(7,7), "openhand");
        Image imgClosedHand = toolkit.getImage(getClass().getResource("/images/closedhand.gif"));
        m_curClosedHand = toolkit.createCustomCursor(imgClosedHand, new Point(7,7), "closedhand");
        Image imgPointHand = toolkit.getImage(getClass().getResource("/images/pointerhand.gif"));
        m_curPointHand = toolkit.createCustomCursor(imgPointHand, new Point(5,0), "pointhand");
	}

	public void setHPLCParts(Vector<HPLCPart> HPLCParts)
	{
		//this.m_vectHPLCParts.removeAllElements();
		if (HPLCParts == null)
		{
			Vector<HPLCPart> newList = new Vector<HPLCPart>();
			m_vectHPLCParts = newList;
		}
		else
		{
			this.m_vectHPLCParts = HPLCParts;
		}
		
		this.repaint();
	}
	
	public Vector<HPLCPart> getHPLCParts()
	{
		return this.m_vectHPLCParts;
	}
	
	public void setSelectedHPLCPart(HPLCPart selectedPart)
	{
		this.m_selectedPart = selectedPart;
	}
	
	public void setSelectedTool(int iSelectedTool)
	{
		this.iSelectedTool = iSelectedTool;
		
		if (iSelectedTool != SELECT_TOOL)
		{
			this.m_selectedPart = null;
		}
		
		if (m_addTubeStartNode != null)
		{
			if (m_addTubeEndNode != null)
				m_addTubeEndNode.removeConnection(m_addTubeStartNode);
			
			m_addTubeStartNode.removeConnection(m_addTubeEndNode);
			m_addTubeEndNode = null;
			m_cursorTubeJoint = null;
			m_addTubeStartNode = null;
			repaint();
		}
	}
	
	public void updateFluidColors()
	{
		// Turn ALL nodes to white fluid color
		for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
		{
			HPLCPart thisPart = this.m_vectHPLCParts.get(i);
			
			for (int j = 0; j < thisPart.getConnectionNodes().size(); j++)
			{
				ConnectionNode thisNode = thisPart.getConnectionNodes().get(j);
				
				thisNode.setFluidColor(Color.WHITE);
			}
		}
		
		// Run through all the HPLCParts looking for pumps and autosamplers
		for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
		{
			HPLCPart thisPart = this.m_vectHPLCParts.get(i);
			
			if (thisPart instanceof HPLCPartPump || thisPart instanceof HPLCPartAutosampler)
			{
				Color fluidColor = thisPart.getFluidColor();
				
				// Get the pump node
				ConnectionNode thisNode = thisPart.getConnectionNodes().get(0);

				boolean bPartConnects = true;
				
				while (bPartConnects)
				{
					bPartConnects = false;
					
					thisNode.setFluidColor(fluidColor);
					
					// Check if it is connected. If not, skip it.
					if (thisNode.getConnectedNodes()[0] == null && thisNode.getConnectedNodes()[1] == null)
						break;
					
					// Get the next node
					ConnectionNode nextNode;
					if (thisNode.getConnectedNodes()[0] == null)
						nextNode = thisNode.getConnectedNodes()[1];
					else
						nextNode = thisNode.getConnectedNodes()[0];
					
					// Propagate the dLength and dInnerDiameter parameters down the line
					while (nextNode.getNumConnections() > 1)
					{
						nextNode.setFluidColor(fluidColor);
			
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
					
					nextNode.setFluidColor(fluidColor);
					
					thisPart = nextNode.getParentHPLCPart();
					
					if (thisPart == null)
						break;
					
					thisNode = thisPart.getInternalConnection(nextNode);
					
					if (thisNode != null)
						bPartConnects = true;
				}
			}
		}
	}
	
	@Override
	public void paint(Graphics g) 
	{
		// TODO Auto-generated method stub
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		updateFluidColors();
		if (m_bShowGrid)
			drawGrid(g2d);
		drawParts(g2d, false);
		drawNewPartCursor(g2d);
		updateAndDrawNewTubeCursor(g2d, false, false);
		drawParts(g2d, true);
		drawTubeConnections(g2d);
		updateAndDrawNewTubeCursor(g2d, true, false);
		drawRemovePart(g2d);
		drawEditPart(g2d);
		
		g2d.setColor(Color.BLACK);
	}

	public void drawTubeConnections(Graphics2D g2d)
	{
		Vector<ConnectionNode> finishedList = new Vector<ConnectionNode>();
		
		for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
		{
			HPLCPart thisPart = m_vectHPLCParts.get(i);
			
			for (int j = 0; j < thisPart.getConnectionNodes().size(); j++)
			{
				ConnectionNode thisNode = thisPart.getConnectionNodes().get(j);
				
				boolean bIsCurrentPath = false;
				boolean bIsEditPath = false;
				
				if (thisNode == this.m_addTubeStartNode)
					bIsCurrentPath = true;
				
				if (thisNode == this.m_editNode)
					bIsEditPath = true;

				// Look for the ends of the tube
				if (thisNode.getNumConnections() != 1)
					continue;
				
				// Check to see if this node is in the finished list
				if(finishedList.contains(thisNode))
					continue;

				ConnectionNode[] connectedNodes = thisNode.getConnectedNodes();
				
				ConnectionNode nextNode;
				// Find the connected node
				if (connectedNodes[0] != null)
					nextNode = connectedNodes[0];
				else
					nextNode = connectedNodes[1];

				if (nextNode == this.m_addTubeStartNode)
					bIsCurrentPath = true;
				if (nextNode == this.m_editNode)
					bIsEditPath = true;

				// Draw the start connection
				Point thisPartPoint = this.getCornerOfCell(thisNode.getParentHPLCPart().getLocation());
				Point thisNodeLocationOnPart = thisNode.getLocation();
				Point thisNodeLocation = new Point(thisPartPoint.x + thisNodeLocationOnPart.x, thisPartPoint.y + thisNodeLocationOnPart.y);
				
				Point nextPartPoint = this.getCornerOfCell(nextNode.getParentHPLCPart().getLocation());
				Point nextNodeLocationOnPart = nextNode.getLocation();
				Point nextNodeLocation = new Point(nextPartPoint.x + nextNodeLocationOnPart.x, nextPartPoint.y + nextNodeLocationOnPart.y);

				// Find the longest length segment of this tube
				Point pointLongest1 = thisNodeLocation;
				Point pointLongest2 = nextNodeLocation;
				double dLongestLength = thisNodeLocation.distance(nextNodeLocation);

				// Pull back the lines a bit
				// Find angle of the line
				double dAngle = Math.atan((double)(nextNodeLocation.y - thisNodeLocation.y) / (double)(nextNodeLocation.x - thisNodeLocation.x));
				dAngle += Math.PI / 2;
				if (nextNodeLocation.x < thisNodeLocation.x)
					dAngle = dAngle + Math.PI;

				// Now find offset of the next location
				double dyOffset = -Math.cos(dAngle) * (double)(Globals.tubeEndRadius);
				double dxOffset = Math.sin(dAngle) * (double)(Globals.tubeEndRadius);
				
				Point nextNodeLocationOffset = new Point(nextNodeLocation.x - (int)Math.round(dxOffset), nextNodeLocation.y - (int)Math.round(dyOffset));

				// Now find the bezier offset of the next location
				double dyBezierOffset = -Math.cos(dAngle) * (double)4;
				double dxBezierOffset = Math.sin(dAngle) * (double)4;

				Point nextBezierLocationOffset = new Point(nextNodeLocation.x - (int)Math.round(dxBezierOffset), nextNodeLocation.y - (int)Math.round(dyBezierOffset));

				AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
				g2d.setComposite(myAlpha);
				
				// Outside circle
				g2d.setColor(Color.BLACK);
				
				Path2D.Double tubePath = new Path2D.Double();
				tubePath.moveTo(thisNodeLocation.x, thisNodeLocation.y);
				
				finishedList.add(thisNode);

				while(nextNode.getNumConnections() == 2)
				{
					ConnectionNode previousNode = thisNode;
					thisNode = nextNode;
					connectedNodes = thisNode.getConnectedNodes();

					thisNodeLocation = nextNodeLocation;
					Point thisNodeLocationOffset1 = nextNodeLocationOffset;
					Point thisBezierLocationOffset1 = nextBezierLocationOffset;

					if (connectedNodes[0] == previousNode)
						nextNode = connectedNodes[1];
					else
						nextNode = connectedNodes[0];

					if (nextNode == this.m_addTubeStartNode)
						bIsCurrentPath = true;
					if (nextNode == this.m_editNode)
						bIsEditPath = true;

					nextPartPoint = this.getCornerOfCell(nextNode.getParentHPLCPart().getLocation());
					nextNodeLocationOnPart = nextNode.getLocation();
					nextNodeLocation = new Point(nextPartPoint.x + nextNodeLocationOnPart.x, nextPartPoint.y + nextNodeLocationOnPart.y);
					
					double dLength = thisNodeLocation.distance(nextNodeLocation);
					if (dLength > dLongestLength)
					{
						pointLongest1 = thisNodeLocation;
						pointLongest2 = nextNodeLocation;
						dLongestLength = dLength;
					}
					
					// Pull back the lines a bit
					// Find angle of the line
					dAngle = Math.atan((double)(nextNodeLocation.y - thisNodeLocation.y) / (double)(nextNodeLocation.x - thisNodeLocation.x));
					dAngle += Math.PI / 2;
					if (nextNodeLocation.x < thisNodeLocation.x)
						dAngle = dAngle + Math.PI;

					// Now find x offset of thisPartPoint
					dyOffset = -Math.cos(dAngle) * (double)(Globals.tubeEndRadius);
					dxOffset = Math.sin(dAngle) * (double)(Globals.tubeEndRadius);
					
					Point thisNodeLocationOffset2 = new Point(thisNodeLocation.x + (int)Math.round(dxOffset), thisNodeLocation.y + (int)Math.round(dyOffset));
					nextNodeLocationOffset = new Point(nextNodeLocation.x - (int)Math.round(dxOffset), nextNodeLocation.y - (int)Math.round(dyOffset));

					// Now find the bezier offsets
					dyBezierOffset = -Math.cos(dAngle) * (double)4;
					dxBezierOffset = Math.sin(dAngle) * (double)4;

					Point thisBezierLocationOffset2 = new Point(thisNodeLocation.x + (int)Math.round(dxBezierOffset), thisNodeLocation.y + (int)Math.round(dyBezierOffset));
					nextBezierLocationOffset = new Point(nextNodeLocation.x - (int)Math.round(dxBezierOffset), nextNodeLocation.y - (int)Math.round(dyBezierOffset));

					// Draw tube to first location
					tubePath.lineTo(thisNodeLocationOffset1.x, thisNodeLocationOffset1.y);

					// Draw bezier curve
					tubePath.curveTo(thisBezierLocationOffset1.x, thisBezierLocationOffset1.y, thisBezierLocationOffset2.x, thisBezierLocationOffset2.y, thisNodeLocationOffset2.x, thisNodeLocationOffset2.y);
					
					finishedList.add(thisNode);
				}
				
				thisNode = nextNode;
				thisNodeLocation = nextNodeLocation;
				
				tubePath.lineTo(thisNodeLocation.x, thisNodeLocation.y);

				// Finally, draw the path
				g2d.setStroke(new BasicStroke((float)Globals.tubeDiameter, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				
				Color tubeColor;
				double dTubeLength;
				double dTubeVolume;
				boolean bTubeVolumeVisible;
				
				
				if (bIsCurrentPath)
				{
					tubeColor = colorFromInnerDiameter(this.m_addTubeStartNode.getInnerDiameter());
					dTubeLength = this.m_addTubeStartNode.getLength();
					dTubeVolume = this.m_addTubeStartNode.getVolume();
					bTubeVolumeVisible = this.m_addTubeStartNode.getVolumeVisible();
				}
				else
				{
					tubeColor = colorFromInnerDiameter(thisNode.getInnerDiameter());
					dTubeLength = thisNode.getLength();
					dTubeVolume = thisNode.getVolume();
					bTubeVolumeVisible = thisNode.getVolumeVisible();
				}

				g2d.setColor(tubeColor);
				myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
				g2d.setComposite(myAlpha);
				g2d.draw(tubePath);
				g2d.setStroke(new BasicStroke((float)Globals.tubeDiameter - 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2d.setColor(thisNode.getFluidColor());
				myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
				g2d.setComposite(myAlpha);
				g2d.draw(tubePath);
				
				if (bIsEditPath)
				{
					g2d.setColor(Color.GREEN);
					g2d.setStroke(new BasicStroke((float)Globals.tubeDiameter, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
					g2d.setComposite(myAlpha);
					g2d.draw(tubePath);					
				}
				
				myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
				g2d.setComposite(myAlpha);
				g2d.setStroke(new BasicStroke(1));

				finishedList.add(thisNode);
				
				// Draw the tube information under the longest length of tubing
				if (pointLongest1.x > pointLongest2.x)
				{
					Point tempPoint = pointLongest1;
					pointLongest1 = pointLongest2;
					pointLongest2 = tempPoint;
				}
				
				String str;
				NumberFormat formatter = new DecimalFormat("#0.###");
				if (!bTubeVolumeVisible)
					str = formatter.format(dTubeLength) + " cm";
				else
					str = formatter.format(dTubeVolume) + " \u00b5L";
				Font arial = new Font("Arial", Font.BOLD, 12);
				
				double strWidth = g2d.getFontMetrics().getStringBounds(str, g2d).getWidth();
				double strHeight = g2d.getFontMetrics().getStringBounds(str, g2d).getHeight();

				g2d.setColor(tubeColor);
				g2d.setFont(arial);
				Point pointText = new Point((pointLongest1.x + pointLongest2.x) / 2, (pointLongest1.y + pointLongest2.y) / 2);
				dAngle = Math.atan((double)(pointLongest2.y - pointLongest1.y) / (double)(pointLongest2.x - pointLongest1.x));

				AffineTransform thisTransform = g2d.getTransform();
				g2d.translate(pointText.x, pointText.y);
				g2d.rotate(dAngle);
				
				g2d.drawString(str, (int)(0 - (strWidth / 2)), (int)(strHeight + (Globals.tubeDiameter / 2)));
				g2d.setTransform(thisTransform);
			}
		}
	}
	
	public Color colorFromInnerDiameter(double dInnerDiameter)
	{
		if (dInnerDiameter == 4)
			return new Color(20, 20, 20);
		else if (dInnerDiameter == 5)
			return new Color(236, 28, 36);
		else if (dInnerDiameter == 7)
			return new Color(255, 241, 0);
		else if (dInnerDiameter == 10)
			return new Color(38, 169, 244);
		else if (dInnerDiameter == 20)
			return new Color(246, 146, 30);
		else if (dInnerDiameter == 30)
			return new Color(55, 179, 74);
		else
			return Color.GRAY;
		
	}
	
	public double getInnerDiameterFromButton()
	{
		if (this.iSelectedTool == TUBE_4_MIL_TOOL)
			return 4.0;
		else if (this.iSelectedTool == TUBE_5_MIL_TOOL)
			return 5.0;
		else if (this.iSelectedTool == TUBE_7_MIL_TOOL)
			return 7.0;
		else if (this.iSelectedTool == TUBE_10_MIL_TOOL)
			return 10.0;
		else if (this.iSelectedTool == TUBE_20_MIL_TOOL)
			return 20.0;
		else if (this.iSelectedTool == TUBE_30_MIL_TOOL)
			return 30.0;
		else
			return 2.5;
	}
	
	// Draws and updates the new tube cursor
	// Shows a bad connection attempt
	// Call before adding a new tube
	public void updateAndDrawNewTubeCursor(Graphics2D g2d, boolean bDraw, boolean bMousePressed)
	{
		if (this.iSelectedTool < TUBE_4_MIL_TOOL || this.iSelectedTool > TUBE_OTHER_TOOL)
			return;
		
		Point mousePosition = this.getMousePosition();
		if (mousePosition == null)
			return;
		
		// Remove existing connections
		if (this.m_addTubeStartNode != null)
			this.m_addTubeStartNode.removeConnection(this.m_addTubeEndNode);
		if (this.m_addTubeEndNode != null)
			this.m_addTubeEndNode.removeConnection(this.m_addTubeStartNode);

		Point cellPosition = pointToCell(mousePosition, 1, 1);
		boolean bOverlaps = false;
		// Now check to see if the point overlaps any of the parts
		HPLCPart thisPart = null;
		for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
		{
			thisPart = m_vectHPLCParts.get(i);
			if (thisPart.getRect().contains(cellPosition))
			{
				bOverlaps = true;
				break;
			}
		}
		
		if (bOverlaps)
		{
			// If the cursor overlaps an HPLC part, there's no need to create a node with a new HPLCPartTubeJoint because we'll have a node on the part.
			this.m_cursorTubeJoint = null;
			
			Point partLocation = getCornerOfCell(thisPart.getLocation());
			ConnectionNode closestNode = null;
			double dDistanceToClosestNode = 9999999;
			
			for (int i = 0; i < thisPart.connectionNodes.size(); i++)
			{
				ConnectionNode thisNode = thisPart.connectionNodes.get(i);

				Point nodeLocation = new Point(thisNode.getX() + partLocation.x, thisNode.getY() + partLocation.y);
				double dDist = nodeLocation.distance((double)mousePosition.x, (double)mousePosition.y);
				if (dDist < dDistanceToClosestNode)
				{
					dDistanceToClosestNode = dDist;
					closestNode = thisNode;
				}
			}
			
			// This should never happen...
			if (closestNode == null)
				return;

			Point nodeLocation = new Point(closestNode.getX() + partLocation.x, closestNode.getY() + partLocation.y);

			// Don't make a connection if the node is already occupied
			if (closestNode.getNumConnections() >= closestNode.getNumAllowedConnections())
			{
				if (bDraw)
					drawBadNode(g2d, nodeLocation);
				return;
			}

			// Don't make a connection to the last node
			if (closestNode == m_addTubeStartNode)
			{
				if (bDraw)
					drawBadNode(g2d, nodeLocation);
				return;
			}
			
			// Is this the first connection of the tube? If so, make the connection..
			if (this.m_addTubeStartNode == null)
			{
				// This is the first connection
				if (bMousePressed)
				{
					this.m_addTubeStartNode = closestNode;
					// Change the color of the tube to match the selected value
					this.m_addTubeStartNode.setInnerDiameter(getInnerDiameterFromButton());
					// When we change the inner diameter, should we change the volume or the length of the column?
					if (closestNode.getVolumeVisible())
					{
						// Maintain the same volume
						this.m_addTubeStartNode.setVolume(closestNode.getVolume());
						this.m_addTubeStartNode.setLength(((m_addTubeStartNode.getVolume() / 1000000000) / (Math.PI * Math.pow((m_addTubeStartNode.getInnerDiameter() * 0.0000254) / 2, 2)) * 100));
					}
					else
					{
						// Maintain the same length
						this.m_addTubeStartNode.setLength(closestNode.getLength());
						this.m_addTubeStartNode.setVolume((m_addTubeStartNode.getLength() / 100) * (Math.PI * Math.pow((m_addTubeStartNode.getInnerDiameter() * 0.0000254) / 2, 2) * 1000000000));
					}

					this.m_addTubeStartNode.setVolumeVisible(closestNode.getVolumeVisible());
					this.m_addTubeStartNode.propagateValues();
					
					fireDocumentChangedEvent();
				}
				if (bDraw)
					drawGoodNode(g2d, nodeLocation);
				
				return;
			}

			// This is not the first connection of the tube. Make sure it's not going to form a tube loop.
			// We don't allow tube loops
			if (m_addTubeStartNode != null && m_addTubeStartNode.getNumConnections() > 0)
			{
				ConnectionNode[] connectedNodes = m_addTubeStartNode.getConnectedNodes();
				ConnectionNode thisNode = m_addTubeStartNode;
				ConnectionNode backNode;
				if (connectedNodes[0] == null || connectedNodes[0] == this.m_addTubeEndNode)
					backNode = connectedNodes[1];
				else
					backNode = connectedNodes[0];
				
				while (backNode.getNumConnections() > 1)
				{
					connectedNodes = backNode.getConnectedNodes();
					if (connectedNodes[0] == thisNode)
					{
						thisNode = backNode;
						backNode = connectedNodes[1];
					}
					else
					{
						thisNode = backNode;
						backNode = connectedNodes[0];	
					}
				}
				
				if (backNode == closestNode)
				{
					// If we made this connection, it would form a tube loop. Can't allow it.
					if (bDraw)
						drawBadNode(g2d, nodeLocation);
					return;
				}
			}
			
			this.m_addTubeEndNode = closestNode;
			this.m_addTubeStartNode.makeConnection(this.m_addTubeEndNode);
			
			if (bDraw)
				drawGoodNode(g2d, nodeLocation);
			
			if (bMousePressed)
			{
				// set the m_addTubeEndNode to null so that it doesn't break the connection again.
				this.m_addTubeStartNode.propagateValues();
				this.m_addTubeStartNode = m_addTubeEndNode;
				this.m_addTubeEndNode = null;
				
				// Can this node accept any more connections? If not, then kill the line.
				if (m_addTubeStartNode.getNumConnections() >= m_addTubeStartNode.getNumAllowedConnections())
					this.m_addTubeStartNode = null;
				
				fireDocumentChangedEvent();
			}

		}
		else
		{
			// Not overlapping a part, so create a new HPLCPartTubeJoint to hold a new node
			//if (this.m_cursorTubeJoint == null)
			//{
				this.m_cursorTubeJoint = new HPLCPartTubeJoint();
				ConnectionNode thisNode = this.m_cursorTubeJoint.getConnectionNodes().get(0);
				
				if (m_addTubeStartNode == null)
				{
					thisNode.setInnerDiameter(this.getInnerDiameterFromButton());
					//thisNode.setLength(10);
					thisNode.setVolume(Math.PI * Math.pow((getInnerDiameterFromButton() * .0000254) / 2, 2) * (thisNode.getLength() / 100.0) * 1000000000.0);
					//thisNode.setVolumeVisible(false);
				}
				else
				{
					thisNode.setInnerDiameter(m_addTubeStartNode.getInnerDiameter());
					// When we change the inner diameter, should we change the volume or the length of the column?
					thisNode.setLength(m_addTubeStartNode.getLength());
					thisNode.setVolume(m_addTubeStartNode.getVolume());
					thisNode.setVolumeVisible(m_addTubeStartNode.getVolumeVisible());
					thisNode.setFluidColor(m_addTubeStartNode.getFluidColor());
				}
			//}

			// Now connect the m_addTubeStartNode with this new part
			if (this.m_addTubeStartNode != null)
			{
				this.m_addTubeEndNode = this.m_cursorTubeJoint.getConnectionNodes().get(0);
				this.m_addTubeStartNode.makeConnection(m_addTubeEndNode);
				//m_addTubeStartNode.makeConnection(this.m_addTubeEndNode);
			}
			
			// Put the selected joint in the right place
			this.m_cursorTubeJoint.setX(cellPosition.x);
			this.m_cursorTubeJoint.setY(cellPosition.y);
			
			if (bDraw)
			{
				// Draw the highlighted node
				Point cornerLocation = getCornerOfCell(cellPosition);
				Point nodeLocation = new Point((int)cornerLocation.getX() + (Globals.gridSpacing / 2), (int)cornerLocation.getY() + (Globals.gridSpacing / 2));
	
				drawGoodNode(g2d, nodeLocation);
			}
			
			if (bMousePressed)
			{
				// Set m_addTubeStartNode to the new joint
				this.m_addTubeStartNode = this.m_cursorTubeJoint.getConnectionNodes().get(0);
				//this.m_addTubeStartNode.setInnerDiameter(this.getInnerDiameterFromButton());
				//this.m_addTubeStartNode.setLength(10);
				//this.m_addTubeStartNode.setVolume(Math.PI * Math.pow((getInnerDiameterFromButton() * .0000254) / 2, 2) * (10.0 / 100.0) * 1000000000.0);
				//this.m_addTubeStartNode.setVolumeVisible(false);

				// Add the new HPLCPart
				this.m_vectHPLCParts.add((HPLCPart)m_cursorTubeJoint.clone());
				
				// Need to create a new m_cursorTubeJoint
				this.m_cursorTubeJoint = null;
				
				fireDocumentChangedEvent();
			}

		}		
	}
	
	public void drawGoodNode(Graphics2D g2d, Point nodeLocation)
	{
		// Closest node is found, now wipe out the space over it.
		AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
		g2d.setComposite(myAlpha);
		
		g2d.setColor(Color.WHITE);
		g2d.fillOval(nodeLocation.x - (Globals.tubeDiameter / 2), nodeLocation.y - (Globals.tubeDiameter / 2), Globals.tubeDiameter + 1, Globals.tubeDiameter + 1);
	
		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4f);
		g2d.setComposite(myAlpha);

		g2d.setColor(Color.GREEN);
		
		g2d.fillOval(nodeLocation.x - (Globals.tubeDiameter / 2), nodeLocation.y - (Globals.tubeDiameter / 2), Globals.tubeDiameter + 1, Globals.tubeDiameter + 1);

		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f);
		g2d.setComposite(myAlpha);

		g2d.drawOval(nodeLocation.x - (Globals.tubeDiameter / 2), nodeLocation.y - (Globals.tubeDiameter / 2), Globals.tubeDiameter, Globals.tubeDiameter);
	
		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
		g2d.setComposite(myAlpha);
	}
	
	public void drawBadNode(Graphics2D g2d, Point nodeLocation)
	{
		// Closest node is found, now wipe out the space over it.
		AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
		g2d.setComposite(myAlpha);
		
		g2d.setColor(Color.WHITE);
		g2d.fillOval(nodeLocation.x - (Globals.tubeDiameter / 2), nodeLocation.y - (Globals.tubeDiameter / 2), Globals.tubeDiameter + 1, Globals.tubeDiameter + 1);
	
		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);
		g2d.setComposite(myAlpha);

		g2d.setColor(Color.RED);

		g2d.fillOval(nodeLocation.x - (Globals.tubeDiameter / 2), nodeLocation.y - (Globals.tubeDiameter / 2), Globals.tubeDiameter + 1, Globals.tubeDiameter + 1);
	
		myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
		g2d.setComposite(myAlpha);
	}
	
	public void drawGrid(Graphics2D g2d)
	{
		g2d.setColor(new Color(230,230,230));
		
		int iXOffset = this.m_originPoint.x % Globals.gridSpacing;
		int x = iXOffset;
		
		while (x < this.getWidth())
		{
			g2d.drawLine(x, 0, x, this.getHeight());
			x += Globals.gridSpacing;
		}
		
		int iYOffset = this.m_originPoint.y % Globals.gridSpacing;
		int y = iYOffset;
		
		while (y < this.getWidth())
		{
			g2d.drawLine(0, y, this.getWidth(), y);
			y += Globals.gridSpacing;
		}
		
		//g2d.setColor(Color.GRAY);
		//g2d.drawLine(0, this.m_originPoint.y, this.getWidth(), this.m_originPoint.y);
		//g2d.drawLine(this.m_originPoint.x, 0, this.m_originPoint.x, this.getHeight());
	}
	
	public void drawParts(Graphics2D g2d, boolean bDrawNodes)
	{
		if (this.m_dragPart == null)
		{
			// If not dragging a part, just draw them all
			for (int i = 0; i < m_vectHPLCParts.size(); i++)
			{
				HPLCPart thisPart = m_vectHPLCParts.get(i);
				if (!bDrawNodes)
					thisPart.drawPart(g2d, getCornerOfCell(new Point(thisPart.x, thisPart.y)));
				else
					thisPart.drawConnectionNodes(g2d, getCornerOfCell(new Point(thisPart.x, thisPart.y)));
			}
		}
		else
		{
			// We're dragging a part
			
			Rectangle dragPartRect = m_dragPart.getRect();
			boolean bOverlaps = false;
			
			for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
			{
				HPLCPart thisPart = m_vectHPLCParts.get(i);
				
				// Skip the part we're dragging
				if (thisPart == m_dragPart)
					continue;
				
				if (!bDrawNodes)
					thisPart.drawPart(g2d, getCornerOfCell(new Point(thisPart.x, thisPart.y)));
				else
					thisPart.drawConnectionNodes(g2d, getCornerOfCell(new Point(thisPart.x, thisPart.y)));

				if (thisPart.getRect().intersects(dragPartRect))
				{
					thisPart.drawHighlight(g2d, getCornerOfCell(thisPart.getLocation()), false);
					bOverlaps = true;
				}
			}
			
			if (!bDrawNodes)
				m_dragPart.drawPart(g2d, getCornerOfCell(m_dragPart.getLocation()));
			else
				m_dragPart.drawConnectionNodes(g2d, getCornerOfCell(m_dragPart.getLocation()));

			if (bOverlaps)
				m_dragPart.drawHighlight(g2d, getCornerOfCell(m_dragPart.getLocation()), false);
			
		}
	}
	
	public void drawNewPartCursor(Graphics2D g2d)
	{
		if (m_selectedPart == null)
			return;
		
		Point mousePosition = this.getMousePosition();
		if (mousePosition != null)
		{
			Point cell = pointToCell(mousePosition, m_selectedPart.getWidth(), m_selectedPart.getHeight());
			m_selectedPart.setX(cell.x);
			m_selectedPart.setY(cell.y);
			
			Rectangle selectedPartRect = m_selectedPart.getRect();
			boolean bOverlaps = false;
			
			for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
			{
				HPLCPart thisPart = m_vectHPLCParts.get(i);
				
				if (thisPart.getRect().intersects(selectedPartRect))
				{
					thisPart.drawHighlight(g2d, getCornerOfCell(new Point(thisPart.x, thisPart.y)), false);
					bOverlaps = true;
				}
			}
			
			if (bOverlaps)
				m_selectedPart.drawHighlight(g2d, getCornerOfCell(cell), false);
			//else
				//m_selectedPart.drawHighlight(g2d, getCornerOfCell(cell), true);
			
			AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);
			g2d.setComposite(myAlpha);

			m_selectedPart.drawPart(g2d, getCornerOfCell(cell));
			m_selectedPart.drawConnectionNodes(g2d, getCornerOfCell(cell));
			
			myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			g2d.setComposite(myAlpha);
			//g2d.drawString(Integer.toString(cell.x) + ", " + Integer.toString(cell.y), 100, 100);
		}
	}
	
	public void drawEditPart(Graphics2D g2d)
	{
		if (this.iSelectedTool != EDIT_TOOL)
			return;
		
		HPLCPart mouseOverPart = getPartMouseOver();
		ConnectionNode[] mouseOverTube = getTubeMouseOver();

		if (mouseOverTube[0] != null)
		{
			
		}
		else if (mouseOverPart != null)
		{
			if (mouseOverPart instanceof HPLCPartTubeJoint)
			{
				
			}
			else
				mouseOverPart.drawOverlay(g2d, getCornerOfCell(mouseOverPart.getRect().getLocation()), true);
		}
	}
	
	public void drawRemovePart(Graphics2D g2d)
	{
		if (this.iSelectedTool != REMOVE_TOOL)
			return;
		
		HPLCPart mouseOverPart = getPartMouseOver();
		ConnectionNode[] mouseOverTube = getTubeMouseOver();

		if (mouseOverTube[0] != null)
		{
			Point thisPartPoint1 = this.getCornerOfCell(mouseOverTube[0].getParentHPLCPart().getLocation());
			Point thisNodeLocationOnPart1 = mouseOverTube[0].getLocation();
			Point thisNodeLocation1 = new Point(thisPartPoint1.x + thisNodeLocationOnPart1.x, thisPartPoint1.y + thisNodeLocationOnPart1.y);

			Point thisPartPoint2 = this.getCornerOfCell(mouseOverTube[1].getParentHPLCPart().getLocation());
			Point thisNodeLocationOnPart2 = mouseOverTube[1].getLocation();
			Point thisNodeLocation2 = new Point(thisPartPoint2.x + thisNodeLocationOnPart2.x, thisPartPoint2.y + thisNodeLocationOnPart2.y);

			double dAngle = Math.atan((double)(thisNodeLocation2.y - thisNodeLocation1.y) / (double)(thisNodeLocation2.x - thisNodeLocation1.x));
			dAngle += Math.PI / 2;
			if (thisNodeLocation2.x < thisNodeLocation1.x)
				dAngle = dAngle + Math.PI;

			// Now find offset of the next location
			double dyOffset = -Math.cos(dAngle) * (double)(Globals.tubeEndRadius);
			double dxOffset = Math.sin(dAngle) * (double)(Globals.tubeEndRadius);
			
			Point thisNodeLocationOffset1 = new Point(thisNodeLocation1.x + (int)Math.round(dxOffset), thisNodeLocation1.y + (int)Math.round(dyOffset));
			Point thisNodeLocationOffset2 = new Point(thisNodeLocation2.x - (int)Math.round(dxOffset), thisNodeLocation2.y - (int)Math.round(dyOffset));

			Path2D.Double redTubePath = new Path2D.Double();
			redTubePath.moveTo(thisNodeLocationOffset1.x, thisNodeLocationOffset1.y);
			redTubePath.lineTo(thisNodeLocationOffset2.x, thisNodeLocationOffset2.y);

			AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
			g2d.setComposite(myAlpha);
			g2d.setColor(Color.RED);
			g2d.setStroke(new BasicStroke((float)Globals.tubeDiameter, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g2d.draw(redTubePath);
			myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			g2d.setComposite(myAlpha);
			g2d.setStroke(new BasicStroke(1));
		}
		else if (mouseOverPart != null)
			mouseOverPart.drawOverlay(g2d, getCornerOfCell(mouseOverPart.getRect().getLocation()), false);
	}
	
	public ConnectionNode[] getTubeMouseOver()
	{
		ConnectionNode[] returnNodes = new ConnectionNode[2];
		returnNodes[0] = null;
		returnNodes[1] = null;
		
		Point mousePoint = this.getMousePosition();
		if (mousePoint == null)
			return returnNodes;

		for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
		{
			HPLCPart thisPart = this.m_vectHPLCParts.get(i);
			
			for (int j = 0; j < thisPart.getConnectionNodes().size(); j++)
			{
				ConnectionNode thisNode = thisPart.getConnectionNodes().get(j);
				
				Point thisPartPoint = this.getCornerOfCell(thisNode.getParentHPLCPart().getLocation());
				Point thisNodeLocationOnPart = thisNode.getLocation();
				Point thisNodeLocation = new Point(thisPartPoint.x + thisNodeLocationOnPart.x, thisPartPoint.y + thisNodeLocationOnPart.y);

				ConnectionNode nextNode1 = thisNode.getConnectedNodes()[0];
				if (nextNode1 != null)
				{
					Point nextPartPoint1 = this.getCornerOfCell(nextNode1.getParentHPLCPart().getLocation());
					Point nextNodeLocationOnPart1 = nextNode1.getLocation();
					Point nextNodeLocation1 = new Point(nextPartPoint1.x + nextNodeLocationOnPart1.x, nextPartPoint1.y + nextNodeLocationOnPart1.y);

					double dAngle = Math.atan((double)(nextNodeLocation1.y - thisNodeLocation.y) / (double)(nextNodeLocation1.x - thisNodeLocation.x));
					dAngle += Math.PI / 2;
					if (nextNodeLocation1.x < thisNodeLocation.x)
						dAngle = dAngle + Math.PI;

					// Now find offset of the next location
					double dyOffset = -Math.cos(dAngle) * (double)(Globals.tubeEndRadius);
					double dxOffset = Math.sin(dAngle) * (double)(Globals.tubeEndRadius);
					
					Point thisNodeLocationOffset = new Point(thisNodeLocation.x + (int)Math.round(dxOffset), thisNodeLocation.y + (int)Math.round(dyOffset));
					Point nextNodeLocationOffset1 = new Point(nextNodeLocation1.x - (int)Math.round(dxOffset), nextNodeLocation1.y - (int)Math.round(dyOffset));

					Line2D.Double line1 = new Line2D.Double();
					line1.setLine(thisNodeLocationOffset, nextNodeLocationOffset1);
					
					if (line1.ptSegDist(mousePoint) < Globals.tubeDiameter / 2)
					{
						returnNodes[0] = thisNode;
						returnNodes[1] = nextNode1;
						return returnNodes;
					}
				}
				
				ConnectionNode nextNode2 = thisNode.getConnectedNodes()[1];
				if (nextNode2 != null)
				{
					Point nextPartPoint2 = this.getCornerOfCell(nextNode2.getParentHPLCPart().getLocation());
					Point nextNodeLocationOnPart2 = nextNode2.getLocation();
					Point nextNodeLocation2 = new Point(nextPartPoint2.x + nextNodeLocationOnPart2.x, nextPartPoint2.y + nextNodeLocationOnPart2.y);
					
					double dAngle = Math.atan((double)(nextNodeLocation2.y - thisNodeLocation.y) / (double)(nextNodeLocation2.x - thisNodeLocation.x));
					dAngle += Math.PI / 2;
					if (nextNodeLocation2.x < thisNodeLocation.x)
						dAngle = dAngle + Math.PI;

					// Now find offset of the next location
					double dyOffset = -Math.cos(dAngle) * (double)(Globals.tubeEndRadius);
					double dxOffset = Math.sin(dAngle) * (double)(Globals.tubeEndRadius);
					
					Point thisNodeLocationOffset = new Point(thisNodeLocation.x + (int)Math.round(dxOffset), thisNodeLocation.y + (int)Math.round(dyOffset));
					Point nextNodeLocationOffset1 = new Point(nextNodeLocation2.x - (int)Math.round(dxOffset), nextNodeLocation2.y - (int)Math.round(dyOffset));

					Line2D.Double line2 = new Line2D.Double();
					line2.setLine(thisNodeLocationOffset, nextNodeLocationOffset1);
					
					if (line2.ptSegDist(mousePoint) < Globals.tubeDiameter / 2)
					{
						returnNodes[0] = thisNode;
						returnNodes[1] = nextNode2;
						return returnNodes;
					}
				}
			}
		}
		
		return returnNodes;
	}
	
	public HPLCPart getPartMouseOver()
	{
		Point mousePoint = this.getMousePosition();
		if (mousePoint == null)
			return null;
		
		Point mouseCell = pointToCell(mousePoint, 1, 1);

		for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
		{
			HPLCPart thisPart = m_vectHPLCParts.get(i);
			Rectangle partRect = thisPart.getRect();
			
			if (partRect.contains(mouseCell))
			{
				// Mouse is over this part
				return thisPart;//thisPart.drawRedOverlay(g2d, getCornerOfCell(partRect.getLocation()));
			}
		}
		
		return null;
	}
	
	public Point getCornerOfCell(Point cell)
	{
		Point cornerPoint = new Point();
		cornerPoint.x = this.m_originPoint.x + (cell.x * Globals.gridSpacing);
		cornerPoint.y = this.m_originPoint.y + (cell.y * Globals.gridSpacing);
		return cornerPoint; 
	}
	
	// returns the cell (relative to the origin) that is in the upper-left hand corner of the block
	public Point pointToCell(Point point, int blockWidth, int blockHeight)
	{
		int iCellX = 0;
		if (blockWidth % 2 == 0)
		{
			// Even width
			iCellX = (int)Math.floor(((double)point.x - (double)(this.m_originPoint.x + (Globals.gridSpacing / 2))) / (double)Globals.gridSpacing) - (blockWidth / 2) + 1;
		}
		else
		{
			// Odd width
			iCellX = (int)Math.floor(((double)point.x - (double)this.m_originPoint.x) / (double)Globals.gridSpacing) - ((blockWidth - 1) / 2);			
		}
		
		int iCellY = 0;
		if (blockHeight % 2 == 0)
		{
			// Even height
			iCellY = (int)Math.floor(((double)point.y - (double)(this.m_originPoint.y + (Globals.gridSpacing / 2))) / (double)Globals.gridSpacing) - (blockHeight / 2) + 1;
		}
		else
		{
			// Odd height
			iCellY = (int)Math.floor(((double)point.y - (double)this.m_originPoint.y) / (double)Globals.gridSpacing) - ((blockHeight - 1) / 2);			
		}
		
		return new Point(iCellX, iCellY);
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() 
	{
		return this.getPreferredSize();
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

		}
		// TODO Auto-generated method stub
		
	}


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseDragged(MouseEvent arg0) 
	{
		if (this.m_dragPart != null)
		{
			// First, find shift of mouse from center of part when user started dragging
			Point originalTopLeftCorner = getCornerOfCell(m_dragStartPartCell);
			Point originalCenter = new Point(originalTopLeftCorner.x + ((m_dragPart.getWidth() * Globals.gridSpacing) / 2), originalTopLeftCorner.y + ((m_dragPart.getHeight() * Globals.gridSpacing) / 2));
			
			Point curMousePoint = this.getMousePosition();
			if (curMousePoint == null)
				return;
			
			Point offsetFromCenter = new Point(m_dragStartMouseDragPoint.x - originalCenter.x, m_dragStartMouseDragPoint.y - originalCenter.y);
			Point equivalentMousePosition = new Point(curMousePoint.x - offsetFromCenter.x, curMousePoint.y - offsetFromCenter.y);
			Point newCell = pointToCell(equivalentMousePosition, m_dragPart.getWidth(), m_dragPart.getHeight());
			
			m_dragPart.setX(newCell.x);
			m_dragPart.setY(newCell.y);
		}		
		
		this.repaint();
	}


	@Override
	public void mouseMoved(MouseEvent arg0) 
	{
		this.m_editNode = null;
		
		if (this.iSelectedTool == SELECT_TOOL)
		{
			HPLCPart mouseOverPart = getPartMouseOver();

			if (mouseOverPart != null)
			{
				if (mouseOverPart instanceof HPLCPartValve)
				{
					HPLCPartValve thisValve = (HPLCPartValve)mouseOverPart;
					
					Point mousePoint = this.getMousePosition();
					Point partPoint = getCornerOfCell(thisValve.getLocation());
					if (thisValve.isPointInValveSwitchArea(new Point(mousePoint.x - partPoint.x, mousePoint.y - partPoint.y)))
					{
						if (thisValve.getCursor() == 1)
							this.setCursor(m_curClockwise);
						else
							this.setCursor(m_curCounterClockwise);
					}
					else
					{
						setCursor(m_curOpenHand);
					}
				}
				else
				{
					setCursor(m_curOpenHand);
				}
			}
			else
				setCursor(Cursor.getDefaultCursor());
		}
		else if (this.iSelectedTool == EDIT_TOOL)
		{
			ConnectionNode[] mouseOverTube = getTubeMouseOver();
			HPLCPart mouseOverPart = getPartMouseOver();

			if (mouseOverTube[0] != null)
			{
				this.setCursor(m_curPointHand);
				this.m_editNode = mouseOverTube[0];
			}
			else if (mouseOverPart != null)
			{
				this.setCursor(m_curPointHand);
				
				if (mouseOverPart instanceof HPLCPartTubeJoint)
					this.m_editNode = mouseOverPart.getConnectionNodes().get(0);
			}
			else
				setCursor(Cursor.getDefaultCursor());
		}
		
		this.repaint();
	}


	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		//fireChangedViewportViewEvent();
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) 
	{
		this.repaint();
	}

	public void removePart(HPLCPart partToRemove)
	{
		for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
		{
			if (partToRemove == m_vectHPLCParts.get(i))
			{
				// Remove all connections in parts connected to this one
				for (int j = 0; j < partToRemove.getConnectionNodes().size(); j++)
				{
					ConnectionNode thisNode = partToRemove.getConnectionNodes().get(j);
					
					for (int k = 0; k < thisNode.getConnectedNodes().length; k++)
					{
						ConnectionNode nextNode = thisNode.getConnectedNodes()[k];
						
						// Remove the connection
						if (nextNode != null)
						{
							nextNode.removeConnection(thisNode);
							
							// Remove the HPLCPart if it is a HPLCPartTubeJoint w/o any connections
							if (nextNode.getNumConnections() == 0)
							{
								if (nextNode.getParentHPLCPart() instanceof HPLCPartTubeJoint)
								{
									removePart(nextNode.getParentHPLCPart());
								}
							}
						}
					}
				}
				m_vectHPLCParts.remove(partToRemove);
				this.repaint();
				fireDocumentChangedEvent();
				return;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent arg0) 
	{
		if (this.iSelectedTool == SELECT_TOOL)
		{
			if (this.m_selectedPart != null)
			{			
				boolean bOverlap = false;
				for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
				{
					Rectangle thisPartRect = m_vectHPLCParts.get(i).getRect();
					Rectangle selectedPartRect = m_selectedPart.getRect();
					if (thisPartRect.intersects(selectedPartRect))
					{
						bOverlap = true;
						break;
					}
				}
				if (bOverlap == false)
				{
					// Add the part.
					
					// If it is a pump or an autosampler, select the color of the fluid.
					if (m_selectedPart instanceof HPLCPartPump || m_selectedPart instanceof HPLCPartAutosampler)
					{
						boolean[] usedColors = new boolean[Globals.standardFluidColors.length];
						for (int i = 0; i < usedColors.length; i++)
							usedColors[i] = false;
						
						for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
						{
							HPLCPart thisPart = m_vectHPLCParts.get(i);
							
							if (thisPart.getFluidColor() != null)
							{
								for (int k = 0; k < usedColors.length; k++)
								{
									if (Globals.standardFluidColors[k].getRed() == thisPart.getFluidColor().getRed()
											&& Globals.standardFluidColors[k].getGreen() == thisPart.getFluidColor().getGreen()
											&& Globals.standardFluidColors[k].getBlue() == thisPart.getFluidColor().getBlue())
										usedColors[k] = true;
								}
							}
								
						}
						
						// Find the first color that's not used
						boolean bFound = false;
						for (int i = 0; i < usedColors.length; i++)
						{
							if (usedColors[i] == false)
							{
								m_selectedPart.setFluidColor(Globals.standardFluidColors[i]);
								bFound = true;
								break;
							}
						}
						
						// If the standard colors get used up, come up with a random one.
						if (!bFound)
						{
							Random rand = new Random();
							
							float r = rand.nextFloat();
							float g = rand.nextFloat();
							float b = rand.nextFloat();
							
							Color randomColor = new Color(r, g, b);

							m_selectedPart.setFluidColor(randomColor);
						}
					}
					this.m_vectHPLCParts.add((HPLCPart)this.m_selectedPart.clone());
					//this.m_vectConnectionNodes.addAll(this.m_selectedPart.getConnectionNodes());
					
					this.m_selectedPart = null;
					this.repaint();
					fireDocumentChangedEvent();
					fireAddPartEvent();
				}
			}
			else
			{
				// We're not dropping a new part
				// Should we switch a valve?
				HPLCPart mouseOverPart = getPartMouseOver();
				
				if (mouseOverPart != null)
				{
					if (mouseOverPart instanceof HPLCPartValve)
					{
						HPLCPartValve thisValve = (HPLCPartValve)mouseOverPart;
						
						Point mousePoint = this.getMousePosition();
						Point partPoint = getCornerOfCell(thisValve.getLocation());
						if (thisValve.isPointInValveSwitchArea(new Point(mousePoint.x - partPoint.x, mousePoint.y - partPoint.y)))
						{			
							thisValve.toggleSwitchState();
							if (thisValve.getCursor() == 1)
								this.setCursor(m_curClockwise);
							else
								this.setCursor(m_curCounterClockwise);
							
							this.repaint();
							fireDocumentChangedEvent();
							return;
						}
					}
					
					// Start dragging the part
					this.m_dragPart = mouseOverPart;
					this.m_dragStartMouseDragPoint = this.getMousePosition();
					this.m_dragStartPartCell = mouseOverPart.getLocation();
					
					this.setCursor(this.m_curClosedHand);
				}
			}
		}
		else if (this.iSelectedTool == REMOVE_TOOL)
		{
			ConnectionNode[] mouseOverTube = getTubeMouseOver();
			
			if (mouseOverTube[0] != null)
			{
				mouseOverTube[0].removeConnection(mouseOverTube[1]);
				mouseOverTube[1].removeConnection(mouseOverTube[0]);
				
				if (mouseOverTube[0].getNumConnections() == 0 && mouseOverTube[0].getParentHPLCPart() instanceof HPLCPartTubeJoint)
					removePart(mouseOverTube[0].getParentHPLCPart());

				if (mouseOverTube[1].getNumConnections() == 0 && mouseOverTube[0].getParentHPLCPart() instanceof HPLCPartTubeJoint)
					removePart(mouseOverTube[1].getParentHPLCPart());
				
				fireDocumentChangedEvent();
			}
			else
			{

				HPLCPart mouseOverPart = getPartMouseOver();

				if (mouseOverPart != null)
				{
					removePart(mouseOverPart);
					fireDocumentChangedEvent();
				}
			}
			
			this.repaint();
		}
		else if (this.iSelectedTool == EDIT_TOOL)
		{
			ConnectionNode[] mouseOverTube = getTubeMouseOver();
			HPLCPart mouseOverPart = getPartMouseOver();

	    	Frame[] frames = Frame.getFrames();

	    	if (mouseOverTube[0] != null)
			{
		    	mouseOverTube[0].showPropertiesDialog(frames[0]);
		    	fireDocumentChangedEvent();
			}
			else if (mouseOverPart != null)
			{
				if (mouseOverPart instanceof HPLCPartTubeJoint)
					this.m_editNode = mouseOverPart.getConnectionNodes().get(0);
				else
					mouseOverPart.showPropertiesDialog(frames[0]);
				
				fireDocumentChangedEvent();
			}
			
			this.repaint();

		}
		else if (this.iSelectedTool >= TUBE_4_MIL_TOOL && this.iSelectedTool <= TUBE_OTHER_TOOL)
		{
			updateAndDrawNewTubeCursor((Graphics2D)this.getGraphics(), false, true);
			fireDocumentChangedEvent();
			this.repaint();
		}
	}


	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
		if (this.m_dragPart != null)
		{
			// Check to see if the part overlaps another part
			Rectangle dragPartRect = m_dragPart.getRect();
			
			for (int i = 0; i < this.m_vectHPLCParts.size(); i++)
			{
				HPLCPart thisPart = m_vectHPLCParts.get(i);
				
				// Skip the part we're dragging
				if (thisPart == m_dragPart)
					continue;
				
				if (thisPart.getRect().intersects(dragPartRect))
				{
					// Overlap detected. Put the part back where it came from.
					m_dragPart.setX(this.m_dragStartPartCell.x);
					m_dragPart.setY(this.m_dragStartPartCell.y);
					fireDocumentChangedEvent();
					break;
				}
				
			}
			
			setCursor(this.m_curOpenHand);
			this.m_dragPart = null;
			this.repaint();
		}
		
	}
	
    public synchronized void addAddPartListener(PartPlacementPanelListener l) 
    {
        m_addPartListeners.add(l);
    }
    
    public synchronized void removeAddPartListener(PartPlacementPanelListener l)
    {
        m_addPartListeners.remove(l);
    }
     
    private synchronized void fireAddPartEvent() 
    {
        AddPartEvent addPartEvent = new AddPartEvent(this);
        Iterator<PartPlacementPanelListener> listeners = m_addPartListeners.iterator();
        while(listeners.hasNext()) 
        {
            ((PartPlacementPanelListener)listeners.next()).partAdded(addPartEvent);
        }
    }

    public synchronized void addDocumentChangedListener(PartPlacementPanelListener l) 
    {
        this.m_documentChangedListeners.add(l);
    }
    
    public synchronized void removeDocumentChangedListener(PartPlacementPanelListener l)
    {
    	m_documentChangedListeners.remove(l);
    }
     
    private synchronized void fireDocumentChangedEvent() 
    {
        DocumentChangedEvent documentChangedEvent = new DocumentChangedEvent(this);
        Iterator<PartPlacementPanelListener> listeners = m_documentChangedListeners.iterator();
        while(listeners.hasNext()) 
        {
            ((PartPlacementPanelListener)listeners.next()).documentChanged(documentChangedEvent);
        }
    }
}  //  @jve:decl-index=0:visual-constraint="-259,126"
