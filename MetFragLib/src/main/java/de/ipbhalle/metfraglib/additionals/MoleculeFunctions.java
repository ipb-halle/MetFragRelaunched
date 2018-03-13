package de.ipbhalle.metfraglib.additionals;

import net.sf.jniinchi.INCHI_RET;

import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.BitSetFingerprint;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.fingerprint.MACCSFingerprinter;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.exceptions.ExplicitHydrogenRepresentationException;
import de.ipbhalle.metfraglib.fingerprint.TanimotoSimilarity;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.precursor.HDTopDownBitArrayPrecursor;

public class MoleculeFunctions {

	public static String generateSmiles(IAtomContainer molecule) {
		SmilesGenerator sg = new SmilesGenerator(SmiFlavor.Generic);
		String smiles = null;
		try {
			smiles = sg.create(molecule);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return smiles;
	}
	
	public static String[] getNormalizedFingerprintSmiles(IMolecularStructure precursorMolecule, IFragment frag) throws Exception {
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
			if(!useSmiles) con = getAtomContainerFromInChI(inchi1);
			else con = getAtomContainerFromSMILES(preSmiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String fpString = MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(con));
		String smiles = MoleculeFunctions.generateSmiles(con);
		
		return new String[] {fpString, smiles};
	}

	public static String getNormalizedFingerprint(IMolecularStructure precursorMolecule, IFragment frag) throws CDKException {
		String preSmiles = frag.getSmiles(precursorMolecule);
		String inchi1 = MoleculeFunctions.getInChIFromSmiles(preSmiles);
		IAtomContainer con = null;
		try {
			con = getAtomContainerFromInChI(inchi1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MACCSFingerprinter fingerprinter = new MACCSFingerprinter();
		IBitFingerprint f1 = null;
		try {
			f1 = fingerprinter.getBitFingerprint(con);
		} catch (CDKException e) {
			f1 = null;
		}
		
		String fpString = MoleculeFunctions.fingerPrintToString(f1);
		
		return fpString;
	}
	
	public static String getInChIFromSmiles(String smiles) throws CDKException {
		String[] inchiInfo = getInChIInfoFromAtomContainer(parseSmiles(smiles));
		return inchiInfo[0];
	}

	
	public static String[] getInChIInfoFromSmiles(String smiles) throws CDKException {
		String[] inchiInfo = getInChIInfoFromAtomContainer(parseSmiles(smiles));
		return inchiInfo;
	}
	
	public static IAtomContainer parseSmiles(String smiles) throws InvalidSmilesException {
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		IAtomContainer precursorMolecule = sp.parseSmiles(smiles);
		prepareAtomContainer(precursorMolecule, true);
		return precursorMolecule;
	}
	
	public static java.util.ArrayList<IAtomContainer> parseSmiles(java.util.ArrayList<String> smiles) {
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		java.util.ArrayList<IAtomContainer> precursorMolecule = new java.util.ArrayList<IAtomContainer>();
		try {
			for(int i = 0; i < smiles.size(); i++) { 
				IAtomContainer con = sp.parseSmiles(smiles.get(i));
				MoleculeFunctions.prepareAtomContainer(con, true);
				precursorMolecule.add(con);
			}
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
		return precursorMolecule;
	}
	
	/**
	 * 
	 * @param smiles
	 * @return
	 */
	public static IAtomContainer parseSmilesImplicitHydrogen(String smiles) {
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		IAtomContainer precursorMolecule = null;
		try {
			precursorMolecule = sp.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
		MoleculeFunctions.prepareAtomContainer(precursorMolecule, true);
		MoleculeFunctions.convertExplicitToImplicitHydrogens(precursorMolecule);
		return precursorMolecule;
	}
	
	public static int countNonHydrogenAtoms(IAtomContainer molecule) {
		int numberAtoms = 0;
		for(int i = 0; i < molecule.getAtomCount(); i++) {
			if(!molecule.getAtom(i).getSymbol().equals("H")) numberAtoms++;
		}
		return numberAtoms;
	}
	
	/**
	 * 
	 * @param molecule
	 * @return
	 * @throws ExplicitHydrogenRepresentationException
	 */
	public static double calculateMonoIsotopicMassImplicitHydrogens(IAtomContainer molecule) throws ExplicitHydrogenRepresentationException {
		double neutralMonoisotopicMass = 0.0;
		for(int i = 0; i < molecule.getAtomCount(); i++) {
			String currentAtomSymbol = molecule.getAtom(i).getSymbol();
			if(currentAtomSymbol.equals("H"))
				throw new ExplicitHydrogenRepresentationException();
			neutralMonoisotopicMass += Constants.getMonoisotopicMassOfAtom(currentAtomSymbol) + molecule.getAtom(i).getImplicitHydrogenCount() * Constants.HYDROGEN_MASS;
		}
		return neutralMonoisotopicMass;
	}
	
	public static IAtomContainer getAtomContainerFromSMILES(String smiles) throws Exception {
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		IAtomContainer molecule = sp.parseSmiles(smiles);
		for(int i = 0; i < molecule.getAtomCount(); i++) {
			if(molecule.getAtom(i).getSymbol().equals("H")) continue;
			else {
				java.util.List<IAtom> atoms = molecule.getConnectedAtomsList(molecule.getAtom(i));
				short numDs = 0;
				for(IAtom atom : atoms)
					if(atom.getSymbol().equals("H") && (atom.getMassNumber() != null && atom.getMassNumber() == 2))
						numDs++;
				molecule.getAtom(i).setProperty(VariableNames.DEUTERIUM_COUNT_NAME, numDs);
			}
		}
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			Aromaticity arom = new Aromaticity(ElectronDonation.cdk(), Cycles.cdkAromaticSet());
			arom.apply(molecule);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return molecule;
	}
	
	/**
	 * 
	 * @param inchi
	 * @return
	 * @throws Exception 
	 */
	public static IAtomContainer getAtomContainerFromInChI(String inchi) throws Exception {
		InChIGeneratorFactory inchiFactory = InChIGeneratorFactory.getInstance();
		InChIToStructure its = inchiFactory.getInChIToStructure(inchi, SilentChemObjectBuilder.getInstance());
		if(its == null) {
			throw new Exception("InChI problem: " + inchi);
		}
		INCHI_RET ret = its.getReturnStatus();
		if (ret == INCHI_RET.WARNING) {
		//	logger.warn("InChI warning: " + its.getMessage());
		} else if (ret != INCHI_RET.OKAY) {
			throw new Exception("InChI problem: " + inchi);
		}
		IAtomContainer molecule = its.getAtomContainer();
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			Aromaticity arom = new Aromaticity(ElectronDonation.cdk(), Cycles.cdkAromaticSet());
			arom.apply(molecule);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return molecule;
	}
	
	/**
	 * 
	 * @param inchi
	 * @return
	 * @throws CDKException 
	 */
	public static String[] getInChIInfoFromAtomContainer(IAtomContainer container) throws CDKException {
		InChIGeneratorFactory inchiFactory = InChIGeneratorFactory.getInstance();
		InChIGenerator inchiGenerator = null;
		try {
			inchiGenerator = inchiFactory.getInChIGenerator(container);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		String inchi = inchiGenerator.getInchi();
		String inchikey = "";
		
		try {
			inchikey = inchiGenerator.getInchiKey();
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return new String[] {inchi, inchikey};
	}
	
	public static IAtomContainer convertImplicitToExplicitHydrogens(IAtomContainer container) {
		IAtomContainer container_clone = null;
		try {
			container_clone = (IAtomContainer)container.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		AtomContainerManipulator.convertImplicitToExplicitHydrogens(container_clone);
		return container_clone;
	}
	
	/*
	public static IAtomContainer convertExplicitToImplicitHydrogens(IAtomContainer container) {
		return AtomContainerManipulator.removeHydrogens(container);
	}*/
	
	
	public static IAtomContainer convertExplicitToImplicitHydrogens(IAtomContainer container) {
		removeHydrogens(container);
		return container;
	}
	
	/**
	 * 
	 * @param container
	 */
	public static void prepareAtomContainer(IAtomContainer container, boolean explicitHydrogens) {
		while(true) {
    		try {
        		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
				Aromaticity arom = new Aromaticity(ElectronDonation.cdk(), Cycles.cdkAromaticSet());
				arom.apply(container);
        	} catch (CDKException e1) {
        		e1.printStackTrace();
        	} catch (java.lang.NullPointerException e) { //bad workaround for cdk bug?! but what shall I do... 
        		continue;
        	}
    		break;
        }
        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
        for(int i = 0; i < container.getAtomCount(); i++) {
        	try {
        		hAdder.addImplicitHydrogens(container, container.getAtom(i));
        	} catch(Exception e) {
        		container.getAtom(i).setImplicitHydrogenCount(0);
        	}
        }
        if(explicitHydrogens) AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        hAdder = null;
	}
	
	public static void removeHydrogens(IAtomContainer molecule) {
		java.util.ArrayList<IAtom> hydrogenAtoms = new java.util.ArrayList<IAtom>();
		java.util.Iterator<IAtom> atoms = molecule.atoms().iterator();
		while(atoms.hasNext()) {
			IAtom currentAtom = atoms.next();
			if(currentAtom.getSymbol().equals("H")) hydrogenAtoms.add(currentAtom);
			java.util.List<IAtom> neighbours = molecule.getConnectedAtomsList(currentAtom);
			int numberHydrogens = 0;
			for(int k = 0; k < neighbours.size(); k++) {
				if(neighbours.get(k).getSymbol().equals("H")) numberHydrogens++;
			}
			currentAtom.setImplicitHydrogenCount(numberHydrogens);
		}
		for(IAtom atom : hydrogenAtoms) {
			molecule.removeAtom(atom);
		}
	}
	
	public static String fingerPrintToString(IBitFingerprint fp) {
		int size = (int)fp.size();
		char[] set = new char[size];
		for(int i = 0; i < size; i++) {
			set[i] = fp.get(i) ? '1' : '0';
		}
		return String.valueOf(set);
	}

	public static IBitFingerprint stringToFingerPrint(String string) {
		java.util.BitSet bitSet = new java.util.BitSet(string.length());
		for(int i = 0; i < string.length(); i++) {
			if(string.charAt(i) == '1') bitSet.set(i);
		}
		return new BitSetFingerprint(bitSet);
	}

	public static FastBitArray stringToFastBitArray(String string) {
		FastBitArray bitSet = new FastBitArray(string.length());
		for(int i = 0; i < string.length(); i++) {
			if(string.charAt(i) == '1') bitSet.set(i);
		}
		return bitSet;
	}
	
	public static double getCDKXLogValue(IAtomContainer molecule) {
		org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor xlogp = new org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor();
		xlogp.calculate(molecule).getValue();
		return 0.0;
	}

	public static double getCDKALogValue(IAtomContainer molecule) {
		return 0.0;
	}
	
	public static IAtomContainer removeHydrogens(IAtomContainer target, int[] skipPositions) throws CloneNotSupportedException {
		IAtomContainer molecule = target.clone();
		java.util.ArrayList<IAtom> hydrogenAtoms = new java.util.ArrayList<IAtom>();
		java.util.Iterator<IAtom> atoms = molecule.atoms().iterator();
		java.util.ArrayList<IAtom> atomsToSkip = new java.util.ArrayList<IAtom>();
		for(int i = 0; i < skipPositions.length; i++) {
			atomsToSkip.add(molecule.getAtom(skipPositions[i]));
		}
		while(atoms.hasNext()) {
			IAtom currentAtom = atoms.next();
			if(currentAtom.getSymbol().equals("H")) {
				//hydrogen can only have one neighbour
				if(atomsToSkip.contains(molecule.getConnectedAtomsList(currentAtom).get(0))) {
					molecule.getConnectedAtomsList(currentAtom).get(0).setImplicitHydrogenCount(0);
					continue;
				}
				else hydrogenAtoms.add(currentAtom);
			}
			java.util.List<IAtom> neighbours = molecule.getConnectedAtomsList(currentAtom);
			int numberHydrogens = 0;
			for(int k = 0; k < neighbours.size(); k++) {
				if(neighbours.get(k).getSymbol().equals("H")) numberHydrogens++;
			}
			currentAtom.setImplicitHydrogenCount(numberHydrogens);
		}
		for(IAtom atom : hydrogenAtoms) {
			molecule.removeAtom(atom);
		}
		return molecule;
	}
	

	public static String getFragmentSmilesHD(IMolecularStructure precursorMolecule, IFragment fragment, int precursorID) throws CloneNotSupportedException {	
		return generateSmiles(getStructureAsIAtomContainerHD(precursorMolecule, fragment, precursorID)).replaceAll("\\[H\\]", "[2H]");
	}
	
	public static IAtomContainer getStructureAsIAtomContainerHD(IMolecularStructure precursorMolecule, IFragment fragment, int precursorIndexHD) throws CloneNotSupportedException {
		HDTopDownBitArrayPrecursor precursor = (HDTopDownBitArrayPrecursor)precursorMolecule;
		de.ipbhalle.metfraglib.FastBitArray atomsFastBitArray = ((de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment)fragment).getAtomsFastBitArray();
		de.ipbhalle.metfraglib.FastBitArray bondsFastBitArray = ((de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment)fragment).getBondsFastBitArray();
		
		IAtomContainer mol = precursor.getStructureAsIAtomContainer().clone();
		prepareAtomContainer(mol, false);
		IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
		IAtomContainer fragmentStructure = builder.newInstance(IAtomContainer.class);
		if(atomsFastBitArray.cardinality() == 1) {
			int firstSetBit = atomsFastBitArray.getFirstSetBit();
			IAtom atom = (IAtom)mol.getAtom(firstSetBit).clone();
			fragmentStructure.addAtom(atom);
			int numberDeuteriums = precursor.getNumberDeuteriumsConnectedToAtomIndex(precursorIndexHD, firstSetBit);
			for(int i = 0; i < numberDeuteriums; i++) {
				IAtom hydrogen = new Atom("H");
				fragmentStructure.addAtom(hydrogen);
				atom.setImplicitHydrogenCount(atom.getImplicitHydrogenCount() - 1);
				IBond bond = new org.openscience.cdk.Bond(hydrogen, atom);
				fragmentStructure.addBond(bond);
			}
			return fragmentStructure;
		}
		for(int i = 0; i < atomsFastBitArray.getSize(); i++) {
			if(atomsFastBitArray.get(i)) {
				int numberDeuteriums = precursor.getNumberDeuteriumsConnectedToAtomIndex(precursorIndexHD, i);
				IAtom atom = mol.getAtom(i);
				for(int k = 0; k < numberDeuteriums; k++) {
					IAtom hydrogen = new Atom("H");
					fragmentStructure.addAtom(hydrogen);
					atom.setImplicitHydrogenCount(atom.getImplicitHydrogenCount() - 1);
					IBond bond = new org.openscience.cdk.Bond(hydrogen, atom);
					fragmentStructure.addBond(bond);
				}
			}
		}
		
		for(int i = 0; i < bondsFastBitArray.getSize(); i++) {
			if(bondsFastBitArray.get(i)) {
				IBond curBond = mol.getBond(i);
				for(IAtom atom : curBond.atoms()) {
					fragmentStructure.addAtom(atom);
				}
				fragmentStructure.addBond(curBond);
			}
		}
	//	loss of hydrogens
		MoleculeFunctions.prepareAtomContainer(fragmentStructure, false);
		
		return fragmentStructure;
	}

}

