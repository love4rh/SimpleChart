package lib.turbok.chart;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import lib.turbok.chart.axis.Axis;
import lib.turbok.chart.axis.DateTimeAxis;
import lib.turbok.chart.axis.NumericAxis;
import lib.turbok.chart.common.Margin;
import lib.turbok.chart.plot.Plot;



/**
 * ChartWnd에서 한 차트가 그려지는 영역 및 실제 그리는 작업을 수행하는 클래스.
 * 마우스 이벤트 등은 ChartWnd에서 받아 적절히 처리하며 여기서는 Swing Event 관련 처리는 하지 않음.
 * 
 * @author TurboK
 */
public class ChartRegion extends ChartObject
{
    private static final int    _titleBoxWidth = 150;
    
    /** ChartPanel 내에서 이 영역의 위치 및 크기 */
    private Rectangle       _rectBounds = null;
    
    /** 축이 그려지는 영역의 너비 */
    private Margin          _axisArea = null;
    
    /** 축과 Plot이 그려지는 영역 */
    private Rectangle       _rectChart = null;
    
    /** 전체 컨트롤의 영역에서 이 Region의 차트가 실제 그려지는 영역 */
    private Rectangle       _chartingRect = null;
    
    /** 기본으로 제공하는 축. 아래 X축(0), 왼쪽 Y축(1), 오른쪽 Y축(2), 위쪽 X축(3) */
    private Axis[]          _defaultAxes = new Axis[4];
    
    private List<Plot>      _plotList = null;
    
    /**
     * 다시 그릴 필요 없는 차트 이미지.
     * 다시 그릴 필요가 있는 경우에만 null로 세팅되며 이 경우 다시 그려져 설정됨.
     */
    private BufferedImage   _drawingImg = null;
    
    /**
     * 차트 왼쪽에 제목 박스를 표시할 지 여부
     */
    private boolean         _showTitleBox = false;
    
    private String          _title = null;

    
    public ChartRegion(ChartPanel parent)
    {
        super(parent);
        
        _rectBounds = new Rectangle(0, 0, 100, 100);

        init();
    }
    
    private void init()
    {
        _axisArea = new Margin(60, 50, 60, 50);

        _plotList = new ArrayList<Plot>();

        // 기본 X축(X) 추가
        _defaultAxes[0] = new NumericAxis(ChartPanel._axisName[0], chartWnd(), this, true);

        // 기본 Y축(Y) 추가
        _defaultAxes[1] = new NumericAxis(ChartPanel._axisName[1], chartWnd(), this, false);
        
        // 기본 오른쪽 축(Y) 추가
        _defaultAxes[2] = new NumericAxis(ChartPanel._axisName[2], chartWnd(), this, false, false);
        _defaultAxes[2].setVisible(false);

        // 기본 위쪽축(X) 추가
        _defaultAxes[3] = new NumericAxis(ChartPanel._axisName[3], chartWnd(), this, true, false);
        _defaultAxes[3].setVisible(false);
    }
    
    public Rectangle getBounds()
    {
        return _rectBounds;
    }
    
    public void setBounds(Rectangle rect)
    {
        _rectBounds = rect;
        
        recalculateLayout(false);
    }
    
    public void setBounds(int x, int y, int width, int height)
    {
        setBounds(new Rectangle(x, y, width, height));
    }
    
    /**
     * 실제 차트가 그려지는 영역의 위치 및 크기 반환.
     * 제목, 축 등의 영역이 빠진 Plot이 그려지는 영역만 반환함.
     */
    public Rectangle getChartingBounds()
    {
        return _chartingRect;
    }
    
    /**
     * 제목박스을 제외한 차트 영역의 크기 반환. 제목박스를 표시하지 않는다면 _rectBounds와 같음 
     * @return
     */
    public Rectangle getChartRegionBounds()
    {
        return _rectChart;
    }
    
