package ie.clarity.cyclingplanner.Model;

import java.util.ArrayList;

/**
 * A History object is a list of recorded Trips stored in an ArrayList.
 * @author Maurice Gavin
 *
 */

public class History 
{
	private ArrayList<Trip> history;
	
	public History()
	{
		history = new ArrayList<Trip>();
	}

	public void setHistory(ArrayList<Trip> history) {
		this.history = history;
	}

	public ArrayList<Trip> getHistory() {
		return history;
	}
	
	public Boolean addTrip(Trip trip)
	{
		history.add(trip);
		return true;
	}
}
