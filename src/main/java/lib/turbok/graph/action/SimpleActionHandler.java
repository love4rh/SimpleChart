package lib.turbok.graph.action;

import java.awt.Point;
import java.util.List;

import lib.turbok.graph.DiagramEditor;
import lib.turbok.graph.object.IGraphLink;
import lib.turbok.graph.object.IGraphNode;
import lib.turbok.graph.object.IGraphObject;
import lib.turbok.graph.object.SimpleLink;
import lib.turbok.graph.object.SimpleNode;



public class SimpleActionHandler implements IGraphActionHandler
{
    private DiagramEditor       _diagramEditor = null;
    
    
    public SimpleActionHandler(DiagramEditor diagramEditor)
    {
        _diagramEditor = diagramEditor;
    }
    
    @Override
    public void OnAddedGraphObject(IGraphObject graphObj)
    {
        //
    }
    
    @Override
    public void OnContextMenu( List<IGraphNode> relatedNode
                             , List<IGraphLink> relatedLink, Point pt )
    {
        System.out.println("Show ContextMenu "); 
    }

    @Override
    public void OnClickObject(IGraphObject selectedObj, Point pt)
    {
        System.out.println("Clicked on " + (selectedObj == null ? "All" : selectedObj));
    }
    
    @Override
    public void OnDblClick(IGraphObject selectedObj, Point pt)
    {
        System.out.println("Double-Clicked on " + (selectedObj == null ? "All" : selectedObj));
        
        if( selectedObj == null && _diagramEditor != null )
        {
            _diagramEditor.addGraphNode( new SimpleNode(0, "Node " + System.currentTimeMillis(), pt )
                                       , false );
        }
    }

    @Override
    public boolean OnConnectingNodes(IGraphNode startNode, IGraphNode endNode, int linkType)
    {
        if( _diagramEditor == null )
            return false;
        
        // 이미 연결되어 있는 경우이므로 그냥 리턴
        if( null != _diagramEditor.getLink(startNode, endNode) )
            return false;
        
        _diagramEditor.addGraphLink( new SimpleLink(startNode, endNode, "Link")
                                   , true );
        
        return true;
    }
    
    @Override
    public void OnDiconnectingLink(IGraphNode startNode, IGraphNode endNode, int linkType)
    {
        //
    }


    @Override
    public void OnNodeMoved(List<IGraphNode> movedNode)
    {
        //
    }

    @Override
    public void OnChangeNodeSelection(List<IGraphNode> selectedNode)
    {
        //
    }

    @Override
    public void OnDeletingNode(IGraphNode nodeObj)
    {
        //
    }

    @Override
    public void OnPainting()
    {
        //
    }

    @Override
    public void OnKeyTyped(int keyCode, boolean ctrlPressed, boolean shiftPressed, boolean altPressed)
    {
        //
    }

}
