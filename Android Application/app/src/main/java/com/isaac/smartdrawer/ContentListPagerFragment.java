package com.isaac.smartdrawer;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ContentListPagerFragment extends ContentListFragment implements DbSyncTask.SyncCallback {
    public static final String ARG_CATEGORY = "con.isaac.smartdrawer.category";

    private static final int REQUEST_CATEGORY = 0;
    private static final int REQUEST_ALARM = 1;

    private static final String DIALOG_CATEGORY = "DialogModify";
    private static final String DIALOG_ALARM = "DialogSetAlarm";

    private ActionMode mActionMode;
    private ActionMode.Callback mMultiSelectionMode;

    private SwipeRefreshLayout mRefreshLayout;
    private String mCategory;

    private OnRefreshListener mOnRefreshListener;

    public interface OnRefreshListener {
        void onRefresh();
    }


    public static ContentListPagerFragment newInstance(String category) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CATEGORY, category);
        ContentListPagerFragment fragment = new ContentListPagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void bindView() {
        super.bindView();

        mCategory = getArguments().getString(ARG_CATEGORY);
        mRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.content_list_swipe_refresh);
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new DbSyncTask().execute(ContentListPagerFragment.this);
            }
        });

        mMultiSelectionMode = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.multiple_select_mode, menu);
                ((ContentPagerAdapter) mAdapter).setSelectable(true);
                return true;
            }
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                switch (item.getItemId()) {
                    case R.id.multi_select_change_category:
                        DialogFragment multiModDialog = MultiModDialog.newInstance(mCategory);
                        multiModDialog.setTargetFragment(ContentListPagerFragment.this, REQUEST_CATEGORY);
                        multiModDialog.show(fm, DIALOG_CATEGORY);
                        break;
                    case R.id.multi_select_set_alarm:
                        DialogFragment multiAlarmDialog = MultiAlarmDialog.newInstance();
                        multiAlarmDialog.setTargetFragment(ContentListPagerFragment.this, REQUEST_ALARM);
                        multiAlarmDialog.show(getActivity().getSupportFragmentManager(), DIALOG_ALARM);
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ((ContentPagerAdapter) mAdapter).setSelectable(false);
                mActionMode = null;
            }
        };

        updateUI();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    @Override
    protected ContentListFragment.ContentAdapter newAdapter() {
        return new ContentPagerAdapter(new ArrayList<Content>());
    }

    private void updateUI() {
        ContentLab contentLab = ContentLab.instance(getActivity());
        List<Content> contents = contentLab.getContents(new String[]{mCategory});
        mAdapter.setContents(contents);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void callback() {
        updateUI();
        if (mOnRefreshListener == null) mOnRefreshListener = (OnRefreshListener) getActivity();
        mOnRefreshListener.onRefresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_CATEGORY:
                String category = data.getStringExtra(MultiModDialog.EXTRA_CATEGORY);
                ((ContentPagerAdapter) mAdapter).modifyCategories(category);
                break;
            case REQUEST_ALARM:
                Date date = (Date) data.getSerializableExtra(MultiAlarmDialog.EXTRA_ALARM_DATE);
                ((ContentPagerAdapter) mAdapter).setAlarms(date, data.getStringExtra(MultiAlarmDialog.EXTRA_ALARM_INFO));
                if (mActionMode != null) mActionMode.finish();
                break;
        }
        callback();
    }

    private class MultiModTask extends AsyncTask<List<Content>, Void, Boolean> {
        @Override
        public Boolean doInBackground(List<Content>... contents) {
            for (Content content : contents[0])
                Client.instance().update(content.getId(), ContentDbSchema.ContentsTable.Cols.CATEGORY, content.getCategory());
            return true;
        }
    }

    public static class MultiModDialog extends DialogFragment {
        public static final String EXTRA_CATEGORY = "com.isaac.smartdrawer.category";

        private AppCompatSpinner mCategoryModifier;
        private String mCategory;

        public static MultiModDialog newInstance(String category) {
            Bundle args = new Bundle();
            args.putString(ARG_CATEGORY, category);
            MultiModDialog fragment = new MultiModDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_category_category_modifier, null);

            mCategory = getArguments().getString(ARG_CATEGORY);

            List<String> categories = ContentLab.instance(getActivity()).getCategories();
            categories.remove(0);

            mCategoryModifier = (AppCompatSpinner) view.findViewById(R.id.dialog_category_category_modifier);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCategoryModifier.setAdapter(adapter);
            for (int i = 0;i<categories.size();i++)
                if (categories.get(i).equals(mCategory)) mCategoryModifier.setSelection(i);

            return new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle("请选择分类")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendResult(Activity.RESULT_OK, mCategoryModifier.getSelectedItem().toString());
                        }
                    })
                    .create();
        }

        private void sendResult(int resultCode, String category) {
            if (getTargetFragment() == null) return;
            Intent intent = new Intent();
            intent.putExtra(EXTRA_CATEGORY, category);
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        }
    }

    public static class MultiAlarmDialog extends DialogFragment {
        public static final String EXTRA_ALARM_DATE = "com.isaac.smartdrawer.alarm_date";
        public static final String EXTRA_ALARM_INFO = "com.isaac.smartdrawer.alarm_info";

        private View mView;
        private ViewPager mViewPager;
        private List<View> mViews;

        public static MultiAlarmDialog newInstance() {
            return new MultiAlarmDialog();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_set_alarm, null);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            mViewPager = (ViewPager) mView.findViewById(R.id.dialog_set_alarm_pager);
            final EditText et = (EditText) mView.findViewById(R.id.edit_text);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.last:
                            mViewPager.setCurrentItem(0);
                            break;
                        case R.id.next:
                            mViewPager.setCurrentItem(1);
                    }
                }
            };

            View dateView = inflater.inflate(R.layout.date_picker, null);
            final DatePicker dp = (DatePicker) dateView.findViewById(R.id.date_picker);
            (dateView.findViewById(R.id.next)).setOnClickListener(listener);
            dp.setMinDate(System.currentTimeMillis() - 1000);

            View timeView = inflater.inflate(R.layout.time_picker, null);
            final TimePicker tp = (TimePicker) timeView.findViewById(R.id.time_picker);
            timeView.findViewById(R.id.last).setOnClickListener(listener);
            tp.setIs24HourView(true);

            mViews = new ArrayList<>();
            mViews.add(dateView);
            mViews.add(timeView);

            final PagerAdapter adapter = new PagerAdapter() {
                @Override
                public int getCount() {
                    return mViews.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return view == object;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView(mViews.get(position));
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    container.addView(mViews.get(position));
                    return mViews.get(position);
                }
            };

            mViewPager.setAdapter(adapter);
            return new AlertDialog.Builder(getActivity())
                    .setView(mView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(),
                                    tp.getCurrentHour(), tp.getCurrentMinute());
                            if (calendar.getTime().before(new Date(System.currentTimeMillis()))){
                                sendResult(Activity.RESULT_CANCELED, null, null);
                                toast.setText("时间错误: "+calendar.getTime().toString());
                                toast.show();
                                return;
                            }

                            sendResult(Activity.RESULT_OK, calendar.getTime(), et.getText().toString());
                            toast.setText("已设置于: " + calendar.getTime().toString());
                            toast.show();
                        }
                    })
                    .create();
        }

        private void sendResult(int resultCode, Date date, String info) {
            if (getTargetFragment() == null) return;
            Intent intent = new Intent();
            intent.putExtra(EXTRA_ALARM_DATE, date);
            intent.putExtra(EXTRA_ALARM_INFO, info);
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        }
    }

    private class ContentPagerAdapter extends ContentListFragment.ContentAdapter{
        private MySelector mSelector;

        public ContentPagerAdapter(List<Content> contents) {
            super(contents);
            mSelector = new MySelector(this);
        }

        @Override
        protected ContentHolder newHolder(View view) {
            return new ContentPagerHolder(view, mSelector);
        }


        public void setSelectable(boolean bool) {
            mSelector.setSelectable(bool);
        }

        public void modifyCategories(String category) {
            if (category.equals(mCategory)) return;

            List<Content> contents = new ArrayList<>();
            List<Integer> integers = mSelector.getSelectedPositions();
            for (int i = 0;i<integers.size();i++) {
                int position = integers.get(i) - i;
                Content content = mContents.get(position);
                mContents.remove(position);
                content.setCategory(category);
                ContentLab.instance(getActivity()).updateContent(content);
                contents.add(content);
            }
            if (mActionMode != null) mActionMode.finish();
            notifyDataSetChanged();
            new MultiModTask().execute(contents);
        }

        public void setAlarms(Date date, String info) {
            try {
                JSONArray array = new JSONArray();
                for (int position : mSelector.getSelectedPositions()) array.put(mContents.get(position).getId());
                JSONObject json = new JSONObject();
                json.put("ids", array);
                Alarms.setAlarm(getActivity(), History.tStrFormat(date), json.toString(), info);
            } catch (JSONException je) {
                Toast.makeText(getActivity(), "未设置闹钟:JSONException", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class ContentPagerHolder extends ContentListFragment.ContentHolder implements View.OnLongClickListener{
        private CheckBox mCheckbox;
        private MySelector mSelector;
        private View mChecker;

        public ContentPagerHolder(View itemView, MySelector selector) {
            super(itemView);

            mCheckbox = (CheckBox) itemView.findViewById(R.id.content_list_item_selector);
            mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    onSelectedChanged(b);
                }
            });
            mChecker = itemView.findViewById(R.id.content_list_item_checker);
            mSelector = selector;

        }

        @Override
        protected void setListeners() {
            super.setListeners();
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void bindContent(Content content) {
            super.bindContent(content);

            setSelectable(mSelector.isSelectable());
            setSelected(mSelector.isSelected(getAdapterPosition()));
        }

        private void onSelectedChanged(boolean bool) {
            mSelector.setSelected(getAdapterPosition(), bool);
            itemView.setActivated(bool);
            mChecker.setActivated(bool);
        }

        public void setSelectable(boolean bool) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                mCheckbox.setVisibility(bool?View.VISIBLE:View.INVISIBLE);
        }

        public void setSelected(boolean bool) {
            mCheckbox.setChecked(bool);
        }

        @Override
        public void onClick(View view) {
            if (mSelector.isSelectable()) {
                setSelected(!mSelector.isSelected(getAdapterPosition()));
            } else {
                super.onClick(view);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mSelector.isSelectable()) return true;
//            if (mActionMode != null) return true;

            mCheckbox.setChecked(true);

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            mActionMode = activity.startSupportActionMode(mMultiSelectionMode);
            return true;
        }
    }

    private class MySelector {
        private SparseBooleanArray mSelector = new SparseBooleanArray();
        private boolean mIsSelectable = false;
        private ContentPagerAdapter mAdapter;

        public MySelector(ContentPagerAdapter adapter) {
            mAdapter = adapter;
        }

        public void setSelected(int position, boolean isChecked) {
            mSelector.put(position, isChecked);
        }

        public boolean isSelected(int position) {
            return mSelector.get(position);
        }

        public boolean isSelectable() {
            return mIsSelectable;
        }

        public void setSelectable(boolean isSelectable) {
            mIsSelectable = isSelectable;
            refreshHolders();
        }

        public List<Integer> getSelectedPositions() {
            List<Integer> positions = new ArrayList<>();
            for (int i = 0; i<mSelector.size(); i++)
                if (mSelector.valueAt(i)) positions.add(mSelector.keyAt(i));
            return positions;
        }

        private void refreshHolders() {
            for (int i = 0;i<mAdapter.getItemCount();i++) {
                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    ((ContentPagerHolder) holder).setSelectable(mIsSelectable);
                    if (!mIsSelectable) ((ContentPagerHolder) holder).setSelected(false);
                }
            }
        }
    }
}
