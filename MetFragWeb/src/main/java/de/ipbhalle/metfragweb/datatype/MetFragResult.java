package de.ipbhalle.metfragweb.datatype;

import org.primefaces.model.chart.HorizontalBarChartModel;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfragweb.datatype.Molecule;

import java.io.Serializable;
import java.util.Locale;

public class MetFragResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2153965391558700716L;
	public Molecule root;
	public java.util.Vector<Molecule> molecules;
	public java.util.List<String> identifiers;
	public String inchikey1 = null;
	public int index;
	//if true it won't be displayed
	public boolean isFiltered;
	
	public MetFragResult(Molecule root, String inchikey1, int index) {
		this.isFiltered = false;
		this.root = root;
		this.inchikey1 = inchikey1;
		this.index = index;
		this.identifiers = new java.util.ArrayList<String>();
		this.identifiers.add(this.root.getIdentifier());
	}

	public MetFragResult(Molecule root, int index) {
		this.root = root;
		this.index = index;
		this.identifiers = new java.util.ArrayList<String>();
		this.identifiers.add(this.root.getIdentifier());
	}

	public void addMolecule(Molecule molecule) {
		if(this.root == null) {
			this.root = molecule;
			this.identifiers = new java.util.ArrayList<String>();
			this.identifiers.add(this.root.getIdentifier());
		}
		else {
			if(this.molecules == null) this.molecules = new java.util.Vector<Molecule>();
			if(this.root.getScore() < molecule.getScore()) {
				this.molecules.add(this.root);
				this.root = molecule;
			}
			else this.molecules.add(molecule);
			this.identifiers.add(molecule.getIdentifier());
		}
	}
	
	public void recalculateScore(java.util.List<Weight> weights) {
		this.root.recalculateScore(weights);
		double maxScore = this.root.getScore();
		int maxIndex = -1;
		if(this.molecules != null) {
			for(int i = 0; i < this.molecules.size(); i++) {
				this.molecules.get(i).recalculateScore(weights);
				if(this.molecules.get(i).getScore() > maxScore) {
					maxIndex = i;
					maxScore = this.molecules.get(i).getScore();
				}
			}
			if(maxIndex != -1) {
				this.molecules.add(this.root);
				this.root = this.molecules.get(maxIndex);
				this.molecules.remove(maxIndex);
			}
		}
	}
	
	public boolean isFiltered() {
		return isFiltered;
	}

	public void setFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}

	public boolean isHasMolecules() {
		return ! (this.molecules == null || this.molecules.size() == 0);
	}
	
	public double getMass() {
		return this.root.getMass();
	}
	
	public double getRoundedMass() {
		return MathTools.round(this.root.mass);
	}
	
	public String getName() {
		return this.root.getName();
	}

	public int getIndex() {
		return this.index;
	}
	
	public double getScore() {
		return this.root.getScore();
	}
	
	public boolean filterByIdentifier(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim();
        if(filterText == null || filterText.equals("")) {
            return true;
        }
        if(value == null) {
            return false;
        }
        java.util.ArrayList<?> list = (java.util.ArrayList<?>)value;
        for(int i = 0; i < list.size(); i++) {
        	if(((String)list.get(i)).equals(filterText)) return true;
        }
        return false;
    }
	
	public String getDisplayFormula() {
		return this.root.getDisplayFormula();
	}
	
	public String getIdentifier() {
		return this.root.getIdentifier();
	}

	public String getOriginalIdentifier() {
		return this.root.getOriginalIdentifier();
	}

	public java.util.List<String> getIdentifiers() {
		return this.identifiers;
	}
	
	public String getMultipleIdentifiers() {
		String ids = this.root.getIdentifier();
		if(this.molecules != null)
			for(int i = 0; i < this.molecules.size(); i++) {
				ids += "<br/ >" + this.molecules.get(i).getIdentifier();
			}
		return ids;
	}

	public String getImageAddress() {
		return this.root.getImageAddress();
	}
	
	public Molecule getRoot() {
		return root;
	}

	public void setRoot(Molecule root) {
		this.root = root;
	}

	public java.util.Vector<Molecule> getMolecules() {
		return this.molecules;
	}

	public void setMolecules(java.util.Vector<Molecule> molecules) {
		this.molecules = molecules;
	}
	
	public String getInchikey1() {
		return inchikey1;
	}

	public boolean isDatabaseLinkAvailable() {
		return this.root.isDatabaseLinkAvailable();
	}

	public String getMultipleDatabaseLink() {
		String links = this.root.getDatabaseLink();
		if(this.molecules != null)
			for(int i = 0; i < this.molecules.size(); i++) {
				links += "<br/ >" + this.molecules.get(i).getDatabaseLink();
			}
		return links; 
	}
	
	public boolean isInChIKeyLinkAvailable() {
		if(this.inchikey1 != null && this.inchikey1.length() != 0 && 
				(this.root.getDatabase().equals("PubChem") || this.root.getDatabase().equals("ChemSpider") || 
				((this.root.getDatabase().equals("LocalSDF") || this.root.getDatabase().equals("LocalCSV") || this.root.getDatabase().equals("LocalPSV")) && this.root.getIdentifier().startsWith("DTXSID"))))
			return true;
		return false;
	}

	public String getInChIKeyLink() {
		if(this.root.getDatabase().equals("PubChem")) return "https://www.ncbi.nlm.nih.gov/pccompound?term=" + this.inchikey1;
		else if(this.root.getDatabase().equals("ChemSpider")) return "http://www.chemspider.com/Search.aspx?q=" + this.inchikey1;
		else if((this.root.getDatabase().equals("LocalSDF") || this.root.getDatabase().equals("LocalCSV") || this.root.getDatabase().equals("LocalPSV")) 
				&& this.root.getIdentifier().startsWith("DTXSID")) return "https://comptox.epa.gov/dashboard/dsstoxdb/results?search=" + this.inchikey1;
		return this.inchikey1; 
	}
	
	public String getDatabaseLink() {
		return this.root.getDatabaseLink(); 
	}
	
	public void setInchikey1(String inchikey1) {
		this.inchikey1 = inchikey1;
	}

	public HorizontalBarChartModel getHorizontalScoreModel() {
		return this.root.getHorizontalScoreModel();
	}
	
	public ScoreSummary[] getScoreSummary() {
		return this.root.getScoreSummary();
	}
	
	public MatchList getMatchList() {
		return this.root.getMatchList();
	}
	
	public int getNumberPeaksExplained() {
		return this.root.getNumberPeaksExplained();
	}
	
	public String[] getAdditionalValueNames() {
		return this.root.getAdditionalValueNames();
	}
	
	public int[] getAdditionalValues() {
		return this.root.getAdditionalValues();
	}
	
	public double getSimScore() {
		return this.root.getSimScore();
	}
}
