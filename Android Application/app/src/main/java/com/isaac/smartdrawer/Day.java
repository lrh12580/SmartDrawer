package com.isaac.smartdrawer;

public class Day {
	public static final int MAX = 100;
	private int cnt_out = 0;
	private int cnt_in = 0;
	private boolean exist = true;
	private int[] outHour = new int[MAX];
	private int[] outMinute = new int[MAX];
	private int[] inHour = new int[MAX];
	private int[] inMinute = new int[MAX];
	
	public Day()
	{
		int i;
		for(i = 1; i < MAX; i++)
		{
			outHour[i] = -1;
			inHour[i] = -1;
		}
	}
	public int getInTime(int x)
	{
		return inHour[x]*60 + inMinute[x];
	}
	
	public int getOutTime(int x)
	{
		return outHour[x]*60 + outMinute[x];
	}
	
	public boolean isExist()
	{
		return exist; 
	}
	
	public void setExist(boolean exist)
	{
		this.exist = exist;
	}
	
	public void in(int hh, int minute)
	{
		inHour[++cnt_in] = hh;
		inMinute[cnt_in] = minute;
	}
	
	public void out(int hh, int minute)
	{
		outHour[++cnt_out] = hh;
		outMinute[cnt_out] = minute;
	}
	

	public int getCnt_in() {
		return cnt_in;
	}
	public void setCnt_in(int cnt_in) {
		this.cnt_in = cnt_in;
	}
	public int getCnt_out() {
		return cnt_out;
	}
	public void setCnt_out(int cnt_out) {
		this.cnt_out = cnt_out;
	}
	public int[] getOutHour() {
		return outHour;
	}
	public void setOutHour(int[] outHour) {
		this.outHour = outHour;
	}
	public int[] getOutMinute() {
		return outMinute;
	}
	public void setOutMinute(int[] outMinute) {
		this.outMinute = outMinute;
	}
	public int[] getInHour() {
		return inHour;
	}
	public void setInHour(int[] inHour) {
		this.inHour = inHour;
	}
	public int[] getInMinute() {
		return inMinute;
	}
	public void setInMinute(int[] inMinute) {
		this.inMinute = inMinute;
	}
}
