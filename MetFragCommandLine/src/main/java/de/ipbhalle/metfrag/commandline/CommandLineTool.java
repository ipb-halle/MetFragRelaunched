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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(CommandLineTool.class);
		if(args == null || args.length == 0) {
			logger.error("Parameter file is missing!");
			return;
		}
		if(args.length > 1) {
			logger.error("Too many arguments! Just one parameter file is needed!");
			return;
		}
		File parameterFile = new File(args[0].trim());
		if(!parameterFile.exists()) {
			logger.error("Parameter file " + parameterFile.getAbsolutePath() + " not found!");
			return;
		}
		if(!parameterFile.canRead()) {
			logger.error("Parameter file " + parameterFile.getAbsolutePath() + " has no read permissions!");
			return;
		}
		if(!parameterFile.isFile()) {
			logger.error("Parameter file " + parameterFile.getAbsolutePath() + " is no regular file!"); 
			return;
		}
		MetFragGlobalSettings settings = null;
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
		//check settings with SettingsChecker	
		SettingsChecker settingsChecker = new SettingsChecker();
		if(!settingsChecker.check(settings)) return;
		
		/*
		 * load hd plugin
		 */
		HydrogenDeuteriumPlugin hdPlugin = new HydrogenDeuteriumPlugin();
		if(!hdPlugin.load(settings)) return;

		//init the MetFrag process
		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
		
		//retrieve candidates from database
		try {
			boolean candidatesRetrieved = mp.retrieveCompounds();
			if(!candidatesRetrieved) throw new Exception();
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Error when retrieving compounds.");
			System.exit(2);
		}
		//run the MetFrag process -> in silico fragmentation, fragment-peak-assignment, scoring
		try {
			mp.run();
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Error when processing compounds.");
			System.exit(3);
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
						candidateWriter.write(scoredCandidateList, (String)settings.get(VariableNames.SAMPLE_NAME), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME));
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("Error: Could not write candidate file.");
						System.exit(4);
					}
				}
			}
			/*
			 * store candidates fragments in your specified format
			 */
			if(settings.get(VariableNames.METFRAG_CANDIDATE_FRAGMENT_WRITER_NAME) != null) {
				IWriter candidateWriter = (IWriter) Class.forName(ClassNames.getClassNameOfFragmentListWriter((String)settings.get(VariableNames.METFRAG_CANDIDATE_FRAGMENT_WRITER_NAME))).getConstructor().newInstance();
				try {
					candidateWriter.write(scoredCandidateList, (String)settings.get(VariableNames.SAMPLE_NAME), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME));
				} catch (Exception e) {
					logger.error("Error: Could not write fragment files.");
					System.exit(5);
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
						System.exit(5);
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
							RenderedImage renderedImage = imageGenerator.generateImage((DefaultBitArrayFragment)matchList.getElement(j).getBestMatchedFragment());
							DefaultList imageList = new DefaultList();
							imageList.addElement(renderedImage);
							imageWriter.write(imageList, (String)settings.get(VariableNames.SAMPLE_NAME) + "_" + scoredCandidateList.getElement(i).getIdentifier() + "_fragment_id" + (j+1), (String)settings.get(VariableNames.STORE_RESULTS_PATH_NAME));
						} catch (Exception e) {
							logger.error("Error: Could not write fragment image files.");
							System.exit(6);
						}
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
