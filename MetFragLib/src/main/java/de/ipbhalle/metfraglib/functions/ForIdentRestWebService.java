package de.ipbhalle.metfraglib.functions;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class ForIdentRestWebService {

	protected Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public java.util.Vector<String> getCategories() throws Exception {
		//https://water.for-ident.org/api/categories
		String urlname = "https://water.for-ident.org/api/categories";
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new java.util.Vector<String>();
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonArray == null) {
			logger.error("Error: Could not create JSON object for fetching categories.");
			throw new Exception();
		}
		java.util.Vector<String> categories = new java.util.Vector<String>();
		
		Object[] objs = jsonArray.toArray();
		for(int k = 0; k < objs.length; k++) {
			JSONObject obj = (JSONObject)objs[k];
			categories.add((String)obj.get("value"));
		}
		return categories;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public java.util.Vector<String> getInChIKeys() throws Exception {
		//https://water.for-ident.org/api/substances/inchiKeys
		String urlname = "https://water.for-ident.org/api/substances/inchiKeys";
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new java.util.Vector<String>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonObject == null) {
			logger.error("Error: Could not create JSON object for fetching InChIKeys.");
			throw new Exception();
		}
		java.util.Vector<String> inchikeys = new java.util.Vector<String>();
		Long numberPages = (Long)jsonObject.get("totalPages");
	
		for(int i = 0; i < numberPages; i++) {
			urlname = "https://water.for-ident.org/api/substances/inchiKeys?page=" + i;
			stream = HelperFunctions.getInputStreamFromURL(urlname);
			jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
			stream.close();
			
			JSONArray jsonArray = (JSONArray)jsonObject.get("content");
			Object[] objs = jsonArray.toArray();
			for(int k = 0; k < objs.length; k++) {
				String inchikey = (String)objs[k];
				if(inchikey != null)
					inchikeys.add(inchikey);
			}
		}
		return inchikeys;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public java.util.Vector<String[]> getInChIKeysAndIdentifiers() throws Exception {
		//https://water.for-ident.org/api/substances/inchiKeys
		String urlname = "https://water.for-ident.org/api/substances/inchiKeysAndStoffidentIds";
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new java.util.Vector<String[]>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonObject == null) {
			logger.error("Error: Could not create JSON object for fetching InChIKeys and identifiers.");
			throw new Exception();
		}
		java.util.Vector<String[]> inchikeysIdentifiers = new java.util.Vector<String[]>();
		Long numberPages = (Long)jsonObject.get("totalPages");
	
		for(int i = 0; i < numberPages; i++) {
			urlname = "https://water.for-ident.org/api/substances/inchiKeysAndStoffidentIds?page=" + i;
			stream = HelperFunctions.getInputStreamFromURL(urlname);
			jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
			stream.close();
			
			JSONArray jsonArray = (JSONArray)jsonObject.get("content");
			Object[] objs = jsonArray.toArray();
			for(int k = 0; k < objs.length; k++) {
				JSONArray entry = (JSONArray)objs[k];
				//if(entry.size() == 2 && entry.get(0) != null) 
				if(entry.get(0) == null) inchikeysIdentifiers.add(new String[] {(String)entry.get(0), (String)entry.get(1)});
			}
		}
		return inchikeysIdentifiers;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public java.util.Vector<String> getIdentifiers() throws Exception {
		//https://water.for-ident.org/api/substances/publicIds
		String urlname = "https://water.for-ident.org/api/substances/stoffidentIds";
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new java.util.Vector<String>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonObject == null) {
			logger.error("Error: Could not create JSON object for fetching InChIKeys.");
			throw new Exception();
		}
		java.util.Vector<String> identifiers = new java.util.Vector<String>();
		Long numberPages = (Long)jsonObject.get("totalPages");
	
		for(int i = 0; i < numberPages; i++) {
			urlname = "https://water.for-ident.org/api/substances/stoffidentIds?page=" + i;
			stream = HelperFunctions.getInputStreamFromURL(urlname);
			jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
			stream.close();
			
			JSONArray jsonArray = (JSONArray)jsonObject.get("content");
			Object[] objs = jsonArray.toArray();
			for(int k = 0; k < objs.length; k++) {
				String identifier = (String)objs[k];
				if(identifier != null)
					identifiers.add(identifier);
			}
		}
		return identifiers;
	}
	
	/**
	 * 
	 * @param mass
	 * @param mzppm
	 * @return
	 * @throws Exception
	 */
	public CandidateList getCandidatesByMass(double mass, double mzppm) throws Exception {
		//https://water.for-ident.org/api/substances?accurateMassMin=100&accurateMassMax=200
		double mzabs = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		String urlname = "https://water.for-ident.org/api/substances?accurateMassMin=" + (mass - mzabs) + "&accurateMassMax=" + (mass + mzabs);
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new CandidateList();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonObject == null) {
			logger.error("Error: Could not create JSON object for fetching candidates by monoisotopic mass.");
			throw new Exception();
		}
		CandidateList candidateList = new CandidateList();
		Long numberPages = (Long)jsonObject.get("totalPages");
	
		for(int i = 0; i < numberPages; i++) {
			urlname = "https://water.for-ident.org/api/substances?accurateMassMin=" + (mass - mzabs) + "&accurateMassMax=" + (mass + mzabs) + "&page=" + i;
			stream = HelperFunctions.getInputStreamFromURL(urlname);
			jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
			stream.close();
			
			JSONArray jsonArray = (JSONArray)jsonObject.get("content");
			Object[] objs = jsonArray.toArray();
			for(int k = 0; k < objs.length; k++) {
				JSONObject entry = (JSONObject)objs[k];
				try {
					String identifier = (String)entry.get("publicID");
					String inchi = (String)((JSONObject)entry.get("inchi")).get("value");
					String inchikey = (String)((JSONObject)entry.get("inchiKey")).get("value");
					String inchikey1 = inchikey.split("-")[0];
					String inchikey2 = inchikey.split("-")[1];
					String name = (String)((JSONObject)entry.get("name")).get("value");
					String smiles = (String)((JSONObject)entry.get("smiles")).get("value");
					String formula = (String)((JSONObject)entry.get("elementalFormula")).get("value");
					//String iupac = (String)entry.get("iupac");
					double accurateMass = (Double)((JSONObject)entry.get("accurateMass")).get("value");
					String tonnage = entry.get("tonnage") == null ? "NA" : (String)((JSONObject)entry.get("tonnage")).get("value");
					Object[] categories = entry.get("categories") == null ? new String[] {"NA"} : (Object[])(((JSONArray)entry.get("categories")).toArray());
					
					String categories_string = (String)((JSONObject)categories[0]).get("value");
					for(int c = 1 ; c < categories.length; c++) 
						categories_string += "," + (String)((JSONObject)categories[c]).get("value");
					
					ICandidate candidate = new TopDownPrecursorCandidate(inchi, identifier);
					candidate.setProperty(VariableNames.INCHI_KEY_NAME, inchikey);
					candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchikey1);
					candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchikey2);
					candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, name);
					candidate.setProperty(VariableNames.SMILES_NAME, smiles);
					candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formula);
					candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, accurateMass);
					candidate.setProperty(VariableNames.FORIDENT_TONNAGE_NAME, tonnage);
					candidate.setProperty(VariableNames.FORIDENT_CATEGORIES_NAME, categories_string);
					candidateList.addElement(candidate);
				}
				catch(Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		return candidateList;
	}

	/**
	 * 
	 * @param molecularFormula
	 * @return
	 * @throws Exception
	 */
	public CandidateList getCandidatesByMolecularFormula(String molecularFormula) throws Exception {
		//https://water.for-ident.org/api/substances?elementalFormula=C16H12Cl2N4O4
		String urlname = "https://water.for-ident.org/api/substances?elementalFormula=" + molecularFormula;
		java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
		if(stream == null) return new CandidateList();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();

		if(jsonObject == null) {
			logger.error("Error: Could not create JSON object for fetching candidates by molecular formula.");
			throw new Exception();
		}
		CandidateList candidateList = new CandidateList();
		Long numberPages = (Long)jsonObject.get("totalPages");
	
		for(int i = 0; i < numberPages; i++) {
			urlname = "https://water.for-ident.org/api/substances?elementalFormula=" + molecularFormula + "&page=" + i;
			stream = HelperFunctions.getInputStreamFromURL(urlname);
			jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
			stream.close();
			
			JSONArray jsonArray = (JSONArray)jsonObject.get("content");
			Object[] objs = jsonArray.toArray();
			for(int k = 0; k < objs.length; k++) {
				JSONObject entry = (JSONObject)objs[k];
				try {
					String identifier = (String)entry.get("publicID");
					String inchi = (String)((JSONObject)entry.get("inchi")).get("value");
					String inchikey = (String)((JSONObject)entry.get("inchiKey")).get("value");
					String inchikey1 = inchikey.split("-")[0];
					String inchikey2 = inchikey.split("-")[1];
					String name = (String)((JSONObject)entry.get("name")).get("value");
					String smiles = (String)((JSONObject)entry.get("smiles")).get("value");
					String formula = (String)((JSONObject)entry.get("elementalFormula")).get("value");
					//String iupac = (String)entry.get("iupac");
					double accurateMass = (Double)((JSONObject)entry.get("accurateMass")).get("value");
					String tonnage = entry.get("tonnage") == null ? "NA" : (String)((JSONObject)entry.get("tonnage")).get("value");
					Object[] categories = entry.get("categories") == null ? new String[] {"NA"} : (Object[])(((JSONArray)entry.get("categories")).toArray());
					
					String categories_string = (String)((JSONObject)categories[0]).get("value");
					for(int c = 1 ; c < categories.length; c++) 
						categories_string += "," + (String)((JSONObject)categories[c]).get("value");
					
					ICandidate candidate = new TopDownPrecursorCandidate(inchi, identifier);
					candidate.setProperty(VariableNames.INCHI_KEY_NAME, inchikey);
					candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchikey1);
					candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchikey2);
					candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, name);
					candidate.setProperty(VariableNames.SMILES_NAME, smiles);
					candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formula);
					candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, accurateMass);
					candidate.setProperty(VariableNames.FORIDENT_TONNAGE_NAME, tonnage);
					candidate.setProperty(VariableNames.FORIDENT_CATEGORIES_NAME, categories_string);
					candidateList.addElement(candidate);
				}
				catch(Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		return candidateList;
	}

	/**
	 * 
	 * @param identifiers
	 * @return
	 * @throws Exception
	 */
	public CandidateList getCandidatesByIdentifier(String[] identifiers) throws Exception {
		//https://water.for-ident.org/api/substances?publicId=SI00000001&publicId=SI00000002
		CandidateList candidateList = new CandidateList();
		String idString = "";
		for(int i = 0; i < identifiers.length; i++) {
			idString += "&stoffidentId=" + identifiers[i];
			if((i % 100 == 0 && i != 0) || (i == identifiers.length - 1)) {
				idString = idString.substring(1, idString.length());
		
				String urlname = "https://water.for-ident.org/api/substances?" + idString;
				System.out.println(urlname);
				java.io.InputStream stream = HelperFunctions.getInputStreamFromURL(urlname);
				if(stream == null) return new CandidateList();
				JSONParser parser = new JSONParser();
				JSONArray jsonArray = (JSONArray)parser.parse(new java.io.InputStreamReader(stream));
				stream.close();
		
				if(jsonArray == null) {
					logger.error("Error: Could not create JSON object for fetching candidate by identifier.");
					throw new Exception();
				}
			
				Object[] objs = jsonArray.toArray();
				for(int k = 0; k < objs.length; k++) {
					JSONObject entry = (JSONObject)objs[k];
					try {
						String identifier = (String)entry.get("publicID");
						String inchi = (String)((JSONObject)entry.get("inchi")).get("value");
						String inchikey = (String)((JSONObject)entry.get("inchiKey")).get("value");
						String inchikey1 = inchikey.split("-")[0];
						String inchikey2 = inchikey.split("-")[1];
						String name = (String)((JSONObject)entry.get("name")).get("value");
						String smiles = (String)((JSONObject)entry.get("smiles")).get("value");
						String formula = (String)((JSONObject)entry.get("elementalFormula")).get("value");
						//String iupac = (String)entry.get("iupac");
						double accurateMass = (Double)((JSONObject)entry.get("accurateMass")).get("value");
						String tonnage = entry.get("tonnage") == null ? "NA" : (String)((JSONObject)entry.get("tonnage")).get("value");
						Object[] categories = entry.get("categories") == null ? new String[] {"NA"} : (Object[])(((JSONArray)entry.get("categories")).toArray());
						
						String categories_string = (String)((JSONObject)categories[0]).get("value");
						for(int c = 1 ; c < categories.length; c++) 
							categories_string += "," + (String)((JSONObject)categories[c]).get("value");
						
						ICandidate candidate = new TopDownPrecursorCandidate(inchi, identifier);
						candidate.setProperty(VariableNames.INCHI_KEY_NAME, inchikey);
						candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchikey1);
						candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchikey2);
						candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, name);
						candidate.setProperty(VariableNames.SMILES_NAME, smiles);
						candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formula);
						candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, accurateMass);
						candidate.setProperty(VariableNames.FORIDENT_TONNAGE_NAME, tonnage);
						candidate.setProperty(VariableNames.FORIDENT_CATEGORIES_NAME, categories_string);
						candidateList.addElement(candidate);
					}
					catch(Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		return candidateList;
	}

	/**
	 * 
	 * @param identifiers
	 * @return
	 * @throws Exception
	 */
	public CandidateList getCandidatesByIdentifier(java.util.Vector<String> identifiers) throws Exception {
		String[] array = new String[identifiers.size()];
		for(int i = 0; i < array.length; i++)
			array[i] = identifiers.get(i);
		return this.getCandidatesByIdentifier(array);
	}
	
	public static void main(String[] args) {
		ForIdentRestWebService sirws = new ForIdentRestWebService();
		try {
			CandidateList ids = sirws.getCandidatesByMass(253.966126, 5.0);
			System.out.println(ids.getNumberElements());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
