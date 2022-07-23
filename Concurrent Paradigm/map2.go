package main

import (
	"encoding/csv"
	"fmt"
	"io"
	"math"
	"os"
	"runtime"
	"strconv"
	"sync"
	"time"
)

type GPScoord struct {
	lat  float64
	long float64
}

type LabelledGPScoord struct {
	GPScoord
	ID    int // point ID
	Label int // cluster ID
}

const N int = 20
const MinPts int = 5
const eps float64 = 0.0003
const filename string = "yellow_tripdata_2009-01-15_9h_21h_clean.csv"

func main() {

	start := time.Now()

	gps, minPt, maxPt := readCSVFile(filename)
	fmt.Printf("Number of points: %d\n", len(gps))

	minPt = GPScoord{40.7, -74.}
	maxPt = GPScoord{40.8, -73.93}

	// geographical limits
	fmt.Printf("SW:(%f , %f)\n", minPt.lat, minPt.long)
	fmt.Printf("NE:(%f , %f) \n\n", maxPt.lat, maxPt.long)

	// Parallel DBSCAN STEP 1.
	incx := (maxPt.long - minPt.long) / float64(N)
	incy := (maxPt.lat - minPt.lat) / float64(N)

	var grid [N][N][]LabelledGPScoord // a grid of GPScoord slices

	// Create the partition
	// triple loop! not very efficient, but easier to understand

	offsets := make(chan int, N*N)

	partitionSize := 0
	for j := 0; j < N; j++ {
		for i := 0; i < N; i++ {

			offsets <- i*10000000 + j*1000000

			for _, pt := range gps {

				// is it inside the expanded grid cell
				if (pt.long >= minPt.long+float64(i)*incx-eps) && (pt.long < minPt.long+float64(i+1)*incx+eps) && (pt.lat >= minPt.lat+float64(j)*incy-eps) && (pt.lat < minPt.lat+float64(j+1)*incy+eps) {
					grid[i][j] = append(grid[i][j], pt) // add the point to this slide
					partitionSize++
				}
			}
		}
	}


	// Parallel DBSCAN STEP 2.
	// Apply DBSCAN on each partition

	var consumerCount int = 200

	jobs := make(chan []LabelledGPScoord)

	var wg sync.WaitGroup

	wg.Add(consumerCount) 

	for i := 1; i <= consumerCount; i++ {
		go consume(jobs, &wg, i, offsets)
	}

	for j := 0; j < N; j++ { //producer
		for i := 0; i < N; i++ {

			jobs <- grid[i][j]
			//fmt.Println("job sent: ", i*10000000+j*1000000)
		}
	}

	close(jobs)

	wg.Wait() 

	// Parallel DBSCAN step 3.
	// merge clusters

	end := time.Now()
	fmt.Printf("\nExecution time: %s of %d points\n", end.Sub(start), partitionSize)
	fmt.Printf("Number of CPUs: %d", runtime.NumCPU())
}

func consume(jobs chan []LabelledGPScoord, wg *sync.WaitGroup, name int, offsets chan int) { //consumer

	for {
		j, more := <-jobs

		if more {
			fmt.Println("Job consumed by thread ", name) //to make sure it works
			DBSCAN(j, MinPts, eps, <-offsets)

		} else {
			wg.Done()
			return
		}
	}
}

//returns the ID of the LabelledGPScoord in the parameters
func (p LabelledGPScoord) getID() int {
	return p.ID
}

//returns the euclidian distance between two points

func (p1 LabelledGPScoord) getDistance(p2 LabelledGPScoord) float64 {

	return math.Sqrt(((p2.lat - p1.lat) * (p2.lat - p1.lat)) + ((p2.long - p1.long) * (p2.long - p1.long)))
}

// Applies DBSCAN algorithm on LabelledGPScoord points
// LabelledGPScoord: the slice of LabelledGPScoord points
// MinPts, eps: parameters for the DBSCAN algorithm
// offset: label of first cluster (also used to identify the cluster)
// returns number of clusters found

