/*
 * Grapher.java 1.0 May 2017
 * 
 * This code is provided under the Academic Academic Free License v. 3.0.
 * For details, please see the http://www.opensource.org/licenses/AFL-3.0.
 */

package uk.ac.leeds.ee11arw;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

/**
 * Class to handle the graph display and its population
 * @author ee11arw
 * @version 1.0
 */
public class Grapher {
	DbConnect dbconn = new DbConnect();
	private static Graph graph = new SingleGraph("EMapper");
	
	public ViewPanel getViewPanel()	{
		ViewPanel viewPanel = (ViewPanel) getView();
		return viewPanel;
	}
	
	/**
	 * Method to convert the graph to a View object for display
	 * @return graphstream View object of the graph
	 */
	public static View getView()	{
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false); //false indicates no JFrame
		return view;
	}
	
	/**
	 * Method to get the graph object that has been made by the grapher method
	 * @return graph
	 */
	public static Graph getGraph() 	{
		return graph;
	}

	/**
	 * Main method to populate and draw the graph display
	 */
	public void grapher() {		
		//Methods to populate the map variables from the database
		dbconn.dbSolarNames();
		dbconn.systemList();
		dbconn.dbSolarConnections();
		
		//add nodes to graph and set their coordinates
		for (int i = 0; i < dbconn.getSolarIdAsStrings().length; i++) {
			//add node to graph by system ID
			String sysID = dbconn.getSolarIdAsStrings()[i];
			int sysIDnum = Integer.parseInt(sysID);
			Node node = graph.addNode(sysID);
			
			//fetch x,y,z coordinates for each system
			float x = dbconn.getMapNodexyz().get(sysIDnum).get(0);
			float y = dbconn.getMapNodexyz().get(sysIDnum).get(1);
			float z = dbconn.getMapNodexyz().get(sysIDnum).get(2);
			//attribute the coordinates to the system. Originally x,y,z produces the map in a side-on fashion
			//so to achieve a top down version you must do x,z,y
			node.setAttribute("xyz", x,z,y);
			
			node.addAttribute("ui.label", dbconn.sysIDtoSysName(Integer.parseInt(sysID)));
		}
		
		//add edges to already existing nodes
		//iterating though hash map of system connections
		Iterator<Entry<Integer, ArrayList<Integer>>> it = dbconn.getMapConnections().entrySet().iterator();
		while (it.hasNext())	{
			Map.Entry<Integer, ArrayList<Integer>> pair = (Entry<Integer, ArrayList<Integer>>)it.next();
			//loop through array of destination systems
			for (int v = 0; v < pair.getValue().size(); v++)	{
				//System.out.println("Key = " + pair.getKey() + " val = " + pair.getValue().get(v));
				try {
					String source = pair.getKey().toString();
					String desti  = pair.getValue().get(v).toString();
					//Due to the way each system has details of each connection in the hash map, duplicate
					//connections possible, so we check if the nodes have a pre existing edge
					if (graph.getNode(source).hasEdgeBetween(desti) == false)	{
						graph.addEdge(source + "_to_" + desti, source, desti);
					}
					} catch (EdgeRejectedException e) {
						System.err.println(e);
					}
				
			}
			it.remove(); //avoids ConcurrentModificationException
		}
		
		//display UI & use style sheet
		graph.addAttribute("ui.stylesheet", "url(" + new File ("").getAbsolutePath() + "\\css\\styleSheet.css)");

		
		//use gs.ui instead of gs-core for full css support
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		//set quality parameters
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");			
	}
}
