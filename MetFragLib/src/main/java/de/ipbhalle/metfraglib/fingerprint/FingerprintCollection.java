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

public class FingerprintCollection {

	private IFingerprinter[] fingerprinter;
	
	public FingerprintCollection() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		String[] fingerprintClassNames = ClassNames.getFingerprintNames();
		this.fingerprinter = new IFingerprinter[fingerprintClassNames.length];
		for(int i = 0; i < fingerprintClassNames.length; i++) 
			this.fingerprinter[i] = (IFingerprinter) Class.forName(ClassNames.getClassOfFingerprintClassName(fingerprintClassNames[i])).getConstructor().newInstance();
	}
	
	public IBitFingerprint[] calculateFingerPrint(IAtomContainer s1) throws CDKException {
		IBitFingerprint[] fps = new IBitFingerprint[fingerprinter.length];
		for(int i = 0; i < this.fingerprinter.length; i++) 
			fps[i] = this.fingerprinter[i].getBitFingerprint(s1);
		return fps;
	}
	
	public String getNameOfFingerprinter(int index) {
		return this.fingerprinter[index].getClass().getName().replaceAll(".*\\.", "");
	}
	
	public String getNormalizedSmiles(IAtomContainer con) {
		
		String smiles = MoleculeFunctions.generateSmiles(con);
		
		return smiles;
	}
	
	public String[] getNormalizedFingerprint(IAtomContainer con) throws Exception {
		
		IBitFingerprint[] fps = this.calculateFingerPrint(con);
		String[] fps_strings = new String[fps.length];
		for(int i = 0; i < this.fingerprinter.length; i++) 
			fps_strings[i] = MoleculeFunctions.fingerPrintToString(fps[i]);
		
		return fps_strings;
	}
	
	public IAtomContainer getNormalizedAtomContainer(IMolecularStructure precursorMolecule, IFragment frag) throws Exception {
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
		
		return con;
	}
	
	public int getNumberFingerprinters() {
		return this.fingerprinter.length;
	}
}
