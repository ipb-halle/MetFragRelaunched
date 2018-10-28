package de.ipbhalle.metfraglib.database;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;

public class OnlineMetaCycDatabase extends AbstractDatabase {
	
	private java.net.HttpURLConnection connection;
	
	public OnlineMetaCycDatabase(Settings settings) {
		super(settings);
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public java.util.ArrayList<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws Exception {
		logger.info("Fetching candidates from MetaCyc");
		java.util.ArrayList<String> cids = new java.util.ArrayList<String>();
		String urlname = "https://websvc.biocyc.org/META/monoisotopicwt?wts=" + monoisotopicMass + "&tol=" + relativeMassDeviation;
		if(logger.isTraceEnabled()) logger.trace(urlname);
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		if(stream == null) return cids;
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.length() != 0) {
					String[] tmp = line.split("\\t");
					if(tmp != null && tmp.length >= 5 && tmp[1].equals("1")) {
						cids.add(tmp[4]);
					}
				}
			}
			stream.close();
			breader.close();
		} catch (java.io.IOException e) {
			logger.error("Error: Could not open result stream when using BioCyc mass search!");
			throw new Exception();
		}
        this.connection.disconnect();
        this.connection = null;
		return cids;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public java.util.ArrayList<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		logger.info("Fetching candidates from MetaCyc");
		java.util.ArrayList<String> cids = new java.util.ArrayList<String>();
		String urlname = "https://websvc.biocyc.org/META/CF?cfs=" + molecularFormula;
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		if(logger.isTraceEnabled()) logger.trace(urlname);
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.length() != 0) {
					String[] tmp = line.split("\\t");
					if(tmp != null && tmp.length >= 4 && tmp[1].equals("1")) {
						cids.add(tmp[2]);
					}
				}
			}
			stream.close();
			breader.close();
		} catch (java.io.IOException e) {
			logger.error("Error: Could not open result stream when using BioCyc sum formula search!");
			throw new Exception();
		}
        this.connection.disconnect();
        this.connection = null;
		return cids;
	}

	//ToDo: check whether identifiers are valid and exist
	public java.util.ArrayList<String> getCandidateIdentifiers(java.util.ArrayList<String> identifiers) {
		logger.info("Fetching candidates from MetaCyc");
		return identifiers;
	}
	
	public ICandidate getCandidateByIdentifierInChI(String identifier) throws Exception {
		String urlname = "https://www.biocyc.org/getxml?id=META:" + identifier + "&detail=high";
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		if(logger.isTraceEnabled()) logger.trace(urlname);
		if(stream == null) return null;
    	
    	SAXReader reader = new SAXReader();
    	Document doc = reader.read(new URL(urlname));
    	Node rootNode = null;
    	try {
    		rootNode = (Node)doc.selectNodes("/ptools-xml/Compound").get(0);
    	}
    	catch(Exception e) {
    		System.out.println(doc.getRootElement().asXML());
    	}

    	ICandidate precursorCandidate = null;
    	
    	String inchikey = "";
    	String inchi = "";
    	String name = "";
    	String formula = "";
    	
    	for(Object node : rootNode.selectNodes("*")) { 
	    	Node currentNode = (Node)node;
    		if(currentNode.getName().equals("inchi-key")) {
    			Element elem = (Element) currentNode;
    			inchikey = elem.getText().trim().split("=")[1];
    		}
    		else if(currentNode.getName().equals("inchi")) {
    			Element elem = (Element) currentNode;
    			inchi = elem.getText().trim();
    		}
    		else if(currentNode.getName().equals("cml")) {
    			Node molNode = (Node) currentNode.selectNodes("molecule").get(0);
	    		name = ((Element)currentNode.selectNodes("molecule").get(0)).attributeValue("title");
	    		for(Object subMolObject : molNode.selectNodes("*")) {
	    			Node subMolNode = (Node) subMolObject;
	    			if(subMolNode.getName().equals("formula")) 
	    				formula = ((Element)subMolNode).attributeValue("concise").replaceAll("\\s*", "");
	    		}
    		}
    	}
    	
        precursorCandidate = new TopDownPrecursorCandidate(inchi, identifier);
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchikey.split("-")[0]);
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchikey.split("-")[1]);
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_NAME, inchikey);
		precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formula);
		precursorCandidate.setProperty(VariableNames.COMPOUND_NAME_NAME, name);
		try {
			precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, new de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula(formula).getMonoisotopicMass());	
		}
		catch(Exception e) {
			throw new Exception ();
		}
    	
		return precursorCandidate;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public ICandidate getCandidateByIdentifier(String identifier) throws Exception {
		String urlname = "https://www.biocyc.org/getxml?id=META:" + identifier + "&detail=low";
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		if(logger.isTraceEnabled()) logger.trace(urlname);
		if(stream == null) return null;
    	
    	SAXReader reader = new SAXReader();
    	Document doc = reader.read(stream);
    	
    	String name = ((Element)doc.selectNodes("/ptools-xml/Compound/common-name").get(0)).getText();
    	
    	Node rootNode = null;
    	try {
    		rootNode = (Node)doc.selectNodes("/ptools-xml/Compound/cml/molecule").get(0);
    	}
    	catch(Exception e) {
    		System.out.println(doc.getRootElement().asXML());
    	}
    	ICandidate precursorCandidate = null;
    	
    	String smiles = "";
    	String formula = "";
    	
    	for(Object node : rootNode.selectNodes("*")) { 
	    	Node currentNode = (Node)node;
    		if(currentNode.getName().equals("string")) {
    			Element elem = (Element) currentNode;
    			if(!elem.attributeValue("title").equals("smiles")) continue;
    			smiles = elem.getText();
    		}
    		else if(currentNode.getName().equals("formula")) {
    			formula = ((Element)currentNode).attributeValue("concise").replaceAll("\\s*", "");
    		}
    	}
    	IAtomContainer con = MoleculeFunctions.parseSmiles(smiles);
		
    	String[] inchiInfo = MoleculeFunctions.getInChIInfoFromAtomContainer(con);
        precursorCandidate = new TopDownPrecursorCandidate(inchiInfo[0], identifier);
        
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchiInfo[1].split("-")[0]);
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchiInfo[1].split("-")[1]);
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_NAME, inchiInfo[1]);
		precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formula);
		precursorCandidate.setProperty(VariableNames.COMPOUND_NAME_NAME, name);
		try {
			precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, new de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula(formula).getMonoisotopicMass());	
		}
		catch(Exception e) {
			throw new Exception ();
		}
    	
		return precursorCandidate;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public CandidateList getCandidateByIdentifier(java.util.ArrayList<String> identifiers) throws Exception {
		CandidateList candidates = new CandidateList();
		for(int i = 0; i < identifiers.size(); i++) {
			ICandidate candidate = this.getCandidateByIdentifier(identifiers.get(i));
			if(candidate != null) candidates.addElement(candidate);
		}
		return candidates;
	}

	public void nullify() {
		
	}

	private InputStream getInputStreamFromURL(String urlname) throws Exception {
		InputStream stream = null;
		
		try {
			URL url = new URL(urlname);
			Proxy proxy = null;
			if(this.settings.containsKey(VariableNames.METACYC_PROXY_SERVER) && this.settings.containsKey(VariableNames.METACYC_PROXY_PORT)
					&& this.settings.get(VariableNames.METACYC_PROXY_SERVER) != null && this.settings.get(VariableNames.METACYC_PROXY_PORT) != null) {
				proxy = new Proxy(Proxy.Type.HTTP, 
							new InetSocketAddress(
									(String)this.settings.get(VariableNames.METACYC_PROXY_SERVER), 
									(Integer)this.settings.get(VariableNames.METACYC_PROXY_PORT))
							);
			}

			if(proxy == null) {
				this.connection = (HttpURLConnection) url.openConnection();
			}
			else {
				this.connection = (HttpURLConnection) url.openConnection(proxy);
			}
			
			this.connection.setConnectTimeout(30000);
			this.connection.setReadTimeout(30000);
			/*
			if (this.connection.getResponseCode() != 200) {
				System.out.println("throwing exception");
				throw new IOException(connection.getResponseMessage());
			}
			*/
			stream = this.connection.getInputStream();
		} catch(MalformedURLException mue) {
			logger.error("Error: Could create URL object! " + urlname);
			throw new Exception();
		} catch (IOException e) {
			logger.error("Error: Could not open URL connection! " + urlname);
			throw new Exception();
		}
		
		return stream;
	}
	
	
	public static void main(String[] args) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 610.15331);
		settings.set(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, 10.0);
		
		settings.set(VariableNames.METACYC_PROXY_SERVER, "www-cache.ipb-halle.de");
		settings.set(VariableNames.METACYC_PROXY_PORT, 3128);
		
		OnlineMetaCycDatabase db = new OnlineMetaCycDatabase(settings);
		
		java.util.ArrayList<String> ids = db.getCandidateIdentifiers(610.15331, 10.0);
		System.out.println("got " + ids.size() + " candidates");
		CandidateList list = db.getCandidateByIdentifier(ids);
		System.out.println(list.getNumberElements());
		//http://websvc.biocyc.org/META/monoisotopicwt?wts=360.15728850299996&tol=5.0
		//http://www.biocyc.org/getxml?id=META:CPD-8908&detail=low
	}
}
