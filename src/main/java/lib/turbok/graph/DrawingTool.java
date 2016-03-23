package lib.turbok.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import lib.turbok.util.UsefulTool;



/**
 * 화면을 그리는데 유용하게 사용될 그리기 툴들 모음
 * 
 * @author TurboK
 */
public enum DrawingTool
{
	DrawingTool
	;
	
	/// 점선을 그리기 위한 Stroke 객체
	private BasicStroke		_dashedStroke = null;
	
	
	private DrawingTool()
	{
		final float dash1[] = {10.0f};
		_dashedStroke = new BasicStroke(1.0f
		                        , BasicStroke.CAP_BUTT
		                        , BasicStroke.JOIN_MITER
		                        , 10.0f, dash1, 0.0f );
	}
	
	/**
	 * 사각형을 점선으로 그리기
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void drawDashedRect(Graphics g, int x, int y, int width, int height)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		Stroke oldStroke = g2.getStroke();
		
		g2.setStroke( _dashedStroke );
		g2.drawRect(x, y, width, height);
		
		g2.setStroke( oldStroke );
	}
	
	/**
	 * 사각형을 점선으로 그리기
	 * @param g
	 * @param rect
	 */
	public void drawDashedRect(Graphics g, Rectangle rect)
	{
		drawDashedRect(g, rect.x, rect.y, rect.width, rect.height);
	}
	
	/**
	 * 입력된 두 점으로 사각형 만들어 반환
	 * @param p1
	 * @param p2
	 * @return
	 */
	public Rectangle getRectangle(Point p1, Point p2)
	{
		int x1 = Math.min(p1.x, p2.x);
    	int y1 = Math.min(p1.y, p2.y);
    	int x2 = Math.max(p1.x, p2.x);
    	int y2 = Math.max(p1.y, p2.y);
    	
    	return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * p1 --> p2로 가는 직선의 기울기를 계산하여 Radian 형태로 반환하는 함수
	 * @param p1
	 * @param p2
	 * @return
	 */
	public double calcTheta(Point p1, Point p2)
	{
	    return calcTheta(p1.x, p1.y, p2.x, p2.y);
	}
	
	public double calcTheta(int x1, int y1, int x2, int y2)
    {
        double theta = 0;
        
        if( x2 == x1 )
            theta = Math.toRadians( y2 >= y1 ? 90 : 270 );
        else
            theta = x2 > x1
                  ? Math.atan((double) (y2 - y1) / (double) (x2 - x1))
                  : Math.atan((double) (y1 - y2) / (double) (x1 - x2)) + Math.PI;

        return theta;
    }
	
	/**
	 * 두 점간 중간점을 계산하여 반환
	 * @param p1
	 * @param p2
	 * @return
	 */
	public Point calcCenter(Point p1, Point p2)
	{   
	    return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);  
	}
	
	/**
	 * p1 --> p2로 가는 직선의 법선 중 p3에서 distance 만큼 거리가 떨어진 점을 계산하여 반환
	 * @param p1
	 * @param p2
	 * @param p3       p1과 p2가 이루는 직선 상에 있는 점이어야 함
	 * @param distance 0이 아닌 정수
	 * @return
	 */
	public Point calcNormalPoint(Point p1, Point p2, Point p3, int distance)
	{
	    // TODO p3가 p1 --> p2 직선에 있는 점인지 검사해야 하나?
	    
	    /* 참고:
         * 기울기가 theta 이고 점 P(x1, y1)을 지나는 직선의 방정식
         * y = (x - x1) * tan(theta) + y1 (where tan(theta) is not infinite)
         * y = x1 (where where tan(theta) is infinite)
         */
	    if( Math.abs(p2.x - p1.x) <= 1 )
	        return new Point(p3.x + distance, p3.y);
	    else if( Math.abs(p2.y - p1.y) <= 1 )
	        return new Point(p3.x, p3.y + distance);

	    double theta = calcTheta(p1, p2) + Math.PI / 2;
	    double tanTheta = Math.tan(theta);
	    
	    int x, y;
	    int offset = (int) (distance / Math.sqrt(1.0 + tanTheta * tanTheta) + (distance < 0 ? -0.5 : 0.5));
	    if( Math.abs(offset) < Math.abs(distance) )
	    {
	        x = p3.x;
	        y = p3.y + distance;
	    }
	    else
	    {
	        x = p3.x + offset;
	        y = (int) ((x - p3.x) * tanTheta + p3.y + (distance < 0 ? -0.5 : 0.5));
	    }
	            
	    return new Point(x, y);
	}
	
