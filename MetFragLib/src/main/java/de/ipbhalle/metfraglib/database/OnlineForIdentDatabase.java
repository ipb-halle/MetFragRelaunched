package de.ipbhalle.metfraglib.database;

import de.ipbhalle.metfraglib.functions.ForIdentRestWebService;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.settings.Settings;

public class OnlineForIdentDatabase extends AbstractDatabase {
	
	private CandidateList candidateList;
	private ForIdentRestWebService forIdentRestWebService;
	
	public OnlineForIdentDatabase(Settings settings) {
		super(settings);
		this.forIdentRestWebService = new ForIdentRestWebService();
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public java.util.Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws Exception {
		logger.info("Fetching candidates from ForIdent");
		java.util.Vector<String> cids = new java.util.Vector<String>();
		this.candidateList = this.forIdentRestWebService.getCandidatesByMass(monoisotopicMass, relativeMassDeviation);
		for(int i = 0; i < this.candidateList.getNumberElements(); i++) {
			cids.add(this.candidateList.getElement(i).getIdentifier());
		}
		return cids;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public java.util.Vector<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		logger.info("Fetching candidates from ForIdent");
		java.util.Vector<String> cids = new java.util.Vector<String>();
		this.candidateList = this.forIdentRestWebService.getCandidatesByMolecularFormula(molecularFormula);
		for(int i = 0; i < this.candidateList.getNumberElements(); i++) {
			cids.add(this.candidateList.getElement(i).getIdentifier());
		}
		return cids;
	}

	//ToDo: check whether identifiers are valid and exist
	public java.util.Vector<String> getCandidateIdentifiers(java.util.Vector<String> identifiers) {
		return identifiers;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public ICandidate getCandidateByIdentifier(String identifier) throws Exception {
		if(this.candidateList == null) { 
			CandidateList candidateList = this.forIdentRestWebService.getCandidatesByIdentifier(new String[] {identifier});
			if(candidateList != null && candidateList.getNumberElements() != 0) return candidateList.getElement(0);
			else return null;
		}
		return this.candidateList.getElement(0);
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public CandidateList getCandidateByIdentifier(java.util.Vector<String> identifiers) throws Exception {
		if(this.candidateList == null) {
			return this.forIdentRestWebService.getCandidatesByIdentifier(identifiers);
		}
		CandidateList candidateList = new CandidateList();
		java.util.Vector<String> identifiersFound = new java.util.Vector<String>();
		for(String id : identifiers) {
			for(int i = 0; i < this.candidateList.getNumberElements(); i++) {
				if(this.candidateList.getElement(i).getIdentifier().equals(id) && !identifiersFound.contains(id)) {
					candidateList.addElement(this.candidateList.getElement(i));
					identifiersFound.add(id);
					break;
				}
			}
		}
		return candidateList;
	}

	public void nullify() {
		this.candidateList = null;
		this.forIdentRestWebService = null;
	}

}
