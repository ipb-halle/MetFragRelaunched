package de.ipbhalle.metfraglib.additionals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.ipbhalle.metfraglib.parameter.Constants;

public class BondEnergies {

	private Map<Bond, Double> bondEnergies;
	
	public BondEnergies() {
		this.bondEnergies = new HashMap<Bond, Double>();                                                                                                                                                                                                            
		this.bondEnergies.put(new Bond("H","H", '-'), 431.0);                                                                                                                                                                                                               
		this.bondEnergies.put(new Bond("H","C",'-'), 412.0);
		this.bondEnergies.put(new Bond("H","Si",'-'), 318.0);
		this.bondEnergies.put(new Bond("H","N",'-'), 388.0);
		this.bondEnergies.put(new Bond("H","P",'-'), 322.0);
		this.bondEnergies.put(new Bond("H","O",'-'), 463.0);
		this.bondEnergies.put(new Bond("H","S",'-'), 338.0);
		this.bondEnergies.put(new Bond("H","F",'-'), 562.0);
		this.bondEnergies.put(new Bond("H","Cl",'-'), 431.0);
		this.bondEnergies.put(new Bond("H","Br",'-'), 366.0);
		this.bondEnergies.put(new Bond("H","I",'-'), 299.0);
		this.bondEnergies.put(new Bond("H","B",'-'), 389.0);
		this.bondEnergies.put(new Bond("H","Ge",'-'), 288.0);
		this.bondEnergies.put(new Bond("H","Sn",'-'), 251.0);
		this.bondEnergies.put(new Bond("H","As",'-'), 247.0);
		this.bondEnergies.put(new Bond("H","Se",'-'), 276.0);
		this.bondEnergies.put(new Bond("H","T",'-'), 238.0);
		this.bondEnergies.put(new Bond("C","C",'-'), 348.0);
		this.bondEnergies.put(new Bond("C","C",'='), 612.0);
		this.bondEnergies.put(new Bond("C","C",'~'), 837.0);
		this.bondEnergies.put(new Bond("C","O",'-'), 360.0);
		this.bondEnergies.put(new Bond("C","O",'='), 743.0);
		this.bondEnergies.put(new Bond("C","N",'-'), 305.0);
		this.bondEnergies.put(new Bond("C","N",'='), 613.0);
		this.bondEnergies.put(new Bond("C","N",'~'),890.0);
		this.bondEnergies.put(new Bond("C","F",'-'),484.0);
		this.bondEnergies.put(new Bond("C","Cl",'-'),338.0);
		this.bondEnergies.put(new Bond("C","Br",'-'),276.0);
		this.bondEnergies.put(new Bond("C","I",'-'), 238.0);
		this.bondEnergies.put(new Bond("C","S",'-'), 272.0);
		this.bondEnergies.put(new Bond("C","S",'='), 573.0);
		this.bondEnergies.put(new Bond("C","Si",'-'), 318.0);
		this.bondEnergies.put(new Bond("C","Ge",'-'), 238.0);
		this.bondEnergies.put(new Bond("C","Sn",'-'), 192.0);
		this.bondEnergies.put(new Bond("C","Pb",'-'), 130.0);
		this.bondEnergies.put(new Bond("C","P",'-'), 264.0);
		this.bondEnergies.put(new Bond("C","B",'-'), 356.0);
		this.bondEnergies.put(new Bond("P","P",'-'), 201.0);
		this.bondEnergies.put(new Bond("P","O",'-'), 335.0);
		this.bondEnergies.put(new Bond("P","O",'='), 544.0);
		this.bondEnergies.put(new Bond("P","S",'='), 335.0);
		this.bondEnergies.put(new Bond("P","F",'-'), 490.0);
		this.bondEnergies.put(new Bond("P","Cl",'-'), 326.0);
		this.bondEnergies.put(new Bond("P","Br",'-'), 264.0);
		this.bondEnergies.put(new Bond("P","I",'-'), 184.0);
		this.bondEnergies.put(new Bond("F","Cl",'-'), 313.0);
		this.bondEnergies.put(new Bond("Si","Si",'-'), 176.0);
		this.bondEnergies.put(new Bond("N","N",'-'), 163.0);
		this.bondEnergies.put(new Bond("N","N",'='), 409.0);
		this.bondEnergies.put(new Bond("N","N",'~'), 944.0);
		this.bondEnergies.put(new Bond("O","O",'-'), 146.0);
		this.bondEnergies.put(new Bond("O","O",'='), 496.0);
		this.bondEnergies.put(new Bond("F","F",'-'), 158.0);
		this.bondEnergies.put(new Bond("Cl","Cl",'-'), 242.0);
		this.bondEnergies.put(new Bond("Br","Br",'-'), 193.0);
		this.bondEnergies.put(new Bond("I","I",'-'), 151.0);
		this.bondEnergies.put(new Bond("At","At",'-'), 116.0);
		this.bondEnergies.put(new Bond("Se","Se",'-'), 172.0);
		this.bondEnergies.put(new Bond("I","O",'-'), 201.0);
		this.bondEnergies.put(new Bond("I","F",'-'), 273.0);
		this.bondEnergies.put(new Bond("I","Cl",'-'), 208.0);
		this.bondEnergies.put(new Bond("I","Br",'-'), 175.0);
		this.bondEnergies.put(new Bond("B","B",'-'), 293.0);
		this.bondEnergies.put(new Bond("B","O",'-'), 536.0);
		this.bondEnergies.put(new Bond("B","F",'-'), 613.0);
		this.bondEnergies.put(new Bond("B","Cl",'-'), 456.0);
		this.bondEnergies.put(new Bond("B","Br",'-'), 377.0);
		this.bondEnergies.put(new Bond("S","Cl",'-'), 255.0);
		this.bondEnergies.put(new Bond("S","S",'='), 425.0);
		this.bondEnergies.put(new Bond("S","O",'='), 522.0);
		this.bondEnergies.put(new Bond("N","O",'='), 607.0);
		this.bondEnergies.put(new Bond("N","O",'-'), 222.0);
		this.bondEnergies.put(new Bond("S","S",'-'), 226.0);
		this.bondEnergies.put(new Bond("F","N",'-'), 272.0);
		this.bondEnergies.put(new Bond("F","O",'-'), 184.0);
		this.bondEnergies.put(new Bond("F","S",'-'), 226.0);
	}
	
