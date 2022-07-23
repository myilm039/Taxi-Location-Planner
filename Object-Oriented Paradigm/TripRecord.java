/**
 * This class is part of a clustering application. The purpose of this class is to store details of taxi trips 
 *
 */
 
 
public class TripRecord{
/**
 * Pickup date and time of the trip
 */
	private String pickup_DateTime;
	
/**
 * Pickup coordinates of the trip
 */
	private GPSCoord pickup_Location;
	
/**
 * Dropoff coordinates of the trip
 */
	private GPSCoord dropoff_Location;
	
/**
 * Distance of the trip
 */
	private float trip_Distance;
	


/**
 *Constructor to initialize our instance variables
 *@param pickupDateTime is the pickup date and time of the trip
 *@param pickupLocation is the pickup coordinates of the trip
 *@param dropoffLocation is the dropoff coordinates of the trip
 *@param tripDistance is the distance of the trip
 */
	
	public TripRecord(String pickupDateTime, GPSCoord pickupLocation, GPSCoord dropoffLocation, float tripDistance){
		
		pickup_DateTime = pickupDateTime;
		pickup_Location = pickupLocation;
		dropoff_Location = dropoffLocation;
		trip_Distance = tripDistance;
		
	}
	
	
	
	
	
}