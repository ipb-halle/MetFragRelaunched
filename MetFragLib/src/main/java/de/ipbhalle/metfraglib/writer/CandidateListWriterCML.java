package de.ipbhalle.metfraglib.writer;

import java.io.IOException;

import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.settings.Settings;

public class CandidateListWriterCML implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws IOException {
		return this.write(list, filename, path);
	}
	
	public boolean write(IList list, String filename, String path) {
		// TODO Auto-generated method stub
		return false;
	}

	public void nullify() {
		// TODO Auto-generated method stub
		
	}

}
