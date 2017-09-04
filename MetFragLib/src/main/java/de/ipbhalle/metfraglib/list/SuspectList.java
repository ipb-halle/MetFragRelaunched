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
		else if(filename.equals(VariableNames.DSSTOX_SUSPECTLIST_NAME)) {
			java.io.InputStream is = SuspectList.class.getResourceAsStream("/" + VariableNames.DSSTOX_SUSPECTLIST_FILE_NAME);
			this.name = VariableNames.DSSTOX_SUSPECTLIST_NAME;
			this.initialise(is, true);
		}
		else {
			File file = new File(filename);
			this.name = file.getName();
			this.initialise(file, false);
		}
	}

	public SuspectList(String filename, boolean isPrefiltered) {
		if(filename.equals(VariableNames.FORIDENT_SUSPECTLIST_NAME)) {
			this.name = VariableNames.FORIDENT_SUSPECTLIST_NAME;
			this.initForIdentSuspectList();
		}
		else if(filename.equals(VariableNames.DSSTOX_SUSPECTLIST_NAME)) {
			java.io.InputStream is = SuspectList.class.getResourceAsStream("/" + VariableNames.DSSTOX_SUSPECTLIST_FILE_NAME);
			this.name = VariableNames.DSSTOX_SUSPECTLIST_NAME;
			this.initialise(is, isPrefiltered);
		}
		else {
			File file = new File(filename);
			this.name = file.getName();
			this.initialise(file, isPrefiltered);
		}
	}

	public SuspectList(String filename, String name) {
		File file = new File(filename);
		this.name = name;
		this.initialise(file, false);
	}

	public SuspectList(String filename, String name, boolean isPrefiltered) {
		File file = new File(filename);
		this.name = name;
		this.initialise(file, isPrefiltered);
	}

	public SuspectList(java.io.InputStream is, String name) {
		this.name = name;
		this.initialise(is, false);
	}

	public SuspectList(java.io.InputStream is, String name, boolean isPrefiltered) {
		this.name = name;
		this.initialise(is, isPrefiltered);
	}

	public SuspectList(String[] inChIKeys, String name, boolean isPrefiltered) {
		this.name = name;
		this.initialise(inChIKeys, isPrefiltered);
	}
	
	protected void initForIdentSuspectList() {
		ForIdentRestWebService firws = new ForIdentRestWebService();
		try {
			java.util.ArrayList<String> inchikeys = firws.getInChIKeys();
			for(int i = 0; i < inchikeys.size(); i++) {
				try {
					int index = 0;
					String currentInChIKey = inchikeys.get(i).split("-")[0];
					while(index < this.list.size() && ((String)this.list.get(index)).compareTo(currentInChIKey) < 0) {
						index++;
					}
					this.list.add(index, currentInChIKey);
				}
				catch(Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * init by string array
	 * 
	 * @param inChIKeys
	 * @param isPrefiltered
	 */
	protected void initialise(String[] inChIKeys, boolean isPrefiltered) {
		for(int i = 0; i < inChIKeys.length; i++) {
			String[] tmp = inChIKeys[i].split("-");
			if(tmp.length < 1 || tmp[0] == null || tmp[0].length() == 0) continue;
			if(!isPrefiltered) {
				int index = 0;
				while(index < this.list.size() && ((String)this.list.get(index)).compareTo(tmp[0]) < 0) {
					index++;
				}
				this.list.add(index, tmp[0]);
			}
			else this.list.add(tmp[0]);
		}
	}
	
	/**
	 * init by file
	 * 
	 * @param file
	 * @param isPrefiltered
	 */
	protected void initialise(File file, boolean isPrefiltered) {
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
				if(!isPrefiltered) {
					int index = 0;
					while(index < this.list.size() && ((String)this.list.get(index)).compareTo(tmp[0]) < 0) {
						index++;
					}
					this.list.add(index, tmp[0]);
				}
				else this.list.add(tmp[0]);
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
	
	/**
	 * init by input stream
	 * 
	 * @param is
	 * @param isPrefiltered
	 */
	protected void initialise(java.io.InputStream is, boolean isPrefiltered) {
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("-");
				if(tmp.length < 1 || tmp[0] == null || tmp[0].length() == 0) continue;
				if(!isPrefiltered) {
					int index = 0;
					while(index < this.list.size() && ((String)this.list.get(index)).compareTo(tmp[0]) < 0) {
						index++;
					}
					this.list.add(index, tmp[0]);
				}
				else this.list.add(tmp[0]);
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
	
	public boolean contains(String key) {
		for(int i = 0; i < this.list.size(); i++) {
			String current = (String)this.list.get(i);
			int compare = current.compareTo(key);
			if(compare == 0) return true;
			if(compare > 0) return false;
		}
		return false;
	}
	
	public String getName() {
		return this.name;
	}
}
