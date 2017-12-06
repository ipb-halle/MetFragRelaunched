package de.ipbhalle.metfrag.misc;

import java.io.IOException;

public class GenerateCombinations {

	public static void main(String[] args) throws IOException {
		
		String[] a1 = {"1e-04", "5e-04", "0.001", "0.005", "0.01", "0.05"};
		String[] b1 = {"1e-04", "5e-04", "0.001", "0.005", "0.01", "0.05"};
		String[] a2 = {"1e-04", "5e-04", "0.001", "0.005", "0.01", "0.05"};
		String[] b2 = {"1e-04", "5e-04", "0.001", "0.005", "0.01", "0.05"};
		
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File("/tmp/pseudocounts_slim.txt")));
		int index = 0;
		for(int i1 = 0; i1 < a1.length; i1++) {
			for(int i2 = 0; i2 < b1.length; i2++) {
				for(int i3 = 0; i3 < a2.length; i3++) {
					for(int i4 = 0; i4 < b2.length; i4++) {
						bwriter.write(a1[i1] + " " + b1[i2] + " " + a2[i3] + " " + b2[i4] + " " + (++index));
						bwriter.newLine();
					}
				}
			}
		}
		bwriter.close();
	}
	
}

