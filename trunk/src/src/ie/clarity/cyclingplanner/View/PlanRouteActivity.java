package ie.clarity.cyclingplanner.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.MenuActivity;
import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.R.color;
import ie.clarity.cyclingplanner.Controller.GPSController;
import ie.clarity.cyclingplanner.Controller.RecordingController;
import ie.clarity.cyclingplanner.Model.PersonalisedRoute;
import ie.clarity.cyclingplanner.Model.RecordingService;
import ie.clarity.cyclingplanner.Model.Route;
import ie.clarity.cyclingplanner.Model.Trip;

public class PlanRouteActivity extends MenuActivity
{
	private String reason = null;
	private String type = null;
	private int reasonState = GONE;
	private int typeState = GONE;
	private ProgressDialog progressDialogue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan_route);
	
		reset();
		
		// Make screen scroll-able
		LinearLayout layout = (LinearLayout)findViewById(R.id.planRouteScreen);
		layout.setScrollContainer(true);
		layout.setVerticalScrollBarEnabled(true);

		// Set Coordinates buttons visibility
		LinearLayout coordsLayout = (LinearLayout)findViewById(R.id.coordinatesLayout);
		coordsLayout.setVisibility(VISIBLE);
		
		// Set Coordinates buttons properties
		Button startPointButton = (Button)findViewById(R.id.coordsPointButton);
		startPointButton.setOnClickListener(new CoordButtonListener());
		
		// Set TextFields invisible initially
		LinearLayout reasonLayout = (LinearLayout)findViewById(R.id.reasonOtherLayout);
		reasonLayout.setVisibility(reasonState);
		LinearLayout typeLayout = (LinearLayout)findViewById(R.id.typeOtherLayout);
		typeLayout.setVisibility(typeState);
		
		// Add Listeners to the TextFields
		AutoCompleteTextView reasonText = (AutoCompleteTextView)findViewById(R.id.reasonOtherText);
		reasonText.addTextChangedListener(new OtherFieldChangedListener());
		
		// Setup the Reason Spinner
		Spinner reasonSpinner = (Spinner)findViewById(R.id.reasonSpinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.reason_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    reasonSpinner.setAdapter(adapter);
	    // Add listener
	    reasonSpinner.setOnItemSelectedListener(new SpinnerItemSelectedListener(1));
	    
		// Setup the Type Spinner
		Spinner typeSpinner = (Spinner)findViewById(R.id.typeSpinner);
	    adapter = ArrayAdapter.createFromResource(
	            this, R.array.type_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    typeSpinner.setAdapter(adapter);
	    // Add listener
	    typeSpinner.setOnItemSelectedListener(new SpinnerItemSelectedListener(2));
		
	    // Setup the Start button
	    Button startButton = (Button)findViewById(R.id.calculateButton);
	    startButton.setOnClickListener(new ButtonClickedListner(this));
	
	}
	
	/**
	 * Clear any data that might be left from a previous trip.
	 * Get the program ready to record again.
	 */
	private void reset() 
	{
		reason = null;
		type = null;
		reasonState = GONE;
		typeState = GONE;
		progressDialogue = null;
		
		setTrip(null);
		personalisedRoute = null;
		recordCtrl = null;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	
	
	//
	// LISTENERS
	//
	
	/**
	 * This listener is for the coordinate buttons.
	 * Launches a screen to set the coordinates.
	 * 
	 * Mode 1: Set the Start Point
	 */
	private class CoordButtonListener implements OnClickListener
	{
		@Override
		public void onClick(View arg0) 
		{
			startActivity(new Intent(PlanRouteActivity.this, SelectCoordsActivity.class));
		}
	}
	
	/**
	 * This private class of PlanRouteActivity handles the selections on the Reason & Type Spinners
	 * When an item is selected from the dropdown menu it is stored globally so that it can be used later.
	 * 
	 * @author Maurice Gavin
	 */
	private class SpinnerItemSelectedListener implements OnItemSelectedListener 
	{
		private int mode;	// Defines whether it is the Reason/Type Spinner
		
		SpinnerItemSelectedListener(int mode)
		{
			this.mode = mode;
		}
		
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
	    {
	    	String text = parent.getItemAtPosition(pos).toString();

	    	if (mode == 1)
	    	{
		    	LinearLayout reasonLayout = (LinearLayout)findViewById(R.id.reasonOtherLayout);
		    		
		    	// If the user selects Other we need to read from the text field instead
		    	if(text.compareTo("Other") == 0) // The strings are equal, Other is selected
		    	{
		    		reasonState = VISIBLE;
		    		reasonLayout.setVisibility(reasonState);
		    		// Read from text box later instead
		    		setReason(null);
		    	}
		    	else
		    	{
		    		// Stores the reason in a string
		    		setReason(text);
		    		reasonState = GONE;
		    		reasonLayout.setVisibility(reasonState);
		    	}
	    	}
	    	else if (mode == 2)
	    	{
	    		// Store the selected item
	    		setType(text);
	    	}
	    }

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	    
	}

	/**
	 * If a text box is edited it we assume that the user is adding a custom Reason
	 * @author Maurice Gavin
	 *
	 */
	private class OtherFieldChangedListener implements TextWatcher
	{		
		@Override
		public void afterTextChanged(Editable s) {
			setReason(s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}
	}
	
	/**
	 * This class is primarily a listener for the Start button at the bottom of the screen.
	 * Its primary responsibilities include:
	 * - Starting the MainActivity; 
	 * - Ending the current Activity;
	 * - Creating a PersonalisedRoute object.
	 *  
	 * @author Maurice Gavin
	 */
	private class ButtonClickedListner implements OnClickListener
	{
		
		private Context context = null;
		
		ButtonClickedListner(Context context)
		{
			this.context = context;
		}
		
		@Override
		public void onClick(View v) 
		{
			
			final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) 
		    {
		        noGPSNotification();
		    } 
		    else 
		    {
		    	// Show loading message
		    	ProgressMessage progressMessage = new ProgressMessage(PlanRouteActivity.this);
		    	
				// Create the PersonalisedRoute object
				personalisedRoute = new PersonalisedRoute();
				personalisedRoute.setReason(reason);
				personalisedRoute.setType(type);
	
				Log.i("TRIP", "Reason: " + personalisedRoute.getReason());
				Log.i("TRIP", "Type: " + personalisedRoute.getType());
				
				startActivity(new Intent(PlanRouteActivity.this, MainActivity.class));
				PlanRouteActivity.this.finish();	// Perhaps don't finish this. It is a useful screen to go back to.
		    }			
		}
	}
	
	/**
	 * Informs the user that they have GPS connectivity
	 */
	private void noGPSNotification() 
	{
		 final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage("Your phone's GPS is disabled. GPS is required to determine your location.\n")
	               .setCancelable(false)
	               .setPositiveButton("GPS Settings...", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {                	   
	                	   Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
	                	   startActivity(myIntent);
	                   }
	               })
	               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) {
	                        dialog.cancel();
	                   }
	               });
	        final AlertDialog alert = builder.create();
	        alert.show();
	}
	
	
	private Handler handler = new Handler() 
	{		
		public void handleMessage() {			
			progressDialogue.dismiss();
		}
	};
	
	private class ProgressMessage extends Thread
	{
		ProgressMessage(Context context)
		{
			progressDialogue = ProgressDialog.show(context, "", "Loading...", true,
					false);

			Thread thread = new Thread(this);
			thread.start();
		}
		
		public void run()
		{			
			handler.sendEmptyMessage(0);
		}

	}
	
}
