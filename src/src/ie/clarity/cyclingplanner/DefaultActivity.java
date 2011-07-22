package ie.clarity.cyclingplanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import ie.clarity.cyclingplanner.Controller.GPSController;
import ie.clarity.cyclingplanner.Controller.RecordingController;
import ie.clarity.cyclingplanner.Model.History;
import ie.clarity.cyclingplanner.Model.PersonalisedRoute;
import ie.clarity.cyclingplanner.Model.RecordingService;
import ie.clarity.cyclingplanner.Model.Trip;
import android.app.Activity;
import android.content.Context;

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
	
	/**
	 * Use this function to generate a unique id for this installation.
	 * @author Maurice Gavin
	 */
	public static class Installation {
	    private static String sID = null;
	    private static final String INSTALLATION = "INSTALLATION";

	    public synchronized static String id(Context context) {
	        if (sID == null) {  
	            File installation = new File(context.getFilesDir(), INSTALLATION);
	            try {
	                if (!installation.exists())
	                    writeInstallationFile(installation);
	                sID = readInstallationFile(installation);
	            } catch (Exception e) {
	                throw new RuntimeException(e);
	            }
	        }
	        return sID;
	    }

	    private static String readInstallationFile(File installation) throws IOException {
	        RandomAccessFile f = new RandomAccessFile(installation, "r");
	        byte[] bytes = new byte[(int) f.length()];
	        f.readFully(bytes);
	        f.close();
	        return new String(bytes);
	    }

	    private static void writeInstallationFile(File installation) throws IOException {
	        FileOutputStream out = new FileOutputStream(installation);
	        String id = UUID.randomUUID().toString();
	        out.write(id.getBytes());
	        out.close();
	    }
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