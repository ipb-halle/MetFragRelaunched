package de.ipbhalle.metfraglib.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.ipbhalle.metfraglib.exceptions.NoValidDatabaseSearchSettingsDefined;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.Settings;

public class OnlineExtendedPubChemDatabase extends OnlinePubChemDatabase {

	protected HashMap<String, Double> cidToNumberOfPubMedReferences;
	protected HashMap<String, Double> cidToNumberOfPatents;
	
	public OnlineExtendedPubChemDatabase(Settings settings) {
		super(settings);
		this.cidToNumberOfPubMedReferences = new java.util.HashMap<String, Double>();
		this.cidToNumberOfPatents = new java.util.HashMap<String, Double>();
		Logger.getLogger("org.apache.axiom.util.stax.dialect.StAXDialectDetector").setLevel(Level.ERROR);
	}

	public void nullify() {
		super.nullify();
		this.cidToNumberOfPatents = null;
		this.cidToNumberOfPubMedReferences = null;
	}
	
	public java.util.ArrayList<String> getCandidateIdentifiers() throws Exception {
		if(this.settings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			((ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).setRetrievingStatusString("Retrieving Candidates");
		logger.info("Fetching candidates from PubChem");
		ArrayList<String> cids = new ArrayList<String>(); 
		if(settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null) {
			cids = this.getCandidateIdentifiers((String[])settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		}
		else if(settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null)
		{
			cids = this.getCandidateIdentifiers((String)settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		}
		else if(settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null) {
			cids = this.getCandidateIdentifiers((Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double)settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		}
		else
			try {
				throw new NoValidDatabaseSearchSettingsDefined();
			} catch (NoValidDatabaseSearchSettingsDefined e) {
				e.printStackTrace();
			}
		/*
		 * retrieve extended information
		 *
		logger.info("Fetching PubMed references");
		statusString = "Retrieving References";
		this.assignNumberOfPubMedReferences(cids);
		logger.info("Fetching patents");
		statusString = "Retrieving Patents";
		this.assignNumberOfPatents(cids);
		statusString = "Retrieving Candidates";
		*/
		return cids;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public ICandidate getCandidateByIdentifier(String identifier) throws Exception {
		
		ICandidate candidate = super.getCandidateByIdentifier(identifier);
		if(candidate == null) return null;
		ArrayList<String> cid = new ArrayList<String>();
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
		
		this.addExtendedInformation(candidate);
		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving Candidates");
		return candidate;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public CandidateList getCandidateByIdentifier(java.util.ArrayList<String> identifiers) throws Exception {
		
		CandidateList candidates = super.getCandidateByIdentifier(identifiers);
		
		ArrayList<String> cids = new ArrayList<String>();
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			cids.add(candidates.getElement(i).getIdentifier());
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
		
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			this.addExtendedInformation(candidates.getElement(i));
		}

		if(processingStatus != null) processingStatus.setRetrievingStatusString("Retrieving Candidates");
		return candidates;
	}
	
	private void addExtendedInformation(ICandidate candidate) {
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
	protected void assignNumberOfPubMedReferences(ArrayList<String> cidsVec) {
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
	
	/**
	 * 
	 * @param cidsVec
	 */
	protected void assignNumberOfPatents(ArrayList<String> cidsVec) {
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
					String[] tmp = idString.split(",");
					for(int k = 0; k < tmp.length; k++) {
						this.cidToNumberOfPatents.put(tmp[k], 0d);
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
	
}
