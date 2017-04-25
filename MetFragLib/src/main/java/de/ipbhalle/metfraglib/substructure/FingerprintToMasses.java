package de.ipbhalle.metfraglib.substructure;

import java.io.IOException;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;

public class FingerprintToMasses {

	private ArrayList<ArrayList<Double>> fingerprintToMasses;
	private ArrayList<ArrayList<Double>> numObservations;
	private ArrayList<FastBitArray> fingerprints;
	private ArrayList<Double> sumNumObservations;
	private ArrayList<Double> alphaProbabilities;
	private ArrayList<Double> betaProbabilities;
	
	public FingerprintToMasses() {
		this.fingerprintToMasses = new ArrayList<ArrayList<Double>>();
		this.numObservations = new ArrayList<ArrayList<Double>>();
		this.fingerprints = new ArrayList<FastBitArray>();
	}
	
	public void addMass(FastBitArray fingerprint, Double mass, Double numObservations) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				this.addToMasses(mass, this.fingerprintToMasses.get(i), numObservations, this.numObservations.get(i));
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToMasses.add(i, new ArrayList<Double>());
				this.fingerprintToMasses.get(i).add(mass);
				this.numObservations.add(i, new ArrayList<Double>());
				this.numObservations.get(i).add(numObservations);
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		this.fingerprintToMasses.add(new ArrayList<Double>());
		this.fingerprintToMasses.get(this.fingerprintToMasses.size() - 1).add(mass);
		this.numObservations.add(new ArrayList<Double>());
		this.numObservations.get(this.numObservations.size() - 1).add(numObservations);
	}

	public void addMass(FastBitArray fingerprint, Double mass, Double numObservations, double mzppm, double mzabs, boolean debug) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				if(debug) System.out.println("found fingerprint");
				this.addToMasses(mass, this.fingerprintToMasses.get(i), numObservations, this.numObservations.get(i),
						mzppm, mzabs, debug);
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				if(debug) {
					System.out.println("fingerprint not found added at " + i);
					System.out.println(this.fingerprints.get(i));
					System.out.println(fingerprint);
				}
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToMasses.add(i, new ArrayList<Double>());
				this.fingerprintToMasses.get(i).add(mass);
				this.numObservations.add(i, new ArrayList<Double>());
				this.numObservations.get(i).add(numObservations);
				return;
			}
		}
		if(debug) System.out.println("fingerprint not found");
		this.fingerprints.add(fingerprint);
		this.fingerprintToMasses.add(new ArrayList<Double>());
		this.fingerprintToMasses.get(this.fingerprintToMasses.size() - 1).add(mass);
		this.numObservations.add(new ArrayList<Double>());
		this.numObservations.get(this.numObservations.size() - 1).add(numObservations);
	}
	
	public void addMass(FastBitArray fingerprint, Double mass, Double numObservations, double mzppm, double mzabs) {
		this.addMass(fingerprint, mass, numObservations, mzppm, mzabs, false);
	}
	
	public void addMass(String fingerprint, Double mass, Double numObservations) {
		this.addMass(new FastBitArray(fingerprint), mass, numObservations);
	}
	
	public void correctMasses(int index, double mass, double mzppm, double mzabs) {
		ArrayList<Double> masses = this.fingerprintToMasses.get(index);
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		
		int bestIndex = 0;
		double bestDev = Integer.MAX_VALUE;
		for(int i = 0; i < masses.size(); i++) {
			double currentMass = masses.get(i);
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				double currentDev = Math.abs(currentMass - mass);
				if(currentDev < bestDev) {
					bestDev = currentDev;
					bestIndex = i;
				}
			}
		}
		
		if(bestDev <= dev) masses.set(bestIndex, mass);
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
	
	public void addToObservations(int index, double value) {
		for(int i = 0; i < this.numObservations.get(index).size(); i++) {
			this.numObservations.get(index).set(i, this.numObservations.get(index).get(i) + value);
		}
	}
	
	public String toStringObservations(int index) {
		String string = String.valueOf(this.numObservations.get(index).get(0));
		for(int i = 1; i < this.numObservations.get(index).size(); i++) {
			string += " " + this.numObservations.get(index).get(i);
		}
		return string;
	}
	
	public void normalizeNumObservations(int index, double value) {
		for(int i = 0; i < this.numObservations.get(index).size(); i++) {
			this.numObservations.get(index).set(i, this.numObservations.get(index).get(i) / value);
		}
	}
	
	public void clearSumNumObservations() {
		this.sumNumObservations = new ArrayList<Double>();
	}
	
	public void calculateSumNumObservations(int index, double toAdd) {
		double sum = 0;
		for(int i = 0; i < this.numObservations.get(index).size(); i++) {
			sum += this.numObservations.get(index).get(i);
		}	
		this.sumNumObservations.add(sum + toAdd);
	}
	
	public double getSumNumObservations(int index) {
		return this.sumNumObservations.get(index);
	}

	public void addBetaProbability(double betaProbability) {
		if(this.betaProbabilities == null) this.betaProbabilities = new ArrayList<Double>();
		this.betaProbabilities.add(betaProbability);
	}

	public void addAlphaProbability(double alphaProbability) {
		if(this.alphaProbabilities == null) this.alphaProbabilities = new ArrayList<Double>();
		this.alphaProbabilities.add(alphaProbability);
	}
	
	public double getAlphaProbabilty(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return 0.0;
		return this.alphaProbabilities.get(index);
	}

	public double getAlphaProbabilty(int index) {
		if(index == -1) return 0.0;
		return this.alphaProbabilities.get(index);
	}

	public double getBetaProbabilty(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return 0.0;
		return this.betaProbabilities.get(index);
	}

	public double getBetaProbabilty(int index) {
		if(index == -1) return 0.0;
		return this.betaProbabilities.get(index);
	}
	
	public double getSumNumObservations(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return 0.0;
		return this.sumNumObservations.get(index);
	}
	
	public ArrayList<Double> getMasses(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return new ArrayList<Double>();
		return this.fingerprintToMasses.get(index);
	}

	public ArrayList<Double> getNumObservations(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return new ArrayList<Double>();
		return this.numObservations.get(index);
	}

	public ArrayList<Double> getMasses(int index) {
		if(index == -1) return new ArrayList<Double>();
		return this.fingerprintToMasses.get(index);
	}

	public ArrayList<Double> getNumObservations(int index) {
		if(index == -1) return new ArrayList<Double>();
		return this.numObservations.get(index);
	}
	
	public boolean containsFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return true;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) return false;
		}
		return false;
	}
	
	public boolean containsMass(Double mass, ArrayList<Double> masses) {
		for(int i = 0; i < masses.size(); i++) {
			int compareResult = masses.get(i).compareTo(mass);
			if(compareResult < 0) return false;
			if(compareResult == 0) return true;
		}
		return false;
	}
	
	public int getIndexOfFingerprint(FastBitArray fingerprint, boolean debug) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return i;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				return -1;
			}
		}
		return -1;
	}
	
	public int getIndexOfFingerprint(FastBitArray fingerprint) {
		return this.getIndexOfFingerprint(fingerprint, false);
	}
	
	public boolean contains(FastBitArray fingerprint, Double mass) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return false;
		return this.containsMass(mass, this.fingerprintToMasses.get(index));
	}
	
	public int getSize(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return 0;
		return this.fingerprintToMasses.get(index).size();
	}
	
	public int getOverallSize() {
		int size = 0;
		for(ArrayList<Double> masses : this.fingerprintToMasses) {
			size += masses.size();
		}
		return size;
	}
	
	public FastBitArray getFingerprint(int index) {
		return this.fingerprints.get(index);
	}
	
	public int getFingerprintSize() {
		return this.fingerprints.size();
	}
	
	public void print(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return;
		ArrayList<Double> masses = this.fingerprintToMasses.get(index);
		ArrayList<Double> numObservations = this.numObservations.get(index);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < masses.size(); i++) {
			System.out.println("-> " + masses.get(i) + ":" + numObservations.get(i));
		}
	}
	
	public void print(String fp) {
		FastBitArray fingerprint = new FastBitArray(fp);
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return;
		ArrayList<Double> masses = this.fingerprintToMasses.get(index);
		ArrayList<Double> numObservations = this.numObservations.get(index);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < masses.size(); i++) {
			System.out.println("-> " + masses.get(i) + ":" + numObservations.get(i));
		}
	}
	
	public String toString(FastBitArray fingerprint) {
		String string = fingerprint.toString();
		int index = this.getIndexOfFingerprint(fingerprint);
		for(int i = 0; i < this.fingerprintToMasses.get(index).size(); i++) {
			string += " " + this.numObservations.get(index).get(i) + " " + this.fingerprintToMasses.get(index).get(i);
		}
		return string;
	}
	
	public String toString(int index) {
		String string = this.fingerprints.get(index).toString();
		for(int i = 0; i < this.fingerprintToMasses.get(index).size(); i++) {
			string += " " + this.numObservations.get(index).get(i) + " " + this.fingerprintToMasses.get(index).get(i);
		}
		return string;
	}
	
	public String toStringIDs(int index) {
		String string = this.fingerprints.get(index).toStringIDs();
		for(int i = 0; i < this.fingerprintToMasses.get(index).size(); i++) {
			string += " " + this.numObservations.get(index).get(i) + " " + this.fingerprintToMasses.get(index).get(i);
		}
		return string;
	}
	
	public void writeToFile(String filename) throws IOException {
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
		for(int i = 0; i < this.fingerprints.size(); i++) {
			bwriter.write(this.fingerprints.get(i).toStringIDs());
			bwriter.newLine();
		}
		bwriter.close();
	}
}
