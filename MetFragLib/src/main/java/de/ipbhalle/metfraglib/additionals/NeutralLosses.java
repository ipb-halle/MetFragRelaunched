package de.ipbhalle.metfraglib.additionals;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.fragment.BitArrayNeutralLoss;
import de.ipbhalle.metfraglib.precursor.DefaultPrecursor;

public class NeutralLosses {

	private static final String[] smartPatterns = {"O", "C(=O)O", "N", "C[Si](C)(C)O", "C[Si](C)C", "CO", "CN"};
	private static final short[] minimumNumberImplicitHydrogens = {1, 1, 2, 9, 9, 1, 0};
	private static final double[] massDifferences = {-1.007825, -1.007825, -1.007825, -1.007825, -1.007825, -1.007825, -1.007825};
	private static final byte[] hydrogenDifferences = {-1, -1, -1, -1, -1, -1, -1};
	private static final double[] monoisotopicMasses = {18.01056, 46.00548, 17.02655, 90.05009, 74.05517, 30.01056, 27.01090};
	
	public static BitArrayNeutralLoss[] getMatchingAtoms(DefaultPrecursor precursorMolecule) {
		SMARTSQueryTool[] smartsQuerytools = new SMARTSQueryTool[smartPatterns.length];
		for(int i = 0; i < smartsQuerytools.length; i++) {
			smartsQuerytools[i] = new SMARTSQueryTool(smartPatterns[i], DefaultChemObjectBuilder.getInstance());
		}
		java.util.Vector<BitArrayNeutralLoss> matchedNeutralLossTypes = new java.util.Vector<BitArrayNeutralLoss>();
		for(byte i = 0; i < smartsQuerytools.length; i++) {
			try {
				if(smartsQuerytools[i].matches(precursorMolecule.getStructureAsIAtomContainer())) {
					/*
					 * get atom indeces containing to a neutral loss
					 */
					java.util.List<java.util.List<Integer>> matchingAtoms = smartsQuerytools[i].getMatchingAtoms();
					/*
					 * store which is a valid loss based on the number of hydrogens
					 */
					boolean[] validMatches = new boolean[matchingAtoms.size()];
					BitArray[] allMatches = new BitArray[matchingAtoms.size()];
					int numberOfValidNeutralLosses = 0;
					/*
					 * check each part that is marked as neutral loss
					 */
					for(int ii = 0; ii < matchingAtoms.size(); ii++) {
						java.util.List<Integer> part = matchingAtoms.get(ii);
						/*
						 * count number of implicit hydrogens of this neutral loss
						 */
						int numberImplicitHydrogens = 0;
						allMatches[ii] = new BitArray(precursorMolecule.getNonHydrogenAtomCount());
						/*
						 * check all atoms 
						 */
						for(int iii = 0; iii < part.size(); iii++) {
							allMatches[ii].set(part.get(iii));
							/*
							 * count number of implicit hydrogens of this neutral loss
							 */
							numberImplicitHydrogens += precursorMolecule.getNumberHydrogensConnectedToAtomIndex(part.get(iii));
						}
						/*
						 * valid neutral loss match if number implicit hydrogens are at least the number of hydrogens
						 * needed for the certain neutral loss
						 */
						if(numberImplicitHydrogens >= minimumNumberImplicitHydrogens[i]) {
							validMatches[ii] = true;
							numberOfValidNeutralLosses++;
						}
					}
					/*
					 * create BitArrayNeutralLosses of valid neutral loss part detections
					 */
					if(numberOfValidNeutralLosses != 0) {
						BitArrayNeutralLoss newDetectedNeutralLoss = 
							new BitArrayNeutralLoss(numberOfValidNeutralLosses, i, precursorMolecule);
						int neutralLossIndexOfBitArrayNeutralLoss = 0;
						for(int k = 0; k < validMatches.length; k++) {
							if(validMatches[k]) {
								newDetectedNeutralLoss.setNeutralLoss(neutralLossIndexOfBitArrayNeutralLoss, allMatches[k]);
								neutralLossIndexOfBitArrayNeutralLoss++;
							}
						}
						/*
						 * store them in vector
						 */
						matchedNeutralLossTypes.add(newDetectedNeutralLoss);
					}
				}
			} catch (CDKException e) {
				e.printStackTrace();
			}
		}
		BitArrayNeutralLoss[] matchedNeutralLossTypesArray = new BitArrayNeutralLoss[matchedNeutralLossTypes.size()];
		for(int i = 0; i < matchedNeutralLossTypes.size(); i++) {
			matchedNeutralLossTypesArray[i] = matchedNeutralLossTypes.get(i);
		}
		return matchedNeutralLossTypesArray;
	}
	
	public static int getNumberNeutralLossesConsidered() {
		return smartPatterns.length;
	}

	public static byte getHydrogenDifference(int index) {
		return hydrogenDifferences[index];
	}
	
	public static double getMassDifference(int index) {
		return massDifferences[index];
	}

	public static double getMonoisotopicMass(int index) {
		return monoisotopicMasses[index];
	}
	
	public static String getSmartsPattern(int index) {
		return smartPatterns[index];
	}
	
	public static int getMinimumNumberOfImplicitHydrogens(int index) {
		return minimumNumberImplicitHydrogens[index];
	}
}
