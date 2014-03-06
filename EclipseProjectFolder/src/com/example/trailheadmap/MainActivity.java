package com.example.trailheadmap;

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
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {

	// Constants
	private static final int DISTANCE_1 = 3;
	private static final int DISTANCE_2 = 10;
	
	// String where dynamic search text is stored
	private String searchString = "";
    private EditText searchText;
	
	// Google Map
    private GoogleMap googleMap;
    private double latitude;
    private double longitude;
    private boolean onlyOnce = true;
    private RadioGroup radioGroup;
    private MarkerOptions[] locationMarkers = new MarkerOptions[2];
    private double[] distances = new double[2]; //in miles
    private Location tempLoc;
    private int viewDistance;
    private OnMyLocationChangeListener locChangeList;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDistance = DISTANCE_1;
        setContentView(R.layout.activity_main);
        searchText = (EditText) findViewById(R.id.search);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group_list_selector);
        radioGroup.check(R.id.distance1);   
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.distance1)
                	viewDistance = DISTANCE_1;
                else if(checkedId == R.id.distance2)
                	viewDistance = DISTANCE_2;
                else
                	viewDistance = 0;
            }
        });
        
        locationMarkers[0] = new MarkerOptions().position(new LatLng(35.29727, -120.68520)).title("Bishop's Peak");
        locationMarkers[1] = new MarkerOptions().position(new LatLng(35.33171, -120.73140)).title("El chorro regional park");
        tempLoc = new Location("temp");
        initListeners();
        try {
            // Loading map
            initilizeMap();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
       
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
         	   for(int i = 0; i < distances.length; i++){
         		   tempLoc.setLatitude(locationMarkers[i].getPosition().latitude);
         		   tempLoc.setLongitude(locationMarkers[i].getPosition().longitude);
         		   distances[i] =  arg0.distanceTo(tempLoc) / 1609.34;
         	   }
         	   
         	   googleMap.clear();
         	   
         	   /** Location marker searching occurs here */
               for(int i = 0; i < locationMarkers.length; i++){
    	           	if(distances[i] < viewDistance && locationMarkers[i].getTitle().contains(searchString))
    	           		googleMap.addMarker(locationMarkers[i]);
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
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }
}