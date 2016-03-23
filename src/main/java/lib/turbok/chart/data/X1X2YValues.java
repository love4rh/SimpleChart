package lib.turbok.chart.data;

public abstract class X1X2YValues implements XYValues
{
    public double getX1(int index)
    {
        return getX(index);
    }
    
    public double getMinimumX1()
    {
        return getMinimumX();
    }
    
    public double getMaximumX1()
    {
        return getMaximumX();
    }
    
    public abstract double getX2(int index);
    
    public abstract double getMinimumX2();
    
    public abstract double getMaximumX2();
}
