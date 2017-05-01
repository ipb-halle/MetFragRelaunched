package de.ipbhalle.metfrag.substructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredTandemMassPeakListReader;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedFingerprintSubstructureAnnotationScoreInitialiser4;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintToMassesHashMap;
import de.ipbhalle.metfraglib.substructure.FingerprintToMassesSimpleHashMap;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;

public class CalculateScoreFromResultFileThreadPF {

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
		
		for(int i = 0; i < resultFiles.length; i++) {
			String id = resultFiles[i].getName().split("\\.")[0];
			int paramFileID = -1;
			for(int j = 0; j < paramFiles.length; j++) {
				if(paramFiles[j].getName().startsWith(id)) {
					paramFileID = j;
					break;
				}
			}
			if(paramFileID == -1) {
				System.out.println(id + " not found as param.");
				continue;
			}
			
			Settings settings = getSettings(paramFiles[paramFileID].getAbsolutePath());
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resultFiles[i].getAbsolutePath());
			
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, ALPHA_VALUE);
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, BETA_VALUE);
			
			FilteredTandemMassPeakListReader reader = new FilteredTandemMassPeakListReader(settings);
			settings.set(VariableNames.PEAK_LIST_NAME, reader.read());
			
			
			ProcessThread thread = new CalculateScoreFromResultFileThreadPF().new ProcessThread(settings, outputfolder);
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
	
	/**
	 * 
	 * @param parameterfile
	 * @return
	 */
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
	
	public static synchronized void increaseNumberFinished () {
		numberFinished++;
		System.out.println("finished " + numberFinished);
	}
	
	/**
	 * 
	 * @author chrisr
	 *
	 */
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
	
	/**
	 * 
	 * @author chrisr
	 *
	 */
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
			
			System.out.println("Read " + candidates.getNumberElements() + " candidates");
			
			AutomatedFingerprintSubstructureAnnotationScoreInitialiser4 init = new AutomatedFingerprintSubstructureAnnotationScoreInitialiser4();
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
			
			try {
				checkProbabilites(settings, false);
			} catch (IOException e) {
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
			PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		
			double alpha = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
			Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
			Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			String filenameConv = (String)settings.get(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_CONV_NAME);
			
			FingerprintToMassesSimpleHashMap negativeFingerprints = new FingerprintToMassesSimpleHashMap();
			
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
					Match match = new CalculateScoreFromResultFileThreadPF().new Match(tmp1[1], Double.parseDouble(tmp1[0]));
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
						//fingerprintToMasses.addMass(currentFingerprint, match.getMass(), 0.0, mzppm, mzabs);
						negativeFingerprints.addMass(currentFingerprint, match.getMass());
					}
				}
			}
		
			// read in fingerprint file and add pseudo count alpha to every observation count
			// at this step we read the absolute frequencies
			FingerprintToMassesHashMap fingerprintToMasses = readFingerprintToMasses(filenameConv, 
					(DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME), mzppm, mzabs, alpha);
			
			double beta = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
			
			double f_seen = (double)settings.get(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
			double f_unseen = negativeFingerprints.getOverallSize();															// f_u
			double sumFingerprintFrequencies = (double)settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
			
			double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;
			settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
			
			double alphaProbability = alpha / denominatorValue; 	// P(f,m) if f,m in F_u
			double betaProbability = beta / denominatorValue;		// p(f,m) not annotated
			
			//fingerprints of F_s
			FastBitArray[] fingerprints = fingerprintToMasses.getFingerprints();
			FastBitArray[] fingerprintsNegative = negativeFingerprints.getFingerprints();
			
			// normalize all absolute frequencies
			// after this stage we have P(f,m)
			fingerprintToMasses.normalizeNumObservations(denominatorValue);
			
			// calculate sum_m P(m,f) for each f
			// add P(m,f) = alphaProbability in case m wasn't observed for f
			// but f was observed in training
			for(int i = 0; i < fingerprints.length; i++) {
				int numUnseenMasses = 0; 
				if(negativeFingerprints.containsFingerprint(fingerprints[i])) {
					numUnseenMasses = negativeFingerprints.getMasses(fingerprints[i]).size();
				}
				fingerprintToMasses.calculateSumNumObservations(fingerprints[i], numUnseenMasses * alphaProbability);
			}
			// add fingerprints seen in testing but not in training
			for(int i = 0; i < fingerprintsNegative.length; i++) {
				// add fingerprints that were not observed in training 
				if(!fingerprintToMasses.containsFingerprint(fingerprintsNegative[i])) {
					java.util.LinkedList<Double> masses = negativeFingerprints.getMasses(fingerprintsNegative[i]);
					java.util.Iterator<?> it = masses.iterator();
					while(it.hasNext()) {
						fingerprintToMasses.addMass(fingerprintsNegative[i], (Double)it.next(), 0.0);
					}
					fingerprintToMasses.calculateSumNumObservations(fingerprintsNegative[i], masses.size() * alphaProbability);
				}
			}
			
			// renew fingerprint array
			fingerprints = fingerprintToMasses.getFingerprints();
			
			for(int i = 0; i < fingerprints.length; i++) {
				fingerprintToMasses.setAlphaProbability(fingerprints[i], alphaProbability / fingerprintToMasses.getSumNumObservations(fingerprints[i]));
				// if you want to check probabilities then uncomment next line
				//fingerprintToMasses.normalizeNumObservations(fingerprints[i], fingerprintToMasses.getSumNumObservations(fingerprints[i]));
			}
			fingerprintToMasses.setBetaProbability(betaProbability / (double)(int)settings.get(VariableNames.NUMBER_BACKGROUND_MASSES_NAME));
			
			for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
				PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
				for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
					// second calculate P(f|m)
					double sum_m = fingerprintToMasses.getSumNumObservations(groupList.getElement(ii).getFingerprint()); 
					if(sum_m == 0) {
					//	System.err.println("Check " + groupList.getPeakmz() + " " + groupList.getElement(ii).getFingerprint().toStringIDs() + " " + groupList.getElement(ii).getFingerprint().toString());
					//	System.out.println(fingerprintToMasses.getIndexOfFingerprint(groupList.getElement(ii).getFingerprint()));
					}
					FastBitArray currentFingerprint = groupList.getElement(ii).getFingerprint();
					double numObservations = fingerprintToMasses.getNumObservations(currentFingerprint, groupList.getPeakmz());
					groupList.getElement(ii).setConditionalProbability_ps(numObservations / sum_m);
				}
				groupList.setProbabilityToConditionalProbability_ps();
			}
			
			settings.set(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME, fingerprintToMasses);
			
			return;
		}
		
		/**
		 * calculate single score for candidate
		 * 
		 * @param settings
		 * @param candidate
		 */
		public void singlePostCalculate(Settings settings, ICandidate candidate) {
			//this.value = 0.0;
			double value = 1.0;
			PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
			FingerprintToMassesHashMap fingerprintToMasses = (FingerprintToMassesHashMap)settings.get(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME);
			
			ArrayList<?> matchlist = (ArrayList<?>)candidate.getProperty("MatchList");
		
			int matches = 0;
			// get foreground fingerprint observations (m_f_observed)
			for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
				// get f_m_observed
				PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
				Double currentMass = peakToFingerprintGroupList.getPeakmz();
				Match currentMatch = getMatchByMass(matchlist, currentMass);
				
				//(fingerprintToMasses.getSize(currentFingerprint));
				if(currentMatch == null) {
					value *= fingerprintToMasses.getBetaProbabilty();
				} else {
					FastBitArray currentFingerprint = new FastBitArray(currentMatch.getFingerprint());
					// ToDo: at this stage try to check all fragments not only the best one
					matches++;
					// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
					double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
					// |F|
					if(matching_prob != 0.0) value *= matching_prob;
					else {
						double alphaProb = fingerprintToMasses.getAlphaProbabilty(currentFingerprint);
						value *= alphaProb;
					}
				}
			}
			
			if(peakToFingerprintGroupListCollection.getNumberElements() == 0) value = 0.0;
			candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore4_Matches", matches);
			candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore4", value);
	 	}
		
		public Match getMatchByMass(ArrayList<?> matches, Double peakMass) {
			for(int i = 0; i < matches.size(); i++) {
				Match match = (Match)matches.get(i);
				if(match.getMass().equals(peakMass)) return match;
			}
			return null;
		}
		
		/**
		 * check whether probabilities sum to 1
		 * 
		 * @param settings
		 * @throws IOException 
		 */
		public void checkProbabilites(Settings settings, boolean echo) throws IOException {
			
			FingerprintToMassesHashMap fingerprintToMasses = (FingerprintToMassesHashMap)settings.get(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME);
			FastBitArray[] fingerprints = fingerprintToMasses.getFingerprints();
			
			for(int i = 0; i < fingerprints.length; i++) {
				fingerprintToMasses.calculateSumNumObservations(fingerprints[i], 0.0);
				double sum = 0.0;
				
				// get f_m_observed
				sum += fingerprintToMasses.getSumNumObservations(fingerprints[i]);
				
				if(echo) System.out.println(fingerprints[i].toStringIDs() + " " + MathTools.round(sum, 15));
			}
		}
		
		/*
		 * 
		 */
		public FingerprintToMassesHashMap readFingerprintToMasses(String filename, DefaultPeakList peakList, double mzppm, double mzabs, double alpha) {
			FingerprintToMassesHashMap fingerprintToMasses = new FingerprintToMassesHashMap();
			BufferedReader breader = null;
			try {
				breader = new BufferedReader(new FileReader(new File(filename)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String line = "";
			try {
				while((line = breader.readLine()) != null) {
					line = line.trim();
					String[] tmp = line.split("\\s+");
					FastBitArray currentFingerprint = new FastBitArray(tmp[0]);
					for(int k = 1; k < tmp.length; k += 2) {
						fingerprintToMasses.addMass(currentFingerprint, Double.parseDouble(tmp[k+1]), Double.parseDouble(tmp[k]) + alpha);
					}
				}
				breader.close();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			for(int j = 0; j < peakList.getNumberElements(); j++) {
				fingerprintToMasses.correctMasses(((IPeak)peakList.getElement(j)).getMass(), mzppm, mzabs);
			}
			
			return fingerprintToMasses;
		}
		
	}
	
}
