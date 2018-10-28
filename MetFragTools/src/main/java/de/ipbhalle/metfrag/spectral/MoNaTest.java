package de.ipbhalle.metfrag.spectral;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MoNaTest {

	public static void main(String[] args) throws Exception {
		URL url = new URL("http://mona.fiehnlab.ucdavis.edu/rest/spectra/search");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);
		conn.setRequestProperty("Content-Type", "application/json");

		// Create the form content
		OutputStream out = conn.getOutputStream();
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		writer.write("{\"compound\":{},\"metadata\":[],\"tags\":[],\"match\":{\"spectra\":\"195.0869:999.00 138.0656:512.39 110.0708:24.08 69.0444:3.10 83.0599:2.30\"}}");
	//	writer.write("{\"compound\":{},\"metadata\":[],\"tags\":[],\"match\":{\"spectra\":\"96.9508:29.4 114.9612:14.8 124.9821:62.2 153.0133:60.3 171.024:14.1 197.9275:999.0 213.9042:47.3 225.9593:12.0\"}}");

		writer.close();
		out.close();

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();

		conn.disconnect();
		
		
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray)parser.parse(new java.io.InputStreamReader(IOUtils.toInputStream(sb.toString())));
		System.out.println(jsonArray.size());
		for(int i = 0; i < jsonArray.size(); i++) {	
			JSONObject obj = (JSONObject)jsonArray.get(i);
			Iterator<?> it = obj.keySet().iterator();
			while(it.hasNext()) {
				String key = (String)it.next();
				try {
					System.out.println(key + " " + obj.get(key).getClass().getName());
					if(key.equals("score")) {
						org.json.simple.JSONObject subObj = ((org.json.simple.JSONObject)obj.get(key));
						Iterator<?> itObj = subObj.keySet().iterator();
						while(itObj.hasNext()) {
							String subKey = (String)itObj.next();
							if(subKey.equals("inchi")) System.out.println("\t" +  subKey + " " + subObj.get(subKey) + " " + subObj.get(subKey).getClass().getName());
							else if(subKey.equals("inchiKey")) System.out.println("\t" +  subKey + " " + subObj.get(subKey) + " " + subObj.get(subKey).getClass().getName());
							else if(subKey.equals("id")) System.out.println("\t" +  subKey + " " + subObj.get(subKey) + " " + subObj.get(subKey).getClass().getName());
							else System.out.println("\t" +  subKey + " " + subObj.get(subKey));
						}
					}
				}
				catch(Exception e) {
					System.out.println(key + " null");
				}
			}
		}
		/*
		JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("esearchresult")).get("idlist");
		Object[] objs = jsonArray.toArray();
		*/
		
		
	}

}
