package ie.clarity.cyclingplanner.View;

import java.util.ArrayList;
import java.util.List;

import ie.clarity.cyclingplanner.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SelectCoordsActivity extends MapActivity 
{
	// Constants
	private final int SET_START = 1;
	private final int SET_END = 2;
	
	// Variables
	private int mode = 0;
	
	// UI Helper Variables
	protected boolean firstTouch = true; // FirstTouch this run of the Activity, not ever.
	protected PinItemisedOverlay tempBackup = null;
	
	// Components
	private MapView map = null;
	private ToggleButton startButton = null;
	private ToggleButton endButton = null;
	
	// Map Components
	List<Overlay> mapOverlays = null;
	PinItemisedOverlay itemisedOverlay = null;
	
	// Time variables
	private long lastTouch = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_coords);
		
		map = (MapView)findViewById(R.id.coord_mapview);
		map.setBuiltInZoomControls(true);
		map.setClickable(true);
		
		// Get a reference to the two buttons
		startButton = (ToggleButton)findViewById(R.id.coordStart);
		endButton = (ToggleButton)findViewById(R.id.coordEnd);
		
		// Set button states
		startButton.setChecked(true);
		endButton.setChecked(false);
		
		// Add Button Listeners
		startButton.setOnClickListener(new CoordButtonListener(this, 1));
		endButton.setOnClickListener(new CoordButtonListener(this, 2));
		
		// Initialise mode to SET_START
		mode = SET_START;
		// Set firstTouch to true
		firstTouch = true;
		
		// Add an itemised overlay to the map
		mapOverlays = map.getOverlays();
		fill();
		
		Drawable drawable = null;
		drawable = getResources().getDrawable(R.drawable.pin_green);
		itemisedOverlay = new PinItemisedOverlay(drawable, this, 1);
		
		mapOverlays.add(itemisedOverlay);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		firstTouch = true;
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if(tempBackup != null){
			restoreRemoved();
		}
	}
	
	/**
	 * This function places markers on the screen on creation.
	 * If this screen has already has had markers placed and it is being re-launched then we need to show the old markers.
	 */
	private void fill() {
		SharedPreferences markers = getSharedPreferences("Coords", MODE_PRIVATE);
		float startLatitude = markers.getFloat("StartLatitude", -1);
		float startLongitude = markers.getFloat("StartLongitude", -1);
		float endLatitude = markers.getFloat("EndLatitude", -1);
		float endLongitude = markers.getFloat("EndLongitude", -1);
		
		if(startLatitude > 0)
		{
			Drawable drawable = getResources().getDrawable(R.drawable.pin_green);
			itemisedOverlay = new PinItemisedOverlay(drawable, this, 1);
			int startLat = (int)(startLatitude*1E6);
			int startLong = (int)(startLongitude*1E6);
			GeoPoint point = new GeoPoint(startLat, startLong);
			itemisedOverlay.addOverlay(new OverlayItem(point, "Location", "Start Point"));
			mapOverlays.add(itemisedOverlay); // Add this itemisedOverlay
		}
		if(endLatitude > 0)
		{
			Drawable drawable = getResources().getDrawable(R.drawable.pin_purple);
			itemisedOverlay = new PinItemisedOverlay(drawable, this, 2);
			int endLat = (int)(endLatitude*1E6);
			int endLong = (int)(endLongitude*1E6);
			GeoPoint point = new GeoPoint(endLat, endLong);
			itemisedOverlay.addOverlay(new OverlayItem(point, "Location", "End Point"));
			mapOverlays.add(itemisedOverlay); // Add this itemisedOverlay
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public class PinItemisedOverlay extends ItemizedOverlay
	{
		private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
		private Context context = null;
		private int type = -1;
		
		public PinItemisedOverlay(Drawable defaultMarker, Context context, int type) {
			super(boundCenter(defaultMarker)); // Must bind overlay, else nothing is drawn. Took me an hour to figure this out!
			this.context = context;
			this.type = type;
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}

		@Override
		public int size() {
			return overlays.size();
		}
		
		public void addOverlay(OverlayItem overlay) {
		    overlays.add(overlay);
		    populate();
		}
		
		public int getType() {
		    return type;
		}
		
		@Override
	    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
		{
			// Don't draw the shadow layer
		    if (!shadow) {
		        super.draw(canvas, mapView, shadow);
		    }
	    }
		
		public void clear()
		{
			overlays.clear();
		}
		
		@Override
		 public boolean onTouchEvent(MotionEvent event, MapView mapView) 
		 {  
			// The program seems to stutter if the user touches the screen rapidly.
			// The below condition should help alleviate the problem.
			if(System.currentTimeMillis() - lastTouch > 500)
			{
		         // when user lifts finger
		         if (event.getAction() == 0)
		         {   
		 			if(firstTouch) { // This boolean condition prevents duplicate markers appearing on the app when the start and end point screen is being revisited.
						firstTouch = false;
						removeExisting();
					}
		 			
		        	 lastTouch = System.currentTimeMillis();
		        	 
		        	 GeoPoint p = mapView.getProjection().fromPixels(
		        			 (int) event.getX(),
		        			 (int) event.getY());
			        storeLocation(p.getLatitudeE6(), p.getLongitudeE6()); // Put the new coord into the Shared Prefs
		        	 
			        /*Toast.makeText(context, 
		        			 p.getLatitudeE6() / 1E6 + ", " + 
		        			 p.getLongitudeE6() /1E6 , 
		        			 Toast.LENGTH_SHORT).show();
		        	*/
			        
			        // Set tempBackup to null, it acts as a flag to indicate that a change has been made and it doesn't need to be restored.
					tempBackup = null;
					
					// Add the new point
					GeoPoint point = new GeoPoint(p.getLatitudeE6(), p.getLongitudeE6());
		        	removeExistingMarkers();
					OverlayItem overlayitem = new OverlayItem(point, "Location", ""+mode);
					addOverlay(overlayitem);
					mapOverlays.add(this);
		         } 
			}
	        return false;
	     }
		
		/**
		 * Remove any existing start/end markers, whichever applies
		 */
		private void removeExistingMarkers() 
		{
			overlays.clear();		
		}
	}
	
	/**
	 * Store the co-ordinates in the shared preferences
	 * @param latitude The longitude to be stored
	 * @param longitude The latitude to be stored
	 */
	private void storeLocation(float latitude, float longitude)
	{
		latitude /= 1E6;
		longitude /= 1E6;
		SharedPreferences sharedStorage = getSharedPreferences("Coords", MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedStorage.edit();
		switch(mode)
		{
		case 1:
			prefEditor.putFloat("StartLatitude", latitude);
			prefEditor.putFloat("StartLongitude", longitude);
			break;
		case 2:
			prefEditor.putFloat("EndLatitude", latitude);
			prefEditor.putFloat("EndLongitude", longitude);
			break;
		}
		prefEditor.commit();
		//Log.i("DEBUG", "This is " + sharedStorage.getFloat("StartLatitude", -1));
	}
	
	/**
	 * Removes existing itemisedOverlays of the same type from the map overlay
	 */
	private void removeExisting()
	{	
		int i = 0;
		PinItemisedOverlay extraBackup = null;
		// If this function is called from an onTouch event then we need to preserve the overlay that is running already.
		if( mapOverlays.size() == 3) {
			extraBackup = (PinItemisedOverlay)(mapOverlays.get(2));
		}
		
		if(tempBackup == null) // In a case where the new tempBackup would be overwriting another tempBackup
			// the overlay that would otherwise become the new tempBackup is actually empty due to the extraBackup clause above.
		{
			// Backup the overlay that is to be removed
			while(i < mapOverlays.size()) {
				if( ((PinItemisedOverlay)(mapOverlays.get(i))).type == mode ) {
					tempBackup = (PinItemisedOverlay)(mapOverlays.get(i));
					mapOverlays.remove(i);
					break;
				}
				i++;
			}
		}
		
		i = 0;
		PinItemisedOverlay tempWorker = null;
		while(i < mapOverlays.size())
		{
			// If the overlay that we are inspecting is of type corresponding to the current
			// mode then we know that it is an old overlay, and so remove it.
			if( ((PinItemisedOverlay)(mapOverlays.get(i))).type != mode )
			{
				tempWorker = (PinItemisedOverlay)(mapOverlays.get(i));
				break;
			}
			i++;
		}
		mapOverlays.clear(); // Clear the list
		mapOverlays.add(tempWorker); // Add the other point back in
		if(extraBackup != null){
			mapOverlays.add(extraBackup);
		}
	}
	
	/**
	 * This function restores a temporary overlay which may need to be restored if nothing else is placed.
	 * The opposite of the removeExisting() function
	 */
	private void restoreRemoved()
	{
		mapOverlays.add(tempBackup);
		tempBackup = null;
	}
	
	// Listeners
	
	/**
	 * Listener for the toggle-able coordinate selection buttons.
	 * Sets the mode. i.e. which point you are setting.
	 */
	private class CoordButtonListener implements OnClickListener
	{
		Context context = null;
		int i = -1;
		
		CoordButtonListener(Context context, int i)
		{
			this.context = context;
			this.i = i;
		}
		
		@Override
		public void onClick(View arg0) 
		{	
			firstTouch = false;
			
			if(i == mode) { // If i == mode then this button is already toggled and we don't need to create a new drawing layer.
				// In fact doing so also calls remove existing which would cause a bug if the user pressed the toggle button more than once, then decided not to place something and instead place a pin of another type.
				getButton(i).setChecked(true); // Finally ensure that the button is not turned off when clicked.
				return;
			}
		
			if(tempBackup != null) // If tempBackup has a value when the toggle button is pressed then it means that no markers have been placed since it was last pressed
				// In this case we will want to restore the old marker to the overlay
			{
				restoreRemoved();
			}
			
			Drawable drawable = null;
			mapOverlays = map.getOverlays();
			
			switch(i)
			{
			case 1:
				mode = SET_START;
				switchButtons(1);
				
				drawable = context.getResources().getDrawable(R.drawable.pin_green);
				itemisedOverlay = new PinItemisedOverlay(drawable, context, 1);
				break;
			case 2:
				mode = SET_END;
				switchButtons(2);
				
				drawable = context.getResources().getDrawable(R.drawable.pin_purple);
				itemisedOverlay = new PinItemisedOverlay(drawable, context, 2);
				break;
			}
			removeExisting();
			mapOverlays.add(itemisedOverlay);	
		}
		
		/**
		 * Toggles the state of the buttons.
		 */
		private void switchButtons(int i)
		{
			if(i == 1) {
				startButton.setChecked(true);
				endButton.setChecked(false);
			}
			else if(i == 2){
				startButton.setChecked(false);
				endButton.setChecked(true);		
			}
		}
		
		private ToggleButton getButton(int i)
		{
			if (i == 1) return startButton;
			else if (i == 2) return endButton;
			return null;
		}
		
	}
		
}

