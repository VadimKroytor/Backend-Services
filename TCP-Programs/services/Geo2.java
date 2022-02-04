package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

/*
This “pedagogical” service delegates to the RESTful Geo service in a stateful
manner to shed light on session management. It receives the coordinates of the
first point in one request and the coordinates of the second point in another.
It needs to somehow “link” the two request (despite the multithreading nature of
the service) and then supply all four real numbers to Geo and return its response
(i.e. no computation is involved).

Example:
1. Run Geo.java.
2. Run configurations on Geo2.java and place these values into the arguments section: 192.168.0.13 36557
3. A new port and host will be provided, telnet these values onto a linux terminal as follows:
 > telnet 192.168.0.11 46311

4. Input the following:
 > 43.77535348921289 -79.5015111512824

The following will be displayed on the linux terminal:
 > Cookie: 0

5. Telnet to the same Geo2 port and address as before again:
 > telnet 192.168.0.11 46311

6. Input:
 > 1 2

The following will be displayed on the linux terminal:
 > Cookie: 1


7. Telnet to the same Geo2 port and address as before again:
 > telnet 192.168.0.11 46311

8. Input: 
 > 43.843195533568355 -79.5394410357609 0
 
The following will be displayed on the linux terminal:
 > 8.13
 
9. Telnet to the same Geo2 port and address as before again:
 > telnet 192.168.0.11 46311

10. Input: 
 > 3 4 1
 
The following will be displayed on the linux terminal:
 > 314.40
 
11. Telnet to the same Geo2 port and address as before again:
 > telnet 192.168.0.11 46311

12. Input: 
 > 10 10 0
 
The following will be displayed on the linux terminal:
 > Invalid Cookie: 0
 
13. Telnet to the same Geo2 port and address as before again:
 > telnet 192.168.0.11 46311

14. Input: 
 > 10
 
The following will be displayed on the linux terminal:
 > Don't understand: 10


*/
public class Geo2 extends Thread {
	private static PrintStream Log = System.out;
	private Socket client;
	static int geoPort;
	static InetAddress geoHost;

	private static final Pattern isDouble = Pattern.compile("^[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?$");

	private Geo2(Socket client) {
		this.client = client;
	}

	public static void main(String[] args) throws Exception {
		geoPort = Integer.parseInt(args[1]);
		geoHost = InetAddress.getByName(args[0]);

		int port = 0;
		InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
		try (ServerSocket server = new ServerSocket(port, 0, host)) {
			Log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
			while (true) {
				Socket client = server.accept();

				(new Geo2(client)).start();
			}
		}

	}

