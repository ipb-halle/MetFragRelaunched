package de.ipbhalle.metfrag.crossvalidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;

import de.ipbhalle.metfrag.ranking.GetRankOfCandidateCSV;
import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;

public class CrossValidationMain {

	public static int NUMBER_THREADS = 4;
	
	public static void main(String[] args) {
		if(args.length != 7 && args.length != 8) {
			System.out.println("numberFolds numberTrials resultFolder inchikeyFolder massesFolder numberThreads maxIterations [alpha,beta,gamma]");
			System.exit(1);
		}
		File resFolder = new File(args[2]);
		File massFolder = new File(args[4]);
		File inchikeyFolder = new File(args[3]);
		int maxIterations = Integer.parseInt(args[6]);
		double[] paramsStart = null;
		if(args.length == 8) {
			paramsStart = new double[3];
			String[] params = args[7].trim().split(",");
			for(int i = 0; i < params.length; i++) {
				paramsStart[i] = Double.parseDouble(params[i]);
			}
		}
		
		CrossValidationMain cv = new CrossValidationMain();
		
		/*
		 * result files as csv
		 */
		File[] resFiles = cv.sortFiles(resFolder.listFiles());
		/*
		 * the precursor masses for the candidate lists
		 * per res file one mass file
		 */
		File[] massFiles = cv.sortFiles(massFolder.listFiles());
		/*
		 * the inchikey1s of the correct precursors
		 * per res file one inchikey file
		 */
		File[] inchikeyFiles = cv.sortFiles(inchikeyFolder.listFiles());
		
		int numberFolds = Integer.parseInt(args[0]);
		int numberTrials = Integer.parseInt(args[1]);
		NUMBER_THREADS = Integer.parseInt(args[5]);
		if(resFiles.length < numberFolds) {
			System.err.println("Error: number folds: " + numberFolds + "; number files: " + resFiles.length);
			System.exit(1);
		}
		if(massFiles.length != resFiles.length) {
			System.err.println("Error: mass and res files differ. " + massFiles.length + " " + resFiles.length);
			System.err.println("Check " + massFolder + " and " + resFolder);
			System.exit(1);
		}
		if(inchikeyFiles.length != resFiles.length) {
			System.err.println("Error: mass and inchikey files differ." + inchikeyFiles.length + " " + resFiles.length);
			System.exit(1);
		}

		String[] inchikeys = new String[inchikeyFiles.length];
		double[] masses = new double[massFiles.length];
		
		for(int i = 0; i < massFiles.length; i++) {
			BufferedReader breader;
			try {
				breader = new BufferedReader(new FileReader(massFiles[i]));
				masses[i] = Double.parseDouble(breader.readLine().trim());
				breader.close();
				breader = new BufferedReader(new FileReader(inchikeyFiles[i]));
				inchikeys[i] = breader.readLine().trim();
				breader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileData[] fileData = new FileData[resFiles.length];
		double percent = 10.0;
		for(int i = 0; i < fileData.length; i++) {
			fileData[i] = cv.new FileData(resFiles[i], inchikeys[i], masses[i]);
			if(((double)i / (double)(fileData.length - 1)) * 100.0 >= percent) {
				percent = Math.round(((double)i / (double)(fileData.length - 1)) * 100.0);
				System.out.print(percent + "% ");
				percent += 10.0;
			}
		}
		System.out.println();
		ArrayList<int[]> folds = generateFolds(numberFolds, resFiles.length);
		System.out.println("generated " + folds.size() + " folds");
		
		double[][] params = new double[numberFolds][8];
		double[][] qualityValues = new double[numberFolds][5];
		for(int fold = 0; fold < numberFolds; fold++) {
			System.out.println("size: " + folds.get(fold).length);
			TrainSingleTry[] singleTries = new TrainSingleTry[numberTrials];
			for(int i = 0; i < numberTrials; i++) {
				singleTries[i] = cv.new TrainSingleTry(fileData);
				if(paramsStart != null) 
					singleTries[i].setParamsStart(paramsStart);
				singleTries[i].setFoldExcluded(i);
				singleTries[i].setFolds(folds);
				singleTries[i].setMaxIterations(maxIterations);
			}
			
			ExecutorService threadExecutor = Executors.newFixedThreadPool(NUMBER_THREADS);
			for(TrainSingleTry scmfp : singleTries) {
				threadExecutor.execute(scmfp);
			}
	
		    threadExecutor.shutdown(); 
		    while(!threadExecutor.isTerminated())
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		    for(int i = 0; i < singleTries.length; i++) {
		    	//alpha beta gamma numberFirstRanks numberTopFiveRanks numberTopTenRanks meanRank meanRRP
		        double[] values = singleTries[i].getParameters();
		    	if(values[3] > params[fold][3]) params[fold] = values;
				else if(values[3] == params[fold][3] && values[1] > params[fold][4]) params[fold] = values;
				else if(values[3] == params[fold][3] && values[1] == params[fold][4] && values[5] > params[fold][5]) params[fold] = values;
		    }		
			
		    System.out.print("final parameters: ");
			for(int i = 0; i < params[fold].length; i++) {
				System.out.print(params[fold][i] + " ");
			}
			System.out.println();
			
			double[] trainedParams = new double[3];
			for(int i = 0; i < 3; i++) trainedParams[i] = params[fold][i];
			/*
			 * test on the excluded fold
			 */
			double[][] rankingValues = cv.testParams(folds, fileData, fold, trainedParams);
	    	//alpha beta gamma numberFirstRanks numberTopFiveRanks numberTopTenRanks meanRank meanRRP
			qualityValues[fold] = cv.getQualityMeasures(rankingValues);
		}
		for(int i = 0; i < numberFolds; i++) {
			System.out.print("test: ");
			for(int j = 0; j < qualityValues[i].length; j++) {
				System.out.print(qualityValues[i][j] + " ");
			}
			System.out.print(" trained: ");
			for(int j = 0; j < params[i].length; j++) {
				System.out.print(params[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	//0-rank 1-better_candidates 2-equal_candidates 3-worse_candidates 4-total_candidates 5-rrp
	public double[] getQualityMeasures(double[][] rankingValues) {
		int numberFirstRanks = 0;
		int numberTopFiveRanks = 0;
		int numberTopTenRanks = 0;
		double meanRank = 0.0;
		double meanRRP = 0.0;
		for(int i = 0; i < rankingValues.length; i++) {
			if(rankingValues[i][0] == 1) numberFirstRanks++;
			if(rankingValues[i][0] <= 5) numberTopFiveRanks++;
			if(rankingValues[i][0] <= 10) numberTopTenRanks++;
			meanRank += rankingValues[i][0];
			meanRRP += rankingValues[i][5];
		}
		meanRank /= (double)rankingValues.length;
		meanRRP /= (double)rankingValues.length;
		return new double[] {numberFirstRanks, numberTopFiveRanks, numberTopTenRanks, meanRank, meanRRP};
	}
	
	/**
	 * 
	 * @param folds
	 * @param fileData
	 * @param foldIncluded
	 * @return
	 */
	public double[][] testParams(ArrayList<int[]> folds, FileData[] fileData, int foldIncluded, double[] trainedParams) {
		double[][] rankingValues = new double[folds.get(foldIncluded).length][5];
		for(int i = 0; i < folds.get(foldIncluded).length; i++) {
			double[] scores = calculateScores(fileData[folds.get(foldIncluded)[i]], trainedParams);
			rankingValues[i] = GetRankOfCandidateCSV.calculateRankingValues(scores, fileData[folds.get(foldIncluded)[i]].getAllInchikeys(), fileData[folds.get(foldIncluded)[i]].getCorrectInchikey());
			//0-rank 1-better_candidates 2-equal_candidates 3-worse_candidates 4-total_candidates 5-rrp
		}
		return rankingValues;
	}
	
	/**
	 * 
	 * @param numberFolds
	 * @param datasetSize
	 * @return
	 */
	public static ArrayList<int[]> generateFolds(int numberFolds, int datasetSize) {
		Random rand = new Random();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(int i = 0; i < datasetSize; i++) ids.add(i);
		int numberDataPointsPerFold = (int)Math.floor((double)datasetSize / (double)numberFolds);
		ArrayList<int[]> folds = new ArrayList<int[]>();
		
		for (int i = 0; i < numberFolds; i++) {
			int[] indeces = null;
			if(i == numberFolds - 1) {
				indeces = new int[Math.max(numberDataPointsPerFold, ids.size())];
			}
			else {
				indeces = new int[numberDataPointsPerFold];
			}
			for(int k = 0; k < indeces.length; k++) {
				int index = rand.nextInt(ids.size());
				indeces[k] = ids.get(index);
				ids.remove(index);
			}
			folds.add(indeces);
		}
		return folds;
	}
	
	/**
	 * 
	 * @param measuredPrecursorMass
	 * @param intensities
	 * @param masses
	 * @param energies
	 * @param params
	 * @return
	 */
	public double calculateScore(double measuredPrecursorMass, double[] intensities, double[] masses, double[] energies, double[] params) {
		double score = 0.0;
		for(int i = 0; i < intensities.length; i++) {
			score += (Math.pow((masses[i] / measuredPrecursorMass) * 100.0, params[0]) * Math.pow(intensities[i], params[1])) / (Math.pow(energies[i], params[2]));
		}
		return score;
	}
	
	/**
	 * 
	 * @param fileData
	 * @param params
	 * @return
	 */
	public double[] calculateScores(FileData fileData, double[] params) {
		double[] scores = new double[fileData.getNumberEntries()];
		ArrayList<MoleculeData> moleculeEntries = fileData.getMoleculeData();
		for(int i = 0; i < fileData.getNumberEntries(); i++) {
			MoleculeData moleculeData = moleculeEntries.get(i);
			scores[i] = calculateScore(fileData.getMeasuredPrecursorMass(), moleculeData.getIntensities(), moleculeData.getMasses(), moleculeData.getEnergies(), params);
		}
		return scores;
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class MoleculeData {
		
		private double[] intensities;
		private double[] masses;
		private double[] energies;
		private String inchkey1;
		private String identifier;
		
		public MoleculeData(double[] intensities, double[] masses, double[] energies, String inchkey1, String identifier) {
			this.intensities = intensities;
			this.masses = masses;
			this.energies = energies;
			this.identifier = identifier;
			this.inchkey1 = inchkey1;
		}
		
		public double[] getIntensities() {
			return this.intensities;
		}
		
		public double[] getMasses() {
			return this.masses;
		}
		public double[] getEnergies() {
			return this.energies;
		}
		public String getInchikey() {
			return this.inchkey1;
		}
		public String getIdentifier() {
			return this.identifier;
		}
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class FileData {
		
		private ArrayList<MoleculeData> moleculeEntries;
		private String correctInchikey;
		private double measuredPrecursorMass;
		private String fileName;
		
		public FileData(File file, String correctInchikey, double measuredPrecursorMass) {
			this.fileName = file.getName();
			this.correctInchikey = correctInchikey;
			this.measuredPrecursorMass = measuredPrecursorMass;
			this.moleculeEntries = new ArrayList<MoleculeData>(); 
			try {
				BufferedReader breader = new BufferedReader(new FileReader(file));
				String header = breader.readLine();
				String[] tmp = header.split("\\|"); 
				HashMap<String, Integer> colNameToIndex = new HashMap<String, Integer>();			
				for(int i = 0; i < tmp.length; i++) {
					colNameToIndex.put(tmp[i].trim(), i);
				}
				
				String line = "";
				while((line = breader.readLine()) != null) {
					tmp = line.split("\\|");
					String peak_string = tmp[colNameToIndex.get("ExplPeaks")];
					String energy_string = tmp[colNameToIndex.get("FragmenterScore_Values")];
					String[] tmp_peak = peak_string.split(";");
					String[] tmp_energy = energy_string.split(";");
					double[] masses = null;
					double[] intensities = null;
					double[] energies = null;
					if(peak_string.contains("_")) {
						masses = new double[tmp_peak.length];
						intensities = new double[tmp_peak.length];
						energies = new double[tmp_energy.length];
					}
					else {
						masses = new double[0];
						intensities = new double[0];
						energies = new double[0];
					}
					
					for(int i = 0; i < masses.length; i++) {
						String[] tmp2 = tmp_peak[i].split("_");
						masses[i] = Double.parseDouble(tmp2[0]);
						intensities[i] = Double.parseDouble(tmp2[1]);
					}
					for(int i = 0; i < energies.length; i++) {
						energies[i] = Double.parseDouble(tmp_energy[i]);
					}
					
					this.moleculeEntries.add(new MoleculeData(intensities, masses, energies, tmp[colNameToIndex.get("InChIKey1")], tmp[colNameToIndex.get("Identifier")]));
				}
				breader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		public String getFileName() {
			return this.fileName;
		}
		
		/**
		 * 
		 * @return
		 */
		public String[] getAllInchikeys() {
			String[] inchikeys = new String[this.moleculeEntries.size()];
			for(int i = 0; i < this.moleculeEntries.size(); i++)
				inchikeys[i] = this.moleculeEntries.get(i).getInchikey();
			return inchikeys;
		}
		
		public double getMeasuredPrecursorMass() {
			return this.measuredPrecursorMass;
		}

		public int getNumberEntries() {
			return this.moleculeEntries.size();
		}
		
		public String getCorrectInchikey() {
			return this.correctInchikey;
		}
		
		public ArrayList<MoleculeData> getMoleculeData() {
			return this.moleculeEntries;
		}
	}
	
	
	public File[] sortFiles(File[] files) { 
		Arrays.sort(files, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return f1.getName().compareTo(f2.getName());
			} });
		return files;
	}

	/**
	 * 
	 * @author chrisr
	 *
	 */
	public class TrainSingleTry implements Runnable {

		private ArrayList<int[]> folds; 
		private FileData[] fileData;
		private int foldExcluded;
		private int maxIterations;
		private double[] parameters;
		private double[] paramsStart;
		
		public TrainSingleTry(FileData[] fileData) {
			this.fileData = fileData;
		}
		
		public void setParamsStart(double[] paramsStart) {
			this.paramsStart = paramsStart;
		}
		
		@Override
		public void run() {
			int numberDatasetsToTrain = 0;
			if(this.folds.size() != 1) {
				for(int i = 0; i < this.folds.size(); i++)
					if(i != this.foldExcluded)
						numberDatasetsToTrain += this.folds.get(i).length;
			}
			else numberDatasetsToTrain = this.folds.get(0).length;
			int[] indecesTraining = new int[numberDatasetsToTrain];
			int index = 0;
			for(int i = 0; i < this.folds.size(); i++) {
				if(i != this.foldExcluded || this.folds.size() == 1) {
					for(int k = 0; k < this.folds.get(i).length; k++) {
						indecesTraining[index] = this.folds.get(i)[k];
						index++;
					}
				}
			}
			
			/*
			 * set initial parameter values
			 */
			double[] params = null;
	        if(this.paramsStart == null) {
				Random rand = new Random();
		        double add1 = (double)(Math.round(rand.nextDouble() * 100)) / 100.0;
		        double alpha_param_start = (double)rand.nextInt(3) + add1;
		        add1 = (double)(Math.round(rand.nextDouble() * 100)) / 100.0;
		        double beta_param_start = (double)rand.nextInt(3) + add1;
		        add1 = (double)(Math.round(rand.nextDouble() * 100)) / 100.0;
		        double gamma_param_start = (double)rand.nextInt(3) + add1;
	
		        params = new double[]{alpha_param_start, beta_param_start, gamma_param_start};
	        }
	        else {
	        	params = this.paramsStart;
	        }
		    
			double step = 0.1;
	        int iteration = 0;

	        double mean_rank = Integer.MAX_VALUE;
	        double mean_rrp = Integer.MIN_VALUE;
	        double num_first = -1d;
	        double num_topfive = -1d;
	        double num_topten = -1d;
	        
			boolean finished = false;
			do {
				if(iteration != 0 && iteration % 10 == 0) step /= 10.0;
				iteration++;
				boolean improved_global = false;
				for(int t = 0; t < params.length; t++) {
					double[] params_tmp = new double[params.length];
					for(int tt = 0; tt < 2; tt++) {
						for(int i = 0; i < params_tmp.length; i++)
	                        params_tmp[i] = params[i];
						double add = step;
	                    if(tt == 1) add *= -1.0;
	                    params_tmp[t] += add;
	                    double[][] rankingValues = new double[indecesTraining.length][5];
	                    for(int ii = 0; ii < indecesTraining.length; ii++) {
	                    	double[] scores = calculateScores(fileData[indecesTraining[ii]], params_tmp);
	                    	rankingValues[ii] = GetRankOfCandidateCSV.calculateRankingValues(scores, this.fileData[indecesTraining[ii]].getAllInchikeys(), this.fileData[indecesTraining[ii]].getCorrectInchikey());
	                    }
	                    //0-numberFirstRanks, 1-numberTopFiveRanks, 2-numberTopTenRanks, 3-meanRank, 4-meanRRP
	                    double[] qualityMeasures = getQualityMeasures(rankingValues);
	                    
	                    double cur_num_first = qualityMeasures[0];
	                    double cur_num_topfive = qualityMeasures[1];
	                    double cur_num_topten = qualityMeasures[2];
	                    double cur_mean_rrp = qualityMeasures[4];
	                    double cur_mean_rank = qualityMeasures[3];
	                    if(num_first < cur_num_first) {
	                    	 improved_global = true;
	                    	 for(int i = 0; i < params_tmp.length; i++)
	                             params[i] = params_tmp[i];
	                    	 mean_rrp = cur_mean_rrp;
	                         num_first = cur_num_first;
	                         num_topfive = cur_num_topfive;
	                         num_topten = cur_num_topten;
	                         mean_rank = cur_mean_rank;
	                         break;

	                    }
	                    else if(num_first == cur_num_first && num_topfive < cur_num_topfive) {
	                    	improved_global = true;
	                   	 	for(int i = 0; i < params_tmp.length; i++)
	                            params[i] = params_tmp[i];
	                   	 	mean_rrp = cur_mean_rrp;
	                        num_first = cur_num_first;
	                        num_topfive = cur_num_topfive;
	                        num_topten = cur_num_topten;
	                        mean_rank = cur_mean_rank;
	                        break;
	                    }
	                    else if(num_first == cur_num_first && num_topfive == cur_num_topfive && num_topten < cur_num_topten) {
	                    	 improved_global = true;
	                    	 for(int i = 0; i < params_tmp.length; i++)
	                             params[i] = params_tmp[i];
	                    	 mean_rrp = cur_mean_rrp;
	                         num_first = cur_num_first;
	                         num_topfive = cur_num_topfive;
	                         num_topten = cur_num_topten;
	                         mean_rank = cur_mean_rank;
	                         break;
	                    }
					}
				}
				if(!improved_global) finished = true;
			}
			while(!finished || this.maxIterations == iteration);
			//alpha beta gamma numberFirstRanks numberTopFiveRanks numberTopTenRanks meanRank meanRRP
			this.parameters = new double[] {params[0], params[1], params[2], mean_rrp, mean_rank, num_first, num_topfive, num_topten}; 
		}
		
		public double[] getParameters() {
			return this.parameters;
		}
		
		public void setMaxIterations(int maxIterations) {
			this.maxIterations = maxIterations;
		}
		
		public void setFoldExcluded(int foldExcluded) {
			this.foldExcluded = foldExcluded;
		}
		
		public void setFolds(ArrayList<int[]> folds) {
			this.folds = folds;
		}
	}
	
}