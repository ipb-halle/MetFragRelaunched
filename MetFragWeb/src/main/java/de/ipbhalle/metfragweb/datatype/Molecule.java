package de.ipbhalle.metfragweb.datatype;

import java.io.Serializable;
import java.util.List;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.PrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class Molecule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7059246953749018121L;
	
	protected String identifier;
	protected double mass;
	protected String formula;
	protected double finalScore;
	protected String imageAddress;
	protected String name;
	protected String inchi;

	protected int numberPeaksExplained;
	
	protected String displayFormula;
	//scores for final score calculation and to display
	protected ScoreSummary[] scoresSummaries;

	protected String[] additionalValueNames;
	protected String[] additionalValueNamesShort;
	protected int[] additionalValues;
	protected MatchList matchList;
	
	protected HorizontalBarChartModel horizontalBarModel;
	protected String databaseName;
	protected Integer simScoreIndex;
	
	public Molecule(String identifier) {
		this.identifier = identifier;
	}

	public Molecule(String identifier, double mass, String formula, final List<Weight> weights, 
			String imageAddress, final ScoreSummary[] scoresSummaries, String inchi) {
		super();
		this.additionalValues = new int[] {100, 200, 110};
		
		this.identifier = identifier;
		this.mass = mass;
		this.formula = formula;
		this.displayFormula = this.formula.replaceAll("([0-9]+)", "<sub>$1</sub>");
		
		this.scoresSummaries = scoresSummaries;
		
		this.recalculateScore(weights);
		this.inchi = inchi;
		this.imageAddress = imageAddress;
		
		this.horizontalBarModel = new HorizontalBarChartModel();
		ChartSeries scoreCharts = new ChartSeries();
		scoreCharts.setLabel("Scores");
		
		int scoreNotForGraphNumber = 0;
		int numberNotForScore = 0;
		for(int i = this.scoresSummaries.length - 1; i >= 0; i--)
			if(!this.scoresSummaries[i].isUsedForScoring()) numberNotForScore++;
    	for(int i = this.scoresSummaries.length - 1; i >= 0; i--) {
    		if(!this.scoresSummaries[i].isUsedForScoring()) {
    			scoreNotForGraphNumber++;
    			continue;  
    		}
    		String end = "th";
    		int scoreIndex = i - numberNotForScore + 1 + scoreNotForGraphNumber;
    		if(scoreIndex == 1) end = "st";
    		if(scoreIndex == 2) end = "nd";
    		if(scoreIndex == 3) end = "rd";
    		scoreCharts.set(scoreIndex + end, scoresSummaries[i].getValue());
        }
    	this.horizontalBarModel.addSeries(scoreCharts);
    	this.horizontalBarModel.setStacked(false);
    	this.horizontalBarModel.setShowPointLabels(true);
    	this.horizontalBarModel.setMouseoverHighlight(true);
    	this.horizontalBarModel.setShowDatatip(false);

    	this.horizontalBarModel.setExtender("extender");
    	if(this.scoresSummaries.length <= 3) this.horizontalBarModel.setBarWidth(30);
    	
    	
    	this.horizontalBarModel.getAxis(AxisType.Y).setTickAngle(-45);
    	
	}
	
	public void recalculateScore(List<Weight> weights) {
		this.finalScore = 0.0;
		int weightIndex = 0;
		int scoreIndex = 0;
		while(scoreIndex < this.scoresSummaries.length) {
			if(this.scoresSummaries[scoreIndex].isUsedForScoring()) {
				this.finalScore += ((double)weights.get(weightIndex).getValue() / 100.0) * this.scoresSummaries[scoreIndex].getValue();
				weightIndex++;
			}
			scoreIndex++;
		}
		this.finalScore = MathTools.round(this.finalScore, 4);
	}
	
	public ScoreSummary[] getScoreSummary() {
		return this.scoresSummaries;
	}
	
    public HorizontalBarChartModel getHorizontalScoreModel() {
        return this.horizontalBarModel;
    }
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getDisplayFormula() {
		return displayFormula;
	}

	public void setDisplayFormula(String displayFormula) {
		this.displayFormula = displayFormula;
	}

	public double getScore() {
		return finalScore;
	}

	public String getInChI() {
		return inchi;
	}

	public void setInChI(String inchi) {
		this.inchi = inchi;
	}

	public void setScore(double score) {
		this.finalScore = score;
	}
	
	public String getImageAddress() {
		return this.imageAddress;
	}
	
	public boolean isDatabaseLinkAvailable() {
		if(databaseName == null) return false;
		if(databaseName.equals("PubChem") || databaseName.equals("KEGG") 
				|| databaseName.equals("ChemSpider") || databaseName.equals("MetaCyc")
				|| databaseName.equals("LipidMaps") || databaseName.equals("LocalHMDB")
				|| databaseName.equals("LocalChEBI")) return true;
		return false;
	}
	
	public String getDatabase() {
		return databaseName;
	}
	
	public String getDatabaseLink() {
		if(databaseName.equals("PubChem")) return "https://pubchem.ncbi.nlm.nih.gov/compound/" + this.identifier;
		else if(databaseName.equals("KEGG")) return "http://www.kegg.jp/dbget-bin/www_bget?cpd:" + this.identifier;
		else if(databaseName.equals("ChemSpider")) return "http://www.chemspider.com/Chemical-Structure." + this.identifier + ".html";
		else if(databaseName.equals("MetaCyc")) return "http://metacyc.org/META/NEW-IMAGE?type=COMPOUND&object=" + this.identifier;
		else if(databaseName.equals("LipidMaps")) return "http://www.lipidmaps.org/data/LMSDRecord.php?LMID=" + this.identifier;
		else if(databaseName.equals("LocalHMDB")) return "http://www.hmdb.ca/metabolites/" + this.identifier;
		else if(databaseName.equals("LocalChEBI")) return "https://www.ebi.ac.uk/chebi/searchId.do?chebiId=" + this.identifier;
		return this.identifier; 
	}
	
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	public void setImageAddress(String imageAddress) {
		this.imageAddress = imageAddress;
	}

	public String[] getAdditionalValueNames() {
		return this.additionalValueNames;
	}

	public void setAdditionalValueNames(String[] additionalValueNames) {
		this.additionalValueNames = additionalValueNames;
	}
	
	@Override
	public boolean equals(Object mol) {
		return this.identifier.equals(((Molecule)mol).getIdentifier());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int[] getAdditionalValues() {
		return additionalValues;
	}
	
	public int getNumberPeaksExplained() {
		return numberPeaksExplained;
	}

	public void setNumberPeaksExplained(int numberPeaksExplained) {
		this.numberPeaksExplained = numberPeaksExplained;
	}

	public void setAdditionalValues(int[] additionalValues) {
		this.additionalValues = additionalValues;
	}
	
	public MatchList getMatchList() {
		return this.matchList; 
	}
	
	public void setMatchList(MatchList matchList) {
		this.matchList = matchList; 
	}
	
	public double getSingleScore(int index) {
		return this.scoresSummaries[index].getValue();
	}

	public void setSimScoreIndex(Integer simScoreIndex) {
		this.simScoreIndex = simScoreIndex;
	}
	
	/**
	 * convert the MetFragWeb molecule to a scored candidate
	 * provides opportunity to be stored as candidate list by
	 * an IWriter
	 * 
	 * @return
	 */
	public ICandidate getCandidate() {
		ICandidate candidate = new PrecursorCandidate(this.inchi, this.identifier);
		candidate.setProperty(VariableNames.FINAL_SCORE_COLUMN_NAME, this.finalScore);
		candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, this.mass);
		candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, this.formula);
		candidate.setProperty(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN, this.numberPeaksExplained);
		if(this.name != null && this.name.length() != 0) candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, this.name);
		candidate.setMatchList(this.matchList);
		
		for(int i = 0; i < this.scoresSummaries.length; i++) {
			candidate.setProperty(this.scoresSummaries[i].getName(), String.valueOf(this.scoresSummaries[i].getRawValue()));
		}
		
		return candidate;
	}
	
	public double getSimScore() {
		try {
			if(this.simScoreIndex != null) return this.scoresSummaries[this.simScoreIndex].getValue();
			else return 0.0;
		}
		catch(Exception e) {
			System.out.println(this.scoresSummaries.length + " " + this.simScoreIndex);
			return 0.0;
		}
	}
}
