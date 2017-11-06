package com.isaac.smartdrawer;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * Created by Isaac on 2016/8/1 0001.
 */

public class DbSyncTask extends AsyncTask<Object, Void, Object> {
    private ContentLab mContentLab;

    public interface SyncCallback{
        void callback();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Context context;
        if (objects[0] instanceof Fragment) context = ((Fragment) objects[0]).getActivity();
        else context = (Context) objects[0];

        mContentLab = ContentLab.instance(context);
        List<String> ids = mContentLab.getIds();
        List<String> categories = mContentLab.getCategories();

        for (Content content : Client.instance().getAllContents()) {
            if (content == null) return null;

            if (ids.contains(content.getId())) {
                mContentLab.updateContent(content);
            } else {
                mContentLab.addContent(content);
            }

            String category = content.getCategory();

            if (!(categories.contains(category) || category.equals("未分类"))) {
                mContentLab.addCategory(category);
                categories.add(category);
            }

        }
        return objects[0];
    }

    @Override
    protected void onPostExecute(Object object) {
        if (object == null) return;

        ((SyncCallback) object).callback();
    }

}
