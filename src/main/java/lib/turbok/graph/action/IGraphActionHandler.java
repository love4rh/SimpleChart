package lib.turbok.graph.action;

import java.awt.Point;
import java.util.List;

import lib.turbok.graph.object.IGraphLink;
import lib.turbok.graph.object.IGraphNode;
import lib.turbok.graph.object.IGraphObject;


/**
 * Graph에 행해지는 Action을 처리하기 위한 핸들러
 * 
 * @author TurboK
 *
 */
public interface IGraphActionHandler
{
    /**
     * 그래프 상 객체가 추가되었을 때 발생하는 이벤트
     * @param graphObj
     */
    public void OnAddedGraphObject(IGraphObject graphObj);
    
    /**
     * Context-Menu 띄우기 위한 핸들러.
     * relatedNode와 relatedLink는 null 일수도 있으며 이런 경우는 객체 선택 없이 Context-Menu를 띄우는 경우임
     * 
     * @param relatedNode   선택된 Node 객체 목록
     * @param relatedLink   선택된 Link 객체 목록
     * @param pt            클릭된 위치
     */
    public void OnContextMenu( List<IGraphNode> relatedNode
                             , List<IGraphLink> relatedLink, Point pt );

    /**
     * 객체가 클릭 되었을 때 발생하는 이벤트 핸들러
     * @param selectedObj
     * @param pt
     */
    public void OnClickObject(IGraphObject selectedObj, Point pt);
    
    /**
     * 노드가 더블 클릭 시 발생하는 이벤트 핸들러
     * 
     * @param selectedObj
     * @param pt            클릭된 위치
     */
    public void OnDblClick(IGraphObject selectedObj, Point pt);
    
    /**
     * 두 노드를 연결할 때 발생하는 이벤트 핸들러
     * @param startNode     연결될 시작 위치의 노드
     * @param endNode       연결될 끝 위치의 노드
     * @param linkType
     * @return  연결 성공여부
     */
    public boolean OnConnectingNodes(IGraphNode startNode, IGraphNode endNode, int linkType);
    
    /**
     * 두 노드 연결이 해제될 때 발생하는 이벤트 핸들러
     * @param startNode
     * @param endNode
     * @param linkType
     */
    public void OnDiconnectingLink(IGraphNode startNode, IGraphNode endNode, int linkType);
    
    /**
     * 노드의 위치가 변경되었을 때 발생하는 이벤트 핸들러
     * @param movedNode     위치가 변경된 노드
     */
    public void OnNodeMoved(List<IGraphNode> movedNode);
    
    /**
     * 노드 선택이 변경되었을 경우 발생하는 이벤트 핸들러
     * @param selectedNode
     */
    public void OnChangeNodeSelection(List<IGraphNode> selectedNode);
    
    /**
     * 노드가 삭제될 때 발생하는 이벤트
     * @param nodeObj
     */
    public void OnDeletingNode(IGraphNode nodeObj);
    
    /**
     * 다이어그램 새로 paint() 했을 경우 발생하는 이벤트
     */
    public void OnPainting();
    
    /**
     * 다이어그램에서 발생하는 Key 이벤트 처리
     * @param event
     */
    public void OnKeyTyped(int keyCode, boolean ctrlPressed, boolean shiftPressed, boolean altPressed);
}
