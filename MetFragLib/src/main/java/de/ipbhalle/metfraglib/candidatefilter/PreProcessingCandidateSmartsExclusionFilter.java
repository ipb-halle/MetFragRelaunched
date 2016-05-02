package de.ipbhalle.metfraglib.candidatefilter;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class PreProcessingCandidateSmartsExclusionFilter extends AbstractPreProcessingCandidateFilter {
	
	private String[] includedSmarts;
	private SMARTSQueryTool[] smartsQuerytools;
	
	public PreProcessingCandidateSmartsExclusionFilter(Settings settings) {
		super(settings);
		try {
			this.includedSmarts = (String[])settings.get(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME);
		}
		catch(NullPointerException e) {
			this.includedSmarts = null;
		}
		catch(ClassCastException e) {
			this.includedSmarts = null;
		}
		if(this.includedSmarts != null && this.includedSmarts.length != 0) {
			this.smartsQuerytools = new SMARTSQueryTool[this.includedSmarts.length];
			for(int i = 0; i < this.includedSmarts.length; i++) {
				smartsQuerytools[i] = new SMARTSQueryTool(this.includedSmarts[i], DefaultChemObjectBuilder.getInstance());
			}
		}
		
	}

	public boolean passesFilter(ICandidate candidate) {
		if(this.includedSmarts == null) return true;
		if(this.smartsQuerytools == null || this.smartsQuerytools.length == 0) return true;
		
		for(int i = 0; i < this.smartsQuerytools.length; i++)
			try {
				if(this.smartsQuerytools[i].matches(candidate.getAtomContainer())) 
					return false;
			} catch (CDKException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		return true;
	}

	public void nullify() {
		this.includedSmarts = null;
		this.smartsQuerytools = null;
	}
}
