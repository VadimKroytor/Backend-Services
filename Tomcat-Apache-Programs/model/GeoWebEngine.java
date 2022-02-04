
package model;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import tcpservices.GeoWebTCPService;

public class GeoWebEngine {
	private static GeoWebEngine engine = null;
	private static PrintStream log = System.out;

	private GeoWebEngine() {
	}

	public static GeoWebEngine getInstance() {
		if (engine == null)
			engine = new GeoWebEngine();
		return engine;
	}

	public String runGeo(String coords, String host, String port) {
		// Host and Port from running GeoWebTCPService.java

		// Try to connect to it

		try (Socket idService = new Socket(InetAddress.getByName(host), Integer.parseInt(port));
				PrintStream req = new PrintStream(idService.getOutputStream(), true);
				Scanner res = new Scanner(idService.getInputStream());) {
			log.printf("Connected to %s:%d\n", idService.getInetAddress(), idService.getPort());

			// Send request and get response

			req.println(coords);
			
			String geoResp = res.nextLine();
			System.out.println(geoResp);
			return geoResp;
		} catch (Exception e) {
			log.println(e);
			return "Failed to complete connection with Geo";
		} finally {
			log.printf("Disconnected from ID %s:%s\n", host, port);
		}
	}
}