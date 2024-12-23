package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.MultipleTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.Settings;


public class OfflineIndividualMoNASpectralSimilarityInitialiser implements IScoreInitialiser {
	private Logger logger = Logger.getLogger(OfflineIndividualMoNASpectralSimilarityInitialiser.class);

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		if(!settings.containsKey(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) || settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) == null) {
			if(settings.containsKey(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) && settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) != null) return;
			
			MultipleTandemMassPeakListReader multiplePeakListReader = new MultipleTandemMassPeakListReader(settings);
			SpectralPeakListCollection spectralPeakLists = null;
			Path offlineSpectralFilePath = null; 
			if(settings.containsKey(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME) && settings.get(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME) != null) {
				offlineSpectralFilePath = Paths.get((String)settings.get(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME));
			}
			
			if (offlineSpectralFilePath !=null && Files.isRegularFile(offlineSpectralFilePath)) {
				logger.info("Load reference data from file: " +offlineSpectralFilePath );
				List<Path> libFiles = new ArrayList<Path>();
				libFiles.add(offlineSpectralFilePath);
				InputStream inStream = Files.newInputStream(offlineSpectralFilePath);
				spectralPeakLists = multiplePeakListReader.readMultiple(inStream);
			} else if (offlineSpectralFilePath !=null && Files.isDirectory(offlineSpectralFilePath)) {
				logger.info("Load reference data from directory: " +offlineSpectralFilePath );
				List<Path> libFiles = Files.walk(offlineSpectralFilePath)
						.filter(Files::isRegularFile)
						.filter(p -> p.getFileName().toString().endsWith(".mb"))
						.collect(Collectors.toList());
				spectralPeakLists = multiplePeakListReader.readMultiple(libFiles);
			}
			else {
				logger.info("Load reference data from resource file \"MoNA-export-LC-MS.mb\".");
				InputStream inStream = null;
				inStream = OfflineIndividualMoNASpectralSimilarityInitialiser.class.getResourceAsStream("/MoNA-export-LC-MS.mb");
				spectralPeakLists = multiplePeakListReader.readMultiple(inStream);
				inStream.close();
			}
			//check whether MoNA InChIKeys are given as resource
			spectralPeakLists.calculateSimilarities((SortedTandemMassPeakList)settings.get(VariableNames.PEAK_LIST_NAME));

			settings.set(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME, spectralPeakLists);
		
		}
	}

	public void postProcessScoreParameters(Settings settings) {
		return;
	}
	
}
