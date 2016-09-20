package de.ipbhalle.metfraglib.scoreinitialisation;

import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.MultipleTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.Settings;

public class OfflineIndividualMoNASpectralSimilarityInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		if(!settings.containsKey(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) || settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) == null) {
			if(settings.containsKey(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) && settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME) != null) return;
			
			String offlineSpectralFilePath = "";
			java.io.InputStream is = null;
			MultipleTandemMassPeakListReader multiplePeakListReader = new MultipleTandemMassPeakListReader(settings);
			SpectralPeakListCollection spectralPeakLists = null;
			if(settings.containsKey(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME) && settings.get(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME) != null) {
				offlineSpectralFilePath = (String)settings.get(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME);
				spectralPeakLists = multiplePeakListReader.readMultiple(offlineSpectralFilePath);
			}
			else { 
				is = OfflineMetFusionSpectralSimilarityScoreInitialiser.class.getResourceAsStream("/MoNA-export-LC-MS.mb");
				spectralPeakLists = multiplePeakListReader.readMultiple(is);
				is.close();
			}
			//check whether MoNA InChIKeys are given as resource
			spectralPeakLists.calculateSimilarities((SortedTandemMassPeakList)settings.get(VariableNames.PEAK_LIST_NAME));

			settings.set(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME, spectralPeakLists);
		
		}
	}

}
