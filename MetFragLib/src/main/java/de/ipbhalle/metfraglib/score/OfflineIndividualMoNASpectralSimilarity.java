package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * fetched spectra in case they are present in the MoNA database
 * search is performed offline by inchikey list
 * 
 * the fetched spectra are used for cosine similarity
 * 
 * @author cruttkie
 *
 */
public class OfflineIndividualMoNASpectralSimilarity extends AbstractScore {
    protected static final Logger logger = LogManager.getLogger();
    protected ICandidate candidate;
	
	public OfflineIndividualMoNASpectralSimilarity(Settings settings) {
		super(settings);
        Configurator.setLevel(logger.getName(), Level.toLevel("info"));
        this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
	}
	
	public void calculate() throws Exception {
		this.value = 0.0;
		logger.debug("Starte Berechnung der OfflineIndividualMoNASpectralSimilarity.");
		//beta = -9, gamma = 0.6
		try {
			SpectralPeakListCollection spectralPeakLists = (SpectralPeakListCollection)settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME);
			logger.info("Using " + spectralPeakLists.getSize() + " reference spectra.");
			String inchikey = (String) this.candidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
			logger.info("Verwende InChIKey: " + inchikey);
			Double bestSimilarityScore = spectralPeakLists.getInchikey1ToSimScore().get(this.candidate.getProperty(VariableNames.INCHI_KEY_1_NAME));
			logger.info("Gefundener Similarity Score: " + bestSimilarityScore);
			if(bestSimilarityScore != null) {
				this.value = bestSimilarityScore;
				logger.info("Similarity Score gesetzt: " + this.value);
			} else {
				logger.info("Kein Similarity Score gefunden, Wert bleibt 0.0");
			}
		} catch(Exception e) {
			logger.error("Fehler bei der Berechnung der Similarity: ", e);
			this.value = 0.0;
		}
		this.calculationFinished = true;
		logger.info("Berechnung abgeschlossen. Finaler Wert: " + this.value);
	}

	public void setOptimalValues(double[] values) {
		this.optimalValues[0] = values[0];
	}
	
	public Double[] calculateSingleMatch(IMatch match) {
		return new Double[] {0.0, null};
	}

	public Double getValue() {
		return this.value;
	}

	@Override
	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
	protected double signum(double beta, double gamma, double x) {
		return 1.0 / (1 + Math.exp(beta * (x - gamma)));
	}
	
}
