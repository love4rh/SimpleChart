package lib.turbok.chart.axis;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import lib.turbok.chart.ChartObject;
import lib.turbok.chart.ChartRegion;
import lib.turbok.chart.ChartPanel;



/**
 * Axis class, which is very important in a chart control.
 * TODO Manual Scaling.
 * @author TurboK
 */
public abstract class Axis extends ChartObject
{
    private ChartRegion     _region = null;
    
    /** 가로축인지 여부 */
    private boolean         _isHorizontal = false;
    
    /** 차트 상에서 축의 위치 */
    private int             _position = 0;
    
    /** 축 표시 범위 */
    private int             _startPosition = 0;
    private int             _endPosition = 0;
    
    /**
     * Tick 표시 위치. true면 Left 혹은 Bottom에 표시.
     * false면 Right 혹은 Top에 표시
     */
    private boolean         _normalLegend = true;
    
    /** 연결된 Plot의 값들 중 최소값 */
    private double          _minimum = 0.0;
    
    /** 연결된 Plot의 값들 중 최대값 */
    private double          _maximum = 100.0;
    
    /** 현재 표시되고 있는 영역의 최소값 */
    private double          _dispMin = 0.0;
    
    /** 현재 표시되고 있는 영역의 최대값 */
    private double          _dispMax = 100.0;
    
    private double          _capturedMin = 0.0;
    
    private double          _capturedMax = 0.0;
    
    /** Tick을 표시하기 위한 간격값 */
    private double          _tickGapValue = 10.0;
    
    /** 자동 Scaling 여부. true이면 _minimum과 _dispMax 값이 자동으로 설정됨. */
    private boolean         _autoScale = false;
    
    /** 최대, 최소값을 자동으로 설정할 지 여부 */
    private boolean         _autoRange = true;
    
    private Stroke          _stroke = null;
    
    private String          _title = null;
    
    private boolean         _showTitle = true;
    
    /** 값 표시용 Tick 길이의 절반 */
    private int             _tickLength = 2;
    
    /** Tick 값 표시 여부 */
    private boolean         _showTickValue = true;


    public Axis(String title, ChartPanel chartWnd, ChartRegion region, boolean isHorizontal)
    {
        this(title, chartWnd, region, isHorizontal, true);
    }
    
    public Axis( String title, ChartPanel chartWnd, ChartRegion region
               , boolean isHorizontal, boolean normalLegend )
    {
        super(chartWnd);
        
        _region = region;
        _title = title;
        _isHorizontal = isHorizontal;
        _normalLegend = normalLegend;
        
        _stroke = new BasicStroke((float) 1.0);
    }
    
    public boolean isHorizontal()
    {
        return _isHorizontal;
    }
    
    public String getTitle()
    {
        return _title;
    }
    
    public void setTitle(String title)
    {
        _title = title;
    }
    
    public boolean isTitleVisible()
    {
        return _showTitle && _title != null && !_title.isEmpty();
    }
    
    public Axis setTitleVisible(boolean b)
    {
        _showTitle = b;
        
        return this;
    }
    
    public boolean isTickValueVisible()
    {
        return _showTickValue;
    }
    
    public void setTickValueVisible(boolean b)
    {
        _showTickValue = b;
    }
    
    public int getPosition()
    {
        return _position;
    }
    
    public int getStartPosition()
    {
        return _startPosition;
    }
    
    public int getEndPosition()
    {
        return _endPosition;
    }
    
    public final void setPosition(int position)
    {
        _position = position;
    }
    
    public final void setStartPosition(int position)
    {
        _startPosition = position;
    }
    
    public final void setEndPosition(int position)
    {
        _endPosition = position;
    }
    
    public Axis setMinimum(double value)
    {
        _capturedMin = _minimum = _dispMin = value;
        
        return this;
    }
    
    public Axis setMaximum(double value)
    {
        _capturedMax = _maximum = _dispMax = value;
        
        return this;
    }
    
    public Axis setMinimumMaximum(double minVal, double maxVal)
    {
        _capturedMin = _minimum = minVal;
        _capturedMax = _maximum = maxVal;
        
        setDiplayMinMax(minVal, maxVal);
        
        return this;
    }
    
    public Axis setDiplayMinMax(double minVal, double maxVal)
    {
        _dispMin = minVal;
        _dispMax = maxVal;
        
        return this;
    }
    
    public double getCapturedMin()
    {
        return _capturedMin;
    }
    
    public double getCapturedMax()
    {
        return _capturedMax;
    }
    
    /**
     * 최소/최대값을 초기상태로 돌리기. 확대 취소가 됨.
     */
    public void resetToInit()
    {
        setDiplayMinMax(_minimum, _maximum);
    }
    
    /**
     * 마지막으로 Capture한 Display 상태로 이동하기
     */
    public void resetToLastCaptured()
    {
        setDiplayMinMax(_capturedMin, _capturedMax);
    }
    
    public void captureState()
    {
        _capturedMin = _dispMin;
        _capturedMax = _dispMax;
    }
    
    public Axis setTickGap(double value)
    {
        _tickGapValue = value;
        
        return this;
    }
    
    public double getTickGap()
    {
        return _tickGapValue;
    }
    
    public double getMimimum()
    {
        return _minimum;
    }
    
    public double getMaximum()
    {
        return _maximum;
    }
    
    public double getDispMin()
    {
        return _dispMin;
    }
    
    public double getDispMax()
    {
        return _dispMax;
    }
    
    public Axis setAutoScaling(boolean b)
    {
        _autoScale = b;
        
        return this;
    }
    
    public boolean isAutoScaling()
    {
        return _autoScale;
    }
    
    public Axis setAutoRange(boolean b)
    {
        _autoRange = b;
        
        return this;
    }
    