	public void run() {
		Log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

		try (Socket client = this.client;
				Scanner req = new Scanner(client.getInputStream()); // get input from user
				PrintStream res = new PrintStream(client.getOutputStream(), true); // pass value to server
		) {
			String response = "";

			File cookieFile = new File("cookieFile.txt");

			if (cookieFile.createNewFile()) {
				System.out.print("Cookie file created \n");
			}

			String request = req.nextLine();

			String doubleRegex = "[+-]?([0-9]+)([.][0-9]+)?(E[+-]?[0-9]+)?";

			Pattern properFormatWithCookie = Pattern
					.compile("^" + doubleRegex + "\\s" + doubleRegex + "\\s" + doubleRegex + "$");
			Pattern properFormatWithoutCookie = Pattern.compile("^" + doubleRegex + "\\s" + doubleRegex + "$");
			if (properFormatWithCookie.matcher(request).matches()
					|| properFormatWithoutCookie.matcher(request).matches()) {
				String[] spaceDelimReq = request.split(" ");
				String firstCoordLat = spaceDelimReq[0];
				String firstCoordLongit = spaceDelimReq[1];
				String cookieValue;

				boolean hasFourLinesInFile = numLinesInFile("cookieFile.txt") == 4;
				boolean hasFiveLinesInFile = numLinesInFile("cookieFile.txt") == 5;
				
				Log.println(numLinesInFile("cookieFile.txt"));
				System.out.println(spaceDelimReq.length);
				if (spaceDelimReq.length == 3 && !hasFourLinesInFile && !hasFiveLinesInFile) {
					response = "Invalid Cookie: " + spaceDelimReq[2];
				} else {

					if (hasFourLinesInFile || hasFiveLinesInFile) {

						try (Socket geoClient = new Socket(geoHost, geoPort)) {
							cookieValue = spaceDelimReq[2];
							String geoResponse = "";
							Scanner geoRes = new Scanner(geoClient.getInputStream());
							PrintStream geoReq = new PrintStream(geoClient.getOutputStream(), true);

							HashMap<String, String> coordsAndCookies = new HashMap<>();
							coordsAndCookies = storeCoordsAndCookiesToHashMap("cookieFile.txt");

							String firstCoord = firstCoordLat + " " + firstCoordLongit;
							String secondCoord = "";
							boolean correctCookieGiven = false;

							if (cookieValue.equals(coordsAndCookies.get("firstCoordCookie"))) {
								secondCoord = coordsAndCookies.get("coordOne");
								correctCookieGiven = true;
							} else if (cookieValue.equals(coordsAndCookies.get("secondCoordCookie"))) {
								secondCoord = coordsAndCookies.get("coordTwo");
								correctCookieGiven = true;
							} else {
								response = "Invalid Cookie: " + cookieValue;
							}
							if (correctCookieGiven) {
								geoReq.println(firstCoord + " " + secondCoord);

								while (geoRes.hasNext()) {
									if (geoRes.hasNextFloat()) {

										geoResponse = geoRes.next();
										break;
									}
									geoRes.next();
								}

								response = geoResponse;

							}
							// this method is called this time to keep track the number of requests made
							// after two cookies have been saved. After two requests have been made after
							// the two cookies have been saved, the cookie file will be deleted.
							storeRequest("");
							if (numLinesInFile("cookieFile.txt") == 6) {
								File file = new File("cookieFile.txt");

								if (file.delete()) {
									System.out.println("Deleted the file: " + file.getName());
								} else {
									System.out.println("Failed to delete the file.");
								}
							}

							geoClient.close();
							geoRes.close();

						} catch (Exception e) {
							System.out.println(e);
						}

					} else if (numLinesInFile("cookieFile.txt") == 6) {
						File file = new File("cookieFile.txt");

						if (file.delete()) {
							System.out.println("Deleted the file: " + file.getName());
						} else {
							System.out.println("Failed to delete the file.");
						}
					} else {
						try {
							storeRequest(request); // saving request
							if (numLinesInFile("cookieFile.txt") == 2) {
								response = "Cookie: 0";
							}
							if (numLinesInFile("cookieFile.txt") == 4) {
								response = "Cookie: 1";
							}
						} catch (Exception e) {
							System.out.println(e);
						}
					}
				}
			} else {
				response = "Don't understand: " + request;
			}

			res.println(response);
			req.close();
			client.close();
		} catch (Exception e) {
			Log.println(e);
		} finally {
			Log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
		}
	}

	public synchronized void storeRequest(String request) {
		try {
			File cookieFile = new File("cookieFile.txt");
			// Linking requests
			FileWriter fr = new FileWriter(cookieFile, true);
			BufferedWriter br = new BufferedWriter(fr);

			if (numLinesInFile("cookieFile.txt") == 0) {
				System.out.println("Storing request to cookie file");
				br.write(request + "\n");
				br.write("0" + "\n");
			} else if (numLinesInFile("cookieFile.txt") == 2) {
				br.write(request + "\n");
				br.write("1" + "\n");
				System.out.println("The two coordinates have been inputted");
			} else if (numLinesInFile("cookieFile.txt") == 4 || numLinesInFile("cookieFile.txt") == 5) {
				br.write("\n");
			}
			br.close();
			fr.close();

		} catch (Exception e) {
			System.out.println("An Exception Error Occurred.");
			e.printStackTrace();
		}
	}

	public static synchronized int numLinesInFile(String fileName) {
		long numLines = 0;
		/*
		 * try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
		 * while (reader.readLine() != null) numLines++; } catch (Exception e) {
		 * System.out.println("An error occurred"); }
		 */
		Path path = Paths.get(fileName);
		try {
			numLines = Files.lines(path).count();
		} catch (Exception e) {
			System.out.println(e);
		}
		return (int) numLines;
	}

	public synchronized static HashMap<String, String> storeCoordsAndCookiesToHashMap(String fileName) {
		HashMap<String, String> reqMap = new HashMap<>();
		try {

			int lineNum = 1;
			File cookieFile = new File(fileName);
			Scanner reader = new Scanner(cookieFile);
			while (reader.hasNextLine()) {
				String data = reader.nextLine();
				if (lineNum == 1) {
					reqMap.put("coordOne", data);
				} else if (lineNum == 2) {
					reqMap.put("firstCoordCookie", data);
				} else if (lineNum == 3) {
					reqMap.put("coordTwo", data);
				} else if (lineNum == 4) {
					reqMap.put("secondCoordCookie", data);
				}
				lineNum++;
			}
			reader.close();
		} catch (Exception e) {
			System.out.println("Error occurred");
		}

		return reqMap;
	}
}