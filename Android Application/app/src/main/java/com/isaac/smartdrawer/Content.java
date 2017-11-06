package com.isaac.smartdrawer;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Isaac on 2016/7/29 0029.
 */
public class Content implements Serializable{
    private String mId, mName, mCategory;
    private boolean mIsExist;

    public Content(String id, String name, String category, boolean isExist) {
        this.mId = id;
        this.mName = name;
        this.mCategory = category;
        this.mIsExist = isExist;
    }


    public String getId() {
            return mId;
        }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

//    public String getPictureDir() {
//        return "PIC_" + mId;
//    }

    public String getCoverPath(Context context) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "COV_" + mId);
        return file.getAbsolutePath();
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public boolean isExist() {
        return mIsExist;
    }

}