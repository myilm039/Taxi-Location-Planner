/**
 * This class is part of a clustering application. This class has the main method. In this class, we first parse
 * the points and details from the CSV file that we want, decide eps and minPts, and then use the DBSCAN algorithm
 * to produce clusters of these points, creating a CSV file in the end
 * containing average x and y coordinates of a cluster, and number of points in a cluster.
 *
 */


import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;


public class TaxiClusters{
	
/**
 * The csv file to get the points for DBSCAN, and get the data for storage in TripRecord
 */	   
   private static String file = "yellow_tripdata_2009-01-15_1hour_clean.CSV";
/**
 * BufferedReader instance to read and parse the CSV file
 */	    
   private static BufferedReader reader = null;
/**
 * A variable to hold and read each row in the CSV file
 */	    
   private static String line = "";
/**
 * List of points to be used by DBSCAN algorithm for clustering
 */	    
   private static ArrayList<GPSCoord> points = new ArrayList<GPSCoord>();
/**
 * The list of clusters, produced by the DBSCAN algorithm
 */	    
   private static List <ArrayList<GPSCoord>>result;
/**
 * Minimum number of points to be considered a cluster by DBSCAN
 */	    
   private static int minPts;
/**
 * Maximum distance between points to be considered belonging to the same cluster
 */	   
   private static double eps;

  
	public static void main(String[]args){
		
		TaxiClusters taxiClusters = new TaxiClusters();
		
		taxiClusters.initialize();
		             
		try{
			
		Cluster clusterer = new Cluster(points, minPts, eps);  	   
      
		result = clusterer.performClustering();
				
		taxiClusters.output();

		}
		
		catch(Exception e){
			
			e.printStackTrace();
		}
        
        
    }
	
/**
 * This method initializes the instance variables, reads and parses the CSV file to store trip records and produce points
 * for clustering
 */	
	public void initialize(){
		
		minPts = 5;
		eps = 0.0003;
		
	try {
	reader = new BufferedReader(new FileReader(file));
	
	reader.readLine();
   while((line = reader.readLine()) != null) {
    
    String[] row = line.split(",");
      
   TripRecord record = new TripRecord(row[4], 
   new GPSCoord(Double.parseDouble(row[8]),Double.parseDouble(row[9])), 
   new GPSCoord(Double.parseDouble(row[12]),Double.parseDouble(row[13])), Float.parseFloat(row[7]));
      
   points.add(new GPSCoord(Double.parseDouble(row[8]),Double.parseDouble(row[9])));

   }
  }
  catch(Exception e) {
   e.printStackTrace();
  }
  finally {
   try {
    reader.close();
   } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
		
	}
		
}
/**
 * This method calculates the arithmetic mean of x coordinates, for all points in a cluster
 * @param list is the cluster that we want to calculate the average longitude
 * @return average x coordinate in a cluster
 */	
public double getAverageX(ArrayList<GPSCoord> list){
	
		double sum =0;
	
		  for(int i=0; i<list.size();i++){
			  
			  sum+= list.get(i).getX();
			  
		  }
		  
		  return sum/list.size();

	
}

/**
 * This method calculates the arithmetic mean of y coordinates, for all points in a cluster
 * @param list is the cluster that we want to calculate the average latitude
 * @return average y coordinate in a cluster
 */
public double getAverageY(ArrayList<GPSCoord> list){
	
		  double sum =0;
	
		  for(int i=0; i<list.size();i++){
			  
			  sum+= list.get(i).getY();
			  
		  }
		  
		  return sum/list.size();
	
	
}
	
 /**
 * This method produces a CSV file, containing average x and y coordinates of a cluster, and number of points in a cluster.
 * It contains all of the cluster details produced by the DBSCAN algorithm
 */   

public void output(){
	
	 Collections.sort(result,new Comparator<ArrayList<GPSCoord>>() {

public int compare(ArrayList<GPSCoord> o1, ArrayList<GPSCoord> o2) {
    return Integer.compare(o1.size(), o2.size());
}
});

Collections.reverse(result);
		
		
	 try (PrintWriter writer = new PrintWriter("clusters.csv")) {

      StringBuilder sb = new StringBuilder();
	  
	  sb.append("Average Longitude");
      sb.append(',');
      sb.append("Average Latitude");
	  sb.append(',');
      sb.append("Number of Points");
      sb.append('\n');
	  
	  for(int i=0; i<result.size();i++){
		  
		  
			  sb.append(getAverageX(result.get(i)));
			  sb.append(',');
			  sb.append(getAverageY(result.get(i)));
			  sb.append(',');
			  sb.append(result.get(i).size());
			  sb.append('\n');
			  
		  
		  
	  }
     
      writer.write(sb.toString());

      System.out.println("Completed!");

    } 
	
	catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
    }
	
	
}
}