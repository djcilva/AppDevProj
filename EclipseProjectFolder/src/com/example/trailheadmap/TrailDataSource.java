package com.example.trailheadmap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
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


    public void updateRating(String name, int _rating) {
        Cursor c = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_NAME + " WHERE " + MySQLiteHelper.COLUMNS[1] + "='" + name + "';", null);
        float rating;
        int numRatings;
        ContentValues values = new ContentValues();
        
        if(c.getCount() != 1)
        	return;
        c.moveToFirst();
        rating = c.getFloat(2);
        numRatings = c.getInt(3);
        rating = (rating * numRatings + _rating) / (numRatings + 1);
        numRatings++;
        
        values.put("_rating", rating);
        values.put("_numRatings", numRatings);
        values.put("_trailName", name);
        
        database.update(MySQLiteHelper.TABLE_NAME, values, "_trailName='" + values.getAsString("_trailName") + "'", null);
        
        
        updateRatingToDatabase(values);
    }

    public Cursor getAllTrails() {
        return database.query(MySQLiteHelper.TABLE_NAME, allColumns, null, null, null, null, null);
    }
    
    public Cursor queryDatabase(String query){
    	return database.rawQuery(query, null);
    }
    
    public void updateRatingToDatabase(ContentValues trail) {
    	try {
			URL url = new URL("http://dontcare.x10.mx/trails/updateOneTrail.php?" + "_trailName="
					+ URLEncoder.encode(trail.getAsString("_trailName") , "UTF-8") + "&_rating="
					+ URLEncoder.encode(trail.getAsString("_rating") , "UTF-8") + "&_numRatings="
					+ URLEncoder.encode(trail.getAsString("_numRatings") , "UTF-8"));

			new AsyncTask<URL, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(URL... urls) {
					boolean success = false;
					try {
						Scanner in;
						in = new Scanner(urls[0].openStream()); 
						if (in.nextLine().equals("1 record updated")) {
							Log.e("tits", "oh it's amazing");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			        return success;
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
    
	protected void uploadTrailToServer(ContentValues trail) {

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
					boolean success = false;
					try {
						Scanner in;
						in = new Scanner(urls[0].openStream()); 
						if (in.nextLine().equals("1 record added")) {
							success = true;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			        return success;
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