package ie.clarity.cyclingplanner.View;

import ie.clarity.cyclingplanner.DefaultActivity;
import ie.clarity.cyclingplanner.R;
import ie.clarity.cyclingplanner.SettingsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The main activity is the application main screen.
 * @author Maurice Gavin
 *
 */
public class MainActivity extends DefaultActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		
		// getMenuInflator inflates a menu resource into a menu object
		getMenuInflater().inflate(R.menu.options, menu);
		// Define what the Intent of the menu options - i.e. What do they do?
		menu.findItem(R.id.settings_menu_item).setIntent(new Intent(this, SettingsActivity.class));
		menu.findItem(R.id.help_menu_item).setIntent(new Intent(this, HelpActivity.class));
		//TODO Need an intent to Launch an About Dialogue
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		startActivity(item.getIntent());
		return true;
	}

}
