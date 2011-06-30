package ie.clarity.cyclingplanner.Controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import android.location.Location;
import android.os.Environment;
import android.util.Log;
import ie.clarity.cyclingplanner.Model.Trip;

/**
 * This class saves a trip to a file on the phone's external memory
 * @author Maurice Gavin
 *
 */
public class SaveTrip 
{
	protected Trip trip = null;
	
	public SaveTrip(Trip trip)
	{
		this.trip = trip;
		saveTrip();
	}
	
	private void saveTrip()
	{
		File saveDir = configureDirectory();
		String text = dataToWrite();
		writeToFile(saveDir, text);
		
		
		
	}

	private String dataToWrite() 
	{
		// Formatting
	    final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
	    DecimalFormat decimalFormat = new DecimalFormat("0.00");
	    DecimalFormat oneDecPointFormat = new DecimalFormat("0.00");
	    
		String output;
		// Start with the TripID
		output = "TripID: " + trip.getTripID() + "\r\n";
		// Write the starting date
		output += "Date: " + trip.getDateAtStart() + "\r\n";
		// Write the time taken
		output += "Time taken: " + sdf.format((trip.getEndTime() - trip.getStartTime() - trip.getTimePaused())) + "\r\n";
		// Write distance
		output += "Distance: " + decimalFormat.format(trip.getDistance()/1000) + " km" + "\r\n";
		// Write average speed
		output += "Average Speed: " + decimalFormat.format(trip.getAverageSpeed()*3.6) + " km/hr" + "\r\n";
		// Write max speed
		output += "Max Speed: " + decimalFormat.format(trip.getMaxSpeed()*3.6) + " km/hr" + "\r\n";
		// Write average pace
		output += "Average Pace: " + decimalFormat.format(trip.getAveragePace()) + " min/km" + "\r\n";
		// Write max pace
		output += "Max Pace: " + decimalFormat.format(trip.getMaxPace()) + " min/km" + "\r\n";
		
		// Write the GPS co-ords
		output+= "\r\n*** GPS ***\r\n";
		int size = trip.getGeoData().getPathTaken().size();
		Location temp = null;
		output += "Latitude\tLongitude\r\n";
		for(int i = 0; i < size; i++)
		{
			temp = trip.getGeoData().getPathTaken().get(i);
			output += temp.getLatitude() + ", ";
			output += temp.getLongitude() + "\r\n";
		}	
		
		return output;
	}

	private void writeToFile(File saveDir, String text) 
	{
		FileWriter textFile = null;
		try {
			textFile = new FileWriter(saveDir.toString() + "/" + trip.getTripID() + ".txt");
		} catch (IOException e) {
			Log.e("STORAGE", "Failed to create the .txt file");
			e.printStackTrace();
			return;
		}
		try {
			textFile.write(text);
		} catch (IOException e) {
			Log.e("STORAGE", "Failed to write text to .txt file");
			e.printStackTrace();
		}
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
	private boolean isStorageWritable()
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
