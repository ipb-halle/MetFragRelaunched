package de.ipbhalle.metfraglib.substructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.ipbhalle.metfraglib.list.DefaultPeakList;

public class AnnotationFileReader {

	private String filename;
	private DefaultPeakList peaklist;
	private PeakToSmartGroupListCollection peakToSmartGroupListCollection;
	
	public AnnotationFileReader(String filename, DefaultPeakList peaklist) {
		this.filename = filename;
		this.peaklist = peaklist;
	}

	public void initialise() throws IOException {
		this.peakToSmartGroupListCollection = new PeakToSmartGroupListCollection();
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		while((line = breader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0) continue;
			if(line.startsWith("#")) continue;
			String[] tmp = line.split("\\s+");
			Double peak = Double.parseDouble(tmp[0]);
			PeakToSmartGroupList peakToSmartGroupList = new PeakToSmartGroupList(peak);
			SmartsGroup smartsGroup = null;
			for(int i = 1; i < tmp.length; i++) {
				if(this.isDoubleValue(tmp[i])) {
					if(smartsGroup != null) this.peakToSmartGroupListCollection.addElement(smartsGroup);
					smartsGroup = new SmartsGroup(Double.parseDouble(tmp[0]));
				}
				else {
					smartsGroup.addElement(tmp[i]);
				}
			}
		}
		breader.close();
	}
	
	private boolean isDoubleValue(String value) {
		try {
			Double.parseDouble(value);
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}
}
