package de.ipbhalle.metfraglib.candidate;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.precursor.TopDownBitArrayPrecursor;

public class TopDownPrecursorCandidate extends PrecursorCandidate {

	public TopDownPrecursorCandidate(String inchi, String identifier) {
		super(inchi, identifier);
	}

	@Override
	public void initialisePrecursorCandidate() throws AtomTypeNotKnownFromInputListException, Exception {
		this.precursorStructure = new TopDownBitArrayPrecursor(this.getImplicitHydrogenAtomContainer());
		this.precursorStructure.preprocessPrecursor();
	}
	
	public TopDownPrecursorCandidate clone() {
		TopDownPrecursorCandidate clone = new TopDownPrecursorCandidate(this.getInChI(), this.getIdentifier());
		java.util.Enumeration<String> keys = this.properties.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			clone.setProperty(key, this.properties.get(key));
		}
		return clone;
	}
}
