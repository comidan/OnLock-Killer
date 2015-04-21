package com.dev.smartlock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by daniele on 09/04/2015.
 */
public class AppDatabase extends SQLiteOpenHelper
{
    private static final String DB_NAME = "APPS_TO_KILL";

    private static final int DB_VERSION = 1;

    public AppDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE KillList ";
        sql += "(package TEXT PRIMARY KEY);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
