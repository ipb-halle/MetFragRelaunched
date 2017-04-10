package de.ipbhalle.metfraglib.substructure;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.list.DefaultList;

public class PeakToFingerprintGroupListCollection extends DefaultList {
	
	// P ( p )
	private double[] peakProbabilities;
	private Integer maximumAnnotatedID = null;
	// needed for p_m_given_f
	FingeprintObservations fingerprintObservations = null;
	
	public PeakToFingerprintGroupListCollection() {
		super();
	}

	public double getSumProbability(FastBitArray fingerprint) {
		return this.fingerprintObservations.getSumProbabilities(fingerprint);
	}
	
	public void calculateFingeprintObservations() {
		this.fingerprintObservations = new FingeprintObservations();
		for(int i = 0; i < this.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = this.getElement(i);
			for(int j = 0; j < groupList.getNumberElements(); j++) {
				this.fingerprintObservations.addFingerprint(groupList.getElement(j).getFingerprint());
			}
		}
		this.fingerprintObservations.calculateSumProbabilities(this);
	}
	
	public void filterByOccurence(int minimumNumberOccurences) {
		java.util.Vector<Object> filteredList = new java.util.Vector<Object>();
		for(int i = 0; i < this.getNumberElements(); i++) {
			this.getElement(i).filterByOccurence(minimumNumberOccurences);
			if(this.getElement(i).getNumberElements() != 0)
				filteredList.add(this.getElement(i));
		}
		this.list = filteredList;
	}
	
	public void calculateSumProbabilities() {
		for(int i = 0; i < this.getNumberElements(); i++) {
			this.getElement(i).calculateSumProbabilites();
		}
	}
	
	public void addElement(PeakToFingerprintGroupList obj) {
		this.list.add(obj);
	}

	public void addElementSorted(PeakToFingerprintGroupList obj) {
		int index = 0;
		while(index < this.list.size()) {
			double peakMz = ((PeakToFingerprintGroupList)this.list.get(index)).getPeakmz();
			if(peakMz < obj.getPeakmz()) index++;
			else break;
		}
		this.list.add(index, obj);
	}

	public void sortElementsByProbability() {
		for(int i = 0; i < this.list.size(); i++) {
			this.getElement(i).sortElementsByProbability();
		}
	}
	
	public void addElement(int index, PeakToSmartsGroupList obj) {
		this.list.add(index, obj);
	}
	
	public PeakToFingerprintGroupList getElement(int index) {
		return (PeakToFingerprintGroupList)this.list.get(index);
	}
	
