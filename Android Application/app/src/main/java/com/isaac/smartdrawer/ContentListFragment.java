package com.isaac.smartdrawer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ContentListFragment extends Fragment {

    private static final String ARG_CANNOTREFRESH = "cannotrefresh";
    protected boolean isAttached;
    protected View mView;

    protected RecyclerView mRecyclerView;
    protected ContentAdapter mAdapter;

    private ThumbnailLoader<ContentHolder> mThumbnailLoader;

    public static ContentListFragment newInstance() {
        ContentListFragment fragment = new ContentListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_CANNOTREFRESH, true);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler responseHandler = new Handler();
        mThumbnailLoader = new ThumbnailLoader<>(responseHandler);
        mThumbnailLoader.setThumbnailLoadListener(new ThumbnailLoader.ThumbnailLoadListener<ContentHolder>() {
            @Override
            public void onThumbnailLoaded(ContentHolder holder, Bitmap thumbnail) {
                if (!isAttached || thumbnail == null) return;
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                holder.bindDrawable(drawable);
            }
        });
        mThumbnailLoader.start();
        mThumbnailLoader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_content_list, container, false);

        if (getArguments().getBoolean(ARG_CANNOTREFRESH))
            mView.findViewById(R.id.content_list_swipe_refresh).setEnabled(false);

        bindView();

        return mView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailLoader.clearQueue();
    }

    protected void bindView() {
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.fragment_content_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = newAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    protected ContentAdapter newAdapter() {
        return new ContentAdapter(new ArrayList<Content>());
    }

    public void setContents(List<Content> contents) {
        if (mAdapter != null) mAdapter.setContents(contents);
    }


    public class ContentHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ColorMatrixColorFilter mGrayFilter;

        protected ImageView mCover;
        protected TextView mName;
        protected TextView mCategory;
        protected Content mContent;
        protected SwipeRefreshLayout mRefreshLayout;

        public ContentHolder(View itemView) {
            super(itemView);

            mCover = (ImageView) itemView.findViewById(R.id.content_list_item_cover);
            mName = (TextView) itemView.findViewById(R.id.content_list_item_name);
            mCategory = (TextView) itemView.findViewById(R.id.content_list_item_category);

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            mGrayFilter = new ColorMatrixColorFilter(matrix);

            setListeners();
        }

        public void bindContent(Content content) {
            if (content.getName().equals("未命名"))
                ((TextView) itemView.findViewById(R.id.content_list_item_id)).setText(content.getId());
            mContent = content;
            mName.setText(mContent.getName());
            mCategory.setText(mContent.getCategory());
        }

        public void bindDrawable(Drawable drawable) {
            mCover.setImageDrawable(drawable);
            if (mContent.isExist()) mCover.setColorFilter(null);
            else mCover.setColorFilter(mGrayFilter);
        }

        protected void setListeners() {
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = ContentPagerActivity.newIntent(getActivity(), mContent.getId(), mContent.getCategory());
            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), mCover, getString(R.string.transition_cover))
                    .toBundle();
            ActivityCompat.startActivity(getActivity(), intent, bundle);
        }
    }

    public class ContentAdapter extends RecyclerView.Adapter<ContentHolder> {
        protected List<Content> mContents;

        public ContentAdapter(List<Content> contents) {
            mContents = contents;
        }

        @Override
        public ContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.holder_content, parent, false);
            return newHolder(view);
        }

        protected ContentHolder newHolder(View view) {
            return new ContentHolder(view);
        }


        @Override
        public void onBindViewHolder(ContentHolder holder, int position) {
            Content content = mContents.get(position);
            holder.bindContent(content);
            mThumbnailLoader.queueThumbnail(holder, content.getCoverPath(getActivity()));
        }

        @Override
        public int getItemCount() {
            return mContents.size();
        }


        public void setContents(List<Content> contents) {
            if (mContents == null) return;
            mContents.clear();
            mContents.addAll(contents);
            notifyDataSetChanged();
        }
    }
}

