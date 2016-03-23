package lib.turbok.graph.tool;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.turbok.graph.object.IGraphLink;
import lib.turbok.graph.object.IGraphNode;
import lib.turbok.util.RunProcess;
import lib.turbok.util.TextFileLineReader;



/**
 * GraphViz 라이브러리에서 제공하는 dot 유틸을 이용하여 Node의 위치를 다시 계산하는 클래스
 * 
 * @author mh9.kim
 *
 */
public class GraphVizDot
{   
    private List<IGraphLink>    _links = null;
    
    
    public GraphVizDot()
    {
        //
    }
    
    public void addLink(IGraphLink link)
    {
        if( _links == null )
            _links = new ArrayList<IGraphLink>();
        
        _links.add(link);
    }
    
    /**
     * 자동 Layout 실행
     * 
     * @param dotPath   GraphViz의 dot.exe 경로
     * @param tempPath  임시파일을 생성할 폴더
     * @return Graph의 전체 크기 반환
     * @throws Exception
     */
    public Dimension doAutoLayout(String dotPath, String tempPath) throws Exception
    {
        if( _links == null || _links.isEmpty() )
            throw new IllegalArgumentException("No link data.");
        
        Map<String, IGraphNode> nodeMap = new HashMap<String, IGraphNode>();
        
        File dotFile = File.createTempFile("graphViz", "_in.dmap", new File(tempPath));

        try
        {
            Writer outWriter = new BufferedWriter(new OutputStreamWriter(
                                        new FileOutputStream(dotFile), "UTF-8"));
            
            outWriter.write("digraph G {\n");
            
            for(IGraphLink link : _links)
            {
                IGraphNode from = link.from();
                IGraphNode to = link.to();
                
                String fName = from.getName().replace(" ", "_");
                String tName = to.getName().replace(" ", "_");
                
                outWriter.write("\"" + tName + "\" -> \"" + fName + "\" [weight=50];\n");
                
                nodeMap.put(fName.toUpperCase(), from);
                nodeMap.put(tName.toUpperCase(), to);
            }
            
            outWriter.write("}\n");
            outWriter.flush();
            outWriter.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return null;
        }
        
        File outFile = File.createTempFile("graphViz", "_out.dmap", new File(tempPath));
        
        // dot 실행
        RunProcess proc = new RunProcess();
        
        int runResult = proc.run( dotPath, dotFile.getAbsolutePath()
                                , "-o" + outFile.getAbsolutePath() );
        
        if( runResult != 0 )
        {
            runResult = proc.run(dotPath, "-c");
            
            runResult = proc.run( dotPath, dotFile.getAbsolutePath()
                    , "-o" + outFile.getAbsolutePath() );
        }
        
        // 생성된 결과 정리
        TextFileLineReader lineReader
                = new TextFileLineReader(outFile.getAbsolutePath(), "UTF-8");
        
        // 첫 줄 "digraph G {"는 버립시다.
        String lineText = lineReader.getNextLine();
        
        String blockText = "";
        Dimension canvasSize = new Dimension(0, 0);
        
        while( null != (lineText = lineReader.getNextLine()) )
        {
            // Block을 찾아야 함.
            blockText += lineText;
            
            // 한 Block이 끝났음을 의미함
            if( lineText.endsWith(";") && !blockText.isEmpty() )
            {
                // 노드의 위치가 있는 정보는 아래 조건을 만족하는 경우임
                int pos = blockText.indexOf("pos=\"");
                if( 0 < pos && -1 == blockText.indexOf(" -> ") )
                {
                    // "["을 찾아 앞쪽에 있는 것은 버리고
                    int p1 = blockText.indexOf("[");
                    int p2 = blockText.indexOf("\"", pos + 5);
                    
                    String nodeName = blockText.substring(0, p1 - 1).trim();
                    if( nodeName.startsWith("\"") )
                        nodeName = nodeName.substring(1, nodeName.length() - 1);
                    
                    String posStr = blockText.substring(pos + 5, p2);
                    
                    int p3 = posStr.indexOf(",");
                    String strX = posStr.substring(0, p3);
                    String strY = posStr.substring(p3 + 1);
                    
                    IGraphNode nodePtr = nodeMap.get(nodeName.toUpperCase());
                    if( nodePtr != null )
                    {
                        int posX = 0, posY = 0;
                        
                        try { posX = Integer.parseInt(strX); } catch( Exception e) { posX = 50; }
                        try { posY = Integer.parseInt(strY); } catch( Exception e) { posY = 50; }
                        
                        if( posX > canvasSize.width )
                            canvasSize.width = posX;
                        if( posY > canvasSize.height )
                            canvasSize.height = posY;
                        
                        nodePtr.setPosition(posX, posY);
                    }
                    
                    // System.out.println(nodeName + " : (" + strX + ", " + strY + ") / " + blockText);
                }
                
                blockText = "";
            }
        }
        
        lineReader.close();
        
        dotFile.delete();
        outFile.delete();
        
        canvasSize.width += 100;
        canvasSize.height += 100;
        
        return canvasSize;
    }
}
