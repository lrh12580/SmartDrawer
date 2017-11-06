package com.isaac.smartdrawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import org.xclcharts.chart.PieChart;
import org.xclcharts.chart.PieData;
import org.xclcharts.common.DensityUtil;
import org.xclcharts.event.click.ArcPosition;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.plot.PlotLegend;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PieChartView extends DemoView implements Runnable {

    private String TAG = "PieChartView";
    private PieChart chart = new PieChart();
    private ArrayList<PieData> chartData = new ArrayList<PieData>();
    public static ArrayList<String> history = new ArrayList<>();
    public static int[] numbers;
    public static int total = 0;
    public static int selectedID = -1;

    public PieChartView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        chartDataSet();
        chartRender();
        //綁定手势滑动事件
        this.bindTouch(this, chart);
        new Thread(this).start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //图所占范围大小
        chart.setChartRange(w, h);
    }


    public void chartRender() {
        try {

            //设置绘图区默认缩进px值
            int[] ltrb = getPieDefaultSpadding();
            float right = DensityUtil.dip2px(getContext(), 100);
            chart.setPadding(ltrb[0], ltrb[1], right, ltrb[3]);
            setTitle();

            //设置起始偏移角度(即第一个扇区从哪个角度开始绘制)
            //chart.setInitialAngle(90);

            //标签显示(隐藏，显示在中间，显示在扇区外面)
            chart.setLabelStyle(XEnum.SliceLabelStyle.INSIDE);
            chart.getLabelPaint().setColor(Color.WHITE);

            //chart.setDataSource(chartData);

            //激活点击监听
            chart.ActiveListenItemClick();
            chart.showClikedFocus();

            //设置允许的平移模式
            chart.disablePanMode();
            chart.disableScale();
//            chart.setPlotPanMode(XEnum.PanMode.HORIZONTAL);

            //显示图例
            PlotLegend legend = chart.getPlotLegend();
            Log.d("PiescreenWidth", PieChartViewActivity.screenWidth+"");
            legend.getPaint().setTextSize((float)PieChartViewActivity.screenWidth / 36);
            legend.show();
            legend.setType(XEnum.LegendType.COLUMN);
            legend.setHorizontalAlign(XEnum.HorizontalAlign.RIGHT);
            legend.setVerticalAlign(XEnum.VerticalAlign.MIDDLE);
            legend.showBox();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
        }
    }

    public void setTitle() {
        chart.setTitle("您的物品使用频率总览");
        //标题
        chart.getPlotTitle().setVerticalAlign(XEnum.VerticalAlign.BOTTOM);
        chart.getPlotTitle().setTitleAlign(XEnum.HorizontalAlign.CENTER);
        chart.getPlotTitle().getTitlePaint().setColor(Color.BLACK);
        chart.getPlotTitle().getTitlePaint().setTextSize(PieChartViewActivity.screenWidth/18);
    }

    public void chartDataSet() {
        int[] color = {Color.GREEN, Color.LTGRAY, Color.BLACK, Color.BLUE, Color.CYAN,
                Color.DKGRAY, Color.GRAY, Color.MAGENTA, Color.RED, Color.YELLOW };
        for(int i = 0; i < numbers.length; i++) {
            double percent = (double)100*numbers[i]/total;
            BigDecimal bg = new BigDecimal(percent);
            String percents = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+"%";
            chartData.add(new PieData(history.get(i), percents, percent, color[i%10]));
        }
    }

    @Override
    public void render(Canvas canvas) {
        try {
            chart.render(canvas);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            chartAnimation();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public void chartAnimation() {
        try {

            chart.setDataSource(chartData);
            chart.getLabelPaint().setTextSize((float) PieChartViewActivity.screenWidth / 27);

            int count = 360 / 10;

            for (int i = 1; i < count; i++) {
                Thread.sleep(40);

                chart.setTotalAngle(10 * i);

                //激活点击监听
                if (count - 1 == i) {
                    chart.setTotalAngle(360);

                    chart.ActiveListenItemClick();
                    //显示边框线，并设置其颜色
                    chart.getArcBorderPaint().setColor(Color.YELLOW);
                    chart.getArcBorderPaint().setStrokeWidth(3);
                }

                postInvalidate();
            }

        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (chart.isPlotClickArea(event.getX(), event.getY())) {
                triggerClick(event.getX(), event.getY());
            }
        }
        return true;
    }

    //触发监听
    public void triggerClick(float x, float y) {
        if (!chart.getListenItemClickStatus()) return;

        ArcPosition record = chart.getPositionRecord(x, y);
        if (null == record) return;
        //用于处理点击时弹开，再点时弹回的效果
        PieData pData = chartData.get(record.getDataID());
        if (record.getDataID() == selectedID) {
            boolean bStatus = chartData.get(selectedID).getSelected();
            chartData.get(selectedID).setSelected(!bStatus);
        } else {
            if (selectedID >= 0)
                chartData.get(selectedID).setSelected(false);
            pData.setSelected(true);
        }
        selectedID = record.getDataID();
        Toast.makeText(this.getContext(), chartData.get(selectedID).getKey()+"的使用频率为"+
        chartData.get(selectedID).getLabel()+"。", Toast.LENGTH_SHORT).show();
        this.refreshChart();
    }

    public String getChartData(int i) {
        return chartData.get(i).getKey();
    }

}
