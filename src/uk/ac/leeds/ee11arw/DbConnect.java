package uk.ac.leeds.ee11arw;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Class to handle the sqlite database connection and process the data into a usable fashion
 * @author ee11arw
 * @version 1.0
 */
public class DbConnect {
	
	/**
	 * Inner Class for storing XYZ coordinate data
	 * adapted from stackoverflow.com/questions/11721013/store-x-and-y-coordinates
	 */
	static class CoordXYZ	{
		private double X;
		private double Y;
		private double Z;
		
		public CoordXYZ()	{
			this(0.0, 0.0, 0.0);
		}
		public CoordXYZ(double X, double Y, double Z)	{
			this.X = X;
			this.Y = Y;
			this.Z = Z;
		}
		public double getX()	{
			return X;
		}
		public double getY()	{
			return Y;
		}
		public double getZ()	{
			return Z;
		}
	}
	/**
	 * Class for data encapsulation of source and destination XYZ.
	 * adapted from http://stackoverflow.com/questions/457629/how-to-return-multiple-objects-from-a-java-method
	 * @param <Source> as CoordXYZ object
	 * @param <Desti> as CoordXYZ object
	 */
	static class CoordXYZPair <Source,Desti>	{
		public final Source s;
		public final Desti d;
		
		public static <Source,Desti> CoordXYZPair<Source,Desti> makePair(Source s, Desti d)	{
			return new CoordXYZPair<Source,Desti>(s,d);
		}
		
		public CoordXYZPair(Source s, Desti d)	{
			this.s = s;
			this.d = d;
		}
	}
	
	//Solar system name - solar system ID (valid systems) BiMap for easy reverse lookup
	private static BiMap<Integer, String> mapNameIDs = HashBiMap.create();
	//Solar system ID - Region ID (All systems)
	private Map<Integer, Integer> mapSysIdRegion = new HashMap<Integer, Integer>();
	//Solar System ID - connected solar system IDs
	private Map<Integer, ArrayList<Integer>> mapConnections = new HashMap<Integer, ArrayList<Integer>>();
	//Solar system ID - x,y,z coordinates
	private Map<Integer, ArrayList<Float>> mapNodexyz = new HashMap<Integer, ArrayList<Float>>();
	//SystemIDs in string array
	private String[] solarIdAsStrings;
	//Source gateID to destination gateID
	private static Map<Integer, Integer> mapGateDestinations = new HashMap<Integer, Integer>();
	//Map to convert between gate item IDs and the system IDs they correspond to
	private static BiMap<Integer,Integer>	mapGatetoSysID = HashBiMap.create();
	
	//Get methods
	public Map<Integer, ArrayList<Float>> getMapNodexyz() {
		return mapNodexyz;
	}
	public String[] getSolarIdAsStrings()	{
		return solarIdAsStrings;
	}
	public Map<Integer, ArrayList<Integer>> getMapConnections() {
		return mapConnections;
	}
	public static BiMap<Integer, String> getMapNameIDs() {
		return mapNameIDs;
	}
	
	/**
	 * @return connection to the sqlite database file
	 */
	private static Connection connect()	{
		Connection conn = null;
		
		String dbfile = new File ("").getAbsolutePath();
		String url = "jdbc:sqlite:" + dbfile + "\\EData.db";
		
		try	{		
			conn = DriverManager.getConnection(url);
		} catch (SQLException e)	{
			System.out.println(e.getMessage());
		}
		return conn;
	}
	
