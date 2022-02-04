package services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import g.Util;

/**
 * Given a username and password, this service authenticates these credentials
 * and returns “OK” or “FAILURE” accordingly. This service will be used later by
 * the shopping cart application to authenticate users. Authentication is done
 * by adding a long salt to the password; using a cryptographic function to hash
 * the result; and then repeating the process count times. The CLIENT table in
 * the Sqlite3 database stores the salt, count, and hash of each user. The table
 * adopts PBKDF2 (Password-Based Key Derivation Function 2) to perform the hash,
 * which is the current best practice. An API for computing PBKDF2 is provided
 * in the hr4413 library (in 4413/lib) through the following method in the
 * g.Util class:
 *
 * public static String hash(String password, String salt, int count) throws
 * Exception
 *
 * If the username is not found in the table, or if found but the computed hash
 * differs from the stored one, return “FAILURE”; otherwise, return “OK”.
 *
 * Input:
 * Nancy@November.me Far2Away
 *
 * Output:
 * OK
 */

public class Auth extends Thread {
    private static PrintStream log = System.out;

    private final String Home = System.getProperty("user.home");
    private final String URL = "jdbc:sqlite:" + Home + "/resources_and_libraries/pkg/sqlite/Models_R_US.db";
    private Socket client;

    private Auth(Socket client) {
        this.client = client;
    }

    private String doRequest(String request) {
        String[] token = request.split("\\s+");
        String username = token[0];
        String password = token[1];
        String correctHash = null;
        String generatedInputHash = null;
        String salt = "";
        int count = 0;
        String response = "FAILURE";

        
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
                
                return response;    
            }
            
        } catch (SQLException e) {
            log.println(e);
            return "SQL Error: " + e.getMessage();
        } finally {
            log.println("Disconnected from database.");
        }
    }

    public void run() {
        log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

        try (Socket client = this.client; // Makes sure that client is closed at end of try-statement.
                Scanner req = new Scanner(client.getInputStream());
                PrintStream res = new PrintStream(client.getOutputStream(), true);) {
            String response = "";
            String request = req.nextLine().trim();

            if (request.matches("^(\\S+)\\s+(\\S+)$")) {
                response = doRequest(request);
            } else {
                response = "Input was not correctly provided." + " Input must be in the form of a "
                        + "username and password seperated by " + "a space.";
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
        InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
        try (ServerSocket server = new ServerSocket(port, 0, host)) {
            log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
            while (true) {
                Socket client = server.accept();

                (new Auth(client)).start();
            }
        }
    }
}