	/**
	 * 끝쪽이 화살표인 Line 그리기
	 * @param g            Drawing 대상 Graphics 객체
	 * @param p1           Position of Start Point
	 * @param p2           Position of End Point
	 * @param radius       각 시작, 끝 포인트에서 떨어진 정도
	 * @param stroke       brush 객체
	 */
	public void drawArrowLine( Graphics g
	                         , Point p1, Point p2
	                         , int radius, Stroke stroke )
	{
	    drawArrowLine((Graphics2D) g, p1.x, p1.y, p2.x, p2.y, 10, radius, stroke);
	}
	
	/**
	 * 끝쪽이 화살표인 Line 그리기
	 * @param g2           Drawing 대상 Graphics 객체
	 * @param x1            시작 X위치
	 * @param y1            시작 Y위치
	 * @param x2            끝 X위치
	 * @param y2            끝 Y위치
	 * @param arrowSize    화살표 크기, (arrowSize * 2) x (arrowSize * 2 - 4) 크기의 화살표가 그려짐
	 * @param radius       각 시작, 끝 포인트에서 떨어진 정도
	 * @param stroke       brush 객체
	 */
	public void drawArrowLine( Graphics2D g2, int x1, int y1, int x2, int y2
	                         , int arrowSize, int radius, Stroke stroke )
    {   
        Stroke oldStroke = g2.getStroke();
        
        int dx = x2 - x1, dy = y2 - y1;
        double d = Math.sqrt(dx * dx + dy * dy );
        
        if( d < 35 ) return;    // TODO 수치 조정

        if( stroke != null )
            g2.setStroke(stroke);
        
        int endX = (int) (x2 - (radius + arrowSize - 5) * dx / d);
        int endY = (int) (y2 - (radius + arrowSize - 5) * dy / d);
        
        g2.drawLine( (int) (x1 + radius * dx / d)
                   , (int) (y1 + radius * dy / d)
                   , endX, endY );
        
        double theta = calcTheta(x1, y1, x2, y2);
        
        int[] arrowX = new int[] { -arrowSize + 3, -arrowSize, +arrowSize, -arrowSize };
        int[] arrowY = new int[] {  0,  -arrowSize + 2,   0,  +arrowSize - 2 };
        
        int[] arrowX_ = new int[] { -arrowSize + 3, -arrowSize, +arrowSize, -arrowSize };
        int[] arrowY_ = new int[] {  0,  -arrowSize + 2,   0,  +arrowSize - 2 };
        
        for(int i = 0; i < arrowX.length; ++i)
        {
            arrowX[i] = (int) (Math.cos(theta) * arrowX_[i] - Math.sin(theta) * arrowY_[i]);
            arrowY[i] = (int) (Math.sin(theta) * arrowX_[i] + Math.cos(theta) * arrowY_[i]);
            
            arrowX[i] += endX;
            arrowY[i] += endY;
        }

        g2.fill( new Polygon(arrowX, arrowY, arrowX.length) );

        g2.setStroke(oldStroke);
    }
	
	/**
	 * 라인피드를 고려한 텍스트 그리기 크기 반환
	 * @param g
	 * @param str
	 * @param lineMargin
	 * @return
	 */
	public Rectangle calcTextMetrics(Graphics g, String str, int lineMargin)
	{
	    return calcTextMetrics(g, UsefulTool.SplitLineText(str, "\n", false, true), lineMargin, null);
	}
	