    public boolean isAutoRange()
    {
        return _autoRange;
    }
    
    public Axis setTickLength(int length)
    {
        _tickLength = length;
        
        return this;
    }
    
    public int getTickLength()
    {
        return _tickLength;
    }
    
    public int valueToPixel(double value)
    {
        double start = _startPosition;
        double length = _endPosition - _startPosition;
        double factor = (_isHorizontal ? (value - _dispMin) : (_dispMax - value))
                        / (_dispMax - _dispMin);

        return (int) (factor * length + start);
    }

    public double pixelToValue(int pixel)
    {
        double start = _startPosition;
        double length = _endPosition - _startPosition;
        double factor = (_isHorizontal ? (pixel - start) : (length + start - pixel)) / length;
        
        return factor * (_dispMax - _dispMin) + _dispMin;
    }

    private void drawHorizonal(Graphics2D g2, int dispType)
    {
        int y = _position;
        int x1 = _startPosition;
        int x2 = _endPosition;

        g2.drawLine(x1, y, x2, y);
        
        if( dispType < 2 )
            return;
        
        g2.setFont(option().axisTickFont());

        int maxTickHeight = 0;
        FontMetrics tickMetric = g2.getFontMetrics(option().axisTickFont());

        g2.setColor(Color.black);
        
        for(Double value = startTickNavigate(tickMetric, g2);
                value != null && value <= _dispMax; value = nextTickValue(value))
        {
            int x = valueToPixel(value);

            g2.drawLine(x, y - _tickLength, x, y + _tickLength);
            
            if( dispType >= 3 && isTickValueVisible() )
            {
                Rectangle tickRect = _normalLegend 
                                   ? DrawingTool.drawString(g2, valueToString(value), x, y + _tickLength + 7, 0, 1, 0)
                                   : DrawingTool.drawString(g2, valueToString(value), x, y, 0, 1, 2);
    
                if( maxTickHeight < tickRect.height )
                    maxTickHeight = tickRect.height;
            }
        }
        
        // 축 Title 그리기
        if( dispType >= 3 && isTitleVisible() )
        {
            FontMetrics titleMetric = g2.getFontMetrics(option().axisFont());
            
            g2.setFont(option().axisFont());
            
            if( _normalLegend )
                DrawingTool.drawStringAtCenter(g2, _title, (x1 + x2) >> 1, y + _tickLength + maxTickHeight + 5, 0);
            else
                DrawingTool.drawStringAtCenter(g2, _title, (x1 + x2) >> 1, y - _tickLength - maxTickHeight - 2 - titleMetric.getHeight(), 0);
        }
    }
    
    private void drawVertical(Graphics2D g2, int dispType)
    {
        int x = _position;
        int y1 = _startPosition;
        int y2 = _endPosition;

        g2.drawLine(x, y1, x, y2);
        
        if( dispType < 2 )
            return;
        
        g2.setFont(option().axisTickFont());
        
        FontMetrics tickMetric = g2.getFontMetrics(option().axisTickFont());
        int adjY = Math.max(5, tickMetric.getLeading());

        g2.setColor(Color.black);
        
        for(Double value = startTickNavigate(tickMetric, g2);
                value != null && value <= _dispMax; value = nextTickValue(value))
        {
            int y = valueToPixel(value);

            g2.drawLine(x - _tickLength, y, x + _tickLength, y);
            
            if( dispType >= 3 && isTickValueVisible() )
            {
                if( _normalLegend )
                    DrawingTool.drawString(g2, valueToString(value), x - _tickLength - 3, y + adjY, 0, 2, 1);
                else
                    DrawingTool.drawString(g2, valueToString(value), x + _tickLength + 3, y + adjY, 0, 0, 1);
            }
        }
        
        // 축 Title 그리기
        if( dispType >= 3 && isTitleVisible() )
        {
            g2.setFont(option().axisFont());
            
            Rectangle rect = _region.getChartRegionBounds();
            FontMetrics titleMetric = g2.getFontMetrics(option().axisFont());
            
            if( _normalLegend )
                DrawingTool.drawStringAtCenter(g2, _title, rect.x + titleMetric.getHeight() - 4, (y2 + y1) >> 1, -90);
            else
                DrawingTool.drawStringAtCenter(g2, _title, rect.x + rect.width - titleMetric.getHeight() + 4, (y2 + y1) >> 1, 90);
        }
    }
    
    /**
     * Draw axis.
     * @param g2        Graphic Device Context
     * @param dispType  0: 안 그림, 1: Line만, 2: Line과 Tick만, 3: Line, Tick, Label
     */
    public void draw(Graphics2D g2, int dispType)
    {
        if( dispType == 0 )
            return;
        
        g2.setColor(Color.black);   // TODO 변경가능
        g2.setStroke(_stroke);      // TODO 변경가능
        
        if( _isHorizontal )
            drawHorizonal(g2, dispType);
        else
            drawVertical(g2, dispType);
    }
    
    /**
     * 축을 표시하기 위한 너비를 Pixel 단위로 반환.
     * @param g2
     * @return
     */
    public abstract int getPreferredBreadth(Graphics2D g2);
    
    /**
     * 축의 Tick 값을 표시할 때 첫 번째 값 바환
     * @param tickMetric
     * @param g2
     * @return
     */
    protected abstract Double startTickNavigate(FontMetrics tickMetric, Graphics2D g2);
    
    /**
     * 다음 Tick 값 반환. 더 이상 표시할 Tick이 없다면 null 반환
     * @param value     바로 직전 Tick의 값
     * @return
     */
    protected abstract Double nextTickValue(Double value);
    
    /**
     * 값을 화면에 표시하기 위한 문자열로 변환
     * @param value
     * @return
     */
    public abstract String valueToString(double value);
}
