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

import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.Controller.RecordingController;
import ie.clarity.cyclingplanner.Model.Trip;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	// Note on GeoPoint: 1E6 is microdegrees constant
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("FLOW","Main Activity Created");
		
		// Configure Recording Services
		//
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
		
		Drawable drawable = this.getResources().getDrawable(R.drawable.point);
		itemisedoverlay = new ItemisedOverlayForMap(drawable, this);
	
		// Example overlay
		GeoPoint point = new GeoPoint((int) (53.347283 * 1E6), (int) (-6.259313 * 1E6)); // 1E6 is microdegrees constant
		OverlayItem overlayitem = new OverlayItem(point, null, "This is your current location.");
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
		
		Toast message = Toast.makeText(this, "Awating GPS fix...", Toast.LENGTH_SHORT);
		noGPSSignalMessage(message);
		
		windowVisible = true;
	}

	private void centreMapView(GeoPoint point) 
	{
		mapCtrl = map.getController();
		mapCtrl.setCenter(point);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Log.i("FLOW","Main Activity Paused");
		windowVisible = false;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i("FLOW","Main Activity Resumed");
		windowVisible = true;
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * The update function updates the UI to reflect the most recent changes.
	 * @param trip The latest copy of the currently recording Trip.
	 */
	public void update(Trip trip, Location location)
	{		
		// Time is updated by the Timer
		valDist.setText(decimalFormat.format(trip.getDistance()/1000) + " km");
		valSpeed.setText(decimalFormat.format(location.getSpeed()*3.6) + " km/hr");	// Convert from [m/s]*3.6 = [km/hr]
		
		// Update the map, centre the map on the current location.
		MapView map = (MapView)findViewById(R.id.mapview);
		
        // Centre the map
		Location current = trip.getGeoData().getMostRecentPoint();
		GeoPoint point = new GeoPoint((int) (current.getLatitude()* 1E6), (int) (current.getLongitude() * 1E6));
		MapController mapCtrl = map.getController();
		mapCtrl.animateTo(point);
		
		// Update the position of the arrow
		OverlayItem overlayitem = new OverlayItem(point, null, "This is your current location.");
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
						
				suspend = false;
				Toast gpsMessage = Toast.makeText(parentContext, "Awating GPS fix...", Toast.LENGTH_SHORT);
				noGPSSignalMessage(message);
				
				recordCtrl.resumeRecording();
			}
				break;
			case 2:	// Pause Button has been pressed
			{
				playButton.setVisibility(VISIBLE);
				pauseButton.setVisibility(GONE);	
				
				Toast message = Toast.makeText(parentContext, "Recording Paused", Toast.LENGTH_LONG);
				message.show();
				
				suspend = true;
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
				           		suspend = true;	// Worker threads will end themselves when they see suspend set to true
				           		
				           		recordCtrl.finishRecording();
				           		recordCtrl = null;	// Get rid of the recordCtrl
				           		mapCtrl = null;
				           		
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
	

	private void noGPSSignalMessage(final Toast message) 
	{
		Thread gpsMessageThread = new Thread() {
	            public void run() {
	                try {
	                    while ((recordCtrl.getNumberNodes() <= 0) && (suspend == false)) {
	                    	message.show();
	                        sleep(1850);
	                    }
	                } catch (Exception e) {
	                    Log.e("GPS", "No GPS Fix Message", e);
	                }
	            }
	        };
	        gpsMessageThread.start();
	}


	
	
	
	
	
	
	
	
	
	
	
	
	// Overlay TESTING
	

	
	
	
	
}
