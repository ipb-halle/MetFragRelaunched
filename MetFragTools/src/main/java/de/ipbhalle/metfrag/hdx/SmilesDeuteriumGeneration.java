package de.ipbhalle.metfrag.hdx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

import net.sf.jniinchi.JniInchiInput;
import net.sf.jniinchi.JniInchiWrapper;

import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.inchi.InChIToStructure;
import de.ipbhalle.metfraglib.molecularformula.HDByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import edu.emory.mathcs.backport.java.util.Arrays;

public class SmilesDeuteriumGeneration {

	public static void main(String[] args) throws Exception {
		boolean withAromaticRings = false;
		boolean combinatorial = false;
		/*
		 * read input inchis
		 */
		ArrayList<String> inchis = new ArrayList<String>();
		ArrayList<Integer> numberToAddDeuteriums = new ArrayList<Integer>();
		ArrayList<String> identifiers = new ArrayList<String>();
		File input = new File(args[0]);
		BufferedReader breader = null;
		if(!input.exists()) {
			inchis.add(args[0]);
			identifiers.add("1");
		}
		else
			breader = new BufferedReader(new FileReader(input));
		String line = "";
		int identifier = 1;
		if(breader != null)
			while ((line = breader.readLine()) != null) {
				String[] tmp = line.trim().split("\\s+");
				inchis.add(tmp[0].trim());
				if(tmp.length >= 2) numberToAddDeuteriums.add(Integer.parseInt(tmp[1].trim()));
				if(tmp.length == 3) identifiers.add(tmp[2].trim());
				else identifiers.add(identifier + "");
				identifier++;
			}
		if(breader != null) breader.close();
		/*
		 * generate deuterated version of inchi
		 */

		IAtomContainerSet set = new AtomContainerSet();
		SmilesGenerator sg = new SmilesGenerator();

		CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
		for (int j = 0; j < inchis.size(); j++) {
			/*
			 * build the jni inchi atom container
			 */
			IAtomContainer its = MoleculeFunctions.parseSmilesImplicitHydrogen(inchis.get(j));
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(its);
			try {
				adder.addImplicitHydrogens(its);
			}
			catch(Exception e) {
				System.out.println("missed " + inchis.get(j) + " " + identifiers.get(j));
				continue;
			}
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(its);
			
			int numberDeuteriums = 0;
			int numberDeuteriumsEasilyExchanged = 0;
			int numberDeuteriumsAromaticExchanged = 0;
			
			int[] toExchange = searchForDeuteriumExchangeablePositions(
					new String[] { "O", "N", "S" }, its);
			
			if(!combinatorial || (numberToAddDeuteriums.size() == 0 || toExchange.length <= numberToAddDeuteriums.get(j))) {	
				for (int i = 0; i < toExchange.length; i++) {
					int numberExchanged = setAllExplicitDeuteriums(its, toExchange[i]);
					numberDeuteriums += numberExchanged;
					numberDeuteriumsEasilyExchanged += numberExchanged;
				}
			}
			else if(toExchange.length > numberToAddDeuteriums.get(j)) {
				ArrayList<IAtomContainer> deuteratedStrutures = new ArrayList<IAtomContainer>();
				ArrayList<Integer> numberDeuteriumsVec = new ArrayList<Integer>();
				ArrayList<Integer> numberDeuteriumsEasilyExchangedVec = new ArrayList<Integer>();
				//get all possible combinations of exchanges with given number 
				//of exchangeable hydrogens
				int[][] combs = getCombinations(toExchange, numberToAddDeuteriums.get(j));

				for (int k = 0; k < combs.length; k++) {
					numberDeuteriumsEasilyExchanged = 0;
					numberDeuteriums = 0;
					IAtomContainer itsNew = new InChIToStructure(inchis.get(j),
							DefaultChemObjectBuilder.getInstance()).getAtomContainer();
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(itsNew);
					adder.addImplicitHydrogens(itsNew);
					AtomContainerManipulator.convertImplicitToExplicitHydrogens(itsNew);
					
					for(int l = 0; l < combs[k].length; l++) {
						addExplicitDeuterium(itsNew, combs[k][l]);
						numberDeuteriumsEasilyExchanged++;
						numberDeuteriums++;
					}
					numberDeuteriumsVec.add(numberDeuteriums);
					numberDeuteriumsEasilyExchangedVec.add(numberDeuteriumsEasilyExchanged);
					deuteratedStrutures.add(itsNew);
				}
				for(int k = 0; k < deuteratedStrutures.size(); k++) {
					String inchi = inchis.get(j);
					IAtomContainer con = deuteratedStrutures.get(k);
					
					HDByteMolecularFormula formula = null;
					try {
						formula = new HDByteMolecularFormula(inchi.split("/")[1]);
					}
					catch(Exception e) {
						System.err.println(identifiers.get(j));
						e.printStackTrace();
						System.exit(1);
					}
					formula.setNumberHydrogens((short)formula.getNumberHydrogens());
					formula.setNumberDeuterium((short) (int) numberDeuteriumsVec.get(k));
					
					con.setProperty(VariableNames.IDENTIFIER_NAME, identifiers.get(j) + "-" + (k+1));
					con.setProperty(VariableNames.INCHI_NAME, inchi);
					//con.setProperty(VariableNames.SMILES_NAME, sg.create(deuteratedStrutures.get(k)));
					con.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formula.toString());
					con.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, formula.getMonoisotopicMass());
					con.setProperty(VariableNames.INCHI_KEY_1_NAME, JniInchiWrapper.getInchiKey(inchi).getKey().split("-")[0]);
					con.setProperty(VariableNames.INCHI_KEY_2_NAME, JniInchiWrapper.getInchiKey(inchi).getKey().split("-")[1]);
					con.setProperty("OSN-Deuteriums", numberDeuteriumsEasilyExchangedVec.get(k));
					con.setProperty("AromaticDeuteriums", 0);
					con.setProperty("MissedDeuteriums", (toExchange.length - numberToAddDeuteriums.get(j)));
					
					set.addAtomContainer(con);
					
					System.out.println(identifiers.get(j) + "-" + (k+1) + "|" + inchi + "|" + sg.create(deuteratedStrutures.get(k)) + "|" 
						+ formula.toString() + "|" + formula.getMonoisotopicMass() + "|"
						+ JniInchiWrapper.getInchiKey(inchi).getKey().split("-")[0] + "|" + JniInchiWrapper.getInchiKey(inchi).getKey().split("-")[1] 
						+ "|" + numberDeuteriumsEasilyExchangedVec.get(k) + "|0|" + (toExchange.length - numberToAddDeuteriums.get(j)));
				}
				continue;
			}
			
