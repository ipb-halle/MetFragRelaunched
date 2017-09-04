package de.ipbhalle.metfraglib.converter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

import de.ipbhalle.metfraglib.additionals.MathTools;

public class GenFormToMetFragInput {
	
	private String nodeString = "";
	
	public GenFormToMetFragInput(String filename, String orig_spec) {
		double[][] original = this.readOrigSpec(orig_spec);
		BufferedReader breader;
		try {
			breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			breader.readLine();
			while((line = breader.readLine()) != null) {
				line = line.trim();
				String[] tmp = line.split("\\s+");
				if(tmp.length == 5) {
					double oldmz = Double.parseDouble(tmp[0]);
					double newmz = Double.parseDouble(tmp[3]);
					for(int i = 0; i < original.length; i++) {
						if(MathTools.round(oldmz, 5) == MathTools.round(original[i][0], 5)) {
							this.nodeString += newmz + " " + original[i][1] + " " + tmp[1] + " " + oldmz + "\n";
							break;
						}
					}
				
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param filename
	 * @return
	 */
	private double[][] readOrigSpec(String filename) {
		BufferedReader breader;
		double[][] values = new double[0][0];
		try {
			breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			java.util.ArrayList<Double> mzs = new java.util.ArrayList<Double>();
			java.util.ArrayList<Double> ints = new java.util.ArrayList<Double>();
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("\\s+");
				mzs.add(Double.parseDouble(tmp[0]));
				ints.add(Double.parseDouble(tmp[1]));
			}
			breader.close();
			values = new double[ints.size()][2];
			for(int i = 0; i < ints.size(); i++) {
				values[i][0] = mzs.get(i);
				values[i][1] = ints.get(i);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return values;
	}
	
	public String getNodeString() {
		return nodeString;
	}

	public static void main(String[] args) {
		GenFormToMetFragInput gftmi = new GenFormToMetFragInput(args[0], args[1]);
		System.out.println(gftmi.getNodeString());
	}
	
}
