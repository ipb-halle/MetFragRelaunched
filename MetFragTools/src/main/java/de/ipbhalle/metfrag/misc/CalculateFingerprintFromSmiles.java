package de.ipbhalle.metfrag.misc;

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.fingerprint.TanimotoSimilarity;

public class CalculateFingerprintFromSmiles {

	public static void main(String[] args) throws InvalidSmilesException {
		boolean useSmiles = true;
		String[] preSmiles_arr = {
				"Oc1cc2[nH]cnc2(cc1Cl)", 
				"Oc1nc2ccc(cc2([nH]1))Cl",
				"OC=1N=C2C=CC=C(C2(N=1))Cl",
				"N1=C(N)C=C(C)=O1"
		};
		for(String preSmiles : preSmiles_arr) {
			String inchi1 = null;
			IAtomContainer con = null;
			try {
				inchi1 = MoleculeFunctions.getInChIFromSmiles(preSmiles);
				if(!useSmiles) con = MoleculeFunctions.getAtomContainerFromInChI(inchi1);
				else con = MoleculeFunctions.getAtomContainerFromSMILES(preSmiles);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String fpString = MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(con));
			System.out.println(inchi1);
			System.out.println(fpString);
		}
		
		String a = "11011";
		String b = "11000";
		String c = "00011";
		
		System.out.println(TanimotoSimilarity.calculateSimilarity(
				MoleculeFunctions.stringToFingerPrint(a), 
				MoleculeFunctions.stringToFingerPrint(b)));
		

		System.out.println(TanimotoSimilarity.calculateSimilarity(
				MoleculeFunctions.stringToFingerPrint(a), 
				MoleculeFunctions.stringToFingerPrint(c)));
		

		System.out.println(TanimotoSimilarity.calculateSimilarity(
				MoleculeFunctions.stringToFingerPrint(b), 
				MoleculeFunctions.stringToFingerPrint(c)));
	}
	
	
}
