package de.ipbhalle.metfraglib.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class GetRankOfCandidateMultipleCSV {

	public static Vector<String> scorePropertyNamesForLog = new Vector<String>();
	public static Vector<String> scorePropertyNamesForTakeRawValue = new Vector<String>();
	public static String delimiter = "\\|";
	public static String[] combinedReferenceScoreValues;
	public static boolean outputSortedList = false;
	public static java.util.Hashtable<String, String> argsHash;
	public static java.util.Hashtable<String, GroupedCandidate> groupedCandidates;
	public static java.util.Hashtable<String, Double> scorenameToMaximum;;

	public static GroupedCandidate correctGroup;

	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("csv") && !tmp[0].equals("inchikey1") && !tmp[0].equals("scorenames")
					&& !tmp[0].equals("weights")) {
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
		if (!argsHash.containsKey("inchikey1")) {
			System.err.println("no inchikey1 defined");
			return false;
		}
		if (!argsHash.containsKey("scorenames")) {
			System.err.println("no scorenames defined");
			return false;
		}
		if (!argsHash.containsKey("weights")) {
			System.err.println("no weights defined");
			return false;
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
					"run: progname csv='csv' inchikey1='inchikey1' scorenames='score1[,score2,...]' weights='weightfile'");
			System.exit(1);
		}
		String csv = argsHash.get("csv");
		String inchikey1 = argsHash.get("inchikey1");
		String scorenames = argsHash.get("scorenames");
		String weightfile = argsHash.get("weights");

		groupedCandidates = new java.util.Hashtable<String, GroupedCandidate>();
		scorenameToMaximum = new java.util.Hashtable<String, Double>();

		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, csv);
		LocalCSVDatabase db = new LocalCSVDatabase(settings);

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
		CandidateList candidates = db.getCandidateByIdentifier(identifiers);

		String[] scoringPropertyNames = scorenames.split(",");
		for (String scoreName : scoringPropertyNames)
			scorenameToMaximum.put(scoreName, 0.0);

		for (int i = 0; i < candidates.getNumberElements(); i++) {
			String currentInChIKey1 = (String) candidates.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);

			double[] scores = new double[scoringPropertyNames.length];
			// get score and check for maximum
			for (int k = 0; k < scores.length; k++) {
				scores[k] = Double.parseDouble((String) candidates.getElement(i).getProperty(scoringPropertyNames[k]));
				if (scores[k] > scorenameToMaximum.get(scoringPropertyNames[k]))
					scorenameToMaximum.put(scoringPropertyNames[k], scores[k]);
			}
			// group candidates by inchikey
			if (groupedCandidates.containsKey(currentInChIKey1)) {
				// add to existing
				groupedCandidates.get(currentInChIKey1).add((String) candidates.getElement(i).getProperty(VariableNames.IDENTIFIER_NAME), scores, i);
			} else {
				// create new
				GroupedCandidate gc = new GetRankOfCandidateMultipleCSV().new GroupedCandidate(currentInChIKey1);
				if (currentInChIKey1.equals(inchikey1)) correctGroup = gc;
				gc.add((String) candidates.getElement(i).getProperty(VariableNames.IDENTIFIER_NAME), scores, i);
				groupedCandidates.put(currentInChIKey1, gc);
			}
		}
		// if correct not found
		if (correctGroup == null) {
			System.out.println("inchikey1=" + inchikey1 + " not found in " + csv);
			System.exit(0);
		}
		
		// store maximal scores
		double[] maximumscores = new double[scoringPropertyNames.length];
		for (int i = 0; i < scoringPropertyNames.length; i++)
			maximumscores[i] = scorenameToMaximum.get(scoringPropertyNames[i]);

		double[][] weights = readWeights(weightfile);
		
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
			
			// print output
			System.out.println(new File(csv).getName() + " " + inchikey1 + " " + rank + " "
					+ groupedCandidates.size() + " "
					+ correctGroup.getBestIdentifier() + " "
					+ candidates.getElement(indexOfCorrect).getProperty("NoExplPeaks") + " "
					+ candidates.getElement(indexOfCorrect).getProperty("NumberPeaksUsed") + " " + rrp + " " + bc + " " + wc
					+ " " + values + " " + maximalValues + " " + weightString);
		}
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
}
