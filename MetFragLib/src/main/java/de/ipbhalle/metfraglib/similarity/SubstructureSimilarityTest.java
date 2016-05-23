package de.ipbhalle.metfraglib.similarity;

import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupListCollection;
import de.ipbhalle.metfraglib.substructure.SmartsGroup;

public class SubstructureSimilarityTest {

	public static void main(String[] args) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		String filename = args[0];
		Double mzppm = Double.parseDouble(args[1]);
		Double mzabs = Double.parseDouble(args[2]);
		Settings settings = new Settings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filename);
		LocalCSVDatabase db = new LocalCSVDatabase(settings);
		java.util.Vector<String> ids = db.getCandidateIdentifiers();
		CandidateList candidateList = db.getCandidateByIdentifier(ids);
		//SmilesOfExplPeaks
		PeakToSmartGroupListCollection peakToSmartGroupListCollection = new PeakToSmartGroupListCollection();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate candidate = candidateList.getElement(i);
			String smilesOfExplPeaks = (String)candidate.getProperty("SmilesOfExplPeaks");
			smilesOfExplPeaks = smilesOfExplPeaks.trim();
			if(smilesOfExplPeaks.equals("NA")) continue;
			String[] pairs = smilesOfExplPeaks.split(";");
			for(int k = 0; k < pairs.length; k++) {
				String[] tmp = pairs[k].split(":");
				Double peak = Double.parseDouble(tmp[0]);
				String smiles = tmp[1];
				PeakToSmartGroupList peakToSmartGroupList = peakToSmartGroupListCollection.getElementByPeak(peak, mzppm, mzabs);
				if(peakToSmartGroupList == null) {
					peakToSmartGroupList = new PeakToSmartGroupList(peak);
					SmartsGroup obj = new SmartsGroup(0.0);
					obj.addElement(smiles);
					peakToSmartGroupList.addElement(obj);
					peakToSmartGroupListCollection.addElement(peakToSmartGroupList);
				}
				else {
					SmartsGroup smartsGroup = peakToSmartGroupList.getElementBySmiles(smiles, 1.0);
					if(smartsGroup != null) {
						smartsGroup.addElement(smiles);
					}
					else {
						smartsGroup = new SmartsGroup(0.0);
						smartsGroup.addElement(smiles);
						peakToSmartGroupList.addElement(smartsGroup);
					}
				}
			}
		}
		
	}

}
