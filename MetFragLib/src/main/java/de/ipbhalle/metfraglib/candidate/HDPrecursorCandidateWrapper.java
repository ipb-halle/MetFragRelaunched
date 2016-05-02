package de.ipbhalle.metfraglib.candidate;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.precursor.HDTopDownBitArrayPrecursor;

public class HDPrecursorCandidateWrapper {
	
	protected ICandidate candidate;
	
	public HDPrecursorCandidateWrapper(ICandidate candidate) {
		this.candidate = candidate;
	}
	
	public void initialisePrecursorCandidate() throws Exception {
		this.candidate.setPrecursorMolecule( 
				new HDTopDownBitArrayPrecursor(
						this.candidate.getImplicitHydrogenAtomContainer(), 
						this.candidate.getProperty(VariableNames.HD_NUMBER_EXCHANGED_HYDROGENS) != null ? (Byte)this.candidate.getProperty(VariableNames.HD_NUMBER_EXCHANGED_HYDROGENS) : (byte)0));
		this.candidate.getPrecursorMolecule().preprocessPrecursor();	
	}

	public void setProperty(String key, Object object) {
		this.candidate.setProperty(key, object);
	}
	
	public Object getProperty(String key) {
		return this.candidate.getProperty(key);
	}
}
