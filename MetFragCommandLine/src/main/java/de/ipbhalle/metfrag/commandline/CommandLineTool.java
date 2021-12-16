package de.ipbhalle.metfrag.commandline;

import java.awt.image.RenderedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import de.ipbhalle.metfrag.plugins.HydrogenDeuteriumPlugin;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.IImageGenerator;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.DefaultList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.parameter.ClassNames;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class CommandLineTool {
	
	public static boolean printHelp = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(CommandLineTool.class);
		/*
		 * read in commandline arguments
		 */
		java.util.Hashtable<String, String> commandLineArguments = processCommandLineArguments(args, logger);
		if(commandLineArguments == null) {
			System.exit(1);
		}
		if(printHelp) {
			printHelp();
			System.exit(0);
		}

		File parameterFile = null;
		MetFragGlobalSettings settings = null;
		if(commandLineArguments.containsKey(VariableNames.PARAMETER_FILE_NAME)) {
			parameterFile = new File((String)commandLineArguments.get(VariableNames.PARAMETER_FILE_NAME));
			if(!parameterFile.exists()) {
				logger.error("Parameter file " + parameterFile.getAbsolutePath() + " not found!");
				System.exit(1);
			}
			if(!parameterFile.canRead()) {
				logger.error("Parameter file " + parameterFile.getAbsolutePath() + " has no read permissions!");
				System.exit(1);
			}
			if(!parameterFile.isFile()) {
				logger.error("Parameter file " + parameterFile.getAbsolutePath() + " is no regular file!"); 
				System.exit(1);
			}
			
			/*
			 * read settings
			 */
			try {
				settings = MetFragGlobalSettings.readSettings(parameterFile, logger);
			}
			catch(Exception e) {
				logger.error("Error reading parameter file " + parameterFile);
				System.exit(1);
			}
		}

		/*
		 * read additional parameters
		 */
		try {
			if(settings == null) {
				settings = MetFragGlobalSettings.readSettings(commandLineArguments, logger);
			} else {
				MetFragGlobalSettings.readSettings(commandLineArguments, settings, logger);
			}
		} catch (Exception e2) {
			logger.error(e2.getMessage());
			logger.error("Error reading settings");
			System.exit(1);
		}
		
		//check settings with SettingsChecker	
		SettingsChecker settingsChecker = new SettingsChecker();
		if(!settingsChecker.check(settings)) {
			System.exit(2);
		}

		/*
		 * load hd plugin
		 */
		HydrogenDeuteriumPlugin hdPlugin = new HydrogenDeuteriumPlugin();
		if(!hdPlugin.load(settings)) System.exit(3);

		//init the MetFrag process
		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
		
		//retrieve candidates from database
		try {
			boolean candidatesRetrieved = mp.retrieveCompounds();
			if(!candidatesRetrieved) throw new Exception();
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Error when retrieving compounds.");
			System.exit(4);
		}
		//run the MetFrag process -> in silico fragmentation, fragment-peak-assignment, scoring
		try {
			mp.run();
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Error when processing compounds.");
			System.exit(5);
		}
		//fetch the scored candidate list
		CandidateList scoredCandidateList = mp.getCandidateList();
		/*
		 * store the results
		 */
		try {
			/*
			 * store candidates in your specified format
			 */
			if(settings.get(VariableNames.METFRAG_CANDIDATE_WRITER_NAME) != null) {
				String[] candidateWriterNames = (String[])settings.get(VariableNames.METFRAG_CANDIDATE_WRITER_NAME);
				for(int k = 0; k < candidateWriterNames.length; k++) {
					IWriter candidateWriter = (IWriter) Class.forName(ClassNames.getClassNameOfCandidateListWriter(candidateWriterNames[k])).getConstructor().newInstance();
					try {
						Object ResultsFile = settings.get(VariableNames.STORE_RESULTS_FILE_NAME);
						if(ResultsFile != null) candidateWriter.writeFile(new File((String)ResultsFile), scoredCandidateList, settings); 
						else candidateWriter.write(scoredCandidateList, (String)settings.get(VariableNames.SAMPLE_NAME), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME), settings);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("Error: Could not write candidate file.");
						System.exit(6);
					}
				}
			}
			/*
			 * store candidates fragments in your specified format
			 */
			if(settings.get(VariableNames.METFRAG_CANDIDATE_FRAGMENT_WRITER_NAME) != null) {
				IWriter candidateWriter = (IWriter) Class.forName(ClassNames.getClassNameOfFragmentListWriter((String)settings.get(VariableNames.METFRAG_CANDIDATE_FRAGMENT_WRITER_NAME))).getConstructor().newInstance();
				try {
					candidateWriter.write(scoredCandidateList, (String)settings.get(VariableNames.SAMPLE_NAME), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME), settings);
				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.error("Error: Could not write fragment files.");
					System.exit(7);
				}
			}
			/*
			 * store candidates in your specified image format
			 */
			if(settings.get(VariableNames.METFRAG_CANDIDATE_IMAGE_GENERATOR_NAME) != null) {
				IWriter imageWriter = (IWriter) Class.forName((String)settings.get(VariableNames.METFRAG_IMAGE_WRITER_NAME)).getConstructor().newInstance();
				IImageGenerator imageGenerator = (IImageGenerator) Class.forName((String)settings.get(VariableNames.METFRAG_CANDIDATE_IMAGE_GENERATOR_NAME)).getConstructor().newInstance();
				for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
					try {
						RenderedImage renderedImage = imageGenerator.generateImage(scoredCandidateList.getElement(i));
						DefaultList imageList = new DefaultList();
						imageList.addElement(renderedImage);
						imageWriter.write(imageList, (String)settings.get(VariableNames.SAMPLE_NAME) + "_" + scoredCandidateList.getElement(i).getIdentifier(), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME));
					} catch (Exception e) {
						logger.error("Error: Could not write candidate image files.");
						System.exit(8);
					}
				}
			}
			/*
			 * store candidate fragments in your specified image format
			 */
			if(settings.get(VariableNames.METFRAG_FRAGMENT_IMAGE_GENERATOR_NAME) != null) {
				IWriter imageWriter = (IWriter) Class.forName((String)settings.get(VariableNames.METFRAG_IMAGE_WRITER_NAME)).getConstructor().newInstance();
				IImageGenerator imageGenerator = (IImageGenerator) Class.forName((String)settings.get(VariableNames.METFRAG_FRAGMENT_IMAGE_GENERATOR_NAME)).getConstructor().newInstance();
				for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
					MatchList matchList = scoredCandidateList.getElement(i).getMatchList();
					for(int j = 0; j < matchList.getNumberElements(); j++) {
						try {
							scoredCandidateList.getElement(i).initialisePrecursorCandidate();
							RenderedImage renderedImage = imageGenerator.generateImage(scoredCandidateList.getElement(i).getPrecursorMolecule(), (DefaultBitArrayFragment)matchList.getElement(j).getBestMatchedFragment());
							DefaultList imageList = new DefaultList();
							imageList.addElement(renderedImage);
							imageWriter.write(imageList, (String)settings.get(VariableNames.SAMPLE_NAME) + "_" + scoredCandidateList.getElement(i).getIdentifier() + "_fragment_id" + (j+1), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME));
							scoredCandidateList.getElement(i).resetPrecursorMolecule();
						} catch (Exception e) {
							logger.error("Error: Could not write fragment image files.");
							System.exit(9);
						}
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(10);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(10);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(10);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			System.exit(10);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.exit(10);
		} catch (SecurityException e) {
			e.printStackTrace();
			System.exit(10);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(10);
		}
	}

	/**
	 * process commandline arguments and return hashtable
	 * 
	 * @param args
	 * @param logger
	 * @return
	 */
	public static java.util.Hashtable<String, String> processCommandLineArguments(String[] args, Logger logger) {
		java.util.Hashtable<String, String> arguments = new java.util.Hashtable<String, String>();
		if(args == null || args.length == 0) {
			logger.error("Parameter file is missing!");
			logger.error("ParameterFile='path_to_parameterfile'");
			return null;
		}
		//preprocess arguments
		String argumentString = args[0];
		for(int i = 1; i < args.length; i++) 
			argumentString += " " + args[i];
		//replace escaped '=' maybe coming from a SMARTS
		argumentString = argumentString.replaceAll("\\\\=", "|").replaceAll("\\s+=", "=").replaceAll("=\\s+", "=").replaceAll("\\s+", " ").replaceAll("\\t", " ");
		String[] arguments_splitted = argumentString.split("\\s+");
		// if length 0 then it must be a parameter file
		try {
			if(arguments_splitted.length == 1) {
				if(arguments_splitted[0].contains("=")) {
					arguments.put(VariableNames.PARAMETER_FILE_NAME, arguments_splitted[0].split("=")[1]);
				}
				else if(arguments_splitted[0].equals("-help") || arguments_splitted[0].equals("--help")) {
					printHelp = true;
					return arguments;
				}
				else {
					arguments.put(VariableNames.PARAMETER_FILE_NAME, arguments_splitted[0]);
				}
				return arguments;
			}
			for(int i = 0; i < arguments_splitted.length; i++) {
				String[] current_argument = arguments_splitted[i].split("=");
				if(current_argument.length != 2) {
					logger.error("Error: Error at " + arguments_splitted[i]);
					return null;
				}
				if(arguments.containsKey(current_argument[0])) {
					logger.error("Error: Argument " + current_argument[0] + " is already defined.");
					return null;
				}
				// replace the placeholder | by =
				current_argument[1] = current_argument[1].replaceAll("\\|", "=");
				//check PeakListString
				if(current_argument[0].equals(VariableNames.PEAK_LIST_STRING_NAME)) {
					current_argument[1] = current_argument[1].replaceAll(";", "\\\n").replaceAll("_", " ");
				}
				arguments.put(current_argument[0], current_argument[1]);
			}
		} catch(Exception e) {
			logger.error("Error: Check commandline parameters!");
			return null;
		}
		return arguments;
	}
	
	public static void printHelp() {
		System.out.println("Usage: java -jar MetFragCL.jar ParameterFile='path_to_parameterfile'");
		System.out.println("\t\t\t(to execute MetFrag with a parameter file)");
		System.out.println("\tor java -jar MetFragCL.jar [args...]");
		System.out.println("\t\t\t(to execute MetFrag with parameters given on command line)");
	}
	
}