    /**
     * 축이 그려지는 영역을 설정함. 각 축은 지정된 영역 내에만 그려지며, 축이 invisible 인 경우는 무시됨.
     * 축의 Tick 값이 짤리는 경우 이 값을 조정해서 짤리지 않도록 해야 함.
     * @param left      왼쪽 축이 그려지는 영역 너비
     * @param top       위쪽 축이 그려지는 영역 높이
     * @param right     오른쪽 축이 그려지는 영역 너비
     * @param bottom    아래쪽 축이 그려지는 영역 높이
     */
    public void setAxisArea(int left, int top, int right, int bottom)
    {
        _axisArea.setMargin(left, top, right, bottom);
    }
    
    public Axis getAxis(int axisIdx)
    {
        return _defaultAxes[axisIdx];
    }
    
    public Axis getAxisBottom()
    {
        return _defaultAxes[0];
    }
    
    public Axis getAxisLeft()
    {
        return _defaultAxes[1];
    }
    
    public Axis getAxisRight()
    {
        return _defaultAxes[2];
    }
    
    public Axis getAxisTop()
    {
        return _defaultAxes[3];
    }
    
    /**
     * 지정한 기본축을 DateTime 축으로 설정함.
     * @param axisIdx   축 인덱스. AXIS_LEFT, AXIS_RIGHT, AXIS_TOP, AXIS_BOTTOM에서 지정
     * @param title     축의 제목. null이면 기본 값이 할당됨.
     * @return
     */
    public Axis setDateTimeAxis(int axisIdx, String title)
    {
        Axis axis = new DateTimeAxis(
                  title == null ? ChartPanel._axisName[axisIdx] : title
                , chartWnd(), this
                , axisIdx == ChartPanel.AXIS_TOP || axisIdx == ChartPanel.AXIS_BOTTOM
                , axisIdx == ChartPanel.AXIS_LEFT || axisIdx == ChartPanel.AXIS_BOTTOM );

        axis.setTitleVisible(title != null);
        
        _defaultAxes[axisIdx] = axis;
         
        return axis;
    }
    
    public void clearAllPlot()
    {
        _plotList.clear();
    }
    
    /**
     * Region에 해당하는 제목상자를 표시할 지 여부 지정
     * @param b
     */
    public ChartRegion showTitleBox(boolean b)
    {
        _showTitleBox = b;
        
        recalculateLayout(false);
        
        return this;
    }
    
    public ChartRegion setRegionTitle(String title)
    {
        _title = title;
        
        return this;
    }
    
    /**
     * Plot을 추가하고 구분하기 위한 인덱스 반환.
     * X축과 Y축은 기본으로 제공되는 축이 자동으로 할당됨.
     * @param plot
     * @return
     */
    public int addPlot(Plot plot)
    {
        return addPlot(plot, ChartPanel.AXIS_BOTTOM, ChartPanel.AXIS_LEFT);
    }
    
    /**
     * Plot을 추가하고 구분하기 위한 인덱스 반환
     * @param plot
     * @param axisX X축 인덱스. AXIS_BOTTOM, AXIS_TOP 혹은 추가된 가로 축의 인덱스 지정.
     * @param axisY Y축 인덱스. AXIS_LEFT, AXIS_RIGHT 혹은 추가된 세로 축의 인덱스 지정.
     * @return
     */
    public int addPlot(Plot plot, int axisX, int axisY)
    {
        plot.setParent(chartWnd());
        plot.setRegion(this);
        
        // 축 지정
        plot.setAxisX(axisX).setAxisY(axisY);
        
        // 지정된 Color가 없을 경우 기본값을 설정함
        if( null == plot.getPlotColor() )
            plot.setPlotColor( option().plotColor(_plotList.size()) );
        
        _plotList.add(plot);
        
        // Layout 다시 잡고 모두 다시 그리도록 설정
        recalculateLayout(true);
        
        return _plotList.size() - 1;
    }
    
