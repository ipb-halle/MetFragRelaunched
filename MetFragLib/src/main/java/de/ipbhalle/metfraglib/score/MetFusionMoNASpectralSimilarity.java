package de.ipbhalle.metfraglib.score;

import org.openscience.cdk.fingerprint.IBitFingerprint;

import de.ipbhalle.metfraglib.fingerprint.TanimotoSimilarity;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

/**
 * fetch spectra from MoNA by spectral similarity
 * currently not working reliably
 * TODO: get similarity scores from MoNA results (currently not contained within the results)
 * 
 * @author cruttkie
 *
 */
public class MetFusionMoNASpectralSimilarity extends AbstractScore {

	protected ICandidate candidate;
	
	public MetFusionMoNASpectralSimilarity(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
	}
	
	public void calculate() {
		
		this.value = 0.0;
		//beta = -9, gamma = 0.6
		try {
			CandidateList candidatesFromMoNA = (CandidateList)settings.get(VariableNames.METFUSION_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME);
			//fingerprint of the current candidate
			IBitFingerprint f2 = TanimotoSimilarity.calculateFingerPrint(this.candidate.getAtomContainer());
			//calculate similarity score
			for(int i = 0; i < candidatesFromMoNA.getNumberElements(); i++) {
				IBitFingerprint f1 = (IBitFingerprint)candidatesFromMoNA.getElement(i).getProperty("Fingerprint");
				double val = TanimotoSimilarity.calculateSimilarity(f1, f2) * (Double)candidatesFromMoNA.getElement(i).getProperty("score");
				this.value += this.signum(-9.0, 0.6, val);
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
