package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupListCollection;
import de.ipbhalle.metfraglib.substructure.SmartsGroup;

public class AutomatedLossAnnotationScoreInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		PeakToSmartGroupListCollection peakToSmartGroupListCollection = new PeakToSmartGroupListCollection();
		String filename = (String)settings.get(VariableNames.SMARTS_LOSS_ANNOTATION_FILE_NAME);
		DefaultPeakList peakList = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
		Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		
		Double neutralPrecursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		Double adductMass = Constants.getIonisationMassByNominalMassDifference((Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME));
		
		java.util.Vector<Double> massDifferences = calculatePeakDifferences(peakList, neutralPrecursorMass, adductMass);
		
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		while((line = breader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0) continue;
			if(line.startsWith("#")) continue;
			String[] tmp = line.split("\\s+");
			Double peak = Double.parseDouble(tmp[0]);
			if(!this.containsMass(peak, massDifferences, mzabs, mzppm)) continue;
			PeakToSmartGroupList peakToSmartGroupList = new PeakToSmartGroupList(peak);
			SmartsGroup smartsGroup = null;
			for(int i = 1; i < tmp.length; i++) {
				if(this.isDoubleValue(tmp[i])) {
					if(smartsGroup != null) 
						peakToSmartGroupList.addElement(smartsGroup);
					smartsGroup = new SmartsGroup(Double.parseDouble(tmp[i]));
				}
				else {
					smartsGroup.addElement(tmp[i]);
				}
				if(i == (tmp.length - 1)) {
					peakToSmartGroupList.addElement(smartsGroup);
					peakToSmartGroupListCollection.addElement(peakToSmartGroupList);
				}
			}
		}
		breader.close();
		settings.set(VariableNames.LOSS_TO_SMARTS_GROUP_LIST_COLLECTION_NAME, peakToSmartGroupListCollection);
	}
	
	private boolean containsMass(double mass, java.util.Vector<Double> massVector, double mzabs, double mzppm) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		
		for(int i = 0; i < massVector.size(); i++) 
		{
			double currentMass = massVector.get(i);
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				return true;
			}
			
		}
		return false;
	}
	
	private java.util.Vector<Double> calculatePeakDifferences(DefaultPeakList peakList, double neutralPrecursorMass, double adductMass) {
		java.util.Vector<Double> peakDifferences = new java.util.Vector<Double>();
		
		double ionmass = neutralPrecursorMass + adductMass;
		
		// calculate mass differences between mass peaks
		for(int i = 0; i < peakList.getNumberElements(); i++) {
			Double currentMass1 = ((TandemMassPeak)peakList.getElement(i)).getMass();
			if(ionmass <= currentMass1) continue;
			for(int j = i + 1; j < peakList.getNumberElements(); j++) {
				Double currentMass2 = ((TandemMassPeak)peakList.getElement(j)).getMass();
				if(currentMass2 <= currentMass1) continue;
				double massDifference = currentMass2 - currentMass1;
				peakDifferences.add(massDifference);
			}
			peakDifferences.add(ionmass - currentMass1);
		}
		
		return peakDifferences;
	}
	
	private boolean isDoubleValue(String value) {
		try {
			Double.parseDouble(value);
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}

}
