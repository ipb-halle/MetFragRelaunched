package de.ipbhalle.metfragweb.datatype;

public class SuspectListFileContainer extends
		java.util.ArrayList<UploadedSuspectListFile> {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -3741937850910152945L;
	protected java.util.Vector<UploadedSuspectListFile> files;

	public SuspectListFileContainer() {
		this.files = new java.util.Vector<UploadedSuspectListFile>();
	}

	public void addFile(UploadedSuspectListFile file) throws Exception {
		java.io.File currentFile = new java.io.File(file.getAbsolutePath());
		if (currentFile.exists() && currentFile.canRead()) {
			int numberEntries = 0;
			java.io.BufferedReader breader = new java.io.BufferedReader(
					new java.io.FileReader(currentFile));
			String line = "";
			while ((line = breader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.length() == 0)
					continue;
				numberEntries++;
			}
			breader.close();
			file.setNumberEntries(numberEntries);
			this.files.add(file);
		} else {
			throw new Exception();
		}
		return;
	}

	public String[] getAbsoluteFileNamesAsString() {
		String[] fileNames = new String[this.files.size()];
		for (int i = 0; i < fileNames.length; i++)
			fileNames[i] = this.files.get(i).getAbsolutePath();
		return fileNames;
	}

	public void removeById(int id) {
		for (int i = 0; i < this.files.size(); i++)
			if (this.files.get(i).getId() == id) {
				this.files.remove(i);
				return;
			}
	}
	
	public void removeByName(String name) {
		for (int i = 0; i < this.files.size(); i++)
			if (this.files.get(i).getName().equals(name)) {
				this.files.remove(i);
				return;
			}
	}

	public boolean contains(UploadedSuspectListFile file) {
		for (int i = 0; i < this.files.size(); i++)
			if (this.files.get(i).getName().equals(file.getName()))
				return true;
		return false;
	}

	@Override
	public UploadedSuspectListFile get(int index) {
		return this.files.get(index);
	}

	@Override
	public java.util.Iterator<UploadedSuspectListFile> iterator() {
		return this.files.iterator();
	}

	@Override
	public boolean isEmpty() {
		return this.files.isEmpty();
	}

	@Override
	public java.util.ListIterator<UploadedSuspectListFile> listIterator() {
		return this.files.listIterator();
	}

	@Override
	public java.util.ListIterator<UploadedSuspectListFile> listIterator(
			int index) {
		return this.files.listIterator(index);
	}

	@Override
	public UploadedSuspectListFile remove(int index) {
		return this.files.remove(index);
	}

	@Override
	public boolean remove(Object file) {
		return this.files.remove(file);
	}

	@Override
	public int size() {
		return this.files.size();
	}

	@Override
	public Object[] toArray() {
		return this.files.toArray();
	}

	public int getTotelFileNumber() {
		return this.files.size();
	}

}
