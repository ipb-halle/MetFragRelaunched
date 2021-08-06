package de.ipbhalle.metfraglib.fingerprint;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.BitSet;

import org.junit.Test;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;

public class TanimotoSimilarityTest {
	//LKJPSUCKSLORMF-UHFFFAOYSA-N 
	final String oldFingerprintString = "000000000000100000000001000000000000100000000000000000000000000000000011000010000000001000001110000001100110010100001000010101000000111000000101000111100101111111111000000000000000000000000000";
	
	@Test
	public void testFingerprint() throws Exception {
		//LKJPSUCKSLORMF-UHFFFAOYSA-N 
		final String smiles = "CN(C(=O)NC1=CC=C(C=C1)Cl)OC";
		final IAtomContainer molecule = MoleculeFunctions.getAtomContainerFromSMILES(smiles);
		MoleculeFunctions.prepareAtomContainer(molecule, true);
		final IBitFingerprint fp = TanimotoSimilarity.calculateFingerPrint(molecule);
		final String bitStringOfFingerprint = this.toBitString(fp);
		assertThat(bitStringOfFingerprint, is(this.oldFingerprintString));
	}
	
	private String toBitString(IBitFingerprint fp) {
		final StringBuilder bitStringBuilder = new StringBuilder();
		final BitSet bitSet = fp.asBitSet();
		for(int i = 0; i < bitSet.size(); i++) {
			boolean bit = bitSet.get(i);
			if(bit) bitStringBuilder.append("1");
			else bitStringBuilder.append("0");
		}
		return bitStringBuilder.toString();
	}
}
