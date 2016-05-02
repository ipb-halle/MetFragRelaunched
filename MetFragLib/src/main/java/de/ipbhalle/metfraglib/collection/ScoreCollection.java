package de.ipbhalle.metfraglib.collection;

import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IScore;

public class ScoreCollection {

	private IScore[] scores;
	private double value;
	
	public ScoreCollection() {}
	
	public ScoreCollection(IScore[] scores) {
		this.scores = scores;
	}
	
	public IScore getScore(int index) {
		return this.scores[index];
	}

	public int getNumberScores() {
		return this.scores.length;
	}
	
	public double getValue() {
		return this.value;
	}

	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
	public void calculate() throws Exception {
		for(int i = 0; i < this.scores.length; i++)
			this.scores[i].calculate();
	}

	public Double[][] calculateSingleMatch(IMatch match) {
		Double[][] values = new Double[2][this.scores.length + 1];
		Double sumValue = 0.0;
		/*
		 * first value of array is the sum of all values
		 */
		for(int i = 0; i < this.scores.length; i++) {
			Double[] tmp = this.scores[i].calculateSingleMatch(match);
			values[0][i + 1] = tmp[0];
			values[1][i + 1] = tmp[1];
			sumValue += values[0][i + 1];
		}
		/*
		 * this is the actual score value used for ranking
		 */
		values[0][0] = sumValue;
		values[1][0] = null;
		return values;
	}
	
	public String toString() {
		String string = this.scores[0].getValue() + "";
		for(int i = 1; i < this.scores.length; i++)
			string += " " + this.scores[i].getValue();
		return string;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public void initNumberScores(int numberScores) {
		this.scores = new IScore[numberScores];
	}
	
	public void setScore(int index, IScore score) {
		this.scores[index] = score;
	}

	public void shallowNullify() {
		if(this.scores != null)
			for(int i = 0; i < this.scores.length; i++)
				if(this.scores[i] != null) this.scores[i].shallowNullify();
	}
	
	public void nullify() {
		if(this.scores != null)
			for(int i = 0; i < this.scores.length; i++)
				if(this.scores[i] != null) this.scores[i].nullify();
		this.scores = null;
	}
	
}
