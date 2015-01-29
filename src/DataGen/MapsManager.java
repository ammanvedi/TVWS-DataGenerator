package DataGen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import DataGen.ReadingPointObject;
import DataGen.randomSpectra;


public class MapsManager {
	
	
	private String apikey = "";
	private final String USER_AGENT = "Mozilla/5.0";
	
	public MapsManager(String key)
	{
		this.apikey = key;
	}
	
	/* 
	 * if a (point to point) line on the random route generated is too
	 * long then it should be split into a set of points spaced equally apart
	 * 
	*/
	
	private ArrayList<double[]> splitLongLine(double[] longlinestart, double[] longlineend, double kmbetween)
	{
		//get the bearing between two points
		double bear = bearing(longlinestart[0], longlinestart[1], longlineend[0], longlineend[1]);
		double kmtravelled = 0.0;
		double dkm = 0.04;
		double brng = Math.toRadians(bear);
		
		//in km 
		
		ArrayList<double[]> splitpoints = new ArrayList<double[]>();
		
		double lat1 = Math.toRadians(longlinestart[0]);
		double lon1 = Math.toRadians(longlinestart[1]);

		do {
			kmtravelled += dkm;
			double dist = kmtravelled/6371.0;
			double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
			double a = Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
			//System.out.println("a = " +  a);
			double lon2 = lon1 + a;

			lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;

			//System.out.println("\n\n" + kmtravelled +"  km from startLatitude = "+Math.toDegrees(lat2)+","+Math.toDegrees(lon2));
			
			double[] splitpoint = {Math.toDegrees(lat2), Math.toDegrees(lon2)};
			splitpoints.add(splitpoint);
			
		} while (kmtravelled < kmbetween);
		
		return splitpoints;
		
	}
	
	
	
	private double metersBetweenPoints(double[] PointA, double[] PointB)
	{
		return distance(PointA[0], PointA[1], PointB[0], PointB[1], 'K');
	}
	
	public static final double distance(double lat1, double lon1, double lat2, double lon2, char unit)
	{
	    double theta = lon1 - lon2;
	    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	    dist = Math.acos(dist);
	    dist = rad2deg(dist);
	    dist = dist * 60 * 1.1515;
	     
	    if (unit == 'K') {
	        dist = dist * 1.609344;
	    }
	    else if (unit == 'N') {
	        dist = dist * 0.8684;
	    }
	     
	    return (dist);
	}
	 
	/**
	 * <p>This function converts decimal degrees to radians.</p>
	 * 
	 * @param deg - the decimal to convert to radians
	 * @return the decimal converted to radians
	 */
	private static final double deg2rad(double deg)
	{
	    return (deg * Math.PI / 180.0);
	}
	 
	/**
	 * <p>This function converts radians to decimal degrees.</p>
	 * 
	 * @param rad - the radian to convert
	 * @return the radian converted to decimal degrees
	 */
	private static final double rad2deg(double rad)
	{
	    return (rad * 180 / Math.PI);
	}
	
	public ArrayList<double[]> getRoute(double[] StartPoint, double[] EndPoint)
	{
		//loopier routes 
		// https://maps.googleapis.com/maps/api/directions/json?origin=51.5352040,-0.1902430&destination=51.5352040,-0.1902430&mode=driving&waypoints=via:51.5033630,-0.1276250&key=AIzaSyA1t67Aa6hyiIiZGebHPfGpJVc2UX8773o
		
		String Directionsapistring = "https://maps.googleapis.com/maps/api/directions/json?origin=" + StartPoint[0] + "," + StartPoint[1] + "&destination=" 
										+ StartPoint[0] + "," + StartPoint[1] + "&mode=driving&waypoints=via:" + EndPoint[0] + "," + EndPoint[1] + "&key=" + this.apikey;
		String jsonresp = "";
		
		try {
			jsonresp = getRequest(Directionsapistring);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("full out failed");
		}
		
		JsonObject directions = JsonObject.readFrom(jsonresp);
		JsonArray routes = directions.get("routes").asArray();
		JsonObject routesObj = routes.get(0).asObject();
		
		JsonArray legs = routesObj.get("legs").asArray();
		JsonArray steps = legs.get(0).asObject().get("steps").asArray();
		
		ArrayList<double[]> routeGeometry = new ArrayList<double[]>();
		
		double[] previouspoint = null;
		
		for(int x = 0; x< steps.size(); x++)
		{
			double[] d = { steps.get(x).asObject().get("end_location").asObject().get("lat").asDouble()
					, steps.get(x).asObject().get("end_location").asObject().get("lng").asDouble()};
			
/*			double[] e = { steps.get(x).asObject().get("start_location").asObject().get("lat").asDouble()
					, steps.get(x).asObject().get("start_location").asObject().get("lng").asDouble()};
*/
			
			if(previouspoint != null)
			{
				//not the first point compare to last 
				double distancefromlast = metersBetweenPoints(d, previouspoint);
				if(distancefromlast > 0.4)
				{
					
					//if the points are more than 40 m apart then 
					//split the point
					routeGeometry.add(d);
					//System.out.println("distance greater than 40m");
					//System.out.println("splitting...");
					routeGeometry.addAll(this.splitLongLine(previouspoint, d, distancefromlast));
				}else {
					//distance between is not large
					//just add point
					routeGeometry.add(d);
				}
				
				
				
				previouspoint = d;
				
			}else{
				previouspoint = d;
				routeGeometry.add(d);
			}
			
			routeGeometry.add(d);
			//routeGeometry.add(e);
			
			//System.out.println(steps.get(x).asObject().get("end_location"));
			//System.out.println(steps.get(x).asObject().get("start_location"));
		}
		//System.out.println(steps.get(1).asObject().get("end_location"));
		//JsonArray endloc = steps.get(0).asObject();
		


		
		
		
		return routeGeometry;
		
	}
	
