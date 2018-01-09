package de.ipbhalle.metfragweb.datatype;

import de.ipbhalle.metfraglib.additionals.MathTools;

public class ScoreSummary {

	protected String name;
	protected double value;
	protected double rawValue;
	protected boolean usedForScoring;
	protected boolean usedForGraph;
	protected boolean databaseScore;
	protected String info;
	
	public ScoreSummary(String name, double value, double rawValue, String info, boolean databaseScore) {
		this.name = name;
		this.value = value;
		this.rawValue = rawValue;
		this.info = info;
		this.usedForGraph = true;
		this.usedForScoring = true;
		this.databaseScore = databaseScore;
	}

	public boolean isDatabaseScore() {
		return databaseScore;
	}

	public void setDatabaseScore(boolean databaseScore) {
		this.databaseScore = databaseScore;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRoundedValue() {
		return MathTools.round(this.value, 4);
	}
	
	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getRawValue() {
		return rawValue;
	}

	public double getRoundedRawValue() {
		return MathTools.round(this.rawValue, 4);
	}
	
	public void setRawValue(double rawValue) {
		this.rawValue = rawValue;
	}

	public boolean isUsedForScoring() {
		return usedForScoring;
	}

	public void setUsedForScoring(boolean usedForScoring) {
		this.usedForScoring = usedForScoring;
	}

	public boolean isUsedForGraph() {
		return usedForGraph;
	}

	public void setUsedForGraph(boolean usedForGraph) {
		this.usedForGraph = usedForGraph;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
}
