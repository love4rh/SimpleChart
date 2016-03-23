package lib.turbok.graph.action;

import lib.turbok.graph.object.IGraphObject;
import lib.turbok.util.DynamicParam;


/**
 * Graph에 행해지는 Action을 정의하기 위한 인터페이스 클래스
 * 
 * @author TurboK
 *
 */
public interface IGraphAction
{
    /**
     * Action 에 영향을 받은 (혹은 받는) 객체 목록 반환
     * @return
     */
    public IGraphObject[] getGraphObject();
    
    /**
     * Action 이전의 객체 속성값.
     * @return
     */
    public DynamicParam getOldProperty();
    
    /**
     * ACtion 이후의 객체 속성값
     * @return
     */
    public DynamicParam getNewProperty();
}

