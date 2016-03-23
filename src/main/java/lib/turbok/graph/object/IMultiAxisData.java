package lib.turbok.graph.object;

import java.awt.Color;
import java.awt.Stroke;



/**
 * MultiAxisChart를 그리기 위한 데이터를 제공 인터페이스.
 * 
 * 제공해야 하는 데이터로 시간(double), From, To, Link Type Link Description.
 * 데이터는 시간 순으로 정렬된 순서대로 반환해 줘야 함.
 * 
 * @author TurboK
 */
public interface IMultiAxisData
{
    /** 자원 해제 */
    public void clear();
    
    /** 전체 데이터 개수 반환 */
    public int size();
    
    /** row 위치의 시간 값 반환 */
    public double timeValue(int row);
    
    /** row 위치에서 호출한 객체 반환. */
    public String caller(int row);
    
    /** row 위치에서 호출된 객체 반환. */
    public String callee(int row);
    
    /** row 위치에서 호출한 객체의 부가 정보 */
    public String callerTip(int row, boolean detail);
    
    /** row 위치에서 호출된 객체의 부가 정보 */
    public String calleeTip(int row, boolean detail);
    
    /** row 위치 연결의 연결에 대한 정보 */
    public String linkToolTip(int row, boolean detail);

    /** 시간 축 최소 값 */
    public double minTime();
    
    /** 시간 축 최대 값 */
    public double maxTime();
    
    /** Caller, Callee를 합한 축의 총 개수 */
    public int sizeOfAxis();
    
    /** 축의 순서 반환. 만약 입력된 이름의 축이 없으면 -1. */
    public int getAxisIndex(String name);
    
    /** 축 이름 반환 */
    public String getAxisName(int index);
    
    /** 호출 형태에 따른 Stroke 반환 */
    public Stroke getLinkStroke(int row, int selectedRow);
    
    /** 호출 형태에 따른 라인 색상 반환 */
    public Color getLinkColor(int row, int selectedRow);

    /** 입력된 두 데이터가 그룹으로 묶일 수 있는 지 여부 반환 */
    public boolean isGrouped(int r1, int r2);
    
    /** timeVal에 해당하는 데이터 상의 위치 반환. */
    public int searchRow(double timeVal, boolean greater);
    
    /** row에 해당하는 시간 값 반환 */
    public Double getTimeValue(int row);
    
    /** row에 해당하는 Caller의 인덱스 반환 */
    public int getCallerAxisIndex(int row);
}
