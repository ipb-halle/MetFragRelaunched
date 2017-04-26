package de.ipbhalle.metfraglib.substructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;

public class FingerprintToMassesHashMap {

	private HashMap<FastBitArray, FingerprintEntry> fingerprintEntries;
	private double betaProbability;
	
	public FingerprintToMassesHashMap() {
		this.fingerprintEntries = new HashMap<FastBitArray, FingerprintEntry>();
		this.betaProbability = 0.0;
	}
	
	/**
	 * 
	 * @param fingerprint
	 * @param mass
	 * @param numObserved
	 */
	public void addMass(FastBitArray fingerprint, Double mass, Double numObserved) {
		if(this.fingerprintEntries.containsKey(fingerprint)) {
			this.fingerprintEntries.get(fingerprint).addMass(mass, numObserved);
			return;
		}
		FingerprintEntry fingerprintEntry = new FingerprintEntry(fingerprint);
		fingerprintEntry.addMass(mass, numObserved);
		this.fingerprintEntries.put(fingerprint, fingerprintEntry);
	}

	/**
	 * 
	 * @param fingerprint
	 * @param mass
	 * @param numObservations
	 * @param mzppm
	 * @param mzabs
	 */
	public void addMass(FastBitArray fingerprint, Double mass, Double numObserved, double mzppm, double mzabs) {
		if(this.fingerprintEntries.containsKey(fingerprint)) {
			this.fingerprintEntries.get(fingerprint).addMass(mass, numObserved, mzppm, mzabs);
			return;
		}
		FingerprintEntry fingerprintEntry = new FingerprintEntry(fingerprint);
		fingerprintEntry.addMass(mass, numObserved);
		this.fingerprintEntries.put(fingerprint, fingerprintEntry);
	}
	
	public void addMass(String fingerprint, Double mass, Double numObservations) {
		this.addMass(new FastBitArray(fingerprint), mass, numObservations);
	}

	public void correctMasses(double mass, double mzppm, double mzabs) {
		Iterator<?> it = this.fingerprintEntries.keySet().iterator();
		while(it.hasNext()) {
			FastBitArray bitArray = (FastBitArray)it.next();
			this.fingerprintEntries.get(bitArray).getMassObservations().correctMass(mass, mzppm, mzabs);
		}
	}
	
	public void correctMasses(FastBitArray fingerprint, double mass, double mzppm, double mzabs) {
		if(this.fingerprintEntries.containsKey(fingerprint))
			this.fingerprintEntries.get(fingerprint).getMassObservations().correctMass(mass, mzppm, mzabs);
	}
	
	protected void addToMasses(Double mass, ArrayList<Double> masses, Double numObservation, ArrayList<Double> numObservations, 
			double mzppm, double mzabs, boolean debug) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		double bestDev = Integer.MAX_VALUE;	
		
