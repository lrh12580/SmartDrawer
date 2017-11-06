package com.isaac.smartdrawer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlarmManagerActivity extends AppCompatActivity {
    private ClientDatabaseHelper mDatabaseHelper;
    private List<Alarms> mAlarmsList;
    private AlarmAdapter mAdapter;

    @BindView(R.id.activity_alarm_manager_toolbar) Toolbar mToolbar;
    @BindView(R.id.activity_alarm_manager_recycler_view) RecyclerView mRecyclerView;

    public static Intent newIntent(Context context) {
        return new Intent(context, AlarmManagerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_management);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle("闹钟管理");

        mDatabaseHelper = ClientDatabaseHelper.getInstance(this);
        mAlarmsList = mDatabaseHelper.getAlarms();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AlarmAdapter(mAlarmsList);
        mRecyclerView.setAdapter(mAdapter);

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }


    class AlarmHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.alarm_name) TextView mName;
        @BindView(R.id.alarm_info) TextView mInfo;
        @BindView(R.id.alarm_time) TextView mTime;
        @BindView(R.id.alarm_date) TextView mDate;

        public AlarmHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindAlarms(Alarms alarms) {
            String str = "";
            ContentLab contentLab = ContentLab.instance(AlarmManagerActivity.this);
            switch (alarms.getIds().size()) {
                case 0:
                    break;
                case 1:
                    str = contentLab.getContent(alarms.getIds().get(0)).getName();
                    break;
                default:
                    str = contentLab.getContent(alarms.getIds().get(0)).getName();
                    str = str + "," + contentLab.getContent(alarms.getIds().get(1)).getName();
                    break;
            }
            mName.setText(str);
            mTime.setText(DateFactory.dateToTime(DateFactory.toDate(alarms.getDate())));
            String dayofweek = new String[]{"日","一","二","三","四","五","六"}[alarms.getDate_dayofWeek()-1];
            mDate.setText(alarms.getDate_month()+"月"+alarms.getDate_day() +"日"+" 周" + dayofweek);
            mInfo.setText(alarms.getInfo());
        }

        @OnClick(R.id.imageButton)
        void onDeleteButtonClicked() {
            int pos = getAdapterPosition();

            Intent intent = new Intent(AlarmManagerActivity.this, AlarmService.class);
            PendingIntent pi = PendingIntent.getActivity(AlarmManagerActivity.this, mAlarmsList.get(pos).getAlarm_id(), intent, 0);
            AlarmManager am = (AlarmManager) AlarmManagerActivity.this.getSystemService(Activity.ALARM_SERVICE);
            am.cancel(pi);

            mDatabaseHelper.deleteAlarms(mAlarmsList.get(pos).getAlarm_id()+"");
            mAlarmsList.remove(pos);
            mAdapter.notifyItemRemoved(pos);
            mAdapter.notifyItemRangeChanged(pos, 1);
        }
    }

    private class AlarmAdapter extends RecyclerView.Adapter<AlarmHolder> {
        private List<Alarms> mAlarms;

        public AlarmAdapter(List<Alarms> alarms) {
            mAlarms = alarms;
        }

        @Override
        public AlarmHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(AlarmManagerActivity.this);
            View view = layoutInflater.inflate(R.layout.holder_alarm, parent, false);
            return new AlarmHolder(view);
        }

        @Override
        public void onBindViewHolder(AlarmHolder holder, int position) {
            holder.bindAlarms(mAlarms.get(position));
        }

        @Override
        public int getItemCount() {
            return mAlarms.size();
        }
    }

}

