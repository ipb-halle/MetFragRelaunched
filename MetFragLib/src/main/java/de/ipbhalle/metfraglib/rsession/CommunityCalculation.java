package de.ipbhalle.metfraglib.rsession;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;

public class CommunityCalculation {

	public BitArrayPrecursor precursor;
	public int numberCommunities;
	public int[] memberships;
	public DefaultBitArrayFragment[] communityFragments;
	
	static {
		Rsession.loadLibrary("igraph");
	}
	
	public CommunityCalculation(BitArrayPrecursor precursor) {
		this.precursor = precursor;
		this.calculateCommunities();
	}
	
	private void calculateCommunities() {
		String nodeString = "";
		for (int i = 0; i < this.precursor.getNonHydrogenAtomCount(); i++) {
			short[] connectedAtomIndexes = this.precursor.getConnectedAtomIndecesOfAtomIndex((short)i);
			for(int k = 0; k < connectedAtomIndexes.length; k++) { 
				if(i < connectedAtomIndexes[k]) nodeString += "," + (i + 1) + "," + (connectedAtomIndexes[k] + 1);
			}
		}
		nodeString = nodeString.substring(1);
		
		String command1 = "g <- graph(c(" + nodeString + "),directed=F)";
		String command2 = "comms <- fastgreedy.community(g)";
		String command3 = "membership(comms)";
		
		double[] membershipDouble = null;
		
		if(Rsession.isSessionLoaded()) membershipDouble = Rsession.giveMemberships(command1, command2, command3);
		
		this.memberships = new int[membershipDouble.length];
		this.numberCommunities = 0;
		for (int i = 0; i < membershipDouble.length; i++) {
			this.memberships[i] = (int) membershipDouble[i] - 1;
			if (this.memberships[i] + 1 > this.numberCommunities)
				this.numberCommunities = this.memberships[i] + 1;
		}

		BitArray[] atomBitArrays = new BitArray[this.numberCommunities];
		for(int i = 0; i < this.numberCommunities; i++) 
			atomBitArrays[i] = new BitArray(this.memberships.length, false);
		
		for(int i = 0; i < this.memberships.length; i++) {
			atomBitArrays[this.memberships[i]].set(i);
		}
		
		this.communityFragments = new DefaultBitArrayFragment[this.numberCommunities];
		for(int i = 0; i < this.communityFragments.length; i++) {
			this.communityFragments[i] = new DefaultBitArrayFragment(this.precursor, atomBitArrays[i]);
		}
	}
	
	public DefaultBitArrayFragment[] getCommunityFragments() {
		return this.communityFragments;
	}
	
	public int getNumberCommunities() {
		return numberCommunities;
	}
	
	public int[] getMemberships() {
		return memberships;
	}

}
