package ie.clarity.cyclingplanner.Model;

import java.util.Date;

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
		averagePace = (1000 * averageSpeed) / 60;
		maxPace = (1000 * maxSpeed) /60;
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



	
}
