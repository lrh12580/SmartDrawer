package com.isaac.smartdrawer;

/**
 * Created by admin on 2016/8/30.
 */
public class MyDate {
    int year, month, day, hour, minute;
    boolean exist;

    public MyDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public MyDate(boolean exist, int hour, int minute) {
        this.exist = exist;
        this.hour = hour;
        this.minute = minute;
    }

    public MyDate(boolean exist, int year, int month, int day, int hour, int minute) {
        this.exist = exist;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getExistName() {
        if (exist == false) {
            return "取走";
        } else {
            return "放回";
        }
    }

}
