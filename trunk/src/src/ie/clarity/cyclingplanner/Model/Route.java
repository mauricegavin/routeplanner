package ie.clarity.cyclingplanner.Model;

import java.util.ArrayList;

import android.location.Location;

/**
 * A Route object contains the geometric co-ordinates of the route.
 * @author Maurice Gavin
 *
 */
public class Route
{
	private ArrayList<Location> pathTaken;
	private Location startPoint = null;
	private Location mostRecentPoint = null;

	Route()
	{
		pathTaken = new ArrayList<Location>();
	}
	
	public void addNode(Location in)
	{
		if(startPoint == null)
		{
			startPoint = in;
		}
		pathTaken.add(in);
		setMostRecentPoint(in);
	}

	public ArrayList<Location> getPathTaken() {
		return pathTaken;
	}

	public void setMostRecentPoint(Location mostRecentPoint) {
		this.mostRecentPoint = mostRecentPoint;
	}

	public Location getMostRecentPoint() {
		return mostRecentPoint;
	}
}
