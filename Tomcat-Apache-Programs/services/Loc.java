package services;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
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

import model.LocEngine;

/**
 * Servlet implementation class Loc
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
 * 1. Run Loc.java.
 * 2. Paste the following into a web browser:
 * > http://localhost:4413/B/Loc?location=4700+Keele+Street+Toronto
 * 
 * Output:
 * > { "lat": 43.775124, "lng": -79.494075 }
 * /////////////////////////////////////////////////////////////////////////////

 * /////////////////////////////////EXAMPLE/////////////////////////////////////
 * 1. Run Loc.java.
 * 2. Paste the following into a web browser:
 * > http://localhost:4413/B/loc?location=
 * 
 * Output:
 * > { "lat": null, "lng": null }
 * /////////////////////////////////////////////////////////////////////////////
 */


@WebServlet(name = "Loc")
public class Loc extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Loc() {
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

		LocEngine engine = LocEngine.getInstance();
		Writer out = response.getWriter();
		PrintStream log = System.out;
		JsonParser parser = new JsonParser();
		HashMap<String, String> locRes = new HashMap<>();
		String output = "";

		response.setContentType("text/plain");

		Map<String, String[]> parameters = request.getParameterMap();
		System.out.println(Arrays.toString(parameters.keySet().toArray()));
		log.println(request.getRequestURI());
		
		if (parameters.containsKey("location")) {
			log.println(request.getRequestURI());

			String mapQuestKey = getInitParameter("MapQuestKey");
			String location = request.getParameter("location");

			locRes = engine.runLoc(location, mapQuestKey);

			output += "{ \"lat\": " + locRes.get("lat") + ", \"lng\": " + locRes.get("lng") + " }";
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
