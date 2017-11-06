package com.isaac.smartdrawer;

import android.content.Context;
import android.util.AttributeSet;

import org.xclcharts.common.DensityUtil;
import org.xclcharts.view.ChartView;

public class DemoView extends ChartView{

	
	public DemoView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	 public DemoView(Context context, AttributeSet attrs){   
	        super(context, attrs);   
	        
	 }
	 
	 public DemoView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		
	 }
	
	//Demo中bar chart所使用的默认偏移值。
	//偏移出来的空间用于显示tick,axistitle....
	protected int[] getBarLnDefaultSpadding()
	{
		int [] ltrb = new int[4];
		ltrb[0] = DensityUtil.dip2px(getContext(), 40); //left	
		ltrb[1] = DensityUtil.dip2px(getContext(), 60); //top	
		ltrb[2] = DensityUtil.dip2px(getContext(), 20); //right	
		ltrb[3] = DensityUtil.dip2px(getContext(), 40); //bottom						
		return ltrb;
	}
	
	protected int[] getPieDefaultSpadding()
	{
		int [] ltrb = new int[4];
		ltrb[0] = DensityUtil.dip2px(getContext(), 20); //left	
		ltrb[1] = DensityUtil.dip2px(getContext(), 65); //top	
		ltrb[2] = DensityUtil.dip2px(getContext(), 20); //right		
		ltrb[3] = DensityUtil.dip2px(getContext(), 20); //bottom						
		return ltrb;
	}
		
	@Override  
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
        super.onSizeChanged(w, h, oldw, oldh);  
    
    }  

}
