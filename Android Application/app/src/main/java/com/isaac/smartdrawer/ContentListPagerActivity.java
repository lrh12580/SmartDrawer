package com.isaac.smartdrawer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class ContentListPagerActivity extends AppCompatActivity
        implements DbSyncTask.SyncCallback, ContentListPagerFragment.OnRefreshListener{
    private TabLayout mTab;
    private ViewPager mPager;
    private List<String> mCategories;

    public static Intent newIntent(Context context) {
        return new Intent(context, ContentListPagerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list_pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.content_list_pager_toolbar);
        setSupportActionBar(toolbar);
        setTitle("我的抽屉");

        Client.setConnection(getSharedPreferences(SettingsActivity.PREF_ADDR, MODE_PRIVATE).getString(SettingsActivity.PREF_ADDR, "192.168.31.76:21567"));

        mTab = (TabLayout) findViewById(R.id.content_list_pager_tab);
        mPager = (ViewPager) findViewById(R.id.content_list_pager_pager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.content_list_pager_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav = (NavigationView) findViewById(R.id.content_list_pager_nav);


        ((ImageView) nav.getHeaderView(0).findViewById(R.id.nav_user_image))
                .setImageDrawable(new BitmapDrawable(getResources(),
                ImageHandler.getImage(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "userImage.jpg").getAbsolutePath())));

        ((TextView) nav.getHeaderView(0).findViewById(R.id.nav_user_name))
                .setText(SettingsActivity.getUserNmae(this));

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_settings:
                        startActivity(SettingsActivity.newIntent(ContentListPagerActivity.this));
                        break;

                    case R.id.nav_category_manager:
                        startActivity(CategoryManagerActivity.newIntent(ContentListPagerActivity.this));
                        break;

                    case R.id.nav_alarm_manager:
                        startActivity(AlarmManagerActivity.newIntent(ContentListPagerActivity.this));
                        break;

                    case R.id.nav_history:
                        startActivity(HistoryActivity.newIntent(ContentListPagerActivity.this));
                        break;

                    case R.id.nav_chart:
                        startActivity(PieChartViewActivity.newIntent(ContentListPagerActivity.this));
                        break;
                }
                return true;
            }
        });

        mCategories = ContentLab.instance(this).getCategories();
        mPager.setAdapter(new ContentListsAdapter(getSupportFragmentManager()));
        mTab.setupWithViewPager(mPager);
        if (mCategories.size()>5) mTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        else mTab.setTabMode(TabLayout.MODE_FIXED);
        new DbSyncTask().execute(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_list_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.content_list_pager_menu_search:
                startActivity(ContentSearchActivity.newIntent(this));
                return true;
        }
        return false;
    }

    @Override
    public void callback() {
        mCategories.clear();
        mCategories.addAll(ContentLab.instance(this).getCategories());
        if (mCategories.size()>5) mTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        else mTab.setTabMode(TabLayout.MODE_FIXED);
        mPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        callback();
    }

    private class ContentListsAdapter extends FragmentStatePagerAdapter {
        public ContentListsAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position) {
            ContentListPagerFragment fragment = ContentListPagerFragment.newInstance(mCategories.get(position));
            fragment.setOnRefreshListener(ContentListPagerActivity.this);
            return fragment;
        }

        @Override
        public int getCount() {
            return mCategories.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCategories.get(position);
        }
    }
}
