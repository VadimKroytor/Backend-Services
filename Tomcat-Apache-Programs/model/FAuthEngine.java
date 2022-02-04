
package model;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import tcpservices.FAuthTCPService;

public class FAuthEngine {
  private static FAuthEngine engine = null;
  private static PrintStream log = System.out;
	
	private FAuthEngine() {	}
	
	public static FAuthEngine getInstance() {
		if (engine == null) engine = new FAuthEngine();
		return engine;
	}
	
	public String runAuth(String username, String password, String host, String port) {
		// Host and Port from running FAuthTCPService.java	  
		// Try to connect to it
		
		System.out.println(host + " " + port);
		try (Socket idService = new Socket(InetAddress.getByName(host), Integer.parseInt(port)); 
				PrintStream req   = new PrintStream(idService.getOutputStream(), true); 
				Scanner res       = new Scanner(idService.getInputStream());
		) {
			log.printf("Connected to %s:%d\n", idService.getInetAddress(), idService.getPort());
		
			// Send request and get response 
			
			req.println(username + " " +  password);
			String output =  res.nextLine();

			System.out.println(output);
			return output;
			
		} catch (Exception e) {
			log.println(e);
			return "Failed to complete connection with Auth";
		} finally {
			log.printf("Disconnected from ID %s:%s\n", host, port);
		}
		
		
	}
}