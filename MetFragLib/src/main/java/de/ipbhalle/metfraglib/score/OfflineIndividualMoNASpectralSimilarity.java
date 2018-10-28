package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

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

	protected ICandidate candidate;
	
	public OfflineIndividualMoNASpectralSimilarity(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
	}
	
	public void calculate() throws Exception {
		this.value = 0.0;
		//beta = -9, gamma = 0.6
		try {
			SpectralPeakListCollection spectralPeakLists = (SpectralPeakListCollection)settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME);
			Double bestSimilarityScore = spectralPeakLists.getInchikey1ToSimScore().get(this.candidate.getProperty(VariableNames.INCHI_KEY_1_NAME));
			if(bestSimilarityScore != null) {
				this.value = bestSimilarityScore;
			}
		} catch(Exception e) {
			this.value = 0.0;
		}
		this.calculationFinished = true;
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
