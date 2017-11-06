package com.isaac.smartdrawer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.isaac.smartdrawer.ContentDbSchema.CategoriesTable;
import com.isaac.smartdrawer.ContentDbSchema.ContentsTable;

/**
 * Created by Isaac on 2016/7/30 0030.
 */
public class ContentBaseHelper extends SQLiteOpenHelper{
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "ContentBase.db";

    public ContentBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ContentsTable.NAME + "(" +
                    ContentsTable.Cols.ID + " TEXT PRIMARY KEY, " +
                    ContentsTable.Cols.NAME + " TEXT, " +
                    ContentsTable.Cols.CATEGORY + " TEXT, " +
                    ContentsTable.Cols.EXIST + " INT" +
                    ")"
        );

        db.execSQL("CREATE TABLE " + CategoriesTable.NAME + "(" +
                CategoriesTable.Cols.CATEGORY + " TEXT PRIMARY KEY" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