	public HashMap<String, ReadingPointObject> GenerateData(double[] StartRoute, double[] EndRoute, double HighFreq, double LowFreq)
	{
		randomSpectra randspec = new randomSpectra();
		HashMap<String, ReadingPointObject> DATASET = this.getTimestamps(getRoute(StartRoute, EndRoute));
		ArrayList<Double> freqs = randspec.getRandomSampledFrequencies(HighFreq, LowFreq);
		ReadingPointObject rp = null;
		
		for(String TS : DATASET.keySet())
		{
			//get some random power readings for this point
			HashMap<Double, Integer> powers = randspec.getPowersForFrequencies(freqs);
			rp = DATASET.get(TS);
			rp.Readings = powers;
			//System.out.println("RP: " + TS + " LATLNG: " + rp.Latitude + " " + rp.Longitude + " readingslength : " + DATASET.get(TS).Readings.size());
			
		}
		
		
		//System.out.println(rp.readingsToJSONObject());
		
		return DATASET;
		
		
	}
	
	public String makeLocationString(HashMap<String, ReadingPointObject> Data)
	{
		String JSON = "\"Location\": {";
		for(String TS : Data.keySet())
		{
			System.out.println("erhehrehr");
			ReadingPointObject rp = Data.get(TS);
			
			JSON += "\"" + TS + "\":";
			JSON += rp.readingsToJsonArray();
			JSON += ",";
		}
		JSON = JSON.substring(0,JSON.length()-1);
		JSON += "}";
		System.out.println("locationdone");
		return JSON;
		
	}
	
	public String dataToJSON(HashMap<String, ReadingPointObject> Data)
	{
		String JSONSTR = "[\"RTLSDR Scanner\", {\"Description\": \"\", \"Calibration\": 41.6937, \"LO\": 0.0, \"Latitude\": \"55.703643333\", \"Time\": \"2014-09-14T14:36:30.292207Z\", \"Stop\": 950,";
		JSONSTR += this.makeSpectrumString(Data);
		JSONSTR += ",";
		JSONSTR += this.makeLocationString(Data);
		JSONSTR += "}]";
		
		return JSONSTR;
	}
	
	public String makeSpectrumString(HashMap<String, ReadingPointObject> Data)
	{
		String JSON = "\"Spectrum\": {";
		
		for(String TS : Data.keySet())
		{
			System.out.println("erhehrehr");
			ReadingPointObject rp = Data.get(TS);
			
			JSON += "\"" + TS + "\":";
			JSON += rp.readingsToJSONObject();
			JSON += ",";
		}
		JSON = JSON.substring(0,JSON.length()-1);
		JSON += "}";
		
		return JSON;
	}
	
	
	
	protected static double bearing(double lat1, double lon1, double lat2, double lon2)
	{
		  double longitude1 = lon1;
		  double longitude2 = lon2;
		  double latitude1 = Math.toRadians(lat1);
		  double latitude2 = Math.toRadians(lat2);
		  double longDiff= Math.toRadians(longitude2-longitude1);
		  double y= Math.sin(longDiff)*Math.cos(latitude2);
		  double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

		  return (Math.toDegrees(Math.atan2(y, x))+360)%360;
	}
	
	private HashMap<String, ReadingPointObject> getTimestamps(ArrayList<double[]> latlngs)
	{
		long unixTime = System.currentTimeMillis() / 1000L;
		long currenttimeoffset = 0;
		//speed of movement in m/s
		double speed = 2.0;
		
		HashMap<String, ReadingPointObject> points = new HashMap<String, ReadingPointObject>();
		
		//for each lat lng, determine the offset from 
		double[] lastpoint = null;
		
		for(double[] latlngpoint : latlngs)
		{
			if(lastpoint != null)
			{
				//distance between this and last point (in MM)
				double dist = metersBetweenPoints(lastpoint, latlngpoint) * 1000;
				//s = d/t --> t = d/s
				double timetaken = dist/speed;
				
				currenttimeoffset += timetaken;
				//System.out.println(currenttimeoffset);
				
				ReadingPointObject rp = new ReadingPointObject();
				rp.Latitude = latlngpoint[0];
				rp.Longitude = latlngpoint[1];
				points.put(Long.toString(unixTime + currenttimeoffset), rp);
				
				
				lastpoint = latlngpoint;
				
			}else {
				//first point, add it and include the base timestamp
				ReadingPointObject rp = new ReadingPointObject();
				rp.Latitude = latlngpoint[0];
				rp.Longitude = latlngpoint[1];
				points.put(Long.toString(unixTime), rp);
				lastpoint = latlngpoint;
				
			}
		}
		
		return points;
	}
	
	private String getRequest(String url) throws Exception
	{
		

		
		URL connectionURL = new URL(url);
		
		HttpURLConnection connection = (HttpURLConnection) connectionURL.openConnection();
		
		connection.setRequestMethod("GET");
		
		connection.setRequestProperty("User-Agent", USER_AGENT);
		
		int responseCode = connection.getResponseCode();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		String inputline;
		
		StringBuffer response = new StringBuffer();
		
		while((inputline = in.readLine()) != null)
		{
			response.append(inputline);
		}
		
		in.close();
		
		System.out.println("The response from the server was " + responseCode);
		System.out.println(response.toString());
		return response.toString();
	}

}
