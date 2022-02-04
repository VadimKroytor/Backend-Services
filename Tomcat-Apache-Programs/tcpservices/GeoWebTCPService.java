package tcpservices;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GeoWebTCPService extends Thread {

	public static PrintStream log = System.out;

	private Socket client;

	public GeoWebTCPService(Socket client) {
		this.client = client;
	}

    public void run() {
        log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

        try (Socket _client = this.client; // Makes sure that client is closed at end of try-statement.
                Scanner req = new Scanner(client.getInputStream());
                PrintStream res = new PrintStream(client.getOutputStream(), true);) {
            String response = "";


            String request = req.nextLine();



            String doubleRegex = "[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?";

			Pattern properGeoInputFormat = Pattern
					.compile("^" + doubleRegex + "\\s" + doubleRegex + "\\s" + doubleRegex + "\\s" + doubleRegex + "$");
			
               
                if (properGeoInputFormat.matcher(request).matches()) {
                	 String[] coords = request.split(" ");
                    double firstLat = Double.parseDouble(coords[0]);
                    double firstLongit = Double.parseDouble(coords[1]);
                    double secondLat = Double.parseDouble(coords[2]);
                    double secondLongit = Double.parseDouble(coords[3]);
    
                    
                    firstLat *= (Math.PI / 180);
                    firstLongit *= (Math.PI / 180);
                    secondLat *= (Math.PI / 180);
                    secondLongit *= (Math.PI / 180);
    
                    double y = Math.cos(firstLat) * Math.cos(secondLat);
                    double x = Math.pow(Math.sin(((secondLat - firstLat) / 2)), 2);
                    x += y * Math.pow(Math.sin(((secondLongit - firstLongit) / 2)), 2);
    
                    double geoDist = 12742 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
                    //response = String.format("%.2f", geoDist) + " kilometers";
                    response = String.format("%f", geoDist);
                }
          
                else {
                    response = "Don't understand: " + request;
                }
            res.println(response);
        } catch (Exception e) {
            log.println(e);
        } finally {
            log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
        }
    }

	public static void main(String[] args) throws Exception {
		int port = 0;
		InetAddress host = InetAddress.getLocalHost();
		File serverLocator = new File("/tmp/" + GeoWebTCPService.class.getName());

		serverLocator.deleteOnExit();
		if (!serverLocator.exists()) {
			serverLocator.createNewFile();
		}

		try (ServerSocket server = new ServerSocket(port, 0, host)) {
			log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());

			// Write this service's Host and Port address to a file, so that
			// the VendorsEngine can automatically retrieve it.
			try (PrintStream out = new PrintStream(serverLocator, "UTF-8")) {
				out.printf("%s:%d\n", server.getInetAddress().getHostAddress(), server.getLocalPort());
			}

			while (true) {
				Socket client = server.accept();
				(new GeoWebTCPService(client)).start();
			}
		}
	}
}