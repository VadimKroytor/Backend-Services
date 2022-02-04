package services;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.google.gson.Gson;

/**
 * This “pedagogical” service sheds light on the challenges involved in building
 * an API Gateway, such as service registration, discovery, and orchestration.
 * It's client is a browser and its URL is: http://host:port/SRV?p1=v1&p2=v2... 
 * where SRV is either Geo or Auth and where p1, p2, ... are the names of
 * parameters of the SRV service requires and v1, v2, ... are the values
 * corresponding to each of those parameters.
 *
 * The job of this service is to discover the needed service (extract its name
 * from the request and find its IP and port if alive); perform inter-protocol
 * transformation; invoke the service; and then return its response.
 *
 * ////////////////////////////////////////////////////////////////////////
 * Examples:
 *
 * /////////////////////////EXAMPLE ONE///////////////////////////////////
 *
 * 1. Run Geo.java and save the hostAddress and port.
 * 2. Run Auth.java and save the hostAddress and port.
 *
 * 4. Run configurations on Gateway.java and put the host and port values from
 *        running both services into the arguments section as follows:
 *  > 192.168.0.11 41537 192.168.0.11 42489
 * ORDER: GEO ADDRESSES,   AUTH ADDRESSES
 *       (Note that the order in which the two services host and port addresses
 *         is placed MATTERS)
 *
 * 5. Run Gateway.java and telnet host and port values into a linux terminal:
 *  > telnet 192.168.0.13 39289
 *
 * 6. Input into linux terminal:
 *  > GET /Auth?username=Nancy@November.me&password=Far2Away HTTP/1.1
 *
 * 7. Output:
 *  > HTTP/1.1 200 OK Server: Java HTTP Server : 1.0 Date: Sun Oct 17
 *  > 16:30:52 EDT 2021 Content-type: text/plain Content-length: 2
 *  > OK
 *
 * 8. Input into linux terminal:
 *  > GET /Geo?coordOne=20.3+30&coordTwo=5+50.54353 HTTP/1.1
 *
 * 9. Output:
 *     > HTTP/1.1 200 OK Server: Java HTTP Server : 1.0 Date: Sun Oct 17
 *  > 17:47:42 EDT 2021 Content-type: text/plain Content-length: 18
 *  > 2797.42 kilometers
 *  > Connection closed by foreign host.
 *
 * 11. Output:
 *  > HTTP/1.1 200 OK Server: Java HTTP Server : 1.0 Date: Tue Oct 19
 *  > 18:48:39 EDT 2021 Content-type: text/plain Content-length: 69
 *  > {"id":"S700_4002","name":"American Airlines: MD-11S","cost":"36.27"}
 *  > Connection closed by foreign host.
 *
 *
 * ///////////////////////////////////////////////////////////////////////
 */

public class Gateway extends Thread {

    private static final PrintStream log = System.out;
    private static final Map<Integer, String> httpResponseCodes = new HashMap<>();
    private static final Pattern isDouble = Pattern.compile("^[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?$");
    // private static final String[] redirectedEndpoints = { "/Geo", "/Auth" }
    
    static int geoSrvPort;
    static InetAddress geoSrvHost;

