package com.isaac.smartdrawer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PieChartViewActivity extends Activity {

    private Button mButtonPie, mButtonCircle;
    private PieChartView mPieChartView;
    public static int screenWidth, screenHeight;
    private static ClientDatabaseHelper mClientDatabaseHelper;

    public static Intent newIntent(Context context) {
        return new Intent(context, PieChartViewActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mClientDatabaseHelper = ClientDatabaseHelper.getInstance(this);
        new InitTask().execute();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置没标题
    }

    public void initData() {
        int count = mClientDatabaseHelper.getHistoryCount();
        List<Goods> strings = new ArrayList<>();
        strings.addAll(mClientDatabaseHelper.getHistories());
        first:for (int i = 0; i < count; i++) {
            String str = strings.get(i).getName();
            for (int j = 0; j < PieChartView.history.size(); j++) {
                if (str.equals(PieChartView.history.get(j))) {
                    continue first;
                }
            }
            PieChartView.history.add(str);
        }
        PieChartView.total = 0;
        PieChartView.numbers = new int[PieChartView.history.size()];
        for (int i = 0; i < PieChartView.numbers.length; i++) {
            PieChartView.numbers[i] = mClientDatabaseHelper.getHistoryNameCount(PieChartView.history.get(i));
            PieChartView.total += PieChartView.numbers[i];
        }
    }

    class InitTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            mClientDatabaseHelper.clear();
            Log.d("mClient", mClientDatabaseHelper.getHistoryCount()+"");

                List<History.Child> histories = Client.instance().getHistory();
                if (histories == null) return null;
                Collections.reverse(histories);
                for (History.Child history : histories) {
                    int exist;
                    if (history.getOption().contains("取出")) exist = 0;
                    else if (history.getOption().contains("放入")) exist = 1;
                    else continue;
                    mClientDatabaseHelper.insertHistory(new Goods(ContentLab.instance(PieChartViewActivity.this).getContent(history.getMId()), exist, History.tStrFormat(history.getDate())));
                }
            initData();
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("ok")) {
                setContentView(R.layout.piechartview_layout);
                mPieChartView = (PieChartView) findViewById(R.id.pieChartView);
                mButtonPie = (Button) findViewById(R.id.pieChartViewButton);
                mButtonCircle = (Button) findViewById(R.id.circleChartViewButton);
                mButtonPie.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = PieChartView.selectedID;
                        if (i != -1) {
                            Intent intent = new Intent(PieChartViewActivity.this, SplineChartViewActivity.class);
                            intent.putExtra("title", mPieChartView.getChartData(i));
                            startActivity(intent);
                        }
                    }
                });
                mButtonCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PieChartViewActivity.this, CircleChartActivity.class);
                        startActivity(intent);
                    }
                });
            }

        }
    }

}
