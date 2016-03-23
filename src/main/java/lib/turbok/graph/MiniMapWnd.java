package lib.turbok.graph;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import javax.swing.JComponent;

import lib.turbok.graph.action.IMiniMapData;
import lib.turbok.graph.object.IGraphLink;
import lib.turbok.graph.object.IGraphNode;



/**
 * 그래프를 작게 나타내어 네비게이션 할 수 있게 하는 미니맵
 * 
 * @author mh9.kim
 *
 */
@SuppressWarnings("serial")
public class MiniMapWnd extends JComponent
{
    private IMiniMapData    _data = null;
    
    private double          _ratioX = 0.0;
    private double          _ratioY = 0.0;
    
    private boolean         _mousePressed = false;
    private Rectangle       _dragRect = null;
    
    private static Color    _nodeNormal   = new Color(  0, 176,  80);
    private static Color    _nodeSelected = new Color(  0,   0, 255);
    private static Color    _nodeExecuted = new Color( 80, 145, 200);
    private static Color    _nodeWarning  = new Color(245, 145,  30);
    private static Color    _nodeRunning  = new Color(210,  35,  35);
    
    private static Color    _linkNormal   = new Color(128, 128, 128);
    private static Color    _linkSelected = new Color(  0,   0, 255);
    private static Color    _dragColor    = new Color( 96,  96,  96);
    

    public MiniMapWnd()
    {
        initialize();
    }
    
    private void initialize()
    {
        setDoubleBuffered(true);

        this.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                _mousePressed = true;
                calcDragRect(e.getPoint(), true);
            }
            
            @Override
            public void mouseReleased(MouseEvent e)
            {   
                calcDragRect(e.getPoint(), true);
                _mousePressed = false;
            }
        } );
        
        this.addMouseMotionListener( new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                if( _mousePressed )
                    calcDragRect(e.getPoint(), true);
            }
        } );
    }
    
    public void setData(IMiniMapData data)
    {
        _data = data;

        repaint();
    }
    
    private double calcX(int x)
    {   
        return _ratioX * x;
    }
    
    private double calcY(int y)
    {   
        return _ratioY * y;
    }
    
    private Point calcPoint(Point pt)
    {
        Point p = new Point();
        
        p.setLocation( calcX(pt.x), calcY(pt.y) );
        
        return p;
    }
    
    private double calcRevX(int x)
    {
        return x / _ratioX;
    }
    
    private double calcRevY(int y)
    {
        return y / _ratioY;
    }
    
    private Rectangle calcRectangle(Rectangle rect)
    {
        Rectangle r = new Rectangle();
        
        r.setRect( calcX(rect.x), calcY(rect.y)
                 , calcX(rect.width), calcY(rect.height) );

        return r;
    }
    
    private void assignAdjustFactor()
    {
        if( _data == null )
            return;
        
        Rectangle rectClient = this.getVisibleRect();
        
        Dimension graphSize = _data.getGraphSize();
        Dimension canvasSize = _data.getCanvasSize();
        
        if( graphSize.width < canvasSize.width )
            graphSize.width = canvasSize.width;
        if( graphSize.height < canvasSize.height )
            graphSize.height = canvasSize.height;
        
        _ratioX = rectClient.getWidth() / graphSize.getWidth();
        _ratioY = rectClient.getHeight() / graphSize.getHeight();
        
        if( _ratioX > 1.0 ) _ratioX = 1.0;
        if( _ratioY > 1.0 ) _ratioY = 1.0;
    }
    
    /**
     * 
     * @param centerPt  드래깅 사각형의 가운데가 되길 바라는 위치
     * @param repaint   다시 그리기 여부
     */
    private void calcDragRect(Point centerPt, boolean repaint)
    {
        if( _data == null )
            return;
        
        assignAdjustFactor();
        
        Rectangle rectClient = this.getVisibleRect();
        Dimension canvasSize = _data.getCanvasSize();
        
        int width = (int) calcX(canvasSize.width) - 1;
        int height = (int) calcY(canvasSize.height) - 1;

        if( _dragRect == null )
            _dragRect = new Rectangle(0, 0, width, height);
        else if( centerPt != null )
            _dragRect.setBounds(centerPt.x - width / 2, centerPt.y - height / 2, width, height);
        
        // rectClient를 넘어갈 수는 없으니 위치를 조정해야 함
        if( _dragRect.x < rectClient.x )
            _dragRect.translate(rectClient.x - _dragRect.x, 0);
        if( _dragRect.y < rectClient.y )
            _dragRect.translate(0, rectClient.y - _dragRect.y);
        if( _dragRect.x + width > rectClient.width )
            _dragRect.translate(rectClient.width - _dragRect.x - width - 1, 0);
        if( _dragRect.y + height > rectClient.height )
            _dragRect.translate(0, rectClient.height - _dragRect.y - height - 1);
        
        if( repaint )
        {
            _data.onMouseMove((int) calcRevX(_dragRect.x), (int) calcRevY(_dragRect.y));
            
            this.repaint();
        }
    }
    
    @Override
    public void paint(Graphics g)
    {
        Rectangle rectClient = this.getVisibleRect();
        
        // 바탕화면 지우기
        g.clearRect( rectClient.x, rectClient.y, rectClient.width, rectClient.height );
        
        if( _data == null )
            return;

        // Clip 영역 설정
        g.setClip( rectClient.x, rectClient.y, rectClient.width, rectClient.height );
        
        assignAdjustFactor();
        
        if( _dragRect == null )
            calcDragRect(null, false);
        
        // Link 그리기
        List<IGraphLink> links = _data.getLinks();
        g.setColor( _linkNormal );
        
        if( links != null )
            for(IGraphLink link : links)
            {
                Point s = calcPoint(link.getPointStart());
                Point e = calcPoint(link.getPointEnd());
                
                g.setColor( link.isSelected() ? _linkSelected : _linkNormal );

                g.drawLine( s.x, s.y, e.x, e.y );
            }
        
        // 노드 그리기
        List<IGraphNode> nodes = _data.getNodes();
        if( nodes != null )
            for(IGraphNode node : nodes)
            {
                Rectangle rect = calcRectangle(node.getBounds());
                
                // 상태에 따른 색상 조정
                // 오류가 있는 경우
                if( node.isStatus(3) )
                    g.setColor( _nodeWarning );
                // 선택이 된 경우
                else if( node.isStatus(1) )
                    g.setColor( _nodeSelected );
                // 실행된 결과가 있는 경우
                else if( node.isStatus(2) )
                    g.setColor( _nodeExecuted );
                // 실행 중인 경우
                else if( node.isStatus(4) )
                    g.setColor( _nodeRunning );
                // 일반적인 경우
                else
                    g.setColor( _nodeNormal );
                    
                g.fillRect( rect.x, rect.y, rect.width, rect.height );
            }
        
        // 표시영역 그리기
        g.setColor( _dragColor );
        if( _dragRect != null )
            DrawingTool.drawDashedRect(g, _dragRect);
    }
}
