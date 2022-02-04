
package services;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.GeoWebEngine;


@WebServlet(name = "GeoWeb",
			urlPatterns = { "/GeoWeb" }
			)
public class GeoWeb extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
/*

This Federated Authentication service receives two URL-encoded parameters username and password and returns OK 
or FAILURE in a text/plain payload based on the authentication of the Auth service of Project A. In other words, 
FAuth does not do any authentication itself. Instead, it simply delegate to Auth by turning its URL parameters to 
a TCP request line and by turning the TCP response line to an HTTP response.

////////////////////////////////////////////////////EXAMPLE///////////////////////////////////////////////////////

1. Run FAuthTCPService.java (This is acting as a black box in this service)
2. Run GeoWebService.java (The ID and port are automatically stored and sent to this service (i.e. no need to run 
	configurations and input ip and port into the arguments section.
3. Paste the following into a browser: 
 > http://localhost:4413/B/GeoWeb?lat=a&lng=2
 
 Output:
 > Don't understand: a 2
 
 Input:
 > http://localhost:4413/B/GeoWeb?lat=1&lng=2
 
 Output:
 > RECEIVED
 
 Input:
 > http://localhost:4413/B/GeoWeb?lat=1&lng=what
 
 Output:
 > Don't understand: 1 2 1 what
 
 Input:
 > http://localhost:4413/B/GeoWeb?lat=3&lng=4
 
 Output:
 > The distance from (1, 2) to (3, 4) is: 314.402951 km
 
  Input:
 > http://localhost:4413/B/GeoWeb?lat=5&lng=6
 
 Output:
 > The distance from (3, 4) to (5, 6) is: 314.115809 km
 

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 */
  public GeoWeb() {
    super();
  }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// We want to return a comma delimited list of the searched up names
		GeoWebEngine engine = GeoWebEngine.getInstance();
		Writer out = response.getWriter();
		HttpSession session = request.getSession(true);
		
		// Set the response type
		response.setContentType("text/plain");

		
		// If session doesn't have name, set it to empty string	
		if (session.getAttribute("coord") ==  null) {
			session.setAttribute("coord", "");
		}
		String sessionStoredCoords = (String) session.getAttribute("coord");

		Map<String, String[]> parameters = request.getParameterMap();

		String output = "";
		// If given id
		if (parameters.containsKey("lat") && parameters.containsKey("lng")) {
			// Get name from id
			
			String geoHost = getInitParameter("serviceAddress");
			String geoPort = getInitParameter("servicePort");
			
			String lat = request.getParameter("lat");
			String longit = request.getParameter("lng");
			String coords = "";
			
			
			String doubleRegex = "[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?";

			Pattern properFormatForLatAndLng = Pattern.compile("^" + doubleRegex + "\\s" + doubleRegex + "$");
			
			
					
			if (sessionStoredCoords.equals("")) {
				
				if (properFormatForLatAndLng.matcher(lat + " " + longit).matches()) {
						
					session.setAttribute("coord", lat + " " + longit);
					out.write("RECEIVED");
				}
				else {
					out.write("Don't understand: " + lat + " " + longit);
				}
			}
			else {
				
				coords += sessionStoredCoords + " " + lat + " " + longit;
				System.out.println(coords);
				String geoResp = engine.runGeo(coords, geoHost, geoPort);	
				
				
				if (geoResp.equals("Don't understand: " + coords)) {
					output = geoResp;
				}
				else {
					String[] sesStoreCoordsComaDelim = sessionStoredCoords.split(" ");
					
					output = "The distance from (" + sesStoreCoordsComaDelim[0] + ", " + sesStoreCoordsComaDelim[1] + 
							") to (" + lat + ", " + longit + ") is: " + geoResp + " km";
					session.setAttribute("coord", lat + " " + longit);
				}
			
				out.write(output);
			}
			
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}