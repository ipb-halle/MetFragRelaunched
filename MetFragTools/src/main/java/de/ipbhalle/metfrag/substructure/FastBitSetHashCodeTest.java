package de.ipbhalle.metfrag.substructure;

import java.util.HashMap;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.substructure.FingerprintToMassesHashMap;

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
		
		FingerprintToMassesHashMap map = new FingerprintToMassesHashMap();
		FastBitArray a = new FastBitArray("000000000000000000000000000000000000000000000000000000000000000000000000000001010001000000001000000000000000000000000010000000000010101000000101000011100001010111101000000000000000000000000000");
		FastBitArray b = new FastBitArray("000000000000000000000000000000000000000000000000000000000000000000000000000001010001000000001000000000000000000000000010000000000010101000000101000011100001010111101000000000000000000000000000");
		
		map.addMass(a, new Double(160.087069230769), 1.0);
		System.out.println(map.contains(a, 160.08706923076));
		
		HashMap<FastBitArray, Integer> test = new HashMap<FastBitArray, Integer>();
		test.put(a, 1);
		
		System.out.println(test.containsKey(b));
		System.out.println(test.containsKey(a));
		
		System.out.println(a.equals(b));
		System.out.println(b.equals(a));
	}
	
	
}
