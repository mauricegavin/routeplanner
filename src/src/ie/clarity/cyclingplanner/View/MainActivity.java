package ie.clarity.cyclingplanner.View;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.Controller.RecordingController;
import ie.clarity.cyclingplanner.Model.Trip;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The main activity is the application main screen.
 * @author Maurice Gavin
 *
 */
public class MainActivity extends MapActivity {
	
	// Declare controllers
	RecordingController recordCtrl = null;
	MapController mapCtrl = null;
	
	// Visibility Constants
	public static final int VISIBLE = 0;
	public static final int INVISIBLE = 4;
	public static final int GONE = 8;
	public boolean windowVisible = false;
	private boolean suspend = false;
	
	// Formatting
    final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    DecimalFormat twoDigitFormat = new DecimalFormat("00");
    
	// References to UI fields which are update-able
	TextView valTime = null;
	TextView valDist = null;
	TextView valSpeed = null;
	
	// Declare reference to control buttons
	protected ImageButton playButton;
	protected ImageButton pauseButton;
	protected ImageButton stopButton;
	
	// Map Components
	protected MapView map = null;
	protected List<Overlay> mapOverlays = null;
	protected ItemisedOverlayForMap itemisedoverlay = null;
	
	// GPS Notification
	Thread gpsMessageThread = null;
	Toast gpsMessage = null;
	
	// Calculating Speed
	protected int updateFrequency = 2500; // In milliseconds
	protected long lastSpeedUpdate = 0; // In milliseconds
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("FLOW","Main Activity: onCreate()");
		
		// Configure Recording Services
		//
		// TODO Lifecycle issues can arise with the server. Deal with this in the RecordingController
		recordCtrl = new RecordingController(this);
	    //
	    // Finished configuring recording
	    
		
		//map = new MapView(this, "0FOlkZbidlt8Z2aLAsQTHTrzXygxPne0d5tIkiQ");
		
		map = (MapView)findViewById(R.id.mapview);
		map.setBuiltInZoomControls(true); // Enable the on-screen controls for zooming in and out
		centreMapView(new GeoPoint((int) (53.347283 * 1E6), (int) (-6.259313 * 1E6))); // Centre's the map.
	
		// Create map overlays
		//
		mapOverlays = map.getOverlays();
		
		// Draw the current location arrow
		Drawable drawable = this.getResources().getDrawable(R.drawable.pointer);
		itemisedoverlay = new ItemisedOverlayForMap(drawable, this);
		
		// Example overlay
		GeoPoint arrow = new GeoPoint((int) (53.347283 * 1E6), (int) (-6.259313 * 1E6)); // 1E6 is microdegrees constant
		OverlayItem overlayitem = new OverlayItem(arrow, "Location", "Still calibrating...");
		itemisedoverlay.addOverlay(overlayitem);
		
		// Add the overlay to the MapView
		mapOverlays.add(itemisedoverlay);
		
		//
		// END OF OVERLAYS
		
		// Get references to stat TextViews
		valTime = (TextView)findViewById(R.id.value_timeelapsed);
		valDist = (TextView)findViewById(R.id.value_distance);
		valSpeed = (TextView)findViewById(R.id.value_speed);
		
		// Get reference to Control Buttons
		playButton = (ImageButton)findViewById(R.id.play);
		pauseButton = (ImageButton)findViewById(R.id.pause);
		stopButton = (ImageButton)findViewById(R.id.stop);
		
		// Configure Control Buttons		
		playButton.setVisibility(GONE);
		
		// Add Listeners to the control buttons
		playButton.setOnClickListener(new ButtonClickedListener(this, 1));
		pauseButton.setOnClickListener(new ButtonClickedListener(this, 2));
		stopButton.setOnClickListener(new ButtonClickedListener(this, 3));
		
		gpsMessage = Toast.makeText(this, "Awating GPS fix...", Toast.LENGTH_SHORT);
		showGPSMessage();
		
		windowVisible = true;
		
		// Deal with Lifecycle issues
		//cleanSharedMemory();
		// If the app is being re-created after the "Back" button was pressed then restore the session from sharedPrefs
		SharedPreferences savingState = getSharedPreferences("RecordState", MODE_PRIVATE);
		
