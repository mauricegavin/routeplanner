package ie.clarity.cyclingplanner.Model;


import java.util.Timer;
import java.util.TimerTask;

import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.Controller.GPSController;
import ie.clarity.cyclingplanner.Controller.IRecordService;
import ie.clarity.cyclingplanner.Controller.RecordingController;
import ie.clarity.cyclingplanner.View.MainActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class RecordingService extends Service implements LocationListener
{
	public final static int STATE_IDLE = 0;
	public final static int STATE_RECORDING = 1;
	public final static int STATE_PAUSED = 2;
	public final static int STATE_FINISHED = 3;
	public int state = STATE_IDLE;
	
	
	// Acquire a reference to the system Location Manager
	LocationManager locationManager = null;
	
	// May not be necessary to keep this, as we can communicate to the RecordingController
	// via the bound ServiceConnection
	private RecordingController recordCtrl;
	private Context context;
	private Timer timer = null;
	
	private final MyServiceBinder myServiceBinder = new MyServiceBinder();
	
	//
	// SERVICE METHODS *************************************
	//
	
	@Override
	public IBinder onBind(Intent arg0) {
		return  myServiceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public class MyServiceBinder extends Binder implements IRecordService {
		public int getState() {
			return state;
		}
		public void start(Context context) {
			RecordingService.this.start(context);
			Log.i("RS","Starting Recording Service");
		}
		public void cancel() {
			RecordingService.this.cancel();
			Log.i("RS","Cancelling Recording Service");
		}
		public void pause() {
			RecordingService.this.pause();
			Log.i("RS","Pausing Recording Service");
		}
		public void resume() {
			RecordingService.this.resume();
			Log.i("RS","Resuming Recording Service");
		}
		public void finish() {
			RecordingService.this.finish();
			Log.i("RS","Finishing Recording Service");
		}
		public void reset() {
			RecordingService.this.state = STATE_IDLE;
		}
	
		@Override
		public long getCurrentTrip() {
			// TODO Auto-generated method stub
			return 0;
		}
		/** 
		 * Add the parent Recording Controller as a Listener
		 * @param rc Reference to the RecordingController
		 */
		@Override
		public void setListener(RecordingController rc) {
			recordCtrl = rc;
		}
	}	
	//
	// END OF SERVICE METHODS *************************************
	//
	

	//
	// STATE CASES *************************************
	// 
	
	/**
	 * Begin recording the user's location
	 */
	public void start(Context context) {

		this.context = context;
		this.state = STATE_RECORDING;
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		// Register the listener with the Location Manager to receive location updates
		Log.i("GPS", "Configuring GPS Provider");
		Log.i("GPS", "Providers: " + locationManager.getProviders(true));
		// Request GPS location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this); // Request GPS fix every 2 seconds.

	    // Add the notify bar and blinking light
		setNotification();
	}

	/**
	 * Resume recording the trip
	 */
	public void resume()
	{
		this.state = STATE_RECORDING;
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
	}
	
	/**
	 * Pause recording of the trip
	 */
	public void pause()
	{
		state = STATE_PAUSED;
		locationManager.removeUpdates(this);
		stopSelf();
	}
	
	/**
	 * Cancel the trip
	 */
	public void cancel()
	{
		state = STATE_FINISHED;
		locationManager.removeUpdates(this);
		clearNotifications() ;
		stopSelf();
	}
	
	/**
	 * Finish recording the trip
	 */
	public void finish()
	{
		state = STATE_FINISHED;
		locationManager.removeUpdates(this);
		clearNotifications();
		stopSelf();
	}
	
	//
	// END OF STATE CASES *************************************
	//
	
	//
	// Implementation of the LocationListener
	//
	@Override
	public void onLocationChanged(Location location)
	{
		notifyObservers(location);
		Log.i("GPS", "User Location Changed " + location.toString());
	}



	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	private void notifyObservers(Location location)
	{
		recordCtrl.update(location);
	}
	
	//
	// End of Location Listener Implementation

	// Notifications
	
	private void setNotification() {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.logo;
		CharSequence tickerText = "Recording...";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		notification.ledARGB = 0xffff00ff;
		notification.ledOnMS = 300;
		notification.ledOffMS = 3000;
		notification.flags = notification.flags |
				Notification.FLAG_ONGOING_EVENT |
				Notification.FLAG_SHOW_LIGHTS |
				Notification.FLAG_INSISTENT |
				Notification.FLAG_NO_CLEAR;

		CharSequence contentTitle = "Route Planner - Recording";
		CharSequence contentText = "Touch to return to the recording screen";
		
		Intent notificationIntent = new Intent(context, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		notification.setLatestEventInfo(context, contentTitle, contentText,	contentIntent);
		final int RECORDING_ID = 1;
		
		mNotificationManager.notify(RECORDING_ID, notification);
	}

	private void clearNotifications() 
	{
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();

		//if (timer!=null) {
          //  timer.cancel();
            //timer.purge();
		//}
	}


}
