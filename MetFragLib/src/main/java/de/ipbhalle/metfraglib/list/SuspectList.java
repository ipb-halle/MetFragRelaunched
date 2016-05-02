package de.ipbhalle.metfraglib.list;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.ipbhalle.metfraglib.functions.ForIdentRestWebService;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class SuspectList extends DefaultList {

	private String name;
	
	public SuspectList(String filename) {
		if(filename.equals(VariableNames.FORIDENT_SUSPECTLIST_NAME)) {
			this.name = VariableNames.FORIDENT_SUSPECTLIST_NAME;
			this.initForIdentSuspectList();
		}
		else {
			File file = new File(filename);
			this.name = file.getName();
			this.initialise(file);
		}
	}

	public SuspectList(String filename, String name) {
		File file = new File(filename);
		this.name = name;
		this.initialise(file);
	}

	public SuspectList(java.io.InputStream is, String name) {
		this.name = name;
		this.initialise(is);
	}
	
	protected void initForIdentSuspectList() {
		ForIdentRestWebService firws = new ForIdentRestWebService();
		try {
			java.util.Vector<String> inchikeys = firws.getInChIKeys();
			for(int i = 0; i < inchikeys.size(); i++) {
				try {
					this.list.add(inchikeys.get(i).split("-")[0]);
				}
				catch(Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void initialise(File file) {
		if(!file.exists()) {
			return;
		}
		if(!file.canRead()) {
			return;
		}
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(file));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("-");
				if(tmp.length < 1 || tmp[0] == null || tmp[0].length() == 0) continue;
				this.list.add(tmp[0]);
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	protected void initialise(java.io.InputStream is) {
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("-");
				if(tmp.length < 1 || tmp[0] == null || tmp[0].length() == 0) continue;
				this.list.add(tmp[0]);
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public String getName() {
		return this.name;
	}
}