	/**
	 * Internal method to populate the system ID to Region ID hash map used for validating systems
	 * in later methods.
	 */
	private void dbregionIDs()	{
		String sql = "SELECT solarSystemID, regionID FROM mapSolarSystems";
		
		//Connection resources in try block to be closed by java automatically without finally .close()
		try (Connection conn = connect();
			 Statement stmt  = conn.createStatement();
			 ResultSet rs    = stmt.executeQuery(sql))	{
			
			while(rs.next())	{
				mapSysIdRegion.put(rs.getInt("solarSystemID"), rs.getInt("regionID"));
			}
		}	catch	(SQLException e)	{
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Populates the instance variable hash map of IDs and System names
	 * Populates the SystemIDs and their x,y,z coordinate in a instance variable
	 */
	public void dbSolarNames()	{
		dbregionIDs();
		String sql = "SELECT * FROM mapSolarSystems";
		
		//Connection resources in try block to be closed by java automatically without finally .close()
		try (Connection conn = connect();
			 Statement stmt  = conn.createStatement();
			 ResultSet rs    = stmt.executeQuery(sql))	{
			
			//loop through result set from sql query
			while (rs.next())	{
				int sysID = rs.getInt("solarSystemID");

				if (validSolarSystem(sysID)) {
					getMapNameIDs().put(rs.getInt("solarSystemID"), rs.getString("solarSystemName"));
					
					ArrayList<Float> node_xyz = new ArrayList<Float>();
					node_xyz.add(rs.getFloat("x"));
					node_xyz.add(rs.getFloat("y"));
					node_xyz.add(rs.getFloat("z"));
					mapNodexyz.put(sysID, node_xyz);
				}
			}
		}	catch	(SQLException e)	{
			System.out.println(e.getMessage());
			}
	}
	
	/**
	 * Method to convert the systemIDs and systemNames to string arrays
	 * Populates the solarIdAsStrings string 1d array
	 * Populates the solarNamesAsStrings string 1d array
	 */
	public void systemList()	{
		//Sets up solarIdAsStrings
		Set<Integer> solarIDs = getMapNameIDs().keySet();
		solarIdAsStrings = new String[solarIDs.size()];
		//convert integer list to string list
		int count = 0;
		for(Integer integer : solarIDs) {
			solarIdAsStrings[count] = integer.toString();
			count += 1;
		}
	}
	
	/**
	 * Method to set up the hash map for connections(edges) to each solar system node
	 */
	public void dbSolarConnections()	{
		String sql = "SELECT fromSolarSystemID, toSolarSystemID FROM mapSolarSystemJumps";
		
		//Connection resources in try block to be closed by java automatically without finally .close()
		try (Connection conn = connect();
			 Statement stmt  = conn.createStatement();
			 ResultSet rs	 = stmt.executeQuery(sql))	{
			
			//loop through result set from sql query
			while (rs.next())	{
				int sourceID = rs.getInt("fromSolarSystemID");
				int destiID = rs.getInt("toSolarSystemID");
				
				
				//check if valid system before adding to connections map, valid and non-valid regions do not
				//connect so there is no need to also check the destination system
				if (validSolarSystem(sourceID))	{
					//if key system exists, get connections array, add new connections, save array as new value
					if (mapConnections.containsKey(sourceID))	{
						ArrayList<Integer> solConnects = new ArrayList<Integer>();
						solConnects = mapConnections.get(sourceID);
						solConnects.add(destiID);
						mapConnections.put(sourceID, solConnects);
					//if key system doesn't exist, make new array, add new connection, save array as value
					}	else {
						ArrayList<Integer> solConnects = new ArrayList<Integer>();
						solConnects.add(destiID);
						mapConnections.put(sourceID, solConnects);
					}
				}
			}
		}	catch (SQLException e)	{
			System.out.println(e.getMessage());
		}	
	}
	
	/**
	 * TODO incomplete in functionality
	 * Method to take in two adjacent systems and return an XYZ pair of the internal gate coordinates
	 * This can then be used in the time calculations to work out approximate time for travel.
	 * @param sSys - Start system
	 * @param dSys - Destination system/gate - must be adjacent in network.
	 * @return null
	 */
	public static CoordXYZPair<CoordXYZ, CoordXYZ> dbGateCoords(String sSys, String dSys)	{
		//if first time run populates both the gateID to destination GateID map and also the GateID
		//to SysID BiMap
		if (mapGateDestinations.isEmpty())	{
			dbpopulateGateDestMap();
		}
		
		String sql = "SELECT itemID, x, y, z "
				+ "FROM mapDenormalize "
				+ "WHERE groupID=10"
				+ " AND solarSystemID=" + sSys;
		
		//Connection resources in try block to be closed by java automatically without finally .close()
		try (Connection conn = connect();
			 Statement stmt  = conn.createStatement();
			 ResultSet rs    = stmt.executeQuery(sql))	{
			
			int destiGateIDinput = mapGatetoSysID.inverse().get(dSys);
			
			while (rs.next())	{
				int destiGateIDdb = mapGateDestinations.get(rs.getInt("itemID"));
				System.out.println(destiGateIDdb);
				if (destiGateIDdb == destiGateIDinput)	{
					System.out.print("testpoint1");
				}
			}
		}	catch (SQLException e)	{
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Method to populate the gateID to gateID destination map and the gateID to SysID for lookup
	 */
	public static void dbpopulateGateDestMap()	{
		String sql = "SELECT * FROM mapJumps";
		//first the gateID to gateID destination map
		//Connection resources in try block to be closed by java automatically without finally .close()
		try (Connection conn = connect();
			 Statement stmt  = conn.createStatement();
			 ResultSet rs    = stmt.executeQuery(sql))	{
			while (rs.next())	{
				mapGateDestinations.put(rs.getInt("stargateID"), rs.getInt("destinationID"));
			}
			rs.close();
		}	catch (SQLException e)	{
			System.out.println(e.getMessage());
		}
		//second the gateID to SysID BiMap
		String sql2 = "SELECT itemID, solarSystemID FROM mapDenormalize WHERE groupID=10";
		try (Connection conn = connect();
			 Statement stmt  = conn.createStatement();
			 ResultSet rs    = stmt.executeQuery(sql2))	{
			while (rs.next())	{
				mapGatetoSysID.put(rs.getInt("itemID"), rs.getInt("solarSystemID"));
			}
		}	catch (SQLException e)	{
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Method to calculate the internal distance within a solar system between the star gates that
	 * connect it to the next solar system within the universe network.
	 * Also converts from the units from Astronomical Units (AU) to meters
	 * Math adapted from :
	 * https://www.reddit.com/r/evetech/comments/2p7qa5/how_to_get_the_distance_in_au_between_two/cnuh9o7/
	 * @param sourceXYZ as CoordXYZ - starting star gate
	 * @param destiXYZ as CoordXYZ - destination star gate
	 * @return long distance in meters
	 */
	public long auDistanceCalc(CoordXYZ sourceXYZ, CoordXYZ destiXYZ)	{
		double sX = sourceXYZ.getX(), sY = sourceXYZ.getY(), sZ = sourceXYZ.getZ();
		double dX = destiXYZ.getX(), dY = destiXYZ.getY(), dZ = destiXYZ.getZ();
		return (long) Math.ceil(Math.sqrt(
				Math.pow((sX - dX), 2) +
				Math.pow((sY - dY), 2) +
				Math.pow((sZ - dZ), 2)) / 9460000000000000L);
	}
	
	/**
	 * Method to calculate the time taken to warp a specified distance given the warp speed
	 * Uses CCP's equation for warp speed travel to include acceleration, cruising and deceleration times
	 * community.eveonline.com/news/dev-blogs/warp-drive-active
	 * math adapted from:
	 * wiki.eveuniversity.org/Warp_time_calculation
	 * @param distance in meters
	 * @param warpSpeed of ship in AU/s
	 * @param subWarpSpeed of ship in m/s
	 * @return double, time in seconds travel took
	 */
	public double warpTimeCalc(long distance, double warpSpeed, int subWarpSpeed)	{
		//calculate required parameters
		double deceleration = Math.min(warpSpeed / 3, 2.0);
		double warpDropoutSpeed = Math.min(subWarpSpeed / 2, 100);
		double warpSpeedInMs = warpSpeed * 149597870700L;
		double minDist = warpSpeed + deceleration;
		
		//local variable initialisation
		double cruiseTime = 0;
		
		if (minDist > distance)	{
			warpSpeedInMs = distance * warpSpeed * deceleration / (warpSpeed + deceleration);
		}	else	{
			cruiseTime = (distance - minDist) / warpSpeedInMs;
		}
		
		double accTime = Math.log(warpSpeedInMs / warpSpeed) / warpSpeed;
		double decTime = Math.log(warpSpeedInMs / warpDropoutSpeed) / deceleration;
		
		return cruiseTime + accTime + decTime;
	}
	
	/**
	 * Method to eliminate inaccessible development systems in the static data
	 * @param systemID, integer of the systemID value
	 * @return boolean, true if valid system
	 */
	private boolean validSolarSystem (int systemID)	{
		//Non-accessible developmental regions
		//Regions: UUA-F4, A821-A, J7HZ-F
		int[] devRegionIDs = {10000004, 10000019, 10000017};
		int regID = mapSysIdRegion.get(systemID);
		//if ID greater than 31000000 system 'wormhole' space and unmappable
		if (systemID > 31000000)	{
			return false;
		} else {
			return !IntStream.of(devRegionIDs).anyMatch( x -> x == regID);
			}
	}
	
	/**
	 * Method to convert from system ID to system name
	 * @param sysID integer system id
	 * @return string system name, null if system id not found
	 */
	public String sysIDtoSysName(int sysID)	{
		if (getMapNameIDs().containsKey(sysID))	{
			return getMapNameIDs().get(sysID);
		}
		else {
			return null;
		}
	}
	/**
	 * Method to convert from system name to system ID
	 * @param sysName string system name
	 * @return Integer system id, null if system name not found
	 */
	public static Integer sysNametoSysID(String sysName)	{
		if (getMapNameIDs().containsValue(sysName))	{
			return getMapNameIDs().inverse().get(sysName);
		}
		else {
			return null;
		}
	}
}