package de.ipbhalle.metfraglib.score;

import org.apache.log4j.Logger;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class CombinedReferenceScore extends AbstractScore {

	private Logger logger = Logger.getLogger(AbstractScore.class);
	
	protected ICandidate scoredCandidate;
	
	public CombinedReferenceScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.scoredCandidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
	}
	
	public void calculate() {
		this.value = 0.0;
		String[] referenceNames = (String[])this.settings.get(VariableNames.COMBINED_REFERENCE_SCORE_VALUES);
		for(int i = 0; i < referenceNames.length; i++)
			if(this.scoredCandidate.getProperties().containsKey(referenceNames[i]))
				try {
					this.value += Double.parseDouble((String)this.scoredCandidate.getProperty(referenceNames[i]));
				} catch(Exception e1) {
					try {
						this.value += (Double)this.scoredCandidate.getProperty(referenceNames[i]);
					}
					catch(Exception e2) {
						this.value += 0.0;
					}
				}
			else
				this.logger.warn("Candidate with identifier " + this.scoredCandidate.getIdentifier() + " has no property value for " + referenceNames[i] + ".");
	}

	public void setOptimalValues(double[] values) {
		this.optimalValues[0] = values[0];
	}
	
	public Double[] calculateSingleMatch(IMatch match) {
		return new Double[] {0.0, null};
	}
	
	@Override
	public void nullify() {
		super.nullify();
	}

	public boolean isBetterValue(double value) {
		return value > this.value ? true : false;
	}
}
