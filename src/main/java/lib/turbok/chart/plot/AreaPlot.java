package lib.turbok.chart.plot;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import lib.turbok.chart.data.XYValues;



public class AreaPlot extends XYValuesPlot
{
    private AlphaComposite  _alphaComp = null;
    
    
    public AreaPlot(XYValues values)
    {
        super(values);
        
        _alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5);
    }

    @Override
    public void draw(Graphics2D g2)
    {
        if( _values == null || _values.isEmpty() )
            return;
        
        g2.setColor(this.getPlotColor());

        GeneralPath path = new GeneralPath();
        
        int x1 = axisX().valueToPixel(_values.getX(0));
        int y1 = axisY().valueToPixel(Math.max(0.0, axisY().getDispMin()));
        int x = x1;
        int y = axisY().valueToPixel(_values.getY(0));

        path.moveTo(x1, y1);
        path.lineTo(x, y);

        for(int index = 1; index < _values.getCount(); ++index)
        {
            x = axisX().valueToPixel(_values.getX(index));
            y = axisY().valueToPixel(_values.getY(index));
            
            path.lineTo(x, y);
        }
        
        path.lineTo(x, y1);
        path.lineTo(x1, y1);
        
        Composite oldComp = g2.getComposite();
        
        // Alpha-Blending
        g2.setComposite(_alphaComp);
        
        g2.fill(path);
        
        g2.setComposite(oldComp);
        
        g2.draw(path);
    }
    
}
