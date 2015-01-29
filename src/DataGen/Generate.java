package DataGen;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;

import DataGen.MapsManager;
import DataGen.randomSpectra;

public class Generate {
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MapsManager m = new MapsManager("AIzaSyA1t67Aa6hyiIiZGebHPfGpJVc2UX8773o");
		randomSpectra r = new randomSpectra();
		/*
		 
		//vale house to downling street
		double[] start = {51.5033630,-0.1276250};
		double[] end = {51.5352040,-0.1902430};
		
		*/
		
		
		/* 
		//ox to ox street
		double[] start = {51.515399, -0.144313};
		double[] end = {51.515970,-0.150847};
		*/
		/*
		//holborn
		double[] start = {51.522501, -0.117331};
		double[] end = {51.517881,-0.119090};
		*/
		
		/*
		//manhattan1
		double[] start = {40.803179, -73.950863};
		double[] end = {40.790822,-73.944597};
		*/
		
		/*
		
		//manhattan2 lincoln tunnel
		double[] start = {40.773469, -74.021244};
		double[] end = {40.765305,-73.991590};
		
		*/
		
		

		/*
		//kenya
		double[] start = {0.136213, 37.464752};
		double[] end = {0.180030,37.766876};
		*/
		
		
		//moscow1
		double[] start = {55.755826, 37.617300};
		double[] end = {55.753279,37.632852};
		
	
		HashMap<String, ReadingPointObject> DATASET = m.GenerateData(start, end, 945.0, 950.0);
		
		PrintWriter out = null;
		PrintWriter out2 = null;
		
		try {
			out = new PrintWriter("/Users/ammanvedi/Desktop/JSONOUTCOMBINED.json");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		out.println(m.dataToJSON(DATASET));
		out.close();


	}

}
