package lib.turbok.graph.object;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;




/**
 * Diagram에서 화살표 형태의 연결선을 처리하기 위한 클래스.
 * TODO 조금 더 일반화 시킵시다.
 * 
 * @author TurboK
 */
public class DiagramLink implements IGraphLink
{
    /** 보통 연결선 색 */
    private static Color        _normalColor = new Color(96, 96, 96);
    private static Color        _selectedColor = Color.blue;
    
    // TODO Link Type에 따른 색상 관리 static 멤버?
    
    
    /** 실선을 그리기 위한 Stroke */
	private static BasicStroke _lineStroke = null;
	
	/** 점선을 그리기 위한 Stroke */
	private static BasicStroke _dashedStroke = null;

	/** 연결선의 시작 노드 객체 */
	private IGraphNode		   _startNode = null;
	
	/** 연결선의 끝 노드 객체 */
	private IGraphNode		   _endNode = null;
	
	/** 연결선 종류. */
	private int                _linkType = 0;
	
    private boolean            _selected = false;
    
    private String             _subInfo = null;
    
    // Point, Polygon 객체가 계속 생성되는 것을 방지하기 위하여 아래 멤버 추가함
    // >>>>
    private Point              _p1 = null;
    private Point              _p2 = null;
    private Polygon            _lineArea = null;
    // <<<<
    
    static
    {
    	_lineStroke = new BasicStroke( 3.0f
					                 , BasicStroke.CAP_BUTT
					                 , BasicStroke.JOIN_MITER
					                 , 10.0f, null, 0.0f );
    	
    	_dashedStroke = new BasicStroke( 3.0f
                                     , BasicStroke.CAP_BUTT
                                     , BasicStroke.JOIN_MITER
                                     , 10.0f, new float[] {10.0f}, 0.0f );
    }
    
    public DiagramLink(IGraphNode from, IGraphNode to)
    {
        this(from, to, 0);
    }
    
    public DiagramLink(IGraphNode from, IGraphNode to, int linkType)
    {
    	_startNode = from;
    	_endNode = to;
    	_linkType = linkType;
    }
    
    public String getSubInfo()
    {
        return _subInfo;
    }
    
    public void setSubInfo(String info)
    {
        _subInfo = info;
    }
    
    private int getNodeRadius()
    {
        // TODO 조정 필요
        return _startNode.getBounds().width - 5;
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

    	Color linkColor = _normalColor;
    	
    	if( isSelected() )
    	    linkColor = _selectedColor;
    	
        g.setColor( linkColor );
        
        DrawingTool.drawArrowLine( g
        						 , _startNode.getCenter(), _endNode.getCenter()
        						 , getNodeRadius()
        						 , (_linkType == 0 ? _lineStroke : _dashedStroke) );
        
        if( _subInfo != null && !_subInfo.isEmpty() )
        {
            Point centerPt = DrawingTool.calcCenter(_startNode.getCenter(), _endNode.getCenter());
            DrawingTool.drawStringAtCenter(g, _subInfo, centerPt.x, centerPt.y - 12, linkColor); 
        }
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
    
    private void addPointToPolygon(Polygon p, Point pt)
    {
        p.addPoint(pt.x, pt.y);
    }
    
    @Override
    public boolean isHitted(int x, int y)
    {
        final int hitGap = 4;
        
        if( _startNode == null || _endNode == null )
            return false;
        
        Point p1 = _startNode.getCenter();
        Point p2 = _endNode.getCenter();

        boolean recalc = _lineArea == null
                        || !DrawingTool.isSamePoint(p1, _p1)
                        || !DrawingTool.isSamePoint(p2, _p2);
        
        if( recalc )
        {
            _lineArea = new Polygon();
        
            addPointToPolygon(_lineArea, DrawingTool.calcNormalPoint(p1, p2, p1, hitGap));
            addPointToPolygon(_lineArea, DrawingTool.calcNormalPoint(p1, p2, p1, -hitGap));
            addPointToPolygon(_lineArea, DrawingTool.calcNormalPoint(p1, p2, p2, -hitGap));
            addPointToPolygon(_lineArea, DrawingTool.calcNormalPoint(p1, p2, p2, hitGap));

            _p1 = p1;
            _p2 = p2;
        }

        return _lineArea.contains(x, y);
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
		
		int radius = getNodeRadius();
		double d = Math.sqrt( (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) );
		
		return new Point( (int) (p1.x + radius * (p2.x - p1.x) / d)
						, (int) (p1.y + radius * (p2.y - p1.y) / d) );
	}

	@Override
	public Point getPointEnd()
	{
		if( _startNode == null || _endNode == null )
			return null;
		
		Point p1 = _startNode.getCenter();
		Point p2 = _endNode.getCenter();
		
		int radius = getNodeRadius();
		double d = Math.sqrt( (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) );
		
		return new Point( (int) (p2.x - radius * (p2.x - p1.x) / d)
						, (int) (p2.y - radius * (p2.y - p1.y) / d) );
	}

    @Override
    public int getType()
    {
        return _linkType;
    }
}
