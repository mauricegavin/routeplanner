package ie.clarity.cyclingplanner.Controller;

import ie.clarity.cyclingplanner.Model.Trip;
import android.content.Context;

public interface IRecordService {
	public int  getState();
	public void start(Trip trip);
	public void cancel();
	public void finish(); // returns trip-id
	public String getCurrentTrip();  // returns trip-id
	public void pause();
	public void resume();
	public void reset();
	public void setListener(RecordingController rc);
}
