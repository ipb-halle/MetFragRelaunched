package de.ipbhalle.metfraglib.fragment;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.NeutralLosses;
import de.ipbhalle.metfraglib.precursor.DefaultPrecursor;

public class BitArrayNeutralLoss {

	private FastBitArray[] neutralLossAtoms;
	/*
	 * relates to de.ipbhalle.metfrag.additionals.NeutralLosses
	 */
	private byte neutralLossIndex;
	
	public BitArrayNeutralLoss(int numberNeutralLosses, byte neutralLossIndex, DefaultPrecursor precursorMolecule) {
		this.neutralLossAtoms = new FastBitArray[numberNeutralLosses];
		this.neutralLossIndex = neutralLossIndex;
	}
	
	public FastBitArray getNeutralLossAtomFastBitArray(int index) {
		return this.neutralLossAtoms[index];
	}
	
	public int getNumberNeutralLosses() {
		return this.neutralLossAtoms.length;
	}
	
	public void setNeutralLoss(int index, FastBitArray neutralLossAtoms) {
		this.neutralLossAtoms[index] = neutralLossAtoms;
	}
	
	public byte getNeutralLossType() {
		return this.neutralLossIndex;
	}
	
	public double getMassDifference() {
		return new NeutralLosses().getMassDifference(this.neutralLossIndex);
	}
	
	public byte getHydrogenDifference() {
		return new NeutralLosses().getHydrogenDifference(this.neutralLossIndex);
	}
	
}
