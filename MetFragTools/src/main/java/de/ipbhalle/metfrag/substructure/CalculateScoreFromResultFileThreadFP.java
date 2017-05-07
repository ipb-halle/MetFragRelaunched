package de.ipbhalle.metfrag.substructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredTandemMassPeakListReader;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedFingerprintSubstructureAnnotationScoreInitialiser3;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintsHashMap;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;

public class CalculateScoreFromResultFileThreadFP {

	public static double ALPHA_VALUE = 0.0001;
	public static double BETA_VALUE = 0.0001;
	public static int numberFinished = 0;
	
	public static void main(String[] args) throws Exception {
		String paramfolder = args[0];
		String resfolder = args[1];
		String outputfolder = args[2];
		String alpha = args[3];
		String beta = args[4];
		int numberThreads = Integer.parseInt(args[5]);
		
		ALPHA_VALUE = Double.parseDouble(alpha);
		BETA_VALUE = Double.parseDouble(beta);
		
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
			
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, ALPHA_VALUE);
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, BETA_VALUE);
			
			FilteredTandemMassPeakListReader reader = new FilteredTandemMassPeakListReader(settings);
			settings.set(VariableNames.PEAK_LIST_NAME, reader.read());
			
			
			ProcessThread thread = new CalculateScoreFromResultFileThreadFP().new ProcessThread(settings, outputfolder);
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
			LocalPSVDatabase db = new LocalPSVDatabase(settings);
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
			
			CandidateList candidates = db.getCandidateByIdentifier(ids);
			if(candidates.getNumberElements() == 0) {
				System.out.println("No candidates found in " + (String)settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
				return;
			}
			
			System.out.println("Read " + candidates.getNumberElements() + " candidates");
			
			AutomatedFingerprintSubstructureAnnotationScoreInitialiser3 init = new AutomatedFingerprintSubstructureAnnotationScoreInitialiser3();
			try {
				init.initScoreParameters(settings);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			postProcessScoreParameters(settings, candidates);
			
			for(int i = 0; i < candidates.getNumberElements(); i++) { 
				singlePostCalculate(settings, candidates.getElement(i));
				candidates.getElement(i).removeProperty("MatchList");
			}
			
			CandidateListWriterCSV writer = new CandidateListWriterCSV();
			try {
				writer.write(candidates, (String)settings.get(VariableNames.SAMPLE_NAME), this.outputFolder);
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
		public void postProcessScoreParameters(Settings settings, CandidateList candidates) {
			// to determine F_u
			MassToFingerprintsHashMap massToFingerprints = new MassToFingerprintsHashMap();
			PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);

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
					Match match = new CalculateScoreFromResultFileThreadFP().new Match(tmp1[1], Double.parseDouble(tmp1[0]));
					matchlist.add(match);
				}
				
				currentCandidate.setProperty("MatchList", matchlist);
				for(int j = 0; j < matchlist.size(); j++) {
					Match match = matchlist.get(j);
					PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(match.getMass());
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
				PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
				
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
		
		public void singlePostCalculate(Settings settings, ICandidate candidate) {
			//this.value = 0.0;
			double value = 1.0;
			PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
			
			int matches = 0;
			Vector<?> matchlist = (Vector<?>)candidate.getProperty("MatchList");
			// get foreground fingerprint observations (m_f_observed)
			for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
				// get f_m_observed
				PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
				Double currentMass = peakToFingerprintGroupList.getPeakmz();
				Match currentMatch = getMatchByMass(matchlist, currentMass);

				//(fingerprintToMasses.getSize(currentFingerprint));
				if(currentMatch == null) {
					value *= peakToFingerprintGroupList.getBetaProb();
				} else {
					FastBitArray currentFingerprint = new FastBitArray(currentMatch.getFingerprint());
					// ToDo: at this stage try to check all fragments not only the best one
					matches++;
					// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
					double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
					// |F|
					if(matching_prob != 0.0) value *= matching_prob;
					else value *= peakToFingerprintGroupList.getAlphaProb();
				}
			}
			if(peakToFingerprintGroupListCollection.getNumberElements() == 0) value = 0.0;
			candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore3_Matches", matches);
			candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore3", value);
	 	}
		
	}
}
