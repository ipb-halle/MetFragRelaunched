package de.ipbhalle.metfrag.hdx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tautomers.InChITautomerGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.molecularformula.HDByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;

public class InChIDeuteriumGeneration {

	public static void main(String[] args) throws Exception {
		boolean withAromaticRings = false;
		boolean combinatorial = true;
		/*
		 * read input inchis
		 */
		ArrayList<String> inchis = new ArrayList<String>();
		ArrayList<Integer> numberToAddDeuteriums = new ArrayList<Integer>();
		ArrayList<String> identifiers = new ArrayList<String>();
		BufferedReader breader = new BufferedReader(new FileReader(new File(args[0])));
		String line = "";
		int identifier = 1;
		while ((line = breader.readLine()) != null) {
			String[] tmp = line.trim().split("\\s+");
			inchis.add(tmp[0].trim());
			if (tmp.length >= 2)
				numberToAddDeuteriums.add(Integer.parseInt(tmp[1].trim()));
			if (tmp.length == 3)
				identifiers.add(tmp[2].trim());
			else
				identifiers.add(identifier + "");
			identifier++;
		}
		breader.close();
		/*
		 * generate deuterated version of inchi
		 */

		InChITautomerGenerator tg = new InChITautomerGenerator();
		java.io.BufferedWriter bwriter = null;
		if (args.length == 2) {
			bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(args[1])));
		}

		CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
		for (int j = 0; j < inchis.size(); j++) {
			System.out.println(inchis.get(j));
			/*
			 * build the atom container
			 */
			IAtomContainer its = InChIGeneratorFactory.getInstance()
					.getInChIToStructure(inchis.get(j), SilentChemObjectBuilder.getInstance()).getAtomContainer();
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(its);
			try {
				adder.addImplicitHydrogens(its);
			} catch (Exception e) {
				System.out.println("missed " + inchis.get(j) + " " + identifiers.get(j));
				continue;
			}
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(its);

			int numberDeuteriums = 0;
			int numberDeuteriumsEasilyExchanged = 0;
			int numberDeuteriumsAromaticExchanged = 0;

			int[] toExchange = searchForDeuteriumExchangeablePositions(new String[] { "O", "N", "S" }, its);

			if (!combinatorial
					|| (numberToAddDeuteriums.size() == 0 || toExchange.length <= numberToAddDeuteriums.get(j))) {
				for (int i = 0; i < toExchange.length; i++) {
					int numberExchanged = setAllExplicitDeuteriums(its, toExchange[i]);
					numberDeuteriums += numberExchanged;
					numberDeuteriumsEasilyExchanged += numberExchanged;
				}
			} else if (toExchange.length > numberToAddDeuteriums.get(j)) {
				ArrayList<IAtomContainer> deuteratedStrutures = new ArrayList<IAtomContainer>();
				ArrayList<Integer> numberDeuteriumsVec = new ArrayList<Integer>();
				ArrayList<Integer> numberDeuteriumsEasilyExchangedVec = new ArrayList<Integer>();
				// get all possible combinations of exchanges with given number
				// of exchangeable hydrogens
				int[][] combs = getCombinations(toExchange, numberToAddDeuteriums.get(j));

				for (int k = 0; k < combs.length; k++) {
					numberDeuteriumsEasilyExchanged = 0;
					numberDeuteriums = 0;
					InChIToStructure itsNew = InChIGeneratorFactory.getInstance().getInChIToStructure(inchis.get(j),
							SilentChemObjectBuilder.getInstance());

					IAtomContainer con = itsNew.getAtomContainer();
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(con);

					java.util.List<IAtomContainer> tautos = tg.getTautomers(con);
					SmilesGenerator sg = new SmilesGenerator(SmiFlavor.Generic);
					for (IAtomContainer tautomer : tautos) {
						System.out.println(sg.create(tautomer));
					}

					IAtomContainer jiosNew = itsNew.getAtomContainer();
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(jiosNew);
					adder.addImplicitHydrogens(jiosNew);
					AtomContainerManipulator.convertImplicitToExplicitHydrogens(jiosNew);

					// System.out.println(Arrays.toString(combs[k]));
					for (int l = 0; l < combs[k].length; l++) {
						addExplicitDeuterium(jiosNew, combs[k][l]);
						numberDeuteriumsEasilyExchanged++;
						numberDeuteriums++;
					}
					// System.out.println("JniInchiOutputStructure");
					// printInfo(jiosNew);
					numberDeuteriumsVec.add(numberDeuteriums);
					numberDeuteriumsEasilyExchangedVec.add(numberDeuteriumsEasilyExchanged);
					deuteratedStrutures.add(jiosNew);
				}

				for (int k = 0; k < deuteratedStrutures.size(); k++) {

					IAtomContainer con = deuteratedStrutures.get(k);
					InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(con);
					String inchi = gen.getInchi();
					String inchiKey = gen.getInchiKey();

					HDByteMolecularFormula formula = null;
					try {
						formula = new HDByteMolecularFormula(inchi.split("/")[1]);
					} catch (Exception e) {
						System.err.println(identifiers.get(j));
						e.printStackTrace();
						System.exit(1);
					}
					formula.setNumberHydrogens((short) formula.getNumberHydrogens());
					formula.setNumberDeuterium((short) (int) numberDeuteriumsVec.get(k));
					double mass = formula.getMonoisotopicMass();
					System.out.println(identifiers.get(j) + "-" + (k + 1) + "|" + inchi + "|" + formula.toString() + "|"
							+ mass + "|" + inchiKey.split("-")[0] + "|"
							+ inchiKey.split("-")[1] + "|"
							+ numberDeuteriumsEasilyExchangedVec.get(k) + "|0|"
							+ (toExchange.length - numberToAddDeuteriums.get(j)) + "|"
							+ (mass + Constants.getMonoisotopicMassOfAtom("D") - Constants.ELECTRON_MASS) + "|"
							+ (mass - Constants.getMonoisotopicMassOfAtom("D") + Constants.ELECTRON_MASS));
				}
				continue;
			}

			/*
			 * for aromatic ring deuteration generate the cdk atomcontainer and detect
			 * aromatic rings
			 */

			if (withAromaticRings
					|| (numberToAddDeuteriums.size() != 0 && numberToAddDeuteriums.get(j) > numberDeuteriums)) {
				int numberMissing = numberToAddDeuteriums.get(j) - numberDeuteriums;
				numberDeuteriums += numberMissing;
				numberDeuteriumsAromaticExchanged += numberMissing;
			}

			HDByteMolecularFormula formula = null;
			try {
				formula = new HDByteMolecularFormula(MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(its)));
			} catch (Exception e) {
				System.err.println(identifiers.get(j));
				e.printStackTrace();
				System.exit(1);
			}
			formula.setNumberHydrogens((short) formula.getNumberHydrogens());
			formula.setNumberDeuterium((short) numberDeuteriums);
			// Identifier|InChI|MolecularFormula|MonoisotopicMass|InChIKey1|InChIKey2|OSN-Deuteriums|AromaticDeuteriums
			if (numberToAddDeuteriums.size() == 0 || numberDeuteriums == numberToAddDeuteriums.get(j)) {
				
				InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(its);
				String inchi = gen.getInchi();
				String inchiKey = gen.getInchiKey();
				
				double mass = formula.getMonoisotopicMass();
				double negMass = mass - Constants.getMonoisotopicMassOfAtom("D") + Constants.ELECTRON_MASS;
				if (numberDeuteriumsEasilyExchanged + numberDeuteriumsAromaticExchanged == 0)
					negMass = mass - Constants.getMonoisotopicMassOfAtom("H") + Constants.ELECTRON_MASS;
				System.out.println(identifiers.get(j) + "|" + inchi + "|" + formula.toString() + "|" + mass + "|"
						+ inchiKey.split("-")[0] + "|"
						+ inchiKey.split("-")[1] + "|"
						+ numberDeuteriumsEasilyExchanged + "|" + numberDeuteriumsAromaticExchanged + "|0" + "|"
						+ (mass + Constants.getMonoisotopicMassOfAtom("D") - Constants.ELECTRON_MASS) + "|" + negMass);
				if (bwriter != null) {
					bwriter.write(identifiers.get(j) + "|" + inchi + "|" + formula.toString() + "|" + mass + "|"
							+ inchiKey.split("-")[0] + "|"
							+ inchiKey.split("-")[1] + "|"
							+ numberDeuteriumsEasilyExchanged + "|" + numberDeuteriumsAromaticExchanged + "|0" + "|"
							+ (mass + Constants.getMonoisotopicMassOfAtom("D") - Constants.ELECTRON_MASS) + "|"
							+ negMass);
					bwriter.newLine();
				}

			} else if (!combinatorial)
				System.err.println(
						"discarded to many easy exchangeable hydrogens " + numberDeuteriums + " " + identifiers.get(j));
			/*
			 * String[] masses = getAllFormulas(formula);
			 * 
			 * 
			 * for(int i = 0; i < masses.length; i++) { System.out.print(masses[i] + " "); }
			 * System.out.println();
			 */
		}
		if (bwriter != null)
			bwriter.close();

	}

	public static double[] getAllMasses(HDByteMolecularFormula formula) {
		short numberDeuterium = formula.getNumberDeuterium();
		double[] masses = new double[numberDeuterium];

		for (int i = 0; i < masses.length; i++) {
			formula.setNumberDeuterium((short) (i + 1));
			masses[i] = formula.getMonoisotopicMass();
		}

		formula.setNumberDeuterium(numberDeuterium);
		return masses;
	}

	public static String[] getAllFormulas(HDByteMolecularFormula formula) {
		short numberDeuterium = formula.getNumberDeuterium();
		String[] formulas = new String[numberDeuterium];

		for (int i = 0; i < formulas.length; i++) {
			formula.setNumberDeuterium((short) (i + 1));
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
	public static int[] searchForDeuteriumExchangeablePositions(String[] elementsToExchange, IAtomContainer its) {
		ArrayList<Integer> positionsToExchange = new ArrayList<Integer>();
		for (int i = 0; i < its.getAtomCount(); i++) {
			String symbol = its.getAtom(i).getSymbol();
			if (symbol.equals("H"))
				continue;
			for (int k = 0; k < elementsToExchange.length; k++) {
				if (symbol.equals(elementsToExchange[k])) {
					int numHs = getNumberExplicitHydrogens(its, i);
					for (int l = 0; l < numHs; l++) {
						positionsToExchange.add(i);
					}
					break;
				}
			}
		}
		int[] array = new int[positionsToExchange.size()];
		for (int i = 0; i < positionsToExchange.size(); i++) {
			array[i] = positionsToExchange.get(i);
		}

		return array;
	}

	public static int getNumberExplicitHydrogens(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		int numHs = 0;
		for (IAtom atom : atoms)
			if (atom.getSymbol().equals("H"))
				numHs++;
		return numHs;
	}
	
	public static boolean addExplicitDeuterium(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		for (IAtom atom : atoms) {
			if (atom.getSymbol().equals("H") && (atom.getMassNumber() == null || atom.getMassNumber() == 1)) {
				atom.setMassNumber(2);
				return true;
			}
		}
		return false;
	}
	
	public static int setAllExplicitDeuteriums(IAtomContainer its, int pos) {
		java.util.List<IAtom> atoms = its.getConnectedAtomsList(its.getAtom(pos));
		int exchanged = 0;
		for (IAtom atom : atoms)
			if (atom.getSymbol().equals("H") && (atom.getMassNumber() == null || atom.getMassNumber() == 1)) {
				atom.setMassNumber(2);
				exchanged++;
			}
		return exchanged;
	}

	public static int[][] getCombinations(int[] toExchange, int numToDraw) {
		ArrayList<String> results = new ArrayList<String>();
		String[] toDrawFrom = new String[toExchange.length];
		for (int i = 0; i < toDrawFrom.length; i++)
			toDrawFrom[i] = String.valueOf(toExchange[i]);

		combinations(toDrawFrom, numToDraw, 0, new String[numToDraw], results);

		int[][] combinations = new int[results.size()][numToDraw];
		for (int i = 0; i < results.size(); i++) {
			String string = results.get(i).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s+", "");
			String[] tmp = string.split(",");
			for (int j = 0; j < tmp.length; j++)
				combinations[i][j] = Integer.parseInt(tmp[j]);
		}

		return combinations;
	}

	public static void combinations(String[] arr, int len, int startPosition, String[] result,
			ArrayList<String> finalResults) {
		if (len == 0) {
			finalResults.add(Arrays.toString(result));
			return;
		}
		for (int i = startPosition; i <= arr.length - len; i++) {
			result[result.length - len] = arr[i];
			combinations(arr, len - 1, i + 1, result, finalResults);
		}
	}

	/**
	 * 
	 * @param molecule
	 * @return
	 */
	public static FastBitArray getAromaticAtoms(IAtomContainer molecule) {
		Aromaticity arom = new Aromaticity(ElectronDonation.cdk(), Cycles.cdkAromaticSet());
		FastBitArray aromaticAtoms = new FastBitArray(molecule.getAtomCount());
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
			arom.apply(molecule);
			Set<IBond> aromaticBonds = arom.findBonds(molecule);
			Iterator<IBond> it = aromaticBonds.iterator();
			while (it.hasNext()) {
				IBond bond = it.next();
				for (int k = 0; k < bond.getAtomCount(); k++)
					aromaticAtoms.set(molecule.indexOf(bond.getAtom(k)));
			}
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return aromaticAtoms;
	}
}
