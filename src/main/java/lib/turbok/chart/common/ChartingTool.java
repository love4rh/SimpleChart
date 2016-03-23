package lib.turbok.chart.common;


public enum ChartingTool
{
    T;
    
    private ChartingTool()
    {
        //
    }
    
    public String trimRight(final String string, final char trimChar)
    {
        final int lastChar = string.length() - 1;
        int i = lastChar;

        while(i >= 0 && string.charAt(i) == trimChar)
            i -= 1;
        
        return (i < lastChar) ? string.substring(0, i + 1) : string;
    }
    
    public String valueToStr(double value)
    {
        String s = trimRight(Double.valueOf(value).toString(), '0');
        
        return trimRight(s, '.');
    }

    public String valueToStr(double value, int digit)
    {
        if( digit == 0 )
            return valueToStr(value);

        String fmt = String.format("%%.%df", digit);
        
        return String.format(fmt, value);
    }
    
    /**
     * value 값이 10에 몇 승에서 처음 0이 아닌 수가 나오는 지 계산.
     * -100, 100 --> 2, 1 --> 0, 0.1 --> -1, 0 --> 0와 같이
     * @param value
     * @return
     */
    public int exponentDigit(double value)
    {
        value = Math.abs(value);

        if( 0 == Double.compare(value, 0) )
            return 0;
        
        int digit = 0;
        
        if( value < 1.0 )
            while( value < 1.0 )
            {
                value *= 10.0;
                digit -= 1;
            }
        else
            while( value >= 10.0 )
            {
                value /= 10.0;
                digit += 1;
            }
        
        return digit;
    }
}
