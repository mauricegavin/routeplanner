package ie.clarity.cyclingplanner;

import ie.clarity.cyclingplanner.View.HelpActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MenuActivity extends DefaultActivity 
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		
		// Populate the menu list
		// Take the resource
		ListView menuList = (ListView) findViewById(R.id.ListView_menu);
		// Take the string resources to be used
		String[] items = { 	getResources().getString(R.string.menuText1),	// Personal Info
							getResources().getString(R.string.menu_item_settings),	// Settings
							getResources().getString(R.string.menu_item_help)};	// Help 
		// An Adapter is needed to put the contents into the menuList
		ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, R.layout.menu_item, items);
		menuList.setAdapter(adapt);
		
		// Add listeners to the menu items
		menuList.setOnItemClickListener(new menuItemListener());
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
	
	public class menuItemListener implements OnItemClickListener
	{
		public menuItemListener() {
		}
		
		/**
		 * onItemClick determines which Menu Item has been selected and launches the appropriate Activity/Dialogue
		 * @param parent		The AdapterView where the click happened.
		 * @param itemClicked	The view within the AdapterView that was clicked (this will be a view provided by the adapter)
		 * @param position		The position of the view in the adapter.
		 * @param id			The row id of the item that was clicked.
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id)
		{	
			TextView textView = (TextView) itemClicked;	// Get the textView that was clicked
			String stringText = textView.getText().toString();	// Take the text from the textView
		
			// Determine which option has been selected
			if (stringText.equalsIgnoreCase(getResources().getString(R.string.menuText1))) 
			{
				// Launch the Personal Info Activity
				//startActivity(new Intent(MenuActivity.this, SettingsActivity.class));
			} 
			else if (stringText.equalsIgnoreCase(getResources().getString(R.string.menu_item_settings))) 
			{
				// Launch the Settings Activity
				startActivity(new Intent(MenuActivity.this, SettingsActivity.class));
			}
			else if (stringText.equalsIgnoreCase(getResources().getString(R.string.menu_item_help)))
			{
				// Launch the Help Activity
				startActivity(new Intent(MenuActivity.this, HelpActivity.class));
			}
		}
			
	}

}

