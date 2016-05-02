package de.ipbhalle.metfraglib.assigner;

import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SiriusNodePeakList;
import de.ipbhalle.metfraglib.match.FragmentFormulaToPeakMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class FragmentToFormulaPeakAssigner extends AbstractAssigner {

	private FragmentList fragmentList;
	protected SiriusNodePeakList peakList;
	
	public FragmentToFormulaPeakAssigner(Settings settings) {
		super(settings);
		this.peakList = (SiriusNodePeakList)this.settings.get(VariableNames.PEAK_LIST_NAME);
	}

	public MatchList assign() {
		MatchList matchList = new MatchList();
		/*
		 * check every formula peak
		 * a peak can be matched by several grouped fragment lists, which is likely by using hydrogen differences when matching
		 * but only one match can be defined for one peak so a match can contain several grouped fragment lists
		 */
		for(int i = 0; i < this.peakList.getNumberElements(); i++) {
			FragmentFormulaToPeakMatch fragmentPeakMatch = null;
			/*
			 * retrieve current formula peak
			 */
			de.ipbhalle.metfraglib.peak.SiriusNodePeak currentFormulaPeak = (de.ipbhalle.metfraglib.peak.SiriusNodePeak)this.peakList.getElement(i);
			/*
			 * if current peak is the precursor peak then continue with the next one
			 */
			if(currentFormulaPeak.isRoot()) continue;
			/*
			 * check every generated fragment
			 */
			for(int j = 0; j < fragmentList.getNumberElements(); j++) {
				/*
				 * fetch the current fragment list from the groupedFragmentList
				 */
				IFragment currentFragment = fragmentList.getElement(j);
				/*
				 * fetch the current formula representing the current fragment list
				 */
				IMolecularFormula fragmentListFormula = currentFragment.getMolecularFormula();
				/*
				 * check if fragment matches to peak based on the formula without hydrogen consideration
				 */
				if(currentFormulaPeak.getMolecularFormula().compareTo(fragmentListFormula, currentFragment.getTreeDepth())) {
					/*
					 * if a former fragment has matched already then add the current fragment list to the match object
					 */
					if(fragmentPeakMatch != null) 
						fragmentPeakMatch.addMatchedFragment(currentFragment, 
							(byte)currentFormulaPeak.getMolecularFormula().getHydrogenDifference(fragmentListFormula)); 
					else {
						fragmentPeakMatch = new FragmentFormulaToPeakMatch(currentFormulaPeak);
						fragmentPeakMatch.addMatchedFragment(currentFragment, (byte)currentFormulaPeak.getMolecularFormula().getHydrogenDifference(fragmentListFormula));
					}
				}
			}
			if(fragmentPeakMatch != null) matchList.addElement(fragmentPeakMatch);
		}
		return matchList;
	}

	public void nullify() {
	}

	public void setFragmentList(IList list) {
		this.fragmentList = (FragmentList)list;
	}

}
