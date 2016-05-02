package de.ipbhalle.metfraglib.score;

import org.openscience.cdk.fingerprint.IBitFingerprint;

import de.ipbhalle.metfraglib.candidate.PrecursorCandidate;
import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedSimilarityTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;

/**
 * fetch spectra from offline spectral file
 * TODO: get similarity scores from MoNA results (currently not contained within the results)
 * 
 * @author cruttkie
 *
 */
public class OfflineMetFusionSpectralSimilarityScore extends AbstractScore {

	protected ICandidate candidate;
	
	public OfflineMetFusionSpectralSimilarityScore(Settings settings) {
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
			SpectralPeakListCollection spectralPeakLists = (SpectralPeakListCollection)settings.get(VariableNames.OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME);
			//fingerprint of the current candidate
			IBitFingerprint f2 = TanimotoSimilarity.calculateFingerPrint(this.candidate.getAtomContainer());
			//calculate similarity score
			java.util.Hashtable<String, SortedSimilarityTandemMassPeakList> inchikey1ToPeakList = spectralPeakLists.getInchikey1ToPeakList();
			java.util.Hashtable<String, Double> inchikey1ToSimScore = spectralPeakLists.getInchikey1ToSimScore();
			java.util.Enumeration<?> it = inchikey1ToPeakList.keys();
			while(it.hasMoreElements()) {
				String inchikey1 = (String)it.nextElement();
				double val = TanimotoSimilarity.calculateSimilarity(inchikey1ToPeakList.get(inchikey1).getFingerprint(), f2) * inchikey1ToSimScore.get(inchikey1);
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
	
	public static void main(String[] args) throws Exception {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, 
				"65.0381 17.51663\n" +
				"108.0472 18.403548\n" +
				"125.0479 26.16408\n" +
				"156.0125 47.006652\n" +
				"173.0383 100");
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 172.0306);
		FilteredStringTandemMassPeakListReader peaklistreader = new FilteredStringTandemMassPeakListReader(settings);
		DefaultPeakList peaklist = peaklistreader.read();
		settings.set(VariableNames.PEAK_LIST_NAME, peaklist);
		
		PrecursorCandidate candidate = new PrecursorCandidate("InChI=1S/C6H8N2O2S/c7-5-1-3-6(4-2-5)11(8,9)10/h1-4H,7H2,(H2,8,9,10)", "Sulfanilamide");
		settings.set(VariableNames.CANDIDATE_NAME, candidate);
		
		OfflineMetFusionSpectralSimilarityScore score = new OfflineMetFusionSpectralSimilarityScore(settings);
		
		score.calculate();
		System.out.println(score.getValue());
	}
	
}
