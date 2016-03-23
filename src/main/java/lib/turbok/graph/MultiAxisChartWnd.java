package lib.turbok.graph;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JToolTip;

import lib.turbok.common.ITabularData;
import lib.turbok.data.RowIndexer;
import lib.turbok.data.RowOperatedStore;
import lib.turbok.data.TabularDataCreator;
import lib.turbok.data.processing.GroupingData;
import lib.turbok.util.TabularDataTool;



/**
 * 여러 축을 사용한 런-차트
 * 
 * @author TurboK
 */
@SuppressWarnings("serial")
public class MultiAxisChartWnd extends GraphComponent
{
    private class AxisItem
    {
        private String      _name = null;
        private long        _startRow = -1;
        private long        _endRow = -1;
        
        public AxisItem(String name, long startRow, long endRow)
        {
            _name = name;
            _startRow = startRow;
            _endRow = endRow;
        }
        
        public String getName() { return _name; }
        
        /** 데이터의 시작 위치. */
        public long s() { return _startRow; }
        
        /** 데이터의 마지막 위치. 이 값까지 포함시켜야 함. */
        public long e() { return _endRow; }
    }
    
    /** 시간 영역을 표시하기 위한 영역의 너비 */
    private static final int    _timeWidth_ = 120;
    
    /** 시간 아이템 한개를 표시할 높이 */
    private static final int    _timeHeight_ = 12;
    
    /** Axis Name을 표시하기 위한 영역의 높이 */
    private static final int    _headerHeight_ = 40;
    
    /** Axis(Item) 표시 영역간 마진*/
    private static final int    _itemMargin_ = 10;
    
    private static final Color _borderColor_ = new Color(130, 135, 144);
    
    /** 컨트롤을 그리기 위한 데이터 */
    // private ITabularData    _data = null;
    
    /** Program Name으로 데이터를 정렬한 인덱스 객체 */
    private RowIndexer          _programSortIndex = null;
    
    /** Program Name 기준으로 정렬된 데이터 */
    private RowOperatedStore    _programSortedData = null;
    
    /** _data 중 시간을 나타내는 컬럼 */
    private int             _timeIdx = -1;
    
    /** _data 중 축을 나타내는 컬럼. 문자형이여야 함. */
    // private int             _axisIdx = -1;

    /** 호출된 프로그램 목록 */
    private List<AxisItem>  _axisItems = null;
    
    /** 시간 최소값 */
    private double          _minTime = 0;
    
    /** 시간 최대값 */
    private double          _maxTime = 0;
    
    /** 한 축이 가질 수 있는 너비 */
    private int             _axisWidth = -1;
    
    /** 한 Call Item의 높이 */
    private int             _itemHeight = _timeHeight_ - 2;
    
    /** 한 Call Item의 너비. TODO 적당한 값 산정. _axisWidth와 연동되어야 하나? */
    private int             _itemWidth = 50;
    
    /** 그림을 그리기 위하여 필요한 전체 크기 */
    private Dimension       _totalSize = new Dimension(0, 0);

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
    
    /** ToolTip으로 표시할 텍스트 */
    private String          _toolTipText = "";
    
    /** 마지막 마우스 위치에 있는 아이템의 실제 데이터 상의 위치 */
    private long            _lastPosStart = -1;
    
    /** 특정 시간에 축별로 가장 많은 로그의 회수 */
    private int             _maxTickCount = -1;
    
    /** 모든 로그를 펼쳐서 그리는 모드 */
    private boolean         _expandMode = false;
    

    // 생성자.
    public MultiAxisChartWnd(GraphComponent.EventListener listener)
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
        
        final float dash1[] = {10.0f};
        _gridStroke = new BasicStroke(1.0f
                                , BasicStroke.CAP_BUTT
                                , BasicStroke.JOIN_MITER
                                , 10.0f, dash1, 0.0f );
        
