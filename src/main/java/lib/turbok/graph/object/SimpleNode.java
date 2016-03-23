package lib.turbok.graph.object;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;


/**
 * 이런 저런 테스트를 위한 간단한 그래프 노드.
 * 
 * @author TurboK
 *
 */
public class SimpleNode implements IGraphNode
{
    private static final int    _halfWidth = 25;
    
    private int                 _id = -1;
    private String              _name = "";
    private Rectangle           _bounds = null;
    private boolean             _selected = false;
    
    
    public SimpleNode(int id, String name, Point location)
    {
        _id = id;
        _name = name;

        _bounds = new Rectangle( location.x - _halfWidth, location.y - _halfWidth
                               , _halfWidth * 2, _halfWidth * 2 );
    }
    
    @Override
    public String getName()
    {
        return _name;
    }
    
    @Override
    public int getID()
    {
        return _id;
    }
    
    @Override
    public void draw(Graphics g, Point scrollPos)
    {
        g.setColor( isSelected() ? Color.green : Color.red );
        g.fillRoundRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height, 8, 8);
        
        g.setColor( Color.black );
        g.drawRoundRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height, 8, 8);
        
        FontMetrics fontMetric = g.getFontMetrics();
        
        Rectangle2D rectTitle = fontMetric.getStringBounds(_name, g);
        
        int titleX = _bounds.x + _bounds.width / 2 - rectTitle.getBounds().width / 2;
        int titleY = _bounds.y + _bounds.height + 2 + rectTitle.getBounds().height;
        
        g.drawString(_name, titleX, titleY);
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
        return _bounds.contains(x, y);
    }
    
    @Override
    public Rectangle getBounds()
    {
        return _bounds;
    }
    
    @Override
    public Point getCenter()
    {
        Point centerPt = _bounds.getLocation();
        
        centerPt.translate(_bounds.width / 2, _bounds.height / 2);

        return centerPt;
    }
    
    @Override
    public Rectangle moveWithOffset(int dx, int dy)
    {
        _bounds.translate(dx, dy);
        
        return _bounds;
    }
    
    @Override
    public boolean isConnectable(IGraphNode from, int linkType)
    {
        return true;
    }

    @Override
    public void onConnectingTo(IGraphNode to, int linkType)
    {
        //
    }

    @Override
    public void onConnectingFrom(IGraphNode from, int linkType)
    {
        //
    }

    @Override
    public void onDisconnectingTo(IGraphNode to, int linkType)
    {
        //
    }

    @Override
    public void onDisconnectingFrom(IGraphNode from, int linkType)
    {
        //
    }

    @Override
    public void setStateToConnecting(IGraphNode from, int linkType)
    {
        //
    }

    @Override
    public void setStateToNormal()
    {
        //
    }

    @Override
    public void setPosition(int x, int y)
    {
        //
    }

    @Override
    public boolean isStatus(int status)
    {
        return false;
    }
    
    @Override
    public boolean canBeStartingNode()
    {
        return true;
    }
}
