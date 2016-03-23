package lib.turbok.graph;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import lib.turbok.graph.action.SimpleActionHandler;


public class HelloWorld extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	public HelloWorld()
	{
		super("Hello, World!");

		DiagramEditor graphComponent = new DiagramEditor();

		graphComponent.setEventHandler( new SimpleActionHandler(graphComponent) );

		getContentPane().add( new JScrollPane(graphComponent) );
	}

	public static void main(String[] args)
	{
		HelloWorld frame = new HelloWorld();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 320);
		frame.setVisible(true);
	}

}
