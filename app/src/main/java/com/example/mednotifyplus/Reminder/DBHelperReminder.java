package com.example.mednotifyplus.Reminder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DBHelperReminder extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MedicineReminders.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "MedicineReminders";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DOSAGE = "dosage";
    public static final String COL_TIME = "time";

    public DBHelperReminder(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_DOSAGE + " TEXT, " +
                COL_TIME + " LONG)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addReminder(String name, String dosage, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DOSAGE, dosage);
        values.put(COL_TIME, time);
        return db.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllReminders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public int updateReminder(int id, String name, String dosage, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DOSAGE, dosage);
        values.put(COL_TIME, time);
        return db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}