package lib.turbok.chart.plot;

import java.awt.Graphics2D;

import lib.turbok.chart.data.XYValues;



public class LinePlot extends XYValuesPlot
{
    // TODO 포인트 Shape
    // Line 그리기 여부
    // Stroke
    
    public LinePlot(XYValues values)
    {
        super(values);
    }

    @Override
    public void draw(Graphics2D g2)
    {
        if( _values == null || _values.isEmpty() )
            return;
        
        g2.setColor(this.getPlotColor());

        int px = axisX().valueToPixel(_values.getX(0));
        int py = axisY().valueToPixel(_values.getY(0));

        for(int index = 1; index < _values.getCount(); ++index)
        {
            int x = axisX().valueToPixel(_values.getX(index));
            int y = axisY().valueToPixel(_values.getY(index));
            
            g2.drawLine(px, py, x, y);
            // g2.fillRect(x - 2, y - 2, 5, 5);
            
            px = x; py = y;
        }
    }
    
}
