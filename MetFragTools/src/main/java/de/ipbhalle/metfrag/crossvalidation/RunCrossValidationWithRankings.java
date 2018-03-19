package de.ipbhalle.metfrag.crossvalidation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import de.ipbhalle.metfraglib.additionals.MathTools;

public class RunCrossValidationWithRankings {

	public static String folder = "rankings_chemspider_9_1";
	//public static String rankings_folder_name = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/ufz_train_eawag_test/rankings";
	//public static String rankings_folder_name = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/cross_validation/eawag_02_new/rankings_testing_combined";
	public static String rankings_folder_name = "/oldhome/cruttkie/svn/eawag/2016hdx/metfrag_ipb/rankings_chemspider/" + folder + "/pos_smiles";
	//public static String rankings_folder_name = "/scratch/cruttkie/fingerprint_training/cross_validation/ianvs/rankings/testing_all";
	//public static String rankings_folder_name = "/home/chrisr/Dokumente/PhD/Talks/FoSem2016/train_ufz_test_eawag_05_11_2016/rankings";
	public static int number_folds = 10;
	public static int number_queries = 1;
	public static String only_metfrag_filename = "rankings_1001.txt";
	//public static String only_metfrag_filename = "rankings_101.txt";
	//public static String given_folds_filename = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/cross_validation/eawag_02_new/folds.txt";
	public static String given_folds_filename = "/oldhome/cruttkie/svn/eawag/2016hdx/metfrag_ipb/rankings_chemspider/folds_pos_1000.txt";
	//public static String given_folds_filename = null;
	//public static String output_file = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/ufz_train_eawag_test/rankings.txt";
	//public static String output_file = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/cross_validation/eawag_02_new/rankings.txt";
	//public static String output_file = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/casmi_eawag_data/run_2016_12_06/cv_rankings.txt";
	//public static String output_file = "/scratch/cruttkie/fingerprint_training/cross_validation/ianvs/rankings/testing_all.txt";
	public static String output_file = "/oldhome/cruttkie/svn/eawag/2016hdx/metfrag_ipb/rankings_chemspider/" + folder + "/pos_rankings.txt";
	//public static String output_file = "/tmp/test.txt";

	public static double DOUBLE_DEV = 10^-6;
	
	public static boolean use_avg_rank = true;
	
	public static String[] forbidden_filenames = {
		"rankings_1001.txt",
		"rankings_1002.txt",
		"rankings_1003.txt",
		"rankings_1004.txt",
		"rankings_1005.txt",
		"rankings_1006.txt",
		"rankings_1007.txt",
		"rankings_1008.txt",
		"rankings_1009.txt",
		"rankings_1010.txt",
		"rankings_1011.txt",
		"rankings_1012.txt",
		"rankings_1013.txt",
		"rankings_1014.txt",
		"rankings_1015.txt"
	};
	
