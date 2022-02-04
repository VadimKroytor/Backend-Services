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

public class FAuthTCPService extends Thread {

	public static PrintStream log = System.out;
	private final String Home = System.getProperty("user.home");
    private final String URL = "jdbc:sqlite:" + Home + "/resources_and_libraries/pkg/sqlite/Models_R_US.db";
    private Socket client;
	
	public FAuthTCPService(Socket client) { 
	  this.client = client;
	}

	public void run() {
		log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

    try (
      Socket client = this.client; 
  		Scanner req = new Scanner(client.getInputStream()); 
  		PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {

    	String request = req.nextLine();
    	String response = "FAILURE";
    	
        if (request.matches("^(\\S+)\\s+(\\S+)$")) {
        	
        	
            String[] token = request.split("\\s+");
            String username = token[0];
            String password = token[1];
            String correctHash = null;
            String generatedInputHash = null;
            String salt = "";
            int count = 0;

    	    // Implement: 
    	    // Given id, find the corresponding name in the database
    	    // HR.vendor database (derby) has id, name
    	    
    	    // Check input is a number (cause id can only be number)
    	    
            try (Connection connection = DriverManager.getConnection(URL)) {
                log.printf("Connected to database: %s\n", connection.getMetaData().getURL());

                String query = "SELECT * FROM Client WHERE name = ?";
                // Example: SELECT * FROM HR.Product WHERE id = 'S10_1678';

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, username);

                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            correctHash = rs.getString("hash");
                            salt = rs.getString("salt");
                            count = rs.getInt("count");
                        }
                        generatedInputHash = g.Util.hash(password, salt, count);
                        System.out.println("generatedInputHash: " + generatedInputHash);
                        System.out.println("correctHash: " + correctHash);

                        if (generatedInputHash.equals(correctHash) && correctHash != null) {
                            response = "OK";
                        }
                    }
                    catch (Exception e) {
                        log.println(e);
                    }                    
                }
                
            } catch (SQLException e) {
                log.println(e);
                System.out.println("SQL Error: " + e.getMessage());
            } finally {
                log.println("Disconnected from database.");
            }
        } else {
            response = "Input was not correctly provided." + " Input must be in the form of a "
                    + "username and password seperated by " + "a space.";
        }
        res.println(response);
    } catch (Exception e) {
        log.print(e);
    } finally {
      log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
    }
	}

	public static void main(String[] args) throws Exception {
		int port = 0;
    InetAddress host = InetAddress.getLocalHost();

    try (ServerSocket server = new ServerSocket(port, 0, host)) {
    	log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());


    	while (true) {
    		Socket client = server.accept();
    		(new FAuthTCPService(client)).start();
    	}
    }
	}
}