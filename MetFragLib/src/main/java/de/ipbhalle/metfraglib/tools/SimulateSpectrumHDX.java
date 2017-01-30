package de.ipbhalle.metfraglib.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.precursor.HDTopDownBitArrayPrecursor;
import de.ipbhalle.metfraglib.precursor.TopDownBitArrayPrecursor;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.match.DefaultFragmentToPeakMatch;

public class SimulateSpectrumHDX {

	public static void main(String[] args) throws Exception {
		
		MetFragGlobalSettings settings = MetFragGlobalSettings.readSettings(new File(args[0]), null);
		String outputfile = args[1];
		int method = Integer.parseInt(args[2]);
		String type = args[3];
		
		CombinedMetFragProcess metfragProcess = new CombinedMetFragProcess(settings);
		
		try {
			metfragProcess.retrieveCompounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
		metfragProcess.run();
		
		ScoredCandidateList scoredCandidateList = (ScoredCandidateList)metfragProcess.getCandidateList();
		
		try {
			double[][] spectrum = null;
			if(type.equals("pos")) spectrum = generateDeuteratedSpectrumPositive(scoredCandidateList.getElement(0), method);
			else if(type.equals("neg")) spectrum = generateDeuteratedSpectrumNegative(scoredCandidateList.getElement(0), method);
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputfile)));
			for(int i = 0; i < spectrum.length; i++) {
				bwriter.write(spectrum[i][0] + " " + spectrum[i][1]);
				bwriter.newLine();
			}
			bwriter.close();
		} catch (AtomTypeNotKnownFromInputListException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param candidate
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException
	 * @throws Exception
	 */
	public static double[][] generateDeuteratedSpectrumPositive(ICandidate candidate, int method) throws AtomTypeNotKnownFromInputListException, Exception {
		int numberPositions = searchForDeuteriumExchangeablePositions((TopDownBitArrayPrecursor)candidate.getPrecursorMolecule()).length;
		HDTopDownBitArrayPrecursor preHDX = new HDTopDownBitArrayPrecursor(candidate.getImplicitHydrogenAtomContainer(), (byte)numberPositions);
		preHDX.preprocessPrecursor();
		MatchList matchList = candidate.getMatchList();
		double[][] spectrumHDX = null;
		if(method == 3) spectrumHDX = new double[matchList.getNumberElements() * 2][2];
		else spectrumHDX = new double[matchList.getNumberElements()][2];
		for(int i = 0; i < matchList.getNumberElements(); i++) {
			IFragment frag = matchList.getElement(i).getBestMatchedFragment();
			IPeak peak = matchList.getElement(i).getMatchedPeak();
			int[] setAtoms = ((DefaultBitArrayFragment)frag).getAtomsBitArray().getSetIndeces();
			int numberDeuteriums = 0;
			for(int k = 0; k < setAtoms.length; k++) {
				numberDeuteriums += preHDX.getNumberDeuteriumsConnectedToAtomIndex(0, setAtoms[k]);
			}
			spectrumHDX[i][1] = peak.getAbsoluteIntensity();
			// [M+D]+
			if(method == 1) {
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            spectrumHDX[i][0] += Constants.getMonoisotopicMassOfAtom("D") - Constants.HYDROGEN_MASS;
                        }
                        // [M]+
                        else if(method == 2) {
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            // nothing to do
                        }
                        // [M]+ [M+D]+
                        else if(method == 3) {
                            spectrumHDX[i * 2][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            spectrumHDX[(i * 2) + 1][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D"); 
                            spectrumHDX[(i * 2) + 1][0] += Constants.getMonoisotopicMassOfAtom("D") - Constants.HYDROGEN_MASS;
                        }
                        // predict based on deuteriums
                        else if(method == 4) {
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            if(numberDeuteriums != 0) spectrumHDX[i][0] += Constants.getMonoisotopicMassOfAtom("D") - Constants.HYDROGEN_MASS;
                        }
                        // predict based on adduct
                        else if(method == 5) {
                            byte adductTypeIndex = ((DefaultFragmentToPeakMatch)matchList.getElement(i)).getBestMatchedFragmentAdductTypeIndex();
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            if(adductTypeIndex == 2) spectrumHDX[i][0] += Constants.getMonoisotopicMassOfAtom("D") - Constants.HYDROGEN_MASS;
                        }
		}
		return spectrumHDX;
	}
	
	/**
	 * 
	 * @param candidate
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException
	 * @throws Exception
	 */
	public static double[][] generateDeuteratedSpectrumNegative(ICandidate candidate, int method) throws AtomTypeNotKnownFromInputListException, Exception {
		int numberPositions = searchForDeuteriumExchangeablePositions((TopDownBitArrayPrecursor)candidate.getPrecursorMolecule()).length;
		HDTopDownBitArrayPrecursor preHDX = new HDTopDownBitArrayPrecursor(candidate.getImplicitHydrogenAtomContainer(), (byte)numberPositions);
		preHDX.preprocessPrecursor();
		MatchList matchList = candidate.getMatchList();
		double[][] spectrumHDX = null;
		if(method == 3) spectrumHDX = new double[matchList.getNumberElements() * 2][2];
		else spectrumHDX = new double[matchList.getNumberElements()][2];
		for(int i = 0; i < matchList.getNumberElements(); i++) {
			IFragment frag = matchList.getElement(i).getBestMatchedFragment();
			IPeak peak = matchList.getElement(i).getMatchedPeak();
			int[] setAtoms = ((DefaultBitArrayFragment)frag).getAtomsBitArray().getSetIndeces();
			int numberDeuteriums = 0;
			for(int k = 0; k < setAtoms.length; k++) {
				numberDeuteriums += preHDX.getNumberDeuteriumsConnectedToAtomIndex(0, setAtoms[k]);
			}
			spectrumHDX[i][1] = peak.getAbsoluteIntensity();
			// [M-D]-
			if(method == 1) {
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            spectrumHDX[i][0] += Constants.HYDROGEN_MASS - Constants.getMonoisotopicMassOfAtom("D");
                        }
                        // [M]-
                        else if(method == 2) {
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            // nothing to do
                        }
                        // [M]- [M-D]-
                        else if(method == 3) {
                            spectrumHDX[i * 2][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            spectrumHDX[(i * 2) + 1][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D"); 
                            spectrumHDX[(i * 2) + 1][0] += Constants.HYDROGEN_MASS - Constants.getMonoisotopicMassOfAtom("D");
                        }
                        // predict based on deuteriums
                        else if(method == 4) {
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            if(numberDeuteriums != 0) spectrumHDX[i][0] += Constants.HYDROGEN_MASS - Constants.getMonoisotopicMassOfAtom("D");
                        }
                        // predict based on adduct
                        else if(method == 5) {
                            byte adductTypeIndex = ((DefaultFragmentToPeakMatch)matchList.getElement(i)).getBestMatchedFragmentAdductTypeIndex();
                            spectrumHDX[i][0] = peak.getMass() - (numberDeuteriums) * Constants.HYDROGEN_MASS + (numberDeuteriums) * Constants.getMonoisotopicMassOfAtom("D");
                            if(adductTypeIndex == 1) spectrumHDX[i][0] += Constants.getMonoisotopicMassOfAtom("D") - Constants.HYDROGEN_MASS;
                        }
		}
		return spectrumHDX;
	}
	
	/**
	 * 
	 * @param con
	 * @return
	 */
	public static int[] searchForDeuteriumExchangeablePositions(TopDownBitArrayPrecursor con) {
		String[] elementsToExchange = Constants.EXCHANGEABLE_DEUTERIUM_POSITIONS;
		java.util.Vector<Integer> positionsToExchange = new java.util.Vector<Integer>();
		for (int i = 0; i < con.getNonHydrogenAtomCount(); i++) {
			String symbol = con.getAtomSymbol(i);
			if (symbol.equals("H"))
				continue;
			for (int k = 0; k < elementsToExchange.length; k++) {
				if (symbol.equals(elementsToExchange[k]) && con.getNumberHydrogensConnectedToAtomIndex(i) > 0) {
					for(int l = 0; l < con.getNumberHydrogensConnectedToAtomIndex(i); l++) {
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
	
}
