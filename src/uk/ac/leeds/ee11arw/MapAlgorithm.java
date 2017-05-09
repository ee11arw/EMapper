/*
 * MapAlgorithm.java 1.0 May 2017
 * 
 * This code is provided under the Academic Academic Free License v. 3.0.
 * For details, please see the http://www.opensource.org/licenses/AFL-3.0.
 */

package uk.ac.leeds.ee11arw;

import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import uk.ac.leeds.ee11arw.Grapher;
import uk.ac.leeds.ee11arw.DbConnect;

/**
 * Class setup to contain methods for running algorithms on the map 
 * @author ee11arw
 */
public class MapAlgorithm {
	
	DbConnect dbconn = new DbConnect();
	
	/**
	 * Method to take in two system names and output the shortest route of systems to
	 * take. Uses A* path finding algorithm.
	 * @param sysStart String of system name to start path from
	 * @param sysEnd String of system name to end path from
	 * @return Array list of the systems for the shortest path
	 */
	public ArrayList<String> aStarShortestPath(String sysStart, String sysEnd)	{
		//run A* path finding algorithm
		Graph graph = Grapher.getGraph();
		AStar astar = new AStar(graph);
		try {
			astar.compute(DbConnect.sysNametoSysID(sysStart).toString(),
							DbConnect.sysNametoSysID(sysEnd).toString());
			} catch (Exception e1) {
				//System parameter does not exist, likely user input error
				return null;
			}
		
		//fetch the shortest route and use an iterator to convert it into a System Name array
		Path path	= astar.getShortestPath();
		Iterator<Node> pathIt = path.getNodeIterator();
		
		ArrayList<String> pathArrayList = new ArrayList<String>();
		while (pathIt.hasNext())	{
			try {
				int sys = Integer.parseInt(pathIt.next().getId());
				pathArrayList.add(dbconn.sysIDtoSysName(sys));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pathArrayList;
	}
}
