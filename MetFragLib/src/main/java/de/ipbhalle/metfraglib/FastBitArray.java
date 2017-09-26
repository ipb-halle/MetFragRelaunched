package de.ipbhalle.metfraglib;

import javolution.util.FastBitSet;

public class FastBitArray {

	/**
	 * array to store bits
	 */
	private FastBitSet bitArray;

	private short size;
	
	/**
	 * 
	 */
	public FastBitArray() {
		this.bitArray = new FastBitSet();
		this.size = 0;
	}
	
	/**
	 * 
	 */
	public FastBitArray(FastBitArray bitArray) {
		FastBitArray copy = bitArray.clone();
		this.bitArray = copy.getArray();
		this.size = copy.getSize();
	}
	/**
	 * 
	 */
	public FastBitArray(FastBitSet bitArray, int size) {
		FastBitSet copy = new FastBitSet();
		copy.or(bitArray);
		this.bitArray = copy;
		this.size = (short)size;
	}
	
	/**
	 * initialise BitArray with a given boolean array
	 * 
	 * @param array
	 */
	public FastBitArray(boolean[] array) {
		this.bitArray = new FastBitSet();
		for(int i = 0; i < array.length; i++)
			this.bitArray.set(i, array[i]);
		this.size = (short)array.length;
	}
	
	/**
	 * initialises BitArray with specified number of bits
	 * 
	 * @param nbits
	 */
	public FastBitArray(int nbits) {
		this.bitArray = new FastBitSet();
		this.size = (short)nbits;
	}

	/**
	 * initialises BitArray with specified number of bits and value at all positions
	 * 
	 * @param nbits
	 * @param value
	 */
	public FastBitArray(int nbits, boolean value) {
		this.bitArray = new FastBitSet();
		if(value) this.bitArray.flip(0, nbits);
		this.size = (short)nbits;
	}

	/**
	 * initialises BitArray with a bitstring
	 * 
	 * @param nbits
	 * @param value
	 */
	public FastBitArray(String bitstring) {
		this.bitArray = new FastBitSet();
		for(int i = 0; i < bitstring.length(); i++) {
			this.bitArray.set(i, bitstring.charAt(i) == '0' ? false : true);
		}
		this.size = (short)bitstring.length();
	}
	
	/**
	 * get next bit index in the BitArray that is set to true after the given index n
	 * if there is no true bit after position n the function returns -1
	 * 
	 * @param index
	 * @return
	 */
	public int nextSetBit(int n) {
		return this.bitArray.nextSetBit(n - 1);
	}

	/**
	 * get next bit index in the BitArray that is set to false after the given index n
	 * if there is no false bit after position n the function returns -1
	 * 
	 * @param index
	 * @return
	 */
	public int nextClearBit(int n) {
		return this.bitArray.nextClearBit(n - 1);
	}
	
	public FastBitArray getDiff(FastBitArray subtrahend) {
		FastBitArray result = this.clone();
		for(int i = 0; i < subtrahend.getSize(); i++) {
			if(subtrahend.get(i)) {
				result.set(i, false);
			}
		}
		return result;
	}
	
	/**
	 * merge the current BitArray with the given one and return a new one
	 * the operation is an 'or' merge 
	 * 
	 * @param toMergeWith
	 * @return
	 */
	public FastBitArray merge(FastBitArray toMergeWith) {
		FastBitArray mergedArray = new FastBitArray(toMergeWith.getSize());
		for(int i = 0; i < this.getSize(); i++) {
			mergedArray.set(i, this.bitArray.get(i) || toMergeWith.get(i));
		}
		return mergedArray;
	}
	
	/**
	 * set bit at position n to true
	 * Warning: function does not check for IndexOutOfBounds
	 * 
	 * @param index
	 */
	public void set(int n) {
		this.bitArray.set(n);
	}

	/**
	 * set bit at position n to given value val
	 * Warning: function does not check for IndexOutOfBounds
	 * 
	 * @param index
	 */
	public void set(int n, boolean val) {
		this.bitArray.set(n, val);
	}
	
	/**
	 * set bit at position n to false
	 * Warning: function does not check for IndexOutOfBounds
	 * 
	 * @param index
	 */
	public void clear(int n) {
		this.bitArray.set(n, false);
	}
	
	/**
	 * returns bit at index n
	 * returns false if n < 0 and n > size of BitArray
	 * 
	 * @param n
	 * @return
	 */
	public boolean get(int n) {
		if(n < 0 || n >= this.getSize()) return false;
		return this.bitArray.get(n);
	}
	
	/**
	 * returns BitArray number of bits
	 * 
	 * @return
	 */
	public short getSize() {
		return this.size;
	}
	
	/**
	 * returns number of bits set to true
	 * 
	 * @return
	 */
	public int cardinality() {
		return this.bitArray.cardinality();
	}
	
	/**
	 * merged the current BitArray with the given one
	 * the operation is an 'and' merge 
	 * 
	 * @param ar
	 * @return
	 */
	public FastBitArray and(FastBitArray ar) {
		FastBitSet copy = new FastBitSet();
		copy.or(ar.getArray());
		copy.and(this.bitArray);
		return new FastBitArray(copy, this.size);
	}
	