			/*
			 * for aromatic ring deuteration generate the cdk atomcontainer and 
			 * detect aromatic rings
			 */
			
			if(withAromaticRings || (numberToAddDeuteriums.size() != 0 && numberToAddDeuteriums.get(j) > numberDeuteriums)) { 
				int numberMissing = numberToAddDeuteriums.get(j) - numberDeuteriums;
				numberDeuteriums += numberMissing;
				numberDeuteriumsAromaticExchanged += numberMissing; 
			}
			
			String inchi = inchis.get(j);

			HDByteMolecularFormula formula = null;
			try {
				formula = new HDByteMolecularFormula(MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(its)));
			}
			catch(Exception e) {
				System.err.println(identifiers.get(j));
				e.printStackTrace();
				System.exit(1);
			}
			formula.setNumberHydrogens((short)formula.getNumberHydrogens());
			formula.setNumberDeuterium((short) numberDeuteriums);
			// Identifier|InChI|MolecularFormula|MonoisotopicMass|InChIKey1|InChIKey2|OSN-Deuteriums|AromaticDeuteriums
			
			if(numberToAddDeuteriums.size() == 0 || numberDeuteriums == numberToAddDeuteriums.get(j)) {
				
				its.setProperty(VariableNames.IDENTIFIER_NAME, identifiers.get(j));
				its.setProperty(VariableNames.INCHI_NAME, inchi);
				//its.setProperty(VariableNames.SMILES_NAME, sg.create(its));
				its.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formula.toString());
				
				double mass = MathTools.round(formula.getMonoisotopicMass());
				its.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, formula.getMonoisotopicMass());
				//its.setProperty(VariableNames.INCHI_KEY_1_NAME, JniInchiWrapper.getInchiKey(inchi).getKey().split("-")[0]);
				//its.setProperty(VariableNames.INCHI_KEY_2_NAME, JniInchiWrapper.getInchiKey(inchi).getKey().split("-")[1]);
				its.setProperty("OSN-Deuteriums", numberDeuteriumsEasilyExchanged);
				its.setProperty("AromaticDeuteriums", numberDeuteriumsAromaticExchanged);
				its.setProperty("MissedDeuteriums", 0);
				
				its.setProperty("M+H", MathTools.round(mass + Constants.getMonoisotopicMassOfAtom("H") - Constants.ELECTRON_MASS));
				its.setProperty("M+D", MathTools.round(mass + Constants.getMonoisotopicMassOfAtom("D") - Constants.ELECTRON_MASS));
				its.setProperty("M-H", MathTools.round(mass - Constants.getMonoisotopicMassOfAtom("H") + Constants.ELECTRON_MASS));
				its.setProperty("M-D", MathTools.round(mass - Constants.getMonoisotopicMassOfAtom("D") + Constants.ELECTRON_MASS));
				
				IAtomContainer con = MoleculeFunctions.removeHydrogens(its, toExchange);
				
				its.setProperty("DeuteratedSMILES", MoleculeFunctions.generateSmiles(con).replaceAll("\\[H\\]", "[2H]"));
				set.addAtomContainer(its);
			}
			else if(!combinatorial) System.err.println("discarded to many easy exchangeable hydrogens " + numberDeuteriums + " " + identifiers.get(j));

		}

		try {
			SDFWriter writer = new SDFWriter(new java.io.FileWriter(new java.io.File(args[1])));
			writer.write(set);
			writer.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		} catch (CDKException e) {
			e.printStackTrace();
		}
	}

	public static void printInfo(IAtomContainer jios) {
		for(int i = 0; i < jios.getAtomCount(); i++) {
			int numDs = getNumberExplicitDeuteriums(jios, i);
			if(numDs > 0) {
				System.out.println(jios.getAtom(i).getSymbol() + " " + i + " H:" + getNumberExplicitRealHydrogens(jios, i) + " D:" + getNumberExplicitDeuteriums(jios, i));
			}
		}
	}

	public static void printInfo(JniInchiInput jios) {
		for(int i = 0; i < jios.getNumAtoms(); i++) {
			if(jios.getAtom(i).getImplicitDeuterium() > 0) {
				System.out.println(jios.getAtom(i).getElementType() + " " + i + " H:" + jios.getAtom(i).getImplicitH() + " D:" + jios.getAtom(i).getImplicitDeuterium());
			}
		}
	}
	
	public static double[] getAllMasses(HDByteMolecularFormula formula) {
		short numberDeuterium = formula.getNumberDeuterium();
		double[] masses = new double[numberDeuterium];
		
		for(int i = 0; i < masses.length; i++) {
			formula.setNumberDeuterium((short)(i + 1));
			masses[i] = formula.getMonoisotopicMass();
		}
		
		formula.setNumberDeuterium(numberDeuterium);
		return masses;
	}
	
	public static String[] getAllFormulas(HDByteMolecularFormula formula) {
		short numberDeuterium = formula.getNumberDeuterium();
		String[] formulas = new String[numberDeuterium];
		
		for(int i = 0; i < formulas.length; i++) {
			formula.setNumberDeuterium((short)(i + 1));
			formulas[i] = formula.toString();
		}
		
		formula.setNumberDeuterium(numberDeuterium);
		return formulas;
	}
	
	/**
	 * 
	 * @param elementsToExchange
	 * @param its
	 * @return
	 */
	public static int[] searchForDeuteriumExchangeablePositions(
			String[] elementsToExchange, IAtomContainer its) {
		ArrayList<Integer> positionsToExchange = new ArrayList<Integer>();
		for (int i = 0; i < its.getAtomCount(); i++) {
			String symbol = its.getAtom(i).getSymbol();
			if (symbol.equals("H"))
				continue;
			for (int k = 0; k < elementsToExchange.length; k++) {
				if (symbol.equals(elementsToExchange[k])) {
					int numHs = getNumberExplicitHydrogens(its, i);
					for(int l = 0; l < numHs; l++) {
						positionsToExchange.add(i);
					}
					break;
				}
			}
		}
		int[] array = new int[positionsToExchange.size()];
		for(int i = 0; i < positionsToExchange.size(); i++) {
			array[i] = positionsToExchange.get(i);
		}
		
		return array;
	}
	
	public static int getNumberExplicitHydrogens(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		int numHs = 0;
		for(IAtom atom : atoms)
			if(atom.getSymbol().equals("H")) numHs++;
		return numHs;
	}

	public static int getNumberExplicitRealHydrogens(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		int numHs = 0;
		for(IAtom atom : atoms)
			if(atom.getSymbol().equals("H") && (atom.getMassNumber() == null || atom.getMassNumber() == 1)) numHs++;
		return numHs;
	}
	
	public static int getNumberExplicitDeuteriums(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		int numDs = 0;
		for(IAtom atom : atoms)
			if(atom.getSymbol().equals("H") && (atom.getMassNumber() != null && atom.getMassNumber() == 2)) numDs++;
		return numDs;
	}
	
	public static boolean addExplicitDeuterium(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		for(IAtom atom : atoms) {
			if(atom.getSymbol().equals("H") && (atom.getMassNumber() == null || atom.getMassNumber() == 1)) {
				atom.setMassNumber(2);
				return true;
			}
		}
		return false;
	}

	public static int setAllExplicitDeuteriums(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		int exchanged = 0;
		for(IAtom atom : atoms)
			if(atom.getSymbol().equals("H") && (atom.getMassNumber() == null || atom.getMassNumber() == 1)) {
				atom.setMassNumber(2);
				exchanged++;
			}
		return exchanged;
	}
	
	public static int[][] getCombinations(int[] toExchange, int numToDraw) {
		ArrayList<String> results = new ArrayList<String>();
		String[] toDrawFrom = new String[toExchange.length];
		for(int i = 0; i < toDrawFrom.length; i++) toDrawFrom[i] = String.valueOf(toExchange[i]);
		
		combinations(toDrawFrom, numToDraw, 0, new String[numToDraw], results);
		
		int[][] combinations = new int[results.size()][numToDraw]; 
		for(int i = 0; i < results.size(); i++) {
			String string = results.get(i).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s+", "");
			String[] tmp = string.split(",");
			for(int j = 0; j < tmp.length; j++) combinations[i][j] = Integer.parseInt(tmp[j]);
		}
		
		return combinations;
	}
	
	public static void combinations(String[] arr, int len, int startPosition, String[] result, ArrayList<String> finalResults){
        if (len == 0){
         	finalResults.add(Arrays.toString(result));
        	return;
        }       
        for (int i = startPosition; i <= arr.length-len; i++){
            result[result.length - len] = arr[i];
            combinations(arr, len-1, i+1, result, finalResults);
        }
    }    
	
	/**
	 * 
	 * @param molecule
	 * @return
	 */
	public static FastBitArray getAromaticAtoms(IAtomContainer molecule) {
		Aromaticity arom = new Aromaticity(ElectronDonation.cdk(),
		Cycles.cdkAromaticSet());
		FastBitArray aromaticAtoms = new FastBitArray(molecule.getAtomCount());
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			arom.apply(molecule);
			Set<IBond> aromaticBonds = arom.findBonds(molecule);
			Iterator<IBond> it = aromaticBonds.iterator();
			while(it.hasNext()) {
				IBond bond = it.next();
				for(int k = 0; k < bond.getAtomCount(); k++)
					aromaticAtoms.set(molecule.getAtomNumber(bond.getAtom(k)));
			}
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return aromaticAtoms;
	}
}