    static int authSrvPort;
    static InetAddress authSrvHost;

 
    static {
        httpResponseCodes.put(100, "HTTP CONTINUE");
        httpResponseCodes.put(101, "SWITCHING PROTOCOLS");
        httpResponseCodes.put(200, "OK");
        httpResponseCodes.put(201, "CREATED");
        httpResponseCodes.put(202, "ACCEPTED");
        httpResponseCodes.put(203, "NON AUTHORITATIVE INFORMATION");
        httpResponseCodes.put(204, "NO CONTENT");
        httpResponseCodes.put(205, "RESET CONTENT");
        httpResponseCodes.put(206, "PARTIAL CONTENT");
        httpResponseCodes.put(300, "MULTIPLE CHOICES");
        httpResponseCodes.put(301, "MOVED PERMANENTLY");
        httpResponseCodes.put(302, "MOVED TEMPORARILY");
        httpResponseCodes.put(303, "SEE OTHER");
        httpResponseCodes.put(304, "NOT MODIFIED");
        httpResponseCodes.put(305, "USE PROXY");
        httpResponseCodes.put(400, "BAD REQUEST");
        httpResponseCodes.put(401, "UNAUTHORIZED");
        httpResponseCodes.put(402, "PAYMENT REQUIRED");
        httpResponseCodes.put(403, "FORBIDDEN");
        httpResponseCodes.put(404, "NOT FOUND");
        httpResponseCodes.put(405, "METHOD NOT ALLOWED");
        httpResponseCodes.put(406, "NOT ACCEPTABLE");
        httpResponseCodes.put(407, "PROXY AUTHENTICATION REQUIRED");
        httpResponseCodes.put(408, "REQUEST TIME OUT");
        httpResponseCodes.put(409, "CONFLICT");
        httpResponseCodes.put(410, "GONE");
        httpResponseCodes.put(411, "LENGTH REQUIRED");
        httpResponseCodes.put(412, "PRECONDITION FAILED");
        httpResponseCodes.put(413, "REQUEST ENTITY TOO LARGE");
        httpResponseCodes.put(414, "REQUEST URI TOO LARGE");
        httpResponseCodes.put(415, "UNSUPPORTED MEDIA TYPE");
        httpResponseCodes.put(500, "INTERNAL SERVER ERROR");
        httpResponseCodes.put(501, "NOT IMPLEMENTED");
        httpResponseCodes.put(502, "BAD GATEWAY");
        httpResponseCodes.put(503, "SERVICE UNAVAILABLE");
        httpResponseCodes.put(504, "GATEWAY TIME OUT");
        httpResponseCodes.put(505, "HTTP VERSION NOT SUPPORTED");
    }

    private Socket client;
    
    private String testOutput = "";

    private Gateway(Socket client) {
        this.client = client;
    }

    private void sendHeaders(PrintStream res, int code, String contentType, String response) {
        sendHeaders(res, code, contentType, response, new String[] {});
    }

    private void sendHeaders(PrintStream res, int code, String contentType, String response, String[] headers) {
        // send HTTP Headers
        res.printf("HTTP/1.1 %d %s\n", code, httpResponseCodes.get(code));
        res.println("Server: Java HTTP Server : 1.0");
        res.println("Date: " + new Date());
        res.println("Content-type: " + contentType);
        res.println("Content-length: " + response.getBytes().length);
        Arrays.stream(headers).forEach(h -> res.println(h));
        res.println(); // blank line between headers and content, very important !
    }

    private String[] getComponents(String resourcePath) {
        if (!resourcePath.contains("?")) {
            return new String[] { resourcePath, "" };
        } else {
            return resourcePath.split("\\?", 2);
        }
    }

    private Map<String, String> getQueryStrings(String qs) throws Exception {
        Map<String, String> queries = new HashMap<>();
        String[] fields = qs.split("&");

        for (String field : fields) {
            String[] pairs = field.split("=", 2);
            if (pairs.length == 2) {
                queries.put(pairs[0], URLDecoder.decode(pairs[1], "UTF-8"));
            }
        }

        return queries;
    }

