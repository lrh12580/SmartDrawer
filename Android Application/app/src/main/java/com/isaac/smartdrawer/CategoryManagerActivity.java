package com.isaac.smartdrawer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryManagerActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY = "com.isaac.smartdrawer.target_category";

    private List<String> mCategories;
    @BindView(R.id.activity_category_manager_toolbar) Toolbar mToolbar;
    @BindView(R.id.activity_category_manager_rv) RecyclerView mRecyclerView;

    public static Intent newIntent(Context context) {
        return new Intent(context, CategoryManagerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle("类别管理");

        mCategories = ContentLab.instance(this).getCategories();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(new CategoryAdapter(this, mCategories));

        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_category_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_category:
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_category_name_modifier, null);
                final TextInputEditText inputEditText = (TextInputEditText) view.findViewById(R.id.dialog_category_name_modifier);
                inputEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.length() == 0) inputEditText.setError(getString(R.string.error_blank));
                        if (mCategories.contains(editable.toString())) inputEditText.setError("该分类已存在！");
                    }
                });
                new AlertDialog.Builder(this)
                        .setTitle("请输入类别名称")
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String newCategory = inputEditText.getText().toString();
                                if (inputEditText.getError() != null) return;
                                ContentLab.instance(CategoryManagerActivity.this).addCategory(newCategory);
                                refresh();
                            }
                        })
                        .create()
                        .show();
                return true;
        }
        return false;
    }

    private void refresh() {
        mCategories.clear();
        mCategories.addAll(ContentLab.instance(this).getCategories());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }


    class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.category_fragment_name) TextView mCategoryName;
        @BindView(R.id.category_fragment_container) LinearLayout mContainer;
        @BindView(R.id.category_fragment_more) ImageButton mMoreBtn;

        public CategoryHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            mMoreBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.category_fragment_more:
                    PopupMenu popupMenu = new PopupMenu(CategoryManagerActivity.this, view);
                    popupMenu.getMenuInflater().inflate(R.menu.category_more, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            final Context context = CategoryManagerActivity.this;
                            View view;
                            switch (item.getItemId()) {
                                case R.id.popup_category_name:
                                    view = LayoutInflater.from(context).inflate(R.layout.dialog_category_name_modifier, null);
                                    final TextInputEditText inputEditText = (TextInputEditText) view.findViewById(R.id.dialog_category_name_modifier);
                                    inputEditText.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                        }
                                        @Override
                                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                        }
                                        @Override
                                        public void afterTextChanged(Editable editable) {
                                            if (editable.length() == 0) inputEditText.setError(getString(R.string.error_blank));
                                            if (mCategories.contains(editable.toString())) inputEditText.setError("该分类已存在！");
                                        }
                                    });
                                    new AlertDialog.Builder(context)
                                            .setTitle("请输入更改后的名称")
                                            .setView(view)
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    String newCategory = inputEditText.getText().toString();
                                                    if (inputEditText.getError() != null) return;
                                                    new ModifyCategoryTask().execute(mCategoryName.getText().toString(), newCategory, "delete");
                                                }
                                            })
                                            .create()
                                            .show();
                                    return true;

                                case R.id.popup_category_change:
                                    view = LayoutInflater.from(context).inflate(R.layout.dialog_category_category_modifier, null);
                                    final AppCompatSpinner spinner = (AppCompatSpinner) view.findViewById(R.id.dialog_category_category_modifier);
                                    List<String> categories = ContentLab.instance(context).getCategories();
                                    categories.remove(0);
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categories);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinner.setAdapter(adapter);
                                    new AlertDialog.Builder(context)
                                            .setTitle("请选择类别")
                                            .setView(view)
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    new ModifyCategoryTask().execute(mCategoryName.getText().toString(), spinner.getSelectedItem().toString());
                                                }
                                            })
                                            .create()
                                            .show();
                                    return true;

                                case R.id.popup_category_delete:
                                    new AlertDialog.Builder(context)
                                            .setTitle("其内物品将归类为 未分类")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    new ModifyCategoryTask().execute(mCategoryName.getText().toString(), "未分类", "delete");
                                                }
                                            })
                                            .create()
                                            .show();
                                    return true;
                            }

                            return false;
                        }
                    });
                    popupMenu.show();
                    break;

                default:
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_CATEGORY, mCategoryName.getText());
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
            }
        }

    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryHolder> {
        private Context mContext;
        private List<String> mCategories;

        public CategoryAdapter(Context context, List<String> categories) {
            mContext = context;
            mCategories = categories;
        }

        @Override
        public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CategoryHolder(LayoutInflater.from(mContext).inflate(R.layout.holder_category, parent, false));
        }

        @Override
        public void onBindViewHolder(CategoryHolder holder, int position) {
            holder.mCategoryName.setText(mCategories.get(position));
            if (position == 0) holder.itemView.setBackground(new ColorDrawable(0x30000000));
            holder.mContainer.removeAllViews();
            List<Content> contents = ContentLab.instance(mContext).getContents(new String[]{mCategories.get(position)});
            for (int i=0; i<(contents.size()<4?contents.size():4); i++) {
                TextView tv = new TextView(mContext);
                tv.setGravity(Gravity.CENTER);
                tv.setText(contents.get(i).getName());
                holder.mContainer.addView(tv);
            }
        }

        @Override
        public int getItemCount() {
            return mCategories.size();
        }
    }



    private class ModifyCategoryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String fromCategory = strings[0];
            String toCategory = strings[1];
            ContentLab contentLab = ContentLab.instance(CategoryManagerActivity.this);
            if (!contentLab.getCategories().contains(toCategory)) contentLab.addCategory(toCategory);

            List<Content> contents = contentLab.getContents(new String[]{fromCategory});
            for (Content content : contents) {
                content.setCategory(toCategory);
                contentLab.updateContent(content);
                Client.instance().update(content.getId(), ContentDbSchema.ContentsTable.Cols.CATEGORY, toCategory);
            }

            if (strings.length>2) ContentLab.instance(CategoryManagerActivity.this).deleteCategory(fromCategory);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            CategoryManagerActivity.this.refresh();
        }
    }
}
