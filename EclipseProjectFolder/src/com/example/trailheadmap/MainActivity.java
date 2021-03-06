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
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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

public class MainActivity extends FragmentActivity implements AddMarkerDialog.AddMarkerDialogListener,
															  RateMarkerDialog.RateMarkerDialogListener {

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
    private Marker focusMarker;
    private LatLng newMarkerLocation;
    private boolean onlyOnce = true;
    private ArrayList<MarkerOptions> locationMarkerList;
    private int viewDistance;
    private OnMyLocationChangeListener locChangeList;
    private TrailDataSource datasource;
    private float[] result = new float[1];

    private static final long LOC_UPDATE_DELAY = 5000;  // miliseconds
    
    private LocationManager m_locManager;

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
        
        // lastTime = SystemClock.elapsedRealtime();
        this.m_locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.m_locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MainActivity.LOC_UPDATE_DELAY, 0, new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				fillArray();
		     	latitude = location.getLatitude();
		     	longitude = location.getLongitude();
		     	if (onlyOnce){
		     	onlyOnce = false;
		     	CameraPosition cameraPosition = new CameraPosition.Builder().target(
			                    new LatLng(latitude, longitude)).zoom(12).build();
			     
			      	   googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		     	}
		     	   
		     	googleMap.clear();
		     	   
		     	redrawMarkers();
			}
			@Override
			public void onProviderDisabled(String provider) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
        });
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
    	/*locChangeList = (new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
            	if (SystemClock.elapsedRealtime() - lastTime < LOC_UPDATE_DELAY) {
            		return;
            	}
            	else {
            		lastTime = SystemClock.elapsedRealtime();
            	}
            	
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
    	});*/
    	
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
            	fillArray();
            }
        }));
        
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
			@Override
			public void onMapClick(LatLng coords) {
				lastMarker = null;
			}
        });
        
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
				initiateRateMarkerDialog(marker);
			}
        });
    }
    
    /** Resets the trail head markers. */
    private void redrawMarkers() {
    	fillArray();
    	float rating;
    	int numRatings;
    	Cursor c;
    	/** Location marker searching occurs here */
        for(int i = 0; i < locationMarkerList.size(); i++){
	        c = datasource.getAllTrails();
	        if(c.getCount() == 0){
	        	Log.w("tits", "didn't return anything");
	    		continue;
	    	}
	    	c.moveToFirst();
	    	while(!(c.getString(1).equals(locationMarkerList.get(i).getTitle()))){
	    		c.moveToNext();
	    	}
	    	rating = c.getFloat(2);
	    	numRatings = c.getInt(3);
     	   Location.distanceBetween(latitude, longitude, locationMarkerList.get(i).getPosition().latitude, locationMarkerList.get(i).getPosition().longitude, result);
	           	if((rating > .2 || numRatings < 10) && (result[0] / 1609.34) < viewDistance && Pattern.compile(Pattern.quote(searchString), Pattern.CASE_INSENSITIVE).matcher(locationMarkerList.get(i).getTitle()).find())
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
    	this.focusMarker = marker;
    	DialogFragment dialog = new RateMarkerDialog();
        dialog.show(getSupportFragmentManager(), "RateMarkerDialog");
    }

	@Override
	public void onRateMarkerClick(DialogFragment dialog, int choice) {
		Toast toast;
		switch (choice) {
			case AddMarkerDialog.RATE_GOOD_INDEX:
				datasource.updateRating(focusMarker.getTitle(), 1);
				toast = Toast.makeText(this, "Apply Good Rating.", Toast.LENGTH_SHORT);
				toast.show();
				break;
		
			case AddMarkerDialog.RATE_BAD_INDEX:
				datasource.updateRating(focusMarker.getTitle(), 0);
				toast = Toast.makeText(this, "Apply Bad Rating.", Toast.LENGTH_SHORT);
				toast.show();
				break;
			
			case AddMarkerDialog.DO_NOT_RATE_INDEX:
				toast = Toast.makeText(this, "Do not change rating.", Toast.LENGTH_SHORT);
				toast.show();
				break;
			
			default:
				return;
		}
		this.lastMarker = null;
		this.focusMarker.hideInfoWindow();
	}
}