package lib.turbok.chart.example;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import lib.turbok.chart.ChartPanel;
import lib.turbok.chart.ChartRegion;
import lib.turbok.chart.data.X1X2YMemoryValues;
import lib.turbok.chart.data.XYMemoryValues;
import lib.turbok.chart.plot.*;
import java.awt.BorderLayout;
import javax.swing.JScrollBar;



@SuppressWarnings("serial")
public class MonitoringWndDemo extends JFrame
{
    private ChartPanel          _chartPanel = null;

    private JScrollBar          _vertScroll = null;
    
    
    public MonitoringWndDemo(final String title)
    {
        super(title);
        
        initializeComponent();
        initilaizeEventHandler();
        
        resizedLayout();
    }
    
    private void initializeComponent()
    {
        final int rX = 1;
        final int rY = 4;
        
        Container contentPane = getContentPane();
        
        contentPane.setPreferredSize(new Dimension(900, 650));
        
        _chartPanel = new ChartPanel(rX, rY, 20, 20);
        _chartPanel.setTitle("Monitoring");
        
        contentPane.add(_chartPanel, BorderLayout.CENTER);

        _vertScroll = new JScrollBar();
        contentPane.add(_vertScroll, BorderLayout.EAST);
        
        long endTime = System.currentTimeMillis();

        for(int x = 0; x < rX; ++x)
            for(int y = 0; y < rY; ++y)
                addDemoChart(endTime, x, y);
    }
    
    private void initilaizeEventHandler()
    {
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent evt)
            {
                resizedLayout();
            }
        });
        
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                System.out.println("Exit...");

                dispose();
                System.exit(0);
            }
        });
    }
    
    protected void resizedLayout()
    {
        // TODO
    }
    
    private void addDemoChart(long endTime, int rx, int ry)
    {
        final long range = 3600 * 24 * 1000;    // 1일
        long startTime = endTime - range;
        long adjStart = startTime / 30000 * 30000;  // 30초 부터
        
        XYMemoryValues memoryUsage = new XYMemoryValues();
        XYMemoryValues cpuUsage = new XYMemoryValues();
        
        for(double v = adjStart; v <= endTime + 30000; v += 30000)
        {
            memoryUsage.addValue(v, 100.0 + Math.random() * 2048);
            cpuUsage.addValue(v, Math.random() * 99.0);
        }
        
        ChartRegion chartRgn = _chartPanel.getChartRegion(rx, ry);

        if( ry == 0 )
        {
            chartRgn.setDateTimeAxis(ChartPanel.AXIS_TOP, "Time")
                    .setMinimumMaximum(startTime, endTime)
                    .setAutoRange(false)
                    .setVisible(true)
                    ;
        }
        
        chartRgn.setDateTimeAxis(ChartPanel.AXIS_BOTTOM, "Time")
                .setMinimumMaximum(startTime, endTime)
                .setAutoRange(false)
                .setVisible(false)
                ;
        
        chartRgn.getAxisLeft()
                .setMinimumMaximum(0, 100)
                .setAutoRange(false)
                .setTitle("CPU (%)")
                ;
        
        chartRgn.getAxisRight()
                .setMinimumMaximum(0, 3072)
                .setAutoRange(false)
                .setTitle("Memory (MB)")
                ;
        
        chartRgn.getAxisRight().setVisible(true);
        
        chartRgn.setAxisArea(60, 50, 67, 50);
        
        int yAxis = ry == 0 ? ChartPanel.AXIS_TOP : ChartPanel.AXIS_BOTTOM;
        
        chartRgn.addPlot(new AreaPlot(memoryUsage), yAxis, ChartPanel.AXIS_RIGHT);
        chartRgn.addPlot(new LinePlot(cpuUsage), yAxis, ChartPanel.AXIS_LEFT);
        
        // Range Plot
        X1X2YMemoryValues x1x2yValue = new X1X2YMemoryValues();
        
        int jobCount = 8;
        for(int i = 1; i <= jobCount; ++i)
        {
            double x1 = startTime + (range / (jobCount + 1)) * Math.random() * i;
            double len = (Math.random() + 1)  * 600000;
            
            x1x2yValue.addValue(x1, x1 + len, (i + 0.5) * 10);
        }
        
        chartRgn.addPlot(new RangePlot(x1x2yValue, true));
    }
    
    public static void centerFrameOnScreen(JFrame frame)
    {
        double horizontalPercent = 0.5;
        double verticalPercent = 0.5;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle s = new Rectangle(0, 0, screenSize.width, screenSize.height);
        Dimension f = frame.getSize();
        
        int w = Math.max(s.width - f.width, 0);
        int h = Math.max(s.height - f.height, 0);
        int x = (int)(horizontalPercent * (double)w) + s.x;
        int y = (int)(verticalPercent * (double)h) + s.y;

        frame.setBounds(x, y, f.width, f.height);
    }

    public static void main(final String[] args)
    {
        final MonitoringWndDemo demo = new MonitoringWndDemo("Monitoring Demo");
        
        demo.pack();
        centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}
