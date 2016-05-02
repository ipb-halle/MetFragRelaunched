package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSuspectListFilter;
import de.ipbhalle.metfraglib.functions.MoNARestWebService;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
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
public class IndividualMoNASpectralSimilarity extends AbstractScore {

	protected ICandidate candidate;
	
	public IndividualMoNASpectralSimilarity(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
	}
	
	public void calculate() throws Exception {
		final PreProcessingCandidateSuspectListFilter MoNAPresentInChIKeys = (PreProcessingCandidateSuspectListFilter)this.settings.get(VariableNames.INDIVIDUAL_MONA_SPECTRAL_SIMILARITY_INCHIKEY_LIST_NAME);
		final MoNARestWebService webService = (MoNARestWebService)this.settings.get(VariableNames.INDIVIDUAL_MONA_SPECTRAL_SIMILARITY_WEB_SERVICE_NAME);
		
		if(MoNAPresentInChIKeys == null || MoNAPresentInChIKeys.passesFilter(this.candidate, false)) {
			SortedTandemMassPeakList peakList = (SortedTandemMassPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
			String inchikey1 = (String)this.candidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
			DefaultPeakList[] monaPeakList = webService.retrievePeakListByInChIKey(inchikey1, peakList.getMeasuredPrecursorMass());
			if(monaPeakList == null || monaPeakList.length == 0) {
				this.value = 0.0;
			}
			else {
				double max = 0.0;
				for(int i = 0; i < monaPeakList.length; i++) {
					double cs = ((SortedTandemMassPeakList)monaPeakList[i]).cosineSimilarity(
							peakList, (Double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME), 
							(Double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME));
					if(cs > max) max = cs;
				}
				this.value = max;
			}
		}
		else this.value = 0.0;
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