		if(savingState.getBoolean("isRecording", true)); // Then the app is recording, and since this is the default initialisation (above) nothing further needs to be done.
		else // If the app is being created in a paused state some more things need to be configured
		{
			// The buttons need to be in the correct state.
			playButton.setVisibility(VISIBLE);
			pauseButton.setVisibility(GONE);
			
			hideGPSMessage(); // UI messages pertaining to GPS need to be hidden
			recordCtrl.pauseRecording(); // The recording controller needs to be told to pause.
		}
		
	}

	private void centreMapView(GeoPoint point) 
	{
		mapCtrl = map.getController();
		mapCtrl.setCenter(point);
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		Log.i("FLOW","Main Activity: onStop()");
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.i("FLOW","Main Activity: onStart()");
	}
	
	@Override
	protected void onDestroy()
	{
		if(recordCtrl != null) // recordCtrl is set to null by the STOP button.
		{ // This condition guards against trying to read a null object in this circumstance.
		// The block will execute if onDestroy() is called by something like pressing the "Back" button.
			
			// Store any data that needs to be saved between instances
			SharedPreferences savingState = getSharedPreferences("RecordState", MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = savingState.edit();
			prefEditor.putBoolean("isRecording", recordCtrl.isRecording()); // True indicates running, false indicates paused
			//TODO Might have to put time in here.
			prefEditor.commit();
		}
		
		super.onDestroy();
		Log.i("FLOW","Main Activity: onDestroy()");
		windowVisible = false;
		hideGPSMessage();
	}
	
	@Override
	protected void onPause() // We do not actually want to pause the application
	// This just means that it will now be running in the background so we want to temporarily disable all UI updates.
	{
		super.onPause();
		Log.i("FLOW","Main Activity: onPause()");
		windowVisible = false;
		if(recordCtrl != null) // Wrap this up to prevent Null Pointer Exception when the Main Activity is ended.
		// When the Activity is ended onPause() is called.
		{
			recordCtrl.cancelMonitorTimer(); // Needs to be called exclusively here instead of calling recordCtrl.pauseRecording().
			// As we want to disable the UI updates and not the recording service.
		}
		hideGPSMessage();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i("FLOW","Main Activity: onResume()");
		windowVisible = true;
		recordCtrl.startMonitorTimer(); // Needs to be called exclusively here instead of calling recordCtrl.resumeRecording().
		// As we want to enable the UI updates and not the recording service (as the recording service is almost certainly already running).
		if(recordCtrl.getRecordingState()) //Use this condition because if the Recording is currently Paused we do not want to override the state of 'suspend' by calling showGPSMessage().
		{
			showGPSMessage();
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void showGPSMessage()
	{
		if(windowVisible)
		{
			suspend = false;
			noGPSSignalMessage();
		}
	}
	
	/**
	 * This function will make the program hide the "Awaiting GPS fix..." message, regardless of the current state.
	 */
	protected void hideGPSMessage()
	{
		suspend = true; // Worker threads will end themselves when they see suspend set to true
		gpsMessage.cancel();
	}
	
	/**
	 * The update function updates the UI to reflect the most recent changes.
	 * @param trip The latest copy of the currently recording Trip.
	 * @param location The latest Location update.
	 */
	public void update(Trip trip, Location location)
	{		
		// A GPS fix has been acquired so get rid of the message
		hideGPSMessage();
		
		// Time is updated by the Timer
		valDist.setText(decimalFormat.format(trip.getDistance()/1000) + " km");
		// Calculate the new speed, only update every few seconds
		if( (System.currentTimeMillis() - lastSpeedUpdate) > updateFrequency ) // If the time since the last speed UI update exceeds the update frequency then update it.
		{	
			valSpeed.setText(decimalFormat.format(location.getSpeed()*3.6) + " km/hr");	// Convert from [m/s]*3.6 = [km/hr]
			lastSpeedUpdate = System.currentTimeMillis();
		}
		
		// Update the map, centre the map on the current location.
		MapView map = (MapView)findViewById(R.id.mapview);
		
        // Centre the map
		Location current = trip.getGeoData().getMostRecentPoint();
		GeoPoint arrow = new GeoPoint((int) (current.getLatitude()* 1E6), (int) (current.getLongitude() * 1E6));
		MapController mapCtrl = map.getController();
		mapCtrl.animateTo(arrow);
		
		// Update the position of the arrow
		OverlayItem overlayitem = new OverlayItem(arrow, "Location", current.getLatitude() + ", " + current.getLongitude());
		itemisedoverlay.clear();
		itemisedoverlay.addOverlay(overlayitem);
		mapOverlays.clear();
		mapOverlays.add(itemisedoverlay);
	
		//int latcenter = (53 + 54) / 2;
		//int lgtcenter = (-6 + -5) / 2;
		//GeoPoint center = new GeoPoint(latcenter, lgtcenter);
		//MapController mc = map.getController();
		//mc.animateTo(center);
		// Add 500 to map span, to guarantee pins fit on map
		//mc.zoomToSpan(500+53 - 54, 500-6 + 5);

		//if (gpspoints == null) {
			//AddPointsToMapLayerTask maptask = new AddPointsToMapLayerTask();
			//maptask.execute(trip);
		//} else {
		//	mapOverlays.add(gpspoints);
		//}
	}

	/**
	 * This function removes all data that is used to restore the state.
	 * It should be called when the user is "Finishing" their trip.
	 */
	private void cleanSharedMemory()
	{
   		// Wipe RecordState data from sharedPreferences
   		SharedPreferences savingState = getSharedPreferences("RecordState", MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = savingState.edit();
		prefEditor.clear();
		prefEditor.commit();
	}
	
	/**
	 * The ButtonListener is used to handle button clicks on the control buttons on the bottom of the screen.
	 * 
	 * Numbers 1-3 are used to identify buttons
	 * Resume = 1
	 * Pause = 2
	 * Stop = 3
	 * 
	 * @author Maurice Gavin
	 */
	private class ButtonClickedListener implements OnClickListener
	{
		private int buttonID = -1;
		private Context parentContext = null;
		
		ButtonClickedListener(Context parent, int id)
		{
			parentContext = parent;
			buttonID = id;
		}
		
		@Override
		public void onClick(View v) 
		{
			switch(buttonID)
			{
			case 1:	// Play Button has been pressed
			{		
				playButton.setVisibility(GONE);
				pauseButton.setVisibility(VISIBLE);
				
				Toast message = Toast.makeText(parentContext, "Recording Resumed", Toast.LENGTH_LONG);
				message.show();
						
				showGPSMessage();
				Toast gpsMessage = Toast.makeText(parentContext, "Awating GPS fix...", Toast.LENGTH_SHORT);
				
				recordCtrl.resumeRecording();
				cleanSharedMemory();
			}
				break;
			case 2:	// Pause Button has been pressed
			{
				playButton.setVisibility(VISIBLE);
				pauseButton.setVisibility(GONE);	
				
				hideGPSMessage();
				Toast message = Toast.makeText(parentContext, "Recording Paused", Toast.LENGTH_LONG);
				message.show();
				
				recordCtrl.pauseRecording();
			}
				break;
			case 3: // If the user wants to stop recording
			{
				// Confirm that they want to end.
				AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
				builder.setMessage("Are you finished cycling?");
				builder.setCancelable(false);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
				           	public void onClick(DialogInterface dialog, int id)
				           	{
								hideGPSMessage();
				           		
				           		recordCtrl.finishRecording();
				           		recordCtrl = null;	// Get rid of the recordCtrl
				           		mapCtrl = null;
				           		
				           		// Wipe RecordState data from sharedPreferences
				           		SharedPreferences savingState = getSharedPreferences("RecordState", MODE_PRIVATE);
				        		SharedPreferences.Editor prefEditor = savingState.edit();
				        		prefEditor.clear();
				        		prefEditor.commit();
				           		
				           		startActivity(new Intent(MainActivity.this, FinishStatsActivity.class));
				           		MainActivity.this.finish();
				           	}
						});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
				           public void onClick(DialogInterface dialog, int id)
				           {
				                dialog.cancel();
				           }
				       });
				AlertDialog confirm = builder.create();
				confirm.show();
			}
				break;
			}
		}
	}

	public void updateUITimer(double timeElapsed) 
	{
		if (windowVisible == true)
		{
			int hours = (int)timeElapsed/3600000; // Find how many hours there are
			timeElapsed = timeElapsed%3600000;
			int minutes = (int)timeElapsed/60000; // Find the number of minutes
			timeElapsed = timeElapsed%60000;
			int seconds = (int)timeElapsed/1000; // Find out how many seconds there are
			valTime.setText(twoDigitFormat.format(hours) + ":" + twoDigitFormat.format(minutes) + ":" + twoDigitFormat.format(seconds));
		}
		// Otherwise the UI is hidden and there is no point in writting text
	}
	
	public void noGPSSignalMessage() 
	{
		if( (gpsMessageThread != null) && (gpsMessageThread.isAlive()) )
		{
			// The thread is already running, no need to make a new one.
			Log.i("THREAD", "GPS Message Thread Lives, no need to make another.");
			// This condition is also important to stop the underlying controller from
			// trying to display the same message. 
			// (The timer that says that a GPS fix hasn't been received recently calls this function)
		}
		else // Begin a new thread
		{
			Log.i("THREAD", "GPS Message Thread Created");
			gpsMessageThread = new Thread() {
	            public void run() {
	                try {
	                	// Since Java seems incapable of ending its own threads the below will suspend it with a null pointer exception when MainActivity is closed.
	                    while (suspend == false) {
	                    	gpsMessage.show();
	                        sleep(1850);
	                        Log.i("THREAD", "I'm a big spaz and am still running!");
	                    }
	                } catch (Exception e) {
	                    Log.e("GPS", "No GPS Fix Message", e);
	                }
	                Log.i("THREAD", "GPS Message Thread Died");
	            }
	        };
	        gpsMessageThread.start();
		}
	}


	
	
	
	
	
	
	
	
	
	
	
	
	// Overlay TESTING
	

	
	
	
	
}
