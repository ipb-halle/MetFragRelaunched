package de.ipbhalle.metfraglib.analyse;

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

	public static String rankings_folder_name = "/home/cruttkie/svn/eawag/2016hdx/metfrag/rankings_7_2/pos";
	public static int number_folds = 10;
	public static int number_queries = 1;
	public static String only_metfrag_filename = "rankings_1005.txt";
	public static String given_folds_filename = null;
	public static String output_file = null;
	
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
				java.util.Vector<Integer> given_folds = new java.util.Vector<Integer>();
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
		if(folds == null) folds = generateFolds(query_names.length);
		number_queries = folds.length;
		System.out.println("Containing " + folds.length + " queries");
		//matrix contains rankings for each file (folds.length)
		//for each weight combination number_allowed_files
		int[][] rank_matrix = new int[folds.length][number_allowed_files]; 
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
					}
					catch(Exception e) {
						System.err.println("Error at " + row_index + " " + file_index + " " 
								+ rank_matrix.length + " " + rank_matrix[0].length);
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
				
				//check all queries/files/spectra
				for(int row_index = 0; row_index < rank_matrix.length; row_index++) {
					//check that current query is not in current fold
					if(current_fold == folds[row_index]) continue;
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
							best_weight_index[current_fold] = weight_index;
						}
					}
					else if(best_ranking_numbers[i] > ranking_numbers[i]) break;
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
				query_testing_rankings[row_index] = current_rank + 1;
				if(current_rank >= check_best_rank_positions) continue;
				test_results[current_fold][current_rank]++;
			}
		}
		System.out.println();
		
		if(output_file != null) writeTestingSummary(folds, query_testing_rankings, query_names);
		
		int[] summed_results = new int[check_best_rank_positions];
		for(int i = 0; i < test_results.length; i++) {
			System.out.print("Fold\t" + (i + 1) + ":");
			for(int j = 0; j < test_results[i].length; j++) {
				System.out.print("\t" + test_results[i][j]);
				summed_results[j] += test_results[i][j];
			}
			System.out.println("\t" + "(" + number_in_fold[i] + ")" + "\t" + all_filenames[valid_indexes[best_weight_index[i]]]);
		}
		System.out.println("===================================================================================================");
		printRankingValues(summed_results, "Sum:\t", "");
		printCumulativeRankingValues(summed_results, "Sum:\t", "");
		
		printCumulativeRankingPercentages(summed_results, "PercSum:", "");
		
		calculateBestWeight(rank_matrix, check_best_rank_positions, valid_indexes, all_filenames);
		
		printRankingsStatistics(only_metfrag_filename, check_best_rank_positions);
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
		printRankingValues(best_ranking_numbers, "Complete:", file_names[valid_indexes[best_weight_index]]);
		printCumulativeRankingValues(best_ranking_numbers, "Complete:", file_names[valid_indexes[best_weight_index]]);
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
		java.util.Vector<String> entries = new java.util.Vector<String>();
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
		java.util.Vector<Integer> entry_indexes = new java.util.Vector<Integer>();
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
	
	public static void printRankingsStatistics(String filename, int check_best_rank_positions) {
		int[] ranking_values = new int[check_best_rank_positions];
		try {
			BufferedReader breader = new BufferedReader(new FileReader(new File(rankings_folder_name + "/" + filename)));
			String line = "";
			while((line = breader.readLine()) != null) {
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("\\s+");
				int current_rank = Integer.parseInt(tmp[2]) - 1;
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
		printRankingValues(ranking_values, "Complete:", filename);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public static void printRankingValues(int[] ranking_values, String name, String filename) {
		System.out.print(name);
		for(int i = 0; i < ranking_values.length; i++)
			System.out.print("\t" + ranking_values[i]);
		System.out.println("\t" + filename);
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
