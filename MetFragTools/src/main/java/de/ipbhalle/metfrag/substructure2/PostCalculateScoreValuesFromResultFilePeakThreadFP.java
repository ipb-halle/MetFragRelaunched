package de.ipbhalle.metfrag.substructure2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.interfaces.IPeakListReader;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.match.MassFingerprintMatch;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;

public class PostCalculateScoreValuesFromResultFilePeakThreadFP {

	public static double ALPHA_VALUE_PEAK = 0.0001;
	public static double BETA_VALUE_PEAK = 0.0001;

	public static int numberFinished = 0;
	public static java.util.Hashtable<String, String> argsHash;

	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("parampath") && !tmp[0].equals("resultpath") && !tmp[0].equals("threads")
					&& !tmp[0].equals("output") && !tmp[0].equals("alpha") && !tmp[0].equals("beta")
					&& !tmp[0].equals("outputtype") && !tmp[0].equals("stdout") && !tmp[0].equals("negscore")
					&& !tmp[0].equals("scorenames") && !tmp[0].equals("transform") && !tmp[0].equals("outputfolder")) {
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
		if (!argsHash.containsKey("alpha")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("beta")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("type")) {
			argsHash.put("type", "csv");
		}
		if (!argsHash.containsKey("stdout")) {
			argsHash.put("stdout", "false");
		}
		if (!argsHash.containsKey("outputtype")) {
			argsHash.put("outputtype", "rank"); // or "list"
		}
		if (!argsHash.containsKey("outputfolder")) {
			argsHash.put("outputfolder", Constants.OS_TEMP_DIR); // or "list"
		}
		if (!argsHash.containsKey("negscore")) {
			argsHash.put("negscore", "false");
		}
		if (!argsHash.containsKey("scorenames")) {
			System.err.println("no scorenames defined");
			return false;
		}
		if (!argsHash.containsKey("transform")) {
			argsHash.put("transform", "false");
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		getArgs(args);

		String paramfolder = (String) argsHash.get("parampath");
		String resfolder = (String) argsHash.get("resultpath");
		String outputfile = (String) argsHash.get("output");
		String alpha = (String) argsHash.get("alpha");
		String beta = (String) argsHash.get("beta");
		int numberThreads = Integer.parseInt(argsHash.get("threads"));
		Boolean stdout = Boolean.parseBoolean(argsHash.get("stdout"));
		String outputtype = (String) argsHash.get("outputtype");
		Boolean negScores = Boolean.parseBoolean(argsHash.get("negscore"));
		String[] scoringPropertyNames = argsHash.get("scorenames").split(",");
		Boolean transformScores = Boolean.parseBoolean(argsHash.get("transform"));
		String outputfolder = (String) argsHash.get("outputfolder");

		ALPHA_VALUE_PEAK = Double.parseDouble(alpha);
		BETA_VALUE_PEAK = Double.parseDouble(beta);

		File _resfolder = new File(resfolder);
		File _paramfolder = new File(paramfolder);

		File[] resultFiles = _resfolder.listFiles();
		File[] paramFiles = _paramfolder.listFiles();

		ArrayList<ProcessThread> threads = new ArrayList<ProcessThread>();

		for (int i = 0; i < paramFiles.length; i++) {
			String id = paramFiles[i].getName().split("\\.")[0];
			int resultFileID = -1;
			for (int j = 0; j < resultFiles.length; j++) {
				if (resultFiles[j].getName().startsWith(id + ".")) {
					resultFileID = j;
					break;
				}
			}
			if (resultFileID == -1) {
				System.err.println(id + " not found as result.");
				continue;
			}

			Settings settings = getSettings(paramFiles[i].getAbsolutePath());
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resultFiles[resultFileID].getAbsolutePath());

			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, ALPHA_VALUE_PEAK);
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, BETA_VALUE_PEAK);

			SettingsChecker sc = new SettingsChecker();
			if (!sc.check(settings)) {
				System.err.println("Error checking settings for " + id);
				continue;
			}
			IPeakListReader peakListReader = (IPeakListReader) Class
					.forName((String) settings.get(VariableNames.METFRAG_PEAK_LIST_READER_NAME))
					.getConstructor(Settings.class).newInstance(settings);

			settings.set(VariableNames.PEAK_LIST_NAME, peakListReader.read());

			ProcessThread thread = new PostCalculateScoreValuesFromResultFilePeakThreadFP().new ProcessThread(
					settings, outputfile, resfolder, paramFiles[i].getAbsolutePath(), outputtype, negScores, 
					transformScores, scoringPropertyNames, stdout);
			threads.add(thread);
		}
		System.err.println("preparation finished");

		ExecutorService executer = Executors.newFixedThreadPool(numberThreads);
		for (ProcessThread thread : threads) {
			executer.execute(thread);
		}
		executer.shutdown();
		while (!executer.isTerminated()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		File folder = new File(outputfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "scores");
		if(!folder.exists()) folder.mkdirs();
		String fileprefix = alpha + "_" + beta;
		fileprefix = fileprefix.replaceAll("\\.", "");
		
		String path = folder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + fileprefix + Constants.OS_SPECIFIC_FILE_SEPARATOR;
		new File(path).mkdirs();
		
		CandidateListWriterPSV psvWriter = new CandidateListWriterPSV();
		for(int i = 0; i < threads.size(); i++) {
			CandidateList candidates = threads.get(i).getCandidateList();
			String paramid = threads.get(i).getParamFileName().replaceAll(".*\\/", "").replaceAll("\\.txt", "");
			String filename = Constants.OS_SPECIFIC_FILE_SEPARATOR + paramid + "_peak.psv";
			filename = path + filename;
			psvWriter.write(candidates, filename);
		}
	}

	public static double[][] readWeights(String weightsfile) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(new File(weightsfile)));
		ArrayList<double[]> weights = new ArrayList<double[]>();
		String line = "";
		while ((line = breader.readLine()) != null) {
			line = line.trim();
			String[] tmp = line.split("\\s+");
			double[] doubleweights = new double[tmp.length];
			for (int i = 0; i < tmp.length; i++)
				doubleweights[i] = Double.parseDouble(tmp[i]);
			weights.add(doubleweights);
		}
		breader.close();
		double[][] weightmatrix = new double[weights.size()][weights.get(0).length];
		for (int i = 0; i < weights.size(); i++) {
			for (int k = 0; k < weights.get(0).length; k++) {
				weightmatrix[i][k] = weights.get(i)[k];
			}
		}
		return weightmatrix;
	}

	public static void printBestRankingsPos(ArrayList<ProcessThread> threads, int bestWeightIndex) {
		for(ProcessThread thread : threads)
			if(thread.ispositivequery) System.out.println(thread.paramFile.replaceAll(".*/", "") + " " + thread.getRanksForWeight()[bestWeightIndex]);
	}

	public static void printBestRankingsNeg(ArrayList<ProcessThread> threads, int bestWeightIndex) {
		for(ProcessThread thread : threads)
			if(!thread.ispositivequery) System.out.println(thread.paramFile.replaceAll(".*/", "") + " " + thread.getRanksForWeight()[bestWeightIndex]);
	}
	
	public static synchronized void increaseNumberFinished(String name) {
		numberFinished++;
	//	System.err.println("finished " + numberFinished + " -> " + name);
	}

	public static Settings getSettings(String parameterfile) {
		File parameterFile = new File(parameterfile);
		MetFragGlobalSettings settings = null;
		try {
			settings = MetFragGlobalSettings.readSettings(parameterFile, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return settings;
	}

	public static MassFingerprintMatch getMatchByMass(ArrayList<?> matches, Double peakMass) {
		return getMatchByMass(matches, peakMass, false);
	}

	public static MassFingerprintMatch getMatchByMass(ArrayList<?> matches, Double peakMass, boolean debug) {
		for (int i = 0; i < matches.size(); i++) {
			MassFingerprintMatch match = (MassFingerprintMatch) matches.get(i);
			if (debug)
				System.out.println(match.getMass());
			if (match.getMass().equals(peakMass))
				return match;
		}
		return null;
	}

	class ProcessThread extends Thread {
		protected Settings settings;
		protected String outputfile;
		protected String resultsfolder;
		protected String paramFile;
		protected String outputtype;
		protected boolean negScores;
		protected String[] scorenames;
		protected boolean tranformscores;
		protected boolean stdout;
		protected int[] ranks_for_weight;
		protected boolean ispositivequery;
		protected CandidateList candidates;
		
		/**
		 * 
		 * @param settings
		 * @param outputFolder
		 */
		public ProcessThread(Settings settings, String outputfile, String resultsfolder, String paramFile,
				String outputtype, boolean negScores, boolean tranformscores, String[] scorenames,
				boolean stdout) {
			this.settings = settings;
			this.outputfile = outputfile;
			this.resultsfolder = resultsfolder;
			this.paramFile = paramFile;
			this.outputtype = outputtype;
			this.negScores = negScores;
			this.scorenames = scorenames;
			this.tranformscores = tranformscores;
			this.stdout = stdout;
			if (this.paramFile.contains("-01."))
				this.ispositivequery = true;
			else
				this.ispositivequery = false;
		}

		public String getParamFileName() {
			return this.paramFile;
		}
		
		/**
		 * 
		 */
		public void run() {
			IDatabase db = null;
			String dbFilename = (String) settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME);
			if (dbFilename.endsWith("psv"))
				db = new LocalPSVDatabase(this.settings);
			else
				db = new LocalCSVDatabase(settings);
			ArrayList<String> ids = null;
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
			if (candidates.getNumberElements() == 0) {
				System.err.println(
						"No candidates found in " + (String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
				return;
			}
			System.err.println(
					dbFilename.replaceAll(".*/", "") + ": Read " + candidates.getNumberElements() + " candidates");
			try {
				this.postProcessScoreParametersPeak(this.settings, candidates);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			for (int i = 0; i < candidates.getNumberElements(); i++) {
				this.settings.set(VariableNames.CANDIDATE_NAME, candidates.getElement(i));
				this.singlePostCalculatePeak(this.settings, candidates.getElement(i));
				this.removeProperties(candidates.getElement(i));
			}
			
			this.candidates = candidates;
			increaseNumberFinished(this.paramFile);
		}

		public void removeProperties(ICandidate candidate) {
			candidate.removeProperty("PeakMatchList");
			candidate.removeProperty("NoExplPeaks");
			candidate.removeProperty("NumberPeaksUsed");
			candidate.removeProperty("LossFingerprintOfExplPeaks");
			candidate.removeProperty("FragmentFingerprintOfExplPeaks");
			candidate.removeProperty("AutomatedLossFingerprintAnnotationScore_Probtypes");
			candidate.removeProperty("PeakMatchList");
			
		}
		
		public int getBestRank(int[] ranks, String dbFilename) {
			int minRank = Integer.MAX_VALUE;
			for(int i = 0; i < ranks.length; i++) {
				if(minRank > ranks[i]) minRank = ranks[i];
			}
			return minRank;
		}
		
		public CandidateList getCandidateList() {
			return this.candidates;
		}
		
		public boolean isPositiveQuery() {
			return this.ispositivequery;
		}

		public int[] getRanksForWeight() {
			return this.ranks_for_weight;
		}

		protected String getInChIKey() throws IOException {
			String inchikey = "";
			BufferedReader breader = new BufferedReader(new FileReader(this.paramFile));
			String line = "";
			while ((line = breader.readLine()) != null) {
				if (line.matches("# [A-Z]*")) {
					inchikey = line.split("\\s+")[1];
					break;
				} else if (line.matches("# InChIKey [A-Z]*-*[A-Z]*-*[A-Z]*")) {
					inchikey = line.split("\\s+")[2].split("-")[0];
					break;
				} else if (line.matches("# [A-Z]*-*[A-Z]*-*[A-Z]*")) {
					inchikey = line.split("\\s+")[1].split("-")[0];
					break;
				}

			}
			breader.close();
			return inchikey;
		}

		/**
		 * 
		 * @param settings
		 * @param candidates
		 * @throws IOException
		 */
		public void postProcessScoreParametersPeak(Settings settings, CandidateList candidates) throws IOException {
			String samplename = (String) settings.get(VariableNames.SAMPLE_NAME);
			String filename = this.resultsfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + samplename + "_data_peak.txt";
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			Double sumFingerprintFrequencies = null;
			Double f_seen = null;
			Double f_unseen = null;
			java.util.HashMap<Double, java.util.LinkedList<Double>> massToObservations = new java.util.HashMap<Double, java.util.LinkedList<Double>>();
			java.util.HashMap<Double, Double> massToBackgroundSize = new java.util.HashMap<Double, Double>();
			boolean valuesStarted = false;
			while ((line = breader.readLine()) != null) {
				line = line.trim();
				if (valuesStarted) {
					String[] tmp = line.split("\\s+");
					Double mass = Double.parseDouble(tmp[0]);
					java.util.LinkedList<Double> observations = new java.util.LinkedList<Double>();
					for (int i = 1; i < (tmp.length - 1); i++) {
						observations.add(Double.parseDouble(tmp[i]));
					}
					massToObservations.put(mass, observations);
					massToBackgroundSize.put(mass, Double.parseDouble(tmp[tmp.length - 1].split(":")[1]));
				} else if (line.startsWith("sumFingerprintFrequencies"))
					sumFingerprintFrequencies = Double.parseDouble(line.split("\\s+")[1]);
				else if (line.startsWith("f_seen"))
					f_seen = Double.parseDouble(line.split("\\s+")[1]);
				else if (line.startsWith("f_unseen"))
					f_unseen = Double.parseDouble(line.split("\\s+")[1]);
				else if (line.startsWith("PeakToFingerprints"))
					valuesStarted = true;
			}
			breader.close();

			double alpha = (double) settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME); // alpha
			double beta = (double) settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME); // beta
			// set value for denominator of P(f,m)
			double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;

			settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);

			double alphaProbability = alpha / denominatorValue; // P(f,m) F_u
			double betaProbability = beta / denominatorValue; // p(f,m) not
																// annotated
			java.util.Iterator<Double> it = massToObservations.keySet().iterator();

			java.util.HashMap<Double, Double> massToSumF = new java.util.HashMap<Double, Double>();
			java.util.HashMap<Double, Double> massToAlphaProb = new java.util.HashMap<Double, Double>();
			java.util.HashMap<Double, Double> massToBetaProb = new java.util.HashMap<Double, Double>();

			while (it.hasNext()) {
				Double mass = it.next();
				java.util.LinkedList<Double> observations = massToObservations.get(mass);
				Double bgsize = massToBackgroundSize.get(mass);
				// sum_f P(f,m)
				// calculate sum of MF_s (including the alpha count) and the
				// joint probabilities
				// at this stage getProbability() returns the absolute counts
				// from the annotation files
				double sum_f = 0.0;
				double sumFsProbabilities = 0.0;
				for (int ii = 0; ii < observations.size(); ii++) {
					// sum_f P(f,m) -> for F_s
					sumFsProbabilities += (observations.get(ii) + alpha) / denominatorValue;
				}

				// calculate the sum of probabilities for un-observed
				// fingerprints for the current mass
				double sumFuProbabilities = alphaProbability * bgsize;

				sum_f += sumFsProbabilities;
				sum_f += sumFuProbabilities;
				sum_f += betaProbability;

				massToSumF.put(mass, sum_f);
				massToAlphaProb.put(mass, alphaProbability / sum_f);
				massToBetaProb.put(mass, betaProbability / sum_f);
				
				//printSum(mass, observations, alpha, betaProbability, sum_f, denominatorValue, bgsize);
			}
			settings.set("PeakMassToSumF", massToSumF);
			settings.set("PeakMassToAlphaProb", massToAlphaProb);
			settings.set("PeakMassToBetaProb", massToBetaProb);
			settings.set("PeakDenominatorValue", denominatorValue);
			return;
		}

		public void printSum(double mass, java.util.LinkedList<Double> obs, double alpha, double betaProb, double sum_f, double denominatorValue, double bgsize) {
			double sum = 0.0;
			for(Double ob : obs) {
				sum += ((ob + alpha) / denominatorValue) / sum_f;
				System.out.println("ob " + ((ob + alpha) / denominatorValue));
			}
			sum += ((alpha / denominatorValue) / sum_f) * bgsize + betaProb / sum_f;
			System.out.println(mass + " " + sum + " " + ((alpha / denominatorValue)));
		}
		
		public String getProbTypeString(ArrayList<Double> matchProb, ArrayList<Integer> matchType,
				ArrayList<Double> matchMasses) {
			String string = "NA";
			if (matchProb.size() >= 1) {
				string = matchType.get(0) + ":" + matchProb.get(0) + ":" + matchMasses.get(0);
			}
			for (int i = 1; i < matchProb.size(); i++) {
				string += ";" + matchType.get(i) + ":" + matchProb.get(i) + ":" + matchMasses.get(i);
			}
			return string;
		}

		protected void readMassToProbType(String line, java.util.Vector<Double> masses,
				java.util.Vector<String> probtypes) {
			if (line.equals("NA"))
				return;
			String[] tmp1 = line.split(";");
			for (int i = 0; i < tmp1.length; i++) {
				String[] tmp2 = tmp1[i].split(":");
				String probType = tmp2[1];
				if (tmp2.length == 3)
					probType += ":" + tmp2[2];
				masses.add(Double.parseDouble(tmp2[0]));
				probtypes.add(probType);
			}
			return;
		}

		public void singlePostCalculatePeak(Settings settings, ICandidate candidate) {
			double value = 0.0;
			int matches = 0;
			java.util.Vector<Double> peakMasses = new java.util.Vector<Double>();
			java.util.Vector<String> peakProbTypes = new java.util.Vector<String>();
			this.readMassToProbType((String) candidate.getProperty("AutomatedPeakFingerprintAnnotationScore_Probtypes"),
					peakMasses, peakProbTypes);
			ArrayList<Double> matchMasses = new ArrayList<Double>();
			ArrayList<Double> matchProb = new ArrayList<Double>();
			ArrayList<Integer> matchType = new ArrayList<Integer>(); // found -
																		// 1;
																		// alpha
																		// - 2;
																		// beta
																		// - 3
			// get foreground fingerprint observations (m_f_observed)

			double alpha = (double) settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME); // alpha
			java.util.HashMap<?, ?> massToSumF = (java.util.HashMap<?, ?>) settings.get("PeakMassToSumF");
			java.util.HashMap<?, ?> massToAlphaProb = (java.util.HashMap<?, ?>) settings.get("PeakMassToAlphaProb");
			java.util.HashMap<?, ?> massToBetaProb = (java.util.HashMap<?, ?>) settings.get("PeakMassToBetaProb");
			Double denominatorValue = (Double) settings.get("PeakDenominatorValue");
			for (int k = 0; k < peakMasses.size(); k++) {
				Double mass = peakMasses.get(k);
				String probType = peakProbTypes.get(k);
				String[] tmp = probType.split(":");
				// (fingerprintToMasses.getSize(currentFingerprint));
				if (tmp.length == 1) {
					double prob = 0.0;
					if (tmp[0].equals("2"))
						prob = (Double) massToAlphaProb.get(mass);
					else if (tmp[0].equals("3"))
						prob = (Double) massToBetaProb.get(mass);
					matchProb.add(prob);
					matchType.add(Integer.parseInt(tmp[0]));
					matchMasses.add(mass);
					value += Math.log(prob);
				} else {
					matches++;
					// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
					double matching_prob = ((Double.parseDouble(tmp[0]) + alpha) / denominatorValue) / (Double) massToSumF.get(mass);
					// |F|
					if (matching_prob != 0.0) {
						value += Math.log(matching_prob);
						matchProb.add(matching_prob);
						matchType.add(1);
						matchMasses.add(mass);
					}
				}
			}

			candidate.setProperty("AutomatedPeakFingerprintAnnotationScore_Matches", matches);
			candidate.setProperty("AutomatedPeakFingerprintAnnotationScore", value);
			candidate.setProperty("AutomatedPeakFingerprintAnnotationScore_Probtypes", getProbTypeString(matchProb, matchType, matchMasses));
		}
	}
}
