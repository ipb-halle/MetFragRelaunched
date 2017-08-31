package de.ipbhalle.metfraglib.fragmenter;

import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragmenter;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractTopDownFragmenter implements IFragmenter {

	protected Settings settings;
	protected ICandidate scoredCandidate;
	protected Byte maximumTreeDepth;
	protected Double minimumFragmentMassLimit;
	
	public AbstractTopDownFragmenter(Settings settings) {
		this.settings = settings;
		this.scoredCandidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.maximumTreeDepth = (Byte)settings.get(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
	}

	public abstract FragmentList generateFragments();

	public Byte getMaximumTreeDepth() {
		return this.maximumTreeDepth;
	}

	public void setMaximumTreeDepth(Byte maximumTreeDepth) {
		this.maximumTreeDepth = maximumTreeDepth;
	}

	public IMolecularStructure getPrecursorMolecule() {
		return this.scoredCandidate.getPrecursorMolecule();
	}

	public abstract java.util.ArrayList<AbstractTopDownBitArrayFragment> getFragmentsOfNextTreeDepth(AbstractTopDownBitArrayFragment precursorFragment);
	
	public void nullify() {
		this.maximumTreeDepth = null;
		this.minimumFragmentMassLimit = null;
	}


	public Double getMinimumFragmentMassLimit() {
		return minimumFragmentMassLimit;
	}

	public void setMinimumFragmentMassLimit(Double minimumFragmentMassLimit) {
		if(minimumFragmentMassLimit < 0.0) this.minimumFragmentMassLimit = 0.0;
		else this.minimumFragmentMassLimit = minimumFragmentMassLimit;
	}

}
