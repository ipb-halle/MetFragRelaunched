package de.ipbhalle.metfraglib.collection;

import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.list.SortedSimilarityTandemMassPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;

public class SpectralPeakListCollection {

	protected java.util.ArrayList<SortedSimilarityTandemMassPeakList> peaklists;
	protected java.util.Hashtable<String, java.util.ArrayList<SortedSimilarityTandemMassPeakList>> inchikey1ToPeakList;
	protected java.util.Hashtable<String, Double> inchikey1ToSimScore;
	protected boolean isPositiveCharge;
	protected double mzabs;
	protected double mzppm;
	protected double minimum_cosine_similarity_limit = 0.0;
	
	public SpectralPeakListCollection(boolean isPositiveCharge, double mzabs, double mzppm) {
		this.peaklists = new java.util.ArrayList<SortedSimilarityTandemMassPeakList>();
		this.mzabs = mzabs;
		this.mzppm = mzppm;
		this.isPositiveCharge = isPositiveCharge;
	}

	public SpectralPeakListCollection(boolean isPositiveCharge, double mzabs, double mzppm, double minimum_cosine_similarity_limit) {
		this.peaklists = new java.util.ArrayList<SortedSimilarityTandemMassPeakList>();
		this.mzabs = mzabs;
		this.mzppm = mzppm;
		this.isPositiveCharge = isPositiveCharge;
		this.minimum_cosine_similarity_limit = minimum_cosine_similarity_limit;
	}
	
	public void nullify() {
		if(this.peaklists != null)
			for(int i = 0; i < peaklists.size(); i++) {
				if(this.peaklists.get(i) != null) this.peaklists.get(i).nullify();
			}
		this.peaklists = null;
		this.inchikey1ToPeakList = null;
		this.inchikey1ToSimScore = null;
	}
	
	/**
	 * get number of peaklists with a similarity score greater 0
	 * 
	 * @return
	 */
	public int getScoredPeakListCount() {
		return this.inchikey1ToSimScore.size();
	}
	
	public void calculateSimilarities(SortedTandemMassPeakList peakList) {
		this.inchikey1ToPeakList = new java.util.Hashtable<String, java.util.ArrayList<SortedSimilarityTandemMassPeakList>>();
		this.inchikey1ToSimScore = new java.util.Hashtable<String, Double>();
		for(int i = 0; i < this.peaklists.size(); i++) {
			if(this.peaklists.get(i).getIsPositiveCharge() == this.isPositiveCharge) {
				double value = this.peaklists.get(i).cosineSimilarity(peakList, this.mzppm, this.mzabs);
				if(value > this.minimum_cosine_similarity_limit) {
					IBitFingerprint fp = null;
					try {
						if(this.peaklists.get(i).getFingerprint() == null) {
							IAtomContainer con = MoleculeFunctions.getAtomContainerFromInChI(this.peaklists.get(i).getInchi());
							MoleculeFunctions.prepareAtomContainer(con, true);
							fp = TanimotoSimilarity.calculateFingerPrint(con);
							this.peaklists.get(i).setFingerprint(fp);
						}
					} catch (Exception e) {
						System.err.println("Spectrum " + i + " excluded during fingerprint calculation.");
						e.printStackTrace();
						continue;
					}
					if(this.inchikey1ToPeakList.containsKey(this.peaklists.get(i).getInchikey1())) {
						if(this.inchikey1ToSimScore.get(this.peaklists.get(i).getInchikey1()) < value) {
							this.inchikey1ToPeakList.get(this.peaklists.get(i).getInchikey1()).add(0, this.peaklists.get(i));
							this.inchikey1ToSimScore.put(this.peaklists.get(i).getInchikey1(), value);
						}
						else {
							this.inchikey1ToPeakList.get(this.peaklists.get(i).getInchikey1()).add(this.peaklists.get(i));
						}
					}
					else {
						java.util.ArrayList<SortedSimilarityTandemMassPeakList> newPeakListArrayList = new java.util.ArrayList<SortedSimilarityTandemMassPeakList>();
						newPeakListArrayList.add(this.peaklists.get(i));
						this.inchikey1ToPeakList.put(this.peaklists.get(i).getInchikey1(), newPeakListArrayList);
						this.inchikey1ToSimScore.put(this.peaklists.get(i).getInchikey1(), value);
					}
				}
			}
		}
	}
	
	public java.util.Hashtable<String, java.util.ArrayList<SortedSimilarityTandemMassPeakList>> getInchikey1ToPeakList() {
		return inchikey1ToPeakList;
	}

	public void setInchikey1ToPeakList(java.util.Hashtable<String, java.util.ArrayList<SortedSimilarityTandemMassPeakList>> inchikey1ToPeakList) {
		this.inchikey1ToPeakList = inchikey1ToPeakList;
	}

	public java.util.Hashtable<String, Double> getInchikey1ToSimScore() {
		return inchikey1ToSimScore;
	}

	public void setInchikey1ToSimScore(java.util.Hashtable<String, Double> inchikey1ToSimScore) {
		this.inchikey1ToSimScore = inchikey1ToSimScore;
	}

	public void addPeakList(SortedSimilarityTandemMassPeakList peakList) {
		this.peaklists.add(peakList);
	}
	
	public SortedSimilarityTandemMassPeakList getPeakList(int index) {
		return this.peaklists.get(index);
	}
	
	public int getSize() {
		if(this.peaklists == null) return 0;
		return this.peaklists.size();
	}
	
}
