package lib.turbok.chart.plot;

import lib.turbok.chart.data.XYValues;



abstract class XYValuesPlot extends Plot
{
    protected XYValues      _values = null;
    
    public XYValuesPlot(XYValues values)
    {
        _values = values;
    }
    
    @Override
    public Double getMinimumX()
    {
        return _values.getMinimumX();
    }
    
    @Override
    public Double getMaximumX()
    {
        return _values.getMaximumX();
    }
    
    @Override
    public Double getMinimumY()
    {
        return _values.getMinimumY();
    }
    
    @Override
    public Double getMaximumY()
    {
        return _values.getMaximumY();
    }
}
