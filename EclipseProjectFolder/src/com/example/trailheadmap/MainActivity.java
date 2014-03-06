package com.example.trailheadmap;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {

	// Constants
	private static final int INITIAL_DISTANCE = 3;
	
	private static final int MAX_DISTANCE = 30;
	
	// String where dynamic search text is stored
	private String searchString = "";
    private EditText searchText;
    //private RadioGroup radioGroup;
    private SeekBar slider;
    private TextView distText;

	
	// Google Map
    private GoogleMap googleMap;
    private double latitude;
    private double longitude;
    private boolean onlyOnce = true;
    private ArrayList<MarkerOptions> locationMarkerList;
    private int viewDistance;
    private OnMyLocationChangeListener locChangeList;
    private TrailDataSource datasource;
    private float[] result = new float[1];

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDistance = INITIAL_DISTANCE;
        setContentView(R.layout.activity_main);
        searchText = (EditText) findViewById(R.id.search);
        locationMarkerList = new ArrayList<MarkerOptions>();
        datasource = new TrailDataSource(this);
        datasource.open();
        
        slider = (SeekBar) findViewById(R.id.searchSlider);
        slider.setMax(MAX_DISTANCE);
        slider.setProgress(viewDistance);
        distText = (TextView) findViewById(R.id.sliderValue);
        updateDistance();
        
        /* TODO here check for internet connection, check onlined database and sink.
         * right now, delete the table and re-add the hard-coded in links
         */
        datasource.clearTable();
        populateTable();
        fillArray();

        initListeners();
        try {
            // Loading map
            initilizeMap();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
       
    }
 
    /**
     * function to load map. If map is not created it will create it for you.
     * */
    private void initilizeMap() {
    	// Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            googleMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
            	googleMap.setOnMyLocationChangeListener(locChangeList);
            }
        }
    }
    
    protected void initListeners(){
    	locChangeList = (new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
         	   latitude = arg0.getLatitude();
         	   longitude = arg0.getLongitude();
         	   if (onlyOnce){
         		   onlyOnce = false;
         		   CameraPosition cameraPosition = new CameraPosition.Builder().target(
    	                       new LatLng(latitude, longitude)).zoom(12).build();
    	        
    	        	   googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
         	   }
         	   
         	   googleMap.clear();
         	   
         	   /** Location marker searching occurs here */
               for(int i = 0; i < locationMarkerList.size(); i++){
            	   Location.distanceBetween(latitude, longitude, locationMarkerList.get(i).getPosition().latitude, locationMarkerList.get(i).getPosition().longitude, result);
    	           	if((result[0] / 1609.34) < viewDistance && Pattern.compile(Pattern.quote(searchString), Pattern.CASE_INSENSITIVE).matcher(locationMarkerList.get(i).getTitle()).find())
    	           		googleMap.addMarker(locationMarkerList.get(i));
               } 
            
            }
           });
    	
    	// Listener for changes in the search box
    	searchText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void afterTextChanged(Editable s) {
				searchString = s.toString();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				return;
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				return;
			}
		});
    	
    	// OnKeyListener to close keyboard when enter is pressed.
    	searchText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					if (event.getAction() == KeyEvent.ACTION_UP) {
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
					}
					return true;
				}
				else
					return false;
			}
		});
    	
    	slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateDistance();
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {				
			}
    	});
    }
 
    private void populateTable(){
    	datasource.createTrail("Bishop's Peak", 35.29727, -120.68520);
    	datasource.createTrail("El chorro regional park" , 35.33171, -120.73140);
    	datasource.createTrail("Reservoir Canyon" , 35.286302, -120.622689);
    	datasource.createTrail("Cerro" , 35.282752, -120.680450);
    	datasource.createTrail("Felsman Loop" , 35.306269, -120.691686);
    	datasource.createTrail("Laguna Lake" , 35.271036, -120.685577);
    	datasource.createTrail("South Hills" , 35.262235, -120.659639);
    	datasource.createTrail("Yucca Ridge" , 35.263087, -120.660932);
    	datasource.createTrail("Lemon Grove Loop", 35.275062, -120.672256);
    	datasource.createTrail("Terrace Hill" , 35.273469, -120.650761);
    	datasource.createTrail("Irish Hills" , 35.237753, -120.780454);
    }
    
    private void fillArray(){
    	Cursor cursor = datasource.getAllTrails();
    	cursor.moveToFirst();
    	while(!cursor.isAfterLast()){
    		locationMarkerList.add(new MarkerOptions().position(new LatLng(cursor.getDouble(4), cursor.getDouble(5))).title(cursor.getString(1)));
    		cursor.moveToNext();
    	}
    }
    
    private void updateDistance() {
    	viewDistance = slider.getProgress();
    	distText.setText(viewDistance + " " + getString(R.string.units));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }
}