package de.ipbhalle.metfrag.substructure2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfrag.ranking.GetRankOfCandidateList;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.match.MassFingerprintMatch;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;

public class RankFromResultFilePeakLossThreadFP {

	public static int numberFinished = 0;
	public static java.util.Hashtable<String, String> argsHash;

	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("parampath") && !tmp[0].equals("resultpathloss") && !tmp[0].equals("resultpathpeak") && !tmp[0].equals("threads")
					&& !tmp[0].equals("output") 
					&& !tmp[0].equals("weights") && !tmp[0].equals("outputtype") && !tmp[0].equals("stdout") && !tmp[0].equals("negscore")
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
		if (!argsHash.containsKey("resultpathpeak")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("resultpathloss")) {
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
		if (!argsHash.containsKey("weights")) {
			System.err.println("no weights defined");
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
		String resfolderpeak = (String) argsHash.get("resultpathpeak");
		String resfolderloss = (String) argsHash.get("resultpathloss");
		String outputfile = (String) argsHash.get("output");
		int numberThreads = Integer.parseInt(argsHash.get("threads"));
		double[][] weights = readWeights(argsHash.get("weights"));
		Boolean stdout = Boolean.parseBoolean(argsHash.get("stdout"));
		String outputtype = (String) argsHash.get("outputtype");
		Boolean negScores = Boolean.parseBoolean(argsHash.get("negscore"));
		String[] scoringPropertyNames = argsHash.get("scorenames").split(",");
		Boolean transformScores = Boolean.parseBoolean(argsHash.get("transform"));
		String outputfolder = (String) argsHash.get("outputfolder");
		
		File _resfolderpeak = new File(resfolderpeak);
		File _resfolderloss = new File(resfolderloss);
		File _paramfolder = new File(paramfolder);

		File[] resultFilesPeak = _resfolderpeak.listFiles();
		File[] resultFilesLoss = _resfolderloss.listFiles();
		File[] paramFiles = _paramfolder.listFiles();

		ArrayList<ProcessThread> threads = new ArrayList<ProcessThread>();

		for (int i = 0; i < paramFiles.length; i++) {
			String id = paramFiles[i].getName().split("\\.")[0];
			int resultFileIDPeak = -1;
			for (int j = 0; j < resultFilesPeak.length; j++) {
				if (resultFilesPeak[j].getName().startsWith(id + "_peak.")) {
					resultFileIDPeak = j;
					break;
				}
			}
			if (resultFileIDPeak == -1) {
				System.err.println(id + " not found as peak result.");
				continue;
			}
			int resultFileIDLoss = -1;
			for (int j = 0; j < resultFilesLoss.length; j++) {
				if (resultFilesLoss[j].getName().startsWith(id + "_loss.")) {
					resultFileIDLoss = j;
					break;
				}
			}
			if (resultFileIDLoss == -1) {
				System.err.println(id + " not found as loss result.");
				continue;
			}

			Settings settings = getSettings(paramFiles[i].getAbsolutePath());

			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resultFilesPeak[resultFileIDPeak].getAbsolutePath());
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME + "_PEAK", resultFilesPeak[resultFileIDPeak].getAbsolutePath());
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME + "_LOSS", resultFilesLoss[resultFileIDLoss].getAbsolutePath());

			SettingsChecker sc = new SettingsChecker();
			if (!sc.check(settings)) {
				System.err.println("Error checking settings for " + id);
				continue;
			}

