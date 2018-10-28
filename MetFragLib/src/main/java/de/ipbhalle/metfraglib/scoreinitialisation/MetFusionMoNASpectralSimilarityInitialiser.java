package de.ipbhalle.metfraglib.scoreinitialisation;

import org.openscience.cdk.fingerprint.IBitFingerprint;

import de.ipbhalle.metfraglib.fingerprint.TanimotoSimilarity;
import de.ipbhalle.metfraglib.functions.MoNARestWebService;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.Settings;

public class MetFusionMoNASpectralSimilarityInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		/*
		 * 
		 */
		//initialise MoNA web service
		MoNARestWebService webService = new MoNARestWebService(settings);
		((ProcessingStatus)settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).setProcessStatusString("Retrieving Results from MoNA");
		CandidateList candidatesFromMoNA = null;
		try {
			candidatesFromMoNA = webService.performSpectrumSimilaritySearch();
		}
		catch(Exception e) {
			settings.set(VariableNames.METFUSION_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME, new CandidateList());
			e.printStackTrace();
			return;
		}
		for(int i = 0; i < candidatesFromMoNA.getNumberElements(); i++) {
			IBitFingerprint fingerprint = TanimotoSimilarity.calculateFingerPrint(candidatesFromMoNA.getElement(i).getAtomContainer());
			candidatesFromMoNA.getElement(i).setProperty("Fingerprint", fingerprint);
		}
		settings.set(VariableNames.METFUSION_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME, candidatesFromMoNA);
			
	}

	public void postProcessScoreParameters(Settings settings) {
		return;
	}
	
}
