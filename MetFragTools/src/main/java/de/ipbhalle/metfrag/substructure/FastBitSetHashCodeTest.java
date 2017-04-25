package de.ipbhalle.metfrag.substructure;

import de.ipbhalle.metfraglib.FastBitArray;

public class FastBitSetHashCodeTest {

	public static void main(String[] args) {
		FastBitArray x = new FastBitArray(166);
		FastBitArray x2 = new FastBitArray(166);
		
		
		
		int[] values = {34,45,82,99,100,151,153,155,158,161};
		for(int i = 0; i < values.length; i++) {
			x.set(values[i]);
		}

		int[] values2 = {99,100,151,153,155,158,161};
		for(int i = 0; i < values2.length; i++) {
			x2.set(values2[i]);
		}
		
		System.out.println(x.hashCode());
		System.out.println(x2.hashCode());
		
	}
	
	
}
