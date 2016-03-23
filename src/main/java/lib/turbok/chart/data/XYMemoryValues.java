package lib.turbok.chart.data;


public class XYMemoryValues implements XYValues
{
    private XMemoryValues   _x = null;
    private XMemoryValues   _y = null;


    public XYMemoryValues()
    {
        _x = new XMemoryValues();
        _y = new XMemoryValues();
    }
    
    public void addValue(double x, double y)
    {
        _x.addValue(x);
        _y.addValue(y);
    }
    
    @Override
    public boolean isEmpty()
    {
        return _x.isEmpty() || _y.isEmpty();
    }
    
    @Override
    public int getCount()
    {
        return _x.getCount();
    }
    
    @Override
    public double getX(int index)
    {
        return _x.getValue(index);
    }
    
    @Override
    public double getY(int index)
    {
        return _y.getValue(index);
    }
    
    @Override
    public String getTooltip(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getValueLabel(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getMinimumX()
    {
        return _x.getMinimum();
    }

    @Override
    public double getMaximumX()
    {
        return _x.getMaximum();
    }

    @Override
    public double getMinimumY()
    {
        return _y.getMinimum();
    }

    @Override
    public double getMaximumY()
    {
        return _y.getMaximum();
    }
    
}
