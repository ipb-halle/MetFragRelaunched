package de.ipbhalle.metfraglib.assigner;

import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class FragmentToTandemMassPeakAssigner extends AbstractAssigner {
	
	protected FragmentList fragmentList;
	protected SortedTandemMassPeakList tandemMassPeakList;
	
	protected Double allowedRelativeMassDeviation;
	protected Double allowedAbsoluteMassDeviation;
	
	protected Boolean considerHydrogenMassDifference;
	protected Boolean positiveMode;
	protected Integer precursorIonType;
	
	public FragmentToTandemMassPeakAssigner(Settings settings) {
		super(settings);
		this.precursorIonType = (Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
		this.positiveMode = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		this.considerHydrogenMassDifference = (Boolean)settings.get(VariableNames.CONSIDER_HYDROGEN_SHIFTS_NAME);
		this.allowedAbsoluteMassDeviation = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		this.allowedRelativeMassDeviation = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		this.tandemMassPeakList = (SortedTandemMassPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
	}

	public MatchList assign() {
		MatchList matchList = new MatchList();
		/*
		 * check every tandem mass peak
		 * a peak can be matched by several grouped fragment lists, which is likely by using hydrogen differences when matching
		 * but only one match can be defined for one peak so a match can contain several grouped fragment lists
		 */
		for(int i = 0; i < this.tandemMassPeakList.getNumberElements(); i++) {
			de.ipbhalle.metfraglib.peak.TandemMassPeak currentTandemMassPeak = (de.ipbhalle.metfraglib.peak.TandemMassPeak)this.tandemMassPeakList.getElement(i);
			/*
			 * initialise mass interval of proper match
			 */
			currentTandemMassPeak.setMassLimitsByMassDeviations(this.allowedRelativeMassDeviation, this.allowedAbsoluteMassDeviation);
			/*
			 * check every generated fragment
			 */
			boolean matched = false;
			IMatch[] match = new IMatch[1];
			for(int j = 0; j < fragmentList.getNumberElements(); j++) {
				if(fragmentList.getElement(j).matchToPeak(currentTandemMassPeak, this.precursorIonType, this.positiveMode, match) == 0) matched = true;
			}
			if(matched) matchList.addElement(match[0]);
		}
		return matchList;
	}


	public double getAllowedRelativeMassDeviation() {
		return allowedRelativeMassDeviation;
	}

	public void setAllowedRelativeMassDeviation(double allowedRelativeMassDeviation) {
		this.allowedRelativeMassDeviation = allowedRelativeMassDeviation;
	}

	public double getAllowedAbsoluteMassDeviation() {
		return allowedAbsoluteMassDeviation;
	}

	public void setAllowedAbsoluteMassDeviation(double allowedAbsoluteMassDeviation) {
		this.allowedAbsoluteMassDeviation = allowedAbsoluteMassDeviation;
	} 
	
	
	public void nullify() {
		// TODO Auto-generated method stub
		
	}

	public void setFragmentList(IList list) {
		this.fragmentList = (FragmentList)list;
	}

}
