package ie.clarity.cyclingplanner.Controller;

import android.content.Context;

public interface IRecordService {
	public int  getState();
	public void start(Context context);
	public void cancel();
	public void finish(); // returns trip-id
	public long getCurrentTrip();  // returns trip-id
	public void pause();
	public void resume();
	public void reset();
	public void setListener(RecordingController rc);
}