			ProcessThread thread = new RankFromResultFilePeakLossThreadFP().new ProcessThread(
					settings, outputfile, resfolderpeak, resfolderloss, paramFiles[i].getAbsolutePath(), outputtype, negScores, weights,
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

		java.util.ArrayList<Integer> weightIndexesOfInterestPos = new java.util.ArrayList<Integer>();
		java.util.ArrayList<Integer> weightIndexesOfInterestNeg = new java.util.ArrayList<Integer>();
	
		for(int i = 0; i < weights.length; i++) {
			weightIndexesOfInterestPos.add(i);
			weightIndexesOfInterestNeg.add(i);
		}
		java.util.ArrayList<Integer> nextWeightIndexesOfInterestPos;
		java.util.ArrayList<Integer> nextWeightIndexesOfInterestNeg;
		
		int[] bestRanksPos = new int[10];
		int bestWeightIndexPos = -1;
		int[] bestRanksNeg = new int[10];
		int bestWeightIndexNeg = -1;
		for (int i = 1; i <= 10; i++) {
			// for pos queries
			int currentBestValuePos = 0;
			nextWeightIndexesOfInterestPos = new java.util.ArrayList<Integer>();
			for (int w = 0; w < weightIndexesOfInterestPos.size(); w++) {
				int thisBestValuePos = 0;
				for (ProcessThread thread : threads) {
					// get rank of query in thread obtained by using weight w
					if (thread.isPositiveQuery() && thread.getRanksForWeight()[weightIndexesOfInterestPos.get(w)] <= i)
						thisBestValuePos++;
				}
				if(currentBestValuePos < thisBestValuePos) {
					currentBestValuePos = thisBestValuePos;
					nextWeightIndexesOfInterestPos.clear();
					nextWeightIndexesOfInterestPos.add(weightIndexesOfInterestPos.get(w));
				} else if(currentBestValuePos == thisBestValuePos) nextWeightIndexesOfInterestPos.add(weightIndexesOfInterestPos.get(w));
			}
			bestRanksPos[i - 1] = currentBestValuePos;
			bestWeightIndexPos = nextWeightIndexesOfInterestPos.get(0);
			weightIndexesOfInterestPos = nextWeightIndexesOfInterestPos;
			// for neg queries
			int currentBestValueNeg = 0;
			nextWeightIndexesOfInterestNeg = new java.util.ArrayList<Integer>();
			for (int w = 0; w < weightIndexesOfInterestNeg.size(); w++) {
				int thisBestValueNeg = 0;
				for (ProcessThread thread : threads) {
					// get rank of query in thread obtained by using weight w
					if (!thread.isPositiveQuery() && thread.getRanksForWeight()[weightIndexesOfInterestNeg.get(w)] <= i)
						thisBestValueNeg++;
				}
				if(currentBestValueNeg < thisBestValueNeg) {
					currentBestValueNeg = thisBestValueNeg;
					nextWeightIndexesOfInterestNeg.clear();
					nextWeightIndexesOfInterestNeg.add(weightIndexesOfInterestNeg.get(w));
				} else if(currentBestValueNeg == thisBestValueNeg) nextWeightIndexesOfInterestNeg.add(weightIndexesOfInterestNeg.get(w));
			}
			bestRanksNeg[i - 1] = currentBestValueNeg;
			bestWeightIndexNeg = nextWeightIndexesOfInterestNeg.get(0);
			weightIndexesOfInterestNeg = nextWeightIndexesOfInterestNeg;
		}
		String bestWeightStringPos = weights[bestWeightIndexPos][0] + "";
		String bestWeightStringNeg = weights[bestWeightIndexNeg][0] + "";
		for(int i = 1; i < weights[bestWeightIndexPos].length; i++) {
			bestWeightStringPos += " " + weights[bestWeightIndexPos][i];
			bestWeightStringNeg += " " + weights[bestWeightIndexNeg][i];
		}
		String bestRankingsStringPos = bestRanksPos[0] + "";
		String bestRankingsStringNeg = bestRanksNeg[0] + "";
		for(int i = 1; i < bestRanksPos.length; i++) {
			bestRankingsStringPos += " " + bestRanksPos[i];
			bestRankingsStringNeg += " " + bestRanksNeg[i];
		}
		
		File folder = new File(outputfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "best_values");
		if(!folder.exists()) folder.mkdirs();
		String fileprefix = resfolderpeak.replaceAll(".*scores\\/", "").replaceAll("\\/.*", "");
		fileprefix = fileprefix.replaceAll("\\.", "");
		fileprefix += "_" + resfolderloss.replaceAll(".*scores\\/", "").replaceAll("\\/.*", "").replaceAll("\\.", "");
		
		String path_pos = folder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "pos" + Constants.OS_SPECIFIC_FILE_SEPARATOR;
		String path_neg = folder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "neg" + Constants.OS_SPECIFIC_FILE_SEPARATOR;
		new File(path_pos).mkdirs();
		new File(path_neg).mkdirs();
		
		java.io.BufferedWriter bwriter_pos1 = new java.io.BufferedWriter(new java.io.FileWriter(new File(path_pos + Constants.OS_SPECIFIC_FILE_SEPARATOR + fileprefix + "_weights.txt")));
		java.io.BufferedWriter bwriter_pos2 = new java.io.BufferedWriter(new java.io.FileWriter(new File(path_pos + Constants.OS_SPECIFIC_FILE_SEPARATOR + fileprefix + "_tops.txt")));
		java.io.BufferedWriter bwriter_neg1 = new java.io.BufferedWriter(new java.io.FileWriter(new File(path_neg + Constants.OS_SPECIFIC_FILE_SEPARATOR + fileprefix + "_weights.txt")));
		java.io.BufferedWriter bwriter_neg2 = new java.io.BufferedWriter(new java.io.FileWriter(new File(path_neg + Constants.OS_SPECIFIC_FILE_SEPARATOR + fileprefix + "_tops.txt")));
		
		bwriter_pos1.write(bestWeightStringPos);
		bwriter_pos1.newLine();
		bwriter_pos2.write(bestRankingsStringPos);
		bwriter_pos2.newLine();
		
		bwriter_pos1.close();
		bwriter_pos2.close();
		
		bwriter_neg1.write(bestWeightStringNeg);
		bwriter_neg1.newLine();
		bwriter_neg2.write(bestRankingsStringNeg);
		bwriter_neg2.newLine();

		bwriter_neg1.close();
		bwriter_neg2.close();
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

	public static synchronized void increaseNumberFinished(String name) {
		numberFinished++;
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
		protected String resultsfolderpeak;
		protected String resultsfolderloss;
		protected String paramFile;
		protected String outputtype;
		protected boolean negScores;
		protected double[][] weights;
		protected String[] scorenames;
		protected boolean tranformscores;
		protected boolean stdout;
		protected int[] ranks_for_weight;
		protected boolean ispositivequery;

		/**
		 * 
		 * @param settings
		 * @param outputFolder
		 */
		public ProcessThread(Settings settings, String outputfile, String resultsfolderpeak, String resultsfolderloss, String paramFile,
				String outputtype, boolean negScores, double[][] weights, boolean tranformscores, String[] scorenames,
				boolean stdout) {
			this.settings = settings;
			this.outputfile = outputfile;
			this.resultsfolderpeak = resultsfolderpeak;
			this.resultsfolderloss = resultsfolderloss;
			this.paramFile = paramFile;
			this.outputtype = outputtype;
			this.negScores = negScores;
			this.weights = weights;
			this.scorenames = scorenames;
			this.tranformscores = tranformscores;
			this.stdout = stdout;
			if (this.paramFile.contains("-01."))
				this.ispositivequery = true;
			else
				this.ispositivequery = false;
		}

		/**
		 * 
		 */
		public void run() {
			String dbFilenamepeak = (String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME + "_PEAK");
			String dbFilenameloss = (String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME + "_LOSS");
			CandidateList candidatespeak = this.getCandidateListFromFile(this.settings, dbFilenamepeak);
			CandidateList candidatesloss = this.getCandidateListFromFile(this.settings, dbFilenameloss);
			
			if(candidatespeak.getNumberElements() != candidatesloss.getNumberElements()) {
				System.err.println("Exception: candidate list peak and loss differ in size " + candidatespeak.getNumberElements() + " " + candidatesloss.getNumberElements());
				return;
			}
			
			try {
				this.addLossScores(candidatespeak, candidatesloss);
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
			System.err.println(dbFilenamepeak.replaceAll(".*/", "") + ": Read " + candidatespeak.getNumberElements() + " candidates");
			
			String correctInChIKey1 = "";
			try {
				correctInChIKey1 = this.getInChIKey();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			GetRankOfCandidateList grocl = new GetRankOfCandidateList(candidatespeak, dbFilenamepeak, correctInChIKey1,
					this.outputfile, weights, this.outputtype, this.negScores, this.tranformscores, this.scorenames,
					this.stdout);
			this.ranks_for_weight = grocl.run_simple();

			increaseNumberFinished(this.paramFile);
		}

		public CandidateList getCandidateListFromFile(Settings settings, String filename) {
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filename);
			IDatabase db = null;
			if (filename.endsWith("psv"))
				db = new LocalPSVDatabase(settings);
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
				return null;
			}
			return candidates;
		}
		
		public void addLossScores(CandidateList candidatespeak, CandidateList candidatesloss) throws Exception {
			for(int i = 0; i < candidatespeak.getNumberElements(); i++) {
				String idpeak = candidatespeak.getElement(i).getIdentifier();
				boolean found = false;
				for(int j = 0; j < candidatesloss.getNumberElements(); j++) {
					String idloss = candidatesloss.getElement(i).getIdentifier();
					if(idpeak.equals(idloss)) {
						found = true;
						candidatespeak.getElement(i).setProperty("AutomatedLossFingerprintAnnotationScore", candidatesloss.getElement(i).getProperty("AutomatedLossFingerprintAnnotationScore"));
						break;
					}
				}
				if(!found) throw new Exception(idpeak + " not found in loss candidate list");
			}
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
		
		protected Double findMatchingLossMass(Object[] masses, Double mass) {
			Double lastMassFound = null;
			Double lastDevFound = null;
			for (int i = 0; i < masses.length; i++) {
				Double currentMass = (Double) masses[i];
				Double currentDev = Math.abs(mass - currentMass);
				if (lastDevFound == null || lastDevFound > currentDev) {
					lastMassFound = currentMass;
					lastDevFound = currentDev;
				} else
					break;
			}
			return lastMassFound;
		}
	}
}
