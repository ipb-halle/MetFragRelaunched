package de.ipbhalle.metfraglib.additionals;

import java.io.IOException;

import net.sf.jniinchi.INCHI_RET;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.BitSetFingerprint;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.exceptions.ExplicitHydrogenRepresentationException;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class MoleculeFunctions {
	
	public static de.ipbhalle.metfraglib.inchi.InChIGeneratorFactory inchiFactory;
	public static IsotopeFactory isotopeFactory;
	public static SmilesParser sp;
    
	static {
		try {
			inchiFactory = de.ipbhalle.metfraglib.inchi.InChIGeneratorFactory.getInstance();
			isotopeFactory = Isotopes.getInstance();
			sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
		} catch (CDKException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String generateSmiles(IAtomContainer molecule) {
		SmilesGenerator sg = new SmilesGenerator();
		String smiles = null;
		try {
			smiles = sg.create(molecule);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return smiles;
	}
	
	public static String getInChIFromSmiles(String smiles) {
		String[] inchiInfo = getInChIInfoFromAtomContainer(parseSmiles(smiles));
		return inchiInfo[0];
	}
	
	public static IAtomContainer parseSmiles(String smiles) {
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		IAtomContainer precursorMolecule = null;
		try {
			precursorMolecule = sp.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
		prepareAtomContainer(precursorMolecule, true);
		return precursorMolecule;
	}
	
	public static java.util.Vector<IAtomContainer> parseSmiles(java.util.Vector<String> smiles) {
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		java.util.Vector<IAtomContainer> precursorMolecule = new java.util.Vector<IAtomContainer>();
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
		de.ipbhalle.metfraglib.inchi.InChIToStructure its = inchiFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());
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
	 */
	public static String[] getInChIInfoFromAtomContainer(IAtomContainer container) {
		de.ipbhalle.metfraglib.inchi.InChIGenerator inchiGenerator = null;
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
	
	public static void prepareDeuteriumAtomContainer(IAtomContainer container) {
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
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        java.util.Iterator<IAtom> atoms = container.atoms().iterator();
        while(atoms.hasNext()) {
        	IAtom currentAtom = atoms.next();
        	if(currentAtom.getSymbol().equals("H") || currentAtom.getProperty(VariableNames.DEUTERIUM_COUNT_NAME) != null) continue;
        	java.util.List<IAtom> neighbourAtoms = container.getConnectedAtomsList(currentAtom);
        	int numberDeuteriums = 0;
        	for(int k = 0; k < neighbourAtoms.size(); k++) {
        		IAtom currentNeighbouredAtom = neighbourAtoms.get(k);
        		if(currentNeighbouredAtom.getSymbol().equals("H") 
        				&& isotopeFactory.configure(currentNeighbouredAtom).getMassNumber() == 2) {
        			numberDeuteriums++;
        		}
        	}
        	currentAtom.setProperty(VariableNames.DEUTERIUM_COUNT_NAME, numberDeuteriums);
        }
        hAdder = null;
	}
	
	public static void removeHydrogens(IAtomContainer molecule) {
		java.util.Vector<IAtom> hydrogenAtoms = new java.util.Vector<IAtom>();
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
			molecule.removeAtomAndConnectedElectronContainers(atom);
		}
	}
	
	public static String fingerPrintToString(IBitFingerprint fp) {
		String string = "";
		for(int i = 0; i < fp.size(); i++) {
			string += fp.get(i) ? "1" : "0";
		}
		return string;
	}

	public static IBitFingerprint stringToFingerPrint(String string) {
		java.util.BitSet bitSet = new java.util.BitSet(string.length());
		for(int i = 0; i < string.length(); i++) {
			if(string.charAt(i) == '1') bitSet.set(i);
		}
		return new BitSetFingerprint(bitSet);
	}
	
	public static double getCDKXLogValue(IAtomContainer molecule) {
		org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor xlogp = new org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor();
		xlogp.calculate(molecule).getValue();
		return 0.0;
	}

	public static double getCDKALogValue(IAtomContainer molecule) {
		return 0.0;
	}
}
