package de.ipbhalle.metfraglib.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class OnlineChemSpiderRestDatabase extends AbstractDatabase {

	private int errors;
	private String apikey;
	private final int MAX_TRIALS = 10;
	
	public OnlineChemSpiderRestDatabase(Settings settings) {
		super(settings);
		this.errors = 0;
		this.apikey = (String)settings.get(VariableNames.CHEMSPIDER_REST_TOKEN_NAME);
		logger.info("Fetching candidates from ChemSpider (REST API)");
	}
	
	@Override
	public ArrayList<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation)
			throws Exception {

		this.errors = 0;
		double mzabs = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		
		HttpPost httpPost = new HttpPost("https://api.rsc.org/compounds/v1/filter/mass/");
		
		httpPost.addHeader("apikey", this.apikey);
		
		String json = "{\"mass\":" + monoisotopicMass + ", \"range\": " + mzabs + "}";

		StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

	    httpPost.setEntity(entity);

		ArrayList<String> identifiers = this.getIdentifieresByQueryID(this.getResults(httpPost));
	    
		return identifiers;
	}

	@Override
	public ArrayList<String> getCandidateIdentifiers(String molecularFormula) throws Exception {

		this.errors = 0;
		HttpPost httpPost = new HttpPost("https://api.rsc.org/compounds/v1/filter/formula/");
		
		httpPost.addHeader("apikey", this.apikey);
		
		String json = "{\"formula\": \"" + molecularFormula + "\"}";

		StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
	    
	    httpPost.setEntity(entity);
	    
		ArrayList<String> identifiers = this.getIdentifieresByQueryID(this.getResults(httpPost));
		
		return identifiers;
	}

	@Override
	public ArrayList<String> getCandidateIdentifiers(ArrayList<String> identifiers)
			throws Exception {
		this.errors = 0;
		ArrayList<String> uniqueCsidArray = new ArrayList<String>();
        for(int i = 0; i < identifiers.size(); i++) {
        	if(!uniqueCsidArray.contains(identifiers.get(i)))
            	uniqueCsidArray.add(identifiers.get(i));
        }
        return uniqueCsidArray;
	}

	@Override
	public ICandidate getCandidateByIdentifier(String identifier)
        throws Exception {
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(identifier);
		CandidateList candidates = this.getCandidateByIdentifier(ids);
		if(candidates == null) return null;
		return candidates.getElement(0);
	}

	@Override
	public CandidateList getCandidateByIdentifier(ArrayList<String> identifiers) throws Exception {

		CandidateList candidateList = new CandidateList();
		if(identifiers.size() == 0) return candidateList;
		
		this.addToCandidateList(identifiers, candidateList);
		
		if(this.errors != 0) this.logger.warn("Could not generate candidates in " + this.errors + " case(s).");
		return candidateList;
	}

	/**
	 * 
	 * @param identifiers
	 * @param candidateList
	 * @throws IOException
	 * @throws ParseException
	 */
	protected void addToCandidateList(List<String> identifiers, CandidateList candidateList) throws IOException, ParseException {
		if(identifiers.size() == 0) return;
		String recordIds = identifiers.get(0);
		for(int i = 1; i < Math.min(identifiers.size(), 100); i++)
			recordIds += "," + identifiers.get(i);
		
		HttpPost httpPost = new HttpPost("https://api.rsc.org/compounds/v1/records/batch");
		
		String json = "{\"recordIds\": [" + recordIds + "], \"fields\": [\"SMILES\",\"MonoisotopicMass\",\"Formula\",\"CommonName\",\"ReferenceCount\",\"DataSourceCount\",\"PubMedCount\",\"RSCCount\"] }";

		StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
	
		httpPost.setEntity(entity);
		    
		JSONObject jsonObject = this.getResults(httpPost);

		if(!jsonObject.containsKey("records")) return;
		
		JSONArray records = (JSONArray)jsonObject.get("records");

		for(Object obj : records) {
			try {
				ICandidate candidate = this.generateCandidate((JSONObject)obj);
				if(candidate != null) candidateList.addElement(candidate);
				else this.errors++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
		}
		
		// continue recursion
		this.addToCandidateList(identifiers.subList(Math.min(identifiers.size(), 100), identifiers.size()), candidateList);
	}
	
	protected ICandidate generateCandidate(JSONObject jsonObject) {
		try {
			String smiles = ((String)jsonObject.get("smiles")).replaceAll("\\n", "");
			Double mass = (Double)jsonObject.get("monoisotopicMass");
			Long dataSourceCount = (Long)jsonObject.get("dataSourceCount");
			String commonName = (String)jsonObject.get("commonName");
			Long referenceCount = (Long)jsonObject.get("referenceCount");
			String formula = (String)jsonObject.get("formula");
			Long pubMedCount = (Long)jsonObject.get("pubMedCount");
			Long rscCount = (Long)jsonObject.get("rscCount");
			String id = String.valueOf((long)jsonObject.get("id"));
			
			String[] inchi = null;
			try {
				inchi = MoleculeFunctions.getInChIInfoFromSmiles(smiles);
			} catch (Exception e) {
				return null;
			}
		
			ICandidate candidate = new TopDownPrecursorCandidate(inchi[0], id);
			candidate.setProperty(VariableNames.INCHI_KEY_NAME, inchi[1]);
			String[] tmp = inchi[1].split("-");
			candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, tmp[0]);
			candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, tmp[1]);
			candidate.setProperty(VariableNames.INCHI_KEY_3_NAME, tmp[2]);
			
			candidate.setProperty(VariableNames.CHEMSPIDER_DATA_SOURCE_COUNT, (double)Math.toIntExact(dataSourceCount));
			candidate.setProperty(VariableNames.CHEMSPIDER_NUMBER_PUBMED_REFERENCES_NAME, (double)Math.toIntExact(pubMedCount));
			candidate.setProperty(VariableNames.CHEMSPIDER_REFERENCE_COUNT, (double)Math.toIntExact(referenceCount));
			candidate.setProperty(VariableNames.CHEMSPIDER_RSC_COUNT, (double)Math.toIntExact(rscCount));
			candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, mass);
			candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, commonName);
			candidate.setProperty(VariableNames.SMILES_NAME, smiles);
			candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, this.processFormula(formula));
			
			return candidate;
		} catch(Exception e) {
			return null;
		}
	}

	private String processFormula(String preFormula) {
		preFormula = preFormula.replaceAll("_\\{([0-9]+)\\}", "$1");
		preFormula = preFormula.replaceAll("\\^\\{([0-9]+)\\}([A-Z][a-z]{0,3})", "\\[$1$2\\]");
		return preFormula;
	}
	
	protected ArrayList<String> getIdentifieresByQueryID(JSONObject jsonObject) throws Exception {
		if(!jsonObject.containsKey("queryId")) return new ArrayList<String>();
		String queryid = (String)jsonObject.get("queryId");
		// first check if status is ok (up to 10 times)
		int trials = 0;
		while(!this.checkStatus(queryid)) {
			trials++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				if(trials >= MAX_TRIALS) throw new Exception();
				continue;
			}
			if(trials >= MAX_TRIALS) throw new Exception();
		}
		
		HttpGet httpGet = new HttpGet("https://api.rsc.org/compounds/v1/filter/" + queryid + "/results");

		JSONObject jsonObject1 = this.getResults(httpGet);
		
		if(!jsonObject1.containsKey("results")) return new ArrayList<String>();
		
		JSONArray array = (JSONArray)jsonObject1.get("results");
		
		ArrayList<String> identifiers = new ArrayList<String>();
		for(Object id : array)
			identifiers.add(String.valueOf((long)id));
		
		return identifiers;
	}
	
	protected boolean checkStatus(String queryID) throws IOException, ParseException {
		HttpGet httpGet = new HttpGet("https://api.rsc.org/compounds/v1/filter/" + queryID + "/status");

		JSONObject jsonObject = this.getResults(httpGet);
		
		if(!jsonObject.containsKey("status")) return false;
		
		if(((String)jsonObject.get("status")).equals("Complete")) return true;
		
		return false;
	}
	
	protected JSONObject getResults(ClassicHttpRequest request) throws IOException, ParseException {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		request.addHeader("apikey", this.apikey);
		CloseableHttpResponse response = httpclient.execute(request);
		
		java.io.InputStream stream = response.getEntity().getContent();
		
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject)parser.parse(new java.io.InputStreamReader(stream));
		stream.close();
		
		return jsonObject;
	}
	
	@Override
	public void nullify() {
		// TODO Auto-generated method stub
		
	}
	
}
