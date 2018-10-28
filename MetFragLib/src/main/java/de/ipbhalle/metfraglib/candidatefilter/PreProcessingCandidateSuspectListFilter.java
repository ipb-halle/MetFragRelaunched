package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.SuspectList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class PreProcessingCandidateSuspectListFilter extends AbstractPreProcessingCandidateFilter {

	private SuspectList[] suspectLists;
	
	public PreProcessingCandidateSuspectListFilter(Settings settings) {
		super(settings);
		String[] suspectListFileNames = (String[])settings.get(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME);
		this.suspectLists = new SuspectList[suspectListFileNames.length];
		for(int i = 0; i < suspectListFileNames.length; i++) {
			this.suspectLists[i] = new SuspectList(suspectListFileNames[i]);
		}
		
	}

	public PreProcessingCandidateSuspectListFilter(Settings settings, boolean isPrefiltered) {
		super(settings);
		String[] suspectListFileNames = (String[])settings.get(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME);
		this.suspectLists = new SuspectList[suspectListFileNames.length];
		for(int i = 0; i < suspectListFileNames.length; i++) {
			this.suspectLists[i] = new SuspectList(suspectListFileNames[i], isPrefiltered);
		}
		
	}
	
	public PreProcessingCandidateSuspectListFilter(String filename, Settings settings) {
		super(settings);
		this.suspectLists = new SuspectList[1];
		this.suspectLists[0] = new SuspectList(filename);
	}

	public PreProcessingCandidateSuspectListFilter(java.io.InputStream is, String name, Settings settings) {
		super(settings);
		this.suspectLists = new SuspectList[1];
		this.suspectLists[0] = new SuspectList(is, name);
	}

	public PreProcessingCandidateSuspectListFilter(String filename, Settings settings, boolean isPrefiltered) {
		super(settings);
		this.suspectLists = new SuspectList[1];
		this.suspectLists[0] = new SuspectList(filename, isPrefiltered);
	}

	public PreProcessingCandidateSuspectListFilter(java.io.InputStream is, String name, Settings settings, boolean isPrefiltered) {
		super(settings);
		this.suspectLists = new SuspectList[1];
		this.suspectLists[0] = new SuspectList(is, name, isPrefiltered);
	}
	
	public PreProcessingCandidateSuspectListFilter(String[] inChIKeys, String name, Settings settings, boolean isPrefiltered) {
		super(settings);
		this.suspectLists = new SuspectList[1];
		this.suspectLists[0] = new SuspectList(inChIKeys, name, isPrefiltered);
	}
	
	@Override
	public boolean passesFilter(ICandidate candidate) {
		String containedIn = "";
		boolean found = false;
		for(int i = 0; i < this.suspectLists.length; i++) {
			if(candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null) {
				String inchikey1 = (String)candidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
				if(inchikey1.length() != 0) {
					if(this.suspectLists[i].contains(inchikey1)) {
						containedIn += this.suspectLists[i].getName() + ";";
						found = true;
					}
				}
			}
		}
		if(found) candidate.setProperty("SuspectLists", containedIn.substring(0, containedIn.length() - 1));
		return found;
	}
	
	public boolean passesFilter(ICandidate candidate, boolean include) {
		String containedIn = "";
		boolean found = false;
		for(int i = 0; i < this.suspectLists.length; i++) {
			if(candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null) {
				String inchikey1 = (String)candidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
				if(inchikey1.length() != 0) {
					if(this.suspectLists[i].contains(inchikey1)) {
						containedIn += this.suspectLists[i].getName() + ";";
						found = true;
					}
				}
			}
		}
		if(found && include) candidate.setProperty("SuspectLists", containedIn.substring(0, containedIn.length() - 1));
		return found;
	}
	
	@Override
	public void nullify() {
		for(int i = 0; i < this.suspectLists.length; i++) this.suspectLists[i].nullify();
		this.suspectLists = null;
	}

}
