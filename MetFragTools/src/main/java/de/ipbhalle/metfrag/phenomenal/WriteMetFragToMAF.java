package de.ipbhalle.metfrag.phenomenal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import edu.emory.mathcs.backport.java.util.Arrays;

public class WriteMetFragToMAF {

	public static java.util.Hashtable<String, String> argsHash;
	
	public static void main(String[] args) {
		if(!getArgs(args)) {
			System.err.println("Error reading parameters.");
			System.exit(1);
		}
		
		String metfragFolder = argsHash.get("metfragFolder");
		String paramFolderName = argsHash.get("paramFolder");
		int numberCandidates = Integer.parseInt(argsHash.get("numberCandidates"));
		String output = argsHash.get("output");
		double mzdev = Double.parseDouble(argsHash.get("mzdev"));
		double rtdev = Double.parseDouble(argsHash.get("rtdev"));
		String mzmlfilename = null;
		if(argsHash.containsKey("mzmlfilename")) mzmlfilename = (String)argsHash.get("mzmlfilename");
		
		// check metfrag result folder
		File resultFolder = new File(metfragFolder);
		if(!resultFolder.exists()) {
			System.err.println(resultFolder.getAbsolutePath() + " not found.");
			System.exit(2);
		}
		// get files to convert to mztab
		File[] files = resultFolder.listFiles();

		// check metfrag param folder
		File paramFolder = new File(paramFolderName);
		if(!paramFolder.exists()) {
			System.err.println(paramFolder.getAbsolutePath() + " not found.");
			System.exit(2);
		}
		// get files to convert to mztab
		File[] paramFiles = paramFolder.listFiles();
		
		CombinedFeatureList combinedFeatures = new WriteMetFragToMAF().new CombinedFeatureList();

		double[] scoreWeights = null;
		String[] scoreNames = null;
		
		for(int i = 0; i < files.length; i++) {
			
			double rt = 0.0f;
			double mz = 0.0;
			double intensity = 0.0f;
			File paramFile = getMatchingParamFile(paramFiles, files[i]);
			boolean isPositive = true;
			int adductType = 1;
			if(paramFile != null) {
				try {
					adductType = getAdductType(paramFile);
					isPositive = getChargeType(paramFile, adductType);
					// get weights and scores
					scoreNames = getScoreNames(paramFile);
					scoreWeights = getScoreWeights(paramFile, scoreNames);
				} catch (Exception e) {
					System.err.println("Error getting adduct/charge information for " + paramFile.getName() + ". Skipping.");
					e.printStackTrace();
					continue;
				}
				if(scoreNames == null || scoreWeights == null) {
					System.err.println("Could not find valid score names and weights for " + files[i].getName() + ". Skipping.");
					continue;
				}
			} else {
				System.err.println("Found no param file for " + files[i].getName() + ". Skipping.");
				continue;
			}
			
			try {
				String name = getSampleName(paramFile).substring(0, files[i].getName().lastIndexOf('.'));
				String[] tmp = name.split("_");
				if(tmp.length == 1) throw new Exception();
				rt = Double.parseDouble(tmp[1]);
				mz = Double.parseDouble(tmp[2]);
				intensity = Double.parseDouble(tmp[3]);
				if(tmp.length == 5) mzmlfilename = generateFileName(tmp);
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println(files[i].getName() + " has no rt and mz information. Check file name. Skipping...");
				continue;
			}
			
			if(mzmlfilename == null) mzmlfilename = "unknown";
			Feature newFeature = new WriteMetFragToMAF().new Feature(mz, rt, intensity, adductType, isPositive, mzmlfilename, files[i]);
			// calculate mass to charge value from neutral mass
			newFeature.adaptMassToChargeValue();
			// include new feature into combined list
			combinedFeatures.addMzSorted(newFeature, mzdev, rtdev);
		}
		
		Object[] filenameArrayObject = combinedFeatures.getFilenames().toArray();
		String[] filenameArray = new String[filenameArrayObject.length];
		for(int i = 0; i < filenameArray.length; i++) {
			filenameArray[i] = (String)filenameArrayObject[i];
		}
		
		Arrays.sort(filenameArray);

		ArrayList<String> lines = combinedFeatures.getLines(filenameArray, numberCandidates, scoreNames, scoreWeights);
		
		// write MAF file
		try {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output)));
			// write header
			String header = "database_identifier\tchemical_formula\tsmiles\tinchi\tmetabolite_identification\tmass_to_charge"
					+ "\tfragmentation\tmodifications\tcharge\tretention_time\ttaxid\tspecies\tdatabase\tdatabase_version"
					+ "\treliability\turi\tsearch_engine\tsearch_engine_score\tComment[scores]\tComment[weights]\tsmallmolecule_abundance_sub"
					+ "\tsmallmolecule_abundance_stdev_sub\tsmallmolecule_abundance_std_error_sub";
			for(String filename : filenameArray) {
				int index = filename.lastIndexOf('.');
				if(index != -1) header += "\t" + filename.substring(0, index);
				else header += "\t" + filename;
			}
			bwriter.write(header);
			bwriter.newLine();
			for(String line : lines) {
				bwriter.write(line);
				bwriter.newLine();
			}
			bwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	
	}

	public static String generateFileName(String[] strings) {
		if(strings.length < 5) return "";
		String name = strings[4];
		for(int i = 5; i < strings.length; i++) {
			name += "_" + strings[i];
		}
		return name;
	}
	
	/**
	 * 
	 * @param args
	 * @return
	 */
	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("paramFolder") && !tmp[0].equals("metfragFolder") && !tmp[0].equals("numberCandidates") && !tmp[0].equals("output")
					&& !tmp[0].equals("rtdev") && !tmp[0].equals("mzdev") && !tmp[0].equals("mzmlfilename")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("metfragFolder")) {
			System.err.println("no metfragFolder defined");
			return false;
		}

		if (!argsHash.containsKey("paramFolder")) {
			System.err.println("no paramFolder defined");
			return false;
		}

		if (!argsHash.containsKey("numberCandidates")) {
			System.err.println("no numberCandidates defined");
			return false;
		}

		if (!argsHash.containsKey("output")) {
			System.err.println("no output defined");
			return false;
		}

		if (!argsHash.containsKey("rtdev")) {
			System.err.println("no rtdev defined");
			return false;
		}

		if (!argsHash.containsKey("mzdev")) {
			System.err.println("no mzdev defined");
			return false;
		}
		
		return true;
	}

	public static File getMatchingParamFile(File[] paramFiles, File resultsFile) {
		String resultID = resultsFile.getName().substring(0, resultsFile.getName().lastIndexOf('.'));
		for(File paramFile : paramFiles) {
			String paramID = paramFile.getName().substring(0, paramFile.getName().lastIndexOf('.'));
			if(paramID.equals(resultID)) return paramFile;
		}
		return null;
	}

	/**
	 * 
	 * @param paramFile
	 * @return
	 * @throws Exception
	 */
	public static String getSampleName(File paramFile) throws Exception {
		String sampleName = "";
		BufferedReader breader = new BufferedReader(new FileReader(paramFile));
		String line = breader.readLine();
		if(line == null) {
			breader.close();
			throw new Exception("No line in parameter file " + paramFile.getName());
		}
		breader.close();
		String[] tmp = line.split("\\s+");
		for(String string : tmp) {
			string = string.trim();
			if(string.startsWith("SampleName")) sampleName = string.split("=")[1].trim().replaceAll(".*\\/", "");
		}
		return sampleName;
	}

	/**
	 * 
	 * @param paramFile
	 * @return
	 * @throws Exception
	 */
	public static int getAdductType(File paramFile) throws Exception {
		String adductType = "1";
		BufferedReader breader = new BufferedReader(new FileReader(paramFile));
		String line = breader.readLine();
		if(line == null) {
			breader.close();
			throw new Exception("No line in parameter file " + paramFile.getName());
		}
		breader.close();
		String[] tmp = line.split("\\s+");
		for(String string : tmp) {
			string = string.trim();
			if(string.startsWith("PrecursorIonMode")) adductType = string.split("=")[1].trim();
		}
		try {
			return Integer.parseInt(adductType);
		} catch(Exception e) {
			
		}
		return Constants.getIonisationNominalMassByType(adductType);
	}

	/**
	 * 
	 * @param paramFile
	 * @return
	 * @throws Exception
	 */
	public static double[] getScoreWeights(File paramFile, String[] scoreNames) throws Exception {
		double[] scoreWeights = null;
		BufferedReader breader = new BufferedReader(new FileReader(paramFile));
		String line = breader.readLine();
		if(line == null) {
			breader.close();
			throw new Exception("No line in parameter file " + paramFile.getName());
		}
		breader.close();
		String[] tmp = line.split("\\s+");
		String[] scoreWeightsString = null;
		for(String string : tmp) {
			string = string.trim();
			if(string.startsWith("MetFragScoreWeights")) {
				scoreWeightsString = string.split("=")[1].trim().split(",");
			}
		}
		if(scoreWeights == null && scoreNames != null) {
			scoreWeights = new double[scoreNames.length];
			for(int i = 0; i < scoreNames.length; i++) {
				scoreWeights[i] = 1.0;
			}
			return scoreWeights;
		}
		scoreWeights = new double[scoreWeightsString.length];
		for(int i = 0; i < scoreWeightsString.length; i++) {
			scoreWeights[i] = Double.parseDouble(scoreWeightsString[i].trim());
		}
		return scoreWeights;
	}

	/**
	 * 
	 * @param paramFile
	 * @return
	 * @throws Exception
	 */
	public static String[] getScoreNames(File paramFile) throws Exception {
		String[] scoreNames = null;
		BufferedReader breader = new BufferedReader(new FileReader(paramFile));
		String line = breader.readLine();
		if(line == null) {
			breader.close();
			throw new Exception("No line in parameter file " + paramFile.getName());
		}
		breader.close();
		String[] tmp = line.split("\\s+");
		for(String string : tmp) {
			string = string.trim();
			if(string.startsWith("MetFragScoreTypes")) scoreNames = string.split("=")[1].trim().split(",");
		}
		return scoreNames;
	}
	
	/**
	 * 
	 * @param paramFile
	 * @param adductType
	 * @return
	 * @throws Exception
	 */
	public static boolean getChargeType(File paramFile, int adductType) throws Exception {
		String chargeType = "true";
		BufferedReader breader = new BufferedReader(new FileReader(paramFile));
		String line = breader.readLine();
		boolean chargeTypeFound = false;
		if(line == null) {
			breader.close();
			throw new Exception("No line in parameter file " + paramFile.getName());
		}
		breader.close();
		String[] tmp = line.split("\\s+");
		for(String string : tmp) {
			string = string.trim();
			if(string.startsWith("IsPositiveIonMode")) {
				chargeType = string.split("=")[1].trim().toLowerCase();
				chargeTypeFound = true;
			}
		}
		if(!chargeTypeFound) {
			return Constants.getIonisationChargeByNominalMassDifference(adductType);
		}
		return chargeType.equals("true") ? true : false;
	}
	
	public static double calcMassToChargeRatio(double neuMass, int adductType, boolean charge) {
		return neuMass + Constants.getIonisationTypeMassCorrection(adductType, charge);
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class CombinedFeatureList {
		private ArrayList<CombinedFeature> combinedFeatures;
		
		public CombinedFeatureList() {
			this.combinedFeatures = new ArrayList<CombinedFeature>();
		}
		
		public int getSize() {
			return this.combinedFeatures.size();
		}
		
		public CombinedFeature get(int i) {
			return this.combinedFeatures.get(i);
		}

		public void add(CombinedFeature combinedFeature) {
			this.combinedFeatures.add(combinedFeature);
		}

		public void addMzSorted(Feature feature, double mzdev, double rtdev) {
			int index = 0;
			while(index < this.combinedFeatures.size()) {
				double mz = this.combinedFeatures.get(index).getConsensusMZ();
				// check mz values
				if(feature.getMz() > mz) {
					index++;
				} else {
					break;
				}
			}
			if(this.combinedFeatures.size() > index && this.combinedFeatures.get(index).equals(feature, mzdev, rtdev)) {
				this.combinedFeatures.get(index).addFeature(feature);
				return;
			} else if(index > 0 && this.combinedFeatures.get(index - 1).equals(feature, mzdev, rtdev)) {
				this.combinedFeatures.get(index - 1).addFeature(feature);
				return;
			}
			this.combinedFeatures.add(index, new CombinedFeature(feature));
		}
		
		/**
		 * 
		 * @param filenameArray
		 * @param numberCandidatesPerFeature
		 * @param scoreNames
		 * @param weights
		 * @return
		 */
		public ArrayList<String> getLines(String[] filenameArray, int numberCandidatesPerFeature, String[] scoreNames, double[] weights) {
			ArrayList<String> allLines = new ArrayList<String>();
			for(int i = 0; i < this.combinedFeatures.size(); i++) {
				ArrayList<String> currentLines = this.combinedFeatures.get(i).getLines(filenameArray, numberCandidatesPerFeature, scoreNames, weights);
				for(int l = 0; l < currentLines.size(); l++) {
					allLines.add(currentLines.get(l));
				}
			}
			return allLines;
		}
		
		/**
		 * 
		 * @return
		 */
		public ArrayList<String> getFilenames() {
			ArrayList<String> filenames = new ArrayList<String>();
			for(int i = 0; i < this.combinedFeatures.size(); i++) {
				String[] _filenames = this.combinedFeatures.get(i).getFileNames();
				for(int k = 0; k < _filenames.length; k++) {
					if(!filenames.contains(_filenames[k])) 
						filenames.add(_filenames[k]);
				}
			}
			return filenames;
		}
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class Feature {
		
		private double mz;
		private double rt;
		private double intensity;
		private int adductType;
		private boolean isPositive;
		private String filename;
		private File mf_origin;
		
		public Feature(double mz, double rt, double intensity, int adductType, boolean isPositive, String filename, File mf_origin) {
			this.mz = mz;
			this.intensity = intensity;
			this.rt = rt;
			this.isPositive = isPositive;
			this.adductType = adductType;
			this.filename = filename;
			this.mf_origin = mf_origin;
		}
		
		public void adaptMassToChargeValue() {
			this.mz = this.mz + Constants.getIonisationMassByNominalMassDifference(this.adductType) + Constants.getChargeMassByType(this.isPositive);
		}
		
		public File getMf_origin() {
			return mf_origin;
		}

		public void setMf_origin(File mf_origin) {
			this.mf_origin = mf_origin;
		}
			
		public int getAdductType() {
			return adductType;
		}

		public void setAdductType(int adductType) {
			this.adductType = adductType;
		}

		public boolean isPositive() {
			return isPositive;
		}

		public void setPositive(boolean isPositive) {
			this.isPositive = isPositive;
		}

		public double getMz() {
			return mz;
		}

		public void setMz(double mz) {
			this.mz = mz;
		}

		public double getRt() {
			return rt;
		}

		public void setRt(double rt) {
			this.rt = rt;
		}

		public double getIntensity() {
			return intensity;
		}

		public void setIntensity(double intensity) {
			this.intensity = intensity;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class CombinedFeature {

		private ArrayList<Feature> features;
		private double consensusMZ;
		private double consensusRT;
		
		public CombinedFeature(Feature feature) {
			this.features = new ArrayList<Feature>();
			this.consensusMZ = feature.getMz();
			this.consensusRT = feature.getRt();
			this.features.add(feature);
		}
		
		public boolean equals(Feature feature, double mzdev, double rtdev) {
			if(((this.consensusMZ - mzdev) <= feature.getMz() && feature.getMz() <= (this.consensusMZ + mzdev)) 
					&& ((this.consensusRT - rtdev) <= feature.getRt() && feature.getRt() <= (this.consensusRT + rtdev))) {
				return true;
			}
			return false;
		}
		
		public void addFeature(Feature feature) {
			this.features.add(feature);
			this.consensusMZ = this.calcConsensusMZ();
			this.consensusRT = this.calcConsensusRT();
		}
		
		public double getConsensusMZ() {
			return consensusMZ;
		}

		public void setConsensusMZ(double consensusMZ) {
			this.consensusMZ = consensusMZ;
		}

		public double getConsensusRT() {
			return consensusRT;
		}

		public void setConsensusRT(double consensusRT) {
			this.consensusRT = consensusRT;
		}

		public double calcConsensusRT() {
			double val = 0.0;
			for(int i = 0; i < this.features.size(); i++)
				val += this.features.get(i).getRt();
			return val / (double)this.features.size();
		}
		
		public double calcConsensusMZ() {
			double val = 0.0;
			for(int i = 0; i < this.features.size(); i++)
				val += this.features.get(i).getMz();
			return val / (double)this.features.size();
		}
		
		/**
		 * 
		 * @param filenameArray
		 * @param numberCandidatesPerFeature
		 * @param scoreNames
		 * @param weights
		 * @return
		 */
		public ArrayList<String> getLines(String[] filenameArray, int numberCandidatesPerFeature, String[] scoreNames, double[] weights) {
			MetFragGlobalSettings settings = new MetFragGlobalSettings();
			CandidateList candidates = new CandidateList();
			HashMap<String, Integer> idToOccurence = new HashMap<String, Integer>();
			HashMap<String, ICandidate> idToCandidate = new HashMap<String, ICandidate>();
			
			ArrayList<String> lines = new ArrayList<String>();
			for(int i = 0; i < this.features.size(); i++) {
				settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, this.features.get(i).getMf_origin().getAbsolutePath());
				LocalCSVDatabase db = new LocalCSVDatabase(settings);
	
				ArrayList<String> identifiers = null;
				try {
					identifiers = db.getCandidateIdentifiers();
				} catch (MultipleHeadersFoundInInputDatabaseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(identifiers.size() == 0) continue;
				CandidateList currentCandidates = db.getCandidateByIdentifier(identifiers);
				// combine candidates and sum the single scoring values
				for(int k = 0; k < currentCandidates.getNumberElements(); k++) {
					currentCandidates.getElement(k).setProperty("AdductTypeMAF", this.features.get(i).getAdductType());
					currentCandidates.getElement(k).setProperty("ChargeTypeMAF", this.features.get(i).isPositive());
					if(!idToOccurence.containsKey(currentCandidates.getElement(k).getIdentifier())) {
						idToOccurence.put(currentCandidates.getElement(k).getIdentifier(), 1);
						idToCandidate.put(currentCandidates.getElement(k).getIdentifier(), currentCandidates.getElement(k));
					} else {
						idToOccurence.put(currentCandidates.getElement(k).getIdentifier(), idToOccurence.get(currentCandidates.getElement(k).getIdentifier()) + 1);
						for(int l = 0; l < scoreNames.length; l++) {
							double score = convertScore(idToCandidate.get(currentCandidates.getElement(k).getIdentifier()).getProperty(scoreNames[l]));
							score += Double.parseDouble((String)currentCandidates.getElement(k).getProperty(scoreNames[l]));
							idToCandidate.get(currentCandidates.getElement(k).getIdentifier()).setProperty(scoreNames[l], score);
						}
					}
				}
				// calculate mean value of the single scoring terms
				for(int k = 0; k < currentCandidates.getNumberElements(); k++) {
					String currentIdentifier = currentCandidates.getElement(k).getIdentifier();
					int occurence = idToOccurence.get(currentIdentifier);
					for(int l = 0; l < scoreNames.length; l++) {
						double score = convertScore(idToCandidate.get(currentIdentifier).getProperty(scoreNames[l]));
						score /= (double)occurence;
						idToCandidate.get(currentCandidates.getElement(k).getIdentifier()).setProperty(scoreNames[l], score);
					}
				}
			}
			// fill combined candidate list 
			java.util.Iterator<String> it = idToCandidate.keySet().iterator();
			while(it.hasNext()) {
				candidates.addElement(idToCandidate.get(it.next()));
			}
			
			// generate output lines for MAF file
			ArrayList<ICandidate> sortedCandidates = getSortedCandidates(candidates, scoreNames, weights);
			ArrayList<String> addedInChIKeys = new ArrayList<String>();
			int index = 0;

			while(index < numberCandidatesPerFeature && index < sortedCandidates.size()) {
				ICandidate currentCandidate = sortedCandidates.get(index);
				String currentInChIKey = (String)currentCandidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
				if(!addedInChIKeys.contains(currentInChIKey)) {
					addedInChIKeys.add(currentInChIKey);
					String currentCandidateLine = currentCandidate.getIdentifier(); // identifier
					String adductType = Constants.getIonisationTypeByNominalMassDifference((Integer)currentCandidate.getProperty("AdductTypeMAF"));
					String charge = (Boolean)currentCandidate.getProperty("ChargeTypeMAF") ? "pos" : "neg";
					if(currentCandidate.getProperty(VariableNames.MOLECULAR_FORMULA_NAME) != null) 
						currentCandidateLine += "\t" + (String)currentCandidate.getProperty(VariableNames.MOLECULAR_FORMULA_NAME); // molecular formula
					else 
						currentCandidateLine += "\t";
					if(currentCandidate.getProperty(VariableNames.SMILES_NAME) != null) 
						currentCandidateLine += "\t" + (String)currentCandidate.getProperty(VariableNames.SMILES_NAME); // smiles
					else 
						currentCandidateLine += "\t";
					if(currentCandidate.getProperty(VariableNames.INCHI_NAME) != null) 
						currentCandidateLine += "\t" + (String)currentCandidate.getProperty(VariableNames.INCHI_NAME); // inchi
					else 
						currentCandidateLine += "\t";
					currentCandidateLine += "\t";													// metabolite_identification
					currentCandidateLine += "\t" + MathTools.round(this.consensusMZ); 			// mass_to_charge
					currentCandidateLine += "\t";													// fragmentation 
					currentCandidateLine += "\t" + adductType;										// modifications
					currentCandidateLine += "\t" + charge;	  										// charge
					currentCandidateLine += "\t" + MathTools.round(this.consensusRT);			// mass_to_charge
					currentCandidateLine += "\t\t\t\t\t\t"; 										// taxid species database database_version
																									// reliability uri 
					currentCandidateLine += "\tMetFrag";											// search_engine
					currentCandidateLine += "\t" + currentCandidate.getProperty("Score");			// search_engine_score
					currentCandidateLine += "\t" + currentCandidate.getProperty("CommentScores");	// commentScores
					currentCandidateLine += "\t" + currentCandidate.getProperty("CommentWeights");	// commentWeights
					currentCandidateLine += "\t\t\t";											// smallmolecule_abundance_sub smallmolecule_abundance_stdev_sub
																									// smallmolecule_abundance_std_error_sub
					// add file names
					for(int k = 0; k < filenameArray.length; k++) {
						boolean found = false;
						for(int l = 0; l < this.features.size(); l++) {
							if(filenameArray[k].compareTo(this.features.get(l).getFilename()) == 0) {
								found = true;
								currentCandidateLine += "\t" + this.features.get(l).getIntensity();
								break;
							}	
						}
						if(!found) currentCandidateLine += "\t" + "0";
					}
					lines.add(currentCandidateLine);
					index++;
				}
			}
			return lines;
		}
		
		/**
		 * 
		 * @param numberCandidates
		 * @param candidates
		 * @param scores
		 * @param weights
		 * @return
		 */
		private ArrayList<ICandidate> getSortedCandidates(CandidateList candidates, String[] scores, double[] weights) {
			double[] maxScores = new double[scores.length];
			ArrayList<ICandidate> candidatesSorted = new ArrayList<ICandidate>();
			for(int i = 0; i < candidates.getNumberElements(); i++) {
				for(int j = 0; j < scores.length; j++) {
					double currentScore = (Double)candidates.getElement(i).getProperty(scores[j]);
					if(maxScores[j] < currentScore)
						maxScores[j] = currentScore;
				}
			}
			for(int i = 0; i < candidates.getNumberElements(); i++) {
				String scoresComment = "";
				String weightsComment = "";
				double candidateConsensusScore = 0.0;
				for(int j = 0; j < scores.length; j++) {
					double currentScore = (Double)candidates.getElement(i).getProperty(scores[j]);
					candidateConsensusScore += (currentScore / maxScores[j]) * weights[j];
					scoresComment += scores[j] + ": " + MathTools.round(currentScore, 2) + ";";
					weightsComment += "w" + (j+1) + ": " + MathTools.round(weights[j], 2) + ";";
				}
				candidates.getElement(i).setProperty("Score", candidateConsensusScore);
				candidates.getElement(i).setProperty("CommentScores", scoresComment.substring(0, scoresComment.length() - 1));
				candidates.getElement(i).setProperty("CommentWeights", weightsComment.substring(0, weightsComment.length() - 1));
				int index = 0;
				while(index < candidatesSorted.size()) {
					double currentCandidateScore = (Double)candidatesSorted.get(index).getProperty("Score");
					if(candidateConsensusScore < currentCandidateScore)	index++;
					else break;
						
				}
				candidatesSorted.add(index, candidates.getElement(i));
			}
			return candidatesSorted;
		}
		
		/**
		 * 
		 * @return
		 */
		public String[] getFileNames() {
			String[] filenames = new String[this.features.size()];
			for(int i = 0; i < filenames.length; i++) {
				filenames[i] = this.features.get(i).getFilename();
			}
			return filenames;
		}
		
		private Double convertScore(Object scoreObject) {
			double score = 0.0;
			try {
				score = (Double)scoreObject;
			} catch(Exception e) {
				score = Double.parseDouble((String)scoreObject);
			}
			return score;
		}
	}
	
}
