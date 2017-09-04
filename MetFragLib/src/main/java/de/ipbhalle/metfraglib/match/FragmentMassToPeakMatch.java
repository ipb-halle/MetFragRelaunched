package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IPeak;

public class FragmentMassToPeakMatch extends DefaultFragmentToPeakMatch {
	
	protected java.util.ArrayList<Double> matchedFragmentMassesToTandemMassPeak;
	protected double matchedFragmentMassOfBestFragment;
	
	public FragmentMassToPeakMatch(IPeak matchedPeak) {
		super(matchedPeak);
		this.matchedFragmentMassesToTandemMassPeak = new java.util.ArrayList<Double>();
		this.numberOfHydrogensDifferToPeakMass = new java.util.ArrayList<Byte>();
	}

	public double getBestMatchFragmentMass() {
		return this.matchedFragmentMassOfBestFragment;
	}
	
	public double getMatchedFragmentMassToTandemMassPeak(int matchedFragmentIndex) {
		return this.matchedFragmentMassesToTandemMassPeak.get(matchedFragmentIndex);
	}
	
	public void initialiseBestMatchedFragment(int index) {
		super.initialiseBestMatchedFragment(index);
		this.matchedFragmentMassOfBestFragment = this.matchedFragmentMassesToTandemMassPeak.get(index);
	}
	
	public void addMatchedFragment(IFragment matchedFragment, byte numberHydrogensDifferToPeakMass, double matchedFragmentMassToPeak) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.matchedFragmentMassesToTandemMassPeak.add(matchedFragmentMassToPeak);
		this.numberOfHydrogensDifferToPeakMass.add(numberHydrogensDifferToPeakMass);
		this.fragmentAdductTypeIndeces.add((byte)0);
	}

	public void addMatchedFragment(IFragment matchedFragment, byte numberHydrogensDifferToPeakMass, double matchedFragmentMassToPeak, byte adductTypeIndex) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.matchedFragmentMassesToTandemMassPeak.add(matchedFragmentMassToPeak);
		this.numberOfHydrogensDifferToPeakMass.add(numberHydrogensDifferToPeakMass);
		this.fragmentAdductTypeIndeces.add(adductTypeIndex);
	}
	
	public void addToMatch(IMatch match) {
		FragmentMassToPeakMatch currentMatch = (FragmentMassToPeakMatch)match;
		
		FastBitArray atomsFastBitArrayOfCurrentFragment = ((AbstractTopDownBitArrayFragment)currentMatch.getMatchedFragmentList().getElement(0)).getAtomsFastBitArray();
		for(int i = 0; i < this.matchedFragmentsList.getNumberElements(); i++) {
			AbstractTopDownBitArrayFragment tmpFragment = (AbstractTopDownBitArrayFragment)this.matchedFragmentsList.getElement(i);
			if(tmpFragment.getAtomsFastBitArray().equals(atomsFastBitArrayOfCurrentFragment))
					return;
		}
		if(match.getBestMatchedFragment() != null) {
			this.addMatchedFragment(
				currentMatch.getBestMatchedFragment(), 
				(byte)currentMatch.getBestMatchedFragmentHydrogenDifference(),
				currentMatch.getBestMatchFragmentMass(),
				currentMatch.getBestMatchedFragmentAdductTypeIndex());
		}
		else {
			this.addMatchedFragment(
				currentMatch.getMatchedFragmentList().getElement(0), 
				(byte)currentMatch.getNumberOfHydrogensDifferToPeakMass(0),
				currentMatch.getMatchedFragmentMassToTandemMassPeak(0),
				currentMatch.getFragmentsAdductTypeIndeces().get(0));
		}
	}

	public void shallowNullify() {
		super.shallowNullify();
		this.matchedFragmentMassesToTandemMassPeak = null;
	}
	
	public void nullify() {
		super.nullify();
		this.matchedFragmentMassesToTandemMassPeak = null;
	}
}
