package lib.turbok.chart;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JPanel;

import lib.turbok.chart.common.Margin;
import lib.turbok.chart.plot.Plot;



@SuppressWarnings("serial")
public class ChartPanel extends JPanel
                        implements MouseListener, MouseMotionListener
{
    public static final int     NO_TITLE = 0;
    public static final int     TITLE_ON_TOP = 1;
    public static final int     TITLE_ON_LEFT = 2;
    public static final int     TITLE_ON_BOTTOM = 3;
    public static final int     TITLE_ON_RIGHT = 4;
    
    public static final int     AXIS_LEFT = 1;
    public static final int     AXIS_RIGHT = 2;
    public static final int     AXIS_BOTTOM = 0;
    public static final int     AXIS_TOP= 3;
    
    public static final String[] _axisName = new String[] { "Bottom", "Left", "Right", "Top" };
    
    
    /** 차트를 그리기 위해 필요한 설정값들 */
    private ChartOption     _option = null;
    
    /** 차트 전체 마진 */
    private Margin          _chartMargin = null;
    
    private Rectangle       _regionBounds = null;
    
    private RenderingHints  _rendering = null;
    
    private AlphaComposite  _alphaComp = null;
    
    /**
     * 차트 제목 표시. 0이면 표시하지 않음.
     * 1: 위쪽, 2: 왼쪽, 3: 아래쪽, 4: 오른쪽
     */
    private int             _showTitle = TITLE_ON_TOP;

    private String          _title = "CHART TITLE";
    
    /**
     * 마우스 버튼이 클릭된 처음 위치.
     */
    private Point           _clickedPoint = null;
    
    /**
     * 드래깅 되고 있는 마지막 포인트
     */
    private Point           _draggedPoint = null;

    /**
     * 클릭된 마우스 버튼. 복수 지정 가능. MouseEvent 클래스 참고.
     */
    private int             _clickedButton = 0;
    
    private ChartRegion     _clickedRegion = null;
    
    /**
     * 차팅 영역 관리 멤버. (X<<32|Y --> Region)
     */
    private Map<Long, ChartRegion>  _regionMap = null;
    
    private int             _regionXSize = 1;
    private int             _regionYSize = 1;
    
    private int             _hGap = 5;
    private int             _vGap = 5;
    
    /** 첫 번째 행에 위치한 차팅 영역에 대한 Y 위치 보정치 */
    private int             _firstRowAdj = 35;
    
    /**
     * 모든 Region 내 차트가 같이 Moving 되도록 하는 옵션
     */
    private boolean         _connectedRegions = true;

    
    public ChartPanel()
    {
        this(1, 1);
    }

    /**
     * Chart Region 가로 x 세로 만큼 생성
     * @param horizontal
     * @param vertical
     */
    public ChartPanel(int horizontal, int vertical)
    {
        this(horizontal, vertical, 5, 5);
    }
    
    public ChartPanel(int horizontal, int vertical, int hGap, int vGap)
    {
        init();
        
        _regionXSize = horizontal;
        _regionYSize = vertical;
        _hGap = hGap;
        _vGap = vGap;

        _regionMap = new TreeMap<Long, ChartRegion>();
        
        for(int x = 0; x < _regionXSize; ++x)
            for(int y = 0; y < _regionYSize; ++y)
            {
                _regionMap.put(createRegionKey(x, y), new ChartRegion(this));
            }
    }
    
    private void init()
    {
        this.setLayout(null);
        this.setDoubleBuffered(true);

        _alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.97);
        
        _rendering = new RenderingHints( RenderingHints.KEY_ANTIALIASING
                                       , RenderingHints.VALUE_ANTIALIAS_ON );
        
        _option = new ChartOption();
        
        _chartMargin = new Margin(10, 10, 10, 10);

        // Component 상태 변화 핸들러 추가
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent evt)
            {
                recalculateLayout(false);
            }
        });
        
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
    
    private long createRegionKey(int x, int y)
    {
        long key = x;
        
        key <<=32;
        key |= y;
        
        return key;
    }

    public final RenderingHints renderingHint() { return _rendering; }
    
    public final AlphaComposite alphaComposite() { return _alphaComp; }
    
    /**
     * pt를 포함하는 Region 반환. 없으면 null.
     * @param pt
     * @return
     */
    private ChartRegion regionAt(Point pt)
    {
        if( !_regionBounds.contains(pt) )
            return null;
        
        int w = (_regionBounds.width + _regionXSize - 1) / _regionXSize;
        int h = (_regionBounds.height + _regionYSize - 1 - _firstRowAdj) / _regionYSize;
        
        int x = pt.x - _regionBounds.x;
        int y = pt.y - _regionBounds.y - _firstRowAdj;
        
        long key = createRegionKey(x / w, Math.max(0, y / h) );
        
        return _regionMap.get(key); 
    }
    
    public ChartRegion getDefaultRegion()
    {
        // TODO 기본 Region 변경방법
        return _regionMap.get(0L);
    }
    
    public ChartRegion getChartRegion(int x, int y)
    {
        return _regionMap.get( createRegionKey(x, y) );
    }
    
    private void cancelMouseAction(boolean restore)
    {
        _clickedButton = 0;
        _draggedPoint = _clickedPoint = null;
        
        if( _clickedRegion != null && restore )
        {
            if( _connectedRegions )
            {
                for(ChartRegion cr : _regionMap.values())
                    cr.resetToLastCaptured();
            }
            else
                _clickedRegion.resetToLastCaptured();

            this.repaint();
        }
        
        _clickedRegion = null;
    }
    
    @Override
    public void mousePressed(MouseEvent evt)
    {
        this.grabFocus();
        
        Point pt = evt.getPoint();

        ChartRegion clickedRegion = regionAt(pt);
        
        // TODO 어디를 클릭했는 지 판단하는 로직 추가. Axis, Chart, Title, Legend 등
        
        // 이전 Action 취소
        if( _clickedButton != 0 && _clickedButton != evt.getButton() )
        {
            cancelMouseAction(true);            
            return;
        }
        
        // 차팅 영역 내에서 처음 마우스 이벤트가 발생한 경우임.
        if( _clickedButton == 0 && clickedRegion != null
                && clickedRegion.getChartingBounds().contains(pt) )
        {
            _draggedPoint = _clickedPoint = pt;

            _clickedRegion = clickedRegion;
            _clickedButton = evt.getButton();
            
            if( _connectedRegions )
            {
                for(ChartRegion cr : _regionMap.values())
                    cr.captureAxisState();
            }
            else
                _clickedRegion.captureAxisState();
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent evt)
    {
        if( _clickedButton != 0 )
        {
            _draggedPoint = evt.getPoint();
            
            // Move Action 처리
            if( _clickedButton == MouseEvent.BUTTON3 && _clickedRegion != null )
            {
                if( _connectedRegions )
                {
                    for(ChartRegion cr : _regionMap.values())
                        cr.translateAxis(_clickedPoint.x, _draggedPoint.x, _clickedPoint.y, _draggedPoint.y);
                }
                else
                    _clickedRegion.translateAxis(_clickedPoint.x, _draggedPoint.x, _clickedPoint.y, _draggedPoint.y);
            }
                
            this.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent evt)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseReleased(MouseEvent evt)
    {
        if( _clickedRegion != null )
        {
            // 영역 확대 Action
            if( _clickedButton == MouseEvent.BUTTON1 )
            {
                // 초기 상태로 되돌리기
                if( _clickedPoint.x > _draggedPoint.x )
                {
                    if( _connectedRegions )
                    {
                        for(ChartRegion cr : _regionMap.values())
                            cr.resetToAxisInit();
                    }
                    else
                        _clickedRegion.resetToAxisInit();
                }
                // 확대
                else if( Math.abs(_draggedPoint.x - _clickedPoint.x) > 10
                        && Math.abs(_draggedPoint.y - _clickedPoint.y) > 10 )
                {
                    int x1 = Math.min(_clickedPoint.x, _draggedPoint.x);
                    int x2 =  Math.max(_clickedPoint.x, _draggedPoint.x);
                    int y1 = Math.min(_clickedPoint.y, _draggedPoint.y);
                    int y2 = Math.max(_clickedPoint.y, _draggedPoint.y);
                    
                    if( _connectedRegions )
                    {       
                        for(ChartRegion cr : _regionMap.values())
                        {
                            int rX = cr.getChartingBounds().x - _clickedRegion.getChartingBounds().x;
                            int rY = cr.getChartingBounds().y - _clickedRegion.getChartingBounds().y;
                            
                            cr.zoomAxis(x1 + rX, x2 + rX, y1 + rY, y2 + rY);
                        }
                    }
                    else
                        _clickedRegion.zoomAxis(x1, x2, y1, y2);
                }
                
                if( _connectedRegions )
                {
                    for(ChartRegion cr : _regionMap.values())
                        cr.resetToRedraw();
                }
                else
                    _clickedRegion.resetToRedraw();
            }
            // 차트 이동
            else if( _clickedButton == MouseEvent.BUTTON3 )
            {
                if( _connectedRegions )
                {
                    for(ChartRegion cr : _regionMap.values())
                        cr.resetToRedraw();
                }
                else
                    _clickedRegion.resetToRedraw();
            }
        }
        
        cancelMouseAction(false);
        this.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent evt)
    {
        // 
    }
    
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        // System.out.println(evt);
    }

    @Override
    public void mouseExited(MouseEvent evt)
    {
        // 
    }

    public ChartOption option()
    {
        return _option;
    }

    /**
     * 전체 컨트롤 크기에서 지정한 마진을 제외한 영역 반환.
     */
    public Rectangle getMarginedBounds()
    {
        Rectangle rect = this.getBounds();
        
        rect.x += _chartMargin.getLeft();
        rect.width -= _chartMargin.getLeftRight();
        rect.y += _chartMargin.getTop();
        rect.height -= _chartMargin.getTopBottom();
        
        return rect;
    }
    
    /**
     * 차트를 그리지 않을 바깥 쪽 마진 설정.
     * 이 영역은 빈 영역으로 남겨지는 부분임.
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setMargin(int left, int top, int right, int bottom)
    {
        _chartMargin.setMargin(left, top, right, bottom);
        recalculateLayout(false);

        repaint();
    }
    
    public boolean isTitleVisible()
    {
        return _showTitle != NO_TITLE && _title != null && !_title.isEmpty();
    }
    
    /**
     * 제목 표시 여부 및 표시 위치 지정.
     * @param pos   NO_TITLE, TITLE_ON_XXXX 값으로 지정
     */
    public void setTitleVisible(int pos)
    {
        _showTitle = pos;
    }
    
    public void setTitle(String title)
    {
        _title = title;
    }
    
    public String getTitle()
    {
        return _title;
    }
    
    /**
     * Plot을 추가하고 구분하기 위한 인덱스 반환.
     * X축과 Y축은 기본으로 제공되는 축이 자동으로 할당됨.
     * 기본 ChartRegion에 추가됨.
     * @param plot
     * @return
     */
    public int addPlot(Plot plot)
    {
        return addPlot(plot, AXIS_BOTTOM, AXIS_LEFT, 0, 0);
    }
    
    /**
     * Plot을 추가하고 구분하기 위한 인덱스 반환.
     * 기본 ChartRegion에 추가됨.
     * @param plot
     * @param axisX X축 인덱스. AXIS_BOTTOM, AXIS_TOP 혹은 추가된 가로 축의 인덱스 지정.
     * @param axisY Y축 인덱스. AXIS_LEFT, AXIS_RIGHT 혹은 추가된 세로 축의 인덱스 지정.
     * @return
     */
    public int addPlot(Plot plot, int axisX, int axisY)
    {
        return addPlot(plot, axisX, axisY, 0, 0);
    }
    
    public int addPlot(Plot plot, int axisX, int axisY, int regionX, int regionY)
    {
        ChartRegion region = this.getChartRegion(regionX, regionY);
        
        if( region == null )
        {
            System.err.println("Invalid Region: (" + regionX + ", " + regionY + ")");
            return -1;
        }
        
        return region.addPlot(plot, axisX, axisY);
    }

    /**
     * 추가된 Plot들의 최소/최대값을 고려하여 축의 Min/Max를 설정.
     */
    protected void recalculateMinMax()
    {
        /*
        List<Double[]> mm = new ArrayList<Double[]>();
        
        // TODO Custom 축 구현되면 수정되어야 함.
        for(int i = 0; i < 4; ++i)
            mm.add(new Double[] { null, null });

        for(Plot plot : _plotList)
        {
            Double[] xy = new Double[] { plot.getMinimumX(), plot.getMaximumX()
                                       , plot.getMinimumY(), plot.getMaximumY() };
            
            int xIdx = plot.getXAxisIdx();
            int yIdx = plot.getYAxisIdx();
            
            Double[] mmVal = mm.get(xIdx);
            if( xy[0] != null && (mmVal[0] == null || mmVal[0] > xy[0]) )
                mmVal[0] = xy[0];
            if( xy[1] != null && (mmVal[1] == null || mmVal[1] < xy[1]) )
                mmVal[1] = xy[1];
            
            mmVal = mm.get(yIdx);
            if( xy[2] != null && (mmVal[0] == null || mmVal[0] > xy[2]) )
                mmVal[0] = xy[2];
            if( xy[3] != null && (mmVal[1] == null || mmVal[1] < xy[3]) )
                mmVal[1] = xy[3];
        }
        
        // TODO Custom 축 구현되면 수정되어야 함.
        for(int i = 0; i < 4; ++i)
        {
            Axis axis = getAxis(i);
            
            if( !axis.isAutoRange() )
                continue;
            
            Double[] mmVal = mm.get(i);
            
            // 유효한 값이 없는 경우임
            if( mmVal[0] == null )
                continue;
            
            // 최소 최대가 같은 경우임.
            if( mmVal[0].equals(mmVal[1]) )
            {
                axis.setMinimumMaximum(mmVal[0] - 1.0, mmVal[1] + 1.0);
                continue;
            }

            // TODO 적당한 gap은?
            double gap = Math.min(1.0, (mmVal[1] - mmVal[0]) * 0.1);
            axis.setMinimumMaximum(mmVal[0] - gap, mmVal[1] + gap);
        }
        // */
    }
    
    public void recalculateLayout(boolean calcAxisRange)
    {
        if( calcAxisRange )
            recalculateMinMax();
        
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        
        Rectangle rect = this.getBounds();
        
        // rect.y를 0으로 맞춰주기. 상대적인 포인트이기 때문에 0이 되어야 하는데 0이 아닌 값인 경우가 있음.
        rect.y = 0;
        
        Margin drawMargin = _chartMargin.clone();

        // 타이틀에 따른 Drawing Margin 조정
        if( this.isTitleVisible() )
        {
            int titleHeight = 30;
            
            if( g2 != null )
            {
                FontMetrics titleMetric = g2.getFontMetrics(option().titleFont());
                titleHeight = titleMetric.getHeight() + 5;
            }
            
            drawMargin.shift(
                      (_showTitle == TITLE_ON_LEFT ? titleHeight : 0)
                    , (_showTitle == TITLE_ON_TOP ? titleHeight : 0)
                    , (_showTitle == TITLE_ON_RIGHT ? titleHeight : 0)
                    , (_showTitle == TITLE_ON_BOTTOM ? titleHeight : 0)
                    );
        }
        
        _regionBounds = new Rectangle(
                  rect.x + drawMargin.getLeft()
                , rect.y + drawMargin.getTop()
                , rect.width - drawMargin.getLeftRight()
                , rect.height - drawMargin.getTopBottom()
                );
        
        int width = (_regionBounds.width + _regionXSize - 1 - _hGap * (_regionXSize - 1) ) / _regionXSize;
        int height = (_regionBounds.height + _regionYSize - 1 - _firstRowAdj - _vGap * (_regionYSize - 1)) / _regionYSize;
        
        for(Entry<Long, ChartRegion> elem : _regionMap.entrySet())
        {
            long key = elem.getKey();
            
            int x = (int) (key >> 32);
            int y = (int) (key & 0xFFFFFFFFL);
            
            Rectangle rectRegion = new Rectangle(
                      _regionBounds.x + x * (width + _hGap)
                    , _regionBounds.y + y * (height + _vGap) + (y == 0 ? 0 : _firstRowAdj)
                    , width, height + (y == 0 ? _firstRowAdj : 0) );
            
            elem.getValue().setBounds(rectRegion);
        }
    }

    private void drawTitle(Graphics2D g2, Rectangle rectWnd)
    {
        g2.setFont(_option.titleFont());
        g2.setColor(_option.titleColor());
        
        FontMetrics titleMetric = g2.getFontMetrics(option().titleFont());
        
        int angle = 0;
        int x = rectWnd.x + rectWnd.width / 2;
        int y = _chartMargin.getTop() + titleMetric.getLeading();
        
        switch( _showTitle )
        {
        case TITLE_ON_TOP:
            x = rectWnd.x + rectWnd.width / 2;
            y = _chartMargin.getTop() + titleMetric.getLeading();
            break;
            
        case TITLE_ON_LEFT:
            x = _chartMargin.getLeft() + titleMetric.getLeading();
            y = rectWnd.y + rectWnd.height / 2;
            angle = -90;
            break;
        
        case TITLE_ON_BOTTOM:
            x = rectWnd.x + rectWnd.width / 2;
            y = rectWnd.y + rectWnd.height - (_chartMargin.getBottom() + titleMetric.getHeight());
            break;
            
        case TITLE_ON_RIGHT:
            x = rectWnd.x + rectWnd.width - (_chartMargin.getRight() + titleMetric.getLeading());
            y = rectWnd.y + rectWnd.height / 2;
            angle = 90;
            break;
        }
        
        DrawingTool.drawStringAtCenter(g2, _title, x, y, angle);
    }
    
    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        Rectangle rectWnd = this.getBounds();
        
        rectWnd.y = 0;
        
        // 바탕화면 지우기
        g2.setBackground(_option.bgColor());
        g2.clearRect( rectWnd.x, rectWnd.y, rectWnd.width, rectWnd.height );
        
        g2.setColor(Color.darkGray);
        g2.drawRect( rectWnd.x, rectWnd.y, rectWnd.width - 1, rectWnd.height - 1 );
        
        // 타이틀 표시
        if( isTitleVisible() )
            drawTitle(g2, rectWnd);
        
        int offsetX = 0, offsetY = 0;
        
        if( _clickedButton == MouseEvent.BUTTON3 )
        {
            offsetX = _draggedPoint.x - _clickedPoint.x;
            offsetY = _draggedPoint.y - _clickedPoint.y;
        }

        // 전체 Charting Region만 클립핑함.
        g2.setClip(_regionBounds);
        
        // Anti-Aliasing
        g2.setRenderingHints(_rendering);
        
        for(ChartRegion cr : _regionMap.values())
        {
            if( _connectedRegions || cr == _clickedRegion )
                cr.draw(g2, offsetX, offsetY);
            else
                cr.draw(g2, 0, 0);
        }

        // 확대 혹은 확대 취소 액션
        if( _clickedRegion != null && _clickedButton == MouseEvent.BUTTON1 )
        {
            // 차팅 영역만 클립핑
            g2.setClip(_clickedRegion.getChartingBounds());
            
            g2.setColor(_option.dragRectColor());
            g2.setStroke(_option.dragRectStroke());

            g2.draw( DrawingTool.getRectangle(_clickedPoint, _draggedPoint) );
        }
        
        g2.setClip(null);
        g2.dispose();
    }
}
