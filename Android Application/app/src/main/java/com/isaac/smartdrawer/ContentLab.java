package com.isaac.smartdrawer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;

import com.isaac.smartdrawer.ContentDbSchema.ContentsTable;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Isaac on 2016/7/30 0030.
 */

public class ContentLab {
    private static ContentLab sContentLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static ContentLab instance(Context context) {
        if (sContentLab == null) {
            synchronized (ContentLab.class) {
                if (sContentLab == null) {
                    sContentLab = new ContentLab(context);
                }
            }
        }
        return sContentLab;
    }

    private ContentLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ContentBaseHelper(mContext).getWritableDatabase();
    }

    public void updateContent(Content content) {
        ContentValues values = new ContentValues();
        values.put(ContentsTable.Cols.NAME, content.getName());
        values.put(ContentsTable.Cols.CATEGORY, content.getCategory());
        values.put(ContentsTable.Cols.EXIST, content.isExist()?1:0);

        updateById(values, content.getId());
    }

    public int[] setExist(List<String> idsInDrawer) {
        if (idsInDrawer == null) {
            Log.i("ContentLab", "noChange");
            return null;
        }

        List<String> idsInDb = getIds();
        List<String> outIds = new ArrayList<>(idsInDb);
        List<String> intersection = new ArrayList<>(idsInDb);
        intersection.retainAll(idsInDrawer);
        List<String> inIds = idsInDrawer;

        outIds.removeAll(intersection);
        inIds.removeAll(intersection);

        ContentValues outValues = new ContentValues();
        outValues.put(ContentsTable.Cols.EXIST, 0);
        ContentValues inValues = new ContentValues();
        inValues.put(ContentsTable.Cols.EXIST, 1);

        int[] out_new = new int[2];

        for (String outId : outIds) {
            updateById(outValues, outId);
        }
        out_new[0] = outIds.size();

        for (String inId : inIds) {
            if (idsInDb.contains(inId)) {
                updateById(inValues, inId);
            } else {
                addContent(new Content(inId, "未命名", "未分类", true));
                out_new[1]+=1;
            }
        }

        return out_new;

    }

    public List<Content> getContentsByName(String name) {
        ContentCursorWrapper cursor = new ContentCursorWrapper(mDatabase.rawQuery("SELECT * FROM " +
                ContentsTable.NAME +
                " WHERE " +
                ContentsTable.Cols.NAME +" LIKE ?",
                new String[]{"%"+name+"%"}));

        List<Content> contents = new ArrayList<>();
        try {
            if (cursor.getCount() == 0) return contents;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                contents.add(cursor.getContent());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return contents;
    }

    public void addContent(Content content) {
        ContentValues values = new ContentValues();

        values.put(ContentsTable.Cols.ID, content.getId());
        values.put(ContentsTable.Cols.NAME, content.getName());
        values.put(ContentsTable.Cols.EXIST, content.isExist()?1:0);
        values.put(ContentsTable.Cols.CATEGORY, content.getCategory());

        mDatabase.insert(ContentsTable.NAME, null, values);
    }

    public void addCategory(String category) {
        ContentValues values = new ContentValues();
        values.put(ContentDbSchema.CategoriesTable.Cols.CATEGORY, category);
        mDatabase.insert(ContentDbSchema.CategoriesTable.NAME, null, values);
    }

    public void deleteCategory(String category) {
        mDatabase.delete(ContentDbSchema.CategoriesTable.NAME,
                ContentDbSchema.CategoriesTable.Cols.CATEGORY + " = ?",
                new String[]{category});
    }

    public void deleteContent(String id) {
        mDatabase.delete(ContentsTable.NAME, ContentsTable.Cols.ID + " = ?", new String[]{id});
    }

    public List<Content> getContents(String[] categories) {
        List<Content> contents = new ArrayList<>();

        ContentCursorWrapper cursor = queryContents(ContentsTable.Cols.CATEGORY + " = ?", categories);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                contents.add(cursor.getContent());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return contents;
    }

    public List<Content> getContents() {
        List<Content> contents = new ArrayList<>();

        ContentCursorWrapper cursor = queryContents(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                contents.add(cursor.getContent());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return contents;
    }

    public Content getContent(String id) {
        ContentCursorWrapper cursor = queryContents(ContentsTable.Cols.ID + " = ?", new String[]{id});
        try {
            if (cursor.getCount() == 0) {
                return new Content(id, id,null,false);
            }
            cursor.moveToFirst();
            return cursor.getContent();
        } finally {
            cursor.close();
        }
    }

    private void updateById(ContentValues values, String id) {
        mDatabase.update(ContentsTable.NAME, values, ContentsTable.Cols.ID + " = ?", new String[]{id});
    }

    private ContentCursorWrapper queryContents(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ContentsTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new ContentCursorWrapper(cursor);
    }

    public List<String> getCategories() {
        CursorWrapper cursor = new CursorWrapper(mDatabase.query(
                ContentDbSchema.CategoriesTable.NAME,
                new String[]{ContentDbSchema.CategoriesTable.Cols.CATEGORY},
                null,
                null,
                null,
                null,
                null
        ));
        List<String> categories = new ArrayList<>();
        categories.add("未分类");
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                categories.add(cursor.getString(cursor.getColumnIndex(ContentDbSchema.CategoriesTable.Cols.CATEGORY)));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return categories;
    }

    public List<String> getIds() {
        ContentCursorWrapper cursor = new ContentCursorWrapper(mDatabase.query(
                ContentsTable.NAME,
                new String[]{ContentsTable.Cols.ID},
                null,
                null,
                null,
                null,
                null
        ));
        List<String> idsInDb = new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                idsInDb.add(cursor.getId());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return idsInDb;
    }
}
