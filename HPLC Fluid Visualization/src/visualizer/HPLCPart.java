package visualizer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Vector;

public abstract class HPLCPart implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected String name;
	protected Vector<ConnectionNode> connectionNodes = new Vector<ConnectionNode>();
	
	Color fluidColor = null;

	public HPLCPart(String strName)
	{
		this.name = strName;
	}
	
	public void setName(String newName)
	{
		this.name = newName;
	}
	
	public double getDispersion(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		return 0;
	}
	
	public double getGradientDelay(double dFlowRate)
	{
		return 0;
	}
	
	public double getPressureDrop(double dFlowRate, int iSolventB, double dSolventBFraction)
	{
		return 0;
	}

	public void showPropertiesDialog(Frame parentFrame)
	{
		
	}
	
	public String getName()
	{
		return this.name;
	}
	
	// Returns the node that is internally connected to the given node
	public ConnectionNode getInternalConnection(ConnectionNode thisNode)
	{
		return null;
	}
	
	@Override
	public Object clone()
	{
          try
	      {
        	  HPLCPart clonedPart = (HPLCPart)super.clone();
        	  for (int i = 0; i < clonedPart.getConnectionNodes().size(); i++)
        	  {
        		  ConnectionNode thisNode = clonedPart.getConnectionNodes().get(i);
        		  thisNode.parentHPLCPart = clonedPart;
        	  }
        	  return clonedPart;
	      }
	      catch( CloneNotSupportedException e )
	      {
	              return null;
	      }
	}
	
	public Vector<ConnectionNode> getConnectionNodes()
	{
		return connectionNodes;
	}
	
	public void setFluidColor(Color newColor)
	{
		this.fluidColor = newColor;
	}
	
	public Color getFluidColor()
	{
		return this.fluidColor;
	}

	public void drawPart(Graphics2D g2d, Point position)
	{
		//g2d.drawRect(position.x, position.y, width * Globals.gridSpacing, height * Globals.gridSpacing);
		return;
	}
	
	public void drawConnectionNodes(Graphics2D g2d, Point position)
	{
		for (int i = 0; i < this.connectionNodes.size(); i++)
		{
			connectionNodes.get(i).drawNode(g2d, position);
		}
	}
	
	public void drawHighlight(Graphics2D g2d, Point position, boolean bGreen)
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
	}
	
	public void drawOverlay(Graphics2D g2d, Point position, boolean bGreen)
	{
		
	}
	
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

	public int getWidth()
	{
		return this.width;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public Point getLocation()
	{
		return new Point(x, y);
	}
	
	public Rectangle getRect()
	{
		return new Rectangle(this.x, this.y, this.width, this.height);
	}
}