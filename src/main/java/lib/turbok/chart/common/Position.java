package lib.turbok.chart.common;



/**
 * This class is designed for defining the position of a chart object,
 * such as axis, series, legend and so on.
 * 
 * 위치 지정 방법: 상대적인 위치, 기준에서 얼마나 떨어져 있는 지 절대 위치 지정.
 *  
 * @author TurboK
 */
public class Position
{
    /**
     * The definition of positioning type.
     * @author TurboK
     */
    public static enum Type
    {
        /** define as relative position */
        RELATIVE
        /** 시작 위치부터 얼마만큼 떨어져 있는 지 Pixel 단위로 지정 */
        , START
        /** 끝 위치부터 얼마만큼 떨어져 있는 지 Pixel 단위로 지정 */
        , END
    }
    
    private Type        _type = Type.RELATIVE;
    
    private double      _position = 0.0;
    
    
    public Position()
    {
        //
    }
    
    public Position(double position, Type type)
    {
        _type = type;
        _position = position;
    }
    
    /**
     * 상대적 비율로 위치 지정.
     * @param ratio real number ranged in [0, 1].
     */
    public void setRelativePosition(double ratio)
    {
        _type = Type.RELATIVE;
        _position = ratio;
    }
    
    /**
     * 절대값으로 위치 지정
     * @param position      위치값
     * @param fromStart     true면 시작부터, false면 end에서
     */
    public void setAbsolutePosition(int position, boolean fromStart)
    {
        _position = position;
        _type = fromStart ? Type.START : Type.END;
    }
    
    public int positionAsPixel(int start, int length)
    {
        int pos = 0;
        
        switch( _type )
        {
        case RELATIVE:
            pos = (int) (_position * length + 0.5) + start;
            break;
            
        case START:
            pos = (int) _position;
            break;
            
        case END:
            pos = length - (int) _position + start;
        }
        
        return pos;
    }
    
    /**
     * start 위치를 0으로 간주하고 위치 계산 
     * @param length
     * @return
     */
    public int positionAsPixel(int length)
    {
        return positionAsPixel(0, length);
    }
}
