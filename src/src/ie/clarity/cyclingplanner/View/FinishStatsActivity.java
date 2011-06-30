package ie.clarity.cyclingplanner.View;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.R;

/**
 * The FinishStatsActivity shows the trip statistics screen.
 * When finished on this screen the activity then needs to send them to the saving screen.
 * 
 * @author Maurice Gavin
 */
public class FinishStatsActivity extends DefaultActivity
{
	
	// Formatting
    final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    DecimalFormat oneDecPointFormat = new DecimalFormat("0.0");
    DecimalFormat twoDigitFormat = new DecimalFormat("00");
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish_stats);
		
		// Set the content of the various fields
		getTrip().compileTrip(); // Makes sure everything is internally up to date before writing to the UI
		showStats();
		
		// Add listeners to the buttons
		Button finishButton = (Button)findViewById(R.id.finish_stats_finishButton);
		finishButton.setOnClickListener(new ButtonClickedListner());
		
	}
	
	private void showStats()
	{		
		// Get reference to all of the text fields
		TextView timeText = (TextView)findViewById(R.id.value_timeelapsed);
		TextView distText = (TextView)findViewById(R.id.value_distance);
		TextView speedText = (TextView)findViewById(R.id.value_speed);
		TextView maxSpeedText = (TextView)findViewById(R.id.value_maxspeed);
		TextView paceText = (TextView)findViewById(R.id.value_pace);
		TextView maxPaceText = (TextView)findViewById(R.id.value_maxpace);
		TextView caloriesText = (TextView)findViewById(R.id.value_calories);
		
		// Display the various statistics
		
		// Time needs to be parsed
		double timeElapsed = getTrip().getEndTime() - getTrip().getTimePaused() - getTrip().getStartTime();
		int hours = (int)timeElapsed/3600000; // Find how many hours there are
		timeElapsed = timeElapsed%3600000;
		int minutes = (int)timeElapsed/60000; // Find the number of minutes
		timeElapsed = timeElapsed%60000;
		int seconds = (int)timeElapsed/1000; // Find out how many seconds there are
		timeText.setText(twoDigitFormat.format(hours) + ":" + twoDigitFormat.format(minutes) + ":" + twoDigitFormat.format(seconds));
		
		distText.setText(String.valueOf(decimalFormat.format(getTrip().getDistance()/1000)) + " km");	// Distance is stored in m
		speedText.setText(String.valueOf(oneDecPointFormat.format(getTrip().getAverageSpeed()*3.6)) + " km/hr");
		maxSpeedText.setText(String.valueOf(oneDecPointFormat.format(getTrip().getMaxSpeed()*3.6)) + " km/hr");
		paceText.setText(String.valueOf(oneDecPointFormat.format(getTrip().getAveragePace())) + " min/km");
		maxPaceText.setText(String.valueOf(oneDecPointFormat.format(getTrip().getMaxPace())) + " min/km");
		//calories
	}

	private class ButtonClickedListner implements OnClickListener
	{
		@Override
		public void onClick(View v) 
		{			
			startActivity(new Intent(FinishStatsActivity.this, FinishQuitActivity.class));
			FinishStatsActivity.this.finish();		
		}
	}
	
}
