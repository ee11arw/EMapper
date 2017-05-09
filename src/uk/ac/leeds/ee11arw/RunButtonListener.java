/*
 * RunButtonListener.java 1.0 May 2017
 * 
 * This code is provided under the Academic Academic Free License v. 3.0.
 * For details, please see the http://www.opensource.org/licenses/AFL-3.0.
 */

package uk.ac.leeds.ee11arw; 

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Class to deal with the back end processes of the GUI button presses.
 * @author ee11arw
 */
public class RunButtonListener implements ActionListener {
	
	MapAlgorithm mapAlg = new MapAlgorithm();

	@Override
	public void actionPerformed(ActionEvent event) {
		//Clear button pressed
		if (event.getActionCommand() == "Clear")	{
			Graph graph = Grapher.getGraph();
			
			//clear previous route node styling
			for (Node node : graph)	{
				node.removeAttribute("ui.class");
			}
			//clear previous route edge styling
			for (Edge edge : graph.getEachEdge()) {
				//clear previous results
				edge.removeAttribute("ui.class");
			}
		}
		
		//Run button pressed
		if (event.getActionCommand() == "Run")	{
			if (MapperMain.getStartSysCombo() !=  MapperMain.getEndSysCombo())	{
				//Find shortest route using combo box outputs
				ArrayList<String> routeList	= mapAlg.aStarShortestPath(
						MapperMain.getStartSysCombo(), MapperMain.getEndSysCombo());
				//set shortest route for display in the GUI
				MapperMain.setRouteData(routeList);
				
				Graph graph = Grapher.getGraph();
				//variables to work out edge names to change their style
				String[] nodeIDs = new String[routeList.size()];
				String[] edgeIDsPossible = new String[(routeList.size() - 1) * 2];
				
				
				for (int j = 0; j < routeList.size(); j++)	{
					//Convert from System name to systemID which is used for node identification
					String NodeID = DbConnect.sysNametoSysID(routeList.get(j)).toString();
					
					//mark up new route with CSS route style class
					Node n = graph.getNode(NodeID);
					nodeIDs[j] = NodeID;
					n.addAttribute("ui.class", "route");
					
					/*
					//Route time calculations - incomplete
					if (j <= routeList.size())	{
						//get connecting node in sequence
						String NodeID2 = DbConnect.sysNametoSysID(routeList.get(j+1)).toString();
						Node n2 = graph.getNode(NodeID);
						//get xyz attribute data
						n.getAttribute("xyz");
					}
					*/
				}
				
				//calculate all possible edge names as only one edge will be between each node
				for (int k = 0; k < nodeIDs.length; k++)	{
					if (nodeIDs.length > k + 1)	{
						edgeIDsPossible[k] = 
								nodeIDs[k] + "_to_" + nodeIDs[k + 1];
						edgeIDsPossible[k + nodeIDs.length - 1] = 
								nodeIDs[k + 1] + "_to_" + nodeIDs[k];
					}
				}
				
				//iterate over edges
				for (Edge edge : graph.getEachEdge()) {
					//set joining edges on route to the route CSS style class
					if (Arrays.asList(edgeIDsPossible).contains(edge.getId()))	{
						edge.setAttribute("ui.class", "routeE");
					}
				}
				
				
			}	else	{
				//error catching user input if the start and end systems are the same
				ArrayList<String> sameSysError = new ArrayList<String>();
				sameSysError.add("Start and destination systems");
				sameSysError.add("are the same. Unable to pathfind");
				MapperMain.setRouteData(sameSysError);
			}
			
		}
	}

}