		for(int i = 0; i < masses.size(); i++) {
			double currentMass = masses.get(i);
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				double currentDev = Math.abs(currentMass - mass);
				if(currentDev < bestDev) {
					bestDev = currentDev;
				}
			}
		}
		// mass already included
		if(bestDev <= dev) {
			if(debug) System.out.println("found mass matching " + bestDev + " " + dev);
			return;
		}
		
		if(debug) System.out.println("adding mass matching " + bestDev + " " + dev);
		masses.add(mass);
		numObservations.add(numObservation);
	}
	
	protected void addToMasses(Double mass, ArrayList<Double> masses, Double numObservation, ArrayList<Double> numObservations, 
			double mzppm, double mzabs) {
		this.addToMasses(mass, masses, numObservation, numObservations, mzppm, mzabs, false);
	}
	
	protected void addToMasses(Double mass, ArrayList<Double> masses, Double numObservation, ArrayList<Double> numObservations) {
		for(int i = 0; i < masses.size(); i++) {
			int compareResult = masses.get(i).compareTo(mass);
			if(compareResult < 0) {
				masses.add(i, mass);
				numObservations.add(i, numObservation);
				return;
			}
			if(compareResult == 0) return;
		}
		masses.add(mass);
		numObservations.add(numObservation);
	}
	
	public void addToObservations(FastBitArray fingerprint, double value) {
		if(this.fingerprintEntries.containsKey(fingerprint))
			this.fingerprintEntries.get(fingerprint).addToNumObserved(value);
	}

	public void normalizeNumObservations(double value) {
		Iterator<?> it = this.fingerprintEntries.keySet().iterator();
		while(it.hasNext())
			this.fingerprintEntries.get((FastBitArray)it.next()).normalizeNumObserved(value);
	}
	
	public FastBitArray[] getFingerprints() {
		FastBitArray[] fingerprints = new FastBitArray[this.fingerprintEntries.size()];
		Iterator<?> it = this.fingerprintEntries.keySet().iterator();
		int index = 0;
		while(it.hasNext()) {
			fingerprints[index] = (FastBitArray)it.next();
			index++;
		}
		return fingerprints;
	}
	
	public void normalizeNumObservations(FastBitArray fingerprint, double value) {
		if(this.fingerprintEntries.containsKey(fingerprint))
			this.fingerprintEntries.get(fingerprint).normalizeNumObserved(value);
	}
	
	public void calculateSumNumObservations(FastBitArray fingerprint, double toAdd) {
		if(this.fingerprintEntries.containsKey(fingerprint))
			this.fingerprintEntries.get(fingerprint).calculateSumNumObserved(toAdd);
	}
	
	public double getSumNumObservations(FastBitArray fingerprint) {
		if(this.fingerprintEntries.containsKey(fingerprint))
			return this.fingerprintEntries.get(fingerprint).getSumNumObservation();
		return 0.0;
	}

	public void setBetaProbability(double betaProbability) {
		this.betaProbability = betaProbability;
	}

	public void setAlphaProbability(FastBitArray fingerprint, double alphaProbability) {
		if(this.fingerprintEntries.containsKey(fingerprint))
			this.fingerprintEntries.get(fingerprint).setAlphaProbability(alphaProbability);
	}
	
	public double getAlphaProbabilty(FastBitArray fingerprint) {
		if(this.fingerprintEntries.containsKey(fingerprint))
			return this.fingerprintEntries.get(fingerprint).getAlphaProbability();
		return 0.0;
	}

	public double getBetaProbabilty() {
		return this.betaProbability;
	}

	public boolean contains(FastBitArray fingerprint, Double mass) {
		if(!this.fingerprintEntries.containsKey(fingerprint)) {
			System.out.println("doen't contain key");
			return false;
		}
		return this.fingerprintEntries.get(fingerprint).containsMass(mass);
	}
	
	public int getSize(FastBitArray fingerprint) {
		if(!this.fingerprintEntries.containsKey(fingerprint))
			return 0;
		return this.fingerprintEntries.get(fingerprint).getMassObservations().size();
	}

	public int getSize(FastBitArray fingerprint, boolean debug) {
		if(!this.fingerprintEntries.containsKey(fingerprint)) {
			if(debug) System.out.println("not contained");
			return 0;
		}
		return this.fingerprintEntries.get(fingerprint).getMassObservations().size();
	}
	
	public int getOverallSize() {
		int size = 0;
		for(FastBitArray fingerprint : this.fingerprintEntries.keySet()) {
			size += this.fingerprintEntries.get(fingerprint).getMassObservations().size();
		}
		return size;
	}
	
	public int getFingerprintSize() {
		return this.fingerprintEntries.size();
	}
	
	public void print(FastBitArray fingerprint) {
		MassObservationLinkedList massObservationLinkedList = this.fingerprintEntries.get(fingerprint).getMassObservations();
		for(MassObservation massObservation : massObservationLinkedList.getLinkedList()) {
			System.out.println("-> " + massObservation.getMass() + ":" + massObservation.getNumObserved());
		}
	}
	
	public void print(String fp) {
		FastBitArray fingerprint = new FastBitArray(fp);
		MassObservationLinkedList massObservationLinkedList = this.fingerprintEntries.get(fingerprint).getMassObservations();
		for(MassObservation massObservation : massObservationLinkedList.getLinkedList()) {
			System.out.println("-> " + massObservation.getMass() + ":" + massObservation.getNumObserved());
		}
	}
	
	public String toString(FastBitArray fingerprint) {
		String string = fingerprint.toString();
		MassObservationLinkedList massObservationLinkedList = this.fingerprintEntries.get(fingerprint).getMassObservations();
		for(MassObservation massObservation : massObservationLinkedList.getLinkedList()) {
			string += " " + massObservation.getNumObserved() + " " + massObservation.getMass();
		}
		return string;
	}
	
	public String toStringIDs(FastBitArray fingerprint) {
		String string = fingerprint.toStringIDs();
		MassObservationLinkedList massObservationLinkedList = this.fingerprintEntries.get(fingerprint).getMassObservations();
		for(MassObservation massObservation : massObservationLinkedList.getLinkedList()) {
			string += " " + massObservation.getNumObserved() + " " + massObservation.getMass();
		}
		return string;
	}

	public boolean containsFingerprint(FastBitArray fingerprint) {
		return this.fingerprintEntries.containsKey(fingerprint);
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class FingerprintEntry {
		
		private MassObservationLinkedList massObservations;
		private FastBitArray fingerprint;
		private Double sumNumObservation;
		private Double alphaProbability;
		private Double betaProbability;
		
		public FingerprintEntry(FastBitArray fingerprint) {
			this.fingerprint = fingerprint;
			this.massObservations = new MassObservationLinkedList();
		}
		
		public FingerprintEntry(FastBitArray fingerprint, Double mass) {
			this.fingerprint = fingerprint;
			this.massObservations = new MassObservationLinkedList();
			this.massObservations.add(new MassObservation(mass));
		}
		
		public FingerprintEntry(FastBitArray fingerprint, Double mass, Double numObserved) {
			this.fingerprint = fingerprint;
			this.massObservations = new MassObservationLinkedList();
			this.massObservations.add(new MassObservation(mass, numObserved));
		}

		public boolean containsMass(Double mass) {
			return this.massObservations.contains(mass);
		}
		
		public void addMass(Double mass) {
			this.massObservations.add(new MassObservation(mass, 0.0));
		}
		
		public void addMass(Double mass, Double numObserved) {
			if(!this.massObservations.contains(mass)) 
				this.massObservations.add(new MassObservation(mass, numObserved));
		}

		public void addMass(Double mass, Double numObserved, double mzppm, double mzabs) {
			if(!this.massObservations.contains(mass, mzppm, mzabs)) 
				this.massObservations.add(new MassObservation(mass, numObserved));
		}

		public MassObservationLinkedList getMassObservations() {
			return massObservations;
		}

		public void setMassObservations(MassObservationLinkedList massObservations) {
			this.massObservations = massObservations;
		}

		public FastBitArray getFingerprint() {
			return fingerprint;
		}

		public void setFingerprint(FastBitArray fingerprint) {
			this.fingerprint = fingerprint;
		}

		public Double getSumNumObservation() {
			return sumNumObservation;
		}

		public void setSumNumObservation(Double sumNumObservation) {
			this.sumNumObservation = sumNumObservation;
		}

		public Double getAlphaProbability() {
			return alphaProbability;
		}

		public void setAlphaProbability(Double alphaProbability) {
			this.alphaProbability = alphaProbability;
		}

		public Double getBetaProbability() {
			return betaProbability;
		}

		public void setBetaProbability(Double betaProbability) {
			this.betaProbability = betaProbability;
		}
		
		public void addToNumObserved(double value) {
			for(MassObservation _massObservation : this.massObservations.getLinkedList()) 
				_massObservation.addToNumObserved(value);
		}
		
		public void normalizeNumObserved(double value) {
			for(MassObservation _massObservation : this.massObservations.getLinkedList()) {
				_massObservation.normalizeNumObserved(value);
			}
		}
		
		public void calculateSumNumObserved(double toAdd) {
			this.sumNumObservation = 0.0;
			for(MassObservation _massObservation : this.massObservations.getLinkedList()) {
				this.sumNumObservation += _massObservation.getNumObserved();
			}
			this.sumNumObservation += toAdd;
		}
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class MassObservationLinkedList {
		
		private LinkedList<MassObservation> massObservations;
		
		public MassObservationLinkedList() {
			this.massObservations = new LinkedList<MassObservation>();
		}
		
		public LinkedList<MassObservation> getLinkedList() {
			return this.massObservations;
		}
		
		public void add(MassObservation massObservation) {
			this.massObservations.add(massObservation);
		}
		
		public boolean contains(MassObservation massObservation) {
			return this.contains(massObservation.getMass());
		}

		public boolean contains(Double mass) {
			for(MassObservation _massObservation : this.massObservations) {
				if(_massObservation.getMass().equals(mass))
					return true;
			}
			return false;
		}

		public boolean contains(MassObservation massObservation, double mzppm, double mzabs) {
			return this.contains(massObservation.getMass(), mzppm, mzabs);
		}

		public boolean contains(Double mass, double mzppm, double mzabs) {
			double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm) + mzabs;
			for(MassObservation _massObservation : this.massObservations) {
				if(_massObservation.getMass() - dev <= mass && mass <= _massObservation.getMass() + dev) 
					return true;
			}
			return false;
		}
		
		public boolean correctMass(Double mass, double mzppm, double mzabs) {
			double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm) + mzabs;
			double bestDev = Integer.MAX_VALUE;
			int bestIndex = -1;
			int currentIndex = -1;
			for(MassObservation _massObservation : this.massObservations) {
				currentIndex++;
				if(_massObservation.getMass() - dev <= mass && mass <= _massObservation.getMass() + dev) {
					double currentDev = Math.abs(_massObservation.getMass() - mass);
					if(currentDev < bestDev) {
						bestDev = currentDev;
						bestIndex = currentIndex;
					}
					
				}	
			}
			if(bestIndex != -1) {
				this.massObservations.get(bestIndex).setMass(mass);
				return true;
			}
			return false;
		}
	
		public int size() {
			return this.massObservations.size();
		}
		
	}
	
	/**
	 * 
	 * @author cruttkie
	 *
	 */
	class MassObservation {
		
		private Double mass;
		private Double numObserved;
		
		public MassObservation(Double mass) {
			this.mass = mass;
			this.numObserved = 0.0;
		}

		public MassObservation(Double mass, Double numObserved) {
			this.mass = mass;
			this.numObserved = numObserved;
		}

		public Double getMass() {
			return mass;
		}

		public void setMass(Double mass) {
			this.mass = mass;
		}

		public Double getNumObserved() {
			return numObserved;
		}

		public void setNumObserved(Double numObserved) {
			this.numObserved = numObserved;
		}
		
		public boolean equals(MassObservation observation) {
			if(mass.equals(observation.getMass())) return true;
			return false;
		}
		
		public void addToNumObserved(double value) {
			this.numObserved += value;
		}
		
		public void normalizeNumObserved(double value) {
			this.numObserved /= value;
		}
	}
}
