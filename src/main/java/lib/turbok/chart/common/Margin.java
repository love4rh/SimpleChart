package lib.turbok.chart.common;


public class Margin implements Cloneable
{
    private int     _top, _left, _bottom, _right;
    
    public Margin(int left, int top, int right, int bottom)
    {
        setMargin(left, top, right, bottom);
    }
    
    public Margin(int leftRight, int topBottom)
    {
        _left = _right = leftRight;
        _top = _bottom = topBottom;
    }
    
    @Override
    public Margin clone()
    {
        return new Margin(_left, _top, _right, _bottom);
    }
    
    public int getLeft()
    {
        return _left;
    }
    
    public int getTop()
    {
        return _top;
    }
    
    public int getRight()
    {
        return _right;
    }
    
    public int getBottom()
    {
        return _bottom;
    }
    
    public void setLeft(int value)
    {
        _left = value;
    }
    
    public void setTop(int value)
    {
        _top = value;
    }
    
    public void setRight(int value)
    {
        _right = value;
    }
    
    public void setBottom(int value)
    {
        _bottom = value;
    }
    
    /**
     * 왼쪽 + 오른쪽 마진
     * @return
     */
    public int getLeftRight()
    {
        return _left + _right;
    }
    
    /**
     * 위쪽 + 아래쪽 마진
     * @return
     */
    public int getTopBottom()
    {
        return _top + _bottom;
    }
    
    public void setMargin(int left, int top, int right, int bottom)
    {
        _left = left;
        _top = top;
        _right = right;
        _bottom = bottom;
    }
    
    public void shift(int left, int top, int right, int bottom)
    {
        _left += left;
        _top += top;
        _right += right;
        _bottom += bottom;
    }
}
