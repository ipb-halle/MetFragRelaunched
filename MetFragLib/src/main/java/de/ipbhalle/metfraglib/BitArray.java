package de.ipbhalle.metfraglib;

public class BitArray {

	/**
	 * array to store bits
	 */
	private boolean[] bitArray;
	
	/**
	 * 
	 */
	public BitArray() {
		this.bitArray = new boolean[0];
	}
	
	/**
	 * initialise BitArray with a given boolean array
	 * 
	 * @param array
	 */
	public BitArray(boolean[] array) {
		this.bitArray = new boolean[array.length];
		for(int i = 0; i < this.bitArray.length; i++)
			this.bitArray[i] = array[i];
	}
	
	/**
	 * initialises BitArray with specified number of bits
	 * 
	 * @param nbits
	 */
	public BitArray(int nbits) {
		this.bitArray = new boolean[nbits];
	}

	/**
	 * initialises BitArray with specified number of bits and value at all positions
	 * 
	 * @param nbits
	 * @param value
	 */
	public BitArray(int nbits, boolean value) {
		this.bitArray = new boolean[nbits];
		for(int i = 0; i < this.bitArray.length; i++)
			this.bitArray[i] = value;
	}
	
	/**
	 * get next bit index in the BitArray that is set to true after the given index n
	 * if there is no true bit after position n the function returns -1
	 * 
	 * @param index
	 * @return
	 */
	public int nextSetBit(int n) {
		for(int i = n + 1; i < this.bitArray.length; i++)
			if(this.bitArray[i]) return i;
		return -1;
	}

	/**
	 * get next bit index in the BitArray that is set to false after the given index n
	 * if there is no false bit after position n the function returns -1
	 * 
	 * @param index
	 * @return
	 */
	public int nextClearBit(int n) {
		for(int i = n + 1; i < this.bitArray.length; i++)
			if(!this.bitArray[i]) return i;
		return -1;
	}
	
	public BitArray getDiff(BitArray subtrahend) {
		BitArray result = this.clone();
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
	public BitArray merge(BitArray toMergeWith) {
		BitArray mergedArray = new BitArray(toMergeWith.getSize());
		for(int i = 0; i < this.bitArray.length; i++) {
			mergedArray.set(i, this.bitArray[i] || toMergeWith.get(i));
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
		this.bitArray[n] = true;
	}

	/**
	 * set bit at position n to given value val
	 * Warning: function does not check for IndexOutOfBounds
	 * 
	 * @param index
	 */
	public void set(int n, boolean val) {
		this.bitArray[n] = val;
	}
	
	/**
	 * set bit at position n to false
	 * Warning: function does not check for IndexOutOfBounds
	 * 
	 * @param index
	 */
	public void clear(int n) {
		this.bitArray[n] = false;
	}
	
	/**
	 * returns bit at index n
	 * returns false if n < 0 and n > size of BitArray
	 * 
	 * @param n
	 * @return
	 */
	public boolean get(int n) {
		if(n < 0 || n >= this.bitArray.length) return false;
		return this.bitArray[n];
	}
	
	/**
	 * returns BitArray number of bits
	 * 
	 * @return
	 */
	public int getSize() {
		return this.bitArray.length;
	}
	
	/**
	 * returns number of bits set to true
	 * 
	 * @return
	 */
	public int cardinality() {
		int num = 0;
		for(int i = 0; i < this.bitArray.length; i++) 
			if(this.bitArray[i]) num++;
		return num;
	}
	
	/**
	 * merged the current BitArray with the given one
	 * the operation is an 'and' merge 
	 * 
	 * @param ar
	 * @return
	 */
	public BitArray and(BitArray ar) {
		if(this.bitArray.length != ar.getSize()) throw new ArrayIndexOutOfBoundsException("Error: BitArrays not of the same length");
		BitArray newAr = new BitArray(this.bitArray.length);
		for(int i = 0; i < newAr.getSize(); i++) {
			newAr.set(i, ar.get(i) && this.bitArray[i]);
		}
		return newAr;
	}
	
	/**
	 * get integer array of indeces that are set to true
	 * 
	 * @return
	 */
	public int[] getSetIndeces() {
		int[] setIndeces = new int[this.cardinality()];
		int index = 0;
		for(int i = 0; i < this.bitArray.length; i++) {
			if(this.bitArray[i]) {
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
	public boolean equals(BitArray bitArray) {
		if(this.bitArray.length != bitArray.getSize()) return false;
		for(int i = 0; i < this.bitArray.length; i++) 
			if(this.bitArray[i] != bitArray.get(i)) return false;
		return true;
	}
	
	/**
	 * sets all bits of the BitArray to val
	 * 
	 * @param val
	 */
	public void setAll(boolean val) {
		for(int i = 0; i < this.bitArray.length; i++)
			this.bitArray[i] = val;
	}
	
	/**
	 * 
	 * @param toCheck
	 * @return
	 */
	public boolean isRealSubset(BitArray toCheck) {
		if(toCheck.cardinality() >= this.cardinality()) return false;
		for(int i = 0; i < this.bitArray.length; i++) {
			if(!this.bitArray[i] && toCheck.get(i)) return false;
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
	public boolean isSubset(BitArray toCheck) {
		if(toCheck.getSize() > this.getSize()) return false;
		for(int i = 0; i < this.bitArray.length; i++) {
			if(!this.bitArray[i] && toCheck.get(i)) return false;
		}
		return true;
	}
	
	/**
	 * returns string with with true positions '1' and false positions '0' 
	 */
	public String toString() {
		String val = "";
		for(int i = 0; i < this.bitArray.length; i++) {
			if(this.bitArray[i]) val += "1";
			else val += "0";
		}
		return val;
	}
	
	/**
	 * 
	 * @return
	 */
	public String toStringIDs() {
		String val = "";
		boolean added = false;
		for(int i = 0; i < this.bitArray.length; i++) {
			if(this.bitArray[i]) {
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
		for(int i = 0; i < this.bitArray.length; i++)
			if(this.bitArray[i]) return i;
		return -1;
	}
	
	/**
	 * returns last position of BitArray set to true
	 * returns -1 if there is no bit set to true
	 * 
	 */
	public int getLastSetBit() {
		for(int i = this.bitArray.length - 1; i >= 0; i--)
			if(this.bitArray[i]) return i;
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
			if(bitIndexes[i] < this.bitArray.length && bitIndexes[i] >= 0)
				this.bitArray[bitIndexes[i]] = true;
			else
				System.err.println("Warning: Could not set bit at position " + bitIndexes[i] + " to true. Out of range!");
		}
	}
	
	public boolean[] getArray() {
		return this.bitArray;
	}
	
	/**
	 * 
	 */
	public BitArray clone() {
		BitArray clone = new BitArray(this.bitArray.length);
		for(int i = 0; i < this.bitArray.length; i++)
			if(this.bitArray[i]) clone.set(i);
		return clone;
	}
}
