package lib.turbok.chart.data;

import java.util.ArrayList;
import java.util.List;



public class XMemoryValues implements XValues
{
    private List<Double>    _values = null;
    
    private double          _minimum = Double.MAX_VALUE;
    
    private double          _maximum = -Double.MAX_VALUE;
    
    
    public XMemoryValues()
    {
        _values = new ArrayList<Double>();
    }
    
    public void addValue(double value)
    {
        if( _minimum > value )
            _minimum = value;
        
        if( _maximum < value )
            _maximum = value;
        
        _values.add(value);
    }
    
    @Override
    public boolean isEmpty()
    {
        return _values.isEmpty();
    }
    
    @Override
    public int getCount()
    {
        return _values.size();
    }
    
    @Override
    public double getValue(int index)
    {
        return _values.get(index);
    }
    
    @Override
    public double getMinimum()
    {
        return _minimum;
    }
    
    @Override
    public double getMaximum()
    {
        return _maximum;
    }
    
    @Override
    public String getTooltip(int index)
    {
        return null;
    }
    
    @Override
    public String getValueLabel(int index)
    {
        return null;
    }
    
}
