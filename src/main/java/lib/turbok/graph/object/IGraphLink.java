package lib.turbok.graph.object;

import java.awt.Point;


/**
 * Graph 상의 Node를 표현하기 위한 인터페이스
 * 
 * @author TurboK
 *
 */
public interface IGraphLink extends IGraphObject
{
    /**
     * 연결선의 시작 위치에 있는 객체 반환
     */
    public IGraphNode from();
    
    /**
     * 연결선의 끝 위치에 있는 객체 반환
     */
    public IGraphNode to();
    
    /**
     * 연결선 시작 위치의 포인트 반환
     * @return
     */
    public Point getPointStart();
    
    /**
     * 연결선 끝 위치의 포인트 반환
     * @return
     */
    public Point getPointEnd();
    
    /**
     * 연결선의 형태 반환
     * @return
     */
    public int getType();
}
