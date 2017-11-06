package com.isaac.smartdrawer;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Isaac on 2016/8/2 0002.
 */
public abstract class History {
    public static final String ID = "id";
    public static final String OPTION = "option";
    public static final String DATE = "time";

    public static final int TYPE_PARENT = 0;
    public static final int TYPE_CHILD = 1;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static class Parent extends History{
        private static int sId = 0;
        private int mId = sId;
        private Date mDate;
        private List<History.Child> mChildren;
        private boolean mIsExpanded;

        public Parent(Date date, List<History.Child> childs) {
            mDate = date;
            mChildren = childs;
            mIsExpanded = false;
            sId++;
        }

        @Override
        public Integer getId() {
            return mId;
        }

        public Date getDate() {
            return mDate;
        }

        public List<History.Child> getChildItemList() {
            return mChildren;
        }

        @Override
        public int getType() {
            return TYPE_PARENT;
        }

        public boolean isExpanded() {
            return mIsExpanded;
        }

        public void setExpanded(boolean expanded) {
            mIsExpanded = expanded;
        }
    }

    public static class Child extends History{
        private String mId;
        private String mOption;
        private Date mDate;

        public Child(String id, String option, Date date) {
            mId = id;
            mDate = date;
            mOption = option;
        }

        public String getMId() {
            return mId;
        }

        public String getOption() {
            return mOption;
        }

        public Date getDate() {
            return mDate;
        }

        @Override
        public int getType() {
            return TYPE_CHILD;
        }

        @Override
        public Integer getId() {
            return null;
        }
    }

    public abstract int getType();

    public abstract Integer getId();

    public static String tStrFormat(Date date) {
        return sdf.format(date);
    }

    public static Date tDateFormat(String string) {
        Date date = new Date();
        try {
            date = sdf.parse(string);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return date;
    }


}
