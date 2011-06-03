package ie.clarity.cyclingplanner;

import android.app.Activity;

public class DefaultActivity extends Activity {
    /** Called when the activity is first created. */
    
	// Create logging file
	private static final String USER_PREFERENCES = "UserPrefs";

	public static String getUserPreferences() {
		return USER_PREFERENCES;
	}
		
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