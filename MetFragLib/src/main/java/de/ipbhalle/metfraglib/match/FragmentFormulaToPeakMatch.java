package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.peak.SiriusNodePeak;

public class FragmentFormulaToPeakMatch extends DefaultFragmentToPeakMatch {

	public FragmentFormulaToPeakMatch(IPeak matchedPeak) {
		super(matchedPeak);
	}
	
	public SiriusNodePeak getMatchedPeak() {
		return (SiriusNodePeak)this.matchedPeak;
	}
	
	public void addMatchedFragment(IFragment matchedFragment, byte numberHydrogensDifferToPeakMass) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.numberOfHydrogensDifferToPeakMass.add(numberHydrogensDifferToPeakMass);
	}

	public void addMatchedFragment(IFragment matchedFragment) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.numberOfHydrogensDifferToPeakMass.add((byte)0);
	}
	
}
