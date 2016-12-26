package com.example.androidreadcallhistory;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SqliteDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CallsDB.db";

    public static final String CallLog = "CallLog";

    public static final String LOG_ID = "logId";
    public static final String TYPE = "type";
    public static final String DATE = "date";
    public static final String DURATION = "duration_sec";
    public static final String NUMBER = "number";

    public SqliteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table " + CallLog + "(logId int , type text,date text,duration_sec text, number text )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CallLog);
        onCreate(db);
    }

    public void insertCallLog(ArrayList<Model> modelArrayList) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues S = new ContentValues();
        Model model = null;
        db.delete(CallLog,null,null);

        for (int i = 0; i < modelArrayList.size(); i++) {

            model = modelArrayList.get(i);

            S.put(LOG_ID, model.getLogId());
            S.put(DATE, model.getDate());
            S.put(TYPE, model.getType());
            S.put(DURATION, model.getDuration());
            S.put(NUMBER, model.getNumber());

            long restul=db.insert(CallLog, null, S);
            long g=restul;
        }
        db.close();
    }
}
