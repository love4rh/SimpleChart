package lib.turbok.chart;



/**
 * Base class of chart objects.
 * 
 * @author TurboK
 */
public abstract class ChartObject
{
   private boolean      _visible = true;
   
   private ChartPanel   _chartWnd = null;
   
   
   public ChartObject()
   {
       //
   }
   
   public ChartObject(ChartPanel chartWnd)
   {
       _chartWnd = chartWnd;
   }
   
   public final boolean isVisible()
   {
       return _visible;
   }
   
   public final void setVisible(boolean b)
   {
       _visible = b;
   }
   
   public final ChartPanel chartWnd()
   {
       return _chartWnd;
   }
   
   public final void setParent(ChartPanel chartWnd)
   {
       _chartWnd = chartWnd;
   }
   
   public final ChartOption option()
   {
       return _chartWnd == null ? null : _chartWnd.option();
   }
}
