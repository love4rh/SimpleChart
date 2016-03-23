package lib.turbok.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import lib.turbok.swing.common.TransferableImage;


@SuppressWarnings("serial")
public abstract class GraphComponent extends JComponent
                                     implements ClipboardOwner
{
    public static interface EventListener
    {
        public void onClickData(long clickedRow, int mouseButton);
    }
    
    /** 이벤트 리스너 */
    private EventListener       _listener = null;
    
    /** 스크롤 된 크기 */
    private Point               _scrollPos = new Point(0, 0);

    /** 마지막 드래그 위치 */
    private Point               _lastDragPoint = null;

    /** 마지막 마우스 위치 */
    private Point               _lastMovePoint = null;
    
    /** 마지막 클릭 위치 */
    private Point               _lastClickPoint = null;
    
    /**
     * 0: Dragging 아님.
     * 1: 확대/축소 모드
     * 2: 이동 모드
     */
    private int                 _dragMode = 0;
    
    private Stroke              _dashedStroke = null;
    
    
    public GraphComponent(EventListener listener)
    {
        _listener = listener;
        
        setDoubleBuffered(true);
        
        _dashedStroke = new BasicStroke(1.0f
                , BasicStroke.CAP_BUTT
                , BasicStroke.JOIN_MITER
                , 10.0f, new float[] { 10.0f }, 0.0f );
        
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setDismissDelay(100000);
        
        this.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                long clickedRow = getRowOfLastMousePoint();
                
                if( e.getClickCount() == 1 )
                {
                    onClickData(clickedRow, e.getButton());
                    
                    if( _listener != null )
                        _listener.onClickData(clickedRow, e.getButton());
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e)
            {
                onMousePressed(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e)
            {   
                onMouseReleased(e);
            }
        } );
        
        this.addMouseMotionListener( new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                onMouseMoved(e);
            }
            
            @Override
            public void mouseMoved(MouseEvent e)
            {
                onMouseMoved(e);
            }
        } );
        
        this.addMouseWheelListener( new MouseAdapter()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                onMouseWheelMoved(e);
            }
        });
        
        this.addKeyListener( new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                switch(e.getKeyCode())
                {
                case KeyEvent.VK_LEFT:
                    scrollPage(1);
                    break;
                case KeyEvent.VK_RIGHT:
                    scrollPage(2);
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_PAGE_UP:
                    scrollPage(3);
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_PAGE_DOWN:
                    scrollPage(4);
                    break;
                }
            }
        });
    }
    
    /**
     * 페이지 단위로 이동.
     * @param direction     이동할 방향. 1: 왼쪽, 2: 오른쪽, 3: 위쪽, 4: 아래쪽
     */
    public void scrollPage(int direction)
    {
        Rectangle rect = this.getContentsArea();
        
        switch( direction )
        {
        case 1:
            scrolling(-rect.width + 50, 0, true);
            break;
        case 2:
            scrolling(rect.width - 50, 0, true);
            break;
        case 3:
            scrolling(0, -rect.height + 50, true);
            break;
        case 4:
            scrolling(0, rect.height - 50, true);
            break;
        }
    }
    
    /** 지정된 위치로 스크롤 이동 */
    public void scrollTo(int sX, int sY, boolean redraw)
    {
        Rectangle rectClient = getVisibleRect();
        Dimension totalSize = getTotalSize();
        
        _scrollPos.x = sX;;
        _scrollPos.y = sY;

        if( _scrollPos.x > totalSize.width - rectClient.width )
            _scrollPos.x = totalSize.width - rectClient.width;
        
        if( _scrollPos.x < 0 )
            _scrollPos.x = 0;
        
        if( _scrollPos.y > totalSize.height - rectClient.height )
            _scrollPos.y = totalSize.height - rectClient.height;
        
        if( _scrollPos.y < 0 )
            _scrollPos.y = 0;
        
        if( redraw )
            this.repaint();
    }
    
    /**
     * 지정된 값 만큼 스크롤함.
     * @param offsetX
     * @param offsetY
     * @param redraw
     */
    public void scrolling(int offsetX, int offsetY, boolean redraw)
    {
        scrollTo(_scrollPos.x + offsetX, _scrollPos.y + offsetY, redraw);
    }
    
    /** 스크롤 정보 반환 */
    public Point getScrollPos()
    {
        return _scrollPos;
    }
    
    /**
     * 제일 왼쪽까지 스크롤
     */
    public void scrollLeftmost()
    {
        _scrollPos.x = 0;
        this.repaint();
    }
    
    /**
     * 제일 오른쪽까지 스크롤
     */
    public void scrollRightmost()
    {
        Rectangle rectClient = getVisibleRect();
        Dimension totalSize = getTotalSize();
        
        _scrollPos.x = rectClient.width > totalSize.width ? 0 : totalSize.width - rectClient.width;

        this.repaint();
    }

    /**
     * 제일 위쪽까지 스크롤
     */
    public void scrollTopmost()
    {
        _scrollPos.y = 0;
        this.repaint();
    }
    
    /**
     * 제일 아래쪽까지 스크롤
     */
    public void scrollBottommost()
    {
        Rectangle rectClient = getVisibleRect();
        Dimension totalSize = getTotalSize();
        
        _scrollPos.y = totalSize.height - rectClient.height;

        this.repaint();
    }
    
    private void onMousePressed(MouseEvent e)
    {
        this.grabFocus();
        
        Point pt = e.getPoint();
        
        if( isContentsArea(pt) )
        {
            // 확대 축소 모드
            if( e.getButton() == MouseEvent.BUTTON1 )
            {
                _lastDragPoint = pt;
                _dragMode = 1;
            }
            // 이동 모드
            else if( e.getButton() == MouseEvent.BUTTON3 )
            {
                this.setCursor( Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );

                _lastDragPoint = pt;
                _dragMode = 2;
            }
        }
        
        _lastClickPoint = pt;
    }
    
    private void onMouseReleased(MouseEvent e)
    {
        if( _dragMode == 1 )
        {
            // 드래그 위치가 2 안쪽이면 클릭이라고 봄.
            if( Math.abs(_lastClickPoint.x - _lastDragPoint.x) > 2
                    || Math.abs(_lastClickPoint.y - _lastDragPoint.y) > 2 )
            {
                // 처음 포인트가 뒤쪽 포인트 보다 x, y 모두 작은 경우만 확대임. 
                boolean isZoom = _lastClickPoint.x < _lastDragPoint.x
                            && _lastClickPoint.y < _lastDragPoint.y;
                
                if( isZoom )
                {
                    Rectangle rect = this.getContentsArea();
                    
                    double xFactor = rect.getWidth() / (double) (_lastDragPoint.x - _lastClickPoint.x);
                    double yFactor = rect.getHeight() / (double) (_lastDragPoint.y - _lastClickPoint.y);
                    
                    zoom(xFactor, yFactor, _lastClickPoint);
                }
                else
                    zoomOut();
            }
            
            repaint();
        }
        
        _dragMode = 0;
        _lastDragPoint = null;
        
        this.setCursor( Cursor.getDefaultCursor() );
    }
    
    private void onMouseWheelMoved(MouseWheelEvent e)
    {
        Rectangle rectClient = this.getVisibleRect();
        
        int wheelRot = e.getWheelRotation() * 5;
        
        // X축으로 스크롤해야 하는 경우 
        if( rectClient.height > getTotalSize().height )
            scrolling(wheelRot, 0, true);
        // Y축 스크롤
        else
            scrolling(0, wheelRot, true);
    }
    
    private void onMouseMoved(MouseEvent e)
    {
        Point pt = e.getPoint();
        
        // 마우스 버튼이 눌린 경우임.
        if( _lastDragPoint != null )
        {
            // 이동 모드
            if( _dragMode == 2 )
                scrolling(_lastDragPoint.x - pt.x, _lastDragPoint.y - pt.y, true);

            _lastDragPoint = pt;
            if( _dragMode == 1 )
                repaint();
        }
        else
        {
            if( isContentsArea(pt) )
            {
                _lastMovePoint = pt;
                repaint();
            }
        }
    }
    
    @Override
    public void paint(Graphics g)
    {
        drawComponent(g, false, false);
        
        // 확대 축소 모드라면 영역 선택 상자 그려야 함.
        if( _dragMode == 1 )
        {
            Graphics2D g2 = (Graphics2D) g;
            
            Rectangle rect = getContentsArea();
            g2.setClip(rect.x, rect.y, rect.width, rect.height);
            
            g2.setStroke(_dashedStroke);
            g2.setColor(Color.DARK_GRAY);

            g2.drawRect( Math.min(_lastClickPoint.x, _lastDragPoint.x)
                       , Math.min(_lastClickPoint.y, _lastDragPoint.y)
                       , Math.abs(_lastDragPoint.x - _lastClickPoint.x)
                       , Math.abs(_lastDragPoint.y - _lastClickPoint.y) );
        }
    }
    
    /** 마우스의 마지막 위치 반환 */
    public Point getLastMousePoint()
    {
        return _lastMovePoint;
    }
    
    /**
     * 그려진 이미지를 PNG 형태의 파일로 저장 혹은 클립보드 복사
     * @param pathName  null이면 클립보드로 복사하고 null이 아니면 지정된 파일에 저장함.
     * @param drawAll   현재 그려진 영역만 저장할 지(false) 모두 저장할 지(true) 여부 지정.
     * @throws Exception
     */
    public void saveImage(String pathName, boolean drawAll) throws Exception
    {
        if( !isPossibleToDraw() )
            throw new IllegalStateException("A data is not set for drawing.");
        
        // 저장.
        BufferedImage bImg = null;
        
        if( drawAll )
            bImg = new BufferedImage(getTotalSize().width, getTotalSize().height, BufferedImage.TYPE_INT_RGB);
        else
        {
            Rectangle rectClient = this.getVisibleRect();
            
            bImg = new BufferedImage(rectClient.width, rectClient.height, BufferedImage.TYPE_INT_RGB);
        }

        Graphics g = bImg.createGraphics();
        
        this.drawComponent(g, drawAll, true);

        // 파일로 저장
        if( pathName != null )
        {
            ImageIO.write(bImg, "png", new File(pathName));
        }
        else
        {
            // 클립보드 복사
            Clipboard clipB = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipB.setContents(new TransferableImage( bImg ), this);
        }
    }

    // interface of ClipboardOwner
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents)
    {
        // nothing to do.
    }
    
    
    /** 사용한 자원 해제 */
    abstract public void clear();
    
    /** 차트를 그릴 수 있는 상태인지 여부 반환 */
    abstract public boolean isPossibleToDraw();
    
    /** 지정한 위치가 실제 컨텐츠가 표시된 영역인지 여부 반환 */
    abstract public boolean isContentsArea(Point pt);
    
    /** 실게 컨텐츠를 표시하는 영역 반환 */
    abstract public Rectangle getContentsArea();
    
    /** 차트를 모두 그렸을 때의 크기 반환 */
    abstract public Dimension getTotalSize();
    
    /** 마우스의 마지막 위치의 항목에 대한 데이터 위치 반환 */
    abstract public long getRowOfLastMousePoint();
    
    /** Component 그리기 */
    abstract protected void drawComponent(Graphics g, boolean drawAll, boolean saveFile);
    
    /**
     * 확대/축소.
     * @return  확대/축소된 화면 비율. 1.0보다 작으면 축소, 크면 확대임.
     */
    abstract public double zoom(double xRatio, double yRatio, Point topLeftPoint);
    
    /** 초기 크기로 돌아 가기 */
    abstract public void zoomOut();
    
    /** 마우스 클릭 했을 때의 이벤트 */
    abstract public void onClickData(long clickedRow, int button);
}
