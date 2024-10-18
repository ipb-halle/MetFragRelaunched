package de.ipbhalle.metfragweb.datatype;

import java.util.List;

import jakarta.faces.model.SelectItem;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

public class CandidateStatistics {
	
	protected LineChartModel scoreDistributionModel;
	protected String scoreDistributionModelPointLabels;
	protected boolean showPointLabels;
	protected int selectedCandidate = 0;
	protected int[] showScoreGraphs;
	protected String[] showScoreGraphsString;
	protected String[] scoreGraphNames;
	protected String legendLabels;
	
	public CandidateStatistics() {
		this.legendLabels = "";
		this.showPointLabels = false;
		this.showScoreGraphs = new int[0];
		this.scoreGraphNames = new String[0];
	}
	
	public void generateScoreDistributionModelView(List<MetFragResult> results) {
		if(results.size() <= 1) {
			this.scoreDistributionModel = null;
			return;
		}
		this.scoreDistributionModel = new LineChartModel();
		this.scoreDistributionModel.getAxis(AxisType.X).setLabel("Candidate Index");
		this.scoreDistributionModel.getAxis(AxisType.Y).setLabel("Score");
		this.scoreDistributionModel.setExtender("extenderScore");
		LineChartSeries series1 = new LineChartSeries();
		series1.setLabel("Final Score");
		this.legendLabels = "['Final Score'";
		LineChartSeries[] scores = new LineChartSeries[this.showScoreGraphs.length];
		for(int k = 0; k < this.showScoreGraphs.length; k++) {
			scores[k] = new LineChartSeries();
			scores[k].setShowMarker(false);
			scores[k].setLabel(this.scoreGraphNames[k]);
			this.legendLabels += ",'" + this.scoreGraphNames[k] + "'";
		}
		this.scoreDistributionModel.setMouseoverHighlight(false);
		this.scoreDistributionModel.setShowDatatip(false);
		this.scoreDistributionModelPointLabels = "['" + results.get(0).getOriginalIdentifier() + "'";
		series1.set(1, results.get(0).getScore());
		for(int k = 0; k < this.showScoreGraphs.length; k++) 
			scores[k].set(1, results.get(0).getRoot().getSingleScore(this.showScoreGraphs[k]));
		for(int i = 1; i < results.size(); i++) {
			series1.set((i + 1), results.get(i).getScore());
			this.scoreDistributionModelPointLabels += ",'" + results.get(i).getOriginalIdentifier() + "'";
			for(int k = 0; k < this.showScoreGraphs.length; k++) 
				scores[k].set((i + 1), results.get(i).getRoot().getSingleScore(this.showScoreGraphs[k]));
		}
		this.scoreDistributionModel.addSeries(series1);
		for(int k = 0; k < this.showScoreGraphs.length; k++) {
			this.scoreDistributionModel.addSeries(scores[k]);
		}
		this.scoreDistributionModelPointLabels += "]";
		this.legendLabels += "]";
	}

	public LineChartModel getScoreDistributionModel() {
		return this.scoreDistributionModel;
	}

	public String getScoreDistributionModelPointLabels() {
		return this.scoreDistributionModelPointLabels;
	}

	public String getLegendLabels() {
		return this.legendLabels;
	}
	
	public int getSelectedCandidate() {
		return this.selectedCandidate;
	}
	
	public String[] getShowScoreGraphs() {
		return this.showScoreGraphsString;
	}

	public void setShowScoreGraphs(String[] showScoreGraphsString, java.util.List<SelectItem> availableScoreNamesForScoreGraph) {
		this.showScoreGraphsString = showScoreGraphsString;
		this.showScoreGraphs = new int[showScoreGraphsString.length];
		this.scoreGraphNames = new String[showScoreGraphsString.length];
		for(int i = 0; i < this.showScoreGraphs.length; i++) {
			this.showScoreGraphs[i] = Integer.parseInt(showScoreGraphsString[i]);
			this.scoreGraphNames[i] = availableScoreNamesForScoreGraph.get(this.showScoreGraphs[i]).getLabel();
		}
	}

	public void setSelectedCandidate(int selectedCandidate) {
		this.selectedCandidate = selectedCandidate;
	}

	public boolean isShowPointLabels() {
		return this.showPointLabels;
	}
	
	public void setShowPointLabels(boolean showPointLabels) {
		this.showPointLabels = showPointLabels;
	}
}
