package com.isaac.smartdrawer;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ContentFragment extends Fragment {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);

    public interface ModifierCallback {
        void callback(Content content);

        void updateUI();
    }

    private static final String ARG_CONTENT = "content";

    public static final int REQUEST_MODIFY = 0;

    private Content mContent;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerAdapter mAdapter;
    private List<History> mParents;

    public static ContentFragment newInstance(Content content) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTENT, content);
        ContentFragment fragment = new ContentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContent = (Content) getArguments().getSerializable(ARG_CONTENT);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() == null) return;
            ((ModifierCallback) getActivity()).updateUI();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.history_swipe_refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetHistoryTask().execute();
            }
        });

        mParents = new ArrayList<>();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_content_history_list);
        mAdapter = new RecyclerAdapter(getActivity(), mParents);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRefreshLayout.setRefreshing(true);
        new GetHistoryTask().execute();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_MODIFY) {
            String name = intent.getStringExtra(ContentPagerActivity.ModifyFragment.EXTRA_NAME);
            String category = intent.getStringExtra(ContentPagerActivity.ModifyFragment.EXTRA_CATEGORY);
            mContent.setName(name);
            mContent.setCategory(category);

            ((ModifierCallback) getActivity()).callback(mContent);
            new GetHistoryTask().execute();
        }
    }

    public Content getContent() {
        return mContent;
    }

    private class GetHistoryTask extends AsyncTask<Void, Void, List<History>> {
        @Override
        protected List<History> doInBackground(Void... params) {

            List<History> parents = new ArrayList<>();
            List<History.Child> children = mContent == null?Client.instance().getHistory():
                    Client.instance().getHistory(mContent.getId());
            if (children == null) return null;

            for (int i = 0; i < children.size(); i++) {
                List<History.Child> childrenToBind = new ArrayList<>();
                if (children.get(i) == null) continue;
                childrenToBind.add(children.get(i));
                Calendar c = getPureDate(children.get(i).getDate());
                for (i++; i < children.size(); i++) {
                    History.Child child = children.get(i);
                    if (c.equals(getPureDate((child.getDate())))) childrenToBind.add(child);
                    else {
                        i--;
                        break;
                    }
                }
                parents.add(new History.Parent(c.getTime(), childrenToBind));
            }

            return parents;
        }

        @Override
        protected void onPostExecute(List<History> parents) {
            if (parents == null) return;

            mParents.clear();
            mParents.addAll(parents);
            mAdapter.notifyDataSetChanged();
            mRefreshLayout.setRefreshing(false);
        }

        private Calendar getPureDate(Date date) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            Calendar calendar = Calendar.getInstance();
            calendar.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            return calendar;
        }
    }

    private abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class ParentViewHolder extends BaseViewHolder {
        private TextView mParentDay;
        private TextView mParentDate;
        private TextView mParentTitle;

        public ParentViewHolder(View itemView) {
            super(itemView);
            mParentDay = (TextView) itemView.findViewById(R.id.history_parent_day);
            mParentDate = (TextView) itemView.findViewById(R.id.history_parent_date);
            mParentTitle = (TextView) itemView.findViewById(R.id.history_parent_title);
        }

        public void bind(Context context, final History.Parent parent, final ParentClickListener listener) {
            Calendar c = Calendar.getInstance();
            c.setTime(parent.getDate());
            mParentDay.setText(String.format(context.getResources().getString(R.string.history_parent_day), c.get(Calendar.DAY_OF_MONTH)));
            mParentDate.setText(String.format(context.getResources().getString(R.string.history_parent_date), c.get(Calendar.MONTH), c.get(Calendar.YEAR)));
            mParentTitle.setText(String.format(context.getResources().getString(R.string.history_parent_title), parent.getChildItemList().size()));

            itemView.findViewById(R.id.history_parent_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (parent.isExpanded()) {
                        parent.setExpanded(false);
                        listener.onHideChildren(parent);
                    } else {
                        parent.setExpanded(true);
                        listener.onExpandChildren(parent);
                    }
                }
            });
        }
    }

    private class ChildViewHolder extends BaseViewHolder {
        private TextView mChildTime;
        private TextView mChildOption;
        private TextView mChildName;

        public ChildViewHolder(View itemView) {
            super(itemView);

            mChildTime = (TextView) itemView.findViewById(R.id.history_child_time);
            mChildOption = (TextView) itemView.findViewById(R.id.history_child_option);
            mChildName = (TextView) itemView.findViewById(R.id.history_child_name);
        }

        public void bind(Context context, History.Child child) {
            mChildTime.setText(sdf.format(child.getDate()));
            mChildOption.setText(child.getOption());
            mChildName.setText(ContentLab.instance(context).getContent(child.getMId()).getName());
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
        private Context mContext;
        private List<History> mHistories;

        public RecyclerAdapter(Context context, List<History> historyList) {
            mContext = context;
            mHistories = historyList;
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            switch (viewType) {
                case History.TYPE_PARENT:
                    return new ParentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.holder_history_parent, viewGroup, false));
                case History.TYPE_CHILD:
                    return new ChildViewHolder(LayoutInflater.from(mContext).inflate(R.layout.holder_history_child, viewGroup, false));
                default:
                    return new ParentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.holder_history_parent, viewGroup, false));
            }

        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case History.TYPE_PARENT:
                    ParentViewHolder parentViewHolder = (ParentViewHolder) holder;
                    parentViewHolder.bind(mContext, (History.Parent) mHistories.get(position), mListener);
                    break;
                case History.TYPE_CHILD:
                    ChildViewHolder childViewHolder = (ChildViewHolder) holder;
                    childViewHolder.bind(mContext, (History.Child) mHistories.get(position));
                    break;
            }
        }

        private ParentClickListener mListener = new ParentClickListener() {
            @Override
            public void onExpandChildren(History.Parent parent) {
                int position = getCurrentPosition(parent.getId());
                addAll(position, parent.getChildItemList().toArray(new History[]{}));
            }

            @Override
            public void onHideChildren(History.Parent parent) {
                int position = getCurrentPosition(parent.getId());
                List<History.Child> children = parent.getChildItemList();
                if (children.size() == 0) return;
                removeAll(position + 1, children.size());
            }
        };

        @Override
        public int getItemViewType(int position) {
            return mHistories.get(position).getType();
        }

        @Override
        public int getItemCount() {
            return mHistories.size();
        }

        private int getCurrentPosition(Integer id) {
            for (int i = 0; i < mHistories.size(); i++) {
                Integer integer = mHistories.get(i).getId();
                if (integer != null && integer.equals(id))
                    return i;
            }
            return -1;
        }

        private void removeAll(int position, int count) {
            for (int i = 0; i < count; i++)
                mHistories.remove(position);
            notifyItemRangeRemoved(position, count);
        }

        private void addAll(int position, History[] historyList) {
            mHistories.addAll(position + 1, Arrays.asList(historyList));
            notifyItemRangeInserted(position + 1, historyList.length);
        }

    }

    private interface ParentClickListener {
        void onExpandChildren(History.Parent parent);

        void onHideChildren(History.Parent parent);
    }
}