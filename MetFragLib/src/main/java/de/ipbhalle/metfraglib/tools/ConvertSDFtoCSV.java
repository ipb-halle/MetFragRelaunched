package de.ipbhalle.metfraglib.tools;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.candidate.PrecursorCandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;

public class ConvertSDFtoCSV {

	/**
	 * @param args
	 * @throws CDKException 
	 */
	public static void main(String[] args) throws CDKException {
		IteratingSDFReader reader;
		CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
		CandidateList candidates = new CandidateList();
		try {
			reader = new IteratingSDFReader(new java.io.FileReader(args[0]), DefaultChemObjectBuilder.getInstance());
			while(reader.hasNext()) {
				IAtomContainer molecule = reader.next();
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				adder.addImplicitHydrogens(molecule);
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
				
				PrecursorCandidate candidate = new PrecursorCandidate((String)molecule.getProperty(VariableNames.INCHI_NAME), (String)molecule.getProperty(VariableNames.IDENTIFIER_NAME));
				java.util.Iterator<Object> keys = molecule.getProperties().keySet().iterator();
				while(keys.hasNext()) {
					String key = (String)keys.next();
					if(molecule.getProperty(key) != null) candidate.setProperty(key, molecule.getProperty(key));
				}
				candidates.addElement(candidate);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		CandidateListWriterPSV writer = new CandidateListWriterPSV();
		try {
			writer.write(candidates, args[1], args[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
