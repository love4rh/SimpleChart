package lib.turbok.chart.data;



public class X1X2YMemoryValues extends X1X2YValues
{
    private XMemoryValues   _x1 = null;
    private XMemoryValues   _x2 = null;
    private XMemoryValues   _y = null;


    public X1X2YMemoryValues()
    {
        _x1 = new XMemoryValues();
        _x2 = new XMemoryValues();
        _y = new XMemoryValues();
    }
    
    public void addValue(double x1, double x2, double y)
    {
        _x1.addValue(x1);
        _x2.addValue(x2);
        _y.addValue(y);
    }
    
    @Override
    public boolean isEmpty()
    {
        return _x1.isEmpty();
    }
    
    @Override
    public int getCount()
    {
        return _x1.getCount();
    }
    
    @Override
    public double getX(int index)
    {
        return _x1.getValue(index);
    }
    
    @Override
    public double getX2(int index)
    {
        return _x2.getValue(index);
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
        return _x1.getMinimum();
    }

    @Override
    public double getMaximumX()
    {
        return _x1.getMaximum();
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

    @Override
    public double getMinimumX2()
    {
        return _x2.getMinimum();
    }

    @Override
    public double getMaximumX2()
    {
        return _x2.getMaximum();
    }
}
