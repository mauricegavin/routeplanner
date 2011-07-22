package ie.clarity.cyclingplanner.Controller;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.Model.RecordingService;
import ie.clarity.cyclingplanner.Model.Route;
import ie.clarity.cyclingplanner.Model.Trip;
import ie.clarity.cyclingplanner.View.FinishStatsActivity;
import ie.clarity.cyclingplanner.View.MainActivity;

public class RecordingController extends DefaultActivity
{
	private static MainActivity context = null;
	protected ServiceConnection connection = null;
	protected Intent rService = null;
	protected IRecordService rs = null;
	protected Timer timer = null;
	 
	private boolean isRecording = false;
	
	private float numberNodes = 0;
	private float sumSpeed = 0;
	private float sumPace = 0;
	
	// Use the Timer to periodically check when the last GPS fix was received.
	protected long lastUpdate = -1;
	protected final long MONITOR_DELAY = 3000; // The delay between checking last GPS update
	protected Timer monitorTimer = null;
	
	public RecordingController(MainActivity context) 
	{
		this.context = context;
		setTrip(new Trip(personalisedRoute));
		start();
	}

	/**
	 * Start recording the trip
	 */
	private void start()
	{		
		final RecordingController refController = RecordingController.this;
		
		rService = new Intent(context, RecordingService.class);
		context.startService(rService);
		
		connection = new ServiceConnection()
		{
				public void onServiceDisconnected(ComponentName name) {
					Log.i("RECORD", "Recording Service Disconnected");
				}
				
				public void onServiceConnected(ComponentName name, IBinder service) 
				{
					Log.i("RECORD", "Recording Service connected");
					rs = (IRecordService) service;
					rs.setListener(refController); // Pass in the reference to the Controller before starting the RecordingService.
					
					switch (rs.getState()) {
						case RecordingService.STATE_IDLE:
							rs.start(getTrip());
							isRecording = true;
							Log.i("RECORD", "State: "+rs.getState() + " Is Recording: " + isRecording);
							((MainActivity)(context)).setTitle("Róthim - Recording...");
							break;
						case RecordingService.STATE_RECORDING:
							isRecording = true;
							Log.i("RECORD", "State: "+rs.getState() + " Is Recording: " + isRecording);
							((MainActivity)(context)).setTitle("Róthim - Resumed...");
							break;
						case RecordingService.STATE_PAUSED:
							isRecording = false;
							Log.i("RECORD", "State: "+rs.getState() + " Is Recording: " + isRecording);
							((MainActivity)(context)).setTitle("Róthim - Paused...");
							break;
					}
					rs.setListener(RecordingController.this);
					//context.unbindService(this);
				}
					
			};

		if(context.bindService(rService, connection, BIND_AUTO_CREATE)) {
			Log.i("GPS", "Recording Service is bound to Controller");
		}
		else {
			Log.e("GPS", "Failed to bind Recording Service to Controller");
		}
	}
	
	/**
	 * The update function is called by objects that the Controller is listening to.
	 * Its called when new trip data has become available and needs to be stored.
	 */
	public void update(Location location, Trip trip)
	{
		// Check the location's accuracy first to decide if we will keep this point at all.
		//if(location.getAccuracy() > 40) // Accuracy is in meters
		//{
		numberNodes = trip.getGeoData().getPathTaken().size();
		if(numberNodes == 0) // If this is the very first location fix
		{
			trip.setStartTime(System.currentTimeMillis()); // Sets the true start time
			trip.setEndTime(System.currentTimeMillis());
			enableUIUpdates(); // Begin the trip timer.
		}

		// Update the distance
		if (trip.getGeoData().getMostRecentPoint() == null)
		{
			// Schedule updates on GPS update frequency
			startMonitorTimer();
		}
		else if(trip.getGeoData().getMostRecentPoint() != null)	// This will be true when the first co-ordinate is received.
		{
			if(location.getSpeed() > 1)	// If speed of travel is < 1m/s ignore it. Person is probably stationary and the distance travelled would be just location error.
				trip.setDistance( trip.getDistance() + trip.getGeoData().getMostRecentPoint().distanceTo(location) ); // Add the distanceTravlled to the latest segment distance for the new total
		}
		Route goegraphicRoute = trip.getGeoData();
		goegraphicRoute.addNode(location);
		
		// Update average and maximum values
		sumSpeed = sumSpeed + location.getSpeed();
		trip.setAverageSpeed( sumSpeed/numberNodes );	// Calculate the new Average Speed
		if(location.getSpeed() > trip.getMaxSpeed())
		{
			trip.setMaxSpeed(location.getSpeed());
		}
			
		// Update the time elapsed
		trip.setEndTime(System.currentTimeMillis());
		lastUpdate = System.currentTimeMillis();
		
		setTrip(trip);
		
		// Notify the MainActivity of the changes so that it can update the UI if necessary.
		context.update(trip, location);
	}
	
