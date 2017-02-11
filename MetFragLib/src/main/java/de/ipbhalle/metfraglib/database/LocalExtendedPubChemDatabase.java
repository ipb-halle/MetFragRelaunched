package de.ipbhalle.metfraglib.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalExtendedPubChemDatabase extends LocalPubChemDatabase {

	protected HashMap<String, Double> cidToNumberOfPubMedReferences;
	protected HashMap<String, Double> cidToNumberOfPatents;
	
	public LocalExtendedPubChemDatabase(Settings settings) {
		super(settings);
	}

	public ICandidate getCandidateByIdentifier(String identifier)
			throws DatabaseIdentifierNotFoundException {
		ICandidate candidate = super.getCandidateByIdentifier(identifier);
		
		Vector<String> cid = new Vector<String>();
		cid.add(identifier);
		ProcessingStatus processingStatus = null;
		if(this.settings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			processingStatus = (ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME);
		logger.info("Fetching PubMed references");
		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving References");
		this.assignNumberOfPubMedReferences(cid);
		logger.info("Fetching patents");
		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving Patents");
		
		this.assignNumberOfPatents(cid);
		this.assignNumberOfPatents(cid);

		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving Candidates");
		return candidate;
	}
	
	/**
	 * 
	 */
	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
		CandidateList candidateList = super.getCandidateByIdentifier(identifiers);

		Vector<String> cids = new Vector<String>();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			cids.add(candidateList.getElement(i).getIdentifier());
		}
		ProcessingStatus processingStatus = null;
		if(this.settings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			processingStatus = (ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME);
		
		logger.info("Fetching PubMed references");
		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving References");
		this.assignNumberOfPubMedReferences(cids);
		logger.info("Fetching patents");
		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving Patents");
		this.assignNumberOfPatents(cids);
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			this.addExtendedInformation(candidateList.getElement(i));
		}

		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving Candidates");
		return candidateList;
	}


	protected void addExtendedInformation(ICandidate candidate) {
		candidate.setProperty(VariableNames.PUBCHEM_NUMBER_PATENTS_NAME, this.cidToNumberOfPatents.get(candidate.getIdentifier()));
		candidate.setProperty(VariableNames.PUBCHEM_NUMBER_PUBMED_REFERENCES_NAME, this.cidToNumberOfPubMedReferences.get(candidate.getIdentifier()));
	}
	
	public double getNumberOfPatents(String identifier) {
		return this.cidToNumberOfPatents.get(identifier) != null ? (Double)this.cidToNumberOfPatents.get(identifier) : 0d;
	}

	public double getNumberOfPubmedReferences(String identifier) {
		return this.cidToNumberOfPubMedReferences.get(identifier) != null ? (Double)this.cidToNumberOfPubMedReferences.get(identifier) : 0d;
	}
	
	/**
	 * 
	 * @param cidsVec
	 * @return
	 */
	protected void assignNumberOfPubMedReferences(Vector<String> cidsVec) {
		String idString = "";
		this.cidToNumberOfPubMedReferences = new java.util.HashMap<String, Double>();
		if(cidsVec == null || cidsVec.size() == 0)
			return;

		JSONParser parser = new JSONParser();
		for(int i = 0; i < cidsVec.size(); i++) {
			idString += "," + cidsVec.get(i);
			if((i % 100 == 0 && i != 0) || (i == cidsVec.size() - 1)) {
				idString = idString.substring(1, idString.length());
				String urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + idString + "/xrefs/PubMedID/JSON";
				logger.trace(urlname);
				java.io.InputStream stream = null;
				try {
					stream = this.getStreamForPubChemInfo(urlname);
				} catch(Exception e) {
					
				}
				if(stream == null) {
					String[] tmp_cids = idString.split(",");
					for(int k = 0; k < tmp_cids.length; k++) {
						this.cidToNumberOfPubMedReferences.put(tmp_cids[k], 0d);
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
					logger.error("Error: Could not fetch PubMed references.");
					this.cidToNumberOfPubMedReferences = new java.util.HashMap<String, Double>();
					for(int k = 0; k < cidsVec.size(); k++) {
						this.cidToNumberOfPubMedReferences.put(cidsVec.get(k), 0d);
					}
					return;
				} catch (ParseException e) {
					logger.error("Error: Could not fetch PubMed references.");
					this.cidToNumberOfPubMedReferences = new java.util.HashMap<String, Double>();
					for(int k = 0; k < cidsVec.size(); k++) {
						this.cidToNumberOfPubMedReferences.put(cidsVec.get(k), 0d);
					}
					return;
				}
				if(jsonObject == null) {
					logger.error("Error: Could not create JSON object for fetching PubMed references.");
					return;
				}
				JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("InformationList")).get("Information");
				Object[] objs = jsonArray.toArray();
				for(int k = 0; k < objs.length; k++) {
					String cid = String.valueOf(((JSONObject)objs[k]).get("CID"));
					JSONArray ar = (JSONArray)((JSONObject)objs[k]).get("PubMedID");
					this.cidToNumberOfPubMedReferences.put(cid, ar != null ? ar.size() : 0d);
				}
				idString = "";
			}
		}
	}

	protected void assignNumberOfPatents(Vector<String> cidsVec) {
		String idString = "";
		this.cidToNumberOfPatents = new java.util.HashMap<String, Double>();
		if(cidsVec == null || cidsVec.size() == 0)
			return;

		JSONParser parser = new JSONParser();
		int numIDs = 100;
		int iStart = 0;
		for(int i = 0; i < cidsVec.size(); i++) {
			idString += "," + cidsVec.get(i);
			if((i % numIDs == 0 && i != 0) || (i == cidsVec.size() - 1)) {
				idString = idString.substring(1, idString.length());
				String urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + idString + "/xrefs/PatentID/JSON";
				logger.trace(urlname);
				java.io.InputStream stream = null;
				try {
					stream = this.getStreamForPubChemInfo(urlname);
				}
				catch(Exception e) {
					logger.error("Error: Could not fetch Patents.");
					break;
				}
				if(stream == null) {
					String[] tmp_cids = idString.split(",");
					for(int k = 0; k < tmp_cids.length; k++) {
						this.cidToNumberOfPatents.put(tmp_cids[k], 0d);
					}
					idString = "";
					continue;
				}
				JSONObject jsonObject = null;
				try {
					jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
					stream.close();
				} catch (Exception e) {
					idString = "";
					numIDs /= 2;
					i = iStart - 1;
					continue;
				}
				if(jsonObject == null) {
					logger.error("Error: Could not create JSON object for fetching Patents.");
					return;
				}
				JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("InformationList")).get("Information");
				Object[] objs = jsonArray.toArray();
				for(int k = 0; k < objs.length; k++) {
					String cid = String.valueOf(((JSONObject)objs[k]).get("CID"));
					JSONArray ar = (JSONArray)((JSONObject)objs[k]).get("PatentID");
					this.cidToNumberOfPatents.put(cid, ar != null ? ar.size() : 0d);
				}
				idString = "";
				iStart = i + 1;
				numIDs = 100;
			}
		}
	}
	
	protected java.io.InputStream getStreamForPubChemInfo(String urlname) throws Exception {		
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
			logger.error("Error: Could create URL object!");
			return null;
		} catch (IOException e) {
			logger.error("Error: Could not open URL connection!");
			return null;
		}
		finally {
		//	if(connection != null) connection.disconnect();
		}
		return stream;
	
	}
}
