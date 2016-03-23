package lib.turbok.graph;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JToolTip;

import lib.turbok.graph.object.IMultiAxisData;



@SuppressWarnings("serial")
public class SequentialChartWnd extends GraphComponent
{
    /** 시간 영역을 표시하기 위한 영역의 너비 */
    private static final int    _timeWidth_ = 120;
    
    /** 시간 아이템 한개를 표시할 높이(px) 최소값. 한 개는 1초라고 생각하자. */
    private static final int    _timeHeightLimit_ = 100;
    
    /** Axis Name을 표시하기 위한 영역의 높이 */
    private static final int    _headerHeight_ = 40;
    
    /** Axis(Item) 표시 영역간 마진*/
    private static final int    _itemMargin_ = 10;
    
    /** Border color */
    private static final Color  _borderColor_ = new Color(130, 135, 144);
    
    /** 마우스가 올라 갔을 때 */
    private static final Color  _colorMouseOver_ = new Color(0, 255, 0);
    
    /** 한 축이 가질 수 있는 너비 */
    private int             _axisWidth = -1;
    
    /** 1초가 가질 수 있는 높이(px). 확대 할 수 있게 하려고 멤버로 뺌 */
    private int             _timeHeight = _timeHeightLimit_;
    
    /** 전체 크기 */
    private Dimension       _totalSize = new Dimension(0, 0);
    
    /** 원천 데이터 */
    private IMultiAxisData  _data = null;
    
    /// Graphic Object 정의 //////////////////////////////////////////////////////
    
    /** Header를 구분하기 위한 라인 펜 */
    private Stroke          _headLineStroke = null;
    
    /** 축을 그리기 위한 라인 펜. Dashed Line. */
    private Stroke          _axisStroke = null;
    
    /** 보통 라인 펜. Solid Line. */
    private Stroke          _lineStroke = null;
    
    /** tick의 grid를 표시하기 위한 펜 */
    private Stroke          _gridStroke = null;
    
    /** */
    private Font            _smallFont = null;
    
    /** Minor Tick 표시 여부 */
    private boolean         _minorTickVisible = true;
    
    /** 마우스의 마지막 위치에 해당하는 연결선 행 번호 */
    private int             _lastOverLine = -1;
    
    /** 클릭한 line의 행 번혼 */
    private int             _lastClickedLine = -1;
    

    public SequentialChartWnd(EventListener listener)
    {
        super(listener);

        initialize();
    }
    
    private void initialize()
    {
        // 그리기 위한 그래픽 객체 생성. 옵션으로 뺄 수 있는 부분임.
        _headLineStroke = new BasicStroke( 2.0f
                                         , BasicStroke.CAP_BUTT
                                         , BasicStroke.JOIN_MITER
                                         , 10.0f, null, 0.0f );
        
        _lineStroke = new BasicStroke( 1.0f
                                     , BasicStroke.CAP_BUTT
                                     , BasicStroke.JOIN_MITER
                                     , 10.0f, null, 0.0f );
        
        _axisStroke = _lineStroke;
        
        final float[] dash1 = {10.0f};
        _gridStroke = new BasicStroke(1.0f
                                , BasicStroke.CAP_BUTT
                                , BasicStroke.JOIN_MITER
                                , 10.0f, dash1, 0.0f );
        
        _smallFont = new Font(null, Font.PLAIN, 10);
    }
    
    public void setData(IMultiAxisData data)
    {
        _data = data;
        
        // TODO
        
        // 한 축이 가지는 너비 초기화. 최초로 그릴 때 다시 계산함.
        _axisWidth = -1;
        
        _lastOverLine = -1;
        _lastClickedLine = -1;
        
        scrollTo(0, 0, true);
        
//        _lastPosStart = -1;
//        _maxTickCount = -1;
//        _expandMode = false;
        
        this.repaint();
    }
    
    private void initializeSize(Graphics g)
    {   
        int prevWidth = 0;
        
        for(int i = 0; i < _data.sizeOfAxis(); ++i)
        {
            Rectangle rectTitle = DrawingTool.calcTextMetrics(g, _data.getAxisName(i), 4);
                
            int w = (int) rectTitle.getWidth() + prevWidth;
            
            if( _axisWidth < w )
                _axisWidth = w;
            
            prevWidth = (int) rectTitle.getWidth();
        }
        
        _axisWidth /= 3;
        _axisWidth += _itemMargin_ * 2;
        
        // 전체 크기 계산
        _totalSize.setSize( _timeWidth_ + _data.sizeOfAxis() * _axisWidth + _axisWidth / 2
                          , (_data.maxTime() - _data.minTime()) * _timeHeight + _headerHeight_ + 10 );
    }
    
