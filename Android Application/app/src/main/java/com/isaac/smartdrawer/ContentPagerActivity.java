package com.isaac.smartdrawer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ContentPagerActivity extends AppCompatActivity implements ContentFragment.ModifierCallback{
    private static final int REQUEST_COVER_CAMERA = 0;
    private static final int REQUEST_COVER_ALBUM = 1;

    private static final String EXTRA_CONTENT_ID = "com.isaac.smartdrawer.content_id";
    private static final String EXTRA_CATEGORY = "com.isaac.smartdrawer.category";
    private static final String DIALOG_MODIFY = "DialogModify";

    private ViewPager mViewPager;
    private ContentPagerAdapter mAdapter;
    private ImageView mIvTarget;
    private List<Content> mContents;
    private CollapsingToolbarLayout mToolbarLayout;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_content);

        mViewPager = (ViewPager) findViewById(R.id.activity_content_content_pager);
        ImageView mIvOutgoing = (ImageView) findViewById(R.id.toolbar_iv_outgoing);
        mIvTarget = (ImageView) findViewById(R.id.toolbar_iv_target);
        mToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.activity_content_toolbar_layout);

        mContents = ContentLab.instance(this).getContents(new String[]{getIntent().getStringExtra(EXTRA_CATEGORY)});

        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new ContentPagerAdapter(fm);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(PagerChangeListener.newInstance(mAdapter, mIvTarget, mIvOutgoing));

        String contentId = getIntent().getStringExtra(EXTRA_CONTENT_ID);
        Toolbar toolbar = (Toolbar)  findViewById(R.id.activity_content_toolbar);
        setSupportActionBar(toolbar);
        for (int i = 0; i < mContents.size(); i++) {
            if (mContents.get(i).getId().equals(contentId)) {
                mViewPager.setCurrentItem(i);
                mIvTarget.setImageDrawable(mAdapter.getCoverBitmap(i));
                mToolbarLayout.setTitle(mContents.get(i).getName());
                break;
            }
        }

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }

    public static Intent newIntent(Context packageContext, String contentId, String category) {
        Intent intent = new Intent(packageContext, ContentPagerActivity.class);
        intent.putExtra(EXTRA_CONTENT_ID, contentId);
        intent.putExtra(EXTRA_CATEGORY, category);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.content_menu_modify:
                ContentFragment fragment = mAdapter.getFragment();
                Content content = fragment.getContent();
                FragmentManager fm = getSupportFragmentManager();
                ModifyFragment dialog = ModifyFragment.newInstance(content);
                dialog.setTargetFragment(fragment, ContentFragment.REQUEST_MODIFY);
                dialog.show(fm, DIALOG_MODIFY);
                break;

            case R.id.content_menu_photo:
                final Dialog bottomsheet = new BottomSheetDialog(this);
                bottomsheet.setContentView(R.layout.dialog_bottom_sheet_photo);
                View.OnClickListener bottomsheetOnClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            switch (view.getId()) {
                                case R.id.choose_from_camera:
                                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    Uri uri = Uri.fromFile(new File(mAdapter.getFragment().getContent().getCoverPath(ContentPagerActivity.this)));
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                    startActivityForResult(cameraIntent, REQUEST_COVER_CAMERA);
                                    break;
                                case R.id.choose_from_album:
                                    PackageManager pm = getPackageManager();
                                    if (PackageManager.PERMISSION_GRANTED !=
                                            pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE",
                                                    "com.isaac.smartdrawer")) {
                                        View parent = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
                                        Snackbar.make(parent, "请允许应用访问存储！", Snackbar.LENGTH_LONG)
                                                .setAction("去设置", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Intent intent = new Intent();
                                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                                                        startActivity(intent);
                                                    }
                                                })
                                                .show();
                                        break;
                                    }

                                    Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                    pickIntent.setType("image/*");
                                    startActivityForResult(pickIntent, REQUEST_COVER_ALBUM);
                                    break;
                            }
                        } finally {
                            bottomsheet.dismiss();
                        }


                    }
                };
                bottomsheet.findViewById(R.id.choose_from_camera).setOnClickListener(bottomsheetOnClick);
                bottomsheet.findViewById(R.id.choose_from_album).setOnClickListener(bottomsheetOnClick);
                bottomsheet.show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void callback(Content content) {
        updateUI();
        new ModifyTask().execute(ContentLab.instance(this).getContent(content.getId()), content);
        ContentLab.instance(this).updateContent(content);
    }

    @Override
    public void updateUI() {
        Content content = ((ContentPagerAdapter) mViewPager.getAdapter()).getFragment().getContent();
        mToolbarLayout.setTitle(content.getName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_COVER_ALBUM:
                try {
                    ImageHandler.fileCopy(ImageHandler.getUriPath(this, data.getData()),
                            mAdapter.getFragment().getContent().getCoverPath(this));
                } catch (IOException ioe) {
                    Snackbar.make((findViewById(android.R.id.content)),
                            "无法完成操作", Snackbar.LENGTH_LONG)
                            .show();
                }
                // 此处不用break
            case REQUEST_COVER_CAMERA:
                String path = mAdapter.getFragment().getContent().getCoverPath(this);
                if (!new File(path).exists()) return;
                mIvTarget.setImageDrawable(new BitmapDrawable(getResources(), ImageHandler.getImage(path)));
                break;
        }
    }


    private class ModifyTask extends AsyncTask<Content, Void, Boolean> {
        @Override
        public Boolean doInBackground(Content... contents) {
            Content before = contents[0];
            Content after = contents[1];
            if (!before.getName().equals(after.getName())) Client.instance().update(after.getId(), ContentDbSchema.ContentsTable.Cols.NAME, after.getName());
            if (!before.getCategory().equals(after.getCategory())) Client.instance().update(after.getId(), ContentDbSchema.ContentsTable.Cols.CATEGORY, after.getCategory());

            return true;
        }
    }

    public static class ModifyFragment extends DialogFragment {
        private static final String ARG_CONTENT = "content";

        public static final String EXTRA_NAME = "com.isaac.smartdrawer.modifier.name";
        public static final String EXTRA_CATEGORY = "com.isaac.smartdrawer.modifier.category";

        private Content mContent;
        private TextInputEditText mNameModifier;
//        private TextInputEditText mCategoryModifier;
        private AppCompatSpinner mCategoryModifier;

        public static ModifyFragment newInstance(Content content) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_CONTENT, content);

            ModifyFragment fragment = new ModifyFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_content_modifier, null);
            mContent = (Content) getArguments().getSerializable(ARG_CONTENT);

            mNameModifier = (TextInputEditText) v.findViewById(R.id.modifier_name);
            mNameModifier.setText(mContent.getName());
            mNameModifier.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() == 0) mNameModifier.setError(getString(R.string.error_blank));
                }
            });

            List<String> categories = ContentLab.instance(getActivity()).getCategories();
            categories.remove(0);

            mCategoryModifier = (AppCompatSpinner) v.findViewById(R.id.modifier_category);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCategoryModifier.setAdapter(adapter);
            for (int i = 0;i<categories.size();i++)
                if (categories.get(i).equals(mContent.getCategory())) mCategoryModifier.setSelection(i);

            return new AlertDialog.Builder(getActivity())
                    .setView(v)
                    .setTitle(R.string.alart_dialog_modify)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newName = mNameModifier.getText().toString();
                            if (newName.equals("")) return;
                            sendResult(Activity.RESULT_OK, newName, mCategoryModifier.getSelectedItem().toString());
                        }
                    })
                    .create();
        }

        private void sendResult(int resultCode, String name, String category) {
            if (getTargetFragment() == null) return;
            Intent intent = new Intent();
            intent.putExtra(EXTRA_NAME, name);
            intent.putExtra(EXTRA_CATEGORY, category);
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        }
    }

    private class ContentPagerAdapter extends FragmentStatePagerAdapter{
        private HashMap<Integer, ContentFragment> mFragmentMap;


        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = ContentFragment.newInstance(mContents.get(position));
            if (mFragmentMap == null) mFragmentMap = new HashMap<>();
            mFragmentMap.put(position, (ContentFragment) fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return mContents.size();
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            super.destroyItem(container, position, object);
            mFragmentMap.remove(position);
        }

//        @DrawableRes
//        public int getDrawable(int position) {
//            return R.drawable.drawable_1;
//        }

        public Drawable getCoverBitmap(int position) {
            String path = mContents.get(position).getCoverPath(ContentPagerActivity.this);
            if (!new File(path).exists()) return ContextCompat.getDrawable(ContentPagerActivity.this, R.drawable.no_cover);
            return new BitmapDrawable(getResources(), ImageHandler.getImage(path));
        }

        public ContentFragment getFragment() {
            return mFragmentMap.get(mViewPager.getCurrentItem());
        }

    }

    private static class PagerChangeListener implements ViewPager.OnPageChangeListener {
        private ImageAnimator mImageAnimator;

        private int mCurrentPosition;

        private int mFinalPosition;

        private boolean mIsScrolling = false;

        public PagerChangeListener(ImageAnimator imageAnimator) {
            mImageAnimator = imageAnimator;
        }

        public static PagerChangeListener newInstance(ContentPagerAdapter adapter, ImageView originImage, ImageView outgoingImage) {
            ImageAnimator imageAnimator = new ImageAnimator(adapter, originImage, outgoingImage);
            return new PagerChangeListener(imageAnimator);
        }

        /**
         * 滑动监听
         *
         * @param position             当前位置
         * @param positionOffset       偏移[当前值+-1]
         * @param positionOffsetPixels 偏移像素
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            Log.e("DEBUG-WCL", "position: " + position + ", positionOffset: " + positionOffset);

            // 以前滑动, 现在终止
            if (isFinishedScrolling(position, positionOffset)) {
                finishScroll(position);
            }

            // 判断前后滑动
            if (isStartingScrollToPrevious(position, positionOffset)) {
                startScroll(position);
            } else if (isStartingScrollToNext(position, positionOffset)) {
                startScroll(position + 1); // 向后滚动需要加1
            }

            // 向后滚动
            if (isScrollingToNext(position, positionOffset)) {
                mImageAnimator.forward(positionOffset);
            } else if (isScrollingToPrevious(position, positionOffset)) { // 向前滚动
                mImageAnimator.backwards(positionOffset);
            }
        }

        /**
         * 终止滑动
         * 滑动 && [偏移是0&&滑动终点] || 动画之中
         *
         * @param position       位置
         * @param positionOffset 偏移量
         * @return 终止滑动
         */
        public boolean isFinishedScrolling(int position, float positionOffset) {
            return mIsScrolling && (positionOffset == 0f && position == mFinalPosition) || !mImageAnimator.isWithin(position);
        }

        /**
         * 从静止到开始滑动, 下一个
         * 未滑动 && 位置是当前位置 && 偏移量不是0
         *
         * @param position       位置
         * @param positionOffset 偏移量
         * @return 是否
         */
        private boolean isStartingScrollToNext(int position, float positionOffset) {
            return !mIsScrolling && position == mCurrentPosition && positionOffset != 0f;
        }

        /**
         * 从静止到开始滑动, 前一个[position会-1]
         *
         * @param position       位置
         * @param positionOffset 偏移量
         * @return 是否
         */
        private boolean isStartingScrollToPrevious(int position, float positionOffset) {
            return !mIsScrolling && position != mCurrentPosition && positionOffset != 0f;
        }

        /**
         * 开始滚动, 向后
         *
         * @param position       位置
         * @param positionOffset 偏移
         * @return 是否
         */
        private boolean isScrollingToNext(int position, float positionOffset) {
            return mIsScrolling && position == mCurrentPosition && positionOffset != 0f;
        }

        /**
         * 开始滚动, 向前
         *
         * @param position       位置
         * @param positionOffset 偏移
         * @return 是否
         */
        private boolean isScrollingToPrevious(int position, float positionOffset) {
            return mIsScrolling && position != mCurrentPosition && positionOffset != 0f;
        }

        /**
         * 开始滑动
         * 滚动开始, 结束位置是position[前滚时position会自动减一], 动画从当前位置到结束位置.
         *
         * @param position 滚动结束之后的位置
         */
        private void startScroll(int position) {
            mIsScrolling = true;
            mFinalPosition = position;

            // 开始滚动动画
            mImageAnimator.start(mCurrentPosition, position);
        }

        /**
         * 如果正在滚动, 结束时, 固定position位置, 停止滚动, 调动截止动画
         *
         * @param position 位置
         */
        private void finishScroll(int position) {
            if (mIsScrolling) {
                mCurrentPosition = position;
                mIsScrolling = false;
                mImageAnimator.end(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //NO-OP
        }

        @Override
        public void onPageSelected(int position) {
            if (!mIsScrolling) {
                mIsScrolling = true;
                mFinalPosition = position;
                mImageAnimator.start(mCurrentPosition, position);
            }
        }
    }

    private static class ImageAnimator {
        private static final float FACTOR = 0.1f;

        private final ContentPagerAdapter mAdapter; // 适配器
        private final ImageView mTargetImage; // 原始图片
        private final ImageView mOutgoingImage; // 渐变图片

        private int mActualStart; // 实际起始位置

        private int mStart;
        private int mEnd;

        public ImageAnimator(ContentPagerAdapter adapter, ImageView targetImage, ImageView outgoingImage) {
            mAdapter = adapter;
            mTargetImage = targetImage;
            mOutgoingImage = outgoingImage;
        }

        /**
         * 启动动画, 之后选择向前或向后滑动
         *
         * @param startPosition 起始位置
         * @param endPosition   终止位置
         */
        public void start(int startPosition, int endPosition) {
            mActualStart = startPosition;

            // 终止位置的图片
//            @DrawableRes int incomeId = mAdapter.getDrawable(endPosition);

            Drawable income = mAdapter.getCoverBitmap(endPosition);
            // 原始图片
            mOutgoingImage.setImageDrawable(mTargetImage.getDrawable()); // 原始的图片

            // 起始图片
            mOutgoingImage.setTranslationX(0f);

            mOutgoingImage.setVisibility(View.VISIBLE);
            mOutgoingImage.setAlpha(1.0f);

            // 目标图片
//            mTargetImage.setImageResource(incomeId);
            mTargetImage.setImageDrawable(income);

            mStart = Math.min(startPosition, endPosition);
            mEnd = Math.max(startPosition, endPosition);
        }

        /**
         * 滑动结束的动画效果
         *
         * @param endPosition 滑动位置
         */
        public void end(int endPosition) {
//            @DrawableRes int incomeId = mAdapter.getDrawable(endPosition);
            Drawable income = mAdapter.getCoverBitmap(endPosition);
            mTargetImage.setTranslationX(0f);

            // 设置原始图片
            if (endPosition == mActualStart) {
                mTargetImage.setImageDrawable(mOutgoingImage.getDrawable());
            } else {
                mTargetImage.setImageDrawable(income);
                mTargetImage.setAlpha(1f);
                mOutgoingImage.setVisibility(View.GONE);
            }
        }

        // 向前滚动, 比如0->1, offset滚动的距离(0->1), 目标渐渐淡出
        public void forward(float positionOffset) {
            Log.e("DEBUG-WCL", "forward-positionOffset: " + positionOffset);
            int width = mTargetImage.getWidth();
            mOutgoingImage.setTranslationX(-positionOffset * (FACTOR * width));
            mTargetImage.setTranslationX((1 - positionOffset) * (FACTOR * width));

            mTargetImage.setAlpha(positionOffset);
        }

        // 向后滚动, 比如1->0, offset滚动的距离(1->0), 目标渐渐淡入
        public void backwards(float positionOffset) {
            Log.e("DEBUG-WCL", "backwards-positionOffset: " + positionOffset);
            int width = mTargetImage.getWidth();
            mOutgoingImage.setTranslationX((1 - positionOffset) * (FACTOR * width));
            mTargetImage.setTranslationX(-(positionOffset) * (FACTOR * width));

            mTargetImage.setAlpha(1 - positionOffset);
        }

        // 判断停止
        public boolean isWithin(int position) {
            return position >= mStart && position < mEnd;
        }
    }
}
