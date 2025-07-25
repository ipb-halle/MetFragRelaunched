package de.ipbhalle.metfraglib.database;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.functions.HelperFunctions;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.Settings;

public class OnlinePubChemDatabase extends AbstractDatabase {

	protected java.util.HashMap<String, String> cidToInChIs = null;
	protected java.util.HashMap<String, String> cidToInChIKeys = null;
	protected java.util.HashMap<String, String> cidToMolecularFormulas = null;
	protected java.util.HashMap<String, Double> cidToMonoisotopicMass = null;
	protected java.util.HashMap<String, Double> cidToXlogP = null;
	protected java.util.HashMap<String, String> cidToSmiles = null;
	protected java.util.HashMap<String, String> cidToIUPACNames = null;
	protected java.util.HashMap<String, String> cidToTitleNames = null;
	protected boolean formulaSearch;

	public OnlinePubChemDatabase(Settings settings) {
		super(settings);
		this.formulaSearch = false;
		this.cidToInChIs = new java.util.HashMap<String, String>();
		this.cidToInChIKeys = new java.util.HashMap<String, String>();
		this.cidToMolecularFormulas = new java.util.HashMap<String, String>();
		this.cidToMonoisotopicMass = new java.util.HashMap<String, Double>();
		this.cidToSmiles = new java.util.HashMap<String, String>();
		this.cidToXlogP = new java.util.HashMap<String, Double>();
		this.cidToIUPACNames = new java.util.HashMap<String, String>();
		this.cidToTitleNames = new java.util.HashMap<String, String>();
		
		Logger.getLogger("org.apache.axiom.util.stax.dialect.StAXDialectDetector").setLevel(Level.ERROR);
	}

