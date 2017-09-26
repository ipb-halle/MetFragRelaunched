package de.ipbhalle.metfraglib.fingerprint;

import java.lang.reflect.InvocationTargetException;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.parameter.ClassNames;

public class Fingerprint {

	private IFingerprinter fingerprinter;
	
	public Fingerprint(String fingerprinterClassName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		this.fingerprinter = (IFingerprinter) Class.forName(ClassNames.getClassOfFingerprintClassName(fingerprinterClassName)).getConstructor().newInstance();
	}
	
	public IBitFingerprint calculateFingerPrint(IAtomContainer s1) throws CDKException {
		return this.fingerprinter.getBitFingerprint(s1);
	}
	
	public String[] getNormalizedFingerprintSmiles(IMolecularStructure precursorMolecule, IFragment frag) throws Exception {
		String preSmiles = frag.getSmiles(precursorMolecule);
		String inchi1 = ""; 
		boolean useSmiles = false;
		try {
			inchi1 = MoleculeFunctions.getInChIFromSmiles(preSmiles);
		} catch(Exception e) {
			System.err.println("Problems converting " + preSmiles);
			useSmiles = true;
		}
		IAtomContainer con = null;
		try {
			if(!useSmiles) con = MoleculeFunctions.getAtomContainerFromInChI(inchi1);
			else con = MoleculeFunctions.getAtomContainerFromSMILES(preSmiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String fpString = MoleculeFunctions.fingerPrintToString(this.calculateFingerPrint(con));
		String smiles = MoleculeFunctions.generateSmiles(con);
		
		return new String[] {fpString, smiles};
	}
}