	public Rectangle calcTextMetrics(Graphics g, String str, int lineMargin, Font font)
    {
        return calcTextMetrics(g, UsefulTool.SplitLineText(str, "\n", false, true), lineMargin, font);
    }
	
	public Rectangle calcTextMetrics(Graphics g, String[] lines, int lineMargin)
	{
	    return calcTextMetrics(g, lines, lineMargin, null);
	}
	
	public Rectangle calcTextMetrics(Graphics g, String[] lines, int lineMargin, Font font)
	{
	    if( lines == null )
	        return new Rectangle(0, 0, 0, 0);
	    
	    FontMetrics fontMetric = font == null ? g.getFontMetrics() : g.getFontMetrics(font);

	    int height = 0;
	    int maxWidth = 0;
        for(String tmpStr : lines)
        {
            if( tmpStr == null )
                tmpStr = " ";
            
            Rectangle2D rectTitle = fontMetric.getStringBounds(tmpStr, g);
            
            int w = (int) rectTitle.getWidth();
            
            if( maxWidth < w )
                maxWidth = w;

            if( height > 0 )
                height += lineMargin;
            
            height += rectTitle.getHeight();
        }
        
        return new Rectangle(0, 0, maxWidth, height);
	}

	/**
	 * 지정된 위치를 가운데로 하여 문자열 그리기
	 * @param g
	 * @param str
	 * @param x
	 * @param y
	 * @param color
	 */
	public void drawStringAtCenter(Graphics g, String str, int x, int y, Color color)
	{
	    FontMetrics fontMetric = g.getFontMetrics();
        Rectangle2D rectTitle = fontMetric.getStringBounds(str, g);
        
        if( color != null )
            g.setColor( color );
        
        g.drawString( str
                    , x - rectTitle.getBounds().width / 2
                    , y + rectTitle.getBounds().height / 2 );
	}
	
	/**
     * 지정된 위치를 가운데로 하여 문자열 그리기. (그림자 효과 추가)
     * @param g
     * @param str
     * @param x
     * @param y
     * @param color
     * @param shadowColor
     */
    public void drawStringAtCenter(Graphics g, String str, int x, int y, Color color, Color shadowColor)
    {
        FontMetrics fontMetric = g.getFontMetrics();
        Rectangle2D rectTitle = fontMetric.getStringBounds(str, g);
        
        x -= rectTitle.getBounds().width / 2;
        y += rectTitle.getBounds().height / 2;
        
        Color oldColor = g.getColor();
        
        if( shadowColor != null )
        {
            g.setColor( shadowColor );
            g.drawString(str, x - 1, y - 1);
            g.drawString(str, x + 1, y + 1);
        }
        
        if( color != null )
            g.setColor(color);

        g.drawString(str, x, y);
        
        g.setColor( oldColor );
    }
	
	public void drawStringAtCenter( Graphics g, String str, int x, int y
	                              , int lineMargin, Color color )
    {
	    String[] lines = UsefulTool.SplitLineText(str, "\n", false, true);
	    Rectangle rectTitle = calcTextMetrics(g, lines, lineMargin);
        
        if( color != null )
            g.setColor( color );

        y -= rectTitle.height / 2;
        
        FontMetrics fontMetric = g.getFontMetrics();
        
        for(String tmpStr : lines)
        {
            Rectangle2D rectLine = fontMetric.getStringBounds(tmpStr, g);
            
            g.drawString( tmpStr
                        , (int) (x - rectLine.getWidth() / 2)
                        , (int) (y + rectLine.getHeight() / 2) );
            
            y += rectLine.getHeight() + lineMargin;
        }
    }
	
