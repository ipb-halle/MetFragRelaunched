package de.ipbhalle.metfraglib.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.ipbhalle.metfraglib.candidate.PrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedSimilarityTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;

public class MoNARestWebService implements Runnable {

	protected String processName = "";
	protected Settings settings;
	protected boolean isProcessingSuccessful;
	protected boolean isProcessingFinished;
	protected Object result;
	protected Logger logger;
	
	public MoNARestWebService(Settings settings)  {
		this.settings = settings;
		this.logger = Logger.getLogger(MoNARestWebService.class);
		this.isProcessingSuccessful = false;
		this.isProcessingFinished = false;
	}
	
	public Object getResult() {
		return this.result;
	}
	
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	public boolean isProcessingSuccessful() {
		return this.isProcessingSuccessful && this.result != null;
	}
	
	public boolean isProcessingFinished() {
		return this.isProcessingFinished;
	}
	
	@Override
	public void run() {
		this.isProcessingFinished = false;
		try {
			if(this.processName.equals("performSpectrumSimilaritySearch")) { 
				this.result = this.performSpectrumSimilaritySearch();
			}
		} catch(Exception e) {
			this.isProcessingSuccessful = false;
			this.isProcessingFinished = true;
			this.logger.error("Could not perform spectrum similarity search.");
			return;
		}
		this.isProcessingSuccessful = true;
		this.isProcessingFinished = true;
	}
	
