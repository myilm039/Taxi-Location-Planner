/**
 * This class is part of a clustering application. The purpose of this class is to store the x and y , 
 * (latitude and longitude) coordinates for clustering
 */


public class GPSCoord{
	
/**
 * x coordinate which is longitude
 */
	private double x;
	
/**
 * y coordinate which is latitude
 */	
	private double y;
/**
 *Constructor to initialize our instance variables
 *@param x is the longitude
 *@param y is the latitude
=
 */	
	 public GPSCoord(double x, double y){
		
		this.x = x;
		this.y = y;
		
	}
/**
 * Getter method for longitude
 *@return x coordinate of the location
 */	
	public double getX(){return x;}
/**
 * Getter method for latitude
 *@return y coordinate of the location
 */		
	public double getY(){return y;}


}