	/**
	 * 
	 * @param mzValue
	 * @param mzppm
	 * @param mzabs
	 * @return
	 */
	public PeakToFingerprintGroupList getElementByPeak(Double mzValue, Double mzppm, Double mzabs) {
		double dev = MathTools.calculateAbsoluteDeviation(mzValue, mzppm) + mzabs;
		double minDev = Integer.MAX_VALUE;
		PeakToFingerprintGroupList bestMatch = null;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = (PeakToFingerprintGroupList)this.list.get(i);
			
			double currentDev = Math.abs(peakToFingerprintGroupList.getPeakmz() - mzValue);
			if(currentDev <= dev) {
				if(currentDev < minDev) {
					minDev = currentDev;
					bestMatch = peakToFingerprintGroupList;
				}
			}
		}
		return bestMatch;
	}
	
	/**
	 * 
	 * @param mzValue
	 * @return
	 */
	public PeakToFingerprintGroupList getElementByPeak(Double mzValue) {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = (PeakToFingerprintGroupList)this.list.get(i);
			if(peakToFingerprintGroupList.getPeakmz().equals(mzValue)) return peakToFingerprintGroupList;
		}
		return null;
	}
	
	/**
	 * 
	 * @param mzValue
	 * @param mzppm
	 * @param mzabs
	 * @return
	 */
	public PeakToFingerprintGroupList getElementByPeakInterval(Double mzValue, Double mzppm, Double mzabs) {
	//	System.out.println("-> getElementByPeakInterval " + mzValue);
		double minDev = Integer.MAX_VALUE;
		PeakToFingerprintGroupList bestMatch = null;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = (PeakToFingerprintGroupList)this.list.get(i);
			double dev = MathTools.calculateAbsoluteDeviation(peakToFingerprintGroupList.getPeakmz(), mzppm) + mzabs;
			double lowerMassBorder = peakToFingerprintGroupList.getPeakmz();
			double upperMassBorder = peakToFingerprintGroupList.getPeakmz() + (2.0 * dev);
	//		System.out.println("-> [" + lowerMassBorder + ", " + upperMassBorder + "]");
			
			if(mzValue < upperMassBorder && mzValue >= lowerMassBorder) {
				double currentDev = Math.abs(mzValue - peakToFingerprintGroupList.getPeakmz());
				if(currentDev < minDev) {
					minDev = currentDev;
					bestMatch = peakToFingerprintGroupList;
				}
			}
		}
	//	if(bestMatch == null) System.out.println("-> nothing found");
	//	else System.out.println("-> found " + bestMatch.getPeakmz());
		return bestMatch;
	}
	
	public void print() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			System.out.print(peakToFingerprintGroupList.getPeakmz());
			for(int j = 0; j < peakToFingerprintGroupList.getNumberElements(); j++) {
				System.out.print(" ");
				peakToFingerprintGroupList.getElement(j).print();
			}
		}
	}
	
	public String toString() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			string += peakToFingerprintGroupList.getPeakmz() + " " + peakToFingerprintGroupList.toString();
		}
		return string;
	}

	public String toStringSmiles() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			string += peakToFingerprintGroupList.getPeakmz() + " " + peakToFingerprintGroupList.toStringSmiles();
		}
		return string;
	}

	public String toStringDetail() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			string += peakToFingerprintGroupList.getPeakmz() + " " + peakToFingerprintGroupList.toStringDetail();
		}
		return string;
	}

	/**
	 * calculate P ( p ) for every peak p
	 * 
	 */
	public void calculatePeakProbabilities() {
		this.peakProbabilities = new double[this.list.size()];
		int totalNumber = 0;
		for(int i = 0; i < this.list.size(); i++) {
			totalNumber += this.getElement(i).getAbsolutePeakFrequency();
			this.peakProbabilities[i] = (double)this.getElement(i).getAbsolutePeakFrequency();
		}
		for(int i = 0; i < this.peakProbabilities.length; i++) {
			this.peakProbabilities[i] /= (double)totalNumber;
		}
	}

	public int[] calculatePeakAbsoluteProbabilities() {
		int[] peakOccurences = new int[this.list.size()];
		for(int i = 0; i < this.list.size(); i++) {
			peakOccurences[i] = this.getElement(i).getAbsolutePeakFrequency();
		}
		return peakOccurences;
	}
	public void setProbabilityToJointProbability() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			peakToFingerprintGroupList.setProbabilityToJointProbability();
		}
	}

	public void setProbabilityToConditionalProbability_sp() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			peakToFingerprintGroupList.setProbabilityToConditionalProbability_sp();
		}
	}
	
	public void setProbabilityToConditionalProbability_ps() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			peakToFingerprintGroupList.setProbabilityToConditionalProbability_ps();
		}
	}
	
	public void setProbabilityToNumberObserved() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			peakToFingerprintGroupList.setProbabilityToNumberObserved();
		}
	}
	
	public void updateConditionalProbabilities() {
		for(int i = 0; i < this.getNumberElements(); i++) {
			this.getElement(i).updateConditionalProbabilities();
		}
	}

	public void updateConditionalProbabilities(int[] substructureAbsoluteProbabilities) {
		for(int i = 0; i < this.getNumberElements(); i++) {
			this.getElement(i).updateConditionalProbabilities(substructureAbsoluteProbabilities);
		}
	}
	
	public void updateJointProbabilities() {
		int numberN = 0;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = (PeakToFingerprintGroupList)this.list.get(i);
			for(int j = 0; j < peakToFingerprintGroupList.getNumberElements(); j++) {
				numberN += peakToFingerprintGroupList.getElement(j).getNumberObserved();
			}
		}
		for(int i = 0; i < this.list.size(); i++) {
			this.getElement(i).updateJointProbabilities(numberN);
		}
	}
	
	public void annotateIds() {
		//store all smarts groups to annotate them with IDs later
		java.util.Vector<FingerprintGroup> fingerprintGroups = new java.util.Vector<FingerprintGroup>();
		int maxAnnotatedId = -1;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = (PeakToFingerprintGroupList)this.getElement(i);
			for(int j = 0; j < peakToFingerprintGroupList.getNumberElements(); j++) {
				FingerprintGroup fingerprintGroup = (FingerprintGroup)peakToFingerprintGroupList.getElement(j);
				fingerprintGroups.add(fingerprintGroup);
			}
		}
		
		int number = fingerprintGroups.size();
		int nextPercent = 1;
		System.out.println(number + " substructures");
		
		for(int i = 0; i < fingerprintGroups.size(); i++) {
			FingerprintGroup fingerprintGroupI = fingerprintGroups.get(i);
			for(int j = 0; j < fingerprintGroups.size(); j++) {
				FingerprintGroup fingerprintGroupJ = fingerprintGroups.get(j);
				if(fingerprintGroupJ.getId() == null) {
					fingerprintGroupI.setId(++maxAnnotatedId);
					break;
				}
				if(fingerprintGroupJ.getFingerprint().equals(fingerprintGroupI.getFingerprint())) {
					fingerprintGroupI.setId(fingerprintGroupJ.getId());
					break;
				}
			}
			//just for percentage output
			double relation = ((double)i / (double)number) * 100.0;
			if(nextPercent < relation) {
				System.out.print(nextPercent + "% ");
				nextPercent = (int)Math.ceil(relation);
			}
		}
		System.out.println();
		this.maximumAnnotatedID = new Integer(maxAnnotatedId); 
	}
	
	// calculate N^(s)
	public int[] calculateSubstructureAbsoluteProbabilities() {
		System.out.println(this.maximumAnnotatedID + " different substructures");
		int[] absoluteProbabilities = new int[this.maximumAnnotatedID + 1];
		for(int i = 0; i < this.getNumberElements(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = this.getElement(i);
			for(int j = 0; j < peakToFingerprintGroupList.getNumberElements(); j++) {
				FingerprintGroup fingerprintGroup = peakToFingerprintGroupList.getElement(j);
				absoluteProbabilities[fingerprintGroup.getId()] += fingerprintGroup.getNumberObserved();
			}
		}
		return absoluteProbabilities;
	}
	
	public void updateJointProbabilitiesWithSubstructures(int[] substructureAbsoluteProbabilities) {
		double sumSubstructures = 0.0;
		// P ( s ) 
		double[] substructureRelativeProbabilities = new double[substructureAbsoluteProbabilities.length];
		for(int i = 0; i < substructureAbsoluteProbabilities.length; i++)
			sumSubstructures += (double)substructureAbsoluteProbabilities[i];
		for(int i = 0; i < substructureAbsoluteProbabilities.length; i++)
			substructureRelativeProbabilities[i] += (double)substructureAbsoluteProbabilities[i] / sumSubstructures; 
		
		for(int i = 0; i < this.list.size(); i++) {
			this.getElement(i).updateJointProbabilitiesWithSubstructures(substructureAbsoluteProbabilities, substructureRelativeProbabilities);
		}
	}

	public void updateJointProbabilitiesWithPeaks(int[] peakAbsoluteProbabilities) {
		double sumPeakOccurences = 0d;
		for(int i = 0; i < peakAbsoluteProbabilities.length; i++) 
			sumPeakOccurences += (double)peakAbsoluteProbabilities[i];
		// P ( p ) 
		double[] peakRelativeProbabilities = new double[peakAbsoluteProbabilities.length];
		for(int i = 0; i < peakAbsoluteProbabilities.length; i++) 
			peakRelativeProbabilities[i] = (double)peakAbsoluteProbabilities[i] / sumPeakOccurences;
		for(int i = 0; i < this.list.size(); i++) {
			this.getElement(i).updateJointProbabilitiesWithPeaks(peakAbsoluteProbabilities, peakRelativeProbabilities, i);
		}
	}
	
	public void updateProbabilities(int[] substructureAbsoluteProbabilities) {
		for(int i = 0; i < this.list.size(); i++) {
			this.getElement(i).updateConditionalProbabilities(substructureAbsoluteProbabilities);
		}
	}

	public Integer getMaximumAnnotatedID() {
		return maximumAnnotatedID;
	}

	public void setMaximumAnnotatedID(Integer maximumAnnotatedID) {
		this.maximumAnnotatedID = maximumAnnotatedID;
	}
	
}
