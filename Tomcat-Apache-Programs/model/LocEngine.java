
package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class LocEngine {
	private static LocEngine engine = null;
	private static PrintStream log = System.out;

	private LocEngine() {
	}

	public static LocEngine getInstance() {
		if (engine == null)
			engine = new LocEngine();
		return engine;
	}

	public HashMap<String, String> runLoc(String location, String mapQuestKey) throws IOException {

		PrintStream log = System.out;
		JsonParser parser = new JsonParser();
		String encodedLocaton = URLEncoder.encode(location, "UTF-8");
		String url = "http://www.mapquestapi.com/geocoding/v1/address?" + "key=" + mapQuestKey + "&location="
				+ encodedLocaton;

		/*
		 * example: http://www.mapquestapi.com/geocoding/v1/address?key=
		 * eq4cXPFbfIRteZlr2dvAOaCnSfT29xmR&location=Washington,DC
		 * 
		 * http://localhost:4413/B/Loc?location=4700+Keele+Street+Toronto
		 */

		String line = "";
		HashMap<String, String> output = new HashMap<>();
		
		try (Scanner in = new Scanner((new URL(url)).openStream()))

		{
			while (in.hasNextLine()) {
				line += in.nextLine();
			}

			JsonPrimitive statusCode = parser.parse(line).getAsJsonObject().get("info").getAsJsonObject()
					.getAsJsonPrimitive("statuscode");

			
			String lat = "null";
			String lng = "null";
			if (statusCode.getAsString().equals("0")) {

				JsonObject latLng = parser.parse(line).getAsJsonObject().getAsJsonArray("results").get(0)
						.getAsJsonObject().getAsJsonArray("locations").get(0).getAsJsonObject()
						.getAsJsonObject("latLng");

				lat = latLng.get("lat").getAsString();
				lng = latLng.get("lng").getAsString();
			}

			output.put("lat", lat);
			output.put("lng", lng);
			
		} catch (Exception e) {
			log.print(e);
		}
		return output;
	}
}