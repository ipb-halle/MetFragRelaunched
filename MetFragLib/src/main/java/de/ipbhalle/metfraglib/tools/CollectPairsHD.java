package de.ipbhalle.metfraglib.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.molecularformula.HDByteMolecularFormula;

public class CollectPairsHD {

	public static void main(String[] args) throws IOException {
		
		if(args == null || args.length != 3) {
			System.err.println("Give me: orig-folder deuterium-folder output-pair-file");
			System.exit(1);
		}
		
		File origfolder = new File(args[0]);
		File deuteriumfolder = new File(args[1]);
		
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File directory, String fileName) {
	            return fileName.endsWith(".txt");
	        }
	    };
		File[] hydrogenspectrafiles = origfolder.listFiles(filter);
		File[] deuteriumspectrafiles = deuteriumfolder.listFiles(filter);
		
		String[] hydrogenspectranames = new String[hydrogenspectrafiles.length];
		String[] deuteriumspectranames = new String[deuteriumspectrafiles.length];
		String[] undeuteratedFormula = new String[deuteriumspectrafiles.length];

		String[] newDeuteriumNames = new String[deuteriumspectrafiles.length];
		
		
		int[] deuteriums = new int[deuteriumspectrafiles.length];
		
		for(int i = 0; i < hydrogenspectranames.length; i++) {
			hydrogenspectranames[i] = hydrogenspectrafiles[i].getName().replaceAll(".*_", "").replaceAll("\\.txt", "");
		}

		for(int i = 0; i < deuteriumspectranames.length; i++) {
			deuteriumspectranames[i] = deuteriumspectrafiles[i].getName().replaceAll("\\.txt", "");
			int numberAdditionalDeuterium = 0;
			String[] tmp = deuteriumspectranames[i].split("_");
			String formula = tmp[tmp.length - 1];
			if(deuteriumspectranames[i].matches(".*_[0-9]*D$")) {
				formula = tmp[tmp.length - 2];
				numberAdditionalDeuterium = Integer.parseInt(tmp[tmp.length - 1].replace("D", ""));
			}	
			HDByteMolecularFormula labelledFormula = null;
			try {
				labelledFormula = new HDByteMolecularFormula(formula);
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
			labelledFormula.setNumberDeuterium((short)(labelledFormula.getNumberDeuterium() + (short)numberAdditionalDeuterium));
			deuteriums[i] = labelledFormula.getNumberDeuterium();
			String name = "";
			for(int k = 0; k < 5; k++) {
				name += tmp[k] + "_";
			}
			name += labelledFormula.toString();
			labelledFormula.setNumberDeuterium((short)0);
			String normalFormula = labelledFormula.toString();
			
			undeuteratedFormula[i] = normalFormula;
			newDeuteriumNames[i] = name;
			deuteriumspectrafiles[i].renameTo(new File(deuteriumfolder + "/" + name + ".txt"));
			
		}
		
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(args[2])));
		for(int i = 0; i < hydrogenspectranames.length; i++) {
			ByteMolecularFormula formula = null;
			try {
				formula = new ByteMolecularFormula(hydrogenspectranames[i].replaceAll(".*_", "").replace(".txt", ""));
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
			for(int j = 0; j < deuteriumspectrafiles.length; j++) {
				ByteMolecularFormula deuformula = null;
				try {
					deuformula = new ByteMolecularFormula(undeuteratedFormula[j]);
				} catch (AtomTypeNotKnownFromInputListException e) {
					e.printStackTrace();
				}
				if(deuformula.compareTo(formula)) {
					bwriter.write(""
							+ hydrogenspectrafiles[i].getAbsolutePath() + " " 
							+ deuteriumspectrafiles[j].getParent() + "/" + newDeuteriumNames[j] + ".txt "
							+ deuteriums[j] + " 5000 2 1");
					bwriter.newLine();		
					break;
				}
			}
		}
		bwriter.close();
		
		
	}
	
	
}
