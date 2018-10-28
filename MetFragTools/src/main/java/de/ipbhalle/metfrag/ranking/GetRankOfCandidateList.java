package de.ipbhalle.metfrag.ranking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class GetRankOfCandidateList {
	public ArrayList<String> scorePropertyNamesForLog = new ArrayList<String>();
	public ArrayList<String> scorePropertyNamesForTakeRawValue = new ArrayList<String>();
	public String delimiter = "\\|";
	public String[] combinedReferenceScoreValues;
	public boolean outputSortedList = false;
	public java.util.Hashtable<String, String> argsHash;
	public ArrayList<String> scoresToTransform;
	public boolean transformScores = false;
	public boolean negScores = false;
	public double[][] weights;
	public String[] scoringPropertyNames;
	public int numberFinished = 0;
	public boolean stdout = false;
	public String outputtype = "rank"; // or "list"

	protected String correctInChIKey1;
	protected String csv;
	protected String outputFile;
	protected GroupedCandidate correctGroup;
	protected CandidateList candidates;

	protected java.util.Hashtable<String, GroupedCandidate> groupedCandidates;
	protected java.util.Hashtable<String, Double> scorenameToMaximum;

	public GetRankOfCandidateList() {
		
	}
	
	public GetRankOfCandidateList(CandidateList candidateList, String csv, String correctInChIKey1, String outputfile,
			double[][] weights, String outputtype, boolean negScores, boolean transformScores, String[] scorenames, boolean stdout) {
		this.candidates = candidateList;
		this.weights = weights;
		this.correctInChIKey1 = correctInChIKey1;
		this.csv = csv;
		this.outputFile = outputfile;
		this.outputtype = outputtype;
		this.negScores = negScores;
		
		this.transformScores = transformScores;
		this.scoringPropertyNames = scorenames;
		this.stdout = stdout;
		this.prepare();
	}

	protected void prepare() {
		this.scoresToTransform = new ArrayList<String>();
		this.scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore");
		this.scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore1");
		this.scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore2");
		this.scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore3");
		this.scoresToTransform.add("AutomatedFingerprintSubstructureAnnotationScore4");
		this.scoresToTransform.add("AutomatedPeakFingerprintAnnotationScore");
		this.scoresToTransform.add("AutomatedLossFingerprintAnnotationScore");
	}
	
	public String toString() {
		return this.csv + "\n" + this.correctInChIKey1 + "\n" + this.outputFile;
	}

	public void run() {
		this.groupedCandidates = new java.util.Hashtable<String, GroupedCandidate>();
		this.scorenameToMaximum = new java.util.Hashtable<String, Double>();

		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, this.csv);

		for (String scoreName : scoringPropertyNames)
			this.scorenameToMaximum.put(scoreName, 0.0);
		
		for (int i = 0; i < this.candidates.getNumberElements(); i++) {
			String currentInChIKey1 = (String) this.candidates.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);
			double[] scores = new double[scoringPropertyNames.length];
			// get score and check for maximum
			for (int k = 0; k < scores.length; k++) {
				try {
					Object score = this.candidates.getElement(i).getProperty(this.scoringPropertyNames[k]);
					if(score.getClass().getName().equals("java.lang.String"))
						scores[k] = Double.parseDouble((String) score);
					else if(score.getClass().getName().equals("java.lang.Double"))
						scores[k] = (Double) score;
					else if(score.getClass().getName().equals("java.lang.Integer"))
						scores[k] = (Integer) score;
				} catch (Exception e) {
					System.err.println("error reading candidate " + currentInChIKey1);
					System.err.println("could not get property " + k + " " + this.scoringPropertyNames[k]);
					e.printStackTrace();
				}
				if (transformScores && scoresToTransform.contains(this.scoringPropertyNames[k])) {
					if (scores[k] != 0.0)
						scores[k] = 1.0 / (Math.abs(Math.log(scores[k])));
				} else if (this.negScores && this.scoresToTransform.contains(this.scoringPropertyNames[k])) {
					if (scores[k] != 0.0) {
						scores[k] = 1.0 / (Math.abs(scores[k]));
						this.candidates.getElement(i).setProperty(scoringPropertyNames[k], scores[k]);
					}
				}
				if (scores[k] > this.scorenameToMaximum.get(this.scoringPropertyNames[k]))
					this.scorenameToMaximum.put(this.scoringPropertyNames[k], scores[k]);
			}
			// group candidates by inchikey
			ICandidate curCandidate = this.candidates.getElement(i);
			if (this.groupedCandidates.containsKey(currentInChIKey1)) {
				// add to existing
				this.groupedCandidates.get(currentInChIKey1).add(
						(String) curCandidate.getProperty(VariableNames.IDENTIFIER_NAME), scores, i,
						(String) curCandidate.getProperty(VariableNames.INCHI_NAME),
						(String) curCandidate.getProperty(VariableNames.SMILES_NAME));
			} else {
				// create new
				GroupedCandidate gc = new GetRankOfCandidateList().new GroupedCandidate(
						currentInChIKey1);
				if (currentInChIKey1.equals(this.correctInChIKey1))
					this.correctGroup = gc;
				gc.add((String) curCandidate.getProperty(VariableNames.IDENTIFIER_NAME), scores, i,
						(String) curCandidate.getProperty(VariableNames.INCHI_NAME),
						(String) curCandidate.getProperty(VariableNames.SMILES_NAME));
				this.groupedCandidates.put(currentInChIKey1, gc);
			}
		}
		// if correct not found
		if (this.correctGroup == null && !this.outputtype.equals("list")) {
			System.err.println("inchikey1=" + this.correctInChIKey1 + " not found in " + this.csv);
			return;
		}

		// store maximal scores
		double[] maximumscores = new double[this.scoringPropertyNames.length];

		for (int i = 0; i < this.scoringPropertyNames.length; i++) {
			maximumscores[i] = this.scorenameToMaximum.get(this.scoringPropertyNames[i]);
		}

		StringBuilder[] outputString = new StringBuilder[this.weights.length];
		if (!this.outputtype.equals("list")) {
			for (int w = 0; w < this.weights.length; w++) {

				int rank = 0;
				double wc = 0;
				double bc = 0;

				// get final score of correct candidate
				double correctFinalScore = this.correctGroup.getBestMaximumScore(this.weights[w], maximumscores);
				java.util.Enumeration<String> keys = this.groupedCandidates.keys();

				// calculate ranking values
				while (keys.hasMoreElements()) {
					GroupedCandidate currentGroup = this.groupedCandidates.get(keys.nextElement());
					double currentScore = currentGroup.getBestMaximumScore(this.weights[w], maximumscores);
					if (currentScore >= correctFinalScore)
						rank++;
					if (currentScore > correctFinalScore)
						bc++;
					if (currentScore < correctFinalScore)
						wc++;
				}
				// calculate RRP of correct
				double rrp = getRRP(bc, wc, this.groupedCandidates.size());
				int indexOfCorrect = this.correctGroup.getBestIndex();

				// define output values
				String values = this.scoringPropertyNames[0] + "="
						+ this.candidates.getElement(indexOfCorrect).getProperty(this.scoringPropertyNames[0]) + "";
				String maximalValues = "Max" + this.scoringPropertyNames[0] + "="
						+ this.scorenameToMaximum.get(this.scoringPropertyNames[0]);
				for (int i = 1; i < this.scoringPropertyNames.length; i++) {
					values += " " + this.scoringPropertyNames[i] + "="
							+ this.candidates.getElement(indexOfCorrect).getProperty(this.scoringPropertyNames[i]);
					maximalValues += " Max" + this.scoringPropertyNames[i] + "="
							+ this.scorenameToMaximum.get(this.scoringPropertyNames[i]);
				}

				String weightString = "weights=" + this.weights[w][0];
				for (int i = 1; i < this.weights[w].length; i++)
					weightString += "," + this.weights[w][i];

				outputString[w] = new StringBuilder();
				outputString[w].append(new File(this.csv).getName());
				outputString[w].append(" ");
				outputString[w].append(this.correctInChIKey1);
				outputString[w].append(" ");
				outputString[w].append(rank);
				outputString[w].append(" ");
				outputString[w].append(this.groupedCandidates.size());
				outputString[w].append(" ");
				outputString[w].append(this.correctGroup.getBestIdentifier());
				outputString[w].append(" ");
				outputString[w].append(this.candidates.getElement(indexOfCorrect).getProperty("NoExplPeaks"));
				outputString[w].append(" ");
				outputString[w].append(this.candidates.getElement(indexOfCorrect).getProperty("NumberPeaksUsed"));
				outputString[w].append(" ");
				outputString[w].append(rrp);
				outputString[w].append(" ");
				outputString[w].append(bc);
				outputString[w].append(" ");
				outputString[w].append(wc);
				outputString[w].append(" ");
				outputString[w].append(values);
				outputString[w].append(" ");
				outputString[w].append(maximalValues);
				outputString[w].append(" ");
				outputString[w].append(weightString);
				outputString[w].append(" ");
				outputString[w].append(this.getAvgRank(rank, (int) bc));
				outputString[w].append(" ");
				outputString[w].append("rank");
				
			}
		}

		if (!stdout && outputtype.equals("rank")) {
			java.io.BufferedWriter bwriter = null;
			try {
				bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(this.outputFile)));
				for (StringBuilder string : outputString) {
					bwriter.write(string.toString());
					bwriter.newLine();
				}
				bwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (stdout && outputtype.equals("rank")) {
			for (StringBuilder string : outputString) {
				System.out.println(string.toString());
			}
		} else if (outputtype.equals("list")) {
			java.util.Enumeration<String> keys = groupedCandidates.keys();

			java.io.BufferedWriter bwriter = null;
			try {
				bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(this.outputFile)));
				while (keys.hasMoreElements()) {
					String curKey = keys.nextElement();
					GroupedCandidate currentGroup = groupedCandidates.get(curKey);
					currentGroup.getBestMaximumScore(weights[0], maximumscores);
					String candidateLine = currentGroup.getBestSmiles() + " "
							+ currentGroup.getCalculatedBestMaximumScore() + " " + curKey;
					bwriter.write(candidateLine);
					bwriter.newLine();
				}
				bwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public int[] run_simple() {
		this.groupedCandidates = new java.util.Hashtable<String, GroupedCandidate>();
		this.scorenameToMaximum = new java.util.Hashtable<String, Double>();

		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, this.csv);

		for (String scoreName : scoringPropertyNames)
			this.scorenameToMaximum.put(scoreName, 0.0);
		
		for (int i = 0; i < this.candidates.getNumberElements(); i++) {
			String currentInChIKey1 = (String) this.candidates.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);
			double[] scores = new double[scoringPropertyNames.length];
			// get score and check for maximum
			for (int k = 0; k < scores.length; k++) {
				try {
					Object score = this.candidates.getElement(i).getProperty(this.scoringPropertyNames[k]);
					if(score.getClass().getName().equals("java.lang.String"))
						scores[k] = Double.parseDouble((String) score);
					else if(score.getClass().getName().equals("java.lang.Double"))
						scores[k] = (Double) score;
					else if(score.getClass().getName().equals("java.lang.Integer"))
						scores[k] = (Integer) score;
				} catch (Exception e) {
					System.err.println("error reading candidate " + currentInChIKey1);
					System.err.println("could not get property " + k + " " + this.scoringPropertyNames[k]);
					e.printStackTrace();
				}
				if (transformScores && scoresToTransform.contains(this.scoringPropertyNames[k])) {
					if (scores[k] != 0.0)
						scores[k] = 1.0 / (Math.abs(Math.log(scores[k])));
				} else if (this.negScores && this.scoresToTransform.contains(this.scoringPropertyNames[k])) {
					if (scores[k] != 0.0) {
						scores[k] = 1.0 / (Math.abs(scores[k]));
						this.candidates.getElement(i).setProperty(scoringPropertyNames[k], scores[k]);
					}
				}
				if (scores[k] > this.scorenameToMaximum.get(this.scoringPropertyNames[k]))
					this.scorenameToMaximum.put(this.scoringPropertyNames[k], scores[k]);
			}
			// group candidates by inchikey
			ICandidate curCandidate = this.candidates.getElement(i);
			if (this.groupedCandidates.containsKey(currentInChIKey1)) {
				// add to existing
				this.groupedCandidates.get(currentInChIKey1).add(
						(String) curCandidate.getProperty(VariableNames.IDENTIFIER_NAME), scores, i,
						(String) curCandidate.getProperty(VariableNames.INCHI_NAME),
						(String) curCandidate.getProperty(VariableNames.SMILES_NAME));
			} else {
				// create new
				GroupedCandidate gc = new GetRankOfCandidateList().new GroupedCandidate(
						currentInChIKey1);
				if (currentInChIKey1.equals(this.correctInChIKey1))
					this.correctGroup = gc;
				gc.add((String) curCandidate.getProperty(VariableNames.IDENTIFIER_NAME), scores, i,
						(String) curCandidate.getProperty(VariableNames.INCHI_NAME),
						(String) curCandidate.getProperty(VariableNames.SMILES_NAME));
				this.groupedCandidates.put(currentInChIKey1, gc);
			}
		}
		// if correct not found
		if (this.correctGroup == null && !this.outputtype.equals("list")) {
			System.err.println("inchikey1=" + this.correctInChIKey1 + " not found in " + this.csv);
			return null;
		}
		
		// store maximal scores
		double[] maximumscores = new double[this.scoringPropertyNames.length];

		for (int i = 0; i < this.scoringPropertyNames.length; i++) {
			maximumscores[i] = this.scorenameToMaximum.get(this.scoringPropertyNames[i]);
		}
		
		int[] ranks = new int[this.weights.length];
		for (int w = 0; w < this.weights.length; w++) {

			int rank = 0;
			// get final score of correct candidate
			double correctFinalScore = this.correctGroup.getBestMaximumScore(this.weights[w], maximumscores);
			java.util.Enumeration<String> keys = this.groupedCandidates.keys();

			// calculate ranking values
			while (keys.hasMoreElements()) {
				GroupedCandidate currentGroup = this.groupedCandidates.get(keys.nextElement());
				double currentScore = currentGroup.getBestMaximumScore(this.weights[w], maximumscores);
				if (currentScore >= correctFinalScore)
					rank++;
			}
			ranks[w] = rank;
		}
		return ranks;
	}
	
	/**
	 * 
	 * @param bc
	 * @param wc
	 * @param numCandidates
	 * @return
	 */
	public static double getRRP(double bc, double wc, int numCandidates) {
		if (numCandidates == 1)
			return 1;
		return 0.5 * (1.0 - (bc - wc) / (double) ((numCandidates - 1)));
	}

	public double getAvgRank(int rank, int bc) {
		// mean((as.numeric(x[1]) - (as.numeric(x[1])-as.numeric(x[2])) +
		// 1):(as.numeric(x[1]))))
		int start = rank - (rank - bc) + 1;
		int number_values = 0;
		double value = 0.0;
		for (int i = start; i <= rank; i++) {
			number_values++;
			value += i;
		}
		return value / (double) number_values;
	}
	
	class GroupedCandidate {
		public String inchikey1;
		public ArrayList<String> identifiers;
		public ArrayList<String> inchis;
		public ArrayList<String> smiles;
		public ArrayList<Integer> indexes;
		public ArrayList<double[]> scores;
		public int bestIndex = 0;
		public double bestMaximumScore;

		public GroupedCandidate(String inchikey1) {
			this.inchikey1 = inchikey1;
			this.identifiers = new ArrayList<String>();
			this.indexes = new ArrayList<Integer>();
			this.smiles = new ArrayList<String>();
			this.inchis = new ArrayList<String>();
			this.scores = new ArrayList<double[]>();
		}

		public ArrayList<double[]> getScores() {
			return this.scores;
		}
		
		public String getBestIdentifier() {
			return this.identifiers.get(this.bestIndex);
		}

		public String getBestInChI() {
			return this.inchis.get(this.bestIndex);
		}

		public String getBestSmiles() {
			return this.smiles.get(this.bestIndex);
		}
		
		public int getBestIndex() {
			return this.indexes.get(this.bestIndex);
		}

		public void add(String identifier, double[] scores, int index, String inchi, String smiles) {
			this.scores.add(scores);
			identifiers.add(identifier);
			indexes.add(index);
			this.smiles.add(smiles);
			this.inchis.add(inchi);
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
			this.bestMaximumScore = maxvalue;
			return maxvalue;
		}
		
		public double getCalculatedBestMaximumScore() {
			return this.bestMaximumScore;
		}
	}
}
