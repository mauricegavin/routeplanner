package ie.clarity.cyclingrouteplanner.Networking;

import ie.clarity.cyclingplanner.Model.Trip;
import ie.clarity.cyclingplanner.View.FinishQuitActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;

/**
 * This class uploads a file from the phone to an external server.
 * 
 * @author Maurice Gavin
 *
 */
public class UploadTrip extends AsyncTask
{
	private Transmitter transmit = null;
	private String tripID = null;
	
	public UploadTrip(String tripID)
	{
		this.tripID = tripID;
		transmit = new Transmitter();
	}
	
	/**
	 * Call this function to begin uploading a file to the server.
	 * @param tripID The filename of the file that is to be uploaded
	 * @return An integer which represents to the success/or lack thereof in uploading.
	 */
	private int upload(String tripID)
	{
		return transmit.send(tripID + ".gpx");
	}
	
	/**
	 * @return The uploader's transmitter
	 */
	public Transmitter getTransmitter() {
		return transmit;
	}

	protected Object doInBackground(Object... arg0)
	{
		int error = upload(tripID);

		//this.publishProgress(error);
		ProgressDialog progressDialog = (ProgressDialog)(arg0[0]);
		progressDialog.dismiss();
		
		FinishQuitActivity context = (FinishQuitActivity)(arg0[1]);
		context.setError(error);
		
		return error;
	}

}
