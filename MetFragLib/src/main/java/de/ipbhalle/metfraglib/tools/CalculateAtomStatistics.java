package de.ipbhalle.metfraglib.tools;

import java.util.List;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.database.LocalPropertyFileDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterXLS;

public class CalculateAtomStatistics {

	public static void main(String[] args) {
		
		String filename = args[0];
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filename);
		
		LocalPropertyFileDatabase localPropertyFileDatabase = new LocalPropertyFileDatabase(settings);
		
		java.util.Vector<String> identifiers = null;
		try {
			identifiers = localPropertyFileDatabase.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		CandidateList candidates = localPropertyFileDatabase.getCandidateByIdentifier(identifiers);
		
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			IAtomContainer mol = MoleculeFunctions.parseSmiles((String)candidates.getElement(i).getProperty(VariableNames.SMILES_NAME));
			Aromaticity arom = new Aromaticity(ElectronDonation.cdk(), Cycles.cdkAromaticSet());
			try {
				java.util.Set<IBond> aromaticBonds = arom.findBonds(mol);
				java.util.Vector<Integer> aromaticsAtomIds = new java.util.Vector<Integer>();
				java.util.Iterator<IBond> it = aromaticBonds.iterator();
				while(it.hasNext()) {
					IBond bond = it.next();
					int atom1 = mol.getAtomNumber(bond.getAtom(0));
					int atom2 = mol.getAtomNumber(bond.getAtom(1));
					if(!aromaticsAtomIds.contains(atom1)) aromaticsAtomIds.add(atom1);
					if(!aromaticsAtomIds.contains(atom2)) aromaticsAtomIds.add(atom2);
				}
				
				determineOxygenAtomStatistics(mol, candidates.getElement(i), aromaticsAtomIds);
				determineNitrogenAtomStatistics(mol, candidates.getElement(i), aromaticsAtomIds);
				determineSulfurAtomStatistics(mol, candidates.getElement(i), aromaticsAtomIds);
				determineCarbonAtomStatistics(mol, candidates.getElement(i), aromaticsAtomIds);
			} catch (CDKException e) {
				e.printStackTrace();
			}
			
		}
		CandidateListWriterXLS writer = new CandidateListWriterXLS();
		try {
			writer.write(candidates, "ID_SMILES_Sample_Name_processed", "/home/cruttkie/Dokumente/PhD/MetFrag/ufz");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void determineCarbonAtomStatistics(IAtomContainer mol, ICandidate candidate, java.util.Vector<Integer> aromaticAtomIndexes) {
		int numberC = 0;
		int numberAliphaticCH = 0;
		int numberAromaticCH = 0;
		for(int i = 0; i < mol.getAtomCount(); i++) {
			IAtom atom = mol.getAtom(i);
			if(atom.getSymbol().equals("C")) {
				numberC++;
				int hydrogens = atom.getImplicitHydrogenCount();
				if(aromaticAtomIndexes.contains(i)) numberAromaticCH += hydrogens;
				else numberAliphaticCH += hydrogens;
			}
		}

		candidate.setProperty("#C", numberC);
		candidate.setProperty("#aliphaticCH", numberAliphaticCH);
		candidate.setProperty("#aromaticCH", numberAromaticCH);
	}
	
	public static void determineSulfurAtomStatistics(IAtomContainer mol, ICandidate candidate, java.util.Vector<Integer> aromaticAtomIndexes) {
		int numberS = 0;
		int numberSH = 0;
		int numberSOO = 0;
		int numberSOOO = 0;
		int numberSOOOO = 0;
		int numberSnoH = 0;
		
		for(int i = 0; i < mol.getAtomCount(); i++) {
			IAtom atom = mol.getAtom(i);
			if(atom.getSymbol().equals("S")) {
				numberS++;
				int hydrogens = atom.getImplicitHydrogenCount();
				int oxygens = countOxygens(mol.getConnectedAtomsList(atom));
				if(oxygens == 4) numberSOOOO++;
				else if(oxygens == 3) numberSOOO++;
				else if(oxygens == 2) numberSOO++;
				else if(hydrogens == 1) numberSH++;
				else if(hydrogens == 0)  numberSnoH++;
				else System.err.println("Somthing wrong with S and " + candidate.getProperty("Identifier"));
			}
		}
		
		candidate.setProperty("#S", numberS);
		candidate.setProperty("#SH", numberSH);
		candidate.setProperty("#SO2", numberSOO);
		candidate.setProperty("#SO3", numberSOOO);
		candidate.setProperty("#SO4", numberSOOOO);	
		candidate.setProperty("#S(noH)", numberSnoH);		
	}
	
	public static int countOxygens(List<IAtom> atoms) {
		int number = 0;
		for(int i = 0; i < atoms.size(); i++)
			number++;
		return number;
	}
	
	public static void determineNitrogenAtomStatistics(IAtomContainer mol, ICandidate candidate, java.util.Vector<Integer> aromaticAtomIndexes) {
		int numberN = 0;
		int numberNH = 0;
		int numberNHH = 0;
		int aromaticNH = 0;
		int aromaticN = 0;
		
		for(int i = 0; i < mol.getAtomCount(); i++) {
			IAtom atom = mol.getAtom(i);
			if(atom.getSymbol().equals("N")) {
				numberN++;
				int hydrogens = atom.getImplicitHydrogenCount();
				if(aromaticAtomIndexes.contains(i)) {
					if(hydrogens == 0) aromaticN++;
					else if(hydrogens == 1) aromaticNH++;
					else System.err.println("Somthing wrong with aromatic N and " + candidate.getProperty("Identifier"));
				}
				else {
					if(hydrogens == 0) continue;
					else if(hydrogens == 1) numberNH++;
					else if(hydrogens == 2) numberNHH++;
					else System.err.println("Somthing wrong with aliphatic N and " + candidate.getProperty("Identifier"));
				}
			}
		}
		
		candidate.setProperty("#N", numberN);
		candidate.setProperty("#NHonly", numberNH);
		candidate.setProperty("#NH2", numberNHH);
		candidate.setProperty("#aromaticNH", aromaticNH);
		candidate.setProperty("#aromaticN", aromaticN);		
	}
	
	public static void determineOxygenAtomStatistics(IAtomContainer mol, ICandidate candidate, java.util.Vector<Integer> aromaticAtomIndexes) throws CDKException {
		SMARTSQueryTool smartsQuerytools = new SMARTSQueryTool("[CX3](=O)[OX2H1]", DefaultChemObjectBuilder.getInstance());
		int numberO = 0;
		int numberOH = 0;
		int numberCO = 0;
		int numberCOOH = 0;
		int numberOnoH = 0;
		FastBitArray carboxyGroupsMarkedOxygens = new FastBitArray(mol.getAtomCount());
		if(smartsQuerytools.matches(mol)) {
			List<List<Integer>> groups = smartsQuerytools.getMatchingAtoms();
			numberCOOH = groups.size();
			for(int i = 0; i < groups.size(); i++) {
				for(int j = 0; j < groups.get(i).size(); j++) {
					if(mol.getAtom(groups.get(i).get(j)).getSymbol().equals("O")) {
						carboxyGroupsMarkedOxygens.set(groups.get(i).get(j));
					}
				}
			}
		}
		FastBitArray carbonylGroupsMarkedOxygens = new FastBitArray(mol.getAtomCount());
		smartsQuerytools = new SMARTSQueryTool("[CX3]=[OX1] ", DefaultChemObjectBuilder.getInstance());
		if(smartsQuerytools.matches(mol)) {
			List<List<Integer>> groups = smartsQuerytools.getMatchingAtoms();
			for(int i = 0; i < groups.size(); i++) {
				for(int j = 0; j < groups.get(i).size(); j++) {
					if(mol.getAtom(groups.get(i).get(j)).getSymbol().equals("O")) {
						if(!carboxyGroupsMarkedOxygens.get(groups.get(i).get(j))) {
							carbonylGroupsMarkedOxygens.set(groups.get(i).get(j));
							numberCO++;
						}
					}
				}
			}
		}
		for(int i = 0; i < mol.getAtomCount(); i++) {
			IAtom atom = mol.getAtom(i);
			if(atom.getSymbol().equals("O")) {
				numberO++;
				if(atom.getImplicitHydrogenCount() > 0 && !carboxyGroupsMarkedOxygens.get(i)) numberOH++;
				if(atom.getImplicitHydrogenCount() == 0 && !carboxyGroupsMarkedOxygens.get(i) && !carbonylGroupsMarkedOxygens.get(i)) numberOnoH++;
			}
		}
		
		candidate.setProperty("#O", numberO);
		candidate.setProperty("#OH", numberOH);
		candidate.setProperty("#C=O", numberCO);
		candidate.setProperty("#COOH", numberCOOH);
		candidate.setProperty("#O(noH)", numberOnoH);		
	}
	
	
	
}
