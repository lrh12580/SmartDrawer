package com.isaac.smartdrawer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class HistoryActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, HistoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_history_toolbar);
        setTitle("全部历史");
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.activity_history_container);
        if (fragment == null) {
            fragment = ContentFragment.newInstance(null);
            fm.beginTransaction()
                    .add(R.id.activity_history_container, fragment)
                    .commit();
        }

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

    }
}
