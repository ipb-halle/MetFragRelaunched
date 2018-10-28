package de.ipbhalle.metfraglib.list;

public abstract class AbstractPeakList extends DefaultList {

	public abstract int getNumberPeaksUsed();
	
	public abstract boolean containsMass(double mass, double mzppm, double mzabs);

	public abstract Double getBestMatchingMass(double mass, double mzppm, double mzabs);
	
}
