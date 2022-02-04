package model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(latitude = "geo-result")
public class GeoBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String distance;
	private String latitude1;
	private String latitude2;
	private String longitude1;
	private String longitude2;
	private String message;
	private String range;


	public GeoBean() {
	}

	public String getDistance() {
		return distance;
	}



	public void setDistance(String distance) {
		this.distance = distance;
	}



	public String getLatitude1() {
		return latitude1;
	}



	public void setLatitude1(String latitude1) {
		this.latitude1 = latitude1;
	}



	public String getLatitude2() {
		return latitude2;
	}



	public void setLatitude2(String latitude2) {
		this.latitude2 = latitude2;
	}



	public String getLongitude1() {
		return longitude1;
	}



	public void setLongitude1(String longitude1) {
		this.longitude1 = longitude1;
	}



	public String getLongitude2() {
		return longitude2;
	}



	public void setLongitude2(String longitude2) {
		this.longitude2 = longitude2;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public String getRange() {
		return range;
	}



	public void setRange(String range) {
		this.range = range;
	}



}