    @Override
    public void clear()
    {
        if( _data != null )
            _data.clear();
    }
    
    @Override
    public boolean isPossibleToDraw()
    {
        return _data != null;
    }
    
    @Override
    public Dimension getTotalSize()
    {
        return _totalSize;
    }
    
    /**
     * 지정한 값의  Y축 위치를 계산하여 반환
     * @param timeVal
     * @return
     */
    private int calcYPos(double timeVal, int scrollY)
    {
        // 아래 _minTime 의 위치임.
        double y = _headerHeight_ + 20 - scrollY;

        y += (timeVal - _data.minTime()) * _timeHeight;

        return (int) y;
    }
    
    private double calcYValue(int y, int scrollY)
    {
        y -= (_headerHeight_ + 20 - scrollY);

        double v = (double) y;
        
        v /= (double) _timeHeight;
        
        v += _data.minTime();
        
        return v;
    }
    
    private int calcAxisPos(int i, int scrollX)
    {
        return _timeWidth_ + i * _axisWidth + _itemMargin_ - scrollX + _axisWidth / 2;
    }
    
    @Override
    protected void drawComponent(Graphics g, boolean drawAll, boolean saveFile)
    {
        Graphics2D g2 = (Graphics2D) g;
        Rectangle rectClient = new Rectangle(this.getVisibleRect());
        
        int scrollX = getScrollPos().x;
        int scrollY = getScrollPos().y;
        
        // 전체를 다 그려야 하는 경우라면
        if( drawAll )
        {
            if( _axisWidth == -1 )
                return;
            
            scrollX = scrollY = 0;
            rectClient = new Rectangle(0, 0, _totalSize.width, _totalSize.height);
        }
        else
            rectClient = this.getVisibleRect();
        
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();
        
        // 바탕화면 지우기
        g.setColor(Color.WHITE);
        g.fillRect( rectClient.x, rectClient.y, rectClient.width, rectClient.height );
        
        // 파일에 저장하는 경우가 아닌 경우 경계선 그리기
        if( !saveFile )
        {
            g.setColor(_borderColor_);
            g.drawRect( rectClient.x, rectClient.y, rectClient.width, rectClient.height );
        }

        if( !isPossibleToDraw() )
            return;
        
        if( _axisWidth == -1 )
            initializeSize(g);
        
        // 그려야 할 영역 클립핑
        g2.setClip( rectClient.x, rectClient.y, rectClient.width, rectClient.height - 5 );
        
        // Header, Time-Zone 표시
        g2.setColor(Color.BLACK);
        g2.setStroke(_headLineStroke);
        
        // 헤더 가로 축 표시
        g2.drawLine( rectClient.x, rectClient.y + _headerHeight_
                   , rectClient.x + rectClient.width, rectClient.y + _headerHeight_);
        
        // 세로 축 표시
        g2.setStroke(_lineStroke);
        g2.drawLine( rectClient.x + _timeWidth_, rectClient.y + 2
                   , rectClient.x + _timeWidth_, rectClient.y + rectClient.height);
        
        // Time 제목 표시
        DrawingTool.drawStringAtCenter( g, "Time"
                                      , rectClient.x + _timeWidth_ / 2, _headerHeight_ / 2
                                      , Color.BLACK );
        
        // Time Zone Tick 표시하기
        g2.setClip( rectClient.x, rectClient.y + _headerHeight_ + 1
                  , rectClient.width - 5, rectClient.height - 5 );
        
        g2.setStroke(_gridStroke);
        
        double dMin = Math.floor( calcYValue(rectClient.y + _headerHeight_, scrollY) );
        double dMax = this.calcYValue(rectClient.y + _headerHeight_ + rectClient.height , scrollY);
        
        int minorTickCount = _timeHeight / (_timeHeightLimit_ >> 1);
        
        final double majorTickGap = 1.0;
        
        for(double tick = dMin; tick <= dMax; tick += majorTickGap)
        {
            int y = calcYPos(tick, scrollY) + rectClient.y;
            
            // Major Tick 표시
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(rectClient.x + _timeWidth_ - 2, y, rectClient.x + rectClient.width, y);

            DrawingTool.drawStringAtRight( g, String.format("%.3f", tick), rectClient.x + _timeWidth_ - 4
                                         , y - 2, Color.BLACK );
            
            // Minor Tick 표시
            if( _minorTickVisible && minorTickCount > 0 )
            {
                double minorTickGap = majorTickGap / minorTickCount;
                
                for(double tick2 = tick + minorTickGap;
                        tick2 < tick + majorTickGap - 0.001; tick2 += minorTickGap)
                {
                    y = calcYPos(tick2, scrollY) + rectClient.y;
                    
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawLine(rectClient.x + _timeWidth_ - 2, y, rectClient.x + rectClient.width, y);

                     DrawingTool.drawStringAtRight( g, String.format("%.3f", tick2), rectClient.x + _timeWidth_ - 4
                                                  , y - 2, Color.LIGHT_GRAY );
                }
            }
        }
        
        // Axis 그리기
        
        // 실제 화면에 표시되고 있는 축의 인덱스
        g2.setClip( rectClient.x + _timeWidth_, rectClient.y
                  , rectClient.width - _timeWidth_, rectClient.height - 5 );
        
        for(int i = 0; i < _data.sizeOfAxis(); ++i)
        {
            String axisName = _data.getAxisName(i);
            
            int x = calcAxisPos(i, scrollX);
            
            // 끝 위치가 표시 영역보다 작다면 그릴 필요 없음
            if( x + _axisWidth / 2 < rectClient.x + _timeWidth_ )
                continue;

            // 시작 위치가 표시 영역을 벗어 난다면 그릴 필요 없음
            if( x - _axisWidth / 2 >= rectClient.x + rectClient.width )
                break;
            
            g2.setClip( rectClient.x + _timeWidth_, rectClient.y
                    , rectClient.width - _timeWidth_, rectClient.height - 5 );
            
            // 제목 표시
            DrawingTool.drawStringAtCenter(g, axisName
                    , x, _headerHeight_ / 2 + 5
                    , 4, Color.BLACK);

            // 축 라인 그리기
            g2.setStroke(_axisStroke);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(x, rectClient.y + _headerHeight_ - 4, x, rectClient.y + rectClient.height);
        }
        
        // Call Flow 그리기. [sAxis, eAxis]에 포함된 축을 호출하는 애들만 그리자.
        g2.setClip( rectClient.x + _timeWidth_ + 1, rectClient.y + _headerHeight_ + 1
                , rectClient.width - _timeWidth_ - 1, rectClient.height - 5 - _headerHeight_ );
        
        double visibleMin = this.calcYValue(rectClient.y, scrollY);
        double visibleMax = this.calcYValue(rectClient.y + rectClient.height, scrollY);
        
        int ds = _data.searchRow(visibleMin, false);
        int de = _data.searchRow(visibleMax, true);
        
        Font oldFont = g2.getFont();

        _lastOverLine = -1;
        
        g2.setFont(_smallFont);
        
        // 마지막에 그려야 할 행들을 넣어 굼.
        List<Integer> lastDrawing = new ArrayList<Integer>();
        for(int r = ds; r <= de; r += 1)
        {
            double timeVal = _data.timeValue(r);

            if( timeVal < visibleMin || timeVal > visibleMax )
                continue;
            
            if( _data.isGrouped(_lastClickedLine, r) )
                lastDrawing.add(r);
            else
                drawItem(g2, r, scrollX, scrollY, rectClient.width, rectClient.y);
        }
        
        for(Integer r : lastDrawing)
            drawItem(g2, r, scrollX, scrollY, rectClient.width, rectClient.y);
        
        if( _lastOverLine != -1 )
            drawItem( g2, _lastOverLine, scrollX, scrollY
                    , rectClient.width, rectClient.y );
        
        g2.setFont(oldFont);
        
        g2.setClip(null);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }
    
