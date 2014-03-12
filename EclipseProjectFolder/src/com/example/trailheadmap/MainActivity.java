package com.example.trailheadmap;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements AddMarkerDialog.AddMarkerDialogListener {

	// Constants
	private static final int INITIAL_DISTANCE = 3;
	
	private static final int MAX_DISTANCE = 30;
	
	/** Request codes for starting new Activities. */
	private static final int ENABLE_GPS_REQUEST_CODE = 1;
	
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
    private Marker lastMarker;
    private LatLng newMarkerLocation;
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
        datasource.getJokesFromServer();

        
        try {
            // Loading map
            initilizeMap();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        initListeners();
        if (googleMap != null) {
        	googleMap.setOnMyLocationChangeListener(locChangeList);
        }
    }
 
    /** Initializes the options menu.*/
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		onPrepareOptionsMenu(menu);
		return true;
	}
    
    /** For handling clicks to the menu. */
    @Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		
		case R.id.enable_gps:
			this.enableGps();
			break;
			
		default: return super.onOptionsItemSelected(item);
		}
		return true;
	}
    
    /** Function to go to the GPS settings so that the user can enable the gps. */
    private void enableGps() {
    	Intent gpsOn = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(gpsOn, ENABLE_GPS_REQUEST_CODE);
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
        }
    }
    
    protected void initListeners(){
    	locChangeList = (new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
               fillArray();
         	   latitude = arg0.getLatitude();
         	   longitude = arg0.getLongitude();
         	   if (onlyOnce){
         		   onlyOnce = false;
         		   CameraPosition cameraPosition = new CameraPosition.Builder().target(
    	                       new LatLng(latitude, longitude)).zoom(12).build();
    	        
    	        	   googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
         	   }
         	   
         	   googleMap.clear();
         	   
         	   redrawMarkers();
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
    	
        googleMap.setOnMapLongClickListener((new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
            	initiateAddMarkerDialog(latLng);
<<<<<<< HEAD
            	//TODO stupid inefficient
=======
>>>>>>> 7f8136e5d118bbb4e0be6a551b8a450acd247d78
            	fillArray();
            }
        }));
        
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

			@Override
			public boolean onMarkerClick(Marker marker) {
				lastMarker = marker;
				marker.showInfoWindow();
				return false;
			}
        });
        
        /** Listener to allow users to rate markers by clicking on their info windows. */
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub
				Toast toast = Toast.makeText(MainActivity.this, "Initiate rating system.", Toast.LENGTH_SHORT);
				toast.show();
				initiateRateMarkerDialog(marker);
			}
        	
        });
    }
    
    /** Resets the trail head markers. */
    private void redrawMarkers() {
    	fillArray();
    	/** Location marker searching occurs here */
        for(int i = 0; i < locationMarkerList.size(); i++){
     	   Location.distanceBetween(latitude, longitude, locationMarkerList.get(i).getPosition().latitude, locationMarkerList.get(i).getPosition().longitude, result);
	           	if((result[0] / 1609.34) < viewDistance && Pattern.compile(Pattern.quote(searchString), Pattern.CASE_INSENSITIVE).matcher(locationMarkerList.get(i).getTitle()).find())
	           		googleMap.addMarker(locationMarkerList.get(i));
	           	// Display last clicked marker's info window for persistence.
	        	if (lastMarker != null) {
	        		lastMarker.showInfoWindow();
	        	}
        } 
    }
 
    
    private void fillArray(){
    	locationMarkerList.clear();
    	Cursor cursor = datasource.getAllTrails();
    	cursor.moveToFirst();
    	while(!cursor.isAfterLast()){
    		locationMarkerList.add(new MarkerOptions().position(new LatLng(cursor.getDouble(4), cursor.getDouble(5))).title(cursor.getString(1)).snippet(this.getString(R.string.rating_text) + (int)(cursor.getDouble(2) * 100) + "%"));
    		cursor.moveToNext();
    	}
    	
    }
    
    private void updateDistance() {
    	viewDistance = slider.getProgress();
    	distText.setText(viewDistance + " " + getString(R.string.units));
    	redrawMarkers();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }
    
    /** Initiates dialog window to add marker. */
    private void initiateAddMarkerDialog(LatLng latLng) {
    	this.newMarkerLocation = latLng;
    	DialogFragment dialog = new AddMarkerDialog();
        dialog.show(getSupportFragmentManager(), "AddMarkerDialog");
    }

    /** Callback method to use add trail head marker when that button is clicked in the dialog. */
	@Override
	public void onAddMarkerClick(DialogFragment dialog) {
		datasource.createTrail(((AddMarkerDialog) dialog).getNewMarkerName(), newMarkerLocation.latitude, newMarkerLocation.longitude);	
		redrawMarkers();
	}
	
	/** Initiates dialog window to rate marker. */
    private void initiateRateMarkerDialog(Marker marker) {
    	// TODO: Initiate rating system
    	//DialogFragment dialog = new RateMarkerDialog();
        //dialog.show(getSupportFragmentManager(), "RateMarkerDialog");
    }
}