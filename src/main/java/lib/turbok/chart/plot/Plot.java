package lib.turbok.chart.plot;

import java.awt.Color;
import java.awt.Graphics2D;

import lib.turbok.chart.ChartObject;
import lib.turbok.chart.ChartRegion;
import lib.turbok.chart.axis.Axis;



public abstract class Plot extends ChartObject
{
    private ChartRegion _region = null;
    protected Color     _plotColor = null;
    
    private int         _xAxis = -1;
    private int         _yAxis = -1;
    
    
    public Plot()
    {
        //
    }
    
    public void setRegion(ChartRegion region)
    {
        _region = region;
    }
    
    /**
     * 가로 축 지정. ChartPanel에 등록된 축의 인덱스 중 하나를 선택하여야 함.
     * @param axisIdx
     * @return
     */
    public final Plot setAxisX(int axisIdx)
    {
        Axis axis = _region.getAxis(axisIdx);
        
        if( !axis.isHorizontal() )
            throw new IllegalArgumentException("X axis must be a Horizontal axis.");

        _xAxis = axisIdx;
        
        return this;
    }
    
    /**
     * 세로 축 지정. ChartPanel에 등록된 축의 인덱스 중 하나를 선택하여야 함.
     * @param axisIdx
     * @return
     */
    public final Plot setAxisY(int axisIdx)
    {
        Axis axis = _region.getAxis(axisIdx);
        
        if( axis.isHorizontal() )
            throw new IllegalArgumentException("Y axis must be a Vertical axis.");
        
        _yAxis = axisIdx;
        
        return this;
    }
    
    public final Axis axisX()
    {
        return _region.getAxis(_xAxis);
    }
    
    public final Axis axisY()
    {
        return _region.getAxis(_yAxis);
    }
    
    public final int getXAxisIdx()
    {
        return _xAxis;
    }
    
    public final int getYAxisIdx()
    {
        return _yAxis;
    }
    
    public final Color getPlotColor()
    {
        return _plotColor;
    }
    
    public final Plot setPlotColor(Color color)
    {
        _plotColor = color;
        
        return this;
    }
    
    /**
     * 차트형태에 맞게 그리기
     * @param g2
     */
    public abstract void draw(Graphics2D g2);
    
    public abstract Double getMinimumX();
    
    public abstract Double getMaximumX();
    
    public abstract Double getMinimumY();
    
    public abstract Double getMaximumY();
}
