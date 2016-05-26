package de.ipbhalle.metfraglib.writer;

import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.settings.Settings;

public class FragmentListWriterCML implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean write(IList list, String filename, String path) {

		return false;
	}

	public void nullify() {
		// TODO Auto-generated method stub
		
	}

}
