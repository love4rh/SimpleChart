package lib.turbok.chart.common;


/**
 * 범위를 나타내기 위한 클래스. 시작과 끝 2개의 Position 값을 가지고 있음.
 * Axis의 시작 ~ 끝 위치 등을 나타내기 위하여 사용함.
 * 
 * @author TurboK
 */
public class Range
{
    private Position    _start = new Position();
    
    private Position    _end = new Position();
    
    
    public Range()
    {
        
    }
    
    public void setRelativeStartPosition(double ratio)
    {
        _start.setRelativePosition(ratio);
    }
    
    public void setRelativeEndPosition(double ratio)
    {
        _end.setRelativePosition(ratio);
    }
    
    public void setAbsoluteStartPosition(int position, boolean fromStart)
    {
        _start.setAbsolutePosition(position, fromStart);
    }
    
    public void setAbsoluteEndPosition(int position, boolean fromStart)
    {
        _end.setAbsolutePosition(position, fromStart);
    }
    
    public int getLength(int length)
    {
        return _end.positionAsPixel(0, length) - _start.positionAsPixel(0, length);
    }
    
    public int getStartPosition(int start, int length)
    {
        return _start.positionAsPixel(start, length);
    }
    
    public int getEndPosition(int start, int length)
    {
        return _end.positionAsPixel(start, length);
    }
    
    public Position getStartPosition()
    {
        return _start;
    }
    
    public Position getEndPosition()
    {
        return _end;
    }
}
