package de.ipbhalle.metfrag.database;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RetriveRefsFromPubChem {

	
	public static void main(String[] args) throws java.io.IOException {
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(args[0])));	
		String line = breader.readLine();	
		breader.close();
		line = line.trim();
		String[] cids = line.split(",");
		java.util.HashMap<String, Integer> cidToNumberOfPatents = assignPatents(cids);
		java.util.HashMap<String, Integer> cidToNumberOfPubMedReferences = assignPubMedReferences(cids);
		for(int i = 0; i < cids.length; i++) {
			System.out.println(cids[i] + "|" + cidToNumberOfPatents.get(cids[i]) + "|" + cidToNumberOfPubMedReferences.get(cids[i]));
		}
	
	}
	
	public static java.util.HashMap<String, Integer> assignPatents(String[] cids) {
		String idString = "";
		java.util.HashMap<String, Integer> cidToNumberOfPatents = new java.util.HashMap<String, Integer>();
		if(cids == null || cids.length == 0)
			return cidToNumberOfPatents;

		JSONParser parser = new JSONParser();
		for(int i = 0; i < cids.length; i++) {
			idString += "," + cids[i];
			if((i % 100 == 0 && i != 0) || (i == cids.length - 1)) {
				idString = idString.substring(1, idString.length());
				String urlname = "http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + idString + "/xrefs/PatentID/JSON";
				java.io.InputStream stream = null;
				if((stream = getStreamForPubChemInfo(urlname)) == null) {
					String[] tmp_cids = idString.split(",");
					for(int k = 0; k < tmp_cids.length; k++) {
						cidToNumberOfPatents.put(tmp_cids[k], 0);
					}
					idString = "";
					continue;
				}
				JSONObject jsonObject = null;
				try {
					jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} 
				if(jsonObject == null) {
					System.err.println("Error: Could not create JSON object for fetching Patents.");
					return cidToNumberOfPatents;
				}
				JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("InformationList")).get("Information");
				Object[] objs = jsonArray.toArray();
				for(int k = 0; k < objs.length; k++) {
					String cid = String.valueOf(((JSONObject)objs[k]).get("CID"));
					JSONArray ar = (JSONArray)((JSONObject)objs[k]).get("PatentID");
					cidToNumberOfPatents.put(cid, ar != null ? ar.size() : 0);
				}
				idString = "";
			}
		}
		return cidToNumberOfPatents;
	}
	
	public static java.util.HashMap<String, Integer> assignPubMedReferences(String[] cids) {
		String idString = "";
		java.util.HashMap<String, Integer> cidToNumberOfPubMedReferences = new java.util.HashMap<String, Integer>();
		if(cids == null || cids.length == 0)
			return cidToNumberOfPubMedReferences;

		JSONParser parser = new JSONParser();
		for(int i = 0; i < cids.length; i++) {
			idString += "," + cids[i];
			if((i % 100 == 0 && i != 0) || (i == cids.length - 1)) {
				idString = idString.substring(1, idString.length());
				String urlname = "http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + idString + "/xrefs/PubMedID/JSON";
				java.io.InputStream stream = null;
				if((stream = getStreamForPubChemInfo(urlname)) == null) {
					String[] tmp_cids = idString.split(",");
					for(int k = 0; k < tmp_cids.length; k++) {
						cidToNumberOfPubMedReferences.put(tmp_cids[k], 0);
					}
					idString = "";
					continue;
				}
				JSONObject jsonObject = null;
				try {
					jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
					stream.close();
					stream = null;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (org.json.simple.parser.ParseException e) {
					e.printStackTrace();
				}
				if(jsonObject == null) {
					System.err.println("Error: Could not create JSON object for fetching PubMed references.");
					return cidToNumberOfPubMedReferences;
				}
				JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("InformationList")).get("Information");
				Object[] objs = jsonArray.toArray();
				for(int k = 0; k < objs.length; k++) {
					String cid = String.valueOf(((JSONObject)objs[k]).get("CID"));
					JSONArray ar = (JSONArray)((JSONObject)objs[k]).get("PubMedID");
					cidToNumberOfPubMedReferences.put(cid, ar != null ? ar.size() : 0);
				}
				idString = "";
			}
		}
		return cidToNumberOfPubMedReferences;
	}
	
	public static java.io.InputStream getStreamForPubChemInfo(String urlname) {		
		java.net.HttpURLConnection connection = null;
		java.io.InputStream stream = null;
		int responseCode = 403;
		try {
			java.net.URL url = new java.net.URL(urlname);
			connection = (java.net.HttpURLConnection) url.openConnection();
			responseCode = connection.getResponseCode();
			if(responseCode == 404) return null;
			
			if (responseCode != 200) {
				throw new IOException(connection.getResponseMessage());
			}
			stream = connection.getInputStream();
		} catch(java.net.MalformedURLException mue) {
			System.err.println("Error: Could create URL object!");
		} catch (IOException e) {
			System.err.println("Error: Could not open URL connection!");
			System.err.println(urlname);
			e.printStackTrace();
		}
		finally {
		//	if(connection != null) connection.disconnect();
		}
		return stream;
	
	}
}
