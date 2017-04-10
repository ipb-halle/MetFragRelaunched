package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.FileWriter;

import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.io.SDFWriter;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.settings.Settings;

public class CandidateListWriterSDF implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean writeFile(File file, IList list, Settings settings) throws Exception {
		IAtomContainerSet set = new AtomContainerSet();
		CandidateList candidateList = null;
		int numberOfPeaksUsed = 0;
		if (list instanceof ScoredCandidateList
				|| list instanceof SortedScoredCandidateList) {
			candidateList = (ScoredCandidateList) list;
			numberOfPeaksUsed = ((ScoredCandidateList) list)
					.getNumberPeaksUsed();
		}
		if (list instanceof CandidateList) {
			candidateList = (CandidateList) list;
		}
		if (candidateList == null)
			return false;
		for (int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate candidate = candidateList.getElement(i);
			IAtomContainer candidateAtomContainer = null;
			try {
				candidateAtomContainer = candidateList.getElement(i).getAtomContainer();
			} catch (Exception e1) {
				System.err.println("Error saving: " + candidateList.getElement(i).getIdentifier());
				continue;
			}

			ICandidate scoredCandidate = candidateList.getElement(i);
			if (scoredCandidate.getMatchList() != null) {
				MatchList matchList = scoredCandidate.getMatchList();
				int countExplainedPeaks = 0;
				for (int l = 0; l < matchList.getNumberElements(); l++) {
					try {
						matchList.getElement(l).getMatchedPeak().getIntensity();
					} catch (RelativeIntensityNotDefinedException e1) {
						continue;
					}
					countExplainedPeaks++;
				}
				candidateAtomContainer.setProperty("NumberPeaksExplained",
						countExplainedPeaks);
			}
			String peaksExplained = "";
			String sumFormulasOfFragmentsExplainedPeaks = "";
			String fragmentAtomArrays = "";
			String fragmentBondArrays = "";
			String fragmentBrokenBondArrays = "";
			if (scoredCandidate.getMatchList() != null) {
				for (int ii = 0; ii < scoredCandidate.getMatchList()
						.getNumberElements(); ii++) {
					try {
						double intensity = scoredCandidate.getMatchList()
								.getElement(ii).getMatchedPeak().getIntensity();
						peaksExplained += scoredCandidate.getMatchList()
								.getElement(ii).getMatchedPeak().getMass()
								+ "_" + intensity + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						continue;
					}
					String formula = scoredCandidate.getMatchList()
							.getElement(ii)
							.getModifiedFormulaStringOfBestMatchedFragment();
					sumFormulasOfFragmentsExplainedPeaks += scoredCandidate
							.getMatchList().getElement(ii).getMatchedPeak()
							.getMass()
							+ ":" + formula + ";";
					fragmentAtomArrays += ((DefaultBitArrayFragment) scoredCandidate
							.getMatchList().getElement(ii)
							.getBestMatchedFragment()).getAtomsFastBitArray()
							+ ";";
					fragmentBondArrays += ((DefaultBitArrayFragment) scoredCandidate
							.getMatchList().getElement(ii)
							.getBestMatchedFragment()).getBondsFastBitArray()
							+ ";";
					fragmentBrokenBondArrays += ((DefaultBitArrayFragment) scoredCandidate
							.getMatchList().getElement(ii)
							.getBestMatchedFragment()).getBrokenBondsFastBitArray()
							+ ";";
				}
				if (sumFormulasOfFragmentsExplainedPeaks.length() != 0)
					sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks
							.substring(0, sumFormulasOfFragmentsExplainedPeaks
									.length() - 1);
				if (peaksExplained.length() != 0)
					peaksExplained = peaksExplained.substring(0,
							peaksExplained.length() - 1);
				if (peaksExplained.length() == 0)
					peaksExplained = "NA";
				if (fragmentAtomArrays.length() != 0)
					fragmentAtomArrays = fragmentAtomArrays.substring(0,
							fragmentAtomArrays.length() - 1);
				if (fragmentBondArrays.length() != 0)
					fragmentBondArrays = fragmentBondArrays.substring(0,
							fragmentBondArrays.length() - 1);
				if (fragmentBrokenBondArrays.length() != 0)
					fragmentBrokenBondArrays = fragmentBrokenBondArrays
							.substring(0, fragmentBrokenBondArrays.length() - 1);
				if (sumFormulasOfFragmentsExplainedPeaks.length() == 0)
					sumFormulasOfFragmentsExplainedPeaks = "NA";
				candidateAtomContainer.setProperty("PeaksExplained",
						peaksExplained);
				candidateAtomContainer.setProperty("FragmentMolecularFormulas",
						sumFormulasOfFragmentsExplainedPeaks);
			}

			java.util.Enumeration<String> keys = candidate.getProperties()
					.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				candidateAtomContainer.setProperty(key,
						checkEmptyProperty(candidate.getProperty(key)));
			}
			candidateAtomContainer.setProperty("NumberPeaksUsed",
					numberOfPeaksUsed);

			MoleculeFunctions.prepareAtomContainer(candidateAtomContainer, true);
			MoleculeFunctions.convertExplicitToImplicitHydrogens(candidateAtomContainer);
			set.addAtomContainer(candidateAtomContainer);
		}

		SDFWriter writer = new SDFWriter(new FileWriter(file));
		writer.write(set);
		writer.close();
		return true;
	}

	private Object checkEmptyProperty(Object prop) {
		try {
			String value = (String)prop;
			if(value.trim().length() == 0) return "NA";
		}
		catch(Exception e) {
			return prop;
		}
		return prop;
	}
	
	public void nullify() {

	}

	@Override
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path
				+ Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".sdf"), list, null);
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), list, null);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}

}
