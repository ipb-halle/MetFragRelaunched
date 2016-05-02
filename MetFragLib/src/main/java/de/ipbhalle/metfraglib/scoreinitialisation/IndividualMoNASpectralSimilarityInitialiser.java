package de.ipbhalle.metfraglib.scoreinitialisation;

import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSuspectListFilter;
import de.ipbhalle.metfraglib.functions.MoNARestWebService;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.score.IndividualMoNASpectralSimilarity;
import de.ipbhalle.metfraglib.settings.Settings;

public class IndividualMoNASpectralSimilarityInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		/*
		 * 
		 */
		
		//initialise inchikey list of present substances in MoNA
		java.io.InputStream is = null;
		String inChIKeyFileName = "";
		PreProcessingCandidateSuspectListFilter MoNAPresentInChIKeys = null;
		if(settings.containsKey(VariableNames.MONA_PRESENT_INCHIKEYS_FILE_NAME) && settings.get(VariableNames.MONA_PRESENT_INCHIKEYS_FILE_NAME) != null) 
			inChIKeyFileName = (String)settings.get(VariableNames.MONA_PRESENT_INCHIKEYS_FILE_NAME);
		else
			is = IndividualMoNASpectralSimilarity.class.getResourceAsStream("/MoNA_InChIKeys.txt");
		if(inChIKeyFileName.length() != 0) 
			MoNAPresentInChIKeys = new PreProcessingCandidateSuspectListFilter(inChIKeyFileName, settings);
		else
			MoNAPresentInChIKeys = new PreProcessingCandidateSuspectListFilter(is, "MoNA_InChIKeys", settings);
		//initialise MoNA web service
		MoNARestWebService webService = new MoNARestWebService(settings);
		
		settings.set(VariableNames.INDIVIDUAL_MONA_SPECTRAL_SIMILARITY_INCHIKEY_LIST_NAME, MoNAPresentInChIKeys);
		settings.set(VariableNames.INDIVIDUAL_MONA_SPECTRAL_SIMILARITY_WEB_SERVICE_NAME, webService);
	}

}
