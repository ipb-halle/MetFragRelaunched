package de.ipbhalle.metfraglib.peaklistreader;

import java.io.IOException;
import java.util.Random;

import massbank.MassBankCommon;
import massbank.MassBankUtilities;
import massbank.MassBankRecordParser;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedMergedTandemMassPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.MergedTandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

public class FilteredMergedRandomMassBankTandemMassPeakListReader extends FilteredTandemMassPeakListReader {

	protected Integer numberRandomSpectra;
	protected Random rand;
	
	public FilteredMergedRandomMassBankTandemMassPeakListReader(Settings settings) {
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
		MassBankCommon mbCommon = new MassBankCommon();
		Double exactMass = (Double) this.settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		java.util.ArrayList<String> massbankIDs = retrieveMassBankIDs(mbCommon, exactMass);

		String[] ids = new String[massbankIDs.size()];
		String[] sites = new String[massbankIDs.size()];
		
		int index = 0;
		for(String tmp : massbankIDs) {
			String[] params = tmp.split("\t");
			ids[index] = params[1].trim();
			sites[index] = params[5].trim();
			index++;
		}
		
		MassBankUtilities mbu = new MassBankUtilities((String)this.settings.get(VariableNames.MASSBANK_URL), (String)this.settings.get(VariableNames.MASSBANK_RECORD_CACHE_DIRECTORY), mbCommon);
		java.util.Vector<double[][]> massbankSpectra = new java.util.Vector<double[][]>();
		
		for(int i = 0; i < ids.length; i++) {
			String recordFileName = mbu.getRecordFile(ids[i]);
			try {
				mbu.retrieveLinks(ids[i], sites[i]);
				mbu.retrieveRecord(ids[i], sites[i]);
				if(MassBankRecordParser.isValidRecord(recordFileName)) {
					double[][] currentMassBankSpectrum = MassBankRecordParser.Read(recordFileName);
					massbankSpectra.add(currentMassBankSpectrum);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		java.util.Vector<MergedTandemMassPeak> mergedTandemMassPeaks = new java.util.Vector<MergedTandemMassPeak>();
		for(int i = 0; i < peakList.getNumberElements(); i++) {
			MergedTandemMassPeak newMergedTandemMassPeak = new MergedTandemMassPeak(peakList.getElement(i).getMass(), peakList.getElement(i).getAbsoluteIntensity());
			newMergedTandemMassPeak.setRelativeIntensity(peakList.getElement(i).getRelativeIntensity());
			mergedTandemMassPeaks.add(newMergedTandemMassPeak);
		}
		
		for(int i = 0; i < this.numberRandomSpectra; i++) {
			double[][] currentRandomSpectrum = this.generateRandomSpectrum(peakList.getNumberElements(), exactMass, massbankSpectra);
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
	protected double[][] generateRandomSpectrum(int numberPeaks, double exactMass, java.util.Vector<double[][]> massbankSpectra) {
		double[][] randomSpectrum = new double[numberPeaks][2];
		/*
		 * store the current number of fetched peaks
		 */
		int numberFetchedPeaks = 0;
		double maxIntensity = 0.0;
		while(numberFetchedPeaks < numberPeaks) {
			/*
			 * draw an index for a spectrum
			 */
			int randomSpec = rand.nextInt(massbankSpectra.size());
			double[][] peakList = massbankSpectra.get(randomSpec);
			/*
			 * draw an index for a peak of the spectrum
			 */
			int randomPeak = rand.nextInt(peakList.length);
			double currentMz = MathTools.round(peakList[randomPeak][0], (Double)this.settings.get(VariableNames.NUMBER_OF_DIGITS_AFTER_ROUNDING_NAME));
			if(currentMz > exactMass) continue;
			boolean alreadyDrawnSimilarPeak = false;
			for(int k = 0; k < numberFetchedPeaks; k++) {
				if(peakList[k][0] == currentMz) {
					alreadyDrawnSimilarPeak = true;
					break;
				}
			}
			if(alreadyDrawnSimilarPeak) continue;
			
			randomSpectrum[numberFetchedPeaks][0] = currentMz;
			randomSpectrum[numberFetchedPeaks][1] = peakList[randomPeak][1];
			if(maxIntensity < randomSpectrum[numberFetchedPeaks][1]) maxIntensity = randomSpectrum[numberFetchedPeaks][1];
			numberFetchedPeaks++;
		}
		for(int i = 0; i < randomSpectrum.length; i++) {
			randomSpectrum[i][1] = MathTools.round((randomSpectrum[i][1] / maxIntensity) * 999.0, 1);
		}
		
		return randomSpectrum;
	}
	
	/**
	 * 
	 */
	private java.util.ArrayList<String> retrieveMassBankIDs(MassBankCommon mbCommon, double exactMass) {
		double searchMassIncrease = 2.0;
		int tolerance = 1;
		java.util.ArrayList<String> result = null;
		
		String massbankURL = (String) this.settings.get(VariableNames.MASSBANK_URL);
		while(result == null || result.size() < 20) {
			String param = "compound=&op1=and&mz="+(exactMass+searchMassIncrease)+"&tol="+tolerance+"&op2=and&formula=&" + 
					"searchType=keyword&sortKey=name&sortAction=1&pageNo=1&exec=&inst_grp=ESI&" + 
					"inst=CE-ESI-TOF&inst=ESI-IT-MS%2FMS&inst=ESI-QqIT-MS%2FMS&inst=ESI-QqQ-MS%2FMS&" + 
					"inst=ESI-QqTOF-MS%2FMS&inst=LC-ESI-IT&inst=LC-ESI-ITFT&inst=LC-ESI-ITTOF&" + 
					"inst=LC-ESI-Q&inst=LC-ESI-QIT&inst=LC-ESI-QQ&inst=LC-ESI-QTOF&ms=all&" + 
					"ms=MS&ms=MS2&ms=MS3&ms=MS4&ion=0H";
			
			String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_QUICK];
			result = mbCommon.execMultiDispatcher(massbankURL, typeName, param);
			searchMassIncrease += 2.0;
			tolerance += 2;
			if(tolerance == 20) {
				System.out.println("Error: Could not find suitable MassBank entries");
				System.exit(1);
			}
		}
		
		return result;
	}
	
}
