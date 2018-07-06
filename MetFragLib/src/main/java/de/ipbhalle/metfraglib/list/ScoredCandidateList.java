package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.exceptions.ScorePropertyNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class ScoredCandidateList extends CandidateList {

	protected int numberPeaksUsed = 0;
	
	public void addElement(ICandidate candidate) {
		this.list.add(candidate);
	}
	
	public ICandidate getElement(int index) {
		return (ICandidate)this.list.get(index);
	}
	
	public SortedScoredCandidateList normaliseScores(Double[] weights, String[] scoreNames, String[] scoreValuesNotToScale) throws ScorePropertyNotDefinedException {
		if(this.getNumberElements() == 0)
			return new SortedScoredCandidateList();
		double[][] normalisedValues = new double[scoreNames.length][this.getNumberElements()];
		/*
		 * calculating normalised values and storing in normalisedValues matrix
		 */
		for(int i = 0; i < normalisedValues.length; i++) {
			//check whether to scale that value
			boolean scaleValue = true;
			if(scoreValuesNotToScale != null) 
				for(int l = 0; l < scoreValuesNotToScale.length; l++)
					if(scoreValuesNotToScale[l].equals(scoreNames[i])) scaleValue = false;
			
			Double maximumScore = (double)Integer.MIN_VALUE;
			Double[] scoreValues = new Double[this.getNumberElements()];
			for(int ii = 0; ii < normalisedValues[0].length; ii++) {
				ICandidate currentCandidate = this.getElement(ii);
				/*
				 * get the score value from the property fields of the candidates
				 */
				try {
					scoreValues[ii] = (Double)currentCandidate.getProperty(scoreNames[i]);
				//	System.out.println(currentCandidate.getProperty("InChIKey1") + " " + scoreNames[i] + " " + scoreValues[ii]);
				}
				catch(java.lang.ClassCastException e) {
					scoreValues[ii] = Double.parseDouble(this.convertScoreValue((String)currentCandidate.getProperty(scoreNames[i])));
				}
				if(scoreValues[ii] == null) throw new ScorePropertyNotDefinedException(scoreNames[i] + " not known or defined for " + currentCandidate.getIdentifier());
				if(scoreValues[ii] > maximumScore) maximumScore = scoreValues[ii];
			}
			boolean negativeValues = false;
			if(maximumScore < 0) {
				negativeValues = true;
				maximumScore = 1.0 / Math.abs(maximumScore);
			}
			
			for(int ii = 0; ii < normalisedValues[0].length; ii++) {
				if(maximumScore != 0.0) {
					normalisedValues[i][ii] = scoreValues[ii];
					if(scaleValue) {
						if(!negativeValues) normalisedValues[i][ii] /= maximumScore;
						else {
							normalisedValues[i][ii] = (1.0 / Math.abs(normalisedValues[i][ii])) / maximumScore;
						}
					}
				}
			}
		}
	
		/*
		 * setting normalised and summed scores in the score collection and generating the SortedScoredCandidateList
		 */
		SortedScoredCandidateList sortedScoredCandidateList = new SortedScoredCandidateList();
		for(int i = 0; i < this.getNumberElements(); i++) {
			Double combinedNormalisedValue = new Double(0);
			for(int ii = 0; ii < normalisedValues.length; ii++) {
				combinedNormalisedValue += normalisedValues[ii][i] * weights[ii];
			} 
			this.getElement(i).setProperty(VariableNames.FINAL_SCORE_COLUMN_NAME, combinedNormalisedValue);
			sortedScoredCandidateList.addElement(this.getElement(i));
		}
		return sortedScoredCandidateList;
	}
	
	public String convertScoreValue(String value) {
		if(value == null) return "0.0";
		if(value.equals("-")) return "0.0";
		if(value.equals("NA")) return "0.0";
		return value;
	}
	
	public int getNumberPeaksUsed() {
		return this.numberPeaksUsed;
	}
	
	public void setNumberPeaksUsed(int numberPeaksUsed) {
		this.numberPeaksUsed = numberPeaksUsed;
	}
}
