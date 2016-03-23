package lib.turbok.chart.plot;

import java.awt.Graphics2D;

import lib.turbok.chart.data.XYValues;



public class ScatterPlot extends XYValuesPlot
{
    // TODO 포인트 Shape
    // Line 그리기 여부
    
    public ScatterPlot(XYValues values)
    {
        super(values);
    }

    @Override
    public void draw(Graphics2D g2)
    {
        if( _values == null || _values.isEmpty() )
            return;
        
        g2.setColor(this.getPlotColor());

        for(int index = 0; index < _values.getCount(); ++index)
        {
            int x = axisX().valueToPixel(_values.getX(index));
            int y = axisY().valueToPixel(_values.getY(index));
            
            g2.fillRect(x - 2, y - 2, 5, 5);
        }
    }
    
}
