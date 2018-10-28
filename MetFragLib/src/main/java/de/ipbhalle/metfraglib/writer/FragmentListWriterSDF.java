package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.io.SDFWriter;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;
import de.ipbhalle.metfraglib.settings.Settings;

public class FragmentListWriterSDF implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean write(IList list, String filename, String path) {
		if(list instanceof ScoredCandidateList) {
			ScoredCandidateList scoredCandidateList = (ScoredCandidateList) list;
			for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
				ICandidate candidate = scoredCandidateList.getElement(i);
				try {
					candidate.initialisePrecursorCandidate();
				} catch (AtomTypeNotKnownFromInputListException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!this.writeSingleMatchList(candidate, candidate.getMatchList(), path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + "_" + candidate.getIdentifier() + ".sdf"))
					System.err.println("Warning: Could not write fragments of candidate " + i + ": " + candidate.getIdentifier());
			}
		}
		return false;
	}

	private boolean writeSingleMatchList(ICandidate candidate, MatchList matchList, String completeFilePath) {
		IAtomContainerSet set = new AtomContainerSet();
		for(int i = 0; i < matchList.getNumberElements(); i++) {
			IAtomContainer fragmentAtomContainer = matchList.getElement(i).getBestMatchedFragment().getStructureAsIAtomContainer(candidate.getPrecursorMolecule());
			fragmentAtomContainer.setProperty("MolecularFormula", matchList.getElement(i).getModifiedFormulaStringOfBestMatchedFragment(candidate.getPrecursorMolecule()).toString());
			String brokenBonds = "";
			int[] brokenBondIndeces = matchList.getElement(i).getBestMatchedFragment().getBrokenBondIndeces();
			String bondStrings = "";
			for(int index : brokenBondIndeces) {
				brokenBonds += index + ";";
				bondStrings += ((BitArrayPrecursor)candidate.getPrecursorMolecule()).getBondAsString((short)index) + ";";
			}
			if(bondStrings.length() != 0) bondStrings = bondStrings.substring(0, bondStrings.length() - 1);
			if(brokenBonds.length() != 0) brokenBonds = brokenBonds.substring(0, brokenBonds.length() - 1);
			fragmentAtomContainer.setProperty("BrokenBondsStrings", bondStrings);
			fragmentAtomContainer.setProperty("BrokenBonds", brokenBonds);
			fragmentAtomContainer.setProperty("Match", matchList.getElement(i).toString());
			
			set.addAtomContainer(fragmentAtomContainer);
		}
		try {
			SDFWriter sdfWriter = new SDFWriter(new FileWriter(new File(completeFilePath)));
			sdfWriter.write(set);
			sdfWriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (CDKException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void nullify() {
		return;
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean writeFile(File file, IList list, Settings settings) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
