package de.ipbhalle.metfraglib.database;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Vector;

import net.sf.jniinchi.INCHI_RET;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class OnlinePubChemDatabaseMicha {

	//containers to store info
	protected java.util.HashMap<String, String> cidToInChIs = null;
	protected java.util.HashMap<String, String> cidToInChIKeys = null;
	protected java.util.HashMap<String, String> cidToMolecularFormulas = null;
	protected java.util.HashMap<String, Double> cidToMonoisotopicMass = null;
	protected java.util.HashMap<String, Double> cidToXlogP = null;
	protected java.util.HashMap<String, String> cidToSmiles = null;
	protected java.util.HashMap<String, String> cidToIUPACNames = null;
	protected java.util.HashMap<String, String> cidToTitleNames = null;
	//structure parsers
	protected InChIGeneratorFactory inchiFactory;
	public SmilesParser sp;

	public OnlinePubChemDatabaseMicha() {
		this.cidToInChIs = new java.util.HashMap<String, String>();
		this.cidToInChIKeys = new java.util.HashMap<String, String>();
		this.cidToMolecularFormulas = new java.util.HashMap<String, String>();
		this.cidToMonoisotopicMass = new java.util.HashMap<String, Double>();
		this.cidToSmiles = new java.util.HashMap<String, String>();
		this.cidToXlogP = new java.util.HashMap<String, Double>();
		this.cidToIUPACNames = new java.util.HashMap<String, String>();
		this.cidToTitleNames = new java.util.HashMap<String, String>();
	}

	//init functions: take what you like
	public void initInChIFactory() throws CDKException {
		this.inchiFactory = InChIGeneratorFactory.getInstance();
	}

	public void initSmilesParser() {
		this.sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
	}

	/**
	 * Get compound ids based on monoisotopic mass and a relative mass deviation
	 * 
	 * @throws Exception 
	 */
	public java.util.Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws Exception  {
		double mzabs = this.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		double minMass = monoisotopicMass - mzabs;
		double maxMass = monoisotopicMass + mzabs;

		String urlname = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pccompound&term=" + minMass + "[MIMass]:" + maxMass + "[MIMass]&retmode=json&retmax=100000";
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		if(stream == null) return new Vector<String>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonObject == null) {
			throw new Exception();
		}
		Vector<String> cids = new Vector<String>();
		JSONArray jsonArray = (JSONArray)((JSONObject)jsonObject.get("esearchresult")).get("idlist");
		Object[] objs = jsonArray.toArray();
		for(int k = 0; k < objs.length; k++) {
			cids.add((String)objs[k]);
		}
		return cids;
	}

	/**
	 * Get compound ids based on molecular formula
	 * 
	 * @param molecularFormula
	 * @return
	 * @throws Exception
	 */
	public java.util.Vector<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		String urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/formula/" + molecularFormula + "/TXT";
		/*
		 * get stream to retrieve listkey
		 */
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		if(stream == null) return new Vector<String>();
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
		String listKey = "";
		breader.readLine();
		String line = breader.readLine();
		if(line.contains("ListKey:")) {
			String[] tmp = line.trim().split("\\s++");
			if(tmp.length == 2) listKey = tmp[1];
		}
	
		stream.close();
		if(listKey.length() == 0) return new Vector<String>();
		/*
		 * build url to get cids
		 */
		urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/listkey/" + listKey + "/cids/TXT";
		stream = this.getInputStreamFromURL(urlname);
		java.util.Vector<String> cids = new Vector<String>();
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
				stream = this.getInputStreamFromURL(urlname);
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
		
		return cids;
	}

	/**
	 * Get IAtomContainer by one single compound id
	 * 
	 * @throws Exception 
	 * 
	 */
	public IAtomContainer getCandidateByIdentifier(String identifier) throws Exception {
		
		//fetch the hits from PubChem
		Vector<String> ids = this.savingRetrievedHits(new String[] {identifier});
		if(ids == null || ids.size() == 0) return null;
		if(this.cidToInChIs.get(identifier) == null)
			return null;
		
		IAtomContainer container = this.getAtomContainerFromInChI(this.cidToInChIs.get(identifier));
		//if you like to use SMILES uncomment here
		//IAtomContainer container = this.getAtomContainerFromSMILES(this.cidToSmiles.get(identifier));
		this.prepareAtomContainer(container);
		
		return container;
	}

	/**
	 * Get many IAtomContainers by one several compound ids
	 * 
	 * @throws Exception 
	 * 
	 */
	public Vector<IAtomContainer> getCandidateByIdentifier(java.util.Vector<String> identifiers) throws Exception {
		//fetch the hits from PubChem
		Vector<String> ids = this.savingRetrievedHits(identifiers);
		
		Vector<IAtomContainer> containers = new Vector<IAtomContainer>();
		if(ids == null || ids.size() == 0) return containers;

		for(int i = 0; i < ids.size(); i++) {
			if(this.cidToInChIs.get(ids.get(i)) == null) {
				System.err.println("Error: Could not retrieve molecule with ID " + ids.get(i));
				throw new Exception();
			}
				
			IAtomContainer container = this.getAtomContainerFromInChI(this.cidToInChIs.get(ids.get(i)));
			//if you like to use SMILES uncomment here
			//IAtomContainer container = this.getAtomContainerFromSMILES(this.cidToSmiles.get(identifier));
			this.prepareAtomContainer(container);
			container.setProperty("Identifier", ids.get(i));
			//set additional properties that you like ....
			
			containers.addElement(container);
		}

		return containers;
	}

	/**
	 * Get compound information for compound ids in given array
	 * Information is stored in hashtable: cid -> info
	 * 
	 * @param cidsVec
	 * @return
	 */
	protected Vector<String> savingRetrievedHits(String[] cidsVec) throws Exception {
		//first refresh all data containers in case a former search was performed
		this.cidToInChIs = new java.util.HashMap<String, String>();
		this.cidToInChIKeys = new java.util.HashMap<String, String>();
		this.cidToMolecularFormulas = new java.util.HashMap<String, String>();
		this.cidToMonoisotopicMass = new java.util.HashMap<String, Double>();
		this.cidToSmiles = new java.util.HashMap<String, String>();
		this.cidToXlogP = new java.util.HashMap<String, Double>();
		this.cidToIUPACNames = new java.util.HashMap<String, String>();
		this.cidToTitleNames = new java.util.HashMap<String, String>();
		//start
		String idString = "";
		if(cidsVec == null || cidsVec.length == 0)
			return new Vector<String>(); 

		java.util.Vector<String> retrievedCandidates = new Vector<String>();
		for(int i = 0; i < cidsVec.length; i++) {
			idString += "," + cidsVec[i];
			//only fetch 100 at once
			if((i % 100 == 0 && i != 0) || (i == cidsVec.length - 1)) {
				idString = idString.substring(1, idString.length());
				String urlname = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/" + idString + "/property/inchi,XLogP,InChIKey,MolecularFormula,IsotopeAtomCount,IsomericSMILES,MonoisotopicMass/CSV";
				java.io.InputStream stream = this.getInputStreamFromURL(urlname);
				if(stream == null) return new Vector<String>();
				java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
				try {
					String line = "";
					breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
					breader.readLine();
					while((line = breader.readLine()) != null) {
						line = line.replaceAll("\",\"", "|").replace("\",", "|").replace(",\"", "|").replaceAll("\"", "");
						String[] tmp = line.trim().split("\\|");
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
	protected Vector<String> savingRetrievedHits(Vector<String> cidsVec) throws Exception {
		String[] cids = new String[cidsVec.size()];
		for(int i = 0; i < cids.length; i++) {
			cids[i] = cidsVec.get(i);
		}
		return this.savingRetrievedHits(cids);
	}

	/**
	 * just free some space in case you don't use the object anymore
	 */
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
	 * Get compound names from PubChem based on compound id vector
	 * 
	 * @param cidsVec
	 */
	protected void assignTitleNames(Vector<String> cidsVec) throws Exception {
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
				java.io.InputStream stream = this.getStreamForPubChemInfo(urlname);
				if(stream == null) {
					String[] tmp_cids = idString.split(",");
					for(int k = 0; k < tmp_cids.length; k++) {
						this.cidToTitleNames.put(tmp_cids[k], "NA");
					}
					idString = "";
					continue;
				}
				JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
				stream.close();
				stream = null;
				
				if(jsonObject == null) throw new Exception();

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
	 * Section for helper function starts here
	 * 
	 * 
	 */
	
	/**
	 * Get InputStream from PubChemREST URL name
	 * 
	 * @param urlname
	 * @param stream
	 * @return
	 */
	protected java.io.InputStream getStreamForPubChemInfo(String urlname) throws Exception {		
		java.net.HttpURLConnection connection = null;
		java.io.InputStream stream = null;
		int responseCode = 403;
		java.net.URL url = new java.net.URL(urlname);
		connection = (java.net.HttpURLConnection) url.openConnection();
		responseCode = connection.getResponseCode();
		if(responseCode == 404) return null;
		
		if (responseCode != 200) {
			throw new IOException(connection.getResponseMessage());
		}
		stream = connection.getInputStream();
		return stream;
	}
	
	/**
	 * 
	 * @param smiles
	 * @return
	 * @throws Exception
	 */
	protected IAtomContainer getAtomContainerFromSMILES(String smiles) throws Exception {
		if(this.sp == null) {
			System.err.println("SMILES-Parser not initialised");
			throw new Exception();
		}
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		return sp.parseSmiles(smiles);
	}
	
	/**
	 * 
	 * @param inchi
	 * @return
	 * @throws Exception
	 */
	protected IAtomContainer getAtomContainerFromInChI(String inchi) throws Exception {
		if(this.inchiFactory == null) {
			System.err.println("InChI-Factory not initialised");
			throw new Exception();
		}
		InChIToStructure its = this.inchiFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());
		if(its == null) {
			throw new Exception("InChI problem: " + inchi);
		}
		INCHI_RET ret = its.getReturnStatus();
		if (ret == INCHI_RET.WARNING) {
		} else if (ret != INCHI_RET.OKAY) {
			throw new Exception("InChI problem: " + inchi);
		}
		return its.getAtomContainer();
	}
	
	/**
	 * 
	 * @param container
	 * @throws CDKException 
	 */
	protected void prepareAtomContainer(IAtomContainer container) throws CDKException {
		while(true) {
    		try {
        		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
				Aromaticity arom = new Aromaticity(ElectronDonation.cdk(), Cycles.cdkAromaticSet());
				arom.apply(container);
    		} catch (java.lang.NullPointerException e) { 
    			//bad workaround for cdk bug?! but what shall I do...
    			//sometimes NullPointerException occurs but not in one of the further trials?!
        		continue;
        	}
    		break;
        }
        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
        for(int i = 0; i < container.getAtomCount(); i++) {
       		hAdder.addImplicitHydrogens(container, container.getAtom(i));
        }
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        hAdder = null;
	}
	
	/**
	 * 
	 * @param peak
	 * @param ppm
	 * @return
	 */
	protected double calculateAbsoluteDeviation(double peak, double ppm)
	{
		return (peak / 1000000.0) * ppm;
	}
	
	/**
	 * 
	 * @param urlname
	 * @return
	 * @throws Exception
	 */
	protected InputStream getInputStreamFromURL(String urlname) throws Exception {
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
	
	/*
	 * testing
	 */
	public static void main(String[] args) {
		OnlinePubChemDatabaseMicha opcdm = new OnlinePubChemDatabaseMicha();
		//stores compound identifiers
		Vector<String> identifiers = null;
		//stores compound AtomContainers
		Vector<IAtomContainer> containers = null;
		try {
			//only needed in case you generate AtomContainers based on InChI
			opcdm.initInChIFactory();
		} catch (CDKException e) {
			System.err.println("Could not init InChI-Factory");
			//e.printStackTrace();
			return;
		}
		//in case you like to use SMILES
		//opcdm.initSmilesParser();
		
		//get by mass		
		try {
			identifiers = opcdm.getCandidateIdentifiers(253.966126, 5.0);
		} catch (Exception e) {
			System.err.println("Could not perform mass search");
			//e.printStackTrace();
			return;
		}
		
		//get by formula
		try {
			//identifiers = opcdm.getCandidateIdentifiers("C7H5Cl2FN2O3");
		} catch (Exception e) {
			System.err.println("Could not perform molecular formula search");
			//e.printStackTrace();
			return;
		}
		
		//get AtomContainers by compound identifiers
		try {
			containers = opcdm.getCandidateByIdentifier(identifiers);
		} catch (Exception e) {
			System.err.println("Could not retrieve AtomContainers based on compound identifiers");
			//e.printStackTrace();
			return;
		}
		
		System.out.println(containers.size() + " compounds retrieved");
	}
	
	
	
}
