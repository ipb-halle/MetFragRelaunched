package de.ipbhalle.metfrag.misc;

import java.io.IOException;

public class GenerateCombinations {

	public static void main(String[] args) throws IOException {
		
		String[] a1 = {"0.5","0.25","0.125","0.0625","0.0125","0.0025","5e-04","1e-04","2e-05","4e-06","8e-07","1.6e-07","3.2e-08"};
		String[] b1 = {"0.5","0.25","0.125","0.0625","0.0125","0.0025","5e-04","1e-04","2e-05","4e-06","8e-07","1.6e-07","3.2e-08"};
		
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File("/tmp/pseudocounts_slim.txt")));
		for(int i1 = 0; i1 < a1.length; i1++) {
			for(int i2 = 0; i2 < b1.length; i2++) {
				bwriter.write(a1[i1] + " " + b1[i2]);
				bwriter.newLine();
			}
		}
		bwriter.close();
	}
	/*
	public static void main(String[] args) throws IOException {
		
		String[] a1 = {"1e-04", "5e-04", "0.0025"};
		String[] b1 = {"1e-04", "5e-04", "0.0025"};
		String[] a2 = {"1e-04", "5e-04", "0.0025"};
		String[] b2 = {"1e-04", "5e-04", "0.0025"};
		
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File("/tmp/pseudocounts_slim.txt")));
		for(int i1 = 0; i1 < a1.length; i1++) {
			for(int i2 = 0; i2 < b1.length; i2++) {
				for(int i3 = 0; i3 < a2.length; i3++) {
					for(int i4 = 0; i4 < b2.length; i4++) {
						bwriter.write(a1[i1] + " " + b1[i2] + " " + a2[i3] + " " + b2[i4]);
						bwriter.newLine();
					}
				}
			}
		}
		bwriter.close();
	}
	*/
	
}

