package de.ipbhalle.metfragweb.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.primefaces.model.file.UploadedFile;

import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfragweb.container.Messages;

public class FileStorer {

	public FileStorer() {
		
	}
	
	public File compressFolder(String folderName, String location, String[] excludeRegex) throws Exception {
		FileOutputStream fos = new FileOutputStream(location);
		java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);
		
		java.util.Vector<String> files = new java.util.Vector<String>();
		this.getFilesOfFolder(new File(folderName), files, excludeRegex);
		
		for(int i = 0; i < files.size(); i++) {
			byte[] buffer = new byte[1024];
			java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry(files.get(i));
			zos.putNextEntry(ze);
			FileInputStream inFile = new FileInputStream(files.get(i));
			int len;
			while ((len = inFile.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			inFile.close();
		}
		zos.close();
		return new File(location);
	}

	public File compressFolder(String folderName, String location, String[] excludeRegex, String prefixToRemove) throws Exception {
		FileOutputStream fos = new FileOutputStream(location);
		java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);
		
		java.util.Vector<String> files = new java.util.Vector<String>();
		this.getFilesOfFolder(new File(folderName), files, excludeRegex);
		
		for(int i = 0; i < files.size(); i++) {
			byte[] buffer = new byte[1024];
			java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry(files.get(i).replaceFirst(".*" + prefixToRemove, ""));
			zos.putNextEntry(ze);
			FileInputStream inFile = new FileInputStream(files.get(i));
			int len;
			while ((len = inFile.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			inFile.close();
		}
		zos.close();
		return new File(location);
	}
	
	protected void getFilesOfFolder(File folder, java.util.Vector<String> files, String[] excludeRegex) {
		for(int i = 0; i < excludeRegex.length; i++) {
			Pattern p = Pattern.compile(excludeRegex[i]);
			Matcher m = p.matcher(folder.getAbsolutePath());
			if(m.matches()) return;
		}
		if(folder.isDirectory()) this.getFilesOfFolder(folder.listFiles(), files, excludeRegex);
		else files.add(folder.getAbsolutePath());
	}

	protected void getFilesOfFolder(File[] folderFiles, java.util.Vector<String> files, String[] excludeRegex) {
		for(int i = 0; i < folderFiles.length; i++) {
			boolean regexFound = false;
			for(int j = 0; j < excludeRegex.length; j++) { 
				Pattern p = Pattern.compile(excludeRegex[j]);
				Matcher m = p.matcher(folderFiles[i].getAbsolutePath());
				if(m.matches()) regexFound = true;
			}
			if(regexFound) continue;
			if(folderFiles[i].isDirectory()) this.getFilesOfFolder(folderFiles[i].listFiles(), files, excludeRegex);
			else files.add(folderFiles[i].getAbsolutePath());
		}
	}
	
	public File[] decompressZipFile(java.util.zip.ZipFile zipFile, File destination) throws IOException {
		java.util.Enumeration<?> zipEntriesEnum = zipFile.entries();
		java.util.List<File> fileList = new java.util.LinkedList<File>();
		while(zipEntriesEnum.hasMoreElements()) {
			java.util.zip.ZipEntry entry = (java.util.zip.ZipEntry)zipEntriesEnum.nextElement();
			InputStream inputStream = zipFile.getInputStream(entry);
			fileList.add(this.saveUploadedFile(inputStream, destination, entry.getName()));
		}
		File[] contentFiles = new File[fileList.size()];
		int index = 0; 
		for(File file : fileList) {
			contentFiles[index] = file;
			index++;
		}	
		return contentFiles;
	}
	
	/**
	 * 
	 * @param folderName
	 * @param errorMessages
	 * @return
	 */
	public File prepareFolder(String folderName, Messages errorMessages) {
		File toDownloadTo = null;
		try {
			toDownloadTo = new File(folderName);
			if (!toDownloadTo.exists())
				toDownloadTo.mkdirs();
		} catch (Exception e1) {
			errorMessages.setMessage("candidateFileError", "Error saving file.");
			return null;
		}
		return toDownloadTo;
	}
	
	/**
	 * 
	 * @param file
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public File saveUploadedFile(UploadedFile file, File path) throws IOException {
		File fileToStore = new File(path.getAbsoluteFile() + Constants.OS_SPECIFIC_FILE_SEPARATOR + file.getFileName());
		InputStream is = file.getInputStream();
		OutputStream out = new FileOutputStream(fileToStore);
		byte buf[] = new byte[1024];
	    int len;
	    while ((len = is.read(buf)) > 0)
	        out.write(buf, 0, len);
	    is.close();
	    out.close();
		return fileToStore;
	}

	/**
	 * 
	 * @param is
	 * @param path
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public File saveUploadedFile(InputStream is, File path, String fileName) throws IOException {
		File file = new File(path.getAbsoluteFile() + Constants.OS_SPECIFIC_FILE_SEPARATOR + fileName);
		File parent = file.getParentFile();
		if(!parent.exists()) parent.mkdirs();
		OutputStream out = new FileOutputStream(file);
		byte buf[] = new byte[1024];
	    int len;
	    while ((len = is.read(buf)) > 0)
	        out.write(buf, 0, len);
	    out.close();
		return file;
	}
	
	/**
	 * 
	 * @param file
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public File saveUploadedFile(File file, File path) throws IOException {
		File fileToStore = new File(path.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + file.getName());
		InputStream is = new FileInputStream(file);
		OutputStream out = new FileOutputStream(fileToStore);
		byte buf[] = new byte[1024];
	    int len;
	    while ((len = is.read(buf)) > 0)
	        out.write(buf, 0, len);
	    is.close();
	    out.close();
		return fileToStore;
	}
}