        _smallFont = new Font(null, Font.PLAIN, 10);
    }
    
    @Override
    public void clear()
    {
        if( _programSortedData != null )
            _programSortedData.clear();
        _programSortedData = null;

        if( _programSortIndex != null )
            _programSortIndex.closeAndDelete();
        _programSortIndex = null;
    }
    
    /**
     * 그래프를 그리기 위한 데이터 설정
     * @param data      원본 데이터
     * @param timeIdx   시간 정보를 담고 있는 컬럼 인덱스. 날짜 혹은 정수형이어야 함.
     * @param callIdx   호출된 프로세스 정보를 담고 이는 컬럼 인덱스.
     */
    public void setData(ITabularData data, int timeIdx, int axisIdx) throws Exception
    {
        clear();
        
//        _data = data;
//        _axisIdx = axisIdx;
        _timeIdx = timeIdx;
        
        // callIdx 기준으로 정렬
        _programSortIndex = (RowIndexer) TabularDataTool.sortedIndex(data, new int[] { axisIdx }, null);

        // 프로그램 이름 기준으로 정렬한 데이터
        _programSortedData
            = TabularDataCreator.newRowOperatedStore("PRG", data, _programSortIndex);

        _maxTime = 0;
        _minTime = Double.MAX_VALUE;
        
        if( _axisItems == null )
            _axisItems = new ArrayList<AxisItem>();
        else
            _axisItems.clear();
        
        GroupingData grpData = new GroupingData(_programSortedData);
        
        grpData.startGrouping(new int[] { axisIdx }, false);
        while( grpData.moveToNextGroup() )
        {
            for(int r = 0; r < grpData.getElemSize(); ++r)
            {
                Object value = grpData.getCell(timeIdx, r);
                
                if( value == null )
                    continue;

                double timeVal = 0;
                
                if( value instanceof Date )
                    timeVal = ((Date) value).getTime();
                else if( value instanceof Long )
                    timeVal = (Long) value;
                else
                    throw new Exception("Invalid time format.");
                
                if( _minTime > timeVal )
                    _minTime = timeVal;
                if( _maxTime < timeVal )
                    _maxTime = timeVal;
            }
            
            _axisItems.add( new AxisItem((String) grpData.getCurrentKey(0)
                                , grpData.getGroupStartPos()
                                , grpData.getGroupStartPos() + grpData.getElemSize() - 1 ) );
        }

        // 한 축이 가지는 너비 초기화. 최초로 그릴 때 다시 계산함.
        _axisWidth = -1;
        
        // 스크롤 위치 초기화
        scrolling(-9999999, -9999999, false);
        
        _lastPosStart = -1;
        _maxTickCount = -1;
        _expandMode = false;
        
        this.repaint();
    }
    
    private void initializeSize(Graphics g)
    {
        FontMetrics fontMetric = g.getFontMetrics();
        
        int prevWidth = 0;
        for(AxisItem item : _axisItems)
        {
            Rectangle2D rectTitle = fontMetric.getStringBounds(item.getName(), g);
            
            int w = (int) rectTitle.getWidth() + prevWidth;
            
            if( _axisWidth < w )
                _axisWidth = w;
            
            prevWidth = (int) rectTitle.getWidth();
        }
        
        _axisWidth /= 2;
        _axisWidth += _itemMargin_ * 2;
        
        // 전체 크기 계산
        _totalSize.setSize( _timeWidth_ + _axisItems.size() * _axisWidth + _axisWidth / 2
                          , (_maxTime - _minTime) * _timeHeight_ + _headerHeight_ + 10 );
    }
    
    @Override
    public String getToolTipText(MouseEvent event)
    {   
        if( _toolTipText != null && !_toolTipText.isEmpty() )
        {
            return _toolTipText;
        }
        
        return super.getToolTipText(event);
    }
    
    @Override
    public JToolTip createToolTip()
    {
//        JScrollableToolTip tip = new JScrollableToolTip(500, 300);
//        tip.setComponent(this);
//        return tip;
        
        return super.createToolTip();
    }
    
    private String makeToolTipText(long s, long e)
    {
        ITabularData ds = _programSortedData;
        
        // TODO ToolTip에 표시할 컬럼은 조정 가능해야 함.
        int[] toolTipColumn = new int[] {5, 6, 7, 8, 9, 10};
        
        StringBuilder sb = new StringBuilder();
        
        Object value = null;
        
        sb.append("<html>");
        
        sb.append("<b>Monotonic Time:</b> ");
        try { value = ds.getCell(2, s); } catch(Exception ex) { value = null; }
        sb.append(value == null ? "" : value.toString());
        sb.append("<br>");
        
        sb.append("<table width=\"620\">");
        
        // 제목 줄 표시
        sb.append("<tr>");
        for(int c : toolTipColumn)
        {
            sb.append("<th>").append(ds.getColumnName(c)).append("</th>");
        }
        sb.append("</tr>");
        
        // 값 표시
        for(long r = s; r < e; ++r)
        {
            sb.append("<tr>");
            for(int c : toolTipColumn)
            {   
                try { value = ds.getCell(c, r); } catch(Exception ex) { value = null; }
                
                sb.append("<td>");
                if( value == null )
                    sb.append("");
                else
                    sb.append(value.toString());
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        
        sb.append("</table>");
        sb.append("</html>");
        
        return sb.toString();
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
    
    public boolean toggleExpand()
    {
        if( _maxTickCount == -1 )
        {
            _expandMode = false;
        }
        else
        {
            if( _expandMode )
            {
                _expandMode = false;
                
                _totalSize.height
                    = (int) ((_maxTime - _minTime) * _timeHeight_ + _headerHeight_ + 10);
            }
            else
            {
                _expandMode = true;
                
                _totalSize.height
                    = (int) ((_maxTime - _minTime) * _timeHeight_ * _maxTickCount + _headerHeight_ + 10);
            }
        }
        
        this.repaint();
        
        return _expandMode;
    }
    
    /**
     * 지정한 값의  Y축 위치를 계산하여 반환
     * @param timeVal
     * @return
     */
    private int calcYPos(double timeVal, int scrollY)
    {
        // 아래 _minTime 의 위치임.
        double y = _headerHeight_ + 15 + _timeHeight_ - scrollY;

        if( _expandMode )
            y += (timeVal - _minTime) * _timeHeight_ * this._maxTickCount;
        else
            y += (timeVal - _minTime) * _timeHeight_;

        return (int) y;
    }
    
    private double calcYValue(int y, int scrollY)
    {
        y -= (_headerHeight_ + 15 + _timeHeight_ - scrollY);

        double v = (double) y;
        
        v /= (double) _timeHeight_;
        
        if( _expandMode )
            v /= (double) _maxTickCount;
        
        v += _minTime;
        
        return v;
    }
    
    private boolean drawOneItem(Graphics2D g2, int x, int y, int count)
    {
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, _itemWidth, _itemHeight);
        
        g2.setColor(Color.ORANGE);
        
        if( count > 0 )
            g2.fillRect(x, y, Math.min(_itemWidth, count * 2), _itemHeight);
        else
            g2.fillRect(x, y, _itemWidth, _itemHeight);

        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, _itemWidth, _itemHeight);
        
        if( count > 0 )
            DrawingTool.drawStringAtLeft(g2, "" + count, x + 2, y + 2, null);
        
        if( getLastMousePoint() != null )
        {
            Rectangle2D rect = new Rectangle(x, y, _itemWidth, _itemHeight);
            
            return rect.contains(this.getLastMousePoint());
        }
        
        return false;
    }
    
    /**
     * @return  마우스 커셔 위치에 그린 아이템이 있는 지 여부 반환.
     *          위치에 없을 경우는 -1, 있을 경우는 0이상의 값 반환. 
     */
    private int drawItem(Graphics g, int x, int y, int count)
    {
        Graphics2D g2 = (Graphics2D) g;

        Font oldFont = g2.getFont();
        g2.setFont(_smallFont);
        
        x -= (_itemWidth >> 1);
        y -= (_itemHeight >> 1);
        
        int overItem = -1;
        
        if( _expandMode )
        {
            for(int i = 0; i < count; ++i)
            {
                if( drawOneItem(g2, x, y + _timeHeight_ * i, 0) )
                    overItem = i;
            }
        }
        else
        {
            if( drawOneItem(g2, x, y, count) )
                overItem = 0;
        }
            
        g2.setFont(oldFont);
        
        return overItem;
    }
    
    @Override
    public boolean isPossibleToDraw()
    {
        return _axisItems != null && !_axisItems.isEmpty();
    }
    
    @Override
    public Dimension getTotalSize()
    {
        return _totalSize;
    }
    
    @Override
    public long getRowOfLastMousePoint()
    {
        return _lastPosStart;
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
        
        if( !saveFile )
        {
            g.setColor(_borderColor_);
            g.drawRect( rectClient.x, rectClient.y, rectClient.width, rectClient.height );
        }

        if( !isPossibleToDraw() )
            return;
        
        if( _axisWidth == -1 )
            initializeSize(g);
        
        // 변하지 않는 영역을 그리자
        
        // Guider 표시
        Point lastMousePt = this.getLastMousePoint();
        if( !saveFile && lastMousePt != null )
        {
            g2.setColor(Color.CYAN);
            
            double value = calcYValue(lastMousePt.y, scrollY) + (_expandMode ? 0 : 0.5);
            
            if( value >= _minTime )
            {
                g.setClip( rectClient.x, rectClient.y + _headerHeight_
                         , rectClient.width, rectClient.height - 5 - _headerHeight_ );
                
                int y = _expandMode ? calcYPos(value, scrollY) : calcYPos((int) value, scrollY);
                y -= _timeHeight_ / 2;

                g2.fillRect(rectClient.x + 2, y, rectClient.width - 4, _timeHeight_);
            }
        }
        
        g.setClip( rectClient.x, rectClient.y, rectClient.width, rectClient.height - 5 );
        
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
        DrawingTool.drawStringAtCenter( g, "Time", rectClient.x + _timeWidth_ / 2
                                      , _headerHeight_ / 2, Color.BLACK );
        
        // Time Zone Tick 표시하기
        g2.setClip( rectClient.x, rectClient.y + _headerHeight_
                  , rectClient.width - 5, rectClient.height );
        
        g2.setStroke(_gridStroke);
        
        int increaseTick = _expandMode ? (_maxTickCount >= 5 ? 1 : 2 ): 5;
        for(double tick = _minTime; tick <= _maxTime; tick += increaseTick)
        {
            int y = calcYPos(tick, scrollY) + rectClient.y;
            
            // 영역을 벗어 나므로 그릴 필요 없음
            if( y < rectClient.y - 20 || y > rectClient.height + rectClient.y + 20 )
                continue;

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine( rectClient.x + _timeWidth_ - 2, y
                       , rectClient.x + rectClient.width, y );
            g2.setColor(Color.BLACK);
            
            DrawingTool.drawStringAtRight( g, "" + tick, rectClient.x + _timeWidth_ - 4
                                         , y - 2, Color.BLACK );
        }
        
        // Axis Name 표시
        g2.setClip( rectClient.x + _timeWidth_, rectClient.y
                  , rectClient.width - _timeWidth_, rectClient.height - 5 );
        
        // 이름이 좀 길어서 새로 정의하였음.
        ITabularData ds = _programSortedData;
        
        _toolTipText = "";
        _lastPosStart = -1;
        
        boolean calcTickMax = _maxTickCount == -1;
        
        for(int i = 0; i < _axisItems.size(); ++i)
        {
            AxisItem item = _axisItems.get(i);
            
            int x = _timeWidth_ + i * _axisWidth + _itemMargin_ - scrollX + _axisWidth / 2;
            
            // 끝 위치가 표시 영역보다 작다면 그릴 필요 없음
            if( x + _axisWidth / 2 < rectClient.x + _timeWidth_ )
                continue;
            
            // 시작 위치가 표시 영역을 벗어 난다면 그릴 필요 없음
            if( x - _axisWidth / 2 >= rectClient.x + rectClient.width )
                break;
            
            g2.setClip( rectClient.x + _timeWidth_, rectClient.y
                    , rectClient.width - _timeWidth_, rectClient.height - 5 );
            
            // 제목 표시
            DrawingTool.drawStringAtCenter(g, item.getName(), x, _headerHeight_ / 2, Color.BLACK);
            
            // 축 그리기
            g2.setStroke(_axisStroke);
            g2.drawLine(x, rectClient.y + _headerHeight_ - 4, x, rectClient.y + rectClient.height);
            
            // 해당 축의 값 표시
            g2.setClip( rectClient.x + _timeWidth_ + 1, rectClient.y + _headerHeight_ + 1
                      , rectClient.width - _timeWidth_ - 1, rectClient.height - 5 - _headerHeight_ );
            
            g2.setStroke(_lineStroke);

            long prevTime = -1;
            int timeCount = 1;  // 중복된 시간이 등장한 회수
            for(long r = item.s(); r <= item.e(); ++r)
            {
                try
                {
                    long timeVal = (Long) ds.getCell(_timeIdx, r);
                    
                    if( prevTime == timeVal )
                    {
                        timeCount += 1;
                    }
                    else if( prevTime != -1 )
                    {
                        int overItem = drawItem(g, x, calcYPos(prevTime, scrollY), timeCount);
                        
                        if( overItem != -1 )
                        {
                            long s = r - timeCount + overItem;

                            _lastPosStart = _programSortedData.getOriginalRowIndex(s);
                            _toolTipText = makeToolTipText(s, _expandMode ? s + 1 : r);
                        }
                        
                        if( calcTickMax && _maxTickCount < timeCount)
                            _maxTickCount = timeCount;
                        
                        timeCount = 1;
                    }
                    
                    prevTime = timeVal;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            if( prevTime != -1 )
            {
                int overItem = drawItem(g, x, calcYPos(prevTime, scrollY), timeCount);
                
                if( overItem != -1 )
                {
                    long s = item.e() + 1 - timeCount + overItem;
                    _lastPosStart = _programSortedData.getOriginalRowIndex(s);
                    
                    _toolTipText = makeToolTipText(s, _expandMode ? s + 1 : item.e() + 1);
                }

                if( calcTickMax && _maxTickCount < timeCount)
                    _maxTickCount = timeCount;
            }
        }

        g2.setClip(null);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    @Override
    public double zoom(double xRatio, double yRatio, Point topLeftPoint)
    {
//        JOptionPane.showMessageDialog(this, "Not implemented yet", "PMLog Viewer", JOptionPane.INFORMATION_MESSAGE);
        
        return 1.0;
    }

    @Override
    public void zoomOut()
    {
        // 할 일 없음.
    }
    
    @Override
    public void onClickData(long clickedRow, int button)
    {
        // 할 일 없음.
    }
}