	/**
	 * Check when the last GPS fix was received.
	 * If the time exceeds a certain limit inform the user
	 * @author Maurice Gavin
	 */
	protected class UpdateMonitor extends TimerTask
	{		
		public void run()
		{
			if( (isRecording) && ((System.currentTimeMillis() - lastUpdate) > 6500) ) // If the time since the last update exceeds 6.5 seconds
			{
				Log.e("GPS", "Its been a while since the last GPS update");
				context.showGPSMessage();
			}
		}
	};
	
	/**
	 * This functions schedules a timer to run which will periodically check how much time has elapsed since the last GPS Location update.
	 */
	public void startMonitorTimer()
	{
		if(monitorTimer == null) // This should stop the timer from creating duplicates while it is still running.
		{
			Log.i("TIMER", "Monitor Timer started/resumed.");
			monitorTimer = new Timer();
			monitorTimer.scheduleAtFixedRate(new UpdateMonitor(), 0, MONITOR_DELAY);
		}
	}
	
	/**
	 * Call this function whenever you want to stop checking the time elapsed between GPS updates.
	 * This will have the effect of stopping updates to the UI.
	 * It disables the timer from updating but doesn't affect the GPS recording.
	 */
	public void cancelMonitorTimer()
	{
		if(monitorTimer != null) { // It is necessary to protect the below statement as the timer may not yet have been initialised. (Initialised when first GPS fix is received)
			monitorTimer.cancel(); // Cancel looking to see when the most recent GPS fix was.
			monitorTimer.purge(); 
			Log.i("TIMER", "Monitor Timer cancelled.");
		}
	}
	
	/**
	 * Resume Recording
	 * Enable GPS updates again.
	 */
	public void resumeRecording()
	{
		isRecording = true;
		Log.i("RECORD", "Resume Recording");
		
		double lengthPaused = System.currentTimeMillis() - getTrip().getPauseStartTime();
		getTrip().setTimePaused( getTrip().getTimePaused() + lengthPaused ); // Append the length of the latest pause to the total paused time
		
		startMonitorTimer();
		
		setListener(); // Resumes RS
	}
	
	/**
	 * Pause the Recording.
	 * Disable GPS updates for now.
	 */
	public void pauseRecording()
	{
		isRecording = false;
		Log.i("RECORD", "Pause Recording");
		
		getTrip().setPauseStartTime(System.currentTimeMillis()); // Record the time at which the pause occurred.
		
		cancelMonitorTimer();
		
		setListener(); // Pauses RS
	}
	
	/**
	 * Can be called to query whether or not the app is currently recording data.
	 * @return The state of recording, either true or false
	 */
	public boolean isRecording()
	{
		return isRecording;
	}
	
	// Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();
    final Runnable mUpdateTimer = new Runnable() {
        public void run() {
            updateTimer();
        }

		private void updateTimer() 
		{
			if(isRecording == false)
				Log.d("TIMER", "isRecording == false");
			if(getTrip() == null)
				Log.d("TIMER", "Trip is null");
			if ((getTrip() != null) && isRecording) 
			{
				Log.d("TIMER", "Updating Timer");
				getTrip().setEndTime(System.currentTimeMillis());
	            double timeElapsed = getTrip().getEndTime() - getTrip().getStartTime() - getTrip().getTimePaused();
	            context.updateUITimer(timeElapsed);
	        }	
		}
    };
   
	private void setListener() 
	{
		connection = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {}
			public void onServiceConnected(ComponentName name, IBinder service) 
			{
				rs = (IRecordService) service;
				if (isRecording) {
					rs.resume();
				} else {
					rs.pause();
				}
				context.unbindService(connection); // Needs to unbind service else multiple services will be bound.
			}
		};
		// This should block until the onServiceConnected (above) completes, but doesn't
		context.bindService(rService, connection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Finish recording this trip.
	 * Compile the Trip and return it.
	 */
	public void finishRecording()
	{
		Log.i("RECORD", "Finish Recording");
		cancelMonitorTimer(); // End the timer
		rs.finish();	// Finish the Recording Service
		//context.unbindService(connection);	// Remove the GPS Recording service
		this.finish();	// Finish the Recording Controller
	}
	
	/**
	 * Start a timer which will update the time in the UI
	 */
	public void enableUIUpdates()
	{
		Log.d("TIMER", "EnableUIUpdates()");
		timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mUpdateTimer);
            }
        }, 0, 1000);  // is run every second
	}

	public float getNumberNodes() {
		return numberNodes;
	}
	
	public boolean getRecordingState()
	{
		return isRecording;
	}
}
