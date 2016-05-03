package de.ipbhalle.metfraglib.tools;

import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSmartsInclusionFilter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class GetCandidateByMatchingSubstructure {

	public static void main(String[] args) {
		
		String[] smarts = new String[] {
				//"C12=C(C([N]C(N1C)=O)=O)N(C)C=N2"
				"c1cccnc1"
		};
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "LocalCSV");
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, "/home/cruttkie/Dokumente/PhD/MetFrag/casmi/2016/nontarget2016/CompoundsTraining.csv");
		settings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME, smarts);
		
		CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
		
		try {
			mp.retrieveCompounds();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		CandidateList candidates = mp.getCandidateList();
		
		PreProcessingCandidateSmartsInclusionFilter candidateFilter = new PreProcessingCandidateSmartsInclusionFilter(settings);
		
		int found = 0;
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			if(candidateFilter.passesFilter(candidates.getElement(i))) {
				System.out.println(
						candidates.getElement(i).getProperty("ufzname") + " " + 
						candidates.getElement(i).getProperty("challengename") + " " +
						candidates.getElement(i).getProperty("Identifier") + " " +
						candidates.getElement(i).getProperty("InChIKey1"));
				found++;
			}
		}
		System.out.println("found " + found + " candidates");
	}
	
}
