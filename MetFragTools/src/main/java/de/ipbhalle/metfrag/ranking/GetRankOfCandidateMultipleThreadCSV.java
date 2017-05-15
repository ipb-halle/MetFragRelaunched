package de.ipbhalle.metfrag.ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class GetRankOfCandidateMultipleThreadCSV {

	public static Vector<String> scorePropertyNamesForLog = new Vector<String>();
	public static Vector<String> scorePropertyNamesForTakeRawValue = new Vector<String>();
	public static String delimiter = "\\|";
	public static String[] combinedReferenceScoreValues;
	public static boolean outputSortedList = false;
	public static java.util.Hashtable<String, String> argsHash;
	public static Vector<String> scoresToTransform;
	public static boolean transformScores = false;
	public static double[][] weights;
	public static String[] scoringPropertyNames;
	public static int numberFinished = 0;
	
	static {
		scoresToTransform = new Vector<String>();
		scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore");
		scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore1");
		scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore2");
		scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore3");
		scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore4");
	}

	public static synchronized void increaseNumberFinished () {
		numberFinished++;
		System.out.println("finished " + numberFinished);
	}
	
	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("csv") && !tmp[0].equals("param") && !tmp[0].equals("scorenames") 
					&& !tmp[0].equals("threads") && !tmp[0].equals("output")
					&& !tmp[0].equals("weights") && !tmp[0].equals("transform")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("csv")) {
			System.err.println("no csv defined");
			return false;
		}
		if (!argsHash.containsKey("threads")) {
			System.err.println("no threads defined");
			return false;
		}
		if (!argsHash.containsKey("param")) {
			System.err.println("no param defined");
			return false;
		}
		if (!argsHash.containsKey("scorenames")) {
			System.err.println("no scorenames defined");
			return false;
		}
		if (!argsHash.containsKey("output")) {
			System.err.println("no output defined");
			return false;
		}
		if (!argsHash.containsKey("weights")) {
			System.err.println("no weights defined");
			return false;
		}
		if (!argsHash.containsKey("transform")) {
			argsHash.put("transform", "false");
		}
		//csv psv auto
		if (!argsHash.containsKey("type")) {
			argsHash.put("type", "csv");
		}
		
		return true;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		boolean argCorrect = getArgs(args);
		if (!argCorrect) {
			System.err.println(
					"run: progname csv='csv' param='param folder' threads='number threads' output='output folder' scorenames='score1[,score2,...]' weights='weightfile' [transform=false]");
			System.exit(1);
		}
		String csv = argsHash.get("csv");
		String param = argsHash.get("param");
		String scorenames = argsHash.get("scorenames");
		String output = argsHash.get("output");
		String type = argsHash.get("type");
		int numberThreads = Integer.parseInt(argsHash.get("threads"));
		
		transformScores = Boolean.parseBoolean(argsHash.get("transform"));
		scoringPropertyNames = scorenames.split(",");
		weights = readWeights(argsHash.get("weights"));
		java.util.HashMap<String, String> csvToInChIKey = parseInChIKeys(csv, param);
		java.util.Iterator<?> it = csvToInChIKey.keySet().iterator();
		
		ArrayList<ProcessingThread> threads = new ArrayList<ProcessingThread>();
		
		while(it.hasNext()) {
			String csvFile = (String)it.next();
			String id = new File(csvFile).getName().split("\\.")[0];
			String inchikey1 = csvToInChIKey.get(csvFile);
			ProcessingThread thread = 
					new GetRankOfCandidateMultipleThreadCSV().new ProcessingThread(csvFile, inchikey1, output + "/" + id + ".txt", type);
			threads.add(thread);
		}
		System.out.println("preparation finished");
		
		ExecutorService executer = Executors.newFixedThreadPool(numberThreads);
		for(ProcessingThread thread : threads) {
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

	public static java.util.HashMap<String, String> parseInChIKeys(String csv, String param) throws IOException {

		File _resfolder = new File(csv);
		File _paramfolder = new File(param);

		File[] resultFiles = _resfolder.listFiles();
		File[] paramFiles = _paramfolder.listFiles();
		
		java.util.HashMap<String, String> csvToInChIKey = new java.util.HashMap<String, String>();
		
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
			BufferedReader breader = new BufferedReader(new FileReader(paramFiles[paramFileID]));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.matches("# [A-Z]*")) {
					csvToInChIKey.put(resultFiles[i].getAbsolutePath(), line.split("\\s+")[1]);
					break;
				}
			}
			breader.close();
		}
		
		return csvToInChIKey;
	}
	
	/**
	 * 
	 * @param bc
	 * @param wc
	 * @param numCandidates
	 * @return
	 */
	public static double getRRP(double bc, double wc, int numCandidates) {
		return 0.5 * (1.0 - (bc - wc) / (double) ((numCandidates - 1)));
	}

	public static int addSorted(java.util.Vector<Double> scores, double score) {
		int index = 0;
		while (index < scores.size() && score < scores.get(index)) {
			index++;
		}
		scores.add(index, score);
		return index;
	}

	/**
	 * read weights from file
	 * 
	 * @param weightsfile
	 * @return
	 * @throws IOException
	 */
	public static double[][] readWeights(String weightsfile) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(new File(weightsfile)));
		Vector<double[]> weights = new Vector<double[]>();
		String line = "";
		while((line = breader.readLine()) != null) {
			line = line.trim();
			String[] tmp = line.split("\\s+");
			double[] doubleweights = new double[tmp.length];
			for(int i = 0; i < tmp.length; i++)
				doubleweights[i] = Double.parseDouble(tmp[i]);
			weights.add(doubleweights);
		}
		breader.close();
		double[][] weightmatrix = new double[weights.size()][weights.get(0).length];
		for(int i = 0; i < weights.size(); i++) {
			for(int k = 0; k < weights.get(0).length; k++) {
				weightmatrix[i][k] = weights.get(i)[k];
			}
		}
		return weightmatrix;
	}
	
	/**
	 * grouping of candidate
	 * 
	 * @author cruttkie
	 *
	 */
	class GroupedCandidate {
		public String inchikey1;
		public Vector<String> identifiers;
		public Vector<Integer> indexes;
		public Vector<double[]> scores;
		public int bestIndex = 0;

		public GroupedCandidate(String inchikey1) {
			this.inchikey1 = inchikey1;
			this.identifiers = new Vector<String>();
			this.indexes = new Vector<Integer>();
			this.scores = new Vector<double[]>();
		}

		public String getBestIdentifier() {
			return this.identifiers.get(this.bestIndex);
		}

		public int getBestIndex() {
			return this.indexes.get(this.bestIndex);
		}
		
		public void add(String identifier, double[] scores, int index) {
			this.scores.add(scores);
			identifiers.add(identifier);
			indexes.add(index);
		}

		public double getBestMaximumScore(double[] weights, double[] maximumscores) {
			double maxvalue = 0.0;
			for (int k = 0; k < scores.size(); k++) {
				double value = 0.0;
				for (int i = 0; i < weights.length; i++) {
					double maxScore = maximumscores[i] == 0 ? 1.0 : maximumscores[i];
					value += weights[i] * (scores.get(k)[i] / maxScore);
				}
				if (maxvalue < value) {
					this.bestIndex = k;
					maxvalue = value;
				}
			}
			return maxvalue;
		}
	}
	
	class ProcessingThread extends Thread {
		protected String correctInChIKey1;
		protected String csv;
		protected String outputFile;
		protected GroupedCandidate correctGroup;
		protected String fileType = "csv";
		
		protected java.util.Hashtable<String, GroupedCandidate> groupedCandidates;
		protected java.util.Hashtable<String, Double> scorenameToMaximum;
		
		public ProcessingThread(String csv, String correctInChIKey1, String outputfile, String fileType) {
			this.correctInChIKey1 = correctInChIKey1;
			this.csv = csv;
			this.outputFile = outputfile;
			this.fileType = fileType;
		}
		
		public String toString() {
			return this.csv + "\n" + this.correctInChIKey1 + "\n" + this.outputFile;
		}
		
		public void run() {
			this.groupedCandidates = new java.util.Hashtable<String, GroupedCandidate>();
			this.scorenameToMaximum = new java.util.Hashtable<String, Double>();
			
			MetFragGlobalSettings settings = new MetFragGlobalSettings();
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, this.csv);
			IDatabase db = null;
			if(csv == "csv") db = new LocalCSVDatabase(settings);
			else if (csv.equals("auto")) {
				if(this.csv.endsWith("psv")) db = new LocalPSVDatabase(settings);
				else db = new LocalCSVDatabase(settings);
			}
			else db = new LocalCSVDatabase(settings);
			Vector<String> identifiers = null;
			try {
				identifiers = db.getCandidateIdentifiers();
			} catch (MultipleHeadersFoundInInputDatabaseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			CandidateList candidates = null;
			try {
				candidates = db.getCandidateByIdentifier(identifiers);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for (String scoreName : scoringPropertyNames)
				this.scorenameToMaximum.put(scoreName, 0.0);

			for (int i = 0; i < candidates.getNumberElements(); i++) {
				String currentInChIKey1 = (String) candidates.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);

				double[] scores = new double[scoringPropertyNames.length];
				// get score and check for maximum
				for (int k = 0; k < scores.length; k++) {
					scores[k] = Double.parseDouble((String) candidates.getElement(i).getProperty(scoringPropertyNames[k]));
					if(transformScores && scoresToTransform.contains(scoringPropertyNames[k])) {
						if(scores[k] != 0.0) scores[k] = 1.0 / (-1.0 * Math.log(scores[k]));
					}
					if (scores[k] > scorenameToMaximum.get(scoringPropertyNames[k]))
						scorenameToMaximum.put(scoringPropertyNames[k], scores[k]);
				}
				// group candidates by inchikey
				if (groupedCandidates.containsKey(currentInChIKey1)) {
					// add to existing
					groupedCandidates.get(currentInChIKey1).add((String) candidates.getElement(i).getProperty(VariableNames.IDENTIFIER_NAME), scores, i);
				} else {
					// create new
					GroupedCandidate gc = new GetRankOfCandidateMultipleThreadCSV().new GroupedCandidate(currentInChIKey1);
					if (currentInChIKey1.equals(correctInChIKey1)) 
						correctGroup = gc;
					gc.add((String) candidates.getElement(i).getProperty(VariableNames.IDENTIFIER_NAME), scores, i);
					groupedCandidates.put(currentInChIKey1, gc);
				}
			}
			// if correct not found
			if (correctGroup == null) {
				System.out.println("inchikey1=" + correctInChIKey1 + " not found in " + csv);
				System.exit(0);
			}
			
			// store maximal scores
			double[] maximumscores = new double[scoringPropertyNames.length];

			for (int i = 0; i < scoringPropertyNames.length; i++) {
				maximumscores[i] = scorenameToMaximum.get(scoringPropertyNames[i]);
			}
			
			String[] outputString = new String[weights.length];
			for(int w = 0; w < weights.length; w++) {
			
				int rank = 0;
				double wc = 0;
				double bc = 0;
		
				// get final score of correct candidate
				double correctFinalScore = correctGroup.getBestMaximumScore(weights[w], maximumscores);
				java.util.Enumeration<String> keys = groupedCandidates.keys();
		
				// calculate ranking values
				while (keys.hasMoreElements()) {
					GroupedCandidate currentGroup = groupedCandidates.get(keys.nextElement());
					double currentScore = currentGroup.getBestMaximumScore(weights[w], maximumscores);
					if (currentScore >= correctFinalScore)
						rank++;
					if (currentScore > correctFinalScore)
						bc++;
					if (currentScore < correctFinalScore)
						wc++;
				}
				// calculate RRP of correct
				double rrp = getRRP(bc, wc, groupedCandidates.size());
				int indexOfCorrect = correctGroup.getBestIndex();
				
				// define output values
				String values = scoringPropertyNames[0] + "="
						+ candidates.getElement(indexOfCorrect).getProperty(scoringPropertyNames[0]) + "";
				String maximalValues = "Max" + scoringPropertyNames[0] + "=" + scorenameToMaximum.get(scoringPropertyNames[0]);
				for (int i = 1; i < scoringPropertyNames.length; i++) {
					values += " " + scoringPropertyNames[i] + "="
							+ candidates.getElement(indexOfCorrect).getProperty(scoringPropertyNames[i]);
					maximalValues += " Max" + scoringPropertyNames[i] + "=" + scorenameToMaximum.get(scoringPropertyNames[i]);
				}
				
				String weightString = "weights=" + weights[w][0];
				for(int i = 1; i < weights[w].length; i++)
					weightString += "," + weights[w][i];
				
				outputString[w] = new File(csv).getName() + " " + correctInChIKey1 + " " + rank + " "
						+ groupedCandidates.size() + " "
						+ correctGroup.getBestIdentifier() + " "
						+ candidates.getElement(indexOfCorrect).getProperty("NoExplPeaks") + " "
						+ candidates.getElement(indexOfCorrect).getProperty("NumberPeaksUsed") + " " + rrp + " " + bc + " " + wc
						+ " " + values + " " + maximalValues + " " + weightString;
			}
			
			java.io.BufferedWriter bwriter = null;
			try {
				bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(this.outputFile)));
				for(String string : outputString) {
					bwriter.write(string);
					bwriter.newLine();
				}
				bwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			increaseNumberFinished();
		}
	}
	
}