package DataGen;

import java.util.HashMap;

public class ReadingPointObject {
	
	public double Latitude;
	public double Longitude;
	public HashMap<Double, Integer> Readings;
	
	public ReadingPointObject()
	{
		Readings = new HashMap<Double, Integer>(); 
	}
	
	public String readingsToJSONObject()
	{
		String JSONSTRING = "{\n";
		
		for(Double frequency : Readings.keySet())
		{
			int power = Readings.get(frequency);
			JSONSTRING += "\n \"" + frequency + "\": " + power + ",";
		}
		JSONSTRING = JSONSTRING.substring(0,JSONSTRING.length()-1);
		JSONSTRING += "}";
		return JSONSTRING;
	}
	
	public String readingsToJsonArray()
	{
		String JSON = "[\n" + this.Latitude + ",\n " + this.Longitude + "]";
		
		return JSON;
	}
	

}
