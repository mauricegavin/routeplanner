package ie.clarity.cyclingplanner.Model;

import java.util.Date;

import android.util.Log;

/**
 * A Trip object contains all information associated with a single journey.
 * @author Maurice Gavin
 *
 */
public class Trip
{
	private String tripID;
	private Date dateAtStart;
	private double startTime;
	private double endTime;
	private double pauseStartTime;
	private double timePaused;	// This is needed to take Pauses into account.
	private float distance;		// The distance that has been covered, in meters
	private Route geoData;		// All of the geographic co-ordinates are stored here
	
	private double averageSpeed;	// Stored as m/s
	private double maxSpeed;
	private double averagePace;		// Stored as min/km
	private double maxPace;
	private double caloriesBurned;
	
	private PersonalisedRoute personalisedRoute;
	
	public Trip()
	{
		setTripId(createTripID());
		initialise();
	}

	public Trip(PersonalisedRoute customRoute)
	{
		setTripId(createTripID());
		setPersonalisedRoute(customRoute);
		initialise();
	}
	
	void initialise()
	{
		setDateAtStart(new Date());
		// Note: Start time is given its true value when the first GPS fix is received
		setStartTime(System.currentTimeMillis());
		setEndTime(System.currentTimeMillis());
		setDistance(0);
		setGeoData(new Route());
		
		setAverageSpeed(0);
		setMaxSpeed(0);
		setAveragePace(0);
		setMaxPace(0);
		setPauseStartTime(0);
		setTimePaused(0);
	}
	
	private String createTripID() 
	{
		String id;
		Date date = new Date();
		
		
		int month = date.getMonth() + 1; // Must add 1 to the month because Jan = 0; Feb = 1, etc.
		int year = date.getYear() + 1900; // getYear() returns the number of years after 1900.

		id = date.getDate() + "_" + month + "_" + year;
		id = id + " " + date.getHours() + "_" + date.getMinutes() + "_" + date.getSeconds();
		
		return id;
	}

	/**
	 * Calculate the values that are not updated elsewhere.
	 */
	public void compileTrip()
	{
		// Pace is time/distance
		averagePace = 60/(averageSpeed*3.6);
		maxPace = 60/(maxSpeed*3.6);
		
		// Calculate Calories Burned
		// Calories Expended [kcal] = METs x weight [kg] x Duration [hr]
		double cycleTime = getEndTime() - getTimePaused() - getStartTime();
		cycleTime = cycleTime/3600000; // Convert from ms to hr.
		
		double weight = 80; //kg //TODO Take weight from user information page when it is done.
		
		// METs Table
		int METs = 0;
		double speedInKmPerHr = averageSpeed*3.6;
		if (speedInKmPerHr < 16){
			METs = 4;
		}
		else if (speedInKmPerHr < 19.5){
			METs = 6;
		}
		else if (speedInKmPerHr < 22.5){
			METs = 8;
		}
		else if (speedInKmPerHr < 26){
			METs = 10;
		}
		else if (speedInKmPerHr < 30.5){
			METs = 12;
		}
		else if (speedInKmPerHr >= 30.5){
			METs = 16;
		}
			
		// Calculation
		caloriesBurned = METs * weight * cycleTime;
	}
	
	public void setTripId(String tripID) {
		this.tripID = tripID;
	}

	public String getTripID() {
		return tripID;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getDistance() {
		return distance;
	}

	public void setGeoData(Route geoData) {
		this.geoData = geoData;
	}

	public Route getGeoData() {
		return geoData;
	}

	public void setAverageSpeed(double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public double getAverageSpeed() {
		return averageSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setAveragePace(double averagePace) {
		this.averagePace = averagePace;
	}

	public double getAveragePace() {
		return averagePace;
	}

	public void setMaxPace(double maxPace) {
		this.maxPace = maxPace;
	}

	public double getMaxPace() {
		return maxPace;
	}

	public void setPersonalisedRoute(PersonalisedRoute personalisedRoute) {
		this.personalisedRoute = personalisedRoute;
	}

	public PersonalisedRoute getPersonalisedRoute() {
		return personalisedRoute;
	}

	public void setDateAtStart(Date dateAtStart) {
		this.dateAtStart = dateAtStart;
	}

	public Date getDateAtStart() {
		return dateAtStart;
	}

	public void setTimePaused(double timePaused) {
		this.timePaused = timePaused;
	}

	public double getTimePaused() {
		return timePaused;
	}

	public void setPauseStartTime(double pauseStartTime) {
		this.pauseStartTime = pauseStartTime;
	}

	public double getPauseStartTime() {
		return pauseStartTime;
	}

	public void setCaloriesBurned(double caloriesBurned) {
		this.caloriesBurned = caloriesBurned;
	}

	public double getCaloriesBurned() {
		return caloriesBurned;
	}



	
}
