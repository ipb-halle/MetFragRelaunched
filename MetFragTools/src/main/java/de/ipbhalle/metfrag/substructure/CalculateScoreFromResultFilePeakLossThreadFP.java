package de.ipbhalle.metfrag.substructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredTandemMassPeakListReader;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedLossFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedPeakFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintsHashMap;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;

public class CalculateScoreFromResultFilePeakLossThreadFP {

	public static double ALPHA_VALUE_PEAK = 0.0001;
	public static double BETA_VALUE_PEAK = 0.0001;
	public static double ALPHA_VALUE_LOSS = 0.0001;
	public static double BETA_VALUE_LOSS = 0.0001;
	
	public static int numberFinished = 0;
	public static java.util.Hashtable<String, String> argsHash;

	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("parampath") && !tmp[0].equals("resultpath") 
					&& !tmp[0].equals("threads") && !tmp[0].equals("output")
					&& !tmp[0].equals("alphaPeak") && !tmp[0].equals("betaPeak") 
					&& !tmp[0].equals("alphaLoss") && !tmp[0].equals("betaLoss")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("parampath")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("resultpath")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("threads")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("output")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("alphaPeak")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("betaPeak")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("alphaLoss")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("betaLoss")) {
			System.err.println("no csv defined");
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		getArgs(args);
		
		String paramfolder = (String)argsHash.get("parampath");
		String resfolder = (String)argsHash.get("resultpath");
		String outputfolder = (String)argsHash.get("output");
		String alphaPeak = (String)argsHash.get("alphaPeak");
		String betaPeak = (String)argsHash.get("betaPeak");
		String alphaLoss = (String)argsHash.get("alphaLoss");
		String betaLoss = (String)argsHash.get("betaLoss");
		int numberThreads = Integer.parseInt(argsHash.get("threads"));
		
		ALPHA_VALUE_PEAK = Double.parseDouble(alphaPeak);
		BETA_VALUE_PEAK = Double.parseDouble(betaPeak);

		ALPHA_VALUE_LOSS = Double.parseDouble(alphaLoss);
		BETA_VALUE_LOSS = Double.parseDouble(betaLoss);
		
		File _resfolder = new File(resfolder);
		File _paramfolder = new File(paramfolder);

		File[] resultFiles = _resfolder.listFiles();
		File[] paramFiles = _paramfolder.listFiles();
		
		ArrayList<ProcessThread> threads = new ArrayList<ProcessThread>();
		
		for(int i = 0; i < paramFiles.length; i++) {
			String id = paramFiles[i].getName().split("\\.")[0];
			int resultFileID = -1;
			for(int j = 0; j < resultFiles.length; j++) {
				if(resultFiles[j].getName().startsWith(id)) {
					resultFileID = j;
					break;
				}
			}
			if(resultFileID == -1) {
				System.out.println(id + " not found as result.");
				continue;
			}

			Settings settings = getSettings(paramFiles[i].getAbsolutePath());
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resultFiles[resultFileID].getAbsolutePath());
			
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, ALPHA_VALUE_PEAK);
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, BETA_VALUE_PEAK);
			
			settings.set(VariableNames.LOSS_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, ALPHA_VALUE_LOSS);
			settings.set(VariableNames.LOSS_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, BETA_VALUE_LOSS);
			
			SettingsChecker sc = new SettingsChecker();
			if(!sc.check(settings)) {
				System.out.println("Error checking settings for " + id);
				continue;
			}
			
			FilteredTandemMassPeakListReader reader = new FilteredTandemMassPeakListReader(settings);
			settings.set(VariableNames.PEAK_LIST_NAME, reader.read());
			
			
			ProcessThread thread = new CalculateScoreFromResultFilePeakLossThreadFP().new ProcessThread(settings, outputfolder);
			threads.add(thread);
		}
		System.out.println("preparation finished");
		
		ExecutorService executer = Executors.newFixedThreadPool(numberThreads);
		for(ProcessThread thread : threads) {
			executer.execute(thread);
		}
		executer.shutdown(); 
	    while(!executer.isTerminated())
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static synchronized void increaseNumberFinished () {
		numberFinished++;
		System.out.println("finished " + numberFinished);
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
		return getMatchByMass(matches, peakMass, false);
	}

	public static Match getMatchByMass(Vector<?> matches, Double peakMass, boolean debug) {
		for(int i = 0; i < matches.size(); i++) {
			Match match = (Match)matches.get(i);
			if(debug) System.out.println(match.getMass());
			if(match.getMass().equals(peakMass)) 
				return match;
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
	
	class ProcessThread extends Thread {
		protected Settings settings;
		protected String outputFolder;

		/**
		 * 
		 * @param settings
		 * @param outputFolder
		 */
		public ProcessThread(Settings settings, String outputFolder) {
			this.settings = settings;
			this.outputFolder = outputFolder;
		}
		
		/**
		 * 
		 */
		public void run() {
			IDatabase db = null;
			String dbFilename = (String)settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME);
			if(dbFilename.endsWith("psv")) db = new LocalPSVDatabase(settings);
			else db = new LocalCSVDatabase(settings);
			Vector<String> ids = null;
			try {
				ids = db.getCandidateIdentifiers();
			} catch (MultipleHeadersFoundInInputDatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			CandidateList candidates = null;
			try {
				candidates = db.getCandidateByIdentifier(ids);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(candidates.getNumberElements() == 0) {
				System.out.println("No candidates found in " + (String)settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
				return;
			}
			
			System.out.println(dbFilename.replaceAll(".*/", "") + ": Read " + candidates.getNumberElements() + " candidates");
			
			AutomatedPeakFingerprintAnnotationScoreInitialiser initPeak = new AutomatedPeakFingerprintAnnotationScoreInitialiser();
			AutomatedLossFingerprintAnnotationScoreInitialiser initLoss = new AutomatedLossFingerprintAnnotationScoreInitialiser();
			try {
				initPeak.initScoreParameters(this.settings);
				initLoss.initScoreParameters(this.settings);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			postProcessScoreParametersPeak(this.settings, candidates);
			postProcessScoreParametersLoss(this.settings, candidates);
			
			for(int i = 0; i < candidates.getNumberElements(); i++) { 
				singlePostCalculatePeak(this.settings, candidates.getElement(i));
				singlePostCalculateLoss(this.settings, candidates.getElement(i));
				candidates.getElement(i).removeProperty("PeakMatchList");
				candidates.getElement(i).removeProperty("LossMatchList");
			}
			
			CandidateListWriterCSV writer = new CandidateListWriterCSV();
			try {
				writer.write(candidates, (String)this.settings.get(VariableNames.SAMPLE_NAME), this.outputFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			increaseNumberFinished();
		}

		/**
		 * 
		 * @param settings
		 * @param candidates
		 */
		public void postProcessScoreParametersPeak(Settings settings, CandidateList candidates) {
			// to determine F_u
			MassToFingerprintsHashMap peakMassToFingerprints = new MassToFingerprintsHashMap();
			MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);

			for(int k = 0; k < candidates.getNumberElements(); k++) {
				/*
				 * check whether the single run was successful
				 */
				ICandidate currentCandidate = candidates.getElement(k);
				String fps = (String)currentCandidate.getProperty("FragmentFingerprintOfExplPeaks");
				if(fps.equals("NA")) {
					currentCandidate.setProperty("PeakMatchList", new Vector<Match>());
					continue;
				}
				String[] tmp = fps.split(";");
				Vector<Match> matchlist = new Vector<Match>();
				for(int i = 0; i < tmp.length; i++) {
					String[] tmp1 = tmp[i].split(":");
					Match match = new CalculateScoreFromResultFilePeakLossThreadFP().new Match(tmp1[1], Double.parseDouble(tmp1[0]));
					matchlist.add(match);
				}
				
				currentCandidate.setProperty("PeakMatchList", matchlist);
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
						peakMassToFingerprints.addFingerprint(match.getMass(), currentFingerprint);
					}
				}
			}

			double alpha = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
			double beta = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
			
			double f_seen = (double)settings.get(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
			double f_unseen = peakMassToFingerprints.getOverallSize();																// f_u
			double sumFingerprintFrequencies = (double)settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
			
			// set value for denominator of P(f,m)
			double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;

			settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
			
			double alphaProbability = alpha / denominatorValue; // P(f,m) F_u
			double betaProbability = beta / denominatorValue;	// p(f,m) not annotated
			
			for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
				MassToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
				
				// sum_f P(f,m)
				// calculate sum of MF_s (including the alpha count) and the joint probabilities
				// at this stage getProbability() returns the absolute counts from the annotation files
				double sum_f = 0.0;
				double sumFsProbabilities = 0.0;
				for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
					// first calculate P(f,m)
					groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getProbability() + alpha) / denominatorValue);
					// sum_f P(f,m) -> for F_s
					sumFsProbabilities += groupList.getElement(ii).getJointProbability();
				}
				
				// calculate the sum of probabilities for un-observed fingerprints for the current mass
				double sumFuProbabilities = alphaProbability * peakMassToFingerprints.getSize(groupList.getPeakmz());
				
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

		public void postProcessScoreParametersLoss(Settings settings, CandidateList candidates) {
			// to determine F_u
			MassToFingerprintsHashMap lossMassToFingerprints = new MassToFingerprintsHashMap();
			MassToFingerprintGroupListCollection lossToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
			
			Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
			Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		
			for(int k = 0; k < candidates.getNumberElements(); k++) {
				/*
				 * check whether the single run was successful
				 */
				ICandidate currentCandidate = candidates.getElement(k);
				String fps = (String)currentCandidate.getProperty("LossFingerprintOfExplPeaks");
				if(fps.equals("NA")) {
					currentCandidate.setProperty("LossMatchList", new Vector<Match>());
					continue;
				}
				String[] tmp = fps.split(";");
				Vector<Match> matchlist = new Vector<Match>();
				for(int i = 0; i < tmp.length; i++) {
					String[] tmp1 = tmp[i].split(":");
					double mass = Double.parseDouble(tmp1[0]);
					MassToFingerprintGroupList matchingLossToFingerprintGroupList = lossToFingerprintGroupListCollection.getElementByPeak(mass, mzppm, mzabs);
					if(matchingLossToFingerprintGroupList != null) mass = matchingLossToFingerprintGroupList.getPeakmz();
					Match match = new CalculateScoreFromResultFilePeakLossThreadFP().new Match(tmp1[1], mass);
					matchlist.add(match);
				}
				
				currentCandidate.setProperty("LossMatchList", matchlist);
				for(int j = 0; j < matchlist.size(); j++) {
					Match match = matchlist.get(j);
					MassToFingerprintGroupList peakToFingerprintGroupList = lossToFingerprintGroupListCollection.getElementByPeak(match.getMass());
					if(peakToFingerprintGroupList == null) continue;
					FastBitArray currentFingerprint = new FastBitArray(match.getFingerprint());
					//	if(match.getMatchedPeak().getMass() < 60) System.out.println(match.getMatchedPeak().getMass() + " " + currentFingerprint + " " + fragSmiles);
					// check whether fingerprint was observed for current peak mass in the training data
					if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
						// if not add the fingerprint to background by addFingerprint function
						// addFingerprint checks also whether fingerprint was already added
						lossMassToFingerprints.addFingerprint(match.getMass(), currentFingerprint);
					}
				}
			}

			double alpha = (double)settings.get(VariableNames.LOSS_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
			double beta = (double)settings.get(VariableNames.LOSS_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
			
			double f_seen = (double)settings.get(VariableNames.LOSS_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
			double f_unseen = lossMassToFingerprints.getOverallSize();																// f_u
			double sumFingerprintFrequencies = (double)settings.get(VariableNames.LOSS_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
			
			// set value for denominator of P(f,m)
			double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;

			settings.set(VariableNames.LOSS_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
			
			double alphaProbability = alpha / denominatorValue; // P(f,m) F_u
			double betaProbability = beta / denominatorValue;	// p(f,m) not annotated
			
			for(int i = 0; i < lossToFingerprintGroupListCollection.getNumberElements(); i++) {
				MassToFingerprintGroupList groupList = lossToFingerprintGroupListCollection.getElement(i);
				
				// sum_f P(f,m)
				// calculate sum of MF_s (including the alpha count) and the joint probabilities
				// at this stage getProbability() returns the absolute counts from the annotation files
				double sum_f = 0.0;
				double sumFsProbabilities = 0.0;
				for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
					// first calculate P(f,m)
					groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getProbability() + alpha) / denominatorValue);
					// sum_f P(f,m) -> for F_s
					sumFsProbabilities += groupList.getElement(ii).getJointProbability();
				}
				
				// calculate the sum of probabilities for un-observed fingerprints for the current mass
				double sumFuProbabilities = alphaProbability * lossMassToFingerprints.getSize(groupList.getPeakmz());
				
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
		
		public String getProbTypeString(Vector<Double> matchProb, Vector<Integer> matchType, Vector<Double> matchMasses) {
			String string = "NA";
			if(matchProb.size() >= 1) {
				string = matchType.get(0) + ":" + matchProb.get(0) + ":" + matchMasses.get(0);
			}
			for(int i = 1; i < matchProb.size(); i++) {
				string += ";" + matchType.get(i) + ":" + matchProb.get(i) + ":" + matchMasses.get(i);
			}
			return string;
		}
		
		public void singlePostCalculatePeak(Settings settings, ICandidate candidate) {
			//this.value = 0.0;
			double value = 0.0;
			MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
			
			int matches = 0;
			Vector<?> matchlist = (Vector<?>)candidate.getProperty("PeakMatchList");
			Vector<Double> matchMasses = new Vector<Double>();
			Vector<Double> matchProb = new Vector<Double>();
			Vector<Integer> matchType = new Vector<Integer>(); // found - 1; alpha - 2; beta - 3
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
					value += Math.log(peakToFingerprintGroupList.getBetaProb());
				} else {
					FastBitArray currentFingerprint = new FastBitArray(currentMatch.getFingerprint());
					// ToDo: at this stage try to check all fragments not only the best one
					matches++;
					// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
					double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
					// |F|
					if(matching_prob != 0.0) {
						value += Math.log(matching_prob);
						matchProb.add(matching_prob);
						matchType.add(1);
						matchMasses.add(currentMass);
					}
					else {
						value += Math.log(peakToFingerprintGroupList.getAlphaProb());
						matchProb.add(peakToFingerprintGroupList.getAlphaProb());
						matchType.add(2);
						matchMasses.add(currentMass);
					}
				}
			}
			if(peakToFingerprintGroupListCollection.getNumberElements() == 0) value = 0.0;
			
			candidate.setProperty("AutomatedPeakFingerprintAnnotationScore_Matches", matches);
			candidate.setProperty("AutomatedPeakFingerprintAnnotationScore", value);
			candidate.setProperty("AutomatedPeakFingerprintAnnotationScore_Probtypes", getProbTypeString(matchProb, matchType, matchMasses));
		}
		

		public void singlePostCalculateLoss(Settings settings, ICandidate candidate) {
			double value = 0.0;
			MassToFingerprintGroupListCollection lossToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);

			int matches = 0;
			Vector<?> matchlist = (Vector<?>)candidate.getProperty("LossMatchList");
			Vector<Double> matchMasses = new Vector<Double>();
			Vector<Double> matchProb = new Vector<Double>();
			Vector<Integer> matchType = new Vector<Integer>(); // found - 1; alpha - 2; beta - 3
			// get foreground fingerprint observations (m_f_observed)
			
			for(int i = 0; i < lossToFingerprintGroupListCollection.getNumberElements(); i++) {
				// get f_m_observed
				MassToFingerprintGroupList lossToFingerprintGroupList = lossToFingerprintGroupListCollection.getElement(i);
				Double currentMass = lossToFingerprintGroupList.getPeakmz();
				boolean debug = false;
			//	if(i == 8) debug = true;
				Match currentMatch = getMatchByMass(matchlist, currentMass, debug);
				if(currentMatch == null) {
					matchProb.add(lossToFingerprintGroupList.getBetaProb());
					matchType.add(3);
					matchMasses.add(currentMass);
					value += Math.log(lossToFingerprintGroupList.getBetaProb());
				} else {
					FastBitArray currentFingerprint = new FastBitArray(currentMatch.getFingerprint());
					// ToDo: at this stage try to check all fragments not only the best one
					matches++;
					// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
					double matching_prob = lossToFingerprintGroupList.getMatchingProbability(currentFingerprint);
					// |F|
					if(matching_prob != 0.0) {
						value += Math.log(matching_prob);
						matchProb.add(matching_prob);
						matchType.add(1);
						matchMasses.add(currentMass);
					}
					else {
						value += Math.log(lossToFingerprintGroupList.getAlphaProb());
						matchProb.add(lossToFingerprintGroupList.getAlphaProb());
						matchType.add(2);
						matchMasses.add(currentMass);
					}
				}
			}

			if(lossToFingerprintGroupListCollection.getNumberElements() == 0) value = 0.0;
			
			candidate.setProperty("AutomatedLossFingerprintAnnotationScore_Matches", matches);
			candidate.setProperty("AutomatedLossFingerprintAnnotationScore", value);
			candidate.setProperty("AutomatedLossFingerprintAnnotationScore_Probtypes", getProbTypeString(matchProb, matchType, matchMasses));
	 	}
	}
}
