package de.ipbhalle.metfrag.misc;

import de.ipbhalle.metfraglib.database.OnlinePubChemDatabase;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class GetMeanNodeDegreeOfCandidate {

	public static void main(String[] args) {
		String[] cids = args[0].split(",");
	
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		
		settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, cids);
	
		OnlinePubChemDatabase db = new OnlinePubChemDatabase(settings);
		
		java.util.ArrayList<String> identifiers = null;
		try {
			identifiers = db.getCandidateIdentifiers();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(identifiers.size() == 0) {
			System.err.println("Identifier not found in PubChem");
			return;
		}
	
		CandidateList candidates = null;
		try {
			candidates = db.getCandidateByIdentifier(identifiers);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			try {
				candidates.getElement(i).initialisePrecursorCandidate();
			} catch (AtomTypeNotKnownFromInputListException e1) {
				e1.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			IMolecularStructure struct = candidates.getElement(i).getPrecursorMolecule();
			System.out.println(candidates.getElement(i).getIdentifier() + " " + struct.getMeanNodeDegree() + " " + struct.getNumNodeDegreeOne());
		
		}
		
	}	
}
