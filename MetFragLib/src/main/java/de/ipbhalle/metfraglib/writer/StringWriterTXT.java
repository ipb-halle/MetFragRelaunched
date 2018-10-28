package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.IOException;

import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.settings.Settings;

public class StringWriterTXT implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean writeFile(File file, IList list, Settings settings) {
		try {
			java.io.BufferedWriter bwriter = 
					new java.io.BufferedWriter(new java.io.FileWriter(file));
			for(int i = 0; i < list.getNumberElements(); i++) {
				bwriter.write((String)list.getElement(i));
				bwriter.newLine();
			}
			bwriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean write(String element, String filename, String path) {
		try {
			java.io.BufferedWriter bwriter = 
					new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".txt")));
			bwriter.write(element);
			bwriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void nullify() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".txt"), list, null);
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), list, null);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}

}
