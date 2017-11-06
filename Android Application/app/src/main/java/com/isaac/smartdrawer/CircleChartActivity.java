package com.isaac.smartdrawer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class CircleChartActivity extends Activity {

    private CircleChartView mCircleChartView1, mCircleChartView2;
    private TimerTask mTimerTask = null;
    private Timer mTimer;
    public static final int UPDATECIRCLEVIEW1 = 0;
    public static final int UPDATECIRCLEVIEW2 = 1;
    public static final int SHOWTOAST = 2;
    static int weekscore = 0, monthscore = 0;
    int temp1 = 0, temp2 = 0;
    public static final String TAG = "CircleChartActivity";
    private ClientDatabaseHelper mClientDatabaseHelper;
    private List<Goods> goodsList = new ArrayList<>();
    private int weeks;
    private FileService mFileService;
    String str = "";
    private TextView week, month, test1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置没标题
        setContentView(R.layout.circlechartview_layout);
        week = (TextView) findViewById(R.id.week_score);
        month = (TextView) findViewById(R.id.month_score);
        test1 = (TextView) findViewById(R.id.test1);
        mClientDatabaseHelper = ClientDatabaseHelper.getInstance(this);
        mFileService = new FileService(CircleChartActivity.this);
        ProgressAsyncTask asyncTask = new ProgressAsyncTask();
        asyncTask.execute();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATECIRCLEVIEW1:
                    mCircleChartView1.setPercentage(76);
                    mCircleChartView1.chartRender();
                    mCircleChartView1.invalidate();
                    //week.setText(msg.obj.toString()+"分");
                    break;
                case UPDATECIRCLEVIEW2:
                    mCircleChartView2.setPercentage(85);
                    mCircleChartView2.chartRender();
                    mCircleChartView2.invalidate();
                    //month.setText(msg.obj.toString()+"分");
                    break;
                case SHOWTOAST:
                    Toast.makeText(CircleChartActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private void init() {
        mCircleChartView1 = (CircleChartView) findViewById(R.id.circleChartView1);
        mCircleChartView2 = (CircleChartView) findViewById(R.id.circleChartView2);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (temp1 < weekscore) {
                    temp1++;
                    Message message = new Message();
                    message.what = UPDATECIRCLEVIEW1;
                    message.obj = temp1;
                    handler.sendMessage(message);
                }
                if (temp2 < monthscore) {
                    temp2++;
                    Message message = new Message();
                    message.what = UPDATECIRCLEVIEW2;
                    message.obj = temp2;
                    handler.sendMessage(message);
                }
                if (temp1 > weekscore && temp2 > monthscore) {
                    temp1 = 0;
                    temp2 = 0;
                    mTimer.cancel();
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 25);
    }

    private String input() {
        String input = "";
        for (int i = 0; i < goodsList.size(); i++) {
            String inOut;
            int day, hour, minute;
            boolean isAdd = true;
            day = calculateDaysFirstDay(goodsList.get(i).getDate());
            hour = goodsList.get(i).getDate_hour();
            minute = goodsList.get(i).getDate_minute();
            if (goodsList.get(i).getExist() == 0) {
                inOut = "out";
            } else {
                inOut = "in";
            }
            String name = goodsList.get(i).getName();
            Log.d("name", name + i);

            if (day <= 28) {
                if (day > 7 && calculateDaysFirstDay(goodsList.get(i - 1).getDate()) <= 7) {
                    input += "end 1\n";
                    if (weeks == 1) {
                        return input;
                    }
                }
                if (day > 14 && calculateDaysFirstDay(goodsList.get(i - 1).getDate()) <= 14) {
                    input += "end 2\n";
                    if (weeks == 2) {
                        return input;
                    }
                }
                if (day > 21 && calculateDaysFirstDay(goodsList.get(i - 1).getDate()) <= 21) {
                    input += "end 3\n";
                    if (weeks == 3) {
                        return input;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (name.equals(goodsList.get(j).getName())) {
                        isAdd = false;
                        break;
                    }
                }
                Log.d("idadd", isAdd + "");
                if (isAdd) {
                    input += "add " + name + "\n";
                }
                input += inOut + " " + name + " " + day + " " + hour + " " + minute + "\n";
            }
            if (day > 28) {
                input += "end 4\n";
                return input;
            }
        }
        return input;
    }

    private void setWeeks() {
        String date = goodsList.get(0).getDate();
        int days = calculateDaysNow(date);
        if (days < 7) {
            weeks = 0;
        }
        if (days >= 7 && days < 14) {
            weeks = 1;
        }
        if (days >= 14 && days < 21) {
            weeks = 2;
        }
        if (days >= 21 && days < 28) {
            weeks = 3;
        }
        if (days == 28) {
            weeks = 4;
        }
        if (days > 28) {
            weeks = 5;
        }
    }

    private int calculateDaysNow(String str) {
        int day = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date now = null;
        Date date = null;
        try {
            date = simpleDateFormat.parse(str);
            Calendar calendar = Calendar.getInstance();
            String dates = calendar.get(Calendar.YEAR) + "-"
                    + (int) (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH)
                    + " " + "00:00:00";
            now = simpleDateFormat.parse(dates);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long l = now.getTime() - date.getTime();
        if (l >= 0) {
            day = (int) (l / (24 * 60 * 60 * 1000)) + 1;
        }
        return day;
    }

    private int calculateDaysFirstDay(String str) {
        int day = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date first = null;
        Date date = null;
        try {
            String firstdate = goodsList.get(0).getDate_year() + "-"
                    + goodsList.get(0).getDate_month() + "-" +
                    goodsList.get(0).getDate_day() + " " + "00:00:00";
            first = simpleDateFormat.parse(firstdate);
            date = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long l = date.getTime() - first.getTime();
        if (l > 0) {
            day = (int) (l / (24 * 60 * 60 * 1000)) + 1;
        }
        return day;
    }

    class ProgressAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            if (mClientDatabaseHelper.getHistoryCount() > 0) {
                int count = mClientDatabaseHelper.getHistoryCount();
                for (int i = 0; i < count; i++) {
                    goodsList.add(mClientDatabaseHelper.getHistories().get(i));
                    Log.d("goodslist", goodsList.get(i).getName());
                }

            }
            if (goodsList.size() > 0) {
                setWeeks();
            }
            if (weeks >= 1) {
                str = input();
                Score score = new Score(CircleChartActivity.this, str);
                try {
                    score.exe();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Message message = new Message();
                message.what = SHOWTOAST;
                message.obj = "您使用天数少于7天，无法显示得分";
                handler.sendMessage(message);
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("ok")) {
                String str1 = null;
                try {
                    str1 = mFileService.FileRead("output");
                    test1.setText(str1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //str1 = "{\"score\":{\"第1周\":{\"身份证\":{\"属性\":\"拿走\",\"属性2\":\"拿走\"},\"身份证1\":{\"属性\":\"拿走\",\"属性2\":\"拿走\"},\"分数\":20},\"第二周\":{\"身份证\":{\"属性\":\"拿走\",\"属性2\":\"拿走\"},\"身份证1\":{\"属性\":\"拿走\",\"属性2\":\"拿走\"},\"得分\":20},\"本月得分\":75}}";
                try {
                    JSONObject jsonObject = new JSONObject(str1).optJSONObject("score");
                    JSONObject jsonObject1 = jsonObject.optJSONObject("第1周");
                    weekscore = jsonObject1.optInt("分数");
                    monthscore = jsonObject.optInt("该月的得分为");
                    //score = Integer.parseInt(str1.substring(str1.length()-3,str1.length()-1));
                    Log.d(TAG, monthscore + " " + weekscore);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                init();
            }
        }
    }
}
