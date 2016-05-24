package de.ipbhalle.metfraglib.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupListCollection;
import de.ipbhalle.metfraglib.substructure.SmartsGroup;

public class WriteSubstructureAnnotationFile {

	public static void main(String[] args) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		String filename = args[0];
		Double mzppm = Double.parseDouble(args[1]);
		Double mzabs = Double.parseDouble(args[2]);
		String output = null;
		if(args.length == 4) output = args[3];
		Settings settings = new Settings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filename);
		LocalPSVDatabase db = new LocalPSVDatabase(settings);
		java.util.Vector<String> ids = db.getCandidateIdentifiers();
		CandidateList candidateList = db.getCandidateByIdentifier(ids);
		//SmilesOfExplPeaks
		PeakToSmartGroupListCollection peakToSmartGroupListCollection = new PeakToSmartGroupListCollection();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate candidate = candidateList.getElement(i);
			String smilesOfExplPeaks = (String)candidate.getProperty("SmilesOfExplPeaks");
			String aromaticSmilesOfExplPeaks = (String)candidate.getProperty("AromaticSmilesOfExplPeaks");
			smilesOfExplPeaks = smilesOfExplPeaks.trim();
			aromaticSmilesOfExplPeaks = aromaticSmilesOfExplPeaks.trim();
			if(smilesOfExplPeaks.equals("NA") || aromaticSmilesOfExplPeaks.equals("NA")) continue;
			String[] pairs = smilesOfExplPeaks.split(";");
			String[] aromaticPairs = aromaticSmilesOfExplPeaks.split(";");
			if(pairs.length != aromaticPairs.length) {
				System.out.println(candidate.getIdentifier() + " " + candidate.getProperty(VariableNames.INCHI_KEY_1_NAME));
				continue;
			}
			for(int k = 0; k < pairs.length; k++) {
				String[] tmp = pairs[k].split(":");
				String[] aromaticTmp = aromaticPairs[k].split(":");
				Double peak = Double.parseDouble(tmp[0]);
				String smiles = null;
				String smarts = null;
				try {
					smiles = tmp[1];
					smarts = aromaticTmp[1];
				}
				catch(Exception e) {
					continue;
				}
				PeakToSmartGroupList peakToSmartGroupList = peakToSmartGroupListCollection.getElementByPeak(peak, mzppm, mzabs);
				if(peakToSmartGroupList == null) {
					peakToSmartGroupList = new PeakToSmartGroupList(peak);
					SmartsGroup obj = new SmartsGroup(0.0);
					obj.addElement(smarts);
					obj.addSmiles(smiles);
					peakToSmartGroupList.addElement(obj);
					peakToSmartGroupListCollection.addElementSorted(peakToSmartGroupList);
				}
				else {
					peakToSmartGroupList.setPeakmz((peakToSmartGroupList.getPeakmz() + peak) / 2.0);
					SmartsGroup smartsGroup = peakToSmartGroupList.getElementBySmiles(smiles, 1.0);
					if(smartsGroup != null) {
						smartsGroup.addElement(smarts);
						smartsGroup.addSmiles(smiles);
					}
					else {
						smartsGroup = new SmartsGroup(0.0);
						smartsGroup.addElement(smarts);
						smartsGroup.addSmiles(smiles);
						peakToSmartGroupList.addElement(smartsGroup);
					}
				}
			}
		}
		peakToSmartGroupListCollection.updateProbabilities();
		if(output == null) peakToSmartGroupListCollection.print();
		else {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output)));
			bwriter.write(peakToSmartGroupListCollection.toString());
			bwriter.close();
		}
	}
}
