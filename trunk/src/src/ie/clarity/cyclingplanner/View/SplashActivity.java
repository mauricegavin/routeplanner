package ie.clarity.cyclingplanner.View;

import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.Model.History;

import java.util.Date;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SplashActivity extends DefaultActivity {

	private static final String PREFERENCES = "Prefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		Initialisation init = new Initialisation();
		init.run();
		
		// Sample code which demonstrates the use of SharedPreferences
		SharedPreferences settings = getSharedPreferences(getUserPreferences(), MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = settings.edit();
		
		// get the current time at launch and save it
		Date date = new Date();
		String dateString = date.toGMTString(); 
		prefEditor.putString("lastLaunch", dateString);
		Log.i("Launch", "Launched at " + dateString);
		prefEditor.commit();
	
		// Apply animation to the title text
		TextView titleText = (TextView) findViewById(R.id.appName);		
		Animation fade_in_immidiately = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		titleText.startAnimation(fade_in_immidiately);

		TextView versionText = (TextView) findViewById(R.id.version);
		TextView authorText = (TextView) findViewById(R.id.author);
		Animation fade_in_later = AnimationUtils.loadAnimation(this, R.anim.fade_in2);
		
		// Add listener to leave splash screen and enter menu screen
		fade_in_later.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation)
			{	
				startActivity(new Intent(SplashActivity.this, PlanRouteActivity.class));
				SplashActivity.this.finish();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {				
			}
			@Override
			public void onAnimationStart(Animation animation) {	
			}
		});
		
		versionText.startAnimation(fade_in_later);
		authorText.startAnimation(fade_in_later);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		
		// Stop animations
		TextView logo1 = (TextView) findViewById(R.id.appName);
		TextView logo2 = (TextView) findViewById(R.id.version);
		TextView logo3 = (TextView) findViewById(R.id.author);
		logo1.clearAnimation();
		logo2.clearAnimation();
		logo3.clearAnimation();
	}
	
	public static String getPreferences() {
		return PREFERENCES;
	}

	// A Thread which runs initialisation tasks from the moment of launch.
	private class Initialisation implements Runnable
	{
		@Override
		public void run()
		{
			// Read in the History.
			history = new History();
		}
	}
}