	/*
	public static String[] forbidden_filenames = {
		"rankings_101.txt",
		"rankings_102.txt",
		"rankings_103.txt",
		"rankings_104.txt",
		"rankings_105.txt",
		"rankings_106.txt",
	};
	*/
	public static void main(String[] args) {
		if(args != null && args.length >= 2) {
			rankings_folder_name = args[0];
			number_folds = Integer.parseInt(args[1]);
			if(args.length == 3) output_file = args[2];
			if(args.length == 4) given_folds_filename = args[3];
		}
		int[] folds = null;
		if(given_folds_filename != null) {
			try {
				BufferedReader breader = new BufferedReader(new FileReader(new File(given_folds_filename)));
				String line = "";
				java.util.ArrayList<Integer> given_folds = new java.util.ArrayList<Integer>();
				while((line = breader.readLine()) != null) {
					String[] tmp = line.split("\\s+");
					given_folds.add(Integer.parseInt(tmp[0].trim()));
				}
				breader.close();
				folds = new int[given_folds.size()];
				for(int i = 0; i < folds.length; i++) folds[i] = given_folds.get(i);
				given_folds = null;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
				
		File rankings_folder = new File(rankings_folder_name);
		
		if(!rankings_folder.exists() || !rankings_folder.isDirectory()) {
			System.err.println("Error: Rankings path is no folder or does not exist.");
			System.exit(1);
		}
		
		String[] all_filenames = rankings_folder.list();
		System.out.println("Found " + all_filenames.length + " ranking files");
		boolean[] forbidden_filenames = new boolean[all_filenames.length];
		
		int number_allowed_files = 0;
		for(int i = 0; i < all_filenames.length; i++) {
			forbidden_filenames[i] = isContainedInForbiddenList(all_filenames[i]);
			if(!forbidden_filenames[i]) number_allowed_files++;
		}
		
		System.out.println(number_allowed_files + " are valid");
		String[] query_names = getEntryNames(all_filenames[0]);
		int[] query_testing_rankings = new int[query_names.length];
		double[] query_testing_avg_rankings = new double[query_names.length];
		int[] query_testing_better_cands = new int[query_names.length];
		int[] query_testing_worse_cands = new int[query_names.length];
		int[] query_testing_total_cands = new int[query_names.length];
		if(folds == null) folds = generateFolds(query_names.length);
		number_queries = folds.length;
		System.out.println("Containing " + folds.length + " queries");
		//matrix contains rankings for each file (folds.length)
		//for each weight combination number_allowed_files
		int[][] rank_matrix = new int[folds.length][number_allowed_files]; 
		double[][] avg_rank_matrix = new double[folds.length][number_allowed_files]; 
		int[][] better_candidates_matrix = new int[folds.length][number_allowed_files]; 
		int[][] worse_candidates_matrix = new int[folds.length][number_allowed_files]; 
		int[][] total_candidates_matrix = new int[folds.length][number_allowed_files]; 
		int[] valid_indexes = new int[number_allowed_files];
		int file_index = 0;
		for(int i = 0; i < forbidden_filenames.length; i++) {
			if(forbidden_filenames[i]) continue;
			try {
				BufferedReader breader = new BufferedReader(new FileReader(new File(rankings_folder_name + "/" + all_filenames[i])));
				valid_indexes[file_index] = i;
				int row_index = 0;
				String line = "";
				while((line = breader.readLine()) != null) {
					if(line.startsWith("#")) continue;
					String[] tmp = line.split("\\s+");
					try {
						rank_matrix[row_index][file_index] = Integer.parseInt(tmp[2]);
						better_candidates_matrix[row_index][file_index] = (int)Math.round(Double.parseDouble(tmp[8]));
						avg_rank_matrix[row_index][file_index] = getAvgRank(rank_matrix[row_index][file_index], better_candidates_matrix[row_index][file_index]);
						worse_candidates_matrix[row_index][file_index] = (int)Math.round(Double.parseDouble(tmp[9]));
						total_candidates_matrix[row_index][file_index] = (int)Math.round(Double.parseDouble(tmp[3]));
					}
					catch(Exception e) {
						System.err.println("Error at " + row_index + " " + file_index + " " 
								+ rank_matrix.length + " " + rank_matrix[0].length + " " + better_candidates_matrix.length 
								+ " " + better_candidates_matrix[0].length + " " + tmp.length);
						System.err.println(line + " " + all_filenames[i]);
						e.printStackTrace();
						System.exit(1);
					}
					row_index++;
				}
				file_index++;
				breader.close();
			}
			catch(Exception e)  {
				e.printStackTrace();
			}
		}
		
		int check_best_rank_positions = 10;
		//to check: top1, top3, top5, top10
		int[][] test_results = new int[number_folds][check_best_rank_positions];
		int[][] avg_test_results = new int[number_folds][check_best_rank_positions];
		int[] best_weight_index = new int[number_folds];
		int[] number_in_fold = new int[number_folds];
		
		//start validation
		for(int current_fold = 0; current_fold < number_folds; current_fold++) {
			System.out.print("Training Fold " + (current_fold + 1) + " ");
			//only validate on top10
			int[] best_ranking_numbers = new int[check_best_rank_positions];
			
			//over all weights
			for(int weight_index = 0; weight_index < rank_matrix[0].length; weight_index++) {
				int[] ranking_numbers = new int[check_best_rank_positions];
				int[] avg_ranking_numbers = new int[check_best_rank_positions];
				//check all queries/files/spectra
				for(int row_index = 0; row_index < rank_matrix.length; row_index++) {
					//check that current query is not in current fold
					if(current_fold != folds[row_index]) {
						int current_rank = rank_matrix[row_index][weight_index] - 1;
						int current_avg_rank = (int)Math.ceil(MathTools.round(avg_rank_matrix[row_index][weight_index] - 1.0));
						//check whether current rank is greater than ranking check limit
						if(current_rank < check_best_rank_positions) {
							ranking_numbers[current_rank]++;
						}
						if(current_avg_rank < check_best_rank_positions) {
							avg_ranking_numbers[current_avg_rank]++;
						}
					}
				}
				
				int[] reference_rankings_numbers = ranking_numbers;
				if(use_avg_rank) reference_rankings_numbers = avg_ranking_numbers;
				
				//check for best rank
				for(int i = 0; i < check_best_rank_positions; i++) {
					if(best_ranking_numbers[i] < reference_rankings_numbers[i]) {
						//replace values if better
						for(int j = 0; j < check_best_rank_positions; j++) {
							best_ranking_numbers[j] = reference_rankings_numbers[j];
							best_weight_index[current_fold] = weight_index;
						}
					}
					else if(best_ranking_numbers[i] > reference_rankings_numbers[i]) break;
				}
			}
			//when training is finished testing starts
			//again go over current queries only checking those in current fold
			//with best weight combination
			System.out.print("Testing Fold " + (current_fold + 1) + " ");
			
			for(int row_index = 0; row_index < rank_matrix.length; row_index++) {
				//check that current query is in current fold
				if(current_fold != folds[row_index]) continue;
				number_in_fold[current_fold]++;
				int current_rank = rank_matrix[row_index][best_weight_index[current_fold]] - 1;
				double current_avg_rank = MathTools.round(avg_rank_matrix[row_index][best_weight_index[current_fold]] - 1);
				int rounded_current_avg_rank = (int)Math.ceil(current_avg_rank);
				int current_better_cands = better_candidates_matrix[row_index][best_weight_index[current_fold]] - 1;
				int current_worse_cands = worse_candidates_matrix[row_index][best_weight_index[current_fold]] - 1;
				int current_total_cands = total_candidates_matrix[row_index][best_weight_index[current_fold]] - 1;
				query_testing_avg_rankings[row_index] = MathTools.round(current_avg_rank + 1.0);
				query_testing_rankings[row_index] = current_rank + 1;
				query_testing_better_cands[row_index] = current_better_cands + 1;
				query_testing_worse_cands[row_index] = current_worse_cands + 1;
				query_testing_total_cands[row_index] = current_total_cands + 1;
				if(current_rank < check_best_rank_positions)
					test_results[current_fold][current_rank]++;
				if(rounded_current_avg_rank < check_best_rank_positions)
					avg_test_results[current_fold][rounded_current_avg_rank]++;
			}
		}
		System.out.println();
		
		double[] rrps = calculateRelativeRankingPositions(query_testing_better_cands, query_testing_worse_cands, query_testing_total_cands);
		
		if(output_file != null) {
			int[][] metfrag_rankings = getRankings(only_metfrag_filename);
			if(!use_avg_rank) writeTestingSummary(folds, query_testing_rankings, query_testing_better_cands, 
					query_testing_worse_cands, query_testing_total_cands, rrps, query_names, metfrag_rankings);
			else writeTestingSummary(folds, query_testing_avg_rankings, query_testing_better_cands, 
					query_testing_worse_cands, query_testing_total_cands, rrps, query_names, metfrag_rankings);
		}
		
		double[] meanrrps = calculateMeanRelativeRankingPositions(rrps, folds);
		double[] testingErrors = calculateRelativeRankingPositionTestErrors(meanrrps);
		int[] summed_results = new int[check_best_rank_positions];
		
		int[][] reference_test_results = test_results;
		if(use_avg_rank) reference_test_results = avg_test_results;
		for(int i = 0; i < reference_test_results.length; i++) {
			System.out.print("Fold\t" + (i + 1) + ":");
			for(int j = 0; j < reference_test_results[i].length; j++) {
				System.out.print("\t" + reference_test_results[i][j]);
				summed_results[j] += reference_test_results[i][j];
			}
			System.out.println("\t" + meanrrps[i] + "\t" + testingErrors[i] + "\t\t" + "(" + number_in_fold[i] + ")" + "\t" + all_filenames[valid_indexes[best_weight_index[i]]]);
		}
		System.out.println("===================================================================================================");
		
		printRankingValues(summed_results, "Sum:\t", "", calculateMeanValue(meanrrps, 4) + "\t" + calculateMeanValue(testingErrors, 5));
		printCumulativeRankingValues(summed_results, "Sum:\t", "");
		
		printCumulativeRankingPercentages(summed_results, "PercSum:", "");
		
		if(!use_avg_rank) calculateBestWeight(rank_matrix, check_best_rank_positions, valid_indexes, all_filenames);
		else calculateBestWeight(avg_rank_matrix, check_best_rank_positions, valid_indexes, all_filenames);
		
		printRankingsStatistics(only_metfrag_filename, check_best_rank_positions);
	}
	
	/**
	 * 
	 * @param values
	 * @return
	 */
	public static double calculateMeanValue(double[] values, int decimalPlaces) {
		double mean = 0.0;
		for(double val : values) {
			mean += val;
		}
		return MathTools.round(mean / (double)values.length, decimalPlaces);
	}
	
	/**
	 * calculate mean rrps per fold
	 * 
	 * @param rrps
	 * @param folds
	 * @return
	 */
	public static double[] calculateMeanRelativeRankingPositions(double[] rrps, int[] folds) {
		double[] meanValues = new double[number_folds];
		double[] number_queries_per_fold = new double[number_folds];
		//add up rrps per fold
		for(int i = 0; i < number_folds; i++) {
			for(int k = 0; k < folds.length; k++) {
				if(folds[k] == i) {
					meanValues[i] += rrps[k];
					number_queries_per_fold[i] += 1.0;
				}
			}
		}
		//calculate mean rrp per fold
		for(int i = 0; i < number_folds; i++) {
			meanValues[i] = MathTools.round(meanValues[i] / number_queries_per_fold[i], 4);
		}
		return meanValues;
	}
	
	/**
	 * calculate testing errors
	 * 
	 * @param rrps
	 * @param folds
	 * @return
	 */
	public static double[] calculateRelativeRankingPositionTestErrors(double[] meanrrps) {
		double[] added_up_errors = new double[number_folds];
		double[] errors = new double[number_folds];
		for(int i = 0; i < number_folds; i++) {
			for(int k = 0; k < number_folds; k++) {
				if(k != i) added_up_errors[i] += meanrrps[k];
			}
			added_up_errors[i] = added_up_errors[i] / (double)(number_folds - 1);
			errors[i] = MathTools.round(Math.pow(meanrrps[i] - added_up_errors[i], 2.0), 6);
		}
		return errors;
	}
	
	public static double getAvgRank(int rank, int bc) {
		//mean((as.numeric(x[1]) - (as.numeric(x[1])-as.numeric(x[2])) + 1):(as.numeric(x[1]))))
		int start = rank - (rank - bc) + 1;
		int number_values = 0; 
		double value = 0.0;
		for(int i = start; i <= rank; i++) {
			number_values++;
			value += i;
		}
		return value / (double)number_values;
	}
	
	public static void calculateBestWeight(int[][] rank_matrix, int check_best_rank_positions, int[] valid_indexes, String[] file_names) {
		int[] best_ranking_numbers = new int[check_best_rank_positions];
		int best_weight_index = 0;
		
		for(int weight_index = 0; weight_index < rank_matrix[0].length; weight_index++) {
			int[] ranking_numbers = new int[check_best_rank_positions];
			
			//check all queries/files/spectra
			for(int row_index = 0; row_index < rank_matrix.length; row_index++) {
				//check that current query is not in current fold
				int current_rank = rank_matrix[row_index][weight_index] - 1;
				
				//check whether current rank is greater than ranking check limit
				if(current_rank >= check_best_rank_positions) continue;
				ranking_numbers[current_rank]++;
			}
			//check for best rank
			for(int i = 0; i < check_best_rank_positions; i++) {
				if(best_ranking_numbers[i] < ranking_numbers[i]) {
					//replace values if better
					for(int j = 0; j < check_best_rank_positions; j++) {
						best_ranking_numbers[j] = ranking_numbers[j];
						best_weight_index = weight_index;
					}
				}
				else if(best_ranking_numbers[i] > ranking_numbers[i]) break;
			}
		}
		System.out.println("===================================================================================================");
		printRankingValues(best_ranking_numbers, "Complete:", file_names[valid_indexes[best_weight_index]], "");
		printCumulativeRankingValues(best_ranking_numbers, "Complete:", file_names[valid_indexes[best_weight_index]]);
	}

	public static void calculateBestWeight(double[][] avg_rank_matrix, int check_best_rank_positions, int[] valid_indexes, String[] file_names) {
		int[] best_ranking_numbers = new int[check_best_rank_positions];
		int best_weight_index = 0;
		
		for(int weight_index = 0; weight_index < avg_rank_matrix[0].length; weight_index++) {
			int[] ranking_numbers = new int[check_best_rank_positions];
			
			//check all queries/files/spectra
			for(int row_index = 0; row_index < avg_rank_matrix.length; row_index++) {
				//check that current query is not in current fold
				double current_avg_rank = MathTools.round(avg_rank_matrix[row_index][weight_index] - 1, 6);
				int rounded_current_avg_rank = (int)Math.ceil(current_avg_rank);
				
				//check whether current rank is greater than ranking check limit
				if(rounded_current_avg_rank >= check_best_rank_positions) continue;
				ranking_numbers[rounded_current_avg_rank]++;
			}
			//check for best rank
			for(int i = 0; i < check_best_rank_positions; i++) {
				if(best_ranking_numbers[i] < ranking_numbers[i]) {
					//replace values if better
					for(int j = 0; j < check_best_rank_positions; j++) {
						best_ranking_numbers[j] = ranking_numbers[j];
						best_weight_index = weight_index;
					}
				}
				else if(best_ranking_numbers[i] > ranking_numbers[i]) break;
			}
		}
		System.out.println("===================================================================================================");
		printRankingValues(best_ranking_numbers, "Complete:", file_names[valid_indexes[best_weight_index]], "");
		printCumulativeRankingValues(best_ranking_numbers, "Complete:", file_names[valid_indexes[best_weight_index]]);
	}
	
	public static double[] calculateRelativeRankingPositions(int[] better_cands_testing, 
			int[] worse_cands_testing, int[] total_cands_testing) {
		double[] rrps = new double[better_cands_testing.length];
		for(int i = 0; i < rrps.length; i++) {
			if(total_cands_testing[i] == 1) rrps[i] = 0;
			else rrps[i] = 0.5 * (1.0 + (double)(better_cands_testing[i] - worse_cands_testing[i]) / (double)(total_cands_testing[i] - 1));
			rrps[i] = MathTools.round(rrps[i], 5);
		}
		return rrps;
	}
	
	public static boolean isContainedInForbiddenList(String filename) {
		for(int i = 0; i < forbidden_filenames.length; i++) {
			if(filename.equals(forbidden_filenames[i])) return true;
		}
		return false;
	}

	public static String[] getEntryNames(String filename) {
		File file = new File(rankings_folder_name + "/" + filename);
		BufferedReader breader;
		java.util.ArrayList<String> entries = new java.util.ArrayList<String>();
		try {
			breader = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(!line.startsWith("#")) {
					String[] tmp = line.split("\\s+");
					entries.add(tmp[0]);
				}
			}
			breader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] entry_array = new String[entries.size()];
		for(int i = 0; i < entry_array.length; i++) {
			entry_array[i] = entries.get(i);
		}
		return entry_array;
	}
	
	public static int getNumberEntries(String filename) {
		File file = new File(rankings_folder_name + "/" + filename);
		BufferedReader breader;
		int number_lines = 0;
		try {
			breader = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(!line.startsWith("#"))
					number_lines++;
			}
			breader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return number_lines;
	}
	
	public static int[] generateFolds(int number_entries) {
		int[] folds = new int[number_entries];
		java.util.ArrayList<Integer> entry_indexes = new java.util.ArrayList<Integer>();
		for(int i = 0; i < number_entries; i++)
			entry_indexes.add(i);
		Random rand = new Random();
		int current_fold = 0;
		while(!entry_indexes.isEmpty()) {
			int current_index = rand.nextInt(entry_indexes.size());
			folds[entry_indexes.get(current_index)] = current_fold;
			entry_indexes.remove(current_index);
			current_fold++;
			if(current_fold == number_folds) current_fold = 0;
		}
		return folds;
	}
	
	public static int[][] getRankings(String filename) {
		java.util.ArrayList<Integer> rankings = new java.util.ArrayList<Integer>();
		java.util.ArrayList<Integer> betterCandidates = new java.util.ArrayList<Integer>();
		try {
			BufferedReader breader = new BufferedReader(new FileReader(new File(rankings_folder_name + "/" + filename)));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("\\s+");
				int current_rank = Integer.parseInt(tmp[2]);
				int current_better = (int)Math.round((Double.parseDouble(tmp[8])));
				rankings.add(current_rank);
				betterCandidates.add(current_better);
			}
			breader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[][] rankings_arr = new int[rankings.size()][2];
		for(int i = 0; i < rankings_arr.length; i++) {
			rankings_arr[i][0] = rankings.get(i);
			rankings_arr[i][1] = betterCandidates.get(i);
		}
		return rankings_arr;
	}

	public static void printRankingsStatistics(String filename, int check_best_rank_positions) {
		int[] ranking_values = new int[check_best_rank_positions];
		try {
			BufferedReader breader = new BufferedReader(new FileReader(new File(rankings_folder_name + "/" + filename)));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("\\s+");
				int current_rank = Integer.parseInt(tmp[2]) - 1;
				if(use_avg_rank) current_rank = (int)Math.ceil(MathTools.round(getAvgRank(current_rank + 1, (int)Math.round(Double.parseDouble(tmp[8]))) - 1.0, 6));
				if(current_rank >= check_best_rank_positions) continue;
				ranking_values[current_rank]++;
			}
			breader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("===================================================================================================");
		printRankingValues(ranking_values, "Complete:", filename, "");
		printCumulativeRankingValues(ranking_values, "Complete:", filename);
		printCumulativeRankingPercentages(ranking_values, "PercComp:", filename);
	}
	
	public static void writeTestingSummary(int[] folds, int[] rankings_testing, String[] query_names) {
		BufferedWriter bwriter;
		try {
			bwriter = new BufferedWriter(new FileWriter(new File(output_file)));
			for(int i = 0; i < query_names.length; i++) {
				bwriter.write(query_names[i] + " " + folds[i] + " " + rankings_testing[i]);	
				bwriter.newLine();
			}

			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	public static void writeTestingSummary(int[] folds, int[] rankings_testing, int[] better_cands_testing, 
			int[] worse_cands_testing, int[] total_cands_testing, double[] rrps, String[] query_names, int[][] rankings_testing_to_add) {
		BufferedWriter bwriter;
		try {
			bwriter = new BufferedWriter(new FileWriter(new File(output_file)));
			bwriter.write("query foldnum rank bc wc tc rrp mfonly bcmfonly avgrank");
			bwriter.newLine();
			for(int i = 0; i < query_names.length; i++) {
				bwriter.write(query_names[i] + " " + folds[i] + " " + rankings_testing[i] + " " + better_cands_testing[i] 
						+ " " + worse_cands_testing[i] + " " + total_cands_testing[i] + " " + rrps[i]
						+ " " + rankings_testing_to_add[i][0] + " " + rankings_testing_to_add[i][1] + " " + getAvgRank(rankings_testing_to_add[i][0], rankings_testing_to_add[i][1]));	
				bwriter.newLine();
			}

			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	public static void writeTestingSummary(int[] folds, double[] rankings_testing, int[] better_cands_testing, 
			int[] worse_cands_testing, int[] total_cands_testing, double[] rrps, String[] query_names, int[][] rankings_testing_to_add) {
		BufferedWriter bwriter;
		try {
			bwriter = new BufferedWriter(new FileWriter(new File(output_file)));
			bwriter.write("query foldnum rank bc wc tc rrp mfonly bcmfonly");
			bwriter.newLine();
			for(int i = 0; i < query_names.length; i++) {
				bwriter.write(query_names[i] + " " + folds[i] + " " + rankings_testing[i] + " " + better_cands_testing[i] 
						+ " " + worse_cands_testing[i] + " " + total_cands_testing[i] + " " + rrps[i]
						+ " " + rankings_testing_to_add[i][0] + " " + rankings_testing_to_add[i][1] + " " + getAvgRank(rankings_testing_to_add[i][0], rankings_testing_to_add[i][1]));	
				bwriter.newLine();
			}

			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	public static void printRankingValues(int[] ranking_values, String name, String filename, String suffix) {
		System.out.print(name);
		for(int i = 0; i < ranking_values.length; i++)
			System.out.print("\t" + ranking_values[i]);
		System.out.println("\t" + filename + suffix);
	}
	
	public static void printCumulativeRankingValues(int[] ranking_values, String name, String filename) {
		int sum = 0;
		System.out.print("C" + name);
		for(int i = 0; i < ranking_values.length; i++) {
			sum += ranking_values[i];
			System.out.print("\t" + sum);
		}
		System.out.println("\t" + filename);
	}
	
	public static void printRankingPercentages(int[] ranking_values, String name, String filename) {
		System.out.print(name);
		for(int i = 0; i < ranking_values.length; i++)
			System.out.print("\t" + (int)MathTools.round(((double)ranking_values[i] / (double)number_queries) * 100, 0));
		System.out.println("\t" + filename);
	}
	
	public static void printCumulativeRankingPercentages(int[] ranking_values, String name, String filename) {
		int sum = 0;
		System.out.print("C" + name);
		for(int i = 0; i < ranking_values.length; i++) {
			sum += ranking_values[i];
			System.out.print("\t" + (int)MathTools.round(((double)sum / (double)number_queries) * 100, 0));
		}
		System.out.println("\t" + filename);
	}
}
