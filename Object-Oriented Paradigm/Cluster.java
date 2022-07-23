
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
/**
 * Implementation of density-based clustering algorithm DBSCAN.
 * 
 * Original Publication: 
 * Ester, Martin; Kriegel, Hans-Peter; Sander, Jörg; Xu, Xiaowei (1996). 
 * Simoudis, Evangelos; Han, Jiawei; Fayyad, Usama M., eds. 
 * A density-based algorithm for discovering clusters in large spatial 
 * databases with noise. Proceedings of the Second International Conference 
 * on Knowledge Discovery and Data Mining (KDD-96). AAAI Press. pp. 226-231
 * 
 * Usage:
 * - Identify type of input values.
 * - Implement metric for input value type using DistanceMetric interface.
 * - Instantiate using {@link #DBSCANClusterer(Collection, int, double, DistanceMetric)}.
 * - Invoke {@link #performClustering()}.
 * 
 * See tests and metrics for example implementation and use.
 * 
 * @author <a href="mailto:cf@christopherfrantz.org">Christopher Frantz</a>
 * @version 0.1
 *
 * @param <V> Input value element type
 */

public class Cluster {
/**
 * Maximum distance between points to be considered belonging to the same cluster
 */	
    private double eps; 
/**
 * Minimum number of points to be considered a cluster by DBSCAN
 */	
    private int minPts;
/**
 * List of points to be used by DBSCAN algorithm for clustering
 */
    private ArrayList<GPSCoord> inputValues;;
/**
 * List to remember the visited points in the DBSCAN algorithm
 */
    private HashSet<GPSCoord> visitedPoints = new HashSet<GPSCoord>();
/**
 *Constructor to initialize our instance variables
 *@param inputValues is the list of points to be used by DBSCAN algorithm for clustering
 *@param minPts is the minumum number of points to be considered a cluster by DBSCAN
 *@param eps is the maximum distance between points to be considered belonging to the same cluster
 */	
    public Cluster(ArrayList<GPSCoord> inputValues, int minPts, double eps) throws DBSCANClusteringException {
		
		this.inputValues = inputValues;
        this.minPts = minPts;
		this.eps = eps;

    }
	
/**
 * This method calculates the euclidian distance between two points
 *@param p1 is the original point
 *@param p2 is the candidate point for clustering, which we want to calculate it's distance to p1
 *@return the euclidian distance between two points in the parameters
 */
	
	public double calculateDistance(GPSCoord p1, GPSCoord p2){
		
		    return Math.sqrt((p2.getY() - p1.getY()) * (p2.getY() - p1.getY()) + (p2.getX() - p1.getX()) * (p2.getX() - p1.getX()));

	}

	/**
     * Determines the neighbours of a given input value.
     * 
     * @param inputValue Input value for which neighbours are to be determined
     * @return list of neighbours
     * @throws DBSCANClusteringDBSCANClusteringException 
     */
	 
    private ArrayList<GPSCoord> getNeighbours(final GPSCoord inputValue) throws DBSCANClusteringException {
        ArrayList<GPSCoord> neighbours = new ArrayList<GPSCoord>();
        for(int i=0; i<inputValues.size(); i++) {
            GPSCoord candidate = inputValues.get(i);
            if (calculateDistance(inputValue, candidate) <= eps) {
                neighbours.add(candidate);
            }
        }
        return neighbours;
    }

  /**
     * Merges the elements of the right collection to the left one and returns
     * the combination.
     * 
     * @param neighbours1 left collection
     * @param neighbours2 right collection
     * @return Modified left collection
     */
    private ArrayList<GPSCoord> mergeRightToLeftCollection( ArrayList<GPSCoord> neighbours1,  ArrayList<GPSCoord> neighbours2) {
        for (int i = 0; i < neighbours2.size(); i++) {
            GPSCoord tempPt = neighbours2.get(i);
            if (!neighbours1.contains(tempPt)) {
                neighbours1.add(tempPt);
            }
        }
        return neighbours1;
    }
	/**
     * Applies the clustering and returns a collection of clusters (i.e. a list
     * of lists of the respective cluster members).
     * 
     * @return
     * @throws DBSCANClusteringDBSCANClusteringException 
     */
    public ArrayList<ArrayList<GPSCoord>> performClustering() throws DBSCANClusteringException {

        ArrayList<ArrayList<GPSCoord>> resultList = new ArrayList<ArrayList<GPSCoord>>();
        visitedPoints.clear();

        ArrayList<GPSCoord> neighbours;
        int index = 0;
		

        while (inputValues.size() > index) {
            GPSCoord p = inputValues.get(index);
            if (!visitedPoints.contains(p)) {

                visitedPoints.add(p);
                neighbours = getNeighbours(p);

                if (neighbours.size() >= minPts) {

                    int ind = 0;
                    while (neighbours.size() > ind) {

                       GPSCoord r = neighbours.get(ind);
                        if (!visitedPoints.contains(r)) {

                            visitedPoints.add(r);
                            ArrayList<GPSCoord> individualNeighbours = getNeighbours(r);
                            if (individualNeighbours.size() >= minPts) {

                                neighbours = mergeRightToLeftCollection(
                                        neighbours,
                                        individualNeighbours);
                            }
                        }
                        ind++;
                    }
                    resultList.add(neighbours);
                }
            }
            index++;
        }
		

        return resultList;
    }

}