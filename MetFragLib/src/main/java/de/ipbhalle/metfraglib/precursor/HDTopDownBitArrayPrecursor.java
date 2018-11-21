package de.ipbhalle.metfraglib.precursor;

import java.util.Arrays;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.molecularformula.HDByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;

public class HDTopDownBitArrayPrecursor extends TopDownBitArrayPrecursor {

	private short[][] numberDeuteriums;
	private byte numberOverallDeuteriums;
	private byte numberExchangedHydrogens;
	private byte numberExchangeableHydrogens;

	public HDTopDownBitArrayPrecursor(IAtomContainer precursorMolecule, byte numberOverallDeuteriums) throws AtomTypeNotKnownFromInputListException {
		super(precursorMolecule);
		this.initialiseNumberHydrogens();
		this.numberOverallDeuteriums = numberOverallDeuteriums;
	}
	
	@Override
	public void preprocessPrecursor() throws AtomTypeNotKnownFromInputListException, Exception {
		super.preprocessPrecursor();
		this.initialise();
	}
	
	protected void initialiseMolecularFormula() throws AtomTypeNotKnownFromInputListException {
		this.molecularFormula = new HDByteMolecularFormula(this);
	}
	
	protected void initialise() {
		int[] posToExchange = this.searchForDeuteriumExchangeablePositions(Constants.EXCHANGEABLE_DEUTERIUM_POSITIONS);
		this.numberExchangeableHydrogens = (byte)posToExchange.length;
		FastBitArray atomsWithDeuterium = new FastBitArray(this.getNonHydrogenAtomCount());
		//just one molecule needs to be generated
		if(this.numberOverallDeuteriums == 0 || posToExchange.length <= this.numberOverallDeuteriums) {	
			this.numberDeuteriums = new short[1][this.getNonHydrogenAtomCount()];
			byte numberEasilyExchanged = 0;
			for (int i = 0; i < posToExchange.length; i++) {
				if(!atomsWithDeuterium.get(posToExchange[i])) {
					atomsWithDeuterium.set(posToExchange[i]);
					this.numberDeuteriums[0][posToExchange[i]] += this.getNumberHydrogensConnectedToAtomIndex(posToExchange[i]);
					numberEasilyExchanged += this.getNumberHydrogensConnectedToAtomIndex(posToExchange[i]);
				}
			}
			this.numberExchangedHydrogens = numberEasilyExchanged;
		}
		//several combinations need to be generated
		else if(posToExchange.length > this.numberOverallDeuteriums) {
			//get all possible combinations of exchanges with given number 
			//of exchangeable hydrogens
			int[][] combs = this.getExchangeCombinations(posToExchange, this.numberOverallDeuteriums);
			this.numberDeuteriums = new short[combs.length][this.getNonHydrogenAtomCount()];
			this.numberExchangedHydrogens = this.numberOverallDeuteriums;
			for (int k = 0; k < combs.length; k++) {
				for(int l = 0; l < combs[k].length; l++) 
					this.numberDeuteriums[k][combs[k][l]] += 1;
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberDeuteratedCombinations() {
		return this.numberDeuteriums.length;
	}
	
	/**
	 * get number of connected deuteriums of atom with atomIndex
	 * 
	 * @param atomIndex
	 * @return
	 */
	public int getNumberDeuteriumsConnectedToAtomIndex(int moleculeIndex, int atomIndex) {
		return this.numberDeuteriums[moleculeIndex][atomIndex];
	}
	
	public byte getNumberExchangeableHydrogens() {
		return numberExchangeableHydrogens;
	}

	public void setNumberExchangeableHydrogens(byte numberExchangeableHydrogens) {
		this.numberExchangeableHydrogens = numberExchangeableHydrogens;
	}

	public int getNumberVariableDeuteriums() {
		return this.numberOverallDeuteriums - this.numberExchangedHydrogens;
	}
	
	public byte getNumberOverallDeuteriums() {
		return numberOverallDeuteriums;
	}

	public void setNumberOverallDeuteriums(byte numberOverallDeuteriums) {
		this.numberOverallDeuteriums = numberOverallDeuteriums;
	}
	
	public int[] searchForDeuteriumExchangeablePositions(String[] elementsToExchange) {
		java.util.ArrayList<Integer> positionsToExchange = new java.util.ArrayList<Integer>();
		for (int i = 0; i < this.getNonHydrogenAtomCount(); i++) {
			String symbol = this.getAtomSymbol(i);
			if (symbol.equals("H"))
				continue;
			for (int k = 0; k < elementsToExchange.length; k++) {
				if (symbol.equals(elementsToExchange[k]) && this.getNumberHydrogensConnectedToAtomIndex(i) > 0) {
					for(int l = 0; l < this.getNumberHydrogensConnectedToAtomIndex(i); l++) {
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
	
	public int[][] getExchangeCombinations(int[] toExchange, int numToDraw) {
		java.util.ArrayList<String> results = new java.util.ArrayList<String>();
		String[] toDrawFrom = new String[toExchange.length];
		for(int i = 0; i < toDrawFrom.length; i++) toDrawFrom[i] = String.valueOf(toExchange[i]);
		
		this.combinations(toDrawFrom, numToDraw, 0, new String[numToDraw], results);
		
		int[][] combinations = new int[results.size()][numToDraw]; 
		for(int i = 0; i < results.size(); i++) {
			String string = results.get(i).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s+", "");
			String[] tmp = string.split(",");
			for(int j = 0; j < tmp.length; j++) combinations[i][j] = Integer.parseInt(tmp[j]);
		}
		
		return combinations;
	}
	
	public void combinations(String[] arr, int len, int startPosition, String[] result, java.util.ArrayList<String> finalResults){
        if (len == 0){
        	String stringResult = Arrays.toString(result);
         	if(!finalResults.contains(stringResult)) finalResults.add(stringResult);
        	return;
        }       
        for (int i = startPosition; i <= arr.length-len; i++){
            result[result.length - len] = arr[i];
            combinations(arr, len-1, i+1, result, finalResults);
        }
    }

	public void printPositions() {
		int[] posToExchange = this.searchForDeuteriumExchangeablePositions(Constants.EXCHANGEABLE_DEUTERIUM_POSITIONS);
		System.out.print("to exchange: ");
		for(int i = 0; i < posToExchange.length; i++)
			System.out.print(posToExchange[i] + " ");
		System.out.println();
		for(int i = 0; i < this.numberDeuteriums.length; i++) {
			for(int k = 0; k < this.numberDeuteriums[i].length; k++) {
				System.out.print(this.numberDeuteriums[i][k] + " ");
			}
			System.out.println();
		}
		
	}
	
	public String getNumberDeuteriumsAsString(int index) {
		String string = "";
		if(this.numberDeuteriums[index].length >= 1) string += this.numberDeuteriums[index][0];
		for(int i = 1; i < this.numberDeuteriums[index].length; i++) {
			string += "," + this.numberDeuteriums[index][i];
		}
		return string;
	}
	
	public byte getNumberExchangedHydrogens() {
		return this.numberExchangedHydrogens;
	}    
	
}