    /**
     * 차트를 새로 다시 그리도록 설정하기
     */
    protected void resetToRedraw()
    {
        if( _drawingImg != null )
        {
            _drawingImg.getGraphics().dispose();
        }
        
        _drawingImg = null;
        
        // TODO 다른 일을 할 거 없나?
    }
    
    /**
     * 추가된 Plot들의 최소/최대값을 고려하여 축의 Min/Max를 설정.
     */
    protected void recalculateMinMax()
    {
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
    }
    
    public void recalculateLayout(boolean calcAxisRange)
    {
        if( calcAxisRange )
            recalculateMinMax();

        Axis x = _defaultAxes[0];
        Axis y = _defaultAxes[1];
        Axis y2 = _defaultAxes[2];
        Axis x2 = _defaultAxes[3];
        
        Rectangle rect = _rectBounds;
        
        _rectChart = new Rectangle(rect);
        
        if( _showTitleBox )
        {
            // 제목을 그릴 영역만큼 조정함
            _rectChart.x += _titleBoxWidth;
            _rectChart.width -= _titleBoxWidth;
        }
        
        _chartingRect = new Rectangle(_rectChart);
        
        Margin adjMargin = new Margin(5, 5);
        
        if( y.isVisible() )
            adjMargin.setLeft(_axisArea.getLeft());
        
        if( x.isVisible() )
            adjMargin.setBottom(_axisArea.getBottom());
        
        if( y2.isVisible() )
            adjMargin.setRight(_axisArea.getRight());
        
        if( x2.isVisible() )
            adjMargin.setTop(_axisArea.getTop());
        
        _chartingRect.x += adjMargin.getLeft();
        _chartingRect.width -= adjMargin.getLeftRight();
        _chartingRect.y += adjMargin.getTop();
        _chartingRect.height -= adjMargin.getTopBottom();
        
        
        x.setPosition(_chartingRect.y + _chartingRect.height);
        x.setStartPosition(_chartingRect.x);
        x.setEndPosition(_chartingRect.x + _chartingRect.width);
        
        y.setPosition(_chartingRect.x);
        y.setStartPosition(_chartingRect.y);
        y.setEndPosition(_chartingRect.y + _chartingRect.height);
        
        y2.setPosition(_chartingRect.x + _chartingRect.width);
        y2.setStartPosition(_chartingRect.y);
        y2.setEndPosition(_chartingRect.y + _chartingRect.height);
        
        x2.setPosition(_chartingRect.y);
        x2.setStartPosition(_chartingRect.x);
        x2.setEndPosition(_chartingRect.x + _chartingRect.width);
        
        // 모두 다시 그리도록 설정
        resetToRedraw();
    }
    
    protected void shiftAxisPos(int offsetX, int offsetY)
    {
        for(Axis axis : _defaultAxes)
        {
            if( axis.isHorizontal() )
            {
                axis.setPosition(axis.getPosition() - offsetY);
                axis.setStartPosition(axis.getStartPosition() - offsetX);
                axis.setEndPosition(axis.getEndPosition() - offsetX);
            }
            else
            {
                axis.setPosition(axis.getPosition() - offsetX);
                axis.setStartPosition(axis.getStartPosition() - offsetY);
                axis.setEndPosition(axis.getEndPosition() - offsetY);
            }
        }
    }
    
    /**
     * 축의 현재 표시상태를 저장함.
     */
    protected void captureAxisState()
    {
        for(Axis axis : _defaultAxes)
            axis.captureState();
    }

    /**
     * captureAxisState()로 최근에 저장된 축의 표시상태를 다시 가져와 설정함.
     */
    protected void resetToLastCaptured()
    {
        for(Axis axis : _defaultAxes)
            axis.resetToLastCaptured();
    }
    
    /**
     * 축의 표시상태를 초기 최소/최대값으로 초기화함.
     */
    protected void resetToAxisInit()
    {
        for(Axis axis : _defaultAxes)
            axis.resetToInit();
    }

