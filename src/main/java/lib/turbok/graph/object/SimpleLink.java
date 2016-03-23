package lib.turbok.graph.object;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;



/**
 * 이런 저런 테스트를 위한 간단한 그래프 노드.
 * 
 * @author TurboK
 *
 */
public class SimpleLink implements IGraphLink
{
	private static BasicStroke	_lineStroke = null;
	
	private static final int    _radius = 35;
	
	private IGraphNode		_startNode = null;
	private IGraphNode		_endNode = null;
	
    private String          _name = "";
    private boolean         _selected = false;
    
    static
    {
    	_lineStroke = new BasicStroke( 3.0f
					                 , BasicStroke.CAP_BUTT
					                 , BasicStroke.JOIN_MITER
					                 , 10.0f, null, 0.0f );
    }
    
    
    public SimpleLink(IGraphNode from, IGraphNode to, String name)
    {
    	_startNode = from;
    	_endNode = to;
    	
    	_name = name;
    }
    
    public String getName()
    {
        return _name;
    }
    
    @Override
    public int getID()
    {
        return 0;
    }
    
    @Override
    public void draw(Graphics g, Point scrollPos)
    {
    	if( _startNode == null || _endNode == null )
    		return;

        g.setColor( isSelected() ? Color.blue : Color.black );

        DrawingTool.drawArrowLine( g
        						 , _startNode.getCenter(), _endNode.getCenter()
        						 // , 16, 5
        						 , _radius, _lineStroke );
    }
    
    @Override
    public boolean isSelectable()
    {
        return true;
    }
    
    @Override
    public boolean isSelected()
    {
        return _selected;
    }
    
    @Override
    public void setSelected(boolean selected)
    {
        _selected = selected;
    }
    
    @Override
    public boolean isHitted(int x, int y)
    {
        if( _startNode == null || _endNode == null )
            return false;
        
        Point p1 = _startNode.getCenter();
        Point p2 = _endNode.getCenter();
        
        Polygon p = new Polygon();

        p.addPoint(p1.x - 3, p1.y - 3);
        p.addPoint(p1.x + 3, p1.y + 3);
        p.addPoint(p2.x + 3, p2.y + 3);
        p.addPoint(p2.x + 3, p2.y - 3);
        
        return p.contains(x, y);
    }

	@Override
	public IGraphNode from()
	{
		return _startNode;
	}

	@Override
	public IGraphNode to()
	{
		return _endNode;
	}

	@Override
	public Point getPointStart()
	{
		if( _startNode == null || _endNode == null )
			return null;
		
		Point p1 = _startNode.getCenter();
		Point p2 = _endNode.getCenter();
		
		double d = Math.sqrt( (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) );
		
		// double cs = (p2.x - p1.x) / d;
		// double ss = (p2.y - p1.y) / d;
		
		return new Point( (int) (p1.x + _radius * (p2.x - p1.x) / d)
						, (int) (p1.y + _radius * (p2.y - p1.y) / d) );
	}

	@Override
	public Point getPointEnd()
	{
		if( _startNode == null || _endNode == null )
			return null;
		
		Point p1 = _startNode.getCenter();
		Point p2 = _endNode.getCenter();
		
		double d = Math.sqrt( (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) );
		
		// double cs = (p2.x - p1.x) / d;
		// double ss = (p2.y - p1.y) / d;
		
		return new Point( (int) (p2.x - _radius * (p2.x - p1.x) / d)
						, (int) (p2.y - _radius * (p2.y - p1.y) / d) );
	}

    @Override
    public int getType()
    {
        return 0;
    }
}
