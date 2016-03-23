package lib.turbok.chart.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import lib.turbok.chart.data.X1X2YValues;
import lib.turbok.chart.data.XYValues;



public class RangePlot extends Plot
{
    private X1X2YValues     _values = null;
    
    private XYValues        _rangeValue = null;
    
    private boolean         _autoY = false;
    
    private int             _barWidth = 20;
    
    private int[]           _autoYPos = null;
    
    private AlphaComposite  _alphaComp = null;
    
    // TODO 포인트 Shape
    // Line 그리기 여부
    // Stroke
    
    public RangePlot(X1X2YValues values)
    {
        this(values, false);
    }
    
    public RangePlot(XYValues values)
    {
        _rangeValue = values;
        _autoY = true;
        _autoYPos = null;
    }
    
    public RangePlot(X1X2YValues values, boolean autoY)
    {
        _values = values;
        _autoY = autoY;
        _autoYPos = null;
        
        _rangeValue = new XYValues()
        {
            @Override
            public boolean isEmpty()
            {
                return _values == null ? true : _values.isEmpty();
            }

            @Override
            public int getCount()
            {
                return _values.getCount();
            }

            @Override
            public double getX(int index)
            {
                return _values.getX(index);
            }

            @Override
            public double getY(int index)
            {
                return _values.getX2(index);
            }

            @Override
            public String getTooltip(int index)
            {
                return _values.getTooltip(index);
            }

            @Override
            public String getValueLabel(int index)
            {
                return _values.getValueLabel(index);
            }

            @Override
            public double getMinimumX() { return 0; }
            
            @Override
            public double getMaximumX() { return 0; }

            @Override
            public double getMinimumY() { return 0; }

            @Override
            public double getMaximumY() { return 0; }
        };
    }
    
    public void setBarWidth(int width)
    {
        _barWidth = width;
    }
    
    /**
     * @param ratio     1.0: Opaque, 0.0: Transparent
     */
    public void setBarTransparency(double ratio)
    {
        _alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) ratio);
    }

    /**
     * 두 선분 [x1, x2], [p1, p2]가 같은 선상에 있다고 할 때 겹치는 영역 계산하여 반환.
     */
    private double calcOverlapLength(double x1, double x2, double p1, double p2)
    {
        if( x2 <= p1 || p2 <= x1 )
            return 0;

        return Math.min(x2,  p2) - Math.max(x1, p1);
    }

    /**
     * 표시할 Bar가 되도록 겹치지 않도록 하는 Y의 위치를 계산하여 저장
     * @return 정상으로 계산된 경우 true. 그렇지 않으면 false.
     */
    private boolean recalcAutoYPos()
    {
        if( !_autoY )
            return true;
        
        int minYPos = axisY().valueToPixel(axisY().getDispMax()) + _barWidth;
        int maxYPos = axisY().valueToPixel(axisY().getDispMin()) - _barWidth;
        
        int yPosCount = (maxYPos - minYPos) / (int) (_barWidth * 1.5);
        
        if( yPosCount <= 0 )
            return false;
        
        _autoYPos = new int[_rangeValue.getCount()];

        // 첫 번째 것은 무조건 제일 위에 위치시킴
        _autoYPos[0] = 1;
        
        List<ArrayList<Integer>> yIndex = new ArrayList<ArrayList<Integer>>();
        for(int i = 0; i < yPosCount; ++i)
            yIndex.add( new ArrayList<Integer>() );
        
        yIndex.get(0).add(0);

        for(int index = 1; index < _rangeValue.getCount(); ++index)
        {
            double x1 = _rangeValue.getX(index);
            double x2 = _rangeValue.getY(index);
            
            int yPos = 0;
            double overlapMin = Double.MAX_VALUE;
            
            for(int i = 0; overlapMin > 0 && i < yPosCount; ++i)
            {
                double overlap = 0.0;
                for(Integer point : yIndex.get(i))
                {
                    double p1 = _rangeValue.getX(point);
                    double p2 = _rangeValue.getY(point);
                    
                    overlap += calcOverlapLength(x1, x2, p1, p2);
                }
                
                if( overlapMin > overlap )
                {
                    overlapMin = overlap;
                    yPos = i;
                }
            }
            
            // 겹치는 부분이 조금이라도 있다면 구분을 위하여 음수로 값을 입력함
            _autoYPos[index] = (overlapMin > 0) ? -(yPos + 1) : (yPos + 1);
            yIndex.get(yPos).add(index);
        }
        
        return true;
    }
    
    @Override
    public Double getMinimumX()
    {
        return _rangeValue.getMinimumX();
    }
    
    @Override
    public Double getMaximumX()
    {
        return _rangeValue.getMaximumX();
    }
    
    @Override
    public Double getMinimumY()
    {
        return _autoY ? null : _values.getMinimumY();
    }
    
    @Override
    public Double getMaximumY()
    {
        return _autoY ? null : _values.getMaximumY();
    }

    @Override
    public void draw(Graphics2D g2)
    {
        if( _rangeValue.isEmpty() )
            return;
        
        if( _autoY && _autoYPos == null )
        {
            if( !recalcAutoYPos() )
                return;
        }

        Color brighterColor = this.getPlotColor().brighter();
        int minYPos = axisY().valueToPixel(axisY().getDispMax()) + _barWidth;
        
        for(int index = 0; index < _rangeValue.getCount(); ++index)
        {
            int x1 = axisX().valueToPixel(_rangeValue.getX(index));
            int x2 = axisX().valueToPixel(_rangeValue.getY(index));
            int y = _autoY ? minYPos + (_autoYPos[index] < 0 ? 5 - _autoYPos[index] : (_autoYPos[index] - 1)) * (int) (_barWidth * 1.5)
                           : axisY().valueToPixel(_values.getY(index));

            g2.setColor(brighterColor);
            
            if( _alphaComp == null )
                g2.fillRect(x1, y - _barWidth / 2, x2 - x1, _barWidth);
            else
            {
                Composite oldComp = g2.getComposite();
                
                // Alpha-Blending
                g2.setComposite(_alphaComp);
                g2.fillRect(x1, y - _barWidth / 2, x2 - x1, _barWidth);
                
                g2.setComposite(oldComp);
            }
            
            g2.setColor(this.getPlotColor());
            g2.drawRect(x1, y - _barWidth / 2, x2 - x1, _barWidth);
        }
    }
}
