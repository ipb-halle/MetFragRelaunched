package de.ipbhalle.metfraglib.scoreinitialisation;

import de.ipbhalle.metfraglib.functions.MoNARestWebService;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class ExactMoNASpectralSimilarityInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		/*
		 * Initialises the online mona query and retrieves candidates by spectral 
		 * similarity. The candidates retrieved from the MoNA database are stored 
		 * in the settings object and can be used within the ExactMoNASpectralSimilarity
		 * class.
		 */
		MoNARestWebService webService = new MoNARestWebService(settings);
		try {
			CandidateList candidateList = webService.performSpectrumSimilaritySearch();
			settings.set(VariableNames.EXACT_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME, candidateList);
		}
		catch(Exception e) {
			settings.set(VariableNames.EXACT_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME, new CandidateList());
			e.printStackTrace();
			return;
		}
	}
	
	public void postProcessScoreParameters(Settings settings) {
		return;
	}

}
