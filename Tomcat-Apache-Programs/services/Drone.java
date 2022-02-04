package services;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.GeoWebEngine;
import model.LocEngine;

/**
 * Servlet implementation class Drone
 * 
 * Given a street address (partial or complete) anywhere on Earth as a GET
 * parameter, named location, this service returns in the HTTP payload a JSON
 * object representing the address’s specs; most importantly, its latitude and
 * longitude. Use the MapQuest’s API
 * (http://www.mapquestapi.com/geocoding/v1/address?) and supply the address and
 * your API key to perform this lookup and capture the latLng element in the
 * returning JSON (see below). If MapQuest returns multiple results, return the
 * first location’s latitude and longitude.
 * 
 * /////////////////////////////////EXAMPLE/////////////////////////////////////
 * 1. Run Geo.java and put the host and port number in the web.xml file specified for the Drone servlet. Place your MapQuest Key in the web.xml as well. 
 * 2. Run Drone.java. 
 * 3. Paste the following into a web browser: >
 * http://localhost:4413/B/Drone?source=4700 Keele Street, Toronto&destination=Steeles Ave West at North West Gate East Side, Toronto
 * 
 * Output: > The estimated delivery time is: 0.5546044 minutes.
 * /////////////////////////////////////////////////////////////////////////////
 * 
 * /////////////////////////////////EXAMPLE/////////////////////////////////////
 * 1. Run Geo.java and put the host and port number in the web.xml file specified for the Drone servlet. Put your MapQuest Key in the web.xml as well. 
 * 2. Run Drone.java. 
 * 3. Paste the following into a web browser: >
 * http://localhost:4413/B/Drone?source=4700 Keele Street, Toronto&destination=
 * 
 * Output: 
 * 
 * > Don't understand: 4700 Keele Street, Toronto 
 * /////////////////////////////////////////////////////////////////////////////
 */

@WebServlet(name = "Drone")
public class Drone extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Drone() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @throws IOException
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LocEngine locEngine = LocEngine.getInstance();
		GeoWebEngine geoWebEngine = GeoWebEngine.getInstance();
		Writer out = response.getWriter();
		PrintStream log = System.out;
		JsonParser parser = new JsonParser();
		String output = "";

		response.setContentType("text/plain");

		Map<String, String[]> parameters = request.getParameterMap();

		if (parameters.containsKey("source") && parameters.containsKey("destination")) {

			String mapQuestKey = getInitParameter("MapQuestKey");

			String source = request.getParameter("source");
			String destination = request.getParameter("destination");

			String geoHost = getInitParameter("geoAddress");
			String geoPort = getInitParameter("geoPort");

			HashMap<String, String> locResSource = new HashMap<>();
			HashMap<String, String> locResDestin = new HashMap<>();

			locResSource = locEngine.runLoc(source, mapQuestKey);
			locResDestin = locEngine.runLoc(destination, mapQuestKey);

			if (locResSource.get("lat").equals("null") || locResDestin.get("lng").equals("null")) {
				output += "Don't understand: " + source + " " + destination;
			} else {
				String latAndLngSource = locResSource.get("lat") + " " + locResSource.get("lng");
				String latAndLngDestin = locResDestin.get("lat") + " " + locResDestin.get("lng");

				String geoRequest = latAndLngSource + " " + latAndLngDestin;
				log.println(geoRequest);
				log.println(geoHost);
				log.println(geoPort);

				String geoRes = geoWebEngine.runGeo(geoRequest, geoHost, geoPort);
				// velocity = distance / time
				// time = distance/velocity

				double time = Double.parseDouble(geoRes) / 150;
				time *= 60;
				output += "The estimated delivery time is: " + time + " minutes.";
			}
			out.write(output);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