	public CandidateList performSpectrumSimilaritySearch() throws Exception {
		DefaultPeakList peaklist = (DefaultPeakList)this.settings.get(VariableNames.PEAK_LIST_NAME);
		String queryString = "{\"compound\":{},\"metadata\":[],\"tags\":[],\"match\":{\"spectra\":\"";
		for(int i = 0; i < peaklist.getNumberElements(); i++) {
			TandemMassPeak peak = (TandemMassPeak)peaklist.getElement(i);
			queryString += peak.getMass() + ":" + peak.getAbsoluteIntensity();
			if(i != (peaklist.getNumberElements() - 1)) queryString += " ";
		}
		queryString += "\"}}";
		String result = performQuery(queryString, "http://mona.fiehnlab.ucdavis.edu/rest/spectra/search");
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray)parser.parse(new java.io.InputStreamReader(IOUtils.toInputStream(result)));
		CandidateList spectralCandidates = new CandidateList();
		this.logger.info("Got " + jsonArray.size() + " results from MoNA.");
		java.util.Vector<String> inchikeys = new java.util.Vector<String>();
		java.util.Vector<Double> scores = new java.util.Vector<Double>();
		for(int i = 0; i < jsonArray.size(); i++) 
		{
			try {
				JSONObject obj = (JSONObject)jsonArray.get(i);
				JSONObject compoundObject = (JSONObject)obj.get("chemicalCompound");
				JSONObject scoreObject = (JSONObject)obj.get("score");
				Double currentScore = (Double)scoreObject.get("score");
				String currentInChIKey1 = (String)compoundObject.get("inchiKey");
				//unique results by inchikey
				int index = inchikeys.indexOf(currentInChIKey1);
				if(index != -1) {
					double scoreAlreadyFound = scores.get(index);
					if(scoreAlreadyFound > currentScore) continue;
					else {
						spectralCandidates.removeElement(index);
						scores.remove(index);
						inchikeys.remove(index);
					}
				}
				ICandidate candidate = new PrecursorCandidate((String)compoundObject.get("inchi"), String.valueOf((Long)compoundObject.get("id")));
				candidate.setProperty(VariableNames.INCHI_KEY_NAME, (String)compoundObject.get("inchiKey"));
				candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, ((String)compoundObject.get("inchiKey")).split("-")[0]);
				candidate.setProperty("scaledScore", scoreObject.get("scaledScore"));
				candidate.setProperty("score", currentScore);
				candidate.setProperty("relativeScore", scoreObject.get("relativeScore"));
				inchikeys.add(currentInChIKey1);
				scores.add(currentScore);
				spectralCandidates.addElement(candidate);
			}
			catch(Exception e) {
				System.err.println("performSpectrumSimilaritySearch MoNARestWebService error");
				spectralCandidates = new CandidateList();
				break;
			}
		}
		this.logger.info("After filtering " + spectralCandidates.getNumberElements() + " results left.");
		return spectralCandidates;
	}
	
	public void nullify() {
		if(this.result != null) this.result = null;
		this.isProcessingSuccessful = false;
		this.isProcessingFinished = false;
	}
	
	public DefaultPeakList[] retrievePeakListByInChIKey(String inchikey, double monoMass) throws Exception {
		/*
		 * TODO
		 * like query does not work for InChIKeys
		 */
		String query = "{\"compound\":{\"inchiKey\":{\"eq\":\"" + inchikey + "-UHFFFAOYSA-N\"}},\"metadata\":[],\"tags\":[]}";
		if(inchikey.length() > 14) query = "{\"compound\":{\"inchiKey\":{\"eq\":\"" + inchikey + "\"}},\"metadata\":[],\"tags\":[]}";
		String result = "";
		try {
			result = this.performQuery(query, "http://mona.fiehnlab.ucdavis.edu/rest/spectra/search");
		} catch (IOException e) {
			this.logger.error("Could not peform MoNA spectrum query. Server not reachable? MoNA results may not be reliable.");
			e.printStackTrace();
			throw new Exception();
		}
		java.util.Vector<DefaultPeakList> peakLists = new java.util.Vector<DefaultPeakList>();
		JSONParser parser = new JSONParser();
		try {
			JSONArray jsonArray = (JSONArray)parser.parse(new java.io.InputStreamReader(IOUtils.toInputStream(result)));
			for(int i = 0; i < jsonArray.size(); i++) {
				JSONObject obj = (JSONObject)jsonArray.get(i);
				String spectrum = (String)obj.get("spectrum");
				if(spectrum == null) continue;
				spectrum = spectrum.trim();
				if(spectrum.length() == 0) continue;
				spectrum = spectrum.replaceAll("\\s", "\n").replaceAll(":", " ");
				DefaultPeakList peaklist = StringTandemMassPeakListReader.readSingle(spectrum, monoMass);
				peakLists.add(peaklist);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new DefaultPeakList[0];
		} catch (ParseException e) {
			e.printStackTrace();
			return new DefaultPeakList[0];
		}
		DefaultPeakList[] peakListsArray = new DefaultPeakList[peakLists.size()];
		for(int i = 0; i < peakListsArray.length; i++)
			peakListsArray[i] = peakLists.get(i);
		return peakListsArray;
	}
	
	protected String performQuery(String query, String urlname) throws IOException {
		URL url = new URL(urlname);
		Proxy proxy = null;
		if(this.settings.containsKey(VariableNames.MONA_PROXY_SERVER) && this.settings.containsKey(VariableNames.MONA_PROXY_PORT)
				&& this.settings.get(VariableNames.MONA_PROXY_SERVER) != null && this.settings.get(VariableNames.MONA_PROXY_PORT) != null) {
			proxy = new Proxy(Proxy.Type.HTTP, 
						new InetSocketAddress((String)this.settings.get(VariableNames.MONA_PROXY_SERVER), (Integer)this.settings.get(VariableNames.MONA_PROXY_PORT)));
		}
		HttpURLConnection conn = null;
		if(proxy == null) conn = (HttpURLConnection) url.openConnection();
		else conn = (HttpURLConnection) url.openConnection(proxy);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);
		conn.setRequestProperty("Content-Type", "application/json");
		
		// Create the form content
		OutputStream out = conn.getOutputStream();
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		writer.write(query);

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

		return sb.toString();
	}

	

	public SortedSimilarityTandemMassPeakList[] retrievePeakListByInChIKeyDetailed(String inchikey, double monoMass) throws Exception {
		/*
		 * TODO
		 * like query does not work for InChIKeys
		 */
		String query = "{\"compound\":{\"inchiKey\":{\"eq\":\"" + inchikey + "-UHFFFAOYSA-N\"}},\"metadata\":[],\"tags\":[]}";
		if(inchikey.length() > 14) query = "{\"compound\":{\"inchiKey\":{\"eq\":\"" + inchikey + "\"}},\"metadata\":[],\"tags\":[]}";
		String result = "";
		try {
			result = this.performQuery(query, "http://mona.fiehnlab.ucdavis.edu/rest/spectra/search");
		} catch (IOException e) {
			this.logger.error("Could not peform MoNA spectrum query. Server not reachable? MoNA results may not be reliable.");
			e.printStackTrace();
			throw new Exception();
		}
		java.util.Vector<SortedSimilarityTandemMassPeakList> peakLists = new java.util.Vector<SortedSimilarityTandemMassPeakList>();
		JSONParser parser = new JSONParser();
		try {
			JSONArray jsonArray = (JSONArray)parser.parse(new java.io.InputStreamReader(IOUtils.toInputStream(result)));
			
			for(int i = 0; i < jsonArray.size(); i++) {
				JSONObject obj = (JSONObject)jsonArray.get(i);
				String spectrum = (String)obj.get("spectrum");
				if(spectrum == null) continue;
				spectrum = spectrum.trim();
				if(spectrum.length() == 0) continue;
				spectrum = spectrum.replaceAll("\\s", "\n").replaceAll(":", " ");
				DefaultPeakList peaklist = StringTandemMassPeakListReader.readSingle(spectrum, monoMass);
				SortedSimilarityTandemMassPeakList peakListDetailed = new SortedSimilarityTandemMassPeakList(monoMass);
				for(int l = 0; l < peaklist.getNumberElements(); l++) {
					peakListDetailed.addElement(peaklist.getElement(l));
				}
				String ionmode = "NA";
				String mslevel = "NA";
				String instrumenttype = "NA";
				String precursortype = "NA";
				String mona_inchikey = (String)((JSONObject)obj.get("biologicalCompound")).get("inchiKey");
				String mona_inchi = (String)((JSONObject)obj.get("biologicalCompound")).get("inchi");
				String mona_mass = "NA";
				String precursor_mz = "NA";
				
				JSONArray metaData = (JSONArray)obj.get("metaData");
				for(int k = 0; k < metaData.size(); k++) {
					String name = (String)((JSONObject)metaData.get(k)).get("name");
					String value = (String)((JSONObject)metaData.get(k)).get("value");
					if(name.equals("ion mode")) ionmode = value;
					if(name.equals("ms level")) mslevel = value;
					if(name.equals("instrument type")) instrumenttype = value;
					if(name.equals("precursor type")) precursortype = value;
					if(name.equals("exact mass")) mona_mass = value;
					if(name.equals("precursor m/z")) precursor_mz = value;
				}
				peakListDetailed.setSampleName(
					String.valueOf((Long)obj.get("id")) + "|" + 
					(String)obj.get("libraryIdentifier") + "|" +
					ionmode + "|" +
					mslevel + "|" +
					instrumenttype + "|" +
					precursortype + "|" +
					mona_inchikey + "|" +
					mona_inchi + "|" +
					mona_mass + "|" +
					precursor_mz
				);
				peakLists.add(peakListDetailed);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new SortedSimilarityTandemMassPeakList[0];
		} catch (ParseException e) {
			e.printStackTrace();
			return new SortedSimilarityTandemMassPeakList[0];
		}
		SortedSimilarityTandemMassPeakList[] peakListsArray = new SortedSimilarityTandemMassPeakList[peakLists.size()];
		for(int i = 0; i < peakListsArray.length; i++)
			peakListsArray[i] = peakLists.get(i);
		return peakListsArray;
	}
	
	public static void main(String[] args) {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.MONA_PROXY_SERVER, "www-cache.ipb-halle.de");
		settings.set(VariableNames.MONA_PROXY_PORT, 3128);
		MoNARestWebService moNARestWebService = new MoNARestWebService(settings);
		
		try {
			moNARestWebService.retrievePeakListByInChIKeyDetailed("MXWJVTOOROXGIU-UHFFFAOYSA-N", 253.9661);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