    /**
     * 수평 축의 값을 x2과 x1의 차이에 해당하는 값만큼 최소/최대값을 이동시키고,
     * 수직 축은 y2과 y1의 차이에 해당하는 값만큼 이동시킴.
     */
    protected void translateAxis(int x1, int x2, int y1, int y2)
    {
        for(Axis axis : _defaultAxes)
        {
            double gap = axis.isHorizontal()
                       ? axis.pixelToValue(x1) - axis.pixelToValue(x2)
                       : axis.pixelToValue(y1) - axis.pixelToValue(y2);

            axis.setDiplayMinMax(axis.getCapturedMin() + gap, axis.getCapturedMax() + gap);
        }
    }

    protected void zoomAxis(int x1, int x2, int y1, int y2)
    {
        for(Axis axis : _defaultAxes)
        {
            double minVal = axis.isHorizontal() ? axis.pixelToValue(x1) : axis.pixelToValue(y2);
            double maxVal = axis.isHorizontal() ? axis.pixelToValue(x2) : axis.pixelToValue(y1);
            
            axis.setDiplayMinMax(minVal, maxVal);
        }
    }
    
    private BufferedImage getDrawAll()
    {
        if( _drawingImg != null )
            return _drawingImg;
        
        Rectangle rectWnd = _rectChart;

        _drawingImg = new BufferedImage(rectWnd.width, rectWnd.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = _drawingImg.createGraphics();
        
        Rectangle rectChart = new Rectangle(_chartingRect);
        
        rectChart.x -= rectWnd.x;
        rectChart.y -= rectWnd.y;

        // Clip 영역 설정
        g2.setClip(rectChart);
        
        g2.setColor(option().chartBgColor());
        g2.fill(rectChart);

        // Anti-Aliasing
        g2.setRenderingHints(chartWnd().renderingHint());
        
        Composite oldComp = g2.getComposite();
        
        // Alpha-Blending
        g2.setComposite(chartWnd().alphaComposite());

        // 축 위치를 바꿨다가...
        shiftAxisPos(rectWnd.x, rectWnd.y);
        
        // Plot들 그리고
        for(Plot plot : _plotList)
        {
            if( plot.isVisible() )
                plot.draw(g2);
        }
        
        // 원래 위치로 돌려줌
        shiftAxisPos(-rectWnd.x, -rectWnd.y);
        
        g2.setClip(null);
        g2.setComposite(oldComp);
        
        return _drawingImg;
    }
    
    protected void draw(Graphics2D g2, int offsetX, int offsetY) 
    {
        if( _rectChart == null || _rectChart.width <= 0 )
            return;
        
        // g2는 전체임 Canvas를 나타냄.
        
        // 차팅된 이미지 가져오기
        BufferedImage buffImg = getDrawAll();
        
        Rectangle rect = _rectChart;
        
        g2.setClip(_chartingRect);
        g2.drawImage(buffImg, null, rect.x + offsetX, rect.y + offsetY);

        // 축 그리기
        // 클립을 조금 넓게 잡은 이유는 경계선에 있는 TickValue가 짤리는 경우를 방지하기 위해서임.
        g2.setClip(rect.x - 30, rect.y - 30, rect.width + 60, rect.height + 60);

        for(Axis axis : _defaultAxes)
        {
            axis.draw(g2, axis.isVisible() ? 3 : 2);
        }
        
        if( _showTitleBox )
        {
            Rectangle rectBox = new Rectangle(_rectBounds.x, _rectBounds.y
                    , _titleBoxWidth - 5, _rectBounds.height );
            
            g2.setClip(rectBox.x, rectBox.y, rectBox.width + 1, rectBox.height + 1);
            
            g2.drawRect(rectBox.x, rectBox.y, rectBox.width, rectBox.height);
            
            if( _title != null )
            {
                g2.setFont(option().titleBoxFont());
                
                DrawingTool.drawString( g2, _title
                        , rectBox.x + rectBox.width / 2
                        , rectBox.y + rectBox.height / 2
                        , 0, 1, 1 );
            }
        }
        
        g2.setClip(null);
    }
}
