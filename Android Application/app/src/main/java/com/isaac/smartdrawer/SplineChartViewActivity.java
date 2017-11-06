package com.isaac.smartdrawer;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SplineChartViewActivity extends Activity {

    SplineChartView mSplineChartView;
    String title;
    private ClientDatabaseHelper mClientDatabaseHelper;
    private Calendar mCalendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置没标题
        mClientDatabaseHelper = ClientDatabaseHelper.getInstance(SplineChartViewActivity.this);
        new InitTask().execute();
    }

    public void initData() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        for (int i = 6; i >= 0; i--) {
            mCalendar.add(Calendar.DATE, -1);
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH)+1;
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            Log.d("View", month + " " + day);
            SplineChartView.dates[i] = month +"."+day;
            SplineChartView.myDates[i] = new MyDate(year, month, day);
        }
        for (int i = 0; i < 7; i++) {
            List<MyDate> myDatess = new ArrayList<>();
            myDatess.addAll(mClientDatabaseHelper.getHistoryNameDate(title, SplineChartView.myDates[i]));
            SplineChartView.number[i] = myDatess.size();
            SplineChartView.calendars[i] = new MyDate[SplineChartView.number[i]];
            for (int k = 0; k < SplineChartView.number[i]; k++) {
                SplineChartView.calendars[i][k] = myDatess.get(k);
            }
        }
    }

    class InitTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            initData();
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("ok")) {
                setContentView(R.layout.splinechartview_layout);
                mSplineChartView = (SplineChartView) findViewById(R.id.splineChartView);
                mSplineChartView.initView(title);
            }
        }
    }
}
