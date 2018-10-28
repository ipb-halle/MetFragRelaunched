package de.ipbhalle.metfraglib.database;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class OnlineKeggDatabase extends AbstractDatabase {
	
	private java.net.HttpURLConnection connection;
	
	public OnlineKeggDatabase(Settings settings) {
		super(settings);
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public java.util.ArrayList<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws Exception {
		logger.info("Fetching candidates from KEGG");
		java.util.ArrayList<String> cids = new java.util.ArrayList<String>();
		double mzabs = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		String urlname = "http://rest.kegg.jp/find/compound/" + (monoisotopicMass - mzabs) + "-" + (monoisotopicMass + mzabs) + "/exact_mass";
		logger.trace(urlname);
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		if(stream == null) return cids;
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.length() != 0) {
					String[] tmp = line.split("\\s+");
					if(tmp != null && tmp.length != 0) {
						if(tmp[0].startsWith("cpd:")) tmp[0] = tmp[0].substring(4);
						cids.add(tmp[0]);
					}
				}
			}
			stream.close();
			breader.close();
		} catch (java.io.IOException e) {
			logger.error("Error: Could not open result stream when using KEGG REST mass search!");
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
		logger.info("Fetching candidates from KEGG");
		java.util.ArrayList<String> cids = new java.util.ArrayList<String>();
		String urlname = "http://rest.kegg.jp/find/compound/" + molecularFormula + "/formula";
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		logger.trace(urlname);
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.length() != 0) {
					String[] tmp = line.split("\\s+");
					if(tmp != null && tmp.length == 2 && tmp[1].trim().equals(molecularFormula)) { 
						if(tmp[0].startsWith("cpd:")) tmp[0] = tmp[0].substring(4);
						cids.add(tmp[0]);
					}
				}
			}
			stream.close();
			breader.close();
		} catch (java.io.IOException e) {
			logger.error("Error: Could not open result stream when using KEGG REST sum formula search!");
			throw new Exception();
		}
        this.connection.disconnect();
        this.connection = null;
		return cids;
	}

	//ToDo: check whether identifiers are valid and exist
	public java.util.ArrayList<String> getCandidateIdentifiers(java.util.ArrayList<String> identifiers) {
		return identifiers;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public ICandidate getCandidateByIdentifier(String identifier) throws Exception {
		String urlname = "http://rest.kegg.jp/get/" + identifier + "/mol";
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		logger.trace(urlname);
		if(stream == null) return null;
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line;
    	try {
			while ((line = breader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (java.io.IOException e) {
			logger.error("Error: Reading mol information out of stream failed.");
			throw new Exception();
		} 
		java.util.List<IAtomContainer> containersList;
		
		MDLV2000Reader reader = new MDLV2000Reader(new java.io.StringReader(sb.toString()));
        ChemFile chemFile = null;
        try {
        	chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
        } catch(Exception e) {
            try {
				reader.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        	return null;
        }
        containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
        this.connection.disconnect();
        this.connection = null;
        
        MoleculeFunctions.prepareAtomContainer(containersList.get(0), true);
        
        try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        String[] inchiInfo = MoleculeFunctions.getInChIInfoFromAtomContainer(containersList.get(0));
        ICandidate precursorCandidate = new TopDownPrecursorCandidate(inchiInfo[0], identifier);
        
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchiInfo[1].split("-")[0]);
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchiInfo[1].split("-")[1]);
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_NAME, inchiInfo[1]);
		precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, inchiInfo[0].split("/")[1]);
		precursorCandidate.setProperty(VariableNames.COMPOUND_NAME_NAME, this.getNameIfIdentifier(identifier));
		try {
			precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, new de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula(inchiInfo[0].split("/")[1]).getMonoisotopicMass());	
		}
		catch(Exception e) {
			throw new Exception ();
		}
		
		return precursorCandidate;
	}

	/**
	 * 
	 * @param identifier
	 * @return
	 * @throws Exception 
	 */
	protected String getNameIfIdentifier(String identifier) throws Exception {
		String urlname = "http://rest.kegg.jp/find/compound/" + identifier;
		java.io.InputStream stream = this.getInputStreamFromURL(urlname);
		logger.trace(urlname);
		if(stream == null) return null;
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
		String name = "NA";
		try {
			String line = breader.readLine();
	    	if(line != null) { 
				line = line.trim();
				String[] tmp = line.split("\\s+");
				if(tmp.length > 1) {
					name = "";
					for(int i = 1; i < tmp.length; i++) 
						name += tmp[i] + " ";
					name = name.split(";")[0];
				}
			}
		} catch (java.io.IOException e) {
			logger.error("Error: Reading mol information out of stream failed.");
			throw new Exception();
		}
		return name.trim();
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
			if(this.settings.containsKey(VariableNames.KEGG_PROXY_SERVER) && this.settings.containsKey(VariableNames.KEGG_PROXY_PORT)
					&& this.settings.get(VariableNames.KEGG_PROXY_SERVER) != null && this.settings.get(VariableNames.KEGG_PROXY_PORT) != null) {
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress((String)this.settings.get(VariableNames.KEGG_PROXY_SERVER), (Integer)this.settings.get(VariableNames.KEGG_PROXY_PORT)));
			}

			if(proxy == null) this.connection = (HttpURLConnection) url.openConnection();
			else this.connection = (HttpURLConnection) url.openConnection(proxy);
			
			this.connection.setConnectTimeout(10000);
			this.connection.setReadTimeout(10000);
			/*
			if (this.connection.getResponseCode() != 200) {
				System.out.println("throwing exception");
				throw new IOException(connection.getResponseMessage());
			}
			*/
			stream = connection.getInputStream();
		} catch(MalformedURLException mue) {
			logger.error("Error: Could not create URL object! " + urlname);
			throw new Exception();
		} catch (IOException e) {
			logger.error("Error: Could not open URL connection! " + urlname);
			e.printStackTrace();
			throw new Exception();
		}
		
		return stream;
	}
	
}
