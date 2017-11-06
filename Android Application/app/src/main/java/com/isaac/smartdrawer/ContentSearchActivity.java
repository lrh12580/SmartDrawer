package com.isaac.smartdrawer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class ContentSearchActivity extends AppCompatActivity {

    private ContentListFragment mFragment;
    private SearchView mSearchView;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, ContentSearchActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.content_search_toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.content_search_container);

        if (fragment == null) {
            fragment = ContentListFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.content_search_container, fragment)
                    .commit();
        }

        mFragment = (ContentListFragment) fragment;

        mSearchView = (SearchView) findViewById(R.id.content_search_view);
        mSearchView.setIconified(false);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint("请输入物品名称");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                List<Content> contents = new ArrayList<>();
                if (!newText.equals("")) contents = ContentLab.instance(ContentSearchActivity.this).getContentsByName(newText);
                mFragment.setContents(contents);
                return true;
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

    }
}
