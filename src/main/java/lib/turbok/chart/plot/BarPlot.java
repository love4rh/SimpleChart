package lib.turbok.chart.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

import lib.turbok.chart.data.XYValues;



public class BarPlot extends XYValuesPlot
{
    private AlphaComposite  _alphaComp = null;
    
    private int     _barWidth = 5;
    
    
    public BarPlot(XYValues values)
    {
        super(values);
        
        _alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5);
    }

    @Override
    public void draw(Graphics2D g2)
    {
        if( _values == null || _values.isEmpty() )
            return;
        
        Composite oldComp = g2.getComposite();
        
        // Alpha-Blending
        g2.setComposite(_alphaComp);
        
        Color lineColor = this.getPlotColor().darker();
        
        int halfWidth = _barWidth >> 1;
        int y1 = axisY().valueToPixel(Math.max(0.0, axisY().getDispMin()));
        
        for(int index = 0; index < _values.getCount(); ++index)
        {
            int x = axisX().valueToPixel(_values.getX(index));
            int y = axisY().valueToPixel(_values.getY(index));
            
            g2.setColor(this.getPlotColor());
            g2.fillRect(x - halfWidth, y, _barWidth, y1 - y);
            
            g2.setColor(lineColor);
            g2.drawRect(x - halfWidth, y, _barWidth, y1 - y);
        }
        
        g2.setComposite(oldComp);
    }
    
}
