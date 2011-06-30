package ie.clarity.cyclingplanner.View;

import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.Controller.SaveTrip;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * The FinishQuitActivity displays the screen where the user decides
 * whether or not to save their trip once finished.
 * 
 * @author Maurice Gavin
 */
public class FinishQuitActivity extends DefaultActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish_saving);
		
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
			if(buttonID == 1)
			{
				//TODO Save some stuff
				SaveTrip saver = new SaveTrip(getTrip());
			}
			else if (buttonID == 2)
			{
				//TODO No saving to be done
			}
			startActivity(new Intent(FinishQuitActivity.this, PlanRouteActivity.class));
			FinishQuitActivity.this.finish();
		}
	}
	
}
