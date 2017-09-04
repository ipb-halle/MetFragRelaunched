package de.ipbhalle.metfrag.substructure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredTandemMassPeakListReader;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedPeakFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprints;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;

public class CalculateScoreFromResultFileFP {

	public static MassToFingerprints massToFingerprints;
	public static double ALPHA_VALUE = 0.0001;
	public static double BETA_VALUE = 0.0001;
	
	public static void main(String[] args) throws Exception {
		String paramfile = args[0];
		String resfile = args[1];
		String outputfolder = args[2];
		String alpha = args[3];
		String beta = args[4];
		String probFile = null;
		if(args.length == 6) probFile = args[5];
		
		ALPHA_VALUE = Double.parseDouble(alpha);
		BETA_VALUE = Double.parseDouble(beta);
		
		Settings settings = getSettings(paramfile);
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resfile);
		
		settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, ALPHA_VALUE);
		settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, BETA_VALUE);
		
		FilteredTandemMassPeakListReader reader = new FilteredTandemMassPeakListReader(settings);
		settings.set(VariableNames.PEAK_LIST_NAME, reader.read());
		
		IDatabase db = null;
		if(resfile.endsWith("psv")) db = new LocalPSVDatabase(settings);
		else db = new LocalCSVDatabase(settings);
		
		ArrayList<String> ids = db.getCandidateIdentifiers();
		CandidateList candidates = db.getCandidateByIdentifier(ids);
		
		System.out.println("Read " + candidates.getNumberElements() + " candidates");
		
		AutomatedPeakFingerprintAnnotationScoreInitialiser init = new AutomatedPeakFingerprintAnnotationScoreInitialiser();
		init.initScoreParameters(settings);
		
		postProcessScoreParameters(settings, candidates);
		
		for(int i = 0; i < candidates.getNumberElements(); i++) { 
			singlePostCalculate(settings, candidates.getElement(i));
			candidates.getElement(i).removeProperty("MatchList");
		}
		
		CandidateListWriterCSV writer = new CandidateListWriterCSV();
		writer.write(candidates, (String)settings.get(VariableNames.SAMPLE_NAME), outputfolder);

		if(probFile != null)
			checkProbabilites(settings, probFile, false);
		else
			checkProbabilites(settings, false);
		
	}
	

	public static void postProcessScoreParameters(Settings settings, CandidateList candidates) {
		// to determin F_u
		massToFingerprints = new MassToFingerprints();
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);

		for(int k = 0; k < candidates.getNumberElements(); k++) {
			/*
			 * check whether the single run was successful
			 */
			ICandidate currentCandidate = candidates.getElement(k);
			String fps = (String)currentCandidate.getProperty("FragmentFingerprintOfExplPeaks");
			if(fps.equals("NA")) {
				currentCandidate.setProperty("MatchList", new ArrayList<Match>());
				continue;
			}
			String[] tmp = fps.split(";");
			ArrayList<Match> matchlist = new ArrayList<Match>();
			for(int i = 0; i < tmp.length; i++) {
				String[] tmp1 = tmp[i].split(":");
				Match match = new CalculateScoreFromResultFileFP().new Match(tmp1[1], Double.parseDouble(tmp1[0]));
				matchlist.add(match);
			}
			
			currentCandidate.setProperty("MatchList", matchlist);
			for(int j = 0; j < matchlist.size(); j++) {
				Match match = matchlist.get(j);
				MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(match.getMass());
				if(peakToFingerprintGroupList == null) continue;
				FastBitArray currentFingerprint = new FastBitArray(match.getFingerprint());
				//	if(match.getMatchedPeak().getMass() < 60) System.out.println(match.getMatchedPeak().getMass() + " " + currentFingerprint + " " + fragSmiles);
				// check whether fingerprint was observed for current peak mass in the training data
				if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
					// if not add the fingerprint to background by addFingerprint function
					// addFingerprint checks also whether fingerprint was already added
					massToFingerprints.addFingerprint(match.getMass(), currentFingerprint);
				}
			}
		}

		double alpha = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
		double beta = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
		
		double f_seen = (double)settings.get(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
		double f_unseen = massToFingerprints.getOverallSize();																// f_u
		double sumFingerprintFrequencies = (double)settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
		
		// set value for denominator of P(f,m)
		double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;

		settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
		
		double alphaProbability = alpha / denominatorValue; // P(f,m) F_u
		double betaProbability = beta / denominatorValue;	// p(f,m) not annotated
		
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			MassToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
			
			// sum_f P(f,m)
			double sum_f = 0.0;
			double sumFsProbabilities = 0.0;
			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// first calculate P(f,m)
				groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getProbability() + alpha) / denominatorValue);
				// sum_f P(f,m) -> for F_s
				sumFsProbabilities += groupList.getElement(ii).getJointProbability();
			}
			
			double sumFuProbabilities = alphaProbability * massToFingerprints.getSize(groupList.getPeakmz());
			
			sum_f += sumFsProbabilities;
			sum_f += sumFuProbabilities;
			sum_f += betaProbability;
			
			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// second calculate P(f|m)
				groupList.getElement(ii).setConditionalProbability_sp(groupList.getElement(ii).getJointProbability() / sum_f);
			}
			
			groupList.setAlphaProb(alphaProbability / sum_f);
			groupList.setBetaProb(betaProbability / sum_f);
			groupList.setProbabilityToConditionalProbability_sp();
			groupList.calculateSumProbabilites();
			
		}
		
		return;
	}
	
	public static void singlePostCalculate(Settings settings, ICandidate candidate) {
		//this.value = 0.0;
		double value = 1.0;
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		
		int matches = 0;
		ArrayList<?> matchlist = (ArrayList<?>)candidate.getProperty("MatchList");
		ArrayList<Double> matchMasses = new ArrayList<Double>();
		ArrayList<Double> matchProb = new ArrayList<Double>();
		ArrayList<Integer> matchType = new ArrayList<Integer>(); // found - 1; alpha - 2; beta - 3
		// get foreground fingerprint observations (m_f_observed)
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			// get f_m_observed
			MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
			Double currentMass = peakToFingerprintGroupList.getPeakmz();
			Match currentMatch = getMatchByMass(matchlist, currentMass);

			//(fingerprintToMasses.getSize(currentFingerprint));
			if(currentMatch == null) {
				matchProb.add(peakToFingerprintGroupList.getBetaProb());
				matchType.add(3);
				matchMasses.add(currentMass);
				value *= peakToFingerprintGroupList.getBetaProb();
			} else {
				FastBitArray currentFingerprint = new FastBitArray(currentMatch.getFingerprint());
				// ToDo: at this stage try to check all fragments not only the best one
				matches++;
				// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
				double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
				// |F|
				if(matching_prob != 0.0) {
					value *= matching_prob;
					matchProb.add(matching_prob);
					matchMasses.add(currentMass);
					matchType.add(1);
				}
				else {
					value *= peakToFingerprintGroupList.getAlphaProb();
					matchProb.add(peakToFingerprintGroupList.getAlphaProb());
					matchMasses.add(currentMass);
					matchType.add(2);
				}
			}
		}
		if(peakToFingerprintGroupListCollection.getNumberElements() == 0) value = 0.0;
		candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore3_Matches", matches);
		candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore3", value);
		candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore3_Probtypes", getProbTypeString(matchProb, matchType, matchMasses));
 	}

	public static String getProbTypeString(ArrayList<Double> matchProb, ArrayList<Integer> matchType, ArrayList<Double> matchMasses) {
		String string = "NA";
		if(matchProb.size() >= 1) {
			string = matchType.get(0) + ":" + matchProb.get(0) + ":" + matchMasses.get(0);
		}
		for(int i = 1; i < matchProb.size(); i++) {
			string += ";" + matchType.get(i) + ":" + matchProb.get(i) + ":" + matchMasses.get(i);
		}
		return string;
	}
	
	/**
	 * check whether probabilities sum to 1
	 * 
	 * @param settings
	 * @throws IOException 
	 */
	public static void checkProbabilites(Settings settings, String probFile, boolean echo) throws IOException {
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
	
		BufferedWriter bwriter = null;
		if(!probFile.equals("")) 
			bwriter = new BufferedWriter(new FileWriter(new File(probFile)));
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			double sum = 0.0;
			
			// get f_m_observed
			MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
			Double currentMass = peakToFingerprintGroupList.getPeakmz();
			LinkedList<FastBitArray> fps = massToFingerprints.getFingerprints(currentMass);
			
			for(int k = 0; k < peakToFingerprintGroupList.getNumberElements(); k++) {
				if(bwriter != null) bwriter.write(peakToFingerprintGroupList.getElement(k).getProbability() + "\n");
				sum += peakToFingerprintGroupList.getElement(k).getProbability();
			}

			for(int k = 0; k < fps.size(); k++) {
				if(bwriter != null) bwriter.write(peakToFingerprintGroupList.getAlphaProb() + "\n");
				sum += peakToFingerprintGroupList.getAlphaProb();
			}
			
			if(bwriter != null) bwriter.write(peakToFingerprintGroupList.getBetaProb() + "\n");
			sum += peakToFingerprintGroupList.getBetaProb();
			
			if(echo) System.out.println(currentMass + " " + MathTools.round(sum, 15));
		}
		
		if(bwriter != null) bwriter.close();
	}
	
	public static void checkProbabilites(Settings settings, boolean echo) throws IOException {
		checkProbabilites(settings, "", echo);
	}
	
	
	public static Settings getSettings(String parameterfile) {
		
		File parameterFile = new File(parameterfile);
		MetFragGlobalSettings settings = null;
		try {
			settings = MetFragGlobalSettings.readSettings(parameterFile, null);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return settings;
	}
	
	public static Match getMatchByMass(ArrayList<?> matches, Double peakMass) {
		for(int i = 0; i < matches.size(); i++) {
			Match match = (Match)matches.get(i);
			if(match.getMass().equals(peakMass)) return match;
		}
		return null;
	}
	
	class Match {
		private String fp;
		private Double mass;
		
		Match(String fp, Double mass) {
			this.fp = fp;
			this.mass = mass;
		}
		
		public Double getMass() {
			return this.mass;
		}
		
		public String getFingerprint() {
			return this.fp;
		}
	}
}
