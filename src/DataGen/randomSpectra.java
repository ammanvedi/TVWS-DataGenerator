package DataGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.text.html.HTMLDocument.Iterator;

public class randomSpectra {
	
	private Random r = new Random();
	private final double bandgap =  0.05078336726;
	private final int MAXPOWER = 10;
	private final int MINPOWER = -40;
	
	public ArrayList<Double> getRandomSampledFrequencies(double lowend, double highend)
	{
		ArrayList<Double> frequencies = new ArrayList<Double>();
		
		for(double c = lowend; c < highend; c+=this.bandgap)
		{
			//System.out.println(c);
			frequencies.add(c);
			//System.out.println(this.intRandomInclusive(MAXPOWER, MINPOWER));
		}
		
		return frequencies;
	}
	
	public int getRandomObservedPower()
	{
		return this.intRandomInclusive(MAXPOWER, MINPOWER);
	}
	
	public HashMap<Double, Integer> getPowersForFrequencies(ArrayList<Double> Frequencies)
	{
		HashMap<Double, Integer> h = new HashMap<Double, Integer>();
		
		for(Double F : Frequencies)
		{
			h.put(F, this.getRandomObservedPower());
			//System.out.println(F + " " + this.getRandomObservedPower());
		}
		
		return h;
	}
	
	private double doubleRandomInclusive(double max, double min) 
	{
		   double r = Math.random();
		   if (r < 0.5) {
		      return ((1 - Math.random()) * (max - min) + min);
		   }
		   return (Math.random() * (max - min) + min);
	}
	
	private int intRandomInclusive(int max, int min) 
	{
		   int ra = this.r.nextInt((max - min) + 1) + min;
		   return ra;
	}

}