    private void drawItem( Graphics2D g2, int r
                         , int scrollX, int scrollY
                         , int clientWidth, int clientY )
    {
        Point lastMovePt = getLastMousePoint();
        
        int callerAxis = _data.getAxisIndex(_data.caller(r));
        int calleeAxis = _data.getAxisIndex(_data.callee(r));
        
        if( callerAxis == -1 || calleeAxis == -1 )
            return;
        
        int x1 = calcAxisPos(callerAxis, scrollX);
        int x2 = calcAxisPos(calleeAxis, scrollX);
        
        int y = calcYPos(_data.timeValue(r), scrollY) + clientY;

        Color color = _data.getLinkColor(r, _lastClickedLine);
        Stroke stroke = _data.getLinkStroke(r, _lastClickedLine);
        
        if( stroke == null )
            stroke = _lineStroke;

        if( _lastOverLine == r )
            color = _colorMouseOver_;

        if( color != null )
            g2.setColor(color);
        
        if( _lastOverLine == -1 && lastMovePt != null && y - 1 <= lastMovePt.y && lastMovePt.y <= y + 1
                && Math.min(x1,  x2) <= lastMovePt.x && lastMovePt.x <= Math.max(x1,  x2) )
        {
            _lastOverLine = r;
            
            return; // 마지막에 한번 더 그리기 위하여 여기서는 그냥 리턴함
        }

        DrawingTool.drawArrowLine(g2, x1 - (x1 < x2 ? 4 : -4), y, x2, y, 7, 5, stroke);
        
        String toolTip = _data.callerTip(r, false);
        
        if( toolTip != null )
        {
            if( x1 < x2 )
                DrawingTool.drawStringAtLeft(g2, toolTip, x1 + 2, y - 10, color);
            else
                DrawingTool.drawStringAtRight(g2, toolTip, x1 - 2, y - 10, color);
            
            if( Math.abs(x1 - x2) > clientWidth - _timeWidth_ - 1 )
            {
                if( x1 > x2 )
                    DrawingTool.drawStringAtLeft(g2, toolTip, x2 + 18, y - 10, color);
                else
                    DrawingTool.drawStringAtRight(g2, toolTip, x2 - 18, y - 10, color);
            }
        }
    }

