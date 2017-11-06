package com.isaac.smartdrawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import org.xclcharts.chart.CircleChart;
import org.xclcharts.chart.PieData;
import org.xclcharts.view.GraphicalView;

import java.util.LinkedList;

public class CircleChartView extends GraphicalView {
	
	private String TAG = "CircleChartView";
	private CircleChart chart = new CircleChart();
	
	//设置图表数据源
	private LinkedList<PieData> mlPieData = new LinkedList<PieData>();	
	private String mDataInfo = "";

	public CircleChartView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setPercentage(0);
		chartRender();
	}
	
	public CircleChartView(Context context, AttributeSet attrs){
        super(context, attrs);   
        setPercentage(0);
		chartRender();
	 }
	 
	 public CircleChartView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			setPercentage(0);
			chartRender();
	 }
	
	 
	 @Override  
	    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
	        super.onSizeChanged(w, h, oldw, oldh);  
	       //图所占范围大小
	        chart.setChartRange(w, h);
	    }  
	 
	 
	public void chartRender()
	{
		try {							
			//设置信息			
			chart.setAttributeInfo(mDataInfo); 	
			//数据源
			chart.setDataSource(mlPieData);

			//背景色
			chart.getBgCirclePaint().setColor(Color.rgb(117, 197, 141));
			//深色
			chart.getFillCirclePaint().setColor(Color.rgb(77, 180, 123));
			//信息颜色
			chart.getDataInfoPaint().setColor(Color.rgb(243, 75, 125));

			chart.getDataInfoPaint().setTextSize(PieChartViewActivity.screenWidth/18);
			chart.getLabelPaint().setTextSize(PieChartViewActivity.screenWidth/18);
			//显示边框
			//chart.showRoundBorder();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
		}
	}

	//百分比
	public void setPercentage(int per)
	{					
		//PieData(标签，百分比，在饼图中对应的颜色)
		mlPieData.clear();	
		int color = Color.rgb(243, 75, 125);
		if(per < 50)
		{
			mDataInfo = "整理习惯非常差";
		}else if(per < 80){
			mDataInfo = "还不错哦";
			color = Color.rgb(246, 202, 13);
		}else{
			mDataInfo = "整理习惯良好";
			color = Color.rgb(72, 201, 176);
		}
		mlPieData.add(new PieData(Integer.toString(per)+"分",per,color));
			
	}

	@Override
    public void render(Canvas canvas) {
        try{
            chart.render(canvas);
        } catch (Exception e){
        	Log.e(TAG, e.toString());
        }
    }

}