    public void run() {
        final String clientAddress = String.format("%s:%d", client.getInetAddress(), client.getPort());
        log.printf("Connected to %s\n", clientAddress);

        try (Socket client = this.client; // Makes sure that client is closed at end of try-statement.
                Scanner req = new Scanner(client.getInputStream());
                PrintStream res = new PrintStream(client.getOutputStream(), true);) {

            String request = req.nextLine();
            String method, resource, version;
            String response = "";
            
            StringTokenizer p = new StringTokenizer(request);
            String endpoint = p.nextToken().toLowerCase();

            List<String> headers = new ArrayList<>();

            try (Scanner parse = new Scanner(request)) {
                method = parse.next();
                resource = parse.next();
                version = parse.next();
            }

            int status = 200;

            try {

                if (!method.equals("GET")) {
                    status = 501;
                } else if (!version.equals("HTTP/1.1")) {
                    status = 505;
                }
                
                //          Geo?coordOne=20.3,+30&coordTwo=5,+50.54353
                else if (resource.startsWith("/Geo?")) {
                    Map<String, String> qs = getQueryStrings(resource.substring(resource.indexOf('?') + 1));

                    // Latitude and Longitude points must be in the form: "a b c d" where a, b, c, d are
                    // real numbers.
                    
                    
                    if (qs.containsKey("coordOne") && qs.containsKey("coordTwo")) {
                            String coordOne = qs.get("coordOne");
                            String coordTwo = qs.get("coordTwo");
                            
                            response = tryToConnectToGeo(geoSrvHost, geoSrvPort, coordOne, coordTwo);
                    }
                }
                //      /Auth?username=Nancy@November.me&password=Far2Away
                else if (resource.startsWith("/Auth?")) {
                    Map<String, String> qs = getQueryStrings(resource.substring(resource.indexOf('?') + 1));
                    // Input must be in the format <username> <password>
                    if (qs.containsKey("username") && qs.containsKey("password")) {

                        String username = qs.get("username");
                        String password = qs.get("password");

                        response = tryToConnectToAuth(authSrvHost, authSrvPort, username, password);
                    }
                }
                
            } catch (Exception e) {
                log.println(e);
                e.printStackTrace(log);
                status = 500;
            }

            if (status != 200 && response.isEmpty()) {
                response = httpResponseCodes.get(status);
            }

            if (headers.size() > 0) {
                sendHeaders(res, status, "text/plain", response, headers.toArray(new String[] {}));
            } else {
                sendHeaders(res, status, "text/plain", response);
            }
            res.println(response);
            res.flush(); // flush character output stream buffer
        } catch (Exception e) {
            log.println(e);
        } finally {
            log.printf("Disconnected from %s\n", clientAddress);
        }
    }

    public String tryToConnectToGeo(InetAddress h, int p, String coordOne, String coordTwo) {
        String srvResponse = "";
        try (Socket srvClient = new Socket(h, p)) {

            Scanner srvRes = new Scanner(srvClient.getInputStream());
            PrintStream srvReq = new PrintStream(srvClient.getOutputStream(), true);

            coordOne = coordOne.replaceAll("[+]", " ");
            coordTwo = coordTwo.replaceAll("[+]", " ");

            srvReq.println(coordOne + " " + coordTwo);

            while (srvRes.hasNextLine()) {
                    srvResponse = srvRes.nextLine();
            }

            srvClient.close();
            srvRes.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        return srvResponse;

    }



    public String tryToConnectToAuth(InetAddress h, int p, String username, String password) {
        String srvResponse = "";
        try (Socket srvClient = new Socket(h, p)) {
            Scanner srvRes = new Scanner(srvClient.getInputStream());
            PrintStream srvReq = new PrintStream(srvClient.getOutputStream(), true);

            srvReq.println(username + " " + password);
            
            srvResponse = srvRes.nextLine();


            srvClient.close();
            srvRes.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        return srvResponse;

    }
    
    public static void main(String[] args) throws Exception {

        geoSrvPort = Integer.parseInt(args[1]);
        geoSrvHost = InetAddress.getByName(args[0]);

        authSrvPort = Integer.parseInt(args[3]);
        authSrvHost = InetAddress.getByName(args[2]);

        int gateWayServerPort = 0;
        InetAddress gateWayHost = InetAddress.getLocalHost(); // .getLoopbackAddress();
        try (ServerSocket server = new ServerSocket(gateWayServerPort, 0, gateWayHost)) {
            log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
            while (true) {
                Socket client = server.accept();
                (new Gateway(client)).start();

            }
        }

    }
}


