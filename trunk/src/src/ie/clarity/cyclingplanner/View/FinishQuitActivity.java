package ie.clarity.cyclingplanner.View;

import java.util.Observable;
import java.util.Observer;

import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.Controller.SaveTrip;
import ie.clarity.cyclingrouteplanner.Networking.UploadTrip;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * The FinishQuitActivity displays the screen where the user decides
 * whether or not to save their trip once finished.
 * 
 * @author Maurice Gavin
 */
public class FinishQuitActivity extends DefaultActivity
{
	protected boolean upload = false;
	protected boolean retry = true;
	protected UploadTrip uploader = null; 
	protected Context context;
	
	// Progress Dialogue - Saving
	static final int PROGRESS_DIALOG_SAVE = 0;
	static final int PROGRESS_DIALOG_UPLOAD = 1;
	protected ProgressThread progressThread = null;
	protected ProgressDialog progressDialog = null;

    // Handle Error messages
    private int error = -1;
    protected ErrorThread errorThread = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish_saving);
	
		context = this; // Store the context.
		
		Button save = (Button)findViewById(R.id.finish_save_quit);
		save.setOnClickListener(new ButtonClickedListener(1));
		
		Button noSave = (Button)findViewById(R.id.finish_quit_no_save);
		noSave.setOnClickListener(new ButtonClickedListener(2));
	}
	
	private class ButtonClickedListener implements OnClickListener
	{
		private int buttonID;
		
		ButtonClickedListener(int id)
		{
			buttonID = id;
		}
		
		@Override
		public void onClick(View v) 
		{
			// Check if the user wants to upload now
			CheckBox uploadBox = (CheckBox)findViewById(R.id.finish_upload);
			if(uploadBox.isChecked()) {
				upload = true;
			}
			else {
				upload = false;
			}
				
			if(buttonID == 1) // Save and Quit Button
			{
				// Create a new SaveTrip object, used to check if memory is available and then to write to memory
				SaveTrip saver = new SaveTrip(getTrip(), context);
				
				// Check to see if the external storage is available for writing to
				if(saver.isStorageWritable())
				{
					showDialog(PROGRESS_DIALOG_SAVE);
					saver.saveTrip(); // Write the trip to a file on the external storage
					
					// Now upload if appropriate
					if(upload)
					{
						uploadTrip();
					}
					else
					{
						finishActivity();
					}
				}
				else
				{
					informStorageError();
				}
			}
			else if (buttonID == 2) // Quit without Saving Button
			{
				quitWithoutSaving();
				//TODO No saving to be done
			}
		}
	}
	
	/**
	 * Call this function if you do not with to save the trip.
	 */
	protected void quitWithoutSaving()
	{
		finishActivity();
	}
	
	/**
	 * If everything that is required is completed then this function is called to finish the activity
	 * E.g. Saving, not saving, uploading, not uploading, acknowledging dialogs, etc. 
	 */
	protected void finishActivity()
	{
		if(progressThread != null) progressThread.setState(ProgressThread.STATE_DONE);
		if(errorThread != null) errorThread.setState(ErrorThread.STATE_DONE);
		
		startActivity(new Intent(FinishQuitActivity.this, PlanRouteActivity.class));
		FinishQuitActivity.this.finish();
	}
	
	/**
	 * This function checks to see if the device has Internet access
	 */
	public boolean isOnline() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if(activeNetworkInfo != null) // Network is available.
			return true;
		else // Network is unavailable
			return false;
		}
	
	/**
	 * Call this function when you want to upload the file to the server.
	 */
	private boolean uploadTrip()
	{
		// First it is important to check if the device has network connectivity.
		if(isOnline())
		{
			showDialog(PROGRESS_DIALOG_UPLOAD);
			
			uploader = new UploadTrip(getTrip().getTripID()); // Sets up the transmitter
			uploader.execute(progressDialog, FinishQuitActivity.this);
			//TODO Create a thread which will try to run the error messages.
			//They are waiting for some control variable to change
			//Change that variable in the AsyncTask.

			return true;
		}
		else
		{
			informNoNetworkConnection();
			return false;
		}
		
	}
	
	  /**
	   * This function reads from the shared preferences to see what the user set as their start/end points
	   * @return A string describing these points.
	   */
	  private String intendedStartEnd()
	  {
		  SharedPreferences sharedStorage = getSharedPreferences("Coords", MODE_PRIVATE);
		  return null;
	  }
	  
	//
	// ERROR MESSAGES
	//

	/**
	 * Informs the user that they cannot write to external memory
	 */
	private void informStorageError() 
	{
		 final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage("Your phone's storage is unavailable.\n\n" +
	        		"Possible reasons:\n" +
	        		"- Your phone's SD Card is removed;\n" +
	        		"- Your phone does't have an SD Card;\n" +
	        		"- Your phone is mounted as a USB storage device.")
	        	   .setTitle("Trip Cannot be Saved")
	               .setCancelable(false)
	               .setPositiveButton("OK, don't quit yet.", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {  
	                	   // Return to the selection menu.
	                   }
	               })
	               .setNegativeButton("Quit without Saving", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {
	                        dialog.cancel();
	                        quitWithoutSaving();
	                   }
	               });
	               ;
	        final AlertDialog alert = builder.create();
	        alert.show();
	}
	
	/**
	 * Informs the user that they are not connected to the Internet.
	 * Brings them to a screen where they can connect if they choose.
	 */
	private void informNoNetworkConnection() 
	{
		 final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage("You cannot upload your Trip at this time.\n\n" +
	        		"Ensure that you are connected to the internet before continuing.")
	        	   .setTitle("No Internet Connection")
	               .setCancelable(false)
	               .setPositiveButton("Network Settings...", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {  
	                	   Intent myIntent = new Intent( Settings.ACTION_WIRELESS_SETTINGS );
	                	   startActivity(myIntent);
	                   }
	               })
	               .setNeutralButton("Back", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {
	                	   // Return to the selection menu.
	                	   dialog.cancel();
	                   }
	               })
	               .setNegativeButton("Do not Upload", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {
	                        dialog.cancel();
	                        finishActivity();
	                   }
	               });
	        final AlertDialog alert = builder.create();
	        alert.show();
	}
	
	/**
	 * Inform the user of the status of the file upload.
	 * @param error The ID of the error.
	 */
	public void verifyUpload() 
	{
		if (error != 0) // If (error == 0) there is no error
		{
			if(retry == true) // Retry the upload once
			{
				retry = false;
				Toast toast = Toast.makeText(context, "Upload unsuccessful. Retrying once...", Toast.LENGTH_SHORT);
				toast.show();
				uploadTrip();
			}
			else // Otherwise continue with error messages
			{
			
				switch(error)
				{
				case 1:
					createErrorMessage("Could not connect to server.");
					return;
				case 2:
					createErrorMessage("Could not log in to server.");
					return;
				case 3:
					createErrorMessage("Could not find file to send.");
					return;
				case 4:
					createErrorMessage("Could not store file on server.");
					return;
				case 5:
					createErrorMessage("Could not logout of server.");
					return;
				case 6:
					createErrorMessage("Could not close transmitted file.");
					return;
				case 7:
					createErrorMessage("Could not disconnect from server.");
					return;
				default:
					createErrorMessage("Unexpected error.");
					return;
				}
			}
		}
		else
		{
			Toast toast = Toast.makeText(context, "Trip has been uploaded", Toast.LENGTH_LONG);
			toast.show();
			((FinishQuitActivity)(context)).finishActivity();
		}
		
	}
	/**
	 * Informs the user that there has been an error uploading the file
	 */
	private void createErrorMessage(String message) 
	{
         uploadHandler.sendMessage(new Message()); // Dismiss uploading progress bar
        
		 final AlertDialog.Builder builder = new AlertDialog.Builder(FinishQuitActivity.this);
	        builder.setMessage(message)
	        	   .setTitle("Upload Error")
	               .setCancelable(false)
	               .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {  
	                	   uploadTrip();
	                   }
	               })
	               .setNegativeButton("Upload Later", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, final int id) 
	                   {  
	                	   dialog.cancel();
	                	   finishActivity();
	                   }
	               });
	        final AlertDialog alert = builder.create();
	        alert.show();
	}

	public void setError(int err) {
		error = err;
	}
	
	// Progress Dialogue
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case PROGRESS_DIALOG_SAVE:
            progressDialog = new ProgressDialog(FinishQuitActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Saving...");
            return progressDialog;
        case PROGRESS_DIALOG_UPLOAD:
        	progressDialog = new ProgressDialog(FinishQuitActivity.this);
        	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	progressDialog.setMessage("Uploading...");
        	progressDialog.setCancelable(false);
        	return progressDialog;
        default:
            return null;
        }
    }

	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case PROGRESS_DIALOG_SAVE:
            progressDialog.setProgress(0);
            progressThread = new ProgressThread(handler);
            progressThread.start();
            break;
        case PROGRESS_DIALOG_UPLOAD:
        	errorThread = new ErrorThread(errorHandler);
            errorThread.start();
        	break;
        }
	}
	
	// UI Callback handler for the Saving Progress Bar
	final Handler handler = new Handler() 
	{
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            progressDialog.setProgress(total);
            if (total >= 100){
                dismissDialog(PROGRESS_DIALOG_SAVE);
                progressThread.setState(ProgressThread.STATE_DONE);
            }
        }
	};
	
	// UI Callback handler for the uploading progress bar
	final Handler uploadHandler = new Handler() 
	{
        public void handleMessage(Message msg) 
        {
            dismissDialog(PROGRESS_DIALOG_UPLOAD);
        }
	};
	
	// UI Callback handler for the error messaging
	final Handler errorHandler = new Handler()
	{
        public void handleMessage(Message msg) 
        {
            if(error != -1)
            {
            	errorThread.setState(ErrorThread.STATE_DONE);
            	verifyUpload();
            	error = -1;
            }
        }
	};
	
	/**
	 * This class provides a progress bar to show the progress of saving/uploading the trip.
	 */
    
	 public class ProgressThread extends Thread 
	 {
		 Handler mHandler;
		 final static int STATE_DONE = 0;
		 final static int STATE_RUNNING = 1;
		 int mState;
		 int total;
	       
		 ProgressThread(Handler h) {
			 mHandler = h;
		 }
	       
		 public void run() {
			 mState = STATE_RUNNING;   
			 total = 0;
			 while (mState == STATE_RUNNING) {
				 try {
					 Thread.sleep(10);
				 } catch (InterruptedException e) {
					 Log.e("ERROR", "Thread Interrupted");
				 }
				 Message msg = mHandler.obtainMessage();
				 msg.arg1 = total;
				 mHandler.sendMessage(msg);
				 total++;
			 }
		 }
	        
		 /* sets the current state for the thread,
		  * used to stop the thread */
		 public void setState(int state) {
			 mState = state;
		 }
		 
		public void setTotal(int i) {
			total = i;			
		}
	 };
	 
	 public class ErrorThread extends Thread 
	 {
		 Handler mHandler;
		 final static int STATE_DONE = 0;
		 final static int STATE_RUNNING = 1;
		 int mState;
	       
		 ErrorThread(Handler h) {
			 mHandler = h;
		 }
	       
		 public void run() {
			 mState = STATE_RUNNING;   
			 while (mState == STATE_RUNNING) 
			 {
				 try {
					 Thread.sleep(50);
				 } catch (InterruptedException e) {
					 Log.e("ERROR", "Thread Interrupted");
				 }
				 Message msg = mHandler.obtainMessage();
				 mHandler.sendMessage(msg);
			 }
		 }
	        
		 /* sets the current state for the thread,
		  * used to stop the thread */
		 public void setState(int state) {
			 mState = state;
		 }

	 };
	
}