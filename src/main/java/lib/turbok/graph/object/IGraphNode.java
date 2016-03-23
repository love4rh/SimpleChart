package lib.turbok.graph.object;

import java.awt.Point;
import java.awt.Rectangle;


/**
 * Graph 상의 Node를 표현하기 위한 인터페이스
 * 
 * @author TurboK
 *
 */
public interface IGraphNode extends IGraphObject
{
    /**
     * 노드 이름 반환
     * @return
     */
    public String getName();
    
    /**
     * 객체가 차지하는 영역 반환
     */
    public Rectangle getBounds();
    
    /**
     * 객체의 중심 위치 반환. 정 가운데 일 필요는 없음
     */
    public Point getCenter();
    
    /**
     * 노드의 위치 설정
     * 
     * @param x
     * @param y
     */
    public void setPosition(int x, int y);
    
    /**
     * 지정된 차이만큼 객체의 위치를 이동하기 위한 메소드
     * @param dx   X 축 이동크기
     * @param dy   Y 축 이동크기
     * @return 이동한 뒤 위치 및 크기
     */
    public Rectangle moveWithOffset(int dx, int dy);
    
    /**
     * 입력된 노드로 부터 이 노드로 연결될 수 있는 지 여부 반환
     * 
     * @param from
     * @param linkType  연결의 종류
     * @return
     */
    public boolean isConnectable(IGraphNode from, int linkType);
    
    /**
     * 이 노드가 to 노드로 연결될 때 발생하는 이벤트 처리
     * @param to
     * @param linkType  연결의 종류
     */
    public void onConnectingTo(IGraphNode to, int linkType);
    
    /**
     * 이 노드가 from 노드로 부터 연결될 때 발생하는 이벤트 처리
     * @param from
     * @param linkType  연결의 종류
     */
    public void onConnectingFrom(IGraphNode from, int linkType);
    
    /**
     * 이 노드가 to 노드로 연결이 끊어질 때 발생하는 이벤트 처리 핸들러
     * @param to
     * @param linkType  연결의 종류
     */
    public void onDisconnectingTo(IGraphNode to, int linkType);
    
    /**
     * 이 노드가 from 노드로 부터 연결이 끊어질 때 발생하는 이벤트 처리 핸들러
     * @param from
     * @param linkType  연결의 종류
     */
    public void onDisconnectingFrom(IGraphNode from, int linkType);
    
    /**
     * 상태를 연결 중 상태로 변경
     * @param from   연결 대상 노드
     * @param linkType  연결의 종류
     */
    public void setStateToConnecting(IGraphNode from, int linkType);
    
    /**
     * 일반적인 상태로 설정하라는 메소드
     */
    public void setStateToNormal();
    
    /**
     * 노드의 상태를 대변하는 메소드. 경우에 따라 정하면 됨
     * @param status
     * @return
     */
    public boolean isStatus(int status);
    
    /**
     * 시작 노드가 될 수 있는 지 여부 반환.
     * @return
     */
    public boolean canBeStartingNode();
}

