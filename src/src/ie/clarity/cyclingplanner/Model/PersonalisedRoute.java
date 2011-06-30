package ie.clarity.cyclingplanner.Model;

/**
 * A PersonalisedRoute object contains the information about the suggested personalised route.
 * @author Maurice Gavin
 *
 */
public class PersonalisedRoute
{
	private String reason;
	private String type;
	
	// Default constructor
	public PersonalisedRoute()
	{
		setReason(null);
		setType(null);
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