	/**
     * 지정된 위치를 가운데로 하여 angle만큼 회전한 문자열 그리기
     * @param g
     * @param str
     * @param x
     * @param y
     * @param angle
     */
    public void drawStringAtCenter(Graphics2D g2, String str, int x, int y, int angle)
    {
        FontMetrics fontMetric = g2.getFontMetrics();
        Rectangle2D rectTitle = fontMetric.getStringBounds(str, g2);

        g2.translate(x, y);
        g2.rotate(Math.toRadians(angle));
        g2.drawString(str, - rectTitle.getBounds().width / 2, rectTitle.getBounds().height / 2);
        g2.rotate(-Math.toRadians(angle));
        g2.translate(-x, -y);
    }
	
	/**
     * 지정된 x를 가장 오른쪽으로 하여 문자열 그리기
     * @param g
     * @param str
     * @param x
     * @param y
     * @param color
     */
    public void drawStringAtRight(Graphics g, String str, int x, int y, Color color)
    {
        FontMetrics fontMetric = g.getFontMetrics();
        
        Rectangle2D rectTitle = fontMetric.getStringBounds(str, g);
        
        if( color != null )
            g.setColor( color );
        
        g.drawString( str
                    , x - rectTitle.getBounds().width
                    , y + rectTitle.getBounds().height / 2 );
    }
    
    /**
     * 지정된 x를 가장 왼쪽으로 하여 문자열 그리기
     * @param g
     * @param str
     * @param x
     * @param y
     * @param color
     */
    public void drawStringAtLeft(Graphics g, String str, int x, int y, Color color)
    {
        FontMetrics fontMetric = g.getFontMetrics();
        
        Rectangle2D rectTitle = fontMetric.getStringBounds(str, g);
        
        if( color != null )
            g.setColor( color );
        
        g.drawString( str
                    , x
                    , y + rectTitle.getBounds().height / 2 );
    }
    
    /**
     * 
     * @param g
     * @param str
     * @param x
     * @param y
     * @param lineMargin
     * @param horizontal    가로 기준. x값을 0: LEFT, 1: CENTER, 2: RIGHT 와 같이 간주하여 그리기
     * @param vertical      세로 기분. y값을 0: TOP, 1: MIDDLE, 2: BOTTOM 와 같이 인식하여 그리기
     */
    public Rectangle drawString( Graphics g, String str, int x, int y
                               , int lineMargin, int horizontal, int vertical )
    {
        String[] lines = UsefulTool.SplitLineText(str, "\n", false, true);
        Rectangle rectTitle = calcTextMetrics(g, lines, lineMargin);
        
        FontMetrics fontMetric = g.getFontMetrics();
        
        int y2 = y; // TOP

        if( vertical == 1 ) // MIDDLE
            y2 -= rectTitle.height / 2;
        else if( vertical == 2 ) // BOTTOM
            y2 -= rectTitle.height;
        
        //y2 += fontMetric.getHeight() - 4;
        
        for(String tmpStr : lines)
        {
            Rectangle2D rectLine = fontMetric.getStringBounds(tmpStr, g);
            
            int x2 = x; // LEFT
            if( horizontal == 1 )   // CENTER
                x2 -= rectLine.getWidth() / 2;
            else if( horizontal == 2 )  // RIGHT
                x2 -= rectLine.getWidth();
            
            g.drawString(tmpStr, x2, (int) (y2 + rectLine.getHeight() / 2));
            
            y2 += rectLine.getHeight() + lineMargin;
        }
        
        return rectTitle;
    }
	
	/**
	 * 두 점이 같은 점인지 여부 반환
	 * @param p1
	 * @param p2
	 * @return
	 */
	public boolean isSamePoint(Point p1, Point p2)
	{
	    if( p1 == null || p2 == null )
	        return false;
	    
	    return p1.equals(p2);
	}
	
	/**
	 * 입력된 사각형을 offset 만큼 줄인 Rectangle 객체 반환
	 * @param rectangle
	 * @param offset
	 * @return
	 */
	public Rectangle deflateRect(Rectangle rectangle, int offset)
	{
	    return new Rectangle( rectangle.x + offset
            	            , rectangle.y + offset
            	            , rectangle.width - offset * 2
            	            , rectangle.height - offset * 2 );
	}
}
