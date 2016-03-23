package lib.turbok.graph.object;

import java.awt.Graphics;
import java.awt.Point;


/**
 * Graph 상의 객체를 표현하기 위한 인터페이스
 * 
 * @author TurboK
 *
 */
public interface IGraphObject
{
    /**
     * 객체 ID 반환
     */
    public int getID();
    
    /**
     * 객체 그리기
     * @param g         drawing 할 Graphic 객체
     * @param scrollPos 스크롤 위치
     */
    public void draw(Graphics g, Point scrollPos);

    /**
     * 선택가능한 지 여부 반환. 선택이 안 되는 객체도 있음
     * @return
     */
    public boolean isSelectable();
    
    /**
     * 선택여부 반환. TODO 상태로 확장해야 할까?
     */
    public boolean isSelected();
    
    /**
     * 선택여부 지정
     * @param selected
     */
    public void setSelected(boolean selected);
    
    /**
     * (x, y) 위치를 클릭했을 때 이 객체가 선택되는지 여부 반환
     * @param x
     * @param y
     * @return
     */
    public boolean isHitted(int x, int y);
}
