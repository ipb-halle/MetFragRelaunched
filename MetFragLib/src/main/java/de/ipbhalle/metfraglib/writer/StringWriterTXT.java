package de.ipbhalle.metfraglib.writer;

import java.io.IOException;

import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.parameter.Constants;

public class StringWriterTXT implements IWriter {

	public boolean write(IList list, String filename, String path) {
		try {
			java.io.BufferedWriter bwriter = 
					new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".txt")));
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

}
