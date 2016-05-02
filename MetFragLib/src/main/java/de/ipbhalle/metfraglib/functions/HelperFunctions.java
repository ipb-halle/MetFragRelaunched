package de.ipbhalle.metfraglib.functions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HelperFunctions {
	
	public static double sumArray(double[] array) {
		double sum = 0.0;
		for(int k = 0; k < array.length; k++) sum += array[k];
		return sum;
	}
	
	/**
	 * 
	 * @param val
	 * @param digits
	 * @return
	 */
	public static double round(double val, double digits) {
		double multiplier = Math.pow(10.0, digits);
		return (double)Math.round(val * multiplier) / multiplier;
	}
	
	/**
	 * 
	 * @param size
	 * @return
	 */
	public static String getRandomString(int size) {
		char[] vals = {'0','1','2','3','4','5','6','7','8','9','Q','W','E','R','T','Z','U','I','O','P','A','S','D','F','G','H','J',
				'K','L','Y','X','C','V','B','N','M'};
		String randomString = "";
		java.util.Random rand = new java.util.Random();
		for(int i = 0; i < size; i++)
			randomString += vals[rand.nextInt(size)];
		
		return randomString;
	}
	
	/**
	 * 
	 * @param urlname
	 * @return
	 */
	public static InputStream getInputStreamFromURL(String urlname) throws Exception {
		InputStream stream = null;
		
		try {
			URL url = new URL(urlname);
			java.net.HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			if (connection.getResponseCode() != 200 && connection.getResponseCode() != 404 && connection.getResponseCode() != 202) {
				throw new IOException(connection.getResponseMessage());
			}
			stream = connection.getInputStream();
		} catch(MalformedURLException mue) {
			System.err.println("Error: Could create URL object!");
			throw new Exception();
		} catch (IOException e) {
			System.err.println("Error: Could not open URL connection!");
			System.err.println(urlname);
			throw new Exception();
		}
		
		return stream;
	}
	
	public static String stringArrayToString(String[] stringArray) {
		if(stringArray == null) return "";
		String string = "";
		if(stringArray.length >= 1) string = stringArray[0];
		for(int i = 1; i < stringArray.length; i++)
			string += "," + stringArray[i];
		return string;
	}
}
