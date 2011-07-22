package ie.clarity.cyclingplanner.Controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import ie.clarity.cyclingplanner.Model.Trip;
import ie.clarity.cyclingplanner.View.FinishQuitActivity.ProgressThread;

/**
 * This class saves a trip to a file on the phone's external memory
 * @author Maurice Gavin
 *
 */
public class SaveTrip 
{
	protected Trip trip = null;
	private Context context;
	
	public SaveTrip(Trip trip, Context context)
	{
		this.trip = trip;
	}
	
	public void saveTrip()
	{
		File saveDir = configureDirectory();
		
		if(saveDir != null)
		{
			writeToFile(saveDir);	
		}
		else
		{
			Log.e("STORAGE", "SD Card not available.");
		}
	}

	private void writeToFile(File saveDir) 
	{
		FileWriter textFile = null;
		
		try {
			textFile = new FileWriter(saveDir.toString() + "/" + trip.getTripID() + ".gpx");
		} catch (IOException e) {
			Log.e("STORAGE", "Failed to create the .txt file");
			e.printStackTrace();
			return;
		}
		
		ExportGPX asGPX = new ExportGPX(context);
		asGPX.prepare(trip, textFile);
		asGPX.write();
		
		try {
			textFile.close();
		} catch (IOException e) {
			Log.e("STORAGE", "Failed to close the .txt file");
			e.printStackTrace();
		}
	}
	

	/**
	 * The configure directory returns the target directory for saving.
	 * If it doesn't already exist it will be created.
	 * 
	 * @return The directory that where trip data is saved
	 */
	private File configureDirectory() 
	{
		if(isStorageWritable())
		{
			File rootDir = Environment.getExternalStorageDirectory();
			File saveDir = new File(rootDir.toString() + "/Android/data/ie.clarity.cyclingplanner/", "history");
			
			// Create a directory; all non-existent ancestor directories are
			// automatically created with .mkdirs()
			if(saveDir.exists())
			{
				Log.i("STORAGE", "This directory already exists, no need to create it again.");
			}
			else
			{
				boolean success = (saveDir).mkdirs();
			
				if (!success) 
				{
					Log.e("STORAGE", "Failed to create Directory");
					return null;
				}
			}
			// At this point the directory that we are saving trips to exists
			return saveDir;
		}
		return null;	// Cannot write to memory
	}

	/**
	 * Determine if external storage is writable.
	 * @return True if is writable, otherwise false.
	 */
	public boolean isStorageWritable()
	{
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
			Log.i("STORAGE","We can read/write to/from the external storage");
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		    return mExternalStorageWriteable;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
			Log.e("STORAGE","We can only read from the external storage");
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		    return mExternalStorageWriteable;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
			Log.e("STORAGE","We cannot read/write to/from the external storage");
			mExternalStorageAvailable = mExternalStorageWriteable = false;
			return mExternalStorageWriteable;
		}
	}
	
}
