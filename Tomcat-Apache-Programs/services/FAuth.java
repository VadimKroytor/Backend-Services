
package services;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.FAuthEngine;

@WebServlet(name = "FAuth", urlPatterns = { "/FAuth" })

public class FAuth extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/*
	 * 
	 * This Federated Authentication service receives two URL-encoded parameters
	 * username and password and returns OK or FAILURE in a text/plain payload based
	 * on the authentication of the Auth service of Project A. In other words, FAuth
	 * does not do any authentication itself. Instead, it simply delegate to Auth by
	 * turning its URL parameters to a TCP request line and by turning the TCP
	 * response line to an HTTP response.
	 * 
	 * /////////////////////////////////EXAMPLE/////////////////////////////////////
	 * 
	 * 
	 * 1. Run FAuthTCPService.java (This is acting as a black box in this service)
	 * 2. Run FAuth.java (The ID and port are automatically stored and sent
	 * to this service (i.e. no need to run configurations and input ip and port
	 * into the arguments section. 3. Paste the following into a web browser: 
	 * > http://localhost:4413/B/FAuth?user=Nancy@November.me&pw=Far2Away
	 * 
	 * Output: > OK
	 * 
	 * /////////////////////////////////////////////////////////////////////////////
	 * 
	 */
	public FAuth() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// We want to return a comma delimited list of the searched up names
		FAuthEngine engine = FAuthEngine.getInstance();
		Writer out = response.getWriter();
		HttpSession session = request.getSession(true);

		// Set the response type
		response.setContentType("text/plain");

		Map<String, String[]> parameters = request.getParameterMap();

		// If given id
		if (parameters.containsKey("user") && parameters.containsKey("pw")) {
			// Get name from id
			String username = request.getParameter("user");
			String password = request.getParameter("pw");

			System.out.println(username);
			System.out.println(password);

			String authHost = getInitParameter("serviceAddress");
			String authPort = getInitParameter("servicePort");

			String output = engine.runAuth(username, password, authHost, authPort);

			out.write(output);
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}