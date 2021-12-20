package de.ipbhalle.metfraglib.substructure;

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.fingerprint.TanimotoSimilarity;
import de.ipbhalle.metfraglib.list.DefaultList;

public class PeakToSmartsGroupListCollection extends DefaultList {
	
	// P ( p )
	private double[] peakProbabilities;
	private Integer maximumAnnotatedID = null;
	
	public PeakToSmartsGroupListCollection() {
		super();
	}

	public void filterByOccurence(int minimumNumberOccurences) {
		java.util.ArrayList<Object> filteredList = new java.util.ArrayList<Object>();
		for(int i = 0; i < this.getNumberElements(); i++) {
			this.getElement(i).filterByOccurence(minimumNumberOccurences);
			if(this.getElement(i).getNumberElements() != 0)
				filteredList.add(this.getElement(i));
		}
		this.list = filteredList;
	}
	
	public void addElement(PeakToSmartsGroupList obj) {
		this.list.add(obj);
	}

	public void addElementSorted(PeakToSmartsGroupList obj) {
		int index = 0;
		while(index < this.list.size()) {
			double peakMz = ((PeakToSmartsGroupList)this.list.get(index)).getPeakmz();
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
	
	public PeakToSmartsGroupList getElement(int index) {
		return (PeakToSmartsGroupList)this.list.get(index);
	}
	
	public PeakToSmartsGroupList getElementByPeak(Double mzValue, Double mzppm, Double mzabs) {
		double dev = MathTools.calculateAbsoluteDeviation(mzValue, mzppm) + mzabs;
		double minDev = Integer.MAX_VALUE;
		PeakToSmartsGroupList bestMatch = null;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = (PeakToSmartsGroupList)this.list.get(i);
			double currentDev = Math.abs(peakToSmartGroupList.getPeakmz() - mzValue);
			if(currentDev <= dev) {
				if(currentDev < minDev) {
					minDev = currentDev;
					bestMatch = peakToSmartGroupList;
				}
			}
		}
		return bestMatch;
	}
	
	public void print() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			System.out.print(peakToSmartGroupList.getPeakmz());
			for(int j = 0; j < peakToSmartGroupList.getNumberElements(); j++) {
				System.out.print(" ");
				peakToSmartGroupList.getElement(j).print();
			}
		}
	}
	
	public String toString() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			string += peakToSmartGroupList.getPeakmz() + " " + peakToSmartGroupList.toString();
		}
		return string;
	}

	public String toStringDetail() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			string += peakToSmartGroupList.getPeakmz() + " " + peakToSmartGroupList.toStringDetail();
		}
		return string;
	}

	public String toStringSmiles() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			string += peakToSmartGroupList.getPeakmz() + " " + peakToSmartGroupList.toStringSmiles(); 
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
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			peakToSmartGroupList.setProbabilityToJointProbability();
		}
	}

	public void setProbabilityToConditionalProbability_sp() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			peakToSmartGroupList.setProbabilityToConditionalProbability_sp();
		}
	}
	
	public void setProbabilityToConditionalProbability_ps() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			peakToSmartGroupList.setProbabilityToConditionalProbability_ps();
		}
	}
	
	public void removeDuplicates() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = (PeakToSmartsGroupList)this.list.get(i);
			peakToSmartGroupList.removeDuplicates();
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
			PeakToSmartsGroupList peakToSmartGroupList = (PeakToSmartsGroupList)this.list.get(i);
			for(int j = 0; j < peakToSmartGroupList.getNumberElements(); j++) {
				numberN += peakToSmartGroupList.getElement(j).getNumberElements();
			}
		}
		for(int i = 0; i < this.list.size(); i++) {
			this.getElement(i).updateJointProbabilities(numberN);
		}
	}
	
	public void annotateIds() {
		//store all smarts groups to annotate them with IDs later
		java.util.ArrayList<SmartsGroup> smartsGroups = new java.util.ArrayList<SmartsGroup>();
		int maxAnnotatedId = -1;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = (PeakToSmartsGroupList)this.getElement(i);
			for(int j = 0; j < peakToSmartGroupList.getNumberElements(); j++) {
				SmartsGroup smartsGroup = (SmartsGroup)peakToSmartGroupList.getElement(j);
				smartsGroups.add(smartsGroup);
			}
		}
		
		int number = smartsGroups.size();
		int nextPercent = 1;
		System.out.println(number + " substructures");
		
		IAtomContainer[] cons = new  IAtomContainer[number];
		//use the first smiles from each group to perform similarity calculation
		//needed to match IDs
		try {
			cons[0] = MoleculeFunctions.parseSmiles(smartsGroups.get(0).getSmiles().get(0));
			for(int i = 1; i < cons.length; i++) 
				cons[i] = MoleculeFunctions.parseSmiles(smartsGroups.get(i).getSmiles().get(0));
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TanimotoSimilarity sims = new TanimotoSimilarity(cons);
		System.out.println("calculated similarities");
		
		for(int i = 0; i < smartsGroups.size(); i++) {
			SmartsGroup smartsGroupI = smartsGroups.get(i);
			for(int j = 0; j < smartsGroups.size(); j++) {
				SmartsGroup smartsGroupJ = smartsGroups.get(j);
				if(smartsGroupJ.getId() == null) {
					smartsGroupI.setId(++maxAnnotatedId);
					break;
				}
				double sim = TanimotoSimilarity.calculateSimilarity(sims.getFingerPrint(i), sims.getFingerPrint(j));
				if(sim == 1.0) {
					smartsGroupI.setId(smartsGroupJ.getId());
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
		this.maximumAnnotatedID = Integer.valueOf(maxAnnotatedId); 
	}
	
	// calculate N^(s)
	public int[] calculateSubstructureAbsoluteProbabilities() {
		System.out.println(this.maximumAnnotatedID + " different substructures");
		int[] absoluteProbabilities = new int[this.maximumAnnotatedID + 1];
		for(int i = 0; i < this.getNumberElements(); i++) {
			PeakToSmartsGroupList peakToSmartGroupList = this.getElement(i);
			for(int j = 0; j < peakToSmartGroupList.getNumberElements(); j++) {
				SmartsGroup smartGroup = peakToSmartGroupList.getElement(j);
				absoluteProbabilities[smartGroup.getId()] += smartGroup.getNumberElements();
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
