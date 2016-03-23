package lib.turbok.chart.data;


public interface XValues
{
    public boolean isEmpty();
    
    public int getCount();
    
    public double getValue(int index);
    
    public double getMinimum();
    
    public double getMaximum();
    
    public abstract String getTooltip(int index);
    
    public abstract String getValueLabel(int index);
}
