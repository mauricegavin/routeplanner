package ie.clarity.cyclingplanner;

import ie.clarity.cyclingplanner.Controller.GPSController;
import ie.clarity.cyclingplanner.Controller.RecordingController;
import ie.clarity.cyclingplanner.Model.History;
import ie.clarity.cyclingplanner.Model.PersonalisedRoute;
import ie.clarity.cyclingplanner.Model.RecordingService;
import ie.clarity.cyclingplanner.Model.Trip;
import android.app.Activity;

public class DefaultActivity extends Activity {
    
	// Create logging file
	private static final String USER_PREFERENCES = "UserPrefs";
	
	// Visibility Constants
	public static final int VISIBLE = 0;
	public static final int INVISIBLE = 4;
	public static final int GONE = 8;
	
	public static String getUserPreferences() {
		return USER_PREFERENCES;
	}
	
	public static void setTrip(Trip trip) {
		DefaultActivity.trip = trip;
	}
	public static Trip getTrip() {
		return trip;
	}

	private static Trip trip;
	public static PersonalisedRoute personalisedRoute;
	public static GPSController gpsCtrl;
	public static RecordingController recordCtrl;
	
	public static History history;
	
	/*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String greeting = getResources().getString(R.string.hello);
        
        Log.i(TAG, "Is onCreate() method.");
    }
	*/
	//@Override
    //public void onStart() {
	//	Log.i(TAG, "Is onStart() method.");
	//}
	
	//@Override
    //public void onResume() {
	//	Log.i(TAG, "Is onResume() method.");
	//}

	/*@Override
    public void onPause() {
		Log.i(TAG, "Is onPause() method.");
	}
	
	@Override
    public void onRestart() {
		Log.i(TAG, "Is onRestart() method.");	
	}
	
	@Override
    public void onStop() {
		Log.i(TAG, "Is onStop() method.");
	}
	
	@Override
    public void onDestroy() {
		Log.i(TAG, "Is onDestroy() method.");
	}
	 */
}