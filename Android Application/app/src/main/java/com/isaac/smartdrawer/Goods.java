package com.isaac.smartdrawer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by admin on 2016/7/30.
 */
public class Goods {
    private String id, name, category, date;
    private int date_year, date_month, date_day, date_hour, date_minute;
    private int exist;
    private Calendar calendar;
    private Date dates;

    public Goods(String id, String name, String category, int exist, String date) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.exist = exist;
        this.date = date;
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            dates = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(dates);
        date_year = calendar.get(Calendar.YEAR);
        date_month = calendar.get(Calendar.MONTH);
        date_day = calendar.get(Calendar.DAY_OF_MONTH);
        date_hour = calendar.get(Calendar.HOUR_OF_DAY);
        date_minute = calendar.get(Calendar.MINUTE);
    }

    public Goods(Content content, int exist, String date) {
        name = content.getName();
        id = content.getId();
        category = content.getCategory();
        this.exist = exist;
        this.date = date;
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            dates = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(dates);
        date_year = calendar.get(Calendar.YEAR);
        date_month = calendar.get(Calendar.MONTH);
        date_day = calendar.get(Calendar.DAY_OF_MONTH);
        date_hour = calendar.get(Calendar.HOUR_OF_DAY);
        date_minute = calendar.get(Calendar.MINUTE);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public int getExist() {
        return exist;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDate_year() {
        return date_year;
    }

    public int getDate_month() {
        return date_month + 1;
    }

    public int getDate_day() {
        return date_day;
    }

    public int getDate_hour() {
        return date_hour;
    }

    public int getDate_minute() {
        return date_minute;
    }

}