	public java.util.ArrayList<String> getCandidateIdentifiers() throws Exception {
		if(this.settings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			((ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).setRetrievingStatusString("Retrieving Candidates");
		java.util.ArrayList<String> identifiers = null;
		logger.info("Fetching candidates from PubChem");
		if(settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null) 
			identifiers = this.getCandidateIdentifiers((String[])settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		else if(settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null)
			identifiers = this.getCandidateIdentifiers((String)settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		else if(settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null)
			identifiers = this.getCandidateIdentifiers((Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double)settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		if(identifiers != null) {
		//	statusString = "Fetching " + identifiers.size() + " Candidates";
			return identifiers;
		}
		return new java.util.ArrayList<String>();
	}

	/**
	 * get cids based on the monoisotopic mass
	 * query is performed via eutils API
	 * @throws Exception 
	 */
	public java.util.ArrayList<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws Exception  {
		double mzabs = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		double minMass = monoisotopicMass - mzabs;
		double maxMass = monoisotopicMass + mzabs;

		String urlname = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pccompound&term=" + minMass + "[MIMass]:" + maxMass + "[MIMass]&retmode=json&retmax=100000";
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new ArrayList<String>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonObject == null) {
			logger.error("Error: Could not create JSON object for fetching candidates by mass.");
			throw new Exception();
		}
		ArrayList<String> cids = new ArrayList<String>();
		JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("esearchresult")).get("idlist");
		Object[] objs = jsonArray.toArray();
		for(int k = 0; k < objs.length; k++) {
			cids.add((String)objs[k]);
		}
		//ArrayList<String> retrievedHits = this.savingRetrievedHits(cids);
		//if(retrievedHits == null) throw new Exception();
		//return retrievedHits;
		return cids;
	}

	/**
	 * get cids based on molecular formula
	 * query is performed via PUB REST API
	 */
	public java.util.ArrayList<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		this.formulaSearch = true;
		String urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/formula/" + molecularFormula + "/TXT";
		/*
		 * get stream to retrieve listkey
		 */
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new ArrayList<String>();
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
		String listKey = "";
		logger.trace(urlname);
		breader.readLine();
		String line = breader.readLine();
		if(line.contains("ListKey:")) {
			String[] tmp = line.trim().split("\\s++");
			if(tmp.length == 2) listKey = tmp[1];
		}
	
		stream.close();
		if(listKey.length() == 0) return new ArrayList<String>();
		/*
		 * build url to get cids
		 */
		urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/listkey/" + listKey + "/cids/TXT";
		logger.trace(urlname);
		stream = HelperFunctions.getInputStreamFromURL(urlname);
		java.util.ArrayList<String> cids = new ArrayList<String>();
		if(stream == null) {
			return cids;
		}
		/*
		 * parse cids
		 */
		line = "";
		breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
		
		while((line = breader.readLine()) != null) {
			/*
			 * check whether the query has finished
			 */
			if(line.startsWith("Your")) {
				/*
				 * if not fetch the url stream again
				 */
				stream = HelperFunctions.getInputStreamFromURL(urlname, this.settings.get(VariableNames.PUBCHEM_PROXY_SERVER), this.settings.get(VariableNames.PUBCHEM_PROXY_PORT));
				breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else
				/*
				 * in case the query has finished store the cids
				 */
				cids.add(0, line.trim());
		}
		stream.close();
		breader.close();
		//ArrayList<String> retrievedHits = savingRetrievedHits(cids);
		//if(retrievedHits == null) throw new Exception();
		//return retrievedHits;
		return cids;
	}

	/**
	 * 
	 */
	public java.util.ArrayList<String> getCandidateIdentifiers(String[] identifiers) throws Exception 
	{
		ArrayList<String> cids = null;
		cids = this.savingRetrievedHits(identifiers);
		if(cids != null)
			return cids;
		throw new Exception();
	}

	/**
	 * 
	 */
	public java.util.ArrayList<String> getCandidateIdentifiers(ArrayList<String> identifiers) throws Exception {
		logger.info("Fetching candidates from PubChem");
		ArrayList<String> cids = null;
		cids = this.savingRetrievedHits(identifiers);
		if(cids != null)
			return cids;
		else return new ArrayList<String>();
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public ICandidate getCandidateByIdentifier(String identifier) throws Exception {
		ArrayList<String> ids = this.savingRetrievedHits(new String[] {identifier});
		if(ids == null || ids.size() == 0) return null;
		if(this.cidToInChIs.get(identifier) == null)
			return null;
		
		ICandidate candidate = new TopDownPrecursorCandidate(this.cidToInChIs.get(identifier), identifier);
		candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, this.cidToInChIKeys.get(identifier).split("-")[0]);
		candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, this.cidToInChIKeys.get(identifier).split("-")[1]);
		candidate.setProperty(VariableNames.INCHI_KEY_NAME, this.cidToInChIKeys.get(identifier));
		candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, this.cidToMolecularFormulas.get(identifier).replaceAll("[\\+\\-][0-9]*", ""));
		candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, this.cidToMonoisotopicMass.get(identifier));
		candidate.setProperty(VariableNames.SMILES_NAME, this.cidToSmiles.get(identifier));
		candidate.setProperty(VariableNames.PUBCHEM_XLOGP_NAME, this.cidToXlogP.get(identifier));
		candidate.setProperty(VariableNames.IUPAC_NAME_NAME, this.cidToIUPACNames.get(identifier));
	//	candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, this.cidToTitleNames.get(identifier) == null ? "NA" : this.cidToTitleNames.get(identifier));
		
		return candidate;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public CandidateList getCandidateByIdentifier(java.util.ArrayList<String> identifiers) throws Exception {
		ArrayList<String> ids = this.savingRetrievedHits(identifiers);
		CandidateList candidates = new CandidateList(); 
		if(ids == null || ids.size() == 0) return candidates;

		for(int i = 0; i < ids.size(); i++) {
			if(this.cidToInChIs.get(ids.get(i)) == null)
				return null;
			
			ICandidate candidate = new TopDownPrecursorCandidate(this.cidToInChIs.get(ids.get(i)), ids.get(i));
			candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, this.cidToInChIKeys.get(ids.get(i)).split("-")[0]);
			candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, this.cidToInChIKeys.get(ids.get(i)).split("-")[1]);
			candidate.setProperty(VariableNames.INCHI_KEY_NAME, this.cidToInChIKeys.get(ids.get(i)));
			candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, this.cidToMolecularFormulas.get(ids.get(i)).replaceAll("[\\+\\-][0-9]*", ""));
			candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, this.cidToMonoisotopicMass.get(ids.get(i)));
			candidate.setProperty(VariableNames.SMILES_NAME, this.cidToSmiles.get(ids.get(i)));
			candidate.setProperty(VariableNames.PUBCHEM_XLOGP_NAME, this.cidToXlogP.get(ids.get(i)) == null ? "NA" : this.cidToXlogP.get(ids.get(i)));
			candidate.setProperty(VariableNames.IUPAC_NAME_NAME, this.cidToIUPACNames.get(ids.get(i)));
		//	candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, this.cidToTitleNames.get(ids.get(i)) == null ? "NA" : this.cidToTitleNames.get(identifiers.get(i)));
			
			candidates.addElement(candidate);
		}

		
		return candidates;
	}

	/**
	 * 
	 * @param molecule
	 * @return
	 */
	protected boolean containsOnlyKnownElements(IAtomContainer molecule) {
		for(int i = 0; i < molecule.getAtomCount(); i++) {
			if(Constants.ELEMENTS.indexOf(molecule.getAtom(i).getSymbol()) == -1)
				return false;
		}
		return true;
	}

	/**
	 * 
	 * @param cidsVec
	 * @return
	 */
	private ArrayList<String> savingRetrievedHits(String[] cidsVec) throws Exception {
		String idString = "";
		if(cidsVec == null || cidsVec.length == 0)
			return new ArrayList<String>(); 

		java.util.ArrayList<String> retrievedCandidates = new ArrayList<String>();
		for(int i = 0; i < cidsVec.length; i++) {
			idString += "," + cidsVec[i];
			if((i % 100 == 0 && i != 0) || (i == cidsVec.length - 1)) {
				idString = idString.substring(1, idString.length());
				String urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + idString + "/property/inchi,XLogP,InChIKey,MolecularFormula,IsotopeAtomCount,SMILES,MonoisotopicMass,IUPACName/CSV";
				logger.trace(urlname);
				java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
				if(stream == null) return new ArrayList<String>();
				java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
				try {
					String line = "";
					breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
					breader.readLine();
					while((line = breader.readLine()) != null) {
						line = line.replaceAll("\",\"", "|").replace("\",", "|").replace(",\"", "|").replaceAll("\"", "");
						String[] tmp = line.trim().split("\\|");
						/*
						 * in case formula was used remove all isotopically labelled compounds
						 */
						/*
						if(this.formulaSearch) {
							try {
								int isotopeAtomCount = Integer.parseInt(tmp[5].trim());
								if(isotopeAtomCount > 0) continue;
							}
							catch(Exception e) {
								//just go on and discard current candidate
								continue;
							}
						}
						*/
						this.cidToInChIs.put(tmp[0].trim(), tmp[1].trim());
						this.cidToInChIKeys.put(tmp[0].trim(), tmp[3].trim());
						this.cidToMolecularFormulas.put(tmp[0].trim(), tmp[4].trim());
						this.cidToMonoisotopicMass.put(tmp[0].trim(), Double.parseDouble(tmp[7].trim()));
						this.cidToSmiles.put(tmp[0].trim(), tmp[6].trim());
						this.cidToXlogP.put(tmp[0].trim(), tmp[2].trim().length() != 0 ? Double.parseDouble(tmp[2].trim()) : null);
						this.cidToIUPACNames.put(tmp[0].trim(), tmp.length > 8 ? tmp[8].trim() : "NA");
						
						retrievedCandidates.add(tmp[0].trim());
					}
					stream.close();
					breader.close();
					idString = "";
				} catch (java.io.IOException e) {
					logger.error("Error: Could not open result stream when using Pubchem HTTP request!");
					throw new Exception();
				}
				finally {
					stream.close();
					breader.close();
				}
			}
		}
	//	this.assignTitleNames(retrievedCandidates);
		return retrievedCandidates;
	}
	
	/**
	 * 
	 * @param cidsVec
	 * @return
	 */
	private ArrayList<String> savingRetrievedHits(ArrayList<String> cidsVec) throws Exception {
		String[] cids = new String[cidsVec.size()];
		for(int i = 0; i < cids.length; i++) {
			cids[i] = cidsVec.get(i);
		}
		return savingRetrievedHits(cids);
	}

	public void nullify() {
		this.cidToInChIKeys = null;
		this.cidToInChIs = null;
		this.cidToMolecularFormulas = null;
		this.cidToSmiles = null;
		this.cidToMonoisotopicMass = null;
		this.cidToXlogP = null;
		this.cidToIUPACNames = null;
		this.cidToTitleNames = null;
	}
	
	/**
	 * 
	 * @param cidsVec
	 */
	protected void assignTitleNames(ArrayList<String> cidsVec) {
		String idString = "";
		this.cidToTitleNames = new java.util.HashMap<String, String>();
		if(cidsVec == null || cidsVec.size() == 0)
			return;

		JSONParser parser = new JSONParser();
		for(int i = 0; i < cidsVec.size(); i++) {
			idString += "," + cidsVec.get(i);
			if((i % 100 == 0 && i != 0) || (i == cidsVec.size() - 1)) {
				idString = idString.substring(1, idString.length());
				String urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + idString + "/description/JSON";
				logger.trace(urlname);
				java.io.InputStream stream = null;
				try {
					stream = this.getStreamForPubChemInfo(urlname);
				} catch (Exception e1) {
					
				}
				if(stream == null) {
					String[] tmp_cids = idString.split(",");
					for(int k = 0; k < tmp_cids.length; k++) {
						this.cidToTitleNames.put(tmp_cids[k], "NA");
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
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if(jsonObject == null) {
					logger.error("Error: Could not create JSON object for fetching PubMed references.");
					return;
				}
				JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("InformationList")).get("Information");
				Object[] objs = jsonArray.toArray();
				for(int k = 0; k < objs.length; k++) {
					String cid = String.valueOf(((JSONObject)objs[k]).get("CID"));
					String titleName = String.valueOf(((JSONObject)objs[k]).get("Title"));
					if(!this.cidToTitleNames.containsKey(cid)) this.cidToTitleNames.put(cid, titleName != null ? titleName : "NA");
				}
				idString = "";
			}
		}
	}


	/**
	 * 
	 * @param urlname
	 * @param stream
	 * @return
	 */
	protected java.io.InputStream getStreamForPubChemInfo(String urlname) throws Exception {		
		java.net.HttpURLConnection connection = null;
		java.io.InputStream stream = null;
		int responseCode = 403;
		try {
			java.net.URL url = new java.net.URL(urlname);

			Proxy proxy = null;
			if(this.settings.containsKey(VariableNames.PUBCHEM_PROXY_SERVER) && this.settings.containsKey(VariableNames.PUBCHEM_PROXY_PORT)
					&& this.settings.get(VariableNames.PUBCHEM_PROXY_SERVER) != null && this.settings.get(VariableNames.PUBCHEM_PROXY_PORT) != null) {
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((String)this.settings.get(VariableNames.PUBCHEM_PROXY_SERVER), (Integer)this.settings.get(VariableNames.PUBCHEM_PROXY_PORT)));
			}

			if(proxy == null) connection = (HttpURLConnection) url.openConnection();
			else connection = (HttpURLConnection) url.openConnection(proxy);
			
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
