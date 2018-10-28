package de.ipbhalle.metfrag.substructure;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.substructure.FingerprintToMassesHashMap;

public class FastBitSetHashCodeTest {

	public static void main(String[] args) {
		FingerprintToMassesHashMap fingerprintToMasses = new FingerprintToMassesHashMap();
		
		fingerprintToMasses.addMass(new FastBitArray("00101"), 100.0, 1.0);
		fingerprintToMasses.addMass(new FastBitArray("00101"), 101.0, 1.0);
		
		double alpha = 0.0;
		double beta = 0.0;
		double denominatorValue = 2 + alpha + 1 * alpha + beta;
		
		fingerprintToMasses.normalizeNumObservations(denominatorValue);
		
		FastBitArray[] fingerprints = fingerprintToMasses.getFingerprints();
		for(int i = 0; i < fingerprints.length; i++) {
			fingerprintToMasses.calculateSumNumObservations(fingerprints[i], alpha);
			System.out.println(fingerprintToMasses.toStringIDs(fingerprints[i]));
			System.out.println(denominatorValue + " " + fingerprints[i].toStringIDs() + " " + fingerprintToMasses.getSumNumObservations(fingerprints[i]));
		}
		System.out.println();
		for(int i = 0; i < fingerprints.length; i++) {
			fingerprintToMasses.calculateSumNumObservations(fingerprints[i], 0.0);
			double sum_m = fingerprintToMasses.getSumNumObservations(fingerprints[i]);
			System.out.println("sum " + sum_m);
			fingerprintToMasses.normalizeNumObservations(fingerprints[i], sum_m);
			System.out.println(fingerprintToMasses.toStringIDs(fingerprints[i]));
			fingerprintToMasses.calculateSumNumObservations(fingerprints[i], 0.0);
			System.out.println(fingerprintToMasses.getSumNumObservations(fingerprints[i]));
		}
	}
	
	
}
