package com.isaac.smartdrawer;

public class Item {
	public Day[] day = new Day[33];
	private String name = "";
	private double outTime = 0;
	private double inTime = 0;
	public Item()
	{
		for(int i = 0; i <= 32; i++)
		{
			day[i] = new Day();
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getOutTime() {
		return outTime;
	}
	public void setOutTime(double outTime) {
		this.outTime = outTime;
	}
	public double getInTime() {
		return inTime;
	}
	public void setInTime(double inTime) {
		this.inTime = inTime;
	}
	
}
