package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class PostProcessingCandidateHDGroupFlagFilter extends AbstractPostProcessingCandidateFilter {

	public PostProcessingCandidateHDGroupFlagFilter(Settings settings) {
		super(settings);
	}
	
	public CandidateList filter(CandidateList candidateList) {
		this.numberPostFilteredCandidates = 0;
		if(candidateList.getNumberElements() == 0) return candidateList;
		CandidateList filteredCandidateList = new SortedScoredCandidateList();
		java.util.ArrayList<String> seenHDGroupFlags = new java.util.ArrayList<String>();
		java.util.HashMap<String, MatchList> hdGroupFlagToMatchList = new java.util.HashMap<String, MatchList>();
		java.util.HashMap<String, IMolecularStructure> hdGroupFlagToMolecularStructure = new java.util.HashMap<String, IMolecularStructure>();
		java.util.HashMap<String, java.util.Hashtable<String, Object>> hdGroupFlagToProperties = new java.util.HashMap<String, java.util.Hashtable<String, Object>>();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate currentCandidate = candidateList.getElement(i);
			if(currentCandidate.getProperty(VariableNames.HD_GROUP_FLAG_NAME) != null) {
				String hdGroupFlag = (String)currentCandidate.getProperty(VariableNames.HD_GROUP_FLAG_NAME);
				java.util.Hashtable<String, Object> currentProperties = currentCandidate.getProperties();
				if(currentProperties != null) {
					if(hdGroupFlagToProperties.containsKey(hdGroupFlag) && hdGroupFlagToProperties.get(hdGroupFlag) != null) {
						java.util.Hashtable<String, Object> storedProperties = hdGroupFlagToProperties.get(hdGroupFlag);
						java.util.Enumeration<String> e = currentProperties.keys();
						while(e.hasMoreElements()) {
							String currentKey = e.nextElement();
							if(!storedProperties.containsKey(currentKey)) {
								storedProperties.put(currentKey, currentProperties.get(currentKey));
							}
						}
						
					}
					else hdGroupFlagToProperties.put(hdGroupFlag, currentProperties);
				}
				if(currentCandidate.getPrecursorMolecule() != null) {
					hdGroupFlagToMolecularStructure.put(hdGroupFlag, currentCandidate.getPrecursorMolecule());
					hdGroupFlagToProperties.put(hdGroupFlag, currentCandidate.getProperties());
				}
				if(currentCandidate.getMatchList() != null) {
					hdGroupFlagToMatchList.put(hdGroupFlag, currentCandidate.getMatchList());
				}
				if(!seenHDGroupFlags.contains(hdGroupFlag)) {
					filteredCandidateList.addElement(currentCandidate);
					seenHDGroupFlags.add(hdGroupFlag);
				}
				else {
					currentCandidate.nullify();
					currentCandidate = null;
					//this.numberPostFilteredCandidates++;
				}
			}
		}
		for(int i = 0; i < filteredCandidateList.getNumberElements(); i++) {
			String hdGroupFlag = (String)filteredCandidateList.getElement(i).getProperty(VariableNames.HD_GROUP_FLAG_NAME);
			//set matchlist
			filteredCandidateList.getElement(i).setMatchList(hdGroupFlagToMatchList.get(hdGroupFlag));
			//set precursor molecule
			filteredCandidateList.getElement(i).setPrecursorMolecule(hdGroupFlagToMolecularStructure.get(hdGroupFlag));
			//set properties (if necessary)
			java.util.Hashtable<String, Object> properties = hdGroupFlagToProperties.get(hdGroupFlag);
			java.util.Enumeration<String> e = properties.keys();
			while(e.hasMoreElements()) {
				String currentKey = e.nextElement();
				//check whether property is already set (so if there's a more candidate specific property present keep it)
				if(filteredCandidateList.getElement(i).getProperty(currentKey) == null) {
					filteredCandidateList.getElement(i).setProperty(currentKey, properties.get(currentKey));
				}
			}
			//remove hd group flag
			filteredCandidateList.getElement(i).removeProperty(VariableNames.HD_GROUP_FLAG_NAME);
		}
		
		return filteredCandidateList;
	}
	
	public void nullify() {
		
	}
	
}
