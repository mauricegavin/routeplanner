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
	private Location lastLocation = null;
	private float sumSpeed = 0;
	private float sumPace = 0;
	
	public RecordingController(MainActivity context) 
	{
		this.context = context;
		setTrip(new Trip(personalisedRoute));
		start(this);
	}

	/**
	 * Start recording the trip
	 */
	private void start(final RecordingController refController)
	{
		//gpsCtrl = new GPSController();
		
		rService = new Intent(context, RecordingService.class);
		context.startService(rService);
		
		connection = new ServiceConnection()
		{
				public void onServiceDisconnected(ComponentName name) {
					Log.i("RECORD", "Recording Service Disconnected");
				}
				
				public void onServiceConnected(ComponentName name, IBinder service) 
				{
					Log.i("DEBUG", "Recording Service connected");
					rs = (IRecordService) service;
					rs.setListener(refController); // Pass in the reference to the Controller before starting the RecordingService.
					
					switch (rs.getState()) {
						case RecordingService.STATE_IDLE:
							rs.start(context);
							isRecording = true;
							Log.i("RECORD", "State: "+rs.getState() + " Is Recording: " + isRecording);
							break;
						case RecordingService.STATE_RECORDING:
							isRecording = true;
							Log.i("RECORD", "State: "+rs.getState() + " Is Recording: " + isRecording);
							break;
						case RecordingService.STATE_PAUSED:
							isRecording = false;
							Log.i("RECORD", "State: "+rs.getState() + " Is Recording: " + isRecording);
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
		
		enableUIUpdates();
	
	}
	
	/**
	 * The update function is called by objects that the Controller is listening to.
	 * Its called when new trip data has become available and needs to be stored.
	 */
	public void update(Location location)
	{
		numberNodes++; 				// Increment #nodes. Needed to calculate averages.
		// Update the distance
		if(lastLocation != null)	// This will be true when the first co-ordinate is received.
		{
			if(location.getSpeed() > 1)	// If speed of travel is < 1m/s ignore it. Person is probably stationary and the distance travelled would be just location error.
				getTrip().setDistance( getTrip().getDistance() + lastLocation.distanceTo(location) ); // Add the distanceTravlled to the latest segment distance for the new total
		}
		Route goegraphicRoute = getTrip().getGeoData();
		goegraphicRoute.addNode(location);
		
		// Update average and maximum values
		sumSpeed = sumSpeed + location.getSpeed();
		getTrip().setAverageSpeed( sumSpeed/numberNodes );	// Calculate the new Average Speed
		if(location.getSpeed() > getTrip().getMaxSpeed())
		{
			getTrip().setMaxSpeed(location.getSpeed());
		}
		
		// Update the time elapsed
		getTrip().setEndTime(System.currentTimeMillis());
		
		lastLocation = location;
		
		// Notify the MainActivity of the changes so that it can update the UI if necessary.
		context.update(getTrip(), location);
	}
	
	/**
	 * Resume Recording
	 * Enable GPS updates again.
	 */
	public void resumeRecording()
	{
		isRecording = true;
		rs.resume();
		Log.i("RECORD", "Resume Recording");
		
		double lengthPaused = System.currentTimeMillis() - getTrip().getPauseStartTime();
		getTrip().setTimePaused( getTrip().getTimePaused() + lengthPaused ); // Append the length of the latest pause to the total paused time
		setListener();
	}
	
	/**
	 * Pause the Recording.
	 * Disable GPS updates for now.
	 */
	public void pauseRecording()
	{
		isRecording = false;
		rs.pause();
		Log.i("RECORD", "Pause Recording");
		
		getTrip().setPauseStartTime(System.currentTimeMillis()); // Record the time at which the pause occurred.
		setListener();
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
				context.unbindService(connection);
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
		disableUIUpdates(); // End the timer
		rs.finish();	// Finish the Recording Service
		//context.unbindService(connection);	// Remove the GPS Recording service
		this.finish();	// Finish the Recording Controller
	}
	
	/**
	 * This function is called when the program is paused.
	 * It disables the timer from updating but doesn't affect the GPS,
	 */
	public void disableUIUpdates()
	{
		timer.cancel();
		timer.purge();
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
            	Log.d("TIMER", "TimerTask run");
                mHandler.post(mUpdateTimer);
            }
        }, 0, 1000);  // is run every second
	}

	public float getNumberNodes() {
		return numberNodes;
	}
}
