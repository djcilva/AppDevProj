package com.example.trailheadmap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "trails";
    public static final String[] COLUMNS = {"_id", "_trailName", "_rating", "_numRatings", "_latitude", "_longitude"};

    private static final String DATABASE_NAME = "gginventory.db";
    private static final int DATABASE_VERSION = 1;


    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_NAME + "("
            + COLUMNS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMNS[1] + " VARCHAR(100), "
            + COLUMNS[2] + " FLOAT, "
            + COLUMNS[3] + " INTEGER, "
            + COLUMNS[4] + " FLOAT, "
            + COLUMNS[5] + " FLOAT "
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
    	Log.w("MySQLiteHelper", DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}