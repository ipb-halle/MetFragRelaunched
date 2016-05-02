package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

public class ExactMoNASpectralSimilarity extends AbstractScore {

	protected ICandidate candidate;
	
	public ExactMoNASpectralSimilarity(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipbhalle.metfraglib.interfaces.IScore#calculate()
	 */
	public void calculate() {
		//thread save
		this.value = 0.0;
		/*
		 * previously calculated by ExactMoNASpectralSimilarityInitialiser
		 */
		CandidateList candidatesFromMoNA = (CandidateList)this.settings.get(VariableNames.EXACT_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME);
		//beta = -9, gamma = 0.6
		try {
			String candidateInchikey = (String)this.candidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
			for(int i = 0; i < candidatesFromMoNA.getNumberElements(); i++) {
				String currentInChIKey = (String)candidatesFromMoNA.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);
				if(candidateInchikey.equals(currentInChIKey)) {
					double score = (Double)candidatesFromMoNA.getElement(i).getProperty("score");
					if(score > this.value) 
						this.value = score;
				}
			}
		} catch(Exception e) {
			System.err.println("calculate ExactMoNASpectralSimilarity error");
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
	
	@Override
	public void nullify() {
		super.nullify();
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
	
	protected String buildMoNAQueryString() {
		//{"compound":{},"metadata":[],"tags":[],"match":{"spectra":"85.0759777777778:69374800 91.0541777777778:536336704 134.0964:781086.7"}}
		String queryString = "{\"compound\":{},\"metadata\":[],\"tags\":[],\"match\":{\"spectra\":\"";
		SortedTandemMassPeakList tandemMassPeakList = (SortedTandemMassPeakList)this.settings.get(VariableNames.PEAK_LIST_NAME);
		for(int i = 0; i < tandemMassPeakList.getNumberElements(); i++) {
			TandemMassPeak peak = tandemMassPeakList.getElement(i);
			queryString += peak.getMass() + ":" + peak.getRelativeIntensity();
			if(i != (tandemMassPeakList.getNumberElements() - 1)) queryString += " ";
		}
		queryString += "\"}}";
		return queryString;
	}
}
