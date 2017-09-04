package de.ipbhalle.metfraglib.peaklistreader;

import java.util.Random;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedMergedTandemMassPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.MergedTandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

public class FilteredMergedRandomTandemMassPeakListReader extends FilteredTandemMassPeakListReader {

	protected Integer numberRandomSpectra;
	protected Random rand;
	
	protected double[] massDifferences = {
			15.02348,
			15.99491,
			16.01872,
			17.00274,
			17.02655,
			18.01056,
			18.99840,
			20.00623,
			26.01565,
			27.01090,
			27.99491,
			28.01872,
			28.03130,
			29.00274,
			29.03913,
			29.99799,
			30.04695,
			30.01056,
			31.01839,
			32.97990,
			32.02621,
			33.98772,
			35.97668,
			43.98983,
			46.04186,
			45.99290,
			47.96699,
			55.05478,
			57.07043,
			57.03404,
			58.07825,
			57.99290,
			59.01330,
			60.02113,
			63.96190,
			77.03913,
			78.91834,
			91.05478,
			126.9045
	};
	
	public FilteredMergedRandomTandemMassPeakListReader(Settings settings) {
		super(settings);
		this.rand = new Random(1000);
		this.numberRandomSpectra = (Integer)this.settings.get(VariableNames.NUMBER_RANDOM_SPECTRA_NAME);
	}

	@Override
	public DefaultPeakList read() {
		/*
		 * reading the query peak list
		 */
		SortedTandemMassPeakList peakList = (SortedTandemMassPeakList)super.read();
		/*
		 * generating randomised spectra queried from massbank
		 */
		Double exactMass = (Double) this.settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
	
		java.util.ArrayList<MergedTandemMassPeak> mergedTandemMassPeaks = new java.util.ArrayList<MergedTandemMassPeak>();
		for(int i = 0; i < peakList.getNumberElements(); i++) {
			MergedTandemMassPeak newMergedTandemMassPeak = new MergedTandemMassPeak(peakList.getElement(i).getMass(), peakList.getElement(i).getAbsoluteIntensity());
			newMergedTandemMassPeak.setRelativeIntensity(peakList.getElement(i).getRelativeIntensity());
			mergedTandemMassPeaks.add(newMergedTandemMassPeak);
		}
		
		int index = 0;
		for(int i = 0; i < this.numberRandomSpectra; i++) {
			double[][] currentRandomSpectrum = this.generateRandomSpectrum(exactMass, peakList);
			for(int k = 0; k < currentRandomSpectrum.length; k++) {
				index = 0;
				boolean found = false;
				while(index < mergedTandemMassPeaks.size()) {
					if(mergedTandemMassPeaks.get(index).getMass() == currentRandomSpectrum[k][0]) {
						found = true;
						break;
					}
					else if(mergedTandemMassPeaks.get(index).getMass() > currentRandomSpectrum[k][0]) {
						break;
					}
					index++;
				}
				if(found) {
					mergedTandemMassPeaks.get(index).getIntensities().add(currentRandomSpectrum[k][1]);
					mergedTandemMassPeaks.get(index).getSpectraIDs().add(i);
				}
				else {
					MergedTandemMassPeak newMergedTandemMassPeak = new MergedTandemMassPeak(currentRandomSpectrum[k][0], -1.0);
					newMergedTandemMassPeak.getIntensities().add(currentRandomSpectrum[k][1]);
					newMergedTandemMassPeak.getSpectraIDs().add(i);
					mergedTandemMassPeaks.add(index, newMergedTandemMassPeak);
				}
			}
		}
		SortedMergedTandemMassPeakList sortedMergedTandemMassPeakList = new SortedMergedTandemMassPeakList(exactMass);
		for(int i = 0; i < mergedTandemMassPeaks.size(); i++) {
			sortedMergedTandemMassPeakList.addElement(mergedTandemMassPeaks.get(i));
		}

		return sortedMergedTandemMassPeakList;
	}

	/**
	 * generate a randomised spectrum from a set of retrieved massbank spectra
	 * 
	 * @param numberPeaks
	 * @param massbankSpectra
	 * @return
	 */
	protected double[][] generateRandomSpectrum(double exactMass, SortedTandemMassPeakList peakList) {
		double[][] randomSpectrum = new double[peakList.getNumberElements()][2];
		/*
		 * store the current number of fetched peaks
		 */
		int numberFetchedPeaks = 0;
		double maxIntensity = 0.0;
		while(numberFetchedPeaks < peakList.getNumberElements()) {
			/*
			 * draw an index for a peak of the spectrum
			 */
			
			int randomPeak = rand.nextInt(numberFetchedPeaks + 1);
			double parentMz = randomPeak == numberFetchedPeaks ? exactMass: randomSpectrum[randomPeak][0];
			boolean alreadyDrawnSimilarPeak = false;
			double currentMz = parentMz - massDifferences[rand.nextInt(massDifferences.length)];
			//System.out.println(randomPeak + " " + numberFetchedPeaks + " " + parentMz);
			
			if(currentMz <= 0.0) continue;
			for(int k = 0; k < numberFetchedPeaks; k++) {
				if(randomSpectrum[k][0] == currentMz) {
					alreadyDrawnSimilarPeak = true;
					break;
				}
			}
			if(alreadyDrawnSimilarPeak) continue;
			
			currentMz += ((rand.nextInt(2) + 1) * (rand.nextInt(2) == 0 ? -1.0 : 1.0));
			randomSpectrum[numberFetchedPeaks][0] = currentMz;
			randomSpectrum[numberFetchedPeaks][1] = peakList.getElement(numberFetchedPeaks).getAbsoluteIntensity();
			if(maxIntensity < randomSpectrum[numberFetchedPeaks][1]) 
				maxIntensity = randomSpectrum[numberFetchedPeaks][1];
			numberFetchedPeaks++;
		}
		for(int i = 0; i < randomSpectrum.length; i++) {
			randomSpectrum[i][1] = MathTools.round((randomSpectrum[i][1] / maxIntensity) * 999.0, 1);
		}
		
		return randomSpectrum;
	}
	
}
