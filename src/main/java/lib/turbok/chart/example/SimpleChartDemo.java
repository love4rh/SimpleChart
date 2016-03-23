package lib.turbok.chart.example;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lib.turbok.chart.data.*;
import lib.turbok.chart.plot.*;

import lib.turbok.chart.ChartPanel;
import lib.turbok.chart.ChartRegion;



@SuppressWarnings("serial")
public class SimpleChartDemo extends JFrame
{
    public SimpleChartDemo(final String title)
    {
        super(title);
        
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

        setupDemoChart();
    }
    
    private void setupDemoChart()
    {
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setPreferredSize(new java.awt.Dimension(900, 600));
        
        this.setContentPane(chartPanel);
        
        ChartPanel chartWnd = createChartWnd();
        chartPanel.add(chartWnd, BorderLayout.CENTER);
        chartWnd.setBounds(0, 0, 900, 300);
    }
    
    private ChartPanel createChartWnd()
    {
        final double gapValue = 1.0;
        final double sampleMax = 100.0;
        
        // Chart Panel 만들기
        ChartPanel chartWnd = new ChartPanel();
        // Chart Panel 내 기본 차팅 영역 선택
        ChartRegion chartRgn = chartWnd.getDefaultRegion();
        
        // 아래 축의 최소/최대, Tick 간격 지정
        chartRgn.getAxisBottom().setMinimum(0)
                                .setMaximum(sampleMax + gapValue * 2)
                                .setTickGap(sampleMax * 0.1);
        
        // 왼쪽 축의 최소/최대, Tick 간격 지정
        chartRgn.getAxisLeft().setMinimum(-50.0)
                              .setMaximum(50.0)
                              .setTickGap(10);

        chartWnd.setPreferredSize(new java.awt.Dimension(900, 300));
        
        // 차팅할 데이터 목록 지정. (X, Y) 2차원 값으로 데이터 객체 정의.
        // 3개를 만들어 각각 다른 종류의 차트에 활용함.
        XYMemoryValues xyValue = new XYMemoryValues();
        XYMemoryValues xyValue2 = new XYMemoryValues();
        XYMemoryValues xyValue3 = new XYMemoryValues();
        
        for(double v = gapValue; v <= sampleMax + gapValue; v += 1.0)
        {
            xyValue.addValue(v, Math.random() * 99.0 - 50.0);
            xyValue2.addValue(v, Math.random() * 99.0 - 50.0);
            xyValue3.addValue(v, Math.random() * 99.0 - 50.0);
        }
        
        // 산포 차트 추가
        chartWnd.addPlot(new ScatterPlot(xyValue));
        // Area Plot 추가
        chartWnd.addPlot(new AreaPlot(xyValue2));
        // 라인 차트 추가
        chartWnd.addPlot(new LinePlot(xyValue3));
        
        // Range Plot
        /*
        X1X2YMemoryValues x1x2yValue = new X1X2YMemoryValues();
        
        for(int i = 0; i < 10; ++i)
        {
            double x1 = Math.random() * sampleMax * 0.9;
            double len = (Math.random() + 1) * sampleMax * 0.1;
            
            x1x2yValue.addValue(x1, x1 + len, (i + 0.5) * 10);
        }
        
        chartWnd.addPlot(new RangePlot(x1x2yValue, true));
        // */
        
        return chartWnd;
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
        final SimpleChartDemo demo = new SimpleChartDemo("Simple Chart Demo");
        
        demo.pack();
        centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}
