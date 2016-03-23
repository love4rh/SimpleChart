package lib.turbok.chart.axis;

import static lib.turbok.chart.common.ChartingTool.T;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import lib.turbok.chart.ChartRegion;
import lib.turbok.chart.ChartPanel;



/**
 * 숫자를 표현하는 축
 * @author TurboK
 */
public class NumericAxis extends Axis
{
    private double      _tickValueAuto = 0;
    private int         _tickDigit = 0;
    
    public NumericAxis(String title, ChartPanel chartWnd, ChartRegion region, boolean isHorizontal)
    {
        this(title, chartWnd, region, isHorizontal, true);
    }
    
    public NumericAxis(String title, ChartPanel chartWnd, ChartRegion region, boolean isHorizontal, boolean normalLegend)
    {
        super(title, chartWnd, region, isHorizontal, normalLegend);
    }

    /**
     * tick 간격값이 1이상이면 0, 미만이면 10^X에서 X를 양수로 변환하여 반환.
     * @param tickGap   Tick간의 간격값. 0보다 큰 숫자.
     * @return
     */
    private int calculateTickDigit(double tickGap)
    {
        int tickDigit = T.exponentDigit(tickGap);
        
        if( tickGap >= 1.0 )
            tickDigit = 0;
        else if( tickDigit < 0 )
            tickDigit = - tickDigit + 1;
        
        return tickDigit;
    }
    
    /**
     * tick 간격과 표시할 최소값을 고려했을 때 표시할 시작값을 계산하여 반환
     * @param tickGap
     * @return
     */
    private double calculateTickBegin(double tickGap)
    {
        double dispMin = this.getDispMin();
        double minVal = Math.round(dispMin / tickGap - 0.49999) * tickGap;

        if( minVal < dispMin )
            minVal += tickGap;
        
        return minVal;
    }
    
    /**
     * NumericAxis에서는 _tickDigit도 계산하여 가지고 있음.
     * @return 표시해야 할 범위를 표시할 때 가장 적당한 Tick 간의 간격을 계산하여 반환
     */
    private double calculateTickGap(FontMetrics tickMetric, Graphics2D g2)
    {
        int length = 0 ; //< 표시할 수 있는 Pixel 길이
        int tickWidth = 0;   // 한 Tick을 표시하는데 필요한 너비 혹은 높이
        
        if( isHorizontal() )
        {
            tickWidth = tickMetric.getStringBounds((T.valueToStr(getMaximum()) + "000"), g2).getBounds().width;
            tickWidth += tickMetric.getHeight();

            length = valueToPixel(getDispMax()) - valueToPixel(getDispMin());
        }
        else
        {
            tickWidth = tickMetric.getHeight();
            length = valueToPixel(getDispMin()) - valueToPixel(getDispMax());
        }
        
        int count = Math.max(1, (length + tickWidth - 1) / (tickWidth + tickMetric.getHeight()));
        double tickValue = (getDispMax() - getDispMin()) / count;
        
        int tickDigit = T.exponentDigit(tickValue);
        double adjPow = 1.0;
        
        if( tickDigit < 0 || tickDigit > 1 )
        {
            adjPow = Math.pow(10, 1 - tickDigit);
        }
        
        tickValue *= adjPow;
        
        if( tickValue < 3.0 )
            tickValue = 1.0;
        else if( tickValue < 7.0 )
            tickValue = 5.0;
        else if( tickValue < 14.0 )
            tickValue = 10.0;   
        else if( tickValue < 34.0 )
            tickValue = 25.0;
        else if( tickValue < 75.0 )
            tickValue = 50.0;
        else
            tickValue = 100.0;
        
        tickValue /= adjPow;
        
        _tickDigit = calculateTickDigit(tickValue);
        
        return tickValue;
    }
    
    /**
     * Tick 값을 표시하기에 적당한 크기를 계산하여 반환.
     */
    private Dimension calculatePrefferedTickSize(FontMetrics tickMetric, double tickValue, int tickDigit)
    {
        double dispMax = this.getDispMax();
        double adjVal = Math.round(dispMax / tickValue - 0.49999) * tickValue;

        if( adjVal < dispMax )
            adjVal += tickValue;

        char[] charSet = T.valueToStr(adjVal, tickDigit).toCharArray();
        
        return new Dimension( tickMetric.charsWidth(charSet, 0, charSet.length) + tickMetric.getHeight()
                            , tickMetric.getHeight());
    }
    
    @Override
    public int getPreferredBreadth(Graphics2D g2)
    {
        int breadth = 0;

        FontMetrics tickMetric = g2.getFontMetrics(option().axisTickFont());
        FontMetrics titleMetric = g2.getFontMetrics(option().axisFont());

        // 가로 축은 높이를 반환해야 함
        if( isHorizontal() )
        {
            breadth = getTickLength() + 5 + (isTickValueVisible() ? tickMetric.getHeight() : 0);
            
            // 타이틀을 표시한다면 아래 추가
            if( isTitleVisible() )
                breadth += titleMetric.getHeight();
        }
        // 세로 축은 너비를 반환
        else
        {
            double tickGap = calculateTickGap(tickMetric, g2);
            
            breadth = getTickLength() + 3
                    + (isTickValueVisible() ? calculatePrefferedTickSize(tickMetric, tickGap, _tickDigit).width
                                            : 0);
            
            // 타이틀을 표시한다면 아래 추가
            if( isTitleVisible() )
                breadth += titleMetric.getHeight();
        }
        
        return breadth;
    }

    @Override
    protected Double startTickNavigate(FontMetrics tickMetric, Graphics2D g2)
    {
        _tickValueAuto = calculateTickGap(tickMetric, g2);
        
        return calculateTickBegin(_tickValueAuto);
    }
    
    @Override
    protected Double nextTickValue(Double value)
    {
        return value + _tickValueAuto;
    }

    @Override
    public String valueToString(double value)
    {
        return T.valueToStr(value, _tickDigit);
    }
}
