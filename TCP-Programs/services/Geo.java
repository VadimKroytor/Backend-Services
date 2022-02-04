package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;


/*
 * Given two points on Earth, this service returns the geodesic distance between them.
 * Each point is specified using its latitude (t) and longitude (n)
 * expressed as signed decimal degrees with East of Greenwich being a negative longitude
 * and south of the Equator being a negative latitude. The distance, in kilometers,
 * between such points is determined by the equation seen on line 58 of Geo.java.
 * This website can be used to verify the calculations:
 * https://www.cqsrg.org/tools/GCDistance/

/////////////////////////////////////////EXAMPLE/////////////////////////////////////////

1. Run Geo.java

2. Input the following into the linux terminal (telnet followed by the host and port number
    gotten from the console after running Geo.java):
 > telnet 192.168.0.11 38065

3. The following output will be displayed:
 > Please input the first and second coordinates (in the form: a b c d where a is latitude and
 > b is longitude for the first coordinate and c is latitude and d is longitude for the second coordinate):

4. Input the four coordinates:
 > -25 150 -50 200

The following output will be displayed:
5083.44 kilometers

/////////////////////////////////////////////////////////////////////////////////////////
 */
public class Geo extends Thread {
    public static PrintStream Log = System.out;

    private Socket client;
    private static final Pattern isDouble = Pattern.compile("^[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?$");
    String doubleRegex = "^[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?$";
    private Geo(Socket client) {
        this.client = client;
    }

    public void run() {
        Log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

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
                    response += " kilometers";
                }
          
                else {
                    response = "Don't understand: " + request;
                }
            res.println(response);
        } catch (Exception e) {
            Log.println(e);
        } finally {
            Log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 0;
        InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
        try (ServerSocket server = new ServerSocket(port, 0, host)) {
            Log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
            while (true) {
                Socket client = server.accept();

                (new Geo(client)).start();
            }
        }
    }
}


