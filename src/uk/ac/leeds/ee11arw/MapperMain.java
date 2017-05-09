
/*
 * MapperMain.java 1.0 May 2017
 * 
 * This code is provided under the Academic Academic Free License v. 3.0.
 * For details, please see the http://www.opensource.org/licenses/AFL-3.0.
 */
package uk.ac.leeds.ee11arw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Camera;

import uk.ac.leeds.ee11arw.DbConnect;
import uk.ac.leeds.ee11arw.Grapher;

/**
 * Main class for the EMapper application to allow for route visualisation and planning
 * for the 'New Eden' Universe of Eve Online.
 * 
 * @author ee11arw
 * @version 1.0
 * @since April 2017
 */
@SuppressWarnings("serial")
public class MapperMain extends JFrame{
	//instance variables
	//DbConnect dbconn = new DbConnect();	
	protected Grapher grapher = new Grapher();
	protected Point mousePoint;
	
	//class member variables for accessing selected items in actionListener
	private static JComboBox<String> startSysCombo;
	private static JComboBox<String> endSysCombo;
	//string array for displaying the path finder output
	private static DefaultListModel<String> routeData;
	private static JList<String> routeList;
	
	//get methods for accessing combo box selected time
	public static String getStartSysCombo()	{
		return (String)startSysCombo.getSelectedItem();
	}
	public static String getEndSysCombo()	{
		return (String)endSysCombo.getSelectedItem();
	}
	/**
	 * Method to set instance variable routeData
	 * Converts from ArrayList into DefaultListModel
	 * @param x ArrayList
	 */
	public static void setRouteData(ArrayList<String> x)	{
		//resets to clear any previous results
		routeData.clear();
		//add elements from ArrayList into ListModel
		for (int i = 0; i < x.size(); i++)	{
			routeData.addElement(x.get(i));
		}
		//update routeList with new data for display
		routeList.setModel(routeData);
	}

	
	public MapperMain()	{
		//super("Emapper");
		//draw graph and populate it with nodes and edges from the static data
		grapher.grapher();
		
		//GUI setup
		setMinimumSize(new Dimension(1200, 900));
		setTitle("EMapper");
		getContentPane().setBackground(Color.white);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		//layout
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().setLayout(gridbag);
		//generic layout
		c.anchor = GridBagConstraints.NORTHWEST;
	    c.insets.top = 5;
	    c.insets.bottom = 5;
	    c.insets.left = 5;
	    c.insets.right = 5;
		
		//fetch graph view and add it to pane
		DefaultView MapView = (DefaultView) Grapher.getView();
		MapView.setPreferredSize(new Dimension(500,500));
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 5;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		/*
		 * Add mouse wheel zoom in hidden inner class
		 */
		MapView.addMouseWheelListener(new MouseWheelListener()	{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)	{
				//int notches = e.getWheelRotation();
				Camera cam = MapView.getCamera();
				if (e.getWheelRotation() < 0.0)	{
					//set max zoom
					if (cam.getViewPercent() >= 0.05)	{
						cam.setViewPercent(cam.getViewPercent() - 0.03);
					}
				}	else	{
					if (cam.getViewPercent() <= 0.95)	{
						//set minimum zoom as not to invert map view
						cam.setViewPercent(cam.getViewPercent() + 0.03);
					}
				}
			}
		});

		getContentPane().add(MapView, c);
		
		//add labels for combo boxes & route output
		JLabel labelStart = new JLabel("Start: ");
		c.gridx = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.fill = GridBagConstraints.NONE;
		getContentPane().add(labelStart, c);
		JLabel labelEnd = new JLabel("Destination: ");
		c.gridy = 1;
		getContentPane().add(labelEnd, c);
		JLabel labelRoute= new JLabel("Route: ");
		c.gridy = 4;
		getContentPane().add(labelRoute, c);
		
		//add combo boxes for starting system and destination system
		String[] ComboSystemNames = DbConnect.getMapNameIDs().values().toArray(new String[0]);
		startSysCombo = new JComboBox<String>(ComboSystemNames);
		endSysCombo = new JComboBox<String>(ComboSystemNames);
		AutoCompletion.enable(startSysCombo);
		AutoCompletion.enable(endSysCombo);
		startSysCombo.setPreferredSize(new Dimension(200,20));
		endSysCombo.setPreferredSize(new Dimension(200,20));
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 2;
		getContentPane().add(startSysCombo, c);
		c.gridy = 1;
		getContentPane().add(endSysCombo, c);
		
		//add run path finder button
		JButton runButton = new JButton("Run");
		runButton.addActionListener(new RunButtonListener());
		c.gridwidth = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(runButton, c);
		
		//add clear path finder button
		JButton clrButton = new JButton("Clear");
		clrButton.addActionListener(new RunButtonListener());
		c.gridx = 3;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(clrButton, c);
		
		//add separator between inputs and output
		JSeparator seperator = new JSeparator(JSeparator.HORIZONTAL);
		seperator.setPreferredSize(new Dimension(250,1));
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 3;
		getContentPane().add(seperator, c);
		
		//Menu bar
		JMenuBar menubar = new JMenuBar();
		JMenu aboutMenu = new JMenu("About");
		JMenuItem infoItem = new JMenuItem("Information");
		infoItem.addActionListener(new AboutDialog());
		aboutMenu.add(infoItem);
		menubar.add(aboutMenu);
		
		setJMenuBar(menubar);		
		
		//add JList for displaying the route output, inside a JScrollPane
		//if no route has been calculated just display 'no results'
		if (routeData == null)	{
			routeData = new DefaultListModel<String>();
			routeData.addElement("No Results");
			
		}
		routeList = new JList<String>(routeData);
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 2;
		c.gridy = 4;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(routeList), c);
		
		//set as visible
		setVisible(true);
		
		//TODO implement new mousemanager to disable node dragging and allow map movement by dragging
		//TODO add route time calculations
	}
	
	/**
	 * Class for the information dialog box
	 * @author ee11arw
	 */
	class AboutDialog implements ActionListener	{
		public void actionPerformed(ActionEvent e)	{
			String information = "<html><b><u>EMAPPER</u></b><p>An Eve Online solar system explorer<br><br>"
					+"Author: ee11arw - 200618378 <br>Platform: Java 1.8.0 <br>Build Date: May 2017"
					+"<br><br>This application is designed as a standalone out of game tool to allow for quick route finding"
					+"<br>across the new eden universe of Eve Online along with providing travel time predictions given"
					+"<br>ship alignment, speed and internal  system gate distances."
					+"<br><br>For map navigation mouse scroll or PgUp/PgDn can be used to zoom, and the arrow keys for"
					+"<br>panning."
					+"<br><br><u>Acknowledgements</u></p>"
					+"<li>Emapper uses EVEs Static Data Export(SDE) made avaliable by CCP"
					+"<br>https://developers.eveonline.com/resource/resources"
					+"<li>The SDE has been kindly reformatted to SQLite which is used for accessability"
					+"<br>https://www.fuzzwork.co.uk/dump/"
					+"<li>Thomas Bierhance for the AutoCompletion combobox class</html>";
			JOptionPane.showMessageDialog(MapperMain.this,
					information,
					"About Emapper", getDefaultCloseOperation());
		}
	}
	
	public static void main(String args[])	{
		new MapperMain();
	}
}
