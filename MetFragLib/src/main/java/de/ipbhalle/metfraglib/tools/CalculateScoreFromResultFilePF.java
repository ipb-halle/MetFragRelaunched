package de.ipbhalle.metfraglib.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredTandemMassPeakListReader;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedFingerprintSubstructureAnnotationScoreInitialiser4;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintToMasses;
import de.ipbhalle.metfraglib.substructure.FingerprintToMassesSimple;
import de.ipbhalle.metfraglib.substructure.MassToFingerprints;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;

public class CalculateScoreFromResultFilePF {

	public static MassToFingerprints massToFingerprints;
	public final static double ALPHA_VALUE = 0.0001;
	public final static double BETA_VALUE = 0.0001;
	
	public static void main(String[] args) throws Exception {
		String paramfile = args[0];
		String resfile = args[1];
		String outputfolder = args[2];
		
		Settings settings = getSettings(paramfile);
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resfile);
		
		settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, ALPHA_VALUE);
		settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, BETA_VALUE);
		
		FilteredTandemMassPeakListReader reader = new FilteredTandemMassPeakListReader(settings);
		settings.set(VariableNames.PEAK_LIST_NAME, reader.read());
		
		LocalPSVDatabase db = new LocalPSVDatabase(settings);
		Vector<String> ids = db.getCandidateIdentifiers();
		CandidateList candidates = db.getCandidateByIdentifier(ids);
		
		System.out.println("Read " + candidates.getNumberElements() + " candidates");
		
		AutomatedFingerprintSubstructureAnnotationScoreInitialiser4 init = new AutomatedFingerprintSubstructureAnnotationScoreInitialiser4();
		init.initScoreParameters(settings);
		System.out.println("finished init");
		postProcessScoreParameters(settings, candidates);
		System.out.println("finished postprocessing");
		for(int i = 0; i < candidates.getNumberElements(); i++) { 
			singlePostCalculate(settings, candidates.getElement(i));
			candidates.getElement(i).removeProperty("MatchList");
		}
		System.out.println("finished score calc");
		CandidateListWriterCSV writer = new CandidateListWriterCSV();
		writer.write(candidates, (String)settings.get(VariableNames.SAMPLE_NAME), outputfolder);
		
		checkProbabilites(settings);
	}

	/**
	 * 
	 * @param settings
	 * @param candidates
	 */
	public static void postProcessScoreParameters(Settings settings, CandidateList candidates) {
		// to determine F_u
		MassToFingerprints massToFingerprints = new MassToFingerprints();
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
	//	FingerprintToMasses fingerprintToMasses = (FingerprintToMasses)settings.get(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME);

		double alpha = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
		Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		
		FingerprintToMassesSimple positiveFingerprints = new FingerprintToMassesSimple();
		FingerprintToMassesSimple negativeFingerprints = new FingerprintToMassesSimple();
		
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
			for(int j = 0; j < groupList.getNumberElements(); j++) {
				positiveFingerprints.addFingerprint(groupList.getElement(j).getFingerprint());
			}
		}
		
		for(int k = 0; k < candidates.getNumberElements(); k++) {
			/*
			 * check whether the single run was successful
			 */
			ICandidate currentCandidate = candidates.getElement(k);
			String fps = (String)currentCandidate.getProperty("FragmentFingerprintOfExplPeaks");
			if(fps.equals("NA")) {
				currentCandidate.setProperty("MatchList", new Vector<Match>());
				continue;
			}
			String[] tmp = fps.split(";");
			Vector<Match> matchlist = new Vector<Match>();
			for(int i = 0; i < tmp.length; i++) {
				String[] tmp1 = tmp[i].split(":");
				Match match = new CalculateScoreFromResultFilePF().new Match(tmp1[1], Double.parseDouble(tmp1[0]));
				matchlist.add(match);
			}
			
			currentCandidate.setProperty("MatchList", matchlist);
			for(int j = 0; j < matchlist.size(); j++) {
				Match match = matchlist.get(j);
				PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(match.getMass());
				if(peakToFingerprintGroupList == null) continue;
				FastBitArray currentFingerprint = new FastBitArray(match.getFingerprint());
				// if(match.getMatchedPeak().getMass() < 60) System.out.println(match.getMatchedPeak().getMass() + " " + currentFingerprint + " " + fragSmiles);
				// check whether fingerprint was observed for current peak mass in the training data
				if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
					// if not add the fingerprint to background by addFingerprint function
					// addFingerprint checks also whether fingerprint was already added
					massToFingerprints.addFingerprint(match.getMass(), currentFingerprint);
					//fingerprintToMasses.addMass(currentFingerprint, match.getMass(), 0.0, mzppm, mzabs);
					negativeFingerprints.addMass(currentFingerprint, match.getMass());
				}
			}
		}
		
		String filename_conv = (String)settings.get(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_CONV_NAME);
		FingerprintToMasses fingerprintToMasses = new FingerprintToMasses();
		BufferedReader breader = null;
		try {
			breader = new BufferedReader(new FileReader(new File(filename_conv)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		try {
			boolean[] negativeAdded = new boolean[negativeFingerprints.getFingerprintSize()];
			while((line = breader.readLine()) != null) {
				line = line.trim();
				String[] tmp = line.split("\\s+");
				FastBitArray currentFingerprint = new FastBitArray(tmp[0]);
				boolean inPos = positiveFingerprints.containsFingerprint(currentFingerprint);
				if(inPos) {
					for(int k = 1; k < tmp.length; k += 2) {
						fingerprintToMasses.addMass(currentFingerprint, Double.parseDouble(tmp[k+1]), Double.parseDouble(tmp[k]), mzppm, mzabs);
					}
				} 
				int inNeg = negativeFingerprints.getIndexOfFingerprint(currentFingerprint);
				if(inNeg != -1) {
					negativeAdded[inNeg] = true;
					Vector<Double> masses = negativeFingerprints.getMasses(inNeg);
					for(int i = 0; i < masses.size(); i++) {
						fingerprintToMasses.addMass(currentFingerprint, masses.get(i), 0.0, mzppm, mzabs);
					}
				}
			}
			breader.close();
			for(int i = 0; i < negativeAdded.length; i++) {
				if(!negativeAdded[i]) {
					FastBitArray currentFingerprint = negativeFingerprints.getFingerprint(i);
					Vector<Double> masses = negativeFingerprints.getMasses(i);
					for(int ii = 0; ii < masses.size(); ii++) {
						fingerprintToMasses.addMass(currentFingerprint, masses.get(ii), 0.0, mzppm, mzabs);
					}
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double beta = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
		
		double f_seen = (double)settings.get(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
		double f_unseen = massToFingerprints.getOverallSize();																// f_u
		double sumFingerprintFrequencies = (double)settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
		
		double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;
		settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
		
		double alphaProbability = alpha / denominatorValue; 	// P(f,m) F_u
		double betaProbability = beta / denominatorValue;		// p(f,m) not annotated
		
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);

			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// first calculate P(f,m)
				groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getNumberObserved() + alpha) / denominatorValue);
				// sum_f P(f,m) -> for F_s
			}

		}
		// normalize probabilities in fingerprintToProbabilities
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			fingerprintToMasses.addToObservations(i, alpha);
		}
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			fingerprintToMasses.normalizeNumObservations(i, denominatorValue);
		}
		fingerprintToMasses.calculateSumNumObservations();
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			fingerprintToMasses.addAlphaProbability(alphaProbability / fingerprintToMasses.getSumNumObservations(i));
		}
		fingerprintToMasses.addBetaProbability(betaProbability / (double)(int)settings.get(VariableNames.NUMBER_BACKGROUND_MASSES_NAME));
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// second calculate P(f|m)
				double sum_m = fingerprintToMasses.getSumNumObservations(groupList.getElement(ii).getFingerprint()); 
				if(sum_m == 0) {
				//	System.err.println("Check " + groupList.getPeakmz() + " " + groupList.getElement(ii).getFingerprint().toStringIDs() + " " + groupList.getElement(ii).getFingerprint().toString());
					System.out.println(fingerprintToMasses.getIndexOfFingerprint(groupList.getElement(ii).getFingerprint()));
				}
				groupList.getElement(ii).setConditionalProbability_ps(groupList.getElement(ii).getJointProbability() / sum_m);
			}
			groupList.setProbabilityToConditionalProbability_ps();
		}
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			fingerprintToMasses.normalizeNumObservations(i, fingerprintToMasses.getSumNumObservations(i));
		}
		fingerprintToMasses.calculateSumNumObservations();
		
		settings.set(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME, fingerprintToMasses);
		
		return;
	}
	
	/**
	 * calculate single score for candidate
	 * 
	 * @param settings
	 * @param candidate
	 */
	public static void singlePostCalculate(Settings settings, ICandidate candidate) {
		//this.value = 0.0;
		double value = 1.0;
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		FingerprintToMasses fingerprintToMasses = (FingerprintToMasses)settings.get(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME);
		
		Vector<?> matchlist = (Vector<?>)candidate.getProperty("MatchList");
	
		int matches = 0;
		// get foreground fingerprint observations (m_f_observed)
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			// get f_m_observed
			PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
			Double currentMass = peakToFingerprintGroupList.getPeakmz();
			Match currentMatch = getMatchByMass(matchlist, currentMass);
			
			//(fingerprintToMasses.getSize(currentFingerprint));
			if(currentMatch == null) {
				value *= fingerprintToMasses.getBetaProbabilty(0);
			} else {
				FastBitArray currentFingerprint = new FastBitArray(currentMatch.getFingerprint());
				// ToDo: at this stage try to check all fragments not only the best one
				matches++;
				// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
				double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
				// |F|
				if(matching_prob != 0.0) value *= matching_prob;
				else {
					value *= fingerprintToMasses.getAlphaProbabilty(currentFingerprint);
				}
			}
		}
		
		if(peakToFingerprintGroupListCollection.getNumberElements() == 0) value = 0.0;
		candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore4_Matches", matches);
		candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore4", value);
 	}
	
	/**
	 * check whether probabilities sum to 1
	 * 
	 * @param settings
	 * @throws IOException 
	 */
	public static void checkProbabilites(Settings settings) throws IOException {
		
		FingerprintToMasses fingerprintToMasses = (FingerprintToMasses)settings.get(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME);
		
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			double sum = 0.0;
			
			// get f_m_observed
			
			sum += fingerprintToMasses.getSumNumObservations(i);
			
		//	System.out.println(fingerprintToMasses.getFingerprint(i).toStringIDs() + " " + MathTools.round(sum, 15));
		}
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
	
	public static Match getMatchByMass(Vector<?> matches, Double peakMass) {
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
