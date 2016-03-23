package lib.turbok.graph;

import static lib.turbok.graph.DrawingTool.DrawingTool;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JViewport;

import lib.turbok.graph.action.IGraphActionHandler;
import lib.turbok.graph.object.IGraphLink;
import lib.turbok.graph.object.IGraphNode;
import lib.turbok.graph.object.IGraphObject;



/**
 * 노드와 노드를 연결하는 간단한 Diagram 을 편집할 수 있는 에디터
 * 
 * @author TurboK
 *
 */
public class DiagramEditor extends JComponent
                           implements MouseListener, MouseMotionListener
{
    private static final long serialVersionUID = 1L;

    /**
     * DiagramEditor의 작성 상태를 표시하기 위한 enum type
     * 
     * @author TurboK
     */
    private static enum Status
    {
          eNormal           ///< 아무 이벤트도 없는 일반적인 상태
        , eConnecting       ///< 노드를 연결 중인 상태
        , eDraggingNode     ///< 선택된 노드를 드래깅 하고 있는 상태
        , eDraggingLink     ///< 선택된 연결선을 드래깅 하고 있는 상태 (지금은 필요 없음. 일종의 Reserved)
        , eSelecting        ///< 노드 혹은 연결선을 선택하고 있는 상태
        ;
    }
    
    /**
     * 노드 목록
     */
    private List<IGraphNode>        _nodes = null;
    
    /**
     * 연결선 목록
     */
    private List<IGraphLink>        _links = null;
    
    /**
     * _nodes 중 선택된 노드객체 목록 관리 멤버
     */
    private List<IGraphNode>        _selectedNodes = null;
    
    /**
     * _links 중 선택된 연결선 객체 목록 관리 멤버
     */
    private List<IGraphLink>        _selectedLinks = null;
    
    /** 
     * 이 컨트롤에서 발생하는 이벤트를 처리하기 위한 핸들러 클래스
     */
    private IGraphActionHandler     _eventHandler = null;
    
    /**
     * 현재 Diagram의 작업 상태
     */
    private Status                  _status = Status.eNormal;
    
    /**
     * 클라이언트 크기
     */
    private Dimension       _clientSize = null;
    
    /**
     * 마우스 포인트 Dragging 의 마지막 위치
     */
    private Point           _draggingPoint = null;
    
    /**
     * 마지막으로 클릭된 (Pressed) 위치
     */
    private Point           _lastPressedPoint = null;
    
    /**
     * 마지막으로 선택된 그래프 객체
     */
    private IGraphObject    _lastSelected = null;
    
    /**
     * 연결 중인 연결선의 종류
     */
    private int             _connectingLinkType = -1;
    
    
    public DiagramEditor()
    {
        initialize();
        
        _clientSize = this.getPreferredSize();
    }
    
    private void initialize()
    {   
        _status = Status.eNormal;
        
        setDoubleBuffered(true);
        
        _nodes = new ArrayList<IGraphNode>();
        _links = new ArrayList<IGraphLink>();
        
        _selectedNodes = new ArrayList<IGraphNode>();
        _selectedLinks = new ArrayList<IGraphLink>();
        
        // this.setDropTarget(dt)
        
        // Add Keyboard Event Handler
        this.addKeyListener( new KeyListener()
        {
            boolean ctrlPress = false;
            boolean shiftPress = false;
            boolean altPress = false;
            
            @Override
            public void keyTyped(KeyEvent e)
            {
                // Nothing to do
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                if( !ctrlPress )
                    ctrlPress = e.getKeyCode() == KeyEvent.VK_CONTROL;
                if( !shiftPress )
                    shiftPress = e.getKeyCode() == KeyEvent.VK_SHIFT;
                if( !altPress )
                    altPress = e.getKeyCode() == KeyEvent.VK_ALT;
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                if( e.getKeyCode() == KeyEvent.VK_CONTROL )
                    ctrlPress = false;
                else if( e.getKeyCode() == KeyEvent.VK_SHIFT )
                    shiftPress = false;
                else if( e.getKeyCode() == KeyEvent.VK_ALT )
                    altPress = false;
                
                if( _eventHandler != null )
                    _eventHandler.OnKeyTyped(e.getKeyCode(), ctrlPress, shiftPress, altPress);
            }
        } );
        
        // Add Mouse Button Event Handler
        this.addMouseListener( this );
        
        // Add Mouse Motion Listener
        this.addMouseMotionListener( this );
    }

    public void setEventHandler(IGraphActionHandler handler)
    {
        _eventHandler = handler;
    }
    
    private void changeStatus(Status status)
    {
        _status = status;
        
        if( _status == Status.eNormal )
        {
            for(IGraphNode nodeObj : _nodes)
                nodeObj.setStateToNormal();
        }
    }
    
    public void setCanvasSize(Dimension dimension)
    {
        if( dimension == null )
            return;
        
        _clientSize.setSize(dimension);
        
        this.setPreferredSize(dimension);
        this.getParent().repaint();
    }
    
    @Override
    public void mousePressed(MouseEvent e)
    {
        this.grabFocus();
        
        // 이 이벤트의 초기상태는 Normal 일 것임.
        // 아니라면 마우스 누름 이벤트 외 다른 루트로 작업이 이루어 지고 있는 것이므로 그냥 리턴
        if( _status != Status.eNormal )
            return;
        
        Point pt = e.getPoint();
        
        // 해당 위치에 선택되는 객체가 있는 지 검사
        IGraphObject selObj = this.hitTest(pt.x, pt.y);
        
        // 1. 왼쪽 버튼을 눌렀다면, 
        if( e.getButton() == 1 )
        {
            _draggingPoint = pt;
            
            // 1.1 기 선택된 노드를 누른 경우라면 위치 변경 모드 (DraggingNode)
            if( selObj != null && selObj instanceof IGraphNode )
            {
                // selObj 및 selObj와 1단계로 연결된 노드들 선택
                if( 0 != (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) )
                {
                    selectObject(selObj, false);

                    for(IGraphLink linkObj : _links)
                    {
                        if( linkObj.to() == selObj || linkObj.from() == selObj )
                        {
                            selectObject(linkObj, true);
                            selectObject(linkObj.to(), true);
                            selectObject(linkObj.from(), true);
                        }
                    }
                }
                else
                {
                    boolean ctrlPressed = (0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK));

                    if( !selObj.isSelected() )
                        selectObject(selObj, ctrlPressed);
                    else if( ctrlPressed )
                    {
                        selObj.setSelected(false);
                        _selectedNodes.remove(selObj);
                    }
                    
                    changeStatus( Status.eDraggingNode );
                }
            }
            // 1.2 그렇지 않은 경우는 Selecting 모드로 들어 감
            else
                changeStatus( Status.eSelecting );
        }
        // 2. 선택된 객체가 Node 라면
        else if( selObj != null && selObj instanceof IGraphNode )
        {
            // 2.1 가운데 버튼이라면 노드 연결 모드임
            if( e.getButton() == 2 )
            {
                selectObject(selObj, false);
                
                startConnecting( (IGraphNode) selObj, 0 );

                _draggingPoint = pt;
            }
            
            // 2.2 오른쪽 버튼을 눌렀다면 그냥 Normal 모드임.
        }

        _lastSelected = selObj;
        _lastPressedPoint = pt;
        
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {   
        Point pt = e.getPoint();
        
        JViewport parent = (JViewport) this.getParent();
        // Scroll 위치를 고려한 마우스 포인트
        Point ptScroll = new Point( pt.x - parent.getViewPosition().x
                                  , pt.y - parent.getViewPosition().y );
        
        // 해당 위치에 선택되는 객체가 있는 지 검사
        IGraphObject selObj = this.hitTest(pt.x, pt.y);
        
        // 눌린 위치와 같은 것인지 판단
        boolean samePos = _lastPressedPoint != null
                && ( pt.x - 2 <= _lastPressedPoint.x && _lastPressedPoint.x <= pt.x + 2
                     && pt.y - 2 <= _lastPressedPoint.y && _lastPressedPoint.y <= pt.y + 2 );
        
        // 3. 가운데 버튼이 누르거나 다른 루트로 연결모드가 되었다면,
        if( _status == Status.eConnecting 
                && selObj != null && selObj instanceof IGraphNode )
        {
            endConnecting( (IGraphNode) selObj );
        }
        // 1. 마우스 오른쪽 버튼이 같은 위치에서 눌렸고
        else if( _status == Status.eNormal && samePos && e.getButton() == 3 )
        {
            // 1.1 빈 곳을 누른 경우
            if( selObj == null )
            {
                selectObject(null, false);
            }
            // 1.2 선택되지 않은 노드에서 누른 경우 --> 새로 선택된 노드 하나만
            else if( !selObj.isSelected() )
            {
                selectObject(selObj, false);
            }
            // 1.2 선택된 노드에서 누른 경우 --> 기존에 선택된 애들, 아무 일도 안함
            
            if( _eventHandler != null )
                _eventHandler.OnContextMenu(_selectedNodes, _selectedLinks, ptScroll);
        }
        // 2. 왼쪽 버튼이 눌린 경우
        else if( e.getButton() == 1 )
        {
            // 2.1 노말 상태이고 더블클릭을 했다면
            if( _status == Status.eSelecting && 2 == e.getClickCount() )
            {
                if( _eventHandler != null )
                {
                    _eventHandler.OnDblClick(selObj, ptScroll);
                    
                    selectObject(selObj, false);  // 선택된 객체 모두 해제
                }
            }
            // 2.2 노말 상태이고 버튼 누를 때의 객체와 같은 애라면
            else if( _status == Status.eNormal && selObj == _lastSelected && selObj != null )
            {
                boolean selectAdd = selObj.isSelected()
                        || 0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK);
   
                // 선택
                selectObject(selObj, selectAdd);
            }
            // 2.3 영역 내 선택
            else if( _status == Status.eSelecting )
            {
                // 마우스를 움직이지 않았다면 그 위치에 있는 객체를 선택 (없을 수도 있음)
                if( samePos )
                {
                    selectObject(selObj, false);
                    
                    if( 1 == e.getClickCount() && _eventHandler != null )
                        _eventHandler.OnClickObject(selObj, ptScroll);
                }
                // 지정한 영역 내에 포함된 객체 모두 선택
                else
                {
                    boolean selectAdd = 0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK);
                    Rectangle selRect = DrawingTool.getRectangle(_lastPressedPoint, _draggingPoint);

                    for(IGraphNode nodeObj : _nodes)
                    {
                    	if( !selectAdd )
                    	{
                    		nodeObj.setSelected(false);
                    		_selectedNodes.remove(nodeObj);
                    	}
                    	
                        if( !nodeObj.isSelected() && nodeObj.isSelectable()
                        		&& selRect.contains(nodeObj.getBounds()) )
                        {
                        	nodeObj.setSelected(true);
                        	_selectedNodes.add(nodeObj);
                        }
                    }
                    
                    for(IGraphLink linkObj : _links)
                    {
                    	if( !selectAdd )
                    	{
                    		linkObj.setSelected(false);
                    		_selectedLinks.remove(linkObj);
                    	}
                    	
                    	if( !linkObj.isSelected() && linkObj.isSelectable()
                    			&& selRect.contains(linkObj.getPointStart())
                    			&& selRect.contains(linkObj.getPointEnd()) )
                    	{
                    		linkObj.setSelected(true);
                    		_selectedLinks.add(linkObj);
                    	}
                    }
                }
                
                List<IGraphNode> selectedNodes = getSelectedNodes();
                if( _eventHandler != null )
                    _eventHandler.OnChangeNodeSelection(selectedNodes);
                
            }
            // 2.4 Dragging 모드였다면
            else if( _status == Status.eDraggingNode )
            {
                List<IGraphNode> selectedNodes = getSelectedNodes();
                
                if( samePos && selObj != null )
                {
                    // 선택된 노드와 연결된 노드 선택
                    if( 0 != (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) )
                    {
                        selectObject(selObj, false);
                        
                        for(IGraphLink linkObj : _links)
                            if( linkObj.to() == selObj || linkObj.from() == selObj )
                            {
                                selectObject(linkObj, true);
                                selectObject(linkObj.to(), true);
                                selectObject(linkObj.from(), true);
                            }
                    }
                    else
                    {
                        /*
                        boolean ctrlPressed = (0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK));

                        if( !selObj.isSelected() )
                            selectObject(selObj, ctrlPressed);
                        else if( ctrlPressed )
                        {
                            selObj.setSelected(false);
                            _selectedNodes.remove(selObj);
                        }
                        // */
                    }
                	
                	if( _eventHandler != null )
                	{
                    	if( 2 == e.getClickCount() )
                		    _eventHandler.OnDblClick(selObj, ptScroll);
                    	else if( 1 == e.getClickCount() )
                    	    _eventHandler.OnClickObject(selObj, ptScroll);
                	}
                }
                else if( !samePos )
                {
                    // 노드 이동 이벤트 발생
                    if( _eventHandler != null )
                        _eventHandler.OnNodeMoved( selectedNodes );
                }
                
                if( _eventHandler != null )
                    _eventHandler.OnChangeNodeSelection(selectedNodes);
            }
        }

        _lastSelected = null;
        changeStatus(Status.eNormal);
        _draggingPoint = null;
        
        this.repaint();
    }
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        //
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {   
        if( _draggingPoint == null )
            return;
        
        Point pt = e.getPoint();
        
        if( _status == Status.eDraggingNode )
        {
	        int dx = pt.x - _draggingPoint.x;
	        int dy = pt.y - _draggingPoint.y;
	        
	        int maxX = 0, maxY = 0;
	        
	        for(IGraphNode nodeObj : _nodes)
	        {
	            if( nodeObj.isSelected() && nodeObj instanceof IGraphNode )
	            {
	                Rectangle bounds = nodeObj.moveWithOffset(dx, dy);
	                if( maxX < bounds.x + bounds.width )
	                    maxX = bounds.x + bounds.width;
	                if( maxY < bounds.y + bounds.height )
	                    maxY = bounds.y + bounds.height;
	            }
	        }
	        
	        if( _clientSize.width < maxX + 50 )
	            _clientSize.width = maxX + 50;
	        if( _clientSize.height < maxY + 50 )
	            _clientSize.height = maxY + 50;
	        
	        setCanvasSize(_clientSize);
        }
        else if( _status == Status.eSelecting )
        {
        	// Nothing to do.
        }

        _draggingPoint = pt;
        
        this.repaint();
    }
    
    @Override
    public void mouseEntered(MouseEvent e)
    {
        // to do nothing
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // to do nothing
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        if( _status == Status.eConnecting || _status == Status.eSelecting )
        {
            _draggingPoint = e.getPoint();
            this.repaint();
        }
    }
    
    public void addGraphNode(IGraphNode nodeObj, boolean redraw)
    {
        if( nodeObj == null )
            return;
        
        _nodes.add(nodeObj);
        
        Rectangle bound = nodeObj.getBounds();
        if( _clientSize.width < bound.x + bound.width + 50 )
            _clientSize.width = bound.x + bound.width + 50;
        
        if( _clientSize.height < bound.y + bound.height + 50 )
            _clientSize.height = bound.y + bound.height + 50;

        this.setCanvasSize(_clientSize);
        
        if( _eventHandler != null )
            _eventHandler.OnAddedGraphObject(nodeObj);
        
        if( redraw )
        {
            repaint();
            this.getParent().repaint();
        }
    }
    
    public void addGraphLink(IGraphLink linkObj, boolean redraw)
    {
        if( linkObj == null )
            return;

        _links.add(linkObj);
        
        IGraphNode from = linkObj.from();
        IGraphNode to = linkObj.to();
        
        from.onConnectingTo(to, linkObj.getType());
        to.onConnectingFrom(from, linkObj.getType());

        if( redraw )
            repaint();
    }

    public boolean connectNodes(IGraphNode fromNode, IGraphNode toNode, int linkType)
    {   
        if( _eventHandler != null
                && toNode.isConnectable(fromNode, linkType) )
        {   
            _eventHandler.OnConnectingNodes(fromNode, toNode, linkType);
            return true;
        }

        return false;
    }

    public void deleteSelected(boolean redraw)
    {
        // ConcurrentModification 오류를 방지하기 위하여 아래와 같이 코드를 중복시켰음.
        
        for(IGraphLink linkObj : _selectedLinks)
        {
            IGraphNode from = linkObj.from();
            IGraphNode to = linkObj.to();
            
            if( _eventHandler != null )
                _eventHandler.OnDiconnectingLink(from, to, linkObj.getType());
            
            from.onDisconnectingTo(to, linkObj.getType());
            to.onDisconnectingFrom(from, linkObj.getType());
            
            _links.remove(linkObj);
        }
        _selectedLinks.clear();

        for(IGraphNode nodeObj : _selectedNodes)
        {   
            int i = 0;
            for(i = 0; i < _links.size(); ++i)
            {
                IGraphLink linkObj = _links.get(i);
                if( nodeObj == linkObj.from() || nodeObj == linkObj.to() )
                {
                    disconnectLink(linkObj);
                    --i;
                }
            }
            
            if( _eventHandler != null )
                _eventHandler.OnDeletingNode(nodeObj);

            _nodes.remove(nodeObj);
        }
        _selectedNodes.clear();
    
        if( redraw )
            this.repaint();
    }
    
    public void deleteNode(IGraphNode nodeObj)
    {
        for(int i = 0; i < _links.size(); ++i)
        {
            IGraphLink linkObj = _links.get(i);
            if( nodeObj == linkObj.from() || nodeObj == linkObj.to() )
                disconnectLink(linkObj);
        }

        if( _eventHandler != null )
            _eventHandler.OnDeletingNode(nodeObj);
        
        _nodes.remove(nodeObj);
        _selectedNodes.remove(nodeObj);
    }
    
    public void disconnectLink(IGraphNode fromNode, IGraphNode toNode)
    {
        disconnectLink( getLink(fromNode, toNode) );
    }
    
    /**
     * nodeObj가 지정된 위치에서 시작하는 노드이고 linkType과 같은 연결선을 끊는 메소드.
     * 위치는 isStart가 true이면 시작위치가 nodeObj인 것을 찾고, false이면 끝위치가 nodeObj인 것을 찾음.
     * 이 메소드는 는 OnDisconnectingLink() 이벤트를 발생시킴.
     * 
     * @param nodeObj
     * @param isStart
     * @param linkType
     */
    public void disconnectLink(IGraphNode nodeObj, boolean isStart, int linkType)
    {
        for(int i = 0; i < _links.size(); ++i)
        {
            IGraphLink linkObj = _links.get(i);
            
            if( linkObj.getType() == linkType
                && (   (isStart && linkObj.from() == nodeObj)
                    || (!isStart && linkObj.to() == nodeObj) )
                )
            {
                disconnectLink(linkObj);
                --i;
            }
        }
    }
    
    /**
     * nodeObj가 지정된 위치에서 시작하는 노드이고 linkType과 같은 연결선을 끊는 메소드.
     * 위치는 isStart가 true이면 시작위치가 nodeObj인 것을 찾고, false이면 끝위치가 nodeObj인 것을 찾음.
     * 이 메소드는 는 OnDisconnectingLink() 이벤트를 발생시키 않음.
     * 
     * @param nodeObj
     * @param isStart
     * @param linkType
     */
    public void deleteLink(IGraphNode nodeObj, boolean isStart, int linkType)
    {
        for(int i = 0; i < _links.size(); ++i)
        {
            IGraphLink linkObj = _links.get(i);
            
            if( linkObj.getType() == linkType
                && (   (isStart && linkObj.from() == nodeObj)
                    || (!isStart && linkObj.to() == nodeObj) )
                )
            {
                deleteLink(linkObj);
                --i;
            }
        }
    }
    
    public void disconnectLink(IGraphLink linkObj)
    {
        if( linkObj == null )
            return;
        
        IGraphNode from = linkObj.from();
        IGraphNode to = linkObj.to();
        
        if( _eventHandler != null )
            _eventHandler.OnDiconnectingLink(from, to, linkObj.getType());
        
        deleteLink(linkObj);
    }
    
    /**
     * 입력된 노드와 연결된 연결선 제거. EventHandler에 이벤트를 발생시키지 않는 버전.
     * 단, 노드의 onDisconnectingTo(), onDisconnectingFrom() 이벤트는 발생함.
     * @param node  연결선의 한 노드
     * @param type  1이면 node가 시작 위치에 있는 것만 제거, 2이면 끝 위치, 3이면 어디에 있던 제거함
     */
    public void deleteLink(IGraphNode node, int type)
    {
        for(int index = 0; index < _links.size(); )
        {
            IGraphLink linkObj = _links.get(index);
            
            if( ((type & 0x01) == 0x01 && node == linkObj.from())
                    || ((type & 0x02) == 0x02 && node == linkObj.to()) )
            {
                deleteLink(linkObj);
                --index;
            }
            
            ++index;
        }
    }
    
    /**
     * 연결선 단순 삭제. EventHandler에 이벤트를 발생시키지 않는 버전.
     * 단, 노드의 onDisconnectingTo(), onDisconnectingFrom() 이벤트는 발생함.
     * disconnectLink()는 EventHandler에 이벤트 발생시키는 버전임.
     * @param linkObj
     */
    public void deleteLink(IGraphLink linkObj)
    {
        IGraphNode from = linkObj.from();
        IGraphNode to = linkObj.to();
        
        from.onDisconnectingTo(to, linkObj.getType());
        to.onDisconnectingFrom(from, linkObj.getType());
        
        _links.remove(linkObj);
        _selectedLinks.remove(linkObj);
    }
    
    /**
     * 두 노드를 연결한 연결선 반환. 없다면 null을 반환 함.
     * 시작, 끝 위치는 고려하지 않으며 연결이 되어 있는 지만 판단함.
     * 
     * @param node1
     * @param node2
     * @return
     */
    public IGraphLink getLink(IGraphNode node1, IGraphNode node2)
    {
        for(IGraphLink linkObj : _links)
        {
            if( (linkObj.from() == node1 || linkObj.from() == node2)
                    && (linkObj.to() == node1 || linkObj.to() == node2) )
                return linkObj;
        }
        
        return null;
    }

    @Override
    public void paint(Graphics g)
    {
        Rectangle rectClient = this.getVisibleRect();
        JViewport parent = (JViewport) this.getParent();
        Point scrollPos = parent.getViewPosition();
        
        // 바탕화면 지우기
        g.clearRect( rectClient.x, rectClient.y, rectClient.width, rectClient.height );

        // Clip 영역 설정
        g.setClip( rectClient.x, rectClient.y, rectClient.width, rectClient.height );
        
        // 연결선 그리기
        for(IGraphLink linkObj : _links)
        	linkObj.draw(g, scrollPos);
        
        // 연결 모드인 경우
        if( _status == Status.eConnecting && _draggingPoint != null
        		&& _lastSelected != null && _lastSelected instanceof IGraphNode )
        {	
        	IGraphNode nodeObj = (IGraphNode) _lastSelected;
        	
        	Point pt1 = nodeObj.getCenter();
        	
        	g.drawLine(pt1.x, pt1.y, _draggingPoint.x, _draggingPoint.y);
        }
        
        // 노드 그리기. 연결 중인 경우 노드가 연결 가능한 지 표현될 수 있도록 기능 추가
        for(IGraphNode nodeObj : _nodes)
            nodeObj.draw(g, scrollPos);
        
        // 선택모드인 경우
        if( _status == Status.eSelecting )
        {
        	DrawingTool.drawDashedRect(g, DrawingTool.getRectangle(_lastPressedPoint, _draggingPoint) );
        }
        
        if( _eventHandler != null )
            _eventHandler.OnPainting();
    }

    /**
     * 
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
    }
    
    /**
     * 해당 위치에 그래프 객체가 있다면 해당 객체를 반환하고, 없다면 null을 반환 함.
     * @param x     X 위치 (pixel)
     * @param y     Y 위치 (pixel)
     * @return
     */
    public IGraphObject hitTest(int x, int y)
    {
    	for(int i = _nodes.size() - 1; i >= 0 ; --i)
    	{
            IGraphNode nodeObj = _nodes.get(i);
            if( nodeObj.isHitted(x, y) )
                return nodeObj;
    	}
        
    	for(int i = _links.size() - 1; i >= 0 ; --i)
    	{
    		IGraphLink linkObj = _links.get(i);
            if( linkObj.isHitted(x, y) )
                return linkObj;
    	}

        return null;
    }
    
    /**
     * 모든 노드와 연결선을 선택하는 메소드
     */
    public void selectAll()
    {
        _selectedNodes.clear();
        _selectedLinks.clear();
        
        for(IGraphNode nodeObj : _nodes)
            nodeObj.setSelected(true);
    
        for(IGraphLink linkObj : _links)
            linkObj.setSelected(true);
        
        _selectedNodes.addAll(_nodes);
        _selectedLinks.addAll(_links);
        
        repaint();
    }
    
    /**
     * 지정된 Object를 선택 상태로 변경함.
     * 
     * @param object    선택할 그래프 객체. null이 올 수 있으며 null 이고 add가 false라면 모든 객체가 선택 해제됨.
     * @param add       기존 선택된 것을 유지하고 추가할 지 여부.
     *                  false라면 기존 선택된 것은 무시하고 입력된 객체만 선택되게 됨.
     */
    public void selectObject(IGraphObject object, boolean add)
    {   
        if( !add )
        {
            _selectedNodes.clear();
            _selectedLinks.clear();
            
            for(IGraphNode nodeObj : _nodes)
                nodeObj.setSelected(false);
        
            for(IGraphLink linkObj : _links)
                linkObj.setSelected(false);
        }

        // null 이거나 선택할 수 없는 객체라면 그냥 리턴
        if( object == null || !object.isSelectable() )
            return;
            
        object.setSelected(true);

        if( object instanceof IGraphNode && !_selectedNodes.contains(object) )
            _selectedNodes.add( (IGraphNode) object );
        else if( object instanceof IGraphLink && !_selectedLinks.contains(object) )
            _selectedLinks.add( (IGraphLink) object );
    }

    public List<IGraphNode> getSelectedNodes()
    {
        return _selectedNodes;
    }
    
    public List<IGraphLink> getSelectedLinks()
    {
        return _selectedLinks;
    }
    
    /**
     * 노드 연결 시작.
     * @param startNode
     * @param linkType      연결 종류 지정. 0이면 기본 연결임.
     */
    public void startConnecting(IGraphNode startNode, int linkType)
    {
        if( !startNode.canBeStartingNode() )
            return;
        
        changeStatus( Status.eConnecting );
        _lastSelected = startNode;
        
        for(IGraphNode nodeObj : _nodes)
            if( nodeObj != startNode)
                nodeObj.setStateToConnecting( startNode, linkType );
        
        _connectingLinkType = linkType;
    }
    
    private void endConnecting(IGraphNode endNode)
    {
        if( _status != Status.eConnecting )
            return;

        connectNodes( (IGraphNode) _lastSelected, endNode, _connectingLinkType );
    }
    
    /**
     * 현재 추가된 연결선 객체 반환. 이런 메소드는 지양하고 싶지만...
     * @return
     */
    public List<IGraphLink> getLinks()
    {
        return _links;
    }
    
    public List<IGraphNode> getNodes()
    {
        return _nodes;
    }
}
