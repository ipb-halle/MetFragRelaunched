package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.additionals.Bond;
import de.ipbhalle.metfraglib.additionals.BondEnergies;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.match.DefaultFragmentToPeakMatch;
import de.ipbhalle.metfraglib.match.FragmentFormulaToPeakMatch;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * score created for the new community fragmenter
 * combines peakScore and bond energy score in one single summand
 */
public class NewFragmenterHierarchicalScore extends AbstractScore {

	protected final double ALPHA = 1.84;
	protected final double BETA = 0.59;
	protected final double GAMMA = 0.47;
	
	protected double[] parameters = {ALPHA, BETA, GAMMA}; 
	
	protected final double WEIGHT_HYDROGEN_PENALTY = 100.0;

	protected ICandidate scoredCandidate;
	protected Byte maximumTreeDepth;
	protected double[] bondEnergies;
	protected int[] bestFragmentIndeces;
	protected MatchList matchList;
	
	protected Double measuredPrecursorMass;
	
	public NewFragmenterHierarchicalScore(Settings settings) {
		super(settings);
		this.scoredCandidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.maximumTreeDepth = (Byte)settings.get(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		this.measuredPrecursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		this.interimResultsCalculated = false;
		this.usesPiecewiseCalculation = true;
		this.initialise();
	}
	
	public void calculate() {
		this.matchList = (MatchList)settings.get(VariableNames.MATCH_LIST_NAME);
		this.value = new Double(0);
		if(this.matchList == null) return;
		this.bestFragmentIndeces = new int[this.matchList.getNumberElements()];
		this.optimalValues = new double[this.matchList.getNumberElements()];
		for(int i = 0; i < this.matchList.getNumberElements(); i++) {
			DefaultFragmentToPeakMatch currentMatch = null;
			if(this.matchList.getElement(i) instanceof FragmentMassToPeakMatch) 
				currentMatch = (FragmentMassToPeakMatch)this.matchList.getElement(i);
			else 
				currentMatch = (FragmentFormulaToPeakMatch)this.matchList.getElement(i);
			FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
			double minimumEnergyPerMatch = (double)Integer.MAX_VALUE;
			double peakScore = 0.0;
			try {
				peakScore = Math.pow((currentMatch.getMatchedPeak().getMass() / this.measuredPrecursorMass) * 10.0, this.parameters[0]) 
						* Math.pow(currentMatch.getMatchedPeak().getIntensity(), this.parameters[1]);
			} catch (RelativeIntensityNotDefinedException e) {
				e.printStackTrace();
			}
			for(int ii = 0; ii < currentFragmentList.getNumberElements(); ii++) {
				IFragment currentFragment = currentFragmentList.getElement(ii);
				/*
				 * check if current fragment is valid based on the tree depth
				 */
				if(currentFragment.getTreeDepth() > this.maximumTreeDepth) continue;
				int[] brokenBondIndeces = currentFragment.getBrokenBondIndeces();
				double energyOfFragment = 0.0;
				for(int bondIndex : brokenBondIndeces) {
					energyOfFragment += this.bondEnergies[bondIndex];
				}
				energyOfFragment += Math.abs(currentMatch.getNumberOfHydrogensDifferToPeakMass(ii)) * this.WEIGHT_HYDROGEN_PENALTY;
				/*
				 * assign optimal bondenergy and store best fragment
				 */
				if(energyOfFragment < minimumEnergyPerMatch) {
					minimumEnergyPerMatch = energyOfFragment;
					this.bestFragmentIndeces[i] = ii;
				}
			}
			currentMatch.initialiseBestMatchedFragment(this.bestFragmentIndeces[i]);
			this.value += peakScore / Math.pow(minimumEnergyPerMatch, this.parameters[2]);
			this.optimalValues[i] = minimumEnergyPerMatch;
		}
		this.calculationFinished = true;
	}

	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
	/**
	 * 
	 */
	public Double[] calculateSingleMatch(IMatch match) {
		DefaultFragmentToPeakMatch currentMatch = null;
		if(match instanceof FragmentMassToPeakMatch) 
			currentMatch = (FragmentMassToPeakMatch)match;
		else 
			currentMatch = (FragmentFormulaToPeakMatch)match;
		FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
		double minimumEnergyPerMatch = (double)Integer.MAX_VALUE;
		double peakScore = 0.0;
		try {
			peakScore = Math.pow((currentMatch.getMatchedPeak().getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
					* Math.pow(currentMatch.getMatchedPeak().getIntensity(), this.BETA);
		} catch (RelativeIntensityNotDefinedException e) {
			e.printStackTrace();
		}
		for(int ii = 0; ii < currentFragmentList.getNumberElements(); ii++) {
			IFragment currentFragment = currentFragmentList.getElement(ii);
			/*
			 * check if current fragment is valid based on the tree depth
			 */
			if(currentFragment.getTreeDepth() > this.maximumTreeDepth) continue;
			int[] brokenBondIndeces = this.getBrokenBondIndeces(currentFragment);
			double energyOfFragment = 0.0;
			for(int bondIndex : brokenBondIndeces) {
				energyOfFragment += this.bondEnergies[bondIndex];
			}
			energyOfFragment += Math.abs(currentMatch.getNumberOfHydrogensDifferToPeakMass(ii)) * this.WEIGHT_HYDROGEN_PENALTY;
			/*
			 * assign optimal bondenergy and store best fragment
			 */
			if(energyOfFragment < minimumEnergyPerMatch) {
				minimumEnergyPerMatch = energyOfFragment;
			}
		}
		this.calculationFinished = true;
		return new Double[] {peakScore / Math.pow(minimumEnergyPerMatch, this.GAMMA), minimumEnergyPerMatch};
	}
	
	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * retrieve broken bonds of given fragment that haven't been considered yet
	 * 
	 * @param currentFragment
	 * @return
	 */
	protected int[] getBrokenBondIndeces(IFragment currentFragment) {
		AbstractTopDownBitArrayFragment castFragment = null;
		BitArray currentBrokenBondIndeces = null;
		try {
			castFragment = ((AbstractTopDownBitArrayFragment)currentFragment);
		}
		catch(ClassCastException e) {
			return currentFragment.getBrokenBondIndeces();
		}
		currentBrokenBondIndeces = castFragment.getBrokenBondsBitArray();
		AbstractTopDownBitArrayFragment father = castFragment.getPrecursorFragment();
		while (father != null) {
			if(father.hasMatched()) {
				currentBrokenBondIndeces = currentBrokenBondIndeces.getDiff(father.getBrokenBondsBitArray());
				
				break;
			}
			father = father.getPrecursorFragment();
		}
		return currentBrokenBondIndeces.getSetIndeces();
	}
	
	protected void initialise() {
		this.bondEnergies = new double[((BitArrayPrecursor)this.scoredCandidate.getPrecursorMolecule()).getNonHydrogenBondCount()];
		BitArrayPrecursor bitArrayPrecursor = (BitArrayPrecursor)this.scoredCandidate.getPrecursorMolecule();
		for(short i = 0; i < bitArrayPrecursor.getNonHydrogenBondCount(); i++) {
			String[] bondAtomsAsString = bitArrayPrecursor.getBondAtomsAsString(i);
			this.bondEnergies[i] = 
					this.getSingleBondEnergy(
							bondAtomsAsString[0], bondAtomsAsString[1], 
							bitArrayPrecursor.getBondOrder(i), bitArrayPrecursor.isAromaticBond(i));
		}
	}
	
	protected double getSingleBondEnergy(String symbol1, String symbol2, String type, boolean isAromaticBond) {
		char typeChar = '=';
		if (!isAromaticBond) {
			if (type.equals("SINGLE"))
				typeChar = '-';
			else if (type.equals("DOUBLE"))
				typeChar = '=';
			else if (type.equals("TRIPLE"))
				typeChar = '~';
			else typeChar = '-';
		}
		BondEnergies be = (BondEnergies)this.settings.get(VariableNames.BOND_ENERGY_OBJECT_NAME);
		return be.get(new Bond(symbol1, symbol2, typeChar));
	}
	
	public void shallowNullify() {
		this.nullify();
	}
	
	public void nullify() {
		super.nullify();
		this.bondEnergies = null;
		this.optimalValues = null;
	}
}