func DBSCAN(points []LabelledGPScoord, minDensity int, epsilon float64, offset int) int {
	var clusters [][]LabelledGPScoord
	var visited []int //this is integer because I store the ID's of the points since all points have a unique ID coming from readCSVFile() function. I didn't use a
	//HashSet because I think this is simpler.

	for _, point := range points {
		if !contains2(visited, point.getID()) {

			visited = append(visited, point.getID())
			neighbours := findNeighbours(point, points, epsilon)

			if len(neighbours) > minDensity {

				for _, pt := range neighbours {

					if !contains2(visited, (&pt).getID()) {

						visited = append(visited, (&pt).getID())

						var individualNeighbours []LabelledGPScoord = findNeighbours(pt, points, epsilon)

						if len(individualNeighbours) > minDensity {
							neighbours = merge(neighbours, individualNeighbours)
						}
					}
				}
				clusters = append(clusters, neighbours)
			}
		}
	}
	fmt.Printf("Partition %10d : [%4d,%6d]\n", offset, len(clusters), len(points))

	return len(clusters)

}

//Finds the neighbours from given array
//depends on Eps variable, which determines
//the distance limit from the point
func findNeighbours(point LabelledGPScoord, points []LabelledGPScoord, epsilon float64) []LabelledGPScoord {
	var neighbours2 []LabelledGPScoord
	for _, candidate := range points {
		if (&candidate).getID() != (&point).getID() && candidate.getDistance(point) <= epsilon {
			neighbours2 = append(neighbours2, candidate)
		}

	}
	return neighbours2
}

//Checks if the array in the parameter contains the value in the parameter (for LabelledGPScoord)
func contains(s []LabelledGPScoord, e LabelledGPScoord) bool { 
	for _, a := range s {
		if a.getID() == e.getID() {
			return true
		}

	}
	return false
}

//Checks if the array in the parameter contains the value in the parameter (for int)
func contains2(s []int, e int) bool {
	for _, a := range s {
		if a == e {
			return true
		}
	}
	return false
}

//to merge individual neighbours and neighbours
func merge(nb []LabelledGPScoord, in []LabelledGPScoord) []LabelledGPScoord {
	for _, pt := range in {
		if contains(nb, pt) == false {
			nb = append(nb, pt)
		}
	}
	return nb

}

// reads a csv file of trip records and returns a slice of the LabelledGPScoord of the pickup locations
// and the minimum and maximum GPS coordinates
func readCSVFile(filename string) (coords []LabelledGPScoord, minPt GPScoord, maxPt GPScoord) {

	coords = make([]LabelledGPScoord, 0, 5000)

	// open csv file
	src, err := os.Open(filename)
	defer src.Close()
	if err != nil {
		panic("File not found...")
	}

	// read and skip first line
	r := csv.NewReader(src)
	record, err := r.Read()
	if err != nil {
		panic("Empty file...")
	}

	minPt.long = 1000000.
	minPt.lat = 1000000.
	maxPt.long = -1000000.
	maxPt.lat = -1000000.

	var n int = 0

	for {
		// read line
		record, err = r.Read()

		// end of file?
		if err == io.EOF {
			break
		}

		if err != nil {
			panic("Invalid file format...")
		}

		// get lattitude
		lat, err := strconv.ParseFloat(record[9], 64)
		if err != nil {
			panic("Data format error (lat)...")
		}

		// is corner point?
		if lat > maxPt.lat {
			maxPt.lat = lat
		}
		if lat < minPt.lat {
			minPt.lat = lat
		}

		// get longitude
		long, err := strconv.ParseFloat(record[8], 64)
		if err != nil {
			panic("Data format error (long)...")
		}

		// is corner point?
		if long > maxPt.long {
			maxPt.long = long
		}

		if long < minPt.long {
			minPt.long = long
		}

		// add point to the slice
		n++
		pt := GPScoord{lat, long}
		coords = append(coords, LabelledGPScoord{pt, n, 0})
	}

	return coords, minPt, maxPt
}
