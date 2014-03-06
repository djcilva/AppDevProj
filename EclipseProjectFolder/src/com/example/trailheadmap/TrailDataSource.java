package com.example.trailheadmap;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TrailDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = MySQLiteHelper.COLUMNS;

    public TrailDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createTrail(String trailName, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put("_trailName", trailName);
        values.put("_rating", 0);
        values.put("_numRatings", 0);
        values.put("_latitude", latitude);
        values.put("_longitude", longitude);

        database.insert(MySQLiteHelper.TABLE_NAME, null, values);
    }
    
    public void clearTable(){
    	database.delete(MySQLiteHelper.TABLE_NAME, null, null);
    }


    public void updateTrail(String trailName, int qty, String type, String notes, String details, int retail, int landscape) {
        //TODO add rating to running average and update
    }

    public Cursor getAllTrails() {
        return database.query(MySQLiteHelper.TABLE_NAME, allColumns, null, null, null, null, null);
    }
    
    public Cursor queryDatabase(String query){
    	return database.rawQuery(query, null);
    }
} 