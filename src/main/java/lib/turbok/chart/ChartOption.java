package lib.turbok.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;


public class ChartOption
{
    /** 전체 배경색 */
    private Color           _bgColor = Color.white;
    
    /** 차팅 영역 배경색 */
    private Color           _chartBgColor = Color.lightGray;
    
    /** 차트 제목 색상 */
    private Color           _titleColor = Color.black;
    
    /** 제목 폰트 */
    private Font            _titleFont = null;
    
    /** Axis Font */
    private Font            _axisFont = null;
    
    private Font            _axisTickFont = null;
    
    private Font            _titleBoxFont = null;
    
    private Color[]         _plotColor = null;
    
    /** The color of dragging rectangle */
    private Color           _dragRectColor = Color.darkGray;
    
    private Stroke          _dragRectStroke = null;
    
    
    public ChartOption()
    {
        // 기본값 설정

        // Font(String name, int style, int size)
        _titleFont = new Font(null, Font.BOLD, 20);
        _axisFont = new Font(null, Font.BOLD, 14);
        
        _titleBoxFont = new Font(null, Font.BOLD, 12);
        _axisTickFont = new Font(null, Font.PLAIN, 12);
        
        _plotColor = new Color[] { Color.decode("#1f77b4"), Color.decode("#d62728"), Color.decode("#2ca02c")
                                 , Color.decode("#ff7f0e"), Color.decode("#9467bd"), Color.decode("#8c564b")
                                 , Color.decode("#e377c2"), Color.decode("#7f7f7f"), Color.decode("#bcbd22")
                                 , Color.decode("#17becf"), Color.decode("#aec7e8"), Color.decode("#ffbb78")
                                 , Color.decode("#98df8a"), Color.decode("#ff9896"), Color.decode("#c5b0d5")
                                 , Color.decode("#c49c94"), Color.decode("#f7b6d2"), Color.decode("#c7c7c7")
                                 , Color.decode("#dbdb8d"), Color.decode("#9edae5")
                                 };
        
        final float dash1[] = {10.0f};
        _dragRectStroke = new BasicStroke(1.0f
                                , BasicStroke.CAP_BUTT
                                , BasicStroke.JOIN_MITER
                                , 10.0f, dash1, 0.0f );
    }
    
    public Color plotColor(int index)
    {
        return _plotColor[index % _plotColor.length];
    }
    
    /**
     * 전체 배경색
     */
    public Color bgColor()
    {
        return _bgColor;
    }
    
    /**
     * 차팅 영역 배경색 반환
     */
    public Color chartBgColor()
    {
        return _chartBgColor;
    }
    
    /**
     * 제목 글자색 반환
     */
    public Color titleColor()
    {
        return _titleColor;
    }
    
    /**
     * 제목 표시용 퐄트
     */
    public Font titleFont()
    {
        return _titleFont;
    }
    
    /**
     * 축 제목 표시용 폰트
     */
    public Font axisFont()
    {
        return _axisFont;
    }
    
    /**
     * 축 값 표시용 폰트
     */
    public Font axisTickFont()
    {
        return _axisTickFont;
    }
    
    public Color dragRectColor()
    {
        return _dragRectColor;
    }
    
    public Stroke dragRectStroke()
    {
        return _dragRectStroke;
    }

    public Font titleBoxFont()
    {
        return _titleBoxFont;
    }
}
