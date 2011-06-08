package ie.clarity.cyclingplanner.View;

import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class HelpActivity extends DefaultActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		// Set GUI parameters
		TextView helpText = (TextView)findViewById(R.id.TextView_helpText); 
		helpText.setMovementMethod(new ScrollingMovementMethod()); 
		
		// Set the text to the help documentation stored in help.txt
		InputStream in = getResources().openRawResource(R.raw.help);
		String stringFile = inputStreamToString(in);
		helpText.setText(stringFile);
		
	}

	 
 /*
  * To convert the InputStream to String we use the
  * Reader.read(char[] buffer) method. We iterate until the
  * Reader return -1 which means there's no more data to
  * read. We use the StringWriter class to produce the string.
  */

	private String inputStreamToString(InputStream in)
	{		
		String contents = null;
		
		if (in != null)
		{
		    StringWriter writer = new StringWriter();

		    char[] buffer = new char[1024];
		     
		    try {
		        BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Log.e("IO", "Could not parse inputStream", e);
					e.printStackTrace();
				}
		         int length = 0;
		         // Write the data from the inputStream into the character array
		         try {
					while ((length = reader.read(buffer)) != -1)
					 {
					     writer.write(buffer, 0, length);
					 }
				} catch (IOException e) {
					Log.e("IO", "Could not parse inputStream", e);
					e.printStackTrace();
				}
				contents = String.valueOf(buffer); // Fill returning String with the buffer
		     }
		     finally 
		     {
		         try {
					in.close();
				} catch (IOException e) {
					Log.e("IO", "Could not close inputStream", e);
					e.printStackTrace();
				}
		     }
		}
		return contents;
	}	
}
