package com.example.trailheadmap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

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
        uploadTrailToServer(values);
    }
    
    public void addTrail(String trailName, float rating, int numRatings, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put("_trailName", trailName);
        values.put("_rating", rating);
        values.put("_numRatings", numRatings);
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
    
	protected void uploadTrailToServer(ContentValues trail) {
		
		// The following code accomplishes these things:
		// create an AsyncTask inner class and call execute on it
		// use URL to upload a joke to the server, and Scanner to check that it was successful
		// use Toast to notify if the upload was successful

		try {
			URL url = new URL("http://dontcare.x10.mx/trails/addOneTrail.php?" + "_trailName="
					+ URLEncoder.encode(trail.getAsString("_trailName") , "UTF-8") + "&_rating="
					+ URLEncoder.encode(trail.getAsString("_rating") , "UTF-8") + "&_numRatings="
					+ URLEncoder.encode(trail.getAsString("_numRatings") , "UTF-8") + "&_latitude="
					+ URLEncoder.encode(trail.getAsString("_latitude") , "UTF-8") + "&_longitude="
					+ URLEncoder.encode(trail.getAsString("_longitude") , "UTF-8"));
			new AsyncTask<URL, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(URL... urls) {
			        return true;
			     }

			     protected void onPostExecute(Boolean success) {
			     }

			}.execute(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	protected void getJokesFromServer() {		
		try {
			URL url = new URL("http://dontcare.x10.mx/trails/getAllTrails.php");
			new AsyncTask<URL, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(URL... urls) {
					try {
						Scanner in, parsed;
						in = new Scanner(urls[0].openStream()); 
						in.useDelimiter("\n");
						while(in.hasNext()){
							parsed = new Scanner(in.next());
							parsed.useDelimiter(",");
							addTrail(parsed.next(), Float.valueOf(parsed.next()), Integer.valueOf(parsed.next()), Double.valueOf(parsed.next()), Double.valueOf(parsed.next()));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			        return true;
			     }

			     protected void onPostExecute(Boolean success) {
			     }

			}.execute(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
} 