	public BondEnergies(String filename) {
		this.readBondEnergies(filename);
	}
	
	protected void readBondEnergies(String filename) {
		this.bondEnergies = new HashMap<Bond, Double>();
		try {
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("\\s+");
				try {
					Bond currentBond = null;
					try {
						currentBond = new Bond(tmp[0].trim(), tmp[1].trim(), tmp[2].trim().charAt(0));
					}
					catch(Exception e) {
						//support second format
						String atom1 = tmp[0].trim().replaceFirst("(^[A-Za-z]*)[^A-Za-z].*", "$1");
						String atom2 = tmp[0].trim().replaceFirst("^.*[^A-Za-z]([A-Za-z]*)$", "$1");
						char bondType = tmp[0].trim().replaceFirst("^[A-Za-z]*(.)[A-Za-z]*$", "$1").charAt(0);
						currentBond = new Bond(atom1, atom2, bondType);
					}
					if(this.bondEnergies.containsKey(currentBond))
						throw new Exception("Error: Bond " + line + " with hash code already exists "
								+ " at energy " + this.bondEnergies.get(currentBond) );
					this.bondEnergies.put(currentBond, Double.parseDouble(tmp[1].trim()));
				}
				catch(Exception e) {
					e.printStackTrace();
					System.err.println(new BondEnergies().getClass().getName() + ": Error: Could not parse line correctly in " + filename);
				}
			}
			breader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Double get(Bond bond) {
		if(this.bondEnergies.containsKey(bond)) {
			return this.bondEnergies.get(bond);
		}
		return Constants.DEFAULT_BOND_ENERGY;
	}
	
	public static void main(String[] args) {
		BondEnergies be = new BondEnergies("/home/cruttkie/metfrag/michaelwitting/21_05_14/processing/10th/bondEnergiesLipids.txt");
		//BondEnergies be = new BondEnergies();
		System.out.println("####");
		System.out.println(be.get(new Bond("H", "H", '-')));
		System.out.println(be.get(new Bond("Cl", "C", '-')));
	}
}