    /**
     * 지정한 위치가 Content를 표시하는 위치인지 여부 반환.
     */
    @Override
    public boolean isContentsArea(Point pt)
    {
        return pt.x > _timeWidth_ && pt.y > _headerHeight_;
    }
    
    @Override
    public Rectangle getContentsArea()
    {
        Rectangle rectClient = getVisibleRect();
        
        return new Rectangle( rectClient.x + _timeWidth_ + 1, rectClient.y + _headerHeight_ + 1
                            , rectClient.width - _timeWidth_ - 1, rectClient.height - 5 - _headerHeight_ );
    }
    
    @Override
    public String getToolTipText(MouseEvent event)
    {
        if( _data != null && _lastOverLine != -1 )
        {
            return _data.linkToolTip(_lastOverLine, true);
        }
        
        return super.getToolTipText(event);
    }
    
    @Override
    public JToolTip createToolTip()
    {
        JToolTip tip = new JToolTip();
        tip.setComponent(this);
        return tip;
    }

    @Override
    public long getRowOfLastMousePoint()
    {
        return _lastOverLine;
    }
    
    public double getZoomRatio()
    {
        return (double) _timeHeight / (double) _timeHeightLimit_;
    }

    /** Y축 줌을 yRatio 값만큼 Zooom하는 메소드 */
    public void zoomAtRatio(double yRatio, Point topLeftPoint)
    {
        zoom(1.0, yRatio * _timeHeightLimit_ / _timeHeight, topLeftPoint);
    }
    
    @Override
    public double zoom(double xRatio, double yRatio, Point topLeftPoint)
    {
        double value = calcYValue(topLeftPoint.y, getScrollPos().y);
        
        _timeHeight *= yRatio;
        
        if( _timeHeight < _timeHeightLimit_ )
            _timeHeight = _timeHeightLimit_;

        _totalSize.setSize( _timeWidth_ + _data.sizeOfAxis() * _axisWidth + _axisWidth / 2
                , (_data.maxTime() - _data.minTime()) * _timeHeight + _headerHeight_ + 10 );
        
        scrollTo(getScrollPos().x, calcYPos(value, 0), false);
        
        return (double) _timeHeight / (double) _timeHeightLimit_;
    }

    @Override
    public void zoomOut()
    {
        int scrollX = getScrollPos().x;
        double scrollY = getScrollPos().y;
        
        scrollY *= _timeHeightLimit_;
        scrollY /= _timeHeight;
        
        _timeHeight = _timeHeightLimit_;
        
        _totalSize.setSize( _timeWidth_ + _data.sizeOfAxis() * _axisWidth + _axisWidth / 2
                , (_data.maxTime() - _data.minTime()) * _timeHeight + _headerHeight_ + 10 );
        
        scrollTo(scrollX, (int)scrollY, false);
    }
    
    @Override
    public void onClickData(long clickedRow, int button)
    {
        if( button == MouseEvent.BUTTON1 )
        {
            _lastClickedLine = (int) clickedRow;
            
            repaint();
        }
    }
    
    public int getLastClickedLine() { return _lastClickedLine; }
    
    /**
     * row에 해당하는 데이터가 보이도록 스크롤함.
     * @param row
     */
    public boolean moveTo(int row)
    {
        Rectangle rectClient = getVisibleRect();
        
        int sY = this.calcYPos(_data.getTimeValue(row), 0);
        int sX = this.calcAxisPos(_data.getCallerAxisIndex(row), 0);

        sY -= rectClient.height / 2;
        sX -= rectClient.width / 2;

        _lastClickedLine = row;
        
        this.scrollTo(sX, sY, true);
        
        return true;
    }
}
