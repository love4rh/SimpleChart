package lib.turbok.chart.axis;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import lib.turbok.chart.ChartRegion;
import lib.turbok.chart.ChartPanel;



/**
 * 날짜 축
 * @author TurboK
 */
public class DateTimeAxis extends Axis
{
    /**
     * 날짜/시간 표시 형식 지정. SimpleDateTimeFormat 클래스의 Format과 같은 형식으로 지정.
     * http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
     * 기본값: 2015-01-01\n00:00:00 임.
     */
    private String              _formatStr = "MM-dd HH:mm";
    
    private DateTimeFormatter   _formatter = null;
    
    private long                _tickGap = 60000;
    
    
    public DateTimeAxis(String title, ChartPanel chartWnd, ChartRegion region, boolean isHorizontal)
    {
        this(title, chartWnd, region, isHorizontal, true);
    }
    
    public DateTimeAxis(String title, ChartPanel chartWnd, ChartRegion region, boolean isHorizontal, boolean normalLegend)
    {
        super(title, chartWnd, region, isHorizontal, normalLegend);
        
        _formatter = DateTimeFormat.forPattern(_formatStr);
        
        // 기본 축 최소/최대 설정 (오류 방지용)
        long now = System.currentTimeMillis();
        long gap = 6 * 3600 * 1000;
        this.setMinimumMaximum(now - gap, now + gap);
    }
    
    /**
     * http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
     * TODO 이 값이 바뀌면 축의 너비를 새로 계산해야 할 수 있음
     * @param format
     */
    public void setFormat(String format)
    {
        _formatStr = format;
        _formatter = DateTimeFormat.forPattern(_formatStr);
    }
    
    @Override
    public int getPreferredBreadth(Graphics2D g2)
    {
        FontMetrics titleMetric = g2.getFontMetrics(option().axisFont());
        
        String sampleText = _formatter.print(System.currentTimeMillis());
        Rectangle rect = DrawingTool.calcTextMetrics(g2, sampleText, 2, option().axisTickFont());

        int breadth = 0;
        
        // 가로 축은 높이를 반환해야 함
        if( isHorizontal() )
        {
            breadth = getTickLength() + 5 + (isTickValueVisible() ? rect.height : 0);
            
            // 타이틀을 표시한다면 아래 추가
            if( isTitleVisible() )
                breadth += titleMetric.getHeight();
        }
        // 세로 축은 너비를 반환
        else
        {
            breadth = getTickLength() + 5 + (isTickValueVisible() ? rect.width : 0);
            
            // 타이틀을 표시한다면 아래 추가
            if( isTitleVisible() )
                breadth += titleMetric.getHeight() + 15;
        }
        
        return breadth;
    }

    /**
     * 날짜/시간 축이므로
     * 1초, 5초, 15초, 20초, 30초,
     * 1분, 5분, 15분, 20분, 30분,
     * 1시간, 2시간, 3시간, 6시간, 12시간,
     * 1일, 5일, 15일
     * 1달, 1년
     * @return 표시해야 할 범위를 표시할 때 가장 적당한 Tick 간의 간격을 계산하여 반환
     */
    @Override
    protected Double startTickNavigate(FontMetrics tickMetric, Graphics2D g2)
    {
        double dispMin = this.getDispMin();
        
        String sampleText = _formatter.print(System.currentTimeMillis());
        Rectangle tickRect = DrawingTool.calcTextMetrics(g2, sampleText, 2, option().axisTickFont());
        
        int tickWidth = (int) ((isHorizontal() ? tickRect.width : tickRect.height) * 1.2);
        double tickValue = Math.abs(pixelToValue(tickWidth * 2) - pixelToValue(tickWidth));
        long secGap = (long) Math.round(tickValue / 1000);
        
        // 초단위
        final long[] guidedGap = new long[] {
          1, 5, 10, 15, 20, 30, 60, 300, 600, 900, 1200, 1800, 3600, 7200, 10800, 21600, 43200, 86400
        };
        
        _tickGap = guidedGap[guidedGap.length - 1];

        // secGap 보다 크거나 같은 guidedGap의 값 중 가장 가까이 있는 값을 선택
        for(int i = 0; i < guidedGap.length; ++i)
        {
            if( guidedGap[i] > secGap )
            {
                _tickGap = guidedGap[i];
                break;
            }
        }
        
        _tickGap *= 1000;

        double minVal = (long) (dispMin / _tickGap) * _tickGap;

        if( minVal < dispMin )
            minVal += _tickGap;

        return minVal;
    }

    @Override
    protected Double nextTickValue(Double value)
    {
        return value + _tickGap;
    }

    @Override
    public String valueToString(double value)
    {
        return _formatter.print((long) value);
    }
}
