package lib.turbok.graph.action;

import java.awt.Dimension;
import java.util.List;

import lib.turbok.graph.object.IGraphLink;
import lib.turbok.graph.object.IGraphNode;


/**
 * MiniMapWnd 컨트롤을 그리기 위한 데이터 및 이벤트를 처리할 인터페이스 클래스
 * 
 * @author TurboK
 *
 */
public interface IMiniMapData
{
    /**
     * 미니맵 상에 나타낼 노드객체 목록 반환
     * @return
     */
    public List<IGraphNode> getNodes();
    
    /**
     * 미니맵 상에 나타낼 노드간 연결객체 목록 반환
     * @return
     */
    public List<IGraphLink> getLinks();
    
    /**
     * 미니맵에 나타낼 그래프의 전체 크기 반환
     * @return
     */
    public Dimension getGraphSize();
    
    /**
     * 그래프가 그려진 화면의 크기 반환 --> 미니맵 상에서 드래깅할 사각형 크기
     * @return
     */
    public Dimension getCanvasSize();
    
    /**
     * 마우스로 드래깅했을 때의 이벤트
     */
    public void onMouseMove(int x, int y);
}
