package com.example.mednotifyplus.Cost;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DBHelperCost extends SQLiteOpenHelper {

    private static final String DB_NAME = "medsDB";
    private static final int DB_VERSION = 5;
    private static final String TABLE_NAME = "medicines";
    private static final String LOG_TABLE = "activity_logs";

    private static boolean isPersistenceEnabled = false;

    private final DatabaseReference firebaseRef;
    private final DatabaseReference logsRef;

    public DBHelperCost(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        if (!isPersistenceEnabled) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                isPersistenceEnabled = true;
            } catch (Exception e) {
                Log.d("DBHelperCost", "Persistence already enabled or failed: " + e.getMessage());
            }
        }

        firebaseRef = FirebaseDatabase.getInstance().getReference("medicines");
        logsRef = FirebaseDatabase.getInstance().getReference("logs");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMedicinesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "name TEXT PRIMARY KEY, " +
                "type TEXT, " +
                "price_this_year REAL, " +
                "price_last_year REAL, " +
                "category TEXT, " +
                "is_favorite INTEGER DEFAULT 0, " +
                "instructions TEXT, " +
                "reference TEXT, " +
                "max_intake INTEGER)";

        String createLogsTable = "CREATE TABLE IF NOT EXISTS " + LOG_TABLE + " (" +
                "timestamp TEXT, " +
                "action TEXT, " +
                "details TEXT)";

        db.execSQL(createMedicinesTable);
        db.execSQL(createLogsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE);
        onCreate(db);
    }

    public void initializeSampleData() {
        insertMedicineOfflineAndSync("Tempra", "Branded", 4.0, 3.6, "Headache", "Take every 6 hours as needed.", "Watsons", 4);
        insertMedicineOfflineAndSync("Diatabs", "Generic", 2.0, 1.8, "Stomach", "Take 1 tablet after each loose bowel movement.", "Generika", 3);
        insertMedicineOfflineAndSync("Bioflu", "Branded", 5.5, 5.0, "Cold & Flu", "Take 1 tablet every 4 hours if symptoms persist.", "Mercury Drug", 3);
    }

    public void insertMedicineOfflineAndSync(String name, String type, double priceThisYear,
                                             double priceLastYear, String category, String instructions,
                                             String reference, int maxIntake) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("type", type);
        cv.put("price_this_year", priceThisYear);
        cv.put("price_last_year", priceLastYear);
        cv.put("category", category);
        cv.put("is_favorite", 0);
        cv.put("instructions", instructions);
        cv.put("reference", reference);
        cv.put("max_intake", maxIntake);

        db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("type", type);
        data.put("price_this_year", priceThisYear);
        data.put("price_last_year", priceLastYear);
        data.put("category", category);
        data.put("instructions", instructions);
        data.put("reference", reference);
        data.put("max_intake", maxIntake);

        firebaseRef.child(name).setValue(data);
        logAction("INSERT", "Inserted medicine: " + name);
    }

    public Cursor getAllMedicines() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT rowid _id, * FROM " + TABLE_NAME + " ORDER BY name ASC", null);
    }

    public Cursor getMedicines(String name, String category, String sortOrder) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT rowid _id, * FROM " + TABLE_NAME);
        boolean hasCondition = false;

        if (!"ALL".equalsIgnoreCase(name)) {
            query.append(" WHERE name LIKE ?");
            hasCondition = true;
        }

        if (!"ALL".equalsIgnoreCase(category)) {
            query.append(hasCondition ? " AND" : " WHERE");
            query.append(" category = ?");
        }

        query.append(" ORDER BY price_this_year ").append(sortOrder);

        if (!"ALL".equalsIgnoreCase(name) && !"ALL".equalsIgnoreCase(category)) {
            return db.rawQuery(query.toString(), new String[]{"%" + name + "%", category});
        } else if (!"ALL".equalsIgnoreCase(name)) {
            return db.rawQuery(query.toString(), new String[]{"%" + name + "%"});
        } else if (!"ALL".equalsIgnoreCase(category)) {
            return db.rawQuery(query.toString(), new String[]{category});
        } else {
            return db.rawQuery(query.toString(), null);
        }
    }

    public void updateFavorite(String name, int isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_favorite", isFavorite);
        db.update(TABLE_NAME, cv, "name = ?", new String[]{name});
    }

    public Cursor getMedicineDetails(String name) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE name = ?", new String[]{name});
    }

    public void syncFromFirebase() {
        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                SQLiteDatabase db = getWritableDatabase();
                db.beginTransaction();
                try {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String name = child.child("name").getValue(String.class);
                        String type = child.child("type").getValue(String.class);
                        Double priceThis = child.child("price_this_year").getValue(Double.class);
                        Double priceLast = child.child("price_last_year").getValue(Double.class);
                        String category = child.child("category").getValue(String.class);
                        String instructions = child.child("instructions").getValue(String.class);
                        String reference = child.child("reference").getValue(String.class);
                        Integer maxIntake = child.child("max_intake").getValue(Integer.class);

                        if (name != null) {
                            ContentValues cv = new ContentValues();
                            cv.put("name", name);
                            cv.put("type", type != null ? type : "");
                            cv.put("price_this_year", priceThis != null ? priceThis : 0.0);
                            cv.put("price_last_year", priceLast != null ? priceLast : 0.0);
                            cv.put("category", category != null ? category : "");
                            cv.put("instructions", instructions != null ? instructions : "");
                            cv.put("reference", reference != null ? reference : "");
                            cv.put("max_intake", maxIntake != null ? maxIntake : 0);
                            cv.put("is_favorite", 0);
                            db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    Log.e("DBHelperCost", "Firebase sync failed: " + e.getMessage());
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("DBHelperCost", "Firebase sync cancelled: " + error.getMessage());
            }
        });
    }

    public void deleteMedicine(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "name = ?", new String[]{name});
        firebaseRef.child(name).removeValue();
        logAction("DELETE", "Deleted medicine: " + name);
    }

    public void updateMedicine(String oldName, String newName, String type, double priceThis,
                               double priceLast, String category, String instructions,
                               String reference, int maxIntake) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("type", type);
        values.put("price_this_year", priceThis);
        values.put("price_last_year", priceLast);
        values.put("category", category);
        values.put("instructions", instructions);
        values.put("reference", reference);
        values.put("max_intake", maxIntake);

        if (!oldName.equals(newName)) {
            db.delete(TABLE_NAME, "name = ?", new String[]{oldName});
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

            firebaseRef.child(oldName).removeValue();
            firebaseRef.child(newName).setValue(valuesToMap(values));
        } else {
            db.update(TABLE_NAME, values, "name = ?", new String[]{oldName});
            firebaseRef.child(newName).updateChildren(valuesToMap(values));
        }

        logAction("UPDATE", "Updated medicine: " + oldName + " to " + newName);
    }

    public void toggleFavorite(String name, boolean isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_favorite", isFavorite ? 1 : 0);
        db.update(TABLE_NAME, values, "name = ?", new String[]{name});
    }

    public void logAction(String action, String details) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        ContentValues logValues = new ContentValues();
        logValues.put("timestamp", timestamp);
        logValues.put("action", action);
        logValues.put("details", details);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(LOG_TABLE, null, logValues);

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", timestamp);
        logEntry.put("action", action);
        logEntry.put("details", details);
        logsRef.push().setValue(logEntry);
    }

    public DatabaseReference getFirebaseRef() {
        return firebaseRef;
    }

    public DatabaseReference getLogsRef() {
        return logsRef;
    }

    private Map<String, Object> valuesToMap(ContentValues values) {
        Map<String, Object> map = new HashMap<>();
        for (String key : values.keySet()) {
            Object value = values.get(key);
            if (value instanceof byte[]) continue;
            map.put(key, value);
        }
        return map;
    }
}
