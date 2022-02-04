package services;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
 Servlet implementation class OAuth
///////////EXAMPLE///////////////

http://localhost:4413/B/OAuth


 */
@WebServlet(name = "OAuth")
public class OAuth extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OAuth() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Writer out = response.getWriter();
		HttpSession session = request.getSession(true);

		// Set the response type
		response.setContentType("text/plain");

		Map<String, String[]> parameters = request.getParameterMap();

		if (parameters.isEmpty()) {

			response.sendRedirect("https://www.eecs.yorku.ca/~roumani/servers/auth/oauth.cgi?back="
					// + URLEncoder.encode(request.getRequestURI(), "UTF-8"));
					// + URLEncoder.encode("http://localhost:4413/B/OAuth", "UTF-8"));
					+ request.getRequestURL());

			// request.
				System.out.println("request content length: " + request.getContentLength());
				Map<String, String[]> newReqParam = request.getParameterMap();

				System.out.println(request.getQueryString() + " why is this not workings");

				if (newReqParam.containsKey("user")) {
					out.write("i am confusion");
				}
				System.out.println(Arrays.toString(newReqParam.keySet().toArray()));
				System.out.println(request.getParameter("user"));

		}
		out.write("what do i do?");
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
