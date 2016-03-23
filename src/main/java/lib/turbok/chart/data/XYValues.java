package lib.turbok.chart.data;


public interface XYValues
{
    public boolean isEmpty();
    
    public int getCount();
    
    public double getX(int index);

    public double getY(int index);
    
    public double getMinimumX();
    
    public double getMaximumX();
    
    public double getMinimumY();
    
    public double getMaximumY();
    
    public String getTooltip(int index);
    
    public String getValueLabel(int index);
}