	/**
	 * get integer array of indeces that are set to true
	 * 
	 * @return
	 */
	public int[] getSetIndeces() {
		int[] setIndeces = new int[this.cardinality()];
		int index = 0;
		for(int i = 0; i < this.getSize(); i++) {
			if(this.bitArray.get(i)) {
				setIndeces[index] = i;
				index++;
			}
		}
		return setIndeces;
	}
	
	/**
	 * returns true if the current and the given BitArray are of same length and have exactly the same
	 * bits set to true 
	 * 
	 * @param bitArray
	 * @return
	 */
	public boolean equals(FastBitArray bitArray) {
		if(this.getSize() != bitArray.getSize()) return false;
		for(int i = 0; i < this.getSize(); i++) 
			if(this.bitArray.get(i) != bitArray.get(i)) return false;
		return true;
	}
	
	public boolean equals(Object object) {
		FastBitArray bitArray = (FastBitArray)object;
		if(this.getSize() != bitArray.getSize()) return false;
		for(int i = 0; i < this.getSize(); i++) 
			if(this.bitArray.get(i) != bitArray.get(i)) return false;
		return true;
	}
	
	public int hashCode() {
		java.util.BitSet bitset = new java.util.BitSet((int)this.size);
		for(int i = 0; i < this.getSize(); i++) 
			if(this.bitArray.get(i)) bitset.set(i);
		return bitset.hashCode();
	}
	
	/**
	 * returns true if the current and the given BitArray are of same length and have exactly the same
	 * bits set to true 
	 * 
	 * @param bitArray
	 * @return
	 */
	public boolean equals(String bitString) {
		if(this.getSize() != bitString.length()) return false;
		for(int i = 0; i < this.getSize(); i++) 
			if((this.bitArray.get(i) && bitString.charAt(i) == '0') || (!this.bitArray.get(i) && bitString.charAt(i) == '1')) return false;
		return true;
	}
	
	/**
	 * sets all bits of the BitArray to val
	 * 
	 * @param val
	 */
	public void setAll(boolean val) {
		this.bitArray.set(0, this.getSize(), val);
	}
	
	/**
	 * 
	 * @param toCheck
	 * @return
	 */
	public boolean isRealSubset(FastBitArray toCheck) {
		if(toCheck.cardinality() >= this.cardinality()) return false;
		for(int i = 0; i < this.getSize(); i++) {
			if(!this.bitArray.get(i) && toCheck.get(i)) return false;
		}
		return true;
	}

	/**
	 * checks whether the given is a subset of the current
	 * checks via 'or'
	 * 
	 * @param toCheck
	 * @return
	 */
	public boolean isSubset(FastBitArray toCheck) {
		if(toCheck.getSize() > this.getSize()) return false;
		for(int i = 0; i < this.getSize(); i++) {
			if(!this.bitArray.get(i) && toCheck.get(i)) return false;
		}
		return true;
	}
	
	/**
	 * returns string with with true positions '1' and false positions '0' 
	 */
	public String toString() {
		char[] set = new char[this.getSize()];
		for(int i = 0; i < this.getSize(); i++) {
			set[i] = this.bitArray.get(i) ? '1' : '0';
		}
		return String.valueOf(set);
	}
	
	/**
	 * 
	 * @return
	 */
	public String toStringIDs() {
		String val = "";
		boolean added = false;
		for(int i = 0; i < this.getSize(); i++) {
			if(this.bitArray.get(i)) {
				if(added) val += "-";
				val += (i + 1);
				added = true;
			}
		}
		return val;
	}
	
	/**
	 * returns first position of BitArray set to true
	 * returns -1 if there is no bit set to true
	 * 
	 * @return
	 */
	public int getFirstSetBit() {
		for(int i = 0; i < this.getSize(); i++)
			if(this.bitArray.get(i)) return i;
		return -1;
	}
	
	/**
	 * returns last position of BitArray set to true
	 * returns -1 if there is no bit set to true
	 * 
	 */
	public int getLastSetBit() {
		for(int i = this.getSize() - 1; i >= 0; i--)
			if(this.bitArray.get(i)) return i;
		return -1;
	}
	/**
	 * sets boolean array to null
	 */
	public void nullify() {
		this.bitArray = null;
	}
	
	/**
	 * sets indeces of BitArray in the given integer array to true 
	 * 
	 * @param bitIndexes
	 */
	public void setBits(int[] bitIndexes) {
		for(int i = 0; i < bitIndexes.length; i++) {
			if(bitIndexes[i] < this.getSize() && bitIndexes[i] >= 0)
				this.bitArray.set(bitIndexes[i]);
			else
				System.err.println("Warning: Could not set bit at position " + bitIndexes[i] + " to true. Out of range!");
		}
	}
	
	public FastBitSet getArray() {
		return this.bitArray;
	}
	
	public int compareTo(FastBitArray array) {
		int len1 = this.getSize();
		int len2 = array.getSize();
		int minLen = Math.min(len1, len2);
		for(int i = 0; i < minLen; i++) {
			//check if obj smaller
			if(!this.get(i) && array.get(i)) return -1;
			if(this.get(i) && !array.get(i)) return 1;
		}
		if(len1 < len2) return -1;
		if(len1 > len2) return 1;
		return 0;
	}
	
	/**
	 * 
	 */
	public FastBitArray clone() {
		FastBitArray clone = new FastBitArray(this.getSize());
		for(int i = 0; i < this.getSize(); i++)
			if(this.bitArray.get(i)) clone.set(i);
		return clone;
	}
}
